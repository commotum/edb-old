//! Canonical immutable index-tree values.
//!
//! This is intentionally a concrete three-level shape, not a general B-tree
//! or storage abstraction.  Datomic's documented shallow, wide trees and the
//! recovered 1.0.7705 `RootNode -> DirNode -> TransposedData` path are the
//! blueprint: roots and directories contain sparse lower-bound keys plus
//! immutable child hashes, while leaves keep datoms in parallel columns.
//! PostgreSQL persistence/publication is a separate boundary.

use crate::encoding::{
    Digest, decode_canonical_value, encode_canonical_value, sha256,
    validate_persistent_index_datoms,
};
use crate::index::IndexPrefix;
use crate::recent::NoHistoryPair;
use crate::{Datom, ErrorCategory, IndexOrder, SemanticError, Value};
use std::cmp::Ordering;
use std::collections::{BTreeMap, HashSet};
use std::mem::size_of;

const TREE_MAGIC: &[u8; 4] = b"ATIX";
pub const TREE_FORMAT_VERSION: u16 = 3;
const HEADER_LEN: usize = 16;
const CHECKSUM_LEN: usize = 32;
const EMPTY_NODE_LEN: usize = HEADER_LEN + CHECKSUM_LEN;
const MAX_TREE_NODE_BYTES: usize = 64 * 1024 * 1024;
const MAX_NODE_ENTRIES: usize = 1_000_000;

const KIND_LEAF: u8 = 1;
const KIND_DIRECTORY: u8 = 2;
const KIND_ROOT: u8 = 3;

const ROUTING_ENTITY_PRESENT: u8 = 1 << 0;
const ROUTING_ATTRIBUTE_PRESENT: u8 = 1 << 1;
const ROUTING_VALUE_PRESENT: u8 = 1 << 2;
const ROUTING_TX_PRESENT: u8 = 1 << 3;
const ROUTING_EXACT_COMPONENTS: u8 =
    ROUTING_ENTITY_PRESENT | ROUTING_ATTRIBUTE_PRESENT | ROUTING_VALUE_PRESENT | ROUTING_TX_PRESENT;
const ROUTING_KNOWN_COMPONENTS: u8 = ROUTING_EXACT_COMPONENTS;

/// Physical construction limits for the fixed shallow tree.
///
/// `target_leaf_bytes` gives ordinary segments a useful working size.  The
/// separate hard maximum lets a legal large scalar occupy one leaf without
/// turning the target into an accidental semantic value-size limitation.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeConfig {
    pub max_leaf_datoms: usize,
    pub target_leaf_bytes: usize,
    pub max_leaf_bytes: usize,
    pub max_leaves_per_directory: usize,
    pub max_directory_bytes: usize,
    pub max_directories_per_root: usize,
    pub max_root_bytes: usize,
}

impl Default for TreeConfig {
    fn default() -> Self {
        Self {
            max_leaf_datoms: 4_096,
            target_leaf_bytes: 256 * 1024,
            // Canonical values may be up to 16 MiB.  A hard limit below that
            // would make an otherwise legal datom impossible to index.
            max_leaf_bytes: 20 * 1024 * 1024,
            max_leaves_per_directory: 1_024,
            max_directory_bytes: MAX_TREE_NODE_BYTES,
            max_directories_per_root: 1_024,
            max_root_bytes: MAX_TREE_NODE_BYTES,
        }
    }
}

impl TreeConfig {
    fn validate(&self) -> Result<(), SemanticError> {
        let count_limits = [
            ("max_leaf_datoms", self.max_leaf_datoms),
            ("max_leaves_per_directory", self.max_leaves_per_directory),
            ("max_directories_per_root", self.max_directories_per_root),
        ];
        for (name, value) in count_limits {
            if value == 0 || value > MAX_NODE_ENTRIES {
                return Err(SemanticError::incorrect(
                    "tree/invalid-config",
                    format!("{name} must be between 1 and {MAX_NODE_ENTRIES}"),
                ));
            }
        }

        let byte_limits = [
            ("target_leaf_bytes", self.target_leaf_bytes),
            ("max_leaf_bytes", self.max_leaf_bytes),
            ("max_directory_bytes", self.max_directory_bytes),
            ("max_root_bytes", self.max_root_bytes),
        ];
        for (name, value) in byte_limits {
            if !(EMPTY_NODE_LEN..=MAX_TREE_NODE_BYTES).contains(&value) {
                return Err(SemanticError::incorrect(
                    "tree/invalid-config",
                    format!("{name} must be between {EMPTY_NODE_LEN} and {MAX_TREE_NODE_BYTES}"),
                ));
            }
        }
        if self.target_leaf_bytes > self.max_leaf_bytes {
            return Err(SemanticError::incorrect(
                "tree/invalid-config",
                "target_leaf_bytes cannot exceed max_leaf_bytes",
            ));
        }
        Ok(())
    }
}

/// The immutable identity and logical span of one tree publication.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeDescriptor {
    pub root_hash: Digest,
    pub order: IndexOrder,
    pub history: bool,
    pub count: u64,
    /// Fixed-size commitments to the first and last leaf datoms. Actual
    /// endpoint values remain only in immutable leaves, so one legal large
    /// scalar cannot be multiplied through the eight-root manifest.
    pub first_hash: Option<Digest>,
    pub last_hash: Option<Digest>,
}

/// Measured work for a streaming build.  `peak_live_datoms` is the important
/// memory witness: construction never retains the full input datom set.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct TreeBuildStats {
    pub input_datoms: u64,
    pub leaf_nodes: u64,
    pub directory_nodes: u64,
    pub root_nodes: u64,
    pub unique_nodes: u64,
    pub encoded_bytes: u64,
    pub peak_live_datoms: u64,
    pub largest_leaf_bytes: u64,
    pub largest_directory_bytes: u64,
    pub root_bytes: u64,
}

#[derive(Clone, Debug)]
pub struct TreeBuild {
    pub descriptor: TreeDescriptor,
    pub nodes: TreeNodeSet,
    pub stats: TreeBuildStats,
}

/// Half-open logical key range that must be reconsidered by a copy-on-write
/// merge. `None` is an unbounded side. Ordinary exact edits infer their own
/// ranges; this explicit form is for callers whose projection policy changed.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct TreeAffectedRange {
    pub start: Option<Datom>,
    pub end: Option<Datom>,
}

impl TreeAffectedRange {
    pub fn new(start: Option<Datom>, end: Option<Datom>) -> Self {
        Self { start, end }
    }

    pub fn unbounded() -> Self {
        Self::default()
    }
}

/// Exact logical changes applied to one current or retained-history tree.
///
/// Current trees permit exact removals and assertions. History trees are
/// append-only except for explicit adjacent noHistory pairs recovered by the
/// recent tier; an attribute-wide or time-wide purge has no representation.
#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct TreeMergeEdits {
    pub removals: Vec<Datom>,
    /// Exact physical members removed because an index projection changed
    /// (currently AVET add/drop). History information remains present in the
    /// authoritative EAVT/AEVT trees; this is not logical history deletion.
    pub projection_removals: Vec<Datom>,
    pub insertions: Vec<Datom>,
    pub no_history_pairs: Vec<NoHistoryPair>,
    pub affected_ranges: Vec<TreeAffectedRange>,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct TreeMergeStats {
    pub removals: u64,
    pub projection_removals: u64,
    pub insertions: u64,
    pub no_history_pairs: u64,
    /// Contiguous old-leaf runs reconsidered by this merge.
    pub affected_ranges: u64,
    pub affected_directories: u64,
    pub affected_leaves: u64,
    pub root_reads: u64,
    pub directory_reads: u64,
    pub leaf_reads: u64,
    pub decoded_bytes: u64,
    pub leaf_nodes_written: u64,
    pub directory_nodes_written: u64,
    pub root_nodes_written: u64,
    pub nodes_written: u64,
    pub encoded_bytes_written: u64,
    /// Existing content hashes reproduced while reconsidering a path.
    pub reused_hashes: u64,
    /// Whole directory subtrees copied into the new root without loading.
    pub reused_directory_refs: u64,
    /// Leaf references copied through an affected directory without loading.
    pub reused_leaf_refs: u64,
    pub output_datoms: u64,
}

/// Copy-on-write result. `new_nodes` contains only content absent from the old
/// resolver, so persistence writes scale with changed paths. Reads of the new
/// descriptor resolve from `new_nodes` first and the old immutable set second.
#[derive(Clone, Debug)]
pub struct TreeMerge {
    pub descriptor: TreeDescriptor,
    pub new_nodes: TreeNodeSet,
    pub stats: TreeMergeStats,
}

/// Concrete in-memory content resolver used by pure construction and
/// adversarial validation tests.  It is deliberately not a backend trait.
#[derive(Clone, Debug, Default)]
pub struct TreeNodeSet {
    nodes: BTreeMap<Digest, Vec<u8>>,
}

impl TreeNodeSet {
    pub fn len(&self) -> usize {
        self.nodes.len()
    }

    pub fn is_empty(&self) -> bool {
        self.nodes.is_empty()
    }

    pub fn get(&self, hash: &Digest) -> Option<&[u8]> {
        self.nodes.get(hash).map(Vec::as_slice)
    }

    pub fn iter(&self) -> impl Iterator<Item = (&Digest, &[u8])> {
        self.nodes
            .iter()
            .map(|(hash, bytes)| (hash, bytes.as_slice()))
    }

    pub fn into_nodes(self) -> BTreeMap<Digest, Vec<u8>> {
        self.nodes
    }

    pub fn from_nodes(nodes: BTreeMap<Digest, Vec<u8>>) -> Self {
        Self { nodes }
    }

    /// Add bytes already named by an external immutable reference. This is
    /// used by the PostgreSQL consolidator to assemble only the old paths a
    /// copy-on-write merge requests, without materializing the full tree.
    pub(crate) fn insert_known(
        &mut self,
        expected_hash: Digest,
        bytes: Vec<u8>,
    ) -> Result<(), SemanticError> {
        if sha256(&bytes) != expected_hash {
            return Err(fault(
                "tree/content-hash-mismatch",
                "resolved tree bytes do not match their immutable reference",
            ));
        }
        if let Some(existing) = self.nodes.insert(expected_hash, bytes.clone())
            && existing != bytes
        {
            return Err(fault(
                "tree/content-hash-collision",
                "one tree identity resolved to different canonical bytes",
            ));
        }
        Ok(())
    }

    fn insert(&mut self, bytes: Vec<u8>) -> Result<(Digest, bool), SemanticError> {
        let hash = sha256(&bytes);
        match self.nodes.get(&hash) {
            Some(existing) if existing != &bytes => Err(fault(
                "tree/content-hash-collision",
                "different canonical tree nodes produced the same SHA-256 identity",
            )),
            Some(_) => Ok((hash, false)),
            None => {
                self.nodes.insert(hash, bytes);
                Ok((hash, true))
            }
        }
    }
}

/// Comparator key retained by a root or directory entry.
///
/// Recovered `datomic.index/sparse-datom` uses ordinary datum-shaped keys,
/// but may put nil in the value slot after an earlier E/A component has
/// already decided the comparison. Rust datoms deliberately cannot contain
/// nil, so the parent-only representation makes that distinction explicit.
/// Variable values also retain an order-preserving prefix representation.
/// 1.0.7705 specializes String/vector; applying the same rule to the native
/// port's other legal unbounded variants prevents parent size from becoming
/// an accidental value-count limit.
#[derive(Clone, Debug)]
pub enum RoutingValue {
    Exact(Value),
    String(Vec<u16>),
    Uri(Vec<u16>),
    Bytes {
        total_len: u32,
        prefix: Vec<u8>,
    },
    Keyword {
        namespace: Option<Vec<u16>>,
        name: Vec<u16>,
    },
    Symbol {
        namespace: Option<Vec<u16>>,
        name: Vec<u16>,
    },
    Tuple {
        total_len: u32,
        prefix: Vec<Option<RoutingValue>>,
    },
}

impl PartialEq for RoutingValue {
    fn eq(&self, other: &Self) -> bool {
        match (self, other) {
            (Self::Exact(left), Self::Exact(right)) => left.stored_eq(right),
            (Self::String(left), Self::String(right)) | (Self::Uri(left), Self::Uri(right)) => {
                left == right
            }
            (
                Self::Bytes {
                    total_len: left_len,
                    prefix: left,
                },
                Self::Bytes {
                    total_len: right_len,
                    prefix: right,
                },
            ) => left_len == right_len && left == right,
            (
                Self::Keyword {
                    namespace: left_namespace,
                    name: left_name,
                },
                Self::Keyword {
                    namespace: right_namespace,
                    name: right_name,
                },
            )
            | (
                Self::Symbol {
                    namespace: left_namespace,
                    name: left_name,
                },
                Self::Symbol {
                    namespace: right_namespace,
                    name: right_name,
                },
            ) => left_namespace == right_namespace && left_name == right_name,
            (
                Self::Tuple {
                    total_len: left_len,
                    prefix: left,
                },
                Self::Tuple {
                    total_len: right_len,
                    prefix: right,
                },
            ) => left_len == right_len && left == right,
            _ => false,
        }
    }
}

impl Eq for RoutingValue {}

impl RoutingValue {
    fn exact(value: &Value) -> Self {
        Self::Exact(value.clone())
    }

    fn to_value(&self) -> Option<Value> {
        match self {
            Self::Exact(value) => Some(value.clone()),
            Self::String(value) => String::from_utf16(value).ok().map(Value::String),
            Self::Uri(value) => String::from_utf16(value).ok().map(Value::Uri),
            Self::Bytes { total_len, prefix } if *total_len as usize == prefix.len() => {
                Some(Value::Bytes(prefix.clone()))
            }
            Self::Keyword { namespace, name } => Some(Value::Keyword(crate::Keyword {
                namespace: namespace
                    .as_ref()
                    .map(|value| String::from_utf16(value))
                    .transpose()
                    .ok()?,
                name: String::from_utf16(name).ok()?,
            })),
            Self::Symbol { namespace, name } => Some(Value::Symbol(crate::Symbol {
                namespace: namespace
                    .as_ref()
                    .map(|value| String::from_utf16(value))
                    .transpose()
                    .ok()?,
                name: String::from_utf16(name).ok()?,
            })),
            Self::Tuple { total_len, prefix } if *total_len as usize == prefix.len() => {
                Some(Value::Tuple(
                    prefix
                        .iter()
                        .map(|value| match value {
                            None => Some(None),
                            Some(value) => Some(Some(value.to_value()?)),
                        })
                        .collect::<Option<Vec<_>>>()?,
                ))
            }
            _ => None,
        }
    }

    fn retained_heap_bytes(&self) -> u64 {
        let u16_bytes =
            |value: &Vec<u16>| (value.capacity() as u64).saturating_mul(size_of::<u16>() as u64);
        match self {
            Self::Exact(value) => value.retained_heap_bytes(),
            Self::String(value) | Self::Uri(value) => u16_bytes(value),
            Self::Bytes { prefix, .. } => prefix.capacity() as u64,
            Self::Keyword { namespace, name } | Self::Symbol { namespace, name } => namespace
                .as_ref()
                .map_or(0, u16_bytes)
                .saturating_add(u16_bytes(name)),
            Self::Tuple { prefix, .. } => (prefix.capacity() as u64)
                .saturating_mul(size_of::<Option<RoutingValue>>() as u64)
                .saturating_add(
                    prefix
                        .iter()
                        .filter_map(Option::as_ref)
                        .fold(0_u64, |total, value| {
                            total.saturating_add(value.retained_heap_bytes())
                        }),
                ),
        }
    }
}

#[derive(Clone, Debug)]
pub struct RoutingKey {
    pub entity: u64,
    pub attribute: u32,
    pub value: Option<RoutingValue>,
    pub tx: u64,
    pub added: bool,
    /// Presence is independent of the numeric payload. Entity zero is a real
    /// native entid (`:db.part/db`), while recovered sparse datums also use
    /// numeric zero for omitted components. Keeping the distinction explicit
    /// prevents valid `{e=0,a=...}` EAVT separators from being rejected.
    components: u8,
}

impl PartialEq for RoutingKey {
    fn eq(&self, other: &Self) -> bool {
        self.entity == other.entity
            && self.attribute == other.attribute
            && match (&self.value, &other.value) {
                (None, None) => true,
                (Some(left), Some(right)) => left == right,
                _ => false,
            }
            && self.tx == other.tx
            && self.added == other.added
            && self.components == other.components
    }
}

impl Eq for RoutingKey {}

impl RoutingKey {
    fn exact(datom: &Datom) -> Self {
        Self {
            entity: datom.entity,
            attribute: datom.attribute,
            value: Some(RoutingValue::exact(&datom.value)),
            tx: datom.tx,
            added: datom.added,
            components: ROUTING_EXACT_COMPONENTS,
        }
    }

    fn sparse(
        entity: Option<u64>,
        attribute: Option<u32>,
        value: Option<RoutingValue>,
        added: bool,
    ) -> Self {
        let mut components = 0;
        if entity.is_some() {
            components |= ROUTING_ENTITY_PRESENT;
        }
        if attribute.is_some() {
            components |= ROUTING_ATTRIBUTE_PRESENT;
        }
        if value.is_some() {
            components |= ROUTING_VALUE_PRESENT;
        }
        Self {
            entity: entity.unwrap_or(0),
            attribute: attribute.unwrap_or(0),
            value,
            tx: 0,
            added,
            components,
        }
    }

    fn has(&self, component: u8) -> bool {
        self.components & component != 0
    }

    fn is_exact(&self) -> bool {
        self.components == ROUTING_EXACT_COMPONENTS
    }

    pub(crate) fn cmp_datom(&self, other: &Datom, order: IndexOrder) -> Ordering {
        let ordering = match order {
            IndexOrder::Eavt => compare_present_to_value(
                self.has(ROUTING_ENTITY_PRESENT),
                &self.entity,
                &other.entity,
            )
            .then_with(|| {
                compare_present_to_value(
                    self.has(ROUTING_ATTRIBUTE_PRESENT),
                    &self.attribute,
                    &other.attribute,
                )
            })
            .then_with(|| {
                compare_present_routing_to_value(
                    self.has(ROUTING_VALUE_PRESENT),
                    self.value.as_ref(),
                    &other.value,
                    true,
                )
            }),
            IndexOrder::Aevt => compare_present_to_value(
                self.has(ROUTING_ATTRIBUTE_PRESENT),
                &self.attribute,
                &other.attribute,
            )
            .then_with(|| {
                compare_present_to_value(
                    self.has(ROUTING_ENTITY_PRESENT),
                    &self.entity,
                    &other.entity,
                )
            })
            .then_with(|| {
                compare_present_routing_to_value(
                    self.has(ROUTING_VALUE_PRESENT),
                    self.value.as_ref(),
                    &other.value,
                    true,
                )
            }),
            IndexOrder::Avet => compare_present_to_value(
                self.has(ROUTING_ATTRIBUTE_PRESENT),
                &self.attribute,
                &other.attribute,
            )
            .then_with(|| {
                compare_present_routing_to_value(
                    self.has(ROUTING_VALUE_PRESENT),
                    self.value.as_ref(),
                    &other.value,
                    true,
                )
            })
            .then_with(|| {
                compare_present_to_value(
                    self.has(ROUTING_ENTITY_PRESENT),
                    &self.entity,
                    &other.entity,
                )
            }),
            IndexOrder::Vaet => compare_present_routing_to_value(
                self.has(ROUTING_VALUE_PRESENT),
                self.value.as_ref(),
                &other.value,
                true,
            )
            .then_with(|| {
                compare_present_to_value(
                    self.has(ROUTING_ATTRIBUTE_PRESENT),
                    &self.attribute,
                    &other.attribute,
                )
            })
            .then_with(|| {
                compare_present_to_value(
                    self.has(ROUTING_ENTITY_PRESENT),
                    &self.entity,
                    &other.entity,
                )
            }),
        };
        ordering.then_with(|| {
            if self.has(ROUTING_TX_PRESENT) {
                other
                    .tx
                    .cmp(&self.tx)
                    .then_with(|| other.added.cmp(&self.added))
            } else {
                // Sparse routing keys are lower-bound prefixes. Treat an
                // omitted descending-T component as below every concrete T,
                // rather than comparing its encoded zero placeholder. This
                // is the explicit counterpart of recovered `make-sparse-lt`,
                // and matters when a preceding omitted component ties a real
                // native zero (notably entity 0 in VAET).
                Ordering::Less
            }
        })
    }

