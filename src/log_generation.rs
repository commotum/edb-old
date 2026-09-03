//! Canonical envelopes for lineage-bound copy-on-write log generations.
//!
//! Format-3 `DurableTransaction` values predate stable database lineage and
//! bind their `database_id` field to the operator-facing name.  Those bytes
//! remain the exact generation-zero representation.  Every newly constructed
//! generation instead stores a format-1 `ATLG` envelope which authenticates
//! the immutable lineage and physical generation as well as one canonical
//! format-3 transaction.  The nested transaction names the lineage too, so a
//! decoder cannot accidentally expose a renamed alias as durable identity.

use crate::{Digest, DurableTransaction, ErrorCategory, SemanticError};
use crate::{decode_transaction, encode_transaction, sha256};

const MAGIC: &[u8; 4] = b"ATLG";
const VERSION: u16 = 1;
const KIND_TRANSACTION: u8 = 1;
const HEADER_LEN: usize = 20;
const CHECKSUM_LEN: usize = 32;
const MAX_LINEAGE_BYTES: usize = 64;
// The nested format accepts a 64 MiB body plus its 48-byte header/checksum.
// The lineage wrapper adds at most 122 bytes. Keeping both terms explicit
// prevents a generation cutover from imposing a smaller transaction limit.
const MAX_CANONICAL_TRANSACTION_BYTES: usize = 64 * 1024 * 1024 + 16 + 32;
const MAX_ENVELOPE_BYTES: usize =
    MAX_CANONICAL_TRANSACTION_BYTES + HEADER_LEN + 2 + MAX_LINEAGE_BYTES + 4 + CHECKSUM_LEN;

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct LineageTransaction {
    pub(crate) lineage_id: String,
    pub(crate) generation: u64,
    pub(crate) transaction: DurableTransaction,
}

impl LineageTransaction {
    pub(crate) fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        validate(self)?;
        let transaction = encode_transaction(&self.transaction)?;
        let lineage = self.lineage_id.as_bytes();
        let lineage_len = u16::try_from(lineage.len()).map_err(|_| {
            incorrect(
                "generation/lineage-length",
                "database lineage exceeds the canonical envelope limit",
            )
        })?;
        let transaction_len = u32::try_from(transaction.len()).map_err(|_| {
            incorrect(
                "generation/transaction-length",
                "canonical transaction exceeds the generation envelope limit",
            )
        })?;
        let body_len = 6_usize
            .checked_add(lineage.len())
            .and_then(|length| length.checked_add(transaction.len()))
            .ok_or_else(|| {
                incorrect(
                    "generation/envelope-length",
                    "generation envelope length overflows usize",
                )
            })?;
        let body_len = u32::try_from(body_len).map_err(|_| {
            incorrect(
                "generation/envelope-length",
                "generation envelope body exceeds u32",
            )
        })?;

        let mut bytes = Vec::with_capacity(HEADER_LEN + body_len as usize + CHECKSUM_LEN);
        bytes.extend_from_slice(MAGIC);
        bytes.extend_from_slice(&VERSION.to_be_bytes());
        bytes.push(KIND_TRANSACTION);
        bytes.push(0);
        bytes.extend_from_slice(&self.generation.to_be_bytes());
        bytes.extend_from_slice(&body_len.to_be_bytes());
        bytes.extend_from_slice(&lineage_len.to_be_bytes());
        bytes.extend_from_slice(lineage);
        bytes.extend_from_slice(&transaction_len.to_be_bytes());
        bytes.extend_from_slice(&transaction);
        let checksum = sha256(&bytes);
        bytes.extend_from_slice(&checksum);
        if bytes.len() > MAX_ENVELOPE_BYTES {
            return Err(incorrect(
                "generation/envelope-length",
                format!("generation envelope exceeds {MAX_ENVELOPE_BYTES} bytes"),
            ));
        }
        Ok(bytes)
    }

    pub(crate) fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() < HEADER_LEN + 2 + 4 + CHECKSUM_LEN || bytes.len() > MAX_ENVELOPE_BYTES {
            return Err(fault(
                "generation/envelope-length",
                "generation envelope has an invalid encoded size",
            ));
        }
        if bytes.get(..4) != Some(MAGIC.as_slice()) {
            return Err(fault(
                "generation/envelope-magic",
                "generation envelope has the wrong magic",
            ));
        }
        let version = u16::from_be_bytes(bytes[4..6].try_into().expect("checked header"));
        if version != VERSION {
            return Err(fault(
                "generation/envelope-version",
                format!("generation envelope version {version} is unsupported"),
            ));
        }
        if bytes[6] != KIND_TRANSACTION || bytes[7] != 0 {
            return Err(fault(
                "generation/envelope-header",
                "generation envelope kind or reserved byte is invalid",
            ));
        }
        let generation = u64::from_be_bytes(bytes[8..16].try_into().expect("checked header"));
        let body_len = u32::from_be_bytes(bytes[16..20].try_into().expect("checked header"));
        let checksum_at = 20_usize.checked_add(body_len as usize).ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "generation envelope length overflows usize",
            )
        })?;
        if checksum_at.checked_add(CHECKSUM_LEN) != Some(bytes.len())
            || sha256(&bytes[..checksum_at]) != bytes[checksum_at..]
        {
            return Err(fault(
                "generation/envelope-checksum",
                "generation envelope length or checksum is invalid",
            ));
        }

        let mut cursor = 20_usize;
        let lineage_len = read_u16(bytes, &mut cursor, checksum_at)? as usize;
        if lineage_len > MAX_LINEAGE_BYTES {
            return Err(fault(
                "generation/lineage-length",
                "database lineage exceeds the canonical envelope limit",
            ));
        }
        let lineage_end = cursor.checked_add(lineage_len).ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "generation lineage length overflows usize",
            )
        })?;
        let lineage_bytes = bytes.get(cursor..lineage_end).ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "generation envelope ends inside its lineage",
            )
        })?;
        let lineage_id = String::from_utf8(lineage_bytes.to_vec()).map_err(|_| {
            fault(
                "generation/lineage-encoding",
                "database lineage is not UTF-8",
            )
        })?;
        cursor = lineage_end;
        let transaction_len = read_u32(bytes, &mut cursor, checksum_at)? as usize;
        let transaction_end = cursor.checked_add(transaction_len).ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "nested transaction length overflows usize",
            )
        })?;
        if transaction_end != checksum_at {
            return Err(fault(
                "generation/envelope-length",
                "generation envelope contains trailing or truncated transaction bytes",
            ));
        }
        let transaction = decode_transaction(&bytes[cursor..transaction_end])?;
        let envelope = Self {
            lineage_id,
            generation,
            transaction,
        };
        validate(&envelope).map_err(|error| {
            fault(
                "generation/invalid-envelope",
                format!("persisted generation envelope failed validation: {error}"),
            )
        })?;
        if envelope.encode()? != bytes {
            return Err(fault(
                "generation/noncanonical-envelope",
                "generation transaction does not have one canonical encoding",
            ));
        }
        Ok(envelope)
    }

    pub(crate) fn hash(&self) -> Result<Digest, SemanticError> {
        Ok(sha256(&self.encode()?))
    }
}

