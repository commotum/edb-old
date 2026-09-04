//! Incremental transaction assessment over one exact immutable database value.
//!
//! The eager [`crate::Database`] transition remains the semantic oracle.  This
//! module is the production-shaped counterpart: it obtains only left-prefix
//! ranges from the db-before value and keeps the proposed successor as a small
//! logical delta.  It deliberately contains no `materialize` fallback.

use crate::identity::{validate_frontier, validate_supported_eid};
use crate::idents::IdentIndex;
use crate::vocabulary::{supported_system_attributes, supported_system_idents};
use crate::{
    Cardinality, DB_ALTER_ATTRIBUTE, DB_ATTR_PREDS, DB_CARDINALITY, DB_ENSURE, DB_ENTITY_ATTRS,
    DB_ENTITY_PREDS, DB_EXCISE, DB_EXCISE_BEFORE, DB_EXCISE_BEFORE_T, DB_IDENT, DB_INDEX,
    DB_INSTALL_ATTRIBUTE, DB_IS_COMPONENT, DB_NO_HISTORY, DB_PART_DB, DB_TUPLE_ATTRS,
    DB_TUPLE_DISCONTINUED, DB_TUPLE_TYPE, DB_TUPLE_TYPES, DB_TX_INSTANT, DB_UNIQUE,
    DB_VALUE_TYPE, DatabaseValue, Datom, EntityRef, ErrorCategory, IndexOrder, IndexPrefix, Schema,
    SemanticError, TX_PARTITION, TupleSpec, TxOp, TxValue, USER_PARTITION, Unique, Value,
    ValueType, eid_to_eidx, eid_to_part, make_eid, t_to_tx,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct AssessmentReadWork {
    pub(crate) prefixes: u64,
    pub(crate) datoms: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct EnsureRequirement {
    pub(crate) entity: u64,
    pub(crate) spec: u64,
    pub(crate) required: Vec<u32>,
    pub(crate) predicates: Vec<String>,
}

#[derive(Clone, Debug)]
pub(crate) struct TieredAssessment {
    pub(crate) basis_t: u64,
    pub(crate) eidx_frontier: u64,
    pub(crate) tx_data: Vec<Datom>,
    pub(crate) tempids: BTreeMap<String, u64>,
    pub(crate) ensures: Vec<EnsureRequirement>,
    pub(crate) successor_schema: Arc<Schema>,
    pub(crate) read_work: AssessmentReadWork,
}

#[derive(Clone, Debug)]
struct LogicalDatom {
    entity: u64,
    attribute: u32,
    value: Value,
    added: bool,
}

struct Reader<'a> {
    base: &'a DatabaseValue,
    work: AssessmentReadWork,
}

impl<'a> Reader<'a> {
    fn new(base: &'a DatabaseValue) -> Self {
        Self {
            base,
            work: AssessmentReadWork::default(),
        }
    }

    fn prefix(&mut self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        self.work.prefixes = self.work.prefixes.saturating_add(1);
        let datoms = self
            .base
            .current_prefix_cursor(prefix)?
            .collect::<Result<Vec<_>, _>>()?;
        self.work.datoms = self
            .work
            .datoms
            .saturating_add(u64::try_from(datoms.len()).unwrap_or(u64::MAX));
        Ok(datoms)
    }

    fn values(&mut self, entity: u64, attribute: u32) -> Result<Vec<Value>, SemanticError> {
        Ok(self
            .prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: None,
            })?
            .into_iter()
            .map(|datom| datom.value)
            .collect())
    }

    fn contains(
        &mut self,
        entity: u64,
        attribute: u32,
        value: &Value,
    ) -> Result<bool, SemanticError> {
        Ok(self
            .prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: Some(value.clone()),
            })?
            .into_iter()
            .any(|datom| datom.value.stored_eq(value)))
    }

    fn lookup(&mut self, attribute: u32, value: &Value) -> Result<Option<u64>, SemanticError> {
        let descriptor = self.base.schema().attribute(attribute)?;
        if descriptor.unique.is_none() {
            return Err(SemanticError::incorrect(
                "transaction/lookup-non-unique",
                format!(
                    "attribute {} is not unique",
                    descriptor.ident.qualified_name()
                ),
            ));
        }
        self.base.schema().validate_value(descriptor, value)?;
        Ok(self
            .prefix(&IndexPrefix::Avet {
                attribute,
                value: Some(value.clone()),
                entity: None,
            })?
            .first()
            .map(|datom| datom.entity))
    }
}

