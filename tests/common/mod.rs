use atomic_core::{
    CapacityLimits, SemanticError, ServiceTransactionReport, TransactionClient, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp,
};
use postgres::Client;
use std::sync::atomic::{AtomicU64, Ordering};
use std::time::Duration;

static NEXT_HOLDER: AtomicU64 = AtomicU64::new(1);

/// Deliberately bypass ordinary PostgreSQL triggers for one tightly scoped
/// fault-injection or maintenance operation, then restore them before
/// returning. Production code must never use this as an ordinary write path.
#[allow(dead_code)]
pub fn with_replica_triggers_disabled<T>(
    client: &mut Client,
    operation: impl FnOnce(&mut Client) -> Result<T, postgres::Error>,
) -> Result<T, postgres::Error> {
    client.batch_execute("SET session_replication_role = replica")?;
    let result = operation(client);
    let restored = client.batch_execute("SET session_replication_role = origin");
    match result {
        Ok(value) => {
            restored?;
            Ok(value)
        }
        Err(error) => {
            let _ = restored;
            Err(error)
        }
    }
}

/// Start the same database-bound transactor used by application callers.
///
/// PostgreSQL fixtures use this instead of reaching through `PostgresStore`
/// to its crate-internal publication primitive.
pub fn start_service(connection: &str, database_id: &str) -> TransactionService {
    start_service_with_limits(connection, database_id, CapacityLimits::default())
}

pub fn start_service_with_limits(
    connection: &str,
    database_id: &str,
    capacity_limits: CapacityLimits,
) -> TransactionService {
    let ordinal = NEXT_HOLDER.fetch_add(1, Ordering::Relaxed);
    TransactionService::start(TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: format!("test-{}-{ordinal}", std::process::id()),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 32,
        capacity_limits,
    })
    .unwrap()
}

/// Submit an explicit compare-basis/import-time request through the public
/// service. Existing durability fixtures use the conditional options to keep
/// their exact historical assertions while exercising the sole writer.
pub fn transact(
    service: &TransactionService,
    request_key: &str,
    compare_basis_t: u64,
    operations: &[TxOp],
    tx_instant: i64,
) -> ServiceTransactionReport {
    try_transact(
        service,
        request_key,
        compare_basis_t,
        operations,
        tx_instant,
    )
    .unwrap()
}

pub fn try_transact(
    service: &TransactionService,
    request_key: &str,
    compare_basis_t: u64,
    operations: &[TxOp],
    tx_instant: i64,
) -> Result<ServiceTransactionReport, SemanticError> {
    try_transact_client(
        &service.client(),
        request_key,
        compare_basis_t,
        operations,
        tx_instant,
    )
}

pub fn try_transact_client(
    client: &TransactionClient,
    request_key: &str,
    compare_basis_t: u64,
    operations: &[TxOp],
    tx_instant: i64,
) -> Result<ServiceTransactionReport, SemanticError> {
    client.transact(
        TransactionRequest::new(request_key, operations.to_vec())
            .comparing_basis(compare_basis_t)
            .with_tx_instant(tx_instant),
        Duration::from_secs(5),
    )
}
