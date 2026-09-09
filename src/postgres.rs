use crate::database_value::{TransactionReadContext, TransactionReadWork};
use crate::log_generation::{
    LineageTransactionContent, generation_transaction_hash, request_key_hash,
};
use crate::peer::{
    ExactEndpoint, ExactOpenStats, TieredReadHandle, TieredSnapshot, WeakTieredReadHandle,
};
use crate::persistent_commitment::{
    PersistentCommitmentCoordinate, advance_persistent_commitment, exact_semantic_changes,
    load_persistent_coordinate, record_persistent_coordinate,
};
use crate::persistent_tree::{TreeNode, decode_tree_node};
use crate::program::ValidatedProgram;
use crate::recent::RecentLimits;
use crate::state_commitment::CommitmentWork;
use crate::state_commitment::{checkpoint_state_hash, verify_checkpoint_state_hash};
use crate::tiered_assessor::{AssessmentLimits, assess_tiered_with_remaining_limits};
use crate::{
    Database, DatabaseValue, Datom, Digest, DurableTransaction, ErrorCategory,
    PostgresConnectionConfig, Program, ProgramBudget, ProgramHash, ProgramKind, ProgramLimits,
    Schema, SemanticError, TxForm, TxFunctions, TxOp, Value, decode_genesis, decode_program,
    decode_transaction, encode_genesis, encode_program, encode_transaction, request_digest, sha256,
    transaction_hash,
};
use postgres::fallible_iterator::FallibleIterator;
use postgres::{Client, GenericClient, IsolationLevel};
use std::collections::{BTreeMap, BTreeSet, VecDeque};
use std::sync::{Arc, Mutex};

#[path = "program_bindings.rs"]
pub(crate) mod program_bindings;
use program_bindings::{
    SharedProgramBudget, expand_submission_forms, persisted_predicates, transaction_program_roots,
    validate_successor_program_bindings, visit_program_closure,
};

pub(crate) type SharedProgramCache = Arc<Mutex<ProgramCache>>;

const DEFAULT_PROGRAM_CACHE_ENTRIES: usize = 64;
const DEFAULT_PROGRAM_CACHE_BYTES: usize = 64 * 1024 * 1024;
// Recovered `datomic.function.Function.memory-size*` charges 1 KiB plus code,
// while the enclosing domain cache also charges its key. Canonical payload
// length is our stable native proxy for source/IR memory.
const PROGRAM_CACHE_ENTRY_OVERHEAD: usize = 1024 + std::mem::size_of::<ProgramHash>();

fn program_cache_weight(payload_len: usize) -> usize {
    payload_len.saturating_add(PROGRAM_CACHE_ENTRY_OVERHEAD)
}

/// Cumulative resolver work and the current bounded cache footprint.
///
/// `decodes` counts successful canonical payload decodes. Deploying a program
/// can seed the cache directly and therefore does not increment it.
/// `validations` counts canonical validation boundaries crossed before cache
/// admission; neither counter changes on a cache hit.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct ProgramCacheStats {
    pub hits: u64,
    pub misses: u64,
    pub decodes: u64,
    pub validations: u64,
    pub evictions: u64,
    pub current_entries: usize,
    pub current_bytes: usize,
}

#[derive(Clone)]
struct CachedProgram {
    program: Arc<ValidatedProgram>,
    /// Canonical payload plus the recovered fixed function/key allowance.
    weight: usize,
}

pub(crate) struct ProgramCache {
    entries: BTreeMap<ProgramHash, CachedProgram>,
    lru: VecDeque<ProgramHash>,
    max_entries: usize,
    max_bytes: usize,
    current_bytes: usize,
    hits: u64,
    misses: u64,
    decodes: u64,
    validations: u64,
    evictions: u64,
}

impl Default for ProgramCache {
    fn default() -> Self {
        Self {
            entries: BTreeMap::new(),
            lru: VecDeque::new(),
            max_entries: DEFAULT_PROGRAM_CACHE_ENTRIES,
            max_bytes: DEFAULT_PROGRAM_CACHE_BYTES,
            current_bytes: 0,
            hits: 0,
            misses: 0,
            decodes: 0,
            validations: 0,
            evictions: 0,
        }
    }
}

impl ProgramCache {
    fn get(&mut self, hash: ProgramHash) -> Option<Arc<ValidatedProgram>> {
        let program = self
            .entries
            .get(&hash)
            .map(|entry| Arc::clone(&entry.program));
        if program.is_some() {
            self.hits = self.hits.saturating_add(1);
            self.touch(hash);
        } else {
            self.misses = self.misses.saturating_add(1);
        }
        program
    }

    fn record_decode_and_validation(&mut self) {
        self.decodes = self.decodes.saturating_add(1);
        self.validations = self.validations.saturating_add(1);
    }

    fn record_validation(&mut self) {
        self.validations = self.validations.saturating_add(1);
    }

    fn insert(&mut self, hash: ProgramHash, program: Arc<ValidatedProgram>, weight: usize) {
        if self.max_entries == 0 || self.max_bytes == 0 || weight > self.max_bytes {
            return;
        }
        if let Some(previous) = self.entries.remove(&hash) {
            self.current_bytes = self.current_bytes.saturating_sub(previous.weight);
        }
        self.entries.insert(hash, CachedProgram { program, weight });
        self.current_bytes = self.current_bytes.saturating_add(weight);
        self.touch(hash);
        self.enforce_limits();
    }

    fn set_limits(&mut self, max_entries: usize, max_bytes: usize) {
        self.max_entries = max_entries;
        self.max_bytes = max_bytes;
        self.enforce_limits();
    }

    fn enforce_limits(&mut self) {
        while self.entries.len() > self.max_entries || self.current_bytes > self.max_bytes {
            let Some(hash) = self.lru.pop_front() else {
                break;
            };
            if let Some(entry) = self.entries.remove(&hash) {
                self.current_bytes = self.current_bytes.saturating_sub(entry.weight);
                self.evictions = self.evictions.saturating_add(1);
            }
        }
    }

    fn touch(&mut self, hash: ProgramHash) {
        self.lru.retain(|cached| cached != &hash);
        self.lru.push_back(hash);
    }

    fn stats(&self) -> ProgramCacheStats {
        ProgramCacheStats {
            hits: self.hits,
            misses: self.misses,
            decodes: self.decodes,
            validations: self.validations,
            evictions: self.evictions,
            current_entries: self.entries.len(),
            current_bytes: self.current_bytes,
        }
    }
}

fn lock_program_cache(cache: &SharedProgramCache) -> std::sync::MutexGuard<'_, ProgramCache> {
    // This cache is derived, replaceable state. Recovering the contents after
    // a panic is safer than turning a cache poison bit into database outage.
    cache
        .lock()
        .unwrap_or_else(std::sync::PoisonError::into_inner)
}

pub(crate) fn shared_program_cache_stats(cache: &SharedProgramCache) -> ProgramCacheStats {
    lock_program_cache(cache).stats()
}

pub(crate) const MIGRATIONS: &[(i64, &str)] = &[
    (1, include_str!("../migrations/0001_atomic.sql")),
    (2, include_str!("../migrations/0002_peer_indexes.sql")),
    (3, include_str!("../migrations/0003_programs.sql")),
    (4, include_str!("../migrations/0004_transactor_leases.sql")),
    (5, include_str!("../migrations/0005_operations.sql")),
    (6, include_str!("../migrations/0006_schema_information.sql")),
    (7, include_str!("../migrations/0007_entity_predicates.sql")),
    (8, include_str!("../migrations/0008_index_publications.sql")),
    (9, include_str!("../migrations/0009_state_commitments.sql")),
    (
        10,
        include_str!("../migrations/0010_program_immutability.sql"),
    ),
    (11, include_str!("../migrations/0011_persistent_trees.sql")),
    (
        12,
        include_str!("../migrations/0012_tree_publication_revisions.sql"),
    ),
    (
        13,
        include_str!("../migrations/0013_lineage_and_tree_gc.sql"),
    ),
    (14, include_str!("../migrations/0014_log_generations.sql")),
    (
        15,
        include_str!("../migrations/0015_persistent_semantic_commitments.sql"),
    ),
    (
        16,
        include_str!("../migrations/0016_activation_semantic_roots.sql"),
    ),
    (
        17,
        include_str!("../migrations/0017_request_snapshot_bases.sql"),
    ),
    (
        18,
        include_str!("../migrations/0018_semantic_commitment_gc.sql"),
    ),
    (19, include_str!("../migrations/0019_dual_predicates.sql")),
    (
        20,
        include_str!("../migrations/0020_request_base_archives.sql"),
    ),
    (
        21,
        include_str!("../migrations/0021_resumable_avet_projection.sql"),
    ),
    (
        22,
        include_str!("../migrations/0022_tree_retirement_intent_dependency.sql"),
    ),
    (
        23,
        include_str!("../migrations/0023_authenticated_index_basis.sql"),
    ),
    (
        24,
        include_str!("../migrations/0024_versioned_program_references.sql"),
    ),
    (
        25,
        include_str!("../migrations/0025_receipt_archive_conversion.sql"),
    ),
];

/// Latest PostgreSQL schema understood by this binary.
///
/// This is an operator compatibility boundary, not a data-format version.
pub const POSTGRES_SCHEMA_VERSION: i64 = 25;

/// Version of the authenticated fixed-dependency walker whose result GC may
/// trust. Any future traversal change that adds roots must bump this version
/// and install a corresponding fail-closed SQL migration before reuse.
pub(crate) const PROGRAM_REFERENCE_WALKER_VERSION: i64 = 1;

/// Oldest installed native SQL schema that this binary can upgrade in place
/// when the catalog already contains a logical database.
///
/// Versions 1--5 stored a prototype logical representation that cannot be
/// reinterpreted as the schema-information/log representation introduced by
/// migration 6.  Those databases require an offline export through an old
/// decoder followed by import into a freshly provisioned catalog. Empty
/// catalogs may still run through the complete migration chain.
pub const POSTGRES_IN_PLACE_UPGRADE_FLOOR: i64 = 6;

const PEER_RUNTIME_TABLES: &[&str] = &[
    "atomic_schema_migrations",
    "atomic_databases",
    "atomic_heads",
    "atomic_transactions",
    "atomic_requests",
    "atomic_index_segments",
    "atomic_index_manifests",
    "atomic_programs",
    "atomic_database_generations",
    "atomic_log_generations",
    "atomic_transaction_contents",
    "atomic_generation_transactions",
    "atomic_generation_requests",
    "atomic_generation_request_bases",
    "atomic_request_base_archives",
    "atomic_request_base_archive_roots",
    "atomic_request_base_archive_completions",
    "atomic_log_generation_activations",
    "atomic_completed_excision_requests",
    "atomic_log_generation_completions",
    "atomic_log_generation_retirements",
    "atomic_index_publications",
    "atomic_tree_nodes",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_tree_publications",
    "atomic_tree_publication_states",
    "atomic_tree_live_sets",
    "atomic_tree_live_nodes",
    "atomic_tree_retirements",
    "atomic_tree_retired_nodes",
    "atomic_tree_retirement_progress",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];

const WRITER_RUNTIME_TABLES: &[&str] = &[
    "atomic_transactor_leases",
    // The invoker tree-manifest validation trigger authenticates a positive
    // generation's endpoint against its immutable completion checkpoint.
    "atomic_log_generation_checkpoints",
    // The invoker semantic-root validation trigger distinguishes ordinary
    // publication from the two fixed-path generation-GC mutations.
    "atomic_log_generation_collection_progress",
    "atomic_log_generation_abandonment_progress",
    "atomic_tree_build_intents",
    "atomic_tree_build_intent_nodes",
    "atomic_tree_delta_headers",
    "atomic_tree_delta_nodes",
    "atomic_generation_request_tempids",
    "atomic_generation_request_bases",
    "atomic_program_generation_refs",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];

const WRITER_INSERT_TABLES: &[&str] = &[
    "atomic_transactions",
    "atomic_requests",
    "atomic_transactor_leases",
    "atomic_tree_nodes",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_programs",
    "atomic_tree_delta_headers",
    "atomic_tree_delta_nodes",
    "atomic_tree_build_intents",
    "atomic_tree_build_intent_nodes",
    "atomic_transaction_contents",
    "atomic_generation_transactions",
    "atomic_generation_requests",
    "atomic_generation_request_bases",
    "atomic_generation_request_tempids",
    "atomic_program_generation_refs",
    "atomic_semantic_commitment_nodes",
    "atomic_semantic_commitment_roots",
];

const WRITER_UPDATE_TABLES: &[&str] = &[
    "atomic_databases",
    "atomic_heads",
    "atomic_transactor_leases",
    "atomic_tree_build_intents",
    "atomic_tree_delta_headers",
];

/// Administrative PostgreSQL owner for schema installation and runtime-role
/// grants. Ordinary peers and transaction services never need this type or
/// its DDL authority.
pub struct PostgresMigrator {
    client: Client,
}

impl PostgresMigrator {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        let client = connection.connect_for("postgres/migration-connect")?;
        Ok(Self { client })
    }

    pub fn from_client(client: Client) -> Self {
        Self { client }
    }

    /// Install every known migration under one transaction-scoped advisory
    /// lock. An older binary refuses an already-installed newer migration.
    pub fn migrate(&mut self) -> Result<(), SemanticError> {
        apply_migrations(&mut self.client)
    }

    /// Grant the exact privileges used on Atomic relations by the native peer
    /// and fenced transaction service. Roles must be dedicated: they may not
    /// be elevated, inherit another role, own database objects, or retain a
    /// writable non-system schema that could shadow an unqualified relation.
    ///
    /// This boundary deliberately does not administer unrelated database-wide
    /// policy such as PostgreSQL's `TEMP` grant. Operators that require a role
    /// with no authority outside Atomic should use a dedicated database and
    /// harden its database ACL separately.
    pub fn grant_runtime_privileges(
        &mut self,
        writer_role: &str,
        peer_role: &str,
    ) -> Result<(), SemanticError> {
        grant_runtime_privileges(&mut self.client, writer_role, peer_role)
    }
}

fn apply_migrations(client: &mut Client) -> Result<(), SemanticError> {
    debug_assert_eq!(
        MIGRATIONS.last().map(|(version, _)| *version),
        Some(POSTGRES_SCHEMA_VERSION)
    );
    let mut transaction = client
        .transaction()
        .map_err(|error| postgres_error("postgres/migration-begin", error))?;
    let schema = pin_current_schema(&mut transaction)?;
    transaction
        .query_one("SELECT pg_advisory_xact_lock($1)", &[&0x41544f4d_i64])
        .map_err(|error| postgres_error("postgres/migration-lock", error))?;
    let migration_table_exists: bool = transaction
        .query_one(
            "SELECT to_regclass('atomic_schema_migrations') IS NOT NULL",
            &[],
        )
        .map_err(|error| postgres_error("postgres/migration-discovery", error))?
        .get(0);
    let installed = if migration_table_exists {
        read_migration_rows(&mut transaction)?
    } else {
        Vec::new()
    };
    validate_migration_rows(&installed, false)?;
    reject_unsupported_populated_upgrade(&mut transaction, &installed)?;
    let installed_version = installed.last().map_or(0, |(version, _)| *version);
    // An already-current catalog may still contain migration 9's historical
    // zero placeholders if it was upgraded by an older binary. Schema-version
    // equality is therefore not evidence that the data migration completed.
    if installed_version >= 9 && state_commitment_backfill_required(&mut transaction)? {
        backfill_state_commitments(&mut transaction).map_err(upgrade_rebuild_required)?;
    }

    for (version, sql) in MIGRATIONS.iter().skip(installed.len()) {
        // Migration 13 assigns the first durable lineage identity to every
        // pre-lineage catalog row.  `atomic_databases` has been immutable
        // since migration 1, so this one administrative data migration must
        // suspend that guard while it fills the new column.  Keep the
        // historical migration bytes unchanged: deployed catalogs authenticate
        // them by checksum, and ordinary runtime mutation remains forbidden.
        if *version == 13 {
            transaction
                .batch_execute(
                    "ALTER TABLE atomic_databases \
                     DISABLE TRIGGER atomic_databases_immutable",
                )
                .map_err(|error| postgres_error("postgres/migration-lineage-guard", error))?;
        }
        transaction
            .batch_execute(sql)
            .map_err(|error| postgres_error("postgres/migration-ddl", error))?;
        if *version == 13 {
            transaction
                .batch_execute(
                    "ALTER TABLE atomic_databases \
                     ENABLE TRIGGER atomic_databases_immutable",
                )
                .map_err(|error| postgres_error("postgres/migration-lineage-guard", error))?;
        }
        if *version == 9 {
            backfill_state_commitments(&mut transaction).map_err(upgrade_rebuild_required)?;
        }
        if matches!(*version, 15 | 16) {
            crate::persistent_commitment::backfill_terminal_persistent_commitments(
                &mut transaction,
            )?;
        }
        let checksum = sha256(sql.as_bytes());
        transaction
            .execute(
                "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
                &[version, &&checksum[..]],
            )
            .map_err(|error| postgres_error("postgres/migration-record", error))?;
    }
    // Migration is also the repair boundary for derived closure metadata, but
    // an already-current healthy catalog must be a read-only verification.
    // Unconditionally rebuilding these ledgers takes table/row locks in the
    // inverse direction of live generation and tree writers and made harmless
    // concurrent `migrate` calls capable of deadlocking runtime work.
    if POSTGRES_SCHEMA_VERSION >= 13 && tree_live_set_backfill_required(&mut transaction)? {
        backfill_tree_live_sets(&mut transaction)?;
    }
    if POSTGRES_SCHEMA_VERSION >= 14 && program_generation_ref_backfill_required(&mut transaction)?
    {
        backfill_program_generation_refs(&mut transaction)?;
    }
    repair_atomic_routine_paths(&mut transaction, &schema)?;
    transaction
        .commit()
        .map_err(|error| postgres_error("postgres/migration-commit", error))
}

/// True only when no live publication fold already owns the missing/current
/// tree membership and the durable derived marker needs administrative repair.
fn tree_live_set_backfill_required<C: GenericClient>(
    client: &mut C,
) -> Result<bool, SemanticError> {
    client
        .query_one(
            "SELECT EXISTS ( \
                 WITH latest AS ( \
                     SELECT DISTINCT ON (database_id) database_id, manifest_hash \
                       FROM atomic_tree_publications \
                      ORDER BY database_id, publication_revision DESC \
                 ) \
                 SELECT 1 FROM latest p \
                 LEFT JOIN atomic_tree_live_sets l USING (database_id) \
                  WHERE (l.database_id IS NULL \
                         OR l.manifest_hash <> p.manifest_hash \
                         OR NOT l.complete) \
                    AND NOT EXISTS ( \
                        SELECT 1 FROM atomic_tree_delta_headers h \
                         WHERE h.manifest_hash = p.manifest_hash \
                           AND h.delta_state = 2 \
                    ) \
             )",
            &[],
        )
        .map(|row| row.get(0))
        .map_err(|error| postgres_error("postgres/tree-live-repair-discovery", error))
}

fn program_generation_ref_backfill_required<C: GenericClient>(
    client: &mut C,
) -> Result<bool, SemanticError> {
    let row = client
        .query_opt(
            "SELECT complete, problem_code, walker_version \
               FROM atomic_program_reference_state WHERE singleton",
            &[],
        )
        .map_err(|error| postgres_error("postgres/program-ref-repair-discovery", error))?;
    let Some(row) = row else { return Ok(true) };
    let version: i64 = row.get(2);
    if version > PROGRAM_REFERENCE_WALKER_VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "postgres/program-reference-walker-too-new",
            "program reference evidence belongs to a newer dependency walker",
        ));
    }
    Ok(!row.get::<_, bool>(0)
        || row.get::<_, Option<String>>(1).is_some()
        || version != PROGRAM_REFERENCE_WALKER_VERSION)
}

