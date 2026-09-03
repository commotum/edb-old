//! Source-shaped differential, root-last backup for one PostgreSQL database.
//!
//! Canonical genesis, log, request identities, state commitments, and program
//! definitions are the authority. When an authenticated native persistent
//! tree is available at or before the captured basis, its complete immutable
//! node closure is copied as a recovery accelerator and rebound to the target
//! timeline on restore. A missing tree is valid and restores by replaying the
//! authoritative log; physical indexes never become backup authority.
//!
//! Live generation-zero envelopes still contain PostgreSQL catalog names.
//! Backup canonicalizes those carriers onto the database's immutable random
//! lineage, so rename does not recopy history or change a point root. Mutable
//! named program aliases are operator configuration and are not database
//! information; all program blobs reachable from temporal `:db/fn` history
//! are included instead.

use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Database, Digest, DurableTransaction, ErrorCategory, PostgresConnectionConfig, PostgresStore,
    PostgresTreeStore, Program, SemanticError, TreeManifestRecord, TreeRootBinding, decode_genesis,
    decode_program, decode_transaction, encode_genesis, encode_transaction, sha256,
    transaction_hash,
};
use crate::{PersistentTreeManifest, persistent_tree};
use postgres::{Client, IsolationLevel};
use std::collections::btree_map::Entry;
use std::collections::{BTreeMap, BTreeSet};
use std::fs::{self, File, OpenOptions};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicU64, Ordering};

const MAGIC: &[u8; 4] = b"ATBK";
const VERSION: u16 = 3;
const CLAIM_MAGIC: &[u8; 4] = b"ATCL";
const CLAIM_VERSION: u16 = 1;
static TEMP_SEQUENCE: AtomicU64 = AtomicU64::new(0);

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackupPoint {
    /// Immutable database identity. Human catalog names are locators and are
    /// deliberately absent from the canonical backup root.
    pub lineage_id: String,
    pub basis_t: u64,
    pub manifest_hash: Digest,
    pub objects_written: usize,
    pub objects_reused: usize,
}

