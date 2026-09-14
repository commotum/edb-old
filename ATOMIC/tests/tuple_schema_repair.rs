use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, Schema, TupleSpec, TxOp, Value, ValueType,
};
use num_bigint::BigInt;

#[test]
fn tuple_and_scalar_boundaries_use_source_string_and_signed_integer_units() {
    let mut schema = Schema::new();
    schema
        .install(tuple_attribute(
            1000,
            "text",
            TupleSpec::Homogeneous(ValueType::String),
        ))
        .unwrap();
    schema
        .install(tuple_attribute(
            1001,
            "integer",
            TupleSpec::Homogeneous(ValueType::BigInt),
        ))
        .unwrap();
    schema
        .install(attribute(
            1002,
            "scalar",
            ValueType::BigInt,
            Cardinality::One,
        ))
        .unwrap();
    let db = Database::new(schema).unwrap().database_value();
    let basis = db.basis_t();
    let attempt = |attribute, value: Value| {
        db.with(
            &[TxOp::Add {
                entity: EntityRef::Temp("value".into()),
                attribute,
                value: value.into(),
            }],
            1000,
        )
    };
    let tuple = |value| Value::Tuple(vec![Some(value), None]);
    for text in ["a".repeat(256), "\u{1f30a}".repeat(128)] {
        attempt(1000, tuple(Value::String(text))).unwrap();
    }
    for text in ["a".repeat(257), "\u{1f30a}".repeat(129)] {
        assert_eq!(
            attempt(1000, tuple(Value::String(text))).unwrap_err().code,
            "transaction/tuple-string-too-large"
        );
    }
    // Two's-complement bit length excludes the sign bit, not the magnitude's
    // top bit for all negatives. The accepted interval is [-2^n, 2^n - 1].
    for (attribute, limit, code) in [
        (1001, 256, "transaction/tuple-bigint-too-large"),
        (1002, 8192, "transaction/bigint-too-large"),
    ] {
        let power: BigInt = BigInt::from(1) << limit;
        let value = |n| {
            if attribute == 1001 {
                tuple(Value::BigInt(n))
            } else {
                Value::BigInt(n)
            }
        };
        for n in [BigInt::from(0), BigInt::from(-1), -&power, &power - 1] {
            attempt(attribute, value(n)).unwrap();
        }
        for n in [power.clone(), -&power - 1] {
            assert_eq!(attempt(attribute, value(n)).unwrap_err().code, code);
        }
    }
    assert_eq!(db.basis_t(), basis, "all attempts are speculative");
}

fn attribute(id: u32, name: &str, value_type: ValueType, cardinality: Cardinality) -> Attribute {
    Attribute::new(
        id,
        Keyword::new("tuple-repair", name),
        value_type,
        cardinality,
    )
}

fn tuple_attribute(id: u32, name: &str, spec: TupleSpec) -> Attribute {
    attribute(id, name, ValueType::Tuple, Cardinality::One).tuple(spec)
}

#[test]
fn homogeneous_tuple_type_is_limited_to_datomic_scalar_types() {
    let permitted = [
        ValueType::BigDec,
        ValueType::BigInt,
        ValueType::Boolean,
        ValueType::Double,
        ValueType::Instant,
        ValueType::Keyword,
        ValueType::Long,
        ValueType::Ref,
        ValueType::String,
        ValueType::Symbol,
        ValueType::Uuid,
        ValueType::Uri,
    ];

    for (offset, value_type) in permitted.into_iter().enumerate() {
        let mut schema = Schema::new();
        schema
            .install(tuple_attribute(
                10 + offset as u32,
                "permitted",
                TupleSpec::Homogeneous(value_type),
            ))
            .unwrap();
    }

    for (offset, value_type) in [ValueType::Bytes, ValueType::Float, ValueType::Tuple]
        .into_iter()
        .enumerate()
    {
        let mut schema = Schema::new();
        let error = schema
            .install(tuple_attribute(
                100 + offset as u32,
                "forbidden",
                TupleSpec::Homogeneous(value_type),
            ))
            .unwrap_err();
        assert_eq!(error.code, "schema/invalid-tuple-element-type");
    }
}

#[test]
fn heterogeneous_tuple_rejects_any_non_tuple_scalar_slot_type() {
    let mut schema = Schema::new();
    let error = schema
        .install(tuple_attribute(
            10,
            "mixed",
            TupleSpec::Heterogeneous(vec![ValueType::Long, ValueType::Float]),
        ))
        .unwrap_err();

    assert_eq!(error.code, "schema/invalid-tuple-element-type");
}

