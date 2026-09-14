use ed25519_dalek::{SigningKey, VerifyingKey, Signature, Signer, Verifier};
use sha2::{Digest, Sha256};
use serde::{Deserialize, Serialize};
use ciborium::value::Value as CborValue;

/// A canonicalized transaction op for the envelope body.
/// Represented as a CBOR array at the wire level.
#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
#[serde(tag = "op", content = "args")]
pub enum EnvOp {
    #[serde(rename = "add")]
    Add((serde_json::Value, String, edb_tx::model::Value)),
    #[serde(rename = "retract")]
    Retract((serde_json::Value, String, Option<edb_tx::model::Value>)),
    #[serde(rename = "cas")]
    Cas((serde_json::Value, String, Option<edb_tx::model::Value>, edb_tx::model::Value)),
    #[serde(rename = "retract-entity")]
    RetractEntity((serde_json::Value,)),
}

#[derive(Debug, Clone, Serialize, Deserialize, PartialEq)]
pub struct UnsignedEnvelopeV1 {
    pub magic: String,                  // "edb.tx"
    pub version: u8,                    // 1
    pub parents: Vec<Vec<u8>>,          // each 32 bytes
    pub features: Vec<String>,
    pub author_pubkey: Vec<u8>,         // 32 bytes
    pub authored_at: Option<u64>,       // micros since epoch
    pub tx_body: Vec<EnvOp>,            // canonical, sorted by CBOR bytes
}

impl UnsignedEnvelopeV1 {
    pub fn new(
        parents: Vec<[u8; 32]>,
        features: Vec<String>,
        author_pubkey: [u8; 32],
        authored_at: Option<u64>,
        mut ops: Vec<EnvOp>,
    ) -> Self {
        // Sort ops by CBOR bytes for canonical ordering
        ops.sort_by(|a, b| cbor_bytes_for_op(a).cmp(&cbor_bytes_for_op(b)));
        Self {
            magic: "edb.tx".to_string(),
            version: 1,
            parents: parents.into_iter().map(|p| p.to_vec()).collect(),
            features,
            author_pubkey: author_pubkey.to_vec(),
            authored_at,
            tx_body: ops,
        }
    }
}

fn cbor_bytes_for_op(op: &EnvOp) -> Vec<u8> {
    // Represent as a CBOR array [opcode, args...]
    let v = match op {
        EnvOp::Add((e,a,v)) => CborValue::Array(vec![
            CborValue::Text("add".into()),
            serde_to_cbor(&serde_json::to_value(e).unwrap_or(serde_json::Value::Null)),
            CborValue::Text(a.clone()),
            serde_to_cbor(&serde_json::to_value(v).unwrap()),
        ]),
        EnvOp::Retract((e,a,ov)) => CborValue::Array(vec![
            CborValue::Text("retract".into()),
            serde_to_cbor(&serde_json::to_value(e).unwrap_or(serde_json::Value::Null)),
            CborValue::Text(a.clone()),
            match ov { Some(v) => serde_to_cbor(&serde_json::to_value(v).unwrap()), None => CborValue::Null },
        ]),
        EnvOp::Cas((e,a,exp,v)) => CborValue::Array(vec![
            CborValue::Text("cas".into()),
            serde_to_cbor(&serde_json::to_value(e).unwrap_or(serde_json::Value::Null)),
            CborValue::Text(a.clone()),
            match exp { Some(x) => serde_to_cbor(&serde_json::to_value(x).unwrap()), None => CborValue::Null },
            serde_to_cbor(&serde_json::to_value(v).unwrap()),
        ]),
        EnvOp::RetractEntity((e,)) => CborValue::Array(vec![
            CborValue::Text("retract-entity".into()),
            serde_to_cbor(&serde_json::to_value(e).unwrap_or(serde_json::Value::Null)),
        ]),
    };
    let mut buf = Vec::new();
    let _ = ciborium::ser::into_writer(&v, &mut buf);
    buf
}

