//! Fixed, read-only backup values using the native immutable tree reader.
use crate::backup::{BackupPoint, BackupReadMetadata};
use crate::idents::IdentIndex;
use crate::index::NormalizedIndexBoundary;
use crate::peer::{DurableTreeCursor, DurableTreeSource, TreeNodeCache};
use crate::persistent_tree::{RootNode, TreeNode, TreeReadStats, decode_tree_node};
use crate::{
    CacheStats, DatabaseValue, Datom, Digest, ErrorCategory, IndexBoundary, IndexOrder,
    IndexPrefix, Keyword, Schema, SemanticError,
};
use std::collections::BTreeMap;
use std::path::{Path, PathBuf};
use std::sync::{Arc, Mutex};

/// Bounds the same decoded-node cache used by live peers. Roots and resident
/// schema/ident metadata are separate, immutable snapshot ownership.
#[derive(Clone, Debug)]
pub struct BackupReadConfig {
    pub cache_entries: usize,
    pub cache_bytes: usize,
}
impl Default for BackupReadConfig {
    fn default() -> Self {
        Self {
            cache_entries: 256,
            cache_bytes: 64 * 1024 * 1024,
        }
    }
}

/// Actual filesystem payload work, not SQL or filesystem block traffic.
/// Counts include open-time roots/metadata and subsequent shared-cursor reads.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct BackupReadStats {
    pub object_reads: u64,
    pub object_bytes: u64,
    pub root_reads: u64,
    pub directory_reads: u64,
    pub leaf_reads: u64,
    pub datoms_yielded: u64,
}

/// One fixed backup point. No transactor, PostgreSQL connection, restore, or
/// eager replay is performed. `db()` clones the same exact immutable basis.
#[derive(Clone)]
pub struct BackupConnection {
    snapshot: BackupSnapshot,
}
impl BackupConnection {
    pub fn open(directory: impl AsRef<Path>) -> Result<Self, SemanticError> {
        Self::open_configured(directory, None, BackupReadConfig::default())
    }
    pub fn open_point(
        directory: impl AsRef<Path>,
        point: &BackupPoint,
    ) -> Result<Self, SemanticError> {
        Self::open_configured(directory, Some(point), BackupReadConfig::default())
    }
    pub fn open_configured(
        directory: impl AsRef<Path>,
        point: Option<&BackupPoint>,
        config: BackupReadConfig,
    ) -> Result<Self, SemanticError> {
        let metadata = crate::backup::open_read_point(directory.as_ref(), point)?;
        Ok(Self {
            snapshot: BackupSnapshot::open(directory.as_ref(), metadata, config)?,
        })
    }
    pub fn db(&self) -> DatabaseValue {
        DatabaseValue::offline(self.snapshot.clone())
    }
    pub fn point(&self) -> &BackupPoint {
        &self.snapshot.inner.metadata.point
    }
    pub fn log(&self) -> crate::LogValue {
        crate::LogValue::from_backup(self.snapshot.clone())
    }
    pub fn read_stats(&self) -> BackupReadStats {
        self.snapshot.source.stats()
    }
    pub fn cache_stats(&self) -> CacheStats {
        self.snapshot.source.cache.stats()
    }
}

#[derive(Clone)]
pub(crate) struct BackupSnapshot {
    inner: Arc<BackupSnapshotInner>,
    source: BackupNodeSource,
}
struct BackupSnapshotInner {
    metadata: BackupReadMetadata,
    roots: BTreeMap<(bool, u8), Arc<RootNode>>,
    schema: Arc<Schema>,
    idents: Arc<IdentIndex>,
}

#[derive(Clone)]
pub(crate) struct BackupNodeSource {
    directory: Arc<PathBuf>,
    cache: TreeNodeCache,
    stats: Arc<Mutex<BackupReadStats>>,
}
impl BackupNodeSource {
    fn stats(&self) -> BackupReadStats {
        *self.stats.lock().unwrap_or_else(|e| e.into_inner())
    }
    pub(crate) fn read_object(&self, hash: Digest) -> Result<Vec<u8>, SemanticError> {
        let observed = crate::io_diagnostics::CacheObservation::start(crate::CacheTier::LocalDisk);
        let result = crate::backup::read_object(&self.directory, hash);
        let bytes = result.as_ref().map_or(0, |bytes| bytes.len() as u64);
        observed.finish(result.is_ok(), result.is_err(), bytes, bytes);
        let mut stats = self.stats.lock().unwrap_or_else(|e| e.into_inner());
        stats.object_reads = stats.object_reads.saturating_add(1);
        stats.object_bytes = stats.object_bytes.saturating_add(bytes);
        result
    }
}
impl DurableTreeSource for BackupNodeSource {
    fn load_node(
        &self,
        hash: Digest,
        stats: &mut TreeReadStats,
    ) -> Result<Arc<TreeNode>, SemanticError> {
        let observed =
            crate::io_diagnostics::CacheObservation::start(crate::CacheTier::DecodedNode);
        if let Some(node) = self.cache.get(&hash) {
            stats.cache_hits = stats.cache_hits.saturating_add(1);
            observed.finish(true, false, 0, 0);
            return Ok(node);
        }
        stats.cache_misses = stats.cache_misses.saturating_add(1);
        observed.finish(false, false, 0, 0);
        let bytes = self.read_object(hash)?;
        let node = Arc::new(decode_tree_node(&hash, &bytes)?);
        let mut totals = self.stats.lock().unwrap_or_else(|e| e.into_inner());
        match node.as_ref() {
            TreeNode::Root(_) => {
                stats.root_reads += 1;
                totals.root_reads += 1;
            }
            TreeNode::Directory(_) => {
                stats.directory_reads += 1;
                totals.directory_reads += 1;
            }
            TreeNode::Leaf(_) => {
                stats.leaf_reads += 1;
                totals.leaf_reads += 1;
            }
        }
        drop(totals);
        // TreeReadStats predates offline reads: decoded_bytes here is payload
        // bytes. It is never forwarded into transaction SQL counters.
        stats.decoded_bytes = stats.decoded_bytes.saturating_add(bytes.len() as u64);
        let retained = node.retained_bytes().saturating_add(bytes.len() as u64);
        self.cache.insert(
            hash,
            Arc::clone(&node),
            usize::try_from(retained).unwrap_or(usize::MAX),
        );
        Ok(node)
    }
}

