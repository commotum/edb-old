//! URI expected results come from datomic.common/compare-ex delegating same-class
//! values to Comparable, and java.net.URI.equals/compareTo/hashCode:
//! https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/URI.html
//! The authority-kind ordering test records the explicit native total-order
//! adaptation; these tests do not use the implementation as their oracle.
mod common;

use atomic_core::edn::{read_edn, write_edn};
use atomic_core::edn_value::{edn_to_value, value_to_edn};
use atomic_core::storage::{
    BlockDatabase, BlockReader, BlockTransactor, BlockWriterOptions, PgBlockStore,
};
use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, Datom, EntityRef, FindElement, FindSpec,
    InputSpec, Keyword, PostgresConnectionConfig, Query, QueryControl, QueryEngine, QueryInput,
    QueryResult, QueryValue, Schema, ServiceTransactionReport, TransactionRequest, TxOp, Unique,
    Value, ValueType, canonical_datom_bytes, request_digest, submission_request_digest, t_to_tx,
};
use std::cmp::Ordering;

const URI_KEY: u32 = 1000;
const SCORE: u32 = 1001;
const ORIGINAL: &str = "HTTP://EXAMPLE.COM/a%2fb";
const ALIAS: &str = "http://example.com/a%2Fb";

fn uri(text: &str) -> Value {
    Value::Uri(text.into())
}

fn spelling(value: &Value) -> &str {
    let Value::Uri(text) = value else {
        panic!("expected a URI, got {value:?}")
    };
    text
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            URI_KEY,
            Keyword::new("item", "uri"),
            ValueType::Uri,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        Attribute::new(
            SCORE,
            Keyword::new("item", "score"),
            ValueType::Long,
            Cardinality::One,
        ),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}

fn original_request() -> TransactionRequest {
    TransactionRequest::new(
        "uri-original",
        vec![
            add(EntityRef::Temp("first".into()), URI_KEY, uri(ORIGINAL)),
            add(EntityRef::Temp("first".into()), SCORE, Value::Long(1)),
        ],
    )
    .with_tx_instant(1000)
}

