use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use num_traits::One;
use std::cmp::Ordering;
use std::mem::size_of;

#[derive(Clone, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
pub struct Keyword {
    pub namespace: Option<String>,
    pub name: String,
}

impl Keyword {
    pub fn new(namespace: impl Into<String>, name: impl Into<String>) -> Self {
        Self {
            namespace: Some(namespace.into()),
            name: name.into(),
        }
    }

    pub fn unqualified(name: impl Into<String>) -> Self {
        Self {
            namespace: None,
            name: name.into(),
        }
    }

    pub fn qualified_name(&self) -> String {
        match &self.namespace {
            Some(namespace) => format!("{namespace}/{}", self.name),
            None => self.name.clone(),
        }
    }
}

#[derive(Clone, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
pub struct Symbol {
    pub namespace: Option<String>,
    pub name: String,
}

impl Symbol {
    pub fn new(namespace: impl Into<String>, name: impl Into<String>) -> Self {
        Self {
            namespace: Some(namespace.into()),
            name: name.into(),
        }
    }

    pub fn unqualified(name: impl Into<String>) -> Self {
        Self {
            namespace: None,
            name: name.into(),
        }
    }

    pub fn qualified_name(&self) -> String {
        match &self.namespace {
            Some(namespace) => format!("{namespace}/{}", self.name),
            None => self.name.clone(),
        }
    }
}

/// Logical values accepted by the semantic model.
///
/// `Instant` is milliseconds since the Unix epoch, matching the documented
/// representation. UUIDs are stored as their 128 bits. URI syntax validation
/// belongs at construction/API boundaries; ordering uses the retained string.
#[derive(Clone, Debug)]
pub enum Value {
    BigDec(BigDecimal),
    BigInt(BigInt),
    Bool(bool),
    Bytes(Vec<u8>),
    Double(f64),
    Float(f32),
    /// Content hash of an immutable native database-function program.
    ///
    /// This deliberately names native program content rather than retaining
    /// Datomic's JVM function object or serialized Clojure/Java code.
    Function([u8; 32]),
    Instant(i64),
    Keyword(Keyword),
    Long(i64),
    Ref(u64),
    String(String),
    Symbol(Symbol),
    Tuple(Vec<Option<Value>>),
    Uuid(u128),
    Uri(String),
}

impl Value {
    /// Bytes retained outside the inline `Value` enum allocation.
    ///
    /// This is an allocator-independent capacity account used for memory-index
    /// admission, not a claim about process RSS. Container capacities and
    /// recursively owned values are included so a large tuple of `nil`s
    /// cannot evade a byte limit merely because its canonical encoding is
    /// compact.
    pub(crate) fn retained_heap_bytes(&self) -> u64 {
        fn string_bytes(value: &String) -> u64 {
            value.capacity() as u64
        }

        match self {
            Self::BigDec(value) => value.digits(),
            Self::BigInt(value) => value.bits().div_ceil(8).max(1),
            Self::Bytes(value) => value.capacity() as u64,
            Self::Keyword(value) => value
                .namespace
                .as_ref()
                .map_or(0, string_bytes)
                .saturating_add(string_bytes(&value.name)),
            Self::String(value) | Self::Uri(value) => string_bytes(value),
            Self::Symbol(value) => value
                .namespace
                .as_ref()
                .map_or(0, string_bytes)
                .saturating_add(string_bytes(&value.name)),
            Self::Tuple(values) => (values.capacity() as u64)
                .saturating_mul(size_of::<Option<Value>>() as u64)
                .saturating_add(
                    values
                        .iter()
                        .filter_map(Option::as_ref)
                        .fold(0_u64, |total, value| {
                            total.saturating_add(value.retained_heap_bytes())
                        }),
                ),
            Self::Bool(_)
            | Self::Double(_)
            | Self::Float(_)
            | Self::Function(_)
            | Self::Instant(_)
            | Self::Long(_)
            | Self::Ref(_)
            | Self::Uuid(_) => 0,
        }
    }

