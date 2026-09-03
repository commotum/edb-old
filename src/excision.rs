//! Pure, source-faithful excision predicate planning.
//!
//! An excision request is ordinary immutable database information. This module
//! only decides which historical datoms a background copy-on-write rewrite
//! removes; it owns no SQL, publication, or synchronization state.

use crate::database::FrozenExcisionRequest;
use crate::{
    DB_PARTITION, Database, Datom, Digest, DurableTransaction, ErrorCategory, IndexOrder,
    SemanticError, Value, View, eid_to_part, sha256, tx_to_t,
};
use std::collections::{BTreeSet, VecDeque};

/// Exact values of recovered 1.0.7705 `datomic.db/BOOT-IDS`.
///
/// `datomic.excise/keeper?` preserves a datom when its entity is in partition
/// zero or its attribute is one of these ids (`excise.clj:139-147`). This is
/// intentionally not a numeric range: native ids 54--72 are useful design
/// evidence but were not members of the recovered keeper set.
pub(crate) const RECOVERED_BOOT_IDS: &[u64] = &[
    0, 1, 2, 3, 4, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
    35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53,
];

pub(crate) fn is_excision_keeper(datom: &Datom) -> bool {
    eid_to_part(datom.entity).is_ok_and(|partition| partition == DB_PARTITION)
        || RECOVERED_BOOT_IDS
            .binary_search(&u64::from(datom.attribute))
            .is_ok()
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum ExcisionTargetKind {
    Entity,
    Attribute,
}

#[derive(Clone, Debug)]
struct ExcisionPredicate {
    request: FrozenExcisionRequest,
    kind: ExcisionTargetKind,
    before_t: u64,
    /// Target plus recursively owned component entities, as-of request_t.
    extent: BTreeSet<u64>,
    reference_attributes: BTreeSet<u32>,
    protected_entity_target: bool,
}

/// Canonical OR-union of every pending predicate in one background batch.
/// Overlapping requests remove a datom once, matching recovered
/// `create-xpreds`/`create-e->xpreds`/`create-a->xpreds`
/// (`excise.clj:331-428`).
#[derive(Clone, Debug, Default)]
pub(crate) struct ExcisionPlan {
    predicates: Vec<ExcisionPredicate>,
}

/// Fully frozen material predicate persisted beside a COW generation build.
///
/// Raw A=15 facts remain the semantic/audit authority. This projection also
/// records every current-schema choice made while planning (target kind,
/// effective cutoff, component extent, and reference classification), so a
/// crash cannot resume an old build under a newly interpreted predicate.
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct PlannedExcisionPredicate {
    pub(crate) request: FrozenExcisionRequest,
    pub(crate) kind: ExcisionTargetKind,
    pub(crate) before_t: u64,
    pub(crate) extent: BTreeSet<u64>,
    pub(crate) reference_attributes: BTreeSet<u32>,
    pub(crate) protected_entity_target: bool,
    pub(crate) hash: Digest,
}

impl ExcisionPlan {
    pub(crate) fn pending_after(
        database: &Database,
        processed_through_t: u64,
    ) -> Result<Self, SemanticError> {
        Self::from_requests(
            database,
            database.pending_excision_requests_after(processed_through_t)?,
        )
    }

    pub(crate) fn from_requests(
        database: &Database,
        mut requests: Vec<FrozenExcisionRequest>,
    ) -> Result<Self, SemanticError> {
        requests.sort_by_key(|request| (request.request_t, request.request_entity));
        let mut canonical = Vec::<FrozenExcisionRequest>::new();
        for request in requests {
            if let Some(previous) = canonical.last()
                && (previous.request_t, previous.request_entity)
                    == (request.request_t, request.request_entity)
            {
                if previous != &request {
                    return Err(SemanticError::new(
                        ErrorCategory::Fault,
                        "excision/request-identity-conflict",
                        "one excision assertion identity reconstructed to different predicates",
                    ));
                }
                continue;
            }
            canonical.push(request);
        }

        let predicates = canonical
            .into_iter()
            .map(|request| ExcisionPredicate::new(database, request))
            .collect::<Result<Vec<_>, _>>()?;
        Ok(Self { predicates })
    }

    pub(crate) fn is_empty(&self) -> bool {
        self.predicates.is_empty()
    }

    pub(crate) fn requests(&self) -> impl Iterator<Item = &FrozenExcisionRequest> {
        self.predicates.iter().map(|predicate| &predicate.request)
    }

    pub(crate) fn frozen_predicates(&self) -> Vec<PlannedExcisionPredicate> {
        self.predicates
            .iter()
            .map(ExcisionPredicate::frozen)
            .collect()
    }

    /// One order-independent commitment to the exact predicates applied by a
    /// physical generation. Individual hashes are sorted before aggregation,
    /// matching declarative transaction/excision set semantics.
    pub(crate) fn request_set_hash(&self) -> Digest {
        let mut hashes = self
            .predicates
            .iter()
            .map(ExcisionPredicate::canonical_hash)
            .collect::<Vec<_>>();
        hashes.sort_unstable();
        let mut bytes = Vec::with_capacity(41 + hashes.len() * 32);
        bytes.extend_from_slice(b"atomic/excision-request-set/v1\0");
        bytes.extend_from_slice(&(hashes.len() as u64).to_be_bytes());
        for hash in hashes {
            bytes.extend_from_slice(&hash);
        }
        sha256(&bytes)
    }

    pub(crate) fn target_kind(
        &self,
        request_entity: u64,
        request_t: u64,
    ) -> Option<ExcisionTargetKind> {
        self.predicates
            .iter()
            .find(|predicate| {
                predicate.request.request_entity == request_entity
                    && predicate.request.request_t == request_t
            })
            .map(|predicate| predicate.kind)
    }

    pub(crate) fn removes(&self, datom: &Datom) -> bool {
        self.predicates
            .iter()
            .any(|predicate| predicate.removes(datom))
    }

    pub(crate) fn removed_count<'a>(&self, datoms: impl IntoIterator<Item = &'a Datom>) -> u64 {
        datoms
            .into_iter()
            .filter(|datom| self.removes(datom))
            .count() as u64
    }

    pub(crate) fn filter_transaction(
        &self,
        transaction: &DurableTransaction,
    ) -> DurableTransaction {
        let mut filtered = transaction.clone();
        filtered.tx_data.retain(|datom| !self.removes(datom));
        filtered
    }
}

