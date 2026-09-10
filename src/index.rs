use crate::identity::validate_permanent_eid;
use crate::{Datom, IndexOrder, Schema, SemanticError, Value, ValueType, t_to_tx, tx_to_t};
use std::cmp::Ordering;
use std::sync::Arc;

/// A transaction component supplied to a raw index boundary.
///
/// Datomic accepts both a logical basis `t` and the corresponding reified
/// transaction entity. Keeping those forms distinct prevents an arbitrary
/// entity id from being silently treated as a transaction while still
/// preserving the recovered `eid->eidx` normalization.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum IndexTransaction {
    T(u64),
    Tx(u64),
}

impl IndexTransaction {
    /// Normalize either public form to the transaction entity stored in a
    /// native datom. Both paths are checked: a `T` must fit the recovered
    /// entity-index domain and a `Tx` must belong to the transaction partition.
    pub fn stored_tx(self) -> Result<u64, SemanticError> {
        match self {
            Self::T(t) => t_to_tx(t),
            Self::Tx(tx) => {
                tx_to_t(tx)?;
                Ok(tx)
            }
        }
    }
}

/// A left-contiguous sequence of zero through four typed index components.
///
/// The type parameters are the component types *in index order*. An enum is
/// used instead of four `Option`s so a gap such as `[a, nil, v]` cannot be
/// represented. `Four` deliberately stops at T: assertion/retraction is not a
/// fifth public raw-index component.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum IndexComponents<First, Second, Third, Fourth> {
    Empty,
    One(First),
    Two(First, Second),
    Three(First, Second, Third),
    Four(First, Second, Third, Fourth),
}

impl<First, Second, Third, Fourth> IndexComponents<First, Second, Third, Fourth> {
    pub const fn len(&self) -> usize {
        match self {
            Self::Empty => 0,
            Self::One(_) => 1,
            Self::Two(_, _) => 2,
            Self::Three(_, _, _) => 3,
            Self::Four(_, _, _, _) => 4,
        }
    }

    pub const fn is_empty(&self) -> bool {
        matches!(self, Self::Empty)
    }
}

/// A typed virtual position in one of Datomic's four raw index orders.
///
/// E/A/V/T component order is encoded in each variant. E and A are already
/// resolved numeric ids and V is already normalized for the attribute. The
/// immutable database API owns the schema/ident/lookup-ref normalization that
/// produces this type; [`IndexBoundary::validate`] is the storage-independent
/// final check before a cursor uses it.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum IndexBoundary {
    Eavt(IndexComponents<u64, u32, Value, IndexTransaction>),
    Aevt(IndexComponents<u32, u64, Value, IndexTransaction>),
    Avet(IndexComponents<u32, Value, u64, IndexTransaction>),
    Vaet(IndexComponents<Value, u32, u64, IndexTransaction>),
}

impl IndexBoundary {
    pub const fn order(&self) -> IndexOrder {
        match self {
            Self::Eavt(_) => IndexOrder::Eavt,
            Self::Aevt(_) => IndexOrder::Aevt,
            Self::Avet(_) => IndexOrder::Avet,
            Self::Vaet(_) => IndexOrder::Vaet,
        }
    }

    pub const fn len(&self) -> usize {
        match self {
            Self::Eavt(components) => components.len(),
            Self::Aevt(components) => components.len(),
            Self::Avet(components) => components.len(),
            Self::Vaet(components) => components.len(),
        }
    }

    pub const fn is_empty(&self) -> bool {
        match self {
            Self::Eavt(components) => components.is_empty(),
            Self::Aevt(components) => components.is_empty(),
            Self::Avet(components) => components.is_empty(),
            Self::Vaet(components) => components.is_empty(),
        }
    }

    /// The qualified attribute whose physical AVET readiness must be checked.
    /// An empty AVET boundary intentionally has no attribute qualification.
    pub const fn avet_attribute(&self) -> Option<u32> {
        match self {
            Self::Avet(IndexComponents::One(attribute))
            | Self::Avet(IndexComponents::Two(attribute, _))
            | Self::Avet(IndexComponents::Three(attribute, _, _))
            | Self::Avet(IndexComponents::Four(attribute, _, _, _)) => Some(*attribute),
            _ => None,
        }
    }

