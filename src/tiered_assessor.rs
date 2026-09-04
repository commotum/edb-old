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
    DB_TUPLE_DISCONTINUED, DB_TUPLE_TYPE, DB_TUPLE_TYPES, DB_TX_INSTANT, DB_UNIQUE, DB_VALUE_TYPE,
    DatabaseValue, Datom, EntityRef, ErrorCategory, IndexOrder, IndexPrefix, Schema, SemanticError,
    TX_PARTITION, TupleSpec, TxFunctions, TxOp, TxValue, USER_PARTITION, Unique, Value, ValueType,
    eid_to_eidx, eid_to_part, make_eid, t_to_tx,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct AssessmentReadWork {
    pub(crate) prefixes: u64,
    pub(crate) datoms: u64,
    pub(crate) retained_bytes: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct AssessmentLimits {
    pub(crate) max_read_datoms: u64,
    pub(crate) max_read_bytes: u64,
}

impl AssessmentLimits {
    fn unbounded() -> Self {
        Self {
            max_read_datoms: u64::MAX,
            max_read_bytes: u64::MAX,
        }
    }
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
    pub(crate) db_before: DatabaseValue,
    pub(crate) db_after: DatabaseValue,
    pub(crate) basis_t: u64,
    pub(crate) eidx_frontier: u64,
    pub(crate) tx_data: Vec<Datom>,
    pub(crate) tempids: BTreeMap<String, u64>,
    pub(crate) ensures: Vec<EnsureRequirement>,
    pub(crate) successor_schema: Arc<Schema>,
    pub(crate) read_work: AssessmentReadWork,
}

impl TieredAssessment {
    /// Resolve only predicates this assessed transaction can execute.  As in
    /// the eager oracle, missing required attributes short-circuit before
    /// persisted predicate bindings are looked up.
    pub(crate) fn predicate_requirements(
        &self,
    ) -> Result<BTreeMap<String, crate::database::PredicateRole>, SemanticError> {
        self.validate_ensure_attributes()?;
        let mut required = BTreeMap::new();
        for datom in self.tx_data.iter().filter(|datom| datom.added) {
            let attribute = self.db_before.schema().attribute(datom.attribute)?;
            for predicate in &attribute.predicates {
                insert_predicate_role(
                    &mut required,
                    predicate,
                    crate::database::PredicateRole::Attribute,
                )?;
            }
        }
        for ensure in &self.ensures {
            for predicate in &ensure.predicates {
                insert_predicate_role(
                    &mut required,
                    predicate,
                    crate::database::PredicateRole::Entity,
                )?;
            }
        }
        Ok(required)
    }

    /// Run the delayed add-data and ensure hooks against the exact immutable
    /// db-before/db-after pair. Persisted entity predicates use the lazy
    /// `DatabaseValue`; eager Rust callbacks remain an oracle-only adapter.
    pub(crate) fn validate_exact(
        &self,
        functions: Option<&TxFunctions>,
    ) -> Result<(), SemanticError> {
        for datom in self.tx_data.iter().filter(|datom| datom.added) {
            let attribute = self.db_before.schema().attribute(datom.attribute)?;
            for predicate in &attribute.predicates {
                let result = functions
                    .ok_or_else(|| {
                        SemanticError::incorrect(
                            "transaction/missing-predicate-context",
                            format!(
                                "attribute {} requires predicate {predicate}",
                                attribute.ident.qualified_name()
                            ),
                        )
                    })?
                    .validate_attribute_predicate(predicate, &datom.value)?;
                if !crate::is_exact_true(&result) {
                    return Err(SemanticError::incorrect(
                        "transaction/attribute-predicate",
                        format!(
                            "entity {} attribute {} value {:?} failed predicate {predicate} with result {result:?}",
                            datom.entity,
                            attribute.ident.qualified_name(),
                            datom.value,
                        ),
                    )
                    .detail("entity", datom.entity.to_string())
                    .detail("attribute", attribute.ident.qualified_name())
                    .detail("value", format!("{:?}", datom.value))
                    .detail("predicate", predicate.clone())
                    .detail("pred_return", format!("{result:?}")));
                }
            }
        }
        self.validate_ensure_attributes()?;
        for ensure in &self.ensures {
            for predicate in &ensure.predicates {
                let result = functions
                    .ok_or_else(|| {
                        SemanticError::incorrect(
                            "transaction/missing-predicate-context",
                            format!("entity spec {} requires predicate {predicate}", ensure.spec),
                        )
                    })?
                    .validate_entity_predicate_exact(predicate, &self.db_after, ensure.entity)?;
                if !crate::is_exact_true(&result) {
                    return Err(SemanticError::incorrect(
                        "transaction/entity-predicate",
                        format!(
                            "entity {} failed predicate {predicate} of spec {}",
                            ensure.entity, ensure.spec
                        ),
                    )
                    .detail("pred_return", format!("{result:?}")));
                }
            }
        }
        Ok(())
    }

    fn validate_ensure_attributes(&self) -> Result<(), SemanticError> {
        for ensure in &self.ensures {
            let mut missing = Vec::new();
            for attribute in &ensure.required {
                if self.db_after.values(ensure.entity, *attribute)?.is_empty() {
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
}

fn insert_predicate_role(
    required: &mut BTreeMap<String, crate::database::PredicateRole>,
    predicate: &str,
    role: crate::database::PredicateRole,
) -> Result<(), SemanticError> {
    if let Some(existing) = required.insert(predicate.to_owned(), role)
        && existing != role
    {
        return Err(SemanticError::incorrect(
            "program/predicate-role-conflict",
            format!("predicate {predicate} is required as both an attribute and entity predicate"),
        ));
    }
    Ok(())
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
    limits: AssessmentLimits,
    work: AssessmentReadWork,
}

impl<'a> Reader<'a> {
    fn new(base: &'a DatabaseValue, limits: AssessmentLimits) -> Self {
        Self {
            base,
            limits,
            work: AssessmentReadWork::default(),
        }
    }

    fn prefix(&mut self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        self.work.prefixes = self.work.prefixes.saturating_add(1);
        let mut datoms = Vec::new();
        for datom in self.base.current_prefix_cursor(prefix)? {
            let datom = datom?;
            let next_datoms = self.work.datoms.checked_add(1).ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Busy,
                    "transaction/read-capacity",
                    "transaction assessment read count overflowed",
                )
            })?;
            let next_bytes = self
                .work
                .retained_bytes
                .checked_add(datom.retained_bytes())
                .ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Busy,
                        "transaction/read-capacity",
                        "transaction assessment read-byte count overflowed",
                    )
                })?;
            if next_datoms > self.limits.max_read_datoms || next_bytes > self.limits.max_read_bytes
            {
                return Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "transaction/read-capacity",
                    format!(
                        "transaction assessment exceeds {} datoms or {} retained bytes",
                        self.limits.max_read_datoms, self.limits.max_read_bytes
                    ),
                ));
            }
            self.work.datoms = next_datoms;
            self.work.retained_bytes = next_bytes;
            datoms.push(datom);
        }
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
    assess_tiered_with_limits(base, ops, tx_instant, AssessmentLimits::unbounded())
}

