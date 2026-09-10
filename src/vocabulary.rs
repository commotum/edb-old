//! Native system vocabulary and canonical genesis information.
//!
//! The numeric IDs mirror Datomic Pro 1.0.7705 where they are fixed or can be
//! recovered deterministically from the bootstrap transactions.  The native
//! kernel replaces JVM function objects with immutable native program hashes
//! while schema, including native fulltext policy, is represented by ordinary datoms, not by this
//! module's Rust descriptors.  The descriptors are only a compact way to
//! construct and verify those canonical datoms.

use crate::SemanticError;
use crate::datom::{Datom, IndexOrder};
use crate::identity::{DB_PARTITION, eid_to_eidx, eid_to_part, t_to_tx};
use crate::schema::{Attribute, Cardinality, TupleSpec, Unique, ValueType};
use crate::value::{Keyword, Value};

pub const MAX_SCHEMA_ATTRIBUTE_ID: u32 = 1_048_576;

/// SHA-256 of the canonical ATMC format-3 encoded genesis emitted immediately
/// before native excision vocabulary support. This pins the one legacy root
/// profile accepted for the ordinary bootstrap-data upgrade; future
/// vocabulary additions must not silently redefine it.
pub(crate) const PRE_EXCISION_GENESIS_HASH: [u8; 32] = [
    0x30, 0xbf, 0x2a, 0xf2, 0xb3, 0xda, 0x46, 0x7b, 0xb9, 0x3e, 0x41, 0xbf, 0xf4, 0xf5, 0x72, 0x16,
    0x11, 0x55, 0xc4, 0x3d, 0x98, 0x13, 0x9d, 0xa0, 0x8e, 0xc3, 0xfb, 0x3f, 0xaf, 0x3a, 0x88, 0xdb,
];

/// Independently captured from the existing PostgreSQL fixture before partition
/// vocabulary was added (4588 encoded bytes). Do not redefine old root identity.
pub(crate) const PRE_PARTITION_GENESIS_HASH: [u8; 32] = [
    0xb9, 0x9d, 0x04, 0xf2, 0x50, 0x79, 0xc8, 0x64, 0x6f, 0xc0, 0x66, 0x97, 0x5d, 0x6d, 0x9b, 0x95,
    0x29, 0x84, 0xa6, 0x8e, 0x31, 0x4d, 0x9d, 0x82, 0x99, 0xdb, 0x62, 0xed, 0x51, 0x1c, 0xab, 0x75,
];

/// Independently captured from the Goal4 library before fulltext installation.
/// ATMC format3,4818 bytes; not recomputed from the new genesis at runtime.
pub(crate) const PRE_FULLTEXT_GENESIS_HASH: [u8; 32] = [
    0x90, 0x74, 0xce, 0xe4, 0xe4, 0xbb, 0xf8, 0x35, 0x8e, 0xf3, 0xfc, 0xd8, 0x04, 0xde, 0x6e, 0xf2,
    0x18, 0xfe, 0x7e, 0x55, 0xe5, 0x71, 0x0d, 0x5f, 0xd6, 0x0a, 0x20, 0x5d, 0x09, 0xec, 0x24, 0x3a,
];

pub const DB_PART_DB: u64 = 0;
pub const DB_ADD: u64 = 1;
pub const DB_RETRACT: u64 = 2;
pub const DB_PART_TX: u64 = 3;
pub const DB_PART_USER: u64 = 4;

pub const DB_IDENT: u64 = 10;
pub const DB_INSTALL_PARTITION: u64 = 11;
pub const DB_INSTALL_ATTRIBUTE: u64 = 13;
/// Excision request target. Recovered fixed id from Datomic Pro 1.0.7705
/// `datomic.db/BOOT-IDS` and `bootstrap-data-upgrades`.
pub const DB_EXCISE: u64 = 15;
pub const DB_EXCISE_ATTRS: u64 = 16;
pub const DB_EXCISE_BEFORE_T: u64 = 17;
pub const DB_EXCISE_BEFORE: u64 = 18;
pub const DB_ALTER_ATTRIBUTE: u64 = 19;

pub const DB_TYPE_REF: u64 = 20;
pub const DB_TYPE_KEYWORD: u64 = 21;
pub const DB_TYPE_LONG: u64 = 22;
pub const DB_TYPE_STRING: u64 = 23;
pub const DB_TYPE_BOOLEAN: u64 = 24;
pub const DB_TYPE_INSTANT: u64 = 25;
pub const DB_TYPE_FN: u64 = 26;
pub const DB_TYPE_BYTES: u64 = 27;

pub const DB_CARDINALITY_ONE: u64 = 35;
pub const DB_CARDINALITY_MANY: u64 = 36;
pub const DB_UNIQUE_VALUE: u64 = 37;
pub const DB_UNIQUE_IDENTITY: u64 = 38;

pub const DB_VALUE_TYPE: u64 = 40;
pub const DB_CARDINALITY: u64 = 41;
pub const DB_UNIQUE: u64 = 42;
pub const DB_IS_COMPONENT: u64 = 43;
pub const DB_INDEX: u64 = 44;
pub const DB_NO_HISTORY: u64 = 45;
pub const DB_TX_INSTANT: u64 = 50;
/// Boolean immutable schema property enabling analyzed fulltext indexing.
pub const DB_FULLTEXT: u64 = 51;
pub const DB_FN: u64 = 52;

