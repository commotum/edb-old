//! Incremental transaction assessment over one exact immutable database value.
//!
//! The eager [`crate::Database`] transition remains the semantic oracle.  This
//! module is the production-shaped counterpart: it obtains only left-prefix
//! ranges from the db-before value and keeps the proposed successor as a small
//! logical delta.  It deliberately contains no `materialize` fallback.

use crate::database::{UpsertIdentityValue, normalize_excision_before_t, validated_entity_tempids};
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
    /// Physical prefix cursor misses. Repeated transaction-local requests for
    /// the same structural prefix are counted in `prefix_hits` instead.
    pub(crate) prefixes: u64,
    pub(crate) prefix_hits: u64,
    pub(crate) datoms: u64,
    pub(crate) retained_bytes: u64,
    /// Active composites selected through the schema's recovered
    /// constituent reverse index. This must follow touched attributes rather
    /// than total installed schema width.
    pub(crate) composite_candidates: u64,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct AssessmentLimits {
    pub(crate) max_read_datoms: u64,
    pub(crate) max_read_bytes: u64,
}

impl AssessmentLimits {
    #[cfg(test)]
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
    #[cfg(test)]
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
    required
        .entry(predicate.to_owned())
        .and_modify(|existing| *existing = existing.include(role))
        .or_insert(role);
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
    prefix_memo: BTreeMap<IndexPrefix, Arc<[Datom]>>,
    history_first_memo: BTreeMap<IndexPrefix, Option<Datom>>,
}

impl<'a> Reader<'a> {
    fn new(base: &'a DatabaseValue, limits: AssessmentLimits) -> Self {
        Self {
            base,
            limits,
            work: AssessmentReadWork::default(),
            prefix_memo: BTreeMap::new(),
            history_first_memo: BTreeMap::new(),
        }
    }

    fn charge(&mut self, datom: &Datom) -> Result<(), SemanticError> {
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
        if next_datoms > self.limits.max_read_datoms || next_bytes > self.limits.max_read_bytes {
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
        Ok(())
    }

    fn prefix(&mut self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        // Recovered ProcessExpander eagerly prefetches identity, composite,
        // redundancy, and uniqueness ranges (`datomic/db.clj`, get-ids and
        // transact-data preparation around 7051-7354). The native assessor's
        // synchronous equivalent is deliberately narrower: memoize an exact
        // structural IndexPrefix for this one assessment, avoiding repeated
        // PostgreSQL/tree work without inventing an asynchronous hint system.
        if let Some(datoms) = self.prefix_memo.get(prefix).map(Arc::clone) {
            self.work.prefix_hits = self.work.prefix_hits.saturating_add(1);
            return Ok(datoms.iter().cloned().collect());
        }
        self.work.prefixes = self.work.prefixes.saturating_add(1);
        let mut datoms = Vec::new();
        for datom in self.base.current_prefix_cursor(prefix)? {
            let datom = datom?;
            self.charge(&datom)?;
            datoms.push(datom);
        }
        self.prefix_memo
            .insert(prefix.clone(), Arc::from(datoms.clone()));
        Ok(datoms)
    }

    fn has_historical_values(&mut self, attribute: u32) -> Result<bool, SemanticError> {
        let prefix = IndexPrefix::Aevt {
            attribute,
            entity: None,
            value: None,
        };
        if let Some(first) = self.history_first_memo.get(&prefix) {
            self.work.prefix_hits = self.work.prefix_hits.saturating_add(1);
            return Ok(first.is_some());
        }
        self.work.prefixes = self.work.prefixes.saturating_add(1);
        let first = self.base.history_prefix_first(&prefix)?;
        if let Some(datom) = &first {
            self.charge(datom)?;
        }
        let present = first.is_some();
        self.history_first_memo.insert(prefix, first);
        Ok(present)
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
#[cfg(test)]
pub(crate) fn assess_tiered(
    base: &DatabaseValue,
    ops: &[TxOp],
    tx_instant: i64,
) -> Result<TieredAssessment, SemanticError> {
    assess_tiered_with_limits(base, ops, tx_instant, AssessmentLimits::unbounded())
}

#[cfg(test)]
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
    assess_tiered_with_remaining_limits(base, ops, tx_instant, limits)
}

/// Assess against the exact allowance left by an enclosing transaction read
/// budget. Unlike the standalone bounded entry point, zero is meaningful
/// here: an assessment that yields no logical datoms may proceed, while the
/// first datom read is rejected by [`Reader::charge`].
pub(crate) fn assess_tiered_with_remaining_limits(
    base: &DatabaseValue,
    ops: &[TxOp],
    tx_instant: i64,
    limits: AssessmentLimits,
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
    let (tempids, eidx_frontier) = resolve_tempids(&mut reader, &ordered, tx, allocation_start)?;
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

    let successor_schema = derive_successor_schema(&mut reader, &logical, tx)?;
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
        #[cfg(test)]
        read_work: reader.work,
    })
}

