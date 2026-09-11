use super::*;
use bigdecimal::BigDecimal;
use num_traits::One;
use std::collections::hash_map::DefaultHasher;

fn logical_hash(value: &Value) -> u64 {
    let mut state = DefaultHasher::new();
    value.logical_hash(&mut state);
    state.finish()
}

// An independent, deliberately bounded rational reference. This is not used on
// the compact extreme scales: materializing those is precisely the old defect.
fn rational(value: &Value) -> (BigInt, BigInt) {
    match value {
        Value::Long(value) => (BigInt::from(*value), BigInt::one()),
        Value::Ref(value) => (BigInt::from(*value), BigInt::one()),
        Value::BigInt(value) => (value.clone(), BigInt::one()),
        Value::BigDec(value) => {
            let (coefficient, scale) = value.as_bigint_and_exponent();
            assert!(scale.unsigned_abs() <= 1200);
            if scale >= 0 {
                (coefficient, BigInt::from(10).pow(scale as u32))
            } else {
                (
                    coefficient * BigInt::from(10).pow(scale.unsigned_abs() as u32),
                    BigInt::one(),
                )
            }
        }
        Value::Float(value) => rational(&Value::Double(f64::from(*value))),
        Value::Double(value) => {
            assert!(value.is_finite());
            let bits = value.to_bits();
            let exponent_bits = ((bits >> 52) & 0x7ff) as i32;
            let mut mantissa = BigInt::from(bits & 0x000f_ffff_ffff_ffff);
            let exponent = if exponent_bits == 0 {
                1 - 1023 - 52
            } else {
                mantissa += BigInt::one() << 52;
                exponent_bits - 1023 - 52
            };
            if bits >> 63 != 0 {
                mantissa = -mantissa;
            }
            if exponent >= 0 {
                (mantissa << exponent as usize, BigInt::one())
            } else {
                (mantissa, BigInt::one() << exponent.unsigned_abs() as usize)
            }
        }
        _ => panic!("finite numeric fixture required"),
    }
}

fn next(seed: &mut u64) -> u64 {
    *seed ^= *seed << 13;
    *seed ^= *seed >> 7;
    *seed ^= *seed << 17;
    *seed
}

#[test]
fn comparison_matches_independent_exact_rationals() {
    let mut values = vec![
        Value::Long(i64::MIN),
        Value::Long(-1),
        Value::Long(0),
        Value::Long(1),
        Value::Long(i64::MAX),
        Value::Ref(0),
        Value::Ref(u64::MAX),
        Value::Double(f64::from_bits(1)),
        Value::Double(f64::MIN_POSITIVE),
        Value::Double(f64::MAX),
        Value::Double(-f64::MAX),
        Value::Double(0.1),
        Value::Float(0.1),
        Value::Double(-0.0),
        Value::Float(-0.0),
        Value::Double(0.5),
        Value::Float(0.5),
    ];
    for coefficient in [-12345, -10, -1, 0, 1, 10, 12345] {
        for scale in [-60, -1, 0, 1, 60] {
            values.push(Value::BigDec(BigDecimal::new(
                BigInt::from(coefficient),
                scale,
            )));
        }
    }
    let mut seed = 0x7468_654e_756d_6265;
    for index in 0..180 {
        let bits = next(&mut seed);
        values.push(match index % 6 {
            0 => Value::Long(bits as i64),
            1 => Value::Ref(bits),
            2 => Value::BigInt((BigInt::from(bits as i64) << 79) + BigInt::from(next(&mut seed))),
            3 => Value::BigDec(BigDecimal::new(
                (BigInt::from(bits as i64) << 97) + BigInt::from(next(&mut seed)),
                (next(&mut seed) % 121) as i64 - 60,
            )),
            // Mask out an exponent bit so the independent reference is finite.
            4 => Value::Float(f32::from_bits(bits as u32 & !0x0080_0000)),
            _ => Value::Double(f64::from_bits(bits & !0x0010_0000_0000_0000)),
        });
    }
    let references: Vec<_> = values.iter().map(rational).collect();
    let hashes: Vec<_> = values.iter().map(logical_hash).collect();
    for (left_index, left) in values.iter().enumerate() {
        for (right_index, right) in values.iter().enumerate() {
            let (ln, ld) = &references[left_index];
            let (rn, rd) = &references[right_index];
            let expected = (ln * rd).cmp(&(rn * ld));
            assert_eq!(left.index_cmp(right), expected, "{left:?} versus {right:?}");
            if expected == Ordering::Equal {
                assert_eq!(hashes[left_index], hashes[right_index]);
            }
        }
    }
}