pub const DB_FN_RETRACT_ENTITY: u64 = 54;
pub const DB_FN_CAS: u64 = 55;
pub const DB_TYPE_UUID: u64 = 56;
pub const DB_TYPE_DOUBLE: u64 = 57;
pub const DB_TYPE_FLOAT: u64 = 58;
pub const DB_TYPE_URI: u64 = 59;
pub const DB_TYPE_BIGINT: u64 = 60;
pub const DB_TYPE_BIGDEC: u64 = 61;
pub const DB_DOC: u64 = 62;
pub const DB_TYPE_TUPLE: u64 = 63;
pub const DB_TYPE_SYMBOL: u64 = 64;
pub const DB_TUPLE_TYPE: u64 = 65;
pub const DB_TUPLE_TYPES: u64 = 66;
pub const DB_TUPLE_ATTRS: u64 = 67;
pub const DB_ENSURE: u64 = 68;
pub const DB_ENTITY_ATTRS: u64 = 69;
pub const DB_ENTITY_PREDS: u64 = 70;
pub const DB_ATTR_PREDS: u64 = 71;
pub const DB_TUPLE_DISCONTINUED: u64 = 72;

/// Convert a schema entity ID into the compact attribute ID used by datoms.
///
/// Installed attributes are restricted to partition 0 and to the recovered
/// 2^20 schema element-vector bound.  The upper bound is inclusive, matching
/// the Goal 11 source contract.
pub fn schema_eid_to_attr_id(eid: u64) -> Result<u32, SemanticError> {
    let partition = eid_to_part(eid)?;
    if partition != DB_PARTITION {
        return Err(SemanticError::incorrect(
            "schema/attribute-outside-db-partition",
            format!(
                "schema entity {eid} belongs to partition {partition}, not partition {DB_PARTITION}"
            ),
        ));
    }

    let eidx = eid_to_eidx(eid)?;
    if eidx > u64::from(MAX_SCHEMA_ATTRIBUTE_ID) {
        return Err(SemanticError::incorrect(
            "schema/attribute-id-out-of-range",
            format!("schema attribute id {eidx} exceeds the maximum {MAX_SCHEMA_ATTRIBUTE_ID}"),
        ));
    }

    u32::try_from(eidx).map_err(|_| {
        SemanticError::incorrect(
            "schema/attribute-id-out-of-range",
            format!("schema attribute id {eidx} cannot be represented as u32"),
        )
    })
}

pub const fn value_type_entity(value_type: ValueType) -> u64 {
    match value_type {
        ValueType::Ref => DB_TYPE_REF,
        ValueType::Keyword => DB_TYPE_KEYWORD,
        ValueType::Long => DB_TYPE_LONG,
        ValueType::String => DB_TYPE_STRING,
        ValueType::Boolean => DB_TYPE_BOOLEAN,
        ValueType::Instant => DB_TYPE_INSTANT,
        ValueType::Function => DB_TYPE_FN,
        ValueType::Bytes => DB_TYPE_BYTES,
        ValueType::Uuid => DB_TYPE_UUID,
        ValueType::Double => DB_TYPE_DOUBLE,
        ValueType::Float => DB_TYPE_FLOAT,
        ValueType::Uri => DB_TYPE_URI,
        ValueType::BigInt => DB_TYPE_BIGINT,
        ValueType::BigDec => DB_TYPE_BIGDEC,
        ValueType::Tuple => DB_TYPE_TUPLE,
        ValueType::Symbol => DB_TYPE_SYMBOL,
    }
}

pub const fn value_type_for_entity(entity: u64) -> Option<ValueType> {
    match entity {
        DB_TYPE_REF => Some(ValueType::Ref),
        DB_TYPE_KEYWORD => Some(ValueType::Keyword),
        DB_TYPE_LONG => Some(ValueType::Long),
        DB_TYPE_STRING => Some(ValueType::String),
        DB_TYPE_BOOLEAN => Some(ValueType::Boolean),
        DB_TYPE_INSTANT => Some(ValueType::Instant),
        DB_TYPE_FN => Some(ValueType::Function),
        DB_TYPE_BYTES => Some(ValueType::Bytes),
        DB_TYPE_UUID => Some(ValueType::Uuid),
        DB_TYPE_DOUBLE => Some(ValueType::Double),
        DB_TYPE_FLOAT => Some(ValueType::Float),
        DB_TYPE_URI => Some(ValueType::Uri),
        DB_TYPE_BIGINT => Some(ValueType::BigInt),
        DB_TYPE_BIGDEC => Some(ValueType::BigDec),
        DB_TYPE_TUPLE => Some(ValueType::Tuple),
        DB_TYPE_SYMBOL => Some(ValueType::Symbol),
        _ => None,
    }
}

pub const fn cardinality_entity(cardinality: Cardinality) -> u64 {
    match cardinality {
        Cardinality::One => DB_CARDINALITY_ONE,
        Cardinality::Many => DB_CARDINALITY_MANY,
    }
}