    /// Datomic-style comparison used by indexes and identity checks.
    ///
    /// It follows recovered `datomic.common/compare`: numeric variants compare
    /// across types, byte arrays are length-first then signed-byte lexical, and
    /// tuples compare lexicographically with nil below every value. We use an
    /// explicit type rank for the supported non-numeric types instead of JVM
    /// class-name ordering.
    pub fn index_cmp(&self, other: &Self) -> Ordering {
        match (Numeric::from_value(self), Numeric::from_value(other)) {
            (Some(left), Some(right)) => return left.cmp(&right),
            (Some(_), None) => return Ordering::Less,
            (None, Some(_)) => return Ordering::Greater,
            (None, None) => {}
        }

        let rank = self.non_numeric_rank().cmp(&other.non_numeric_rank());
        if rank != Ordering::Equal {
            return rank;
        }

        match (self, other) {
            (Self::Bytes(left), Self::Bytes(right)) => compare_bytes(left, right),
            (Self::Keyword(left), Self::Keyword(right)) => compare_named(
                left.namespace.as_deref(),
                &left.name,
                right.namespace.as_deref(),
                &right.name,
            ),
            (Self::Symbol(left), Self::Symbol(right)) => compare_named(
                left.namespace.as_deref(),
                &left.name,
                right.namespace.as_deref(),
                &right.name,
            ),
            (Self::Function(left), Self::Function(right)) => left.cmp(right),
            (Self::Bool(left), Self::Bool(right)) => left.cmp(right),
            (Self::String(left), Self::String(right)) => compare_utf16(left, right),
            (Self::Uri(left), Self::Uri(right)) => compare_utf16(left, right),
            (Self::Instant(left), Self::Instant(right)) => left.cmp(right),
            (Self::Uuid(left), Self::Uuid(right)) => compare_uuid(*left, *right),
            (Self::Tuple(left), Self::Tuple(right)) => compare_tuple(left, right),
            _ => unreachable!("equal value ranks must have matching variants"),
        }
    }

    /// Equality used to decide whether a stored fact is redundant. The
    /// recovered implementation adds BigDecimal scale to numeric equality at
    /// this boundary even though index comparison ignores scale.
    pub fn stored_eq(&self, other: &Self) -> bool {
        self.index_cmp(other) == Ordering::Equal
            && match (self, other) {
                (Self::BigDec(left), Self::BigDec(right)) => {
                    left.fractional_digit_count() == right.fractional_digit_count()
                }
                (Self::Tuple(left), Self::Tuple(right)) => {
                    left.len() == right.len()
                        && left
                            .iter()
                            .zip(right)
                            .all(|(left, right)| match (left, right) {
                                (None, None) => true,
                                (Some(left), Some(right)) => left.stored_eq(right),
                                _ => false,
                            })
                }
                _ => true,
            }
    }

    /// Total ordering for physical/canonical storage boundaries.
    ///
    /// Datomic's logical comparator intentionally considers numerically equal
    /// values equal across representations. Its stored equality adds the
    /// BigDecimal scale distinction. Persistent Rust collections therefore
    /// need the same distinction as a final tie-breaker or two legal stored
    /// values such as `1.0M` and `1.00M` collapse into one sort position.
    pub fn stored_cmp(&self, other: &Self) -> Ordering {
        self.index_cmp(other).then_with(|| match (self, other) {
            (Self::BigDec(left), Self::BigDec(right)) => left
                .fractional_digit_count()
                .cmp(&right.fractional_digit_count()),
            (Self::Tuple(left), Self::Tuple(right)) => left
                .iter()
                .zip(right)
                .find_map(|(left, right)| {
                    let ordering = match (left, right) {
                        (None, None) => Ordering::Equal,
                        (None, Some(_)) => Ordering::Less,
                        (Some(_), None) => Ordering::Greater,
                        (Some(left), Some(right)) => left.stored_cmp(right),
                    };
                    ordering.is_ne().then_some(ordering)
                })
                .unwrap_or_else(|| left.len().cmp(&right.len())),
            _ => Ordering::Equal,
        })
    }

