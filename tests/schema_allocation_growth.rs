//! Public allocation witnesses. The million-identity boundary check is opt-in:
//! `cargo test --release --test schema_allocation_growth -- --ignored --nocapture`.
//! It asserts the desired late-install behavior; before the allocator repair
//! its failure is evidence of the gap, not a passing expected-error regression.

mod common;

use atomic_core::{
    Connection, DB_TX_INSTANT, Database, EntityRef, IndexOrder, PostgresMigrator, PostgresStore,
    Schema, TransactionRequest, TransactionService, TxOp, USER_PARTITION, View, eid_to_eidx,
    eid_to_part,
};
use postgres::fallible_iterator::FallibleIterator;
use sha2::{Digest, Sha256};
use std::time::{Duration, Instant};

const PARTITION_BOUNDARY: u64 = 524_288;
const LAST_NATIVE_SCHEMA_ID: u64 = 1_048_576;
const BATCH: usize = 16_384;

fn retractions(count: usize) -> Vec<TxOp> {
    (0..count)
        .map(|number| TxOp::RetractEntity(EntityRef::Temp(format!("unused-{number:06}"))))
        .collect()
}

#[test]
fn retract_only_tempids_issue_real_frontier_without_live_user_facts() {
    let before = Database::bootstrap().unwrap();
    let frontier = before.eidx_frontier();
    let operations = retractions(128);
    let start = Instant::now();
    let eager = before.with(&operations, 10).unwrap();
    let exact = before.database_value().with(&operations, 10).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids.len(), 128);
    assert_eq!(eager.db_after.eidx_frontier(), frontier + 128);
    assert_eq!(exact.db_after.eidx_frontier(), frontier + 128);
    for (offset, entity) in eager.tempids.values().enumerate() {
        assert_eq!(eid_to_part(*entity).unwrap(), USER_PARTITION);
        assert_eq!(eid_to_eidx(*entity).unwrap(), frontier + offset as u64);
    }
    assert!(
        eager
            .tx_data
            .iter()
            .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
    );
    assert!(
        eager
            .db_after
            .datoms(View::Current, IndexOrder::Eavt)
            .iter()
            .all(|datom| eid_to_part(datom.entity).unwrap() != USER_PARTITION)
    );
    assert_eq!(before.eidx_frontier(), frontier);
    eprintln!(
        "retract-only public eager+exact:128 real tempids, frontier {frontier}->{}, zero live user facts; complete {:?}",
        eager.db_after.eidx_frontier(),
        start.elapsed()
    );
}

#[test]
fn reserved_and_ordinary_domains_do_not_spend_each_others_indices() {
    let before = Database::bootstrap().unwrap();
    let text = r#"[{:db/id "a-schema" :db/ident :growth/field :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
                   {:db/id "z-ordinary" :db/ident :growth/entity}]"#;
    let eager = before.with_edn(text, 1).unwrap();
    let exact = before.database_value().with_edn(text, 1).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids["a-schema"], 1_000);
    assert_eq!(eid_to_eidx(eager.tempids["z-ordinary"]).unwrap(), 1_000);
    assert_eq!(
        eid_to_part(eager.tempids["z-ordinary"]).unwrap(),
        USER_PARTITION
    );
    assert_eq!(eager.db_after.eidx_frontier(), 1_001);
    let later = eager.db_after.with_edn(r#"[{:db/id "schema" :db/ident :growth/later :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#, 2).unwrap();
    assert_eq!(later.tempids["schema"], 1_001);
    eager.db_after.validate_invariants().unwrap();
    later.db_after.validate_invariants().unwrap();
}