pub(crate) fn assess_tiered_with_limits(
    base: &DatabaseValue,
    ops: &[TxOp],
    tx_instant: i64,
    limits: AssessmentLimits,
) -> Result<TieredAssessment, SemanticError> {
    if limits.max_read_datoms == 0 || limits.max_read_bytes == 0 {
        return Err(SemanticError::incorrect(
            "transaction/invalid-read-capacity",
            "transaction assessment read limits must be positive",
        ));
    }
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
    let initial_allocation_start =
        base.eidx_frontier()
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
    let mut reader = Reader::new(base, limits);
    let (mut logical, allocation_start) =
        prepare_schema_information(&mut reader, &ordered, tx, initial_allocation_start)?;
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

    let db_after = DatabaseValue::transaction_overlay(
        base.clone(),
        Arc::from(tx_data.clone()),
        Arc::clone(&successor_schema),
        basis_t,
        eidx_frontier,
        tx_instant,
    )?;

    Ok(TieredAssessment {
        db_before: base.clone(),
        db_after,
        basis_t,
        eidx_frontier,
        tx_data,
        tempids,
        ensures,
        successor_schema,
        read_work: reader.work,
    })
}

fn prepare_schema_information(
    reader: &mut Reader<'_>,
    ops: &[TxOp],
    tx: u64,
    mut allocation_frontier: u64,
) -> Result<(Vec<LogicalDatom>, u64), SemanticError> {
    let mut seen = BTreeSet::new();
    let mut installed = Vec::new();
    let mut candidate = reader.base.schema().clone();
    let mut changes = Vec::<(crate::Attribute, bool)>::new();
    let exact_upgrade = is_exact_excision_bootstrap_ops(reader.base.schema(), ops);

    for op in ops {
        let (attribute, install) = match op {
            TxOp::InstallAttribute(attribute) => (attribute, true),
            TxOp::AlterAttribute(attribute) => (attribute, false),
            _ => continue,
        };
        if !seen.insert(attribute.id) {
            return Err(SemanticError::conflict(
                "schema/multiple-changes",
                format!(
                    "attribute {} has multiple schema changes in one transaction",
                    attribute.id
                ),
            ));
        }
        crate::schema_eid_to_attr_id(u64::from(attribute.id))?;
        if install {
            let reserved = supported_system_idents()
                .iter()
                .any(|(entity, _)| *entity == u64::from(attribute.id));
            if reserved && !exact_upgrade {
                return Err(SemanticError::incorrect(
                    "schema/reserved-system-entity",
                    format!(
                        "entity {} is reserved by the native vocabulary",
                        attribute.id
                    ),
                ));
            }
            if u64::from(attribute.id) >= allocation_frontier {
                if u64::from(attribute.id) != allocation_frontier {
                    return Err(SemanticError::incorrect(
                        "schema/noncontiguous-attribute-id",
                        format!(
                            "fresh schema entity {} must use issued frontier {}",
                            attribute.id, allocation_frontier
                        ),
                    ));
                }
                allocation_frontier = allocation_frontier.checked_add(1).ok_or_else(|| {
                    SemanticError::incorrect(
                        "schema/attribute-id-overflow",
                        "schema entity allocation exhausted the entity-index space",
                    )
                })?;
            }
            candidate.install(attribute.clone())?;
            installed.push(attribute.id);
        } else {
            candidate.alter(attribute.clone())?;
        }
        changes.push((attribute.clone(), install));
    }
    candidate.validate_tuple_installations(&installed)?;

    let metadata_attributes = [
        DB_IDENT as u32,
        DB_VALUE_TYPE as u32,
        DB_CARDINALITY as u32,
        DB_UNIQUE as u32,
        DB_IS_COMPONENT as u32,
        DB_INDEX as u32,
        DB_NO_HISTORY as u32,
        DB_TUPLE_TYPE as u32,
        DB_TUPLE_TYPES as u32,
        DB_TUPLE_ATTRS as u32,
        DB_ATTR_PREDS as u32,
        DB_TUPLE_DISCONTINUED as u32,
    ];
    let mut logical = Vec::new();
    for (attribute, install) in changes {
        let desired = crate::schema::attribute_information_datoms(&attribute, &candidate, tx)?;
        let current = (!install)
            .then(|| reader.base.schema().attribute(attribute.id))
            .transpose()?;
        let property_changed = |metadata_attribute| {
            install
                || current.is_some_and(|current| {
                    attribute_property_changed(current, &attribute, metadata_attribute)
                })
        };
        for fact in reader.prefix(&IndexPrefix::Eavt {
            entity: u64::from(attribute.id),
            attribute: None,
            value: None,
        })? {
            if metadata_attributes.contains(&fact.attribute)
                && property_changed(fact.attribute)
                && !desired.iter().any(|wanted| same_eav(wanted, &fact))
            {
                logical.push(LogicalDatom {
                    entity: fact.entity,
                    attribute: fact.attribute,
                    value: fact.value,
                    added: false,
                });
            }
        }
        for datom in desired {
            if property_changed(datom.attribute)
                && !reader.contains(datom.entity, datom.attribute, &datom.value)?
            {
                logical.push(LogicalDatom {
                    entity: datom.entity,
                    attribute: datom.attribute,
                    value: datom.value,
                    added: true,
                });
            }
        }
    }
    validate_frontier(allocation_frontier)?;
    Ok((logical, allocation_frontier))
}