    pub fn is_nan(&self) -> bool {
        matches!(self, Self::Float(value) if value.is_nan())
            || matches!(self, Self::Double(value) if value.is_nan())
    }

    pub fn type_name(&self) -> &'static str {
        match self {
            Self::BigDec(_) => "bigdec",
            Self::BigInt(_) => "bigint",
            Self::Bool(_) => "boolean",
            Self::Bytes(_) => "bytes",
            Self::Double(_) => "double",
            Self::Float(_) => "float",
            Self::Function(_) => "function",
            Self::Instant(_) => "instant",
            Self::Keyword(_) => "keyword",
            Self::Long(_) => "long",
            Self::Ref(_) => "ref",
            Self::String(_) => "string",
            Self::Symbol(_) => "symbol",
            Self::Tuple(_) => "tuple",
            Self::Uuid(_) => "uuid",
            Self::Uri(_) => "uri",
        }
    }

    fn non_numeric_rank(&self) -> u8 {
        match self {
            Self::Bytes(_) => 1,
            Self::Keyword(_) => 2,
            Self::Symbol(_) => 3,
            Self::Bool(_) => 4,
            Self::String(_) => 5,
            Self::Uri(_) => 6,
            Self::Instant(_) => 7,
            Self::Uuid(_) => 8,
            Self::Tuple(_) => 9,
            // Keep all existing native ranks stable as this value type is
            // added; hashes have a deterministic bytewise order at the end.
            Self::Function(_) => 10,
            Self::BigDec(_)
            | Self::BigInt(_)
            | Self::Double(_)
            | Self::Float(_)
            | Self::Long(_)
            | Self::Ref(_) => 0,
        }
    }
}

impl PartialEq for Value {
    fn eq(&self, other: &Self) -> bool {
        self.index_cmp(other) == Ordering::Equal
    }
}

impl Eq for Value {}

fn compare_bytes(left: &[u8], right: &[u8]) -> Ordering {
    left.len().cmp(&right.len()).then_with(|| {
        left.iter()
            .map(|byte| *byte as i8)
            .cmp(right.iter().map(|byte| *byte as i8))
    })
}

fn compare_utf16(left: &str, right: &str) -> Ordering {
    left.encode_utf16().cmp(right.encode_utf16())
}

fn compare_named(
    left_namespace: Option<&str>,
    left_name: &str,
    right_namespace: Option<&str>,
    right_name: &str,
) -> Ordering {
    match (left_namespace, right_namespace) {
        (None, None) => compare_utf16(left_name, right_name),
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => {
            compare_utf16(left, right).then_with(|| compare_utf16(left_name, right_name))
        }
    }
}

fn compare_uuid(left: u128, right: u128) -> Ordering {
    let left = ((left >> 64) as u64 as i64, left as u64 as i64);
    let right = ((right >> 64) as u64 as i64, right as u64 as i64);
    left.cmp(&right)
}

fn compare_tuple(left: &[Option<Value>], right: &[Option<Value>]) -> Ordering {
    for (left, right) in left.iter().zip(right) {
        let ordering = match (left, right) {
            (None, None) => Ordering::Equal,
            (None, Some(_)) => Ordering::Less,
            (Some(_), None) => Ordering::Greater,
            (Some(left), Some(right)) => left.index_cmp(right),
        };
        if ordering != Ordering::Equal {
            return ordering;
        }
    }
    left.len().cmp(&right.len())
}

#[derive(Clone, Debug)]
enum Numeric {
    NegativeInfinity,
    Finite {
        numerator: BigInt,
        denominator: BigInt,
    },
    PositiveInfinity,
    NaN,
}

