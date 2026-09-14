#![cfg(unix)]

mod common;
use atomic_core::*;
use bigdecimal::BigDecimal;
use common::product_support::{Fixture, Server, cli};
use num_bigint::BigInt;
use std::os::unix::fs::PermissionsExt;
use std::time::{Duration, Instant};

const DECIMAL: u32 = 1_000;
const INTEGER: u32 = 1_001;
const SCALE: u32 = 1_002;
const COMPACT: u32 = 1_003;
const WAIT: Duration = Duration::from_secs(30);

fn decimal(text: &str) -> Value {
    Value::BigDec(text.parse().unwrap())
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn aggregate(attribute: u32, function: Aggregate) -> Query {
    Query::new(
        FindSpec::Scalar(FindElement::Aggregate {
            function,
            variable: "value".into(),
        }),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::var("entity"),
            Term::Constant(Value::Ref(u64::from(attribute))),
            Term::var("value"),
        )))],
    )
}

fn scalar(outcome: QueryOutcome) -> Value {
    let QueryResult::Scalar(Some(QueryValue::Scalar(ref value))) = outcome.result else {
        panic!("expected scalar, got {:?}", outcome.result)
    };
    value.clone()
}

fn scale(db: &DatabaseValue, entity: u64) -> i64 {
    let values = db.values(entity, SCALE).unwrap();
    let [Value::BigDec(value)] = values.as_slice() else {
        panic!("decimal expected")
    };
    value.fractional_digit_count()
}

fn encoded_values(datoms: &[Datom]) -> Vec<u8> {
    // Datom equality intentionally uses logical numeric equality. Compare
    // canonical value bytes too, so representation/scale drift cannot hide.
    encode_program_output(&ProgramOutput::Query(
        datoms
            .iter()
            .map(|datom| vec![datom.value.clone()])
            .collect(),
    ))
    .unwrap()
}

fn check_numeric_queries(db: &DatabaseValue) {
    for (attribute, expected) in [
        (DECIMAL, decimal("4.00")),
        (
            INTEGER,
            Value::BigInt("18446744073709551617".parse().unwrap()),
        ),
    ] {
        let prepared = PreparedQuery::new(&aggregate(attribute, Aggregate::Sum)).unwrap();
        let result = scalar(
            prepared
                .execute(
                    &[QueryDataSource::database("$", db.clone())],
                    &[],
                    &QueryControl::default(),
                )
                .unwrap(),
        );
        assert_eq!(result, expected);
        assert_eq!(
            result.type_name(),
            expected.type_name(),
            "exact sum result type"
        );
    }
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable("sum".into())),
        vec![Clause::Function {
            function: Function::Add,
            source: "$".into(),
            args: vec![
                Term::Constant(decimal("1.25")),
                Term::Constant(decimal("2.75")),
            ],
            binding: Binding::Scalar("sum".into()),
        }],
    );
    let result = scalar(db.query(&query, &[], &QueryControl::default()).unwrap());
    assert!(matches!(result, Value::BigDec(_)));
    assert_eq!(result, decimal("4.00"));
    assert_eq!(
        scalar(
            db.query(
                &aggregate(DECIMAL, Aggregate::Average),
                &[],
                &QueryControl::default()
            )
            .unwrap()
        ),
        Value::Double(2.0),
    );
}