fn validate(envelope: &LineageTransaction) -> Result<(), SemanticError> {
    if envelope.generation == 0 {
        return Err(incorrect(
            "generation/zero",
            "lineage-bound generations must be positive",
        ));
    }
    if !is_canonical_lineage(&envelope.lineage_id) {
        return Err(incorrect(
            "generation/lineage",
            "database lineage must be a lowercase RFC 4122 version-4 UUID",
        ));
    }
    if envelope.transaction.database_id != envelope.lineage_id {
        return Err(incorrect(
            "generation/transaction-lineage",
            "nested transaction must name the immutable database lineage",
        ));
    }
    Ok(())
}

pub(crate) fn is_canonical_lineage(value: &str) -> bool {
    let bytes = value.as_bytes();
    bytes.len() == 36
        && [8, 13, 18, 23]
            .into_iter()
            .all(|index| bytes[index] == b'-')
        && bytes[14] == b'4'
        && matches!(bytes[19], b'8' | b'9' | b'a' | b'b')
        && bytes.iter().enumerate().all(|(index, byte)| {
            [8, 13, 18, 23].contains(&index) || byte.is_ascii_digit() || matches!(byte, b'a'..=b'f')
        })
}

/// Stable lookup key for idempotency records in lineage-bound generations.
/// Caller-chosen request keys can contain business identifiers, so only this
/// domain-separated digest crosses the durable generation boundary.
pub(crate) fn request_key_hash(
    lineage_id: &str,
    request_key: &str,
) -> Result<Digest, SemanticError> {
    if !is_canonical_lineage(lineage_id) {
        return Err(incorrect(
            "generation/lineage",
            "database lineage must be a lowercase RFC 4122 version-4 UUID",
        ));
    }
    if request_key.is_empty() {
        return Err(incorrect(
            "generation/empty-request-key",
            "idempotency request key cannot be empty",
        ));
    }
    let key_length = u64::try_from(request_key.len()).map_err(|_| {
        incorrect(
            "generation/request-key-length",
            "idempotency request key length exceeds u64",
        )
    })?;
    let mut bytes = Vec::with_capacity(44 + request_key.len());
    bytes.extend_from_slice(b"atomic/generation-request-key/v1\0");
    bytes.extend_from_slice(lineage_id.as_bytes());
    bytes.extend_from_slice(&key_length.to_be_bytes());
    bytes.extend_from_slice(request_key.as_bytes());
    Ok(sha256(&bytes))
}