fn prepare_schema_information(
    reader: &mut Reader<'_>,
    ops: &[TxOp],
    tx: u64,
    mut allocation_frontier: u64,
) -> Result<(Vec<LogicalDatom>, u64), SemanticError> {
    if !ops
        .iter()
        .any(|op| matches!(op, TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_)))
    {
        return Ok((Vec::new(), allocation_frontier));
    }

    let mut seen = BTreeSet::new();
    let mut installed = Vec::new();
    // A real schema edit needs one mutable candidate for cross-attribute
    // validation. Ordinary data transactions return above and never copy the
    // resident schema merely to discover that it did not change.
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
) -> Result<Arc<Schema>, SemanticError> {
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
        return Ok(reader.base.schema_arc());
    }

    // The authenticated Schema is the resident cache recovered `Db` keeps for
    // exactly this purpose. Reconstruct unchanged information from that cache;
    // issuing one EAVT seek for every installed attribute made a one-attribute
    // alter proportional to the whole schema and allowed an unrelated schema
    // size to exhaust the transaction read budget.
    //
    // For an entity whose raw schema-as-data facts are actually being edited,
    // retain the physical spelling of its current information. This matters
    // for details intentionally absent from the typed projection, notably a
    // composite's historical constituent ident after an ident rename. An
    // ident repurpose can also affect an active composite without touching the
    // composite entity, so include only composites that depend on the ident's
    // previous target. Discontinued composites deliberately keep their frozen
    // resolved constituents, matching the recovered reverse-link removal.
    let mut physical_entities = BTreeSet::new();
    for datom in logical.iter().filter(|datom| {
        schema_information_attribute(datom.attribute)
            && datom.entity != DB_PART_DB
            && reader
                .base
                .schema()
                .attribute(u32::try_from(datom.entity).unwrap_or(u32::MAX))
                .is_ok()
    }) {
        physical_entities.insert(u32::try_from(datom.entity).map_err(|_| {
            SemanticError::incorrect(
                "schema/attribute-id-range",
                "schema entity does not fit the native attribute-id domain",
            )
        })?);
    }
    for target in logical.iter().filter_map(|datom| {
        (datom.entity == DB_PART_DB
            && matches!(
                u64::from(datom.attribute),
                DB_INSTALL_ATTRIBUTE | DB_ALTER_ATTRIBUTE
            ))
        .then_some(&datom.value)
    }) {
        if let Value::Ref(target) = target
            && let Ok(attribute) = crate::schema_eid_to_attr_id(*target)
            && reader.base.schema().attribute(attribute).is_ok()
        {
            physical_entities.insert(attribute);
        }
    }
    let retargeted_attributes = logical
        .iter()
        .filter_map(|datom| {
            if !datom.added || u64::from(datom.attribute) != DB_IDENT {
                return None;
            }
            let Value::Keyword(ident) = &datom.value else {
                return None;
            };
            let previous = reader.base.schema().resolve_ident(ident)?;
            (u64::from(previous) != datom.entity).then_some(previous)
        })
        .collect::<BTreeSet<_>>();
    if !retargeted_attributes.is_empty() {
        physical_entities.extend(reader.base.schema().attributes().filter_map(|attribute| {
            let Some(TupleSpec::Composite(constituents)) = &attribute.tuple else {
                return None;
            };
            (!attribute.tuple_discontinued
                && constituents
                    .iter()
                    .any(|constituent| retargeted_attributes.contains(constituent)))
            .then_some(attribute.id)
        }));
    }

    let mut current = Vec::new();
    for attribute in reader.base.schema().attributes() {
        current.push(Datom {
            entity: DB_PART_DB,
            attribute: DB_INSTALL_ATTRIBUTE as u32,
            value: Value::Ref(u64::from(attribute.id)),
            tx: 0,
            added: true,
        });
        if physical_entities.contains(&attribute.id) {
            current.extend(
                reader
                    .prefix(&IndexPrefix::Eavt {
                        entity: u64::from(attribute.id),
                        attribute: None,
                        value: None,
                    })?
                    .into_iter()
                    .filter(|datom| schema_information_attribute(datom.attribute)),
            );
        } else {
            current.extend(crate::schema::attribute_information_datoms(
                attribute,
                reader.base.schema(),
                0,
            )?);
        }
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
    Schema::derive_from_information(&current, &idents).map(Arc::new)
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
                    // `:db/index true` is only the logical schema fact. The
                    // recovered Attribute.hasAVET also requires physical
                    // storage availability; while a background backfill is
                    // pending, adding uniqueness must still fail. The sole
                    // synchronous exception is an attribute with no history,
                    // for which there is nothing to backfill.
                    if !reader.base.physical_avet_ready(proposed.id)
                        && reader.has_historical_values(proposed.id)?
                    {
                        return Err(SemanticError::incorrect(
                            "schema/unique-requires-avet",
                            "adding uniqueness to an attribute with historical values requires a physically ready AVET index",
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
    validate_attribute_uniqueness_with_work(facts).map(|_| ())
}

/// Validate one newly unique attribute with the same logical comparator used
/// by AVET, while retaining physically distinct representations such as
/// BigDecimals with different scales as separate facts. Sorting makes the
/// work O(n log n), instead of comparing every fact with every later fact.
/// The returned count is a direct scaling witness for unit tests; optimized
/// builds compile the counter increments away.
fn validate_attribute_uniqueness_with_work(facts: &[Datom]) -> Result<usize, SemanticError> {
    let earliest_nan = facts.iter().position(|fact| fact.value.is_nan());
    let mut ordered = facts
        .iter()
        .enumerate()
        .filter(|(_, fact)| !fact.value.is_nan())
        .collect::<Vec<_>>();
    let mut comparisons = 0_usize;
    ordered.sort_by(|left, right| {
        count_comparison(&mut comparisons);
        left.1
            .value
            .index_cmp(&right.1.value)
            .then(left.1.entity.cmp(&right.1.entity))
            .then_with(|| left.1.value.stored_cmp(&right.1.value))
    });
    let mut group_start = 0;
    let mut earliest_conflict = None;
    while group_start < ordered.len() {
        let mut group_end = group_start + 1;
        while group_end < ordered.len() {
            count_comparison(&mut comparisons);
            if ordered[group_start]
                .1
                .value
                .index_cmp(&ordered[group_end].1.value)
                .is_ne()
            {
                break;
            }
            group_end += 1;
        }
        let group = &ordered[group_start..group_end];
        if group
            .windows(2)
            .any(|pair| pair[0].1.entity != pair[1].1.entity)
        {
            let first = group
                .iter()
                .map(|(original, _)| *original)
                .min()
                .expect("a uniqueness group is nonempty");
            earliest_conflict =
                Some(earliest_conflict.map_or(first, |prior: usize| prior.min(first)));
        }
        group_start = group_end;
    }
    if earliest_nan.is_some_and(|nan| earliest_conflict.is_none_or(|conflict| nan <= conflict)) {
        return Err(SemanticError::incorrect(
            "transaction/nan-cannot-identify",
            "NaN cannot participate in uniqueness",
        ));
    }
    if earliest_conflict.is_some() {
        return Err(SemanticError::conflict(
            "schema/unique-change-conflict",
            "current values must be unique before adding uniqueness",
        ));
    }
    Ok(comparisons)
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
    tx: u64,
    allocation_start: u64,
) -> Result<(BTreeMap<String, u64>, u64), SemanticError> {
    let names = validated_entity_tempids(ops)?;
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
            value,
        } = op
        else {
            continue;
        };
        let descriptor = reader.base.schema().attribute(*attribute)?;
        let Some(unique) = descriptor.unique else {
            continue;
        };
        let value = resolve_upsert_identity_value(reader, *attribute, value, tx)?;
        if value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in upsert or uniqueness",
            ));
        }
        identities.push((positions[name], *attribute, value, unique));
    }
    union_identity_assertions(&mut union, &identities);

    let mut existing_by_root = BTreeMap::new();
    for (temp, attribute, value, unique) in &identities {
        let Some(value) = value.resolved() else {
            continue;
        };
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

/// Group equivalent identity assertions in index-key order. Recovered
/// `get-ids` first builds keyed identity groups and only then joins tempids;
/// this sorted Rust form preserves that shape and deterministic lowest-index
/// union roots without the former all-pairs walk.
fn union_identity_assertions(
    union: &mut UnionFind,
    identities: &[(usize, u32, UpsertIdentityValue, Unique)],
) -> usize {
    let mut ordered = identities
        .iter()
        .enumerate()
        .filter_map(|(index, (_, _, _, unique))| (*unique == Unique::Identity).then_some(index))
        .collect::<Vec<_>>();
    let mut comparisons = 0_usize;
    ordered.sort_by(|left, right| {
        count_comparison(&mut comparisons);
        let (_, left_attribute, left_value, _) = &identities[*left];
        let (_, right_attribute, right_value, _) = &identities[*right];
        left_attribute
            .cmp(right_attribute)
            .then_with(|| compare_upsert_identity_values(left_value, right_value))
    });
    for pair in ordered.windows(2) {
        count_comparison(&mut comparisons);
        let (left_temp, left_attribute, left_value, _) = &identities[pair[0]];
        let (right_temp, right_attribute, right_value, _) = &identities[pair[1]];
        if left_attribute == right_attribute && left_value.same_key(right_value) {
            union.join(*left_temp, *right_temp);
        }
    }
    comparisons
}

fn compare_upsert_identity_values(
    left: &UpsertIdentityValue,
    right: &UpsertIdentityValue,
) -> Ordering {
    match (left, right) {
        (UpsertIdentityValue::Resolved(left), UpsertIdentityValue::Resolved(right)) => {
            left.index_cmp(right)
        }
        (UpsertIdentityValue::TempRef(left), UpsertIdentityValue::TempRef(right)) => {
            left.cmp(right)
        }
        (UpsertIdentityValue::Resolved(_), UpsertIdentityValue::TempRef(_)) => Ordering::Less,
        (UpsertIdentityValue::TempRef(_), UpsertIdentityValue::Resolved(_)) => Ordering::Greater,
    }
}

#[inline]
fn count_comparison(comparisons: &mut usize) {
    if cfg!(test) {
        *comparisons = comparisons.saturating_add(1);
    }
}

fn resolve_upsert_identity_value(
    reader: &mut Reader<'_>,
    attribute: u32,
    value: &TxValue,
    tx: u64,
) -> Result<UpsertIdentityValue, SemanticError> {
    let descriptor = reader.base.schema().attribute(attribute)?;
    match value {
        TxValue::Scalar(value) => {
            validate_explicit_value_refs(reader.base, value)?;
            reader.base.schema().validate_value(descriptor, value)?;
            Ok(UpsertIdentityValue::Resolved(value.clone()))
        }
        TxValue::Entity(EntityRef::Temp(name)) if descriptor.value_type == ValueType::Ref => {
            Ok(UpsertIdentityValue::TempRef(name.clone()))
        }
        TxValue::Entity(entity) if descriptor.value_type == ValueType::Ref => {
            let value = Value::Ref(resolve_entity(reader, entity, tx, &BTreeMap::new())?);
            reader.base.schema().validate_value(descriptor, &value)?;
            Ok(UpsertIdentityValue::Resolved(value))
        }
        TxValue::Entity(_) => Err(SemanticError::incorrect(
            "transaction/value-type",
            format!(
                "attribute {} requires {:?}",
                descriptor.ident.qualified_name(),
                descriptor.value_type
            ),
        )),
    }
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
    // Recovered `Db` maintains a constituent -> composite reverse map and its
    // transaction prefetcher consults that map for each touched attribute
    // (`composites-prefetcher` / `create-composite`). Preserve that shape:
    // schema size must not become ordinary transaction work.
    let composite_ids = touched
        .iter()
        .flat_map(|(_, attribute)| reader.base.schema().composites_for_constituent(*attribute))
        .collect::<BTreeSet<_>>();
    reader.work.composite_candidates = reader
        .work
        .composite_candidates
        .saturating_add(composite_ids.len() as u64);
    for composite_id in composite_ids {
        let composite = reader.base.schema().attribute(composite_id)?.clone();
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
    let mut touched_ea = BTreeMap::<(u64, u32), Vec<&LogicalDatom>>::new();
    let mut unique_indices = Vec::new();
    let mut excision_entities = BTreeSet::new();
    for (index, datom) in logical.iter().enumerate() {
        touched_ea
            .entry((datom.entity, datom.attribute))
            .or_default()
            .push(datom);
        let descriptor = successor_schema.attribute(datom.attribute)?;
        if descriptor.unique.is_some() {
            unique_indices.push(index);
        }
        if matches!(
            u64::from(datom.attribute),
            DB_EXCISE | DB_EXCISE_BEFORE_T | DB_EXCISE_BEFORE
        ) {
            excision_entities.insert(datom.entity);
        }
    }
    for (&(entity, attribute), deltas) in &touched_ea {
        let descriptor = successor_schema.attribute(attribute)?;
        let values = successor_values_from_deltas(reader, entity, attribute, deltas)?;
        if descriptor.cardinality == Cardinality::One && values.len() > 1 {
            return Err(SemanticError::conflict(
                "transaction/cardinality-one-conflict",
                "resulting database has multiple cardinality-one values",
            ));
        }
    }
    let (unique_groups, _) = group_unique_deltas(logical, unique_indices);
    for group in unique_groups {
        if group.value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in uniqueness",
            ));
        }
        if successor_owner_count(
            reader,
            group.attribute,
            group.value,
            group.deltas.as_slice(),
        )? > 1
        {
            return Err(SemanticError::conflict(
                "transaction/unique-conflict",
                "resulting database has multiple holders of a unique value",
            ));
        }
    }
    for entity in excision_entities {
        // Cutoff attributes are ordinary information until this entity
        // actually retains an A=15 excision request in the successor. This
        // matches eager `validate_excision_requests` and avoids rejecting an
        // entity which merely edits or retracts request metadata.
        if successor_values_from_deltas(
            reader,
            entity,
            DB_EXCISE as u32,
            touched_ea
                .get(&(entity, DB_EXCISE as u32))
                .map(Vec::as_slice)
                .unwrap_or(&[]),
        )?
        .is_empty()
        {
            continue;
        }
        let before_t = successor_values_from_deltas(
            reader,
            entity,
            DB_EXCISE_BEFORE_T as u32,
            touched_ea
                .get(&(entity, DB_EXCISE_BEFORE_T as u32))
                .map(Vec::as_slice)
                .unwrap_or(&[]),
        )?;
        let before = successor_values_from_deltas(
            reader,
            entity,
            DB_EXCISE_BEFORE as u32,
            touched_ea
                .get(&(entity, DB_EXCISE_BEFORE as u32))
                .map(Vec::as_slice)
                .unwrap_or(&[]),
        )?;
        if !before_t.is_empty() && !before.is_empty() {
            return Err(SemanticError::incorrect(
                "transaction/excision-cutoff-conflict",
                "an excision request may use at most one cutoff",
            ));
        }
        if let Some(value) = before_t.first() {
            let Value::Long(value) = value else {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/excision-cutoff-type",
                    ":db.excise/beforeT does not contain a long",
                ));
            };
            normalize_excision_before_t(*value)?;
        }
    }
    Ok(())
}

