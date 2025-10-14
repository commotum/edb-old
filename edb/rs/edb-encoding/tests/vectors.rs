use edb_encoding::{encode_scalar, encode_tuple_v1, tuple_order_bytes, ValueType};
use serde::Deserialize;
use std::fs;
use std::path::Path;

#[derive(Deserialize)]
struct ScalarVectors {
    value_type: String,
    vectors: Vec<ScalarVector>,
}

#[derive(Deserialize)]
struct ScalarVector {
    value: serde_json::Value,
    bytes_hex: String,
}

#[derive(Deserialize)]
struct TupleRgba {
    elem_type: String,
    value: Vec<serde_json::Value>,
    tuple_bytes_hex: String,
    v_bytes_hex: String,
}

fn hex(s: &str) -> Vec<u8> {
    let s = s.trim();
    (0..s.len())
        .step_by(2)
        .map(|i| u8::from_str_radix(&s[i..i + 2], 16).unwrap())
        .collect()
}

fn vec_path(name: &str) -> String {
    // crate root -> ../../01-encoding/tests/vectors
    let base = Path::new(env!("CARGO_MANIFEST_DIR"))
        .join("../../01-encoding/tests/vectors");
    base.join(name).to_string_lossy().to_string()
}

#[test]
fn test_double_vectors() {
    let p = vec_path("double.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "DOUBLE");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Double, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex), "double vector mismatch for {:?}", v.value);
    }
}

#[test]
fn test_long_vectors() {
    let p = vec_path("long.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "LONG");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Long, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex), "long vector mismatch for {:?}", v.value);
    }
}

#[test]
fn test_bigint_vectors() {
    let p = vec_path("bigint.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "BIGINT");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Bigint, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_decimal_vectors() {
    let p = vec_path("decimal.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "DECIMAL");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Decimal, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_float32_vectors() {
    let p = vec_path("float32.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "FLOAT32");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Float32, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_float16_vectors() {
    let p = vec_path("float16.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "FLOAT16");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Float16, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_bfloat16_vectors() {
    let p = vec_path("bfloat16.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "BFLOAT16");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Bfloat16, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_string_nfc_vectors() {
    let p = vec_path("string_nfc.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "STRING");
    let bytes0 = encode_scalar(ValueType::String, &sv.vectors[0].value).expect("enc0");
    let bytes1 = encode_scalar(ValueType::String, &sv.vectors[1].value).expect("enc1");
    assert_eq!(bytes0, bytes1, "NFC normalization must match");
    assert_eq!(bytes0, hex(&sv.vectors[0].bytes_hex));
}

#[test]
fn test_keyword_vectors() {
    let p = vec_path("keyword.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "KEYWORD");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Keyword, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_uuid_vectors() {
    let p = vec_path("uuid.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "UUID");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Uuid, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_uint8_vectors() {
    let p = vec_path("uint8.json");
    let txt = fs::read_to_string(&p).expect("read");
    let sv: ScalarVectors = serde_json::from_str(&txt).expect("json");
    assert_eq!(sv.value_type, "UINT8");
    for v in sv.vectors {
        let bytes = encode_scalar(ValueType::Uint8, &v.value).expect("encode");
        assert_eq!(bytes, hex(&v.bytes_hex));
    }
}

#[test]
fn test_tuple_rgba_vectors() {
    let p = vec_path("tuple_rgba.json");
    let txt = fs::read_to_string(&p).expect("read");
    let tv: TupleRgba = serde_json::from_str(&txt).expect("json");
    assert_eq!(tv.elem_type, "UINT8");
    let bytes = encode_tuple_v1(&tv.value, ValueType::Uint8).expect("enc tuple");
    assert_eq!(bytes, hex(&tv.tuple_bytes_hex));
    let v_bytes = tuple_order_bytes(&bytes).expect("v*");
    assert_eq!(v_bytes, hex(&tv.v_bytes_hex));
}

#[test]
fn test_tuple_strings_vectors() {
    let p = vec_path("tuple_strings.json");
    let txt = fs::read_to_string(&p).expect("read");
    let tv: TupleRgba = serde_json::from_str(&txt).expect("json");
    assert_eq!(tv.elem_type, "STRING");
    let bytes = encode_tuple_v1(&tv.value, ValueType::String).expect("enc tuple");
    assert_eq!(bytes, hex(&tv.tuple_bytes_hex));
    let v_bytes = tuple_order_bytes(&bytes).expect("v*");
    assert_eq!(v_bytes, hex(&tv.v_bytes_hex));
}
