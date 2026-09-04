use crate::idents::IdentIndex;
use crate::operations::tree_manifest_advisory_key;
use crate::persistent_tree::{
    ChildRef, DirectoryNode, LeafSegment, RootNode, TreeBuildStats, TreeConfig, TreeMergeEdits,
    TreeNode, TreeNodeSet, TreeRangeResult, TreeReadStats, TreeSeekResult, build_tree,
    decode_tree_node, merge_tree,
};
use crate::postgres::{
    is_postgres_connection_error, postgres_error, read_authenticated_log_range, recover_to,
    verify_schema_compatibility,
};
use crate::recent::{
    EndpointProjection, RecentCursor, RecentCursorStats, RecentLimits, RecentRange, RecentTier,
};
use crate::state_commitment::{checkpoint_information, verify_checkpoint_state_hash};
use crate::{
    Database, DatabaseValue, Datom, Digest, DurableTransaction, Entity, EntityIdentifier,
    ErrorCategory, IndexManifest, IndexOrder, IndexPrefix, IndexSegment, ManifestTree,
    PersistentTreeManifest, PostgresConnectionConfig, PostgresTreeStore, PullPattern, Query,
    QueryControl, QueryExtensions, QueryInput, QueryOutcome, QueryValue, SemanticError,
    TreeManifestRecord, TreePublicationDelta, TreePublishOutcome, TreeRootBinding, View,
    decode_index_manifest, decode_index_segment, encode_genesis, sha256, tx_to_t,
};
#[cfg(test)]
use crate::{SegmentRef, encode_index_manifest, encode_index_segment};
use postgres::{Client, GenericClient};
use std::collections::{BTreeMap, BTreeSet, VecDeque};
use std::ops::Deref;
use std::sync::atomic::{AtomicU64, Ordering};
use std::sync::{Arc, Mutex, MutexGuard, OnceLock, RwLock};
use std::time::{Duration, Instant};

const DEFAULT_SEGMENT_DATOMS: usize = 4_096;
type LoadedBase = (Database, Digest, u64);
type BaseSelection = (Option<LoadedBase>, u64);

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum IndexBuildFault {
    None,
    AfterSegments,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IndexBuildReceipt {
    pub publication_revision: u64,
    pub basis_t: u64,
    pub manifest_hash: Digest,
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
    pub compatibility_materializations: u64,
    pub compatibility_hits: u64,
    pub compatibility_failures: u64,
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
/// `published_candidates` includes older candidates not inspected after a
/// valid newer base wins; `examined_candidates` and `rejected_candidates`
/// describe the actual newest-to-oldest authentication scan.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ExactOpenStats {
    pub(crate) published_candidates: u64,
    pub(crate) examined_candidates: u64,
    pub(crate) rejected_candidates: u64,
    pub(crate) selected_publication_revision: u64,
    pub(crate) selected_manifest_hash: Digest,
    pub(crate) tail_transactions: u64,
}

#[derive(Debug, Default)]
struct PeerLoadCounters {
    manifest_candidates: AtomicU64,
    root_reads: AtomicU64,
    directory_reads: AtomicU64,
    leaf_reads: AtomicU64,
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
            compatibility_materializations: self
                .compatibility_materializations
                .load(Ordering::Relaxed),
            compatibility_hits: self.compatibility_hits.load(Ordering::Relaxed),
            compatibility_failures: self.compatibility_failures.load(Ordering::Relaxed),
        }
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
    metadata: Arc<MetadataProjection>,
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

    fn avet_unready_after(&self, endpoint: &Self) -> BTreeSet<u32> {
        endpoint
            .schema
            .attributes()
            .filter(|attribute| attribute.indexed || attribute.unique.is_some())
            .filter_map(|attribute| {
                self.schema
                    .attribute(attribute.id)
                    .is_ok_and(|base| !base.indexed && base.unique.is_none())
                    .then_some(attribute.id)
            })
            .collect()
    }
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

    pub fn reconnect(&mut self) -> Result<(), SemanticError> {
        let mut client = self.connection.connect_for("index/reconnect")?;
        verify_schema_compatibility(&mut client)?;
        self.tree_store.reconnect()?;
        self.client = client;
        Ok(())
    }

    pub fn consolidate_with_fault(
        &mut self,
        fault_point: IndexBuildFault,
    ) -> Result<IndexBuildReceipt, SemanticError> {
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
        if let Some((previous, _, _)) = selection.usable.as_ref()
            && previous.publication_revision == selection.newest_observed_revision
            && selection.newest_live_complete
            && previous.basis_t == basis_t
            && previous.tx_hash == tx_hash
            && previous.state_hash == stored_state
        {
            let manifest_hash = previous.hash()?;
            let stats = self.tree_store.stats();
            return Ok(IndexBuildReceipt {
                publication_revision: previous.publication_revision,
                basis_t,
                manifest_hash,
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
            });
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

        let can_increment = selection.usable.as_ref().is_some_and(|(previous, _, _)| {
            selection.newest_live_complete
                && previous.publication_revision == selection.newest_observed_revision
        });
        let build = if can_increment {
            let (previous, base_projection, old_cache) = selection
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
                &tail,
                &tail_hashes,
                &base_projection,
                endpoint_projection,
                eidx_frontier,
                &self.tree_config,
                old_cache,
            )?
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

        let tree_manifest = PersistentTreeManifest {
            database_id: self.database_id.clone(),
            publication_revision,
            basis_t,
            tx_hash,
            state_hash: stored_state,
            excision_generation,
            eidx_frontier: build.eidx_frontier,
            trees: build.trees,
        };
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
            basis_t,
            tx_hash,
            state_hash: stored_state,
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
            self.tree_store.publish_manifest_with_delta(
                &tree_record,
                expected_publication_revision,
                &build.publication_delta,
            )
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
        let tree_store_stats = self.tree_store.stats();
        Ok(IndexBuildReceipt {
            publication_revision,
            basis_t,
            manifest_hash: tree_manifest_hash,
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
        })
    }
}

struct NativeIndexBuild {
    trees: Vec<ManifestTree>,
    nodes: TreeNodeSet,
    eidx_frontier: u64,
    input_datoms: u64,
    tail_datoms: u64,
    encoded_bytes: u64,
    reused_subtrees: u64,
    publication_delta: TreePublicationDelta,
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
        eidx_frontier: database.eidx_frontier(),
        input_datoms: stats.input_datoms,
        tail_datoms: 0,
        encoded_bytes: stats.encoded_bytes,
        reused_subtrees: 0,
        publication_delta: TreePublicationDelta::Replace { live_nodes },
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
    let build = build_initial_native(database, &TreeConfig::default())?;
    let manifest = PersistentTreeManifest {
        database_id: database_id.to_owned(),
        publication_revision,
        basis_t: database.basis_t(),
        tx_hash,
        state_hash,
        excision_generation: log_generation,
        eidx_frontier: build.eidx_frontier,
        trees: build.trees,
    };
    let payload = manifest.encode()?;
    let manifest_hash = sha256(&payload);
    let record = TreeManifestRecord {
        database_id: database_id.to_owned(),
        publication_revision,
        basis_t: manifest.basis_t,
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
        store.stage_manifest_with_delta(&record, expected_revision, &build.publication_delta)
    })();
    if let Err(error) = staged {
        store.release_build_intent()?;
        return Err(error);
    }
    Ok(manifest_hash)
}

