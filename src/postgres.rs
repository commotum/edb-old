use crate::database::{AssessedTransaction, PredicateRole};
use crate::encoding::program_call_digest;
use crate::program::ValidatedProgram;
use crate::state_commitment::{checkpoint_state_hash, verify_checkpoint_state_hash};
use crate::{
    CallableRef, Database, Datom, Digest, DurableTransaction, ErrorCategory,
    PostgresConnectionConfig, Program, ProgramBudget, ProgramCall, ProgramHash, ProgramKind,
    ProgramLimits, ProgramOutput, ProgramRuntime, Schema, SemanticError, TxForm, TxFunctions, TxOp,
    Value, decode_genesis, decode_program, decode_transaction, encode_genesis, encode_program,
    encode_transaction, request_digest, sha256, transaction_hash,
};
use postgres::{Client, GenericClient, IsolationLevel};
use std::collections::{BTreeMap, BTreeSet, VecDeque};
use std::sync::{Arc, Mutex};

type SharedProgramBudget = Arc<Mutex<ProgramBudget<'static>>>;
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

const MIGRATIONS: &[(i64, &str)] = &[
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
];

/// Latest PostgreSQL schema understood by this binary.
///
/// This is an operator compatibility boundary, not a data-format version.
pub const POSTGRES_SCHEMA_VERSION: i64 = 12;

const PEER_RUNTIME_TABLES: &[&str] = &[
    "atomic_schema_migrations",
    "atomic_databases",
    "atomic_heads",
    "atomic_transactions",
    "atomic_requests",
    "atomic_index_segments",
    "atomic_index_manifests",
    "atomic_programs",
    "atomic_program_versions",
    "atomic_active_programs",
    "atomic_database_generations",
    "atomic_index_publications",
    "atomic_tree_nodes",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_tree_publications",
];

const WRITER_RUNTIME_TABLES: &[&str] = &["atomic_transactor_leases"];

const WRITER_INSERT_TABLES: &[&str] = &[
    "atomic_transactions",
    "atomic_requests",
    "atomic_transactor_leases",
    "atomic_tree_nodes",
    "atomic_tree_manifests",
    "atomic_tree_manifest_roots",
    "atomic_tree_publications",
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

    /// Grant the exact table privileges used by the native peer and fenced
    /// transaction service. Roles must already exist and must not be elevated,
    /// own Atomic relations, or inherit another role's privileges.
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

    for (version, sql) in MIGRATIONS.iter().skip(installed.len()) {
        transaction
            .batch_execute(sql)
            .map_err(|error| postgres_error("postgres/migration-ddl", error))?;
        let checksum = sha256(sql.as_bytes());
        transaction
            .execute(
                "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
                &[version, &&checksum[..]],
            )
            .map_err(|error| postgres_error("postgres/migration-record", error))?;
    }
    transaction
        .commit()
        .map_err(|error| postgres_error("postgres/migration-commit", error))
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
    verify_schema_compatibility(&mut transaction)?;
    let schema: String = transaction
        .query_one("SELECT current_schema()", &[])
        .map_err(|error| postgres_error("postgres/runtime-grants-schema", error))?
        .get(0);
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
    for role in [writer_role, peer_role] {
        validate_runtime_role(&mut transaction, role, &schema)?;
    }

    let schema_ident = quote_identifier(&schema)?;
    let database_ident = quote_identifier(&database)?;
    let peer_ident = quote_identifier(peer_role)?;
    let writer_ident = quote_identifier(writer_role)?;
    let all_tables = PEER_RUNTIME_TABLES
        .iter()
        .chain(WRITER_RUNTIME_TABLES)
        .copied()
        .collect::<Vec<_>>();
    let all_relations = relation_list(&schema_ident, &all_tables);
    for role_ident in [&writer_ident, &peer_ident] {
        transaction
            .batch_execute(&format!(
                "REVOKE ALL PRIVILEGES ON TABLE {all_relations} FROM {role_ident}; \
                 REVOKE CREATE ON SCHEMA {schema_ident} FROM {role_ident}; \
                 GRANT CONNECT ON DATABASE {database_ident} TO {role_ident}; \
                 GRANT USAGE ON SCHEMA {schema_ident} TO {role_ident}"
            ))
            .map_err(|error| postgres_error("postgres/runtime-grants-reset", error))?;
    }
    let peer_relations = relation_list(&schema_ident, PEER_RUNTIME_TABLES);
    transaction
        .batch_execute(&format!(
            "GRANT SELECT ON TABLE {peer_relations} TO {peer_ident}; \
             GRANT SELECT ON TABLE {peer_relations} TO {writer_ident}; \
             GRANT SELECT ON TABLE {} TO {writer_ident}; \
             GRANT UPDATE ON TABLE {schema_ident}.\"atomic_databases\", \
                                   {schema_ident}.\"atomic_heads\" TO {writer_ident}; \
             GRANT INSERT ON TABLE {} TO {writer_ident}; \
             GRANT UPDATE ON TABLE {schema_ident}.\"atomic_transactor_leases\" TO {writer_ident}",
            relation_list(&schema_ident, WRITER_RUNTIME_TABLES),
            relation_list(&schema_ident, WRITER_INSERT_TABLES),
        ))
        .map_err(|error| postgres_error("postgres/runtime-grants-apply", error))?;
    transaction
        .commit()
        .map_err(|error| postgres_error("postgres/runtime-grants-commit", error))
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
                             WHERE c.relowner = r.oid AND n.nspname = $2 \
                               AND c.relname LIKE 'atomic\\_%' ESCAPE '\\') \
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
    let owns_atomic_relation: bool = row.get(7);
    if elevated || inherits_membership || owns_atomic_relation {
        return Err(SemanticError::incorrect(
            "postgres/runtime-role-not-least-privilege",
            format!("role {role} is elevated, inherits another role, or owns an Atomic relation"),
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
    }
}

fn validate_program_name_version(name: &str, version: u64) -> Result<(), SemanticError> {
    if name.is_empty() {
        return Err(SemanticError::incorrect(
            "postgres/empty-program-name",
            "program name cannot be empty",
        ));
    }
    if version == 0 {
        return Err(SemanticError::incorrect(
            "postgres/invalid-program-version",
            "program version must be positive",
        ));
    }
    Ok(())
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

fn qualified_program_ident(name: &str) -> Result<crate::Keyword, SemanticError> {
    let Some((namespace, local)) = name.split_once('/') else {
        return Err(SemanticError::incorrect(
            "program/unqualified-predicate",
            format!("predicate {name} must be a fully qualified symbol"),
        ));
    };
    if namespace.is_empty() || local.is_empty() || local.contains('/') {
        return Err(SemanticError::incorrect(
            "program/unqualified-predicate",
            format!("predicate {name} must be a fully qualified symbol"),
        ));
    }
    Ok(crate::Keyword::new(namespace, local))
}

/// Resolve a database function exactly at the recovered `Db.getFn` boundary:
/// first resolve its ident in this immutable db-before, then read its
/// cardinality-one `:db/fn` value. The native value is a content hash rather
/// than a JVM function object.
fn bound_program_hash(
    database: &Database,
    ident: &crate::Keyword,
) -> Result<ProgramHash, SemanticError> {
    let entity = database.entid(ident).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::NotFound,
            "program/function-not-found",
            format!(
                "database function {} is not installed",
                ident.qualified_name()
            ),
        )
    })?;
    match database.values(entity, crate::DB_FN as u32).as_slice() {
        [Value::Function(hash)] => Ok(*hash),
        [] => Err(SemanticError::incorrect(
            "program/not-a-database-function",
            format!("entity {} has no :db/fn value", ident.qualified_name()),
        )),
        _ => Err(fault(
            "program/invalid-function-binding",
            format!(
                "entity {} has a malformed :db/fn value",
                ident.qualified_name()
            ),
        )),
    }
}

