//! Fixed-size, portable SHA-256 checkpoints for bounded archive node-set hashing.
//!
//! This preserves the existing v1 digest exactly. Checkpoints are administrative
//! progress, not independent authenticity evidence: their SQL owner must enforce
//! the same immutable source and sorted-node cursor across resumptions.
use crate::{Digest, SemanticError};

const DOMAIN: &[u8] = b"atomic/request-base-archive-node-set/v1\0";
const MAGIC: &[u8; 5] = b"ASHC1";
const CHECKPOINT_BYTES: usize = 5 + 8 + 32 + 1 + 64;
const INITIAL: [u32; 8] = [
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19,
];

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct ReceiptArchiveHash {
    state: [u32; 8],
    length: u64,
    buffer: [u8; 64],
    buffered: u8,
}

impl ReceiptArchiveHash {
    pub(crate) fn new(total_node_count: u64) -> Self {
        let mut result = Self {
            state: INITIAL,
            length: 0,
            buffer: [0; 64],
            buffered: 0,
        };
        result.update(DOMAIN).expect("fixed domain length");
        result
            .update(&total_node_count.to_be_bytes())
            .expect("fixed count length");
        result
    }

    pub(crate) fn update(&mut self, mut bytes: &[u8]) -> Result<(), SemanticError> {
        // SHA-256's terminal bit length is an unsigned 64-bit integer.
        let length = self
            .length
            .checked_add(bytes.len() as u64)
            .filter(|length| *length <= u64::MAX / 8)
            .ok_or_else(|| invalid("checkpoint input exceeds SHA-256's byte-length limit"))?;
        self.length = length;
        if self.buffered != 0 {
            let buffered = usize::from(self.buffered);
            let copied = bytes.len().min(64 - buffered);
            self.buffer[buffered..buffered + copied].copy_from_slice(&bytes[..copied]);
            self.buffered += copied as u8;
            bytes = &bytes[copied..];
            if self.buffered == 64 {
                sha2::compress256(&mut self.state, &[self.buffer.into()]);
                self.buffer = [0; 64];
                self.buffered = 0;
            }
        }
        while bytes.len() >= 64 {
            let block: [u8; 64] = bytes[..64].try_into().expect("complete block");
            sha2::compress256(&mut self.state, &[block.into()]);
            bytes = &bytes[64..];
        }
        if !bytes.is_empty() {
            self.buffer[..bytes.len()].copy_from_slice(bytes);
            self.buffered = bytes.len() as u8;
        }
        Ok(())
    }

    pub(crate) fn encode(&self) -> Vec<u8> {
        let mut bytes = Vec::with_capacity(CHECKPOINT_BYTES);
        bytes.extend_from_slice(MAGIC);
        bytes.extend_from_slice(&self.length.to_be_bytes());
        for word in self.state {
            bytes.extend_from_slice(&word.to_be_bytes());
        }
        bytes.push(self.buffered);
        bytes.extend_from_slice(&self.buffer);
        bytes
    }

    /// Bytes after the fixed domain/count prefix; bind this to the durable
    /// sorted-hash cursor (`hashed_nodes * 32`) before resuming a checkpoint.
    pub(crate) fn processed_bytes(&self) -> u64 {
        self.length - (DOMAIN.len() + 8) as u64
    }

