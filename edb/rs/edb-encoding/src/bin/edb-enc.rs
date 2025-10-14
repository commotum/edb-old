use edb_encoding::{decode_scalar, encode_scalar, encode_tuple_v1, tuple_order_bytes, ValueType};
use serde_json::json;

fn parse_type(s: &str) -> Result<ValueType, String> {
    match s.to_uppercase().as_str() {
        "LONG" => Ok(ValueType::Long),
        "DOUBLE" => Ok(ValueType::Double),
        "BOOLEAN" => Ok(ValueType::Boolean),
        "STRING" => Ok(ValueType::String),
        "KEYWORD" => Ok(ValueType::Keyword),
        "UUID" => Ok(ValueType::Uuid),
        "INSTANT" => Ok(ValueType::Instant),
        "REF" => Ok(ValueType::Ref),
        "BYTES" => Ok(ValueType::Bytes),
        "UINT8" => Ok(ValueType::Uint8),
        "BIGINT" => Ok(ValueType::Bigint),
        "DECIMAL" => Ok(ValueType::Decimal),
        "FLOAT32" => Ok(ValueType::Float32),
        "FLOAT16" => Ok(ValueType::Float16),
        "BFLOAT16" => Ok(ValueType::Bfloat16),
        _ => Err("unknown type".into()),
    }
}

fn hex(bytes: &[u8]) -> String { bytes.iter().map(|b| format!("{:02x}", b)).collect() }

fn parse_values_csv(csv: &str) -> Vec<serde_json::Value> {
    csv.split(',').map(|s| {
        let s = s.trim();
        // Allow symbolic floats and plain strings in quotes
        match s {
            "NaN" | "+inf" | "-inf" => serde_json::Value::from(s),
            _ => if let Ok(i) = s.parse::<i64>() { json!(i) }
                 else if let Ok(f) = s.parse::<f64>() { json!(f) }
                 else if (s.starts_with('"') && s.ends_with('"')) || s.starts_with(':') || s.contains('-') || s.contains('.') {
                    // treat as string/keyword/uuid/decimal/bigint string
                    serde_json::Value::from(s.trim_matches('"'))
                 } else { serde_json::Value::from(s) },
        }
    }).collect()
}

fn main() {
    let args: Vec<String> = std::env::args().collect();
    if args.len() < 3 {
        eprintln!("Usage:\n  edb-enc encode <TYPE> <VALUE>\n  edb-enc encode-tuple <TYPE> <CSV_VALUES>\n  edb-enc decode <TYPE> <HEX>\n");
        std::process::exit(2);
    }
    let cmd = &args[1];
    match cmd.as_str() {
        "encode" => {
            let vt = parse_type(&args[2]).expect("type");
            let v = &args[3];
            // Try number, else treat as string
            let json_val = if v == "NaN" || v == "+inf" || v == "-inf" { serde_json::Value::from(v.as_str()) }
                           else if let Ok(i) = v.parse::<i64>() { json!(i) }
                           else if let Ok(f) = v.parse::<f64>() { json!(f) }
                           else { serde_json::Value::from(v.as_str()) };
            let b = encode_scalar(vt, &json_val).expect("encode");
            println!("{}", hex(&b));
        }
        "encode-tuple" => {
            let vt = parse_type(&args[2]).expect("type");
            let vals = parse_values_csv(&args[3]);
            let b = encode_tuple_v1(&vals, vt).expect("enc tuple");
            let vbytes = tuple_order_bytes(&b).expect("v*");
            println!("tuple: {}\nv*: {}", hex(&b), hex(vbytes));
        }
        "decode" => {
            let vt = parse_type(&args[2]).expect("type");
            let hexstr = &args[3];
            let bytes: Vec<u8> = (0..hexstr.len()).step_by(2).map(|i| u8::from_str_radix(&hexstr[i..i+2], 16).unwrap()).collect();
            let val = decode_scalar(vt, &bytes).expect("decode");
            println!("{}", val);
        }
        _ => {
            eprintln!("unknown command");
            std::process::exit(2);
        }
    }
}