#[test]
fn composite_attribute_itself_must_be_cardinality_one() {
    let mut schema = Schema::new();
    let composite = attribute(10, "parts", ValueType::Tuple, Cardinality::Many)
        .tuple(TupleSpec::Composite(vec![20, 21]));
    let error = schema.install(composite).unwrap_err();

    assert_eq!(error.code, "schema/composite-must-be-cardinality-one");
}

#[test]
fn composite_constituents_are_checked_against_the_complete_successor_schema() {
    let mut successor = Schema::new();

    // Installing the composite first is intentional: operation ordering must
    // not affect validation of the complete proposed schema.
    successor
        .install(tuple_attribute(
            10,
            "parts",
            TupleSpec::Composite(vec![20, 21]),
        ))
        .unwrap();
    assert_eq!(
        successor.validate_tuple_definitions().unwrap_err().code,
        "schema/invalid-tuple-attributes"
    );

    successor
        .install(attribute(20, "part-a", ValueType::Long, Cardinality::One))
        .unwrap();
    successor
        .install(attribute(21, "part-b", ValueType::String, Cardinality::One))
        .unwrap();

    successor.validate_tuple_definitions().unwrap();
}

#[test]
fn composite_constituents_must_be_cardinality_one_tuple_scalars() {
    for (constituent_type, cardinality) in [
        (ValueType::String, Cardinality::Many),
        (ValueType::Float, Cardinality::One),
        (ValueType::Bytes, Cardinality::One),
    ] {
        let mut schema = Schema::new();
        schema
            .install(attribute(20, "valid", ValueType::Long, Cardinality::One))
            .unwrap();
        schema
            .install(attribute(21, "invalid", constituent_type, cardinality))
            .unwrap();
        schema
            .install(tuple_attribute(
                10,
                "parts",
                TupleSpec::Composite(vec![20, 21]),
            ))
            .unwrap();

        assert_eq!(
            schema.validate_tuple_definitions().unwrap_err().code,
            "schema/invalid-tuple-attributes"
        );
    }
}

#[test]
fn composite_cannot_refer_to_itself() {
    let mut schema = Schema::new();
    schema
        .install(attribute(20, "part", ValueType::Long, Cardinality::One))
        .unwrap();
    schema
        .install(tuple_attribute(
            10,
            "self-reference",
            TupleSpec::Composite(vec![10, 20]),
        ))
        .unwrap();

    assert_eq!(
        schema.validate_tuple_definitions().unwrap_err().code,
        "schema/invalid-tuple-attributes"
    );
}

#[test]
fn duplicate_constituents_follow_the_1_0_7705_validation_rule() {
    let mut schema = Schema::new();
    schema
        .install(attribute(20, "part", ValueType::Long, Cardinality::One))
        .unwrap();
    schema
        .install(tuple_attribute(
            10,
            "duplicate-part",
            TupleSpec::Composite(vec![20, 20]),
        ))
        .unwrap();

    // `tuple-install-errors` applies `tuple-attr-value-type` to every slot; it
    // does not impose a distinctness condition on :db/tupleAttrs.
    schema.validate_tuple_definitions().unwrap();
}

#[test]
fn composite_cannot_be_discontinued_during_installation() {
    let mut schema = Schema::new();
    let mut composite = tuple_attribute(10, "parts", TupleSpec::Composite(vec![20, 21]));
    composite.tuple_discontinued = true;
    schema.install(composite).unwrap();

    assert_eq!(
        schema.validate_tuple_installations(&[10]).unwrap_err().code,
        "schema/cannot-discontinue-at-install"
    );
}

#[test]
fn installed_tuple_definition_remains_immutable() {
    const PART_A: u32 = 1_000;
    const PART_B: u32 = 1_001;
    const COMPOSITE: u32 = 1_002;

    let mut schema = Schema::new();
    schema
        .install(attribute(
            PART_A,
            "part-a",
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(attribute(
            PART_B,
            "part-b",
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(tuple_attribute(
            COMPOSITE,
            "parts",
            TupleSpec::Composite(vec![PART_A, PART_B]),
        ))
        .unwrap();
    schema.validate_tuple_definitions().unwrap();

    let db = Database::new(schema).unwrap();
    let mut changed = db.schema().attribute(COMPOSITE).unwrap().clone();
    changed.tuple = Some(TupleSpec::Heterogeneous(vec![
        ValueType::Long,
        ValueType::String,
    ]));

    let error = db
        .with(&[TxOp::AlterAttribute(changed)], 1_000)
        .unwrap_err();
    assert_eq!(error.code, "schema/tuple-definition-immutable");
}