/// Rebuild the exact per-generation temporal program roots and their fixed
/// content dependencies. SQL cannot authenticate native values, so this is a
/// Rust migration/repair boundary; any corrupt log or missing program aborts
/// the migration and leaves GC fail-closed.
fn backfill_program_generation_refs<C: GenericClient>(client: &mut C) -> Result<(), SemanticError> {
    client
        .batch_execute(
            // Builders allocate/lock their generation owner before writing a
            // membership. Keep the exceptional offline repair in that same
            // order so it cannot form the old generation<->membership cycle.
            "LOCK TABLE atomic_log_generations IN SHARE MODE; \
             LOCK TABLE atomic_generation_transactions IN SHARE MODE; \
             LOCK TABLE atomic_transactions IN SHARE MODE; \
             LOCK TABLE atomic_heads, atomic_log_generation_builds, \
                        atomic_log_generation_activations, atomic_log_generation_retirements, \
                        atomic_log_generation_collection_progress, \
                        atomic_log_generation_abandonment_progress, \
                        atomic_generation_requests, atomic_requests, \
                        atomic_transaction_contents, atomic_programs, \
                        atomic_program_generation_refs IN SHARE MODE",
        )
        .map_err(|error| postgres_error("postgres/program-ref-lock", error))?;
    client
        .execute(
            "INSERT INTO atomic_program_reference_state \
                (singleton, complete, problem_code, walker_version) \
             VALUES (true, false, 'program/reference-rebuild', 0) \
             ON CONFLICT (singleton) DO UPDATE \
                SET complete = false, problem_code = 'program/reference-rebuild', \
                    updated_at = clock_timestamp()",
            &[],
        )
        .map_err(|error| postgres_error("postgres/program-ref-incomplete", error))?;
    // Restore stages authenticated code and its marks before future log
    // batches. Preserve those deliberate roots only while its builder remains
    // unpublished and unclaimed; a GC claim must never regain drained marks.
    // The owner->reference table locks above also serialize staging across
    // this capture/clear boundary.
    let mut staged = BTreeMap::<(String, u64), BTreeSet<Digest>>::new();
    for row in client
        .query(
            "SELECT r.database_id, r.log_generation, r.program_hash \
           FROM atomic_program_generation_refs r \
           JOIN atomic_log_generation_builds b \
             ON b.database_id=r.database_id AND b.generation=r.log_generation \
          WHERE NOT EXISTS (SELECT 1 FROM atomic_heads h \
                             WHERE h.database_id=b.database_id AND h.log_generation=b.generation) \
            AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                             WHERE a.database_id=b.database_id AND a.generation=b.generation) \
            AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_collection_progress p \
                             WHERE p.database_id=b.database_id AND p.generation=b.generation) \
            AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress p \
                             WHERE p.database_id=b.database_id AND p.generation=b.generation)",
            &[],
        )
        .map_err(|error| postgres_error("postgres/program-ref-staged-roots", error))?
    {
        let database_id: String = row.get(0);
        let generation = pg_basis(row.get(1), "staged program reference generation")?;
        staged
            .entry((database_id, generation))
            .or_default()
            .insert(digest(row.get(2), "staged program root")?);
    }
    client
        .execute("DELETE FROM atomic_program_generation_refs", &[])
        .map_err(|error| postgres_error("postgres/program-ref-clear", error))?;

    let databases = client
        .query(
            "SELECT d.database_id, d.genesis_hash, h.log_generation, h.basis_t, h.tx_hash \
               FROM atomic_databases d LEFT JOIN atomic_heads h USING (database_id) \
              ORDER BY d.database_id",
            &[],
        )
        .map_err(|error| postgres_error("postgres/program-ref-databases", error))?;
    for row in databases {
        let database_id: String = row.get(0);
        let genesis_hash = digest(row.get(1), "program-reference genesis hash")?;
        let active_generation = row
            .get::<_, Option<i64>>(2)
            .map(|generation| pg_basis(generation, "program-reference active generation"))
            .transpose()?;
        let active_endpoint = active_generation
            .map(|_| {
                Ok::<_, SemanticError>((
                    pg_basis(row.get(3), "program-reference published basis")?,
                    digest(row.get(4), "program-reference published hash")?,
                ))
            })
            .transpose()?;
        let mut generations = client
            .query(
                "SELECT generation FROM atomic_log_generations \
                  WHERE database_id = $1 ORDER BY generation",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/program-ref-generations", error))?
            .into_iter()
            .map(|row| pg_basis(row.get(0), "program-reference generation"))
            .collect::<Result<BTreeSet<_>, _>>()?;
        let has_legacy: bool = client
            .query_one(
                "SELECT COALESCE($2::bigint = 0, false) \
                        OR EXISTS (SELECT 1 FROM atomic_transactions WHERE database_id = $1) \
                        OR EXISTS (SELECT 1 FROM atomic_log_generation_retirements \
                                    WHERE database_id = $1 AND generation = 0)",
                &[&database_id, &active_generation.map(sql_basis).transpose()?],
            )
            .map_err(|error| postgres_error("postgres/program-ref-legacy", error))?
            .get(0);
        if has_legacy {
            generations.insert(0);
        }

        for generation in generations {
            let terminal = if generation == 0 {
                client
                    .query_opt(
                        "SELECT basis_t, tx_hash FROM atomic_transactions \
                          WHERE database_id = $1 ORDER BY basis_t DESC LIMIT 1",
                        &[&database_id],
                    )
                    .map_err(|error| postgres_error("postgres/program-ref-legacy-head", error))?
            } else {
                client
                    .query_opt(
                        "SELECT basis_t, tx_hash FROM atomic_generation_transactions \
                          WHERE database_id = $1 AND generation = $2 \
                          ORDER BY basis_t DESC LIMIT 1",
                        &[&database_id, &sql_basis(generation)?],
                    )
                    .map_err(|error| {
                        postgres_error("postgres/program-ref-generation-head", error)
                    })?
            };
            let (basis, hash) = match terminal {
                Some(row) => (
                    pg_basis(row.get(0), "program-reference terminal basis")?,
                    digest(row.get(1), "program-reference terminal hash")?,
                ),
                None => (0, genesis_hash),
            };
            let partial = program_reference_partial_generation(
                client,
                &database_id,
                generation,
                active_generation,
            )?;
            if partial.is_none() {
                // Active and retained readable generations still require a
                // complete authenticated semantic replay. Typed non-readable
                // build/GC owners may legitimately lack receipt/log prefixes.
                // A valid surviving prefix is not proof that the published
                // endpoint survived: anchor it before rebuilding any marks.
                validate_program_reference_endpoint(
                    client,
                    &database_id,
                    generation,
                    if Some(generation) == active_generation {
                        active_endpoint
                    } else {
                        None
                    },
                    basis,
                    hash,
                )?;
                recover_generation_to(client, &database_id, generation, basis, hash)?;
            }
            let mut roots = retained_generation_program_roots(
                client,
                &database_id,
                generation,
                partial.unwrap_or(false),
            )?;
            if partial == Some(false) {
                roots.extend(
                    staged
                        .remove(&(database_id.clone(), generation))
                        .unwrap_or_default(),
                );
            }
            let reachable = authenticated_program_closure(client, roots)?;
            let generation_sql = sql_basis(generation)?;
            for hash in reachable {
                client
                    .execute(
                        "INSERT INTO atomic_program_generation_refs \
                             (database_id, log_generation, program_hash) \
                         VALUES ($1, $2, $3)",
                        &[&database_id, &generation_sql, &&hash[..]],
                    )
                    .map_err(|error| postgres_error("postgres/program-ref-write", error))?;
            }
        }
    }
    let updated = client
        .execute(
            "UPDATE atomic_program_reference_state \
                SET complete = true, problem_code = NULL, walker_version = $1, \
                    updated_at = clock_timestamp() \
              WHERE singleton",
            &[&PROGRAM_REFERENCE_WALKER_VERSION],
        )
        .map_err(|error| postgres_error("postgres/program-ref-complete", error))?;
    if updated != 1 {
        return Err(fault(
            "postgres/program-ref-state",
            "program reference completeness singleton is absent",
        ));
    }
    Ok(())
}

/// Publication authority is independent of the rows being authenticated.
/// A retired readable generation's successor records its final basis. Newer
/// catalogs also retain an exact semantic coordinate; old pre-v15 retired
/// generations may lack that derived coordinate, but never the final basis.
fn validate_program_reference_endpoint<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    active_endpoint: Option<(u64, Digest)>,
    retained_basis: u64,
    retained_hash: Digest,
) -> Result<(), SemanticError> {
    let (expected_basis, expected_hash) = if let Some((basis, hash)) = active_endpoint {
        (basis, Some(hash))
    } else {
        let row = client
            .query_opt(
                "SELECT a.prior_generation, a.prior_basis_t, c.tx_hash \
                   FROM atomic_log_generation_retirements r \
                   JOIN atomic_log_generation_activations a \
                     ON a.database_id=r.database_id AND a.generation=r.successor_generation \
                   LEFT JOIN atomic_semantic_commitment_roots c \
                     ON c.database_id=r.database_id AND c.generation=r.generation \
                    AND c.basis_t=a.prior_basis_t \
                  WHERE r.database_id=$1 AND r.generation=$2",
                &[&database_id, &sql_basis(generation)?],
            )
            .map_err(|error| postgres_error("postgres/program-ref-retired-endpoint", error))?
            .ok_or_else(|| {
                fault(
                    "postgres/program-ref-endpoint-authority",
                    "readable generation has neither an active head nor retirement authority",
                )
            })?;
        if pg_basis(row.get(0), "program-reference retired predecessor")? != generation {
            return Err(fault(
                "postgres/program-ref-endpoint-authority",
                "successor activation does not identify its retired predecessor",
            ));
        }
        (
            pg_basis(row.get(1), "program-reference retired basis")?,
            row.get::<_, Option<Vec<u8>>>(2)
                .map(|hash| digest(hash, "program-reference retired hash"))
                .transpose()?,
        )
    };
    if retained_basis != expected_basis || expected_hash.is_some_and(|hash| retained_hash != hash) {
        return Err(fault(
            "postgres/program-ref-endpoint-mismatch",
            "retained generation terminal does not match its published or retired endpoint",
        ));
    }
    Ok(())
}

/// `Some(false)` is an unpublished builder: receipts may be absent, but its
/// log must remain a contiguous prefix. `Some(true)` is a durable GC claim,
/// which closes reader admission and may have drained an initial log prefix.
fn program_reference_partial_generation<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    active_generation: Option<u64>,
) -> Result<Option<bool>, SemanticError> {
    let row = client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_log_generation_collection_progress \
                         WHERE database_id = $1 AND generation = $2), \
                EXISTS (SELECT 1 FROM atomic_log_generation_abandonment_progress \
                         WHERE database_id = $1 AND generation = $2), \
                EXISTS (SELECT 1 FROM atomic_log_generation_builds b \
                         WHERE b.database_id = $1 AND b.generation = $2 \
                           AND NOT EXISTS (SELECT 1 FROM atomic_log_generation_activations a \
                                            WHERE a.database_id = b.database_id \
                                              AND a.generation = b.generation))",
            &[&database_id, &sql_basis(generation)?],
        )
        .map_err(|error| postgres_error("postgres/program-ref-generation-state", error))?;
    let collecting: bool = row.get(0);
    let abandoning: bool = row.get(1);
    let building: bool = row.get(2);
    if Some(generation) == active_generation {
        if collecting || abandoning {
            return Err(fault(
                "postgres/program-ref-active-collection",
                "active generation has a collection claim",
            ));
        }
        return Ok(None);
    }
    if collecting || abandoning {
        Ok(Some(true))
    } else if building {
        Ok(Some(false))
    } else {
        Ok(None)
    }
}

/// Hash-authenticate every retained canonical value, independently of receipt
/// retention and noHistory's materialized projection. This deliberately marks
/// code in retractions and otherwise hidden log facts until their owning rows
/// have actually been collected. It does not confer readability on a builder
/// or a partially collected generation.
fn retained_generation_program_roots<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    allow_missing_prefix: bool,
) -> Result<BTreeSet<Digest>, SemanticError> {
    let catalog = client
        .query_one(
            "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/program-ref-catalog", error))?;
    let lineage: String = catalog.get(0);
    let genesis: Vec<u8> = catalog.get(1);
    let genesis_hash = digest(catalog.get(2), "program reference genesis hash")?;
    if sha256(&genesis) != genesis_hash {
        return Err(fault(
            "recovery/genesis-checksum-mismatch",
            "genesis row does not match its digest",
        ));
    }
    let mut roots = transaction_program_roots(&decode_genesis(&genesis)?);
    let rows = if generation == 0 {
        client.query(
            "SELECT basis_t, previous_hash, tx_hash, payload, state_hash, \
                    NULL::bytea, NULL::bigint, NULL::text, NULL::bigint, NULL::bigint, NULL::smallint \
               FROM atomic_transactions WHERE database_id = $1 ORDER BY basis_t",
            &[&database_id],
        )
    } else {
        client.query(
            "SELECT t.basis_t, t.previous_hash, t.tx_hash, c.payload, t.state_hash, \
                    t.content_hash, t.eidx_frontier, c.lineage_id, c.basis_t, c.eidx_frontier, c.envelope_version \
               FROM atomic_generation_transactions t \
               LEFT JOIN atomic_transaction_contents c USING (content_hash) \
              WHERE t.database_id = $1 AND t.generation = $2 ORDER BY t.basis_t",
            &[&database_id, &sql_basis(generation)?],
        )
    }.map_err(|error| postgres_error("postgres/program-ref-retained-log", error))?;
    let mut previous: Option<(u64, Digest)> = None;
    for row in rows {
        let basis = pg_basis(row.get(0), "program reference transaction basis")?;
        let previous_hash = digest(row.get(1), "program reference predecessor")?;
        let hash = digest(row.get(2), "program reference transaction hash")?;
        if let Some((previous_basis, expected_hash)) = previous {
            if basis != previous_basis + 1 || previous_hash != expected_hash {
                return Err(fault(
                    "recovery/noncontiguous-basis",
                    "retained generation log has a gap or broken predecessor",
                ));
            }
        } else if (basis != 1 && !allow_missing_prefix)
            || (basis == 1 && previous_hash != genesis_hash)
        {
            return Err(fault(
                "recovery/predecessor-mismatch",
                "retained generation does not begin at its required genesis",
            ));
        }
        let payload: Vec<u8> = row.get::<_, Option<Vec<u8>>>(3).ok_or_else(|| {
            fault(
                "recovery/missing-transaction-content",
                "retained generation references missing immutable transaction content",
            )
        })?;
        let datoms = if generation == 0 {
            if transaction_hash(&payload) != hash {
                return Err(fault(
                    "recovery/transaction-checksum-mismatch",
                    "retained transaction does not match its digest",
                ));
            }
            let transaction = decode_transaction(&payload)?;
            if transaction.database_id != database_id
                || transaction.basis_t != basis
                || transaction.previous_hash != previous_hash
            {
                return Err(fault(
                    "recovery/envelope-mismatch",
                    "retained transaction coordinates disagree with its row",
                ));
            }
            transaction.tx_data
        } else {
            let content_hash = digest(row.get(5), "program reference content hash")?;
            let state_hash = digest(row.get(4), "program reference state hash")?;
            let frontier = pg_basis(row.get(6), "program reference entity frontier")?;
            let content = LineageTransactionContent::decode(&payload)?;
            if sha256(&payload) != content_hash {
                return Err(fault(
                    "recovery/content-checksum-mismatch",
                    "retained transaction content does not match its digest",
                ));
            }
            if row.get::<_, Option<String>>(7).as_deref() != Some(lineage.as_str())
                || row.get::<_, Option<i64>>(8) != Some(sql_basis(basis)?)
                || row.get::<_, Option<i64>>(9) != Some(sql_basis(frontier)?)
                || row.get::<_, Option<i16>>(10) != Some(1)
                || content.lineage_id != lineage
                || content.basis_t != basis
                || content.eidx_frontier != frontier
            {
                return Err(fault(
                    "recovery/content-coordinate-mismatch",
                    "retained content metadata disagrees with its membership",
                ));
            }
            if generation_transaction_hash(
                &lineage,
                generation,
                basis,
                previous_hash,
                content_hash,
                state_hash,
                frontier,
            )? != hash
            {
                return Err(fault(
                    "recovery/generation-membership-mismatch",
                    "retained generation membership commitment is invalid",
                ));
            }
            content.tx_data
        };
        roots.extend(transaction_program_roots(&datoms));
        previous = Some((basis, hash));
    }
    Ok(roots)
}

fn authenticated_program_closure<C: GenericClient>(
    client: &mut C,
    roots: BTreeSet<Digest>,
) -> Result<BTreeSet<Digest>, SemanticError> {
    let mut resolve = |hash: ProgramHash| {
        let row = client
            .query_opt(
                "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-ref-program", error))?
            .ok_or_else(|| {
                fault(
                    "postgres/program-ref-missing-program",
                    "temporal database information references an absent program blob",
                )
            })?;
        let stored_kind: i16 = row.get(0);
        let stored_arity: i16 = row.get(1);
        let payload: Vec<u8> = row.get(2);
        if sha256(&payload) != hash {
            return Err(fault(
                "postgres/program-ref-hash",
                "temporal program blob does not match its content hash",
            ));
        }
        let program = decode_program(&payload)?;
        if program_kind_i16(program.kind) != stored_kind || i16::from(program.arity) != stored_arity
        {
            return Err(fault(
                "postgres/program-ref-metadata",
                "temporal program metadata disagrees with canonical program bytes",
            ));
        }
        Ok(Arc::new(ValidatedProgram::from_canonical(program)))
    };
    visit_program_closure(&mut resolve, roots, &mut |_, _| {})
}

fn state_commitment_backfill_required<C: GenericClient>(
    client: &mut C,
) -> Result<bool, SemanticError> {
    client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM atomic_transactions \
             WHERE state_hash = decode(repeat('00', 32), 'hex'))",
            &[],
        )
        .map(|row| row.get(0))
        .map_err(|error| postgres_error("postgres/state-backfill-discovery", error))
}

fn upgrade_rebuild_required(cause: SemanticError) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unsupported,
        "postgres/upgrade-rebuild-required",
        "pre-commitment transaction history cannot be upgraded by canonical replay; export with a compatible old decoder and import into a freshly provisioned catalog",
    )
    .detail("cause", cause.code)
}

