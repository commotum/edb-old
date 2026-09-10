//! Generated branch/retain/discard schedules over an authenticated native base.
//! Pure eager values remain the information oracle, never a source of writes.
use atomic_core::{
    Attribute, Cardinality, Database, DatabaseValue, EntityRef, IndexOrder, Keyword, Peer,
    PostgresIndexer, PostgresMigrator, PostgresStore, Schema, TxOp, Value, ValueType,
};
use std::collections::BTreeMap;
use std::time::{Duration, Instant};

mod common;

fn schema() -> Schema {
    let mut schema = Schema::new();
    for (id, name, cardinality) in [
        (1000, "one", Cardinality::One),
        (1001, "many", Cardinality::Many),
    ] {
        let mut attribute =
            Attribute::new(id, Keyword::new("item", name), ValueType::Long, cardinality);
        attribute.indexed = true;
        schema.install(attribute).unwrap();
    }
    schema
}

fn compare(actual: &DatabaseValue, expected: &Database) {
    common::assert_same_information(actual, expected);
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            actual.clone().history().collect_datoms(order).unwrap(),
            expected
                .database_value()
                .history()
                .collect_datoms(order)
                .unwrap()
        );
    }
}

fn exercise(mut expected: Database, mut actual: DatabaseValue) {
    let original = (actual.clone(), expected.clone());
    let mut retained = Vec::new();
    let mut entities = BTreeMap::new();
    let mut seed = 0x6aed_250c_f338_4b12_u64;
    let mut construction = Duration::ZERO;
    for step in 1..=64 {
        seed ^= seed << 13;
        seed ^= seed >> 7;
        seed ^= seed << 17;
        let slot = (seed % 12) as usize;
        let attribute = 1000 + ((seed >> 8) & 1) as u32;
        let entity = entities
            .get(&slot)
            .copied()
            .map(EntityRef::Id)
            .unwrap_or_else(|| EntityRef::Temp(format!("slot-{slot}")));
        let operations = if seed & 7 == 0 && entities.contains_key(&slot) {
            vec![TxOp::Retract {
                entity,
                attribute,
                value: None,
            }]
        } else {
            vec![TxOp::Add {
                entity,
                attribute,
                value: Value::Long((seed % 17) as i64).into(),
            }]
        };
        let eager_report = expected.with(&operations, step).unwrap();
        let started = Instant::now();
        let report = actual.with(&operations, step).unwrap();
        construction += started.elapsed();
        assert_eq!(report.tempids, eager_report.tempids);
        assert_eq!(report.tx_data, eager_report.tx_data);
        if let Some(entity) = report.tempids.get(&format!("slot-{slot}")) {
            entities.insert(slot, *entity);
        }
        expected = eager_report.db_after;
        actual = report.db_after;
        let selected = entities[&slot];
        assert_eq!(
            actual.values(selected, attribute).unwrap(),
            expected
                .database_value()
                .values(selected, attribute)
                .unwrap()
        );
        if step % 8 == 0 {
            compare(&actual, &expected);
            retained.push((actual.clone(), expected.clone()));
            // Siblings deliberately allocate from the same parent frontier.
            // Their entity IDs may coincide, but ownership/results must not.
            let mut siblings = Vec::new();
            for branch in 0..8 {
                let intent = [TxOp::Add {
                    entity: EntityRef::Temp("sibling".into()),
                    attribute: 1000,
                    value: Value::Long(100 + branch).into(),
                }];
                let before = Instant::now();
                let fork = actual.with(&intent, step + 1).unwrap();
                construction += before.elapsed();
                let pure = expected.with(&intent, step + 1).unwrap();
                assert_eq!(fork.tempids, pure.tempids);
                compare(&fork.db_after, &pure.db_after);
                siblings.push(fork.db_after);
            }
            drop(siblings);
            compare(&actual, &expected);
            eprintln!(
                "generated speculative seed=0x6aed250cf3384b12 depth={step} sibling_width=8 native_with_cumulative_us={} retained_ancestors={}",
                construction.as_micros(),
                retained.len()
            );
        }
    }
    for (actual, expected) in retained {
        compare(&actual, &expected);
        let cutoff = actual.basis_t() / 2;
        common::assert_same_information(
            &actual.clone().as_of(cutoff),
            &expected.database_value().as_of(cutoff),
        );
        common::assert_same_information(
            &actual.since(cutoff),
            &expected.database_value().since(cutoff),
        );
    }
    compare(&original.0, &original.1);
}

#[test]
fn generated_branches_share_values_without_cross_branch_mutation() {
    let eager = Database::new(schema()).unwrap();
    exercise(eager.clone(), eager.database_value());
}

#[test]
fn generated_native_branches_preserve_exact_information_without_publication() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required for native branch sharing");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "shared_speculation");
    let connection = &fixture.connection;
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    let database = format!("shared-speculation-{}", fixture.schema);
    let mut store = PostgresStore::connect(connection).unwrap();
    let eager = store.create_database(&database, schema()).unwrap();
    PostgresIndexer::connect(connection, &database)
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(connection, &database, 32).unwrap();
    let before = peer.database_value();
    exercise(eager, before.clone());
    assert_eq!(peer.sync().unwrap().basis_t(), before.basis_t());
    assert_eq!(
        store.recover(&database).unwrap().basis_t(),
        before.basis_t()
    );
    assert_eq!(peer.load_stats().compatibility_materializations, 0);
}
