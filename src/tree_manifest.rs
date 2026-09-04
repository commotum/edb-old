//! Canonical manifest for the eight persistent index-tree roots.
//!
//! PostgreSQL stores the important coordinates relationally, but peers never
//! trust an opaque row and a parallel payload independently. This envelope is
//! decoded canonically and then compared field-for-field with those rows.

use crate::persistent_tree::TreeDescriptor;
use crate::{Digest, ErrorCategory, IndexOrder, SemanticError, sha256};

const MAGIC: &[u8; 4] = b"ATIM";
const ROOT_REVISION_VERSION: u16 = 4;
const AVET_PROJECTION_VERSION: u16 = 5;
const VERSION: u16 = 6;
const HEADER_LEN: usize = 12;
const CHECKSUM_LEN: usize = 32;
const ROOTS: usize = 8;
const MAX_MANIFEST_BYTES: usize = 64 * 1024 * 1024;
const AVET_WORK_BYTES: usize = 16;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ManifestTree {
    pub descriptor: TreeDescriptor,
    /// Encoded bytes of the root node itself, not its subtree.
    pub root_bytes: u64,
}

/// One resumable physical AVET projection transition.
///
/// Logical schema facts can become durable before an attribute's historical
/// AEVT range has been copied into (or removed from) AVET.  This fixed-size
/// coordinate is authenticated by the manifest so peers can keep the
/// attribute unavailable while the indexer advances one immutable chunk at a
/// time. `offset` is an ordinal in a deterministically regenerated external
/// AVET sort, never a copied value, so a legal large value cannot inflate the
/// manifest.
#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd)]
pub struct AvetProjectionWork {
    pub attribute: u32,
    pub adding: bool,
    pub history: bool,
    /// Addition first empties a possibly stale target range, then copies the
    /// exact AEVT projection. Removal only uses the clearing phase.
    pub clearing: bool,
    pub offset: u64,
}