impl BackupSnapshot {
    fn open(
        directory: &Path,
        metadata: BackupReadMetadata,
        config: BackupReadConfig,
    ) -> Result<Self, SemanticError> {
        let source = BackupNodeSource {
            directory: Arc::new(directory.to_path_buf()),
            cache: TreeNodeCache::new(config.cache_entries, config.cache_bytes),
            stats: Arc::new(Mutex::new(BackupReadStats {
                object_reads: metadata.open_object_reads,
                object_bytes: metadata.open_object_bytes,
                ..BackupReadStats::default()
            })),
        };
        let mut roots = BTreeMap::new();
        for tree in &metadata.manifest.trees {
            let descriptor = &tree.descriptor;
            let node = source.load_node(descriptor.root_hash, &mut TreeReadStats::default())?;
            let TreeNode::Root(root) = node.as_ref() else {
                return Err(fault(
                    "backup/read-root-kind",
                    "backup descriptor is not a tree root",
                ));
            };
            if root.order != descriptor.order
                || root.history != descriptor.history
                || root.count != descriptor.count
                || (root.count == 0) != root.directories.is_empty()
            {
                return Err(fault(
                    "backup/read-root-content",
                    "backup root disagrees with its descriptor",
                ));
            }
            if roots
                .insert(
                    (root.history, order_tag(root.order)),
                    Arc::new(root.clone()),
                )
                .is_some()
            {
                return Err(fault(
                    "backup/duplicate-read-root",
                    "backup repeats an index root",
                ));
            }
        }
        if roots.len() != 8 {
            return Err(fault(
                "backup/missing-read-root",
                "backup needs all eight current/history roots",
            ));
        }
        let (schema, idents) = crate::peer::derive_offline_metadata(&roots, |hash| {
            source.load_node(hash, &mut TreeReadStats::default())
        })?;
        Ok(Self {
            source,
            inner: Arc::new(BackupSnapshotInner {
                metadata,
                roots,
                schema,
                idents,
            }),
        })
    }
    pub(crate) fn lineage_id(&self) -> &str {
        &self.inner.metadata.point.lineage_id
    }
    pub(crate) fn basis_t(&self) -> u64 {
        self.inner.metadata.manifest.basis_t
    }
    pub(crate) fn generation(&self) -> u64 {
        self.inner.metadata.point.log_generation
    }
    pub(crate) fn database_value(&self) -> DatabaseValue {
        DatabaseValue::offline(self.clone())
    }
    pub(crate) fn eidx_frontier(&self) -> u64 {
        self.inner.metadata.manifest.eidx_frontier
    }
    pub(crate) fn reserved_allocation(
        &self,
    ) -> Option<crate::reserved_allocation::ReservedAllocation> {
        self.inner.metadata.reserved_allocation
    }
    pub(crate) fn read_log_transaction(
        &self,
        t: u64,
        predecessor: Option<Digest>,
    ) -> Result<(crate::LogTransaction, Digest, u64), SemanticError> {
        crate::backup::read_log_transaction(
            &self.source.directory,
            &self.inner.metadata,
            t,
            predecessor,
            &mut |hash| self.source.read_object(hash),
            &mut |hash| self.source.load_node(hash, &mut TreeReadStats::default()),
        )
    }
    pub(crate) fn schema(&self) -> &Schema {
        &self.inner.schema
    }
    pub(crate) fn schema_arc(&self) -> Arc<Schema> {
        Arc::clone(&self.inner.schema)
    }
    pub(crate) fn entid(&self, ident: &Keyword) -> Option<u64> {
        self.inner.idents.resolve(ident)
    }
    pub(crate) fn ident(&self, entity: u64) -> Option<&Keyword> {
        self.inner.idents.ident(entity)
    }
    pub(crate) fn avet_ready(&self, attribute: u32) -> bool {
        !self
            .inner
            .metadata
            .manifest
            .pending_avet
            .iter()
            .any(|work| work.attribute == attribute)
    }
    fn root(&self, history: bool, order: IndexOrder) -> Arc<RootNode> {
        Arc::clone(&self.inner.roots[&(history, order_tag(order))])
    }
    pub(crate) fn cursor(&self, history: bool, order: IndexOrder) -> BackupIndexCursor {
        self.wrap(
            DurableTreeCursor::new(
                self.source.clone(),
                self.root(history, order),
                history,
                order,
                None,
                None,
            ),
            order,
        )
    }
    pub(crate) fn prefix_cursor(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<BackupIndexCursor, SemanticError> {
        prefix.validate()?;
        if let IndexPrefix::Avet { attribute, .. } = prefix {
            self.ensure_avet_ready(*attribute)?;
        }
        Ok(self.wrap(
            DurableTreeCursor::new_prefix(
                self.source.clone(),
                self.root(history, prefix.order()),
                history,
                prefix.clone(),
            ),
            prefix.order(),
        ))
    }
    pub(crate) fn boundary_cursor(
        &self,
        history: bool,
        boundary: &IndexBoundary,
        reverse: bool,
    ) -> Result<BackupIndexCursor, SemanticError> {
        if let Some(attribute) = boundary.avet_attribute() {
            self.ensure_avet_ready(attribute)?;
        }
        Ok(self.normalized_cursor(history, boundary.normalized()?, reverse))
    }
    pub(crate) fn normalized_cursor(
        &self,
        history: bool,
        boundary: NormalizedIndexBoundary,
        reverse: bool,
    ) -> BackupIndexCursor {
        let order = boundary.order();
        let root = self.root(history, order);
        self.wrap(
            if reverse {
                DurableTreeCursor::new_reverse(self.source.clone(), root, history, boundary)
            } else {
                DurableTreeCursor::new_forward_boundary(
                    self.source.clone(),
                    root,
                    history,
                    boundary,
                )
            },
            order,
        )
    }
    fn ensure_avet_ready(&self, attribute: u32) -> Result<(), SemanticError> {
        let indexed = self
            .schema()
            .attribute(attribute)
            .is_ok_and(|a| a.indexed || a.unique.is_some());
        if !indexed {
            return Err(SemanticError::incorrect(
                "peer/avet-attribute-not-indexed",
                "attribute has no AVET storage",
            ));
        }
        if !self.avet_ready(attribute) {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "peer/avet-not-ready",
                "AVET projection is incomplete at this fixed backup point",
            ));
        }
        Ok(())
    }
    fn wrap(
        &self,
        cursor: DurableTreeCursor<BackupNodeSource>,
        order: IndexOrder,
    ) -> BackupIndexCursor {
        crate::io_diagnostics::record_index_cursor(order);
        BackupIndexCursor {
            cursor,
            snapshot: self.clone(),
            order,
            failed: false,
        }
    }
    pub(crate) fn resolve_program(
        &self,
        hash: crate::ProgramHash,
    ) -> Result<Arc<crate::program::ValidatedProgram>, SemanticError> {
        let program = crate::decode_program(&self.source.read_object(hash)?)?;
        if crate::program_hash(&program)? != hash {
            return Err(fault(
                "backup/program-hash",
                "backup program does not match its identity",
            ));
        }
        Ok(Arc::new(crate::program::ValidatedProgram::from_canonical(
            program,
        )))
    }
}

