use crate::cow_generation::{
    GenerationLogRow, GenerationRewriter, SourceLogEncoding, SourceLogRow, SourceRequest,
    SourceRequestKey,
};
use crate::database::ExcisionCutoff;
use crate::excision::{ExcisionTargetKind, PlannedExcisionPredicate};
use crate::log_generation::LineageTransactionContent;
use crate::peer::stage_full_generation_tree;
use crate::persistent_tree::{TreeNode, TreeNodeSet, decode_tree_node, validate_tree};
use crate::postgres::{
    AuthenticatedLogTransaction, insert_program_generation_refs, read_authenticated_log_range,
    recover_generation_to, verify_schema_compatibility,
};
use crate::{
    Datom, Digest, IndexOrder, PersistentTreeManifest, PostgresConnectionConfig, PostgresTreeStore,
    SemanticError, decode_genesis, decode_index_manifest, decode_index_segment, decode_transaction,
    sha256, transaction_hash,
};
use postgres::{Client, GenericClient, IsolationLevel};
use std::collections::{BTreeMap, BTreeSet};
use std::time::Duration;

/// Recommended grace period for routine garbage collection.
///
/// Datomic's capacity guidance recommends roughly a month outside an initial
/// import, but its collector accepts the operator's chosen older-than boundary.
/// Atomic does the same: [`PostgresOperator::garbage_inventory`] and
/// [`PostgresOperator::collect_garbage`] accept shorter ages deliberately for
/// imports and controlled maintenance. Snapshot pins and exact live-membership
/// checks remain authoritative regardless of the chosen grace period.
pub const RECOMMENDED_GARBAGE_COLLECTION_AGE: Duration = Duration::from_secs(30 * 24 * 60 * 60);

/// One operator call advances one oldest root globally.  Its potentially
/// large retired-node ledger is drained separately by the bound below.
pub const MAX_TREE_RETIREMENTS_PER_GC: usize = 1;

/// Maximum changed-path rows moved from one claimed root per call.
pub const MAX_TREE_RETIREMENT_NODES_PER_GC: usize = 512;

/// One operator call advances one content-first intent globally.
pub const MAX_TREE_BUILD_INTENTS_PER_GC: usize = 1;

/// Maximum upload-ledger rows drained from that intent per call.
pub const MAX_TREE_BUILD_INTENT_NODES_PER_GC: usize = 512;

/// Maximum exact immutable values drained in one operator transaction.
pub const MAX_TREE_NODES_PER_GC: usize = 512;

/// Maximum request-base archive closure pins released in one operator
/// transaction. Raw values are merely marked here and become eligible for the
/// ordinary exact tree-node collector on a later call.
pub const MAX_REQUEST_BASE_ARCHIVE_NODES_PER_GC: usize = 512;

/// Maximum detached program blobs drained in one operator transaction.
pub const MAX_PROGRAMS_PER_GC: usize = 512;

/// One retired authoritative log generation is advanced per operator call.
/// The durable SQL phase cursor makes repeated calls restart-safe.
pub const MAX_LOG_GENERATIONS_PER_GC: usize = 1;

/// Maximum rows detached from one retired log generation per call. Metadata
/// phases may remove a single row; no phase exceeds this bound.
pub const MAX_LOG_GENERATION_ROWS_PER_GC: usize = 512;

/// Maximum immutable semantic-root coordinates detached from one terminal
/// log generation in one operator transaction.
pub const MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC: usize = 512;