/// Assess a complete transaction without retaining or constructing a complete
/// current/history database.
pub(crate) fn assess_tiered(
    base: &DatabaseValue,
    ops: &[TxOp],
    tx_instant: i64,
) -> Result<TieredAssessment, SemanticError> {
    if base
        .last_tx_instant()?
        .is_some_and(|prior| tx_instant < prior)
    {
        return Err(SemanticError::incorrect(
            "transaction/non-monotonic-instant",
            format!("transaction instant {tx_instant} precedes db-before"),
        ));
    }
    let basis_t = base.basis_t().checked_add(1).ok_or_else(|| {
        SemanticError::incorrect(
            "transaction/basis-overflow",
            "database basis cannot advance beyond u64",
        )
    })?;
    let tx = t_to_tx(basis_t)?;
    let initial_allocation_start = base
        .eidx_frontier()
        .max(basis_t.checked_add(1).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/basis-overflow",
                "transaction time cannot advance the issued frontier",
            )
        })?);
    validate_frontier(initial_allocation_start)?;

    let mut ordered = ops.to_vec();
    ordered.sort_by(crate::transaction::compare_tx_op);
    validate_tx_instant_forms(&ordered, tx_instant)?;
    let mut reader = Reader::new(base);
    let (mut logical, allocation_start) = prepare_schema_information(
        &mut reader,
        &ordered,
        tx,
        initial_allocation_start,
    )?;
    let (tempids, eidx_frontier) = resolve_tempids(&mut reader, &ordered, allocation_start)?;
    let mut ensures = Vec::new();
    let mut touched = BTreeSet::new();
    for op in &ordered {
        expand_op(
            &mut reader,
            op,
            tx,
            &tempids,
            &mut logical,
            &mut ensures,
            &mut touched,
        )?;
    }
    ensures.sort_by_key(|ensure| (ensure.entity, ensure.spec));
    ensures.dedup_by_key(|ensure| (ensure.entity, ensure.spec));

    dedupe(&mut logical);
    validate_same_transaction(base.schema(), &logical)?;
    add_cardinality_one_retractions(&mut reader, &mut logical)?;
    dedupe(&mut logical);
    validate_same_transaction(base.schema(), &logical)?;
    synthesize_schema_hooks(&mut reader, &mut logical)?;
    dedupe(&mut logical);
    validate_same_transaction(base.schema(), &logical)?;
    derive_composites(&mut reader, &mut logical, &touched)?;
    logical.push(LogicalDatom {
        entity: tx,
        attribute: DB_TX_INSTANT as u32,
        value: Value::Instant(tx_instant),
        added: true,
    });
    dedupe(&mut logical);
    validate_same_transaction(base.schema(), &logical)?;

    let successor_schema = Arc::new(derive_successor_schema(&mut reader, &logical, tx)?);
    validate_schema_transition(&mut reader, &successor_schema, &logical)?;
    validate_delta_successor(&mut reader, &successor_schema, &logical)?;
    validate_ensure_attributes(&mut reader, &logical, &ensures)?;
    let mut tx_data = material_changes(&mut reader, &logical, tx)?;
    tx_data.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));

    Ok(TieredAssessment {
        basis_t,
        eidx_frontier,
        tx_data,
        tempids,
        ensures,
        successor_schema,
        read_work: reader.work,
    })
}

fn resolve_tempids(
    reader: &mut Reader<'_>,
    ops: &[TxOp],
    allocation_start: u64,
) -> Result<(BTreeMap<String, u64>, u64), SemanticError> {
    let mut names = BTreeSet::new();
    for op in ops {
        collect_tempids_op(op, &mut names);
    }
    let names = names.into_iter().collect::<Vec<_>>();
    let positions = names
        .iter()
        .enumerate()
        .map(|(index, name)| (name.clone(), index))
        .collect::<BTreeMap<_, _>>();
    let mut union = UnionFind::new(names.len());
    let mut identities = Vec::new();
    for op in ops {
        let TxOp::Add {
            entity: EntityRef::Temp(name),
            attribute,
            value: TxValue::Scalar(value),
        } = op
        else {
            continue;
        };
        let descriptor = reader.base.schema().attribute(*attribute)?;
        let Some(unique) = descriptor.unique else {
            continue;
        };
        reader.base.schema().validate_value(descriptor, value)?;
        if value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in upsert or uniqueness",
            ));
        }
        identities.push((positions[name], *attribute, value.clone(), unique));
    }
    for left in 0..identities.len() {
        for right in left + 1..identities.len() {
            let (left_temp, left_attr, left_value, left_unique) = &identities[left];
            let (right_temp, right_attr, right_value, right_unique) = &identities[right];
            if *left_unique == Unique::Identity
                && *right_unique == Unique::Identity
                && left_attr == right_attr
                && left_value.index_cmp(right_value).is_eq()
            {
                union.join(*left_temp, *right_temp);
            }
        }
    }

    let mut existing_by_root = BTreeMap::new();
    for (temp, attribute, value, unique) in &identities {
        if let Some(existing) = reader.lookup(*attribute, value)? {
            if *unique == Unique::Value {
                return Err(SemanticError::conflict(
                    "transaction/unique-value-conflict",
                    format!("unique value is already held by entity {existing}"),
                ));
            }
            let root = union.root(*temp);
            if let Some(previous) = existing_by_root.insert(root, existing)
                && previous != existing
            {
                return Err(SemanticError::conflict(
                    "transaction/upsert-conflict",
                    format!("one tempid resolves to both {previous} and {existing}"),
                ));
            }
        }
    }

    let mut next = allocation_start;
    let mut allocated_by_root = BTreeMap::new();
    let mut result = BTreeMap::new();
    for (index, name) in names.iter().enumerate() {
        let root = union.root(index);
        let entity = if let Some(existing) = existing_by_root.get(&root) {
            *existing
        } else if let Some(allocated) = allocated_by_root.get(&root) {
            *allocated
        } else {
            let allocated = make_eid(USER_PARTITION, next)?;
            next = next.checked_add(1).ok_or_else(|| {
                SemanticError::incorrect(
                    "transaction/entity-id-overflow",
                    "tempid allocation exhausted the entity-index space",
                )
            })?;
            allocated_by_root.insert(root, allocated);
            allocated
        };
        result.insert(name.clone(), entity);
    }
    validate_frontier(next)?;
    Ok((result, next))
}

