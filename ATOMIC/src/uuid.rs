//! Time-local UUID values, not transaction clocks or allocation reservations.
//! Squuid follows recovered datomic/common.clj; UUIDv7 uses RFC9562 section5.7.
use crate::{ErrorCategory, SemanticError};
use rand::TryRng;
use std::time::{SystemTime, UNIX_EPOCH};

const VERSION_MASK: u128 = 0xf << 76;
const VARIANT_MASK: u128 = 3 << 62;
const V7_MAX_MILLIS: u64 = (1 << 48) - 1;

fn unix_millis() -> Result<u64, SemanticError> {
    let elapsed = SystemTime::now().duration_since(UNIX_EPOCH).map_err(|_| {
        SemanticError::incorrect("uuid/clock-before-epoch", "UUID clock is before Unix epoch")
    })?;
    u64::try_from(elapsed.as_millis()).map_err(|_| {
        SemanticError::incorrect(
            "uuid/clock-out-of-range",
            "UUID clock exceeds supported range",
        )
    })
}

fn random_bits() -> Result<u128, SemanticError> {
    // Stateless OS randomness avoids duplicating a process-local RNG stream
    // after fork. Failure is explicit rather than falling back to clock-only IDs.
    let mut bytes = [0u8; 16];
    rand::rngs::SysRng.try_fill_bytes(&mut bytes).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unavailable,
            "uuid/randomness-unavailable",
            "operating-system UUID randomness is unavailable",
        )
    })?;
    Ok(u128::from_be_bytes(bytes))
}

fn versioned(bits: u128, version: u128) -> u128 {
    (bits & !VERSION_MASK & !VARIANT_MASK) | (version << 76) | (2 << 62)
}

/// Semi-sequential UUID with Unix seconds in its leading32bits, version4 and
/// the standard variant. Remaining90bits are random. Reveals coarse creation
/// time; no strict ordering within a second or after clock regression.
pub fn squuid() -> Result<u128, SemanticError> {
    squuid_at(unix_millis()?)
}

/// Generate a squuid with a supplied Unix-millisecond clock. Precision is one
/// second; times whose seconds do not fit32bits are rejected, never wrapped.
pub fn squuid_at(unix_millis: u64) -> Result<u128, SemanticError> {
    let seconds = squuid_seconds(unix_millis)?;
    Ok(squuid_parts(seconds, random_bits()?))
}

fn squuid_seconds(millis: u64) -> Result<u32, SemanticError> {
    u32::try_from(millis / 1000).map_err(|_| {
        SemanticError::incorrect(
            "uuid/squuid-time-out-of-range",
            "squuid seconds exceed32bits",
        )
    })
}

fn squuid_parts(seconds: u32, random: u128) -> u128 {
    versioned((u128::from(seconds) << 96) | (random & ((1 << 96) - 1)), 4)
}

/// Read the leading32bits as Unix seconds, expressed in milliseconds. Like the
/// Datomic helper, this cannot establish that an arbitrary UUID was a squuid.
pub fn squuid_time_millis(uuid: u128) -> u64 {
    ((uuid >> 96) as u64) * 1000
}

/// RFC9562 UUIDv7:48-bit Unix milliseconds, version7/standard variant and74random
/// bits. No counter or monotonic-clock state; same-tick IDs have random order.
pub fn uuid_v7() -> Result<u128, SemanticError> {
    uuid_v7_at(unix_millis()?)
}

/// Generate UUIDv7 using an explicit Unix-millisecond clock. Useful for injected
/// application clocks. Retain generated intent when retrying a transaction;
/// calling this again produces a different UUID, not an idempotency key lookup.
pub fn uuid_v7_at(unix_millis: u64) -> Result<u128, SemanticError> {
    if unix_millis > V7_MAX_MILLIS {
        return Err(SemanticError::incorrect(
            "uuid/v7-time-out-of-range",
            "UUIDv7 milliseconds exceed48bits",
        ));
    }
    Ok(v7_parts(unix_millis, random_bits()?))
}

fn v7_parts(millis: u64, random: u128) -> u128 {
    versioned((u128::from(millis) << 80) | (random & ((1 << 80) - 1)), 7)
}

