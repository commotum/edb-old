use edb_index::{AvetIndexer, VaetIndexer};
use edb_tx::model::{TxPrimitive, Value};
use rusqlite::Connection;

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

#[test]
fn avet_eq_and_range_scan() {
    let path = db_path("avet");
    let mut idx = AvetIndexer::open(&path).expect("open");
    let a = ":user/age".to_string();
    let mut prims: Vec<TxPrimitive> = Vec::new();
    prims.push(TxPrimitive { added: true, e: 1, a: a.clone(), v: Value::Long(10) });
    prims.push(TxPrimitive { added: true, e: 2, a: a.clone(), v: Value::Long(20) });
    prims.push(TxPrimitive { added: true, e: 3, a: a.clone(), v: Value::Long(30) });
    idx.apply_primitives(&prims, 1).unwrap();
    idx.merge().unwrap();

    let vb_20 = edb_encoding::encode_scalar(edb_encoding::ValueType::Long, &serde_json::json!(20)).unwrap();
    let rows = idx.scan_av_eq(&a, &vb_20).unwrap();
    assert!(rows.iter().any(|d| d.e == 2));

    let vb_15 = edb_encoding::encode_scalar(edb_encoding::ValueType::Long, &serde_json::json!(15)).unwrap();
    let vb_25 = edb_encoding::encode_scalar(edb_encoding::ValueType::Long, &serde_json::json!(25)).unwrap();
    let range = idx.scan_av_range(&a, &vb_15, Some(&vb_25)).unwrap();
    let es: Vec<i64> = range.into_iter().map(|d| d.e).collect();
    assert!(es.len() == 1 && es[0] == 2);
}

#[test]
fn vaet_scan_v() {
    let path = db_path("vaet");
    // Insert minimal attrs row so VAET recognizes :db.type/ref
    let conn = Connection::open(&path).unwrap();
    conn.execute(
        "CREATE TABLE IF NOT EXISTS attrs(ident TEXT PRIMARY KEY, vt INTEGER NOT NULL);",
        [],
    ).unwrap();
    conn.execute(
        "INSERT OR REPLACE INTO attrs(ident, vt) VALUES(?1, ?2)",
        rusqlite::params![":rel/child", 8i64],
    ).unwrap();

    let mut idx = VaetIndexer::open(&path).expect("open");
    let mut prims: Vec<TxPrimitive> = Vec::new();
    prims.push(TxPrimitive { added: true, e: 10, a: ":rel/child".into(), v: Value::Ref(20) });
    prims.push(TxPrimitive { added: true, e: 11, a: ":rel/child".into(), v: Value::Ref(21) });
    idx.apply_primitives(&prims, 1).unwrap();
    idx.merge().unwrap();

    let rows = idx.scan_v(20).unwrap();
    assert!(rows.iter().any(|d| d.e == 10 && d.a == ":rel/child"));
}
