//! Dependency work is counted at real name lookups/source visits, without a
//! test-only schema traversal on the measured path. Fixtures and independent
//! expectations are constructed before the complete validation operation.
use super::*;
use crate::database_value::TransactionReadContext;
use crate::{
    Attribute, Cardinality, Database, EntityRef, Instruction, Keyword, Program, Schema, Symbol,
    TxOp, TxReport, ValueType,
};
use std::time::Instant;

fn eid(index: u64) -> u64 {
    crate::make_eid(crate::USER_PARTITION, index).unwrap()
}

fn add(entity: u64, attribute: u64, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(entity),
        attribute: attribute as u32,
        value: value.into(),
    }
}

fn named(entity: u64, name: &str) -> TxOp {
    add(
        entity,
        crate::DB_IDENT,
        Value::Keyword(Keyword::new("guard", name)),
    )
}

fn predicate(kind: ProgramKind) -> (ProgramHash, Arc<ValidatedProgram>) {
    let program = Program {
        kind,
        arity: 1,
        instructions: vec![
            Instruction::PushConstant(Value::Bool(true)),
            Instruction::Return,
        ],
    };
    let hash = crate::program_hash(&program).unwrap();
    (hash, Arc::new(ValidatedProgram::from_canonical(program)))
}

fn dependency_database(attribute: bool) -> Database {
    let mut schema = Schema::new();
    let mut number = Attribute::new(
        1_000,
        Keyword::new("item", "number"),
        ValueType::Long,
        Cardinality::One,
    );
    if attribute {
        number = number.predicate("guard/active");
    }
    schema.install(number).unwrap();
    let database = Database::new(schema).unwrap();
    if attribute {
        database
    } else {
        database
            .with(
                &[add(
                    eid(1),
                    crate::DB_ENTITY_PREDS,
                    Value::Symbol(Symbol::new("guard", "active")),
                )],
                1_000,
            )
            .unwrap()
            .db_after
    }
}

fn validate(report: &TxReport, programs: &ResolvedPrograms) -> Result<(), SemanticError> {
    validate_successor_program_bindings(
        &mut |hash| {
            Ok(Arc::clone(
                programs.get(&hash).expect("known fixture program"),
            ))
        },
        &report.db_before.database_value(),
        &report.db_after.database_value(),
        &report.tx_data,
    )
}

#[test]
fn unrelated_ident_validation_uses_points_not_unchanged_attribute_enumeration() {
    for width in [32, 256, 1_024] {
        let mut schema = Schema::new();
        for offset in 0..width {
            schema
                .install(
                    Attribute::new(
                        1_000 + offset,
                        Keyword::new("wide", format!("value-{offset}")),
                        ValueType::Long,
                        Cardinality::One,
                    )
                    .predicate(format!("guard/unused-{offset}")),
                )
                .unwrap();
        }
        let database = Database::new(schema)
            .unwrap()
            .with(
                &(0..3)
                    .map(|offset| {
                        add(
                            eid(1 + offset),
                            crate::DB_ENTITY_PREDS,
                            Value::Symbol(Symbol::new("guard", format!("unresolved-{offset}"))),
                        )
                    })
                    .collect::<Vec<_>>(),
                1_000,
            )
            .unwrap()
            .db_after;
        let report = database
            .with(&[named(eid(10), "unrelated-enum")], 2_000)
            .unwrap();
        dependency_work::take();
        let start = Instant::now();
        let mut logical_reads = 0;
        let mut source_reads = 0;
        for _ in 0..16 {
            let context = Arc::new(TransactionReadContext::new(100, 1 << 20));
            let before = report
                .db_before
                .database_value()
                .with_transaction_read_context(Arc::clone(&context));
            let after = report
                .db_after
                .database_value()
                .with_transaction_read_context(Arc::clone(&context));
            validate_successor_program_bindings(
                &mut |_| panic!("unrelated unused predicate cannot gate a write"),
                &before,
                &after,
                &report.tx_data,
            )
            .unwrap();
            let reads = context.snapshot().unwrap();
            logical_reads += reads.logical_datoms;
            source_reads += reads.source_datoms;
            drop((before, after, context));
        }
        let elapsed = start.elapsed();
        let work = dependency_work::take();
        assert_eq!(work.attribute_point_lookups, 16);
        assert_eq!(work.active_attribute_names, 0);
        // Identity writes still inspect entity predicate declarations. This is
        // explicit remaining source work, not a claim that all idents are O(1).
        assert_eq!(work.entity_predicate_datoms, 16 * 3);
        assert_eq!(logical_reads, 16 * 4);
        assert_eq!(source_reads, 16 * 4);
        eprintln!(
            "binding validation width={width}, rounds=16, work={work:?}, logical={logical_reads}, source={source_reads}, validation/check/drop={elapsed:?}"
        );
    }
}