impl AvetProjectionWork {
    pub fn new(attribute: u32, adding: bool) -> Self {
        Self {
            attribute,
            adding,
            history: false,
            clearing: true,
            offset: 0,
        }
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct PersistentTreeManifest {
    pub database_id: String,
    /// Monotonic identity of this physical root publication. This is
    /// independent of the logical transaction basis so an idle database can
    /// replace a derived root without inventing a transaction.
    pub publication_revision: u64,
    pub basis_t: u64,
    /// Greatest logical transaction basis fully represented by this physical
    /// index publication. This is deliberately distinct from `basis_t`: a
    /// schema transaction can be present in the logical value while its AVET
    /// projection is still advancing through immutable same-basis roots.
    pub index_basis_t: u64,
    pub tx_hash: Digest,
    pub state_hash: Digest,
    pub excision_generation: u64,
    pub eidx_frontier: u64,
    pub trees: Vec<ManifestTree>,
    /// Attribute projection work not yet physically complete in AVET.
    /// Presence is also the durable `storageHasAVET=false` witness.
    pub pending_avet: Vec<AvetProjectionWork>,
}

impl PersistentTreeManifest {
    pub fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        self.encode_version(VERSION)
    }

    fn encode_version(&self, version: u16) -> Result<Vec<u8>, SemanticError> {
        self.validate()?;
        if version == ROOT_REVISION_VERSION && !self.pending_avet.is_empty() {
            return Err(SemanticError::incorrect(
                "tree/manifest-version",
                "v4 tree manifests cannot carry pending AVET work",
            ));
        }
        if version < VERSION {
            // V4 roots were necessarily complete. V5 added pending AVET work
            // but did not authenticate an exact index basis; decoding maps an
            // incomplete V5 root to zero so a new binary cannot falsely
            // satisfy an index waiter after an in-place upgrade.
            let represented = if version == AVET_PROJECTION_VERSION && !self.pending_avet.is_empty()
            {
                0
            } else {
                self.basis_t
            };
            if self.index_basis_t != represented {
                return Err(SemanticError::incorrect(
                    "tree/manifest-version",
                    "pre-v6 tree manifests cannot represent this index basis",
                ));
            }
        }
        let mut body = Vec::new();
        put_bytes(&mut body, self.database_id.as_bytes())?;
        put_u64(&mut body, self.publication_revision);
        put_u64(&mut body, self.basis_t);
        if version >= VERSION {
            put_u64(&mut body, self.index_basis_t);
        }
        body.extend_from_slice(&self.tx_hash);
        body.extend_from_slice(&self.state_hash);
        put_u64(&mut body, self.excision_generation);
        put_u64(&mut body, self.eidx_frontier);
        put_u32(&mut body, ROOTS as u32);
        for tree in &self.trees {
            body.push(order_tag(tree.descriptor.order));
            body.push(u8::from(tree.descriptor.history));
            body.extend_from_slice(&tree.descriptor.root_hash);
            put_u64(&mut body, tree.descriptor.count);
            put_u64(&mut body, tree.root_bytes);
            encode_optional_digest(&mut body, tree.descriptor.first_hash);
            encode_optional_digest(&mut body, tree.descriptor.last_hash);
        }
        if version >= AVET_PROJECTION_VERSION {
            put_u32(
                &mut body,
                u32::try_from(self.pending_avet.len()).map_err(|_| {
                    SemanticError::incorrect(
                        "tree/manifest-pending-avet-count",
                        "pending AVET work exceeds u32",
                    )
                })?,
            );
            for work in &self.pending_avet {
                put_u32(&mut body, work.attribute);
                body.push(u8::from(work.adding));
                body.push(u8::from(work.history));
                body.push(u8::from(work.clearing));
                body.push(0);
                put_u64(&mut body, work.offset);
            }
        }
        let body_len = u32::try_from(body.len()).map_err(|_| {
            SemanticError::incorrect(
                "tree/manifest-size",
                "persistent tree manifest body exceeds u32",
            )
        })?;
        let mut bytes = Vec::with_capacity(HEADER_LEN + body.len() + CHECKSUM_LEN);
        bytes.extend_from_slice(MAGIC);
        bytes.extend_from_slice(&version.to_be_bytes());
        bytes.extend_from_slice(&[0, 0]);
        bytes.extend_from_slice(&body_len.to_be_bytes());
        bytes.extend_from_slice(&body);
        let checksum = sha256(&bytes);
        bytes.extend_from_slice(&checksum);
        if bytes.len() > MAX_MANIFEST_BYTES {
            return Err(SemanticError::incorrect(
                "tree/manifest-size",
                format!("persistent tree manifest exceeds {MAX_MANIFEST_BYTES} bytes"),
            ));
        }
        Ok(bytes)
    }

    pub fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() < HEADER_LEN + CHECKSUM_LEN || bytes.len() > MAX_MANIFEST_BYTES {
            return Err(fault(
                "tree/manifest-size",
                "persistent tree manifest has an invalid encoded size",
            ));
        }
        if bytes.get(..4) != Some(MAGIC.as_slice()) {
            return Err(fault(
                "tree/manifest-magic",
                "tree manifest has the wrong magic",
            ));
        }
        let version = u16::from_be_bytes(bytes[4..6].try_into().expect("checked header"));
        if !matches!(
            version,
            ROOT_REVISION_VERSION | AVET_PROJECTION_VERSION | VERSION
        ) {
            return Err(fault(
                "tree/manifest-version",
                format!("tree manifest version {version} is unsupported"),
            ));
        }
        if bytes[6..8] != [0, 0] {
            return Err(fault(
                "tree/manifest-header",
                "tree manifest reserved header bytes must be zero",
            ));
        }
        let body_len =
            u32::from_be_bytes(bytes[8..12].try_into().expect("checked header")) as usize;
        let checksum_at = HEADER_LEN.checked_add(body_len).ok_or_else(|| {
            fault(
                "tree/manifest-length",
                "tree manifest length overflows usize",
            )
        })?;
        if checksum_at + CHECKSUM_LEN != bytes.len()
            || sha256(&bytes[..checksum_at]) != bytes[checksum_at..]
        {
            return Err(fault(
                "tree/manifest-checksum",
                "tree manifest length or checksum is invalid",
            ));
        }
        let mut cursor = Cursor::new(&bytes[HEADER_LEN..checksum_at]);
        let database_id = String::from_utf8(cursor.bytes()?.to_vec()).map_err(|_| {
            fault(
                "tree/manifest-database",
                "tree manifest database id is not UTF-8",
            )
        })?;
        let publication_revision = cursor.u64()?;
        let basis_t = cursor.u64()?;
        let encoded_index_basis_t = (version >= VERSION).then(|| cursor.u64()).transpose()?;
        let tx_hash = cursor.digest()?;
        let state_hash = cursor.digest()?;
        let excision_generation = cursor.u64()?;
        let eidx_frontier = cursor.u64()?;
        if cursor.u32()? as usize != ROOTS {
            return Err(fault(
                "tree/manifest-root-count",
                "tree manifest must contain exactly eight roots",
            ));
        }
        let mut trees = Vec::with_capacity(ROOTS);
        for _ in 0..ROOTS {
            let order = decode_order(cursor.u8()?)?;
            let history = cursor.boolean()?;
            let root_hash = cursor.digest()?;
            let count = cursor.u64()?;
            let root_bytes = cursor.u64()?;
            let first_hash = decode_optional_digest(&mut cursor)?;
            let last_hash = decode_optional_digest(&mut cursor)?;
            trees.push(ManifestTree {
                descriptor: TreeDescriptor {
                    root_hash,
                    order,
                    history,
                    count,
                    first_hash,
                    last_hash,
                },
                root_bytes,
            });
        }
        let pending_avet = if version >= AVET_PROJECTION_VERSION {
            let count = cursor.u32()? as usize;
            if count > usize::try_from(crate::MAX_SCHEMA_ATTRIBUTE_ID).unwrap_or(usize::MAX)
                || count > cursor.remaining() / AVET_WORK_BYTES
            {
                return Err(fault(
                    "tree/manifest-pending-avet-count",
                    "pending AVET work count exceeds the canonical body or schema space",
                ));
            }
            let mut pending = Vec::with_capacity(count);
            for _ in 0..count {
                let attribute = cursor.u32()?;
                let adding = cursor.boolean()?;
                let history = cursor.boolean()?;
                let clearing = cursor.boolean()?;
                if cursor.u8()? != 0 {
                    return Err(fault(
                        "tree/manifest-pending-avet-reserved",
                        "pending AVET work has nonzero reserved bytes",
                    ));
                }
                pending.push(AvetProjectionWork {
                    attribute,
                    adding,
                    history,
                    clearing,
                    offset: cursor.u64()?,
                });
            }
            pending
        } else {
            Vec::new()
        };
        let index_basis_t = encoded_index_basis_t.unwrap_or_else(|| {
            if version == AVET_PROJECTION_VERSION && !pending_avet.is_empty() {
                // V5 did not authenticate this coordinate. Zero is the safe
                // upgrade floor: the next completed V6 publication advances
                // it precisely, while no waiter can be released too early.
                0
            } else {
                basis_t
            }
        });
        cursor.finish()?;
        let manifest = Self {
            database_id,
            publication_revision,
            index_basis_t,
            basis_t,
            tx_hash,
            state_hash,
            excision_generation,
            eidx_frontier,
            trees,
            pending_avet,
        };
        manifest.validate()?;
        if manifest.encode_version(version)? != bytes {
            return Err(fault(
                "tree/noncanonical-manifest",
                "persistent tree manifest does not have one canonical encoding",
            ));
        }
        Ok(manifest)
    }