fn is_exact_excision_bootstrap_ops(schema: &Schema, ops: &[TxOp]) -> bool {
    let ids = [
        DB_EXCISE as u32,
        crate::DB_EXCISE_ATTRS as u32,
        DB_EXCISE_BEFORE_T as u32,
        DB_EXCISE_BEFORE as u32,
    ];
    if ops.len() != ids.len() || ids.iter().any(|id| schema.attribute(*id).is_ok()) {
        return false;
    }
    ids.iter().all(|id| {
        let expected = supported_system_attributes()
            .into_iter()
            .find(|attribute| attribute.id == *id);
        ops.iter().any(|op| {
            matches!((op, &expected), (TxOp::InstallAttribute(actual), Some(expected)) if actual == expected)
        })
    })
}

fn synthesize_schema_hooks(
    reader: &mut Reader<'_>,
    logical: &mut Vec<LogicalDatom>,
) -> Result<(), SemanticError> {
    let mut explicit = BTreeMap::<u64, u32>::new();
    for datom in logical.iter().filter(|datom| {
        matches!(
            u64::from(datom.attribute),
            DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
        )
    }) {
        if !datom.added || datom.entity != DB_PART_DB {
            return Err(SemanticError::incorrect(
                "schema/invalid-hook-datom",
                "attribute install/alter hooks must be assertions on :db.part/db",
            ));
        }
        let Value::Ref(target) = datom.value else {
            return Err(SemanticError::incorrect(
                "schema/invalid-hook-target",
                "attribute install/alter hook values must be entity references",
            ));
        };
        let attribute = crate::schema_eid_to_attr_id(target)?;
        let expected = if reader.base.schema().attribute(attribute).is_ok() {
            DB_ALTER_ATTRIBUTE as u32
        } else {
            DB_INSTALL_ATTRIBUTE as u32
        };
        if datom.attribute != expected {
            return Err(SemanticError::incorrect(
                "schema/wrong-hook-kind",
                "schema hook kind does not match db-before installation state",
            ));
        }
        if let Some(prior) = explicit.insert(target, datom.attribute)
            && prior != datom.attribute
        {
            return Err(SemanticError::conflict(
                "schema/conflicting-hooks",
                "one schema entity cannot be installed and altered together",
            ));
        }
    }

    let mut touched = BTreeSet::new();
    for datom in logical
        .iter()
        .filter(|datom| is_attribute_hook_property(datom.attribute))
    {
        crate::schema_eid_to_attr_id(datom.entity)?;
        if reader.contains(datom.entity, datom.attribute, &datom.value)? != datom.added {
            touched.insert(datom.entity);
        }
    }
    logical.retain(|datom| {
        !matches!(
            u64::from(datom.attribute),
            DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
        )
    });
    for target in touched {
        let attribute = crate::schema_eid_to_attr_id(target)?;
        logical.push(LogicalDatom {
            entity: DB_PART_DB,
            attribute: if reader.base.schema().attribute(attribute).is_ok() {
                DB_ALTER_ATTRIBUTE as u32
            } else {
                DB_INSTALL_ATTRIBUTE as u32
            },
            value: Value::Ref(target),
            added: true,
        });
    }
    Ok(())
}