/// Non-reversible replacement for a copied request-content digest. It keeps a
/// stable occupied decision coordinate without retaining a dictionary-testable
/// hash of the original forms that may themselves contain excised values.
pub(crate) fn tombstone_request_digest(
    lineage_id: &str,
    generation: u64,
    basis_t: u64,
    key_hash: Digest,
) -> Result<Digest, SemanticError> {
    if !is_canonical_lineage(lineage_id) || generation == 0 || basis_t == 0 {
        return Err(incorrect(
            "generation/request-tombstone-coordinate",
            "request tombstone requires lineage and positive generation/basis",
        ));
    }
    let mut bytes = Vec::with_capacity(86);
    bytes.extend_from_slice(b"atomic/generation-request-tombstone/v1\0");
    bytes.extend_from_slice(lineage_id.as_bytes());
    bytes.extend_from_slice(&generation.to_be_bytes());
    bytes.extend_from_slice(&basis_t.to_be_bytes());
    bytes.extend_from_slice(&key_hash);
    Ok(sha256(&bytes))
}

fn read_u16(bytes: &[u8], cursor: &mut usize, end: usize) -> Result<u16, SemanticError> {
    let next = cursor.checked_add(2).ok_or_else(|| {
        fault(
            "generation/envelope-length",
            "generation envelope cursor overflow",
        )
    })?;
    let slice = bytes
        .get(*cursor..next)
        .filter(|_| next <= end)
        .ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "generation envelope ends inside a u16",
            )
        })?;
    *cursor = next;
    Ok(u16::from_be_bytes(slice.try_into().expect("checked u16")))
}

fn read_u32(bytes: &[u8], cursor: &mut usize, end: usize) -> Result<u32, SemanticError> {
    let next = cursor.checked_add(4).ok_or_else(|| {
        fault(
            "generation/envelope-length",
            "generation envelope cursor overflow",
        )
    })?;
    let slice = bytes
        .get(*cursor..next)
        .filter(|_| next <= end)
        .ok_or_else(|| {
            fault(
                "generation/envelope-length",
                "generation envelope ends inside a u32",
            )
        })?;
    *cursor = next;
    Ok(u32::from_be_bytes(slice.try_into().expect("checked u32")))
}

fn incorrect(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Incorrect, code, message)
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Datom, Value, make_eid, t_to_tx};

    const LINEAGE: &str = "01234567-89ab-4def-8123-456789abcdef";

    fn transaction() -> DurableTransaction {
        DurableTransaction {
            database_id: LINEAGE.to_owned(),
            basis_t: 1,
            previous_hash: [7; 32],
            eidx_frontier: 2,
            tempids: Default::default(),
            tx_data: vec![Datom {
                entity: make_eid(crate::USER_PARTITION, 1).unwrap(),
                attribute: 50,
                value: Value::Instant(1_000),
                tx: t_to_tx(1).unwrap(),
                added: true,
            }],
        }
    }

    #[test]
    fn lineage_transaction_round_trips_canonically() {
        let envelope = LineageTransaction {
            lineage_id: LINEAGE.to_owned(),
            generation: 41,
            transaction: transaction(),
        };
        let encoded = envelope.encode().unwrap();
        assert_eq!(LineageTransaction::decode(&encoded).unwrap(), envelope);
        assert_eq!(
            LineageTransaction::decode(&encoded)
                .unwrap()
                .encode()
                .unwrap(),
            encoded
        );
        assert_eq!(envelope.hash().unwrap(), sha256(&encoded));
    }

    #[test]
    fn generation_and_lineage_are_authenticated() {
        let envelope = LineageTransaction {
            lineage_id: LINEAGE.to_owned(),
            generation: 1,
            transaction: transaction(),
        };
        let mut encoded = envelope.encode().unwrap();
        encoded[15] ^= 1;
        assert_eq!(
            LineageTransaction::decode(&encoded).unwrap_err().code,
            "generation/envelope-checksum"
        );

        let mut wrong = transaction();
        wrong.database_id = "11234567-89ab-4def-8123-456789abcdef".into();
        assert_eq!(
            LineageTransaction {
                lineage_id: LINEAGE.into(),
                generation: 1,
                transaction: wrong,
            }
            .encode()
            .unwrap_err()
            .code,
            "generation/transaction-lineage"
        );
    }

    #[test]
    fn only_canonical_v4_uuid_lineages_are_admitted() {
        assert!(is_canonical_lineage(LINEAGE));
        for value in [
            "",
            "01234567-89AB-4DEF-8123-456789ABCDEF",
            "01234567-89ab-3def-8123-456789abcdef",
            "01234567-89ab-4def-7123-456789abcdef",
            "0123456789ab4def8123456789abcdef",
        ] {
            assert!(!is_canonical_lineage(value), "{value}");
        }
    }

    #[test]
    fn durable_request_lookup_does_not_embed_plaintext_key() {
        let key = "customer@example.test/remove";
        let digest = request_key_hash(LINEAGE, key).unwrap();
        assert_eq!(digest.len(), 32);
        assert_ne!(digest, request_key_hash(LINEAGE, "different").unwrap());
        assert_eq!(
            request_key_hash(LINEAGE, "").unwrap_err().code,
            "generation/empty-request-key"
        );
        let tombstone = tombstone_request_digest(LINEAGE, 1, 2, digest).unwrap();
        assert_ne!(tombstone, sha256(key.as_bytes()));
        assert_ne!(tombstone, digest);
    }
}
