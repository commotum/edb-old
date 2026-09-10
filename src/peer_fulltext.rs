//! Fulltext reads share the native snapshot's exact retention fence and only
//! positive bounded caches. No writer availability is needed to query pages.
use super::*;
use crate::fulltext_store::{FulltextMutation, load_page, load_projection};
use crate::{
    FulltextCursor, FulltextProjection, FulltextReadLimits, FulltextReadStats, FulltextRecord,
};

#[path = "peer_fulltext_diff.rs"]
mod diff;

#[derive(Clone)]
pub struct NativeFulltextReader {
    snapshot: TieredSnapshot,
    projection: FulltextProjection,
}
impl NativeFulltextReader {
    pub fn cache_stats(&self) -> crate::FulltextCacheStats {
        self.snapshot.core.fulltext_cache.stats()
    }
    pub fn source_basis_t(&self) -> u64 {
        self.projection.source_basis_t
    }
    pub fn source_generation(&self) -> u64 {
        self.projection.source_generation
    }
    pub fn record_count(&self) -> u64 {
        self.projection.record_count
    }
    pub fn projection(&self) -> &FulltextProjection {
        &self.projection
    }
    pub fn prefix(
        &self,
        prefix: &[u8],
        limits: FulltextReadLimits,
    ) -> Result<FulltextCursor, SemanticError> {
        let snapshot = self.snapshot.clone();
        let source = self.projection.source_manifest;
        FulltextCursor::new(
            &self.projection,
            prefix,
            limits,
            Box::new(move |hash, stats| {
                if let Some(page) = snapshot.core.fulltext_cache.get(source, hash) {
                    stats.cache_hits += 1;
                    return Ok(page);
                }
                // Goal2 rule: only a cold read touches pin/connection health. A
                // lost pin reacquires this exact lineage/root/generation or fails.
                snapshot.core.root_pins.ensure()?;
                let mut io = lock(&snapshot.core.io);
                if io.client.is_closed() {
                    reconnect_peer_io(&snapshot.core, &mut io)?;
                    snapshot.core.root_pins.ensure()?;
                }
                // Identical misses use the existing one bounded PostgreSQL lane.
                if let Some(page) = snapshot.core.fulltext_cache.get(source, hash) {
                    stats.cache_hits += 1;
                    return Ok(page);
                }
                let before = stats.block_bytes;
                let page = load_page(&mut io.client, source, hash, stats)?;
                snapshot.core.fulltext_cache.insert(
                    source,
                    hash,
                    Arc::clone(&page),
                    (stats.block_bytes - before) as usize,
                );
                Ok(page)
            }),
        )
    }
    pub fn get(&self, key: &[u8]) -> Result<Option<FulltextRecord>, SemanticError> {
        self.get_with_stats(key).map(|(record, _)| record)
    }
    pub fn get_with_stats(
        &self,
        key: &[u8],
    ) -> Result<(Option<FulltextRecord>, FulltextReadStats), SemanticError> {
        let mut cursor = self.prefix(
            key,
            FulltextReadLimits {
                max_records: 1,
                max_block_bytes: 128 * 1024 * 1024,
            },
        )?;
        let record = cursor
            .next()
            .transpose()?
            .filter(|record| record.key == key);
        Ok((record, cursor.stats()))
    }
}

impl TieredSnapshot {
    pub(crate) fn fulltext_reader(&self) -> Result<NativeFulltextReader, SemanticError> {
        let source = self.state.tree_base.as_ref().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unavailable,
                "fulltext/index-unavailable",
                "database has no published native search source",
            )
        })?;
        let projection = if let Some(header) = self.core.fulltext_cache.header(source.manifest_hash)
        {
            header
        } else {
            self.core.root_pins.ensure()?;
            let mut io = lock(&self.core.io);
            if io.client.is_closed() {
                reconnect_peer_io(&self.core, &mut io)?;
                self.core.root_pins.ensure()?;
            }
            let header =
                load_projection(&mut io.client, source.manifest_hash)?.ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Unavailable,
                        "fulltext/index-unavailable",
                        "search projection has not been published for this native source",
                    )
                })?;
            if header.source_basis_t != source.manifest.basis_t
                || header.source_generation != source.manifest.excision_generation
            {
                return Err(fault(
                    "fulltext/source-binding",
                    "search header does not describe its authenticated source manifest",
                ));
            }
            self.core.fulltext_cache.insert_header(header.clone());
            header
        };
        if projection.analyzer_version != u32::from(crate::fulltext_analysis::ANALYZER_VERSION) {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "fulltext/analyzer-version",
                "published search index uses another analyzer version",
            ));
        }
        Ok(NativeFulltextReader {
            snapshot: self.clone(),
            projection,
        })
    }
}
impl DatabaseValue {
    /// Eager values return None; native values with missing search projection
    /// return an explicit lag error, never an implicit whole-database scan.
    pub fn native_fulltext_reader(&self) -> Result<Option<NativeFulltextReader>, SemanticError> {
        self.fulltext_native_snapshot()
            .map(|snapshot| snapshot.fulltext_reader())
            .transpose()
    }
}

