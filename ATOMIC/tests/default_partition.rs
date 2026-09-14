//! Execution defaults are fallbacks, not forced per-tempid directives or
//! durable request data. Saved outcomes precede current configuration lookup.
mod common;

use atomic_core::{
    Attribute, CapacityLimits, Cardinality, Connection, DB_IDENT, DB_PART_DB, Database,
    DatabaseValue, EntityRef, IndexOrder, Keyword, Schema, TransactionDefaults, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, USER_PARTITION, Unique, Value, ValueType,
    eid_to_eidx, eid_to_part, implicit_part, t_to_tx,
};
use std::time::{Duration, Instant};

const KEY: u32 = 1000;
const NUMBER: u32 = 1001;
const CHILDREN: u32 = 1002;
const INSTALL: &str = r#"[{:db/id "orders-part" :db/ident :part/orders :db.install/_partition :db.part/db}
 {:db/id "customers-part" :db/ident :part/customers :db.install/_partition :db.part/db}]"#;
const ALLOCATE: &str = r#"[{:db/id "order" :item/key "order" :item/n 1 :item/children [{:item/n 2}]}
 {:item/n 3}
 {:db/id "forced" :item/n 4}
 {:db/id "matched" :item/n 5}
 {:db/id "reserved-match" :item/n 6}
 {:db/id "tx-meta" :item/n 7}
 {:db/id "new-schema" :db/ident :item/later :db/valueType :db.type/string :db/cardinality :db.cardinality/one}
 {:db/force-partition {"forced" :db.part/user "tx-meta" :db.part/tx "new-schema" :db.part/user}}
 {:db/match-partition {"matched" "forced" "reserved-match" :db.part/tx}}]"#;

fn defaults(name: &str) -> TransactionDefaults {
    TransactionDefaults::default().with_default_partition(Keyword::new("part", name))
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                KEY,
                Keyword::new("item", "key"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            NUMBER,
            Keyword::new("item", "n"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                CHILDREN,
                Keyword::new("item", "children"),
                ValueType::Ref,
                Cardinality::Many,
            )
            .component(),
        )
        .unwrap();
    schema
}

fn add(name: &str, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(name.into()),
        attribute,
        value: value.into(),
    }
}

fn installed() -> Database {
    Database::new(schema())
        .unwrap()
        .with_edn(INSTALL, 1000)
        .unwrap()
        .db_after
}

fn assert_allocations(database: &DatabaseValue, partition: u32) {
    let values = database.collect_datoms(IndexOrder::Eavt).unwrap();
    for (number, expected) in [
        (1, partition),
        (2, partition),
        (3, partition),
        (4, USER_PARTITION),
        (5, USER_PARTITION),
        (6, partition),
        (7, atomic_core::TX_PARTITION),
    ] {
        let matches: Vec<_> = values
            .iter()
            .filter(|d| d.attribute == NUMBER && d.value == Value::Long(number))
            .collect();
        assert_eq!(matches.len(), 1, "one entity for n={number}");
        assert_eq!(
            eid_to_part(matches[0].entity).unwrap(),
            expected,
            "n={number}"
        );
    }
    let new_schema = database.entid(&Keyword::new("item", "later")).unwrap();
    assert_eq!(eid_to_part(new_schema).unwrap(), atomic_core::DB_PARTITION);
    assert!(database.schema().attribute(new_schema as u32).is_ok());
}

#[test]
fn defaults_cover_anonymous_nested_and_named_tempids_without_overriding_explicit_policy() {
    let base = installed();
    let partition =
        eid_to_eidx(base.entid(&Keyword::new("part", "orders")).unwrap()).unwrap() as u32;
    let eager = base
        .with_edn_with_defaults(ALLOCATE, 2000, &defaults("orders"))
        .unwrap();
    let native = base
        .database_value()
        .with_edn_with_defaults(ALLOCATE, 2000, &defaults("orders"))
        .unwrap();
    common::assert_same_information(&eager.db_after, &native.db_after);
    assert_allocations(&native.db_after, partition);
    assert_eq!(
        native.tempids["tx-meta"],
        t_to_tx(native.db_after.basis_t()).unwrap()
    );
    assert!(
        base.database_value()
            .collect_datoms(IndexOrder::Eavt)
            .unwrap()
            .iter()
            .all(|d| d.attribute != NUMBER)
    );
    let unchanged_api = base
        .with(&[add("old", NUMBER, Value::Long(8))], 2000)
        .unwrap();
    assert_eq!(
        eid_to_part(unchanged_api.tempids["old"]).unwrap(),
        USER_PARTITION
    );
}