#[allow(clippy::too_many_arguments)]
fn expand_op(
    reader: &mut Reader<'_>,
    op: &TxOp,
    tx: u64,
    tempids: &BTreeMap<String, u64>,
    logical: &mut Vec<LogicalDatom>,
    ensures: &mut Vec<EnsureRequirement>,
    touched: &mut BTreeSet<(u64, u32)>,
) -> Result<(), SemanticError> {
    match op {
        TxOp::Add {
            entity,
            attribute,
            value,
        } => {
            let entity = resolve_entity(reader, entity, tx, tempids)?;
            let value = resolve_value(reader, *attribute, value, tx, tempids)?;
            validate_entity_predicate_name(*attribute, &value)?;
            if u64::from(*attribute) == DB_ENSURE {
                let Value::Ref(spec) = value else {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-ensure",
                        ":db/ensure must name an entity spec",
                    ));
                };
                ensures.push(resolve_entity_spec(reader, entity, spec)?);
            } else if !is_derived_composite(reader.base.schema(), *attribute)? {
                touched.insert((entity, *attribute));
                logical.push(LogicalDatom {
                    entity,
                    attribute: *attribute,
                    value,
                    added: true,
                });
            }
        }
        TxOp::Retract {
            entity,
            attribute,
            value,
        } => {
            let entity = resolve_entity(reader, entity, tx, tempids)?;
            reader.base.schema().attribute(*attribute)?;
            if u64::from(*attribute) == DB_ENSURE {
                return Err(SemanticError::incorrect(
                    "transaction/virtual-ensure",
                    ":db/ensure is virtual and cannot be retracted",
                ));
            }
            if is_derived_composite(reader.base.schema(), *attribute)? {
                if let Some(value) = value {
                    let _ = resolve_value(reader, *attribute, value, tx, tempids)?;
                }
            } else {
                touched.insert((entity, *attribute));
                if let Some(value) = value {
                    logical.push(LogicalDatom {
                        entity,
                        attribute: *attribute,
                        value: resolve_value(reader, *attribute, value, tx, tempids)?,
                        added: false,
                    });
                } else {
                    for datom in reader.prefix(&IndexPrefix::Eavt {
                        entity,
                        attribute: Some(*attribute),
                        value: None,
                    })? {
                        logical.push(LogicalDatom {
                            entity,
                            attribute: *attribute,
                            value: datom.value,
                            added: false,
                        });
                    }
                }
            }
        }
        TxOp::Cas {
            entity,
            attribute,
            old,
            new,
        } => {
            let entity = resolve_entity(reader, entity, tx, tempids)?;
            let descriptor = reader.base.schema().attribute(*attribute)?;
            if u64::from(*attribute) == DB_ENSURE {
                return Err(SemanticError::incorrect(
                    "transaction/virtual-ensure",
                    ":db/ensure is virtual and cannot be compared and swapped",
                ));
            }
            if descriptor.cardinality != Cardinality::One {
                return Err(SemanticError::incorrect(
                    "transaction/cas-requires-cardinality-one",
                    "compare-and-swap requires a cardinality-one attribute",
                ));
            }
            let observed = reader.values(entity, *attribute)?;
            let expected = old
                .as_ref()
                .map(|value| resolve_value(reader, *attribute, value, tx, tempids))
                .transpose()?;
            let matches = match (expected.as_ref(), observed.as_slice()) {
                (None, []) => true,
                (Some(expected), [observed]) => observed.stored_eq(expected),
                _ => false,
            };
            if !matches {
                return Err(SemanticError::new(
                    ErrorCategory::Conflict,
                    "transaction/cas-failed",
                    "compare-and-swap did not match db-before",
                ));
            }
            let value = resolve_value(reader, *attribute, new, tx, tempids)?;
            validate_entity_predicate_name(*attribute, &value)?;
            if !is_derived_composite(reader.base.schema(), *attribute)? {
                touched.insert((entity, *attribute));
                logical.push(LogicalDatom {
                    entity,
                    attribute: *attribute,
                    value,
                    added: true,
                });
            }
        }
        TxOp::RetractEntity(entity) => {
            let entity = resolve_entity(reader, entity, tx, tempids)?;
            if eid_to_part(entity)? == TX_PARTITION {
                return Err(SemanticError::incorrect(
                    "transaction/reset-tx-instant",
                    "transaction entities cannot be retracted",
                ));
            }
            expand_retract_entity(reader, entity, logical, touched, &mut BTreeSet::new())?;
        }
        TxOp::Ensure { entity, spec } => {
            let entity = resolve_entity(reader, entity, tx, tempids)?;
            let spec = resolve_entity(reader, spec, tx, tempids)?;
            ensures.push(resolve_entity_spec(reader, entity, spec)?);
        }
        TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => unreachable!(),
    }
    Ok(())
}