/// Extract the timestamp after checking the version and variant. A UUID is not
/// authenticated provenance; this field need not be the actual creation time.
pub fn uuid_v7_time_millis(uuid: u128) -> Result<u64, SemanticError> {
    if (uuid & VERSION_MASK) >> 76 != 7 || (uuid & VARIANT_MASK) >> 62 != 2 {
        return Err(SemanticError::incorrect(
            "uuid/not-v7",
            "UUID is not version7/standard variant",
        ));
    }
    Ok((uuid >> 80) as u64)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::Value;

    #[test]
    fn squuid_layout_precision_boundaries_and_native_order_are_explicit() {
        for seconds in [0, 1, i32::MAX as u32, 1 << 31, u32::MAX] {
            for random in [0, u128::MAX] {
                let uuid = squuid_parts(seconds, random);
                assert_eq!(squuid_time_millis(uuid), u64::from(seconds) * 1000);
                assert_eq!((uuid >> 76) & 15, 4);
                assert_eq!((uuid >> 62) & 3, 2);
            }
        }
        assert_eq!(squuid_seconds(1999).unwrap(), 1);
        assert_eq!(
            squuid_seconds(u64::from(u32::MAX) * 1000 + 999).unwrap(),
            u32::MAX
        );
        assert!(squuid_at((u64::from(u32::MAX) + 1) * 1000).is_err());
        let before = squuid_parts(i32::MAX as u32, 0);
        let after = squuid_parts(1 << 31, 0);
        assert!(before < after); // Byte/unsigned ordering.
        // Preserve the existing signed-half UUID index comparator, not a hidden
        // index format change. Time locality has a sign-boundary discontinuity.
        assert!(Value::Uuid(before).index_cmp(&Value::Uuid(after)).is_gt());
    }

    #[test]
    fn v7_matches_rfc_vector_and_rejects_wrong_versions_or_overflow() {
        // RFC9562 AppendixA.6. Random bits are supplied only to this pure test.
        let expected = 0x017f22e279b07cc398c4dc0c0c07398f;
        assert_eq!(v7_parts(1_645_557_742_000, expected), expected);
        assert_eq!(uuid_v7_time_millis(expected).unwrap(), 1_645_557_742_000);
        for millis in [0, 1, V7_MAX_MILLIS] {
            for random in [0, u128::MAX] {
                assert_eq!(
                    uuid_v7_time_millis(v7_parts(millis, random)).unwrap(),
                    millis
                );
            }
        }
        assert!(uuid_v7_at(V7_MAX_MILLIS + 1).is_err());
        assert!(uuid_v7_time_millis(expected ^ (1 << 76)).is_err());
        assert!(uuid_v7_time_millis(expected ^ (1 << 63)).is_err());
        assert!(v7_parts(1, u128::MAX) < v7_parts(2, 0));
        // No process-local counter makes same-tick or regressed clocks monotonic.
        assert!(v7_parts(2, u128::MAX) > v7_parts(2, 0));
        assert!(v7_parts(2, 0) > v7_parts(1, u128::MAX));
    }

    #[test]
    fn public_generators_use_randomness_and_keep_stored_uuid_encoding() {
        let mut uuids = std::collections::BTreeSet::new();
        for _ in 0..128 {
            let squuid = squuid_at(1_700_000_000_123).unwrap();
            let v7 = uuid_v7_at(1_700_000_000_123).unwrap();
            assert_eq!(squuid_time_millis(squuid), 1_700_000_000_000);
            assert_eq!(uuid_v7_time_millis(v7).unwrap(), 1_700_000_000_123);
            assert!(uuids.insert(squuid));
            assert!(uuids.insert(v7));
        }
        assert_eq!((squuid().unwrap() >> 76) & 15, 4);
        assert_eq!((uuid_v7().unwrap() >> 76) & 15, 7);
        // These remain the same u128 payload in the existing Value variant.
        assert!(matches!(
            Value::Uuid(*uuids.first().unwrap()),
            Value::Uuid(_)
        ));
    }
}