pub(crate) struct BackupIndexCursor {
    cursor: DurableTreeCursor<BackupNodeSource>,
    snapshot: BackupSnapshot,
    order: IndexOrder,
    failed: bool,
}
impl BackupIndexCursor {
    pub(crate) fn next_with_poll(
        &mut self,
        poll: &mut dyn FnMut() -> Result<bool, SemanticError>,
    ) -> Option<Result<Datom, SemanticError>> {
        if self.failed {
            return None;
        }
        let result = (|| loop {
            if !poll()? {
                return Ok(None);
            }
            let Some(datom) = self.cursor.next_datom()? else {
                return Ok(None);
            };
            if self.order == IndexOrder::Avet && !self.snapshot.avet_ready(datom.attribute) {
                continue;
            }
            let mut stats = self
                .snapshot
                .source
                .stats
                .lock()
                .unwrap_or_else(|e| e.into_inner());
            stats.datoms_yielded = stats.datoms_yielded.saturating_add(1);
            return Ok(Some(datom));
        })();
        match result {
            Ok(Some(datom)) => Some(Ok(datom)),
            Ok(None) => {
                self.failed = true;
                None
            }
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}
impl Iterator for BackupIndexCursor {
    type Item = Result<Datom, SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        self.next_with_poll(&mut || Ok(true))
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
fn fault(code: &'static str, message: &'static str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
