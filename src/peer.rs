use crate::database_value::{LogicalReadObserver, TransactionReadContext};
use crate::idents::IdentIndex;
use crate::index::{IndexComponents, NormalizedIndexBoundary};
use crate::operations::tree_manifest_advisory_key;
use crate::persistent_tree::{
    ChildRef, DirectoryNode, LeafSegment, RootNode, TreeBuildStats, TreeConfig, TreeMergeEdits,
    TreeNode, TreeNodeSet, TreeRangeResult, TreeReadStats, TreeSeekResult, build_tree,
    decode_tree_node, discover_merge_no_history_pairs, merge_tree_with_boundary_loader,
};
use crate::postgres::{
    is_postgres_connection_error, postgres_error, read_authenticated_log_range, recover_to,
    verify_schema_compatibility, visit_authenticated_log_range,
};
use crate::recent::{
    EndpointProjection, RecentCursor, RecentCursorStats, RecentLimits, RecentRange, RecentTier,
    retained_entry_stats,
};
use crate::state_commitment::{checkpoint_information, verify_checkpoint_state_hash};
use crate::{
    AvetProjectionWork, Database, DatabaseIdentity, DatabaseValue, Datom, Digest,
    DurableTransaction, Entity, EntityIdentifier, ErrorCategory, IndexBoundary, IndexManifest,
    IndexOrder, IndexPrefix, IndexSegment, ManifestTree, PersistentTreeManifest,
    PostgresConnectionConfig, PostgresTreeStore, PullPattern, Query, QueryControl, QueryExtensions,
    QueryInput, QueryOutcome, QueryValue, SemanticError, ServiceTransactionReport,
    TreeManifestRecord, TreePublicationDelta, TreePublishOutcome, TreeRootBinding, View,
    decode_index_manifest, decode_index_segment, encode_genesis, sha256, tx_to_t,
};
#[cfg(test)]
use crate::{SegmentRef, encode_index_manifest, encode_index_segment};
use postgres::{Client, GenericClient};
#[cfg(test)]
#[path = "peer_merge_preload_tests.rs"]
mod merge_preload_tests;
use std::collections::{BTreeMap, BTreeSet, VecDeque};
use std::ops::Deref;
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::{Arc, Condvar, Mutex, MutexGuard, OnceLock, RwLock};
use std::time::{Duration, Instant};

#[path = "native_log.rs"]
pub(crate) mod native_log;

const DEFAULT_SEGMENT_DATOMS: usize = 4_096;
const MANIFEST_SELECTION_PAGE_SIZE: i64 = 32;
const MAX_MANIFEST_CANDIDATE_PROBES: u64 = 256;
const AVET_PROJECTION_CHUNK_DATOMS: usize = 512;
const AVET_SORT_FAN_IN: u64 = 4;
const AVET_SORT_PAGE_DATOMS: usize = 8;
type LoadedBase = (Database, Digest, u64);
type BaseSelection = (Option<LoadedBase>, u64);

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum IndexBuildFault {
    None,
    AfterSegments,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum IndexBuildScope {
    Administrative,
    /// Finish this finite demand, including its physical maintenance, without
    /// turning transactions that arrive during that maintenance into demand.
    Background {
        through: u64,
    },
}

impl IndexBuildScope {
    fn is_background(self) -> bool {
        matches!(self, Self::Background { .. })
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IndexBuildReceipt {
    pub publication_revision: u64,
    pub basis_t: u64,
    pub manifest_hash: Digest,
    /// Candidates actually fetched and authenticated newest-first. A healthy
    /// newest publication makes this one regardless of retained revisions.
    pub manifest_candidates_examined: u64,
    /// Examined candidates rejected as corrupt, incomplete, or unauthoritative.
    pub manifest_candidates_rejected: u64,
    /// Selection exhausted its operational corruption-probe budget. An
    /// administrative build may still recover and publish a replacement.
    pub manifest_probe_limit_reached: bool,
    /// Attributes whose AVET projection remains authenticated but incomplete
    /// in this fully live publication.
    pub pending_avet_projections: usize,
    /// This root is durable progress, but its bounded live-set fold and/or a
    /// same-basis projection successor still requires another worker step.
    pub index_work_remaining: bool,
    /// Compatibility name for the number of unique immutable tree nodes.
    pub segment_count: usize,
    pub reused: bool,
    pub input_datoms: u64,
    pub node_writes: u64,
    pub node_reuses: u64,
    /// Authenticated old nodes fetched while deriving this candidate.
    pub node_reads: u64,
    pub node_read_bytes: u64,
    /// Untouched directory/leaf references copied into successor roots.
    pub reused_subtrees: u64,
    /// Authoritative log datoms between the prior tree basis and this basis.
    pub tail_datoms: u64,
    pub encoded_bytes: u64,
    pub max_depth: u8,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct CacheStats {
    pub hits: u64,
    pub misses: u64,
    pub evictions: u64,
    pub current_entries: usize,
    pub current_bytes: usize,
    pub peak_entries: usize,
    pub peak_bytes: usize,
    pub oversized_bypasses: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct RecoveryStats {
    pub base_t: u64,
    pub target_t: u64,
    pub tail_transactions: u64,
    /// PostgreSQL transaction-range streams used to authenticate the writer
    /// tail. This is zero for a covered endpoint and one for any non-empty
    /// tail, independent of transaction count.
    pub tail_range_reads: u64,
    pub rejected_manifests: u64,
}

/// Process-local I/O and compatibility work performed by one shared peer.
///
/// Native open/adoption reads the manifest, its eight root nodes, and the
/// narrow authenticated paths needed to reconstruct resident schema/ident
/// caches. Directory/leaf counts expose that bounded bootstrap work and later
/// lazy reads, while compatibility materializations make use of the old full
/// `Database` oracle visible rather than silently folding it into startup.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct PeerLoadStats {
    pub manifest_candidates: u64,
    pub root_reads: u64,
    pub directory_reads: u64,
    pub leaf_reads: u64,
    /// Completed or dropped native cursors. This is the exact source-range
    /// count; memo replays do not open another cursor.
    pub cursor_ranges: u64,
    pub cursor_cache_hits: u64,
    pub cursor_cache_misses: u64,
    pub cursor_root_reads: u64,
    pub cursor_directory_reads: u64,
    pub cursor_leaf_reads: u64,
    pub cursor_sql_reads: u64,
    /// Canonical durable-node payload bytes fetched from PostgreSQL by those
    /// cursors. Cache hits correctly contribute zero bytes.
    pub cursor_sql_read_bytes: u64,
    pub cursor_recent_datoms_examined: u64,
    pub cursor_recent_datoms_yielded: u64,
    pub compatibility_materializations: u64,
    pub compatibility_hits: u64,
    pub compatibility_failures: u64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ResidentMetadataStats {
    pub(crate) schema_attributes: usize,
    pub(crate) schema_information_datoms: usize,
    pub(crate) schema_estimated_bytes: u64,
    pub(crate) ident_names: usize,
    pub(crate) ident_entities: usize,
    pub(crate) ident_estimated_bytes: u64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ResidentTreeRootStats {
    pub(crate) children: usize,
    pub(crate) estimated_bytes: u64,
}

/// Complete logical coordinate of one immutable value in one authoritative
/// log generation. Unlike a live head, this can name a historical source
/// value while an administrative rewrite or idempotent retry is in progress.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct ExactEndpoint {
    pub(crate) generation: u64,
    pub(crate) basis_t: u64,
    pub(crate) tx_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) eidx_frontier: u64,
}

impl ExactEndpoint {
    fn validate(self) -> Result<Self, SemanticError> {
        crate::t_to_tx(self.basis_t)?;
        crate::identity::validate_frontier(self.eidx_frontier)?;
        sql_basis(self.generation)?;
        if self.state_hash == [0; 32] {
            return Err(SemanticError::incorrect(
                "peer/exact-endpoint-zero-state",
                "an exact native endpoint requires a nonzero semantic state commitment",
            ));
        }
        Ok(self)
    }
}

/// Auditable work performed while choosing one authenticated native base.
/// Counts describe only actual newest-to-oldest authentication work; opening
/// a healthy newest publication does not count retained publication history.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ExactOpenStats {
    pub(crate) examined_candidates: u64,
    pub(crate) rejected_candidates: u64,
    pub(crate) selected_publication_revision: u64,
    pub(crate) selected_manifest_hash: Digest,
    pub(crate) tail_transactions: u64,
    pub(crate) tail_range_reads: u64,
}

#[derive(Debug, Default)]
struct PeerLoadCounters {
    manifest_candidates: AtomicU64,
    root_reads: AtomicU64,
    directory_reads: AtomicU64,
    leaf_reads: AtomicU64,
    cursor_ranges: AtomicU64,
    cursor_cache_hits: AtomicU64,
    cursor_cache_misses: AtomicU64,
    cursor_root_reads: AtomicU64,
    cursor_directory_reads: AtomicU64,
    cursor_leaf_reads: AtomicU64,
    cursor_sql_reads: AtomicU64,
    cursor_sql_read_bytes: AtomicU64,
    cursor_recent_datoms_examined: AtomicU64,
    cursor_recent_datoms_yielded: AtomicU64,
    compatibility_materializations: AtomicU64,
    compatibility_hits: AtomicU64,
    compatibility_failures: AtomicU64,
}

impl PeerLoadCounters {
    fn snapshot(&self) -> PeerLoadStats {
        PeerLoadStats {
            manifest_candidates: self.manifest_candidates.load(Ordering::Relaxed),
            root_reads: self.root_reads.load(Ordering::Relaxed),
            directory_reads: self.directory_reads.load(Ordering::Relaxed),
            leaf_reads: self.leaf_reads.load(Ordering::Relaxed),
            cursor_ranges: self.cursor_ranges.load(Ordering::Relaxed),
            cursor_cache_hits: self.cursor_cache_hits.load(Ordering::Relaxed),
            cursor_cache_misses: self.cursor_cache_misses.load(Ordering::Relaxed),
            cursor_root_reads: self.cursor_root_reads.load(Ordering::Relaxed),
            cursor_directory_reads: self.cursor_directory_reads.load(Ordering::Relaxed),
            cursor_leaf_reads: self.cursor_leaf_reads.load(Ordering::Relaxed),
            cursor_sql_reads: self.cursor_sql_reads.load(Ordering::Relaxed),
            cursor_sql_read_bytes: self.cursor_sql_read_bytes.load(Ordering::Relaxed),
            cursor_recent_datoms_examined: self
                .cursor_recent_datoms_examined
                .load(Ordering::Relaxed),
            cursor_recent_datoms_yielded: self.cursor_recent_datoms_yielded.load(Ordering::Relaxed),
            compatibility_materializations: self
                .compatibility_materializations
                .load(Ordering::Relaxed),
            compatibility_hits: self.compatibility_hits.load(Ordering::Relaxed),
            compatibility_failures: self.compatibility_failures.load(Ordering::Relaxed),
        }
    }

    fn record_cursor(&self, stats: PeerCursorStats) {
        self.cursor_ranges.fetch_add(1, Ordering::Relaxed);
        self.cursor_cache_hits
            .fetch_add(stats.tree.cache_hits, Ordering::Relaxed);
        self.cursor_cache_misses
            .fetch_add(stats.tree.cache_misses, Ordering::Relaxed);
        self.cursor_root_reads
            .fetch_add(stats.tree.root_reads, Ordering::Relaxed);
        self.cursor_directory_reads
            .fetch_add(stats.tree.directory_reads, Ordering::Relaxed);
        self.cursor_leaf_reads
            .fetch_add(stats.tree.leaf_reads, Ordering::Relaxed);
        self.cursor_sql_reads.fetch_add(
            stats
                .tree
                .root_reads
                .saturating_add(stats.tree.directory_reads)
                .saturating_add(stats.tree.leaf_reads),
            Ordering::Relaxed,
        );
        self.cursor_sql_read_bytes
            .fetch_add(stats.tree.decoded_bytes, Ordering::Relaxed);
        self.cursor_recent_datoms_examined
            .fetch_add(stats.recent.datoms_examined, Ordering::Relaxed);
        self.cursor_recent_datoms_yielded
            .fetch_add(stats.recent.datoms_yielded, Ordering::Relaxed);
    }
}

#[derive(Debug)]
struct SegmentCache {
    capacity: usize,
    entries: BTreeMap<Digest, Arc<IndexSegment>>,
    recency: VecDeque<Digest>,
    stats: CacheStats,
}

impl SegmentCache {
    fn new(capacity: usize) -> Self {
        Self {
            capacity,
            entries: BTreeMap::new(),
            recency: VecDeque::new(),
            stats: CacheStats::default(),
        }
    }

    fn get(&mut self, hash: &Digest) -> Option<Arc<IndexSegment>> {
        let value = self.entries.get(hash).cloned();
        if value.is_some() {
            self.stats.hits += 1;
            self.recency.retain(|candidate| candidate != hash);
            self.recency.push_back(*hash);
        } else {
            self.stats.misses += 1;
        }
        value
    }

    fn insert(&mut self, hash: Digest, segment: Arc<IndexSegment>) {
        if self.capacity == 0 {
            return;
        }
        self.entries.insert(hash, segment);
        self.recency.retain(|candidate| *candidate != hash);
        self.recency.push_back(hash);
        while self.entries.len() > self.capacity {
            if let Some(oldest) = self.recency.pop_front() {
                self.entries.remove(&oldest);
                self.stats.evictions += 1;
            }
        }
        self.stats.current_entries = self.entries.len();
        self.stats.peak_entries = self.stats.peak_entries.max(self.entries.len());
    }
}

#[derive(Debug)]
struct CachedTreeNode {
    node: Arc<TreeNode>,
    bytes: usize,
}

#[derive(Debug)]
struct TreeNodeCache {
    max_entries: usize,
    max_bytes: usize,
    entries: BTreeMap<Digest, CachedTreeNode>,
    recency: VecDeque<Digest>,
    stats: CacheStats,
}

/// Typed views keep the cache-owned `Arc<TreeNode>` alive without cloning a
/// directory's routing table or a leaf's value columns. Cache eviction only
/// drops the cache's reference; an active seek/cursor remains valid.
#[derive(Clone, Debug)]
struct LoadedDirectory(Arc<TreeNode>);

impl Deref for LoadedDirectory {
    type Target = DirectoryNode;

    fn deref(&self) -> &Self::Target {
        match self.0.as_ref() {
            TreeNode::Directory(directory) => directory,
            _ => unreachable!("LoadedDirectory is constructed only from a directory node"),
        }
    }
}

#[derive(Clone, Debug)]
struct LoadedLeaf(Arc<TreeNode>);

impl Deref for LoadedLeaf {
    type Target = LeafSegment;

    fn deref(&self) -> &Self::Target {
        match self.0.as_ref() {
            TreeNode::Leaf(leaf) => leaf,
            _ => unreachable!("LoadedLeaf is constructed only from a leaf node"),
        }
    }
}

impl TreeNodeCache {
    fn new(max_entries: usize, max_bytes: usize) -> Self {
        Self {
            max_entries,
            max_bytes,
            entries: BTreeMap::new(),
            recency: VecDeque::new(),
            stats: CacheStats::default(),
        }
    }

    fn get(&mut self, hash: &Digest) -> Option<Arc<TreeNode>> {
        let node = self.entries.get(hash).map(|entry| Arc::clone(&entry.node));
        if node.is_some() {
            self.stats.hits = self.stats.hits.saturating_add(1);
            self.recency.retain(|candidate| candidate != hash);
            self.recency.push_back(*hash);
        } else {
            self.stats.misses = self.stats.misses.saturating_add(1);
        }
        node
    }

    fn insert(&mut self, hash: Digest, node: Arc<TreeNode>, bytes: usize) {
        if self.max_entries == 0 || self.max_bytes == 0 || bytes > self.max_bytes {
            self.stats.oversized_bypasses = self.stats.oversized_bypasses.saturating_add(1);
            return;
        }
        if let Some(old) = self.entries.remove(&hash) {
            self.stats.current_bytes = self.stats.current_bytes.saturating_sub(old.bytes);
        }
        self.entries.insert(hash, CachedTreeNode { node, bytes });
        self.stats.current_bytes = self.stats.current_bytes.saturating_add(bytes);
        self.recency.retain(|candidate| candidate != &hash);
        self.recency.push_back(hash);
        while self.entries.len() > self.max_entries || self.stats.current_bytes > self.max_bytes {
            let Some(oldest) = self.recency.pop_front() else {
                break;
            };
            if let Some(old) = self.entries.remove(&oldest) {
                self.stats.current_bytes = self.stats.current_bytes.saturating_sub(old.bytes);
                self.stats.evictions = self.stats.evictions.saturating_add(1);
            }
        }
        self.stats.current_entries = self.entries.len();
        self.stats.peak_entries = self.stats.peak_entries.max(self.entries.len());
        self.stats.peak_bytes = self.stats.peak_bytes.max(self.stats.current_bytes);
    }
}

#[derive(Clone)]
struct TreeBase {
    manifest: PersistentTreeManifest,
    manifest_hash: Digest,
    roots: BTreeMap<(bool, u8), Arc<RootNode>>,
    root_residency: ResidentTreeRootStats,
    metadata: Arc<MetadataProjection>,
}

impl TreeBase {
    fn new(
        manifest: PersistentTreeManifest,
        manifest_hash: Digest,
        roots: BTreeMap<(bool, u8), Arc<RootNode>>,
        metadata: Arc<MetadataProjection>,
    ) -> Self {
        let root_residency =
            roots
                .values()
                .fold(ResidentTreeRootStats::default(), |mut total, root| {
                    total.children = total.children.saturating_add(root.directories.len());
                    total.estimated_bytes = total
                        .estimated_bytes
                        .saturating_add(root.estimated_retained_bytes());
                    total
                });
        Self {
            manifest,
            manifest_hash,
            roots,
            root_residency,
            metadata,
        }
    }
}

/// Small discardable projection needed to classify recent AVET/VAET datoms.
/// Both inputs remain ordinary datoms authenticated by the native manifest;
/// `Schema` and `IdentIndex` are always re-derived and are never authorities.
#[derive(Clone)]
struct MetadataProjection {
    schema_current: Arc<[Datom]>,
    idents: Arc<IdentIndex>,
    schema: Arc<crate::Schema>,
}

impl MetadataProjection {
    fn from_information(
        mut schema_current: Vec<Datom>,
        ident_assertions: Vec<Datom>,
    ) -> Result<Self, SemanticError> {
        sort_dedup_datoms(&mut schema_current, IndexOrder::Eavt);
        let idents = IdentIndex::derive(ident_assertions.iter(), crate::DB_IDENT as u32)?;
        Self::from_current_and_idents(schema_current, idents)
    }

    fn from_current_and_idents(
        mut schema_current: Vec<Datom>,
        idents: IdentIndex,
    ) -> Result<Self, SemanticError> {
        sort_dedup_datoms(&mut schema_current, IndexOrder::Eavt);
        retain_schema_working_set(&mut schema_current);
        let schema = crate::Schema::derive_from_information(&schema_current, &idents)?;
        Ok(Self {
            schema_current: schema_current.into(),
            idents: Arc::new(idents),
            schema: Arc::new(schema),
        })
    }

    fn from_database(database: &Database) -> Result<Self, SemanticError> {
        let schema_current = database
            .datoms(View::Current, IndexOrder::Eavt)
            .into_iter()
            .filter(|datom| schema_information_attribute(datom.attribute))
            .collect();
        let ident_assertions = database
            .datoms(View::History, IndexOrder::Aevt)
            .into_iter()
            .filter(|datom| datom.added && datom.attribute == crate::DB_IDENT as u32)
            .collect();
        Self::from_information(schema_current, ident_assertions)
    }

    fn apply(&self, transactions: &[DurableTransaction]) -> Result<Self, SemanticError> {
        if !transactions.iter().any(|transaction| {
            transaction
                .tx_data
                .iter()
                .any(|datom| schema_information_attribute(datom.attribute))
        }) {
            return Ok(self.clone());
        }

        // Schema/ident edits are rare. Only those edits copy the small
        // authenticated working set and derive a replacement projection;
        // ordinary transaction successors retain all three resident Arcs.
        let mut current = self.schema_current.to_vec();
        let mut idents = (*self.idents).clone();
        for transaction in transactions {
            let mut ident_updates = transaction
                .tx_data
                .iter()
                .filter(|datom| datom.added && datom.attribute == crate::DB_IDENT as u32)
                .collect::<Vec<_>>();
            ident_updates.sort_by(|left, right| ident_assertion_cmp(left, right));
            for datom in ident_updates {
                idents.apply_assertion(datom, crate::DB_IDENT as u32)?;
            }
            for datom in &transaction.tx_data {
                if !schema_information_attribute(datom.attribute) {
                    continue;
                }
                if datom.added {
                    if !current.iter().any(|fact| same_eav(fact, datom)) {
                        current.push(datom.clone());
                    }
                } else {
                    current.retain(|fact| !same_eav(fact, datom));
                }
            }
        }
        Self::from_current_and_idents(current, idents)
    }

    fn endpoint(&self) -> EndpointProjection {
        EndpointProjection::from_schema(Arc::clone(&self.schema))
    }

    fn resident_stats(&self) -> ResidentMetadataStats {
        let schema_information_bytes = self.schema_current.iter().fold(0_u64, |bytes, datom| {
            bytes.saturating_add(datom.retained_bytes())
        });
        ResidentMetadataStats {
            schema_attributes: self.schema.attributes().count(),
            schema_information_datoms: self.schema_current.len(),
            schema_estimated_bytes: self
                .schema
                .estimated_retained_bytes()
                .saturating_add(schema_information_bytes),
            ident_names: self.idents.name_count(),
            ident_entities: self.idents.entity_count(),
            ident_estimated_bytes: self.idents.estimated_retained_bytes(),
        }
    }
}

/// Fold the recovered `storageHasAVET`/`needsAVET` state alongside ordinary
/// authenticated schema information.  A logical false->true AVET transition
/// needs a background backfill exactly when the attribute had values *before*
/// that transaction. Values asserted by the enabling transaction itself are
/// already classified into the recent AVET tier and need no durable backfill.
///
/// `has_base_history` is deliberately a one-datom AEVT probe.  It is invoked
/// only for newly enabled attributes and memoized across the tail, preserving
/// the recovered `has-values?` decision without scanning an attribute range.
fn apply_metadata_and_avet_readiness<F>(
    base: &MetadataProjection,
    initial_unready: &BTreeSet<u32>,
    transactions: &[DurableTransaction],
    mut has_base_history: F,
) -> Result<(MetadataProjection, BTreeSet<u32>), SemanticError>
where
    F: FnMut(u32) -> Result<bool, SemanticError>,
{
    let mut metadata = base.clone();
    let mut unready = initial_unready.clone();
    let mut base_history = BTreeMap::<u32, bool>::new();
    let mut prior_tail_history = BTreeSet::<u32>::new();

    for transaction in transactions {
        let endpoint = metadata.apply(std::slice::from_ref(transaction))?;
        // MetadataProjection::apply retains all three Arcs for ordinary data
        // transactions. Pointer identity is therefore a proof that no schema
        // transition exists; avoid walking every resident attribute merely to
        // rediscover that fact on every commit.
        let changed_avet = if Arc::ptr_eq(&metadata.schema, &endpoint.schema) {
            Vec::new()
        } else {
            changed_avet_attributes(&metadata.schema, &endpoint.schema)
        };
        for (attribute, before, after) in changed_avet {
            if !after {
                // Dropping AVET also drops any pending backfill. This matters
                // to an unqualified AVET scan, which must not be poisoned by
                // a no-longer-indexed attribute.
                unready.remove(&attribute);
            } else if !before {
                let had_history = if prior_tail_history.contains(&attribute) {
                    true
                } else if let Some(had_history) = base_history.get(&attribute) {
                    *had_history
                } else {
                    let had_history = has_base_history(attribute)?;
                    base_history.insert(attribute, had_history);
                    had_history
                };
                if had_history {
                    unready.insert(attribute);
                } else {
                    // Empty attributes can toggle physical AVET membership
                    // synchronously, matching recovered add-avet/add-unique.
                    unready.remove(&attribute);
                }
            }
        }
        prior_tail_history.extend(transaction.tx_data.iter().map(|datom| datom.attribute));
        metadata = endpoint;
    }

    Ok((metadata, unready))
}

fn manifest_avet_unready(manifest: &PersistentTreeManifest) -> BTreeSet<u32> {
    manifest
        .pending_avet
        .iter()
        // A removal's stale physical rows are excluded by RecentTier's
        // endpoint-schema membership filter on both scan and prefix paths.
        // storageHasAVET readiness gates additions only; unrelated AVET
        // attributes remain available while a dropped range is reclaimed.
        .filter(|work| work.adding)
        .map(|work| work.attribute)
        .collect()
}

/// Bind authenticated physical AVET work to the schema reconstructed from the
/// same immutable tree value. Shape-valid manifest bytes alone cannot prove
/// the direction: additions require endpoint AVET membership, while removals
/// require its absence. All work must name a real installed attribute.
pub(crate) fn validate_avet_work_directions(
    pending_avet: &[AvetProjectionWork],
    schema: &crate::Schema,
) -> Result<(), SemanticError> {
    for work in pending_avet {
        let attribute = schema.attribute(work.attribute).map_err(|_| {
            fault(
                "tree/pending-avet-schema-mismatch",
                format!(
                    "pending AVET work names missing attribute {}",
                    work.attribute
                ),
            )
        })?;
        let endpoint_has_avet = attribute.indexed || attribute.unique.is_some();
        if endpoint_has_avet != work.adding {
            return Err(fault(
                "tree/pending-avet-schema-mismatch",
                format!(
                    "pending AVET direction for attribute {} disagrees with endpoint schema",
                    work.attribute
                ),
            ));
        }
    }
    Ok(())
}

/// Keep only the current install/alter hooks and facts belonging to the
/// attribute entities they name. `:db/ident` remains globally available from
/// `IdentIndex`; retaining every ident-bearing entity here would turn this
/// small derived schema cache back into a duplicate database projection.
fn retain_schema_working_set(current: &mut Vec<Datom>) {
    let installed = current
        .iter()
        .filter_map(|datom| {
            if datom.added
                && datom.entity == crate::DB_PART_DB
                && matches!(
                    u64::from(datom.attribute),
                    crate::DB_INSTALL_ATTRIBUTE | crate::DB_ALTER_ATTRIBUTE
                )
            {
                match &datom.value {
                    crate::Value::Ref(entity) => Some(*entity),
                    _ => None,
                }
            } else {
                None
            }
        })
        .collect::<BTreeSet<_>>();
    current.retain(|datom| {
        (datom.entity == crate::DB_PART_DB
            && matches!(
                u64::from(datom.attribute),
                crate::DB_INSTALL_ATTRIBUTE | crate::DB_ALTER_ATTRIBUTE
            ))
            || (installed.contains(&datom.entity) && schema_information_attribute(datom.attribute))
    });
}

fn schema_information_attribute(attribute: u32) -> bool {
    matches!(
        u64::from(attribute),
        crate::DB_IDENT
            | crate::DB_INSTALL_ATTRIBUTE
            | crate::DB_ALTER_ATTRIBUTE
            | crate::DB_VALUE_TYPE
            | crate::DB_CARDINALITY
            | crate::DB_UNIQUE
            | crate::DB_IS_COMPONENT
            | crate::DB_INDEX
            | crate::DB_NO_HISTORY
            | crate::DB_TUPLE_TYPE
            | crate::DB_TUPLE_TYPES
            | crate::DB_TUPLE_ATTRS
            | crate::DB_TUPLE_DISCONTINUED
            | crate::DB_ATTR_PREDS
    )
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

fn same_logical_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.index_cmp(&right.value) == std::cmp::Ordering::Equal
}

fn ident_assertion_cmp(left: &Datom, right: &Datom) -> std::cmp::Ordering {
    left.tx
        .cmp(&right.tx)
        .then(left.entity.cmp(&right.entity))
        .then_with(|| left.value.stored_cmp(&right.value))
        .then(left.added.cmp(&right.added))
}

/// Deterministic foreground entry point for Goal 4's background consolidation
/// job. Scheduling is deliberately left to the caller.
pub struct PostgresIndexer {
    client: Client,
    tree_store: PostgresTreeStore,
    source_pin_client: Option<Client>,
    connection: PostgresConnectionConfig,
    database_id: String,
    segment_datoms: usize,
    tree_config: TreeConfig,
}

impl PostgresIndexer {
    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured(
            &PostgresConnectionConfig::plaintext(connection),
            database_id,
        )
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
    ) -> Result<Self, SemanticError> {
        let mut client = connection.connect_for("index/connect")?;
        verify_schema_compatibility(&mut client)?;
        let tree_store = PostgresTreeStore::connect_configured(connection)?;
        Ok(Self {
            client,
            tree_store,
            source_pin_client: None,
            connection: connection.clone(),
            database_id: database_id.into(),
            segment_datoms: DEFAULT_SEGMENT_DATOMS,
            tree_config: TreeConfig::default(),
        })
    }

    pub fn with_segment_datoms(mut self, segment_datoms: usize) -> Result<Self, SemanticError> {
        if segment_datoms == 0 {
            return Err(SemanticError::incorrect(
                "index/zero-segment-size",
                "segment size must be positive",
            ));
        }
        self.segment_datoms = segment_datoms;
        self.tree_config.max_leaf_datoms = segment_datoms;
        Ok(self)
    }

    pub fn with_tree_config(mut self, config: TreeConfig) -> Result<Self, SemanticError> {
        // `build_tree` is the single validator. An empty canonical build is a
        // cheap way to reject an impossible physical configuration here.
        let _ = build_tree(IndexOrder::Eavt, false, Vec::new(), &config)?;
        self.segment_datoms = config.max_leaf_datoms;
        self.tree_config = config;
        Ok(self)
    }

    /// Select the local work disk used by recovered-style external AVET
    /// merge runs. Files are unnamed and disposable; durable state remains in
    /// PostgreSQL and an interrupted sort regenerates from its pinned AEVT
    /// source. `ATOMIC_INDEX_WORK_DIRECTORY` supplies the process default.
    pub fn with_index_work_directory(
        mut self,
        directory: impl AsRef<std::path::Path>,
    ) -> Result<Self, SemanticError> {
        self.tree_store = self
            .tree_store
            .with_index_work_directory(directory.as_ref())?;
        Ok(self)
    }

    pub fn consolidate(&mut self) -> Result<IndexBuildReceipt, SemanticError> {
        let mut retried_connection = false;
        let mut retried_publication_race = false;
        loop {
            match self.consolidate_with_fault(IndexBuildFault::None) {
                Err(error) if is_postgres_connection_error(&error) && !retried_connection => {
                    // Reselect the entire immutable build after reconnect;
                    // never resume a half-observed SQL transaction.
                    self.reconnect()?;
                    retried_connection = true;
                }
                Err(error)
                    if matches!(
                        error.code,
                        "tree/publication-cas-lost" | "tree/publication-revision-conflict"
                    ) && !retried_publication_race =>
                {
                    // A physical-root race is expected background-index
                    // behavior. Reselect once so an identical winner becomes
                    // an idempotent/no-op receipt and a different winner is
                    // the next immutable base.
                    retried_publication_race = true;
                }
                result => return result,
            }
        }
    }

    /// Create the first native publication only while the authoritative value
    /// is still the fixed database bootstrap/application-schema value.
    ///
    /// Ordinary transactor startup uses this narrow exception so a newly
    /// created database is immediately usable without a separate operator
    /// step. Once user history exists, a missing native publication is an
    /// administrative condition: silently calling [`Self::consolidate`] there
    /// would make activation perform an unbounded `recover_to` over the entire
    /// log and defeat the native root-plus-tail recovery contract.
    pub(crate) fn consolidate_fresh_database(
        &mut self,
    ) -> Result<Option<IndexBuildReceipt>, SemanticError> {
        let (basis_t, _) = read_head(&mut self.client, &self.database_id)?;
        if basis_t > 1 {
            return Ok(None);
        }
        self.consolidate().map(Some)
    }

    pub fn reconnect(&mut self) -> Result<(), SemanticError> {
        let mut client = self.connection.connect_for("index/reconnect")?;
        verify_schema_compatibility(&mut client)?;
        self.tree_store.reconnect()?;
        self.client = client;
        self.source_pin_client = None;
        Ok(())
    }

    pub fn consolidate_with_fault(
        &mut self,
        fault_point: IndexBuildFault,
    ) -> Result<IndexBuildReceipt, SemanticError> {
        let mut next_fault = fault_point;
        loop {
            if let Some(receipt) =
                self.consolidate_once(next_fault, IndexBuildScope::Administrative)?
            {
                return Ok(receipt);
            }
            // A successful partial projection is already an immutable restart
            // point. Fault injection applies to the caller's first physical
            // attempt only; later chunks use the ordinary path.
            next_fault = IndexBuildFault::None;
        }
    }

    /// Advance one bounded automatic-indexing step. `None` means one durable
    /// live-set fold batch was advanced and the caller should reselect before
    /// doing more work. Unlike explicit administrative consolidation, this
    /// entry point never reconstructs the full database from the log.
    pub(crate) fn consolidate_background_once(
        &mut self,
        through: u64,
    ) -> Result<Option<IndexBuildReceipt>, SemanticError> {
        self.consolidate_once(
            IndexBuildFault::None,
            IndexBuildScope::Background { through },
        )
    }

    fn consolidate_once(
        &mut self,
        fault_point: IndexBuildFault,
        scope: IndexBuildScope,
    ) -> Result<Option<IndexBuildReceipt>, SemanticError> {
        self.tree_store.reset_stats();
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("index/build-begin", error))?;
        let (basis_t, tx_hash) = read_head(&mut transaction, &self.database_id)?;
        let excision_generation = read_excision_generation(&mut transaction, &self.database_id)?;
        if basis_t == 0 && excision_generation == 0 {
            return Err(SemanticError::incorrect(
                "index/empty-database",
                "legacy generation zero has no authenticated genesis tree coordinate",
            ));
        }
        let stored_state = read_state_hash(
            &mut transaction,
            &self.database_id,
            excision_generation,
            basis_t,
        )?;
        let selection = load_latest_native_manifest(
            &mut transaction,
            &mut self.tree_store,
            &self.database_id,
            basis_t,
            excision_generation,
        )?;
        if selection
            .usable
            .as_ref()
            .is_some_and(|(previous, _, _, _)| {
                previous.publication_revision == selection.newest_observed_revision
                    && selection.newest_live_complete
                    && previous.pending_avet.is_empty()
            })
        {
            self.tree_store.discard_completed_avet_sort();
        }
        if scope.is_background() && !selection.newest_live_complete {
            let manifest_hash = selection.newest_observed_manifest_hash.ok_or_else(|| {
                background_rebuild_required(
                    "automatic indexing has no native publication to extend",
                )
            })?;
            if !selection.newest_live_work_pending {
                return Err(background_rebuild_required(
                    "the newest native publication has no resumable live-set work",
                ));
            }
            drop(transaction);
            self.tree_store.advance_publication_work(manifest_hash)?;
            return Ok(None);
        }
        if let Some((previous, previous_hash, _, _)) = selection.usable.as_ref()
            && previous.publication_revision == selection.newest_observed_revision
            && selection.newest_live_complete
            && previous.pending_avet.is_empty()
            && (matches!(scope, IndexBuildScope::Background { through } if previous.basis_t >= through)
                || (previous.basis_t == basis_t
                    && previous.tx_hash == tx_hash
                    && previous.state_hash == stored_state))
        {
            // The selector authenticated this publication independently of
            // the current head. Once finite background demand is covered,
            // newer ordinary novelty belongs to the scheduler's next byte
            // threshold, not to this publication's live-set completion.
            let manifest_hash = *previous_hash;
            let stats = self.tree_store.stats();
            return Ok(Some(IndexBuildReceipt {
                publication_revision: previous.publication_revision,
                basis_t: previous.basis_t,
                manifest_hash,
                manifest_candidates_examined: selection.manifest_candidates_examined,
                manifest_candidates_rejected: selection.manifest_candidates_rejected,
                manifest_probe_limit_reached: selection.manifest_probe_limit_reached,
                pending_avet_projections: 0,
                index_work_remaining: false,
                segment_count: 0,
                reused: true,
                input_datoms: 0,
                node_writes: 0,
                node_reuses: 0,
                node_reads: stats.node_rows_read,
                node_read_bytes: stats.node_read_bytes,
                reused_subtrees: 0,
                tail_datoms: 0,
                encoded_bytes: 0,
                max_depth: 3,
            }));
        }
        let expected_publication_revision = selection.newest_observed_revision;
        let publication_revision =
            expected_publication_revision
                .checked_add(1)
                .ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Unsupported,
                        "tree/publication-revision-exhausted",
                        "persistent tree publication revision is exhausted",
                    )
                })?;

        let advancing_pending = selection
            .usable
            .as_ref()
            .is_some_and(|(previous, _, _, _)| {
                selection.newest_live_complete
                    && previous.publication_revision == selection.newest_observed_revision
                    && !previous.pending_avet.is_empty()
            });
        let can_increment = selection
            .usable
            .as_ref()
            .is_some_and(|(previous, _, _, _)| {
                selection.newest_live_complete
                    && previous.pending_avet.is_empty()
                    && (scope.is_background()
                        || previous.publication_revision == selection.newest_observed_revision)
            });
        // Selection may decode/cache source nodes before a potentially long
        // external AVET sort. Acquire the same shared manifest coordinate
        // used by immutable peers, then revalidate after the lock closes the
        // select-versus-GC race. Keeping this connection alive through CAS
        // publication prevents zero-age GC from retiring the source paths.
        let source_manifest_pin = if advancing_pending || can_increment {
            let (previous, manifest_hash, _, _) = selection
                .usable
                .as_ref()
                .expect("source pin eligibility requires a usable predecessor");
            let pin_client = match self.source_pin_client.take() {
                Some(client) => client,
                None => {
                    let mut client = self.connection.connect_for("index/source-manifest-pin")?;
                    verify_schema_compatibility(&mut client)?;
                    client
                }
            };
            Some(acquire_index_source_manifest_pin(
                pin_client,
                &self.database_id,
                previous.publication_revision,
                *manifest_hash,
            )?)
        } else {
            None
        };
        let fallback_predecessor = if can_increment
            && scope.is_background()
            && selection
                .usable
                .as_ref()
                .is_some_and(|(previous, _, _, _)| {
                    previous.publication_revision != selection.newest_observed_revision
                }) {
            if selection.newest_observed_generation != Some(excision_generation) {
                return Err(background_rebuild_required(
                    "the newest native publication belongs to a different log generation",
                ));
            }
            let newest_hash = selection
                .newest_observed_manifest_hash
                .expect("a positive newest revision has a manifest hash");
            let newest_roots = load_relational_root_hashes(&mut transaction, newest_hash)?;
            let previous_roots = manifest_root_hashes(
                &selection
                    .usable
                    .as_ref()
                    .expect("incremental eligibility requires a usable predecessor")
                    .0,
            );
            if newest_roots != previous_roots {
                return Err(background_rebuild_required(
                    "the corrupt newest publication does not share the authenticated base roots",
                ));
            }
            Some(newest_hash)
        } else {
            None
        };
        let predecessor_index_basis_t = selection
            .usable
            .as_ref()
            .map(|(previous, _, _, _)| previous.index_basis_t);
        let mut publish_basis_t = basis_t;
        let mut publish_tx_hash = tx_hash;
        let mut publish_state_hash = stored_state;
        let mut build = if advancing_pending {
            let (previous, previous_hash, base_projection, old_cache) = selection
                .usable
                .expect("pending eligibility requires a usable newest predecessor");
            publish_basis_t = previous.basis_t;
            publish_tx_hash = previous.tx_hash;
            publish_state_hash = previous.state_hash;
            drop(transaction);
            build_avet_projection_step(
                &mut self.tree_store,
                &previous,
                previous_hash,
                &base_projection,
                &self.tree_config,
                old_cache,
            )?
        } else if can_increment {
            let (previous, previous_hash, base_projection, old_cache) = selection
                .usable
                .expect("incremental eligibility requires a usable predecessor");
            let authenticated_tail = load_authenticated_index_tail(
                &mut transaction,
                &self.database_id,
                excision_generation,
                previous.basis_t,
                previous.tx_hash,
                basis_t,
                tx_hash,
            )?;
            let tail = authenticated_tail
                .iter()
                .map(|(_, transaction)| transaction.clone())
                .collect::<Vec<_>>();
            let tail_hashes = authenticated_tail
                .iter()
                .map(|(hash, _)| *hash)
                .collect::<Vec<_>>();
            let endpoint_projection = base_projection.apply(&tail)?;
            let eidx_frontier = tail.last().map_or(previous.eidx_frontier, |transaction| {
                transaction.eidx_frontier
            });
            drop(transaction);
            build_incremental_native(
                &mut self.tree_store,
                &previous,
                previous_hash,
                &tail,
                &tail_hashes,
                &base_projection,
                endpoint_projection,
                eidx_frontier,
                &self.tree_config,
                old_cache,
            )?
        } else if scope.is_background() {
            return Err(if selection.manifest_probe_limit_reached {
                background_rebuild_required(
                    "automatic indexing reached the bounded corrupt-manifest probe limit; run explicit administrative consolidation",
                )
                .detail(
                    "examined_candidates",
                    selection.manifest_candidates_examined.to_string(),
                )
                .detail(
                    "maximum_candidate_probes",
                    MAX_MANIFEST_CANDIDATE_PROBES.to_string(),
                )
            } else {
                background_rebuild_required(
                    "automatic indexing found no authenticated incremental base",
                )
            });
        } else {
            // The first native root is the one intentional full pass. Legacy
            // flat manifests are a read-only migration fallback; they are not
            // written by this indexer.
            let recovered = recover_to(&mut transaction, &self.database_id, basis_t, tx_hash)?;
            if recovered.final_hash != tx_hash
                || !verify_checkpoint_state_hash(&recovered.database, stored_state)?.matches()
            {
                return Err(fault(
                    "tree/state-commitment-mismatch",
                    "initial tree input does not match the authoritative committed state",
                ));
            }
            let database = recovered.database;
            drop(transaction);
            build_initial_native(&database, &self.tree_config)?
        };
        if let Some(predecessor_manifest_hash) = fallback_predecessor {
            let TreePublicationDelta::Incremental {
                predecessor_manifest_hash: claimed_predecessor,
                ..
            } = &mut build.publication_delta
            else {
                unreachable!("incremental repair produced a replacement delta");
            };
            *claimed_predecessor = predecessor_manifest_hash;
        }
        let completed_avet_sort = build.completed_avet_sort;
        let index_basis_t = if build.pending_avet.is_empty() {
            // Recovered `complete-indexing` advances indexBasisT only when
            // the completed root is accepted. The final same-basis AVET step
            // is therefore genuine physical progress.
            publish_basis_t
        } else {
            predecessor_index_basis_t.ok_or_else(|| {
                fault(
                    "tree/missing-index-basis-predecessor",
                    "pending AVET work requires an authenticated predecessor index basis",
                )
            })?
        };

        let tree_manifest = PersistentTreeManifest {
            database_id: self.database_id.clone(),
            publication_revision,
            index_basis_t,
            basis_t: publish_basis_t,
            tx_hash: publish_tx_hash,
            state_hash: publish_state_hash,
            excision_generation,
            eidx_frontier: build.eidx_frontier,
            trees: build.trees,
            pending_avet: build.pending_avet,
        };
        let projection_complete = tree_manifest.pending_avet.is_empty();
        let tree_manifest_payload = tree_manifest.encode()?;
        let tree_manifest_hash = sha256(&tree_manifest_payload);
        let tree_roots = tree_manifest
            .trees
            .iter()
            .map(|tree| TreeRootBinding {
                order: tree.descriptor.order,
                history: tree.descriptor.history,
                root_hash: tree.descriptor.root_hash,
                datom_count: tree.descriptor.count,
                encoded_bytes: tree.root_bytes,
            })
            .collect::<Vec<_>>();
        let tree_record = TreeManifestRecord {
            database_id: self.database_id.clone(),
            publication_revision,
            basis_t: publish_basis_t,
            index_basis_t,
            tx_hash: publish_tx_hash,
            state_hash: publish_state_hash,
            excision_generation,
            eidx_frontier: build.eidx_frontier,
            manifest_hash: tree_manifest_hash,
            payload: tree_manifest_payload,
            roots: tree_roots,
        };
        let upload_hashes = build.nodes.iter().map(|(hash, _)| *hash).collect();
        self.tree_store.begin_build_intent(
            &self.database_id,
            tree_record.excision_generation,
            expected_publication_revision,
            tree_manifest_hash,
            &upload_hashes,
        )?;
        let publication = (|| {
            for (hash, payload) in build.nodes.iter() {
                self.tree_store.insert_node(*hash, payload)?;
            }
            if fault_point == IndexBuildFault::AfterSegments {
                return Err(SemanticError::new(
                    ErrorCategory::Interrupted,
                    "index/injected-failure",
                    "injected failure after immutable tree-node insertion",
                ));
            }
            if scope.is_background() {
                self.tree_store.publish_manifest_with_delta_bounded(
                    &tree_record,
                    expected_publication_revision,
                    &build.publication_delta,
                )
            } else {
                self.tree_store.publish_manifest_with_delta(
                    &tree_record,
                    expected_publication_revision,
                    &build.publication_delta,
                )
            }
        })();
        let release = self.tree_store.release_build_intent();
        let publication = match publication {
            Ok(publication) => {
                release?;
                publication
            }
            Err(error) => {
                release?;
                return Err(error);
            }
        };
        if let Some(source_pin) = source_manifest_pin {
            self.source_pin_client = Some(source_pin.release()?);
        }
        if let Some(sort_key) = completed_avet_sort {
            // Keep the complete immutable source run across staging, CAS, and
            // injected failure. Only a durable publication (including an
            // exact ambiguous retry) makes it disposable.
            self.tree_store.discard_avet_sort(sort_key)?;
        }
        let tree_store_stats = self.tree_store.stats();
        // Administrative consolidation preserves its one-call completion
        // contract by looping projection successors. Background work reports
        // every durable publication immediately, while explicitly retaining
        // the follow-up flag for bounded live-set folding/reselection.
        if scope == IndexBuildScope::Administrative && !projection_complete {
            return Ok(None);
        }
        Ok(Some(IndexBuildReceipt {
            publication_revision,
            basis_t: publish_basis_t,
            manifest_hash: tree_manifest_hash,
            manifest_candidates_examined: selection.manifest_candidates_examined,
            manifest_candidates_rejected: selection.manifest_candidates_rejected,
            manifest_probe_limit_reached: selection.manifest_probe_limit_reached,
            pending_avet_projections: tree_manifest.pending_avet.len(),
            index_work_remaining: scope.is_background(),
            segment_count: build.nodes.len(),
            reused: publication == TreePublishOutcome::AlreadyPublished,
            input_datoms: build.input_datoms,
            node_writes: tree_store_stats.node_writes,
            node_reuses: tree_store_stats.node_reuses,
            node_reads: tree_store_stats.node_rows_read,
            node_read_bytes: tree_store_stats.node_read_bytes,
            reused_subtrees: build.reused_subtrees,
            tail_datoms: build.tail_datoms,
            encoded_bytes: build.encoded_bytes,
            max_depth: 3,
        }))
    }
}

struct IndexSourceManifestPin {
    client: Client,
    key: i64,
}

fn acquire_index_source_manifest_pin(
    mut client: Client,
    database_id: &str,
    publication_revision: u64,
    manifest_hash: Digest,
) -> Result<IndexSourceManifestPin, SemanticError> {
    let key = tree_manifest_advisory_key(&manifest_hash);
    client
        .query_one("SELECT pg_advisory_lock_shared($1)", &[&key])
        .map_err(|error| postgres_error("index/source-manifest-pin", error))?;
    let available = client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                              WHERE database_id = $1 AND publication_revision = $2 \
                                AND manifest_hash = $3) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress \
                                     WHERE database_id = $1 \
                                       AND publication_revision = $2 \
                                       AND manifest_hash = $3)",
            &[
                &database_id,
                &sql_basis(publication_revision)?,
                &&manifest_hash[..],
            ],
        )
        .map_err(|error| postgres_error("index/source-manifest-pin-verify", error))?
        .get::<_, bool>(0);
    if !available {
        return Err(SemanticError::new(
            ErrorCategory::Conflict,
            "index/source-manifest-retired",
            "native source publication retired before the index build pinned it",
        ));
    }
    Ok(IndexSourceManifestPin { client, key })
}

impl IndexSourceManifestPin {
    fn release(mut self) -> Result<Client, SemanticError> {
        let unlocked = self
            .client
            .query_one("SELECT pg_advisory_unlock_shared($1)", &[&self.key])
            .map_err(|error| postgres_error("index/source-manifest-unpin", error))?
            .get::<_, bool>(0);
        if !unlocked {
            return Err(fault(
                "index/source-manifest-pin-lost",
                "index source session no longer holds its manifest pin",
            ));
        }
        Ok(self.client)
    }
}

fn background_rebuild_required(message: &'static str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "index/background-rebuild-required",
        message,
    )
}

struct NativeIndexBuild {
    trees: Vec<ManifestTree>,
    nodes: TreeNodeSet,
    pending_avet: Vec<AvetProjectionWork>,
    eidx_frontier: u64,
    input_datoms: u64,
    tail_datoms: u64,
    encoded_bytes: u64,
    reused_subtrees: u64,
    /// Disposable external-sort workspace that becomes unnecessary only once
    /// this candidate is durably visible (or confirmed as already visible).
    completed_avet_sort: Option<Digest>,
    publication_delta: TreePublicationDelta,
}

/// Fully materialize the eight immutable native roots for an explicitly
/// named logical value. This is the intentional broad builder used by initial
/// indexing, generation staging, and administrative log reconstruction; the
/// ordinary writer and peer remain root-plus-tail.
pub(crate) struct FullNativeTreeBuild {
    pub(crate) manifest: PersistentTreeManifest,
    pub(crate) nodes: TreeNodeSet,
}

fn build_initial_native(
    database: &Database,
    config: &TreeConfig,
) -> Result<NativeIndexBuild, SemanticError> {
    let (current_eavt, history_eavt) = checkpoint_information(database)?;
    let mut nodes = TreeNodeSet::default();
    let mut trees = Vec::with_capacity(8);
    let mut stats = TreeBuildStats::default();
    for history in [false, true] {
        let source = if history {
            &history_eavt
        } else {
            &current_eavt
        };
        for order in all_index_orders() {
            let mut datoms = source
                .iter()
                .filter(|datom| index_member(database, datom, order))
                .cloned()
                .collect::<Vec<_>>();
            datoms.sort_by(|left, right| left.cmp_in(right, order));
            let built = build_tree(order, history, datoms, config)?;
            merge_tree_stats(&mut stats, built.stats);
            let root_bytes = built
                .nodes
                .get(&built.descriptor.root_hash)
                .ok_or_else(|| fault("tree/missing-built-root", "tree builder omitted its root"))?
                .len() as u64;
            trees.push(ManifestTree {
                descriptor: built.descriptor,
                root_bytes,
            });
            for (hash, bytes) in built.nodes.into_nodes() {
                nodes.insert_known(hash, bytes)?;
            }
        }
    }
    let live_nodes = nodes.iter().map(|(hash, _)| *hash).collect();
    Ok(NativeIndexBuild {
        trees,
        nodes,
        pending_avet: Vec::new(),
        eidx_frontier: database.eidx_frontier(),
        input_datoms: stats.input_datoms,
        tail_datoms: 0,
        encoded_bytes: stats.encoded_bytes,
        reused_subtrees: 0,
        completed_avet_sort: None,
        publication_delta: TreePublicationDelta::Replace { live_nodes },
    })
}

pub(crate) fn build_full_native_tree(
    database_id: &str,
    publication_revision: u64,
    log_generation: u64,
    tx_hash: Digest,
    state_hash: Digest,
    database: &Database,
) -> Result<FullNativeTreeBuild, SemanticError> {
    let build = build_initial_native(database, &TreeConfig::default())?;
    Ok(FullNativeTreeBuild {
        manifest: PersistentTreeManifest {
            database_id: database_id.to_owned(),
            publication_revision,
            index_basis_t: database.basis_t(),
            basis_t: database.basis_t(),
            tx_hash,
            state_hash,
            excision_generation: log_generation,
            eidx_frontier: build.eidx_frontier,
            trees: build.trees,
            pending_avet: Vec::new(),
        },
        nodes: build.nodes,
    })
}

/// Prepare the derived tree for an inactive log generation without exposing
/// its root. The caller deliberately keeps this store's build-intent session
/// pin through log activation and root-last tree publication.
pub(crate) fn stage_full_generation_tree(
    store: &mut PostgresTreeStore,
    database_id: &str,
    log_generation: u64,
    tx_hash: Digest,
    state_hash: Digest,
    database: &Database,
) -> Result<Digest, SemanticError> {
    let expected_revision = store.current_publication_revision(database_id)?;
    let publication_revision = expected_revision.checked_add(1).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "tree/publication-revision-exhausted",
            "persistent tree publication revision is exhausted",
        )
    })?;
    let build = build_full_native_tree(
        database_id,
        publication_revision,
        log_generation,
        tx_hash,
        state_hash,
        database,
    )?;
    let manifest = build.manifest;
    let payload = manifest.encode()?;
    let manifest_hash = sha256(&payload);
    let record = TreeManifestRecord {
        database_id: database_id.to_owned(),
        publication_revision,
        basis_t: manifest.basis_t,
        index_basis_t: manifest.index_basis_t,
        tx_hash,
        state_hash,
        excision_generation: log_generation,
        eidx_frontier: manifest.eidx_frontier,
        manifest_hash,
        payload,
        roots: manifest
            .trees
            .iter()
            .map(|tree| TreeRootBinding {
                order: tree.descriptor.order,
                history: tree.descriptor.history,
                root_hash: tree.descriptor.root_hash,
                datom_count: tree.descriptor.count,
                encoded_bytes: tree.root_bytes,
            })
            .collect(),
    };
    let upload_hashes = build.nodes.iter().map(|(hash, _)| *hash).collect();
    store.begin_build_intent(
        database_id,
        log_generation,
        expected_revision,
        manifest_hash,
        &upload_hashes,
    )?;
    let staged = (|| {
        for (hash, bytes) in build.nodes.iter() {
            store.insert_node(*hash, bytes)?;
        }
        store.stage_manifest_with_delta(
            &record,
            expected_revision,
            &TreePublicationDelta::Replace {
                live_nodes: build.nodes.iter().map(|(hash, _)| *hash).collect(),
            },
        )
    })();
    if let Err(error) = staged {
        store.release_build_intent()?;
        return Err(error);
    }
    Ok(manifest_hash)
}

struct NativeManifestSelection {
    newest_observed_revision: u64,
    newest_observed_manifest_hash: Option<Digest>,
    newest_observed_generation: Option<u64>,
    newest_live_complete: bool,
    newest_live_work_pending: bool,
    manifest_candidates_examined: u64,
    manifest_candidates_rejected: u64,
    manifest_probe_limit_reached: bool,
    usable: Option<(
        PersistentTreeManifest,
        Digest,
        MetadataProjection,
        TreeNodeSet,
    )>,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
struct TreePublicationWindow {
    newest_revision: Option<u64>,
}

#[derive(Debug)]
struct TreePublicationLocator {
    publication_revision: u64,
    manifest_hash: Vec<u8>,
    collecting: bool,
}

/// Capture a stable upper revision with one indexed newest-row lookup. Later
/// keyset pages ignore publications committed after this statement, so load
/// work describes one finite selection window even when an indexer publishes
/// concurrently. Deliberately do not count retained publications: COUNT(*)
/// would make a healthy newest-valid open do work proportional to history.
fn tree_publication_window<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    required_manifest: Option<Digest>,
) -> Result<TreePublicationWindow, SemanticError> {
    let through = sql_basis(through)?;
    let generation = sql_basis(excision_generation)?;
    let row = if let Some(required_manifest) = required_manifest {
        // Manifest hashes are globally unique. Keep historical idempotency
        // receipt opens on that direct index rather than walking newer roots.
        client
            .query_opt(
                "SELECT publication_revision FROM atomic_tree_publications \
                  WHERE manifest_hash = $1 AND database_id = $2 \
                    AND basis_t <= $3 AND log_generation = $4",
                &[&&required_manifest[..], &database_id, &through, &generation],
            )
            .map_err(|error| postgres_error("tree/required-publication-window", error))?
    } else {
        client
            .query_opt(
                "SELECT publication_revision FROM atomic_tree_publications \
                  WHERE database_id = $1 AND basis_t <= $2 AND log_generation = $3 \
                  ORDER BY publication_revision DESC LIMIT 1",
                &[&database_id, &through, &generation],
            )
            .map_err(|error| postgres_error("tree/publication-window", error))?
    };
    Ok(TreePublicationWindow {
        newest_revision: row
            .map(|row| pg_basis(row.get(0), "newest eligible tree publication revision"))
            .transpose()?,
    })
}

/// Read only fixed-width publication locators. Manifest/root payloads are
/// fetched for one selected locator at a time, so corrupt retained history can
/// require more pages but can never make selection materialize every payload.
fn tree_publication_locator_page<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    required_manifest: Option<Digest>,
    window: TreePublicationWindow,
    before_revision: Option<u64>,
) -> Result<Vec<TreePublicationLocator>, SemanticError> {
    let Some(newest_revision) = window.newest_revision else {
        return Ok(Vec::new());
    };
    let before_revision = before_revision.map(sql_basis).transpose()?;
    let rows = if let Some(required_manifest) = required_manifest {
        if before_revision.is_some() {
            return Ok(Vec::new());
        }
        client
            .query(
                "SELECT p.publication_revision, p.manifest_hash, \
                        EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                                 WHERE progress.database_id = p.database_id \
                                   AND progress.publication_revision = p.publication_revision \
                                   AND progress.manifest_hash = p.manifest_hash) \
                   FROM atomic_tree_publications p \
                  WHERE p.manifest_hash = $1 AND p.database_id = $2 \
                    AND p.basis_t <= $3 AND p.log_generation = $4 \
                    AND p.publication_revision <= $5",
                &[
                    &&required_manifest[..],
                    &database_id,
                    &sql_basis(through)?,
                    &sql_basis(excision_generation)?,
                    &sql_basis(newest_revision)?,
                ],
            )
            .map_err(|error| postgres_error("tree/required-publication-locator", error))?
    } else {
        client
            .query(
                "SELECT p.publication_revision, p.manifest_hash, \
                    EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                             WHERE progress.database_id = p.database_id \
                               AND progress.publication_revision = p.publication_revision \
                               AND progress.manifest_hash = p.manifest_hash) \
               FROM atomic_tree_publications p \
              WHERE p.database_id = $1 AND p.basis_t <= $2 \
                AND p.log_generation = $3 \
                AND p.publication_revision <= $4 \
                AND ($5::bigint IS NULL OR p.publication_revision < $5) \
              ORDER BY p.publication_revision DESC LIMIT $6",
                &[
                    &database_id,
                    &sql_basis(through)?,
                    &sql_basis(excision_generation)?,
                    &sql_basis(newest_revision)?,
                    &before_revision,
                    &MANIFEST_SELECTION_PAGE_SIZE,
                ],
            )
            .map_err(|error| postgres_error("tree/publication-locator-page", error))?
    };
    rows.into_iter()
        .map(|row| {
            Ok(TreePublicationLocator {
                publication_revision: pg_basis(row.get(0), "tree publication locator revision")?,
                manifest_hash: row.get(1),
                collecting: row.get(2),
            })
        })
        .collect()
}

fn load_latest_native_manifest<C: GenericClient>(
    client: &mut C,
    tree_store: &mut PostgresTreeStore,
    database_id: &str,
    through: u64,
    excision_generation: u64,
) -> Result<NativeManifestSelection, SemanticError> {
    // Read the physical root coordinate independently of manifest validity.
    // A corrupt newest candidate still consumed its revision and the repair
    // must CAS after it, never reuse its coordinate.
    let newest = client
        .query_opt(
            "SELECT p.publication_revision, p.manifest_hash, p.log_generation, \
                    COALESCE(l.complete, false), \
                    EXISTS (SELECT 1 FROM atomic_tree_delta_headers h \
                             WHERE h.manifest_hash = p.manifest_hash \
                               AND h.delta_state = 2) \
               FROM atomic_tree_publications p \
               LEFT JOIN atomic_tree_live_sets l \
                 ON l.database_id = p.database_id \
                AND l.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("index/tree-live-membership", error))?;
    let (
        newest_observed_revision,
        newest_observed_manifest_hash,
        newest_observed_generation,
        newest_live_complete,
        newest_live_work_pending,
    ) = if let Some(row) = newest {
        (
            pg_basis(row.get(0), "newest tree publication revision")?,
            Some(digest(row.get(1), "newest tree manifest hash")?),
            Some(pg_basis(row.get(2), "newest tree log generation")?),
            row.get(3),
            row.get(4),
        )
    } else {
        (0, None, None, false, false)
    };
    let window = tree_publication_window(client, database_id, through, excision_generation, None)?;
    let mut before_revision = None;
    let mut examined = 0_u64;
    let mut rejected = 0_u64;
    let mut probe_limit_reached = false;
    'candidate_pages: loop {
        let locators = tree_publication_locator_page(
            client,
            database_id,
            through,
            excision_generation,
            None,
            window,
            before_revision,
        )?;
        let Some(last_revision) = locators.last().map(|locator| locator.publication_revision)
        else {
            break;
        };
        for locator in locators {
            if examined == MAX_MANIFEST_CANDIDATE_PROBES {
                probe_limit_reached = true;
                break 'candidate_pages;
            }
            examined = examined.saturating_add(1);
            if locator.collecting {
                rejected = rejected.saturating_add(1);
                continue;
            }
            let manifest_hash = match digest(locator.manifest_hash, "tree manifest locator hash") {
                Ok(hash) => hash,
                Err(_) => {
                    rejected = rejected.saturating_add(1);
                    continue;
                }
            };
            match load_indexer_tree_manifest_candidate(
                client,
                tree_store,
                database_id,
                through,
                excision_generation,
                locator.publication_revision,
                manifest_hash,
            ) {
                Ok(base) => {
                    return Ok(NativeManifestSelection {
                        newest_observed_revision,
                        newest_observed_manifest_hash,
                        newest_observed_generation,
                        newest_live_complete,
                        newest_live_work_pending,
                        manifest_candidates_examined: examined,
                        manifest_candidates_rejected: rejected,
                        manifest_probe_limit_reached: false,
                        usable: Some(base),
                    });
                }
                Err(error) if is_postgres_connection_error(&error) => return Err(error),
                Err(_) => {
                    rejected = rejected.saturating_add(1);
                }
            }
        }
        before_revision = Some(last_revision);
    }
    Ok(NativeManifestSelection {
        newest_observed_revision,
        newest_observed_manifest_hash,
        newest_observed_generation,
        newest_live_complete,
        newest_live_work_pending,
        manifest_candidates_examined: examined,
        manifest_candidates_rejected: rejected,
        manifest_probe_limit_reached: probe_limit_reached,
        usable: None,
    })
}

fn load_indexer_tree_manifest_candidate<C: GenericClient>(
    client: &mut C,
    tree_store: &mut PostgresTreeStore,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    publication_revision: u64,
    manifest_hash: Digest,
) -> Result<
    (
        PersistentTreeManifest,
        Digest,
        MetadataProjection,
        TreeNodeSet,
    ),
    SemanticError,
> {
    let row = client
        .query_opt(
            "SELECT m.basis_t, m.tx_hash, m.state_hash, m.eidx_frontier, \
                    m.manifest_hash, m.payload \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
                AND m.log_generation = p.log_generation \
               JOIN atomic_databases catalog ON catalog.database_id = m.database_id \
               LEFT JOIN atomic_transactions legacy \
                 ON m.log_generation = 0 AND legacy.database_id = m.database_id \
                AND legacy.basis_t = m.basis_t AND legacy.tx_hash = m.tx_hash \
                AND legacy.state_hash = m.state_hash \
               LEFT JOIN atomic_generation_transactions native \
                 ON m.log_generation > 0 AND native.database_id = m.database_id \
                AND native.generation = m.log_generation AND native.basis_t = m.basis_t \
                AND native.tx_hash = m.tx_hash AND native.state_hash = m.state_hash \
               LEFT JOIN atomic_semantic_commitment_roots bootstrap \
                 ON m.log_generation > 0 AND m.basis_t = 0 \
                AND bootstrap.database_id = m.database_id \
                AND bootstrap.generation = m.log_generation AND bootstrap.basis_t = 0 \
                AND bootstrap.tx_hash = m.tx_hash AND bootstrap.state_hash = m.state_hash \
                AND bootstrap.eidx_frontier = m.eidx_frontier \
                AND bootstrap.commitment_version = 2 \
                AND bootstrap.tx_hash = catalog.genesis_hash \
               LEFT JOIN atomic_semantic_commitment_roots semantic \
                 ON semantic.database_id = m.database_id \
                AND semantic.generation = m.log_generation \
                AND semantic.basis_t = m.basis_t \
                AND semantic.tx_hash = m.tx_hash \
                AND semantic.state_hash = m.state_hash \
                AND semantic.eidx_frontier = m.eidx_frontier \
                AND semantic.commitment_version = 2 \
              WHERE p.database_id = $1 AND p.publication_revision = $2 \
                AND p.manifest_hash = $3 AND p.basis_t <= $4 \
                AND p.log_generation = $5 \
                AND semantic.tx_hash IS NOT NULL \
                AND ((m.log_generation = 0 AND legacy.tx_hash IS NOT NULL) \
                  OR (m.log_generation > 0 AND m.basis_t = 0 \
                      AND bootstrap.tx_hash IS NOT NULL) \
                  OR (m.log_generation > 0 AND m.basis_t > 0 \
                      AND native.tx_hash IS NOT NULL))",
            &[
                &database_id,
                &sql_basis(publication_revision)?,
                &&manifest_hash[..],
                &sql_basis(through)?,
                &sql_basis(excision_generation)?,
            ],
        )
        .map_err(|error| postgres_error("index/tree-manifest-candidate", error))?
        .ok_or_else(|| {
            fault(
                "index/tree-manifest-authority",
                "tree publication does not resolve to an authoritative manifest",
            )
        })?;
    let basis_t = pg_basis(row.get(0), "tree manifest basis")?;
    let tx_hash = digest(row.get(1), "tree manifest transaction hash")?;
    let state_hash = digest(row.get(2), "tree manifest state hash")?;
    let eidx_frontier = pg_basis(row.get(3), "tree manifest entity frontier")?;
    let stored_manifest_hash = digest(row.get(4), "tree manifest hash")?;
    let payload: Vec<u8> = row.get(5);
    if stored_manifest_hash != manifest_hash || sha256(&payload) != manifest_hash {
        return Err(fault(
            "index/tree-manifest-hash",
            "tree manifest bytes do not match their publication",
        ));
    }
    let manifest = PersistentTreeManifest::decode(&payload)?;
    if manifest.database_id != database_id
        || manifest.publication_revision != publication_revision
        || manifest.basis_t != basis_t
        || manifest.tx_hash != tx_hash
        || manifest.state_hash != state_hash
        || manifest.excision_generation != excision_generation
        || manifest.eidx_frontier != eidx_frontier
    {
        return Err(fault(
            "index/tree-manifest-metadata",
            "canonical tree manifest disagrees with its authoritative SQL row",
        ));
    }
    let roots = client
        .query(
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $1 \
               ORDER BY history, index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("index/tree-roots", error))?;
    if roots.len() != 8 {
        return Err(fault(
            "index/tree-root-count",
            "published tree manifest does not have eight root bindings",
        ));
    }
    for root in roots {
        let tag = u8::try_from(root.get::<_, i16>(0))
            .map_err(|_| fault("index/tree-root-order", "tree root order is outside u8"))?;
        if tag > 3 {
            return Err(fault(
                "index/tree-root-order",
                "tree root order is outside the four native indexes",
            ));
        }
        let order = order_from_tag(tag);
        let history: bool = root.get(1);
        let expected = manifest.tree(order, history).ok_or_else(|| {
            fault(
                "index/tree-root-coordinate",
                "manifest omits a root binding",
            )
        })?;
        if digest(root.get(2), "tree root hash")? != expected.descriptor.root_hash
            || pg_basis(root.get(3), "tree root count")? != expected.descriptor.count
            || pg_basis(root.get(4), "tree root bytes")? != expected.root_bytes
        {
            return Err(fault(
                "index/tree-root-binding",
                "canonical and relational root bindings disagree",
            ));
        }
    }
    let (metadata, cache) = derive_metadata_from_store(tree_store, &manifest)?;
    validate_avet_work_directions(&manifest.pending_avet, &metadata.schema)?;
    Ok((manifest, manifest_hash, metadata, cache))
}

fn manifest_root_hashes(manifest: &PersistentTreeManifest) -> BTreeMap<(bool, u8), Digest> {
    manifest
        .trees
        .iter()
        .map(|tree| {
            (
                (tree.descriptor.history, order_tag(tree.descriptor.order)),
                tree.descriptor.root_hash,
            )
        })
        .collect()
}

fn load_relational_root_hashes<C: GenericClient>(
    client: &mut C,
    manifest_hash: Digest,
) -> Result<BTreeMap<(bool, u8), Digest>, SemanticError> {
    let rows = client
        .query(
            "SELECT index_order, history, root_hash \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $1 \
              ORDER BY history, index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("index/tree-repair-roots", error))?;
    if rows.len() != 8 {
        return Err(background_rebuild_required(
            "the corrupt newest publication does not retain eight relational roots",
        ));
    }
    let mut roots = BTreeMap::new();
    for row in rows {
        let order = u8::try_from(row.get::<_, i16>(0)).map_err(|_| {
            background_rebuild_required(
                "the corrupt newest publication has an invalid relational root order",
            )
        })?;
        if order > 3
            || roots
                .insert(
                    (row.get(1), order),
                    digest(row.get(2), "tree repair root hash")?,
                )
                .is_some()
        {
            return Err(background_rebuild_required(
                "the corrupt newest publication has invalid relational root coordinates",
            ));
        }
    }
    Ok(roots)
}

fn load_authenticated_index_tail<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    log_generation: u64,
    base_t: u64,
    base_hash: Digest,
    target_t: u64,
    target_hash: Digest,
) -> Result<Vec<(Digest, DurableTransaction)>, SemanticError> {
    if base_t > target_t {
        return Err(fault(
            "index/tree-base-ahead",
            "persistent tree base is ahead of the captured log head",
        ));
    }
    let rows = read_authenticated_log_range(
        client,
        database_id,
        log_generation,
        base_t,
        target_t,
        base_hash,
    )?;
    let end_hash = rows.last().map_or(base_hash, |row| row.tx_hash);
    if end_hash != target_hash {
        return Err(fault(
            "index/tail-head-mismatch",
            "tree base plus authenticated tail does not reach the captured head",
        ));
    }
    Ok(rows
        .into_iter()
        .map(|row| (row.tx_hash, row.transaction))
        .collect())
}

#[derive(Clone)]
struct CurrentTreeChange {
    old: Option<Datom>,
    current: Option<Datom>,
}

#[allow(clippy::too_many_arguments)] // Immutable base, tail, projections, limits, and warm node set are distinct boundaries.
fn build_incremental_native(
    store: &mut PostgresTreeStore,
    previous: &PersistentTreeManifest,
    previous_manifest_hash: Digest,
    tail: &[DurableTransaction],
    tail_hashes: &[Digest],
    base_projection: &MetadataProjection,
    endpoint_projection: MetadataProjection,
    eidx_frontier: u64,
    config: &TreeConfig,
    mut old_cache: TreeNodeSet,
) -> Result<NativeIndexBuild, SemanticError> {
    let limits = RecentLimits {
        soft_datoms: u64::MAX,
        soft_bytes: u64::MAX,
        hard_datoms: u64::MAX,
        hard_bytes: u64::MAX,
    };
    let recent = RecentTier::new_authenticated(
        previous.database_id.clone(),
        previous.basis_t,
        previous.tx_hash,
        tail_hashes.iter().copied().zip(tail.iter().cloned()),
        endpoint_projection.endpoint(),
        limits,
    )?;
    let tail_datoms = tail.iter().try_fold(0_u64, |count, transaction| {
        count
            .checked_add(transaction.tx_data.len() as u64)
            .ok_or_else(|| fault("index/tail-size-overflow", "tail datom count overflow"))
    })?;

    let current_eavt = previous.tree(IndexOrder::Eavt, false).ok_or_else(|| {
        fault(
            "index/missing-current-eavt",
            "manifest omitted current EAVT",
        )
    })?;
    let history_aevt = previous.tree(IndexOrder::Aevt, true).ok_or_else(|| {
        fault(
            "index/missing-history-aevt",
            "manifest omitted history AEVT",
        )
    })?;
    let mut changes = BTreeMap::<Vec<u8>, CurrentTreeChange>::new();
    let mut current_lookup = IndexEavLookup::new(store, &current_eavt.descriptor, &mut old_cache)?;
    for datom in tail.iter().flat_map(|transaction| &transaction.tx_data) {
        let key = stored_eav_key(datom)?;
        if changes.contains_key(&key) {
            continue;
        }
        let found = current_lookup.exact(store, datom, &mut old_cache)?;
        changes.insert(
            key,
            CurrentTreeChange {
                old: found.clone(),
                current: found,
            },
        );
    }
    for datom in tail.iter().flat_map(|transaction| &transaction.tx_data) {
        let key = stored_eav_key(datom)?;
        let change = changes
            .get_mut(&key)
            .expect("tail E/A/V was collected above");
        if datom.added {
            // Datomic's transaction reducer makes one deliberate exception to
            // ordinary redundant-assertion elimination: :db.alter/attribute
            // is a transaction event even when the same E/A/V hook is already
            // current (`filter-assess-tx-datoms`/Db.with in 1.0.7705). The
            // eager kernel and durable replay therefore move the current hook
            // to the newest tx. Preserve that exact replacement here while
            // leaving every ordinary same-E/A/V assertion as a no-op.
            if change.current.is_none() || u64::from(datom.attribute) == crate::DB_ALTER_ATTRIBUTE {
                change.current = Some(datom.clone());
            }
        } else {
            change.current = None;
        }
    }
    let ordinary_removals = changes
        .values()
        .filter_map(|change| match (&change.old, &change.current) {
            (Some(old), Some(current)) if same_stored_datom(old, current) => None,
            (Some(old), _) => Some(old.clone()),
            _ => None,
        })
        .collect::<Vec<_>>();
    let ordinary_insertions = changes
        .values()
        .filter_map(|change| match (&change.old, &change.current) {
            (Some(old), Some(current)) if same_stored_datom(old, current) => None,
            (_, Some(current)) => Some(current.clone()),
            _ => None,
        })
        .collect::<Vec<_>>();

    let (_, endpoint_avet_unready) =
        apply_metadata_and_avet_readiness(base_projection, &BTreeSet::new(), tail, |attribute| {
            let (lower, upper) = attribute_bounds(attribute)?;
            Ok(
                postgres_tree_seek(store, &history_aevt.descriptor, &lower, &mut old_cache)?
                    .is_some_and(|datom| {
                        datom.attribute == attribute
                            && datom.cmp_in(&upper, IndexOrder::Aevt).is_lt()
                    }),
            )
        })?;
    let mut projection_changes =
        changed_avet_attributes(&base_projection.schema, &endpoint_projection.schema)
            .into_iter()
            // Recovered `can-immediately-toggle-storage-has-avet?` makes an empty
            // attribute ready in the same indexing pass.  Only removals and additions
            // for which the storageHasAVET fold found prior values need broad work.
            .filter(|(attribute, _, after)| !*after || endpoint_avet_unready.contains(attribute))
            .map(|change @ (attribute, _, _)| (attribute, change))
            .collect::<BTreeMap<_, _>>();
    // A false->true transition can be hidden by a disable/re-enable cycle
    // inside one uncovered tail. The recovered storageHasAVET fold retains
    // that fact even when base and endpoint schemas compare equal.
    for attribute in endpoint_avet_unready {
        projection_changes.entry(attribute).or_insert((
            attribute,
            effective_avet(&base_projection.schema, attribute),
            true,
        ));
    }
    let changed_avet = projection_changes.into_values().collect::<Vec<_>>();
    // Changed AVET projections become authenticated resumable work. This
    // tail publication deliberately leaves those physical ranges untouched;
    // same-basis successor publications below copy/remove fixed-size chunks.
    let avet_backfills = BTreeMap::<u32, (Vec<Datom>, Vec<Datom>)>::new();
    let avet_drops = BTreeMap::<u32, (Vec<Datom>, Vec<Datom>)>::new();
    let pending_avet = changed_avet
        .iter()
        .map(|(attribute, _, after)| AvetProjectionWork::new(*attribute, *after))
        .collect::<Vec<_>>();

    // Recovered noHistory filtering is local to physically rebuilt segments,
    // but the four history orders are alternative projections of the same
    // facts and their leaf boundaries differ. Use the canonical EAVT selected
    // stream to decide exact omissions, then apply those witnesses to every
    // sibling where the fact is a member. This is a deliberate native
    // consistency strengthening: Datomic documents no precise removal time,
    // and this keeps deterministic cross-index query coherence without a
    // global or fixed-point sweep.
    let history_eavt = previous.tree(IndexOrder::Eavt, true).ok_or_else(|| {
        fault(
            "index/missing-history-eavt",
            "prior manifest omitted a history EAVT tree",
        )
    })?;
    let mut eavt_history_edits = history_edits(
        IndexOrder::Eavt,
        &recent,
        &changed_avet,
        &avet_backfills,
        &avet_drops,
    )?;
    canonicalize_merge_edits(&mut eavt_history_edits, IndexOrder::Eavt);
    preload_merge_paths(
        store,
        &history_eavt.descriptor,
        &eavt_history_edits,
        &mut old_cache,
    )?;
    let coordinated_no_history_pairs =
        discover_merge_no_history_pairs(&history_eavt.descriptor, &old_cache, &eavt_history_edits)?;

    let mut candidate_nodes = TreeNodeSet::default();
    let mut trees = Vec::with_capacity(8);
    let mut encoded_bytes = 0_u64;
    let mut reused_subtrees = 0_u64;
    let mut retired_nodes = BTreeSet::new();
    for history in [false, true] {
        for order in all_index_orders() {
            let old = previous.tree(order, history).ok_or_else(|| {
                fault("index/missing-tree", "prior manifest omitted an index tree")
            })?;
            let mut edits = if history {
                let mut edits =
                    history_edits(order, &recent, &changed_avet, &avet_backfills, &avet_drops)?;
                for pair in &coordinated_no_history_pairs {
                    let avet_projection_changed = order == IndexOrder::Avet
                        && changed_avet
                            .iter()
                            .any(|(attribute, _, _)| *attribute == pair.retraction.attribute);
                    if !avet_projection_changed
                        && schema_index_member(
                            &endpoint_projection.schema,
                            &pair.retraction,
                            order,
                        )?
                    {
                        edits.no_history_pairs.push(pair.clone());
                    }
                }
                // Discovery was coordinated above. Disable the per-order pass
                // so no sibling can make an additional locality-dependent
                // omission after the canonical union is fixed.
                edits.no_history_attributes.clear();
                edits
            } else {
                current_edits(
                    order,
                    &ordinary_removals,
                    &ordinary_insertions,
                    &base_projection.schema,
                    &endpoint_projection.schema,
                    &changed_avet,
                    &avet_backfills,
                    &avet_drops,
                )?
            };
            canonicalize_merge_edits(&mut edits, order);
            preload_merge_paths(store, &old.descriptor, &edits, &mut old_cache)?;
            let merged = merge_native_tree(store, &old.descriptor, &old_cache, &edits, config)?;
            let root_bytes = merged
                .new_nodes
                .get(&merged.descriptor.root_hash)
                .or_else(|| old_cache.get(&merged.descriptor.root_hash))
                .ok_or_else(|| {
                    fault(
                        "index/missing-merged-root",
                        "copy-on-write merge did not resolve its root bytes",
                    )
                })?
                .len() as u64;
            encoded_bytes = encoded_bytes.saturating_add(merged.stats.encoded_bytes_written);
            reused_subtrees = reused_subtrees
                .saturating_add(merged.stats.reused_directory_refs)
                .saturating_add(merged.stats.reused_leaf_refs)
                .saturating_add(merged.stats.reused_hashes);
            retired_nodes.extend(merged.retired_nodes.iter().copied());
            trees.push(ManifestTree {
                descriptor: merged.descriptor,
                root_bytes,
            });
            for (hash, bytes) in merged.new_nodes.into_nodes() {
                candidate_nodes.insert_known(hash, bytes)?;
            }
        }
    }
    let added_nodes = candidate_nodes.iter().map(|(hash, _)| *hash).collect();
    Ok(NativeIndexBuild {
        trees,
        nodes: candidate_nodes,
        pending_avet,
        eidx_frontier,
        input_datoms: tail_datoms,
        tail_datoms,
        encoded_bytes,
        reused_subtrees,
        completed_avet_sort: None,
        publication_delta: TreePublicationDelta::Incremental {
            predecessor_manifest_hash: previous_manifest_hash,
            added_nodes,
            retired_nodes,
        },
    })
}

/// Advance one authenticated AVET projection chunk without advancing logical
/// time. Every returned node set is bounded by one input chunk plus the
/// shallow changed paths, and the successor manifest remains explicitly
/// unavailable for the attribute until both current and history phases seal.
fn build_avet_projection_step(
    store: &mut PostgresTreeStore,
    previous: &PersistentTreeManifest,
    previous_manifest_hash: Digest,
    metadata: &MetadataProjection,
    config: &TreeConfig,
    mut old_cache: TreeNodeSet,
) -> Result<NativeIndexBuild, SemanticError> {
    let work = *previous.pending_avet.first().ok_or_else(|| {
        SemanticError::incorrect(
            "index/no-pending-avet-projection",
            "AVET projection step requires authenticated pending work",
        )
    })?;
    if effective_avet(&metadata.schema, work.attribute) != work.adding {
        return Err(fault(
            "index/pending-avet-schema-mismatch",
            "pending AVET projection direction disagrees with the manifest schema",
        ));
    }

    let target_index = previous
        .trees
        .iter()
        .position(|tree| {
            tree.descriptor.order == IndexOrder::Avet && tree.descriptor.history == work.history
        })
        .ok_or_else(|| {
            fault(
                "index/missing-projection-target",
                "manifest omitted AVET projection target",
            )
        })?;
    let target = &previous.trees[target_index];
    let mut edits = TreeMergeEdits::default();
    let mut projection_source_root = None;
    let (chunk_datoms, chunk_complete, sort_key) = if work.clearing {
        // Clearing always removes the shrinking AVET prefix. This is required
        // not only for a drop, but for disable/data/re-enable tails whose old
        // physical AVET range may be stale before the exact AEVT projection is
        // copied back.
        let chunk = postgres_tree_attribute_chunk(
            store,
            &target.descriptor,
            work.attribute,
            0,
            0,
            0,
            AVET_PROJECTION_CHUNK_DATOMS,
            config.max_leaf_bytes as u64,
            &mut old_cache,
        )?;
        edits.projection_removals = chunk.datoms.clone();
        (chunk.datoms, chunk.complete, None)
    } else {
        debug_assert!(work.adding, "only AVET additions have a copy phase");
        let source = previous
            .tree(IndexOrder::Aevt, work.history)
            .ok_or_else(|| {
                fault(
                    "index/missing-projection-source",
                    "manifest omitted AEVT projection source",
                )
            })?;
        projection_source_root = Some(source.descriptor.root_hash);
        let (sort_key, final_level, row_count) = prepare_avet_projection_sort(
            store,
            &previous.database_id,
            &source.descriptor,
            work.attribute,
            config,
            &mut old_cache,
        )?;
        if work.offset > row_count {
            return Err(fault(
                "index/avet-projection-offset",
                "authenticated AVET projection offset exceeds its regenerated source",
            ));
        }
        if !store.avet_projection_prefix_is_validated(
            source.descriptor.root_hash,
            target.descriptor.root_hash,
            work.attribute,
            work.history,
            work.offset,
        ) {
            if let Err(error) = validate_avet_projection_prefix(
                store,
                &target.descriptor,
                work.attribute,
                sort_key,
                final_level,
                work.offset,
                row_count,
                config.max_leaf_bytes as u64,
                &mut old_cache,
            ) {
                store.discard_avet_sort(sort_key)?;
                return Err(error);
            }
            store.remember_validated_avet_projection_prefix(
                source.descriptor.root_hash,
                target.descriptor.root_hash,
                work.attribute,
                work.history,
                work.offset,
            );
        }
        let datoms = match read_avet_projection_output(
            store,
            sort_key,
            final_level,
            work.offset,
            row_count,
            AVET_PROJECTION_CHUNK_DATOMS,
            config.max_leaf_bytes as u64,
        ) {
            Ok(datoms) => datoms,
            Err(error) => {
                // A finalized spill is disposable derived work. Keeping a
                // corrupt ready marker would make this long-lived indexer
                // retry the same bad run forever; discard it so the next
                // bounded attempt regenerates from immutable AEVT.
                store.discard_avet_sort(sort_key)?;
                return Err(error);
            }
        };
        let complete = work
            .offset
            .checked_add(datoms.len() as u64)
            .is_some_and(|next| next == row_count);
        if datoms.is_empty() && !complete {
            return Err(fault(
                "index/avet-sort-gap",
                "external AVET sort ended before its authenticated row count",
            ));
        }
        edits.insertions = datoms.clone();
        (datoms, complete, Some(sort_key))
    };
    if chunk_datoms
        .iter()
        .any(|datom| datom.attribute != work.attribute)
    {
        return Err(fault(
            "index/avet-projection-range",
            "bounded AVET projection source escaped its attribute range",
        ));
    }
    canonicalize_merge_edits(&mut edits, IndexOrder::Avet);

    let mut trees = previous.trees.clone();
    let mut nodes = TreeNodeSet::default();
    let mut retired_nodes = BTreeSet::new();
    let mut encoded_bytes = 0_u64;
    let mut reused_subtrees = 0_u64;
    if !chunk_datoms.is_empty() {
        preload_merge_paths(store, &target.descriptor, &edits, &mut old_cache)?;
        let merged = merge_native_tree(store, &target.descriptor, &old_cache, &edits, config)?;
        let root_bytes = merged
            .new_nodes
            .get(&merged.descriptor.root_hash)
            .or_else(|| old_cache.get(&merged.descriptor.root_hash))
            .ok_or_else(|| {
                fault(
                    "index/missing-projection-root",
                    "bounded AVET projection merge did not resolve its root",
                )
            })?
            .len() as u64;
        encoded_bytes = merged.stats.encoded_bytes_written;
        reused_subtrees = merged
            .stats
            .reused_directory_refs
            .saturating_add(merged.stats.reused_leaf_refs)
            .saturating_add(merged.stats.reused_hashes);
        retired_nodes.extend(merged.retired_nodes.iter().copied());
        trees[target_index] = ManifestTree {
            descriptor: merged.descriptor,
            root_bytes,
        };
        for (hash, bytes) in merged.new_nodes.into_nodes() {
            nodes.insert_known(hash, bytes)?;
        }
    }

    let mut pending_avet = previous.pending_avet.clone();
    if chunk_complete {
        if work.clearing && work.adding {
            pending_avet[0] = AvetProjectionWork {
                clearing: false,
                offset: 0,
                ..work
            };
        } else if work.history {
            pending_avet.remove(0);
        } else {
            pending_avet[0] = AvetProjectionWork {
                history: true,
                clearing: true,
                offset: 0,
                ..work
            };
        }
    } else if !work.clearing {
        pending_avet[0] = AvetProjectionWork {
            offset: work
                .offset
                .checked_add(chunk_datoms.len() as u64)
                .ok_or_else(|| {
                    fault(
                        "index/avet-projection-offset",
                        "AVET projection offset overflow",
                    )
                })?,
            ..work
        };
    }

    if let (Some(source_root), Some(next)) = (projection_source_root, pending_avet.first())
        && next.attribute == work.attribute
        && next.history == work.history
        && !next.clearing
    {
        store.remember_validated_avet_projection_prefix(
            source_root,
            trees[target_index].descriptor.root_hash,
            next.attribute,
            next.history,
            next.offset,
        );
    }

    let added_nodes = nodes.iter().map(|(hash, _)| *hash).collect();
    Ok(NativeIndexBuild {
        trees,
        nodes,
        pending_avet,
        eidx_frontier: previous.eidx_frontier,
        input_datoms: chunk_datoms.len() as u64,
        tail_datoms: 0,
        encoded_bytes,
        reused_subtrees,
        completed_avet_sort: if chunk_complete { sort_key } else { None },
        publication_delta: TreePublicationDelta::Incremental {
            predecessor_manifest_hash: previous_manifest_hash,
            added_nodes,
            retired_nodes,
        },
    })
}

/// Build (or reuse) one session-local external sort of an immutable AEVT
/// attribute range. Recovered 1.0.7705 spills bounded runs and merges no more
/// than four at a time before `merge-one-index`; this keeps that algorithmic
/// boundary with anonymous local work files containing checksummed native
/// frames. Only the compact final ordinal is durable in the manifest.
#[allow(clippy::too_many_arguments)]
fn prepare_avet_projection_sort(
    store: &mut PostgresTreeStore,
    database_id: &str,
    source: &crate::persistent_tree::TreeDescriptor,
    attribute: u32,
    config: &TreeConfig,
    cache: &mut TreeNodeSet,
) -> Result<(Digest, u32, u64), SemanticError> {
    let work_key = avet_sort_work_key(database_id, source, attribute);
    if let Some((level, count)) = store.avet_sort_ready(work_key)? {
        return Ok((work_key, level, count));
    }
    store.reset_avet_sort(work_key)?;

    let mut directory = 0_u32;
    let mut leaf = 0_u32;
    let mut slot = 0_u32;
    let mut run_count = 0_u64;
    let mut row_count = 0_u64;
    loop {
        let mut chunk = postgres_tree_attribute_chunk(
            store,
            source,
            attribute,
            directory,
            leaf,
            slot,
            AVET_PROJECTION_CHUNK_DATOMS,
            config.max_leaf_bytes as u64,
            cache,
        )?;
        if chunk
            .datoms
            .iter()
            .any(|datom| datom.attribute != attribute)
        {
            return Err(fault(
                "index/avet-sort-range",
                "AEVT external-sort run escaped its attribute range",
            ));
        }
        chunk
            .datoms
            .sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
        if chunk
            .datoms
            .windows(2)
            .any(|pair| !pair[0].cmp_in(&pair[1], IndexOrder::Avet).is_lt())
        {
            return Err(fault(
                "index/avet-sort-duplicate",
                "AEVT projection is not a strict AVET set",
            ));
        }
        let payloads = chunk
            .datoms
            .iter()
            .map(crate::encoding::canonical_datom_bytes)
            .collect::<Result<Vec<_>, _>>()?;
        store.insert_avet_sort_rows(work_key, 0, run_count, 0, &payloads)?;
        row_count = row_count
            .checked_add(payloads.len() as u64)
            .ok_or_else(|| fault("index/avet-sort-size", "AVET sort row count overflow"))?;
        if !payloads.is_empty() {
            run_count = run_count
                .checked_add(1)
                .ok_or_else(|| fault("index/avet-sort-size", "AVET sort run count overflow"))?;
        }
        if chunk.complete {
            break;
        }
        directory = chunk.directory;
        leaf = chunk.leaf;
        slot = chunk.slot;
        // Source traversal is intentionally not a whole-tree cache. The next
        // fixed structural seek reloads at most one root/directory/leaf path.
        *cache = TreeNodeSet::default();
    }

    let mut level = 0_u32;
    while run_count > 1 {
        let next_level = level
            .checked_add(1)
            .ok_or_else(|| fault("index/avet-sort-size", "AVET sort level overflow"))?;
        let output_runs = run_count.div_ceil(AVET_SORT_FAN_IN);
        for output_run in 0..output_runs {
            let first_input = output_run
                .checked_mul(AVET_SORT_FAN_IN)
                .ok_or_else(|| fault("index/avet-sort-size", "AVET sort run overflow"))?;
            let last_input = first_input.saturating_add(AVET_SORT_FAN_IN).min(run_count);
            merge_avet_sort_runs(
                store,
                work_key,
                level,
                first_input..last_input,
                next_level,
                output_run,
                config.max_leaf_bytes as u64,
            )?;
        }
        store.delete_avet_sort_level(work_key, level)?;
        level = next_level;
        run_count = output_runs;
    }
    store.finish_avet_sort(work_key, level, row_count)?;
    Ok((work_key, level, row_count))
}

fn avet_sort_work_key(
    database_id: &str,
    source: &crate::persistent_tree::TreeDescriptor,
    attribute: u32,
) -> Digest {
    let mut bytes = Vec::with_capacity(database_id.len() + 96);
    // v2 binds offsets to tree-ordering v4 (logical value before T/op and
    // only then a stored-representation tie-break). Never resume an ordinal
    // generated by the earlier physical-value-first comparator.
    bytes.extend_from_slice(b"atomic/avet-external-sort/v2\0");
    bytes.extend_from_slice(&(database_id.len() as u64).to_be_bytes());
    bytes.extend_from_slice(database_id.as_bytes());
    bytes.extend_from_slice(&source.root_hash);
    bytes.extend_from_slice(&attribute.to_be_bytes());
    bytes.push(u8::from(source.history));
    bytes.push(match source.order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    });
    sha256(&bytes)
}

struct AvetSortReader {
    run_id: u64,
    next_ordinal: u64,
    buffered: VecDeque<Datom>,
    exhausted: bool,
}

impl AvetSortReader {
    fn new(run_id: u64) -> Self {
        Self {
            run_id,
            next_ordinal: 0,
            buffered: VecDeque::new(),
            exhausted: false,
        }
    }

    fn refill(
        &mut self,
        store: &mut PostgresTreeStore,
        work_key: Digest,
        level: u32,
        maximum_bytes: u64,
    ) -> Result<(), SemanticError> {
        if self.exhausted || !self.buffered.is_empty() {
            return Ok(());
        }
        let rows = store.read_avet_sort_page(
            work_key,
            level,
            self.run_id,
            self.next_ordinal,
            AVET_SORT_PAGE_DATOMS,
            maximum_bytes,
        )?;
        if rows.is_empty() {
            self.exhausted = true;
            return Ok(());
        }
        for (ordinal, payload) in rows {
            if ordinal != self.next_ordinal {
                return Err(fault(
                    "index/avet-sort-ordinal",
                    "external AVET sort run has an ordinal gap",
                ));
            }
            let mut decoded = crate::encoding::decode_canonical_datoms(&payload, 1)?;
            let datom = decoded.pop().expect("one canonical datom was requested");
            if crate::encoding::canonical_datom_bytes(&datom)? != payload {
                return Err(fault(
                    "index/avet-sort-canonical",
                    "external AVET sort row is not canonical",
                ));
            }
            self.buffered.push_back(datom);
            self.next_ordinal = self
                .next_ordinal
                .checked_add(1)
                .ok_or_else(|| fault("index/avet-sort-size", "AVET sort ordinal overflow"))?;
        }
        Ok(())
    }
}

#[allow(clippy::too_many_arguments)]
fn merge_avet_sort_runs(
    store: &mut PostgresTreeStore,
    work_key: Digest,
    input_level: u32,
    input_runs: std::ops::Range<u64>,
    output_level: u32,
    output_run: u64,
    maximum_bytes: u64,
) -> Result<(), SemanticError> {
    let mut readers = input_runs.map(AvetSortReader::new).collect::<Vec<_>>();
    let mut output = Vec::<Vec<u8>>::new();
    let mut output_bytes = 0_u64;
    let mut output_ordinal = 0_u64;
    let mut previous = None::<Datom>;
    loop {
        for reader in &mut readers {
            reader.refill(store, work_key, input_level, maximum_bytes)?;
        }
        let selected = readers
            .iter()
            .enumerate()
            .filter_map(|(index, reader)| reader.buffered.front().map(|datom| (index, datom)))
            .min_by(|(_, left), (_, right)| left.cmp_in(right, IndexOrder::Avet))
            .map(|(index, _)| index);
        let Some(selected) = selected else {
            break;
        };
        let datom = readers[selected]
            .buffered
            .pop_front()
            .expect("selected sort reader has a head");
        if previous
            .as_ref()
            .is_some_and(|previous| !previous.cmp_in(&datom, IndexOrder::Avet).is_lt())
        {
            return Err(fault(
                "index/avet-sort-order",
                "external AVET merge did not produce a strict global order",
            ));
        }
        let payload = crate::encoding::canonical_datom_bytes(&datom)?;
        let payload_bytes = payload.len() as u64;
        if !output.is_empty()
            && (output.len() >= AVET_PROJECTION_CHUNK_DATOMS
                || output_bytes.saturating_add(payload_bytes) > maximum_bytes)
        {
            store.insert_avet_sort_rows(
                work_key,
                output_level,
                output_run,
                output_ordinal,
                &output,
            )?;
            output_ordinal = output_ordinal
                .checked_add(output.len() as u64)
                .ok_or_else(|| fault("index/avet-sort-size", "AVET sort ordinal overflow"))?;
            output.clear();
            output_bytes = 0;
        }
        output_bytes = output_bytes.saturating_add(payload_bytes);
        output.push(payload);
        previous = Some(datom);
    }
    if !output.is_empty() {
        store.insert_avet_sort_rows(work_key, output_level, output_run, output_ordinal, &output)?;
    }
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn read_avet_projection_output(
    store: &mut PostgresTreeStore,
    work_key: Digest,
    final_level: u32,
    first_ordinal: u64,
    row_count: u64,
    maximum_datoms: usize,
    maximum_bytes: u64,
) -> Result<Vec<Datom>, SemanticError> {
    let mut output = Vec::new();
    let mut output_bytes = 0_u64;
    let mut next = first_ordinal;
    while output.len() < maximum_datoms && next < row_count {
        let rows = store.read_avet_sort_page(
            work_key,
            final_level,
            0,
            next,
            AVET_SORT_PAGE_DATOMS.min(maximum_datoms - output.len()),
            maximum_bytes.saturating_sub(output_bytes).max(1),
        )?;
        if rows.is_empty() {
            break;
        }
        for (ordinal, payload) in rows {
            if ordinal != next {
                return Err(fault(
                    "index/avet-sort-ordinal",
                    "final external AVET sort has an ordinal gap",
                ));
            }
            if !output.is_empty()
                && output_bytes.saturating_add(payload.len() as u64) > maximum_bytes
            {
                return Ok(output);
            }
            let mut decoded = crate::encoding::decode_canonical_datoms(&payload, 1)?;
            let datom = decoded.pop().expect("one canonical datom was requested");
            if output
                .last()
                .is_some_and(|previous: &Datom| !previous.cmp_in(&datom, IndexOrder::Avet).is_lt())
            {
                return Err(fault(
                    "index/avet-sort-order",
                    "final external AVET sort page is not strictly ordered",
                ));
            }
            output_bytes = output_bytes.saturating_add(payload.len() as u64);
            output.push(datom);
            next = next
                .checked_add(1)
                .ok_or_else(|| fault("index/avet-sort-size", "AVET sort ordinal overflow"))?;
        }
    }
    Ok(output)
}

#[allow(clippy::too_many_arguments)]
fn validate_avet_projection_prefix(
    store: &mut PostgresTreeStore,
    target: &crate::persistent_tree::TreeDescriptor,
    attribute: u32,
    sort_key: Digest,
    final_level: u32,
    offset: u64,
    row_count: u64,
    maximum_bytes: u64,
    cache: &mut TreeNodeSet,
) -> Result<(), SemanticError> {
    let mut directory = 0_u32;
    let mut leaf = 0_u32;
    let mut slot = 0_u32;
    let mut compared = 0_u64;
    loop {
        let chunk = postgres_tree_attribute_chunk(
            store,
            target,
            attribute,
            directory,
            leaf,
            slot,
            AVET_PROJECTION_CHUNK_DATOMS,
            maximum_bytes,
            cache,
        )?;
        let chunk_count = chunk.datoms.len() as u64;
        if compared.saturating_add(chunk_count) > offset {
            return Err(fault(
                "index/avet-projection-prefix-mismatch",
                "pending AVET target contains rows beyond its authenticated source offset",
            ));
        }
        if !chunk.datoms.is_empty() {
            for (index, actual) in chunk.datoms.iter().enumerate() {
                let ordinal = compared.checked_add(index as u64).ok_or_else(|| {
                    fault(
                        "index/avet-projection-prefix-mismatch",
                        "pending AVET prefix ordinal overflow",
                    )
                })?;
                let expected = read_avet_projection_output(
                    store,
                    sort_key,
                    final_level,
                    ordinal,
                    row_count,
                    1,
                    maximum_bytes,
                )?;
                if expected.len() != 1 || !same_stored_datom(actual, &expected[0]) {
                    return Err(fault(
                        "index/avet-projection-prefix-mismatch",
                        "pending AVET target is not the exact sorted AEVT source prefix",
                    ));
                }
            }
        }
        compared = compared.checked_add(chunk_count).ok_or_else(|| {
            fault(
                "index/avet-projection-prefix-mismatch",
                "pending AVET prefix count overflow",
            )
        })?;
        if chunk.complete {
            if compared == offset {
                return Ok(());
            }
            return Err(fault(
                "index/avet-projection-prefix-mismatch",
                "pending AVET target ends before its authenticated source offset",
            ));
        }
        if chunk.datoms.is_empty() || compared == offset {
            return Err(fault(
                "index/avet-projection-prefix-mismatch",
                "pending AVET target range is not one exact source prefix",
            ));
        }
        directory = chunk.directory;
        leaf = chunk.leaf;
        slot = chunk.slot;
    }
}

#[allow(clippy::too_many_arguments)]
fn current_edits(
    order: IndexOrder,
    removals: &[Datom],
    insertions: &[Datom],
    base_schema: &crate::Schema,
    endpoint_schema: &crate::Schema,
    changed_avet: &[(u32, bool, bool)],
    avet_backfills: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
    avet_drops: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
) -> Result<TreeMergeEdits, SemanticError> {
    let changed = |attribute| {
        order == IndexOrder::Avet
            && changed_avet
                .iter()
                .any(|(candidate, _, _)| *candidate == attribute)
    };
    let mut edits = TreeMergeEdits::default();
    for datom in removals {
        if !changed(datom.attribute) && schema_index_member(base_schema, datom, order)? {
            edits.removals.push(datom.clone());
        }
    }
    for datom in insertions {
        if !changed(datom.attribute) && schema_index_member(endpoint_schema, datom, order)? {
            edits.insertions.push(datom.clone());
        }
    }
    if order == IndexOrder::Avet {
        for (current, _) in avet_backfills.values() {
            edits.insertions.extend(current.iter().cloned());
        }
        for (current, _) in avet_drops.values() {
            edits.projection_removals.extend(current.iter().cloned());
        }
    }
    Ok(edits)
}

fn history_edits(
    order: IndexOrder,
    recent: &RecentTier,
    changed_avet: &[(u32, bool, bool)],
    avet_backfills: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
    avet_drops: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
) -> Result<TreeMergeEdits, SemanticError> {
    let changed = |attribute| {
        order == IndexOrder::Avet
            && changed_avet
                .iter()
                .any(|(candidate, _, _)| *candidate == attribute)
    };
    let projection = recent.projection();
    let mut edits = TreeMergeEdits {
        insertions: recent
            .datoms(order)
            .iter()
            .filter(|datom| !changed(datom.attribute))
            .cloned()
            .collect(),
        // Recovered `filter-nohist-pairs` runs after old and new segment data
        // are merged. Let `merge_tree` inspect every complete selected leaf
        // stream so incidental older durable pairs in rewritten segments are
        // eligible too; untouched leaves remain opaque and unchanged.
        no_history_attributes: projection
            .schema()
            .attributes()
            .filter(|attribute| attribute.no_history && !changed(attribute.id))
            .map(|attribute| attribute.id)
            .collect(),
        ..TreeMergeEdits::default()
    };
    if order == IndexOrder::Avet {
        for (_, history) in avet_backfills.values() {
            edits.insertions.extend(history.iter().cloned());
        }
        for (_, history) in avet_drops.values() {
            edits.projection_removals.extend(history.iter().cloned());
        }
    }
    Ok(edits)
}

fn canonicalize_merge_edits(edits: &mut TreeMergeEdits, order: IndexOrder) {
    sort_dedup_datoms(&mut edits.removals, order);
    sort_dedup_datoms(&mut edits.projection_removals, order);
    sort_dedup_datoms(&mut edits.insertions, order);
    edits
        .no_history_pairs
        .sort_by(|left, right| left.retraction.cmp_in(&right.retraction, order));
}

fn changed_avet_attributes(
    base: &crate::Schema,
    endpoint: &crate::Schema,
) -> Vec<(u32, bool, bool)> {
    let mut attributes = base
        .attributes()
        .chain(endpoint.attributes())
        .map(|attribute| attribute.id)
        .collect::<Vec<_>>();
    attributes.sort_unstable();
    attributes.dedup();
    attributes
        .into_iter()
        .filter_map(|attribute| {
            let before = effective_avet(base, attribute);
            let after = effective_avet(endpoint, attribute);
            (before != after).then_some((attribute, before, after))
        })
        .collect()
}

fn effective_avet(schema: &crate::Schema, attribute: u32) -> bool {
    schema
        .attribute(attribute)
        .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some())
}

fn schema_index_member(
    schema: &crate::Schema,
    datom: &Datom,
    order: IndexOrder,
) -> Result<bool, SemanticError> {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => Ok(true),
        IndexOrder::Avet => {
            let attribute = schema.attribute(datom.attribute)?;
            Ok(attribute.indexed || attribute.unique.is_some())
        }
        IndexOrder::Vaet => {
            Ok(schema.attribute(datom.attribute)?.value_type == crate::ValueType::Ref)
        }
    }
}

fn sort_dedup_datoms(datoms: &mut Vec<Datom>, order: IndexOrder) {
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    datoms.dedup_by(|right, left| same_stored_datom(left, right));
}

fn stored_eav_key(datom: &Datom) -> Result<Vec<u8>, SemanticError> {
    let value = crate::encoding::encode_canonical_value(&datom.value)?;
    let mut key = Vec::with_capacity(12 + value.len());
    key.extend_from_slice(&datom.entity.to_be_bytes());
    key.extend_from_slice(&datom.attribute.to_be_bytes());
    key.extend_from_slice(&value);
    Ok(key)
}

fn eav_bound(exemplar: &Datom, lower: bool) -> Datom {
    Datom {
        entity: exemplar.entity,
        attribute: exemplar.attribute,
        value: exemplar.value.clone(),
        tx: if lower { u64::MAX } else { 0 },
        added: lower,
    }
}

fn attribute_bounds(attribute: u32) -> Result<(Datom, Datom), SemanticError> {
    let end = attribute.checked_add(1).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "index/attribute-range-overflow",
            "AVET attribute range cannot represent an exclusive upper bound",
        )
    })?;
    let bound = |attribute| Datom {
        entity: 0,
        attribute,
        value: crate::Value::Double(f64::NEG_INFINITY),
        tx: u64::MAX,
        added: true,
    };
    Ok((bound(attribute), bound(end)))
}

fn load_old_tree_node(
    store: &mut PostgresTreeStore,
    cache: &mut TreeNodeSet,
    hash: Digest,
) -> Result<TreeNode, SemanticError> {
    if let Some(bytes) = cache.get(&hash) {
        return decode_tree_node(&hash, bytes);
    }
    let bytes = store.load_node(hash)?.ok_or_else(|| {
        fault(
            "index/missing-tree-node",
            "published tree path references missing immutable content",
        )
    })?;
    let node = decode_tree_node(&hash, &bytes)?;
    cache.insert_known(hash, bytes)?;
    Ok(node)
}

fn load_old_root(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    cache: &mut TreeNodeSet,
) -> Result<RootNode, SemanticError> {
    let TreeNode::Root(root) = load_old_tree_node(store, cache, descriptor.root_hash)? else {
        return Err(fault(
            "index/tree-root-kind",
            "tree descriptor resolves to a non-root node",
        ));
    };
    if root.order != descriptor.order
        || root.history != descriptor.history
        || root.count != descriptor.count
        || (root.count == 0) != root.directories.is_empty()
    {
        return Err(fault(
            "index/tree-root-content",
            "tree root disagrees with its manifest descriptor",
        ));
    }
    Ok(root)
}

fn load_old_directory(
    store: &mut PostgresTreeStore,
    cache: &mut TreeNodeSet,
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
) -> Result<DirectoryNode, SemanticError> {
    let TreeNode::Directory(directory) = load_old_tree_node(store, cache, reference.hash)? else {
        return Err(fault(
            "index/tree-directory-kind",
            "root child resolves to a non-directory node",
        ));
    };
    validate_loaded_child_key(
        reference,
        directory.order,
        directory.history,
        directory.count,
        directory.leaves.first().map(|leaf| &leaf.key),
        order,
        history,
    )?;
    Ok(directory)
}

fn load_old_leaf(
    store: &mut PostgresTreeStore,
    cache: &mut TreeNodeSet,
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
) -> Result<LeafSegment, SemanticError> {
    let TreeNode::Leaf(leaf) = load_old_tree_node(store, cache, reference.hash)? else {
        return Err(fault(
            "index/tree-leaf-kind",
            "directory child resolves to a non-leaf node",
        ));
    };
    let first = leaf.datom(0);
    validate_loaded_child_datom(
        reference,
        leaf.order,
        leaf.history,
        leaf.len() as u64,
        first.as_ref(),
        order,
        history,
    )?;
    Ok(leaf)
}

fn postgres_tree_seek(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    key: &Datom,
    cache: &mut TreeNodeSet,
) -> Result<Option<Datom>, SemanticError> {
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(None);
    }
    let first_directory = floor_tree_child(&root.directories, key, descriptor.order);
    for (directory_index, reference) in root.directories.iter().enumerate().skip(first_directory) {
        let directory = load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )?;
        let first_leaf = if directory_index == first_directory {
            floor_tree_child(&directory.leaves, key, descriptor.order)
        } else {
            0
        };
        for leaf_ref in directory.leaves.iter().skip(first_leaf) {
            let leaf = load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
            let index = leaf_lower_bound(&leaf, key, descriptor.order);
            if let Some(datom) = leaf.datom(index) {
                return Ok(Some(datom));
            }
        }
    }
    Ok(None)
}

/// The current-EAV assessment retains one authenticated directory and leaf,
/// not just their encoded bytes. Adjacent bulk edits must not decode an entire
/// segment for every assertion. Memory is bounded by one shallow tree path.
struct IndexEavLookup {
    root: RootNode,
    directory: Option<(usize, DirectoryNode)>,
    leaf: Option<(usize, usize, LeafSegment)>,
}

impl IndexEavLookup {
    fn new(
        store: &mut PostgresTreeStore,
        descriptor: &crate::persistent_tree::TreeDescriptor,
        cache: &mut TreeNodeSet,
    ) -> Result<Self, SemanticError> {
        Ok(Self {
            root: load_old_root(store, descriptor, cache)?,
            directory: None,
            leaf: None,
        })
    }

    /// BigDecimal scale is ordered after T/op. Seek through the complete
    /// logical-comparator group until the exact stored representation appears.
    fn exact(
        &mut self,
        store: &mut PostgresTreeStore,
        exemplar: &Datom,
        cache: &mut TreeNodeSet,
    ) -> Result<Option<Datom>, SemanticError> {
        let lower = eav_bound(exemplar, true);
        let mut candidate = self.seek(store, &lower, false, cache)?;
        while let Some(datom) = candidate {
            if !same_logical_eav(&datom, exemplar) {
                return Ok(None);
            }
            if same_eav(&datom, exemplar) {
                return Ok(Some(datom));
            }
            candidate = self.seek(store, &datom, true, cache)?;
        }
        Ok(None)
    }

    fn seek(
        &mut self,
        store: &mut PostgresTreeStore,
        key: &Datom,
        strict: bool,
        cache: &mut TreeNodeSet,
    ) -> Result<Option<Datom>, SemanticError> {
        if self.root.directories.is_empty() {
            return Ok(None);
        }
        let order = self.root.order;
        let history = self.root.history;
        let first_directory = floor_tree_child(&self.root.directories, key, order);
        for directory_index in first_directory..self.root.directories.len() {
            if self
                .directory
                .as_ref()
                .is_none_or(|(index, _)| *index != directory_index)
            {
                let directory = load_old_directory(
                    store,
                    cache,
                    &self.root.directories[directory_index],
                    order,
                    history,
                )?;
                self.directory = Some((directory_index, directory));
            }
            let directory = &self.directory.as_ref().expect("loaded directory").1;
            let first_leaf = if directory_index == first_directory {
                floor_tree_child(&directory.leaves, key, order)
            } else {
                0
            };
            for leaf_index in first_leaf..directory.leaves.len() {
                if self.leaf.as_ref().is_none_or(|(directory, leaf, _)| {
                    (*directory, *leaf) != (directory_index, leaf_index)
                }) {
                    let leaf =
                        load_old_leaf(store, cache, &directory.leaves[leaf_index], order, history)?;
                    self.leaf = Some((directory_index, leaf_index, leaf));
                }
                let leaf = &self.leaf.as_ref().expect("loaded leaf").2;
                let mut index = if directory_index == first_directory && leaf_index == first_leaf {
                    leaf_lower_bound(leaf, key, order)
                } else {
                    0
                };
                while let Some(datom) = leaf.datom(index) {
                    if !strict || datom.cmp_in(key, order).is_gt() {
                        return Ok(Some(datom));
                    }
                    index += 1;
                }
            }
        }
        Ok(None)
    }
}

struct TreeStructuralChunk {
    datoms: Vec<Datom>,
    directory: u32,
    leaf: u32,
    slot: u32,
    complete: bool,
}

/// Read at most one fixed working chunk from an attribute range. For a new
/// cursor the tree seek establishes absolute root/directory/leaf ordinals;
/// later calls resume those ordinals without copying a possibly huge value
/// into manifest metadata. The source root must remain immutable for the
/// lifetime of the cursor (the AVET-add state machine uses AEVT).
#[allow(clippy::too_many_arguments)]
fn postgres_tree_attribute_chunk(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    attribute: u32,
    resume_directory: u32,
    resume_leaf: u32,
    resume_slot: u32,
    maximum_datoms: usize,
    maximum_bytes: u64,
    cache: &mut TreeNodeSet,
) -> Result<TreeStructuralChunk, SemanticError> {
    if !matches!(descriptor.order, IndexOrder::Aevt | IndexOrder::Avet) {
        return Err(SemanticError::incorrect(
            "index/invalid-avet-projection-source",
            "AVET projection chunks require an AEVT or AVET source",
        ));
    }
    if maximum_datoms == 0 || maximum_bytes == 0 {
        return Err(SemanticError::incorrect(
            "index/invalid-avet-projection-limit",
            "AVET projection chunk limits must be positive",
        ));
    }
    let (start, end) = attribute_bounds(attribute)?;
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(TreeStructuralChunk {
            datoms: Vec::new(),
            directory: 0,
            leaf: 0,
            slot: 0,
            complete: true,
        });
    }

    let fresh = resume_directory == 0 && resume_leaf == 0 && resume_slot == 0;
    let mut directory_index = if fresh {
        floor_tree_child(&root.directories, &start, descriptor.order)
    } else {
        usize::try_from(resume_directory).map_err(|_| {
            fault(
                "index/avet-projection-cursor",
                "AVET projection directory cursor exceeds usize",
            )
        })?
    };
    if directory_index >= root.directories.len() {
        return Err(fault(
            "index/avet-projection-cursor",
            "AVET projection directory cursor is outside its immutable source root",
        ));
    }
    let mut output = Vec::new();
    let mut retained_bytes = 0_u64;
    while directory_index < root.directories.len() {
        let reference = &root.directories[directory_index];
        if !reference.key.cmp_datom(&end, descriptor.order).is_lt() {
            return Ok(TreeStructuralChunk {
                datoms: output,
                directory: 0,
                leaf: 0,
                slot: 0,
                complete: true,
            });
        }
        let directory = load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )?;
        let mut leaf_index = if fresh
            && directory_index == floor_tree_child(&root.directories, &start, descriptor.order)
        {
            floor_tree_child(&directory.leaves, &start, descriptor.order)
        } else {
            usize::try_from(resume_leaf).map_err(|_| {
                fault(
                    "index/avet-projection-cursor",
                    "AVET projection leaf cursor exceeds usize",
                )
            })?
        };
        if !fresh && directory_index != usize::try_from(resume_directory).unwrap_or(usize::MAX) {
            leaf_index = 0;
        }
        while leaf_index < directory.leaves.len() {
            let leaf_ref = &directory.leaves[leaf_index];
            if !leaf_ref.key.cmp_datom(&end, descriptor.order).is_lt() {
                return Ok(TreeStructuralChunk {
                    datoms: output,
                    directory: 0,
                    leaf: 0,
                    slot: 0,
                    complete: true,
                });
            }
            let leaf = load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
            let mut slot = if fresh
                && directory_index == floor_tree_child(&root.directories, &start, descriptor.order)
                && leaf_index == floor_tree_child(&directory.leaves, &start, descriptor.order)
            {
                leaf_lower_bound(&leaf, &start, descriptor.order)
            } else if !fresh
                && directory_index == usize::try_from(resume_directory).unwrap_or(usize::MAX)
                && leaf_index == usize::try_from(resume_leaf).unwrap_or(usize::MAX)
            {
                usize::try_from(resume_slot).map_err(|_| {
                    fault(
                        "index/avet-projection-cursor",
                        "AVET projection slot cursor exceeds usize",
                    )
                })?
            } else {
                0
            };
            if slot > leaf.len() {
                return Err(fault(
                    "index/avet-projection-cursor",
                    "AVET projection slot cursor is outside its immutable source leaf",
                ));
            }
            while slot < leaf.len() {
                let datom = leaf.datom(slot).expect("validated leaf columns");
                if datom.cmp_in(&start, descriptor.order).is_lt() {
                    slot += 1;
                    continue;
                }
                if !datom.cmp_in(&end, descriptor.order).is_lt() {
                    return Ok(TreeStructuralChunk {
                        datoms: output,
                        directory: 0,
                        leaf: 0,
                        slot: 0,
                        complete: true,
                    });
                }
                let datom_bytes = datom.retained_bytes();
                if !output.is_empty()
                    && (output.len() >= maximum_datoms
                        || retained_bytes.saturating_add(datom_bytes) > maximum_bytes)
                {
                    return Ok(TreeStructuralChunk {
                        datoms: output,
                        directory: u32::try_from(directory_index).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection directory cursor exceeds u32",
                            )
                        })?,
                        leaf: u32::try_from(leaf_index).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection leaf cursor exceeds u32",
                            )
                        })?,
                        slot: u32::try_from(slot).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection slot cursor exceeds u32",
                            )
                        })?,
                        complete: false,
                    });
                }
                retained_bytes = retained_bytes.saturating_add(datom_bytes);
                output.push(datom);
                slot += 1;
            }
            leaf_index += 1;
        }
        directory_index += 1;
    }
    Ok(TreeStructuralChunk {
        datoms: output,
        directory: 0,
        leaf: 0,
        slot: 0,
        complete: true,
    })
}

/// Read one exact prefix through authenticated child references. This is the
/// bootstrap primitive for schema/ident reconstruction: it requires no typed
/// schema and therefore avoids a circular metadata side channel.
fn tree_datoms_with_prefix<F>(
    root: &RootNode,
    prefix: &IndexPrefix,
    load_node: &mut F,
) -> Result<Vec<Datom>, SemanticError>
where
    F: FnMut(Digest) -> Result<Arc<TreeNode>, SemanticError>,
{
    prefix.validate()?;
    let order = prefix.order();
    if root.order != order {
        return Err(fault(
            "tree/metadata-root-order",
            "metadata prefix was routed through the wrong persistent index",
        ));
    }
    if root.directories.is_empty() {
        return Ok(Vec::new());
    }

    let mut output = Vec::new();
    let first_directory = prefix_start_child(&root.directories, prefix);
    for directory_ref in root.directories.iter().skip(first_directory) {
        if directory_ref.key.cmp_prefix(prefix).is_gt() {
            break;
        }
        let loaded_directory = load_node(directory_ref.hash)?;
        let TreeNode::Directory(directory) = loaded_directory.as_ref() else {
            return Err(fault(
                "tree/metadata-directory-kind",
                "metadata root child is not a directory",
            ));
        };
        validate_loaded_child_key(
            directory_ref,
            directory.order,
            directory.history,
            directory.count,
            directory.leaves.first().map(|child| &child.key),
            root.order,
            root.history,
        )?;
        let first_leaf = prefix_start_child(&directory.leaves, prefix);
        for leaf_ref in directory.leaves.iter().skip(first_leaf) {
            if leaf_ref.key.cmp_prefix(prefix).is_gt() {
                break;
            }
            let loaded_leaf = load_node(leaf_ref.hash)?;
            let TreeNode::Leaf(leaf) = loaded_leaf.as_ref() else {
                return Err(fault(
                    "tree/metadata-leaf-kind",
                    "metadata directory child is not a leaf",
                ));
            };
            let first = leaf.datom(0);
            validate_loaded_child_datom(
                leaf_ref,
                leaf.order,
                leaf.history,
                leaf.len() as u64,
                first.as_ref(),
                root.order,
                root.history,
            )?;
            for index in 0..leaf.len() {
                let datom = leaf.datom(index).expect("validated metadata leaf columns");
                match crate::index::compare_prefix(&datom, prefix) {
                    std::cmp::Ordering::Less => {}
                    std::cmp::Ordering::Equal => output.push(datom),
                    std::cmp::Ordering::Greater => return Ok(output),
                }
            }
        }
    }
    Ok(output)
}

fn derive_metadata_from_roots<F>(
    roots: &BTreeMap<(bool, u8), Arc<RootNode>>,
    mut load_node: F,
) -> Result<MetadataProjection, SemanticError>
where
    F: FnMut(Digest) -> Result<Arc<TreeNode>, SemanticError>,
{
    let root = |history: bool, order: IndexOrder| {
        roots
            .get(&(history, order_tag(order)))
            .map(Arc::as_ref)
            .ok_or_else(|| {
                fault(
                    "tree/missing-metadata-root",
                    "persistent publication omits an index required for metadata reconstruction",
                )
            })
    };
    let history_aevt = root(true, IndexOrder::Aevt)?;
    let current_aevt = root(false, IndexOrder::Aevt)?;
    let current_eavt = root(false, IndexOrder::Eavt)?;

    // Recovered `ident-setting-datoms` reads assertions from AEVT and folds
    // them in transaction order. Atomic's history tree contains the complete
    // retained stream, and :db/ident is an immutable built-in without
    // :db/noHistory, so aliases and later name repurposing survive bases.
    let ident_prefix = IndexPrefix::Aevt {
        attribute: crate::DB_IDENT as u32,
        entity: None,
        value: None,
    };
    let ident_assertions = tree_datoms_with_prefix(history_aevt, &ident_prefix, &mut load_node)?
        .into_iter()
        .filter(|datom| datom.added)
        .collect::<Vec<_>>();
    let idents = IdentIndex::derive(ident_assertions.iter(), crate::DB_IDENT as u32)?;

    // As recovered `run-hooks` does, discover installed attributes from the
    // two current hook attributes, then read each complete current schema
    // entity through EAVT. General entity idents and unrelated open-entity
    // facts never become a second schema projection.
    let mut schema_current = Vec::new();
    for attribute in [crate::DB_INSTALL_ATTRIBUTE, crate::DB_ALTER_ATTRIBUTE] {
        let prefix = IndexPrefix::Aevt {
            attribute: attribute as u32,
            entity: None,
            value: None,
        };
        schema_current.extend(tree_datoms_with_prefix(
            current_aevt,
            &prefix,
            &mut load_node,
        )?);
    }
    let mut installed = BTreeSet::new();
    for datom in &schema_current {
        if !datom.added || datom.entity != crate::DB_PART_DB {
            continue;
        }
        let crate::Value::Ref(entity) = datom.value else {
            return Err(fault(
                "tree/invalid-schema-hook",
                "current schema hook does not contain an attribute entity ref",
            ));
        };
        installed.insert(entity);
    }
    for entity in installed {
        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: None,
            value: None,
        };
        schema_current.extend(
            tree_datoms_with_prefix(current_eavt, &prefix, &mut load_node)?
                .into_iter()
                .filter(|datom| schema_information_attribute(datom.attribute)),
        );
    }
    MetadataProjection::from_current_and_idents(schema_current, idents)
}

fn derive_metadata_from_store(
    store: &mut PostgresTreeStore,
    manifest: &PersistentTreeManifest,
) -> Result<(MetadataProjection, TreeNodeSet), SemanticError> {
    let mut cache = TreeNodeSet::default();
    let mut roots = BTreeMap::new();
    // A manifest candidate is accepted only after all eight root payloads
    // authenticate against their descriptors. Metadata reconstruction needs
    // three of them, but accepting a candidate with a corrupt sibling root
    // would defer failure until incremental merge and prevent orderly fallback
    // to the preceding immutable publication.
    for history in [false, true] {
        for order in all_index_orders() {
            let descriptor = &manifest
                .tree(order, history)
                .ok_or_else(|| {
                    fault(
                        "tree/missing-root",
                        "manifest omits one of the eight required persistent indexes",
                    )
                })?
                .descriptor;
            roots.insert(
                (history, order_tag(order)),
                Arc::new(load_old_root(store, descriptor, &mut cache)?),
            );
        }
    }
    let metadata = derive_metadata_from_roots(&roots, |hash| {
        load_old_tree_node(store, &mut cache, hash).map(Arc::new)
    })?;
    Ok((metadata, cache))
}

fn merge_native_tree(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    preloaded: &TreeNodeSet,
    edits: &TreeMergeEdits,
    config: &TreeConfig,
) -> Result<crate::persistent_tree::TreeMerge, SemanticError> {
    merge_tree_with_boundary_loader(descriptor, preloaded, edits, config, &mut |hash| {
        store.load_node(*hash)?.ok_or_else(|| {
            fault(
                "index/missing-tree-node",
                "published tree boundary references missing immutable content",
            )
        })
    })
}

fn preload_merge_paths(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    edits: &TreeMergeEdits,
    cache: &mut TreeNodeSet,
) -> Result<(), SemanticError> {
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(());
    }
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
    sort_dedup_datoms(&mut points, descriptor.order);
    let selected = select_merge_leaves(&root, &points, |reference| {
        load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )
    })?;
    for (directory_index, leaf_index) in selected.leaves {
        let leaf_ref = &selected.directories[&directory_index].leaves[leaf_index];
        load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
    }
    Ok(())
}

struct SelectedMergeLeaves {
    directories: BTreeMap<usize, DirectoryNode>,
    leaves: BTreeSet<(usize, usize)>,
}

/// Plan the union of touched and separator-repair paths before decoding their
/// leaves. Cached immutable bytes still require authentication, but doing that
/// once per insertion turned one bulk merge into repeated whole-leaf decoding.
/// Coordinates (not just hashes) retain every distinct parent/child witness.
fn select_merge_leaves(
    root: &RootNode,
    points: &[Datom],
    mut load_directory: impl FnMut(&ChildRef) -> Result<DirectoryNode, SemanticError>,
) -> Result<SelectedMergeLeaves, SemanticError> {
    let mut directories = BTreeMap::new();
    let mut leaves = BTreeSet::new();
    for point in points {
        let directory_index = floor_tree_child(&root.directories, point, root.order);
        let directory = match directories.entry(directory_index) {
            std::collections::btree_map::Entry::Occupied(entry) => entry.into_mut(),
            std::collections::btree_map::Entry::Vacant(entry) => {
                entry.insert(load_directory(&root.directories[directory_index])?)
            }
        };
        // The affected directory itself is re-encoded even when only one
        // interior leaf changes. Root separator repair authenticates its new
        // first and last datoms, which may live in otherwise untouched leaves.
        leaves.insert((directory_index, 0));
        leaves.insert((directory_index, directory.leaves.len() - 1));
        let leaf_index = floor_tree_child(&directory.leaves, point, root.order);
        let first_leaf = leaf_index.saturating_sub(1);
        let last_leaf = leaf_index
            .saturating_add(1)
            .min(directory.leaves.len().saturating_sub(1));
        for leaf_index in first_leaf..=last_leaf {
            leaves.insert((directory_index, leaf_index));
        }

        // Rewriting a leaf or directory can change the sparse separator on
        // either side even though those neighboring children are themselves
        // reused. `merge_tree` authenticates those boundary datoms when it
        // repairs routing keys, so preload exactly those adjacent paths too.
        if let Some(previous_index) = directory_index.checked_sub(1) {
            let previous = match directories.entry(previous_index) {
                std::collections::btree_map::Entry::Occupied(entry) => entry.into_mut(),
                std::collections::btree_map::Entry::Vacant(entry) => {
                    entry.insert(load_directory(&root.directories[previous_index])?)
                }
            };
            leaves.insert((previous_index, previous.leaves.len() - 1));
        }
        let next_index = directory_index + 1;
        if let Some(next_ref) = root.directories.get(next_index) {
            if let std::collections::btree_map::Entry::Vacant(entry) = directories.entry(next_index)
            {
                entry.insert(load_directory(next_ref)?);
            }
            leaves.insert((next_index, 0));
        }
    }
    Ok(SelectedMergeLeaves {
        directories,
        leaves,
    })
}

fn all_index_orders() -> [IndexOrder; 4] {
    [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ]
}

fn merge_tree_stats(total: &mut TreeBuildStats, one: TreeBuildStats) {
    total.input_datoms = total.input_datoms.saturating_add(one.input_datoms);
    total.leaf_nodes = total.leaf_nodes.saturating_add(one.leaf_nodes);
    total.directory_nodes = total.directory_nodes.saturating_add(one.directory_nodes);
    total.root_nodes = total.root_nodes.saturating_add(one.root_nodes);
    total.unique_nodes = total.unique_nodes.saturating_add(one.unique_nodes);
    total.encoded_bytes = total.encoded_bytes.saturating_add(one.encoded_bytes);
    total.peak_live_datoms = total.peak_live_datoms.max(one.peak_live_datoms);
    total.largest_leaf_bytes = total.largest_leaf_bytes.max(one.largest_leaf_bytes);
    total.largest_directory_bytes = total
        .largest_directory_bytes
        .max(one.largest_directory_bytes);
    total.root_bytes = total.root_bytes.saturating_add(one.root_bytes);
}

/// One immutable native database endpoint. This is the Rust counterpart of
/// the recovered `Db` fields that describe index roots, memidx, resident
/// elements/idents, and basis coordinates. It deliberately contains neither
/// live-peer coordination nor an eager compatibility value.
#[derive(Clone)]
struct TieredState {
    basis_t: u64,
    eidx_frontier: u64,
    current_hash: Digest,
    current_state_hash: Digest,
    excision_generation: u64,
    durable_base_t: u64,
    tree_base: Option<Arc<TreeBase>>,
    _root_pin: Option<Arc<RootPin>>,
    /// Protects the authoritative generation used by this immutable value,
    /// including log-only values which have no physical tree root to pin.
    _generation_pin: Arc<GenerationPin>,
    recent: Arc<RecentTier>,
    metadata: Arc<MetadataProjection>,
    /// Existing attributes whose AVET membership became true after the
    /// durable base. Their pre-base values cannot appear until a covering
    /// indexing publication backfills them.
    avet_unready: Arc<BTreeSet<u32>>,
    generation: u64,
}

/// Live peers pair an immutable native endpoint with an optional eager oracle.
/// Keeping the cell outside `TieredState` prevents native values and eventual
/// writer state from accidentally retaining a complete `Database`.
#[derive(Clone)]
struct PeerState {
    tiered: Arc<TieredState>,
    compatibility: Arc<PeerCompatibility>,
}

impl Deref for PeerState {
    type Target = TieredState;

    fn deref(&self) -> &Self::Target {
        &self.tiered
    }
}

struct PeerIo {
    client: Client,
    tree_cache: TreeNodeCache,
}

/// Everything needed only by the explicit eager-oracle path. Native database
/// values never retain this attachment or its legacy segment cache.
struct PeerCompatibility {
    value: OnceLock<Arc<Database>>,
    segments: Arc<Mutex<SegmentCache>>,
}

struct RootPinState {
    client: Option<Client>,
    counts: BTreeMap<Digest, u64>,
    generation_counts: BTreeMap<u64, u64>,
}

/// One PostgreSQL session per live PeerCore, regardless of how many immutable
/// values it retains. PostgreSQL session advisory locks are reference counted
/// here: the first state using a manifest acquires its shared lock and the last
/// state drop releases it.
struct RootPinManager {
    connection: PostgresConnectionConfig,
    database_id: String,
    application_name: String,
    state: Mutex<RootPinState>,
}

impl RootPinManager {
    fn connect(
        connection: &PostgresConnectionConfig,
        database_id: &str,
    ) -> Result<Arc<Self>, SemanticError> {
        let manager = Arc::new(Self {
            connection: connection.clone(),
            database_id: database_id.to_owned(),
            application_name: root_pin_application_name(database_id),
            state: Mutex::new(RootPinState {
                client: None,
                counts: BTreeMap::new(),
                generation_counts: BTreeMap::new(),
            }),
        });
        {
            let mut state = lock(&manager.state);
            manager.reconnect_locked(&mut state)?;
        }
        Ok(manager)
    }

    fn acquire(
        self: &Arc<Self>,
        tree: Option<&TreeBase>,
    ) -> Result<Option<Arc<RootPin>>, SemanticError> {
        let Some(tree) = tree else {
            return Ok(None);
        };
        self.acquire_manifest(tree.manifest_hash, true).map(Some)
    }

    /// `verify_authority=false` is a pre-discovery fence only. The caller
    /// must authenticate the exact required manifest before exposing a value.
    /// Retaining that same pin prevents an archive conversion from changing
    /// source ownership between the scan's READ COMMITTED statements.
    fn acquire_manifest(
        self: &Arc<Self>,
        manifest_hash: Digest,
        verify_authority: bool,
    ) -> Result<Arc<RootPin>, SemanticError> {
        let mut state = lock(&self.state);
        self.ensure_locked(&mut state)?;
        if !state.counts.contains_key(&manifest_hash)
            && let Err(error) = if verify_authority {
                acquire_root_pin(
                    state.client.as_mut().expect("root pin session was ensured"),
                    manifest_hash,
                )
            } else {
                state
                    .client
                    .as_mut()
                    .expect("root pin session was ensured")
                    .query_one(
                        "SELECT pg_advisory_lock_shared($1)",
                        &[&tree_manifest_advisory_key(&manifest_hash)],
                    )
                    .map(|_| ())
                    .map_err(|error| postgres_error("peer/exact-manifest-fence", error))
            }
        {
            // The server may have accepted the session lock before a later
            // verification statement failed. Discarding the whole session
            // is the only unconditional way to release an advisory lock
            // whose stack depth is now uncertain.
            state.client = None;
            // Existing immutable values may already own counted pins on
            // this manager. Re-establish those locks before returning the
            // new acquisition error whenever PostgreSQL is reachable.
            let _ = self.reconnect_locked(&mut state);
            return Err(error);
        }
        *state.counts.entry(manifest_hash).or_default() += 1;
        drop(state);
        Ok(Arc::new(RootPin {
            manager: Arc::clone(self),
            manifest_hash,
        }))
    }

    fn acquire_generation(
        self: &Arc<Self>,
        generation: u64,
    ) -> Result<Arc<GenerationPin>, SemanticError> {
        let mut state = lock(&self.state);
        self.ensure_locked(&mut state)?;
        if !state.generation_counts.contains_key(&generation)
            && let Err(error) = acquire_generation_pin(
                state.client.as_mut().expect("root pin session was ensured"),
                &self.database_id,
                generation,
            )
        {
            state.client = None;
            let _ = self.reconnect_locked(&mut state);
            return Err(error);
        }
        *state.generation_counts.entry(generation).or_default() += 1;
        drop(state);
        Ok(Arc::new(GenerationPin {
            manager: Arc::clone(self),
            generation,
        }))
    }

    fn ensure(&self) -> Result<(), SemanticError> {
        let mut state = lock(&self.state);
        self.ensure_locked(&mut state)
    }

    fn ensure_locked(&self, state: &mut RootPinState) -> Result<(), SemanticError> {
        let healthy = if let Some(client) = state.client.as_mut() {
            match client.simple_query("SELECT 1") {
                Ok(_) => true,
                Err(error) => {
                    let error = postgres_error("peer/root-pin-health", error);
                    if is_postgres_connection_error(&error) {
                        false
                    } else {
                        return Err(error);
                    }
                }
            }
        } else {
            false
        };
        if healthy {
            return Ok(());
        }
        state.client = None;
        self.reconnect_locked(state)
    }

    fn reconnect_locked(&self, state: &mut RootPinState) -> Result<(), SemanticError> {
        let mut client = self.connection.connect_for("peer/root-pin-connect")?;
        verify_schema_compatibility(&mut client)?;
        client
            .query_one(
                "SELECT set_config('application_name', $1, false)",
                &[&self.application_name],
            )
            .map_err(|error| postgres_error("peer/root-pin-name", error))?;
        client
            .batch_execute("SET default_transaction_read_only = on")
            .map_err(|error| postgres_error("peer/root-pin-read-only", error))?;
        // PostgreSQL restart drops every advisory lock. Reacquire every live
        // generation and manifest before this manager is considered healthy.
        // The operator-chosen GC grace covers unavailable intervals; a zero or
        // short horizon deliberately accepts that risk, and reacquisition
        // fails closed if collection already began.
        for generation in state.generation_counts.keys() {
            acquire_generation_pin(&mut client, &self.database_id, *generation)?;
        }
        for hash in state.counts.keys() {
            acquire_root_pin(&mut client, *hash)?;
        }
        state.client = Some(client);
        Ok(())
    }

    fn hashes(&self) -> Vec<Digest> {
        lock(&self.state).counts.keys().copied().collect()
    }

    fn release(&self, manifest_hash: Digest) {
        let mut state = lock(&self.state);
        let Some(count) = state.counts.get_mut(&manifest_hash) else {
            return;
        };
        *count = count.saturating_sub(1);
        if *count != 0 {
            return;
        }
        state.counts.remove(&manifest_hash);
        // Drop cannot report an unlock failure. Discard an uncertain session
        // instead of leaking one uncounted advisory-lock depth forever, then
        // best-effort restore every pin that still has a live local owner.
        let unlocked = state.client.as_mut().is_none_or(|client| {
            client
                .query_one(
                    "SELECT pg_advisory_unlock_shared($1)",
                    &[&tree_manifest_advisory_key(&manifest_hash)],
                )
                .is_ok_and(|row| row.get(0))
        });
        if !unlocked {
            state.client = None;
            let _ = self.reconnect_locked(&mut state);
        }
    }

    fn release_generation(&self, generation: u64) {
        let mut state = lock(&self.state);
        let Some(count) = state.generation_counts.get_mut(&generation) else {
            return;
        };
        *count = count.saturating_sub(1);
        if *count != 0 {
            return;
        }
        state.generation_counts.remove(&generation);
        let generation_sql = sql_basis(generation).expect("a pinned generation fit PostgreSQL");
        let unlocked = state.client.as_mut().is_none_or(|client| {
            client
                .query_opt(
                    "SELECT atomic_log_generation_pin_key($1, $2)",
                    &[&self.database_id, &generation_sql],
                )
                .ok()
                .flatten()
                .and_then(|row| row.get::<_, Option<i64>>(0))
                .is_some_and(|key| {
                    client
                        .query_one("SELECT pg_advisory_unlock_shared($1)", &[&key])
                        .is_ok_and(|row| row.get(0))
                })
        });
        if !unlocked {
            state.client = None;
            let _ = self.reconnect_locked(&mut state);
        }
    }
}

fn acquire_generation_pin(
    client: &mut Client,
    database_id: &str,
    generation: u64,
) -> Result<(), SemanticError> {
    let generation_sql = sql_basis(generation)?;
    let key: i64 = client
        .query_opt(
            "SELECT atomic_log_generation_pin_key($1, $2)",
            &[&database_id, &generation_sql],
        )
        .map_err(|error| postgres_error("peer/generation-pin-key", error))?
        .ok_or_else(|| fault("peer/generation-pin-database", "database no longer exists"))?
        .get(0);
    client
        .query_one("SELECT pg_advisory_lock_shared($1)", &[&key])
        .map_err(|error| postgres_error("peer/generation-pin-acquire", error))?;
    let available: bool = match client.query_one(
        "SELECT EXISTS (SELECT 1 FROM atomic_heads \
                              WHERE database_id = $1 AND log_generation = $2) \
                    OR EXISTS (SELECT 1 FROM atomic_log_generation_retirements \
                                WHERE database_id = $1 AND generation = $2 \
                                  AND collecting_at IS NULL)",
        &[&database_id, &generation_sql],
    ) {
        Ok(row) => row.get(0),
        Err(error) => {
            let error = postgres_error("peer/generation-pin-verify", error);
            let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&key]);
            return Err(error);
        }
    };
    if !available {
        let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&key]);
        return Err(fault(
            "peer/generation-retired-during-load",
            "log generation became unavailable before the immutable value was pinned",
        ));
    }
    Ok(())
}

fn acquire_root_pin(client: &mut Client, manifest_hash: Digest) -> Result<(), SemanticError> {
    client
        .query_one(
            "SELECT pg_advisory_lock_shared($1)",
            &[&tree_manifest_advisory_key(&manifest_hash)],
        )
        .map_err(|error| postgres_error("peer/root-pin-acquire", error))?;
    // Close the load-versus-GC race. If GC obtained the exclusive lock first,
    // this SELECT runs after its commit and refuses to expose a state whose
    // publication has just been retired.
    let key = tree_manifest_advisory_key(&manifest_hash);
    let published: bool = match client.query_one(
        "SELECT (EXISTS (SELECT 1 FROM atomic_tree_publications \
                             WHERE manifest_hash = $1) \
                     AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress \
                                     WHERE manifest_hash = $1)) \
                    OR EXISTS ( \
                         SELECT 1 FROM atomic_request_base_archives archive \
                         JOIN atomic_request_base_archive_completions complete \
                           ON complete.manifest_hash = archive.manifest_hash \
                        WHERE archive.manifest_hash = $1 \
                    )",
        &[&&manifest_hash[..]],
    ) {
        Ok(row) => row.get(0),
        Err(error) => {
            let error = postgres_error("peer/root-pin-verify", error);
            let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&key]);
            return Err(error);
        }
    };
    if !published {
        let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&key]);
        return Err(fault(
            "peer/root-retired-during-load",
            "tree publication retired before the immutable peer value was pinned",
        ));
    }
    Ok(())
}

fn root_pin_application_name(database_id: &str) -> String {
    let digest = sha256(database_id.as_bytes());
    let suffix: String = digest[..16]
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect();
    format!("atomic-pin-{suffix}")
}

struct RootPin {
    manager: Arc<RootPinManager>,
    manifest_hash: Digest,
}

struct GenerationPin {
    manager: Arc<RootPinManager>,
    generation: u64,
}

impl Drop for GenerationPin {
    fn drop(&mut self) {
        self.manager.release_generation(self.generation);
    }
}

impl Drop for RootPin {
    fn drop(&mut self) {
        self.manager.release(self.manifest_hash);
    }
}

/// Process-local resources required by immutable native reads. Caches and the
/// PostgreSQL I/O lane are shared across values, while root/generation pins in
/// each `TieredState` keep their exact durable content alive.
struct TieredReadCore {
    database_id: String,
    lineage_id: String,
    connection: PostgresConnectionConfig,
    recent_limits: RecentLimits,
    load_counters: PeerLoadCounters,
    root_pins: Arc<RootPinManager>,
    programs: crate::postgres::SharedProgramCache,
    io: Mutex<PeerIo>,
}

struct PeerCore {
    read: Arc<TieredReadCore>,
    allow_compatibility: bool,
    recent_limits: RecentLimits,
    /// Serializes catch-up/adoption work. Readers never take this lock.
    update: Mutex<()>,
    /// Explicit transaction-report observation channel. `None` is the normal
    /// state; reports are retained only after the caller opts in.
    reports: Mutex<Option<VecDeque<ServiceTransactionReport>>>,
    report_ready: Condvar,
    state_advanced: Condvar,
    observed_basis: Mutex<u64>,
    /// One publication cell for all peer-observable connection state.
    state: RwLock<Arc<PeerState>>,
}

fn reconnect_peer_io(core: &TieredReadCore, io: &mut PeerIo) -> Result<(), SemanticError> {
    let mut client = core.connection.connect_for("peer/reconnect")?;
    verify_schema_compatibility(&mut client)?;
    verify_database_lineage(&mut client, &core.database_id, &core.lineage_id)?;
    io.client = client;
    Ok(())
}

fn reconnect_peer_io_with_timeout(
    core: &TieredReadCore,
    io: &mut PeerIo,
    timeout: Duration,
) -> Result<(), SemanticError> {
    let mut client = core
        .connection
        .connect_for_with_timeout("peer/reconnect", Some(timeout))?;
    verify_schema_compatibility(&mut client)?;
    verify_database_lineage(&mut client, &core.database_id, &core.lineage_id)?;
    io.client = client;
    Ok(())
}

/// A cloneable live peer connection. Every clone shares one monotonically
/// advancing state, while database values returned from `db` remain immutable
/// after the connection advances.
#[derive(Clone)]
pub struct Peer {
    core: Arc<PeerCore>,
}

impl Peer {
    #[cfg(test)]
    pub(crate) fn pause_updates_for_test(&self) -> MutexGuard<'_, ()> {
        lock(&self.core.update)
    }

    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured(
            &PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_capacity,
        )
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            connection,
            database_id,
            cache_capacity,
            cache_capacity.saturating_mul(512 * 1024),
        )
    }

    pub fn connect_with_cache_limits(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            &PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_entries,
            cache_bytes,
        )
    }

    pub fn connect_configured_with_cache_limits(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_limits(
            connection,
            database_id,
            cache_entries,
            cache_bytes,
            RecentLimits::default(),
        )
    }

    pub fn connect_with_limits(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_limits(
            &PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_entries,
            cache_bytes,
            recent_limits,
        )
    }

    pub fn connect_configured_with_limits(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        Self::connect_with_mode(
            connection,
            database_id.into(),
            cache_entries,
            cache_bytes,
            recent_limits,
            false,
        )
    }

    /// Explicit administrative/legacy adapter. Unlike ordinary peer opens,
    /// this may reconstruct an entire eager database when no native root exists.
    pub fn connect_compatibility(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_compatibility_configured(
            &PostgresConnectionConfig::plaintext(connection),
            database_id,
            cache_entries,
        )
    }

    pub fn connect_compatibility_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_with_mode(
            connection,
            database_id.into(),
            cache_entries,
            cache_entries.saturating_mul(512 * 1024),
            RecentLimits::default(),
            true,
        )
    }

    fn connect_with_mode(
        connection: &PostgresConnectionConfig,
        database_id: String,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
        allow_compatibility: bool,
    ) -> Result<Self, SemanticError> {
        let mut client = connection.connect_for("peer/connect")?;
        // Fail before reading a head or any derived value when this peer does
        // not understand the installed PostgreSQL schema.
        verify_schema_compatibility(&mut client)?;
        let lineage_id = read_database_lineage(&mut client, &database_id)?;
        let (head_basis, head_hash) = read_head(&mut client, &database_id)?;
        let excision_generation = read_excision_generation(&mut client, &database_id)?;
        let mut cache = SegmentCache::new(cache_entries);
        let mut tree_cache = TreeNodeCache::new(cache_entries, cache_bytes);
        let load_counters = PeerLoadCounters::default();
        let tree_base = load_latest_tree_base(
            &mut client,
            &database_id,
            head_basis,
            excision_generation,
            &load_counters,
            &mut tree_cache,
        )?
        .map(Arc::new);
        let (
            basis_t,
            eidx_frontier,
            current_hash,
            current_state_hash,
            durable_base_t,
            recent,
            metadata,
            avet_unready,
            compatibility,
        ) = if let Some(base) = tree_base.as_deref() {
            let base_metadata = Arc::clone(&base.metadata);
            let tail = read_authenticated_tail(
                &mut client,
                &database_id,
                excision_generation,
                TailBase {
                    basis_t: base.manifest.basis_t,
                    tx_hash: base.manifest.tx_hash,
                    state_hash: base.manifest.state_hash,
                    eidx_frontier: base.manifest.eidx_frontier,
                },
                head_basis,
                None,
            )?;
            if tail.end_hash != head_hash {
                return Err(fault(
                    "peer/head-mismatch",
                    "native tree plus authenticated tail does not reach observed head",
                ));
            }
            let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
                &base_metadata,
                &manifest_avet_unready(&base.manifest),
                &tail.transactions,
                |attribute| {
                    tree_base_has_attribute_history(
                        &mut client,
                        base,
                        attribute,
                        &load_counters,
                        &mut tree_cache,
                    )
                },
            )?;
            let metadata = Arc::new(metadata);
            let avet_unready = Arc::new(avet_unready);
            let AuthenticatedTail {
                transactions,
                transaction_hashes,
                end_hash,
                end_state_hash,
                eidx_frontier,
                range_reads: _,
                state_hashes: _,
            } = tail;
            let recent = Arc::new(RecentTier::new_authenticated_existing(
                &database_id,
                base.manifest.basis_t,
                base.manifest.tx_hash,
                transaction_hashes.into_iter().zip(transactions),
                metadata.endpoint(),
                recent_limits,
            )?);
            (
                head_basis,
                eidx_frontier,
                end_hash,
                end_state_hash,
                base.manifest.basis_t,
                recent,
                metadata,
                avet_unready,
                OnceLock::new(),
            )
        } else {
            if !allow_compatibility {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "peer/native-index-required",
                    "ordinary peers require a native index; run administrative consolidation or explicitly open the eager compatibility adapter",
                ));
            }
            // Pre-native databases remain readable, but the fallback is
            // deliberately explicit: only a valid native publication gets
            // the root-only startup path.
            load_counters
                .compatibility_materializations
                .fetch_add(1, Ordering::Relaxed);
            let legacy_base = if excision_generation == 0 {
                load_latest_base(&mut client, &database_id, head_basis, &mut cache)?
            } else {
                None
            };
            let (mut database, mut current_hash, durable_base_t) = match legacy_base {
                Some(base) => base,
                None => {
                    let recovered = recover_to(&mut client, &database_id, head_basis, head_hash)?;
                    (recovered.database, recovered.final_hash, 0)
                }
            };
            if database.basis_t() < head_basis {
                apply_tail(
                    &mut client,
                    &database_id,
                    excision_generation,
                    &mut database,
                    &mut current_hash,
                    head_basis,
                )?;
            }
            if current_hash != head_hash {
                return Err(fault(
                    "peer/head-mismatch",
                    "legacy peer open does not reach observed head",
                ));
            }
            let state_hash =
                read_state_hash(&mut client, &database_id, excision_generation, head_basis)?;
            let metadata = Arc::new(MetadataProjection::from_database(&database)?);
            let recent = Arc::new(RecentTier::new(
                &database_id,
                head_basis,
                head_hash,
                Vec::new(),
                metadata.endpoint(),
                recent_limits,
            )?);
            let cell = OnceLock::new();
            let database = Arc::new(database);
            let _ = cell.set(Arc::clone(&database));
            (
                head_basis,
                database.eidx_frontier(),
                head_hash,
                state_hash,
                durable_base_t,
                recent,
                metadata,
                Arc::new(BTreeSet::new()),
                cell,
            )
        };
        // Catalog names are addresses, not identities. Close the initial
        // multi-statement observation window before exposing a live handle;
        // every reconnect repeats this check.
        verify_database_lineage(&mut client, &database_id, &lineage_id)?;
        let root_pins = RootPinManager::connect(connection, &database_id)?;
        let generation_pin = root_pins.acquire_generation(excision_generation)?;
        let root_pin = root_pins.acquire(tree_base.as_deref())?;
        let compatibility = Arc::new(PeerCompatibility {
            value: compatibility,
            segments: Arc::new(Mutex::new(cache)),
        });
        let read = Arc::new(TieredReadCore {
            database_id,
            lineage_id,
            connection: connection.clone(),
            recent_limits,
            load_counters,
            root_pins,
            programs: Arc::new(Mutex::new(crate::postgres::ProgramCache::default())),
            io: Mutex::new(PeerIo { client, tree_cache }),
        });
        Ok(Self {
            core: Arc::new(PeerCore {
                read,
                allow_compatibility,
                recent_limits,
                update: Mutex::new(()),
                reports: Mutex::new(None),
                report_ready: Condvar::new(),
                state_advanced: Condvar::new(),
                observed_basis: Mutex::new(basis_t),
                state: RwLock::new(Arc::new(PeerState {
                    tiered: Arc::new(TieredState {
                        basis_t,
                        eidx_frontier,
                        current_hash,
                        current_state_hash,
                        excision_generation,
                        durable_base_t,
                        tree_base,
                        _root_pin: root_pin,
                        _generation_pin: generation_pin,
                        recent,
                        metadata,
                        avet_unready,
                        generation: 0,
                    }),
                    compatibility,
                })),
            }),
        })
    }

    /// Capture the current immutable native value without SQL or eager recovery.
    pub fn db(&self) -> DatabaseValue {
        self.database_value()
    }

    /// Capture the immutable transaction log at this peer's current endpoint.
    /// Capture performs no I/O and retains the exact generation's read pins.
    pub fn log(&self) -> native_log::LogValue {
        native_log::LogValue::new(TieredSnapshot {
            core: Arc::clone(&self.core.read),
            state: Arc::clone(&self.state().tiered),
        })
    }

    /// Explicit eager kernel/oracle adapter; may read the entire database.
    /// Use `try_db_compatibility` to handle storage errors without panicking.
    pub fn db_compatibility(&self) -> Arc<Database> {
        self.try_db_compatibility()
            .unwrap_or_else(|error| panic!("peer database materialization failed: {error}"))
    }

    pub fn try_db_compatibility(&self) -> Result<Arc<Database>, SemanticError> {
        let state = self.state();
        self.database_for_state(&state)
    }

    pub fn basis_t(&self) -> u64 {
        self.state().basis_t
    }
    pub fn durable_base_t(&self) -> u64 {
        self.state().durable_base_t
    }

    /// Physical index-root coordinate currently adopted by this connection.
    /// It is independent of the logical transaction basis.
    pub fn durable_base_revision(&self) -> u64 {
        self.state()
            .tree_base
            .as_ref()
            .map_or(0, |base| base.manifest.publication_revision)
    }
    pub fn excision_generation(&self) -> u64 {
        self.state().excision_generation
    }
    pub fn generation(&self) -> u64 {
        self.state().generation
    }

    pub fn load_stats(&self) -> PeerLoadStats {
        self.core.read.load_counters.snapshot()
    }

    /// Process-local native manifests that must remain reachable for the live
    /// connection or an older immutable snapshot. Goal 15's SQL GC can use
    /// this seam when it adds leases; Goal 13 deliberately owns no SQL policy.
    pub fn pinned_manifest_hashes(&self) -> Vec<Digest> {
        self.core.read.root_pins.hashes()
    }
    pub fn cache_stats(&self) -> CacheStats {
        let io = lock(&self.core.read.io);
        let state = self.state();
        let segments = lock(&state.compatibility.segments);
        let mut stats = io.tree_cache.stats;
        stats.hits = stats.hits.saturating_add(segments.stats.hits);
        stats.misses = stats.misses.saturating_add(segments.stats.misses);
        stats.evictions = stats.evictions.saturating_add(segments.stats.evictions);
        stats.current_entries = stats.current_entries.saturating_add(segments.entries.len());
        stats.peak_entries = stats.peak_entries.max(stats.current_entries);
        stats
    }

    pub fn snapshot(&self) -> PeerSnapshot {
        self.snapshot_for_state(self.state())
    }

    pub(crate) fn tiered_snapshot(&self) -> TieredSnapshot {
        let state = self.state();
        TieredSnapshot {
            core: Arc::clone(&self.core.read),
            state: Arc::clone(&state.tiered),
        }
    }

    fn snapshot_for_state(&self, state: Arc<PeerState>) -> PeerSnapshot {
        PeerSnapshot {
            native: TieredSnapshot {
                core: Arc::clone(&self.core.read),
                state: Arc::clone(&state.tiered),
            },
            compatibility: Arc::clone(&state.compatibility),
        }
    }

    /// Capture one immutable native database value without constructing the
    /// eager compatibility oracle.
    pub fn database_value(&self) -> DatabaseValue {
        self.tiered_snapshot().database_value()
    }

    /// Stable logical database identity retained when this peer was opened.
    pub fn identity(&self) -> DatabaseIdentity {
        DatabaseIdentity::new(
            self.core.read.database_id.clone(),
            self.core.read.lineage_id.clone(),
        )
    }

    #[cfg(unix)]
    pub(crate) fn open_socket_report(
        &self,
        wire: crate::encoding::WireReport,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        if wire.before.basis_t.checked_add(1) != Some(wire.after.basis_t)
            || wire.before.generation != wire.after.generation
        {
            return Err(fault(
                "transport/report-coordinate",
                "invalid before/after receipt coordinates",
            ));
        }
        let snapshot = self.tiered_snapshot();
        let (before, _) = snapshot.open_exact_sharing_core(
            &self.core.read.database_id,
            wire.before,
            Some(wire.before_manifest),
        )?;
        let (after, _) = snapshot.open_exact_sharing_core(
            &self.core.read.database_id,
            wire.after,
            Some(wire.after_manifest),
        )?;
        Ok(ServiceTransactionReport {
            db_before: before.database_value(),
            db_after: after.database_value(),
            basis_t: wire.after.basis_t,
            tx_hash: wire.after.tx_hash,
            tx_data: wire.tx_data,
            tempids: wire.tempids,
            replayed: wire.replayed,
        })
    }

    pub fn recent_stats(&self) -> crate::recent::RecentStats {
        self.state().recent.stats()
    }

    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.database_value().entity(identifier)
    }

    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value().query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value()
            .query_with_extensions(query, inputs, control, extensions)
    }

    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value().pull(pattern, entity)
    }

    pub fn sync(&self) -> Result<DatabaseValue, SemanticError> {
        self.sync_database_value()
    }

    /// Explicit eager catch-up for oracle/admin callers.
    pub fn sync_compatibility(&self) -> Result<Arc<Database>, SemanticError> {
        match self.sync_once(true) {
            Err(error) if is_postgres_connection_error(&error) => {
                self.reconnect()?;
                self.sync_once(true)
            }
            result => result,
        }
        .and_then(|state| compatibility_value(&state))
    }

    fn sync_once(&self, require_compatibility: bool) -> Result<Arc<PeerState>, SemanticError> {
        self.core.read.root_pins.ensure()?;
        let _update = lock(&self.core.update);
        let mut io = lock(&self.core.read.io);
        self.refresh_after_excision_locked(&mut io)?;
        let (target, _) = read_head(&mut io.client, &self.core.read.database_id)?;
        self.advance_to_locked(&mut io, target, require_compatibility)
    }

    pub fn sync_to(&self, target: u64, timeout: Duration) -> Result<DatabaseValue, SemanticError> {
        self.sync_to_database_value(target, timeout)
    }

    pub fn sync_to_compatibility(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<Arc<Database>, SemanticError> {
        let state = self.wait_for_basis(target, timeout, true)?;
        compatibility_value(&state)
    }

    /// Advance the shared native tree/recent value without constructing the
    /// legacy in-memory `Database`. This is the normal low-footprint sync path.
    pub fn sync_snapshot(&self) -> Result<PeerSnapshot, SemanticError> {
        let state = match self.sync_once(false) {
            Err(error) if is_postgres_connection_error(&error) => {
                self.reconnect()?;
                self.sync_once(false)?
            }
            result => result?,
        };
        Ok(self.snapshot_for_state(state))
    }

    /// Advance and return the ordinary immutable native database value.
    pub fn sync_database_value(&self) -> Result<DatabaseValue, SemanticError> {
        self.sync_snapshot()
            .map(|snapshot| snapshot.database_value())
    }

    pub fn sync_to_snapshot(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<PeerSnapshot, SemanticError> {
        let state = self.wait_for_basis(target, timeout, false)?;
        Ok(self.snapshot_for_state(state))
    }

    /// Wait for a requested logical basis and return its native value without
    /// eager compatibility materialization.
    pub fn sync_to_database_value(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.sync_to_snapshot(target, timeout)
            .map(|snapshot| snapshot.database_value())
    }

    /// Wait for physical indexing, including outstanding AVET projections.
    pub fn sync_index(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.wait_for_frontier(target, timeout, "peer/sync-index-timeout", |_, state| {
            Ok(state
                .tree_base
                .as_ref()
                .is_some_and(|base| base.manifest.index_basis_t >= target))
        })
    }

    /// Wait for schema adoption and readiness of the adopted schema's AVET
    /// projections. This may also wait for newer concurrently adopted schema
    /// work; it never reports readiness from logical basis alone.
    pub fn sync_schema(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.wait_for_frontier(target, timeout, "peer/sync-schema-timeout", |_, state| {
            Ok(state.avet_unready.is_empty())
        })
    }

    /// Wait for every excision request through target and the active
    /// generation's root-last completion marker, without eager recovery.
    pub fn sync_excise(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.wait_for_frontier(target, timeout, "peer/sync-excise-timeout", |peer, state| {
            let generation = sql_basis(state.excision_generation)?;
            if generation > 0 {
                let complete: bool = lock(&peer.core.read.io).client.query_one(
                    "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_completions \
                     WHERE database_id = $1 AND generation = $2)",
                    &[&peer.core.read.database_id, &generation],
                ).map_err(|error| postgres_error("peer/excision-completion", error))?.get(0);
                if !complete { return Ok(false); }
            }
            let value = peer.snapshot_for_state(Arc::clone(state)).database_value().history();
            for datom in value.prefix_cursor(&IndexPrefix::Aevt {
                attribute: crate::DB_EXCISE as u32, entity: None, value: None,
            })? {
                let datom = datom?;
                let request_t = crate::tx_to_t(datom.tx)?;
                if !datom.added || request_t > target { continue; }
                if generation == 0 { return Ok(false); }
                let complete: bool = lock(&peer.core.read.io).client.query_one(
                    "SELECT EXISTS (SELECT 1 FROM atomic_completed_excision_requests \
                     WHERE database_id = $1 AND generation = $2 AND request_t = $3 AND request_entity = $4)",
                    &[&peer.core.read.database_id, &generation, &sql_basis(request_t)?, &sql_basis(datom.entity)?],
                ).map_err(|error| postgres_error("peer/excision-request-completion", error))?.get(0);
                if !complete { return Ok(false); }
            }
            Ok(true)
        })
    }

    fn wait_for_frontier(
        &self,
        target: u64,
        timeout: Duration,
        code: &'static str,
        ready: impl Fn(&Self, &Arc<PeerState>) -> Result<bool, SemanticError>,
    ) -> Result<DatabaseValue, SemanticError> {
        let deadline = Instant::now() + timeout;
        loop {
            match self
                .sync_database_value()
                .and_then(|_| self.refresh_index())
                .and_then(|_| {
                    let state = self.state();
                    if state.basis_t >= target && ready(self, &state)? {
                        Ok(Some(self.snapshot_for_state(state).database_value()))
                    } else {
                        Ok(None)
                    }
                }) {
                Ok(Some(value)) => return Ok(value),
                Ok(None) => {}
                // COW activation precedes root-last native publication. Keep
                // the old value intact and wait for that real prerequisite.
                Err(error) if error.code == "peer/native-index-required" => {}
                Err(error) if is_postgres_connection_error(&error) => {}
                Err(error) => return Err(error),
            }
            let remaining = deadline.saturating_duration_since(Instant::now());
            if remaining.is_zero() {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    code,
                    "timed out waiting for the requested native frontier",
                )
                .detail("target_t", target.to_string()));
            }
            std::thread::sleep(remaining.min(Duration::from_millis(10)));
        }
    }

    fn wait_for_basis(
        &self,
        target: u64,
        timeout: Duration,
        require_compatibility: bool,
    ) -> Result<Arc<PeerState>, SemanticError> {
        let deadline = Instant::now() + timeout;
        loop {
            if let Err(error) = self.core.read.root_pins.ensure() {
                if !is_postgres_connection_error(&error) {
                    return Err(error);
                }
                if !self.reconnect_before(deadline)? {
                    return Err(sync_timeout(target));
                }
                continue;
            }
            // Never retain the updater or I/O/cache mutex across sleep. Lazy
            // readers can continue loading nodes while a waiter observes an
            // unchanged authoritative head.
            let observed_head = {
                let mut io = lock(&self.core.read.io);
                read_head(&mut io.client, &self.core.read.database_id).map(|head| head.0)
            };
            let head = match observed_head {
                Ok(head) => head,
                Err(error) if is_postgres_connection_error(&error) => {
                    if !self.reconnect_before(deadline)? {
                        return Err(sync_timeout(target));
                    }
                    continue;
                }
                Err(error) => return Err(error),
            };
            if head >= target {
                let attempt = {
                    let _update = lock(&self.core.update);
                    let mut io = lock(&self.core.read.io);
                    self.refresh_after_excision_locked(&mut io).and_then(|_| {
                        let (rechecked, _) =
                            read_head(&mut io.client, &self.core.read.database_id)?;
                        if rechecked >= target {
                            self.advance_to_locked(&mut io, target, require_compatibility)
                                .map(Some)
                        } else {
                            Ok(None)
                        }
                    })
                };
                match attempt {
                    Ok(Some(state)) => return Ok(state),
                    Ok(None) => {}
                    Err(error) if is_postgres_connection_error(&error) => {
                        if !self.reconnect_before(deadline)? {
                            return Err(sync_timeout(target));
                        }
                        continue;
                    }
                    Err(error) => return Err(error),
                }
            }
            if Instant::now() >= deadline {
                return Err(sync_timeout(target));
            }
            std::thread::sleep(Duration::from_millis(10));
        }
    }

    /// Reborrow a checked PostgreSQL connection while retaining immutable
    /// peer values, pinned roots, observation state, and bounded caches.
    pub fn reconnect(&self) -> Result<(), SemanticError> {
        self.core.read.root_pins.ensure()?;
        let mut io = lock(&self.core.read.io);
        reconnect_peer_io(&self.core.read, &mut io)
    }

    fn reconnect_before(&self, deadline: Instant) -> Result<bool, SemanticError> {
        loop {
            let remaining = deadline.saturating_duration_since(Instant::now());
            if remaining.is_zero() {
                return Ok(false);
            }
            let result = {
                let mut io = lock(&self.core.read.io);
                reconnect_peer_io_with_timeout(&self.core.read, &mut io, remaining)
            };
            match result {
                Ok(()) => return Ok(true),
                Err(error) if is_postgres_connection_error(&error) => {
                    std::thread::sleep(Duration::from_millis(10));
                }
                Err(error) => return Err(error),
            }
        }
    }

    /// Enable this connection's transaction-report channel. Reports begin
    /// with the next successfully adopted transaction; opening a peer never
    /// manufactures reports for the already-observed log prefix.
    pub fn enable_tx_reports(&self) -> bool {
        let _update = lock(&self.core.update);
        let mut slot = lock(&self.core.reports);
        if slot.is_some() {
            return false;
        }
        *slot = Some(VecDeque::new());
        true
    }

    /// Remove the optional report channel and release every unconsumed report.
    pub fn remove_tx_reports(&self) -> bool {
        let _update = lock(&self.core.update);
        let removed = lock(&self.core.reports).take().is_some();
        self.core.report_ready.notify_all();
        removed
    }

    pub fn tx_reports_enabled(&self) -> bool {
        lock(&self.core.reports).is_some()
    }

    pub fn take_tx_reports(&self) -> Vec<ServiceTransactionReport> {
        // Share the updater lock so draining cannot race successor adoption.
        // Unlike the old implementation, draining reports does not publish a
        // counterfeit new database generation.
        let _update = lock(&self.core.update);
        let mut slot = lock(&self.core.reports);
        let Some(reports) = slot.as_mut() else {
            return Vec::new();
        };
        reports.drain(..).collect()
    }

    pub fn try_next_tx_report(&self) -> Option<ServiceTransactionReport> {
        lock(&self.core.reports)
            .as_mut()
            .and_then(VecDeque::pop_front)
    }

    pub fn next_tx_report(
        &self,
        timeout: Duration,
    ) -> Result<Option<ServiceTransactionReport>, SemanticError> {
        let deadline = Instant::now() + timeout;
        let mut slot = lock(&self.core.reports);
        loop {
            let Some(queue) = slot.as_mut() else {
                return Ok(None);
            };
            if let Some(report) = queue.pop_front() {
                return Ok(Some(report));
            }
            let remaining = deadline.saturating_duration_since(Instant::now());
            if remaining.is_zero() {
                return Ok(None);
            }
            let (next, waited) = self
                .core
                .report_ready
                .wait_timeout(slot, remaining)
                .unwrap_or_else(std::sync::PoisonError::into_inner);
            slot = next;
            if waited.timed_out() {
                return Ok(None);
            }
        }
    }

    /// Install one exact committed native successor supplied by the fenced
    /// service. This operation performs no PostgreSQL I/O: once the service
    /// has acknowledged COMMIT, local observation cannot turn that success
    /// into an ordinary storage error.
    pub(crate) fn adopt_committed_report(
        &self,
        report: &ServiceTransactionReport,
    ) -> Result<DatabaseValue, SemanticError> {
        let before = report.db_before.native_tiered_snapshot().ok_or_else(|| {
            fault(
                "peer/report-before-not-native",
                "transaction report db-before is not one direct native value",
            )
        })?;
        let after = report.db_after.native_tiered_snapshot().ok_or_else(|| {
            fault(
                "peer/report-after-not-native",
                "transaction report db-after is not one direct native value",
            )
        })?;
        if before.core.database_id != self.core.read.database_id
            || after.core.database_id != self.core.read.database_id
            || before.core.lineage_id != self.core.read.lineage_id
            || after.core.lineage_id != self.core.read.lineage_id
        {
            return Err(fault(
                "peer/report-database-identity",
                "transaction report belongs to a different database lineage",
            ));
        }
        if report.basis_t == 0
            || before.basis_t().checked_add(1) != Some(report.basis_t)
            || after.basis_t() != report.basis_t
            || after.transaction_hash() != report.tx_hash
        {
            return Err(fault(
                "peer/report-coordinate",
                "transaction report before/after values do not form its claimed successor",
            ));
        }

        let _update = lock(&self.core.update);
        let current = self.state();
        if current.basis_t > report.basis_t {
            return Ok(self.database_value());
        }
        if current.basis_t == report.basis_t {
            if current.current_hash != report.tx_hash {
                return Err(fault(
                    "peer/report-fork",
                    "transaction report conflicts with the already adopted basis",
                ));
            }
            return Ok(self.database_value());
        }
        if current.basis_t != before.basis_t() || current.current_hash != before.transaction_hash()
        {
            return Err(fault(
                "peer/report-gap",
                "transaction reports must be adopted in contiguous source order",
            ));
        }

        let mut tiered = (*after.state).clone();
        tiered.generation = current.generation.saturating_add(1);
        let published = Arc::new(PeerState {
            tiered: Arc::new(tiered),
            compatibility: Arc::new(PeerCompatibility {
                value: OnceLock::new(),
                segments: Arc::clone(&current.compatibility.segments),
            }),
        });
        self.publish_arc(Arc::clone(&published));
        if !report.replayed
            && let Some(queue) = lock(&self.core.reports).as_mut()
        {
            queue.push_back(report.clone());
            self.core.report_ready.notify_one();
        }
        Ok(TieredSnapshot {
            core: Arc::clone(&self.core.read),
            state: Arc::clone(&published.tiered),
        }
        .database_value())
    }

    /// Adopt a newly published physical base without changing the connection's
    /// logical basis. This is the native counterpart of recovered
    /// `notify-index`: it is optional for correctness and leaves old `Arc`
    /// snapshots untouched.
    pub fn refresh_index(&self) -> Result<bool, SemanticError> {
        match self.refresh_index_once() {
            Err(error) if is_postgres_connection_error(&error) => {
                self.reconnect()?;
                self.refresh_index_once()
            }
            result => result,
        }
    }

    fn refresh_index_once(&self) -> Result<bool, SemanticError> {
        self.core.read.root_pins.ensure()?;
        let _update = lock(&self.core.update);
        let mut io = lock(&self.core.read.io);
        self.refresh_after_excision_locked(&mut io)?;
        let state = self.state();
        let through = state.basis_t;
        let observed_revision =
            current_tree_publication_revision(&mut io.client, &self.core.read.database_id)?;
        let adopted_revision = state
            .tree_base
            .as_ref()
            .map_or(0, |base| base.manifest.publication_revision);
        if observed_revision <= adopted_revision {
            return Ok(false);
        }
        let loaded = {
            let PeerIo {
                client, tree_cache, ..
            } = &mut *io;
            load_latest_tree_base(
                client,
                &self.core.read.database_id,
                through,
                state.excision_generation,
                &self.core.read.load_counters,
                tree_cache,
            )?
        };
        let Some(tree_base) = loaded.map(Arc::new) else {
            return Ok(false);
        };
        if tree_base.manifest.publication_revision <= adopted_revision {
            return Ok(false);
        }
        if tree_base.manifest.basis_t < state.durable_base_t {
            return Err(fault(
                "peer/tree-basis-regressed",
                "newer physical tree publication regressed the adopted logical basis",
            ));
        }
        let tail = read_authenticated_tail(
            &mut io.client,
            &self.core.read.database_id,
            state.excision_generation,
            TailBase {
                basis_t: tree_base.manifest.basis_t,
                tx_hash: tree_base.manifest.tx_hash,
                state_hash: tree_base.manifest.state_hash,
                eidx_frontier: tree_base.manifest.eidx_frontier,
            },
            through,
            None,
        )?;
        if tail.end_hash != state.current_hash
            || tail.end_state_hash != state.current_state_hash
            || tail.eidx_frontier != state.eidx_frontier
        {
            return Err(fault(
                "peer/index-adoption-divergence",
                "new native tree plus its authenticated tail diverges from the current endpoint",
            ));
        }
        let base_metadata = Arc::clone(&tree_base.metadata);
        let (metadata, avet_unready) = {
            let PeerIo {
                client, tree_cache, ..
            } = &mut *io;
            apply_metadata_and_avet_readiness(
                &base_metadata,
                &manifest_avet_unready(&tree_base.manifest),
                &tail.transactions,
                |attribute| {
                    tree_base_has_attribute_history(
                        client,
                        &tree_base,
                        attribute,
                        &self.core.read.load_counters,
                        tree_cache,
                    )
                },
            )?
        };
        let metadata = Arc::new(metadata);
        let avet_unready = Arc::new(avet_unready);
        let recent = Arc::new(RecentTier::new_authenticated_existing(
            &self.core.read.database_id,
            tree_base.manifest.basis_t,
            tree_base.manifest.tx_hash,
            tail.transaction_hashes.into_iter().zip(tail.transactions),
            metadata.endpoint(),
            self.core.recent_limits,
        )?);
        let mut successor = (*state.tiered).clone();
        successor.durable_base_t = tree_base.manifest.basis_t;
        successor._root_pin = self.core.read.root_pins.acquire(Some(&tree_base))?;
        successor.tree_base = Some(tree_base);
        successor.recent = recent;
        successor.metadata = metadata;
        successor.avet_unready = avet_unready;
        successor.generation = successor.generation.saturating_add(1);
        self.publish(PeerState {
            tiered: Arc::new(successor),
            compatibility: Arc::clone(&state.compatibility),
        });
        Ok(true)
    }

    fn advance_to_locked(
        &self,
        io: &mut PeerIo,
        target: u64,
        require_compatibility: bool,
    ) -> Result<Arc<PeerState>, SemanticError> {
        let state = self.state();
        if target <= state.basis_t {
            if require_compatibility && state.compatibility.value.get().is_none() {
                let mut segments = lock(&state.compatibility.segments);
                let database = counted_compatibility_materialization_with_io(
                    &self.core.read,
                    io,
                    &mut segments,
                    &state.tiered,
                )?;
                let _ = state.compatibility.value.set(database);
            }
            return Ok(state);
        }
        // A lagging peer with no observer queue need not replay the entire
        // indexed prefix. Adopt a newer authenticated root and only its bounded
        // tail, just as a fresh native open does. Opted-in reports still require
        // each intermediate transaction; they may intentionally retain history.
        if !require_compatibility
            && lock(&self.core.reports).is_none()
            && let Some(published) = self.advance_from_newer_base_locked(io, &state, target)?
        {
            return Ok(published);
        }
        let tail = read_authenticated_tail(
            &mut io.client,
            &self.core.read.database_id,
            state.excision_generation,
            TailBase {
                basis_t: state.basis_t,
                tx_hash: state.current_hash,
                state_hash: state.current_state_hash,
                eidx_frontier: state.eidx_frontier,
            },
            target,
            None,
        )?;
        let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
            &state.metadata,
            &state.avet_unready,
            &tail.transactions,
            |attribute| {
                peer_state_has_attribute_history_with_io(&self.core.read, io, &state, attribute)
            },
        )?;
        let metadata = Arc::new(metadata);
        let recent = state.recent.extend_authenticated_existing(
            tail.transaction_hashes
                .iter()
                .copied()
                .zip(tail.transactions.iter().cloned()),
            metadata.endpoint(),
        )?;
        let mut successor = (*state.tiered).clone();
        successor.basis_t = target;
        successor.eidx_frontier = tail.eidx_frontier;
        successor.current_hash = tail.end_hash;
        successor.current_state_hash = tail.end_state_hash;
        successor.recent = Arc::new(recent);
        successor.metadata = metadata;
        successor.avet_unready = Arc::new(avet_unready);
        successor.generation = successor.generation.saturating_add(1);
        let compatibility = Arc::new(PeerCompatibility {
            value: OnceLock::new(),
            segments: Arc::clone(&state.compatibility.segments),
        });
        if let Some(cached) = state.compatibility.value.get() {
            let cached = Arc::clone(cached);
            let mut database = (*cached).clone();
            for transaction in &tail.transactions {
                database = database.apply_committed(transaction)?;
            }
            verify_materialized_endpoint(&database, &successor)?;
            let _ = compatibility.value.set(Arc::new(database));
        } else if require_compatibility {
            let mut segments = lock(&state.compatibility.segments);
            let database = counted_compatibility_materialization_with_io(
                &self.core.read,
                io,
                &mut segments,
                &successor,
            )?;
            let _ = compatibility.value.set(database);
        }
        let published = Arc::new(PeerState {
            tiered: Arc::new(successor),
            compatibility,
        });
        // Prepare all intermediate values before publishing anything. A bad
        // tail must change neither the live head nor the optional report queue.
        let reports = if lock(&self.core.reports).is_some() {
            self.tail_reports_with_io(io, &state, &tail)?
        } else {
            Vec::new()
        };
        self.publish_arc(Arc::clone(&published));
        if let Some(queue) = lock(&self.core.reports).as_mut() {
            queue.extend(reports);
            self.core.report_ready.notify_all();
        }
        Ok(published)
    }

    fn advance_from_newer_base_locked(
        &self,
        io: &mut PeerIo,
        state: &Arc<PeerState>,
        target: u64,
    ) -> Result<Option<Arc<PeerState>>, SemanticError> {
        let PeerIo { client, tree_cache } = io;
        let adopted_revision = state
            .tree_base
            .as_ref()
            .map_or(0, |base| base.manifest.publication_revision);
        if current_tree_publication_revision(client, &self.core.read.database_id)?
            <= adopted_revision
        {
            return Ok(None);
        }
        let Some(base) = load_latest_tree_base(
            client,
            &self.core.read.database_id,
            target,
            state.excision_generation,
            &self.core.read.load_counters,
            tree_cache,
        )?
        else {
            return Ok(None);
        };
        if base.manifest.basis_t <= state.basis_t {
            return Ok(None);
        }
        let tail = read_authenticated_tail(
            client,
            &self.core.read.database_id,
            state.excision_generation,
            TailBase {
                basis_t: base.manifest.basis_t,
                tx_hash: base.manifest.tx_hash,
                state_hash: base.manifest.state_hash,
                eidx_frontier: base.manifest.eidx_frontier,
            },
            target,
            None,
        )?;
        let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
            &base.metadata,
            &manifest_avet_unready(&base.manifest),
            &tail.transactions,
            |attribute| {
                tree_base_has_attribute_history(
                    client,
                    &base,
                    attribute,
                    &self.core.read.load_counters,
                    tree_cache,
                )
            },
        )?;
        let recent = RecentTier::new_authenticated_existing(
            &self.core.read.database_id,
            base.manifest.basis_t,
            base.manifest.tx_hash,
            tail.transaction_hashes.into_iter().zip(tail.transactions),
            metadata.endpoint(),
            self.core.recent_limits,
        )?;
        let mut successor = (*state.tiered).clone();
        successor.basis_t = target;
        successor.eidx_frontier = tail.eidx_frontier;
        successor.current_hash = tail.end_hash;
        successor.current_state_hash = tail.end_state_hash;
        successor.durable_base_t = base.manifest.basis_t;
        successor._root_pin = self.core.read.root_pins.acquire(Some(&base))?;
        successor.tree_base = Some(Arc::new(base));
        successor.recent = Arc::new(recent);
        successor.metadata = Arc::new(metadata);
        successor.avet_unready = Arc::new(avet_unready);
        successor.generation = successor.generation.saturating_add(1);
        let published = Arc::new(PeerState {
            tiered: Arc::new(successor),
            compatibility: Arc::new(PeerCompatibility {
                value: OnceLock::new(),
                segments: Arc::clone(&state.compatibility.segments),
            }),
        });
        self.publish_arc(Arc::clone(&published));
        Ok(Some(published))
    }

    fn tail_reports_with_io(
        &self,
        io: &mut PeerIo,
        initial: &PeerState,
        tail: &AuthenticatedTail,
    ) -> Result<Vec<ServiceTransactionReport>, SemanticError> {
        let mut before = initial.clone();
        let mut reports = Vec::with_capacity(tail.transactions.len());
        for ((transaction, tx_hash), state_hash) in tail
            .transactions
            .iter()
            .zip(&tail.transaction_hashes)
            .zip(&tail.state_hashes)
        {
            let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
                &before.metadata,
                &before.avet_unready,
                std::slice::from_ref(transaction),
                |attribute| {
                    peer_state_has_attribute_history_with_io(
                        &self.core.read,
                        io,
                        &before,
                        attribute,
                    )
                },
            )?;
            let recent = before.recent.extend_authenticated_existing(
                std::iter::once((*tx_hash, transaction.clone())),
                metadata.endpoint(),
            )?;
            let mut after = (*before.tiered).clone();
            after.basis_t = transaction.basis_t;
            after.eidx_frontier = transaction.eidx_frontier;
            after.current_hash = *tx_hash;
            after.current_state_hash = *state_hash;
            after.recent = Arc::new(recent);
            after.metadata = Arc::new(metadata);
            after.avet_unready = Arc::new(avet_unready);
            after.generation = after.generation.saturating_add(1);
            let after = Arc::new(after);
            let value = |state| {
                TieredSnapshot {
                    core: Arc::clone(&self.core.read),
                    state,
                }
                .database_value()
            };
            // ATLC deliberately excludes receipt names. Reconstruct them only
            // for opted-in observers, from the current generation's ordinary
            // request receipt (retired/excised receipts do not retain names).
            let tempids = if initial.excision_generation == 0 {
                transaction.tempids.clone()
            } else {
                io.client
                    .query(
                        "SELECT t.tempid_name, t.entity_id \
                     FROM atomic_generation_requests r \
                     JOIN atomic_generation_request_tempids t \
                     USING (database_id, generation, request_key_hash) \
                     WHERE r.database_id = $1 AND r.generation = $2 AND r.basis_t = $3 \
                     ORDER BY t.tempid_name",
                        &[
                            &self.core.read.database_id,
                            &sql_basis(initial.excision_generation)?,
                            &sql_basis(transaction.basis_t)?,
                        ],
                    )
                    .map_err(|error| postgres_error("peer/report-tempids", error))?
                    .into_iter()
                    .map(|row| {
                        Ok((
                            row.get::<_, String>(0),
                            pg_basis(row.get(1), "report tempid entity")?,
                        ))
                    })
                    .collect::<Result<BTreeMap<_, _>, SemanticError>>()?
            };
            reports.push(ServiceTransactionReport {
                db_before: value(Arc::clone(&before.tiered)),
                db_after: value(Arc::clone(&after)),
                basis_t: transaction.basis_t,
                tx_hash: *tx_hash,
                tx_data: transaction.tx_data.clone(),
                tempids,
                replayed: false,
            });
            before.tiered = after;
        }
        Ok(reports)
    }

    fn database_for_state(&self, state: &Arc<PeerState>) -> Result<Arc<Database>, SemanticError> {
        database_for_compatibility(
            &TieredSnapshot {
                core: Arc::clone(&self.core.read),
                state: Arc::clone(&state.tiered),
            },
            &state.compatibility,
        )
    }

    fn refresh_after_excision_locked(&self, io: &mut PeerIo) -> Result<bool, SemanticError> {
        let state = self.state();
        let generation = read_excision_generation(&mut io.client, &self.core.read.database_id)?;
        if generation == state.excision_generation {
            return Ok(false);
        }
        if generation < state.excision_generation {
            return Err(fault(
                "peer/excision-generation-regressed",
                "database excision generation moved backwards",
            ));
        }
        let (basis, hash) = read_head(&mut io.client, &self.core.read.database_id)?;
        // A physical generation change is not a transaction. Before adopting
        // it, opted-in observers must receive the unseen transactions from the
        // retained source generation, with their original before/after values.
        // Stage everything first: an unavailable ancestor must not silently
        // advance the connection past a hole in its report stream.
        let mut reports = Vec::new();
        if lock(&self.core.reports).is_some() {
            let mut ancestry = Vec::new();
            let mut next = generation;
            while next != state.excision_generation {
                let row = io
                    .client
                    .query_opt(
                        "SELECT prior_generation, prior_basis_t, basis_t, head_hash \
                     FROM atomic_log_generation_activations \
                     WHERE database_id = $1 AND generation = $2",
                        &[&self.core.read.database_id, &sql_basis(next)?],
                    )
                    .map_err(|error| postgres_error("peer/report-generation", error))?
                    .ok_or_else(|| {
                        fault(
                            "peer/report-generation-gap",
                            "report generation ancestry is unavailable",
                        )
                    })?;
                let prior = pg_basis(row.get(0), "prior generation")?;
                if prior >= next || prior < state.excision_generation {
                    return Err(fault(
                        "peer/report-generation-gap",
                        "report generation ancestry does not reach the captured value",
                    ));
                }
                ancestry.push((
                    next,
                    pg_basis(row.get(1), "prior basis")?,
                    pg_basis(row.get(2), "activation basis")?,
                    digest(row.get(3), "activation head")?,
                ));
                next = prior;
            }
            let mut before = (*state).clone();
            for (next, prior_basis, activated_basis, activated_hash) in ancestry.into_iter().rev() {
                if prior_basis < before.basis_t {
                    return Err(fault(
                        "peer/report-generation-gap",
                        "generation source predates the captured value",
                    ));
                }
                let tail = read_authenticated_tail(
                    &mut io.client,
                    &self.core.read.database_id,
                    before.excision_generation,
                    TailBase {
                        basis_t: before.basis_t,
                        tx_hash: before.current_hash,
                        state_hash: before.current_state_hash,
                        eidx_frontier: before.eidx_frontier,
                    },
                    prior_basis,
                    None,
                )?;
                reports.extend(self.tail_reports_with_io(io, &before, &tail)?);
                before = self.build_generation_state_locked(
                    io,
                    &before,
                    next,
                    activated_basis,
                    activated_hash,
                )?;
            }
            let tail = read_authenticated_tail(
                &mut io.client,
                &self.core.read.database_id,
                generation,
                TailBase {
                    basis_t: before.basis_t,
                    tx_hash: before.current_hash,
                    state_hash: before.current_state_hash,
                    eidx_frontier: before.eidx_frontier,
                },
                basis,
                None,
            )?;
            reports.extend(self.tail_reports_with_io(io, &before, &tail)?);
        }
        let successor = self.build_generation_state_locked(io, &state, generation, basis, hash)?;
        self.publish(successor);
        if let Some(queue) = lock(&self.core.reports).as_mut() {
            queue.extend(reports);
            self.core.report_ready.notify_all();
        }
        Ok(true)
    }

    fn build_generation_state_locked(
        &self,
        io: &mut PeerIo,
        state: &PeerState,
        generation: u64,
        basis: u64,
        hash: Digest,
    ) -> Result<PeerState, SemanticError> {
        {
            let mut segments = lock(&state.compatibility.segments);
            let capacity = segments.capacity;
            *segments = SegmentCache::new(capacity);
        }
        let tree_entries = io.tree_cache.max_entries;
        let tree_bytes = io.tree_cache.max_bytes;
        io.tree_cache = TreeNodeCache::new(tree_entries, tree_bytes);
        let tree_base = {
            let PeerIo {
                client, tree_cache, ..
            } = &mut *io;
            load_latest_tree_base(
                client,
                &self.core.read.database_id,
                basis,
                generation,
                &self.core.read.load_counters,
                tree_cache,
            )?
        }
        .map(Arc::new);
        let successor = if let Some(base) = tree_base.as_deref() {
            let base_metadata = Arc::clone(&base.metadata);
            let tail = read_authenticated_tail(
                &mut io.client,
                &self.core.read.database_id,
                generation,
                TailBase {
                    basis_t: base.manifest.basis_t,
                    tx_hash: base.manifest.tx_hash,
                    state_hash: base.manifest.state_hash,
                    eidx_frontier: base.manifest.eidx_frontier,
                },
                basis,
                None,
            )?;
            if tail.end_hash != hash {
                return Err(fault(
                    "peer/excision-head-mismatch",
                    "post-excision native base and tail do not reach the new head",
                ));
            }
            let (metadata, avet_unready) = {
                let PeerIo {
                    client, tree_cache, ..
                } = &mut *io;
                apply_metadata_and_avet_readiness(
                    &base_metadata,
                    &manifest_avet_unready(&base.manifest),
                    &tail.transactions,
                    |attribute| {
                        tree_base_has_attribute_history(
                            client,
                            base,
                            attribute,
                            &self.core.read.load_counters,
                            tree_cache,
                        )
                    },
                )?
            };
            let metadata = Arc::new(metadata);
            let avet_unready = Arc::new(avet_unready);
            let recent = Arc::new(RecentTier::new_authenticated_existing(
                &self.core.read.database_id,
                base.manifest.basis_t,
                base.manifest.tx_hash,
                tail.transaction_hashes.into_iter().zip(tail.transactions),
                metadata.endpoint(),
                self.core.recent_limits,
            )?);
            let root_pin = self.core.read.root_pins.acquire(Some(base))?;
            let generation_pin = self.core.read.root_pins.acquire_generation(generation)?;
            PeerState {
                tiered: Arc::new(TieredState {
                    basis_t: basis,
                    eidx_frontier: tail.eidx_frontier,
                    current_hash: hash,
                    current_state_hash: tail.end_state_hash,
                    excision_generation: generation,
                    durable_base_t: base.manifest.basis_t,
                    tree_base,
                    _root_pin: root_pin,
                    _generation_pin: generation_pin,
                    recent,
                    metadata,
                    avet_unready,
                    generation: state.generation.saturating_add(1),
                }),
                compatibility: Arc::new(PeerCompatibility {
                    value: OnceLock::new(),
                    segments: Arc::clone(&state.compatibility.segments),
                }),
            }
        } else {
            if !self.core.allow_compatibility {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "peer/native-index-required",
                    "new log generation has no usable native index; administrative consolidation is required",
                ));
            }
            self.core
                .read
                .load_counters
                .compatibility_materializations
                .fetch_add(1, Ordering::Relaxed);
            let recovered =
                recover_to(&mut io.client, &self.core.read.database_id, basis, hash)?.database;
            let metadata = Arc::new(MetadataProjection::from_database(&recovered)?);
            let recent = Arc::new(RecentTier::new(
                &self.core.read.database_id,
                basis,
                hash,
                Vec::new(),
                metadata.endpoint(),
                self.core.recent_limits,
            )?);
            let compatibility = OnceLock::new();
            let recovered = Arc::new(recovered);
            let _ = compatibility.set(Arc::clone(&recovered));
            let generation_pin = self.core.read.root_pins.acquire_generation(generation)?;
            PeerState {
                tiered: Arc::new(TieredState {
                    basis_t: basis,
                    eidx_frontier: recovered.eidx_frontier(),
                    current_hash: hash,
                    current_state_hash: read_state_hash(
                        &mut io.client,
                        &self.core.read.database_id,
                        generation,
                        basis,
                    )?,
                    excision_generation: generation,
                    durable_base_t: 0,
                    tree_base: None,
                    _root_pin: None,
                    _generation_pin: generation_pin,
                    recent,
                    metadata,
                    avet_unready: Arc::new(BTreeSet::new()),
                    generation: state.generation.saturating_add(1),
                }),
                compatibility: Arc::new(PeerCompatibility {
                    value: compatibility,
                    segments: Arc::clone(&state.compatibility.segments),
                }),
            }
        };
        Ok(successor)
    }

    fn state(&self) -> Arc<PeerState> {
        self.core
            .state
            .read()
            .unwrap_or_else(std::sync::PoisonError::into_inner)
            .clone()
    }

    fn publish(&self, state: PeerState) {
        self.publish_arc(Arc::new(state));
    }

    fn publish_arc(&self, state: Arc<PeerState>) {
        let mut observed = lock(&self.core.observed_basis);
        *observed = state.basis_t;
        *self
            .core
            .state
            .write()
            .unwrap_or_else(std::sync::PoisonError::into_inner) = state;
        self.core.state_advanced.notify_all();
    }

    /// Local observation only: no I/O/update lock can stretch this wait past
    /// a stalled SQL operation. A committed result remains known if it expires.
    pub(crate) fn wait_for_local_basis(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<(), SemanticError> {
        let deadline = Instant::now()
            .checked_add(timeout)
            .ok_or_else(|| sync_timeout(target))?;
        let mut observed = lock(&self.core.observed_basis);
        while *observed < target {
            let remaining = deadline.saturating_duration_since(Instant::now());
            if remaining.is_zero() {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "connection/observation-timeout",
                    "transaction committed but local observation has not caught up",
                )
                .detail("basis_t", target.to_string()));
            }
            observed = self
                .core
                .state_advanced
                .wait_timeout(observed, remaining)
                .unwrap_or_else(std::sync::PoisonError::into_inner)
                .0;
        }
        Ok(())
    }
}

/// Concrete immutable native value shared by peers, exact read APIs, and the
/// bounded writer. It retains only read I/O/cache resources and one tiered
/// endpoint; it cannot coordinate a live peer or materialize an eager value.
#[derive(Clone)]
pub(crate) struct TieredSnapshot {
    core: Arc<TieredReadCore>,
    state: Arc<TieredState>,
}

/// Public peer snapshot. Native information is held by `TieredSnapshot`, while
/// the eager oracle cache remains an explicit compatibility-only attachment.
#[derive(Clone)]
pub struct PeerSnapshot {
    native: TieredSnapshot,
    compatibility: Arc<PeerCompatibility>,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct PeerCursorStats {
    pub tree: TreeReadStats,
    pub recent: RecentCursorStats,
}

/// Lazy ordered merge of one immutable durable tree and its authenticated
/// recent tier. The cursor owns the snapshot and every currently visited
/// node, so connection advancement and cache eviction cannot change it.
pub struct PeerIndexCursor {
    durable: DurableTreeCursor,
    recent: RecentCursor,
    history: bool,
    order: IndexOrder,
    reverse: bool,
    durable_next: Option<Datom>,
    recent_next: Option<Datom>,
    failed: bool,
    work_recorded: bool,
}

struct DurableTreeCursor {
    snapshot: TieredSnapshot,
    root: Arc<RootNode>,
    history: bool,
    order: IndexOrder,
    start: Option<Datom>,
    end: Option<Datom>,
    prefix: Option<IndexPrefix>,
    forward_boundary: Option<TreeBoundary>,
    reverse_boundary: Option<TreeBoundary>,
    directory_index: usize,
    leaf_index: usize,
    directory: Option<LoadedDirectory>,
    leaf: Option<LoadedLeaf>,
    datom_index: usize,
    stats: TreeReadStats,
    exhausted: bool,
}

/// One normalized raw seek boundary plus the prefix form needed to compare
/// sparse persistent-tree routing keys. A full E/A/V/T boundary compares a
/// routing key's T only after its logical three-component prefix; `added` and
/// stored-value ties intentionally remain outside the public boundary.
#[derive(Clone, Debug)]
struct TreeBoundary {
    normalized: NormalizedIndexBoundary,
    routing_prefix: Option<IndexPrefix>,
    tx: Option<u64>,
}

impl TreeBoundary {
    fn new(normalized: NormalizedIndexBoundary) -> Self {
        let (routing_prefix, tx) = match &normalized {
            NormalizedIndexBoundary::Eavt(components) => match components {
                IndexComponents::Empty => (None, None),
                IndexComponents::One(e) => (
                    Some(IndexPrefix::Eavt {
                        entity: *e,
                        attribute: None,
                        value: None,
                    }),
                    None,
                ),
                IndexComponents::Two(e, a) => (
                    Some(IndexPrefix::Eavt {
                        entity: *e,
                        attribute: Some(*a),
                        value: None,
                    }),
                    None,
                ),
                IndexComponents::Three(e, a, v) => (
                    Some(IndexPrefix::Eavt {
                        entity: *e,
                        attribute: Some(*a),
                        value: Some(v.clone()),
                    }),
                    None,
                ),
                IndexComponents::Four(e, a, v, t) => (
                    Some(IndexPrefix::Eavt {
                        entity: *e,
                        attribute: Some(*a),
                        value: Some(v.clone()),
                    }),
                    Some(*t),
                ),
            },
            NormalizedIndexBoundary::Aevt(components) => match components {
                IndexComponents::Empty => (None, None),
                IndexComponents::One(a) => (
                    Some(IndexPrefix::Aevt {
                        attribute: *a,
                        entity: None,
                        value: None,
                    }),
                    None,
                ),
                IndexComponents::Two(a, e) => (
                    Some(IndexPrefix::Aevt {
                        attribute: *a,
                        entity: Some(*e),
                        value: None,
                    }),
                    None,
                ),
                IndexComponents::Three(a, e, v) => (
                    Some(IndexPrefix::Aevt {
                        attribute: *a,
                        entity: Some(*e),
                        value: Some(v.clone()),
                    }),
                    None,
                ),
                IndexComponents::Four(a, e, v, t) => (
                    Some(IndexPrefix::Aevt {
                        attribute: *a,
                        entity: Some(*e),
                        value: Some(v.clone()),
                    }),
                    Some(*t),
                ),
            },
            NormalizedIndexBoundary::Avet(components) => match components {
                IndexComponents::Empty => (None, None),
                IndexComponents::One(a) => (
                    Some(IndexPrefix::Avet {
                        attribute: *a,
                        value: None,
                        entity: None,
                    }),
                    None,
                ),
                IndexComponents::Two(a, v) => (
                    Some(IndexPrefix::Avet {
                        attribute: *a,
                        value: Some(v.clone()),
                        entity: None,
                    }),
                    None,
                ),
                IndexComponents::Three(a, v, e) => (
                    Some(IndexPrefix::Avet {
                        attribute: *a,
                        value: Some(v.clone()),
                        entity: Some(*e),
                    }),
                    None,
                ),
                IndexComponents::Four(a, v, e, t) => (
                    Some(IndexPrefix::Avet {
                        attribute: *a,
                        value: Some(v.clone()),
                        entity: Some(*e),
                    }),
                    Some(*t),
                ),
            },
            NormalizedIndexBoundary::Vaet(components) => match components {
                IndexComponents::Empty => (None, None),
                IndexComponents::One(v) => (
                    Some(IndexPrefix::Vaet {
                        value: v.clone(),
                        attribute: None,
                        entity: None,
                    }),
                    None,
                ),
                IndexComponents::Two(v, a) => (
                    Some(IndexPrefix::Vaet {
                        value: v.clone(),
                        attribute: Some(*a),
                        entity: None,
                    }),
                    None,
                ),
                IndexComponents::Three(v, a, e) => (
                    Some(IndexPrefix::Vaet {
                        value: v.clone(),
                        attribute: Some(*a),
                        entity: Some(*e),
                    }),
                    None,
                ),
                IndexComponents::Four(v, a, e, t) => (
                    Some(IndexPrefix::Vaet {
                        value: v.clone(),
                        attribute: Some(*a),
                        entity: Some(*e),
                    }),
                    Some(*t),
                ),
            },
        };
        Self {
            normalized,
            routing_prefix,
            tx,
        }
    }

    fn compare_routing_key(&self, key: &crate::persistent_tree::RoutingKey) -> std::cmp::Ordering {
        let Some(prefix) = &self.routing_prefix else {
            return std::cmp::Ordering::Equal;
        };
        let primary = key.cmp_prefix(prefix);
        if primary.is_ne() {
            return primary;
        }
        let Some(tx) = self.tx else {
            return std::cmp::Ordering::Equal;
        };
        if key.tx == 0 {
            // A validated sparse routing key with omitted T is below every
            // concrete member of the tied logical prefix.
            std::cmp::Ordering::Less
        } else {
            // T sorts descending. Equality intentionally covers assertion,
            // retraction, and every strict stored representation at this T.
            tx.cmp(&key.tx)
        }
    }
}

impl DurableTreeCursor {
    fn new(
        snapshot: TieredSnapshot,
        root: Arc<RootNode>,
        history: bool,
        order: IndexOrder,
        start: Option<Datom>,
        end: Option<Datom>,
    ) -> Self {
        let exhausted = root.directories.is_empty();
        let directory_index = start
            .as_ref()
            .map_or(0, |key| floor_tree_child(&root.directories, key, order));
        Self {
            snapshot,
            root,
            history,
            order,
            start,
            end,
            prefix: None,
            forward_boundary: None,
            reverse_boundary: None,
            directory_index,
            leaf_index: 0,
            directory: None,
            leaf: None,
            datom_index: 0,
            stats: TreeReadStats::default(),
            exhausted,
        }
    }

    fn new_prefix(
        snapshot: TieredSnapshot,
        root: Arc<RootNode>,
        history: bool,
        prefix: IndexPrefix,
    ) -> Self {
        let order = prefix.order();
        let exhausted = root.directories.is_empty();
        let directory_index = prefix_start_child(&root.directories, &prefix);
        Self {
            snapshot,
            root,
            history,
            order,
            start: None,
            end: None,
            prefix: Some(prefix),
            forward_boundary: None,
            reverse_boundary: None,
            directory_index,
            leaf_index: 0,
            directory: None,
            leaf: None,
            datom_index: 0,
            stats: TreeReadStats::default(),
            exhausted,
        }
    }

    fn new_forward_boundary(
        snapshot: TieredSnapshot,
        root: Arc<RootNode>,
        history: bool,
        boundary: NormalizedIndexBoundary,
    ) -> Self {
        let order = boundary.order();
        let boundary = TreeBoundary::new(boundary);
        let exhausted = root.directories.is_empty();
        let directory_index = boundary_start_child(&root.directories, &boundary);
        Self {
            snapshot,
            root,
            history,
            order,
            start: None,
            end: None,
            prefix: None,
            forward_boundary: Some(boundary),
            reverse_boundary: None,
            directory_index,
            leaf_index: 0,
            directory: None,
            leaf: None,
            datom_index: 0,
            stats: TreeReadStats::default(),
            exhausted,
        }
    }

    fn new_reverse(
        snapshot: TieredSnapshot,
        root: Arc<RootNode>,
        history: bool,
        boundary: NormalizedIndexBoundary,
    ) -> Self {
        let order = boundary.order();
        let boundary = TreeBoundary::new(boundary);
        let directory_index = boundary_floor_child(&root.directories, &boundary);
        Self {
            snapshot,
            root,
            history,
            order,
            start: None,
            end: None,
            prefix: None,
            forward_boundary: None,
            reverse_boundary: Some(boundary),
            directory_index: directory_index.unwrap_or(0),
            leaf_index: 0,
            directory: None,
            leaf: None,
            datom_index: 0,
            stats: TreeReadStats::default(),
            exhausted: directory_index.is_none(),
        }
    }

    fn next_datom(&mut self) -> Result<Option<Datom>, SemanticError> {
        if self.reverse_boundary.is_some() {
            return self.next_reverse_datom();
        }
        if self.exhausted {
            return Ok(None);
        }
        loop {
            if let Some(leaf) = &self.leaf {
                while let Some(datom) = leaf.datom(self.datom_index) {
                    self.datom_index += 1;
                    if let Some(prefix) = &self.prefix {
                        match crate::index::compare_prefix(&datom, prefix) {
                            std::cmp::Ordering::Less => continue,
                            std::cmp::Ordering::Equal => {}
                            std::cmp::Ordering::Greater => {
                                self.exhausted = true;
                                return Ok(None);
                            }
                        }
                    }
                    if self
                        .forward_boundary
                        .as_ref()
                        .is_some_and(|boundary| boundary.normalized.compare_datom(&datom).is_lt())
                    {
                        continue;
                    }
                    if self
                        .start
                        .as_ref()
                        .is_some_and(|start| datom.cmp_in(start, self.order).is_lt())
                    {
                        continue;
                    }
                    if self
                        .end
                        .as_ref()
                        .is_some_and(|end| !datom.cmp_in(end, self.order).is_lt())
                    {
                        self.exhausted = true;
                        return Ok(None);
                    }
                    return Ok(Some(datom));
                }
                self.leaf = None;
                self.leaf_index = self.leaf_index.saturating_add(1);
            }

            if let Some(directory) = &self.directory {
                if let Some(reference) = directory.leaves.get(self.leaf_index).cloned() {
                    if self
                        .prefix
                        .as_ref()
                        .is_some_and(|prefix| reference.key.cmp_prefix(prefix).is_gt())
                    {
                        self.exhausted = true;
                        return Ok(None);
                    }
                    if self
                        .end
                        .as_ref()
                        .is_some_and(|end| !reference.key.cmp_datom(end, self.order).is_lt())
                    {
                        self.exhausted = true;
                        return Ok(None);
                    }
                    let leaf = self.snapshot.load_leaf(
                        &reference,
                        self.order,
                        self.history,
                        &mut self.stats,
                    )?;
                    self.datom_index = if let Some(start) = &self.start {
                        leaf_lower_bound(&leaf, start, self.order)
                    } else if let Some(prefix) = &self.prefix {
                        leaf_prefix_lower_bound(&leaf, prefix)
                    } else if let Some(boundary) = &self.forward_boundary {
                        leaf_boundary_lower_bound(&leaf, boundary)
                    } else {
                        0
                    };
                    self.leaf = Some(leaf);
                    continue;
                }
                self.directory = None;
                self.directory_index = self.directory_index.saturating_add(1);
            }

            let Some(reference) = self.root.directories.get(self.directory_index).cloned() else {
                self.exhausted = true;
                return Ok(None);
            };
            if self
                .prefix
                .as_ref()
                .is_some_and(|prefix| reference.key.cmp_prefix(prefix).is_gt())
            {
                self.exhausted = true;
                return Ok(None);
            }
            if self
                .end
                .as_ref()
                .is_some_and(|end| !reference.key.cmp_datom(end, self.order).is_lt())
            {
                self.exhausted = true;
                return Ok(None);
            }
            let directory = self.snapshot.load_directory(
                &reference,
                self.order,
                self.history,
                &mut self.stats,
            )?;
            self.leaf_index = if let Some(key) = &self.start {
                floor_tree_child(&directory.leaves, key, self.order)
            } else if let Some(prefix) = &self.prefix {
                prefix_start_child(&directory.leaves, prefix)
            } else if let Some(boundary) = &self.forward_boundary {
                boundary_start_child(&directory.leaves, boundary)
            } else {
                0
            };
            self.directory = Some(directory);
        }
    }

    fn next_reverse_datom(&mut self) -> Result<Option<Datom>, SemanticError> {
        if self.exhausted {
            return Ok(None);
        }
        loop {
            if let Some(leaf) = &self.leaf {
                while self.datom_index > 0 {
                    self.datom_index -= 1;
                    let datom = leaf
                        .datom(self.datom_index)
                        .expect("validated parallel leaf columns");
                    if self
                        .reverse_boundary
                        .as_ref()
                        .expect("reverse cursor retains its boundary")
                        .normalized
                        .compare_datom(&datom)
                        .is_gt()
                    {
                        continue;
                    }
                    return Ok(Some(datom));
                }
                self.leaf = None;
                if self.leaf_index > 0 {
                    self.leaf_index -= 1;
                } else {
                    self.directory = None;
                    if self.directory_index > 0 {
                        self.directory_index -= 1;
                    } else {
                        self.exhausted = true;
                        return Ok(None);
                    }
                }
            }

            if let Some(directory) = &self.directory {
                let reference = directory
                    .leaves
                    .get(self.leaf_index)
                    .cloned()
                    .expect("reverse leaf index was selected from this directory");
                let leaf = self.snapshot.load_leaf(
                    &reference,
                    self.order,
                    self.history,
                    &mut self.stats,
                )?;
                self.datom_index = leaf_reverse_upper_bound(
                    &leaf,
                    self.reverse_boundary
                        .as_ref()
                        .expect("reverse cursor retains its boundary"),
                );
                self.leaf = Some(leaf);
                continue;
            }

            let reference = self
                .root
                .directories
                .get(self.directory_index)
                .cloned()
                .expect("reverse directory index was selected from the root");
            let directory = self.snapshot.load_directory(
                &reference,
                self.order,
                self.history,
                &mut self.stats,
            )?;
            let Some(leaf_index) = boundary_floor_child(
                &directory.leaves,
                self.reverse_boundary
                    .as_ref()
                    .expect("reverse cursor retains its boundary"),
            ) else {
                if self.directory_index > 0 {
                    self.directory_index -= 1;
                    continue;
                }
                self.exhausted = true;
                return Ok(None);
            };
            self.leaf_index = leaf_index;
            self.directory = Some(directory);
        }
    }
}

impl PeerIndexCursor {
    pub fn stats(&self) -> PeerCursorStats {
        PeerCursorStats {
            tree: self.durable.stats,
            recent: self.recent.stats(),
        }
    }

    fn fill_durable(&mut self) -> Result<(), SemanticError> {
        while self.durable_next.is_none() {
            let Some(datom) = self.durable.next_datom()? else {
                break;
            };
            let recent = &self.durable.snapshot.state.recent;
            if !recent.includes(self.order, &datom)?
                || (self.order == IndexOrder::Avet
                    && self
                        .durable
                        .snapshot
                        .state
                        .avet_unready
                        .contains(&datom.attribute))
                || (!self.history && recent.touches_current(&datom))
            {
                continue;
            }
            self.durable_next = Some(datom);
        }
        Ok(())
    }

    fn next_result(&mut self) -> Result<Option<Datom>, SemanticError> {
        self.fill_durable()?;
        while self.recent_next.is_none() {
            let Some(datom) = self.recent.next() else {
                break;
            };
            if self.order == IndexOrder::Avet
                && self
                    .durable
                    .snapshot
                    .state
                    .avet_unready
                    .contains(&datom.attribute)
            {
                continue;
            }
            self.recent_next = Some(datom);
        }
        let take_durable = match (&self.durable_next, &self.recent_next) {
            (Some(durable), Some(recent)) if self.reverse => {
                !durable.cmp_in(recent, self.order).is_lt()
            }
            (Some(durable), Some(recent)) => !recent.cmp_in(durable, self.order).is_lt(),
            (Some(_), None) => true,
            (None, Some(_)) => false,
            (None, None) => return Ok(None),
        };
        if take_durable {
            let datom = self.durable_next.take();
            if datom.as_ref().is_some_and(|durable| {
                self.recent_next
                    .as_ref()
                    .is_some_and(|recent| recent.cmp_in(durable, self.order).is_eq())
            }) {
                self.recent_next = None;
            }
            Ok(datom)
        } else {
            Ok(self.recent_next.take())
        }
    }
}

impl Iterator for PeerIndexCursor {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.next_result() {
            Ok(Some(datom)) => Some(Ok(datom)),
            Ok(None) => None,
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

impl Drop for PeerIndexCursor {
    fn drop(&mut self) {
        if self.work_recorded {
            return;
        }
        self.work_recorded = true;
        let stats = self.stats();
        self.durable
            .snapshot
            .core
            .load_counters
            .record_cursor(stats);
    }
}

impl TieredSnapshot {
    /// Fetch authenticated immutable code; release the I/O lane before any
    /// interpreter runs and recursively opens native read cursors.
    pub(crate) fn resolve_program(
        &self,
        hash: crate::ProgramHash,
    ) -> Result<Arc<crate::program::ValidatedProgram>, SemanticError> {
        let mut io = lock(&self.core.io);
        if io.client.is_closed() {
            reconnect_peer_io(&self.core, &mut io)?;
        }
        crate::postgres::resolve_program_in(&mut io.client, &self.core.programs, hash)
    }
    /// Open an already-committed immutable value without applying writer
    /// admission limits. Exact reports remain readable if an operator later
    /// lowers the live writer's recent-tier ceiling.
    pub(crate) fn open_exact_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        Self::open_exact_configured_for(
            connection,
            database_id.into(),
            endpoint,
            required_manifest,
            ExactOpenConfiguration {
                cache_entries,
                cache_bytes,
                recent_limits,
                purpose: ExactOpenPurpose::ImmutableRead,
            },
        )
    }

    /// Open the exact value used to activate a writer. Unlike an immutable
    /// peer/database-value open, writer admission has a hard recent-memory
    /// ceiling: reject an uncovered tail as soon as its retained account
    /// crosses that ceiling, before constructing its indexes in memory.
    pub(crate) fn open_writer_exact_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        Self::open_exact_configured_for(
            connection,
            database_id.into(),
            endpoint,
            required_manifest,
            ExactOpenConfiguration {
                cache_entries,
                cache_bytes,
                recent_limits,
                purpose: ExactOpenPurpose::WriterActivation,
            },
        )
    }

    fn open_exact_configured_for(
        connection: &PostgresConnectionConfig,
        database_id: String,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
        configuration: ExactOpenConfiguration,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        let endpoint = endpoint.validate()?;
        let mut client = connection.connect_for("peer/exact-open")?;
        verify_schema_compatibility(&mut client)?;
        let lineage_id = read_database_lineage(&mut client, &database_id)?;
        let counters = PeerLoadCounters::default();
        let mut tree_cache =
            TreeNodeCache::new(configuration.cache_entries, configuration.cache_bytes);
        let root_pins = RootPinManager::connect(connection, &database_id)?;
        let required_generation_pin = required_manifest
            .map(|_| root_pins.acquire_generation(endpoint.generation))
            .transpose()?;
        let required_root_pin = required_manifest
            .map(|hash| root_pins.acquire_manifest(hash, false))
            .transpose()?;
        let (base, scan_stats) = exact_tree_selection(
            scan_latest_tree_base(
                &mut client,
                &database_id,
                endpoint.basis_t,
                endpoint.generation,
                required_manifest,
                &counters,
                &mut tree_cache,
            )?,
            required_manifest,
        )?;

        // Unqualified selection learns its hash during discovery. Required
        // receipt selection already owns the fence acquired before discovery.
        let generation_pin = match required_generation_pin {
            Some(pin) => pin,
            None => root_pins.acquire_generation(endpoint.generation)?,
        };
        let root_pin = match required_root_pin {
            Some(pin) => Some(pin),
            None => root_pins
                .acquire(Some(&base))
                .map_err(|error| exact_pin_error(error, required_manifest))?,
        };
        let (state, tail_transactions, tail_range_reads) = build_exact_tiered_state(
            &mut client,
            ExactTieredBuild {
                database_id: &database_id,
                endpoint,
                base,
                generation_pin,
                root_pin,
                recent_limits: configuration.recent_limits,
                purpose: configuration.purpose,
                local_generation: 0,
                counters: &counters,
            },
            &mut tree_cache,
        )?;
        let stats = exact_open_stats(&state, scan_stats, tail_transactions, tail_range_reads)?;
        verify_database_lineage(&mut client, &database_id, &lineage_id)?;
        let core = Arc::new(TieredReadCore {
            database_id,
            lineage_id,
            connection: connection.clone(),
            recent_limits: configuration.recent_limits,
            load_counters: counters,
            root_pins,
            programs: Arc::new(Mutex::new(crate::postgres::ProgramCache::default())),
            io: Mutex::new(PeerIo { client, tree_cache }),
        });
        Ok((
            Self {
                core,
                state: Arc::new(state),
            },
            stats,
        ))
    }

    /// Re-select a native base while preserving this value's exact logical
    /// endpoint. The returned state acquires its own manifest/generation pin;
    /// the old immutable snapshot and its pins are untouched.
    pub(crate) fn rebase_exact(
        &self,
        required_manifest: Option<Digest>,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        self.open_exact_sharing_core(
            self.core.database_id.as_str(),
            self.endpoint(),
            required_manifest,
        )
    }

    /// Open another exact immutable endpoint while reusing this database's
    /// read core. Historical idempotency receipts need distinct states and
    /// root pins, but they do not need a private PostgreSQL I/O session, tree
    /// cache, or root-pin manager for every retained report.
    pub(crate) fn open_exact_sharing_core(
        &self,
        database_id: &str,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        self.open_exact_sharing_core_for(
            database_id,
            endpoint,
            required_manifest,
            ExactOpenPurpose::ImmutableRead,
        )
    }

    /// Writer-purpose variant of shared-core exact open. Receipt replay can
    /// reuse the live writer's PostgreSQL session and tree cache, but it must
    /// retain the same streamed hard-capacity gate as cold activation.
    pub(crate) fn open_writer_exact_sharing_core(
        &self,
        database_id: &str,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        self.open_exact_sharing_core_for(
            database_id,
            endpoint,
            required_manifest,
            ExactOpenPurpose::WriterActivation,
        )
    }

    fn open_exact_sharing_core_for(
        &self,
        database_id: &str,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
        purpose: ExactOpenPurpose,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        if self.core.database_id != database_id {
            return Err(fault(
                "peer/shared-core-database-mismatch",
                "an exact native value cannot reuse another database's read core",
            ));
        }
        let endpoint = endpoint.validate()?;
        self.core.root_pins.ensure()?;
        let required_generation_pin = required_manifest
            .map(|_| self.core.root_pins.acquire_generation(endpoint.generation))
            .transpose()?;
        let required_root_pin = required_manifest
            .map(|hash| self.core.root_pins.acquire_manifest(hash, false))
            .transpose()?;
        let mut io = lock(&self.core.io);
        let scan = {
            let PeerIo { client, tree_cache } = &mut *io;
            scan_latest_tree_base(
                client,
                &self.core.database_id,
                endpoint.basis_t,
                endpoint.generation,
                required_manifest,
                &self.core.load_counters,
                tree_cache,
            )?
        };
        let (base, scan_stats) = exact_tree_selection(scan, required_manifest)?;
        let generation_pin = match required_generation_pin {
            Some(pin) => pin,
            None => self
                .core
                .root_pins
                .acquire_generation(endpoint.generation)?,
        };
        let root_pin = match required_root_pin {
            Some(pin) => Some(pin),
            None => self
                .core
                .root_pins
                .acquire(Some(&base))
                .map_err(|error| exact_pin_error(error, required_manifest))?,
        };
        let local_generation = self.state.generation.saturating_add(1);
        let (state, tail_transactions, tail_range_reads) = {
            let PeerIo {
                client, tree_cache, ..
            } = &mut *io;
            build_exact_tiered_state(
                client,
                ExactTieredBuild {
                    database_id: &self.core.database_id,
                    endpoint,
                    base,
                    generation_pin,
                    root_pin,
                    recent_limits: self.core.recent_limits,
                    purpose,
                    local_generation,
                    counters: &self.core.load_counters,
                },
                tree_cache,
            )?
        };
        let stats = exact_open_stats(&state, scan_stats, tail_transactions, tail_range_reads)?;
        Ok((
            Self {
                core: Arc::clone(&self.core),
                state: Arc::new(state),
            },
            stats,
        ))
    }

    #[cfg(test)]
    pub(crate) fn shares_read_core(&self, other: &Self) -> bool {
        Arc::ptr_eq(&self.core, &other.core)
    }

    pub(crate) fn endpoint(&self) -> ExactEndpoint {
        ExactEndpoint {
            generation: self.state.excision_generation,
            basis_t: self.state.basis_t,
            tx_hash: self.state.current_hash,
            state_hash: self.state.current_state_hash,
            eidx_frontier: self.state.eidx_frontier,
        }
    }

    pub(crate) fn required_manifest_hash(&self) -> Result<Digest, SemanticError> {
        self.state
            .tree_base
            .as_ref()
            .map(|base| base.manifest_hash)
            .ok_or_else(|| {
                fault(
                    "peer/native-index-required",
                    "native report has no durable root",
                )
            })
    }

    pub fn basis_t(&self) -> u64 {
        self.state.basis_t
    }

    /// Exclusive entity-index issuance frontier at this exact immutable basis.
    pub fn eidx_frontier(&self) -> u64 {
        self.state.eidx_frontier
    }

    /// Transaction instant at this exact basis, read through the native index
    /// rather than by materializing the compatibility `Database`.
    pub fn last_tx_instant(&self) -> Result<Option<i64>, SemanticError> {
        self.last_tx_instant_observed(None, None)
    }

    pub(crate) fn last_tx_instant_observed(
        &self,
        read_observer: Option<&LogicalReadObserver>,
        read_context: Option<&TransactionReadContext>,
    ) -> Result<Option<i64>, SemanticError> {
        if self.state.basis_t == 0 {
            return Ok(None);
        }
        let transaction = crate::t_to_tx(self.state.basis_t)?;
        let mut datoms = self.prefix_cursor(
            false,
            &IndexPrefix::Eavt {
                entity: transaction,
                attribute: Some(crate::DB_TX_INSTANT as u32),
                value: None,
            },
        )?;
        let first = match datoms.next().transpose() {
            Ok(datom) => datom,
            Err(error) => {
                if let Some(context) = read_context {
                    context.record_native_cursor(datoms.stats())?;
                }
                return Err(error);
            }
        };
        if let (Some(observer), Some(datom)) = (read_observer, &first)
            && let Err(error) = observer.charge_datom(datom)
        {
            if let Some(context) = read_context {
                context.record_native_cursor(datoms.stats())?;
            }
            return Err(error);
        }
        let second = match datoms.next().transpose() {
            Ok(datom) => datom,
            Err(error) => {
                if let Some(context) = read_context {
                    context.record_native_cursor(datoms.stats())?;
                }
                return Err(error);
            }
        };
        if let Some(context) = read_context {
            context.record_native_cursor(datoms.stats())?;
        }
        if let (Some(observer), Some(datom)) = (read_observer, &second) {
            observer.charge_datom(datom)?;
        }
        match (first, second) {
            (
                Some(Datom {
                    value: crate::Value::Instant(instant),
                    added: true,
                    ..
                }),
                None,
            ) => Ok(Some(instant)),
            _ => Err(fault(
                "peer/invalid-last-tx-instant",
                "native snapshot does not contain exactly one current transaction instant at its basis",
            )),
        }
    }

    /// Discardable schema projection derived from authenticated information
    /// datoms for this exact immutable snapshot.
    pub fn schema(&self) -> &crate::Schema {
        &self.state.metadata.schema
    }

    /// Share this exact value's authenticated resident schema projection.
    /// The projection is immutable and already owned by the tiered state, so
    /// an ordinary transaction need not copy every installed attribute.
    pub(crate) fn schema_arc(&self) -> Arc<crate::Schema> {
        Arc::clone(&self.state.metadata.schema)
    }

    /// Physical AVET availability is an immutable property of this native
    /// value, distinct from the logical `:db/index`/`:db/unique` facts in its
    /// schema.  The distinction is the recovered `Attribute.hasAVET` boundary
    /// used by `add-unique` while a background backfill is outstanding.
    pub(crate) fn avet_ready(&self, attribute: u32) -> bool {
        effective_avet(&self.state.metadata.schema, attribute)
            && !self.state.avet_unready.contains(&attribute)
    }

    fn has_attribute_history(
        &self,
        attribute: u32,
        read_observer: Option<&LogicalReadObserver>,
        read_context: Option<&TransactionReadContext>,
    ) -> Result<bool, SemanticError> {
        let mut cursor = self.prefix_cursor(
            true,
            &IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            },
        )?;
        let datom = match cursor.next().transpose() {
            Ok(datom) => datom,
            Err(error) => {
                if let Some(context) = read_context {
                    context.record_native_cursor(cursor.stats())?;
                }
                return Err(error);
            }
        };
        if let Some(context) = read_context {
            context.record_native_cursor(cursor.stats())?;
        }
        if let (Some(observer), Some(datom)) = (read_observer, &datom) {
            observer.charge_datom(datom)?;
        }
        Ok(datom.is_some())
    }

    /// Path-copy one authenticated committed transaction into a successor
    /// native value. This is the recovered `Db.acceptDataCheck` boundary: the
    /// caller prepares it before publication, then installs the returned value
    /// after head CAS without recovery, SQL reads, or eager materialization.
    pub(crate) fn authenticated_successor(
        &self,
        tx_hash: Digest,
        state_hash: Digest,
        transaction: DurableTransaction,
        successor_schema: &crate::Schema,
        read_context: &TransactionReadContext,
    ) -> Result<Self, SemanticError> {
        self.authenticated_successor_checked(
            tx_hash,
            state_hash,
            transaction,
            Some(successor_schema),
            Some(read_context.observer_ref()),
            Some(read_context),
        )
    }

    /// Reconstruct a known committed successor from one authenticated log
    /// member. Unlike the assessment path there is no independently derived
    /// schema to compare; metadata is deterministically folded from the
    /// authenticated transaction itself.
    pub(crate) fn authenticated_successor_from_log(
        &self,
        tx_hash: Digest,
        state_hash: Digest,
        transaction: DurableTransaction,
    ) -> Result<Self, SemanticError> {
        self.authenticated_successor_checked(tx_hash, state_hash, transaction, None, None, None)
    }

    fn authenticated_successor_checked(
        &self,
        tx_hash: Digest,
        state_hash: Digest,
        transaction: DurableTransaction,
        successor_schema: Option<&crate::Schema>,
        read_observer: Option<&LogicalReadObserver>,
        read_context: Option<&TransactionReadContext>,
    ) -> Result<Self, SemanticError> {
        let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
            &self.state.metadata,
            &self.state.avet_unready,
            std::slice::from_ref(&transaction),
            |attribute| self.has_attribute_history(attribute, read_observer, read_context),
        )?;
        let metadata = Arc::new(metadata);
        if successor_schema.is_some_and(|schema| metadata.schema.as_ref() != schema) {
            return Err(fault(
                "peer/successor-schema-mismatch",
                "committed transaction metadata does not derive the assessed successor schema",
            ));
        }
        let basis_t = transaction.basis_t;
        let eidx_frontier = transaction.eidx_frontier;
        let recent = if successor_schema.is_some() {
            self.state.recent.extend_authenticated(
                std::iter::once((tx_hash, transaction)),
                metadata.endpoint(),
            )?
        } else {
            self.state.recent.extend_authenticated_existing(
                std::iter::once((tx_hash, transaction)),
                metadata.endpoint(),
            )?
        };

        let mut state = (*self.state).clone();
        state.basis_t = basis_t;
        state.eidx_frontier = eidx_frontier;
        state.current_hash = tx_hash;
        state.current_state_hash = state_hash;
        state.recent = Arc::new(recent);
        state.metadata = metadata;
        state.avet_unready = Arc::new(avet_unready);
        state.generation = state.generation.saturating_add(1);
        Ok(Self {
            core: Arc::clone(&self.core),
            state: Arc::new(state),
        })
    }

    pub(crate) fn recent_stats(&self) -> crate::recent::RecentStats {
        self.state.recent.stats()
    }

    pub(crate) fn resident_metadata_stats(&self) -> ResidentMetadataStats {
        self.state.metadata.resident_stats()
    }

    pub(crate) fn resident_tree_root_stats(&self) -> ResidentTreeRootStats {
        self.state
            .tree_base
            .as_ref()
            .map_or_else(ResidentTreeRootStats::default, |base| base.root_residency)
    }

    pub(crate) fn durable_manifest_hash(&self) -> Option<Digest> {
        self.state.tree_base.as_ref().map(|base| base.manifest_hash)
    }

    pub(crate) fn tree_cache_stats(&self) -> CacheStats {
        lock(&self.core.io).tree_cache.stats
    }

    pub(crate) fn load_stats(&self) -> PeerLoadStats {
        self.core.load_counters.snapshot()
    }

    pub(crate) fn database_value(&self) -> crate::DatabaseValue {
        crate::DatabaseValue::tiered(self.clone())
    }

    /// Execute against this captured immutable native snapshot. Advancing the
    /// live peer cannot change the database value observed by the query.
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value().query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value()
            .query_with_extensions(query, inputs, control, extensions)
    }

    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.database_value().entity(identifier)
    }

    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value().pull(pattern, entity)
    }

    pub fn durable_base_t(&self) -> Option<u64> {
        self.state
            .tree_base
            .as_ref()
            .map(|base| base.manifest.basis_t)
    }

    pub fn durable_base_revision(&self) -> Option<u64> {
        self.state
            .tree_base
            .as_ref()
            .map(|base| base.manifest.publication_revision)
    }

    pub fn transaction_hash(&self) -> Digest {
        self.state.current_hash
    }

    /// Resolve a current or historical ident alias from the resident cache
    /// reconstructed from ordinary authenticated `:db/ident` datoms.
    pub fn entid(&self, ident: &crate::Keyword) -> Option<u64> {
        self.state.metadata.idents.resolve(ident)
    }

    /// Return the latest asserted ident for an entity. Older names remain
    /// accepted by [`PeerSnapshot::entid`] unless subsequently repurposed.
    pub fn ident(&self, entity: u64) -> Option<&crate::Keyword> {
        self.state.metadata.idents.ident(entity)
    }

    pub fn seek(
        &self,
        history: bool,
        order: IndexOrder,
        key: &Datom,
    ) -> Result<TreeSeekResult, SemanticError> {
        self.ensure_avet_ready(order, (order == IndexOrder::Avet).then_some(key.attribute))?;
        let mut cursor = self.range_cursor(history, order, Some(key), None)?;
        let datom = cursor.next().transpose()?;
        Ok(TreeSeekResult {
            datom,
            stats: cursor.stats().tree,
        })
    }

    /// Open a lazy half-open range over the exact immutable database value.
    /// Construction touches no directory or leaf; iteration loads one
    /// root-to-leaf path at a time and merges the persistent recent cursor.
    pub fn range_cursor(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<PeerIndexCursor, SemanticError> {
        // Amortize the session-pin health/reacquire check across the complete
        // cursor. Individual directory/leaf loads must remain pure cache/SQL
        // seeks, not add one PostgreSQL round trip per node.
        self.core.root_pins.ensure()?;
        self.ensure_avet_ready(order, None)?;
        if let (Some(start), Some(end)) = (start, end)
            && !start.cmp_in(end, order).is_lt()
        {
            return Err(SemanticError::incorrect(
                "peer/invalid-tree-range",
                "tree range start must precede its exclusive end",
            ));
        }
        let (_, root) = self.exact_tree(history, order)?;
        let range = RecentRange::new(start.cloned(), end.cloned());
        let recent = self.state.recent.cursor(history, order, &range)?;
        let durable = DurableTreeCursor::new(
            self.clone(),
            root,
            history,
            order,
            start.cloned(),
            end.cloned(),
        );
        Ok(PeerIndexCursor {
            durable,
            recent,
            history,
            order,
            reverse: false,
            durable_next: None,
            recent_next: None,
            failed: false,
            work_recorded: false,
        })
    }

    /// Open a lazy cursor over one left-contiguous prefix of this exact
    /// immutable native value. Both the durable tree and bounded recent tier
    /// lower-bound seek to the virtual prefix; iteration stops at its end.
    pub fn prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<PeerIndexCursor, SemanticError> {
        prefix.validate()?;
        self.core.root_pins.ensure()?;
        let requested_attribute = match prefix {
            IndexPrefix::Avet { attribute, .. } => Some(*attribute),
            _ => None,
        };
        self.ensure_avet_ready(prefix.order(), requested_attribute)?;
        let order = prefix.order();
        let (_, root) = self.exact_tree(history, order)?;
        let recent = self.state.recent.prefix_cursor(history, prefix)?;
        let durable = DurableTreeCursor::new_prefix(self.clone(), root, history, prefix.clone());
        Ok(PeerIndexCursor {
            durable,
            recent,
            history,
            order,
            reverse: false,
            durable_next: None,
            recent_next: None,
            failed: false,
            work_recorded: false,
        })
    }

    /// Open a lazy forward raw-index cursor at the typed virtual boundary.
    /// Missing suffix components compare equal, so traversal starts before
    /// the lowest matching datom and then continues through the index.
    pub(crate) fn seek_boundary_cursor(
        &self,
        history: bool,
        boundary: &IndexBoundary,
    ) -> Result<PeerIndexCursor, SemanticError> {
        self.ensure_avet_ready(boundary.order(), boundary.avet_attribute())?;
        self.boundary_cursor_existing_projection(history, boundary, false)
    }

    /// Open a lazy reverse raw-index cursor at the typed virtual boundary.
    /// Construction retains the resident root and positions the memory tree;
    /// durable directory and leaf reads remain deferred until first demand.
    pub(crate) fn reverse_boundary_cursor(
        &self,
        history: bool,
        boundary: &IndexBoundary,
    ) -> Result<PeerIndexCursor, SemanticError> {
        self.ensure_avet_ready(boundary.order(), boundary.avet_attribute())?;
        self.boundary_cursor_existing_projection(history, boundary, true)
    }

    /// Read only the already-present projection at an arbitrary routing key.
    /// A speculative overlay may supply a newly enabled attribute via AEVT;
    /// its base must still seek past that absent attribute without pretending
    /// the base itself has completed a physical backfill. Ordinary qualified
    /// public cursors check readiness before calling this internal primitive.
    pub(crate) fn boundary_cursor_existing_projection(
        &self,
        history: bool,
        boundary: &IndexBoundary,
        reverse: bool,
    ) -> Result<PeerIndexCursor, SemanticError> {
        let normalized = boundary.normalized()?;
        let order = normalized.order();
        self.core.root_pins.ensure()?;
        let (_, root) = self.exact_tree(history, order)?;
        let recent = if reverse {
            self.state
                .recent
                .reverse_boundary_cursor(history, &normalized)
        } else {
            self.state.recent.boundary_cursor(history, &normalized)
        };
        let durable = if reverse {
            DurableTreeCursor::new_reverse(self.clone(), root, history, normalized)
        } else {
            DurableTreeCursor::new_forward_boundary(self.clone(), root, history, normalized)
        };
        Ok(PeerIndexCursor {
            durable,
            recent,
            history,
            order,
            reverse,
            durable_next: None,
            recent_next: None,
            failed: false,
            work_recorded: false,
        })
    }

    pub fn range(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<TreeRangeResult, SemanticError> {
        let mut cursor = self.range_cursor(history, order, start, end)?;
        let datoms = cursor.by_ref().collect::<Result<Vec<_>, _>>()?;
        Ok(TreeRangeResult {
            datoms,
            stats: cursor.stats().tree,
        })
    }

    pub fn datoms(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.range(history, order, None, None)
    }

    /// Read one left-contiguous index prefix without materializing unrelated
    /// tree content. One predecessor child is included because a prefix group
    /// may begin at the end of the segment preceding the first sparse key that
    /// itself compares equal.
    pub fn datoms_with_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.core.root_pins.ensure()?;
        let requested_attribute = match prefix {
            IndexPrefix::Avet { attribute, .. } => Some(*attribute),
            _ => None,
        };
        self.ensure_avet_ready(prefix.order(), requested_attribute)?;
        let durable = self.durable_with_prefix(history, prefix)?;
        let order = prefix.order();
        let merged = if history {
            self.state.recent.merge_history_range(
                order,
                &durable.datoms,
                &RecentRange::unbounded(),
            )?
        } else {
            self.state.recent.merge_current_range(
                order,
                &durable.datoms,
                &RecentRange::unbounded(),
            )?
        };
        Ok(TreeRangeResult {
            datoms: merged
                .into_iter()
                .filter(|datom| crate::index::compare_prefix(datom, prefix).is_eq())
                .collect(),
            stats: durable.stats,
        })
    }

    fn ensure_avet_ready(
        &self,
        order: IndexOrder,
        attribute: Option<u32>,
    ) -> Result<(), SemanticError> {
        if order != IndexOrder::Avet {
            return Ok(());
        }
        let Some(attribute) = attribute else {
            // An unqualified AVET cursor remains useful while one attribute
            // is backfilled: PeerIndexCursor excludes precisely those
            // physically-unready attributes from both durable and recent
            // input streams.
            return Ok(());
        };
        if !effective_avet(&self.state.metadata.schema, attribute) {
            return Err(SemanticError::incorrect(
                "peer/avet-attribute-not-indexed",
                format!("attribute {attribute} does not have AVET storage at this database value"),
            ));
        }
        if !self.state.avet_unready.contains(&attribute) {
            return Ok(());
        }
        Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "peer/avet-not-ready",
            format!(
                "AVET backfill is not yet published for attribute {attribute}; consolidate and refresh the native index"
            ),
        ))
    }

    fn durable_with_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<TreeRangeResult, SemanticError> {
        prefix.validate()?;
        let order = prefix.order();
        let (_, root) = self.exact_tree(history, order)?;
        let mut stats = TreeReadStats::default();
        let mut datoms = Vec::new();
        if root.directories.is_empty() {
            return Ok(TreeRangeResult { datoms, stats });
        }
        let first_directory = prefix_start_child(&root.directories, prefix);
        for directory_ref in root.directories.iter().skip(first_directory) {
            if directory_ref.key.cmp_prefix(prefix).is_gt() {
                break;
            }
            let directory = self.load_directory(directory_ref, order, history, &mut stats)?;
            let first_leaf = prefix_start_child(&directory.leaves, prefix);
            for leaf_ref in directory.leaves.iter().skip(first_leaf) {
                if leaf_ref.key.cmp_prefix(prefix).is_gt() {
                    break;
                }
                let leaf = self.load_leaf(leaf_ref, order, history, &mut stats)?;
                for index in 0..leaf.len() {
                    let datom = leaf.datom(index).expect("validated parallel leaf columns");
                    match crate::index::compare_prefix(&datom, prefix) {
                        std::cmp::Ordering::Less => {}
                        std::cmp::Ordering::Equal => datoms.push(datom),
                        std::cmp::Ordering::Greater => {
                            return Ok(TreeRangeResult { datoms, stats });
                        }
                    }
                }
            }
        }
        Ok(TreeRangeResult { datoms, stats })
    }

    fn exact_tree(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<(&TreeBase, Arc<RootNode>), SemanticError> {
        let base = self.state.tree_base.as_deref().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unavailable,
                "peer/no-tree-base",
                "this database has no published persistent tree yet",
            )
        })?;
        let recent = self.state.recent.stats();
        if base.manifest.basis_t != recent.base_t
            || base.manifest.tx_hash != recent.base_hash
            || recent.end_t != self.state.basis_t
            || recent.end_hash != self.state.current_hash
        {
            return Err(fault(
                "peer/tree-tail-anchor",
                "persistent tree and recent tier do not describe one contiguous database value",
            ));
        }
        let root = base
            .roots
            .get(&(history, order_tag(order)))
            .cloned()
            .ok_or_else(|| {
                fault(
                    "peer/missing-tree-root",
                    "tree base omitted a required root",
                )
            })?;
        Ok((base, root))
    }

    fn load_directory(
        &self,
        reference: &ChildRef,
        order: IndexOrder,
        history: bool,
        stats: &mut TreeReadStats,
    ) -> Result<LoadedDirectory, SemanticError> {
        let node = self.load_node(reference.hash, stats)?;
        let TreeNode::Directory(directory) = node.as_ref() else {
            return Err(fault(
                "peer/tree-child-kind",
                "root child is not a directory node",
            ));
        };
        validate_loaded_child_key(
            reference,
            directory.order,
            directory.history,
            directory.count,
            directory.leaves.first().map(|child| &child.key),
            order,
            history,
        )?;
        Ok(LoadedDirectory(node))
    }

    fn load_leaf(
        &self,
        reference: &ChildRef,
        order: IndexOrder,
        history: bool,
        stats: &mut TreeReadStats,
    ) -> Result<LoadedLeaf, SemanticError> {
        let node = self.load_node(reference.hash, stats)?;
        let TreeNode::Leaf(leaf) = node.as_ref() else {
            return Err(fault(
                "peer/tree-child-kind",
                "directory child is not a leaf node",
            ));
        };
        let first = leaf.datom(0);
        validate_loaded_child_datom(
            reference,
            leaf.order,
            leaf.history,
            leaf.len() as u64,
            first.as_ref(),
            order,
            history,
        )?;
        Ok(LoadedLeaf(node))
    }

    fn load_node(
        &self,
        hash: Digest,
        stats: &mut TreeReadStats,
    ) -> Result<Arc<TreeNode>, SemanticError> {
        let mut io = lock(&self.core.io);
        if let Some(node) = io.tree_cache.get(&hash) {
            stats.cache_hits = stats.cache_hits.saturating_add(1);
            return Ok(node);
        }
        stats.cache_misses = stats.cache_misses.saturating_add(1);
        let query = io
            .client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("peer/tree-node-read", error));
        let row = match query {
            Err(error) if is_postgres_connection_error(&error) => {
                reconnect_peer_io(&self.core, &mut io)?;
                io.client
                    .query_opt(
                        "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                        &[&&hash[..]],
                    )
                    .map_err(|error| postgres_error("peer/tree-node-read", error))?
            }
            result => result?,
        }
        .ok_or_else(|| fault("peer/missing-tree-node", "tree child is missing"))?;
        let bytes: Vec<u8> = row.get(0);
        let node = Arc::new(decode_tree_node(&hash, &bytes)?);
        stats.decoded_bytes = stats.decoded_bytes.saturating_add(bytes.len() as u64);
        match node.as_ref() {
            TreeNode::Root(_) => stats.root_reads = stats.root_reads.saturating_add(1),
            TreeNode::Directory(_) => {
                stats.directory_reads = stats.directory_reads.saturating_add(1);
                self.core
                    .load_counters
                    .directory_reads
                    .fetch_add(1, Ordering::Relaxed);
            }
            TreeNode::Leaf(_) => {
                stats.leaf_reads = stats.leaf_reads.saturating_add(1);
                self.core
                    .load_counters
                    .leaf_reads
                    .fetch_add(1, Ordering::Relaxed);
            }
        }
        let retained = usize::try_from(node.retained_bytes()).unwrap_or(usize::MAX);
        // Charge both the stable encoded footprint and decoded allocations.
        // This is conservative—the SQL payload buffer is dropped here—but it
        // prevents compact encodings from disguising large resident values.
        let cache_weight = bytes.len().saturating_add(retained);
        io.tree_cache.insert(hash, Arc::clone(&node), cache_weight);
        Ok(node)
    }
}

impl PeerSnapshot {
    pub(crate) fn tiered_snapshot(&self) -> TieredSnapshot {
        self.native.clone()
    }

    pub fn basis_t(&self) -> u64 {
        self.native.basis_t()
    }

    pub fn eidx_frontier(&self) -> u64 {
        self.native.eidx_frontier()
    }

    pub fn last_tx_instant(&self) -> Result<Option<i64>, SemanticError> {
        self.native.last_tx_instant()
    }

    pub fn schema(&self) -> &crate::Schema {
        self.native.schema()
    }

    /// Extract only the immutable native value. The returned database value
    /// retains neither this peer wrapper nor its eager compatibility cell.
    pub fn database_value(&self) -> crate::DatabaseValue {
        self.native.database_value()
    }

    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.native.query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.native
            .query_with_extensions(query, inputs, control, extensions)
    }

    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.native.entity(identifier)
    }

    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.native.pull(pattern, entity)
    }

    pub fn durable_base_t(&self) -> Option<u64> {
        self.native.durable_base_t()
    }

    pub fn durable_base_revision(&self) -> Option<u64> {
        self.native.durable_base_revision()
    }

    pub fn transaction_hash(&self) -> Digest {
        self.native.transaction_hash()
    }

    pub fn entid(&self, ident: &crate::Keyword) -> Option<u64> {
        self.native.entid(ident)
    }

    pub fn ident(&self, entity: u64) -> Option<&crate::Keyword> {
        self.native.ident(entity)
    }

    pub fn try_eager_oracle(&self) -> Result<Arc<Database>, SemanticError> {
        database_for_compatibility(&self.native, &self.compatibility)
    }

    pub fn eager_oracle(&self) -> Arc<Database> {
        self.try_eager_oracle()
            .unwrap_or_else(|error| panic!("peer snapshot materialization failed: {error}"))
    }

    pub fn seek(
        &self,
        history: bool,
        order: IndexOrder,
        key: &Datom,
    ) -> Result<TreeSeekResult, SemanticError> {
        self.native.seek(history, order, key)
    }

    pub fn range_cursor(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<PeerIndexCursor, SemanticError> {
        self.native.range_cursor(history, order, start, end)
    }

    pub fn prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<PeerIndexCursor, SemanticError> {
        self.native.prefix_cursor(history, prefix)
    }

    pub fn range(
        &self,
        history: bool,
        order: IndexOrder,
        start: Option<&Datom>,
        end: Option<&Datom>,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.native.range(history, order, start, end)
    }

    pub fn datoms(
        &self,
        history: bool,
        order: IndexOrder,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.native.datoms(history, order)
    }

    pub fn datoms_with_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<TreeRangeResult, SemanticError> {
        self.native.datoms_with_prefix(history, prefix)
    }
}

fn database_for_compatibility(
    native: &TieredSnapshot,
    compatibility: &PeerCompatibility,
) -> Result<Arc<Database>, SemanticError> {
    if let Some(database) = compatibility.value.get() {
        native
            .core
            .load_counters
            .compatibility_hits
            .fetch_add(1, Ordering::Relaxed);
        return Ok(Arc::clone(database));
    }
    native.core.root_pins.ensure()?;
    let mut io = lock(&native.core.io);
    let mut segments = lock(&compatibility.segments);
    // Recheck after acquiring the one shared native I/O lane so concurrent
    // compatibility callers coalesce into a single explicit materialization.
    if let Some(database) = compatibility.value.get() {
        native
            .core
            .load_counters
            .compatibility_hits
            .fetch_add(1, Ordering::Relaxed);
        return Ok(Arc::clone(database));
    }
    let materialized = match counted_compatibility_materialization_with_io(
        &native.core,
        &mut io,
        &mut segments,
        &native.state,
    ) {
        Err(error) if is_postgres_connection_error(&error) => {
            reconnect_peer_io(&native.core, &mut io)?;
            counted_compatibility_materialization_with_io(
                &native.core,
                &mut io,
                &mut segments,
                &native.state,
            )
        }
        result => result,
    };
    match materialized {
        Ok(database) => {
            let _ = compatibility.value.set(Arc::clone(&database));
            Ok(compatibility
                .value
                .get()
                .map(Arc::clone)
                .unwrap_or(database))
        }
        Err(error) => Err(error),
    }
}

fn counted_compatibility_materialization_with_io(
    core: &TieredReadCore,
    io: &mut PeerIo,
    segments: &mut SegmentCache,
    state: &TieredState,
) -> Result<Arc<Database>, SemanticError> {
    core.load_counters
        .compatibility_materializations
        .fetch_add(1, Ordering::Relaxed);
    match materialize_compatibility_with_io(core, io, segments, state) {
        Ok(database) => Ok(database),
        Err(error) => {
            core.load_counters
                .compatibility_failures
                .fetch_add(1, Ordering::Relaxed);
            Err(error)
        }
    }
}

fn materialize_compatibility_with_io(
    core: &TieredReadCore,
    io: &mut PeerIo,
    segments: &mut SegmentCache,
    state: &TieredState,
) -> Result<Arc<Database>, SemanticError> {
    let legacy_base = if state.excision_generation == 0 {
        load_latest_base(&mut io.client, &core.database_id, state.basis_t, segments)?
    } else {
        None
    };
    let (mut database, mut hash, _) = match legacy_base {
        Some(base) => base,
        None => {
            let recovered = recover_to(
                &mut io.client,
                &core.database_id,
                state.basis_t,
                state.current_hash,
            )?;
            (recovered.database, recovered.final_hash, 0)
        }
    };
    if database.basis_t() < state.basis_t {
        apply_tail(
            &mut io.client,
            &core.database_id,
            state.excision_generation,
            &mut database,
            &mut hash,
            state.basis_t,
        )?;
    }
    if hash != state.current_hash {
        return Err(fault(
            "peer/compatibility-hash",
            "materialized database does not reach the snapshot transaction hash",
        ));
    }
    verify_materialized_endpoint(&database, state)?;
    Ok(Arc::new(database))
}

fn compatibility_value(state: &PeerState) -> Result<Arc<Database>, SemanticError> {
    state
        .compatibility
        .value
        .get()
        .map(Arc::clone)
        .ok_or_else(|| {
            fault(
                "peer/missing-compatibility-value",
                "compatibility sync did not materialize its requested database value",
            )
        })
}

fn sync_timeout(target: u64) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "peer/sync-timeout",
        format!("basis {target} is not yet committed"),
    )
}

fn verify_materialized_endpoint(
    database: &Database,
    state: &TieredState,
) -> Result<(), SemanticError> {
    if database.basis_t() != state.basis_t || database.eidx_frontier() != state.eidx_frontier {
        return Err(fault(
            "peer/compatibility-endpoint",
            "materialized database basis or entity frontier disagrees with native snapshot",
        ));
    }
    if state.current_state_hash != [0; 32]
        && !verify_checkpoint_state_hash(database, state.current_state_hash)?.matches()
    {
        return Err(fault(
            "peer/tail-state-commitment-mismatch",
            "materialized database does not produce the authoritative endpoint commitment",
        ));
    }
    Ok(())
}

fn validate_loaded_child_key(
    reference: &ChildRef,
    actual_order: IndexOrder,
    actual_history: bool,
    actual_count: u64,
    actual_first: Option<&crate::persistent_tree::RoutingKey>,
    expected_order: IndexOrder,
    expected_history: bool,
) -> Result<(), SemanticError> {
    if actual_order != expected_order
        || actual_history != expected_history
        || actual_count != reference.count
        || actual_first.is_none_or(|first| reference.key.cmp_key(first, expected_order).is_gt())
    {
        return Err(fault(
            "peer/tree-child-reference",
            "loaded tree child disagrees with its authenticated parent reference",
        ));
    }
    Ok(())
}

fn validate_loaded_child_datom(
    reference: &ChildRef,
    actual_order: IndexOrder,
    actual_history: bool,
    actual_count: u64,
    actual_first: Option<&Datom>,
    expected_order: IndexOrder,
    expected_history: bool,
) -> Result<(), SemanticError> {
    if actual_order != expected_order
        || actual_history != expected_history
        || actual_count != reference.count
        || actual_first.is_none_or(|first| reference.key.cmp_datom(first, expected_order).is_gt())
    {
        return Err(fault(
            "peer/tree-child-reference",
            "loaded tree child disagrees with its authenticated parent reference",
        ));
    }
    Ok(())
}

fn floor_tree_child(children: &[ChildRef], key: &Datom, order: IndexOrder) -> usize {
    children
        .partition_point(|child| !child.key.cmp_datom(key, order).is_gt())
        .saturating_sub(1)
}

fn prefix_start_child(children: &[ChildRef], prefix: &IndexPrefix) -> usize {
    children
        .partition_point(|child| child.key.cmp_prefix(prefix).is_lt())
        .saturating_sub(1)
}

fn boundary_floor_child(children: &[ChildRef], boundary: &TreeBoundary) -> Option<usize> {
    children
        .partition_point(|child| !boundary.compare_routing_key(&child.key).is_gt())
        .checked_sub(1)
}

fn boundary_start_child(children: &[ChildRef], boundary: &TreeBoundary) -> usize {
    // Equal virtual keys can span children (operation and stored-value ties
    // are omitted from the boundary). Forward seeks must start before all of
    // them; reverse seeks use the last equal child instead.
    children
        .partition_point(|child| boundary.compare_routing_key(&child.key).is_lt())
        .saturating_sub(1)
}

fn leaf_lower_bound(leaf: &LeafSegment, key: &Datom, order: IndexOrder) -> usize {
    let mut low = 0;
    let mut high = leaf.len();
    while low < high {
        let middle = low + (high - low) / 2;
        let datom = leaf.datom(middle).expect("validated parallel leaf columns");
        if datom.cmp_in(key, order).is_lt() {
            low = middle + 1;
        } else {
            high = middle;
        }
    }
    low
}

fn leaf_prefix_lower_bound(leaf: &LeafSegment, prefix: &IndexPrefix) -> usize {
    let mut low = 0;
    let mut high = leaf.len();
    while low < high {
        let middle = low + (high - low) / 2;
        let datom = leaf.datom(middle).expect("validated parallel leaf columns");
        if crate::index::compare_prefix(&datom, prefix).is_lt() {
            low = middle + 1;
        } else {
            high = middle;
        }
    }
    low
}

fn leaf_boundary_lower_bound(leaf: &LeafSegment, boundary: &TreeBoundary) -> usize {
    let mut low = 0;
    let mut high = leaf.len();
    while low < high {
        let middle = low + (high - low) / 2;
        let datom = leaf.datom(middle).expect("validated parallel leaf columns");
        if boundary.normalized.compare_datom(&datom).is_lt() {
            low = middle + 1;
        } else {
            high = middle;
        }
    }
    low
}

fn leaf_reverse_upper_bound(leaf: &LeafSegment, boundary: &TreeBoundary) -> usize {
    let mut low = 0;
    let mut high = leaf.len();
    while low < high {
        let middle = low + (high - low) / 2;
        let datom = leaf.datom(middle).expect("validated parallel leaf columns");
        if !boundary.normalized.compare_datom(&datom).is_gt() {
            low = middle + 1;
        } else {
            high = middle;
        }
    }
    low
}

fn same_stored_datom(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.tx == right.tx
        && left.added == right.added
        && left.value.stored_eq(&right.value)
}

fn lock<T>(mutex: &Mutex<T>) -> MutexGuard<'_, T> {
    mutex
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner)
}

fn read_excision_generation<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<u64, SemanticError> {
    let row = client
        .query_opt(
            "SELECT excision_generation FROM atomic_database_generations WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("peer/excision-generation", error))?
        .ok_or_else(|| {
            fault(
                "peer/missing-excision-generation",
                "database has no operations generation row; run migrations",
            )
        })?;
    pg_basis(row.get(0), "excision generation")
}

fn current_tree_publication_revision<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<u64, SemanticError> {
    let row = client
        .query_opt(
            "SELECT publication_revision FROM atomic_tree_publications \
             WHERE database_id = $1 ORDER BY publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("peer/tree-publication-revision", error))?;
    row.map(|row| pg_basis(row.get(0), "tree publication revision"))
        .transpose()
        .map(|revision| revision.unwrap_or(0))
}

fn load_cached_tree_node_from_client<C: GenericClient>(
    client: &mut C,
    hash: Digest,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<Arc<TreeNode>, SemanticError> {
    if let Some(node) = cache.get(&hash) {
        return Ok(node);
    }
    let row = client
        .query_opt(
            "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
            &[&&hash[..]],
        )
        .map_err(|error| postgres_error("peer/avet-readiness-node-read", error))?
        .ok_or_else(|| {
            fault(
                "peer/missing-avet-readiness-node",
                "published AEVT history path references missing immutable content",
            )
        })?;
    let bytes: Vec<u8> = row.get(0);
    let node = Arc::new(decode_tree_node(&hash, &bytes)?);
    match node.as_ref() {
        TreeNode::Directory(_) => {
            counters.directory_reads.fetch_add(1, Ordering::Relaxed);
        }
        TreeNode::Leaf(_) => {
            counters.leaf_reads.fetch_add(1, Ordering::Relaxed);
        }
        TreeNode::Root(_) => {}
    }
    let retained = usize::try_from(node.retained_bytes()).unwrap_or(usize::MAX);
    cache.insert(
        hash,
        Arc::clone(&node),
        bytes.len().saturating_add(retained),
    );
    Ok(node)
}

/// Test recovered `has-values?` against only the durable base of a tail.
/// Construction-time callers do not yet own a `TieredSnapshot`, so this is a
/// direct root-to-one-leaf AEVT probe rather than a full range materialization.
fn tree_base_has_attribute_history<C: GenericClient>(
    client: &mut C,
    base: &TreeBase,
    attribute: u32,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<bool, SemanticError> {
    let history = true;
    let order = IndexOrder::Aevt;
    let root = base
        .roots
        .get(&(history, order_tag(order)))
        .ok_or_else(|| {
            fault(
                "peer/missing-history-aevt-root",
                "native base omitted its history AEVT root",
            )
        })?;
    if root.directories.is_empty() {
        return Ok(false);
    }
    let prefix = IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    };
    let first_directory = prefix_start_child(&root.directories, &prefix);
    for (directory_index, reference) in root.directories.iter().enumerate().skip(first_directory) {
        let node = load_cached_tree_node_from_client(client, reference.hash, counters, cache)?;
        let TreeNode::Directory(directory) = node.as_ref() else {
            return Err(fault(
                "peer/avet-readiness-directory-kind",
                "history AEVT root child is not a directory",
            ));
        };
        validate_loaded_child_key(
            reference,
            directory.order,
            directory.history,
            directory.count,
            directory.leaves.first().map(|child| &child.key),
            order,
            history,
        )?;
        let first_leaf = if directory_index == first_directory {
            prefix_start_child(&directory.leaves, &prefix)
        } else {
            0
        };
        for leaf_reference in directory.leaves.iter().skip(first_leaf) {
            let node =
                load_cached_tree_node_from_client(client, leaf_reference.hash, counters, cache)?;
            let TreeNode::Leaf(leaf) = node.as_ref() else {
                return Err(fault(
                    "peer/avet-readiness-leaf-kind",
                    "history AEVT directory child is not a leaf",
                ));
            };
            validate_loaded_child_datom(
                leaf_reference,
                leaf.order,
                leaf.history,
                leaf.len() as u64,
                leaf.datom(0).as_ref(),
                order,
                history,
            )?;
            let offset = leaf_prefix_lower_bound(leaf, &prefix);
            if let Some(candidate) = leaf.datom(offset) {
                return Ok(crate::index::compare_prefix(&candidate, &prefix).is_eq());
            }
        }
    }
    Ok(false)
}

/// Test recovered `has-values?` while a live peer already owns its I/O guard.
/// History existence is a union across the immutable recent tier and durable
/// base, so this can probe each directly without opening a public cursor (and
/// recursively locking the same non-reentrant `PeerIo` mutex).
fn peer_state_has_attribute_history_with_io(
    core: &TieredReadCore,
    io: &mut PeerIo,
    state: &PeerState,
    attribute: u32,
) -> Result<bool, SemanticError> {
    let prefix = IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    };
    if state.recent.prefix_cursor(true, &prefix)?.next().is_some() {
        return Ok(true);
    }
    if let Some(base) = state.tree_base.as_deref() {
        let PeerIo {
            client, tree_cache, ..
        } = io;
        return tree_base_has_attribute_history(
            client,
            base,
            attribute,
            &core.load_counters,
            tree_cache,
        );
    }
    let database = state.compatibility.value.get().ok_or_else(|| {
        fault(
            "peer/avet-readiness-source-missing",
            "log-only peer state has no compatibility value for AVET readiness",
        )
    })?;
    Ok(!database.history_with_prefix(&prefix)?.is_empty())
}

fn derive_metadata_from_client<C: GenericClient>(
    client: &mut C,
    roots: &BTreeMap<(bool, u8), Arc<RootNode>>,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<MetadataProjection, SemanticError> {
    derive_metadata_from_roots(roots, |hash| {
        if let Some(node) = cache.get(&hash) {
            return Ok(node);
        }
        let row = client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("peer/metadata-node-read", error))?
            .ok_or_else(|| {
                fault(
                    "peer/missing-metadata-node",
                    "published metadata path references missing immutable content",
                )
            })?;
        let bytes: Vec<u8> = row.get(0);
        let node = Arc::new(decode_tree_node(&hash, &bytes)?);
        match node.as_ref() {
            TreeNode::Directory(_) => {
                counters.directory_reads.fetch_add(1, Ordering::Relaxed);
            }
            TreeNode::Leaf(_) => {
                counters.leaf_reads.fetch_add(1, Ordering::Relaxed);
            }
            TreeNode::Root(_) => {}
        }
        let retained = usize::try_from(node.retained_bytes()).unwrap_or(usize::MAX);
        cache.insert(
            hash,
            Arc::clone(&node),
            bytes.len().saturating_add(retained),
        );
        Ok(node)
    })
}

/// Load the small manifest and its eight resident roots, then reconstruct the
/// always-resident ident/schema working set through authenticated tree paths.
/// Application leaves remain untouched until a snapshot read requests them.
/// As in the recovered peer, schema and names are derived from ordinary index
/// information rather than duplicated inside the durable root envelope.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
struct TreeBaseScanStats {
    examined_candidates: u64,
    rejected_candidates: u64,
}

enum TreeBaseScan {
    Selected(Box<TreeBase>, TreeBaseScanStats),
    NoPublication(TreeBaseScanStats),
    AllInvalid(TreeBaseScanStats),
    RequiredCollecting(TreeBaseScanStats),
}

fn scan_latest_tree_base<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    required_manifest: Option<Digest>,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<TreeBaseScan, SemanticError> {
    let through_sql = sql_basis(through)?;
    let generation_sql = sql_basis(excision_generation)?;
    let window = tree_publication_window(
        client,
        database_id,
        through,
        excision_generation,
        required_manifest,
    )?;
    if let Some(required_hash) = required_manifest {
        let archive_exists: bool = client
            .query_one(
                "SELECT EXISTS (SELECT 1 FROM atomic_request_base_archives archive \
                  JOIN atomic_request_base_archive_completions complete \
                    ON complete.manifest_hash = archive.manifest_hash \
                  WHERE archive.database_id = $1 AND archive.generation = $2 \
                    AND archive.basis_t <= $3 AND archive.manifest_hash = $4)",
                &[
                    &database_id,
                    &generation_sql,
                    &through_sql,
                    &&required_hash[..],
                ],
            )
            .map_err(|error| postgres_error("peer/exact-tree-archive-exists", error))?
            .get(0);
        if archive_exists {
            let mut stats = TreeBaseScanStats {
                examined_candidates: 1,
                ..TreeBaseScanStats::default()
            };
            counters.manifest_candidates.fetch_add(1, Ordering::Relaxed);
            if window.newest_revision.is_some() {
                stats.rejected_candidates = 1;
                return Ok(TreeBaseScan::AllInvalid(stats));
            }
            return match load_required_request_base_archive(
                client,
                database_id,
                through,
                excision_generation,
                required_hash,
                counters,
                cache,
            ) {
                Ok(base) => Ok(TreeBaseScan::Selected(Box::new(base), stats)),
                Err(error) if is_postgres_connection_error(&error) => Err(error),
                Err(_) => {
                    stats.rejected_candidates = 1;
                    Ok(TreeBaseScan::AllInvalid(stats))
                }
            };
        }
    }
    let mut stats = TreeBaseScanStats::default();
    if window.newest_revision.is_none() {
        return Ok(TreeBaseScan::NoPublication(stats));
    }
    if let (Some(required_hash), Some(newest_revision)) =
        (required_manifest, window.newest_revision)
    {
        let collecting: bool = client
            .query_one(
                "SELECT EXISTS ( \
                    SELECT 1 FROM atomic_tree_publications publication \
                    JOIN atomic_tree_retirement_progress progress \
                      ON progress.database_id = publication.database_id \
                     AND progress.publication_revision = publication.publication_revision \
                     AND progress.manifest_hash = publication.manifest_hash \
                    WHERE publication.database_id = $1 \
                      AND publication.log_generation = $2 \
                      AND publication.basis_t <= $3 \
                      AND publication.manifest_hash = $4 \
                      AND publication.publication_revision <= $5)",
                &[
                    &database_id,
                    &generation_sql,
                    &through_sql,
                    &&required_hash[..],
                    &sql_basis(newest_revision)?,
                ],
            )
            .map_err(|error| postgres_error("peer/exact-tree-collecting", error))?
            .get(0);
        if collecting {
            stats.examined_candidates = 1;
            stats.rejected_candidates = 1;
            return Ok(TreeBaseScan::RequiredCollecting(stats));
        }
    }
    let mut before_revision = None;
    loop {
        let locators = tree_publication_locator_page(
            client,
            database_id,
            through,
            excision_generation,
            required_manifest,
            window,
            before_revision,
        )?;
        let Some(last_revision) = locators.last().map(|locator| locator.publication_revision)
        else {
            break;
        };
        for locator in locators {
            if stats.examined_candidates == MAX_MANIFEST_CANDIDATE_PROBES {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "peer/native-index-repair-required",
                    "native manifest selection reached the bounded corruption probe limit; publish an explicit administrative repair root",
                )
                .detail("examined_candidates", stats.examined_candidates.to_string())
                .detail(
                    "maximum_candidate_probes",
                    MAX_MANIFEST_CANDIDATE_PROBES.to_string(),
                ));
            }
            stats.examined_candidates = stats.examined_candidates.saturating_add(1);
            counters.manifest_candidates.fetch_add(1, Ordering::Relaxed);
            if locator.collecting {
                stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
                if required_manifest.is_some() {
                    return Ok(TreeBaseScan::RequiredCollecting(stats));
                }
                continue;
            }
            let manifest_hash = match digest(locator.manifest_hash, "tree manifest locator hash") {
                Ok(hash) => hash,
                Err(_) => {
                    stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
                    continue;
                }
            };
            match load_peer_tree_manifest_candidate(
                client,
                database_id,
                through,
                excision_generation,
                locator.publication_revision,
                manifest_hash,
                counters,
                cache,
            ) {
                Ok(base) => return Ok(TreeBaseScan::Selected(Box::new(base), stats)),
                Err(error) if is_postgres_connection_error(&error) => return Err(error),
                Err(error) if error.code == "peer/tree-publication-collecting" => {
                    stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
                    if required_manifest.is_some() {
                        return Ok(TreeBaseScan::RequiredCollecting(stats));
                    }
                }
                Err(_) => {
                    stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
                }
            }
        }
        before_revision = Some(last_revision);
    }
    Ok(TreeBaseScan::AllInvalid(stats))
}

#[allow(clippy::too_many_arguments)]
fn load_peer_tree_manifest_candidate<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    publication_revision: u64,
    manifest_hash: Digest,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<TreeBase, SemanticError> {
    let row = client
        .query_opt(
            "SELECT m.basis_t, m.tx_hash, m.state_hash, m.eidx_frontier, \
                    m.manifest_hash, m.payload, \
                    ((m.log_generation = 0 AND legacy.tx_hash IS NOT NULL) \
                      OR (m.log_generation > 0 AND m.basis_t = 0 \
                          AND bootstrap.tx_hash IS NOT NULL) \
                      OR (m.log_generation > 0 AND m.basis_t > 0 \
                          AND native.tx_hash IS NOT NULL)) \
                    AND semantic.tx_hash IS NOT NULL, \
                    EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                             WHERE progress.database_id = p.database_id \
                               AND progress.publication_revision = p.publication_revision \
                               AND progress.manifest_hash = p.manifest_hash) \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
                AND m.log_generation = p.log_generation \
               JOIN atomic_databases catalog ON catalog.database_id = m.database_id \
               LEFT JOIN atomic_transactions legacy \
                 ON m.log_generation = 0 AND legacy.database_id = m.database_id \
                AND legacy.basis_t = m.basis_t AND legacy.tx_hash = m.tx_hash \
                AND legacy.state_hash = m.state_hash \
               LEFT JOIN atomic_generation_transactions native \
                 ON m.log_generation > 0 AND native.database_id = m.database_id \
                AND native.generation = m.log_generation AND native.basis_t = m.basis_t \
                AND native.tx_hash = m.tx_hash AND native.state_hash = m.state_hash \
               LEFT JOIN atomic_semantic_commitment_roots bootstrap \
                 ON m.log_generation > 0 AND m.basis_t = 0 \
                AND bootstrap.database_id = m.database_id \
                AND bootstrap.generation = m.log_generation AND bootstrap.basis_t = 0 \
                AND bootstrap.tx_hash = m.tx_hash AND bootstrap.state_hash = m.state_hash \
                AND bootstrap.eidx_frontier = m.eidx_frontier \
                AND bootstrap.commitment_version = 2 \
                AND bootstrap.tx_hash = catalog.genesis_hash \
               LEFT JOIN atomic_semantic_commitment_roots semantic \
                 ON semantic.database_id = m.database_id \
                AND semantic.generation = m.log_generation \
                AND semantic.basis_t = m.basis_t \
                AND semantic.tx_hash = m.tx_hash \
                AND semantic.state_hash = m.state_hash \
                AND semantic.eidx_frontier = m.eidx_frontier \
                AND semantic.commitment_version = 2 \
              WHERE p.database_id = $1 AND p.publication_revision = $2 \
                AND p.manifest_hash = $3 AND p.basis_t <= $4 \
                AND p.log_generation = $5",
            &[
                &database_id,
                &sql_basis(publication_revision)?,
                &&manifest_hash[..],
                &sql_basis(through)?,
                &sql_basis(excision_generation)?,
            ],
        )
        .map_err(|error| postgres_error("peer/tree-manifest-candidate", error))?
        .ok_or_else(|| {
            fault(
                "peer/tree-manifest-authority",
                "tree publication does not resolve to its canonical manifest",
            )
        })?;
    let basis_t = pg_basis(row.get(0), "tree manifest basis")?;
    let tx_hash = digest(row.get(1), "tree manifest transaction hash")?;
    let state_hash = digest(row.get(2), "tree manifest state hash")?;
    let eidx_frontier = pg_basis(row.get(3), "tree manifest entity frontier")?;
    let stored_manifest_hash = digest(row.get(4), "tree manifest hash")?;
    let payload: Vec<u8> = row.get(5);
    let authoritative: bool = row.get(6);
    let collecting: bool = row.get(7);
    if collecting {
        return Err(fault(
            "peer/tree-publication-collecting",
            "tree publication began collection during candidate loading",
        ));
    }
    if !authoritative {
        return Err(fault(
            "peer/tree-manifest-authority",
            "tree publication is not bound to an authoritative generation endpoint",
        ));
    }
    if stored_manifest_hash != manifest_hash || sha256(&payload) != manifest_hash {
        return Err(fault(
            "peer/tree-manifest-hash",
            "tree manifest bytes do not match their publication",
        ));
    }
    let manifest = PersistentTreeManifest::decode(&payload)?;
    if manifest.database_id != database_id
        || manifest.publication_revision != publication_revision
        || manifest.basis_t != basis_t
        || manifest.tx_hash != tx_hash
        || manifest.state_hash != state_hash
        || manifest.excision_generation != excision_generation
        || manifest.eidx_frontier != eidx_frontier
    {
        return Err(fault(
            "peer/tree-manifest-metadata",
            "canonical tree manifest disagrees with its authenticated SQL row",
        ));
    }
    let root_rows = client
        .query(
            "SELECT r.index_order, r.history, r.root_hash, r.datom_count, \
                    r.encoded_bytes, n.payload \
               FROM atomic_tree_manifest_roots r \
               JOIN atomic_tree_nodes n ON n.node_hash = r.root_hash \
              WHERE r.manifest_hash = $1 \
              ORDER BY r.history, r.index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("peer/tree-roots", error))?;
    if root_rows.len() != 8 {
        return Err(fault(
            "peer/tree-root-count",
            "published tree manifest does not resolve to eight roots",
        ));
    }
    let mut roots = BTreeMap::new();
    for root_row in root_rows {
        let tag: i16 = root_row.get(0);
        let tag = u8::try_from(tag)
            .map_err(|_| fault("peer/tree-root-order", "tree root order is outside u8"))?;
        if tag > 3 {
            return Err(fault(
                "peer/tree-root-order",
                "tree root order is outside the four native indexes",
            ));
        }
        let order = order_from_tag(tag);
        let history: bool = root_row.get(1);
        let root_hash = digest(root_row.get(2), "tree root hash")?;
        let count = pg_basis(root_row.get(3), "tree root datom count")?;
        let encoded_bytes = pg_basis(root_row.get(4), "tree root encoded bytes")?;
        let root_payload: Vec<u8> = root_row.get(5);
        let described = manifest
            .tree(order, history)
            .ok_or_else(|| fault("peer/tree-root-coordinate", "manifest omitted a tree root"))?;
        if described.descriptor.root_hash != root_hash
            || described.descriptor.count != count
            || described.root_bytes != encoded_bytes
            || root_payload.len() as u64 != encoded_bytes
        {
            return Err(fault(
                "peer/tree-root-binding",
                "canonical and relational tree root bindings disagree",
            ));
        }
        let TreeNode::Root(root) = decode_tree_node(&root_hash, &root_payload)? else {
            return Err(fault(
                "peer/tree-root-kind",
                "tree manifest references a non-root node",
            ));
        };
        counters.root_reads.fetch_add(1, Ordering::Relaxed);
        if root.order != order
            || root.history != history
            || root.count != count
            || (count == 0) != root.directories.is_empty()
            || (count > 0
                && !root.directories.first().is_some_and(|first| {
                    described.descriptor.first_hash.is_some_and(|expected| {
                        crate::persistent_tree::routing_key_hash(&first.key)
                            .is_ok_and(|actual| actual == expected)
                    })
                }))
        {
            return Err(fault(
                "peer/tree-root-content",
                "decoded tree root disagrees with its manifest descriptor",
            ));
        }
        if roots
            .insert((history, order_tag(order)), Arc::new(root))
            .is_some()
        {
            return Err(fault(
                "peer/tree-root-duplicate",
                "tree manifest repeats a root coordinate",
            ));
        }
    }
    // Metadata is part of candidate validity. Missing or corrupt required
    // descendants reject this publication and let selection try the preceding
    // immutable publication.
    let metadata = Arc::new(derive_metadata_from_client(
        client, &roots, counters, cache,
    )?);
    validate_avet_work_directions(&manifest.pending_avet, &metadata.schema)?;
    Ok(TreeBase::new(manifest, manifest_hash, roots, metadata))
}

/// Load an exact retry-only archive. Archives never enter the ordinary
/// publication scan above: their sole purpose is to preserve the physical
/// db-before named by an immutable request receipt across portable restore.
fn load_required_request_base_archive<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    generation: u64,
    required_hash: Digest,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<TreeBase, SemanticError> {
    let row = client
        .query_opt(
            "SELECT archive.archive_revision, archive.basis_t, archive.tx_hash, \
                    archive.state_hash, archive.eidx_frontier, archive.manifest_hash, \
                    archive.payload, complete.manifest_hash IS NOT NULL, \
                    (((archive.basis_t = 0 AND bootstrap.tx_hash IS NOT NULL) \
                       OR (archive.basis_t > 0 AND native.tx_hash IS NOT NULL)) \
                      AND semantic.tx_hash IS NOT NULL) \
               FROM atomic_request_base_archives archive \
               JOIN atomic_databases catalog \
                 ON catalog.database_id = archive.database_id \
               LEFT JOIN atomic_request_base_archive_completions complete \
                 ON complete.manifest_hash = archive.manifest_hash \
               LEFT JOIN atomic_generation_transactions native \
                 ON archive.basis_t > 0 \
                AND native.database_id = archive.database_id \
                AND native.generation = archive.generation \
                AND native.basis_t = archive.basis_t \
                AND native.tx_hash = archive.tx_hash \
                AND native.state_hash = archive.state_hash \
                AND native.eidx_frontier = archive.eidx_frontier \
               LEFT JOIN atomic_semantic_commitment_roots bootstrap \
                 ON archive.basis_t = 0 \
                AND bootstrap.database_id = archive.database_id \
                AND bootstrap.generation = archive.generation \
                AND bootstrap.basis_t = 0 \
                AND bootstrap.tx_hash = archive.tx_hash \
                AND bootstrap.tx_hash = catalog.genesis_hash \
                AND bootstrap.state_hash = archive.state_hash \
                AND bootstrap.eidx_frontier = archive.eidx_frontier \
                AND bootstrap.commitment_version = 2 \
               LEFT JOIN atomic_semantic_commitment_roots semantic \
                 ON semantic.database_id = archive.database_id \
                AND semantic.generation = archive.generation \
                AND semantic.basis_t = archive.basis_t \
                AND semantic.tx_hash = archive.tx_hash \
                AND semantic.state_hash = archive.state_hash \
                AND semantic.eidx_frontier = archive.eidx_frontier \
                AND semantic.commitment_version = 2 \
              WHERE archive.database_id = $1 AND archive.generation = $2 \
                AND archive.basis_t <= $3 AND archive.manifest_hash = $4",
            &[
                &database_id,
                &sql_basis(generation)?,
                &sql_basis(through)?,
                &&required_hash[..],
            ],
        )
        .map_err(|error| postgres_error("peer/request-base-archive", error))?
        .ok_or_else(|| {
            fault(
                "peer/request-base-archive-absent",
                "required request-base archive disappeared during exact loading",
            )
        })?;
    let archive_revision = pg_basis(row.get(0), "request-base archive revision")?;
    let basis_t = pg_basis(row.get(1), "request-base archive basis")?;
    let tx_hash = digest(row.get(2), "request-base archive transaction hash")?;
    let state_hash = digest(row.get(3), "request-base archive state hash")?;
    let eidx_frontier = pg_basis(row.get(4), "request-base archive entity frontier")?;
    let manifest_hash = digest(row.get(5), "request-base archive manifest hash")?;
    let payload: Vec<u8> = row.get(6);
    let complete: bool = row.get(7);
    let authoritative: bool = row.get(8);
    if !complete || !authoritative || manifest_hash != required_hash {
        return Err(fault(
            "peer/request-base-archive-authority",
            "required request-base archive is incomplete or has no authoritative semantic coordinate",
        ));
    }
    if sha256(&payload) != manifest_hash {
        return Err(fault(
            "peer/request-base-archive-hash",
            "request-base archive bytes do not match their immutable hash",
        ));
    }
    let manifest = PersistentTreeManifest::decode(&payload)?;
    if manifest.database_id != database_id
        || manifest.publication_revision != archive_revision
        || manifest.basis_t != basis_t
        || manifest.tx_hash != tx_hash
        || manifest.state_hash != state_hash
        || manifest.excision_generation != generation
        || manifest.eidx_frontier != eidx_frontier
    {
        return Err(fault(
            "peer/request-base-archive-metadata",
            "canonical request-base archive disagrees with its authenticated SQL row",
        ));
    }

    let root_rows = client
        .query(
            "SELECT root.index_order, root.history, root.root_hash, root.datom_count, \
                    root.encoded_bytes, node.payload \
               FROM atomic_request_base_archive_roots root \
               JOIN atomic_tree_nodes node ON node.node_hash = root.root_hash \
              WHERE root.manifest_hash = $1 \
              ORDER BY root.history, root.index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("peer/request-base-archive-roots", error))?;
    if root_rows.len() != 8 {
        return Err(fault(
            "peer/request-base-archive-root-count",
            "request-base archive does not resolve to eight roots",
        ));
    }
    let mut roots = BTreeMap::new();
    for root_row in root_rows {
        let tag = u8::try_from(root_row.get::<_, i16>(0)).map_err(|_| {
            fault(
                "peer/request-base-archive-root-order",
                "request-base archive root order is outside u8",
            )
        })?;
        if tag > 3 {
            return Err(fault(
                "peer/request-base-archive-root-order",
                "request-base archive root order is outside the four native indexes",
            ));
        }
        let order = order_from_tag(tag);
        let history: bool = root_row.get(1);
        let root_hash = digest(root_row.get(2), "request-base archive root hash")?;
        let count = pg_basis(root_row.get(3), "request-base archive root datom count")?;
        let encoded_bytes = pg_basis(root_row.get(4), "request-base archive root bytes")?;
        let root_payload: Vec<u8> = root_row.get(5);
        let described = manifest.tree(order, history).ok_or_else(|| {
            fault(
                "peer/request-base-archive-root-coordinate",
                "request-base archive manifest omitted a tree root",
            )
        })?;
        if described.descriptor.root_hash != root_hash
            || described.descriptor.count != count
            || described.root_bytes != encoded_bytes
            || root_payload.len() as u64 != encoded_bytes
        {
            return Err(fault(
                "peer/request-base-archive-root-binding",
                "canonical and relational request-base archive roots disagree",
            ));
        }
        let TreeNode::Root(root) = decode_tree_node(&root_hash, &root_payload)? else {
            return Err(fault(
                "peer/request-base-archive-root-kind",
                "request-base archive references a non-root node",
            ));
        };
        counters.root_reads.fetch_add(1, Ordering::Relaxed);
        if root.order != order
            || root.history != history
            || root.count != count
            || (count == 0) != root.directories.is_empty()
            || (count > 0
                && !root.directories.first().is_some_and(|first| {
                    described.descriptor.first_hash.is_some_and(|expected| {
                        crate::persistent_tree::routing_key_hash(&first.key)
                            .is_ok_and(|actual| actual == expected)
                    })
                }))
        {
            return Err(fault(
                "peer/request-base-archive-root-content",
                "decoded request-base archive root disagrees with its descriptor",
            ));
        }
        if roots
            .insert((history, order_tag(order)), Arc::new(root))
            .is_some()
        {
            return Err(fault(
                "peer/request-base-archive-root-duplicate",
                "request-base archive repeats a root coordinate",
            ));
        }
    }

    // Restore completion already authenticated the immutable planned closure,
    // and archive-aware GC keeps every planned node alive until the binding is
    // retired. Opening an exact value therefore follows the same root-plus-
    // metadata-path boundary as an ordinary native publication. Descendants
    // are content-hash checked by the shared tree reader only when a query
    // reaches them; walking all eight trees here would make an old idempotent
    // retry O(total database) and eagerly fill the writer cache.
    let metadata = Arc::new(derive_metadata_from_client(
        client, &roots, counters, cache,
    )?);
    validate_avet_work_directions(&manifest.pending_avet, &metadata.schema)?;
    Ok(TreeBase::new(manifest, manifest_hash, roots, metadata))
}

fn load_latest_tree_base<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    excision_generation: u64,
    counters: &PeerLoadCounters,
    cache: &mut TreeNodeCache,
) -> Result<Option<TreeBase>, SemanticError> {
    match scan_latest_tree_base(
        client,
        database_id,
        through,
        excision_generation,
        None,
        counters,
        cache,
    )? {
        TreeBaseScan::Selected(base, _) => Ok(Some(*base)),
        TreeBaseScan::NoPublication(_) => Ok(None),
        TreeBaseScan::AllInvalid(stats) => Err(fault(
            "peer/all-native-publications-invalid",
            format!(
                "the generation has published native roots, but every examined candidate failed authenticated loading (examined={}, rejected={})",
                stats.examined_candidates, stats.rejected_candidates
            ),
        )),
        TreeBaseScan::RequiredCollecting(_) => Err(fault(
            "peer/unexpected-required-tree-scan",
            "an unqualified native-base scan reported a required manifest state",
        )),
    }
}

fn exact_tree_selection(
    scan: TreeBaseScan,
    required_manifest: Option<Digest>,
) -> Result<(TreeBase, TreeBaseScanStats), SemanticError> {
    match scan {
        TreeBaseScan::Selected(base, stats) => Ok((*base, stats)),
        TreeBaseScan::NoPublication(stats) => {
            let (code, message) = if required_manifest.is_some() {
                (
                    "peer/exact-manifest-absent",
                    "the required native manifest is not published at or before the exact endpoint",
                )
            } else {
                (
                    "peer/exact-no-native-publication",
                    "the named generation has no native publication at or before the exact endpoint",
                )
            };
            Err(SemanticError::new(
                ErrorCategory::Unavailable,
                code,
                format!(
                    "{message} (examined={}, rejected={})",
                    stats.examined_candidates, stats.rejected_candidates
                ),
            ))
        }
        TreeBaseScan::AllInvalid(stats) => {
            let (code, message) = if required_manifest.is_some() {
                (
                    "peer/exact-manifest-corrupt",
                    "the required native manifest failed authenticated loading",
                )
            } else {
                (
                    "peer/exact-all-native-publications-invalid",
                    "every native publication for the named generation failed authenticated loading",
                )
            };
            Err(fault(
                code,
                format!(
                    "{message} (examined={}, rejected={})",
                    stats.examined_candidates, stats.rejected_candidates
                ),
            ))
        }
        TreeBaseScan::RequiredCollecting(stats) => Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "peer/exact-manifest-collecting",
            format!(
                "the required native manifest is already being collected (examined={}, rejected={})",
                stats.examined_candidates, stats.rejected_candidates
            ),
        )),
    }
}

fn exact_pin_error(error: SemanticError, required_manifest: Option<Digest>) -> SemanticError {
    if required_manifest.is_some() && error.code == "peer/root-retired-during-load" {
        SemanticError::new(
            ErrorCategory::Unavailable,
            "peer/exact-manifest-collecting",
            "the required native manifest began collection before its snapshot pin was acquired",
        )
    } else {
        error
    }
}

struct ExactTieredBuild<'a> {
    database_id: &'a str,
    endpoint: ExactEndpoint,
    base: TreeBase,
    generation_pin: Arc<GenerationPin>,
    root_pin: Option<Arc<RootPin>>,
    recent_limits: RecentLimits,
    purpose: ExactOpenPurpose,
    local_generation: u64,
    counters: &'a PeerLoadCounters,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum ExactOpenPurpose {
    ImmutableRead,
    WriterActivation,
}

#[derive(Clone, Copy, Debug)]
struct ExactOpenConfiguration {
    cache_entries: usize,
    cache_bytes: usize,
    recent_limits: RecentLimits,
    purpose: ExactOpenPurpose,
}

fn build_exact_tiered_state<C: GenericClient>(
    client: &mut C,
    build: ExactTieredBuild<'_>,
    tree_cache: &mut TreeNodeCache,
) -> Result<(TieredState, u64, u64), SemanticError> {
    let ExactTieredBuild {
        database_id,
        endpoint,
        base,
        generation_pin,
        root_pin,
        recent_limits,
        purpose,
        local_generation,
        counters,
    } = build;
    let base_metadata = Arc::clone(&base.metadata);
    let tail = read_authenticated_tail(
        client,
        database_id,
        endpoint.generation,
        TailBase {
            basis_t: base.manifest.basis_t,
            tx_hash: base.manifest.tx_hash,
            state_hash: base.manifest.state_hash,
            eidx_frontier: base.manifest.eidx_frontier,
        },
        endpoint.basis_t,
        (purpose == ExactOpenPurpose::WriterActivation).then_some(recent_limits),
    )?;
    if tail.end_hash != endpoint.tx_hash
        || tail.end_state_hash != endpoint.state_hash
        || tail.eidx_frontier != endpoint.eidx_frontier
    {
        return Err(fault(
            "peer/exact-endpoint-mismatch",
            "native base plus authenticated generation tail does not reach every requested endpoint coordinate",
        ));
    }
    let tail_transactions = tail.transactions.len() as u64;
    let tail_range_reads = tail.range_reads;
    let (metadata, avet_unready) = apply_metadata_and_avet_readiness(
        &base_metadata,
        &manifest_avet_unready(&base.manifest),
        &tail.transactions,
        |attribute| tree_base_has_attribute_history(client, &base, attribute, counters, tree_cache),
    )?;
    let metadata = Arc::new(metadata);
    let avet_unready = Arc::new(avet_unready);
    let recent = Arc::new(RecentTier::new_authenticated_existing(
        database_id,
        base.manifest.basis_t,
        base.manifest.tx_hash,
        tail.transaction_hashes.into_iter().zip(tail.transactions),
        metadata.endpoint(),
        recent_limits,
    )?);
    let durable_base_t = base.manifest.basis_t;
    Ok((
        TieredState {
            basis_t: endpoint.basis_t,
            eidx_frontier: endpoint.eidx_frontier,
            current_hash: endpoint.tx_hash,
            current_state_hash: endpoint.state_hash,
            excision_generation: endpoint.generation,
            durable_base_t,
            tree_base: Some(Arc::new(base)),
            _root_pin: root_pin,
            _generation_pin: generation_pin,
            recent,
            metadata,
            avet_unready,
            generation: local_generation,
        },
        tail_transactions,
        tail_range_reads,
    ))
}

fn exact_open_stats(
    state: &TieredState,
    scan: TreeBaseScanStats,
    tail_transactions: u64,
    tail_range_reads: u64,
) -> Result<ExactOpenStats, SemanticError> {
    let base = state.tree_base.as_ref().ok_or_else(|| {
        fault(
            "peer/exact-native-base-lost",
            "strict native construction produced no durable tree base",
        )
    })?;
    Ok(ExactOpenStats {
        examined_candidates: scan.examined_candidates,
        rejected_candidates: scan.rejected_candidates,
        selected_publication_revision: base.manifest.publication_revision,
        selected_manifest_hash: base.manifest_hash,
        tail_transactions,
        tail_range_reads,
    })
}

struct AuthenticatedTail {
    transactions: Vec<DurableTransaction>,
    transaction_hashes: Vec<Digest>,
    state_hashes: Vec<Digest>,
    end_hash: Digest,
    end_state_hash: Digest,
    eidx_frontier: u64,
    range_reads: u64,
}

#[derive(Clone, Copy)]
struct TailBase {
    basis_t: u64,
    tx_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
}

/// Authenticate the canonical transaction chain without constructing the
/// kernel's full current/history indexes. `state_hash` is carried as an
/// endpoint assertion and is verified when compatibility materialization is
/// requested. It cannot be recomputed cheaply from a transaction delta until
/// the semantic commitment's persistent update witness is stored with native
/// manifests; transaction payload hashes and predecessor links remain fully
/// authenticated here.
fn read_authenticated_tail<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    log_generation: u64,
    base: TailBase,
    target_t: u64,
    writer_limits: Option<RecentLimits>,
) -> Result<AuthenticatedTail, SemanticError> {
    if target_t < base.basis_t {
        return Err(fault(
            "peer/tail-target-before-base",
            "requested tail endpoint precedes its native base",
        ));
    }
    if target_t == base.basis_t {
        return Ok(AuthenticatedTail {
            transactions: Vec::new(),
            transaction_hashes: Vec::new(),
            state_hashes: Vec::new(),
            end_hash: base.tx_hash,
            end_state_hash: base.state_hash,
            eidx_frontier: base.eidx_frontier,
            range_reads: 0,
        });
    }
    let tail_transactions = target_t - base.basis_t;
    let mut end_hash = base.tx_hash;
    let mut end_state_hash = base.state_hash;
    let mut eidx_frontier = base.eidx_frontier;
    let mut transactions = Vec::new();
    let mut transaction_hashes = Vec::new();
    let mut state_hashes = Vec::new();
    let mut scanned_transactions = 0_u64;
    let mut datoms = 0_u64;
    let mut accounted_bytes = 0_u64;
    visit_authenticated_log_range(
        client,
        database_id,
        log_generation,
        base.basis_t,
        target_t,
        base.tx_hash,
        |row| {
            let state_hash = row.state_hash;
            if state_hash == [0; 32] {
                return Err(fault(
                    "peer/missing-state-commitment",
                    "native peer tail row has no semantic state commitment",
                ));
            }
            let transaction = row.transaction;
            if transaction.eidx_frontier < eidx_frontier {
                return Err(fault(
                    "peer/tail-frontier-regressed",
                    "native transaction entity frontier moved backwards",
                ));
            }
            if let Some(limits) = writer_limits {
                let retained = retained_entry_stats(&transaction)?;
                scanned_transactions = scanned_transactions.checked_add(1).ok_or_else(|| {
                    fault(
                        "peer/writer-preflight-count-overflow",
                        "writer-tail preflight transaction count overflow",
                    )
                })?;
                datoms = datoms.checked_add(retained.datoms).ok_or_else(|| {
                    fault(
                        "peer/writer-preflight-count-overflow",
                        "writer-tail preflight datom count overflow",
                    )
                })?;
                accounted_bytes = accounted_bytes
                    .checked_add(retained.accounted_bytes)
                    .ok_or_else(|| {
                        fault(
                            "peer/writer-preflight-count-overflow",
                            "writer-tail preflight resident-byte count overflow",
                        )
                    })?;
                if datoms > limits.hard_datoms || accounted_bytes > limits.hard_bytes {
                    return Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "recent/hard-capacity",
                    format!(
                        "writer startup recent tail crosses hard capacity after {scanned_transactions} of {tail_transactions} transactions ({datoms} datoms/{accounted_bytes} accounted resident bytes, limits {}/{})",
                        limits.hard_datoms, limits.hard_bytes
                    ),
                )
                .detail("preflight_transactions", scanned_transactions.to_string())
                .detail("preflight_datoms", datoms.to_string())
                .detail("preflight_accounted_bytes", accounted_bytes.to_string())
                .detail("tail_transactions", tail_transactions.to_string())
                .detail("preflight_stopped_basis_t", transaction.basis_t.to_string())
                .detail("preflight_range_reads", "1"));
                }
            }
            eidx_frontier = transaction.eidx_frontier;
            end_hash = row.tx_hash;
            end_state_hash = state_hash;
            transaction_hashes.push(row.tx_hash);
            state_hashes.push(state_hash);
            transactions.push(transaction);
            Ok(())
        },
    )?;
    Ok(AuthenticatedTail {
        transactions,
        transaction_hashes,
        state_hashes,
        end_hash,
        end_state_hash,
        eidx_frontier,
        range_reads: 1,
    })
}

fn read_state_hash<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    log_generation: u64,
    basis_t: u64,
) -> Result<Digest, SemanticError> {
    if basis_t == 0 && log_generation == 0 {
        return Ok([0; 32]);
    }
    let row = if basis_t == 0 {
        client.query_opt(
            "SELECT state_hash FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2 AND basis_t = 0",
            &[&database_id, &sql_basis(log_generation)?],
        )
    } else if log_generation == 0 {
        client.query_opt(
            "SELECT state_hash FROM atomic_transactions \
              WHERE database_id = $1 AND basis_t = $2",
            &[&database_id, &sql_basis(basis_t)?],
        )
    } else {
        client.query_opt(
            "SELECT state_hash FROM atomic_generation_transactions \
              WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
            &[
                &database_id,
                &sql_basis(log_generation)?,
                &sql_basis(basis_t)?,
            ],
        )
    }
    .map_err(|error| postgres_error("peer/state-hash", error))?
    .ok_or_else(|| {
        fault(
            "peer/missing-state-hash",
            "snapshot endpoint row is missing",
        )
    })?;
    digest(row.get(0), "snapshot state commitment")
}

fn load_latest_base<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    cache: &mut SegmentCache,
) -> Result<Option<LoadedBase>, SemanticError> {
    Ok(load_latest_base_with_stats(client, database_id, through, cache)?.0)
}

fn load_latest_base_with_stats<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    cache: &mut SegmentCache,
) -> Result<BaseSelection, SemanticError> {
    let through_sql = sql_basis(through)?;
    let rows = client
        .query(
            "SELECT m.basis_t, m.tx_hash, m.manifest_hash, m.payload, \
                    p.manifest_hash IS NOT NULL AS published \
             FROM atomic_index_manifests m \
             LEFT JOIN atomic_index_publications p \
               ON p.database_id = m.database_id \
              AND p.basis_t = m.basis_t \
              AND p.tx_hash = m.tx_hash \
              AND p.manifest_hash = m.manifest_hash \
             WHERE m.database_id = $1 AND m.basis_t <= $2 \
             ORDER BY m.basis_t DESC",
            &[&database_id, &through_sql],
        )
        .map_err(|error| postgres_error("peer/index-manifests", error))?;
    let mut rejected = 0_u64;
    for row in rows {
        let basis = pg_basis(row.get(0), "index manifest")?;
        let tx_hash = digest(row.get(1), "index transaction hash")?;
        let manifest_hash = digest(row.get(2), "manifest hash")?;
        let payload: Vec<u8> = row.get(3);
        let published: bool = row.get(4);
        if !published {
            rejected += 1;
            continue;
        }
        if sha256(&payload) != manifest_hash {
            rejected += 1;
            continue;
        }
        let Ok(manifest) = decode_index_manifest(&payload) else {
            rejected += 1;
            continue;
        };
        if manifest.database_id != database_id
            || manifest.basis_t != basis
            || manifest.tx_hash != tx_hash
        {
            rejected += 1;
            continue;
        }
        if let Ok(database) = load_manifest_database(client, &manifest, cache) {
            return Ok((Some((database, tx_hash, basis)), rejected));
        }
        rejected += 1;
    }
    Ok((None, rejected))
}

fn load_manifest_database<C: GenericClient>(
    client: &mut C,
    manifest: &IndexManifest,
    cache: &mut SegmentCache,
) -> Result<Database, SemanticError> {
    let mut lists = BTreeMap::<(bool, u8), Vec<Datom>>::new();
    for reference in &manifest.segments {
        let segment = if let Some(segment) = cache.get(&reference.hash) {
            segment
        } else {
            let row = client
                .query_opt(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&reference.hash[..]],
                )
                .map_err(|error| postgres_error("peer/index-segment", error))?
                .ok_or_else(|| {
                    fault(
                        "index/missing-segment",
                        "manifest references a missing segment",
                    )
                })?;
            let payload: Vec<u8> = row.get(0);
            if sha256(&payload) != reference.hash {
                return Err(fault(
                    "index/segment-checksum",
                    "segment row does not match its hash",
                ));
            }
            let decoded = Arc::new(decode_index_segment(&payload)?);
            cache.insert(reference.hash, Arc::clone(&decoded));
            decoded
        };
        if segment.order != reference.order
            || segment.history != reference.history
            || segment.datoms.len() != reference.count as usize
        {
            return Err(fault(
                "index/segment-reference-mismatch",
                "segment metadata disagrees with its manifest reference",
            ));
        }
        lists
            .entry((reference.history, order_tag(reference.order)))
            .or_default()
            .extend(segment.datoms.iter().cloned());
    }
    for ((_, tag), datoms) in &lists {
        let order = order_from_tag(*tag);
        if datoms
            .windows(2)
            .any(|pair| pair[0].cmp_in(&pair[1], order).is_gt())
        {
            return Err(fault(
                "index/cross-segment-order",
                "combined segments are not ordered",
            ));
        }
    }
    let current = lists.remove(&(false, 0)).unwrap_or_default();
    let history = lists.remove(&(true, 0)).unwrap_or_default();
    let mut genesis = Vec::new();
    let mut history_chunks = vec![Vec::new(); manifest.basis_t as usize];
    for datom in history {
        let t = tx_to_t(datom.tx).map_err(|error| {
            fault(
                "index/datom-transaction",
                format!("historical segment contains an invalid transaction id: {error}"),
            )
        })?;
        if t > manifest.basis_t {
            return Err(fault(
                "index/datom-basis",
                "historical segment contains an out-of-range transaction",
            ));
        }
        if t == 0 {
            genesis.push(datom);
        } else {
            history_chunks[(t - 1) as usize].push(datom);
        }
    }
    let database = Database::from_index_base(
        manifest.basis_t,
        manifest.eidx_frontier,
        current,
        genesis,
        history_chunks,
    )?;
    // Index-time noHistory filtering must be idempotent at this endpoint. This
    // deliberately says nothing about closed intervals: an interval that was
    // not consolidated before the flag became false remains ordinary history.
    let (_, canonical_history) = checkpoint_information(&database)?;
    let retained_history = database.datoms(View::History, IndexOrder::Eavt);
    if !same_stored_datoms(&canonical_history, &retained_history) {
        return Err(fault(
            "index/noncanonical-no-history",
            "persistent index base is not idempotent under endpoint :db/noHistory consolidation",
        ));
    }
    let catalog = client
        .query_opt(
            "SELECT genesis_hash FROM atomic_databases WHERE database_id = $1",
            &[&manifest.database_id],
        )
        .map_err(|error| postgres_error("peer/index-genesis-catalog", error))?
        .ok_or_else(|| {
            fault(
                "index/missing-database",
                "manifest database no longer exists",
            )
        })?;
    let expected_genesis = digest(catalog.get(0), "catalog genesis hash")?;
    let actual_genesis = sha256(&encode_genesis(database.genesis_datoms())?);
    if actual_genesis != expected_genesis {
        return Err(fault(
            "index/genesis-mismatch",
            "persistent index base is not rooted in the catalog genesis",
        ));
    }
    let basis = sql_basis(manifest.basis_t)?;
    let authoritative = client
        .query_opt(
            "SELECT tx_hash, state_hash FROM atomic_transactions \
             WHERE database_id = $1 AND basis_t = $2",
            &[&manifest.database_id, &basis],
        )
        .map_err(|error| postgres_error("peer/index-authoritative-transaction", error))?
        .ok_or_else(|| {
            fault(
                "index/missing-authoritative-transaction",
                "manifest basis has no authoritative log transaction",
            )
        })?;
    if digest(authoritative.get(0), "authoritative transaction hash")? != manifest.tx_hash {
        return Err(fault(
            "index/transaction-hash-mismatch",
            "manifest does not identify the authoritative log transaction",
        ));
    }
    let expected_state = digest(authoritative.get(1), "authoritative state commitment")?;
    if expected_state == [0; 32] {
        return Err(fault(
            "index/missing-state-commitment",
            "manifest basis predates authoritative state commitments",
        ));
    }
    if !verify_checkpoint_state_hash(&database, expected_state)?.matches() {
        return Err(fault(
            "index/state-commitment-mismatch",
            "persistent index base does not match the authoritative transaction state",
        ));
    }
    for history in [false, true] {
        for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
            let actual = lists
                .remove(&(history, order_tag(order)))
                .unwrap_or_default();
            let expected = database.datoms(
                if history {
                    View::History
                } else {
                    View::Current
                },
                order,
            );
            if actual != expected {
                return Err(fault(
                    "index/redundant-root-mismatch",
                    "persistent index orders disagree",
                ));
            }
        }
    }
    Ok(database)
}

fn apply_tail<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    log_generation: u64,
    database: &mut Database,
    previous_hash: &mut Digest,
    target: u64,
) -> Result<Vec<DurableTransaction>, SemanticError> {
    let rows = read_authenticated_log_range(
        client,
        database_id,
        log_generation,
        database.basis_t(),
        target,
        *previous_hash,
    )?;
    let mut reports = Vec::with_capacity(rows.len());
    let mut target_state = None;
    for row in rows {
        *database = if row.excision_replay {
            database.apply_excised_committed(&row.transaction)?
        } else {
            database.apply_committed(&row.transaction)?
        };
        target_state = Some(row.state_hash);
        *previous_hash = row.tx_hash;
        reports.push(row.transaction);
    }
    // The canonical transaction hash chain authenticates every intermediate
    // tail delta; only the requested tail endpoint needs an O(N) state hash.
    // Any intermediate commitment becomes mandatory if that basis is itself
    // requested as a recovery target or index base.
    if let Some(expected) = target_state
        && expected != [0; 32]
        && !verify_checkpoint_state_hash(database, expected)?.matches()
    {
        return Err(fault(
            "peer/tail-state-commitment-mismatch",
            "tail endpoint does not produce its authoritative database state",
        ));
    }
    Ok(reports)
}

fn index_member(database: &Database, datom: &Datom, order: IndexOrder) -> bool {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => true,
        IndexOrder::Avet => database
            .schema()
            .attribute(datom.attribute)
            .is_ok_and(|a| a.indexed || a.unique.is_some()),
        IndexOrder::Vaet => database
            .schema()
            .attribute(datom.attribute)
            .is_ok_and(|a| a.value_type == crate::ValueType::Ref),
    }
}

fn same_stored_datoms(left: &[Datom], right: &[Datom]) -> bool {
    left.len() == right.len()
        && left.iter().zip(right).all(|(left, right)| {
            left.entity == right.entity
                && left.attribute == right.attribute
                && left.tx == right.tx
                && left.added == right.added
                && left.value.stored_eq(&right.value)
        })
}

fn read_head<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<(u64, Digest), SemanticError> {
    let row = client
        .query_opt(
            "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("peer/head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/database-not-found",
                format!("database {database_id} does not exist"),
            )
        })?;
    Ok((
        pg_basis(row.get(0), "head")?,
        digest(row.get(1), "head hash")?,
    ))
}

fn read_database_lineage<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<String, SemanticError> {
    client
        .query_opt(
            "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("peer/database-lineage", error))?
        .map(|row| row.get(0))
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/database-not-found",
                format!("database {database_id} does not exist"),
            )
        })
}

fn verify_database_lineage<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    expected_lineage: &str,
) -> Result<(), SemanticError> {
    let actual = read_database_lineage(client, database_id)?;
    if actual == expected_lineage {
        return Ok(());
    }
    Err(fault(
        "peer/database-lineage-changed",
        "database catalog address now names a different durable lineage",
    ))
}

fn order_tag(order: IndexOrder) -> u8 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}
fn order_from_tag(tag: u8) -> IndexOrder {
    match tag {
        0 => IndexOrder::Eavt,
        1 => IndexOrder::Aevt,
        2 => IndexOrder::Avet,
        3 => IndexOrder::Vaet,
        _ => unreachable!(),
    }
}
fn sql_basis(value: u64) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "postgres/basis-out-of-range",
            "PostgreSQL BIGINT cannot represent this basis",
        )
    })
}
fn pg_basis(value: i64, record: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        fault(
            "index/negative-basis",
            format!("{record} contains a negative basis"),
        )
    })
}
fn digest(bytes: Vec<u8>, record: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|bytes: Vec<u8>| {
        fault(
            "index/invalid-digest",
            format!("{record} has {} bytes", bytes.len()),
        )
    })
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, TxValue, Value, ValueType,
    };
    use postgres::NoTls;
    use std::time::{SystemTime, UNIX_EPOCH};

    fn unique_database(prefix: &str) -> String {
        format!(
            "{prefix}_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        )
    }

    #[test]
    fn bulk_merge_preloads_each_directory_and_leaf_coordinate_once() {
        let datoms = (0..512)
            .map(|index| Datom {
                entity: crate::make_eid(crate::USER_PARTITION, index + 1).unwrap(),
                attribute: 1_000,
                value: Value::String("x".repeat(128)),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        let config = TreeConfig {
            max_leaf_datoms: 16,
            max_leaves_per_directory: 4,
            ..TreeConfig::default()
        };
        let built = build_tree(IndexOrder::Eavt, true, datoms.clone(), &config).unwrap();
        let TreeNode::Root(root) = decode_tree_node(
            &built.descriptor.root_hash,
            built.nodes.get(&built.descriptor.root_hash).unwrap(),
        )
        .unwrap() else {
            panic!("root expected")
        };
        let mut directory_loads = BTreeMap::<Digest, usize>::new();
        let selected = select_merge_leaves(&root, &datoms, |reference| {
            *directory_loads.entry(reference.hash).or_default() += 1;
            let TreeNode::Directory(directory) =
                decode_tree_node(&reference.hash, built.nodes.get(&reference.hash).unwrap())?
            else {
                panic!("directory expected")
            };
            Ok(directory)
        })
        .unwrap();
        assert_eq!(directory_loads.len(), root.directories.len());
        assert!(directory_loads.values().all(|loads| *loads == 1));
        assert_eq!(
            selected.leaves.len(),
            selected
                .directories
                .values()
                .map(|directory| directory.leaves.len())
                .sum::<usize>()
        );
        assert!(selected.leaves.len() < datoms.len() / 8);
    }

    #[test]
    fn pending_avet_direction_is_bound_to_reconstructed_schema() {
        let mut schema = Schema::new();
        let mut indexed = Attribute::new(
            1_000,
            Keyword::new("item", "indexed"),
            ValueType::Long,
            Cardinality::One,
        );
        indexed.indexed = true;
        schema.install(indexed).unwrap();
        schema
            .install(Attribute::new(
                1_001,
                Keyword::new("item", "plain"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();

        validate_avet_work_directions(&[AvetProjectionWork::new(1_000, true)], &schema).unwrap();
        validate_avet_work_directions(&[AvetProjectionWork::new(1_001, false)], &schema).unwrap();
        for invalid in [
            AvetProjectionWork::new(1_001, true),
            AvetProjectionWork::new(1_000, false),
            AvetProjectionWork::new(1_002, true),
        ] {
            assert_eq!(
                validate_avet_work_directions(&[invalid], &schema)
                    .unwrap_err()
                    .code,
                "tree/pending-avet-schema-mismatch"
            );
        }
    }

    #[test]
    fn reverse_full_t_boundary_routes_after_all_operation_and_stored_value_ties() {
        use bigdecimal::BigDecimal;
        use std::str::FromStr;

        let entity = crate::make_eid(crate::USER_PARTITION, 1).unwrap();
        let mut datoms = (1..=40)
            .map(|t| Datom {
                entity,
                attribute: 1_000,
                value: Value::BigDec(BigDecimal::from_str("1").unwrap()),
                tx: crate::t_to_tx(t).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        datoms.extend([
            Datom {
                value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
                tx: crate::t_to_tx(20).unwrap(),
                ..datoms[0].clone()
            },
            Datom {
                value: Value::BigDec(BigDecimal::from_str("1.000").unwrap()),
                tx: crate::t_to_tx(20).unwrap(),
                added: false,
                ..datoms[0].clone()
            },
        ]);
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let config = TreeConfig {
            max_leaf_datoms: 2,
            target_leaf_bytes: 512,
            max_leaf_bytes: 2_048,
            max_leaves_per_directory: 2,
            max_directory_bytes: 2_048,
            max_directories_per_root: 64,
            max_root_bytes: 64 * 1_024,
        };
        let build = build_tree(IndexOrder::Eavt, true, datoms.clone(), &config).unwrap();
        let root = match decode_tree_node(
            &build.descriptor.root_hash,
            build.nodes.get(&build.descriptor.root_hash).unwrap(),
        )
        .unwrap()
        {
            TreeNode::Root(root) => root,
            _ => panic!("tree descriptor must address a root"),
        };
        assert!(root.directories.len() > 1);

        let normalized = IndexBoundary::Eavt(IndexComponents::Four(
            entity,
            1_000,
            Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
            crate::IndexTransaction::T(20),
        ))
        .normalized()
        .unwrap();
        let boundary = TreeBoundary::new(normalized);
        let expected_lower =
            datoms.partition_point(|datom| boundary.normalized.compare_datom(datom).is_lt());
        let expected_upper =
            datoms.partition_point(|datom| !boundary.normalized.compare_datom(datom).is_gt());
        assert_eq!(
            datoms[expected_lower..expected_upper]
                .iter()
                .filter(|datom| datom.tx == crate::t_to_tx(20).unwrap())
                .count(),
            3,
            "the virtual T lower bound must precede operation and scale ties"
        );
        assert_eq!(
            datoms[..expected_upper]
                .iter()
                .filter(|datom| datom.tx == crate::t_to_tx(20).unwrap())
                .count(),
            3,
            "the virtual T upper bound must include assertion/retraction and scale ties"
        );

        let directory_index = boundary_floor_child(&root.directories, &boundary).unwrap();
        let directory = match decode_tree_node(
            &root.directories[directory_index].hash,
            build
                .nodes
                .get(&root.directories[directory_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Directory(directory) => directory,
            _ => panic!("root child must be a directory"),
        };
        let leaf_index = boundary_floor_child(&directory.leaves, &boundary).unwrap();
        let leaf = match decode_tree_node(
            &directory.leaves[leaf_index].hash,
            build.nodes.get(&directory.leaves[leaf_index].hash).unwrap(),
        )
        .unwrap()
        {
            TreeNode::Leaf(leaf) => leaf,
            _ => panic!("directory child must be a leaf"),
        };
        let local_upper = leaf_reverse_upper_bound(&leaf, &boundary);
        let global_upper = root.directories[..directory_index]
            .iter()
            .map(|child| child.count as usize)
            .sum::<usize>()
            + directory.leaves[..leaf_index]
                .iter()
                .map(|child| child.count as usize)
                .sum::<usize>()
            + local_upper;
        assert_eq!(global_upper, expected_upper);

        let forward_directory_index = boundary_start_child(&root.directories, &boundary);
        let forward_directory = match decode_tree_node(
            &root.directories[forward_directory_index].hash,
            build
                .nodes
                .get(&root.directories[forward_directory_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Directory(directory) => directory,
            _ => panic!("root child must be a directory"),
        };
        let forward_leaf_index = boundary_start_child(&forward_directory.leaves, &boundary);
        let forward_leaf = match decode_tree_node(
            &forward_directory.leaves[forward_leaf_index].hash,
            build
                .nodes
                .get(&forward_directory.leaves[forward_leaf_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Leaf(leaf) => leaf,
            _ => panic!("directory child must be a leaf"),
        };
        let global_lower = root.directories[..forward_directory_index]
            .iter()
            .map(|child| child.count as usize)
            .sum::<usize>()
            + forward_directory.leaves[..forward_leaf_index]
                .iter()
                .map(|child| child.count as usize)
                .sum::<usize>()
            + leaf_boundary_lower_bound(&forward_leaf, &boundary);
        assert_eq!(global_lower, expected_lower);
    }

    #[test]
    fn exact_native_scan_states_and_endpoint_validation_are_distinct() {
        let stats = TreeBaseScanStats {
            examined_candidates: 2,
            rejected_candidates: 2,
        };
        assert_eq!(
            exact_tree_selection(TreeBaseScan::NoPublication(Default::default()), None)
                .err()
                .expect("no publication must fail")
                .code,
            "peer/exact-no-native-publication"
        );
        assert_eq!(
            exact_tree_selection(
                TreeBaseScan::NoPublication(Default::default()),
                Some([1; 32])
            )
            .err()
            .expect("a missing required publication must fail")
            .code,
            "peer/exact-manifest-absent"
        );
        assert_eq!(
            exact_tree_selection(TreeBaseScan::AllInvalid(stats), None)
                .err()
                .expect("all-invalid publications must fail")
                .code,
            "peer/exact-all-native-publications-invalid"
        );
        assert_eq!(
            exact_tree_selection(TreeBaseScan::AllInvalid(stats), Some([1; 32]))
                .err()
                .expect("an invalid required publication must fail")
                .code,
            "peer/exact-manifest-corrupt"
        );
        assert_eq!(
            exact_tree_selection(TreeBaseScan::RequiredCollecting(stats), Some([1; 32]))
                .err()
                .expect("a collecting required publication must fail")
                .code,
            "peer/exact-manifest-collecting"
        );
        assert_eq!(
            ExactEndpoint {
                generation: 1,
                basis_t: 0,
                tx_hash: [1; 32],
                state_hash: [0; 32],
                eidx_frontier: Database::bootstrap().unwrap().eidx_frontier(),
            }
            .validate()
            .unwrap_err()
            .code,
            "peer/exact-endpoint-zero-state"
        );
    }

    #[test]
    fn ordinary_wide_schema_metadata_transition_reuses_the_projection() {
        let mut schema = Schema::new();
        for attribute in 1_000..1_128 {
            schema
                .install(Attribute::new(
                    attribute,
                    Keyword::new("wide", format!("attribute-{attribute}")),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
        }
        let database = Database::new(schema).unwrap();
        let assessed = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("entity".into()),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(1)),
                }],
                10,
            )
            .unwrap();
        let metadata = MetadataProjection::from_database(&database).unwrap();
        let transaction = DurableTransaction {
            database_id: "wide-schema".into(),
            basis_t: assessed.db_after.basis_t(),
            previous_hash: [7; 32],
            eidx_frontier: assessed.db_after.eidx_frontier(),
            tempids: assessed.tempids,
            tx_data: assessed.tx_data,
        };
        let (endpoint, unready) =
            apply_metadata_and_avet_readiness(&metadata, &BTreeSet::new(), &[transaction], |_| {
                panic!("ordinary data must not perform an AVET history probe")
            })
            .unwrap();
        assert!(Arc::ptr_eq(&metadata.schema, &endpoint.schema));
        assert!(Arc::ptr_eq(&metadata.idents, &endpoint.idents));
        assert!(Arc::ptr_eq(
            &metadata.schema_current,
            &endpoint.schema_current
        ));
        assert!(unready.is_empty());
        let resident = endpoint.resident_stats();
        assert_eq!(
            resident.schema_attributes,
            database.schema().attributes().count()
        );
        assert!(resident.schema_information_datoms >= 128);
        assert!(resident.schema_estimated_bytes > 0);
        assert!(resident.ident_names >= 128);
        assert!(resident.ident_entities >= 128);
        assert!(resident.ident_estimated_bytes > 0);
    }

    #[test]
    fn strict_native_open_and_rebase_preserve_the_exact_endpoint_and_pins() {
        let Some(connection) = std::env::var("ATOMIC_POSTGRES_URL").ok() else {
            return;
        };
        let mut migrator = crate::PostgresMigrator::connect(&connection).unwrap();
        migrator.migrate().unwrap();

        let database_id = unique_database("exact_native_open");
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("exact", "name"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        let mut store = crate::PostgresStore::connect(&connection).unwrap();
        store.create_database(&database_id, schema).unwrap();
        drop(store);

        let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
        let receipt = indexer.consolidate().unwrap();
        let peer = Peer::connect(&connection, &database_id, 64).unwrap();
        let endpoint = peer.tiered_snapshot().endpoint();
        let (exact, opened) = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            Some(receipt.manifest_hash),
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(exact.endpoint(), endpoint);
        assert_eq!(exact.durable_manifest_hash(), Some(receipt.manifest_hash));
        assert_eq!(opened.selected_manifest_hash, receipt.manifest_hash);
        assert_eq!(
            opened.selected_publication_revision,
            receipt.publication_revision
        );
        assert_eq!(opened.rejected_candidates, 0);

        let (rebased, rebased_stats) = exact.rebase_exact(Some(receipt.manifest_hash)).unwrap();
        assert_eq!(rebased.endpoint(), endpoint);
        assert_eq!(rebased.durable_manifest_hash(), Some(receipt.manifest_hash));
        assert_eq!(rebased_stats.selected_manifest_hash, receipt.manifest_hash);
        assert!(!Arc::ptr_eq(&exact.state, &rebased.state));
        assert_eq!(
            lock(&exact.core.root_pins.state)
                .counts
                .get(&receipt.manifest_hash),
            Some(&2)
        );
        assert_eq!(
            exact.datoms(false, IndexOrder::Eavt).unwrap().datoms,
            rebased.datoms(false, IndexOrder::Eavt).unwrap().datoms
        );
        assert_eq!(exact.endpoint(), endpoint, "the old value did not advance");
        drop(rebased);
        assert_eq!(
            lock(&exact.core.root_pins.state)
                .counts
                .get(&receipt.manifest_hash),
            Some(&1)
        );
        assert!(exact.tree_cache_stats().current_entries <= 64);

        let absent = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            Some([0xff; 32]),
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .err()
        .expect("an absent required manifest must fail");
        assert_eq!(absent.code, "peer/exact-manifest-absent");

        let unpublished_id = unique_database("exact_native_unpublished");
        let mut unpublished_store = crate::PostgresStore::connect(&connection).unwrap();
        let unpublished = unpublished_store
            .create_database(&unpublished_id, Schema::new())
            .unwrap();
        let no_publication = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &unpublished_id,
            ExactEndpoint {
                generation: 0,
                basis_t: unpublished.basis_t(),
                tx_hash: [1; 32],
                state_hash: [1; 32],
                eidx_frontier: unpublished.eidx_frontier(),
            },
            None,
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .err()
        .expect("a generation without a publication must fail");
        assert_eq!(no_publication.code, "peer/exact-no-native-publication");
    }

    #[test]
    fn exact_receipt_discovery_ignores_partial_archives_and_fences_handoff() {
        let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
            return;
        };
        crate::PostgresMigrator::connect(&connection)
            .unwrap()
            .migrate()
            .unwrap();
        let database_id = unique_database("archive_discovery_fence");
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("fence", "value"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        crate::PostgresStore::connect(&connection)
            .unwrap()
            .create_database(&database_id, schema)
            .unwrap();
        let receipt = PostgresIndexer::connect(&connection, &database_id)
            .unwrap()
            .consolidate()
            .unwrap();
        let endpoint = Peer::connect(&connection, &database_id, 64)
            .unwrap()
            .tiered_snapshot()
            .endpoint();
        let config = PostgresConnectionConfig::plaintext(&connection);
        let mut administrator = Client::connect(&connection, NoTls).unwrap();
        // Model the intermediate conversion state without bypassing any read
        // authentication: the header is intentionally not a completed owner.
        let mut staging = administrator.transaction().unwrap();
        staging
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        staging.execute(
            "INSERT INTO atomic_request_base_archives \
                 (database_id,generation,archive_revision,basis_t,tx_hash,state_hash, \
                  eidx_frontier,manifest_version,manifest_hash,payload,expected_node_count,node_set_hash) \
             SELECT database_id,log_generation,publication_revision,basis_t,tx_hash,state_hash, \
                    eidx_frontier,manifest_version,manifest_hash,payload,1,$2 \
               FROM atomic_tree_manifests WHERE manifest_hash=$1",
            &[&&receipt.manifest_hash[..], &&[0u8;32][..]],
        ).unwrap();
        staging.commit().unwrap();

        let pins = RootPinManager::connect(&config, &database_id).unwrap();
        let _generation = pins.acquire_generation(endpoint.generation).unwrap();
        let fence = pins.acquire_manifest(receipt.manifest_hash, false).unwrap();
        let key = tree_manifest_advisory_key(&receipt.manifest_hash);
        let mut competitor = administrator.transaction().unwrap();
        assert!(
            !competitor
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&key])
                .unwrap()
                .get::<_, bool>(0),
            "handoff must not pass a pre-discovery fence"
        );
        competitor.rollback().unwrap();
        let (exact, _) = TieredSnapshot::open_exact_configured(
            &config,
            &database_id,
            endpoint,
            Some(receipt.manifest_hash),
            64,
            1024 * 1024,
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(exact.endpoint(), endpoint);
        assert_eq!(exact.durable_manifest_hash(), Some(receipt.manifest_hash));
        assert_eq!(
            exact
                .core
                .load_counters
                .snapshot()
                .compatibility_materializations,
            0
        );
        drop(fence);
        let mut competitor = administrator.transaction().unwrap();
        assert!(
            !competitor
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&key])
                .unwrap()
                .get::<_, bool>(0),
            "the returned value must retain its discovery pin"
        );
        competitor.rollback().unwrap();
        drop(exact);

        // A genuinely contradictory completed owner is still corruption,
        // rather than silently preferring either copy of the same hash.
        let mut staging = administrator.transaction().unwrap();
        assert!(
            staging
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&key])
                .unwrap()
                .get::<_, bool>(0)
        );
        staging
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        staging
            .execute(
                "INSERT INTO atomic_request_base_archive_completions(manifest_hash) VALUES($1)",
                &[&&receipt.manifest_hash[..]],
            )
            .unwrap();
        staging.commit().unwrap();
        let error = TieredSnapshot::open_exact_configured(
            &config,
            &database_id,
            endpoint,
            Some(receipt.manifest_hash),
            64,
            1024 * 1024,
            RecentLimits::default(),
        )
        .err()
        .expect("two completed source owners must fail closed");
        assert_eq!(error.code, "peer/exact-manifest-corrupt");
        let mut cleanup = administrator.transaction().unwrap();
        assert!(
            cleanup
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&key])
                .unwrap()
                .get::<_, bool>(0),
            "a rejected open must release its temporary fence"
        );
        cleanup
            .batch_execute("SET LOCAL session_replication_role=replica")
            .unwrap();
        cleanup
            .execute(
                "DELETE FROM atomic_request_base_archive_completions WHERE manifest_hash=$1",
                &[&&receipt.manifest_hash[..]],
            )
            .unwrap();
        cleanup
            .execute(
                "DELETE FROM atomic_request_base_archives WHERE manifest_hash=$1",
                &[&&receipt.manifest_hash[..]],
            )
            .unwrap();
        cleanup.commit().unwrap();
    }

    #[test]
    fn corrupt_required_manifest_is_distinct_from_an_absent_manifest() {
        let Some(connection) = std::env::var("ATOMIC_POSTGRES_URL").ok() else {
            return;
        };
        let mut migrator = crate::PostgresMigrator::connect(&connection).unwrap();
        migrator.migrate().unwrap();

        let database_id = unique_database("exact_required_corrupt");
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("exact", "value"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        let mut store = crate::PostgresStore::connect(&connection).unwrap();
        store.create_database(&database_id, schema).unwrap();
        let receipt = PostgresIndexer::connect(&connection, &database_id)
            .unwrap()
            .consolidate()
            .unwrap();
        let endpoint = Peer::connect(&connection, &database_id, 64)
            .unwrap()
            .tiered_snapshot()
            .endpoint();

        let mut client = Client::connect(&connection, NoTls).unwrap();
        let mut fault = client.transaction().unwrap();
        fault
            .batch_execute("ALTER TABLE atomic_tree_manifests DISABLE TRIGGER USER")
            .unwrap();
        assert_eq!(
            fault
                .execute(
                    "UPDATE atomic_tree_manifests \
                        SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
                      WHERE manifest_hash = $1",
                    &[&&receipt.manifest_hash[..]],
                )
                .unwrap(),
            1
        );
        fault
            .batch_execute("ALTER TABLE atomic_tree_manifests ENABLE TRIGGER USER")
            .unwrap();
        fault.commit().unwrap();

        let error = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            Some(receipt.manifest_hash),
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .err()
        .expect("a corrupt required manifest must not be reported absent");
        assert_eq!(error.code, "peer/exact-manifest-corrupt");
    }

    #[test]
    fn manifest_selection_pages_past_corrupt_history_and_stops_at_a_valid_newest() {
        let Some(connection) = std::env::var("ATOMIC_POSTGRES_URL").ok() else {
            return;
        };
        let mut migrator = crate::PostgresMigrator::connect(&connection).unwrap();
        migrator.migrate().unwrap();

        let database_id = unique_database("manifest_selection_pages");
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("page", "value"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        let mut store = crate::PostgresStore::connect(&connection).unwrap();
        let created = store.create_database(&database_id, schema).unwrap();
        drop(store);

        let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
        let first = indexer.consolidate().unwrap();
        assert_eq!(first.publication_revision, 1);
        let endpoint = Peer::connect(&connection, &database_id, 64)
            .unwrap()
            .tiered_snapshot()
            .endpoint();
        let build = build_initial_native(&created, &TreeConfig::default()).unwrap();
        let mut tree_store = PostgresTreeStore::connect(&connection).unwrap();
        for (hash, payload) in build.nodes.iter() {
            tree_store.insert_node(*hash, payload).unwrap();
        }
        let retained_publications = u64::try_from(MANIFEST_SELECTION_PAGE_SIZE).unwrap() + 2;
        let mut newest_manifest_hash = [0; 32];
        for publication_revision in 2..=retained_publications {
            let manifest = PersistentTreeManifest {
                database_id: database_id.clone(),
                publication_revision,
                index_basis_t: endpoint.basis_t,
                basis_t: endpoint.basis_t,
                tx_hash: endpoint.tx_hash,
                state_hash: endpoint.state_hash,
                excision_generation: endpoint.generation,
                eidx_frontier: endpoint.eidx_frontier,
                trees: build.trees.clone(),
                pending_avet: Vec::new(),
            };
            let payload = manifest.encode().unwrap();
            let manifest_hash = sha256(&payload);
            newest_manifest_hash = manifest_hash;
            let record = TreeManifestRecord {
                database_id: database_id.clone(),
                publication_revision,
                basis_t: manifest.basis_t,
                index_basis_t: manifest.index_basis_t,
                tx_hash: manifest.tx_hash,
                state_hash: manifest.state_hash,
                excision_generation: manifest.excision_generation,
                eidx_frontier: manifest.eidx_frontier,
                manifest_hash,
                payload,
                roots: manifest
                    .trees
                    .iter()
                    .map(|tree| TreeRootBinding {
                        order: tree.descriptor.order,
                        history: tree.descriptor.history,
                        root_hash: tree.descriptor.root_hash,
                        datom_count: tree.descriptor.count,
                        encoded_bytes: tree.root_bytes,
                    })
                    .collect(),
            };
            assert_eq!(
                tree_store
                    .publish_manifest(&record, publication_revision - 1)
                    .unwrap(),
                TreePublishOutcome::Published
            );
        }
        drop(tree_store);

        // Leave only revision one valid. Selection must continue through more
        // than one fixed locator page instead of imposing a semantic fallback
        // cap or materializing all manifest payloads at once.
        let mut client = Client::connect(&connection, NoTls).unwrap();
        // Compatibility publications intentionally carry unknown membership.
        // These synthetic roots all have the exact same node closure as the
        // already-complete first root, so bind that truthful closure to the
        // final revision before asking the administrative indexer to repair it.
        assert_eq!(
            client
                .execute(
                    "UPDATE atomic_tree_live_sets \
                        SET manifest_hash = $1, complete = true, problem_code = NULL \
                      WHERE database_id = $2",
                    &[&&newest_manifest_hash[..], &database_id],
                )
                .unwrap(),
            1
        );
        let mut fault = client.transaction().unwrap();
        fault
            .batch_execute("ALTER TABLE atomic_tree_manifests DISABLE TRIGGER USER")
            .unwrap();
        assert_eq!(
            fault
                .execute(
                    "UPDATE atomic_tree_manifests \
                        SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
                      WHERE database_id = $1 AND publication_revision > 1",
                    &[&database_id],
                )
                .unwrap(),
            retained_publications - 1
        );
        fault
            .batch_execute("ALTER TABLE atomic_tree_manifests ENABLE TRIGGER USER")
            .unwrap();
        fault.commit().unwrap();

        let (oldest, paged) = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            None,
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(paged.examined_candidates, retained_publications);
        assert_eq!(paged.rejected_candidates, retained_publications - 1);
        assert_eq!(paged.selected_manifest_hash, first.manifest_hash);
        drop(oldest);

        let repaired = indexer.consolidate().unwrap();
        assert_eq!(repaired.publication_revision, retained_publications + 1);
        assert_eq!(repaired.manifest_candidates_examined, retained_publications);
        assert_eq!(
            repaired.manifest_candidates_rejected,
            retained_publications - 1
        );
        assert!(!repaired.manifest_probe_limit_reached);

        let (newest, fast) = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            None,
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .unwrap();
        assert_eq!(fast.examined_candidates, 1);
        assert_eq!(fast.rejected_candidates, 0);
        assert_eq!(fast.selected_manifest_hash, repaired.manifest_hash);
        drop(newest);

        let no_op = indexer.consolidate().unwrap();
        assert!(no_op.reused);
        assert_eq!(no_op.manifest_candidates_examined, 1);
        assert_eq!(no_op.manifest_candidates_rejected, 0);
        assert!(!no_op.manifest_probe_limit_reached);
    }

    #[test]
    fn exact_rebase_pins_old_and_new_manifests_until_each_value_drops() {
        let Some(connection) = std::env::var("ATOMIC_POSTGRES_URL").ok() else {
            return;
        };
        let mut migrator = crate::PostgresMigrator::connect(&connection).unwrap();
        migrator.migrate().unwrap();

        let database_id = unique_database("exact_rebase_distinct_roots");
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("exact", "value"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        let mut store = crate::PostgresStore::connect(&connection).unwrap();
        let created = store.create_database(&database_id, schema).unwrap();
        let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
        let old_publication = indexer.consolidate().unwrap();
        let endpoint = Peer::connect(&connection, &database_id, 64)
            .unwrap()
            .tiered_snapshot()
            .endpoint();
        let (old, _) = TieredSnapshot::open_exact_configured(
            &PostgresConnectionConfig::plaintext(&connection),
            &database_id,
            endpoint,
            Some(old_publication.manifest_hash),
            64,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )
        .unwrap();

        // Publish a physically distinct but semantically identical root at
        // the same logical endpoint. This isolates rebase/pin ownership from
        // transaction processing and from a background-index timing race.
        let build = build_initial_native(&created, &TreeConfig::default()).unwrap();
        let manifest = PersistentTreeManifest {
            database_id: database_id.clone(),
            publication_revision: old_publication.publication_revision + 1,
            index_basis_t: endpoint.basis_t,
            basis_t: endpoint.basis_t,
            tx_hash: endpoint.tx_hash,
            state_hash: endpoint.state_hash,
            excision_generation: endpoint.generation,
            eidx_frontier: endpoint.eidx_frontier,
            trees: build.trees,
            pending_avet: Vec::new(),
        };
        let payload = manifest.encode().unwrap();
        let new_manifest_hash = sha256(&payload);
        let record = TreeManifestRecord {
            database_id: database_id.clone(),
            publication_revision: manifest.publication_revision,
            basis_t: manifest.basis_t,
            index_basis_t: manifest.index_basis_t,
            tx_hash: manifest.tx_hash,
            state_hash: manifest.state_hash,
            excision_generation: manifest.excision_generation,
            eidx_frontier: manifest.eidx_frontier,
            manifest_hash: new_manifest_hash,
            payload,
            roots: manifest
                .trees
                .iter()
                .map(|tree| TreeRootBinding {
                    order: tree.descriptor.order,
                    history: tree.descriptor.history,
                    root_hash: tree.descriptor.root_hash,
                    datom_count: tree.descriptor.count,
                    encoded_bytes: tree.root_bytes,
                })
                .collect(),
        };
        let mut tree_store = PostgresTreeStore::connect(&connection).unwrap();
        for (hash, bytes) in build.nodes.iter() {
            tree_store.insert_node(*hash, bytes).unwrap();
        }
        assert_eq!(
            tree_store
                .publish_manifest(&record, old_publication.publication_revision)
                .unwrap(),
            TreePublishOutcome::Published
        );
        assert_ne!(new_manifest_hash, old_publication.manifest_hash);
        let (new, _) = old.rebase_exact(Some(new_manifest_hash)).unwrap();
        assert_eq!(old.endpoint(), endpoint);
        assert_eq!(new.endpoint(), endpoint);
        assert_eq!(
            old.durable_manifest_hash(),
            Some(old_publication.manifest_hash)
        );
        assert_eq!(new.durable_manifest_hash(), Some(new_manifest_hash));

        let pins = Arc::clone(&old.core.root_pins);
        let mut hashes = pins.hashes();
        hashes.sort_unstable();
        let mut expected = vec![old_publication.manifest_hash, new_manifest_hash];
        expected.sort_unstable();
        assert_eq!(hashes, expected);

        drop(new);
        assert_eq!(pins.hashes(), vec![old_publication.manifest_hash]);
        drop(old);
        assert!(pins.hashes().is_empty());
    }

    #[test]
    fn tree_metadata_rebuild_preserves_aliases_without_retaining_assertion_history() {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("item", "kind"),
                ValueType::Ref,
                Cardinality::One,
            ))
            .unwrap();
        let database = Database::new(schema).unwrap();
        let old = Keyword::new("kind", "old");
        let new = Keyword::new("kind", "new");
        let created = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("enum".into()),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(old.clone())),
                }],
                1_000,
            )
            .unwrap();
        let original = created.tempids["enum"];
        let renamed = created
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(original),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(new.clone())),
                }],
                2_000,
            )
            .unwrap();
        let repurposed = renamed
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("replacement".into()),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(old.clone())),
                }],
                3_000,
            )
            .unwrap();
        let replacement = repurposed.tempids["replacement"];

        let build = build_initial_native(&repurposed.db_after, &TreeConfig::default()).unwrap();
        let mut roots = BTreeMap::new();
        for tree in &build.trees {
            let bytes = build
                .nodes
                .get(&tree.descriptor.root_hash)
                .expect("builder emitted each root");
            let TreeNode::Root(root) = decode_tree_node(&tree.descriptor.root_hash, bytes).unwrap()
            else {
                panic!("manifest descriptor did not resolve to a root");
            };
            roots.insert(
                (tree.descriptor.history, order_tag(tree.descriptor.order)),
                Arc::new(root),
            );
        }
        let projection = derive_metadata_from_roots(&roots, |hash| {
            let bytes = build.nodes.get(&hash).ok_or_else(|| {
                fault(
                    "test/missing-tree-node",
                    "metadata derivation requested an absent test node",
                )
            })?;
            decode_tree_node(&hash, bytes).map(Arc::new)
        })
        .unwrap();

        assert_eq!(projection.idents.resolve(&old), Some(replacement));
        assert_eq!(projection.idents.resolve(&new), Some(original));
        assert_eq!(projection.idents.ident(original), Some(&new));
        assert_eq!(projection.idents.ident(replacement), Some(&old));
        assert_eq!(
            projection.schema.attribute(1_000).unwrap(),
            repurposed.db_after.schema().attribute(1_000).unwrap()
        );
        assert!(
            projection
                .schema_current
                .iter()
                .all(|datom| datom.entity != original && datom.entity != replacement),
            "general ident entities belong only in the folded ident maps"
        );
    }

    #[test]
    fn canonical_segment_and_manifest_round_trip() {
        let segment = IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 10,
                attribute: 2,
                value: Value::String("Ada".into()),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            }],
        };
        let bytes = encode_index_segment(&segment).unwrap();
        assert_eq!(decode_index_segment(&bytes).unwrap(), segment);
        let manifest = IndexManifest {
            database_id: "db".into(),
            basis_t: 1,
            tx_hash: [3; 32],
            eidx_frontier: 1000,
            segments: vec![SegmentRef {
                order: IndexOrder::Eavt,
                history: false,
                ordinal: 0,
                hash: sha256(&bytes),
                count: 1,
            }],
        };
        let encoded = encode_index_manifest(&manifest).unwrap();
        assert_eq!(
            hex(&sha256(&bytes)),
            "e6d5353a75da82b12ec3053f1c56ccd4639fef0de60333f2f6e20994dc89b555"
        );
        assert_eq!(
            hex(&sha256(&encoded)),
            "ffe27b48f28077b2be3f732808abd846fa09e7b380eba786979bb4280a49beea"
        );
        let decoded = decode_index_manifest(&encoded).unwrap();
        assert_eq!(encode_index_manifest(&decoded).unwrap(), encoded);
    }

    fn hex(bytes: &Digest) -> String {
        bytes.iter().map(|byte| format!("{byte:02x}")).collect()
    }

    #[test]
    fn malformed_segment_order_and_manifest_gaps_fail_closed() {
        let datom = |entity| Datom {
            entity,
            attribute: 2,
            value: Value::String("x".into()),
            tx: crate::t_to_tx(1).unwrap(),
            added: true,
        };
        assert_eq!(
            encode_index_segment(&IndexSegment {
                order: IndexOrder::Eavt,
                history: true,
                datoms: vec![datom(2), datom(1)]
            })
            .unwrap_err()
            .code,
            "encoding/unsorted-index-segment"
        );
        let manifest = IndexManifest {
            database_id: "db".into(),
            basis_t: 1,
            tx_hash: [0; 32],
            eidx_frontier: 1000,
            segments: vec![SegmentRef {
                order: IndexOrder::Eavt,
                history: false,
                ordinal: 1,
                hash: [0; 32],
                count: 1,
            }],
        };
        assert_eq!(
            encode_index_manifest(&manifest).unwrap_err().code,
            "encoding/index-segment-gap"
        );
    }

    #[test]
    fn immutable_cache_hits_and_eviction_do_not_change_values() {
        let first = Arc::new(IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 1,
                attribute: 2,
                value: Value::Long(3),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            }],
        });
        let second = Arc::new(IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 2,
                attribute: 2,
                value: Value::Long(4),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            }],
        });
        let mut cache = SegmentCache::new(1);
        cache.insert([1; 32], Arc::clone(&first));
        assert_eq!(cache.get(&[1; 32]).unwrap().datoms, first.datoms);
        cache.insert([2; 32], Arc::clone(&second));
        assert!(cache.get(&[1; 32]).is_none());
        assert_eq!(cache.get(&[2; 32]).unwrap().datoms, second.datoms);
        assert_eq!(
            cache.stats,
            CacheStats {
                hits: 2,
                misses: 1,
                evictions: 1,
                current_entries: 1,
                peak_entries: 1,
                ..CacheStats::default()
            }
        );
    }
}