/// Migration 9 introduced authenticated materialized-state commitments. Its
/// SQL DDL deliberately creates zero placeholders for existing immutable log
/// rows; the administrative migration path replaces those placeholders by
/// replaying the canonical v3 genesis/log once, in basis order, under the same
/// transaction and advisory lock as the schema change.
fn backfill_state_commitments<C: GenericClient>(client: &mut C) -> Result<(), SemanticError> {
    client
        .batch_execute(
            "ALTER TABLE atomic_transactions \
             DISABLE TRIGGER atomic_transactions_immutable",
        )
        .map_err(|error| postgres_error("postgres/state-backfill-disable-guard", error))?;
    let databases = client
        .query(
            "SELECT database_id, genesis, genesis_hash \
             FROM atomic_databases ORDER BY database_id",
            &[],
        )
        .map_err(|error| postgres_error("postgres/state-backfill-catalog", error))?;
    for catalog in databases {
        let database_id: String = catalog.get(0);
        let genesis: Vec<u8> = catalog.get(1);
        let genesis_hash = digest(catalog.get(2), "migration genesis hash")?;
        if sha256(&genesis) != genesis_hash {
            return Err(fault(
                "postgres/state-backfill-genesis-hash",
                "migration genesis bytes do not match their hash",
            ));
        }
        let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
        let mut previous_hash = genesis_hash;
        let rows = client
            .query(
                "SELECT basis_t, previous_hash, tx_hash, payload, state_hash \
                 FROM atomic_transactions WHERE database_id = $1 ORDER BY basis_t",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/state-backfill-log", error))?;
        for row in rows {
            let basis_t = pg_basis(row.get(0), "migration transaction")?;
            let stored_previous = digest(row.get(1), "migration predecessor")?;
            let tx_hash = digest(row.get(2), "migration transaction hash")?;
            let payload: Vec<u8> = row.get(3);
            let stored_state_hash = digest(row.get(4), "migration state hash")?;
            if basis_t != database.basis_t().saturating_add(1)
                || stored_previous != previous_hash
                || transaction_hash(&payload) != tx_hash
            {
                return Err(fault(
                    "postgres/state-backfill-log-chain",
                    "migration transaction log is not one contiguous authenticated chain",
                ));
            }
            let envelope = decode_transaction(&payload)?;
            if envelope.database_id != database_id
                || envelope.basis_t != basis_t
                || envelope.previous_hash != stored_previous
            {
                return Err(fault(
                    "postgres/state-backfill-envelope",
                    "migration transaction envelope disagrees with its SQL row",
                ));
            }
            database = database.apply_committed(&envelope)?;
            let state_hash = checkpoint_state_hash(&database)?;
            if stored_state_hash == [0; 32] {
                let basis_sql = sql_basis(basis_t)?;
                let updated = client
                    .execute(
                        "UPDATE atomic_transactions SET state_hash = $3 \
                         WHERE database_id = $1 AND basis_t = $2 \
                           AND state_hash = decode(repeat('00', 32), 'hex')",
                        &[&database_id, &basis_sql, &&state_hash[..]],
                    )
                    .map_err(|error| postgres_error("postgres/state-backfill-write", error))?;
                if updated != 1 {
                    return Err(fault(
                        "postgres/state-backfill-existing-value",
                        "pre-commitment row changed during locked canonical replay",
                    ));
                }
            } else if stored_state_hash != state_hash {
                return Err(fault(
                    "postgres/state-backfill-existing-value",
                    "existing state commitment disagrees with canonical replay",
                ));
            }
            previous_hash = tx_hash;
        }
        let head = client
            .query_one(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/state-backfill-head", error))?;
        let head_basis = pg_basis(head.get(0), "migration head")?;
        let head_hash = digest(head.get(1), "migration head hash")?;
        if head_basis != database.basis_t() || head_hash != previous_hash {
            return Err(fault(
                "postgres/state-backfill-head-mismatch",
                "migration replay did not reach the published database head",
            ));
        }
    }
    client
        .batch_execute(
            "ALTER TABLE atomic_transactions \
             ENABLE TRIGGER atomic_transactions_immutable",
        )
        .map_err(|error| postgres_error("postgres/state-backfill-enable-guard", error))
}

/// Authenticate only the newest v12 graph for each database and seed its one
/// current live membership. Historical publications intentionally receive no
/// inferred node garbage; future incremental merges maintain this set by
/// exact changed-path deltas.
fn backfill_tree_live_sets<C: GenericClient>(client: &mut C) -> Result<(), SemanticError> {
    let databases = client
        .query(
            "SELECT DISTINCT database_id FROM atomic_tree_publications ORDER BY database_id",
            &[],
        )
        .map_err(|error| postgres_error("postgres/tree-live-publications", error))?;
    for row in databases {
        let database_id: String = row.get(0);
        client
            .query_one(
                "SELECT database_id FROM atomic_databases \
                  WHERE database_id = $1 FOR UPDATE",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/tree-live-database-lock", error))?;
        let manifest_hash = digest(
            client
                .query_one(
                    "SELECT manifest_hash FROM atomic_tree_publications \
                      WHERE database_id = $1 \
                      ORDER BY publication_revision DESC LIMIT 1",
                    &[&database_id],
                )
                .map_err(|error| postgres_error("postgres/tree-live-current", error))?
                .get(0),
            "live manifest hash",
        )?;
        let closure = authenticated_manifest_nodes(client, manifest_hash);
        client
            .execute(
                "DELETE FROM atomic_tree_live_sets WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/tree-live-repair-status", error))?;
        match closure {
            Ok(nodes) => {
                client
                    .execute(
                        "DELETE FROM atomic_tree_live_nodes WHERE database_id = $1",
                        &[&database_id],
                    )
                    .map_err(|error| postgres_error("postgres/tree-live-repair-nodes", error))?;
                let nodes = nodes.into_iter().collect::<Vec<_>>();
                for chunk in nodes.chunks(512) {
                    let batch = chunk.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
                    client
                        .execute(
                            "INSERT INTO atomic_tree_live_nodes (database_id, node_hash) \
                             SELECT $1, node_hash FROM unnest($2::bytea[]) AS node_hash",
                            &[&database_id, &batch],
                        )
                        .map_err(|error| postgres_error("postgres/tree-live-write", error))?;
                }
                client
                    .execute(
                        "INSERT INTO atomic_tree_live_sets \
                               (database_id, manifest_hash, complete, problem_code) \
                         VALUES ($1, $2, true, NULL)",
                        &[&database_id, &&manifest_hash[..]],
                    )
                    .map_err(|error| postgres_error("postgres/tree-live-status", error))?;
            }
            Err(error) => {
                client
                    .execute(
                        "INSERT INTO atomic_tree_live_sets \
                               (database_id, manifest_hash, complete, problem_code) \
                         VALUES ($1, $2, false, $3)",
                        &[&database_id, &&manifest_hash[..], &error.code],
                    )
                    .map_err(|error| postgres_error("postgres/tree-live-status", error))?;
            }
        }
    }
    Ok(())
}

fn authenticated_manifest_nodes<C: GenericClient>(
    client: &mut C,
    manifest_hash: Digest,
) -> Result<BTreeSet<Digest>, SemanticError> {
    let row = client
        .query_opt(
            "SELECT p.database_id, p.publication_revision, p.basis_t, p.tx_hash, \
                    m.state_hash, m.excision_generation, m.eidx_frontier, \
                    m.manifest_version, m.payload, \
                    COALESCE(legacy.state_hash, native.state_hash), \
                    p.log_generation, m.log_generation \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_manifests m \
                 ON m.database_id = p.database_id \
                AND m.publication_revision = p.publication_revision \
                AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                AND m.manifest_hash = p.manifest_hash \
               LEFT JOIN atomic_transactions legacy \
                 ON p.log_generation = 0 AND legacy.database_id = p.database_id \
                AND legacy.basis_t = p.basis_t AND legacy.tx_hash = p.tx_hash \
               LEFT JOIN atomic_generation_transactions native \
                 ON p.log_generation > 0 AND native.database_id = p.database_id \
                AND native.generation = p.log_generation \
                AND native.basis_t = p.basis_t AND native.tx_hash = p.tx_hash \
              WHERE p.manifest_hash = $1 \
                AND ((p.log_generation = 0 AND legacy.tx_hash IS NOT NULL) \
                  OR (p.log_generation > 0 AND native.tx_hash IS NOT NULL))",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("postgres/tree-closure-manifest", error))?
        .ok_or_else(|| {
            fault(
                "postgres/tree-closure-missing-manifest",
                "published tree is missing its manifest or authoritative transaction",
            )
        })?;
    let database_id: String = row.get(0);
    let revision = pg_basis(row.get(1), "closure publication revision")?;
    let basis_t = pg_basis(row.get(2), "closure publication basis")?;
    let tx_hash = digest(row.get(3), "closure transaction hash")?;
    let state_hash = digest(row.get(4), "closure state hash")?;
    let generation = pg_basis(row.get(5), "closure excision generation")?;
    let frontier = pg_basis(row.get(6), "closure entity frontier")?;
    let version: i16 = row.get(7);
    let payload: Vec<u8> = row.get(8);
    let authoritative_state = digest(row.get(9), "closure authoritative state hash")?;
    let publication_generation = pg_basis(row.get(10), "closure publication generation")?;
    let manifest_generation = pg_basis(row.get(11), "closure manifest generation")?;
    let manifest = crate::PersistentTreeManifest::decode(&payload)?;
    if version != crate::PersistentTreeManifest::encoded_version(&payload)?
        || sha256(&payload) != manifest_hash
        || manifest.database_id != database_id
        || manifest.publication_revision != revision
        || manifest.basis_t != basis_t
        || manifest.tx_hash != tx_hash
        || manifest.state_hash != state_hash
        || state_hash != authoritative_state
        || manifest.excision_generation != generation
        || publication_generation != generation
        || manifest_generation != generation
        || manifest.eidx_frontier != frontier
    {
        return Err(fault(
            "postgres/tree-closure-manifest-mismatch",
            "published tree manifest is not one canonical authoritative value",
        ));
    }
    let root_rows = client
        .query(
            "SELECT index_order, history, root_hash, datom_count, encoded_bytes \
               FROM atomic_tree_manifest_roots WHERE manifest_hash = $1",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("postgres/tree-closure-roots", error))?;
    if root_rows.len() != manifest.trees.len() {
        return Err(fault(
            "postgres/tree-closure-root-count",
            "published tree does not have exactly its eight relational root bindings",
        ));
    }
    let mut relational_roots = BTreeMap::new();
    for root in root_rows {
        let order = match root.get::<_, i16>(0) {
            value @ 0..=3 => value,
            _ => {
                return Err(fault(
                    "postgres/tree-closure-root-order",
                    "tree root binding has an invalid index order",
                ));
            }
        };
        let history: bool = root.get(1);
        if relational_roots
            .insert(
                (order, history),
                (
                    digest(root.get(2), "closure root hash")?,
                    pg_basis(root.get(3), "closure root count")?,
                    pg_basis(root.get(4), "closure root bytes")?,
                ),
            )
            .is_some()
        {
            return Err(fault(
                "postgres/tree-closure-duplicate-root",
                "tree manifest has a duplicate relational root binding",
            ));
        }
    }
    if !manifest.trees.iter().all(|tree| {
        relational_roots.get(&(
            match tree.descriptor.order {
                crate::IndexOrder::Eavt => 0,
                crate::IndexOrder::Aevt => 1,
                crate::IndexOrder::Avet => 2,
                crate::IndexOrder::Vaet => 3,
            },
            tree.descriptor.history,
        )) == Some(&(
            tree.descriptor.root_hash,
            tree.descriptor.count,
            tree.root_bytes,
        ))
    }) {
        return Err(fault(
            "postgres/tree-closure-root-mismatch",
            "canonical tree manifest disagrees with its relational roots",
        ));
    }

    let mut pending = manifest
        .trees
        .iter()
        .map(|tree| tree.descriptor.root_hash)
        .collect::<Vec<_>>();
    let mut nodes = BTreeMap::new();
    while let Some(hash) = pending.pop() {
        if nodes.contains_key(&hash) {
            continue;
        }
        let payload: Vec<u8> = client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/tree-closure-node", error))?
            .ok_or_else(|| {
                fault(
                    "postgres/tree-closure-missing-node",
                    "published tree references a missing immutable node",
                )
            })?
            .get(0);
        match decode_tree_node(&hash, &payload)? {
            TreeNode::Root(root) => {
                pending.extend(root.directories.into_iter().map(|child| child.hash));
            }
            TreeNode::Directory(directory) => {
                pending.extend(directory.leaves.into_iter().map(|child| child.hash));
            }
            TreeNode::Leaf(_) => {}
        }
        nodes.insert(hash, payload);
    }
    let node_set = crate::persistent_tree::TreeNodeSet::from_nodes(nodes);
    for tree in &manifest.trees {
        crate::persistent_tree::validate_tree(&tree.descriptor, &node_set)?;
    }
    Ok(node_set.iter().map(|(hash, _)| *hash).collect())
}

fn reject_unsupported_populated_upgrade<C: GenericClient>(
    client: &mut C,
    installed: &[(i64, Vec<u8>)],
) -> Result<(), SemanticError> {
    let installed_version = installed.last().map_or(0, |(version, _)| *version);
    if installed_version >= POSTGRES_IN_PLACE_UPGRADE_FLOOR {
        return Ok(());
    }
    let catalog_exists: bool = client
        .query_one("SELECT to_regclass('atomic_databases') IS NOT NULL", &[])
        .map_err(|error| postgres_error("postgres/migration-discovery", error))?
        .get(0);
    if !catalog_exists {
        return Ok(());
    }
    let populated: bool = client
        .query_one("SELECT EXISTS (SELECT 1 FROM atomic_databases)", &[])
        .map_err(|error| postgres_error("postgres/migration-population-check", error))?
        .get(0);
    if populated {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "postgres/upgrade-rebuild-required",
            format!(
                "populated native SQL schema version {installed_version} predates the supported in-place upgrade floor {POSTGRES_IN_PLACE_UPGRADE_FLOOR}; export with a compatible old decoder and import into a freshly provisioned catalog"
            ),
        ));
    }
    Ok(())
}

fn migration_table_exists<C: GenericClient>(client: &mut C) -> Result<bool, SemanticError> {
    client
        .query_one(
            "SELECT to_regclass('atomic_schema_migrations') IS NOT NULL",
            &[],
        )
        .map(|row| row.get(0))
        .map_err(|error| postgres_error("postgres/migration-discovery", error))
}

fn read_migration_rows<C: GenericClient>(
    client: &mut C,
) -> Result<Vec<(i64, Vec<u8>)>, SemanticError> {
    client
        .query(
            "SELECT version, checksum FROM atomic_schema_migrations ORDER BY version",
            &[],
        )
        .map_err(|error| postgres_error("postgres/migration-read", error))
        .map(|rows| {
            rows.into_iter()
                .map(|row| (row.get(0), row.get(1)))
                .collect()
        })
}

fn validate_migration_rows(
    installed: &[(i64, Vec<u8>)],
    require_complete: bool,
) -> Result<(), SemanticError> {
    if let Some((version, _)) = installed
        .iter()
        .find(|(version, _)| *version > POSTGRES_SCHEMA_VERSION)
    {
        return Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "postgres/schema-too-new",
            format!(
                "database schema version {version} is newer than binary version {POSTGRES_SCHEMA_VERSION}"
            ),
        ));
    }
    if installed.len() > MIGRATIONS.len() {
        return Err(fault(
            "postgres/migration-history-invalid",
            "installed migration history is not a prefix known to this binary",
        ));
    }
    for ((version, checksum), (expected_version, sql)) in installed.iter().zip(MIGRATIONS) {
        if version != expected_version {
            return Err(fault(
                "postgres/migration-history-invalid",
                format!(
                    "installed migration {version} appears where version {expected_version} is required"
                ),
            ));
        }
        if checksum.as_slice() != sha256(sql.as_bytes()) {
            return Err(fault(
                "postgres/migration-checksum-mismatch",
                format!("installed migration {version} differs from this binary"),
            ));
        }
    }
    if require_complete && installed.len() != MIGRATIONS.len() {
        let installed_version = installed.last().map_or(0, |(version, _)| *version);
        return Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "postgres/schema-upgrade-required",
            format!(
                "database schema version {installed_version} is older than required version {POSTGRES_SCHEMA_VERSION}"
            ),
        ));
    }
    Ok(())
}

pub(crate) fn verify_schema_compatibility<C: GenericClient>(
    client: &mut C,
) -> Result<(), SemanticError> {
    if !migration_table_exists(client)? {
        return Err(SemanticError::new(
            ErrorCategory::Unavailable,
            "postgres/schema-not-installed",
            "Atomic PostgreSQL migrations have not been installed",
        ));
    }
    validate_migration_rows(&read_migration_rows(client)?, true)
}

fn grant_runtime_privileges(
    client: &mut Client,
    writer_role: &str,
    peer_role: &str,
) -> Result<(), SemanticError> {
    if writer_role == peer_role {
        return Err(SemanticError::incorrect(
            "postgres/runtime-roles-not-distinct",
            "writer and peer runtime roles must be distinct",
        ));
    }
    let mut transaction = client
        .transaction()
        .map_err(|error| postgres_error("postgres/runtime-grants-begin", error))?;
    let schema = pin_current_schema(&mut transaction)?;
    verify_schema_compatibility(&mut transaction)?;
    repair_atomic_routine_paths(&mut transaction, &schema)?;
    let database: String = transaction
        .query_one("SELECT current_database()", &[])
        .map_err(|error| postgres_error("postgres/runtime-grants-database", error))?
        .get(0);
    let public_can_create: bool = transaction
        .query_one(
            "SELECT EXISTS (\
                 SELECT 1 FROM pg_namespace n, \
                      LATERAL aclexplode(COALESCE(n.nspacl, acldefault('n', n.nspowner))) a \
                  WHERE n.nspname = current_schema() \
                    AND a.grantee = 0 AND a.privilege_type = 'CREATE'\
             )",
            &[],
        )
        .map_err(|error| postgres_error("postgres/runtime-grants-public", error))?
        .get(0);
    if public_can_create {
        return Err(SemanticError::incorrect(
            "postgres/runtime-schema-public-create",
            format!(
                "schema {schema} grants CREATE to PUBLIC; revoke it before provisioning runtime roles"
            ),
        ));
    }
    let public_has_relation_privileges: bool = transaction
        .query_one(
            r#"SELECT EXISTS (
                 SELECT 1
                   FROM pg_class c
                   JOIN pg_namespace n ON n.oid = c.relnamespace,
                        LATERAL aclexplode(
                            COALESCE(c.relacl, acldefault('r', c.relowner))
                        ) a
                  WHERE n.nspname = $1
                    AND c.relkind IN ('r', 'p', 'v')
                    AND c.relname LIKE 'atomic\_%' ESCAPE '\'
                    AND a.grantee = 0
                 UNION ALL
                 SELECT 1
                   FROM pg_class c
                   JOIN pg_namespace n ON n.oid = c.relnamespace
                   JOIN pg_attribute column_acl ON column_acl.attrelid = c.oid,
                        LATERAL aclexplode(column_acl.attacl) a
                  WHERE n.nspname = $1
                    AND c.relkind IN ('r', 'p', 'v')
                    AND c.relname LIKE 'atomic\_%' ESCAPE '\'
                    AND column_acl.attnum > 0
                    AND NOT column_acl.attisdropped
                    AND a.grantee = 0
             )"#,
            &[&schema],
        )
        .map_err(|error| postgres_error("postgres/runtime-grants-public-relations", error))?
        .get(0);
    if public_has_relation_privileges {
        return Err(SemanticError::incorrect(
            "postgres/runtime-relations-public-privileges",
            format!(
                "schema {schema} grants privileges on Atomic relations to PUBLIC; revoke them before provisioning runtime roles"
            ),
        ));
    }
    for role in [writer_role, peer_role] {
        validate_runtime_role(&mut transaction, role, &schema)?;
    }

    let schema_ident = quote_identifier(&schema)?;
    let database_ident = quote_identifier(&database)?;
    let peer_ident = quote_identifier(peer_role)?;
    let writer_ident = quote_identifier(writer_role)?;
    // Reset direct grants across the discovered namespace, not merely the
    // current positive grant whitelist. Otherwise a privilege on an
    // administrative table (or a table added by a future migration) would
    // survive provisioning even though it is absent from the runtime policy.
    let atomic_relations = transaction
        .query(
            "SELECT c.relname::text \
               FROM pg_class c \
               JOIN pg_namespace n ON n.oid = c.relnamespace \
              WHERE n.nspname = $1 \
                AND c.relkind IN ('r', 'p', 'v') \
                AND c.relname LIKE 'atomic\\_%' ESCAPE '\\' \
              ORDER BY c.relname",
            &[&schema],
        )
        .map_err(|error| postgres_error("postgres/runtime-grants-relations", error))?
        .into_iter()
        .map(|row| row.get::<_, String>(0))
        .collect::<Vec<_>>();
    if atomic_relations.is_empty() {
        return Err(fault(
            "postgres/runtime-relations-missing",
            "installed schema contains no Atomic runtime relations",
        ));
    }
    let all_relations = atomic_relations
        .iter()
        .map(|relation| {
            quote_identifier(relation).map(|relation| format!("{schema_ident}.{relation}"))
        })
        .collect::<Result<Vec<_>, _>>()?
        .join(", ");
    let atomic_routines = transaction
        .query(
            "SELECT p.proname::text, pg_get_function_identity_arguments(p.oid) \
               FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
              WHERE n.nspname = $1 AND p.proname LIKE 'atomic\\_%' ESCAPE '\\' \
              ORDER BY p.proname, pg_get_function_identity_arguments(p.oid)",
            &[&schema],
        )
        .map_err(|error| postgres_error("postgres/runtime-grants-routines", error))?
        .into_iter()
        .map(|row| {
            let name = quote_identifier(&row.get::<_, String>(0))?;
            let arguments: String = row.get(1);
            Ok(format!("{schema_ident}.{name}({arguments})"))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    for role_ident in [&writer_ident, &peer_ident] {
        transaction
            .batch_execute(&format!(
                "REVOKE ALL PRIVILEGES ON TABLE {all_relations} FROM {role_ident}; \
                 REVOKE CREATE ON SCHEMA {schema_ident} FROM {role_ident}; \
                 GRANT CONNECT ON DATABASE {database_ident} TO {role_ident}; \
                 GRANT USAGE ON SCHEMA {schema_ident} TO {role_ident}"
            ))
            .map_err(|error| postgres_error("postgres/runtime-grants-reset", error))?;
        for routine in &atomic_routines {
            transaction
                .batch_execute(&format!(
                    "REVOKE ALL ON FUNCTION {routine} FROM {role_ident}"
                ))
                .map_err(|error| postgres_error("postgres/runtime-grants-function-reset", error))?;
        }
    }
    let peer_relations = relation_list(&schema_ident, PEER_RUNTIME_TABLES);
    // The tree-publication trigger takes `atomic_databases FOR UPDATE` as its
    // database-scoped serialization point. PostgreSQL therefore requires the
    // writer to hold UPDATE on that catalog relation. The immutable-catalog
    // trigger remains the independent authority that rejects actual changes;
    // this is a trusted-writer row-lock capability, not a mutable-catalog API.
    transaction
        .batch_execute(&format!(
            "GRANT SELECT ON TABLE {peer_relations} TO {peer_ident}; \
             GRANT SELECT ON TABLE {peer_relations} TO {writer_ident}; \
             GRANT SELECT ON TABLE {} TO {writer_ident}; \
             GRANT UPDATE ON TABLE {} TO {writer_ident}; \
             GRANT INSERT ON TABLE {} TO {writer_ident}; \
             GRANT EXECUTE ON FUNCTION {schema_ident}.atomic_publish_tree(text, bigint, bigint, bytea, bytea), \
                                       {schema_ident}.atomic_request_base_archive_build_live(text, bigint), \
                                       {schema_ident}.atomic_heartbeat_tree_build(bytea), \
                                       {schema_ident}.atomic_finish_tree_build(bytea), \
                                       {schema_ident}.atomic_apply_tree_publication_work(bytea, bigint), \
                                       {schema_ident}.atomic_tree_database_build_pin_key(text), \
                                       {schema_ident}.atomic_semantic_commitment_gc_pin_key(), \
                                       {schema_ident}.atomic_log_generation_pin_key(text, bigint) TO {writer_ident}; \
             GRANT EXECUTE ON FUNCTION {schema_ident}.atomic_log_generation_pin_key(text, bigint) TO {peer_ident}",
            relation_list(&schema_ident, WRITER_RUNTIME_TABLES),
            relation_list(&schema_ident, WRITER_UPDATE_TABLES),
            relation_list(&schema_ident, WRITER_INSERT_TABLES),
        ))
        .map_err(|error| postgres_error("postgres/runtime-grants-apply", error))?;
    transaction
        .commit()
        .map_err(|error| postgres_error("postgres/runtime-grants-commit", error))
}

/// Pin all unqualified migration and grant work to the caller-selected
/// installation schema. Explicitly listing `pg_temp` last suppresses
/// PostgreSQL's usual implicit temporary-schema precedence for relations.
fn pin_current_schema<C: GenericClient>(client: &mut C) -> Result<String, SemanticError> {
    let schema = client
        .query_one("SELECT current_schema()::text", &[])
        .map_err(|error| postgres_error("postgres/current-schema", error))?
        .get::<_, Option<String>>(0)
        .ok_or_else(|| {
            SemanticError::incorrect(
                "postgres/current-schema-missing",
                "connection search_path has no installation schema",
            )
        })?;
    let schema_ident = quote_identifier(&schema)?;
    client
        .batch_execute(&format!(
            "SET LOCAL search_path TO {schema_ident}, pg_catalog, pg_temp"
        ))
        .map_err(|error| postgres_error("postgres/current-schema-pin", error))?;
    Ok(schema)
}

/// Migration and role provisioning are repair boundaries for functions
/// installed by older binaries. Every Atomic routine gets the installation
/// schema first and the temporary schema last. This is required even for
/// SECURITY INVOKER trigger functions: they otherwise inherit a runtime
/// writer's implicit `pg_temp` precedence and can validate direct DML against
/// attacker-controlled temporary relations.
fn repair_atomic_routine_paths<C: GenericClient>(
    client: &mut C,
    schema: &str,
) -> Result<(), SemanticError> {
    let schema_ident = quote_identifier(schema)?;
    let expected_path = format!("{schema_ident}, pg_catalog, pg_temp");
    let routines = client
        .query(
            "SELECT p.proname::text, pg_get_function_identity_arguments(p.oid), \
                    p.prokind::text, \
                    (SELECT option_value \
                       FROM pg_options_to_table(p.proconfig) \
                      WHERE option_name = 'search_path') \
               FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
              WHERE n.nspname = $1 \
                AND p.proname LIKE 'atomic\\_%' ESCAPE '\\' \
              ORDER BY p.proname, pg_get_function_identity_arguments(p.oid)",
            &[&schema],
        )
        .map_err(|error| postgres_error("postgres/routine-path-discovery", error))?;
    for row in routines {
        if row.get::<_, Option<String>>(3).as_deref() == Some(expected_path.as_str()) {
            continue;
        }
        let function = quote_identifier(&row.get::<_, String>(0))?;
        let arguments: String = row.get(1);
        let routine_kind = match row.get::<_, String>(2).as_str() {
            "f" | "w" => "FUNCTION",
            "p" => "PROCEDURE",
            _ => {
                return Err(fault(
                    "postgres/routine-path-kind",
                    "Atomic namespace contains a routine kind whose lookup path cannot be pinned",
                ));
            }
        };
        client
            .batch_execute(&format!(
                "ALTER {routine_kind} {schema_ident}.{function}({arguments}) \
                 SET search_path TO {schema_ident}, pg_catalog, pg_temp"
            ))
            .map_err(|error| postgres_error("postgres/routine-path", error))?;
    }
    Ok(())
}

fn validate_runtime_role<C: GenericClient>(
    client: &mut C,
    role: &str,
    schema: &str,
) -> Result<(), SemanticError> {
    let row = client
        .query_opt(
            "SELECT r.oid, r.rolsuper, r.rolcreaterole, r.rolcreatedb, \
                    r.rolreplication, r.rolbypassrls, \
                    EXISTS (SELECT 1 FROM pg_auth_members m WHERE m.member = r.oid), \
                    EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace \
                             WHERE c.relowner = r.oid \
                               AND n.nspname !~ '^pg_' \
                               AND n.nspname <> 'information_schema'), \
                    EXISTS (SELECT 1 FROM pg_proc p JOIN pg_namespace n ON n.oid = p.pronamespace \
                             WHERE p.proowner = r.oid \
                               AND n.nspname !~ '^pg_' \
                               AND n.nspname <> 'information_schema'), \
                    EXISTS (SELECT 1 FROM pg_namespace n \
                             WHERE n.nspowner = r.oid \
                               AND n.nspname !~ '^pg_' \
                               AND n.nspname <> 'information_schema'), \
                    EXISTS (SELECT 1 FROM pg_database d WHERE d.datdba = r.oid), \
                    EXISTS (SELECT 1 FROM pg_namespace n \
                             WHERE n.nspname <> $2 \
                               AND n.nspname !~ '^pg_' \
                               AND n.nspname <> 'information_schema' \
                               AND has_schema_privilege(r.oid, n.oid, 'CREATE')), \
                    has_database_privilege(r.oid, current_database(), 'CREATE'), \
                    EXISTS (SELECT 1 \
                              FROM pg_class c \
                              JOIN pg_namespace n ON n.oid = c.relnamespace \
                              JOIN pg_attribute column_acl ON column_acl.attrelid = c.oid, \
                                   LATERAL aclexplode(column_acl.attacl) a \
                             WHERE n.nspname = $2 \
                               AND c.relkind IN ('r', 'p', 'v') \
                               AND c.relname LIKE 'atomic\\_%' ESCAPE '\\' \
                               AND column_acl.attnum > 0 \
                               AND NOT column_acl.attisdropped \
                               AND a.grantee = r.oid) \
               FROM pg_roles r WHERE r.rolname = $1",
            &[&role, &schema],
        )
        .map_err(|error| postgres_error("postgres/runtime-role-read", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/runtime-role-not-found",
                format!("PostgreSQL role {role} does not exist"),
            )
        })?;
    let elevated = row.get::<_, bool>(1)
        || row.get::<_, bool>(2)
        || row.get::<_, bool>(3)
        || row.get::<_, bool>(4)
        || row.get::<_, bool>(5);
    let inherits_membership: bool = row.get(6);
    let owns_non_system_relation: bool = row.get(7);
    let owns_non_system_routine: bool = row.get(8);
    let owns_non_system_schema: bool = row.get(9);
    let owns_database: bool = row.get(10);
    let can_create_in_other_schema: bool = row.get(11);
    let can_create_schema: bool = row.get(12);
    let has_atomic_column_privileges: bool = row.get(13);
    if elevated
        || inherits_membership
        || owns_non_system_relation
        || owns_non_system_routine
        || owns_non_system_schema
        || owns_database
        || can_create_in_other_schema
        || can_create_schema
        || has_atomic_column_privileges
    {
        return Err(SemanticError::incorrect(
            "postgres/runtime-role-not-least-privilege",
            format!(
                "role {role} is elevated, inherits another role, owns database objects, can create outside the Atomic schema, or holds column-level privileges on Atomic relations"
            ),
        ));
    }
    Ok(())
}