fn expand_retract_entity(
    reader: &mut Reader<'_>,
    entity: u64,
    logical: &mut Vec<LogicalDatom>,
    touched: &mut BTreeSet<(u64, u32)>,
    visited: &mut BTreeSet<u64>,
) -> Result<(), SemanticError> {
    if !visited.insert(entity) {
        return Ok(());
    }
    for fact in reader.prefix(&IndexPrefix::Eavt {
        entity,
        attribute: None,
        value: None,
    })? {
        let attribute = reader.base.schema().attribute(fact.attribute)?;
        if attribute.component
            && let Value::Ref(child) = fact.value
        {
            expand_retract_entity(reader, child, logical, touched, visited)?;
        }
        touched.insert((entity, fact.attribute));
        logical.push(LogicalDatom {
            entity,
            attribute: fact.attribute,
            value: fact.value,
            added: false,
        });
    }
    for fact in reader.prefix(&IndexPrefix::Vaet {
        value: Value::Ref(entity),
        attribute: None,
        entity: None,
    })? {
        touched.insert((fact.entity, fact.attribute));
        logical.push(LogicalDatom {
            entity: fact.entity,
            attribute: fact.attribute,
            value: fact.value,
            added: false,
        });
    }
    Ok(())
}

fn resolve_entity(
    reader: &mut Reader<'_>,
    entity: &EntityRef,
    tx: u64,
    tempids: &BTreeMap<String, u64>,
) -> Result<u64, SemanticError> {
    match entity {
        EntityRef::Id(entity) => {
            validate_explicit_entity_id(reader.base, *entity)?;
            Ok(*entity)
        }
        EntityRef::Ident(ident) => reader.base.entid(ident).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-ident",
                format!("unknown ident {}", ident.qualified_name()),
            )
        }),
        EntityRef::Temp(name) => tempids.get(name).copied().ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-tempid",
                format!("unknown tempid {name}"),
            )
        }),
        EntityRef::Lookup { attribute, value } => {
            reader.lookup(*attribute, value)?.ok_or_else(|| {
                SemanticError::incorrect(
                    "transaction/lookup-not-found",
                    "lookup ref did not resolve in db-before",
                )
            })
        }
        EntityRef::Tx => Ok(tx),
    }
}

fn validate_explicit_entity_id(base: &DatabaseValue, entity: u64) -> Result<(), SemanticError> {
    validate_supported_eid(entity)?;
    let eidx = eid_to_eidx(entity)?;
    if eidx >= base.eidx_frontier() {
        return Err(SemanticError::incorrect(
            "transaction/invalid-entity-id",
            format!(
                "entity id {entity} has unissued index {eidx}; issued indexes are below {}",
                base.eidx_frontier()
            ),
        ));
    }
    Ok(())
}

fn resolve_value(
    reader: &mut Reader<'_>,
    attribute: u32,
    value: &TxValue,
    tx: u64,
    tempids: &BTreeMap<String, u64>,
) -> Result<Value, SemanticError> {
    let descriptor = reader.base.schema().attribute(attribute)?;
    let value = match (descriptor.value_type, value) {
        (ValueType::Ref, TxValue::Entity(entity)) => {
            Value::Ref(resolve_entity(reader, entity, tx, tempids)?)
        }
        (_, TxValue::Scalar(value)) => {
            validate_explicit_value_refs(reader.base, value)?;
            value.clone()
        }
        _ => {
            return Err(SemanticError::incorrect(
                "transaction/value-type",
                format!(
                    "attribute {} requires {:?}",
                    descriptor.ident.qualified_name(),
                    descriptor.value_type
                ),
            ));
        }
    };
    reader.base.schema().validate_value(descriptor, &value)?;
    Ok(value)
}