struct UniqueDeltaGroup<'a> {
    attribute: u32,
    value: &'a Value,
    first_index: usize,
    deltas: Vec<&'a LogicalDatom>,
}

fn group_unique_deltas(
    logical: &[LogicalDatom],
    mut indices: Vec<usize>,
) -> (Vec<UniqueDeltaGroup<'_>>, usize) {
    let mut comparisons = 0;
    indices.sort_by(|left, right| {
        count_comparison(&mut comparisons);
        logical[*left]
            .attribute
            .cmp(&logical[*right].attribute)
            .then_with(|| logical[*left].value.index_cmp(&logical[*right].value))
            .then(left.cmp(right))
    });
    let mut groups = Vec::new();
    for indices in equal_groups(&indices, |left, right| {
        count_comparison(&mut comparisons);
        logical[*left].attribute == logical[*right].attribute
            && logical[*left]
                .value
                .index_cmp(&logical[*right].value)
                .is_eq()
    }) {
        groups.push(UniqueDeltaGroup {
            attribute: logical[indices[0]].attribute,
            value: &logical[indices[0]].value,
            first_index: indices[0],
            deltas: indices.iter().map(|index| &logical[*index]).collect(),
        });
    }
    // The former touched-A/V vector validated keys at their first occurrence
    // in canonical logical order. Preserve that failure precedence even
    // though grouping itself uses the source-shaped A/V key order.
    groups.sort_by_key(|group| group.first_index);
    (groups, comparisons)
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
    let deltas = logical
        .iter()
        .filter(|datom| datom.entity == entity && datom.attribute == attribute)
        .collect::<Vec<_>>();
    successor_values_from_deltas(reader, entity, attribute, &deltas)
}