fn resolve_program_in<C: GenericClient>(
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

fn database_callable_entity(
    database: &Database,
    reference: &crate::EntityRef,
) -> Result<u64, SemanticError> {
    match reference {
        crate::EntityRef::Id(entity) => Ok(*entity),
        crate::EntityRef::Ident(ident) => database.entid(ident).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "program/function-not-found",
                format!(
                    "database function {} is not installed",
                    ident.qualified_name()
                ),
            )
        }),
        crate::EntityRef::Lookup { attribute, value } => {
            database.lookup(*attribute, value)?.ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "program/function-not-found",
                    "database function lookup reference did not resolve",
                )
            })
        }
        crate::EntityRef::Temp(_) | crate::EntityRef::Tx => Err(SemanticError::incorrect(
            "program/non-temporal-callable",
            "database functions must resolve from db-before, not a transaction-local entity",
        )),
    }
}

fn callable_hash(
    database: &Database,
    callable: &CallableRef,
) -> Result<ProgramHash, SemanticError> {
    match callable {
        CallableRef::Database(reference) => {
            let entity = database_callable_entity(database, reference)?;
            match database.values(entity, crate::DB_FN as u32).as_slice() {
                [Value::Function(hash)] => Ok(*hash),
                [] => Err(SemanticError::incorrect(
                    "program/not-a-database-function",
                    format!("entity {entity} has no :db/fn value"),
                )),
                _ => Err(fault(
                    "program/invalid-function-binding",
                    format!("entity {entity} has a malformed :db/fn value"),
                )),
            }
        }
        CallableRef::ExactHash(hash) => Ok(*hash),
        CallableRef::Local(symbol) => Err(SemanticError::new(
            ErrorCategory::NotFound,
            "program/local-function-not-found",
            format!(
                "no process-local function registry contains {}",
                symbol.qualified_name()
            ),
        )),
    }
}

fn execute_program_calls_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    db_before: &Database,
    calls: &[ProgramCall],
    budget: &mut ProgramBudget<'_>,
) -> Result<Vec<TxForm>, SemanticError> {
    let mut ordered = calls
        .iter()
        .map(|call| Ok((program_call_digest(call)?, call)))
        .collect::<Result<Vec<_>, SemanticError>>()?;
    ordered.sort_by(|left, right| left.0.cmp(&right.0));

    let mut forms = Vec::new();
    for (_, call) in ordered {
        expand_program_call_in(client, cache, db_before, call, budget, 0, &mut forms)?;
    }
    Ok(forms)
}