pub const fn cardinality_for_entity(entity: u64) -> Option<Cardinality> {
    match entity {
        DB_CARDINALITY_ONE => Some(Cardinality::One),
        DB_CARDINALITY_MANY => Some(Cardinality::Many),
        _ => None,
    }
}

pub const fn unique_entity(unique: Unique) -> u64 {
    match unique {
        Unique::Value => DB_UNIQUE_VALUE,
        Unique::Identity => DB_UNIQUE_IDENTITY,
    }
}

pub const fn unique_for_entity(entity: u64) -> Option<Unique> {
    match entity {
        DB_UNIQUE_VALUE => Some(Unique::Value),
        DB_UNIQUE_IDENTITY => Some(Unique::Identity),
        _ => None,
    }
}

/// Every system entity whose ident is retained by the native kernel.
///
/// JVM Fressian tags, language/code metadata, install-function, and
/// JVM install-function machinery are intentionally absent. `:db/fn`
/// remains as an ordinary attribute whose value is a native program hash.
pub fn supported_system_idents() -> Vec<(u64, Keyword)> {
    SYSTEM_IDENT_SPECS
        .iter()
        .map(|&(entity, namespace, name)| (entity, Keyword::new(namespace, name)))
        .collect()
}

/// Typed projections of the installed native system attributes.
///
/// These values are construction/verification aids.  Once genesis is loaded,
/// runtime schema caches must be re-derived from its ordinary datoms.
pub fn supported_system_attributes() -> Vec<Attribute> {
    vec![
        Attribute::new(
            attr_id(DB_IDENT),
            kw("db", "ident"),
            ValueType::Keyword,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        partition_install_attribute(),
        Attribute::new(
            attr_id(DB_INSTALL_ATTRIBUTE),
            kw("db.install", "attribute"),
            ValueType::Ref,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_EXCISE),
            kw("db", "excise"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_EXCISE_ATTRS),
            kw("db.excise", "attrs"),
            ValueType::Ref,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_EXCISE_BEFORE_T),
            kw("db.excise", "beforeT"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_EXCISE_BEFORE),
            kw("db.excise", "before"),
            ValueType::Instant,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_ALTER_ATTRIBUTE),
            kw("db.alter", "attribute"),
            ValueType::Ref,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_VALUE_TYPE),
            kw("db", "valueType"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_CARDINALITY),
            kw("db", "cardinality"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_UNIQUE),
            kw("db", "unique"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_IS_COMPONENT),
            kw("db", "isComponent"),
            ValueType::Boolean,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_INDEX),
            kw("db", "index"),
            ValueType::Boolean,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_NO_HISTORY),
            kw("db", "noHistory"),
            ValueType::Boolean,
            Cardinality::One,
        ),
        indexed(Attribute::new(
            attr_id(DB_TX_INSTANT),
            kw("db", "txInstant"),
            ValueType::Instant,
            Cardinality::One,
        )),
        fulltext_attribute(),
        Attribute::new(
            attr_id(DB_FN),
            kw("db", "fn"),
            ValueType::Function,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_DOC),
            kw("db", "doc"),
            ValueType::String,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_TUPLE_TYPE),
            kw("db", "tupleType"),
            ValueType::Keyword,
            Cardinality::One,
        ),
        Attribute::new(
            attr_id(DB_TUPLE_TYPES),
            kw("db", "tupleTypes"),
            ValueType::Tuple,
            Cardinality::One,
        )
        .tuple(TupleSpec::Homogeneous(ValueType::Keyword)),
        Attribute::new(
            attr_id(DB_TUPLE_ATTRS),
            kw("db", "tupleAttrs"),
            ValueType::Tuple,
            Cardinality::One,
        )
        .tuple(TupleSpec::Homogeneous(ValueType::Keyword)),
        Attribute::new(
            attr_id(DB_ENSURE),
            kw("db", "ensure"),
            ValueType::Ref,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_ENTITY_ATTRS),
            kw("db.entity", "attrs"),
            ValueType::Keyword,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_ENTITY_PREDS),
            kw("db.entity", "preds"),
            ValueType::Symbol,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_ATTR_PREDS),
            kw("db.attr", "preds"),
            ValueType::Symbol,
            Cardinality::Many,
        ),
        Attribute::new(
            attr_id(DB_TUPLE_DISCONTINUED),
            kw("db.tuple", "discontinued"),
            ValueType::Boolean,
            Cardinality::One,
        ),
    ]
}

