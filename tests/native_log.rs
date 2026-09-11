mod common;

use atomic_core::{
    Attribute, Cardinality, Connection, EntityRef, IndexOrder, IndexTransaction, Keyword,
    LogTransaction, LogValue, Peer, PostgresOperator, Schema, ServiceTransactionReport, TimePoint,
    TransactionRequest, TransactionService, TxOp, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::sync::mpsc;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const COUNT: u32 = 1_000;
const TIMEOUT: Duration = Duration::from_secs(30);

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

fn setup(no_history: bool) -> Option<(common::PostgresFixture, String, String)> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP native log: ATOMIC_POSTGRES_URL is unset");
        return None;
    };
    let scope = common::PostgresFixture::new(&connection, "native_log");
    let connection = scope.connection.clone();
    common::install(&connection).unwrap();
    let database_id = unique("native_log");
    let mut schema = Schema::new();
    let mut count = Attribute::new(
        COUNT,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    count.no_history = no_history;
    schema.install(count).unwrap();
    common::TestStore::connect(&connection)
        .unwrap()
        .create_database(&database_id, schema)
        .unwrap();
    Some((scope, connection, database_id))
}

fn write(
    service: &TransactionService,
    key: &str,
    entity: EntityRef,
    count: i64,
    instant: i64,
) -> ServiceTransactionReport {
    service
        .client()
        .transact(
            TransactionRequest::new(
                key,
                vec![TxOp::Add {
                    entity,
                    attribute: COUNT,
                    value: Value::Long(count).into(),
                }],
            )
            .with_tx_instant(instant),
            TIMEOUT,
        )
        .unwrap()
}

fn transactions(
    log: &LogValue,
    start: Option<TimePoint>,
    end: Option<TimePoint>,
) -> Vec<LogTransaction> {
    log.tx_range(start, end)
        .unwrap()
        .collect::<Result<Vec<_>, _>>()
        .unwrap()
}

