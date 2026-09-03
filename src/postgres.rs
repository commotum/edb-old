use crate::{
    Database, Datom, Digest, DurableTransaction, ErrorCategory, Program, ProgramControl,
    ProgramHash, ProgramInvocation, ProgramKind, ProgramOutput, ProgramRuntime, Schema,
    SchemaChange, SemanticError, TxFunctions, TxOp, Value, decode_program, decode_schema,
    decode_transaction, encode_program, encode_schema, encode_transaction, program_request_digest,
    request_digest, sha256, transaction_hash,
};
use postgres::{Client, GenericClient, NoTls};
use std::collections::{BTreeMap, VecDeque};

const MIGRATIONS: &[(i64, &str)] = &[
    (1, include_str!("../migrations/0001_atomic.sql")),
    (2, include_str!("../migrations/0002_peer_indexes.sql")),
    (3, include_str!("../migrations/0003_programs.sql")),
    (4, include_str!("../migrations/0004_transactor_leases.sql")),
];
const GENESIS_HASH: Digest = [0; 32];

fn program_kind_i16(kind: ProgramKind) -> i16 {
    match kind {
        ProgramKind::Transaction => 0,
        ProgramKind::AttributePredicate => 1,
        ProgramKind::Query => 2,
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
            "transactor {} no longer owns lease {} epoch {}",
            lease.holder_id, lease.scope, lease.epoch
        ),
    )
}

fn verify_lease<C: GenericClient>(
    client: &mut C,
    lease: &TransactorLease,
) -> Result<(), SemanticError> {
    let epoch = sql_basis(lease.epoch)?;
    let valid = client
        .query_opt(
            "SELECT holder_id = $2 AND epoch = $3 AND expires_at > clock_timestamp() \
             FROM atomic_transactor_leases WHERE lease_scope = $1 FOR UPDATE",
            &[&lease.scope, &lease.holder_id, &epoch],
        )
        .map_err(|error| postgres_error("postgres/lease-fence", error))?
        .is_some_and(|row| row.get::<_, bool>(0));
    if valid {
        Ok(())
    } else {
        Err(leadership_lost(lease))
    }
}

