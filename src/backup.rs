//! Source-shaped differential, root-last backup for one PostgreSQL database.
//!
//! Canonical genesis, log, request identities, state commitments, and program
//! definitions are the authority. When an authenticated native persistent
//! tree is available at or before the captured basis, its complete immutable
//! node closure is copied as a recovery accelerator and rebound to the target
//! timeline on restore. A missing tree is valid and restores by replaying the
//! authoritative log; physical indexes never become backup authority.
//!
//! Capture authenticates immutable envelopes, their linked coordinates and
//! reachable content without replaying every database prefix. A hash-valid
//! log can still make false semantic claims: semantic verification and restore
//! replay it and check each state commitment. Presence-only verification does
//! not make that semantic proof; deep verification also compares every tree.
//! Reusing an already-published point authenticates its complete physical
//! object graph and exact logical coordinates, without semantic replay.
//! Repairing a damaged source request-base accelerator can still require
//! administrative log recovery before its exact portable copy is emitted.
//!
//! Live generation-zero envelopes still contain PostgreSQL catalog names.
//! Backup canonicalizes those carriers onto the database's immutable random
//! lineage, so rename does not recopy history or change a point root. Program
//! selection is temporal `:db/fn` information; every immutable blob reachable
//! from that history is included without a second mutable activation catalog.
//!
//! The filesystem destination is an operator-owned private repository, not an
//! adversarial shared namespace. Entry points reject symlinked or group/world-
//! writable repository directories and non-regular roots/objects. They do not
//! attempt to defend against a privileged process concurrently replacing path
//! ancestors or mutating already-published regular files.

use crate::log_generation::{
    GenerationTransactionMembership, LineageTransactionContent,
    encode_generation_transaction_membership, request_key_hash, tombstone_request_digest,
};
use crate::peer::build_full_native_tree;
use crate::postgres::recover_generation_to;
use crate::sql_io::SqlClient as Client;
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Database, Digest, DurableTransaction, ErrorCategory, PostgresConnectionConfig, PostgresStore,
    PostgresTreeStore, Program, SemanticError, TreeManifestRecord, TreePublicationDelta,
    TreeRootBinding, decode_genesis, decode_program, decode_transaction, encode_genesis, sha256,
    transaction_hash,
};
use crate::{PersistentTreeManifest, persistent_tree};
use postgres::IsolationLevel;
use sha2::{Digest as _, Sha256};
use std::collections::{BTreeMap, BTreeSet};
use std::fs::{self, File, OpenOptions};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::sync::Arc;
use std::sync::atomic::{AtomicU64, Ordering};

#[cfg(test)]
#[path = "backup_allocation_tests.rs"]
mod allocation_tests;
#[cfg(test)]
#[path = "backup_tree_semantics_tests.rs"]
mod tree_semantics_tests;

const MAGIC: &[u8; 4] = b"ATBK";
const VERSION: u16 = 5;
const LEGACY_VERSION: u16 = 3;
const CLAIM_MAGIC: &[u8; 4] = b"ATCL";
const CLAIM_VERSION: u16 = 1;
const REQUEST_MAGIC: &[u8; 4] = b"ATRQ";
const REQUEST_VERSION: u16 = 3;
const LEGACY_REQUEST_VERSION: u16 = 2;
const COMPLETED_EXCISION_MAGIC: &[u8; 4] = b"ATEX";
const COMPLETED_EXCISION_VERSION: u16 = 1;
const LEGACY_MEMBERSHIP_DOMAIN: &[u8] = b"atomic/backup-legacy-membership/v1\0";
const MAX_BACKUP_ATTEMPTS: usize = 3;
const RESTORE_BATCH_ROWS: usize = 256;
const LEGACY_RECEIPT_TAIL_BYTES: u64 = 4 * 1024 * 1024;
const LEGACY_RECEIPT_TAIL_DATOMS: u64 = 16_384;
const RESTORE_SCRATCH_PINS: usize = 16;
static TEMP_SEQUENCE: AtomicU64 = AtomicU64::new(0);

#[path = "backup_read.rs"]
mod backup_read;
pub(crate) use backup_read::{BackupReadMetadata, open_read_point, read_log_transaction};
use backup_read::{capture_exact_read_tree, capture_read_log_index};

#[path = "backup_file.rs"]
mod backup_file;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct BackupPoint {
    /// Immutable database identity. Human catalog names are locators and are
    /// deliberately absent from the canonical backup root.
    pub lineage_id: String,
    /// Physical information generation selected for this logical basis. A
    /// privacy excision can publish a new value at the same `basis_t`.
    pub log_generation: u64,
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

/// In-process evidence from the mandatory deep pass, not a persisted trust
/// flag. Retain only the main accelerator's schema/coordinate, never one full
/// database per receipt archive or historical tree.
struct VerifiedMainTree {
    manifest_hash: Digest,
    basis_t: u64,
    state_hash: Digest,
    eidx_frontier: u64,
    schema: Arc<crate::Schema>,
}

struct VerifiedBackupSource {
    verification: BackupVerification,
    main_tree: Option<VerifiedMainTree>,
}

/// Exact target identity established by comparing its authoritative rows to
/// the deeply verified backup. A final locked-head check consumes this proof
/// after optional physical publication, including on an ambiguous retry.
struct MatchedRestoreTarget {
    lineage_id: String,
    generation: u64,
    basis_t: u64,
    tx_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
    genesis_hash: Digest,
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
    version: u16,
    lineage_id: String,
    log_generation: u64,
    basis: u64,
    genesis_hash: Digest,
    head_transaction_hash: Digest,
    head_state_hash: Digest,
    request_head_hash: Digest,
    completed_excision_head_hash: Digest,
    // Read-only v3 compatibility. Version 4 roots leave these empty and name
    // bounded linked values instead of flattening history into the root.
    transactions: Vec<Digest>,
    state_hashes: Vec<Digest>,
    requests: Vec<RequestRow>,
    programs: Vec<ProgramRow>,
    tree: Option<TreeBackup>,
    // Authenticated sparse t -> portable membership lookup for direct log reads.
    read_log_index: Option<Digest>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct TreeBackup {
    manifest_hash: Digest,
    legacy_node_hashes: Option<Vec<Digest>>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct BackupRequestRecord {
    lineage_id: String,
    log_generation: u64,
    basis: u64,
    transaction_hash: Digest,
    previous_hash: Digest,
    request_key_hash: Digest,
    digest: Digest,
    request_kind: u8,
    /// Portable immutable native tree that is the exact db-before base for a
    /// kind-2 request. The request-chain checksum makes this binding part of
    /// the logical backup root; tree nodes remain deduplicated objects.
    base_manifest_hash: Option<Digest>,
    receipt_tempids: BTreeMap<String, u64>,
}

/// One immutable generation link. For positive generations its encoded bytes
/// are exactly Atomic's live membership-hash preimage, so the backup object
/// hash is the authoritative transaction hash. Generation zero is converted
/// onto an equivalent lineage-bound backup domain without relabelling legacy
/// alias-bound bytes as native generation content.
#[derive(Clone, Debug, Eq, PartialEq)]
struct BackupMembershipRecord {
    lineage_id: String,
    log_generation: u64,
    basis: u64,
    previous_hash: Digest,
    content_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct BackupCompletedExcision {
    lineage_id: String,
    request_t: u64,
    request_entity: u64,
    previous_hash: Digest,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct BackupClaim {
    lineage_id: String,
    genesis_hash: Digest,
}

#[derive(Clone, Debug)]
struct ManifestRoot {
    manifest: Manifest,
}

type ParentContext = ManifestRoot;

#[derive(Clone, Debug)]
struct LoadedBackupEntry {
    transaction_hash: Digest,
    content_hash: Option<Digest>,
    reserved_frontier: Option<u64>,
    transaction: DurableTransaction,
    request: BackupRequestRecord,
    legacy_state_hash: Option<Digest>,
}

#[derive(Clone, Debug)]
struct LoadedBackupLog {
    entries: Vec<LoadedBackupEntry>,
    objects_read: usize,
}

/// Allocation proof follows the same authenticated prefix as database replay.
/// Legacy observation is a lower bound, not permission to allocate from a
/// physically excised history; a modern checkpoint carries the full proof.
struct BackupAllocationReplay {
    observed: crate::reserved_allocation::ReservedAllocation,
    modern: bool,
}

impl BackupAllocationReplay {
    fn new(database: &Database) -> Result<Self, SemanticError> {
        let mut observed = crate::reserved_allocation::ReservedAllocation::initial();
        observed.observe_datoms(database.genesis_datoms())?;
        Ok(Self {
            observed,
            modern: false,
        })
    }

    fn apply(
        &mut self,
        database: &Database,
        transaction: &DurableTransaction,
        reserved_frontier: Option<u64>,
        allow_excision: bool,
    ) -> Result<Database, SemanticError> {
        if let Some(frontier) = reserved_frontier {
            let after = crate::reserved_allocation::ReservedAllocation::from_frontier(
                frontier,
                transaction.eidx_frontier,
            )?;
            // V2 replay records come from content.to_transaction(): these
            // synthetic tempids encode only canonical issuance witnesses.
            // Caller-named aliases, including upserts, remain separate in
            // BackupRequestRecord and must not be treated as fresh here.
            self.observed
                .validate_fresh_witnesses(transaction.tempids.values().copied())?;
            let database = database.apply_committed_with_reserved_allocation(
                transaction,
                self.observed,
                after,
                allow_excision,
            )?;
            self.observed = after;
            self.modern = true;
            Ok(database)
        } else {
            if self.modern {
                return Err(fault(
                    "backup/allocation-version-regression",
                    "backup allocation format regresses from ATLC v2 to v1",
                ));
            }
            let database = if allow_excision {
                database.apply_excised_committed(transaction)?
            } else {
                database.apply_committed(transaction)?
            };
            self.observed.observe_transaction(transaction)?;
            Ok(database)
        }
    }
}

/// Deterministic interruption points used to prove root-last publication.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
#[doc(hidden)]
pub enum BackupFault {
    #[default]
    None,
    AfterGenerationPinned,
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
    /// Stop inside the first SQL staging batch after its content value is
    /// inserted but before its generation membership is inserted. The batch
    /// must roll back both sides and leave no ownerless immutable value.
    AfterFirstContentInserted,
    /// Stop after the inactive generation and its authenticated checkpoint
    /// are durable, but before the small root-publication transaction.
    BeforeCommit,
    /// Stop after the root transaction commits but before acknowledging it.
    AfterCommitBeforeResponse,
    /// Leave a published native root's bounded membership fold unfinished.
    AfterTreePublication,
}

pub struct PortableBackup {
    connection: PostgresConnectionConfig,
    client: Client,
    maintenance: crate::MaintenanceControl,
}

#[derive(Clone, Copy, Debug)]
struct BackupGenerationPin {
    generation: u64,
    key: i64,
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
        let client = connection.connect_for("backup/connect")?;
        Ok(Self {
            connection: connection.clone(),
            client,
            maintenance: crate::MaintenanceControl::default(),
        })
    }

    pub fn with_maintenance_control(mut self, control: crate::MaintenanceControl) -> Self {
        self.maintenance = control;
        self
    }

    /// Copy an authenticated immutable information point. Ordinary capture
    /// and existing-point authentication do not replay transaction semantics;
    /// damaged source request-base accelerators can require recovery. Use deep
    /// verification to prove semantics; restore does so before activation.
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
        self.maintenance.check()?;
        let identity = crate::database_catalog::resolve_name_in(&mut self.client, database_id)?;
        let database_id = identity.database_id.as_str();
        for attempt in 0..MAX_BACKUP_ATTEMPTS {
            if attempt > 0 {
                match self.connection.connect_for("backup/connect") {
                    Ok(client) => self.client = client,
                    Err(_error) if attempt + 1 < MAX_BACKUP_ATTEMPTS => continue,
                    Err(error) => return Err(error),
                }
            }
            match self.backup_database_once(database_id, directory, fault_at) {
                Ok(point) => return Ok(point),
                Err(error)
                    if fault_at == BackupFault::None
                        && attempt + 1 < MAX_BACKUP_ATTEMPTS
                        && crate::postgres::is_postgres_connection_error(&error) =>
                {
                    continue;
                }
                Err(error) => return Err(error),
            }
        }
        unreachable!("bounded backup attempt loop always returns")
    }

    fn backup_database_once(
        &mut self,
        database_id: &str,
        directory: &Path,
        fault_at: BackupFault,
    ) -> Result<BackupPoint, SemanticError> {
        self.backup_database_once_with_pin_probe(database_id, directory, fault_at, || {})
    }

    /// Test seam for exercising operations that must lose to a live backup.
    /// The callback runs after the real source-generation session pin is held
    /// and before the repeatable-read copy starts.
    #[doc(hidden)]
    pub fn backup_database_with_pin_probe<F>(
        &mut self,
        database_id: &str,
        directory: &Path,
        probe: F,
    ) -> Result<BackupPoint, SemanticError>
    where
        F: FnOnce(),
    {
        let identity = crate::database_catalog::resolve_name_in(&mut self.client, database_id)?;
        self.backup_database_once_with_pin_probe(
            &identity.database_id,
            directory,
            BackupFault::None,
            probe,
        )
    }

    fn backup_database_once_with_pin_probe<F>(
        &mut self,
        database_id: &str,
        directory: &Path,
        fault_at: BackupFault,
        probe: F,
    ) -> Result<BackupPoint, SemanticError>
    where
        F: FnOnce(),
    {
        // Serializing writers to one repository makes stale staging cleanup
        // safe: a retry never unlinks a file that another current backup is
        // still filling. The file lock is released automatically on every
        // return path, including injected failures and panics.
        let _directory_guard = prepare_directory(directory)?;
        // The repeatable-read snapshot makes SQL rows stable, while this
        // session lock keeps the selected immutable generation eligible for
        // reads until every reachable object and the root-last manifest are
        // durable. In particular, privacy collection cannot win the narrow
        // head-read-versus-pin race and erase source values mid-copy.
        let pin = acquire_backup_generation_pin(&mut self.client, database_id)?;
        if fault_at == BackupFault::AfterGenerationPinned {
            release_backup_generation_pin(&mut self.client, pin)?;
            return Err(injected("backup/after-generation-pinned"));
        }
        probe();
        let result = self.backup_database_pinned(database_id, directory, fault_at, pin);
        let release = release_backup_generation_pin(&mut self.client, pin);
        match (result, release) {
            (Ok(point), Ok(())) => Ok(point),
            (Ok(_), Err(error)) => Err(error),
            (Err(error), _) => Err(error),
        }
    }

    fn backup_database_pinned(
        &mut self,
        database_id: &str,
        directory: &Path,
        fault_at: BackupFault,
        pin: BackupGenerationPin,
    ) -> Result<BackupPoint, SemanticError> {
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
        let genesis_database = Database::from_genesis(decoded_genesis.clone())?;
        let genesis_state_hash = checkpoint_state_hash(&genesis_database)?;
        let genesis_frontier = genesis_database.eidx_frontier();
        let head = transaction
            .query_one(
                "SELECT basis_t, tx_hash, \
                        COALESCE((to_jsonb(atomic_heads)->>'log_generation')::bigint, 0) \
                   FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/head", error))?;
        let basis = unsigned(head.get(0), "head basis")?;
        let head_hash = digest(head.get(1), "head hash")?;
        let log_generation = unsigned(head.get(2), "head log generation")?;
        if log_generation != pin.generation {
            return Err(SemanticError::conflict(
                "backup/generation-race",
                "active log generation changed before the backup snapshot was pinned",
            ));
        }
        let parent = select_incremental_parent(
            directory,
            &lineage_id,
            genesis_hash,
            genesis_state_hash,
            log_generation,
            basis,
        )?;
        let prefix_basis = parent.as_ref().map_or(0, |parent| parent.manifest.basis);
        let start_basis = prefix_basis.checked_add(1).ok_or_else(|| {
            fault(
                "backup/basis-overflow",
                "incremental backup start basis overflowed",
            )
        })?;
        let expected_tail_len =
            usize::try_from(basis.saturating_sub(prefix_basis)).map_err(|_| {
                fault(
                    "backup/tail-too-large",
                    "incremental transaction tail is not representable",
                )
            })?;
        let (source_previous, portable_previous, prior_frontier) = if let Some(parent) = &parent {
            authenticate_source_parent(
                &mut transaction,
                directory,
                database_id,
                &lineage_id,
                genesis_hash,
                genesis_state_hash,
                genesis_frontier,
                parent,
            )?
        } else {
            (genesis_hash, genesis_hash, genesis_frontier)
        };
        let request_previous = parent
            .as_ref()
            .map_or(genesis_hash, |parent| parent.manifest.request_head_hash);
        let mut temporal_programs = BTreeSet::new();
        if parent.is_none() {
            for datom in &decoded_genesis {
                collect_function_hashes(&datom.value, &mut temporal_programs);
            }
        }
        let start_basis_sql = i64::try_from(start_basis).map_err(|_| {
            fault(
                "backup/basis-overflow",
                "incremental start basis exceeds PostgreSQL bigint",
            )
        })?;
        let basis_sql = i64::try_from(basis).map_err(|_| {
            fault(
                "backup/basis-overflow",
                "backup basis exceeds PostgreSQL bigint",
            )
        })?;
        let mut publisher = ObjectPublisher::new(directory, fault_at);
        publisher.maintenance = Some(self.maintenance.clone());
        if parent.is_none() {
            publisher.publish(genesis_hash, &genesis)?;
        }
        let captured = capture_log_tail(
            &mut transaction,
            database_id,
            &lineage_id,
            log_generation,
            start_basis,
            basis,
            start_basis_sql,
            basis_sql,
            expected_tail_len,
            source_previous,
            portable_previous,
            request_previous,
            prior_frontier,
            &mut temporal_programs,
            &mut publisher,
        )?;
        if head_hash != captured.source_head_hash {
            return Err(fault(
                "backup/head-mismatch",
                "head does not match snapshot chain",
            ));
        }
        let completed_excision_head_hash = capture_completed_excisions(
            &mut transaction,
            database_id,
            &lineage_id,
            log_generation,
            genesis_hash,
            &mut publisher,
        )?;
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
        let mut manifest = Manifest {
            version: VERSION,
            lineage_id,
            log_generation,
            basis,
            genesis_hash,
            head_transaction_hash: captured.portable_head_hash,
            head_state_hash: captured
                .state_hashes
                .last()
                .copied()
                .unwrap_or(genesis_state_hash),
            request_head_hash: captured.request_head_hash,
            completed_excision_head_hash,
            transactions: Vec::new(),
            state_hashes: Vec::new(),
            requests: Vec::new(),
            programs: Vec::new(),
            tree: None,
            read_log_index: None,
        };

        // A backup point is a logical (lineage, t), not whichever replaceable
        // physical index revision happened to be current during this retry.
        // A repaired tree can be republished at the same logical basis. If a
        // root already exists, authenticate it and prove the live snapshot
        // has the same transaction information
        // before reusing it. This check precedes physical-tree capture so a
        // harmless repair does not even leave an unreferenced backup object.
        if let Some(mut point) = reusable_existing_point(directory, &manifest)? {
            point.objects_written = publisher.written;
            point.objects_reused = publisher.reused;
            transaction.commit().map_err(|error| {
                crate::postgres::postgres_error("backup/snapshot-commit", error)
            })?;
            return Ok(point);
        }
        let tree_log = TreeCaptureLog {
            start_basis,
            backup_basis: basis,
            source_transaction_hashes: &captured.source_transaction_hashes,
            portable_transaction_hashes: &captured.portable_transaction_hashes,
            portable_frontiers: &captured.portable_frontiers,
            state_hashes: &captured.state_hashes,
        };
        let tree = capture_tree_backup(
            &mut transaction,
            database_id,
            &manifest.lineage_id,
            log_generation,
            &tree_log,
            &mut publisher,
        )?;
        let (read_tree, reserved_allocation) = capture_exact_read_tree(
            &self.connection,
            database_id,
            &manifest,
            captured.source_head_hash,
            tree,
            &mut publisher,
        )?;
        manifest.tree = Some(read_tree);
        manifest.read_log_index = Some(capture_read_log_index(
            parent.as_ref().and_then(|p| p.manifest.read_log_index),
            start_basis,
            &captured.portable_transaction_hashes,
            reserved_allocation,
            &mut publisher,
        )?);
        transaction
            .commit()
            .map_err(|error| crate::postgres::postgres_error("backup/snapshot-commit", error))?;

        if fault_at == BackupFault::AfterObjects {
            return Err(injected("backup/after-objects"));
        }
        let encoded = encode_manifest(&manifest)?;
        let manifest_hash = sha256(&encoded);
        let snapshot_path = snapshot_path(directory, basis, manifest.log_generation);
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
            // that race by the same full object authentication used by an ordinary
            // retry, never by replacing its root.
            if error.category != ErrorCategory::Interrupted {
                match reusable_existing_point(directory, &manifest) {
                    Ok(Some(mut point)) => {
                        point.objects_written = publisher.written;
                        point.objects_reused = publisher.reused;
                        return Ok(point);
                    }
                    Ok(None) => {}
                    Err(resolution) if error.code == "backup/file-conflict" => {
                        return Err(resolution);
                    }
                    Err(_) => {}
                }
            }
            return Err(error);
        }
        Ok(BackupPoint {
            lineage_id: manifest.lineage_id.clone(),
            log_generation: manifest.log_generation,
            basis_t: basis,
            manifest_hash,
            objects_written: publisher.written,
            objects_reused: publisher.reused,
        })
    }

    /// List every retained immutable root. Multiple entries can have the same
    /// logical `basis_t`: an excision publishes a distinct information value
    /// in a later generation without deleting the privacy-sensitive earlier
    /// root. Operators must be able to see that residue explicitly.
    pub fn list_backup_points(directory: &Path) -> Result<Vec<BackupPoint>, SemanticError> {
        validate_backup_directory(directory, true)?;
        let mut points = Vec::new();
        for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list"))? {
            let entry = entry.map_err(io_error("backup/list-entry"))?;
            let Some((basis, generation)) = parse_snapshot_name(&entry.file_name())? else {
                continue;
            };
            require_regular_file(&entry.path(), "backup/root-type")?;
            let bytes = backup_file::read_manifest_file(&entry.path())?;
            let manifest = decode_manifest(&bytes)?;
            if manifest.basis != basis || manifest.log_generation != generation {
                return Err(fault(
                    "backup/root-name",
                    "snapshot filename does not identify its manifest generation and basis",
                ));
            }
            verify_claim(directory, &manifest)?;
            points.push(BackupPoint {
                lineage_id: manifest.lineage_id,
                log_generation: manifest.log_generation,
                basis_t: manifest.basis,
                manifest_hash: sha256(&bytes),
                objects_written: 0,
                objects_reused: 0,
            });
        }
        points.sort_by_key(|point| (point.basis_t, point.log_generation));
        Ok(points)
    }

    /// Convenience logical-time view. When excision retained more than one
    /// root at a basis, this deliberately returns that basis once; use
    /// `list_backup_points` for privacy inventory and exact coordinates.
    pub fn list_backups(directory: &Path) -> Result<Vec<u64>, SemanticError> {
        let mut bases = Self::list_backup_points(directory)?
            .into_iter()
            .map(|point| point.basis_t)
            .collect::<Vec<_>>();
        bases.dedup();
        Ok(bases)
    }

    /// Validate the constant root and all named log/program values. Tree
    /// routing nodes are read to discover children, while leaf payloads are
    /// checked only by file metadata. This is the native `read-all=false`
    /// boundary; linked log values must be read because they carry their own
    /// predecessor references.
    pub fn verify_backup_presence(
        directory: &Path,
        basis: u64,
    ) -> Result<BackupPoint, SemanticError> {
        validate_backup_directory(directory, true)?;
        let (manifest, manifest_hash) = load_manifest(directory, basis)?;
        Self::verify_loaded_backup_presence(directory, manifest, manifest_hash)
    }

    /// Presence-check one exact retained information generation.
    pub fn verify_backup_point_presence(
        directory: &Path,
        basis: u64,
        log_generation: u64,
    ) -> Result<BackupPoint, SemanticError> {
        validate_backup_directory(directory, true)?;
        let (manifest, manifest_hash) = load_manifest_generation(directory, basis, log_generation)?;
        Self::verify_loaded_backup_presence(directory, manifest, manifest_hash)
    }

    fn verify_loaded_backup_presence(
        directory: &Path,
        manifest: Manifest,
        manifest_hash: Digest,
    ) -> Result<BackupPoint, SemanticError> {
        verify_claim(directory, &manifest)?;
        let log = load_backup_log(directory, &manifest)?;
        backup_read::verify_read_log_index(directory, &manifest, &log)?;
        load_completed_excisions(directory, &manifest)?;
        verify_program_presence(directory, &manifest, &log)?;
        if let Some(tree) = &manifest.tree {
            verify_tree_presence(directory, &manifest, &log, tree)?;
        }
        for tree in request_base_trees(&log) {
            verify_tree_presence(directory, &manifest, &log, &tree)?;
        }
        Ok(BackupPoint {
            lineage_id: manifest.lineage_id,
            log_generation: manifest.log_generation,
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
        validate_backup_directory(directory, true)?;
        let (manifest, manifest_hash) = load_manifest(directory, basis)?;
        Self::verify_loaded_backup(directory, manifest, manifest_hash, deep)
            .map(|verified| verified.verification)
    }

    /// Verify one exact retained information generation. The basis-only
    /// convenience above intentionally selects the latest generation.
    pub fn verify_backup_point(
        directory: &Path,
        basis: u64,
        log_generation: u64,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
        validate_backup_directory(directory, true)?;
        let (manifest, manifest_hash) = load_manifest_generation(directory, basis, log_generation)?;
        Self::verify_loaded_backup(directory, manifest, manifest_hash, deep)
            .map(|verified| verified.verification)
    }

    fn verify_loaded_backup(
        directory: &Path,
        manifest: Manifest,
        manifest_hash: Digest,
        deep: bool,
    ) -> Result<VerifiedBackupSource, SemanticError> {
        verify_claim(directory, &manifest)?;
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
        database.entity_origin =
            crate::entity_identity::DatabaseOrigin::durable(&manifest.lineage_id);
        let mut allocation_replay = BackupAllocationReplay::new(&database)?;
        let mut previous = manifest.genesis_hash;
        let mut current_state_hash = checkpoint_state_hash(&database)?;
        let log = load_backup_log(directory, &manifest)?;
        let mut objects_read =
            1 + log.objects_read + backup_read::verify_read_log_index(directory, &manifest, &log)?;
        // Check each accelerator while the single authoritative replay is at
        // its exact endpoint. Receipt archives can name many earlier bases;
        // retaining one Database clone per archive would make deep verification
        // grow with the number of endpoints as well as the database size.
        let mut trees_by_basis = BTreeMap::<u64, Vec<TreeBackup>>::new();
        let mut verified_main_tree = None;
        if deep {
            let mut trees = request_base_trees(&log);
            if let Some(tree) = &manifest.tree {
                trees.push(tree.clone());
            }
            for tree in trees {
                let decoded = decode_bound_tree_manifest(directory, &manifest, &log, &tree, None)?;
                objects_read = objects_read.saturating_add(1);
                let at_basis = trees_by_basis.entry(decoded.basis_t).or_default();
                if !at_basis
                    .iter()
                    .any(|existing| existing.manifest_hash == tree.manifest_hash)
                {
                    at_basis.push(tree);
                }
            }
        }
        if let Some(trees) = trees_by_basis.remove(&0) {
            for tree in trees {
                objects_read = objects_read.saturating_add(verify_tree_backup(
                    directory,
                    &manifest,
                    &log,
                    &tree,
                    &database,
                    current_state_hash,
                )?);
                if manifest
                    .tree
                    .as_ref()
                    .is_some_and(|main| main.manifest_hash == tree.manifest_hash)
                {
                    verified_main_tree = Some(VerifiedMainTree {
                        manifest_hash: tree.manifest_hash,
                        basis_t: database.basis_t(),
                        state_hash: current_state_hash,
                        eidx_frontier: database.eidx_frontier(),
                        schema: database.schema_arc(),
                    });
                }
            }
        }
        for entry in &log.entries {
            for datom in &entry.transaction.tx_data {
                collect_function_hashes(&datom.value, &mut required_programs);
            }
            if entry.transaction.previous_hash != previous {
                return Err(fault(
                    "backup/invalid-chain",
                    "backup transaction predecessor is invalid",
                ));
            }
            previous = entry.transaction_hash;
            let allow_excision = if manifest.version == VERSION && manifest.log_generation > 0 {
                match entry.request.request_kind {
                    0 => true,
                    1 | 2 => false,
                    _ => {
                        return Err(fault(
                            "backup/request-kind",
                            "backup transaction has an invalid request kind",
                        ));
                    }
                }
            } else {
                false
            };
            database = allocation_replay.apply(
                &database,
                &entry.transaction,
                entry.reserved_frontier,
                allow_excision,
            )?;
            current_state_hash = checkpoint_state_hash(&database)?;
            if let Some(trees) = trees_by_basis.remove(&entry.transaction.basis_t) {
                for tree in trees {
                    objects_read = objects_read.saturating_add(verify_tree_backup(
                        directory,
                        &manifest,
                        &log,
                        &tree,
                        &database,
                        current_state_hash,
                    )?);
                    if manifest
                        .tree
                        .as_ref()
                        .is_some_and(|main| main.manifest_hash == tree.manifest_hash)
                    {
                        verified_main_tree = Some(VerifiedMainTree {
                            manifest_hash: tree.manifest_hash,
                            basis_t: database.basis_t(),
                            state_hash: current_state_hash,
                            eidx_frontier: database.eidx_frontier(),
                            schema: database.schema_arc(),
                        });
                    }
                }
            }
            if entry
                .legacy_state_hash
                .is_some_and(|expected| expected != current_state_hash)
            {
                return Err(fault(
                    "backup/state-commitment",
                    format!(
                        "backup transaction {} has the wrong state commitment",
                        entry.transaction.basis_t
                    ),
                ));
            }
        }
        if database.basis_t() != manifest.basis
            || previous != manifest.head_transaction_hash
            || (manifest.version == VERSION && current_state_hash != manifest.head_state_hash)
        {
            return Err(fault(
                "backup/basis-mismatch",
                "backup reconstruction disagrees with its endpoint root",
            ));
        }
        if !trees_by_basis.is_empty() {
            return Err(fault(
                "backup/tree-basis",
                "backed-up tree metadata is outside the reconstructed log range",
            ));
        }
        let completed_excisions = load_completed_excisions(directory, &manifest)?;
        let request_identities = database
            .historical_excision_requests()?
            .into_iter()
            .map(|request| (request.request_t, request.request_entity))
            .collect::<BTreeSet<_>>();
        if completed_excisions
            .iter()
            .any(|identity| !request_identities.contains(identity))
        {
            return Err(fault(
                "backup/completed-excision-fact",
                "completed excision identity has no historical A=15 request fact",
            ));
        }
        objects_read = objects_read.saturating_add(completed_excisions.len());
        let programs = load_program_graph(directory, required_programs)?;
        objects_read += programs.len();
        verify_legacy_program_declarations(&manifest, &programs)?;
        if let Some(tree) = &manifest.tree
            && !deep
        {
            verify_tree_presence(directory, &manifest, &log, tree)?;
        }
        for tree in request_base_trees(&log) {
            if !deep {
                verify_tree_presence(directory, &manifest, &log, &tree)?;
            }
        }
        // Transitions check immutable input semantics and each recorded
        // commitment above. Audit the derived caches/history once at this
        // explicit recovery endpoint instead of rebuilding every prefix.
        database.validate_invariants()?;
        Ok(VerifiedBackupSource {
            main_tree: verified_main_tree,
            verification: BackupVerification {
                point: BackupPoint {
                    lineage_id: manifest.lineage_id.clone(),
                    log_generation: manifest.log_generation,
                    basis_t: manifest.basis,
                    manifest_hash,
                    objects_written: 0,
                    objects_reused: 0,
                },
                database,
                objects_read,
            },
        })
    }

    pub fn restore_backup(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
    ) -> Result<Database, SemanticError> {
        self.restore_backup_selected(
            directory,
            basis,
            None,
            target_database_id,
            RestoreFault::None,
            None,
            None,
        )
    }

    /// Restore one exact retained information generation. The basis-only
    /// convenience deliberately chooses the latest generation at that basis.
    pub fn restore_backup_point(
        &mut self,
        directory: &Path,
        basis: u64,
        log_generation: u64,
        target_database_id: &str,
    ) -> Result<Database, SemanticError> {
        self.restore_backup_selected(
            directory,
            basis,
            Some(log_generation),
            target_database_id,
            RestoreFault::None,
            None,
            None,
        )
    }

    #[doc(hidden)]
    pub fn restore_backup_with_fault(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
        fault_at: RestoreFault,
    ) -> Result<Database, SemanticError> {
        self.restore_backup_selected(
            directory,
            basis,
            None,
            target_database_id,
            fault_at,
            None,
            None,
        )
    }

    /// Test seam at the dangerous builder-to-publication lock handoff. The
    /// callback runs after the restore serialization lock is held and the
    /// shared builder pin has been released, but before root publication.
    #[doc(hidden)]
    pub fn restore_backup_with_activation_probe<F>(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
        mut probe: F,
    ) -> Result<Database, SemanticError>
    where
        F: FnMut(),
    {
        self.restore_backup_selected(
            directory,
            basis,
            None,
            target_database_id,
            RestoreFault::None,
            Some(&mut probe),
            None,
        )
    }

    /// Test seam after exact target-row proof and optional tree publication,
    /// immediately before the final locked identity/head comparison.
    #[doc(hidden)]
    pub fn restore_backup_with_completion_probe<F>(
        &mut self,
        directory: &Path,
        basis: u64,
        target_database_id: &str,
        mut probe: F,
    ) -> Result<Database, SemanticError>
    where
        F: FnMut(),
    {
        self.restore_backup_selected(
            directory,
            basis,
            None,
            target_database_id,
            RestoreFault::None,
            None,
            Some(&mut probe),
        )
    }

    #[allow(clippy::too_many_arguments)]
    fn restore_backup_selected(
        &mut self,
        directory: &Path,
        basis: u64,
        log_generation: Option<u64>,
        target_database_id: &str,
        fault_at: RestoreFault,
        activation_probe: Option<&mut dyn FnMut()>,
        completion_probe: Option<&mut dyn FnMut()>,
    ) -> Result<Database, SemanticError> {
        self.maintenance.check()?;
        if target_database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "backup/empty-target",
                "restore target cannot be empty",
            ));
        }
        validate_backup_directory(directory, true)?;
        let (source_manifest, source_manifest_hash) = match log_generation {
            Some(generation) => load_manifest_generation(directory, basis, generation)?,
            None => load_manifest(directory, basis)?,
        };
        let verified =
            Self::verify_loaded_backup(directory, source_manifest, source_manifest_hash, true)?;
        let verification = &verified.verification;
        let (manifest, manifest_hash) = match log_generation {
            Some(generation) => load_manifest_generation(directory, basis, generation)?,
            None => load_manifest(directory, basis)?,
        };
        if manifest_hash != verification.point.manifest_hash {
            return Err(fault(
                "backup/root-changed",
                "published backup root changed after verification",
            ));
        }
        let log = load_backup_log(directory, &manifest)?;
        // Restore never provisions or upgrades its target. The deployment
        // must have run the explicit migrator before constructing this
        // operator; re-check at the operation boundary to fail closed if the
        // catalog changed since connection.
        drop(PostgresStore::connect_configured(&self.connection)?);
        let genesis = read_object(directory, manifest.genesis_hash)?;
        let genesis_hash = sha256(&genesis);
        let decoded_genesis = decode_genesis(&genesis)?;
        let mut required_programs = BTreeSet::new();
        for datom in &decoded_genesis {
            collect_function_hashes(&datom.value, &mut required_programs);
        }
        for entry in &log.entries {
            for datom in &entry.transaction.tx_data {
                collect_function_hashes(&datom.value, &mut required_programs);
            }
        }
        let restored_programs = load_program_graph(directory, required_programs)?;
        let completed_excisions = load_completed_excisions(directory, &manifest)?;
        let target_storage_id = ensure_restore_target(
            &mut self.client,
            &manifest,
            target_database_id,
            &genesis,
            genesis_hash,
        )?;
        // Resolve the public target once. Canonical artifacts and every later
        // retry/build operation use this immutable storage identity, not a
        // mutable name that another operator can rename or reuse.
        let target_database_id = target_storage_id.as_str();
        if let Some(matched_target) = target_matches_backup(
            &mut self.client,
            directory,
            &manifest,
            &log,
            &completed_excisions,
            target_database_id,
            &verification.database,
        )? {
            // Exact replay after an ambiguous acknowledgement is safe and
            // leaves the already-published target untouched.
            cleanup_active_restore_build_if_present(&mut self.client, target_database_id)?;
            return complete_restored_target(
                &self.connection,
                directory,
                &manifest,
                &log,
                target_database_id,
                &verification.database,
                verified.main_tree.as_ref(),
                &matched_target,
                fault_at,
                completion_probe,
            );
        }