    pub(crate) fn cmp_key(&self, other: &Self, order: IndexOrder) -> Ordering {
        compare_routing_parts(
            self.entity,
            self.attribute,
            self.value.as_ref(),
            self.tx,
            self.added,
            self.components,
            other.entity,
            other.attribute,
            other.value.as_ref(),
            other.tx,
            other.added,
            other.components,
            order,
        )
    }

    pub(crate) fn cmp_prefix(&self, prefix: &IndexPrefix) -> Ordering {
        match prefix {
            IndexPrefix::Eavt {
                entity,
                attribute,
                value,
            } => compare_present_to_value(self.has(ROUTING_ENTITY_PRESENT), &self.entity, entity)
                .then_with(|| {
                    attribute.map_or(Ordering::Equal, |attribute| {
                        compare_present_to_value(
                            self.has(ROUTING_ATTRIBUTE_PRESENT),
                            &self.attribute,
                            &attribute,
                        )
                    })
                })
                .then_with(|| {
                    value.as_ref().map_or(Ordering::Equal, |value| {
                        compare_present_routing_to_value(
                            self.has(ROUTING_VALUE_PRESENT),
                            self.value.as_ref(),
                            value,
                            false,
                        )
                    })
                }),
            IndexPrefix::Aevt {
                attribute,
                entity,
                value,
            } => compare_present_to_value(
                self.has(ROUTING_ATTRIBUTE_PRESENT),
                &self.attribute,
                attribute,
            )
            .then_with(|| {
                entity.map_or(Ordering::Equal, |entity| {
                    compare_present_to_value(
                        self.has(ROUTING_ENTITY_PRESENT),
                        &self.entity,
                        &entity,
                    )
                })
            })
            .then_with(|| {
                value.as_ref().map_or(Ordering::Equal, |value| {
                    compare_present_routing_to_value(
                        self.has(ROUTING_VALUE_PRESENT),
                        self.value.as_ref(),
                        value,
                        false,
                    )
                })
            }),
            IndexPrefix::Avet {
                attribute,
                value,
                entity,
            } => compare_present_to_value(
                self.has(ROUTING_ATTRIBUTE_PRESENT),
                &self.attribute,
                attribute,
            )
            .then_with(|| {
                value.as_ref().map_or(Ordering::Equal, |value| {
                    compare_present_routing_to_value(
                        self.has(ROUTING_VALUE_PRESENT),
                        self.value.as_ref(),
                        value,
                        false,
                    )
                })
            })
            .then_with(|| {
                entity.map_or(Ordering::Equal, |entity| {
                    compare_present_to_value(
                        self.has(ROUTING_ENTITY_PRESENT),
                        &self.entity,
                        &entity,
                    )
                })
            }),
            IndexPrefix::Vaet {
                value,
                attribute,
                entity,
            } => compare_present_routing_to_value(
                self.has(ROUTING_VALUE_PRESENT),
                self.value.as_ref(),
                value,
                false,
            )
            .then_with(|| {
                attribute.map_or(Ordering::Equal, |attribute| {
                    compare_present_to_value(
                        self.has(ROUTING_ATTRIBUTE_PRESENT),
                        &self.attribute,
                        &attribute,
                    )
                })
            })
            .then_with(|| {
                entity.map_or(Ordering::Equal, |entity| {
                    compare_present_to_value(
                        self.has(ROUTING_ENTITY_PRESENT),
                        &self.entity,
                        &entity,
                    )
                })
            }),
        }
    }

    fn as_datom(&self) -> Option<Datom> {
        if !self.is_exact() {
            return None;
        }
        Some(Datom {
            entity: self.entity,
            attribute: self.attribute,
            value: self.value.as_ref()?.to_value()?,
            tx: self.tx,
            added: self.added,
        })
    }

    fn retained_heap_bytes(&self) -> u64 {
        self.value
            .as_ref()
            .map_or(0, RoutingValue::retained_heap_bytes)
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ChildRef {
    pub key: RoutingKey,
    pub hash: Digest,
    pub count: u64,
}

#[allow(clippy::too_many_arguments)]
fn compare_routing_parts(
    left_entity: u64,
    left_attribute: u32,
    left_value: Option<&RoutingValue>,
    left_tx: u64,
    left_added: bool,
    left_components: u8,
    right_entity: u64,
    right_attribute: u32,
    right_value: Option<&RoutingValue>,
    right_tx: u64,
    right_added: bool,
    right_components: u8,
    order: IndexOrder,
) -> Ordering {
    let ordering = match order {
        IndexOrder::Eavt => compare_present_values(
            left_components & ROUTING_ENTITY_PRESENT != 0,
            &left_entity,
            right_components & ROUTING_ENTITY_PRESENT != 0,
            &right_entity,
        )
        .then_with(|| {
            compare_present_values(
                left_components & ROUTING_ATTRIBUTE_PRESENT != 0,
                &left_attribute,
                right_components & ROUTING_ATTRIBUTE_PRESENT != 0,
                &right_attribute,
            )
        })
        .then_with(|| {
            compare_present_routing_values(
                left_components & ROUTING_VALUE_PRESENT != 0,
                left_value,
                right_components & ROUTING_VALUE_PRESENT != 0,
                right_value,
                true,
            )
        }),
        IndexOrder::Aevt => compare_present_values(
            left_components & ROUTING_ATTRIBUTE_PRESENT != 0,
            &left_attribute,
            right_components & ROUTING_ATTRIBUTE_PRESENT != 0,
            &right_attribute,
        )
        .then_with(|| {
            compare_present_values(
                left_components & ROUTING_ENTITY_PRESENT != 0,
                &left_entity,
                right_components & ROUTING_ENTITY_PRESENT != 0,
                &right_entity,
            )
        })
        .then_with(|| {
            compare_present_routing_values(
                left_components & ROUTING_VALUE_PRESENT != 0,
                left_value,
                right_components & ROUTING_VALUE_PRESENT != 0,
                right_value,
                true,
            )
        }),
        IndexOrder::Avet => compare_present_values(
            left_components & ROUTING_ATTRIBUTE_PRESENT != 0,
            &left_attribute,
            right_components & ROUTING_ATTRIBUTE_PRESENT != 0,
            &right_attribute,
        )
        .then_with(|| {
            compare_present_routing_values(
                left_components & ROUTING_VALUE_PRESENT != 0,
                left_value,
                right_components & ROUTING_VALUE_PRESENT != 0,
                right_value,
                true,
            )
        })
        .then_with(|| {
            compare_present_values(
                left_components & ROUTING_ENTITY_PRESENT != 0,
                &left_entity,
                right_components & ROUTING_ENTITY_PRESENT != 0,
                &right_entity,
            )
        }),
        IndexOrder::Vaet => compare_present_routing_values(
            left_components & ROUTING_VALUE_PRESENT != 0,
            left_value,
            right_components & ROUTING_VALUE_PRESENT != 0,
            right_value,
            true,
        )
        .then_with(|| {
            compare_present_values(
                left_components & ROUTING_ATTRIBUTE_PRESENT != 0,
                &left_attribute,
                right_components & ROUTING_ATTRIBUTE_PRESENT != 0,
                &right_attribute,
            )
        })
        .then_with(|| {
            compare_present_values(
                left_components & ROUTING_ENTITY_PRESENT != 0,
                &left_entity,
                right_components & ROUTING_ENTITY_PRESENT != 0,
                &right_entity,
            )
        }),
    };
    ordering.then_with(|| {
        match (
            left_components & ROUTING_TX_PRESENT != 0,
            right_components & ROUTING_TX_PRESENT != 0,
        ) {
            (false, false) => Ordering::Equal,
            (false, true) => Ordering::Less,
            (true, false) => Ordering::Greater,
            (true, true) => right_tx
                .cmp(&left_tx)
                .then_with(|| right_added.cmp(&left_added)),
        }
    })
}

fn compare_present_to_value<T: Ord>(present: bool, left: &T, right: &T) -> Ordering {
    if present {
        left.cmp(right)
    } else {
        Ordering::Less
    }
}

fn compare_present_values<T: Ord>(
    left_present: bool,
    left: &T,
    right_present: bool,
    right: &T,
) -> Ordering {
    match (left_present, right_present) {
        (false, false) => Ordering::Equal,
        (false, true) => Ordering::Less,
        (true, false) => Ordering::Greater,
        (true, true) => left.cmp(right),
    }
}

fn compare_present_routing_to_value(
    present: bool,
    left: Option<&RoutingValue>,
    right: &Value,
    stored: bool,
) -> Ordering {
    if present {
        compare_routing_to_value(
            left.expect("routing presence was validated before comparison"),
            right,
            stored,
        )
    } else {
        Ordering::Less
    }
}

fn compare_present_routing_values(
    left_present: bool,
    left: Option<&RoutingValue>,
    right_present: bool,
    right: Option<&RoutingValue>,
    stored: bool,
) -> Ordering {
    match (left_present, right_present) {
        (false, false) => Ordering::Equal,
        (false, true) => Ordering::Less,
        (true, false) => Ordering::Greater,
        (true, true) => compare_routing_values(
            left.expect("routing presence was validated before comparison"),
            right.expect("routing presence was validated before comparison"),
            stored,
        ),
    }
}

fn compare_optional_routing_values(
    left: Option<&RoutingValue>,
    right: Option<&RoutingValue>,
    stored: bool,
) -> Ordering {
    match (left, right) {
        (None, None) => Ordering::Equal,
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => compare_routing_values(left, right, stored),
    }
}

fn compare_optional_routing_to_value(
    left: Option<&RoutingValue>,
    right: Option<&Value>,
    stored: bool,
) -> Ordering {
    match (left, right) {
        (None, None) => Ordering::Equal,
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => compare_routing_to_value(left, right, stored),
    }
}

fn routing_value_rank(value: &RoutingValue) -> u8 {
    match value {
        RoutingValue::Exact(value) => value_rank(value),
        RoutingValue::Bytes { .. } => 1,
        RoutingValue::Keyword { .. } => 2,
        RoutingValue::Symbol { .. } => 3,
        RoutingValue::String(_) => 5,
        RoutingValue::Uri(_) => 6,
        RoutingValue::Tuple { .. } => 9,
    }
}

fn value_rank(value: &Value) -> u8 {
    match value {
        Value::BigDec(_)
        | Value::BigInt(_)
        | Value::Double(_)
        | Value::Float(_)
        | Value::Long(_)
        | Value::Ref(_) => 0,
        Value::Bytes(_) => 1,
        Value::Keyword(_) => 2,
        Value::Symbol(_) => 3,
        Value::Bool(_) => 4,
        Value::String(_) => 5,
        Value::Uri(_) => 6,
        Value::Instant(_) => 7,
        Value::Uuid(_) => 8,
        Value::Tuple(_) => 9,
        Value::Function(_) => 10,
    }
}

fn compare_routing_values(left: &RoutingValue, right: &RoutingValue, stored: bool) -> Ordering {
    let rank = routing_value_rank(left).cmp(&routing_value_rank(right));
    if rank.is_ne() {
        return rank;
    }
    match (left, right) {
        (RoutingValue::Exact(left), RoutingValue::Exact(right)) if stored => left.stored_cmp(right),
        (RoutingValue::Exact(left), RoutingValue::Exact(right)) => left.index_cmp(right),
        (RoutingValue::Exact(left), right) => {
            compare_routing_to_value(right, left, stored).reverse()
        }
        (left, RoutingValue::Exact(right)) => compare_routing_to_value(left, right, stored),
        (RoutingValue::String(left), RoutingValue::String(right))
        | (RoutingValue::Uri(left), RoutingValue::Uri(right)) => left.cmp(right),
        (
            RoutingValue::Bytes {
                total_len: left_len,
                prefix: left,
            },
            RoutingValue::Bytes {
                total_len: right_len,
                prefix: right,
            },
        ) => compare_routing_bytes(*left_len, left, *right_len, right),
        (
            RoutingValue::Keyword {
                namespace: left_namespace,
                name: left_name,
            },
            RoutingValue::Keyword {
                namespace: right_namespace,
                name: right_name,
            },
        )
        | (
            RoutingValue::Symbol {
                namespace: left_namespace,
                name: left_name,
            },
            RoutingValue::Symbol {
                namespace: right_namespace,
                name: right_name,
            },
        ) => compare_routing_named(
            left_namespace.as_deref(),
            left_name,
            right_namespace.as_deref(),
            right_name,
        ),
        (RoutingValue::Tuple { prefix: left, .. }, RoutingValue::Tuple { prefix: right, .. }) => {
            compare_routing_tuples(left, right, stored)
        }
        // Dedicated variants are canonical for these ranks, so validation
        // rejects every mixed case before a node can be encoded or accepted.
        _ => Ordering::Equal,
    }
}

fn compare_routing_to_value(left: &RoutingValue, right: &Value, stored: bool) -> Ordering {
    let rank = routing_value_rank(left).cmp(&value_rank(right));
    if rank.is_ne() {
        return rank;
    }
    match (left, right) {
        (RoutingValue::Exact(left), right) if stored => left.stored_cmp(right),
        (RoutingValue::Exact(left), right) => left.index_cmp(right),
        (RoutingValue::String(left), Value::String(right))
        | (RoutingValue::Uri(left), Value::Uri(right)) => {
            left.iter().copied().cmp(right.encode_utf16())
        }
        (RoutingValue::Bytes { total_len, prefix }, Value::Bytes(right)) => {
            compare_routing_bytes(*total_len, prefix, right.len() as u32, right)
        }
        (RoutingValue::Keyword { namespace, name }, Value::Keyword(right)) => {
            compare_routing_named_to_value(
                namespace.as_deref(),
                name,
                right.namespace.as_deref(),
                &right.name,
            )
        }
        (RoutingValue::Symbol { namespace, name }, Value::Symbol(right)) => {
            compare_routing_named_to_value(
                namespace.as_deref(),
                name,
                right.namespace.as_deref(),
                &right.name,
            )
        }
        (RoutingValue::Tuple { prefix, .. }, Value::Tuple(right)) => {
            compare_routing_tuple_to_value(prefix, right, stored)
        }
        _ => Ordering::Equal,
    }
}

fn compare_routing_bytes(left_len: u32, left: &[u8], right_len: u32, right: &[u8]) -> Ordering {
    left_len.cmp(&right_len).then_with(|| {
        left.iter()
            .map(|byte| *byte as i8)
            .cmp(right.iter().map(|byte| *byte as i8))
    })
}

fn compare_routing_named(
    left_namespace: Option<&[u16]>,
    left_name: &[u16],
    right_namespace: Option<&[u16]>,
    right_name: &[u16],
) -> Ordering {
    match (left_namespace, right_namespace) {
        (None, None) => left_name.cmp(right_name),
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => left.cmp(right).then_with(|| left_name.cmp(right_name)),
    }
}

fn compare_routing_named_to_value(
    left_namespace: Option<&[u16]>,
    left_name: &[u16],
    right_namespace: Option<&str>,
    right_name: &str,
) -> Ordering {
    match (left_namespace, right_namespace) {
        (None, None) => left_name.iter().copied().cmp(right_name.encode_utf16()),
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => left
            .iter()
            .copied()
            .cmp(right.encode_utf16())
            .then_with(|| left_name.iter().copied().cmp(right_name.encode_utf16())),
    }
}

fn compare_routing_tuples(
    left: &[Option<RoutingValue>],
    right: &[Option<RoutingValue>],
    stored: bool,
) -> Ordering {
    for (left, right) in left.iter().zip(right) {
        let ordering = compare_optional_routing_values(left.as_ref(), right.as_ref(), stored);
        if ordering.is_ne() {
            return ordering;
        }
    }
    left.len().cmp(&right.len())
}

fn compare_routing_tuple_to_value(
    left: &[Option<RoutingValue>],
    right: &[Option<Value>],
    stored: bool,
) -> Ordering {
    for (left, right) in left.iter().zip(right) {
        let ordering = compare_optional_routing_to_value(left.as_ref(), right.as_ref(), stored);
        if ordering.is_ne() {
            return ordering;
        }
    }
    left.len().cmp(&right.len())
}

/// Recover the shortest useful value separating two adjacent child spans.
/// This is the Rust translation of 1.0.7705 `strdiff`, `vecdiff`, and
/// `mindiff`, extended to equivalent order-preserving prefixes for the
/// native value set. UTF-16 storage deliberately permits a Java separator to
/// end between a surrogate pair without making it a logical Rust String.
fn minimum_difference(prior: &Value, current: &Value) -> RoutingValue {
    let candidate = match (prior, current) {
        (Value::String(prior), Value::String(current)) => {
            RoutingValue::String(minimum_utf16_difference(
                &prior.encode_utf16().collect::<Vec<_>>(),
                &current.encode_utf16().collect::<Vec<_>>(),
            ))
        }
        (Value::Uri(prior), Value::Uri(current)) => RoutingValue::Uri(minimum_utf16_difference(
            &prior.encode_utf16().collect::<Vec<_>>(),
            &current.encode_utf16().collect::<Vec<_>>(),
        )),
        (Value::Bytes(prior), Value::Bytes(current)) => RoutingValue::Bytes {
            total_len: current.len() as u32,
            prefix: minimum_bytes_difference(prior, current),
        },
        (Value::Keyword(prior), Value::Keyword(current)) => {
            let (namespace, name) = minimum_named_difference(
                prior.namespace.as_deref(),
                &prior.name,
                current.namespace.as_deref(),
                &current.name,
            );
            RoutingValue::Keyword { namespace, name }
        }
        (Value::Symbol(prior), Value::Symbol(current)) => {
            let (namespace, name) = minimum_named_difference(
                prior.namespace.as_deref(),
                &prior.name,
                current.namespace.as_deref(),
                &current.name,
            );
            RoutingValue::Symbol { namespace, name }
        }
        (Value::Tuple(prior), Value::Tuple(current)) => RoutingValue::Tuple {
            total_len: current.len() as u32,
            prefix: minimum_tuple_difference(prior, current),
        },
        _ => RoutingValue::exact(current),
    };
    if candidate
        .to_value()
        .is_some_and(|value| value.stored_eq(current))
    {
        RoutingValue::exact(current)
    } else {
        candidate
    }
}

fn minimum_utf16_difference(prior: &[u16], current: &[u16]) -> Vec<u16> {
    let difference = current
        .iter()
        .enumerate()
        .find(|(index, unit)| prior.get(*index) != Some(unit))
        .map_or(current.len(), |(index, _)| index + 1);
    current[..difference].to_vec()
}

fn minimum_bytes_difference(prior: &[u8], current: &[u8]) -> Vec<u8> {
    if prior.len() != current.len() {
        return Vec::new();
    }
    let difference = current
        .iter()
        .enumerate()
        .find(|(index, byte)| prior.get(*index) != Some(byte))
        .map_or(current.len(), |(index, _)| index + 1);
    current[..difference].to_vec()
}

fn minimum_named_difference(
    prior_namespace: Option<&str>,
    prior_name: &str,
    current_namespace: Option<&str>,
    current_name: &str,
) -> (Option<Vec<u16>>, Vec<u16>) {
    if prior_namespace != current_namespace {
        let namespace = match (prior_namespace, current_namespace) {
            (_, None) => None,
            (None, Some(_)) => Some(Vec::new()),
            (Some(prior), Some(current)) => Some(minimum_utf16_difference(
                &prior.encode_utf16().collect::<Vec<_>>(),
                &current.encode_utf16().collect::<Vec<_>>(),
            )),
        };
        return (namespace, Vec::new());
    }
    (
        current_namespace.map(|value| value.encode_utf16().collect()),
        minimum_utf16_difference(
            &prior_name.encode_utf16().collect::<Vec<_>>(),
            &current_name.encode_utf16().collect::<Vec<_>>(),
        ),
    )
}

fn minimum_tuple_difference(
    prior: &[Option<Value>],
    current: &[Option<Value>],
) -> Vec<Option<RoutingValue>> {
    for (index, current_value) in current.iter().enumerate() {
        let Some(prior_value) = prior.get(index) else {
            return current[..=index]
                .iter()
                .map(|value| value.as_ref().map(RoutingValue::exact))
                .collect();
        };
        let ordering = match (prior_value, current_value) {
            (None, None) => Ordering::Equal,
            (None, Some(_)) => Ordering::Less,
            (Some(_), None) => Ordering::Greater,
            (Some(prior), Some(current)) => prior.stored_cmp(current),
        };
        if ordering.is_ne() {
            let mut prefix = current[..index]
                .iter()
                .map(|value| value.as_ref().map(RoutingValue::exact))
                .collect::<Vec<_>>();
            prefix.push(match (prior_value, current_value) {
                (Some(prior), Some(current)) => Some(minimum_difference(prior, current)),
                (None, Some(current)) => Some(RoutingValue::exact(current)),
                (_, None) => None,
            });
            return prefix;
        }
    }
    current
        .iter()
        .map(|value| value.as_ref().map(RoutingValue::exact))
        .collect()
}

fn sparse_routing_key(order: IndexOrder, prior: &Datom, current: &Datom) -> RoutingKey {
    let added = current.added;
    match order {
        IndexOrder::Avet if current.attribute != prior.attribute => {
            RoutingKey::sparse(None, Some(current.attribute), None, added)
        }
        IndexOrder::Avet => {
            let value = minimum_difference(&prior.value, &current.value);
            if value != RoutingValue::exact(&current.value) {
                RoutingKey::sparse(None, Some(current.attribute), Some(value), added)
            } else {
                RoutingKey::exact(current)
            }
        }
        IndexOrder::Vaet if prior.value.stored_cmp(&current.value).is_ne() => {
            RoutingKey::sparse(None, None, Some(RoutingValue::exact(&current.value)), added)
        }
        IndexOrder::Vaet if current.attribute != prior.attribute => RoutingKey::sparse(
            None,
            Some(current.attribute),
            Some(RoutingValue::exact(&current.value)),
            added,
        ),
        IndexOrder::Vaet => RoutingKey::exact(current),
        IndexOrder::Eavt if current.entity != prior.entity => {
            RoutingKey::sparse(Some(current.entity), None, None, added)
        }
        IndexOrder::Eavt if current.attribute != prior.attribute => {
            RoutingKey::sparse(Some(current.entity), Some(current.attribute), None, added)
        }
        IndexOrder::Eavt => sparse_value_routing_key(order, prior, current),
        IndexOrder::Aevt if current.attribute != prior.attribute => {
            RoutingKey::sparse(None, Some(current.attribute), None, added)
        }
        IndexOrder::Aevt if current.entity != prior.entity => {
            RoutingKey::sparse(Some(current.entity), Some(current.attribute), None, added)
        }
        IndexOrder::Aevt => sparse_value_routing_key(order, prior, current),
    }
}

fn sparse_value_routing_key(order: IndexOrder, prior: &Datom, current: &Datom) -> RoutingKey {
    let value = minimum_difference(&prior.value, &current.value);
    if value == RoutingValue::exact(&current.value) {
        return RoutingKey::exact(current);
    }
    let entity = match order {
        IndexOrder::Eavt | IndexOrder::Aevt => Some(current.entity),
        IndexOrder::Avet | IndexOrder::Vaet => None,
    };
    RoutingKey::sparse(entity, Some(current.attribute), Some(value), current.added)
}

pub(crate) fn datom_boundary_hash(datom: &Datom) -> Result<Digest, SemanticError> {
    routing_key_hash(&RoutingKey::exact(datom))
}

pub(crate) fn routing_key_hash(key: &RoutingKey) -> Result<Digest, SemanticError> {
    let mut encoded = Vec::new();
    encode_routing_key(&mut encoded, key)?;
    Ok(sha256(&encoded))
}

/// Recovered `TransposedData`, represented with native typed columns.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct LeafSegment {
    pub order: IndexOrder,
    pub history: bool,
    pub entities: Vec<u64>,
    pub attributes: Vec<u32>,
    pub values: Vec<Value>,
    pub transactions: Vec<u64>,
    pub assertions: Vec<bool>,
}

impl LeafSegment {
    pub fn len(&self) -> usize {
        self.entities.len()
    }