fn quote_identifier(identifier: &str) -> Result<String, SemanticError> {
    if identifier.is_empty() || identifier.contains('\0') {
        return Err(SemanticError::incorrect(
            "postgres/invalid-role-name",
            "PostgreSQL identifiers must be non-empty and contain no NUL",
        ));
    }
    Ok(format!("\"{}\"", identifier.replace('"', "\"\"")))
}

fn relation_list(schema_ident: &str, tables: &[&str]) -> String {
    tables
        .iter()
        .map(|table| format!("{schema_ident}.\"{table}\""))
        .collect::<Vec<_>>()
        .join(", ")
}

fn program_kind_i16(kind: ProgramKind) -> i16 {
    match kind {
        ProgramKind::Transaction => 0,
        ProgramKind::AttributePredicate => 1,
        ProgramKind::Query => 2,
        ProgramKind::EntityPredicate => 3,
        ProgramKind::DualPredicate => 4,
    }
}

fn validate_lease_args(
    scope: &str,
    holder_id: &str,
    lease_millis: u64,
) -> Result<(), SemanticError> {
    if scope.is_empty() || holder_id.is_empty() || lease_millis == 0 {
        return Err(SemanticError::incorrect(
            "postgres/invalid-lease",
            "lease scope, holder and positive duration are required",
        ));
    }
    Ok(())
}

fn leadership_lost(lease: &TransactorLease) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "postgres/leadership-lost",
        format!(
            "transactor {} no longer owns database {} epoch {}",
            lease.holder_id, lease.database_id, lease.epoch
        ),
    )
}

fn verify_lease<C: GenericClient>(
    client: &mut C,
    lease: &TransactorLease,
    database_id: &str,
) -> Result<(), SemanticError> {
    if lease.database_id != database_id {
        return Err(leadership_lost(lease));
    }
    let epoch = sql_basis(lease.epoch)?;
    let valid = client
        .query_opt(
            "SELECT holder_id = $2 AND epoch = $3 AND expires_at > clock_timestamp() \
             FROM atomic_transactor_leases WHERE lease_scope = $1 FOR UPDATE",
            &[&lease.database_id, &lease.holder_id, &epoch],
        )
        .map_err(|error| postgres_error("postgres/lease-fence", error))?
        .is_some_and(|row| row.get::<_, bool>(0));
    if valid {
        Ok(())
    } else {
        Err(leadership_lost(lease))
    }
}

pub(crate) fn resolve_program_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    hash: ProgramHash,
) -> Result<Arc<ValidatedProgram>, SemanticError> {
    if let Some(program) = lock_program_cache(cache).get(hash) {
        return Ok(program);
    }
    let row = client
        .query_opt(
            "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
            &[&&hash[..]],
        )
        .map_err(|error| postgres_error("postgres/program-read", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/program-not-found",
                "database function refers to program content that is not deployed",
            )
        })?;
    let kind: i16 = row.get(0);
    let arity: i16 = row.get(1);
    let payload: Vec<u8> = row.get(2);
    if sha256(&payload) != hash {
        return Err(fault(
            "postgres/program-hash-mismatch",
            "persisted program bytes do not match their content hash",
        ));
    }
    let program = decode_program(&payload)?;
    lock_program_cache(cache).record_decode_and_validation();
    if program_kind_i16(program.kind) != kind || i16::from(program.arity) != arity {
        return Err(fault(
            "postgres/program-metadata-mismatch",
            "persisted program metadata does not match canonical bytes",
        ));
    }
    let program = Arc::new(ValidatedProgram::from_canonical(program));
    lock_program_cache(cache).insert(
        hash,
        Arc::clone(&program),
        program_cache_weight(payload.len()),
    );
    Ok(program)
}

