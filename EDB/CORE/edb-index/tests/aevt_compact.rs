use edb_index::AevtIndexer;
use edb_tx::model::{TxPrimitive, Value};

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

#[test]
fn aevt_compaction_preserves_scans() {
    let path = db_path("aevt_compact");
    let mut idx = AevtIndexer::open(&path).expect("open");
    let a = ":item/tag".to_string();
    // Create multiple segments by alternating small merges
    for i in 0..12 {
        let prims = vec![TxPrimitive { added: true, e: i, a: a.clone(), v: Value::String(format!("T{}", i)) }];
        idx.apply_primitives(&prims, i as i64).unwrap();
        idx.merge().unwrap();
    }
    // Scan attribute after compaction triggers; all e values should be present
    let rows = idx.scan_a(&a).unwrap();
    let mut es: Vec<i64> = rows.into_iter().map(|d| d.e).collect();
    es.sort();
    assert_eq!(es.len(), 12);
    assert_eq!(es[0], 0);
    assert_eq!(es[11], 11);
}

