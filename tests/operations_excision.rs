use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, ExcisionFault, ExcisionSpec, ExcisionTarget,
    IndexOrder, Keyword, Peer, PortableBackup, PostgresIndexer, PostgresOperator, PostgresStore,
    Schema, TxOp, TxValue, USER_PARTITION, Value, ValueType, View, make_eid, t_to_tx,
};
use postgres::{Client, NoTls};
use std::fs;
use std::path::PathBuf;
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const DB_TX_INSTANT: u32 = 50;
const SECRET: u32 = 1_000;
const RETAINED: u32 = 1_001;
const CHILD: u32 = 1_002;
const RELATED: u32 = 1_003;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn user(eidx: u64) -> u64 {
    make_eid(USER_PARTITION, eidx).unwrap()
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

fn backup_directory() -> PathBuf {
    std::env::temp_dir().join(unique("atomic_excision_backup"))
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            SECRET,
            Keyword::new("person", "secret"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            RETAINED,
            Keyword::new("person", "retained"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(
            Attribute::new(
                CHILD,
                Keyword::new("person", "child"),
                ValueType::Ref,
                Cardinality::Many,
            )
            .component(),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            RELATED,
            Keyword::new("person", "related"),
            ValueType::Ref,
            Cardinality::Many,
        ))
        .unwrap();
    schema
}

fn add(entity: u64, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Id(entity),
        attribute,
        value: TxValue::Scalar(value),
    }
}

fn contains_entity(database: &atomic_core::Database, entity: u64) -> bool {
    database
        .datoms(View::History, IndexOrder::Eavt)
        .iter()
        .any(|datom| {
            datom.entity == entity || matches!(datom.value, Value::Ref(value) if value == entity)
        })
}

#[test]
fn entity_excision_is_atomic_recursive_audited_and_invalidates_peers() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("excise_entity");
    let directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let seeded = common::transact(
        &service,
        "seed",
        created.basis_t(),
        &[
            add(user(42), SECRET, Value::String("root-secret".into())),
            add(user(42), RETAINED, Value::Long(7)),
            add(user(42), CHILD, Value::Ref(user(43))),
            add(user(43), SECRET, Value::String("child-secret".into())),
            add(user(44), RELATED, Value::Ref(user(42))),
            add(user(44), RETAINED, Value::Long(9)),
        ],
        1_000,
    );
    service.shutdown();
    let mut indexer = PostgresIndexer::connect(&connection, &database_id).unwrap();
    indexer.consolidate().unwrap();
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    let old_snapshot = peer.db();
    assert!(contains_entity(&old_snapshot, user(42)));

    let mut backups = PortableBackup::connect(&connection).unwrap();
    backups.backup_database(&database_id, &directory).unwrap();
    let verified = PortableBackup::verify_backup(&directory, seeded.basis_t, true).unwrap();
    let spec = ExcisionSpec {
        excision_id: "forget-person-1000".into(),
        target: ExcisionTarget::Entity {
            entity: user(42),
            attributes: Vec::new(),
        },
        before_t: None,
    };
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let error = operator
        .excise_database_with_fault(&database_id, &spec, &verified, ExcisionFault::AfterRewrite)
        .unwrap_err();
    assert_eq!(
        (error.category, error.code),
        (ErrorCategory::Interrupted, "excision/injected-fault")
    );
    assert!(
        PostgresStore::connect(&connection)
            .unwrap()
            .recover(&database_id)
            .map(|database| contains_entity(&database, user(42)))
            .unwrap()
    );
    let mut raw = Client::connect(&connection, NoTls).unwrap();
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_excisions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        1
    );

    let receipt = operator
        .excise_database(&database_id, &spec, &verified)
        .unwrap();
    assert!(!receipt.replayed);
    assert!(receipt.removed_datoms >= 5);
    assert_ne!(receipt.old_head_hash, receipt.new_head_hash);
    assert_eq!(receipt.generation, 1);
    assert!(contains_entity(&old_snapshot, user(42)));
    let refreshed = peer.sync().unwrap();
    assert_eq!(peer.excision_generation(), 1);
    assert!(!contains_entity(&refreshed, user(42)));
    assert!(!contains_entity(&refreshed, user(43)));
    assert_eq!(refreshed.values(user(44), RETAINED), vec![&Value::Long(9)]);
    assert_eq!(
        raw.query_one(
            "SELECT count(*) FROM atomic_index_manifests WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get::<_, i64>(0),
        0
    );
    for secret in ["root-secret", "child-secret"] {
        assert_eq!(
            raw.query_one(
                "SELECT count(*) FROM atomic_transactions \
                 WHERE database_id = $1 AND position(convert_to($2, 'UTF8') in payload) > 0",
                &[&database_id, &secret],
            )
            .unwrap()
            .get::<_, i64>(0),
            0
        );
        assert_eq!(
            raw.query_one(
                "SELECT count(*) FROM atomic_index_segments \
                 WHERE position(convert_to($1, 'UTF8') in payload) > 0",
                &[&secret],
            )
            .unwrap()
            .get::<_, i64>(0),
            0
        );
    }
    let recovered = PostgresStore::connect(&connection)
        .unwrap()
        .recover(&database_id)
        .unwrap();
    assert!(!contains_entity(&recovered, user(42)));
    assert!(
        PostgresOperator::connect(&connection)
            .unwrap()
            .inspect_database(&database_id, true)
            .unwrap()
            .healthy()
    );
    assert!(
        operator
            .excise_database(&database_id, &spec, &verified)
            .unwrap()
            .replayed
    );
    fs::remove_dir_all(directory).unwrap();
}