    /// Validate resolved identifiers and normalize T/Tx exactly once before
    /// traversal. Schema-directed V validation and ident/lookup resolution
    /// remain at the immutable database boundary, where the exact schema and
    /// ident map are available.
    pub fn validate(&self) -> Result<(), SemanticError> {
        self.normalized().map(|_| ())
    }

    pub(crate) fn normalized(&self) -> Result<NormalizedIndexBoundary, SemanticError> {
        fn entity(entity: u64) -> Result<u64, SemanticError> {
            validate_permanent_eid(entity)?;
            Ok(entity)
        }

        fn normalized_value(value: &Value) -> Result<Value, SemanticError> {
            if let Value::Ref(entity) = value {
                validate_permanent_eid(*entity)?;
            }
            Ok(value.clone())
        }

        fn vaet_value(value: &Value) -> Result<Value, SemanticError> {
            if !matches!(value, Value::Ref(_)) {
                return Err(SemanticError::incorrect(
                    "index/vaet-value-not-ref",
                    "VAET boundary value must resolve to an entity reference",
                ));
            }
            normalized_value(value)
        }

        Ok(match self {
            Self::Eavt(components) => NormalizedIndexBoundary::Eavt(match components {
                IndexComponents::Empty => IndexComponents::Empty,
                IndexComponents::One(e) => IndexComponents::One(entity(*e)?),
                IndexComponents::Two(e, a) => IndexComponents::Two(entity(*e)?, *a),
                IndexComponents::Three(e, a, v) => {
                    IndexComponents::Three(entity(*e)?, *a, normalized_value(v)?)
                }
                IndexComponents::Four(e, a, v, t) => {
                    IndexComponents::Four(entity(*e)?, *a, normalized_value(v)?, t.stored_tx()?)
                }
            }),
            Self::Aevt(components) => NormalizedIndexBoundary::Aevt(match components {
                IndexComponents::Empty => IndexComponents::Empty,
                IndexComponents::One(a) => IndexComponents::One(*a),
                IndexComponents::Two(a, e) => IndexComponents::Two(*a, entity(*e)?),
                IndexComponents::Three(a, e, v) => {
                    IndexComponents::Three(*a, entity(*e)?, normalized_value(v)?)
                }
                IndexComponents::Four(a, e, v, t) => {
                    IndexComponents::Four(*a, entity(*e)?, normalized_value(v)?, t.stored_tx()?)
                }
            }),
            Self::Avet(components) => NormalizedIndexBoundary::Avet(match components {
                IndexComponents::Empty => IndexComponents::Empty,
                IndexComponents::One(a) => IndexComponents::One(*a),
                IndexComponents::Two(a, v) => IndexComponents::Two(*a, normalized_value(v)?),
                IndexComponents::Three(a, v, e) => {
                    IndexComponents::Three(*a, normalized_value(v)?, entity(*e)?)
                }
                IndexComponents::Four(a, v, e, t) => {
                    IndexComponents::Four(*a, normalized_value(v)?, entity(*e)?, t.stored_tx()?)
                }
            }),
            Self::Vaet(components) => NormalizedIndexBoundary::Vaet(match components {
                IndexComponents::Empty => IndexComponents::Empty,
                IndexComponents::One(v) => IndexComponents::One(vaet_value(v)?),
                IndexComponents::Two(v, a) => IndexComponents::Two(vaet_value(v)?, *a),
                IndexComponents::Three(v, a, e) => {
                    IndexComponents::Three(vaet_value(v)?, *a, entity(*e)?)
                }
                IndexComponents::Four(v, a, e, t) => {
                    IndexComponents::Four(vaet_value(v)?, *a, entity(*e)?, t.stored_tx()?)
                }
            }),
        })
    }

    /// Boundary needed to derive a current forward seek from raw history.
    /// A T-qualified seek must observe the complete logical E/A/V group
    /// before deciding which filtered/current datoms survive, then apply the
    /// original four-component boundary to those survivors.
    pub(crate) fn current_group_start(&self) -> Self {
        match self {
            Self::Eavt(IndexComponents::Four(e, a, v, _)) => {
                Self::Eavt(IndexComponents::Three(*e, *a, v.clone()))
            }
            Self::Aevt(IndexComponents::Four(a, e, v, _)) => {
                Self::Aevt(IndexComponents::Three(*a, *e, v.clone()))
            }
            Self::Avet(IndexComponents::Four(a, v, e, _)) => {
                Self::Avet(IndexComponents::Three(*a, v.clone(), *e))
            }
            Self::Vaet(IndexComponents::Four(v, a, e, _)) => {
                Self::Vaet(IndexComponents::Three(v.clone(), *a, *e))
            }
            _ => self.clone(),
        }
    }
}

