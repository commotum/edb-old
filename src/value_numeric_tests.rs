use super::*;
use bigdecimal::BigDecimal;
use num_traits::One;
use std::alloc::{GlobalAlloc, Layout, System};
use std::cell::Cell;
use std::collections::hash_map::DefaultHasher;
use std::hint::black_box;
use std::time::Instant;

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

#[derive(Clone, Copy, Debug, Default, PartialEq, Eq)]
struct Allocations {
    calls: usize,
    bytes: usize,
    live: usize,
    peak: usize,
}

thread_local! {
    static TRACK: Cell<Option<Allocations>> = const { Cell::new(None) };
}

struct CountingAllocator;

#[global_allocator]
static ALLOCATOR: CountingAllocator = CountingAllocator;

fn allocated(bytes: usize) {
    let _ = TRACK.try_with(|tracking| {
        if let Some(mut totals) = tracking.get() {
            totals.calls += 1;
            totals.bytes += bytes;
            totals.live += bytes;
            totals.peak = totals.peak.max(totals.live);
            tracking.set(Some(totals));
        }
    });
}

fn deallocated(bytes: usize) {
    let _ = TRACK.try_with(|tracking| {
        if let Some(mut totals) = tracking.get() {
            totals.live = totals.live.saturating_sub(bytes);
            tracking.set(Some(totals));
        }
    });
}

// SAFETY: all allocations/layouts are forwarded unchanged to System. The
// per-thread counters contain no allocation, and tracking is off by default.
unsafe impl GlobalAlloc for CountingAllocator {
    unsafe fn alloc(&self, layout: Layout) -> *mut u8 {
        let pointer = unsafe { System.alloc(layout) };
        if !pointer.is_null() {
            allocated(layout.size());
        }
        pointer
    }
    unsafe fn alloc_zeroed(&self, layout: Layout) -> *mut u8 {
        let pointer = unsafe { System.alloc_zeroed(layout) };
        if !pointer.is_null() {
            allocated(layout.size());
        }
        pointer
    }
    unsafe fn dealloc(&self, pointer: *mut u8, layout: Layout) {
        deallocated(layout.size());
        unsafe {
            System.dealloc(pointer, layout);
        }
    }
    unsafe fn realloc(&self, pointer: *mut u8, layout: Layout, new_size: usize) -> *mut u8 {
        let pointer = unsafe { System.realloc(pointer, layout, new_size) };
        if !pointer.is_null() {
            deallocated(layout.size());
            allocated(new_size);
        }
        pointer
    }
}

fn measure(operation: impl FnOnce()) -> (Allocations, std::time::Duration) {
    struct Reset;
    impl Drop for Reset {
        fn drop(&mut self) {
            TRACK.with(|tracking| tracking.set(None));
        }
    }
    TRACK.with(|tracking| {
        assert!(tracking.get().is_none());
        tracking.set(Some(Allocations::default()));
    });
    let reset = Reset;
    let start = Instant::now();
    operation(); // Includes temporary destruction, not just the comparison loop.
    let elapsed = start.elapsed();
    let allocations = TRACK.with(|tracking| tracking.get().unwrap());
    drop(reset);
    (allocations, elapsed)
}

#[test]
fn complete_integer_comparison_and_hash_are_allocation_free() {
    let values = [
        Value::Long(i64::MIN),
        Value::Long(-1),
        Value::Long(0),
        Value::Long(1),
        Value::Long(10),
        Value::Long(i64::MAX),
        Value::Ref(0),
        Value::Ref(1),
        Value::Ref(10),
        Value::Ref(i64::MAX as u64),
        Value::Ref(u64::MAX),
    ];
    let (allocations, elapsed) = measure(|| {
        for _ in 0..1000 {
            for left in &values {
                black_box(logical_hash(black_box(left)));
                for right in &values {
                    black_box(black_box(left).index_cmp(black_box(right)));
                }
            }
        }
    });
    assert_eq!(allocations, Allocations::default());
    eprintln!(
        "integer identity: 121000 compares + 11000 hashes, {allocations:?}, complete={elapsed:?}"
    );
}

#[test]
fn comparison_and_hash_cost_follow_coefficient_not_exponent_span() {
    for digits in [3_u32, 1024] {
        let coefficient = BigInt::from(10).pow(digits - 1) + BigInt::from(23);
        let mut baseline = None;
        for scale in [0, 1000, 100_000, i64::MAX - 1, i64::MIN] {
            let left = Value::BigDec(BigDecimal::new(coefficient.clone(), scale));
            let right = Value::BigDec(BigDecimal::new(&coefficient * 10, scale + 1));
            let (allocations, elapsed) = measure(|| {
                for _ in 0..100 {
                    assert_eq!(
                        black_box(&left).index_cmp(black_box(&right)),
                        Ordering::Equal
                    );
                    assert_eq!(
                        logical_hash(black_box(&left)),
                        logical_hash(black_box(&right))
                    );
                }
            });
            assert_eq!(
                allocations.live, 0,
                "all comparison/hash temporaries released"
            );
            if digits == 3 {
                assert_eq!(allocations, Allocations::default());
            }
            assert_eq!(
                *baseline.get_or_insert(allocations),
                allocations,
                "cost changed with exponent"
            );
            eprintln!(
                "numeric identity: digits={digits} scale={scale} 100 compares + 200 hashes, {allocations:?}, complete={elapsed:?}"
            );
        }
    }
}