fn alias_request(text: &str) -> TransactionRequest {
    TransactionRequest::from_edn(
        "uri-upsert",
        &format!(r#"[{{:db/id "upsert" :item/uri #atomic/uri "{text}" :item/score 2}}]"#),
    )
    .unwrap()
    .with_tx_instant(2000)
}

fn assert_key_spelling(db: &DatabaseValue, entity: u64, expected: &str) {
    let values = db.values(entity, URI_KEY).unwrap();
    assert_eq!(values.len(), 1);
    // Value equality deliberately cannot detect an unwanted spelling rewrite.
    assert_eq!(spelling(&values[0]), expected);
}

#[test]
fn uri_logical_and_stored_equality_fold_only_the_documented_components() {
    for (left, right) in [
        ("HTTP://example.com/a", "http://example.com/a"),
        ("http://EXAMPLE.COM/a", "http://example.com/a"),
        ("http://example.com/a%2fb", "http://example.com/a%2Fb"),
        (
            "http://u%3aser@example.com/p",
            "http://u%3Aser@example.com/p",
        ),
        (
            "http://example.com/p?q=%aa#f%bc",
            "http://example.com/p?q=%AA#f%BC",
        ),
        (
            "MAILTO:User%2fTag@example.com",
            "mailto:User%2FTag@example.com",
        ),
        ("../a%2fb", "../a%2Fb"),
        ("http://example.com:080/a", "http://example.com:80/a"),
        (ORIGINAL, ALIAS),
    ] {
        let (left, right) = (uri(left), uri(right));
        assert_eq!(
            left.index_cmp(&right),
            Ordering::Equal,
            "{left:?} / {right:?}"
        );
        assert_eq!(right.index_cmp(&left), Ordering::Equal);
        assert_eq!(left, right);
        assert!(left.stored_eq(&right));
        assert_eq!(left.stored_cmp(&right), Ordering::Equal);
        assert_eq!(
            Value::Tuple(vec![Some(left)]),
            Value::Tuple(vec![Some(right)])
        );
    }
}

#[test]
fn uri_equality_does_not_perform_url_normalization_or_component_case_folding() {
    for (left, right) in [
        ("http://example.com/a", "http://example.com:80/a"),
        ("https://example.com/a", "https://example.com:443/a"),
        ("http://example.com/a/../b", "http://example.com/b"),
        ("http://example.com/./a", "http://example.com/a"),
        ("http://example.com/%41", "http://example.com/A"),
        ("http://example.com/a%2fb", "http://example.com/a/b"),
        ("http://example.com/A", "http://example.com/a"),
        ("http://User@example.com/a", "http://user@example.com/a"),
        ("mailto:User@example.com", "mailto:user@example.com"),
        ("mailto:user@EXAMPLE.COM", "mailto:user@example.com"),
        ("http://example.com/a?Q=x", "http://example.com/a?q=x"),
        ("http://example.com/a#Part", "http://example.com/a#part"),
        ("http://example.com/a", "http://example.com/a?"),
        ("http://example.com/a", "http://example.com/a#"),
        ("http://example.com", "http://example.com/"),
    ] {
        let (left, right) = (uri(left), uri(right));
        assert_ne!(left, right, "{left:?} / {right:?}");
        assert!(!left.stored_eq(&right));
        assert_ne!(left.index_cmp(&right), Ordering::Equal);
        assert_eq!(left.index_cmp(&right), right.index_cmp(&left).reverse());
    }
}

#[test]
fn uri_order_compares_components_and_numeric_ports_not_original_strings() {
    for (less, greater) in [
        ("a:/p", "B:/p"),
        ("http://a.example/p", "HTTP://B.example/p"),
        ("http://example.com:9/p", "http://example.com:10/p"),
        ("http://example.com/p", "http://example.com:0/p"),
        ("http://a@z.example/p", "http://b@a.example/p"),
        ("http://z.example/p", "http://a@a.example/p"),
        ("x:/z", "x:a"),
        ("http://example.com/p", "http://example.com/p?"),
        ("http://example.com/p?q", "http://example.com/p?q#"),
        ("http://example.com/%2fa", "http://example.com/%2Fb"),
        ("mailto:A@example.com", "mailto:a@example.com"),
    ] {
        let (less, greater) = (uri(less), uri(greater));
        assert_eq!(
            less.index_cmp(&greater),
            Ordering::Less,
            "{less:?} / {greater:?}"
        );
        assert_eq!(greater.index_cmp(&less), Ordering::Greater);
    }
}

#[test]
fn native_authority_order_preserves_equality_substitution_and_transitivity() {
    // Java's raw mixed-authority fallback places registry Z_ between raw A/a,
    // despite server A == a. Native keys use None < Server < Registry instead.
    let values = [
        uri("http:/p"),
        uri("http://A/p"),
        uri("http://a/p"),
        uri("http://Z_/p"),
        uri("http://z_/p"),
    ];
    assert_eq!(values[1], values[2]);
    assert_eq!(values[0].index_cmp(&values[1]), Ordering::Less);
    assert_eq!(values[2].index_cmp(&values[3]), Ordering::Less);
    assert_eq!(values[3].index_cmp(&values[4]), Ordering::Less);
    for a in &values {
        for b in &values {
            assert_eq!(a.index_cmp(b), b.index_cmp(a).reverse());
            for c in &values {
                if a == b {
                    assert_eq!(a.index_cmp(c), b.index_cmp(c));
                    assert_eq!(c.index_cmp(a), c.index_cmp(b));
                }
                if a.index_cmp(b).is_le() && b.index_cmp(c).is_le() {
                    assert!(a.index_cmp(c).is_le());
                }
            }
        }
    }
}

#[test]
fn uri_aliases_match_through_hash_joins_and_collapse_query_duplicates() {
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("left".into()),
            FindElement::Variable("right".into()),
        ]),
        vec![],
    );
    query.inputs = vec![
        InputSpec::Relation(vec![Some("key".into()), Some("left".into())]),
        InputSpec::Relation(vec![Some("key".into()), Some("right".into())]),
    ];
    let pairs: Vec<_> = (0..32)
        .map(|i| match i % 4 {
            0 => (
                format!("HTTP://EXAMPLE.COM/{i}%2fa"),
                format!("http://example.com/{i}%2Fa"),
            ),
            1 => (
                format!("http://example.com:080/{i}"),
                format!("http://example.com:80/{i}"),
            ),
            2 => (
                format!("MAILTO:User{i}%2fTag@example.com"),
                format!("mailto:User{i}%2FTag@example.com"),
            ),
            _ => (
                format!("http://u%3aser@example.com/{i}?q=%aa#f%bc"),
                format!("http://u%3Aser@example.com/{i}?q=%AA#f%BC"),
            ),
        })
        .collect();
    let inputs = vec![
        QueryInput::Relation(
            pairs
                .iter()
                .enumerate()
                .map(|(i, (left, _))| vec![uri(left), Value::Long(i as i64)])
                .collect(),
        ),
        QueryInput::Relation(
            pairs
                .iter()
                .enumerate()
                .map(|(i, (_, right))| vec![uri(right), Value::Long(i as i64 + 100)])
                .collect(),
        ),
    ];
    let joined =
        QueryEngine::execute_sources(&query, &[], &inputs, &QueryControl::default()).unwrap();
    assert!(
        joined.stats.hash_join_build_rows > 0,
        "exercise logical URI hashing"
    );
    let QueryResult::Relation(rows) = joined.result else {
        panic!("expected relation")
    };
    let mut rows: Vec<_> = rows.into_iter().map(QueryValue::Tuple).collect();
    rows.sort_by(QueryValue::canonical_cmp);
    let expected: Vec<_> = (0..32)
        .map(|i| {
            QueryValue::Tuple(vec![
                QueryValue::Scalar(Value::Long(i)),
                QueryValue::Scalar(Value::Long(i + 100)),
            ])
        })
        .collect();
    assert_eq!(rows, expected);

    let mut distinct = Query::new(
        FindSpec::Collection(FindElement::Variable("uri".into())),
        vec![],
    );
    distinct.inputs = vec![InputSpec::Collection("uri".into())];
    let result = QueryEngine::execute_sources(
        &distinct,
        &[],
        &[QueryInput::Collection(vec![
            uri(ORIGINAL),
            uri(ALIAS),
            uri(ORIGINAL),
        ])],
        &QueryControl::default(),
    )
    .unwrap();
    let QueryResult::Collection(values) = result.result else {
        panic!("expected collection")
    };
    assert_eq!(values.len(), 1);
    assert_eq!(values[0], QueryValue::Scalar(uri(ORIGINAL)));
}