    pub(crate) fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() != CHECKPOINT_BYTES || &bytes[..5] != MAGIC {
            return Err(invalid("checkpoint size or format is invalid"));
        }
        let length = u64::from_be_bytes(bytes[5..13].try_into().expect("length width"));
        let mut state = [0; 8];
        for (word, encoded) in state.iter_mut().zip(bytes[13..45].chunks_exact(4)) {
            *word = u32::from_be_bytes(encoded.try_into().expect("word width"));
        }
        let buffered = bytes[45];
        let buffer: [u8; 64] = bytes[46..].try_into().expect("buffer width");
        if length < (DOMAIN.len() + 8) as u64
            || length > u64::MAX / 8
            || buffered >= 64
            || u64::from(buffered) != length % 64
            || buffer[usize::from(buffered)..]
                .iter()
                .any(|byte| *byte != 0)
            || (length < 64 && state != INITIAL)
            || (length < 64 && &buffer[..DOMAIN.len()] != DOMAIN)
        {
            return Err(invalid(
                "checkpoint length, block state, or padding is invalid",
            ));
        }
        Ok(Self {
            state,
            length,
            buffer,
            buffered,
        })
    }

    pub(crate) fn finalize(mut self) -> Digest {
        let buffered = usize::from(self.buffered);
        self.buffer[buffered] = 0x80;
        if buffered >= 56 {
            sha2::compress256(&mut self.state, &[self.buffer.into()]);
            self.buffer = [0; 64];
        }
        self.buffer[56..].copy_from_slice(&(self.length * 8).to_be_bytes());
        sha2::compress256(&mut self.state, &[self.buffer.into()]);
        let mut digest = [0; 32];
        for (encoded, word) in digest.chunks_exact_mut(4).zip(self.state) {
            encoded.copy_from_slice(&word.to_be_bytes());
        }
        digest
    }
}

fn invalid(message: &'static str) -> SemanticError {
    SemanticError::incorrect("operations/archive-hash-checkpoint", message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use sha2::{Digest as _, Sha256};

    fn reference(count: u64, input: &[u8]) -> Digest {
        let mut digest = Sha256::new();
        digest.update(DOMAIN);
        digest.update(count.to_be_bytes());
        digest.update(input);
        digest.finalize().into()
    }

    #[test]
    fn arbitrary_block_padding_chunk_and_checkpoint_boundaries_match_sha256() {
        for length in 0..=513 {
            let input: Vec<_> = (0..length).map(|index| (index * 73 + 19) as u8).collect();
            for chunk in [1, 2, 7, 31, 32, 55, 56, 63, 64, 65, 127, 513] {
                let mut digest = ReceiptArchiveHash::new(1234);
                digest = ReceiptArchiveHash::decode(&digest.encode()).unwrap();
                let mut processed = 0;
                for bytes in input.chunks(chunk) {
                    digest.update(bytes).unwrap();
                    processed += bytes.len();
                    assert_eq!(digest.encode().len(), CHECKPOINT_BYTES);
                    let resumed = ReceiptArchiveHash::decode(&digest.encode()).unwrap();
                    assert_eq!(resumed, digest);
                    assert_eq!(resumed.processed_bytes(), processed as u64);
                    digest = resumed;
                }
                assert_eq!(
                    digest.finalize(),
                    reference(1234, &input),
                    "length={length} chunk={chunk}"
                );
            }
        }
    }

    #[test]
    fn sorted_archive_hashes_resume_to_existing_v1_digest() {
        let hashes: std::collections::BTreeSet<Digest> = (0u64..1000)
            .map(|index| Sha256::digest(index.to_be_bytes()).into())
            .collect();
        let mut expected = Sha256::new();
        expected.update(DOMAIN);
        expected.update((hashes.len() as u64).to_be_bytes());
        let mut actual = ReceiptArchiveHash::new(hashes.len() as u64);
        for hash in hashes {
            expected.update(hash);
            actual.update(&hash).unwrap();
            actual = ReceiptArchiveHash::decode(&actual.encode()).unwrap();
        }
        assert_eq!(actual.finalize(), <Digest>::from(expected.finalize()));
    }

    #[test]
    fn malformed_checkpoints_and_overflow_fail_without_mutating_state() {
        let valid = ReceiptArchiveHash::new(9).encode();
        for length in 0..valid.len() {
            assert!(ReceiptArchiveHash::decode(&valid[..length]).is_err());
        }
        let mut bytes = valid.clone();
        bytes.push(0);
        assert!(ReceiptArchiveHash::decode(&bytes).is_err());
        for offset in [0, 5, 13, 45, 109] {
            let mut bytes = valid.clone();
            bytes[offset] ^= 0xff;
            assert!(
                ReceiptArchiveHash::decode(&bytes).is_err(),
                "offset={offset}"
            );
        }
        let mut digest = ReceiptArchiveHash {
            state: INITIAL,
            length: u64::MAX / 8,
            buffer: [0; 64],
            buffered: 63,
        };
        let original = digest.clone();
        assert!(digest.update(&[0]).is_err());
        assert_eq!(digest, original);
    }
}
