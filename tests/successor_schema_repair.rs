use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, Schema, TxOp, TxValue, Unique, Value,
    ValueType,
};

const TAG: u32 = 1_000;
const CODE: u32 = 1_001;

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            TAG,
            Keyword::new("item", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    let mut code = Attribute::new(
        CODE,
        Keyword::new("item", "code"),
        ValueType::String,
        Cardinality::One,
    );
    code.indexed = true;
    schema.install(code).unwrap();
    schema
}

fn add(entity: &str, attribute: u32, value: &str) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(entity.into()),
        attribute,
        value: TxValue::Scalar(Value::String(value.into())),
    }
}

#[test]
fn successor_cardinality_rejects_conflicting_same_transaction_data() {
    let db = Database::new(schema()).unwrap();
    let mut tag = db.schema().attribute(TAG).unwrap().clone();
    tag.cardinality = Cardinality::One;

    let error = db
        .with(
            &[
                add("item", TAG, "red"),
                add("item", TAG, "blue"),
                TxOp::AlterAttribute(tag),
            ],
            1_000,
        )
        .expect_err("the successor schema must validate the complete proposed database");

    assert!(
        error.code == "schema/cardinality-change-conflict"
            || error.code == "transaction/cardinality-one-conflict",
        "unexpected error: {error:?}"
    );
}

#[test]
fn successor_uniqueness_rejects_duplicates_added_in_the_same_transaction() {
    let db = Database::new(schema()).unwrap();
    let mut code = db.schema().attribute(CODE).unwrap().clone();
    code.unique = Some(Unique::Value);
    code.indexed = true;

    let error = db
        .with(
            &[
                add("left", CODE, "duplicate"),
                add("right", CODE, "duplicate"),
                TxOp::AlterAttribute(code),
            ],
            1_000,
        )
        .expect_err("the successor uniqueness constraint must include same-transaction facts");

    assert!(
        error.code == "schema/uniqueness-change-conflict"
            || error.code == "schema/unique-change-conflict"
            || error.code == "transaction/unique-conflict",
        "unexpected error: {error:?}"
    );
}

#[test]
fn every_accepted_schema_transition_is_immediately_invariant_valid() {
    let db = Database::new(schema()).unwrap();
    let mut code = db.schema().attribute(CODE).unwrap().clone();
    code.unique = Some(Unique::Identity);
    code.indexed = true;

    let report = db
        .with(
            &[add("item", CODE, "only"), TxOp::AlterAttribute(code)],
            1_000,
        )
        .unwrap();

    report.db_after.validate_invariants().unwrap();
}