impl Numeric {
    fn from_value(value: &Value) -> Option<Self> {
        match value {
            Value::Long(value) => Some(Self::integer(BigInt::from(*value))),
            Value::Ref(value) => Some(Self::integer(BigInt::from(*value))),
            Value::BigInt(value) => Some(Self::integer(value.clone())),
            Value::BigDec(value) => {
                let (mut numerator, scale) = value.as_bigint_and_exponent();
                if scale >= 0 {
                    Some(Self::Finite {
                        numerator,
                        denominator: pow10(scale as u64),
                    })
                } else {
                    numerator *= pow10(scale.unsigned_abs());
                    Some(Self::integer(numerator))
                }
            }
            Value::Float(value) => Some(Self::from_f32(*value)),
            Value::Double(value) => Some(Self::from_f64(*value)),
            _ => None,
        }
    }

    fn integer(numerator: BigInt) -> Self {
        Self::Finite {
            numerator,
            denominator: BigInt::one(),
        }
    }

    fn from_f32(value: f32) -> Self {
        if value.is_nan() {
            return Self::NaN;
        }
        if value == f32::INFINITY {
            return Self::PositiveInfinity;
        }
        if value == f32::NEG_INFINITY {
            return Self::NegativeInfinity;
        }
        let bits = value.to_bits();
        let negative = bits >> 31 != 0;
        let exponent_bits = ((bits >> 23) & 0xff) as i32;
        let fraction = bits & 0x7f_ffff;
        let (significand, exponent) = if exponent_bits == 0 {
            (BigInt::from(fraction), 1 - 127 - 23)
        } else {
            (BigInt::from((1 << 23) | fraction), exponent_bits - 127 - 23)
        };
        Self::binary(significand, exponent, negative)
    }

    fn from_f64(value: f64) -> Self {
        if value.is_nan() {
            return Self::NaN;
        }
        if value == f64::INFINITY {
            return Self::PositiveInfinity;
        }
        if value == f64::NEG_INFINITY {
            return Self::NegativeInfinity;
        }
        let bits = value.to_bits();
        let negative = bits >> 63 != 0;
        let exponent_bits = ((bits >> 52) & 0x7ff) as i32;
        let fraction = bits & 0x000f_ffff_ffff_ffff;
        let (significand, exponent) = if exponent_bits == 0 {
            (BigInt::from(fraction), 1 - 1023 - 52)
        } else {
            (
                BigInt::from((1_u64 << 52) | fraction),
                exponent_bits - 1023 - 52,
            )
        };
        Self::binary(significand, exponent, negative)
    }

    fn binary(mut numerator: BigInt, exponent: i32, negative: bool) -> Self {
        if negative {
            numerator = -numerator;
        }
        if exponent >= 0 {
            numerator <<= exponent as usize;
            Self::integer(numerator)
        } else {
            Self::Finite {
                numerator,
                denominator: BigInt::one() << exponent.unsigned_abs() as usize,
            }
        }
    }

    fn cmp(&self, other: &Self) -> Ordering {
        use Numeric::*;
        match (self, other) {
            (NaN, NaN) => Ordering::Equal,
            (NaN, _) => Ordering::Greater,
            (_, NaN) => Ordering::Less,
            (NegativeInfinity, NegativeInfinity) | (PositiveInfinity, PositiveInfinity) => {
                Ordering::Equal
            }
            (NegativeInfinity, _) | (_, PositiveInfinity) => Ordering::Less,
            (PositiveInfinity, _) | (_, NegativeInfinity) => Ordering::Greater,
            (
                Finite {
                    numerator: left_numerator,
                    denominator: left_denominator,
                },
                Finite {
                    numerator: right_numerator,
                    denominator: right_denominator,
                },
            ) => (left_numerator * right_denominator).cmp(&(right_numerator * left_denominator)),
        }
    }
}