impl PostgresIndexer {
    pub fn with_fulltext_build_limits(mut self, limits: crate::FulltextBuildLimits) -> Self {
        self.fulltext_build_limits = limits;
        self
    }
    pub fn fulltext_build_stats(&self) -> crate::FulltextBuildStats {
        self.fulltext_build_stats
    }
    /// Last optional search build failure; canonical publication stays valid.
    pub fn fulltext_build_error(&self) -> Option<&SemanticError> {
        self.fulltext_build_error.as_ref()
    }

    /// Reconstruct a missing search sidecar from the current authenticated
    /// publication. Restore/upgrade do not need to rewrite canonical manifests.
    pub fn rebuild_fulltext(&mut self) -> Result<Option<FulltextProjection>, SemanticError> {
        self.latest_fulltext_projection(false)
    }

    /// Normal startup/retry can extend an authenticated earlier search root;
    /// the public repair entry point deliberately remains a full-build oracle.
    pub(crate) fn ensure_latest_fulltext(
        &mut self,
    ) -> Result<Option<FulltextProjection>, SemanticError> {
        self.latest_fulltext_projection(true)
    }

    fn latest_fulltext_projection(
        &mut self,
        incremental: bool,
    ) -> Result<Option<FulltextProjection>, SemanticError> {
        let row=self.client.query_opt("SELECT publication_revision,manifest_hash FROM atomic_tree_publications WHERE database_id=$1 ORDER BY publication_revision DESC LIMIT 1", &[&self.database_id])
            .map_err(|e|postgres_error("fulltext/select-source",e))?;
        let Some(row) = row else { return Ok(None) };
        let revision = pg_basis(row.get(0), "fulltext source revision")?;
        let hash = digest(row.get(1), "fulltext source hash")?;
        self.build_fulltext_projection(revision, hash, incremental)
    }
    pub(super) fn ensure_fulltext_projection(
        &mut self,
        revision: u64,
        hash: Digest,
    ) -> Result<Option<FulltextProjection>, SemanticError> {
        self.build_fulltext_projection(revision, hash, true)
    }