/// Canonical self-describing native database information at logical t=0.
///
/// Ordering is EAV/value order, independent of construction order.  Genesis
/// contains no transaction-instant datum because it is a protocol base, not a
/// committed user transaction.
pub fn canonical_genesis_datoms() -> Vec<Datom> {
    let tx = t_to_tx(0).expect("logical genesis t is always representable");
    let mut datoms = Vec::new();

    for (entity, ident) in supported_system_idents() {
        datoms.push(assertion(entity, DB_IDENT, Value::Keyword(ident), tx));
    }

    for attribute in supported_system_attributes() {
        let entity = u64::from(attribute.id);
        datoms.push(assertion(
            entity,
            DB_VALUE_TYPE,
            Value::Ref(value_type_entity(attribute.value_type)),
            tx,
        ));
        datoms.push(assertion(
            entity,
            DB_CARDINALITY,
            Value::Ref(cardinality_entity(attribute.cardinality)),
            tx,
        ));

        if let Some(unique) = attribute.unique {
            datoms.push(assertion(
                entity,
                DB_UNIQUE,
                Value::Ref(unique_entity(unique)),
                tx,
            ));
        }
        if attribute.indexed {
            datoms.push(assertion(entity, DB_INDEX, Value::Bool(true), tx));
        }
        if attribute.component {
            datoms.push(assertion(entity, DB_IS_COMPONENT, Value::Bool(true), tx));
        }
        if attribute.no_history {
            datoms.push(assertion(entity, DB_NO_HISTORY, Value::Bool(true), tx));
        }
        if let Some(TupleSpec::Homogeneous(value_type)) = attribute.tuple {
            datoms.push(assertion(
                entity,
                DB_TUPLE_TYPE,
                Value::Keyword(value_type_ident(value_type)),
                tx,
            ));
        }
        if attribute.tuple_discontinued {
            datoms.push(assertion(
                entity,
                DB_TUPLE_DISCONTINUED,
                Value::Bool(true),
                tx,
            ));
        }

        datoms.push(assertion(
            DB_PART_DB,
            DB_INSTALL_ATTRIBUTE,
            Value::Ref(entity),
            tx,
        ));
    }

    for part in [DB_PART_DB, DB_PART_TX, DB_PART_USER] {
        datoms.push(assertion(
            DB_PART_DB,
            DB_INSTALL_PARTITION,
            Value::Ref(part),
            tx,
        ));
    }
    datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
    debug_assert!(datoms.windows(2).all(|pair| pair[0] != pair[1]));
    datoms
}

/// Exact native genesis emitted before the excision vocabulary was restored.
///
/// Existing catalogs must never have their stored genesis reinterpreted or
/// rewritten: its hash is the root of the authenticated transaction chain.
/// Migration installs ids 15--18 as one ordinary schema-information
/// transaction, matching recovered 1.0.7705
/// `datomic.db/bootstrap-data-upgrades` (`db.clj:5235-5254`). This helper is
/// intentionally crate-private and exact; it is not another supported way to
/// create a database.
pub(crate) fn pre_excision_genesis_datoms() -> Vec<Datom> {
    pre_partition_genesis_datoms()
        .into_iter()
        .filter(|datom| {
            !(matches!(
                datom.entity,
                DB_EXCISE | DB_EXCISE_ATTRS | DB_EXCISE_BEFORE_T | DB_EXCISE_BEFORE
            ) || datom.entity == DB_PART_DB
                && u64::from(datom.attribute) == DB_INSTALL_ATTRIBUTE
                && matches!(
                    datom.value,
                    Value::Ref(DB_EXCISE | DB_EXCISE_ATTRS | DB_EXCISE_BEFORE_T | DB_EXCISE_BEFORE)
                ))
        })
        .collect()
}

/// Exact pre-partition profile, not a supported new-database constructor.
pub(crate) fn pre_partition_genesis_datoms() -> Vec<Datom> {
    pre_fulltext_genesis_datoms()
        .into_iter()
        .filter(|datom| {
            datom.entity != DB_INSTALL_PARTITION
                && u64::from(datom.attribute) != DB_INSTALL_PARTITION
                && !(datom.entity == DB_PART_DB
                    && u64::from(datom.attribute) == DB_INSTALL_ATTRIBUTE
                    && datom.value == Value::Ref(DB_INSTALL_PARTITION))
        })
        .collect()
}

pub(crate) fn pre_fulltext_genesis_datoms() -> Vec<Datom> {
    canonical_genesis_datoms()
        .into_iter()
        .filter(|datom| {
            !(datom.entity == DB_FULLTEXT && u64::from(datom.attribute) != DB_IDENT)
                && !(datom.entity == DB_PART_DB
                    && u64::from(datom.attribute) == DB_INSTALL_ATTRIBUTE
                    && datom.value == Value::Ref(DB_FULLTEXT))
        })
        .collect()
}

pub(crate) fn fulltext_attribute() -> Attribute {
    Attribute::new(
        DB_FULLTEXT as u32,
        kw("db", "fulltext"),
        ValueType::Boolean,
        Cardinality::One,
    )
}

/// Explicit ordinary vocabulary installation for an older native database.
/// Use a stable durable request key for exact retry after uncertain outcomes.
pub fn fulltext_vocabulary_upgrade_ops() -> Vec<crate::TxOp> {
    vec![crate::TxOp::InstallAttribute(fulltext_attribute())]
}

pub(crate) fn is_exact_fulltext_upgrade_ops(schema: &crate::Schema, ops: &[crate::TxOp]) -> bool {
    schema.attribute(DB_FULLTEXT as u32).is_err()
        && matches!(ops, [crate::TxOp::InstallAttribute(attribute)] if attribute == &fulltext_attribute())
}

