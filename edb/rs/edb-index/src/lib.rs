use base64::{engine::general_purpose, Engine as _};
use edb_encoding::{encode_scalar, ValueType};
use edb_encoding::scalar::{encode_scalar_string, parse_varuint_be};
use edb_store_sqlite::{RootStore, SegmentStore, SqliteStore, StoreError};
use edb_tx::model::{TxPrimitive, Value};
use serde::{Deserialize, Serialize};
use sha2::{Digest, Sha256};
use rusqlite::{Connection, OptionalExtension, params};

#[derive(thiserror::Error, Debug)]
pub enum IndexError {
    #[error("store: {0}")]
    Store(#[from] StoreError),
    #[error("serde: {0}")]
    Serde(#[from] serde_json::Error),
    #[error("encode: {0}")]
    Encode(String),
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Datom {
    pub e: i64,
    pub a: String,
    pub v_b64: String,
    pub t: i64,
    pub added: bool,
}

impl Datom {
    pub fn new(e: i64, a: String, v_bytes: Vec<u8>, t: i64, added: bool) -> Self {
        let v_b64 = general_purpose::STANDARD_NO_PAD.encode(v_bytes);
        Self { e, a, v_b64, t, added }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct EavtRootV1 {
    version: u8,
    segments: Vec<String>, // list of segment ids
}

pub struct EavtIndexer {
    store: SqliteStore,
    memory: Vec<Datom>,
    conn: Connection,
    merge_threshold: usize,
}

impl EavtIndexer {
    pub fn open(path: &str) -> Result<Self, IndexError> {
        let store = SqliteStore::open(path)?;
        let conn = Connection::open(path).map_err(StoreError::from)?;
        Ok(Self { store, memory: Vec::new(), conn, merge_threshold: 1024 })
    }

    pub fn apply_primitives(&mut self, prims: &[TxPrimitive], t: i64) -> Result<(), IndexError> {
        for p in prims {
            let vt = self.lookup_value_type(&p.a).unwrap_or_else(|| value_type_of(&p.v));
            let vjson = value_to_json(&p.v);
            let v_bytes = encode_scalar(vt, &vjson).map_err(IndexError::Encode)?;
            self.memory.push(Datom::new(p.e, p.a.clone(), v_bytes, t, p.added));
        }
        Ok(())
    }

    pub fn merge(&mut self) -> Result<String, IndexError> {
        // Sort memory datoms by E asc, A asc, V asc, T desc
        self.memory.sort_by(compare_datom_eavt);
        let encoded = serde_json::to_vec(&self.memory)?;
        let mut hasher = Sha256::new();
        hasher.update(&encoded);
        let id = hex::encode(hasher.finalize());
        let _ = self.store.put_segment_if_absent(&id, &encoded)?;
        let root = EavtRootV1 { version: 1, segments: vec![id.clone()] };
        let root_bytes = serde_json::to_vec(&root)?;
        let head = self.store.get_root("eavt");
        if let Ok(Some((rev, _))) = head {
            let _ = self.store.cas_root("eavt", rev, &root_bytes)?;
        } else {
            self.store.init_root("eavt", &root_bytes)?;
        }
        self.memory.clear();
        Ok(id)
    }

    /// Merge recent in-memory datoms into a durable segment when the
    /// configured threshold has been reached. Returns Some(segment_id)
    /// when a merge occurred.
    pub fn maybe_merge(&mut self) -> Result<Option<String>, IndexError> {
        if self.memory.len() >= self.merge_threshold {
            let id = self.merge()?;
            Ok(Some(id))
        } else {
            Ok(None)
        }
    }

    /// Configure the in-memory merge threshold (number of datoms buffered
    /// before triggering a background merge via maybe_merge()).
    pub fn set_merge_threshold(&mut self, threshold: usize) { self.merge_threshold = threshold; }

    pub fn scan_entity(&self, e: i64) -> Result<Vec<Datom>, IndexError> {
        let mut results: Vec<Datom> = Vec::new();
        // Persisted segments
        if let Some((_rev, root_bytes)) = self.store.get_root("eavt")? {
            if let Ok(root) = serde_json::from_slice::<EavtRootV1>(&root_bytes) {
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<Datom>>(&bytes) {
                            // binary search on e (segments are sorted)
                            let slice = &datoms;
                            let lo = lower_bound_e(slice, e);
                            let mut i = lo;
                            while i < slice.len() && slice[i].e == e {
                                results.push(slice[i].clone());
                                i += 1;
                            }
                        }
                    }
                }
            }
        }
        // Include in-memory; memory is maintained sorted after merge
        for d in self.memory.iter() {
            if d.e == e { results.push(d.clone()); }
        }
        // Sort final results by EAVT (they all share same E, so A/V/T sort applies)
        results.sort_by(compare_datom_eavt);
        Ok(results)
    }

    /// Scan by (e, a): returns datoms for entity `e` and attribute `a`
    /// ordered by EAVT (T desc within identical E/A/V).
    pub fn scan_ea(&self, e: i64, a: &str) -> Result<Vec<Datom>, IndexError> {
        let mut results: Vec<Datom> = Vec::new();
        // Persisted segments
        if let Some((_rev, root_bytes)) = self.store.get_root("eavt")? {
            if let Ok(root) = serde_json::from_slice::<EavtRootV1>(&root_bytes) {
                let a_key = attr_sort_key(a);
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<Datom>>(&bytes) {
                            let (lo, hi) = bounds_for_e(&datoms, e);
                            if lo < hi {
                                let start = lower_bound_ea_in_range(&datoms[lo..hi], &a_key) + lo;
                                let mut i = start;
                                while i < hi {
                                    let d = &datoms[i];
                                    if attr_sort_key(&d.a) != a_key { break; }
                                    results.push(d.clone());
                                    i += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        // Memory buffer
        for d in self.memory.iter() {
            if d.e == e && d.a == a { results.push(d.clone()); }
        }
        results.sort_by(compare_datom_eavt);
        Ok(results)
    }

    /// Scan by (e, a, v_prefix): returns datoms whose value bytes begin
    /// with the provided prefix.
    pub fn scan_eav_prefix(&self, e: i64, a: &str, v_prefix: &[u8]) -> Result<Vec<Datom>, IndexError> {
        let mut results: Vec<Datom> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("eavt")? {
            if let Ok(root) = serde_json::from_slice::<EavtRootV1>(&root_bytes) {
                let a_key = attr_sort_key(a);
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<Datom>>(&bytes) {
                            let (lo, hi) = bounds_for_e(&datoms, e);
                            if lo < hi {
                                let start = lower_bound_ea_in_range(&datoms[lo..hi], &a_key) + lo;
                                let mut i = start;
                                while i < hi {
                                    let d = &datoms[i];
                                    if attr_sort_key(&d.a) != a_key { break; }
                                    if let Ok(v_bytes) = general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()) {
                                        if v_bytes.starts_with(v_prefix) { results.push(d.clone()); }
                                    }
                                    i += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() {
            if d.e == e && d.a == a {
                if let Ok(v_bytes) = general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()) {
                    if v_bytes.starts_with(v_prefix) { results.push(d.clone()); }
                }
            }
        }
        results.sort_by(compare_datom_eavt);
        Ok(results)
    }
}

fn value_type_of(v: &Value) -> ValueType {
    v.value_type()
}

fn value_to_json(v: &Value) -> serde_json::Value {
    match v {
        Value::Long(x) => serde_json::json!(*x),
        Value::Double(x) => serde_json::json!(*x),
        Value::Boolean(b) => serde_json::json!(*b),
        Value::String(s) => serde_json::json!(s),
        Value::Keyword(s) => serde_json::json!(s),
        Value::Uuid(u) => serde_json::json!(u.to_string()),
        Value::Instant(x) => serde_json::json!(*x),
        Value::Ref(x) => serde_json::json!(*x),
        Value::Bytes(b) => serde_json::json!(String::from_utf8_lossy(b).to_string()),
        Value::Uint8(x) => serde_json::json!(*x as u64),
        Value::Bigint(s) => serde_json::json!(s),
        Value::Decimal(s) => serde_json::json!(s),
    }
}

fn compare_datom_eavt(d1: &Datom, d2: &Datom) -> std::cmp::Ordering {
    use std::cmp::Ordering::*;
    // E asc
    match d1.e.cmp(&d2.e) {
        Equal => {
            // A asc by normalized content bytes (drop length prefix)
            let a1 = attr_sort_key(&d1.a);
            let a2 = attr_sort_key(&d2.a);
            match a1.cmp(&a2) {
                Equal => {
                    // V asc by order-preserving bytes
                    let v1 = general_purpose::STANDARD_NO_PAD
                        .decode(d1.v_b64.as_bytes())
                        .unwrap_or_default();
                    let v2 = general_purpose::STANDARD_NO_PAD
                        .decode(d2.v_b64.as_bytes())
                        .unwrap_or_default();
                    match v1.cmp(&v2) {
                        Equal => {
                            // T desc
                            d2.t.cmp(&d1.t)
                        }
                        other => other,
                    }
                }
                other => other,
            }
        }
        other => other,
    }
}

fn lower_bound_e(datoms: &[Datom], e: i64) -> usize {
    let mut lo = 0usize;
    let mut hi = datoms.len();
    while lo < hi {
        let mid = (lo + hi) / 2;
        if datoms[mid].e < e {
            lo = mid + 1;
        } else {
            hi = mid;
        }
    }
    lo
}

/// Return (lo, hi) bounds for all datoms with entity `e` in a sorted segment slice.
fn bounds_for_e(datoms: &[Datom], e: i64) -> (usize, usize) {
    let lo = lower_bound_e(datoms, e);
    let mut hi = lo;
    while hi < datoms.len() && datoms[hi].e == e { hi += 1; }
    (lo, hi)
}

/// Within a range of a single-entity slice (sorted by A,V,T), return the
/// first index whose attribute sort key >= `a_key`.
fn lower_bound_ea_in_range(datoms: &[Datom], a_key: &[u8]) -> usize {
    let mut lo = 0usize;
    let mut hi = datoms.len();
    while lo < hi {
        let mid = (lo + hi) / 2;
        let mid_key = attr_sort_key(&datoms[mid].a);
        if mid_key < a_key {
            lo = mid + 1;
        } else {
            hi = mid;
        }
    }
    lo
}

fn attr_sort_key(a: &str) -> Vec<u8> {
    // Use encoding crate’s NFC normalization, then strip the varuint length prefix
    let enc = encode_scalar_string(a);
    let (_len, i) = parse_varuint_be(&enc).unwrap_or((0, 1));
    enc[i..].to_vec()
}

impl EavtIndexer {
    fn lookup_value_type(&self, ident: &str) -> Option<ValueType> {
        let vt_i: Option<i64> = self
            .conn
            .query_row("SELECT vt FROM attrs WHERE ident=?1", params![ident], |r| r.get(0))
            .optional()
            .ok()
            .flatten();
        vt_i.and_then(map_vt_i64)
    }
}

fn map_vt_i64(v: i64) -> Option<ValueType> {
    match v {
        1 => Some(ValueType::Long),
        2 => Some(ValueType::Double),
        3 => Some(ValueType::Boolean),
        4 => Some(ValueType::String),
        5 => Some(ValueType::Keyword),
        6 => Some(ValueType::Uuid),
        7 => Some(ValueType::Instant),
        8 => Some(ValueType::Ref),
        9 => Some(ValueType::Bytes),
        10 => Some(ValueType::Uint8),
        11 => Some(ValueType::Bigint),
        12 => Some(ValueType::Decimal),
        13 => Some(ValueType::Float32),
        14 => Some(ValueType::Float16),
        15 => Some(ValueType::Bfloat16),
        _ => None,
    }
}