    fn build_fulltext_projection(
        &mut self,
        revision: u64,
        hash: Digest,
        incremental: bool,
    ) -> Result<Option<FulltextProjection>, SemanticError> {
        crate::database_catalog::require_active_id_in(&mut self.client, &self.database_id)?;
        self.fulltext_build_stats = crate::FulltextBuildStats::default();
        let mut store = crate::FulltextStore::connect(&self.connection)?;
        if let Some(projection) =
            store.open(hash, u32::from(crate::fulltext_analysis::ANALYZER_VERSION))?
        {
            return Ok(Some(projection));
        }
        // Read only coordinates here; the exact opener below authenticates the
        // source, including a native genesis with no ordinary transaction row.
        let source = self.client.query_opt(
            "SELECT m.basis_t,m.tx_hash,m.state_hash,m.log_generation,m.eidx_frontier FROM atomic_tree_publications p JOIN atomic_tree_manifests m ON m.manifest_hash=p.manifest_hash WHERE p.database_id=$1 AND p.publication_revision=$2 AND p.manifest_hash=$3",
            &[&self.database_id,&sql_basis(revision)?,&&hash[..]])
            .map_err(|e|postgres_error("fulltext/source-coordinates",e))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "fulltext/source-unavailable",
                    "search source manifest is no longer retained",
                )
            })?;
        let endpoint = ExactEndpoint {
            basis_t: pg_basis(source.get(0), "fulltext source basis")?,
            tx_hash: digest(source.get(1), "fulltext source tx hash")?,
            state_hash: digest(source.get(2), "fulltext source state hash")?,
            generation: pg_basis(source.get(3), "fulltext source generation")?,
            eidx_frontier: pg_basis(source.get(4), "fulltext source frontier")?,
        };
        let (snapshot, _) = TieredSnapshot::open_exact_configured(
            &self.connection,
            &self.database_id,
            endpoint,
            Some(hash),
            128,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )?;
        let db = snapshot.database_value();
        if !db.schema().attributes().any(|attribute| attribute.fulltext) {
            return Ok(None);
        }
        if incremental && let Some(predecessor) = self.fulltext_predecessor(revision, &snapshot)? {
            let new_attributes = db
                .schema()
                .attributes()
                .filter(|a| a.fulltext && predecessor.snapshot.schema().attribute(a.id).is_err())
                .map(|a| a.id)
                .collect();
            let mut difference =
                diff::HistoryDifference::new(predecessor.snapshot.clone(), snapshot.clone())?;
            let (empty_corpus, empty_work) = crate::fulltext::empty_fulltext_corpus(
                predecessor.snapshot.schema(),
                &predecessor,
            )?;
            // Initial indexing has no existing text to reuse. Bulk packing its
            // admitted delta avoids one path-copy upload per initial record.
            // Do not choose this path merely because the old corpus is empty:
            // unchanged/nontext publications must still reuse their old root.
            let mut first_text = None;
            if empty_corpus.is_some() {
                for change in difference.by_ref() {
                    let (datom, insert) = change?;
                    if datom.added && db.schema().attribute(datom.attribute)?.fulltext {
                        if !insert {
                            return Err(fault(
                                "fulltext/empty-predecessor-document",
                                "an authenticated empty predecessor cannot remove a document",
                            ));
                        }
                        first_text = Some((datom, insert));
                        break;
                    }
                }
            }
            let bulk = first_text.is_some();
            let mut records = crate::fulltext::DeltaRecords::new(
                first_text.into_iter().map(Ok).chain(&mut difference),
                db.schema(),
                &predecessor,
                new_attributes,
            );
            let mutations = records.by_ref().map(|record| {
                record.map(|(record, insert)| FulltextMutation {
                    key: record.key,
                    value: insert.then_some(record.value),
                })
            });
            let (projection, mut stats) = if bulk {
                store.update_empty(
                    hash,
                    snapshot.basis_t(),
                    snapshot.state.excision_generation,
                    crate::fulltext_analysis::ANALYZER_VERSION,
                    predecessor.projection(),
                    empty_corpus.expect("new text found only after proving an empty corpus"),
                    mutations,
                    &self.fulltext_build_limits,
                )?
            } else {
                store.update(
                    hash,
                    snapshot.basis_t(),
                    snapshot.state.excision_generation,
                    crate::fulltext_analysis::ANALYZER_VERSION,
                    predecessor.projection(),
                    mutations,
                    &self.fulltext_build_limits,
                )?
            };
            stats.documents_added = records.stats.documents_added;
            stats.documents_removed = records.stats.documents_removed;
            stats.tokenized_bytes = records.stats.tokenized_bytes;
            stats.statistics_reads = records.stats.statistics_reads + empty_work.statistics_reads;
            stats.statistics_read_bytes =
                records.stats.statistics_read_bytes + empty_work.statistics_read_bytes;
            drop(records);
            stats.source_references_examined = difference.references_examined;
            stats.source_datoms_examined = difference.datoms_examined;
            stats.source_nodes_read = difference.source_nodes_read();
            stats.source_node_bytes = difference.source_node_bytes();
            stats.predecessor_candidates = self.fulltext_build_stats.predecessor_candidates;
            self.fulltext_build_stats = stats;
            return Ok(Some(projection));
        }
        let mut records = crate::fulltext::fulltext_records(&db)?;
        let (projection, mut stats) = store.build(
            hash,
            snapshot.basis_t(),
            snapshot.state.excision_generation,
            u32::from(crate::fulltext_analysis::ANALYZER_VERSION),
            &mut records,
            &self.fulltext_build_limits,
        )?;
        stats.documents_added = records.stats.documents_added;
        stats.tokenized_bytes = records.stats.tokenized_bytes;
        stats.source_datoms_examined = records.datoms_examined;
        drop(records);
        let loads = snapshot.load_stats();
        stats.source_nodes_read = loads.root_reads + loads.directory_reads + loads.leaf_reads;
        stats.source_node_bytes = diff::source_node_bytes(&snapshot);
        stats.predecessor_candidates = self.fulltext_build_stats.predecessor_candidates;
        self.fulltext_build_stats = stats;
        Ok(Some(projection))
    }

    fn fulltext_predecessor(
        &mut self,
        revision: u64,
        target: &TieredSnapshot,
    ) -> Result<Option<NativeFulltextReader>, SemanticError> {
        let row = self.client.query_opt(
            "SELECT p.publication_revision,p.manifest_hash,m.basis_t,m.tx_hash,m.state_hash,m.log_generation,m.eidx_frontier \
             FROM atomic_tree_publications p \
             JOIN atomic_tree_manifests m ON m.manifest_hash=p.manifest_hash \
             JOIN atomic_fulltext_projections f ON f.manifest_hash=p.manifest_hash \
             WHERE p.database_id=$1 AND p.publication_revision<$2 \
               AND p.log_generation=$3 AND p.basis_t<=$4 AND f.analyzer_version=$5 \
             ORDER BY p.publication_revision DESC LIMIT 1",
            &[&self.database_id, &sql_basis(revision)?, &sql_basis(target.state.excision_generation)?,
                &sql_basis(target.basis_t())?, &(crate::fulltext_analysis::ANALYZER_VERSION as i32)],
        ).map_err(|e| postgres_error("fulltext/select-predecessor", e))?;
        let Some(row) = row else {
            return Ok(None);
        };
        self.fulltext_build_stats.predecessor_candidates += 1;
        let predecessor_revision = pg_basis(row.get(0), "fulltext predecessor revision")?;
        let hash = digest(row.get(1), "fulltext predecessor hash")?;
        let endpoint = ExactEndpoint {
            basis_t: pg_basis(row.get(2), "fulltext predecessor basis")?,
            tx_hash: digest(row.get(3), "fulltext predecessor tx")?,
            state_hash: digest(row.get(4), "fulltext predecessor state")?,
            generation: pg_basis(row.get(5), "fulltext predecessor generation")?,
            eidx_frontier: pg_basis(row.get(6), "fulltext predecessor frontier")?,
        };
        let (snapshot, opened) = TieredSnapshot::open_exact_configured(
            &self.connection,
            &self.database_id,
            endpoint,
            Some(hash),
            128,
            8 * 1024 * 1024,
            RecentLimits::default(),
        )?;
        if opened.selected_publication_revision != predecessor_revision
            || snapshot.core.lineage_id != target.core.lineage_id
            || snapshot.state.excision_generation != target.state.excision_generation
            || snapshot.basis_t() > target.basis_t()
            || (snapshot.basis_t() == target.basis_t()
                && (snapshot.state.current_hash != target.state.current_hash
                    || snapshot.state.current_state_hash != target.state.current_state_hash))
        {
            return Err(fault(
                "fulltext/predecessor-binding",
                "search predecessor is not an earlier source of the captured lineage",
            ));
        }
        // Fulltext and valueType are immutable for an installed attribute.
        // Identity aliases/cardinality/noHistory changes do not change analysis;
        // a genuine projection-definition change requires the full rebuild.
        for attribute in snapshot.schema().attributes() {
            if attribute.fulltext
                && target
                    .schema()
                    .attribute(attribute.id)
                    .map_or(true, |next| {
                        !next.fulltext || next.value_type != attribute.value_type
                    })
            {
                return Ok(None);
            }
        }
        for attribute in target.schema().attributes().filter(|a| a.fulltext) {
            if snapshot
                .schema()
                .attribute(attribute.id)
                .is_ok_and(|old| !old.fulltext)
            {
                return Ok(None);
            }
        }
        Ok(Some(snapshot.fulltext_reader()?))
    }
}
