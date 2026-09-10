use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, EntityRef, ErrorCategory, IndexOrder,
    IndexPrefix, Keyword, Schema, SpeculationLimits, TxForm, TxOp, USER_PARTITION, Value,
    ValueType, make_eid,
};
use std::process::Command;
use std::time::Instant;

const LABEL: u32 = 1_000;
const COMPONENT: u32 = 1_001;
const LINK: u32 = 1_002;
const STACK_BYTES: usize = 256 * 1_024;

fn node(index: usize) -> u64 {
    make_eid(USER_PARTITION, 10_000 + index as u64).unwrap()
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
        entity: EntityRef::Id(node(entity)),
        attribute,
        value: value.into(),
    }
}

fn flat_chain(depth: usize) -> Database {
    let mut operations = Vec::with_capacity(2 * depth + 4);
    for index in 0..depth + 2 {
        operations.push(add(index, LABEL, Value::Long(index as i64)));
        if index + 1 < depth {
            operations.push(add(index, COMPONENT, Value::Ref(node(index + 1))));
        }
    }
    // Incoming ordinary refs retract, but neither their owner nor a target of
    // an outgoing noncomponent edge belongs to the component closure.
    operations.push(add(depth, LINK, Value::Ref(node(depth / 2))));
    operations.push(add(depth, LINK, Value::Ref(node(depth + 1))));
    operations.push(add(0, LINK, Value::Ref(node(depth + 1))));
    Database::new(schema())
        .unwrap()
        .with(&operations, 10)
        .unwrap()
        .db_after
}

fn attribute_facts(db: &DatabaseValue, attribute: u32) -> Vec<atomic_core::Datom> {
    db.datoms_with_prefix(&IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    })
    .unwrap()
}

fn assert_chain_retracted(after: &DatabaseValue, depth: usize, tx_data: &[atomic_core::Datom]) {
    let labels = attribute_facts(after, LABEL);
    assert_eq!(labels.len(), 2, "only the two outside entities survive");
    assert_eq!(labels[0].entity, node(depth));
    assert_eq!(labels[1].entity, node(depth + 1));
    assert!(attribute_facts(after, COMPONENT).is_empty());
    let links = attribute_facts(after, LINK);
    assert_eq!(links.len(), 1);
    assert_eq!(
        (links[0].entity, &links[0].value),
        (node(depth), &Value::Ref(node(depth + 1)))
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
    let before = flat_chain(depth);
    let retained = before.database_value();
    let metrics = std::thread::Builder::new()
        .name(format!("component-{engine}"))
        .stack_size(STACK_BYTES)
        .spawn(move || {
            let started = Instant::now();
            let operations = [TxOp::RetractEntity(EntityRef::Id(node(0)))];
            match engine.as_str() {
                "eager" => {
                    let report = before.with(&operations, 11).unwrap();
                    assert_chain_retracted(
                        &report.db_after.database_value(),
                        depth,
                        &report.tx_data,
                    );
                    assert_eq!(
                        attribute_facts(&report.db_before.database_value(), LABEL).len(),
                        depth + 2
                    );
                    drop(report);
                }
                "exact" => {
                    let report = before.database_value().with(&operations, 11).unwrap();
                    assert_chain_retracted(&report.db_after, depth, &report.tx_data);
                    assert_eq!(attribute_facts(&report.db_before, LABEL).len(), depth + 2);
                    drop(report);
                }
                _ => panic!("unknown engine"),
            }
            drop(before);
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
    for depth in [1_024, 8_192] {
        for engine in ["eager", "exact"] {
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
        operations.push(add(from, COMPONENT, Value::Ref(node(to))));
    }
    for (from, to) in [(5, 2), (0, 6), (4, 6)] {
        operations.push(add(from, LINK, Value::Ref(node(to))));
    }
    let before = Database::new(schema())
        .unwrap()
        .with(&operations, 10)
        .unwrap()
        .db_after;
    let original = before.datoms(atomic_core::View::Current, IndexOrder::Eavt);
    let forms = [TxOp::RetractEntity(EntityRef::Id(node(0)))];
    let eager = before.with(&forms, 11).unwrap();
    let exact = before.database_value().with(&forms, 11).unwrap();
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(
        attribute_facts(&exact.db_after, LABEL)
            .iter()
            .map(|datom| datom.entity)
            .collect::<Vec<_>>(),
        vec![node(4), node(5), node(6)]
    );
    assert!(attribute_facts(&exact.db_after, COMPONENT).is_empty());
    assert_eq!(
        exact.db_after.values(node(4), LINK).unwrap(),
        vec![Value::Ref(node(6))]
    );
    assert!(exact.db_after.values(node(5), LINK).unwrap().is_empty());
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
    let before = flat_chain(256).database_value();
    let original = before.datoms(IndexOrder::Eavt).unwrap();
    let basis = before.basis_t();
    let forms = [TxForm::Op(TxOp::RetractEntity(EntityRef::Id(node(0))))];
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
    assert_chain_retracted(&successful.db_after, 256, &successful.tx_data);
}