fn expand_submission_forms_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    db_before: &Database,
    submitted: &[TxForm],
    budget: &mut ProgramBudget<'_>,
) -> Result<Vec<TxForm>, SemanticError> {
    let mut forms = Vec::with_capacity(submitted.len());
    let mut calls = Vec::new();
    for form in submitted {
        match form {
            TxForm::Op(_) | TxForm::EntityMap(_) => forms.push(form.clone()),
            TxForm::ProgramCall(call) => calls.push(call.clone()),
            TxForm::Call(_) => {
                return Err(SemanticError::incorrect(
                    "service/process-local-call",
                    "process-local Rust transaction callbacks cannot cross the authoritative service boundary",
                ));
            }
        }
    }
    forms.extend(execute_program_calls_in(
        client, cache, db_before, &calls, budget,
    )?);
    Ok(forms)
}

fn expand_program_call_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    db_before: &Database,
    call: &ProgramCall,
    budget: &mut ProgramBudget<'_>,
    depth: usize,
    output: &mut Vec<TxForm>,
) -> Result<(), SemanticError> {
    if depth > 32 {
        return Err(SemanticError::incorrect(
            "transaction/function-depth",
            "persisted transaction-function expansion exceeded 32 nested calls",
        ));
    }
    let hash = callable_hash(db_before, &call.function)?;
    let program = resolve_program_in(client, cache, hash)?;
    if program.program().kind != ProgramKind::Transaction {
        return Err(SemanticError::incorrect(
            "program/not-transaction-function",
            "transaction data called a non-transaction program",
        ));
    }
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_prevalidated_runtime_with_budget(&program, db_before, &call.arguments, budget)?
    else {
        unreachable!("program kind was checked");
    };
    for form in forms {
        match form {
            TxForm::ProgramCall(nested) => {
                expand_program_call_in(
                    client,
                    cache,
                    db_before,
                    &nested,
                    budget,
                    depth + 1,
                    output,
                )?;
            }
            TxForm::Call(_) => {
                return Err(SemanticError::incorrect(
                    "program/process-local-output",
                    "persisted transaction functions cannot emit process-local Rust callbacks",
                ));
            }
            form => output.push(form),
        }
    }
    Ok(())
}

fn validate_successor_program_bindings_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    assessed: &AssessedTransaction,
) -> Result<(), SemanticError> {
    let report = assessed.report();
    let mut changed_function_entities = BTreeSet::new();
    let mut changed_predicate_names = BTreeSet::new();

    for datom in &report.tx_data {
        match u64::from(datom.attribute) {
            crate::DB_FN => {
                changed_function_entities.insert(datom.entity);
            }
            crate::DB_IDENT => {
                changed_function_entities.insert(datom.entity);
            }
            crate::DB_ATTR_PREDS | crate::DB_ENTITY_PREDS => {
                let Value::Symbol(symbol) = &datom.value else {
                    return Err(SemanticError::incorrect(
                        "program/invalid-predicate-name",
                        "predicate bindings must contain symbols",
                    ));
                };
                let name = symbol.qualified_name();
                qualified_program_ident(&name)?;
                changed_predicate_names.insert(name);
            }
            _ => {}
        }
    }

    // A changed :db/fn value is content-addressed and must resolve before the
    // source transaction publishes. Db.getFn also accepts an eid, so neither
    // an ident nor a qualified ident is required for a generic function. An
    // ident rename is included only because it can break a symbol-named
    // predicate reference even when the hash itself is unchanged.
    for entity in changed_function_entities {
        for database in [&report.db_before, &report.db_after] {
            for value in database.values(entity, crate::DB_IDENT as u32) {
                if let Value::Keyword(ident) = value {
                    changed_predicate_names.insert(ident.qualified_name());
                }
            }
        }

        let functions = report.db_after.values(entity, crate::DB_FN as u32);
        if functions.is_empty() {
            continue;
        }
        let [Value::Function(hash)] = functions.as_slice() else {
            return Err(fault(
                "program/invalid-function-binding",
                format!("entity {entity} has a malformed :db/fn value"),
            ));
        };
        resolve_program_in(client, cache, *hash)?;
    }

    // Validate only dependency names whose binding/reference changed. An old
    // unused bad binding is not transaction input and must not become a
    // global availability gate for unrelated writes.
    let changed_roles = predicate_roles_in(&report.db_after, &changed_predicate_names)?;
    for name in changed_predicate_names {
        let Some(role) = changed_roles.get(&name).copied() else {
            continue;
        };
        let ident = qualified_program_ident(&name)?;
        let hash = bound_program_hash(&report.db_after, &ident)?;
        let program = resolve_program_in(client, cache, hash)?;
        let expected = match role {
            PredicateRole::Attribute => ProgramKind::AttributePredicate,
            PredicateRole::Entity => ProgramKind::EntityPredicate,
        };
        if program.program().kind != expected {
            return Err(SemanticError::incorrect(
                match role {
                    PredicateRole::Attribute => "program/not-attribute-predicate",
                    PredicateRole::Entity => "program/not-entity-predicate",
                },
                format!("active program {name} has the wrong predicate role"),
            ));
        }
    }
    Ok(())
}