#[test]
fn explicit_reserved_claims_precede_allocation_and_survive_no_op_successors() {
    let before = Database::bootstrap()
        .unwrap()
        .with(&retractions(32), 1)
        .unwrap()
        .db_after;
    let text = r#"[{:db/id "schema" :db/ident :growth/claimed :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
                   [:db/retractEntity 1010]]"#;
    let eager = before.with_edn(text, 2).unwrap();
    let exact = before.database_value().with_edn(text, 2).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tempids["schema"], 1_011);
    assert!(eager.tx_data.iter().all(|datom| datom.entity != 1_010));
    let no_op = eager
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(1_020))], 3)
        .unwrap();
    let exact_no_op = exact
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(1_020))], 3)
        .unwrap();
    assert!(
        no_op
            .tx_data
            .iter()
            .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
    );
    let next_text = r#"[{:db/id "next" :db/ident :growth/next :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#;
    let next = no_op.db_after.with_edn(next_text, 4).unwrap();
    let exact_next = exact_no_op.db_after.with_edn(next_text, 4).unwrap();
    assert_eq!(next.tempids, exact_next.tempids);
    assert_eq!(next.tempids["next"], 1_021);
    assert_eq!(before.eidx_frontier(), 1_032);
    assert_eq!(next.db_after.eidx_frontier(), 1_032);
}

#[test]
fn stored_ref_and_tuple_ref_operands_reserve_ids_but_numeric_slots_do_not() {
    let before = Database::bootstrap().unwrap().with_edn(r#"[
        {:db/id "ref" :db/ident :growth/ref :db/valueType :db.type/ref :db/cardinality :db.cardinality/one}
        {:db/id "tuple" :db/ident :growth/tuple :db/valueType :db.type/tuple :db/tupleTypes [:db.type/ref :db.type/long] :db/cardinality :db.cardinality/one}]
        "#, 1).unwrap().db_after.with(&retractions(32), 2).unwrap().db_after;
    let text = r#"[{:db/id "entity" :growth/ref 1010 :growth/tuple [1015 1000000]}
                   {:db/id "schema" :db/ident :growth/after-refs :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#;
    let eager = before.with_edn(text, 3).unwrap();
    let exact = before.database_value().with_edn(text, 3).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids["schema"], 1_016);
    assert_eq!(
        eid_to_eidx(eager.tempids["entity"]).unwrap(),
        before.eidx_frontier()
    );
}

fn grow_to(mut database: Database, frontier: u64) -> Database {
    let start = Instant::now();
    let before = database.eidx_frontier();
    let mut batches = 0;
    while database.eidx_frontier() < frontier {
        let count = usize::try_from(frontier - database.eidx_frontier())
            .unwrap()
            .min(BATCH);
        let report = database
            .with(&retractions(count), database.basis_t() as i64 + 10)
            .unwrap();
        assert_eq!(report.tempids.len(), count);
        assert_eq!(
            report.db_after.eidx_frontier(),
            database.eidx_frontier() + count as u64
        );
        assert!(
            report
                .tx_data
                .iter()
                .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
        );
        database = report.db_after;
        batches += 1;
        if batches % 8 == 0 {
            eprintln!(
                "allocation growth frontier={} batches={batches} elapsed={:?}",
                database.eidx_frontier(),
                start.elapsed()
            );
        }
    }
    assert_eq!(database.eidx_frontier(), frontier);
    assert!(
        database
            .datoms(View::Current, IndexOrder::Eavt)
            .iter()
            .all(|datom| eid_to_part(datom.entity).unwrap() != USER_PARTITION)
    );
    eprintln!(
        "public allocation growth {before}->{frontier}: {batches} batches of at most{BATCH}; complete {:?}",
        start.elapsed()
    );
    database
}

