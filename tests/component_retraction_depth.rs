use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, EntityRef, ErrorCategory, IndexOrder,
    IndexPrefix, Keyword, Schema, SpeculationLimits, TxForm, TxOp, TxValue, Value, ValueType,
};
use std::process::Command;
use std::time::Instant;

const LABEL: u32 = 1_000;
const COMPONENT: u32 = 1_001;
const LINK: u32 = 1_002;
const STACK_BYTES: usize = 256 * 1_024;

fn name(index: usize) -> String {
    format!("node-{index:020}")
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            LABEL,
            Keyword::new("graph", "label"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            COMPONENT,
            Keyword::new("graph", "components"),
            ValueType::Ref,
            Cardinality::Many,
        )
        .component(),
        Attribute::new(
            LINK,
            Keyword::new("graph", "link"),
            ValueType::Ref,
            Cardinality::Many,
        ),
    ] {
        schema.install(attribute).unwrap();
    }
    schema
}

fn add(entity: usize, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(name(entity)),
        attribute,
        value: value.into(),
    }
}

fn link(entity: usize, attribute: u32, target: usize) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(name(entity)),
        attribute,
        value: TxValue::Entity(EntityRef::Temp(name(target))),
    }
}

struct Graph<T = Database> {
    database: T,
    ids: Vec<u64>,
}

fn chain_operations(depth: usize) -> Vec<TxOp> {
    let mut operations = Vec::with_capacity(2 * depth + 4);
    for index in 0..depth + 2 {
        operations.push(add(index, LABEL, Value::Long(index as i64)));
        if index + 1 < depth {
            operations.push(link(index, COMPONENT, index + 1));
        }
    }
    // Incoming ordinary refs retract, but neither their owner nor a target of
    // an outgoing noncomponent edge belongs to the component closure.
    operations.push(link(depth, LINK, depth / 2));
    operations.push(link(depth, LINK, depth + 1));
    operations.push(link(0, LINK, depth + 1));
    operations
}

fn flat_chain(depth: usize) -> Graph {
    let report = Database::new(schema())
        .unwrap()
        .with(&chain_operations(depth), 10)
        .unwrap();
    Graph {
        ids: (0..depth + 2)
            .map(|index| report.tempids[&name(index)])
            .collect(),
        database: report.db_after,
    }
}

fn native_chain(depth: usize) -> Graph<DatabaseValue> {
    let report = Database::new(schema())
        .unwrap()
        .database_value()
        .with(&chain_operations(depth), 10)
        .unwrap();
    Graph {
        ids: (0..depth + 2)
            .map(|index| report.tempids[&name(index)])
            .collect(),
        database: report.db_after,
    }
}

fn attribute_facts(db: &DatabaseValue, attribute: u32) -> Vec<atomic_core::Datom> {
    db.datoms_with_prefix(&IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    })
    .unwrap()
}

fn assert_chain_retracted(after: &DatabaseValue, ids: &[u64], tx_data: &[atomic_core::Datom]) {
    let depth = ids.len() - 2;
    let labels = attribute_facts(after, LABEL);
    assert_eq!(labels.len(), 2, "only the two outside entities survive");
    assert_eq!(labels[0].entity, ids[depth]);
    assert_eq!(labels[1].entity, ids[depth + 1]);
    assert!(attribute_facts(after, COMPONENT).is_empty());
    let links = attribute_facts(after, LINK);
    assert_eq!(links.len(), 1);
    assert_eq!(
        (links[0].entity, &links[0].value),
        (ids[depth], &Value::Ref(ids[depth + 1]))
    );
    assert_eq!(
        tx_data.iter().filter(|datom| !datom.added).count(),
        2 * depth + 1
    );
    assert_eq!(
        tx_data.iter().filter(|datom| datom.added).count(),
        1,
        "only the transaction instant is asserted"
    );
}

fn run_depth_child(engine: String, depth: usize) {
    // Build from flat primitive forms before entering the small-stack worker:
    // this is stored graph depth, not nested input or recursive test fixtures.
    enum Seed {
        Eager(Database),
        Exact(DatabaseValue),
    }
    let setup = Instant::now();
    let (before, ids, retained) = match engine.as_str() {
        "eager" => {
            let Graph { database, ids } = flat_chain(depth);
            let retained = database.database_value();
            (Seed::Eager(database), ids, retained)
        }
        "exact" => {
            let Graph { database, ids } = native_chain(depth);
            let retained = database.clone();
            (Seed::Exact(database), ids, retained)
        }
        _ => panic!("unknown engine"),
    };
    eprintln!(
        "COMPONENT_SEED engine={engine} depth={depth} flat_seed_us={}",
        setup.elapsed().as_micros()
    );
    let metrics = std::thread::Builder::new()
        .name(format!("component-{engine}"))
        .stack_size(STACK_BYTES)
        .spawn(move || {
            let started = Instant::now();
            let operations = [TxOp::RetractEntity(EntityRef::Id(ids[0]))];
            match before {
                Seed::Eager(before) => {
                    let report = before.with(&operations, 11).unwrap();
                    assert_chain_retracted(
                        &report.db_after.database_value(),
                        &ids,
                        &report.tx_data,
                    );
                    assert_eq!(
                        attribute_facts(&report.db_before.database_value(), LABEL).len(),
                        depth + 2
                    );
                    drop(report);
                    drop(before);
                }
                Seed::Exact(before) => {
                    let report = before.with(&operations, 11).unwrap();
                    assert_chain_retracted(&report.db_after, &ids, &report.tx_data);
                    assert_eq!(attribute_facts(&report.db_before, LABEL).len(), depth + 2);
                    drop(report);
                    drop(before);
                }
            }
            (engine, started.elapsed())
        })
        .unwrap()
        .join()
        .unwrap();
    assert_eq!(attribute_facts(&retained, LABEL).len(), depth + 2);
    assert_eq!(attribute_facts(&retained, COMPONENT).len(), depth - 1);
    eprintln!(
        "COMPONENT_DEPTH engine={} depth={depth} stack_bytes={STACK_BYTES} retractions={} with_checks_and_report_drop_us={} retained_before=true",
        metrics.0,
        2 * depth + 1,
        metrics.1.as_micros()
    );
}

