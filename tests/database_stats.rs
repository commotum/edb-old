use atomic_core::{
    Attribute, BackgroundIndexingConfig, Cardinality, Connection, Database, DatabaseStats,
    DatabaseValue, EntityRef, ErrorCategory, IndexOrder, Keyword, PostgresMigrator, PostgresStore,
    QueryControl, Schema, TransactionRequest, TransactionService, TransactionServiceConfig, TxOp,
    Value, ValueType,
};
use postgres::{Client, NoTls};
use std::sync::{
    Arc,
    atomic::{AtomicBool, Ordering},
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const HOURS: u32 = 1_000;
const WAIT: Duration = Duration::from_secs(15);

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            HOURS,
            Keyword::new("project", "hours"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn set(entity: EntityRef, hours: i64) -> Vec<TxOp> {
    vec![TxOp::Add {
        entity,
        attribute: HOURS,
        value: Value::Long(hours).into(),
    }]
}

fn expected(value: &DatabaseValue) -> DatabaseStats {
    let mut stats = DatabaseStats::default();
    for datom in value
        .clone()
        .history()
        .collect_datoms(IndexOrder::Eavt)
        .unwrap()
    {
        stats.datoms += 1;
        stats.attrs.entry(datom.attribute).or_default().count += 1;
    }
    stats
}

#[test]
fn counts_are_history_based_and_respect_immutable_views() {
    let first = Database::new(schema())
        .unwrap()
        .with(&set(EntityRef::Temp("project".into()), 3), 1_000)
        .unwrap();
    let old = first.db_after.database_value();
    let second = first
        .db_after
        .with(&set(EntityRef::Id(first.tempids["project"]), 7), 2_000)
        .unwrap();
    let current = second.db_after.database_value();
    assert_eq!(old.db_stats().unwrap().attrs[&HOURS].count, 1);
    assert_eq!(current.db_stats().unwrap().attrs[&HOURS].count, 3);
    assert_eq!(
        current.db_stats().unwrap(),
        current.clone().history().db_stats().unwrap()
    );
    for view in [
        current.clone(),
        current.clone().as_of(old.basis_t()),
        current.clone().since(old.basis_t()),
        current.clone().filter(|_, datom| datom.attribute == HOURS),
        current.clone().history().filter(|_, datom| !datom.added),
    ] {
        assert_eq!(view.db_stats().unwrap(), expected(&view));
    }
    assert_eq!(old.db_stats().unwrap().attrs[&HOURS].count, 1);
    let retractions = current.filter(|_, datom| !datom.added).db_stats().unwrap();
    assert_eq!(retractions.datoms, 1);
    assert_eq!(retractions.attrs[&HOURS].count, 1);
}

#[test]
fn stats_control_checks_empty_scans_cancellation_work_and_extreme_timeout() {
    let value = Database::new(schema()).unwrap().database_value();
    let empty = value.clone().filter(|_, _| false);
    let cancel = Arc::new(AtomicBool::new(true));
    let control = QueryControl {
        cancel: Arc::clone(&cancel),
        ..Default::default()
    };
    assert_eq!(
        empty.db_stats_with_control(&control).unwrap_err().code,
        "database-stats/canceled"
    );
    cancel.store(false, Ordering::Relaxed);
    let canceled_inside_filter = value.clone().filter(move |_, _| {
        cancel.store(true, Ordering::Relaxed);
        false
    });
    assert_eq!(
        canceled_inside_filter
            .db_stats_with_control(&control)
            .unwrap_err()
            .code,
        "database-stats/canceled"
    );
    let zero_time = QueryControl {
        timeout: Some(Duration::ZERO),
        ..Default::default()
    };
    assert_eq!(
        empty.db_stats_with_control(&zero_time).unwrap_err().code,
        "database-stats/timeout"
    );
    let zero_work = QueryControl {
        max_work: 0,
        ..Default::default()
    };
    assert_eq!(
        empty.db_stats_with_control(&zero_work).unwrap(),
        DatabaseStats::default()
    );
    assert_eq!(
        value.db_stats_with_control(&zero_work).unwrap_err().code,
        "database-stats/work-limit"
    );
    let extreme = QueryControl {
        timeout: Some(Duration::MAX),
        ..Default::default()
    };
    assert_eq!(
        value.db_stats_with_control(&extreme).unwrap(),
        value.db_stats().unwrap()
    );
}

struct Fixture {
    admin: Client,
    schema: String,
    connection: String,
}

impl Fixture {
    fn new(connection: &str) -> Self {
        let schema = format!(
            "stats_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut admin = Client::connect(connection, NoTls).unwrap();
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

impl Drop for Fixture {
    fn drop(&mut self) {
        // Only the generated schema owned by this fixture is removed.
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

#[test]
fn native_stats_and_finite_index_requests_use_the_existing_worker() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED native statistics/index request check: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    let connection = &fixture.connection;
    const DATABASE: &str = "statistics";
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresStore::connect(connection)
        .unwrap()
        .create_database(DATABASE, schema())
        .unwrap();
    let service = TransactionService::start_with_indexing(
        TransactionServiceConfig {
            connection: connection.clone(),
            database_id: DATABASE.into(),
            holder_id: "stats-writer".into(),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 8,
            capacity_limits: Default::default(),
        },
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let peer = Connection::attach(connection, client.clone(), 8).unwrap();
    let first = peer
        .transact(
            TransactionRequest::new("first", set(EntityRef::Temp("project".into()), 3)),
            WAIT,
        )
        .unwrap();
    let entity = first.tempids["project"];
    let old = first.db_after;
    let old_stats = old.db_stats().unwrap();
    let second = peer
        .transact(
            TransactionRequest::new("second", set(EntityRef::Id(entity), 7)),
            WAIT,
        )
        .unwrap();
    let current = second.db_after;
    assert_eq!(current.db_stats().unwrap(), expected(&current));
    assert_eq!(current.db_stats().unwrap().attrs[&HOURS].count, 3);
    assert_eq!(
        current.clone().as_of(old.basis_t()).db_stats().unwrap(),
        old_stats
    );
    let request = peer.request_index().unwrap();
    assert_eq!(request.target_t, current.basis_t());
    assert!(request.scheduled);
    let duplicate = client.request_index().unwrap();
    assert_eq!(duplicate.target_t, request.target_t);
    assert!(!duplicate.scheduled);
    for ordinal in 0..4 {
        peer.transact(
            TransactionRequest::new(
                format!("newer-{ordinal}"),
                set(EntityRef::Id(entity), 10 + ordinal),
            ),
            WAIT,
        )
        .unwrap();
    }
    let indexed = peer.sync_index(request.target_t, WAIT).unwrap();
    assert!(indexed.basis_t() >= request.target_t);
    assert_eq!(
        request.target_t,
        current.basis_t(),
        "later commits extended a captured target"
    );
    assert!(peer.db().basis_t() > request.target_t);
    let latest_request = peer.request_index().unwrap();
    assert_eq!(latest_request.target_t, peer.db().basis_t());
    peer.sync_index(latest_request.target_t, WAIT).unwrap();
    assert!(!peer.request_index().unwrap().scheduled);
    assert_eq!(old.db_stats().unwrap(), old_stats);
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    let reader = Connection::connect(connection, DATABASE, 8).unwrap();
    assert_eq!(
        reader.request_index().unwrap_err().code,
        "connection/no-writer"
    );
    assert_eq!(
        reader.db().db_stats().unwrap(),
        peer.db().db_stats().unwrap()
    );
    service.shutdown();
    assert_eq!(
        peer.request_index().unwrap_err().category,
        ErrorCategory::Unavailable
    );
    assert_eq!(
        client.request_index().unwrap_err().category,
        ErrorCategory::Unavailable
    );
    println!(
        "DATABASE_MAINTENANCE_OK finite_target={} latest_target={} history_count={} eager_materializations=0",
        request.target_t,
        latest_request.target_t,
        peer.db().db_stats().unwrap().datoms
    );
}