fn request_hash_with_predicates(base: Digest, hashes: &[ProgramHash]) -> Digest {
    let mut hashes = hashes.to_vec();
    hashes.sort();
    hashes.dedup();
    let mut bytes = Vec::with_capacity(32 + hashes.len() * 32);
    bytes.extend_from_slice(&base);
    for hash in hashes {
        bytes.extend_from_slice(&hash);
    }
    sha256(&bytes)
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum CommitFault {
    None,
    BeforeTransactionInsert,
    AfterTransactionInsert,
    AfterHeadUpdate,
    #[doc(hidden)]
    AfterHeadUpdateProcessAbort,
    AfterCommitBeforeResponse,
}

#[derive(Clone, Debug)]
pub struct CommitReceipt {
    pub database: Database,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub tempids: BTreeMap<String, u64>,
    pub tx_data: Vec<Datom>,
    pub schema_changes: Vec<SchemaChange>,
    pub replayed: bool,
}

#[derive(Clone, Debug)]
pub struct ProgramCommitReceipt {
    pub commit: CommitReceipt,
    pub program_hash: ProgramHash,
    pub program_version: Option<u64>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TransactorLease {
    pub scope: String,
    pub holder_id: String,
    pub epoch: u64,
}

/// The one concrete durable boundary for Atomic.
///
/// `Database::with` remains pure; this owner performs PostgreSQL locking,
/// publication, retry resolution, and recovery around the kernel transition.
pub struct PostgresStore {
    client: Client,
    current: BTreeMap<String, (Digest, Database)>,
    program_cache: BTreeMap<ProgramHash, Program>,
    program_lru: VecDeque<ProgramHash>,
    program_cache_capacity: usize,
}

impl PostgresStore {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        let client = Client::connect(connection, NoTls)
            .map_err(|error| postgres_error("postgres/connect", error))?;
        Ok(Self::from_client(client))
    }

    pub fn from_client(client: Client) -> Self {
        Self {
            client,
            current: BTreeMap::new(),
            program_cache: BTreeMap::new(),
            program_lru: VecDeque::new(),
            program_cache_capacity: 64,
        }
    }

    pub fn set_program_cache_capacity(&mut self, capacity: usize) {
        self.program_cache_capacity = capacity;
        while self.program_cache.len() > capacity {
            if let Some(hash) = self.program_lru.pop_front() {
                self.program_cache.remove(&hash);
            }
        }
    }

    pub fn cached_programs(&self) -> usize {
        self.program_cache.len()
    }

    pub fn migrate(&mut self) -> Result<(), SemanticError> {
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/migration-begin", error))?;
        transaction
            .query_one("SELECT pg_advisory_xact_lock($1)", &[&0x41544f4d_i64])
            .map_err(|error| postgres_error("postgres/migration-lock", error))?;
        for (version, sql) in MIGRATIONS {
            let checksum = sha256(sql.as_bytes());
            transaction
                .batch_execute(sql)
                .map_err(|error| postgres_error("postgres/migration-ddl", error))?;
            let stored = transaction
                .query_opt(
                    "SELECT checksum FROM atomic_schema_migrations WHERE version = $1",
                    &[version],
                )
                .map_err(|error| postgres_error("postgres/migration-read", error))?;
            if let Some(row) = stored {
                let stored: Vec<u8> = row.get(0);
                if stored.as_slice() != checksum {
                    return Err(fault(
                        "postgres/migration-checksum-mismatch",
                        format!("installed migration {version} differs from this binary"),
                    ));
                }
            } else {
                transaction
                    .execute(
                        "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
                        &[version, &&checksum[..]],
                    )
                    .map_err(|error| postgres_error("postgres/migration-record", error))?;
            }
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/migration-commit", error))
    }

    pub fn acquire_lease(
        &mut self,
        scope: &str,
        holder_id: &str,
        lease_millis: u64,
    ) -> Result<TransactorLease, SemanticError> {
        validate_lease_args(scope, holder_id, lease_millis)?;
        let lease_millis = sql_basis(lease_millis)?;
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/lease-acquire-begin", error))?;
        transaction
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended($1, 0))",
                &[&scope],
            )
            .map_err(|error| postgres_error("postgres/lease-acquire-advisory-lock", error))?;
        let current = transaction
            .query_opt(
                "SELECT holder_id, epoch, expires_at > clock_timestamp() \
                 FROM atomic_transactor_leases WHERE lease_scope = $1 FOR UPDATE",
                &[&scope],
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
                        &[&scope, &holder_id, &lease_millis],
                    )
                    .map_err(|error| postgres_error("postgres/lease-acquire-insert", error))?;
                1
            }
            Some(row) => {
                let current_holder: String = row.get(0);
                let current_epoch: i64 = row.get(1);
                let live: bool = row.get(2);
                if live && current_holder != holder_id {
                    return Err(SemanticError::new(
                        ErrorCategory::Unavailable,
                        "postgres/lease-held",
                        format!("lease {scope} is held by another transactor"),
                    ));
                }
                let next_epoch = if live {
                    current_epoch
                } else {
                    current_epoch.checked_add(1).ok_or_else(|| {
                        fault("postgres/lease-epoch-overflow", "lease epoch overflow")
                    })?
                };
                transaction
                    .execute(
                        "UPDATE atomic_transactor_leases \
                         SET holder_id = $2, epoch = $3, \
                             expires_at = clock_timestamp() + \
                                          $4::bigint * interval '1 millisecond' \
                         WHERE lease_scope = $1",
                        &[&scope, &holder_id, &next_epoch, &lease_millis],
                    )
                    .map_err(|error| postgres_error("postgres/lease-acquire-update", error))?;
                pg_basis(next_epoch, "lease epoch")?
            }
        };
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/lease-acquire-commit", error))?;
        Ok(TransactorLease {
            scope: scope.to_owned(),
            holder_id: holder_id.to_owned(),
            epoch,
        })
    }

    pub fn renew_lease(
        &mut self,
        lease: &TransactorLease,
        lease_millis: u64,
    ) -> Result<(), SemanticError> {
        validate_lease_args(&lease.scope, &lease.holder_id, lease_millis)?;
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
                &[&lease.scope, &lease.holder_id, &epoch, &lease_millis],
            )
            .map_err(|error| postgres_error("postgres/lease-renew", error))?;
        if updated == 1 {
            Ok(())
        } else {
            Err(leadership_lost(lease))
        }
    }

    pub fn release_lease(&mut self, lease: &TransactorLease) -> Result<(), SemanticError> {
        let epoch = sql_basis(lease.epoch)?;
        let updated = self
            .client
            .execute(
                "UPDATE atomic_transactor_leases SET expires_at = clock_timestamp() \
                 WHERE lease_scope = $1 AND holder_id = $2 AND epoch = $3",
                &[&lease.scope, &lease.holder_id, &epoch],
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
        let database = Database::new(schema.clone())?;
        let encoded = encode_schema(&schema)?;
        let hash = sha256(&encoded);
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/create-begin", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_databases (database_id, bootstrap_schema, bootstrap_hash) \
                 VALUES ($1, $2, $3)",
                &[&database_id, &&encoded[..], &&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/create-catalog", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
                &[&database_id, &&GENESIS_HASH[..]],
            )
            .map_err(|error| postgres_error("postgres/create-head", error))?;
        transaction
            .commit()
            .map_err(|error| postgres_error("postgres/create-commit", error))?;
        self.current
            .insert(database_id.to_owned(), (GENESIS_HASH, database.clone()));
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
            self.client
                .query_opt(
                    "SELECT 1 FROM atomic_databases WHERE database_id = $1",
                    &[&database_id],
                )
                .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
                .ok_or_else(|| not_found(database_id))?;
            GENESIS_HASH
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

    /// Persist an immutable content-addressed program and bind a database-local
    /// name/version to it. Repeating the identical deployment is idempotent;
    /// rebinding a version fails.
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
        self.cache_program(hash, program.clone());
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
        if let Some(program) = self.program_cache.get(&hash).cloned() {
            self.touch_program(hash);
            return Ok(program);
        }
        let row = self
            .client
            .query_opt(
                "SELECT kind, arity, payload FROM atomic_programs WHERE program_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| postgres_error("postgres/program-read", error))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "postgres/program-not-found",
                    "program hash is not deployed",
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
        if program_kind_i16(program.kind) != kind || i16::from(program.arity) != arity {
            return Err(fault(
                "postgres/program-metadata-mismatch",
                "persisted program metadata does not match canonical bytes",
            ));
        }
        self.cache_program(hash, program.clone());
        Ok(program)
    }

    fn cache_program(&mut self, hash: ProgramHash, program: Program) {
        if self.program_cache_capacity == 0 {
            return;
        }
        self.program_cache.insert(hash, program);
        self.touch_program(hash);
        while self.program_cache.len() > self.program_cache_capacity {
            if let Some(evicted) = self.program_lru.pop_front() {
                self.program_cache.remove(&evicted);
            }
        }
    }

    fn touch_program(&mut self, hash: ProgramHash) {
        self.program_lru.retain(|cached| cached != &hash);
        self.program_lru.push_back(hash);
    }

    #[allow(clippy::too_many_arguments)]
    pub fn transact_program(
        &mut self,
        database_id: &str,
        name: &str,
        request_key: &str,
        expected_basis_t: u64,
        arguments: &[Value],
        tx_instant: i64,
        control: ProgramControl<'_>,
    ) -> Result<ProgramCommitReceipt, SemanticError> {
        let (version, hash, _) = self.resolve_active_program(database_id, name)?;
        let mut receipt = self.transact_program_hash(
            database_id,
            hash,
            request_key,
            expected_basis_t,
            arguments,
            tx_instant,
            control,
        )?;
        receipt.program_version = Some(version);
        Ok(receipt)
    }

    /// Execute an exact immutable program version against the locked
    /// db-before. Its hash and every active persisted attribute predicate hash
    /// are part of idempotency identity; only emitted ordinary operations are
    /// stored in history.
    #[allow(clippy::too_many_arguments)]
    pub fn transact_program_hash(
        &mut self,
        database_id: &str,
        hash: ProgramHash,
        request_key: &str,
        expected_basis_t: u64,
        arguments: &[Value],
        tx_instant: i64,
        control: ProgramControl<'_>,
    ) -> Result<ProgramCommitReceipt, SemanticError> {
        let program = self.resolve_program(hash)?;
        if program.kind != ProgramKind::Transaction {
            return Err(SemanticError::incorrect(
                "program/not-transaction-function",
                "requested program is not a transaction function",
            ));
        }
        let predicate_db = self.recover(database_id)?;
        let (functions, predicate_hashes) =
            self.persisted_predicates(database_id, &predicate_db)?;
        let request_hash = program_request_digest(
            hash,
            arguments,
            tx_instant,
            expected_basis_t,
            &predicate_hashes,
        )?;
        let arguments = arguments.to_vec();
        let commit = self.transact_generated(
            database_id,
            request_key,
            expected_basis_t,
            tx_instant,
            request_hash,
            CommitFault::None,
            None,
            Some(&functions),
            move |db_before| match ProgramRuntime
                .execute(&program, db_before, &arguments, control)?
            {
                ProgramOutput::Transaction(ops) => Ok(ops),
                _ => unreachable!("program kind was checked"),
            },
        )?;
        Ok(ProgramCommitReceipt {
            commit,
            program_hash: hash,
            program_version: None,
        })
    }

    /// Compose multiple exact transaction programs into one unordered atomic
    /// transaction. Every invocation reads the same locked db-before; no
    /// invocation can observe another invocation's emitted operations.
    pub fn transact_programs(
        &mut self,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        invocations: &[ProgramInvocation],
        tx_instant: i64,
        control: ProgramControl<'_>,
    ) -> Result<CommitReceipt, SemanticError> {
        if invocations.is_empty() {
            return Err(SemanticError::incorrect(
                "program/empty-invocations",
                "at least one program invocation is required",
            ));
        }
        let mut resolved = Vec::with_capacity(invocations.len());
        for invocation in invocations {
            let program = self.resolve_program(invocation.hash)?;
            if program.kind != ProgramKind::Transaction {
                return Err(SemanticError::incorrect(
                    "program/not-transaction-function",
                    "every invoked program must be a transaction function",
                ));
            }
            resolved.push((invocation.clone(), program));
        }
        let predicate_db = self.recover(database_id)?;
        let (functions, predicate_hashes) =
            self.persisted_predicates(database_id, &predicate_db)?;
        let mut invocation_digests = Vec::with_capacity(invocations.len());
        for invocation in invocations {
            invocation_digests.push(program_request_digest(
                invocation.hash,
                &invocation.arguments,
                tx_instant,
                expected_basis_t,
                &predicate_hashes,
            )?);
        }
        invocation_digests.sort();
        let mut request_bytes = Vec::with_capacity(invocation_digests.len() * 32);
        for digest in invocation_digests {
            request_bytes.extend_from_slice(&digest);
        }
        let request_hash = sha256(&request_bytes);
        self.transact_generated(
            database_id,
            request_key,
            expected_basis_t,
            tx_instant,
            request_hash,
            CommitFault::None,
            None,
            Some(&functions),
            move |db_before| {
                let mut operations = Vec::new();
                for (invocation, program) in &resolved {
                    let ProgramOutput::Transaction(mut emitted) = ProgramRuntime.execute(
                        program,
                        db_before,
                        &invocation.arguments,
                        control,
                    )?
                    else {
                        unreachable!("program kind was checked");
                    };
                    operations.append(&mut emitted);
                }
                Ok(operations)
            },
        )
    }

    /// Apply ordinary operations while resolving schema predicate names to
    /// exact persisted hashes for this attempt.
    pub fn transact_with_persisted_predicates(
        &mut self,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<CommitReceipt, SemanticError> {
        let predicate_db = self.recover(database_id)?;
        let (functions, hashes) = self.persisted_predicates(database_id, &predicate_db)?;
        let base = request_digest(ops, tx_instant, expected_basis_t)?;
        let request_hash = request_hash_with_predicates(base, &hashes);
        self.transact_generated(
            database_id,
            request_key,
            expected_basis_t,
            tx_instant,
            request_hash,
            CommitFault::None,
            None,
            Some(&functions),
            |_| Ok(ops.to_vec()),
        )
    }

    fn persisted_predicates(
        &mut self,
        database_id: &str,
        database: &Database,
    ) -> Result<(TxFunctions, Vec<ProgramHash>), SemanticError> {
        let mut names: Vec<_> = database
            .schema()
            .attributes()
            .flat_map(|attribute| attribute.predicates.iter().cloned())
            .collect();
        names.sort();
        names.dedup();
        let mut functions = TxFunctions::new();
        let mut hashes = Vec::new();
        for name in names {
            let (_, hash, program) = self.resolve_active_program(database_id, &name)?;
            if program.kind != ProgramKind::AttributePredicate {
                return Err(SemanticError::incorrect(
                    "program/not-attribute-predicate",
                    format!("active program {name} is not an attribute predicate"),
                ));
            }
            hashes.push(hash);
            let predicate_db = database.clone();
            functions.register_attribute_predicate(name, move |value| {
                match ProgramRuntime.execute(
                    &program,
                    &predicate_db,
                    std::slice::from_ref(value),
                    ProgramControl::default(),
                )? {
                    ProgramOutput::AttributePredicate(value) => Ok(value),
                    _ => unreachable!("program kind was checked"),
                }
            });
        }
        Ok((functions, hashes))
    }

    pub fn transact(
        &mut self,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<CommitReceipt, SemanticError> {
        self.transact_with_fault(
            database_id,
            request_key,
            expected_basis_t,
            ops,
            tx_instant,
            CommitFault::None,
        )
    }

    pub fn transact_fenced(
        &mut self,
        lease: &TransactorLease,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<CommitReceipt, SemanticError> {
        let request_hash = request_digest(ops, tx_instant, expected_basis_t)?;
        self.transact_generated(
            database_id,
            request_key,
            expected_basis_t,
            tx_instant,
            request_hash,
            CommitFault::None,
            Some(lease),
            None,
            |_| Ok(ops.to_vec()),
        )
    }

    /// Deterministic publication kill points used by real-PostgreSQL tests.
    /// Pre-commit points return an injected failure and rely on transaction
    /// drop rollback. The final point commits, then reports UnknownOutcome.
    pub fn transact_with_fault(
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
            expected_basis_t,
            tx_instant,
            request_hash,
            fault_point,
            None,
            None,
            |_| Ok(ops.to_vec()),
        )
    }

    #[allow(clippy::too_many_arguments)]
    fn transact_generated<F>(
        &mut self,
        database_id: &str,
        request_key: &str,
        expected_basis_t: u64,
        tx_instant: i64,
        request_hash: Digest,
        fault_point: CommitFault,
        lease: Option<&TransactorLease>,
        functions: Option<&TxFunctions>,
        generate: F,
    ) -> Result<CommitReceipt, SemanticError>
    where
        F: FnOnce(&Database) -> Result<Vec<TxOp>, SemanticError>,
    {
        if request_key.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-request-key",
                "idempotency request key cannot be empty",
            ));
        }
        let expected_basis = sql_basis(expected_basis_t)?;
        let cached = self.current.get(database_id).cloned();
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/transact-begin", error))?;
        if let Some(lease) = lease {
            verify_lease(&mut transaction, lease)?;
        }

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
            transaction
                .commit()
                .map_err(|error| postgres_error("postgres/idempotency-read-commit", error))?;
            let receipt = receipt(recovered, true);
            if basis == head_basis && hash == head_hash {
                self.current
                    .insert(database_id.to_owned(), (hash, receipt.database.clone()));
            }
            return Ok(receipt);
        }

        if head_basis_i64 != expected_basis {
            return Err(SemanticError::conflict(
                "postgres/stale-basis",
                format!("expected basis {expected_basis_t}, current basis is {head_basis}"),
            ));
        }

        let db_before = match cached {
            Some((hash, database)) if hash == head_hash && database.basis_t() == head_basis => {
                database
            }
            _ => recover_to(&mut transaction, database_id, head_basis, head_hash)?.database,
        };
        let ops = generate(&db_before)?;
        let report = match functions {
            Some(functions) => db_before.with_function_context(&ops, functions, tx_instant)?,
            None => db_before.with(&ops, tx_instant)?,
        };
        let envelope = DurableTransaction {
            database_id: database_id.to_owned(),
            basis_t: report.db_after.basis_t(),
            previous_hash: head_hash,
            next_eid: report.db_after.next_eid(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
            schema_changes: report.schema_changes.clone(),
        };
        let payload = encode_transaction(&envelope)?;
        let tx_hash = transaction_hash(&payload);
        let next_basis = sql_basis(envelope.basis_t)?;

        if fault_point == CommitFault::BeforeTransactionInsert {
            return Err(injected("before transaction insert"));
        }
        transaction
            .execute(
                "INSERT INTO atomic_transactions \
                 (database_id, basis_t, previous_hash, tx_hash, payload) \
                 VALUES ($1, $2, $3, $4, $5)",
                &[
                    &database_id,
                    &next_basis,
                    &&head_hash[..],
                    &&tx_hash[..],
                    &&payload[..],
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
                    &expected_basis,
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

        transaction.commit().map_err(|error| {
            if error.as_db_error().is_some() {
                postgres_error("postgres/commit-rejected", error)
            } else {
                unknown_outcome(request_key, error.to_string())
            }
        })?;
        if fault_point == CommitFault::AfterCommitBeforeResponse {
            return Err(unknown_outcome(
                request_key,
                "injected acknowledgment loss after PostgreSQL commit",
            ));
        }
        let receipt = CommitReceipt {
            database: report.db_after,
            basis_t: envelope.basis_t,
            tx_hash,
            tempids: envelope.tempids,
            tx_data: envelope.tx_data,
            schema_changes: envelope.schema_changes,
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
            "SELECT bootstrap_schema, bootstrap_hash FROM atomic_databases \
             WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("postgres/recovery-catalog", error))?
        .ok_or_else(|| not_found(database_id))?;
    let bootstrap: Vec<u8> = catalog.get(0);
    let bootstrap_hash = digest(catalog.get::<_, Vec<u8>>(1), "bootstrap schema hash")?;
    if sha256(&bootstrap) != bootstrap_hash {
        return Err(fault(
            "recovery/bootstrap-checksum-mismatch",
            "bootstrap schema row does not match its digest",
        ));
    }
    let schema = decode_schema(&bootstrap)?;
    let mut database = Database::new(schema)?;
    let target_basis_sql = sql_basis(target_basis)?;
    let rows = client
        .query(
            "SELECT basis_t, previous_hash, tx_hash, payload \
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

    let mut previous_hash = GENESIS_HASH;
    let mut final_transaction = None;
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
    Ok(Recovered {
        database,
        final_transaction,
        final_hash: previous_hash,
    })
}

fn receipt(recovered: Recovered, replayed: bool) -> CommitReceipt {
    let basis_t = recovered.database.basis_t();
    let transaction = recovered
        .final_transaction
        .expect("idempotent outcomes always have a positive basis");
    CommitReceipt {
        database: recovered.database,
        basis_t,
        tx_hash: recovered.final_hash,
        tempids: transaction.tempids,
        tx_data: transaction.tx_data,
        schema_changes: transaction.schema_changes,
        replayed,
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