#[test]
fn captured_native_log_is_lazy_time_bounded_and_preserves_no_history_transaction_data() {
    let Some((_scope, postgres, id)) = setup(true) else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let connection = Connection::connect(&postgres, &id, 4).unwrap();
    let initial_log = connection.log();
    let initial_basis = initial_log.basis_t();
    let first = write(&writer, "first", EntityRef::Temp("item".into()), 10, 1_000);
    let item = first.tempids["item"];
    let second = write(&writer, "second", EntityRef::Id(item), 20, 1_000);
    connection.sync_to(second.basis_t, TIMEOUT).unwrap();
    let captured = connection.log();
    let third = write(&writer, "third", EntityRef::Id(item), 30, 2_000);
    connection.sync_to(third.basis_t, TIMEOUT).unwrap();
    common::consolidate(&postgres, &id).unwrap();
    connection.sync_index(third.basis_t, TIMEOUT).unwrap();
    let latest = connection.log();
    let current = connection.db();
    assert!(
        !current
            .clone()
            .history()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .any(|datom| datom.entity == item
                && datom.attribute == COUNT
                && datom.value == Value::Long(10)),
        "index consolidation should exercise actual noHistory loss"
    );
    assert_eq!(
        latest.tx_data(IndexTransaction::T(first.basis_t)).unwrap(),
        Some(first.tx_data.clone())
    );
    assert_eq!(
        latest
            .tx_data(IndexTransaction::Tx(
                atomic_core::t_to_tx(second.basis_t).unwrap()
            ))
            .unwrap(),
        Some(second.tx_data.clone())
    );
    assert_eq!(
        captured
            .tx_data(IndexTransaction::T(third.basis_t))
            .unwrap(),
        None
    );
    assert_eq!(latest.tx_data(IndexTransaction::T(0)).unwrap(), None);
    assert_eq!(initial_log.basis_t(), initial_basis);
    assert_eq!(
        transactions(&initial_log, None, None).len() as u64,
        initial_basis
    );
    assert_eq!(
        transactions(&captured, None, None).len() as u64,
        second.basis_t
    );
    assert_eq!(
        transactions(&latest, None, None).len() as u64,
        third.basis_t
    );
    let ids = |start, end| {
        transactions(&latest, start, end)
            .into_iter()
            .map(|tx| tx.t)
            .collect::<Vec<_>>()
    };
    assert_eq!(
        ids(
            Some(TimePoint::Instant(1_000)),
            Some(TimePoint::Instant(2_000))
        ),
        vec![first.basis_t, second.basis_t]
    );
    assert_eq!(
        ids(Some(TimePoint::Instant(1_001)), None),
        vec![third.basis_t],
        "instant boundaries round up, not as-of down"
    );
    assert_eq!(
        ids(
            Some(TimePoint::Tx(atomic_core::t_to_tx(second.basis_t).unwrap())),
            Some(TimePoint::T(third.basis_t))
        ),
        vec![second.basis_t]
    );
    assert_eq!(
        ids(
            Some(TimePoint::T(third.basis_t)),
            Some(TimePoint::T(first.basis_t))
        ),
        Vec::<u64>::new()
    );
    assert!(
        transactions(&captured, Some(TimePoint::Instant(1_001)), None).is_empty(),
        "captured instant resolution must not see a future transaction"
    );
    assert!(ids(Some(TimePoint::Instant(2_001)), None).is_empty());
    assert!(latest.tx_range(Some(TimePoint::T(u64::MAX)), None).is_err());
    assert_eq!(
        latest
            .tx_ids(Some(TimePoint::T(first.basis_t)), None)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap(),
        [first.basis_t, second.basis_t, third.basis_t].map(|t| atomic_core::t_to_tx(t).unwrap())
    );

    let mut cursor = latest
        .tx_range(Some(TimePoint::T(first.basis_t)), None)
        .unwrap();
    assert_eq!(cursor.stats().transactions_read, 0);
    assert_eq!(cursor.stats().range_reads, 0);
    assert_eq!(cursor.next().unwrap().unwrap().data, first.tx_data);
    assert_eq!(cursor.stats().transactions_read, 1);
    assert_eq!(cursor.stats().range_reads, 1);
    assert_eq!(cursor.stats().peak_buffered_transactions, 1);
    assert!(cursor.stats().payload_bytes_read > 0);
    assert_eq!(cursor.next().unwrap().unwrap().data, second.tx_data);
    assert_eq!(cursor.next().unwrap().unwrap().data, third.tx_data);
    assert!(cursor.next().is_none());
    assert!(cursor.next().is_none());
    assert_eq!(cursor.stats().transactions_read, 3);
    assert_eq!(connection.load_stats().compatibility_materializations, 0);
    writer.shutdown();
    drop(connection);
    assert_eq!(
        transactions(&captured, Some(TimePoint::T(first.basis_t)), None).len(),
        2,
        "immutable log outlives its connection and writer"
    );
}