pub(crate) fn insert_program_generation_refs<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    datoms: &[Datom],
) -> Result<(), SemanticError> {
    let hashes = authenticated_program_closure(client, transaction_program_roots(datoms))?;
    let generation = sql_basis(generation)?;
    for hash in hashes {
        client
            .execute(
                "INSERT INTO atomic_program_generation_refs \
                     (database_id, log_generation, program_hash) \
                 VALUES ($1, $2, $3) ON CONFLICT DO NOTHING",
                &[&database_id, &generation, &&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-generation-ref", error))?;
    }
    Ok(())
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum CommitFault {
    None,
    BeforeTransactionInsert,
    AfterTransactionInsert,
    AfterHeadUpdate,
    #[doc(hidden)]
    AfterHeadUpdateProcessAbort,
    AfterCommitBeforeResponse,
}

#[derive(Clone, Debug)]
pub(crate) struct CommitReceipt {
    pub db_before: DatabaseValue,
    pub database: DatabaseValue,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub tempids: BTreeMap<String, u64>,
    pub tx_data: Vec<Datom>,
    pub replayed: bool,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct TransactorLease {
    pub database_id: String,
    pub holder_id: String,
    pub epoch: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct CapacityLimits {
    pub max_transaction_ops: usize,
    pub max_transaction_bytes: usize,
    pub max_history_transactions: u64,
    pub max_transaction_read_datoms: u64,
    pub max_transaction_read_bytes: u64,
    pub writer_tree_cache_entries: usize,
    pub writer_tree_cache_bytes: usize,
    pub program: ProgramLimits,
}

/// Representation-level state retained by one PostgreSQL writer.
///
/// Counts deliberately describe semantic objects rather than allocator RSS,
/// making the bounded-writer acceptance test deterministic.  The eager fields
/// expose the prototype dependency that Goal 16 removes; they remain in the
/// metric afterward as a permanent regression guard and must stay zero for a
/// production tiered writer.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct WriterResidencyStats {
    pub eager_database_values: usize,
    pub eager_current_facts: usize,
    pub eager_history_datoms: usize,
    pub recent_datoms: u64,
    pub recent_accounted_bytes: u64,
    pub tree_cache_entries: usize,
    pub tree_cache_bytes: usize,
    /// Child references retained by the eight decoded native roots. Roots
    /// live in `TreeBase`, outside the discardable node cache.
    pub resident_tree_root_children: usize,
    /// Estimated decoded root allocations, including child vectors and
    /// recursively owned routing-key data. Canonical payload bytes are not
    /// retained here and are not included in this figure.
    pub resident_tree_root_estimated_bytes: u64,
    /// Resident authenticated schema/ident projections. These are expected to
    /// scale with metadata cardinality, so their size is explicit rather than
    /// hidden inside the database-size-independent writer claim.
    pub resident_schema_attributes: usize,
    pub resident_schema_information_datoms: usize,
    pub resident_schema_estimated_bytes: u64,
    pub resident_ident_names: usize,
    pub resident_ident_entities: usize,
    pub resident_ident_estimated_bytes: u64,
    pub native_manifest_candidates: u64,
    pub native_root_reads: u64,
    pub native_directory_reads: u64,
    pub native_leaf_reads: u64,
    pub publication_revision: u64,
    /// Logical datoms delivered across the complete last committed
    /// transaction, from persisted-function expansion through assessment,
    /// successor validation/predicates, and commitment membership recovery.
    /// This is not a physical PostgreSQL/tree-node I/O counter.
    pub last_transaction_read_datoms: u64,
    /// Deterministic retained width of those delivered logical datoms.
    pub last_transaction_read_bytes: u64,
    /// Successful datoms delivered by underlying exact-prefix cursors. Memo
    /// replays increase logical reads but leave this source count unchanged.
    pub last_transaction_source_read_datoms: u64,
    pub last_transaction_source_read_bytes: u64,
    pub last_transaction_prefix_memo_hits: u64,
    pub last_transaction_prefix_memo_misses: u64,
    pub last_transaction_prefix_memo_admissions: u64,
    pub last_transaction_prefix_memo_rejections: u64,
    pub last_transaction_prefix_memo_peak_entries: usize,
    pub last_transaction_prefix_memo_peak_bytes: u64,
    /// Exact native cursor source work during the last committed operation.
    /// SQL counts are successful immutable-node rows, and byte counts are
    /// canonical node payloads rather than PostgreSQL/wire overhead.
    pub last_native_cursor_ranges: u64,
    pub last_native_cache_hits: u64,
    pub last_native_cache_misses: u64,
    pub last_native_sql_root_reads: u64,
    pub last_native_sql_directory_reads: u64,
    pub last_native_sql_leaf_reads: u64,
    pub last_native_sql_reads: u64,
    pub last_native_sql_read_bytes: u64,
    pub last_native_recent_datoms_examined: u64,
    pub last_native_recent_datoms_yielded: u64,
    pub last_commitment_node_visits: u64,
    pub last_commitment_node_hashes: u64,
    pub last_commitment_leaf_changes: u64,
    pub last_commitment_sql_node_reads: u64,
    pub last_commitment_sql_node_read_bytes: u64,
    pub last_commitment_sql_node_writes: u64,
    pub last_commitment_sql_node_write_bytes: u64,
    pub last_commitment_sql_coordinate_reads: u64,
    pub last_commitment_sql_coordinate_read_bytes: u64,
    pub last_commitment_sql_coordinate_writes: u64,
    pub last_commitment_sql_coordinate_write_bytes: u64,
}

impl Default for CapacityLimits {
    fn default() -> Self {
        Self {
            max_transaction_ops: 100_000,
            max_transaction_bytes: 64 * 1024 * 1024,
            max_history_transactions: i64::MAX as u64,
            max_transaction_read_datoms: 1_000_000,
            max_transaction_read_bytes: 64 * 1024 * 1024,
            writer_tree_cache_entries: 4_096,
            writer_tree_cache_bytes: 64 * 1024 * 1024,
            program: ProgramLimits::default(),
        }
    }
}

#[derive(Clone)]
struct WriterState {
    database: TieredSnapshot,
    commitment: PersistentCommitmentCoordinate,
    publication_revision: u64,
    last_read_work: TransactionReadWork,
    last_commitment_work: CommitmentWork,
}

/// The one concrete durable boundary for Atomic.
///
/// `Database::with` remains pure; this owner performs PostgreSQL locking,
/// publication, retry resolution, and recovery around the kernel transition.
pub struct PostgresStore {
    client: Client,
    connection: Option<PostgresConnectionConfig>,
    current: BTreeMap<String, WriterState>,
    /// Reuse immutable receipt I/O independently of admitted writer state.
    /// Weak core ownership retains neither a historical endpoint nor its pins.
    receipt_read_cores: BTreeMap<String, WeakTieredReadHandle>,
    program_cache: SharedProgramCache,
    capacity_limits: CapacityLimits,
    writer_recent_limits: RecentLimits,
}

impl PostgresStore {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        let mut client = connection.connect_for("postgres/connect")?;
        verify_schema_compatibility(&mut client)?;
        Ok(Self::from_configured_client(client, connection.clone()))
    }

    fn from_configured_client(client: Client, connection: PostgresConnectionConfig) -> Self {
        Self {
            client,
            connection: Some(connection),
            current: BTreeMap::new(),
            receipt_read_cores: BTreeMap::new(),
            program_cache: Arc::new(Mutex::new(ProgramCache::default())),
            capacity_limits: CapacityLimits::default(),
            writer_recent_limits: RecentLimits::default(),
        }
    }

    /// Low-level construction for crate-owned migration/recovery fixtures that
    /// already control the session and its schema. Runtime callers must use a
    /// checked `connect` constructor.
    #[cfg(test)]
    pub(crate) fn from_client(client: Client) -> Self {
        Self {
            client,
            connection: None,
            current: BTreeMap::new(),
            receipt_read_cores: BTreeMap::new(),
            program_cache: Arc::new(Mutex::new(ProgramCache::default())),
            capacity_limits: CapacityLimits::default(),
            writer_recent_limits: RecentLimits::default(),
        }
    }

    pub(crate) fn set_capacity_limits(
        &mut self,
        limits: CapacityLimits,
    ) -> Result<(), SemanticError> {
        if limits.max_transaction_ops == 0
            || limits.max_transaction_bytes == 0
            || limits.max_history_transactions == 0
            || limits.max_transaction_read_datoms == 0
            || limits.max_transaction_read_bytes == 0
            || !limits.program.is_valid()
        {
            return Err(SemanticError::incorrect(
                "postgres/invalid-capacity-limits",
                "transaction read/write and persisted-program capacity limits must be positive",
            ));
        }
        self.capacity_limits = limits;
        Ok(())
    }

    pub(crate) fn set_writer_recent_limits(
        &mut self,
        limits: RecentLimits,
    ) -> Result<(), SemanticError> {
        if limits.soft_datoms == 0
            || limits.soft_bytes == 0
            || limits.hard_datoms == 0
            || limits.hard_bytes == 0
            || limits.soft_datoms > limits.hard_datoms
            || limits.soft_bytes > limits.hard_bytes
        {
            return Err(SemanticError::incorrect(
                "postgres/invalid-writer-recent-limits",
                "writer recent-tier soft limits must be positive and cannot exceed hard limits",
            ));
        }
        self.writer_recent_limits = limits;
        Ok(())
    }

    pub fn set_program_cache_capacity(&mut self, capacity: usize) {
        let mut cache = lock_program_cache(&self.program_cache);
        let max_bytes = cache.max_bytes;
        cache.set_limits(capacity, max_bytes);
    }

    /// Bound cached program content by both entry count and canonical payload
    /// bytes. A zero limit disables retention without disabling execution.
    pub fn set_program_cache_limits(&mut self, max_entries: usize, max_bytes: usize) {
        lock_program_cache(&self.program_cache).set_limits(max_entries, max_bytes);
    }

    pub fn cached_programs(&self) -> usize {
        self.program_cache_stats().current_entries
    }

    pub fn program_cache_stats(&self) -> ProgramCacheStats {
        shared_program_cache_stats(&self.program_cache)
    }

    /// Report deterministic retained writer state for boundedness tests and
    /// operator diagnostics. This does not walk PostgreSQL or materialize a
    /// database value.
    pub fn writer_residency_stats(&self, database_id: &str) -> WriterResidencyStats {
        let Some(state) = self.current.get(database_id) else {
            return WriterResidencyStats::default();
        };
        let recent = state.database.recent_stats();
        let cache = state.database.tree_cache_stats();
        let roots = state.database.resident_tree_root_stats();
        let load = state.database.load_stats();
        let metadata = state.database.resident_metadata_stats();
        WriterResidencyStats {
            eager_database_values: 0,
            eager_current_facts: 0,
            eager_history_datoms: 0,
            recent_datoms: recent.datoms,
            recent_accounted_bytes: recent.accounted_bytes,
            tree_cache_entries: cache.current_entries,
            tree_cache_bytes: cache.current_bytes,
            resident_tree_root_children: roots.children,
            resident_tree_root_estimated_bytes: roots.estimated_bytes,
            resident_schema_attributes: metadata.schema_attributes,
            resident_schema_information_datoms: metadata.schema_information_datoms,
            resident_schema_estimated_bytes: metadata.schema_estimated_bytes,
            resident_ident_names: metadata.ident_names,
            resident_ident_entities: metadata.ident_entities,
            resident_ident_estimated_bytes: metadata.ident_estimated_bytes,
            native_manifest_candidates: load.manifest_candidates,
            native_root_reads: load.root_reads,
            native_directory_reads: load.directory_reads,
            native_leaf_reads: load.leaf_reads,
            publication_revision: state.publication_revision,
            last_transaction_read_datoms: state.last_read_work.logical_datoms,
            last_transaction_read_bytes: state.last_read_work.logical_retained_bytes,
            last_transaction_source_read_datoms: state.last_read_work.source_datoms,
            last_transaction_source_read_bytes: state.last_read_work.source_retained_bytes,
            last_transaction_prefix_memo_hits: state.last_read_work.prefix_hits,
            last_transaction_prefix_memo_misses: state.last_read_work.prefix_misses,
            last_transaction_prefix_memo_admissions: state.last_read_work.memo_admissions,
            last_transaction_prefix_memo_rejections: state.last_read_work.memo_rejections,
            last_transaction_prefix_memo_peak_entries: state.last_read_work.memo_peak_entries,
            last_transaction_prefix_memo_peak_bytes: state.last_read_work.memo_peak_retained_bytes,
            last_native_cursor_ranges: state.last_read_work.native_cursor_ranges,
            last_native_cache_hits: state.last_read_work.native_cache_hits,
            last_native_cache_misses: state.last_read_work.native_cache_misses,
            last_native_sql_root_reads: state.last_read_work.native_sql_root_reads,
            last_native_sql_directory_reads: state.last_read_work.native_sql_directory_reads,
            last_native_sql_leaf_reads: state.last_read_work.native_sql_leaf_reads,
            last_native_sql_reads: state.last_read_work.native_sql_reads,
            last_native_sql_read_bytes: state.last_read_work.native_sql_read_bytes,
            last_native_recent_datoms_examined: state.last_read_work.native_recent_datoms_examined,
            last_native_recent_datoms_yielded: state.last_read_work.native_recent_datoms_yielded,
            last_commitment_node_visits: state.last_commitment_work.node_visits,
            last_commitment_node_hashes: state.last_commitment_work.node_hashes,
            last_commitment_leaf_changes: state.last_commitment_work.leaf_changes,
            last_commitment_sql_node_reads: state.last_commitment_work.sql_node_reads,
            last_commitment_sql_node_read_bytes: state.last_commitment_work.sql_node_read_bytes,
            last_commitment_sql_node_writes: state.last_commitment_work.sql_node_writes,
            last_commitment_sql_node_write_bytes: state.last_commitment_work.sql_node_write_bytes,
            last_commitment_sql_coordinate_reads: state.last_commitment_work.sql_coordinate_reads,
            last_commitment_sql_coordinate_read_bytes: state
                .last_commitment_work
                .sql_coordinate_read_bytes,
            last_commitment_sql_coordinate_writes: state.last_commitment_work.sql_coordinate_writes,
            last_commitment_sql_coordinate_write_bytes: state
                .last_commitment_work
                .sql_coordinate_write_bytes,
        }
    }

    pub(crate) fn program_cache_handle(&self) -> SharedProgramCache {
        Arc::clone(&self.program_cache)
    }

    fn remember_receipt_read_core(&mut self, database_id: &str, core: WeakTieredReadHandle) {
        // Expired receipts must not leave an unbounded catalog of dead weak
        // entries. Live entries are proportional to independently retained
        // database cores; ordinary same-database commits do not walk the map.
        if self.receipt_read_cores.len() >= 64 && !self.receipt_read_cores.contains_key(database_id)
        {
            self.receipt_read_cores
                .retain(|_, core| core.upgrade().is_some());
        }
        self.receipt_read_cores.insert(database_id.to_owned(), core);
    }

    /// Read-only runtime compatibility gate. Schema installation is an
    /// explicit administrative action; a transactor never grants itself DDL
    /// authority while starting.
    pub fn verify_migrations(&mut self) -> Result<(), SemanticError> {
        verify_schema_compatibility(&mut self.client)
    }

    /// Replace a failed runtime connection without reusing potentially stale
    /// mutable recovery state. This never retries a transaction: callers must
    /// resolve an ambiguous write by request key before deciding what to do.
    pub fn reconnect(&mut self) -> Result<(), SemanticError> {
        let connection = self.connection.as_ref().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "postgres/reconnect-unconfigured",
                "this PostgreSQL store was created from a caller-owned client",
            )
        })?;
        let mut client = connection.connect_for("postgres/reconnect")?;
        verify_schema_compatibility(&mut client)?;
        self.client = client;
        self.current.clear();
        Ok(())
    }

    pub(crate) fn activate_transactor_state(
        &mut self,
        lease: &TransactorLease,
        lease_millis: u64,
    ) -> Result<crate::RecoveryStats, SemanticError> {
        let database_id = lease.database_id.clone();
        let connection = self.connection.clone().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "postgres/native-writer-unconfigured",
                "the native writer requires a configured PostgreSQL connection",
            )
        })?;
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .start()
            .map_err(|error| postgres_error("postgres/activation-begin", error))?;
        verify_lease(&mut transaction, lease, &database_id)?;
        let head = transaction
            .query_one(
                "SELECT basis_t, tx_hash, log_generation FROM atomic_heads \
                  WHERE database_id = $1 FOR UPDATE",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/activation-head", error))?;
        let target_t = pg_basis(head.get(0), "activation head")?;
        let target_hash = digest(head.get(1), "activation head hash")?;
        let generation = pg_basis(head.get(2), "activation log generation")?;
        let commitment =
            load_persistent_coordinate(&mut transaction, &database_id, generation, target_t)?
                .ok_or_else(|| {
                    fault(
                        "postgres/activation-semantic-root-missing",
                        "head has no exact v2 semantic commitment coordinate",
                    )
                })?;
        if commitment.tx_hash != target_hash {
            return Err(fault(
                "postgres/activation-coordinate-mismatch",
                "head transaction hash disagrees with its v2 semantic commitment",
            ));
        }
        let endpoint = exact_endpoint(&commitment);
        let (database, opened) = TieredSnapshot::open_writer_exact_configured(
            &connection,
            database_id.clone(),
            endpoint,
            None,
            self.capacity_limits.writer_tree_cache_entries,
            self.capacity_limits.writer_tree_cache_bytes,
            self.writer_recent_limits,
        )?;

        // Recovery may be longer than the normal heartbeat interval. The row
        // lock prevents takeover during recovery; extend this exact epoch
        // before releasing it and admitting requests.
        let epoch = sql_basis(lease.epoch)?;
        let lease_millis = sql_basis(lease_millis)?;
        let renewed = transaction
            .execute(
                "UPDATE atomic_transactor_leases \
                 SET expires_at = clock_timestamp() + \
                                  $4::bigint * interval '1 millisecond' \
                 WHERE lease_scope = $1 AND holder_id = $2 AND epoch = $3",
                &[&database_id, &lease.holder_id, &epoch, &lease_millis],
            )
            .map_err(|error| postgres_error("postgres/activation-renew", error))?;
        if renewed != 1 {
            return Err(leadership_lost(lease));
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/activation-commit", error))?;
        self.current.insert(
            database_id,
            WriterState {
                database,
                commitment,
                publication_revision: opened.selected_publication_revision,
                last_read_work: TransactionReadWork::default(),
                last_commitment_work: CommitmentWork::default(),
            },
        );
        Ok(crate::RecoveryStats {
            base_t: target_t.saturating_sub(opened.tail_transactions),
            target_t,
            tail_transactions: opened.tail_transactions,
            tail_range_reads: opened.tail_range_reads,
            rejected_manifests: opened.rejected_candidates,
        })
    }

    /// Adopt one index publication without changing the writer's exact
    /// logical endpoint. The old immutable value remains valid for reports;
    /// this only shortens the live writer's recent tail.
    pub(crate) fn adopt_published_tree(
        &mut self,
        database_id: &str,
        published_revision: u64,
    ) -> Result<(), SemanticError> {
        let Some(current) = self.current.get(database_id).cloned() else {
            return Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "postgres/writer-not-activated",
                "the native writer has no activated immutable value",
            ));
        };
        if published_revision <= current.publication_revision {
            return Ok(());
        }
        let revision = sql_basis(published_revision)?;
        let generation = sql_basis(current.commitment.generation)?;
        let row = self
            .client
            .query_opt(
                "SELECT manifest_hash FROM atomic_tree_publications \
                  WHERE database_id = $1 AND publication_revision = $2 \
                    AND log_generation = $3",
                &[&database_id, &revision, &generation],
            )
            .map_err(|error| postgres_error("postgres/writer-rebase-publication", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/writer-publication-not-found",
                    "the requested native publication does not exist in the writer generation",
                )
            })?;
        let manifest = digest(row.get::<_, Vec<u8>>(0), "writer publication manifest")?;
        let (database, opened) = current.database.rebase_exact(Some(manifest))?;
        if database.endpoint() != exact_endpoint(&current.commitment) {
            return Err(fault(
                "postgres/writer-rebase-endpoint",
                "rebasing changed the writer's immutable logical endpoint",
            ));
        }
        self.current.insert(
            database_id.to_owned(),
            WriterState {
                database,
                publication_revision: opened.selected_publication_revision,
                ..current
            },
        );
        Ok(())
    }

    pub(crate) fn acquire_lease(
        &mut self,
        database_id: &str,
        holder_id: &str,
        lease_millis: u64,
    ) -> Result<TransactorLease, SemanticError> {
        validate_lease_args(database_id, holder_id, lease_millis)?;
        let lease_millis = sql_basis(lease_millis)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/lease-acquire-begin", error))?;
        transaction
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended($1, 0))",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/lease-acquire-advisory-lock", error))?;
        if transaction
            .query_opt(
                "SELECT 1 FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/lease-database", error))?
            .is_none()
        {
            return Err(not_found(database_id));
        }
        let current = transaction
            .query_opt(
                "SELECT holder_id, epoch, expires_at > clock_timestamp() \
                 FROM atomic_transactor_leases WHERE lease_scope = $1 FOR UPDATE",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/lease-acquire-lock", error))?;
        let epoch = match current {
            None => {
                transaction
                    .execute(
                        "INSERT INTO atomic_transactor_leases \
                         (lease_scope, holder_id, epoch, expires_at) \
                         VALUES ($1, $2, 1, \
                                 clock_timestamp() + $3::bigint * interval '1 millisecond')",
                        &[&database_id, &holder_id, &lease_millis],
                    )
                    .map_err(|error| postgres_error("postgres/lease-acquire-insert", error))?;
                1
            }
            Some(row) => {
                let current_holder: String = row.get(0);
                let current_epoch: i64 = row.get(1);
                let live: bool = row.get(2);
                if live {
                    return Err(SemanticError::new(
                        ErrorCategory::Unavailable,
                        "postgres/lease-held",
                        format!(
                            "database {database_id} is already served by transactor {current_holder}"
                        ),
                    ));
                }
                let next_epoch = current_epoch.checked_add(1).ok_or_else(|| {
                    fault("postgres/lease-epoch-overflow", "lease epoch overflow")
                })?;
                transaction
                    .execute(
                        "UPDATE atomic_transactor_leases \
                         SET holder_id = $2, epoch = $3, \
                             expires_at = clock_timestamp() + \
                                          $4::bigint * interval '1 millisecond' \
                         WHERE lease_scope = $1",
                        &[&database_id, &holder_id, &next_epoch, &lease_millis],
                    )
                    .map_err(|error| postgres_error("postgres/lease-acquire-update", error))?;
                pg_basis(next_epoch, "lease epoch")?
            }
        };
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/lease-acquire-commit", error))?;
        Ok(TransactorLease {
            database_id: database_id.to_owned(),
            holder_id: holder_id.to_owned(),
            epoch,
        })
    }

    pub(crate) fn renew_lease(
        &mut self,
        lease: &TransactorLease,
        lease_millis: u64,
    ) -> Result<(), SemanticError> {
        validate_lease_args(&lease.database_id, &lease.holder_id, lease_millis)?;
        let lease_millis = sql_basis(lease_millis)?;
        let epoch = sql_basis(lease.epoch)?;
        let updated = self
            .client
            .execute(
                "UPDATE atomic_transactor_leases \
                 SET expires_at = clock_timestamp() + \
                                  $4::bigint * interval '1 millisecond' \
                 WHERE lease_scope = $1 AND holder_id = $2 AND epoch = $3 \
                   AND expires_at > clock_timestamp()",
                &[&lease.database_id, &lease.holder_id, &epoch, &lease_millis],
            )
            .map_err(|error| postgres_error("postgres/lease-renew", error))?;
        if updated == 1 {
            Ok(())
        } else {
            Err(leadership_lost(lease))
        }
    }

    pub(crate) fn release_lease(&mut self, lease: &TransactorLease) -> Result<(), SemanticError> {
        let epoch = sql_basis(lease.epoch)?;
        let updated = self
            .client
            .execute(
                "UPDATE atomic_transactor_leases SET expires_at = clock_timestamp() \
                 WHERE lease_scope = $1 AND holder_id = $2 AND epoch = $3",
                &[&lease.database_id, &lease.holder_id, &epoch],
            )
            .map_err(|error| postgres_error("postgres/lease-release", error))?;
        if updated == 1 {
            Ok(())
        } else {
            Err(leadership_lost(lease))
        }
    }

    pub fn create_database(
        &mut self,
        database_id: &str,
        schema: Schema,
    ) -> Result<Database, SemanticError> {
        if database_id.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-database-id",
                "database id cannot be empty",
            ));
        }
        let schema_ops: Vec<_> = schema
            .attributes()
            .cloned()
            .map(TxOp::InstallAttribute)
            .collect();
        let database = Database::new(schema)?;
        let bootstrap = Database::bootstrap()?;
        let encoded = encode_genesis(bootstrap.genesis_datoms())?;
        let genesis_hash = sha256(&encoded);
        let initial_envelope = if schema_ops.is_empty() {
            None
        } else {
            if database.basis_t() != 1 {
                return Err(fault(
                    "postgres/invalid-initial-schema-basis",
                    "initial application schema must occupy exactly transaction t=1",
                ));
            }
            let tx = crate::t_to_tx(1).expect("initial schema basis is representable");
            let envelope = DurableTransaction {
                database_id: database_id.to_owned(),
                basis_t: 1,
                previous_hash: genesis_hash,
                eidx_frontier: database.eidx_frontier(),
                tempids: BTreeMap::new(),
                tx_data: database
                    .datoms(crate::View::History, crate::IndexOrder::Eavt)
                    .into_iter()
                    .filter(|datom| datom.tx == tx)
                    .collect(),
            };
            let request_hash = request_digest(&schema_ops, 0, 0)?;
            let state_hash = checkpoint_state_hash(&database)?;
            Some((envelope, request_hash, state_hash))
        };
        let bootstrap_state_hash = checkpoint_state_hash(&bootstrap)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/create-begin", error))?;
        let lineage_id: String = transaction
            .query_one(
                "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
                 VALUES ($1, $2, $3) RETURNING lineage_id",
                &[&database_id, &&encoded[..], &&genesis_hash[..]],
            )
            .map_err(|error| postgres_error("postgres/create-catalog", error))?
            .get(0);
        let generation_i64 = 1_i64;
        transaction
            .execute(
                "INSERT INTO atomic_log_generations \
                     (database_id, generation, lineage_id, build_kind, request_count) \
                 VALUES ($1, $2, $3, 0, 0)",
                &[&database_id, &generation_i64, &lineage_id],
            )
            .map_err(|error| postgres_error("postgres/create-generation", error))?;
        let generation = 1;
        // Creation already materializes the complete bootstrap value. Seed
        // its PostgreSQL-owned semantic treap in this same transaction before
        // any generation/head row can become visible.
        crate::persistent_commitment::record_eager_endpoint(
            &mut transaction,
            database_id,
            generation,
            genesis_hash,
            bootstrap_state_hash,
            &bootstrap,
        )?;
        transaction
            .execute(
                "INSERT INTO atomic_heads (database_id, basis_t, tx_hash, log_generation) \
                 VALUES ($1, 0, $2, $3)",
                &[&database_id, &&genesis_hash[..], &generation_i64],
            )
            .map_err(|error| postgres_error("postgres/create-head", error))?;
        // A build-kind-zero generation is born active; it never passed through
        // COW staging and therefore has no retirement or source linkage.
        transaction
            .execute(
                "INSERT INTO atomic_log_generation_activations \
                     (database_id, generation, prior_generation, prior_basis_t, \
                      basis_t, head_hash, state_hash, manifest_hash) \
                 VALUES ($1, $2, 0, 0, 0, $3, $4, NULL)",
                &[
                    &database_id,
                    &generation_i64,
                    &&genesis_hash[..],
                    &&bootstrap_state_hash[..],
                ],
            )
            .map_err(|error| postgres_error("postgres/create-generation", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_log_generation_completions(database_id, generation) \
                 VALUES ($1, $2)",
                &[&database_id, &generation_i64],
            )
            .map_err(|error| postgres_error("postgres/create-generation-completion", error))?;
        if let Some((envelope, request_hash, state_hash)) = &initial_envelope {
            let content = LineageTransactionContent::from_transaction(
                &lineage_id,
                bootstrap.eidx_frontier(),
                envelope,
            )?;
            let payload = content.encode()?;
            if payload.len() > self.capacity_limits.max_transaction_bytes {
                return Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "postgres/transaction-byte-capacity",
                    "initial schema transaction exceeds the configured encoded-byte limit",
                ));
            }
            let content_hash = sha256(&payload);
            let tx_hash = generation_transaction_hash(
                &lineage_id,
                generation,
                1,
                genesis_hash,
                content_hash,
                *state_hash,
                envelope.eidx_frontier,
            )?;
            let idem_key = request_key_hash(&lineage_id, "__atomic/create-schema/v1")?;
            transaction
                .execute(
                    "INSERT INTO atomic_transaction_contents \
                         (content_hash, lineage_id, basis_t, \
                          eidx_frontier, payload) \
                     VALUES ($1, $2, 1, $3, $4)",
                    &[
                        &&content_hash[..],
                        &lineage_id,
                        &sql_basis(envelope.eidx_frontier)?,
                        &&payload[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/create-schema-content", error))?;
            transaction
                .execute(
                    "INSERT INTO atomic_generation_transactions \
                         (database_id, generation, basis_t, previous_hash, tx_hash, \
                          content_hash, state_hash, eidx_frontier) \
                     VALUES ($1, $2, 1, $3, $4, $5, $6, $7)",
                    &[
                        &database_id,
                        &generation_i64,
                        &&genesis_hash[..],
                        &&tx_hash[..],
                        &&content_hash[..],
                        &&state_hash[..],
                        &sql_basis(envelope.eidx_frontier)?,
                    ],
                )
                .map_err(|error| postgres_error("postgres/create-schema-transaction", error))?;
            transaction
                .execute(
                    "INSERT INTO atomic_generation_requests \
                         (database_id, generation, request_key_hash, request_digest, \
                          request_kind, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, 1, 1, $5)",
                    &[
                        &database_id,
                        &generation_i64,
                        &&idem_key[..],
                        &&request_hash[..],
                        &&tx_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/create-schema-request", error))?;
            crate::persistent_commitment::record_eager_endpoint(
                &mut transaction,
                database_id,
                generation,
                tx_hash,
                *state_hash,
                &database,
            )?;
            let updated = transaction
                .execute(
                    "UPDATE atomic_heads SET basis_t = 1, tx_hash = $1 \
                     WHERE database_id = $2 AND log_generation = $3 \
                       AND basis_t = 0 AND tx_hash = $4",
                    &[
                        &&tx_hash[..],
                        &database_id,
                        &generation_i64,
                        &&genesis_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/create-schema-publication", error))?;
            if updated != 1 {
                return Err(SemanticError::conflict(
                    "postgres/create-schema-cas-failed",
                    "new database head no longer identifies its exact genesis",
                ));
            }
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/create-commit", error))?;
        Ok(database)
    }

    pub fn recover(&mut self, database_id: &str) -> Result<Database, SemanticError> {
        let row = self
            .client
            .query_opt(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/recovery-head", error))?
            .ok_or_else(|| not_found(database_id))?;
        let basis = pg_basis(row.get::<_, i64>(0), "head")?;
        let hash = digest(row.get::<_, Vec<u8>>(1), "head transaction hash")?;
        Ok(recover_to(&mut self.client, database_id, basis, hash)?.database)
    }

    pub fn recover_basis(
        &mut self,
        database_id: &str,
        basis_t: u64,
    ) -> Result<Database, SemanticError> {
        let head = self
            .client
            .query_opt(
                "SELECT h.log_generation, d.genesis_hash \
                   FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
                  WHERE h.database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
            .ok_or_else(|| not_found(database_id))?;
        let generation = pg_basis(head.get(0), "head log generation")?;
        let hash = if basis_t == 0 {
            digest(head.get::<_, Vec<u8>>(1), "genesis hash")?
        } else {
            let basis = sql_basis(basis_t)?;
            let generation_sql = sql_basis(generation)?;
            let row = if generation == 0 {
                self.client
                    .query_opt(
                        "SELECT tx_hash FROM atomic_transactions \
                         WHERE database_id = $1 AND basis_t = $2",
                        &[&database_id, &basis],
                    )
                    .map_err(|error| postgres_error("postgres/recovery-basis-hash", error))?
            } else {
                self.client
                    .query_opt(
                        "SELECT tx_hash FROM atomic_generation_transactions \
                         WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
                        &[&database_id, &generation_sql, &basis],
                    )
                    .map_err(|error| {
                        postgres_error("postgres/recovery-generation-basis-hash", error)
                    })?
            }
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/basis-not-found",
                    format!("database {database_id} has no basis {basis_t}"),
                )
            })?;
            digest(row.get::<_, Vec<u8>>(0), "basis transaction hash")?
        };
        Ok(
            recover_generation_to(&mut self.client, database_id, generation, basis_t, hash)?
                .database,
        )
    }

    /// Resolve the durable decision for one admitted request without
    /// resubmitting or re-evaluating its transaction data.
    ///
    /// `None` means that no committed decision for this key is visible in the
    /// database. A returned receipt is reconstructed from the immutable log
    /// transaction named by the request record, including its exact
    /// `db-before`; it is therefore safe to use after an acknowledgement
    /// timeout without guessing the server-selected basis or transaction time.
    pub(crate) fn resolve_request_outcome(
        &mut self,
        database_id: &str,
        request_key: &str,
    ) -> Result<Option<CommitReceipt>, SemanticError> {
        if request_key.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-request-key",
                "idempotency request key cannot be empty",
            ));
        }
        let connection = self.connection.clone().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "postgres/native-writer-unconfigured",
                "exact request reconstruction requires a configured PostgreSQL connection",
            )
        })?;
        let cached = self.current.get(database_id).cloned();
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/request-outcome-begin", error))?;
        // The authoritative writer holds FOR UPDATE on this row through both
        // request binding and head publication. Waiting on the same row makes
        // an absent outcome definitive with respect to the ambiguous attempt,
        // rather than observing a snapshot taken while its commit is in flight.
        let head = transaction
            .query_opt(
                "SELECT h.basis_t, h.tx_hash, h.log_generation, d.lineage_id \
                   FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
                  WHERE h.database_id = $1 FOR SHARE OF h",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/request-outcome-database", error))?;
        let Some(head) = head else {
            transaction
                .commit()
                .map_err(|error| postgres_error("postgres/request-outcome-commit", error))?;
            return Err(not_found(database_id));
        };
        let head_basis = pg_basis(head.get(0), "head basis")?;
        let head_hash = digest(head.get::<_, Vec<u8>>(1), "head transaction hash")?;
        let generation = pg_basis(head.get(2), "head log generation")?;
        let lineage_id: String = head.get(3);
        let cached =
            cached.filter(|state| state.database.read_handle().lineage_id() == lineage_id.as_str());
        let shared_read_core = cached
            .as_ref()
            .map(|state| state.database.read_handle())
            .or_else(|| self.receipt_read_cores.get(database_id)?.upgrade())
            .filter(|core| core.lineage_id() == lineage_id.as_str());
        let generation_sql = sql_basis(generation)?;
        let head_commitment =
            load_persistent_coordinate(&mut transaction, database_id, generation, head_basis)?
                .ok_or_else(|| {
                    fault(
                        "postgres/request-outcome-head-root-missing",
                        "head has no exact v2 semantic commitment coordinate",
                    )
                })?;
        if head_commitment.tx_hash != head_hash {
            return Err(fault(
                "postgres/request-outcome-head-coordinate",
                "head transaction hash disagrees with its semantic commitment",
            ));
        }
        let key_hash = request_key_hash(&lineage_id, request_key)?;
        let row = if generation == 0 {
            transaction.query_opt(
                "SELECT basis_t, tx_hash, 1::smallint FROM atomic_requests \
                 WHERE database_id = $1 AND request_key = $2",
                &[&database_id, &request_key],
            )
        } else {
            transaction.query_opt(
                "SELECT basis_t, tx_hash, request_kind FROM atomic_generation_requests \
                 WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3",
                &[&database_id, &generation_sql, &&key_hash[..]],
            )
        }
        .map_err(|error| postgres_error("postgres/request-outcome-read", error))?;
        let Some(row) = row else {
            transaction
                .commit()
                .map_err(|error| postgres_error("postgres/request-outcome-commit", error))?;
            return Ok(None);
        };
        if row.get::<_, i16>(2) == 0 {
            return Err(SemanticError::conflict(
                "postgres/idempotency-predates-excision",
                "request key was committed before excision, but its original receipt was deliberately erased",
            ));
        }
        let basis = pg_basis(row.get::<_, i64>(0), "request outcome")?;
        let hash = digest(row.get::<_, Vec<u8>>(1), "request transaction hash")?;
        let (receipt, replay_state) = reconstruct_exact_request_receipt(
            &mut transaction,
            &connection,
            database_id,
            generation,
            basis,
            hash,
            row.get::<_, i16>(2),
            key_hash,
            self.capacity_limits.writer_tree_cache_entries,
            self.capacity_limits.writer_tree_cache_bytes,
            self.writer_recent_limits,
            shared_read_core.as_ref(),
        )?;
        let receipt_read_core = replay_state.database.read_handle().downgrade();
        let live_head_state = if basis == head_basis && hash == head_hash {
            select_freshest_head_writer_state(cached, replay_state, &head_commitment)?
        } else {
            None
        };
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/request-outcome-commit", error))?;
        self.remember_receipt_read_core(database_id, receipt_read_core);
        if let Some(live_head_state) = live_head_state {
            self.current.insert(database_id.to_owned(), live_head_state);
        }
        Ok(Some(receipt))
    }

    /// Upload immutable native program content. This is preparation only:
    /// selection/"activation" is an ordinary transaction asserting the
    /// returned hash as a `Value::Function` at `:db/fn`.
    pub fn deploy_program_blob(&mut self, program: &Program) -> Result<ProgramHash, SemanticError> {
        let encoded = encode_program(program)?;
        let hash = sha256(&encoded);
        let kind = program_kind_i16(program.kind);
        let arity = i16::from(program.arity);
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/program-deploy-begin", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_programs (program_hash, kind, arity, payload) \
                 VALUES ($1, $2, $3, $4) ON CONFLICT (program_hash) DO NOTHING",
                &[&&hash[..], &kind, &arity, &&encoded[..]],
            )
            .map_err(|error| postgres_error("postgres/program-insert", error))?;
        let row = transaction
            .query_one(
                "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-verify", error))?;
        let stored_kind: i16 = row.get(0);
        let stored_arity: i16 = row.get(1);
        let stored_payload: Vec<u8> = row.get(2);
        if stored_kind != kind || stored_arity != arity || stored_payload != encoded {
            return Err(fault(
                "postgres/program-hash-collision",
                "stored program hash resolves to different metadata or bytes",
            ));
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/program-deploy-commit", error))?;
        self.cache_program(hash, program.clone(), program_cache_weight(encoded.len()));
        Ok(hash)
    }

    pub fn resolve_program(&mut self, hash: ProgramHash) -> Result<Program, SemanticError> {
        let cache = Arc::clone(&self.program_cache);
        let program = resolve_program_in(&mut self.client, &cache, hash)?;
        Ok(program.program().clone())
    }

    fn cache_program(&self, hash: ProgramHash, program: Program, weight: usize) {
        let program = Arc::new(ValidatedProgram::from_canonical(program));
        let mut cache = lock_program_cache(&self.program_cache);
        cache.record_validation();
        cache.insert(hash, program, weight);
    }

    #[allow(clippy::too_many_arguments)]
    #[cfg_attr(test, allow(dead_code))]
    pub(crate) fn transact_authoritative_fenced(
        &mut self,
        lease: &TransactorLease,
        database_id: &str,
        request_key: &str,
        compare_basis_t: Option<u64>,
        forms: &[TxForm],
        tx_instant_override: Option<i64>,
        request_hash: Digest,
    ) -> Result<CommitReceipt, SemanticError> {
        self.transact_authoritative_fenced_at(
            lease,
            database_id,
            request_key,
            compare_basis_t,
            forms,
            tx_instant_override,
            request_hash,
            CommitFault::None,
        )
    }

    /// Exercise the real fenced/form-expanding authority path at a
    /// deterministic publication boundary. This is deliberately test-only:
    /// production callers may observe an unknown outcome but cannot request
    /// one.
    #[cfg(test)]
    #[allow(clippy::too_many_arguments)]
    pub(crate) fn transact_authoritative_fenced_with_fault(
        &mut self,
        lease: &TransactorLease,
        database_id: &str,
        request_key: &str,
        compare_basis_t: Option<u64>,
        forms: &[TxForm],
        tx_instant_override: Option<i64>,
        request_hash: Digest,
        fault_point: CommitFault,
    ) -> Result<CommitReceipt, SemanticError> {
        self.transact_authoritative_fenced_at(
            lease,
            database_id,
            request_key,
            compare_basis_t,
            forms,
            tx_instant_override,
            request_hash,
            fault_point,
        )
    }

    #[allow(clippy::too_many_arguments)]
    fn transact_authoritative_fenced_at(
        &mut self,
        lease: &TransactorLease,
        database_id: &str,
        request_key: &str,
        compare_basis_t: Option<u64>,
        forms: &[TxForm],
        tx_instant_override: Option<i64>,
        request_hash: Digest,
        fault_point: CommitFault,
    ) -> Result<CommitReceipt, SemanticError> {
        let max_primitive_ops = self.capacity_limits.max_transaction_ops;
        self.transact_generated(
            database_id,
            request_key,
            compare_basis_t,
            tx_instant_override,
            request_hash,
            fault_point,
            Some(lease),
            None,
            |transaction, db_before, shared_budget, program_cache| {
                let mut budget = shared_budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                let forms = expand_submission_forms(
                    &mut |hash| resolve_program_in(transaction, program_cache, hash),
                    db_before,
                    forms,
                    &mut budget,
                )?;
                db_before.normalize_persisted_forms_with_limit(&forms, max_primitive_ops)
            },
        )
    }

    /// Deterministic publication kill points used by real-PostgreSQL tests.
    /// Pre-commit points return an injected failure and rely on transaction
    /// drop rollback. The final point commits, then reports UnknownOutcome.
    #[cfg(test)]
    pub(crate) fn transact_with_fault(
        &mut self,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        ops: &[TxOp],
        tx_instant: i64,
        fault_point: CommitFault,
    ) -> Result<CommitReceipt, SemanticError> {
        let request_hash = request_digest(ops, tx_instant, expected_basis_t)?;
        self.transact_generated(
            database_id,
            request_key,
            Some(expected_basis_t),
            Some(tx_instant),
            request_hash,
            fault_point,
            None,
            None,
            |_, _, _, _| Ok(ops.to_vec()),
        )
    }

    #[allow(clippy::too_many_arguments)]
    fn transact_generated<F>(
        &mut self,
        database_id: &str,
        request_key: &str,
        compare_basis_t: Option<u64>,
        tx_instant_override: Option<i64>,
        request_hash: Digest,
        fault_point: CommitFault,
        lease: Option<&TransactorLease>,
        functions: Option<&TxFunctions>,
        generate: F,
    ) -> Result<CommitReceipt, SemanticError>
    where
        F: FnOnce(
            &mut postgres::Transaction<'_>,
            &DatabaseValue,
            &SharedProgramBudget,
            &SharedProgramCache,
        ) -> Result<Vec<TxOp>, SemanticError>,
    {
        if request_key.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-request-key",
                "idempotency request key cannot be empty",
            ));
        }
        let cached = self.current.get(database_id).cloned();
        let connection = self.connection.clone().ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "postgres/native-writer-unconfigured",
                "the native writer requires a configured PostgreSQL connection",
            )
        })?;
        let program_cache = Arc::clone(&self.program_cache);
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/transact-begin", error))?;
        if let Some(lease) = lease {
            verify_lease(&mut transaction, lease, database_id)?;
        }
        // Program GC takes SHARE on the authoritative log before deriving
        // temporal :db/fn reachability. Acquire the compatible writer mode
        // before resolving any new binding so GC either observes the committed
        // reference or completes before this attempt validates the blob. The
        // mode remains compatible with writers for other databases.
        transaction
            .batch_execute(
                "LOCK TABLE atomic_transactions IN ROW EXCLUSIVE MODE; \
                 LOCK TABLE atomic_generation_transactions IN ROW EXCLUSIVE MODE",
            )
            .map_err(|error| postgres_error("postgres/transact-log-lock", error))?;

        let head = transaction
            .query_opt(
                "SELECT h.basis_t, h.tx_hash, h.log_generation, d.lineage_id \
                   FROM atomic_heads h JOIN atomic_databases d USING (database_id) \
                  WHERE h.database_id = $1 FOR UPDATE OF h",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/transact-lock-head", error))?
            .ok_or_else(|| not_found(database_id))?;
        let head_basis_i64: i64 = head.get(0);
        let head_basis = pg_basis(head_basis_i64, "head")?;
        let head_hash = digest(head.get::<_, Vec<u8>>(1), "head transaction hash")?;
        let log_generation_i64: i64 = head.get(2);
        let log_generation = pg_basis(log_generation_i64, "head log generation")?;
        let lineage_id: String = head.get(3);
        let cached =
            cached.filter(|state| state.database.read_handle().lineage_id() == lineage_id.as_str());
        let shared_read_core = cached
            .as_ref()
            .map(|state| state.database.read_handle())
            .or_else(|| self.receipt_read_cores.get(database_id)?.upgrade())
            .filter(|core| core.lineage_id() == lineage_id.as_str());
        let idem_key_hash = request_key_hash(&lineage_id, request_key)?;
        let head_commitment =
            load_persistent_coordinate(&mut transaction, database_id, log_generation, head_basis)?
                .ok_or_else(|| {
                    fault(
                        "postgres/head-semantic-root-missing",
                        "the locked head has no exact v2 semantic commitment coordinate",
                    )
                })?;
        if head_commitment.tx_hash != head_hash {
            return Err(fault(
                "postgres/head-coordinate-mismatch",
                "the locked head disagrees with its v2 semantic commitment coordinate",
            ));
        }

        let existing_request = if log_generation == 0 {
            transaction.query_opt(
                "SELECT request_digest, basis_t, tx_hash, 1::smallint \
                   FROM atomic_requests \
                  WHERE database_id = $1 AND request_key = $2",
                &[&database_id, &request_key],
            )
        } else {
            transaction.query_opt(
                "SELECT request_digest, basis_t, tx_hash, request_kind \
                   FROM atomic_generation_requests \
                  WHERE database_id = $1 AND generation = $2 \
                    AND request_key_hash = $3",
                &[&database_id, &log_generation_i64, &&idem_key_hash[..]],
            )
        }
        .map_err(|error| postgres_error("postgres/idempotency-read", error))?;
        if let Some(row) = existing_request {
            if row.get::<_, i16>(3) == 0 {
                return Err(SemanticError::conflict(
                    "postgres/idempotency-predates-excision",
                    "request key was committed before excision, but its original request digest and receipt were deliberately erased",
                ));
            }
            let stored_request = digest(row.get::<_, Vec<u8>>(0), "request digest")?;
            if stored_request != request_hash {
                return Err(SemanticError::conflict(
                    "postgres/idempotency-key-reused",
                    "idempotency key is already bound to a different request",
                ));
            }
            let basis = pg_basis(row.get::<_, i64>(1), "request outcome")?;
            let hash = digest(row.get::<_, Vec<u8>>(2), "request transaction hash")?;
            let (receipt, replay_state) = reconstruct_exact_request_receipt(
                &mut transaction,
                &connection,
                database_id,
                log_generation,
                basis,
                hash,
                row.get::<_, i16>(3),
                idem_key_hash,
                self.capacity_limits.writer_tree_cache_entries,
                self.capacity_limits.writer_tree_cache_bytes,
                self.writer_recent_limits,
                shared_read_core.as_ref(),
            )?;
            let receipt_read_core = replay_state.database.read_handle().downgrade();
            let live_head_state = if basis == head_basis && hash == head_hash {
                select_freshest_head_writer_state(cached, replay_state, &head_commitment)?
            } else {
                None
            };
            if transaction.commit().is_err() {
                self.current.remove(database_id);
                return Err(unknown_outcome(
                    idem_key_hash,
                    "outcome-read",
                    "PostgreSQL did not acknowledge the idempotent outcome read",
                ));
            }
            if fault_point == CommitFault::AfterCommitBeforeResponse {
                self.current.remove(database_id);
                return Err(unknown_outcome(
                    idem_key_hash,
                    "outcome-read",
                    "injected acknowledgment loss after idempotent outcome read",
                ));
            }
            self.remember_receipt_read_core(database_id, receipt_read_core);
            if let Some(live_head_state) = live_head_state {
                self.current.insert(database_id.to_owned(), live_head_state);
            }
            return Ok(receipt);
        }

        // Generation zero is readable for migration and old idempotent
        // outcomes, but it has no request-base binding relation. Do not create
        // new receipts whose exact db-before could later be retired.
        if log_generation == 0 {
            return Err(SemanticError::new(
                ErrorCategory::Unsupported,
                "postgres/native-writer-requires-generation",
                "new native writer transactions require a positive log generation",
            ));
        }

        if let Some(expected_basis_t) = compare_basis_t {
            let expected_basis = sql_basis(expected_basis_t)?;
            if head_basis_i64 != expected_basis {
                return Err(SemanticError::conflict(
                    "postgres/stale-basis",
                    format!("expected basis {expected_basis_t}, current basis is {head_basis}"),
                ));
            }
        }

        if head_basis >= self.capacity_limits.max_history_transactions {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "postgres/history-capacity",
                "configured transaction-history capacity has been reached",
            ));
        }

        let shared_snapshot = cached.as_ref().map(|state| state.database.clone());
        let writer_before = match cached {
            Some(state)
                if state.commitment == head_commitment
                    && state.database.endpoint() == exact_endpoint(&head_commitment) =>
            {
                state
            }
            _ => open_exact_state(
                &connection,
                database_id,
                head_commitment.clone(),
                None,
                self.capacity_limits.writer_tree_cache_entries,
                self.capacity_limits.writer_tree_cache_bytes,
                self.writer_recent_limits,
                shared_snapshot.as_ref(),
                true,
            )?,
        };
        let db_before_snapshot = writer_before.database.clone();
        let db_before = db_before_snapshot.database_value();
        let read_context = Arc::new(TransactionReadContext::new(
            self.capacity_limits.max_transaction_read_datoms,
            self.capacity_limits.max_transaction_read_bytes,
        ));
        let observed_db_before = db_before
            .clone()
            .with_transaction_read_context(Arc::clone(&read_context));
        let shared_budget = Arc::new(Mutex::new(ProgramBudget::new(
            self.capacity_limits.program.control(),
        )?));
        let ops = generate(
            &mut transaction,
            &observed_db_before,
            &shared_budget,
            &program_cache,
        )?;
        let server_now = postgres_now_millis(&mut transaction)?;
        let tx_instant =
            select_tx_instant(&observed_db_before, server_now, tx_instant_override, &ops)?;
        if ops.len() > self.capacity_limits.max_transaction_ops {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "postgres/transaction-op-capacity",
                "transaction exceeds the configured operation limit",
            ));
        }
        let remaining = read_context.remaining()?;
        let mut assessed = assess_tiered_with_remaining_limits(
            &observed_db_before,
            &ops,
            tx_instant,
            AssessmentLimits {
                max_read_datoms: remaining.datoms,
                max_read_bytes: remaining.retained_bytes,
            },
        )?;
        assessed.db_before = assessed
            .db_before
            .with_transaction_read_context(Arc::clone(&read_context));
        assessed.db_after = assessed
            .db_after
            .with_transaction_read_context(Arc::clone(&read_context));
        validate_successor_program_bindings(
            &mut |hash| resolve_program_in(&mut transaction, &program_cache, hash),
            &assessed.db_before,
            &assessed.db_after,
            &assessed.tx_data,
        )?;
        let persisted_functions;
        let functions = match functions {
            Some(functions) => Some(functions),
            None => {
                persisted_functions = persisted_predicates(
                    &mut |hash| resolve_program_in(&mut transaction, &program_cache, hash),
                    &assessed.db_before,
                    &assessed.predicate_requirements()?,
                    Arc::clone(&shared_budget),
                )?;
                Some(&persisted_functions)
            }
        };
        assessed.validate_exact(functions)?;
        let semantic_changes = exact_semantic_changes(&assessed.db_before, &assessed.tx_data)?;
        let (next_root, mut commitment_work) = advance_persistent_commitment(
            &mut transaction,
            head_commitment.root,
            &semantic_changes,
        )?;
        let state_hash = next_root.state_hash(assessed.basis_t, assessed.eidx_frontier);
        let envelope = DurableTransaction {
            database_id: database_id.to_owned(),
            basis_t: assessed.basis_t,
            previous_hash: head_hash,
            eidx_frontier: assessed.eidx_frontier,
            tempids: assessed.tempids.clone(),
            tx_data: assessed.tx_data.clone(),
        };
        let (payload, content_hash, tx_hash) = if log_generation == 0 {
            let payload = encode_transaction(&envelope)?;
            let tx_hash = transaction_hash(&payload);
            (payload, None, tx_hash)
        } else {
            let content = LineageTransactionContent::from_transaction(
                &lineage_id,
                db_before.eidx_frontier(),
                &envelope,
            )?;
            let payload = content.encode()?;
            let content_hash = sha256(&payload);
            let tx_hash = generation_transaction_hash(
                &lineage_id,
                log_generation,
                envelope.basis_t,
                head_hash,
                content_hash,
                state_hash,
                envelope.eidx_frontier,
            )?;
            (payload, Some(content_hash), tx_hash)
        };
        if payload.len() > self.capacity_limits.max_transaction_bytes {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "postgres/transaction-byte-capacity",
                "transaction exceeds the configured encoded-byte limit",
            ));
        }
        let next_basis = sql_basis(envelope.basis_t)?;
        let successor = db_before_snapshot.authenticated_successor(
            tx_hash,
            state_hash,
            envelope.clone(),
            &assessed.successor_schema,
            read_context.as_ref(),
        )?;
        if successor.endpoint()
            != (ExactEndpoint {
                generation: log_generation,
                basis_t: envelope.basis_t,
                tx_hash,
                state_hash,
                eidx_frontier: envelope.eidx_frontier,
            })
        {
            return Err(fault(
                "postgres/successor-coordinate",
                "the prepared native successor disagrees with its transaction coordinate",
            ));
        }
        let next_commitment = PersistentCommitmentCoordinate {
            database_id: database_id.to_owned(),
            generation: log_generation,
            basis_t: envelope.basis_t,
            tx_hash,
            state_hash,
            eidx_frontier: envelope.eidx_frontier,
            root: next_root,
        };
        let request_base_manifest = if log_generation == 0 {
            None
        } else {
            Some(db_before_snapshot.durable_manifest_hash().ok_or_else(|| {
                fault(
                    "postgres/native-request-base-missing",
                    "a native generation commit requires an authenticated durable db-before base",
                )
            })?)
        };

        if fault_point == CommitFault::BeforeTransactionInsert {
            return Err(injected("before transaction insert"));
        }
        if log_generation == 0 {
            transaction
                .execute(
                    "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                     VALUES ($1, $2, $3, $4, $5, $6)",
                    &[
                        &database_id,
                        &next_basis,
                        &&head_hash[..],
                        &&tx_hash[..],
                        &&payload[..],
                        &&state_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/transaction-insert", error))?;
        } else {
            let content_hash = content_hash.expect("positive generation has canonical content");
            transaction
                .execute(
                    "INSERT INTO atomic_transaction_contents \
                         (content_hash, lineage_id, basis_t, \
                          eidx_frontier, payload) \
                     VALUES ($1, $2, $3, $4, $5) \
                     ON CONFLICT (content_hash) DO NOTHING",
                    &[
                        &&content_hash[..],
                        &lineage_id,
                        &next_basis,
                        &sql_basis(envelope.eidx_frontier)?,
                        &&payload[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/transaction-content-insert", error))?;
            let stored = transaction
                .query_one(
                    "SELECT lineage_id, basis_t, eidx_frontier, \
                            envelope_version, payload \
                       FROM atomic_transaction_contents WHERE content_hash = $1",
                    &[&&content_hash[..]],
                )
                .map_err(|error| postgres_error("postgres/transaction-content-verify", error))?;
            if stored.get::<_, String>(0) != lineage_id
                || stored.get::<_, i64>(1) != next_basis
                || pg_basis(stored.get(2), "stored content frontier")? != envelope.eidx_frontier
                || stored.get::<_, i16>(3) != 1
                || stored.get::<_, Vec<u8>>(4) != payload
            {
                return Err(fault(
                    "postgres/transaction-content-collision",
                    "content hash resolves to different transaction metadata or bytes",
                ));
            }
            transaction
                .execute(
                    "INSERT INTO atomic_generation_transactions \
                         (database_id, generation, basis_t, previous_hash, tx_hash, \
                          content_hash, state_hash, eidx_frontier) \
                     VALUES ($1, $2, $3, $4, $5, $6, $7, $8)",
                    &[
                        &database_id,
                        &log_generation_i64,
                        &next_basis,
                        &&head_hash[..],
                        &&tx_hash[..],
                        &&content_hash[..],
                        &&state_hash[..],
                        &sql_basis(envelope.eidx_frontier)?,
                    ],
                )
                .map_err(|error| postgres_error("postgres/generation-transaction-insert", error))?;
        }
        if fault_point == CommitFault::AfterTransactionInsert {
            return Err(injected("after transaction insert"));
        }
        if log_generation == 0 {
            transaction
                .execute(
                    "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, $5)",
                    &[
                        &database_id,
                        &request_key,
                        &&request_hash[..],
                        &next_basis,
                        &&tx_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/idempotency-insert", error))?;
        } else {
            transaction
                .execute(
                    "INSERT INTO atomic_generation_requests \
                         (database_id, generation, request_key_hash, request_digest, \
                          request_kind, basis_t, tx_hash) \
                     VALUES ($1, $2, $3, $4, 2, $5, $6)",
                    &[
                        &database_id,
                        &log_generation_i64,
                        &&idem_key_hash[..],
                        &&request_hash[..],
                        &next_basis,
                        &&tx_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/generation-idempotency-insert", error))?;
            let request_base_manifest =
                request_base_manifest.expect("positive generation has a durable native base");
            transaction
                .execute(
                    "INSERT INTO atomic_generation_request_bases \
                         (database_id, generation, request_key_hash, base_manifest_hash) \
                     VALUES ($1, $2, $3, $4)",
                    &[
                        &database_id,
                        &log_generation_i64,
                        &&idem_key_hash[..],
                        &&request_base_manifest[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/request-base-insert", error))?;
            for (name, entity) in &envelope.tempids {
                transaction
                    .execute(
                        "INSERT INTO atomic_generation_request_tempids \
                             (database_id, generation, request_key_hash, tempid_name, entity_id) \
                         VALUES ($1, $2, $3, $4, $5)",
                        &[
                            &database_id,
                            &log_generation_i64,
                            &&idem_key_hash[..],
                            name,
                            &sql_basis(*entity)?,
                        ],
                    )
                    .map_err(|error| postgres_error("postgres/request-tempid-insert", error))?;
            }
            insert_program_generation_refs(
                &mut transaction,
                database_id,
                log_generation,
                &envelope.tx_data,
            )?;
        }
        let coordinate_work = record_persistent_coordinate(&mut transaction, &next_commitment)?;
        commitment_work.absorb(coordinate_work);
        let updated = transaction
            .execute(
                "UPDATE atomic_heads SET basis_t = $1, tx_hash = $2 \
                 WHERE database_id = $3 AND log_generation = $4 \
                   AND basis_t = $5 AND tx_hash = $6",
                &[
                    &next_basis,
                    &&tx_hash[..],
                    &database_id,
                    &log_generation_i64,
                    &head_basis_i64,
                    &&head_hash[..],
                ],
            )
            .map_err(|error| postgres_error("postgres/head-publication", error))?;
        if updated != 1 {
            return Err(SemanticError::conflict(
                "postgres/head-cas-failed",
                "database head no longer matches db-before",
            ));
        }
        if fault_point == CommitFault::AfterHeadUpdate {
            return Err(injected("after head update"));
        }
        if fault_point == CommitFault::AfterHeadUpdateProcessAbort {
            std::process::abort();
        }

        // Construct every report and process-local successor object before
        // publication. After PostgreSQL acknowledges the commit, installing
        // this already-built immutable state is infallible.
        let receipt = CommitReceipt {
            db_before: db_before.clone().without_transaction_read_context(),
            database: successor
                .database_value()
                .without_transaction_read_context(),
            basis_t: envelope.basis_t,
            tx_hash,
            tempids: envelope.tempids.clone(),
            tx_data: envelope.tx_data.clone(),
            replayed: false,
        };
        let next_writer = WriterState {
            database: successor,
            commitment: next_commitment,
            publication_revision: writer_before.publication_revision,
            last_read_work: read_context.snapshot()?,
            last_commitment_work: commitment_work,
        };
        if transaction.commit().is_err() {
            self.current.remove(database_id);
            return Err(unknown_outcome(
                idem_key_hash,
                "publication",
                "PostgreSQL did not acknowledge the transaction commit",
            ));
        }
        if fault_point == CommitFault::AfterCommitBeforeResponse {
            self.current.remove(database_id);
            return Err(unknown_outcome(
                idem_key_hash,
                "publication",
                "injected acknowledgment loss after PostgreSQL commit",
            ));
        }
        self.remember_receipt_read_core(
            database_id,
            next_writer.database.read_handle().downgrade(),
        );
        self.current.insert(database_id.to_owned(), next_writer);
        Ok(receipt)
    }
}

pub(crate) struct Recovered {
    pub(crate) database: Database,
    pub(crate) final_hash: Digest,
}

#[derive(Clone, Debug)]
pub(crate) struct AuthenticatedLogTransaction {
    pub(crate) transaction: DurableTransaction,
    pub(crate) tx_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) payload: Vec<u8>,
    pub(crate) content_hash: Option<Digest>,
    pub(crate) legacy_request_key: Option<String>,
    pub(crate) request_key_hash: Option<Digest>,
    pub(crate) request_digest: Digest,
    /// Only COW tombstones may require replay with a removed predecessor
    /// assertion. Ordinary commits remain strict even in a positive generation.
    pub(crate) excision_replay: bool,
}

/// Authenticate one contiguous range in an explicitly named log generation.
/// This is the shared reader for peer, indexer, recovery, backup, and
/// operations paths; consumers should not reinterpret ATLC as a legacy ATMC
/// envelope or reproduce membership hashing locally.
pub(crate) fn read_authenticated_log_range<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    after_basis: u64,
    through_basis: u64,
    predecessor_hash: Digest,
) -> Result<Vec<AuthenticatedLogTransaction>, SemanticError> {
    let mut output = Vec::new();
    visit_authenticated_log_range(
        client,
        database_id,
        generation,
        after_basis,
        through_basis,
        predecessor_hash,
        |transaction| {
            output.push(transaction);
            Ok(())
        },
    )?;
    Ok(output)
}

/// Stream one authenticated range through a caller-owned bounded consumer.
/// This keeps writer activation to one PostgreSQL range stream and permits a
/// hard-capacity rejection after one decoded transaction, without first
/// retaining or rereading the complete tail.
pub(crate) fn visit_authenticated_log_range<C, F>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    after_basis: u64,
    through_basis: u64,
    predecessor_hash: Digest,
    mut visit: F,
) -> Result<u64, SemanticError>
where
    C: GenericClient,
    F: FnMut(AuthenticatedLogTransaction) -> Result<(), SemanticError>,
{
    if through_basis < after_basis {
        return Err(fault(
            "recovery/range-order",
            "transaction range ends before its starting basis",
        ));
    }
    let lineage_id: String = client
        .query_opt(
            "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/log-range-catalog", error))?
        .ok_or_else(|| not_found(database_id))?
        .get(0);
    let after = sql_basis(after_basis)?;
    let through = sql_basis(through_basis)?;
    let generation_sql = sql_basis(generation)?;
    let mut rows = if generation == 0 {
        client
            .query_raw(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, t.payload, \
                        t.state_hash, NULL::bytea, NULL::bigint, NULL::text, \
                        1::smallint, r.request_key, NULL::bytea, r.request_digest \
                   FROM atomic_transactions t \
                   JOIN atomic_requests r \
                     ON r.database_id = t.database_id AND r.basis_t = t.basis_t \
                    AND r.tx_hash = t.tx_hash \
                  WHERE t.database_id = $1 AND t.basis_t > $2 AND t.basis_t <= $3 \
                  ORDER BY t.basis_t",
                [
                    &database_id as &(dyn postgres::types::ToSql + Sync),
                    &after,
                    &through,
                ],
            )
            .map_err(|error| postgres_error("postgres/log-range-legacy", error))?
    } else {
        client
            .query_raw(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, c.payload, \
                        t.state_hash, t.content_hash, t.eidx_frontier, c.lineage_id, \
                        r.request_kind, NULL::text, r.request_key_hash, r.request_digest \
                   FROM atomic_generation_transactions t \
                   JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
                   JOIN atomic_generation_requests r \
                     ON r.database_id = t.database_id AND r.generation = t.generation \
                    AND r.basis_t = t.basis_t AND r.tx_hash = t.tx_hash \
                  WHERE t.database_id = $1 AND t.generation = $2 \
                    AND t.basis_t > $3 AND t.basis_t <= $4 ORDER BY t.basis_t",
                [
                    &database_id as &(dyn postgres::types::ToSql + Sync),
                    &generation_sql,
                    &after,
                    &through,
                ],
            )
            .map_err(|error| postgres_error("postgres/log-range-generation", error))?
    };
    let mut expected_basis = after_basis;
    let mut expected_previous = predecessor_hash;
    let mut visited = 0_u64;
    while let Some(row) = rows
        .next()
        .map_err(|error| postgres_error("postgres/log-range-stream", error))?
    {
        expected_basis = expected_basis
            .checked_add(1)
            .ok_or_else(|| fault("recovery/basis-overflow", "transaction basis overflow"))?;
        let basis = pg_basis(row.get(0), "transaction range basis")?;
        let stored_previous = digest(row.get(1), "transaction range predecessor")?;
        let tx_hash = digest(row.get(2), "transaction range hash")?;
        let payload: Vec<u8> = row.get(3);
        let state_hash = digest(row.get(4), "transaction range state hash")?;
        let request_kind: i16 = row.get(8);
        let legacy_request_key: Option<String> = row.get(9);
        let stored_request_key_hash: Option<Vec<u8>> = row.get(10);
        let request_digest = digest(row.get::<_, Vec<u8>>(11), "transaction request digest")?;
        if basis != expected_basis || stored_previous != expected_previous {
            return Err(fault(
                "recovery/invalid-log-link",
                "transaction range is noncontiguous or has a predecessor mismatch",
            ));
        }
        let content_hash = if generation == 0 {
            None
        } else {
            Some(digest(row.get(5), "transaction content hash")?)
        };
        let transaction = if generation == 0 {
            if transaction_hash(&payload) != tx_hash {
                return Err(fault(
                    "recovery/transaction-checksum-mismatch",
                    "legacy transaction payload does not match its hash",
                ));
            }
            let transaction = decode_transaction(&payload)?;
            if transaction.database_id != database_id
                || transaction.basis_t != basis
                || transaction.previous_hash != stored_previous
            {
                return Err(fault(
                    "recovery/envelope-mismatch",
                    "legacy transaction envelope disagrees with its row",
                ));
            }
            transaction
        } else {
            let content_hash = content_hash.expect("positive generation has content hash");
            let frontier = pg_basis(row.get(6), "transaction content frontier")?;
            let content_lineage: String = row.get(7);
            if sha256(&payload) != content_hash {
                return Err(fault(
                    "recovery/content-checksum-mismatch",
                    "lineage transaction content does not match its hash",
                ));
            }
            let content = LineageTransactionContent::decode(&payload)?;
            if content_lineage != lineage_id
                || content.lineage_id != lineage_id
                || content.basis_t != basis
                || content.eidx_frontier != frontier
            {
                return Err(fault(
                    "recovery/content-coordinate-mismatch",
                    "lineage transaction content disagrees with its membership row",
                ));
            }
            if generation_transaction_hash(
                &lineage_id,
                generation,
                basis,
                stored_previous,
                content_hash,
                state_hash,
                frontier,
            )? != tx_hash
            {
                return Err(fault(
                    "recovery/generation-membership-mismatch",
                    "lineage transaction membership commitment is invalid",
                ));
            }
            let mut transaction = content.to_transaction(stored_previous);
            // ATLC commits the stable lineage, not a mutable catalog alias.
            // Consumers authenticate that lineage above, then project the
            // transaction into the alias through which this database was
            // opened so recent-tier and receipt boundaries remain coherent.
            transaction.database_id = database_id.to_owned();
            transaction
        };
        if !matches!(request_kind, 0..=2) || (generation == 0 && request_kind != 1) {
            return Err(fault(
                "recovery/request-kind",
                "transaction request record has an invalid kind",
            ));
        }
        if (generation == 0
            && (legacy_request_key.as_deref().is_none_or(str::is_empty)
                || stored_request_key_hash.is_some()))
            || (generation > 0
                && (legacy_request_key.is_some() || stored_request_key_hash.is_none()))
        {
            return Err(fault(
                "recovery/request-identity",
                "transaction request record has invalid generation identity metadata",
            ));
        }
        visit(AuthenticatedLogTransaction {
            transaction,
            tx_hash,
            state_hash,
            payload,
            content_hash,
            legacy_request_key,
            request_key_hash: stored_request_key_hash
                .map(|hash| digest(hash, "transaction request key hash"))
                .transpose()?,
            request_digest,
            excision_replay: request_kind == 0,
        })?;
        visited = visited.checked_add(1).ok_or_else(|| {
            fault(
                "recovery/basis-overflow",
                "transaction range row count overflow",
            )
        })?;
        expected_previous = tx_hash;
    }
    if visited != through_basis - after_basis {
        return Err(fault(
            "recovery/missing-transaction",
            "transaction range is incomplete or lacks its exact request record",
        ));
    }
    Ok(visited)
}

pub(crate) fn recover_to<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    target_basis: u64,
    target_hash: Digest,
) -> Result<Recovered, SemanticError> {
    let generation = client
        .query_opt(
            "SELECT log_generation FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/recovery-generation", error))?
        .ok_or_else(|| not_found(database_id))?;
    let generation = pg_basis(generation.get(0), "head log generation")?;
    recover_generation_to(client, database_id, generation, target_basis, target_hash)
}

/// Replay one explicitly named immutable log generation. Ordinary callers use
/// `recover_to`, which resolves the currently active generation once. COW and
/// same-lineage restore code use this form so an inactive candidate can be
/// authenticated without confusing it with the published head.
pub(crate) fn recover_generation_to<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    target_basis: u64,
    target_hash: Digest,
) -> Result<Recovered, SemanticError> {
    let catalog = client
        .query_opt(
            "SELECT lineage_id, genesis, genesis_hash FROM atomic_databases \
             WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
        .ok_or_else(|| not_found(database_id))?;
    let lineage_id: String = catalog.get(0);
    let genesis: Vec<u8> = catalog.get(1);
    let genesis_hash = digest(catalog.get::<_, Vec<u8>>(2), "genesis hash")?;
    if sha256(&genesis) != genesis_hash {
        return Err(fault(
            "recovery/genesis-checksum-mismatch",
            "genesis row does not match its digest",
        ));
    }
    let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
    let target_basis_sql = sql_basis(target_basis)?;
    let generation_sql = sql_basis(generation)?;
    let rows = if generation == 0 {
        client
            .query(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, t.payload, \
                        t.state_hash, NULL::bytea, NULL::bigint, NULL::text, \
                        1::smallint \
                   FROM atomic_transactions t \
                   JOIN atomic_requests r \
                     ON r.database_id = t.database_id AND r.basis_t = t.basis_t \
                    AND r.tx_hash = t.tx_hash \
                  WHERE t.database_id = $1 AND t.basis_t <= $2 \
                  ORDER BY t.basis_t",
                &[&database_id, &target_basis_sql],
            )
            .map_err(|error| postgres_error("postgres/recovery-log", error))?
    } else {
        client
            .query(
                "SELECT t.basis_t, t.previous_hash, t.tx_hash, c.payload, \
                        t.state_hash, t.content_hash, t.eidx_frontier, c.lineage_id, \
                        r.request_kind \
                   FROM atomic_generation_transactions t \
                   JOIN atomic_transaction_contents c \
                     ON c.content_hash = t.content_hash \
                   JOIN atomic_generation_requests r \
                     ON r.database_id = t.database_id \
                    AND r.generation = t.generation AND r.basis_t = t.basis_t \
                    AND r.tx_hash = t.tx_hash \
                  WHERE t.database_id = $1 AND t.generation = $2 \
                    AND t.basis_t <= $3 ORDER BY t.basis_t",
                &[&database_id, &generation_sql, &target_basis_sql],
            )
            .map_err(|error| postgres_error("postgres/recovery-generation-log", error))?
    };
    if rows.len() != usize::try_from(target_basis).unwrap_or(usize::MAX) {
        return Err(fault(
            "recovery/missing-transaction",
            "published transaction chain is incomplete",
        ));
    }

    let mut previous_hash = genesis_hash;
    let mut target_state_hash = [0; 32];
    for (offset, row) in rows.into_iter().enumerate() {
        let expected_basis = offset as u64 + 1;
        let basis = pg_basis(row.get::<_, i64>(0), "transaction")?;
        if basis != expected_basis {
            return Err(fault(
                "recovery/noncontiguous-basis",
                format!("expected transaction {expected_basis}, got {basis}"),
            ));
        }
        let stored_previous = digest(row.get::<_, Vec<u8>>(1), "predecessor hash")?;
        let stored_hash = digest(row.get::<_, Vec<u8>>(2), "transaction hash")?;
        let payload: Vec<u8> = row.get(3);
        let stored_state_hash = digest(row.get::<_, Vec<u8>>(4), "transaction state hash")?;
        if stored_previous != previous_hash {
            return Err(fault(
                "recovery/predecessor-mismatch",
                format!("transaction {basis} does not link to its predecessor"),
            ));
        }
        let envelope = if generation == 0 {
            if transaction_hash(&payload) != stored_hash {
                return Err(fault(
                    "recovery/transaction-checksum-mismatch",
                    format!("transaction {basis} row does not match its digest"),
                ));
            }
            let envelope = decode_transaction(&payload)?;
            if envelope.database_id != database_id
                || envelope.basis_t != basis
                || envelope.previous_hash != previous_hash
            {
                return Err(fault(
                    "recovery/envelope-mismatch",
                    format!("transaction {basis} envelope disagrees with its row"),
                ));
            }
            envelope
        } else {
            let content_hash = digest(row.get::<_, Vec<u8>>(5), "content hash")?;
            let frontier = pg_basis(row.get::<_, i64>(6), "transaction frontier")?;
            let stored_lineage: String = row.get(7);
            if sha256(&payload) != content_hash {
                return Err(fault(
                    "recovery/content-checksum-mismatch",
                    format!("transaction {basis} content does not match its digest"),
                ));
            }
            let content = LineageTransactionContent::decode(&payload)?;
            if stored_lineage != lineage_id
                || content.lineage_id != lineage_id
                || content.basis_t != basis
                || content.eidx_frontier != frontier
            {
                return Err(fault(
                    "recovery/content-coordinate-mismatch",
                    format!("transaction {basis} content disagrees with its generation row"),
                ));
            }
            let expected_hash = generation_transaction_hash(
                &lineage_id,
                generation,
                basis,
                previous_hash,
                content_hash,
                stored_state_hash,
                frontier,
            )?;
            if expected_hash != stored_hash {
                return Err(fault(
                    "recovery/generation-membership-mismatch",
                    format!("transaction {basis} membership commitment is invalid"),
                ));
            }
            content.to_transaction(previous_hash)
        };
        let request_kind: i16 = row.get(8);
        database = if generation == 0 || matches!(request_kind, 1 | 2) {
            database.apply_committed(&envelope)?
        } else if request_kind == 0 {
            database.apply_excised_committed(&envelope)?
        } else {
            return Err(fault(
                "recovery/request-kind",
                format!("transaction {basis} has an invalid request kind"),
            ));
        };
        target_state_hash = stored_state_hash;
        previous_hash = stored_hash;
    }
    if database.basis_t() != target_basis || previous_hash != target_hash {
        return Err(fault(
            "recovery/head-mismatch",
            "replayed transaction chain does not reach the requested head",
        ));
    }
    database.validate_invariants()?;
    // Transaction bytes and the predecessor hash chain authenticate every
    // applied delta. The requested row's state commitment authenticates the
    // resulting materialized value, so re-hashing every intermediate value
    // would add quadratic work without strengthening this recovery result.
    if target_state_hash != [0; 32]
        && !verify_checkpoint_state_hash(&database, target_state_hash)?.matches()
    {
        return Err(fault(
            "recovery/state-commitment-mismatch",
            format!(
                "transaction {target_basis} state commitment does not match its database value"
            ),
        ));
    }
    Ok(Recovered {
        database,
        final_hash: previous_hash,
    })
}

fn load_request_tempids<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    request_key_hash: Digest,
) -> Result<BTreeMap<String, u64>, SemanticError> {
    let generation = sql_basis(generation)?;
    client
        .query(
            "SELECT tempid_name, entity_id FROM atomic_generation_request_tempids \
             WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3 \
             ORDER BY tempid_name",
            &[&database_id, &generation, &&request_key_hash[..]],
        )
        .map_err(|error| postgres_error("postgres/request-tempids-read", error))?
        .into_iter()
        .map(|row| {
            let name: String = row.get(0);
            let entity = pg_basis(row.get(1), "request tempid entity")?;
            Ok((name, entity))
        })
        .collect()
}

fn exact_endpoint(coordinate: &PersistentCommitmentCoordinate) -> ExactEndpoint {
    ExactEndpoint {
        generation: coordinate.generation,
        basis_t: coordinate.basis_t,
        tx_hash: coordinate.tx_hash,
        state_hash: coordinate.state_hash,
        eidx_frontier: coordinate.eidx_frontier,
    }
}

/// Keep exact replay receipts independent from the live writer's physical
/// representation. A request-base binding may reconstruct the same logical
/// head from an older tree publication; installing that value would lengthen
/// the live recent tier and retain history that a covering publication already
/// made unnecessary. Publication revision numbers are local to their normal or
/// archived manifest namespace, so they are deliberately not compared here:
/// an already-installed exact-head value is the authoritative live choice.
fn select_freshest_head_writer_state(
    cached: Option<WriterState>,
    reconstructed: WriterState,
    head: &PersistentCommitmentCoordinate,
) -> Result<Option<WriterState>, SemanticError> {
    let endpoint = exact_endpoint(head);
    if reconstructed.commitment != *head || reconstructed.database.endpoint() != endpoint {
        return Err(fault(
            "postgres/replay-head-coordinate",
            "the reconstructed idempotent outcome does not name the locked logical head",
        ));
    }
    // The reconstructed state exists to serve an immutable receipt. It may
    // legitimately exceed a writer limit lowered after commit, so never turn
    // it into mutable writer state without a writer-purpose admission pass.
    // An already-installed exact head has crossed that boundary already.
    Ok(cached.filter(|state| state.commitment == *head && state.database.endpoint() == endpoint))
}

#[allow(clippy::too_many_arguments)]
fn open_exact_state(
    connection: &PostgresConnectionConfig,
    database_id: &str,
    commitment: PersistentCommitmentCoordinate,
    required_manifest: Option<Digest>,
    cache_entries: usize,
    cache_bytes: usize,
    recent_limits: RecentLimits,
    shared_snapshot: Option<&TieredSnapshot>,
    writer_admission: bool,
) -> Result<WriterState, SemanticError> {
    let endpoint = exact_endpoint(&commitment);
    let (database, opened) = match (shared_snapshot, writer_admission) {
        (Some(shared_snapshot), true) => shared_snapshot.open_writer_exact_sharing_core(
            database_id,
            endpoint,
            required_manifest,
        )?,
        (Some(shared_snapshot), false) => {
            shared_snapshot.open_exact_sharing_core(database_id, endpoint, required_manifest)?
        }
        (None, true) => TieredSnapshot::open_writer_exact_configured(
            connection,
            database_id.to_owned(),
            endpoint,
            required_manifest,
            cache_entries,
            cache_bytes,
            recent_limits,
        )?,
        (None, false) => TieredSnapshot::open_exact_configured(
            connection,
            database_id.to_owned(),
            endpoint,
            required_manifest,
            cache_entries,
            cache_bytes,
            recent_limits,
        )?,
    };
    finish_exact_state(commitment, database, opened)
}

fn finish_exact_state(
    commitment: PersistentCommitmentCoordinate,
    database: TieredSnapshot,
    opened: ExactOpenStats,
) -> Result<WriterState, SemanticError> {
    if database.endpoint() != exact_endpoint(&commitment) {
        return Err(fault(
            "postgres/native-open-endpoint",
            "the native writer opened a value at the wrong logical endpoint",
        ));
    }
    Ok(WriterState {
        database,
        commitment,
        publication_revision: opened.selected_publication_revision,
        last_read_work: TransactionReadWork::default(),
        last_commitment_work: CommitmentWork::default(),
    })
}

#[allow(clippy::too_many_arguments)]
fn reconstruct_exact_request_receipt<C: GenericClient>(
    client: &mut C,
    connection: &PostgresConnectionConfig,
    database_id: &str,
    generation: u64,
    basis_t: u64,
    tx_hash: Digest,
    request_kind: i16,
    request_key_hash: Digest,
    cache_entries: usize,
    cache_bytes: usize,
    recent_limits: RecentLimits,
    shared_read_core: Option<&TieredReadHandle>,
) -> Result<(CommitReceipt, WriterState), SemanticError> {
    if !matches!(request_kind, 1 | 2) {
        return Err(fault(
            "postgres/request-kind",
            "only ordinary requests have reconstructable transaction reports",
        ));
    }
    if generation == 0 && request_kind == 2 {
        return Err(fault(
            "postgres/request-base-generation",
            "generation-zero request records cannot name a native base binding",
        ));
    }
    let before_basis = basis_t.checked_sub(1).ok_or_else(|| {
        fault(
            "postgres/request-basis",
            "a transaction request cannot identify the genesis basis",
        )
    })?;
    let committed = load_persistent_coordinate(client, database_id, generation, basis_t)?
        .ok_or_else(|| {
            fault(
                "postgres/request-semantic-root-missing",
                "request outcome has no exact v2 semantic commitment coordinate",
            )
        })?;
    if committed.tx_hash != tx_hash {
        return Err(fault(
            "postgres/request-coordinate-mismatch",
            "request outcome disagrees with its semantic commitment coordinate",
        ));
    }
    let before = load_persistent_coordinate(client, database_id, generation, before_basis)?
        .ok_or_else(|| {
            fault(
                "postgres/request-before-root-missing",
                "request db-before has no exact v2 semantic commitment coordinate",
            )
        })?;
    let required_manifest = if request_kind == 2 {
        let generation_sql = sql_basis(generation)?;
        let row = client
            .query_opt(
                "SELECT base_manifest_hash FROM atomic_generation_request_bases \
                  WHERE database_id = $1 AND generation = $2 AND request_key_hash = $3",
                &[&database_id, &generation_sql, &&request_key_hash[..]],
            )
            .map_err(|error| postgres_error("postgres/request-base-read", error))?
            .ok_or_else(|| {
                fault(
                    "postgres/request-base-missing",
                    "native request outcome has no immutable db-before base binding",
                )
            })?;
        Some(digest(
            row.get::<_, Vec<u8>>(0),
            "request base manifest hash",
        )?)
    } else {
        None
    };
    let before_state = if let Some(core) = shared_read_core {
        let (database, opened) =
            core.open_exact(database_id, exact_endpoint(&before), required_manifest)?;
        finish_exact_state(before, database, opened)?
    } else {
        open_exact_state(
            connection,
            database_id,
            before,
            required_manifest,
            cache_entries,
            cache_bytes,
            recent_limits,
            None,
            false,
        )?
    };
    let mut transactions = read_authenticated_log_range(
        client,
        database_id,
        generation,
        before_basis,
        basis_t,
        before_state.commitment.tx_hash,
    )?;
    if transactions.len() != 1 {
        return Err(fault(
            "postgres/request-transaction-count",
            "request receipt reconstruction requires exactly one authenticated transaction",
        ));
    }
    let authenticated = transactions.pop().expect("length checked");
    if authenticated.tx_hash != tx_hash || authenticated.state_hash != committed.state_hash {
        return Err(fault(
            "postgres/request-transaction-coordinate",
            "authenticated request transaction disagrees with its committed coordinate",
        ));
    }
    if generation > 0 && authenticated.request_key_hash != Some(request_key_hash) {
        return Err(fault(
            "postgres/request-identity-mismatch",
            "authenticated transaction names a different idempotency key",
        ));
    }
    let transaction = authenticated.transaction;
    let db_after = before_state.database.authenticated_successor_from_log(
        tx_hash,
        committed.state_hash,
        transaction.clone(),
    )?;
    if db_after.endpoint() != exact_endpoint(&committed) {
        return Err(fault(
            "postgres/request-successor-endpoint",
            "reconstructed request successor has the wrong logical endpoint",
        ));
    }
    let tempids = if generation == 0 {
        transaction.tempids.clone()
    } else {
        load_request_tempids(client, database_id, generation, request_key_hash)?
    };
    let receipt = CommitReceipt {
        db_before: before_state.database.database_value(),
        database: db_after.database_value(),
        basis_t,
        tx_hash,
        tempids,
        tx_data: transaction.tx_data,
        replayed: true,
    };
    Ok((
        receipt,
        WriterState {
            database: db_after,
            commitment: committed,
            publication_revision: before_state.publication_revision,
            last_read_work: TransactionReadWork::default(),
            last_commitment_work: CommitmentWork::default(),
        },
    ))
}

fn postgres_now_millis<C: GenericClient>(client: &mut C) -> Result<i64, SemanticError> {
    client
        .query_one(
            "SELECT floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint",
            &[],
        )
        .map(|row| row.get(0))
        .map_err(|error| postgres_error("postgres/read-clock", error))
}

fn select_tx_instant(
    db_before: &DatabaseValue,
    server_now: i64,
    option_override: Option<i64>,
    ops: &[TxOp],
) -> Result<i64, SemanticError> {
    let mut data_override = None;
    for op in ops {
        if let TxOp::Add {
            entity: crate::EntityRef::Tx,
            attribute,
            value: crate::TxValue::Scalar(Value::Instant(instant)),
        } = op
            && *attribute == crate::DB_TX_INSTANT as u32
            && data_override.replace(*instant).is_some()
        {
            return Err(SemanticError::incorrect(
                "transaction/multiple-tx-instants",
                ":db/txInstant may be specified only once",
            ));
        }
    }
    let explicit = match (option_override, data_override) {
        (Some(left), Some(right)) if left != right => {
            return Err(SemanticError::incorrect(
                "transaction/tx-instant-mismatch",
                "transaction option and transaction data specify different instants",
            ));
        }
        (Some(instant), _) | (_, Some(instant)) => Some(instant),
        (None, None) => None,
    };
    let previous = db_before.last_tx_instant()?;
    if let Some(instant) = explicit {
        if instant > server_now {
            return Err(SemanticError::incorrect(
                "transaction/future-tx-instant",
                format!("transaction instant {instant} exceeds transactor clock {server_now}"),
            ));
        }
        if previous.is_some_and(|basis| instant < basis) {
            return Err(SemanticError::incorrect(
                "transaction/past-tx-instant",
                format!("transaction instant {instant} precedes basis instant {previous:?}"),
            ));
        }
        Ok(instant)
    } else {
        Ok(previous.map_or(server_now, |basis| basis.max(server_now)))
    }
}

fn sql_basis(basis: u64) -> Result<i64, SemanticError> {
    i64::try_from(basis).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "postgres/basis-out-of-range",
            "PostgreSQL BIGINT cannot represent this basis",
        )
    })
}