    pub fn is_empty(&self) -> bool {
        self.entities.is_empty()
    }

    pub fn datom(&self, index: usize) -> Option<Datom> {
        Some(Datom {
            entity: *self.entities.get(index)?,
            attribute: *self.attributes.get(index)?,
            value: self.values.get(index)?.clone(),
            tx: *self.transactions.get(index)?,
            added: *self.assertions.get(index)?,
        })
    }

    fn first(&self) -> Option<Datom> {
        self.datom(0)
    }

    fn last(&self) -> Option<Datom> {
        self.len()
            .checked_sub(1)
            .and_then(|index| self.datom(index))
    }

    fn datoms(&self) -> Vec<Datom> {
        (0..self.len())
            .map(|index| self.datom(index).expect("validated parallel columns"))
            .collect()
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct DirectoryNode {
    pub order: IndexOrder,
    pub history: bool,
    pub count: u64,
    pub leaves: Vec<ChildRef>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct RootNode {
    pub order: IndexOrder,
    pub history: bool,
    pub count: u64,
    pub directories: Vec<ChildRef>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum TreeNode {
    Leaf(LeafSegment),
    Directory(DirectoryNode),
    Root(RootNode),
}

impl TreeNode {
    pub fn order(&self) -> IndexOrder {
        match self {
            Self::Leaf(node) => node.order,
            Self::Directory(node) => node.order,
            Self::Root(node) => node.order,
        }
    }

    pub fn history(&self) -> bool {
        match self {
            Self::Leaf(node) => node.history,
            Self::Directory(node) => node.history,
            Self::Root(node) => node.history,
        }
    }

    /// Conservative bytes retained by the decoded immutable node itself.
    ///
    /// The encoded payload is accounted separately by the cache because it is
    /// the stable lower-bound observed at the I/O boundary. This count covers
    /// decoded vector capacities and recursively owned value/routing data, so
    /// compactly encoded tuples and sparse keys cannot evade the resident
    /// cache ceiling.
    pub(crate) fn retained_bytes(&self) -> u64 {
        let child_refs = |children: &Vec<ChildRef>| {
            (children.capacity() as u64)
                .saturating_mul(size_of::<ChildRef>() as u64)
                .saturating_add(children.iter().fold(0_u64, |total, child| {
                    total.saturating_add(child.key.retained_heap_bytes())
                }))
        };
        let heap = match self {
            Self::Root(root) => child_refs(&root.directories),
            Self::Directory(directory) => child_refs(&directory.leaves),
            Self::Leaf(leaf) => (leaf.entities.capacity() as u64)
                .saturating_mul(size_of::<u64>() as u64)
                .saturating_add(
                    (leaf.attributes.capacity() as u64).saturating_mul(size_of::<u32>() as u64),
                )
                .saturating_add(
                    (leaf.values.capacity() as u64).saturating_mul(size_of::<Value>() as u64),
                )
                .saturating_add(leaf.values.iter().fold(0_u64, |total, value| {
                    total.saturating_add(value.retained_heap_bytes())
                }))
                .saturating_add(
                    (leaf.transactions.capacity() as u64).saturating_mul(size_of::<u64>() as u64),
                )
                .saturating_add(
                    (leaf.assertions.capacity() as u64).saturating_mul(size_of::<bool>() as u64),
                ),
        };
        (size_of::<Self>() as u64).saturating_add(heap)
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct TreeReadStats {
    pub root_reads: u64,
    pub directory_reads: u64,
    pub leaf_reads: u64,
    pub decoded_bytes: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeSeekResult {
    pub datom: Option<Datom>,
    pub stats: TreeReadStats,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeRangeResult {
    pub datoms: Vec<Datom>,
    pub stats: TreeReadStats,
}

/// Build one current or history tree from an already sorted stream.
///
/// Callers choose membership (all EAVT/AEVT, indexed AVET, refs VAET); this
/// constructor owns only physical order and tree integrity.  Rejecting an
/// unsorted or duplicate stream keeps one canonical tree for one information
/// set.
pub fn build_tree(
    order: IndexOrder,
    history: bool,
    datoms: impl IntoIterator<Item = Datom>,
    config: &TreeConfig,
) -> Result<TreeBuild, SemanticError> {
    config.validate()?;

    let mut nodes = TreeNodeSet::default();
    let mut stats = TreeBuildStats::default();
    let mut leaf = LeafBuilder::new(order, history);
    let mut pending_directory = Vec::new();
    let mut root_directories = Vec::new();
    let mut previous = None::<Datom>;
    let mut tree_first = None::<Datom>;
    let mut tree_last = None::<Datom>;

    for datom in datoms {
        if let Some(previous) = &previous
            && !previous.cmp_in(&datom, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "tree/noncanonical-input",
                "tree input must be strictly ordered in the declared index order",
            ));
        }
        let value_bytes = encode_canonical_value(&datom.value)?;
        let prospective_bytes = leaf.encoded_len_with(value_bytes.len())?;
        if !leaf.is_empty()
            && (leaf.len() == config.max_leaf_datoms
                || prospective_bytes > config.target_leaf_bytes)
        {
            let reference = flush_leaf(&mut leaf, &mut nodes, &mut stats, config)?;
            push_leaf_reference(
                reference,
                &mut pending_directory,
                &mut root_directories,
                &mut nodes,
                &mut stats,
                config,
            )?;
        }

        let prospective_bytes = leaf.encoded_len_with(value_bytes.len())?;
        if prospective_bytes > config.max_leaf_bytes {
            return Err(SemanticError::incorrect(
                "tree/leaf-size-limit",
                format!(
                    "one leaf would require {prospective_bytes} bytes, above max_leaf_bytes {}",
                    config.max_leaf_bytes
                ),
            ));
        }
        if leaf.len() == config.max_leaf_datoms {
            return Err(SemanticError::incorrect(
                "tree/leaf-count-limit",
                "one leaf exceeded max_leaf_datoms",
            ));
        }

        tree_first.get_or_insert_with(|| datom.clone());
        tree_last = Some(datom.clone());
        previous = Some(datom.clone());
        leaf.push(datom, value_bytes.len());
        stats.input_datoms = checked_add_u64(stats.input_datoms, 1, "tree datom count")?;
        stats.peak_live_datoms = stats.peak_live_datoms.max(leaf.len() as u64);
    }

    if !leaf.is_empty() {
        let reference = flush_leaf(&mut leaf, &mut nodes, &mut stats, config)?;
        push_leaf_reference(
            reference,
            &mut pending_directory,
            &mut root_directories,
            &mut nodes,
            &mut stats,
            config,
        )?;
    }
    if !pending_directory.is_empty() {
        let reference = flush_directory(
            &mut pending_directory,
            &mut nodes,
            &mut stats,
            order,
            history,
            config,
        )?;
        push_built_directory_reference(reference, &mut root_directories, &nodes, config)?;
    }

    let root = RootNode {
        order,
        history,
        count: stats.input_datoms,
        directories: root_directories,
    };
    let root_bytes = encode_tree_node(&TreeNode::Root(root))?;
    if root_bytes.len() > config.max_root_bytes {
        return Err(SemanticError::incorrect(
            "tree/root-size-limit",
            format!(
                "root requires {} bytes, above max_root_bytes {}",
                root_bytes.len(),
                config.max_root_bytes
            ),
        ));
    }
    let root_len = root_bytes.len() as u64;
    let (root_hash, inserted) = nodes.insert(root_bytes)?;
    record_node(&mut stats, root_len, inserted)?;
    stats.root_nodes = 1;
    stats.root_bytes = root_len;

    let descriptor = TreeDescriptor {
        root_hash,
        order,
        history,
        count: stats.input_datoms,
        first_hash: tree_first.as_ref().map(datom_boundary_hash).transpose()?,
        last_hash: tree_last.as_ref().map(datom_boundary_hash).transpose()?,
    };
    validate_tree(&descriptor, &nodes)?;
    Ok(TreeBuild {
        descriptor,
        nodes,
        stats,
    })
}

/// Apply localized logical edits by copying only affected paths in the fixed
/// root-directory-leaf tree.
///
/// The old resolver is never cloned into the result. Unchanged directory and
/// leaf references retain their hashes; `new_nodes` is therefore the exact
/// content-first write set for a later PostgreSQL root publication.
pub fn merge_tree(
    descriptor: &TreeDescriptor,
    old_nodes: &TreeNodeSet,
    edits: &TreeMergeEdits,
    config: &TreeConfig,
) -> Result<TreeMerge, SemanticError> {
    config.validate()?;
    validate_merge_edits(descriptor.order, descriptor.history, edits)?;

    let mut context = MergeContext::new(old_nodes);
    let old_root = context.load_root(descriptor)?;
    let removals = combined_removals(edits, descriptor.order)?;
    let points = affected_points(edits, descriptor.order);
    let pair_removals = pair_removals(edits, descriptor.order)?;
    let mut applied = AppliedEdits::default();
    let mut output_directories = Vec::new();
    let mut inside_affected_run = false;

    if old_root.directories.is_empty() {
        let must_reconsider = !points.is_empty()
            || edits
                .affected_ranges
                .iter()
                .any(|range| range_intersects(range, None, None, descriptor.order));
        if must_reconsider {
            context.stats.affected_ranges = 1;
            let merged = apply_leaf_edits(
                Vec::new(),
                &removals,
                &edits.insertions,
                &pair_removals,
                descriptor.order,
                &mut applied,
            )?;
            let leaves = pack_merge_leaves(
                merged,
                descriptor.order,
                descriptor.history,
                config,
                &mut context,
            )?;
            let directories = pack_merge_directories(
                leaves,
                descriptor.order,
                descriptor.history,
                config,
                &mut context,
            )?;
            for directory in directories {
                push_merge_directory_reference(
                    directory,
                    &mut output_directories,
                    descriptor.order,
                    descriptor.history,
                    config,
                    &mut context,
                )?;
            }
        }
    } else {
        for (directory_index, directory_ref) in old_root.directories.iter().enumerate() {
            let lower = if directory_index == 0 {
                None
            } else {
                Some(&directory_ref.key)
            };
            let upper = old_root
                .directories
                .get(directory_index + 1)
                .map(|reference| &reference.key);
            if !interval_is_affected(
                lower,
                upper,
                &points,
                &edits.affected_ranges,
                descriptor.order,
            ) {
                let predecessor = directory_index
                    .checked_sub(1)
                    .map(|index| old_root.directories[index].hash);
                push_merge_directory_reference(
                    MergeChild::reused(directory_ref.clone(), predecessor),
                    &mut output_directories,
                    descriptor.order,
                    descriptor.history,
                    config,
                    &mut context,
                )?;
                context.stats.reused_directory_refs =
                    context.stats.reused_directory_refs.saturating_add(1);
                inside_affected_run = false;
                continue;
            }

            context.stats.affected_directories =
                context.stats.affected_directories.saturating_add(1);
            let directory =
                context.load_directory(directory_ref, descriptor.order, descriptor.history)?;
            let mut output_leaves = Vec::new();
            for (leaf_index, leaf_ref) in directory.leaves.iter().enumerate() {
                let leaf_lower = if directory_index == 0 && leaf_index == 0 {
                    None
                } else {
                    Some(&leaf_ref.key)
                };
                let leaf_upper = directory
                    .leaves
                    .get(leaf_index + 1)
                    .map(|reference| &reference.key)
                    .or(upper);
                if !interval_is_affected(
                    leaf_lower,
                    leaf_upper,
                    &points,
                    &edits.affected_ranges,
                    descriptor.order,
                ) {
                    let predecessor = leaf_index
                        .checked_sub(1)
                        .map(|index| directory.leaves[index].hash);
                    output_leaves.push(MergeChild::reused(leaf_ref.clone(), predecessor));
                    context.stats.reused_leaf_refs =
                        context.stats.reused_leaf_refs.saturating_add(1);
                    inside_affected_run = false;
                    continue;
                }

                if !inside_affected_run {
                    context.stats.affected_ranges = context.stats.affected_ranges.saturating_add(1);
                    inside_affected_run = true;
                }
                context.stats.affected_leaves = context.stats.affected_leaves.saturating_add(1);
                let leaf = context.load_leaf(leaf_ref, descriptor.order, descriptor.history)?;
                let removals =
                    datoms_in_interval(&removals, leaf_lower, leaf_upper, descriptor.order);
                let insertions =
                    datoms_in_interval(&edits.insertions, leaf_lower, leaf_upper, descriptor.order);
                let filtered =
                    datoms_in_interval(&pair_removals, leaf_lower, leaf_upper, descriptor.order);
                let merged = apply_leaf_edits(
                    leaf.datoms(),
                    &removals,
                    &insertions,
                    &filtered,
                    descriptor.order,
                    &mut applied,
                )?;
                output_leaves.extend(pack_merge_leaves(
                    merged,
                    descriptor.order,
                    descriptor.history,
                    config,
                    &mut context,
                )?);
            }

            let directories = pack_merge_directories(
                output_leaves,
                descriptor.order,
                descriptor.history,
                config,
                &mut context,
            )?;
            for directory in directories {
                push_merge_directory_reference(
                    directory,
                    &mut output_directories,
                    descriptor.order,
                    descriptor.history,
                    config,
                    &mut context,
                )?;
            }
        }
    }

    let expected_filtered = edits.no_history_pairs.len().saturating_mul(2) as u64;
    if applied.removals != removals.len() as u64
        || applied.insertions != edits.insertions.len() as u64
        || applied.filtered != expected_filtered
    {
        return Err(fault(
            "tree/unapplied-edit",
            "one or more exact tree edits did not map to an old leaf range",
        ));
    }

    let output_count = sum_child_counts(&output_directories)?;
    let first_hash = output_directories
        .first()
        .map(|reference| routing_key_hash(&reference.key))
        .transpose()?;
    let last_hash = if output_directories.is_empty() {
        None
    } else if output_directories.last().map(|reference| reference.hash)
        == old_root.directories.last().map(|reference| reference.hash)
    {
        descriptor.last_hash
    } else {
        context
            .last_datom(
                output_directories
                    .last()
                    .expect("non-empty root has a final directory"),
                descriptor.order,
                descriptor.history,
            )?
            .as_ref()
            .map(datom_boundary_hash)
            .transpose()?
    };
    let root = RootNode {
        order: descriptor.order,
        history: descriptor.history,
        count: output_count,
        directories: output_directories,
    };
    let root_bytes = encode_tree_node(&TreeNode::Root(root))?;
    if root_bytes.len() > config.max_root_bytes {
        return Err(SemanticError::incorrect(
            "tree/root-size-limit",
            format!(
                "root requires {} bytes, above max_root_bytes {}",
                root_bytes.len(),
                config.max_root_bytes
            ),
        ));
    }
    let root_hash = context.insert_node(root_bytes, MergeNodeKind::Root)?;
    context.stats.removals = applied.removals;
    context.stats.projection_removals = edits.projection_removals.len() as u64;
    context.stats.insertions = applied.insertions;
    context.stats.no_history_pairs = edits.no_history_pairs.len() as u64;
    context.stats.output_datoms = output_count;

    Ok(TreeMerge {
        descriptor: TreeDescriptor {
            root_hash,
            order: descriptor.order,
            history: descriptor.history,
            count: output_count,
            first_hash,
            last_hash,
        },
        new_nodes: context.new_nodes,
        stats: context.stats,
    })
}

/// Decode and authenticate one node.  Node format versioning is independent
/// of the old flat index-segment envelope.
pub fn decode_tree_node(expected_hash: &Digest, bytes: &[u8]) -> Result<TreeNode, SemanticError> {
    if sha256(bytes) != *expected_hash {
        return Err(fault(
            "tree/content-hash-mismatch",
            "tree node bytes do not match the requested content hash",
        ));
    }
    if bytes.len() < EMPTY_NODE_LEN || bytes.len() > MAX_TREE_NODE_BYTES {
        return Err(fault(
            "tree/node-size",
            "tree node is outside the supported canonical size range",
        ));
    }
    if bytes.get(0..4) != Some(TREE_MAGIC.as_slice()) {
        return Err(fault("tree/magic", "tree node has the wrong magic"));
    }
    let version = u16::from_be_bytes(bytes[4..6].try_into().expect("checked header"));
    if version != TREE_FORMAT_VERSION {
        return Err(fault(
            "tree/unsupported-version",
            format!("tree node version {version} is unsupported; expected {TREE_FORMAT_VERSION}"),
        ));
    }
    let kind = bytes[6];
    let order = decode_order(bytes[7])?;
    let history = decode_bool(bytes[8], "history flag")?;
    if bytes[9..12] != [0, 0, 0] {
        return Err(fault(
            "tree/noncanonical-header",
            "tree node reserved header bytes must be zero",
        ));
    }
    let body_len = u32::from_be_bytes(bytes[12..16].try_into().expect("checked header")) as usize;
    let expected_len = HEADER_LEN
        .checked_add(body_len)
        .and_then(|length| length.checked_add(CHECKSUM_LEN))
        .ok_or_else(|| fault("tree/length-overflow", "tree node length overflow"))?;
    if expected_len != bytes.len() {
        return Err(fault(
            "tree/length-mismatch",
            "tree node length does not match its canonical header",
        ));
    }
    let checksum_at = HEADER_LEN + body_len;
    let expected_checksum = sha256(&bytes[..checksum_at]);
    if bytes[checksum_at..] != expected_checksum {
        return Err(fault(
            "tree/checksum-mismatch",
            "tree node checksum does not match its header and body",
        ));
    }

    let mut cursor = TreeCursor::new(&bytes[HEADER_LEN..checksum_at]);
    let node = match kind {
        KIND_LEAF => TreeNode::Leaf(decode_leaf(order, history, &mut cursor)?),
        KIND_DIRECTORY => TreeNode::Directory(decode_directory(order, history, &mut cursor)?),
        KIND_ROOT => TreeNode::Root(decode_root(order, history, &mut cursor)?),
        _ => return Err(fault("tree/invalid-kind", "tree node kind is unknown")),
    };
    cursor.finish()?;
    validate_local_node(&node)?;
    if encode_tree_node(&node)? != bytes {
        return Err(fault(
            "tree/noncanonical-node",
            "tree node does not have a unique canonical encoding",
        ));
    }
    Ok(node)
}

/// Resolve and exhaustively validate a complete tree in memory.
///
/// This is the adversarial/build-time checker. Lazy peer reads can decode one
/// authenticated path at a time; publication and repair tools can use this
/// full walk to detect missing children, kind/order/history substitutions,
/// count mismatches, and overlapping or gapped routing ranges.
pub fn validate_tree(
    descriptor: &TreeDescriptor,
    nodes: &TreeNodeSet,
) -> Result<(), SemanticError> {
    let root_bytes = resolve(nodes, &descriptor.root_hash)?;
    let root = expect_root(decode_tree_node(&descriptor.root_hash, root_bytes)?)?;
    require_tree_identity(
        root.order,
        root.history,
        descriptor.order,
        descriptor.history,
    )?;
    if root.count != descriptor.count {
        return Err(fault(
            "tree/descriptor-count-mismatch",
            "tree descriptor and root disagree on datom count",
        ));
    }

    let mut visited = HashSet::new();
    visited.insert(descriptor.root_hash);
    let span = validate_root_children(&root, nodes, &mut visited)?;
    let first_hash = span.first.as_ref().map(datom_boundary_hash).transpose()?;
    let last_hash = span.last.as_ref().map(datom_boundary_hash).transpose()?;
    if span.count != descriptor.count
        || first_hash != descriptor.first_hash
        || last_hash != descriptor.last_hash
    {
        return Err(fault(
            "tree/descriptor-range-mismatch",
            "tree descriptor does not match the resolved root range",
        ));
    }
    Ok(())
}

/// Return the first datom greater than or equal to a complete index key.
/// Routing follows sparse lower bounds in the declared order for EAVT, AEVT,
/// AVET, and VAET alike.
pub fn seek_tree(
    descriptor: &TreeDescriptor,
    nodes: &TreeNodeSet,
    key: &Datom,
) -> Result<TreeSeekResult, SemanticError> {
    let mut stats = TreeReadStats::default();
    let root = load_root(descriptor, nodes, &mut stats)?;
    if root.directories.is_empty() {
        return Ok(TreeSeekResult { datom: None, stats });
    }

    let mut root_index = floor_child(&root.directories, key, descriptor.order);
    while root_index < root.directories.len() {
        let directory_ref = &root.directories[root_index];
        let directory = load_directory(
            directory_ref,
            descriptor.order,
            descriptor.history,
            nodes,
            &mut stats,
        )?;
        let mut leaf_index = if root_index == floor_child(&root.directories, key, descriptor.order)
        {
            floor_child(&directory.leaves, key, descriptor.order)
        } else {
            0
        };
        while leaf_index < directory.leaves.len() {
            let leaf = load_leaf(
                &directory.leaves[leaf_index],
                descriptor.order,
                descriptor.history,
                nodes,
                &mut stats,
            )?;
            let index = leaf_lower_bound(&leaf, key, descriptor.order);
            if let Some(datom) = leaf.datom(index) {
                return Ok(TreeSeekResult {
                    datom: Some(datom),
                    stats,
                });
            }
            leaf_index += 1;
        }
        root_index += 1;
    }
    Ok(TreeSeekResult { datom: None, stats })
}

/// Read an inclusive-lower/exclusive-upper range without touching unrelated
/// leaves. `None` denotes an unbounded side.
pub fn range_tree(
    descriptor: &TreeDescriptor,
    nodes: &TreeNodeSet,
    start: Option<&Datom>,
    end: Option<&Datom>,
) -> Result<TreeRangeResult, SemanticError> {
    if let (Some(start), Some(end)) = (start, end)
        && !start.cmp_in(end, descriptor.order).is_lt()
    {
        return Err(SemanticError::incorrect(
            "tree/invalid-range",
            "tree range start must be strictly below its exclusive end",
        ));
    }
    let mut stats = TreeReadStats::default();
    let root = load_root(descriptor, nodes, &mut stats)?;
    let mut output = Vec::new();
    if root.directories.is_empty() {
        return Ok(TreeRangeResult {
            datoms: output,
            stats,
        });
    }

    let first_directory = start.map_or(0, |key| {
        floor_child(&root.directories, key, descriptor.order)
    });
    for directory_ref in root.directories.iter().skip(first_directory) {
        if end.is_some_and(|end| !directory_ref.key.cmp_datom(end, descriptor.order).is_lt()) {
            break;
        }
        let directory = load_directory(
            directory_ref,
            descriptor.order,
            descriptor.history,
            nodes,
            &mut stats,
        )?;
        let first_leaf = start.map_or(0, |key| {
            floor_child(&directory.leaves, key, descriptor.order)
        });
        for leaf_ref in directory.leaves.iter().skip(first_leaf) {
            if end.is_some_and(|end| !leaf_ref.key.cmp_datom(end, descriptor.order).is_lt()) {
                break;
            }
            let leaf = load_leaf(
                leaf_ref,
                descriptor.order,
                descriptor.history,
                nodes,
                &mut stats,
            )?;
            for index in 0..leaf.len() {
                let datom = leaf.datom(index).expect("validated leaf");
                if start.is_some_and(|start| datom.cmp_in(start, descriptor.order).is_lt()) {
                    continue;
                }
                if end.is_some_and(|end| !datom.cmp_in(end, descriptor.order).is_lt()) {
                    return Ok(TreeRangeResult {
                        datoms: output,
                        stats,
                    });
                }
                output.push(datom);
            }
        }
    }
    Ok(TreeRangeResult {
        datoms: output,
        stats,
    })
}

#[derive(Clone, Copy)]
enum MergeNodeKind {
    Leaf,
    Directory,
    Root,
}

#[derive(Default)]
struct AppliedEdits {
    removals: u64,
    insertions: u64,
    filtered: u64,
}

#[derive(Clone)]
enum MergeBoundary {
    New,
    Reused { predecessor: Option<Digest> },
}

#[derive(Clone)]
struct MergeChild {
    reference: ChildRef,
    boundary: MergeBoundary,
}

impl MergeChild {
    fn new(reference: ChildRef) -> Self {
        Self {
            reference,
            boundary: MergeBoundary::New,
        }
    }

    fn reused(reference: ChildRef, predecessor: Option<Digest>) -> Self {
        Self {
            reference,
            boundary: MergeBoundary::Reused { predecessor },
        }
    }
}

struct MergeContext<'a> {
    old_nodes: &'a TreeNodeSet,
    new_nodes: TreeNodeSet,
    loaded_old: HashSet<Digest>,
    stats: TreeMergeStats,
}

impl<'a> MergeContext<'a> {
    fn new(old_nodes: &'a TreeNodeSet) -> Self {
        Self {
            old_nodes,
            new_nodes: TreeNodeSet::default(),
            loaded_old: HashSet::new(),
            stats: TreeMergeStats::default(),
        }
    }

    fn load_root(&mut self, descriptor: &TreeDescriptor) -> Result<RootNode, SemanticError> {
        let root = expect_root(self.load_node(&descriptor.root_hash, MergeNodeKind::Root)?)?;
        require_tree_identity(
            root.order,
            root.history,
            descriptor.order,
            descriptor.history,
        )?;
        let first_matches = match descriptor.first_hash {
            None => root.directories.is_empty(),
            Some(first_hash) => root.directories.first().is_some_and(|reference| {
                routing_key_hash(&reference.key).is_ok_and(|hash| hash == first_hash)
            }),
        };
        if root.count != descriptor.count
            || !first_matches
            || (root.count == 0) != descriptor.last_hash.is_none()
        {
            return Err(fault(
                "tree/descriptor-range-mismatch",
                "tree descriptor does not match its authenticated root",
            ));
        }
        Ok(root)
    }

    fn load_directory(
        &mut self,
        reference: &ChildRef,
        order: IndexOrder,
        history: bool,
    ) -> Result<DirectoryNode, SemanticError> {
        let directory =
            expect_directory(self.load_node(&reference.hash, MergeNodeKind::Directory)?)?;
        require_tree_identity(directory.order, directory.history, order, history)?;
        require_child_key_reference(
            reference,
            directory.count,
            directory.leaves.first().map(|leaf| &leaf.key),
            order,
        )?;
        Ok(directory)
    }

    fn load_leaf(
        &mut self,
        reference: &ChildRef,
        order: IndexOrder,
        history: bool,
    ) -> Result<LeafSegment, SemanticError> {
        let leaf = expect_leaf(self.load_node(&reference.hash, MergeNodeKind::Leaf)?)?;
        require_tree_identity(leaf.order, leaf.history, order, history)?;
        require_child_datom_reference(reference, leaf.len() as u64, leaf.first().as_ref(), order)?;
        Ok(leaf)
    }

    fn load_node(&mut self, hash: &Digest, kind: MergeNodeKind) -> Result<TreeNode, SemanticError> {
        let from_new = self.new_nodes.get(hash).is_some();
        if !from_new {
            let byte_count = self.old_nodes.get(hash).map(<[u8]>::len).ok_or_else(|| {
                fault(
                    "tree/missing-node",
                    format!("tree content {} is missing", short_hash(hash)),
                )
            })?;
            if self.loaded_old.insert(*hash) {
                match kind {
                    MergeNodeKind::Leaf => {
                        self.stats.leaf_reads = self.stats.leaf_reads.saturating_add(1)
                    }
                    MergeNodeKind::Directory => {
                        self.stats.directory_reads = self.stats.directory_reads.saturating_add(1)
                    }
                    MergeNodeKind::Root => {
                        self.stats.root_reads = self.stats.root_reads.saturating_add(1)
                    }
                }
                self.stats.decoded_bytes =
                    self.stats.decoded_bytes.saturating_add(byte_count as u64);
            }
        }
        let bytes = self
            .new_nodes
            .get(hash)
            .or_else(|| self.old_nodes.get(hash))
            .expect("tree node existence was checked");
        decode_tree_node(hash, bytes)
    }

    fn insert_node(
        &mut self,
        bytes: Vec<u8>,
        kind: MergeNodeKind,
    ) -> Result<Digest, SemanticError> {
        let hash = sha256(&bytes);
        if let Some(existing) = self.old_nodes.get(&hash) {
            if existing != bytes {
                return Err(fault(
                    "tree/content-hash-collision",
                    "new tree content collides with different old bytes",
                ));
            }
            self.stats.reused_hashes = self.stats.reused_hashes.saturating_add(1);
            return Ok(hash);
        }
        if let Some(existing) = self.new_nodes.get(&hash) {
            if existing != bytes {
                return Err(fault(
                    "tree/content-hash-collision",
                    "new tree content collides with different candidate bytes",
                ));
            }
            self.stats.reused_hashes = self.stats.reused_hashes.saturating_add(1);
            return Ok(hash);
        }

        let byte_count = bytes.len() as u64;
        let (stored_hash, inserted) = self.new_nodes.insert(bytes)?;
        debug_assert!(inserted);
        debug_assert_eq!(stored_hash, hash);
        self.stats.nodes_written = self.stats.nodes_written.saturating_add(1);
        self.stats.encoded_bytes_written =
            self.stats.encoded_bytes_written.saturating_add(byte_count);
        match kind {
            MergeNodeKind::Leaf => {
                self.stats.leaf_nodes_written = self.stats.leaf_nodes_written.saturating_add(1)
            }
            MergeNodeKind::Directory => {
                self.stats.directory_nodes_written =
                    self.stats.directory_nodes_written.saturating_add(1)
            }
            MergeNodeKind::Root => {
                self.stats.root_nodes_written = self.stats.root_nodes_written.saturating_add(1)
            }
        }
        Ok(hash)
    }

    fn last_datom(
        &mut self,
        directory_ref: &ChildRef,
        order: IndexOrder,
        history: bool,
    ) -> Result<Option<Datom>, SemanticError> {
        let directory = self.load_directory(directory_ref, order, history)?;
        let leaf_ref = directory.leaves.last().ok_or_else(|| {
            fault(
                "tree/empty-directory",
                "non-empty root resolved an empty directory",
            )
        })?;
        Ok(self.load_leaf(leaf_ref, order, history)?.last())
    }

    fn first_datom(
        &mut self,
        directory_ref: &ChildRef,
        order: IndexOrder,
        history: bool,
    ) -> Result<Option<Datom>, SemanticError> {
        let directory = self.load_directory(directory_ref, order, history)?;
        let leaf_ref = directory.leaves.first().ok_or_else(|| {
            fault(
                "tree/empty-directory",
                "non-empty root resolved an empty directory",
            )
        })?;
        Ok(self.load_leaf(leaf_ref, order, history)?.first())
    }

    fn route_leaf_reference(
        &mut self,
        previous: Option<&ChildRef>,
        child: &MergeChild,
        order: IndexOrder,
        history: bool,
    ) -> Result<ChildRef, SemanticError> {
        if let MergeBoundary::Reused { predecessor } = child.boundary
            && predecessor == previous.map(|reference| reference.hash)
        {
            return Ok(child.reference.clone());
        }
        let current = self.load_leaf(&child.reference, order, history)?;
        let current_first = current
            .first()
            .ok_or_else(|| fault("tree/empty-leaf", "routing repair resolved an empty leaf"))?;
        let mut reference = child.reference.clone();
        reference.key = if let Some(previous) = previous {
            let prior = self.load_leaf(previous, order, history)?;
            let prior_last = prior.last().ok_or_else(|| {
                fault(
                    "tree/empty-leaf",
                    "routing repair resolved an empty prior leaf",
                )
            })?;
            sparse_routing_key(order, &prior_last, &current_first)
        } else {
            RoutingKey::exact(&current_first)
        };
        Ok(reference)
    }

    fn route_directory_reference(
        &mut self,
        previous: Option<&ChildRef>,
        child: &MergeChild,
        order: IndexOrder,
        history: bool,
    ) -> Result<ChildRef, SemanticError> {
        if let MergeBoundary::Reused { predecessor } = child.boundary
            && predecessor == previous.map(|reference| reference.hash)
        {
            return Ok(child.reference.clone());
        }
        let current_first = self
            .first_datom(&child.reference, order, history)?
            .ok_or_else(|| {
                fault(
                    "tree/empty-directory",
                    "routing repair resolved an empty current directory",
                )
            })?;
        let mut reference = child.reference.clone();
        reference.key = if let Some(previous) = previous {
            let prior_last = self.last_datom(previous, order, history)?.ok_or_else(|| {
                fault(
                    "tree/empty-directory",
                    "routing repair resolved an empty prior directory",
                )
            })?;
            sparse_routing_key(order, &prior_last, &current_first)
        } else {
            RoutingKey::exact(&current_first)
        };
        Ok(reference)
    }
}

fn validate_merge_edits(
    order: IndexOrder,
    history: bool,
    edits: &TreeMergeEdits,
) -> Result<(), SemanticError> {
    validate_sorted_edit_stream(&edits.removals, order, "removals")?;
    validate_sorted_edit_stream(&edits.projection_removals, order, "projection removals")?;
    validate_sorted_edit_stream(&edits.insertions, order, "insertions")?;
    validate_persistent_index_datoms(order, &edits.removals)?;
    validate_persistent_index_datoms(order, &edits.projection_removals)?;
    validate_persistent_index_datoms(order, &edits.insertions)?;

    if history && !edits.removals.is_empty() {
        return Err(SemanticError::incorrect(
            "tree/history-removal",
            "retained history accepts additions and exact noHistory pairs, not arbitrary removals",
        ));
    }
    if !history && !edits.no_history_pairs.is_empty() {
        return Err(SemanticError::incorrect(
            "tree/current-no-history-filter",
            "noHistory pair filtering applies only to retained history",
        ));
    }
    if !history
        && edits
            .removals
            .iter()
            .chain(&edits.insertions)
            .any(|datom| !datom.added)
    {
        return Err(SemanticError::incorrect(
            "tree/current-retraction",
            "current-tree edits must remove or insert assertion datoms",
        ));
    }
    for removal in edits.removals.iter().chain(&edits.projection_removals) {
        if edits
            .insertions
            .binary_search_by(|candidate| candidate.cmp_in(removal, order))
            .is_ok()
        {
            return Err(SemanticError::incorrect(
                "tree/contradictory-edit",
                "the same exact datom cannot be both removed and inserted",
            ));
        }
    }

    let mut pair_datoms = Vec::with_capacity(edits.no_history_pairs.len().saturating_mul(2));
    let mut previous_retraction = None::<&Datom>;
    for pair in &edits.no_history_pairs {
        if pair.retraction.added
            || !pair.assertion.added
            || !same_eav(&pair.retraction, &pair.assertion)
            || !pair.retraction.cmp_in(&pair.assertion, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "tree/invalid-no-history-pair",
                "noHistory filter requires an ordered exact retraction/assertion E/A/V pair",
            ));
        }
        if previous_retraction
            .is_some_and(|previous| !previous.cmp_in(&pair.retraction, order).is_lt())
        {
            return Err(SemanticError::incorrect(
                "tree/noncanonical-no-history-pairs",
                "noHistory pairs must be strictly ordered by retraction",
            ));
        }
        previous_retraction = Some(&pair.retraction);
        pair_datoms.push(pair.retraction.clone());
        pair_datoms.push(pair.assertion.clone());
    }
    pair_datoms.sort_by(|left, right| left.cmp_in(right, order));
    if pair_datoms
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], order).is_lt())
    {
        return Err(SemanticError::incorrect(
            "tree/duplicate-no-history-datom",
            "one exact history datom cannot participate in multiple pair removals",
        ));
    }
    validate_persistent_index_datoms(order, &pair_datoms)?;