fn derive_successor_schema(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    tx: u64,
) -> Result<Schema, SemanticError> {
    // Schema and ident projections are resident authenticated metadata. Most
    // transactions do not touch either; preserve the immutable projection
    // directly instead of issuing one durable EAVT seek per attribute merely
    // to rediscover an unchanged schema.
    if !logical.iter().any(|datom| {
        schema_information_attribute(datom.attribute)
            || matches!(
                u64::from(datom.attribute),
                DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
            )
    }) {
        return Ok(reader.base.schema().clone());
    }

    let mut current = reader
        .prefix(&IndexPrefix::Eavt {
            entity: DB_PART_DB,
            attribute: None,
            value: None,
        })?
        .into_iter()
        .filter(|datom| {
            matches!(
                u64::from(datom.attribute),
                DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
            )
        })
        .collect::<Vec<_>>();
    let attributes = reader
        .base
        .schema()
        .attributes()
        .map(|attribute| attribute.id)
        .collect::<Vec<_>>();
    for attribute in attributes {
        current.extend(
            reader
                .prefix(&IndexPrefix::Eavt {
                    entity: u64::from(attribute),
                    attribute: None,
                    value: None,
                })?
                .into_iter()
                .filter(|datom| schema_information_attribute(datom.attribute)),
        );
    }
    for datom in logical.iter().filter(|datom| {
        schema_information_attribute(datom.attribute)
            || matches!(
                u64::from(datom.attribute),
                DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
            )
    }) {
        if datom.added {
            if !current.iter().any(|fact| {
                fact.entity == datom.entity
                    && fact.attribute == datom.attribute
                    && fact.value.stored_eq(&datom.value)
            }) {
                current.push(Datom {
                    entity: datom.entity,
                    attribute: datom.attribute,
                    value: datom.value.clone(),
                    tx,
                    added: true,
                });
            }
        } else {
            current.retain(|fact| {
                !(fact.entity == datom.entity
                    && fact.attribute == datom.attribute
                    && fact.value.stored_eq(&datom.value))
            });
        }
    }

    // Only attribute and system aliases are required to derive Schema. The
    // full general ident cache remains resident in the concrete tiered value
    // and is updated delta-first by TransactionOverlay.
    let mut ident_assertions = Vec::new();
    for (entity, ident) in supported_system_idents() {
        ident_assertions.push(ident_datom(entity, ident, 0));
    }
    for attribute in reader.base.schema().attributes() {
        ident_assertions.push(ident_datom(
            u64::from(attribute.id),
            attribute.ident.clone(),
            0,
        ));
    }
    for (ident, attribute) in reader.base.schema().ident_aliases() {
        ident_assertions.push(ident_datom(u64::from(attribute), ident.clone(), 0));
    }
    ident_assertions.extend(
        logical
            .iter()
            .filter(|datom| datom.added && u64::from(datom.attribute) == DB_IDENT)
            .map(|datom| Datom {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                tx,
                added: true,
            }),
    );
    let idents = IdentIndex::derive(ident_assertions.iter(), DB_IDENT as u32)?;
    Schema::derive_from_information(&current, &idents)
}

