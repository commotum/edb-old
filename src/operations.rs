use crate::cow_generation::{
    GenerationLogRow, GenerationRewriter, SourceLogEncoding, SourceLogRow, SourceRequest,
    SourceRequestKey,
};
use crate::database::ExcisionCutoff;
use crate::excision::{ExcisionTargetKind, PlannedExcisionPredicate};
use crate::log_generation::LineageTransactionContent;
use crate::persistent_tree::{TreeNode, TreeNodeSet, decode_tree_node, validate_tree};
use crate::postgres::{
    AuthenticatedLogTransaction, insert_program_generation_refs, read_authenticated_log_range,
    recover_generation_to, verify_schema_compatibility,
};
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    BackupVerification, DB_PARTITION, Database, Digest, MAX_EIDX, PersistentTreeManifest,
    PostgresConnectionConfig, SemanticError, Value, View, decode_genesis, decode_index_manifest,
    decode_index_segment, decode_transaction, eid_to_part, encode_genesis, encode_transaction,
    sha256, transaction_hash, tx_to_t,
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

/// Maximum detached program blobs drained in one operator transaction.
pub const MAX_PROGRAMS_PER_GC: usize = 512;

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
    /// Unique immutable native nodes reachable from this database's retained
    /// publications, including roots, directories, and leaves.
    pub tree_nodes: u64,
    pub tree_node_bytes: u64,
    /// Globally stored native nodes proven unreachable only when shared
    /// reachability is complete. Zero is conservative when it is not.
    pub orphan_tree_nodes: u64,
    /// Some other database has an undecodable published root. Global orphan
    /// counts are therefore withheld rather than guessed.
    pub shared_reachability_uncertain: bool,
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
    pub applied: bool,
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

#[derive(Default)]
struct GarbageCandidates {
    segments: Vec<Digest>,
    programs: Vec<Digest>,
    tree_publications: Vec<TreePublicationGarbage>,
    tree_build_intents: Vec<TreeBuildIntentGarbage>,
    tree_manifests: Vec<Digest>,
    tree_nodes: Vec<Digest>,
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
            applied,
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ExcisionFault {
    None,
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
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
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
        let rows = transaction
            .query(
                "SELECT basis_t, previous_hash, tx_hash, payload \
                 FROM atomic_transactions WHERE database_id = $1 ORDER BY basis_t",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/transactions", error))?;
        let mut previous = genesis_hash;
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
        if metrics.transactions != basis || previous != head_hash {
            problem(
                &mut problems,
                "integrity/head-mismatch",
                "head does not match the complete transaction chain",
            );
        }
        let dangling: i64 = transaction
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
        metrics.requests = count(
            &mut transaction,
            "SELECT count(*) FROM atomic_requests WHERE database_id = $1",
            database_id,
        )?;
        if dangling != 0 {
            problem(
                &mut problems,
                "integrity/dangling-request",
                format!("{dangling} durable requests do not identify a transaction"),
            );
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
        let mut candidates = garbage_candidates(&mut transaction, millis)?;
        // Publication visibility is independent of derived membership.  Move
        // one root's restart-safe fold after taking this call's dry-equivalent
        // inventory so newly enabled GC work is considered on the next call,
        // preserving exact preview/apply results.
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
            require_gc_collected(collected, "tree publication")?;
        }
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
            require_gc_collected(collected, "tree build intent")?;
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
        candidates.tree_nodes = collected_nodes;
        candidates.programs = transaction
            .query(
                "SELECT program_hash FROM atomic_collect_program_garbage($1, $2) AS program_hash",
                &[&millis, &(MAX_PROGRAMS_PER_GC as i64)],
            )
            .map_err(|error| operation_error("operations/gc-programs", error))?
            .into_iter()
            .map(|row| digest(row.get(0), "collected program hash"))
            .collect::<Result<Vec<_>, _>>()?;
        candidates.programs.sort_unstable();
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
        process_excision_with_session_fences(&mut self.client, database_id, fault_point)
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
    let worker_locked = client
        .query_one("SELECT pg_try_advisory_lock($1)", &[&worker_key])
        .map_err(|error| operation_error("excision/worker-pin", error))?
        .get::<_, bool>(0);
    if !worker_locked {
        let _ = client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&builder_key]);
        return Err(SemanticError::new(
            crate::ErrorCategory::Busy,
            "excision/already-running",
            "another background excision worker owns this database",
        ));
    }