#[test]
fn exact_binary_values_hash_like_their_full_decimal_expansion() {
    let mut values = vec![
        0.0,
        -0.0,
        0.1,
        -0.1,
        0.5,
        1.0,
        10.0,
        f64::from_bits(1),
        f64::MIN_POSITIVE,
        f64::MAX,
        -f64::MAX,
    ];
    let mut seed = 0x436f_6d70_6172_6973;
    for _ in 0..200 {
        values.push(f64::from_bits(next(&mut seed) & !0x0010_0000_0000_0000));
    }
    for value in values {
        let binary = Value::Double(value);
        let (numerator, denominator) = rational(&binary);
        // Reference denominator is a power of two, without normalization.
        let power = denominator.bits() as u32 - 1;
        let decimal = Value::BigDec(BigDecimal::new(
            numerator * BigInt::from(5).pow(power),
            i64::from(power),
        ));
        assert_eq!(binary.index_cmp(&decimal), Ordering::Equal, "{binary:?}");
        assert_eq!(logical_hash(&binary), logical_hash(&decimal), "{binary:?}");
    }
    for value in [
        0.1_f32,
        f32::from_bits(1),
        f32::MIN_POSITIVE,
        f32::MAX,
        -0.0,
    ] {
        assert_eq!(
            logical_hash(&Value::Float(value)),
            logical_hash(&Value::Double(f64::from(value)))
        );
    }
}

#[test]
fn compact_extreme_exponents_keep_exact_order_and_hash() {
    // A future reintroduction of exponent expansion should fail this subprocess,
    // not consume the test runner's machine. No limits are changed in the parent.
    const CHILD: &str = "ATOMIC_NUMERIC_EXTREME_CHILD";
    if std::env::var_os(CHILD).is_none() {
        let mut command = std::process::Command::new(std::env::current_exe().unwrap());
        command
            .args([
                "--exact",
                "value::numeric::tests::compact_extreme_exponents_keep_exact_order_and_hash",
                "--nocapture",
            ])
            .env(CHILD, "1");
        #[cfg(unix)]
        {
            use std::os::unix::process::CommandExt;
            // SAFETY: the post-fork hook invokes only async-signal-safe setrlimit
            // and errno retrieval. It neither allocates nor takes Rust locks.
            unsafe {
                command.pre_exec(|| {
                    let no_core = libc::rlimit {
                        rlim_cur: 0,
                        rlim_max: 0,
                    };
                    let memory = libc::rlimit {
                        rlim_cur: 512 * 1024 * 1024,
                        rlim_max: 512 * 1024 * 1024,
                    };
                    if libc::setrlimit(libc::RLIMIT_CORE, &no_core) != 0
                        || libc::setrlimit(libc::RLIMIT_AS, &memory) != 0
                    {
                        return Err(std::io::Error::last_os_error());
                    }
                    Ok(())
                });
            }
        }
        let output = command.output().unwrap();
        assert!(
            output.status.success(),
            "child failed: {}\n{}",
            String::from_utf8_lossy(&output.stdout),
            String::from_utf8_lossy(&output.stderr)
        );
        return;
    }
    let decimal =
        |coefficient, scale| Value::BigDec(BigDecimal::new(BigInt::from(coefficient), scale));
    for (left, right) in [
        (decimal(1, i64::MIN), decimal(10, i64::MIN + 1)),
        (decimal(1, i64::MAX - 1), decimal(10, i64::MAX)),
        (decimal(-123, i64::MIN), decimal(-1230, i64::MIN + 1)),
    ] {
        assert_eq!(left.index_cmp(&right), Ordering::Equal);
        assert_eq!(logical_hash(&left), logical_hash(&right));
        assert!(!left.stored_eq(&right));
        let left = Value::Tuple(vec![Some(left)]);
        let right = Value::Tuple(vec![Some(right)]);
        assert!(left.stored_eq(&right));
        assert_eq!(logical_hash(&left), logical_hash(&right));
    }
    let ordered = [
        Value::Double(f64::NEG_INFINITY),
        decimal(-10, i64::MIN),
        decimal(-1, i64::MIN),
        Value::Long(-1),
        Value::Double(-f64::from_bits(1)),
        decimal(-1, i64::MAX),
        Value::Long(0),
        decimal(1, i64::MAX),
        Value::Double(f64::from_bits(1)),
        Value::Long(1),
        decimal(1, i64::MIN),
        decimal(10, i64::MIN),
        Value::Double(f64::INFINITY),
        Value::Double(f64::NAN),
        Value::Bool(false),
    ];
    for (index, left) in ordered.iter().enumerate() {
        for (right_index, right) in ordered.iter().enumerate() {
            assert_eq!(left.index_cmp(right), index.cmp(&right_index));
        }
    }
    for scale in [i64::MIN, -100_000, 0, 100_000, i64::MAX] {
        let zero = decimal(0, scale);
        assert_eq!(zero, Value::Long(0));
        assert_eq!(logical_hash(&zero), logical_hash(&Value::Double(-0.0)));
    }
}