#[test]
fn default_fallback_does_not_conflict_with_unified_tempids_or_existing_identity() {
    let base = installed();
    let implicit = implicit_part(500).unwrap();
    let ops = [
        add("ordinary", KEY, Value::String("shared".into())),
        add("forced", KEY, Value::String("shared".into())),
        TxOp::ForcePartition {
            tempid: "forced".into(),
            partition: EntityRef::Id(implicit),
        },
    ];
    let seed = base.with_defaults(&ops, 2000, &defaults("orders")).unwrap();
    assert_eq!(seed.tempids["ordinary"], seed.tempids["forced"]);
    assert_eq!(
        atomic_core::partition_eid(seed.tempids["ordinary"]).unwrap(),
        implicit
    );
    let preview = base
        .database_value()
        .with_defaults(&ops, 2000, &defaults("orders"))
        .unwrap();
    common::assert_same_information(&seed.db_after, &preview.db_after);
    let upsert_ops = [
        add("same", KEY, Value::String("shared".into())),
        add("same", NUMBER, Value::Long(9)),
    ];
    let upsert = seed
        .db_after
        .with_defaults(&upsert_ops, 3000, &defaults("customers"))
        .unwrap();
    assert_eq!(upsert.tempids["same"], seed.tempids["ordinary"]);
    common::assert_same_information(
        &upsert.db_after,
        &seed
            .db_after
            .database_value()
            .with_defaults(&upsert_ops, 3000, &defaults("customers"))
            .unwrap()
            .db_after,
    );
}

#[test]
fn missing_nonpartition_and_reserved_defaults_fail_explicitly_without_affecting_old_apis() {
    let base = installed();
    let ops = [add("new", NUMBER, Value::Long(8))];
    for (name, expected) in [
        (
            Keyword::new("part", "missing"),
            "transaction/default-partition-not-found",
        ),
        (
            Keyword::new("item", "n"),
            "transaction/invalid-default-partition",
        ),
        (
            Keyword::new("db.part", "db"),
            "transaction/invalid-default-partition",
        ),
        (
            Keyword::new("db.part", "tx"),
            "transaction/invalid-default-partition",
        ),
    ] {
        let defaults = TransactionDefaults::default().with_default_partition(name);
        assert_eq!(
            base.with_defaults(&ops, 2000, &defaults).unwrap_err().code,
            expected
        );
        assert_eq!(
            base.database_value()
                .with_defaults(&ops, 2000, &defaults)
                .unwrap_err()
                .code,
            expected
        );
    }
    assert!(base.with(&ops, 2000).is_ok());
    assert_eq!(base.entid(&Keyword::new("db.part", "db")), Some(DB_PART_DB));
}

fn service(connection: &str, defaults: TransactionDefaults) -> TransactionService {
    TransactionService::start_with_defaults(
        TransactionServiceConfig {
            connection: atomic_core::PostgresConnectionConfig::plaintext(connection),
            database_id: "defaults".into(),
            holder_id: format!("defaults-{}", std::process::id()),
            lease_duration: Duration::from_secs(5),
            renew_interval: Duration::from_millis(100),
            queue_capacity: 32,
            capacity_limits: CapacityLimits::default(),
        },
        defaults,
    )
    .unwrap()
}