/// Maximum globally unreferenced semantic commitment nodes reclaimed in one
/// operator transaction. Removing one parent may expose children only to a
/// later call, keeping every transaction bounded.
pub const MAX_SEMANTIC_COMMITMENT_NODES_PER_GC: usize = 512;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IntegrityProblem {
    pub code: String,
    pub message: String,
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct OperationalMetrics {
    pub basis_t: u64,
    pub index_basis_t: u64,
    pub index_lag: u64,
    pub transactions: u64,
    pub transaction_bytes: u64,
    pub requests: u64,
    pub current_datoms: u64,
    pub history_datoms: u64,
    pub manifests: u64,
    pub segments: u64,
    pub segment_bytes: u64,
    pub programs: u64,
    pub active_leases: u64,
    pub orphan_segments: u64,
    pub orphan_programs: u64,
    /// Newest append-only physical root revision visible in this report.
    pub tree_publication_revision: u64,
    /// Published native root revisions retained for this database.
    pub tree_publications: u64,
    /// Canonical native manifests retained for this database.
    pub tree_manifests: u64,
    /// Authenticated AVET attribute projections still being copied or
    /// removed in the newest usable native publication. Zero means every
    /// logically requested AVET projection is physically ready.
    pub pending_avet_projections: u64,
    /// Unique immutable native nodes reachable from this database's retained
    /// publications, including roots, directories, and leaves.
    pub tree_nodes: u64,
    pub tree_node_bytes: u64,
    /// Immutable exact-retry roots retained outside the ordinary accelerator
    /// publication chain for this database.
    pub request_base_archives: u64,
    /// Closure-membership rows currently pinning archive tree values.
    pub request_base_archive_nodes: u64,
    /// Durable native request receipts bound to either a normal publication
    /// or an exact-retry archive.
    pub request_base_bindings: u64,
    /// Globally stored native nodes proven unreachable only when shared
    /// reachability is complete. Zero is conservative when it is not.
    pub orphan_tree_nodes: u64,
    /// Some other database has an undecodable published root. Global orphan
    /// counts are therefore withheld rather than guessed.
    pub shared_reachability_uncertain: bool,
    /// Immutable semantic-state coordinates retained for this database.
    pub semantic_commitment_roots: u64,
    /// Unique semantic commitment nodes reachable from those coordinates.
    pub semantic_commitment_nodes: u64,
    pub semantic_commitment_node_bytes: u64,
    /// Globally stored semantic nodes not reachable from any retained root.
    /// Collection removes only the no-incoming-reference frontier each call.
    pub orphan_semantic_commitment_nodes: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IntegrityReport {
    pub database_id: String,
    pub metrics: OperationalMetrics,
    pub problems: Vec<IntegrityProblem>,
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct GarbageInventory {
    pub segment_hashes: Vec<Digest>,
    pub program_hashes: Vec<Digest>,
    /// Superseded native root publications safe to retire. A publication only
    /// becomes old when its successor was published, not when this root was
    /// first written.
    pub tree_publications: Vec<TreePublicationGarbage>,
    /// Abandoned content-first upload intents whose session pin disappeared
    /// and whose last heartbeat crossed the same grace boundary.
    pub tree_build_intents: Vec<TreeBuildIntentGarbage>,
    /// Manifests removed with the superseded root publications above.
    pub tree_manifest_hashes: Vec<Digest>,
    /// Exact post-publication garbage marks drained after all current and
    /// pending memberships have ceased to retain them.
    pub tree_node_hashes: Vec<Digest>,
    /// Restored exact-retry roots advanced by one bounded archive release.
    /// These roots never participate in ordinary accelerator selection.
    pub request_base_archives: Vec<RequestBaseArchiveGarbage>,
    /// Retired authoritative log generations advanced by exactly one durable
    /// phase. Empty phases are included because closing admission or moving
    /// the restart cursor is itself material collection progress.
    pub log_generations: Vec<LogGenerationGarbage>,
    /// Exact semantic root coordinates detached as the first phase of a
    /// terminal generation's collection.
    pub semantic_commitment_roots: Vec<SemanticCommitmentRootGarbage>,
    /// Globally unreferenced immutable semantic nodes removed this call.
    pub semantic_commitment_node_hashes: Vec<Digest>,
    pub applied: bool,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct SemanticCommitmentRootGarbage {
    pub database_id: String,
    pub generation: u64,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub current_root: Option<Digest>,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct LogGenerationGarbage {
    pub database_id: String,
    pub generation: u64,
    /// True for a never-activated rewrite whose source was superseded or an
    /// isolated failed initial restore; false for an authoritative generation
    /// retired by an activation edge.
    pub abandoned: bool,
    /// Phase reported after this call. When an input phase was empty, the SQL
    /// collector advances the cursor and reports the following phase.
    pub collection_phase: u16,
    pub rows_removed: u64,
    /// Rows above that were semantic commitment roots. A nonzero value means
    /// the durable log phase cursor intentionally did not advance.
    pub semantic_roots_removed: u64,
    pub is_complete: bool,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct TreePublicationGarbage {
    pub database_id: String,
    pub publication_revision: u64,
    pub manifest_hash: Digest,
    /// False means this root can retire as metadata but carried no trustworthy
    /// old-minus-new node witness (for example a legacy or repair transition).
    pub garbage_complete: bool,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct TreeBuildIntentGarbage {
    pub database_id: String,
    pub log_generation: u64,
    pub expected_revision: u64,
    pub manifest_hash: Digest,
    /// True for an abandoned upload whose exact uploaded values become
    /// garbage marks. False is bounded cleanup of a successfully consumed
    /// publication ledger and never marks its values as garbage.
    pub abandoned: bool,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct RequestBaseArchiveGarbage {
    pub database_id: String,
    pub generation: u64,
    pub manifest_hash: Digest,
    pub rows_removed: u64,
    pub is_complete: bool,
}

#[derive(Default)]
struct GarbageCandidates {
    segments: Vec<Digest>,
    programs: Vec<Digest>,
    tree_publications: Vec<TreePublicationGarbage>,
    tree_build_intents: Vec<TreeBuildIntentGarbage>,
    tree_manifests: Vec<Digest>,
    tree_nodes: Vec<Digest>,
    request_base_archives: Vec<RequestBaseArchiveGarbage>,
    log_generations: Vec<LogGenerationGarbage>,
    semantic_roots: Vec<SemanticCommitmentRootGarbage>,
    semantic_nodes: Vec<Digest>,
}

impl GarbageCandidates {
    fn inventory(self, applied: bool) -> GarbageInventory {
        GarbageInventory {
            segment_hashes: self.segments,
            program_hashes: self.programs,
            tree_publications: self.tree_publications,
            tree_build_intents: self.tree_build_intents,
            tree_manifest_hashes: self.tree_manifests,
            tree_node_hashes: self.tree_nodes,
            request_base_archives: self.request_base_archives,
            log_generations: self.log_generations,
            semantic_commitment_roots: self.semantic_roots,
            semantic_commitment_node_hashes: self.semantic_nodes,
            applied,
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ExcisionFault {
    None,
    AfterCapture,
    AfterCandidateStaged,
    AfterActivation,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ExcisionReceipt {
    pub database_id: String,
    pub source_generation: u64,
    pub generation: u64,
    pub basis_t: u64,
    pub request_count: u64,
    pub removed_datoms: u64,
    pub old_head_hash: Digest,
    pub new_head_hash: Digest,
    pub resumed: bool,
}

impl IntegrityReport {
    pub fn healthy(&self) -> bool {
        self.problems.is_empty()
    }
}

pub struct PostgresOperator {
    client: Client,
    _connection: PostgresConnectionConfig,
}

impl PostgresOperator {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        let mut client = connection.connect_for("operations/connect")?;
        verify_schema_compatibility(&mut client)?;
        Ok(Self {
            client,
            _connection: connection.clone(),
        })
    }

    pub fn inspect_database(
        &mut self,
        database_id: &str,
        deep_derived: bool,
    ) -> Result<IntegrityReport, SemanticError> {
        // Every figure and the semantic recovery below must describe one
        // PostgreSQL snapshot. Read Committed would permit a later query to
        // observe a head/root publication absent from earlier counts.
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .read_only(true)
            .start()
            .map_err(|error| operation_error("operations/inspection-begin", error))?;
        let head = transaction
            .query_opt(
                "SELECT basis_t, tx_hash, log_generation \
                   FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/head", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    crate::ErrorCategory::NotFound,
                    "operations/database-not-found",
                    format!("database {database_id} does not exist"),
                )
            })?;
        let basis = positive_or_zero(head.get::<_, i64>(0), "head basis")?;
        let head_hash = digest(head.get::<_, Vec<u8>>(1), "head hash")?;
        let log_generation = positive_or_zero(head.get(2), "head log generation")?;
        let mut metrics = OperationalMetrics {
            basis_t: basis,
            ..OperationalMetrics::default()
        };
        let mut problems = Vec::new();
        let catalog = transaction
            .query_one(
                "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/genesis", error))?;
        let genesis: Vec<u8> = catalog.get(0);
        let genesis_hash = digest(catalog.get(1), "genesis hash")?;
        if sha256(&genesis) != genesis_hash || decode_genesis(&genesis).is_err() {
            problem(
                &mut problems,
                "integrity/invalid-genesis",
                "genesis is not canonical or does not match its hash",
            );
        }
        let mut previous = genesis_hash;
        if log_generation == 0 {
            let rows = transaction
                .query(
                    "SELECT basis_t, previous_hash, tx_hash, payload \
                     FROM atomic_transactions WHERE database_id = $1 ORDER BY basis_t",
                    &[&database_id],
                )
                .map_err(|error| operation_error("operations/transactions", error))?;
            for (offset, row) in rows.iter().enumerate() {
                let expected = offset as u64 + 1;
                let actual = positive_or_zero(row.get(0), "transaction basis")?;
                let stored_previous = digest(row.get(1), "previous hash")?;
                let stored_hash = digest(row.get(2), "transaction hash")?;
                let payload: Vec<u8> = row.get(3);
                metrics.transactions += 1;
                metrics.transaction_bytes += payload.len() as u64;
                if actual != expected {
                    problem(
                        &mut problems,
                        "integrity/noncontiguous-basis",
                        format!("expected basis {expected}, found {actual}"),
                    );
                }
                if stored_previous != previous {
                    problem(
                        &mut problems,
                        "integrity/predecessor-mismatch",
                        format!("basis {actual} does not name the preceding transaction"),
                    );
                }
                if transaction_hash(&payload) != stored_hash {
                    problem(
                        &mut problems,
                        "integrity/transaction-hash-mismatch",
                        format!("basis {actual} payload does not match its hash"),
                    );
                }
                match decode_transaction(&payload) {
                    Ok(transaction)
                        if transaction.database_id == database_id
                            && transaction.basis_t == actual
                            && transaction.previous_hash == stored_previous => {}
                    Ok(_) => problem(
                        &mut problems,
                        "integrity/transaction-envelope-mismatch",
                        format!("basis {actual} envelope metadata does not match its row"),
                    ),
                    Err(error) => problem(
                        &mut problems,
                        error.code,
                        format!("basis {actual}: {}", error.message),
                    ),
                }
                previous = stored_hash;
            }
        } else {
            match read_authenticated_log_range(
                &mut transaction,
                database_id,
                log_generation,
                0,
                basis,
                genesis_hash,
            ) {
                Ok(rows) => {
                    metrics.transactions = rows.len() as u64;
                    metrics.transaction_bytes =
                        rows.iter().map(|row| row.payload.len() as u64).sum();
                    previous = rows.last().map_or(genesis_hash, |row| row.tx_hash);
                }
                Err(error) => problem(&mut problems, error.code, error.message),
            }
        }
        if metrics.transactions != basis || previous != head_hash {
            problem(
                &mut problems,
                "integrity/head-mismatch",
                "head does not match the complete transaction chain",
            );
        }
        let (dangling, requests): (i64, i64) = if log_generation == 0 {
            let dangling = transaction
                .query_one(
                    "SELECT count(*) FROM atomic_requests r \
                     LEFT JOIN atomic_transactions t \
                       ON t.database_id = r.database_id AND t.basis_t = r.basis_t \
                      AND t.tx_hash = r.tx_hash \
                     WHERE r.database_id = $1 AND t.database_id IS NULL",
                    &[&database_id],
                )
                .map_err(|error| operation_error("operations/requests", error))?
                .get(0);
            let requests = transaction
                .query_one(
                    "SELECT count(*) FROM atomic_requests WHERE database_id=$1",
                    &[&database_id],
                )
                .map_err(|error| operation_error("operations/requests", error))?
                .get(0);
            (dangling, requests)
        } else {
            let row = transaction
                .query_one(
                    "SELECT count(*) FILTER (WHERE t.database_id IS NULL), count(*) \
                       FROM atomic_generation_requests r \
                       LEFT JOIN atomic_generation_transactions t \
                         ON t.database_id=r.database_id AND t.generation=r.generation \
                        AND t.basis_t=r.basis_t AND t.tx_hash=r.tx_hash \
                      WHERE r.database_id=$1 AND r.generation=$2",
                    &[&database_id, &sql_u64(log_generation, "log generation")?],
                )
                .map_err(|error| operation_error("operations/generation-requests", error))?;
            (row.get(0), row.get(1))
        };
        metrics.requests = positive_or_zero(requests, "request count")?;
        if dangling != 0 {
            problem(
                &mut problems,
                "integrity/dangling-request",
                format!("{dangling} durable requests do not identify a transaction"),
            );
        }
        if log_generation > 0 {
            let invalid_native_bases: i64 = transaction
                .query_one(
                    "SELECT count(*) \
                       FROM atomic_generation_requests request \
                       LEFT JOIN atomic_generation_request_bases base \
                         ON base.database_id = request.database_id \
                        AND base.generation = request.generation \
                        AND base.request_key_hash = request.request_key_hash \
                       LEFT JOIN atomic_tree_publications publication \
                         ON publication.manifest_hash = base.base_manifest_hash \
                        AND publication.database_id = request.database_id \
                        AND publication.log_generation = request.generation \
                       LEFT JOIN atomic_tree_manifests manifest \
                         ON manifest.manifest_hash = publication.manifest_hash \
                        AND manifest.database_id = publication.database_id \
                        AND manifest.publication_revision = publication.publication_revision \
                        AND manifest.basis_t = publication.basis_t \
                        AND manifest.tx_hash = publication.tx_hash \
                        AND manifest.log_generation = publication.log_generation \
                       LEFT JOIN atomic_semantic_commitment_roots semantic \
                         ON semantic.database_id = manifest.database_id \
                        AND semantic.generation = manifest.log_generation \
                        AND semantic.basis_t = manifest.basis_t \
                        AND semantic.tx_hash = manifest.tx_hash \
                        AND semantic.state_hash = manifest.state_hash \
                        AND semantic.eidx_frontier = manifest.eidx_frontier \
                        AND semantic.commitment_version = 2 \
                       LEFT JOIN atomic_request_base_archives archive \
                         ON archive.manifest_hash = base.base_manifest_hash \
                        AND archive.database_id = request.database_id \
                        AND archive.generation = request.generation \
                       LEFT JOIN atomic_request_base_archive_completions archive_complete \
                         ON archive_complete.manifest_hash = archive.manifest_hash \
                       LEFT JOIN atomic_semantic_commitment_roots archive_semantic \
                         ON archive_semantic.database_id = archive.database_id \
                        AND archive_semantic.generation = archive.generation \
                        AND archive_semantic.basis_t = archive.basis_t \
                        AND archive_semantic.tx_hash = archive.tx_hash \
                        AND archive_semantic.state_hash = archive.state_hash \
                        AND archive_semantic.eidx_frontier = archive.eidx_frontier \
                        AND archive_semantic.commitment_version = 2 \
                      WHERE request.database_id = $1 AND request.generation = $2 \
                        AND ( \
                             (request.request_kind = 2 AND ( \
                                  base.base_manifest_hash IS NULL \
                                  OR ( \
                                      CASE WHEN publication.manifest_hash IS NOT NULL \
                                                AND manifest.manifest_hash IS NOT NULL \
                                                AND manifest.manifest_version IN (4, 5) \
                                                AND semantic.database_id IS NOT NULL \
                                                AND publication.basis_t <= request.basis_t - 1 \
                                                AND NOT EXISTS ( \
                                                    SELECT 1 FROM atomic_tree_retirement_progress progress \
                                                     WHERE progress.manifest_hash = base.base_manifest_hash \
                                                ) \
                                           THEN 1 ELSE 0 END \
                                      + CASE WHEN archive.manifest_hash IS NOT NULL \
                                                   AND archive.manifest_version IN (4, 5) \
                                                   AND archive_complete.manifest_hash IS NOT NULL \
                                                   AND archive_semantic.database_id IS NOT NULL \
                                                   AND archive.basis_t <= request.basis_t - 1 \
                                              THEN 1 ELSE 0 END \
                                  ) <> 1 \
                             )) \
                             OR (request.request_kind <> 2 \
                                 AND base.base_manifest_hash IS NOT NULL) \
                        )",
                    &[&database_id, &sql_u64(log_generation, "log generation")?],
                )
                .map_err(|error| operation_error("operations/request-bases", error))?
                .get(0);
            if invalid_native_bases != 0 {
                problem(
                    &mut problems,
                    "integrity/invalid-request-base",
                    format!(
                        "{invalid_native_bases} native requests lack one exact authenticated db-before base"
                    ),
                );
            }
        }

        metrics.manifests = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            database_id,
        )?;
        // The append-only publication row, not a merely uploaded manifest,
        // makes a legacy derived value eligible. Interrupted content-first
        // writes therefore remain harmless orphan candidates.
        let manifests = transaction
            .query(
                "SELECT p.basis_t, p.tx_hash, p.manifest_hash, \
                        m.database_id, m.basis_t, m.tx_hash, m.manifest_hash, m.payload \
                 FROM atomic_index_publications p \
                 LEFT JOIN atomic_index_manifests m \
                   ON m.database_id = p.database_id AND m.basis_t = p.basis_t \
                  AND m.tx_hash = p.tx_hash AND m.manifest_hash = p.manifest_hash \
                 WHERE p.database_id = $1 ORDER BY p.basis_t",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/manifests", error))?;
        let mut referenced_segments = BTreeSet::new();
        for row in manifests {
            let manifest_basis = positive_or_zero(row.get(0), "manifest basis")?;
            let tx_hash = digest(row.get(1), "manifest transaction hash")?;
            let manifest_hash = digest(row.get(2), "manifest hash")?;
            let stored_database: Option<String> = row.get(3);
            let stored_basis: Option<i64> = row.get(4);
            let stored_tx: Option<Vec<u8>> = row.get(5);
            let stored_hash: Option<Vec<u8>> = row.get(6);
            let payload: Option<Vec<u8>> = row.get(7);
            let (
                Some(stored_database),
                Some(stored_basis),
                Some(stored_tx),
                Some(stored_hash),
                Some(payload),
            ) = (
                stored_database,
                stored_basis,
                stored_tx,
                stored_hash,
                payload,
            )
            else {
                problem(
                    &mut problems,
                    "integrity/missing-manifest",
                    format!("published manifest at basis {manifest_basis} is absent"),
                );
                continue;
            };
            let stored_basis = positive_or_zero(stored_basis, "stored manifest basis")?;
            let stored_tx = digest(stored_tx, "stored manifest transaction hash")?;
            let stored_hash = digest(stored_hash, "stored manifest hash")?;
            let mut valid = stored_database == database_id
                && stored_basis == manifest_basis
                && stored_tx == tx_hash
                && stored_hash == manifest_hash;
            if sha256(&payload) != manifest_hash {
                problem(
                    &mut problems,
                    "integrity/manifest-hash-mismatch",
                    format!("manifest at basis {manifest_basis} has invalid hash"),
                );
                continue;
            }
            match decode_index_manifest(&payload) {
                Ok(manifest) => {
                    if manifest.database_id != database_id
                        || manifest.basis_t != manifest_basis
                        || manifest.tx_hash != tx_hash
                    {
                        valid = false;
                        problem(
                            &mut problems,
                            "integrity/manifest-envelope-mismatch",
                            format!("manifest at basis {manifest_basis} does not match its row"),
                        );
                    }
                    if valid {
                        referenced_segments.extend(manifest.segments.iter().map(|item| item.hash));
                        metrics.index_basis_t = metrics.index_basis_t.max(manifest_basis);
                    }
                }
                Err(error) => problem(&mut problems, error.code, error.message),
            }
        }
        for hash in &referenced_segments {
            let row = transaction
                .query_opt(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/segment", error))?;
            let Some(row) = row else {
                problem(
                    &mut problems,
                    "integrity/missing-segment",
                    format!("manifest references absent segment {}", hex(hash)),
                );
                continue;
            };
            let payload: Vec<u8> = row.get(0);
            metrics.segments = metrics.segments.saturating_add(1);
            metrics.segment_bytes = metrics.segment_bytes.saturating_add(payload.len() as u64);
            if sha256(&payload) != *hash {
                problem(
                    &mut problems,
                    "integrity/segment-hash-mismatch",
                    format!("segment {} has invalid content hash", hex(hash)),
                );
            } else if deep_derived && let Err(error) = decode_index_segment(&payload) {
                problem(&mut problems, error.code, error.message);
            }
        }
        inspect_native_trees(
            &mut transaction,
            database_id,
            deep_derived,
            &mut metrics,
            &mut problems,
        )?;
        metrics.request_base_archives = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_request_base_archives WHERE database_id = $1",
            database_id,
        )?;
        metrics.request_base_archive_nodes = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_request_base_archive_nodes node \
              JOIN atomic_request_base_archives archive \
                ON archive.manifest_hash = node.manifest_hash \
             WHERE archive.database_id = $1",
            database_id,
        )?;
        metrics.request_base_bindings = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_generation_request_bases WHERE database_id = $1",
            database_id,
        )?;
        inspect_semantic_commitments(&mut transaction, database_id, &mut metrics)?;
        metrics.index_lag = basis.saturating_sub(metrics.index_basis_t);
        match global_derived_reachability(&mut transaction, &BTreeSet::new())? {
            Some(reachable) => {
                metrics.orphan_segments = count_unreachable_hashes(
                    &mut transaction,
                    "SELECT segment_hash FROM atomic_index_segments",
                    &reachable.legacy_segments,
                    "stored segment hash",
                )?;
                metrics.orphan_tree_nodes = count_unreachable_hashes(
                    &mut transaction,
                    "SELECT node_hash FROM atomic_tree_nodes",
                    &reachable.tree_nodes,
                    "stored tree node hash",
                )?;
            }
            None => {
                metrics.shared_reachability_uncertain = true;
                metrics.orphan_segments = 0;
                metrics.orphan_tree_nodes = 0;
            }
        }
        metrics.programs = count_global(&mut transaction, "SELECT count(*) FROM atomic_programs")?;
        // Inspection must report corrupt durable values rather than aborting
        // before it can return an integrity report.  GC deliberately keeps
        // using the strict scanner below because deletion is unsafe when any
        // temporal reference is unreadable.
        let temporal_programs =
            inspect_temporal_program_references(&mut transaction, database_id, &mut problems)?;
        let stored_programs = transaction
            .query("SELECT program_hash FROM atomic_programs", &[])
            .map_err(|error| operation_error("operations/program-metrics", error))?;
        metrics.orphan_programs = match temporal_programs {
            Some(temporal_programs) => stored_programs
                .into_iter()
                .map(|row| digest(row.get(0), "program hash"))
                .collect::<Result<Vec<_>, _>>()?
                .into_iter()
                .filter(|hash| !temporal_programs.contains(hash))
                .count() as u64,
            // A corrupt database outside the inspected identity makes the
            // global orphan count unknowable, but must not make this
            // database's inspection fail or appear unhealthy.  Zero is the
            // conservative (never falsely reclaimable) count.
            None => 0,
        };
        metrics.active_leases = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_transactor_leases \
             WHERE lease_scope = $1 AND expires_at > clock_timestamp()",
            database_id,
        )?;

        match crate::postgres::recover_to(&mut transaction, database_id, basis, head_hash) {
            Ok(recovered) => {
                let database = recovered.database;
                metrics.current_datoms = database
                    .datoms(crate::View::Current, crate::IndexOrder::Eavt)
                    .len() as u64;
                metrics.history_datoms = database
                    .datoms(crate::View::History, crate::IndexOrder::Eavt)
                    .len() as u64;
            }
            Err(error) => problem(&mut problems, error.code, error.message),
        }
        let report = IntegrityReport {
            database_id: database_id.into(),
            metrics,
            problems,
        };
        transaction
            .commit()
            .map_err(|error| operation_error("operations/inspection-commit", error))?;
        Ok(report)
    }

    /// Preview physical values older than the caller-selected age horizon.
    ///
    /// Passing an age below [`RECOMMENDED_GARBAGE_COLLECTION_AGE`], including
    /// zero, is an explicit operational choice for imports or controlled
    /// maintenance; it is not rejected. Connected native snapshots are still
    /// pinned, but the caller must account for disconnected/long-lived readers.
    pub fn garbage_inventory(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        let millis = garbage_age_millis(older_than)?;
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .start()
            .map_err(|error| operation_error("operations/gc-inventory-begin", error))?;
        let candidates = garbage_candidates(&mut transaction, millis)?;
        transaction
            .commit()
            .map_err(|error| operation_error("operations/gc-inventory-commit", error))?;
        Ok(candidates.inventory(false))
    }

    /// Reclaim physical values older than the caller-selected age horizon.
    ///
    /// The horizon may deliberately be shorter than
    /// [`RECOMMENDED_GARBAGE_COLLECTION_AGE`]. Exact references and connected
    /// snapshot pins remain protected; a short horizon reduces the safety
    /// margin for readers whose PostgreSQL pin session is unavailable.
    pub fn collect_garbage(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        let millis = garbage_age_millis(older_than)?;
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .start()
            .map_err(|error| operation_error("operations/gc-begin", error))?;
        let candidates = garbage_candidates(&mut transaction, millis)?;
        // A sealed build-intent ledger remains an exact liveness pin after its
        // publication delta has folded. Drain that bookkeeping before retiring
        // the matching root: otherwise the retirement can make a legitimately
        // published state-2 intent look like an unpublished activated build to
        // the intent owner later in this same transaction.
        for intent in &candidates.tree_build_intents {
            let collected: bool = transaction
                .query_one(
                    "SELECT atomic_collect_tree_build_intent($1, $2, $3)",
                    &[
                        &&intent.manifest_hash[..],
                        &millis,
                        &(MAX_TREE_BUILD_INTENT_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-tree-build-intent", error))?
                .get(0);
            require_gc_collected(
                collected,
                &format!(
                    "tree build intent database={} generation={} expected-revision={} \
                     manifest={} abandoned={}",
                    intent.database_id,
                    intent.log_generation,
                    intent.expected_revision,
                    hex(&intent.manifest_hash),
                    intent.abandoned,
                ),
            )?;
        }
        for publication in &candidates.tree_publications {
            let collected: bool = transaction
                .query_one(
                    "SELECT atomic_collect_tree_retirement($1, $2, $3, $4, $5)",
                    &[
                        &publication.database_id,
                        &sql_u64(publication.publication_revision, "publication revision")?,
                        &&publication.manifest_hash[..],
                        &millis,
                        &(MAX_TREE_RETIREMENT_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-tree-publication", error))?
                .get(0);
            require_gc_collected(
                collected,
                &format!(
                    "tree publication database={} revision={} manifest={}",
                    publication.database_id,
                    publication.publication_revision,
                    hex(&publication.manifest_hash),
                ),
            )?;
        }
        // Raw values are selected only from the durable exact-mark ledger by
        // the fixed-path owner function. Replace the dry prediction with the
        // hashes actually deleted under this same snapshot.
        let mut collected_nodes = transaction
            .query(
                "SELECT node_hash FROM atomic_collect_tree_garbage($1) AS node_hash",
                &[&(MAX_TREE_NODES_PER_GC as i64)],
            )
            .map_err(|error| operation_error("operations/gc-tree-nodes", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "collected tree node hash"))
            .collect::<Result<Vec<_>, _>>()?;
        collected_nodes.sort_unstable();
        if collected_nodes != candidates.tree_nodes {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-tree-preview-diverged",
                "tree-value collection diverged from its same-snapshot preview",
            ));
        }
        // Receipt archives are generation roots, not ordinary accelerator
        // publications. Release one bounded archive before any semantic/log
        // phase so the owning generation becomes irrevocably collectible
        // before its exact db-before bindings disappear.
        let mut collected_request_base_archives =
            Vec::with_capacity(candidates.request_base_archives.len());
        for candidate in &candidates.request_base_archives {
            let row = transaction
                .query_one(
                    "SELECT rows_removed, is_complete \
                       FROM atomic_collect_request_base_archive($1, $2, $3, $4, $5)",
                    &[
                        &candidate.database_id,
                        &sql_u64(candidate.generation, "request-base archive generation")?,
                        &&candidate.manifest_hash[..],
                        &millis,
                        &(MAX_REQUEST_BASE_ARCHIVE_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-request-base-archive", error))?;
            collected_request_base_archives.push(RequestBaseArchiveGarbage {
                database_id: candidate.database_id.clone(),
                generation: candidate.generation,
                manifest_hash: candidate.manifest_hash,
                rows_removed: positive_or_zero(row.get(0), "collected request-base archive rows")?,
                is_complete: row.get(1),
            });
        }
        if collected_request_base_archives != candidates.request_base_archives {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-request-base-archive-preview-diverged",
                "request-base archive collection diverged from its same-snapshot preview",
            ));
        }
        let mut collected_programs = transaction
            .query(
                "SELECT program_hash FROM atomic_collect_program_garbage($1, $2) AS program_hash",
                &[&millis, &(MAX_PROGRAMS_PER_GC as i64)],
            )
            .map_err(|error| operation_error("operations/gc-programs", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "collected program hash"))
            .collect::<Result<Vec<_>, _>>()?;
        collected_programs.sort_unstable();
        if collected_programs != candidates.programs {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-program-preview-diverged",
                "program collection diverged from its same-snapshot preview",
            ));
        }
        // Drain only the frontier visible in the preview. Semantic roots
        // removed below may expose parent nodes; those become work for the
        // next bounded call, mirroring native-tree publication work.
        let mut collected_semantic_nodes = transaction
            .query(
                "SELECT node_hash \
                   FROM atomic_collect_semantic_commitment_garbage($1, $2) AS node_hash",
                &[&millis, &(MAX_SEMANTIC_COMMITMENT_NODES_PER_GC as i64)],
            )
            .map_err(|error| operation_error("operations/gc-semantic-nodes", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "collected semantic commitment node"))
            .collect::<Result<Vec<_>, _>>()?;
        collected_semantic_nodes.sort_unstable();
        if collected_semantic_nodes != candidates.semantic_nodes {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-semantic-node-preview-diverged",
                "semantic-node collection diverged from its same-snapshot preview",
            ));
        }
        let mut collected_semantic_roots = Vec::new();
        let mut collected_generations = Vec::with_capacity(candidates.log_generations.len());
        for candidate in &candidates.log_generations {
            if candidate.semantic_roots_removed > 0 {
                let rows = transaction
                    .query(
                        "SELECT basis_t, tx_hash, current_root \
                           FROM atomic_collect_semantic_commitment_generation_roots(\
                                $1, $2, $3, $4, $5)",
                        &[
                            &candidate.database_id,
                            &sql_u64(candidate.generation, "log generation")?,
                            &millis,
                            &(MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC as i64),
                            &candidate.abandoned,
                        ],
                    )
                    .map_err(|error| operation_error("operations/gc-semantic-roots", error))?;
                for row in rows {
                    collected_semantic_roots.push(SemanticCommitmentRootGarbage {
                        database_id: candidate.database_id.clone(),
                        generation: candidate.generation,
                        basis_t: positive_or_zero(row.get(0), "semantic root basis")?,
                        tx_hash: digest(row.get(1), "semantic root transaction hash")?,
                        current_root: optional_digest(
                            row.get(2),
                            "semantic commitment current root",
                        )?,
                    });
                }
                collected_generations.push(candidate.clone());
                continue;
            }
            let collector = if candidate.abandoned {
                "SELECT rows_removed, abandonment_phase, is_complete \
                   FROM atomic_abandon_log_generation($1, $2, $3, $4)"
            } else {
                "SELECT rows_removed, collection_phase, is_complete \
                   FROM atomic_collect_log_generation($1, $2, $3, $4)"
            };
            let row = transaction
                .query_one(
                    collector,
                    &[
                        &candidate.database_id,
                        &sql_u64(candidate.generation, "log generation")?,
                        &millis,
                        &(MAX_LOG_GENERATION_ROWS_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-log-generation", error))?;
            collected_generations.push(LogGenerationGarbage {
                database_id: candidate.database_id.clone(),
                generation: candidate.generation,
                abandoned: candidate.abandoned,
                collection_phase: positive_i16(row.get(1), "log collection phase")?,
                rows_removed: positive_or_zero(row.get(0), "collected log-generation rows")?,
                semantic_roots_removed: 0,
                is_complete: row.get(2),
            });
        }
        if collected_generations != candidates.log_generations {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-log-generation-preview-diverged",
                "retired log-generation collection diverged from its same-snapshot preview",
            ));
        }
        collected_semantic_roots.sort_unstable();
        if collected_semantic_roots != candidates.semantic_roots {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "operations/gc-semantic-root-preview-diverged",
                "semantic-root collection diverged from its same-snapshot preview",
            ));
        }
        // Publication visibility is independent of derived membership. Move
        // one root's restart-safe fold only after this call has drained the
        // exact values it previewed; marks enabled by the fold are therefore
        // considered by the next call, never silently added to this result.
        if let Some(row) = transaction
            .query_opt(
                "SELECT h.manifest_hash \
                   FROM atomic_tree_delta_headers h \
                   JOIN atomic_tree_publications p ON p.manifest_hash = h.manifest_hash \
                  WHERE h.delta_state = 2 \
                  ORDER BY p.published_at, p.database_id, p.publication_revision \
                  LIMIT 1",
                &[],
            )
            .map_err(|error| operation_error("operations/gc-publication-work-read", error))?
        {
            let manifest_hash = digest(row.get(0), "pending tree publication hash")?;
            transaction
                .query_one(
                    "SELECT atomic_apply_tree_publication_work($1, $2)",
                    &[
                        &&manifest_hash[..],
                        &(MAX_TREE_RETIREMENT_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-publication-work", error))?;
        }
        transaction
            .commit()
            .map_err(|error| operation_error("operations/gc-commit", error))?;
        Ok(candidates.inventory(true))
    }

    /// Process ordinary A=15 (`:db/excise`) facts through a background,
    /// copy-on-write log generation. A backup remains strongly recommended,
    /// matching Datomic's operational guidance, but it is not an authorization
    /// token and is therefore absent from this semantic API.
    pub fn process_excision_requests(
        &mut self,
        database_id: &str,
    ) -> Result<ExcisionReceipt, SemanticError> {
        self.process_excision_requests_with_fault(database_id, ExcisionFault::None)
    }

    #[doc(hidden)]
    pub fn process_excision_requests_with_fault(
        &mut self,
        database_id: &str,
        fault_point: ExcisionFault,
    ) -> Result<ExcisionReceipt, SemanticError> {
        process_excision_with_session_fences(
            &mut self.client,
            &self._connection,
            database_id,
            fault_point,
        )
    }

    /// True only when every A=15 request at or before `through_t` is reflected
    /// by the active generation and its root-last completion marker.
    pub fn sync_excise(
        &mut self,
        database_id: &str,
        through_t: u64,
    ) -> Result<bool, SemanticError> {
        sync_excise_in(&mut self.client, database_id, through_t)
    }
}

const EXCISION_LOG_BATCH: u64 = 256;
const EXCISION_COMPLETION_BATCH: i64 = 512;

fn process_excision_with_session_fences(
    client: &mut Client,
    connection: &PostgresConnectionConfig,
    database_id: &str,
    fault_point: ExcisionFault,
) -> Result<ExcisionReceipt, SemanticError> {
    let lock_row = client
        .query_opt(
            "SELECT atomic_tree_database_build_pin_key($1), \
                    hashtextextended('atomic/excision-worker/v1/' || lineage_id, \
                                     4707476001900298240::bigint) \
               FROM atomic_databases WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/lock-coordinates", error))?
        .ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::NotFound,
                "excision/database-not-found",
                format!("database {database_id} does not exist"),
            )
        })?;
    let builder_key: i64 = lock_row.get(0);
    let worker_key: i64 = lock_row.get(1);
    let builder_locked: bool = client
        .query_one("SELECT pg_try_advisory_lock_shared($1)", &[&builder_key])
        .map_err(|error| operation_error("excision/builder-pin", error))?
        .get(0);
    if !builder_locked {
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "excision/restore-in-progress",
            "a point restore currently fences generation builders",
        ));
    }
    let worker_locked = match client.query_one("SELECT pg_try_advisory_lock($1)", &[&worker_key]) {
        Ok(row) => row.get::<_, bool>(0),
        Err(error) => {
            // The first session lock survives statement errors. Do not leave
            // this long-lived operator handle silently fencing restores when
            // acquisition of the stacked worker coordinate fails.
            let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&builder_key]);
            return Err(operation_error("excision/worker-pin", error));
        }
    };
    if !worker_locked {
        let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&builder_key]);
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "excision/already-running",
            "another background excision worker owns this database",
        ));
    }

    let result = process_excision_fenced(client, connection, database_id, fault_point);
    let worker_released = client
        .query_one("SELECT pg_advisory_unlock($1)", &[&worker_key])
        .is_ok_and(|row| row.get::<_, bool>(0));
    let builder_released = client
        .query_one("SELECT pg_advisory_unlock_shared($1)", &[&builder_key])
        .is_ok_and(|row| row.get::<_, bool>(0));
    if !worker_released || !builder_released {
        // This is a dedicated operator session. If precise stacked release
        // fails, dropping every session advisory lock is safer than returning
        // a handle that silently wedges future restore/excision operations.
        let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
    }
    match result {
        Err(error) => Err(error),
        Ok(receipt) => {
            if !worker_released || !builder_released {
                return Err(SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "excision/session-pin-lost",
                    "background excision session lost its advisory fence",
                ));
            }
            Ok(receipt)
        }
    }
}

