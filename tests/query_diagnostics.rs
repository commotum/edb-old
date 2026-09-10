use atomic_core::*;
use std::collections::BTreeSet;
use std::time::Instant;
mod common;

fn schema() -> Schema {
    let mut schema = Schema::new();
    let mut age = Attribute::new(
        1000,
        Keyword::new("person", "age"),
        ValueType::Long,
        Cardinality::One,
    );
    age.indexed = true;
    schema.install(age).unwrap();
    schema
        .install(Attribute::new(
            1001,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
}
fn facts() -> Vec<TxOp> {
    [
        ("alice", 42, "Alice"),
        ("bob", 20, "Bob"),
        ("anna", 42, "Anna"),
    ]
    .into_iter()
    .flat_map(|(entity, age, name)| {
        [
            TxOp::Add {
                entity: EntityRef::Temp(entity.into()),
                attribute: 1000,
                value: Value::Long(age).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp(entity.into()),
                attribute: 1001,
                value: Value::String(name.into()).into(),
            },
        ]
    })
    .collect()
}
fn pattern(attribute: u32, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        Term::var("e"),
        Term::Constant(Value::Long(attribute.into())),
        value,
    )))
}
fn selective() -> Query {
    Query::new(
        FindSpec::Collection(FindElement::Variable("name".into())),
        vec![
            pattern(1001, Term::var("name")),
            pattern(1000, Term::Constant(Value::Long(42))),
        ],
    )
}
fn control() -> QueryControl {
    QueryControl {
        diagnostics: Some(Default::default()),
        ..Default::default()
    }
}
fn check_trace(outcome: &QueryOutcome) {
    let trace = outcome.diagnostics.as_ref().unwrap();
    assert!(!trace.truncated);
    assert_eq!(trace.steps.len(), 2);
    assert_eq!(
        trace
            .steps
            .iter()
            .map(|step| step.clause_path.as_str())
            .collect::<Vec<_>>(),
        ["where/1", "where/0"]
    );
    assert_eq!(trace.steps[0].original_index, 1);
    assert!(trace.steps[0].binds_in.is_empty());
    assert_eq!(trace.steps[0].binds_out, ["e"]);
    assert_eq!(trace.steps[1].binds_in, ["e"]);
    assert_eq!(trace.steps[1].binds_out, ["e", "name"]);
    assert_eq!(trace.steps[0].rows_in, 1);
    assert_eq!(trace.steps[0].rows_out, Some(2));
    assert!(trace.steps[0].warnings.contains(&QueryWarning::Expansion));
    assert!(trace.steps[0].work.index_seeks > 0);
    assert!(
        trace
            .steps
            .iter()
            .all(|step| step.bindings_complete && step.status == QueryStepStatus::Complete)
    );
    assert_eq!(
        trace
            .steps
            .iter()
            .map(|step| step.work.datoms_examined)
            .sum::<u64>(),
        outcome.stats.datoms_examined
    );
    for step in &outcome.plan {
        let detailed = &trace.steps[step.diagnostic_step.unwrap()];
        assert_eq!(detailed.access, step.access);
        assert_eq!(detailed.rows_out, Some(step.rows_after));
    }
}

#[test]
fn original_clauses_actual_order_bindings_and_warnings_are_correlated() {
    let db = Database::new(schema())
        .unwrap()
        .with(&facts(), 1000)
        .unwrap()
        .db_after
        .database_value();
    let prepared = PreparedQuery::new(&selective()).unwrap();
    let sources = [QueryDataSource::database("$", db)];
    let plain = prepared
        .execute(&sources, &[], &Default::default())
        .unwrap();
    assert!(plain.diagnostics.is_none());
    assert!(plain.plan.iter().all(|step| step.diagnostic_step.is_none()));
    let detailed = prepared.execute(&sources, &[], &control()).unwrap();
    assert_eq!(plain.result, detailed.result);
    assert_eq!(plain.stats, detailed.stats);
    check_trace(&detailed);
    let scan = prepared
        .execute(
            &sources,
            &[],
            &QueryControl {
                force_scan: true,
                ..control()
            },
        )
        .unwrap();
    assert_eq!(plain.result, scan.result);
    assert!(
        scan.diagnostics
            .unwrap()
            .steps
            .iter()
            .any(|step| step.warnings.contains(&QueryWarning::FullScan))
    );
}