impl ExcisionPredicate {
    fn new(database: &Database, request: FrozenExcisionRequest) -> Result<Self, SemanticError> {
        // Recovered type selection: attrs force entity form; otherwise an
        // installed partition-zero attribute target selects attribute form.
        let kind = if request.attributes.is_empty()
            && eid_to_part(request.target).is_ok_and(|partition| partition == DB_PARTITION)
            && u32::try_from(request.target)
                .ok()
                .is_some_and(|attribute| database.schema().attribute(attribute).is_ok())
        {
            ExcisionTargetKind::Attribute
        } else {
            ExcisionTargetKind::Entity
        };
        let protected_entity_target = kind == ExcisionTargetKind::Entity
            && eid_to_part(request.target).is_ok_and(|partition| partition == DB_PARTITION);
        let before_t = database.excision_before_t(&request);
        let reference_attributes = database
            .schema()
            .attributes()
            .filter(|attribute| attribute.value_type == crate::ValueType::Ref)
            .map(|attribute| attribute.id)
            .collect();
        let extent = if kind == ExcisionTargetKind::Entity && !protected_entity_target {
            component_extent_as_of(database, &request)?
        } else {
            BTreeSet::from([request.target])
        };
        Ok(Self {
            request,
            kind,
            before_t,
            extent,
            reference_attributes,
            protected_entity_target,
        })
    }

    fn frozen(&self) -> PlannedExcisionPredicate {
        PlannedExcisionPredicate {
            request: self.request.clone(),
            kind: self.kind,
            before_t: self.before_t,
            extent: self.extent.clone(),
            reference_attributes: self.reference_attributes.clone(),
            protected_entity_target: self.protected_entity_target,
            hash: self.canonical_hash(),
        }
    }

    fn canonical_hash(&self) -> Digest {
        let mut bytes = Vec::new();
        bytes.extend_from_slice(b"atomic/excision-predicate/v1\0");
        bytes.extend_from_slice(&self.request.request_entity.to_be_bytes());
        bytes.extend_from_slice(&self.request.request_t.to_be_bytes());
        bytes.extend_from_slice(&self.request.target.to_be_bytes());
        match self.request.cutoff {
            None => bytes.push(0),
            Some(crate::database::ExcisionCutoff::BeforeT(t)) => {
                bytes.push(1);
                bytes.extend_from_slice(&t.to_be_bytes());
            }
            Some(crate::database::ExcisionCutoff::BeforeInstant(instant)) => {
                bytes.push(2);
                bytes.extend_from_slice(&instant.to_be_bytes());
            }
        }
        bytes.push(match self.kind {
            ExcisionTargetKind::Entity => 0,
            ExcisionTargetKind::Attribute => 1,
        });
        bytes.extend_from_slice(&self.before_t.to_be_bytes());
        bytes.push(u8::from(self.protected_entity_target));
        bytes.extend_from_slice(&(self.request.attributes.len() as u64).to_be_bytes());
        for attribute in &self.request.attributes {
            bytes.extend_from_slice(&attribute.to_be_bytes());
        }
        bytes.extend_from_slice(&(self.extent.len() as u64).to_be_bytes());
        for entity in &self.extent {
            bytes.extend_from_slice(&entity.to_be_bytes());
        }
        bytes.extend_from_slice(&(self.reference_attributes.len() as u64).to_be_bytes());
        for attribute in &self.reference_attributes {
            bytes.extend_from_slice(&attribute.to_be_bytes());
        }
        sha256(&bytes)
    }