    for (index, range) in edits.affected_ranges.iter().enumerate() {
        if let (Some(start), Some(end)) = (&range.start, &range.end)
            && !start.cmp_in(end, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "tree/invalid-affected-range",
                "affected range start must be below its exclusive end",
            ));
        }
        if index > 0 {
            let previous = &edits.affected_ranges[index - 1];
            let (Some(previous_end), Some(start)) = (&previous.end, &range.start) else {
                return Err(SemanticError::incorrect(
                    "tree/noncanonical-affected-ranges",
                    "unbounded affected ranges must occur only at an outer edge",
                ));
            };
            if previous_end.cmp_in(start, order).is_gt() {
                return Err(SemanticError::incorrect(
                    "tree/noncanonical-affected-ranges",
                    "affected ranges must be sorted and non-overlapping",
                ));
            }
        }
    }
    Ok(())
}

fn validate_sorted_edit_stream(
    datoms: &[Datom],
    order: IndexOrder,
    label: &str,
) -> Result<(), SemanticError> {
    if datoms
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], order).is_lt())
    {
        return Err(SemanticError::incorrect(
            "tree/noncanonical-edits",
            format!("tree {label} must be strictly ordered"),
        ));
    }
    Ok(())
}

fn affected_points(edits: &TreeMergeEdits, order: IndexOrder) -> Vec<Datom> {
    let mut points = edits
        .removals
        .iter()
        .chain(&edits.projection_removals)
        .chain(&edits.insertions)
        .cloned()
        .collect::<Vec<_>>();
    for pair in &edits.no_history_pairs {
        points.push(pair.retraction.clone());
        points.push(pair.assertion.clone());
    }
    points.sort_by(|left, right| left.cmp_in(right, order));
    points.dedup_by(|right, left| same_datom(left, right));
    points
}

fn combined_removals(
    edits: &TreeMergeEdits,
    order: IndexOrder,
) -> Result<Vec<Datom>, SemanticError> {
    let mut removals = edits
        .removals
        .iter()
        .chain(&edits.projection_removals)
        .cloned()
        .collect::<Vec<_>>();
    removals.sort_by(|left, right| left.cmp_in(right, order));
    if removals
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], order).is_lt())
    {
        return Err(SemanticError::incorrect(
            "tree/duplicate-removal",
            "ordinary and projection removal streams overlap",
        ));
    }
    Ok(removals)
}

