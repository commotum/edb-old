//! Catalog names resolve at ingress only; captured handles never follow reuse.
mod common;
use atomic_core::{
    Attribute, CapacityLimits, Cardinality, Connection, DatabaseCatalog, EntityRef, Keyword, Peer,
    PostgresConnectionConfig, PostgresIndexer, PostgresMigrator, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TransactionStandby, TxOp, Value, ValueType,
};
use std::time::Duration;

const COUNT: u32 = 1000;
const WAIT: Duration = Duration::from_secs(10);

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn service_config(connection: &str, name: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id: name.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(200),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}
fn request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: COUNT,
            value: Value::Long(value).into(),
        }],
    )
}
fn fixture(label: &str) -> Option<common::PostgresFixture> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP lifecycle handles: ATOMIC_POSTGRES_URL is unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    Some(fixture)
}

#[test]
fn renamed_handles_and_waiting_standby_ignore_reused_public_name() {
    let Some(fixture) = fixture("lifecycle_handle_rename") else {
        return;
    };
    let connection = &fixture.connection;
    let config = PostgresConnectionConfig::plaintext(connection);
    let mut catalog = DatabaseCatalog::connect(connection).unwrap();
    let original = catalog
        .create_if_absent("original", schema())
        .unwrap()
        .database;
    let writer =
        TransactionService::start(service_config(connection, "original", "first")).unwrap();
    let attached = Connection::attach(connection, writer.client(), 0).unwrap();
    let first = attached.transact(request("first", 11), WAIT).unwrap();
    let retained = first.db_after.clone();
    let reference = retained.snapshot_reference().unwrap();
    let identity = attached.identity().clone();
    assert_eq!(identity.database_id(), original.database_id);
    let standby = TransactionStandby::start(
        service_config(connection, "original", "waiting"),
        Duration::from_millis(20),
    )
    .unwrap();

    catalog.rename("original", "renamed").unwrap();
    assert!(
        matches!(Peer::connect(connection, "original", 0), Err(error) if error.code == "catalog/name-not-found")
    );
    assert!(
        matches!(PostgresIndexer::connect(connection, "original"), Err(error) if error.code == "catalog/name-not-found")
    );
    let replacement = catalog
        .create_if_absent("original", schema())
        .unwrap()
        .database;
    assert_ne!(replacement.database_id, original.database_id);
    assert_ne!(replacement.lineage_id, original.lineage_id);
    let new_writer =
        TransactionService::start(service_config(connection, "original", "replacement")).unwrap();
    let named = Connection::connect(connection, "original", 0).unwrap();
    assert_eq!(named.identity().database_id(), replacement.database_id);
    let renamed = Connection::connect(connection, "renamed", 0).unwrap();
    assert_eq!(renamed.identity(), &identity);
    // Attaching after rename uses the writer's identity, not its former name.
    let late_attachment = Connection::attach(connection, writer.client(), 0).unwrap();
    assert_eq!(late_attachment.identity(), &identity);
    let second = late_attachment
        .transact(request("second", 22), WAIT)
        .unwrap();
    let mut indexer = PostgresIndexer::connect(connection, "renamed").unwrap();
    assert_eq!(indexer.consolidate().unwrap().basis_t, second.basis_t);
    assert_eq!(
        reference
            .open(&config, 0, 0)
            .unwrap()
            .snapshot_key()
            .unwrap(),
        retained.snapshot_key().unwrap()
    );
    assert_eq!(
        attached
            .reopen_snapshot(&reference)
            .unwrap()
            .snapshot_key()
            .unwrap(),
        retained.snapshot_key().unwrap()
    );
    assert_eq!(
        attached
            .sync()
            .unwrap()
            .values(second.tempids["item"], COUNT)
            .unwrap(),
        vec![Value::Long(22)]
    );
    assert!(
        named
            .db()
            .values(first.tempids["item"], COUNT)
            .unwrap()
            .is_empty()
    );

    writer.shutdown();
    let activated = standby.await_active(WAIT).unwrap();
    assert_eq!(activated.client().identity(), identity);
    let after = activated
        .client()
        .transact(request("standby", 33), WAIT)
        .unwrap();
    assert_eq!(after.db_before.basis_t(), second.basis_t);
    assert_eq!(
        new_writer.client().identity().database_id(),
        replacement.database_id
    );
    activated.shutdown();
    new_writer.shutdown();
}

#[test]
fn retirement_forbids_new_opens_but_keeps_captured_values_and_identity_isolation() {
    let Some(fixture) = fixture("lifecycle_handle_retire") else {
        return;
    };
    let connection = &fixture.connection;
    let config = PostgresConnectionConfig::plaintext(connection);
    let mut catalog = DatabaseCatalog::connect(connection).unwrap();
    let original = catalog
        .create_if_absent("items", schema())
        .unwrap()
        .database;
    let writer =
        TransactionService::start(service_config(connection, "items", "retiring")).unwrap();
    let attached = Connection::attach(connection, writer.client(), 0).unwrap();
    let receipt = attached
        .transact(request("before-retire", 7), WAIT)
        .unwrap();
    let eid = receipt.tempids["item"];
    let mut indexer = PostgresIndexer::connect(connection, "items")
        .unwrap()
        .with_segment_datoms(4)
        .unwrap();
    indexer.consolidate().unwrap();
    let cold_peer = Peer::connect(connection, "items", 0).unwrap();
    let retained = cold_peer.database_value();
    let reference = retained.snapshot_reference().unwrap();
    let before_reads = cold_peer.load_stats().cursor_sql_reads;
    let standby = TransactionStandby::start(
        service_config(connection, "items", "must-not-retarget"),
        Duration::from_millis(20),
    )
    .unwrap();
    catalog.retire("items").unwrap();
    assert!(
        matches!(Peer::connect(connection, "items", 0), Err(error) if error.code == "catalog/name-not-found")
    );
    assert!(
        matches!(Connection::attach(connection, writer.client(), 0), Err(error) if error.code == "catalog/database-retired")
    );
    assert!(
        matches!(reference.open(&config, 0, 0), Err(error) if error.code == "catalog/database-retired")
    );
    assert!(
        matches!(attached.reopen_snapshot(&reference), Err(error) if error.code == "catalog/database-retired")
    );
    assert_eq!(
        indexer.consolidate().unwrap_err().code,
        "catalog/database-retired"
    );
    assert_eq!(retained.values(eid, COUNT).unwrap(), vec![Value::Long(7)]);
    assert!(
        cold_peer.load_stats().cursor_sql_reads > before_reads,
        "retained value must perform a genuine cold PostgreSQL page read after retirement"
    );
    assert_eq!(
        attached.db().values(eid, COUNT).unwrap(),
        vec![Value::Long(7)]
    );
    let replacement = catalog
        .create_if_absent("items", schema())
        .unwrap()
        .database;
    assert_ne!(replacement.database_id, original.database_id);
    writer.shutdown();
    let error = match standby.await_active(WAIT) {
        Ok(service) => {
            service.shutdown();
            panic!("retired standby activated");
        }
        Err(error) => error,
    };
    assert_eq!(error.code, "catalog/database-retired");
    let replacement_writer =
        TransactionService::start(service_config(connection, "items", "new")).unwrap();
    let new_connection = Connection::connect(connection, "items", 0).unwrap();
    assert_eq!(
        new_connection.identity().database_id(),
        replacement.database_id
    );
    assert_ne!(new_connection.identity(), attached.identity());
    assert_eq!(retained.values(eid, COUNT).unwrap(), vec![Value::Long(7)]);
    replacement_writer.shutdown();
}
