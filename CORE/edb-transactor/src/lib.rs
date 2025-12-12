use rusqlite::{params, Connection, OptionalExtension};
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_encoding::ValueType;
use edb_index::EavtIndexer;
use edb_store_sqlite::{RootStore, SqliteStore, StoreError, LogStore};
use edb_tx::{allocator::TempResolver, normalize_grammar, model::Value, traits::DbView, validate::normalize_and_validate, TxOp, TxReport, UniquenessResult, TxFnRegistry};

#[derive(thiserror::Error, Debug)]
pub enum TxrError {
    #[error("store: {0}")]
    Store(#[from] StoreError),
    #[error("sqlite: {0}")]
    Sqlite(#[from] rusqlite::Error),
    #[error("tx: {0}")]
    Tx(#[from] edb_tx::validate::TxError),
    #[error("invalid schema: {0}")]
    InvalidSchema(String),
}

pub struct SqliteTransactor {
    pub store: SqliteStore,
    pub conn: Connection,
    subscribers: Vec<std::sync::mpsc::Sender<TxReport>>,
    eavt: Option<EavtIndexer>,
    aevt: Option<edb_index::AevtIndexer>,
    avet: Option<edb_index::AvetIndexer>,
    vaet: Option<edb_index::VaetIndexer>,
    tx_fns: TxFnRegistry,
    // Merge/compaction latency accumulators (ms)
    merges_ms_total: u64,
    merges_events: u64,
    compactions_ms_total: u64,
    compactions_events: u64,
}

pub enum DbViewKind {
    Current,
    AsOf(i64),
    Since(i64),
    History,
}

pub struct Db<'a> {
    txr: &'a SqliteTransactor,
    kind: DbViewKind,
}

impl<'a> Db<'a> {
    pub fn entity_attrs(&self, e: i64) -> Result<Vec<(String, Value)>, TxrError> {
        match self.kind {
            DbViewKind::Current => {
                let dbv = SqliteDbView { conn: &self.txr.conn };
                Ok(edb_tx::traits::DbView::entity_attrs(&dbv, e))
            }
            DbViewKind::AsOf(t) => self.txr.as_of_entity_attrs(e, t),
            DbViewKind::Since(t) => self.txr.since_entity_attrs(e, t),
            DbViewKind::History => {
                // For history views, entity_attrs is not well-defined; return current as a reasonable default
                let dbv = SqliteDbView { conn: &self.txr.conn };
                Ok(edb_tx::traits::DbView::entity_attrs(&dbv, e))
            }
        }
    }

    pub fn history_entity(&self, e: i64) -> Result<Vec<edb_tx::model::TxPrimitive>, TxrError> {
        match self.kind {
            DbViewKind::History => self.txr.history_entity(e),
            DbViewKind::AsOf(_) | DbViewKind::Since(_) | DbViewKind::Current => self.txr.history_entity(e),
        }
    }
}

#[derive(Debug, Clone, Copy)]
pub struct IndexStats {
    pub eavt: usize,
    pub aevt: usize,
    pub avet: usize,
    pub vaet: usize,
    pub merges_ms_total: u64,
    pub merges_events: u64,
    pub compactions_ms_total: u64,
    pub compactions_events: u64,
}

impl SqliteTransactor {
    pub fn open(path: &str) -> Result<Self, TxrError> {
        let store = SqliteStore::open(path)?;
        let conn = Connection::open(path)?;
        let eavt = EavtIndexer::open(path).ok();
        let aevt = edb_index::AevtIndexer::open(path).ok();
        let avet = edb_index::AvetIndexer::open(path).ok();
        let vaet = edb_index::VaetIndexer::open(path).ok();
        let txr = Self { store, conn, subscribers: Vec::new(), eavt, aevt, avet, vaet, tx_fns: TxFnRegistry::new(), merges_ms_total: 0, merges_events: 0, compactions_ms_total: 0, compactions_events: 0 };
        txr.init_local_tables()?;
        Ok(txr)
    }

    fn init_local_tables(&self) -> Result<(), TxrError> {
        self.conn.execute_batch(
            r#"
            CREATE TABLE IF NOT EXISTS attrs(
              ident TEXT PRIMARY KEY,
              vt INTEGER NOT NULL,
              card INTEGER NOT NULL,
              uniq INTEGER NOT NULL,
              is_component INTEGER NOT NULL,
              no_history INTEGER NOT NULL,
              doc TEXT
            );
            CREATE TABLE IF NOT EXISTS current(
              e INTEGER NOT NULL,
              a TEXT NOT NULL,
              vjson TEXT NOT NULL,
              PRIMARY KEY(e,a)
            );
            CREATE TABLE IF NOT EXISTS unique_idx(
              a TEXT NOT NULL,
              vkey TEXT NOT NULL,
              e INTEGER NOT NULL,
              PRIMARY KEY(a, vkey)
            );
            CREATE TABLE IF NOT EXISTS meta(
              k TEXT PRIMARY KEY,
              v TEXT NOT NULL
            );
            INSERT OR IGNORE INTO meta(k,v) VALUES('next_e','1000');
            INSERT OR IGNORE INTO meta(k,v) VALUES('last_tx_instant','0');
            CREATE TABLE IF NOT EXISTS aliases(
              alias TEXT PRIMARY KEY,
              target TEXT NOT NULL
            );
            -- Envelope storage (linear mode)
            CREATE TABLE IF NOT EXISTS tx_envelopes(
              tx_id BLOB PRIMARY KEY,
              unsigned BLOB NOT NULL,
              sig BLOB NOT NULL,
              author_pk BLOB NOT NULL,
              authored_at INTEGER,
              applied INTEGER NOT NULL DEFAULT 0
            );
            CREATE TABLE IF NOT EXISTS tx_edges(
              child BLOB NOT NULL,
              parent BLOB NOT NULL
            );
            CREATE TABLE IF NOT EXISTS heads(
              tx_id BLOB PRIMARY KEY
            );
            "#,
        )?;
        Ok(())
    }

    pub fn register_tx_fn(&mut self, ident: &str, f: Box<dyn edb_tx::TxFunction + Send + Sync>) {
        self.tx_fns.insert(ident.to_string(), f);
    }

    /// Return counts of index segments and cumulative latency for observability.
    pub fn index_stats(&self) -> Result<IndexStats, TxrError> {
        #[derive(serde::Deserialize)]
        struct RootV1 { version: u8, segments: Vec<String> }
        fn count_segments(conn: &edb_store_sqlite::SqliteStore, name: &str) -> usize {
            match conn.get_root(name) {
                Ok(Some((_rev, bytes))) => serde_json::from_slice::<RootV1>(&bytes).map(|r| r.segments.len()).unwrap_or(0),
                _ => 0,
            }
        }
        Ok(IndexStats {
            eavt: count_segments(&self.store, "eavt"),
            aevt: count_segments(&self.store, "aevt"),
            avet: count_segments(&self.store, "avet"),
            vaet: count_segments(&self.store, "vaet"),
            merges_ms_total: self.merges_ms_total,
            merges_events: self.merges_events,
            compactions_ms_total: self.compactions_ms_total,
            compactions_events: self.compactions_events,
        })
    }

    // DB-level constructors for richer API
    pub fn db<'a>(&'a self) -> Db<'a> { Db { txr: self, kind: DbViewKind::Current } }
    pub fn as_of<'a>(&'a self, t: i64) -> Db<'a> { Db { txr: self, kind: DbViewKind::AsOf(t) } }
    pub fn since<'a>(&'a self, t: i64) -> Db<'a> { Db { txr: self, kind: DbViewKind::Since(t) } }
    pub fn history_db<'a>(&'a self) -> Db<'a> { Db { txr: self, kind: DbViewKind::History } }

    pub fn install_attribute(&self, attr: &Attribute) -> Result<(), TxrError> {
        // Enforce spec: :db.type/bytes is equality-only; cannot be unique or used for lookup refs.
        if matches!(attr.value_type, ValueType::Bytes) && !matches!(attr.unique, AttrUnique::None) {
            return Err(TxrError::InvalidSchema("bytes attributes cannot be unique or used in lookup refs".into()));
        }
        self.conn.execute(
            "INSERT OR REPLACE INTO attrs(ident,vt,card,uniq,is_component,no_history,doc) VALUES(?,?,?,?,?,?,?)",
            params![
                &attr.ident,
                attr.value_type as i64,
                match attr.cardinality { AttrCardinality::One => 1i64, AttrCardinality::Many => 2i64 },
                match attr.unique { AttrUnique::None => 0i64, AttrUnique::Identity => 1i64, AttrUnique::Value => 2i64 },
                if attr.is_component {1i64} else {0i64},
                if attr.no_history {1i64} else {0i64},
                &attr.doc
            ],
        )?;
        Ok(())
    }

    /// Install an alias mapping from `alias` to existing attribute `target`.
    pub fn install_alias(&self, alias: &str, target: &str) -> Result<(), TxrError> {
        if alias == target { return Err(TxrError::InvalidSchema("alias equals target".into())); }
        // Ensure target exists
        let exists: Option<i64> = self.conn.query_row("SELECT 1 FROM attrs WHERE ident=?1", params![target], |r| r.get(0)).optional()?;
        if exists.is_none() { return Err(TxrError::InvalidSchema("target ident does not exist".into())); }
        // Ensure alias is not already a real ident
        let exists_alias: Option<i64> = self.conn.query_row("SELECT 1 FROM attrs WHERE ident=?1", params![alias], |r| r.get(0)).optional()?;
        if exists_alias.is_some() { return Err(TxrError::InvalidSchema("alias collides with existing ident".into())); }
        self.conn.execute(
            "INSERT OR REPLACE INTO aliases(alias,target) VALUES(?1,?2)",
            params![alias, target],
        )?;
        Ok(())
    }

    fn next_entid(&self) -> Result<i64, TxrError> {
        let cur: String = self
            .conn
            .query_row("SELECT v FROM meta WHERE k='next_e'", [], |r| r.get(0))?;
        let mut x: i64 = cur.parse().unwrap_or(1000);
        let ret = x;
        x += 1;
        self.conn.execute("UPDATE meta SET v=?1 WHERE k='next_e'", params![x.to_string()])?;
        Ok(ret)
    }

    pub fn apply_tx(&mut self, ops_json: &[serde_json::Value]) -> Result<TxReport, TxrError> {
        // Build and validate outside the write transaction to avoid borrow conflicts
        let (ops, meta_json) = {
            let dbview = SqliteDbView { conn: &self.conn };
            let mut temps = TempResolver::new();
            let mut alloc = SqliteAllocator { txr: self };
            let normalized = normalize_grammar(&dbview, &mut alloc, &mut temps, ops_json, Some(&self.tx_fns));
            let ops: Vec<TxOp> = normalized.ops;
            let mut alloc2 = SqliteAllocator { txr: self };
            let _rep = normalize_and_validate(&dbview, &ops, &mut alloc2)?;
            (ops, normalized.meta)
        };
        let dbview = SqliteDbView { conn: &self.conn };
        let mut alloc2 = SqliteAllocator { txr: self };
        let report = normalize_and_validate(&dbview, &ops, &mut alloc2)?;
        // Allocate a tx entity id before starting the write tx to avoid borrow conflicts
        let tx_eid = self.next_entid()?;
        let tx = self.conn.transaction()?;
        // Compute txInstant (monotonic micros)
        let last_inst_str: String = tx.query_row("SELECT v FROM meta WHERE k='last_tx_instant'", [], |r| r.get(0))?;
        let last_inst: i64 = last_inst_str.parse().unwrap_or(0);
        let mut tx_inst = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as i64;
        if let Some(ref m) = meta_json {
            if let Some(n) = m.get("txInstant").and_then(|j| j.as_i64()) { tx_inst = n; }
        }
        if tx_inst <= last_inst { tx_inst = last_inst + 1; }
        // Update last_tx_instant
        tx.execute("UPDATE meta SET v=?1 WHERE k='last_tx_instant'", params![tx_inst.to_string()])?;
        // Apply primitives to current and unique_idx
        for p in &report.primitives {
            if p.added {
                let vjson = serde_json::to_string(&p.v).unwrap();
                tx.execute(
                    "INSERT OR REPLACE INTO current(e,a,vjson) VALUES(?,?,?)",
                    params![p.e, &p.a, vjson],
                )?;
                if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                    tx.execute(
                        "INSERT OR REPLACE INTO unique_idx(a,vkey,e) VALUES(?,?,?)",
                        params![a, vkey, p.e],
                    )?;
                }
            } else {
                // retract
                tx.execute("DELETE FROM current WHERE e=?1 AND a=?2", params![p.e, &p.a])?;
                if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                    tx.execute("DELETE FROM unique_idx WHERE a=?1 AND vkey=?2", params![a, vkey])?;
                }
            }
        }
        // Append to log within the same transaction for atomicity
        // Write log entry including meta and tx_eid
        #[derive(serde::Serialize)]
        struct TxLogEntry<'a> { primitives: &'a [edb_tx::model::TxPrimitive], meta: serde_json::Value, tx_eid: i64, tx_instant: i64 }
        let meta_obj = meta_json.clone().unwrap_or_else(|| serde_json::json!({}));
        let body = serde_json::to_vec(&TxLogEntry { primitives: &report.primitives, meta: meta_obj, tx_eid, tx_instant: tx_inst }).unwrap();
        tx.execute("INSERT INTO log(val) VALUES(?1)", params![body])?;
        // Get t for this connection
        let t: i64 = tx.query_row("SELECT last_insert_rowid()", [], |r| r.get(0))?;
        tx.commit()?;

        // CAS root update after commit (single-writer; race unlikely)
        let head = self.store.get_root("head")?;
        let (rev, _) = if let Some((rev, val)) = head { (rev, val) } else { self.store.init_root("head", br#"{"t":0}"#)?; (self.store.get_root("head")?.unwrap().0, vec![]) };
        let new_head = serde_json::to_vec(&serde_json::json!({"t": t})).unwrap();
        let _ = self.store.cas_root("head", rev, &new_head)?;

        let mut rep = report;
        rep.t = Some(t);
        rep.tx_eid = Some(tx_eid);
        rep.meta = meta_json;
        // Update indexes and measure merge/compaction latency
        #[derive(serde::Deserialize)]
        struct RootV1 { version: u8, segments: Vec<String> }
        let count_segments = |name: &str| -> usize {
            match self.store.get_root(name) {
                Ok(Some((_rev, bytes))) => serde_json::from_slice::<RootV1>(&bytes).map(|r| r.segments.len()).unwrap_or(0),
                _ => 0,
            }
        };
        use std::time::Instant;
        if let Some(idx) = self.eavt.as_mut() {
            let _ = idx.apply_primitives(&rep.primitives, t);
            let before = count_segments("eavt");
            let start = Instant::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments("eavt");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.aevt.as_mut() {
            let _ = idx.apply_primitives(&rep.primitives, t);
            let before = count_segments("aevt");
            let start = Instant::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments("aevt");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.avet.as_mut() {
            let _ = idx.apply_primitives(&rep.primitives, t);
            let before = count_segments("avet");
            let start = Instant::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments("avet");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.vaet.as_mut() {
            let _ = idx.apply_primitives(&rep.primitives, t);
            let before = count_segments("vaet");
            let start = Instant::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments("vaet");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        // Broadcast to subscribers; remove dead ones
        self.subscribers.retain(|tx| tx.send(rep.clone()).is_ok());
        Ok(rep)
    }

    /// Submit a signed CBOR envelope and apply it (linear mode).
    pub fn submit_envelope(&mut self, unsigned: &[u8], sig: &[u8]) -> Result<TxReport, TxrError> {
        // Decode envelope to inspect parents/features/author/tx_body
        #[allow(dead_code)]
        #[derive(serde::Deserialize)]
        struct EnvV1 {
            magic: String,
            version: u8,
            parents: Vec<serde_bytes::ByteBuf>,
            features: Vec<String>,
            author_pubkey: serde_bytes::ByteBuf,
            authored_at: Option<u64>,
            tx_body: serde_cbor::Value,
        }
        // Use serde_cbor for quick decode to Value for this path
        let env: EnvV1 = serde_cbor::from_slice(unsigned).map_err(|e| TxrError::InvalidSchema(format!("bad envelope: {e}")))?;
        if env.magic != "edb.tx" || env.version != 1 { return Err(TxrError::InvalidSchema("unsupported envelope".into())); }
        // Feature gating (MVP): reject any unknown/declared features for now
        if !env.features.is_empty() { return Err(TxrError::InvalidSchema("unknown/unsupported feature".into())); }
        // Verify signature
        if !edb_envelope::verify(unsigned, sig, env.author_pubkey.as_ref()) { return Err(TxrError::InvalidSchema("bad signature".into())); }
        // Compute tx_id
        let tx_id = edb_envelope::tx_id(unsigned);
        // Linear mode: parent must match current head (or genesis if no heads)
        let heads: Vec<Vec<u8>> = {
            let mut out = Vec::new();
            let mut stmt = self.conn.prepare("SELECT tx_id FROM heads")?;
            let rows = stmt.query_map([], |r| r.get::<_, Vec<u8>>(0))?;
            for row in rows { out.push(row?); }
            out
        };
        if heads.is_empty() {
            if !env.parents.is_empty() {
                return Err(TxrError::InvalidSchema("non-genesis envelope has no current head".into()));
            }
        } else {
            if env.parents.len() != 1 || env.parents[0].as_ref() != heads[0].as_slice() {
                return Err(TxrError::InvalidSchema("parent mismatch (linear mode)".into()));
            }
        }
        // Store envelope and edge
        self.conn.execute(
            "INSERT OR IGNORE INTO tx_envelopes(tx_id,unsigned,sig,author_pk,authored_at,applied) VALUES(?1,?2,?3,?4,?5,0)",
            rusqlite::params![&tx_id[..], unsigned, sig, env.author_pubkey.as_ref(), env.authored_at.map(|x| x as i64)],
        )?;
        if let Some(parent) = env.parents.get(0) {
            self.conn.execute(
                "INSERT INTO tx_edges(child,parent) VALUES(?1,?2)",
                rusqlite::params![&tx_id[..], parent.as_ref()],
            )?;
            // Remove parent from heads
            self.conn.execute("DELETE FROM heads WHERE tx_id=?1", rusqlite::params![parent.as_ref()])?;
        }
        // Add new head
        self.conn.execute("INSERT OR REPLACE INTO heads(tx_id) VALUES(?1)", rusqlite::params![&tx_id[..]])?;

        // Decode tx_body into TxOps
        // We encoded tx_body as an array of arrays [op, ...]. Here we accept only add/retract/cas/retract-entity.
        let mut ops: Vec<edb_tx::model::TxOp> = Vec::new();
        let cbor = env.tx_body;
        let arr = match cbor { serde_cbor::Value::Array(v) => v, _ => return Err(TxrError::InvalidSchema("tx_body must be array".into())) };
        for item in arr {
            let row = match item { serde_cbor::Value::Array(v) => v, _ => return Err(TxrError::InvalidSchema("op must be array".into())) };
            if row.is_empty() { continue; }
            let tag = match &row[0] { serde_cbor::Value::Text(s) => s.clone(), _ => return Err(TxrError::InvalidSchema("op tag must be text".into())) };
            match tag.as_str() {
                "add" => {
                    if row.len() != 4 { return Err(TxrError::InvalidSchema("add len".into())); }
                    let e = cbor_to_entity_ref(&row[1]);
                    let a = expect_text(&row[2])?;
                    let v: edb_tx::model::Value = serde_cbor::value::from_value(row[3].clone()).map_err(|_| TxrError::InvalidSchema("bad v".into()))?;
                    ops.push(edb_tx::model::TxOp::Add { e, a, v });
                }
                "retract" => {
                    if row.len() != 4 { return Err(TxrError::InvalidSchema("retract len".into())); }
                    let e = cbor_to_entity_ref(&row[1]);
                    let a = expect_text(&row[2])?;
                    let v = if matches!(row[3], serde_cbor::Value::Null) { None } else { Some(serde_cbor::value::from_value(row[3].clone()).map_err(|_| TxrError::InvalidSchema("bad v".into()))?) };
                    ops.push(edb_tx::model::TxOp::Retract { e, a, v });
                }
                "cas" => {
                    if row.len() != 5 { return Err(TxrError::InvalidSchema("cas len".into())); }
                    let e = cbor_to_entity_ref(&row[1]);
                    let a = expect_text(&row[2])?;
                    let expected = if matches!(row[3], serde_cbor::Value::Null) { None } else { Some(serde_cbor::value::from_value(row[3].clone()).map_err(|_| TxrError::InvalidSchema("bad expected".into()))?) };
                    let v: edb_tx::model::Value = serde_cbor::value::from_value(row[4].clone()).map_err(|_| TxrError::InvalidSchema("bad v".into()))?;
                    ops.push(edb_tx::model::TxOp::Cas { e, a, expected, v });
                }
                "retract-entity" => {
                    if row.len() != 2 { return Err(TxrError::InvalidSchema("retract-entity len".into())); }
                    // Expand later via grammar; for now, model as retract all attrs via DbView in normalization/validate path
                    let _e = cbor_to_entity_ref(&row[1]);
                    // Represent as a grammar op that validate can understand by invoking cascade later.
                    // Push a special retract with None attribute to signal cascade (handled through grammar earlier); omit here.
                    // For now, just ignore; clients should prefer explicit retracts.
                }
                _ => return Err(TxrError::InvalidSchema("unknown op".into())),
            }
        }
        // Validate and apply (reuse apply_tx core write)
        let dbview = SqliteDbView { conn: &self.conn };
        let mut alloc = SqliteAllocator { txr: self };
        let report = normalize_and_validate(&dbview, &ops, &mut alloc)?;
        // Apply primitives and log (reuse inner portion of apply_tx)
        let tx_eid = self.next_entid()?;
        let tx = self.conn.transaction()?;
        // txInstant monotonic; use authored_at if provided
        let last_inst_str: String = tx.query_row("SELECT v FROM meta WHERE k='last_tx_instant'", [], |r| r.get(0))?;
        let last_inst: i64 = last_inst_str.parse().unwrap_or(0);
        let mut tx_inst = env.authored_at.map(|x| x as i64).unwrap_or_else(|| {
            std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros() as i64
        });
        if tx_inst <= last_inst { tx_inst = last_inst + 1; }
        tx.execute("UPDATE meta SET v=?1 WHERE k='last_tx_instant'", rusqlite::params![tx_inst.to_string()])?;
        for p in &report.primitives {
            if p.added {
                let vjson = serde_json::to_string(&p.v).unwrap();
                tx.execute("INSERT OR REPLACE INTO current(e,a,vjson) VALUES(?,?,?)", rusqlite::params![p.e, &p.a, vjson])?;
                if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                    tx.execute("INSERT OR REPLACE INTO unique_idx(a,vkey,e) VALUES(?,?,?)", rusqlite::params![a, vkey, p.e])?;
                }
            } else {
                tx.execute("DELETE FROM current WHERE e=?1 AND a=?2", rusqlite::params![p.e, &p.a])?;
                if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                    tx.execute("DELETE FROM unique_idx WHERE a=?1 AND vkey=?2", rusqlite::params![a, vkey])?;
                }
            }
        }
        // Log JSON body for replay compatibility (temporary)
        #[derive(serde::Serialize)]
        struct TxLogEntry<'a> { primitives: &'a [edb_tx::model::TxPrimitive], meta: serde_json::Value, tx_eid: i64, tx_instant: i64 }
        let body = serde_json::to_vec(&TxLogEntry { primitives: &report.primitives, meta: serde_json::json!({}), tx_eid, tx_instant: tx_inst }).unwrap();
        tx.execute("INSERT INTO log(val) VALUES(?1)", rusqlite::params![body])?;
        let t: i64 = tx.query_row("SELECT last_insert_rowid()", [], |r| r.get(0))?;
        tx.commit()?;
        // Mark envelope applied
        self.conn.execute("UPDATE tx_envelopes SET applied=1 WHERE tx_id=?1", rusqlite::params![&tx_id[..]])?;
        // Update head root for demo
        let head = self.store.get_root("head")?;
        let (rev, _) = if let Some((rev, val)) = head { (rev, val) } else { self.store.init_root("head", br#"{"t":0}"#)?; (self.store.get_root("head")?.unwrap().0, vec![]) };
        let new_head = serde_json::to_vec(&serde_json::json!({"t": t})).unwrap();
        let _ = self.store.cas_root("head", rev, &new_head)?;
        // EAVT/AEVT/AVET/VAET apply/merge with latency accounting
        #[derive(serde::Deserialize)]
        struct RootV1B { version: u8, segments: Vec<String> }
        let count_segments_b = |name: &str| -> usize {
            match self.store.get_root(name) {
                Ok(Some((_rev, bytes))) => serde_json::from_slice::<RootV1B>(&bytes).map(|r| r.segments.len()).unwrap_or(0),
                _ => 0,
            }
        };
        use std::time::Instant as InstantB;
        if let Some(idx) = self.eavt.as_mut() {
            let _ = idx.apply_primitives(&report.primitives, t);
            let before = count_segments_b("eavt");
            let start = InstantB::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments_b("eavt");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.aevt.as_mut() {
            let _ = idx.apply_primitives(&report.primitives, t);
            let before = count_segments_b("aevt");
            let start = InstantB::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments_b("aevt");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.avet.as_mut() {
            let _ = idx.apply_primitives(&report.primitives, t);
            let before = count_segments_b("avet");
            let start = InstantB::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments_b("avet");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        if let Some(idx) = self.vaet.as_mut() {
            let _ = idx.apply_primitives(&report.primitives, t);
            let before = count_segments_b("vaet");
            let start = InstantB::now();
            if idx.maybe_merge().ok().flatten().is_some() {
                let dur = start.elapsed().as_millis() as u64;
                let after = count_segments_b("vaet");
                if after < before { self.compactions_ms_total += dur; self.compactions_events += 1; } else { self.merges_ms_total += dur; self.merges_events += 1; }
            }
        }
        let mut rep = report;
        rep.t = Some(t);
        rep.tx_eid = Some(tx_eid);
        // Broadcast
        self.subscribers.retain(|tx| tx.send(rep.clone()).is_ok());
        Ok(rep)
    }

    pub fn subscribe(&mut self) -> std::sync::mpsc::Receiver<TxReport> {
        let (tx, rx) = std::sync::mpsc::channel();
        self.subscribers.push(tx);
        rx
    }

    pub fn replay_current_from_log(&mut self) -> Result<(), TxrError> {
        let entries = self.store.read_log_range(1, i64::MAX)?;
        let tx = self.conn.transaction()?;
        tx.execute("DELETE FROM current", [])?;
        tx.execute("DELETE FROM unique_idx", [])?;
        for (_seq, bytes) in entries {
            // Support both legacy Vec<TxPrimitive> and new TxLogEntry
            #[derive(serde::Deserialize)]
            struct TxLogEntry { primitives: Vec<edb_tx::model::TxPrimitive> }
            let primitives: Vec<edb_tx::model::TxPrimitive> = match serde_json::from_slice::<TxLogEntry>(&bytes) {
                Ok(e) => e.primitives,
                Err(_) => serde_json::from_slice::<Vec<edb_tx::model::TxPrimitive>>(&bytes).unwrap_or_default(),
            };
            for p in primitives.into_iter() {
                if p.added {
                    let vjson = serde_json::to_string(&p.v).unwrap();
                    tx.execute("INSERT OR REPLACE INTO current(e,a,vjson) VALUES(?,?,?)", params![p.e, &p.a, vjson])?;
                    if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                        tx.execute("INSERT OR REPLACE INTO unique_idx(a,vkey,e) VALUES(?,?,?)", params![a, vkey, p.e])?;
                    }
                } else {
                    tx.execute("DELETE FROM current WHERE e=?1 AND a=?2", params![p.e, &p.a])?;
                    if let Some((a, vkey)) = unique_key_for_tx(&tx, &p.a, &p.v) {
                        tx.execute("DELETE FROM unique_idx WHERE a=?1 AND vkey=?2", params![a, vkey])?;
                    }
                }
            }
        }
        tx.commit()?;
        Ok(())
    }

    pub fn as_of_entity_attrs(&self, e: i64, t: i64) -> Result<Vec<(String, edb_tx::model::Value)>, TxrError> {
        let entries = self.store.read_log_range(1, t + 1)?;
        use std::collections::HashMap;
        let mut cur: HashMap<String, edb_tx::model::Value> = HashMap::new();
        for (_seq, bytes) in entries {
            #[derive(serde::Deserialize)]
            struct TxLogEntry { primitives: Vec<edb_tx::model::TxPrimitive> }
            let primitives: Vec<edb_tx::model::TxPrimitive> = match serde_json::from_slice::<TxLogEntry>(&bytes) {
                Ok(e) => e.primitives,
                Err(_) => serde_json::from_slice::<Vec<edb_tx::model::TxPrimitive>>(&bytes).unwrap_or_default(),
            };
            for p in primitives.into_iter() {
                if p.e != e { continue; }
                if p.added {
                    cur.insert(p.a.clone(), p.v.clone());
                } else {
                    cur.remove(&p.a);
                }
            }
        }
        Ok(cur.into_iter().collect())
    }

    /// Since view: returns the attribute map for entity `e` considering only datoms
    /// added by transactions with t > `since_t` (simple helper for consumers/tests).
    pub fn since_entity_attrs(&self, e: i64, since_t: i64) -> Result<Vec<(String, edb_tx::model::Value)>, TxrError> {
        let entries = self.store.read_log_range(since_t + 1, i64::MAX)?;
        use std::collections::HashMap;
        let mut cur: HashMap<String, edb_tx::model::Value> = HashMap::new();
        for (_seq, bytes) in entries {
            #[derive(serde::Deserialize)]
            struct TxLogEntry { primitives: Vec<edb_tx::model::TxPrimitive> }
            let primitives: Vec<edb_tx::model::TxPrimitive> = match serde_json::from_slice::<TxLogEntry>(&bytes) {
                Ok(e) => e.primitives,
                Err(_) => serde_json::from_slice::<Vec<edb_tx::model::TxPrimitive>>(&bytes).unwrap_or_default(),
            };
            for p in primitives.into_iter() {
                if p.e != e { continue; }
                if p.added { cur.insert(p.a.clone(), p.v.clone()); } else { cur.remove(&p.a); }
            }
        }
        Ok(cur.into_iter().collect())
    }

    /// History view: returns all primitives (assertions and retractions) about `e`,
    /// in transaction order.
    pub fn history_entity(&self, e: i64) -> Result<Vec<edb_tx::model::TxPrimitive>, TxrError> {
        let entries = self.store.read_log_range(1, i64::MAX)?;
        let mut out: Vec< edb_tx::model::TxPrimitive > = Vec::new();
        for (_seq, bytes) in entries {
            #[derive(serde::Deserialize)]
            struct TxLogEntry { primitives: Vec<edb_tx::model::TxPrimitive> }
            let primitives: Vec<edb_tx::model::TxPrimitive> = match serde_json::from_slice::<TxLogEntry>(&bytes) {
                Ok(e) => e.primitives,
                Err(_) => serde_json::from_slice::<Vec<edb_tx::model::TxPrimitive>>(&bytes).unwrap_or_default(),
            };
            for p in primitives.into_iter() { if p.e == e { out.push(p); } }
        }
        Ok(out)
    }
}

struct SqliteDbView<'a> {
    conn: &'a Connection,
}

impl<'a> DbView for SqliteDbView<'a> {
    fn lookup_by_unique(&self, attr_ident: &str, v: &Value) -> Option<i64> {
        let vkey = value_key(v);
        self.conn
            .query_row(
                "SELECT e FROM unique_idx WHERE a=?1 AND vkey=?2",
                params![attr_ident, vkey],
                |r| r.get::<_, i64>(0),
            )
            .optional()
            .ok()
            .flatten()
    }
    fn current_value(&self, e: i64, attr_ident: &str) -> Option<Value> {
        self.conn
            .query_row(
                "SELECT vjson FROM current WHERE e=?1 AND a=?2",
                params![e, attr_ident],
                |r| r.get::<_, String>(0),
            )
            .optional()
            .ok()
            .flatten()
            .map(|s| serde_json::from_str::<Value>(&s).unwrap())
    }
    fn uniqueness_check(&self, attr_ident: &str, v: &Value) -> UniquenessResult {
        match self.lookup_by_unique(attr_ident, v) {
            Some(e) => UniquenessResult::Present(e),
            None => UniquenessResult::Absent,
        }
    }
    fn get_attr(&self, ident: &str) -> Option<Attribute> {
        // Resolve alias to target ident if needed
        let target: Option<String> = self
            .conn
            .query_row(
                "SELECT ident FROM attrs WHERE ident=?1 UNION SELECT target FROM aliases WHERE alias=?1 LIMIT 1",
                params![ident],
                |r| r.get(0),
            )
            .optional()
            .ok()
            .flatten();
        let ident_q = target.as_deref().unwrap_or(ident);
        self.conn
            .query_row(
                "SELECT ident,vt,card,uniq,is_component,no_history,doc FROM attrs WHERE ident=?1",
                params![ident_q],
                |r| {
                    let ident: String = r.get(0)?;
                    let vt_i: i64 = r.get(1)?;
                    let vt = match vt_i {
                        1 => edb_encoding::ValueType::Long,
                        2 => edb_encoding::ValueType::Double,
                        3 => edb_encoding::ValueType::Boolean,
                        4 => edb_encoding::ValueType::String,
                        5 => edb_encoding::ValueType::Keyword,
                        6 => edb_encoding::ValueType::Uuid,
                        7 => edb_encoding::ValueType::Instant,
                        8 => edb_encoding::ValueType::Ref,
                        9 => edb_encoding::ValueType::Bytes,
                        10 => edb_encoding::ValueType::Uint8,
                        11 => edb_encoding::ValueType::Bigint,
                        12 => edb_encoding::ValueType::Decimal,
                        13 => edb_encoding::ValueType::Float32,
                        14 => edb_encoding::ValueType::Float16,
                        15 => edb_encoding::ValueType::Bfloat16,
                        _ => edb_encoding::ValueType::String,
                    };
                    let card_i: i64 = r.get(2)?;
                    let uniq_i: i64 = r.get(3)?;
                    let is_comp: i64 = r.get(4)?;
                    let no_hist: i64 = r.get(5)?;
                    let doc: Option<String> = r.get(6)?;
                    Ok(Attribute {
                        ident,
                        value_type: vt,
                        cardinality: if card_i == 1 { AttrCardinality::One } else { AttrCardinality::Many },
                        unique: match uniq_i { 1 => AttrUnique::Identity, 2 => AttrUnique::Value, _ => AttrUnique::None },
                        is_component: is_comp == 1,
                        no_history: no_hist == 1,
                        doc,
                        aliases: vec![],
                    })
                },
            )
            .optional()
            .ok()
            .flatten()
    }

    fn entity_attrs(&self, e: i64) -> Vec<(String, Value)> {
        let mut out = Vec::new();
        if let Ok(mut stmt) = self.conn.prepare("SELECT a, vjson FROM current WHERE e=?1") {
            if let Ok(rows) = stmt.query_map(params![e], |r| Ok((r.get::<_, String>(0)?, r.get::<_, String>(1)?))) {
                for row in rows.flatten() {
                    if let Ok(v) = serde_json::from_str::<Value>(&row.1) { out.push((row.0, v)); }
                }
            }
        }
        out
    }
}

fn value_key(v: &Value) -> String {
    match v {
        Value::Long(x) => format!("L:{}", x),
        Value::Double(x) => format!("D:{:?}", x),
        Value::Boolean(b) => format!("B:{}", b),
        Value::String(s) => format!("S:{}", s),
        Value::Keyword(s) => format!("K:{}", s),
        Value::Uuid(u) => format!("U:{}", u),
        Value::Instant(x) => format!("I:{}", x),
        Value::Ref(x) => format!("R:{}", x),
        Value::Bytes(b) => format!("X:{}", b.len()),
        Value::Uint8(x) => format!("U8:{}", x),
        Value::Bigint(s) => format!("BI:{}", s),
        Value::Decimal(s) => format!("BD:{}", s),
    }
}

// unique_key_for_tx: unique key lookup within an active transaction

fn unique_key_for_tx(tx: &rusqlite::Transaction<'_>, a: &str, v: &Value) -> Option<(String, String)> {
    // Read unique flag directly within the active transaction
    let uniq_i: Option<i64> = tx
        .query_row("SELECT uniq FROM attrs WHERE ident=?1", params![a], |r| r.get(0))
        .optional()
        .ok()
        .flatten();
    if matches!(uniq_i, Some(1 | 2)) { Some((a.to_string(), value_key(v))) } else { None }
}

struct SqliteAllocator<'a> {
    txr: &'a SqliteTransactor,
}

impl<'a> edb_tx::allocator::EntidAllocator for SqliteAllocator<'a> {
    fn allocate(&mut self) -> i64 { self.txr.next_entid().unwrap() }
}

fn expect_text(v: &serde_cbor::Value) -> Result<String, TxrError> {
    match v { serde_cbor::Value::Text(s) => Ok(s.clone()), _ => Err(TxrError::InvalidSchema("expected text".into())) }
}

fn cbor_to_entity_ref(v: &serde_cbor::Value) -> edb_tx::model::EntityRef {
    match v {
        serde_cbor::Value::Integer(i) => edb_tx::model::EntityRef::Entid(i128::from(*i) as i64),
        serde_cbor::Value::Text(s) => edb_tx::model::EntityRef::TempId(edb_tx::model::TempId(s.clone())),
        serde_cbor::Value::Array(arr) => {
            if arr.len() == 3 {
                if let serde_cbor::Value::Text(tag) = &arr[0] {
                    if tag == "lookup" {
                        let a = expect_text(&arr[1]).unwrap_or_default();
                        let val: edb_tx::model::Value = serde_cbor::value::from_value(arr[2].clone()).unwrap();
                        return edb_tx::model::EntityRef::LookupRef { attr: a, value: val };
                    }
                }
            }
            edb_tx::model::EntityRef::TempId(edb_tx::model::TempId("unhandled".into()))
        }
        _ => edb_tx::model::EntityRef::TempId(edb_tx::model::TempId("unhandled".into())),
    }
}