pub(crate) fn validate_fulltext_upgrade_transition(
    before: &crate::Schema,
    after: &crate::Schema,
    ops: &[crate::TxOp],
) -> Result<(), SemanticError> {
    if before.attribute(DB_FULLTEXT as u32).is_err()
        && after.attribute(DB_FULLTEXT as u32).is_ok()
        && !is_exact_fulltext_upgrade_ops(before, ops)
    {
        return Err(SemanticError::incorrect(
            "schema/fulltext-upgrade-required",
            "fulltext vocabulary requires its exact explicit upgrade transaction",
        ));
    }
    Ok(())
}

pub(crate) fn partition_install_attribute() -> Attribute {
    Attribute::new(
        DB_INSTALL_PARTITION as u32,
        kw("db.install", "partition"),
        ValueType::Ref,
        Cardinality::Many,
    )
}

/// Explicit ordinary transaction for a database created before partition support.
/// Requires absent partition vocabulary; retry the same durable request key after
/// an uncertain outcome. Newly created databases already contain these facts.
pub fn partition_vocabulary_upgrade_ops() -> Vec<crate::TxOp> {
    let mut ops = vec![crate::TxOp::InstallAttribute(partition_install_attribute())];
    ops.extend(
        [DB_PART_DB, DB_PART_TX, DB_PART_USER]
            .into_iter()
            .map(|part| crate::TxOp::Add {
                entity: crate::EntityRef::Id(DB_PART_DB),
                attribute: DB_INSTALL_PARTITION as u32,
                value: Value::Ref(part).into(),
            }),
    );
    ops
}

pub(crate) fn is_exact_partition_upgrade_ops(schema: &crate::Schema, ops: &[crate::TxOp]) -> bool {
    if schema.attribute(DB_INSTALL_PARTITION as u32).is_ok() {
        return false;
    }
    let expected = partition_vocabulary_upgrade_ops();
    ops.len() == expected.len()
        && expected.iter().all(|expected| {
            ops.iter()
                .any(|actual| crate::transaction::compare_tx_op(actual, expected).is_eq())
        })
}

pub(crate) fn is_partition_upgrade_marker(op: &crate::TxOp) -> bool {
    matches!(op, crate::TxOp::Add {
        entity: crate::EntityRef::Id(DB_PART_DB),
        attribute,
        value: crate::TxValue::Scalar(Value::Ref(DB_PART_DB | DB_PART_TX | DB_PART_USER)),
    } if u64::from(*attribute) == DB_INSTALL_PARTITION)
}

fn indexed(mut attribute: Attribute) -> Attribute {
    attribute.indexed = true;
    attribute
}

fn assertion(entity: u64, attribute: u64, value: Value, tx: u64) -> Datom {
    Datom {
        entity,
        attribute: attr_id(attribute),
        value,
        tx,
        added: true,
    }
}

fn attr_id(entity: u64) -> u32 {
    schema_eid_to_attr_id(entity).expect("native vocabulary attribute IDs are valid")
}

fn kw(namespace: &str, name: &str) -> Keyword {
    Keyword::new(namespace, name)
}

pub(crate) fn value_type_ident(value_type: ValueType) -> Keyword {
    let entity = value_type_entity(value_type);
    SYSTEM_IDENT_SPECS
        .iter()
        .find_map(|&(candidate, namespace, name)| {
            (candidate == entity).then(|| kw(namespace, name))
        })
        .expect("every supported value type has a system ident")
}

