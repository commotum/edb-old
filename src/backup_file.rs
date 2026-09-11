//! Admit repository files before allocating their bodies. Cache budgets bound
//! retained data, not the size of a legal value. Use the actual codec ceilings;
//! authenticate formats without a ceiling in constant scratch space first.
use super::{Digest, SemanticError, fault, io_error};
use sha2::{Digest as _, Sha256};
use std::fs::File;
use std::io::{Read, Seek};
use std::path::Path;

const SCRATCH_BYTES: usize = 64 * 1024;
// Fixed lineage UUID (u32 length + 36 bytes), coordinates, digests, option tags.
const CLAIM_BYTES: u64 = 110;
const MAX_ROOT_BYTES: u64 = 46 + 40 + 16 + 5 * 32 + 2 * 33;

fn open_file(path: &Path) -> Result<(File, u64), SemanticError> {
    let file = File::open(path).map_err(io_error("backup/file-read"))?;
    let metadata = file.metadata().map_err(io_error("backup/file-metadata"))?;
    if !metadata.is_file() {
        return Err(fault("backup/file-type", "backup file is not regular"));
    }
    Ok((file, metadata.len()))
}

fn invalid_size() -> SemanticError {
    fault(
        "backup/file-size",
        "backup file size exceeds its format limit or disagrees with its header",
    )
}

fn prefix(file: &mut File, len: u64) -> Result<Vec<u8>, SemanticError> {
    let mut bytes = vec![0; len.min(16) as usize];
    file.read_exact(&mut bytes)
        .map_err(io_error("backup/file-header"))?;
    file.rewind().map_err(io_error("backup/file-seek"))?;
    Ok(bytes)
}

fn envelope_size(len: u64, body: u64, overhead: u64, limit: u64) -> Result<(), SemanticError> {
    if len > limit || body.checked_add(overhead) != Some(len) {
        return Err(invalid_size());
    }
    Ok(())
}

/// Never follows a growing file past the admitted length, including in the
/// authentication pass. The extra byte detects growth without allocating it.
fn require_eof(reader: &mut impl Read) -> Result<(), SemanticError> {
    let mut extra = [0];
    if reader
        .read(&mut extra)
        .map_err(io_error("backup/file-read"))?
        != 0
    {
        return Err(invalid_size());
    }
    Ok(())
}

fn read_admitted(reader: &mut impl Read, len: u64) -> Result<Vec<u8>, SemanticError> {
    let len = usize::try_from(len).map_err(|_| invalid_size())?;
    let mut bytes = Vec::new();
    bytes.try_reserve_exact(len).map_err(|_| {
        fault(
            "backup/file-allocation",
            "cannot allocate the admitted backup object",
        )
    })?;
    bytes.resize(len, 0);
    reader
        .read_exact(&mut bytes)
        .map_err(io_error("backup/file-read"))?;
    require_eof(reader)?;
    Ok(bytes)
}

fn hash_prefix(reader: &mut impl Read, mut len: u64) -> Result<Digest, SemanticError> {
    let mut hash = Sha256::new();
    let mut scratch = [0; SCRATCH_BYTES];
    while len != 0 {
        let count = len.min(scratch.len() as u64) as usize;
        reader
            .read_exact(&mut scratch[..count])
            .map_err(io_error("backup/file-read"))?;
        hash.update(&scratch[..count]);
        len -= count as u64;
    }
    Ok(hash.finalize().into())
}

pub(super) fn read_object_file(path: &Path, expected: Digest) -> Result<Vec<u8>, SemanticError> {
    let (mut file, len) = open_file(path)?;
    let header = prefix(&mut file, len)?;
    match header.get(..4) {
        Some(b"ATIX") if header.len() == 16 => envelope_size(
            len,
            u32::from_be_bytes(header[12..16].try_into().unwrap()) as u64,
            48,
            crate::persistent_tree::MAX_TREE_NODE_BYTES as u64,
        )?,
        Some(b"ATIM") if header.len() >= 12 => envelope_size(
            len,
            u32::from_be_bytes(header[8..12].try_into().unwrap()) as u64,
            44,
            crate::tree_manifest::MAX_MANIFEST_BYTES as u64,
        )?,
        Some(b"ATMC" | b"ATLC") if header.len() == 16 => {
            let limit = if header.starts_with(b"ATLC") {
                crate::log_generation::MAX_ENVELOPE_BYTES
            } else {
                crate::encoding::MAX_BLOB_BYTES
            };
            envelope_size(
                len,
                u64::from_be_bytes(header[8..16].try_into().unwrap()),
                48,
                limit as u64,
            )?;
        }
        Some(b"ATLX") if len != 118 => return Err(invalid_size()),
        // Header + length-prefixed lineage UUID + two coordinates + previous
        // object digest + checksum. The decoder's minimum is not its size.
        Some(b"ATEX") if len != 6 + 40 + 16 + 32 + 32 => return Err(invalid_size()),
        Some(b"ATLX" | b"ATEX") => {}
        _ => {
            // Receipt names/legacy carriers have no codec-wide size ceiling.
            // Preserve those valid values, but never allocate a corrupt file
            // merely because its metadata claims it is large. Hash again after
            // loading at the caller, so this is not a replacement for admission
            // authentication if a file changes between passes.
            if hash_prefix(&mut file, len)? != expected {
                return Err(fault(
                    "backup/object-corrupt",
                    "backup object failed its hash",
                ));
            }
            require_eof(&mut file)?;
            file.rewind().map_err(io_error("backup/file-seek"))?;
        }
    }
    read_admitted(&mut file, len)
}