fn ident_datom(entity: u64, ident: crate::Keyword, tx: u64) -> Datom {
    Datom {
        entity,
        attribute: DB_IDENT as u32,
        value: Value::Keyword(ident),
        tx,
        added: true,
    }
}

fn validate_schema_transition(
    reader: &mut Reader<'_>,
    successor: &Schema,
    logical: &[LogicalDatom],
) -> Result<(), SemanticError> {
    for required in crate::canonical_genesis_datoms() {
        if logical.iter().any(|datom| {
            !datom.added
                && datom.entity == required.entity
                && datom.attribute == required.attribute
                && datom.value.stored_eq(&required.value)
        }) {
            return Err(SemanticError::incorrect(
                "schema/native-information-immutable",
                "native genesis information cannot be retracted or replaced",
            ));
        }
    }
    for system in supported_system_attributes() {
        if reader.base.schema().attribute(system.id).is_ok()
            && successor.attribute(system.id)? != &system
        {
            return Err(SemanticError::incorrect(
                "schema/native-attribute-immutable",
                format!(
                    "native attribute {} cannot be altered",
                    system.ident.qualified_name()
                ),
            ));
        }
    }
    for current in reader.base.schema().attributes() {
        if successor.attribute(current.id).is_err() {
            return Err(SemanticError::incorrect(
                "schema/removed-attribute",
                format!("committed information removed attribute {}", current.id),
            ));
        }
    }

    let mut installed = Vec::new();
    for proposed in successor.attributes() {
        match reader.base.schema().attribute(proposed.id) {
            Ok(current) if current == proposed => {}
            Ok(current) => {
                if current.value_type != proposed.value_type {
                    return Err(SemanticError::incorrect(
                        "schema/value-type-immutable",
                        "an installed attribute's value type cannot change",
                    ));
                }
                if current.tuple != proposed.tuple {
                    return Err(SemanticError::incorrect(
                        "schema/tuple-definition-immutable",
                        "an installed tuple definition cannot change",
                    ));
                }
                if current.tuple_discontinued && !proposed.tuple_discontinued {
                    return Err(SemanticError::incorrect(
                        "schema/tuple-discontinuation-irreversible",
                        "a discontinued composite tuple cannot be resumed",
                    ));
                }
                if current.cardinality == Cardinality::Many
                    && proposed.cardinality == Cardinality::One
                {
                    let facts = successor_attribute_datoms(reader, logical, proposed.id)?;
                    let mut counts = BTreeMap::<u64, usize>::new();
                    for fact in facts {
                        *counts.entry(fact.entity).or_default() += 1;
                    }
                    if counts.values().any(|count| *count > 1) {
                        return Err(SemanticError::conflict(
                            "schema/cardinality-change-conflict",
                            "current data contains multiple values for one entity",
                        ));
                    }
                }
                if current.unique.is_none() && proposed.unique.is_some() {
                    if !current.indexed {
                        return Err(SemanticError::incorrect(
                            "schema/unique-requires-avet",
                            "adding uniqueness requires an existing AVET index",
                        ));
                    }
                    validate_attribute_uniqueness(&successor_attribute_datoms(
                        reader,
                        logical,
                        proposed.id,
                    )?)?;
                }
            }
            Err(_) => {
                let reserved = supported_system_idents()
                    .iter()
                    .any(|(entity, _)| *entity == u64::from(proposed.id));
                if reserved
                    && supported_system_attributes()
                        .iter()
                        .find(|attribute| attribute.id == proposed.id)
                        != Some(proposed)
                {
                    return Err(SemanticError::incorrect(
                        "schema/reserved-system-entity",
                        format!(
                            "entity {} is reserved by the native vocabulary",
                            proposed.id
                        ),
                    ));
                }
                installed.push(proposed.id);
            }
        }
    }
    successor.validate_tuple_installations(&installed)
}