    let result = process_excision_fenced(client, database_id, fault_point);
    let worker_release = client
        .query_one("SELECT pg_advisory_unlock($1)", &[&worker_key])
        .map_err(|error| operation_error("excision/worker-unpin", error));
    let builder_release = client
        .query_one("SELECT pg_advisory_unlock_shared($1)", &[&builder_key])
        .map_err(|error| operation_error("excision/builder-unpin", error));
    match result {
        Err(error) => Err(error),
        Ok(receipt) => {
            if !worker_release?.get::<_, bool>(0) || !builder_release?.get::<_, bool>(0) {
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
    database_id: &str,
    fault_point: ExcisionFault,
) -> Result<ExcisionReceipt, SemanticError> {
    if let Some(receipt) = resume_activated_excision(client, database_id)? {
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
    database_id: &str,
    lineage_id: &str,
    genesis_hash: Digest,
    source_generation: u64,
    captured_basis: u64,
    captured_hash: Digest,
    fault_point: ExcisionFault,
) -> Result<ExcisionReceipt, SemanticError> {
    let mut snapshot = client
        .build_transaction()
        .isolation_level(IsolationLevel::RepeatableRead)
        .start()
        .map_err(|error| operation_error("excision/snapshot-begin", error))?;
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
    let source_database = recover_generation_to(
        &mut snapshot,
        database_id,
        source_generation,
        captured_basis,
        captured_hash,
    )?
    .database;
    let completed = completed_excision_identities(&mut snapshot, database_id, source_generation)?;
    snapshot
        .query_one(
            "SELECT database_id FROM atomic_databases WHERE database_id = $1 FOR UPDATE",
            &[&database_id],
        )
        .map_err(|error| operation_error("excision/lock-generation-counter", error))?;
    let generation = positive_or_zero(
        snapshot
            .query_one(
                "SELECT COALESCE(max(generation), 0) + 1 \
                   FROM atomic_log_generations WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/allocate-generation", error))?
            .get(0),
        "allocated generation",
    )?;
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
                &sql_u64(captured_basis, "captured basis")?,
                &&captured_hash[..],
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
    snapshot
        .commit()
        .map_err(|error| operation_error("excision/capture-commit", error))?;

    rewrite_source_through(
        client,
        database_id,
        source_generation,
        generation,
        captured_basis,
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

    let (final_basis, final_source_hash) = loop {
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
        match client.query_one(
            "SELECT atomic_activate_log_generation($1, $2, $3, $4, $5, NULL)",
            &[
                &database_id,
                &sql_u64(generation, "generation")?,
                &sql_u64(active_basis, "activation basis")?,
                &&rewriter.current_head_hash()[..],
                &&rewriter.current_state_hash()?[..],
            ],
        ) {
            Ok(_) => break (active_basis, active_hash),
            Err(error) => {
                let semantic = operation_error("excision/activate", error);
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
    client
        .query_one(
            "SELECT atomic_complete_excision_generation($1, $2, NULL)",
            &[&database_id, &sql_u64(generation, "generation")?],
        )
        .map_err(|error| operation_error("excision/complete", error))?;
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
        resumed: false,
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
    let old_head_hash = digest(row.get(6), "captured source head")?;
    let removed_datoms = positive_or_zero(row.get(7), "removed datoms")?;
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
            && stored_version == 4
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
                "SELECT tx_hash, state_hash FROM atomic_transactions \
                 WHERE database_id = $1 AND basis_t = $2",
                &[&database_id, &sql_u64(basis, "tree publication basis")?],
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
        all_nodes.extend(manifest_nodes);
        // A prior excision generation remains retained physical history, but
        // it is no longer a usable index for the current database value.
        if publication_valid && stored_generation == generation {
            metrics.index_basis_t = metrics.index_basis_t.max(basis);
        }
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
                    m.manifest_version, m.manifest_hash, m.payload, t.state_hash \
             FROM atomic_tree_publications p LEFT JOIN atomic_tree_manifests m \
               ON m.database_id = p.database_id \
              AND m.publication_revision = p.publication_revision \
              AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
              AND m.manifest_hash = p.manifest_hash \
             LEFT JOIN atomic_transactions t \
               ON t.database_id = p.database_id AND t.basis_t = p.basis_t \
              AND t.tx_hash = p.tx_hash",
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
        if stored_database != published_database
            || stored_revision != published_revision
            || stored_basis != published_basis
            || stored_tx != published_tx
            || stored_state != authoritative_state
            || stored_version != 4
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
    reachable.tree_nodes.extend(native_nodes.keys().copied());
    let native_node_set = TreeNodeSet::from_nodes(native_nodes);
    if native_trees
        .iter()
        .any(|tree| validate_tree(tree, &native_node_set).is_err())
    {
        return Ok(None);
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

fn normalize_t_or_tx(value: u64) -> Result<u64, SemanticError> {
    if value <= MAX_EIDX {
        return Ok(value);
    }
    tx_to_t(value).map_err(|_| {
        SemanticError::incorrect(
            "excision/invalid-before-t",
            ":before_t must be a logical t or a transaction entity id",
        )
    })
}

fn same_database_information(left: &Database, right: &Database) -> bool {
    left.same_information_as(right)
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
    // Advance only one globally oldest root in this call. This is a contiguous
    // per-database prefix by construction; a pin on an earlier root prevents
    // a later root of that database from being selected out of order.
    let mut tree_publications = Vec::new();
    let mut selected_retirement_nodes = Vec::new();
    let mut finishing_retirements = Vec::new();
    for row in client
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
              ORDER BY r.retired_at, r.database_id, r.publication_revision \
              LIMIT $2",
            &[&older_than_millis, &(MAX_TREE_RETIREMENTS_PER_GC as i64)],
        )
        .map_err(|error| operation_error("operations/gc-tree-publications", error))?
    {
        let publication = TreePublicationGarbage {
            database_id: row.get(0),
            publication_revision: positive_or_zero(row.get(1), "tree publication revision")?,
            manifest_hash: digest(row.get(2), "tree publication manifest hash")?,
            garbage_complete: row.get(3),
        };
        // A live immutable PeerState holds the matching session-level shared
        // lock. Advisory-key collisions only make this return false, retaining
        // extra data conservatively.
        if try_lock_tree_manifest_for_gc(client, publication.manifest_hash)? {
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
        }
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
    for row in client
        .query(
            "SELECT database_id, log_generation, expected_revision, manifest_hash, intent_state, \
                    (SELECT count(*) FROM atomic_tree_build_intent_nodes n \
                      WHERE n.manifest_hash = i.manifest_hash), \
                    (SELECT count(*) FROM atomic_tree_delta_nodes d \
                      WHERE d.manifest_hash = i.manifest_hash) \
               FROM atomic_tree_build_intents i \
              WHERE (i.intent_state = 2 \
                     AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_headers h \
                                     WHERE h.manifest_hash = i.manifest_hash)) \
                 OR i.intent_state = 3 \
                 OR (i.intent_state IN (0, 1) \
                     AND i.heartbeat_at < clock_timestamp() - \
                                          $1::bigint * interval '1 millisecond' \
                     AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                     WHERE p.manifest_hash = i.manifest_hash) \
                     AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                     WHERE a.manifest_hash = i.manifest_hash)) \
              ORDER BY CASE WHEN i.intent_state IN (2, 3) THEN 0 ELSE 1 END, \
                       i.heartbeat_at, i.manifest_hash \
              LIMIT $2",
            &[&older_than_millis, &(MAX_TREE_BUILD_INTENTS_PER_GC as i64)],
        )
        .map_err(|error| operation_error("operations/gc-tree-build-intents", error))?
    {
        let state: i16 = row.get(4);
        let intent = TreeBuildIntentGarbage {
            database_id: row.get(0),
            log_generation: positive_or_zero(row.get(1), "build log generation")?,
            expected_revision: positive_or_zero(row.get(2), "build expected revision")?,
            manifest_hash: digest(row.get(3), "build intent manifest hash")?,
            abandoned: state != 2,
        };
        if try_lock_tree_build_for_gc(client, intent.manifest_hash)? {
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
        }
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
    Ok(GarbageCandidates {
        segments,
        programs,
        tree_publications,
        tree_build_intents,
        tree_manifests,
        tree_nodes,
    })
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

/// Exact content hashes reachable from ordinary temporal database
/// information. Until a derived SQL reference index is added, decode the
/// authoritative immutable log under the GC table lock. Guessing from legacy
/// deployment aliases can delete a superseded function still required by an
/// as-of database value.
fn inspect_temporal_program_references<C: postgres::GenericClient>(
    client: &mut C,
    inspected_database_id: &str,
    problems: &mut Vec<IntegrityProblem>,
) -> Result<Option<BTreeSet<Digest>>, SemanticError> {
    let mut referenced = BTreeSet::new();
    let mut complete = true;
    for row in client
        .query("SELECT database_id, genesis FROM atomic_databases", &[])
        .map_err(|error| operation_error("operations/inspect-program-genesis", error))?
    {
        let database_id: String = row.get(0);
        let payload: Vec<u8> = row.get(1);
        match decode_genesis(&payload) {
            Ok(datoms) => {
                for datom in datoms {
                    collect_function_hashes(&datom.value, &mut referenced);
                }
            }
            Err(error) => {
                complete = false;
                if database_id == inspected_database_id {
                    problem(
                        problems,
                        error.code,
                        format!("genesis program-reference scan: {}", error.message),
                    );
                }
            }
        }
    }
    for row in client
        .query(
            "SELECT database_id, basis_t, payload FROM atomic_transactions",
            &[],
        )
        .map_err(|error| operation_error("operations/inspect-program-transactions", error))?
    {
        let database_id: String = row.get(0);
        let basis = positive_or_zero(row.get(1), "program-reference basis")?;
        let payload: Vec<u8> = row.get(2);
        match decode_transaction(&payload) {
            Ok(transaction) => {
                for datom in transaction.tx_data {
                    collect_function_hashes(&datom.value, &mut referenced);
                }
            }
            Err(error) => {
                complete = false;
                if database_id == inspected_database_id {
                    problem(
                        problems,
                        error.code,
                        format!("basis {basis} program-reference scan: {}", error.message),
                    );
                }
            }
        }
    }
    Ok(complete.then_some(referenced))
}

fn collect_function_hashes(value: &Value, output: &mut BTreeSet<Digest>) {
    match value {
        Value::Function(hash) => {
            output.insert(*hash);
        }
        Value::Tuple(values) => {
            for value in values.iter().flatten() {
                collect_function_hashes(value, output);
            }
        }
        _ => {}
    }
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

fn digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|_| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/invalid-digest",
            format!("{label} is not 32 bytes"),
        )
    })
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