fn predicate_roles_in(
    database: &Database,
    names: &BTreeSet<String>,
) -> Result<BTreeMap<String, PredicateRole>, SemanticError> {
    let mut roles = BTreeMap::<String, (bool, bool)>::new();
    for name in database
        .schema()
        .attributes()
        .flat_map(|attribute| &attribute.predicates)
        .filter(|name| names.contains(*name))
    {
        roles.entry(name.clone()).or_default().0 = true;
    }
    for datom in database.datoms_with_prefix(&crate::IndexPrefix::Aevt {
        attribute: crate::DB_ENTITY_PREDS as u32,
        entity: None,
        value: None,
    })? {
        let Value::Symbol(symbol) = &datom.value else {
            return Err(fault(
                "postgres/invalid-entity-predicate",
                "current :db.entity/preds information is not a symbol",
            ));
        };
        let name = symbol.qualified_name();
        if names.contains(&name) {
            roles.entry(name).or_default().1 = true;
        }
    }
    roles
        .into_iter()
        .map(|(name, (attribute, entity))| {
            let role = match (attribute, entity) {
                (true, false) => PredicateRole::Attribute,
                (false, true) => PredicateRole::Entity,
                (true, true) => {
                    return Err(SemanticError::incorrect(
                        "program/predicate-role-conflict",
                        format!(
                            "predicate {name} is used as both an attribute and entity predicate"
                        ),
                    ));
                }
                (false, false) => unreachable!("only operative names are inserted"),
            };
            Ok((name, role))
        })
        .collect()
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
    pub db_before: Database,
    pub database: Database,
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
    pub program: ProgramLimits,
}

impl Default for CapacityLimits {
    fn default() -> Self {
        Self {
            max_transaction_ops: 100_000,
            max_transaction_bytes: 64 * 1024 * 1024,
            max_history_transactions: i64::MAX as u64,
            program: ProgramLimits::default(),
        }
    }
}

/// The one concrete durable boundary for Atomic.
///
/// `Database::with` remains pure; this owner performs PostgreSQL locking,
/// publication, retry resolution, and recovery around the kernel transition.
pub struct PostgresStore {
    client: Client,
    current: BTreeMap<String, (Digest, Database)>,
    program_cache: SharedProgramCache,
    capacity_limits: CapacityLimits,
}