fn successor_attribute_datoms(
    reader: &mut Reader<'_>,
    logical: &[LogicalDatom],
    attribute: u32,
) -> Result<Vec<Datom>, SemanticError> {
    let mut facts = reader.prefix(&IndexPrefix::Aevt {
        attribute,
        entity: None,
        value: None,
    })?;
    for datom in logical.iter().filter(|datom| datom.attribute == attribute) {
        if datom.added {
            if !facts
                .iter()
                .any(|fact| fact.entity == datom.entity && fact.value.stored_eq(&datom.value))
            {
                facts.push(Datom {
                    entity: datom.entity,
                    attribute,
                    value: datom.value.clone(),
                    tx: 0,
                    added: true,
                });
            }
        } else {
            facts.retain(|fact| {
                !(fact.entity == datom.entity && fact.value.stored_eq(&datom.value))
            });
        }
    }
    Ok(facts)
}

fn validate_attribute_uniqueness(facts: &[Datom]) -> Result<(), SemanticError> {
    for (index, left) in facts.iter().enumerate() {
        if left.value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in uniqueness",
            ));
        }
        if facts[index + 1..]
            .iter()
            .any(|right| left.entity != right.entity && left.value.index_cmp(&right.value).is_eq())
        {
            return Err(SemanticError::conflict(
                "schema/unique-change-conflict",
                "current values must be unique before adding uniqueness",
            ));
        }
    }
    Ok(())
}

fn schema_information_attribute(attribute: u32) -> bool {
    matches!(
        u64::from(attribute),
        DB_IDENT
            | DB_INSTALL_ATTRIBUTE
            | DB_ALTER_ATTRIBUTE
            | DB_VALUE_TYPE
            | DB_CARDINALITY
            | DB_UNIQUE
            | DB_IS_COMPONENT
            | DB_INDEX
            | DB_NO_HISTORY
            | DB_TUPLE_TYPE
            | DB_TUPLE_TYPES
            | DB_TUPLE_ATTRS
            | DB_TUPLE_DISCONTINUED
            | DB_ATTR_PREDS
    )
}

