use edb_index::AvetIndexer;
use edb_tx::model::{TxPrimitive, Value};

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

#[test]
fn avet_double_range() {
    let path = db_path("avet_double");
    let mut idx = AvetIndexer::open(&path).expect("open");
    let a = ":m/val".to_string();
    let prims = vec![
        TxPrimitive { added: true, e: 1, a: a.clone(), v: Value::Double(-1.0) },
        TxPrimitive { added: true, e: 2, a: a.clone(), v: Value::Double(0.0) },
        TxPrimitive { added: true, e: 3, a: a.clone(), v: Value::Double(1.0) },
    ];
    idx.apply_primitives(&prims, 1).unwrap();
    idx.merge().unwrap();
    let vb_neg_half = edb_encoding::encode_scalar(edb_encoding::ValueType::Double, &serde_json::json!(-0.5)).unwrap();
    let vb_half = edb_encoding::encode_scalar(edb_encoding::ValueType::Double, &serde_json::json!(0.5)).unwrap();
    let rows = idx.scan_av_range(&a, &vb_neg_half, Some(&vb_half)).unwrap();
    let es: Vec<i64> = rows.into_iter().map(|d| d.e).collect();
    assert!(es.len() == 1 && es[0] == 2);
}

#[test]
fn avet_instant_range() {
    let path = db_path("avet_inst");
    let mut idx = AvetIndexer::open(&path).expect("open");
    let a = ":time/inst".to_string();
    let prims = vec![
        TxPrimitive { added: true, e: 1, a: a.clone(), v: Value::Instant(1_000_000) },
        TxPrimitive { added: true, e: 2, a: a.clone(), v: Value::Instant(2_000_000) },
        TxPrimitive { added: true, e: 3, a: a.clone(), v: Value::Instant(3_000_000) },
    ];
    idx.apply_primitives(&prims, 1).unwrap();
    idx.merge().unwrap();
    let vb_start = edb_encoding::encode_scalar(edb_encoding::ValueType::Instant, &serde_json::json!(1_500_000)).unwrap();
    let vb_end = edb_encoding::encode_scalar(edb_encoding::ValueType::Instant, &serde_json::json!(2_500_000)).unwrap();
    let rows = idx.scan_av_range(&a, &vb_start, Some(&vb_end)).unwrap();
    let es: Vec<i64> = rows.into_iter().map(|d| d.e).collect();
    assert!(es.len() == 1 && es[0] == 2);
}

#[test]
fn avet_bigint_decimal_ranges() {
    let path = db_path("avet_big_dec");
    let mut idx = AvetIndexer::open(&path).expect("open");
    // BigInt
    let a_bi = ":num/bi".to_string();
    let prims_bi = vec![
        TxPrimitive { added: true, e: 1, a: a_bi.clone(), v: Value::Bigint("-10".into()) },
        TxPrimitive { added: true, e: 2, a: a_bi.clone(), v: Value::Bigint("0".into()) },
        TxPrimitive { added: true, e: 3, a: a_bi.clone(), v: Value::Bigint("10".into()) },
    ];
    idx.apply_primitives(&prims_bi, 1).unwrap();
    // Decimal
    let a_dec = ":num/dec".to_string();
    let prims_dec = vec![
        TxPrimitive { added: true, e: 4, a: a_dec.clone(), v: Value::Decimal("-2.5".into()) },
        TxPrimitive { added: true, e: 5, a: a_dec.clone(), v: Value::Decimal("0.0".into()) },
        TxPrimitive { added: true, e: 6, a: a_dec.clone(), v: Value::Decimal("1.25".into()) },
    ];
    idx.apply_primitives(&prims_dec, 2).unwrap();
    idx.merge().unwrap();

    // Bigint range [-5, 5) should capture e=2 only
    let bi_start = edb_encoding::encode_scalar(edb_encoding::ValueType::Bigint, &serde_json::json!("-5")).unwrap();
    let bi_end = edb_encoding::encode_scalar(edb_encoding::ValueType::Bigint, &serde_json::json!("5")).unwrap();
    let rows_bi = idx.scan_av_range(&a_bi, &bi_start, Some(&bi_end)).unwrap();
    let es_bi: Vec<i64> = rows_bi.into_iter().map(|d| d.e).collect();
    assert!(es_bi.len() == 1 && es_bi[0] == 2);

    // Decimal range [0.0, 2.0) captures e=5 and e=6
    let dec_start = edb_encoding::encode_scalar(edb_encoding::ValueType::Decimal, &serde_json::json!("0.0")).unwrap();
    let dec_end = edb_encoding::encode_scalar(edb_encoding::ValueType::Decimal, &serde_json::json!("2.0")).unwrap();
    let rows_dec = idx.scan_av_range(&a_dec, &dec_start, Some(&dec_end)).unwrap();
    let mut es_dec: Vec<i64> = rows_dec.into_iter().map(|d| d.e).collect();
    es_dec.sort();
    assert_eq!(es_dec, vec![5, 6]);
}