fn process_excision_fenced(
    client: &mut Client,
    connection: &PostgresConnectionConfig,
    database_id: &str,
    fault_point: ExcisionFault,
) -> Result<ExcisionReceipt, SemanticError> {
    if let Some(receipt) = resume_activated_excision(client, connection, database_id)? {
        return Ok(receipt);
    }

    let head = client
        .query_opt(
            "SELECT h.log_generation, h.basis_t, h.tx_hash, d.lineage_id, d.genesis_hash \
               FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
              WHERE h.database_id = $1",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/read-head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::NotFound,
                "excision/database-not-found",
                format!("database {database_id} has no published head"),
            )
        })?;
    let source_generation = positive_or_zero(head.get(0), "source generation")?;
    let captured_basis = positive_or_zero(head.get(1), "source basis")?;
    let captured_hash = digest(head.get(2), "source head hash")?;
    let lineage_id: String = head.get(3);
    let genesis_hash = digest(head.get(4), "genesis hash")?;

    let source_pin_key: i64 = client
        .query_one(
            "SELECT atomic_log_generation_pin_key($1, $2)",
            &[
                &database_id,
                &sql_u64(source_generation, "source generation")?,
            ],
        )
        .map_err(|error| operation_error("excision/source-pin-key", error))?
        .get(0);
    let source_locked: bool = client
        .query_one("SELECT pg_try_advisory_lock_shared($1)", &[&source_pin_key])
        .map_err(|error| operation_error("excision/source-pin", error))?
        .get(0);
    if !source_locked {
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "excision/source-collecting",
            "the selected source generation is being collected",
        ));
    }
    let result = build_and_activate_excision(
        client,
        connection,
        database_id,
        &lineage_id,
        genesis_hash,
        source_generation,
        captured_basis,
        captured_hash,
        fault_point,
    );
    let release = client
        .query_one("SELECT pg_advisory_unlock_shared($1)", &[&source_pin_key])
        .map_err(|error| operation_error("excision/source-unpin", error));
    match result {
        Err(error) => Err(error),
        Ok(receipt) => {
            if !release?.get::<_, bool>(0) {
                return Err(SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "excision/source-pin-lost",
                    "background excision lost its source-generation pin",
                ));
            }
            Ok(receipt)
        }
    }
}

#[allow(clippy::too_many_arguments)]
fn build_and_activate_excision(
    client: &mut Client,
    connection: &PostgresConnectionConfig,
    database_id: &str,
    lineage_id: &str,
    genesis_hash: Digest,
    source_generation: u64,
    captured_basis: u64,
    captured_hash: Digest,
    fault_point: ExcisionFault,
) -> Result<ExcisionReceipt, SemanticError> {
    let mut snapshot = client
        .transaction()
        .map_err(|error| operation_error("excision/snapshot-begin", error))?;
    // Every builder allocates a database-local generation while holding this
    // row. Take it before the first MVCC read so a concurrently committed
    // restore/excision allocation cannot be invisible to MAX(generation).
    snapshot
        .query_one(
            "SELECT database_id FROM atomic_databases WHERE database_id = $1 FOR UPDATE",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/lock-generation-counter", error))?;
    let stable_head = snapshot
        .query_one(
            "SELECT log_generation, basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/snapshot-head", error))?;
    if positive_or_zero(stable_head.get(0), "snapshot generation")? != source_generation
        || positive_or_zero(stable_head.get(1), "snapshot basis")? != captured_basis
        || digest(stable_head.get(2), "snapshot head hash")? != captured_hash
    {
        return Err(SemanticError::new(
            crate::ErrorCategory::Conflict,
            "excision/source-changed-before-capture",
            "the active log changed while the excision source was being pinned",
        ));
    }
    let inactive = snapshot
        .query(
            "SELECT g.generation, b.captured_basis_t, b.captured_head_hash, \
                    b.frozen_plan_hash, g.request_count \
               FROM atomic_log_generations g \
               JOIN atomic_log_generation_builds b \
                 ON b.database_id=g.database_id AND b.generation=g.generation \
              WHERE g.database_id=$1 AND g.build_kind=1 \
                AND b.source_generation=$2 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                 WHERE a.database_id=g.database_id \
                                   AND a.generation=g.generation) \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress abandoned \
                                 WHERE abandoned.database_id=g.database_id \
                                   AND abandoned.generation=g.generation) \
              ORDER BY g.generation",
            &[
                &database_id,
                &sql_u64(source_generation, "source generation")?,
            ],
        )
        .map_err(|error| operation_error("excision/find-inactive-build", error))?;
    if inactive.len() > 1 {
        return Err(SemanticError::new(
            crate::ErrorCategory::Fault,
            "excision/multiple-inactive-builds",
            "more than one inactive excision build targets the active source generation",
        ));
    }
    let (generation, plan_basis, plan_source_hash, stored_plan_hash, stored_request_count, resumed) =
        if let Some(row) = inactive.first() {
            (
                positive_or_zero(row.get(0), "inactive generation")?,
                positive_or_zero(row.get(1), "inactive captured basis")?,
                digest(row.get(2), "inactive captured head")?,
                Some(digest(row.get(3), "inactive frozen plan")?),
                Some(positive_or_zero(row.get(4), "inactive request count")?),
                true,
            )
        } else {
            (
                positive_or_zero(
                    snapshot
                        .query_one(
                            "SELECT COALESCE(max(generation), 0) + 1 \
                               FROM atomic_log_generations WHERE database_id = $1",
                            &[&database_id],
                        )
                        .map_err(|error| operation_error("excision/allocate-generation", error))?
                        .get(0),
                    "allocated generation",
                )?,
                captured_basis,
                captured_hash,
                None,
                None,
                false,
            )
        };
    let source_database = recover_generation_to(
        &mut snapshot,
        database_id,
        source_generation,
        plan_basis,
        plan_source_hash,
    )?
    .database;
    let completed = completed_excision_identities(&mut snapshot, database_id, source_generation)?;
    let mut rewriter = GenerationRewriter::for_excision(
        lineage_id,
        generation,
        genesis_hash,
        &source_database,
        &completed,
    )?;
    let predicates = rewriter.frozen_predicates();
    let request_count = predicates.len() as u64;
    let request_set_hash = rewriter.request_set_hash();
    let resume_checkpoint = if let Some(stored_plan_hash) = stored_plan_hash {
        if stored_plan_hash != request_set_hash || stored_request_count != Some(request_count) {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/frozen-plan-mismatch",
                "inactive excision build no longer reconstructs to its frozen predicate set",
            ));
        }
        let mut expected_predicate_hashes = predicates
            .iter()
            .map(|predicate| predicate.hash)
            .collect::<Vec<_>>();
        expected_predicate_hashes.sort_unstable();
        let stored_predicate_hashes = snapshot
            .query(
                "SELECT predicate_hash FROM atomic_generation_excision_predicates \
                  WHERE database_id=$1 AND generation=$2 ORDER BY predicate_hash",
                &[&database_id, &sql_u64(generation, "generation")?],
            )
            .map_err(|error| operation_error("excision/resume-predicates", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "stored predicate hash"))
            .collect::<Result<Vec<_>, _>>()?;
        if stored_predicate_hashes != expected_predicate_hashes {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/frozen-predicates-mismatch",
                "inactive excision predicate rows disagree with the authenticated frozen plan",
            ));
        }
        let checkpoint = snapshot
            .query_one(
                "SELECT through_basis_t, head_hash, state_hash, source_head_hash, \
                        eidx_frontier, removed_datoms \
                   FROM atomic_log_generation_checkpoints \
                  WHERE database_id=$1 AND generation=$2 \
                  ORDER BY through_basis_t DESC LIMIT 1",
                &[&database_id, &sql_u64(generation, "generation")?],
            )
            .map_err(|error| operation_error("excision/resume-checkpoint", error))?;
        Some((
            positive_or_zero(checkpoint.get(0), "checkpoint basis")?,
            digest(checkpoint.get(1), "checkpoint head")?,
            digest(checkpoint.get(2), "checkpoint state")?,
            digest(checkpoint.get(3), "checkpoint source head")?,
            positive_or_zero(checkpoint.get(4), "checkpoint frontier")?,
            positive_or_zero(checkpoint.get(5), "checkpoint removed datoms")?,
        ))
    } else {
        snapshot
            .execute(
                "INSERT INTO atomic_log_generations \
                     (database_id, generation, lineage_id, build_kind, request_count) \
                 VALUES ($1, $2, $3, 1, $4)",
                &[
                    &database_id,
                    &sql_u64(generation, "generation")?,
                    &lineage_id,
                    &sql_u64(request_count, "request count")?,
                ],
            )
            .map_err(|error| operation_error("excision/create-generation", error))?;
        snapshot
            .execute(
                "INSERT INTO atomic_log_generation_builds \
                     (database_id, generation, source_generation, captured_basis_t, \
                      captured_head_hash, frozen_plan_hash) \
                 VALUES ($1, $2, $3, $4, $5, $6)",
                &[
                    &database_id,
                    &sql_u64(generation, "generation")?,
                    &sql_u64(source_generation, "source generation")?,
                    &sql_u64(plan_basis, "captured basis")?,
                    &&plan_source_hash[..],
                    &&request_set_hash[..],
                ],
            )
            .map_err(|error| operation_error("excision/create-build", error))?;
        for predicate in &predicates {
            insert_excision_predicate(&mut snapshot, database_id, generation, predicate)?;
        }
        insert_excision_checkpoint(
            &mut snapshot,
            database_id,
            generation,
            0,
            genesis_hash,
            rewriter.current_state_hash()?,
            genesis_hash,
            rewriter.current_eidx_frontier(),
            0,
        )?;
        None
    };
    snapshot
        .commit()
        .map_err(|error| operation_error("excision/capture-commit", error))?;

    if fault_point == ExcisionFault::AfterCapture {
        return Err(SemanticError::new(
            crate::ErrorCategory::Interrupted,
            "excision/injected-fault",
            "injected failure after freezing the excision source and plan",
        ));
    }

    if let Some(checkpoint) = resume_checkpoint {
        replay_source_through(
            client,
            database_id,
            source_generation,
            checkpoint.0,
            &mut rewriter,
        )?;
        if rewriter.current_head_hash() != checkpoint.1
            || rewriter.current_state_hash()? != checkpoint.2
            || rewriter.current_source_hash() != checkpoint.3
            || rewriter.current_eidx_frontier() != checkpoint.4
            || rewriter.removed_datoms() != checkpoint.5
        {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/checkpoint-mismatch",
                "inactive excision checkpoint does not match deterministic source replay",
            ));
        }
    }
    rewrite_source_through(
        client,
        database_id,
        source_generation,
        generation,
        plan_basis,
        &mut rewriter,
    )?;
    loop {
        let row = client
            .query_one(
                "SELECT rows_advanced, is_sealed \
                   FROM atomic_stage_excision_completions($1, $2, $3)",
                &[
                    &database_id,
                    &sql_u64(generation, "generation")?,
                    &EXCISION_COMPLETION_BATCH,
                ],
            )
            .map_err(|error| operation_error("excision/stage-completions", error))?;
        if row.get::<_, bool>(1) {
            break;
        }
    }
    if fault_point == ExcisionFault::AfterCandidateStaged {
        return Err(SemanticError::new(
            crate::ErrorCategory::Interrupted,
            "excision/injected-fault",
            "injected failure after the COW candidate became durable",
        ));
    }

    let source_had_tree: bool = client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                            WHERE database_id = $1 AND log_generation = $2)",
            &[
                &database_id,
                &sql_u64(source_generation, "source generation")?,
            ],
        )
        .map_err(|error| operation_error("excision/source-tree", error))?
        .get(0);
    let (final_basis, final_source_hash, manifest_hash, mut tree_store) = loop {
        let head = client
            .query_one(
                "SELECT log_generation, basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/catchup-head", error))?;
        let active_generation = positive_or_zero(head.get(0), "catch-up generation")?;
        let active_basis = positive_or_zero(head.get(1), "catch-up basis")?;
        let active_hash = digest(head.get(2), "catch-up hash")?;
        if active_generation != source_generation {
            return Err(SemanticError::new(
                crate::ErrorCategory::Conflict,
                "excision/source-generation-replaced",
                "another generation replaced the excision source",
            ));
        }
        if active_basis > rewriter.current_basis() {
            rewrite_source_through(
                client,
                database_id,
                source_generation,
                generation,
                active_basis,
                &mut rewriter,
            )?;
            continue;
        }
        rewriter.expect_through(active_basis);
        let candidate_state_hash = rewriter.current_state_hash()?;
        let mut staged_tree = if source_had_tree {
            let mut store = PostgresTreeStore::connect_configured(connection)?;
            let manifest_hash = stage_full_generation_tree(
                &mut store,
                database_id,
                generation,
                rewriter.current_head_hash(),
                candidate_state_hash,
                rewriter.current_database(),
            )?;
            Some((manifest_hash, store))
        } else {
            None
        };
        let manifest_parameter = staged_tree
            .as_ref()
            .map(|(manifest_hash, _)| manifest_hash.as_slice());
        // Excision already owns a complete materialized candidate. Convert
        // that endpoint into the PostgreSQL-resident semantic treap and
        // activate it in one transaction: neither a coordinate-less head nor
        // a root for a failed CAS can become visible.
        let activation = (|| {
            let mut transaction = client
                .transaction()
                .map_err(|error| operation_error("excision/activation-begin", error))?;
            crate::persistent_commitment::record_eager_endpoint(
                &mut transaction,
                database_id,
                generation,
                rewriter.current_head_hash(),
                candidate_state_hash,
                rewriter.current_database(),
            )?;
            transaction
                .query_one(
                    "SELECT atomic_activate_log_generation($1, $2, $3, $4, $5, $6)",
                    &[
                        &database_id,
                        &sql_u64(generation, "generation")?,
                        &sql_u64(active_basis, "activation basis")?,
                        &&rewriter.current_head_hash()[..],
                        &&candidate_state_hash[..],
                        &manifest_parameter,
                    ],
                )
                .map_err(|error| operation_error("excision/activate", error))?;
            transaction
                .commit()
                .map_err(|error| operation_error("excision/activation-commit", error))
        })();
        match activation {
            Ok(()) => {
                let (manifest_hash, tree_store) = staged_tree
                    .take()
                    .map_or((None, None), |(hash, store)| (Some(hash), Some(store)));
                break (active_basis, active_hash, manifest_hash, tree_store);
            }
            Err(semantic) => {
                if let Some((_, store)) = staged_tree.as_mut() {
                    store.release_build_intent()?;
                }
                if semantic.category == crate::ErrorCategory::Conflict {
                    continue;
                }
                return Err(semantic);
            }
        }
    };
    if fault_point == ExcisionFault::AfterActivation {
        return Err(SemanticError::new(
            crate::ErrorCategory::Interrupted,
            "excision/injected-fault",
            "injected failure after generation activation",
        ));
    }
    let manifest_parameter = manifest_hash.as_ref().map(|hash| hash.as_slice());
    client
        .query_one(
            "SELECT atomic_complete_excision_generation($1, $2, $3)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &manifest_parameter,
            ],
        )
        .map_err(|error| operation_error("excision/complete", error))?;
    if let (Some(manifest_hash), Some(store)) = (manifest_hash, tree_store.as_mut()) {
        drain_excision_tree_publication(client, store, manifest_hash)?;
        store.release_build_intent()?;
    }
    cleanup_completed_generation_build(client, database_id, generation)?;
    let outcome = rewriter.finish()?;
    Ok(ExcisionReceipt {
        database_id: database_id.to_owned(),
        source_generation,
        generation,
        basis_t: final_basis,
        request_count,
        removed_datoms: outcome.removed_datoms,
        old_head_hash: final_source_hash,
        new_head_hash: outcome.head_hash,
        resumed,
    })
}

fn rewrite_source_through(
    client: &mut Client,
    database_id: &str,
    source_generation: u64,
    candidate_generation: u64,
    through_basis: u64,
    rewriter: &mut GenerationRewriter,
) -> Result<(), SemanticError> {
    while rewriter.current_basis() < through_basis {
        let after = rewriter.current_basis();
        let through = through_basis.min(after.saturating_add(EXCISION_LOG_BATCH));
        let mut transaction = client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .start()
            .map_err(|error| operation_error("excision/rewrite-begin", error))?;
        let source_rows = read_authenticated_log_range(
            &mut transaction,
            database_id,
            source_generation,
            after,
            through,
            rewriter.current_source_hash(),
        )?;
        for source in source_rows {
            let source = source_log_row(source, source_generation)?;
            let row = rewriter.rewrite_row(source)?;
            insert_generation_log_row(&mut transaction, database_id, candidate_generation, &row)?;
            let transaction_value =
                LineageTransactionContent::decode(&row.payload)?.to_transaction(row.previous_hash);
            insert_program_generation_refs(
                &mut transaction,
                database_id,
                candidate_generation,
                &transaction_value.tx_data,
            )?;
        }
        insert_excision_checkpoint(
            &mut transaction,
            database_id,
            candidate_generation,
            rewriter.current_basis(),
            rewriter.current_head_hash(),
            rewriter.current_state_hash()?,
            rewriter.current_source_hash(),
            rewriter.current_eidx_frontier(),
            rewriter.removed_datoms(),
        )?;
        transaction
            .commit()
            .map_err(|error| operation_error("excision/rewrite-commit", error))?;
    }
    Ok(())
}

/// Reconstruct deterministic in-memory rewrite state through the newest
/// durable checkpoint. No candidate row is rewritten: the checkpoint is
/// compared to this replay before new suffix rows may be appended.
fn replay_source_through(
    client: &mut Client,
    database_id: &str,
    source_generation: u64,
    through_basis: u64,
    rewriter: &mut GenerationRewriter,
) -> Result<(), SemanticError> {
    while rewriter.current_basis() < through_basis {
        let after = rewriter.current_basis();
        let through = through_basis.min(after.saturating_add(EXCISION_LOG_BATCH));
        let rows = read_authenticated_log_range(
            client,
            database_id,
            source_generation,
            after,
            through,
            rewriter.current_source_hash(),
        )?;
        for row in rows {
            rewriter.rewrite_row(source_log_row(row, source_generation)?)?;
        }
    }
    Ok(())
}

fn source_log_row(
    row: AuthenticatedLogTransaction,
    generation: u64,
) -> Result<SourceLogRow, SemanticError> {
    let encoding = if generation == 0 {
        SourceLogEncoding::Legacy(row.payload)
    } else {
        SourceLogEncoding::Lineage {
            generation,
            content_hash: row.content_hash.ok_or_else(|| {
                SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "excision/missing-source-content-hash",
                    "positive source transaction lacks immutable content identity",
                )
            })?,
            payload: row.payload,
        }
    };
    let key = if generation == 0 {
        SourceRequestKey::LegacyPlaintext(row.legacy_request_key.ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/missing-source-request-key",
                "legacy source request lacks its durable identity",
            )
        })?)
    } else {
        SourceRequestKey::Digest(row.request_key_hash.ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/missing-source-request-key",
                "positive source request lacks its durable digest identity",
            )
        })?)
    };
    Ok(SourceLogRow {
        tx_hash: row.tx_hash,
        state_hash: row.state_hash,
        encoding,
        transaction: row.transaction,
        request: SourceRequest {
            key,
            request_digest: row.request_digest,
        },
    })
}

fn insert_excision_predicate<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    predicate: &PlannedExcisionPredicate,
) -> Result<(), SemanticError> {
    let (cutoff_kind, cutoff_value) = match predicate.request.cutoff {
        None => (0_i16, None),
        Some(ExcisionCutoff::BeforeT(t)) => (1, Some(sql_u64(t, "excision cutoff")?)),
        Some(ExcisionCutoff::BeforeInstant(instant)) => (2, Some(instant)),
    };
    let target_kind = match predicate.kind {
        ExcisionTargetKind::Entity => 0_i16,
        ExcisionTargetKind::Attribute => 1_i16,
    };
    let requested_attributes = predicate
        .request
        .attributes
        .iter()
        .map(|value| sql_u64(*value, "excision attribute"))
        .collect::<Result<Vec<_>, _>>()?;
    let component_extent = predicate
        .extent
        .iter()
        .map(|value| sql_u64(*value, "excision component"))
        .collect::<Result<Vec<_>, _>>()?;
    let reference_attributes = predicate
        .reference_attributes
        .iter()
        .map(|value| {
            i32::try_from(*value).map_err(|_| {
                SemanticError::new(
                    crate::ErrorCategory::Unsupported,
                    "excision/attribute-out-of-range",
                    "attribute id cannot be represented by PostgreSQL INTEGER",
                )
            })
        })
        .collect::<Result<Vec<_>, _>>()?;
    client
        .execute(
            "INSERT INTO atomic_generation_excision_predicates \
                 (database_id, generation, request_entity, request_t, target_id, \
                  target_kind, requested_cutoff_kind, requested_cutoff_value, \
                  effective_before_t, requested_attributes, component_extent, \
                  reference_attributes, protected_entity_target, predicate_hash) \
             VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &sql_u64(predicate.request.request_entity, "request entity")?,
                &sql_u64(predicate.request.request_t, "request transaction")?,
                &sql_u64(predicate.request.target, "target entity")?,
                &target_kind,
                &cutoff_kind,
                &cutoff_value,
                &sql_u64(predicate.before_t, "effective cutoff")?,
                &requested_attributes,
                &component_extent,
                &reference_attributes,
                &predicate.protected_entity_target,
                &&predicate.hash[..],
            ],
        )
        .map_err(|error| operation_error("excision/stage-predicate", error))?;
    Ok(())
}

