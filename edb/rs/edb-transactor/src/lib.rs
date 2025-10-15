use rusqlite::{params, Connection, OptionalExtension};
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_encoding::ValueType;
use edb_index::EavtIndexer;
use edb_store_sqlite::{RootStore, SqliteStore, StoreError, LogStore};
use edb_tx::{allocator::TempResolver, grammar::normalize_grammar_ops, model::Value, traits::DbView, validate::normalize_and_validate, TxOp, TxReport, UniquenessResult};

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
}

impl SqliteTransactor {
    pub fn open(path: &str) -> Result<Self, TxrError> {
        let store = SqliteStore::open(path)?;
        let conn = Connection::open(path)?;
        let eavt = EavtIndexer::open(path).ok();
        let txr = Self { store, conn, subscribers: Vec::new(), eavt };
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
            CREATE TABLE IF NOT EXISTS aliases(
              alias TEXT PRIMARY KEY,
              target TEXT NOT NULL
            );
            "#,
        )?;
        Ok(())
    }

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
        let report = {
            let dbview = SqliteDbView { conn: &self.conn };
            let mut temps = TempResolver::new();
            let mut alloc = SqliteAllocator { txr: self };
            let ops: Vec<TxOp> = normalize_grammar_ops(&dbview, &mut alloc, &mut temps, ops_json);
            let mut alloc2 = SqliteAllocator { txr: self };
            normalize_and_validate(&dbview, &ops, &mut alloc2)?
        };

        let tx = self.conn.transaction()?;
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
        let body = serde_json::to_vec(&report.primitives).unwrap();
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
        // Update EAVT memory and merge for demo purposes
        if let Some(idx) = self.eavt.as_mut() {
            let _ = idx.apply_primitives(&rep.primitives, t);
            // For MVP, merge every tx; later add thresholds
            let _ = idx.merge();
        }
        // Broadcast to subscribers; remove dead ones
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
            let primitives: Vec<edb_tx::model::TxPrimitive> = serde_json::from_slice(&bytes).unwrap_or_default();
            for p in primitives {
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