#[test]
fn native_log_keeps_its_exact_generation_across_excision_recovery_and_new_writes() {
    let Some((_scope, postgres, id)) = setup(false) else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let peer = Peer::connect(&postgres, &id, 4).unwrap();
    let inserted = write(
        &writer,
        "inserted",
        EntityRef::Temp("item".into()),
        7,
        1_000,
    );
    let item = inserted.tempids["item"];
    peer.sync_to(inserted.basis_t, TIMEOUT).unwrap();
    let original = peer.log();
    let mut original_cursor = original
        .tx_range(Some(TimePoint::T(inserted.basis_t)), None)
        .unwrap();
    let excision = writer
        .client()
        .transact(
            TransactionRequest::new(
                "excise",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("excision".into()),
                    attribute: atomic_core::DB_EXCISE as u32,
                    value: Value::Ref(item).into(),
                }],
            )
            .with_tx_instant(2_000),
            TIMEOUT,
        )
        .unwrap();
    writer.shutdown();
    PostgresOperator::connect(&postgres)
        .unwrap()
        .process_excision_requests(
            &atomic_core::DatabaseCatalog::connect(&postgres)
                .unwrap()
                .resolve(&id)
                .unwrap()
                .database_id,
        )
        .unwrap();
    drop(peer);
    let recovered = Peer::connect(&postgres, &id, 4).unwrap();
    let after_excision = recovered.log();
    assert!(after_excision.generation() > original.generation());
    assert_eq!(after_excision.basis_t(), excision.basis_t);
    assert_eq!(
        original_cursor.next().unwrap().unwrap().data,
        inserted.tx_data
    );
    assert!(original_cursor.next().is_none());
    assert_eq!(
        original
            .tx_data(IndexTransaction::T(inserted.basis_t))
            .unwrap(),
        Some(inserted.tx_data.clone())
    );
    assert!(
        !after_excision
            .tx_data(IndexTransaction::T(inserted.basis_t))
            .unwrap()
            .unwrap()
            .iter()
            .any(|datom| datom.entity == item && datom.attribute == COUNT)
    );

    // A separately restarted authority appends to the new generation only.
    let writer = common::start_service(&postgres, &id);
    let newer = write(
        &writer,
        "new-generation",
        EntityRef::Temp("new".into()),
        9,
        3_000,
    );
    recovered.sync_to(newer.basis_t, TIMEOUT).unwrap();
    assert_eq!(
        transactions(&after_excision, None, None).len() as u64,
        excision.basis_t
    );
    assert_eq!(
        transactions(&recovered.log(), None, None).len() as u64,
        newer.basis_t
    );
    assert_eq!(original.basis_t(), inserted.basis_t);
    assert_eq!(recovered.load_stats().compatibility_materializations, 0);
    writer.shutdown();
}

fn with_parameter(connection: &str, key: &str, value: &str) -> String {
    assert!(
        value
            .chars()
            .all(|character| character.is_ascii_alphanumeric() || character == '_')
    );
    if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
        format!(
            "{connection}{}{key}={value}",
            if connection.contains('?') { "&" } else { "?" }
        )
    } else {
        format!("{connection} {key}='{value}'")
    }
}

struct Roles {
    client: Client,
    writer: String,
    peer: String,
}
impl Drop for Roles {
    fn drop(&mut self) {
        let _ = self.client.batch_execute(&format!(
            "DROP OWNED BY \"{}\"; DROP OWNED BY \"{}\"; DROP ROLE \"{}\"; DROP ROLE \"{}\"",
            self.writer, self.peer, self.writer, self.peer
        ));
    }
}