fn validate_explicit_value_refs(base: &DatabaseValue, value: &Value) -> Result<(), SemanticError> {
    match value {
        Value::Ref(entity) => validate_explicit_entity_id(base, *entity),
        Value::Tuple(slots) => {
            for value in slots.iter().flatten() {
                validate_explicit_value_refs(base, value)?;
            }
            Ok(())
        }
        _ => Ok(()),
    }
}

fn resolve_entity_spec(
    reader: &mut Reader<'_>,
    entity: u64,
    spec: u64,
) -> Result<EnsureRequirement, SemanticError> {
    let mut required = Vec::new();
    for value in reader.values(spec, DB_ENTITY_ATTRS as u32)? {
        let Value::Keyword(ident) = value else {
            return Err(SemanticError::incorrect(
                "transaction/invalid-entity-spec",
                ":db.entity/attrs values must be attribute keywords",
            ));
        };
        let attribute = reader.base.entid(&ident).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/unknown-spec-attribute",
                format!(
                    "entity spec names unknown attribute {}",
                    ident.qualified_name()
                ),
            )
        })?;
        required.push(crate::schema_eid_to_attr_id(attribute)?);
    }
    required.sort_unstable();
    required.dedup();
    let mut predicates = Vec::new();
    for value in reader.values(spec, DB_ENTITY_PREDS as u32)? {
        let Value::Symbol(symbol) = value else {
            return Err(SemanticError::incorrect(
                "transaction/invalid-entity-spec",
                ":db.entity/preds values must be symbols",
            ));
        };
        if symbol
            .namespace
            .as_deref()
            .is_none_or(|namespace| namespace.is_empty() || namespace.contains('/'))
            || symbol.name.is_empty()
            || symbol.name.contains('/')
        {
            return Err(SemanticError::incorrect(
                "transaction/unqualified-entity-predicate",
                ":db.entity/preds values must be fully qualified symbols",
            ));
        }
        predicates.push(symbol.qualified_name());
    }
    predicates.sort();
    predicates.dedup();
    Ok(EnsureRequirement {
        entity,
        spec,
        required,
        predicates,
    })
}

fn add_cardinality_one_retractions(
    reader: &mut Reader<'_>,
    logical: &mut Vec<LogicalDatom>,
) -> Result<(), SemanticError> {
    let additions = logical
        .iter()
        .filter(|datom| datom.added)
        .cloned()
        .collect::<Vec<_>>();
    for addition in additions {
        if reader
            .base
            .schema()
            .attribute(addition.attribute)?
            .cardinality
            != Cardinality::One
        {
            continue;
        }
        for existing in reader.prefix(&IndexPrefix::Eavt {
            entity: addition.entity,
            attribute: Some(addition.attribute),
            value: None,
        })? {
            if !existing.value.stored_eq(&addition.value) {
                logical.push(LogicalDatom {
                    entity: existing.entity,
                    attribute: existing.attribute,
                    value: existing.value,
                    added: false,
                });
            }
        }
    }
    Ok(())
}

fn derive_composites(
    reader: &mut Reader<'_>,
    logical: &mut Vec<LogicalDatom>,
    touched: &BTreeSet<(u64, u32)>,
) -> Result<(), SemanticError> {
    let composites = reader
        .base
        .schema()
        .attributes()
        .filter(|attribute| {
            matches!(attribute.tuple, Some(TupleSpec::Composite(_)))
                && !attribute.tuple_discontinued
        })
        .cloned()
        .collect::<Vec<_>>();
    for composite in composites {
        let TupleSpec::Composite(constituents) = composite.tuple.as_ref().unwrap() else {
            unreachable!()
        };
        let entities = touched
            .iter()
            .filter(|(_, attribute)| constituents.contains(attribute))
            .map(|(entity, _)| *entity)
            .collect::<BTreeSet<_>>();
        for entity in entities {
            let old = reader.values(entity, composite.id)?.into_iter().next();
            let mut slots = Vec::with_capacity(constituents.len());
            for constituent in constituents {
                slots.push(
                    successor_values(reader, logical, entity, *constituent)?
                        .into_iter()
                        .next(),
                );
            }
            let new = (!slots.iter().all(Option::is_none)).then_some(Value::Tuple(slots));
            if let Some(old) = &old
                && new.as_ref().is_none_or(|new| !old.stored_eq(new))
            {
                logical.push(LogicalDatom {
                    entity,
                    attribute: composite.id,
                    value: old.clone(),
                    added: false,
                });
            }
            if let Some(new) = new
                && old.as_ref().is_none_or(|old| !old.stored_eq(&new))
            {
                reader.base.schema().validate_value(&composite, &new)?;
                logical.push(LogicalDatom {
                    entity,
                    attribute: composite.id,
                    value: new,
                    added: true,
                });
            }
        }
    }
    Ok(())
}