#[test]
fn fresh_nonfunction_ident_cannot_silently_bind_an_unresolved_predicate() {
    for attribute in [false, true] {
        let database = dependency_database(attribute);
        let report = database.with(&[named(eid(2), "active")], 2_000).unwrap();
        let error = validate(&report, &BTreeMap::new()).unwrap_err();
        assert_eq!(error.code, "program/not-a-database-function");
    }
}

#[test]
fn aliases_and_repurposed_names_follow_the_exact_successor_dictionary() {
    let (attribute_hash, attribute_program) = predicate(ProgramKind::AttributePredicate);
    let (entity_hash, entity_program) = predicate(ProgramKind::EntityPredicate);
    let programs = BTreeMap::from([
        (attribute_hash, attribute_program),
        (entity_hash, entity_program),
    ]);
    for attribute in [false, true] {
        let (correct, wrong, error_code) = if attribute {
            (
                attribute_hash,
                entity_hash,
                "program/not-attribute-predicate",
            )
        } else {
            (entity_hash, attribute_hash, "program/not-entity-predicate")
        };
        let original = eid(2);
        let installed = dependency_database(attribute)
            .with(
                &[
                    named(original, "active"),
                    add(original, crate::DB_FN, Value::Function(correct)),
                ],
                2_000,
            )
            .unwrap();
        validate(&installed, &programs).unwrap();
        let renamed = installed
            .db_after
            .with(&[named(original, "renamed")], 3_000)
            .unwrap();
        validate(&renamed, &programs).unwrap();
        assert_eq!(
            renamed.db_after.entid(&Keyword::new("guard", "active")),
            Some(original)
        );
        let wrong_by_eid = renamed
            .db_after
            .with(
                &[add(original, crate::DB_FN, Value::Function(wrong))],
                4_000,
            )
            .unwrap();
        assert_eq!(
            validate(&wrong_by_eid, &programs).unwrap_err().code,
            error_code
        );

        let replacement = eid(3);
        let repurposed_wrong = renamed
            .db_after
            .with(
                &[
                    named(replacement, "active"),
                    add(replacement, crate::DB_FN, Value::Function(wrong)),
                ],
                4_000,
            )
            .unwrap();
        assert_eq!(
            validate(&repurposed_wrong, &programs).unwrap_err().code,
            error_code
        );
        let repurposed = renamed
            .db_after
            .with(
                &[
                    named(replacement, "active"),
                    add(replacement, crate::DB_FN, Value::Function(correct)),
                ],
                4_000,
            )
            .unwrap();
        validate(&repurposed, &programs).unwrap();
        assert_eq!(
            repurposed.db_after.entid(&Keyword::new("guard", "active")),
            Some(replacement)
        );
        let unrelated_old_function = repurposed
            .db_after
            .with(
                &[add(original, crate::DB_FN, Value::Function(wrong))],
                5_000,
            )
            .unwrap();
        // The old function no longer owns the predicate's operative alias.
        validate(&unrelated_old_function, &programs).unwrap();
        assert_eq!(
            renamed.db_after.entid(&Keyword::new("guard", "active")),
            Some(original)
        );
    }
}

#[test]
fn removing_the_last_attribute_reference_removes_its_binding_obligation() {
    let database = dependency_database(true);
    let removed = database
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(1_000),
                attribute: crate::DB_ATTR_PREDS as u32,
                value: Some(Value::Symbol(Symbol::new("guard", "active")).into()),
            }],
            2_000,
        )
        .unwrap();
    validate(&removed, &BTreeMap::new()).unwrap();
    assert!(
        !removed
            .db_after
            .schema()
            .has_attribute_predicate("guard/active")
    );
    assert!(
        removed
            .db_before
            .schema()
            .has_attribute_predicate("guard/active")
    );
    let newly_named = removed
        .db_after
        .with(&[named(eid(2), "active")], 3_000)
        .unwrap();
    validate(&newly_named, &BTreeMap::new()).unwrap();
}

#[test]
fn entity_dependency_scan_still_obeys_the_shared_source_read_budget() {
    let database = Database::new(Schema::new())
        .unwrap()
        .with(
            &(0..3)
                .map(|offset| {
                    add(
                        eid(1 + offset),
                        crate::DB_ENTITY_PREDS,
                        Value::Symbol(Symbol::new("guard", format!("unresolved-{offset}"))),
                    )
                })
                .collect::<Vec<_>>(),
            1_000,
        )
        .unwrap()
        .db_after;
    let context = Arc::new(TransactionReadContext::new(2, 1 << 20));
    let value = database
        .database_value()
        .with_transaction_read_context(Arc::clone(&context));
    let error = predicate_roles(
        &value,
        &BTreeSet::from(["guard/unrelated".to_owned()]),
        &BTreeSet::new(),
    )
    .unwrap_err();
    assert_eq!(error.category, ErrorCategory::Busy);
    assert_eq!(error.code, "transaction/read-capacity");
}