#[test]
fn peer_role_reads_log_without_writer_privileges_or_head_lock() {
    let Some((_scope, postgres, id)) = setup(false) else {
        return;
    };
    let writer_role = unique("native_log_writer");
    let peer_role = unique("native_log_peer");
    let password = unique("native_log_password");
    let mut client = Client::connect(&postgres, NoTls).unwrap();
    client
        .batch_execute(&format!(
            "CREATE ROLE \"{writer_role}\" LOGIN PASSWORD '{password}'; CREATE ROLE \"{peer_role}\" LOGIN PASSWORD '{password}'"
        ))
        .unwrap();
    let mut roles = Roles {
        client,
        writer: writer_role.clone(),
        peer: peer_role.clone(),
    };
    atomic_core::storage::PgBlockStore::connect(&atomic_core::PostgresConnectionConfig::plaintext(
        &postgres,
    ))
    .unwrap()
    .grant_runtime_privileges(&writer_role, &peer_role)
    .unwrap();
    let peer_connection = with_parameter(
        &with_parameter(&postgres, "user", &peer_role),
        "password",
        &password,
    );
    let mut restricted = Client::connect(&peer_connection, NoTls).unwrap();
    let denied = restricted
        .execute("UPDATE atomic_objects SET payload = payload", &[])
        .unwrap_err();
    assert_eq!(denied.code().map(|code| code.code()), Some("42501"));
    let writer = common::start_service(&postgres, &id);
    let committed = write(
        &writer,
        "read-role",
        EntityRef::Temp("item".into()),
        11,
        1_000,
    );
    let peer = Peer::connect(&peer_connection, &id, 4).unwrap();
    let captured = peer.log();
    let mut transaction = roles.client.transaction().unwrap();
    transaction
        .query_one(
            "SELECT revision FROM atomic_refs WHERE key = $1 FOR UPDATE",
            &[&atomic_core::storage::BlockDatabase::resolve(
                &atomic_core::PostgresConnectionConfig::plaintext(&postgres),
                &id,
            )
            .unwrap()
            .reference_key()],
        )
        .unwrap();
    let (sender, receiver) = mpsc::channel();
    let basis = committed.basis_t;
    let worker = std::thread::spawn(move || {
        sender
            .send(captured.tx_data(IndexTransaction::T(basis)))
            .unwrap()
    });
    let result = receiver.recv_timeout(Duration::from_secs(15));
    transaction.rollback().unwrap();
    worker.join().unwrap();
    assert_eq!(
        result
            .expect("native log read waited on writer/head lock")
            .unwrap(),
        Some(committed.tx_data)
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
    drop(peer);
    drop(restricted);
    writer.shutdown();
    drop(roles);
}

#[test]
fn lazy_log_authenticates_each_payload_and_fuses_on_corruption() {
    let Some((_scope, postgres, id)) = setup(false) else {
        return;
    };
    let writer = common::start_service(&postgres, &id);
    let first = write(&writer, "first", EntityRef::Temp("item".into()), 1, 1_000);
    let second = write(
        &writer,
        "second",
        EntityRef::Id(first.tempids["item"]),
        2,
        2_000,
    );
    writer.shutdown();
    let peer = Peer::connect(&postgres, &id, 4).unwrap();
    let log = peer.log();
    let mut cursor = log
        .tx_range(Some(TimePoint::T(first.basis_t)), None)
        .unwrap();
    assert_eq!(cursor.next().unwrap().unwrap().data, first.tx_data);
    let mut client = Client::connect(&postgres, NoTls).unwrap();
    let config = atomic_core::PostgresConnectionConfig::plaintext(&postgres);
    let mut store = atomic_core::storage::PgBlockStore::connect(&config).unwrap();
    let database = atomic_core::storage::BlockDatabase::resolve(&config, &id).unwrap();
    let root_id = store
        .read_ref(&database.reference_key())
        .unwrap()
        .unwrap()
        .value
        .unwrap()
        .try_into()
        .unwrap();
    let root = atomic_core::storage::root::DatabaseRoot::decode(
        &root_id,
        &store.get(root_id).unwrap().unwrap(),
    )
    .unwrap();
    let root_log = atomic_core::storage::log::LogRoot::open(&mut store, root.log.unwrap()).unwrap();
    let hash = root_log
        .read_record(&mut store, second.basis_t)
        .unwrap()
        .unwrap()
        .id
        .to_vec();
    let payload: Vec<u8> = client
        .query_one("SELECT payload FROM atomic_objects WHERE id=$1", &[&hash])
        .unwrap()
        .get(0);
    let mut changed = payload.clone();
    let last = changed.last_mut().unwrap();
    *last ^= 1;
    // The selected content belongs to this unique database lineage. Restore
    // it before assertions, even if the read unexpectedly succeeds.
    let affected = client
        .execute(
            "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
            &[&hash, &changed],
        )
        .unwrap();
    assert_eq!(affected, 1);
    let result = cursor.next();
    let exhausted = cursor.next();
    let restored = client
        .execute(
            "UPDATE atomic_objects SET payload=$2 WHERE id=$1",
            &[&hash, &payload],
        )
        .unwrap();
    assert_eq!(restored, 1);
    assert_eq!(result.unwrap().unwrap_err().code, "storage/object-corrupt");
    assert!(exhausted.is_none());
    assert_eq!(cursor.stats().transactions_read, 1);
    assert_eq!(cursor.stats().range_reads, 2);
    assert_eq!(
        log.tx_data(IndexTransaction::T(second.basis_t)).unwrap(),
        Some(second.tx_data)
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