#[derive(Clone, Debug)]
pub struct BackupVerification {
    pub point: BackupPoint,
    pub database: Database,
    pub objects_read: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct RequestRow {
    key: String,
    digest: Digest,
    basis: u64,
    tx_hash: Digest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct ProgramRow {
    hash: Digest,
    kind: i16,
    arity: i16,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct Manifest {
    lineage_id: String,
    basis: u64,
    genesis_hash: Digest,
    transactions: Vec<Digest>,
    state_hashes: Vec<Digest>,
    requests: Vec<RequestRow>,
    programs: Vec<ProgramRow>,
    tree: Option<TreeBackup>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct TreeBackup {
    manifest_hash: Digest,
    node_hashes: Vec<Digest>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct BackupClaim {
    lineage_id: String,
    genesis_hash: Digest,
}

/// Deterministic interruption points used to prove root-last publication.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
#[doc(hidden)]
pub enum BackupFault {
    #[default]
    None,
    AfterFirstObjectStaged,
    AfterObjects,
    AfterManifestStaged,
    AfterManifestPublished,
}

/// Deterministic interruption points used to prove atomic restore/retry.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
#[doc(hidden)]
pub enum RestoreFault {
    #[default]
    None,
    BeforeCommit,
    AfterCommitBeforeResponse,
}

pub struct PortableBackup {
    connection: PostgresConnectionConfig,
    client: Client,
}

impl PortableBackup {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        // Backup is a runtime operation, not a provisioning path. Reuse the
        // normal compatibility gate and then open its long-lived snapshot
        // session with the identical transport policy.
        drop(PostgresStore::connect_configured(connection)?);
        let client = connection.connect()?;
        Ok(Self {
            connection: connection.clone(),
            client,
        })
    }

    pub fn backup_database(
        &mut self,
        database_id: &str,
        directory: &Path,
    ) -> Result<BackupPoint, SemanticError> {
        self.backup_database_with_fault(database_id, directory, BackupFault::None)
    }

    #[doc(hidden)]
    pub fn backup_database_with_fault(
        &mut self,
        database_id: &str,
        directory: &Path,
        fault_at: BackupFault,
    ) -> Result<BackupPoint, SemanticError> {
        prepare_directory(directory)?;
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .read_only(true)
            .start()
            .map_err(|error| crate::postgres::postgres_error("backup/snapshot", error))?;
        let catalog = transaction
            .query_opt(
                "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases \
                 WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/catalog", error))?
            .ok_or_else(|| not_found(database_id))?;
        let lineage_id: String = catalog.get(0);
        if !valid_lineage_id(&lineage_id) {
            return Err(fault(
                "backup/lineage-id",
                "database lineage id is not a canonical UUID",
            ));
        }
        let genesis: Vec<u8> = catalog.get(1);
        let genesis_hash = digest(catalog.get(2), "genesis hash")?;
        let decoded_genesis = decode_genesis(&genesis)?;
        if sha256(&genesis) != genesis_hash || encode_genesis(&decoded_genesis)? != genesis {
            return Err(fault(
                "backup/genesis-corrupt",
                "genesis is not canonical or hash-valid",
            ));
        }
        ensure_claim(
            directory,
            &BackupClaim {
                lineage_id: lineage_id.clone(),
                genesis_hash,
            },
        )?;
        let mut temporal_programs = BTreeSet::new();
        for datom in &decoded_genesis {
            collect_function_hashes(&datom.value, &mut temporal_programs);
        }
        let mut reconstructed = Database::from_genesis(decoded_genesis)?;
        let head = transaction
            .query_one(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/head", error))?;
        let basis = unsigned(head.get(0), "head basis")?;
        let head_hash = digest(head.get(1), "head hash")?;
        let tx_rows = transaction
            .query(
                "SELECT basis_t, tx_hash, payload, state_hash FROM atomic_transactions \
                 WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
                &[&database_id, &(basis as i64)],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/transactions", error))?;
        if tx_rows.len() != basis as usize {
            return Err(fault(
                "backup/incomplete-chain",
                "snapshot transaction chain is incomplete",
            ));
        }
        let mut publisher = ObjectPublisher::new(directory, fault_at);
        publisher.publish(genesis_hash, &genesis)?;
        let mut source_transaction_hashes = Vec::with_capacity(tx_rows.len());
        let mut transactions = Vec::with_capacity(tx_rows.len());
        let mut portable_frontiers = Vec::with_capacity(tx_rows.len());
        let mut state_hashes = Vec::with_capacity(tx_rows.len());
        let mut source_previous = genesis_hash;
        let mut portable_previous = genesis_hash;
        for (offset, row) in tx_rows.into_iter().enumerate() {
            let row_basis = unsigned(row.get(0), "transaction basis")?;
            let hash = digest(row.get(1), "transaction hash")?;
            let payload: Vec<u8> = row.get(2);
            let state_hash = digest(row.get(3), "transaction state hash")?;
            let decoded = decode_transaction(&payload)?;
            for datom in &decoded.tx_data {
                collect_function_hashes(&datom.value, &mut temporal_programs);
            }
            if row_basis != offset as u64 + 1
                || decoded.database_id != database_id
                || decoded.basis_t != row_basis
                || decoded.previous_hash != source_previous
                || transaction_hash(&payload) != hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    format!("invalid transaction {row_basis}"),
                ));
            }
            // PostgreSQL generations predating the lineage format bind their
            // durable envelope to a mutable catalog name. The backup object
            // graph canonicalizes only that carrier: datoms, tempids,
            // frontier, and basis remain byte-for-byte semantic values, while
            // identity and predecessor links use the immutable lineage. Thus
            // a rename does not recopy the entire historical chain.
            let portable = DurableTransaction {
                database_id: lineage_id.clone(),
                basis_t: decoded.basis_t,
                previous_hash: portable_previous,
                eidx_frontier: decoded.eidx_frontier,
                tempids: decoded.tempids,
                tx_data: decoded.tx_data,
            };
            let portable_payload = encode_transaction(&portable)?;
            let portable_hash = transaction_hash(&portable_payload);
            reconstructed = reconstructed.apply_committed(&portable)?;
            if checkpoint_state_hash(&reconstructed)? != state_hash {
                return Err(fault(
                    "backup/state-commitment",
                    format!("transaction {row_basis} has an invalid state commitment"),
                ));
            }
            source_previous = hash;
            portable_previous = portable_hash;
            source_transaction_hashes.push(hash);
            transactions.push(portable_hash);
            portable_frontiers.push(portable.eidx_frontier);
            state_hashes.push(state_hash);
            publisher.publish(portable_hash, &portable_payload)?;
        }
        if head_hash != source_previous {
            return Err(fault(
                "backup/head-mismatch",
                "head does not match snapshot chain",
            ));
        }
        if reconstructed.basis_t() != basis {
            return Err(fault(
                "backup/basis-mismatch",
                "semantically reconstructed snapshot does not reach its observed head",
            ));
        }
        let request_rows = transaction
            .query(
                "SELECT request_key, request_digest, basis_t, tx_hash FROM atomic_requests \
                 WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
                &[&database_id, &(basis as i64)],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/requests", error))?;
        if request_rows.len() != transactions.len() {
            return Err(fault(
                "backup/incomplete-requests",
                "snapshot does not contain exactly one request identity per transaction",
            ));
        }
        let mut requests = Vec::with_capacity(request_rows.len());
        for (offset, row) in request_rows.into_iter().enumerate() {
            let request_basis = unsigned(row.get(2), "request basis")?;
            let source_hash = digest(row.get(3), "request transaction hash")?;
            if request_basis != offset as u64 + 1
                || source_transaction_hashes.get(offset) != Some(&source_hash)
            {
                return Err(fault(
                    "backup/request-mismatch",
                    "request identity does not name its authoritative source transaction",
                ));
            }
            requests.push(RequestRow {
                key: row.get(0),
                digest: digest(row.get(1), "request digest")?,
                basis: request_basis,
                tx_hash: transactions[offset],
            });
        }
        let mut program_rows = BTreeMap::new();
        // Database functions are temporal database information. Preserve
        // every content hash reachable from history, including superseded
        // bindings needed by as-of values. Mutable named deployment/version
        // aliases are operator configuration outside the immutable database
        // value and therefore cannot be part of a point identified only by t.
        let mut pending_programs = temporal_programs;
        while let Some(hash) = pending_programs.pop_first() {
            if program_rows.contains_key(&hash) {
                continue;
            }
            let row = transaction
                .query_opt(
                    "SELECT program_hash, kind, arity, payload FROM atomic_programs \
                     WHERE program_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| crate::postgres::postgres_error("backup/program", error))?
                .ok_or_else(|| {
                    fault(
                        "backup/missing-program",
                        format!(
                            "temporal :db/fn program graph refers to missing program {}",
                            hex(&hash)
                        ),
                    )
                })?;
            let (program, payload) = validated_program_row(&row)?;
            collect_program_dependencies(&decode_program(&payload)?, &mut pending_programs);
            publisher.publish(hash, &payload)?;
            program_rows.insert(hash, program);
        }
        let mut programs = Vec::with_capacity(program_rows.len());
        for (hash, program) in program_rows {
            debug_assert_eq!(hash, program.hash);
            programs.push(program);
        }
        let mut manifest = Manifest {
            lineage_id,
            basis,
            genesis_hash,
            transactions,
            state_hashes,
            requests,
            programs,
            tree: None,
        };

        // A backup point is a logical (lineage, t), not whichever replaceable
        // physical index revision happened to be current during this retry.
        // Goal 13 deliberately permits a repaired tree to be republished at
        // the same logical basis. If a root already exists, authenticate it
        // and prove the live snapshot has the same transaction information
        // before reusing it. This check precedes physical-tree capture so a
        // harmless repair does not even leave an unreferenced backup object.
        if let Some(point) = reusable_existing_point(directory, &manifest)? {
            transaction.commit().map_err(|error| {
                crate::postgres::postgres_error("backup/snapshot-commit", error)
            })?;
            return Ok(point);
        }
        let tree = capture_tree_backup(
            &mut transaction,
            database_id,
            &manifest.lineage_id,
            basis,
            &source_transaction_hashes,
            &manifest.transactions,
            &portable_frontiers,
            &manifest.state_hashes,
            &mut publisher,
        )?;
        manifest.tree = tree;
        transaction
            .commit()
            .map_err(|error| crate::postgres::postgres_error("backup/snapshot-commit", error))?;

        if fault_at == BackupFault::AfterObjects {
            return Err(injected("backup/after-objects"));
        }
        let encoded = encode_manifest(&manifest)?;
        let manifest_hash = sha256(&encoded);
        let snapshot_path = snapshots(directory).join(format!("{basis:020}.atbk"));
        let publication_fault = if fault_at == BackupFault::AfterManifestStaged {
            PublishFault::AfterStaged
        } else if fault_at == BackupFault::AfterManifestPublished {
            PublishFault::AfterPublished
        } else {
            PublishFault::None
        };
        if let Err(error) = publish_exact(&snapshot_path, &encoded, publication_fault) {
            // A concurrent backup may have won publication with another
            // valid physical revision for this same logical point. Resolve
            // that race by the same semantic proof used by an ordinary
            // retry, never by replacing its root.
            if error.code == "backup/file-conflict"
                && let Some(point) = reusable_existing_point(directory, &manifest)?
            {
                return Ok(point);
            }
            return Err(error);
        }
        Ok(BackupPoint {
            lineage_id: manifest.lineage_id.clone(),
            basis_t: basis,
            manifest_hash,
            objects_written: publisher.written,
            objects_reused: publisher.reused,
        })
    }

    pub fn list_backups(directory: &Path) -> Result<Vec<u64>, SemanticError> {
        let mut points = Vec::new();
        for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list"))? {
            let entry = entry.map_err(io_error("backup/list-entry"))?;
            if entry.path().extension().and_then(|value| value.to_str()) == Some("atbk") {
                let bytes = fs::read(entry.path()).map_err(io_error("backup/list-read"))?;
                let manifest = decode_manifest(&bytes)?;
                let expected_name = format!("{:020}.atbk", manifest.basis);
                if entry.file_name() != std::ffi::OsStr::new(&expected_name) {
                    return Err(fault(
                        "backup/root-name",
                        "snapshot filename does not identify its manifest basis",
                    ));
                }
                verify_claim(directory, &manifest)?;
                points.push(manifest.basis);
            }
        }
        points.sort_unstable();
        points.dedup();
        Ok(points)
    }

    /// Check the root descriptor, directory claim, and presence of every
    /// referenced immutable object without reading object payloads. This is
    /// the native equivalent of Datomic's `read-all=false` verification.
    pub fn verify_backup_presence(
        directory: &Path,
        basis: u64,
    ) -> Result<BackupPoint, SemanticError> {
        let (manifest, manifest_hash) = load_manifest(directory, basis)?;
        verify_claim(directory, &manifest)?;
        for hash in manifest_object_hashes(&manifest) {
            ensure_object_present(directory, hash)?;
        }
        Ok(BackupPoint {
            lineage_id: manifest.lineage_id,
            basis_t: manifest.basis,
            manifest_hash,
            objects_written: 0,
            objects_reused: 0,
        })
    }

    pub fn verify_backup(
        directory: &Path,
        basis: u64,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
        // Always prove reachability first. `deep` controls whether program
        // payloads are read; the authoritative genesis/log must be decoded in
        // either mode because this API returns a semantically verified value.
        // Call `verify_backup_presence` for a strictly metadata-only check.
        Self::verify_backup_presence(directory, basis)?;
        let (manifest, manifest_hash) = load_manifest(directory, basis)?;
        let genesis = read_object(directory, manifest.genesis_hash)?;
        let decoded_genesis = decode_genesis(&genesis)?;
        if encode_genesis(&decoded_genesis)? != genesis {
            return Err(fault(
                "backup/noncanonical-genesis",
                "backup genesis is not canonically encoded",
            ));
        }
        let mut required_programs = BTreeSet::new();
        for datom in &decoded_genesis {
            collect_function_hashes(&datom.value, &mut required_programs);
        }
        let mut database = Database::from_genesis(decoded_genesis)?;
        let mut previous = manifest.genesis_hash;
        let mut objects_read = 1;
        for (offset, hash) in manifest.transactions.iter().enumerate() {
            let payload = read_object(directory, *hash)?;
            objects_read += 1;
            let transaction = decode_transaction(&payload)?;
            for datom in &transaction.tx_data {
                collect_function_hashes(&datom.value, &mut required_programs);
            }
            if transaction.database_id != manifest.lineage_id
                || transaction.basis_t != offset as u64 + 1
                || transaction.previous_hash != previous
                || transaction_hash(&payload) != *hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    "backup transaction chain is invalid",
                ));
            }
            previous = *hash;
            database = database.apply_committed(&transaction)?;
            if checkpoint_state_hash(&database)? != manifest.state_hashes[offset] {
                return Err(fault(
                    "backup/state-commitment",
                    format!(
                        "backup transaction {} has the wrong state commitment",
                        offset + 1
                    ),
                ));
            }
        }
        if database.basis_t() != manifest.basis {
            return Err(fault(
                "backup/basis-mismatch",
                "backup reconstruction has wrong basis",
            ));
        }
        for (offset, request) in manifest.requests.iter().enumerate() {
            if request.basis != offset as u64 + 1
                || manifest.transactions.get(offset) != Some(&request.tx_hash)
            {
                return Err(fault(
                    "backup/request-mismatch",
                    "request does not name a backup transaction",
                ));
            }
        }
        let declared_programs: BTreeSet<_> = manifest
            .programs
            .iter()
            .map(|program| program.hash)
            .collect();
        if let Some(missing) = required_programs.difference(&declared_programs).next() {
            return Err(fault(
                "backup/missing-program",
                format!("backup manifest omits required program {}", hex(missing)),
            ));
        }
        if deep {
            for program in &manifest.programs {
                let bytes = read_object(directory, program.hash)?;
                objects_read += 1;
                let decoded = decode_program(&bytes)?;
                if program_kind(&decoded) != program.kind
                    || i16::from(decoded.arity) != program.arity
                {
                    return Err(fault(
                        "backup/program-metadata",
                        "program metadata mismatch",
                    ));
                }
                collect_program_dependencies(&decoded, &mut required_programs);
            }
            if let Some(missing) = required_programs.difference(&declared_programs).next() {
                return Err(fault(
                    "backup/missing-program",
                    format!("backup omits dependent program {}", hex(missing)),
                ));
            }
            if let Some(extra) = declared_programs.difference(&required_programs).next() {
                return Err(fault(
                    "backup/extra-program",
                    format!(
                        "backup manifest includes unreachable program {}",
                        hex(extra)
                    ),
                ));
            }
            if let Some(tree) = &manifest.tree {
                objects_read += verify_tree_backup(directory, &manifest, tree)?;
            }
        }
        Ok(BackupVerification {
            point: BackupPoint {
                lineage_id: manifest.lineage_id.clone(),
                basis_t: manifest.basis,
                manifest_hash,
                objects_written: 0,
                objects_reused: 0,
            },
            database,
            objects_read,
        })
    }