fn predicate(predicate: Predicate, constant: i64) -> Clause {
    Clause::Predicate {
        predicate,
        source: "$".into(),
        args: vec![Term::var("x"), Term::Constant(Value::Long(constant))],
    }
}
fn nested_query() -> Query {
    let mut child = Query::new(
        FindSpec::Scalar(FindElement::Variable("x".into())),
        vec![predicate(Predicate::Greater, 0)],
    );
    child.inputs = vec![InputSpec::Scalar("x".into())];
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable("y".into())),
        vec![
            Clause::Rule {
                source: "$".into(),
                name: "keep".into(),
                args: vec![Term::var("x")],
            },
            Clause::Or {
                join: Some(vec!["x".into()]),
                branches: vec![
                    vec![predicate(Predicate::Less, 3)],
                    vec![predicate(Predicate::Greater, 2)],
                ],
            },
            Clause::Function {
                function: Function::Query(Box::new(child)),
                source: "$".into(),
                args: vec![Term::var("x")],
                binding: Binding::Scalar("y".into()),
            },
        ],
    );
    query.inputs = vec![InputSpec::Collection("x".into())];
    query.rules = vec![Rule {
        name: "keep".into(),
        head: vec!["x".into()],
        required: BTreeSet::from([0]),
        clauses: vec![Clause::Not {
            join: Some(vec!["x".into()]),
            clauses: vec![predicate(Predicate::Eq, 2)],
        }],
    }];
    query
}

#[test]
fn nested_rules_negation_or_and_sequences_keep_distinct_phases() {
    let query = nested_query();
    let inputs = [QueryInput::Collection(vec![
        Value::Long(1),
        Value::Long(2),
        Value::Long(3),
    ])];
    let plain = QueryEngine::execute_sources(&query, &[], &inputs, &Default::default()).unwrap();
    let detailed = QueryEngine::execute_sources(&query, &[], &inputs, &control()).unwrap();
    assert_eq!(plain.result, detailed.result);
    let trace = detailed.diagnostics.as_ref().unwrap();
    assert!(!trace.truncated);
    for (id, step) in trace.steps.iter().enumerate() {
        assert_eq!(step.id, id);
        assert!(step.phase_id < trace.phases.len());
        assert!(step.status != QueryStepStatus::Running);
    }
    assert!(
        trace
            .steps
            .iter()
            .any(|step| step.status == QueryStepStatus::AwaitNegative)
    );
    assert!(
        trace
            .steps
            .iter()
            .any(|step| step.clause_path == "rules/0/0/not/0")
    );
    assert!(
        trace
            .steps
            .iter()
            .any(|step| step.clause_path == "where/1/or/0/0")
    );
    assert!(
        trace
            .steps
            .iter()
            .any(|step| step.clause_path == "where/2/query/where/0")
    );
    assert!(
        trace
            .phases
            .iter()
            .any(|phase| phase.kind == QueryPhaseKind::Rule)
    );
    assert!(
        trace
            .phases
            .iter()
            .any(|phase| phase.kind == QueryPhaseKind::Negation)
    );
    assert!(
        trace
            .phases
            .iter()
            .any(|phase| phase.parent.is_some() && phase.parent_step.is_some())
    );
    for phase in &trace.phases {
        assert!(phase.parent.is_none_or(|parent| parent < phase.id));
    }
    let mut sequence = QueryEngine::sequence_sources(&query, &[], &inputs, &control()).unwrap();
    assert!(
        sequence
            .diagnostics()
            .unwrap()
            .steps
            .iter()
            .any(|step| step.clause_path.contains("/query/"))
    );
    assert_eq!(
        sequence
            .by_ref()
            .collect::<Result<Vec<_>, _>>()
            .unwrap()
            .len(),
        2
    );
}