struct NativeManifestSelection {
    newest_observed_revision: u64,
    newest_live_complete: bool,
    usable: Option<(PersistentTreeManifest, MetadataProjection, TreeNodeSet)>,
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
    let newest_observed_revision = tree_store.current_publication_revision(database_id)?;
    let newest_live_complete = client
        .query_opt(
            "SELECT l.complete \
               FROM atomic_tree_publications p \
               LEFT JOIN atomic_tree_live_sets l \
                 ON l.database_id = p.database_id \
                AND l.manifest_hash = p.manifest_hash \
              WHERE p.database_id = $1 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("index/tree-live-membership", error))?
        .is_some_and(|row| row.get::<_, Option<bool>>(0) == Some(true));
    let rows = client
        .query(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.eidx_frontier, m.manifest_hash, m.payload \
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
              WHERE m.database_id = $1 AND m.basis_t <= $2 \
                AND m.log_generation = $3 \
                AND semantic.tx_hash IS NOT NULL \
                AND ((m.log_generation = 0 AND legacy.tx_hash IS NOT NULL) \
                  OR (m.log_generation > 0 AND m.basis_t = 0 \
                      AND bootstrap.tx_hash IS NOT NULL) \
                  OR (m.log_generation > 0 AND m.basis_t > 0 \
                      AND native.tx_hash IS NOT NULL)) \
              ORDER BY p.publication_revision DESC",
            &[
                &database_id,
                &sql_basis(through)?,
                &sql_basis(excision_generation)?,
            ],
        )
        .map_err(|error| postgres_error("index/tree-manifests", error))?;
    for row in rows {
        let publication_revision = pg_basis(row.get(0), "tree publication revision")?;
        let basis_t = pg_basis(row.get(1), "tree manifest basis")?;
        let tx_hash = digest(row.get(2), "tree manifest transaction hash")?;
        let state_hash = digest(row.get(3), "tree manifest state hash")?;
        let eidx_frontier = pg_basis(row.get(4), "tree manifest entity frontier")?;
        let manifest_hash = digest(row.get(5), "tree manifest hash")?;
        let payload: Vec<u8> = row.get(6);
        let candidate = (|| {
            if sha256(&payload) != manifest_hash {
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
            Ok((manifest, metadata, cache))
        })();
        if let Ok(base) = candidate {
            return Ok(NativeManifestSelection {
                newest_observed_revision,
                newest_live_complete,
                usable: Some(base),
            });
        }
    }
    Ok(NativeManifestSelection {
        newest_observed_revision,
        newest_live_complete,
        usable: None,
    })
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
    entity: u64,
    attribute: u32,
    value: crate::Value,
    old: Option<Datom>,
    current: Option<Datom>,
}

#[allow(clippy::too_many_arguments)] // Immutable base, tail, projections, limits, and warm node set are distinct boundaries.
fn build_incremental_native(
    store: &mut PostgresTreeStore,
    previous: &PersistentTreeManifest,
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
    let history_eavt = previous.tree(IndexOrder::Eavt, true).ok_or_else(|| {
        fault(
            "index/missing-history-eavt",
            "manifest omitted history EAVT",
        )
    })?;
    let mut changes = BTreeMap::<Vec<u8>, CurrentTreeChange>::new();
    for datom in tail.iter().flat_map(|transaction| &transaction.tx_data) {
        let key = stored_eav_key(datom)?;
        if changes.contains_key(&key) {
            continue;
        }
        let lower = eav_bound(datom, true);
        let found = postgres_tree_seek(store, &current_eavt.descriptor, &lower, &mut old_cache)?
            .filter(|candidate| same_eav(candidate, datom));
        changes.insert(
            key,
            CurrentTreeChange {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
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
            if change.current.is_none() {
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

    // noHistory is evaluated exactly at this indexing endpoint. Only E/A/V
    // groups touched by the uncovered tail can become a new eligible pair;
    // prior physical omissions are never recomputed or resurrected.
    let mut durable_history_for_pairs = Vec::new();
    for change in changes.values() {
        if !endpoint_projection
            .schema
            .attribute(change.attribute)
            .is_ok_and(|attribute| attribute.no_history)
        {
            continue;
        }
        let exemplar = Datom {
            entity: change.entity,
            attribute: change.attribute,
            value: change.value.clone(),
            tx: u64::MAX,
            added: true,
        };
        let lower = eav_bound(&exemplar, true);
        let upper = eav_bound(&exemplar, false);
        durable_history_for_pairs.extend(postgres_tree_range(
            store,
            &history_eavt.descriptor,
            Some(&lower),
            Some(&upper),
            &mut old_cache,
        )?);
    }
    sort_dedup_datoms(&mut durable_history_for_pairs, IndexOrder::Eavt);

    let changed_avet =
        changed_avet_attributes(&base_projection.schema, &endpoint_projection.schema);
    let mut avet_backfills = BTreeMap::<u32, (Vec<Datom>, Vec<Datom>)>::new();
    let mut avet_drops = BTreeMap::<u32, (Vec<Datom>, Vec<Datom>)>::new();
    if !changed_avet.is_empty() {
        let current_aevt = &previous
            .tree(IndexOrder::Aevt, false)
            .ok_or_else(|| {
                fault(
                    "index/missing-current-aevt",
                    "manifest omitted current AEVT",
                )
            })?
            .descriptor;
        let history_aevt = &previous
            .tree(IndexOrder::Aevt, true)
            .ok_or_else(|| {
                fault(
                    "index/missing-history-aevt",
                    "manifest omitted history AEVT",
                )
            })?
            .descriptor;
        let current_avet = &previous
            .tree(IndexOrder::Avet, false)
            .ok_or_else(|| {
                fault(
                    "index/missing-current-avet",
                    "manifest omitted current AVET",
                )
            })?
            .descriptor;
        let history_avet = &previous
            .tree(IndexOrder::Avet, true)
            .ok_or_else(|| {
                fault(
                    "index/missing-history-avet",
                    "manifest omitted history AVET",
                )
            })?
            .descriptor;
        for (attribute, before, after) in &changed_avet {
            let (lower, upper) = attribute_bounds(*attribute)?;
            if !before && *after {
                let durable_current = postgres_tree_range(
                    store,
                    current_aevt,
                    Some(&lower),
                    Some(&upper),
                    &mut old_cache,
                )?;
                let durable_history = postgres_tree_range(
                    store,
                    history_aevt,
                    Some(&lower),
                    Some(&upper),
                    &mut old_cache,
                )?;
                let final_current = recent
                    .merge_current_range(
                        IndexOrder::Aevt,
                        &durable_current,
                        &RecentRange::unbounded(),
                    )?
                    .into_iter()
                    .filter(|datom| datom.attribute == *attribute)
                    .collect::<Vec<_>>();
                let final_history = recent
                    .consolidate_history_range(
                        IndexOrder::Aevt,
                        &durable_history,
                        &RecentRange::unbounded(),
                    )?
                    .into_iter()
                    .filter(|datom| datom.attribute == *attribute)
                    .collect::<Vec<_>>();
                avet_backfills.insert(*attribute, (final_current, final_history));
            } else if *before && !after {
                let current = postgres_tree_range(
                    store,
                    current_avet,
                    Some(&lower),
                    Some(&upper),
                    &mut old_cache,
                )?;
                let history = postgres_tree_range(
                    store,
                    history_avet,
                    Some(&lower),
                    Some(&upper),
                    &mut old_cache,
                )?;
                avet_drops.insert(*attribute, (current, history));
            }
        }
    }

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
                history_edits(
                    order,
                    &recent,
                    &durable_history_for_pairs,
                    &changed_avet,
                    &avet_backfills,
                    &avet_drops,
                )?
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
            let merged = merge_tree(&old.descriptor, &old_cache, &edits, config)?;
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
        eidx_frontier,
        input_datoms: tail_datoms,
        tail_datoms,
        encoded_bytes,
        reused_subtrees,
        publication_delta: TreePublicationDelta::Incremental {
            predecessor_manifest_hash: previous.hash()?,
            added_nodes,
            retired_nodes,
        },
    })
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
    durable_history_for_pairs: &[Datom],
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
    let mut durable = Vec::new();
    for datom in durable_history_for_pairs {
        if schema_index_member(projection.schema(), datom, order)? {
            durable.push(datom.clone());
        }
    }
    durable.sort_by(|left, right| left.cmp_in(right, order));
    let mut edits = TreeMergeEdits {
        insertions: recent
            .datoms(order)
            .iter()
            .filter(|datom| !changed(datom.attribute))
            .cloned()
            .collect(),
        no_history_pairs: recent
            .no_history_pairs(order, &durable)?
            .into_iter()
            .filter(|pair| !changed(pair.retraction.attribute))
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

fn postgres_tree_range(
    store: &mut PostgresTreeStore,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    start: Option<&Datom>,
    end: Option<&Datom>,
    cache: &mut TreeNodeSet,
) -> Result<Vec<Datom>, SemanticError> {
    if let (Some(start), Some(end)) = (start, end)
        && !start.cmp_in(end, descriptor.order).is_lt()
    {
        return Err(SemanticError::incorrect(
            "index/invalid-tree-range",
            "tree range start must be below its exclusive end",
        ));
    }
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(Vec::new());
    }
    let first_directory = start.map_or(0, |key| {
        floor_tree_child(&root.directories, key, descriptor.order)
    });
    let mut output = Vec::new();
    for (directory_index, reference) in root.directories.iter().enumerate().skip(first_directory) {
        if end.is_some_and(|end| !reference.key.cmp_datom(end, descriptor.order).is_lt()) {
            break;
        }
        let directory = load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )?;
        let first_leaf = if directory_index == first_directory {
            start.map_or(0, |key| {
                floor_tree_child(&directory.leaves, key, descriptor.order)
            })
        } else {
            0
        };
        for leaf_ref in directory.leaves.iter().skip(first_leaf) {
            if end.is_some_and(|end| !leaf_ref.key.cmp_datom(end, descriptor.order).is_lt()) {
                break;
            }
            let leaf = load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
            for index in 0..leaf.len() {
                let datom = leaf.datom(index).expect("validated leaf columns");
                if start.is_some_and(|start| datom.cmp_in(start, descriptor.order).is_lt()) {
                    continue;
                }
                if end.is_some_and(|end| !datom.cmp_in(end, descriptor.order).is_lt()) {
                    return Ok(output);
                }
                output.push(datom);
            }
        }
    }
    Ok(output)
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
    let mut last_directory_touched = false;
    for point in points {
        let directory_index = floor_tree_child(&root.directories, &point, descriptor.order);
        let directory_ref = &root.directories[directory_index];
        let directory = load_old_directory(
            store,
            cache,
            directory_ref,
            descriptor.order,
            descriptor.history,
        )?;
        // The affected directory itself is re-encoded even when only one
        // interior leaf changes. Root separator repair authenticates its new
        // first and last datoms, which may live in otherwise untouched leaves.
        if let Some(first) = directory.leaves.first() {
            load_old_leaf(store, cache, first, descriptor.order, descriptor.history)?;
        }
        if let Some(last) = directory.leaves.last()
            && directory.leaves.first() != Some(last)
        {
            load_old_leaf(store, cache, last, descriptor.order, descriptor.history)?;
        }
        let leaf_index = floor_tree_child(&directory.leaves, &point, descriptor.order);
        let first_leaf = leaf_index.saturating_sub(1);
        let last_leaf = leaf_index
            .saturating_add(1)
            .min(directory.leaves.len().saturating_sub(1));
        for leaf_ref in &directory.leaves[first_leaf..=last_leaf] {
            load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
        }

        // Rewriting a leaf or directory can change the sparse separator on
        // either side even though those neighboring children are themselves
        // reused. `merge_tree` authenticates those boundary datoms when it
        // repairs routing keys, so preload exactly those adjacent paths too.
        if let Some(previous_index) = directory_index.checked_sub(1) {
            let previous_ref = &root.directories[previous_index];
            let previous = load_old_directory(
                store,
                cache,
                previous_ref,
                descriptor.order,
                descriptor.history,
            )?;
            if let Some(last) = previous.leaves.last() {
                load_old_leaf(store, cache, last, descriptor.order, descriptor.history)?;
            }
        }
        if let Some(next_ref) = root.directories.get(directory_index.saturating_add(1)) {
            let next =
                load_old_directory(store, cache, next_ref, descriptor.order, descriptor.history)?;
            if let Some(first) = next.leaves.first() {
                load_old_leaf(store, cache, first, descriptor.order, descriptor.history)?;
            }
        }
        if directory_index + 1 == root.directories.len() {
            last_directory_touched = true;
        }
    }
    // A rewritten final directory can still end in an untouched old leaf.
    // The merge authenticates it to derive the new descriptor's last key.
    if last_directory_touched {
        let directory_ref = root
            .directories
            .last()
            .expect("non-empty root has a final directory");
        let directory = load_old_directory(
            store,
            cache,
            directory_ref,
            descriptor.order,
            descriptor.history,
        )?;
        let leaf_ref = directory
            .leaves
            .last()
            .expect("validated directory has a final leaf");
        load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
    }
    Ok(())
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
        let manifest_hash = tree.manifest_hash;
        let mut state = lock(&self.state);
        self.ensure_locked(&mut state)?;
        if !state.counts.contains_key(&manifest_hash)
            && let Err(error) = acquire_root_pin(
                state.client.as_mut().expect("root pin session was ensured"),
                manifest_hash,
            )
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
        Ok(Some(Arc::new(RootPin {
            manager: Arc::clone(self),
            manifest_hash,
        })))
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
        "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                            WHERE manifest_hash = $1) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress \
                                    WHERE manifest_hash = $1)",
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
    connection: PostgresConnectionConfig,
    recent_limits: RecentLimits,
    load_counters: PeerLoadCounters,
    root_pins: Arc<RootPinManager>,
    io: Mutex<PeerIo>,
}

struct PeerCore {
    read: Arc<TieredReadCore>,
    recent_limits: RecentLimits,
    /// Serializes catch-up/adoption work. Readers never take this lock.
    update: Mutex<()>,
    /// Explicit transaction-report observation channel. `None` is the normal
    /// state; reports are retained only after the caller opts in.
    reports: Mutex<Option<VecDeque<DurableTransaction>>>,
    /// One publication cell for all peer-observable connection state.
    state: RwLock<Arc<PeerState>>,
}

fn reconnect_peer_io(core: &TieredReadCore, io: &mut PeerIo) -> Result<(), SemanticError> {
    let mut client = core.connection.connect_for("peer/reconnect")?;
    verify_schema_compatibility(&mut client)?;
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
        let database_id = database_id.into();
        let mut client = connection.connect_for("peer/connect")?;
        // Fail before reading a head or any derived value when this peer does
        // not understand the installed PostgreSQL schema.
        verify_schema_compatibility(&mut client)?;
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
            )?;
            if tail.end_hash != head_hash {
                return Err(fault(
                    "peer/head-mismatch",
                    "native tree plus authenticated tail does not reach observed head",
                ));
            }
            let metadata = Arc::new(base_metadata.apply(&tail.transactions)?);
            let avet_unready = Arc::new(base_metadata.avet_unready_after(&metadata));
            let AuthenticatedTail {
                transactions,
                transaction_hashes,
                end_hash,
                end_state_hash,
                eidx_frontier,
            } = tail;
            let recent = Arc::new(RecentTier::new_authenticated(
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
        let root_pins = RootPinManager::connect(connection, &database_id)?;
        let generation_pin = root_pins.acquire_generation(excision_generation)?;
        let root_pin = root_pins.acquire(tree_base.as_deref())?;
        let compatibility = Arc::new(PeerCompatibility {
            value: compatibility,
            segments: Arc::new(Mutex::new(cache)),
        });
        let read = Arc::new(TieredReadCore {
            database_id,
            connection: connection.clone(),
            recent_limits,
            load_counters,
            root_pins,
            io: Mutex::new(PeerIo { client, tree_cache }),
        });
        Ok(Self {
            core: Arc::new(PeerCore {
                read,
                recent_limits,
                update: Mutex::new(()),
                reports: Mutex::new(None),
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

    /// Materialize the compatibility kernel value for the current immutable
    /// snapshot. Native tree users should prefer [`Peer::snapshot`]; this
    /// method retains the original infallible API and panics only when durable
    /// data is corrupt or unavailable. [`Peer::try_db`] exposes that failure.
    pub fn db(&self) -> Arc<Database> {
        self.try_db()
            .unwrap_or_else(|error| panic!("peer database materialization failed: {error}"))
    }

    pub fn try_db(&self) -> Result<Arc<Database>, SemanticError> {
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

    pub fn sync(&self) -> Result<Arc<Database>, SemanticError> {
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

    pub fn sync_to(&self, target: u64, timeout: Duration) -> Result<Arc<Database>, SemanticError> {
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

    pub fn sync_to_snapshot(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<PeerSnapshot, SemanticError> {
        let state = self.wait_for_basis(target, timeout, false)?;
        Ok(self.snapshot_for_state(state))
    }

    fn wait_for_basis(
        &self,
        target: u64,
        timeout: Duration,
        require_compatibility: bool,
    ) -> Result<Arc<PeerState>, SemanticError> {
        let deadline = Instant::now() + timeout;
        loop {
            self.core.read.root_pins.ensure()?;
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
        lock(&self.core.reports).take().is_some()
    }

    pub fn tx_reports_enabled(&self) -> bool {
        lock(&self.core.reports).is_some()
    }

    pub fn take_tx_reports(&self) -> Vec<DurableTransaction> {
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
        let metadata = Arc::new(base_metadata.apply(&tail.transactions)?);
        let avet_unready = Arc::new(base_metadata.avet_unready_after(&metadata));
        let recent = Arc::new(RecentTier::new_authenticated(
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
        )?;
        let metadata = Arc::new(state.metadata.apply(&tail.transactions)?);
        let mut avet_unready = (*state.avet_unready).clone();
        avet_unready.extend(state.metadata.avet_unready_after(&metadata));
        let recent = state.recent.extend_authenticated(
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
        self.publish_arc(Arc::clone(&published));
        if let Some(reports) = lock(&self.core.reports).as_mut() {
            reports.extend(tail.transactions);
        }
        Ok(published)
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
            )?;
            if tail.end_hash != hash {
                return Err(fault(
                    "peer/excision-head-mismatch",
                    "post-excision native base and tail do not reach the new head",
                ));
            }
            let metadata = Arc::new(base_metadata.apply(&tail.transactions)?);
            let avet_unready = Arc::new(base_metadata.avet_unready_after(&metadata));
            let recent = Arc::new(RecentTier::new_authenticated(
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
        self.publish(successor);
        Ok(true)
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
        *self
            .core
            .state
            .write()
            .unwrap_or_else(std::sync::PoisonError::into_inner) = state;
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
    durable_next: Option<Datom>,
    recent_next: Option<Datom>,
    failed: bool,
}

struct DurableTreeCursor {
    snapshot: TieredSnapshot,
    root: Arc<RootNode>,
    history: bool,
    order: IndexOrder,
    start: Option<Datom>,
    end: Option<Datom>,
    prefix: Option<IndexPrefix>,
    directory_index: usize,
    leaf_index: usize,
    directory: Option<LoadedDirectory>,
    leaf: Option<LoadedLeaf>,
    datom_index: usize,
    stats: TreeReadStats,
    exhausted: bool,
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
            directory_index,
            leaf_index: 0,
            directory: None,
            leaf: None,
            datom_index: 0,
            stats: TreeReadStats::default(),
            exhausted,
        }
    }

    fn next_datom(&mut self) -> Result<Option<Datom>, SemanticError> {
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
                    self.datom_index = self.start.as_ref().map_or_else(
                        || {
                            self.prefix
                                .as_ref()
                                .map_or(0, |prefix| leaf_prefix_lower_bound(&leaf, prefix))
                        },
                        |start| leaf_lower_bound(&leaf, start, self.order),
                    );
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
            self.leaf_index = self.start.as_ref().map_or_else(
                || {
                    self.prefix
                        .as_ref()
                        .map_or(0, |prefix| prefix_start_child(&directory.leaves, prefix))
                },
                |key| floor_tree_child(&directory.leaves, key, self.order),
            );
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
        if self.recent_next.is_none() {
            self.recent_next = self.recent.next();
        }
        let take_durable = match (&self.durable_next, &self.recent_next) {
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

impl TieredSnapshot {
    /// Open one generation-qualified immutable value strictly from an
    /// authenticated native publication plus its authenticated log tail.
    /// There is intentionally no legacy segment, eager `Database`, or
    /// `recover_to` branch in this writer-facing constructor.
    pub(crate) fn open_exact_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        endpoint: ExactEndpoint,
        required_manifest: Option<Digest>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<(Self, ExactOpenStats), SemanticError> {
        let endpoint = endpoint.validate()?;
        let database_id = database_id.into();
        let mut client = connection.connect_for("peer/exact-open")?;
        verify_schema_compatibility(&mut client)?;
        let counters = PeerLoadCounters::default();
        let mut tree_cache = TreeNodeCache::new(cache_entries, cache_bytes);
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

        // Pins close both load-versus-tree-GC and load-versus-generation-GC
        // races before the authoritative tail is read.
        let root_pins = RootPinManager::connect(connection, &database_id)?;
        let generation_pin = root_pins.acquire_generation(endpoint.generation)?;
        let root_pin = root_pins
            .acquire(Some(&base))
            .map_err(|error| exact_pin_error(error, required_manifest))?;
        let (state, tail_transactions) = build_exact_tiered_state(
            &mut client,
            &database_id,
            endpoint,
            base,
            generation_pin,
            root_pin,
            recent_limits,
            0,
        )?;
        let stats = exact_open_stats(&state, scan_stats, tail_transactions)?;
        let core = Arc::new(TieredReadCore {
            database_id,
            connection: connection.clone(),
            recent_limits,
            load_counters: counters,
            root_pins,
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
        let endpoint = self.endpoint().validate()?;
        self.core.root_pins.ensure()?;
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
        let generation_pin = self
            .core
            .root_pins
            .acquire_generation(endpoint.generation)?;
        let root_pin = self
            .core
            .root_pins
            .acquire(Some(&base))
            .map_err(|error| exact_pin_error(error, required_manifest))?;
        let local_generation = self.state.generation.saturating_add(1);
        let (state, tail_transactions) = build_exact_tiered_state(
            &mut io.client,
            &self.core.database_id,
            endpoint,
            base,
            generation_pin,
            root_pin,
            self.core.recent_limits,
            local_generation,
        )?;
        let stats = exact_open_stats(&state, scan_stats, tail_transactions)?;
        Ok((
            Self {
                core: Arc::clone(&self.core),
                state: Arc::new(state),
            },
            stats,
        ))
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
        let first = datoms.next().transpose()?;
        let second = datoms.next().transpose()?;
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
    ) -> Result<Self, SemanticError> {
        self.authenticated_successor_checked(
            tx_hash,
            state_hash,
            transaction,
            Some(successor_schema),
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
        self.authenticated_successor_checked(tx_hash, state_hash, transaction, None)
    }

    fn authenticated_successor_checked(
        &self,
        tx_hash: Digest,
        state_hash: Digest,
        transaction: DurableTransaction,
        successor_schema: Option<&crate::Schema>,
    ) -> Result<Self, SemanticError> {
        let metadata = Arc::new(
            self.state
                .metadata
                .apply(std::slice::from_ref(&transaction))?,
        );
        if successor_schema.is_some_and(|schema| metadata.schema.as_ref() != schema) {
            return Err(fault(
                "peer/successor-schema-mismatch",
                "committed transaction metadata does not derive the assessed successor schema",
            ));
        }
        let mut avet_unready = (*self.state.avet_unready).clone();
        avet_unready.extend(self.state.metadata.avet_unready_after(&metadata));
        let basis_t = transaction.basis_t;
        let eidx_frontier = transaction.eidx_frontier;
        let recent = self
            .state
            .recent
            .extend_authenticated(std::iter::once((tx_hash, transaction)), metadata.endpoint())?;

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

    pub(crate) fn state_hash(&self) -> Digest {
        self.state.current_state_hash
    }

    pub(crate) fn excision_generation(&self) -> u64 {
        self.state.excision_generation
    }

    pub(crate) fn recent_stats(&self) -> crate::recent::RecentStats {
        self.state.recent.stats()
    }

    pub(crate) fn durable_manifest_hash(&self) -> Option<Digest> {
        self.state.tree_base.as_ref().map(|base| base.manifest_hash)
    }

    pub(crate) fn tree_cache_stats(&self) -> CacheStats {
        lock(&self.core.io).tree_cache.stats
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
            durable_next: None,
            recent_next: None,
            failed: false,
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
            durable_next: None,
            recent_next: None,
            failed: false,
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
        if order != IndexOrder::Avet || self.state.avet_unready.is_empty() {
            return Ok(());
        }
        if attribute.is_some_and(|attribute| !self.state.avet_unready.contains(&attribute)) {
            return Ok(());
        }
        let attributes = self
            .state
            .avet_unready
            .iter()
            .map(u32::to_string)
            .collect::<Vec<_>>()
            .join(",");
        Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "peer/avet-not-ready",
            format!(
                "AVET backfill is not yet published for attribute(s) {attributes}; consolidate and refresh the native index"
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
            return Ok(node);
        }
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
    published_candidates: u64,
    examined_candidates: u64,
    rejected_candidates: u64,
}

enum TreeBaseScan {
    Selected(TreeBase, TreeBaseScanStats),
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
    let required_manifest = required_manifest.map(|hash| hash.to_vec());
    // Count from the publication authority itself. Broken/missing manifest or
    // semantic-root joins are invalid candidates, never evidence that no
    // publication exists.
    let published = client
        .query(
            "SELECT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                            WHERE progress.database_id = p.database_id \
                              AND progress.publication_revision = p.publication_revision \
                              AND progress.manifest_hash = p.manifest_hash) \
               FROM atomic_tree_publications p \
              WHERE p.database_id = $1 AND p.basis_t <= $2 \
                AND p.log_generation = $3 \
                AND ($4::bytea IS NULL OR p.manifest_hash = $4)",
            &[
                &database_id,
                &through_sql,
                &generation_sql,
                &required_manifest,
            ],
        )
        .map_err(|error| postgres_error("peer/tree-publication-scan", error))?;
    let mut stats = TreeBaseScanStats {
        published_candidates: published.len() as u64,
        ..TreeBaseScanStats::default()
    };
    if published.is_empty() {
        return Ok(TreeBaseScan::NoPublication(stats));
    }
    if required_manifest.is_some() && published.iter().any(|row| row.get::<_, bool>(0)) {
        stats.examined_candidates = 1;
        stats.rejected_candidates = 1;
        return Ok(TreeBaseScan::RequiredCollecting(stats));
    }
    let rows = client
        .query(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.eidx_frontier, m.manifest_hash, m.payload, \
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
              WHERE m.database_id = $1 AND m.basis_t <= $2 \
                AND m.log_generation = $3 \
                AND ($4::bytea IS NULL OR p.manifest_hash = $4) \
              ORDER BY p.publication_revision DESC",
            &[
                &database_id,
                &through_sql,
                &generation_sql,
                &required_manifest,
            ],
        )
        .map_err(|error| postgres_error("peer/tree-manifests", error))?;
    for row in rows {
        stats.examined_candidates = stats.examined_candidates.saturating_add(1);
        counters.manifest_candidates.fetch_add(1, Ordering::Relaxed);
        let publication_revision = pg_basis(row.get(0), "tree publication revision")?;
        let basis_t = pg_basis(row.get(1), "tree manifest basis")?;
        let tx_hash = digest(row.get(2), "tree manifest transaction hash")?;
        let state_hash = digest(row.get(3), "tree manifest state hash")?;
        let eidx_frontier = pg_basis(row.get(4), "tree manifest entity frontier")?;
        let manifest_hash = digest(row.get(5), "tree manifest hash")?;
        let payload: Vec<u8> = row.get(6);
        let authoritative: bool = row.get(7);
        let collecting: bool = row.get(8);
        if collecting {
            stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
            if required_manifest.is_some() {
                return Ok(TreeBaseScan::RequiredCollecting(stats));
            }
            continue;
        }
        let candidate = (|| {
            if !authoritative {
                return Err(fault(
                    "peer/tree-manifest-authority",
                    "tree publication is not bound to an authoritative generation endpoint",
                ));
            }
            if sha256(&payload) != manifest_hash {
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
                let described = manifest.tree(order, history).ok_or_else(|| {
                    fault("peer/tree-root-coordinate", "manifest omitted a tree root")
                })?;
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
            // Metadata is part of candidate validity. Missing or corrupt
            // required descendants reject this publication and let the outer
            // loop consider the preceding immutable publication.
            let metadata = Arc::new(derive_metadata_from_client(
                client, &roots, counters, cache,
            )?);
            Ok(TreeBase {
                manifest,
                manifest_hash,
                roots,
                metadata,
            })
        })();
        if let Ok(base) = candidate {
            return Ok(TreeBaseScan::Selected(base, stats));
        }
        stats.rejected_candidates = stats.rejected_candidates.saturating_add(1);
    }
    Ok(TreeBaseScan::AllInvalid(stats))
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
        TreeBaseScan::Selected(base, _) => Ok(Some(base)),
        TreeBaseScan::NoPublication(_) => Ok(None),
        TreeBaseScan::AllInvalid(stats) => Err(fault(
            "peer/all-native-publications-invalid",
            format!(
                "the generation has published native roots, but every candidate failed authenticated loading (published={}, examined={}, rejected={})",
                stats.published_candidates, stats.examined_candidates, stats.rejected_candidates
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
        TreeBaseScan::Selected(base, stats) => Ok((base, stats)),
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
                    "{message} (published={}, examined={}, rejected={})",
                    stats.published_candidates,
                    stats.examined_candidates,
                    stats.rejected_candidates
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
                    "{message} (published={}, examined={}, rejected={})",
                    stats.published_candidates,
                    stats.examined_candidates,
                    stats.rejected_candidates
                ),
            ))
        }
        TreeBaseScan::RequiredCollecting(stats) => Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "peer/exact-manifest-collecting",
            format!(
                "the required native manifest is already being collected (published={}, examined={}, rejected={})",
                stats.published_candidates, stats.examined_candidates, stats.rejected_candidates
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

fn build_exact_tiered_state<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    endpoint: ExactEndpoint,
    base: TreeBase,
    generation_pin: Arc<GenerationPin>,
    root_pin: Option<Arc<RootPin>>,
    recent_limits: RecentLimits,
    local_generation: u64,
) -> Result<(TieredState, u64), SemanticError> {
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
    let metadata = Arc::new(base_metadata.apply(&tail.transactions)?);
    let avet_unready = Arc::new(base_metadata.avet_unready_after(&metadata));
    let recent = Arc::new(RecentTier::new_authenticated(
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
    ))
}

fn exact_open_stats(
    state: &TieredState,
    scan: TreeBaseScanStats,
    tail_transactions: u64,
) -> Result<ExactOpenStats, SemanticError> {
    let base = state.tree_base.as_ref().ok_or_else(|| {
        fault(
            "peer/exact-native-base-lost",
            "strict native construction produced no durable tree base",
        )
    })?;
    Ok(ExactOpenStats {
        published_candidates: scan.published_candidates,
        examined_candidates: scan.examined_candidates,
        rejected_candidates: scan.rejected_candidates,
        selected_publication_revision: base.manifest.publication_revision,
        selected_manifest_hash: base.manifest_hash,
        tail_transactions,
    })
}

struct AuthenticatedTail {
    transactions: Vec<DurableTransaction>,
    transaction_hashes: Vec<Digest>,
    end_hash: Digest,
    end_state_hash: Digest,
    eidx_frontier: u64,
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
) -> Result<AuthenticatedTail, SemanticError> {
    if target_t < base.basis_t {
        return Err(fault(
            "peer/tail-target-before-base",
            "requested tail endpoint precedes its native base",
        ));
    }
    let mut end_hash = base.tx_hash;
    let mut end_state_hash = base.state_hash;
    let mut eidx_frontier = base.eidx_frontier;
    let rows = read_authenticated_log_range(
        client,
        database_id,
        log_generation,
        base.basis_t,
        target_t,
        base.tx_hash,
    )?;
    let mut transactions = Vec::with_capacity(rows.len());
    let mut transaction_hashes = Vec::with_capacity(rows.len());
    for row in rows {
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
        eidx_frontier = transaction.eidx_frontier;
        end_hash = row.tx_hash;
        end_state_hash = state_hash;
        transaction_hashes.push(row.tx_hash);
        transactions.push(transaction);
    }
    Ok(AuthenticatedTail {
        transactions,
        transaction_hashes,
        end_hash,
        end_state_hash,
        eidx_frontier,
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

pub(crate) fn recover_transactor_state<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    target_t: u64,
    target_hash: Digest,
) -> Result<(Database, Digest, RecoveryStats), SemanticError> {
    let mut cache = SegmentCache::new(0);
    let log_generation = read_excision_generation(client, database_id)?;
    let (base, rejected_manifests) = if log_generation == 0 {
        load_latest_base_with_stats(client, database_id, target_t, &mut cache)?
    } else {
        (None, 0)
    };
    let (mut database, mut hash, base_t) = match base {
        Some((database, hash, basis)) => (database, hash, basis),
        None => {
            let recovered = recover_to(client, database_id, target_t, target_hash)?;
            return Ok((
                recovered.database,
                recovered.final_hash,
                RecoveryStats {
                    base_t: 0,
                    target_t,
                    tail_transactions: target_t,
                    rejected_manifests,
                },
            ));
        }
    };
    let tail_transactions = target_t.checked_sub(base_t).ok_or_else(|| {
        fault(
            "peer/base-ahead-of-head",
            "selected persistent base is ahead of the authoritative head",
        )
    })?;
    if tail_transactions > 0 {
        apply_tail(
            client,
            database_id,
            log_generation,
            &mut database,
            &mut hash,
            target_t,
        )?;
    }
    if database.basis_t() != target_t || hash != target_hash {
        return Err(fault(
            "peer/head-mismatch",
            "verified base plus log tail did not reach the authoritative head",
        ));
    }
    Ok((
        database,
        hash,
        RecoveryStats {
            base_t,
            target_t,
            tail_transactions,
            rejected_manifests,
        },
    ))
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
    fn exact_native_scan_states_and_endpoint_validation_are_distinct() {
        let stats = TreeBaseScanStats {
            published_candidates: 2,
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
            basis_t: endpoint.basis_t,
            tx_hash: endpoint.tx_hash,
            state_hash: endpoint.state_hash,
            excision_generation: endpoint.generation,
            eidx_frontier: endpoint.eidx_frontier,
            trees: build.trees,
        };
        let payload = manifest.encode().unwrap();
        let new_manifest_hash = sha256(&payload);
        let record = TreeManifestRecord {
            database_id: database_id.clone(),
            publication_revision: manifest.publication_revision,
            basis_t: manifest.basis_t,
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