/// Cursor-ready form with the public T/Tx choice normalized to the canonical
/// transaction entity stored by [`Datom`].
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) enum NormalizedIndexBoundary {
    Eavt(IndexComponents<u64, u32, Value, u64>),
    Aevt(IndexComponents<u32, u64, Value, u64>),
    Avet(IndexComponents<u32, Value, u64, u64>),
    Vaet(IndexComponents<Value, u32, u64, u64>),
    /// Internal forward position after every datom equal to the supplied
    /// logical prefix. It changes neither public components nor stored keys.
    After(Box<Self>),
}

impl NormalizedIndexBoundary {
    pub(crate) fn order(&self) -> IndexOrder {
        match self {
            Self::Eavt(_) => IndexOrder::Eavt,
            Self::Aevt(_) => IndexOrder::Aevt,
            Self::Avet(_) => IndexOrder::Avet,
            Self::Vaet(_) => IndexOrder::Vaet,
            Self::After(boundary) => boundary.order(),
        }
    }

    pub(crate) fn after_prefix(self) -> Self {
        match self {
            Self::After(_) => self,
            _ => Self::After(Box::new(self)),
        }
    }

    pub(crate) fn is_after_prefix(&self) -> bool {
        matches!(self, Self::After(_))
    }

    pub(crate) fn unbiased(&self) -> &Self {
        match self {
            Self::After(boundary) => boundary,
            _ => self,
        }
    }

    /// Compare an actual datom to this virtual boundary in index order.
    /// Missing suffix components compare equal, which is the semantic key to
    /// placing forward seek before the lowest match and reverse seek after the
    /// highest match without manufacturing minimum/maximum Values. This is the
    /// direct Rust form of recovered `reverse-datum-spec` (`db.clj:2040-2063`),
    /// whose increments/MAX fields and `asserting=false` manufacture the same
    /// upper position for a concrete-datum tree API.
    pub(crate) fn compare_datom(&self, datom: &Datom) -> Ordering {
        fn tx(datom: &Datom, boundary_tx: u64) -> Ordering {
            // Transaction sorts descending in every native index.
            boundary_tx.cmp(&datom.tx)
        }

        match self {
            Self::After(boundary) => boundary.compare_datom(datom).then(Ordering::Less),
            Self::Eavt(components) => match components {
                IndexComponents::Empty => Ordering::Equal,
                IndexComponents::One(e) => datom.entity.cmp(e),
                IndexComponents::Two(e, a) => {
                    datom.entity.cmp(e).then_with(|| datom.attribute.cmp(a))
                }
                IndexComponents::Three(e, a, v) => datom
                    .entity
                    .cmp(e)
                    .then_with(|| datom.attribute.cmp(a))
                    .then_with(|| datom.value.index_cmp(v)),
                IndexComponents::Four(e, a, v, t) => datom
                    .entity
                    .cmp(e)
                    .then_with(|| datom.attribute.cmp(a))
                    .then_with(|| datom.value.index_cmp(v))
                    .then_with(|| tx(datom, *t)),
            },
            Self::Aevt(components) => match components {
                IndexComponents::Empty => Ordering::Equal,
                IndexComponents::One(a) => datom.attribute.cmp(a),
                IndexComponents::Two(a, e) => {
                    datom.attribute.cmp(a).then_with(|| datom.entity.cmp(e))
                }
                IndexComponents::Three(a, e, v) => datom
                    .attribute
                    .cmp(a)
                    .then_with(|| datom.entity.cmp(e))
                    .then_with(|| datom.value.index_cmp(v)),
                IndexComponents::Four(a, e, v, t) => datom
                    .attribute
                    .cmp(a)
                    .then_with(|| datom.entity.cmp(e))
                    .then_with(|| datom.value.index_cmp(v))
                    .then_with(|| tx(datom, *t)),
            },
            Self::Avet(components) => match components {
                IndexComponents::Empty => Ordering::Equal,
                IndexComponents::One(a) => datom.attribute.cmp(a),
                IndexComponents::Two(a, v) => datom
                    .attribute
                    .cmp(a)
                    .then_with(|| datom.value.index_cmp(v)),
                IndexComponents::Three(a, v, e) => datom
                    .attribute
                    .cmp(a)
                    .then_with(|| datom.value.index_cmp(v))
                    .then_with(|| datom.entity.cmp(e)),
                IndexComponents::Four(a, v, e, t) => datom
                    .attribute
                    .cmp(a)
                    .then_with(|| datom.value.index_cmp(v))
                    .then_with(|| datom.entity.cmp(e))
                    .then_with(|| tx(datom, *t)),
            },
            Self::Vaet(components) => match components {
                IndexComponents::Empty => Ordering::Equal,
                IndexComponents::One(v) => datom.value.index_cmp(v),
                IndexComponents::Two(v, a) => datom
                    .value
                    .index_cmp(v)
                    .then_with(|| datom.attribute.cmp(a)),
                IndexComponents::Three(v, a, e) => datom
                    .value
                    .index_cmp(v)
                    .then_with(|| datom.attribute.cmp(a))
                    .then_with(|| datom.entity.cmp(e)),
                IndexComponents::Four(v, a, e, t) => datom
                    .value
                    .index_cmp(v)
                    .then_with(|| datom.attribute.cmp(a))
                    .then_with(|| datom.entity.cmp(e))
                    .then_with(|| tx(datom, *t)),
            },
        }
    }

