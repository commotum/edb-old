use crate::persistent_tree::{TreeNode, TreeNodeSet, decode_tree_node, validate_tree};
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    BackupVerification, DB_PARTITION, Database, Digest, MAX_EIDX, PersistentTreeManifest,
    SemanticError, Value, View, decode_genesis, decode_index_manifest, decode_index_segment,
    decode_transaction, eid_to_part, encode_genesis, encode_transaction, sha256, transaction_hash,
    tx_to_t,
};
use postgres::{Client, IsolationLevel, NoTls};
use std::collections::{BTreeMap, BTreeSet};
use std::time::Duration;

/// Smallest supported grace period for ordinary garbage collection.
///
/// Datomic's operational guidance warns that a recent boundary can invalidate
/// index values still held by long-running consumers and recommends at least a
/// month outside initial import. Atomic has no separate import-mode GC API, so
/// the public operator always enforces that production-safe floor. Tests make
/// values old by backdating their PostgreSQL timestamps, never by weakening
/// this boundary.
pub const MIN_GARBAGE_COLLECTION_AGE: Duration = Duration::from_secs(30 * 24 * 60 * 60);

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
    /// Published or abandoned immutable manifests safe to remove after their
    /// roots cease to be eligible.
    pub tree_manifest_hashes: Vec<Digest>,
    /// Aged native values unreachable from every retained root publication.
    pub tree_node_hashes: Vec<Digest>,
    pub applied: bool,
}

#[derive(Clone, Debug, Eq, PartialEq, Ord, PartialOrd)]
pub struct TreePublicationGarbage {
    pub database_id: String,
    pub publication_revision: u64,
    pub manifest_hash: Digest,
}

#[derive(Default)]
struct GarbageCandidates {
    segments: Vec<Digest>,
    programs: Vec<Digest>,
    tree_publications: Vec<TreePublicationGarbage>,
    tree_manifests: Vec<Digest>,
    tree_nodes: Vec<Digest>,
}

