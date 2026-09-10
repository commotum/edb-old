#![allow(dead_code)]

#[cfg(unix)]
pub mod product_support;

use atomic_core::{
    CapacityLimits, Database, DatabaseValue, Datom, IndexOrder, Schema, SemanticError,
    ServiceTransactionReport, TransactionClient, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, View,
};
use postgres::Client;
use std::sync::{
    Arc,
    atomic::{AtomicU64, Ordering},
};
use std::time::Duration;

static NEXT_HOLDER: AtomicU64 = AtomicU64::new(1);

/// Disposable schema-scoped PostgreSQL fixture. Fault tests must not mutate
/// shared catalogs or invoke global reclamation against unrelated databases.
pub struct PostgresFixture {
    admin: Client,
    pub schema: String,
    pub connection: String,
}

impl PostgresFixture {
    pub fn new(connection: &str, label: &str) -> Self {
        assert!(
            label
                .bytes()
                .all(|byte| byte.is_ascii_lowercase() || byte == b'_')
        );
        let schema = format!(
            "{label}_{}_{}",
            std::process::id(),
            std::time::SystemTime::now()
                .duration_since(std::time::UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut admin = Client::connect(connection, postgres::NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let scoped =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if connection.contains('?') { "&" } else { "?" }
                )
            } else {
                format!("{connection} options='-csearch_path={schema},pg_catalog'")
            };
        Self {
            admin,
            schema,
            connection: scoped,
        }
    }
}

impl Drop for PostgresFixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

/// Uniform test-only observation of eager oracle values and native report
/// values. Production code deliberately has no eager materialization escape
/// hatch; differential fixtures compare through exact ordered reads.
pub trait InformationSource {
    fn test_basis_t(&self) -> u64;
    fn test_eidx_frontier(&self) -> u64;
    fn test_schema(&self) -> &Schema;
    fn test_datoms(&self, view: View, order: IndexOrder) -> Vec<Datom>;
}

impl InformationSource for Database {
    fn test_basis_t(&self) -> u64 {
        self.basis_t()
    }

    fn test_eidx_frontier(&self) -> u64 {
        self.eidx_frontier()
    }

    fn test_schema(&self) -> &Schema {
        self.schema()
    }

    fn test_datoms(&self, view: View, order: IndexOrder) -> Vec<Datom> {
        self.datoms(view, order)
    }
}

impl InformationSource for Arc<Database> {
    fn test_basis_t(&self) -> u64 {
        self.basis_t()
    }

    fn test_eidx_frontier(&self) -> u64 {
        self.eidx_frontier()
    }

    fn test_schema(&self) -> &Schema {
        self.schema()
    }

    fn test_datoms(&self, view: View, order: IndexOrder) -> Vec<Datom> {
        self.datoms(view, order)
    }
}

impl InformationSource for DatabaseValue {
    fn test_basis_t(&self) -> u64 {
        self.basis_t()
    }

    fn test_eidx_frontier(&self) -> u64 {
        self.eidx_frontier()
    }

    fn test_schema(&self) -> &Schema {
        self.schema()
    }

    fn test_datoms(&self, view: View, order: IndexOrder) -> Vec<Datom> {
        match view {
            View::Current => self.datoms(order),
            View::History => self.clone().history().datoms(order),
            View::AsOf(t) => self.clone().as_of(t).datoms(order),
            View::Since(t) => self.clone().since(t).datoms(order),
        }
        .expect("exact test database read")
    }
}

pub fn assert_same_information(left: &impl InformationSource, right: &impl InformationSource) {
    assert_eq!(left.test_basis_t(), right.test_basis_t());
    assert_eq!(left.test_eidx_frontier(), right.test_eidx_frontier());
    assert_eq!(left.test_schema(), right.test_schema());
    for view in [View::Current, View::History] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(
                left.test_datoms(view, order),
                right.test_datoms(view, order)
            );
        }
    }
}

/// Deliberately bypass ordinary PostgreSQL triggers for one tightly scoped
/// fault-injection or maintenance operation, then restore them before
/// returning. Production code must never use this as an ordinary write path.
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
