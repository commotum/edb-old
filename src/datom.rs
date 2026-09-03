use crate::Value;
use std::cmp::Ordering;

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
    pub fn cmp_in(&self, other: &Self, index: IndexOrder) -> Ordering {
        let ordering = match index {
            IndexOrder::Eavt => self
                .entity
                .cmp(&other.entity)
                .then(self.attribute.cmp(&other.attribute))
                .then_with(|| self.value.index_cmp(&other.value)),
            IndexOrder::Aevt => self
                .attribute
                .cmp(&other.attribute)
                .then(self.entity.cmp(&other.entity))
                .then_with(|| self.value.index_cmp(&other.value)),
            IndexOrder::Avet => self
                .attribute
                .cmp(&other.attribute)
                .then_with(|| self.value.index_cmp(&other.value))
                .then(self.entity.cmp(&other.entity)),
            IndexOrder::Vaet => self
                .value
                .index_cmp(&other.value)
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

    #[test]
    fn transaction_sorts_descending_and_assertion_first() {
        let older = Datom {
            entity: 1,
            attribute: 2,
            value: Value::Long(3),
            tx: 4,
            added: true,
        };
        let mut newer = older.clone();
        newer.tx = 5;
        assert_eq!(newer.cmp_in(&older, IndexOrder::Eavt), Ordering::Less);

        let mut retraction = newer.clone();
        retraction.added = false;
        assert_eq!(newer.cmp_in(&retraction, IndexOrder::Eavt), Ordering::Less);
    }
}
