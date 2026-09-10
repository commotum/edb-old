mod common;
use atomic_core::*;
use std::time::Duration;

fn attribute() -> Attribute {
    Attribute::new(
        1000,
        Keyword::new("fulltext-fixture", "body"),
        ValueType::String,
        Cardinality::One,
    )
}
fn hex(bytes: &[u8]) -> String {
    bytes.iter().map(|byte| format!("{byte:02x}")).collect()
}

#[test]
fn old_attribute_request_and_program_output_bytes_stay_exact() {
    let ops = [TxOp::InstallAttribute(attribute())];
    let forms = ops.iter().cloned().map(TxForm::Op).collect::<Vec<_>>();
    assert_eq!(
        hex(&request_digest(&ops, 42, 1).unwrap()),
        "ba9918e553e1c7bb25a85fcd25452731195984eee05573bfc8b0f8e94c9ff82f"
    );
    assert_eq!(
        hex(&submission_request_digest(&forms, Some(1), Some(42)).unwrap()),
        "71a99b7092341eced50fde3b4b5cb821d869486d7fbe5f1d0e6c0ee019819a96"
    );
    assert_eq!(
        hex(&sha256(
            &encode_program_output(&ProgramOutput::Transaction(forms)).unwrap()
        )),
        "f3dd531804d980fed966cae6d3c0882b4825677ebd854a09712e6970bdb01354"
    );
    assert_ne!(
        request_digest(&ops, 42, 1).unwrap(),
        request_digest(&[TxOp::InstallAttribute(attribute().fulltext())], 42, 1).unwrap()
    );
}

#[test]
fn fulltext_schema_is_string_only_immutable_ordinary_information() {
    let before = Database::bootstrap().unwrap();
    let fulltext = attribute().fulltext();
    let report = before
        .with(&[TxOp::InstallAttribute(fulltext.clone())], 1)
        .unwrap();
    let native = before
        .database_value()
        .with(&[TxOp::InstallAttribute(fulltext.clone())], 1)
        .unwrap();
    assert_eq!(report.tx_data, native.tx_data);
    assert_eq!(native.db_after.schema().attribute(1000).unwrap(), &fulltext);
    assert_eq!(
        native.db_after.values(1000, DB_FULLTEXT as u32).unwrap(),
        vec![Value::Bool(true)]
    );
    let map = EntityMap {
        id: Some(EntityRef::Temp("text".into())),
        attributes: vec![
            (
                AttributeRef::Id(DB_IDENT as u32),
                MapValue::Value(Value::Keyword(attribute().ident).into()),
            ),
            (
                AttributeRef::Id(DB_VALUE_TYPE as u32),
                MapValue::Value(TxValue::Entity(EntityRef::Ident(Keyword::new(
                    "db.type", "string",
                )))),
            ),
            (
                AttributeRef::Id(DB_CARDINALITY as u32),
                MapValue::Value(TxValue::Entity(EntityRef::Ident(Keyword::new(
                    "db.cardinality",
                    "one",
                )))),
            ),
            (
                AttributeRef::Id(DB_FULLTEXT as u32),
                MapValue::Value(Value::Bool(true).into()),
            ),
        ],
    };
    let mapped = before
        .with_forms(&[TxForm::EntityMap(map.clone())], &TxFunctions::new(), 1)
        .unwrap();
    let mapped_native = before
        .database_value()
        .with_forms(&[TxForm::EntityMap(map)], 1)
        .unwrap();
    assert_eq!(mapped.tx_data, report.tx_data);
    assert_eq!(mapped_native.tx_data, report.tx_data);
    let mut invalid = fulltext.clone();
    invalid.value_type = ValueType::Long;
    assert_eq!(
        before
            .with(&[TxOp::InstallAttribute(invalid)], 1)
            .unwrap_err()
            .code,
        "schema/fulltext-must-be-string"
    );
    for op in [
        TxOp::AlterAttribute(attribute()),
        TxOp::Add {
            entity: EntityRef::Id(1000),
            attribute: DB_FULLTEXT as u32,
            value: Value::Bool(false).into(),
        },
        TxOp::Retract {
            entity: EntityRef::Id(1000),
            attribute: DB_FULLTEXT as u32,
            value: None,
        },
    ] {
        assert_eq!(
            report
                .db_after
                .with(std::slice::from_ref(&op), 2)
                .unwrap_err()
                .code,
            "schema/fulltext-immutable"
        );
        assert_eq!(
            native
                .db_after
                .with(std::slice::from_ref(&op), 2)
                .unwrap_err()
                .code,
            "schema/fulltext-immutable"
        );
    }
    let mut changed = fulltext;
    changed.indexed = true;
    let after = report
        .db_after
        .with(&[TxOp::AlterAttribute(changed)], 2)
        .unwrap();
    assert!(after.db_after.schema().attribute(1000).unwrap().fulltext);
    let plain = before
        .with(&[TxOp::InstallAttribute(attribute())], 1)
        .unwrap();
    assert_eq!(
        plain
            .db_after
            .with(&[TxOp::AlterAttribute(attribute().fulltext())], 2)
            .unwrap_err()
            .code,
        "schema/fulltext-immutable"
    );
}

