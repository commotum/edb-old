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
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Database, Digest, DurableTransaction, ErrorCategory, PostgresConnectionConfig, PostgresStore,
    PostgresTreeStore, Program, SemanticError, TreeManifestRecord, TreePublicationDelta,
    TreeRootBinding, decode_genesis, decode_program, decode_transaction, encode_genesis, sha256,
    transaction_hash,
};
use crate::{PersistentTreeManifest, persistent_tree};
use postgres::{Client, IsolationLevel};
use sha2::{Digest as _, Sha256};
use std::collections::{BTreeMap, BTreeSet};
use std::fs::{self, File, OpenOptions};
use std::io::Write;
use std::path::{Path, PathBuf};
use std::sync::atomic::{AtomicU64, Ordering};

const MAGIC: &[u8; 4] = b"ATBK";
const VERSION: u16 = 4;
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
static TEMP_SEQUENCE: AtomicU64 = AtomicU64::new(0);

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
    transaction: DurableTransaction,
    request: BackupRequestRecord,
    legacy_state_hash: Option<Digest>,
}

#[derive(Clone, Debug)]
struct LoadedBackupLog {
    entries: Vec<LoadedBackupEntry>,
    objects_read: usize,
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
}

pub struct PortableBackup {
    connection: PostgresConnectionConfig,
    client: Client,
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
        for attempt in 0..MAX_BACKUP_ATTEMPTS {
            if attempt > 0 {
                match self.connection.connect() {
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
        self.backup_database_once_with_pin_probe(database_id, directory, BackupFault::None, probe)
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
        let mut reconstructed = if parent.is_none() {
            for datom in &decoded_genesis {
                collect_function_hashes(&datom.value, &mut temporal_programs);
            }
            Some(genesis_database)
        } else {
            None
        };
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
            &mut reconstructed,
            &mut temporal_programs,
            &mut publisher,
        )?;
        if head_hash != captured.source_head_hash {
            return Err(fault(
                "backup/head-mismatch",
                "head does not match snapshot chain",
            ));
        }
        if reconstructed
            .as_ref()
            .is_some_and(|database| database.basis_t() != basis)
        {
            return Err(fault(
                "backup/basis-mismatch",
                "semantically reconstructed snapshot does not reach its observed head",
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
        };

        // A backup point is a logical (lineage, t), not whichever replaceable
        // physical index revision happened to be current during this retry.
        // Goal 13 deliberately permits a repaired tree to be republished at
        // the same logical basis. If a root already exists, authenticate it
        // and prove the live snapshot has the same transaction information
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
        manifest.tree = tree;
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
            // that race by the same semantic proof used by an ordinary
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
            let bytes = fs::read(entry.path()).map_err(io_error("backup/list-read"))?;
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
    }

    fn verify_loaded_backup(
        directory: &Path,
        manifest: Manifest,
        manifest_hash: Digest,
        deep: bool,
    ) -> Result<BackupVerification, SemanticError> {
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
        let mut previous = manifest.genesis_hash;
        let mut current_state_hash = checkpoint_state_hash(&database)?;
        let log = load_backup_log(directory, &manifest)?;
        let mut objects_read = 1 + log.objects_read;
        let mut pending_avet_by_basis = BTreeMap::<u64, Vec<Vec<crate::AvetProjectionWork>>>::new();
        if deep {
            let mut trees = request_base_trees(&log);
            if let Some(tree) = &manifest.tree {
                trees.push(tree.clone());
            }
            for tree in trees {
                let decoded = decode_bound_tree_manifest(directory, &manifest, &log, &tree, None)?;
                pending_avet_by_basis
                    .entry(decoded.basis_t)
                    .or_default()
                    .push(decoded.pending_avet);
            }
        }
        if let Some(work_sets) = pending_avet_by_basis.remove(&0) {
            for pending in work_sets {
                crate::peer::validate_avet_work_directions(&pending, database.schema())?;
            }
        }
        let tree_basis = if deep {
            manifest
                .tree
                .as_ref()
                .map(|tree| {
                    PersistentTreeManifest::decode(&read_object(directory, tree.manifest_hash)?)
                        .map(|tree| tree.basis_t)
                })
                .transpose()?
        } else {
            None
        };
        let mut tree_state_hash = None;
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
            database = if manifest.version == VERSION && manifest.log_generation > 0 {
                match entry.request.request_kind {
                    0 => database.apply_excised_committed(&entry.transaction)?,
                    1 | 2 => database.apply_committed(&entry.transaction)?,
                    _ => {
                        return Err(fault(
                            "backup/request-kind",
                            "backup transaction has an invalid request kind",
                        ));
                    }
                }
            } else {
                database.apply_committed(&entry.transaction)?
            };
            current_state_hash = checkpoint_state_hash(&database)?;
            if let Some(work_sets) = pending_avet_by_basis.remove(&entry.transaction.basis_t) {
                for pending in work_sets {
                    crate::peer::validate_avet_work_directions(&pending, database.schema())?;
                }
            }
            if tree_basis == Some(entry.transaction.basis_t) {
                tree_state_hash = Some(current_state_hash);
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
        if !pending_avet_by_basis.is_empty() {
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
        if let Some(tree) = &manifest.tree {
            if deep {
                objects_read +=
                    verify_tree_backup(directory, &manifest, &log, tree, tree_state_hash)?;
            } else {
                verify_tree_presence(directory, &manifest, &log, tree)?;
            }
        }
        for tree in request_base_trees(&log) {
            if deep {
                objects_read += verify_tree_backup(directory, &manifest, &log, &tree, None)?;
            } else {
                verify_tree_presence(directory, &manifest, &log, &tree)?;
            }
        }
        Ok(BackupVerification {
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
        self.restore_backup_selected(directory, basis, None, target_database_id, fault_at, None)
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
        )
    }

    fn restore_backup_selected(
        &mut self,
        directory: &Path,
        basis: u64,
        log_generation: Option<u64>,
        target_database_id: &str,
        fault_at: RestoreFault,
        activation_probe: Option<&mut dyn FnMut()>,
    ) -> Result<Database, SemanticError> {
        if target_database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "backup/empty-target",
                "restore target cannot be empty",
            ));
        }
        let verification = match log_generation {
            Some(generation) => Self::verify_backup_point(directory, basis, generation, true)?,
            None => Self::verify_backup(directory, basis, true)?,
        };
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
        ensure_restore_target(
            &mut self.client,
            &manifest,
            target_database_id,
            &genesis,
            genesis_hash,
        )?;
        if target_matches_backup(
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
        if fault_at == RestoreFault::AfterCommitBeforeResponse {
            return Err(injected("backup/restore-after-activation"));
        }
        if !target_matches_backup(
            &mut self.client,
            directory,
            &manifest,
            &log,
            &completed_excisions,
            target_database_id,
            &verification.database,
        )? {
            return Err(fault(
                "backup/restore-postcondition",
                "restored authoritative rows do not exactly match the requested backup point",
            ));
        }
        complete_restored_target(
            &self.connection,
            directory,
            &manifest,
            &log,
            target_database_id,
            &verification.database,
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
            "SELECT EXISTS (SELECT 1 FROM atomic_heads \
                              WHERE database_id = $1 AND log_generation = $2)",
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
) -> Result<(), SemanticError> {
    let mut transaction = client
        .transaction()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-catalog-begin", error))?;
    transaction
        .query_one(
            "SELECT pg_advisory_xact_lock(hashtextextended('atomic/restore/' || $1, 0))",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-lock", error))?;
    if let Some(target) = transaction
        .query_opt(
            "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases \
             WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-target", error))?
    {
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
        transaction.commit().map_err(|error| {
            crate::postgres::postgres_error("backup/restore-catalog-commit", error)
        })?;
        return Ok(());
    }

    transaction
        .execute(
            "INSERT INTO atomic_databases (database_id, lineage_id, genesis, genesis_hash) \
             VALUES ($1, $2, $3, $4)",
            &[
                &target_database_id,
                &manifest.lineage_id,
                &genesis,
                &&manifest.genesis_hash[..],
            ],
        )
        .map_err(restore_catalog_error)?;
    // Do not publish a synthetic genesis head. Until the archive has been
    // completely staged and its first head is installed atomically, runtime
    // opens see no database value and therefore fail closed.
    transaction
        .commit()
        .map_err(|error| crate::postgres::postgres_error("backup/restore-catalog-commit", error))
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
        let expected_content = LineageTransactionContent::from_transaction(
            &manifest.lineage_id,
            prior_frontier,
            &entry.transaction,
        )?;
        if content != expected_content {
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

fn install_restored_content<C: postgres::GenericClient>(
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
        || stored.get::<_, i16>(3) != 1
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

fn stage_restore_generation(
    client: &mut Client,
    manifest: &Manifest,
    prepared: &PreparedRestore,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    candidate: RestoreCandidate,
    fault_at: RestoreFault,
) -> Result<RestoredGenerationHead, SemanticError> {
    let generation_sql = sql_u64(candidate.generation, "restored log generation")?;
    let mut previous = manifest.genesis_hash;
    let mut state_hash = prepared.genesis_state_hash;
    let mut frontier = prepared.genesis_frontier;
    for chunk in prepared.rows.chunks(RESTORE_BATCH_ROWS) {
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
        .any(|row| row.request.base_manifest_hash.is_some())
    {
        return Ok(());
    }
    let genesis = read_object(directory, manifest.genesis_hash)?;
    let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
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
            let next_database = match row.request.request_kind {
                0 => database.apply_excised_committed(&durable)?,
                1 | 2 => database.apply_committed(&durable)?,
                _ => {
                    return Err(fault(
                        "backup/restore-request-kind",
                        "restored semantic coordinate has an invalid request kind",
                    ));
                }
            };
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

fn install_restored_program<C: postgres::GenericClient>(
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
fn capture_log_tail<C: postgres::GenericClient>(
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
    reconstructed: &mut Option<Database>,
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
            if let Some(database) = reconstructed.take() {
                let database = database.apply_committed(&portable)?;
                if checkpoint_state_hash(&database)? != state_hash {
                    return Err(fault(
                        "backup/state-commitment",
                        format!("legacy transaction {basis} has an invalid state commitment"),
                    ));
                }
                *reconstructed = Some(database);
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
                || content_version != 1
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
            if let Some(database) = reconstructed.take() {
                let database = if request_kind_i16 == 0 {
                    database.apply_excised_committed(&transaction)?
                } else {
                    database.apply_committed(&transaction)?
                };
                if checkpoint_state_hash(&database)? != state_hash {
                    return Err(fault(
                        "backup/state-commitment",
                        format!("generation transaction {basis} has an invalid state commitment"),
                    ));
                }
                *reconstructed = Some(database);
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

fn capture_completed_excisions<C: postgres::GenericClient>(
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

fn complete_restored_target(
    connection: &PostgresConnectionConfig,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    target_database_id: &str,
    expected: &Database,
) -> Result<Database, SemanticError> {
    restore_tree_backup(connection, directory, manifest, log, target_database_id)?;
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
    manifest: &Manifest,
    _log: &LoadedBackupLog,
    target_database_id: &str,
) -> Result<(), SemanticError> {
    let Some(tree) = &manifest.tree else {
        // The authoritative log is complete. A target without a captured
        // physical root deliberately recovers from that log and may be
        // consolidated normally after restore.
        return Ok(());
    };
    let source_payload = read_object(directory, tree.manifest_hash)?;
    let source = PersistentTreeManifest::decode(&source_payload)?;

    let mut client = connection.connect()?;
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
    if target_generation == 0 || target_lineage != manifest.lineage_id {
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
                "SELECT tx_hash, state_hash FROM atomic_generation_transactions \
                 WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                &[&target_database_id, &generation_sql, &basis_sql],
            )
            .map_err(|error| crate::postgres::postgres_error("backup/restore-tree-log", error))?;
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
    let recovered_target = recover_generation_to(
        &mut client,
        target_database_id,
        target_generation,
        target.basis_t,
        target.tx_hash,
    )?;
    if recovered_target.final_hash != target.tx_hash
        || recovered_target.database.eidx_frontier() != target.eidx_frontier
        || checkpoint_state_hash(&recovered_target.database)? != target.state_hash
    {
        return Err(fault(
            "backup/restore-tree-reconstruction",
            "target generation log does not reproduce the restored tree coordinate",
        ));
    }
    crate::peer::validate_avet_work_directions(
        &target.pending_avet,
        recovered_target.database.schema(),
    )?;
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
            crate::peer::validate_avet_work_directions(
                &existing.pending_avet,
                recovered_target.database.schema(),
            )?;
            // Readiness is monotone at one immutable database coordinate.
            // A complete existing accelerator always dominates. Two partial
            // accelerators have no useful total order (clearing has no
            // ordinal), so preserve the valid existing value and resume it.
            // Only a complete backup source may replace a partial existing
            // value at the next physical revision.
            if existing.pending_avet.is_empty() || !target.pending_avet.is_empty() {
                return Ok(());
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
        for hash in &reachable {
            let payload = read_object(directory, *hash)?;
            store.insert_node(*hash, &payload)?;
        }
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
        (Ok(()), Ok(())) => Ok(()),
    }
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
            let payload = read_object(directory, *hash)?;
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
        for hash in &reachable {
            store.insert_node(*hash, &read_object(directory, *hash)?)?;
        }
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

fn capture_tree_backup<C: postgres::GenericClient>(
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

fn capture_tree_candidate<C: postgres::GenericClient>(
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
fn capture_bound_request_tree<C: postgres::GenericClient>(
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

fn reconstruct_bound_request_tree<C: postgres::GenericClient>(
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
                || request
                    .receipt_tempids
                    .values()
                    .any(|entity| !content.allocations.contains(entity))
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
        Ok(LoadedBackupLog {
            objects_read: entries.len().saturating_mul(3),
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
    expected_state_hash: Option<Digest>,
) -> Result<usize, SemanticError> {
    let manifest = decode_bound_tree_manifest(directory, backup, log, tree, expected_state_hash)?;
    let mut legacy_reachable = tree.legacy_node_hashes.as_ref().map(|_| BTreeSet::new());
    let mut objects_read: usize = 1;
    for tree_root in &manifest.trees {
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
        objects_read = objects_read.saturating_add(validated.nodes_read as usize);
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
    Ok(objects_read)
}

/// Prove that every restored kind-2 receipt names the deterministic archive
/// rebuilt from its portable base, and that no extra archive/binding entered
/// the active generation. This is part of ambiguous-acknowledgement replay:
/// merely matching the log head is insufficient if its db-before values can
/// no longer be reopened exactly.
fn match_restored_request_base_archives<C: postgres::GenericClient>(
    client: &mut C,
    directory: &Path,
    backup: &Manifest,
    log: &LoadedBackupLog,
    target_database_id: &str,
    target_generation: u64,
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
        != portable_hashes.len() as u64
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

fn target_matches_backup<C: postgres::GenericClient>(
    client: &mut C,
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
    completed_excisions: &[(u64, u64)],
    target_database_id: &str,
    expected_database: &Database,
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
    let Some(head) = client
        .query_opt(
            "SELECT basis_t, tx_hash, log_generation FROM atomic_heads WHERE database_id = $1",
            &[&target_database_id],
        )
        .map_err(|error| crate::postgres::postgres_error("backup/restore-check-head", error))?
    else {
        return Ok(false);
    };
    let target_basis = unsigned(head.get(0), "restored head basis")?;
    let target_head_hash = digest(head.get(1), "restored head hash")?;
    let target_generation = unsigned(head.get(2), "restored log generation")?;
    if target_generation == 0 || target_basis != manifest.basis {
        return Ok(false);
    }
    let recovered = crate::postgres::recover_generation_to(
        client,
        target_database_id,
        target_generation,
        target_basis,
        target_head_hash,
    )?;
    if !recovered.database.same_information_as(expected_database) {
        return Ok(false);
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
        return Ok(false);
    }
    let restored_request_bases = match_restored_request_base_archives(
        client,
        directory,
        manifest,
        log,
        target_database_id,
        target_generation,
    )?;
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
            return Ok(false);
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
        let content = LineageTransactionContent::decode(&content_payload)?;
        let expected_content = LineageTransactionContent::from_transaction(
            &manifest.lineage_id,
            prior_frontier,
            &entry.transaction,
        )?;
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
            || content_version != 1
            || content != expected_content
            || request_key_hash != entry.request.request_key_hash
            || request_digest != expected_request_digest
            || request_kind != i16::from(entry.request.request_kind)
            || request_tx_hash != row_hash
            || request_base_hash
                != entry
                    .request
                    .base_manifest_hash
                    .and_then(|portable| restored_request_bases.get(&portable).copied())
            || receipt != entry.request.receipt_tempids
        {
            return Ok(false);
        }
        previous = row_hash;
        prior_frontier = row_frontier;
    }
    if !receipts.is_empty() || target_head_hash != previous {
        return Ok(false);
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
        return Ok(false);
    }
    if restored_completed != completed_excisions {
        return Ok(false);
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
            return Ok(false);
        };
        if row.get::<_, i16>(0) != kind
            || row.get::<_, i16>(1) != arity
            || row.get::<_, Vec<u8>>(2) != payload
        {
            return Ok(false);
        }
    }
    Ok(true)
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
}

impl<'a> ObjectPublisher<'a> {
    fn new(directory: &'a Path, fault_at: BackupFault) -> Self {
        Self {
            directory,
            fault_at,
            seen: BTreeSet::new(),
            written: 0,
            reused: 0,
        }
    }

    fn publish(&mut self, hash: Digest, bytes: &[u8]) -> Result<(), SemanticError> {
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
    let path = objects(directory).join(hex(&hash));
    require_regular_file(&path, "backup/object-type")?;
    let bytes = fs::read(path).map_err(io_error("backup/object-read"))?;
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
    let bytes = fs::read(path).map_err(io_error("backup/manifest-read"))?;
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
fn authenticate_source_parent<C: postgres::GenericClient>(
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
            || row.get::<_, i16>(9) != 1
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

    let verified = PortableBackup::verify_backup_point(
        directory,
        candidate.basis,
        candidate.log_generation,
        true,
    )?;
    let (existing, manifest_hash) =
        load_manifest_generation(directory, candidate.basis, candidate.log_generation)?;
    if manifest_hash != verified.point.manifest_hash {
        return Err(fault(
            "backup/root-changed",
            "published backup root changed while it was being verified",
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
    let bytes = fs::read(path).map_err(io_error("backup/claim-read"))?;
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
    fn v4_manifest_is_a_constant_size_endpoint_root() {
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
        };
        let encoded = encode_manifest(&manifest).unwrap();
        assert_eq!(encoded.len(), 295);
        assert_eq!(decode_manifest(&encoded).unwrap(), manifest);

        let mut no_tree = manifest;
        no_tree.basis = 1;
        no_tree.tree = None;
        let encoded = encode_manifest(&no_tree).unwrap();
        assert_eq!(encoded.len(), 263);
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