    pub fn restore_backup(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
    ) -> Result<Database, SemanticError> {
        self.restore_backup_with_fault(directory, basis, target_database_id, RestoreFault::None)
    }

    #[doc(hidden)]
    pub fn restore_backup_with_fault(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
        fault_at: RestoreFault,
    ) -> Result<Database, SemanticError> {
        if target_database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "backup/empty-target",
                "restore target cannot be empty",
            ));
        }
        let verification = Self::verify_backup(directory, basis, true)?;
        let manifest_bytes = fs::read(snapshots(directory).join(format!("{basis:020}.atbk")))
            .map_err(io_error("backup/manifest-read"))?;
        if sha256(&manifest_bytes) != verification.point.manifest_hash {
            return Err(fault(
                "backup/root-changed",
                "published backup root changed after verification",
            ));
        }
        let manifest = decode_manifest(&manifest_bytes)?;
        // Restore never provisions or upgrades its target. The deployment
        // must have run the explicit migrator before constructing this
        // operator; re-check at the operation boundary to fail closed if the
        // catalog changed since connection.
        drop(PostgresStore::connect_configured(&self.connection)?);
        let genesis = read_object(directory, manifest.genesis_hash)?;
        let genesis_hash = sha256(&genesis);
        let mut restored_database = Database::from_genesis(decode_genesis(&genesis)?)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| crate::postgres::postgres_error("backup/restore-begin", error))?;
        transaction
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended('atomic/restore/' || $1, 0))",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-lock", error))?;
        if transaction
            .query_opt(
                "SELECT 1 FROM atomic_databases WHERE database_id = $1",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-target", error))?
            .is_some()
        {
            if target_matches_backup(&mut transaction, directory, &manifest, target_database_id)? {
                // Exact replay after an ambiguous acknowledgement is safe and
                // leaves the already-published target untouched.
                drop(transaction);
                return complete_restored_target(
                    &self.connection,
                    directory,
                    &manifest,
                    target_database_id,
                    &verification.database,
                );
            }
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "backup/target-exists",
                "restore target exists but is not this exact backup point",
            ));
        }
        transaction
            .execute(
                "INSERT INTO atomic_databases (database_id, lineage_id, genesis, genesis_hash) \
                 VALUES ($1, $2, $3, $4)",
                &[
                    &target_database_id,
                    &manifest.lineage_id,
                    &&genesis[..],
                    &&genesis_hash[..],
                ],
            )
            .map_err(restore_catalog_error)?;
        transaction
            .execute(
                "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
                &[&target_database_id, &&genesis_hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-head", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_database_generations (database_id) VALUES ($1)",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-generation", error))?;
        let mut previous = genesis_hash;
        for (offset, source_hash) in manifest.transactions.iter().enumerate() {
            let source = decode_transaction(&read_object(directory, *source_hash)?)?;
            let restored = DurableTransaction {
                database_id: target_database_id.into(),
                basis_t: source.basis_t,
                previous_hash: previous,
                eidx_frontier: source.eidx_frontier,
                tempids: source.tempids,
                tx_data: source.tx_data,
            };
            let payload = encode_transaction(&restored)?;
            let hash = transaction_hash(&payload);
            restored_database = restored_database.apply_committed(&restored)?;
            let state_hash = checkpoint_state_hash(&restored_database)?;
            if state_hash != manifest.state_hashes[offset] {
                return Err(fault(
                    "backup/restore-state-commitment",
                    format!(
                        "restored state at basis {} differs from the backup",
                        restored.basis_t
                    ),
                ));
            }
            let basis_sql = restored.basis_t as i64;
            transaction
                .execute(
                    "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                     VALUES ($1, $2, $3, $4, $5, $6)",
                    &[
                        &target_database_id,
                        &basis_sql,
                        &&previous[..],
                        &&hash[..],
                        &&payload[..],
                        &&manifest.state_hashes[offset][..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-transaction", error)
                })?;
            let request = manifest
                .requests
                .iter()
                .find(|request| request.basis == restored.basis_t)
                .ok_or_else(|| {
                    fault(
                        "backup/missing-request",
                        format!("basis {} has no request", restored.basis_t),
                    )
                })?;
            transaction
                .execute(
                    "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, $5)",
                    &[
                        &target_database_id,
                        &request.key,
                        &&request.digest[..],
                        &basis_sql,
                        &&hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-request", error)
                })?;
            transaction
                .execute(
                    "UPDATE atomic_heads SET basis_t = $2, tx_hash = $3 \
                     WHERE database_id = $1",
                    &[&target_database_id, &basis_sql, &&hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-publish", error)
                })?;
            // Prove this row was fully published while it is the head, then
            // defer the constraint again for the next restored transaction.
            // This keeps every ordinary integrity trigger enabled without
            // requiring the final head to equal every historical row.
            transaction
                .batch_execute(
                    "SET CONSTRAINTS atomic_transactions_require_publication IMMEDIATE; \
                     SET CONSTRAINTS atomic_transactions_require_publication DEFERRED",
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-publication-check", error)
                })?;
            previous = hash;
            debug_assert_eq!(offset as u64 + 1, restored.basis_t);
        }
        for program in &manifest.programs {
            let payload = read_object(directory, program.hash)?;
            let inserted = transaction
                .execute(
                    "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
                     VALUES ($1, $2, $3, $4) ON CONFLICT (program_hash) DO NOTHING",
                    &[
                        &&program.hash[..],
                        &program.kind,
                        &program.arity,
                        &&payload[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-program", error)
                })?;
            if inserted == 0 {
                let existing = transaction
                    .query_one(
                        "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                        &[&&program.hash[..]],
                    )
                    .map_err(|error| {
                        crate::postgres::postgres_error("backup/restore-existing-program", error)
                    })?;
                if existing.get::<_, i16>(0) != program.kind
                    || existing.get::<_, i16>(1) != program.arity
                    || existing.get::<_, Vec<u8>>(2) != payload
                {
                    return Err(fault(
                        "backup/restore-program-conflict",
                        "existing content-addressed program does not match its hash",
                    ));
                }
            }
        }
        if !target_matches_backup(&mut transaction, directory, &manifest, target_database_id)? {
            return Err(fault(
                "backup/restore-postcondition",
                "restored authoritative rows do not exactly match the requested backup point",
            ));
        }
        if fault_at == RestoreFault::BeforeCommit {
            return Err(injected("backup/restore-before-commit"));
        }
        transaction
            .commit()
            .map_err(|error| crate::postgres::postgres_error("backup/restore-commit", error))?;
        if fault_at == RestoreFault::AfterCommitBeforeResponse {
            return Err(injected("backup/restore-after-commit"));
        }
        complete_restored_target(
            &self.connection,
            directory,
            &manifest,
            target_database_id,
            &verification.database,
        )
    }
}

fn complete_restored_target(
    connection: &PostgresConnectionConfig,
    directory: &Path,
    manifest: &Manifest,
    target_database_id: &str,
    expected: &Database,
) -> Result<Database, SemanticError> {
    restore_tree_backup(connection, directory, manifest, target_database_id)?;
    let mut store = PostgresStore::connect_configured(connection)?;
    let restored = store.recover(target_database_id)?;
    if !restored.same_information_as(expected) {
        return Err(fault(
            "backup/restore-verify",
            "restored database information differs from the verified backup point",
        ));
    }
    Ok(restored)
}

fn restore_tree_backup(
    connection: &PostgresConnectionConfig,
    directory: &Path,
    backup: &Manifest,
    target_database_id: &str,
) -> Result<(), SemanticError> {
    let Some(tree) = &backup.tree else {
        // The authoritative log is complete. A target without a captured
        // physical root deliberately recovers from that log and may be
        // consolidated normally after restore.
        return Ok(());
    };
    let source_payload = read_object(directory, tree.manifest_hash)?;
    let source = PersistentTreeManifest::decode(&source_payload)?;
    let mut nodes = BTreeMap::new();
    for hash in &tree.node_hashes {
        nodes.insert(*hash, read_object(directory, *hash)?);
    }
    let node_set = persistent_tree::TreeNodeSet::from_nodes(nodes);
    for tree in &source.trees {
        persistent_tree::validate_tree(&tree.descriptor, &node_set)?;
    }
    let nodes = node_set.into_nodes();

    let mut client = connection.connect()?;
    let basis_sql = i64::try_from(source.basis_t).map_err(|_| {
        SemanticError::incorrect("backup/tree-basis", "tree basis exceeds PostgreSQL bigint")
    })?;
    let row = client
        .query_one(
            "SELECT tx_hash, state_hash FROM atomic_transactions \
             WHERE database_id = $1 AND basis_t = $2",
            &[&target_database_id, &basis_sql],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-tree-log", error))?;
    let target_tx_hash = digest(row.get(0), "restored tree transaction hash")?;
    let target_state_hash = digest(row.get(1), "restored tree state hash")?;
    if target_state_hash != source.state_hash {
        return Err(fault(
            "backup/restore-tree-state",
            "restored tree basis has a different semantic state commitment",
        ));
    }
    let target = PersistentTreeManifest {
        database_id: target_database_id.to_owned(),
        publication_revision: 1,
        basis_t: source.basis_t,
        tx_hash: target_tx_hash,
        state_hash: target_state_hash,
        excision_generation: 0,
        eidx_frontier: source.eidx_frontier,
        trees: source.trees,
    };
    let payload = target.encode()?;
    let manifest_hash = sha256(&payload);
    let roots = target
        .trees
        .iter()
        .map(|tree| TreeRootBinding {
            order: tree.descriptor.order,
            history: tree.descriptor.history,
            root_hash: tree.descriptor.root_hash,
            datom_count: tree.descriptor.count,
            encoded_bytes: tree.root_bytes,
        })
        .collect();
    let record = TreeManifestRecord {
        database_id: target_database_id.to_owned(),
        publication_revision: 1,
        basis_t: target.basis_t,
        tx_hash: target.tx_hash,
        state_hash: target.state_hash,
        excision_generation: 0,
        eidx_frontier: target.eidx_frontier,
        manifest_hash,
        payload,
        roots,
    };
    let mut store = PostgresTreeStore::connect_configured(connection)?;
    for (hash, payload) in nodes {
        store.insert_node(hash, &payload)?;
    }
    store.publish_manifest(&record, 0)?;
    Ok(())
}

fn insert_backup_object(
    objects: &mut BTreeMap<Digest, Vec<u8>>,
    hash: Digest,
    payload: Vec<u8>,
) -> Result<(), SemanticError> {
    if sha256(&payload) != hash {
        return Err(fault(
            "backup/object-hash",
            "captured immutable value does not match its content hash",
        ));
    }
    match objects.entry(hash) {
        Entry::Vacant(entry) => {
            entry.insert(payload);
        }
        Entry::Occupied(entry) if entry.get() != &payload => {
            return Err(fault(
                "backup/object-collision",
                "one content hash names two different captured values",
            ));
        }
        Entry::Occupied(_) => {}
    }
    Ok(())
}

fn capture_tree_backup<C: postgres::GenericClient>(
    client: &mut C,
    database_id: &str,
    lineage_id: &str,
    backup_basis: u64,
    source_transaction_hashes: &[Digest],
    portable_transaction_hashes: &[Digest],
    state_hashes: &[Digest],
    objects: &mut BTreeMap<Digest, Vec<u8>>,
) -> Result<Option<TreeBackup>, SemanticError> {
    let backup_basis_sql = i64::try_from(backup_basis).map_err(|_| {
        SemanticError::incorrect(
            "backup/basis-overflow",
            "backup basis exceeds PostgreSQL bigint",
        )
    })?;
    let row = client
        .query_opt(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier, m.manifest_hash, m.payload \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t \
                AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
               JOIN atomic_database_generations g \
                 ON g.database_id = m.database_id \
                AND g.excision_generation = m.excision_generation \
              WHERE p.database_id = $1 AND p.basis_t <= $2 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[&database_id, &backup_basis_sql],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/tree-publication", error))?;
    let Some(row) = row else {
        return Ok(None);
    };
    let publication_revision = unsigned(row.get(0), "tree publication revision")?;
    let basis_t = unsigned(row.get(1), "tree basis")?;
    let tx_hash = digest(row.get(2), "tree transaction hash")?;
    let state_hash = digest(row.get(3), "tree state hash")?;
    let excision_generation = unsigned(row.get(4), "tree excision generation")?;
    let eidx_frontier = unsigned(row.get(5), "tree entity frontier")?;
    let manifest_hash = digest(row.get(6), "tree manifest hash")?;
    let payload: Vec<u8> = row.get(7);
    let offset = usize::try_from(
        basis_t
            .checked_sub(1)
            .ok_or_else(|| fault("backup/tree-basis", "published tree has basis zero"))?,
    )
    .map_err(|_| {
        fault(
            "backup/tree-basis",
            "published tree basis is not representable",
        )
    })?;
    if source_transaction_hashes.get(offset) != Some(&tx_hash)
        || state_hashes.get(offset) != Some(&state_hash)
        || sha256(&payload) != manifest_hash
    {
        return Err(fault(
            "backup/tree-authority",
            "published tree does not identify the captured authoritative log",
        ));
    }
    let decoded = PersistentTreeManifest::decode(&payload)?;
    if decoded.database_id != database_id
        || decoded.publication_revision != publication_revision
        || decoded.basis_t != basis_t
        || decoded.tx_hash != tx_hash
        || decoded.state_hash != state_hash
        || decoded.excision_generation != excision_generation
        || decoded.eidx_frontier != eidx_frontier
        || decoded.hash()? != manifest_hash
    {
        return Err(fault(
            "backup/tree-manifest",
            "tree manifest payload disagrees with its published SQL coordinates",
        ));
    }
    let root_rows = client
        .query(
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $1 \
              ORDER BY history, index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/tree-roots", error))?;
    if root_rows.len() != decoded.trees.len() {
        return Err(fault(
            "backup/tree-roots",
            "published tree does not have exactly its canonical root bindings",
        ));
    }
    for (row, tree) in root_rows.into_iter().zip(&decoded.trees) {
        if decode_tree_order(row.get(0))? != tree.descriptor.order
            || row.get::<_, bool>(1) != tree.descriptor.history
            || digest(row.get(2), "tree root hash")? != tree.descriptor.root_hash
            || unsigned(row.get(3), "tree root count")? != tree.descriptor.count
            || unsigned(row.get(4), "tree root bytes")? != tree.root_bytes
        {
            return Err(fault(
                "backup/tree-root-mismatch",
                "tree manifest payload and relational roots disagree",
            ));
        }
    }

    let nodes = load_reachable_tree_nodes(client, &decoded)?;
    let node_set = persistent_tree::TreeNodeSet::from_nodes(nodes);
    for tree in &decoded.trees {
        persistent_tree::validate_tree(&tree.descriptor, &node_set)?;
    }
    let nodes = node_set.into_nodes();
    let node_hashes = nodes.keys().copied().collect();
    let portable_tx_hash = *portable_transaction_hashes.get(offset).ok_or_else(|| {
        fault(
            "backup/tree-basis",
            "published tree basis is outside the portable transaction chain",
        )
    })?;
    let portable_tx = objects
        .get(&portable_tx_hash)
        .ok_or_else(|| {
            fault(
                "backup/tree-transaction",
                "portable tree transaction is absent",
            )
        })
        .and_then(|payload| decode_transaction(payload))?;
    if portable_tx.eidx_frontier != eidx_frontier {
        return Err(fault(
            "backup/tree-frontier",
            "published tree entity frontier disagrees with its authoritative transaction",
        ));
    }
    // Tree nodes are already identity-free content. Rebind only the native
    // root envelope to the portable lineage transaction chain and normalize
    // PostgreSQL's replaceable publication/generation coordinates. Those
    // coordinates govern the live cache, not the logical backup point.
    let portable = PersistentTreeManifest {
        database_id: lineage_id.to_owned(),
        publication_revision: 1,
        basis_t,
        tx_hash: portable_tx_hash,
        state_hash,
        excision_generation: 0,
        eidx_frontier,
        trees: decoded.trees,
    };
    let portable_payload = portable.encode()?;
    let portable_manifest_hash = sha256(&portable_payload);
    insert_backup_object(objects, portable_manifest_hash, portable_payload)?;
    for (hash, payload) in nodes {
        insert_backup_object(objects, hash, payload)?;
    }
    Ok(Some(TreeBackup {
        manifest_hash: portable_manifest_hash,
        node_hashes,
    }))
}

fn load_reachable_tree_nodes<C: postgres::GenericClient>(
    client: &mut C,
    manifest: &PersistentTreeManifest,
) -> Result<BTreeMap<Digest, Vec<u8>>, SemanticError> {
    let mut pending: Vec<_> = manifest
        .trees
        .iter()
        .map(|tree| tree.descriptor.root_hash)
        .collect();
    let mut nodes = BTreeMap::new();
    while let Some(hash) = pending.pop() {
        if nodes.contains_key(&hash) {
            continue;
        }
        let row = client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/tree-node", error))?
            .ok_or_else(|| {
                fault(
                    "backup/tree-node-missing",
                    format!("published tree node {} is missing", hex(&hash)),
                )
            })?;
        let payload: Vec<u8> = row.get(0);
        let node = persistent_tree::decode_tree_node(&hash, &payload)?;
        match &node {
            persistent_tree::TreeNode::Root(root) => {
                pending.extend(root.directories.iter().map(|child| child.hash));
            }
            persistent_tree::TreeNode::Directory(directory) => {
                pending.extend(directory.leaves.iter().map(|child| child.hash));
            }
            persistent_tree::TreeNode::Leaf(_) => {}
        }
        nodes.insert(hash, payload);
    }
    Ok(nodes)
}

fn decode_tree_order(value: i16) -> Result<crate::IndexOrder, SemanticError> {
    match value {
        0 => Ok(crate::IndexOrder::Eavt),
        1 => Ok(crate::IndexOrder::Aevt),
        2 => Ok(crate::IndexOrder::Avet),
        3 => Ok(crate::IndexOrder::Vaet),
        _ => Err(fault(
            "backup/tree-index-order",
            "tree root has an invalid index order",
        )),
    }
}

fn verify_tree_backup(
    directory: &Path,
    backup: &Manifest,
    tree: &TreeBackup,
) -> Result<usize, SemanticError> {
    let payload = read_object(directory, tree.manifest_hash)?;
    let manifest = PersistentTreeManifest::decode(&payload)?;
    let offset = usize::try_from(
        manifest
            .basis_t
            .checked_sub(1)
            .ok_or_else(|| fault("backup/tree-basis", "backed-up tree has basis zero"))?,
    )
    .map_err(|_| {
        fault(
            "backup/tree-basis",
            "backed-up tree basis is not representable",
        )
    })?;
    let transaction_hash = *backup.transactions.get(offset).ok_or_else(|| {
        fault(
            "backup/tree-basis",
            "backed-up tree basis is outside the transaction chain",
        )
    })?;
    let transaction = decode_transaction(&read_object(directory, transaction_hash)?)?;
    if manifest.database_id != backup.lineage_id
        || manifest.publication_revision != 1
        || manifest.basis_t > backup.basis
        || transaction_hash != manifest.tx_hash
        || backup.state_hashes.get(offset) != Some(&manifest.state_hash)
        || manifest.excision_generation != 0
        || manifest.eidx_frontier != transaction.eidx_frontier
        || manifest.hash()? != tree.manifest_hash
    {
        return Err(fault(
            "backup/tree-binding",
            "backed-up tree does not identify this backup lineage and point",
        ));
    }
    let mut nodes = BTreeMap::new();
    for hash in &tree.node_hashes {
        nodes.insert(*hash, read_object(directory, *hash)?);
    }
    let mut reachable = BTreeSet::new();
    let mut pending: Vec<_> = manifest
        .trees
        .iter()
        .map(|tree| tree.descriptor.root_hash)
        .collect();
    while let Some(hash) = pending.pop() {
        if !reachable.insert(hash) {
            continue;
        }
        let payload = nodes.get(&hash).ok_or_else(|| {
            fault(
                "backup/tree-node-missing",
                format!("backup omits reachable tree node {}", hex(&hash)),
            )
        })?;
        match persistent_tree::decode_tree_node(&hash, payload)? {
            persistent_tree::TreeNode::Root(root) => {
                pending.extend(root.directories.into_iter().map(|child| child.hash));
            }
            persistent_tree::TreeNode::Directory(directory) => {
                pending.extend(directory.leaves.into_iter().map(|child| child.hash));
            }
            persistent_tree::TreeNode::Leaf(_) => {}
        }
    }
    if reachable.iter().copied().collect::<Vec<_>>() != tree.node_hashes {
        return Err(fault(
            "backup/tree-node-set",
            "backup tree node list is not its exact reachable closure",
        ));
    }
    let object_count = nodes.len() + 1;
    let node_set = persistent_tree::TreeNodeSet::from_nodes(nodes);
    for tree in &manifest.trees {
        persistent_tree::validate_tree(&tree.descriptor, &node_set)?;
    }
    Ok(object_count)
}

fn target_matches_backup<C: postgres::GenericClient>(
    client: &mut C,
    directory: &Path,
    manifest: &Manifest,
    target_database_id: &str,
) -> Result<bool, SemanticError> {
    let Some(catalog) = client
        .query_opt(
            "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-catalog", error))?
    else {
        return Ok(false);
    };
    let lineage_id: String = catalog.get(0);
    let genesis: Vec<u8> = catalog.get(1);
    let genesis_hash = digest(catalog.get(2), "restored genesis hash")?;
    if lineage_id != manifest.lineage_id
        || genesis_hash != manifest.genesis_hash
        || genesis != read_object(directory, manifest.genesis_hash)?
    {
        return Ok(false);
    }

    let rows = client
        .query(
            "SELECT basis_t, previous_hash, tx_hash, payload, state_hash \
             FROM atomic_transactions WHERE database_id = $1 ORDER BY basis_t",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-log", error))?;
    if rows.len() != manifest.transactions.len() {
        return Ok(false);
    }
    let mut expected_hashes = Vec::with_capacity(rows.len());
    let mut previous = manifest.genesis_hash;
    for (offset, (row, source_hash)) in rows.into_iter().zip(&manifest.transactions).enumerate() {
        let source = decode_transaction(&read_object(directory, *source_hash)?)?;
        let expected = DurableTransaction {
            database_id: target_database_id.to_owned(),
            basis_t: source.basis_t,
            previous_hash: previous,
            eidx_frontier: source.eidx_frontier,
            tempids: source.tempids,
            tx_data: source.tx_data,
        };
        let expected_payload = encode_transaction(&expected)?;
        let expected_hash = transaction_hash(&expected_payload);
        let row_basis = unsigned(row.get(0), "restored transaction basis")?;
        let row_previous = digest(row.get(1), "restored predecessor hash")?;
        let row_hash = digest(row.get(2), "restored transaction hash")?;
        let row_payload: Vec<u8> = row.get(3);
        let row_state_hash = digest(row.get(4), "restored state hash")?;
        if row_basis != offset as u64 + 1
            || row_previous != previous
            || row_hash != expected_hash
            || row_payload != expected_payload
            || row_state_hash != manifest.state_hashes[offset]
        {
            return Ok(false);
        }
        previous = expected_hash;
        expected_hashes.push(expected_hash);
    }

    let head = client
        .query_one(
            "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-head", error))?;
    if unsigned(head.get(0), "restored head basis")? != manifest.basis
        || digest(head.get(1), "restored head hash")? != previous
    {
        return Ok(false);
    }

    let requests = client
        .query(
            "SELECT request_key, request_digest, basis_t, tx_hash FROM atomic_requests \
             WHERE database_id = $1 ORDER BY basis_t",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-requests", error))?;
    if requests.len() != manifest.requests.len() {
        return Ok(false);
    }
    for (offset, (row, expected)) in requests.into_iter().zip(&manifest.requests).enumerate() {
        if row.get::<_, String>(0) != expected.key
            || digest(row.get(1), "restored request digest")? != expected.digest
            || unsigned(row.get(2), "restored request basis")? != expected.basis
            || digest(row.get(3), "restored request transaction hash")? != expected_hashes[offset]
        {
            return Ok(false);
        }
    }

    for program in &manifest.programs {
        let Some(row) = client
            .query_opt(
                "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                &[&&program.hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-check-program", error)
            })?
        else {
            return Ok(false);
        };
        if row.get::<_, i16>(0) != program.kind
            || row.get::<_, i16>(1) != program.arity
            || row.get::<_, Vec<u8>>(2) != read_object(directory, program.hash)?
        {
            return Ok(false);
        }
    }
    let generation = client
        .query_opt(
            "SELECT excision_generation FROM atomic_database_generations WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-generation", error)
        })?;
    Ok(generation.is_some_and(|row| row.get::<_, i64>(0) == 0))
}

