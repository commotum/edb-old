use crate::index::IndexRoots;
use crate::{
    Cardinality, Datom, ErrorCategory, IndexOrder, IndexPrefix, Schema, SemanticError, TupleSpec,
    Unique, Value, ValueType,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::fmt;
use std::sync::Arc;

#[derive(Clone, Debug)]
pub enum EntityRef {
    Id(u64),
    Ident(crate::Keyword),
    Temp(String),
    Lookup { attribute: u32, value: Value },
    Tx,
}

#[derive(Clone, Debug)]
pub enum TxValue {
    Scalar(Value),
    Entity(EntityRef),
}

impl From<Value> for TxValue {
    fn from(value: Value) -> Self {
        Self::Scalar(value)
    }
}

#[derive(Clone, Debug)]
pub enum TxOp {
    Add {
        entity: EntityRef,
        attribute: u32,
        value: TxValue,
    },
    Retract {
        entity: EntityRef,
        attribute: u32,
        value: Option<TxValue>,
    },
    Cas {
        entity: EntityRef,
        attribute: u32,
        old: Option<TxValue>,
        new: TxValue,
    },
    RetractEntity(EntityRef),
    Ensure {
        entity: EntityRef,
        required: Vec<u32>,
    },
    /// Native normalized form of an ordinary schema installation transaction.
    InstallAttribute(crate::Attribute),
    /// Native normalized form of an ordinary schema alteration transaction.
    AlterAttribute(crate::Attribute),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum SchemaChange {
    Install(crate::Attribute),
    Alter(crate::Attribute),
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum View {
    Current,
    AsOf(u64),
    Since(u64),
    History,
}

type FilterPredicate = dyn Fn(&Database, &Datom) -> bool + Send + Sync;

/// A composable view over one immutable database value.
///
/// Predicates are applied to historical information before point-in-time
/// retraction collapse, matching the recovered `filter-index` flow. That is
/// what lets a filter remove an entire bad transaction and reveal the prior
/// value rather than merely hiding the current assertion.
#[derive(Clone)]
pub struct DatabaseView<'a> {
    database: &'a Database,
    after_t: Option<u64>,
    through_t: Option<u64>,
    history: bool,
    predicates: Vec<Arc<FilterPredicate>>,
}

impl fmt::Debug for DatabaseView<'_> {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("DatabaseView")
            .field("after_t", &self.after_t)
            .field("through_t", &self.through_t)
            .field("history", &self.history)
            .field("predicate_count", &self.predicates.len())
            .finish()
    }
}

#[derive(Clone, Debug)]
pub struct TxReport {
    pub db_before: Database,
    pub db_after: Database,
    pub tx_data: Vec<Datom>,
    pub tempids: BTreeMap<String, u64>,
    pub schema_changes: Vec<SchemaChange>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct CurrentFact {
    entity: u64,
    attribute: u32,
    value: Value,
    tx: u64,
}

#[derive(Clone, Debug)]
struct LogicalDatom {
    entity: u64,
    attribute: u32,
    value: Value,
    added: bool,
}

#[derive(Clone, Debug)]
struct EnsureCheck {
    entity: u64,
    required: Vec<u32>,
}

/// Immutable, single-process database value.
///
/// The transaction assessor deliberately remains straightforward and is the
/// semantic oracle inherited from Goal 1. Observable reads use immutable
/// Datomic-shaped index roots; history is chunked by transaction so successor
/// values structurally share all prior chunks.
#[derive(Clone, Debug)]
pub struct Database {
    schema: Arc<Schema>,
    basis_t: u64,
    next_eid: u64,
    last_tx_instant: Option<i64>,
    tx_instant_attribute: Option<u32>,
    current: Arc<[CurrentFact]>,
    history: Arc<[Arc<[Datom]>]>,
    schema_history: Arc<[Arc<[SchemaChange]>]>,
    current_indexes: IndexRoots,
    history_indexes: IndexRoots,
}

impl Database {
    pub fn new(schema: Schema) -> Result<Self, SemanticError> {
        let tx_instant_attribute = schema
            .resolve_ident(&crate::Keyword::new("db", "txInstant"))
            .ok_or_else(|| {
                SemanticError::incorrect(
                    "schema/missing-tx-instant",
                    "a database schema must contain :db/txInstant",
                )
            })?;
        let tx_instant = schema.attribute(tx_instant_attribute)?;
        if tx_instant.value_type != ValueType::Instant || tx_instant.cardinality != Cardinality::One
        {
            return Err(SemanticError::incorrect(
                "schema/invalid-tx-instant-attribute",
                ":db/txInstant must be a cardinality-one instant",
            ));
        }
        Ok(Self {
            schema: Arc::new(schema),
            basis_t: 0,
            next_eid: 1_000,
            last_tx_instant: None,
            tx_instant_attribute: Some(tx_instant_attribute),
            current: Arc::default(),
            history: Arc::default(),
            schema_history: Arc::default(),
            current_indexes: IndexRoots::default(),
            history_indexes: IndexRoots::default(),
        })
    }

    pub fn with_tx_instant_attribute(mut self, attribute: u32) -> Result<Self, SemanticError> {
        let schema = self.schema.attribute(attribute)?;
        if schema.value_type != ValueType::Instant || schema.cardinality != Cardinality::One {
            return Err(SemanticError::incorrect(
                "schema/invalid-tx-instant-attribute",
                "transaction instant attribute must be cardinality-one instant",
            ));
        }
        self.tx_instant_attribute = Some(attribute);
        Ok(self)
    }

    pub fn schema(&self) -> &Schema {
        &self.schema
    }

    /// Schema information transacted after database construction, grouped by
    /// transaction. The initial `Schema` is the native bootstrap input.
    pub fn schema_changes(&self) -> impl Iterator<Item = (u64, &SchemaChange)> {
        self.schema_history
            .iter()
            .enumerate()
            .flat_map(|(offset, chunk)| {
                let tx = offset as u64 + 1;
                chunk.iter().map(move |change| (tx, change))
            })
    }

    pub fn basis_t(&self) -> u64 {
        self.basis_t
    }