fn insert_generation_log_row<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    row: &GenerationLogRow,
) -> Result<(), SemanticError> {
    client
        .execute(
            "INSERT INTO atomic_transaction_contents \
                 (content_hash, lineage_id, basis_t, eidx_frontier, payload) \
             SELECT $1, g.lineage_id, $2, $3, $4 \
               FROM atomic_log_generations g \
              WHERE g.database_id = $5 AND g.generation = $6 \
             ON CONFLICT (content_hash) DO NOTHING",
            &[
                &&row.content_hash[..],
                &sql_u64(row.basis_t, "transaction basis")?,
                &sql_u64(row.eidx_frontier, "entity frontier")?,
                &row.payload,
                &database_id,
                &sql_u64(generation, "generation")?,
            ],
        )
        .map_err(|error| operation_error("excision/stage-content", error))?;
    let stored = client
        .query_one(
            "SELECT basis_t, eidx_frontier, payload FROM atomic_transaction_contents \
              WHERE content_hash = $1",
            &[&&row.content_hash[..]],
        )
        .map_err(|error| operation_error("excision/verify-content", error))?;
    if positive_or_zero(stored.get(0), "content basis")? != row.basis_t
        || positive_or_zero(stored.get(1), "content frontier")? != row.eidx_frontier
        || stored.get::<_, Vec<u8>>(2) != row.payload
    {
        return Err(SemanticError::new(
            crate::ErrorCategory::Fault,
            "excision/content-hash-collision",
            "immutable transaction content hash resolves to different bytes or coordinates",
        ));
    }
    client
        .execute(
            "INSERT INTO atomic_generation_transactions \
                 (database_id,generation,basis_t,previous_hash,tx_hash,content_hash, \
                  state_hash,eidx_frontier) \
             VALUES ($1,$2,$3,$4,$5,$6,$7,$8)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &sql_u64(row.basis_t, "transaction basis")?,
                &&row.previous_hash[..],
                &&row.tx_hash[..],
                &&row.content_hash[..],
                &&row.state_hash[..],
                &sql_u64(row.eidx_frontier, "entity frontier")?,
            ],
        )
        .map_err(|error| operation_error("excision/stage-membership", error))?;
    client
        .execute(
            "INSERT INTO atomic_generation_requests \
                 (database_id,generation,request_key_hash,request_digest,request_kind, \
                  basis_t,tx_hash) VALUES ($1,$2,$3,$4,0,$5,$6)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &&row.request_key_hash[..],
                &&row.request_digest[..],
                &sql_u64(row.basis_t, "transaction basis")?,
                &&row.tx_hash[..],
            ],
        )
        .map_err(|error| operation_error("excision/stage-request", error))?;
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn insert_excision_checkpoint<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    through_basis: u64,
    head_hash: Digest,
    state_hash: Digest,
    source_head_hash: Digest,
    eidx_frontier: u64,
    removed_datoms: u64,
) -> Result<(), SemanticError> {
    client
        .execute(
            "INSERT INTO atomic_log_generation_checkpoints \
                 (database_id,generation,through_basis_t,head_hash,state_hash, \
                  source_head_hash,eidx_frontier,removed_datoms) \
             VALUES ($1,$2,$3,$4,$5,$6,$7,$8)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &sql_u64(through_basis, "checkpoint basis")?,
                &&head_hash[..],
                &&state_hash[..],
                &&source_head_hash[..],
                &sql_u64(eidx_frontier, "entity frontier")?,
                &sql_u64(removed_datoms, "removed datoms")?,
            ],
        )
        .map_err(|error| operation_error("excision/stage-checkpoint", error))?;
    Ok(())
}

fn completed_excision_identities<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
) -> Result<BTreeSet<(u64, u64)>, SemanticError> {
    if generation == 0 {
        return Ok(BTreeSet::new());
    }
    if !client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_completions \
                            WHERE database_id = $1 AND generation = $2)",
            &[&database_id, &sql_u64(generation, "generation")?],
        )
        .map_err(|error| operation_error("excision/source-completion", error))?
        .get::<_, bool>(0)
    {
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "excision/source-not-complete",
            "the active generation has not completed its derived-root publication",
        ));
    }
    client
        .query(
            "SELECT request_t, request_entity FROM atomic_completed_excision_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_t, request_entity",
            &[&database_id, &sql_u64(generation, "generation")?],
        )
        .map_err(|error| operation_error("excision/completed-requests", error))?
        .into_iter()
        .map(|row| {
            Ok((
                positive_or_zero(row.get(0), "completed request transaction")?,
                positive_or_zero(row.get(1), "completed request entity")?,
            ))
        })
        .collect()
}

fn cleanup_completed_generation_build(
    client: &mut Client,
    database_id: &str,
    generation: u64,
) -> Result<(), SemanticError> {
    loop {
        let row = client
            .query_one(
                "SELECT rows_removed, is_complete \
                   FROM atomic_cleanup_log_generation_build($1, $2, $3)",
                &[
                    &database_id,
                    &sql_u64(generation, "generation")?,
                    &EXCISION_COMPLETION_BATCH,
                ],
            )
            .map_err(|error| operation_error("excision/cleanup-build", error))?;
        if row.get::<_, bool>(1) {
            return Ok(());
        }
    }
}

fn resume_activated_excision(
    client: &mut Client,
    connection: &PostgresConnectionConfig,
    database_id: &str,
) -> Result<Option<ExcisionReceipt>, SemanticError> {
    let row = client
        .query_opt(
            "SELECT a.generation, a.prior_generation, a.basis_t, a.head_hash, \
                    a.manifest_hash, g.request_count, b.captured_head_hash, \
                    c.removed_datoms \
               FROM atomic_heads h \
               JOIN atomic_log_generation_activations a \
                 ON a.database_id=h.database_id AND a.generation=h.log_generation \
               JOIN atomic_log_generations g \
                 ON g.database_id=a.database_id AND g.generation=a.generation \
               JOIN atomic_log_generation_builds b \
                 ON b.database_id=g.database_id AND b.generation=g.generation \
               JOIN atomic_log_generation_checkpoints c \
                 ON c.database_id=g.database_id AND c.generation=g.generation \
                AND c.through_basis_t=a.basis_t AND c.head_hash=a.head_hash \
              WHERE h.database_id=$1 AND g.build_kind=1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_completions done \
                                 WHERE done.database_id=h.database_id \
                                   AND done.generation=h.log_generation)",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/resume-activation", error))?;
    let Some(row) = row else { return Ok(None) };
    let generation = positive_or_zero(row.get(0), "generation")?;
    let source_generation = positive_or_zero(row.get(1), "source generation")?;
    let basis_t = positive_or_zero(row.get(2), "activation basis")?;
    let new_head_hash = digest(row.get(3), "activation head")?;
    let manifest: Option<Vec<u8>> = row.get(4);
    let request_count = positive_or_zero(row.get(5), "request count")?;
    let captured_source_hash = digest(row.get(6), "captured source head")?;
    let removed_datoms = positive_or_zero(row.get(7), "removed datoms")?;
    let old_head_hash = if basis_t == 0 {
        digest(
            client
                .query_one(
                    "SELECT genesis_hash FROM atomic_databases WHERE database_id=$1",
                    &[&database_id],
                )
                .map_err(|error| operation_error("excision/resume-source-genesis", error))?
                .get(0),
            "source genesis hash",
        )?
    } else {
        let source_row = if source_generation == 0 {
            client.query_opt(
                "SELECT tx_hash FROM atomic_transactions \
                  WHERE database_id=$1 AND basis_t=$2",
                &[&database_id, &sql_u64(basis_t, "activation basis")?],
            )
        } else {
            client.query_opt(
                "SELECT tx_hash FROM atomic_generation_transactions \
                  WHERE database_id=$1 AND generation=$2 AND basis_t=$3",
                &[
                    &database_id,
                    &sql_u64(source_generation, "source generation")?,
                    &sql_u64(basis_t, "activation basis")?,
                ],
            )
        }
        .map_err(|error| operation_error("excision/resume-source-head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/resume-source-head-missing",
                "activation predecessor generation lacks its exact source endpoint",
            )
        })?;
        digest(source_row.get(0), "activation source head")?
    };
    if basis_t
        == positive_or_zero(
            client
                .query_one(
                    "SELECT captured_basis_t FROM atomic_log_generation_builds \
                      WHERE database_id=$1 AND generation=$2",
                    &[&database_id, &sql_u64(generation, "generation")?],
                )
                .map_err(|error| operation_error("excision/resume-captured-basis", error))?
                .get(0),
            "captured basis",
        )?
        && old_head_hash != captured_source_hash
    {
        return Err(SemanticError::new(
            crate::ErrorCategory::Fault,
            "excision/resume-source-head-mismatch",
            "activation predecessor disagrees with the frozen capture endpoint",
        ));
    }
    client
        .query_one(
            "SELECT atomic_complete_excision_generation($1,$2,$3)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &manifest.as_deref(),
            ],
        )
        .map_err(|error| operation_error("excision/resume-completion", error))?;
    if let Some(manifest) = manifest.as_deref() {
        let manifest_hash = digest(manifest.to_vec(), "activation manifest")?;
        let mut tree_store = PostgresTreeStore::connect_configured(connection)?;
        drain_excision_tree_publication(client, &mut tree_store, manifest_hash)?;
    }
    cleanup_completed_generation_build(client, database_id, generation)?;
    Ok(Some(ExcisionReceipt {
        database_id: database_id.to_owned(),
        source_generation,
        generation,
        basis_t,
        request_count,
        removed_datoms,
        old_head_hash,
        new_head_hash,
        resumed: true,
    }))
}

fn drain_excision_tree_publication(
    client: &mut Client,
    tree_store: &mut PostgresTreeStore,
    manifest_hash: Digest,
) -> Result<(), SemanticError> {
    loop {
        // The boolean result names whether this manifest is the complete live
        // root. A later same-generation root can legitimately supersede it,
        // so header disappearance—not a permanently true return value—is the
        // terminal condition for its bounded work ledger.
        let _ = tree_store.advance_publication_work(manifest_hash)?;
        let pending: bool = client
            .query_one(
                "SELECT EXISTS (SELECT 1 FROM atomic_tree_delta_headers \
                                WHERE manifest_hash=$1 AND delta_state=2)",
                &[&&manifest_hash[..]],
            )
            .map_err(|error| operation_error("excision/tree-publication-work", error))?
            .get(0);
        if !pending {
            return Ok(());
        }
    }
}

fn sync_excise_in(
    client: &mut Client,
    database_id: &str,
    through_t: u64,
) -> Result<bool, SemanticError> {
    let head = client
        .query_opt(
            "SELECT log_generation,basis_t,tx_hash FROM atomic_heads WHERE database_id=$1",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/sync-head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                crate::ErrorCategory::NotFound,
                "excision/database-not-found",
                format!("database {database_id} has no published head"),
            )
        })?;
    let generation = positive_or_zero(head.get(0), "generation")?;
    let basis = positive_or_zero(head.get(1), "basis")?;
    let hash = digest(head.get(2), "head hash")?;
    if generation > 0
        && !client
            .query_one(
                "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_completions \
                                WHERE database_id=$1 AND generation=$2)",
                &[&database_id, &sql_u64(generation, "generation")?],
            )
            .map_err(|error| operation_error("excision/sync-completion", error))?
            .get::<_, bool>(0)
    {
        // Activation makes the rewritten log authoritative first. The
        // separate completion marker is the linearization point promised by
        // sync-excise, so an interrupted post-activation worker is simply not
        // caught up yet rather than an exceptional source state.
        return Ok(false);
    }
    let database = recover_generation_to(client, database_id, generation, basis, hash)?.database;
    let requested = database
        .historical_excision_requests()?
        .into_iter()
        .filter(|request| request.request_t <= through_t)
        .map(|request| (request.request_t, request.request_entity))
        .collect::<BTreeSet<_>>();
    if requested.is_empty() {
        return Ok(true);
    }
    if generation == 0 {
        return Ok(false);
    }
    let completed = completed_excision_identities(client, database_id, generation)?;
    Ok(requested.is_subset(&completed))
}

#[derive(Default)]
struct GlobalDerivedReachability {
    legacy_segments: BTreeSet<Digest>,
    tree_nodes: BTreeSet<Digest>,
}

fn inspect_semantic_commitments<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    metrics: &mut OperationalMetrics,
) -> Result<(), SemanticError> {
    // Content nodes are structurally shared across every immutable database
    // value. UNION (rather than UNION ALL) makes this a set walk and remains
    // finite even if owner-level corruption introduced a cycle.
    let row = client
        .query_one(
            "WITH RECURSIVE database_reachable(node_hash) AS ( \
                 SELECT current_root FROM atomic_semantic_commitment_roots \
                  WHERE database_id = $1 AND current_root IS NOT NULL \
                 UNION \
                 SELECT child.node_hash \
                   FROM database_reachable reachable \
                   JOIN atomic_semantic_commitment_nodes parent \
                     ON parent.node_hash = reachable.node_hash \
                   CROSS JOIN LATERAL ( \
                       VALUES (parent.left_hash), (parent.right_hash) \
                   ) AS child(node_hash) \
                  WHERE child.node_hash IS NOT NULL \
             ), global_reachable(node_hash) AS ( \
                 SELECT current_root FROM atomic_semantic_commitment_roots \
                  WHERE current_root IS NOT NULL \
                 UNION \
                 SELECT child.node_hash \
                   FROM global_reachable reachable \
                   JOIN atomic_semantic_commitment_nodes parent \
                     ON parent.node_hash = reachable.node_hash \
                   CROSS JOIN LATERAL ( \
                       VALUES (parent.left_hash), (parent.right_hash) \
                   ) AS child(node_hash) \
                  WHERE child.node_hash IS NOT NULL \
             ) \
             SELECT (SELECT count(*) FROM atomic_semantic_commitment_roots \
                      WHERE database_id = $1), \
                    (SELECT count(*) FROM database_reachable), \
                    (SELECT COALESCE(sum(octet_length(node.payload)), 0) \
                       FROM database_reachable reachable \
                       JOIN atomic_semantic_commitment_nodes node \
                         ON node.node_hash = reachable.node_hash), \
                    (SELECT count(*) FROM atomic_semantic_commitment_nodes node \
                      WHERE NOT EXISTS (SELECT 1 FROM global_reachable reachable \
                                         WHERE reachable.node_hash = node.node_hash))",
            &[&database_id],
        )
        .map_err(|error| operation_error("operations/semantic-commitment-metrics", error))?;
    metrics.semantic_commitment_roots =
        positive_or_zero(row.get(0), "semantic commitment root count")?;
    metrics.semantic_commitment_nodes =
        positive_or_zero(row.get(1), "semantic commitment node count")?;
    metrics.semantic_commitment_node_bytes =
        positive_or_zero(row.get(2), "semantic commitment node bytes")?;
    metrics.orphan_semantic_commitment_nodes =
        positive_or_zero(row.get(3), "orphan semantic commitment node count")?;
    Ok(())
}

