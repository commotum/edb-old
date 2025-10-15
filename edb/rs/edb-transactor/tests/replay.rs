use edb_transactor::SqliteTransactor;
use edb_schema::{Attribute, AttrCardinality, AttrUnique};
use edb_encoding::ValueType;
use rusqlite::Connection;

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

fn attr(ident: &str, vt: ValueType, card: AttrCardinality, unique: AttrUnique) -> Attribute {
    Attribute { ident: ident.to_string(), value_type: vt, cardinality: card, unique, is_component: false, no_history: false, doc: None, aliases: vec![] }
}

#[test]
fn monotonic_t_increments() {
    let path = db_path("monotonic_t");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    // install minimal attribute
    txr.install_attribute(&attr(":user/name", ValueType::String, AttrCardinality::One, AttrUnique::None)).unwrap();
    let ops1 = vec![serde_json::json!(["add", 1, ":user/name", "Alice"])];
    let rep1 = txr.apply_tx(&ops1).expect("apply1");
    let ops2 = vec![serde_json::json!(["add", 1, ":user/name", "Alice2"])];
    let rep2 = txr.apply_tx(&ops2).expect("apply2");
    assert!(rep1.t.is_some() && rep2.t.is_some());
    assert!(rep2.t.unwrap() > rep1.t.unwrap());
}

#[test]
fn replay_rebuilds_current_and_unique_idx() {
    let path = db_path("replay");
    {
        let mut txr = SqliteTransactor::open(&path).expect("open");
        txr.install_attribute(&attr(":user/id", ValueType::String, AttrCardinality::One, AttrUnique::Identity)).unwrap();
        txr.install_attribute(&attr(":user/name", ValueType::String, AttrCardinality::One, AttrUnique::None)).unwrap();
        let ops = vec![
            serde_json::json!(["add", 1, ":user/name", "Alice"]),
            serde_json::json!(["add", 1, ":user/id", "U1"]),
        ];
        let _rep = txr.apply_tx(&ops).expect("apply");
    }
    // New transactor instance on same DB; purge tables and replay
    let mut txr2 = SqliteTransactor::open(&path).expect("open2");
    txr2.replay_current_from_log().expect("replay");
    // Inspect the DB directly
    let conn = Connection::open(&path).unwrap();
    let name: String = conn.query_row("SELECT vjson FROM current WHERE e=1 AND a=':user/name'", [], |r| r.get(0)).unwrap();
    assert_eq!(serde_json::from_str::<edb_tx::model::Value>(&name).unwrap(), edb_tx::model::Value::String("Alice".into()));
    let owner: i64 = conn.query_row("SELECT e FROM unique_idx WHERE a=':user/id' AND vkey='S:U1'", [], |r| r.get(0)).unwrap();
    assert_eq!(owner, 1);
}
