use ed25519_dalek::{SigningKey, VerifyingKey};
use edb_transactor::SqliteTransactor;
use edb_schema::{Attribute, AttrCardinality, AttrUnique};
use edb_encoding::ValueType;
use edb_envelope::{UnsignedEnvelopeV1, EnvOp, to_unsigned_bytes, sign};

fn tmp_db_path(name: &str) -> String {
    let mut p = std::env::temp_dir();
    p.push(format!("{}_{}.db", name, std::process::id()));
    p.to_string_lossy().to_string()
}

fn gen_keypair() -> (SigningKey, [u8;32]) {
    let mut seed = [0u8; 32];
    for (i, b) in seed.iter_mut().enumerate() { *b = (i as u8).wrapping_mul(7).wrapping_add(3); }
    let sk = SigningKey::from_bytes(&seed);
    let vk: VerifyingKey = sk.verifying_key();
    (sk, *vk.as_bytes())
}

#[test]
fn reject_bad_signature() {
    let path = tmp_db_path("env_bad_sig");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let (_sk, vk_bytes) = gen_keypair();
    // Build a trivial env body with one add op
    let ops = vec![EnvOp::Add((serde_json::json!("temp:e"), ":a/x".into(), edb_tx::model::Value::Long(1)))];
    let unsigned = UnsignedEnvelopeV1::new(vec![], vec![], vk_bytes, None, ops);
    let bytes = to_unsigned_bytes(&unsigned);
    let wrong_seed = [1u8; 32];
    let wrong_sk = SigningKey::from_bytes(&wrong_seed);
    let sig = sign(&bytes, &wrong_sk);
    let err = txr.submit_envelope(&bytes, &sig).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("bad signature"));
}

#[test]
fn reject_unknown_feature() {
    let path = tmp_db_path("env_feat");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    let (sk, vk_bytes) = gen_keypair();
    let ops = vec![EnvOp::Add((serde_json::json!("temp:e"), ":a/x".into(), edb_tx::model::Value::Long(1)))];
    let unsigned = UnsignedEnvelopeV1::new(vec![], vec!["future:thing".into()], vk_bytes, None, ops);
    let bytes = to_unsigned_bytes(&unsigned);
    let sig = sign(&bytes, &sk);
    let err = txr.submit_envelope(&bytes, &sig).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("unknown/unsupported feature"));
}

#[test]
fn reject_parent_mismatch_linear_mode() {
    let path = tmp_db_path("env_parent");
    let mut txr = SqliteTransactor::open(&path).expect("open");
    // Install minimal schema for :a/x used by the envelope body
    let attr = Attribute { ident: ":a/x".into(), value_type: ValueType::Long, cardinality: AttrCardinality::One, unique: AttrUnique::None, is_component: false, no_history: false, doc: None, aliases: vec![] };
    txr.install_attribute(&attr).unwrap();
    let (sk, vk_bytes) = gen_keypair();
    // First, create a head by submitting a valid envelope (no parent)
    {
        let ops = vec![EnvOp::Add((serde_json::json!("temp:e"), ":a/x".into(), edb_tx::model::Value::Long(1)))];
        let unsigned = UnsignedEnvelopeV1::new(vec![], vec![], vk_bytes, None, ops);
        let bytes = to_unsigned_bytes(&unsigned);
        let sig = sign(&bytes, &sk);
        txr.submit_envelope(&bytes, &sig).expect("apply env1");
    }
    // Now forge a wrong parent (all zero hash)
    let wrong_parent = [0u8; 32];
    let ops2 = vec![EnvOp::Add((serde_json::json!("temp:e2"), ":a/x".into(), edb_tx::model::Value::Long(2)))];
    let unsigned2 = UnsignedEnvelopeV1::new(vec![wrong_parent], vec![], vk_bytes, None, ops2);
    let bytes2 = to_unsigned_bytes(&unsigned2);
    let sig2 = sign(&bytes2, &sk);
    let err = txr.submit_envelope(&bytes2, &sig2).unwrap_err();
    let msg = format!("{}", err);
    assert!(msg.contains("parent mismatch"));
}