fn prepare_directory(directory: &Path) -> Result<(), SemanticError> {
    let root_existed = directory.exists();
    fs::create_dir_all(objects(directory)).map_err(io_error("backup/create-objects"))?;
    fs::create_dir_all(snapshots(directory)).map_err(io_error("backup/create-snapshots"))?;
    sync_directory(&objects(directory), "backup/sync-objects-directory")?;
    sync_directory(&snapshots(directory), "backup/sync-snapshots-directory")?;
    sync_directory(directory, "backup/sync-directory")?;
    if !root_existed && let Some(parent) = directory.parent() {
        sync_directory(parent, "backup/sync-parent-directory")?;
    }
    Ok(())
}

fn ensure_claim(directory: &Path, claim: &BackupClaim) -> Result<(), SemanticError> {
    let encoded = encode_claim(claim)?;
    let path = directory.join("CLAIM");
    if path.exists() {
        return require_matching_claim(&path, claim);
    }
    match publish_exact(&path, &encoded, PublishFault::None) {
        Ok(_) => Ok(()),
        Err(error) if error.code == "backup/file-conflict" => require_matching_claim(&path, claim),
        Err(error) => Err(error),
    }
}

fn require_matching_claim(path: &Path, expected: &BackupClaim) -> Result<(), SemanticError> {
    let bytes = fs::read(path).map_err(io_error("backup/read-claim"))?;
    if decode_claim(&bytes)? == *expected {
        Ok(())
    } else {
        Err(SemanticError::new(
            ErrorCategory::Conflict,
            "backup/claim-conflict",
            "backup directory is claimed by another database lineage",
        ))
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum PublishFault {
    None,
    AfterStaged,
    AfterPublished,
}

fn write_object(
    directory: &Path,
    hash: Digest,
    bytes: &[u8],
    fault_at: PublishFault,
) -> Result<bool, SemanticError> {
    if sha256(bytes) != hash {
        return Err(fault(
            "backup/object-hash",
            "object bytes do not match their name",
        ));
    }
    let path = objects(directory).join(hex(&hash));
    publish_exact(&path, bytes, fault_at)
}

/// Stage bytes in the destination directory, fsync the complete file, then
/// atomically install it without replacing an existing value. A hard-link is
/// the portable Unix no-clobber equivalent of a rename publication: readers
/// observe either no final name or all bytes, and concurrent different bytes
/// cannot overwrite one another. The parent directory is fsynced before
/// success is reported.
fn publish_exact(path: &Path, bytes: &[u8], fault_at: PublishFault) -> Result<bool, SemanticError> {
    if path.exists() {
        return existing_file_matches(path, bytes).map(|()| false);
    }
    let parent = path.parent().ok_or_else(|| {
        SemanticError::incorrect("backup/file-parent", "backup file has no parent directory")
    })?;
    let (temp, mut file) = loop {
        let sequence = TEMP_SEQUENCE.fetch_add(1, Ordering::Relaxed);
        let candidate = parent.join(format!(
            ".atomic-tmp-{}-{sequence:016x}",
            std::process::id()
        ));
        match OpenOptions::new()
            .write(true)
            .create_new(true)
            .open(&candidate)
        {
            Ok(file) => break (candidate, file),
            Err(error) if error.kind() == std::io::ErrorKind::AlreadyExists => continue,
            Err(error) => return Err(io_error("backup/create-temp")(error)),
        }
    };
    file.write_all(bytes)
        .map_err(io_error("backup/write-temp"))?;
    file.sync_all().map_err(io_error("backup/sync-temp"))?;
    drop(file);
    if fault_at == PublishFault::AfterStaged {
        return Err(injected("backup/after-file-staged"));
    }
    match fs::hard_link(&temp, path) {
        Ok(()) => {
            if fault_at == PublishFault::AfterPublished {
                sync_directory(parent, "backup/sync-published-directory")?;
                return Err(injected("backup/after-file-published"));
            }
            fs::remove_file(&temp).map_err(io_error("backup/remove-temp"))?;
            sync_directory(parent, "backup/sync-published-directory")?;
            Ok(true)
        }
        Err(error) if error.kind() == std::io::ErrorKind::AlreadyExists => {
            let result = existing_file_matches(path, bytes).map(|()| false);
            fs::remove_file(&temp).map_err(io_error("backup/remove-racing-temp"))?;
            sync_directory(parent, "backup/sync-racing-directory")?;
            result
        }
        Err(error) => {
            let _ = fs::remove_file(&temp);
            Err(io_error("backup/publish")(error))
        }
    }
}

fn existing_file_matches(path: &Path, bytes: &[u8]) -> Result<(), SemanticError> {
    if fs::read(path).map_err(io_error("backup/read-existing"))? == bytes {
        Ok(())
    } else {
        Err(fault(
            "backup/file-conflict",
            "backup file already exists with different bytes",
        ))
    }
}

fn sync_directory(directory: &Path, code: &'static str) -> Result<(), SemanticError> {
    File::open(directory)
        .and_then(|file| file.sync_all())
        .map_err(io_error(code))
}

fn read_object(directory: &Path, hash: Digest) -> Result<Vec<u8>, SemanticError> {
    let bytes =
        fs::read(objects(directory).join(hex(&hash))).map_err(io_error("backup/object-read"))?;
    if sha256(&bytes) != hash {
        return Err(fault(
            "backup/object-corrupt",
            format!("object {} failed its hash", hex(&hash)),
        ));
    }
    Ok(bytes)
}

fn objects(directory: &Path) -> PathBuf {
    directory.join("objects")
}

fn snapshots(directory: &Path) -> PathBuf {
    directory.join("snapshots")
}

fn load_manifest(directory: &Path, basis: u64) -> Result<(Manifest, Digest), SemanticError> {
    let path = snapshots(directory).join(format!("{basis:020}.atbk"));
    let bytes = fs::read(path).map_err(io_error("backup/manifest-read"))?;
    let hash = sha256(&bytes);
    let manifest = decode_manifest(&bytes)?;
    if manifest.basis != basis {
        return Err(fault(
            "backup/root-basis",
            "requested basis does not match the snapshot manifest",
        ));
    }
    Ok((manifest, hash))
}

/// Reuse an already-published root only after authenticating its full object
/// graph and comparing every authoritative logical field captured from the
/// live repeatable-read snapshot. The physical tree pointer is intentionally
/// excluded: it is a replaceable derived representation of the same value.
fn reusable_existing_point(
    directory: &Path,
    candidate: &Manifest,
) -> Result<Option<BackupPoint>, SemanticError> {
    let path = snapshots(directory).join(format!("{:020}.atbk", candidate.basis));
    match fs::symlink_metadata(&path) {
        Ok(metadata) if metadata.file_type().is_file() => {}
        Ok(_) => {
            return Err(fault(
                "backup/root-type",
                "published backup root is not a regular file",
            ));
        }
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => return Ok(None),
        Err(error) => return Err(io_error("backup/root-metadata")(error)),
    }

    let verified = PortableBackup::verify_backup(directory, candidate.basis, true)?;
    let (existing, manifest_hash) = load_manifest(directory, candidate.basis)?;
    if manifest_hash != verified.point.manifest_hash {
        return Err(fault(
            "backup/root-changed",
            "published backup root changed while it was being verified",
        ));
    }
    let same_logical_point = existing.lineage_id == candidate.lineage_id
        && existing.basis == candidate.basis
        && existing.genesis_hash == candidate.genesis_hash
        && existing.transactions == candidate.transactions
        && existing.state_hashes == candidate.state_hashes
        && existing.requests == candidate.requests
        && existing.programs == candidate.programs;
    if !same_logical_point {
        return Err(SemanticError::new(
            ErrorCategory::Conflict,
            "backup/point-conflict",
            "backup root at this lineage and basis contains different logical information",
        ));
    }
    Ok(Some(BackupPoint {
        lineage_id: candidate.lineage_id.clone(),
        basis_t: candidate.basis,
        manifest_hash,
        objects_written: 0,
        objects_reused: manifest_object_hashes(&existing).len(),
    }))
}

fn manifest_object_hashes(manifest: &Manifest) -> BTreeSet<Digest> {
    let mut hashes: BTreeSet<_> = std::iter::once(manifest.genesis_hash)
        .chain(manifest.transactions.iter().copied())
        .chain(manifest.programs.iter().map(|program| program.hash))
        .collect();
    if let Some(tree) = &manifest.tree {
        hashes.insert(tree.manifest_hash);
        hashes.extend(tree.node_hashes.iter().copied());
    }
    hashes
}

fn ensure_object_present(directory: &Path, hash: Digest) -> Result<(), SemanticError> {
    let metadata = fs::symlink_metadata(objects(directory).join(hex(&hash)))
        .map_err(io_error("backup/object-missing"))?;
    if !metadata.file_type().is_file() {
        return Err(fault(
            "backup/object-type",
            format!("object {} is not a regular file", hex(&hash)),
        ));
    }
    Ok(())
}

fn encode_claim(claim: &BackupClaim) -> Result<Vec<u8>, SemanticError> {
    let mut bytes = Vec::new();
    bytes.extend_from_slice(CLAIM_MAGIC);
    bytes.extend_from_slice(&CLAIM_VERSION.to_be_bytes());
    put_string(&mut bytes, &claim.lineage_id)?;
    bytes.extend_from_slice(&claim.genesis_hash);
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_claim(bytes: &[u8]) -> Result<BackupClaim, SemanticError> {
    if bytes.len() < 110 || &bytes[..4] != CLAIM_MAGIC {
        return Err(fault(
            "backup/claim-header",
            "backup claim header is invalid",
        ));
    }
    if u16::from_be_bytes([bytes[4], bytes[5]]) != CLAIM_VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/claim-version",
            "unsupported backup claim version",
        ));
    }
    let checksum_at = bytes
        .len()
        .checked_sub(32)
        .ok_or_else(|| fault("backup/claim-length", "backup claim is truncated"))?;
    if sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..] {
        return Err(fault(
            "backup/claim-checksum",
            "backup claim checksum is invalid",
        ));
    }
    let mut cursor = Cursor::new(&bytes[6..checksum_at]);
    let claim = BackupClaim {
        lineage_id: cursor.string()?,
        genesis_hash: cursor.digest()?,
    };
    cursor.finish()?;
    if !valid_lineage_id(&claim.lineage_id) || encode_claim(&claim)? != bytes {
        return Err(fault(
            "backup/noncanonical-claim",
            "backup claim is not canonical",
        ));
    }
    Ok(claim)
}