    /// Lower-bound comparison used while deriving a current value from raw
    /// recent history. A four-component boundary must begin at the start of
    /// its complete logical E/A/V group: only after selecting that group's
    /// endpoint winner is it safe to apply the T boundary. Starting within
    /// the history group could resurrect an older assertion hidden by a
    /// newer operation that sorts just before the requested T.
    pub(crate) fn compare_current_group_start(&self, datom: &Datom) -> Ordering {
        match self {
            Self::After(boundary)
                if matches!(
                    boundary.as_ref(),
                    Self::Eavt(IndexComponents::Four(..))
                        | Self::Aevt(IndexComponents::Four(..))
                        | Self::Avet(IndexComponents::Four(..))
                        | Self::Vaet(IndexComponents::Four(..))
                ) =>
            {
                // T-qualified current reads must still select the winner
                // from its entire group before applying an exclusive T.
                boundary.compare_current_group_start(datom)
            }
            Self::Eavt(IndexComponents::Four(e, a, v, _)) => datom
                .entity
                .cmp(e)
                .then_with(|| datom.attribute.cmp(a))
                .then_with(|| datom.value.index_cmp(v)),
            Self::Aevt(IndexComponents::Four(a, e, v, _)) => datom
                .attribute
                .cmp(a)
                .then_with(|| datom.entity.cmp(e))
                .then_with(|| datom.value.index_cmp(v)),
            Self::Avet(IndexComponents::Four(a, v, e, _)) => datom
                .attribute
                .cmp(a)
                .then_with(|| datom.value.index_cmp(v))
                .then_with(|| datom.entity.cmp(e)),
            Self::Vaet(IndexComponents::Four(v, a, e, _)) => datom
                .value
                .index_cmp(v)
                .then_with(|| datom.attribute.cmp(a))
                .then_with(|| datom.entity.cmp(e)),
            _ => self.compare_datom(datom),
        }
    }

    #[cfg(test)]
    pub(crate) fn matches(&self, datom: &Datom) -> bool {
        self.compare_datom(datom).is_eq()
    }

    /// Forward lower bound and reverse upper bound in an already sorted slice.
    /// This helper fixtures positioning; production Stage 2 cursors use the
    /// same comparator during tree descent instead of collecting a slice.
    #[cfg(test)]
    fn positions_in(&self, datoms: &[Datom]) -> (usize, usize) {
        let forward = datoms.partition_point(|datom| self.compare_datom(datom).is_lt());
        let reverse_exclusive = datoms.partition_point(|datom| !self.compare_datom(datom).is_gt());
        (forward, reverse_exclusive)
    }
}

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

