use crate::state_commitment::checkpoint_state_hash;
use crate::{
    BackupVerification, DB_PARTITION, Database, Digest, IndexManifest, MAX_EIDX, PostgresStore,
    SemanticError, Value, View, decode_genesis, decode_index_manifest, decode_index_segment,
    decode_transaction, eid_to_part, encode_genesis, encode_transaction, sha256, transaction_hash,
    tx_to_t,
};
use postgres::{Client, NoTls};
use std::collections::BTreeSet;
use std::time::Duration;

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
    pub applied: bool,
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
    connection: String,
    client: Client,
}

impl PostgresOperator {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        let client = Client::connect(connection, NoTls)
            .map_err(|error| operation_error("operations/connect", error))?;
        Ok(Self {
            connection: connection.into(),
            client,
        })
    }

    pub fn inspect_database(
        &mut self,
        database_id: &str,
        deep_derived: bool,
    ) -> Result<IntegrityReport, SemanticError> {
        let head = self
            .client
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
        let catalog = self
            .client
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
        let rows = self
            .client
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
        let dangling: i64 = self
            .client
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
            &mut self.client,
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

        let manifests = self
            .client
            .query(
                "SELECT basis_t, tx_hash, manifest_hash, payload \
                 FROM atomic_index_manifests WHERE database_id = $1 ORDER BY basis_t",
                &[&database_id],
            )
            .map_err(|error| operation_error("operations/manifests", error))?;
        metrics.manifests = manifests.len() as u64;
        let mut referenced_segments = BTreeSet::new();
        for row in manifests {
            let manifest_basis = positive_or_zero(row.get(0), "manifest basis")?;
            let tx_hash = digest(row.get(1), "manifest transaction hash")?;
            let manifest_hash = digest(row.get(2), "manifest hash")?;
            let payload: Vec<u8> = row.get(3);
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
                    validate_manifest_row(
                        &mut problems,
                        database_id,
                        manifest_basis,
                        tx_hash,
                        &manifest,
                    );
                    referenced_segments.extend(manifest.segments.iter().map(|item| item.hash));
                    metrics.index_basis_t = metrics.index_basis_t.max(manifest_basis);
                }
                Err(error) => problem(&mut problems, error.code, error.message),
            }
        }
        metrics.index_lag = basis.saturating_sub(metrics.index_basis_t);
        for hash in &referenced_segments {
            let row = self
                .client
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
        let segment_row = self
            .client
            .query_one(
                "SELECT count(*), COALESCE(sum(octet_length(payload)), 0), \
                        count(*) FILTER (WHERE NOT EXISTS \
                          (SELECT 1 FROM atomic_index_manifests m \
                           WHERE position(encode(s.segment_hash, 'hex') in \
                                          encode(m.payload, 'hex')) > 0)) \
                 FROM atomic_index_segments s",
                &[],
            )
            .map_err(|error| operation_error("operations/segment-metrics", error))?;
        metrics.segments = positive_or_zero(segment_row.get(0), "segment count")?;
        metrics.segment_bytes = positive_or_zero(segment_row.get(1), "segment bytes")?;
        // Precise reachability is decoded above for this database. Global GC
        // computes it across all manifests; this SQL figure is informational.
        metrics.orphan_segments = positive_or_zero(segment_row.get(2), "orphan segments")?;
        metrics.programs = count_global(&mut self.client, "SELECT count(*) FROM atomic_programs")?;
        // Inspection must report corrupt durable values rather than aborting
        // before it can return an integrity report.  GC deliberately keeps
        // using the strict scanner below because deletion is unsafe when any
        // temporal reference is unreadable.
        let temporal_programs =
            inspect_temporal_program_references(&mut self.client, database_id, &mut problems)?;
        let unversioned_programs = self
            .client
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
        metrics.active_leases = count_global(
            &mut self.client,
            "SELECT count(*) FROM atomic_transactor_leases \
             WHERE expires_at > clock_timestamp()",
        )?;

        match PostgresStore::connect(&self.connection)
            .and_then(|mut store| store.recover(database_id))
        {
            Ok(database) => {
                metrics.current_datoms = database
                    .datoms(crate::View::Current, crate::IndexOrder::Eavt)
                    .len() as u64;
                metrics.history_datoms = database
                    .datoms(crate::View::History, crate::IndexOrder::Eavt)
                    .len() as u64;
            }
            Err(error) => problem(&mut problems, error.code, error.message),
        }
        Ok(IntegrityReport {
            database_id: database_id.into(),
            metrics,
            problems,
        })
    }

    pub fn garbage_inventory(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        let millis = duration_millis(older_than)?;
        garbage_candidates(&mut self.client, millis).map(|(segments, programs)| GarbageInventory {
            segment_hashes: segments,
            program_hashes: programs,
            applied: false,
        })
    }

    pub fn collect_garbage(
        &mut self,
        older_than: Duration,
    ) -> Result<GarbageInventory, SemanticError> {
        let millis = duration_millis(older_than)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| operation_error("operations/gc-begin", error))?;
        transaction
            .batch_execute(
                "LOCK TABLE atomic_index_manifests IN SHARE MODE; \
                 LOCK TABLE atomic_program_versions IN SHARE MODE; \
                 LOCK TABLE atomic_transactions IN SHARE MODE",
            )
            .map_err(|error| operation_error("operations/gc-lock", error))?;
        let (segments, programs) = garbage_candidates(&mut transaction, millis)?;
        transaction
            .batch_execute("SET LOCAL session_replication_role = replica")
            .map_err(|error| operation_error("operations/gc-privilege", error))?;
        for hash in &segments {
            transaction
                .execute(
                    "DELETE FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/gc-segment", error))?;
        }
        for hash in &programs {
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
        Ok(GarbageInventory {
            segment_hashes: segments,
            program_hashes: programs,
            applied: true,
        })
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
        delete_unreferenced_segments(&mut transaction)?;
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
) -> Result<(Vec<Digest>, Vec<Digest>), SemanticError> {
    let manifest_rows = client
        .query(
            "SELECT m.manifest_hash, m.payload \
             FROM atomic_index_manifests m \
             JOIN atomic_index_publications p \
               ON p.database_id = m.database_id \
              AND p.basis_t = m.basis_t \
              AND p.tx_hash = m.tx_hash \
              AND p.manifest_hash = m.manifest_hash",
            &[],
        )
        .map_err(|error| operation_error("operations/gc-manifests", error))?;
    let mut referenced = BTreeSet::new();
    for row in manifest_rows {
        let hash = digest(row.get(0), "published manifest hash")?;
        let payload: Vec<u8> = row.get(1);
        if sha256(&payload) == hash
            && let Ok(manifest) = decode_index_manifest(&payload)
        {
            referenced.extend(manifest.segments.into_iter().map(|segment| segment.hash));
        }
    }
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
        .filter(|hash| !referenced.contains(hash))
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
    Ok((segments, programs))
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

fn delete_unreferenced_segments<C: postgres::GenericClient>(
    client: &mut C,
) -> Result<(), SemanticError> {
    let rows = client
        .query(
            "SELECT m.manifest_hash, m.payload \
             FROM atomic_index_manifests m \
             JOIN atomic_index_publications p \
               ON p.database_id = m.database_id \
              AND p.basis_t = m.basis_t \
              AND p.tx_hash = m.tx_hash \
              AND p.manifest_hash = m.manifest_hash",
            &[],
        )
        .map_err(|error| operation_error("excision/remaining-manifests", error))?;
    let mut reachable = BTreeSet::new();
    for row in rows {
        let hash = digest(row.get(0), "published manifest hash")?;
        let payload: Vec<u8> = row.get(1);
        if sha256(&payload) == hash
            && let Ok(manifest) = decode_index_manifest(&payload)
        {
            reachable.extend(manifest.segments.into_iter().map(|segment| segment.hash));
        }
    }
    let rows = client
        .query("SELECT segment_hash FROM atomic_index_segments", &[])
        .map_err(|error| operation_error("excision/segments", error))?;
    for row in rows {
        let hash = digest(row.get(0), "segment hash")?;
        if !reachable.contains(&hash) {
            client
                .execute(
                    "DELETE FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("excision/delete-segment", error))?;
        }
    }
    Ok(())
}

fn duration_millis(duration: Duration) -> Result<i64, SemanticError> {
    i64::try_from(duration.as_millis()).map_err(|_| {
        SemanticError::incorrect("operations/duration-overflow", "duration is too large")
    })
}

fn validate_manifest_row(
    problems: &mut Vec<IntegrityProblem>,
    database_id: &str,
    basis: u64,
    tx_hash: Digest,
    manifest: &IndexManifest,
) {
    if manifest.database_id != database_id
        || manifest.basis_t != basis
        || manifest.tx_hash != tx_hash
    {
        problem(
            problems,
            "integrity/manifest-envelope-mismatch",
            format!("manifest at basis {basis} does not match its row"),
        );
    }
}

fn count(client: &mut Client, sql: &str, database_id: &str) -> Result<u64, SemanticError> {
    let value: i64 = client
        .query_one(sql, &[&database_id])
        .map_err(|error| operation_error("operations/count", error))?
        .get(0);
    positive_or_zero(value, "count")
}

fn count_global(client: &mut Client, sql: &str) -> Result<u64, SemanticError> {
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