fn verify_claim(directory: &Path, manifest: &Manifest) -> Result<(), SemanticError> {
    let bytes = fs::read(directory.join("CLAIM")).map_err(io_error("backup/claim-read"))?;
    let claim = decode_claim(&bytes)?;
    if claim.lineage_id != manifest.lineage_id || claim.genesis_hash != manifest.genesis_hash {
        return Err(SemanticError::new(
            ErrorCategory::Conflict,
            "backup/claim-mismatch",
            "backup claim and snapshot lineage differ",
        ));
    }
    Ok(())
}

fn manifest_is_canonical(manifest: &Manifest) -> bool {
    if !valid_lineage_id(&manifest.lineage_id)
        || manifest.state_hashes.iter().any(|hash| hash == &[0; 32])
        || manifest
            .requests
            .iter()
            .enumerate()
            .any(|(offset, request)| {
                request.key.is_empty()
                    || request.basis != offset as u64 + 1
                    || manifest.transactions.get(offset) != Some(&request.tx_hash)
            })
        || manifest
            .requests
            .iter()
            .map(|row| row.key.as_str())
            .collect::<BTreeSet<_>>()
            .len()
            != manifest.requests.len()
        || !strictly_sorted_by(&manifest.programs, |row| row.hash)
        || manifest.programs.iter().any(|row| {
            !(0..=3).contains(&row.kind) || !(0..=i16::from(u8::MAX)).contains(&row.arity)
        })
        || manifest.tree.as_ref().is_some_and(|tree| {
            tree.node_hashes.is_empty()
                || !strictly_sorted_by(&tree.node_hashes, |hash| *hash)
                || tree.node_hashes.binary_search(&tree.manifest_hash).is_ok()
        })
    {
        return false;
    }
    true
}