    /// Hash this value's current canonical v6 encoding. This is deliberately
    /// not called `hash`: a manifest decoded from stored v4/v5 bytes is
    /// addressed by the hash of those exact bytes, not by re-encoding its
    /// logical value.
    pub fn canonical_v6_hash(&self) -> Result<Digest, SemanticError> {
        Ok(sha256(&self.encode()?))
    }

    /// Compatibility spelling retained for callers compiled against the V5
    /// API. It hashes the current canonical format, just as the old method did
    /// before the format advanced.
    #[deprecated(note = "use canonical_v6_hash")]
    pub fn canonical_v5_hash(&self) -> Result<Digest, SemanticError> {
        self.canonical_v6_hash()
    }

    pub fn tree(&self, order: IndexOrder, history: bool) -> Option<&ManifestTree> {
        self.trees
            .iter()
            .find(|tree| tree.descriptor.order == order && tree.descriptor.history == history)
    }

    pub(crate) fn encoded_version(bytes: &[u8]) -> Result<i16, SemanticError> {
        if bytes.len() < HEADER_LEN + CHECKSUM_LEN || bytes.get(..4) != Some(MAGIC.as_slice()) {
            return Err(fault(
                "tree/manifest-header",
                "tree manifest has no readable version header",
            ));
        }
        let version = u16::from_be_bytes(bytes[4..6].try_into().expect("checked header"));
        if !matches!(
            version,
            ROOT_REVISION_VERSION | AVET_PROJECTION_VERSION | VERSION
        ) {
            return Err(fault(
                "tree/manifest-version",
                format!("tree manifest version {version} is unsupported"),
            ));
        }
        Ok(version as i16)
    }