fn validate_delta_successor(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
) -> Result<(), SemanticError> {
    let mut touched_ea = BTreeSet::new();
    let mut touched_av = Vec::<(u32, Value)>::new();
    let mut excision_entities = BTreeSet::new();
    for datom in logical {
        touched_ea.insert((datom.entity, datom.attribute));
        let descriptor = reader.base.schema().attribute(datom.attribute)?;
        if descriptor.unique.is_some()
            && !touched_av.iter().any(|(attribute, value)| {
                *attribute == datom.attribute && value.stored_eq(&datom.value)
            })
        {
            touched_av.push((datom.attribute, datom.value.clone()));
        }
        if matches!(
            u64::from(datom.attribute),
            DB_EXCISE | DB_EXCISE_BEFORE_T | DB_EXCISE_BEFORE
        ) {
            excision_entities.insert(datom.entity);
        }
    }
    for (entity, attribute) in touched_ea {
        let descriptor = reader.base.schema().attribute(attribute)?;
        let values = successor_values(reader, logical, entity, attribute)?;
        if descriptor.cardinality == Cardinality::One && values.len() > 1 {
            return Err(SemanticError::conflict(
                "transaction/cardinality-one-conflict",
                "resulting database has multiple cardinality-one values",
            ));
        }
    }
    for (attribute, value) in touched_av {
        if value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in uniqueness",
            ));
        }
        let owners = successor_owners(reader, logical, attribute, &value)?;
        if owners.len() > 1 {
            return Err(SemanticError::conflict(
                "transaction/unique-conflict",
                "resulting database has multiple holders of a unique value",
            ));
        }
    }
    for entity in excision_entities {
        let before_t = successor_values(reader, logical, entity, DB_EXCISE_BEFORE_T as u32)?;
        let before = successor_values(reader, logical, entity, DB_EXCISE_BEFORE as u32)?;
        if !before_t.is_empty() && !before.is_empty() {
            return Err(SemanticError::incorrect(
                "transaction/excision-cutoff-conflict",
                "an excision request may use at most one cutoff",
            ));
        }
    }
    Ok(())
}

fn validate_ensure_attributes(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    ensures: &[EnsureRequirement],
) -> Result<(), SemanticError> {
    for ensure in ensures {
        let mut missing = Vec::new();
        for attribute in &ensure.required {
            if successor_values(reader, logical, ensure.entity, *attribute)?.is_empty() {
                missing.push(*attribute);
            }
        }
        if !missing.is_empty() {
            return Err(SemanticError::incorrect(
                "transaction/entity-spec",
                format!(
                    "entity {} is missing attributes {missing:?} of spec {}",
                    ensure.entity, ensure.spec
                ),
            ));
        }
    }
    Ok(())
}

fn successor_values(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    entity: u64,
    attribute: u32,
) -> Result<Vec<Value>, SemanticError> {
    let mut values = reader.values(entity, attribute)?;
    for datom in logical
        .iter()
        .filter(|datom| datom.entity == entity && datom.attribute == attribute)
    {
        if datom.added {
            if !values.iter().any(|value| value.stored_eq(&datom.value)) {
                values.push(datom.value.clone());
            }
        } else {
            values.retain(|value| !value.stored_eq(&datom.value));
        }
    }
    values.sort_by(Value::stored_cmp);
    Ok(values)
}

fn successor_owners(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    attribute: u32,
    value: &Value,
) -> Result<Vec<u64>, SemanticError> {
    let mut owners = reader
        .prefix(&IndexPrefix::Avet {
            attribute,
            value: Some(value.clone()),
            entity: None,
        })?
        .into_iter()
        .map(|datom| datom.entity)
        .collect::<Vec<_>>();
    for datom in logical
        .iter()
        .filter(|datom| datom.attribute == attribute && datom.value.stored_eq(value))
    {
        if datom.added {
            if !owners.contains(&datom.entity) {
                owners.push(datom.entity);
            }
        } else {
            owners.retain(|entity| *entity != datom.entity);
        }
    }
    owners.sort_unstable();
    owners.dedup();
    Ok(owners)
}

fn material_changes(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    tx: u64,
) -> Result<Vec<Datom>, SemanticError> {
    let mut result = Vec::new();
    for datom in logical {
        let existed = reader.contains(datom.entity, datom.attribute, &datom.value)?;
        if existed != datom.added {
            result.push(Datom {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                tx,
                added: datom.added,
            });
        }
    }
    Ok(result)
}

