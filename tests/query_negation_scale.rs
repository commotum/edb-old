//! Count complete indexed-query work, including every suspended-root retry.
//! The expected entity sets come from fixture ordinals, not another evaluator.
use atomic_core::{
    Attribute, Cardinality, Clause, DataPattern, Database, DatabaseValue, EntityRef, FindElement,
    FindSpec, Keyword, Predicate, Query, QueryControl, QueryResult, QueryStats, QueryValue, Rule,
    Schema, Term, TxOp, Value, ValueType,
};
use std::collections::BTreeSet;

const GROUP: u32 = 1_000;
const BLOCKED: u32 = 1_001;
const FLAGGED: u32 = 1_002;
const SIZES: [usize; 3] = [32, 128, 512];

#[derive(Clone, Copy, Debug)]
enum Negation {
    Direct,
    RequiredRuleUnderNot,
    NotInsideRequiredRule,
}

fn fixture(count: usize) -> (DatabaseValue, BTreeSet<u64>) {
    let mut schema = Schema::new();
    for (id, name, value_type) in [
        (GROUP, "group", ValueType::Long),
        (BLOCKED, "blocked", ValueType::Boolean),
        (FLAGGED, "flagged", ValueType::Boolean),
    ] {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("scale", name),
            value_type,
            Cardinality::One,
        );
        attribute.indexed = true;
        schema.install(attribute).unwrap();
    }
    let mut ops = Vec::new();
    for ordinal in 0..count {
        let entity = EntityRef::Temp(format!("person-{ordinal:04}"));
        ops.push(TxOp::Add {
            entity: entity.clone(),
            attribute: GROUP,
            value: Value::Long((ordinal % 2) as i64).into(),
        });
        for (attribute, excluded) in [(BLOCKED, ordinal % 3 == 0), (FLAGGED, ordinal % 5 == 0)] {
            if excluded {
                ops.push(TxOp::Add {
                    entity: entity.clone(),
                    attribute,
                    value: Value::Bool(true).into(),
                });
            }
        }
    }
    let report = Database::new(schema).unwrap().with(&ops, 1_000).unwrap();
    let expected = (0..count)
        .filter(|ordinal| {
            if ordinal % 2 == 0 {
                ordinal % 3 != 0
            } else {
                ordinal % 5 != 0
            }
        })
        .map(|ordinal| report.tempids[&format!("person-{ordinal:04}")])
        .collect();
    (report.db_after.database_value(), expected)
}

fn pattern(attribute: &str, value: Term) -> Clause {
    Clause::Pattern(Box::new(DataPattern::new(
        Term::var("e"),
        Term::Constant(Value::Keyword(Keyword::new("scale", attribute))),
        value,
    )))
}

fn not(clause: Clause) -> Clause {
    Clause::Not {
        join: Some(vec!["e".into()]),
        clauses: vec![clause],
    }
}

fn call(name: &str) -> Clause {
    Clause::Rule {
        source: "$".into(),
        name: name.into(),
        args: vec![Term::var("e")],
    }
}

fn query(negation: Negation, reverse: bool) -> Query {
    let mut rules = Vec::new();
    let mut branches = Vec::new();
    for (group, attribute) in [(0, "blocked"), (1, "flagged")] {
        let fact = pattern(attribute, Term::Constant(Value::Bool(true)));
        let negative = match negation {
            Negation::Direct => not(fact),
            Negation::RequiredRuleUnderNot | Negation::NotInsideRequiredRule => {
                let body = match negation {
                    Negation::RequiredRuleUnderNot => fact,
                    Negation::NotInsideRequiredRule => not(fact),
                    Negation::Direct => unreachable!(),
                };
                rules.push(Rule {
                    name: attribute.into(),
                    head: vec!["e".into()],
                    required: [0].into_iter().collect(),
                    clauses: vec![body],
                });
                match negation {
                    Negation::RequiredRuleUnderNot => not(call(attribute)),
                    Negation::NotInsideRequiredRule => call(attribute),
                    Negation::Direct => unreachable!(),
                }
            }
        };
        branches.push(vec![
            Clause::Predicate {
                predicate: Predicate::Eq,
                source: "$".into(),
                args: vec![Term::var("group"), Term::Constant(Value::Long(group))],
            },
            negative,
        ]);
    }
    if reverse {
        branches.reverse();
        rules.reverse();
        for branch in &mut branches {
            branch.reverse();
        }
    }
    let mut clauses = vec![
        pattern("group", Term::var("group")),
        Clause::Or {
            join: Some(vec!["e".into(), "group".into()]),
            branches,
        },
    ];
    if reverse {
        clauses.reverse();
    }
    let mut query = Query::new(
        FindSpec::Relation(vec![FindElement::Variable("e".into())]),
        clauses,
    );
    query.rules = rules;
    query
}

fn assert_linear_growth(negation: Negation) {
    let mut costs: [Option<(usize, QueryStats)>; 2] = [None, None];
    for count in SIZES {
        let (database, expected) = fixture(count);
        for (order, reverse) in [false, true].into_iter().enumerate() {
            let outcome = database
                .query(&query(negation, reverse), &[], &QueryControl::default())
                .unwrap();
            let QueryResult::Relation(rows) = outcome.result else {
                panic!("expected an entity relation");
            };
            let actual = rows
                .into_iter()
                .map(|row| match row.as_slice() {
                    [QueryValue::Scalar(Value::Ref(entity))] => *entity,
                    other => panic!("expected one entity ID, got {other:?}"),
                })
                .collect::<BTreeSet<_>>();
            assert_eq!(
                actual, expected,
                "{negation:?}, n={count}, reverse={reverse}"
            );
            eprintln!(
                "negation-scale case={negation:?} n={count} reverse={reverse} work={} datoms={} seeks={} clauses={} rule_iterations={} allocated_bytes={}",
                outcome.stats.work,
                outcome.stats.datoms_examined,
                outcome.stats.index_seeks,
                outcome.stats.clauses_executed,
                outcome.stats.rule_iterations,
                outcome.stats.allocated_value_bytes,
            );
            if let Some((small_n, small)) = &costs[order] {
                // Four times the input permits six times the complete work plus
                // fixed overhead. Whole-root/Or-row replay grows sixteenfold.
                // Count all setup, successful prefixes and suspended attempts.
                // Fail at the first bad scale to keep regressions inexpensive.
                assert!(
                    outcome.stats.work <= small.work * 6 + 256,
                    "{negation:?}, reverse={reverse} grew from n={small_n} work={} to n={count} work={}: repeated negative-demand work is not near-linear",
                    small.work,
                    outcome.stats.work,
                );
                assert!(
                    outcome.stats.datoms_examined <= small.datoms_examined * 6 + 16,
                    "{negation:?}, reverse={reverse} grew from n={small_n} datoms={} to n={count} datoms={}: root or branch scans are being replayed",
                    small.datoms_examined,
                    outcome.stats.datoms_examined,
                );
            }
            costs[order] = Some((count, outcome.stats));
        }
    }
}

#[test]
fn or_branches_with_direct_not_have_near_linear_complete_work() {
    assert_linear_growth(Negation::Direct);
}

#[test]
fn or_branches_with_negated_required_rules_have_near_linear_complete_work() {
    assert_linear_growth(Negation::RequiredRuleUnderNot);
}

#[test]
fn or_branches_calling_required_rules_with_not_have_near_linear_complete_work() {
    assert_linear_growth(Negation::NotInsideRequiredRule);
}
