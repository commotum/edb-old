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
        // Append to existing root segments if present
        let mut segments: Vec<String> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("eavt")? {
            if let Ok(root) = serde_json::from_slice::<EavtRootV1>(&root_bytes) { segments = root.segments; }
        }
        segments.push(id.clone());
        let root = EavtRootV1 { version: 1, segments };
        let root_bytes = serde_json::to_vec(&root)?;
        let head = self.store.get_root("eavt");
        if let Ok(Some((rev, _))) = head { let _ = self.store.cas_root("eavt", rev, &root_bytes)?; } else { self.store.init_root("eavt", &root_bytes)?; }
        self.memory.clear();
        // Compact if too many segments accumulated
        let _ = self.compact_if_needed(8)?;
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
        if mid_key.as_slice() < a_key {
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

    fn compact_if_needed(&mut self, max_segments: usize) -> Result<Option<String>, IndexError> {
        if let Some((rev, bytes)) = self.store.get_root("eavt")? {
            if let Ok(root) = serde_json::from_slice::<EavtRootV1>(&bytes) {
                if root.segments.len() > max_segments {
                    let mut all: Vec<Datom> = Vec::new();
                    for sid in root.segments {
                        if let Some(seg) = self.store.get_segment(&sid)? {
                            let mut v: Vec<Datom> = serde_json::from_slice(&seg)?;
                            all.append(&mut v);
                        }
                    }
                    all.sort_by(compare_datom_eavt);
                    let encoded = serde_json::to_vec(&all)?;
                    let mut hasher = Sha256::new(); hasher.update(&encoded);
                    let id = hex::encode(hasher.finalize());
                    let _ = self.store.put_segment_if_absent(&id, &encoded)?;
                    let new_root = EavtRootV1 { version: 1, segments: vec![id.clone()] };
                    let root_bytes = serde_json::to_vec(&new_root)?;
                    let _ = self.store.cas_root("eavt", rev, &root_bytes)?;
                    return Ok(Some(id));
                }
            }
        }
        Ok(None)
    }
}

fn map_vt_i64(v: i64) -> Option<ValueType> {
    ValueType::try_from(v).ok()
}