fn is_attribute_hook_property(attribute: u32) -> bool {
    matches!(
        u64::from(attribute),
        DB_VALUE_TYPE
            | DB_CARDINALITY
            | DB_UNIQUE
            | DB_IS_COMPONENT
            | DB_INDEX
            | DB_NO_HISTORY
            | DB_TUPLE_TYPE
            | DB_TUPLE_TYPES
            | DB_TUPLE_ATTRS
            | DB_ATTR_PREDS
            | DB_TUPLE_DISCONTINUED
    )
}

fn attribute_property_changed(
    current: &crate::Attribute,
    proposed: &crate::Attribute,
    attribute: u32,
) -> bool {
    match u64::from(attribute) {
        DB_IDENT => current.ident != proposed.ident,
        DB_VALUE_TYPE => current.value_type != proposed.value_type,
        DB_CARDINALITY => current.cardinality != proposed.cardinality,
        DB_UNIQUE => current.unique != proposed.unique,
        DB_IS_COMPONENT => current.component != proposed.component,
        DB_INDEX => current.indexed != proposed.indexed,
        DB_NO_HISTORY => current.no_history != proposed.no_history,
        DB_TUPLE_TYPE | DB_TUPLE_TYPES | DB_TUPLE_ATTRS => current.tuple != proposed.tuple,
        DB_ATTR_PREDS => current.predicates != proposed.predicates,
        DB_TUPLE_DISCONTINUED => current.tuple_discontinued != proposed.tuple_discontinued,
        _ => false,
    }
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
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
        TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
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
    successor_schema: &Schema,
    logical: &[LogicalDatom],
) -> Result<(), SemanticError> {
    let mut touched_ea = BTreeSet::new();
    let mut touched_av = Vec::<(u32, Value)>::new();
    let mut excision_entities = BTreeSet::new();
    for datom in logical {
        touched_ea.insert((datom.entity, datom.attribute));
        let descriptor = successor_schema.attribute(datom.attribute)?;
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
        let descriptor = successor_schema.attribute(attribute)?;
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
        if existed != datom.added
            || (datom.added && u64::from(datom.attribute) == DB_ALTER_ATTRIBUTE)
        {
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
        let assessed = assess_tiered(&value, &ops, 11).unwrap();
        assert_eq!(assessed.basis_t, expected.db_after.basis_t());
        assert_eq!(assessed.eidx_frontier, expected.db_after.eidx_frontier());
        assert_eq!(assessed.tempids, expected.tempids);
        assert_eq!(assessed.tx_data, expected.tx_data);
        assert_eq!(
            assessed.db_after.values(entity, COUNT).unwrap(),
            expected
                .db_after
                .values(entity, COUNT)
                .into_iter()
                .cloned()
                .collect::<Vec<_>>()
        );
        assert_eq!(assessed.db_after.basis_t(), expected.db_after.basis_t());
        assert_eq!(
            assessed.db_after.last_tx_instant().unwrap(),
            expected.db_after.last_tx_instant()
        );
        assessed.validate_exact(None).unwrap();
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
        let assessed = assess_tiered(&value, &ops, 11).unwrap();
        assert_eq!(assessed.tx_data, expected.tx_data);
    }

    #[test]
    fn schema_install_and_alter_match_eager_metadata_information() {
        let initial = Database::new(schema()).unwrap();
        let installed_id = u32::try_from(initial.eidx_frontier()).unwrap();
        let installed = Attribute::new(
            installed_id,
            Keyword::new("item", "status"),
            ValueType::Keyword,
            Cardinality::One,
        );
        let install_ops = vec![TxOp::InstallAttribute(installed.clone())];
        let expected_install = initial.with(&install_ops, 10).unwrap();
        let assessed_install = assess_tiered(
            &DatabaseValue::eager(Arc::new(initial.clone())),
            &install_ops,
            10,
        )
        .unwrap();
        assert_eq!(assessed_install.tx_data, expected_install.tx_data);
        assert_eq!(
            assessed_install.successor_schema.as_ref(),
            expected_install.db_after.schema()
        );
        assert_eq!(
            assessed_install.eidx_frontier,
            expected_install.db_after.eidx_frontier()
        );

        let mut altered = installed;
        altered.indexed = true;
        altered.no_history = true;
        altered.ident = Keyword::new("item", "state");
        let alter_ops = vec![TxOp::AlterAttribute(altered)];
        let expected_alter = expected_install.db_after.with(&alter_ops, 11).unwrap();
        let assessed_alter = assess_tiered(
            &DatabaseValue::eager(Arc::new(expected_install.db_after)),
            &alter_ops,
            11,
        )
        .unwrap();
        assert_eq!(assessed_alter.tx_data, expected_alter.tx_data);
        assert_eq!(
            assessed_alter.successor_schema.as_ref(),
            expected_alter.db_after.schema()
        );
        assert_eq!(
            assessed_alter.db_after.schema(),
            expected_alter.db_after.schema()
        );
    }

    #[test]
    fn rejection_codes_match_eager_for_local_conflicts_and_invalid_inputs() {
        let initial = Database::new(schema()).unwrap();
        let seeded = initial
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("one".into()),
                        attribute: NAME,
                        value: TxValue::Scalar(Value::String("one".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("one".into()),
                        attribute: COUNT,
                        value: TxValue::Scalar(Value::Long(1)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("two".into()),
                        attribute: NAME,
                        value: TxValue::Scalar(Value::String("two".into())),
                    },
                ],
                10,
            )
            .unwrap()
            .db_after;
        let one = seeded
            .lookup(NAME, &Value::String("one".into()))
            .unwrap()
            .unwrap();
        let two = seeded
            .lookup(NAME, &Value::String("two".into()))
            .unwrap()
            .unwrap();
        let cases = vec![
            vec![TxOp::Cas {
                entity: EntityRef::Id(one),
                attribute: COUNT,
                old: Some(TxValue::Scalar(Value::Long(99))),
                new: TxValue::Scalar(Value::Long(2)),
            }],
            vec![
                TxOp::Add {
                    entity: EntityRef::Id(one),
                    attribute: COUNT,
                    value: TxValue::Scalar(Value::Long(2)),
                },
                TxOp::Add {
                    entity: EntityRef::Id(one),
                    attribute: COUNT,
                    value: TxValue::Scalar(Value::Long(3)),
                },
            ],
            vec![TxOp::Add {
                entity: EntityRef::Id(two),
                attribute: NAME,
                value: TxValue::Scalar(Value::String("one".into())),
            }],
            vec![TxOp::Add {
                entity: EntityRef::Id(one),
                attribute: COUNT,
                value: TxValue::Scalar(Value::String("wrong-type".into())),
            }],
        ];

        for ops in cases {
            let eager = seeded.with(&ops, 11).unwrap_err();
            let tiered = assess_tiered(&DatabaseValue::eager(Arc::new(seeded.clone())), &ops, 11)
                .unwrap_err();
            assert_eq!(tiered.category, eager.category, "ops: {ops:?}");
            assert_eq!(tiered.code, eager.code, "ops: {ops:?}");
        }
    }

    #[test]
    fn read_capacity_stops_broad_assessment_at_the_cursor_boundary() {
        let initial = Database::new(schema()).unwrap();
        let seeded = initial.with(&add("one", 1), 10).unwrap().db_after;
        let entity = seeded
            .lookup(NAME, &Value::String("one".into()))
            .unwrap()
            .unwrap();
        let error = assess_tiered_with_limits(
            &DatabaseValue::eager(Arc::new(seeded)),
            &[TxOp::RetractEntity(EntityRef::Id(entity))],
            11,
            AssessmentLimits {
                max_read_datoms: 1,
                max_read_bytes: u64::MAX,
            },
        )
        .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Busy);
        assert_eq!(error.code, "transaction/read-capacity");

        let invalid = assess_tiered_with_limits(
            &DatabaseValue::eager(Arc::new(Database::new(schema()).unwrap())),
            &[],
            10,
            AssessmentLimits {
                max_read_datoms: 0,
                max_read_bytes: 1,
            },
        )
        .unwrap_err();
        assert_eq!(invalid.code, "transaction/invalid-read-capacity");
    }
}