fn successor_values_from_deltas(
    reader: &mut Reader<'_>,
    entity: u64,
    attribute: u32,
    deltas: &[&LogicalDatom],
) -> Result<Vec<Value>, SemanticError> {
    let values = reader.values(entity, attribute)?;
    let mut deltas = deltas.to_vec();
    deltas.sort_by(|left, right| left.value.stored_cmp(&right.value));
    let mut result = Vec::with_capacity(values.len().saturating_add(deltas.len()));
    let (mut value_index, mut delta_index) = (0, 0);
    while value_index < values.len() || delta_index < deltas.len() {
        match (values.get(value_index), deltas.get(delta_index)) {
            (Some(value), Some(delta)) => match value.stored_cmp(&delta.value) {
                Ordering::Less => {
                    result.push(value.clone());
                    value_index += 1;
                }
                Ordering::Equal => {
                    if delta.added {
                        result.push(value.clone());
                    }
                    value_index += 1;
                    delta_index += 1;
                }
                Ordering::Greater => {
                    if delta.added {
                        result.push(delta.value.clone());
                    }
                    delta_index += 1;
                }
            },
            (Some(value), None) => {
                result.push(value.clone());
                value_index += 1;
            }
            (None, Some(delta)) => {
                if delta.added {
                    result.push(delta.value.clone());
                }
                delta_index += 1;
            }
            (None, None) => break,
        }
    }
    Ok(result)
}