fn serde_to_cbor(j: &serde_json::Value) -> CborValue {
    match j {
        serde_json::Value::Null => CborValue::Null,
        serde_json::Value::Bool(b) => CborValue::Bool(*b),
        serde_json::Value::Number(n) => {
            if let Some(i) = n.as_i64() { CborValue::Integer(i.into()) }
            else if let Some(u) = n.as_u64() { CborValue::Integer(u.into()) }
            else if let Some(f) = n.as_f64() { CborValue::Float(f) } else { CborValue::Null }
        }
        serde_json::Value::String(s) => CborValue::Text(s.clone()),
        serde_json::Value::Array(arr) => CborValue::Array(arr.iter().map(serde_to_cbor).collect()),
        serde_json::Value::Object(map) => {
            // Maintain insertion order by sorting keys for determinism
            let mut entries: Vec<_> = map.iter().collect();
            entries.sort_by_key(|(k, _)| *k);
            CborValue::Map(entries.into_iter().map(|(k, v)| (CborValue::Text(k.clone()), serde_to_cbor(v))).collect())
        }
    }
}

pub fn to_unsigned_bytes(env: &UnsignedEnvelopeV1) -> Vec<u8> {
    // Represent the envelope as a CBOR array in fixed field order.
    let v = CborValue::Array(vec![
        CborValue::Text(env.magic.clone()),
        CborValue::Integer((env.version as u64).into()),
        CborValue::Array(env.parents.iter().map(|p| CborValue::Bytes(p.clone())).collect()),
        CborValue::Array(env.features.iter().map(|f| CborValue::Text(f.clone())).collect()),
        CborValue::Bytes(env.author_pubkey.clone()),
        match env.authored_at { Some(x) => CborValue::Integer(x.into()), None => CborValue::Null },
        CborValue::Array(env.tx_body.iter().map(|op| {
            // Encode as array form to ensure stable wire (match cbor_bytes_for_op)
            let bytes = cbor_bytes_for_op(op);
            // Decode back to Value to avoid nested re-encoding discrepancies
            ciborium::de::from_reader(bytes.as_slice()).unwrap_or(CborValue::Null)
        }).collect()),
    ]);
    let mut buf = Vec::new();
    let _ = ciborium::ser::into_writer(&v, &mut buf);
    buf
}

pub fn tx_id(unsigned_bytes: &[u8]) -> [u8; 32] {
    let mut hasher = Sha256::new();
    hasher.update(unsigned_bytes);
    let out = hasher.finalize();
    let mut id = [0u8; 32];
    id.copy_from_slice(&out[..]);
    id
}

pub fn sign(unsigned_bytes: &[u8], sk: &SigningKey) -> [u8; 64] {
    let sig: Signature = sk.sign(unsigned_bytes);
    sig.to_bytes()
}

pub fn verify(unsigned_bytes: &[u8], sig_bytes: &[u8], vk_bytes: &[u8]) -> bool {
    if vk_bytes.len() != 32 || sig_bytes.len() != 64 { return false; }
    let vk = VerifyingKey::from_bytes(vk_bytes.try_into().unwrap());
    if vk.is_err() { return false; }
    let vk = vk.unwrap();
    let sig = Signature::from_bytes(sig_bytes.try_into().unwrap());
    vk.verify(unsigned_bytes, &sig).is_ok()
}

/// Convert TxOps into EnvOps for inclusion in the envelope body. Entity refs in ops are
/// serialized as JSON values; lookup/temps should be resolved in normalization.
pub fn env_ops_from_txops(ops: &[edb_tx::model::TxOp]) -> Vec<EnvOp> {
    ops.iter().map(|op| match op {
        edb_tx::model::TxOp::Add { e, a, v } => EnvOp::Add((entity_ref_to_json(e), a.clone(), v.clone())),
        edb_tx::model::TxOp::Retract { e, a, v } => EnvOp::Retract((entity_ref_to_json(e), a.clone(), v.clone())),
        edb_tx::model::TxOp::Cas { e, a, expected, v } => EnvOp::Cas((entity_ref_to_json(e), a.clone(), expected.clone(), v.clone())),
    }).collect()
}

fn entity_ref_to_json(er: &edb_tx::model::EntityRef) -> serde_json::Value {
    match er {
        edb_tx::model::EntityRef::Entid(id) => serde_json::json!(*id),
        edb_tx::model::EntityRef::TempId(t) => serde_json::json!(t.0),
        edb_tx::model::EntityRef::LookupRef { attr, value } => serde_json::json!(["lookup", attr, value]),
    }
}