impl PostgresStore {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        let client = connection.connect_for("postgres/connect")?;
        Ok(Self::from_client(client))
    }

    pub fn from_client(client: Client) -> Self {
        Self {
            client,
            current: BTreeMap::new(),
            program_cache: Arc::new(Mutex::new(ProgramCache::default())),
            capacity_limits: CapacityLimits::default(),
        }
    }

    pub(crate) fn set_capacity_limits(
        &mut self,
        limits: CapacityLimits,
    ) -> Result<(), SemanticError> {
        if limits.max_transaction_ops == 0
            || limits.max_transaction_bytes == 0
            || limits.max_history_transactions == 0
            || !limits.program.is_valid()
        {
            return Err(SemanticError::incorrect(
                "postgres/invalid-capacity-limits",
                "transaction and persisted-program capacity limits must be positive",
            ));
        }
        self.capacity_limits = limits;
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

    pub(crate) fn program_cache_handle(&self) -> SharedProgramCache {
        Arc::clone(&self.program_cache)
    }

    pub fn migrate(&mut self) -> Result<(), SemanticError> {
        // Compatibility shim for existing administrative callers. New code
        // should use `PostgresMigrator`, which cannot be mistaken for a
        // transaction-service runtime handle.
        apply_migrations(&mut self.client)
    }

    /// Read-only runtime compatibility gate. Schema installation is an
    /// explicit administrative action; a transactor never grants itself DDL
    /// authority while starting.
    pub fn verify_migrations(&mut self) -> Result<(), SemanticError> {
        verify_schema_compatibility(&mut self.client)
    }

    pub(crate) fn activate_transactor_state(
        &mut self,
        lease: &TransactorLease,
        lease_millis: u64,
    ) -> Result<crate::RecoveryStats, SemanticError> {
        let database_id = lease.database_id.clone();
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(IsolationLevel::RepeatableRead)
            .start()
            .map_err(|error| postgres_error("postgres/activation-begin", error))?;
        verify_lease(&mut transaction, lease, &database_id)?;
        let head = transaction
            .query_one(
                "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/activation-head", error))?;
        let target_t = pg_basis(head.get(0), "activation head")?;
        let target_hash = digest(head.get(1), "activation head hash")?;
        let (database, hash, stats) = crate::peer::recover_transactor_state(
            &mut transaction,
            &database_id,
            target_t,
            target_hash,
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
        self.current.insert(database_id, (hash, database));
        Ok(stats)
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
        let encoded = encode_genesis(Database::bootstrap()?.genesis_datoms())?;
        let genesis_hash = sha256(&encoded);
        let initial = if schema_ops.is_empty() {
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
            let payload = encode_transaction(&envelope)?;
            let tx_hash = transaction_hash(&payload);
            let request_hash = request_digest(&schema_ops, 0, 0)?;
            let state_hash = checkpoint_state_hash(&database)?;
            Some((payload, tx_hash, request_hash, state_hash))
        };
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/create-begin", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
                 VALUES ($1, $2, $3)",
                &[&database_id, &&encoded[..], &&genesis_hash[..]],
            )
            .map_err(|error| postgres_error("postgres/create-catalog", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
                &[&database_id, &&genesis_hash[..]],
            )
            .map_err(|error| postgres_error("postgres/create-head", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_database_generations (database_id) VALUES ($1)",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/create-generation", error))?;
        let final_hash = if let Some((payload, tx_hash, request_hash, state_hash)) = &initial {
            transaction
                .execute(
                    "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                     VALUES ($1, 1, $2, $3, $4, $5)",
                    &[
                        &database_id,
                        &&genesis_hash[..],
                        &&tx_hash[..],
                        &&payload[..],
                        &&state_hash[..],
                    ],
                )
                .map_err(|error| postgres_error("postgres/create-schema-transaction", error))?;
            transaction
                .execute(
                    "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                     VALUES ($1, '__atomic/create-schema/v1', $2, 1, $3)",
                    &[&database_id, &&request_hash[..], &&tx_hash[..]],
                )
                .map_err(|error| postgres_error("postgres/create-schema-request", error))?;
            let updated = transaction
                .execute(
                    "UPDATE atomic_heads SET basis_t = 1, tx_hash = $1 \
                     WHERE database_id = $2 AND basis_t = 0 AND tx_hash = $3",
                    &[&&tx_hash[..], &database_id, &&genesis_hash[..]],
                )
                .map_err(|error| postgres_error("postgres/create-schema-publication", error))?;
            if updated != 1 {
                return Err(SemanticError::conflict(
                    "postgres/create-schema-cas-failed",
                    "new database head no longer identifies its exact genesis",
                ));
            }
            *tx_hash
        } else {
            genesis_hash
        };
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/create-commit", error))?;
        self.current
            .insert(database_id.to_owned(), (final_hash, database.clone()));
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
        let recovered = recover_to(&mut self.client, database_id, basis, hash)?.database;
        self.current
            .insert(database_id.to_owned(), (hash, recovered.clone()));
        Ok(recovered)
    }

    pub fn recover_basis(
        &mut self,
        database_id: &str,
        basis_t: u64,
    ) -> Result<Database, SemanticError> {
        let hash = if basis_t == 0 {
            let row = self
                .client
                .query_opt(
                    "SELECT genesis_hash FROM atomic_databases WHERE database_id = $1",
                    &[&database_id],
                )
                .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
                .ok_or_else(|| not_found(database_id))?;
            digest(row.get::<_, Vec<u8>>(0), "genesis hash")?
        } else {
            let basis = sql_basis(basis_t)?;
            let row = self
                .client
                .query_opt(
                    "SELECT tx_hash FROM atomic_transactions \
                     WHERE database_id = $1 AND basis_t = $2",
                    &[&database_id, &basis],
                )
                .map_err(|error| postgres_error("postgres/recovery-basis-hash", error))?
                .ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::NotFound,
                        "postgres/basis-not-found",
                        format!("database {database_id} has no basis {basis_t}"),
                    )
                })?;
            digest(row.get::<_, Vec<u8>>(0), "basis transaction hash")?
        };
        Ok(recover_to(&mut self.client, database_id, basis_t, hash)?.database)
    }

    /// Resolve the durable decision for one admitted request without
    /// resubmitting or re-evaluating its transaction data.
    ///
    /// `None` means that no committed decision for this key is visible in the
    /// database. A returned receipt is reconstructed from the immutable log
    /// transaction named by the request record, including its exact
    /// `db-before`; it is therefore safe to use after an acknowledgement
    /// timeout without guessing the server-selected basis or transaction time.
    #[cfg(test)]
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
        let mut transaction = self
            .client
            .build_transaction()
            .isolation_level(postgres::IsolationLevel::RepeatableRead)
            .read_only(true)
            .start()
            .map_err(|error| postgres_error("postgres/request-outcome-begin", error))?;
        let row = transaction
            .query_opt(
                "SELECT basis_t, tx_hash FROM atomic_requests \
                 WHERE database_id = $1 AND request_key = $2",
                &[&database_id, &request_key],
            )
            .map_err(|error| postgres_error("postgres/request-outcome-read", error))?;
        let Some(row) = row else {
            let database_exists = transaction
                .query_opt(
                    "SELECT 1 FROM atomic_heads WHERE database_id = $1",
                    &[&database_id],
                )
                .map_err(|error| postgres_error("postgres/request-outcome-database", error))?
                .is_some();
            transaction
                .commit()
                .map_err(|error| postgres_error("postgres/request-outcome-commit", error))?;
            return if database_exists {
                Ok(None)
            } else {
                Err(not_found(database_id))
            };
        };
        let basis = pg_basis(row.get::<_, i64>(0), "request outcome")?;
        let hash = digest(row.get::<_, Vec<u8>>(1), "request transaction hash")?;
        let recovered = recover_to(&mut transaction, database_id, basis, hash)?;
        let previous_hash = recovered
            .final_transaction
            .as_ref()
            .ok_or_else(|| {
                fault(
                    "recovery/invalid-request-basis",
                    "durable request does not identify a positive transaction",
                )
            })?
            .previous_hash;
        let previous_basis = basis.checked_sub(1).ok_or_else(|| {
            fault(
                "recovery/invalid-request-basis",
                "durable request does not identify a positive transaction",
            )
        })?;
        let db_before =
            recover_to(&mut transaction, database_id, previous_basis, previous_hash)?.database;
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/request-outcome-commit", error))?;
        Ok(Some(receipt(recovered, db_before, true)))
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

    /// Legacy operator metadata retained during migration. It does not select
    /// code for transactions; only temporal `:db/fn` information does.
    pub fn deploy_program(
        &mut self,
        database_id: &str,
        name: &str,
        version: u64,
        program: &Program,
    ) -> Result<ProgramHash, SemanticError> {
        validate_program_name_version(name, version)?;
        let encoded = encode_program(program)?;
        let hash = sha256(&encoded);
        let version = sql_basis(version)?;
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
            .execute(
                "INSERT INTO atomic_program_versions (database_id, name, version, program_hash) \
                 VALUES ($1, $2, $3, $4) ON CONFLICT (database_id, name, version) DO NOTHING",
                &[&database_id, &name, &version, &&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-version-insert", error))?;
        let bound: Vec<u8> = transaction
            .query_one(
                "SELECT program_hash FROM atomic_program_versions \
                 WHERE database_id = $1 AND name = $2 AND version = $3",
                &[&database_id, &name, &version],
            )
            .map_err(|error| postgres_error("postgres/program-version-verify", error))?
            .get(0);
        if digest(bound, "program version hash")? != hash {
            return Err(SemanticError::conflict(
                "postgres/program-version-immutable",
                "program name/version is already bound to different content",
            ));
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/program-deploy-commit", error))?;
        self.cache_program(hash, program.clone(), program_cache_weight(encoded.len()));
        Ok(hash)
    }

    /// Conditionally publish an immutable program version. `None` expects no
    /// active version; an identical target is an idempotent replay.
    pub fn activate_program(
        &mut self,
        database_id: &str,
        name: &str,
        expected_version: Option<u64>,
        version: u64,
    ) -> Result<ProgramHash, SemanticError> {
        validate_program_name_version(name, version)?;
        let version_sql = sql_basis(version)?;
        let expected_sql = expected_version.map(sql_basis).transpose()?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/program-activate-begin", error))?;
        let target = transaction
            .query_opt(
                "SELECT program_hash FROM atomic_program_versions \
                 WHERE database_id = $1 AND name = $2 AND version = $3",
                &[&database_id, &name, &version_sql],
            )
            .map_err(|error| postgres_error("postgres/program-activate-target", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/program-version-not-found",
                    format!("program {name} version {version} is not deployed"),
                )
            })?;
        let target_hash = digest(target.get::<_, Vec<u8>>(0), "target program hash")?;
        let active = transaction
            .query_opt(
                "SELECT version, program_hash FROM atomic_active_programs \
                 WHERE database_id = $1 AND name = $2 FOR UPDATE",
                &[&database_id, &name],
            )
            .map_err(|error| postgres_error("postgres/program-activate-lock", error))?;
        if let Some(row) = &active {
            let active_version: i64 = row.get(0);
            let active_hash = digest(row.get::<_, Vec<u8>>(1), "active program hash")?;
            if active_version == version_sql && active_hash == target_hash {
                transaction
                    .commit()
                    .map_err(|error| postgres_error("postgres/program-activate-replay", error))?;
                return Ok(target_hash);
            }
        }
        let actual = active.as_ref().map(|row| row.get::<_, i64>(0));
        if actual != expected_sql {
            return Err(SemanticError::conflict(
                "postgres/program-activation-conflict",
                format!(
                    "expected active version {:?}, found {:?}",
                    expected_version,
                    actual.map(|value| value as u64)
                ),
            ));
        }
        transaction
            .execute(
                "INSERT INTO atomic_active_programs (database_id, name, version, program_hash) \
                 VALUES ($1, $2, $3, $4) \
                 ON CONFLICT (database_id, name) DO UPDATE \
                 SET version = EXCLUDED.version, program_hash = EXCLUDED.program_hash",
                &[&database_id, &name, &version_sql, &&target_hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-activate", error))?;
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/program-activate-commit", error))?;
        Ok(target_hash)
    }

    pub fn resolve_active_program(
        &mut self,
        database_id: &str,
        name: &str,
    ) -> Result<(u64, ProgramHash, Program), SemanticError> {
        let row = self
            .client
            .query_opt(
                "SELECT version, program_hash FROM atomic_active_programs \
                 WHERE database_id = $1 AND name = $2",
                &[&database_id, &name],
            )
            .map_err(|error| postgres_error("postgres/program-active-read", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/active-program-not-found",
                    format!("program {name} is not active"),
                )
            })?;
        let version = pg_basis(row.get(0), "program version")?;
        let hash = digest(row.get::<_, Vec<u8>>(1), "active program hash")?;
        let program = self.resolve_program(hash)?;
        Ok((version, hash, program))
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
        let max_primitive_ops = self.capacity_limits.max_transaction_ops;
        self.transact_generated(
            database_id,
            request_key,
            compare_basis_t,
            tx_instant_override,
            request_hash,
            CommitFault::None,
            Some(lease),
            None,
            |transaction, db_before, shared_budget, program_cache| {
                let mut budget = shared_budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                let forms = expand_submission_forms_in(
                    transaction,
                    program_cache,
                    db_before,
                    forms,
                    &mut budget,
                )?;
                db_before.normalize_forms_with_limit(&forms, &TxFunctions::new(), max_primitive_ops)
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
            &Database,
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
            .batch_execute("LOCK TABLE atomic_transactions IN ROW EXCLUSIVE MODE")
            .map_err(|error| postgres_error("postgres/transact-log-lock", error))?;

        let head = transaction
            .query_opt(
                "SELECT basis_t, tx_hash FROM atomic_heads \
                 WHERE database_id = $1 FOR UPDATE",
                &[&database_id],
            )
            .map_err(|error| postgres_error("postgres/transact-lock-head", error))?
            .ok_or_else(|| not_found(database_id))?;
        let head_basis_i64: i64 = head.get(0);
        let head_basis = pg_basis(head_basis_i64, "head")?;
        let head_hash = digest(head.get::<_, Vec<u8>>(1), "head transaction hash")?;

        if let Some(row) = transaction
            .query_opt(
                "SELECT request_digest, basis_t, tx_hash FROM atomic_requests \
                 WHERE database_id = $1 AND request_key = $2",
                &[&database_id, &request_key],
            )
            .map_err(|error| postgres_error("postgres/idempotency-read", error))?
        {
            let stored_request = digest(row.get::<_, Vec<u8>>(0), "request digest")?;
            if stored_request != request_hash {
                return Err(SemanticError::conflict(
                    "postgres/idempotency-key-reused",
                    "idempotency key is already bound to a different request",
                ));
            }
            let basis = pg_basis(row.get::<_, i64>(1), "request outcome")?;
            let hash = digest(row.get::<_, Vec<u8>>(2), "request transaction hash")?;
            let recovered = recover_to(&mut transaction, database_id, basis, hash)?;
            let previous_hash = recovered
                .final_transaction
                .as_ref()
                .expect("durable requests always name a positive transaction")
                .previous_hash;
            let db_before = recover_to(
                &mut transaction,
                database_id,
                basis.saturating_sub(1),
                previous_hash,
            )?
            .database;
            transaction
                .commit()
                .map_err(|error| unknown_outcome(request_key, error.to_string()))?;
            let receipt = receipt(recovered, db_before, true);
            if basis == head_basis && hash == head_hash {
                self.current
                    .insert(database_id.to_owned(), (hash, receipt.database.clone()));
            }
            return Ok(receipt);
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

        let db_before = match cached {
            Some((hash, database)) if hash == head_hash && database.basis_t() == head_basis => {
                database
            }
            _ => recover_to(&mut transaction, database_id, head_basis, head_hash)?.database,
        };
        let shared_budget = Arc::new(Mutex::new(ProgramBudget::new(
            self.capacity_limits.program.control(),
        )?));
        let ops = generate(&mut transaction, &db_before, &shared_budget, &program_cache)?;
        let server_now = postgres_now_millis(&mut transaction)?;
        let tx_instant = select_tx_instant(&db_before, server_now, tx_instant_override, &ops)?;
        if ops.len() > self.capacity_limits.max_transaction_ops {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "postgres/transaction-op-capacity",
                "transaction exceeds the configured operation limit",
            ));
        }
        let assessed = db_before.assess_with_context(&ops, tx_instant)?;
        validate_successor_program_bindings_in(&mut transaction, &program_cache, &assessed)?;
        let persisted_functions;
        let functions = match functions {
            Some(functions) => Some(functions),
            None => {
                persisted_functions = persisted_predicates_in(
                    &mut transaction,
                    &program_cache,
                    &db_before,
                    &assessed.predicate_requirements()?,
                    Arc::clone(&shared_budget),
                )?;
                Some(&persisted_functions)
            }
        };
        let report = assessed.validate(functions)?;
        let envelope = DurableTransaction {
            database_id: database_id.to_owned(),
            basis_t: report.db_after.basis_t(),
            previous_hash: head_hash,
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        };
        let payload = encode_transaction(&envelope)?;
        if payload.len() > self.capacity_limits.max_transaction_bytes {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "postgres/transaction-byte-capacity",
                "transaction exceeds the configured encoded-byte limit",
            ));
        }
        let tx_hash = transaction_hash(&payload);
        let state_hash = checkpoint_state_hash(&report.db_after)?;
        let next_basis = sql_basis(envelope.basis_t)?;

        if fault_point == CommitFault::BeforeTransactionInsert {
            return Err(injected("before transaction insert"));
        }
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
        if fault_point == CommitFault::AfterTransactionInsert {
            return Err(injected("after transaction insert"));
        }
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
        let updated = transaction
            .execute(
                "UPDATE atomic_heads SET basis_t = $1, tx_hash = $2 \
                 WHERE database_id = $3 AND basis_t = $4 AND tx_hash = $5",
                &[
                    &next_basis,
                    &&tx_hash[..],
                    &database_id,
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

        transaction
            .commit()
            .map_err(|error| unknown_outcome(request_key, error.to_string()))?;
        if fault_point == CommitFault::AfterCommitBeforeResponse {
            return Err(unknown_outcome(
                request_key,
                "injected acknowledgment loss after PostgreSQL commit",
            ));
        }
        let receipt = CommitReceipt {
            db_before,
            database: report.db_after,
            basis_t: envelope.basis_t,
            tx_hash,
            tempids: envelope.tempids,
            tx_data: envelope.tx_data,
            replayed: false,
        };
        self.current.insert(
            database_id.to_owned(),
            (receipt.tx_hash, receipt.database.clone()),
        );
        Ok(receipt)
    }
}

pub(crate) struct Recovered {
    pub(crate) database: Database,
    pub(crate) final_transaction: Option<DurableTransaction>,
    pub(crate) final_hash: Digest,
}

pub(crate) fn recover_to<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    target_basis: u64,
    target_hash: Digest,
) -> Result<Recovered, SemanticError> {
    let catalog = client
        .query_opt(
            "SELECT genesis, genesis_hash FROM atomic_databases \
             WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
        .ok_or_else(|| not_found(database_id))?;
    let genesis: Vec<u8> = catalog.get(0);
    let genesis_hash = digest(catalog.get::<_, Vec<u8>>(1), "genesis hash")?;
    if sha256(&genesis) != genesis_hash {
        return Err(fault(
            "recovery/genesis-checksum-mismatch",
            "genesis row does not match its digest",
        ));
    }
    let mut database = Database::from_genesis(decode_genesis(&genesis)?)?;
    let target_basis_sql = sql_basis(target_basis)?;
    let rows = client
        .query(
            "SELECT basis_t, previous_hash, tx_hash, payload, state_hash \
             FROM atomic_transactions \
             WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t",
            &[&database_id, &target_basis_sql],
        )
        .map_err(|error| postgres_error("postgres/recovery-log", error))?;
    if rows.len() != usize::try_from(target_basis).unwrap_or(usize::MAX) {
        return Err(fault(
            "recovery/missing-transaction",
            "published transaction chain is incomplete",
        ));
    }

    let mut previous_hash = genesis_hash;
    let mut final_transaction = None;
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
        database = database.apply_committed(&envelope)?;
        target_state_hash = stored_state_hash;
        previous_hash = stored_hash;
        final_transaction = Some(envelope);
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
        final_transaction,
        final_hash: previous_hash,
    })
}

fn receipt(recovered: Recovered, db_before: Database, replayed: bool) -> CommitReceipt {
    let basis_t = recovered.database.basis_t();
    let transaction = recovered
        .final_transaction
        .expect("idempotent outcomes always have a positive basis");
    CommitReceipt {
        db_before,
        database: recovered.database,
        basis_t,
        tx_hash: recovered.final_hash,
        tempids: transaction.tempids,
        tx_data: transaction.tx_data,
        replayed,
    }
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

fn persisted_predicates_in<C: GenericClient>(
    client: &mut C,
    cache: &SharedProgramCache,
    database: &Database,
    required: &BTreeMap<String, PredicateRole>,
    shared_budget: SharedProgramBudget,
) -> Result<TxFunctions, SemanticError> {
    let mut functions = TxFunctions::new();
    for (name, role) in required {
        let hash = bound_program_hash(database, &qualified_program_ident(name)?)?;
        let program = resolve_program_in(client, cache, hash)?;
        if *role == PredicateRole::Attribute {
            if program.program().kind != ProgramKind::AttributePredicate {
                return Err(SemanticError::incorrect(
                    "program/not-attribute-predicate",
                    format!("active program {name} is not an attribute predicate"),
                ));
            }
            let predicate_db = database.clone();
            let budget = Arc::clone(&shared_budget);
            functions.register_attribute_value_predicate(name.clone(), move |value| {
                let mut budget = budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                match ProgramRuntime.execute_prevalidated_with_budget(
                    &program,
                    &predicate_db,
                    std::slice::from_ref(value),
                    &mut budget,
                )? {
                    ProgramOutput::AttributePredicate(value) => Ok(value),
                    _ => unreachable!("program kind was checked"),
                }
            });
        } else {
            if program.program().kind != ProgramKind::EntityPredicate {
                return Err(SemanticError::incorrect(
                    "program/not-entity-predicate",
                    format!("active program {name} is not an entity predicate"),
                ));
            }
            let budget = Arc::clone(&shared_budget);
            functions.register_entity_value_predicate(name.clone(), move |db_after, entity| {
                let mut budget = budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                match ProgramRuntime.execute_prevalidated_with_budget(
                    &program,
                    db_after,
                    &[Value::Ref(entity)],
                    &mut budget,
                )? {
                    ProgramOutput::EntityPredicate(value) => Ok(value),
                    _ => unreachable!("program kind was checked"),
                }
            });
        }
    }
    Ok(functions)
}

fn select_tx_instant(
    db_before: &Database,
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
    let previous = db_before.last_tx_instant();
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

fn unknown_outcome(request_key: &str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(
        ErrorCategory::UnknownOutcome,
        "postgres/unknown-outcome",
        message,
    )
    .detail("request_key", request_key)
}

pub(crate) fn postgres_error(code: &'static str, error: postgres::Error) -> SemanticError {
    let category = match error.as_db_error().map(|error| error.code().code()) {
        Some("23505" | "40001" | "40P01") => ErrorCategory::Conflict,
        Some("42501") => ErrorCategory::Forbidden,
        Some(_) => ErrorCategory::Fault,
        None => ErrorCategory::Unavailable,
    };
    SemanticError::new(category, code, error.to_string())
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
}
