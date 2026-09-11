mod common;

use atomic_core::*;
use std::collections::BTreeSet;
use std::sync::{Arc, atomic::AtomicBool};
use std::time::Duration;

const TEXT: u32 = 1000;
const STATUS: u32 = 1001;

fn schema(fulltext: bool) -> Schema {
    let mut schema = Schema::new();
    let mut text = Attribute::new(
        TEXT,
        Keyword::new("article", "text"),
        ValueType::String,
        Cardinality::One,
    );
    if fulltext {
        text = text.fulltext();
    }
    schema.install(text).unwrap();
    schema
        .install(Attribute::new(
            STATUS,
            Keyword::new("article", "status"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn seed() -> TxReport {
    let mut ops = Vec::new();
    for (entity, text, status) in [
        ("a", "Jane's blue river", "active"),
        ("b", "Jane blue mountain", "archived"),
        ("c", "Distant river", "active"),
    ] {
        for (attribute, value) in [(TEXT, text), (STATUS, status)] {
            ops.push(TxOp::Add {
                entity: EntityRef::Temp(entity.into()),
                attribute,
                value: Value::String(value.into()).into(),
            });
        }
    }
    Database::new(schema(true))
        .unwrap()
        .with(&ops, 1000)
        .unwrap()
}
fn fields() -> Vec<Option<Variable>> {
    ["e", "v", "t", "score"]
        .into_iter()
        .map(|name| Some(name.into()))
        .collect()
}
fn query(structured: bool) -> Query {
    let mut query = Query::new(
        FindSpec::Relation(
            ["e", "v", "t", "score"]
                .into_iter()
                .map(|name| FindElement::Variable(name.into()))
                .collect(),
        ),
        vec![Clause::Function {
            function: Function::Fulltext,
            source: "$".into(),
            args: vec![
                Term::Constant(Value::Keyword(Keyword::new("article", "text"))),
                Term::var("search"),
            ],
            binding: Binding::Relation(fields()),
        }],
    );
    query.inputs = vec![InputSpec::Scalar("search".into())];
    if structured {
        query
            .clauses
            .push(Clause::Pattern(Box::new(DataPattern::new(
                Term::var("e"),
                Term::Constant(Value::Ref(u64::from(STATUS))),
                Term::Constant(Value::String("active".into())),
            ))));
    }
    query
}
fn rows(outcome: QueryOutcome) -> Vec<(u64, String, u64, f64)> {
    let QueryResult::Relation(rows) = outcome.result else {
        panic!("relation")
    };
    rows.into_iter()
        .map(|row| {
            let [
                QueryValue::Scalar(Value::Ref(e)),
                QueryValue::Scalar(Value::String(v)),
                QueryValue::Scalar(Value::Ref(t)),
                QueryValue::Scalar(Value::Double(score)),
            ] = row.as_slice()
            else {
                panic!("four fulltext columns")
            };
            assert!(score.is_finite() && *score > 0.0);
            (*e, v.clone(), *t, *score)
        })
        .collect()
}
fn search(db: &DatabaseValue, q: &Query, text: &str) -> QueryOutcome {
    db.query(
        q,
        &[QueryInput::Scalar(Value::String(text.into()))],
        &QueryControl::default(),
    )
    .unwrap()
}
fn entities(rows: &[(u64, String, u64, f64)]) -> BTreeSet<u64> {
    rows.iter().map(|row| row.0).collect()
}

#[test]
fn fulltext_relation_joins_structured_facts_and_prepared_inputs_are_rebound() {
    let seed = seed();
    let db = seed.db_after.database_value();
    let q = query(true);
    let result = search(&db, &q, "JANE");
    assert_eq!(result.stats.fulltext_searches, 1);
    let found = rows(result);
    assert_eq!(entities(&found), BTreeSet::from([seed.tempids["a"]]));
    assert_eq!(found[0].1, "Jane's blue river");
    assert_eq!(found[0].2, t_to_tx(seed.db_after.basis_t()).unwrap());
    let empty = search(&db, &q, "the");
    assert_eq!(empty.stats.fulltext_lagging_searches, 0);
    assert!(rows(empty).is_empty());
    let mut cache = PreparedQueryCache::new(2, 64 * 1024);
    let prepared = cache.prepare(&q).unwrap();
    let sources = [QueryDataSource::database("$", db.clone())];
    for (term, expected) in [
        ("jane", BTreeSet::from([seed.tempids["a"]])),
        ("distant", BTreeSet::from([seed.tempids["c"]])),
    ] {
        let result = prepared
            .execute(
                &sources,
                &[QueryInput::Scalar(Value::String(term.into()))],
                &QueryControl::default(),
            )
            .unwrap();
        assert_eq!(entities(&rows(result)), expected);
    }
    let plain = Database::new(schema(false)).unwrap().database_value();
    assert!(
        prepared
            .execute(
                &[QueryDataSource::database("$", plain)],
                &[QueryInput::Scalar(Value::String("jane".into()))],
                &QueryControl::default()
            )
            .is_err()
    );
}

#[test]
fn fulltext_uses_retained_temporal_filtered_and_speculative_values() {
    let seed = seed();
    let a = seed.tempids["a"];
    let b = seed.tempids["b"];
    let old = seed.db_after.database_value();
    let change = [TxOp::Add {
        entity: EntityRef::Id(a),
        attribute: TEXT,
        value: Value::String("Juliet green meadow".into()).into(),
    }];
    let preview = old.with(&change, 2000).unwrap().db_after;
    let after = seed
        .db_after
        .with(&change, 2000)
        .unwrap()
        .db_after
        .database_value();
    let q = query(false);
    assert_eq!(
        entities(&rows(search(&old, &q, "jane"))),
        BTreeSet::from([a, b])
    );
    for db in [&preview, &after] {
        assert_eq!(entities(&rows(search(db, &q, "jane"))), BTreeSet::from([b]));
        assert_eq!(
            entities(&rows(search(db, &q, "juliet"))),
            BTreeSet::from([a])
        );
    }
    assert_eq!(
        entities(&rows(search(
            &after.clone().as_of(old.basis_t()),
            &q,
            "jane"
        ))),
        BTreeSet::from([a, b])
    );
    assert_eq!(
        entities(&rows(search(&after.clone().history(), &q, "jane"))),
        BTreeSet::from([a, b])
    );
    assert!(rows(search(&after.clone().since(old.basis_t()), &q, "jane")).is_empty());
    let filtered = old.clone().filter(move |_, datom| datom.entity != a);
    assert_eq!(
        entities(&rows(search(&filtered, &q, "jane"))),
        BTreeSet::from([b])
    );
    assert_eq!(
        entities(&rows(search(&old, &q, "jane"))),
        BTreeSet::from([a, b])
    );
}

#[test]
fn fulltext_queries_fail_closed_on_type_source_and_resource_limits() {
    let db = seed().db_after.database_value();
    let q = query(false);
    let input = [QueryInput::Scalar(Value::String("jane".into()))];
    for control in [
        QueryControl {
            max_work: 2,
            ..QueryControl::default()
        },
        QueryControl {
            max_intermediate_rows: 1,
            ..QueryControl::default()
        },
        QueryControl {
            max_result_rows: 1,
            ..QueryControl::default()
        },
        QueryControl {
            cancel: Arc::new(AtomicBool::new(true)),
            ..QueryControl::default()
        },
        QueryControl {
            timeout: Some(Duration::ZERO),
            ..QueryControl::default()
        },
    ] {
        assert!(db.query(&q, &input, &control).is_err());
    }
    assert_eq!(
        db.query(
            &q,
            &[QueryInput::Scalar(Value::Long(7))],
            &QueryControl::default()
        )
        .unwrap_err()
        .code,
        "query/fulltext-search"
    );
    assert!(
        QueryEngine::execute_sources(
            &q,
            &[QueryDataSource::tuples("$", vec![])],
            &input,
            &QueryControl::default()
        )
        .is_err()
    );
    let mut empty = q.clone();
    empty.inputs.push(InputSpec::Collection("unused".into()));
    let mut inputs = input.to_vec();
    inputs.push(QueryInput::Collection(vec![]));
    assert!(
        QueryEngine::execute_sources(
            &empty,
            &[QueryDataSource::tuples("$", vec![])],
            &inputs,
            &QueryControl::default()
        )
        .is_err()
    );
}

#[test]
fn public_fulltext_syntax_scores_and_explicit_truncation() {
    let seed = seed();
    let database = seed.db_after.database_value();
    let (a, b, c) = (seed.tempids["a"], seed.tempids["b"], seed.tempids["c"]);
    for (expression, expected) in [
        ("JANE'S", BTreeSet::from([a, b])),
        ("\"blue river\"", BTreeSet::from([a])),
        ("riv*", BTreeSet::from([a, c])),
        ("jane river", BTreeSet::from([a, b, c])),
        ("jane AND river", BTreeSet::from([a])),
        ("jane AND NOT mountain", BTreeSet::from([a])),
        ("(mountain OR distant) AND NOT jane", BTreeSet::from([c])),
        ("\"jane river\"", BTreeSet::new()),
        ("jane's AND the", BTreeSet::from([a, b])),
    ] {
        let report = database
            .fulltext(TEXT, expression, &FulltextOptions::default())
            .unwrap();
        assert_eq!(
            report
                .hits
                .iter()
                .map(|hit| hit.entity)
                .collect::<BTreeSet<_>>(),
            expected,
            "{expression}"
        );
        assert!(!report.stats.truncated);
        assert!(
            report
                .hits
                .iter()
                .all(|hit| hit.score.is_finite() && hit.score > 0.0)
        );
    }
    for expression in [
        "NOT mountain",
        "jane OR NOT mountain",
        "*",
        "j?ne",
        "jane^2",
        "article:jane",
    ] {
        assert_eq!(
            database
                .fulltext(TEXT, expression, &FulltextOptions::default())
                .unwrap_err()
                .code,
            "fulltext/query-syntax",
            "{expression}"
        );
    }
    let all = database
        .fulltext(TEXT, "river", &FulltextOptions::default())
        .unwrap();
    let limited = database
        .fulltext(
            TEXT,
            "river",
            &FulltextOptions {
                limit: 1,
                ..FulltextOptions::default()
            },
        )
        .unwrap();
    assert_eq!(all.hits.len(), 2);
    assert_eq!(limited.hits, all.hits[..1]);
    assert!(limited.stats.truncated);
    // Same term frequency, but the shorter document earns the higher native
    // BM25 score. This checks ranking direction, not Lucene's floating result.
    assert_eq!(all.hits[0].entity, c);
    assert!(all.hits[0].score > all.hits[1].score);
    let zero = database
        .fulltext(
            TEXT,
            "river",
            &FulltextOptions {
                limit: 0,
                ..FulltextOptions::default()
            },
        )
        .unwrap();
    assert!(zero.hits.is_empty() && zero.stats.truncated);
}

#[test]
fn many_strings_on_one_entity_and_transaction_remain_distinct_history_facts() {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("article", "text"),
                ValueType::String,
                Cardinality::Many,
            )
            .fulltext(),
        )
        .unwrap();
    let base = Database::new(schema).unwrap();
    let values = ["River blue", "River green"];
    let seeded = base
        .with(
            &values
                .into_iter()
                .map(|value| TxOp::Add {
                    entity: EntityRef::Temp("article".into()),
                    attribute: TEXT,
                    value: Value::String(value.into()).into(),
                })
                .collect::<Vec<_>>(),
            1_000,
        )
        .unwrap();
    let before = seeded.db_after.database_value();
    let entity = seeded.tempids["article"];
    let hits = before
        .fulltext(TEXT, "river", &FulltextOptions::default())
        .unwrap()
        .hits;
    assert_eq!(hits.len(), 2);
    assert!(
        hits.iter()
            .all(|hit| hit.entity == entity && hit.tx == t_to_tx(before.basis_t()).unwrap())
    );
    assert_eq!(
        hits.iter()
            .map(|hit| hit.value.as_str())
            .collect::<BTreeSet<_>>(),
        BTreeSet::from(values)
    );
    let after = seeded
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: TEXT,
                value: Some(Value::String(values[0].into()).into()),
            }],
            2_000,
        )
        .unwrap()
        .db_after
        .database_value();
    let current = after
        .fulltext(TEXT, "river", &FulltextOptions::default())
        .unwrap()
        .hits;
    assert_eq!(current.len(), 1);
    assert_eq!(current[0].value, values[1]);
    for view in [
        before.clone(),
        after.clone().as_of(before.basis_t()),
        after.clone().history(),
    ] {
        let historical = view
            .fulltext(TEXT, "river", &FulltextOptions::default())
            .unwrap()
            .hits;
        assert_eq!(historical, hits);
    }
    assert!(
        after
            .clone()
            .since(before.basis_t())
            .fulltext(TEXT, "river", &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
    let q = query(false);
    assert_eq!(rows(search(&after.clone().history(), &q, "river")).len(), 2);
}

#[test]
fn long_unicode_terms_are_not_truncated_or_conflated() {
    // 300 alphabetic Unicode characters (600 UTF-8 bytes) before the differing
    // suffix; an old-style 255-byte/character token cap would conflate these.
    let shared = "é".repeat(300);
    let first = format!("{shared}river");
    let second = format!("{shared}ridge");
    let seeded = Database::new(schema(true))
        .unwrap()
        .with(
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: TEXT,
                    value: Value::String(first.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("second".into()),
                    attribute: TEXT,
                    value: Value::String(second.clone()).into(),
                },
            ],
            1_000,
        )
        .unwrap();
    let database = seeded.db_after.database_value();
    for (term, expected) in [
        (&first, seeded.tempids["first"]),
        (&second, seeded.tempids["second"]),
    ] {
        let found = database
            .fulltext(TEXT, &term.to_uppercase(), &FulltextOptions::default())
            .unwrap();
        assert_eq!(found.hits.len(), 1);
        assert_eq!(found.hits[0].entity, expected);
        assert_eq!(&found.hits[0].value, term);
    }
    let prefix = format!("{shared}ri*");
    let all = database
        .fulltext(TEXT, &prefix, &FulltextOptions::default())
        .unwrap();
    assert_eq!(all.hits.len(), 2);
    // Repeating an exact term already covered by a prefix does not duplicate
    // rows or artificially increase scores. Native postings exercise the same
    // expression through the durable test below.
    let overlap = database
        .fulltext(
            TEXT,
            &format!("{prefix} OR {first}"),
            &FulltextOptions::default(),
        )
        .unwrap();
    assert_eq!(overlap.hits, all.hits);
    assert!(
        database
            .fulltext(TEXT, &shared, &FulltextOptions::default())
            .unwrap()
            .hits
            .is_empty()
    );
}

