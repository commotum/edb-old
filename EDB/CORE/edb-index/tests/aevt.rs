use edb_index::AevtIndexer;
use edb_tx::model::{TxPrimitive, Value};

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

#[test]
fn aevt_scan_a_and_ae() {
    let path = db_path("aevt");
    let mut idx = AevtIndexer::open(&path).expect("open");
    let a = ":user/name".to_string();
    let mut prims: Vec<TxPrimitive> = Vec::new();
    prims.push(TxPrimitive { added: true, e: 1, a: a.clone(), v: Value::String("Alice".into()) });
    prims.push(TxPrimitive { added: true, e: 2, a: a.clone(), v: Value::String("Bob".into()) });
    prims.push(TxPrimitive { added: true, e: 1, a: ":user/age".into(), v: Value::Long(30) });
    idx.apply_primitives(&prims, 1).unwrap();
    idx.merge().unwrap();

    let rows_a = idx.scan_a(&a).unwrap();
    let es: Vec<i64> = rows_a.into_iter().map(|d| d.e).collect();
    assert!(es.contains(&1) && es.contains(&2));

    let rows_ae = idx.scan_ae(&a, 1).unwrap();
    assert!(rows_ae.len() == 1);
    assert_eq!(rows_ae[0].e, 1);
}