fn inspect_native_trees<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    deep: bool,
    metrics: &mut OperationalMetrics,
    problems: &mut Vec<IntegrityProblem>,
) -> Result<(), SemanticError> {
    metrics.tree_manifests = count(
        client,
        "SELECT count(*) FROM atomic_tree_manifests WHERE database_id = $1",
        database_id,
    )?;
    let generation = client
        .query_opt(
            "SELECT excision_generation FROM atomic_database_generations WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| operation_error("operations/tree-generation", error))?
        .map(|row| positive_or_zero(row.get(0), "tree generation"))
        .transpose()?
        .unwrap_or(0);
    let publications = client
        .query(
            "SELECT publication_revision, basis_t, tx_hash, manifest_hash \
             FROM atomic_tree_publications WHERE database_id = $1 \
             ORDER BY publication_revision",
            &[&database_id],
        )
        .map_err(|error| operation_error("operations/tree-publications", error))?;
    metrics.tree_publications = publications.len() as u64;
    let mut previous_revision = None;
    let mut all_nodes = BTreeMap::<Digest, Vec<u8>>::new();
    let mut newest_manifest_hash = None;
    let mut newest_authenticated_nodes = None;

    for publication in publications {
        let revision = positive_or_zero(publication.get(0), "tree publication revision")?;
        let basis = positive_or_zero(publication.get(1), "tree publication basis")?;
        let published_tx = digest(publication.get(2), "tree publication transaction hash")?;
        let manifest_hash = digest(publication.get(3), "tree publication manifest hash")?;
        newest_manifest_hash = Some(manifest_hash);
        newest_authenticated_nodes = None;
        metrics.tree_publication_revision = metrics.tree_publication_revision.max(revision);
        if let Some(previous) = previous_revision
            && revision != previous + 1
        {
            problem(
                problems,
                "integrity/tree-publication-gap",
                format!("expected tree revision {}, found {revision}", previous + 1),
            );
        }
        // GC may retire a contiguous prefix, so the first retained revision is
        // not required to be one. Any gap inside the retained suffix remains
        // corruption.
        previous_revision = Some(revision);

        let Some(row) = client
            .query_opt(
                "SELECT database_id, publication_revision, basis_t, tx_hash, state_hash, \
                        excision_generation, eidx_frontier, manifest_version, payload \
                 FROM atomic_tree_manifests WHERE manifest_hash = $1",
                &[&&manifest_hash[..]],
            )
            .map_err(|error| operation_error("operations/tree-manifest", error))?
        else {
            problem(
                problems,
                "integrity/missing-tree-manifest",
                format!("tree revision {revision} names an absent manifest"),
            );
            continue;
        };
        let stored_database: String = row.get(0);
        let stored_revision = positive_or_zero(row.get(1), "tree manifest revision")?;
        let stored_basis = positive_or_zero(row.get(2), "tree manifest basis")?;
        let stored_tx = digest(row.get(3), "tree manifest transaction hash")?;
        let stored_state = digest(row.get(4), "tree manifest state hash")?;
        let stored_generation = positive_or_zero(row.get(5), "tree manifest generation")?;
        let stored_frontier = positive_or_zero(row.get(6), "tree manifest frontier")?;
        let stored_version: i16 = row.get(7);
        let payload: Vec<u8> = row.get(8);
        let mut publication_valid = stored_database == database_id
            && stored_revision == revision
            && stored_basis == basis
            && stored_tx == published_tx
            && PersistentTreeManifest::encoded_version(&payload)
                .is_ok_and(|version| stored_version == version)
            && sha256(&payload) == manifest_hash;
        if !publication_valid {
            problem(
                problems,
                "integrity/tree-manifest-row-mismatch",
                format!("tree revision {revision} has inconsistent manifest metadata"),
            );
        }
        let authoritative = client
            .query_opt(
                "SELECT tx_hash, state_hash FROM ( \
                     SELECT tx_hash, state_hash FROM atomic_transactions \
                      WHERE $3::bigint=0 AND database_id=$1 AND basis_t=$2 \
                     UNION ALL \
                     SELECT tx_hash, state_hash FROM atomic_generation_transactions \
                      WHERE $3::bigint>0 AND database_id=$1 AND generation=$3 \
                        AND basis_t=$2 \
                     UNION ALL \
                     SELECT tx_hash, state_hash FROM atomic_semantic_commitment_roots \
                      WHERE $3::bigint>0 AND $2::bigint=0 AND database_id=$1 \
                        AND generation=$3 AND basis_t=0 AND commitment_version=2 \
                 ) authoritative",
                &[
                    &database_id,
                    &sql_u64(basis, "tree publication basis")?,
                    &sql_u64(stored_generation, "tree publication generation")?,
                ],
            )
            .map_err(|error| operation_error("operations/tree-authority", error))?;
        let authoritative_matches = authoritative
            .map(|row| {
                Ok::<_, SemanticError>(
                    digest(row.get(0), "authoritative tree transaction hash")? == stored_tx
                        && digest(row.get(1), "authoritative tree state hash")? == stored_state,
                )
            })
            .transpose()?
            .unwrap_or(false);
        if !authoritative_matches {
            publication_valid = false;
            problem(
                problems,
                "integrity/tree-authority-mismatch",
                format!("tree revision {revision} does not identify its authoritative value"),
            );
        }
        let manifest = match PersistentTreeManifest::decode(&payload) {
            Ok(manifest) => manifest,
            Err(error) => {
                problem(
                    problems,
                    error.code,
                    format!("tree revision {revision}: {}", error.message),
                );
                continue;
            }
        };
        if manifest.database_id != database_id
            || manifest.publication_revision != revision
            || manifest.basis_t != basis
            || manifest.tx_hash != stored_tx
            || manifest.state_hash != stored_state
            || manifest.excision_generation != stored_generation
            || manifest.eidx_frontier != stored_frontier
        {
            publication_valid = false;
            problem(
                problems,
                "integrity/tree-manifest-envelope-mismatch",
                format!("tree revision {revision} payload disagrees with relational metadata"),
            );
        }
        if !native_manifest_roots_match(client, manifest_hash, &manifest)? {
            publication_valid = false;
            problem(
                problems,
                "integrity/tree-root-bindings-mismatch",
                format!("tree revision {revision} root bindings disagree with its manifest"),
            );
        }
        let mut manifest_nodes = BTreeMap::new();
        for tree in &manifest.trees {
            match add_reachable_tree_nodes(client, tree.descriptor.root_hash, &mut manifest_nodes) {
                Ok(()) => {
                    if deep
                        && let Err(error) = validate_tree(
                            &tree.descriptor,
                            &TreeNodeSet::from_nodes(manifest_nodes.clone()),
                        )
                    {
                        publication_valid = false;
                        problem(
                            problems,
                            error.code,
                            format!("tree revision {revision}: {}", error.message),
                        );
                    }
                }
                Err(error) => {
                    publication_valid = false;
                    problem(
                        problems,
                        error.code,
                        format!("tree revision {revision}: {}", error.message),
                    );
                }
            }
        }
        if publication_valid {
            newest_authenticated_nodes =
                Some(manifest_nodes.keys().copied().collect::<BTreeSet<_>>());
        }
        // A prior excision generation remains retained physical history, but
        // it is no longer a usable index for the current database value.
        if publication_valid && stored_generation == generation {
            metrics.index_basis_t = metrics.index_basis_t.max(basis);
            metrics.pending_avet_projections = manifest.pending_avet.len() as u64;
            if deep {
                inspect_native_semantic_projection(
                    client,
                    database_id,
                    revision,
                    stored_generation,
                    basis,
                    stored_tx,
                    &manifest.pending_avet,
                    &manifest_nodes,
                    problems,
                );
            }
        }
        all_nodes.extend(manifest_nodes);
    }
    if !native_live_membership_matches(
        client,
        database_id,
        newest_manifest_hash,
        newest_authenticated_nodes.as_ref(),
    )? {
        problem(
            problems,
            "integrity/tree-live-membership-mismatch",
            "current native live membership is absent, incomplete, or disagrees with authenticated reachability",
        );
    }
    metrics.tree_nodes = all_nodes.len() as u64;
    metrics.tree_node_bytes = all_nodes.values().map(|bytes| bytes.len() as u64).sum();
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn inspect_native_semantic_projection<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    revision: u64,
    generation: u64,
    basis_t: u64,
    tx_hash: Digest,
    pending_avet: &[crate::AvetProjectionWork],
    nodes: &BTreeMap<Digest, Vec<u8>>,
    problems: &mut Vec<IntegrityProblem>,
) {
    // A peer may fall back to any retained, structurally valid publication in
    // the active generation when a newer one is corrupt. Therefore deep
    // inspection must compare every such candidate with its named log value,
    // not just the newest root. Project one order at a time to avoid retaining
    // eight duplicate datom vectors during this already-broad operation.
    let recovered = match recover_generation_to(client, database_id, generation, basis_t, tx_hash) {
        Ok(recovered) => recovered,
        Err(error) => {
            problem(
                problems,
                error.code,
                format!(
                    "tree revision {revision} authoritative replay: {}",
                    error.message
                ),
            );
            return;
        }
    };
    if let Err(error) =
        crate::peer::validate_avet_work_directions(pending_avet, recovered.database.schema())
    {
        problem(
            problems,
            error.code,
            format!(
                "tree revision {revision} pending AVET work: {}",
                error.message
            ),
        );
    }

    let expected_current = recovered
        .database
        .datoms(crate::View::Current, IndexOrder::Eavt);
    let replayed_history = recovered
        .database
        .datoms(crate::View::History, IndexOrder::Eavt);
    let physical_history = match native_tree_datoms(nodes, IndexOrder::Eavt, true) {
        Ok(datoms) => datoms,
        Err(error) => {
            problem(
                problems,
                error.code,
                format!("tree revision {revision} history EAVT: {}", error.message),
            );
            return;
        }
    };
    if let Err(error) = validate_physical_history_projection(
        &replayed_history,
        &physical_history,
        recovered.database.basis_t(),
    ) {
        problem(
            problems,
            error.code,
            format!("tree revision {revision} history EAVT: {}", error.message),
        );
    }

    for work in pending_avet.iter().filter(|work| !work.clearing) {
        let source = native_tree_datoms(nodes, IndexOrder::Aevt, work.history);
        let target = native_tree_datoms(nodes, IndexOrder::Avet, work.history);
        match (source, target) {
            (Ok(source), Ok(target)) => {
                let mut source = source
                    .into_iter()
                    .filter(|datom| datom.attribute == work.attribute)
                    .collect::<Vec<_>>();
                source.sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
                let target = target
                    .into_iter()
                    .filter(|datom| datom.attribute == work.attribute)
                    .collect::<Vec<_>>();
                let offset = usize::try_from(work.offset).ok();
                let exact_prefix = offset
                    .and_then(|offset| source.get(..offset))
                    .is_some_and(|prefix| same_stored_datoms(&target, prefix));
                if !exact_prefix {
                    problem(
                        problems,
                        "integrity/tree-pending-avet-prefix-mismatch",
                        format!(
                            "tree revision {revision} pending AVET attribute {} is not the exact AEVT prefix at offset {}",
                            work.attribute, work.offset
                        ),
                    );
                }
            }
            (Err(error), _) | (_, Err(error)) => problem(
                problems,
                error.code,
                format!(
                    "tree revision {revision} pending AVET attribute {}: {}",
                    work.attribute, error.message
                ),
            ),
        }
    }

    for history in [false, true] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            if history && order == IndexOrder::Eavt {
                continue;
            }
            let source = if history {
                physical_history.as_slice()
            } else {
                expected_current.as_slice()
            };
            let mut expected = match derive_index_projection(&recovered.database, source, order) {
                Ok(expected) => expected,
                Err(error) => {
                    problem(
                        problems,
                        error.code,
                        format!(
                            "tree revision {revision} {order:?} history={history} projection: {}",
                            error.message
                        ),
                    );
                    continue;
                }
            };
            let mut observed = match native_tree_datoms(nodes, order, history) {
                Ok(observed) => observed,
                Err(error) => {
                    problem(
                        problems,
                        error.code,
                        format!(
                            "tree revision {revision} {order:?} history={history}: {}",
                            error.message
                        ),
                    );
                    continue;
                }
            };
            if order == IndexOrder::Avet {
                observed.retain(|datom| {
                    !avet_projection_is_pending(pending_avet, datom.attribute, history)
                });
                expected.retain(|datom| {
                    !avet_projection_is_pending(pending_avet, datom.attribute, history)
                });
            }
            if !same_stored_datoms(&observed, &expected) {
                problem(
                    problems,
                    "integrity/tree-derived-index-mismatch",
                    format!(
                        "tree revision {revision} {order:?} history={history} disagrees with authoritative generation {generation} at basis {basis_t} ({} vs {} datoms; {} pending AVET attribute(s) excluded)",
                        observed.len(),
                        expected.len(),
                        pending_avet
                            .iter()
                            .filter(|work| !work.history || history)
                            .count(),
                    ),
                );
            }
        }
    }
}

fn avet_projection_is_pending(
    pending_avet: &[crate::AvetProjectionWork],
    attribute: u32,
    history: bool,
) -> bool {
    pending_avet
        .iter()
        .any(|work| work.attribute == attribute && (!work.history || history))
}

fn native_tree_datoms(
    nodes: &BTreeMap<Digest, Vec<u8>>,
    order: IndexOrder,
    history: bool,
) -> Result<Vec<Datom>, SemanticError> {
    let mut datoms = Vec::new();
    for (hash, payload) in nodes {
        let TreeNode::Leaf(leaf) = decode_tree_node(hash, payload)? else {
            continue;
        };
        if leaf.order != order || leaf.history != history {
            continue;
        }
        for index in 0..leaf.len() {
            datoms.push(
                leaf.datom(index)
                    .expect("authenticated leaf columns have equal lengths"),
            );
        }
    }
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    Ok(datoms)
}

fn derive_index_projection(
    database: &crate::Database,
    source: &[Datom],
    order: IndexOrder,
) -> Result<Vec<Datom>, SemanticError> {
    let mut datoms = source
        .iter()
        .filter_map(|datom| {
            let included = match order {
                IndexOrder::Eavt | IndexOrder::Aevt => Ok(true),
                IndexOrder::Avet => database
                    .schema()
                    .attribute(datom.attribute)
                    .map(|attribute| attribute.indexed || attribute.unique.is_some()),
                IndexOrder::Vaet => database
                    .schema()
                    .attribute(datom.attribute)
                    .map(|attribute| attribute.value_type == crate::ValueType::Ref),
            };
            match included {
                Ok(true) => Some(Ok(datom.clone())),
                Ok(false) => None,
                Err(error) => Some(Err(error)),
            }
        })
        .collect::<Result<Vec<_>, _>>()?;
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    Ok(datoms)
}

fn same_stored_datoms(left: &[Datom], right: &[Datom]) -> bool {
    left.len() == right.len()
        && left
            .iter()
            .zip(right)
            .all(|(left, right)| same_stored_datom(left, right))
}

fn same_stored_datom(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
        && left.tx == right.tx
        && left.added == right.added
}

/// Prove that physical EAVT history is an authenticated-log projection, not
/// merely a self-consistent alternative tree. Within one logical E/A/V group,
/// each contiguous omitted run must reduce to empty by repeatedly deleting a
/// retraction followed by an assertion. This admits the nested omissions that
/// successive consolidation jobs can expose (`R R A A`), while rejecting a
/// fabricated fact, a one-sided omission, or an omission across a retained
/// fact. Every matched retraction must also have a noHistory=true opportunity
/// at a transaction boundary on or after it. This proves permission, not the
/// exact scheduler instant: Datomic documents no precise time at which an
/// eligible pair is physically forgotten.
fn validate_physical_history_projection(
    replayed: &[Datom],
    physical: &[Datom],
    through_t: u64,
) -> Result<(), SemanticError> {
    let opportunities = no_history_opportunities(replayed, through_t)?;
    let mut replay_offset = 0_usize;
    let mut physical_offset = 0_usize;

    while replay_offset < replayed.len() {
        let group_start = replay_offset;
        replay_offset += 1;
        while replay_offset < replayed.len()
            && same_logical_eav(&replayed[group_start], &replayed[replay_offset])
        {
            replay_offset += 1;
        }
        let group = &replayed[group_start..replay_offset];

        if let Some(actual) = physical.get(physical_offset)
            && logical_eav_cmp(actual, &group[0]).is_lt()
        {
            return Err(integrity_fault(
                "physical EAVT history contains a fact absent from authoritative replay",
            ));
        }

        let mut expected_in_group = 0_usize;
        let mut missing_start = 0_usize;
        while let Some(actual) = physical.get(physical_offset) {
            match logical_eav_cmp(actual, &group[0]) {
                std::cmp::Ordering::Less => {
                    return Err(integrity_fault(
                        "physical EAVT history contains a fact absent from authoritative replay",
                    ));
                }
                std::cmp::Ordering::Greater => break,
                std::cmp::Ordering::Equal => {}
            }

            while expected_in_group < group.len()
                && !same_stored_datom(&group[expected_in_group], actual)
            {
                expected_in_group += 1;
            }
            if expected_in_group == group.len() {
                return Err(integrity_fault(
                    "physical EAVT history contains a fact absent from authoritative replay",
                ));
            }
            validate_history_omission(
                &group[missing_start..expected_in_group],
                &opportunities,
                through_t,
            )?;
            expected_in_group += 1;
            missing_start = expected_in_group;
            physical_offset += 1;
        }
        validate_history_omission(&group[missing_start..], &opportunities, through_t)?;
    }

    if physical_offset != physical.len() {
        return Err(integrity_fault(
            "physical EAVT history has trailing facts absent from authoritative replay",
        ));
    }
    Ok(())
}

fn same_logical_eav(left: &Datom, right: &Datom) -> bool {
    logical_eav_cmp(left, right).is_eq()
}

fn logical_eav_cmp(left: &Datom, right: &Datom) -> std::cmp::Ordering {
    left.entity
        .cmp(&right.entity)
        .then(left.attribute.cmp(&right.attribute))
        .then_with(|| left.value.index_cmp(&right.value))
}

#[derive(Clone, Copy, Debug, Default)]
struct NoHistoryTransactionFold {
    asserted: Option<bool>,
    saw_retraction: bool,
}

/// Precompute inclusive transaction intervals in which each attribute had
/// `:db/noHistory` enabled. Schema facts in one transaction are folded as one
/// declarative change: an asserted boolean is the resulting value, while a
/// retraction-only transaction falls back to false. In particular, retracting
/// `false` never means `true`.
fn no_history_opportunities(
    replayed: &[Datom],
    through_t: u64,
) -> Result<BTreeMap<u32, Vec<(u64, u64)>>, SemanticError> {
    let mut transaction_folds = BTreeMap::<(u32, u64), NoHistoryTransactionFold>::new();
    for datom in replayed {
        if datom.attribute != crate::DB_NO_HISTORY as u32 {
            continue;
        }
        let attribute = crate::schema_eid_to_attr_id(datom.entity).map_err(|_| {
            integrity_fault("authoritative noHistory schema fact has an invalid schema entity")
        })?;
        let value = match datom.value {
            crate::Value::Bool(value) => value,
            _ => {
                return Err(integrity_fault(
                    "authoritative noHistory schema history has a non-boolean value",
                ));
            }
        };
        let at = crate::tx_to_t(datom.tx)?;
        let fold = transaction_folds.entry((attribute, at)).or_default();
        if datom.added {
            if fold.asserted.is_some_and(|asserted| asserted != value) {
                return Err(integrity_fault(
                    "authoritative noHistory schema transaction asserts conflicting values",
                ));
            }
            fold.asserted = Some(value);
        } else {
            fold.saw_retraction = true;
        }
    }

    let mut changes = BTreeMap::<u32, Vec<(u64, bool)>>::new();
    for ((attribute, at), fold) in transaction_folds {
        let enabled = fold.asserted.unwrap_or(false);
        debug_assert!(fold.asserted.is_some() || fold.saw_retraction);
        changes.entry(attribute).or_default().push((at, enabled));
    }

    let mut intervals = BTreeMap::<u32, Vec<(u64, u64)>>::new();
    for (attribute, changes) in changes {
        let mut enabled_from = None::<u64>;
        let mut attribute_intervals = Vec::new();
        for (at, enabled) in changes {
            if at > through_t {
                break;
            }
            if !enabled {
                if let Some(start) = enabled_from.take() {
                    attribute_intervals.push((start, at.saturating_sub(1)));
                }
            } else if enabled_from.is_none() {
                enabled_from = Some(at);
            }
        }
        if let Some(start) = enabled_from {
            attribute_intervals.push((start, through_t));
        }
        if !attribute_intervals.is_empty() {
            intervals.insert(attribute, attribute_intervals);
        }
    }
    Ok(intervals)
}

fn validate_history_omission(
    omitted: &[Datom],
    opportunities: &BTreeMap<u32, Vec<(u64, u64)>>,
    through_t: u64,
) -> Result<(), SemanticError> {
    let mut retractions = Vec::<&Datom>::new();
    for datom in omitted {
        if !datom.added {
            retractions.push(datom);
            continue;
        }
        let Some(retraction) = retractions.pop() else {
            return Err(integrity_fault(
                "physical EAVT history omission is not reducible noHistory history",
            ));
        };
        let retraction_t = crate::tx_to_t(retraction.tx)?;
        let permitted = opportunities
            .get(&retraction.attribute)
            .is_some_and(|intervals| {
                let candidate = intervals.partition_point(|(_, end)| *end < retraction_t);
                intervals
                    .get(candidate)
                    .is_some_and(|(start, _)| *start <= through_t)
            });
        if !permitted {
            return Err(integrity_fault(
                "physical EAVT history omission had no noHistory=true opportunity",
            ));
        }
    }
    if !retractions.is_empty() {
        return Err(integrity_fault(
            "physical EAVT history omits an unpaired authoritative retraction",
        ));
    }
    Ok(())
}

fn integrity_fault(message: impl Into<String>) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Fault,
        "integrity/tree-history-projection-mismatch",
        message,
    )
}

fn native_live_membership_matches<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    newest_manifest_hash: Option<Digest>,
    expected: Option<&BTreeSet<Digest>>,
) -> Result<bool, SemanticError> {
    let Some(status) = client
        .query_opt(
            "SELECT manifest_hash, complete, problem_code \
               FROM atomic_tree_live_sets WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| operation_error("operations/tree-live-status", error))?
    else {
        let stored_nodes: i64 = client
            .query_one(
                "SELECT count(*) FROM atomic_tree_live_nodes WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/tree-live-nodes", error))?
            .get(0);
        return Ok(newest_manifest_hash.is_none() && stored_nodes == 0);
    };
    let manifest_hash = digest(status.get(0), "tree live manifest hash")?;
    let complete: bool = status.get(1);
    let problem_code: Option<String> = status.get(2);
    let stored = client
        .query(
            "SELECT node_hash FROM atomic_tree_live_nodes \
             WHERE database_id = $1 ORDER BY node_hash",
            &[&database_id],
        )
        .map_err(|error| operation_error("operations/tree-live-nodes", error))?
        .into_iter()
        .map(|row| digest(row.get(0), "tree live node hash"))
        .collect::<Result<BTreeSet<_>, _>>()?;
    Ok(complete
        && problem_code.is_none()
        && newest_manifest_hash == Some(manifest_hash)
        && expected.is_some_and(|expected| &stored == expected))
}

fn native_manifest_roots_match<C: postgres::GenericClient>(
    client: &mut C,
    manifest_hash: Digest,
    manifest: &PersistentTreeManifest,
) -> Result<bool, SemanticError> {
    let rows = client
        .query(
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
             FROM atomic_tree_manifest_roots WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| operation_error("operations/tree-roots", error))?;
    if rows.len() != manifest.trees.len() {
        return Ok(false);
    }
    let mut roots = BTreeMap::new();
    for row in rows {
        let order: i16 = row.get(0);
        let history: bool = row.get(1);
        if roots
            .insert(
                (order, history),
                (
                    digest(row.get(2), "tree root hash")?,
                    positive_or_zero(row.get(3), "tree root datom count")?,
                    positive_or_zero(row.get(4), "tree root bytes")?,
                ),
            )
            .is_some()
        {
            return Ok(false);
        }
    }
    Ok(manifest.trees.iter().all(|tree| {
        roots.get(&(
            index_order_tag(tree.descriptor.order),
            tree.descriptor.history,
        )) == Some(&(
            tree.descriptor.root_hash,
            tree.descriptor.count,
            tree.root_bytes,
        ))
    }))
}

fn add_reachable_tree_nodes<C: postgres::GenericClient>(
    client: &mut C,
    root: Digest,
    output: &mut BTreeMap<Digest, Vec<u8>>,
) -> Result<(), SemanticError> {
    let mut pending = vec![root];
    while let Some(hash) = pending.pop() {
        if output.contains_key(&hash) {
            continue;
        }
        let row = client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| operation_error("operations/tree-node", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "integrity/missing-tree-node",
                    format!("published tree references absent node {}", hex(&hash)),
                )
            })?;
        let payload: Vec<u8> = row.get(0);
        let node = decode_tree_node(&hash, &payload)?;
        match &node {
            TreeNode::Root(root) => {
                pending.extend(root.directories.iter().map(|child| child.hash));
            }
            TreeNode::Directory(directory) => {
                pending.extend(directory.leaves.iter().map(|child| child.hash));
            }
            TreeNode::Leaf(_) => {}
        }
        output.insert(hash, payload);
    }
    Ok(())
}