#[test]
fn postgres_default_change_restart_and_invalid_name_never_reinterpret_a_saved_receipt() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED PostgreSQL default partition: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "default_partition");
    common::install(&fixture.connection).unwrap();
    let mut store = common::TestStore::connect(&fixture.connection).unwrap();
    let created = store.create_database("defaults", schema()).unwrap();
    let bootstrap = service(&fixture.connection, TransactionDefaults::default());
    let install = bootstrap
        .client()
        .transact(
            TransactionRequest::from_edn("install", INSTALL)
                .unwrap()
                .comparing_basis(created.basis_t())
                .with_tx_instant(1000),
            Duration::from_secs(10),
        )
        .unwrap();
    bootstrap.shutdown();
    common::consolidate(&fixture.connection, "defaults").unwrap();
    let orders = install
        .db_after
        .entid(&Keyword::new("part", "orders"))
        .unwrap();
    let customers = install
        .db_after
        .entid(&Keyword::new("part", "customers"))
        .unwrap();
    let start = Instant::now();
    let writer = service(&fixture.connection, defaults("orders"));
    let request = TransactionRequest::from_edn("family", ALLOCATE)
        .unwrap()
        .comparing_basis(install.basis_t)
        .with_tx_instant(2000);
    let preview = install
        .db_after
        .with_edn_with_defaults(ALLOCATE, 2000, &defaults("orders"))
        .unwrap();
    let first = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    common::assert_same_information(&first.db_after, &preview.db_after);
    assert_allocations(&first.db_after, eid_to_eidx(orders).unwrap() as u32);
    writer.shutdown();
    common::consolidate(&fixture.connection, "defaults").unwrap();

    let writer = service(&fixture.connection, defaults("customers"));
    let replay = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(replay.tempids, first.tempids);
    assert_eq!(replay.tx_data, first.tx_data);
    common::assert_same_information(&replay.db_after, &first.db_after);
    let next = writer
        .client()
        .transact(
            TransactionRequest::from_edn("next", "[{:db/id \"next\" :item/n 8}]")
                .unwrap()
                .comparing_basis(first.basis_t)
                .with_tx_instant(3000),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_eq!(
        eid_to_part(next.tempids["next"]).unwrap(),
        eid_to_eidx(customers).unwrap() as u32
    );
    let upsert = writer
        .client()
        .transact(
            TransactionRequest::from_edn(
                "upsert",
                "[{:db/id \"same\" :item/key \"order\" :item/n 9}]",
            )
            .unwrap()
            .comparing_basis(next.basis_t)
            .with_tx_instant(4000),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_eq!(upsert.tempids["same"], first.tempids["order"]);
    assert_eq!(
        eid_to_part(upsert.tempids["same"]).unwrap(),
        eid_to_eidx(orders).unwrap() as u32
    );
    // Renaming retains the old ident as an alias. It stays a valid default
    // until deliberately repurposed to a nonpartition entity.
    let renamed = writer
        .client()
        .transact(
            TransactionRequest::new(
                "rename",
                vec![TxOp::Add {
                    entity: EntityRef::Id(customers),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(Keyword::new("part", "renamed")).into(),
                }],
            )
            .comparing_basis(upsert.basis_t)
            .with_tx_instant(5000),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_eq!(
        renamed.db_after.entid(&Keyword::new("part", "customers")),
        Some(customers)
    );
    let repurposed = writer
        .client()
        .transact(
            TransactionRequest::new(
                "repurpose",
                vec![add(
                    "not-a-partition",
                    DB_IDENT as u32,
                    Value::Keyword(Keyword::new("part", "customers")),
                )],
            )
            .comparing_basis(renamed.basis_t)
            .with_tx_instant(6000),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_ne!(
        repurposed
            .db_after
            .entid(&Keyword::new("part", "customers")),
        Some(customers)
    );
    writer.shutdown();
    common::consolidate(&fixture.connection, "defaults").unwrap();
    let writer = service(&fixture.connection, defaults("customers"));
    let replay = writer
        .client()
        .transact(request, Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(replay.tx_data, first.tx_data);
    let failed = writer
        .client()
        .transact(
            TransactionRequest::new(
                "missing-default",
                vec![add("fresh", NUMBER, Value::Long(10))],
            )
            .comparing_basis(repurposed.basis_t)
            .with_tx_instant(7000),
            Duration::from_secs(10),
        )
        .unwrap_err();
    assert_eq!(failed.code, "transaction/invalid-default-partition");
    writer.shutdown();
    let writer = service(&fixture.connection, defaults("missing"));
    let original = TransactionRequest::from_edn("family", ALLOCATE)
        .unwrap()
        .comparing_basis(install.basis_t)
        .with_tx_instant(2000);
    let replay = writer
        .client()
        .transact(original, Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let failed = writer
        .client()
        .transact(
            TransactionRequest::new(
                "missing-config",
                vec![add("fresh", NUMBER, Value::Long(10))],
            )
            .comparing_basis(repurposed.basis_t)
            .with_tx_instant(7000),
            Duration::from_secs(10),
        )
        .unwrap_err();
    assert_eq!(failed.code, "transaction/default-partition-not-found");
    writer.shutdown();
    let reopened = Connection::connect(&fixture.connection, "defaults", 8).unwrap();
    assert_eq!(reopened.db().basis_t(), repurposed.basis_t);
    common::assert_same_information(&repurposed.db_after, &store.recover("defaults").unwrap());
    eprintln!(
        "default partition complete configured workflows/restarts/retries/recovery: {:?}",
        start.elapsed()
    );
}