fn program(q: Query) -> Program {
    Program {
        kind: ProgramKind::Query,
        arity: 1,
        instructions: vec![
            Instruction::Query(
                QueryTemplate::native(q, vec![0], vec![QueryTemplateSource::current("$")]).unwrap(),
            ),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(4), Instruction::EmitRow(4)],
            },
            Instruction::Return,
        ],
    }
}

#[test]
fn fulltext_templates_select_new_abi_in_nested_queries_and_share_program_budget() {
    let seed = seed();
    let db = seed.db_after.database_value();
    let inner = query(true);
    let mut outer = Query::new(
        inner.find.clone(),
        vec![Clause::Function {
            function: Function::Query(Box::new(inner)),
            source: "$".into(),
            args: vec![Term::var("search")],
            binding: Binding::Relation(fields()),
        }],
    );
    outer.inputs = vec![InputSpec::Scalar("search".into())];
    assert_eq!(search(&db, &outer, "jane").stats.fulltext_searches, 1);
    let p = program(outer);
    let bytes = encode_program(&p).unwrap();
    assert_eq!(&bytes[16..18], &9u16.to_be_bytes());
    assert_eq!(decode_program(&bytes).unwrap(), p);
    for version in [4u16, 5, 6, 7, 8] {
        let mut old = bytes.clone();
        old[16..18].copy_from_slice(&version.to_be_bytes());
        let end = old.len() - 32;
        let hash = sha256(&old[..end]);
        old[end..].copy_from_slice(&hash);
        assert!(decode_program(&old).is_err());
    }
    let result = ProgramRuntime
        .execute_query(
            &p,
            &db,
            &[Value::String("jane".into())],
            ProgramControl::default(),
        )
        .unwrap();
    assert!(matches!(result, ProgramOutput::Query(ref rows) if rows.len() == 1));
    let mut budget = ProgramBudget::new(ProgramControl {
        fuel: 100_000,
        max_value_bytes: 1_024,
        ..ProgramControl::default()
    })
    .unwrap();
    let before = budget.remaining_fuel();
    let error = ProgramRuntime
        .execute_query_with_budget(&p, &db, &[Value::String("jane".into())], &mut budget)
        .unwrap_err();
    assert_eq!(error.code, "query/value-byte-limit");
    assert!(budget.remaining_fuel() < before);
}