    fn validate(&self) -> Result<(), SemanticError> {
        if self.database_id.is_empty() || self.publication_revision == 0 || self.eidx_frontier == 0
        {
            return Err(SemanticError::incorrect(
                "tree/manifest-metadata",
                "tree manifest requires a database id, positive publication revision and entity frontier",
            ));
        }
        if self.state_hash == [0; 32] {
            return Err(SemanticError::incorrect(
                "tree/manifest-state",
                "tree manifest cannot bind the legacy zero state commitment",
            ));
        }
        if self.index_basis_t > self.basis_t {
            return Err(SemanticError::incorrect(
                "tree/manifest-index-basis",
                "tree manifest index basis cannot exceed its logical basis",
            ));
        }
        if self.pending_avet.is_empty() {
            if self.index_basis_t != self.basis_t {
                return Err(SemanticError::incorrect(
                    "tree/manifest-index-basis",
                    "a complete tree manifest must be indexed through its logical basis",
                ));
            }
        } else if self.index_basis_t >= self.basis_t {
            return Err(SemanticError::incorrect(
                "tree/manifest-index-basis",
                "a tree manifest with pending AVET work must retain an earlier index basis",
            ));
        }
        if self.trees.len() != ROOTS {
            return Err(SemanticError::incorrect(
                "tree/manifest-root-count",
                "tree manifest must contain exactly eight roots",
            ));
        }
        for (index, tree) in self.trees.iter().enumerate() {
            let expected_history = index >= 4;
            let expected_order = order_from_index(index % 4);
            if tree.descriptor.history != expected_history
                || tree.descriptor.order != expected_order
            {
                return Err(SemanticError::incorrect(
                    "tree/manifest-root-order",
                    "tree roots must be canonical current EAVT/AEVT/AVET/VAET then history",
                ));
            }
            if tree.root_bytes == 0 {
                return Err(SemanticError::incorrect(
                    "tree/manifest-root-bytes",
                    "tree root encoded size must be positive",
                ));
            }
            match (
                tree.descriptor.count,
                tree.descriptor.first_hash,
                tree.descriptor.last_hash,
            ) {
                (0, None, None) => {}
                (0, _, _) => {
                    return Err(SemanticError::incorrect(
                        "tree/manifest-empty-range",
                        "empty tree cannot name first or last datoms",
                    ));
                }
                (_, Some(_), Some(_)) => {}
                _ => {
                    return Err(SemanticError::incorrect(
                        "tree/manifest-range",
                        "non-empty tree needs first/last boundary commitments",
                    ));
                }
            }
        }
        let mut previous = None;
        for work in &self.pending_avet {
            if work.attribute == 0 || previous.is_some_and(|attribute| attribute >= work.attribute)
            {
                return Err(SemanticError::incorrect(
                    "tree/manifest-pending-avet-order",
                    "pending AVET work must name positive, strictly ordered attributes",
                ));
            }
            if work.attribute > crate::MAX_SCHEMA_ATTRIBUTE_ID {
                return Err(SemanticError::incorrect(
                    "tree/manifest-pending-avet-attribute",
                    "pending AVET work exceeds the schema attribute id space",
                ));
            }
            if work.clearing && work.offset != 0 {
                return Err(SemanticError::incorrect(
                    "tree/manifest-pending-avet-cursor",
                    "AVET clearing work must restart from the shrinking range prefix",
                ));
            }
            if !work.adding && !work.clearing {
                return Err(SemanticError::incorrect(
                    "tree/manifest-pending-avet-phase",
                    "AVET removal work cannot enter the addition phase",
                ));
            }
            if !work.clearing {
                let source_count = self
                    .tree(IndexOrder::Aevt, work.history)
                    .map(|tree| tree.descriptor.count)
                    .unwrap_or(0);
                if work.offset > source_count {
                    return Err(SemanticError::incorrect(
                        "tree/manifest-pending-avet-cursor",
                        "AVET projection offset exceeds its immutable AEVT source tree",
                    ));
                }
            }
            previous = Some(work.attribute);
        }
        Ok(())
    }
}

