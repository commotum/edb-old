use crate::Value;
use std::cmp::Ordering;
use std::mem::size_of;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Datom {
    pub entity: u64,
    pub attribute: u32,
    pub value: Value,
    pub tx: u64,
    pub added: bool,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum IndexOrder {
    Eavt,
    Aevt,
    Avet,
    Vaet,
}

impl Datom {
    /// Deterministic account of the inline datom plus recursively owned value
    /// storage. It intentionally excludes allocator headers and shared index
    /// locators, which callers add according to their representation.
    pub(crate) fn retained_bytes(&self) -> u64 {
        (size_of::<Self>() as u64).saturating_add(self.value.retained_heap_bytes())
    }

    pub fn cmp_in(&self, other: &Self, index: IndexOrder) -> Ordering {
        let ordering = match index {
            IndexOrder::Eavt => self
                .entity
                .cmp(&other.entity)
                .then(self.attribute.cmp(&other.attribute))
                .then_with(|| self.value.stored_cmp(&other.value)),
            IndexOrder::Aevt => self
                .attribute
                .cmp(&other.attribute)
                .then(self.entity.cmp(&other.entity))
                .then_with(|| self.value.stored_cmp(&other.value)),
            IndexOrder::Avet => self
                .attribute
                .cmp(&other.attribute)
                .then_with(|| self.value.stored_cmp(&other.value))
                .then(self.entity.cmp(&other.entity)),
            IndexOrder::Vaet => self
                .value
                .stored_cmp(&other.value)
                .then(self.attribute.cmp(&other.attribute))
                .then(self.entity.cmp(&other.entity)),
        };
        ordering
            .then_with(|| other.tx.cmp(&self.tx))
            .then_with(|| other.added.cmp(&self.added))
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::t_to_tx;

    #[test]
    fn transaction_sorts_descending_and_assertion_first() {
        let older = Datom {
            entity: 1,
            attribute: 2,
            value: Value::Long(3),
            tx: t_to_tx(4).unwrap(),
            added: true,
        };
        let mut newer = older.clone();
        newer.tx = t_to_tx(5).unwrap();
        assert_eq!(newer.cmp_in(&older, IndexOrder::Eavt), Ordering::Less);

        let mut retraction = newer.clone();
        retraction.added = false;
        assert_eq!(newer.cmp_in(&retraction, IndexOrder::Eavt), Ordering::Less);
    }
}