        // Decode values before taking the builder pin, then durably create the
        // candidate ownership root before uploading any of them. Each content
        // value commits in the same batch as its generation membership, and
        // each program commits with its generation mark. None is observable
        // through the active head until the final root transaction.
        let rows = prepare_restore_rows(directory, &manifest, &log)?;
        let build_pin = acquire_restore_build_pin(&mut self.client, target_database_id)?;
        let staged = (|| {
            let candidate = ensure_restore_candidate(
                &mut self.client,
                &manifest,
                manifest_hash,
                target_database_id,
            )?;
            stage_restored_programs(
                &mut self.client,
                &restored_programs,
                target_database_id,
                candidate.generation,
            )?;
            let restored = stage_restore_generation(
                &mut self.client,
                &manifest,
                &rows,
                &completed_excisions,
                target_database_id,
                candidate,
                fault_at,
                &self.maintenance,
            )?;
            stage_restore_semantic_coordinates(
                &mut self.client,
                directory,
                &manifest,
                &rows,
                target_database_id,
                candidate,
            )?;
            restore_request_base_trees(
                &self.connection,
                &mut self.client,
                directory,
                &manifest,
                &log,
                target_database_id,
                candidate.generation,
            )?;
            restore_legacy_receipt_trees(
                &self.connection,
                &mut self.client,
                directory,
                &manifest,
                &log,
                &rows,
                target_database_id,
                candidate.generation,
            )?;
            Ok((candidate, restored))
        })();
        let (candidate, restored) = match staged {
            Ok(value) => value,
            Err(error) => {
                // The staging error remains primary, but the release helper
                // performs ambiguity-safe session cleanup before we return.
                let _ = release_restore_build_pin(&mut self.client, build_pin);
                return Err(error);
            }
        };
        if fault_at == RestoreFault::BeforeCommit {
            release_restore_build_pin(&mut self.client, build_pin)?;
            return Err(injected("backup/restore-before-activation"));
        }
        activate_restore_candidate(
            &mut self.client,
            &manifest,
            target_database_id,
            candidate,
            &restored,
            build_pin,
            activation_probe,
        )?;
        crate::change_notices::publish(&self.connection, target_database_id);
        if fault_at == RestoreFault::AfterCommitBeforeResponse {
            return Err(injected("backup/restore-after-activation"));
        }
        let Some(matched_target) = target_matches_backup(
            &mut self.client,
            directory,
            &manifest,
            &log,
            &completed_excisions,
            target_database_id,
            &verification.database,
        )?
        else {
            return Err(fault(
                "backup/restore-postcondition",
                "restored authoritative rows do not exactly match the requested backup point",
            ));
        };
        complete_restored_target(
            &self.connection,
            directory,
            &manifest,
            &log,
            target_database_id,
            &verification.database,
            verified.main_tree.as_ref(),
            &matched_target,
            fault_at,
            completion_probe,
        )
    }
}

fn acquire_backup_generation_pin(
    client: &mut Client,
    database_id: &str,
) -> Result<BackupGenerationPin, SemanticError> {
    for _ in 0..MAX_BACKUP_ATTEMPTS {
        let row = client
            .query_opt(
                "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/pin-head", error))?
            .ok_or_else(|| not_found(database_id))?;
        let generation = unsigned(row.get(0), "backup pin generation")?;
        let generation_sql = sql_u64(generation, "backup pin generation")?;
        let key: Option<i64> = client
            .query_one(
                "SELECT atomic_log_generation_pin_key($1, $2)",
                &[&database_id, &generation_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/pin-key", error))?
            .get(0);
        let Some(key) = key else {
            return Err(not_found(database_id));
        };
        if let Err(error) = client.query_one("SELECT pg_advisory_lock_shared($1)", &[&key]) {
            let original = crate::postgres::postgres_error("backup/pin-acquire", error);
            // A lost acknowledgement does not tell us whether PostgreSQL
            // acquired the session lock. Clear the session before returning;
            // a dead connection has already released it server-side.
            let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
            return Err(original);
        }
        let still_active: bool = match client.query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_heads h \
                              JOIN atomic_database_identities i USING(database_id) \
                              WHERE h.database_id = $1 AND h.log_generation = $2 \
                                AND i.retired_at IS NULL)",
            &[&database_id, &generation_sql],
        ) {
            Ok(row) => row.get(0),
            Err(error) => {
                let original = crate::postgres::postgres_error("backup/pin-verify", error);
                // Session advisory locks survive statement errors. Always
                // unwind the pin before returning a still-live connection;
                // if the session itself died PostgreSQL releases its locks.
                let released = client
                    .query_one("SELECT pg_advisory_unlock_shared($1)", &[&key])
                    .is_ok_and(|row| row.get::<_, bool>(0));
                if !released {
                    let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
                }
                return Err(original);
            }
        };
        if still_active {
            return Ok(BackupGenerationPin { generation, key });
        }
        let unlocked: bool = client
            .query_one("SELECT pg_advisory_unlock_shared($1)", &[&key])
            .map_err(|error| crate::postgres::postgres_error("backup/pin-race-release", error))?
            .get(0);
        if !unlocked {
            return Err(fault(
                "backup/pin-race-release",
                "backup generation pin disappeared while resolving a head race",
            ));
        }
    }
    Err(SemanticError::conflict(
        "backup/generation-race",
        "active log generation changed repeatedly while acquiring the backup pin",
    ))
}

fn release_backup_generation_pin(
    client: &mut Client,
    pin: BackupGenerationPin,
) -> Result<(), SemanticError> {
    let unlocked: bool = match client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&pin.key])
    {
        Ok(row) => row.get(0),
        Err(error) => {
            let original = crate::postgres::postgres_error("backup/pin-release", error);
            // A statement-level failure can leave a session advisory lock
            // alive. This operator session owns no unrelated long-lived
            // locks, so fail closed and clear its lock set before reuse.
            let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
            return Err(original);
        }
    };
    if unlocked {
        Ok(())
    } else {
        Err(fault(
            "backup/pin-release",
            "backup generation pin was not held by its source session",
        ))
    }
}

fn acquire_restore_build_pin(
    client: &mut Client,
    target_database_id: &str,
) -> Result<i64, SemanticError> {
    let key: Option<i64> = client
        .query_one(
            "SELECT atomic_tree_database_build_pin_key($1)",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-build-pin-key", error))?
        .get(0);
    let key = key.ok_or_else(|| not_found(target_database_id))?;
    if let Err(error) = client.query_one("SELECT pg_advisory_lock_shared($1)", &[&key]) {
        let original = crate::postgres::postgres_error("backup/restore-build-pin-acquire", error);
        let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
        return Err(original);
    }
    Ok(key)
}

fn release_restore_build_pin(client: &mut Client, key: i64) -> Result<(), SemanticError> {
    let unlocked: bool = match client.query_one("SELECT pg_advisory_unlock_shared($1)", &[&key]) {
        Ok(row) => row.get(0),
        Err(error) => {
            let original =
                crate::postgres::postgres_error("backup/restore-build-pin-release", error);
            let _ = client.batch_execute("SELECT pg_advisory_unlock_all()");
            return Err(original);
        }
    };
    if unlocked {
        Ok(())
    } else {
        Err(fault(
            "backup/restore-build-pin-release",
            "restore builder pin was not held by its staging session",
        ))
    }
}

struct RestoredGenerationHead {
    head_hash: Digest,
    state_hash: Digest,
}

#[derive(Clone, Copy, Debug)]
struct RestoreCandidate {
    generation: u64,
    initial: bool,
}

#[derive(Clone, Debug)]
struct PreparedRestoreRow {
    basis_t: u64,
    eidx_frontier: u64,
    content_hash: Digest,
    content_payload: Vec<u8>,
    state_hash: Digest,
    request: BackupRequestRecord,
}

#[derive(Clone, Debug)]
struct PreparedRestore {
    rows: Vec<PreparedRestoreRow>,
    genesis_state_hash: Digest,
    genesis_frontier: u64,
}

fn ensure_restore_target(
    client: &mut Client,
    manifest: &Manifest,
    target_database_id: &str,
    genesis: &[u8],
    genesis_hash: Digest,
) -> Result<String, SemanticError> {
    let mut transaction = client
        .transaction()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-catalog-begin", error))?;
    crate::database_catalog::lock_names(&mut transaction, &[target_database_id])?;
    if let Some(target) = transaction
        .query_opt(
            "SELECT d.lineage_id,d.genesis,d.genesis_hash,d.database_id,i.retired_at IS NOT NULL \
             FROM atomic_database_names n JOIN atomic_database_identities i USING(database_id) \
             JOIN atomic_databases d USING(database_id) WHERE n.name=$1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-target", error))?
    {
        if target.get::<_, bool>(4) {
            return Err(SemanticError::conflict(
                "backup/retired-lineage-requires-reclamation",
                "retired database storage must be fully reclaimed before this lineage is restored in the same catalog",
            ));
        }
        if target.get::<_, String>(0) != manifest.lineage_id
            || target.get::<_, Vec<u8>>(1) != genesis
            || digest(target.get(2), "restore target genesis hash")? != genesis_hash
        {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "backup/target-lineage-conflict",
                "restore target belongs to another database lineage",
            ));
        }
        let storage_id = target.get::<_, String>(3);
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-catalog-commit", error)
        })?;
        return Ok(storage_id);
    }

    if let Some(existing) = transaction
        .query_opt(
            "SELECT i.retired_at IS NOT NULL FROM atomic_databases d \
         JOIN atomic_database_identities i USING(database_id) WHERE d.lineage_id=$1",
            &[&manifest.lineage_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-lineage", error))?
    {
        let retired: bool = existing.get(0);
        return Err(SemanticError::conflict(
            if retired {
                "backup/retired-lineage-requires-reclamation"
            } else {
                "backup/lineage-exists"
            },
            if retired {
                "retired database storage must be fully reclaimed before restoring its lineage; a different catalog remains an independent restore target"
            } else {
                "this database lineage already has a name in the target catalog"
            },
        ));
    }
    let storage_id =
        crate::database_catalog::allocate_storage_id_in(&mut transaction, target_database_id)?;

    transaction
        .execute(
            "INSERT INTO atomic_databases (database_id, lineage_id, genesis, genesis_hash) \
             VALUES ($1, $2, $3, $4)",
            &[
                &storage_id,
                &manifest.lineage_id,
                &genesis,
                &&manifest.genesis_hash[..],
            ],
        )
        .map_err(restore_catalog_error)?;
    crate::database_catalog::register_new_in(
        &mut transaction,
        target_database_id,
        &storage_id,
        &manifest.lineage_id,
    )?;
    // Do not publish a synthetic genesis head. Until the archive has been
    // completely staged and its first head is installed atomically, runtime
    // opens see no database value and therefore fail closed.
    transaction
        .commit()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-catalog-commit", error))?;
    Ok(storage_id)
}

fn prepare_restore_rows(
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
) -> Result<PreparedRestore, SemanticError> {
    let genesis = read_object(directory, manifest.genesis_hash)?;
    let genesis_database = Database::from_genesis(decode_genesis(&genesis)?)?;
    let genesis_state_hash = checkpoint_state_hash(&genesis_database)?;
    let genesis_frontier = genesis_database.eidx_frontier();
    let mut prior_frontier = genesis_frontier;
    let mut rows = Vec::with_capacity(log.entries.len());
    for entry in &log.entries {
        let (content_hash, content_payload, content) =
            if let Some(content_hash) = entry.content_hash {
                let payload = read_object(directory, content_hash)?;
                let content = LineageTransactionContent::decode(&payload)?;
                if sha256(&payload) != content_hash {
                    return Err(fault(
                        "backup/restore-content-hash",
                        "backup transaction content failed its digest during restore",
                    ));
                }
                (content_hash, payload, content)
            } else {
                let content = LineageTransactionContent::from_transaction(
                    &manifest.lineage_id,
                    prior_frontier,
                    &entry.transaction,
                )?;
                let payload = content.encode()?;
                (sha256(&payload), payload, content)
            };
        let matches_transaction = if entry.content_hash.is_some() {
            content.to_transaction(entry.transaction.previous_hash) == entry.transaction
                && content.reserved_frontier == entry.reserved_frontier
        } else {
            content
                == LineageTransactionContent::from_transaction(
                    &manifest.lineage_id,
                    prior_frontier,
                    &entry.transaction,
                )?
        };
        if !matches_transaction {
            return Err(fault(
                "backup/restore-content-binding",
                "backup content and reconstructed transaction disagree",
            ));
        }
        let state_hash = entry.legacy_state_hash.ok_or_else(|| {
            fault(
                "backup/restore-state",
                "backup transaction has no authenticated state commitment",
            )
        })?;
        rows.push(PreparedRestoreRow {
            basis_t: content.basis_t,
            eidx_frontier: content.eidx_frontier,
            content_hash,
            content_payload,
            state_hash,
            request: entry.request.clone(),
        });
        prior_frontier = content.eidx_frontier;
    }
    Ok(PreparedRestore {
        rows,
        genesis_state_hash,
        genesis_frontier,
    })
}

fn stage_restored_programs(
    client: &mut Client,
    programs: &BTreeMap<Digest, (Program, Vec<u8>)>,
    target_database_id: &str,
    generation: u64,
) -> Result<(), SemanticError> {
    let generation_sql = sql_u64(generation, "restored program generation")?;
    let programs = programs.iter().collect::<Vec<_>>();
    for chunk in programs.chunks(RESTORE_BATCH_ROWS) {
        let mut transaction = client.transaction().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-program-begin", error)
        })?;
        for (program_hash, (program, payload)) in chunk {
            install_restored_program(&mut transaction, program_hash, program, payload)?;
            // Candidate creation precedes uploads. Install the content and
            // its exact generation mark atomically so an interrupted restore
            // is either retained by this build or remains only as a
            // pre-existing globally shared value. The ordinary age-gated
            // program candidate ledger handles deletion after abandonment
            // removes the last generation mark.
            transaction
                .execute(
                    "INSERT INTO atomic_program_generation_refs \
                         (database_id, log_generation, program_hash) \
                     VALUES ($1, $2, $3) ON CONFLICT DO NOTHING",
                    &[&target_database_id, &generation_sql, &&program_hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-program-reference", error)
                })?;
        }
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-program-commit", error)
        })?;
    }
    Ok(())
}

fn install_restored_content<C: crate::sql_io::GenericClient>(
    client: &mut C,
    manifest: &Manifest,
    row: &PreparedRestoreRow,
) -> Result<(), SemanticError> {
    let basis_sql = sql_u64(row.basis_t, "restored content basis")?;
    let frontier_sql = sql_u64(row.eidx_frontier, "restored content frontier")?;
    client
        .execute(
            "INSERT INTO atomic_transaction_contents \
                         (content_hash, lineage_id, basis_t, eidx_frontier, payload) \
                     VALUES ($1, $2, $3, $4, $5) \
                     ON CONFLICT (content_hash) DO NOTHING",
            &[
                &&row.content_hash[..],
                &manifest.lineage_id,
                &basis_sql,
                &frontier_sql,
                &&row.content_payload[..],
            ],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-content-insert", error))?;
    let stored = client
        .query_one(
            "SELECT lineage_id, basis_t, eidx_frontier, envelope_version, payload \
                       FROM atomic_transaction_contents WHERE content_hash = $1",
            &[&&row.content_hash[..]],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-content-verify", error))?;
    if stored.get::<_, String>(0) != manifest.lineage_id
        || stored.get::<_, i64>(1) != basis_sql
        || stored.get::<_, i64>(2) != frontier_sql
        || stored.get::<_, i16>(3)
            != i16::try_from(LineageTransactionContent::decode(&row.content_payload)?.version())
                .expect("ATLC version fits i16")
        || stored.get::<_, Vec<u8>>(4) != row.content_payload
    {
        return Err(fault(
            "backup/restore-content-conflict",
            "content hash resolves to different immutable transaction information",
        ));
    }
    Ok(())
}

fn ensure_restore_candidate(
    client: &mut Client,
    manifest: &Manifest,
    manifest_hash: Digest,
    target_database_id: &str,
) -> Result<RestoreCandidate, SemanticError> {
    let mut transaction = client.transaction().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-candidate-begin", error)
    })?;
    transaction
        .query_one(
            "SELECT pg_advisory_xact_lock(hashtextextended('atomic/restore/' || $1, 0))",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-lock", error))?;
    // Every generation coordinate is local to one durable database lineage.
    // This row lock is shared with create/excision allocators, so unrelated
    // databases cannot perturb ordering and two builders cannot choose the
    // same successor.
    transaction
        .query_one(
            "SELECT 1 FROM atomic_databases WHERE database_id = $1 FOR UPDATE",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-database-lock", error))?;
    let current = transaction
        .query_opt(
            "SELECT basis_t, tx_hash, log_generation FROM atomic_heads \
             WHERE database_id = $1 FOR UPDATE",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-current-head", error))?;
    let current_coordinate = current
        .as_ref()
        .map(|row| {
            Ok((
                row.get::<_, i64>(0),
                digest(row.get(1), "restore current head hash")?,
                row.get::<_, i64>(2),
            ))
        })
        .transpose()?;
    if current_coordinate.is_none() {
        let abandonment_claimed: bool = transaction
            .query_one(
                "SELECT EXISTS ( \
                     SELECT 1 FROM atomic_log_generation_abandonment_progress \
                      WHERE database_id = $1)",
                &[&target_database_id],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-abandonment-claim", error)
            })?
            .get(0);
        if abandonment_claimed {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "backup/restore-target-abandoning",
                "headless restore target is being reclaimed; retry after abandonment completes",
            ));
        }
    }
    let builds = transaction
        .query(
            "SELECT g.generation, g.build_kind, b.source_generation, b.captured_basis_t, \
                    b.captured_head_hash, b.frozen_plan_hash, b.restore_manifest_hash, \
                    b.restore_basis_t, b.restore_head_hash \
               FROM atomic_log_generation_builds b \
               JOIN atomic_log_generations g \
                 ON g.database_id = b.database_id AND g.generation = b.generation \
              WHERE b.database_id = $1 \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                 WHERE a.database_id = b.database_id \
                                   AND a.generation = b.generation) \
                AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress p \
                                 WHERE p.database_id = b.database_id \
                                   AND p.generation = b.generation) \
              ORDER BY g.generation",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-existing-build", error))?;
    let mut matching_build = None;
    let mut conflicting_restore = false;
    for build in &builds {
        let kind = build.get::<_, i16>(1);
        // An inactive excision build is independent work. A restore can
        // supersede its captured source atomically; afterward the loser is a
        // legitimate abandonment candidate. Treating it as a global mutex
        // needlessly wedges recovery. Restore builds are different: two
        // requested points must not race under one database alias.
        if current_coordinate.is_some() && kind == 1 {
            continue;
        }
        let frozen_plan = build
            .get::<_, Option<Vec<u8>>>(5)
            .map(|hash| digest(hash, "initial restore manifest"))
            .transpose()?;
        let build_manifest = build
            .get::<_, Option<Vec<u8>>>(6)
            .map(|hash| digest(hash, "restore build manifest"))
            .transpose()?;
        let build_head = build
            .get::<_, Option<Vec<u8>>>(8)
            .map(|hash| digest(hash, "restore build head"))
            .transpose()?;
        let initial_matches = current_coordinate.is_none()
            && kind == 0
            && build.get::<_, Option<i64>>(2).is_none()
            && build.get::<_, i64>(3) == 0
            && digest(build.get(4), "initial restore genesis")? == manifest.genesis_hash
            && frozen_plan == Some(manifest_hash)
            && build_manifest.is_none()
            && build.get::<_, Option<i64>>(7).is_none()
            && build_head.is_none();
        let existing_matches =
            current_coordinate.is_some_and(|(current_basis, current_hash, current_generation)| {
                kind == 2
                    && build.get::<_, Option<i64>>(2) == Some(current_generation)
                    && build.get::<_, i64>(3) == current_basis
                    && digest(build.get(4), "restore build captured head")
                        .is_ok_and(|hash| hash == current_hash)
                    && frozen_plan.is_none()
                    && build_manifest == Some(manifest_hash)
                    && build.get::<_, Option<i64>>(7)
                        == sql_u64(manifest.basis, "restore build basis").ok()
                    && build_head == Some(manifest.head_transaction_hash)
            });
        if initial_matches || existing_matches {
            if matching_build.replace(build).is_some() {
                conflicting_restore = true;
            }
        } else {
            // Headless catalogs admit only their one authenticated kind-zero
            // build. With an active head, any other unclaimed restore build
            // represents a competing requested point and must be resolved or
            // permanently claimed before another restore starts.
            conflicting_restore = true;
        }
    }
    if conflicting_restore {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "backup/restore-build-exists",
            "another restore build must be completed or collected before this point can restore",
        ));
    }
    if let Some(build) = matching_build {
        let generation = unsigned(build.get(0), "existing restore generation")?;
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-candidate-commit", error)
        })?;
        return Ok(RestoreCandidate {
            generation,
            initial: current_coordinate.is_none(),
        });
    }
    let initial = current_coordinate.is_none();
    let build_kind: i16 = if initial { 0 } else { 2 };
    let maximum_generation = transaction
        .query_one(
            "SELECT COALESCE(max(generation), 0) FROM atomic_log_generations \
              WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-generation-max", error))?
        .get::<_, i64>(0);
    let generation = if initial {
        manifest.log_generation.max(1)
    } else {
        unsigned(maximum_generation, "maximum restore generation")?
            .max(manifest.log_generation)
            .checked_add(1)
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unsupported,
                    "backup/restore-generation-exhausted",
                    "database-local log generation is exhausted",
                )
            })?
    };
    let generation_i64 = sql_u64(generation, "staged restore generation")?;
    transaction
        .execute(
            "INSERT INTO atomic_log_generations \
                 (database_id, generation, lineage_id, build_kind, request_count) \
             VALUES ($1, $2, $3, $4, 0)",
            &[
                &target_database_id,
                &generation_i64,
                &manifest.lineage_id,
                &build_kind,
            ],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-generation-stage", error)
        })?;
    if let Some((current_basis, current_hash, current_generation)) = current_coordinate {
        transaction
            .execute(
                "INSERT INTO atomic_log_generation_builds \
                     (database_id, generation, source_generation, captured_basis_t, \
                      captured_head_hash, restore_manifest_hash, restore_basis_t, \
                      restore_head_hash) \
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8)",
                &[
                    &target_database_id,
                    &generation_i64,
                    &current_generation,
                    &current_basis,
                    &&current_hash[..],
                    &&manifest_hash[..],
                    &sql_u64(manifest.basis, "restore basis")?,
                    &&manifest.head_transaction_hash[..],
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-build-stage", error)
            })?;
    } else {
        transaction
            .execute(
                "INSERT INTO atomic_log_generation_builds \
                     (database_id, generation, source_generation, captured_basis_t, \
                      captured_head_hash, frozen_plan_hash) \
                 VALUES ($1, $2, NULL, 0, $3, $4)",
                &[
                    &target_database_id,
                    &generation_i64,
                    &&manifest.genesis_hash[..],
                    &&manifest_hash[..],
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-initial-build-stage", error)
            })?;
    }
    transaction.commit().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-candidate-commit", error)
    })?;
    Ok(RestoreCandidate {
        generation,
        initial,
    })
}

#[allow(clippy::too_many_arguments)]
fn stage_restore_generation(
    client: &mut Client,
    manifest: &Manifest,
    prepared: &PreparedRestore,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    candidate: RestoreCandidate,
    fault_at: RestoreFault,
    control: &crate::MaintenanceControl,
) -> Result<RestoredGenerationHead, SemanticError> {
    let generation_sql = sql_u64(candidate.generation, "restored log generation")?;
    let mut previous = manifest.genesis_hash;
    let mut state_hash = prepared.genesis_state_hash;
    let mut frontier = prepared.genesis_frontier;
    for chunk in prepared.rows.chunks(RESTORE_BATCH_ROWS) {
        control.check()?;
        let mut transaction = client.transaction().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-generation-begin", error)
        })?;
        for row in chunk {
            let basis_sql = sql_u64(row.basis_t, "restored transaction basis")?;
            let frontier_sql = sql_u64(row.eidx_frontier, "restored transaction frontier")?;
            install_restored_content(&mut transaction, manifest, row)?;
            if fault_at == RestoreFault::AfterFirstContentInserted
                && row.basis_t == prepared.rows[0].basis_t
            {
                return Err(injected("backup/restore-after-content-insert"));
            }
            let tx_hash = crate::log_generation::generation_transaction_hash(
                &manifest.lineage_id,
                candidate.generation,
                row.basis_t,
                previous,
                row.content_hash,
                row.state_hash,
                row.eidx_frontier,
            )?;
            transaction
                .execute(
                    "INSERT INTO atomic_generation_transactions \
                         (database_id, generation, basis_t, previous_hash, tx_hash, \
                          content_hash, state_hash, eidx_frontier) \
                     VALUES ($1, $2, $3, $4, $5, $6, $7, $8) \
                     ON CONFLICT (database_id, generation, basis_t) DO NOTHING",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &basis_sql,
                        &&previous[..],
                        &&tx_hash[..],
                        &&row.content_hash[..],
                        &&row.state_hash[..],
                        &frontier_sql,
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-generation-transaction", error)
                })?;
            let stored = transaction
                .query_one(
                    "SELECT previous_hash, tx_hash, content_hash, state_hash, eidx_frontier \
                       FROM atomic_generation_transactions \
                      WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                    &[&target_database_id, &generation_sql, &basis_sql],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error(
                        "backup/restore-generation-transaction-verify",
                        error,
                    )
                })?;
            if digest(stored.get(0), "restored previous hash")? != previous
                || digest(stored.get(1), "restored transaction hash")? != tx_hash
                || digest(stored.get(2), "restored content hash")? != row.content_hash
                || digest(stored.get(3), "restored state hash")? != row.state_hash
                || stored.get::<_, i64>(4) != frontier_sql
            {
                return Err(fault(
                    "backup/restore-generation-conflict",
                    "staged generation row disagrees with the backup point",
                ));
            }
            let request_digest = if row.request.request_kind == 0 {
                tombstone_request_digest(
                    &manifest.lineage_id,
                    candidate.generation,
                    row.basis_t,
                    row.request.request_key_hash,
                )?
            } else {
                row.request.digest
            };
            let request_kind = i16::from(row.request.request_kind);
            transaction
                .execute(
                    "INSERT INTO atomic_generation_requests \
                         (database_id, generation, request_key_hash, request_digest, \
                          request_kind, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, $5, $6, $7) \
                     ON CONFLICT (database_id, generation, request_key_hash) DO NOTHING",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &&row.request.request_key_hash[..],
                        &&request_digest[..],
                        &request_kind,
                        &basis_sql,
                        &&tx_hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-generation-request", error)
                })?;
            let stored = transaction
                .query_one(
                    "SELECT request_key_hash, request_digest, request_kind, tx_hash \
                       FROM atomic_generation_requests \
                      WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                    &[&target_database_id, &generation_sql, &basis_sql],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error(
                        "backup/restore-generation-request-verify",
                        error,
                    )
                })?;
            if digest(stored.get(0), "restored request key")? != row.request.request_key_hash
                || digest(stored.get(1), "restored request digest")? != request_digest
                || stored.get::<_, i16>(2) != request_kind
                || digest(stored.get(3), "restored request transaction")? != tx_hash
            {
                return Err(fault(
                    "backup/restore-request-conflict",
                    "staged request row disagrees with the backup point",
                ));
            }
            for (name, entity) in &row.request.receipt_tempids {
                transaction
                    .execute(
                        "INSERT INTO atomic_generation_request_tempids \
                             (database_id, generation, request_key_hash, tempid_name, entity_id) \
                         VALUES ($1, $2, $3, $4, $5) \
                         ON CONFLICT (database_id, generation, request_key_hash, tempid_name) \
                         DO NOTHING",
                        &[
                            &target_database_id,
                            &generation_sql,
                            &&row.request.request_key_hash[..],
                            &name,
                            &sql_u64(*entity, "restored receipt entity")?,
                        ],
                    )
                    .map_err(|error| {
                        crate::postgres::postgres_error("backup/restore-request-tempid", error)
                    })?;
            }
            let receipt = transaction
                .query(
                    "SELECT tempid_name, entity_id FROM atomic_generation_request_tempids \
                      WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3 \
                      ORDER BY tempid_name",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &&row.request.request_key_hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-request-tempid-verify", error)
                })?
                .into_iter()
                .map(|stored| {
                    Ok((
                        stored.get::<_, String>(0),
                        unsigned(stored.get(1), "restored receipt entity")?,
                    ))
                })
                .collect::<Result<BTreeMap<_, _>, SemanticError>>()?;
            if receipt != row.request.receipt_tempids {
                return Err(fault(
                    "backup/restore-receipt-conflict",
                    "staged transaction receipt disagrees with the backup point",
                ));
            }
            previous = tx_hash;
            state_hash = row.state_hash;
            frontier = row.eidx_frontier;
        }
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-generation-commit", error)
        })?;
        control.after_batch()?;
    }
    // Membership hashes bind the local generation number. Initial restore of
    // a positive-generation archive can preserve its hash chain, but every
    // rewind or forward restore deliberately allocates a new local generation
    // and therefore has a different head hash for identical transaction
    // content. The portable endpoint commitment that must remain equal is the
    // database state; `previous` is the newly rebound generation head passed
    // to the atomic activation below.
    if state_hash != manifest.head_state_hash {
        return Err(fault(
            "backup/restore-generation-state",
            "staged restore generation does not reproduce the backup endpoint state",
        ));
    }
    stage_restore_checkpoint(
        client,
        manifest,
        completed_excisions,
        target_database_id,
        candidate,
        previous,
        state_hash,
        frontier,
    )?;
    Ok(RestoredGenerationHead {
        head_hash: previous,
        state_hash,
    })
}

