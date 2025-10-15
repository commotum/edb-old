use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;
use rusqlite::params;

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

fn attr(ident: &str, vt: ValueType, card: AttrCardinality, unique: AttrUnique) -> Attribute {
    Attribute { ident: ident.to_string(), value_type: vt, cardinality: card, unique, is_component: false, no_history: false, doc: None, aliases: vec![] }
}

#[derive(serde::Deserialize)]
struct TxLogEntry { tx_instant: i64 }

#[test]
fn tx_meta_txinstant_monotonic_and_asof() {
    let path = db_path("tx_meta_asof");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    txr.install_attribute(&attr(":user/name", ValueType::String, AttrCardinality::One, AttrUnique::None)).unwrap();

    // Tx1 with explicit txInstant
    let ops1 = vec![
        serde_json::json!(["tx-meta", {"txInstant": 10}]),
        serde_json::json!(["add", 1, ":user/name", "Alice"]),
    ];
    let rep1 = txr.apply_tx(&ops1).expect("tx1");
    let t1 = rep1.t.unwrap();

    // Read meta last_tx_instant
    let last1: String = txr.conn.query_row("SELECT v FROM meta WHERE k='last_tx_instant'", [], |r| r.get(0)).unwrap();
    assert!(last1.parse::<i64>().unwrap() >= 10);

    // Tx2 with older txInstant (5) should bump to last+1
    let ops2 = vec![
        serde_json::json!(["tx-meta", {"txInstant": 5}]),
        serde_json::json!(["add", 1, ":user/name", "Alicia"]),
    ];
    let rep2 = txr.apply_tx(&ops2).expect("tx2");
    let _t2 = rep2.t.unwrap();
    let last2: String = txr.conn.query_row("SELECT v FROM meta WHERE k='last_tx_instant'", [], |r| r.get(0)).unwrap();
    assert!(last2.parse::<i64>().unwrap() > last1.parse::<i64>().unwrap());

    // Inspect last log entry for tx_instant
    let val: Vec<u8> = txr.conn.query_row("SELECT val FROM log ORDER BY seq DESC LIMIT 1", [], |r| r.get(0)).unwrap();
    if let Ok(entry) = serde_json::from_slice::<TxLogEntry>(&val) {
        assert!(entry.tx_instant >= 10);
    }

    // As-of snapshot at t1 should show Alice
    let attrs = txr.as_of_entity_attrs(1, t1).expect("asof");
    let name = attrs.iter().find(|(a, _)| a == ":user/name").unwrap().1.clone();
    assert_eq!(name, edb_tx::model::Value::String("Alice".into()));

    // Current should be Alicia
    let vjson: String = txr.conn.query_row("SELECT vjson FROM current WHERE e=?1 AND a=?2", params![1, ":user/name"], |r| r.get(0)).unwrap();
    let cur: edb_tx::model::Value = serde_json::from_str(&vjson).unwrap();
    assert_eq!(cur, edb_tx::model::Value::String("Alicia".into()));
}
