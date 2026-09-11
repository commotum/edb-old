mod common;
use atomic_core::{
    Attribute, AttributeRef, Cardinality, DB_IDENT, Database, EntityMap, EntityRef, Keyword,
    MapValue, Schema, TransactionRequest, TransactionService, TransactionServiceConfig, TxForm,
    TxFunctions, TxOp, TxValue, Unique, Value, ValueType,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const TARGET_KEY: u32 = 1_000;
const OWNER_TARGET: u32 = 1_001;
const OWNER_LABEL: u32 = 1_002;
const OWNER_TAG: u32 = 1_003;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                TARGET_KEY,
                Keyword::new("target", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(
            Attribute::new(
                OWNER_TARGET,
                Keyword::new("owner", "target"),
                ValueType::Ref,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            OWNER_LABEL,
            Keyword::new("owner", "label"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            OWNER_TAG,
            Keyword::new("owner", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: TxValue::Scalar(value),
    }
}

fn add_ref(entity: EntityRef, attribute: u32, value: EntityRef) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: TxValue::Entity(value),
    }
}

fn seed_ops() -> Vec<TxOp> {
    vec![
        add(
            EntityRef::Temp("target".into()),
            TARGET_KEY,
            Value::String("target-1".into()),
        ),
        add(
            EntityRef::Temp("target".into()),
            DB_IDENT as u32,
            Value::Keyword(Keyword::new("target", "one")),
        ),
        add_ref(
            EntityRef::Temp("owner".into()),
            OWNER_TARGET,
            EntityRef::Temp("target".into()),
        ),
        add(
            EntityRef::Temp("owner".into()),
            OWNER_LABEL,
            Value::String("original".into()),
        ),
    ]
}

#[test]
fn pure_kernel_upserts_ref_valued_identities_and_unifies_symbolic_ref_values() {
    let initial = Database::new(schema()).unwrap();
    let seeded = initial.with(&seed_ops(), 10).unwrap();
    let target = seeded.tempids["target"];
    let owner = seeded.tempids["owner"];

    let by_id = seeded
        .db_after
        .with(
            &[
                add_ref(
                    EntityRef::Temp("owner-by-id".into()),
                    OWNER_TARGET,
                    EntityRef::Id(target),
                ),
                add(
                    EntityRef::Temp("owner-by-id".into()),
                    OWNER_LABEL,
                    Value::String("by-id".into()),
                ),
            ],
            11,
        )
        .unwrap();
    assert_eq!(by_id.tempids["owner-by-id"], owner);
    assert_eq!(
        by_id
            .db_after
            .values(owner, OWNER_LABEL)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>(),
        [Value::String("by-id".into())]
    );

    let by_lookup = by_id
        .db_after
        .with(
            &[
                add_ref(
                    EntityRef::Temp("owner-by-lookup".into()),
                    OWNER_TARGET,
                    EntityRef::Lookup {
                        attribute: TARGET_KEY,
                        value: Value::String("target-1".into()),
                    },
                ),
                add(
                    EntityRef::Temp("owner-by-lookup".into()),
                    OWNER_TAG,
                    Value::String("looked-up".into()),
                ),
            ],
            12,
        )
        .unwrap();
    assert_eq!(by_lookup.tempids["owner-by-lookup"], owner);
    assert_eq!(
        by_lookup
            .db_after
            .values(owner, OWNER_TAG)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>(),
        [Value::String("looked-up".into())]
    );

    let by_ident = by_lookup
        .db_after
        .with(
            &[
                add_ref(
                    EntityRef::Temp("owner-by-ident".into()),
                    OWNER_TARGET,
                    EntityRef::Ident(Keyword::new("target", "one")),
                ),
                add(
                    EntityRef::Temp("owner-by-ident".into()),
                    OWNER_TAG,
                    Value::String("identified".into()),
                ),
            ],
            13,
        )
        .unwrap();
    assert_eq!(by_ident.tempids["owner-by-ident"], owner);
    assert_eq!(
        by_ident
            .db_after
            .values(owner, OWNER_TAG)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>(),
        [
            Value::String("identified".into()),
            Value::String("looked-up".into())
        ]
    );

    let value_only = by_ident
        .db_after
        .with(
            &[add_ref(
                EntityRef::Id(owner),
                OWNER_TARGET,
                EntityRef::Temp("value-only".into()),
            )],
            14,
        )
        .unwrap_err();
    assert_eq!(value_only.code, "transaction/tempid-not-an-entity");

    // The same rule is applied after entity-map normalization, not merely to
    // callers that submit primitive TxOps directly.
    let map_value_only = by_ident
        .db_after
        .with_forms(
            &[TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Id(owner)),
                attributes: vec![(
                    AttributeRef::Id(OWNER_TARGET),
                    MapValue::Value(TxValue::Entity(EntityRef::Temp("map-value-only".into()))),
                )],
            })],
            &TxFunctions::new(),
            14,
        )
        .unwrap_err();
    assert_eq!(map_value_only.code, "transaction/tempid-not-an-entity");

    // Recovered ProcessExpander keeps an unresolved reference tempid as the
    // raw identity key. Two new entities naming that same target therefore
    // unify before permanent ids replace either side of the reference.
    let symbolic = Database::new(schema())
        .unwrap()
        .with(
            &[
                add(
                    EntityRef::Temp("new-target".into()),
                    TARGET_KEY,
                    Value::String("target-2".into()),
                ),
                add_ref(
                    EntityRef::Temp("left".into()),
                    OWNER_TARGET,
                    EntityRef::Temp("new-target".into()),
                ),
                add(
                    EntityRef::Temp("left".into()),
                    OWNER_TAG,
                    Value::String("left".into()),
                ),
                add_ref(
                    EntityRef::Temp("right".into()),
                    OWNER_TARGET,
                    EntityRef::Temp("new-target".into()),
                ),
                add(
                    EntityRef::Temp("right".into()),
                    OWNER_TAG,
                    Value::String("right".into()),
                ),
            ],
            10,
        )
        .unwrap();
    assert_eq!(symbolic.tempids["left"], symbolic.tempids["right"]);
    assert_eq!(
        symbolic
            .db_after
            .values(symbolic.tempids["left"], OWNER_TAG)
            .into_iter()
            .cloned()
            .collect::<Vec<_>>(),
        [Value::String("left".into()), Value::String("right".into())]
    );
}

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn service_config(connection: &str, database_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: unique("ref-identity-holder"),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

#[test]
fn production_assessor_upserts_a_ref_valued_identity_after_restart() {
    let Some(connection) = connection() else {
        return;
    };
    common::install(&connection).unwrap();
    let database_id = unique("ref_unique_identity");
    common::TestStore::connect(&connection)
        .unwrap()
        .create_database(&database_id, schema())
        .unwrap();

    let service = TransactionService::start(service_config(&connection, &database_id)).unwrap();
    let seeded = service
        .client()
        .transact(
            TransactionRequest::new("seed-ref-identity", seed_ops()).with_tx_instant(10),
            Duration::from_secs(5),
        )
        .unwrap();
    let target = seeded.tempids["target"];
    let owner = seeded.tempids["owner"];
    service.shutdown();

    let restarted = TransactionService::start(service_config(&connection, &database_id)).unwrap();
    let upserted = restarted
        .client()
        .transact(
            TransactionRequest::new(
                "upsert-ref-identity",
                vec![
                    add_ref(
                        EntityRef::Temp("owner-upsert".into()),
                        OWNER_TARGET,
                        EntityRef::Id(target),
                    ),
                    add(
                        EntityRef::Temp("owner-upsert".into()),
                        OWNER_LABEL,
                        Value::String("after-restart".into()),
                    ),
                    add_ref(
                        EntityRef::Temp("owner-ident".into()),
                        OWNER_TARGET,
                        EntityRef::Ident(Keyword::new("target", "one")),
                    ),
                    add(
                        EntityRef::Temp("owner-ident".into()),
                        OWNER_TAG,
                        Value::String("identified".into()),
                    ),
                    add_ref(
                        EntityRef::Temp("owner-lookup".into()),
                        OWNER_TARGET,
                        EntityRef::Lookup {
                            attribute: TARGET_KEY,
                            value: Value::String("target-1".into()),
                        },
                    ),
                    add(
                        EntityRef::Temp("owner-lookup".into()),
                        OWNER_TAG,
                        Value::String("looked-up".into()),
                    ),
                ],
            )
            .with_tx_instant(11),
            Duration::from_secs(5),
        )
        .unwrap();
    assert_eq!(upserted.tempids["owner-upsert"], owner);
    assert_eq!(upserted.tempids["owner-ident"], owner);
    assert_eq!(upserted.tempids["owner-lookup"], owner);
    assert_eq!(
        upserted.db_after.values(owner, OWNER_LABEL).unwrap(),
        [Value::String("after-restart".into())]
    );

    let symbolic = restarted
        .client()
        .transact(
            TransactionRequest::new(
                "symbolic-ref-identity",
                vec![
                    add(
                        EntityRef::Temp("new-target".into()),
                        TARGET_KEY,
                        Value::String("target-2".into()),
                    ),
                    add_ref(
                        EntityRef::Temp("left".into()),
                        OWNER_TARGET,
                        EntityRef::Temp("new-target".into()),
                    ),
                    add(
                        EntityRef::Temp("left".into()),
                        OWNER_TAG,
                        Value::String("left".into()),
                    ),
                    add_ref(
                        EntityRef::Temp("right".into()),
                        OWNER_TARGET,
                        EntityRef::Temp("new-target".into()),
                    ),
                    add(
                        EntityRef::Temp("right".into()),
                        OWNER_TAG,
                        Value::String("right".into()),
                    ),
                ],
            )
            .with_tx_instant(12),
            Duration::from_secs(5),
        )
        .unwrap();
    assert_eq!(symbolic.tempids["left"], symbolic.tempids["right"]);
    assert_eq!(
        symbolic
            .db_after
            .values(symbolic.tempids["left"], OWNER_TAG)
            .unwrap(),
        [Value::String("left".into()), Value::String("right".into())]
    );

    let basis_before_rejection = symbolic.basis_t;
    let value_only = restarted
        .client()
        .transact(
            TransactionRequest::new(
                "value-only-ref-tempid",
                vec![add_ref(
                    EntityRef::Id(owner),
                    OWNER_TARGET,
                    EntityRef::Temp("value-only".into()),
                )],
            )
            .with_tx_instant(13),
            Duration::from_secs(5),
        )
        .unwrap_err();
    assert_eq!(value_only.code, "transaction/tempid-not-an-entity");
    restarted.shutdown();
    assert_eq!(
        common::TestStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .unwrap()
            .basis_t(),
        basis_before_rejection
    );
}
