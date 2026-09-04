use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, PostgresMigrator, PostgresStore, Schema,
    ServiceTransactionReport, TransactionRequest, TransactionService, TransactionServiceConfig,
    TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const PERSON_EMAIL: u32 = 1_000;
const PERSON_NAME: u32 = 1_001;
const PERSON_AGE: u32 = 1_002;
const PERSON_TAG: u32 = 1_003;
const PERSON_FRIEND: u32 = 1_004;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique_name(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                PERSON_EMAIL,
                Keyword::new("person", "email"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_AGE,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_TAG,
            Keyword::new("person", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_FRIEND,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn config(connection: &str, database_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: unique_name("transactor_differential_holder"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn scalar(value: Value) -> TxValue {
    TxValue::Scalar(value)
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: scalar(value),
    }
}

/// Apply one request to both semantic engines and compare every observable
/// part of the transaction report and both immutable database values.
fn transact_both(
    service: &TransactionService,
    oracle: &mut Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) -> ServiceTransactionReport {
    let expected = oracle.with(&operations, instant).unwrap();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap();

    assert!(!actual.replayed, "a fresh request was reported as a replay");
    assert_eq!(actual.basis_t, expected.db_after.basis_t());
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(actual.tempids, expected.tempids);
    common::assert_same_information(&actual.db_before, &expected.db_before);
    common::assert_same_information(&actual.db_after, &expected.db_after);

    *oracle = expected.db_after;
    actual
}

fn reject_both(
    service: &TransactionService,
    oracle: &Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) {
    let expected = oracle.with(&operations, instant).unwrap_err();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap_err();
    assert_eq!(actual, expected);
}

/// Tiny fixed-seed generator used to vary entity and value choices without
/// making this conformance witness probabilistic or adding a property-test
/// dependency. The operation family is selected separately so every semantic
/// boundary is exercised on every run.
struct Deterministic(u64);

impl Deterministic {
    fn next(&mut self) -> u64 {
        let mut value = self.0;
        value ^= value << 13;
        value ^= value >> 7;
        value ^= value << 17;
        self.0 = value;
        value
    }

    fn index(&mut self, len: usize) -> usize {
        (self.next() as usize) % len
    }
}

/// Compare the production assessor with `Database::with` without repeatedly
/// materializing the growing database. Every successful transition compares
/// the complete transaction report plus constant-size database metadata; the
/// complete information state is compared once at the end of the sequence.
fn generated_step_both(
    service: &TransactionService,
    oracle: &mut Database,
    request_key: String,
    operations: Vec<TxOp>,
    instant: i64,
) -> bool {
    let expected = oracle.with(&operations, instant);
    let actual = service.client().transact(
        TransactionRequest::new(request_key, operations)
            .comparing_basis(oracle.basis_t())
            .with_tx_instant(instant),
        Duration::from_secs(5),
    );

    match (expected, actual) {
        (Ok(expected), Ok(actual)) => {
            assert!(!actual.replayed);
            assert_eq!(actual.db_before.basis_t(), expected.db_before.basis_t());
            assert_eq!(
                actual.db_before.eidx_frontier(),
                expected.db_before.eidx_frontier()
            );
            assert_eq!(actual.db_before.schema(), expected.db_before.schema());
            assert_eq!(actual.basis_t, expected.db_after.basis_t());
            assert_eq!(actual.db_after.basis_t(), expected.db_after.basis_t());
            assert_eq!(
                actual.db_after.eidx_frontier(),
                expected.db_after.eidx_frontier()
            );
            assert_eq!(actual.db_after.schema(), expected.db_after.schema());
            assert_eq!(actual.tx_data, expected.tx_data);
            assert_eq!(actual.tempids, expected.tempids);
            *oracle = expected.db_after;
            true
        }
        (Err(expected), Err(actual)) => {
            // Messages are diagnostics, not semantic identity. Category and
            // stable code are the public anomaly contract shared by the two
            // assessors.
            assert_eq!(actual.category, expected.category);
            assert_eq!(actual.code, expected.code);
            false
        }
        (expected, actual) => {
            panic!("production/oracle outcome mismatch: expected={expected:?}, actual={actual:?}")
        }
    }
}

#[test]
fn production_writer_matches_the_pure_kernel_across_transactions_and_restart() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique_name("transactor_differential");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let mut oracle = store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let service = TransactionService::start(config(&connection, &database_id)).unwrap();
    let seeded = transact_both(
        &service,
        &mut oracle,
        "seed-people",
        vec![
            add(
                EntityRef::Temp("alice".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_NAME,
                Value::String("Ada".into()),
            ),
            add(EntityRef::Temp("alice".into()), PERSON_AGE, Value::Long(36)),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("logic".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("math".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_EMAIL,
                Value::String("bob@example.test".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_NAME,
                Value::String("Bob".into()),
            ),
            TxOp::Add {
                entity: EntityRef::Temp("bob".into()),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Temp("alice".into())),
            },
        ],
        10_000,
    );
    let alice = seeded.tempids["alice"];
    let bob = seeded.tempids["bob"];

    let upserted = transact_both(
        &service,
        &mut oracle,
        "upsert-and-replace",
        vec![
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_NAME,
                Value::String("Ada Lovelace".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_AGE,
                Value::Long(37),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_TAG,
                Value::String("computing".into()),
            ),
            TxOp::Retract {
                entity: EntityRef::Temp("alice-upsert".into()),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("math".into()))),
            },
        ],
        10_001,
    );
    assert_eq!(upserted.tempids["alice-upsert"], alice);

    // Force the next assessment to reconstruct its immutable db-before from
    // the durable root and log, not from the first service's resident value.
    service.shutdown();
    let service = TransactionService::start(config(&connection, &database_id)).unwrap();

    reject_both(
        &service,
        &oracle,
        "stale-cas",
        vec![TxOp::Cas {
            entity: EntityRef::Id(alice),
            attribute: PERSON_AGE,
            old: Some(scalar(Value::Long(36))),
            new: scalar(Value::Long(38)),
        }],
        10_002,
    );

    transact_both(
        &service,
        &mut oracle,
        "cas-and-retract",
        vec![
            TxOp::Cas {
                entity: EntityRef::Id(alice),
                attribute: PERSON_AGE,
                old: Some(scalar(Value::Long(37))),
                new: scalar(Value::Long(38)),
            },
            TxOp::Retract {
                entity: EntityRef::Id(alice),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("logic".into()))),
            },
            TxOp::Retract {
                entity: EntityRef::Id(bob),
                attribute: PERSON_NAME,
                value: None,
            },
        ],
        10_002,
    );

    let mut age = oracle.schema().attribute(PERSON_AGE).unwrap().clone();
    age.ident = Keyword::new("person", "years");
    age.no_history = true;
    transact_both(
        &service,
        &mut oracle,
        "alter-age-schema",
        vec![TxOp::AlterAttribute(age)],
        10_003,
    );
    transact_both(
        &service,
        &mut oracle,
        "post-alter-and-retract-entity",
        vec![
            add(EntityRef::Id(alice), PERSON_AGE, Value::Long(39)),
            TxOp::RetractEntity(EntityRef::Id(bob)),
        ],
        10_004,
    );
    service.shutdown();

    let mut store = PostgresStore::connect(&connection).unwrap();
    let recovered = store.recover(&database_id).unwrap();
    common::assert_same_information(&recovered, &oracle);
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "age")),
        Some(PERSON_AGE)
    );
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "years")),
        Some(PERSON_AGE)
    );
}