const SYSTEM_IDENT_SPECS: &[(u64, &str, &str)] = &[
    (DB_PART_DB, "db.part", "db"),
    (DB_ADD, "db", "add"),
    (DB_RETRACT, "db", "retract"),
    (DB_PART_TX, "db.part", "tx"),
    (DB_PART_USER, "db.part", "user"),
    (DB_IDENT, "db", "ident"),
    (DB_INSTALL_PARTITION, "db.install", "partition"),
    (DB_INSTALL_ATTRIBUTE, "db.install", "attribute"),
    (DB_EXCISE, "db", "excise"),
    (DB_EXCISE_ATTRS, "db.excise", "attrs"),
    (DB_EXCISE_BEFORE_T, "db.excise", "beforeT"),
    (DB_EXCISE_BEFORE, "db.excise", "before"),
    (DB_ALTER_ATTRIBUTE, "db.alter", "attribute"),
    (DB_TYPE_REF, "db.type", "ref"),
    (DB_TYPE_KEYWORD, "db.type", "keyword"),
    (DB_TYPE_LONG, "db.type", "long"),
    (DB_TYPE_STRING, "db.type", "string"),
    (DB_TYPE_BOOLEAN, "db.type", "boolean"),
    (DB_TYPE_INSTANT, "db.type", "instant"),
    (DB_TYPE_FN, "db.type", "fn"),
    (DB_TYPE_BYTES, "db.type", "bytes"),
    (DB_CARDINALITY_ONE, "db.cardinality", "one"),
    (DB_CARDINALITY_MANY, "db.cardinality", "many"),
    (DB_UNIQUE_VALUE, "db.unique", "value"),
    (DB_UNIQUE_IDENTITY, "db.unique", "identity"),
    (DB_VALUE_TYPE, "db", "valueType"),
    (DB_CARDINALITY, "db", "cardinality"),
    (DB_UNIQUE, "db", "unique"),
    (DB_IS_COMPONENT, "db", "isComponent"),
    (DB_INDEX, "db", "index"),
    (DB_NO_HISTORY, "db", "noHistory"),
    (DB_TX_INSTANT, "db", "txInstant"),
    (DB_FULLTEXT, "db", "fulltext"),
    (DB_FN, "db", "fn"),
    (DB_FN_RETRACT_ENTITY, "db.fn", "retractEntity"),
    (DB_FN_CAS, "db.fn", "cas"),
    (DB_TYPE_UUID, "db.type", "uuid"),
    (DB_TYPE_DOUBLE, "db.type", "double"),
    (DB_TYPE_FLOAT, "db.type", "float"),
    (DB_TYPE_URI, "db.type", "uri"),
    (DB_TYPE_BIGINT, "db.type", "bigint"),
    (DB_TYPE_BIGDEC, "db.type", "bigdec"),
    (DB_DOC, "db", "doc"),
    (DB_TYPE_TUPLE, "db.type", "tuple"),
    (DB_TYPE_SYMBOL, "db.type", "symbol"),
    (DB_TUPLE_TYPE, "db", "tupleType"),
    (DB_TUPLE_TYPES, "db", "tupleTypes"),
    (DB_TUPLE_ATTRS, "db", "tupleAttrs"),
    (DB_ENSURE, "db", "ensure"),
    (DB_ENTITY_ATTRS, "db.entity", "attrs"),
    (DB_ENTITY_PREDS, "db.entity", "preds"),
    (DB_ATTR_PREDS, "db.attr", "preds"),
    (DB_TUPLE_DISCONTINUED, "db.tuple", "discontinued"),
];

#[cfg(test)]
mod tests {
    use super::*;
    use crate::identity::{TX_PARTITION, USER_PARTITION, make_eid};