/// Rebuild v2 semantic coordinates for a restored generation in one forward
/// pass. Request-base publications are authenticated against these rows, and
/// retaining them lets exact historical receipts open without replaying an
/// eager database. Nodes are content-addressed and path-copied, so successive
/// bases share all unaffected structure.
fn stage_restore_semantic_coordinates(
    client: &mut Client,
    directory: &Path,
    manifest: &Manifest,
    prepared: &PreparedRestore,
    target_database_id: &str,
    candidate: RestoreCandidate,
) -> Result<(), SemanticError> {
    if !prepared
        .rows
        .iter()
        .any(|row| matches!(row.request.request_kind, 1 | 2))
    {
        return Ok(());
    }
    // Legacy ordinary receipts do not carry a physical request-base hash,
    // but their exact db-before/db-after values need these same semantic
    // coordinates after restoration. Tombstones have no report to reopen.
    let genesis = read_object(directory, manifest.genesis_hash)?;
    let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
    let mut allocation_replay = BackupAllocationReplay::new(&database)?;
    let genesis_state_hash = checkpoint_state_hash(&database)?;
    let mut transaction = client.transaction().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-semantic-genesis-begin", error)
    })?;
    let mut root = crate::persistent_commitment::record_eager_endpoint(
        &mut transaction,
        target_database_id,
        candidate.generation,
        manifest.genesis_hash,
        genesis_state_hash,
        &database,
    )?;
    transaction.commit().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-semantic-genesis-commit", error)
    })?;

    let mut previous = manifest.genesis_hash;
    for chunk in prepared.rows.chunks(RESTORE_BATCH_ROWS) {
        let mut transaction = client.transaction().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-semantic-begin", error)
        })?;
        for row in chunk {
            let content = LineageTransactionContent::decode(&row.content_payload)?;
            let mut durable = content.to_transaction(previous);
            durable.database_id = target_database_id.to_owned();
            let changes =
                crate::persistent_commitment::eager_semantic_changes(&database, &durable.tx_data)?;
            let (next_root, _) = crate::persistent_commitment::advance_persistent_commitment(
                &mut transaction,
                root,
                &changes,
            )?;
            let allow_excision = match row.request.request_kind {
                0 => true,
                1 | 2 => false,
                _ => {
                    return Err(fault(
                        "backup/restore-request-kind",
                        "restored semantic coordinate has an invalid request kind",
                    ));
                }
            };
            let next_database = allocation_replay.apply(
                &database,
                &durable,
                content.reserved_frontier,
                allow_excision,
            )?;
            let state_hash = next_root.state_hash(row.basis_t, row.eidx_frontier);
            if state_hash != row.state_hash
                || checkpoint_state_hash(&next_database)? != row.state_hash
            {
                return Err(fault(
                    "backup/restore-semantic-state",
                    "restored transaction does not reproduce its v2 semantic commitment",
                ));
            }
            let tx_hash = crate::log_generation::generation_transaction_hash(
                &manifest.lineage_id,
                candidate.generation,
                row.basis_t,
                previous,
                row.content_hash,
                row.state_hash,
                row.eidx_frontier,
            )?;
            crate::persistent_commitment::record_persistent_coordinate(
                &mut transaction,
                &crate::persistent_commitment::PersistentCommitmentCoordinate {
                    database_id: target_database_id.to_owned(),
                    generation: candidate.generation,
                    basis_t: row.basis_t,
                    tx_hash,
                    state_hash: row.state_hash,
                    eidx_frontier: row.eidx_frontier,
                    root: next_root,
                },
            )?;
            root = next_root;
            database = next_database;
            previous = tx_hash;
        }
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-semantic-commit", error)
        })?;
    }
    database.validate_invariants()?;
    Ok(())
}

fn stage_restore_completions(
    client: &mut Client,
    target_database_id: &str,
    generation_sql: i64,
    completed_excisions: &[(u64, u64)],
) -> Result<(), SemanticError> {
    let expected_count = i64::try_from(completed_excisions.len()).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/restore-completion-count",
            "completed-excision set exceeds PostgreSQL bigint",
        )
    })?;
    let stage = client
        .query_opt(
            "SELECT phase, row_count FROM atomic_log_generation_completion_stages \
              WHERE database_id = $1 AND generation = $2",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-completion-state", error)
        })?;
    let already_sealed = stage
        .as_ref()
        .is_some_and(|row| row.get::<_, i16>(0) == 2 && row.get::<_, i64>(1) == expected_count);
    if !already_sealed {
        if stage.as_ref().is_some_and(|row| row.get::<_, i16>(0) != 1) {
            return Err(fault(
                "backup/restore-completion-state",
                "restore completion stage has an incompatible phase or cardinality",
            ));
        }
        if completed_excisions.is_empty() {
            let empty: Vec<i64> = Vec::new();
            client
                .query_one(
                    "SELECT atomic_stage_restore_completions($1, $2, $3, $4)",
                    &[&target_database_id, &generation_sql, &empty, &empty],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-completion-stage", error)
                })?;
        } else {
            for chunk in completed_excisions.chunks(RESTORE_BATCH_ROWS) {
                let mut request_ts = Vec::with_capacity(chunk.len());
                let mut request_entities = Vec::with_capacity(chunk.len());
                for &(request_t, request_entity) in chunk {
                    request_ts.push(sql_u64(request_t, "completed request t")?);
                    request_entities.push(sql_u64(request_entity, "completed request entity")?);
                }
                client
                    .query_one(
                        "SELECT atomic_stage_restore_completions($1, $2, $3, $4)",
                        &[
                            &target_database_id,
                            &generation_sql,
                            &request_ts,
                            &request_entities,
                        ],
                    )
                    .map_err(|error| {
                        crate::postgres::postgres_error("backup/restore-completion-stage", error)
                    })?;
            }
        }
        let staged_completions = client
            .query(
                "SELECT request_t, request_entity FROM atomic_completed_excision_requests \
                  WHERE database_id = $1 AND generation = $2 \
                  ORDER BY request_t, request_entity",
                &[&target_database_id, &generation_sql],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-completion-verify", error)
            })?
            .into_iter()
            .map(|row| {
                Ok((
                    unsigned(row.get(0), "staged completion t")?,
                    unsigned(row.get(1), "staged completion entity")?,
                ))
            })
            .collect::<Result<Vec<_>, SemanticError>>()?;
        if staged_completions != completed_excisions {
            return Err(fault(
                "backup/restore-completion-conflict",
                "staged completed-excision set disagrees with the backup point",
            ));
        }
        client
            .query_one(
                "SELECT atomic_seal_restore_completions($1, $2, $3)",
                &[&target_database_id, &generation_sql, &expected_count],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-completion-seal", error)
            })?;
    }
    let sealed = client
        .query_opt(
            "SELECT 1 FROM atomic_log_generation_completion_stages \
              WHERE database_id = $1 AND generation = $2 \
                AND phase = 2 AND row_count = $3 AND sealed_at IS NOT NULL",
            &[&target_database_id, &generation_sql, &expected_count],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-completion-sealed", error)
        })?;
    if sealed.is_none() {
        return Err(fault(
            "backup/restore-completion-incomplete",
            "restore completion set did not reach its durable sealed root",
        ));
    }
    let staged_completions = client
        .query(
            "SELECT request_t, request_entity FROM atomic_completed_excision_requests \
              WHERE database_id = $1 AND generation = $2 \
              ORDER BY request_t, request_entity",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-completion-final-verify", error)
        })?
        .into_iter()
        .map(|row| {
            Ok((
                unsigned(row.get(0), "sealed completion t")?,
                unsigned(row.get(1), "sealed completion entity")?,
            ))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    if staged_completions != completed_excisions {
        return Err(fault(
            "backup/restore-completion-conflict",
            "sealed completed-excision set disagrees with the backup point",
        ));
    }
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn stage_restore_checkpoint(
    client: &mut Client,
    manifest: &Manifest,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    candidate: RestoreCandidate,
    head_hash: Digest,
    state_hash: Digest,
    frontier: u64,
) -> Result<(), SemanticError> {
    let generation_sql = sql_u64(candidate.generation, "restore checkpoint generation")?;
    stage_restore_completions(
        client,
        target_database_id,
        generation_sql,
        completed_excisions,
    )?;
    let mut transaction = client.transaction().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-checkpoint-begin", error)
    })?;
    let source_head_hash: Option<&[u8]> = if candidate.initial {
        None
    } else {
        Some(&manifest.head_transaction_hash)
    };
    transaction
        .execute(
            "INSERT INTO atomic_log_generation_checkpoints \
                 (database_id, generation, through_basis_t, head_hash, state_hash, \
                  source_head_hash, eidx_frontier, removed_datoms) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, 0) \
             ON CONFLICT (database_id, generation, through_basis_t) DO NOTHING",
            &[
                &target_database_id,
                &generation_sql,
                &sql_u64(manifest.basis, "restore checkpoint basis")?,
                &&head_hash[..],
                &&state_hash[..],
                &source_head_hash,
                &sql_u64(frontier, "restore checkpoint frontier")?,
            ],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-checkpoint-stage", error)
        })?;
    let checkpoint = transaction
        .query_one(
            "SELECT head_hash, state_hash, source_head_hash, eidx_frontier, removed_datoms \
               FROM atomic_log_generation_checkpoints \
              WHERE database_id = $1 AND generation = $2 AND through_basis_t = $3",
            &[
                &target_database_id,
                &generation_sql,
                &sql_u64(manifest.basis, "restore checkpoint basis")?,
            ],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-checkpoint-verify", error)
        })?;
    if digest(checkpoint.get(0), "restore checkpoint head")? != head_hash
        || digest(checkpoint.get(1), "restore checkpoint state")? != state_hash
        || checkpoint
            .get::<_, Option<Vec<u8>>>(2)
            .map(|hash| digest(hash, "restore checkpoint source"))
            .transpose()?
            != (!candidate.initial).then_some(manifest.head_transaction_hash)
        || unsigned(checkpoint.get(3), "restore checkpoint frontier")? != frontier
        || checkpoint.get::<_, i64>(4) != 0
    {
        return Err(fault(
            "backup/restore-checkpoint-conflict",
            "durable restore checkpoint disagrees with its staged generation",
        ));
    }
    transaction
        .commit()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-checkpoint-commit", error))
}

fn activate_restore_candidate(
    client: &mut Client,
    manifest: &Manifest,
    target_database_id: &str,
    candidate: RestoreCandidate,
    restored: &RestoredGenerationHead,
    build_pin: i64,
    activation_probe: Option<&mut dyn FnMut()>,
) -> Result<(), SemanticError> {
    // The builder pin and the restore serialization lock form an atomic
    // handoff. GC first needs the builder lock and then this restore lock. We
    // therefore acquire the latter while still holding the former, release
    // the session pin inside the transaction, and publish before the xact
    // lock can be released. There is no instant at which a live, fully staged
    // restore is eligible for a permanent abandonment claim.
    let mut build_pin_released = false;
    let activation = (|| {
        let mut transaction = client.transaction().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-activation-begin", error)
        })?;
        transaction
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended('atomic/restore/' || $1, 0))",
                &[&target_database_id],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-lock", error))?;
        let unlocked: bool = transaction
            .query_one("SELECT pg_advisory_unlock_shared($1)", &[&build_pin])
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-build-pin-handoff", error)
            })?
            .get(0);
        // A false result proves the session pin is already absent. Record
        // that fact before reporting the invariant failure so cleanup does
        // not mask the more useful error.
        build_pin_released = true;
        if !unlocked {
            return Err(fault(
                "backup/restore-build-pin-handoff",
                "restore builder pin disappeared before activation handoff",
            ));
        }
        if let Some(probe) = activation_probe {
            probe();
        }
        // A restore carrying native request bases has already rebuilt every
        // semantic coordinate in one forward pass. Do not replay the full
        // database again merely to rediscover its endpoint. Older backups do
        // not carry those roots, so retain the one eager administrative seed
        // for that compatibility path.
        if let Some(endpoint) = crate::persistent_commitment::load_persistent_coordinate(
            &mut transaction,
            target_database_id,
            candidate.generation,
            manifest.basis,
        )? {
            if endpoint.tx_hash != restored.head_hash || endpoint.state_hash != restored.state_hash
            {
                return Err(fault(
                    "backup/restore-semantic-endpoint",
                    "staged semantic endpoint disagrees with the restored generation head",
                ));
            }
        } else {
            let endpoint = crate::postgres::recover_generation_to(
                &mut transaction,
                target_database_id,
                candidate.generation,
                manifest.basis,
                restored.head_hash,
            )?
            .database;
            crate::persistent_commitment::record_eager_endpoint(
                &mut transaction,
                target_database_id,
                candidate.generation,
                restored.head_hash,
                restored.state_hash,
                &endpoint,
            )?;
        }
        let generation_sql = sql_u64(candidate.generation, "restore activation generation")?;
        let basis_sql = sql_u64(manifest.basis, "restore activation basis")?;
        if candidate.initial {
            transaction
                .query_one(
                    "SELECT atomic_activate_initial_log_generation($1, $2, $3, $4, $5)",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &basis_sql,
                        &&restored.head_hash[..],
                        &&restored.state_hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-initial-activate", error)
                })?;
        } else {
            transaction
                .query_one(
                    "SELECT atomic_activate_log_generation($1, $2, $3, $4, $5, NULL)",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &basis_sql,
                        &&restored.head_hash[..],
                        &&restored.state_hash[..],
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-activate", error)
                })?;
        }
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-activation-commit", error)
        })
    })();
    if !build_pin_released {
        // The restore transaction failed before the server confirmed the
        // session unlock. Once it has dropped, resolve either side of an
        // ambiguous unlock acknowledgement before this client is reused.
        let release = release_restore_build_pin(client, build_pin);
        match (activation, release) {
            (Ok(()), Ok(())) => {}
            (Ok(()), Err(error)) => return Err(error),
            (Err(error), _) => return Err(error),
        }
    } else {
        activation?;
    }
    cleanup_restore_build(client, target_database_id, candidate.generation)
}

fn cleanup_restore_build(
    client: &mut Client,
    target_database_id: &str,
    generation: u64,
) -> Result<(), SemanticError> {
    let generation_sql = sql_u64(generation, "restore cleanup generation")?;
    loop {
        let row = client
            .query_one(
                "SELECT rows_removed, is_complete \
                   FROM atomic_cleanup_log_generation_build($1, $2, $3)",
                &[&target_database_id, &generation_sql, &4096_i64],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-build-cleanup", error)
            })?;
        if row.get::<_, bool>(1) {
            return Ok(());
        }
    }
}

fn cleanup_active_restore_build_if_present(
    client: &mut Client,
    target_database_id: &str,
) -> Result<(), SemanticError> {
    let Some(row) = client
        .query_opt(
            "SELECT h.log_generation \
               FROM atomic_heads h \
              WHERE h.database_id = $1 \
                AND EXISTS (SELECT 1 FROM atomic_log_generation_builds b \
                             WHERE b.database_id = h.database_id \
                               AND b.generation = h.log_generation)",
            &[&target_database_id],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-build-cleanup-read", error)
        })?
    else {
        return Ok(());
    };
    cleanup_restore_build(
        client,
        target_database_id,
        unsigned(row.get(0), "active restore cleanup generation")?,
    )
}

fn install_restored_program<C: crate::sql_io::GenericClient>(
    client: &mut C,
    program_hash: &Digest,
    program: &Program,
    payload: &[u8],
) -> Result<(), SemanticError> {
    let kind = program_kind(program);
    let arity = i16::from(program.arity);
    client
        .execute(
            "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
             VALUES ($1, $2, $3, $4) ON CONFLICT (program_hash) DO NOTHING",
            &[&&program_hash[..], &kind, &arity, &payload],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-program", error))?;
    let existing = client
        .query_one(
            "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
            &[&&program_hash[..]],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-existing-program", error)
        })?;
    if existing.get::<_, i16>(0) != kind
        || existing.get::<_, i16>(1) != arity
        || existing.get::<_, Vec<u8>>(2) != payload
    {
        return Err(fault(
            "backup/restore-program-conflict",
            "existing content-addressed program does not match its hash",
        ));
    }
    Ok(())
}

fn sql_u64(value: u64, label: &str) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::incorrect(
            "backup/postgres-integer",
            format!("{label} exceeds PostgreSQL bigint"),
        )
    })
}

struct CapturedLogTail {
    source_head_hash: Digest,
    portable_head_hash: Digest,
    request_head_hash: Digest,
    source_transaction_hashes: Vec<Digest>,
    portable_transaction_hashes: Vec<Digest>,
    portable_frontiers: Vec<u64>,
    state_hashes: Vec<Digest>,
}

#[allow(clippy::too_many_arguments)]
fn capture_log_tail<C: crate::sql_io::GenericClient>(
    client: &mut C,
    database_id: &str,
    lineage_id: &str,
    log_generation: u64,
    start_basis: u64,
    _backup_basis: u64,
    start_basis_sql: i64,
    backup_basis_sql: i64,
    expected_tail_len: usize,
    mut source_previous: Digest,
    mut portable_previous: Digest,
    mut request_previous: Digest,
    mut prior_frontier: u64,
    temporal_programs: &mut BTreeSet<Digest>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<CapturedLogTail, SemanticError> {
    let mut source_transaction_hashes = Vec::with_capacity(expected_tail_len);
    let mut portable_transaction_hashes = Vec::with_capacity(expected_tail_len);
    let mut portable_frontiers = Vec::with_capacity(expected_tail_len);
    let mut state_hashes = Vec::with_capacity(expected_tail_len);

    if log_generation == 0 {
        let rows = client
            .query(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, t.payload, t.state_hash, \
                        r.request_key, r.request_digest, r.tx_hash \
                   FROM atomic_transactions t JOIN atomic_requests r \
                     ON r.database_id = t.database_id AND r.basis_t = t.basis_t \
                  WHERE t.database_id = $1 AND t.basis_t >= $2 AND t.basis_t <= $3 \
                  ORDER BY t.basis_t",
                &[&database_id, &start_basis_sql, &backup_basis_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/legacy-log", error))?;
        if rows.len() != expected_tail_len {
            return Err(fault(
                "backup/incomplete-chain",
                "legacy snapshot does not contain one transaction and request per basis",
            ));
        }
        for (offset, row) in rows.into_iter().enumerate() {
            let basis = unsigned(row.get(0), "legacy transaction basis")?;
            let stored_previous = digest(row.get(1), "legacy predecessor hash")?;
            let source_hash = digest(row.get(2), "legacy transaction hash")?;
            let source_payload: Vec<u8> = row.get(3);
            let state_hash = digest(row.get(4), "legacy transaction state hash")?;
            let request_key: String = row.get(5);
            let request_digest = digest(row.get(6), "legacy request digest")?;
            let request_tx_hash = digest(row.get(7), "legacy request transaction hash")?;
            let source = decode_transaction(&source_payload)?;
            if basis != start_basis + offset as u64
                || stored_previous != source_previous
                || source_hash != transaction_hash(&source_payload)
                || request_tx_hash != source_hash
                || (source.database_id != database_id && source.database_id != lineage_id)
                || source.basis_t != basis
                || source.previous_hash != source_previous
                || state_hash == [0; 32]
            {
                return Err(fault(
                    "backup/invalid-chain",
                    format!("legacy transaction {basis} is not canonical"),
                ));
            }

            // Legacy ATIM values contain mutable catalog identity and caller
            // tempid spellings. Convert their immutable information into ATLC;
            // receipt names move to the request record where they belong.
            // This per-record normalization changes neither datoms nor their
            // state commitment. Exact frontier advancement and state semantics
            // are proved by verification/restore, not by the copy operation.
            let content =
                LineageTransactionContent::from_transaction(lineage_id, prior_frontier, &source)?;
            let content_payload = content.encode()?;
            let content_hash = sha256(&content_payload);
            let portable = content.to_transaction(portable_previous);
            let membership = BackupMembershipRecord {
                lineage_id: lineage_id.to_owned(),
                log_generation,
                basis,
                previous_hash: portable_previous,
                content_hash,
                state_hash,
                eidx_frontier: content.eidx_frontier,
            };
            let membership_payload = encode_backup_membership(&membership)?;
            let portable_hash = sha256(&membership_payload);
            for datom in &portable.tx_data {
                collect_function_hashes(&datom.value, temporal_programs);
            }
            let request = BackupRequestRecord {
                lineage_id: lineage_id.to_owned(),
                log_generation,
                basis,
                transaction_hash: portable_hash,
                previous_hash: request_previous,
                request_key_hash: request_key_hash(lineage_id, &request_key)?,
                digest: request_digest,
                request_kind: 1,
                base_manifest_hash: None,
                receipt_tempids: source.tempids.clone(),
            };
            let request_payload = encode_request_record(&request)?;
            let request_hash = sha256(&request_payload);
            publisher.publish(content_hash, &content_payload)?;
            publisher.publish(portable_hash, &membership_payload)?;
            publisher.publish(request_hash, &request_payload)?;

            source_previous = source_hash;
            portable_previous = portable_hash;
            request_previous = request_hash;
            prior_frontier = content.eidx_frontier;
            source_transaction_hashes.push(source_hash);
            portable_transaction_hashes.push(portable_hash);
            portable_frontiers.push(content.eidx_frontier);
            state_hashes.push(state_hash);
        }
    } else {
        let generation_sql = i64::try_from(log_generation).map_err(|_| {
            fault(
                "backup/generation-overflow",
                "active log generation exceeds PostgreSQL bigint",
            )
        })?;
        let mut receipt_tempids: BTreeMap<u64, (Digest, BTreeMap<String, u64>)> = BTreeMap::new();
        for row in client
            .query(
                "SELECT r.basis_t, r.request_key_hash, x.tempid_name, x.entity_id \
                   FROM atomic_generation_requests r \
                   JOIN atomic_generation_request_tempids x \
                     ON x.database_id = r.database_id AND x.generation = r.generation \
                    AND x.request_key_hash = r.request_key_hash \
                  WHERE r.database_id = $1 AND r.generation = $2 \
                    AND r.basis_t >= $3 AND r.basis_t <= $4 \
                  ORDER BY r.basis_t, x.tempid_name",
                &[
                    &database_id,
                    &generation_sql,
                    &start_basis_sql,
                    &backup_basis_sql,
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/generation-request-tempids", error)
            })?
        {
            let basis = unsigned(row.get(0), "request receipt basis")?;
            let key_hash = digest(row.get(1), "request receipt key hash")?;
            let name: String = row.get(2);
            let entity = unsigned(row.get(3), "request receipt entity")?;
            let entry = receipt_tempids
                .entry(basis)
                .or_insert_with(|| (key_hash, BTreeMap::new()));
            if entry.0 != key_hash || name.is_empty() || entry.1.insert(name, entity).is_some() {
                return Err(fault(
                    "backup/request-receipt",
                    "generation request receipt rows are not canonical",
                ));
            }
        }

        let rows = client
            .query(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, t.content_hash, \
                        t.state_hash, t.eidx_frontier, c.lineage_id, \
                        c.basis_t, c.eidx_frontier, c.envelope_version, c.payload, \
                        r.request_key_hash, r.request_digest, r.request_kind, r.tx_hash, \
                        b.base_manifest_hash \
                   FROM atomic_generation_transactions t \
                   JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
                   JOIN atomic_generation_requests r \
                     ON r.database_id = t.database_id AND r.generation = t.generation \
                    AND r.basis_t = t.basis_t \
                   LEFT JOIN atomic_generation_request_bases b \
                     ON b.database_id = r.database_id AND b.generation = r.generation \
                    AND b.request_key_hash = r.request_key_hash \
                  WHERE t.database_id = $1 AND t.generation = $2 \
                    AND t.basis_t >= $3 AND t.basis_t <= $4 ORDER BY t.basis_t",
                &[
                    &database_id,
                    &generation_sql,
                    &start_basis_sql,
                    &backup_basis_sql,
                ],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/generation-log", error))?;
        if rows.len() != expected_tail_len {
            return Err(fault(
                "backup/incomplete-chain",
                "active generation does not contain one transaction and request per basis",
            ));
        }
        let mut captured_bases = BTreeMap::new();
        for (offset, row) in rows.into_iter().enumerate() {
            let basis = unsigned(row.get(0), "generation transaction basis")?;
            let stored_previous = digest(row.get(1), "generation predecessor hash")?;
            let transaction_hash = digest(row.get(2), "generation transaction hash")?;
            let content_hash = digest(row.get(3), "generation content hash")?;
            let state_hash = digest(row.get(4), "generation state hash")?;
            let eidx_frontier = unsigned(row.get(5), "generation entity frontier")?;
            let content_lineage_id: String = row.get(6);
            let content_basis = unsigned(row.get(7), "content basis")?;
            let content_frontier = unsigned(row.get(8), "content entity frontier")?;
            let content_version: i16 = row.get(9);
            let content_payload: Vec<u8> = row.get(10);
            let request_key_hash = digest(row.get(11), "generation request key hash")?;
            let request_digest = digest(row.get(12), "generation request digest")?;
            let request_kind_i16: i16 = row.get(13);
            let request_tx_hash = digest(row.get(14), "generation request transaction hash")?;
            let source_base_manifest = row
                .get::<_, Option<Vec<u8>>>(15)
                .map(|hash| digest(hash, "generation request base manifest"))
                .transpose()?;
            let membership = BackupMembershipRecord {
                lineage_id: lineage_id.to_owned(),
                log_generation,
                basis,
                previous_hash: stored_previous,
                content_hash,
                state_hash,
                eidx_frontier,
            };
            let membership_payload = encode_backup_membership(&membership)?;
            let content = LineageTransactionContent::decode(&content_payload)?;
            if basis != start_basis + offset as u64
                || stored_previous != source_previous
                || transaction_hash != sha256(&membership_payload)
                || request_tx_hash != transaction_hash
                || sha256(&content_payload) != content_hash
                || content_lineage_id != lineage_id
                || content.lineage_id != lineage_id
                || content_basis != basis
                || content.basis_t != basis
                || content_frontier != eidx_frontier
                || content.eidx_frontier != eidx_frontier
                || content_version
                    != i16::try_from(content.version()).expect("ATLC version fits i16")
                || state_hash == [0; 32]
                || !matches!(request_kind_i16, 0..=2)
                || (request_kind_i16 == 2) != source_base_manifest.is_some()
                || (request_kind_i16 == 0
                    && request_digest
                        != tombstone_request_digest(
                            lineage_id,
                            log_generation,
                            basis,
                            request_key_hash,
                        )?)
            {
                return Err(fault(
                    "backup/invalid-chain",
                    format!("generation transaction {basis} is not canonical"),
                ));
            }
            let transaction = content.to_transaction(stored_previous);
            for datom in &transaction.tx_data {
                collect_function_hashes(&datom.value, temporal_programs);
            }
            let (receipt_key, receipt) = receipt_tempids
                .remove(&basis)
                .unwrap_or((request_key_hash, BTreeMap::new()));
            if receipt_key != request_key_hash || (request_kind_i16 == 0 && !receipt.is_empty()) {
                return Err(fault(
                    "backup/request-receipt",
                    "generation request receipt disagrees with its request identity",
                ));
            }
            let base_manifest_hash = match source_base_manifest {
                None => None,
                Some(source_hash) => {
                    let portable = if let Some(portable) = captured_bases.get(&source_hash) {
                        *portable
                    } else {
                        let portable = capture_bound_request_tree(
                            client,
                            database_id,
                            lineage_id,
                            log_generation,
                            source_hash,
                            publisher,
                        )?;
                        captured_bases.insert(source_hash, portable);
                        portable
                    };
                    Some(portable)
                }
            };
            let request = BackupRequestRecord {
                lineage_id: lineage_id.to_owned(),
                log_generation,
                basis,
                transaction_hash,
                previous_hash: request_previous,
                request_key_hash,
                digest: request_digest,
                request_kind: request_kind_i16 as u8,
                base_manifest_hash,
                receipt_tempids: receipt,
            };
            let request_payload = encode_request_record(&request)?;
            let request_hash = sha256(&request_payload);
            publisher.publish(content_hash, &content_payload)?;
            publisher.publish(transaction_hash, &membership_payload)?;
            publisher.publish(request_hash, &request_payload)?;

            source_previous = transaction_hash;
            portable_previous = transaction_hash;
            request_previous = request_hash;
            source_transaction_hashes.push(transaction_hash);
            portable_transaction_hashes.push(transaction_hash);
            portable_frontiers.push(eidx_frontier);
            state_hashes.push(state_hash);
        }
        if !receipt_tempids.is_empty() {
            return Err(fault(
                "backup/request-receipt",
                "generation has receipt tempids outside its transaction tail",
            ));
        }
    }

    Ok(CapturedLogTail {
        source_head_hash: source_previous,
        portable_head_hash: portable_previous,
        request_head_hash: request_previous,
        source_transaction_hashes,
        portable_transaction_hashes,
        portable_frontiers,
        state_hashes,
    })
}

fn capture_completed_excisions<C: crate::sql_io::GenericClient>(
    client: &mut C,
    database_id: &str,
    lineage_id: &str,
    log_generation: u64,
    genesis_hash: Digest,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Digest, SemanticError> {
    // Generation zero predates the generation-scoped completion root. Its
    // migration drops the old operational mirror, so it has no trustworthy
    // completed set to preserve. Every positive active generation must have
    // the root marker; without it rows are merely inactive staging data.
    if log_generation == 0 {
        return Ok(genesis_hash);
    }
    let generation_sql = sql_u64(log_generation, "completed excision generation")?;
    let marker = client
        .query_opt(
            "SELECT 1 FROM atomic_heads h \
               JOIN atomic_log_generation_completions m \
                 ON m.database_id = h.database_id AND m.generation = h.log_generation \
              WHERE h.database_id = $1 AND h.log_generation = $2",
            &[&database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/completed-excision-root", error)
        })?;
    if marker.is_none() {
        return Err(fault(
            "backup/missing-completion-root",
            "active log generation has no completed-excision root marker",
        ));
    }
    let rows = client
        .query(
            "SELECT request_t, request_entity FROM atomic_completed_excision_requests \
             WHERE database_id = $1 AND generation = $2 \
             ORDER BY request_t, request_entity",
            &[&database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/completed-excision-read", error)
        })?;
    let mut previous = genesis_hash;
    let mut prior_coordinate = None;
    for row in rows {
        let request_t = unsigned(row.get(0), "completed excision request t")?;
        let request_entity = unsigned(row.get(1), "completed excision request entity")?;
        let coordinate = (request_t, request_entity);
        if request_t == 0 || prior_coordinate.is_some_and(|prior| prior >= coordinate) {
            return Err(fault(
                "backup/completed-excision-order",
                "completed excision identities are not canonical and distinct",
            ));
        }
        let record = BackupCompletedExcision {
            lineage_id: lineage_id.to_owned(),
            request_t,
            request_entity,
            previous_hash: previous,
        };
        let payload = encode_completed_excision(&record)?;
        previous = sha256(&payload);
        publisher.publish(previous, &payload)?;
        prior_coordinate = Some(coordinate);
    }
    Ok(previous)
}

#[allow(clippy::too_many_arguments)]
fn complete_restored_target(
    connection: &PostgresConnectionConfig,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    target_database_id: &str,
    expected: &Database,
    main_tree: Option<&VerifiedMainTree>,
    matched: &MatchedRestoreTarget,
    fault_at: RestoreFault,
    completion_probe: Option<&mut dyn FnMut()>,
) -> Result<Database, SemanticError> {
    restore_tree_backup(
        connection,
        directory,
        manifest,
        log,
        target_database_id,
        main_tree,
        matched,
        fault_at,
    )?;
    if let Some(probe) = completion_probe {
        probe();
    }
    let mut client = connection.connect_for("backup/connect")?;
    let mut transaction = client.transaction().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-final-check-begin", error)
    })?;
    let head = transaction
        .query_opt(
            "SELECT h.basis_t, h.tx_hash, h.log_generation, d.lineage_id, d.genesis_hash, \
                t.state_hash, t.eidx_frontier \
           FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
           LEFT JOIN atomic_generation_transactions t \
             ON t.database_id = h.database_id AND t.generation = h.log_generation \
            AND t.basis_t = h.basis_t \
          WHERE h.database_id = $1 FOR SHARE OF h, d",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-final-check", error))?
        .ok_or_else(|| {
            fault(
                "backup/restore-target-changed",
                "restored target head disappeared before completion",
            )
        })?;
    let basis = unsigned(head.get(0), "final restored basis")?;
    let state = head
        .get::<_, Option<Vec<u8>>>(5)
        .map(|bytes| digest(bytes, "final restored state"))
        .transpose()?;
    let frontier = head
        .get::<_, Option<i64>>(6)
        .map(|value| unsigned(value, "final restored frontier"))
        .transpose()?;
    if basis != matched.basis_t
        || digest(head.get(1), "final restored transaction")? != matched.tx_hash
        || unsigned(head.get(2), "final restored generation")? != matched.generation
        || head.get::<_, String>(3) != matched.lineage_id
        || digest(head.get(4), "final restored genesis")? != matched.genesis_hash
        || (basis > 0
            && (state != Some(matched.state_hash) || frontier != Some(matched.eidx_frontier)))
        || expected.basis_t() != matched.basis_t
        || expected.eidx_frontier() != matched.eidx_frontier
        || checkpoint_state_hash(expected)? != matched.state_hash
    {
        return Err(fault(
            "backup/restore-target-changed",
            "restored target no longer names the exactly verified information generation",
        ));
    }
    transaction.commit().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-final-check-commit", error)
    })?;
    // Database contains immutable information, not a mutable catalog name or
    // local generation. Exact canonical row equality proves this same value
    // under the rebound target identity without replaying its log again.
    Ok(expected.clone())
}