fn successor_owner_count(
    reader: &mut Reader<'_>,
    attribute: u32,
    value: &Value,
    deltas: &[&LogicalDatom],
) -> Result<usize, SemanticError> {
    let mut facts = reader.prefix(&IndexPrefix::Avet {
        attribute,
        value: Some(value.clone()),
        entity: None,
    })?;
    facts.sort_by(|left, right| {
        left.entity
            .cmp(&right.entity)
            .then_with(|| left.value.stored_cmp(&right.value))
    });
    let mut counts = BTreeMap::<u64, usize>::new();
    for fact in &facts {
        *counts.entry(fact.entity).or_default() += 1;
    }
    for delta in deltas {
        let present = facts
            .binary_search_by(|fact| {
                fact.entity
                    .cmp(&delta.entity)
                    .then_with(|| fact.value.stored_cmp(&delta.value))
            })
            .is_ok();
        match (delta.added, present) {
            (true, false) => *counts.entry(delta.entity).or_default() += 1,
            (false, true) => {
                if let Some(count) = counts.get_mut(&delta.entity) {
                    *count -= 1;
                }
            }
            _ => {}
        }
    }
    Ok(counts.values().filter(|count| **count != 0).count())
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
    let _ = dedupe_with_work(datoms);
}

fn dedupe_with_work(datoms: &mut Vec<LogicalDatom>) -> usize {
    // Recovered `create-deduper` is a transaction-local HashSet. Values need
    // the native port's stricter stored identity here so legal BigDecimal
    // scale variants do not collapse. Sorting by that same canonical identity
    // gives deterministic O(n log n) behavior without a second value wrapper.
    let mut comparisons = 0;
    datoms.sort_by(|left, right| {
        count_comparison(&mut comparisons);
        compare_logical(left, right)
    });
    datoms.dedup_by(|right, left| {
        count_comparison(&mut comparisons);
        same_logical(left, right)
    });
    comparisons
}