#[test]
fn generated_production_transactions_match_the_pure_kernel() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique_name("generated_transactor_differential");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let mut oracle = store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let mut service = TransactionService::start(config(&connection, &database_id)).unwrap();
    let mut seed_ops = Vec::new();
    for ordinal in 0..8_u64 {
        let tempid = format!("person-{ordinal}");
        seed_ops.extend([
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_EMAIL,
                Value::String(format!("person-{ordinal}@example.test")),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_NAME,
                Value::String(format!("Person {ordinal}")),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_AGE,
                Value::Long(20 + ordinal as i64),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_TAG,
                Value::String(format!("seed-tag-{}", ordinal % 3)),
            ),
        ]);
        if ordinal > 0 {
            seed_ops.push(TxOp::Add {
                entity: EntityRef::Temp(tempid),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Temp(format!("person-{}", ordinal - 1))),
            });
        }
    }
    let seeded = transact_both(&service, &mut oracle, "generated-seed", seed_ops, 20_000);
    let entities: Vec<u64> = (0..8)
        .map(|ordinal| seeded.tempids[&format!("person-{ordinal}")])
        .collect();

    // Keep schema/data interaction inside the generated production run, not
    // only in isolated assessor units. This one localized transition covers
    // rename/alias derivation, AVET enablement over extant values, no-history
    // metadata, background publication, and exact report equivalence.
    let mut altered_tag = oracle.schema().attribute(PERSON_TAG).unwrap().clone();
    altered_tag.ident = Keyword::new("person", "label");
    altered_tag.indexed = true;
    altered_tag.no_history = true;
    assert!(generated_step_both(
        &service,
        &mut oracle,
        "generated-schema-transition".into(),
        vec![TxOp::AlterAttribute(altered_tag)],
        20_001,
    ));

    let mut random = Deterministic(0x4d59_5df4_d0f3_3173);
    let mut coverage = [0_usize; 12];
    let mut successes = 0_usize;
    let mut rejections = 0_usize;

    // Six passes through every operation family. The fixed-seed choices vary
    // entities, values, and declaration order while retaining a permanently
    // reproducible failing case if the two assessors ever diverge.
    for step in 0..72_usize {
        let family = step % coverage.len();
        coverage[family] += 1;
        let entity = entities[random.index(entities.len())];
        let nonce = random.next();
        let mut operations = match family {
            // Cardinality-one replacement.
            0 => vec![add(
                EntityRef::Id(entity),
                PERSON_NAME,
                Value::String(format!("generated-name-{step}-{nonce}")),
            )],
            // Cardinality-many growth.
            1 => vec![add(
                EntityRef::Id(entity),
                PERSON_TAG,
                Value::String(format!("generated-tag-{}", nonce % 11)),
            )],
            // Exact cardinality-many retraction, preferring a fact that is
            // currently present so this normally changes information.
            2 => {
                let value = oracle
                    .values(entity, PERSON_TAG)
                    .into_iter()
                    .next()
                    .cloned()
                    .unwrap_or_else(|| Value::String(format!("missing-tag-{nonce}")));
                vec![TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_TAG,
                    value: Some(scalar(value)),
                }]
            }
            // Ref-valued cardinality-one replacement.
            3 => vec![TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Id(entities[random.index(entities.len())])),
            }],
            // Unique-identity upsert. The first two identities are never
            // retracted by this generator, so this always resolves an
            // otherwise-new tempid onto an existing entity.
            4 => {
                let owner = random.index(2);
                let tempid = format!("generated-upsert-{step}");
                vec![
                    add(
                        EntityRef::Temp(tempid.clone()),
                        PERSON_EMAIL,
                        Value::String(format!("person-{owner}@example.test")),
                    ),
                    add(
                        EntityRef::Temp(tempid),
                        PERSON_NAME,
                        Value::String(format!("upserted-name-{step}")),
                    ),
                ]
            }
            // Successful CAS, including the Datomic `nil` old-value case
            // after an entity has been retracted.
            5 => {
                let old = oracle
                    .values(entity, PERSON_AGE)
                    .into_iter()
                    .next()
                    .cloned()
                    .map(scalar);
                vec![TxOp::Cas {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_AGE,
                    old,
                    new: scalar(Value::Long(100 + step as i64)),
                }]
            }
            // Stale CAS conflict.
            6 => {
                let wrong = oracle
                    .values(entity, PERSON_AGE)
                    .into_iter()
                    .next()
                    .and_then(|value| match value {
                        Value::Long(value) => Some(Value::Long(value.saturating_add(1))),
                        _ => None,
                    })
                    .unwrap_or(Value::Long(-1));
                vec![TxOp::Cas {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_AGE,
                    old: Some(scalar(wrong)),
                    new: scalar(Value::Long(200 + step as i64)),
                }]
            }
            // Attribute-wide cardinality-many retraction.
            7 => vec![TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: PERSON_TAG,
                value: None,
            }],
            // Entity retraction. Preserve two stable identity owners for the
            // upsert and uniqueness-conflict families.
            8 => vec![TxOp::RetractEntity(EntityRef::Id(
                entities[2 + random.index(entities.len() - 2)],
            ))],
            // Unique-value conflict between two permanent entities.
            9 => vec![add(
                EntityRef::Id(entities[0]),
                PERSON_EMAIL,
                Value::String("person-1@example.test".into()),
            )],
            // Stable incorrect-type failure.
            10 => vec![add(
                EntityRef::Id(entity),
                PERSON_NAME,
                Value::Long(step as i64),
            )],
            // A multi-form transaction with a lookup ref. Reversing an
            // unordered declaration on half the cases also exercises the
            // kernel's normalization boundary.
            11 => vec![
                add(
                    EntityRef::Id(entity),
                    PERSON_TAG,
                    Value::String(format!("batch-tag-{}", nonce % 7)),
                ),
                add(
                    EntityRef::Id(entities[random.index(entities.len())]),
                    PERSON_NAME,
                    Value::String(format!("batch-name-{step}")),
                ),
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_FRIEND,
                    value: TxValue::Entity(EntityRef::Lookup {
                        attribute: PERSON_EMAIL,
                        value: Value::String(format!("person-{}@example.test", random.index(2))),
                    }),
                },
            ],
            _ => unreachable!(),
        };
        if nonce & 1 == 1 {
            operations.reverse();
        }

        let accepted = generated_step_both(
            &service,
            &mut oracle,
            format!("generated-step-{step}"),
            operations,
            20_002 + step as i64,
        );
        let expected_rejection = matches!(family, 6 | 9 | 10);
        assert_eq!(
            accepted, !expected_rejection,
            "unexpected outcome for generated operation family {family} at step {step}"
        );
        if accepted {
            successes += 1;
        } else {
            rejections += 1;
        }

        // Reconstruct the production writer twice so the same generated run
        // covers resident, recovered-tail, and recovered-index assessment.
        if matches!(step, 23 | 47) {
            service.shutdown();
            service = TransactionService::start(config(&connection, &database_id)).unwrap();
        }
    }

    assert_eq!(coverage, [6; 12]);
    assert_eq!(successes, 54);
    assert_eq!(rejections, 18);
    service.shutdown();

    // One linear full-state comparison avoids an O(N^2) test while proving
    // that matching reports compose into the same current and historical
    // information in every index order after durable recovery.
    let mut store = PostgresStore::connect(&connection).unwrap();
    let recovered = store.recover(&database_id).unwrap();
    common::assert_same_information(&recovered, &oracle);
}