fn pg_basis(basis: i64, record: &str) -> Result<u64, SemanticError> {
    u64::try_from(basis).map_err(|_| {
        fault(
            "recovery/negative-basis",
            format!("{record} contains a negative basis"),
        )
    })
}

fn digest(bytes: Vec<u8>, record: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|bytes: Vec<u8>| {
        fault(
            "recovery/invalid-digest-length",
            format!("{record} digest has {} bytes instead of 32", bytes.len()),
        )
    })
}

fn not_found(database_id: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::NotFound,
        "postgres/database-not-found",
        format!("database {database_id} does not exist"),
    )
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

fn injected(point: &str) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Interrupted,
        "postgres/injected-failure",
        format!("injected failure {point}"),
    )
}

fn unknown_outcome(
    request_key_hash: Digest,
    ambiguity_kind: &'static str,
    message: impl Into<String>,
) -> SemanticError {
    SemanticError::new(
        ErrorCategory::UnknownOutcome,
        "postgres/unknown-outcome",
        message,
    )
    .detail("request_key_hash", digest_hex(&request_key_hash))
    .detail("ambiguity_kind", ambiguity_kind)
}

fn digest_hex(digest: &Digest) -> String {
    use std::fmt::Write as _;
    let mut output = String::with_capacity(digest.len() * 2);
    for byte in digest {
        let _ = write!(output, "{byte:02x}");
    }
    output
}

