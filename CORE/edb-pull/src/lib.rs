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
    // Variants with per-spec limits and optional max-depth override
    NestedLimit { attr: String, specs: Vec<AttrSpec>, limit: Option<usize>, max_depth: Option<usize> },
    ReverseNestedLimit { attr: String, specs: Vec<AttrSpec>, limit: Option<usize>, max_depth: Option<usize> },
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
        self.pull_entity_inner_with_limits(e, specs, 0, self.max_depth, self.limit)
    }

    fn pull_entity_inner_with_limits(&self, e: i64, specs: &[AttrSpec], depth: usize, stop_depth: usize, local_limit: Option<usize>) -> Result<serde_json::Value, PullError> {
        if depth >= stop_depth { return Ok(serde_json::Value::Object(serde_json::Map::new())); }
        let mut out = serde_json::Map::new();
        for s in specs {
            match s {
                AttrSpec::Attr(a) => {
                    let (ident, vt, card, is_comp) = self.attr_meta_full(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? {
                            // Component-default expansion: if ref + component, expand nested map
                            if vt == edb_encoding::ValueType::Ref && is_comp {
                                if let Some(e2) = extract_ref(&val) {
                                    let nested = self.pull_component_entity(e2, depth+1, stop_depth, local_limit)?;
                                    out.insert(ident, nested);
                                } else {
                                    out.insert(ident, val);
                                }
                            } else {
                                out.insert(ident, val);
                            }
                        }
                    } else {
                        let vals = self.current_values_many_with_limit(e, &ident, vt, local_limit)?;
                        out.insert(ident, serde_json::Value::Array(vals));
                    }
                }
                AttrSpec::AttrAs { attr, alias } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? { out.insert(alias.clone(), val); }
                    } else {
                        let vals = self.current_values_many_with_limit(e, &ident, vt, local_limit)?;
                        out.insert(alias.clone(), serde_json::Value::Array(vals));
                    }
                }
                AttrSpec::AttrDefault { attr, default } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(val) = self.current_value(e, &ident)? { out.insert(ident, val); } else { out.insert(ident, default.clone()); }
                    } else {
                        let vals = self.current_values_many_with_limit(e, &ident, vt, local_limit)?;
                        if vals.is_empty() { out.insert(ident, default.clone()); } else { out.insert(ident, serde_json::Value::Array(vals)); }
                    }
                }
                AttrSpec::Reverse(a) => {
                    let ident = self.resolve_attr(a)?.ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    let ids = self.apply_limit_ids_with_limit(ids, local_limit);
                    out.insert(format!("_{}", ident), serde_json::json!(ids));
                }
                AttrSpec::Nested { attr, specs: sub } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    if card == 1 {
                        if let Some(v) = self.current_value(e, &ident)? {
                            if let Some(e2) = extract_ref(&v) {
                                let nested = self.pull_entity_inner_with_limits(e2, sub, depth+1, stop_depth, local_limit)?;
                                out.insert(ident, nested);
                            } else { out.insert(ident, v); }
                        }
                    } else {
                        if vt == edb_encoding::ValueType::Ref {
                            let ids = self.forward_refs_many(e, &ident)?;
                            let ids = self.apply_limit_ids_with_limit(ids, local_limit);
                            let mut coll = Vec::new();
                            for e2 in ids { coll.push(self.pull_entity_inner_with_limits(e2, sub, depth+1, stop_depth, local_limit)?); }
                            out.insert(ident, serde_json::Value::Array(coll));
                        } else {
                            let vals = self.current_values_many_with_limit(e, &ident, vt, local_limit)?;
                            out.insert(ident, serde_json::Value::Array(vals));
                        }
                    }
                }
                AttrSpec::ReverseNested { attr, specs: sub } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    let ids = self.reverse_refs(&ident, e)?;
                    let ids = self.apply_limit_ids_with_limit(ids, local_limit);
                    let mut coll = Vec::new();
                    for e2 in ids { coll.push(self.pull_entity_inner_with_limits(e2, sub, depth+1, stop_depth, local_limit)?); }
                    out.insert(format!("_{}", ident), serde_json::Value::Array(coll));
                }
                AttrSpec::NestedLimit { attr, specs: sub, limit, max_depth } => {
                    let (ident, vt, card) = self.attr_meta(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    let eff_stop = match max_depth { Some(md) => depth + *md, None => stop_depth };
                    let eff_limit = limit.or(local_limit);
                    if card == 1 {
                        if let Some(v) = self.current_value(e, &ident)? {
                            if let Some(e2) = extract_ref(&v) {
                                let nested = self.pull_entity_inner_with_limits(e2, sub, depth+1, eff_stop, eff_limit)?;
                                out.insert(ident, nested);
                            } else { out.insert(ident, v); }
                        }
                    } else {
                        if vt == edb_encoding::ValueType::Ref {
                            let ids = self.forward_refs_many(e, &ident)?;
                            let ids = self.apply_limit_ids_with_limit(ids, eff_limit);
                            let mut coll = Vec::new();
                            for e2 in ids { coll.push(self.pull_entity_inner_with_limits(e2, sub, depth+1, eff_stop, eff_limit)?); }
                            out.insert(ident, serde_json::Value::Array(coll));
                        } else {
                            let vals = self.current_values_many_with_limit(e, &ident, vt, eff_limit)?;
                            out.insert(ident, serde_json::Value::Array(vals));
                        }
                    }
                }
                AttrSpec::ReverseNestedLimit { attr, specs: sub, limit, max_depth } => {
                    let ident = self.resolve_attr(attr)?.ok_or_else(|| PullError::UnknownAttribute(attr.clone()))?;
                    let eff_stop = match max_depth { Some(md) => depth + *md, None => stop_depth };
                    let eff_limit = limit.or(local_limit);
                    let ids = self.reverse_refs(&ident, e)?;
                    let ids = self.apply_limit_ids_with_limit(ids, eff_limit);
                    let mut coll = Vec::new();
                    for e2 in ids { coll.push(self.pull_entity_inner_with_limits(e2, sub, depth+1, eff_stop, eff_limit)?); }
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
            return Ok(ids);
        }
        Ok(vec![])
    }

    fn current_values_many_with_limit(&self, e: i64, a: &str, vt: edb_encoding::ValueType, limit: Option<usize>) -> Result<Vec<serde_json::Value>, PullError> {
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
            if let Some(limit) = limit { if out.len() > limit { out.truncate(limit); } }
            return Ok(out);
        }
        Ok(vec![])
    }

    fn apply_limit_ids_with_limit(&self, mut ids: Vec<i64>, limit: Option<usize>) -> Vec<i64> {
        if let Some(limit) = limit { if ids.len() > limit { ids.truncate(limit); } }
        ids
    }

    fn attr_meta_full(&self, ident: &str) -> Result<Option<(String, edb_encoding::ValueType, i64, bool)>, PullError> {
        let row: Option<(String, i64, i64, i64)> = self.conn
            .query_row(
                "SELECT ident,vt,card,is_component FROM attrs WHERE ident=(SELECT COALESCE((SELECT ident FROM attrs WHERE ident=?1),(SELECT target FROM aliases WHERE alias=?1)))",
                params![ident],
                |r| Ok((r.get::<_, String>(0)?, r.get::<_, i64>(1)?, r.get::<_, i64>(2)?, r.get::<_, i64>(3)?)),
            )
            .optional()?;
        if let Some((ident, vt_i, card, comp_i)) = row {
            let vt = super_map_vt(vt_i).ok_or_else(|| PullError::UnknownAttribute(ident.clone()))?;
            Ok(Some((ident, vt, card, comp_i != 0)))
        } else { Ok(None) }
    }

    fn pull_component_entity(&self, e: i64, depth: usize, stop_depth: usize, local_limit: Option<usize>) -> Result<serde_json::Value, PullError> {
        if depth >= stop_depth { return Ok(serde_json::Value::Object(serde_json::Map::new())); }
        let mut out = serde_json::Map::new();
        let mut stmt = self.conn.prepare("SELECT c.a, c.vjson, a.vt, a.card, a.is_component FROM current c JOIN attrs a ON c.a=a.ident WHERE c.e=?1")?;
        let rows = stmt.query_map(params![e], |r| Ok((
            r.get::<_, String>(0)?,
            r.get::<_, String>(1)?,
            r.get::<_, i64>(2)?,
            r.get::<_, i64>(3)?,
            r.get::<_, i64>(4)?,
        )))?;
        let mut many_acc: std::collections::BTreeMap<String, Vec<serde_json::Value>> = std::collections::BTreeMap::new();
        for row in rows {
            let (a, vjson, vt_i, card, comp_i) = row?;
            let vt = super_map_vt(vt_i).ok_or_else(|| PullError::UnknownAttribute(a.clone()))?;
            if card == 1 {
                let val = to_plain_json(&vjson)?;
                if vt == edb_encoding::ValueType::Ref && comp_i != 0 {
                    if let Some(e2) = extract_ref(&val) {
                        out.insert(a.clone(), self.pull_component_entity(e2, depth+1, stop_depth, local_limit)?);
                    } else {
                        out.insert(a.clone(), val);
                    }
                } else {
                    out.insert(a.clone(), val);
                }
            } else {
                // accumulate many; apply limit at the end
                let ent_val = if vt == edb_encoding::ValueType::Ref {
                    // keep raw eid number
                    to_plain_json(&vjson)?
                } else {
                    to_plain_json(&vjson)?
                };
                many_acc.entry(a.clone()).or_default().push(ent_val);
            }
        }
        // Truncate many collections
        if let Some(limit) = local_limit {
            for (_k, v) in many_acc.iter_mut() { if v.len() > limit { v.truncate(limit); } }
        }
        for (k, v) in many_acc.into_iter() { out.insert(k, serde_json::Value::Array(v)); }
        Ok(serde_json::Value::Object(out))
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
    edb_encoding::ValueType::try_from(v).ok()
}