fn encode_optional_digest(output: &mut Vec<u8>, digest: Option<Digest>) {
    let Some(digest) = digest else {
        output.push(0);
        return;
    };
    output.push(1);
    output.extend_from_slice(&digest);
}

fn decode_optional_digest(cursor: &mut Cursor<'_>) -> Result<Option<Digest>, SemanticError> {
    match cursor.u8()? {
        0 => Ok(None),
        1 => Ok(Some(cursor.digest()?)),
        _ => Err(fault(
            "tree/manifest-option",
            "tree manifest optional datom tag is invalid",
        )),
    }
}

fn order_tag(order: IndexOrder) -> u8 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}

fn decode_order(tag: u8) -> Result<IndexOrder, SemanticError> {
    match tag {
        0 => Ok(IndexOrder::Eavt),
        1 => Ok(IndexOrder::Aevt),
        2 => Ok(IndexOrder::Avet),
        3 => Ok(IndexOrder::Vaet),
        _ => Err(fault(
            "tree/manifest-order",
            "tree manifest index order is invalid",
        )),
    }
}

fn order_from_index(index: usize) -> IndexOrder {
    match index {
        0 => IndexOrder::Eavt,
        1 => IndexOrder::Aevt,
        2 => IndexOrder::Avet,
        3 => IndexOrder::Vaet,
        _ => unreachable!("manifest order index is modulo four"),
    }
}

fn put_bytes(output: &mut Vec<u8>, value: &[u8]) -> Result<(), SemanticError> {
    let length = u32::try_from(value.len()).map_err(|_| {
        SemanticError::incorrect("tree/manifest-length", "manifest value exceeds u32")
    })?;
    put_u32(output, length);
    output.extend_from_slice(value);
    Ok(())
}

fn put_u32(output: &mut Vec<u8>, value: u32) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn put_u64(output: &mut Vec<u8>, value: u64) {
    output.extend_from_slice(&value.to_be_bytes());
}

struct Cursor<'a> {
    bytes: &'a [u8],
    offset: usize,
}

