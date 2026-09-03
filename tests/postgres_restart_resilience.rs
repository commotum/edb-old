use atomic_core::{
    Attribute, Cardinality, EntityRef, IndexOrder, Keyword, Peer, PostgresIndexer,
    PostgresMigrator, PostgresStore, PostgresTreeStore, Schema, TransactionRequest,
    TransactionServiceConfig, TransactionStandby, TxOp, TxValue, Value, ValueType, View,
};
use std::process::Command;
use std::thread;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const ITEM_NAME: u32 = 1_000;

struct RestartFixture {
    connection: String,
    data: String,
    pg_ctl: String,
    log: String,
    start_options: Option<String>,
    running: bool,
}

impl RestartFixture {
    fn from_environment() -> Option<Self> {
        Some(Self {
            connection: std::env::var("ATOMIC_RESTART_POSTGRES_URL").ok()?,
            data: std::env::var("ATOMIC_RESTART_POSTGRES_DATA").ok()?,
            pg_ctl: std::env::var("ATOMIC_RESTART_PG_CTL").ok()?,
            log: std::env::var("ATOMIC_RESTART_POSTGRES_LOG")
                .unwrap_or_else(|_| "/tmp/atomic-restart-postgres.log".into()),
            start_options: std::env::var("ATOMIC_RESTART_POSTGRES_OPTIONS").ok(),
            running: true,
        })
    }

    fn stop(&mut self) {
        let status = Command::new(&self.pg_ctl)
            .args(["-D", &self.data, "-m", "fast", "-w", "stop"])
            .status()
            .unwrap();
        assert!(
            status.success(),
            "dedicated PostgreSQL fixture did not stop"
        );
        self.running = false;
    }

    fn start(&mut self) {
        let mut command = Command::new(&self.pg_ctl);
        command.args(["-D", &self.data, "-l", &self.log]);
        if let Some(options) = &self.start_options {
            command.args(["-o", options]);
        }
        let status = command.args(["-w", "start"]).status().unwrap();
        assert!(
            status.success(),
            "dedicated PostgreSQL fixture did not restart"
        );
        self.running = true;
    }
}

impl Drop for RestartFixture {
    fn drop(&mut self) {
        if !self.running {
            let mut command = Command::new(&self.pg_ctl);
            command.args(["-D", &self.data, "-l", &self.log]);
            if let Some(options) = &self.start_options {
                command.args(["-o", options]);
            }
            let _ = command.args(["-w", "start"]).status();
        }
    }
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            ITEM_NAME,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn request(key: &str, name: &str, compare_basis: u64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_NAME,
            value: TxValue::Scalar(Value::String(name.into())),
        }],
    )
    .comparing_basis(compare_basis)
    .with_tx_instant(i64::try_from(compare_basis).unwrap() * 1_000)
}

fn standby_config(connection: &str, database_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id: database_id.into(),
        holder_id: unique("restart-standby"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: Default::default(),
    }
}

/// This test intentionally controls a dedicated PostgreSQL process. It never
/// uses `ATOMIC_POSTGRES_URL`, so an ordinary shared integration fixture
/// cannot be stopped accidentally.
#[test]
fn live_handles_reborrow_after_server_restart_without_losing_peer_values() {
    let Some(mut fixture) = RestartFixture::from_environment() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("restart-db");
    let mut migrator = PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();

    let service =
        atomic_core::TransactionService::start(standby_config(&connection, &database_id)).unwrap();
    let first = service
        .client()
        .transact(
            request("restart-first", "before", created.basis_t()),
            Duration::from_secs(2),
        )
        .unwrap();
    service.shutdown();

    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    let first_publication = indexer.consolidate().unwrap();
    let mut tree_store = PostgresTreeStore::connect(&connection).unwrap();
    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    let old_database = peer.try_db().unwrap();
    let old_snapshot = peer.sync_snapshot().unwrap();
    assert_eq!(old_database.basis_t(), first.basis_t);
    assert_eq!(old_snapshot.basis_t(), first.basis_t);

    fixture.stop();
    // d/db-shaped access returns the already-published immutable value while
    // both the transactor and storage process are unavailable.
    assert_eq!(peer.db().basis_t(), first.basis_t);
    assert!(
        !old_database
            .datoms(View::Current, IndexOrder::Eavt)
            .is_empty()
    );

    let waiting_peer = peer.clone();
    let target = first.basis_t + 1;
    let waiter =
        thread::spawn(move || waiting_peer.sync_to_snapshot(target, Duration::from_secs(15)));
    let standby = TransactionStandby::start(
        standby_config(&connection, &database_id),
        Duration::from_millis(20),
    )
    .unwrap();
    thread::sleep(Duration::from_millis(100));
    fixture.start();

    let active = standby.await_active(Duration::from_secs(10)).unwrap();
    let second = active
        .client()
        .transact(
            request("restart-second", "after", first.basis_t),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(second.basis_t, target);
    let synchronized = waiter.join().unwrap().unwrap();
    assert_eq!(synchronized.basis_t(), target);
    assert_eq!(old_database.basis_t(), first.basis_t);
    assert_eq!(old_snapshot.basis_t(), first.basis_t);

    store.reconnect().unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), target);
    tree_store.reconnect().unwrap();
    assert_eq!(
        tree_store
            .current_publication_revision(&database_id)
            .unwrap(),
        first_publication.publication_revision
    );
    let successor_publication = indexer.consolidate().unwrap();
    assert_eq!(successor_publication.basis_t, target);
    assert!(successor_publication.publication_revision > first_publication.publication_revision);
    peer.refresh_index().unwrap();
    active.shutdown();
}