impl PartialOrd for IndexPrefix {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for IndexPrefix {
    fn cmp(&self, other: &Self) -> Ordering {
        prefix_rank(self)
            .cmp(&prefix_rank(other))
            .then_with(|| match (self, other) {
                (
                    Self::Eavt {
                        entity: left_entity,
                        attribute: left_attribute,
                        value: left_value,
                    },
                    Self::Eavt {
                        entity: right_entity,
                        attribute: right_attribute,
                        value: right_value,
                    },
                ) => left_entity
                    .cmp(right_entity)
                    .then_with(|| left_attribute.cmp(right_attribute))
                    .then_with(|| compare_optional_value(left_value, right_value)),
                (
                    Self::Aevt {
                        attribute: left_attribute,
                        entity: left_entity,
                        value: left_value,
                    },
                    Self::Aevt {
                        attribute: right_attribute,
                        entity: right_entity,
                        value: right_value,
                    },
                ) => left_attribute
                    .cmp(right_attribute)
                    .then_with(|| left_entity.cmp(right_entity))
                    .then_with(|| compare_optional_value(left_value, right_value)),
                (
                    Self::Avet {
                        attribute: left_attribute,
                        value: left_value,
                        entity: left_entity,
                    },
                    Self::Avet {
                        attribute: right_attribute,
                        value: right_value,
                        entity: right_entity,
                    },
                ) => left_attribute
                    .cmp(right_attribute)
                    .then_with(|| compare_optional_value(left_value, right_value))
                    .then_with(|| left_entity.cmp(right_entity)),
                (
                    Self::Vaet {
                        value: left_value,
                        attribute: left_attribute,
                        entity: left_entity,
                    },
                    Self::Vaet {
                        value: right_value,
                        attribute: right_attribute,
                        entity: right_entity,
                    },
                ) => left_value
                    .index_cmp(right_value)
                    .then_with(|| left_attribute.cmp(right_attribute))
                    .then_with(|| left_entity.cmp(right_entity)),
                _ => Ordering::Equal,
            })
    }
}

fn prefix_rank(prefix: &IndexPrefix) -> u8 {
    match prefix {
        IndexPrefix::Eavt { .. } => 0,
        IndexPrefix::Aevt { .. } => 1,
        IndexPrefix::Avet { .. } => 2,
        IndexPrefix::Vaet { .. } => 3,
    }
}

fn compare_optional_value(left: &Option<Value>, right: &Option<Value>) -> Ordering {
    match (left, right) {
        (None, None) => Ordering::Equal,
        (None, Some(_)) => Ordering::Less,
        (Some(_), None) => Ordering::Greater,
        (Some(left), Some(right)) => left.index_cmp(right),
    }
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

impl TryFrom<&IndexPrefix> for IndexBoundary {
    type Error = SemanticError;

    fn try_from(prefix: &IndexPrefix) -> Result<Self, Self::Error> {
        prefix.validate()?;
        Ok(match prefix {
            IndexPrefix::Eavt {
                entity,
                attribute: None,
                value: None,
            } => Self::Eavt(IndexComponents::One(*entity)),
            IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: None,
            } => Self::Eavt(IndexComponents::Two(*entity, *attribute)),
            IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: Some(value),
            } => Self::Eavt(IndexComponents::Three(*entity, *attribute, value.clone())),
            IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            } => Self::Aevt(IndexComponents::One(*attribute)),
            IndexPrefix::Aevt {
                attribute,
                entity: Some(entity),
                value: None,
            } => Self::Aevt(IndexComponents::Two(*attribute, *entity)),
            IndexPrefix::Aevt {
                attribute,
                entity: Some(entity),
                value: Some(value),
            } => Self::Aevt(IndexComponents::Three(*attribute, *entity, value.clone())),
            IndexPrefix::Avet {
                attribute,
                value: None,
                entity: None,
            } => Self::Avet(IndexComponents::One(*attribute)),
            IndexPrefix::Avet {
                attribute,
                value: Some(value),
                entity: None,
            } => Self::Avet(IndexComponents::Two(*attribute, value.clone())),
            IndexPrefix::Avet {
                attribute,
                value: Some(value),
                entity: Some(entity),
            } => Self::Avet(IndexComponents::Three(*attribute, value.clone(), *entity)),
            IndexPrefix::Vaet {
                value,
                attribute: None,
                entity: None,
            } => Self::Vaet(IndexComponents::One(value.clone())),
            IndexPrefix::Vaet {
                value,
                attribute: Some(attribute),
                entity: None,
            } => Self::Vaet(IndexComponents::Two(value.clone(), *attribute)),
            IndexPrefix::Vaet {
                value,
                attribute: Some(attribute),
                entity: Some(entity),
            } => Self::Vaet(IndexComponents::Three(value.clone(), *attribute, *entity)),
            // `validate` rejects these gap shapes before this match. Keeping
            // the arm explicit prevents a future prefix variant from silently
            // acquiring different compatibility semantics.
            _ => unreachable!("validated IndexPrefix has no component gap"),
        })
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

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{MAX_EIDX, USER_PARTITION, make_eid};
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    fn decimal(value: &str) -> Value {
        Value::BigDec(BigDecimal::from_str(value).unwrap())
    }