#[allow(clippy::too_many_arguments)]
fn restore_tree_backup(
    connection: &PostgresConnectionConfig,
    directory: &Path,
    manifest: &Manifest,
    _log: &LoadedBackupLog,
    target_database_id: &str,
    verified: Option<&VerifiedMainTree>,
    matched: &MatchedRestoreTarget,
    fault_at: RestoreFault,
) -> Result<(), SemanticError> {
    let Some(tree) = &manifest.tree else {
        // The authoritative log is complete. A target without a captured
        // physical root deliberately recovers from that log and may be
        // consolidated normally after restore.
        return Ok(());
    };
    let verified = verified.ok_or_else(|| {
        fault(
            "backup/restore-tree-proof",
            "main tree has no exact deep-verification evidence",
        )
    })?;
    let source_payload = read_object(directory, tree.manifest_hash)?;
    let source = PersistentTreeManifest::decode(&source_payload)?;
    if tree.manifest_hash != verified.manifest_hash
        || source.basis_t != verified.basis_t
        || source.state_hash != verified.state_hash
        || source.eidx_frontier != verified.eidx_frontier
    {
        return Err(fault(
            "backup/restore-tree-proof",
            "main tree disagrees with its verified immutable coordinate",
        ));
    }

    let mut client = connection.connect_for("backup/connect")?;
    let basis_sql = i64::try_from(source.basis_t).map_err(|_| {
        SemanticError::incorrect("backup/tree-basis", "tree basis exceeds PostgreSQL bigint")
    })?;
    let identity = client
        .query_one(
            "SELECT h.log_generation, d.lineage_id \
               FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
              WHERE h.database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-tree-generation", error)
        })?;
    let target_generation = unsigned(identity.get(0), "restored tree generation")?;
    let target_lineage: String = identity.get(1);
    if target_generation == 0
        || target_lineage != manifest.lineage_id
        || target_generation != matched.generation
        || target_lineage != matched.lineage_id
    {
        return Err(fault(
            "backup/restore-tree-generation",
            "restored tree target is not on the expected lineage generation",
        ));
    }
    let generation_sql = sql_u64(target_generation, "restored tree generation")?;
    let (target_tx_hash, target_state_hash) = if source.basis_t == 0 {
        (manifest.genesis_hash, source.state_hash)
    } else {
        let row = client
            .query_one(
                "SELECT tx_hash, state_hash, eidx_frontier FROM atomic_generation_transactions \
                 WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                &[&target_database_id, &generation_sql, &basis_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-tree-log", error))?;
        if unsigned(row.get(2), "restored tree frontier")? != verified.eidx_frontier {
            return Err(fault(
                "backup/restore-tree-state",
                "restored tree basis has a different issued frontier",
            ));
        }
        (
            digest(row.get(0), "restored tree transaction hash")?,
            digest(row.get(1), "restored tree state hash")?,
        )
    };
    if target_state_hash != source.state_hash {
        return Err(fault(
            "backup/restore-tree-state",
            "restored tree basis has a different semantic state commitment",
        ));
    }
    let mut store = PostgresTreeStore::connect_configured(connection)?;
    let expected_revision = store.current_publication_revision(target_database_id)?;
    let publication_revision = expected_revision.checked_add(1).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/tree-revision-exhausted",
            "restored tree publication revision is exhausted",
        )
    })?;
    let target = PersistentTreeManifest {
        database_id: target_database_id.to_owned(),
        publication_revision,
        index_basis_t: source.index_basis_t,
        basis_t: source.basis_t,
        tx_hash: target_tx_hash,
        state_hash: target_state_hash,
        excision_generation: target_generation,
        eidx_frontier: source.eidx_frontier,
        trees: source.trees,
        pending_avet: source.pending_avet,
    };
    // Native request-base restores rebuild every persistent coordinate. An
    // older/no-request-base restore may legitimately have only its head seed;
    // exact canonical target-row proof plus the verified source witness still
    // proves an earlier main base without requiring a synthetic coordinate.
    if let Some(coordinate) = crate::persistent_commitment::load_persistent_coordinate(
        &mut client,
        target_database_id,
        target_generation,
        target.basis_t,
    )? && (coordinate.tx_hash != target.tx_hash
        || coordinate.state_hash != target.state_hash
        || coordinate.eidx_frontier != target.eidx_frontier)
    {
        return Err(fault(
            "backup/restore-tree-reconstruction",
            "persistent target coordinate disagrees with verified main tree",
        ));
    }
    crate::peer::validate_avet_work_directions(&target.pending_avet, &verified.schema)?;
    let payload = target.encode()?;
    let manifest_hash = sha256(&payload);
    let roots: Vec<TreeRootBinding> = target
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
        publication_revision,
        basis_t: target.basis_t,
        index_basis_t: target.index_basis_t,
        tx_hash: target.tx_hash,
        state_hash: target.state_hash,
        excision_generation: target_generation,
        eidx_frontier: target.eidx_frontier,
        manifest_hash,
        payload,
        roots,
    };
    // An ambiguous prior publication, or an indexer that already rebuilt the
    // same accelerator, is sufficient. Do not manufacture an endless stream
    // of equivalent physical revisions on restore retry.
    if let Some(existing) = client
        .query_opt(
            "SELECT p.publication_revision, m.manifest_hash FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.manifest_hash = p.manifest_hash \
                AND m.log_generation = p.log_generation \
              WHERE p.database_id = $1 AND p.log_generation = $2 \
                AND p.basis_t = $3 AND m.state_hash = $4 \
              ORDER BY p.publication_revision DESC LIMIT 1",
            &[
                &target_database_id,
                &generation_sql,
                &basis_sql,
                &&target_state_hash[..],
            ],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-tree-existing", error))?
    {
        let existing_revision = unsigned(existing.get(0), "existing restored tree revision")?;
        let existing_hash = digest(existing.get(1), "existing restored tree manifest hash")?;
        let existing_record = store
            .load_manifest(target_database_id, existing_revision)?
            .ok_or_else(|| {
                fault(
                    "backup/restore-tree-existing",
                    "published restored tree has no readable manifest record",
                )
            })?;
        if existing_record.manifest_hash != existing_hash
            || sha256(&existing_record.payload) != existing_hash
        {
            return Err(fault(
                "backup/restore-tree-existing-hash",
                "existing restored tree manifest bytes do not match their immutable hash",
            ));
        }
        let existing = PersistentTreeManifest::decode(&existing_record.payload)?;
        if existing.database_id == target_database_id
            && existing.basis_t == target.basis_t
            && existing.tx_hash == target.tx_hash
            && existing.state_hash == target.state_hash
            && existing.excision_generation == target.excision_generation
            && existing.eidx_frontier == target.eidx_frontier
        {
            validate_stored_tree_graph(&mut store, &existing)?;
            crate::peer::validate_avet_work_directions(&existing.pending_avet, &verified.schema)?;
            // Readiness is monotone at one immutable database coordinate.
            // A complete existing accelerator always dominates. Two partial
            // accelerators have no useful total order (clearing has no
            // ordinal), so preserve the valid existing value and resume it.
            // Only a complete backup source may replace a partial existing
            // value at the next physical revision.
            if existing.pending_avet.is_empty() || !target.pending_avet.is_empty() {
                if fault_at == RestoreFault::AfterTreePublication {
                    return Err(injected("backup/restore-after-tree-publication"));
                }
                return finish_restored_publication_work(&mut client, &mut store, existing_hash);
            }
        }
    }
    let mut reachable = BTreeSet::new();
    for tree_root in &target.trees {
        let root_hash = tree_root.descriptor.root_hash;
        let expected_root_bytes = tree_root.root_bytes;
        let validated = persistent_tree::validate_tree_streaming(&tree_root.descriptor, |hash| {
            let payload = read_object(directory, *hash)?;
            if *hash == root_hash && payload.len() as u64 != expected_root_bytes {
                return Err(fault(
                    "backup/tree-root-bytes",
                    "backed-up tree root size disagrees with its manifest",
                ));
            }
            Ok(payload)
        })?;
        reachable.extend(validated.node_hashes);
    }
    if let Some(expected) = &tree.legacy_node_hashes
        && expected.as_slice() != reachable.iter().copied().collect::<Vec<_>>()
    {
        return Err(fault(
            "backup/tree-node-set",
            "legacy tree node list is not its exact reachable closure",
        ));
    }
    store.begin_build_intent(
        target_database_id,
        record.excision_generation,
        expected_revision,
        manifest_hash,
        &reachable,
    )?;
    let publication = (|| {
        upload_backup_tree_nodes(&mut store, directory, &reachable)?;
        let delta = TreePublicationDelta::Replace {
            live_nodes: reachable,
        };
        store.publish_manifest_with_delta(&record, expected_revision, &delta)?;
        Ok(())
    })();
    let release = store.release_build_intent();
    match (publication, release) {
        (Err(error), _) => Err(error),
        (Ok(()), Err(error)) => Err(error),
        (Ok(()), Ok(())) => {
            if fault_at == RestoreFault::AfterTreePublication {
                return Err(injected("backup/restore-after-tree-publication"));
            }
            finish_restored_publication_work(&mut client, &mut store, manifest_hash)
        }
    }
}

/// Complete only this restored publication's finite membership work. Each
/// owner call commits at most 512 nodes outside any head/publication lock.
/// A concurrent successor can consume the final header before we observe it;
/// header disappearance is terminal, not an invitation to chase that newer
/// publication (and never a reason to rebuild/replay the restored database).
fn finish_restored_publication_work(
    client: &mut Client,
    store: &mut PostgresTreeStore,
    manifest_hash: Digest,
) -> Result<(), SemanticError> {
    loop {
        let pending = client
            .query_opt(
                "SELECT delta_state FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
                &[&&manifest_hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-tree-work", error))?;
        let Some(pending) = pending else {
            return Ok(());
        };
        if pending.get::<_, i16>(0) != 2 {
            return Err(fault(
                "backup/restore-tree-work-state",
                "published restored tree has a non-published membership work header",
            ));
        }
        // false can mean this exact header was consumed and superseded in
        // between the two calls. Reselect the same hash, never the latest.
        store.advance_publication_work(manifest_hash)?;
    }
}

/// Reuse the normal bounded upload/byte-verification path during restoration.
/// The source iterator never materializes the complete archive node payloads.
/// A valid oversized node is still admitted alone, under the existing codec
/// maximum; upload limits bound ordinary batch buffers, not datom semantics.
fn upload_backup_tree_nodes(
    store: &mut PostgresTreeStore,
    directory: &Path,
    hashes: &BTreeSet<Digest>,
) -> Result<(), SemanticError> {
    upload_restored_tree_nodes(store, hashes, &mut |hash| read_object(directory, *hash))
}

fn upload_restored_tree_nodes(
    store: &mut PostgresTreeStore,
    hashes: &BTreeSet<Digest>,
    load_node: &mut dyn FnMut(&Digest) -> Result<Vec<u8>, SemanticError>,
) -> Result<(), SemanticError> {
    let limits = crate::NodeUploadLimits::default();
    let mut batch = Vec::<(Digest, Vec<u8>)>::new();
    let mut bytes = 0_usize;
    for hash in hashes {
        let payload = load_node(hash)?;
        if !batch.is_empty()
            && (batch.len() >= limits.max_nodes
                || bytes.saturating_add(payload.len()) > limits.max_bytes)
        {
            store.insert_nodes(
                batch
                    .iter()
                    .map(|(hash, payload)| (*hash, payload.as_slice())),
                limits,
            )?;
            batch.clear();
            bytes = 0;
        }
        bytes = bytes.saturating_add(payload.len());
        batch.push((*hash, payload));
        if batch.len() >= limits.max_nodes || bytes >= limits.max_bytes {
            store.insert_nodes(
                batch
                    .iter()
                    .map(|(hash, payload)| (*hash, payload.as_slice())),
                limits,
            )?;
            batch.clear();
            bytes = 0;
        }
    }
    if !batch.is_empty() {
        store.insert_nodes(
            batch
                .iter()
                .map(|(hash, payload)| (*hash, payload.as_slice())),
            limits,
        )?;
    }
    Ok(())
}

fn validate_stored_tree_graph(
    store: &mut PostgresTreeStore,
    manifest: &PersistentTreeManifest,
) -> Result<(), SemanticError> {
    for tree in &manifest.trees {
        let root_hash = tree.descriptor.root_hash;
        let expected_root_bytes = tree.root_bytes;
        persistent_tree::validate_tree_streaming(&tree.descriptor, |hash| {
            let payload = store.load_node(*hash)?.ok_or_else(|| {
                fault(
                    "backup/restore-tree-existing-node",
                    format!("existing tree node {} is missing", hex(hash)),
                )
            })?;
            if *hash == root_hash && payload.len() as u64 != expected_root_bytes {
                return Err(fault(
                    "backup/restore-tree-existing-root-bytes",
                    "existing tree root size disagrees with its manifest",
                ));
            }
            Ok(payload)
        })?;
    }
    Ok(())
}

fn restore_request_base_trees(
    connection: &PostgresConnectionConfig,
    client: &mut Client,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    target_database_id: &str,
    target_generation: u64,
) -> Result<(), SemanticError> {
    let mut bindings = BTreeMap::<Digest, Vec<Digest>>::new();
    for entry in &log.entries {
        match (entry.request.request_kind, entry.request.base_manifest_hash) {
            (2, Some(base)) => bindings
                .entry(base)
                .or_default()
                .push(entry.request.request_key_hash),
            (0 | 1, None) => {}
            _ => {
                return Err(fault(
                    "backup/request-base-binding",
                    "request kind and portable base-tree binding disagree",
                ));
            }
        }
    }
    let mut archives = bindings
        .into_iter()
        .map(|(portable_manifest_hash, request_keys)| {
            let tree = TreeBackup {
                manifest_hash: portable_manifest_hash,
                legacy_node_hashes: None,
            };
            let source = decode_bound_tree_manifest(directory, manifest, log, &tree, None)?;
            Ok((source.basis_t, portable_manifest_hash, source, request_keys))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    // The archive revision is local to this restore generation and derived
    // solely from authenticated backup content, so interrupted staging
    // resumes at the identical immutable coordinates.
    archives.sort_by_key(|(basis, portable, _, _)| (*basis, *portable));
    for (offset, (_, _portable_manifest_hash, source, request_keys)) in
        archives.into_iter().enumerate()
    {
        let archive_revision = request_base_archive_revision(offset)?;
        restore_request_base_archive(
            connection,
            client,
            directory,
            manifest,
            target_database_id,
            target_generation,
            archive_revision,
            source,
            &request_keys,
        )?;
    }
    Ok(())
}

/// PersistentTreeManifest retains the source implementation's numeric
/// publication coordinate. Archive roots are not members of the ordinary
/// monotonically increasing publication chain, so allocate them downward
/// from PostgreSQL BIGINT's ceiling. This deterministic disjoint namespace
/// prevents an otherwise identical basis-zero archive and accelerator from
/// acquiring the same content hash and becoming an ambiguous request base.
fn request_base_archive_revision(offset: usize) -> Result<u64, SemanticError> {
    (i64::MAX as u64)
        .checked_sub(u64::try_from(offset).map_err(|_| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "backup/request-base-archive-count",
                "restored request-base archive count is not representable",
            )
        })?)
        .filter(|revision| *revision > 0)
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "backup/request-base-archive-count",
                "restored request-base archive count exceeds its revision namespace",
            )
        })
}

/// Bound old receipt reconstruction by both transaction count and owned
/// datom/value bytes. A single historical transaction remains indivisible:
/// checkpoint on either side rather than splitting its semantics.
fn legacy_receipt_checkpoints(log: &LoadedBackupLog) -> Result<BTreeSet<u64>, SemanticError> {
    let Some(last) = log
        .entries
        .iter()
        .rev()
        .find(|entry| entry.request.request_kind == 1)
    else {
        return Ok(BTreeSet::new());
    };
    let through = last.transaction.basis_t - 1;
    let mut checkpoints = BTreeSet::from([0]);
    let mut count = 0_usize;
    let mut bytes = 0_u64;
    let mut datoms = 0_u64;
    for entry in log
        .entries
        .iter()
        .take_while(|entry| entry.transaction.basis_t <= through)
    {
        let transaction = &entry.transaction;
        let size = transaction.tx_data.iter().fold(
            crate::encode_transaction(transaction)?.len() as u64,
            |total, datom| total.saturating_add(datom.retained_bytes()),
        );
        let width = transaction.tx_data.len() as u64;
        if count > 0
            && (count == RESTORE_BATCH_ROWS
                || bytes.saturating_add(size) > LEGACY_RECEIPT_TAIL_BYTES
                || datoms.saturating_add(width) > LEGACY_RECEIPT_TAIL_DATOMS)
        {
            checkpoints.insert(transaction.basis_t - 1);
            count = 0;
            bytes = 0;
            datoms = 0;
        }
        count += 1;
        bytes = bytes.saturating_add(size);
        datoms = datoms.saturating_add(width);
        if count == RESTORE_BATCH_ROWS
            || bytes >= LEGACY_RECEIPT_TAIL_BYTES
            || datoms >= LEGACY_RECEIPT_TAIL_DATOMS
        {
            checkpoints.insert(transaction.basis_t);
            count = 0;
            bytes = 0;
            datoms = 0;
        }
    }
    checkpoints.insert(through);
    Ok(checkpoints)
}

// A separate owned session gives pin cleanup an unambiguous failure boundary:
// dropping it releases every transferred lock even if SQL unlock fails. It
// never contaminates the reusable PortableBackup binding client.
struct RestoreScratchPins {
    client: Client,
    keys: Vec<i64>,
}

impl RestoreScratchPins {
    fn upload(
        &mut self,
        store: &mut PostgresTreeStore,
        build: &crate::peer::FullNativeTreeBuild,
    ) -> Result<(), SemanticError> {
        debug_assert!(self.keys.len() < RESTORE_SCRATCH_PINS);
        let hash = sha256(&build.manifest.encode()?);
        let nodes = build.nodes.iter().map(|(hash, _)| *hash).collect();
        // This is an archive upload, not the normal publication chain. Its
        // deterministic private coordinate must survive unrelated indexing
        // progress between an interrupted restore and its retry.
        let revision = build.manifest.publication_revision - 1;
        store.begin_build_intent(
            &build.manifest.database_id,
            build.manifest.excision_generation,
            revision,
            hash,
            &nodes,
        )?;
        let staged = (|| {
            let key = crate::tree_store::tree_build_advisory_key(&hash);
            self.client
                .query_one("SELECT pg_advisory_lock_shared($1)", &[&key])
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-scratch-pin", error)
                })?;
            self.keys.push(key);
            store.insert_nodes(
                build.nodes.iter().map(|(hash, bytes)| (*hash, bytes)),
                store.node_upload_limits(),
            )?;
            Ok(())
        })();
        let release = store.release_build_intent();
        staged?;
        release?;
        Ok(())
    }

    fn clear(&mut self) -> Result<(), SemanticError> {
        for key in self.keys.drain(..) {
            if !self
                .client
                .query_one("SELECT pg_advisory_unlock_shared($1)", &[&key])
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-scratch-unpin", error)
                })?
                .get::<_, bool>(0)
            {
                return Err(fault(
                    "backup/restore-scratch-pin-lost",
                    "restore scratch upload lost its session pin",
                ));
            }
        }
        Ok(())
    }
}

#[allow(clippy::too_many_arguments)]
fn restore_legacy_receipt_trees(
    connection: &PostgresConnectionConfig,
    client: &mut Client,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    prepared: &PreparedRestore,
    target_database_id: &str,
    target_generation: u64,
) -> Result<(), SemanticError> {
    let checkpoints = legacy_receipt_checkpoints(log)?;
    if checkpoints.is_empty() {
        return Ok(());
    }
    let mut offset = request_base_trees(log).len();
    let mut next_revision = || {
        let revision = request_base_archive_revision(offset)?;
        offset += 1;
        Ok::<_, SemanticError>(revision)
    };
    let genesis = Database::from_genesis(decode_genesis(&read_object(
        directory,
        manifest.genesis_hash,
    )?)?)?;
    let (mut builder, first) = crate::peer::RestoreTreeBuilder::new(
        target_database_id,
        target_generation,
        manifest.genesis_hash,
        &genesis,
        next_revision()?,
    )?;
    let mut store = PostgresTreeStore::connect_configured(connection)?;
    let mut scratch = RestoreScratchPins {
        client: connection.connect_for("backup/restore-scratch-connect")?,
        keys: Vec::new(),
    };
    let mut persist = |build: crate::peer::FullNativeTreeBuild,
                       archive: bool,
                       store: &mut PostgresTreeStore|
     -> Result<(), SemanticError> {
        scratch.upload(store, &build)?;
        if archive || scratch.keys.len() == RESTORE_SCRATCH_PINS {
            let revision = build.manifest.publication_revision;
            restore_request_base_archive_from_nodes(
                connection,
                client,
                manifest,
                target_database_id,
                target_generation,
                revision,
                build.manifest,
                &[],
                &mut |hash| {
                    store.load_node(*hash)?.ok_or_else(|| {
                        fault(
                            "backup/restore-scratch-node-missing",
                            "staged receipt checkpoint node is missing",
                        )
                    })
                },
            )?;
            // A complete generation-owned archive now protects every reused
            // descendant, including earlier projection-step output.
            scratch.clear()?;
        }
        Ok(())
    };
    persist(first, true, &mut store)?;
    builder.finish_upload(&mut store)?;
    let mut previous = manifest.genesis_hash;
    let mut tail = Vec::new();
    let through = *checkpoints.last().expect("nonempty checkpoints");
    for row in prepared
        .rows
        .iter()
        .take_while(|row| row.basis_t <= through)
    {
        let content = LineageTransactionContent::decode(&row.content_payload)?;
        let mut transaction = content.to_transaction(previous);
        transaction.database_id = target_database_id.to_owned();
        let hash = crate::log_generation::generation_transaction_hash(
            &manifest.lineage_id,
            target_generation,
            row.basis_t,
            previous,
            row.content_hash,
            row.state_hash,
            row.eidx_frontier,
        )?;
        tail.push((hash, transaction));
        previous = hash;
        if checkpoints.contains(&row.basis_t) {
            let build = builder.advance(&mut store, &tail, row.state_hash, next_revision()?)?;
            let complete = !builder.has_pending_projection();
            persist(build, complete, &mut store)?;
            builder.finish_upload(&mut store)?;
            while builder.has_pending_projection() {
                let build = builder.advance_projection(&mut store, next_revision()?)?;
                let complete = !builder.has_pending_projection();
                persist(build, complete, &mut store)?;
                builder.finish_upload(&mut store)?;
            }
            tail.clear();
        }
    }
    debug_assert!(tail.is_empty());
    debug_assert_eq!(builder.stats().tail_transactions, through);
    #[cfg(test)]
    eprintln!(
        "legacy receipt archive restore: checkpoints={}, builder={:?}, tree_store_only={:?}",
        checkpoints.len(),
        builder.stats(),
        store.stats()
    );
    Ok(())
}

#[allow(clippy::too_many_arguments)]
fn restore_request_base_archive(
    connection: &PostgresConnectionConfig,
    binding_client: &mut Client,
    directory: &Path,
    backup: &Manifest,
    target_database_id: &str,
    target_generation: u64,
    archive_revision: u64,
    source: PersistentTreeManifest,
    request_keys: &[Digest],
) -> Result<(), SemanticError> {
    restore_request_base_archive_from_nodes(
        connection,
        binding_client,
        backup,
        target_database_id,
        target_generation,
        archive_revision,
        source,
        request_keys,
        &mut |hash| read_object(directory, *hash),
    )
}

#[allow(clippy::too_many_arguments)]
fn restore_request_base_archive_from_nodes(
    connection: &PostgresConnectionConfig,
    binding_client: &mut Client,
    backup: &Manifest,
    target_database_id: &str,
    target_generation: u64,
    archive_revision: u64,
    source: PersistentTreeManifest,
    request_keys: &[Digest],
    load_node: &mut dyn FnMut(&Digest) -> Result<Vec<u8>, SemanticError>,
) -> Result<(), SemanticError> {
    let generation_sql = sql_u64(target_generation, "restored request-base generation")?;
    let basis_sql = sql_u64(source.basis_t, "restored request-base basis")?;
    let (target_tx_hash, target_state_hash) = if source.basis_t == 0 {
        (backup.genesis_hash, source.state_hash)
    } else {
        let row = binding_client
            .query_opt(
                "SELECT tx_hash, state_hash FROM atomic_generation_transactions \
                  WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                &[&target_database_id, &generation_sql, &basis_sql],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-request-base-log", error)
            })?
            .ok_or_else(|| {
                fault(
                    "backup/restore-request-base-log",
                    "restored request base has no target generation coordinate",
                )
            })?;
        (
            digest(row.get(0), "restored request-base transaction hash")?,
            digest(row.get(1), "restored request-base state hash")?,
        )
    };
    if target_state_hash != source.state_hash {
        return Err(fault(
            "backup/restore-request-base-state",
            "restored request base has a different semantic state commitment",
        ));
    }
    let target = PersistentTreeManifest {
        database_id: target_database_id.to_owned(),
        publication_revision: archive_revision,
        index_basis_t: source.index_basis_t,
        basis_t: source.basis_t,
        tx_hash: target_tx_hash,
        state_hash: target_state_hash,
        excision_generation: target_generation,
        eidx_frontier: source.eidx_frontier,
        trees: source.trees,
        pending_avet: source.pending_avet,
    };
    let payload = target.encode()?;
    let target_manifest_hash = sha256(&payload);
    let roots: Vec<TreeRootBinding> = target
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
    let mut reachable = BTreeSet::new();
    for tree_root in &target.trees {
        let root_hash = tree_root.descriptor.root_hash;
        let expected_root_bytes = tree_root.root_bytes;
        let validated = persistent_tree::validate_tree_streaming(&tree_root.descriptor, |hash| {
            let payload = load_node(hash)?;
            if *hash == root_hash && payload.len() as u64 != expected_root_bytes {
                return Err(fault(
                    "backup/tree-root-bytes",
                    "backed-up request-base root size disagrees with its manifest",
                ));
            }
            Ok(payload)
        })?;
        reachable.extend(validated.node_hashes);
    }
    let node_set_hash = request_base_archive_node_set_hash(&reachable);
    let expected_node_count = sql_u64(reachable.len() as u64, "request-base archive nodes")?;
    let archive_revision_sql = sql_u64(archive_revision, "request-base archive revision")?;
    let manifest_version = PersistentTreeManifest::encoded_version(&payload)?;
    binding_client
        .execute(
            "INSERT INTO atomic_request_base_archives \
                 (database_id, generation, archive_revision, basis_t, tx_hash, state_hash, \
                  eidx_frontier, manifest_version, manifest_hash, payload, \
                  expected_node_count, node_set_hash) \
             VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12) \
             ON CONFLICT (manifest_hash) DO NOTHING",
            &[
                &target_database_id,
                &generation_sql,
                &archive_revision_sql,
                &basis_sql,
                &&target.tx_hash[..],
                &&target.state_hash[..],
                &sql_u64(target.eidx_frontier, "request-base archive frontier")?,
                &manifest_version,
                &&target_manifest_hash[..],
                &payload,
                &expected_node_count,
                &&node_set_hash[..],
            ],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-request-base-archive", error)
        })?;
    let stored = binding_client
        .query_one(
            "SELECT database_id, generation, archive_revision, basis_t, tx_hash, state_hash, \
                    eidx_frontier, manifest_version, payload, expected_node_count, node_set_hash \
               FROM atomic_request_base_archives WHERE manifest_hash = $1",
            &[&&target_manifest_hash[..]],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-request-base-archive-verify", error)
        })?;
    if stored.get::<_, String>(0) != target_database_id
        || stored.get::<_, i64>(1) != generation_sql
        || stored.get::<_, i64>(2) != archive_revision_sql
        || stored.get::<_, i64>(3) != basis_sql
        || digest(stored.get(4), "request-base archive transaction")? != target.tx_hash
        || digest(stored.get(5), "request-base archive state")? != target.state_hash
        || unsigned(stored.get(6), "request-base archive frontier")? != target.eidx_frontier
        || stored.get::<_, i16>(7) != manifest_version
        || stored.get::<_, Vec<u8>>(8) != payload
        || stored.get::<_, i64>(9) != expected_node_count
        || digest(stored.get(10), "request-base archive node set")? != node_set_hash
    {
        return Err(fault(
            "backup/restore-request-base-archive-conflict",
            "request-base archive identity is occupied by different immutable content",
        ));
    }

    let already_complete: bool = binding_client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_request_base_archive_completions \
                             WHERE manifest_hash = $1)",
            &[&&target_manifest_hash[..]],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-request-base-completion-read", error)
        })?
        .get(0);
    if !already_complete {
        let hashes = reachable.iter().copied().collect::<Vec<_>>();
        for chunk in hashes.chunks(RESTORE_BATCH_ROWS) {
            let batch = chunk.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
            binding_client
                .execute(
                    "INSERT INTO atomic_request_base_archive_nodes(manifest_hash, node_hash) \
                     SELECT $1, node_hash FROM unnest($2::bytea[]) AS node_hash \
                     ON CONFLICT DO NOTHING",
                    &[&&target_manifest_hash[..], &batch],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-request-base-node-plan", error)
                })?;
        }
        let mut store = PostgresTreeStore::connect_configured(connection)?;
        upload_restored_tree_nodes(&mut store, &reachable, load_node)?;
        for root in &roots {
            let order: i16 = match root.order {
                crate::IndexOrder::Eavt => 0,
                crate::IndexOrder::Aevt => 1,
                crate::IndexOrder::Avet => 2,
                crate::IndexOrder::Vaet => 3,
            };
            binding_client
                .execute(
                    "INSERT INTO atomic_request_base_archive_roots \
                         (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
                     VALUES ($1, $2, $3, $4, $5, $6) ON CONFLICT DO NOTHING",
                    &[
                        &&target_manifest_hash[..],
                        &order,
                        &root.history,
                        &&root.root_hash[..],
                        &sql_u64(root.datom_count, "request-base root datoms")?,
                        &sql_u64(root.encoded_bytes, "request-base root bytes")?,
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-request-base-root", error)
                })?;
        }
        binding_client
            .query_one(
                "SELECT atomic_complete_request_base_archive($1, $2, $3)",
                &[
                    &target_database_id,
                    &generation_sql,
                    &&target_manifest_hash[..],
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-request-base-complete", error)
            })?;
    }
    // The complete archive and every immutable request binding are durable
    // before the candidate head is made visible. Ordinary peer selection has
    // no path to this table; only the exact bound-manifest reader can open it.
    insert_restored_request_base_bindings(
        binding_client,
        target_database_id,
        target_generation,
        target_manifest_hash,
        request_keys,
    )
}

fn request_base_archive_node_set_hash(node_hashes: &BTreeSet<Digest>) -> Digest {
    let mut hasher = Sha256::new();
    hasher.update(b"atomic/request-base-archive-node-set/v1\0");
    hasher.update((node_hashes.len() as u64).to_be_bytes());
    for node_hash in node_hashes {
        hasher.update(node_hash);
    }
    hasher.finalize().into()
}

fn insert_restored_request_base_bindings(
    client: &mut Client,
    target_database_id: &str,
    target_generation: u64,
    target_manifest_hash: Digest,
    request_keys: &[Digest],
) -> Result<(), SemanticError> {
    let generation_sql = sql_u64(target_generation, "restored request-base generation")?;
    let mut transaction = client.transaction().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-request-base-bind-begin", error)
    })?;
    for request_key in request_keys {
        transaction
            .execute(
                "INSERT INTO atomic_generation_request_bases \
                     (database_id, generation, request_key_hash, base_manifest_hash) \
                 VALUES ($1, $2, $3, $4) ON CONFLICT DO NOTHING",
                &[
                    &target_database_id,
                    &generation_sql,
                    &&request_key[..],
                    &&target_manifest_hash[..],
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-request-base-bind", error)
            })?;
        let stored = transaction
            .query_one(
                "SELECT base_manifest_hash FROM atomic_generation_request_bases \
                  WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3",
                &[&target_database_id, &generation_sql, &&request_key[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-request-base-verify", error)
            })?;
        if digest(stored.get(0), "restored request-base binding")? != target_manifest_hash {
            return Err(fault(
                "backup/restore-request-base-conflict",
                "restored request already names a different immutable db-before base",
            ));
        }
    }
    transaction.commit().map_err(|error| {
        crate::postgres::postgres_error("backup/restore-request-base-bind-commit", error)
    })
}

/// Captured-log coordinates needed to authenticate a replaceable tree root.
/// Keeping them together makes the invariant visible: every selected tree is
/// checked against one immutable snapshot tail, never against independently
/// sampled arrays.
struct TreeCaptureLog<'a> {
    start_basis: u64,
    backup_basis: u64,
    source_transaction_hashes: &'a [Digest],
    portable_transaction_hashes: &'a [Digest],
    portable_frontiers: &'a [u64],
    state_hashes: &'a [Digest],
}

#[derive(Clone, Copy)]
struct TreeCaptureContext<'a, 'log> {
    database_id: &'a str,
    lineage_id: &'a str,
    log_generation: u64,
    log: &'a TreeCaptureLog<'log>,
}

#[derive(Clone, Copy)]
enum TreeCaptureSource {
    Publication,
    RequestBaseArchive {
        expected_node_count: u64,
        node_set_hash: Digest,
    },
}