    pub fn next_eid(&self) -> u64 {
        self.next_eid
    }

    /// Reconstitute a verified immutable value from a persistent index base.
    ///
    /// This is intentionally crate-private: durable manifests are the only
    /// caller, and all ordinary state transitions continue through `with` or
    /// `apply_committed`.
    pub(crate) fn from_index_base(
        schema: Schema,
        basis_t: u64,
        next_eid: u64,
        current_datoms: Vec<Datom>,
        history_chunks: Vec<Vec<Datom>>,
        schema_chunks: Vec<Vec<SchemaChange>>,
    ) -> Result<Self, SemanticError> {
        if basis_t == 0
            || history_chunks.len() != usize::try_from(basis_t).unwrap_or(usize::MAX)
            || schema_chunks.len() != usize::try_from(basis_t).unwrap_or(usize::MAX)
        {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "index/base-basis-mismatch",
                "index base must contain one history and schema chunk per positive basis",
            ));
        }
        let tx_instant_attribute = schema
            .resolve_ident(&crate::Keyword::new("db", "txInstant"))
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "index/missing-tx-instant",
                    "index base schema has no :db/txInstant",
                )
            })?;
        let mut current = Vec::with_capacity(current_datoms.len());
        for datom in &current_datoms {
            if !datom.added || datom.tx == 0 || datom.tx > basis_t {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "index/invalid-current-datom",
                    "current index base contains a retraction or invalid transaction",
                ));
            }
            schema.validate_value(schema.attribute(datom.attribute)?, &datom.value)?;
            current.push(CurrentFact {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                tx: datom.tx,
            });
        }
        current.sort_by(compare_current);
        if current
            .windows(2)
            .any(|pair| compare_current(&pair[0], &pair[1]).is_eq())
        {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "index/duplicate-current-datom",
                "current index base contains duplicate facts",
            ));
        }
        let last_tx_instant = history_chunks
            .last()
            .and_then(|chunk| {
                chunk.iter().find_map(|datom| {
                    (datom.entity == basis_t
                        && datom.attribute == tx_instant_attribute
                        && datom.added)
                        .then(|| match datom.value {
                            Value::Instant(value) => Some(value),
                            _ => None,
                        })
                        .flatten()
                })
            })
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "index/invalid-last-tx-instant",
                    "index base lacks its final transaction instant",
                )
            })?;
        let history: Vec<Arc<[Datom]>> = history_chunks.into_iter().map(Arc::from).collect();
        let schema_history: Vec<Arc<[SchemaChange]>> =
            schema_chunks.into_iter().map(Arc::from).collect();
        let current_datoms = facts_as_datoms(&current);
        let mut database = Self {
            schema: Arc::new(schema),
            basis_t,
            next_eid,
            last_tx_instant: Some(last_tx_instant),
            tx_instant_attribute: Some(tx_instant_attribute),
            current: current.into(),
            history: history.into(),
            schema_history: schema_history.into(),
            current_indexes: IndexRoots::default(),
            history_indexes: IndexRoots::default(),
        };
        database.current_indexes = IndexRoots::build(&database.schema, current_datoms);
        database.history_indexes =
            IndexRoots::build(&database.schema, database.history_datoms().cloned());
        database.validate_invariants()?;
        Ok(database)
    }

    /// Greatest transaction t whose transaction instant is at or before the
    /// supplied millisecond instant, or zero when the instant predates the DB.
    pub fn t_at_or_before_instant(&self, instant: i64) -> u64 {
        let Some(attribute) = self.tx_instant_attribute else {
            return 0;
        };
        self.current_indexes
            .matching(&IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            })
            .expect("internally constructed index prefix is valid")
            .iter()
            .filter_map(|datom| match datom.value {
                Value::Instant(value) if value <= instant => Some(datom.tx),
                _ => None,
            })
            .max()
            .unwrap_or(0)
    }

    /// Least transaction t whose transaction instant is at or after the
    /// supplied instant, or the next t when the instant is beyond this DB.
    pub fn t_at_or_after_instant(&self, instant: i64) -> u64 {
        let Some(attribute) = self.tx_instant_attribute else {
            return self.basis_t + 1;
        };
        self.current_indexes
            .matching(&IndexPrefix::Aevt {
                attribute,
                entity: None,
                value: None,
            })
            .expect("internally constructed index prefix is valid")
            .iter()
            .filter_map(|datom| match datom.value {
                Value::Instant(value) if value >= instant => Some(datom.tx),
                _ => None,
            })
            .min()
            .unwrap_or(self.basis_t + 1)
    }

    pub fn as_of_instant(&self, instant: i64) -> DatabaseView<'_> {
        self.view(View::AsOf(self.t_at_or_before_instant(instant)))
    }

    pub fn view(&self, view: View) -> DatabaseView<'_> {
        let (after_t, through_t, history) = match view {
            View::Current => (None, Some(self.basis_t), false),
            View::AsOf(t) => (None, Some(t.min(self.basis_t)), false),
            View::Since(t) => (Some(t), Some(self.basis_t), false),
            View::History => (None, Some(self.basis_t), true),
        };
        DatabaseView {
            database: self,
            after_t,
            through_t,
            history,
            predicates: Vec::new(),
        }
    }

    pub fn datoms(&self, view: View, order: IndexOrder) -> Vec<Datom> {
        match view {
            View::Current => self.current_indexes.get(order).to_vec(),
            View::History => self.history_indexes.get(order).to_vec(),
            View::AsOf(t) => {
                let facts = replay(self.history_datoms().filter(|d| d.tx <= t));
                IndexRoots::build(&self.schema, facts_as_datoms(&facts))
                    .get(order)
                    .to_vec()
            }
            View::Since(t) => {
                let facts = replay(self.history_datoms().filter(|d| d.tx > t));
                IndexRoots::build(&self.schema, facts_as_datoms(&facts))
                    .get(order)
                    .to_vec()
            }
        }
    }

    pub fn values(&self, entity: u64, attribute: u32) -> Vec<&Value> {
        self.current_indexes
            .matching(&IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: None,
            })
            .expect("internally constructed index prefix is valid")
            .iter()
            .map(|datom| &datom.value)
            .collect()
    }

    /// Exact current-database matches for a left-contiguous index prefix.
    pub fn datoms_with_prefix(&self, prefix: &IndexPrefix) -> Result<&[Datom], SemanticError> {
        self.current_indexes.matching(prefix)
    }

    /// Current-database datoms at or after a left-contiguous index prefix.
    pub fn seek_datoms(&self, prefix: &IndexPrefix) -> Result<&[Datom], SemanticError> {
        self.current_indexes.seek(prefix)
    }

    /// Current datoms at or before a left-contiguous prefix, in reverse index
    /// order. The owned result keeps the public API independent of a future
    /// segmented reverse cursor.
    pub fn reverse_seek_datoms(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        self.current_indexes.reverse_seek(prefix)
    }

    /// Historical assertions and retractions matching an index prefix.
    pub fn history_with_prefix(&self, prefix: &IndexPrefix) -> Result<&[Datom], SemanticError> {
        self.history_indexes.matching(prefix)
    }

    /// Half-open AVET value range for one indexed attribute.
    pub fn avet_range(
        &self,
        attribute: u32,
        start: Option<&Value>,
        end: Option<&Value>,
    ) -> Result<&[Datom], SemanticError> {
        let schema = self.schema.attribute(attribute)?;
        if !(schema.indexed || schema.unique.is_some()) {
            return Err(SemanticError::incorrect(
                "index/attribute-not-in-avet",
                format!(
                    "attribute {} is not present in AVET",
                    schema.ident.qualified_name()
                ),
            ));
        }
        if start
            .zip(end)
            .is_some_and(|(start, end)| start.index_cmp(end).is_gt())
        {
            return Err(SemanticError::incorrect(
                "index/invalid-range",
                "AVET range start must not be greater than its end",
            ));
        }
        Ok(self.current_indexes.avet_range(attribute, start, end))
    }

    fn history_datoms(&self) -> impl Iterator<Item = &Datom> {
        self.history.iter().flat_map(|chunk| chunk.iter())
    }

    /// Cross-check all immutable roots and post-state invariants against the
    /// retained straightforward fact representation.
    pub fn validate_invariants(&self) -> Result<(), SemanticError> {
        let current = facts_as_datoms(&self.current);
        let expected_current = IndexRoots::build(&self.schema, current);
        let history: Vec<_> = self.history_datoms().cloned().collect();
        let expected_history = IndexRoots::build(&self.schema, history);
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            if self.current_indexes.get(order) != expected_current.get(order) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/current-index-divergence",
                    format!("current {order:?} root diverged from facts"),
                ));
            }
            if self.history_indexes.get(order) != expected_history.get(order) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/history-index-divergence",
                    format!("history {order:?} root diverged from transaction chunks"),
                ));
            }
        }
        validate_cardinality(&self.schema, &self.current)?;
        validate_uniqueness(&self.schema, &self.current)?;
        let mut replayed = replay(self.history_datoms());
        replayed.sort_by(compare_current);
        if replayed.as_slice() != self.current.as_ref() {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/history-current-divergence",
                "replaying transaction history does not reproduce current facts",
            ));
        }
        if self.history.len() != self.basis_t as usize {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/history-basis-divergence",
                "transaction history chunk count does not match database basis",
            ));
        }
        if self.schema_history.len() != self.basis_t as usize {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/schema-history-basis-divergence",
                "schema history chunk count does not match database basis",
            ));
        }
        if let Some(tx_instant_attribute) = self.tx_instant_attribute {
            for (offset, chunk) in self.history.iter().enumerate() {
                let tx = offset as u64 + 1;
                if chunk.iter().any(|datom| datom.tx != tx)
                    || chunk
                        .iter()
                        .filter(|datom| {
                            datom.entity == tx
                                && datom.attribute == tx_instant_attribute
                                && datom.added
                                && matches!(datom.value, Value::Instant(_))
                        })
                        .count()
                        != 1
                {
                    return Err(SemanticError::new(
                        ErrorCategory::Fault,
                        "kernel/invalid-transaction-chunk",
                        format!("history chunk for transaction {tx} is malformed"),
                    ));
                }
            }
        }
        if self.history_datoms().any(|datom| datom.tx > self.basis_t) {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/future-datom",
                "history contains a datom beyond the database basis",
            ));
        }
        Ok(())
    }

    /// Validate the observable invariants of replacing one installed schema
    /// descriptor. Actual schema changes remain transactions in the production
    /// kernel; this helper exists so foundation fixtures can judge a proposed
    /// transition without introducing a second mutation path.
    pub fn validate_schema_change(&self, proposed: &crate::Attribute) -> Result<(), SemanticError> {
        let current = self.schema.attribute(proposed.id)?;
        self.schema.validate_attribute(proposed)?;
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
        if current.cardinality == Cardinality::Many && proposed.cardinality == Cardinality::One {
            let mut counts = BTreeMap::<u64, usize>::new();
            for fact in self
                .current
                .iter()
                .filter(|fact| fact.attribute == proposed.id)
            {
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
                    "adding uniqueness in the Pro model requires an existing AVET index",
                ));
            }
            validate_uniqueness_for_attribute(&self.current, proposed.id)?;
        }
        Ok(())
    }

    pub fn lookup(&self, attribute: u32, value: &Value) -> Result<Option<u64>, SemanticError> {
        let schema = self.schema.attribute(attribute)?;
        if schema.unique.is_none() {
            return Err(SemanticError::incorrect(
                "transaction/lookup-non-unique",
                format!("attribute {} is not unique", schema.ident.qualified_name()),
            ));
        }
        Ok(self
            .current_indexes
            .matching(&IndexPrefix::Avet {
                attribute,
                value: Some(value.clone()),
                entity: None,
            })?
            .first()
            .map(|datom| datom.entity))
    }

    /// Pure transaction application. `tx_instant` is supplied by the caller so
    /// the same inputs always produce the same result.
    pub fn with(&self, ops: &[TxOp], tx_instant: i64) -> Result<TxReport, SemanticError> {
        self.with_context(ops, None, tx_instant)
    }

    /// Rebuild one already-assessed committed successor from its material
    /// transaction record. Persistence uses this path during recovery; it
    /// deliberately does not rerun transaction functions, tempid resolution,
    /// or any other request-time behavior.
    pub(crate) fn apply_committed(
        &self,
        transaction: &crate::DurableTransaction,
    ) -> Result<Self, SemanticError> {
        let tx = self.basis_t.checked_add(1).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/basis-overflow",
                "database basis cannot advance beyond u64",
            )
        })?;
        if transaction.basis_t != tx {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/noncontiguous-basis",
                format!(
                    "expected transaction basis {tx}, got {}",
                    transaction.basis_t
                ),
            ));
        }

        let expected_next_eid = transaction
            .tempids
            .values()
            .copied()
            .filter(|entity| *entity >= self.next_eid)
            .max()
            .map_or(Ok(self.next_eid), |entity| {
                entity.checked_add(1).ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::Fault,
                        "recovery/entity-id-overflow",
                        "committed tempid allocation overflows u64",
                    )
                })
            })?;
        if transaction.next_eid != expected_next_eid {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/next-eid-mismatch",
                format!(
                    "expected next entity id {expected_next_eid}, got {}",
                    transaction.next_eid
                ),
            ));
        }

        let tx_instant_attribute = self.tx_instant_attribute.ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/missing-tx-instant",
                "database has no transaction instant attribute",
            )
        })?;
        let instants: Vec<_> = transaction
            .tx_data
            .iter()
            .filter_map(|datom| {
                (datom.entity == tx && datom.attribute == tx_instant_attribute && datom.added)
                    .then_some(&datom.value)
            })
            .collect();
        let tx_instant = match instants.as_slice() {
            [Value::Instant(instant)] => *instant,
            _ => {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/invalid-tx-instant",
                    "committed transaction must contain exactly one own transaction instant",
                ));
            }
        };
        if self
            .last_tx_instant
            .is_some_and(|previous| tx_instant < previous)
        {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/non-monotonic-instant",
                "committed transaction instant precedes its predecessor",
            ));
        }

        let schema_ops: Vec<_> = transaction
            .schema_changes
            .iter()
            .map(|change| match change {
                SchemaChange::Install(attribute) => TxOp::InstallAttribute(attribute.clone()),
                SchemaChange::Alter(attribute) => TxOp::AlterAttribute(attribute.clone()),
            })
            .collect();
        let (successor_schema, prepared_changes) = self.prepare_schema_changes(&schema_ops)?;
        if prepared_changes != transaction.schema_changes {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/noncanonical-schema-changes",
                "committed schema changes are duplicated or out of canonical order",
            ));
        }

        let mut logical = Vec::with_capacity(transaction.tx_data.len());
        for datom in &transaction.tx_data {
            if datom.tx != tx {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/datom-transaction-mismatch",
                    "committed datom does not name its enclosing transaction",
                ));
            }
            let attribute = self.schema.attribute(datom.attribute)?;
            self.schema.validate_value(attribute, &datom.value)?;
            let existed = contains_fact(&self.current, datom.entity, datom.attribute, &datom.value);
            if existed == datom.added {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/nonmaterial-datom",
                    "committed datom is not a material change from db-before",
                ));
            }
            if logical.iter().any(|prior: &LogicalDatom| {
                prior.entity == datom.entity
                    && prior.attribute == datom.attribute
                    && prior.value.stored_eq(&datom.value)
            }) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/duplicate-datom",
                    "committed transaction contains duplicate or contradictory datoms",
                ));
            }
            logical.push(LogicalDatom {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                added: datom.added,
            });
        }

        let mut final_current = apply_logical(&self.current, &logical, tx);
        validate_cardinality(&successor_schema, &final_current)?;
        validate_uniqueness(&successor_schema, &final_current)?;
        final_current.sort_by(compare_current);

        let mut db_after = self.clone();
        db_after.schema = successor_schema;
        db_after.basis_t = tx;
        db_after.next_eid = transaction.next_eid;
        db_after.last_tx_instant = Some(tx_instant);
        let current_datoms = facts_as_datoms(&final_current);
        db_after.current = final_current.into();
        let mut history_chunks: Vec<_> = self.history.iter().cloned().collect();
        history_chunks.push(Arc::from(transaction.tx_data.clone()));
        db_after.history = history_chunks.into();
        let mut schema_chunks: Vec<_> = self.schema_history.iter().cloned().collect();
        schema_chunks.push(Arc::from(transaction.schema_changes.clone()));
        db_after.schema_history = schema_chunks.into();
        db_after.current_indexes = IndexRoots::build(&db_after.schema, current_datoms);
        db_after.history_indexes =
            IndexRoots::build(&db_after.schema, db_after.history_datoms().cloned());
        db_after.validate_invariants()?;
        Ok(db_after)
    }

    pub(crate) fn with_function_context(
        &self,
        ops: &[TxOp],
        functions: &crate::TxFunctions,
        tx_instant: i64,
    ) -> Result<TxReport, SemanticError> {
        self.with_context(ops, Some(functions), tx_instant)
    }

    fn with_context(
        &self,
        ops: &[TxOp],
        functions: Option<&crate::TxFunctions>,
        tx_instant: i64,
    ) -> Result<TxReport, SemanticError> {
        if let Some(previous) = self.last_tx_instant
            && tx_instant < previous
        {
            return Err(SemanticError::incorrect(
                "transaction/non-monotonic-instant",
                format!("transaction instant {tx_instant} precedes {previous}"),
            ));
        }

        let tx = self.basis_t + 1;
        let mut ordered_ops = ops.to_vec();
        ordered_ops.sort_by_key(|op| format!("{op:?}"));

        // Schema changes are part of this transaction's information set, but
        // all ordinary attribute resolution below deliberately uses db-before.
        // This preserves `ProcessInpoint`/`require-attr` timing: a newly
        // installed attribute becomes usable only in the next transaction.
        let (successor_schema, schema_changes) = self.prepare_schema_changes(&ordered_ops)?;

        let tempids = self.resolve_tempids(&ordered_ops)?;
        let mut logical = Vec::new();
        let mut ensures = Vec::new();
        let mut touched_constituents = BTreeSet::new();

        for op in &ordered_ops {
            self.expand_op(
                op,
                tx,
                &tempids,
                &mut logical,
                &mut ensures,
                &mut touched_constituents,
            )?;
        }

        dedupe(&mut logical);
        validate_same_transaction(&self.schema, &logical)?;
        add_cardinality_one_retractions(&self.schema, &self.current, &mut logical)?;
        dedupe(&mut logical);
        validate_same_transaction(&self.schema, &logical)?;

        let provisional = apply_logical(&self.current, &logical, tx);
        let composites = derive_composites(
            &self.schema,
            &self.current,
            &provisional,
            &touched_constituents,
        )?;
        logical.extend(composites);

        if let Some(attribute) = self.tx_instant_attribute {
            logical.push(LogicalDatom {
                entity: tx,
                attribute,
                value: Value::Instant(tx_instant),
                added: true,
            });
        }

        dedupe(&mut logical);
        validate_same_transaction(&self.schema, &logical)?;
        self.validate_attribute_predicates(&logical, functions)?;

        let mut final_current = apply_logical(&self.current, &logical, tx);
        validate_cardinality(&self.schema, &final_current)?;
        validate_uniqueness(&self.schema, &final_current)?;
        validate_ensures(&final_current, &ensures)?;

        let mut tx_data = material_changes(&self.current, &final_current, &logical, tx);
        tx_data.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        final_current.sort_by(compare_current);

        let mut db_after = self.clone();
        db_after.basis_t = tx;
        db_after.last_tx_instant = Some(tx_instant);
        db_after.schema = successor_schema;
        db_after.next_eid = tempids
            .values()
            .copied()
            .filter(|id| *id >= self.next_eid)
            .max()
            .map_or(self.next_eid, |id| id + 1);
        let current_datoms = facts_as_datoms(&final_current);
        db_after.current = final_current.into();
        let mut history_chunks: Vec<_> = self.history.iter().cloned().collect();
        history_chunks.push(Arc::from(tx_data.clone()));
        db_after.history = history_chunks.into();
        let mut schema_chunks: Vec<_> = self.schema_history.iter().cloned().collect();
        schema_chunks.push(Arc::from(schema_changes.clone()));
        db_after.schema_history = schema_chunks.into();
        db_after.current_indexes = IndexRoots::build(&db_after.schema, current_datoms);
        db_after.history_indexes =
            IndexRoots::build(&db_after.schema, db_after.history_datoms().cloned());

        Ok(TxReport {
            db_before: self.clone(),
            db_after,
            tx_data,
            tempids,
            schema_changes,
        })
    }

    fn validate_attribute_predicates(
        &self,
        logical: &[LogicalDatom],
        functions: Option<&crate::TxFunctions>,
    ) -> Result<(), SemanticError> {
        for datom in logical.iter().filter(|datom| datom.added) {
            let attribute = self.schema.attribute(datom.attribute)?;
            for predicate in &attribute.predicates {
                let accepted = functions
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
                if !accepted {
                    return Err(SemanticError::incorrect(
                        "transaction/attribute-predicate",
                        format!(
                            "entity {} attribute {} value failed predicate {predicate}",
                            datom.entity,
                            attribute.ident.qualified_name()
                        ),
                    ));
                }
            }
        }
        Ok(())
    }

    fn prepare_schema_changes(
        &self,
        ops: &[TxOp],
    ) -> Result<(Arc<Schema>, Vec<SchemaChange>), SemanticError> {
        if !ops
            .iter()
            .any(|op| matches!(op, TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_)))
        {
            return Ok((Arc::clone(&self.schema), Vec::new()));
        }
        let mut seen = BTreeSet::new();
        let mut successor = self.schema.as_ref().clone();
        let mut changes = Vec::new();
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
            if install {
                successor.install(attribute.clone())?;
                changes.push(SchemaChange::Install(attribute.clone()));
            } else {
                // Validate against db-before and its current facts. The docs
                // require conflicting data to be repaired before an alteration.
                self.validate_schema_change(attribute)?;
                successor.alter(attribute.clone())?;
                changes.push(SchemaChange::Alter(attribute.clone()));
            }
        }
        changes.sort_by_key(|change| match change {
            SchemaChange::Install(attribute) | SchemaChange::Alter(attribute) => attribute.id,
        });
        Ok((Arc::new(successor), changes))
    }

    fn resolve_tempids(&self, ops: &[TxOp]) -> Result<BTreeMap<String, u64>, SemanticError> {
        let mut names = BTreeSet::new();
        for op in ops {
            collect_tempids_op(op, &mut names);
        }
        let names: Vec<_> = names.into_iter().collect();
        let positions: BTreeMap<_, _> = names
            .iter()
            .enumerate()
            .map(|(index, name)| (name.clone(), index))
            .collect();
        let mut union = UnionFind::new(names.len());
        let mut identities: Vec<(usize, u32, Value, Unique)> = Vec::new();

        for op in ops {
            let TxOp::Add {
                entity: EntityRef::Temp(name),
                attribute,
                value: TxValue::Scalar(value),
            } = op
            else {
                continue;
            };
            let schema = self.schema.attribute(*attribute)?;
            let Some(unique) = schema.unique else {
                continue;
            };
            self.schema.validate_value(schema, value)?;
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

        let mut existing_by_root: BTreeMap<usize, u64> = BTreeMap::new();
        for (temp, attribute, value, unique) in &identities {
            if let Some(existing) = self.lookup(*attribute, value)? {
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

        let mut next = self.next_eid;
        let mut allocated_by_root = BTreeMap::new();
        let mut result = BTreeMap::new();
        for (index, name) in names.iter().enumerate() {
            let root = union.root(index);
            let id = if let Some(existing) = existing_by_root.get(&root) {
                *existing
            } else if let Some(allocated) = allocated_by_root.get(&root) {
                *allocated
            } else {
                let allocated = next;
                next += 1;
                allocated_by_root.insert(root, allocated);
                allocated
            };
            result.insert(name.clone(), id);
        }
        Ok(result)
    }

    #[allow(clippy::too_many_arguments)]
    fn expand_op(
        &self,
        op: &TxOp,
        tx: u64,
        tempids: &BTreeMap<String, u64>,
        logical: &mut Vec<LogicalDatom>,
        ensures: &mut Vec<EnsureCheck>,
        touched_constituents: &mut BTreeSet<(u64, u32)>,
    ) -> Result<(), SemanticError> {
        match op {
            TxOp::Add {
                entity,
                attribute,
                value,
            } => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                let value = self.resolve_value(*attribute, value, tx, tempids)?;
                if self.is_derived_composite(*attribute)? {
                    // Recovered `ProcessExpander` allows the value to
                    // participate in tempid/upsert resolution, then removes
                    // direct composite datoms before deriving composites from
                    // constituent changes.
                    return Ok(());
                }
                touched_constituents.insert((entity, *attribute));
                logical.push(LogicalDatom {
                    entity,
                    attribute: *attribute,
                    value,
                    added: true,
                });
            }
            TxOp::Retract {
                entity,
                attribute,
                value,
            } => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                self.schema.attribute(*attribute)?;
                if self.is_derived_composite(*attribute)? {
                    if let Some(value) = value {
                        self.resolve_value(*attribute, value, tx, tempids)?;
                    }
                    return Ok(());
                }
                touched_constituents.insert((entity, *attribute));
                if let Some(value) = value {
                    let value = self.resolve_value(*attribute, value, tx, tempids)?;
                    logical.push(LogicalDatom {
                        entity,
                        attribute: *attribute,
                        value,
                        added: false,
                    });
                } else {
                    for fact in self
                        .current
                        .iter()
                        .filter(|fact| fact.entity == entity && fact.attribute == *attribute)
                    {
                        logical.push(LogicalDatom {
                            entity,
                            attribute: *attribute,
                            value: fact.value.clone(),
                            added: false,
                        });
                    }
                }
            }
            TxOp::Cas {
                entity,
                attribute,
                old,
                new,
            } => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                let schema = self.schema.attribute(*attribute)?;
                if schema.cardinality != Cardinality::One {
                    return Err(SemanticError::incorrect(
                        "transaction/cas-requires-cardinality-one",
                        "compare-and-swap requires a cardinality-one attribute",
                    ));
                }
                let observed = self.values(entity, *attribute);
                let expected = old
                    .as_ref()
                    .map(|value| self.resolve_value(*attribute, value, tx, tempids))
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
                let value = self.resolve_value(*attribute, new, tx, tempids)?;
                if self.is_derived_composite(*attribute)? {
                    return Ok(());
                }
                touched_constituents.insert((entity, *attribute));
                logical.push(LogicalDatom {
                    entity,
                    attribute: *attribute,
                    value,
                    added: true,
                });
            }
            TxOp::RetractEntity(entity) => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                let mut visited = BTreeSet::new();
                self.expand_retract_entity(entity, logical, touched_constituents, &mut visited)?;
            }
            TxOp::Ensure { entity, required } => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                for attribute in required {
                    self.schema.attribute(*attribute)?;
                }
                ensures.push(EnsureCheck {
                    entity,
                    required: required.clone(),
                });
            }
            TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
        }
        Ok(())
    }

    fn expand_retract_entity(
        &self,
        entity: u64,
        logical: &mut Vec<LogicalDatom>,
        touched: &mut BTreeSet<(u64, u32)>,
        visited: &mut BTreeSet<u64>,
    ) -> Result<(), SemanticError> {
        if !visited.insert(entity) {
            return Ok(());
        }
        let facts: Vec<_> = self
            .current_indexes
            .matching(&IndexPrefix::Eavt {
                entity,
                attribute: None,
                value: None,
            })?
            .to_vec();
        for fact in facts {
            let attribute = self.schema.attribute(fact.attribute)?;
            if attribute.component
                && let Value::Ref(child) = fact.value
            {
                self.expand_retract_entity(child, logical, touched, visited)?;
            }
            touched.insert((entity, fact.attribute));
            logical.push(LogicalDatom {
                entity,
                attribute: fact.attribute,
                value: fact.value,
                added: false,
            });
        }

        // The documented built-in retracts facts where the target is either E
        // or V. VAET makes this proportional to incoming references rather
        // than a scan of all current facts.
        let incoming: Vec<_> = self
            .current_indexes
            .matching(&IndexPrefix::Vaet {
                value: Value::Ref(entity),
                attribute: None,
                entity: None,
            })?
            .to_vec();
        for fact in incoming {
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
        &self,
        entity: &EntityRef,
        tx: u64,
        tempids: &BTreeMap<String, u64>,
    ) -> Result<u64, SemanticError> {
        match entity {
            EntityRef::Id(id) => Ok(*id),
            EntityRef::Ident(ident) => {
                self.schema
                    .resolve_ident(ident)
                    .map(u64::from)
                    .ok_or_else(|| {
                        SemanticError::incorrect(
                            "transaction/unknown-ident",
                            format!("unknown ident {}", ident.qualified_name()),
                        )
                    })
            }
            EntityRef::Temp(tempid) => tempids.get(tempid).copied().ok_or_else(|| {
                SemanticError::incorrect(
                    "transaction/unknown-tempid",
                    format!("unknown tempid {tempid}"),
                )
            }),
            EntityRef::Lookup { attribute, value } => {
                self.lookup(*attribute, value)?.ok_or_else(|| {
                    SemanticError::incorrect(
                        "transaction/lookup-not-found",
                        "lookup ref did not resolve in db-before",
                    )
                })
            }
            EntityRef::Tx => Ok(tx),
        }
    }

    fn resolve_value(
        &self,
        attribute: u32,
        value: &TxValue,
        tx: u64,
        tempids: &BTreeMap<String, u64>,
    ) -> Result<Value, SemanticError> {
        let schema = self.schema.attribute(attribute)?;
        let value = match (schema.value_type, value) {
            (ValueType::Ref, TxValue::Entity(entity)) => {
                Value::Ref(self.resolve_entity(entity, tx, tempids)?)
            }
            (_, TxValue::Scalar(value)) => value.clone(),
            _ => {
                return Err(SemanticError::incorrect(
                    "transaction/value-type",
                    format!(
                        "attribute {} requires {:?}",
                        schema.ident.qualified_name(),
                        schema.value_type
                    ),
                ));
            }
        };
        self.schema.validate_value(schema, &value)?;
        Ok(value)
    }

    fn is_derived_composite(&self, attribute: u32) -> Result<bool, SemanticError> {
        let attribute = self.schema.attribute(attribute)?;
        Ok(matches!(attribute.tuple, Some(TupleSpec::Composite(_)))
            && !attribute.tuple_discontinued)
    }
}