#[test]
fn deep_component_retraction_is_stack_safe_in_isolated_processes() {
    const CHILD_ENV: &str = "ATOMIC_COMPONENT_RETRACTION_CHILD";
    if let Ok(engine) = std::env::var(CHILD_ENV) {
        let depth = std::env::var("ATOMIC_COMPONENT_RETRACTION_DEPTH")
            .unwrap()
            .parse()
            .unwrap();
        run_depth_child(engine, depth);
        return;
    }
    for (engine, depths) in [("eager", [1_024, 2_048]), ("exact", [1_024, 8_192])] {
        for depth in depths {
            let mut command = Command::new(std::env::current_exe().unwrap());
            command
                .args([
                    "--exact",
                    "deep_component_retraction_is_stack_safe_in_isolated_processes",
                    "--nocapture",
                ])
                .env(CHILD_ENV, engine)
                .env("ATOMIC_COMPONENT_RETRACTION_DEPTH", depth.to_string());
            #[cfg(unix)]
            {
                use std::os::unix::process::CommandExt;
                // A regression may abort this child, never the parent harness;
                // suppress core dumps rather than writing large failure files.
                unsafe {
                    command.pre_exec(|| {
                        let limit = libc::rlimit {
                            rlim_cur: 0,
                            rlim_max: 0,
                        };
                        if libc::setrlimit(libc::RLIMIT_CORE, &limit) == 0 {
                            Ok(())
                        } else {
                            Err(std::io::Error::last_os_error())
                        }
                    });
                }
            }
            let output = command.output().unwrap();
            eprint!("{}", String::from_utf8_lossy(&output.stderr));
            assert!(
                output.status.success(),
                "engine={engine} depth={depth} status={} stdout={} stderr={}",
                output.status,
                String::from_utf8_lossy(&output.stdout),
                String::from_utf8_lossy(&output.stderr)
            );
        }
    }
}

#[test]
fn cycles_shared_components_and_incoming_refs_preserve_exact_closure() {
    let mut operations = (0..7)
        .map(|index| add(index, LABEL, Value::Long(index as i64)))
        .collect::<Vec<_>>();
    for (from, to) in [(0, 1), (0, 2), (1, 3), (2, 3), (3, 0), (4, 3)] {
        operations.push(link(from, COMPONENT, to));
    }
    for (from, to) in [(5, 2), (0, 6), (4, 6)] {
        operations.push(link(from, LINK, to));
    }
    let seed = Database::new(schema())
        .unwrap()
        .with(&operations, 10)
        .unwrap();
    let ids = (0..7)
        .map(|index| seed.tempids[&name(index)])
        .collect::<Vec<_>>();
    let before = seed.db_after;
    let original = before.datoms(atomic_core::View::Current, IndexOrder::Eavt);
    let forms = [TxOp::RetractEntity(EntityRef::Id(ids[0]))];
    let eager = before.with(&forms, 11).unwrap();
    let exact = before.database_value().with(&forms, 11).unwrap();
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(
        attribute_facts(&exact.db_after, LABEL)
            .iter()
            .map(|datom| datom.entity)
            .collect::<Vec<_>>(),
        vec![ids[4], ids[5], ids[6]]
    );
    assert!(attribute_facts(&exact.db_after, COMPONENT).is_empty());
    assert_eq!(
        exact.db_after.values(ids[4], LINK).unwrap(),
        vec![Value::Ref(ids[6])]
    );
    assert!(exact.db_after.values(ids[5], LINK).unwrap().is_empty());
    assert_eq!(
        exact.tx_data.iter().filter(|datom| !datom.added).count(),
        12
    );
    assert_eq!(
        before.datoms(atomic_core::View::Current, IndexOrder::Eavt),
        original
    );
}

#[test]
fn bounded_retraction_failure_leaves_the_database_value_unchanged() {
    let graph = flat_chain(256);
    let before = graph.database.database_value();
    let original = before.datoms(IndexOrder::Eavt).unwrap();
    let basis = before.basis_t();
    let forms = [TxForm::Op(TxOp::RetractEntity(EntityRef::Id(graph.ids[0])))];
    for limits in [
        SpeculationLimits {
            max_read_datoms: 32,
            ..Default::default()
        },
        SpeculationLimits {
            max_read_bytes: 512,
            ..Default::default()
        },
    ] {
        let error = before
            .with_forms_with_limits(&forms, 11, limits)
            .unwrap_err();
        assert_eq!(
            (error.category, error.code),
            (ErrorCategory::Busy, "transaction/read-capacity")
        );
        assert_eq!(before.basis_t(), basis);
        assert_eq!(before.datoms(IndexOrder::Eavt).unwrap(), original);
    }
    let successful = before.with_forms(&forms, 11).unwrap();
    assert_chain_retracted(&successful.db_after, &graph.ids, &successful.tx_data);
}
