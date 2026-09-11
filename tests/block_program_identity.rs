//! Current program-codec identity remains independent of PostgreSQL maintenance.
//! Reachability/GC coverage lives in storage::excision::tests and block_gc.
use atomic_core::{Schema, Value};

fn decode_hex(text: &str) -> Vec<u8> {
    assert!(text.len().is_multiple_of(2));
    text.as_bytes()
        .chunks_exact(2)
        .map(|pair| {
            let digit = |byte: u8| match byte {
                b'0'..=b'9' => byte - b'0',
                b'a'..=b'f' => byte - b'a' + 10,
                _ => panic!("fixture contains invalid hexadecimal"),
            };
            (digit(pair[0]) << 4) | digit(pair[1])
        })
        .collect()
}

fn fixture_query_field(field: &str) -> Vec<u8> {
    let line = include_str!("fixtures/program_query_abi7.hex")
        .lines()
        .find_map(|line| line.strip_prefix(&format!("{field} ")))
        .expect("fixed program fixture field");
    decode_hex(line)
}

#[test]
fn abi7_query_bytes_hash_and_observation_are_stable() {
    let bytes = fixture_query_field("program");
    let program = atomic_core::decode_program(&bytes).unwrap();
    assert_eq!(atomic_core::encode_program(&program).unwrap(), bytes);
    assert_eq!(&bytes[16..18], &7u16.to_be_bytes());
    assert_eq!(
        atomic_core::program_hash(&program).unwrap().as_slice(),
        fixture_query_field("hash")
    );
    let output = atomic_core::ProgramRuntime
        .execute_runtime(
            &program,
            &atomic_core::Database::new(Schema::new()).unwrap(),
            &[atomic_core::RuntimeValue::Vector(
                (1..=4)
                    .map(|value| atomic_core::RuntimeValue::Scalar(Value::Long(value)))
                    .collect(),
            )],
            Default::default(),
        )
        .unwrap();
    assert!(
        matches!(&output, atomic_core::ProgramOutput::Query(rows) if rows == &vec![vec![Value::Long(7)]])
    );
    assert_eq!(
        atomic_core::encode_program_output(&output).unwrap(),
        fixture_query_field("output")
    );
}