    #[test]
    fn fulltext_upgrade_preserves_every_exact_older_genesis_profile() {
        for (genesis, size, hash) in [
            (
                pre_excision_genesis_datoms(),
                4051,
                PRE_EXCISION_GENESIS_HASH,
            ),
            (
                pre_partition_genesis_datoms(),
                4588,
                PRE_PARTITION_GENESIS_HASH,
            ),
            (
                pre_fulltext_genesis_datoms(),
                4818,
                PRE_FULLTEXT_GENESIS_HASH,
            ),
        ] {
            let encoded = crate::encode_genesis(&genesis).unwrap();
            assert_eq!(encoded.len(), size);
            assert_eq!(crate::sha256(&encoded), hash);
            let before = crate::Database::from_genesis(genesis.clone()).unwrap();
            assert!(before.schema().attribute(DB_FULLTEXT as u32).is_err());
            let ops = fulltext_vocabulary_upgrade_ops();
            let mut altered = fulltext_attribute();
            altered.indexed = true;
            assert!(
                before
                    .with(&[crate::TxOp::InstallAttribute(altered)], 10)
                    .is_err()
            );
            let raw = [
                crate::TxOp::Add {
                    entity: crate::EntityRef::Id(DB_FULLTEXT),
                    attribute: DB_VALUE_TYPE as u32,
                    value: Value::Ref(DB_TYPE_BOOLEAN).into(),
                },
                crate::TxOp::Add {
                    entity: crate::EntityRef::Id(DB_FULLTEXT),
                    attribute: DB_CARDINALITY as u32,
                    value: Value::Ref(DB_CARDINALITY_ONE).into(),
                },
            ];
            assert_eq!(
                before.with(&raw, 10).unwrap_err().code,
                "schema/fulltext-upgrade-required"
            );
            assert_eq!(
                before.database_value().with(&raw, 10).unwrap_err().code,
                "schema/fulltext-upgrade-required"
            );
            let report = before.with(&ops, 10).unwrap();
            let native = before.database_value().with(&ops, 10).unwrap();
            assert_eq!(native.tx_data, report.tx_data);
            assert_eq!(native.db_after.schema(), report.db_after.schema());
            assert_eq!(report.db_after.genesis_datoms(), genesis);
            assert_eq!(
                report
                    .db_after
                    .schema()
                    .attribute(DB_FULLTEXT as u32)
                    .unwrap(),
                &fulltext_attribute()
            );
            let durable = crate::DurableTransaction {
                database_id: "fulltext-upgrade".into(),
                basis_t: report.db_after.basis_t(),
                previous_hash: hash,
                eidx_frontier: report.db_after.eidx_frontier(),
                tempids: report.tempids.clone(),
                tx_data: report.tx_data.clone(),
            };
            assert!(
                before
                    .apply_committed(&durable)
                    .unwrap()
                    .same_information_as(&report.db_after)
            );
            let attr = Attribute::new(
                1000,
                kw("search", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext();
            let installed = report
                .db_after
                .with(&[crate::TxOp::InstallAttribute(attr.clone())], 20)
                .unwrap();
            assert_eq!(installed.db_after.schema().attribute(1000).unwrap(), &attr);
            let mut extra = ops;
            extra.push(crate::TxOp::Add {
                entity: crate::EntityRef::Id(DB_PART_USER),
                attribute: DB_DOC as u32,
                value: Value::String("extra".into()).into(),
            });
            assert!(before.with(&extra, 10).is_err());
            let mut smuggled = durable;
            smuggled.tx_data.push(Datom {
                entity: DB_PART_USER,
                attribute: DB_DOC as u32,
                value: Value::String("extra".into()),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            });
            smuggled
                .tx_data
                .sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
            assert_eq!(
                before.apply_committed(&smuggled).unwrap_err().code,
                "recovery/noncanonical-fulltext-bootstrap-upgrade"
            );
        }
    }

    #[test]
    fn partition_upgrade_preserves_exact_legacy_genesis_and_replay() {
        let encoded = crate::encode_genesis(&pre_partition_genesis_datoms()).unwrap();
        assert_eq!(encoded.len(), 4588);
        assert_eq!(crate::sha256(&encoded), PRE_PARTITION_GENESIS_HASH);
        for genesis in [
            pre_partition_genesis_datoms(),
            pre_excision_genesis_datoms(),
        ] {
            let before = crate::Database::from_genesis(genesis.clone()).unwrap();
            assert!(
                before
                    .schema()
                    .attribute(DB_INSTALL_PARTITION as u32)
                    .is_err()
            );
            let ops = partition_vocabulary_upgrade_ops();
            let report = before.with(&ops, 10).unwrap();
            assert_eq!(report.db_after.genesis_datoms(), genesis);
            assert_eq!(report.tx_data.len(), 8);
            let durable = crate::DurableTransaction {
                database_id: "legacy-partition-upgrade".into(),
                basis_t: report.db_after.basis_t(),
                previous_hash: [0; 32],
                eidx_frontier: report.db_after.eidx_frontier(),
                tempids: report.tempids.clone(),
                tx_data: report.tx_data.clone(),
            };
            let replayed = before.apply_committed(&durable).unwrap();
            assert!(replayed.same_information_as(&report.db_after));
            for part in [DB_PART_DB, DB_PART_TX, DB_PART_USER] {
                replayed
                    .schema()
                    .validate_partition_bits(part as u32)
                    .unwrap();
            }
            assert!(before.with(&ops[..3], 10).is_err());
            let mut modified = ops.clone();
            let crate::TxOp::InstallAttribute(attribute) = &mut modified[0] else {
                unreachable!()
            };
            attribute.indexed = true;
            assert!(before.with(&modified, 10).is_err());
            let mut missing_marker = durable.clone();
            missing_marker.tx_data.retain(|datom| {
                !(datom.attribute == DB_INSTALL_PARTITION as u32
                    && datom.value == Value::Ref(DB_PART_USER))
            });
            assert!(before.apply_committed(&missing_marker).is_err());
            let mut smuggled = durable;
            smuggled.tx_data.push(Datom {
                entity: DB_PART_USER,
                attribute: DB_DOC as u32,
                value: Value::String("unrelated bootstrap payload".into()),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            });
            smuggled
                .tx_data
                .sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
            assert_eq!(
                before.apply_committed(&smuggled).unwrap_err().code,
                "recovery/noncanonical-partition-bootstrap-upgrade"
            );
        }
    }

    #[test]
    fn schema_entity_conversion_checks_partition_and_bound() {
        assert_eq!(schema_eid_to_attr_id(72).unwrap(), 72);
        assert_eq!(
            schema_eid_to_attr_id(u64::from(MAX_SCHEMA_ATTRIBUTE_ID)).unwrap(),
            MAX_SCHEMA_ATTRIBUTE_ID
        );
        assert_eq!(
            schema_eid_to_attr_id(u64::from(MAX_SCHEMA_ATTRIBUTE_ID) + 1)
                .unwrap_err()
                .code,
            "schema/attribute-id-out-of-range"
        );
        assert_eq!(
            schema_eid_to_attr_id(make_eid(USER_PARTITION, 72).unwrap())
                .unwrap_err()
                .code,
            "schema/attribute-outside-db-partition"
        );
        assert_eq!(
            schema_eid_to_attr_id(make_eid(TX_PARTITION, 72).unwrap())
                .unwrap_err()
                .code,
            "schema/attribute-outside-db-partition"
        );
    }

    #[test]
    fn system_enum_mappings_round_trip() {
        for value_type in [
            ValueType::Ref,
            ValueType::Keyword,
            ValueType::Long,
            ValueType::String,
            ValueType::Boolean,
            ValueType::Instant,
            ValueType::Function,
            ValueType::Bytes,
            ValueType::Uuid,
            ValueType::Double,
            ValueType::Float,
            ValueType::Uri,
            ValueType::BigInt,
            ValueType::BigDec,
            ValueType::Tuple,
            ValueType::Symbol,
        ] {
            assert_eq!(
                value_type_for_entity(value_type_entity(value_type)),
                Some(value_type)
            );
        }
        for cardinality in [Cardinality::One, Cardinality::Many] {
            assert_eq!(
                cardinality_for_entity(cardinality_entity(cardinality)),
                Some(cardinality)
            );
        }
        for unique in [Unique::Value, Unique::Identity] {
            assert_eq!(unique_for_entity(unique_entity(unique)), Some(unique));
        }
    }

    #[test]
    fn genesis_is_canonical_self_describing_information() {
        let genesis = canonical_genesis_datoms();
        let tx = t_to_tx(0).unwrap();

        assert!(
            genesis
                .windows(2)
                .all(|pair| { pair[0].cmp_in(&pair[1], IndexOrder::Eavt).is_lt() })
        );
        assert!(genesis.iter().all(|datom| datom.tx == tx && datom.added));
        assert!(
            !genesis
                .iter()
                .any(|datom| { datom.entity == tx && datom.attribute == attr_id(DB_TX_INSTANT) })
        );
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_IDENT
                && datom.attribute == attr_id(DB_UNIQUE)
                && datom.value == Value::Ref(DB_UNIQUE_IDENTITY)
        }));
        assert!(!genesis.iter().any(|datom| {
            datom.entity == DB_IDENT
                && datom.attribute == attr_id(DB_INDEX)
                && datom.value == Value::Bool(true)
        }));
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_TX_INSTANT
                && datom.attribute == attr_id(DB_INDEX)
                && datom.value == Value::Bool(true)
        }));
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_FULLTEXT
                && datom.attribute == attr_id(DB_IDENT)
                && datom.value == Value::Keyword(kw("db", "fulltext"))
        }));
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_TYPE_FN
                && datom.attribute == attr_id(DB_IDENT)
                && datom.value == Value::Keyword(kw("db.type", "fn"))
        }));
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_FN
                && datom.attribute == attr_id(DB_VALUE_TYPE)
                && datom.value == Value::Ref(DB_TYPE_FN)
        }));
        assert!(genesis.iter().any(|datom| {
            datom.entity == DB_FN
                && datom.attribute == attr_id(DB_CARDINALITY)
                && datom.value == Value::Ref(DB_CARDINALITY_ONE)
        }));
        for (entity, ident, value_type, cardinality) in [
            (
                DB_EXCISE,
                kw("db", "excise"),
                DB_TYPE_REF,
                DB_CARDINALITY_ONE,
            ),
            (
                DB_EXCISE_ATTRS,
                kw("db.excise", "attrs"),
                DB_TYPE_REF,
                DB_CARDINALITY_MANY,
            ),
            (
                DB_EXCISE_BEFORE_T,
                kw("db.excise", "beforeT"),
                DB_TYPE_LONG,
                DB_CARDINALITY_ONE,
            ),
            (
                DB_EXCISE_BEFORE,
                kw("db.excise", "before"),
                DB_TYPE_INSTANT,
                DB_CARDINALITY_ONE,
            ),
        ] {
            assert!(genesis.iter().any(|datom| {
                datom.entity == entity
                    && datom.attribute == attr_id(DB_IDENT)
                    && datom.value == Value::Keyword(ident.clone())
            }));
            assert!(genesis.iter().any(|datom| {
                datom.entity == entity
                    && datom.attribute == attr_id(DB_VALUE_TYPE)
                    && datom.value == Value::Ref(value_type)
            }));
            assert!(genesis.iter().any(|datom| {
                datom.entity == entity
                    && datom.attribute == attr_id(DB_CARDINALITY)
                    && datom.value == Value::Ref(cardinality)
            }));
            assert!(genesis.iter().any(|datom| {
                datom.entity == DB_PART_DB
                    && datom.attribute == attr_id(DB_INSTALL_ATTRIBUTE)
                    && datom.value == Value::Ref(entity)
            }));
        }
        assert!(
            supported_system_attributes()
                .iter()
                .any(|attribute| u64::from(attribute.id) == DB_FULLTEXT)
        );

        for attribute in supported_system_attributes() {
            assert!(genesis.iter().any(|datom| {
                datom.entity == DB_PART_DB
                    && datom.attribute == attr_id(DB_INSTALL_ATTRIBUTE)
                    && datom.value == Value::Ref(u64::from(attribute.id))
            }));
        }
    }

    #[test]
    fn pre_excision_genesis_is_one_exact_upgrade_behind_current() {
        let legacy = pre_excision_genesis_datoms();
        assert!(legacy.len() < canonical_genesis_datoms().len());
        let encoded = crate::encode_genesis(&legacy).unwrap();
        assert_eq!(encoded.len(), 4_051);
        assert_eq!(crate::sha256(&encoded), PRE_EXCISION_GENESIS_HASH);
        for entity in [
            DB_EXCISE,
            DB_EXCISE_ATTRS,
            DB_EXCISE_BEFORE_T,
            DB_EXCISE_BEFORE,
        ] {
            assert!(!legacy.iter().any(|datom| {
                datom.entity == entity
                    || (datom.entity == DB_PART_DB
                        && datom.attribute == attr_id(DB_INSTALL_ATTRIBUTE)
                        && datom.value == Value::Ref(entity))
            }));
        }
    }
}