fn global_derived_reachability<C: postgres::GenericClient>(
    client: &mut C,
    excluded_tree_manifests: &BTreeSet<Digest>,
) -> Result<Option<GlobalDerivedReachability>, SemanticError> {
    let mut reachable = GlobalDerivedReachability::default();
    for row in client
        .query(
            "SELECT p.database_id, p.basis_t, p.tx_hash, p.manifest_hash, \
                    m.database_id, m.basis_t, m.tx_hash, m.manifest_hash, m.payload, \
                    t.database_id \
             FROM atomic_index_publications p LEFT JOIN atomic_index_manifests m \
               ON p.database_id = m.database_id AND p.basis_t = m.basis_t \
              AND p.tx_hash = m.tx_hash AND p.manifest_hash = m.manifest_hash \
             LEFT JOIN atomic_transactions t \
               ON t.database_id = p.database_id AND t.basis_t = p.basis_t \
              AND t.tx_hash = p.tx_hash",
            &[],
        )
        .map_err(|error| operation_error("operations/reachability-manifests", error))?
    {
        let published_database: String = row.get(0);
        let published_basis = positive_or_zero(row.get(1), "published manifest basis")?;
        let published_tx = digest(row.get(2), "published manifest transaction hash")?;
        let published_hash = digest(row.get(3), "published manifest hash")?;
        let stored_database: Option<String> = row.get(4);
        let stored_basis: Option<i64> = row.get(5);
        let stored_tx: Option<Vec<u8>> = row.get(6);
        let stored_hash: Option<Vec<u8>> = row.get(7);
        let payload: Option<Vec<u8>> = row.get(8);
        let authoritative_database: Option<String> = row.get(9);
        let (
            Some(stored_database),
            Some(stored_basis),
            Some(stored_tx),
            Some(stored_hash),
            Some(payload),
            Some(authoritative_database),
        ) = (
            stored_database,
            stored_basis,
            stored_tx,
            stored_hash,
            payload,
            authoritative_database,
        )
        else {
            return Ok(None);
        };
        let stored_basis = positive_or_zero(stored_basis, "stored manifest basis")?;
        let stored_tx = digest(stored_tx, "stored manifest transaction hash")?;
        let stored_hash = digest(stored_hash, "stored manifest hash")?;
        if authoritative_database != published_database
            || stored_database != published_database
            || stored_basis != published_basis
            || stored_tx != published_tx
            || stored_hash != published_hash
            || sha256(&payload) != published_hash
        {
            return Ok(None);
        }
        let Ok(manifest) = decode_index_manifest(&payload) else {
            return Ok(None);
        };
        if manifest.database_id != published_database
            || manifest.basis_t != published_basis
            || manifest.tx_hash != published_tx
        {
            return Ok(None);
        }
        for segment in manifest.segments {
            let Some(segment_row) = client
                .query_opt(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&segment.hash[..]],
                )
                .map_err(|error| operation_error("operations/reachability-segment", error))?
            else {
                return Ok(None);
            };
            let segment_payload: Vec<u8> = segment_row.get(0);
            if sha256(&segment_payload) != segment.hash {
                return Ok(None);
            }
            let Ok(decoded) = decode_index_segment(&segment_payload) else {
                return Ok(None);
            };
            if decoded.order != segment.order
                || decoded.history != segment.history
                || decoded.datoms.len() != segment.count as usize
            {
                return Ok(None);
            }
            reachable.legacy_segments.insert(segment.hash);
        }
    }
    let mut native_nodes = BTreeMap::new();
    let mut native_trees = Vec::new();
    for row in client
        .query(
            "SELECT p.database_id, p.publication_revision, p.basis_t, p.tx_hash, \
                    p.manifest_hash, m.database_id, m.publication_revision, m.basis_t, \
                    m.tx_hash, m.state_hash, m.excision_generation, m.eidx_frontier, \
                    m.manifest_version, m.manifest_hash, m.payload, \
                    COALESCE(t0.state_hash, tg.state_hash) \
             FROM atomic_tree_publications p LEFT JOIN atomic_tree_manifests m \
               ON m.database_id = p.database_id \
              AND m.publication_revision = p.publication_revision \
              AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
              AND m.manifest_hash = p.manifest_hash \
             LEFT JOIN atomic_transactions t0 \
               ON p.log_generation=0 AND t0.database_id=p.database_id \
              AND t0.basis_t=p.basis_t AND t0.tx_hash=p.tx_hash \
             LEFT JOIN atomic_generation_transactions tg \
               ON p.log_generation>0 AND tg.database_id=p.database_id \
              AND tg.generation=p.log_generation AND tg.basis_t=p.basis_t \
              AND tg.tx_hash=p.tx_hash",
            &[],
        )
        .map_err(|error| operation_error("operations/reachability-tree-manifests", error))?
    {
        let published_database: String = row.get(0);
        let published_revision = positive_or_zero(row.get(1), "published tree revision")?;
        let published_basis = positive_or_zero(row.get(2), "published tree basis")?;
        let published_tx = digest(row.get(3), "published tree transaction hash")?;
        let published_hash = digest(row.get(4), "published tree manifest hash")?;
        // Candidate selection first validates the complete published graph,
        // then calls this scanner again with only advisory-locked obsolete
        // publications excluded. The remaining walk is the exact protection
        // set for shared immutable nodes.
        if excluded_tree_manifests.contains(&published_hash) {
            continue;
        }
        let stored_database: Option<String> = row.get(5);
        let stored_revision: Option<i64> = row.get(6);
        let stored_basis: Option<i64> = row.get(7);
        let stored_tx: Option<Vec<u8>> = row.get(8);
        let stored_state: Option<Vec<u8>> = row.get(9);
        let stored_generation: Option<i64> = row.get(10);
        let stored_frontier: Option<i64> = row.get(11);
        let stored_version: Option<i16> = row.get(12);
        let stored_hash: Option<Vec<u8>> = row.get(13);
        let payload: Option<Vec<u8>> = row.get(14);
        let authoritative_state: Option<Vec<u8>> = row.get(15);
        let (
            Some(stored_database),
            Some(stored_revision),
            Some(stored_basis),
            Some(stored_tx),
            Some(stored_state),
            Some(stored_generation),
            Some(stored_frontier),
            Some(stored_version),
            Some(stored_hash),
            Some(payload),
            Some(authoritative_state),
        ) = (
            stored_database,
            stored_revision,
            stored_basis,
            stored_tx,
            stored_state,
            stored_generation,
            stored_frontier,
            stored_version,
            stored_hash,
            payload,
            authoritative_state,
        )
        else {
            return Ok(None);
        };
        let stored_revision = positive_or_zero(stored_revision, "stored tree revision")?;
        let stored_basis = positive_or_zero(stored_basis, "stored tree basis")?;
        let stored_tx = digest(stored_tx, "stored tree transaction hash")?;
        let stored_state = digest(stored_state, "stored tree state hash")?;
        let stored_generation = positive_or_zero(stored_generation, "stored tree generation")?;
        let stored_frontier = positive_or_zero(stored_frontier, "stored tree frontier")?;
        let stored_hash = digest(stored_hash, "stored tree manifest hash")?;
        let authoritative_state = digest(authoritative_state, "authoritative tree state hash")?;
        let Ok(encoded_version) = PersistentTreeManifest::encoded_version(&payload) else {
            // Global reachability feeds only conservative orphan accounting
            // and reclamation eligibility. Corruption in an unrelated
            // database makes that global answer unknowable; it must not abort
            // this database's scoped inspection. The target database's own
            // publication walk above still reports the precise corruption.
            return Ok(None);
        };
        if stored_database != published_database
            || stored_revision != published_revision
            || stored_basis != published_basis
            || stored_tx != published_tx
            || stored_state != authoritative_state
            || stored_version != encoded_version
            || stored_hash != published_hash
            || sha256(&payload) != published_hash
        {
            return Ok(None);
        }
        let Ok(manifest) = PersistentTreeManifest::decode(&payload) else {
            return Ok(None);
        };
        if manifest.database_id != published_database
            || manifest.publication_revision != published_revision
            || manifest.basis_t != published_basis
            || manifest.tx_hash != published_tx
            || manifest.state_hash != stored_state
            || manifest.excision_generation != stored_generation
            || manifest.eidx_frontier != stored_frontier
        {
            return Ok(None);
        }
        if !native_manifest_roots_match(client, published_hash, &manifest)? {
            return Ok(None);
        }
        for tree in &manifest.trees {
            if add_reachable_tree_nodes(client, tree.descriptor.root_hash, &mut native_nodes)
                .is_err()
            {
                return Ok(None);
            }
            native_trees.push(tree.descriptor.clone());
        }
    }
    let native_node_set = TreeNodeSet::from_nodes(native_nodes);
    if native_trees
        .iter()
        .any(|tree| validate_tree(tree, &native_node_set).is_err())
    {
        return Ok(None);
    }
    reachable
        .tree_nodes
        .extend(native_node_set.iter().map(|(hash, _)| *hash));
    // Both partial content-first plans and completed request-base archives
    // are exact liveness roots. Their own completion/open path authenticates
    // the full graph; orphan metrics must conservatively retain every planned
    // hash even while an interrupted upload is still incomplete.
    for row in client
        .query(
            "SELECT DISTINCT node_hash FROM atomic_request_base_archive_nodes",
            &[],
        )
        .map_err(|error| operation_error("operations/reachability-request-base-archive", error))?
    {
        reachable.tree_nodes.insert(digest(
            row.get(0),
            "request-base archive reachable node hash",
        )?);
    }
    Ok(Some(reachable))
}

fn count_unreachable_hashes<C: postgres::GenericClient>(
    client: &mut C,
    sql: &str,
    reachable: &BTreeSet<Digest>,
    label: &str,
) -> Result<u64, SemanticError> {
    let mut count = 0_u64;
    for row in client
        .query(sql, &[])
        .map_err(|error| operation_error("operations/reachability-count", error))?
    {
        if !reachable.contains(&digest(row.get(0), label)?) {
            count = count.saturating_add(1);
        }
    }
    Ok(count)
}

fn index_order_tag(order: crate::IndexOrder) -> i16 {
    match order {
        crate::IndexOrder::Eavt => 0,
        crate::IndexOrder::Aevt => 1,
        crate::IndexOrder::Avet => 2,
        crate::IndexOrder::Vaet => 3,
    }
}

fn sql_u64(value: u64, label: &str) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::new(
            crate::ErrorCategory::Unsupported,
            "operations/value-out-of-range",
            format!("{label} cannot be represented by PostgreSQL BIGINT"),
        )
    })
}

fn garbage_candidates<C: postgres::GenericClient>(
    client: &mut C,
    older_than_millis: i64,
) -> Result<GarbageCandidates, SemanticError> {
    // Keep one global lock order: semantic admission/collection first, then
    // tree-manifest and generation locks. Writers take the matching shared
    // semantic lock before inserting their first content node.
    let semantic_gc_locked: bool = client
        .query_one(
            "SELECT pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key())",
            &[],
        )
        .map_err(|error| operation_error("operations/gc-semantic-lock", error))?
        .get(0);
    if !semantic_gc_locked {
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "operations/semantic-gc-pinned",
            "semantic commitment collection is already active",
        ));
    }
    // Advance only one globally oldest root in this call. This is a contiguous
    // per-database prefix by construction; a pin on an earlier root prevents
    // a later root of that database from being selected out of order.
    let mut tree_publications = Vec::new();
    let mut selected_retirement_nodes = Vec::new();
    let mut finishing_retirements = Vec::new();
    let mut retirement_offset = 0_i64;
    'retirement_pages: loop {
        let rows = client
            .query(
                "SELECT r.database_id, r.publication_revision, r.manifest_hash, \
                    r.garbage_complete, \
                    (SELECT count(*) FROM atomic_tree_retired_nodes n \
                      WHERE n.database_id = r.database_id \
                        AND n.publication_revision = r.publication_revision) \
              FROM atomic_tree_retirements r \
               JOIN atomic_tree_publications p \
                 ON p.database_id = r.database_id \
                AND p.publication_revision = r.publication_revision \
                AND p.manifest_hash = r.manifest_hash \
              WHERE r.bookkeeping_complete \
                AND (EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress \
                              WHERE progress.database_id = r.database_id \
                                AND progress.publication_revision = r.publication_revision \
                                AND progress.manifest_hash = r.manifest_hash) \
                     OR r.retired_at < clock_timestamp() - \
                                       $1::bigint * interval '1 millisecond') \
                AND EXISTS (SELECT 1 FROM atomic_tree_publications newer \
                            WHERE newer.database_id = r.database_id \
                              AND newer.publication_revision > r.publication_revision) \
                AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications older \
                                WHERE older.database_id = r.database_id \
                                  AND older.publication_revision < r.publication_revision) \
                AND NOT EXISTS ( \
                    SELECT 1 \
                      FROM atomic_generation_request_bases base \
                      JOIN atomic_heads head \
                        ON head.database_id = r.database_id \
                       AND head.log_generation = p.log_generation \
                     WHERE base.base_manifest_hash = r.manifest_hash \
                ) \
                AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents intent \
                                 WHERE intent.manifest_hash = r.manifest_hash) \
              ORDER BY r.retired_at, r.database_id, r.publication_revision \
              LIMIT 64 OFFSET $2",
                &[&older_than_millis, &retirement_offset],
            )
            .map_err(|error| operation_error("operations/gc-tree-publications", error))?;
        if rows.is_empty() {
            break;
        }
        let row_count = rows.len() as i64;
        for row in rows {
            let publication = TreePublicationGarbage {
                database_id: row.get(0),
                publication_revision: positive_or_zero(row.get(1), "tree publication revision")?,
                manifest_hash: digest(row.get(2), "tree publication manifest hash")?,
                garbage_complete: row.get(3),
            };
            // A live immutable PeerState holds the matching session-level
            // shared lock. Probe past pinned roots from other databases while
            // preserving the oldest-publication prefix within each database.
            if !try_lock_tree_manifest_for_gc(client, publication.manifest_hash)? {
                continue;
            }
            // The upload ledger is a liveness owner independent of the
            // publication. Acquire its fence even though the selection query
            // found no row: a builder takes the shared form before inserting
            // an intent, so this closes the read/lock race. It also prevents
            // bounded candidate caps from retiring a publication whose
            // matching intent was not admitted to this GC batch.
            if !try_lock_tree_build_for_gc(client, publication.manifest_hash)? {
                continue;
            }
            let retired_node_count = positive_or_zero(row.get(4), "retired tree node count")?;
            for node in client
                .query(
                    "SELECT node_hash FROM atomic_tree_retired_nodes \
                      WHERE database_id = $1 AND publication_revision = $2 \
                      ORDER BY node_hash LIMIT $3",
                    &[
                        &publication.database_id,
                        &sql_u64(publication.publication_revision, "publication revision")?,
                        &(MAX_TREE_RETIREMENT_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-retirement-node-batch", error))?
            {
                selected_retirement_nodes.push((
                    publication.database_id.clone(),
                    publication.publication_revision,
                    digest(node.get(0), "retired tree node hash")?,
                ));
            }
            if retired_node_count <= MAX_TREE_RETIREMENT_NODES_PER_GC as u64 {
                finishing_retirements.push((
                    publication.database_id.clone(),
                    publication.publication_revision,
                    publication.manifest_hash,
                ));
            }
            tree_publications.push(publication);
            if tree_publications.len() >= MAX_TREE_RETIREMENTS_PER_GC {
                break 'retirement_pages;
            }
        }
        if row_count < 64 {
            break;
        }
        retirement_offset = retirement_offset.saturating_add(row_count);
    }
    let tree_manifests = finishing_retirements
        .iter()
        .map(|(_, _, manifest_hash)| *manifest_hash)
        .collect::<Vec<_>>();
    let mut tree_build_intents = Vec::new();
    let mut selected_intent_nodes = Vec::new();
    let mut selected_intent_delta_nodes = Vec::new();
    let mut marked_intent_nodes = Vec::new();
    let mut finishing_abandoned_manifests = Vec::new();
    let mut intent_offset = 0_i64;
    'intent_pages: loop {
        let rows = client
            .query(
            "SELECT database_id, log_generation, expected_revision, manifest_hash, intent_state, \
                    (SELECT count(*) FROM atomic_tree_build_intent_nodes n \
                      WHERE n.manifest_hash = i.manifest_hash), \
                    (SELECT count(*) FROM atomic_tree_delta_nodes d \
                      WHERE d.manifest_hash = i.manifest_hash) \
               FROM atomic_tree_build_intents i \
              WHERE ((i.intent_state = 2 \
                      AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_headers h \
                                      WHERE h.manifest_hash = i.manifest_hash)) \
                  OR i.intent_state = 3 \
                  OR (i.intent_state IN (0, 1) \
                      AND i.heartbeat_at < clock_timestamp() - \
                                           $1::bigint * interval '1 millisecond' \
                      AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                      WHERE p.manifest_hash = i.manifest_hash) \
                      AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                      WHERE a.manifest_hash = i.manifest_hash))) \
                AND NOT (i.intent_state <> 2 \
                         AND EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                     WHERE a.manifest_hash = i.manifest_hash) \
                         AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                         WHERE p.manifest_hash = i.manifest_hash)) \
              ORDER BY CASE WHEN i.intent_state IN (2, 3) THEN 0 ELSE 1 END, \
                       i.heartbeat_at, i.manifest_hash \
              LIMIT 64 OFFSET $2",
                &[&older_than_millis, &intent_offset],
            )
            .map_err(|error| operation_error("operations/gc-tree-build-intents", error))?;
        if rows.is_empty() {
            break;
        }
        let row_count = rows.len() as i64;
        for row in rows {
            let state: i16 = row.get(4);
            let intent = TreeBuildIntentGarbage {
                database_id: row.get(0),
                log_generation: positive_or_zero(row.get(1), "build log generation")?,
                expected_revision: positive_or_zero(row.get(2), "build expected revision")?,
                manifest_hash: digest(row.get(3), "build intent manifest hash")?,
                abandoned: state != 2,
            };
            if !try_lock_tree_build_for_gc(client, intent.manifest_hash)? {
                continue;
            }
            let intent_node_count = positive_or_zero(row.get(5), "build intent node count")?;
            let delta_node_count = positive_or_zero(row.get(6), "build delta node count")?;
            let mut selected_count = 0_usize;
            for node in client
                .query(
                    "SELECT node_hash, EXISTS (SELECT 1 FROM atomic_tree_nodes stored \
                                               WHERE stored.node_hash = n.node_hash) \
                       FROM atomic_tree_build_intent_nodes n \
                      WHERE manifest_hash = $1 \
                      ORDER BY node_hash LIMIT $2",
                    &[
                        &&intent.manifest_hash[..],
                        &(MAX_TREE_BUILD_INTENT_NODES_PER_GC as i64),
                    ],
                )
                .map_err(|error| operation_error("operations/gc-intent-node-batch", error))?
            {
                let hash = digest(node.get(0), "build intent node hash")?;
                selected_intent_nodes.push((intent.manifest_hash, hash));
                selected_count += 1;
                if intent.abandoned && node.get::<_, bool>(1) {
                    marked_intent_nodes.push(hash);
                }
            }
            let remaining = MAX_TREE_BUILD_INTENT_NODES_PER_GC.saturating_sub(selected_count);
            if intent.abandoned && remaining > 0 {
                for node in client
                    .query(
                        "SELECT node_hash FROM atomic_tree_delta_nodes \
                          WHERE manifest_hash = $1 \
                          ORDER BY node_hash LIMIT $2",
                        &[&&intent.manifest_hash[..], &(remaining as i64)],
                    )
                    .map_err(|error| operation_error("operations/gc-intent-delta-batch", error))?
                {
                    selected_intent_delta_nodes.push((
                        intent.manifest_hash,
                        digest(node.get(0), "abandoned delta node hash")?,
                    ));
                }
            }
            if intent.abandoned
                && intent_node_count.saturating_add(delta_node_count)
                    <= MAX_TREE_BUILD_INTENT_NODES_PER_GC as u64
            {
                finishing_abandoned_manifests.push(intent.manifest_hash);
            }
            tree_build_intents.push(intent);
            if tree_build_intents.len() >= MAX_TREE_BUILD_INTENTS_PER_GC {
                break 'intent_pages;
            }
        }
        if row_count < 64 {
            break;
        }
        intent_offset = intent_offset.saturating_add(row_count);
    }
    let tree_nodes = predicted_tree_node_garbage(
        client,
        &selected_retirement_nodes,
        &finishing_retirements,
        &selected_intent_nodes,
        &selected_intent_delta_nodes,
        &finishing_abandoned_manifests,
        &marked_intent_nodes,
    )?;

    // The legacy flat index has no exact post-CAS retirement witness. Age is
    // not reachability, so those segments remain retained. Program blobs have
    // a separate complete per-log-generation reference ledger.
    let segments = Vec::new();
    let programs = client
        .query(
            "SELECT c.program_hash \
               FROM atomic_program_gc_candidates c \
              WHERE c.candidate_at < clock_timestamp() - \
                                     $1::bigint * interval '1 millisecond' \
                AND EXISTS (SELECT 1 FROM atomic_program_reference_state s \
                            WHERE s.singleton AND s.complete AND s.problem_code IS NULL) \
                AND NOT EXISTS (SELECT 1 FROM atomic_program_generation_refs r \
                                WHERE r.program_hash = c.program_hash) \
              ORDER BY c.candidate_at, c.program_hash \
              LIMIT $2",
            &[&older_than_millis, &(MAX_PROGRAMS_PER_GC as i64)],
        )
        .map_err(|error| operation_error("operations/gc-program-candidates", error))?
        .into_iter()
        .map(|row| digest(row.get(0), "program garbage hash"))
        .collect::<Result<Vec<_>, _>>()?;
    let request_base_archives = request_base_archive_candidates(client, older_than_millis)?;
    let mut semantic_nodes = if request_base_archives.is_empty() {
        client
            .query(
                "SELECT node_hash \
               FROM atomic_semantic_commitment_nodes node \
              WHERE node.created_at < clock_timestamp() - \
                                      $1::bigint * interval '1 millisecond' \
                AND NOT EXISTS (SELECT 1 FROM atomic_semantic_commitment_roots root \
                                 WHERE root.current_root = node.node_hash) \
                AND NOT EXISTS (SELECT 1 FROM atomic_semantic_commitment_nodes parent \
                                 WHERE parent.left_hash = node.node_hash \
                                    OR parent.right_hash = node.node_hash) \
              ORDER BY node.created_at, node.node_hash LIMIT $2",
                &[
                    &older_than_millis,
                    &(MAX_SEMANTIC_COMMITMENT_NODES_PER_GC as i64),
                ],
            )
            .map_err(|error| operation_error("operations/gc-semantic-node-candidates", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "semantic commitment garbage hash"))
            .collect::<Result<Vec<_>, _>>()?
    } else {
        Vec::new()
    };
    semantic_nodes.sort_unstable();
    let log_generations = if request_base_archives.is_empty() {
        log_generation_candidates(client, older_than_millis)?
    } else {
        Vec::new()
    };
    let semantic_roots = if let Some(candidate) = log_generations
        .iter()
        .find(|candidate| candidate.semantic_roots_removed > 0)
    {
        semantic_root_candidates(
            client,
            &candidate.database_id,
            candidate.generation,
            MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC,
        )?
    } else {
        Vec::new()
    };
    Ok(GarbageCandidates {
        segments,
        programs,
        tree_publications,
        tree_build_intents,
        tree_manifests,
        tree_nodes,
        request_base_archives,
        log_generations,
        semantic_roots,
        semantic_nodes,
    })
}