    fn removes(&self, datom: &Datom) -> bool {
        if is_excision_keeper(datom) || tx_to_t(datom.tx).map_or(true, |t| t >= self.before_t) {
            return false;
        }
        match self.kind {
            ExcisionTargetKind::Attribute => u64::from(datom.attribute) == self.request.target,
            ExcisionTargetKind::Entity if self.protected_entity_target => false,
            ExcisionTargetKind::Entity => {
                let reference = self.reference_attributes.contains(&datom.attribute)
                    && matches!(datom.value, Value::Ref(_));
                let references = |entity| {
                    reference && matches!(datom.value, Value::Ref(value) if value == entity)
                };
                let selected_target_attribute = self.request.attributes.is_empty()
                    || self
                        .request
                        .attributes
                        .contains(&u64::from(datom.attribute));
                let target_fact = selected_target_attribute
                    && (datom.entity == self.request.target || references(self.request.target));
                let component_fact =
                    self.extent.contains(&datom.entity) && datom.entity != self.request.target;
                let component_reference = reference
                    && matches!(datom.value, Value::Ref(value) if value != self.request.target && self.extent.contains(&value));
                target_fact || component_fact || component_reference
            }
        }
    }
}

/// Recovered `component-es-set` restricts only the first hop by requested
/// attrs, then follows every component edge recursively (`excise.clj:39-90`).
/// Its data input is history as-of the A=15 assertion. Attribute classification
/// comes from the current database value, matching the recovered calls through
/// `component-attr?` and `ref?`: later data cannot expand an old request, but a
/// later supported component-schema alteration can change its extent.
fn component_extent_as_of(
    database: &Database,
    request: &FrozenExcisionRequest,
) -> Result<BTreeSet<u64>, SemanticError> {
    let as_of = database.datoms(View::AsOf(request.request_t), IndexOrder::Eavt);
    // Db.asOf windows data, but recovered `component-attr?` and `ref?` call
    // the database's current attribute projection. Value type is immutable;
    // component ownership may have changed since the request and therefore
    // deliberately uses the current schema (`excise.clj:39-90,175-201`).
    let component_attributes: BTreeSet<_> = database
        .schema()
        .attributes()
        .filter(|attribute| attribute.component)
        .map(|attribute| attribute.id)
        .collect();
    let reference_attributes: BTreeSet<_> = database
        .schema()
        .attributes()
        .filter(|attribute| attribute.value_type == crate::ValueType::Ref)
        .map(|attribute| attribute.id)
        .collect();

    let mut extent = BTreeSet::from([request.target]);
    let mut queue = VecDeque::from([(request.target, true)]);
    while let Some((entity, first_hop)) = queue.pop_front() {
        for datom in as_of.iter().filter(|datom| datom.entity == entity) {
            if !component_attributes.contains(&datom.attribute)
                || !reference_attributes.contains(&datom.attribute)
                || (first_hop
                    && !request.attributes.is_empty()
                    && !request.attributes.contains(&u64::from(datom.attribute)))
            {
                continue;
            }
            let Value::Ref(component) = datom.value else {
                continue;
            };
            if extent.insert(component) {
                queue.push_back((component, false));
            }
        }
    }
    Ok(extent)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, DB_DOC, DB_EXCISE, DB_EXCISE_ATTRS, DB_TX_INSTANT, EntityRef,
        Keyword, Schema, TxOp, TxValue, USER_PARTITION, ValueType, make_eid, t_to_tx,
    };

    const COMPONENT: u32 = 1_000;
    const LINK: u32 = 1_001;
    const SECRET: u32 = 1_002;

    fn schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    COMPONENT,
                    Keyword::new("test", "component"),
                    ValueType::Ref,
                    Cardinality::One,
                )
                .component(),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                LINK,
                Keyword::new("test", "link"),
                ValueType::Ref,
                Cardinality::Many,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                SECRET,
                Keyword::new("test", "secret"),
                ValueType::String,
                Cardinality::Many,
            ))
            .unwrap();
        schema
    }

    #[test]
    fn keeper_uses_exact_boot_ids_and_entity_partition() {
        let tx = t_to_tx(1).unwrap();
        let user = make_eid(USER_PARTITION, 42).unwrap();
        for attribute in RECOVERED_BOOT_IDS {
            assert!(is_excision_keeper(&Datom {
                entity: user,
                attribute: *attribute as u32,
                value: Value::Long(1),
                tx,
                added: true,
            }));
        }
        assert!(!is_excision_keeper(&Datom {
            entity: user,
            attribute: DB_DOC as u32,
            value: Value::String("application metadata".into()),
            tx,
            added: true,
        }));
        assert!(is_excision_keeper(&Datom {
            entity: 42,
            attribute: SECRET,
            value: Value::String("schema entity".into()),
            tx,
            added: true,
        }));
    }

    #[test]
    fn selected_first_hop_then_recursive_components_match_recovered_extent() {
        let database = Database::new(schema()).unwrap();
        let data = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("root".into()),
                        attribute: COMPONENT,
                        value: TxValue::Entity(EntityRef::Temp("child".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("root".into()),
                        attribute: LINK,
                        value: TxValue::Entity(EntityRef::Temp("unowned".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("root".into()),
                        attribute: SECRET,
                        value: Value::String("root stays".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("child".into()),
                        attribute: COMPONENT,
                        value: TxValue::Entity(EntityRef::Temp("grandchild".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("child".into()),
                        attribute: SECRET,
                        value: Value::String("child goes".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("grandchild".into()),
                        attribute: COMPONENT,
                        value: TxValue::Entity(EntityRef::Temp("root".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("grandchild".into()),
                        attribute: SECRET,
                        value: Value::String("grandchild goes".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("unowned".into()),
                        attribute: LINK,
                        value: TxValue::Entity(EntityRef::Temp("child".into())),
                    },
                ],
                100,
            )
            .unwrap();
        let root = data.tempids["root"];
        let child = data.tempids["child"];
        let grandchild = data.tempids["grandchild"];
        let unowned = data.tempids["unowned"];
        let request = data
            .db_after
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(root)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_ATTRS as u32,
                        value: TxValue::Entity(EntityRef::Id(u64::from(COMPONENT))),
                    },
                ],
                200,
            )
            .unwrap();
        let later = request
            .db_after
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Id(child),
                        attribute: SECRET,
                        value: Value::String("future child stays".into()).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(root),
                        attribute: COMPONENT,
                        value: TxValue::Entity(EntityRef::Temp("later-child".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("later-child".into()),
                        attribute: SECRET,
                        value: Value::String("future extent stays".into()).into(),
                    },
                ],
                300,
            )
            .unwrap()
            .db_after;
        let plan = ExcisionPlan::pending_after(&later, 0).unwrap();
        assert_eq!(plan.requests().count(), 1);
        assert_eq!(
            plan.target_kind(request.tempids["request"], 3),
            Some(ExcisionTargetKind::Entity)
        );

        let history = later.datoms(View::History, IndexOrder::Eavt);
        let find = |entity, attribute, text: &str| {
            history.iter().find(|datom| {
                datom.entity == entity
                    && datom.attribute == attribute
                    && datom.value == Value::String(text.into())
                    && datom.added
            })
        };
        assert!(!plan.removes(find(root, SECRET, "root stays").unwrap()));
        assert!(plan.removes(find(child, SECRET, "child goes").unwrap()));
        assert!(plan.removes(find(grandchild, SECRET, "grandchild goes").unwrap()));
        assert!(!plan.removes(find(child, SECRET, "future child stays").unwrap()));
        let later_child = later
            .values(root, COMPONENT)
            .into_iter()
            .filter_map(|value| match value {
                Value::Ref(entity) if *entity != child => Some(*entity),
                _ => None,
            })
            .next()
            .unwrap();
        assert!(!plan.removes(find(later_child, SECRET, "future extent stays").unwrap()));

        let root_component = history.iter().find(|datom| {
            datom.entity == root
                && datom.attribute == COMPONENT
                && datom.value == Value::Ref(child)
                && datom.added
        });
        assert!(plan.removes(root_component.unwrap()));
        let inbound_child = history.iter().find(|datom| {
            datom.entity == unowned
                && datom.attribute == LINK
                && datom.value == Value::Ref(child)
                && datom.added
        });
        assert!(plan.removes(inbound_child.unwrap()));
        assert!(
            history
                .iter()
                .filter(|datom| {
                    datom.entity == request.tempids["request"]
                        && datom.attribute == DB_EXCISE as u32
                })
                .all(|datom| !plan.removes(datom))
        );
    }

    #[test]
    fn component_extent_windows_edges_at_request_but_uses_current_schema() {
        let database = Database::new(schema()).unwrap();
        let data = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("root".into()),
                        attribute: LINK,
                        value: TxValue::Entity(EntityRef::Temp("child".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("child".into()),
                        attribute: SECRET,
                        value: Value::String("owned after schema change".into()).into(),
                    },
                ],
                100,
            )
            .unwrap();
        let root = data.tempids["root"];
        let child = data.tempids["child"];
        let request = data
            .db_after
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(root)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_ATTRS as u32,
                        value: TxValue::Entity(EntityRef::Id(u64::from(LINK))),
                    },
                ],
                200,
            )
            .unwrap();

        let current_link = request
            .db_after
            .schema()
            .attribute(LINK)
            .unwrap()
            .clone()
            .component();
        let after_schema_change = request
            .db_after
            .with(&[TxOp::AlterAttribute(current_link)], 300)
            .unwrap()
            .db_after;
        let plan = ExcisionPlan::pending_after(&after_schema_change, 0).unwrap();
        let child_secret = after_schema_change
            .datoms(View::History, IndexOrder::Eavt)
            .into_iter()
            .find(|datom| {
                datom.entity == child
                    && datom.attribute == SECRET
                    && datom.value == Value::String("owned after schema change".into())
                    && datom.added
            })
            .unwrap();

        assert!(plan.removes(&child_secret));
    }

    #[test]
    fn protected_entity_request_is_recorded_but_removes_nothing() {
        let database = Database::bootstrap().unwrap();
        let request = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(crate::DB_PART_DB)),
                }],
                100,
            )
            .unwrap()
            .db_after;
        let plan = ExcisionPlan::pending_after(&request, 0).unwrap();
        assert!(!plan.is_empty());
        assert_eq!(
            plan.removed_count(request.datoms(View::History, IndexOrder::Eavt).iter()),
            0
        );
    }

    #[test]
    fn overlapping_predicates_form_one_canonical_union() {
        let database = Database::new(schema()).unwrap();
        let data = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("entity".into()),
                    attribute: SECRET,
                    value: Value::String("remove once".into()).into(),
                }],
                100,
            )
            .unwrap();
        let entity = data.tempids["entity"];
        let requests = data
            .db_after
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("one".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(entity)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("two".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(u64::from(SECRET))),
                    },
                ],
                200,
            )
            .unwrap()
            .db_after;
        let plan = ExcisionPlan::pending_after(&requests, 0).unwrap();
        assert_eq!(plan.requests().count(), 2);
        let candidate = requests
            .datoms(View::History, IndexOrder::Eavt)
            .into_iter()
            .find(|datom| datom.entity == entity && datom.attribute == SECRET)
            .unwrap();
        assert!(plan.removes(&candidate));
        assert_eq!(plan.removed_count([&candidate]), 1);
    }

    #[test]
    fn filtered_envelope_keeps_transaction_identity_fields_unchanged() {
        let database = Database::new(schema()).unwrap();
        let data = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("entity".into()),
                    attribute: SECRET,
                    value: Value::String("remove".into()).into(),
                }],
                100,
            )
            .unwrap();
        let entity = data.tempids["entity"];
        let request = data
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(entity)),
                }],
                200,
            )
            .unwrap()
            .db_after;
        let plan = ExcisionPlan::pending_after(&request, 0).unwrap();
        let transaction = DurableTransaction {
            database_id: "test".into(),
            basis_t: data.db_after.basis_t(),
            previous_hash: [7; 32],
            eidx_frontier: data.db_after.eidx_frontier(),
            tempids: data.tempids,
            tx_data: data.tx_data,
        };
        let filtered = plan.filter_transaction(&transaction);
        assert_eq!(filtered.database_id, transaction.database_id);
        assert_eq!(filtered.basis_t, transaction.basis_t);
        assert_eq!(filtered.previous_hash, transaction.previous_hash);
        assert_eq!(filtered.eidx_frontier, transaction.eidx_frontier);
        assert_eq!(filtered.tempids, transaction.tempids);
        assert!(filtered.tx_data.len() < transaction.tx_data.len());
        assert!(
            filtered
                .tx_data
                .iter()
                .any(|datom| { datom.attribute == DB_TX_INSTANT as u32 && datom.added })
        );
    }
}