fn validate_same_transaction(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<(), SemanticError> {
    validate_same_transaction_with_work(schema, datoms).map(|_| ())
}

#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd)]
enum SameTransactionConflict {
    Datoms,
    CardinalityOne,
    Unique,
}

#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd)]
struct ConflictCandidate {
    left: usize,
    right: usize,
    kind: SameTransactionConflict,
}

fn validate_same_transaction_with_work(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<usize, SemanticError> {
    // 1.0.7705 uses transaction-local EAV, EA/op and AV keyed maps here
    // (`create-op-validator`, `create-card-one-validator`, and
    // `create-unique-value-validator`). Three deterministic sorted key views
    // are the closest Rust equivalent without requiring Value to implement a
    // single equality relation at both stored and logical-index boundaries.
    let mut descriptor = Vec::with_capacity(datoms.len());
    let mut first_schema_error = None;
    for (index, datom) in datoms.iter().enumerate() {
        match schema.attribute(datom.attribute) {
            Ok(attribute) => descriptor.push(Some(attribute)),
            Err(error) => {
                if first_schema_error.is_none() {
                    first_schema_error = Some((index, error));
                }
                descriptor.push(None);
            }
        }
    }

    let mut comparisons = 0;
    let mut conflict = None;

    let mut exact = (0..datoms.len()).collect::<Vec<_>>();
    exact.sort_by(|left_index, right_index| {
        count_comparison(&mut comparisons);
        let left = &datoms[*left_index];
        let right = &datoms[*right_index];
        left.entity
            .cmp(&right.entity)
            .then(left.attribute.cmp(&right.attribute))
            .then_with(|| left.value.stored_cmp(&right.value))
            .then(left_index.cmp(right_index))
    });
    for group in equal_groups(&exact, |left, right| {
        count_comparison(&mut comparisons);
        let left = &datoms[*left];
        let right = &datoms[*right];
        left.entity == right.entity
            && left.attribute == right.attribute
            && left.value.stored_eq(&right.value)
    }) {
        let first = group[0];
        if let Some(right) = group
            .iter()
            .copied()
            .find(|right| datoms[*right].added != datoms[first].added)
        {
            record_conflict(
                &mut conflict,
                ConflictCandidate {
                    left: first.min(right),
                    right: first.max(right),
                    kind: SameTransactionConflict::Datoms,
                },
            );
        }
    }

    let mut cardinality_one = (0..datoms.len())
        .filter(|index| {
            datoms[*index].added
                && descriptor[*index]
                    .is_some_and(|attribute| attribute.cardinality == Cardinality::One)
        })
        .collect::<Vec<_>>();
    cardinality_one.sort_by_key(|index| {
        let datom = &datoms[*index];
        (datom.entity, datom.attribute, *index)
    });
    for group in equal_groups(&cardinality_one, |left, right| {
        count_comparison(&mut comparisons);
        datoms[*left].entity == datoms[*right].entity
            && datoms[*left].attribute == datoms[*right].attribute
    }) {
        let first = group[0];
        if let Some(right) = group
            .iter()
            .copied()
            .find(|right| datoms[first].value.index_cmp(&datoms[*right].value).is_ne())
        {
            record_conflict(
                &mut conflict,
                ConflictCandidate {
                    left: first,
                    right,
                    kind: SameTransactionConflict::CardinalityOne,
                },
            );
        }
    }

    let mut unique = (0..datoms.len())
        .filter(|index| {
            datoms[*index].added
                && descriptor[*index].is_some_and(|attribute| attribute.unique.is_some())
        })
        .collect::<Vec<_>>();
    unique.sort_by(|left, right| {
        count_comparison(&mut comparisons);
        let left_datom = &datoms[*left];
        let right_datom = &datoms[*right];
        left_datom
            .attribute
            .cmp(&right_datom.attribute)
            .then_with(|| left_datom.value.index_cmp(&right_datom.value))
            .then(left.cmp(right))
    });
    for group in equal_groups(&unique, |left, right| {
        count_comparison(&mut comparisons);
        datoms[*left].attribute == datoms[*right].attribute
            && datoms[*left].value.index_cmp(&datoms[*right].value).is_eq()
    }) {
        let first = group[0];
        if let Some(right) = group
            .iter()
            .copied()
            .find(|right| datoms[first].entity != datoms[*right].entity)
        {
            record_conflict(
                &mut conflict,
                ConflictCandidate {
                    left: first,
                    right,
                    kind: SameTransactionConflict::Unique,
                },
            );
        }
    }

    if let Some((index, error)) = first_schema_error
        && conflict.is_none_or(|conflict| index <= conflict.left)
    {
        return Err(error);
    }
    match conflict.map(|conflict| conflict.kind) {
        Some(SameTransactionConflict::Datoms) => Err(SemanticError::conflict(
            "transaction/datoms-conflict",
            "addition and retraction of the same E/A/V conflict",
        )),
        Some(SameTransactionConflict::CardinalityOne) => Err(SemanticError::conflict(
            "transaction/cardinality-one-conflict",
            "two values for one cardinality-one E/A conflict",
        )),
        Some(SameTransactionConflict::Unique) => Err(SemanticError::conflict(
            "transaction/unique-conflict",
            "two entities assert the same unique A/V",
        )),
        None => Ok(comparisons),
    }
}

fn equal_groups<T>(
    sorted: &[T],
    mut equivalent: impl FnMut(&T, &T) -> bool,
) -> impl Iterator<Item = &[T]> {
    let mut start = 0;
    std::iter::from_fn(move || {
        if start == sorted.len() {
            return None;
        }
        let mut end = start + 1;
        while end < sorted.len() && equivalent(&sorted[start], &sorted[end]) {
            end += 1;
        }
        let group = &sorted[start..end];
        start = end;
        Some(group)
    })
}

fn record_conflict(current: &mut Option<ConflictCandidate>, candidate: ConflictCandidate) {
    if current.is_none_or(|current| candidate < current) {
        *current = Some(candidate);
    }
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
#[path = "tiered_assessor_scaling_tests.rs"]
mod scaling_tests;

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
        assert!(assessed.read_work.prefix_hits > 0);
        assert!(assessed.read_work.datoms > 0);
    }

    #[test]
    fn identical_prefix_reads_hit_the_transaction_local_memo_without_recharging_work() {
        let initial = Database::new(schema()).unwrap();
        let seeded = initial.with(&add("one", 1), 10).unwrap().db_after;
        let value = DatabaseValue::eager(Arc::new(seeded));
        let prefix = IndexPrefix::Avet {
            attribute: NAME,
            value: Some(Value::String("one".into())),
            entity: None,
        };
        let mut reader = Reader::new(
            &value,
            AssessmentLimits {
                max_read_datoms: 1,
                max_read_bytes: u64::MAX,
            },
        );

        let first = reader.prefix(&prefix).unwrap();
        assert_eq!(first.len(), 1);
        let charged = reader.work;
        assert_eq!(charged.prefixes, 1);
        assert_eq!(charged.prefix_hits, 0);
        assert_eq!(charged.datoms, 1);
        assert!(charged.retained_bytes > 0);

        // A second physical scan would exceed the one-datom budget. The exact
        // same result instead comes from the assessment-local memo and leaves
        // all I/O/read-capacity counters unchanged except the hit witness.
        let second = reader.prefix(&prefix).unwrap();
        assert_eq!(second, first);
        assert_eq!(reader.work.prefixes, charged.prefixes);
        assert_eq!(reader.work.datoms, charged.datoms);
        assert_eq!(reader.work.retained_bytes, charged.retained_bytes);
        assert_eq!(reader.work.prefix_hits, 1);
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
    fn unique_enablement_uses_history_and_matches_the_eager_oracle() {
        let initial = Database::new(schema()).unwrap();
        let mut unique_empty = initial.schema().attribute(COUNT).unwrap().clone();
        unique_empty.unique = Some(Unique::Identity);
        let empty_ops = vec![TxOp::AlterAttribute(unique_empty)];
        let eager = initial.with(&empty_ops, 10).unwrap();
        let tiered = assess_tiered(
            &DatabaseValue::eager(Arc::new(initial.clone())),
            &empty_ops,
            10,
        )
        .unwrap();
        assert_eq!(tiered.tx_data, eager.tx_data);

        let asserted = initial.with(&add("past", 1), 10).unwrap();
        let entity = asserted.tempids["item"];
        let historical = asserted
            .db_after
            .with(
                &[TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: COUNT,
                    value: Some(TxValue::Scalar(Value::Long(1))),
                }],
                11,
            )
            .unwrap()
            .db_after;
        assert!(historical.values(entity, COUNT).is_empty());
        let mut unique_historical = historical.schema().attribute(COUNT).unwrap().clone();
        unique_historical.unique = Some(Unique::Identity);
        let history_ops = vec![TxOp::AlterAttribute(unique_historical)];
        let eager = historical.with(&history_ops, 12).unwrap_err();
        let tiered = assess_tiered(
            &DatabaseValue::eager(Arc::new(historical)),
            &history_ops,
            12,
        )
        .unwrap_err();
        assert_eq!(eager.code, "schema/unique-requires-avet");
        assert_eq!(tiered.category, eager.category);
        assert_eq!(tiered.code, eager.code);
    }

    #[test]
    fn excision_cutoff_domain_and_request_scope_match_the_eager_oracle() {
        fn request(before_t: i64) -> Vec<TxOp> {
            vec![
                TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE as u32,
                    value: TxValue::Entity(EntityRef::Id(crate::DB_FULLTEXT)),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: DB_EXCISE_BEFORE_T as u32,
                    value: TxValue::Scalar(Value::Long(before_t)),
                },
            ]
        }

        fn assert_same_outcome(ops: &[TxOp]) {
            let database = Database::bootstrap().unwrap();
            let eager = database.with(ops, 10);
            let tiered = assess_tiered(&DatabaseValue::eager(Arc::new(database)), ops, 10);
            match (eager, tiered) {
                (Ok(eager), Ok(tiered)) => assert_eq!(tiered.tx_data, eager.tx_data),
                (Err(eager), Err(tiered)) => {
                    assert_eq!(tiered.category, eager.category);
                    assert_eq!(tiered.code, eager.code);
                }
                outcomes => panic!("eager/tiered excision divergence: {outcomes:?}"),
            }
        }

        for invalid in [
            -1,
            i64::try_from(make_eid(USER_PARTITION, 1).unwrap()).unwrap(),
        ] {
            let ops = request(invalid);
            assert_same_outcome(&ops);
            assert_eq!(
                Database::bootstrap()
                    .unwrap()
                    .with(&ops, 10)
                    .unwrap_err()
                    .code,
                "transaction/excision-before-t"
            );
        }
        assert_same_outcome(&request(1));
        assert_same_outcome(&request(i64::try_from(t_to_tx(1).unwrap()).unwrap()));

        let both = vec![
            TxOp::Add {
                entity: EntityRef::Temp("request".into()),
                attribute: DB_EXCISE as u32,
                value: TxValue::Entity(EntityRef::Id(crate::DB_FULLTEXT)),
            },
            TxOp::Add {
                entity: EntityRef::Temp("request".into()),
                attribute: DB_EXCISE_BEFORE_T as u32,
                value: TxValue::Scalar(Value::Long(-1)),
            },
            TxOp::Add {
                entity: EntityRef::Temp("request".into()),
                attribute: DB_EXCISE_BEFORE as u32,
                value: TxValue::Scalar(Value::Instant(1)),
            },
        ];
        assert_same_outcome(&both);
        assert_eq!(
            Database::bootstrap()
                .unwrap()
                .with(&both, 10)
                .unwrap_err()
                .code,
            "transaction/excision-cutoff-conflict"
        );

        // A cutoff-shaped fact is not an excision request by itself. Even an
        // otherwise invalid cutoff remains ordinary data until A=15 survives
        // in the same successor entity.
        let metadata_only = vec![TxOp::Add {
            entity: EntityRef::Temp("metadata".into()),
            attribute: DB_EXCISE_BEFORE_T as u32,
            value: TxValue::Scalar(Value::Long(-1)),
        }];
        assert_same_outcome(&metadata_only);
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

        let zero_read = assess_tiered_with_remaining_limits(
            &DatabaseValue::eager(Arc::new(initial)),
            &[],
            10,
            AssessmentLimits {
                max_read_datoms: 0,
                max_read_bytes: 0,
            },
        )
        .unwrap();
        assert_eq!(zero_read.read_work.datoms, 0);
        assert_eq!(zero_read.read_work.retained_bytes, 0);

        let seeded = Database::new(schema())
            .unwrap()
            .with(&add("one", 1), 10)
            .unwrap()
            .db_after;
        let entity = seeded
            .lookup(NAME, &Value::String("one".into()))
            .unwrap()
            .unwrap();
        let exhausted = assess_tiered_with_remaining_limits(
            &DatabaseValue::eager(Arc::new(seeded)),
            &[TxOp::RetractEntity(EntityRef::Id(entity))],
            11,
            AssessmentLimits {
                max_read_datoms: 0,
                max_read_bytes: 0,
            },
        )
        .unwrap_err();
        assert_eq!(exhausted.category, ErrorCategory::Busy);
        assert_eq!(exhausted.code, "transaction/read-capacity");
    }

    #[test]
    fn predicate_requirements_union_roles_for_one_symbol() {
        let mut required = BTreeMap::new();
        insert_predicate_role(
            &mut required,
            "test.predicates/shared",
            crate::database::PredicateRole::Attribute,
        )
        .unwrap();
        insert_predicate_role(
            &mut required,
            "test.predicates/shared",
            crate::database::PredicateRole::Entity,
        )
        .unwrap();
        assert_eq!(
            required["test.predicates/shared"],
            crate::database::PredicateRole::Both
        );
    }
}