fn strictly_sorted_by<T, K: Ord>(values: &[T], key: impl Fn(&T) -> K) -> bool {
    values.windows(2).all(|pair| key(&pair[0]) < key(&pair[1]))
}

fn valid_lineage_id(value: &str) -> bool {
    value.len() == 36
        && value.as_bytes()[14] == b'4'
        && matches!(value.as_bytes()[19], b'8' | b'9' | b'a' | b'b')
        && value.bytes().enumerate().all(|(index, byte)| match index {
            8 | 13 | 18 | 23 => byte == b'-',
            _ => byte.is_ascii_digit() || (b'a'..=b'f').contains(&byte),
        })
}

fn encode_manifest(manifest: &Manifest) -> Result<Vec<u8>, SemanticError> {
    let mut body = Vec::new();
    put_string(&mut body, &manifest.lineage_id)?;
    put_u64(&mut body, manifest.basis);
    body.extend_from_slice(&manifest.genesis_hash);
    put_hashes(&mut body, &manifest.transactions)?;
    put_hashes(&mut body, &manifest.state_hashes)?;
    put_u32(&mut body, manifest.requests.len())?;
    for request in &manifest.requests {
        put_string(&mut body, &request.key)?;
        body.extend_from_slice(&request.digest);
        put_u64(&mut body, request.basis);
        body.extend_from_slice(&request.tx_hash);
    }
    put_u32(&mut body, manifest.programs.len())?;
    for program in &manifest.programs {
        body.extend_from_slice(&program.hash);
        body.extend_from_slice(&program.kind.to_be_bytes());
        body.extend_from_slice(&program.arity.to_be_bytes());
    }
    match &manifest.tree {
        None => body.push(0),
        Some(tree) => {
            body.push(1);
            body.extend_from_slice(&tree.manifest_hash);
            put_hashes(&mut body, &tree.node_hashes)?;
        }
    }
    let mut bytes = Vec::new();
    bytes.extend_from_slice(MAGIC);
    bytes.extend_from_slice(&VERSION.to_be_bytes());
    let body_len = u64::try_from(body.len()).map_err(|_| {
        SemanticError::incorrect("backup/too-large", "backup manifest is too large")
    })?;
    bytes.extend_from_slice(&body_len.to_be_bytes());
    bytes.extend_from_slice(&body);
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_manifest(bytes: &[u8]) -> Result<Manifest, SemanticError> {
    if bytes.len() < 46 || &bytes[..4] != MAGIC {
        return Err(fault(
            "backup/manifest-header",
            "backup manifest header is invalid",
        ));
    }
    if u16::from_be_bytes([bytes[4], bytes[5]]) != VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/manifest-version",
            "unsupported backup manifest version",
        ));
    }
    let len = usize::try_from(u64::from_be_bytes(
        bytes[6..14].try_into().expect("fixed slice"),
    ))
    .map_err(|_| {
        fault(
            "backup/manifest-length",
            "manifest length is not representable",
        )
    })?;
    let checksum_at = 14usize
        .checked_add(len)
        .ok_or_else(|| fault("backup/manifest-length", "manifest length overflow"))?;
    let expected_len = checksum_at
        .checked_add(32)
        .ok_or_else(|| fault("backup/manifest-length", "manifest length overflow"))?;
    if bytes.len() != expected_len
        || sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..]
    {
        return Err(fault(
            "backup/manifest-checksum",
            "backup manifest length or checksum is invalid",
        ));
    }
    let mut cursor = Cursor::new(&bytes[14..14 + len]);
    let lineage_id = cursor.string()?;
    let basis = cursor.u64()?;
    let genesis_hash = cursor.digest()?;
    let transactions = cursor.hashes()?;
    let state_hashes = cursor.hashes()?;
    let request_count = cursor.count_with_minimum(76)?;
    let mut requests = Vec::with_capacity(request_count);
    for _ in 0..request_count {
        requests.push(RequestRow {
            key: cursor.string()?,
            digest: cursor.digest()?,
            basis: cursor.u64()?,
            tx_hash: cursor.digest()?,
        });
    }
    let program_count = cursor.count_with_minimum(36)?;
    let mut programs = Vec::with_capacity(program_count);
    for _ in 0..program_count {
        programs.push(ProgramRow {
            hash: cursor.digest()?,
            kind: cursor.i16()?,
            arity: cursor.i16()?,
        });
    }
    let tree = match cursor.u8()? {
        0 => None,
        1 => Some(TreeBackup {
            manifest_hash: cursor.digest()?,
            node_hashes: cursor.hashes()?,
        }),
        _ => {
            return Err(fault(
                "backup/manifest-tree-tag",
                "backup manifest has an invalid tree tag",
            ));
        }
    };
    cursor.finish()?;
    let manifest = Manifest {
        lineage_id,
        basis,
        genesis_hash,
        transactions,
        state_hashes,
        requests,
        programs,
        tree,
    };
    let basis_len = usize::try_from(basis).map_err(|_| {
        fault(
            "backup/manifest-basis",
            "manifest basis is not representable",
        )
    })?;
    if manifest.transactions.len() != basis_len
        || manifest.state_hashes.len() != basis_len
        || manifest.requests.len() != basis_len
        || !manifest_is_canonical(&manifest)
        || encode_manifest(&manifest)? != bytes
    {
        return Err(fault(
            "backup/noncanonical-manifest",
            "backup manifest is not canonical",
        ));
    }
    Ok(manifest)
}