impl<'a> DatabaseView<'a> {
    pub fn filter<F>(mut self, predicate: F) -> Self
    where
        F: Fn(&Database, &Datom) -> bool + Send + Sync + 'static,
    {
        self.predicates.push(Arc::new(predicate));
        self
    }

    pub fn as_of(mut self, t: u64) -> Self {
        self.through_t = Some(self.through_t.map_or(t, |current| current.min(t)));
        self
    }

    pub fn since(mut self, t: u64) -> Self {
        self.after_t = Some(self.after_t.map_or(t, |current| current.max(t)));
        self
    }

    pub fn history(mut self) -> Self {
        self.history = true;
        self
    }

    pub fn datoms(&self, order: IndexOrder) -> Vec<Datom> {
        let selected: Vec<_> = self
            .database
            .history_datoms()
            .filter(|datom| self.after_t.is_none_or(|after| datom.tx > after))
            .filter(|datom| self.through_t.is_none_or(|through| datom.tx <= through))
            .filter(|datom| {
                self.predicates
                    .iter()
                    .all(|predicate| predicate(self.database, datom))
            })
            .cloned()
            .collect();
        let roots = if self.history {
            IndexRoots::build(self.database.schema(), selected)
        } else {
            let current = replay(selected.iter());
            IndexRoots::build(self.database.schema(), facts_as_datoms(&current))
        };
        roots.get(order).to_vec()
    }