// ---------------- AVET (A, V, E, T) ----------------

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AvetDatom {
    pub a: String,
    pub v_b64: String,
    pub e: i64,
    pub t: i64,
    pub added: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct AvetRootV1 {
    version: u8,
    segments: Vec<String>,
}

pub struct AvetIndexer {
    store: SqliteStore,
    memory: Vec<AvetDatom>,
    conn: Connection,
    merge_threshold: usize,
}

impl AvetIndexer {
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
            self.memory.push(AvetDatom { a: p.a.clone(), v_b64: general_purpose::STANDARD_NO_PAD.encode(v_bytes), e: p.e, t, added: p.added });
        }
        Ok(())
    }

    pub fn maybe_merge(&mut self) -> Result<Option<String>, IndexError> {
        if self.memory.len() >= self.merge_threshold { Ok(Some(self.merge()?)) } else { Ok(None) }
    }

    pub fn merge(&mut self) -> Result<String, IndexError> {
        self.memory.sort_by(compare_datom_avet);
        let encoded = serde_json::to_vec(&self.memory)?;
        let mut hasher = Sha256::new();
        hasher.update(&encoded);
        let id = hex::encode(hasher.finalize());
        let _ = self.store.put_segment_if_absent(&id, &encoded)?;
        let mut segments: Vec<String> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("avet")? {
            if let Ok(root) = serde_json::from_slice::<AvetRootV1>(&root_bytes) { segments = root.segments; }
        }
        segments.push(id.clone());
        let root = AvetRootV1 { version: 1, segments };
        let root_bytes = serde_json::to_vec(&root)?;
        let head = self.store.get_root("avet");
        if let Ok(Some((rev, _))) = head { let _ = self.store.cas_root("avet", rev, &root_bytes)?; } else { self.store.init_root("avet", &root_bytes)?; }
        self.memory.clear();
        // Compact if too many segments accumulated
        let _ = self.compact_if_needed(8)?;
        Ok(id)
    }

    pub fn scan_av_eq(&self, a: &str, v_bytes: &[u8]) -> Result<Vec<AvetDatom>, IndexError> {
        let mut results: Vec<AvetDatom> = Vec::new();
        let a_key = attr_sort_key(a);
        let v_key = v_bytes.to_vec();
        if let Some((_rev, root_bytes)) = self.store.get_root("avet")? {
            if let Ok(root) = serde_json::from_slice::<AvetRootV1>(&root_bytes) {
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<AvetDatom>>(&bytes) {
                            let (lo, hi) = bounds_for_a(&datoms, &a_key);
                            if lo < hi {
                                let start = lower_bound_av_in_range(&datoms[lo..hi], &v_key) + lo;
                                let mut i = start;
                                while i < hi {
                                    let d = &datoms[i];
                                    if general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()).unwrap_or_default() != v_key { break; }
                                    results.push(d.clone());
                                    i += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() {
            if attr_sort_key(&d.a) == a_key {
                if general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()).unwrap_or_default() == v_key { results.push(d.clone()); }
            }
        }
        results.sort_by(compare_datom_avet);
        Ok(results)
    }

    /// Scan AV range: for attribute `a`, return datoms with value bytes in [v_start, v_end)
    /// (when v_end is None, the upper bound is unbounded).
    pub fn scan_av_range(&self, a: &str, v_start: &[u8], v_end: Option<&[u8]>) -> Result<Vec<AvetDatom>, IndexError> {
        let mut results: Vec<AvetDatom> = Vec::new();
        let a_key = attr_sort_key(a);
        if let Some((_rev, root_bytes)) = self.store.get_root("avet")? {
            if let Ok(root) = serde_json::from_slice::<AvetRootV1>(&root_bytes) {
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<AvetDatom>>(&bytes) {
                            let (lo, hi) = bounds_for_a(&datoms, &a_key);
                            if lo < hi {
                                let start = lower_bound_av_in_range(&datoms[lo..hi], v_start) + lo;
                                let mut i = start;
                                while i < hi {
                                    let d = &datoms[i];
                                    let v = general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()).unwrap_or_default();
                                    if v.as_slice() < v_start { i += 1; continue; }
                                    if let Some(end) = v_end { if v.as_slice() >= end { break; } }
                                    results.push(d.clone());
                                    i += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() {
            if attr_sort_key(&d.a).as_slice() == a_key.as_slice() {
                let v = general_purpose::STANDARD_NO_PAD.decode(d.v_b64.as_bytes()).unwrap_or_default();
                if v.as_slice() >= v_start && v_end.map(|end| v.as_slice() < end).unwrap_or(true) {
                    results.push(d.clone());
                }
            }
        }
        results.sort_by(compare_datom_avet);
        Ok(results)
    }

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

fn compare_datom_avet(d1: &AvetDatom, d2: &AvetDatom) -> std::cmp::Ordering {
    use std::cmp::Ordering::*;
    match attr_sort_key(&d1.a).cmp(&attr_sort_key(&d2.a)) {
        Equal => {
            let v1 = general_purpose::STANDARD_NO_PAD.decode(d1.v_b64.as_bytes()).unwrap_or_default();
            let v2 = general_purpose::STANDARD_NO_PAD.decode(d2.v_b64.as_bytes()).unwrap_or_default();
            match v1.cmp(&v2) {
                Equal => match d1.e.cmp(&d2.e) { Equal => d2.t.cmp(&d1.t), other => other },
                other => other,
            }
        }
        other => other,
    }
}

fn lower_bound_a(datoms: &[AvetDatom], a_key: &[u8]) -> usize {
    let mut lo = 0usize; let mut hi = datoms.len();
    while lo < hi { let mid = (lo + hi)/2; let mk = attr_sort_key(&datoms[mid].a); if mk.as_slice() < a_key { lo = mid+1; } else { hi = mid; } }
    lo
}

fn bounds_for_a(datoms: &[AvetDatom], a_key: &[u8]) -> (usize, usize) {
    let lo = lower_bound_a(datoms, a_key);
    let mut hi = lo;
    while hi < datoms.len() && attr_sort_key(&datoms[hi].a).as_slice() == a_key { hi += 1; }
    (lo, hi)
}

fn lower_bound_av_in_range(datoms: &[AvetDatom], v_key: &[u8]) -> usize {
    let mut lo = 0usize; let mut hi = datoms.len();
    while lo < hi {
        let mid = (lo + hi)/2;
        let midv = general_purpose::STANDARD_NO_PAD.decode(datoms[mid].v_b64.as_bytes()).unwrap_or_default();
        if midv.as_slice() < v_key { lo = mid + 1; } else { hi = mid; }
    }
    lo
}

impl AvetIndexer {
    fn compact_if_needed(&mut self, max_segments: usize) -> Result<Option<String>, IndexError> {
        if let Some((rev, bytes)) = self.store.get_root("avet")? {
            if let Ok(root) = serde_json::from_slice::<AvetRootV1>(&bytes) {
                if root.segments.len() > max_segments {
                    let mut all: Vec<AvetDatom> = Vec::new();
                    for sid in root.segments {
                        if let Some(seg) = self.store.get_segment(&sid)? {
                            let mut v: Vec<AvetDatom> = serde_json::from_slice(&seg)?;
                            all.append(&mut v);
                        }
                    }
                    all.sort_by(compare_datom_avet);
                    let encoded = serde_json::to_vec(&all)?;
                    let mut hasher = Sha256::new(); hasher.update(&encoded);
                    let id = hex::encode(hasher.finalize());
                    let _ = self.store.put_segment_if_absent(&id, &encoded)?;
                    let new_root = AvetRootV1 { version: 1, segments: vec![id.clone()] };
                    let root_bytes = serde_json::to_vec(&new_root)?;
                    let _ = self.store.cas_root("avet", rev, &root_bytes)?;
                    return Ok(Some(id));
                }
            }
        }
        Ok(None)
    }
}

// ---------------- VAET (V -> A -> E -> T) for refs ----------------

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct VaetDatom {
    pub v_e: i64,
    pub a: String,
    pub e: i64,
    pub t: i64,
    pub added: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct VaetRootV1 { version: u8, segments: Vec<String> }

pub struct VaetIndexer {
    store: SqliteStore,
    memory: Vec<VaetDatom>,
    conn: Connection,
    merge_threshold: usize,
}

impl VaetIndexer {
    pub fn open(path: &str) -> Result<Self, IndexError> {
        let store = SqliteStore::open(path)?;
        let conn = Connection::open(path).map_err(StoreError::from)?;
        Ok(Self { store, memory: Vec::new(), conn, merge_threshold: 1024 })
    }

    pub fn apply_primitives(&mut self, prims: &[TxPrimitive], t: i64) -> Result<(), IndexError> {
        for p in prims {
            // Only ref attributes are included
            if let Some(vt) = self.lookup_value_type(&p.a) {
                if matches!(vt, ValueType::Ref) {
                    if let Value::Ref(target) = p.v.clone() {
                        self.memory.push(VaetDatom { v_e: target, a: p.a.clone(), e: p.e, t, added: p.added });
                    }
                }
            }
        }
        Ok(())
    }

    pub fn maybe_merge(&mut self) -> Result<Option<String>, IndexError> { if self.memory.len() >= self.merge_threshold { Ok(Some(self.merge()?)) } else { Ok(None) } }

    pub fn merge(&mut self) -> Result<String, IndexError> {
        self.memory.sort_by(compare_datom_vaet);
        let encoded = serde_json::to_vec(&self.memory)?;
        let mut hasher = Sha256::new(); hasher.update(&encoded);
        let id = hex::encode(hasher.finalize());
        let _ = self.store.put_segment_if_absent(&id, &encoded)?;
        let mut segments: Vec<String> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("vaet")? {
            if let Ok(root) = serde_json::from_slice::<VaetRootV1>(&root_bytes) { segments = root.segments; }
        }
        segments.push(id.clone());
        let root = VaetRootV1 { version: 1, segments };
        let root_bytes = serde_json::to_vec(&root)?;
        let head = self.store.get_root("vaet");
        if let Ok(Some((rev,_))) = head { let _ = self.store.cas_root("vaet", rev, &root_bytes)?; } else { self.store.init_root("vaet", &root_bytes)?; }
        self.memory.clear();
        let _ = self.compact_if_needed(8)?;
        Ok(id)
    }


    pub fn scan_v(&self, v_e: i64) -> Result<Vec<VaetDatom>, IndexError> {
        let mut results: Vec<VaetDatom> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("vaet")? {
            if let Ok(root) = serde_json::from_slice::<VaetRootV1>(&root_bytes) {
                for sid in root.segments {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<VaetDatom>>(&bytes) {
                            let lo = lower_bound_v(datoms.as_slice(), v_e);
                            let mut i = lo;
                            while i < datoms.len() && datoms[i].v_e == v_e { results.push(datoms[i].clone()); i += 1; }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() { if d.v_e == v_e { results.push(d.clone()); } }
        results.sort_by(compare_datom_vaet);
        Ok(results)
    }

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

fn compare_datom_vaet(d1: &VaetDatom, d2: &VaetDatom) -> std::cmp::Ordering {
    use std::cmp::Ordering::*;
    match d1.v_e.cmp(&d2.v_e) {
        Equal => match attr_sort_key(&d1.a).cmp(&attr_sort_key(&d2.a)) { Equal => match d1.e.cmp(&d2.e) { Equal => d2.t.cmp(&d1.t), other => other }, other => other },
        other => other,
    }
}

fn lower_bound_v(datoms: &[VaetDatom], v_e: i64) -> usize {
    let mut lo = 0usize; let mut hi = datoms.len();
    while lo < hi { let mid = (lo + hi)/2; if datoms[mid].v_e < v_e { lo = mid+1; } else { hi = mid; } }
    lo
}

impl VaetIndexer {
    fn compact_if_needed(&mut self, max_segments: usize) -> Result<Option<String>, IndexError> {
        if let Some((rev, bytes)) = self.store.get_root("vaet")? {
            if let Ok(root) = serde_json::from_slice::<VaetRootV1>(&bytes) {
                if root.segments.len() > max_segments {
                    let mut all: Vec<VaetDatom> = Vec::new();
                    for sid in root.segments {
                        if let Some(seg) = self.store.get_segment(&sid)? {
                            let mut v: Vec<VaetDatom> = serde_json::from_slice(&seg)?;
                            all.append(&mut v);
                        }
                    }
                    all.sort_by(compare_datom_vaet);
                    let encoded = serde_json::to_vec(&all)?;
                    let mut hasher = Sha256::new(); hasher.update(&encoded);
                    let id = hex::encode(hasher.finalize());
                    let _ = self.store.put_segment_if_absent(&id, &encoded)?;
                    let new_root = VaetRootV1 { version: 1, segments: vec![id.clone()] };
                    let root_bytes = serde_json::to_vec(&new_root)?;
                    let _ = self.store.cas_root("vaet", rev, &root_bytes)?;
                    return Ok(Some(id));
                }
            }
        }
        Ok(None)
    }
}

// ---------------- AEVT (A, E, V, T) ----------------

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AevtDatom {
    pub a: String,
    pub e: i64,
    pub v_b64: String,
    pub t: i64,
    pub added: bool,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
struct AevtRootV1 { version: u8, segments: Vec<String> }

pub struct AevtIndexer {
    store: SqliteStore,
    memory: Vec<AevtDatom>,
    conn: Connection,
    merge_threshold: usize,
}

impl AevtIndexer {
    fn compact_if_needed(&mut self, max_segments: usize) -> Result<Option<String>, IndexError> {
        if let Some((rev, bytes)) = self.store.get_root("aevt")? {
            if let Ok(root) = serde_json::from_slice::<AevtRootV1>(&bytes) {
                if root.segments.len() > max_segments {
                    let mut all: Vec<AevtDatom> = Vec::new();
                    for sid in root.segments {
                        if let Some(seg) = self.store.get_segment(&sid)? {
                            let mut v: Vec<AevtDatom> = serde_json::from_slice(&seg)?;
                            all.append(&mut v);
                        }
                    }
                    all.sort_by(compare_datom_aevt);
                    let encoded = serde_json::to_vec(&all)?;
                    let mut hasher = Sha256::new(); hasher.update(&encoded);
                    let id = hex::encode(hasher.finalize());
                    let _ = self.store.put_segment_if_absent(&id, &encoded)?;
                    let new_root = AevtRootV1 { version: 1, segments: vec![id.clone()] };
                    let root_bytes = serde_json::to_vec(&new_root)?;
                    let _ = self.store.cas_root("aevt", rev, &root_bytes)?;
                    return Ok(Some(id));
                }
            }
        }
        Ok(None)
    }
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
            self.memory.push(AevtDatom { a: p.a.clone(), e: p.e, v_b64: general_purpose::STANDARD_NO_PAD.encode(v_bytes), t, added: p.added });
        }
        Ok(())
    }

    pub fn maybe_merge(&mut self) -> Result<Option<String>, IndexError> { if self.memory.len() >= self.merge_threshold { Ok(Some(self.merge()?)) } else { Ok(None) } }

    pub fn merge(&mut self) -> Result<String, IndexError> {
        self.memory.sort_by(compare_datom_aevt);
        let encoded = serde_json::to_vec(&self.memory)?;
        let mut hasher = Sha256::new(); hasher.update(&encoded);
        let id = hex::encode(hasher.finalize());
        let _ = self.store.put_segment_if_absent(&id, &encoded)?;
        let mut segments: Vec<String> = Vec::new();
        if let Some((_rev, root_bytes)) = self.store.get_root("aevt")? { if let Ok(root) = serde_json::from_slice::<AevtRootV1>(&root_bytes) { segments = root.segments; } }
        segments.push(id.clone());
        let root = AevtRootV1 { version: 1, segments };
        let root_bytes = serde_json::to_vec(&root)?;
        if let Ok(Some((rev,_))) = self.store.get_root("aevt") { let _ = self.store.cas_root("aevt", rev, &root_bytes)?; } else { self.store.init_root("aevt", &root_bytes)?; }
        self.memory.clear();
        let _ = self.compact_if_needed(8)?;
        Ok(id)
    }

    pub fn scan_a(&self, a: &str) -> Result<Vec<AevtDatom>, IndexError> {
        let mut results: Vec<AevtDatom> = Vec::new();
        let a_key = attr_sort_key(a);
        if let Some((_rev, root_bytes)) = self.store.get_root("aevt")? {
            if let Ok(root) = serde_json::from_slice::<AevtRootV1>(&root_bytes) {
                for sid in root.segments.clone() {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<AevtDatom>>(&bytes) {
                            let (lo, hi) = bounds_for_aevt_a(&datoms, &a_key);
                            let mut i = lo;
                            while i < hi { results.push(datoms[i].clone()); i += 1; }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() { if attr_sort_key(&d.a).as_slice() == a_key.as_slice() { results.push(d.clone()); } }
        results.sort_by(compare_datom_aevt);
        Ok(results)
    }

    pub fn scan_ae(&self, a: &str, e: i64) -> Result<Vec<AevtDatom>, IndexError> {
        let mut results: Vec<AevtDatom> = Vec::new();
        let a_key = attr_sort_key(a);
        if let Some((_rev, root_bytes)) = self.store.get_root("aevt")? {
            if let Ok(root) = serde_json::from_slice::<AevtRootV1>(&root_bytes) {
                for sid in root.segments.clone() {
                    if let Some(bytes) = self.store.get_segment(&sid)? {
                        if let Ok(datoms) = serde_json::from_slice::<Vec<AevtDatom>>(&bytes) {
                            let (lo, hi) = bounds_for_aevt_a(&datoms, &a_key);
                            if lo < hi {
                                let start = lower_bound_ae_in_range(&datoms[lo..hi], e) + lo;
                                let mut i = start;
                                while i < hi {
                                    let d = &datoms[i];
                                    if d.e != e { break; }
                                    results.push(d.clone());
                                    i += 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        for d in self.memory.iter() { if d.a == a && d.e == e { results.push(d.clone()); } }
        results.sort_by(compare_datom_aevt);
        Ok(results)
    }

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

fn compare_datom_aevt(d1: &AevtDatom, d2: &AevtDatom) -> std::cmp::Ordering {
    use std::cmp::Ordering::*;
    match attr_sort_key(&d1.a).cmp(&attr_sort_key(&d2.a)) {
        Equal => match d1.e.cmp(&d2.e) {
            Equal => {
                let v1 = general_purpose::STANDARD_NO_PAD.decode(d1.v_b64.as_bytes()).unwrap_or_default();
                let v2 = general_purpose::STANDARD_NO_PAD.decode(d2.v_b64.as_bytes()).unwrap_or_default();
                match v1.cmp(&v2) { Equal => d2.t.cmp(&d1.t), other => other }
            }
            other => other,
        },
        other => other,
    }
}

fn lower_bound_ae_in_range(datoms: &[AevtDatom], e: i64) -> usize {
    let mut lo = 0usize; let mut hi = datoms.len();
    while lo < hi { let mid = (lo + hi)/2; if datoms[mid].e < e { lo = mid + 1; } else { hi = mid; } }
    lo
}

fn bounds_for_aevt_a(datoms: &[AevtDatom], a_key: &[u8]) -> (usize, usize) {
    let mut lo = 0usize; let mut hi = datoms.len();
    while lo < hi {
        let mid = (lo + hi)/2;
        let mk = attr_sort_key(&datoms[mid].a);
        if mk.as_slice() < a_key { lo = mid + 1; } else { hi = mid; }
    }
    let start = lo;
    let mut end = start;
    while end < datoms.len() && attr_sort_key(&datoms[end].a).as_slice() == a_key { end += 1; }
    (start, end)
}