    fn datom(entity: u64, attribute: u32, value: Value, t: u64, added: bool) -> Datom {
        Datom {
            entity,
            attribute,
            value,
            tx: t_to_tx(t).unwrap(),
            added,
        }
    }

    fn scalar_fixture() -> Vec<Datom> {
        vec![
            datom(9, 20, decimal("1"), 8, true),
            datom(10, 19, decimal("1"), 8, true),
            datom(10, 20, decimal("0"), 8, true),
            datom(10, 20, decimal("1"), 9, true),
            datom(10, 20, decimal("1.0"), 8, true),
            datom(10, 20, decimal("1.00"), 8, true),
            datom(10, 20, decimal("1.000"), 8, false),
            datom(10, 20, decimal("1"), 7, true),
            datom(10, 20, decimal("2"), 8, true),
            datom(10, 21, decimal("1"), 8, true),
            datom(11, 20, decimal("1"), 8, true),
        ]
    }

    fn ref_fixture() -> Vec<Datom> {
        vec![
            datom(9, 20, Value::Ref(30), 8, true),
            datom(10, 19, Value::Ref(30), 8, true),
            datom(10, 20, Value::Ref(29), 8, true),
            datom(10, 20, Value::Ref(30), 9, true),
            datom(10, 20, Value::Ref(30), 8, true),
            datom(10, 20, Value::Ref(30), 8, false),
            datom(10, 20, Value::Ref(30), 7, true),
            datom(10, 20, Value::Ref(31), 8, true),
            datom(10, 21, Value::Ref(30), 8, true),
            datom(11, 20, Value::Ref(30), 8, true),
        ]
    }

    fn logical_value_is(value: &Value, expected: &Value) -> bool {
        value.index_cmp(expected).is_eq()
    }

    fn assert_virtual_group(
        mut datoms: Vec<Datom>,
        boundary: IndexBoundary,
        expected: impl Fn(&Datom) -> bool,
    ) -> (Vec<Datom>, usize, usize) {
        let order = boundary.order();
        datoms.sort_by(|left, right| left.cmp_in(right, order));
        let normalized = boundary.normalized().unwrap();
        assert_eq!(normalized.order(), order);
        let (forward, reverse_exclusive) = normalized.positions_in(&datoms);
        let expected_indices = datoms
            .iter()
            .enumerate()
            .filter_map(|(index, datom)| expected(datom).then_some(index))
            .collect::<Vec<_>>();
        assert!(!expected_indices.is_empty());
        assert_eq!(forward, expected_indices[0]);
        assert_eq!(
            reverse_exclusive,
            expected_indices[expected_indices.len() - 1] + 1
        );
        assert!(
            datoms[..forward]
                .iter()
                .all(|datom| normalized.compare_datom(datom).is_lt())
        );
        assert!(
            datoms[forward..reverse_exclusive]
                .iter()
                .all(|datom| normalized.matches(datom))
        );
        assert!(
            datoms[reverse_exclusive..]
                .iter()
                .all(|datom| normalized.compare_datom(datom).is_gt())
        );
        (datoms, forward, reverse_exclusive)
    }