fn capture_tree_backup<C: crate::sql_io::GenericClient>(
    client: &mut C,
    database_id: &str,
    lineage_id: &str,
    log_generation: u64,
    log: &TreeCaptureLog<'_>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Option<TreeBackup>, SemanticError> {
    let backup_basis_sql = i64::try_from(log.backup_basis).map_err(|_| {
        SemanticError::incorrect(
            "backup/basis-overflow",
            "backup basis exceeds PostgreSQL bigint",
        )
    })?;
    let start_basis_sql = i64::try_from(log.start_basis).map_err(|_| {
        SemanticError::incorrect(
            "backup/basis-overflow",
            "tree start basis exceeds PostgreSQL bigint",
        )
    })?;
    let generation_sql = i64::try_from(log_generation).map_err(|_| {
        SemanticError::incorrect(
            "backup/generation-overflow",
            "tree generation exceeds PostgreSQL bigint",
        )
    })?;
    let rows = client
        .query(
            "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                    m.excision_generation, m.eidx_frontier, m.manifest_hash, m.payload \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t \
                AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
                AND m.log_generation = p.log_generation \
              JOIN atomic_database_generations g \
                 ON g.database_id = m.database_id \
                AND g.excision_generation = m.excision_generation \
              WHERE p.database_id = $1 AND p.log_generation = $2 \
                AND p.basis_t >= $3 AND p.basis_t <= $4 \
              ORDER BY p.publication_revision DESC",
            &[
                &database_id,
                &generation_sql,
                &start_basis_sql,
                &backup_basis_sql,
            ],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/tree-publication", error))?;
    // Native trees are replaceable accelerators, never backup authority. Walk
    // newest to oldest and use the first root whose complete immutable graph
    // authenticates against the captured log. A damaged derived root must not
    // prevent a valid logical backup; if no physical candidate survives, log
    // replay remains an exact restore path.
    for row in rows {
        if let Some(tree) = capture_tree_candidate(
            client,
            &row,
            TreeCaptureSource::Publication,
            TreeCaptureContext {
                database_id,
                lineage_id,
                log_generation,
                log,
            },
            publisher,
        )? {
            return Ok(Some(tree));
        }
    }
    Ok(None)
}

fn capture_tree_candidate<C: crate::sql_io::GenericClient>(
    client: &mut C,
    row: &postgres::Row,
    source: TreeCaptureSource,
    context: TreeCaptureContext<'_, '_>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Option<TreeBackup>, SemanticError> {
    let TreeCaptureContext {
        database_id,
        lineage_id,
        log_generation,
        log,
    } = context;
    let Ok(publication_revision) = unsigned(row.get(0), "tree publication revision") else {
        return Ok(None);
    };
    let Ok(basis_t) = unsigned(row.get(1), "tree basis") else {
        return Ok(None);
    };
    let Ok(tx_hash) = digest(row.get(2), "tree transaction hash") else {
        return Ok(None);
    };
    let Ok(state_hash) = digest(row.get(3), "tree state hash") else {
        return Ok(None);
    };
    let Ok(excision_generation) = unsigned(row.get(4), "tree excision generation") else {
        return Ok(None);
    };
    let Ok(eidx_frontier) = unsigned(row.get(5), "tree entity frontier") else {
        return Ok(None);
    };
    let Ok(manifest_hash) = digest(row.get(6), "tree manifest hash") else {
        return Ok(None);
    };
    let payload: Vec<u8> = row.get(7);
    let Some(offset) = basis_t
        .checked_sub(log.start_basis)
        .and_then(|offset| usize::try_from(offset).ok())
    else {
        return Ok(None);
    };
    let Some(&portable_tx_hash) = log.portable_transaction_hashes.get(offset) else {
        return Ok(None);
    };
    if log.source_transaction_hashes.get(offset) != Some(&tx_hash)
        || log.state_hashes.get(offset) != Some(&state_hash)
        || log.portable_frontiers.get(offset) != Some(&eidx_frontier)
        || sha256(&payload) != manifest_hash
    {
        return Ok(None);
    }
    let Ok(decoded) = PersistentTreeManifest::decode(&payload) else {
        return Ok(None);
    };
    if decoded.database_id != database_id
        || decoded.publication_revision != publication_revision
        || decoded.basis_t != basis_t
        || decoded.tx_hash != tx_hash
        || decoded.state_hash != state_hash
        || decoded.excision_generation != excision_generation
        || excision_generation != log_generation
        || decoded.eidx_frontier != eidx_frontier
    {
        return Ok(None);
    }
    // Keep the selected immutable graph live until this repeatable-read
    // snapshot has copied its last node. GC takes the matching exclusive
    // transaction lock before removing an obsolete publication or any node
    // reachable only through it.
    client
        .query_one(
            "SELECT pg_advisory_xact_lock_shared($1)",
            &[&crate::operations::tree_manifest_advisory_key(
                &manifest_hash,
            )],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/tree-root-pin", error))?;
    let roots_sql = match source {
        TreeCaptureSource::Publication => {
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $1 \
              ORDER BY history, index_order"
        }
        TreeCaptureSource::RequestBaseArchive { .. } => {
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_request_base_archive_roots WHERE manifest_hash = $1 \
              ORDER BY history, index_order"
        }
    };
    let root_rows = client
        .query(roots_sql, &[&&manifest_hash[..]])
        .map_err(|error| crate::postgres::postgres_error("backup/tree-roots-read", error))?;
    if root_rows.len() != decoded.trees.len() {
        return Ok(None);
    }
    for (root_row, tree) in root_rows.into_iter().zip(&decoded.trees) {
        let Ok(order) = decode_tree_order(root_row.get(0)) else {
            return Ok(None);
        };
        let Ok(root_hash) = digest(root_row.get(2), "tree root hash") else {
            return Ok(None);
        };
        let Ok(root_count) = unsigned(root_row.get(3), "tree root count") else {
            return Ok(None);
        };
        let Ok(root_bytes) = unsigned(root_row.get(4), "tree root bytes") else {
            return Ok(None);
        };
        if order != tree.descriptor.order
            || root_row.get::<_, bool>(1) != tree.descriptor.history
            || root_hash != tree.descriptor.root_hash
            || root_count != tree.descriptor.count
            || root_bytes != tree.root_bytes
        {
            return Ok(None);
        }
    }

    // First authenticate the complete candidate without publishing any of it.
    // If validation fails we can try an older root without leaving copied junk
    // that no backup root could ever reference. SQL read failures remain fatal;
    // only malformed/missing derived content is eligible for fallback.
    let mut node_hashes = BTreeSet::new();
    for tree in &decoded.trees {
        let root_hash = tree.descriptor.root_hash;
        let expected_root_bytes = tree.root_bytes;
        let mut read_error = None;
        let validation = persistent_tree::validate_tree_streaming(&tree.descriptor, |hash| {
            let row = match client.query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            ) {
                Ok(row) => row,
                Err(error) => {
                    let error = crate::postgres::postgres_error("backup/tree-node-read", error);
                    read_error = Some(error.clone());
                    return Err(error);
                }
            }
            .ok_or_else(|| {
                fault(
                    "backup/tree-node-missing",
                    format!("published tree node {} is missing", hex(hash)),
                )
            })?;
            let payload: Vec<u8> = row.get(0);
            if *hash == root_hash && payload.len() as u64 != expected_root_bytes {
                return Err(fault(
                    "backup/tree-root-bytes",
                    "native tree root size disagrees with its manifest",
                ));
            }
            Ok(payload)
        });
        if let Some(error) = read_error {
            return Err(error);
        }
        let Ok(validated) = validation else {
            return Ok(None);
        };
        debug_assert!(validated.peak_live_decoded_nodes <= 3);
        debug_assert_eq!(validated.peak_live_payloads, 1);
        node_hashes.extend(validated.node_hashes);
    }
    if let TreeCaptureSource::RequestBaseArchive {
        expected_node_count,
        node_set_hash,
    } = source
        && (node_hashes.len() as u64 != expected_node_count
            || request_base_archive_node_set_hash(&node_hashes) != node_set_hash)
    {
        return Ok(None);
    }

    // Publish children only after the whole physical graph proved valid. The
    // portable manifest follows its children, and the snapshot root follows
    // every object after this function returns.
    for hash in &node_hashes {
        let row = client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/tree-node-copy", error))?
            .ok_or_else(|| {
                fault(
                    "backup/tree-node-vanished",
                    format!("validated tree node {} vanished", hex(hash)),
                )
            })?;
        let payload: Vec<u8> = row.get(0);
        persistent_tree::decode_tree_node(hash, &payload)?;
        publisher.publish(*hash, &payload)?;
    }

    // Tree nodes are already identity-free content. Rebind only the native
    // root envelope to the portable lineage transaction chain and normalize
    // PostgreSQL's replaceable publication/generation coordinates. Those
    // coordinates govern the live cache, not the logical backup point.
    let portable = PersistentTreeManifest {
        database_id: lineage_id.to_owned(),
        publication_revision: 1,
        index_basis_t: decoded.index_basis_t,
        basis_t,
        tx_hash: portable_tx_hash,
        state_hash,
        excision_generation: log_generation,
        eidx_frontier,
        trees: decoded.trees,
        pending_avet: decoded.pending_avet,
    };
    let portable_payload = portable.encode()?;
    let portable_manifest_hash = sha256(&portable_payload);
    publisher.publish(portable_manifest_hash, &portable_payload)?;
    Ok(Some(TreeBackup {
        manifest_hash: portable_manifest_hash,
        legacy_node_hashes: None,
    }))
}

/// Copy the exact db-before retained by a native request. The binding and its
/// semantic coordinate are receipt authority; its physical tree remains a
/// replaceable accelerator. At this explicit administrative boundary a
/// corrupt tree is rebuilt from the pinned immutable generation log rather
/// than imposing unbounded replay on the ordinary writer.
fn capture_bound_request_tree<C: crate::sql_io::GenericClient>(
    client: &mut C,
    database_id: &str,
    lineage_id: &str,
    log_generation: u64,
    source_manifest_hash: Digest,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Digest, SemanticError> {
    if log_generation == 0 {
        return Err(fault(
            "backup/request-base-generation",
            "native request bases require a positive source generation",
        ));
    }
    let generation_sql = sql_u64(log_generation, "request base generation")?;
    let variants = client
        .query_one(
            "SELECT (SELECT count(*) FROM atomic_tree_publications publication \
                      WHERE publication.database_id = $1 \
                        AND publication.log_generation = $2 \
                        AND publication.manifest_hash = $3), \
                    (SELECT count(*) FROM atomic_request_base_archives archive \
                      JOIN atomic_request_base_archive_completions complete \
                        ON complete.manifest_hash = archive.manifest_hash \
                      WHERE archive.database_id = $1 \
                        AND archive.generation = $2 \
                        AND archive.manifest_hash = $3)",
            &[&database_id, &generation_sql, &&source_manifest_hash[..]],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/request-base-variants", error))?;
    let normal_count = unsigned(variants.get(0), "request base publication variants")?;
    let archive_count = unsigned(variants.get(1), "request base archive variants")?;
    if normal_count.saturating_add(archive_count) != 1 {
        return Err(fault(
            "backup/request-base-variant",
            "native request must name exactly one normal publication or completed archive",
        ));
    }
    let (row, source) = if normal_count == 1 {
        let row = client
            .query_opt(
                "SELECT p.publication_revision, m.basis_t, m.tx_hash, m.state_hash, \
                        m.excision_generation, m.eidx_frontier, m.manifest_hash, m.payload \
                   FROM atomic_tree_publications p \
                   JOIN atomic_tree_manifests m \
                     ON m.database_id = p.database_id \
                    AND m.publication_revision = p.publication_revision \
                    AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                    AND m.manifest_hash = p.manifest_hash \
                    AND m.log_generation = p.log_generation \
                  WHERE p.database_id = $1 AND p.log_generation = $2 \
                    AND p.manifest_hash = $3 \
                    AND EXISTS ( \
                        SELECT 1 FROM atomic_semantic_commitment_roots semantic \
                         WHERE semantic.database_id = m.database_id \
                           AND semantic.generation = m.log_generation \
                           AND semantic.basis_t = m.basis_t \
                           AND semantic.tx_hash = m.tx_hash \
                           AND semantic.state_hash = m.state_hash \
                           AND semantic.eidx_frontier = m.eidx_frontier \
                           AND semantic.commitment_version = 2 \
                    )",
                &[&database_id, &generation_sql, &&source_manifest_hash[..]],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/request-base-read", error))?
            .ok_or_else(|| {
                fault(
                    "backup/request-base-corrupt",
                    "normal request base has no authenticated manifest and semantic coordinate",
                )
            })?;
        (row, TreeCaptureSource::Publication)
    } else {
        let row = client
            .query_opt(
                "SELECT archive.archive_revision, archive.basis_t, archive.tx_hash, \
                        archive.state_hash, archive.generation, archive.eidx_frontier, \
                        archive.manifest_hash, archive.payload, \
                        archive.expected_node_count, archive.node_set_hash \
                   FROM atomic_request_base_archives archive \
                   JOIN atomic_request_base_archive_completions complete \
                     ON complete.manifest_hash = archive.manifest_hash \
                  WHERE archive.database_id = $1 AND archive.generation = $2 \
                    AND archive.manifest_hash = $3 \
                    AND EXISTS ( \
                        SELECT 1 FROM atomic_semantic_commitment_roots semantic \
                         WHERE semantic.database_id = archive.database_id \
                           AND semantic.generation = archive.generation \
                           AND semantic.basis_t = archive.basis_t \
                           AND semantic.tx_hash = archive.tx_hash \
                           AND semantic.state_hash = archive.state_hash \
                           AND semantic.eidx_frontier = archive.eidx_frontier \
                           AND semantic.commitment_version = 2 \
                    )",
                &[&database_id, &generation_sql, &&source_manifest_hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/request-base-archive-read", error)
            })?
            .ok_or_else(|| {
                fault(
                    "backup/request-base-archive-corrupt",
                    "request-base archive is incomplete or has no authenticated semantic coordinate",
                )
            })?;
        let expected_node_count = unsigned(row.get(8), "request-base archive node count")?;
        let node_set_hash = digest(row.get(9), "request-base archive node-set hash")?;
        (
            row,
            TreeCaptureSource::RequestBaseArchive {
                expected_node_count,
                node_set_hash,
            },
        )
    };
    let basis = unsigned(row.get(1), "request base basis")?;
    let (tx_hash, state_hash, eidx_frontier) = if basis == 0 {
        let catalog = client
            .query_one(
                "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/request-base-genesis", error)
            })?;
        let genesis: Vec<u8> = catalog.get(0);
        let genesis_hash = digest(catalog.get(1), "request base genesis hash")?;
        if sha256(&genesis) != genesis_hash {
            return Err(fault(
                "backup/request-base-genesis",
                "request base genesis payload does not match its hash",
            ));
        }
        let database = Database::from_genesis(decode_genesis(&genesis)?)?;
        (
            genesis_hash,
            checkpoint_state_hash(&database)?,
            database.eidx_frontier(),
        )
    } else {
        let basis_sql = sql_u64(basis, "request base basis")?;
        let coordinate = client
            .query_opt(
                "SELECT tx_hash, state_hash, eidx_frontier \
                   FROM atomic_generation_transactions \
                  WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                &[&database_id, &generation_sql, &basis_sql],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/request-base-coordinate", error)
            })?
            .ok_or_else(|| {
                fault(
                    "backup/request-base-coordinate",
                    "request base has no immutable generation transaction coordinate",
                )
            })?;
        (
            digest(coordinate.get(0), "request base transaction hash")?,
            digest(coordinate.get(1), "request base state hash")?,
            unsigned(coordinate.get(2), "request base entity frontier")?,
        )
    };
    let source_hashes = [tx_hash];
    // Positive-generation ATLG membership hashes are already portable: they
    // bind the immutable lineage, never the mutable database alias.
    let portable_hashes = source_hashes;
    let frontiers = [eidx_frontier];
    let states = [state_hash];
    let log = TreeCaptureLog {
        start_basis: basis,
        backup_basis: basis,
        source_transaction_hashes: &source_hashes,
        portable_transaction_hashes: &portable_hashes,
        portable_frontiers: &frontiers,
        state_hashes: &states,
    };
    if let Some(tree) = capture_tree_candidate(
        client,
        &row,
        source,
        TreeCaptureContext {
            database_id,
            lineage_id,
            log_generation,
            log: &log,
        },
        publisher,
    )? {
        return Ok(tree.manifest_hash);
    }
    reconstruct_bound_request_tree(
        client,
        BoundRequestCoordinate {
            source_database_id: database_id,
            portable_lineage_id: lineage_id,
            log_generation,
            basis,
            tx_hash,
            state_hash,
            eidx_frontier,
        },
        publisher,
    )
}

struct BoundRequestCoordinate<'a> {
    source_database_id: &'a str,
    portable_lineage_id: &'a str,
    log_generation: u64,
    basis: u64,
    tx_hash: Digest,
    state_hash: Digest,
    eidx_frontier: u64,
}

fn reconstruct_bound_request_tree<C: crate::sql_io::GenericClient>(
    client: &mut C,
    coordinate: BoundRequestCoordinate<'_>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Digest, SemanticError> {
    let recovered = recover_generation_to(
        client,
        coordinate.source_database_id,
        coordinate.log_generation,
        coordinate.basis,
        coordinate.tx_hash,
    )?;
    if recovered.final_hash != coordinate.tx_hash
        || recovered.database.basis_t() != coordinate.basis
        || recovered.database.eidx_frontier() != coordinate.eidx_frontier
        || checkpoint_state_hash(&recovered.database)? != coordinate.state_hash
    {
        return Err(fault(
            "backup/request-base-reconstruction",
            "authoritative generation log does not reproduce the bound request db-before",
        ));
    }

    let build = build_full_native_tree(
        coordinate.portable_lineage_id,
        1,
        coordinate.log_generation,
        coordinate.tx_hash,
        coordinate.state_hash,
        &recovered.database,
    )?;
    if build.manifest.basis_t != coordinate.basis
        || build.manifest.eidx_frontier != coordinate.eidx_frontier
    {
        return Err(fault(
            "backup/request-base-reconstruction",
            "rebuilt request db-before has the wrong immutable coordinate",
        ));
    }
    for (hash, payload) in build.nodes.iter() {
        publisher.publish(*hash, payload)?;
    }
    let payload = build.manifest.encode()?;
    let manifest_hash = sha256(&payload);
    publisher.publish(manifest_hash, &payload)?;
    Ok(manifest_hash)
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

fn load_backup_log(
    directory: &Path,
    manifest: &Manifest,
) -> Result<LoadedBackupLog, SemanticError> {
    if manifest.version == VERSION {
        // Do not trust a constant-size root's basis as an allocation size.
        // A valid chain grows this vector one authenticated record at a time;
        // a damaged huge basis therefore fails on its first bad link instead
        // of attempting an attacker-sized eager allocation.
        let mut entries = Vec::new();
        let mut current_transaction_hash = manifest.head_transaction_hash;
        let mut request_hash = manifest.request_head_hash;
        let mut request_keys = BTreeSet::new();
        for basis in (1..=manifest.basis).rev() {
            let membership_payload = read_object(directory, current_transaction_hash)?;
            let membership =
                decode_backup_membership(&membership_payload, manifest.log_generation)?;
            if membership.lineage_id != manifest.lineage_id
                || membership.basis != basis
                || sha256(&membership_payload) != current_transaction_hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    "portable generation membership chain is invalid",
                ));
            }
            let content_payload = read_object(directory, membership.content_hash)?;
            let content = LineageTransactionContent::decode(&content_payload)?;
            if sha256(&content_payload) != membership.content_hash
                || content.lineage_id != manifest.lineage_id
                || content.basis_t != basis
                || content.eidx_frontier != membership.eidx_frontier
            {
                return Err(fault(
                    "backup/content-binding",
                    "portable transaction content disagrees with its generation membership",
                ));
            }
            let transaction = content.to_transaction(membership.previous_hash);
            let request_payload = read_object(directory, request_hash)?;
            let request = decode_request_record(&request_payload)?;
            if request.lineage_id != manifest.lineage_id
                || request.log_generation != manifest.log_generation
                || request.basis != basis
                || request.transaction_hash != current_transaction_hash
                || !request_keys.insert(request.request_key_hash)
            {
                return Err(fault(
                    "backup/request-chain",
                    "portable request chain is invalid",
                ));
            }
            let previous_transaction_hash = membership.previous_hash;
            let previous_request_hash = request.previous_hash;
            entries.push(LoadedBackupEntry {
                transaction_hash: current_transaction_hash,
                content_hash: Some(membership.content_hash),
                reserved_frontier: content.reserved_frontier,
                transaction,
                request,
                legacy_state_hash: Some(membership.state_hash),
            });
            current_transaction_hash = previous_transaction_hash;
            request_hash = previous_request_hash;
        }
        if current_transaction_hash != manifest.genesis_hash
            || request_hash != manifest.genesis_hash
        {
            return Err(fault(
                "backup/log-root",
                "portable log chains do not terminate at genesis",
            ));
        }
        entries.reverse();
        let receipt_genesis_reads = if entries
            .iter()
            .any(|entry| !entry.request.receipt_tempids.is_empty())
        {
            let genesis = read_object(directory, manifest.genesis_hash)?;
            let mut prior_frontier =
                Database::from_genesis(decode_genesis(&genesis)?)?.eidx_frontier();
            for entry in &entries {
                validate_backup_receipt(
                    &entry.transaction,
                    &entry.request,
                    prior_frontier,
                    entry.reserved_frontier,
                )?;
                prior_frontier = entry.transaction.eidx_frontier;
            }
            1
        } else {
            0
        };
        Ok(LoadedBackupLog {
            objects_read: entries
                .len()
                .saturating_mul(3)
                .saturating_add(receipt_genesis_reads),
            entries,
        })
    } else {
        let mut entries = Vec::with_capacity(manifest.transactions.len());
        let mut previous = manifest.genesis_hash;
        for (offset, hash) in manifest.transactions.iter().enumerate() {
            let payload = read_object(directory, *hash)?;
            let transaction = decode_transaction(&payload)?;
            let request = &manifest.requests[offset];
            if transaction.database_id != manifest.lineage_id
                || transaction.basis_t != offset as u64 + 1
                || transaction.previous_hash != previous
                || transaction_hash(&payload) != *hash
                || request.tx_hash != *hash
            {
                return Err(fault(
                    "backup/invalid-chain",
                    "legacy portable transaction chain is invalid",
                ));
            }
            previous = *hash;
            let receipt_tempids = transaction.tempids.clone();
            entries.push(LoadedBackupEntry {
                transaction_hash: *hash,
                content_hash: None,
                reserved_frontier: None,
                transaction,
                request: BackupRequestRecord {
                    lineage_id: manifest.lineage_id.clone(),
                    log_generation: 0,
                    basis: request.basis,
                    transaction_hash: request.tx_hash,
                    previous_hash: manifest.genesis_hash,
                    request_key_hash: request_key_hash(&manifest.lineage_id, &request.key)?,
                    digest: request.digest,
                    request_kind: 1,
                    base_manifest_hash: None,
                    receipt_tempids,
                },
                legacy_state_hash: Some(manifest.state_hashes[offset]),
            });
        }
        Ok(LoadedBackupLog {
            objects_read: entries.len(),
            entries,
        })
    }
}

/// Receipts also contain existing/upsert and transaction identities. Only a
/// genuinely new non-transaction ID requires a canonical issuance witness;
/// requiring every receipt ID to be newly allocated rejects valid old bytes.
fn validate_backup_receipt(
    transaction: &DurableTransaction,
    request: &BackupRequestRecord,
    prior_frontier: u64,
    reserved_frontier: Option<u64>,
) -> Result<(), SemanticError> {
    crate::identity::validate_frontier(prior_frontier)?;
    if request.receipt_tempids.is_empty() {
        return Ok(());
    }
    let allocations: BTreeSet<_> = transaction.tempids.values().copied().collect();
    for entity in request.receipt_tempids.values().copied() {
        let partition = crate::eid_to_part(entity)?;
        let index = crate::eid_to_eidx(entity)?;
        if partition == crate::DB_PARTITION
            && reserved_frontier.is_some_and(|frontier| index >= frontier)
        {
            return Err(fault(
                "backup/request-chain",
                "receipt names a reserved identity beyond its authenticated checkpoint",
            ));
        }
        let valid = if partition == crate::TX_PARTITION {
            entity == crate::t_to_tx(transaction.basis_t)?
        } else {
            index < transaction.eidx_frontier
                && (index < prior_frontier || allocations.contains(&entity))
        };
        if !valid {
            return Err(fault(
                "backup/request-chain",
                "receipt names neither a previously issued ID nor a witnessed new allocation",
            ));
        }
    }
    Ok(())
}

fn load_program_graph(
    directory: &Path,
    mut pending: BTreeSet<Digest>,
) -> Result<BTreeMap<Digest, (Program, Vec<u8>)>, SemanticError> {
    let mut programs = BTreeMap::new();
    while let Some(hash) = pending.pop_first() {
        if programs.contains_key(&hash) {
            continue;
        }
        let payload = read_object(directory, hash)?;
        let program = decode_program(&payload)?;
        collect_program_dependencies(&program, &mut pending);
        programs.insert(hash, (program, payload));
    }
    Ok(programs)
}

fn verify_program_presence(
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
) -> Result<(), SemanticError> {
    let genesis = read_object(directory, manifest.genesis_hash)?;
    let genesis = decode_genesis(&genesis)?;
    let mut required = BTreeSet::new();
    for datom in &genesis {
        collect_function_hashes(&datom.value, &mut required);
    }
    for entry in &log.entries {
        for datom in &entry.transaction.tx_data {
            collect_function_hashes(&datom.value, &mut required);
        }
    }
    let programs = load_program_graph(directory, required)?;
    verify_legacy_program_declarations(manifest, &programs)
}

fn verify_legacy_program_declarations(
    manifest: &Manifest,
    programs: &BTreeMap<Digest, (Program, Vec<u8>)>,
) -> Result<(), SemanticError> {
    if manifest.version != LEGACY_VERSION {
        return Ok(());
    }
    let declared: BTreeMap<_, _> = manifest
        .programs
        .iter()
        .map(|row| (row.hash, row))
        .collect();
    if declared.len() != programs.len() || !declared.keys().eq(programs.keys()) {
        return Err(fault(
            "backup/program-set",
            "legacy manifest program set is not its exact temporal closure",
        ));
    }
    for (hash, (program, _)) in programs {
        let row = declared[hash];
        if row.kind != program_kind(program) || row.arity != i16::from(program.arity) {
            return Err(fault(
                "backup/program-metadata",
                "legacy program metadata disagrees with its canonical payload",
            ));
        }
    }
    Ok(())
}

fn decode_bound_tree_manifest(
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    tree: &TreeBackup,
    expected_state_hash: Option<Digest>,
) -> Result<PersistentTreeManifest, SemanticError> {
    let payload = read_object(directory, tree.manifest_hash)?;
    let manifest = PersistentTreeManifest::decode(&payload)?;
    let (transaction_hash, frontier, derived_state) = if manifest.basis_t == 0 {
        let genesis = read_object(directory, backup.genesis_hash)?;
        let database = Database::from_genesis(decode_genesis(&genesis)?)?;
        (
            backup.genesis_hash,
            database.eidx_frontier(),
            Some(checkpoint_state_hash(&database)?),
        )
    } else {
        let offset = usize::try_from(manifest.basis_t - 1).map_err(|_| {
            fault(
                "backup/tree-basis",
                "backed-up tree basis is not representable",
            )
        })?;
        let entry = log.entries.get(offset).ok_or_else(|| {
            fault(
                "backup/tree-basis",
                "backed-up tree basis is outside the transaction chain",
            )
        })?;
        (
            entry.transaction_hash,
            entry.transaction.eidx_frontier,
            entry.legacy_state_hash,
        )
    };
    let required_state = expected_state_hash.or(derived_state);
    if manifest.database_id != backup.lineage_id
        || manifest.publication_revision != 1
        || manifest.basis_t > backup.basis
        || transaction_hash != manifest.tx_hash
        || required_state.is_some_and(|state| state != manifest.state_hash)
        || manifest.excision_generation != backup.log_generation
        || manifest.eidx_frontier != frontier
        || sha256(&payload) != tree.manifest_hash
    {
        return Err(fault(
            "backup/tree-binding",
            "backed-up tree does not identify this backup lineage and point",
        ));
    }
    Ok(manifest)
}

fn request_base_trees(log: &LoadedBackupLog) -> Vec<TreeBackup> {
    log.entries
        .iter()
        .filter_map(|entry| entry.request.base_manifest_hash)
        .collect::<BTreeSet<_>>()
        .into_iter()
        .map(|manifest_hash| TreeBackup {
            manifest_hash,
            legacy_node_hashes: None,
        })
        .collect()
}

fn verify_tree_presence(
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    tree: &TreeBackup,
) -> Result<(), SemanticError> {
    let manifest = decode_bound_tree_manifest(directory, backup, log, tree, None)?;
    let mut reachable = BTreeSet::new();
    for tree_root in &manifest.trees {
        let root_hash = tree_root.descriptor.root_hash;
        let root_payload = read_object(directory, root_hash)?;
        if root_payload.len() as u64 != tree_root.root_bytes {
            return Err(fault(
                "backup/tree-root-bytes",
                "backed-up tree root size disagrees with its manifest",
            ));
        }
        let persistent_tree::TreeNode::Root(root) =
            persistent_tree::decode_tree_node(&root_hash, &root_payload)?
        else {
            return Err(fault(
                "backup/tree-root-kind",
                "tree descriptor does not name a root node",
            ));
        };
        reachable.insert(root_hash);
        if root.order != tree_root.descriptor.order
            || root.history != tree_root.descriptor.history
            || root.count != tree_root.descriptor.count
        {
            return Err(fault(
                "backup/tree-root-binding",
                "tree root routing metadata disagrees with its descriptor",
            ));
        }
        for directory_ref in root.directories {
            let directory_payload = read_object(directory, directory_ref.hash)?;
            let persistent_tree::TreeNode::Directory(node) =
                persistent_tree::decode_tree_node(&directory_ref.hash, &directory_payload)?
            else {
                return Err(fault(
                    "backup/tree-directory-kind",
                    "tree root does not name a directory node",
                ));
            };
            reachable.insert(directory_ref.hash);
            if node.order != root.order
                || node.history != root.history
                || node.count != directory_ref.count
            {
                return Err(fault(
                    "backup/tree-directory-binding",
                    "tree directory routing metadata disagrees with its parent",
                ));
            }
            for leaf in node.leaves {
                ensure_object_present(directory, leaf.hash)?;
                reachable.insert(leaf.hash);
            }
        }
    }
    if let Some(expected) = &tree.legacy_node_hashes
        && expected.as_slice() != reachable.into_iter().collect::<Vec<_>>()
    {
        return Err(fault(
            "backup/tree-node-set",
            "legacy tree node list is not its exact shallow reachable closure",
        ));
    }
    Ok(())
}

fn verify_tree_backup(
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    tree: &TreeBackup,
    database: &Database,
    expected_state_hash: Digest,
) -> Result<usize, SemanticError> {
    let manifest =
        decode_bound_tree_manifest(directory, backup, log, tree, Some(expected_state_hash))?;
    let mut legacy_reachable = tree.legacy_node_hashes.as_ref().map(|_| BTreeSet::new());
    let mut objects_read: usize = 1;
    let mut load = |root: &crate::ManifestTree| {
        let (datoms, validated) = read_validated_backup_tree(directory, root)?;
        objects_read = objects_read.saturating_add(validated.nodes_read as usize);
        if let Some(reachable) = &mut legacy_reachable {
            reachable.extend(validated.node_hashes);
        }
        Ok::<_, SemanticError>(datoms)
    };
    verify_tree_projection_semantics(&manifest, database, &mut load)?;
    if let (Some(expected), Some(reachable)) = (&tree.legacy_node_hashes, legacy_reachable)
        && expected.as_slice() != reachable.into_iter().collect::<Vec<_>>()
    {
        return Err(fault(
            "backup/tree-node-set",
            "legacy tree node list is not its exact reachable closure",
        ));
    }
    Ok(objects_read)
}

/// A restored legacy checkpoint has no portable expected root hash. Its
/// structural closure and coordinate labels alone cannot prove agreement
/// with the canonical log. Use the same semantic comparison as deep backup
/// verification, against the caller's exact forward-replayed database value.
/// The caller holds the generation/root lifetime fence during these reads.
fn verify_restored_tree_semantics<C: crate::sql_io::GenericClient>(
    client: &mut C,
    manifest: &PersistentTreeManifest,
    database: &Database,
) -> Result<(), SemanticError> {
    verify_tree_projection_semantics(manifest, database, &mut |root| {
        read_validated_tree_with_loader(root, &mut |hash| {
            client
                .query_opt(
                    "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-semantic-tree-node", error)
                })?
                .map(|row| row.get(0))
                .ok_or_else(|| {
                    fault(
                        "backup/restore-semantic-tree-node",
                        "restored checkpoint references a missing semantic tree node",
                    )
                })
        })
        .map(|(datoms, _)| datoms)
    })
}

