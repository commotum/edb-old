use ed25519_dalek::{SigningKey, SecretKey, VerifyingKey};
use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique, Attribute};
use edb_transactor::SqliteTransactor;

fn db_path(name: &str) -> String {
    std::fs::create_dir_all("target").ok();
    let ts = std::time::SystemTime::now().duration_since(std::time::UNIX_EPOCH).unwrap().as_micros();
    format!("target/{}_{}.sqlite", name, ts)
}

fn attr(ident: &str, vt: ValueType, card: AttrCardinality, unique: AttrUnique) -> Attribute {
    Attribute { ident: ident.to_string(), value_type: vt, cardinality: card, unique, is_component: false, no_history: false, doc: None, aliases: vec![] }
}

#[test]
fn submit_envelope_adds_current_and_heads() {
    // Keys
    let sk = SigningKey::from_bytes(&SecretKey::from_bytes(&[7u8; 32]).unwrap());
    let vk: VerifyingKey = sk.verifying_key();
    let author_pk: [u8;32] = (*vk.as_bytes()).into();

    // Transactor
    let path = db_path("env_add");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    txr.install_attribute(&attr(":user/name", ValueType::String, AttrCardinality::One, AttrUnique::None)).unwrap();

    // Build TxOp
    let ops = vec![edb_tx::model::TxOp::Add { e: edb_tx::model::EntityRef::Entid(1), a: ":user/name".into(), v: edb_tx::model::Value::String("Alice".into()) }];
    let env_ops = edb_envelope::env_ops_from_txops(&ops);
    let env = edb_envelope::UnsignedEnvelopeV1::new(vec![], vec![], author_pk, None, env_ops);
    let unsigned = edb_envelope::to_unsigned_bytes(&env);
    let sig = edb_envelope::sign(&unsigned, &sk);

    let rep = txr.submit_envelope(&unsigned, &sig).expect("submit");
    assert!(rep.t.is_some());

    // Verify current row written
    let name: String = txr.conn.query_row("SELECT vjson FROM current WHERE e=1 AND a=':user/name'", [], |r| r.get(0)).unwrap();
    let val: edb_tx::model::Value = serde_json::from_str(&name).unwrap();
    assert_eq!(val, edb_tx::model::Value::String("Alice".into()));

    // Verify head recorded
    let count: i64 = txr.conn.query_row("SELECT COUNT(*) FROM heads", [], |r| r.get(0)).unwrap();
    assert_eq!(count, 1);

    // AVET should contain the (a,v)->e mapping datom in memory/segment
    if let Some(mut avet) = edb_index::AvetIndexer::open(&path).ok() {
        // scan equality for :user/name "Alice"
        let v_bytes = edb_encoding::encode_scalar(edb_encoding::ValueType::String, &serde_json::json!("Alice")).unwrap();
        let rows = avet.scan_av_eq(":user/name", &v_bytes).unwrap();
        assert!(rows.iter().any(|d| d.e == 1));
    }
}