#[test]
fn bounded_capture_is_payload_free_and_does_not_change_semantic_limits() {
    let secret = "never-log-the-input-or-result-value";
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable("answer".into())),
        vec![Clause::Function {
            function: Function::Ground,
            source: "$".into(),
            args: vec![Term::Constant(Value::String(secret.into()))],
            binding: Binding::Scalar("answer".into()),
        }],
    );
    let prepared = PreparedQuery::new(&query).unwrap();
    let plain = prepared.execute(&[], &[], &Default::default()).unwrap();
    for options in [
        QueryDiagnosticOptions::default(),
        QueryDiagnosticOptions {
            max_steps: 0,
            max_bytes: 1024,
        },
        QueryDiagnosticOptions {
            max_steps: 32,
            max_bytes: 1,
        },
    ] {
        let detailed = prepared
            .execute(
                &[],
                &[],
                &QueryControl {
                    diagnostics: Some(options),
                    ..Default::default()
                },
            )
            .unwrap();
        assert_eq!(plain.result, detailed.result);
        assert_eq!(plain.stats, detailed.stats);
        let trace = detailed.diagnostics.unwrap();
        assert!(trace.steps.len() <= options.max_steps);
        assert!(trace.capture_bytes <= options.max_bytes);
        assert!(trace.inspection_work <= options.max_bytes);
        assert_eq!(
            trace.truncated,
            options.max_steps == 0 || options.max_bytes == 1
        );
        let edn = edn::write_edn(&query_diagnostics_to_edn(&trace)).unwrap();
        assert!(!edn.contains(secret));
    }
    let work = usize::try_from(plain.stats.work).unwrap() - 1;
    let plain = prepared
        .execute(
            &[],
            &[],
            &QueryControl {
                max_work: work,
                ..Default::default()
            },
        )
        .unwrap_err();
    let detailed = prepared
        .execute(
            &[],
            &[],
            &QueryControl {
                max_work: work,
                ..control()
            },
        )
        .unwrap_err();
    assert_eq!(plain.code, detailed.code);
    let cancelled = control();
    cancelled
        .cancel
        .store(true, std::sync::atomic::Ordering::Release);
    assert_eq!(
        prepared.execute(&[], &[], &cancelled).unwrap_err().category,
        ErrorCategory::Interrupted
    );
}

#[test]
fn complete_small_query_cost_reports_optional_capture_without_hidden_work() {
    let mut query = Query::new(
        FindSpec::Collection(FindElement::Variable("x".into())),
        vec![predicate(Predicate::Greater, 4)],
    );
    query.inputs = vec![InputSpec::Collection("x".into())];
    let prepared = PreparedQuery::new(&query).unwrap();
    let input = [QueryInput::Collection((0..256).map(Value::Long).collect())];
    let mut expected_work = None;
    for enabled in [false, true] {
        let started = Instant::now();
        let mut observed = (0, 0);
        for _ in 0..8 {
            let outcome = prepared
                .execute(
                    &[],
                    &input,
                    &QueryControl {
                        diagnostics: enabled.then(QueryDiagnosticOptions::default),
                        ..Default::default()
                    },
                )
                .unwrap();
            let QueryResult::Collection(rows) = &outcome.result else {
                panic!("collection");
            };
            assert_eq!(rows.len(), 251);
            assert_eq!(
                *expected_work.get_or_insert(outcome.stats.work),
                outcome.stats.work
            );
            if let Some(trace) = &outcome.diagnostics {
                observed = (trace.capture_bytes, trace.inspection_work);
                assert!(!trace.truncated);
            }
            drop(outcome);
        }
        eprintln!(
            "query diagnostics enabled={enabled} complete_execute/verify/drop_8={:?} work_per_query={} capture_bytes={} inspection_work={}",
            started.elapsed(),
            expected_work.unwrap(),
            observed.0,
            observed.1
        );
    }
}

#[test]
fn postgres_native_clauses_keep_identity_and_index_selectivity() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL query diagnostics: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "query_diagnostics");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    PostgresStore::connect(&fixture.connection)
        .unwrap()
        .create_database("diagnostics", schema())
        .unwrap();
    let service = common::start_service(&fixture.connection, "diagnostics");
    let connection = Connection::attach(&fixture.connection, service.client(), 0).unwrap();
    let report = connection
        .transact(
            TransactionRequest::new("fixture", facts()),
            std::time::Duration::from_secs(20),
        )
        .unwrap();
    drop(report);
    let target = connection.request_index().unwrap().target_t;
    connection
        .sync_index(target, std::time::Duration::from_secs(20))
        .unwrap();
    let outcome = connection
        .db()
        .query(&selective(), &[], &control())
        .unwrap();
    check_trace(&outcome);
    assert_eq!(
        outcome.diagnostics.as_ref().unwrap().steps[0].access,
        "AVET seek"
    );
    service.shutdown();
}