fn verify_tree_projection_semantics(
    manifest: &PersistentTreeManifest,
    database: &Database,
    load: &mut dyn FnMut(&crate::ManifestTree) -> Result<Vec<crate::Datom>, SemanticError>,
) -> Result<(), SemanticError> {
    use crate::operations::{
        derive_index_projection, same_stored_datoms, validate_physical_history_projection,
    };
    use crate::{IndexOrder, View};
    if database.basis_t() != manifest.basis_t {
        return Err(fault(
            "backup/tree-basis",
            "tree verification requires its exact replayed database value",
        ));
    }
    crate::peer::validate_avet_work_directions(&manifest.pending_avet, database.schema())?;
    let current = database.datoms(View::Current, IndexOrder::Eavt);
    let replayed_history = database.datoms(View::History, IndexOrder::Eavt);
    let physical_history = load(
        manifest
            .tree(IndexOrder::Eavt, true)
            .expect("manifest validates eight roots"),
    )?;
    validate_physical_history_projection(&replayed_history, &physical_history, database.basis_t())
        .map_err(|error| tree_semantic_error(manifest, IndexOrder::Eavt, true, error.message))?;
    for root in &manifest.trees {
        let order = root.descriptor.order;
        let history = root.descriptor.history;
        if history && order == IndexOrder::Eavt {
            continue;
        }
        let mut observed = load(root)?;
        let source = if history { &physical_history } else { &current };
        let mut expected = derive_index_projection(database, source, order)?;
        if order == IndexOrder::Avet {
            validate_pending_avet_projection(
                &manifest.pending_avet,
                &observed,
                source,
                &replayed_history,
                history,
            )
            .map_err(|error| tree_semantic_error(manifest, order, history, error.message))?;
            let pending = |attribute| {
                manifest
                    .pending_avet
                    .iter()
                    .any(|work| work.attribute == attribute && (!work.history || history))
            };
            observed.retain(|datom| !pending(datom.attribute));
            expected.retain(|datom| !pending(datom.attribute));
        }
        if !same_stored_datoms(&observed, &expected) {
            return Err(tree_semantic_error(
                manifest,
                order,
                history,
                format!(
                    "physical index disagrees with authoritative replay ({} observed vs {} expected datoms)",
                    observed.len(),
                    expected.len()
                ),
            ));
        }
    }
    Ok(())
}

/// Authenticate the entire physical graph without collecting its datoms or
/// claiming agreement with semantic replay. Unlike presence checking this
/// reads every leaf and verifies descriptor counts, routing and ordering.
fn verify_tree_structure(
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    tree: &TreeBackup,
) -> Result<(), SemanticError> {
    let manifest = decode_bound_tree_manifest(directory, backup, log, tree, None)?;
    let mut legacy_reachable = tree.legacy_node_hashes.as_ref().map(|_| BTreeSet::new());
    for root in &manifest.trees {
        let validated = persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
            read_backup_tree_node(directory, root, *hash)
        })?;
        if let Some(reachable) = &mut legacy_reachable {
            reachable.extend(validated.node_hashes);
        }
    }
    if let (Some(expected), Some(reachable)) = (&tree.legacy_node_hashes, legacy_reachable)
        && expected.as_slice() != reachable.into_iter().collect::<Vec<_>>()
    {
        return Err(fault(
            "backup/tree-node-set",
            "legacy tree node list is not its exact reachable closure",
        ));
    }
    Ok(())
}

fn read_backup_tree_node(
    directory: &Path,
    root: &crate::ManifestTree,
    hash: Digest,
) -> Result<Vec<u8>, SemanticError> {
    let payload = read_object(directory, hash)?;
    if hash == root.descriptor.root_hash && payload.len() as u64 != root.root_bytes {
        return Err(fault(
            "backup/tree-root-bytes",
            "backed-up tree root size disagrees with its manifest",
        ));
    }
    Ok(payload)
}

/// Payload memory remains one node at a time; datoms for one index are
/// materialized for exact comparison. The structural walk authenticates
/// ordering/routing while the loader collects only reachable leaf contents.
fn read_validated_backup_tree(
    directory: &Path,
    root: &crate::ManifestTree,
) -> Result<(Vec<crate::Datom>, persistent_tree::StreamingTreeValidation), SemanticError> {
    read_validated_tree_with_loader(root, &mut |hash| read_object(directory, hash))
}

fn read_validated_tree_with_loader(
    root: &crate::ManifestTree,
    load_node: &mut dyn FnMut(Digest) -> Result<Vec<u8>, SemanticError>,
) -> Result<(Vec<crate::Datom>, persistent_tree::StreamingTreeValidation), SemanticError> {
    let mut datoms = Vec::new();
    let validated = persistent_tree::validate_tree_streaming(&root.descriptor, |hash| {
        let payload = load_node(*hash)?;
        if *hash == root.descriptor.root_hash && payload.len() as u64 != root.root_bytes {
            return Err(fault(
                "backup/tree-root-bytes",
                "tree root size disagrees with its manifest",
            ));
        }
        if let persistent_tree::TreeNode::Leaf(leaf) =
            persistent_tree::decode_tree_node(hash, &payload)?
        {
            for index in 0..leaf.len() {
                datoms.push(leaf.datom(index).expect("validated leaf columns"));
            }
        }
        Ok(payload)
    })?;
    datoms.sort_by(|left, right| left.cmp_in(right, root.descriptor.order));
    Ok((datoms, validated))
}

/// A copy phase is an exact AEVT-derived AVET prefix. Clearing phases can
/// retain stale ranges across disable/data/re-enable tails; those are hidden
/// from peer queries, but even stale data must occur in the authoritative log.
/// A pending marker is never permission to smuggle fabricated values through
/// backup verification. Once the current phase finishes it is checked exactly
/// by the ordinary comparison, even while history is still pending.
pub(crate) fn validate_pending_avet_projection(
    pending_avet: &[crate::AvetProjectionWork],
    observed: &[crate::Datom],
    source: &[crate::Datom],
    replayed_history: &[crate::Datom],
    history: bool,
) -> Result<(), SemanticError> {
    for work in pending_avet.iter().filter(|work| !work.history || history) {
        let actual = observed
            .iter()
            .filter(|datom| datom.attribute == work.attribute)
            .cloned()
            .collect::<Vec<_>>();
        if work.history == history && !work.clearing {
            let mut expected = source
                .iter()
                .filter(|datom| datom.attribute == work.attribute)
                .cloned()
                .collect::<Vec<_>>();
            expected.sort_by(|left, right| left.cmp_in(right, crate::IndexOrder::Avet));
            let prefix = usize::try_from(work.offset)
                .ok()
                .and_then(|offset| expected.get(..offset));
            if !prefix.is_some_and(|prefix| crate::operations::same_stored_datoms(&actual, prefix))
            {
                return Err(fault(
                    "integrity/tree-pending-avet-projection-mismatch",
                    format!(
                        "pending attribute {} is not its exact copy prefix at offset {}",
                        work.attribute, work.offset
                    ),
                ));
            }
        } else {
            for datom in actual {
                let present = replayed_history
                    .binary_search_by(|candidate| candidate.cmp_in(&datom, crate::IndexOrder::Eavt))
                    .ok()
                    .is_some_and(|index| {
                        crate::operations::same_stored_datoms(
                            &replayed_history[index..index + 1],
                            std::slice::from_ref(&datom),
                        )
                    });
                if !present || (!history && !datom.added) {
                    return Err(fault(
                        "integrity/tree-pending-avet-projection-mismatch",
                        format!(
                            "pending attribute {} contains data absent from authoritative replay",
                            work.attribute
                        ),
                    ));
                }
            }
        }
    }
    Ok(())
}

fn tree_semantic_error(
    manifest: &PersistentTreeManifest,
    order: crate::IndexOrder,
    history: bool,
    message: impl Into<String>,
) -> SemanticError {
    fault("backup/tree-semantic-mismatch", message)
        .detail("basis_t", manifest.basis_t.to_string())
        .detail("log_generation", manifest.excision_generation.to_string())
        .detail("order", format!("{order:?}"))
        .detail("history", history.to_string())
}

/// Prove that every restored kind-2 receipt names the deterministic archive
/// rebuilt from its portable base, and that no extra archive/binding entered
/// the active generation. This is part of ambiguous-acknowledgement replay:
/// merely matching the log head is insufficient if its db-before values can
/// no longer be reopened exactly.
#[allow(clippy::too_many_arguments)]
fn match_restored_request_base_archives<C: crate::sql_io::GenericClient>(
    client: &mut C,
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    target_database_id: &str,
    target_generation: u64,
    authenticated_archive_count: Option<u64>,
) -> Result<BTreeMap<Digest, Digest>, SemanticError> {
    let generation_sql = sql_u64(target_generation, "restored request-base generation")?;
    let mut portable_hashes = BTreeSet::new();
    let mut expected_bindings = 0_u64;
    for entry in &log.entries {
        match (entry.request.request_kind, entry.request.base_manifest_hash) {
            (2, Some(hash)) => {
                portable_hashes.insert(hash);
                expected_bindings = expected_bindings.saturating_add(1);
            }
            (0 | 1, None) => {}
            _ => {
                return Err(fault(
                    "backup/restore-check-request-base",
                    "portable request kind and db-before binding disagree",
                ));
            }
        }
    }
    let stored_counts = client
        .query_one(
            "SELECT (SELECT count(*) FROM atomic_request_base_archives archive \
                      WHERE archive.database_id = $1 AND archive.generation = $2), \
                    (SELECT count(*) FROM atomic_generation_request_bases base \
                      WHERE base.database_id = $1 AND base.generation = $2)",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-request-base-counts", error)
        })?;
    if unsigned(stored_counts.get(0), "restored request-base archive count")?
        != authenticated_archive_count.unwrap_or(portable_hashes.len() as u64)
        || unsigned(stored_counts.get(1), "restored request-base binding count")?
            != expected_bindings
    {
        return Err(fault(
            "backup/restore-check-request-base-counts",
            "restored generation has missing or extra request-base authority",
        ));
    }

    let mut archives = portable_hashes
        .into_iter()
        .map(|portable_hash| {
            let tree = TreeBackup {
                manifest_hash: portable_hash,
                legacy_node_hashes: None,
            };
            let source = decode_bound_tree_manifest(directory, backup, log, &tree, None)?;
            Ok((source.basis_t, portable_hash, source))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    archives.sort_by_key(|(basis, portable_hash, _)| (*basis, *portable_hash));
    let mut matched = BTreeMap::new();
    for (offset, (_, portable_hash, source)) in archives.into_iter().enumerate() {
        let archive_revision = request_base_archive_revision(offset)?;
        let target_tx_hash = if source.basis_t == 0 {
            backup.genesis_hash
        } else {
            let row = client
                .query_opt(
                    "SELECT tx_hash, state_hash, eidx_frontier \
                       FROM atomic_generation_transactions \
                      WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                    &[
                        &target_database_id,
                        &generation_sql,
                        &sql_u64(source.basis_t, "restored request-base basis")?,
                    ],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error(
                        "backup/restore-check-request-base-coordinate",
                        error,
                    )
                })?
                .ok_or_else(|| {
                    fault(
                        "backup/restore-check-request-base-coordinate",
                        "restored request base has no generation transaction coordinate",
                    )
                })?;
            let tx_hash = digest(row.get(0), "restored request-base transaction hash")?;
            if digest(row.get(1), "restored request-base state hash")? != source.state_hash
                || unsigned(row.get(2), "restored request-base frontier")? != source.eidx_frontier
            {
                return Err(fault(
                    "backup/restore-check-request-base-coordinate",
                    "restored request-base coordinate disagrees with its portable source",
                ));
            }
            tx_hash
        };
        let target = PersistentTreeManifest {
            database_id: target_database_id.to_owned(),
            publication_revision: archive_revision,
            index_basis_t: source.index_basis_t,
            basis_t: source.basis_t,
            tx_hash: target_tx_hash,
            state_hash: source.state_hash,
            excision_generation: target_generation,
            eidx_frontier: source.eidx_frontier,
            trees: source.trees,
            pending_avet: source.pending_avet,
        };
        let expected_payload = target.encode()?;
        let expected_hash = sha256(&expected_payload);
        let row = client
            .query_opt(
                "SELECT archive.database_id, archive.generation, archive.archive_revision, \
                        archive.basis_t, archive.tx_hash, archive.state_hash, \
                        archive.eidx_frontier, archive.manifest_version, archive.payload, \
                        archive.expected_node_count, archive.node_set_hash \
                   FROM atomic_request_base_archives archive \
                   JOIN atomic_request_base_archive_completions complete \
                     ON complete.manifest_hash = archive.manifest_hash \
                   JOIN atomic_semantic_commitment_roots semantic \
                     ON semantic.database_id = archive.database_id \
                    AND semantic.generation = archive.generation \
                    AND semantic.basis_t = archive.basis_t \
                    AND semantic.tx_hash = archive.tx_hash \
                    AND semantic.state_hash = archive.state_hash \
                    AND semantic.eidx_frontier = archive.eidx_frontier \
                    AND semantic.commitment_version = 2 \
                  WHERE archive.manifest_hash = $1",
                &[&&expected_hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-check-request-base-archive", error)
            })?
            .ok_or_else(|| {
                fault(
                    "backup/restore-check-request-base-archive",
                    "expected completed request-base archive is absent",
                )
            })?;
        let expected_node_count = unsigned(row.get(9), "restored archive node count")?;
        let expected_node_set_hash = digest(row.get(10), "restored archive node-set hash")?;
        if row.get::<_, String>(0) != target_database_id
            || unsigned(row.get(1), "restored archive generation")? != target_generation
            || unsigned(row.get(2), "restored archive revision")? != archive_revision
            || unsigned(row.get(3), "restored archive basis")? != target.basis_t
            || digest(row.get(4), "restored archive transaction")? != target.tx_hash
            || digest(row.get(5), "restored archive state")? != target.state_hash
            || unsigned(row.get(6), "restored archive frontier")? != target.eidx_frontier
            || row.get::<_, i16>(7) != PersistentTreeManifest::encoded_version(&expected_payload)?
            || row.get::<_, Vec<u8>>(8) != expected_payload
            || client
                .query_one(
                    "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                                     WHERE manifest_hash = $1)",
                    &[&&expected_hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error(
                        "backup/restore-check-request-base-variant",
                        error,
                    )
                })?
                .get::<_, bool>(0)
        {
            return Err(fault(
                "backup/restore-check-request-base-archive",
                "restored request-base archive is noncanonical or ambiguous",
            ));
        }

        let root_rows = client
            .query(
                "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
                   FROM atomic_request_base_archive_roots WHERE manifest_hash = $1 \
                  ORDER BY history, index_order",
                &[&&expected_hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-check-request-base-roots", error)
            })?;
        if root_rows.len() != target.trees.len() {
            return Err(fault(
                "backup/restore-check-request-base-roots",
                "restored request-base archive has an incomplete root projection",
            ));
        }
        for (root_row, tree) in root_rows.into_iter().zip(&target.trees) {
            if decode_tree_order(root_row.get(0))? != tree.descriptor.order
                || root_row.get::<_, bool>(1) != tree.descriptor.history
                || digest(root_row.get(2), "restored archive root hash")?
                    != tree.descriptor.root_hash
                || unsigned(root_row.get(3), "restored archive root count")?
                    != tree.descriptor.count
                || unsigned(root_row.get(4), "restored archive root bytes")? != tree.root_bytes
            {
                return Err(fault(
                    "backup/restore-check-request-base-roots",
                    "restored request-base root projection disagrees with its manifest",
                ));
            }
        }

        let mut reachable = BTreeSet::new();
        for tree in &target.trees {
            let root_hash = tree.descriptor.root_hash;
            let expected_root_bytes = tree.root_bytes;
            let validated =
                persistent_tree::validate_tree_streaming(&tree.descriptor, |node_hash| {
                    let payload: Vec<u8> = client
                        .query_opt(
                            "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                            &[&&node_hash[..]],
                        )
                        .map_err(|error| {
                            crate::postgres::postgres_error(
                                "backup/restore-check-request-base-node",
                                error,
                            )
                        })?
                        .ok_or_else(|| {
                            fault(
                                "backup/restore-check-request-base-node",
                                "restored request-base archive references a missing node",
                            )
                        })?
                        .get(0);
                    if payload != read_object(directory, *node_hash)?
                        || (*node_hash == root_hash && payload.len() as u64 != expected_root_bytes)
                    {
                        return Err(fault(
                            "backup/restore-check-request-base-node",
                            "restored request-base node differs from its portable source",
                        ));
                    }
                    Ok(payload)
                })?;
            reachable.extend(validated.node_hashes);
        }
        let planned = client
            .query(
                "SELECT node_hash FROM atomic_request_base_archive_nodes \
                  WHERE manifest_hash = $1 ORDER BY node_hash",
                &[&&expected_hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error(
                    "backup/restore-check-request-base-node-plan",
                    error,
                )
            })?
            .into_iter()
            .map(|row| digest(row.get(0), "restored request-base planned node"))
            .collect::<Result<BTreeSet<_>, SemanticError>>()?;
        if planned != reachable
            || expected_node_count != reachable.len() as u64
            || expected_node_set_hash != request_base_archive_node_set_hash(&reachable)
        {
            return Err(fault(
                "backup/restore-check-request-base-closure",
                "restored request-base closure disagrees with its completion commitment",
            ));
        }
        matched.insert(portable_hash, expected_hash);
    }
    Ok(matched)
}

fn target_matches_backup(
    client: &mut Client,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    expected_database: &Database,
) -> Result<Option<MatchedRestoreTarget>, SemanticError> {
    let mut transaction = client
        .transaction()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-begin", error))?;
    let Some(row) = transaction
        .query_opt(
            "SELECT log_generation FROM atomic_heads WHERE database_id=$1",
            &[&target_database_id],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-generation", error)
        })?
    else {
        return Ok(None);
    };
    let generation: i64 = row.get(0);
    let key: Option<i64> = transaction
        .query_one(
            "SELECT atomic_log_generation_pin_key($1,$2)",
            &[&target_database_id, &generation],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-pin-key", error))?
        .get(0);
    let Some(key) = key else {
        return Ok(None);
    };
    transaction
        .query_one("SELECT pg_advisory_xact_lock_shared($1)", &[&key])
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-pin", error))?;
    if !transaction
        .query_one(
            "SELECT EXISTS(SELECT 1 FROM atomic_heads WHERE database_id=$1 AND log_generation=$2)",
            &[&target_database_id, &generation],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-pinned-generation", error)
        })?
        .get::<_, bool>(0)
    {
        return Ok(None);
    }
    // Keep this transaction-scoped shared generation fence through every
    // historical archive read. An autocommit advisory-xact call would release
    // the fence before its first tree node was authenticated.
    let matched = target_matches_backup_pinned(
        &mut transaction,
        directory,
        manifest,
        log,
        completed_excisions,
        target_database_id,
        expected_database,
        generation,
    )?;
    transaction
        .commit()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-commit", error))?;
    Ok(matched)
}

#[allow(clippy::too_many_arguments)]
fn target_matches_backup_pinned<C: crate::sql_io::GenericClient>(
    client: &mut C,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    expected_database: &Database,
    pinned_generation: i64,
) -> Result<Option<MatchedRestoreTarget>, SemanticError> {
    let Some(catalog) = client
        .query_opt(
            "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-catalog", error))?
    else {
        return Ok(None);
    };
    let lineage_id: String = catalog.get(0);
    let genesis: Vec<u8> = catalog.get(1);
    let genesis_hash = digest(catalog.get(2), "restored genesis hash")?;
    if lineage_id != manifest.lineage_id
        || genesis_hash != manifest.genesis_hash
        || genesis != read_object(directory, manifest.genesis_hash)?
    {
        return Ok(None);
    }
    let Some(head) = client
        .query_opt(
            "SELECT basis_t, tx_hash, log_generation FROM atomic_heads WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-head", error))?
    else {
        return Ok(None);
    };
    let target_basis = unsigned(head.get(0), "restored head basis")?;
    let target_head_hash = digest(head.get(1), "restored head hash")?;
    let target_generation = unsigned(head.get(2), "restored log generation")?;
    if target_generation == 0
        || target_basis != manifest.basis
        || head.get::<_, i64>(2) != pinned_generation
    {
        return Ok(None);
    }
    let generation_sql = sql_u64(target_generation, "restored log generation")?;
    let rows = client
        .query(
            "SELECT t.basis_t, t.previous_hash, t.tx_hash, t.content_hash, t.state_hash, \
                    t.eidx_frontier, c.lineage_id, c.basis_t, c.eidx_frontier, \
                    c.envelope_version, c.payload, r.request_key_hash, r.request_digest, \
                    r.request_kind, r.tx_hash, base.base_manifest_hash \
               FROM atomic_generation_transactions t \
               JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
               JOIN atomic_generation_requests r \
                 ON r.database_id = t.database_id AND r.generation = t.generation \
                AND r.basis_t = t.basis_t \
               LEFT JOIN atomic_generation_request_bases base \
                 ON base.database_id = r.database_id AND base.generation = r.generation \
                AND base.request_key_hash = r.request_key_hash \
              WHERE t.database_id = $1 AND t.generation = $2 ORDER BY t.basis_t",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-log", error))?;
    if rows.len() != log.entries.len() {
        return Ok(None);
    }
    let mut request_base_bindings = Vec::with_capacity(rows.len());
    let mut receipts: BTreeMap<u64, (Digest, BTreeMap<String, u64>)> = BTreeMap::new();
    for row in client
        .query(
            "SELECT r.basis_t, r.request_key_hash, x.tempid_name, x.entity_id \
               FROM atomic_generation_requests r \
               JOIN atomic_generation_request_tempids x \
                 ON x.database_id = r.database_id AND x.generation = r.generation \
                AND x.request_key_hash = r.request_key_hash \
              WHERE r.database_id = $1 AND r.generation = $2 \
              ORDER BY r.basis_t, x.tempid_name",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-tempids", error))?
    {
        let basis = unsigned(row.get(0), "restored receipt basis")?;
        let key_hash = digest(row.get(1), "restored receipt key hash")?;
        let entry = receipts
            .entry(basis)
            .or_insert_with(|| (key_hash, BTreeMap::new()));
        if entry.0 != key_hash
            || entry
                .1
                .insert(
                    row.get::<_, String>(2),
                    unsigned(row.get(3), "restored receipt entity")?,
                )
                .is_some()
        {
            return Ok(None);
        }
    }
    let mut previous = manifest.genesis_hash;
    let mut prior_frontier = Database::from_genesis(decode_genesis(&genesis)?)?.eidx_frontier();
    for (row, entry) in rows.into_iter().zip(&log.entries) {
        let row_basis = unsigned(row.get(0), "restored transaction basis")?;
        let row_previous = digest(row.get(1), "restored predecessor hash")?;
        let row_hash = digest(row.get(2), "restored transaction hash")?;
        let row_content_hash = digest(row.get(3), "restored content hash")?;
        let row_state_hash = digest(row.get(4), "restored state hash")?;
        let row_frontier = unsigned(row.get(5), "restored transaction frontier")?;
        let content_lineage: String = row.get(6);
        let content_basis = unsigned(row.get(7), "restored content basis")?;
        let content_frontier = unsigned(row.get(8), "restored content frontier")?;
        let content_version: i16 = row.get(9);
        let content_payload: Vec<u8> = row.get(10);
        let request_key_hash = digest(row.get(11), "restored request key hash")?;
        let request_digest = digest(row.get(12), "restored request digest")?;
        let request_kind: i16 = row.get(13);
        let request_tx_hash = digest(row.get(14), "restored request transaction hash")?;
        let request_base_hash = row
            .get::<_, Option<Vec<u8>>>(15)
            .map(|hash| digest(hash, "restored request base hash"))
            .transpose()?;
        let actual_content = LineageTransactionContent::decode(&content_payload)?;
        let expected_content = if let Some(hash) = entry.content_hash {
            LineageTransactionContent::decode(&read_object(directory, hash)?)?
        } else {
            LineageTransactionContent::from_transaction(
                &manifest.lineage_id,
                prior_frontier,
                &entry.transaction,
            )?
        };
        let membership_hash = crate::log_generation::generation_transaction_hash(
            &manifest.lineage_id,
            target_generation,
            row_basis,
            row_previous,
            row_content_hash,
            row_state_hash,
            row_frontier,
        )?;
        let expected_request_digest = if entry.request.request_kind == 0 {
            tombstone_request_digest(
                &manifest.lineage_id,
                target_generation,
                row_basis,
                entry.request.request_key_hash,
            )?
        } else {
            entry.request.digest
        };
        let (_, receipt) = receipts
            .remove(&row_basis)
            .unwrap_or((request_key_hash, BTreeMap::new()));
        if row_basis != entry.transaction.basis_t
            || row_previous != previous
            || row_hash != membership_hash
            || row_state_hash != entry.legacy_state_hash.unwrap_or([0; 32])
            || sha256(&content_payload) != row_content_hash
            || content_lineage != manifest.lineage_id
            || content_basis != row_basis
            || content_frontier != row_frontier
            || content_version
                != i16::try_from(actual_content.version()).expect("ATLC version fits i16")
            || content_payload != expected_content.encode()?
            || request_key_hash != entry.request.request_key_hash
            || request_digest != expected_request_digest
            || request_kind != i16::from(entry.request.request_kind)
            || request_tx_hash != row_hash
            || receipt != entry.request.receipt_tempids
        {
            return Ok(None);
        }
        previous = row_hash;
        prior_frontier = row_frontier;
        request_base_bindings.push((request_base_hash, entry.request.base_manifest_hash));
    }
    if !receipts.is_empty()
        || target_head_hash != previous
        || target_basis != expected_database.basis_t()
        || prior_frontier != expected_database.eidx_frontier()
    {
        return Ok(None);
    }
    let legacy_checkpoints = legacy_receipt_checkpoints(log)?;
    let mut authenticated_archive_count = None;
    if !legacy_checkpoints.is_empty() {
        let mut complete_bases = BTreeSet::new();
        let mut legacy_trees = BTreeMap::<u64, Vec<PersistentTreeManifest>>::new();
        let mut newest_bases = BTreeSet::new();
        let mut archive_count = 0;
        for row in client.query(
            "SELECT archive.basis_t, archive.payload, archive.manifest_hash, archive.archive_revision, \
                    archive.tx_hash, archive.state_hash, archive.eidx_frontier, archive.manifest_version, \
                    archive.expected_node_count, archive.node_set_hash FROM atomic_request_base_archives archive \
             JOIN atomic_request_base_archive_completions complete USING (manifest_hash) \
             JOIN atomic_semantic_commitment_roots semantic \
               ON semantic.database_id=archive.database_id AND semantic.generation=archive.generation \
              AND semantic.basis_t=archive.basis_t AND semantic.tx_hash=archive.tx_hash \
              AND semantic.state_hash=archive.state_hash AND semantic.eidx_frontier=archive.eidx_frontier \
              AND semantic.commitment_version=2 \
             WHERE archive.database_id=$1 AND archive.generation=$2 ORDER BY archive.basis_t DESC, archive.archive_revision ASC",
            &[&target_database_id, &generation_sql],
        ).map_err(|error| crate::postgres::postgres_error("backup/restore-check-legacy-archives", error))? {
            let basis = unsigned(row.get(0), "legacy receipt archive basis")?;
            let payload: Vec<u8> = row.get(1);
            let archive = PersistentTreeManifest::decode(&payload)?;
            let hash = digest(row.get(2), "legacy receipt archive hash")?;
            if sha256(&payload) != hash || archive.database_id != target_database_id
                || archive.excision_generation != target_generation || archive.basis_t != basis
                || archive.publication_revision != unsigned(row.get(3), "legacy receipt archive revision")?
                || archive.tx_hash != digest(row.get(4), "legacy receipt archive transaction")?
                || archive.state_hash != digest(row.get(5), "legacy receipt archive state")?
                || archive.eidx_frontier != unsigned(row.get(6), "legacy receipt archive frontier")?
                || PersistentTreeManifest::encoded_version(&payload)? != row.get::<_, i16>(7) {
                return Err(fault("backup/restore-check-legacy-archive", "legacy receipt archive differs from its authenticated coordinate"));
            }
            validate_legacy_receipt_archive(client, &archive, hash, unsigned(row.get(8), "legacy receipt archive nodes")?, digest(row.get(9), "legacy receipt archive node set")?)?;
            archive_count += 1;
            if newest_bases.insert(basis) && legacy_checkpoints.contains(&basis) && archive.pending_avet.is_empty() {
                complete_bases.insert(basis);
            }
            legacy_trees.entry(basis).or_default().push(archive);
        }
        let roots: i64 = client
            .query_one(
                "SELECT count(*) FROM atomic_semantic_commitment_roots WHERE database_id=$1 \
             AND generation=$2 AND basis_t<=$3 AND commitment_version=2",
                &[
                    &target_database_id,
                    &generation_sql,
                    &sql_u64(target_basis, "legacy receipt endpoint")?,
                ],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-check-legacy-coordinates", error)
            })?
            .get(0);
        if complete_bases != legacy_checkpoints || roots as u64 != target_basis + 1 {
            // Older restores could publish a matching canonical generation
            // without historical receipt read bases. An active generation is
            // not an archive-upload owner: repair by staging another exactly
            // equivalent generation, never by bypassing that ownership guard.
            return Ok(None);
        }
        let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
        let mut replay = BackupAllocationReplay::new(&database)?;
        let mut previous = manifest.genesis_hash;
        let verify_coordinate =
            |client: &mut C, database: &Database, tx_hash| -> Result<(), SemanticError> {
                let coordinate = crate::persistent_commitment::load_persistent_coordinate(
                    client,
                    target_database_id,
                    target_generation,
                    database.basis_t(),
                )?
                .ok_or_else(|| {
                    fault(
                        "backup/restore-check-legacy-coordinate",
                        "legacy receipt semantic coordinate is missing",
                    )
                })?;
                if coordinate.tx_hash != tx_hash
                    || coordinate.state_hash != checkpoint_state_hash(database)?
                    || coordinate.eidx_frontier != database.eidx_frontier()
                {
                    return Err(fault(
                        "backup/restore-check-legacy-coordinate",
                        "legacy receipt semantic coordinate differs from the authoritative replay",
                    ));
                }
                Ok(())
            };
        verify_coordinate(client, &database, previous)?;
        if let Some(trees) = legacy_trees.remove(&0) {
            for tree in trees {
                verify_restored_tree_semantics(client, &tree, &database)?;
            }
        }
        for entry in &log.entries {
            let content = if let Some(hash) = entry.content_hash {
                LineageTransactionContent::decode(&read_object(directory, hash)?)?
            } else {
                LineageTransactionContent::from_transaction(
                    &manifest.lineage_id,
                    database.eidx_frontier(),
                    &entry.transaction,
                )?
            };
            let durable = content.to_transaction(previous);
            database = replay.apply(
                &database,
                &durable,
                content.reserved_frontier,
                entry.request.request_kind == 0,
            )?;
            previous = crate::log_generation::generation_transaction_hash(
                &manifest.lineage_id,
                target_generation,
                database.basis_t(),
                previous,
                sha256(&content.encode()?),
                checkpoint_state_hash(&database)?,
                database.eidx_frontier(),
            )?;
            verify_coordinate(client, &database, previous)?;
            if let Some(trees) = legacy_trees.remove(&database.basis_t()) {
                for tree in trees {
                    verify_restored_tree_semantics(client, &tree, &database)?;
                }
            }
        }
        if !legacy_trees.is_empty() {
            return Err(fault(
                "backup/restore-check-legacy-archive",
                "receipt archive is outside the authoritative restored prefix",
            ));
        }
        authenticated_archive_count = Some(archive_count);
    }
    // A different legitimate same-basis information generation can have a
    // different archive set. First establish exact canonical log/request
    // equality; only then is a missing/different archive a corrupt proof of
    // this requested point rather than an ordinary nonmatching restore target.
    let restored_request_bases = match_restored_request_base_archives(
        client,
        directory,
        manifest,
        log,
        target_database_id,
        target_generation,
        authenticated_archive_count,
    )?;
    if request_base_bindings.into_iter().any(|(actual, portable)| {
        actual != portable.and_then(|hash| restored_request_bases.get(&hash).copied())
    }) {
        return Ok(None);
    }
    let restored_completed = client
        .query(
            "SELECT c.request_t, c.request_entity \
               FROM atomic_heads h \
               JOIN atomic_log_generation_completions m \
                 ON m.database_id = h.database_id AND m.generation = h.log_generation \
               JOIN atomic_completed_excision_requests c \
                 ON c.database_id = h.database_id AND c.generation = h.log_generation \
              WHERE h.database_id = $1 AND h.log_generation = $2 \
              ORDER BY c.request_t, c.request_entity",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-completions", error)
        })?
        .into_iter()
        .map(|row| {
            Ok((
                unsigned(row.get(0), "restored completed request t")?,
                unsigned(row.get(1), "restored completed request entity")?,
            ))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    let has_completion_root: bool = client
        .query_one(
            "SELECT EXISTS ( \
                 SELECT 1 FROM atomic_heads h \
                   JOIN atomic_log_generation_completions m \
                     ON m.database_id = h.database_id AND m.generation = h.log_generation \
                  WHERE h.database_id = $1 AND h.log_generation = $2)",
            &[&target_database_id, &generation_sql],
        )
        .map_err(|error| {
            crate::postgres::postgres_error("backup/restore-check-completion-root", error)
        })?
        .get(0);
    if !has_completion_root {
        return Ok(None);
    }
    if restored_completed != completed_excisions {
        return Ok(None);
    }

    let decoded_genesis = decode_genesis(&genesis)?;
    let mut required_programs = BTreeSet::new();
    for datom in &decoded_genesis {
        collect_function_hashes(&datom.value, &mut required_programs);
    }
    for entry in &log.entries {
        for datom in &entry.transaction.tx_data {
            collect_function_hashes(&datom.value, &mut required_programs);
        }
    }
    let programs = load_program_graph(directory, required_programs)?;
    for (hash, (program, payload)) in programs {
        let kind = program_kind(&program);
        let arity = i16::from(program.arity);
        let Some(row) = client
            .query_opt(
                "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| {
                crate::postgres::postgres_error("backup/restore-check-program", error)
            })?
        else {
            return Ok(None);
        };
        if row.get::<_, i16>(0) != kind
            || row.get::<_, i16>(1) != arity
            || row.get::<_, Vec<u8>>(2) != payload
        {
            return Ok(None);
        }
    }
    Ok(Some(MatchedRestoreTarget {
        lineage_id,
        generation: target_generation,
        basis_t: target_basis,
        tx_hash: target_head_hash,
        state_hash: checkpoint_state_hash(expected_database)?,
        eidx_frontier: expected_database.eidx_frontier(),
        genesis_hash: manifest.genesis_hash,
    }))
}