#[test]
fn exact_edn_canonical_datoms_and_request_digests_keep_uri_spelling() {
    for text in [ORIGINAL, ALIAS, "MAILTO:User%2fTag@example.com", "../a%2fb"] {
        let encoded = write_edn(&value_to_edn(&uri(text)).unwrap()).unwrap();
        assert_eq!(encoded, format!(r#"#atomic/uri "{text}""#));
        let decoded = edn_to_value(&read_edn(&encoded).unwrap()).unwrap();
        assert_eq!(spelling(&decoded), text);
    }
    let original = Datom {
        entity: 100,
        attribute: URI_KEY,
        value: uri(ORIGINAL),
        tx: t_to_tx(2).unwrap(),
        added: true,
    };
    let alias = Datom {
        value: uri(ALIAS),
        ..original.clone()
    };
    assert_ne!(
        canonical_datom_bytes(&original).unwrap(),
        canonical_datom_bytes(&alias).unwrap()
    );
    let original_ops = vec![add(EntityRef::Temp("same".into()), URI_KEY, uri(ORIGINAL))];
    let alias_ops = vec![add(EntityRef::Temp("same".into()), URI_KEY, uri(ALIAS))];
    assert_ne!(
        request_digest(&original_ops, 1000, 1).unwrap(),
        request_digest(&alias_ops, 1000, 1).unwrap()
    );
    let original = alias_request(ORIGINAL);
    let alias = alias_request(ALIAS);
    assert_ne!(
        submission_request_digest(&original.forms, None, Some(2000)).unwrap(),
        submission_request_digest(&alias.forms, None, Some(2000)).unwrap(),
        "logical identity equality must not coalesce distinct exact requests"
    );
}

#[test]
fn native_uri_identity_upserts_lookup_refs_and_redundancy_preserve_the_first_fact() {
    let initial = Database::new(schema()).unwrap().database_value();
    let first = initial.with_forms(&original_request().forms, 1000).unwrap();
    let entity = first.tempids["first"];
    assert_eq!(
        first.db_after.lookup(URI_KEY, &uri(ALIAS)).unwrap(),
        Some(entity)
    );
    let upsert = first
        .db_after
        .with_forms(&alias_request(ALIAS).forms, 2000)
        .unwrap();
    assert_eq!(upsert.tempids["upsert"], entity);
    assert!(
        upsert
            .tx_data
            .iter()
            .all(|datom| datom.attribute != URI_KEY),
        "an equivalent URI is a redundant assertion, not a retract/add rewrite"
    );
    assert_key_spelling(&upsert.db_after, entity, ORIGINAL);
    let lookup = upsert
        .db_after
        .with(
            &[add(
                EntityRef::Lookup {
                    attribute: URI_KEY,
                    value: uri("hTtP://Example.Com/a%2Fb"),
                },
                SCORE,
                Value::Long(3),
            )],
            3000,
        )
        .unwrap();
    assert_eq!(
        lookup.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(3)]
    );
    assert_eq!(
        first.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(1)]
    );
    assert!(initial.lookup(URI_KEY, &uri(ALIAS)).unwrap().is_none());
    assert_key_spelling(&lookup.db_after, entity, ORIGINAL);
    for distinct in ["http://example.com:80/a%2fb", "http://example.com/a/b"] {
        assert_eq!(
            lookup.db_after.lookup(URI_KEY, &uri(distinct)).unwrap(),
            None
        );
    }
}

