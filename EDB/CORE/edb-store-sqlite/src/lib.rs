use rusqlite::{params, Connection, OptionalExtension};

#[derive(thiserror::Error, Debug)]
pub enum StoreError {
    #[error("sqlite: {0}")]
    Sqlite(#[from] rusqlite::Error),
}

pub trait SegmentStore {
    fn get_segment(&self, id: &str) -> Result<Option<Vec<u8>>, StoreError>;
    fn put_segment_if_absent(&self, id: &str, val: &[u8]) -> Result<bool, StoreError>;
}

pub trait RootStore {
    fn get_root(&self, name: &str) -> Result<Option<(i64, Vec<u8>)>, StoreError>;
    fn init_root(&self, name: &str, val: &[u8]) -> Result<(), StoreError>;
    fn cas_root(&self, name: &str, expected_rev: i64, new_val: &[u8]) -> Result<Option<i64>, StoreError>;
}

pub trait LogStore {
    fn append_log(&self, val: &[u8]) -> Result<i64, StoreError>;
    fn read_log_range(&self, start: i64, end: i64) -> Result<Vec<(i64, Vec<u8>)>, StoreError>;
}

pub struct SqliteStore {
    conn: Connection,
}

impl SqliteStore {
    pub fn open(path: &str) -> Result<Self, StoreError> {
        let conn = Connection::open(path)?;
        conn.pragma_update(None, "journal_mode", "WAL")?;
        conn.pragma_update(None, "synchronous", "FULL")?;
        let s = Self { conn };
        s.init_schema()?;
        Ok(s)
    }

    fn init_schema(&self) -> Result<(), StoreError> {
        self.conn.execute_batch(
            r#"
            CREATE TABLE IF NOT EXISTS segments(
              id TEXT PRIMARY KEY,
              val BLOB NOT NULL
            );
            CREATE TABLE IF NOT EXISTS roots(
              name TEXT PRIMARY KEY,
              rev INTEGER NOT NULL,
              val BLOB NOT NULL
            );
            CREATE TABLE IF NOT EXISTS log(
              seq INTEGER PRIMARY KEY AUTOINCREMENT,
              val BLOB NOT NULL
            );
            "#,
        )?;
        Ok(())
    }
}

impl SegmentStore for SqliteStore {
    fn get_segment(&self, id: &str) -> Result<Option<Vec<u8>>, StoreError> {
        let mut stmt = self.conn.prepare("SELECT val FROM segments WHERE id = ?1")?;
        let row = stmt.query_row(params![id], |r| r.get::<_, Vec<u8>>(0)).optional()?;
        Ok(row)
    }
    fn put_segment_if_absent(&self, id: &str, val: &[u8]) -> Result<bool, StoreError> {
        let changed = self.conn.execute("INSERT OR IGNORE INTO segments(id,val) VALUES(?1,?2)", params![id, val])?;
        Ok(changed == 1)
    }
}

impl RootStore for SqliteStore {
    fn get_root(&self, name: &str) -> Result<Option<(i64, Vec<u8>)>, StoreError> {
        let mut stmt = self.conn.prepare("SELECT rev, val FROM roots WHERE name = ?1")?;
        let row = stmt
            .query_row(params![name], |r| Ok((r.get::<_, i64>(0)?, r.get::<_, Vec<u8>>(1)?)))
            .optional()?;
        Ok(row)
    }
    fn init_root(&self, name: &str, val: &[u8]) -> Result<(), StoreError> {
        self.conn.execute(
            "INSERT OR IGNORE INTO roots(name,rev,val) VALUES(?1,0,?2)",
            params![name, val],
        )?;
        Ok(())
    }
    fn cas_root(&self, name: &str, expected_rev: i64, new_val: &[u8]) -> Result<Option<i64>, StoreError> {
        let changed = self.conn.execute(
            "UPDATE roots SET rev = rev + 1, val = ?1 WHERE name = ?2 AND rev = ?3",
            params![new_val, name, expected_rev],
        )?;
        if changed == 1 {
            Ok(Some(expected_rev + 1))
        } else {
            Ok(None)
        }
    }
}

impl LogStore for SqliteStore {
    fn append_log(&self, val: &[u8]) -> Result<i64, StoreError> {
        self.conn.execute("INSERT INTO log(val) VALUES(?1)", params![val])?;
        Ok(self.conn.last_insert_rowid())
    }
    fn read_log_range(&self, start: i64, end: i64) -> Result<Vec<(i64, Vec<u8>)>, StoreError> {
        let mut stmt = self.conn.prepare("SELECT seq, val FROM log WHERE seq >= ?1 AND seq < ?2 ORDER BY seq ASC")?;
        let rows = stmt.query_map(params![start, end], |r| Ok((r.get::<_, i64>(0)?, r.get::<_, Vec<u8>>(1)?)))?;
        let mut out = Vec::new();
        for row in rows { out.push(row?); }
        Ok(out)
    }
}
