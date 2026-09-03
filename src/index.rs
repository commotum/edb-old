use crate::{Datom, IndexOrder, Schema, SemanticError, Value, ValueType};
use std::cmp::Ordering;
use std::sync::Arc;

/// A typed prefix for one of Datomic's four index orders.
///
/// The shape prevents callers from accidentally supplying components in the
/// wrong order. Optional components must be contiguous from the left.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum IndexPrefix {
    Eavt {
        entity: u64,
        attribute: Option<u32>,
        value: Option<Value>,
    },
    Aevt {
        attribute: u32,
        entity: Option<u64>,
        value: Option<Value>,
    },
    Avet {
        attribute: u32,
        value: Option<Value>,
        entity: Option<u64>,
    },
    Vaet {
        value: Value,
        attribute: Option<u32>,
        entity: Option<u64>,
    },
}

impl IndexPrefix {
    pub fn order(&self) -> IndexOrder {
        match self {
            Self::Eavt { .. } => IndexOrder::Eavt,
            Self::Aevt { .. } => IndexOrder::Aevt,
            Self::Avet { .. } => IndexOrder::Avet,
            Self::Vaet { .. } => IndexOrder::Vaet,
        }
    }

    pub(crate) fn validate(&self) -> Result<(), SemanticError> {
        let has_gap = match self {
            Self::Eavt {
                attribute, value, ..
            } => attribute.is_none() && value.is_some(),
            Self::Aevt { entity, value, .. } => entity.is_none() && value.is_some(),
            Self::Avet { value, entity, .. } => value.is_none() && entity.is_some(),
            Self::Vaet {
                attribute, entity, ..
            } => attribute.is_none() && entity.is_some(),
        };
        if has_gap {
            return Err(SemanticError::incorrect(
                "index/non-contiguous-prefix",
                "index prefix components must be contiguous from the left",
            ));
        }
        Ok(())
    }
}

/// Immutable roots for a single database value.
///
/// `Arc` gives snapshots cheap clones and makes the ownership boundary match
/// the recovered immutable database/index-root design. Current roots are
/// rebuilt from a transaction's small in-memory working set for now; the
/// PostgreSQL milestone can replace their construction without changing the
/// read contract.
#[derive(Clone, Debug, Default)]
pub(crate) struct IndexRoots {
    eavt: Arc<[Datom]>,
    aevt: Arc<[Datom]>,
    avet: Arc<[Datom]>,
    vaet: Arc<[Datom]>,
}

impl IndexRoots {
    pub(crate) fn build(schema: &Schema, datoms: impl IntoIterator<Item = Datom>) -> Self {
        let all: Vec<_> = datoms.into_iter().collect();
        let eavt = sorted(all.clone(), IndexOrder::Eavt);
        let aevt = sorted(all.clone(), IndexOrder::Aevt);
        let avet = sorted(
            all.iter()
                .filter(|datom| {
                    schema
                        .attribute(datom.attribute)
                        .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some())
                })
                .cloned()
                .collect(),
            IndexOrder::Avet,
        );
        let vaet = sorted(
            all.into_iter()
                .filter(|datom| {
                    schema
                        .attribute(datom.attribute)
                        .is_ok_and(|attribute| attribute.value_type == ValueType::Ref)
                })
                .collect(),
            IndexOrder::Vaet,
        );
        Self {
            eavt: eavt.into(),
            aevt: aevt.into(),
            avet: avet.into(),
            vaet: vaet.into(),
        }
    }

    pub(crate) fn get(&self, order: IndexOrder) -> &[Datom] {
        match order {
            IndexOrder::Eavt => &self.eavt,
            IndexOrder::Aevt => &self.aevt,
            IndexOrder::Avet => &self.avet,
            IndexOrder::Vaet => &self.vaet,
        }
    }

    pub(crate) fn matching(&self, prefix: &IndexPrefix) -> Result<&[Datom], SemanticError> {
        prefix.validate()?;
        let datoms = self.get(prefix.order());
        let start = datoms.partition_point(|datom| compare_prefix(datom, prefix).is_lt());
        let len = datoms[start..].partition_point(|datom| compare_prefix(datom, prefix).is_eq());
        Ok(&datoms[start..start + len])
    }

    pub(crate) fn seek(&self, prefix: &IndexPrefix) -> Result<&[Datom], SemanticError> {
        prefix.validate()?;
        let datoms = self.get(prefix.order());
        let start = datoms.partition_point(|datom| compare_prefix(datom, prefix).is_lt());
        Ok(&datoms[start..])
    }

    pub(crate) fn reverse_seek(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        prefix.validate()?;
        let datoms = self.get(prefix.order());
        let end = datoms.partition_point(|datom| !compare_prefix(datom, prefix).is_gt());
        Ok(datoms[..end].iter().rev().cloned().collect())
    }

    pub(crate) fn avet_range(
        &self,
        attribute: u32,
        start: Option<&Value>,
        end: Option<&Value>,
    ) -> &[Datom] {
        let datoms = self.get(IndexOrder::Avet);
        let lower = datoms.partition_point(|datom| {
            datom.attribute < attribute
                || (datom.attribute == attribute
                    && start.is_some_and(|start| datom.value.index_cmp(start).is_lt()))
        });
        let upper = datoms.partition_point(|datom| {
            datom.attribute < attribute
                || (datom.attribute == attribute
                    && end.is_none_or(|end| datom.value.index_cmp(end).is_lt()))
        });
        &datoms[lower..upper]
    }
}

fn sorted(mut datoms: Vec<Datom>, order: IndexOrder) -> Vec<Datom> {
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    datoms
}

pub(crate) fn compare_prefix(datom: &Datom, prefix: &IndexPrefix) -> Ordering {
    match prefix {
        IndexPrefix::Eavt {
            entity,
            attribute,
            value,
        } => datom
            .entity
            .cmp(entity)
            .then_with(|| attribute.map_or(Ordering::Equal, |a| datom.attribute.cmp(&a)))
            .then_with(|| {
                value
                    .as_ref()
                    .map_or(Ordering::Equal, |v| datom.value.index_cmp(v))
            }),
        IndexPrefix::Aevt {
            attribute,
            entity,
            value,
        } => datom
            .attribute
            .cmp(attribute)
            .then_with(|| entity.map_or(Ordering::Equal, |e| datom.entity.cmp(&e)))
            .then_with(|| {
                value
                    .as_ref()
                    .map_or(Ordering::Equal, |v| datom.value.index_cmp(v))
            }),
        IndexPrefix::Avet {
            attribute,
            value,
            entity,
        } => datom
            .attribute
            .cmp(attribute)
            .then_with(|| {
                value
                    .as_ref()
                    .map_or(Ordering::Equal, |v| datom.value.index_cmp(v))
            })
            .then_with(|| entity.map_or(Ordering::Equal, |e| datom.entity.cmp(&e))),
        IndexPrefix::Vaet {
            value,
            attribute,
            entity,
        } => datom
            .value
            .index_cmp(value)
            .then_with(|| attribute.map_or(Ordering::Equal, |a| datom.attribute.cmp(&a)))
            .then_with(|| entity.map_or(Ordering::Equal, |e| datom.entity.cmp(&e))),
    }
}