#[test]
#[ignore = "explicit million-allocation boundary witness; run optimized"]
fn late_automatic_partition_and_schema_installation_survive_ordinary_growth() {
    let started = Instant::now();
    let database = grow_to(Database::bootstrap().unwrap(), PARTITION_BOUNDARY);
    let partition = r#"[{:db/id "late-partition" :db/ident :growth.part/late :db.install/_partition :db.part/db}]"#;
    let partition_eager = database.with_edn(partition, database.basis_t() as i64 + 10);
    let partition_exact = database
        .database_value()
        .with_edn(partition, database.basis_t() as i64 + 10);
    eprintln!(
        "named partition at frontier{PARTITION_BOUNDARY}: eager={:?}; exact={:?}",
        partition_eager
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code)),
        partition_exact
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code))
    );
    assert_eq!(partition_eager.is_ok(), partition_exact.is_ok());

    let database = grow_to(database, LAST_NATIVE_SCHEMA_ID);
    let schema = r#"[{:db/id "late-schema" :db/ident :growth/late :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#;
    let ceiling = database.with_edn(schema, database.basis_t() as i64 + 10);
    eprintln!(
        "schema at native inclusive ceiling{LAST_NATIVE_SCHEMA_ID}: {:?}",
        ceiling
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code))
    );
    assert!(
        ceiling.is_ok(),
        "a schema addition at this ordinary frontier must remain possible"
    );
    // This is a speculative branch, not a committed reservation. Do not let a
    // successful probe silently alter the growth fixture's reserved occupancy.
    drop(ceiling);
    let database = grow_to(database, LAST_NATIVE_SCHEMA_ID + 1);
    let schema_eager = database.with_edn(schema, database.basis_t() as i64 + 10);
    let schema_exact = database
        .database_value()
        .with_edn(schema, database.basis_t() as i64 + 10);
    eprintln!(
        "automatic schema at frontier{}: eager={:?}; exact={:?}",
        database.eidx_frontier(),
        schema_eager
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code)),
        schema_exact
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code))
    );
    assert_eq!(schema_eager.is_ok(), schema_exact.is_ok());
    eprintln!(
        "complete public boundary witness: {:?}; retained user facts=0; reserved occupancy remains bootstrap",
        started.elapsed()
    );
    assert!(
        partition_eager.is_ok() && schema_eager.is_ok(),
        "ordinary data issuance must not exhaust automatic reserved partition/schema allocation"
    );
}

fn commit_growth(
    writer: &TransactionService,
    current: &mut atomic_core::DatabaseValue,
    target: u64,
    next_batch: &mut usize,
) -> Option<atomic_core::ServiceTransactionReport> {
    let start = Instant::now();
    let before = current.eidx_frontier();
    let mut first = None;
    while current.eidx_frontier() < target {
        let count = usize::try_from(target - current.eidx_frontier())
            .unwrap()
            .min(BATCH);
        let request =
            TransactionRequest::new(format!("growth-{:04}", *next_batch), retractions(count));
        let report = writer
            .client()
            .transact(request, Duration::from_secs(30))
            .unwrap();
        assert_eq!(report.tempids.len(), count);
        assert_eq!(
            report.db_after.eidx_frontier(),
            current.eidx_frontier() + count as u64
        );
        assert!(
            report
                .tx_data
                .iter()
                .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
        );
        *current = report.db_after.clone();
        if first.is_none() {
            first = Some(report);
        }
        *next_batch += 1;
        if *next_batch % 8 == 0 {
            eprintln!(
                "PostgreSQL allocation frontier={} total_batches={} elapsed={:?}",
                current.eidx_frontier(),
                next_batch,
                start.elapsed()
            );
        }
    }
    eprintln!(
        "PostgreSQL complete growth {before}->{target}: {:?}",
        start.elapsed()
    );
    first
}

