use rusqlite::{params, Connection, OptionalExtension};
use serde::{Deserialize, Serialize};
use thiserror::Error;
use base64::Engine;

#[derive(Error, Debug)]
pub enum PullError {
    #[error("sqlite: {0}")]
    Sqlite(#[from] rusqlite::Error),
    #[error("json: {0}")]
    Json(#[from] serde_json::Error),
    #[error("unknown attribute: {0}")]
    UnknownAttribute(String),
}

#[derive(Clone, Debug, Serialize, Deserialize, PartialEq)]
#[serde(tag = "type", content = "data")]
pub enum AttrSpec {
    Attr(String),
    Reverse(String),
    Nested { attr: String, specs: Vec<AttrSpec> },
    ReverseNested { attr: String, specs: Vec<AttrSpec> },
    AttrAs { attr: String, alias: String },
    AttrDefault { attr: String, default: serde_json::Value },
}

pub struct Puller<'a> {
    pub conn: &'a Connection,
}

fn to_plain_json(raw: &str) -> Result<serde_json::Value, PullError> {
    // Try to decode as edb_tx::Value and map to plain JSON; fallback to raw
    if let Ok(v) = serde_json::from_str::<edb_tx::model::Value>(raw) {
        Ok(match v {
            edb_tx::model::Value::Long(x) => serde_json::json!(x),
            edb_tx::model::Value::Double(x) => serde_json::json!(x),
            edb_tx::model::Value::Boolean(b) => serde_json::json!(b),
            edb_tx::model::Value::String(s) => serde_json::json!(s),
            edb_tx::model::Value::Keyword(s) => serde_json::json!(s),
            edb_tx::model::Value::Uuid(u) => serde_json::json!(u),
            edb_tx::model::Value::Instant(t) => serde_json::json!(t),
            edb_tx::model::Value::Ref(e) => serde_json::json!(e),
            edb_tx::model::Value::Bytes(b) => serde_json::json!(base64::engine::general_purpose::STANDARD_NO_PAD.encode(b)),
            edb_tx::model::Value::Uint8(u) => serde_json::json!(u),
            edb_tx::model::Value::Bigint(s) => serde_json::json!(s),
            edb_tx::model::Value::Decimal(s) => serde_json::json!(s),
        })
    } else {
        Ok(serde_json::from_str::<serde_json::Value>(raw)?)
    }
}

fn extract_ref(v: &serde_json::Value) -> Option<i64> {
    match v {
        serde_json::Value::Number(n) => n.as_i64(),
        _ => None,
    }
}

impl<'a> Puller<'a> {
    pub fn new(conn: &'a Connection) -> Self { Self { conn } }

    pub fn pull_entity(&self, e: i64, specs: &[AttrSpec]) -> Result<serde_json::Value, PullError> {
        let mut out = serde_json::Map::new();
        for s in specs {
            match s {
                AttrSpec::Attr(a) => {
                    let ident = self.resolve_attr(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    if let Some(val) = self.current_value(e, &ident)? {
                        out.insert(ident, val);
                    }
                }
                AttrSpec::AttrAs { attr, alias } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if let Some(val) = self.current_value(e, &ident)? {
                        out.insert(alias.clone(), val);
                    }
                }
                AttrSpec::AttrDefault { attr, default } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if let Some(val) = self.current_value(e, &ident)? {
                        out.insert(ident, val);
                    } else {
                        out.insert(ident, default.clone());
                    }
                }
                AttrSpec::Reverse(a) => {
                    let ident = self.resolve_attr(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    out.insert(format!("_{}", ident), serde_json::json!(ids));
                }
                AttrSpec::Nested { attr, specs: sub } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if let Some(v) = self.current_value(e, &ident)? {
                        if let Some(e2) = extract_ref(&v) {
                            let nested = self.pull_entity(e2, sub)?;
                            out.insert(ident, nested);
                        } else {
                            out.insert(ident, v);
                        }
                    }
                }
                AttrSpec::ReverseNested { attr, specs: sub } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    let mut coll = Vec::new();
                    for e2 in ids { coll.push(self.pull_entity(e2, sub)?); }
                    out.insert(format!("_{}", ident), serde_json::Value::Array(coll));
                }
            }
        }
        Ok(serde_json::Value::Object(out))
    }

    fn resolve_attr(&self, ident: &str) -> Result<Option<String>, PullError> {
        let target: Option<String> = self
            .conn
            .query_row(
                "SELECT ident FROM attrs WHERE ident=?1 UNION SELECT target FROM aliases WHERE alias=?1 LIMIT 1",
                params![ident],
                |r| r.get(0),
            )
            .optional()?;
        Ok(target)
    }

    fn current_value(&self, e: i64, a: &str) -> Result<Option<serde_json::Value>, PullError> {
        let vjson: Option<String> = self
            .conn
            .query_row(
                "SELECT vjson FROM current WHERE e=?1 AND a=?2",
                params![e, a],
                |r| r.get::<_, String>(0),
            )
            .optional()?;
        match vjson {
            Some(s) => Ok(Some(to_plain_json(&s)?)),
            None => Ok(None),
        }
    }

    fn reverse_refs(&self, a: &str, target_e: i64) -> Result<Vec<i64>, PullError> {
        let vjson = serde_json::to_string(&edb_tx::model::Value::Ref(target_e))?;
        let mut stmt = self
            .conn
            .prepare("SELECT e FROM current WHERE a=?1 AND vjson=?2")?;
        let rows = stmt.query_map(params![a, vjson], |r| r.get::<_, i64>(0))?;
        let mut out = Vec::new();
        for row in rows { out.push(row?); }
        Ok(out)
    }
}