fn validate_tx_instant_forms(ops: &[TxOp], selected: i64) -> Result<(), SemanticError> {
    let mut explicit = None;
    for op in ops {
        match op {
            TxOp::Add {
                entity,
                attribute,
                value,
            } if u64::from(*attribute) == DB_TX_INSTANT => {
                if !matches!(entity, EntityRef::Tx) {
                    return Err(SemanticError::incorrect(
                        "transaction/reset-tx-instant",
                        ":db/txInstant may be asserted only on the current transaction",
                    ));
                }
                let TxValue::Scalar(Value::Instant(instant)) = value else {
                    return Err(SemanticError::incorrect(
                        "transaction/invalid-tx-instant",
                        ":db/txInstant must be a scalar instant",
                    ));
                };
                if explicit.replace(*instant).is_some() {
                    return Err(SemanticError::incorrect(
                        "transaction/multiple-tx-instants",
                        ":db/txInstant may be specified only once",
                    ));
                }
            }
            TxOp::Retract { attribute, .. } | TxOp::Cas { attribute, .. }
                if u64::from(*attribute) == DB_TX_INSTANT =>
            {
                return Err(SemanticError::incorrect(
                    "transaction/reset-tx-instant",
                    ":db/txInstant cannot be retracted or changed",
                ));
            }
            _ => {}
        }
    }
    if explicit.is_some_and(|instant| instant != selected) {
        return Err(SemanticError::incorrect(
            "transaction/tx-instant-mismatch",
            "explicit :db/txInstant differs from the transactor-selected instant",
        ));
    }
    Ok(())
}

fn validate_entity_predicate_name(attribute: u32, value: &Value) -> Result<(), SemanticError> {
    if u64::from(attribute) != DB_ENTITY_PREDS {
        return Ok(());
    }
    let Value::Symbol(symbol) = value else {
        return Ok(());
    };
    if symbol
        .namespace
        .as_deref()
        .is_none_or(|namespace| namespace.is_empty() || namespace.contains('/'))
        || symbol.name.is_empty()
        || symbol.name.contains('/')
    {
        return Err(SemanticError::incorrect(
            "transaction/unqualified-entity-predicate",
            ":db.entity/preds values must be fully qualified symbols",
        ));
    }
    Ok(())
}

fn is_derived_composite(schema: &Schema, attribute: u32) -> Result<bool, SemanticError> {
    let attribute = schema.attribute(attribute)?;
    Ok(matches!(attribute.tuple, Some(TupleSpec::Composite(_))) && !attribute.tuple_discontinued)
}

fn dedupe(datoms: &mut Vec<LogicalDatom>) {
    let mut result = Vec::new();
    for datom in datoms.drain(..) {
        if !result.iter().any(|existing| same_logical(existing, &datom)) {
            result.push(datom);
        }
    }
    result.sort_by(compare_logical);
    *datoms = result;
}