#[test]
fn native_fulltext_schema_survives_exact_retry_indexing_and_reopen() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL required");
        return;
    };
    let fixture = common::PostgresFixture::new(&connection, "fulltext_schema");
    let connection = &fixture.connection;
    PostgresMigrator::connect(connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(connection).unwrap();
    store
        .create_database("fulltext-schema", Schema::new())
        .unwrap();
    let service = common::start_service(connection, "fulltext-schema");
    let request = TransactionRequest::new(
        "install-text",
        vec![TxOp::InstallAttribute(attribute().fulltext())],
    )
    .with_tx_instant(1);
    let report = service
        .client()
        .transact(request.clone(), Duration::from_secs(30))
        .unwrap();
    let repeated = service
        .client()
        .transact(request, Duration::from_secs(30))
        .unwrap();
    assert!(repeated.replayed);
    assert_eq!(report.tx_data, repeated.tx_data);
    service.shutdown();
    PostgresIndexer::connect(connection, "fulltext-schema")
        .unwrap()
        .consolidate()
        .unwrap();
    let peer = Peer::connect(connection, "fulltext-schema", 8).unwrap();
    assert!(
        peer.database_value()
            .schema()
            .attribute(1000)
            .unwrap()
            .fulltext
    );
    assert_eq!(
        peer.database_value()
            .values(1000, DB_FULLTEXT as u32)
            .unwrap(),
        vec![Value::Bool(true)]
    );
    assert!(
        store
            .recover("fulltext-schema")
            .unwrap()
            .schema()
            .attribute(1000)
            .unwrap()
            .fulltext
    );
}

#[test]
fn actual_pre_fulltext_binary_upgrades_without_reinterpreting_genesis() {
    let Ok(url) = std::env::var("ATOMIC_FULLTEXT_LEGACY_URL") else {
        eprintln!("SKIP: ATOMIC_FULLTEXT_LEGACY_URL required for one-time old-binary upgrade");
        return;
    };
    let name = "legacy-fulltext";
    let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
    let row = admin
        .query_one(
            "SELECT genesis,genesis_hash FROM atomic_databases WHERE database_id=$1",
            &[&name],
        )
        .unwrap();
    let genesis: Vec<u8> = row.get(0);
    let hash: Vec<u8> = row.get(1);
    assert_eq!(genesis.len(), 4818);
    assert_eq!(
        hex(&hash),
        "9074cee4e4bbf8358ef3fcd804de6ef218fe7e55e5710d5fd60a205d09ec243a"
    );
    assert_eq!(sha256(&genesis), hash.as_slice());
    // SQL migrations may add optional search storage; they must not install
    // vocabulary or rewrite the old canonical root as a side effect.
    PostgresMigrator::connect(&url).unwrap().migrate().unwrap();
    let initial = Peer::connect(&url, name, 8).unwrap().db();
    assert_eq!(initial.basis_t(), 0);
    assert!(initial.schema().attribute(DB_FULLTEXT as u32).is_err());
    let ops = fulltext_vocabulary_upgrade_ops();
    let preview = initial.with(&ops, 10).unwrap();
    let service = common::start_service(&url, name);
    let request = TransactionRequest::new("fulltext-vocabulary-upgrade/v1", ops)
        .comparing_basis(0)
        .with_tx_instant(10);
    let upgraded = service
        .client()
        .transact(request.clone(), Duration::from_secs(30))
        .unwrap();
    common::assert_same_information(&preview.db_after, &upgraded.db_after);
    assert!(initial.schema().attribute(DB_FULLTEXT as u32).is_err());
    assert_eq!(
        initial.collect_datoms(IndexOrder::Eavt).unwrap(),
        decode_genesis(&genesis).unwrap()
    );
    service.shutdown();
    PostgresIndexer::connect(&url, name)
        .unwrap()
        .consolidate()
        .unwrap();
    let service = common::start_service(&url, name);
    let replay = service
        .client()
        .transact(request, Duration::from_secs(30))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, upgraded.tx_hash);
    service.shutdown();
    let recovered = PostgresStore::connect(&url).unwrap().recover(name).unwrap();
    common::assert_same_information(&recovered, &upgraded.db_after);
    let row = admin
        .query_one(
            "SELECT genesis,genesis_hash FROM atomic_databases WHERE database_id=$1",
            &[&name],
        )
        .unwrap();
    assert_eq!(row.get::<_, Vec<u8>>(0), genesis);
    assert_eq!(row.get::<_, Vec<u8>>(1), hash);
    println!(
        "LEGACY_FULLTEXT_UPGRADE_OK old_genesis_bytes=4818 basis_t=1 exact_retry=true retained_old=true indexed_recovery=true"
    );
}