fn pair_removals(edits: &TreeMergeEdits, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
    let mut removals = edits
        .no_history_pairs
        .iter()
        .flat_map(|pair| [pair.retraction.clone(), pair.assertion.clone()])
        .collect::<Vec<_>>();
    removals.sort_by(|left, right| left.cmp_in(right, order));
    if removals
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], order).is_lt())
    {
        return Err(SemanticError::incorrect(
            "tree/duplicate-no-history-datom",
            "one exact history datom cannot participate in multiple pair removals",
        ));
    }
    Ok(removals)
}

fn interval_is_affected(
    lower: Option<&RoutingKey>,
    upper: Option<&RoutingKey>,
    points: &[Datom],
    ranges: &[TreeAffectedRange],
    order: IndexOrder,
) -> bool {
    points
        .iter()
        .any(|point| key_in_interval(point, lower, upper, order))
        || ranges
            .iter()
            .any(|range| range_intersects(range, lower, upper, order))
}

fn key_in_interval(
    key: &Datom,
    lower: Option<&RoutingKey>,
    upper: Option<&RoutingKey>,
    order: IndexOrder,
) -> bool {
    lower.is_none_or(|lower| !lower.cmp_datom(key, order).is_gt())
        && upper.is_none_or(|upper| upper.cmp_datom(key, order).is_gt())
}

fn range_intersects(
    range: &TreeAffectedRange,
    lower: Option<&RoutingKey>,
    upper: Option<&RoutingKey>,
    order: IndexOrder,
) -> bool {
    range
        .end
        .as_ref()
        .is_none_or(|end| lower.is_none_or(|lower| lower.cmp_datom(end, order).is_lt()))
        && range
            .start
            .as_ref()
            .is_none_or(|start| upper.is_none_or(|upper| upper.cmp_datom(start, order).is_gt()))
}

fn datoms_in_interval(
    datoms: &[Datom],
    lower: Option<&RoutingKey>,
    upper: Option<&RoutingKey>,
    order: IndexOrder,
) -> Vec<Datom> {
    datoms
        .iter()
        .filter(|datom| key_in_interval(datom, lower, upper, order))
        .cloned()
        .collect()
}

fn apply_leaf_edits(
    mut datoms: Vec<Datom>,
    removals: &[Datom],
    insertions: &[Datom],
    filtered: &[Datom],
    order: IndexOrder,
    applied: &mut AppliedEdits,
) -> Result<Vec<Datom>, SemanticError> {
    for removal in removals {
        remove_exact(&mut datoms, removal, order, "tree/remove-missing")?;
        applied.removals = applied.removals.saturating_add(1);
    }
    for insertion in insertions {
        let position = datoms
            .binary_search_by(|candidate| candidate.cmp_in(insertion, order))
            .unwrap_or_else(|position| position);
        if datoms
            .get(position)
            .is_some_and(|candidate| same_datom(candidate, insertion))
        {
            return Err(SemanticError::incorrect(
                "tree/duplicate-insertion",
                "tree insertion already exists in the old value",
            ));
        }
        datoms.insert(position, insertion.clone());
        applied.insertions = applied.insertions.saturating_add(1);
    }
    for removal in filtered {
        remove_exact(&mut datoms, removal, order, "tree/no-history-pair-missing")?;
        applied.filtered = applied.filtered.saturating_add(1);
    }
    Ok(datoms)
}

fn remove_exact(
    datoms: &mut Vec<Datom>,
    removal: &Datom,
    order: IndexOrder,
    code: &'static str,
) -> Result<(), SemanticError> {
    let position = datoms
        .binary_search_by(|candidate| candidate.cmp_in(removal, order))
        .map_err(|_| {
            SemanticError::incorrect(code, "exact datom removal is absent from the merged range")
        })?;
    if !same_datom(&datoms[position], removal) {
        return Err(SemanticError::incorrect(
            code,
            "exact datom removal does not match stored content",
        ));
    }
    datoms.remove(position);
    Ok(())
}

fn pack_merge_leaves(
    datoms: Vec<Datom>,
    order: IndexOrder,
    history: bool,
    config: &TreeConfig,
    context: &mut MergeContext<'_>,
) -> Result<Vec<MergeChild>, SemanticError> {
    let mut references = Vec::new();
    let mut builder = LeafBuilder::new(order, history);
    let mut previous = None::<Datom>;
    for datom in datoms {
        if previous
            .as_ref()
            .is_some_and(|previous| !previous.cmp_in(&datom, order).is_lt())
        {
            return Err(fault(
                "tree/noncanonical-merge-output",
                "merged leaf datoms are not strictly ordered",
            ));
        }
        let value_bytes = encode_canonical_value(&datom.value)?;
        let prospective_bytes = builder.encoded_len_with(value_bytes.len())?;
        if !builder.is_empty()
            && (builder.len() == config.max_leaf_datoms
                || prospective_bytes > config.target_leaf_bytes)
        {
            references.push(flush_merge_leaf(&mut builder, context, config)?);
        }
        let prospective_bytes = builder.encoded_len_with(value_bytes.len())?;
        if prospective_bytes > config.max_leaf_bytes {
            return Err(SemanticError::incorrect(
                "tree/leaf-size-limit",
                format!(
                    "one leaf would require {prospective_bytes} bytes, above max_leaf_bytes {}",
                    config.max_leaf_bytes
                ),
            ));
        }
        if builder.len() == config.max_leaf_datoms {
            return Err(SemanticError::incorrect(
                "tree/leaf-count-limit",
                "one merged leaf exceeded max_leaf_datoms",
            ));
        }
        previous = Some(datom.clone());
        builder.push(datom, value_bytes.len());
    }
    if !builder.is_empty() {
        references.push(flush_merge_leaf(&mut builder, context, config)?);
    }
    Ok(references)
}

fn flush_merge_leaf(
    builder: &mut LeafBuilder,
    context: &mut MergeContext<'_>,
    config: &TreeConfig,
) -> Result<MergeChild, SemanticError> {
    let leaf = builder.take();
    let first = leaf.first().expect("only non-empty leaves are flushed");
    let count = leaf.len() as u64;
    let bytes = encode_tree_node(&TreeNode::Leaf(leaf))?;
    if bytes.len() > config.max_leaf_bytes {
        return Err(SemanticError::incorrect(
            "tree/leaf-size-limit",
            "encoded merged leaf exceeded max_leaf_bytes",
        ));
    }
    let hash = context.insert_node(bytes, MergeNodeKind::Leaf)?;
    Ok(MergeChild::new(ChildRef {
        key: RoutingKey::exact(&first),
        hash,
        count,
    }))
}

fn pack_merge_directories(
    leaves: Vec<MergeChild>,
    order: IndexOrder,
    history: bool,
    config: &TreeConfig,
    context: &mut MergeContext<'_>,
) -> Result<Vec<MergeChild>, SemanticError> {
    let mut directories = Vec::new();
    let mut pending = Vec::new();
    for leaf in leaves {
        let routed = context.route_leaf_reference(pending.last(), &leaf, order, history)?;
        let would_exceed_count = pending.len() == config.max_leaves_per_directory;
        let would_exceed_bytes =
            directory_encoded_len(&pending, Some(&routed))? > config.max_directory_bytes;
        if !pending.is_empty() && (would_exceed_count || would_exceed_bytes) {
            directories.push(flush_merge_directory(
                &mut pending,
                order,
                history,
                config,
                context,
            )?);
        }
        let routed = context.route_leaf_reference(pending.last(), &leaf, order, history)?;
        if directory_encoded_len(&pending, Some(&routed))? > config.max_directory_bytes {
            return Err(SemanticError::incorrect(
                "tree/directory-size-limit",
                "one directory entry exceeds max_directory_bytes",
            ));
        }
        pending.push(routed);
    }
    if !pending.is_empty() {
        directories.push(flush_merge_directory(
            &mut pending,
            order,
            history,
            config,
            context,
        )?);
    }
    Ok(directories)
}

fn flush_merge_directory(
    leaves: &mut Vec<ChildRef>,
    order: IndexOrder,
    history: bool,
    config: &TreeConfig,
    context: &mut MergeContext<'_>,
) -> Result<MergeChild, SemanticError> {
    let leaves = std::mem::take(leaves);
    let first = leaves
        .first()
        .expect("only non-empty directories are flushed")
        .key
        .clone();
    let count = sum_child_counts(&leaves)?;
    let bytes = encode_tree_node(&TreeNode::Directory(DirectoryNode {
        order,
        history,
        count,
        leaves,
    }))?;
    if bytes.len() > config.max_directory_bytes {
        return Err(SemanticError::incorrect(
            "tree/directory-size-limit",
            "encoded merged directory exceeded max_directory_bytes",
        ));
    }
    let hash = context.insert_node(bytes, MergeNodeKind::Directory)?;
    Ok(MergeChild::new(ChildRef {
        key: first,
        hash,
        count,
    }))
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

fn load_root(
    descriptor: &TreeDescriptor,
    nodes: &TreeNodeSet,
    stats: &mut TreeReadStats,
) -> Result<RootNode, SemanticError> {
    let bytes = resolve(nodes, &descriptor.root_hash)?;
    stats.root_reads += 1;
    stats.decoded_bytes = checked_add_u64(stats.decoded_bytes, bytes.len() as u64, "read bytes")?;
    let root = expect_root(decode_tree_node(&descriptor.root_hash, bytes)?)?;
    require_tree_identity(
        root.order,
        root.history,
        descriptor.order,
        descriptor.history,
    )?;
    if root.count != descriptor.count {
        return Err(fault(
            "tree/descriptor-count-mismatch",
            "tree descriptor and root disagree on datom count",
        ));
    }
    Ok(root)
}

fn load_directory(
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
    nodes: &TreeNodeSet,
    stats: &mut TreeReadStats,
) -> Result<DirectoryNode, SemanticError> {
    let bytes = resolve(nodes, &reference.hash)?;
    stats.directory_reads += 1;
    stats.decoded_bytes = checked_add_u64(stats.decoded_bytes, bytes.len() as u64, "read bytes")?;
    let directory = expect_directory(decode_tree_node(&reference.hash, bytes)?)?;
    require_tree_identity(directory.order, directory.history, order, history)?;
    require_child_key_reference(
        reference,
        directory.count,
        directory.leaves.first().map(|r| &r.key),
        order,
    )?;
    Ok(directory)
}

fn load_leaf(
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
    nodes: &TreeNodeSet,
    stats: &mut TreeReadStats,
) -> Result<LeafSegment, SemanticError> {
    let bytes = resolve(nodes, &reference.hash)?;
    stats.leaf_reads += 1;
    stats.decoded_bytes = checked_add_u64(stats.decoded_bytes, bytes.len() as u64, "read bytes")?;
    let leaf = expect_leaf(decode_tree_node(&reference.hash, bytes)?)?;
    require_tree_identity(leaf.order, leaf.history, order, history)?;
    require_child_datom_reference(reference, leaf.len() as u64, leaf.first().as_ref(), order)?;
    Ok(leaf)
}

fn floor_child(children: &[ChildRef], key: &Datom, order: IndexOrder) -> usize {
    children
        .partition_point(|reference| !reference.key.cmp_datom(key, order).is_gt())
        .saturating_sub(1)
}

fn leaf_lower_bound(leaf: &LeafSegment, key: &Datom, order: IndexOrder) -> usize {
    let mut low = 0;
    let mut high = leaf.len();
    while low < high {
        let middle = low + (high - low) / 2;
        if leaf
            .datom(middle)
            .expect("validated leaf")
            .cmp_in(key, order)
            .is_lt()
        {
            low = middle + 1;
        } else {
            high = middle;
        }
    }
    low
}

fn validate_root_children(
    root: &RootNode,
    nodes: &TreeNodeSet,
    visited: &mut HashSet<Digest>,
) -> Result<Span, SemanticError> {
    let mut combined = Span::default();
    for reference in &root.directories {
        if !visited.insert(reference.hash) {
            return Err(fault(
                "tree/duplicate-child",
                "one tree cannot route to the same content node twice",
            ));
        }
        let directory = expect_directory(decode_tree_node(
            &reference.hash,
            resolve(nodes, &reference.hash)?,
        )?)?;
        require_tree_identity(directory.order, directory.history, root.order, root.history)?;
        let span = validate_directory_children(&directory, nodes, visited)?;
        require_child_span(reference, &span, combined.last.as_ref(), root.order)?;
        combined.push(span, root.order)?;
    }
    if combined.count != root.count {
        return Err(fault(
            "tree/root-count-mismatch",
            "root count does not equal its resolved directory counts",
        ));
    }
    Ok(combined)
}

fn validate_directory_children(
    directory: &DirectoryNode,
    nodes: &TreeNodeSet,
    visited: &mut HashSet<Digest>,
) -> Result<Span, SemanticError> {
    let mut combined = Span::default();
    for reference in &directory.leaves {
        if !visited.insert(reference.hash) {
            return Err(fault(
                "tree/duplicate-child",
                "one tree cannot route to the same content node twice",
            ));
        }
        let leaf = expect_leaf(decode_tree_node(
            &reference.hash,
            resolve(nodes, &reference.hash)?,
        )?)?;
        require_tree_identity(leaf.order, leaf.history, directory.order, directory.history)?;
        let span = Span {
            first: leaf.first(),
            last: leaf.last(),
            count: leaf.len() as u64,
        };
        require_child_span(reference, &span, combined.last.as_ref(), directory.order)?;
        combined.push(span, directory.order)?;
    }
    if combined.count != directory.count {
        return Err(fault(
            "tree/directory-count-mismatch",
            "directory count does not equal its resolved leaf counts",
        ));
    }
    Ok(combined)
}

#[derive(Default)]
struct Span {
    first: Option<Datom>,
    last: Option<Datom>,
    count: u64,
}

impl Span {
    fn push(&mut self, other: Self, order: IndexOrder) -> Result<(), SemanticError> {
        let Some(other_first) = other.first else {
            if other.count != 0 || other.last.is_some() {
                return Err(fault(
                    "tree/invalid-empty-span",
                    "tree child has an invalid empty span",
                ));
            }
            return Ok(());
        };
        let other_last = other.last.ok_or_else(|| {
            fault(
                "tree/invalid-span",
                "non-empty tree child is missing its upper bound",
            )
        })?;
        if let Some(previous_last) = &self.last {
            if !previous_last.cmp_in(&other_first, order).is_lt() {
                return Err(fault(
                    "tree/range-overlap",
                    "adjacent immutable tree child ranges overlap or are out of order",
                ));
            }
        } else {
            self.first = Some(other_first.clone());
        }
        self.last = Some(other_last);
        self.count = checked_add_u64(self.count, other.count, "tree span count")?;
        Ok(())
    }
}

fn require_child_count(reference: &ChildRef, actual_count: u64) -> Result<(), SemanticError> {
    if reference.count != actual_count {
        return Err(fault(
            "tree/child-count-mismatch",
            "tree child reference count does not match resolved content",
        ));
    }
    Ok(())
}

fn require_child_key_reference(
    reference: &ChildRef,
    actual_count: u64,
    actual_first: Option<&RoutingKey>,
    order: IndexOrder,
) -> Result<(), SemanticError> {
    require_child_count(reference, actual_count)?;
    if actual_first.is_none_or(|first| reference.key.cmp_key(first, order).is_gt()) {
        return Err(fault(
            "tree/child-range-mismatch",
            "tree child routing key is above its resolved content",
        ));
    }
    Ok(())
}

fn require_child_datom_reference(
    reference: &ChildRef,
    actual_count: u64,
    actual_first: Option<&Datom>,
    order: IndexOrder,
) -> Result<(), SemanticError> {
    require_child_count(reference, actual_count)?;
    if actual_first.is_none_or(|first| reference.key.cmp_datom(first, order).is_gt()) {
        return Err(fault(
            "tree/child-range-mismatch",
            format!(
                "tree child routing key {:?} is above its resolved first datom {:?} in {order:?}",
                reference.key, actual_first
            ),
        ));
    }
    Ok(())
}

fn require_child_span(
    reference: &ChildRef,
    span: &Span,
    prior_last: Option<&Datom>,
    order: IndexOrder,
) -> Result<(), SemanticError> {
    require_child_count(reference, span.count)?;
    let first = span.first.as_ref().ok_or_else(|| {
        fault(
            "tree/child-range-mismatch",
            "non-empty child reference resolved an empty span",
        )
    })?;
    let expected = prior_last.map_or_else(
        || RoutingKey::exact(first),
        |prior| sparse_routing_key(order, prior, first),
    );
    if reference.key != expected {
        return Err(fault(
            "tree/noncanonical-routing-key",
            "tree child routing key is not the canonical sparse separator",
        ));
    }
    if prior_last.is_some_and(|prior| !reference.key.cmp_datom(prior, order).is_gt())
        || reference.key.cmp_datom(first, order).is_gt()
    {
        return Err(fault(
            "tree/invalid-routing-boundary",
            "routing separator must be above the prior span and at or below its child span",
        ));
    }
    Ok(())
}

fn require_tree_identity(
    actual_order: IndexOrder,
    actual_history: bool,
    expected_order: IndexOrder,
    expected_history: bool,
) -> Result<(), SemanticError> {
    if actual_order != expected_order || actual_history != expected_history {
        return Err(fault(
            "tree/identity-mismatch",
            "tree parent and child disagree on index order or history projection",
        ));
    }
    Ok(())
}

fn resolve<'a>(nodes: &'a TreeNodeSet, hash: &Digest) -> Result<&'a [u8], SemanticError> {
    nodes.get(hash).ok_or_else(|| {
        fault(
            "tree/missing-node",
            format!("tree content {} is missing", short_hash(hash)),
        )
    })
}

fn expect_root(node: TreeNode) -> Result<RootNode, SemanticError> {
    match node {
        TreeNode::Root(node) => Ok(node),
        _ => Err(fault(
            "tree/root-kind-mismatch",
            "tree root hash did not resolve to a root node",
        )),
    }
}

fn expect_directory(node: TreeNode) -> Result<DirectoryNode, SemanticError> {
    match node {
        TreeNode::Directory(node) => Ok(node),
        _ => Err(fault(
            "tree/directory-kind-mismatch",
            "tree directory hash did not resolve to a directory node",
        )),
    }
}

fn expect_leaf(node: TreeNode) -> Result<LeafSegment, SemanticError> {
    match node {
        TreeNode::Leaf(node) => Ok(node),
        _ => Err(fault(
            "tree/leaf-kind-mismatch",
            "tree leaf hash did not resolve to a leaf segment",
        )),
    }
}

struct LeafBuilder {
    leaf: LeafSegment,
    encoded_len: usize,
}

impl LeafBuilder {
    fn new(order: IndexOrder, history: bool) -> Self {
        Self {
            leaf: LeafSegment {
                order,
                history,
                entities: Vec::new(),
                attributes: Vec::new(),
                values: Vec::new(),
                transactions: Vec::new(),
                assertions: Vec::new(),
            },
            // envelope + one column count
            encoded_len: EMPTY_NODE_LEN + 4,
        }
    }

    fn len(&self) -> usize {
        self.leaf.len()
    }

    fn is_empty(&self) -> bool {
        self.leaf.is_empty()
    }

    fn encoded_len_with(&self, value_len: usize) -> Result<usize, SemanticError> {
        // entity + attribute + value length/value + tx + assertion
        self.encoded_len
            .checked_add(8 + 4 + 4 + value_len + 8 + 1)
            .ok_or_else(|| fault("tree/length-overflow", "leaf encoded length overflow"))
    }

    fn push(&mut self, datom: Datom, value_len: usize) {
        self.encoded_len = self
            .encoded_len_with(value_len)
            .expect("hard size limit was checked");
        self.leaf.entities.push(datom.entity);
        self.leaf.attributes.push(datom.attribute);
        self.leaf.values.push(datom.value);
        self.leaf.transactions.push(datom.tx);
        self.leaf.assertions.push(datom.added);
    }

    fn take(&mut self) -> LeafSegment {
        let order = self.leaf.order;
        let history = self.leaf.history;
        std::mem::replace(self, Self::new(order, history)).leaf
    }
}

fn flush_leaf(
    builder: &mut LeafBuilder,
    nodes: &mut TreeNodeSet,
    stats: &mut TreeBuildStats,
    config: &TreeConfig,
) -> Result<ChildRef, SemanticError> {
    let leaf = builder.take();
    let first = leaf.first().expect("only non-empty leaves are flushed");
    let count = leaf.len() as u64;
    let bytes = encode_tree_node(&TreeNode::Leaf(leaf))?;
    if bytes.len() > config.max_leaf_bytes {
        return Err(SemanticError::incorrect(
            "tree/leaf-size-limit",
            "encoded leaf exceeded max_leaf_bytes",
        ));
    }
    stats.leaf_nodes = checked_add_u64(stats.leaf_nodes, 1, "leaf node count")?;
    stats.largest_leaf_bytes = stats.largest_leaf_bytes.max(bytes.len() as u64);
    let byte_count = bytes.len() as u64;
    let (hash, inserted) = nodes.insert(bytes)?;
    record_node(stats, byte_count, inserted)?;
    Ok(ChildRef {
        key: RoutingKey::exact(&first),
        hash,
        count,
    })
}