pub(super) fn read_claim_file(path: &Path) -> Result<Vec<u8>, SemanticError> {
    let (mut file, len) = open_file(path)?;
    if len != CLAIM_BYTES {
        return Err(invalid_size());
    }
    read_admitted(&mut file, len)
}

pub(super) fn read_manifest_file(path: &Path) -> Result<Vec<u8>, SemanticError> {
    let (mut file, len) = open_file(path)?;
    let header = prefix(&mut file, len)?;
    if header.len() < 14 || !header.starts_with(super::MAGIC) {
        return Err(fault(
            "backup/manifest-header",
            "backup manifest header is invalid",
        ));
    }
    let version = u16::from_be_bytes(header[4..6].try_into().unwrap());
    if !matches!(version, super::LEGACY_VERSION | super::VERSION) {
        return Err(SemanticError::new(
            crate::ErrorCategory::Unsupported,
            "backup/manifest-version",
            "unsupported backup manifest version; use a current-version backup",
        ));
    }
    let limit = if version == super::VERSION {
        MAX_ROOT_BYTES
    } else {
        u64::MAX
    };
    envelope_size(
        len,
        u64::from_be_bytes(header[6..14].try_into().unwrap()),
        46,
        limit,
    )?;
    if version != super::VERSION {
        // Existing legacy roots contain growing flat lists. Do not invent a
        // size cap for them, or allocate their body before checksum admission.
        let hash = hash_prefix(&mut file, len - 32)?;
        let mut checksum = [0; 32];
        file.read_exact(&mut checksum)
            .map_err(io_error("backup/file-read"))?;
        require_eof(&mut file)?;
        if hash != checksum {
            return Err(fault(
                "backup/manifest-checksum",
                "backup manifest checksum is invalid",
            ));
        }
        file.rewind().map_err(io_error("backup/file-seek"))?;
    }
    read_admitted(&mut file, len)
}

pub(super) fn file_matches(path: &Path, expected: &[u8]) -> Result<bool, SemanticError> {
    let (mut file, len) = open_file(path)?;
    if len != expected.len() as u64 {
        return Ok(false);
    }
    let mut scratch = [0; SCRATCH_BYTES];
    for chunk in expected.chunks(SCRATCH_BYTES) {
        file.read_exact(&mut scratch[..chunk.len()])
            .map_err(io_error("backup/read-existing"))?;
        if &scratch[..chunk.len()] != chunk {
            return Ok(false);
        }
    }
    require_eof(&mut file)?;
    Ok(true)
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::{Cursor, Write};

    #[test]
    fn completed_excision_files_accept_the_full_canonical_encoding() {
        let record = super::super::BackupCompletedExcision {
            lineage_id: "82d578ad-21e1-436e-8b9f-7039dd9d87d5".into(),
            request_t: 7,
            request_entity: 12345,
            previous_hash: [3; 32],
        };
        let bytes = super::super::encode_completed_excision(&record).unwrap();
        let mut file = tempfile::NamedTempFile::new().unwrap();
        file.write_all(&bytes).unwrap();
        assert_eq!(
            read_object_file(file.path(), crate::sha256(&bytes)).unwrap(),
            bytes
        );
    }

    #[test]
    fn admitted_reads_reject_growth_without_following_it() {
        let mut source = Cursor::new(vec![42; 1024]);
        assert_eq!(
            read_admitted(&mut source, 12).unwrap_err().code,
            "backup/file-size"
        );
        assert_eq!(source.position(), 13);
        let mut truncated = Cursor::new(vec![42; 11]);
        assert!(read_admitted(&mut truncated, 12).is_err());
    }

    #[test]
    fn sparse_objects_roots_claims_and_existing_files_are_admitted_before_allocation() {
        let mut file = tempfile::NamedTempFile::new().unwrap();
        // A native header with a forged, hash-valid-size claim must still obey
        // the native codec limit. A sparse fixture costs no 96 MiB allocation.
        let len = 96 * 1024 * 1024u64;
        let mut header = [0; 16];
        header[..4].copy_from_slice(b"ATIX");
        header[12..16].copy_from_slice(&((len - 48) as u32).to_be_bytes());
        file.write_all(&header).unwrap();
        file.as_file().set_len(len).unwrap();
        assert_eq!(
            read_object_file(file.path(), [0; 32]).unwrap_err().code,
            "backup/file-size"
        );
        assert_eq!(
            read_claim_file(file.path()).unwrap_err().code,
            "backup/file-size"
        );
        assert!(!file_matches(file.path(), b"small expected object").unwrap());
        header[..4].copy_from_slice(b"ATBK");
        header[4..6].copy_from_slice(&super::super::VERSION.to_be_bytes());
        header[6..14].copy_from_slice(&(len - 46).to_be_bytes());
        file.as_file_mut().rewind().unwrap();
        file.write_all(&header).unwrap();
        assert_eq!(
            read_manifest_file(file.path()).unwrap_err().code,
            "backup/file-size"
        );
        header[4..6].copy_from_slice(&99u16.to_be_bytes());
        file.as_file_mut().rewind().unwrap();
        file.write_all(&header).unwrap();
        assert_eq!(
            read_manifest_file(file.path()).unwrap_err().code,
            "backup/manifest-version"
        );
    }
}