#[test]
#[ignore = "explicit PostgreSQL million-allocation and restart witness"]
fn postgres_late_allocation_growth_retains_frontier_and_receipts_across_restart() {
    let url = std::env::var("ATOMIC_POSTGRES_URL")
        .expect("configure the disposable PostgreSQL fixture explicitly");
    let fixture = common::PostgresFixture::new(&url, "allocation_growth");
    let fixture_connection = fixture.connection.clone();
    let fixture_schema = fixture.schema.clone();
    // Retain the explicitly requested baseline even if a later diagnostic
    // fails. Default runs still drop their scoped fixture on every exit path.
    let _cleanup = if std::env::var("ATOMIC_ALLOCATION_KEEP_FIXTURE").as_deref() == Ok("1") {
        eprintln!(
            "RETAINED allocation baseline schema={fixture_schema} database=growth; no reseeding on upgrade"
        );
        std::mem::forget(fixture);
        None
    } else {
        Some(fixture)
    };
    PostgresMigrator::connect(&fixture_connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&fixture_connection).unwrap();
    store.create_database("growth", Schema::new()).unwrap();
    drop(store);
    let started = Instant::now();
    let writer = common::start_service(&fixture_connection, "growth");
    let peer = Connection::connect(&fixture_connection, "growth", 8).unwrap();
    let mut current = peer.db();
    let mut batch = 0;
    let first = commit_growth(&writer, &mut current, PARTITION_BOUNDARY, &mut batch).unwrap();
    let partition = TransactionRequest::from_edn("late-partition", r#"[{:db/id "late-partition" :db/ident :growth.part/late :db.install/_partition :db.part/db}]"#).unwrap();
    let partition = writer.client().transact(partition, Duration::from_secs(30));
    eprintln!(
        "PostgreSQL late partition at524288: {:?}",
        partition
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code))
    );
    if let Ok(report) = &partition {
        current = report.db_after.clone();
    }
    commit_growth(&writer, &mut current, LAST_NATIVE_SCHEMA_ID + 1, &mut batch);
    let before_probe = current.clone();
    let schema = TransactionRequest::from_edn("late-schema", r#"[{:db/id "late-schema" :db/ident :growth/late :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#).unwrap();
    let schema = writer.client().transact(schema, Duration::from_secs(30));
    eprintln!(
        "PostgreSQL late schema at1048577: {:?}",
        schema
            .as_ref()
            .map(|report| &report.tempids)
            .map_err(|error| (&error.category, &error.code))
    );
    if let Ok(report) = &schema {
        current = report.db_after.clone();
    }
    assert!(
        before_probe
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .all(|datom| eid_to_part(datom.entity).unwrap() != USER_PARTITION)
    );
    let expected_frontier = current.eidx_frontier();
    let expected_basis = current.basis_t();
    writer.shutdown();
    drop(peer);
    drop(current);
    let restarted = common::start_service(&fixture_connection, "growth");
    let replay = restarted
        .client()
        .transact(
            TransactionRequest::new("growth-0000", retractions(BATCH)),
            Duration::from_secs(30),
        )
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tempids, first.tempids);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(
        replay.db_after.eidx_frontier(),
        first.db_after.eidx_frontier()
    );
    let peer = Connection::connect(&fixture_connection, "growth", 8).unwrap();
    assert_eq!(peer.db().eidx_frontier(), expected_frontier);
    assert_eq!(peer.db().basis_t(), expected_basis);
    restarted.shutdown();
    let key = peer.db().snapshot_key().unwrap();
    let mut inspect = postgres::Client::connect(&fixture_connection, postgres::NoTls).unwrap();
    let size = inspect.query_one("SELECT count(*), COALESCE(sum(octet_length(c.payload)), 0)::bigint FROM atomic_generation_transactions t JOIN atomic_transaction_contents c USING(content_hash) WHERE t.database_id = 'growth'", &[]).unwrap();
    let first_row = inspect.query_one("SELECT r.request_digest, r.tx_hash, t.state_hash FROM atomic_generation_requests r JOIN atomic_generation_transactions t USING(database_id, generation, basis_t) WHERE r.database_id = 'growth' AND r.basis_t = $1", &[&(first.basis_t as i64)]).unwrap();
    let tempid_size = inspect.query_one("SELECT count(*), pg_total_relation_size('atomic_generation_request_tempids') FROM atomic_generation_request_tempids WHERE database_id = 'growth'", &[]).unwrap();
    let hex = |bytes: Vec<u8>| {
        bytes
            .iter()
            .map(|byte| format!("{byte:02x}"))
            .collect::<String>()
    };
    eprintln!(
        "PostgreSQL baseline schema={} database=growth head_basis={expected_basis} frontier={expected_frontier}; canonical_transactions={} canonical_transaction_payload_bytes={}; first_request=growth-0000 request_digest={} tx_hash={} state_hash={} first_allocated={} last_allocated={} count={}; complete {:?}",
        fixture_schema,
        size.get::<_, i64>(0),
        size.get::<_, i64>(1),
        hex(first_row.get(0)),
        hex(first_row.get(1)),
        hex(first_row.get(2)),
        first.tempids.values().next().unwrap(),
        first.tempids.values().next_back().unwrap(),
        first.tempids.len(),
        started.elapsed()
    );
    eprintln!(
        "native snapshot={key:?}; tempid_receipt_rows={} tempid_table_and_indexes_bytes={}",
        tempid_size.get::<_, i64>(0),
        tempid_size.get::<_, i64>(1)
    );
    assert!(
        partition.is_ok() && schema.is_ok(),
        "late automatic system allocation must remain possible after durable ordinary growth"
    );
}