    #[test]
    fn every_index_positions_zero_through_four_components_on_opposite_sides() {
        let one = decimal("1");

        for (boundary, expected) in [
            (IndexBoundary::Eavt(IndexComponents::Empty), 0_usize),
            (IndexBoundary::Eavt(IndexComponents::One(10)), 1),
            (IndexBoundary::Eavt(IndexComponents::Two(10, 20)), 2),
            (
                IndexBoundary::Eavt(IndexComponents::Three(10, 20, one.clone())),
                3,
            ),
            (
                IndexBoundary::Eavt(IndexComponents::Four(
                    10,
                    20,
                    one.clone(),
                    IndexTransaction::T(8),
                )),
                4,
            ),
        ] {
            assert_eq!(boundary.len(), expected);
            assert_eq!(boundary.is_empty(), expected == 0);
            assert_virtual_group(scalar_fixture(), boundary, |datom| match expected {
                0 => true,
                1 => datom.entity == 10,
                2 => datom.entity == 10 && datom.attribute == 20,
                3 => {
                    datom.entity == 10
                        && datom.attribute == 20
                        && logical_value_is(&datom.value, &one)
                }
                4 => {
                    datom.entity == 10
                        && datom.attribute == 20
                        && logical_value_is(&datom.value, &one)
                        && datom.tx == t_to_tx(8).unwrap()
                }
                _ => unreachable!(),
            });
        }

        for (boundary, expected) in [
            (IndexBoundary::Aevt(IndexComponents::Empty), 0_usize),
            (IndexBoundary::Aevt(IndexComponents::One(20)), 1),
            (IndexBoundary::Aevt(IndexComponents::Two(20, 10)), 2),
            (
                IndexBoundary::Aevt(IndexComponents::Three(20, 10, one.clone())),
                3,
            ),
            (
                IndexBoundary::Aevt(IndexComponents::Four(
                    20,
                    10,
                    one.clone(),
                    IndexTransaction::Tx(t_to_tx(8).unwrap()),
                )),
                4,
            ),
        ] {
            assert_virtual_group(scalar_fixture(), boundary, |datom| match expected {
                0 => true,
                1 => datom.attribute == 20,
                2 => datom.attribute == 20 && datom.entity == 10,
                3 => {
                    datom.attribute == 20
                        && datom.entity == 10
                        && logical_value_is(&datom.value, &one)
                }
                4 => {
                    datom.attribute == 20
                        && datom.entity == 10
                        && logical_value_is(&datom.value, &one)
                        && datom.tx == t_to_tx(8).unwrap()
                }
                _ => unreachable!(),
            });
        }

        for (boundary, expected) in [
            (IndexBoundary::Avet(IndexComponents::Empty), 0_usize),
            (IndexBoundary::Avet(IndexComponents::One(20)), 1),
            (
                IndexBoundary::Avet(IndexComponents::Two(20, one.clone())),
                2,
            ),
            (
                IndexBoundary::Avet(IndexComponents::Three(20, one.clone(), 10)),
                3,
            ),
            (
                IndexBoundary::Avet(IndexComponents::Four(
                    20,
                    one.clone(),
                    10,
                    IndexTransaction::T(8),
                )),
                4,
            ),
        ] {
            assert_eq!(boundary.avet_attribute(), (expected != 0).then_some(20));
            assert_virtual_group(scalar_fixture(), boundary, |datom| match expected {
                0 => true,
                1 => datom.attribute == 20,
                2 => datom.attribute == 20 && logical_value_is(&datom.value, &one),
                3 => {
                    datom.attribute == 20
                        && logical_value_is(&datom.value, &one)
                        && datom.entity == 10
                }
                4 => {
                    datom.attribute == 20
                        && logical_value_is(&datom.value, &one)
                        && datom.entity == 10
                        && datom.tx == t_to_tx(8).unwrap()
                }
                _ => unreachable!(),
            });
        }

        let reference = Value::Ref(30);
        for (boundary, expected) in [
            (IndexBoundary::Vaet(IndexComponents::Empty), 0_usize),
            (
                IndexBoundary::Vaet(IndexComponents::One(reference.clone())),
                1,
            ),
            (
                IndexBoundary::Vaet(IndexComponents::Two(reference.clone(), 20)),
                2,
            ),
            (
                IndexBoundary::Vaet(IndexComponents::Three(reference.clone(), 20, 10)),
                3,
            ),
            (
                IndexBoundary::Vaet(IndexComponents::Four(
                    reference.clone(),
                    20,
                    10,
                    IndexTransaction::T(8),
                )),
                4,
            ),
        ] {
            assert_virtual_group(ref_fixture(), boundary, |datom| match expected {
                0 => true,
                1 => logical_value_is(&datom.value, &reference),
                2 => logical_value_is(&datom.value, &reference) && datom.attribute == 20,
                3 => {
                    logical_value_is(&datom.value, &reference)
                        && datom.attribute == 20
                        && datom.entity == 10
                }
                4 => {
                    logical_value_is(&datom.value, &reference)
                        && datom.attribute == 20
                        && datom.entity == 10
                        && datom.tx == t_to_tx(8).unwrap()
                }
                _ => unreachable!(),
            });
        }
    }

