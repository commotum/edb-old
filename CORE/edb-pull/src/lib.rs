use base64::engine::general_purpose::STANDARD_NO_PAD;
use base64::Engine;
use thiserror::Error;
use serde::{Serialize, Deserialize};
use rusqlite::{Connection, OptionalExtension, params};

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
    db_path: Option<String>,
    limit: Option<usize>,
    max_depth: usize,
}

impl<'a> Puller<'a> {
    pub fn new(conn: &'a Connection) -> Self { Self { conn, db_path: None, limit: None, max_depth: 8 } }
    pub fn new_with_path(conn: &'a Connection, db_path: impl Into<String>) -> Self { Self { conn, db_path: Some(db_path.into()), limit: None, max_depth: 8 } }
    pub fn with_limit(mut self, limit: Option<usize>) -> Self { self.limit = limit; self }
    pub fn with_max_depth(mut self, max_depth: usize) -> Self { self.max_depth = max_depth; self }

    pub fn pull_entity(&self, e: i64, specs: &[AttrSpec]) -> Result<serde_json::Value, PullError> {
        self.pull_entity_inner(e, specs, 0)
    }

    fn pull_entity_inner(&self, e: i64, specs: &[AttrSpec], depth: usize) -> Result<serde_json::Value, PullError> {
        if depth >= self.max_depth { return Ok(serde_json::Value::Object(serde_json::Map::new())); }
        let mut out = serde_json::Map::new();
        for s in specs {
            match s {
                AttrSpec::Attr(a) => {
                    let (ident, vt, card) = self.attr_meta(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? { out.insert(ident, val); }
                    } else {
                        let vals = self.current_values_many(e, &ident, vt)?;
                        out.insert(ident, serde_json::Value::Array(vals));
                    }
                }
                AttrSpec::AttrAs { attr, alias } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? { out.insert(alias.clone(), val); }
                    } else {
                        let vals = self.current_values_many(e, &ident, vt)?;
                        out.insert(alias.clone(), serde_json::Value::Array(vals));
                    }
                }
                AttrSpec::AttrDefault { attr, default } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? { out.insert(ident, val); } else { out.insert(ident, default.clone()); }
                    } else {
                        let vals = self.current_values_many(e, &ident, vt)?;
                        if vals.is_empty() { out.insert(ident, default.clone()); } else { out.insert(ident, serde_json::Value::Array(vals)); }
                    }
                }
                AttrSpec::Reverse(a) => {
                    let ident = self.resolve_attr(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    let ids = self.apply_limit_ids(ids);
                    out.insert(format!("_{}", ident), serde_json::json!(ids));
                }
                AttrSpec::Nested { attr, specs: sub } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(v) = self.current_value(e, &ident)? {
                            if let Some(e2) = extract_ref(&v) {
                                let nested = self.pull_entity_inner(e2, sub, depth+1)?;
                                out.insert(ident, nested);
                            } else { out.insert(ident, v); }
                        }
                    } else {
                        if vt == edb_encoding::ValueType::Ref {
                            let ids = self.forward_refs_many(e, &ident)?;
                            let ids = self.apply_limit_ids(ids);
                            let mut coll = Vec::new();
                            for e2 in ids { coll.push(self.pull_entity_inner(e2, sub, depth+1)?); }
                            out.insert(ident, serde_json::Value::Array(coll));
                        } else {
                            let vals = self.current_values_many(e, &ident, vt)?;
                            out.insert(ident, serde_json::Value::Array(vals));
                        }
                    }
                }
                AttrSpec::ReverseNested { attr, specs: sub } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    let ids = self.apply_limit_ids(ids);
                    let mut coll = Vec::new();
                    for e2 in ids { coll.push(self.pull_entity_inner(e2, sub, depth+1)?); }
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