#[test]
fn uri_aliases_unify_distinct_tempids_in_the_same_native_transaction() {
    let db = Database::new(schema()).unwrap().database_value();
    let report = db
        .with(
            &[
                add(EntityRef::Temp("a".into()), URI_KEY, uri(ORIGINAL)),
                add(EntityRef::Temp("b".into()), URI_KEY, uri(ALIAS)),
            ],
            1000,
        )
        .unwrap();
    assert_eq!(report.tempids["a"], report.tempids["b"]);
    assert_eq!(
        report
            .db_after
            .values(report.tempids["a"], URI_KEY)
            .unwrap()
            .len(),
        1
    );
}

#[test]
fn invalid_uri_admission_is_atomic_and_alias_retraction_removes_the_original_fact() {
    let initial = Database::new(schema()).unwrap().database_value();
    let first = initial.with_forms(&original_request().forms, 1000).unwrap();
    let entity = first.tempids["first"];
    for text in ["http://a b", "http://a/%zz", "http://[wrong]/", "x:"] {
        let edn = format!(r#"#atomic/uri "{text}""#);
        assert!(edn_to_value(&read_edn(&edn).unwrap()).is_err());
        let error = first
            .db_after
            .with(
                &[
                    add(EntityRef::Id(entity), SCORE, Value::Long(99)),
                    add(EntityRef::Id(entity), URI_KEY, uri(text)),
                ],
                2000,
            )
            .unwrap_err();
        assert_eq!(error.code, "value/invalid-uri");
        assert_eq!(
            first.db_after.values(entity, SCORE).unwrap(),
            vec![Value::Long(1)]
        );
    }
    let retracted = first
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: URI_KEY,
                value: Some(uri(ALIAS).into()),
            }],
            2000,
        )
        .unwrap();
    assert!(
        retracted
            .db_after
            .lookup(URI_KEY, &uri(ORIGINAL))
            .unwrap()
            .is_none()
    );
    let fact = retracted
        .tx_data
        .iter()
        .find(|d| d.attribute == URI_KEY)
        .unwrap();
    assert!(!fact.added);
    assert_eq!(spelling(&fact.value), ORIGINAL);
    assert_key_spelling(&first.db_after, entity, ORIGINAL);
}