    #[test]
    fn full_t_boundary_contains_every_assertion_retraction_and_stored_value_tie() {
        let boundary = IndexBoundary::Eavt(IndexComponents::Four(
            10,
            20,
            decimal("1"),
            IndexTransaction::T(8),
        ));
        let (datoms, forward, reverse_exclusive) =
            assert_virtual_group(scalar_fixture(), boundary, |datom| {
                datom.entity == 10
                    && datom.attribute == 20
                    && logical_value_is(&datom.value, &decimal("1"))
                    && datom.tx == t_to_tx(8).unwrap()
            });
        let group = &datoms[forward..reverse_exclusive];
        assert_eq!(group.len(), 3);
        assert!(group.iter().any(|datom| datom.added));
        assert!(group.iter().any(|datom| !datom.added));
        assert!(
            group
                .windows(2)
                .all(|pair| pair[0].cmp_in(&pair[1], IndexOrder::Eavt).is_lt())
        );
    }

    #[test]
    fn missing_virtual_value_puts_forward_and_reverse_at_the_same_gap() {
        let mut datoms = vec![
            datom(10, 20, decimal("1"), 8, true),
            datom(10, 20, decimal("2"), 8, true),
        ];
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Avet));
        let boundary = IndexBoundary::Avet(IndexComponents::Two(20, decimal("1.5")))
            .normalized()
            .unwrap();
        let (forward, reverse_exclusive) = boundary.positions_in(&datoms);
        assert_eq!(forward, reverse_exclusive);
        assert_eq!(forward, 1);
        assert!(boundary.compare_datom(&datoms[forward - 1]).is_lt());
        assert!(boundary.compare_datom(&datoms[forward]).is_gt());
    }

    #[test]
    fn transaction_and_reference_components_are_checked_before_positioning() {
        assert_eq!(
            IndexTransaction::T(8).stored_tx().unwrap(),
            t_to_tx(8).unwrap()
        );
        assert_eq!(
            IndexTransaction::Tx(t_to_tx(9).unwrap())
                .stored_tx()
                .unwrap(),
            t_to_tx(9).unwrap()
        );
        assert_eq!(
            IndexTransaction::T(MAX_EIDX + 1)
                .stored_tx()
                .unwrap_err()
                .code,
            "identity/eidx-out-of-range"
        );
        assert_eq!(
            IndexTransaction::Tx(make_eid(USER_PARTITION, 8).unwrap())
                .stored_tx()
                .unwrap_err()
                .code,
            "identity/not-a-transaction-id"
        );
        assert_eq!(
            IndexBoundary::Vaet(IndexComponents::One(Value::Long(8)))
                .validate()
                .unwrap_err()
                .code,
            "index/vaet-value-not-ref"
        );
    }

    #[test]
    fn legacy_prefix_converts_without_changing_logical_value_matching() {
        let prefix = IndexPrefix::Avet {
            attribute: 20,
            value: Some(decimal("1.00")),
            entity: Some(10),
        };
        let boundary = IndexBoundary::try_from(&prefix).unwrap();
        assert_eq!(boundary.len(), 3);
        let normalized = boundary.normalized().unwrap();
        assert!(normalized.matches(&datom(10, 20, decimal("1.0"), 8, true)));

        let gap = IndexPrefix::Eavt {
            entity: 10,
            attribute: None,
            value: Some(decimal("1")),
        };
        assert_eq!(
            IndexBoundary::try_from(&gap).unwrap_err().code,
            "index/non-contiguous-prefix"
        );
    }
}