    fn attr_meta(&self, ident: &str) -> Result<Option<(String, edb_encoding::ValueType, i64)>, PullError> {
        let row: Option<(String, i64, i64)> = self.conn
            .query_row(
                "SELECT ident,vt,card FROM attrs WHERE ident=(SELECT COALESCE((SELECT ident FROM attrs WHERE ident=?1),(SELECT target FROM aliases WHERE alias=?1)))",
                params![ident],
                |r| Ok((r.get::<_, String>(0)?, r.get::<_, i64>(1)?, r.get::<_, i64>(2)?)),
            )
            .optional()?;
        if let Some((ident, vt_i, card)) = row {
            let vt = super_map_vt(vt_i).ok_or_else(|| PullError::UnknownAttribute(ident.clone()))?;
            Ok(Some((ident, vt, card)))
        } else { Ok(None) }
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

    fn forward_refs_many(&self, e: i64, a: &str) -> Result<Vec<i64>, PullError> {
        if let Some(ref path) = self.db_path {
            let idx = edb_index::EavtIndexer::open(path).map_err(|_| PullError::Sqlite(rusqlite::Error::InvalidQuery))?;
            let datoms = idx.scan_ea(e, a).map_err(|_| PullError::Sqlite(rusqlite::Error::InvalidQuery))?;
            let mut seen = std::collections::HashSet::new();
            let mut ids = Vec::new();
            for d in datoms {
                if seen.insert(d.v_b64.clone()) {
                    if d.added {
                        if let Ok(bytes) = STANDARD_NO_PAD.decode(d.v_b64.as_bytes()) {
                            if bytes.len() == 8 { ids.push(i64::from_be_bytes(bytes.try_into().unwrap())); }
                        }
                    }
                }
            }
            return Ok(self.apply_limit_ids(ids));
        }
        Ok(vec![])
    }

    fn current_values_many(&self, e: i64, a: &str, vt: edb_encoding::ValueType) -> Result<Vec<serde_json::Value>, PullError> {
        if let Some(ref path) = self.db_path {
            let idx = edb_index::EavtIndexer::open(path).map_err(|_| PullError::Sqlite(rusqlite::Error::InvalidQuery))?;
            let datoms = idx.scan_ea(e, a).map_err(|_| PullError::Sqlite(rusqlite::Error::InvalidQuery))?;
            let mut seen = std::collections::HashSet::new();
            let mut out = Vec::new();
            for d in datoms {
                if seen.insert(d.v_b64.clone()) {
                    if d.added {
                        if let Ok(bytes) = STANDARD_NO_PAD.decode(d.v_b64.as_bytes()) {
                            if let Ok(val) = edb_encoding::decode_scalar(vt, &bytes) { out.push(val); }
                        }
                    }
                }
            }
            if let Some(limit) = self.limit { if out.len() > limit { out.truncate(limit); } }
            return Ok(out);
        }
        Ok(vec![])
    }

    fn apply_limit_ids(&self, mut ids: Vec<i64>) -> Vec<i64> {
        if let Some(limit) = self.limit { if ids.len() > limit { ids.truncate(limit); } }
        ids
    }
}

fn to_plain_json(raw: &str) -> Result<serde_json::Value, PullError> {
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
            edb_tx::model::Value::Bytes(b) => serde_json::json!(STANDARD_NO_PAD.encode(b)),
            edb_tx::model::Value::Uint8(u) => serde_json::json!(u),
            edb_tx::model::Value::Bigint(s) => serde_json::json!(s),
            edb_tx::model::Value::Decimal(s) => serde_json::json!(s),
        })
    } else {
        Ok(serde_json::from_str::<serde_json::Value>(raw)?)
    }
}

fn extract_ref(v: &serde_json::Value) -> Option<i64> {
    match v { serde_json::Value::Number(n) => n.as_i64(), _ => None }
}

fn super_map_vt(v: i64) -> Option<edb_encoding::ValueType> {
    match v {
        1 => Some(edb_encoding::ValueType::Long),
        2 => Some(edb_encoding::ValueType::Double),
        3 => Some(edb_encoding::ValueType::Boolean),
        4 => Some(edb_encoding::ValueType::String),
        5 => Some(edb_encoding::ValueType::Keyword),
        6 => Some(edb_encoding::ValueType::Uuid),
        7 => Some(edb_encoding::ValueType::Instant),
        8 => Some(edb_encoding::ValueType::Ref),
        9 => Some(edb_encoding::ValueType::Bytes),
        10 => Some(edb_encoding::ValueType::Uint8),
        11 => Some(edb_encoding::ValueType::Bigint),
        12 => Some(edb_encoding::ValueType::Decimal),
        13 => Some(edb_encoding::ValueType::Float32),
        14 => Some(edb_encoding::ValueType::Float16),
        15 => Some(edb_encoding::ValueType::Bfloat16),
        _ => None,
    }
}