    pub fn datoms_with_prefix(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        let datoms = self.datoms(prefix.order());
        let mut matching = Vec::new();
        for datom in datoms {
            if prefix.matches(&datom)? {
                matching.push(datom);
            }
        }
        Ok(matching)
    }
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
        TxOp::RetractEntity(entity) | TxOp::Ensure { entity, .. } => {
            collect_tempids_entity(entity, output)
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
    if let EntityRef::Temp(tempid) = entity {
        output.insert(tempid.clone());
    }
}

fn dedupe(datoms: &mut Vec<LogicalDatom>) {
    let mut result: Vec<LogicalDatom> = Vec::new();
    for datom in datoms.drain(..) {
        if !result.iter().any(|existing| same_logical(existing, &datom)) {
            result.push(datom);
        }
    }
    result.sort_by(compare_logical);
    *datoms = result;
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
        .then_with(|| left.value.index_cmp(&right.value))
        .then_with(|| right.added.cmp(&left.added))
}

fn validate_same_transaction(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<(), SemanticError> {
    for (index, left) in datoms.iter().enumerate() {
        schema.attribute(left.attribute)?;
        for right in &datoms[index + 1..] {
            if left.entity == right.entity
                && left.attribute == right.attribute
                && left.value.index_cmp(&right.value).is_eq()
                && left.added != right.added
            {
                return Err(SemanticError::conflict(
                    "transaction/datoms-conflict",
                    "addition and retraction of the same E/A/V conflict",
                ));
            }
            let attribute = schema.attribute(left.attribute)?;
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

fn add_cardinality_one_retractions(
    schema: &Schema,
    current: &[CurrentFact],
    datoms: &mut Vec<LogicalDatom>,
) -> Result<(), SemanticError> {
    let additions: Vec<_> = datoms.iter().filter(|datom| datom.added).cloned().collect();
    for addition in additions {
        if schema.attribute(addition.attribute)?.cardinality != Cardinality::One {
            continue;
        }
        for existing in current.iter().filter(|fact| {
            fact.entity == addition.entity
                && fact.attribute == addition.attribute
                && !fact.value.stored_eq(&addition.value)
        }) {
            datoms.push(LogicalDatom {
                entity: existing.entity,
                attribute: existing.attribute,
                value: existing.value.clone(),
                added: false,
            });
        }
    }
    Ok(())
}

fn derive_composites(
    schema: &Schema,
    before: &[CurrentFact],
    after_user_data: &[CurrentFact],
    touched: &BTreeSet<(u64, u32)>,
) -> Result<Vec<LogicalDatom>, SemanticError> {
    let mut result = Vec::new();
    for composite in schema.attributes().filter(|attribute| {
        matches!(attribute.tuple, Some(TupleSpec::Composite(_))) && !attribute.tuple_discontinued
    }) {
        let TupleSpec::Composite(constituents) = composite.tuple.as_ref().unwrap() else {
            unreachable!()
        };
        let entities: BTreeSet<_> = touched
            .iter()
            .filter(|(_, attribute)| constituents.contains(attribute))
            .map(|(entity, _)| *entity)
            .collect();
        for entity in entities {
            let old = find_fact(before, entity, composite.id).map(|fact| fact.value.clone());
            let slots: Vec<_> = constituents
                .iter()
                .map(|attribute| {
                    find_fact(after_user_data, entity, *attribute).map(|fact| fact.value.clone())
                })
                .collect();
            let new = if slots.iter().all(Option::is_none) {
                None
            } else {
                Some(Value::Tuple(slots))
            };
            if let Some(old) = &old
                && new.as_ref().is_none_or(|new| !old.stored_eq(new))
            {
                result.push(LogicalDatom {
                    entity,
                    attribute: composite.id,
                    value: old.clone(),
                    added: false,
                });
            }
            if let Some(new) = new
                && old.as_ref().is_none_or(|old| !old.stored_eq(&new))
            {
                schema.validate_value(composite, &new)?;
                result.push(LogicalDatom {
                    entity,
                    attribute: composite.id,
                    value: new,
                    added: true,
                });
            }
        }
    }
    Ok(result)
}

fn apply_logical(current: &[CurrentFact], datoms: &[LogicalDatom], tx: u64) -> Vec<CurrentFact> {
    let mut result = current.to_vec();
    for datom in datoms.iter().filter(|datom| !datom.added) {
        result.retain(|fact| {
            !(fact.entity == datom.entity
                && fact.attribute == datom.attribute
                && fact.value.stored_eq(&datom.value))
        });
    }
    for datom in datoms.iter().filter(|datom| datom.added) {
        if !result.iter().any(|fact| {
            fact.entity == datom.entity
                && fact.attribute == datom.attribute
                && fact.value.stored_eq(&datom.value)
        }) {
            result.push(CurrentFact {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                tx,
            });
        }
    }
    result
}

fn validate_cardinality(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
    for (index, left) in facts.iter().enumerate() {
        if schema.attribute(left.attribute)?.cardinality != Cardinality::One {
            continue;
        }
        if facts[index + 1..]
            .iter()
            .any(|right| left.entity == right.entity && left.attribute == right.attribute)
        {
            return Err(SemanticError::conflict(
                "transaction/cardinality-one-conflict",
                "resulting database has multiple cardinality-one values",
            ));
        }
    }
    Ok(())
}

fn validate_uniqueness(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
    for (index, left) in facts.iter().enumerate() {
        let attribute = schema.attribute(left.attribute)?;
        if attribute.unique.is_none() {
            continue;
        }
        if left.value.is_nan() {
            return Err(SemanticError::incorrect(
                "transaction/nan-cannot-identify",
                "NaN cannot participate in uniqueness",
            ));
        }
        if facts[index + 1..].iter().any(|right| {
            left.attribute == right.attribute
                && left.entity != right.entity
                && left.value.index_cmp(&right.value).is_eq()
        }) {
            return Err(SemanticError::conflict(
                "transaction/unique-conflict",
                "resulting database has multiple holders of a unique value",
            ));
        }
    }
    Ok(())
}

fn validate_uniqueness_for_attribute(
    facts: &[CurrentFact],
    attribute: u32,
) -> Result<(), SemanticError> {
    for (index, left) in facts.iter().enumerate() {
        if left.attribute != attribute {
            continue;
        }
        if facts[index + 1..].iter().any(|right| {
            right.attribute == attribute
                && left.entity != right.entity
                && left.value.index_cmp(&right.value).is_eq()
        }) {
            return Err(SemanticError::conflict(
                "schema/unique-change-conflict",
                "current values must be unique before adding uniqueness",
            ));
        }
    }
    Ok(())
}

fn validate_ensures(facts: &[CurrentFact], ensures: &[EnsureCheck]) -> Result<(), SemanticError> {
    for ensure in ensures {
        let missing: Vec<_> = ensure
            .required
            .iter()
            .filter(|attribute| {
                !facts
                    .iter()
                    .any(|fact| fact.entity == ensure.entity && fact.attribute == **attribute)
            })
            .copied()
            .collect();
        if !missing.is_empty() {
            return Err(SemanticError::incorrect(
                "transaction/entity-spec",
                format!("entity {} is missing attributes {missing:?}", ensure.entity),
            ));
        }
    }
    Ok(())
}

fn material_changes(
    before: &[CurrentFact],
    after: &[CurrentFact],
    logical: &[LogicalDatom],
    tx: u64,
) -> Vec<Datom> {
    logical
        .iter()
        .filter(|datom| {
            let existed_before = contains_fact(before, datom.entity, datom.attribute, &datom.value);
            let exists_after = contains_fact(after, datom.entity, datom.attribute, &datom.value);
            if datom.added {
                !existed_before && exists_after
            } else {
                existed_before && !exists_after
            }
        })
        .map(|datom| Datom {
            entity: datom.entity,
            attribute: datom.attribute,
            value: datom.value.clone(),
            tx,
            added: datom.added,
        })
        .collect()
}

fn contains_fact(facts: &[CurrentFact], entity: u64, attribute: u32, value: &Value) -> bool {
    facts.iter().any(|fact| {
        fact.entity == entity && fact.attribute == attribute && fact.value.stored_eq(value)
    })
}

fn find_fact(facts: &[CurrentFact], entity: u64, attribute: u32) -> Option<&CurrentFact> {
    facts
        .iter()
        .find(|fact| fact.entity == entity && fact.attribute == attribute)
}

fn compare_current(left: &CurrentFact, right: &CurrentFact) -> Ordering {
    left.entity
        .cmp(&right.entity)
        .then(left.attribute.cmp(&right.attribute))
        .then_with(|| left.value.index_cmp(&right.value))
}

fn facts_as_datoms(facts: &[CurrentFact]) -> Vec<Datom> {
    facts
        .iter()
        .map(|fact| Datom {
            entity: fact.entity,
            attribute: fact.attribute,
            value: fact.value.clone(),
            tx: fact.tx,
            added: true,
        })
        .collect()
}

fn replay<'a>(datoms: impl Iterator<Item = &'a Datom>) -> Vec<CurrentFact> {
    let mut result = Vec::new();
    for datom in datoms {
        if datom.added {
            if !contains_fact(&result, datom.entity, datom.attribute, &datom.value) {
                result.push(CurrentFact {
                    entity: datom.entity,
                    attribute: datom.attribute,
                    value: datom.value.clone(),
                    tx: datom.tx,
                });
            }
        } else {
            result.retain(|fact| {
                !(fact.entity == datom.entity
                    && fact.attribute == datom.attribute
                    && fact.value.stored_eq(&datom.value))
            });
        }
    }
    result
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
            let (low, high) = if left < right {
                (left, right)
            } else {
                (right, left)
            };
            self.parent[high] = low;
        }
    }
}