impl<'a> Cursor<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, offset: 0 }
    }

    fn finish(&self) -> Result<(), SemanticError> {
        if self.offset == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "tree/manifest-trailing-bytes",
                "tree manifest contains trailing bytes",
            ))
        }
    }

    fn remaining(&self) -> usize {
        self.bytes.len().saturating_sub(self.offset)
    }

    fn take(&mut self, length: usize) -> Result<&'a [u8], SemanticError> {
        let end = self
            .offset
            .checked_add(length)
            .ok_or_else(|| fault("tree/manifest-length", "tree manifest cursor overflow"))?;
        let value = self
            .bytes
            .get(self.offset..end)
            .ok_or_else(|| fault("tree/manifest-truncated", "tree manifest is truncated"))?;
        self.offset = end;
        Ok(value)
    }

    fn u8(&mut self) -> Result<u8, SemanticError> {
        Ok(self.take(1)?[0])
    }

    fn boolean(&mut self) -> Result<bool, SemanticError> {
        match self.u8()? {
            0 => Ok(false),
            1 => Ok(true),
            _ => Err(fault(
                "tree/manifest-bool",
                "tree manifest boolean is invalid",
            )),
        }
    }

    fn u32(&mut self) -> Result<u32, SemanticError> {
        Ok(u32::from_be_bytes(
            self.take(4)?.try_into().expect("exact cursor slice"),
        ))
    }

    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(
            self.take(8)?.try_into().expect("exact cursor slice"),
        ))
    }

    fn digest(&mut self) -> Result<Digest, SemanticError> {
        Ok(self.take(32)?.try_into().expect("exact cursor slice"))
    }

    fn bytes(&mut self) -> Result<&'a [u8], SemanticError> {
        let length = self.u32()? as usize;
        self.take(length)
    }
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::persistent_tree::{TreeConfig, build_tree};
    use crate::{Datom, USER_PARTITION, Value, make_eid, t_to_tx};

    fn manifest() -> PersistentTreeManifest {
        let mut trees = Vec::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                trees.push(ManifestTree {
                    descriptor: TreeDescriptor {
                        root_hash: [order_tag(order) + u8::from(history); 32],
                        order,
                        history,
                        count: 1,
                        first_hash: Some([11; 32]),
                        last_hash: Some([12; 32]),
                    },
                    root_bytes: 80,
                });
            }
        }
        PersistentTreeManifest {
            database_id: "db".into(),
            publication_revision: 1,
            index_basis_t: 2,
            basis_t: 2,
            tx_hash: [7; 32],
            state_hash: [8; 32],
            excision_generation: 0,
            eidx_frontier: 1_000,
            trees,
            pending_avet: Vec::new(),
        }
    }

    #[test]
    fn manifest_round_trip_is_canonical() {
        let manifest = manifest();
        let bytes = manifest.encode().unwrap();
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), manifest);
        assert_eq!(manifest.canonical_v6_hash().unwrap(), sha256(&bytes));

        let mut genesis = manifest.clone();
        genesis.basis_t = 0;
        genesis.index_basis_t = 0;
        let bytes = genesis.encode().unwrap();
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), genesis);
    }

    #[test]
    fn legacy_v4_and_v5_decode_without_inventing_progress() {
        let manifest = manifest();
        let bytes = manifest.encode_version(ROOT_REVISION_VERSION).unwrap();
        assert_eq!(PersistentTreeManifest::encoded_version(&bytes).unwrap(), 4);
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), manifest);
        assert_eq!(sha256(&bytes), sha256(&manifest.encode_version(4).unwrap()));

        let bytes = manifest.encode_version(AVET_PROJECTION_VERSION).unwrap();
        assert_eq!(PersistentTreeManifest::encoded_version(&bytes).unwrap(), 5);
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), manifest);

        let mut incomplete = manifest;
        incomplete.index_basis_t = 0;
        incomplete.pending_avet = vec![AvetProjectionWork::new(42, true)];
        let bytes = incomplete.encode_version(AVET_PROJECTION_VERSION).unwrap();
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), incomplete);
    }

    #[test]
    fn pending_projection_round_trip_and_count_are_bounded() {
        let mut manifest = manifest();
        manifest.index_basis_t = 1;
        manifest.pending_avet = vec![AvetProjectionWork {
            attribute: 42,
            adding: true,
            history: true,
            clearing: false,
            offset: 1,
        }];
        let bytes = manifest.encode().unwrap();
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), manifest);

        let mut older_index = manifest.clone();
        older_index.index_basis_t = 0;
        assert_ne!(older_index.encode().unwrap(), bytes);

        // The pending count is the final body word when there are no rows.
        // Recompute the checksum so decode reaches the allocation guard rather
        // than rejecting this as an unrelated checksum failure.
        let mut impossible = self::manifest().encode().unwrap();
        let count_offset = impossible.len() - CHECKSUM_LEN - 4;
        impossible[count_offset..count_offset + 4].copy_from_slice(&u32::MAX.to_be_bytes());
        let checksum = sha256(&impossible[..impossible.len() - CHECKSUM_LEN]);
        let checksum_offset = impossible.len() - CHECKSUM_LEN;
        impossible[checksum_offset..].copy_from_slice(&checksum);
        assert_eq!(
            PersistentTreeManifest::decode(&impossible)
                .unwrap_err()
                .code,
            "tree/manifest-pending-avet-count"
        );
    }

    #[test]
    fn corrupt_noncanonical_and_incomplete_manifests_fail_closed() {
        let manifest = manifest();
        let mut corrupt = manifest.encode().unwrap();
        corrupt[20] ^= 1;
        assert_eq!(
            PersistentTreeManifest::decode(&corrupt).unwrap_err().code,
            "tree/manifest-checksum"
        );

        let mut legacy = manifest.encode().unwrap();
        legacy[4..6].copy_from_slice(&3_u16.to_be_bytes());
        assert_eq!(
            PersistentTreeManifest::decode(&legacy).unwrap_err().code,
            "tree/manifest-version"
        );

        let mut zero_revision = manifest.clone();
        zero_revision.publication_revision = 0;
        assert_eq!(
            zero_revision.encode().unwrap_err().code,
            "tree/manifest-metadata"
        );

        let mut successor = manifest.clone();
        successor.publication_revision += 1;
        assert_ne!(
            successor.canonical_v6_hash().unwrap(),
            manifest.canonical_v6_hash().unwrap()
        );

        let mut future_index = manifest.clone();
        future_index.index_basis_t = future_index.basis_t + 1;
        assert_eq!(
            future_index.encode().unwrap_err().code,
            "tree/manifest-index-basis"
        );

        let mut lagging_complete = manifest.clone();
        lagging_complete.index_basis_t -= 1;
        assert_eq!(
            lagging_complete.encode().unwrap_err().code,
            "tree/manifest-index-basis"
        );

        let mut falsely_complete = manifest.clone();
        falsely_complete.pending_avet = vec![AvetProjectionWork::new(42, true)];
        assert_eq!(
            falsely_complete.encode().unwrap_err().code,
            "tree/manifest-index-basis"
        );

        let mut wrong_order = manifest.clone();
        wrong_order.trees.swap(0, 1);
        assert_eq!(
            wrong_order.encode().unwrap_err().code,
            "tree/manifest-root-order"
        );

        let mut missing = manifest;
        missing.trees.pop();
        assert_eq!(
            missing.encode().unwrap_err().code,
            "tree/manifest-root-count"
        );
    }

    #[test]
    fn manifest_size_is_independent_of_database_values_and_metadata() {
        // ATIM v2 copied first and last values into all eight descriptors;
        // the first Goal 13 draft also copied every schema and ident datom.
        // V5 contains fixed-size commitments only, so neither legal values nor
        // cumulative metadata history can turn its defensive envelope limit
        // into a database semantic limit.
        // A moderately large value is sufficient to prove that only its
        // digest reaches ATIM while keeping this codec unit test cheap.
        let large = Value::String("x".repeat(256 * 1024));
        let mut trees = Vec::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let datom = Datom {
                    entity: make_eid(USER_PARTITION, 1).unwrap(),
                    attribute: 7,
                    value: large.clone(),
                    tx: t_to_tx(2).unwrap(),
                    added: true,
                };
                let build = build_tree(order, history, [datom], &TreeConfig::default()).unwrap();
                let root_bytes = build.nodes.get(&build.descriptor.root_hash).unwrap().len() as u64;
                trees.push(ManifestTree {
                    descriptor: build.descriptor,
                    root_bytes,
                });
            }
        }
        let manifest = PersistentTreeManifest {
            database_id: "large-boundary".into(),
            publication_revision: 1,
            index_basis_t: 2,
            basis_t: 2,
            tx_hash: [7; 32],
            state_hash: [8; 32],
            excision_generation: 0,
            eidx_frontier: 1_000,
            trees,
            pending_avet: Vec::new(),
        };
        let bytes = manifest.encode().unwrap();
        assert!(bytes.len() < 2_048);
        assert_eq!(PersistentTreeManifest::decode(&bytes).unwrap(), manifest);
    }
}