/// Select one inactive exact-retry archive and acquire the same generation,
/// manifest, and restore/build fences that the owner SQL function repeats.
/// A partially released archive remains first-class work until its header is
/// gone; only then may semantic/log generation collection advance.
fn request_base_archive_candidates<C: postgres::GenericClient>(
    client: &mut C,
    older_than_millis: i64,
) -> Result<Vec<RequestBaseArchiveGarbage>, SemanticError> {
    const PROBE_PAGE: i64 = 64;
    let maximum_rows = MAX_REQUEST_BASE_ARCHIVE_NODES_PER_GC as u64;
    let mut offset = 0_i64;
    loop {
        let rows = client
            .query(
                "SELECT archive.database_id, archive.generation, archive.manifest_hash, \
                        (SELECT count(*) FROM atomic_generation_request_bases base \
                          WHERE base.database_id = archive.database_id \
                            AND base.generation = archive.generation \
                            AND base.base_manifest_hash = archive.manifest_hash), \
                        (SELECT count(*) FROM atomic_request_base_archive_completions complete \
                          WHERE complete.manifest_hash = archive.manifest_hash), \
                        (SELECT count(*) FROM atomic_request_base_archive_nodes node \
                          WHERE node.manifest_hash = archive.manifest_hash), \
                        (SELECT count(*) FROM atomic_request_base_archive_roots root \
                          WHERE root.manifest_hash = archive.manifest_hash), \
                        retirement.generation IS NOT NULL \
                   FROM atomic_request_base_archives archive \
                   JOIN atomic_log_generations generation \
                     ON generation.database_id = archive.database_id \
                    AND generation.generation = archive.generation \
                   JOIN atomic_databases database \
                     ON database.database_id = archive.database_id \
                   LEFT JOIN atomic_log_generation_builds build \
                     ON build.database_id = archive.database_id \
                    AND build.generation = archive.generation \
                   LEFT JOIN atomic_log_generation_retirements retirement \
                     ON retirement.database_id = archive.database_id \
                    AND retirement.generation = archive.generation \
                   LEFT JOIN atomic_log_generation_abandonment_progress abandonment \
                     ON abandonment.database_id = archive.database_id \
                    AND abandonment.generation = archive.generation \
                  WHERE NOT EXISTS ( \
                            SELECT 1 FROM atomic_heads active \
                             WHERE active.database_id = archive.database_id \
                               AND active.log_generation = archive.generation \
                        ) \
                    AND ( \
                        (retirement.generation IS NOT NULL \
                         AND (retirement.collecting_at IS NOT NULL \
                              OR retirement.retired_at \
                                 + $1::bigint * interval '1 millisecond' \
                                    <= clock_timestamp())) \
                        OR \
                        (retirement.generation IS NULL \
                         AND build.generation IS NOT NULL \
                         AND NOT EXISTS ( \
                               SELECT 1 FROM atomic_log_generation_activations activation \
                                WHERE activation.database_id = archive.database_id \
                                  AND activation.generation = archive.generation \
                         ) \
                         AND ( \
                              (generation.build_kind = 0 \
                               AND build.source_generation IS NULL \
                               AND build.captured_basis_t = 0 \
                               AND build.captured_head_hash = database.genesis_hash \
                               AND build.frozen_plan_hash IS NOT NULL \
                               AND build.restore_manifest_hash IS NULL \
                               AND build.restore_basis_t IS NULL \
                               AND build.restore_head_hash IS NULL \
                               AND NOT EXISTS ( \
                                     SELECT 1 FROM atomic_heads any_head \
                                      WHERE any_head.database_id = archive.database_id \
                               ) \
                               AND NOT EXISTS ( \
                                     SELECT 1 FROM atomic_log_generations other \
                                      WHERE other.database_id = archive.database_id \
                                        AND other.generation <> archive.generation \
                               )) \
                              OR (generation.build_kind = 2 \
                                  AND build.source_generation IS NOT NULL) \
                         ) \
                         AND (abandonment.generation IS NOT NULL OR ( \
                              generation.created_at \
                                + $1::bigint * interval '1 millisecond' \
                                    <= clock_timestamp() \
                              AND (generation.build_kind = 0 OR NOT EXISTS ( \
                                   SELECT 1 FROM atomic_heads source \
                                    WHERE source.database_id = archive.database_id \
                                      AND source.log_generation = build.source_generation \
                              )) \
                         ))) \
                    ) \
                  ORDER BY (retirement.collecting_at IS NULL \
                            AND abandonment.generation IS NULL), \
                           COALESCE(retirement.collecting_at, abandonment.updated_at, \
                                    retirement.retired_at, generation.created_at), \
                           archive.database_id, archive.generation, archive.archive_revision \
                  LIMIT $2 OFFSET $3",
                &[&older_than_millis, &PROBE_PAGE, &offset],
            )
            .map_err(|error| {
                operation_error("operations/gc-request-base-archive-candidates", error)
            })?;
        if rows.is_empty() {
            return Ok(Vec::new());
        }
        let row_count = rows.len() as i64;
        for row in rows {
            let database_id: String = row.get(0);
            let generation = positive_or_zero(row.get(1), "request-base archive generation")?;
            let manifest_hash = digest(row.get(2), "request-base archive manifest hash")?;
            let binding_count = positive_or_zero(row.get(3), "request-base binding count")?;
            let completion_count =
                positive_or_zero(row.get(4), "request-base archive completion count")?;
            let node_count = positive_or_zero(row.get(5), "request-base archive node count")?;
            let root_count = positive_or_zero(row.get(6), "request-base archive root count")?;
            let retired: bool = row.get(7);
            let generation_sql = sql_u64(generation, "request-base archive generation")?;
            let generation_key: Option<i64> = client
                .query_one(
                    "SELECT atomic_log_generation_pin_key($1, $2)",
                    &[&database_id, &generation_sql],
                )
                .map_err(|error| {
                    operation_error("operations/gc-request-base-generation-pin-key", error)
                })?
                .get(0);
            let Some(generation_key) = generation_key else {
                continue;
            };
            let generation_locked: bool = client
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&generation_key])
                .map_err(|error| {
                    operation_error("operations/gc-request-base-generation-pin", error)
                })?
                .get(0);
            let manifest_locked = if generation_locked {
                try_lock_tree_manifest_for_gc(client, manifest_hash)?
            } else {
                false
            };
            if !generation_locked || !manifest_locked {
                continue;
            }
            if !retired {
                let lock_row = client
                    .query_one(
                        "SELECT atomic_tree_database_build_pin_key($1), \
                                hashtextextended('atomic/excision-worker/v1/' || lineage_id, \
                                                 4707476001900298240::bigint), \
                                hashtextextended('atomic/restore/' || $1, 0) \
                           FROM atomic_databases WHERE database_id = $1",
                        &[&database_id],
                    )
                    .map_err(|error| {
                        operation_error("operations/gc-request-base-restore-pin-keys", error)
                    })?;
                let lock_keys = [
                    lock_row.get::<_, Option<i64>>(0),
                    lock_row.get::<_, Option<i64>>(1),
                    lock_row.get::<_, Option<i64>>(2),
                ];
                let mut all_locked = true;
                for lock_key in lock_keys {
                    let Some(lock_key) = lock_key else {
                        all_locked = false;
                        break;
                    };
                    let locked: bool = client
                        .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&lock_key])
                        .map_err(|error| {
                            operation_error("operations/gc-request-base-restore-pin", error)
                        })?
                        .get(0);
                    if !locked {
                        all_locked = false;
                        break;
                    }
                }
                if !all_locked {
                    continue;
                }
            }
            let (rows_removed, is_complete) = if binding_count > maximum_rows {
                (maximum_rows, false)
            } else {
                let bounded_nodes = node_count.min(maximum_rows);
                let complete = node_count <= maximum_rows;
                let terminal_rows = if complete {
                    root_count.saturating_add(1)
                } else {
                    0
                };
                (
                    binding_count
                        .saturating_add(completion_count)
                        .saturating_add(bounded_nodes)
                        .saturating_add(terminal_rows),
                    complete,
                )
            };
            return Ok(vec![RequestBaseArchiveGarbage {
                database_id,
                generation,
                manifest_hash,
                rows_removed,
                is_complete,
            }]);
        }
        if row_count < PROBE_PAGE {
            return Ok(Vec::new());
        }
        offset = offset.saturating_add(PROBE_PAGE);
    }
}

/// Select and preview one retired physical log generation without closing
/// admission. The exclusive transaction lock proves that no connected peer
/// or backup still owns the generation; the owner SQL function takes the same
/// lock again when the preview is applied.
fn log_generation_candidates<C: postgres::GenericClient>(
    client: &mut C,
    older_than_millis: i64,
) -> Result<Vec<LogGenerationGarbage>, SemanticError> {
    const PROBE_PAGE: i64 = 64;
    let mut offset = 0_i64;
    loop {
        let rows = client
            .query(
                "SELECT r.database_id, r.generation, \
                        COALESCE(progress.phase, 0::smallint) \
                   FROM atomic_log_generation_retirements r \
                   LEFT JOIN atomic_log_generation_collection_progress progress \
                     ON progress.database_id = r.database_id \
                    AND progress.generation = r.generation \
                  WHERE (progress.generation IS NOT NULL OR \
                         r.retired_at + $1::bigint * interval '1 millisecond' \
                             <= clock_timestamp()) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_heads h \
                                     WHERE h.database_id = r.database_id \
                                       AND h.log_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents i \
                                     WHERE i.database_id = r.database_id \
                                       AND i.log_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifests m \
                                     WHERE m.database_id = r.database_id \
                                       AND m.log_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                     WHERE p.database_id = r.database_id \
                                       AND p.log_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirements tr \
                                     WHERE tr.database_id = r.database_id \
                                       AND tr.log_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_builds b \
                                     WHERE b.database_id = r.database_id \
                                       AND (b.generation = r.generation \
                                            OR b.source_generation = r.generation)) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_retirements successor \
                                     WHERE successor.database_id = r.database_id \
                                       AND successor.successor_generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_generation_request_bases base \
                                     WHERE base.database_id = r.database_id \
                                       AND base.generation = r.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archives archive \
                                     WHERE archive.database_id = r.database_id \
                                       AND archive.generation = r.generation) \
                  ORDER BY progress.generation IS NULL, \
                           COALESCE(progress.updated_at, r.retired_at), \
                           r.database_id, r.generation \
                  LIMIT $2 OFFSET $3",
                &[&older_than_millis, &PROBE_PAGE, &offset],
            )
            .map_err(|error| operation_error("operations/gc-log-generation-candidates", error))?;
        if rows.is_empty() {
            break;
        }
        let row_count = rows.len() as i64;
        for row in rows {
            let database_id: String = row.get(0);
            let generation = positive_or_zero(row.get(1), "retired log generation")?;
            let phase = positive_i16(row.get(2), "log collection phase")?;
            let generation_sql = sql_u64(generation, "log generation")?;
            let pin_key: Option<i64> = client
                .query_one(
                    "SELECT atomic_log_generation_pin_key($1, $2)",
                    &[&database_id, &generation_sql],
                )
                .map_err(|error| operation_error("operations/gc-log-generation-pin-key", error))?
                .get(0);
            let Some(pin_key) = pin_key else { continue };
            let locked: bool = client
                .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&pin_key])
                .map_err(|error| operation_error("operations/gc-log-generation-pin", error))?
                .get(0);
            if locked {
                return Ok(vec![preview_log_generation_phase(
                    client,
                    database_id,
                    generation,
                    phase,
                )?]);
            }
        }
        if row_count < PROBE_PAGE {
            break;
        }
        offset = offset.saturating_add(PROBE_PAGE);
    }
    abandoned_log_generation_candidates(client, older_than_millis)
}

fn abandoned_log_generation_candidates<C: postgres::GenericClient>(
    client: &mut C,
    older_than_millis: i64,
) -> Result<Vec<LogGenerationGarbage>, SemanticError> {
    const PROBE_PAGE: i64 = 64;
    let mut offset = 0_i64;
    loop {
        let rows = client
            .query(
                "SELECT g.database_id, g.generation, \
                        COALESCE(progress.phase, 0::smallint) \
                   FROM atomic_log_generations g \
                   JOIN atomic_log_generation_builds b \
                     ON b.database_id = g.database_id AND b.generation = g.generation \
                   JOIN atomic_databases d ON d.database_id = g.database_id \
                   LEFT JOIN atomic_log_generation_abandonment_progress progress \
                     ON progress.database_id = g.database_id \
                    AND progress.generation = g.generation \
                  WHERE NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                     WHERE a.database_id = g.database_id \
                                       AND a.generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_heads active \
                                     WHERE active.database_id = g.database_id \
                                       AND active.log_generation = g.generation) \
                    AND ( \
                         (g.build_kind = 0 \
                          AND b.source_generation IS NULL \
                          AND b.captured_basis_t = 0 \
                          AND b.captured_head_hash = d.genesis_hash \
                          AND b.frozen_plan_hash IS NOT NULL \
                          AND b.restore_manifest_hash IS NULL \
                          AND b.restore_basis_t IS NULL \
                          AND b.restore_head_hash IS NULL \
                          AND NOT EXISTS (SELECT 1 FROM atomic_heads h \
                                           WHERE h.database_id = g.database_id) \
                          AND NOT EXISTS (SELECT 1 FROM atomic_log_generations other \
                                           WHERE other.database_id = g.database_id \
                                             AND other.generation <> g.generation)) \
                         OR (g.build_kind IN (1, 2) \
                             AND b.source_generation IS NOT NULL) \
                    ) \
                    AND (progress.generation IS NOT NULL OR ( \
                         g.created_at + $1::bigint * interval '1 millisecond' \
                             <= clock_timestamp() \
                         AND (g.build_kind = 0 OR NOT EXISTS ( \
                              SELECT 1 FROM atomic_heads source \
                               WHERE source.database_id = g.database_id \
                                 AND source.log_generation = b.source_generation \
                         )) \
                    )) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents i \
                                     WHERE i.database_id = g.database_id \
                                       AND i.log_generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifests m \
                                     WHERE m.database_id = g.database_id \
                                       AND m.log_generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                     WHERE p.database_id = g.database_id \
                                       AND p.log_generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirements r \
                                     WHERE r.database_id = g.database_id \
                                       AND r.log_generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_builds dependent \
                                     WHERE dependent.database_id = g.database_id \
                                       AND dependent.source_generation = g.generation \
                                       AND dependent.generation <> g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_generation_request_bases base \
                                     WHERE base.database_id = g.database_id \
                                       AND base.generation = g.generation) \
                    AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archives archive \
                                     WHERE archive.database_id = g.database_id \
                                       AND archive.generation = g.generation) \
                  ORDER BY progress.generation IS NULL, \
                           COALESCE(progress.updated_at, g.created_at), \
                           g.database_id, g.generation \
                  LIMIT $2 OFFSET $3",
                &[&older_than_millis, &PROBE_PAGE, &offset],
            )
            .map_err(|error| {
                operation_error("operations/gc-abandoned-generation-candidates", error)
            })?;
        if rows.is_empty() {
            return Ok(Vec::new());
        }
        let row_count = rows.len() as i64;
        for row in rows {
            let database_id: String = row.get(0);
            let generation = positive_or_zero(row.get(1), "abandoned log generation")?;
            let phase = positive_i16(row.get(2), "log abandonment phase")?;
            let generation_sql = sql_u64(generation, "abandoned log generation")?;
            let lock_row = client
                .query_one(
                    "SELECT atomic_tree_database_build_pin_key($1), \
                            hashtextextended('atomic/excision-worker/v1/' || lineage_id, \
                                             4707476001900298240::bigint), \
                            atomic_log_generation_pin_key($1, $2), \
                            hashtextextended('atomic/restore/' || $1, 0) \
                       FROM atomic_databases WHERE database_id = $1",
                    &[&database_id, &generation_sql],
                )
                .map_err(|error| {
                    operation_error("operations/gc-abandoned-generation-pin-keys", error)
                })?;
            let lock_keys = [
                lock_row.get::<_, Option<i64>>(0),
                lock_row.get::<_, Option<i64>>(1),
                lock_row.get::<_, Option<i64>>(2),
                lock_row.get::<_, Option<i64>>(3),
            ];
            let mut all_locked = true;
            for lock_key in lock_keys {
                let Some(lock_key) = lock_key else {
                    all_locked = false;
                    break;
                };
                let locked: bool = client
                    .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&lock_key])
                    .map_err(|error| {
                        operation_error("operations/gc-abandoned-generation-pin", error)
                    })?
                    .get(0);
                if !locked {
                    all_locked = false;
                    break;
                }
            }
            if all_locked {
                return Ok(vec![preview_abandoned_log_generation_phase(
                    client,
                    database_id,
                    generation,
                    phase,
                )?]);
            }
        }
        if row_count < PROBE_PAGE {
            return Ok(Vec::new());
        }
        offset = offset.saturating_add(PROBE_PAGE);
    }
}

fn preview_abandoned_log_generation_phase<C: postgres::GenericClient>(
    client: &mut C,
    database_id: String,
    generation: u64,
    phase: u16,
) -> Result<LogGenerationGarbage, SemanticError> {
    let generation_sql = sql_u64(generation, "abandoned log generation")?;
    let maximum_rows = MAX_LOG_GENERATION_ROWS_PER_GC as i64;
    let semantic_roots = bounded_generation_row_count(
        client,
        "SELECT 1 FROM atomic_semantic_commitment_roots \
          WHERE database_id = $1 AND generation = $2 \
          ORDER BY basis_t LIMIT $3",
        &database_id,
        generation_sql,
        MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC as i64,
    )?;
    if semantic_roots > 0 {
        return Ok(LogGenerationGarbage {
            database_id,
            generation,
            abandoned: true,
            collection_phase: phase,
            rows_removed: semantic_roots,
            semantic_roots_removed: semantic_roots,
            is_complete: false,
        });
    }
    let rows_removed = match phase {
        0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_request_tempids \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_key_hash, tempid_name LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        1 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        2 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_transactions \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        3 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_program_generation_refs \
              WHERE database_id = $1 AND log_generation = $2 \
              ORDER BY program_hash LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        4 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_garbage_contents \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY content_hash LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        5 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_completed_excision_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_t, request_entity LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        6 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_excision_predicates \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_t, request_entity LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        7 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_checkpoints \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY through_basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        8 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_completion_stages \
              WHERE database_id = $1 AND generation = $2 LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        9 => 0,
        10 => 1,
        _ => {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "operations/invalid-log-abandonment-phase",
                format!("inactive log generation has invalid abandonment phase {phase}"),
            ));
        }
    };
    let (collection_phase, is_complete) = if phase == 10 {
        (10, true)
    } else if rows_removed == 0 {
        (phase + 1, false)
    } else {
        (phase, false)
    };
    Ok(LogGenerationGarbage {
        database_id,
        generation,
        abandoned: true,
        collection_phase,
        rows_removed,
        semantic_roots_removed: 0,
        is_complete,
    })
}