#[allow(clippy::too_many_arguments)]
fn push_leaf_reference(
    reference: ChildRef,
    pending: &mut Vec<ChildRef>,
    root: &mut Vec<ChildRef>,
    nodes: &mut TreeNodeSet,
    stats: &mut TreeBuildStats,
    config: &TreeConfig,
) -> Result<(), SemanticError> {
    let routed = route_after_leaf(pending.last(), &reference, nodes)?;
    let would_exceed_count = pending.len() == config.max_leaves_per_directory;
    let would_exceed_bytes =
        directory_encoded_len(pending, Some(&routed))? > config.max_directory_bytes;
    if !pending.is_empty() && (would_exceed_count || would_exceed_bytes) {
        let directory = flush_directory(
            pending,
            nodes,
            stats,
            reference_order(&reference, nodes)?,
            reference_history(&reference, nodes)?,
            config,
        )?;
        push_built_directory_reference(directory, root, nodes, config)?;
    }
    let routed = route_after_leaf(pending.last(), &reference, nodes)?;
    if directory_encoded_len(pending, Some(&routed))? > config.max_directory_bytes {
        return Err(SemanticError::incorrect(
            "tree/directory-size-limit",
            "one directory entry exceeds max_directory_bytes",
        ));
    }
    pending.push(routed);
    Ok(())
}

fn route_after_leaf(
    previous: Option<&ChildRef>,
    current: &ChildRef,
    nodes: &TreeNodeSet,
) -> Result<ChildRef, SemanticError> {
    let mut routed = current.clone();
    let Some(previous) = previous else {
        return Ok(routed);
    };
    let current_first = current.key.as_datom().ok_or_else(|| {
        fault(
            "tree/build-routing-key",
            "new leaf reference did not retain its exact first datom",
        )
    })?;
    let previous_leaf = expect_leaf(decode_tree_node(
        &previous.hash,
        resolve(nodes, &previous.hash)?,
    )?)?;
    let previous_last = previous_leaf.last().ok_or_else(|| {
        fault(
            "tree/empty-leaf",
            "new directory referenced an empty previous leaf",
        )
    })?;
    routed.key = sparse_routing_key(previous_leaf.order, &previous_last, &current_first);
    Ok(routed)
}

fn flush_directory(
    pending: &mut Vec<ChildRef>,
    nodes: &mut TreeNodeSet,
    stats: &mut TreeBuildStats,
    order: IndexOrder,
    history: bool,
    config: &TreeConfig,
) -> Result<ChildRef, SemanticError> {
    let leaves = std::mem::take(pending);
    let first = leaves
        .first()
        .expect("only non-empty directories are flushed")
        .key
        .clone();
    let count = sum_child_counts(&leaves)?;
    let bytes = encode_tree_node(&TreeNode::Directory(DirectoryNode {
        order,
        history,
        count,
        leaves,
    }))?;
    if bytes.len() > config.max_directory_bytes {
        return Err(SemanticError::incorrect(
            "tree/directory-size-limit",
            "encoded directory exceeded max_directory_bytes",
        ));
    }
    stats.directory_nodes = checked_add_u64(stats.directory_nodes, 1, "directory node count")?;
    stats.largest_directory_bytes = stats.largest_directory_bytes.max(bytes.len() as u64);
    let byte_count = bytes.len() as u64;
    let (hash, inserted) = nodes.insert(bytes)?;
    record_node(stats, byte_count, inserted)?;
    Ok(ChildRef {
        key: first,
        hash,
        count,
    })
}

fn push_directory_reference(
    reference: ChildRef,
    root: &mut Vec<ChildRef>,
    config: &TreeConfig,
) -> Result<(), SemanticError> {
    if root.len() == config.max_directories_per_root {
        return Err(SemanticError::incorrect(
            "tree/root-capacity",
            "fixed shallow tree exceeded max_directories_per_root",
        ));
    }
    if root_encoded_len(root, Some(&reference))? > config.max_root_bytes {
        return Err(SemanticError::incorrect(
            "tree/root-size-limit",
            "one more directory would exceed max_root_bytes",
        ));
    }
    root.push(reference);
    Ok(())
}

fn push_merge_directory_reference(
    child: MergeChild,
    root: &mut Vec<ChildRef>,
    order: IndexOrder,
    history: bool,
    config: &TreeConfig,
    context: &mut MergeContext<'_>,
) -> Result<(), SemanticError> {
    let reference = context.route_directory_reference(root.last(), &child, order, history)?;
    push_directory_reference(reference, root, config)
}

fn push_built_directory_reference(
    reference: ChildRef,
    root: &mut Vec<ChildRef>,
    nodes: &TreeNodeSet,
    config: &TreeConfig,
) -> Result<(), SemanticError> {
    let mut routed = reference;
    if let Some(previous) = root.last() {
        let current_first = routed.key.as_datom().ok_or_else(|| {
            fault(
                "tree/build-routing-key",
                "new directory reference did not retain its exact first datom",
            )
        })?;
        let previous_directory = expect_directory(decode_tree_node(
            &previous.hash,
            resolve(nodes, &previous.hash)?,
        )?)?;
        let previous_leaf_ref = previous_directory.leaves.last().ok_or_else(|| {
            fault(
                "tree/empty-directory",
                "new root referenced an empty previous directory",
            )
        })?;
        let previous_leaf = expect_leaf(decode_tree_node(
            &previous_leaf_ref.hash,
            resolve(nodes, &previous_leaf_ref.hash)?,
        )?)?;
        let previous_last = previous_leaf.last().ok_or_else(|| {
            fault(
                "tree/empty-leaf",
                "new root referenced an empty previous leaf",
            )
        })?;
        routed.key = sparse_routing_key(previous_directory.order, &previous_last, &current_first);
    }
    push_directory_reference(routed, root, config)
}

fn reference_order(reference: &ChildRef, nodes: &TreeNodeSet) -> Result<IndexOrder, SemanticError> {
    Ok(expect_leaf(decode_tree_node(
        &reference.hash,
        resolve(nodes, &reference.hash)?,
    )?)?
    .order)
}

fn reference_history(reference: &ChildRef, nodes: &TreeNodeSet) -> Result<bool, SemanticError> {
    Ok(expect_leaf(decode_tree_node(
        &reference.hash,
        resolve(nodes, &reference.hash)?,
    )?)?
    .history)
}

fn record_node(
    stats: &mut TreeBuildStats,
    bytes: u64,
    inserted: bool,
) -> Result<(), SemanticError> {
    if inserted {
        stats.unique_nodes = checked_add_u64(stats.unique_nodes, 1, "unique node count")?;
        stats.encoded_bytes = checked_add_u64(stats.encoded_bytes, bytes, "encoded byte count")?;
    }
    Ok(())
}

fn sum_child_counts(children: &[ChildRef]) -> Result<u64, SemanticError> {
    children.iter().try_fold(0, |sum, child| {
        checked_add_u64(sum, child.count, "tree child count")
    })
}

fn directory_encoded_len(
    existing: &[ChildRef],
    next: Option<&ChildRef>,
) -> Result<usize, SemanticError> {
    child_container_encoded_len(existing, next)
}

fn root_encoded_len(
    existing: &[ChildRef],
    next: Option<&ChildRef>,
) -> Result<usize, SemanticError> {
    child_container_encoded_len(existing, next)
}

fn child_container_encoded_len(
    existing: &[ChildRef],
    next: Option<&ChildRef>,
) -> Result<usize, SemanticError> {
    let mut length = EMPTY_NODE_LEN + 8 + 4;
    for reference in existing.iter().chain(next) {
        length = length
            .checked_add(child_reference_encoded_len(reference)?)
            .ok_or_else(|| fault("tree/length-overflow", "tree node length overflow"))?;
    }
    Ok(length)
}

fn child_reference_encoded_len(reference: &ChildRef) -> Result<usize, SemanticError> {
    let value_len = reference
        .key
        .value
        .as_ref()
        .map(encode_routing_value_bytes)
        .transpose()?
        .map_or(0, |value| 4 + value.len());
    // Datum-shaped key fields + value-presence/value + tx/assertion + explicit
    // component-presence mask + hash + count.
    Ok(8 + 4 + 1 + value_len + 8 + 1 + 1 + 32 + 8)
}

fn encode_tree_node(node: &TreeNode) -> Result<Vec<u8>, SemanticError> {
    validate_local_node(node)?;
    let mut body = Vec::new();
    let kind = match node {
        TreeNode::Leaf(leaf) => {
            put_len(&mut body, leaf.len())?;
            for value in &leaf.entities {
                put_u64(&mut body, *value);
            }
            for value in &leaf.attributes {
                put_u32(&mut body, *value);
            }
            for value in &leaf.values {
                put_bytes(&mut body, &encode_canonical_value(value)?)?;
            }
            for value in &leaf.transactions {
                put_u64(&mut body, *value);
            }
            for value in &leaf.assertions {
                body.push(u8::from(*value));
            }
            KIND_LEAF
        }
        TreeNode::Directory(directory) => {
            put_u64(&mut body, directory.count);
            encode_children(&mut body, &directory.leaves)?;
            KIND_DIRECTORY
        }
        TreeNode::Root(root) => {
            put_u64(&mut body, root.count);
            encode_children(&mut body, &root.directories)?;
            KIND_ROOT
        }
    };
    if body.len() > u32::MAX as usize {
        return Err(fault(
            "tree/node-size",
            "tree node body cannot be represented by the version-1 header",
        ));
    }
    let total = HEADER_LEN
        .checked_add(body.len())
        .and_then(|length| length.checked_add(CHECKSUM_LEN))
        .ok_or_else(|| fault("tree/length-overflow", "tree node length overflow"))?;
    if total > MAX_TREE_NODE_BYTES {
        return Err(fault(
            "tree/node-size",
            "canonical tree node exceeds the global node limit",
        ));
    }
    let mut encoded = Vec::with_capacity(total);
    encoded.extend_from_slice(TREE_MAGIC);
    encoded.extend_from_slice(&TREE_FORMAT_VERSION.to_be_bytes());
    encoded.push(kind);
    encoded.push(encode_order(node.order()));
    encoded.push(u8::from(node.history()));
    encoded.extend_from_slice(&[0, 0, 0]);
    encoded.extend_from_slice(&(body.len() as u32).to_be_bytes());
    encoded.extend_from_slice(&body);
    let checksum = sha256(&encoded);
    encoded.extend_from_slice(&checksum);
    Ok(encoded)
}

fn encode_children(output: &mut Vec<u8>, children: &[ChildRef]) -> Result<(), SemanticError> {
    put_len(output, children.len())?;
    for child in children {
        encode_routing_key(output, &child.key)?;
        output.extend_from_slice(&child.hash);
        put_u64(output, child.count);
    }
    Ok(())
}

fn decode_leaf(
    order: IndexOrder,
    history: bool,
    cursor: &mut TreeCursor<'_>,
) -> Result<LeafSegment, SemanticError> {
    let count = cursor.collection_len()?;
    let mut entities = Vec::with_capacity(count);
    for _ in 0..count {
        entities.push(cursor.u64()?);
    }
    let mut attributes = Vec::with_capacity(count);
    for _ in 0..count {
        attributes.push(cursor.u32()?);
    }
    let mut values = Vec::with_capacity(count);
    for _ in 0..count {
        values.push(decode_canonical_value(cursor.bytes()?)?);
    }
    let mut transactions = Vec::with_capacity(count);
    for _ in 0..count {
        transactions.push(cursor.u64()?);
    }
    let mut assertions = Vec::with_capacity(count);
    for _ in 0..count {
        assertions.push(cursor.boolean()?);
    }
    Ok(LeafSegment {
        order,
        history,
        entities,
        attributes,
        values,
        transactions,
        assertions,
    })
}

fn decode_directory(
    order: IndexOrder,
    history: bool,
    cursor: &mut TreeCursor<'_>,
) -> Result<DirectoryNode, SemanticError> {
    let count = cursor.u64()?;
    let leaves = decode_children(cursor)?;
    Ok(DirectoryNode {
        order,
        history,
        count,
        leaves,
    })
}

fn decode_root(
    order: IndexOrder,
    history: bool,
    cursor: &mut TreeCursor<'_>,
) -> Result<RootNode, SemanticError> {
    let count = cursor.u64()?;
    let directories = decode_children(cursor)?;
    Ok(RootNode {
        order,
        history,
        count,
        directories,
    })
}

fn decode_children(cursor: &mut TreeCursor<'_>) -> Result<Vec<ChildRef>, SemanticError> {
    let count = cursor.collection_len()?;
    let mut children = Vec::with_capacity(count);
    for _ in 0..count {
        children.push(ChildRef {
            key: decode_routing_key(cursor)?,
            hash: cursor.digest()?,
            count: cursor.u64()?,
        });
    }
    Ok(children)
}

fn encode_routing_key(output: &mut Vec<u8>, key: &RoutingKey) -> Result<(), SemanticError> {
    put_u64(output, key.entity);
    put_u32(output, key.attribute);
    match &key.value {
        None => output.push(0),
        Some(value) => {
            output.push(1);
            put_bytes(output, &encode_routing_value_bytes(value)?)?;
        }
    }
    put_u64(output, key.tx);
    output.push(u8::from(key.added));
    output.push(key.components);
    Ok(())
}

fn decode_routing_key(cursor: &mut TreeCursor<'_>) -> Result<RoutingKey, SemanticError> {
    let entity = cursor.u64()?;
    let attribute = cursor.u32()?;
    let value = match cursor.u8()? {
        0 => None,
        1 => Some(decode_routing_value_bytes(cursor.bytes()?)?),
        _ => {
            return Err(fault(
                "tree/invalid-routing-value-tag",
                "tree routing value presence must be zero or one",
            ));
        }
    };
    let tx = cursor.u64()?;
    let added = cursor.boolean()?;
    let components = cursor.u8()?;
    Ok(RoutingKey {
        entity,
        attribute,
        value,
        tx,
        added,
        components,
    })
}

fn encode_routing_value_bytes(value: &RoutingValue) -> Result<Vec<u8>, SemanticError> {
    let mut output = Vec::new();
    encode_routing_value(&mut output, value, 0)?;
    Ok(output)
}

fn encode_routing_value(
    output: &mut Vec<u8>,
    value: &RoutingValue,
    depth: usize,
) -> Result<(), SemanticError> {
    if depth > 16 {
        return Err(fault(
            "tree/routing-value-depth",
            "routing tuple nesting exceeds 16 levels",
        ));
    }
    match value {
        RoutingValue::Exact(value) => {
            output.push(0);
            put_bytes(output, &encode_canonical_value(value)?)?;
        }
        RoutingValue::String(units) => {
            output.push(1);
            put_utf16(output, units)?;
        }
        RoutingValue::Uri(units) => {
            output.push(2);
            put_utf16(output, units)?;
        }
        RoutingValue::Bytes { total_len, prefix } => {
            output.push(3);
            put_u32(output, *total_len);
            put_bytes(output, prefix)?;
        }
        RoutingValue::Keyword { namespace, name } => {
            output.push(4);
            put_optional_utf16(output, namespace.as_deref())?;
            put_utf16(output, name)?;
        }
        RoutingValue::Symbol { namespace, name } => {
            output.push(5);
            put_optional_utf16(output, namespace.as_deref())?;
            put_utf16(output, name)?;
        }
        RoutingValue::Tuple { total_len, prefix } => {
            output.push(6);
            put_u32(output, *total_len);
            put_len(output, prefix.len())?;
            for value in prefix {
                match value {
                    None => output.push(0),
                    Some(value) => {
                        output.push(1);
                        encode_routing_value(output, value, depth + 1)?;
                    }
                }
            }
        }
    }
    Ok(())
}

fn decode_routing_value_bytes(bytes: &[u8]) -> Result<RoutingValue, SemanticError> {
    let mut cursor = TreeCursor::new(bytes);
    let value = decode_routing_value(&mut cursor, 0)?;
    cursor.finish()?;
    Ok(value)
}

fn decode_routing_value(
    cursor: &mut TreeCursor<'_>,
    depth: usize,
) -> Result<RoutingValue, SemanticError> {
    if depth > 16 {
        return Err(fault(
            "tree/routing-value-depth",
            "routing tuple nesting exceeds 16 levels",
        ));
    }
    match cursor.u8()? {
        0 => {
            let value = decode_canonical_value(cursor.bytes()?)?;
            Ok(RoutingValue::Exact(value))
        }
        1 => Ok(RoutingValue::String(cursor.utf16()?)),
        2 => Ok(RoutingValue::Uri(cursor.utf16()?)),
        3 => Ok(RoutingValue::Bytes {
            total_len: cursor.u32()?,
            prefix: cursor.bytes()?.to_vec(),
        }),
        4 => Ok(RoutingValue::Keyword {
            namespace: cursor.optional_utf16()?,
            name: cursor.utf16()?,
        }),
        5 => Ok(RoutingValue::Symbol {
            namespace: cursor.optional_utf16()?,
            name: cursor.utf16()?,
        }),
        6 => {
            let total_len = cursor.u32()?;
            let count = cursor.collection_len()?;
            let mut prefix = Vec::with_capacity(count);
            for _ in 0..count {
                prefix.push(match cursor.u8()? {
                    0 => None,
                    1 => Some(decode_routing_value(cursor, depth + 1)?),
                    _ => {
                        return Err(fault(
                            "tree/invalid-routing-value-tag",
                            "routing tuple slot presence must be zero or one",
                        ));
                    }
                });
            }
            Ok(RoutingValue::Tuple { total_len, prefix })
        }
        _ => Err(fault(
            "tree/invalid-routing-value-tag",
            "tree routing value kind is unknown",
        )),
    }
}

fn validate_local_node(node: &TreeNode) -> Result<(), SemanticError> {
    match node {
        TreeNode::Leaf(leaf) => validate_leaf(leaf),
        TreeNode::Directory(directory) => {
            if directory.leaves.is_empty() || directory.count == 0 {
                return Err(fault(
                    "tree/empty-directory",
                    "directory nodes must reference at least one datom",
                ));
            }
            validate_children(directory.order, directory.history, &directory.leaves)?;
            if sum_child_counts(&directory.leaves)? != directory.count {
                return Err(fault(
                    "tree/directory-count-mismatch",
                    "directory count does not equal child reference counts",
                ));
            }
            Ok(())
        }
        TreeNode::Root(root) => {
            if root.directories.is_empty() {
                if root.count != 0 {
                    return Err(fault(
                        "tree/empty-root-count",
                        "an empty root must have count zero",
                    ));
                }
                return Ok(());
            }
            if root.count == 0 {
                return Err(fault(
                    "tree/nonempty-root-count",
                    "a non-empty root must have a positive count",
                ));
            }
            validate_children(root.order, root.history, &root.directories)?;
            if sum_child_counts(&root.directories)? != root.count {
                return Err(fault(
                    "tree/root-count-mismatch",
                    "root count does not equal child reference counts",
                ));
            }
            Ok(())
        }
    }
}

fn validate_leaf(leaf: &LeafSegment) -> Result<(), SemanticError> {
    let count = leaf.entities.len();
    if count == 0 {
        return Err(fault(
            "tree/empty-leaf",
            "leaf segments must contain at least one datom",
        ));
    }
    if count > MAX_NODE_ENTRIES
        || leaf.attributes.len() != count
        || leaf.values.len() != count
        || leaf.transactions.len() != count
        || leaf.assertions.len() != count
    {
        return Err(fault(
            "tree/column-count-mismatch",
            "leaf segment columns must have one value per datom",
        ));
    }
    if !leaf.history && leaf.assertions.iter().any(|added| !added) {
        return Err(fault(
            "tree/current-retraction",
            "a current index leaf cannot contain a retraction",
        ));
    }
    let datoms = leaf.datoms();
    if datoms
        .windows(2)
        .any(|pair| !pair[0].cmp_in(&pair[1], leaf.order).is_lt())
    {
        return Err(fault(
            "tree/noncanonical-leaf-order",
            "leaf datoms must be strictly ordered in the declared index order",
        ));
    }
    validate_persistent_index_datoms(leaf.order, &datoms)?;
    Ok(())
}