fn old_generation_fingerprint(client: &mut postgres::Client) -> (String, i64, i64) {
    let mut digest = Sha256::new();
    let mut transactions = 0i64;
    // Include every original canonical byte and immutable transaction/request
    // field, not merely a count or a newly reconstructed receipt.
    for row in client.query("SELECT row_to_json(t)::text, row_to_json(r)::text, c.payload, c.envelope_version FROM atomic_generation_transactions t JOIN atomic_transaction_contents c USING(content_hash) JOIN atomic_generation_requests r ON r.database_id=t.database_id AND r.generation=t.generation AND r.basis_t=t.basis_t WHERE t.database_id='growth' AND t.generation=1 AND t.basis_t<=65 ORDER BY t.basis_t", &[]).unwrap() {
        for column in 0..2 {
            let value: String = row.get(column);
            digest.update((value.len() as u64).to_be_bytes());
            digest.update(value.as_bytes());
        }
        let payload: Vec<u8> = row.get(2);
        digest.update((payload.len() as u64).to_be_bytes());
        digest.update(payload);
        digest.update(row.get::<_, i16>(3).to_be_bytes());
        transactions += 1;
    }
    let mut rows = client.query_raw("SELECT t.basis_t,n.tempid_name,n.entity_id FROM atomic_generation_request_tempids n JOIN atomic_generation_requests t USING(database_id,generation,request_key_hash) WHERE n.database_id='growth' AND n.generation=1 AND t.basis_t<=65 ORDER BY t.basis_t,n.tempid_name", std::iter::empty::<&str>()).unwrap();
    let mut names = 0i64;
    while let Some(row) = rows.next().unwrap() {
        digest.update(row.get::<_, i64>(0).to_be_bytes());
        let name: String = row.get(1);
        digest.update((name.len() as u64).to_be_bytes());
        digest.update(name.as_bytes());
        digest.update(row.get::<_, i64>(2).to_be_bytes());
        names += 1;
    }
    (format!("{:x}", digest.finalize()), transactions, names)
}