fn validate_legacy_receipt_archive<C: crate::sql_io::GenericClient>(
    client: &mut C,
    archive: &PersistentTreeManifest,
    hash: Digest,
    expected_count: u64,
    expected_set: Digest,
) -> Result<(), SemanticError> {
    let roots = client.query("SELECT index_order,history,root_hash,datom_count,encoded_bytes FROM atomic_request_base_archive_roots WHERE manifest_hash=$1 ORDER BY history,index_order", &[&&hash[..]])
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-legacy-roots", error))?;
    if roots.len() != archive.trees.len() {
        return Err(fault(
            "backup/restore-check-legacy-roots",
            "legacy receipt archive root projection is incomplete",
        ));
    }
    let mut reachable = BTreeSet::new();
    for (row, tree) in roots.into_iter().zip(&archive.trees) {
        if decode_tree_order(row.get(0))? != tree.descriptor.order
            || row.get::<_, bool>(1) != tree.descriptor.history
            || digest(row.get(2), "legacy archive root")? != tree.descriptor.root_hash
            || unsigned(row.get(3), "legacy archive root count")? != tree.descriptor.count
            || unsigned(row.get(4), "legacy archive root bytes")? != tree.root_bytes
        {
            return Err(fault(
                "backup/restore-check-legacy-roots",
                "legacy receipt archive root projection differs from its manifest",
            ));
        }
        let validated = persistent_tree::validate_tree_streaming(&tree.descriptor, |node_hash| {
            let payload: Vec<u8> = client
                .query_opt(
                    "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
                    &[&&node_hash[..]],
                )
                .map_err(|error| {
                    crate::postgres::postgres_error("backup/restore-check-legacy-node", error)
                })?
                .ok_or_else(|| {
                    fault(
                        "backup/restore-check-legacy-node",
                        "legacy receipt archive references a missing node",
                    )
                })?
                .get(0);
            if *node_hash == tree.descriptor.root_hash && payload.len() as u64 != tree.root_bytes {
                return Err(fault(
                    "backup/restore-check-legacy-node",
                    "legacy receipt archive root size differs from its manifest",
                ));
            }
            Ok(payload)
        })?;
        reachable.extend(validated.node_hashes);
    }
    let planned = client.query("SELECT node_hash FROM atomic_request_base_archive_nodes WHERE manifest_hash=$1 ORDER BY node_hash", &[&&hash[..]])
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-legacy-closure", error))?
        .into_iter().map(|row| digest(row.get(0), "legacy archive planned node")).collect::<Result<BTreeSet<_>, _>>()?;
    if planned != reachable
        || expected_count != reachable.len() as u64
        || expected_set != request_base_archive_node_set_hash(&reachable)
    {
        return Err(fault(
            "backup/restore-check-legacy-closure",
            "legacy receipt archive membership differs from its complete authenticated closure",
        ));
    }
    Ok(())
}

#[derive(Debug)]
struct BackupDirectoryGuard {
    _lock: File,
}

fn prepare_directory(directory: &Path) -> Result<BackupDirectoryGuard, SemanticError> {
    let root_existed = fs::symlink_metadata(directory).is_ok();
    if root_existed {
        require_private_directory(directory, "backup/directory")?;
    }
    create_private_directory(directory, "backup/create-directory")?;
    create_private_directory(&objects(directory), "backup/create-objects")?;
    create_private_directory(&snapshots(directory), "backup/create-snapshots")?;
    validate_backup_directory(directory, false)?;
    let guard = lock_backup_directory(directory)?;
    // An abrupt process exit can strand at most the currently staged file.
    // Sweep only our exact private staging-name grammar, only in the three
    // directories where publication occurs, and only when the entry itself
    // is a regular file. `symlink_metadata` and `remove_file` never traverse
    // a matching symlink.
    cleanup_stale_publish_temps(directory)?;
    sync_directory(&objects(directory), "backup/sync-objects-directory")?;
    sync_directory(&snapshots(directory), "backup/sync-snapshots-directory")?;
    sync_directory(directory, "backup/sync-directory")?;
    if !root_existed && let Some(parent) = directory.parent() {
        sync_directory(parent, "backup/sync-parent-directory")?;
    }
    Ok(guard)
}

fn lock_backup_directory(directory: &Path) -> Result<BackupDirectoryGuard, SemanticError> {
    let path = directory.join(".atomic-lock");
    let file = match fs::symlink_metadata(&path) {
        Ok(metadata) => {
            if !metadata.file_type().is_file() {
                return Err(SemanticError::new(
                    ErrorCategory::Forbidden,
                    "backup/lock-type",
                    "backup repository lock is not a regular file",
                ));
            }
            OpenOptions::new()
                .read(true)
                .write(true)
                .open(&path)
                .map_err(io_error("backup/open-lock"))?
        }
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => {
            #[cfg(unix)]
            {
                use std::os::unix::fs::OpenOptionsExt as _;
                match OpenOptions::new()
                    .read(true)
                    .write(true)
                    .create_new(true)
                    .mode(0o600)
                    .open(&path)
                {
                    Ok(file) => file,
                    Err(error) if error.kind() == std::io::ErrorKind::AlreadyExists => {
                        require_regular_file(&path, "backup/lock-type")?;
                        OpenOptions::new()
                            .read(true)
                            .write(true)
                            .open(&path)
                            .map_err(io_error("backup/open-lock"))?
                    }
                    Err(error) => return Err(io_error("backup/create-lock")(error)),
                }
            }
            #[cfg(not(unix))]
            {
                match OpenOptions::new()
                    .read(true)
                    .write(true)
                    .create_new(true)
                    .open(&path)
                {
                    Ok(file) => file,
                    Err(error) if error.kind() == std::io::ErrorKind::AlreadyExists => {
                        require_regular_file(&path, "backup/lock-type")?;
                        OpenOptions::new()
                            .read(true)
                            .write(true)
                            .open(&path)
                            .map_err(io_error("backup/open-lock"))?
                    }
                    Err(error) => return Err(io_error("backup/create-lock")(error)),
                }
            }
        }
        Err(error) => return Err(io_error("backup/lock-metadata")(error)),
    };
    if !file
        .metadata()
        .map_err(io_error("backup/lock-metadata"))?
        .is_file()
    {
        return Err(SemanticError::new(
            ErrorCategory::Forbidden,
            "backup/lock-type",
            "backup repository lock is not a regular file",
        ));
    }
    file.lock().map_err(io_error("backup/lock"))?;
    Ok(BackupDirectoryGuard { _lock: file })
}

fn cleanup_stale_publish_temps(directory: &Path) -> Result<(), SemanticError> {
    for parent in [
        directory.to_path_buf(),
        objects(directory),
        snapshots(directory),
    ] {
        let mut removed = false;
        for entry in fs::read_dir(&parent).map_err(io_error("backup/temp-list"))? {
            let entry = entry.map_err(io_error("backup/temp-list-entry"))?;
            if !is_publish_temp_name(&entry.file_name()) {
                continue;
            }
            let metadata =
                fs::symlink_metadata(entry.path()).map_err(io_error("backup/temp-metadata"))?;
            if !metadata.file_type().is_file() {
                continue;
            }
            fs::remove_file(entry.path()).map_err(io_error("backup/remove-stale-temp"))?;
            removed = true;
        }
        if removed {
            sync_directory(&parent, "backup/sync-temp-cleanup-directory")?;
        }
    }
    Ok(())
}

fn is_publish_temp_name(name: &std::ffi::OsStr) -> bool {
    let Some(name) = name.to_str() else {
        return false;
    };
    let Some(rest) = name.strip_prefix(".atomic-tmp-") else {
        return false;
    };
    let Some((pid, sequence)) = rest.rsplit_once('-') else {
        return false;
    };
    let Ok(parsed_pid) = pid.parse::<u32>() else {
        return false;
    };
    pid == parsed_pid.to_string()
        && sequence.len() == 16
        && sequence
            .bytes()
            .all(|byte| byte.is_ascii_digit() || (b'a'..=b'f').contains(&byte))
}

fn create_private_directory(path: &Path, code: &'static str) -> Result<(), SemanticError> {
    #[cfg(unix)]
    {
        use std::os::unix::fs::DirBuilderExt as _;
        let mut builder = fs::DirBuilder::new();
        builder.recursive(true).mode(0o700);
        builder.create(path).map_err(io_error(code))
    }
    #[cfg(not(unix))]
    {
        fs::create_dir_all(path).map_err(io_error(code))
    }
}

fn validate_backup_directory(directory: &Path, require_claim: bool) -> Result<(), SemanticError> {
    require_private_directory(directory, "backup/directory")?;
    require_private_directory(&objects(directory), "backup/objects-directory")?;
    require_private_directory(&snapshots(directory), "backup/snapshots-directory")?;
    if require_claim {
        require_regular_file(&directory.join("CLAIM"), "backup/claim-type")?;
    }
    Ok(())
}

fn require_private_directory(path: &Path, code: &'static str) -> Result<(), SemanticError> {
    let metadata = fs::symlink_metadata(path).map_err(io_error(code))?;
    if !metadata.file_type().is_dir() {
        return Err(SemanticError::new(
            ErrorCategory::Forbidden,
            code,
            format!("{} is not a real directory", path.display()),
        ));
    }
    #[cfg(unix)]
    {
        use std::os::unix::fs::MetadataExt as _;
        if metadata.mode() & 0o077 != 0 {
            return Err(SemanticError::new(
                ErrorCategory::Forbidden,
                "backup/directory-permissions",
                format!(
                    "{} is group/world accessible; backup storage must be operator-private",
                    path.display()
                ),
            ));
        }
    }
    Ok(())
}

fn require_regular_file(path: &Path, code: &'static str) -> Result<(), SemanticError> {
    let metadata = fs::symlink_metadata(path).map_err(io_error(code))?;
    if !metadata.file_type().is_file() {
        return Err(SemanticError::new(
            ErrorCategory::Forbidden,
            code,
            format!("{} is not a regular file", path.display()),
        ));
    }
    Ok(())
}

fn ensure_claim(directory: &Path, claim: &BackupClaim) -> Result<(), SemanticError> {
    let encoded = encode_claim(claim)?;
    let path = directory.join("CLAIM");
    match fs::symlink_metadata(&path) {
        Ok(_) => return require_matching_claim(&path, claim),
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => {}
        Err(error) => return Err(io_error("backup/claim-metadata")(error)),
    }
    match publish_exact(&path, &encoded, PublishFault::None) {
        Ok(_) => Ok(()),
        Err(error) if error.code == "backup/file-conflict" => require_matching_claim(&path, claim),
        Err(error) => Err(error),
    }
}

fn require_matching_claim(path: &Path, expected: &BackupClaim) -> Result<(), SemanticError> {
    require_regular_file(path, "backup/claim-type")?;
    let bytes = backup_file::read_claim_file(path)?;
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

/// Copies authenticated immutable values immediately and retains only their
/// hashes. This mirrors recovered backup's value copier: duplicate content is
/// checked but counted once, and the separately published snapshot root is
/// still the sole discoverability boundary.
struct ObjectPublisher<'a> {
    directory: &'a Path,
    fault_at: BackupFault,
    seen: BTreeSet<Digest>,
    written: usize,
    reused: usize,
    maintenance: Option<crate::MaintenanceControl>,
    batch_objects: usize,
    batch_bytes: usize,
}

impl<'a> ObjectPublisher<'a> {
    fn new(directory: &'a Path, fault_at: BackupFault) -> Self {
        Self {
            directory,
            fault_at,
            seen: BTreeSet::new(),
            written: 0,
            reused: 0,
            maintenance: None,
            batch_objects: 0,
            batch_bytes: 0,
        }
    }

    fn publish(&mut self, hash: Digest, bytes: &[u8]) -> Result<(), SemanticError> {
        if let Some(control) = &self.maintenance {
            control.check()?;
        }
        let first = self.seen.insert(hash);
        let fault_at = if first
            && self.seen.len() == 1
            && self.fault_at == BackupFault::AfterFirstObjectStaged
        {
            PublishFault::AfterStaged
        } else {
            PublishFault::None
        };
        let written = write_object(self.directory, hash, bytes, fault_at)?;
        if first {
            if written {
                self.written += 1;
            } else {
                self.reused += 1;
            }
            self.batch_objects += 1;
            self.batch_bytes = self.batch_bytes.saturating_add(bytes.len());
            if self.batch_objects >= 64 || self.batch_bytes >= 4 * 1024 * 1024 {
                if let Some(control) = &self.maintenance {
                    control.after_batch()?;
                }
                self.batch_objects = 0;
                self.batch_bytes = 0;
            }
        }
        Ok(())
    }
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
    match fs::symlink_metadata(path) {
        Ok(_) => return existing_file_matches(path, bytes).map(|()| false),
        Err(error) if error.kind() == std::io::ErrorKind::NotFound => {}
        Err(error) => return Err(io_error("backup/file-metadata")(error)),
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
    require_regular_file(path, "backup/file-type")?;
    if backup_file::file_matches(path, bytes)? {
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

pub(crate) fn read_object(directory: &Path, hash: Digest) -> Result<Vec<u8>, SemanticError> {
    let path = objects(directory).join(hex(&hash));
    require_regular_file(&path, "backup/object-type")?;
    let bytes = backup_file::read_object_file(&path, hash)?;
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

fn snapshot_path(directory: &Path, basis: u64, log_generation: u64) -> PathBuf {
    snapshots(directory).join(format!("{basis:020}-g{log_generation:020}.atbk"))
}

fn parse_snapshot_name(name: &std::ffi::OsStr) -> Result<Option<(u64, u64)>, SemanticError> {
    let Some(name) = name.to_str() else {
        if Path::new(name).extension() == Some(std::ffi::OsStr::new("atbk")) {
            return Err(fault(
                "backup/root-name",
                "backup snapshot filename is not UTF-8",
            ));
        }
        return Ok(None);
    };
    let Some(stem) = name.strip_suffix(".atbk") else {
        return Ok(None);
    };
    let parse_coordinate = |value: &str, label: &str| {
        if value.len() != 20 || !value.bytes().all(|byte| byte.is_ascii_digit()) {
            return Err(fault(
                "backup/root-name",
                format!("backup snapshot {label} is not a canonical 20-digit integer"),
            ));
        }
        value.parse::<u64>().map_err(|_| {
            fault(
                "backup/root-name",
                format!("backup snapshot {label} is out of range"),
            )
        })
    };
    if stem.len() == 20 {
        return Ok(Some((parse_coordinate(stem, "basis")?, 0)));
    }
    let Some((basis, generation)) = stem.split_once("-g") else {
        return Err(fault(
            "backup/root-name",
            "backup snapshot filename is not canonical",
        ));
    };
    if generation.contains("-g") {
        return Err(fault(
            "backup/root-name",
            "backup snapshot filename has more than one generation separator",
        ));
    }
    Ok(Some((
        parse_coordinate(basis, "basis")?,
        parse_coordinate(generation, "generation")?,
    )))
}

fn load_manifest_path(
    path: &Path,
    basis: u64,
    log_generation: u64,
) -> Result<(Manifest, Digest), SemanticError> {
    require_regular_file(path, "backup/root-type")?;
    let bytes = backup_file::read_manifest_file(path)?;
    let hash = sha256(&bytes);
    let manifest = decode_manifest(&bytes)?;
    if manifest.basis != basis || manifest.log_generation != log_generation {
        return Err(fault(
            "backup/root-coordinate",
            "snapshot filename does not match its manifest generation and basis",
        ));
    }
    Ok((manifest, hash))
}

fn load_manifest_generation(
    directory: &Path,
    basis: u64,
    log_generation: u64,
) -> Result<(Manifest, Digest), SemanticError> {
    let mut selected: Option<(Manifest, Digest)> = None;
    for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list-root"))? {
        let entry = entry.map_err(io_error("backup/list-root-entry"))?;
        if parse_snapshot_name(&entry.file_name())? != Some((basis, log_generation)) {
            continue;
        }
        let candidate = load_manifest_path(&entry.path(), basis, log_generation)?;
        if selected
            .as_ref()
            .is_some_and(|(_, hash)| *hash != candidate.1)
        {
            return Err(fault(
                "backup/root-coordinate-conflict",
                "one backup generation and basis names different immutable roots",
            ));
        }
        selected = Some(candidate);
    }
    selected.ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::NotFound,
            "backup/root-not-found",
            format!("no backup root exists at generation {log_generation}, basis {basis}"),
        )
    })
}

fn load_manifest(directory: &Path, basis: u64) -> Result<(Manifest, Digest), SemanticError> {
    let mut generation = None;
    for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list-root"))? {
        let entry = entry.map_err(io_error("backup/list-root-entry"))?;
        let Some((candidate_basis, candidate_generation)) =
            parse_snapshot_name(&entry.file_name())?
        else {
            continue;
        };
        if candidate_basis == basis
            && generation.is_none_or(|current| candidate_generation > current)
        {
            generation = Some(candidate_generation);
        }
    }
    let generation = generation.ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::NotFound,
            "backup/root-not-found",
            format!("no backup root exists at basis {basis}"),
        )
    })?;
    load_manifest_generation(directory, basis, generation)
}

fn select_incremental_parent(
    directory: &Path,
    lineage_id: &str,
    genesis_hash: Digest,
    genesis_state_hash: Digest,
    log_generation: u64,
    basis: u64,
) -> Result<Option<ParentContext>, SemanticError> {
    let mut latest = None;
    for entry in fs::read_dir(snapshots(directory)).map_err(io_error("backup/list-parent"))? {
        let entry = entry.map_err(io_error("backup/list-parent-entry"))?;
        let Some((point, generation)) = parse_snapshot_name(&entry.file_name())? else {
            continue;
        };
        if generation == log_generation
            && point < basis
            && latest.is_none_or(|current| point > current)
        {
            latest = Some(point);
        }
    }
    let Some(latest) = latest else {
        return Ok(None);
    };
    let (manifest, _) = load_manifest_generation(directory, latest, log_generation)?;
    verify_claim(directory, &manifest)?;
    let parent = ManifestRoot { manifest };
    if parent.manifest.lineage_id != lineage_id
        || parent.manifest.log_generation != log_generation
        || parent.manifest.genesis_hash != genesis_hash
        || (parent.manifest.basis == 0
            && (parent.manifest.head_transaction_hash != genesis_hash
                || parent.manifest.head_state_hash != genesis_state_hash))
    {
        return Err(SemanticError::new(
            ErrorCategory::Conflict,
            "backup/parent-identity",
            "latest backup root is not a prefix of this database lineage",
        ));
    }
    // Version 3 request metadata was flattened into its snapshot root and has
    // no immutable predecessor-linked request head. Read it, but start a new
    // self-contained v4 chain rather than inventing a bridge object.
    if parent.manifest.version != VERSION {
        return Ok(None);
    }
    Ok(Some(parent))
}

#[allow(clippy::too_many_arguments)]
fn authenticate_source_parent<C: crate::sql_io::GenericClient>(
    client: &mut C,
    directory: &Path,
    database_id: &str,
    lineage_id: &str,
    genesis_hash: Digest,
    genesis_state_hash: Digest,
    genesis_frontier: u64,
    parent: &ParentContext,
) -> Result<(Digest, Digest, u64), SemanticError> {
    if parent.manifest.basis == 0 {
        if parent.manifest.head_transaction_hash != genesis_hash
            || parent.manifest.head_state_hash != genesis_state_hash
        {
            return Err(fault(
                "backup/parent-anchor",
                "basis-zero parent has the wrong genesis anchor",
            ));
        }
        return Ok((genesis_hash, genesis_hash, genesis_frontier));
    }
    let membership_payload = read_object(directory, parent.manifest.head_transaction_hash)?;
    let membership = decode_backup_membership(&membership_payload, parent.manifest.log_generation)?;
    let content_payload = read_object(directory, membership.content_hash)?;
    let content = LineageTransactionContent::decode(&content_payload)?;
    if membership.lineage_id != lineage_id
        || membership.basis != parent.manifest.basis
        || membership.state_hash != parent.manifest.head_state_hash
        || sha256(&membership_payload) != parent.manifest.head_transaction_hash
        || sha256(&content_payload) != membership.content_hash
        || content.lineage_id != lineage_id
        || content.basis_t != membership.basis
        || content.eidx_frontier != membership.eidx_frontier
    {
        return Err(fault(
            "backup/parent-anchor",
            "parent portable membership or content anchor is invalid",
        ));
    }
    let basis_sql = i64::try_from(parent.manifest.basis).map_err(|_| {
        fault(
            "backup/parent-basis",
            "parent basis exceeds PostgreSQL bigint",
        )
    })?;
    let source_hash = if parent.manifest.log_generation > 0 {
        let generation_sql = i64::try_from(parent.manifest.log_generation).map_err(|_| {
            fault(
                "backup/generation-overflow",
                "parent generation exceeds PostgreSQL bigint",
            )
        })?;
        let row = client
            .query_opt(
                "SELECT t.previous_hash, t.tx_hash, t.content_hash, t.state_hash, \
                        t.eidx_frontier, c.payload, c.lineage_id, c.basis_t, \
                        c.eidx_frontier, c.envelope_version \
                   FROM atomic_generation_transactions t \
                   JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
                  WHERE t.database_id = $1 AND t.generation = $2 AND t.basis_t = $3",
                &[&database_id, &generation_sql, &basis_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/parent-generation", error))?
            .ok_or_else(|| {
                fault(
                    "backup/parent-row-missing",
                    "source generation no longer contains the backup parent basis",
                )
            })?;
        let source_hash = digest(row.get(1), "parent source transaction hash")?;
        if digest(row.get(0), "parent source predecessor")? != membership.previous_hash
            || source_hash != parent.manifest.head_transaction_hash
            || digest(row.get(2), "parent source content hash")? != membership.content_hash
            || digest(row.get(3), "parent source state hash")? != membership.state_hash
            || unsigned(row.get(4), "parent source frontier")? != membership.eidx_frontier
            || row.get::<_, Vec<u8>>(5) != content_payload
            || row.get::<_, String>(6) != lineage_id
            || unsigned(row.get(7), "parent content basis")? != membership.basis
            || unsigned(row.get(8), "parent content frontier")? != membership.eidx_frontier
            || row.get::<_, i16>(9)
                != i16::try_from(content.version()).expect("ATLC version fits i16")
        {
            return Err(fault(
                "backup/parent-source",
                "source generation differs from the backup parent",
            ));
        }
        source_hash
    } else {
        let row = client
            .query_opt(
                "SELECT tx_hash, payload, state_hash FROM atomic_transactions \
                 WHERE database_id = $1 AND basis_t = $2",
                &[&database_id, &basis_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/parent-row", error))?
            .ok_or_else(|| {
                fault(
                    "backup/parent-row-missing",
                    "legacy source no longer contains the backup parent basis",
                )
            })?;
        let source_hash = digest(row.get(0), "parent source transaction hash")?;
        let source_payload: Vec<u8> = row.get(1);
        let source_state_hash = digest(row.get(2), "parent source state hash")?;
        let source = decode_transaction(&source_payload)?;
        let prior_frontier = if membership.basis == 1 {
            genesis_frontier
        } else {
            let previous_payload = read_object(directory, membership.previous_hash)?;
            decode_backup_membership(&previous_payload, 0)?.eidx_frontier
        };
        let rebound =
            LineageTransactionContent::from_transaction(lineage_id, prior_frontier, &source)?;
        if source.basis_t != parent.manifest.basis
            || (source.database_id != database_id && source.database_id != lineage_id)
            || transaction_hash(&source_payload) != source_hash
            || source_state_hash != membership.state_hash
            || rebound != content
        {
            return Err(fault(
                "backup/parent-source",
                "legacy source information differs from the backup parent",
            ));
        }
        source_hash
    };
    Ok((
        source_hash,
        parent.manifest.head_transaction_hash,
        membership.eidx_frontier,
    ))
}

/// Reuse an already-published root only after authenticating its full object
/// graph and comparing every authoritative logical field captured from the
/// live repeatable-read snapshot. The physical tree pointer is intentionally
/// excluded: it is a replaceable derived representation of the same value.
fn reusable_existing_point(
    directory: &Path,
    candidate: &Manifest,
) -> Result<Option<BackupPoint>, SemanticError> {
    let path = snapshot_path(directory, candidate.basis, candidate.log_generation);
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

    let (existing, manifest_hash) =
        load_manifest_generation(directory, candidate.basis, candidate.log_generation)?;
    verify_claim(directory, &existing)?;
    let genesis = read_object(directory, existing.genesis_hash)?;
    let decoded_genesis = decode_genesis(&genesis)?;
    if encode_genesis(&decoded_genesis)? != genesis {
        return Err(fault(
            "backup/genesis-corrupt",
            "backup genesis is not canonically encoded",
        ));
    }
    let log = load_backup_log(directory, &existing)?;
    backup_read::verify_read_log_index(directory, &existing, &log)?;
    let (head_hash, head_state_hash) = match log.entries.last() {
        Some(entry) => (entry.transaction_hash, entry.legacy_state_hash),
        None => (
            existing.genesis_hash,
            Some(checkpoint_state_hash(&Database::from_genesis(
                decoded_genesis,
            )?)?),
        ),
    };
    if head_hash != existing.head_transaction_hash
        || (existing.version == VERSION && head_state_hash != Some(existing.head_state_hash))
    {
        return Err(fault(
            "backup/basis-mismatch",
            "backup endpoint root disagrees with its authenticated log coordinate",
        ));
    }
    load_completed_excisions(directory, &existing)?;
    // Despite its historical name this helper reads and authenticates every
    // temporal/transitive program payload, not merely its file's presence.
    verify_program_presence(directory, &existing, &log)?;
    if let Some(tree) = &existing.tree {
        verify_tree_structure(directory, &existing, &log, tree)?;
    }
    for tree in request_base_trees(&log) {
        verify_tree_structure(directory, &existing, &log, &tree)?;
    }
    let (_, observed_hash) =
        load_manifest_generation(directory, candidate.basis, candidate.log_generation)?;
    if manifest_hash != observed_hash {
        return Err(fault(
            "backup/root-changed",
            "published backup root changed while its object graph was being authenticated",
        ));
    }
    let same_logical_point = existing.lineage_id == candidate.lineage_id
        && existing.log_generation == candidate.log_generation
        && existing.basis == candidate.basis
        && existing.genesis_hash == candidate.genesis_hash
        && existing.head_transaction_hash == candidate.head_transaction_hash
        && existing.head_state_hash == candidate.head_state_hash
        && existing.request_head_hash == candidate.request_head_hash
        && existing.completed_excision_head_hash == candidate.completed_excision_head_hash
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
        log_generation: candidate.log_generation,
        basis_t: candidate.basis,
        manifest_hash,
        objects_written: 0,
        objects_reused: 0,
    }))
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

fn encode_backup_membership(record: &BackupMembershipRecord) -> Result<Vec<u8>, SemanticError> {
    if record.log_generation > 0 {
        return encode_generation_transaction_membership(
            &record.lineage_id,
            record.log_generation,
            record.basis,
            record.previous_hash,
            record.content_hash,
            record.state_hash,
            record.eidx_frontier,
        );
    }
    if !valid_lineage_id(&record.lineage_id)
        || record.basis == 0
        || record.content_hash == [0; 32]
        || record.state_hash == [0; 32]
    {
        return Err(SemanticError::incorrect(
            "backup/legacy-membership-coordinate",
            "portable legacy membership has an invalid lineage, basis, content, or state",
        ));
    }
    let mut bytes = Vec::with_capacity(LEGACY_MEMBERSHIP_DOMAIN.len() + 156);
    bytes.extend_from_slice(LEGACY_MEMBERSHIP_DOMAIN);
    bytes.extend_from_slice(record.lineage_id.as_bytes());
    put_u64(&mut bytes, 0);
    put_u64(&mut bytes, record.basis);
    bytes.extend_from_slice(&record.previous_hash);
    bytes.extend_from_slice(&record.content_hash);
    bytes.extend_from_slice(&record.state_hash);
    put_u64(&mut bytes, record.eidx_frontier);
    Ok(bytes)
}

fn decode_backup_membership(
    bytes: &[u8],
    log_generation: u64,
) -> Result<BackupMembershipRecord, SemanticError> {
    let record = if log_generation > 0 {
        let decoded = GenerationTransactionMembership::decode(bytes)?;
        BackupMembershipRecord {
            lineage_id: decoded.lineage_id,
            log_generation: decoded.generation,
            basis: decoded.basis_t,
            previous_hash: decoded.previous_hash,
            content_hash: decoded.content_hash,
            state_hash: decoded.state_hash,
            eidx_frontier: decoded.eidx_frontier,
        }
    } else {
        let expected_len = LEGACY_MEMBERSHIP_DOMAIN.len() + 156;
        if bytes.len() != expected_len || !bytes.starts_with(LEGACY_MEMBERSHIP_DOMAIN) {
            return Err(fault(
                "backup/legacy-membership-encoding",
                "portable legacy membership has an invalid encoding",
            ));
        }
        let mut cursor = Cursor::new(&bytes[LEGACY_MEMBERSHIP_DOMAIN.len()..]);
        let lineage_id = std::str::from_utf8(cursor.take(36)?)
            .map_err(|_| {
                fault(
                    "backup/legacy-membership-lineage",
                    "portable legacy membership lineage is not UTF-8",
                )
            })?
            .to_owned();
        let generation = cursor.u64()?;
        let record = BackupMembershipRecord {
            lineage_id,
            log_generation: generation,
            basis: cursor.u64()?,
            previous_hash: cursor.digest()?,
            content_hash: cursor.digest()?,
            state_hash: cursor.digest()?,
            eidx_frontier: cursor.u64()?,
        };
        cursor.finish()?;
        record
    };
    if record.log_generation != log_generation || encode_backup_membership(&record)? != bytes {
        return Err(fault(
            "backup/noncanonical-membership",
            "backup transaction membership is not canonical for its root generation",
        ));
    }
    Ok(record)
}

fn encode_request_record(record: &BackupRequestRecord) -> Result<Vec<u8>, SemanticError> {
    encode_request_record_version(record, REQUEST_VERSION)
}

fn encode_request_record_version(
    record: &BackupRequestRecord,
    version: u16,
) -> Result<Vec<u8>, SemanticError> {
    if !valid_lineage_id(&record.lineage_id)
        || record.basis == 0
        || record.request_key_hash == [0; 32]
        || !matches!(record.request_kind, 0..=2)
        || (record.request_kind == 2) != record.base_manifest_hash.is_some()
        || (version == LEGACY_REQUEST_VERSION
            && (record.request_kind == 2 || record.base_manifest_hash.is_some()))
        || !matches!(version, LEGACY_REQUEST_VERSION | REQUEST_VERSION)
        || (record.request_kind == 0 && !record.receipt_tempids.is_empty())
        || record
            .receipt_tempids
            .iter()
            .any(|(name, entity)| name.is_empty() || *entity > i64::MAX as u64)
    {
        return Err(SemanticError::incorrect(
            "backup/request-record",
            "request record identity, basis, kind, or receipt is invalid",
        ));
    }
    let mut bytes = Vec::new();
    bytes.extend_from_slice(REQUEST_MAGIC);
    bytes.extend_from_slice(&version.to_be_bytes());
    put_string(&mut bytes, &record.lineage_id)?;
    put_u64(&mut bytes, record.log_generation);
    put_u64(&mut bytes, record.basis);
    bytes.extend_from_slice(&record.transaction_hash);
    bytes.extend_from_slice(&record.previous_hash);
    bytes.extend_from_slice(&record.request_key_hash);
    bytes.extend_from_slice(&record.digest);
    bytes.push(record.request_kind);
    if version == REQUEST_VERSION {
        match record.base_manifest_hash {
            None => bytes.push(0),
            Some(hash) => {
                bytes.push(1);
                bytes.extend_from_slice(&hash);
            }
        }
    }
    put_u32(&mut bytes, record.receipt_tempids.len())?;
    for (name, entity) in &record.receipt_tempids {
        put_string(&mut bytes, name)?;
        put_u64(&mut bytes, *entity);
    }
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_request_record(bytes: &[u8]) -> Result<BackupRequestRecord, SemanticError> {
    if bytes.len() < 223 || &bytes[..4] != REQUEST_MAGIC {
        return Err(fault(
            "backup/request-record-header",
            "backup request record header is invalid",
        ));
    }
    let version = u16::from_be_bytes([bytes[4], bytes[5]]);
    if !matches!(version, LEGACY_REQUEST_VERSION | REQUEST_VERSION) {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/request-record-version",
            "unsupported backup request record version",
        ));
    }
    let checksum_at = bytes.len() - 32;
    if sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..] {
        return Err(fault(
            "backup/request-record-checksum",
            "backup request record checksum is invalid",
        ));
    }
    let mut cursor = Cursor::new(&bytes[6..checksum_at]);
    let lineage_id = cursor.string()?;
    let log_generation = cursor.u64()?;
    let basis = cursor.u64()?;
    let transaction_hash = cursor.digest()?;
    let previous_hash = cursor.digest()?;
    let request_key_hash = cursor.digest()?;
    let digest = cursor.digest()?;
    let request_kind = cursor.u8()?;
    let base_manifest_hash = if version == REQUEST_VERSION {
        match cursor.u8()? {
            0 => None,
            1 => Some(cursor.digest()?),
            _ => {
                return Err(fault(
                    "backup/request-record-base-tag",
                    "backup request record has an invalid base-tree tag",
                ));
            }
        }
    } else {
        None
    };
    let count = cursor.count_with_minimum(12)?;
    let mut receipt_tempids = BTreeMap::new();
    for _ in 0..count {
        let name = cursor.string()?;
        let entity = cursor.u64()?;
        if receipt_tempids.insert(name, entity).is_some() {
            return Err(fault(
                "backup/request-record-receipt",
                "backup request receipt contains duplicate tempid names",
            ));
        }
    }
    let record = BackupRequestRecord {
        lineage_id,
        log_generation,
        basis,
        transaction_hash,
        previous_hash,
        request_key_hash,
        digest,
        request_kind,
        base_manifest_hash,
        receipt_tempids,
    };
    cursor.finish()?;
    if !valid_lineage_id(&record.lineage_id)
        || record.basis == 0
        || encode_request_record_version(&record, version)? != bytes
    {
        return Err(fault(
            "backup/noncanonical-request-record",
            "backup request record is not canonical",
        ));
    }
    Ok(record)
}