pub(crate) fn postgres_error(code: &'static str, error: postgres::Error) -> SemanticError {
    let database_error = error.as_db_error();
    let sqlstate = database_error.map(|error| error.code().code());
    // PostgreSQL can report restart/failover as a server SQLSTATE before the
    // socket disappears. Those are transport availability, not corrupt SQL.
    let transport = sqlstate.is_none()
        || sqlstate.is_some_and(|state| {
            state.starts_with("08") || matches!(state, "57P01" | "57P02" | "57P03")
        });
    let category = match sqlstate {
        Some("23505" | "40001" | "40P01") => ErrorCategory::Conflict,
        Some("42501") => ErrorCategory::Forbidden,
        Some("57014") => ErrorCategory::Interrupted,
        Some("55P03") => ErrorCategory::Busy,
        Some(state) if state.starts_with("08") || matches!(state, "57P01" | "57P02" | "57P03") => {
            ErrorCategory::Unavailable
        }
        Some(_) => ErrorCategory::Fault,
        None => ErrorCategory::Unavailable,
    };
    let mut semantic = if let Some(database_error) = database_error {
        let mut semantic = SemanticError::new(
            category,
            code,
            sanitize_postgres_message(database_error.message()),
        )
        .detail("postgres_sqlstate", database_error.code().code());
        for (name, value) in [
            ("postgres_constraint", database_error.constraint()),
            ("postgres_schema", database_error.schema()),
            ("postgres_table", database_error.table()),
            ("postgres_column", database_error.column()),
            ("postgres_routine", database_error.routine()),
        ] {
            if let Some(value) = value {
                semantic = semantic.detail(name, sanitize_postgres_identifier(value));
            }
        }
        semantic
    } else {
        // Driver/transport Display strings can contain connection locators.
        // The stable operation code and transport marker retain actionable
        // structure without echoing credentials or DSNs.
        SemanticError::new(category, code, "PostgreSQL transport error")
    };
    if transport {
        semantic = semantic.detail("postgres_transport", "true");
    }
    semantic
}

