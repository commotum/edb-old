#![cfg(unix)]

mod common;
use atomic_core::*;
use common::product_support::{Fixture, Server, cli};
use std::collections::BTreeSet;
use std::os::unix::fs::PermissionsExt;
use std::time::Duration;

const TAG: u32 = 1_000;
const BLOCKED: u32 = 1_001;
const WAIT: Duration = Duration::from_secs(20);

fn pattern(attribute: u32, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        Term::var("e"),
        Term::Constant(Value::Ref(u64::from(attribute))),
        value,
    )))
}

fn call(name: &str) -> Clause {
    Clause::Rule {
        source: "$".into(),
        name: name.into(),
        args: vec![Term::var("e")],
    }
}

fn eligible() -> Query {
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("e".into())]),
        vec![call("eligible")],
    );
    query.rules = vec![
        Rule {
            name: "eligible".into(),
            head: vec!["e".into()],
            required: BTreeSet::new(),
            clauses: vec![
                pattern(TAG, Term::var("tag")),
                Clause::Not {
                    join: Some(vec!["e".into()]),
                    clauses: vec![call("blocked")],
                },
            ],
        },
        Rule {
            name: "blocked".into(),
            head: vec!["e".into()],
            required: BTreeSet::from([0]),
            clauses: vec![pattern(BLOCKED, Term::Constant(Value::Bool(true)))],
        },
    ];
    query
}

fn assert_entities(db: &DatabaseValue, query: &Query, expected: &[u64]) {
    let result = db
        .query(query, &[], &QueryControl::default())
        .unwrap()
        .result;
    let QueryResult::Relation(rows) = result else {
        panic!("expected relation")
    };
    let actual: BTreeSet<_> = rows
        .into_iter()
        .map(|row| match row.as_slice() {
            [QueryValue::Scalar(Value::Ref(entity))] => *entity,
            other => panic!("unexpected entity result: {other:?}"),
        })
        .collect();
    assert_eq!(actual, expected.iter().copied().collect());
}

#[test]
fn cli_transactor_repairs_identity_and_peer_queries_across_restart() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED declarative repair PostgreSQL witness: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = Fixture::new(&connection);
    if let Some((writer, peer)) = &fixture.roles {
        cli(
            &fixture.admin_url,
            &["migrate", "--writer-role", writer, "--peer-role", peer],
        );
    } else {
        cli(&fixture.admin_url, &["migrate"]);
    }
    const DATABASE: &str = "declarative_repairs";
    cli(&fixture.admin_url, &["create", "--database", DATABASE]);
    let directory = tempfile::tempdir().unwrap();
    std::fs::set_permissions(directory.path(), std::fs::Permissions::from_mode(0o700)).unwrap();
    let endpoint = directory.path().join("transactor.sock");
    let mut server = Server::start(&fixture.writer_url, DATABASE, &endpoint);
    let peer = Connection::connect(&fixture.peer_url, DATABASE, 128).unwrap();
    let schema = peer
        .transact_socket(
            &endpoint,
            TransactionRequest::new(
                "schema",
                vec![
                    TxOp::InstallAttribute(Attribute::new(
                        TAG,
                        Keyword::new("review", "tag"),
                        ValueType::String,
                        Cardinality::Many,
                    )),
                    TxOp::InstallAttribute(Attribute::new(
                        BLOCKED,
                        Keyword::new("review", "blocked"),
                        ValueType::Boolean,
                        Cardinality::One,
                    )),
                ],
            )
            .with_tx_instant(1),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    let empty = schema.db_after;
    let seed_request = TransactionRequest::from_forms(
        "collision-and-query",
        vec![
            TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Temp("__map/00000000000000000000".into())),
                attributes: vec![
                    (
                        AttributeRef::Id(TAG),
                        MapValue::Value(Value::String("explicit".into()).into()),
                    ),
                    (
                        AttributeRef::Id(BLOCKED),
                        MapValue::Value(Value::Bool(true).into()),
                    ),
                ],
            }),
            TxForm::EntityMap(EntityMap {
                id: None,
                attributes: vec![(
                    AttributeRef::Id(TAG),
                    MapValue::Value(Value::String("anonymous".into()).into()),
                )],
            }),
        ],
    )
    .with_tx_instant(2);
    let committed = peer
        .transact_socket(&endpoint, seed_request.clone(), WAIT)
        .unwrap();
    assert!(!committed.replayed);
    let seed_hash = committed.tx_hash;
    let seed = committed.report.unwrap();
    let entity = |tag: &str| {
        seed.tx_data
            .iter()
            .find(|d| d.attribute == TAG && d.value == Value::String(tag.into()))
            .unwrap()
            .entity
    };
    let explicit = entity("explicit");
    let anonymous = entity("anonymous");
    assert_ne!(
        explicit, anonymous,
        "R2 must not merge explicit and anonymous identities"
    );
    assert_entities(&empty, &eligible(), &[]);
    assert_entities(&seed.db_after, &eligible(), &[anonymous]);
    for reverse in [false, true] {
        let mut clauses = vec![
            pattern(TAG, Term::var("tag")),
            Clause::Or {
                join: None,
                branches: [explicit, anonymous]
                    .into_iter()
                    .map(|id| {
                        vec![Clause::Predicate {
                            predicate: Predicate::Eq,
                            source: "$".into(),
                            args: vec![Term::var("e"), Term::Constant(Value::Ref(id))],
                        }]
                    })
                    .collect(),
            },
        ];
        if reverse {
            clauses.reverse();
        }
        assert_entities(
            &seed.db_after,
            &Query::new(
                FindSpec::Relation(vec![FindElement::Variable("e".into())]),
                clauses,
            ),
            &[explicit, anonymous],
        );
    }
    let changed = peer
        .transact_socket(
            &endpoint,
            TransactionRequest::new(
                "block-anonymous",
                vec![TxOp::Add {
                    entity: EntityRef::Id(anonymous),
                    attribute: BLOCKED,
                    value: Value::Bool(true).into(),
                }],
            )
            .with_tx_instant(3),
            WAIT,
        )
        .unwrap()
        .report
        .unwrap();
    assert_entities(&changed.db_after, &eligible(), &[]);
    assert_entities(&seed.db_after, &eligible(), &[anonymous]);
    let old_history = seed
        .db_after
        .clone()
        .history()
        .datoms(IndexOrder::Eavt)
        .unwrap();
    server.stop();
    let mut replacement = Server::start(&fixture.writer_url, DATABASE, &endpoint);
    let reopened = Connection::connect(&fixture.peer_url, DATABASE, 128).unwrap();
    let replay = reopened
        .transact_socket(&endpoint, seed_request, WAIT)
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, seed_hash);
    let report = replay.report.unwrap();
    assert_eq!(report.tempids, seed.tempids);
    assert_eq!(report.tx_data, seed.tx_data);
    assert_eq!(
        report.db_before.datoms(IndexOrder::Eavt).unwrap(),
        empty.datoms(IndexOrder::Eavt).unwrap()
    );
    assert_eq!(
        report
            .db_after
            .clone()
            .history()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        old_history
    );
    assert_entities(&report.db_after, &eligible(), &[anonymous]);
    assert_entities(&reopened.sync().unwrap(), &eligible(), &[]);
    replacement.stop();
    eprintln!(
        "DECLARATIVE_REPAIRS_PG_OK identity=two_entities negated_rule=complete or_orders=equal retained_history=exact restart_retry=exact restricted_roles={}",
        fixture.roles.is_some()
    );
}