fn put_hashes(output: &mut Vec<u8>, values: &[Digest]) -> Result<(), SemanticError> {
    put_u32(output, values.len())?;
    for value in values {
        output.extend_from_slice(value);
    }
    Ok(())
}

fn put_string(output: &mut Vec<u8>, value: &str) -> Result<(), SemanticError> {
    put_u32(output, value.len())?;
    output.extend_from_slice(value.as_bytes());
    Ok(())
}

fn put_u32(output: &mut Vec<u8>, value: usize) -> Result<(), SemanticError> {
    let value = u32::try_from(value).map_err(|_| {
        SemanticError::incorrect("backup/too-large", "backup collection is too large")
    })?;
    output.extend_from_slice(&value.to_be_bytes());
    Ok(())
}

fn put_u64(output: &mut Vec<u8>, value: u64) {
    output.extend_from_slice(&value.to_be_bytes());
}

struct Cursor<'a> {
    bytes: &'a [u8],
    at: usize,
}

impl<'a> Cursor<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, at: 0 }
    }
    fn take(&mut self, len: usize) -> Result<&'a [u8], SemanticError> {
        let end = self
            .at
            .checked_add(len)
            .ok_or_else(|| fault("backup/manifest-length", "manifest length overflow"))?;
        let bytes = self
            .bytes
            .get(self.at..end)
            .ok_or_else(|| fault("backup/manifest-truncated", "manifest is truncated"))?;
        self.at = end;
        Ok(bytes)
    }
    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.take(8)?.try_into().expect("fixed")))
    }
    fn u8(&mut self) -> Result<u8, SemanticError> {
        Ok(self.take(1)?[0])
    }
    fn i16(&mut self) -> Result<i16, SemanticError> {
        Ok(i16::from_be_bytes(self.take(2)?.try_into().expect("fixed")))
    }
    fn digest(&mut self) -> Result<Digest, SemanticError> {
        self.take(32)?
            .try_into()
            .map_err(|_| fault("backup/manifest-truncated", "digest truncated"))
    }
    fn count_with_minimum(&mut self, minimum_item_bytes: usize) -> Result<usize, SemanticError> {
        debug_assert!(minimum_item_bytes > 0);
        let count = u32::from_be_bytes(self.take(4)?.try_into().expect("fixed")) as usize;
        let remaining = self.bytes.len().saturating_sub(self.at);
        if count > remaining / minimum_item_bytes {
            return Err(fault(
                "backup/manifest-count",
                "manifest count cannot fit in the remaining bytes",
            ));
        }
        Ok(count)
    }
    fn string(&mut self) -> Result<String, SemanticError> {
        let len = u32::from_be_bytes(self.take(4)?.try_into().expect("fixed")) as usize;
        String::from_utf8(self.take(len)?.to_vec())
            .map_err(|_| fault("backup/manifest-utf8", "manifest string is not UTF-8"))
    }
    fn hashes(&mut self) -> Result<Vec<Digest>, SemanticError> {
        let n = self.count_with_minimum(32)?;
        (0..n).map(|_| self.digest()).collect()
    }
    fn finish(&self) -> Result<(), SemanticError> {
        if self.at == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "backup/manifest-trailing",
                "manifest has trailing bytes",
            ))
        }
    }
}

