use edb_encoding::ValueType;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ValueTypeSet {
    mask: u32,
}

impl Default for ValueTypeSet {
    fn default() -> Self {
        Self::any()
    }
}

impl ValueTypeSet {
    pub fn any() -> Self {
        let mut mask = 0u32;
        for vt in ALL_VALUE_TYPES {
            mask |= bit(vt);
        }
        Self { mask }
    }

    pub fn none() -> Self {
        Self { mask: 0 }
    }

    pub fn of_one(vt: ValueType) -> Self {
        Self { mask: bit(vt) }
    }

    pub fn of_numeric_types() -> Self {
        let mut set = Self::none();
        for vt in NUMERIC_TYPES {
            set.insert(vt);
        }
        set
    }

    pub fn of_numeric_and_instant_types() -> Self {
        let mut set = Self::of_numeric_types();
        set.insert(ValueType::Instant);
        set
    }

    pub fn of_keywords() -> Self {
        let mut set = Self::none();
        set.insert(ValueType::Ref);
        set.insert(ValueType::Keyword);
        set
    }

    pub fn of_longs() -> Self {
        let mut set = Self::none();
        set.insert(ValueType::Ref);
        set.insert(ValueType::Long);
        set
    }

    pub fn insert(&mut self, vt: ValueType) -> bool {
        let before = self.mask;
        self.mask |= bit(vt);
        before != self.mask
    }

    pub fn len(&self) -> usize {
        self.mask.count_ones() as usize
    }

    pub fn union(&self, other: &ValueTypeSet) -> ValueTypeSet {
        ValueTypeSet { mask: self.mask | other.mask }
    }

    pub fn intersection(&self, other: &ValueTypeSet) -> ValueTypeSet {
        ValueTypeSet { mask: self.mask & other.mask }
    }

    pub fn difference(&self, other: &ValueTypeSet) -> ValueTypeSet {
        ValueTypeSet { mask: self.mask & !other.mask }
    }

    pub fn exemplar(&self) -> Option<ValueType> {
        self.iter().next()
    }

    pub fn is_subset(&self, other: &ValueTypeSet) -> bool {
        (self.mask & !other.mask) == 0
    }

    pub fn is_disjoint(&self, other: &ValueTypeSet) -> bool {
        (self.mask & other.mask) == 0
    }

    pub fn contains(&self, vt: ValueType) -> bool {
        (self.mask & bit(vt)) != 0
    }

    pub fn is_empty(&self) -> bool {
        self.mask == 0
    }

    pub fn is_unit(&self) -> bool {
        self.len() == 1
    }

    pub fn is_only_numeric(&self) -> bool {
        self.is_subset(&ValueTypeSet::of_numeric_types())
    }

    pub fn iter(&self) -> impl Iterator<Item = ValueType> + '_ {
        ALL_VALUE_TYPES
            .iter()
            .copied()
            .filter(|vt| self.contains(*vt))
    }
}

impl From<ValueType> for ValueTypeSet {
    fn from(t: ValueType) -> Self {
        ValueTypeSet::of_one(t)
    }
}

impl std::iter::FromIterator<ValueType> for ValueTypeSet {
    fn from_iter<I: IntoIterator<Item = ValueType>>(iter: I) -> Self {
        let mut set = ValueTypeSet::none();
        for vt in iter {
            set.insert(vt);
        }
        set
    }
}

impl std::iter::Extend<ValueType> for ValueTypeSet {
    fn extend<I: IntoIterator<Item = ValueType>>(&mut self, iter: I) {
        for vt in iter {
            self.insert(vt);
        }
    }
}

const ALL_VALUE_TYPES: [ValueType; 15] = [
    ValueType::Long,
    ValueType::Double,
    ValueType::Boolean,
    ValueType::String,
    ValueType::Keyword,
    ValueType::Uuid,
    ValueType::Instant,
    ValueType::Ref,
    ValueType::Bytes,
    ValueType::Uint8,
    ValueType::Bigint,
    ValueType::Decimal,
    ValueType::Float32,
    ValueType::Float16,
    ValueType::Bfloat16,
];

const NUMERIC_TYPES: [ValueType; 7] = [
    ValueType::Long,
    ValueType::Double,
    ValueType::Uint8,
    ValueType::Bigint,
    ValueType::Decimal,
    ValueType::Float32,
    ValueType::Float16,
];

const fn bit(vt: ValueType) -> u32 {
    1u32 << (vt as u32)
}
