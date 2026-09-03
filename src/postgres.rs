use crate::{
    Database, Datom, Digest, DurableTransaction, ErrorCategory, Schema, SchemaChange,
    SemanticError, TxOp, decode_schema, decode_transaction, encode_schema, encode_transaction,
    request_digest, sha256, transaction_hash,
};
use postgres::{Client, GenericClient, NoTls};
use std::collections::BTreeMap;

const MIGRATIONS: &[(i64, &str)] = &[
    (1, include_str!("../migrations/0001_atomic.sql")),
    (2, include_str!("../migrations/0002_peer_indexes.sql")),
];
const GENESIS_HASH: Digest = [0; 32];

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

/// The one concrete durable boundary for Atomic.
///
/// `Database::with` remains pure; this owner performs PostgreSQL locking,
/// publication, retry resolution, and recovery around the kernel transition.
pub struct PostgresStore {
    client: Client,
    current: BTreeMap<String, (Digest, Database)>,
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
        }
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
        if request_key.is_empty() {
            return Err(SemanticError::incorrect(
                "postgres/empty-request-key",
                "idempotency request key cannot be empty",
            ));
        }
        let request_hash = request_digest(ops, tx_instant, expected_basis_t)?;
        let expected_basis = sql_basis(expected_basis_t)?;
        let cached = self.current.get(database_id).cloned();
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("postgres/transact-begin", error))?;

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
        let report = db_before.with(ops, tx_instant)?;
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