#[test]
fn native_stored_fulltext_query_and_transaction_survive_replay_and_restart() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED actual PostgreSQL fulltext: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "fulltext_query_program");
    let url = &fixture.connection;
    common::install(url).unwrap();
    let mut store = common::TestStore::connect(url).unwrap();
    let created = store.create_database("search", schema(true)).unwrap();
    let query_program = program(query(true));
    let query_hash = store.deploy_program_blob(&query_program).unwrap();
    let mut selection = query(true);
    selection.find = FindSpec::Relation(vec![FindElement::Variable("e".into())]);
    let transaction_program = Program {
        kind: ProgramKind::Transaction,
        arity: 1,
        instructions: vec![
            Instruction::Query(
                QueryTemplate::native(selection, vec![0], vec![QueryTemplateSource::current("$")])
                    .unwrap(),
            ),
            Instruction::ForEach {
                body: vec![
                    Instruction::Unpack(1),
                    Instruction::PushConstant(Value::String("selected".into())),
                    Instruction::EmitAdd(STATUS),
                ],
            },
            Instruction::Return,
        ],
    };
    let transaction_hash = store.deploy_program_blob(&transaction_program).unwrap();
    let writer = common::start_service(url, "search");
    let mut ops = Vec::new();
    for (entity, text, status) in [
        ("a", "Jane's blue river", "active"),
        ("b", "Jane blue mountain", "archived"),
    ] {
        for (attribute, value) in [(TEXT, text), (STATUS, status)] {
            ops.push(TxOp::Add {
                entity: EntityRef::Temp(entity.into()),
                attribute,
                value: Value::String(value.into()).into(),
            });
        }
    }
    ops.extend([
        TxOp::Add {
            entity: EntityRef::Temp("selector".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("article", "select")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("selector".into()),
            attribute: DB_FN as u32,
            value: Value::Function(transaction_hash).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("query".into()),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(Keyword::new("article", "search")).into(),
        },
        TxOp::Add {
            entity: EntityRef::Temp("query".into()),
            attribute: DB_FN as u32,
            value: Value::Function(query_hash).into(),
        },
    ]);
    let seeded = common::transact(&writer, "seed", created.basis_t(), &ops, 1_000);
    let connection = Connection::connect(url, "search", 32).unwrap();
    let request = writer.client().request_index().unwrap();
    let captured = connection
        .sync_index(request.target_t, Duration::from_secs(20))
        .unwrap();
    let deadline = std::time::Instant::now() + Duration::from_secs(20);
    loop {
        match captured.fulltext(TEXT, "jane", &FulltextOptions::default()) {
            Ok(report) => {
                assert!(report.stats.index_basis_t >= seeded.basis_t);
                assert_eq!(report.hits.len(), 2);
                break;
            }
            Err(error)
                if error.code == "fulltext/index-unavailable"
                    && std::time::Instant::now() < deadline =>
            {
                std::thread::sleep(Duration::from_millis(20))
            }
            Err(error) => panic!("native fulltext was not available: {}", error.code),
        }
    }
    let resolved = store.resolve_program(query_hash).unwrap();
    let exact = captured
        .fulltext(TEXT, "jane", &FulltextOptions::default())
        .unwrap();
    for expression in ["j* OR jane", "j* OR ja* OR jane", "jane OR jane"] {
        let overlap = captured
            .fulltext(TEXT, expression, &FulltextOptions::default())
            .unwrap();
        assert_eq!(
            overlap.hits, exact.hits,
            "overlapping native postings changed document frequencies: {expression}"
        );
    }
    assert_eq!(resolved, query_program);
    let args = [Value::String("jane".into())];
    let expected = ProgramRuntime
        .execute_query(&resolved, &captured, &args, ProgramControl::default())
        .unwrap();
    assert!(
        matches!(&expected, ProgramOutput::Query(rows) if rows.len() == 1 && rows[0][0] == Value::Ref(seeded.tempids["a"]))
    );
    let call = ProgramCall {
        function: CallableRef::Database(EntityRef::Ident(Keyword::new("article", "select"))),
        arguments: vec![RuntimeValue::Scalar(args[0].clone())],
    };
    let preview = captured
        .with_forms(&[TxForm::ProgramCall(call.clone())], 2_000)
        .unwrap();
    let intent = TransactionRequest::new("select", vec![])
        .calling(call)
        .comparing_basis(seeded.basis_t)
        .with_tx_instant(2_000);
    let committed = writer
        .client()
        .transact(intent.clone(), Duration::from_secs(20))
        .unwrap();
    common::assert_same_information(&preview.db_after, &committed.db_after);
    assert_eq!(
        committed
            .db_after
            .values(seeded.tempids["a"], STATUS)
            .unwrap(),
        vec![Value::String("selected".into())]
    );
    assert_eq!(
        captured.values(seeded.tempids["a"], STATUS).unwrap(),
        vec![Value::String("active".into())]
    );
    let retried = writer
        .client()
        .transact(intent.clone(), Duration::from_secs(20))
        .unwrap();
    assert!(retried.replayed);
    assert_eq!(retried.tx_hash, committed.tx_hash);
    writer.shutdown();
    drop(store);
    let mut store = common::TestStore::connect(url).unwrap();
    let restored = store.resolve_program(query_hash).unwrap();
    assert_eq!(
        encode_program(&restored).unwrap(),
        encode_program(&query_program).unwrap()
    );
    let reopened = ProgramRuntime
        .execute_query(&restored, &captured, &args, ProgramControl::default())
        .unwrap();
    let (ProgramOutput::Query(reopened), ProgramOutput::Query(expected)) = (reopened, expected)
    else {
        panic!("stored program changed result kind")
    };
    assert_eq!(reopened, expected);
    let restarted = common::start_service(url, "search");
    let retried = restarted
        .client()
        .transact(intent, Duration::from_secs(20))
        .unwrap();
    assert!(retried.replayed);
    assert_eq!(retried.tx_hash, committed.tx_hash);
    assert_eq!(
        retried.db_after.snapshot_key().unwrap(),
        committed.db_after.snapshot_key().unwrap()
    );
    restarted.shutdown();
    assert_eq!(connection.load_stats().compatibility_materializations, 0);
    eprintln!(
        "actual PostgreSQL fulltext: durable ABI9 query and transaction; exact native view; speculative/committed selection; receipt replay before/after restart"
    );
}