fn program_kind(program: &Program) -> i16 {
    match program.kind {
        crate::ProgramKind::Transaction => 0,
        crate::ProgramKind::AttributePredicate => 1,
        crate::ProgramKind::Query => 2,
        crate::ProgramKind::EntityPredicate => 3,
    }
}

fn validated_program_row(row: &postgres::Row) -> Result<(ProgramRow, Vec<u8>), SemanticError> {
    let hash = digest(row.get(0), "program hash")?;
    let kind: i16 = row.get(1);
    let arity: i16 = row.get(2);
    let payload: Vec<u8> = row.get(3);
    let decoded = decode_program(&payload)?;
    if sha256(&payload) != hash
        || program_kind(&decoded) != kind
        || i16::from(decoded.arity) != arity
    {
        return Err(fault(
            "backup/program-corrupt",
            "program content or metadata is invalid",
        ));
    }
    Ok((ProgramRow { hash, kind, arity }, payload))
}

fn collect_function_hashes(value: &crate::Value, output: &mut BTreeSet<Digest>) {
    match value {
        crate::Value::Function(hash) => {
            output.insert(*hash);
        }
        crate::Value::Tuple(values) => {
            for value in values.iter().flatten() {
                collect_function_hashes(value, output);
            }
        }
        _ => {}
    }
}

fn collect_program_dependencies(program: &Program, output: &mut BTreeSet<Digest>) {
    fn entity(entity: &crate::EntityRef, output: &mut BTreeSet<Digest>) {
        if let crate::EntityRef::Lookup { value, .. } = entity {
            collect_function_hashes(value, output);
        }
    }

    fn term(term: &crate::QueryTerm, output: &mut BTreeSet<Digest>) {
        if let crate::QueryTerm::Constant(value) = term {
            collect_function_hashes(value, output);
        }
    }

    fn instructions(values: &[crate::Instruction], output: &mut BTreeSet<Digest>) {
        for instruction in values {
            match instruction {
                crate::Instruction::PushConstant(value) => collect_function_hashes(value, output),
                crate::Instruction::PushEntity(value) => entity(value, output),
                crate::Instruction::If {
                    then_branch,
                    else_branch,
                } => {
                    instructions(then_branch, output);
                    instructions(else_branch, output);
                }
                crate::Instruction::ForEach { body } => instructions(body, output),
                crate::Instruction::Query(query) => {
                    for pattern in query.patterns() {
                        term(&pattern.entity, output);
                        term(&pattern.value, output);
                    }
                }
                crate::Instruction::EmitCall { function, .. } => match function {
                    crate::CallableRef::ExactHash(hash) => {
                        output.insert(*hash);
                    }
                    crate::CallableRef::Database(entity_ref) => entity(entity_ref, output),
                    crate::CallableRef::Local(_) => {}
                },
                _ => {}
            }
        }
    }

    instructions(&program.instructions, output);
}

fn digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes
        .try_into()
        .map_err(|_| fault("backup/digest", format!("{label} is not 32 bytes")))
}

fn unsigned(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| fault("backup/negative", format!("{label} is negative")))
}

fn hex(bytes: &Digest) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
fn restore_catalog_error(error: postgres::Error) -> SemanticError {
    if error.as_db_error().is_some_and(|database_error| {
        database_error.code() == &postgres::error::SqlState::UNIQUE_VIOLATION
            && database_error.constraint() == Some("atomic_databases_lineage_id_key")
    }) {
        return SemanticError::new(
            ErrorCategory::Conflict,
            "backup/lineage-exists",
            "this database lineage already has a name in the target catalog",
        );
    }
    crate::postgres::postgres_error("backup/restore-catalog", error)
}
fn injected(code: &'static str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Interrupted,
        code,
        "deterministic backup fault injected",
    )
}
fn not_found(database_id: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::NotFound,
        "backup/database-not-found",
        format!("database {database_id} does not exist"),
    )
}
fn io_error(code: &'static str) -> impl FnOnce(std::io::Error) -> SemanticError {
    move |error| SemanticError::new(ErrorCategory::Unavailable, code, error.to_string())
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::time::{SystemTime, UNIX_EPOCH};

    fn temporary_directory(label: &str) -> PathBuf {
        let unique = SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("clock after epoch")
            .as_nanos();
        let path = std::env::temp_dir().join(format!(
            "atomic-backup-{label}-{}-{unique}",
            std::process::id()
        ));
        fs::create_dir(&path).expect("create test directory");
        path
    }

    #[test]
    fn staged_file_is_never_visible_under_its_final_name_and_retry_converges() {
        let directory = temporary_directory("staged");
        let path = directory.join("root.atbk");
        let error = publish_exact(&path, b"complete-root", PublishFault::AfterStaged)
            .expect_err("injected interruption");
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert!(!path.exists());
        assert!(fs::read_dir(&directory).unwrap().all(|entry| {
            entry
                .unwrap()
                .file_name()
                .to_string_lossy()
                .starts_with('.')
        }));

        assert!(publish_exact(&path, b"complete-root", PublishFault::None).unwrap());
        assert_eq!(fs::read(&path).unwrap(), b"complete-root");
        assert!(!publish_exact(&path, b"complete-root", PublishFault::None).unwrap());
        fs::remove_dir_all(directory).unwrap();
    }

    #[test]
    fn acknowledged_ambiguity_exposes_only_complete_content_and_retry_reuses_it() {
        let directory = temporary_directory("published");
        let path = directory.join("root.atbk");
        let error = publish_exact(&path, b"complete-root", PublishFault::AfterPublished)
            .expect_err("injected interruption");
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert_eq!(fs::read(&path).unwrap(), b"complete-root");
        assert!(!publish_exact(&path, b"complete-root", PublishFault::None).unwrap());
        let conflict = publish_exact(&path, b"different-root", PublishFault::None).unwrap_err();
        assert_eq!(
            (conflict.category, conflict.code),
            (ErrorCategory::Fault, "backup/file-conflict")
        );
        fs::remove_dir_all(directory).unwrap();
    }

    #[test]
    fn binary_claim_binds_durable_lineage_and_genesis_and_rejects_tampering() {
        let claim = BackupClaim {
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            genesis_hash: sha256(b"canonical-genesis"),
        };
        let encoded = encode_claim(&claim).unwrap();
        assert_eq!(decode_claim(&encoded).unwrap(), claim);
        assert_ne!(&encoded, claim.lineage_id.as_bytes());
        let mut damaged = encoded;
        damaged[10] ^= 1;
        assert_eq!(
            decode_claim(&damaged).unwrap_err().code,
            "backup/claim-checksum"
        );
    }
}
