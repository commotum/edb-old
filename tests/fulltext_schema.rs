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
#[test]
fn attribute_requests_and_program_outputs_are_canonical_and_include_fulltext() {
    fn encoded(attribute: Attribute) -> (Digest, Digest, Vec<u8>) {
        let ops = [TxOp::InstallAttribute(attribute)];
        let forms = ops.iter().cloned().map(TxForm::Op).collect::<Vec<_>>();
        (
            request_digest(&ops, 42, 1).unwrap(),
            submission_request_digest(&forms, Some(1), Some(42)).unwrap(),
            encode_program_output(&ProgramOutput::Transaction(forms)).unwrap(),
        )
    }
    let first = attribute().predicate("app/nonempty").predicate("app/short");
    let equivalent = attribute()
        .predicate("app/short")
        .predicate("app/nonempty")
        .predicate("app/short");
    assert_eq!(encoded(first.clone()), encoded(equivalent.clone()));
    assert_eq!(
        encoded(first.clone().fulltext()),
        encoded(equivalent.fulltext())
    );
    let plain = encoded(first.clone());
    let fulltext = encoded(first.fulltext());
    assert_ne!(plain.0, fulltext.0);
    assert_ne!(plain.1, fulltext.1);
    assert_ne!(plain.2, fulltext.2);
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
    common::install(connection).unwrap();
    let mut store = common::TestStore::connect(connection).unwrap();
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
    common::consolidate(connection, "fulltext-schema").unwrap();
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