impl GarbageCandidates {
    fn inventory(self, applied: bool) -> GarbageInventory {
        GarbageInventory {
            segment_hashes: self.segments,
            program_hashes: self.programs,
            tree_publications: self.tree_publications,
            tree_manifest_hashes: self.tree_manifests,
            tree_node_hashes: self.tree_nodes,
            applied,
        }
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum ExcisionTarget {
    Entity {
        entity: u64,
        /// Empty means every attribute of the entity.
        attributes: Vec<u32>,
    },
    Attribute(u32),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ExcisionSpec {
    pub excision_id: String,
    pub target: ExcisionTarget,
    /// Exclusive transaction-basis cutoff. `None` means all history.
    pub before_t: Option<u64>,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ExcisionFault {
    None,
    AfterRewrite,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ExcisionReceipt {
    pub database_id: String,
    pub excision_id: String,
    pub basis_t: u64,
    pub removed_datoms: u64,
    pub old_head_hash: Digest,
    pub new_head_hash: Digest,
    pub generation: u64,
    pub replayed: bool,
}

impl IntegrityReport {
    pub fn healthy(&self) -> bool {
        self.problems.is_empty()
    }
}

pub struct PostgresOperator {
    client: Client,
}

impl PostgresOperator {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        let client = Client::connect(connection, NoTls)
            .map_err(|error| operation_error("operations/connect", error))?;
        Ok(Self { client })
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
        let unversioned_programs = transaction
            .query(
                "SELECT p.program_hash FROM atomic_programs p WHERE NOT EXISTS \
                 (SELECT 1 FROM atomic_program_versions v WHERE v.program_hash = p.program_hash)",
                &[],
            )
            .map_err(|error| operation_error("operations/program-metrics", error))?;
        metrics.orphan_programs = match temporal_programs {
            Some(temporal_programs) => unversioned_programs
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
        lock_gc_inputs(&mut transaction)?;
        let candidates = garbage_candidates(&mut transaction, millis)?;
        transaction
            .commit()
            .map_err(|error| operation_error("operations/gc-inventory-commit", error))?;
        Ok(candidates.inventory(false))
    }

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
        lock_gc_inputs(&mut transaction)?;
        let candidates = garbage_candidates(&mut transaction, millis)?;
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .map_err(|error| operation_error("operations/gc-privilege", error))?;
        for publication in &candidates.tree_publications {
            let deleted = transaction
                .execute(
                    "DELETE FROM atomic_tree_publications \
                     WHERE database_id = $1 AND publication_revision = $2 \
                       AND manifest_hash = $3",
                    &[
                        &publication.database_id,
                        &sql_u64(publication.publication_revision, "publication revision")?,
                        &&publication.manifest_hash[..],
                    ],
                )
                .map_err(|error| operation_error("operations/gc-tree-publication", error))?;
            require_one_gc_delete(deleted, "tree publication")?;
        }
        for hash in &candidates.tree_manifests {
            transaction
                .execute(
                    "DELETE FROM atomic_tree_manifest_roots WHERE manifest_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-tree-roots", error))?;
            let deleted = transaction
                .execute(
                    "DELETE FROM atomic_tree_manifests WHERE manifest_hash = $1 \
                       AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications \
                                       WHERE manifest_hash = $1)",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-tree-manifest", error))?;
            require_one_gc_delete(deleted, "tree manifest")?;
        }
        for hash in &candidates.tree_nodes {
            let deleted = transaction
                .execute(
                    "DELETE FROM atomic_tree_nodes WHERE node_hash = $1 \
                       AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots \
                                       WHERE root_hash = $1)",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-tree-node", error))?;
            require_one_gc_delete(deleted, "tree node")?;
        }
        for hash in &candidates.segments {
            transaction
                .execute(
                    "DELETE FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-segment", error))?;
        }
        for hash in &candidates.programs {
            transaction
                .execute(
                    "DELETE FROM atomic_programs WHERE program_hash = $1 \
                     AND NOT EXISTS (SELECT 1 FROM atomic_program_versions \
                                     WHERE program_hash = $1)",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-program", error))?;
        }
        transaction
            .batch_execute("SET LOCAL session_replication_role = origin")
            .map_err(|error| operation_error("operations/gc-restore-triggers", error))?;
        transaction
            .commit()
            .map_err(|error| operation_error("operations/gc-commit", error))?;
        Ok(candidates.inventory(true))
    }

    /// Permanently rewrite matching history outside the logical timeline.
    ///
    /// The verified backup must cover the currently locked head. The whole
    /// chain, durable request references, head, derived-root invalidation,
    /// generation bump and non-sensitive audit record change in one SQL
    /// transaction. This is intentionally a privileged, exceptional path.
    pub fn excise_database(
        &mut self,
        database_id: &str,
        spec: &ExcisionSpec,
        backup: &BackupVerification,
    ) -> Result<ExcisionReceipt, SemanticError> {
        self.excise_database_with_fault(database_id, spec, backup, ExcisionFault::None)
    }

    pub fn excise_database_with_fault(
        &mut self,
        database_id: &str,
        spec: &ExcisionSpec,
        backup: &BackupVerification,
        fault_point: ExcisionFault,
    ) -> Result<ExcisionReceipt, SemanticError> {
        validate_excision_spec(spec)?;
        let before_t = spec.before_t.map(normalize_t_or_tx).transpose()?;
        let (target_kind, target_id, attributes) = normalized_target(&spec.target)?;
        let target_id_sql = sql_u64(target_id, "excision target")?;
        let attribute_ids: Vec<i32> = attributes
            .iter()
            .map(|value| {
                i32::try_from(*value).map_err(|_| {
                    SemanticError::incorrect(
                        "excision/attribute-out-of-range",
                        "attribute id cannot be represented by PostgreSQL INTEGER",
                    )
                })
            })
            .collect::<Result<_, _>>()?;
        let before_t_sql = before_t
            .map(|value| sql_u64(value, "before-t"))
            .transpose()?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| operation_error("excision/begin", error))?;
        let head = transaction
            .query_opt(
                "SELECT basis_t, tx_hash FROM atomic_heads \
                 WHERE database_id = $1 FOR UPDATE",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/lock-head", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    crate::ErrorCategory::NotFound,
                    "excision/database-not-found",
                    format!("database {database_id} does not exist"),
                )
            })?;
        let basis_t = positive_or_zero(head.get(0), "head basis")?;
        let old_head_hash = digest(head.get(1), "head hash")?;

        if let Some(row) = transaction
            .query_opt(
                "SELECT backup_basis_t, backup_manifest_hash, target_kind, target_id, \
                        attribute_ids, before_t, removed_datoms, old_head_hash, \
                        new_head_hash, generation \
                 FROM atomic_excisions WHERE database_id = $1 AND excision_id = $2",
                &[&database_id, &spec.excision_id],
            )
            .map_err(|error| operation_error("excision/idempotency-read", error))?
        {
            let stored_attrs: Vec<i32> = row.get(4);
            let stored_before: Option<i64> = row.get(5);
            let same = positive_or_zero(row.get(0), "backup basis")? == backup.point.basis_t
                && digest(row.get(1), "backup manifest hash")? == backup.point.manifest_hash
                && row.get::<_, i16>(2) == target_kind
                && positive_or_zero(row.get(3), "target id")? == target_id
                && stored_attrs == attribute_ids
                && stored_before == before_t_sql;
            if !same {
                return Err(SemanticError::new(
                    crate::ErrorCategory::Conflict,
                    "excision/idempotency-key-reused",
                    "excision id is already bound to another predicate or backup",
                ));
            }
            let receipt = ExcisionReceipt {
                database_id: database_id.into(),
                excision_id: spec.excision_id.clone(),
                basis_t,
                removed_datoms: positive_or_zero(row.get(6), "removed datoms")?,
                old_head_hash: digest(row.get(7), "old head hash")?,
                new_head_hash: digest(row.get(8), "new head hash")?,
                generation: positive_or_zero(row.get(9), "generation")?,
                replayed: true,
            };
            transaction
                .commit()
                .map_err(|error| operation_error("excision/idempotency-commit", error))?;
            return Ok(receipt);
        }

        if backup.point.database_id != database_id
            || backup.point.basis_t != basis_t
            || backup.database.basis_t() != basis_t
        {
            return Err(SemanticError::incorrect(
                "excision/backup-does-not-cover-head",
                "verified backup must belong to this database and exactly cover the locked head",
            ));
        }
        let recovered =
            crate::postgres::recover_to(&mut transaction, database_id, basis_t, old_head_hash)?
                .database;
        if !same_database_information(&recovered, &backup.database) {
            return Err(SemanticError::new(
                crate::ErrorCategory::Fault,
                "excision/backup-state-mismatch",
                "verified backup does not reproduce the database being excised",
            ));
        }
        validate_excision_target(&recovered, &spec.target)?;
        let extent = component_extent(&recovered, &spec.target)?;
        let rows = transaction
            .query(
                "SELECT basis_t, payload FROM atomic_transactions \
                 WHERE database_id = $1 ORDER BY basis_t",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/read-chain", error))?;
        let mut rewritten = Vec::with_capacity(rows.len());
        let genesis_hash = sha256(&encode_genesis(recovered.genesis_datoms())?);
        let mut rewritten_database = Database::from_genesis(recovered.genesis_datoms().to_vec())?;
        let mut previous = genesis_hash;
        let mut removed_datoms = 0_u64;
        let tx_instant_attribute = recovered
            .schema()
            .resolve_ident(&crate::Keyword::new("db", "txInstant"))
            .expect("Database construction requires :db/txInstant");
        for row in rows {
            let row_basis = positive_or_zero(row.get(0), "transaction basis")?;
            let payload: Vec<u8> = row.get(1);
            let mut envelope = decode_transaction(&payload)?;
            let before = envelope.tx_data.len();
            envelope.tx_data.retain(|datom| {
                !excision_matches(datom, &spec.target, &extent, before_t, tx_instant_attribute)
            });
            removed_datoms += (before - envelope.tx_data.len()) as u64;
            envelope.previous_hash = previous;
            let payload = encode_transaction(&envelope)?;
            let hash = transaction_hash(&payload);
            rewritten_database = rewritten_database.apply_committed(&envelope)?;
            let state_hash = checkpoint_state_hash(&rewritten_database)?;
            rewritten.push((row_basis, previous, hash, payload, state_hash));
            previous = hash;
        }
        let new_head_hash = if basis_t == 0 { genesis_hash } else { previous };

        transaction
            .batch_execute(
                "LOCK TABLE atomic_index_publications IN SHARE ROW EXCLUSIVE MODE; \
                 LOCK TABLE atomic_index_manifests IN SHARE ROW EXCLUSIVE MODE; \
                 SET LOCAL session_replication_role = replica",
            )
            .map_err(|error| operation_error("excision/privileged-mode", error))?;
        transaction
            .execute(
                "DELETE FROM atomic_index_publications WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/invalidate-index-publications", error))?;
        transaction
            .execute(
                "DELETE FROM atomic_index_manifests WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/invalidate-indexes", error))?;
        for (row_basis, predecessor, hash, payload, state_hash) in &rewritten {
            let row_basis = sql_u64(*row_basis, "transaction basis")?;
            transaction
                .execute(
                    "UPDATE atomic_transactions \
                     SET previous_hash = $1, tx_hash = $2, payload = $3, state_hash = $4 \
                     WHERE database_id = $5 AND basis_t = $6",
                    &[
                        &&predecessor[..],
                        &&hash[..],
                        &payload,
                        &&state_hash[..],
                        &database_id,
                        &row_basis,
                    ],
                )
                .map_err(|error| operation_error("excision/rewrite-transaction", error))?;
            transaction
                .execute(
                    "UPDATE atomic_requests SET tx_hash = $1 \
                     WHERE database_id = $2 AND basis_t = $3",
                    &[&&hash[..], &database_id, &row_basis],
                )
                .map_err(|error| operation_error("excision/rewrite-request", error))?;
        }
        transaction
            .execute(
                "UPDATE atomic_heads SET tx_hash = $1 WHERE database_id = $2",
                &[&&new_head_hash[..], &database_id],
            )
            .map_err(|error| operation_error("excision/rewrite-head", error))?;
        if fault_point == ExcisionFault::AfterRewrite {
            return Err(SemanticError::new(
                crate::ErrorCategory::Interrupted,
                "excision/injected-fault",
                "injected failure after history rewrite",
            ));
        }
        let generation: i64 = transaction
            .query_one(
                "UPDATE atomic_database_generations \
                 SET excision_generation = excision_generation + 1 \
                 WHERE database_id = $1 RETURNING excision_generation",
                &[&database_id],
            )
            .map_err(|error| operation_error("excision/generation", error))?
            .get(0);
        transaction
            .execute(
                "INSERT INTO atomic_excisions \
                 (database_id, excision_id, backup_basis_t, backup_manifest_hash, \
                  target_kind, target_id, attribute_ids, before_t, removed_datoms, \
                  old_head_hash, new_head_hash, generation) \
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12)",
                &[
                    &database_id,
                    &spec.excision_id,
                    &sql_u64(backup.point.basis_t, "backup basis")?,
                    &&backup.point.manifest_hash[..],
                    &target_kind,
                    &target_id_sql,
                    &attribute_ids,
                    &before_t_sql,
                    &sql_u64(removed_datoms, "removed datoms")?,
                    &&old_head_hash[..],
                    &&new_head_hash[..],
                    &generation,
                ],
            )
            .map_err(|error| operation_error("excision/audit", error))?;
        transaction
            .batch_execute("SET LOCAL session_replication_role = origin")
            .map_err(|error| operation_error("excision/restore-triggers", error))?;
        crate::postgres::recover_to(&mut transaction, database_id, basis_t, new_head_hash)?;
        transaction
            .commit()
            .map_err(|error| operation_error("excision/commit", error))?;
        Ok(ExcisionReceipt {
            database_id: database_id.into(),
            excision_id: spec.excision_id.clone(),
            basis_t,
            removed_datoms,
            old_head_hash,
            new_head_hash,
            generation: positive_or_zero(generation, "generation")?,
            replayed: false,
        })
    }
}

fn validate_excision_spec(spec: &ExcisionSpec) -> Result<(), SemanticError> {
    if spec.excision_id.is_empty() {
        return Err(SemanticError::incorrect(
            "excision/empty-id",
            "excision id cannot be empty",
        ));
    }
    Ok(())
}

fn normalized_target(target: &ExcisionTarget) -> Result<(i16, u64, Vec<u32>), SemanticError> {
    match target {
        ExcisionTarget::Entity { entity, attributes } => {
            let mut attributes = attributes.clone();
            attributes.sort_unstable();
            attributes.dedup();
            Ok((0, *entity, attributes))
        }
        ExcisionTarget::Attribute(attribute) => Ok((1, u64::from(*attribute), Vec::new())),
    }
}

fn validate_excision_target(
    database: &Database,
    target: &ExcisionTarget,
) -> Result<(), SemanticError> {
    let tx_instant = database
        .schema()
        .resolve_ident(&crate::Keyword::new("db", "txInstant"))
        .expect("Database construction requires :db/txInstant");
    match target {
        ExcisionTarget::Entity { entity, attributes } => {
            let partition = eid_to_part(*entity)?;
            if partition == DB_PARTITION {
                return Err(SemanticError::new(
                    crate::ErrorCategory::Forbidden,
                    "excision/protected-entity",
                    "entities in :db.part/db cannot be excised",
                ));
            }
            for attribute in attributes {
                database.schema().attribute(*attribute)?;
                if *attribute == tx_instant {
                    return Err(protected_attribute());
                }
            }
        }
        ExcisionTarget::Attribute(attribute) => {
            database.schema().attribute(*attribute)?;
            if *attribute == tx_instant {
                return Err(protected_attribute());
            }
        }
    }
    Ok(())
}

fn protected_attribute() -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Forbidden,
        "excision/protected-attribute",
        ":db/txInstant and operational audit facts cannot be excised",
    )
}