fn preview_log_generation_phase<C: postgres::GenericClient>(
    client: &mut C,
    database_id: String,
    generation: u64,
    phase: u16,
) -> Result<LogGenerationGarbage, SemanticError> {
    let generation_sql = sql_u64(generation, "log generation")?;
    let maximum_rows = MAX_LOG_GENERATION_ROWS_PER_GC as i64;
    let semantic_roots = bounded_generation_row_count(
        client,
        "SELECT 1 FROM atomic_semantic_commitment_roots \
          WHERE database_id = $1 AND generation = $2 \
          ORDER BY basis_t LIMIT $3",
        &database_id,
        generation_sql,
        MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC as i64,
    )?;
    if semantic_roots > 0 {
        return Ok(LogGenerationGarbage {
            database_id,
            generation,
            abandoned: false,
            collection_phase: phase,
            rows_removed: semantic_roots,
            semantic_roots_removed: semantic_roots,
            is_complete: false,
        });
    }
    let rows_removed = match phase {
        0 if generation == 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_index_publications \
              WHERE database_id = $1 AND $2::bigint = 0 ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        1 if generation == 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_index_manifests \
              WHERE database_id = $1 AND $2::bigint = 0 ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        2 if generation > 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_request_tempids \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_key_hash, tempid_name LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        3 if generation == 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_requests WHERE database_id = $1 AND $2::bigint = 0 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        3 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        4 if generation == 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_transactions WHERE database_id = $1 AND $2::bigint = 0 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        4 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_generation_transactions \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY basis_t LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        5 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_program_generation_refs \
              WHERE database_id = $1 AND log_generation = $2 \
              ORDER BY program_hash LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        6 if generation > 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_garbage_contents \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY content_hash LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        7 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_completed_excision_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_t, request_entity LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        8 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_completions \
              WHERE database_id = $1 AND generation = $2 LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        9 if generation > 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generation_activations \
              WHERE database_id = $1 AND generation = $2 LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        10 if generation > 0 => bounded_generation_row_count(
            client,
            "SELECT 1 FROM atomic_log_generations \
              WHERE database_id = $1 AND generation = $2 LIMIT $3",
            &database_id,
            generation_sql,
            maximum_rows,
        )?,
        11 => 1,
        0..=10 => 0,
        _ => {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "operations/invalid-log-collection-phase",
                format!("retired log generation has invalid collection phase {phase}"),
            ));
        }
    };
    let (collection_phase, is_complete) = if phase == 11 {
        (11, true)
    } else if rows_removed == 0 {
        (phase + 1, false)
    } else {
        (phase, false)
    };
    Ok(LogGenerationGarbage {
        database_id,
        generation,
        abandoned: false,
        collection_phase,
        rows_removed,
        semantic_roots_removed: 0,
        is_complete,
    })
}

fn bounded_generation_row_count<C: postgres::GenericClient>(
    client: &mut C,
    selection: &str,
    database_id: &str,
    generation: i64,
    maximum_rows: i64,
) -> Result<u64, SemanticError> {
    let sql = format!("SELECT count(*) FROM ({selection}) selected");
    let count: i64 = client
        .query_one(&sql, &[&database_id, &generation, &maximum_rows])
        .map_err(|error| operation_error("operations/gc-log-generation-preview", error))?
        .get(0);
    positive_or_zero(count, "previewed log-generation rows")
}

fn semantic_root_candidates<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    maximum_roots: usize,
) -> Result<Vec<SemanticCommitmentRootGarbage>, SemanticError> {
    let rows = client
        .query(
            "SELECT basis_t, tx_hash, current_root \
               FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY basis_t LIMIT $3",
            &[
                &database_id,
                &sql_u64(generation, "semantic root generation")?,
                &(maximum_roots as i64),
            ],
        )
        .map_err(|error| operation_error("operations/gc-semantic-root-candidates", error))?;
    rows.into_iter()
        .map(|row| {
            Ok(SemanticCommitmentRootGarbage {
                database_id: database_id.to_owned(),
                generation,
                basis_t: positive_or_zero(row.get(0), "semantic root basis")?,
                tx_hash: digest(row.get(1), "semantic root transaction hash")?,
                current_root: optional_digest(row.get(2), "semantic commitment current root")?,
            })
        })
        .collect()
}

/// Predict the exact bounded value drain after the selected root prefix has
/// hypothetically retired. This is the dry-run counterpart of the owner SQL
/// function; it never scans node payloads or treats unmarked old content as
/// garbage.
fn predicted_tree_node_garbage<C: postgres::GenericClient>(
    client: &mut C,
    selected_retirement_nodes: &[(String, u64, Digest)],
    finishing_retirements: &[(String, u64, Digest)],
    selected_intent_nodes: &[(Digest, Digest)],
    selected_intent_delta_nodes: &[(Digest, Digest)],
    finishing_abandoned_manifests: &[Digest],
    marked_intent_nodes: &[Digest],
) -> Result<Vec<Digest>, SemanticError> {
    let retirement_databases = selected_retirement_nodes
        .iter()
        .map(|(database_id, _, _)| database_id.clone())
        .collect::<Vec<_>>();
    let retirement_revisions = selected_retirement_nodes
        .iter()
        .map(|(_, revision, _)| sql_u64(*revision, "publication revision"))
        .collect::<Result<Vec<_>, _>>()?;
    let retirement_nodes = selected_retirement_nodes
        .iter()
        .map(|(_, _, node_hash)| node_hash.to_vec())
        .collect::<Vec<_>>();
    let finishing_databases = finishing_retirements
        .iter()
        .map(|(database_id, _, _)| database_id.clone())
        .collect::<Vec<_>>();
    let finishing_revisions = finishing_retirements
        .iter()
        .map(|(_, revision, _)| sql_u64(*revision, "publication revision"))
        .collect::<Result<Vec<_>, _>>()?;
    let finishing_manifests = finishing_retirements
        .iter()
        .map(|(_, _, manifest_hash)| manifest_hash.to_vec())
        .collect::<Vec<_>>();
    let intent_manifests = selected_intent_nodes
        .iter()
        .map(|(manifest_hash, _)| manifest_hash.to_vec())
        .collect::<Vec<_>>();
    let intent_nodes = selected_intent_nodes
        .iter()
        .map(|(_, node_hash)| node_hash.to_vec())
        .collect::<Vec<_>>();
    let marked_intent_nodes = marked_intent_nodes
        .iter()
        .map(|node_hash| node_hash.to_vec())
        .collect::<Vec<_>>();
    let intent_delta_manifests = selected_intent_delta_nodes
        .iter()
        .map(|(manifest_hash, _)| manifest_hash.to_vec())
        .collect::<Vec<_>>();
    let intent_delta_nodes = selected_intent_delta_nodes
        .iter()
        .map(|(_, node_hash)| node_hash.to_vec())
        .collect::<Vec<_>>();
    let finishing_abandoned_manifests = finishing_abandoned_manifests
        .iter()
        .map(|manifest_hash| manifest_hash.to_vec())
        .collect::<Vec<_>>();
    client
        .query(
            "WITH selected_retirement_nodes(database_id, publication_revision, node_hash) AS ( \
                 SELECT * FROM unnest($1::text[], $2::bigint[], $3::bytea[]) \
             ), finishing_retirements(database_id, publication_revision, manifest_hash) AS ( \
                 SELECT * FROM unnest($4::text[], $5::bigint[], $6::bytea[]) \
             ), selected_intent_nodes(manifest_hash, node_hash) AS ( \
                 SELECT * FROM unnest($7::bytea[], $8::bytea[]) \
             ), selected_intent_delta_nodes(manifest_hash, node_hash) AS ( \
                 SELECT * FROM unnest($9::bytea[], $10::bytea[]) \
             ), finishing_abandoned_manifests(manifest_hash) AS ( \
                 SELECT * FROM unnest($11::bytea[]) \
             ), pending(node_hash) AS ( \
                 SELECT node_hash FROM atomic_tree_garbage_nodes \
                 UNION \
                 SELECT node_hash FROM selected_retirement_nodes \
                 UNION \
                 SELECT * FROM unnest($12::bytea[]) \
             ) \
             SELECT p.node_hash \
               FROM pending p \
              WHERE EXISTS (SELECT 1 FROM atomic_tree_nodes n WHERE n.node_hash = p.node_hash) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_live_nodes l \
                         WHERE l.node_hash = p.node_hash \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_retired_nodes r \
                         WHERE r.node_hash = p.node_hash \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM selected_retirement_nodes selected \
                                WHERE selected.database_id = r.database_id \
                                  AND selected.publication_revision = r.publication_revision \
                                  AND selected.node_hash = r.node_hash \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_delta_nodes d \
                         WHERE d.node_hash = p.node_hash \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM selected_intent_delta_nodes selected \
                                WHERE selected.manifest_hash = d.manifest_hash \
                                  AND selected.node_hash = d.node_hash \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_build_intent_nodes i \
                         WHERE i.node_hash = p.node_hash \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM selected_intent_nodes selected \
                                WHERE selected.manifest_hash = i.manifest_hash \
                                  AND selected.node_hash = i.node_hash \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_manifest_roots r \
                         WHERE r.root_hash = p.node_hash \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM finishing_retirements finishing \
                                WHERE finishing.manifest_hash = r.manifest_hash \
                           ) \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM finishing_abandoned_manifests finishing \
                                WHERE finishing.manifest_hash = r.manifest_hash \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_request_base_archive_nodes archive \
                         WHERE archive.node_hash = p.node_hash \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_request_base_archive_roots archive_root \
                         WHERE archive_root.root_hash = p.node_hash \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 \
                          FROM ( \
                                SELECT DISTINCT ON (database_id) \
                                       database_id, manifest_hash \
                                  FROM atomic_tree_publications \
                                 ORDER BY database_id, publication_revision DESC \
                               ) current_root \
                          LEFT JOIN atomic_tree_live_sets l \
                            ON l.database_id = current_root.database_id \
                         WHERE l.database_id IS NULL \
                            OR l.manifest_hash <> current_root.manifest_hash \
                            OR NOT l.complete \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 \
                          FROM atomic_tree_manifest_roots r \
                          JOIN atomic_tree_live_sets l ON l.manifest_hash = r.manifest_hash \
                         WHERE l.complete \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM atomic_tree_live_nodes n \
                                WHERE n.database_id = l.database_id \
                                  AND n.node_hash = r.root_hash \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_retirements r \
                         WHERE NOT r.garbage_complete \
                           AND NOT EXISTS ( \
                               SELECT 1 FROM finishing_retirements finishing \
                                WHERE finishing.database_id = r.database_id \
                                  AND finishing.publication_revision = r.publication_revision \
                           ) \
                    ) \
                AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_publications publication \
                         WHERE EXISTS ( \
                                   SELECT 1 FROM atomic_tree_publications newer \
                                    WHERE newer.database_id = publication.database_id \
                                      AND newer.publication_revision > publication.publication_revision \
                               ) \
                           AND NOT EXISTS ( \
                                   SELECT 1 FROM finishing_retirements finishing \
                                    WHERE finishing.database_id = publication.database_id \
                                      AND finishing.publication_revision = publication.publication_revision \
                               ) \
                           AND NOT EXISTS ( \
                                   SELECT 1 FROM atomic_tree_retirements r \
                                    WHERE r.database_id = publication.database_id \
                                      AND r.publication_revision = publication.publication_revision \
                                      AND r.manifest_hash = publication.manifest_hash \
                               ) \
                    ) \
              ORDER BY p.node_hash \
              LIMIT $13",
            &[
                &retirement_databases,
                &retirement_revisions,
                &retirement_nodes,
                &finishing_databases,
                &finishing_revisions,
                &finishing_manifests,
                &intent_manifests,
                &intent_nodes,
                &intent_delta_manifests,
                &intent_delta_nodes,
                &finishing_abandoned_manifests,
                &marked_intent_nodes,
                &(MAX_TREE_NODES_PER_GC as i64),
            ],
        )
        .map_err(|error| operation_error("operations/gc-tree-node-candidates", error))?
        .into_iter()
        .map(|row| digest(row.get(0), "tree garbage node hash"))
        .collect()
}

fn try_lock_tree_manifest_for_gc<C: postgres::GenericClient>(
    client: &mut C,
    manifest_hash: Digest,
) -> Result<bool, SemanticError> {
    client
        .query_one(
            "SELECT pg_try_advisory_xact_lock($1)",
            &[&tree_manifest_advisory_key(&manifest_hash)],
        )
        .map_err(|error| operation_error("operations/gc-root-pin", error))
        .map(|row| row.get(0))
}

fn try_lock_tree_build_for_gc<C: postgres::GenericClient>(
    client: &mut C,
    manifest_hash: Digest,
) -> Result<bool, SemanticError> {
    client
        .query_one(
            "SELECT pg_try_advisory_xact_lock($1)",
            &[&crate::tree_store::tree_build_advisory_key(&manifest_hash)],
        )
        .map_err(|error| operation_error("operations/gc-build-pin", error))
        .map(|row| row.get(0))
}

/// Stable PostgreSQL advisory-lock coordinate for one native manifest.
///
/// PostgreSQL exposes a 64-bit session lock key while SHA-256 is 256 bits.
/// Truncation collisions therefore conservatively pin both values; they can
/// retain garbage but can never make a live root collectible.
pub(crate) fn tree_manifest_advisory_key(manifest_hash: &Digest) -> i64 {
    let mut key = [0_u8; 8];
    key.copy_from_slice(&manifest_hash[..8]);
    i64::from_be_bytes(key) ^ 0x4154_4743_0000_0000_i64
}

fn require_gc_collected(collected: bool, label: &str) -> Result<(), SemanticError> {
    if collected {
        return Ok(());
    }
    Err(SemanticError::new(
        crate::ErrorCategory::Fault,
        "operations/gc-candidate-changed",
        format!("locked {label} candidate changed before deletion"),
    ))
}

/// Exact program hashes reachable from retained temporal log generations.
/// The migration-authenticated reference ledger covers legacy and native
/// generations, including fixed transitive dependencies. Guessing from
/// mutable deployment aliases could delete a superseded function still
/// required by an as-of database value.
fn inspect_temporal_program_references<C: postgres::GenericClient>(
    client: &mut C,
    inspected_database_id: &str,
    problems: &mut Vec<IntegrityProblem>,
) -> Result<Option<BTreeSet<Digest>>, SemanticError> {
    let state = client
        .query_opt(
            "SELECT complete, problem_code FROM atomic_program_reference_state \
              WHERE singleton",
            &[],
        )
        .map_err(|error| operation_error("operations/inspect-program-ref-state", error))?;
    let Some(state) = state else {
        problem(
            problems,
            "integrity/program-reference-state-missing",
            "program temporal-reference completeness marker is absent",
        );
        return Ok(None);
    };
    let complete: bool = state.get(0);
    let problem_code: Option<String> = state.get(1);
    if !complete || problem_code.is_some() {
        problem(
            problems,
            "integrity/program-references-incomplete",
            problem_code.unwrap_or_else(|| "program reference index is incomplete".into()),
        );
        return Ok(None);
    }

    let mut referenced = BTreeSet::new();
    for row in client
        .query(
            "SELECT r.database_id, r.program_hash, p.program_hash \
               FROM atomic_program_generation_refs r \
               LEFT JOIN atomic_programs p ON p.program_hash=r.program_hash",
            &[],
        )
        .map_err(|error| operation_error("operations/inspect-program-references", error))?
    {
        let database_id: String = row.get(0);
        let hash = digest(row.get(1), "program reference hash")?;
        let stored: Option<Vec<u8>> = row.get(2);
        if stored.is_none() {
            if database_id == inspected_database_id {
                problem(
                    problems,
                    "integrity/dangling-program-reference",
                    format!("program reference {} has no immutable value", hex(&hash)),
                );
            }
            return Ok(None);
        }
        referenced.insert(hash);
    }
    Ok(Some(referenced))
}

fn garbage_age_millis(duration: Duration) -> Result<i64, SemanticError> {
    i64::try_from(duration.as_millis()).map_err(|_| {
        SemanticError::incorrect("operations/duration-overflow", "duration is too large")
    })
}

fn count<C: postgres::GenericClient>(
    client: &mut C,
    sql: &str,
    database_id: &str,
) -> Result<u64, SemanticError> {
    let value: i64 = client
        .query_one(sql, &[&database_id])
        .map_err(|error| operation_error("operations/count", error))?
        .get(0);
    positive_or_zero(value, "count")
}

fn count_global<C: postgres::GenericClient>(
    client: &mut C,
    sql: &str,
) -> Result<u64, SemanticError> {
    let value: i64 = client
        .query_one(sql, &[])
        .map_err(|error| operation_error("operations/count", error))?
        .get(0);
    positive_or_zero(value, "count")
}

fn positive_or_zero(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/negative-value",
            format!("{label} is negative"),
        )
    })
}

fn positive_i16(value: i16, label: &str) -> Result<u16, SemanticError> {
    u16::try_from(value).map_err(|_| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/negative-value",
            format!("{label} is negative"),
        )
    })
}

fn digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|_| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/invalid-digest",
            format!("{label} is not 32 bytes"),
        )
    })
}

fn optional_digest(bytes: Option<Vec<u8>>, label: &str) -> Result<Option<Digest>, SemanticError> {
    bytes.map(|bytes| digest(bytes, label)).transpose()
}

fn problem(
    problems: &mut Vec<IntegrityProblem>,
    code: impl Into<String>,
    message: impl Into<String>,
) {
    problems.push(IntegrityProblem {
        code: code.into(),
        message: message.into(),
    });
}

fn hex(bytes: &Digest) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}

fn operation_error(code: &'static str, error: postgres::Error) -> SemanticError {
    crate::postgres::postgres_error(code, error)
}

#[cfg(test)]
mod history_projection_tests {
    use super::*;
    use crate::Value;
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    const USER_ATTRIBUTE: u32 = 100;

    fn datom(entity: u64, attribute: u32, value: Value, t: u64, added: bool) -> Datom {
        Datom {
            entity,
            attribute,
            value,
            tx: crate::t_to_tx(t).unwrap(),
            added,
        }
    }

    fn no_history(value: bool, t: u64, added: bool) -> Datom {
        datom(
            u64::from(USER_ATTRIBUTE),
            crate::DB_NO_HISTORY as u32,
            Value::Bool(value),
            t,
            added,
        )
    }

    fn canonical(mut datoms: Vec<Datom>) -> Vec<Datom> {
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        datoms
    }

    #[test]
    fn nested_no_history_omission_is_reducible_across_repeated_jobs() {
        let decimal = |spelling: &str| Value::BigDec(BigDecimal::from_str(spelling).unwrap());
        let replayed = canonical(vec![
            // Newest-to-oldest within one logical E/A/V group is R R A A.
            // Removing the inner pair exposes the outer pair to a later job.
            datom(1_000, USER_ATTRIBUTE, decimal("1.0"), 4, false),
            datom(1_000, USER_ATTRIBUTE, decimal("1.00"), 3, false),
            datom(1_000, USER_ATTRIBUTE, decimal("1.00"), 2, true),
            datom(1_000, USER_ATTRIBUTE, decimal("1.0"), 1, true),
            no_history(true, 3, true),
        ]);
        let physical = replayed
            .iter()
            .filter(|datom| datom.attribute == crate::DB_NO_HISTORY as u32)
            .cloned()
            .collect::<Vec<_>>();

        validate_physical_history_projection(&replayed, &physical, 5).unwrap();
    }

    #[test]
    fn physical_history_must_be_an_exact_replay_subsequence() {
        let replayed = canonical(vec![
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 2, false),
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 1, true),
            no_history(true, 2, true),
        ]);
        validate_physical_history_projection(&replayed, &replayed, 3).unwrap();

        let mut fabricated = replayed.clone();
        fabricated.push(datom(2_000, USER_ATTRIBUTE, Value::Long(9), 1, true));
        fabricated.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        assert_eq!(
            validate_physical_history_projection(&replayed, &fabricated, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );

        let one_sided = replayed
            .iter()
            .filter(|datom| !(datom.attribute == USER_ATTRIBUTE && datom.added))
            .cloned()
            .collect::<Vec<_>>();
        assert_eq!(
            validate_physical_history_projection(&replayed, &one_sided, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );
    }

    #[test]
    fn retraction_only_no_history_change_never_enables_omission() {
        let replayed = canonical(vec![
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 2, false),
            datom(1_000, USER_ATTRIBUTE, Value::Long(7), 1, true),
            // A standalone retraction of false yields the cardinality-one
            // default, false. It is not an assertion of true.
            no_history(false, 2, false),
        ]);
        let physical = replayed
            .iter()
            .filter(|datom| datom.attribute == crate::DB_NO_HISTORY as u32)
            .cloned()
            .collect::<Vec<_>>();
        assert_eq!(
            validate_physical_history_projection(&replayed, &physical, 3)
                .unwrap_err()
                .code,
            "integrity/tree-history-projection-mismatch"
        );
    }

    #[test]
    fn current_avet_becomes_checkable_before_history_projection_finishes() {
        let current_phase = crate::AvetProjectionWork::new(USER_ATTRIBUTE, true);
        assert!(avet_projection_is_pending(
            &[current_phase],
            USER_ATTRIBUTE,
            false
        ));
        assert!(avet_projection_is_pending(
            &[current_phase],
            USER_ATTRIBUTE,
            true
        ));

        let history_phase = crate::AvetProjectionWork {
            history: true,
            ..current_phase
        };
        assert!(!avet_projection_is_pending(
            &[history_phase],
            USER_ATTRIBUTE,
            false
        ));
        assert!(avet_projection_is_pending(
            &[history_phase],
            USER_ATTRIBUTE,
            true
        ));
    }
}