fn validate_same_transaction(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<(), SemanticError> {
    for (index, left) in datoms.iter().enumerate() {
        let attribute = schema.attribute(left.attribute)?;
        for right in &datoms[index + 1..] {
            if left.entity == right.entity
                && left.attribute == right.attribute
                && left.value.stored_eq(&right.value)
                && left.added != right.added
            {
                return Err(SemanticError::conflict(
                    "transaction/datoms-conflict",
                    "addition and retraction of the same E/A/V conflict",
                ));
            }
            if left.entity == right.entity
                && left.attribute == right.attribute
                && left.added
                && right.added
                && attribute.cardinality == Cardinality::One
                && left.value.index_cmp(&right.value).is_ne()
            {
                return Err(SemanticError::conflict(
                    "transaction/cardinality-one-conflict",
                    "two values for one cardinality-one E/A conflict",
                ));
            }
            if left.attribute == right.attribute
                && left.added
                && right.added
                && attribute.unique.is_some()
                && left.value.index_cmp(&right.value).is_eq()
                && left.entity != right.entity
            {
                return Err(SemanticError::conflict(
                    "transaction/unique-conflict",
                    "two entities assert the same unique A/V",
                ));
            }
        }
    }
    Ok(())
}

fn same_logical(left: &LogicalDatom, right: &LogicalDatom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.added == right.added
        && left.value.stored_eq(&right.value)
}

fn compare_logical(left: &LogicalDatom, right: &LogicalDatom) -> Ordering {
    left.entity
        .cmp(&right.entity)
        .then(left.attribute.cmp(&right.attribute))
        .then_with(|| left.value.stored_cmp(&right.value))
        .then_with(|| right.added.cmp(&left.added))
}

fn collect_tempids_op(op: &TxOp, output: &mut BTreeSet<String>) {
    match op {
        TxOp::Add { entity, value, .. } => {
            collect_tempids_entity(entity, output);
            collect_tempids_value(value, output);
        }
        TxOp::Retract { entity, value, .. } => {
            collect_tempids_entity(entity, output);
            if let Some(value) = value {
                collect_tempids_value(value, output);
            }
        }
        TxOp::Cas {
            entity, old, new, ..
        } => {
            collect_tempids_entity(entity, output);
            if let Some(old) = old {
                collect_tempids_value(old, output);
            }
            collect_tempids_value(new, output);
        }
        TxOp::RetractEntity(entity) => collect_tempids_entity(entity, output),
        TxOp::Ensure { entity, spec } => {
            collect_tempids_entity(entity, output);
            collect_tempids_entity(spec, output);
        }
        TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
    }
}

fn collect_tempids_value(value: &TxValue, output: &mut BTreeSet<String>) {
    if let TxValue::Entity(entity) = value {
        collect_tempids_entity(entity, output);
    }
}

fn collect_tempids_entity(entity: &EntityRef, output: &mut BTreeSet<String>) {
    if let EntityRef::Temp(name) = entity {
        output.insert(name.clone());
    }
}

#[derive(Debug)]
struct UnionFind {
    parent: Vec<usize>,
}

impl UnionFind {
    fn new(size: usize) -> Self {
        Self {
            parent: (0..size).collect(),
        }
    }

    fn root(&self, mut index: usize) -> usize {
        while self.parent[index] != index {
            index = self.parent[index];
        }
        index
    }

    fn join(&mut self, left: usize, right: usize) {
        let left = self.root(left);
        let right = self.root(right);
        if left != right {
            let (first, second) = if left < right {
                (left, right)
            } else {
                (right, left)
            };
            self.parent[second] = first;
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Attribute, Database, Keyword};
    use std::sync::Arc;

    const NAME: u32 = 1_000;
    const COUNT: u32 = 1_001;
    const TAG: u32 = 1_002;
    const PARENT: u32 = 1_003;

    fn schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    NAME,
                    Keyword::new("item", "name"),
                    ValueType::String,
                    Cardinality::One,
                )
                .unique(Unique::Identity),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                COUNT,
                Keyword::new("item", "count"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                TAG,
                Keyword::new("item", "tag"),
                ValueType::String,
                Cardinality::Many,
            ))
            .unwrap();
        schema
            .install(
                Attribute::new(
                    PARENT,
                    Keyword::new("item", "parent"),
                    ValueType::Ref,
                    Cardinality::One,
                )
                .component(),
            )
            .unwrap();
        schema
    }

    fn add(name: &str, count: i64) -> Vec<TxOp> {
        vec![
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: NAME,
                value: TxValue::Scalar(Value::String(name.into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: COUNT,
                value: TxValue::Scalar(Value::Long(count)),
            },
        ]
    }

    #[test]
    fn ordinary_assessment_matches_eager_upsert_replacement_and_retraction() {
        let initial = Database::new(schema()).unwrap();
        let seeded = initial.with(&add("one", 1), 10).unwrap().db_after;
        let entity = seeded
            .lookup(NAME, &Value::String("one".into()))
            .unwrap()
            .unwrap();
        let ops = vec![
            TxOp::Add {
                entity: EntityRef::Temp("same".into()),
                attribute: NAME,
                value: TxValue::Scalar(Value::String("one".into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("same".into()),
                attribute: COUNT,
                value: TxValue::Scalar(Value::Long(2)),
            },
            TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: TAG,
                value: TxValue::Scalar(Value::String("kept".into())),
            },
            TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: TAG,
                value: Some(TxValue::Scalar(Value::String("absent".into()))),
            },
        ];
        let expected = seeded.with(&ops, 11).unwrap();
        let value = DatabaseValue::eager(Arc::new(seeded));
        let assessed = assess_tiered_ordinary(&value, &ops, 11).unwrap();
        assert_eq!(assessed.basis_t, expected.db_after.basis_t());
        assert_eq!(assessed.eidx_frontier, expected.db_after.eidx_frontier());
        assert_eq!(assessed.tempids, expected.tempids);
        assert_eq!(assessed.tx_data, expected.tx_data);
        assert!(assessed.read_work.prefixes > 0);
        assert!(assessed.read_work.datoms > 0);
    }

    #[test]
    fn retract_entity_component_and_incoming_refs_match_eager() {
        let initial = Database::new(schema()).unwrap();
        let seeded = initial
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("parent".into()),
                        attribute: NAME,
                        value: TxValue::Scalar(Value::String("parent".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("child".into()),
                        attribute: NAME,
                        value: TxValue::Scalar(Value::String("child".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("parent".into()),
                        attribute: PARENT,
                        value: TxValue::Entity(EntityRef::Temp("child".into())),
                    },
                ],
                10,
            )
            .unwrap()
            .db_after;
        let parent = seeded
            .lookup(NAME, &Value::String("parent".into()))
            .unwrap()
            .unwrap();
        let ops = vec![TxOp::RetractEntity(EntityRef::Id(parent))];
        let expected = seeded.with(&ops, 11).unwrap();
        let value = DatabaseValue::eager(Arc::new(seeded));
        let assessed = assess_tiered_ordinary(&value, &ops, 11).unwrap();
        assert_eq!(assessed.tx_data, expected.tx_data);
    }
}