/// Deliberately separate from seeding: this can only use an explicitly named
/// retained old-binary fixture. It never creates, replaces, or removes it.
#[test]
#[ignore = "explicit retained old-binary schema; migrates and appends two repair transactions, never reseeds"]
fn postgres_upgrade_retained_legacy_growth_without_rewriting_original_receipts() {
    let url = std::env::var("ATOMIC_POSTGRES_URL").expect("configure disposable PostgreSQL");
    let schema = std::env::var("ATOMIC_ALLOCATION_UPGRADE_SCHEMA")
        .expect("name the retained old-binary fixture explicitly");
    assert!(
        schema.starts_with("allocation_growth_")
            && schema
                .bytes()
                .all(|byte| byte.is_ascii_lowercase() || byte.is_ascii_digit() || byte == b'_')
    );
    let connection = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
        format!(
            "{url}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if url.contains('?') { "&" } else { "?" }
        )
    } else {
        format!("{url} options='-csearch_path={schema},pg_catalog'")
    };
    let started = Instant::now();
    let mut inspect = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    let original = old_generation_fingerprint(&mut inspect);
    assert_eq!(original.1, 65);
    assert_eq!(original.2, 1_047_577);
    let endpoint: i64 = inspect.query_one("SELECT eidx_frontier FROM atomic_generation_transactions WHERE database_id='growth' AND generation=1 AND basis_t=65", &[]).unwrap().get(0);
    assert_eq!(endpoint, 1_048_577);
    let first_row = inspect.query_one("SELECT encode(r.request_digest,'hex'),encode(r.tx_hash,'hex'),encode(t.state_hash,'hex') FROM atomic_generation_requests r JOIN atomic_generation_transactions t USING(database_id,generation,basis_t) WHERE r.database_id='growth' AND r.generation=1 AND r.basis_t=1", &[]).unwrap();
    assert_eq!(
        first_row.get::<_, String>(0),
        "7c078db2a8df8e445e03f8a3fa629e1ba26d29c66e8d14800a080856d04a5b79"
    );
    assert_eq!(
        first_row.get::<_, String>(1),
        "cce0b5eb6afbdf72b693da3b5a9fd911c49a4324da6ff29da331d647184f4ed9"
    );
    assert_eq!(
        first_row.get::<_, String>(2),
        "7f686514ee5869379b9b372b45f5ce1533d71fd2f7cc25339f4274e7aff461ec"
    );
    eprintln!(
        "before retained upgrade schema={schema} original65 transactions+1047577 namedallocations SHA256={} complete {:?}",
        original.0,
        started.elapsed()
    );
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let writer = common::start_service(&connection, "growth");
    let first = writer
        .client()
        .transact(
            TransactionRequest::new("growth-0000", retractions(BATCH)),
            Duration::from_secs(30),
        )
        .unwrap();
    assert!(first.replayed);
    assert_eq!(first.basis_t, 1);
    assert_eq!(first.tempids.len(), BATCH);
    assert_eq!(*first.tempids.values().next().unwrap(), 17_592_186_045_416);
    assert_eq!(
        *first.tempids.values().next_back().unwrap(),
        17_592_186_061_799
    );
    let partition = TransactionRequest::from_edn("late-partition", r#"[{:db/id "late-partition" :db/ident :growth.part/late :db.install/_partition :db.part/db}]"#).unwrap();
    let attribute = TransactionRequest::from_edn("late-schema", r#"[{:db/id "late-schema" :db/ident :growth/late :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#).unwrap();
    let part = writer
        .client()
        .transact(partition.clone(), Duration::from_secs(30))
        .unwrap();
    let attr = writer
        .client()
        .transact(attribute.clone(), Duration::from_secs(30))
        .unwrap();
    assert_eq!(part.tempids["late-partition"], 1_000);
    assert_eq!(attr.tempids["late-schema"], 1_001);
    assert_eq!(part.basis_t, 66);
    assert_eq!(attr.basis_t, 67);
    assert_eq!(attr.db_after.eidx_frontier(), 1_048_577);
    writer.shutdown();
    let reopened = Connection::connect(&connection, "growth", 8).unwrap();
    assert_eq!(reopened.db().basis_t(), 67);
    assert_eq!(reopened.db().eidx_frontier(), 1_048_577);
    let restarted = common::start_service(&connection, "growth");
    for (request, expected) in [
        (
            TransactionRequest::new("growth-0000", retractions(BATCH)),
            first,
        ),
        (partition, part),
        (attribute, attr),
    ] {
        let replay = restarted
            .client()
            .transact(request, Duration::from_secs(30))
            .unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, expected.tx_hash);
        assert_eq!(replay.tempids, expected.tempids);
        assert_eq!(replay.basis_t, expected.basis_t);
    }
    restarted.shutdown();
    let after = old_generation_fingerprint(&mut inspect);
    assert_eq!(original, after);
    eprintln!(
        "after retained upgrade schema={schema} original SHA256={} unchanged; partition1000/schema1001, head67/frontier1048577, old+new exactretries afterrestart; complete {:?}",
        after.0,
        started.elapsed()
    );
}