fn validate_children(
    order: IndexOrder,
    history: bool,
    children: &[ChildRef],
) -> Result<(), SemanticError> {
    if children.len() > MAX_NODE_ENTRIES {
        return Err(fault(
            "tree/child-count-limit",
            "tree node has too many child references",
        ));
    }
    for child in children {
        if child.count == 0 {
            return Err(fault(
                "tree/empty-child-reference",
                "tree child references must have positive counts",
            ));
        }
        if let Some(value) = &child.key.value {
            validate_routing_value(value, 0)?;
        }
    }
    if children
        .windows(2)
        .any(|pair| !pair[0].key.cmp_key(&pair[1].key, order).is_lt())
    {
        return Err(fault(
            "tree/noncanonical-routing-order",
            "tree routing lower bounds must be strictly ordered",
        ));
    }
    if !history && children.iter().any(|child| !child.key.added) {
        return Err(fault(
            "tree/current-retraction",
            "a current index routing key cannot be a retraction",
        ));
    }
    let first = children[0].key.as_datom().ok_or_else(|| {
        fault(
            "tree/noncanonical-first-routing-key",
            "the first child must retain its exact first datom",
        )
    })?;
    if first.tx == 0 {
        return Err(fault(
            "tree/noncanonical-first-routing-key",
            "the first child cannot use a sparse transaction placeholder",
        ));
    }
    validate_persistent_index_datoms(order, &[first])?;
    for child in children.iter().skip(1) {
        validate_routing_shape(&child.key, order)?;
    }
    Ok(())
}

fn validate_routing_shape(key: &RoutingKey, order: IndexOrder) -> Result<(), SemanticError> {
    if key.components & !ROUTING_KNOWN_COMPONENTS != 0
        || key.has(ROUTING_VALUE_PRESENT) != key.value.is_some()
        || (!key.has(ROUTING_ENTITY_PRESENT) && key.entity != 0)
        || (!key.has(ROUTING_ATTRIBUTE_PRESENT) && key.attribute != 0)
        || (!key.has(ROUTING_TX_PRESENT) && key.tx != 0)
    {
        return Err(fault(
            "tree/invalid-routing-key",
            "routing key payload and component-presence mask disagree",
        ));
    }
    if key.has(ROUTING_TX_PRESENT) {
        if !key.is_exact() {
            return Err(fault(
                "tree/invalid-routing-key",
                "a routing key with a transaction component must be exact",
            ));
        }
        let datom = key.as_datom().ok_or_else(|| {
            fault(
                "tree/invalid-routing-key",
                "an exact routing key must contain a value",
            )
        })?;
        return validate_persistent_index_datoms(order, &[datom]);
    }
    let valid = match order {
        IndexOrder::Eavt => {
            key.has(ROUTING_ENTITY_PRESENT)
                && (key.has(ROUTING_ATTRIBUTE_PRESENT) || !key.has(ROUTING_VALUE_PRESENT))
        }
        IndexOrder::Aevt => {
            key.has(ROUTING_ATTRIBUTE_PRESENT)
                && (key.has(ROUTING_ENTITY_PRESENT) || !key.has(ROUTING_VALUE_PRESENT))
        }
        IndexOrder::Avet => key.has(ROUTING_ATTRIBUTE_PRESENT) && !key.has(ROUTING_ENTITY_PRESENT),
        IndexOrder::Vaet => key.has(ROUTING_VALUE_PRESENT) && !key.has(ROUTING_ENTITY_PRESENT),
    };
    if !valid {
        return Err(fault(
            "tree/invalid-routing-key",
            "sparse routing key omits the leading component for its index order",
        ));
    }
    if let Some(value) = &key.value {
        validate_routing_value(value, 0)?;
    }
    Ok(())
}

fn validate_routing_value(value: &RoutingValue, depth: usize) -> Result<(), SemanticError> {
    if depth > 16 {
        return Err(fault(
            "tree/routing-value-depth",
            "routing tuple nesting exceeds 16 levels",
        ));
    }
    match value {
        RoutingValue::Exact(value) => {
            encode_canonical_value(value)?;
        }
        RoutingValue::String(units) | RoutingValue::Uri(units) => {
            if units.len().saturating_mul(2) > MAX_TREE_NODE_BYTES {
                return Err(fault(
                    "tree/routing-value-size",
                    "routing UTF-16 prefix exceeds the tree node limit",
                ));
            }
        }
        RoutingValue::Bytes { total_len, prefix } => {
            if *total_len as usize > 16 * 1024 * 1024 || prefix.len() > *total_len as usize {
                return Err(fault(
                    "tree/routing-value-size",
                    "routing byte prefix exceeds its value length or scalar limit",
                ));
            }
        }
        RoutingValue::Keyword { namespace, name } | RoutingValue::Symbol { namespace, name } => {
            let units = namespace.as_ref().map_or(0, Vec::len) + name.len();
            if units.saturating_mul(2) > MAX_TREE_NODE_BYTES {
                return Err(fault(
                    "tree/routing-value-size",
                    "routing named-value prefix exceeds the tree node limit",
                ));
            }
        }
        RoutingValue::Tuple { total_len, prefix } => {
            if *total_len as usize > MAX_NODE_ENTRIES || prefix.len() > *total_len as usize {
                return Err(fault(
                    "tree/routing-value-size",
                    "routing tuple prefix exceeds its logical tuple length",
                ));
            }
            for value in prefix.iter().flatten() {
                validate_routing_value(value, depth + 1)?;
            }
        }
    }
    Ok(())
}

fn same_datom(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
        && left.tx == right.tx
        && left.added == right.added
}

fn encode_order(order: IndexOrder) -> u8 {
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
            "tree/invalid-order",
            "tree node index order is unknown",
        )),
    }
}

fn decode_bool(tag: u8, name: &str) -> Result<bool, SemanticError> {
    match tag {
        0 => Ok(false),
        1 => Ok(true),
        _ => Err(fault(
            "tree/invalid-boolean",
            format!("tree node {name} is not a canonical boolean"),
        )),
    }
}

fn checked_add_u64(left: u64, right: u64, name: &str) -> Result<u64, SemanticError> {
    left.checked_add(right)
        .ok_or_else(|| fault("tree/count-overflow", format!("{name} overflow")))
}

fn short_hash(hash: &Digest) -> String {
    hash[..6].iter().map(|byte| format!("{byte:02x}")).collect()
}

fn put_len(output: &mut Vec<u8>, length: usize) -> Result<(), SemanticError> {
    if length > MAX_NODE_ENTRIES {
        return Err(fault(
            "tree/collection-limit",
            "tree node collection exceeds its canonical entry limit",
        ));
    }
    let length = u32::try_from(length)
        .map_err(|_| fault("tree/collection-limit", "tree node collection exceeds u32"))?;
    put_u32(output, length);
    Ok(())
}

fn put_bytes(output: &mut Vec<u8>, bytes: &[u8]) -> Result<(), SemanticError> {
    let length = u32::try_from(bytes.len())
        .map_err(|_| fault("tree/value-size", "tree scalar bytes exceed u32"))?;
    put_u32(output, length);
    output.extend_from_slice(bytes);
    Ok(())
}

fn put_utf16(output: &mut Vec<u8>, units: &[u16]) -> Result<(), SemanticError> {
    let length = u32::try_from(units.len())
        .map_err(|_| fault("tree/value-size", "routing UTF-16 prefix exceeds u32"))?;
    put_u32(output, length);
    for unit in units {
        put_u16(output, *unit);
    }
    Ok(())
}

fn put_optional_utf16(output: &mut Vec<u8>, units: Option<&[u16]>) -> Result<(), SemanticError> {
    match units {
        None => output.push(0),
        Some(units) => {
            output.push(1);
            put_utf16(output, units)?;
        }
    }
    Ok(())
}

fn put_u16(output: &mut Vec<u8>, value: u16) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn put_u32(output: &mut Vec<u8>, value: u32) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn put_u64(output: &mut Vec<u8>, value: u64) {
    output.extend_from_slice(&value.to_be_bytes());
}

struct TreeCursor<'a> {
    bytes: &'a [u8],
    position: usize,
}