fn pow10(exponent: u64) -> BigInt {
    let mut result = BigInt::one();
    let mut base = BigInt::from(10_u8);
    let mut exponent = exponent;
    while exponent > 0 {
        if exponent & 1 == 1 {
            result *= &base;
        }
        exponent >>= 1;
        if exponent > 0 {
            base = &base * &base;
        }
    }
    result
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    #[test]
    fn numbers_compare_across_representations() {
        let one = Value::Long(1);
        assert_eq!(one.index_cmp(&Value::Double(1.0)), Ordering::Equal);
        assert_eq!(
            one.index_cmp(&Value::BigInt(BigInt::one())),
            Ordering::Equal
        );
        assert_eq!(
            one.index_cmp(&Value::BigDec(BigDecimal::from_str("1.00").unwrap())),
            Ordering::Equal
        );
        assert_eq!(
            Value::Double(0.1).index_cmp(&Value::BigDec(BigDecimal::from_str("0.1").unwrap())),
            Ordering::Greater,
            "binary 0.1 is slightly greater than exact decimal 0.1"
        );
    }

    #[test]
    fn big_decimal_storage_equality_preserves_scale() {
        let one = Value::BigDec(BigDecimal::from_str("1.0").unwrap());
        let another = Value::BigDec(BigDecimal::from_str("1.00").unwrap());
        assert_eq!(one.index_cmp(&another), Ordering::Equal);
        assert!(!one.stored_eq(&another));
    }

    #[test]
    fn byte_order_matches_recovered_length_then_signed_bytes() {
        assert_eq!(
            Value::Bytes(vec![255]).index_cmp(&Value::Bytes(vec![0])),
            Ordering::Less
        );
        assert_eq!(
            Value::Bytes(vec![255, 255]).index_cmp(&Value::Bytes(vec![0])),
            Ordering::Greater
        );
    }

    #[test]
    fn tuple_nil_is_low_and_comparison_is_lexicographic() {
        let low = Value::Tuple(vec![Some(Value::Long(1)), None]);
        let high = Value::Tuple(vec![Some(Value::Long(1)), Some(Value::Long(0))]);
        assert_eq!(low.index_cmp(&high), Ordering::Less);
    }

    #[test]
    fn float_special_values_have_a_stable_order() {
        assert_eq!(
            Value::Double(f64::NEG_INFINITY).index_cmp(&Value::Long(i64::MIN)),
            Ordering::Less
        );
        assert_eq!(
            Value::Double(f64::NAN).index_cmp(&Value::Double(f64::INFINITY)),
            Ordering::Greater
        );
        assert_eq!(
            Value::Double(-0.0).index_cmp(&Value::Long(0)),
            Ordering::Equal
        );
    }

    #[test]
    fn supported_value_order_is_total_and_transitive() {
        let values = vec![
            Value::Long(-1),
            Value::BigInt(BigInt::from(0)),
            Value::Float(0.5),
            Value::Double(f64::INFINITY),
            Value::Double(f64::NAN),
            Value::Function([0x5a; 32]),
            Value::Bytes(vec![255]),
            Value::Keyword(Keyword::new("a", "b")),
            Value::Symbol(Symbol::new("a", "b")),
            Value::Bool(false),
            Value::String("s".into()),
            Value::Uri("https://example.test".into()),
            Value::Instant(0),
            Value::Uuid(0),
            Value::Tuple(vec![None, Some(Value::Long(1))]),
        ];
        for left in &values {
            for right in &values {
                assert_eq!(left.index_cmp(right), right.index_cmp(left).reverse());
                for third in &values {
                    if left.index_cmp(right) != Ordering::Greater
                        && right.index_cmp(third) != Ordering::Greater
                    {
                        assert_ne!(left.index_cmp(third), Ordering::Greater);
                    }
                }
            }
        }
    }

    #[test]
    fn strings_use_java_compatible_utf16_order() {
        // U+10000 sorts below U+E000 by UTF-16 code units, but above it by
        // Unicode scalar value and Rust's default UTF-8 byte ordering.
        assert_eq!(
            Value::String("\u{10000}".into()).index_cmp(&Value::String("\u{e000}".into())),
            Ordering::Less
        );
    }

    #[test]
    fn function_values_compare_by_immutable_content_hash() {
        let low = Value::Function([0; 32]);
        let mut high_hash = [0; 32];
        high_hash[31] = 1;
        let high = Value::Function(high_hash);

        assert_eq!(low.index_cmp(&high), Ordering::Less);
        assert_eq!(low.stored_cmp(&low.clone()), Ordering::Equal);
        assert_eq!(low.type_name(), "function");
    }
}