#[test]
fn attribute_cutoff_backup_binding_and_protected_facts_are_enforced() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("excise_attribute");
    let other_database = unique("excise_other");
    let directory = backup_directory();
    let other_directory = backup_directory();
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema()).unwrap();
    let service = common::start_service(&connection, &database_id);
    let old = common::transact(
        &service,
        "old",
        created.basis_t(),
        &[add(user(45), SECRET, Value::String("old-secret".into()))],
        1_000,
    );
    let new = common::transact(
        &service,
        "new",
        old.basis_t,
        &[add(user(45), SECRET, Value::String("new-retained".into()))],
        2_000,
    );
    service.shutdown();
    let other = store.create_database(&other_database, schema()).unwrap();
    let mut backups = PortableBackup::connect(&connection).unwrap();
    backups.backup_database(&database_id, &directory).unwrap();
    backups
        .backup_database(&other_database, &other_directory)
        .unwrap();
    let verified = PortableBackup::verify_backup(&directory, new.basis_t, true).unwrap();
    let wrong = PortableBackup::verify_backup(&other_directory, other.basis_t(), true).unwrap();
    let spec = ExcisionSpec {
        excision_id: "retention-window".into(),
        target: ExcisionTarget::Attribute(SECRET),
        before_t: Some(t_to_tx(new.basis_t).unwrap()),
    };
    let mut operator = PostgresOperator::connect(&connection).unwrap();
    let error = operator
        .excise_database(&database_id, &spec, &wrong)
        .unwrap_err();
    assert_eq!(error.code, "excision/backup-does-not-cover-head");
    for target in [
        ExcisionTarget::Attribute(DB_TX_INSTANT),
        ExcisionTarget::Entity {
            entity: 1,
            attributes: Vec::new(),
        },
    ] {
        let protected = ExcisionSpec {
            excision_id: unique("protected"),
            target,
            before_t: None,
        };
        let error = operator
            .excise_database(&database_id, &protected, &verified)
            .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Forbidden);
    }
    let receipt = operator
        .excise_database(&database_id, &spec, &verified)
        .unwrap();
    assert_eq!(receipt.removed_datoms, 1);
    let recovered = store.recover(&database_id).unwrap();
    assert_eq!(
        recovered.values(user(45), SECRET),
        vec![&Value::String("new-retained".into())]
    );
    assert!(
        !recovered
            .datoms(View::History, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.value == Value::String("old-secret".into()))
    );

    fs::remove_dir_all(directory).unwrap();
    fs::remove_dir_all(other_directory).unwrap();
}