impl<'a> TreeCursor<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, position: 0 }
    }

    fn finish(&self) -> Result<(), SemanticError> {
        if self.position == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "tree/trailing-bytes",
                "tree node contains trailing bytes",
            ))
        }
    }

    fn take(&mut self, length: usize) -> Result<&'a [u8], SemanticError> {
        let end = self
            .position
            .checked_add(length)
            .ok_or_else(|| fault("tree/length-overflow", "tree node length overflow"))?;
        let value = self.bytes.get(self.position..end).ok_or_else(|| {
            fault(
                "tree/truncated-node",
                "tree node ended before its declared content",
            )
        })?;
        self.position = end;
        Ok(value)
    }

    fn array<const N: usize>(&mut self) -> Result<[u8; N], SemanticError> {
        self.take(N)?
            .try_into()
            .map_err(|_| fault("tree/truncated-node", "tree node fixed field is truncated"))
    }

    fn u8(&mut self) -> Result<u8, SemanticError> {
        Ok(self.take(1)?[0])
    }

    fn u32(&mut self) -> Result<u32, SemanticError> {
        Ok(u32::from_be_bytes(self.array()?))
    }

    fn u16(&mut self) -> Result<u16, SemanticError> {
        Ok(u16::from_be_bytes(self.array()?))
    }

    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.array()?))
    }

    fn digest(&mut self) -> Result<Digest, SemanticError> {
        self.array()
    }

    fn collection_len(&mut self) -> Result<usize, SemanticError> {
        let count = self.u32()? as usize;
        if count > MAX_NODE_ENTRIES {
            return Err(fault(
                "tree/collection-limit",
                "tree node collection exceeds its canonical entry limit",
            ));
        }
        Ok(count)
    }

    fn bytes(&mut self) -> Result<&'a [u8], SemanticError> {
        let length = self.u32()? as usize;
        if length > MAX_TREE_NODE_BYTES {
            return Err(fault(
                "tree/value-size",
                "tree scalar bytes exceed the node limit",
            ));
        }
        self.take(length)
    }

    fn utf16(&mut self) -> Result<Vec<u16>, SemanticError> {
        let length = self.u32()? as usize;
        let byte_length = length
            .checked_mul(2)
            .ok_or_else(|| fault("tree/value-size", "routing UTF-16 length overflow"))?;
        if byte_length > MAX_TREE_NODE_BYTES {
            return Err(fault(
                "tree/value-size",
                "routing UTF-16 prefix exceeds the tree node limit",
            ));
        }
        let mut units = Vec::with_capacity(length);
        for _ in 0..length {
            units.push(self.u16()?);
        }
        Ok(units)
    }

    fn optional_utf16(&mut self) -> Result<Option<Vec<u16>>, SemanticError> {
        match self.u8()? {
            0 => Ok(None),
            1 => Ok(Some(self.utf16()?)),
            _ => Err(fault(
                "tree/invalid-routing-value-tag",
                "routing namespace presence must be zero or one",
            )),
        }
    }

    fn boolean(&mut self) -> Result<bool, SemanticError> {
        decode_bool(self.u8()?, "assertion flag")
    }
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{USER_PARTITION, canonical_genesis_datoms, make_eid, t_to_tx};

    fn datom(entity_index: u64, attribute: u32, value: i64, t: u64, added: bool) -> Datom {
        Datom {
            entity: make_eid(USER_PARTITION, entity_index).unwrap(),
            attribute,
            value: Value::Long(value),
            tx: t_to_tx(t).unwrap(),
            added,
        }
    }

    fn tiny_config() -> TreeConfig {
        TreeConfig {
            max_leaf_datoms: 3,
            target_leaf_bytes: 512,
            max_leaf_bytes: 2_048,
            max_leaves_per_directory: 2,
            max_directory_bytes: 2_048,
            max_directories_per_root: 8,
            max_root_bytes: 4_096,
        }
    }

    fn sorted_datoms(order: IndexOrder, history: bool) -> Vec<Datom> {
        let mut datoms = (1..=17)
            .map(|index| {
                datom(
                    (index % 7 + 1) as u64,
                    (index % 4 + 1) as u32,
                    (index % 6) as i64,
                    index as u64,
                    !history || index % 3 != 0,
                )
            })
            .collect::<Vec<_>>();
        datoms.sort_by(|left, right| left.cmp_in(right, order));
        datoms
    }

    #[test]
    fn all_four_orders_build_validate_seek_and_range() {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let datoms = sorted_datoms(order, true);
            let build = build_tree(order, true, datoms.clone(), &tiny_config()).unwrap();
            validate_tree(&build.descriptor, &build.nodes).unwrap();
            assert_eq!(build.descriptor.count, datoms.len() as u64);
            assert!(build.stats.leaf_nodes > 1);
            assert!(build.stats.directory_nodes > 1);
            assert!(build.stats.peak_live_datoms <= 3);

            for expected in &datoms {
                let found = seek_tree(&build.descriptor, &build.nodes, expected).unwrap();
                assert!(same_datom(found.datom.as_ref().unwrap(), expected));
                assert_eq!(found.stats.root_reads, 1);
                assert!(found.stats.directory_reads >= 1);
                assert!(found.stats.leaf_reads >= 1);
            }

            let expected = datoms[4..12].to_vec();
            let range = range_tree(
                &build.descriptor,
                &build.nodes,
                Some(&datoms[4]),
                Some(&datoms[12]),
            )
            .unwrap();
            assert_eq!(range.datoms, expected);
            assert!(range.stats.leaf_reads < build.stats.leaf_nodes);
        }
    }

    #[test]
    fn one_datom_leaves_distinguish_real_entity_zero_from_an_omitted_component() {
        let datoms = canonical_genesis_datoms();
        assert!(
            datoms.iter().filter(|datom| datom.entity == 0).count() > 1,
            "genesis must exercise multiple :db.part/db facts"
        );
        let config = TreeConfig {
            max_leaf_datoms: 1,
            target_leaf_bytes: 512,
            max_leaf_bytes: 2_048,
            max_leaves_per_directory: 16,
            max_directory_bytes: 64 * 1_024,
            max_directories_per_root: 64,
            max_root_bytes: 64 * 1_024,
        };
        let build = build_tree(IndexOrder::Eavt, true, datoms.clone(), &config).unwrap();
        validate_tree(&build.descriptor, &build.nodes).unwrap();
        assert_exact_reads(&build, &datoms);

        let root = expect_root(
            decode_tree_node(
                &build.descriptor.root_hash,
                build.nodes.get(&build.descriptor.root_hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        let first_directory = expect_directory(
            decode_tree_node(
                &root.directories[0].hash,
                build.nodes.get(&root.directories[0].hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        assert!(first_directory.leaves.iter().skip(1).any(|leaf| {
            leaf.key.entity == 0
                && leaf.key.has(ROUTING_ENTITY_PRESENT)
                && !leaf.key.has(ROUTING_TX_PRESENT)
        }));
    }

    #[test]
    fn sparse_vaet_omissions_sort_below_real_entity_zero_and_transaction() {
        let prior = Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: 18,
            value: Value::Ref(1_000),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        let current = Datom {
            entity: 0,
            attribute: 19,
            value: Value::Ref(1_000),
            tx: t_to_tx(2).unwrap(),
            added: true,
        };
        assert!(prior.cmp_in(&current, IndexOrder::Vaet).is_lt());

        let separator = sparse_routing_key(IndexOrder::Vaet, &prior, &current);
        assert!(!separator.has(ROUTING_ENTITY_PRESENT));
        assert!(!separator.has(ROUTING_TX_PRESENT));
        assert!(separator.cmp_datom(&prior, IndexOrder::Vaet).is_gt());
        assert!(separator.cmp_datom(&current, IndexOrder::Vaet).is_lt());
        assert!(
            separator
                .cmp_key(&RoutingKey::exact(&current), IndexOrder::Vaet)
                .is_lt()
        );
        assert!(
            separator
                .cmp_prefix(&IndexPrefix::Vaet {
                    value: Value::Ref(1_000),
                    attribute: Some(19),
                    entity: Some(0),
                })
                .is_lt()
        );
    }

    #[test]
    fn leaves_are_canonical_columnar_values_and_build_is_deterministic() {
        let datoms = sorted_datoms(IndexOrder::Eavt, true);
        let first = build_tree(IndexOrder::Eavt, true, datoms.clone(), &tiny_config()).unwrap();
        let second = build_tree(IndexOrder::Eavt, true, datoms, &tiny_config()).unwrap();
        assert_eq!(first.descriptor, second.descriptor);
        assert_eq!(first.nodes.nodes, second.nodes.nodes);

        let leaf = first
            .nodes
            .iter()
            .find_map(
                |(hash, bytes)| match decode_tree_node(hash, bytes).unwrap() {
                    TreeNode::Leaf(leaf) => Some(leaf),
                    _ => None,
                },
            )
            .unwrap();
        assert_eq!(leaf.entities.len(), leaf.attributes.len());
        assert_eq!(leaf.entities.len(), leaf.values.len());
        assert_eq!(leaf.entities.len(), leaf.transactions.len());
        assert_eq!(leaf.entities.len(), leaf.assertions.len());
    }

    #[test]
    fn decoded_node_weight_counts_recursive_value_capacity() {
        let slots = 32_768;
        let node = TreeNode::Leaf(LeafSegment {
            order: IndexOrder::Eavt,
            history: false,
            entities: vec![make_eid(USER_PARTITION, 1).unwrap()],
            attributes: vec![7],
            values: vec![Value::Tuple(vec![None; slots])],
            transactions: vec![t_to_tx(1).unwrap()],
            assertions: vec![true],
        });
        assert!(
            node.retained_bytes() >= (slots * size_of::<Option<Value>>()) as u64,
            "a compactly encoded tuple must still consume its decoded allocation budget"
        );
    }

    #[test]
    fn target_splits_early_but_hard_byte_and_count_bounds_hold() {
        let mut datoms = (1..=20)
            .map(|index| Datom {
                entity: make_eid(USER_PARTITION, index).unwrap(),
                attribute: 1,
                value: Value::String("x".repeat(120)),
                tx: t_to_tx(index).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let build = build_tree(IndexOrder::Eavt, false, datoms, &tiny_config()).unwrap();
        assert!(build.stats.largest_leaf_bytes <= tiny_config().max_leaf_bytes as u64);
        assert!(build.stats.peak_live_datoms <= tiny_config().max_leaf_datoms as u64);
        assert!(build.stats.leaf_nodes >= 7);
        for (hash, bytes) in build.nodes.iter() {
            match decode_tree_node(hash, bytes).unwrap() {
                TreeNode::Leaf(leaf) => {
                    assert!(leaf.len() <= tiny_config().max_leaf_datoms);
                    assert!(bytes.len() <= tiny_config().max_leaf_bytes);
                }
                TreeNode::Directory(directory) => {
                    assert!(directory.leaves.len() <= tiny_config().max_leaves_per_directory);
                    assert!(bytes.len() <= tiny_config().max_directory_bytes);
                }
                TreeNode::Root(root) => {
                    assert!(root.directories.len() <= tiny_config().max_directories_per_root);
                    assert!(bytes.len() <= tiny_config().max_root_bytes);
                }
            }
        }
    }

    fn large_key_config() -> TreeConfig {
        TreeConfig {
            max_leaf_datoms: 1,
            target_leaf_bytes: 400 * 1024,
            max_leaf_bytes: 512 * 1024,
            max_leaves_per_directory: 4,
            max_directory_bytes: 640 * 1024,
            max_directories_per_root: 16,
            max_root_bytes: 640 * 1024,
        }
    }

    fn assert_exact_reads(build: &TreeBuild, datoms: &[Datom]) {
        for (index, expected) in datoms.iter().enumerate() {
            let found = seek_tree(&build.descriptor, &build.nodes, expected)
                .unwrap()
                .datom
                .unwrap();
            assert!(same_datom(&found, expected));
            let end = datoms.get(index + 1);
            let range = range_tree(&build.descriptor, &build.nodes, Some(expected), end).unwrap();
            let expected_range = if end.is_some() {
                vec![expected.clone()]
            } else {
                datoms[index..].to_vec()
            };
            assert_eq!(range.datoms, expected_range);
        }
    }

    #[test]
    fn sparse_parent_keys_remove_false_large_string_limits_in_all_orders() {
        let tail = "z".repeat(256 * 1024);
        for order in [IndexOrder::Eavt, IndexOrder::Aevt, IndexOrder::Avet] {
            let mut datoms = (0..16)
                .map(|index| Datom {
                    entity: make_eid(USER_PARTITION, index + 1).unwrap(),
                    attribute: 7,
                    value: if order == IndexOrder::Avet {
                        Value::String(format!("{}{}", char::from(b'a' + index as u8), tail))
                    } else {
                        Value::String(tail.clone())
                    },
                    tx: t_to_tx(index + 1).unwrap(),
                    added: true,
                })
                .collect::<Vec<_>>();
            datoms.sort_by(|left, right| left.cmp_in(right, order));
            let build = build_tree(order, false, datoms.clone(), &large_key_config()).unwrap();
            validate_tree(&build.descriptor, &build.nodes).unwrap();
            assert_exact_reads(&build, &datoms);

            let root = expect_root(
                decode_tree_node(
                    &build.descriptor.root_hash,
                    build.nodes.get(&build.descriptor.root_hash).unwrap(),
                )
                .unwrap(),
            )
            .unwrap();
            assert_eq!(root.directories.len(), 4);
            assert!(
                root.directories
                    .iter()
                    .skip(1)
                    .all(|child| child_reference_encoded_len(child).unwrap() < 128)
            );
            for directory_ref in &root.directories {
                let directory = expect_directory(
                    decode_tree_node(
                        &directory_ref.hash,
                        build.nodes.get(&directory_ref.hash).unwrap(),
                    )
                    .unwrap(),
                )
                .unwrap();
                assert_eq!(directory.leaves.len(), 4);
                assert!(
                    directory
                        .leaves
                        .iter()
                        .skip(1)
                        .all(|child| child_reference_encoded_len(child).unwrap() < 128)
                );
            }
        }
    }

    fn assert_compact_avet_values(values: Vec<Value>) {
        let mut datoms = values
            .into_iter()
            .enumerate()
            .map(|(index, value)| Datom {
                entity: make_eid(USER_PARTITION, index as u64 + 1).unwrap(),
                attribute: 11,
                value,
                tx: t_to_tx(index as u64 + 1).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
        let build =
            build_tree(IndexOrder::Avet, false, datoms.clone(), &large_key_config()).unwrap();
        validate_tree(&build.descriptor, &build.nodes).unwrap();
        assert_exact_reads(&build, &datoms);
        let root = expect_root(
            decode_tree_node(
                &build.descriptor.root_hash,
                build.nodes.get(&build.descriptor.root_hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        assert!(root.directories.len() > 1);
        assert!(
            root.directories
                .iter()
                .skip(1)
                .all(|child| child_reference_encoded_len(child).unwrap() < 256)
        );
        for directory_ref in &root.directories {
            let directory = expect_directory(
                decode_tree_node(
                    &directory_ref.hash,
                    build.nodes.get(&directory_ref.hash).unwrap(),
                )
                .unwrap(),
            )
            .unwrap();
            assert!(
                directory
                    .leaves
                    .iter()
                    .skip(1)
                    .all(|child| child_reference_encoded_len(child).unwrap() < 256)
            );
        }
    }

    #[test]
    fn compact_native_separators_cover_bytes_uri_keyword_and_symbol() {
        let tail = "v".repeat(256 * 1024);
        assert_compact_avet_values(
            (0..8)
                .map(|index| {
                    let mut bytes = vec![b'v'; 256 * 1024];
                    bytes[0] = b'a' + index;
                    Value::Bytes(bytes)
                })
                .collect(),
        );
        assert_compact_avet_values(
            (0..8)
                .map(|index| Value::Uri(format!("{}{}", char::from(b'a' + index), tail)))
                .collect(),
        );
        assert_compact_avet_values(
            (0..8)
                .map(|index| {
                    Value::Keyword(crate::Keyword::new(
                        format!("{}{}", char::from(b'a' + index), tail),
                        "name",
                    ))
                })
                .collect(),
        );
        assert_compact_avet_values(
            (0..8)
                .map(|index| {
                    Value::Symbol(crate::Symbol::new(
                        "namespace",
                        format!("{}{}", char::from(b'a' + index), tail),
                    ))
                })
                .collect(),
        );
    }

    #[test]
    fn tuple_separators_keep_only_the_minimum_differing_prefix() {
        let tail = "q".repeat(256 * 1024);
        let mut datoms = (0..8)
            .map(|index| Datom {
                entity: make_eid(USER_PARTITION, index + 1).unwrap(),
                attribute: 9,
                value: Value::Tuple(vec![
                    Some(Value::String(format!(
                        "{}{}",
                        char::from(b'a' + index as u8),
                        tail
                    ))),
                    Some(Value::String(tail.clone())),
                ]),
                tx: t_to_tx(index + 1).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
        let mut config = large_key_config();
        config.target_leaf_bytes = 600 * 1024;
        config.max_leaf_bytes = 768 * 1024;
        config.max_directory_bytes = 1_200 * 1024;
        config.max_root_bytes = 1_200 * 1024;
        let build = build_tree(IndexOrder::Avet, false, datoms.clone(), &config).unwrap();
        validate_tree(&build.descriptor, &build.nodes).unwrap();
        assert_exact_reads(&build, &datoms);
        let root = expect_root(
            decode_tree_node(
                &build.descriptor.root_hash,
                build.nodes.get(&build.descriptor.root_hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        assert!(
            root.directories
                .iter()
                .skip(1)
                .all(|child| child_reference_encoded_len(child).unwrap() < 128)
        );
    }

    #[test]
    fn empty_tree_has_one_authenticated_root_and_no_reads_below_it() {
        let build = build_tree(
            IndexOrder::Avet,
            false,
            Vec::<Datom>::new(),
            &TreeConfig::default(),
        )
        .unwrap();
        assert_eq!(build.nodes.len(), 1);
        assert_eq!(build.descriptor.count, 0);
        assert_eq!(build.descriptor.first_hash, None);
        assert_eq!(build.descriptor.last_hash, None);
        let result = seek_tree(&build.descriptor, &build.nodes, &datom(1, 1, 1, 1, true)).unwrap();
        assert_eq!(result.datom, None);
        assert_eq!(result.stats.root_reads, 1);
        assert_eq!(result.stats.directory_reads, 0);
        assert_eq!(result.stats.leaf_reads, 0);
    }

    #[test]
    fn unsorted_duplicates_and_retractions_in_current_fail_closed() {
        let one = datom(1, 1, 1, 1, true);
        let two = datom(2, 1, 1, 2, true);
        let error = build_tree(
            IndexOrder::Eavt,
            false,
            vec![two.clone(), one.clone()],
            &TreeConfig::default(),
        )
        .unwrap_err();
        assert_eq!(error.code, "tree/noncanonical-input");
        let error = build_tree(
            IndexOrder::Eavt,
            false,
            vec![one.clone(), one],
            &TreeConfig::default(),
        )
        .unwrap_err();
        assert_eq!(error.code, "tree/noncanonical-input");
        let error = build_tree(
            IndexOrder::Eavt,
            false,
            vec![Datom {
                added: false,
                ..two
            }],
            &TreeConfig::default(),
        )
        .unwrap_err();
        assert_eq!(error.code, "tree/current-retraction");
    }

    #[test]
    fn corrupt_hash_version_kind_and_missing_child_are_rejected() {
        let build = build_tree(
            IndexOrder::Eavt,
            true,
            sorted_datoms(IndexOrder::Eavt, true),
            &tiny_config(),
        )
        .unwrap();
        let root_bytes = build.nodes.get(&build.descriptor.root_hash).unwrap();
        let mut corrupt = root_bytes.to_vec();
        corrupt[20] ^= 0x80;
        let error = decode_tree_node(&build.descriptor.root_hash, &corrupt).unwrap_err();
        assert_eq!(error.code, "tree/content-hash-mismatch");

        let mut unsupported = root_bytes.to_vec();
        unsupported[4..6].copy_from_slice(&99_u16.to_be_bytes());
        let unsupported_hash = sha256(&unsupported);
        let error = decode_tree_node(&unsupported_hash, &unsupported).unwrap_err();
        assert_eq!(error.code, "tree/unsupported-version");

        let root = expect_root(decode_tree_node(&build.descriptor.root_hash, root_bytes).unwrap())
            .unwrap();
        let leaf_hash = expect_directory(
            decode_tree_node(
                &root.directories[0].hash,
                build.nodes.get(&root.directories[0].hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap()
        .leaves[0]
            .hash;
        let mut missing = build.nodes.clone();
        missing.nodes.remove(&leaf_hash);
        let error = validate_tree(&build.descriptor, &missing).unwrap_err();
        assert_eq!(error.code, "tree/missing-node");

        let leaf_bytes = build.nodes.get(&leaf_hash).unwrap();
        let error =
            expect_directory(decode_tree_node(&leaf_hash, leaf_bytes).unwrap()).unwrap_err();
        assert_eq!(error.code, "tree/directory-kind-mismatch");
    }

    #[test]
    fn forged_counts_bounds_order_and_overlaps_are_rejected() {
        let config = tiny_config();
        let build = build_tree(
            IndexOrder::Eavt,
            true,
            sorted_datoms(IndexOrder::Eavt, true),
            &config,
        )
        .unwrap();
        let root = expect_root(
            decode_tree_node(
                &build.descriptor.root_hash,
                build.nodes.get(&build.descriptor.root_hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();

        let mut wrong_count = root.clone();
        wrong_count.directories[0].count += 1;
        wrong_count.count += 1;
        let mut count_nodes = build.nodes.clone();
        let count_bytes = encode_tree_node(&TreeNode::Root(wrong_count)).unwrap();
        let (count_hash, _) = count_nodes.insert(count_bytes).unwrap();
        let mut count_descriptor = build.descriptor.clone();
        count_descriptor.root_hash = count_hash;
        count_descriptor.count += 1;
        let error = validate_tree(&count_descriptor, &count_nodes).unwrap_err();
        assert_eq!(error.code, "tree/child-count-mismatch");

        let mut wrong_bound = root.clone();
        wrong_bound.directories[0].key.value = Some(RoutingValue::Exact(Value::Long(-999)));
        let mut bound_nodes = build.nodes.clone();
        let bound_bytes = encode_tree_node(&TreeNode::Root(wrong_bound)).unwrap();
        let (bound_hash, _) = bound_nodes.insert(bound_bytes).unwrap();
        let mut bound_descriptor = build.descriptor.clone();
        bound_descriptor.root_hash = bound_hash;
        bound_descriptor.first_hash = Some([99; 32]);
        let error = validate_tree(&bound_descriptor, &bound_nodes).unwrap_err();
        assert_eq!(error.code, "tree/noncanonical-routing-key");

        // An exact child-first key is a safe lower bound, but is still a
        // noncanonical and potentially enormous replacement for the recovered
        // minimum separator.
        let second_directory = expect_directory(
            decode_tree_node(
                &root.directories[1].hash,
                build.nodes.get(&root.directories[1].hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        let second_leaf = expect_leaf(
            decode_tree_node(
                &second_directory.leaves[0].hash,
                build.nodes.get(&second_directory.leaves[0].hash).unwrap(),
            )
            .unwrap(),
        )
        .unwrap();
        let mut verbose_separator = root.clone();
        verbose_separator.directories[1].key = RoutingKey::exact(&second_leaf.first().unwrap());
        let mut separator_nodes = build.nodes.clone();
        let separator_bytes = encode_tree_node(&TreeNode::Root(verbose_separator)).unwrap();
        let (separator_hash, _) = separator_nodes.insert(separator_bytes).unwrap();
        let mut separator_descriptor = build.descriptor.clone();
        separator_descriptor.root_hash = separator_hash;
        let error = validate_tree(&separator_descriptor, &separator_nodes).unwrap_err();
        assert_eq!(error.code, "tree/noncanonical-routing-key");

        let mut wrong_identity = root.clone();
        wrong_identity.order = IndexOrder::Aevt;
        let identity_bytes = encode_tree_node(&TreeNode::Root(wrong_identity)).unwrap_err();
        assert_eq!(identity_bytes.code, "tree/noncanonical-routing-order");

        // A canonical root that repeats a valid directory necessarily routes
        // overlapping ranges; exhaustive resolution must reject it.
        let mut overlap = root.clone();
        let duplicate = overlap.directories[0].clone();
        overlap.directories.insert(1, duplicate.clone());
        overlap.count += duplicate.count;
        let error = encode_tree_node(&TreeNode::Root(overlap)).unwrap_err();
        assert_eq!(error.code, "tree/noncanonical-routing-order");
    }

    #[test]
    fn root_capacity_is_an_explicit_fixed_depth_limit() {
        let mut config = tiny_config();
        config.max_leaf_datoms = 1;
        config.max_leaves_per_directory = 1;
        config.max_directories_per_root = 1;
        let error = build_tree(
            IndexOrder::Eavt,
            true,
            sorted_datoms(IndexOrder::Eavt, true),
            &config,
        )
        .unwrap_err();
        assert_eq!(error.code, "tree/root-capacity");
    }

    fn cow_config() -> TreeConfig {
        TreeConfig {
            max_leaf_datoms: 8,
            target_leaf_bytes: 4_096,
            max_leaf_bytes: 8_192,
            max_leaves_per_directory: 8,
            max_directory_bytes: 8_192,
            max_directories_per_root: 128,
            max_root_bytes: 32 * 1_024,
        }
    }

    fn overlay(old: &TreeNodeSet, new: &TreeNodeSet) -> TreeNodeSet {
        let mut nodes = old.nodes.clone();
        for (hash, bytes) in &new.nodes {
            if let Some(existing) = nodes.insert(*hash, bytes.clone()) {
                assert_eq!(existing, *bytes);
            }
        }
        TreeNodeSet { nodes }
    }

    #[test]
    fn localized_current_replacement_rewrites_one_path_and_reuses_the_rest() {
        let config = cow_config();
        let base_datoms = (1..=4_096)
            .map(|index| datom(index, 1, index as i64, index, true))
            .collect::<Vec<_>>();
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let mut datoms = base_datoms.clone();
            datoms.sort_by(|left, right| left.cmp_in(right, order));
            let old = build_tree(order, false, datoms.clone(), &config).unwrap();
            let removed = base_datoms[2_051].clone();
            let inserted = Datom {
                tx: t_to_tx(5_000).unwrap(),
                ..removed.clone()
            };
            let edits = TreeMergeEdits {
                removals: vec![removed.clone()],
                insertions: vec![inserted.clone()],
                ..TreeMergeEdits::default()
            };

            let first = merge_tree(&old.descriptor, &old.nodes, &edits, &config).unwrap();
            let second = merge_tree(&old.descriptor, &old.nodes, &edits, &config).unwrap();
            assert_eq!(first.descriptor, second.descriptor);
            assert_eq!(first.new_nodes.nodes, second.new_nodes.nodes);
            assert_eq!(first.stats, second.stats);

            let mut expected = datoms;
            expected.retain(|datom| !same_datom(datom, &removed));
            expected.push(inserted);
            expected.sort_by(|left, right| left.cmp_in(right, order));
            let complete = overlay(&old.nodes, &first.new_nodes);
            validate_tree(&first.descriptor, &complete).unwrap();
            let actual = range_tree(&first.descriptor, &complete, None, None)
                .unwrap()
                .datoms;
            assert_eq!(actual, expected);

            // With unchanged packing boundaries, the COW and full-build roots
            // are identical. Sparse separator repair authenticates only the
            // changed path and its boundary neighbors at both tree levels.
            let rebuilt = build_tree(order, false, expected, &config).unwrap();
            assert_eq!(first.descriptor, rebuilt.descriptor);
            assert_eq!(first.stats.root_reads, 1);
            assert_eq!(first.stats.directory_reads, 3);
            assert_eq!(first.stats.leaf_reads, 5);
            assert_eq!(first.stats.affected_directories, 1);
            assert_eq!(first.stats.affected_leaves, 1);
            assert_eq!(first.stats.affected_ranges, 1);
            assert_eq!(first.stats.nodes_written, 3);
            assert_eq!(first.stats.reused_leaf_refs, 7);
            assert_eq!(first.stats.reused_directory_refs, 63);
            assert!(first.stats.nodes_written * 100 < old.stats.unique_nodes);
        }
    }

    #[test]
    fn history_appends_and_filters_only_the_explicit_no_history_pair() {
        let order = IndexOrder::Eavt;
        let config = cow_config();
        let mut durable = (1..=128)
            .map(|index| datom(index, 1, index as i64, index, true))
            .collect::<Vec<_>>();
        let forgotten_assertion = datom(50, 2, 7, 10, true);
        let retained_assertion = datom(51, 2, 8, 11, true);
        durable.extend([forgotten_assertion.clone(), retained_assertion.clone()]);
        durable.sort_by(|left, right| left.cmp_in(right, order));
        let old = build_tree(order, true, durable.clone(), &config).unwrap();

        let forgotten_retraction = Datom {
            tx: t_to_tx(200).unwrap(),
            added: false,
            ..forgotten_assertion.clone()
        };
        let retained_retraction = Datom {
            tx: t_to_tx(201).unwrap(),
            added: false,
            ..retained_assertion.clone()
        };
        let mut insertions = vec![forgotten_retraction.clone(), retained_retraction.clone()];
        insertions.sort_by(|left, right| left.cmp_in(right, order));
        let edits = TreeMergeEdits {
            insertions,
            no_history_pairs: vec![NoHistoryPair {
                retraction: forgotten_retraction.clone(),
                assertion: forgotten_assertion.clone(),
            }],
            ..TreeMergeEdits::default()
        };
        let merged = merge_tree(&old.descriptor, &old.nodes, &edits, &config).unwrap();
        let complete = overlay(&old.nodes, &merged.new_nodes);
        validate_tree(&merged.descriptor, &complete).unwrap();
        let actual = range_tree(&merged.descriptor, &complete, None, None)
            .unwrap()
            .datoms;

        let mut expected = durable;
        expected.push(forgotten_retraction.clone());
        expected.push(retained_retraction.clone());
        expected.retain(|candidate| {
            !same_datom(candidate, &forgotten_retraction)
                && !same_datom(candidate, &forgotten_assertion)
        });
        expected.sort_by(|left, right| left.cmp_in(right, order));
        assert_eq!(actual, expected);
        assert!(
            actual
                .iter()
                .any(|datom| same_datom(datom, &retained_assertion))
        );
        assert!(
            actual
                .iter()
                .any(|datom| same_datom(datom, &retained_retraction))
        );
        assert_eq!(merged.stats.insertions, 2);
        assert_eq!(merged.stats.no_history_pairs, 1);

        let rebuilt = build_tree(order, true, expected, &config).unwrap();
        assert_eq!(merged.descriptor.count, rebuilt.descriptor.count);

        let arbitrary_removal = TreeMergeEdits {
            removals: vec![retained_assertion],
            ..TreeMergeEdits::default()
        };
        let error =
            merge_tree(&old.descriptor, &old.nodes, &arbitrary_removal, &config).unwrap_err();
        assert_eq!(error.code, "tree/history-removal");
    }

    #[test]
    fn copy_on_write_split_respects_the_fixed_root_capacity() {
        let config = TreeConfig {
            max_leaf_datoms: 2,
            target_leaf_bytes: 2_048,
            max_leaf_bytes: 4_096,
            max_leaves_per_directory: 2,
            max_directory_bytes: 4_096,
            max_directories_per_root: 1,
            max_root_bytes: 4_096,
        };
        let old_datoms = vec![
            datom(10, 1, 10, 1, true),
            datom(20, 1, 20, 2, true),
            datom(30, 1, 30, 3, true),
            datom(40, 1, 40, 4, true),
        ];
        let old = build_tree(IndexOrder::Eavt, false, old_datoms, &config).unwrap();
        let edits = TreeMergeEdits {
            insertions: vec![datom(35, 1, 35, 5, true), datom(37, 1, 37, 6, true)],
            ..TreeMergeEdits::default()
        };
        let error = merge_tree(&old.descriptor, &old.nodes, &edits, &config).unwrap_err();
        assert_eq!(error.code, "tree/root-capacity");
    }

    #[test]
    fn explicit_affected_range_reconsiders_only_its_leaf_without_new_content() {
        let config = cow_config();
        let datoms = (1..=256)
            .map(|index| datom(index, 1, index as i64, index, true))
            .collect::<Vec<_>>();
        let old = build_tree(IndexOrder::Eavt, false, datoms.clone(), &config).unwrap();
        let edits = TreeMergeEdits {
            affected_ranges: vec![TreeAffectedRange::new(
                Some(datoms[100].clone()),
                Some(datoms[101].clone()),
            )],
            ..TreeMergeEdits::default()
        };
        let merged = merge_tree(&old.descriptor, &old.nodes, &edits, &config).unwrap();
        assert_eq!(merged.descriptor, old.descriptor);
        assert!(merged.new_nodes.is_empty());
        assert_eq!(merged.stats.affected_ranges, 1);
        assert_eq!(merged.stats.affected_directories, 1);
        assert_eq!(merged.stats.affected_leaves, 1);
        assert_eq!(merged.stats.reused_hashes, 3);
    }
}
