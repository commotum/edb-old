use edb_encoding::ValueType;
use serde::{Deserialize, Serialize};
use uuid::Uuid;

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum Value {
    Long(i64),
    Double(f64),
    Boolean(bool),
    String(String),
    Keyword(String),
    Uuid(Uuid),
    Instant(i64),
    Ref(i64),
    Bytes(Vec<u8>),
    Uint8(u8),
    Bigint(String),   // decimal string
    Decimal(String),  // canonical decimal string
}

impl Value {
    pub fn value_type(&self) -> ValueType {
        match self {
            Value::Long(_) => ValueType::Long,
            Value::Double(_) => ValueType::Double,
            Value::Boolean(_) => ValueType::Boolean,
            Value::String(_) => ValueType::String,
            Value::Keyword(_) => ValueType::Keyword,
            Value::Uuid(_) => ValueType::Uuid,
            Value::Instant(_) => ValueType::Instant,
            Value::Ref(_) => ValueType::Ref,
            Value::Bytes(_) => ValueType::Bytes,
            Value::Uint8(_) => ValueType::Uint8,
            Value::Bigint(_) => ValueType::Bigint,
            Value::Decimal(_) => ValueType::Decimal,
        }
    }
}

#[derive(Debug, Clone, PartialEq, Eq, Hash, Serialize, Deserialize)]
pub struct TempId(pub String);

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum EntityRef {
    Entid(i64),
    TempId(TempId),
    LookupRef { attr: String, value: Value },
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub enum TxOp {
    Add { e: EntityRef, a: String, v: Value },
    Retract { e: EntityRef, a: String, v: Option<Value> },
    Cas { e: EntityRef, a: String, expected: Option<Value>, v: Value },
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct TxPrimitive {
    pub added: bool,
    pub e: i64,
    pub a: String,
    pub v: Value,
}

#[derive(Debug, Clone, PartialEq, Serialize, Deserialize)]
pub struct TxReport {
    pub t: Option<i64>,
    pub tx_eid: Option<i64>,
    pub tempids: Vec<(TempId, i64)>,
    pub touched_attrs: Vec<String>,
    pub primitives: Vec<TxPrimitive>,
    #[serde(default)]
    pub meta: Option<serde_json::Value>,
}