fn component_extent(
    database: &Database,
    target: &ExcisionTarget,
) -> Result<BTreeSet<u64>, SemanticError> {
    let ExcisionTarget::Entity { entity, attributes } = target else {
        return Ok(BTreeSet::new());
    };
    let mut extent = BTreeSet::from([*entity]);
    let mut pending = vec![(*entity, Some(attributes.as_slice()))];
    let history = database.datoms(View::History, crate::IndexOrder::Eavt);
    while let Some((owner, first_attributes)) = pending.pop() {
        for datom in history.iter().filter(|datom| datom.entity == owner) {
            if first_attributes
                .is_some_and(|attrs| !attrs.is_empty() && !attrs.contains(&datom.attribute))
            {
                continue;
            }
            let Ok(attribute) = database.schema().attribute(datom.attribute) else {
                continue;
            };
            let Value::Ref(child) = &datom.value else {
                continue;
            };
            if attribute.component && extent.insert(*child) {
                pending.push((*child, None));
            }
        }
    }
    Ok(extent)
}

fn excision_matches(
    datom: &crate::Datom,
    target: &ExcisionTarget,
    extent: &BTreeSet<u64>,
    before_t: Option<u64>,
    tx_instant_attribute: u32,
) -> bool {
    if eid_to_part(datom.entity).is_ok_and(|partition| partition == DB_PARTITION)
        || datom.attribute == tx_instant_attribute
        || before_t.is_some_and(|cutoff| tx_to_t(datom.tx).is_ok_and(|t| t >= cutoff))
    {
        return false;
    }
    match target {
        ExcisionTarget::Attribute(attribute) => datom.attribute == *attribute,
        ExcisionTarget::Entity { entity, attributes } => {
            let references =
                |candidate| matches!(datom.value, Value::Ref(value) if value == candidate);
            let root = (datom.entity == *entity || references(*entity))
                && (attributes.is_empty() || attributes.contains(&datom.attribute));
            let component = extent.iter().any(|candidate| {
                *candidate != *entity && (datom.entity == *candidate || references(*candidate))
            });
            root || component
        }
    }
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

    for publication in publications {
        let revision = positive_or_zero(publication.get(0), "tree publication revision")?;
        let basis = positive_or_zero(publication.get(1), "tree publication basis")?;
        let published_tx = digest(publication.get(2), "tree publication transaction hash")?;
        let manifest_hash = digest(publication.get(3), "tree publication manifest hash")?;
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
        for tree in &manifest.trees {
            match add_reachable_tree_nodes(client, tree.descriptor.root_hash, &mut all_nodes) {
                Ok(()) => {
                    if deep
                        && let Err(error) = validate_tree(
                            &tree.descriptor,
                            &TreeNodeSet::from_nodes(all_nodes.clone()),
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
        // A prior excision generation remains retained physical history, but
        // it is no longer a usable index for the current database value.
        if publication_valid && stored_generation == generation {
            metrics.index_basis_t = metrics.index_basis_t.max(basis);
        }
    }
    metrics.tree_nodes = all_nodes.len() as u64;
    metrics.tree_node_bytes = all_nodes.values().map(|bytes| bytes.len() as u64).sum();
    Ok(())
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
    // First authenticate every published value, including roots that might be
    // retired below. A corrupt candidate is not permission to erase evidence;
    // uncertainty anywhere in the shared content-addressed store fails closed.
    global_derived_reachability(client, &BTreeSet::new())?.ok_or_else(|| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/reachability-uncertain",
            "a published derived root is corrupt or incomplete; refusing to declare shared content collectible",
        )
    })?;

    let mut tree_publications = Vec::new();
    let mut excluded_publications = BTreeSet::new();
    // `lead(published_at)` is the instant this physical value became garbage.
    // Using the old root's own timestamp would violate the root-last source
    // protocol by immediately reclaiming a long-lived root after a new build.
    for row in client
        .query(
            "SELECT database_id, publication_revision, manifest_hash \
               FROM (SELECT database_id, publication_revision, manifest_hash, \
                            lead(published_at) OVER ( \
                              PARTITION BY database_id \
                              ORDER BY publication_revision) AS retired_at \
                       FROM atomic_tree_publications) retired \
              WHERE retired_at < clock_timestamp() - \
                                 $1::bigint * interval '1 millisecond' \
              ORDER BY database_id, publication_revision",
            &[&older_than_millis],
        )
        .map_err(|error| operation_error("operations/gc-tree-publications", error))?
    {
        let publication = TreePublicationGarbage {
            database_id: row.get(0),
            publication_revision: positive_or_zero(row.get(1), "tree publication revision")?,
            manifest_hash: digest(row.get(2), "tree publication manifest hash")?,
        };
        // A live immutable PeerState holds the matching session-level shared
        // lock. Advisory-key collisions only make this return false, retaining
        // extra data conservatively.
        if try_lock_tree_manifest_for_gc(client, publication.manifest_hash)? {
            excluded_publications.insert(publication.manifest_hash);
            tree_publications.push(publication);
        }
    }

    let mut tree_manifests = excluded_publications.clone();
    // Content-first publication normally leaves nodes, not manifest rows, on
    // interruption because manifest/root/publication share one transaction.
    // Direct operational repair may nevertheless leave an unpublished
    // manifest. Once its own grace has elapsed it is equally replaceable.
    for row in client
        .query(
            "SELECT m.manifest_hash FROM atomic_tree_manifests m \
              WHERE m.created_at < clock_timestamp() - \
                                   $1::bigint * interval '1 millisecond' \
                AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications p \
                                WHERE p.manifest_hash = m.manifest_hash) \
              ORDER BY m.manifest_hash",
            &[&older_than_millis],
        )
        .map_err(|error| operation_error("operations/gc-tree-manifests", error))?
    {
        let hash = digest(row.get(0), "unpublished tree manifest hash")?;
        if try_lock_tree_manifest_for_gc(client, hash)? {
            tree_manifests.insert(hash);
        }
    }

    let mut reachable = global_derived_reachability(client, &excluded_publications)?.ok_or_else(|| {
        SemanticError::new(
            crate::ErrorCategory::Fault,
            "operations/reachability-uncertain",
            "a retained derived root is corrupt or incomplete; refusing to declare shared content collectible",
        )
    })?;
    // Preserve a young unpublished manifest as a resumable content-first
    // build. It is not peer-visible, but deleting an interior node underneath
    // it would turn an otherwise safe retry into latent corruption.
    let mut unpublished_nodes = BTreeMap::new();
    for row in client
        .query(
            "SELECT m.manifest_hash, r.root_hash \
               FROM atomic_tree_manifests m \
               JOIN atomic_tree_manifest_roots r \
                 ON r.manifest_hash = m.manifest_hash \
               LEFT JOIN atomic_tree_publications p \
                 ON p.manifest_hash = m.manifest_hash \
              WHERE p.manifest_hash IS NULL \
              ORDER BY m.manifest_hash, r.index_order, r.history",
            &[],
        )
        .map_err(|error| operation_error("operations/gc-unpublished-tree-roots", error))?
    {
        let manifest_hash = digest(row.get(0), "unpublished manifest hash")?;
        if tree_manifests.contains(&manifest_hash) {
            continue;
        }
        let root_hash = digest(row.get(1), "unpublished tree root hash")?;
        add_reachable_tree_nodes(client, root_hash, &mut unpublished_nodes).map_err(|error| {
            SemanticError::new(
                crate::ErrorCategory::Fault,
                "operations/reachability-uncertain",
                format!(
                    "a retained unpublished tree is incomplete: {}",
                    error.message
                ),
            )
        })?;
    }
    reachable
        .tree_nodes
        .extend(unpublished_nodes.keys().copied());

    let segment_rows = client
        .query(
            "SELECT segment_hash FROM atomic_index_segments \
             WHERE created_at < clock_timestamp() - \
                                $1::bigint * interval '1 millisecond' \
             ORDER BY segment_hash",
            &[&older_than_millis],
        )
        .map_err(|error| operation_error("operations/gc-segments", error))?;
    let segments = segment_rows
        .into_iter()
        .map(|row| digest(row.get(0), "segment hash"))
        .collect::<Result<Vec<_>, _>>()?
        .into_iter()
        .filter(|hash| !reachable.legacy_segments.contains(hash))
        .collect();
    let temporal_programs = temporal_program_references(client)?;
    let program_rows = client
        .query(
            "SELECT p.program_hash FROM atomic_programs p \
             WHERE p.created_at < clock_timestamp() - \
                                  $1::bigint * interval '1 millisecond' \
               AND NOT EXISTS (SELECT 1 FROM atomic_program_versions v \
                               WHERE v.program_hash = p.program_hash) \
             ORDER BY p.program_hash",
            &[&older_than_millis],
        )
        .map_err(|error| operation_error("operations/gc-programs", error))?;
    let programs = program_rows
        .into_iter()
        .map(|row| digest(row.get(0), "program hash"))
        .collect::<Result<Vec<_>, _>>()?
        .into_iter()
        .filter(|hash| !temporal_programs.contains(hash))
        .collect();
    let tree_nodes = client
        .query(
            "SELECT node_hash FROM atomic_tree_nodes \
              WHERE created_at < clock_timestamp() - \
                                 $1::bigint * interval '1 millisecond' \
              ORDER BY node_hash",
            &[&older_than_millis],
        )
        .map_err(|error| operation_error("operations/gc-tree-nodes", error))?
        .into_iter()
        .map(|row| digest(row.get(0), "tree node hash"))
        .collect::<Result<Vec<_>, _>>()?
        .into_iter()
        .filter(|hash| !reachable.tree_nodes.contains(hash))
        .collect();
    Ok(GarbageCandidates {
        segments,
        programs,
        tree_publications,
        tree_manifests: tree_manifests.into_iter().collect(),
        tree_nodes,
    })
}

fn lock_gc_inputs<C: postgres::GenericClient>(client: &mut C) -> Result<(), SemanticError> {
    // One order for inventory and apply serializes collectors and blocks every
    // content/root writer while candidate reachability is computed. Ordinary
    // peer reads remain ACCESS SHARE and are protected by root pins instead of
    // a database-wide read outage.
    client
        .batch_execute(
            "LOCK TABLE atomic_transactions IN SHARE MODE; \
             LOCK TABLE atomic_programs IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_program_versions IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_index_segments IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_index_manifests IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_index_publications IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_tree_nodes IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_tree_manifests IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_tree_manifest_roots IN SHARE ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_tree_publications IN SHARE ROW EXCLUSIVE MODE",
        )
        .map_err(|error| operation_error("operations/gc-lock", error))
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

fn require_one_gc_delete(deleted: u64, label: &str) -> Result<(), SemanticError> {
    if deleted == 1 {
        return Ok(());
    }
    Err(SemanticError::new(
        crate::ErrorCategory::Fault,
        "operations/gc-candidate-changed",
        format!("locked {label} candidate disappeared before deletion"),
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

fn temporal_program_references<C: postgres::GenericClient>(
    client: &mut C,
) -> Result<BTreeSet<Digest>, SemanticError> {
    let mut referenced = BTreeSet::new();
    for row in client
        .query("SELECT genesis FROM atomic_databases", &[])
        .map_err(|error| operation_error("operations/gc-genesis", error))?
    {
        let payload: Vec<u8> = row.get(0);
        for datom in decode_genesis(&payload)? {
            collect_function_hashes(&datom.value, &mut referenced);
        }
    }
    for row in client
        .query("SELECT payload FROM atomic_transactions", &[])
        .map_err(|error| operation_error("operations/gc-transactions", error))?
    {
        let payload: Vec<u8> = row.get(0);
        for datom in decode_transaction(&payload)?.tx_data {
            collect_function_hashes(&datom.value, &mut referenced);
        }
    }
    Ok(referenced)
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
    if duration < MIN_GARBAGE_COLLECTION_AGE {
        return Err(SemanticError::incorrect(
            "operations/gc-boundary-too-recent",
            "garbage collection requires an age boundary of at least 30 days",
        ));
    }
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