#[test]
fn exact_numeric_queries_scale_sensitive_history_and_receipts_survive_cli_restart() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED numeric PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["install", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["install"]);
    }
    const DATABASE: &str = "numeric_repairs";
    cli(&fixture.admin_url, &["create", "--database", DATABASE]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("writer.sock");
    let mut server = Server::start(&fixture.writer_url, DATABASE, &endpoint);
    let peer = Connection::connect(&fixture.peer_url, DATABASE, 128).unwrap();
    let attributes = [
        (DECIMAL, "decimal", ValueType::BigDec),
        (INTEGER, "integer", ValueType::BigInt),
        (SCALE, "scale", ValueType::BigDec),
        (COMPACT, "compact", ValueType::BigDec),
    ]
    .into_iter()
    .map(|(id, name, value_type)| {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("numeric", name),
            value_type,
            Cardinality::One,
        );
        attribute.indexed = true;
        TxOp::InstallAttribute(attribute)
    })
    .collect();
    peer.transact_socket(
        &endpoint,
        TransactionRequest::new("schema", attributes),
        WAIT,
    )
    .unwrap()
    .report
    .unwrap();
    let mut seed_ops = Vec::new();
    for (name, dec, integer, exponent) in [
        ("left", "1.25", "9223372036854775808", 100_000),
        ("right", "2.75", "9223372036854775809", -100_000),
    ] {
        let entity = EntityRef::Temp(name.into());
        seed_ops.extend([
            add(entity.clone(), DECIMAL, decimal(dec)),
            add(
                entity.clone(),
                INTEGER,
                Value::BigInt(integer.parse().unwrap()),
            ),
            add(
                entity,
                COMPACT,
                Value::BigDec(BigDecimal::new(BigInt::from(1), exponent)),
            ),
        ]);
    }
    seed_ops.push(add(EntityRef::Temp("scaled".into()), SCALE, decimal("1.0")));
    let request = TransactionRequest::new("numeric-seed", seed_ops);
    let started = Instant::now();
    let seed = peer
        .transact_socket(&endpoint, request.clone(), WAIT)
        .unwrap()
        .report
        .unwrap();
    check_numeric_queries(&seed.db_after);
    let entity = seed.tempids["scaled"];
    let before = seed.db_after.clone();
    let changed = peer
        .transact_socket(
            &endpoint,
            TransactionRequest::new(
                "scale-only",
                vec![add(EntityRef::Id(entity), SCALE, decimal("1.00"))],
            ),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    assert_eq!(scale(&before, entity), 1);
    assert_eq!(scale(&changed.db_after, entity), 2);
    let scale_changes: Vec<_> = changed
        .tx_data
        .iter()
        .filter(|d| d.attribute == SCALE)
        .collect();
    assert_eq!(scale_changes.len(), 2);
    assert_eq!(scale_changes.iter().filter(|d| d.added).count(), 1);
    let history = changed
        .db_after
        .clone()
        .history()
        .datoms(IndexOrder::Eavt)
        .unwrap();
    // Native covering roots must compare compact decimal exponents without
    // changing their encoded coefficient/scale representation.
    peer.sync_index(changed.basis_t, WAIT).unwrap();
    server.stop();
    drop(peer);
    let reopened = Connection::connect(&fixture.peer_url, DATABASE, 128).unwrap();
    let after = reopened.db();
    check_numeric_queries(&after);
    assert_eq!(scale(&after, entity), 2);
    let reopened_history = after.clone().history().datoms(IndexOrder::Eavt).unwrap();
    assert_eq!(reopened_history, history);
    assert_eq!(encoded_values(&reopened_history), encoded_values(&history));
    let mut server = Server::start(&fixture.writer_url, DATABASE, &endpoint);
    let replay = reopened.transact_socket(&endpoint, request, WAIT).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, seed.tx_hash);
    let report = replay.report.unwrap();
    assert_eq!(report.basis_t, seed.basis_t);
    assert_eq!(report.tempids, seed.tempids);
    assert_eq!(report.tx_data, seed.tx_data);
    assert_eq!(
        encoded_values(&report.tx_data),
        encoded_values(&seed.tx_data)
    );
    assert_eq!(scale(&report.db_after, entity), 1);
    assert_eq!(scale(&before, entity), 1);
    check_numeric_queries(&report.db_after);
    server.stop();
    eprintln!(
        "NUMERIC_PG_OK exact_sum=true arithmetic=true prepared=true stored_scale=true compact_scale=100000 restart_retry=exact elapsed_ms={}",
        started.elapsed().as_millis()
    );
}