fn encode_completed_excision(record: &BackupCompletedExcision) -> Result<Vec<u8>, SemanticError> {
    if !valid_lineage_id(&record.lineage_id) || record.request_t == 0 {
        return Err(SemanticError::incorrect(
            "backup/completed-excision-record",
            "completed excision requires a canonical lineage and positive request t",
        ));
    }
    let mut bytes = Vec::new();
    bytes.extend_from_slice(COMPLETED_EXCISION_MAGIC);
    bytes.extend_from_slice(&COMPLETED_EXCISION_VERSION.to_be_bytes());
    put_string(&mut bytes, &record.lineage_id)?;
    put_u64(&mut bytes, record.request_t);
    put_u64(&mut bytes, record.request_entity);
    bytes.extend_from_slice(&record.previous_hash);
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_completed_excision(bytes: &[u8]) -> Result<BackupCompletedExcision, SemanticError> {
    if bytes.len() < 118 || bytes.get(..4) != Some(COMPLETED_EXCISION_MAGIC.as_slice()) {
        return Err(fault(
            "backup/completed-excision-header",
            "completed excision object has an invalid header",
        ));
    }
    if u16::from_be_bytes([bytes[4], bytes[5]]) != COMPLETED_EXCISION_VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/completed-excision-version",
            "unsupported completed excision object version",
        ));
    }
    let checksum_at = bytes.len() - 32;
    if sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..] {
        return Err(fault(
            "backup/completed-excision-checksum",
            "completed excision object checksum is invalid",
        ));
    }
    let mut cursor = Cursor::new(&bytes[6..checksum_at]);
    let record = BackupCompletedExcision {
        lineage_id: cursor.string()?,
        request_t: cursor.u64()?,
        request_entity: cursor.u64()?,
        previous_hash: cursor.digest()?,
    };
    cursor.finish()?;
    if encode_completed_excision(&record)? != bytes {
        return Err(fault(
            "backup/noncanonical-completed-excision",
            "completed excision object is not canonical",
        ));
    }
    Ok(record)
}

fn load_completed_excisions(
    directory: &Path,
    manifest: &Manifest,
) -> Result<Vec<(u64, u64)>, SemanticError> {
    if manifest.version == LEGACY_VERSION {
        return Ok(Vec::new());
    }
    let mut current = manifest.completed_excision_head_hash;
    let mut reversed = Vec::new();
    let mut seen_hashes = BTreeSet::new();
    while current != manifest.genesis_hash {
        if !seen_hashes.insert(current) {
            return Err(fault(
                "backup/completed-excision-cycle",
                "completed excision chain contains a cycle",
            ));
        }
        let payload = read_object(directory, current)?;
        let record = decode_completed_excision(&payload)?;
        if record.lineage_id != manifest.lineage_id || sha256(&payload) != current {
            return Err(fault(
                "backup/completed-excision-chain",
                "completed excision chain is not bound to this lineage",
            ));
        }
        reversed.push((record.request_t, record.request_entity));
        current = record.previous_hash;
    }
    reversed.reverse();
    if !reversed.windows(2).all(|pair| pair[0] < pair[1]) {
        return Err(fault(
            "backup/completed-excision-order",
            "completed excision chain is not in canonical identity order",
        ));
    }
    Ok(reversed)
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
    let path = directory.join("CLAIM");
    require_regular_file(&path, "backup/claim-type")?;
    let bytes = backup_file::read_claim_file(&path)?;
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
    if !valid_lineage_id(&manifest.lineage_id) {
        return false;
    }
    match manifest.version {
        LEGACY_VERSION => {
            let Ok(basis_len) = usize::try_from(manifest.basis) else {
                return false;
            };
            manifest.log_generation == 0
                && manifest.completed_excision_head_hash == manifest.genesis_hash
                && manifest.transactions.len() == basis_len
                && manifest.state_hashes.len() == basis_len
                && manifest.requests.len() == basis_len
                && manifest.state_hashes.iter().all(|hash| hash != &[0; 32])
                && manifest
                    .requests
                    .iter()
                    .enumerate()
                    .all(|(offset, request)| {
                        !request.key.is_empty()
                            && request.basis == offset as u64 + 1
                            && manifest.transactions.get(offset) == Some(&request.tx_hash)
                    })
                && manifest
                    .requests
                    .iter()
                    .map(|row| row.key.as_str())
                    .collect::<BTreeSet<_>>()
                    .len()
                    == manifest.requests.len()
                && strictly_sorted_by(&manifest.programs, |row| row.hash)
                && manifest.programs.iter().all(|row| {
                    (0..=3).contains(&row.kind) && (0..=i16::from(u8::MAX)).contains(&row.arity)
                })
                && manifest.tree.as_ref().is_none_or(|tree| {
                    tree.legacy_node_hashes.as_ref().is_some_and(|hashes| {
                        !hashes.is_empty()
                            && strictly_sorted_by(hashes, |hash| *hash)
                            && hashes.binary_search(&tree.manifest_hash).is_err()
                    })
                })
        }
        VERSION => {
            manifest.transactions.is_empty()
                && manifest.state_hashes.is_empty()
                && manifest.requests.is_empty()
                && manifest.programs.is_empty()
                && manifest.head_state_hash != [0; 32]
                && manifest
                    .tree
                    .as_ref()
                    .is_none_or(|tree| tree.legacy_node_hashes.is_none())
                && if manifest.basis == 0 {
                    manifest.head_transaction_hash == manifest.genesis_hash
                        && manifest.request_head_hash == manifest.genesis_hash
                        && manifest.completed_excision_head_hash == manifest.genesis_hash
                } else {
                    manifest.head_transaction_hash != manifest.genesis_hash
                        && manifest.request_head_hash != manifest.genesis_hash
                }
        }
        _ => false,
    }
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
    if manifest.version == VERSION {
        put_u64(&mut body, manifest.log_generation);
    } else if manifest.log_generation != 0 {
        return Err(SemanticError::incorrect(
            "backup/legacy-generation",
            "version 3 manifests have implicit generation zero",
        ));
    }
    put_u64(&mut body, manifest.basis);
    body.extend_from_slice(&manifest.genesis_hash);
    if manifest.version == VERSION {
        body.extend_from_slice(&manifest.head_transaction_hash);
        body.extend_from_slice(&manifest.head_state_hash);
        body.extend_from_slice(&manifest.request_head_hash);
        body.extend_from_slice(&manifest.completed_excision_head_hash);
        match &manifest.tree {
            None => body.push(0),
            Some(tree) => {
                body.push(1);
                body.extend_from_slice(&tree.manifest_hash);
            }
        }
        match manifest.read_log_index {
            Some(hash) => {
                body.push(1);
                body.extend_from_slice(&hash);
            }
            None => body.push(0),
        }
    } else if manifest.version == LEGACY_VERSION {
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
                put_hashes(
                    &mut body,
                    tree.legacy_node_hashes.as_deref().ok_or_else(|| {
                        fault(
                            "backup/legacy-tree-nodes",
                            "version 3 tree is missing its node list",
                        )
                    })?,
                )?;
            }
        }
    } else {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/manifest-version",
            "unsupported backup manifest version",
        ));
    }
    let mut bytes = Vec::new();
    bytes.extend_from_slice(MAGIC);
    bytes.extend_from_slice(&manifest.version.to_be_bytes());
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
    let version = u16::from_be_bytes([bytes[4], bytes[5]]);
    if !matches!(version, LEGACY_VERSION | VERSION) {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "backup/manifest-version",
            "unsupported backup manifest version; create a fresh current-version database and backup, or use the originating version to read this archive",
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
    let log_generation = if version == VERSION { cursor.u64()? } else { 0 };
    let basis = cursor.u64()?;
    let genesis_hash = cursor.digest()?;
    let completed_excision_head_hash;
    let (
        head_transaction_hash,
        head_state_hash,
        request_head_hash,
        transactions,
        state_hashes,
        requests,
        programs,
        tree,
    ) = if version == VERSION {
        let head_transaction_hash = cursor.digest()?;
        let head_state_hash = cursor.digest()?;
        let request_head_hash = cursor.digest()?;
        completed_excision_head_hash = cursor.digest()?;
        let tree = match cursor.u8()? {
            0 => None,
            1 => Some(TreeBackup {
                manifest_hash: cursor.digest()?,
                legacy_node_hashes: None,
            }),
            _ => {
                return Err(fault(
                    "backup/manifest-tree-tag",
                    "backup manifest has an invalid tree tag",
                ));
            }
        };
        (
            head_transaction_hash,
            head_state_hash,
            request_head_hash,
            Vec::new(),
            Vec::new(),
            Vec::new(),
            Vec::new(),
            tree,
        )
    } else {
        completed_excision_head_hash = genesis_hash;
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
                legacy_node_hashes: Some(cursor.hashes()?),
            }),
            _ => {
                return Err(fault(
                    "backup/manifest-tree-tag",
                    "backup manifest has an invalid tree tag",
                ));
            }
        };
        let head_transaction_hash = transactions.last().copied().unwrap_or(genesis_hash);
        let head_state_hash = state_hashes.last().copied().unwrap_or([0; 32]);
        (
            head_transaction_hash,
            head_state_hash,
            genesis_hash,
            transactions,
            state_hashes,
            requests,
            programs,
            tree,
        )
    };
    let read_log_index = if version == VERSION {
        match cursor.u8()? {
            0 => None,
            1 => Some(cursor.digest()?),
            _ => return Err(fault("backup/log-index-tag", "invalid log index tag")),
        }
    } else {
        None
    };
    cursor.finish()?;
    let manifest = Manifest {
        version,
        lineage_id,
        log_generation,
        basis,
        genesis_hash,
        head_transaction_hash,
        head_state_hash,
        request_head_hash,
        completed_excision_head_hash,
        transactions,
        state_hashes,
        requests,
        programs,
        tree,
        read_log_index,
    };
    if !manifest_is_canonical(&manifest) || encode_manifest(&manifest)? != bytes {
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
        crate::ProgramKind::DualPredicate => 4,
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
        match entity {
            crate::EntityRef::Lookup { value, .. } => collect_function_hashes(value, output),
            crate::EntityRef::LookupInput { value, .. } => input(value, output),
            _ => {}
        }
    }

    fn input(value: &crate::TxValue, output: &mut BTreeSet<Digest>) {
        match value {
            crate::TxValue::Scalar(value) => collect_function_hashes(value, output),
            crate::TxValue::Entity(value) => entity(value, output),
            crate::TxValue::Tuple(values) => {
                for value in values.iter().flatten() {
                    input(value, output);
                }
            }
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
                crate::Instruction::PredicateDispatch { attribute, entity } => {
                    instructions(attribute, output);
                    instructions(entity, output);
                }
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
        #[cfg(unix)]
        {
            use std::os::unix::fs::PermissionsExt as _;
            fs::set_permissions(&path, fs::Permissions::from_mode(0o700))
                .expect("make test directory private");
        }
        path
    }

    #[test]
    fn staged_file_is_never_visible_under_its_final_name_and_retry_converges() {
        let directory = temporary_directory("staged");
        let guard = prepare_directory(&directory).unwrap();
        let path = directory.join("root.atbk");
        let error = publish_exact(&path, b"complete-root", PublishFault::AfterStaged)
            .expect_err("injected interruption");
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert!(!path.exists());
        assert!(
            fs::read_dir(&directory)
                .unwrap()
                .any(|entry| is_publish_temp_name(&entry.unwrap().file_name()))
        );
        drop(guard);

        // The next repository publication owns the directory lock, removes
        // the abandoned complete staging file, and then proceeds normally.
        let _retry_guard = prepare_directory(&directory).unwrap();
        assert!(
            !fs::read_dir(&directory)
                .unwrap()
                .any(|entry| is_publish_temp_name(&entry.unwrap().file_name()))
        );

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

    #[test]
    fn current_manifest_is_a_constant_size_endpoint_root() {
        let genesis_hash = sha256(b"genesis");
        let manifest = Manifest {
            version: VERSION,
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            log_generation: 42,
            basis: u64::MAX,
            genesis_hash,
            head_transaction_hash: sha256(b"head transaction"),
            head_state_hash: sha256(b"head state"),
            request_head_hash: sha256(b"request head"),
            completed_excision_head_hash: sha256(b"completed excisions"),
            transactions: Vec::new(),
            state_hashes: Vec::new(),
            requests: Vec::new(),
            programs: Vec::new(),
            tree: Some(TreeBackup {
                manifest_hash: sha256(b"tree manifest"),
                legacy_node_hashes: None,
            }),
            read_log_index: None,
        };
        let encoded = encode_manifest(&manifest).unwrap();
        assert_eq!(encoded.len(), 296);
        assert_eq!(decode_manifest(&encoded).unwrap(), manifest);

        // Previous development archives are not silently reinterpreted as the
        // current read-index format. Reject before touching database state.
        let mut unsupported = encoded.clone();
        unsupported[4..6].copy_from_slice(&4u16.to_be_bytes());
        let end = unsupported.len() - 32;
        let checksum = sha256(&unsupported[..end]);
        unsupported[end..].copy_from_slice(&checksum);
        let error = decode_manifest(&unsupported).unwrap_err();
        assert_eq!(error.category, ErrorCategory::Unsupported);
        assert_eq!(error.code, "backup/manifest-version");
        assert!(error.message.contains("fresh current-version database"));

        let mut no_tree = manifest;
        no_tree.basis = 1;
        no_tree.tree = None;
        let encoded = encode_manifest(&no_tree).unwrap();
        assert_eq!(encoded.len(), 264);
        assert_eq!(decode_manifest(&encoded).unwrap(), no_tree);
    }

    #[test]
    fn linked_request_record_is_canonical_and_tamper_evident() {
        let mut record = BackupRequestRecord {
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            log_generation: 42,
            basis: 7,
            transaction_hash: sha256(b"transaction"),
            previous_hash: sha256(b"previous request"),
            request_key_hash: sha256(b"client-request-7"),
            digest: sha256(b"request payload"),
            request_kind: 1,
            base_manifest_hash: None,
            receipt_tempids: BTreeMap::from([("new-entity".into(), 1234)]),
        };
        let encoded = encode_request_record(&record).unwrap();
        assert_eq!(decode_request_record(&encoded).unwrap(), record);
        let mut damaged = encoded;
        damaged[80] ^= 1;
        assert_eq!(
            decode_request_record(&damaged).unwrap_err().code,
            "backup/request-record-checksum"
        );

        // Version two remains a readable archive format; version three adds
        // only the authenticated optional native db-before root.
        let legacy = encode_request_record_version(&record, LEGACY_REQUEST_VERSION).unwrap();
        assert_eq!(decode_request_record(&legacy).unwrap(), record);
        record.request_kind = 2;
        record.base_manifest_hash = Some(sha256(b"portable db-before manifest"));
        let native = encode_request_record(&record).unwrap();
        assert_eq!(decode_request_record(&native).unwrap(), record);
        // This is an encoder input/version mismatch, not a byte stream that
        // reached decode-side canonicality checking.
        assert_eq!(
            encode_request_record_version(&record, LEGACY_REQUEST_VERSION)
                .unwrap_err()
                .code,
            "backup/request-record"
        );
    }

    #[test]
    fn positive_generation_backup_membership_is_the_live_hash_preimage() {
        let record = BackupMembershipRecord {
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            log_generation: 42,
            basis: 7,
            previous_hash: sha256(b"previous"),
            content_hash: sha256(b"content"),
            state_hash: sha256(b"state"),
            eidx_frontier: 1_234,
        };
        let encoded = encode_backup_membership(&record).unwrap();
        assert_eq!(
            encoded,
            encode_generation_transaction_membership(
                &record.lineage_id,
                record.log_generation,
                record.basis,
                record.previous_hash,
                record.content_hash,
                record.state_hash,
                record.eidx_frontier,
            )
            .unwrap()
        );
        assert_eq!(
            sha256(&encoded),
            crate::log_generation::generation_transaction_hash(
                &record.lineage_id,
                record.log_generation,
                record.basis,
                record.previous_hash,
                record.content_hash,
                record.state_hash,
                record.eidx_frontier,
            )
            .unwrap()
        );
        assert_eq!(
            decode_backup_membership(&encoded, record.log_generation).unwrap(),
            record
        );
    }

    #[test]
    fn portable_v1_and_v2_preserve_existing_and_transaction_receipt_ids() {
        use crate::{DB_IDENT, EntityRef, Keyword, TxOp, Value};
        let lineage = "12345678-1234-4abc-8def-123456789abc";
        for modern in [false, true] {
            let initial = Database::bootstrap().unwrap();
            let before = if modern {
                initial
            } else {
                Database::from_genesis(initial.genesis_datoms().to_vec()).unwrap()
            };
            let report = before
                .with(
                    &[TxOp::Add {
                        entity: EntityRef::Temp("new-entity".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("receipt", "new")).into(),
                    }],
                    1_000,
                )
                .unwrap();
            let genesis = encode_genesis(before.genesis_datoms()).unwrap();
            let genesis_hash = sha256(&genesis);
            let mut transaction = DurableTransaction {
                database_id: lineage.into(),
                basis_t: 1,
                previous_hash: genesis_hash,
                eidx_frontier: report.db_after.eidx_frontier(),
                tempids: report.tempids.clone(),
                tx_data: report.tx_data.clone(),
            };
            transaction
                .tempids
                .insert("existing-upsert".into(), crate::DB_IDENT);
            transaction
                .tempids
                .insert("current-transaction".into(), crate::t_to_tx(1).unwrap());
            let content = if modern {
                LineageTransactionContent::from_transaction_v2(
                    lineage,
                    before.eidx_frontier(),
                    before.reserved_allocation().unwrap(),
                    report.db_after.reserved_allocation().unwrap(),
                    &transaction,
                )
                .unwrap()
            } else {
                LineageTransactionContent::from_transaction(
                    lineage,
                    before.eidx_frontier(),
                    &transaction,
                )
                .unwrap()
            };
            assert!(!content.allocations.contains(&crate::DB_IDENT));
            assert!(!content.allocations.contains(&crate::t_to_tx(1).unwrap()));
            let content_payload = content.encode().unwrap();
            let state_hash = checkpoint_state_hash(&report.db_after).unwrap();
            let membership = BackupMembershipRecord {
                lineage_id: lineage.into(),
                log_generation: 7,
                basis: 1,
                previous_hash: genesis_hash,
                content_hash: sha256(&content_payload),
                state_hash,
                eidx_frontier: content.eidx_frontier,
            };
            let membership_payload = encode_backup_membership(&membership).unwrap();
            let request = BackupRequestRecord {
                lineage_id: lineage.into(),
                log_generation: 7,
                basis: 1,
                transaction_hash: sha256(&membership_payload),
                previous_hash: genesis_hash,
                request_key_hash: request_key_hash(lineage, "receipt-request").unwrap(),
                digest: sha256(b"receipt-request-payload"),
                request_kind: 1,
                base_manifest_hash: None,
                receipt_tempids: transaction.tempids.clone(),
            };
            let request_payload = encode_request_record(&request).unwrap();
            let manifest = Manifest {
                version: VERSION,
                lineage_id: lineage.into(),
                log_generation: 7,
                basis: 1,
                genesis_hash,
                head_transaction_hash: sha256(&membership_payload),
                head_state_hash: state_hash,
                request_head_hash: sha256(&request_payload),
                completed_excision_head_hash: genesis_hash,
                transactions: Vec::new(),
                state_hashes: Vec::new(),
                requests: Vec::new(),
                programs: Vec::new(),
                tree: None,
                read_log_index: None,
            };
            let directory = temporary_directory("allocation-receipt-versions");
            let guard = prepare_directory(&directory).unwrap();
            ensure_claim(
                &directory,
                &BackupClaim {
                    lineage_id: lineage.into(),
                    genesis_hash,
                },
            )
            .unwrap();
            let mut publisher = ObjectPublisher::new(&directory, BackupFault::None);
            for (hash, bytes) in [
                (genesis_hash, &genesis),
                (membership.content_hash, &content_payload),
                (manifest.head_transaction_hash, &membership_payload),
                (manifest.request_head_hash, &request_payload),
            ] {
                publisher.publish(hash, bytes).unwrap();
            }
            publish_exact(
                &snapshot_path(&directory, 1, 7),
                &encode_manifest(&manifest).unwrap(),
                PublishFault::None,
            )
            .unwrap();
            let verified = PortableBackup::verify_backup_point(&directory, 1, 7, true).unwrap();
            assert!(verified.database.same_information_as(&report.db_after));
            let log = load_backup_log(&directory, &manifest).unwrap();
            let prepared = prepare_restore_rows(&directory, &manifest, &log).unwrap();
            assert_eq!(prepared.rows[0].content_payload, content_payload);
            assert_eq!(
                encode_request_record(&log.entries[0].request).unwrap(),
                request_payload
            );
            assert_eq!(log.entries[0].reserved_frontier, content.reserved_frontier);
            let mut invalid = request.clone();
            invalid
                .receipt_tempids
                .insert("unwitnessed-new".into(), before.eidx_frontier());
            assert_eq!(
                validate_backup_receipt(
                    &log.entries[0].transaction,
                    &invalid,
                    before.eidx_frontier(),
                    log.entries[0].reserved_frontier,
                )
                .unwrap_err()
                .code,
                "backup/request-chain"
            );
            invalid = request;
            invalid
                .receipt_tempids
                .insert("future-transaction".into(), crate::t_to_tx(2).unwrap());
            assert_eq!(
                validate_backup_receipt(
                    &log.entries[0].transaction,
                    &invalid,
                    before.eidx_frontier(),
                    log.entries[0].reserved_frontier,
                )
                .unwrap_err()
                .code,
                "backup/request-chain"
            );
            drop(publisher);
            drop(guard);
            fs::remove_dir_all(directory).unwrap();
        }
    }

    #[test]
    fn legacy_receipt_checkpoint_admission_bounds_count_bytes_and_indivisible_transactions() {
        let entry = |basis_t, datoms: Vec<crate::Datom>| LoadedBackupEntry {
            transaction_hash: [0; 32],
            content_hash: None,
            reserved_frontier: None,
            transaction: DurableTransaction {
                database_id: "fixture".into(),
                basis_t,
                previous_hash: [0; 32],
                eidx_frontier: 1_001,
                tempids: BTreeMap::new(),
                tx_data: datoms,
            },
            request: BackupRequestRecord {
                lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
                log_generation: 0,
                basis: basis_t,
                transaction_hash: [0; 32],
                previous_hash: [0; 32],
                request_key_hash: [0; 32],
                digest: [0; 32],
                request_kind: 1,
                base_manifest_hash: None,
                receipt_tempids: BTreeMap::new(),
            },
            legacy_state_hash: None,
        };
        let mut log = LoadedBackupLog {
            entries: (1..=517).map(|basis| entry(basis, Vec::new())).collect(),
            objects_read: 0,
        };
        assert_eq!(
            legacy_receipt_checkpoints(&log).unwrap(),
            BTreeSet::from([0, 256, 512, 516])
        );
        let datom = crate::Datom {
            entity: 1_000,
            attribute: 1_000,
            value: crate::Value::Long(1),
            tx: crate::t_to_tx(2).unwrap(),
            added: true,
        };
        log.entries = vec![
            entry(1, Vec::new()),
            entry(
                2,
                (0..=LEGACY_RECEIPT_TAIL_DATOMS)
                    .map(|index| crate::Datom {
                        value: crate::Value::Long(index as i64),
                        ..datom.clone()
                    })
                    .collect(),
            ),
            entry(3, Vec::new()),
            entry(4, Vec::new()),
        ];
        assert_eq!(
            legacy_receipt_checkpoints(&log).unwrap(),
            BTreeSet::from([0, 1, 2, 3])
        );
        log.entries[1].transaction.tx_data = vec![crate::Datom {
            entity: 1_000,
            attribute: 1_000,
            value: crate::Value::String("x".repeat(LEGACY_RECEIPT_TAIL_BYTES as usize)),
            tx: crate::t_to_tx(2).unwrap(),
            added: true,
        }];
        assert_eq!(
            legacy_receipt_checkpoints(&log).unwrap(),
            BTreeSet::from([0, 1, 2, 3])
        );
        for entry in &mut log.entries {
            entry.request.request_kind = 0;
        }
        assert!(legacy_receipt_checkpoints(&log).unwrap().is_empty());
    }

    #[test]
    fn portable_legacy_normalization_preserves_state_and_deep_frontier_validation() {
        use crate::{DB_IDENT, EntityRef, Keyword, Schema, TxOp, Value};

        let lineage = "12345678-1234-4abc-8def-123456789abc";
        let before = Database::new(Schema::new()).unwrap();
        let report = before
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("private-caller-spelling".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("fixture", "legacy")).into(),
                }],
                1000,
            )
            .unwrap();
        let genesis = encode_genesis(before.genesis_datoms()).unwrap();
        let genesis_hash = sha256(&genesis);
        let state_hash = checkpoint_state_hash(&report.db_after).unwrap();

        for extra_frontier in [0, 1] {
            let source = DurableTransaction {
                database_id: "mutable-legacy-alias".into(),
                basis_t: report.db_after.basis_t(),
                previous_hash: genesis_hash,
                eidx_frontier: report.db_after.eidx_frontier() + extra_frontier,
                tempids: report.tempids.clone(),
                tx_data: report.tx_data.clone(),
            };
            let source = decode_transaction(&crate::encode_transaction(&source).unwrap()).unwrap();
            // This is the same local normalization used by generation-zero
            // capture. It authenticates shape without replaying db-before.
            let content = LineageTransactionContent::from_transaction(
                lineage,
                before.eidx_frontier(),
                &source,
            )
            .unwrap();
            let content_payload = content.encode().unwrap();
            for private in ["mutable-legacy-alias", "private-caller-spelling"] {
                assert!(
                    !content_payload
                        .windows(private.len())
                        .any(|bytes| bytes == private.as_bytes())
                );
            }
            let membership = BackupMembershipRecord {
                lineage_id: lineage.into(),
                log_generation: 0,
                basis: source.basis_t,
                previous_hash: genesis_hash,
                content_hash: sha256(&content_payload),
                state_hash,
                eidx_frontier: content.eidx_frontier,
            };
            let membership_payload = encode_backup_membership(&membership).unwrap();
            let membership_hash = sha256(&membership_payload);
            let request = BackupRequestRecord {
                lineage_id: lineage.into(),
                log_generation: 0,
                basis: source.basis_t,
                transaction_hash: membership_hash,
                previous_hash: genesis_hash,
                request_key_hash: request_key_hash(lineage, "legacy-request").unwrap(),
                digest: sha256(b"legacy-request-digest"),
                request_kind: 1,
                base_manifest_hash: None,
                receipt_tempids: source.tempids,
            };
            let request_payload = encode_request_record(&request).unwrap();
            let manifest = Manifest {
                version: VERSION,
                lineage_id: lineage.into(),
                log_generation: 0,
                basis: source.basis_t,
                genesis_hash,
                head_transaction_hash: membership_hash,
                head_state_hash: state_hash,
                request_head_hash: sha256(&request_payload),
                completed_excision_head_hash: genesis_hash,
                transactions: Vec::new(),
                state_hashes: Vec::new(),
                requests: Vec::new(),
                programs: Vec::new(),
                tree: None,
                read_log_index: None,
            };
            let directory = temporary_directory("legacy-copy-frontier");
            let _guard = prepare_directory(&directory).unwrap();
            ensure_claim(
                &directory,
                &BackupClaim {
                    lineage_id: lineage.into(),
                    genesis_hash,
                },
            )
            .unwrap();
            let mut publisher = ObjectPublisher::new(&directory, BackupFault::None);
            publisher.publish(genesis_hash, &genesis).unwrap();
            publisher
                .publish(membership.content_hash, &content_payload)
                .unwrap();
            publisher
                .publish(membership_hash, &membership_payload)
                .unwrap();
            publisher
                .publish(manifest.request_head_hash, &request_payload)
                .unwrap();
            publish_exact(
                &snapshot_path(&directory, manifest.basis, 0),
                &encode_manifest(&manifest).unwrap(),
                PublishFault::None,
            )
            .unwrap();
            PortableBackup::verify_backup_presence(&directory, manifest.basis).unwrap();
            let verified = PortableBackup::verify_backup(&directory, manifest.basis, true);
            if extra_frontier == 0 {
                assert_eq!(
                    checkpoint_state_hash(&verified.unwrap().database).unwrap(),
                    state_hash
                );
            } else {
                assert_eq!(
                    verified.unwrap_err().code,
                    "recovery/eidx-frontier-mismatch"
                );
            }
            fs::remove_dir_all(directory).unwrap();
        }
    }

    #[test]
    fn completed_excision_link_is_canonical_and_tamper_evident() {
        let record = BackupCompletedExcision {
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            request_t: 9,
            request_entity: 12_345,
            previous_hash: sha256(b"prior completion"),
        };
        let encoded = encode_completed_excision(&record).unwrap();
        assert_eq!(decode_completed_excision(&encoded).unwrap(), record);
        let mut damaged = encoded;
        damaged[70] ^= 1;
        assert_eq!(
            decode_completed_excision(&damaged).unwrap_err().code,
            "backup/completed-excision-checksum"
        );
    }

    #[test]
    fn v3_manifest_remains_readable_without_becoming_the_write_format() {
        let genesis_hash = sha256(b"legacy genesis");
        let transaction_hash = sha256(b"legacy transaction");
        let state_hash = sha256(b"legacy state");
        let legacy = Manifest {
            version: LEGACY_VERSION,
            lineage_id: "12345678-1234-4abc-8def-123456789abc".into(),
            log_generation: 0,
            basis: 1,
            genesis_hash,
            head_transaction_hash: transaction_hash,
            head_state_hash: state_hash,
            request_head_hash: genesis_hash,
            completed_excision_head_hash: genesis_hash,
            transactions: vec![transaction_hash],
            state_hashes: vec![state_hash],
            requests: vec![RequestRow {
                key: "legacy-request".into(),
                digest: sha256(b"legacy request digest"),
                basis: 1,
                tx_hash: transaction_hash,
            }],
            programs: Vec::new(),
            tree: None,
            read_log_index: None,
        };
        let encoded = encode_manifest(&legacy).unwrap();
        assert_eq!(u16::from_be_bytes([encoded[4], encoded[5]]), LEGACY_VERSION);
        assert_eq!(decode_manifest(&encoded).unwrap(), legacy);
    }

    #[cfg(unix)]
    #[test]
    fn repository_root_must_be_a_private_real_directory() {
        use std::os::unix::fs::{PermissionsExt as _, symlink};

        for mode in [0o777, 0o755, 0o744] {
            let accessible = temporary_directory("group-or-world-accessible");
            fs::set_permissions(&accessible, fs::Permissions::from_mode(mode)).unwrap();
            let error = prepare_directory(&accessible).unwrap_err();
            assert_eq!(
                (error.category, error.code),
                (ErrorCategory::Forbidden, "backup/directory-permissions")
            );
            fs::set_permissions(&accessible, fs::Permissions::from_mode(0o700)).unwrap();
            fs::remove_dir_all(accessible).unwrap();
        }

        let real = temporary_directory("real-root");
        let link = real.with_extension("symlink");
        symlink(&real, &link).unwrap();
        let error = prepare_directory(&link).unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (ErrorCategory::Forbidden, "backup/directory")
        );
        fs::remove_file(link).unwrap();
        fs::remove_dir_all(real).unwrap();
    }

    #[cfg(unix)]
    #[test]
    fn stale_temp_cleanup_never_follows_or_removes_matching_symlinks() {
        use std::os::unix::fs::symlink;

        let directory = temporary_directory("stale-symlink");
        let guard = prepare_directory(&directory).unwrap();
        let target = directory.join("operator-file");
        fs::write(&target, b"must survive").unwrap();
        let link = objects(&directory).join(".atomic-tmp-1234-0000000000000001");
        symlink(&target, &link).unwrap();
        drop(guard);

        let _retry_guard = prepare_directory(&directory).unwrap();
        assert!(
            fs::symlink_metadata(&link)
                .unwrap()
                .file_type()
                .is_symlink()
        );
        assert_eq!(fs::read(&target).unwrap(), b"must survive");
        fs::remove_dir_all(directory).unwrap();
    }
}