fn assert_replayed(actual: &ServiceTransactionReport, expected: &ServiceTransactionReport) {
    assert!(actual.replayed);
    assert_eq!(actual.basis_t, expected.basis_t);
    assert_eq!(actual.tx_hash, expected.tx_hash);
    assert_eq!(actual.tempids, expected.tempids);
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(actual.db_before.basis_t(), expected.db_before.basis_t());
    assert_eq!(actual.db_after.basis_t(), expected.db_after.basis_t());
    // Exact bytes catch spelling drift that logical Datom equality would hide.
    let bytes = |report: &ServiceTransactionReport| {
        report
            .tx_data
            .iter()
            .map(|datom| canonical_datom_bytes(datom).unwrap())
            .collect::<Vec<_>>()
    };
    assert_eq!(bytes(actual), bytes(expected));
}

#[test]
fn postgres_uri_identity_survives_indexing_reopen_and_exact_receipt_retry() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL URI durability: set ATOMIC_POSTGRES_URL");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "uri_values");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let database = BlockDatabase::create(&config, "uri_values", schema()).unwrap();
    let mut writer =
        BlockTransactor::claim(&config, database.clone(), BlockWriterOptions::default()).unwrap();
    let first_request = original_request();
    let first = writer.transact(&first_request).unwrap();
    let entity = first.tempids["first"];
    let request = alias_request(ALIAS).comparing_basis(first.basis_t);
    let second = writer.transact(&request).unwrap();
    assert_eq!(second.tempids["upsert"], entity);
    assert!(
        second
            .tx_data
            .iter()
            .all(|datom| datom.attribute != URI_KEY)
    );
    let third = writer
        .transact(
            &TransactionRequest::new(
                "uri-lookup",
                vec![add(
                    EntityRef::Lookup {
                        attribute: URI_KEY,
                        value: uri(ALIAS),
                    },
                    SCORE,
                    Value::Long(3),
                )],
            )
            .with_tx_instant(3000),
        )
        .unwrap();
    writer.release().unwrap();
    common::consolidate(&fixture.connection, "uri_values").unwrap();

    let reader = BlockReader::connect(&config, Default::default()).unwrap();
    let reopened = reader
        .capture(&database.reference_key())
        .unwrap()
        .database_value();
    assert_eq!(reopened.basis_t(), third.basis_t);
    assert_eq!(reopened.lookup(URI_KEY, &uri(ALIAS)).unwrap(), Some(entity));
    assert_key_spelling(&reopened, entity, ORIGINAL);
    assert_eq!(
        reopened.values(entity, SCORE).unwrap(),
        vec![Value::Long(3)]
    );

    let mut writer =
        BlockTransactor::claim(&config, database, BlockWriterOptions::default()).unwrap();
    let replay = writer.transact(&request).unwrap();
    assert_replayed(&replay, &second);
    assert_replayed(&writer.transact(&first_request).unwrap(), &first);
    assert_eq!(
        replay.db_before.values(entity, SCORE).unwrap(),
        vec![Value::Long(1)]
    );
    assert_eq!(
        replay.db_after.values(entity, SCORE).unwrap(),
        vec![Value::Long(2)]
    );
    assert_key_spelling(&replay.db_before, entity, ORIGINAL);
    assert_key_spelling(&replay.db_after, entity, ORIGINAL);
    let changed_spelling = alias_request(ORIGINAL).comparing_basis(first.basis_t);
    assert_eq!(
        writer.transact(&changed_spelling).unwrap_err().code,
        "postgres/idempotency-key-reused"
    );
    assert_eq!(writer.db().unwrap().basis_t(), third.basis_t);
    writer.release().unwrap();
}