fn sanitize_postgres_identifier(value: &str) -> String {
    value
        .chars()
        .filter(|character| !character.is_control())
        .take(128)
        .collect()
}

fn sanitize_postgres_message(value: &str) -> String {
    let mut output = String::with_capacity(value.len().min(512));
    let mut quoted = None;
    for character in value.chars() {
        if output.len() >= 512 {
            break;
        }
        if let Some(delimiter) = quoted {
            if character == delimiter {
                quoted = None;
                output.push_str("<redacted>");
            }
            continue;
        }
        if matches!(character, '\'' | '"') {
            quoted = Some(character);
        } else if character.is_control() {
            output.push(' ');
        } else {
            output.push(character);
        }
    }
    if quoted.is_some() {
        output.push_str("<redacted>");
    }
    let output = output.trim();
    if output.is_empty() {
        "PostgreSQL server error".to_owned()
    } else {
        output.to_owned()
    }
}

pub(crate) fn is_postgres_connection_error(error: &SemanticError) -> bool {
    error.category == ErrorCategory::Unavailable
        && error
            .details
            .get("postgres_transport")
            .is_some_and(|value| value == "true")
}

#[cfg(test)]
mod migration_compatibility_tests {
    use super::*;

    fn known_rows() -> Vec<(i64, Vec<u8>)> {
        MIGRATIONS
            .iter()
            .map(|(version, sql)| (*version, sha256(sql.as_bytes()).to_vec()))
            .collect()
    }

    #[test]
    fn runtime_requires_the_complete_exact_migration_prefix() {
        let rows = known_rows();
        validate_migration_rows(&rows, true).unwrap();

        let error = validate_migration_rows(&rows[..rows.len() - 1], true).unwrap_err();
        assert_eq!(error.code, "postgres/schema-upgrade-required");
        validate_migration_rows(&rows[..rows.len() - 1], false).unwrap();

        let mut corrupt = rows.clone();
        corrupt[3].1[0] ^= 1;
        let error = validate_migration_rows(&corrupt, true).unwrap_err();
        assert_eq!(error.code, "postgres/migration-checksum-mismatch");

        let mut gap = rows.clone();
        gap.remove(3);
        let error = validate_migration_rows(&gap, false).unwrap_err();
        assert_eq!(error.code, "postgres/migration-history-invalid");
    }

    #[test]
    fn every_old_binary_path_rejects_a_future_schema_version() {
        let mut rows = known_rows();
        rows.push((POSTGRES_SCHEMA_VERSION + 1, vec![0; 32]));
        for require_complete in [false, true] {
            let error = validate_migration_rows(&rows, require_complete).unwrap_err();
            assert_eq!(error.code, "postgres/schema-too-new");
            assert_eq!(error.category, ErrorCategory::Unavailable);
        }
    }

    #[test]
    fn runtime_role_names_are_quoted_as_identifiers() {
        assert_eq!(quote_identifier("writer").unwrap(), "\"writer\"");
        assert_eq!(quote_identifier("odd\"role").unwrap(), "\"odd\"\"role\"");
        assert_eq!(
            quote_identifier("").unwrap_err().code,
            "postgres/invalid-role-name"
        );
    }

    #[test]
    fn postgres_diagnostics_redact_quoted_values_and_control_text() {
        assert_eq!(
            sanitize_postgres_message(
                "duplicate key value violates unique constraint \"secret@example.com\"\n"
            ),
            "duplicate key value violates unique constraint <redacted>"
        );
        assert_eq!(sanitize_postgres_identifier("safe\nname"), "safename");
        assert_eq!(sanitize_postgres_message("\"unterminated"), "<redacted>");
    }
}
