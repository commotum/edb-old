use crate::identity::{validate_frontier, validate_supported_eid};
use crate::idents::IdentIndex;
use crate::index::IndexRoots;
use crate::state_commitment::{CommitmentWork, SemanticStateCommitment};
use crate::vocabulary::{
    DB_EXCISE, DB_EXCISE_ATTRS, DB_EXCISE_BEFORE, DB_EXCISE_BEFORE_T, DB_IDENT, DB_TX_INSTANT,
    PRE_EXCISION_GENESIS_HASH, canonical_genesis_datoms, pre_excision_genesis_datoms,
    supported_system_attributes, supported_system_idents,
};
use crate::{
    Cardinality, Datom, ErrorCategory, INITIAL_EIDX_FRONTIER, IndexOrder, IndexPrefix, Schema,
    SemanticError, TX_PARTITION, TupleSpec, USER_PARTITION, Unique, Value, ValueType, eid_to_eidx,
    eid_to_part, make_eid, t_to_tx, tx_to_t,
};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum EntityRef {
    Id(u64),
    Ident(crate::Keyword),
    Temp(String),
    Lookup {
        attribute: u32,
        value: Value,
    },
    /// A lookup key whose ref or tuple-ref slots use transaction entity forms.
    /// Resolution is always against db-before, never transaction-local tempids.
    LookupInput {
        attribute: u32,
        value: Box<TxValue>,
    },
    Tx,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum TxValue {
    Scalar(Value),
    Entity(EntityRef),
    /// Transaction-only tuple slots. References remain symbolic until entity
    /// allocation; `None` is a tuple nil. Stored tuple values never contain
    /// unresolved identities, and nested tuple slots are not valid schema data.
    Tuple(Vec<Option<TxValue>>),
}

/// The raw value key used while resolving unique identities. Datomic permits
/// uniqueness on refs (`datomic_pro_docs/03_schema/03_identity_and_uniqueness.md`),
/// and recovered `ProcessExpander/get-ids` compares unresolved ref tempids
/// before replacing them (`db.clj:6712-6797, 7366-7455`). Reference tempids
/// therefore remain symbolic here: two entity tempids asserting the same
/// ref-valued identity must unify even when the referenced entity is new.
#[derive(Clone, Debug)]
pub(crate) enum UpsertIdentityValue {
    Resolved(Value),
    TempRef(String),
    Tuple(Vec<Option<UpsertIdentityValue>>),
}

impl UpsertIdentityValue {
    pub(crate) fn same_key(&self, other: &Self) -> bool {
        match (self, other) {
            (Self::Resolved(left), Self::Resolved(right)) => left.index_cmp(right).is_eq(),
            (Self::TempRef(left), Self::TempRef(right)) => left == right,
            (Self::Tuple(left), Self::Tuple(right)) => {
                left.len() == right.len()
                    && left
                        .iter()
                        .zip(right)
                        .all(|(left, right)| match (left, right) {
                            (None, None) => true,
                            (Some(left), Some(right)) => left.same_key(right),
                            _ => false,
                        })
            }
            _ => false,
        }
    }

    pub(crate) fn resolved(&self) -> Option<&Value> {
        match self {
            Self::Resolved(value) => Some(value),
            Self::TempRef(_) | Self::Tuple(_) => None,
        }
    }

    pub(crate) fn is_nan(&self) -> bool {
        self.resolved().is_some_and(Value::is_nan)
    }
}

/// Resolve only ref-typed input slots; scalar and nil slots retain their
/// stored meaning. Symbolic references remain available to identity grouping.
pub(crate) fn resolve_tuple_input(
    schema: &Schema,
    attribute: &crate::Attribute,
    slots: &[Option<TxValue>],
    mut resolve: impl FnMut(&EntityRef) -> Result<UpsertIdentityValue, SemanticError>,
) -> Result<UpsertIdentityValue, SemanticError> {
    let types = match &attribute.tuple {
        Some(TupleSpec::Homogeneous(kind)) => vec![*kind; slots.len()],
        Some(TupleSpec::Heterogeneous(types)) => types.clone(),
        Some(TupleSpec::Composite(attributes)) => attributes
            .iter()
            .map(|attribute| {
                schema
                    .attribute(*attribute)
                    .map(|attribute| attribute.value_type)
            })
            .collect::<Result<Vec<_>, _>>()?,
        None => {
            return Err(SemanticError::incorrect(
                "transaction/value-type",
                "tuple input requires a tuple attribute",
            ));
        }
    };
    if !(2..=8).contains(&slots.len()) || types.len() != slots.len() {
        return Err(SemanticError::incorrect(
            "transaction/invalid-tuple-length",
            "tuple input does not match its schema length",
        ));
    }
    let mut resolved = Vec::with_capacity(slots.len());
    for (slot, kind) in slots.iter().zip(types) {
        resolved.push(match slot {
            None => None,
            Some(TxValue::Scalar(value)) => Some(UpsertIdentityValue::Resolved(value.clone())),
            Some(TxValue::Entity(entity)) if kind == ValueType::Ref => Some(resolve(entity)?),
            Some(_) => return Err(SemanticError::incorrect("transaction/invalid-tuple-element", "tuple reference input requires a ref-typed slot; nested tuples are not scalar slots")),
        });
    }
    // A temporary ref's final numeric id cannot change tuple shape/type.
    // Validate with a type-only placeholder, never store or allocate it.
    let shape = Value::Tuple(
        resolved
            .iter()
            .map(|slot| {
                slot.as_ref().map(|value| {
                    value
                        .resolved()
                        .cloned()
                        .unwrap_or(Value::Ref(crate::DB_IDENT))
                })
            })
            .collect(),
    );
    schema.validate_value(attribute, &shape)?;
    if resolved
        .iter()
        .flatten()
        .all(|value| value.resolved().is_some())
    {
        Ok(UpsertIdentityValue::Resolved(shape))
    } else {
        Ok(UpsertIdentityValue::Tuple(resolved))
    }
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
        spec: EntityRef,
    },
    /// Native normalized form of an ordinary schema installation transaction.
    InstallAttribute(crate::Attribute),
    /// Native normalized form of an ordinary schema alteration transaction.
    AlterAttribute(crate::Attribute),
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) enum SchemaChange {
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

#[derive(Clone, Debug)]
pub struct TxReport {
    pub db_before: Database,
    pub db_after: Database,
    pub tx_data: Vec<Datom>,
    pub tempids: BTreeMap<String, u64>,
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
    spec: u64,
    required: Vec<u32>,
    predicates: Vec<String>,
}

/// The time boundary exactly as it existed when `:db/excise` was asserted.
/// The effective predicate is additionally capped by `request_t`, so facts
/// added after the request can never be removed by delayed background work.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum ExcisionCutoff {
    BeforeT(u64),
    BeforeInstant(i64),
}

/// One immutable excision predicate reconstructed from ordinary history.
///
/// Recovered 1.0.7705's repair detector identifies requests by the transaction
/// that asserted A=15 and pulls the entity as-of that transaction
/// (`tools/detect_ch197761.clj:51-73`). Keeping the same identity prevents
/// later edits to the request entity from changing work already requested.
#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct FrozenExcisionRequest {
    pub(crate) request_entity: u64,
    pub(crate) request_t: u64,
    pub(crate) target: u64,
    pub(crate) attributes: BTreeSet<u64>,
    pub(crate) cutoff: Option<ExcisionCutoff>,
}

/// One pure, fully assessed successor plus the predicate checks that remain
/// before it may be accepted.  Keeping this boundary explicit mirrors the
/// recovered `filter-assess-tx-datoms`/delayed predicate-resolution flow:
/// redundancy, cardinality, tempids, and db-after are decided before any
/// process-local or persisted predicate code is looked up or run.
pub(crate) struct AssessedTransaction {
    report: TxReport,
    ensures: Vec<EnsureCheck>,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum PredicateRole {
    Attribute,
    Entity,
    Both,
}

impl PredicateRole {
    pub(crate) fn include(self, role: Self) -> Self {
        if self == role { self } else { Self::Both }
    }

    pub(crate) fn requires_attribute(self) -> bool {
        matches!(self, Self::Attribute | Self::Both)
    }

    pub(crate) fn requires_entity(self) -> bool {
        matches!(self, Self::Entity | Self::Both)
    }
}

impl AssessedTransaction {
    pub(crate) fn validate(
        self,
        functions: Option<&crate::TxFunctions>,
    ) -> Result<TxReport, SemanticError> {
        self.report.db_before.validate_attribute_predicates_with(
            self.report.db_before.schema(),
            &self.report.tx_data,
            functions,
        )?;
        validate_ensures(&self.report.db_after, &self.ensures, functions)?;
        Ok(self.report)
    }
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
    idents: Arc<IdentIndex>,
    basis_t: u64,
    /// Exclusive global entity-index issuance frontier. Entity IDs themselves
    /// retain their partition bits; this field deliberately stores only the
    /// recovered low 42-bit `eidx` boundary.
    eidx_frontier: u64,
    last_tx_instant: Option<i64>,
    tx_instant_attribute: Option<u32>,
    current: Arc<[CurrentFact]>,
    /// Canonical immutable t=0 information. This is indexed and queryable but
    /// is not one of the positive user transaction chunks counted by basis_t.
    genesis: Arc<[Datom]>,
    history: Arc<[Arc<[Datom]>]>,
    current_indexes: IndexRoots,
    history_indexes: IndexRoots,
    /// Packing-independent semantic roots carried with the immutable value.
    /// Successors update these roots from their material transaction delta;
    /// recovery/index constructors rebuild them once from authoritative facts.
    semantic_state: SemanticStateCommitment,
    /// Diagnostic work for the transition that produced this value. This is
    /// deliberately outside the semantic digest.
    semantic_commitment_work: CommitmentWork,
}

impl Database {
    /// Wrap this eager kernel snapshot as the exact immutable value consumed
    /// by read-side APIs. Cloning `Database` preserves its shared immutable
    /// index roots.
    pub fn database_value(&self) -> crate::DatabaseValue {
        crate::DatabaseValue::from(self)
    }

    /// Construct the exact native t=0 database. Application schema is
    /// installed by ordinary positive transactions, never appended to
    /// genesis.
    pub fn bootstrap() -> Result<Self, SemanticError> {
        Self::from_genesis(canonical_genesis_datoms())
    }

    /// Convenience constructor that bootstraps the database and installs the
    /// supplied application attributes in one ordinary transaction. The
    /// resulting value is therefore at basis 1 when `schema` is non-empty.
    /// New code that needs to observe creation separately should use
    /// [`Database::bootstrap`] and transact the schema explicitly.
    pub fn new(schema: Schema) -> Result<Self, SemanticError> {
        schema.validate_tuple_definitions()?;
        for attribute in schema.attributes() {
            if supported_system_idents()
                .iter()
                .any(|(entity, _)| *entity == u64::from(attribute.id))
            {
                return Err(SemanticError::incorrect(
                    "schema/reserved-system-entity",
                    format!(
                        "entity {} is reserved by the native system vocabulary",
                        attribute.id
                    ),
                ));
            }
        }
        if schema.ident_aliases().next().is_some() {
            return Err(SemanticError::incorrect(
                "schema/bootstrap-alias",
                "initial schema aliases must be introduced by an ordinary rename transaction",
            ));
        }
        let database = Self::bootstrap()?;
        let operations: Vec<_> = schema
            .attributes()
            .cloned()
            .map(TxOp::InstallAttribute)
            .collect();
        if operations.is_empty() {
            Ok(database)
        } else {
            Ok(database.with(&operations, 0)?.db_after)
        }
    }

    pub(crate) fn from_genesis(genesis: Vec<Datom>) -> Result<Self, SemanticError> {
        validate_genesis_information(&genesis)?;
        let current = replay(genesis.iter());
        let current_datoms = facts_as_datoms(&current);
        let idents = IdentIndex::derive(genesis.iter(), DB_IDENT as u32)?;
        let derived_schema = Schema::derive_from_information(&current_datoms, &idents)?;
        let tx_instant_attribute = DB_TX_INSTANT as u32;
        let tx_instant = derived_schema.attribute(tx_instant_attribute)?;
        if tx_instant.value_type != ValueType::Instant || tx_instant.cardinality != Cardinality::One
        {
            return Err(SemanticError::incorrect(
                "schema/invalid-tx-instant-attribute",
                ":db/txInstant must be a cardinality-one instant",
            ));
        }
        let highest_attribute = derived_schema
            .attributes()
            .map(|attribute| u64::from(attribute.id))
            .max()
            .unwrap_or(0);
        let eidx_frontier = INITIAL_EIDX_FRONTIER.max(highest_attribute.saturating_add(1));
        validate_frontier(eidx_frontier)?;
        let current_indexes = IndexRoots::build(&derived_schema, current_datoms);
        let history_indexes = IndexRoots::build(&derived_schema, genesis.iter().cloned());
        let mut database = Self {
            schema: Arc::new(derived_schema),
            idents: Arc::new(idents),
            basis_t: 0,
            eidx_frontier,
            last_tx_instant: None,
            tx_instant_attribute: Some(tx_instant_attribute),
            current: current.into(),
            genesis: genesis.into(),
            history: Arc::default(),
            current_indexes,
            history_indexes,
            semantic_state: SemanticStateCommitment::default(),
            semantic_commitment_work: CommitmentWork::default(),
        };
        database.semantic_state = SemanticStateCommitment::recompute(&database)?;
        database.validate_invariants()?;
        Ok(database)
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

    /// Share the immutable resident schema projection without rebuilding it.
    /// Transaction assessment uses this at the unchanged-schema fast path;
    /// the eager kernel already stores the projection behind an `Arc`.
    pub(crate) fn schema_arc(&self) -> Arc<Schema> {
        Arc::clone(&self.schema)
    }

    /// Resolve any entity ident, including enum values and historical aliases.
    pub fn entid(&self, ident: &crate::Keyword) -> Option<u64> {
        self.idents.resolve(ident)
    }

    /// Return the most recently asserted ident for an entity.
    pub fn ident(&self, entity: u64) -> Option<&crate::Keyword> {
        self.idents.ident(entity)
    }

    /// Drop and deterministically rebuild every schema-dependent cache from
    /// immutable information. This has no semantic effect and exists as both
    /// a recovery primitive and an executable proof that the caches are not a
    /// second authority.
    pub fn rebuild_derived_caches(&self) -> Result<Self, SemanticError> {
        let mut rebuilt = self.clone();
        let mut replayed = replay(rebuilt.history_datoms());
        replayed.sort_by(compare_current);
        let current = facts_as_datoms(&replayed);
        let idents = IdentIndex::derive(rebuilt.history_datoms(), DB_IDENT as u32)?;
        let schema = Arc::new(Schema::derive_from_information(&current, &idents)?);
        rebuilt.idents = Arc::new(idents);
        rebuilt.schema = schema;
        rebuilt.current = replayed.into();
        rebuilt.current_indexes = IndexRoots::build(&rebuilt.schema, current);
        rebuilt.history_indexes =
            IndexRoots::build(&rebuilt.schema, rebuilt.history_datoms().cloned());
        rebuilt.validate_invariants()?;
        Ok(rebuilt)
    }

    pub(crate) fn genesis_datoms(&self) -> &[Datom] {
        &self.genesis
    }

    pub fn basis_t(&self) -> u64 {
        self.basis_t
    }

    /// Transaction time at the current basis. The pure kernel only checks the
    /// lower bound; the authoritative transactor owns the server-clock upper
    /// bound for an explicit override.
    pub(crate) fn last_tx_instant(&self) -> Option<i64> {
        self.last_tx_instant
    }

    /// Exclusive non-negative entity-index issuance frontier.
    pub fn eidx_frontier(&self) -> u64 {
        self.eidx_frontier
    }

    pub(crate) fn semantic_state_commitment(&self) -> &SemanticStateCommitment {
        &self.semantic_state
    }

    #[cfg(test)]
    pub(crate) fn semantic_commitment_work(&self) -> CommitmentWork {
        self.semantic_commitment_work
    }

    /// Reconstitute a verified immutable value from a persistent index base.
    ///
    /// This is intentionally crate-private: durable manifests are the only
    /// caller, and all ordinary state transitions continue through `with` or
    /// `apply_committed`.
    pub(crate) fn from_index_base(
        basis_t: u64,
        eidx_frontier: u64,
        current_datoms: Vec<Datom>,
        genesis_datoms: Vec<Datom>,
        history_chunks: Vec<Vec<Datom>>,
    ) -> Result<Self, SemanticError> {
        validate_frontier(eidx_frontier)?;
        t_to_tx(basis_t).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "index/basis-out-of-range",
                format!("index base basis cannot be represented as a transaction id: {error}"),
            )
        })?;
        if basis_t == 0 || history_chunks.len() != usize::try_from(basis_t).unwrap_or(usize::MAX) {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "index/base-basis-mismatch",
                "index base must contain one history chunk per positive basis",
            ));
        }
        validate_genesis_information(&genesis_datoms)?;
        let history: Vec<Arc<[Datom]>> = history_chunks.into_iter().map(Arc::from).collect();
        let all_information: Vec<_> = genesis_datoms
            .iter()
            .chain(history.iter().flat_map(|chunk| chunk.iter()))
            .cloned()
            .collect();
        let idents = IdentIndex::derive(all_information.iter(), DB_IDENT as u32)?;
        let derived_schema = Schema::derive_from_information(&current_datoms, &idents)?;
        let tx_instant_attribute = DB_TX_INSTANT as u32;
        let tx_instant = derived_schema
            .attribute(tx_instant_attribute)
            .map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "index/missing-tx-instant",
                    format!("index base has no valid :db/txInstant: {error}"),
                )
            })?;
        if tx_instant.value_type != ValueType::Instant || tx_instant.cardinality != Cardinality::One
        {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "index/invalid-tx-instant",
                "index base :db/txInstant is not cardinality-one instant",
            ));
        }
        let mut current = Vec::with_capacity(current_datoms.len());
        for datom in &current_datoms {
            let datom_t = tx_to_t(datom.tx).map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "index/invalid-current-transaction",
                    format!("current index datom has an invalid transaction id: {error}"),
                )
            })?;
            if !datom.added || datom_t > basis_t {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "index/invalid-current-datom",
                    "current index base contains a retraction or invalid transaction",
                ));
            }
            validate_stored_entity(datom.entity, eidx_frontier, basis_t)?;
            validate_stored_value(&datom.value, eidx_frontier, basis_t)?;
            derived_schema
                .validate_value(derived_schema.attribute(datom.attribute)?, &datom.value)?;
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
        let last_tx = t_to_tx(basis_t).expect("basis was checked above");
        let last_tx_instant = history
            .last()
            .and_then(|chunk| {
                chunk.iter().find_map(|datom| {
                    if datom.entity == last_tx
                        && datom.attribute == tx_instant_attribute
                        && datom.added
                    {
                        match datom.value {
                            Value::Instant(value) => Some(value),
                            _ => None,
                        }
                    } else {
                        None
                    }
                })
            })
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "index/invalid-last-tx-instant",
                    "index base lacks its final transaction instant",
                )
            })?;
        let current_datoms = facts_as_datoms(&current);
        let mut database = Self {
            schema: Arc::new(derived_schema),
            idents: Arc::new(idents),
            basis_t,
            eidx_frontier,
            last_tx_instant: Some(last_tx_instant),
            tx_instant_attribute: Some(tx_instant_attribute),
            current: current.into(),
            genesis: genesis_datoms.into(),
            history: history.into(),
            current_indexes: IndexRoots::default(),
            history_indexes: IndexRoots::default(),
            semantic_state: SemanticStateCommitment::default(),
            semantic_commitment_work: CommitmentWork::default(),
        };
        database.current_indexes = IndexRoots::build(&database.schema, current_datoms);
        database.history_indexes =
            IndexRoots::build(&database.schema, database.history_datoms().cloned());
        database.semantic_state = SemanticStateCommitment::recompute(&database)?;
        database.validate_invariants()?;
        Ok(database)
    }

    /// Recovered `as-of-t` resolution for a millisecond instant.
    ///
    /// The AVET lower-bound seek returns the first transaction at or after the
    /// instant.  An exact match is used directly; otherwise its predecessor T
    /// is used.  Consequently, when several transactions share the exact same
    /// millisecond, the earliest of them wins.  This is the documented
    /// imprecision of instant time points; T/Tx remain exact.  An instant after
    /// the database resolves to the next T, which is observationally current.
    pub fn t_at_or_before_instant(&self, instant: i64) -> u64 {
        let Some(attribute) = self.tx_instant_attribute else {
            return 0;
        };
        let candidate = self
            .current_indexes
            .seek(&IndexPrefix::Avet {
                attribute,
                value: Some(Value::Instant(instant)),
                entity: None,
            })
            .expect("internally constructed index prefix is valid")
            .iter()
            .next()
            .filter(|datom| datom.attribute == attribute);
        match candidate {
            Some(datom) => {
                let Value::Instant(candidate_instant) = &datom.value else {
                    unreachable!("validated :db/txInstant index contains only instants")
                };
                let candidate_t =
                    tx_to_t(datom.tx).expect("validated :db/txInstant datom has a transaction id");
                if *candidate_instant == instant {
                    candidate_t
                } else {
                    candidate_t
                        .checked_sub(1)
                        .expect("positive transaction T has a predecessor boundary")
                }
            }
            None => self.basis_t.saturating_add(1),
        }
    }

    /// Least transaction t whose transaction instant is at or after the
    /// supplied instant, or the next t when the instant is beyond this DB.
    pub fn t_at_or_after_instant(&self, instant: i64) -> u64 {
        let Some(attribute) = self.tx_instant_attribute else {
            return self.basis_t + 1;
        };
        self.current_indexes
            .seek(&IndexPrefix::Avet {
                attribute,
                value: Some(Value::Instant(instant)),
                entity: None,
            })
            .expect("internally constructed index prefix is valid")
            .iter()
            .next()
            .filter(|datom| datom.attribute == attribute)
            .map(|datom| {
                tx_to_t(datom.tx).expect("validated :db/txInstant datom has a transaction id")
            })
            .unwrap_or(self.basis_t + 1)
    }

    pub fn as_of_instant(&self, instant: i64) -> crate::DatabaseValue {
        self.view(View::AsOf(self.t_at_or_before_instant(instant)))
    }

    /// Derive the same owned immutable value used by query, pull, and entity
    /// APIs. Keeping one implementation avoids the old borrowed view's
    /// divergent filter context and whole-index rebuild semantics.
    pub fn view(&self, view: View) -> crate::DatabaseValue {
        let value = self.database_value();
        match view {
            View::Current => value,
            View::AsOf(t) => value.as_of(t),
            View::Since(t) => value.since(t),
            View::History => value.history(),
        }
    }

    pub fn datoms(&self, view: View, order: IndexOrder) -> Vec<Datom> {
        match view {
            View::Current => self.current_indexes.get(order).to_vec(),
            View::History => self.history_indexes.get(order).to_vec(),
            View::AsOf(t) => {
                let facts = replay(
                    self.history_datoms()
                        .filter(|d| tx_to_t(d.tx).is_ok_and(|datom_t| datom_t <= t)),
                );
                IndexRoots::build(&self.schema, facts_as_datoms(&facts))
                    .get(order)
                    .to_vec()
            }
            View::Since(t) => {
                let facts = replay(
                    self.history_datoms()
                        .filter(|d| tx_to_t(d.tx).is_ok_and(|datom_t| datom_t > t)),
                );
                IndexRoots::build(&self.schema, facts_as_datoms(&facts))
                    .get(order)
                    .to_vec()
            }
        }
    }

    /// Borrow one already-resident current or history index without copying
    /// it. `DatabaseValue` uses this narrow eager-oracle seam to give its
    /// public fallible cursor the same construction-time laziness as a native
    /// tree cursor.
    pub(crate) fn index_datoms(&self, history: bool, order: IndexOrder) -> &[Datom] {
        if history {
            self.history_indexes.get(order)
        } else {
            self.current_indexes.get(order)
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

    /// Reconstruct every currently pending excision request after a durable
    /// processed-through frontier. Request data is read as-of the transaction
    /// that asserted `:db/excise`, not from the mutable current entity.
    pub(crate) fn pending_excision_requests_after(
        &self,
        processed_through_t: u64,
    ) -> Result<Vec<FrozenExcisionRequest>, SemanticError> {
        let targets = self.current_indexes.matching(&IndexPrefix::Aevt {
            attribute: DB_EXCISE as u32,
            entity: None,
            value: None,
        })?;
        let mut requests = Vec::new();
        for target_datom in targets {
            let request_t = tx_to_t(target_datom.tx).map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "excision/invalid-request-transaction",
                    format!("excision request has an invalid assertion transaction: {error}"),
                )
            })?;
            if request_t <= processed_through_t {
                continue;
            }
            requests.push(self.freeze_excision_assertion(target_datom)?);
        }
        requests.sort_by_key(|request| (request.request_t, request.request_entity));
        Ok(requests)
    }

    /// Historical excision assertions are an audit surface, distinct from the
    /// current A=15 assertions that still need background work. A later
    /// ordinary retraction removes a request from the pending set but does not
    /// erase the assertion from history.
    pub(crate) fn historical_excision_requests(
        &self,
    ) -> Result<Vec<FrozenExcisionRequest>, SemanticError> {
        let assertions = self.history_indexes.matching(&IndexPrefix::Aevt {
            attribute: DB_EXCISE as u32,
            entity: None,
            value: None,
        })?;
        let mut requests = assertions
            .iter()
            .filter(|datom| datom.added)
            .map(|datom| self.freeze_excision_assertion(datom))
            .collect::<Result<Vec<_>, _>>()?;
        requests.sort_by_key(|request| (request.request_t, request.request_entity));
        Ok(requests)
    }

    fn freeze_excision_assertion(
        &self,
        target_datom: &Datom,
    ) -> Result<FrozenExcisionRequest, SemanticError> {
        let request_t = tx_to_t(target_datom.tx).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "excision/invalid-request-transaction",
                format!("excision request has an invalid assertion transaction: {error}"),
            )
        })?;
        let as_asserted = self.datoms(View::AsOf(request_t), IndexOrder::Eavt);
        let request_datoms: Vec<_> = as_asserted
            .iter()
            .filter(|datom| datom.entity == target_datom.entity)
            .collect();
        let targets: Vec<_> = request_datoms
            .iter()
            .filter_map(|datom| {
                (datom.attribute == DB_EXCISE as u32)
                    .then_some(&datom.value)
                    .and_then(|value| match value {
                        Value::Ref(target) => Some(*target),
                        _ => None,
                    })
            })
            .collect();
        let target = match targets.as_slice() {
            [target] => *target,
            _ => {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "excision/invalid-frozen-target",
                    "an excision assertion does not reconstruct to exactly one reference target",
                ));
            }
        };
        let attributes = request_datoms
            .iter()
            .filter_map(|datom| {
                (datom.attribute == DB_EXCISE_ATTRS as u32)
                    .then_some(&datom.value)
                    .and_then(|value| match value {
                        Value::Ref(attribute) => Some(*attribute),
                        _ => None,
                    })
            })
            .collect();
        let before_t = one_excision_value(&request_datoms, DB_EXCISE_BEFORE_T as u32)?;
        let before = one_excision_value(&request_datoms, DB_EXCISE_BEFORE as u32)?;
        let cutoff = match (before_t, before) {
            (None, None) => None,
            (Some(Value::Long(value)), None) => Some(ExcisionCutoff::BeforeT(
                normalize_excision_before_t(*value)?,
            )),
            (None, Some(Value::Instant(value))) => Some(ExcisionCutoff::BeforeInstant(*value)),
            (Some(_), Some(_)) => {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "excision/frozen-cutoff-conflict",
                    "frozen excision request contains both beforeT and before",
                ));
            }
            _ => {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "excision/invalid-frozen-cutoff",
                    "frozen excision request cutoff has the wrong stored type",
                ));
            }
        };
        Ok(FrozenExcisionRequest {
            request_entity: target_datom.entity,
            request_t,
            target,
            attributes,
            cutoff,
        })
    }

    /// Resolve the exclusive transaction boundary of a frozen request. The
    /// request transaction is always an additional cap, preserving recovered
    /// `pred-and-extent`'s `< request-t` rule even if indexing is delayed.
    pub(crate) fn excision_before_t(&self, request: &FrozenExcisionRequest) -> u64 {
        let requested = match request.cutoff {
            Some(ExcisionCutoff::BeforeT(t)) => t,
            Some(ExcisionCutoff::BeforeInstant(instant)) => self.t_at_or_after_instant(instant),
            None => request.request_t,
        };
        request.request_t.min(requested)
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
        self.genesis
            .iter()
            .chain(self.history.iter().flat_map(|chunk| chunk.iter()))
    }

    /// Cross-check all immutable roots and post-state invariants against the
    /// retained straightforward fact representation.
    pub fn validate_invariants(&self) -> Result<(), SemanticError> {
        validate_frontier(self.eidx_frontier)?;
        validate_genesis_information(&self.genesis)?;
        self.schema.validate_tuple_definitions()?;
        t_to_tx(self.basis_t).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "kernel/basis-out-of-range",
                format!("database basis cannot be represented as a transaction id: {error}"),
            )
        })?;
        let current = facts_as_datoms(&self.current);
        let derived_idents = IdentIndex::derive(self.history_datoms(), DB_IDENT as u32)?;
        let derived_schema = Schema::derive_from_information(&current, &derived_idents)?;
        if derived_idents.cmp_observation(&self.idents).is_ne() || derived_schema != *self.schema {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/derived-schema-divergence",
                "schema or ident cache diverged from ordinary database information",
            ));
        }
        let expected_semantic_state = SemanticStateCommitment::from_current(&current)?;
        if expected_semantic_state.metadata() != self.semantic_state.metadata() {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/semantic-commitment-divergence",
                "incremental semantic commitment diverged from current datoms",
            ));
        }
        let expected_current = IndexRoots::build(&self.schema, current);
        let history: Vec<_> = self.history_datoms().cloned().collect();
        let expected_history = IndexRoots::build(&self.schema, history);
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            if !same_stored_datoms(self.current_indexes.get(order), expected_current.get(order)) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/current-index-divergence",
                    format!("current {order:?} root diverged from facts"),
                ));
            }
            if !same_stored_datoms(self.history_indexes.get(order), expected_history.get(order)) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/history-index-divergence",
                    format!("history {order:?} root diverged from transaction chunks"),
                ));
            }
        }
        validate_cardinality(&self.schema, &self.current)?;
        validate_uniqueness(&self.schema, &self.current)?;
        validate_excision_requests(&self.current)?;
        for fact in self.current.iter() {
            let attribute = self.schema.attribute(fact.attribute)?;
            self.schema.validate_value(attribute, &fact.value)?;
        }
        let mut replayed = replay(self.history_datoms());
        replayed.sort_by(compare_current);
        if !same_stored_current(&replayed, &self.current) {
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
        if let Some(tx_instant_attribute) = self.tx_instant_attribute {
            for (offset, chunk) in self.history.iter().enumerate() {
                let t = offset as u64 + 1;
                let tx = t_to_tx(t).map_err(|error| {
                    SemanticError::new(
                        ErrorCategory::Fault,
                        "kernel/invalid-history-transaction",
                        format!("history transaction {t} cannot be represented: {error}"),
                    )
                })?;
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
                        format!("history chunk for transaction t={t}, tx={tx} is malformed"),
                    ));
                }
            }
        }
        for datom in self.genesis.iter() {
            validate_stored_entity(datom.entity, self.eidx_frontier, self.basis_t)?;
            validate_stored_value(&datom.value, self.eidx_frontier, self.basis_t)?;
        }
        for datom in self.history.iter().flat_map(|chunk| chunk.iter()) {
            let datom_t = tx_to_t(datom.tx).map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/invalid-datom-transaction",
                    format!("history datom has an invalid transaction entity id: {error}"),
                )
            })?;
            if datom_t == 0 || datom_t > self.basis_t {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "kernel/future-datom",
                    "history contains a datom outside the positive database basis",
                ));
            }
            validate_stored_entity(datom.entity, self.eidx_frontier, self.basis_t)?;
            validate_stored_value(&datom.value, self.eidx_frontier, self.basis_t)?;
        }
        Ok(())
    }

    /// Exact information equality for internal recovery/index cross-checks.
    /// `Value::PartialEq` intentionally follows Datomic's logical comparator,
    /// so it is too weak at a storage boundary where BigDecimal scale matters.
    pub(crate) fn same_information_as(&self, other: &Self) -> bool {
        self.basis_t == other.basis_t
            && self.eidx_frontier == other.eidx_frontier
            && self.schema == other.schema
            && self.same_current_as(other)
            && same_stored_datoms(
                &self.datoms(View::History, IndexOrder::Eavt),
                &other.datoms(View::History, IndexOrder::Eavt),
            )
    }

    pub(crate) fn same_current_as(&self, other: &Self) -> bool {
        self.basis_t == other.basis_t
            && self.eidx_frontier == other.eidx_frontier
            && self.schema == other.schema
            && same_stored_datoms(
                &self.datoms(View::Current, IndexOrder::Eavt),
                &other.datoms(View::Current, IndexOrder::Eavt),
            )
    }

    /// Validate the observable invariants of replacing one installed schema
    /// descriptor. Actual schema changes remain transactions in the production
    /// kernel; this helper exists so foundation fixtures can judge a proposed
    /// transition without introducing a second mutation path.
    pub fn validate_schema_change(&self, proposed: &crate::Attribute) -> Result<(), SemanticError> {
        self.validate_schema_change_against(proposed, &self.current, false)
    }

    fn validate_schema_change_against(
        &self,
        proposed: &crate::Attribute,
        facts: &[CurrentFact],
        allow_discontinued_ident_retarget: bool,
    ) -> Result<(), SemanticError> {
        let current = self.schema.attribute(proposed.id)?;
        self.schema.validate_attribute(proposed)?;
        if current.value_type != proposed.value_type {
            return Err(SemanticError::incorrect(
                "schema/value-type-immutable",
                "an installed attribute's value type cannot change",
            ));
        }
        if current.tuple != proposed.tuple && !allow_discontinued_ident_retarget {
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
            for fact in facts.iter().filter(|fact| fact.attribute == proposed.id) {
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
            // Recovered Datomic Pro distinguishes logical `:db/index` from
            // physical AVET availability (`Attribute.hasAVET`).  The eager
            // oracle has no asynchronous physical tier, so an existing
            // logical AVET member is ready.  An unindexed attribute may gain
            // uniqueness only when it has never had a value: in that case
            // there is no historical range to backfill and AVET can become
            // available synchronously.  `has-values?` in 1.0.7705 makes this
            // exact test against history AEVT, not merely current facts.
            let has_historical_values = self
                .history_datoms()
                .any(|datom| datom.attribute == proposed.id);
            if !current.indexed && has_historical_values {
                return Err(SemanticError::incorrect(
                    "schema/unique-requires-avet",
                    "adding uniqueness to an attribute with historical values requires a physically ready AVET index",
                ));
            }
            validate_uniqueness_for_attribute(facts, proposed.id)?;
        }
        Ok(())
    }

    fn schema_changes_to(
        &self,
        successor: &Schema,
        final_current: &[CurrentFact],
        logical: &[LogicalDatom],
    ) -> Result<Vec<SchemaChange>, SemanticError> {
        // Protect the exact stored root information, not whatever a newer
        // binary now emits for newly created databases. Recovered Datomic
        // installs bootstrap data upgrades as ordinary transactions; mutating
        // old genesis here would silently change the root chain identity.
        for required in self.genesis.iter() {
            if !contains_fact(
                final_current,
                required.entity,
                required.attribute,
                &required.value,
            ) {
                return Err(SemanticError::incorrect(
                    "schema/native-information-immutable",
                    "native genesis information cannot be retracted or replaced",
                ));
            }
        }
        for system in supported_system_attributes() {
            if self.schema.attribute(system.id).is_ok()
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
        for current in self.schema.attributes() {
            if successor.attribute(current.id).is_err() {
                return Err(SemanticError::incorrect(
                    "schema/removed-attribute",
                    format!(
                        "committed information removed installed attribute {}",
                        current.id
                    ),
                ));
            }
        }
        let mut changes = Vec::new();
        let mut installed = Vec::new();
        for proposed in successor.attributes() {
            match self.schema.attribute(proposed.id) {
                Ok(current) if current == proposed => {}
                Ok(current) => {
                    let tuple_property_touched = logical.iter().any(|datom| {
                        datom.entity == u64::from(proposed.id)
                            && matches!(
                                u64::from(datom.attribute),
                                crate::DB_TUPLE_TYPE
                                    | crate::DB_TUPLE_TYPES
                                    | crate::DB_TUPLE_ATTRS
                            )
                    });
                    let allow_discontinued_ident_retarget = current.tuple_discontinued
                        && proposed.tuple_discontinued
                        && !tuple_property_touched;
                    self.validate_schema_change_against(
                        proposed,
                        final_current,
                        allow_discontinued_ident_retarget,
                    )?;
                    changes.push(SchemaChange::Alter(proposed.clone()));
                }
                Err(_) => {
                    let reserved = supported_system_idents()
                        .iter()
                        .any(|(entity, _)| *entity == u64::from(proposed.id));
                    if reserved && !self.is_exact_excision_bootstrap_upgrade(successor, proposed) {
                        return Err(SemanticError::incorrect(
                            "schema/reserved-system-entity",
                            format!(
                                "entity {} is reserved by the native vocabulary",
                                proposed.id
                            ),
                        ));
                    }
                    installed.push(proposed.id);
                    changes.push(SchemaChange::Install(proposed.clone()));
                }
            }
        }
        successor.validate_tuple_installations(&installed)?;
        Ok(changes)
    }

    fn is_exact_excision_bootstrap_upgrade(
        &self,
        successor: &Schema,
        proposed: &crate::Attribute,
    ) -> bool {
        const IDS: [u32; 4] = [
            DB_EXCISE as u32,
            DB_EXCISE_ATTRS as u32,
            DB_EXCISE_BEFORE_T as u32,
            DB_EXCISE_BEFORE as u32,
        ];
        same_stored_datoms(&self.genesis, &pre_excision_genesis_datoms())
            && IDS.contains(&proposed.id)
            && supported_system_attributes()
                .iter()
                .find(|attribute| attribute.id == proposed.id)
                == Some(proposed)
            && IDS.iter().all(|id| {
                self.schema.attribute(*id).is_err()
                    && successor.attribute(*id).is_ok_and(|candidate| {
                        supported_system_attributes()
                            .iter()
                            .find(|attribute| attribute.id == *id)
                            == Some(candidate)
                    })
            })
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
        self.assess_context(ops, tx_instant)?.validate(None)
    }

    /// Rebuild one already-assessed committed successor from its material
    /// transaction record. Persistence uses this path during recovery; it
    /// deliberately does not rerun transaction functions, tempid resolution,
    /// or any other request-time behavior. The predecessor is a validated
    /// native value. This checks every new semantic input; independent full
    /// cache/root/history audits belong at explicit recovery endpoints, via
    /// `validate_invariants`, rather than at each intermediate prefix.
    pub(crate) fn apply_committed(
        &self,
        transaction: &crate::DurableTransaction,
    ) -> Result<Self, SemanticError> {
        self.apply_committed_with_excision(transaction, false)
    }

    /// Replay a transaction from a generation whose history was filtered by
    /// an authenticated excision rewrite. A later retraction can legitimately
    /// remain after the earlier assertion it once retracted was removed. Only
    /// that one non-material shape is relaxed; duplicate additions, schema
    /// transitions, transaction identity, instants, cardinality, uniqueness,
    /// and every other recovery invariant remain strict.
    pub(crate) fn apply_excised_committed(
        &self,
        transaction: &crate::DurableTransaction,
    ) -> Result<Self, SemanticError> {
        self.apply_committed_with_excision(transaction, true)
    }

    fn apply_committed_with_excision(
        &self,
        transaction: &crate::DurableTransaction,
        allow_dangling_retractions: bool,
    ) -> Result<Self, SemanticError> {
        let t = self.basis_t.checked_add(1).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/basis-overflow",
                "database basis cannot advance beyond u64",
            )
        })?;
        let tx = t_to_tx(t).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/basis-out-of-range",
                format!("database basis cannot be represented as a transaction id: {error}"),
            )
        })?;
        if transaction.basis_t != t {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/noncontiguous-basis",
                format!(
                    "expected transaction basis {t}, got {}",
                    transaction.basis_t
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

        let mut logical = Vec::with_capacity(transaction.tx_data.len());
        let mut seen = StoredFactIndex::default();
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
            if seen.contains(datom.entity, datom.attribute, &datom.value) {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/duplicate-datom",
                    "committed transaction contains duplicate or contradictory datoms",
                ));
            }
            seen.insert(datom.entity, datom.attribute, &datom.value, logical.len());
            logical.push(LogicalDatom {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                added: datom.added,
            });
        }
        drop(seen);

        // Hook assertions are transaction events, not merely current facts.
        // Validate them before the materiality check so malformed explicit
        // hooks fail by the same rules as pure assessment. In particular, a
        // repeated :db.alter/attribute assertion is a legal (and necessary)
        // event even though the same E/A/V is already current.
        self.validate_committed_schema_hooks(&logical)
            .map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/invalid-schema-hooks",
                    format!("committed schema hooks are invalid: {error}"),
                )
            })?;
        self.validate_exact_committed_excision_bootstrap_upgrade(
            &logical,
            &transaction.tx_data,
            tx_instant,
        )?;
        let before_index = StoredFactIndex::from_current(&self.current);
        for datom in &transaction.tx_data {
            let existed = before_index.contains(datom.entity, datom.attribute, &datom.value);
            if existed == datom.added
                && !(allow_dangling_retractions && !datom.added && !existed)
                && !(datom.added && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32)
            {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/nonmaterial-datom",
                    "committed datom is not a material change from db-before",
                ));
            }
        }
        drop(before_index);

        let mut final_current = apply_logical(&self.current, &logical, tx);
        validate_excision_requests(&final_current)?;
        let proposed_history: Vec<_> = self
            .history_datoms()
            .chain(transaction.tx_data.iter())
            .cloned()
            .collect();
        let successor_idents = IdentIndex::derive(proposed_history.iter(), DB_IDENT as u32)?;
        let successor_current = facts_as_datoms(&final_current);
        let derived_schema = Arc::new(Schema::derive_from_information(
            &successor_current,
            &successor_idents,
        )?);
        let recovered_changes = self
            .schema_changes_to(&derived_schema, &final_current, &logical)
            .map_err(|error| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/invalid-schema-transition",
                    format!("committed schema transition is invalid: {error}"),
                )
            })?;
        let expected_frontier = expected_frontier_after_commit(
            self.eidx_frontier,
            t,
            &transaction.tempids,
            &recovered_changes,
        )?;
        if transaction.eidx_frontier != expected_frontier {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/eidx-frontier-mismatch",
                format!(
                    "expected issued entity-index frontier {expected_frontier}, got {}",
                    transaction.eidx_frontier
                ),
            ));
        }
        validate_cardinality(&derived_schema, &final_current)?;
        validate_uniqueness(&derived_schema, &final_current)?;
        if derived_schema.as_ref() != self.schema.as_ref() {
            // Prior values were validated under the predecessor schema. A
            // schema transition must additionally validate all retained values
            // under its successor (including affected tuple definitions).
            for fact in &final_current {
                let attribute = derived_schema.attribute(fact.attribute)?;
                derived_schema.validate_value(attribute, &fact.value)?;
            }
        }
        // A validated predecessor already establishes these bounds for its
        // immutable genesis/history. Check every new stored datum here, even
        // a dangling excision retraction; this is input validation, not an
        // optional internal consistency audit. The enclosing tx identity and
        // positive contiguous basis were checked above.
        for datom in &transaction.tx_data {
            validate_stored_entity(datom.entity, transaction.eidx_frontier, t)?;
            validate_stored_value(&datom.value, transaction.eidx_frontier, t)?;
        }
        final_current.sort_by(compare_current);

        let mut db_after = self.clone();
        db_after.schema = derived_schema;
        db_after.idents = Arc::new(successor_idents);
        db_after.basis_t = t;
        db_after.eidx_frontier = transaction.eidx_frontier;
        db_after.last_tx_instant = Some(tx_instant);
        let current_datoms = facts_as_datoms(&final_current);
        db_after.current = final_current.into();
        let mut history_chunks: Vec<_> = self.history.iter().cloned().collect();
        history_chunks.push(Arc::from(transaction.tx_data.clone()));
        db_after.history = history_chunks.into();
        db_after.current_indexes = IndexRoots::build(&db_after.schema, current_datoms);
        db_after.history_indexes =
            IndexRoots::build(&db_after.schema, db_after.history_datoms().cloned());
        let (semantic_state, semantic_commitment_work) =
            self.semantic_state.advance(self, &transaction.tx_data)?;
        db_after.semantic_state = semantic_state;
        db_after.semantic_commitment_work = semantic_commitment_work;
        // The predecessor is an already validated native value, and every
        // newly admitted datum and semantic transition has been checked above.
        // Derived indexes/schema/idents and the incremental commitment are
        // trusted constructor outputs. Independently rebuilding all of them
        // and replaying all history after EACH prefix would turn an ordered
        // recovery into repeated full database audits. Explicit deep-recovery
        // boundaries call the unchanged public `validate_invariants` audit;
        // callers also retain their authoritative state/hash comparisons.
        Ok(db_after)
    }

    pub(crate) fn with_function_context(
        &self,
        ops: &[TxOp],
        functions: &crate::TxFunctions,
        tx_instant: i64,
    ) -> Result<TxReport, SemanticError> {
        self.assess_context(ops, tx_instant)?
            .validate(Some(functions))
    }

    fn assess_context(
        &self,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<AssessedTransaction, SemanticError> {
        crate::transaction::validate_ops_input(ops)?;
        if let Some(previous) = self.last_tx_instant
            && tx_instant < previous
        {
            return Err(SemanticError::incorrect(
                "transaction/non-monotonic-instant",
                format!("transaction instant {tx_instant} precedes {previous}"),
            ));
        }

        let t = self.basis_t.checked_add(1).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/basis-overflow",
                "database basis cannot advance beyond u64",
            )
        })?;
        let tx = t_to_tx(t)?;
        let allocation_start = self.eidx_frontier.max(t.checked_add(1).ok_or_else(|| {
            SemanticError::incorrect(
                "transaction/basis-overflow",
                "transaction time cannot advance the issued frontier",
            )
        })?);
        validate_frontier(allocation_start)?;
        let mut ordered_ops = ops.to_vec();
        ordered_ops.sort_by(crate::transaction::compare_tx_op);
        self.validate_tx_instant_forms(&ordered_ops, tx_instant)?;

        // Typed schema forms are syntax only. Lower them to ordinary
        // self-describing datoms before assessment; all non-schema operation
        // resolution below still uses db-before, matching ProcessInpoint.
        let (schema_logical, _schema_changes, allocation_start) =
            self.prepare_schema_information(&ordered_ops, tx, allocation_start)?;

        let (tempids, eidx_frontier) = self.resolve_tempids(&ordered_ops, tx, allocation_start)?;
        let mut logical = schema_logical;
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
        ensures.sort_by_key(|ensure| (ensure.entity, ensure.spec));
        ensures.dedup_by_key(|ensure| (ensure.entity, ensure.spec));

        dedupe(&mut logical);
        validate_same_transaction(&self.schema, &logical)?;
        add_cardinality_one_retractions(&self.schema, &self.current, &mut logical)?;
        dedupe(&mut logical);
        validate_same_transaction(&self.schema, &logical)?;

        // Recovered `filter-assess-tx-datoms` discovers missing schema hooks
        // only after ordinary schema datoms have passed redundancy and
        // cardinality assessment. Derive events from material property
        // changes here so a no-op reassertion cannot manufacture history.
        self.synthesize_schema_hooks(&mut logical)?;
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
        let mut final_current = apply_logical(&self.current, &logical, tx);
        validate_excision_requests(&final_current)?;
        let mut tx_data = material_changes(&self.current, &final_current, &logical, tx);
        tx_data.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let proposed_history: Vec<_> = self
            .history_datoms()
            .chain(tx_data.iter())
            .cloned()
            .collect();
        let successor_idents = IdentIndex::derive(proposed_history.iter(), DB_IDENT as u32)?;
        let successor_current = facts_as_datoms(&final_current);
        let successor_schema = Arc::new(Schema::derive_from_information(
            &successor_current,
            &successor_idents,
        )?);
        let _actual_schema_changes =
            self.schema_changes_to(&successor_schema, &final_current, &logical)?;

        // Hooks judge the complete proposed database. A newly installed
        // attribute was unavailable during expansion, while its validated
        // descriptor is immediately operative on db-after.
        validate_cardinality(&successor_schema, &final_current)?;
        validate_uniqueness(&successor_schema, &final_current)?;
        final_current.sort_by(compare_current);

        let mut db_after = self.clone();
        db_after.basis_t = t;
        db_after.last_tx_instant = Some(tx_instant);
        db_after.schema = successor_schema;
        db_after.idents = Arc::new(successor_idents);
        db_after.eidx_frontier = eidx_frontier;
        let current_datoms = facts_as_datoms(&final_current);
        db_after.current = final_current.into();
        let mut history_chunks: Vec<_> = self.history.iter().cloned().collect();
        history_chunks.push(Arc::from(tx_data.clone()));
        db_after.history = history_chunks.into();
        db_after.current_indexes = IndexRoots::build(&db_after.schema, current_datoms);
        db_after.history_indexes =
            IndexRoots::build(&db_after.schema, db_after.history_datoms().cloned());
        let (semantic_state, semantic_commitment_work) =
            self.semantic_state.advance(self, &tx_data)?;
        db_after.semantic_state = semantic_state;
        db_after.semantic_commitment_work = semantic_commitment_work;

        // Keep the pure transition and durable replay on one acceptance
        // boundary. This is intentionally an internal cross-check, not a
        // second mutation path.
        db_after.validate_invariants()?;

        Ok(AssessedTransaction {
            report: TxReport {
                db_before: self.clone(),
                db_after,
                tx_data,
                tempids,
            },
            ensures,
        })
    }

    fn synthesize_schema_hooks(
        &self,
        logical: &mut Vec<LogicalDatom>,
    ) -> Result<(), SemanticError> {
        self.validate_schema_hook_events(logical)?;
        let touched = self.material_schema_hook_targets(logical)?;

        // Explicit hooks are accepted as transaction syntax, but the
        // assessor owns their canonical event set. This prevents a lone
        // :db.alter/attribute assertion from surviving without the material
        // controlled-property change that gives it meaning.
        logical.retain(|datom| {
            datom.attribute != crate::DB_INSTALL_ATTRIBUTE as u32
                && datom.attribute != crate::DB_ALTER_ATTRIBUTE as u32
        });

        for target in touched {
            let attribute = crate::schema_eid_to_attr_id(target)?;
            logical.push(LogicalDatom {
                entity: crate::DB_PART_DB,
                attribute: if self.schema.attribute(attribute).is_ok() {
                    crate::DB_ALTER_ATTRIBUTE as u32
                } else {
                    crate::DB_INSTALL_ATTRIBUTE as u32
                },
                value: Value::Ref(target),
                added: true,
            });
        }
        Ok(())
    }

    /// Validate explicit schema hook events. Both pure assessment and durable
    /// recovery use this routine so hook shape, target, and kind have one
    /// definition.
    fn validate_schema_hook_events(
        &self,
        logical: &[LogicalDatom],
    ) -> Result<BTreeMap<u64, u32>, SemanticError> {
        let mut explicit = BTreeMap::<u64, u32>::new();
        for datom in logical.iter().filter(|datom| {
            datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                || datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
        }) {
            if !datom.added || datom.entity != crate::DB_PART_DB {
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
            let installed = self.schema.attribute(attribute).is_ok();
            let expected = if installed {
                crate::DB_ALTER_ATTRIBUTE as u32
            } else {
                crate::DB_INSTALL_ATTRIBUTE as u32
            };
            if datom.attribute != expected {
                return Err(SemanticError::incorrect(
                    "schema/wrong-hook-kind",
                    if installed {
                        "an installed attribute must use :db.alter/attribute"
                    } else {
                        "a new attribute must use :db.install/attribute"
                    },
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

        Ok(explicit)
    }

    /// Hook discovery runs on the same materiality boundary as ordinary
    /// transaction data. Merely mentioning a controlled metadata attribute
    /// is insufficient: its E/A/V membership must differ from db-before.
    fn material_schema_hook_targets(
        &self,
        logical: &[LogicalDatom],
    ) -> Result<BTreeSet<u64>, SemanticError> {
        let mut touched = BTreeSet::new();
        for datom in logical
            .iter()
            .filter(|datom| is_attribute_hook_property(datom.attribute))
        {
            crate::schema_eid_to_attr_id(datom.entity)?;
            let existed = contains_fact(&self.current, datom.entity, datom.attribute, &datom.value);
            if existed != datom.added {
                touched.insert(datom.entity);
            }
        }
        Ok(touched)
    }

    /// Recovery cannot synthesize information that was absent from the
    /// committed transaction. Every material hook-controlled schema property
    /// therefore has to carry the install/alter event that pure assessment
    /// would have synthesized before publication.
    fn validate_committed_schema_hooks(
        &self,
        logical: &[LogicalDatom],
    ) -> Result<(), SemanticError> {
        let explicit = self.validate_schema_hook_events(logical)?;
        let touched = self.material_schema_hook_targets(logical)?;
        for target in &touched {
            if !explicit.contains_key(target) {
                return Err(SemanticError::incorrect(
                    "schema/missing-hook",
                    format!(
                        "schema entity {target} changes a hook-controlled property without its install/alter event"
                    ),
                ));
            }
        }
        for target in explicit.keys() {
            if !touched.contains(target) {
                return Err(SemanticError::incorrect(
                    "schema/orphan-hook",
                    format!(
                        "schema entity {target} has an install/alter event without a material hook-controlled property change"
                    ),
                ));
            }
        }
        Ok(())
    }

    fn validate_exact_committed_excision_bootstrap_upgrade(
        &self,
        logical: &[LogicalDatom],
        committed: &[Datom],
        tx_instant: i64,
    ) -> Result<(), SemanticError> {
        const IDS: [u64; 4] = [
            DB_EXCISE,
            DB_EXCISE_ATTRS,
            DB_EXCISE_BEFORE_T,
            DB_EXCISE_BEFORE,
        ];
        let touches_upgrade = logical.iter().any(|datom| {
            IDS.contains(&datom.entity)
                || ((datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                    || datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32)
                    && matches!(datom.value, Value::Ref(target) if IDS.contains(&target)))
        });
        if !touches_upgrade || self.schema.attribute(DB_EXCISE as u32).is_ok() {
            return Ok(());
        }

        let ops = supported_system_attributes()
            .into_iter()
            .filter(|attribute| IDS.contains(&u64::from(attribute.id)))
            .map(TxOp::InstallAttribute)
            .collect::<Vec<_>>();
        let expected = self.with(&ops, tx_instant).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/invalid-excision-bootstrap-upgrade",
                format!("cannot construct canonical excision bootstrap upgrade: {error}"),
            )
        })?;
        if !same_stored_datoms(committed, &expected.tx_data) {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/noncanonical-excision-bootstrap-upgrade",
                "reserved excision vocabulary must be installed by its exact canonical transaction",
            ));
        }
        Ok(())
    }

    fn validate_attribute_predicates_with(
        &self,
        schema: &Schema,
        assessed: &[Datom],
        functions: Option<&crate::TxFunctions>,
    ) -> Result<(), SemanticError> {
        for datom in assessed.iter().filter(|datom| datom.added) {
            let attribute = schema.attribute(datom.attribute)?;
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
        Ok(())
    }

    fn prepare_schema_information(
        &self,
        ops: &[TxOp],
        tx: u64,
        mut allocation_frontier: u64,
    ) -> Result<(Vec<LogicalDatom>, Vec<SchemaChange>, u64), SemanticError> {
        let mut seen = BTreeSet::new();
        let mut installed = Vec::new();
        let mut candidate = self.schema.as_ref().clone();
        let mut changes = Vec::new();
        let excision_bootstrap_upgrade = self.is_exact_excision_bootstrap_ops(ops);

        // First form the complete descriptor set. This temporary Rust value
        // is only a lowering aid for composite names; it is discarded and the
        // operative successor is independently re-derived from datoms below.
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
                if reserved
                    && !(excision_bootstrap_upgrade
                        && matches!(
                            u64::from(attribute.id),
                            DB_EXCISE | DB_EXCISE_ATTRS | DB_EXCISE_BEFORE_T | DB_EXCISE_BEFORE
                        ))
                {
                    return Err(SemanticError::incorrect(
                        "schema/reserved-system-entity",
                        format!(
                            "entity {} is reserved by the native system vocabulary",
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
                changes.push(SchemaChange::Install(attribute.clone()));
            } else {
                candidate.alter(attribute.clone())?;
                changes.push(SchemaChange::Alter(attribute.clone()));
            }
        }
        candidate.validate_tuple_installations(&installed)?;

        let metadata_attributes = [
            DB_IDENT as u32,
            crate::DB_VALUE_TYPE as u32,
            crate::DB_CARDINALITY as u32,
            crate::DB_UNIQUE as u32,
            crate::DB_IS_COMPONENT as u32,
            crate::DB_INDEX as u32,
            crate::DB_NO_HISTORY as u32,
            crate::DB_TUPLE_TYPE as u32,
            crate::DB_TUPLE_TYPES as u32,
            crate::DB_TUPLE_ATTRS as u32,
            crate::DB_ATTR_PREDS as u32,
            crate::DB_TUPLE_DISCONTINUED as u32,
        ];
        let mut logical = Vec::new();
        for change in &changes {
            let (attribute, install) = match change {
                SchemaChange::Install(attribute) => (attribute, true),
                SchemaChange::Alter(attribute) => (attribute, false),
            };
            let desired = crate::schema::attribute_information_datoms(attribute, &candidate, tx)?;
            let current = if install {
                None
            } else {
                Some(self.schema.attribute(attribute.id)?)
            };
            let property_changed = |metadata_attribute| {
                install
                    || current.is_some_and(|current| {
                        attribute_property_changed(current, attribute, metadata_attribute)
                    })
            };

            // Typed forms are only convenience syntax. Lower alterations to
            // the same set-difference of ordinary metadata facts that a user
            // could submit directly. Comparing semantic tuple descriptors,
            // rather than their ident encoding, also avoids rewriting an
            // immutable composite merely because a constituent was renamed.
            for fact in self.current.iter().filter(|fact| {
                fact.entity == u64::from(attribute.id)
                    && metadata_attributes.contains(&fact.attribute)
                    && property_changed(fact.attribute)
            }) {
                if !desired.iter().any(|datom| {
                    datom.entity == fact.entity
                        && datom.attribute == fact.attribute
                        && datom.value.stored_eq(&fact.value)
                }) {
                    logical.push(LogicalDatom {
                        entity: fact.entity,
                        attribute: fact.attribute,
                        value: fact.value.clone(),
                        added: false,
                    });
                }
            }
            logical.extend(desired.into_iter().filter_map(|datom| {
                if property_changed(datom.attribute)
                    && !contains_fact(&self.current, datom.entity, datom.attribute, &datom.value)
                {
                    Some(LogicalDatom {
                        entity: datom.entity,
                        attribute: datom.attribute,
                        value: datom.value,
                        added: true,
                    })
                } else {
                    None
                }
            }));
        }
        changes.sort_by_key(|change| match change {
            SchemaChange::Install(attribute) | SchemaChange::Alter(attribute) => attribute.id,
        });
        validate_frontier(allocation_frontier)?;
        Ok((logical, changes, allocation_frontier))
    }

    fn is_exact_excision_bootstrap_ops(&self, ops: &[TxOp]) -> bool {
        const IDS: [u32; 4] = [
            DB_EXCISE as u32,
            DB_EXCISE_ATTRS as u32,
            DB_EXCISE_BEFORE_T as u32,
            DB_EXCISE_BEFORE as u32,
        ];
        if !same_stored_datoms(&self.genesis, &pre_excision_genesis_datoms()) || ops.len() != 4 {
            return false;
        }
        IDS.iter().all(|id| {
            let expected = supported_system_attributes()
                .into_iter()
                .find(|attribute| attribute.id == *id)
                .expect("every excision bootstrap id has a descriptor");
            ops.iter()
                .any(|op| matches!(op, TxOp::InstallAttribute(attribute) if attribute == &expected))
        })
    }

    fn resolve_tempids(
        &self,
        ops: &[TxOp],
        tx: u64,
        allocation_start: u64,
    ) -> Result<(BTreeMap<String, u64>, u64), SemanticError> {
        let names = validated_entity_tempids(ops)?;
        let names: Vec<_> = names.into_iter().collect();
        let positions: BTreeMap<_, _> = names
            .iter()
            .enumerate()
            .map(|(index, name)| (name.clone(), index))
            .collect();
        let mut union = UnionFind::new(names.len());
        let mut identities: Vec<(usize, u32, UpsertIdentityValue, Unique)> = Vec::new();

        for op in ops {
            let TxOp::Add {
                entity: EntityRef::Temp(name),
                attribute,
                value,
            } = op
            else {
                continue;
            };
            let schema = self.schema.attribute(*attribute)?;
            let Some(unique) = schema.unique else {
                continue;
            };
            let value = self.resolve_upsert_identity_value(*attribute, value, tx)?;
            if value.is_nan() {
                return Err(SemanticError::incorrect(
                    "transaction/nan-cannot-identify",
                    "NaN cannot participate in upsert or uniqueness",
                ));
            }
            identities.push((positions[name], *attribute, value, unique));
        }

        for left in 0..identities.len() {
            for right in left + 1..identities.len() {
                let (left_temp, left_attr, left_value, left_unique) = &identities[left];
                let (right_temp, right_attr, right_value, right_unique) = &identities[right];
                if *left_unique == Unique::Identity
                    && *right_unique == Unique::Identity
                    && left_attr == right_attr
                    && left_value.same_key(right_value)
                {
                    union.join(*left_temp, *right_temp);
                }
            }
        }

        let mut existing_by_root: BTreeMap<usize, u64> = BTreeMap::new();
        for (temp, attribute, value, unique) in &identities {
            let Some(value) = value.resolved() else {
                continue;
            };
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

        let mut next = allocation_start;
        let mut allocated_by_root = BTreeMap::new();
        let mut result = BTreeMap::new();
        for (index, name) in names.iter().enumerate() {
            let root = union.root(index);
            let id = if let Some(existing) = existing_by_root.get(&root) {
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
            result.insert(name.clone(), id);
        }
        validate_frontier(next)?;
        Ok((result, next))
    }

    fn resolve_upsert_identity_value(
        &self,
        attribute: u32,
        value: &TxValue,
        tx: u64,
    ) -> Result<UpsertIdentityValue, SemanticError> {
        let descriptor = self.schema.attribute(attribute)?;
        match value {
            TxValue::Tuple(slots) => {
                for value in slots.iter().flatten() {
                    if let TxValue::Scalar(value) = value {
                        self.validate_explicit_value_refs(value)?;
                    }
                }
                resolve_tuple_input(&self.schema, descriptor, slots, |entity| match entity {
                    EntityRef::Temp(name) => Ok(UpsertIdentityValue::TempRef(name.clone())),
                    entity => self
                        .resolve_entity(entity, tx, &BTreeMap::new())
                        .map(|entity| UpsertIdentityValue::Resolved(Value::Ref(entity))),
                })
            }
            TxValue::Scalar(value) => {
                self.validate_explicit_value_refs(value)?;
                self.schema.validate_value(descriptor, value)?;
                Ok(UpsertIdentityValue::Resolved(value.clone()))
            }
            TxValue::Entity(EntityRef::Temp(name)) if descriptor.value_type == ValueType::Ref => {
                Ok(UpsertIdentityValue::TempRef(name.clone()))
            }
            TxValue::Entity(entity) if descriptor.value_type == ValueType::Ref => {
                let value = Value::Ref(self.resolve_entity(entity, tx, &BTreeMap::new())?);
                self.schema.validate_value(descriptor, &value)?;
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
                validate_entity_predicate_name(*attribute, &value)?;
                if *attribute == crate::DB_ENSURE as u32 {
                    let Value::Ref(spec) = value else {
                        return Err(SemanticError::incorrect(
                            "transaction/invalid-ensure",
                            ":db/ensure must name an entity spec",
                        ));
                    };
                    ensures.push(self.resolve_entity_spec(entity, spec)?);
                    return Ok(());
                }
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
                if *attribute == crate::DB_ENSURE as u32 {
                    return Err(SemanticError::incorrect(
                        "transaction/virtual-ensure",
                        ":db/ensure is virtual and cannot be retracted",
                    ));
                }
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
                if *attribute == crate::DB_ENSURE as u32 {
                    return Err(SemanticError::incorrect(
                        "transaction/virtual-ensure",
                        ":db/ensure is virtual and cannot be compared and swapped",
                    ));
                }
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
                validate_entity_predicate_name(*attribute, &value)?;
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
                if eid_to_part(entity)? == TX_PARTITION {
                    return Err(SemanticError::incorrect(
                        "transaction/reset-tx-instant",
                        "transaction entities cannot be retracted",
                    ));
                }
                let mut visited = BTreeSet::new();
                self.expand_retract_entity(entity, logical, touched_constituents, &mut visited)?;
            }
            TxOp::Ensure { entity, spec } => {
                let entity = self.resolve_entity(entity, tx, tempids)?;
                let spec = self.resolve_entity(spec, tx, tempids)?;
                ensures.push(self.resolve_entity_spec(entity, spec)?);
            }
            TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
        }
        Ok(())
    }

    fn resolve_entity_spec(&self, entity: u64, spec: u64) -> Result<EnsureCheck, SemanticError> {
        let mut required = Vec::new();
        for value in self.values(spec, crate::DB_ENTITY_ATTRS as u32) {
            let Value::Keyword(ident) = value else {
                return Err(SemanticError::incorrect(
                    "transaction/invalid-entity-spec",
                    ":db.entity/attrs values must be attribute keywords",
                ));
            };
            let attribute = self.entid(ident).ok_or_else(|| {
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
        for value in self.values(spec, crate::DB_ENTITY_PREDS as u32) {
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
        Ok(EnsureCheck {
            entity,
            spec,
            required,
            predicates,
        })
    }

    fn validate_tx_instant_forms(
        &self,
        ops: &[TxOp],
        selected_instant: i64,
    ) -> Result<(), SemanticError> {
        let attribute = DB_TX_INSTANT as u32;
        let mut explicit = None;
        for op in ops {
            match op {
                TxOp::Add {
                    entity,
                    attribute: candidate,
                    value,
                } if *candidate == attribute => {
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
                TxOp::Retract {
                    attribute: candidate,
                    ..
                }
                | TxOp::Cas {
                    attribute: candidate,
                    ..
                } if *candidate == attribute => {
                    return Err(SemanticError::incorrect(
                        "transaction/reset-tx-instant",
                        ":db/txInstant cannot be retracted or changed",
                    ));
                }
                _ => {}
            }
        }
        if explicit.is_some_and(|instant| instant != selected_instant) {
            return Err(SemanticError::incorrect(
                "transaction/tx-instant-mismatch",
                "explicit :db/txInstant differs from the transactor-selected instant",
            ));
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
            EntityRef::Id(id) => {
                self.validate_explicit_entity_id(*id)?;
                Ok(*id)
            }
            EntityRef::Ident(ident) => self.entid(ident).ok_or_else(|| {
                SemanticError::incorrect(
                    "transaction/unknown-ident",
                    format!("unknown ident {}", ident.qualified_name()),
                )
            }),
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
            EntityRef::LookupInput { attribute, value } => self
                .database_value()
                .resolve_lookup_input(*attribute, value)?
                .ok_or_else(|| {
                    SemanticError::incorrect(
                        "transaction/lookup-not-found",
                        "lookup ref did not resolve in db-before",
                    )
                }),
            EntityRef::Tx => Ok(tx),
        }
    }

    fn validate_explicit_entity_id(&self, entity: u64) -> Result<(), SemanticError> {
        validate_supported_eid(entity)?;
        let eidx = eid_to_eidx(entity)?;
        if eidx >= self.eidx_frontier {
            return Err(SemanticError::incorrect(
                "transaction/invalid-entity-id",
                format!(
                    "entity id {entity} has unissued index {eidx}; issued indexes are below {}",
                    self.eidx_frontier
                ),
            ));
        }
        Ok(())
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
            (ValueType::Tuple, TxValue::Tuple(slots)) => {
                for value in slots.iter().flatten() {
                    if let TxValue::Scalar(value) = value {
                        self.validate_explicit_value_refs(value)?;
                    }
                }
                resolve_tuple_input(&self.schema, schema, slots, |entity| {
                    self.resolve_entity(entity, tx, tempids)
                        .map(|entity| UpsertIdentityValue::Resolved(Value::Ref(entity)))
                })?
                .resolved()
                .expect("allocated tuple has no symbolic references")
                .clone()
            }
            (ValueType::Ref, TxValue::Entity(entity)) => {
                Value::Ref(self.resolve_entity(entity, tx, tempids)?)
            }
            (_, TxValue::Scalar(value)) => {
                self.validate_explicit_value_refs(value)?;
                value.clone()
            }
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

    fn validate_explicit_value_refs(&self, value: &Value) -> Result<(), SemanticError> {
        match value {
            Value::Ref(entity) => self.validate_explicit_entity_id(*entity),
            Value::Tuple(slots) => {
                for value in slots.iter().flatten() {
                    self.validate_explicit_value_refs(value)?;
                }
                Ok(())
            }
            _ => Ok(()),
        }
    }

    fn is_derived_composite(&self, attribute: u32) -> Result<bool, SemanticError> {
        let attribute = self.schema.attribute(attribute)?;
        Ok(matches!(attribute.tuple, Some(TupleSpec::Composite(_)))
            && !attribute.tuple_discontinued)
    }
}

fn validate_entity_predicate_name(attribute: u32, value: &Value) -> Result<(), SemanticError> {
    if attribute != crate::DB_ENTITY_PREDS as u32 {
        return Ok(());
    }
    let Value::Symbol(symbol) = value else {
        // The ordinary schema value-type check owns this diagnostic.
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

/// Match recovered `ProcessExpander`: permanent ids are assigned to tempids in
/// E position. A reference tempid may be used in V only when another
/// normalized datom establishes it as an entity; otherwise `replace_tempid`
/// raises `:db.error/tempid-not-an-entity` (`db.clj:7376-7390`). Running this
/// after form normalization also covers tempids introduced by nested maps.
pub(crate) fn validated_entity_tempids(ops: &[TxOp]) -> Result<BTreeSet<String>, SemanticError> {
    let mut entities = BTreeSet::new();
    let mut values = BTreeSet::new();
    for op in ops {
        collect_tempids_op(op, &mut entities, &mut values);
    }
    if let Some(tempid) = values.difference(&entities).next() {
        return Err(SemanticError::incorrect(
            "transaction/tempid-not-an-entity",
            format!("tempid '{tempid}' is used only as a value in transaction data"),
        )
        .detail("tempid", tempid.clone()));
    }
    Ok(entities)
}

fn collect_tempids_op(op: &TxOp, entities: &mut BTreeSet<String>, values: &mut BTreeSet<String>) {
    match op {
        TxOp::Add { entity, value, .. } => {
            collect_tempids_entity(entity, entities);
            collect_tempids_value(value, values);
        }
        TxOp::Retract { entity, value, .. } => {
            collect_tempids_entity(entity, entities);
            if let Some(value) = value {
                collect_tempids_value(value, values);
            }
        }
        TxOp::Cas {
            entity, old, new, ..
        } => {
            collect_tempids_entity(entity, entities);
            if let Some(old) = old {
                collect_tempids_value(old, values);
            }
            collect_tempids_value(new, values);
        }
        TxOp::RetractEntity(entity) => collect_tempids_entity(entity, entities),
        TxOp::Ensure { entity, spec } => {
            collect_tempids_entity(entity, entities);
            collect_tempids_entity(spec, values);
        }
        TxOp::InstallAttribute(_) | TxOp::AlterAttribute(_) => {}
    }
}

fn collect_tempids_value(value: &TxValue, output: &mut BTreeSet<String>) {
    match value {
        TxValue::Entity(entity) => collect_tempids_entity(entity, output),
        TxValue::Tuple(slots) => {
            for value in slots.iter().flatten() {
                if let TxValue::Entity(entity) = value {
                    collect_tempids_entity(entity, output);
                }
            }
        }
        TxValue::Scalar(_) => {}
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

fn is_attribute_hook_property(attribute: u32) -> bool {
    matches!(
        u64::from(attribute),
        crate::DB_VALUE_TYPE
            | crate::DB_CARDINALITY
            | crate::DB_UNIQUE
            | crate::DB_IS_COMPONENT
            | crate::DB_INDEX
            | crate::DB_NO_HISTORY
            | crate::DB_TUPLE_TYPE
            | crate::DB_TUPLE_TYPES
            | crate::DB_TUPLE_ATTRS
            | crate::DB_ATTR_PREDS
            | crate::DB_TUPLE_DISCONTINUED
    )
}

fn attribute_property_changed(
    current: &crate::Attribute,
    proposed: &crate::Attribute,
    attribute: u32,
) -> bool {
    match u64::from(attribute) {
        crate::DB_IDENT => current.ident != proposed.ident,
        crate::DB_VALUE_TYPE => current.value_type != proposed.value_type,
        crate::DB_CARDINALITY => current.cardinality != proposed.cardinality,
        crate::DB_UNIQUE => current.unique != proposed.unique,
        crate::DB_IS_COMPONENT => current.component != proposed.component,
        crate::DB_INDEX => current.indexed != proposed.indexed,
        crate::DB_NO_HISTORY => current.no_history != proposed.no_history,
        crate::DB_TUPLE_TYPE | crate::DB_TUPLE_TYPES | crate::DB_TUPLE_ATTRS => {
            current.tuple != proposed.tuple
        }
        crate::DB_ATTR_PREDS => current.predicates != proposed.predicates,
        crate::DB_TUPLE_DISCONTINUED => current.tuple_discontinued != proposed.tuple_discontinued,
        _ => false,
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

fn validate_same_transaction(
    schema: &Schema,
    datoms: &[LogicalDatom],
) -> Result<(), SemanticError> {
    for (index, left) in datoms.iter().enumerate() {
        schema.attribute(left.attribute)?;
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
    let mut index = StoredFactIndex::from_current(current);
    let mut result: Vec<_> = current.iter().cloned().map(Some).collect();
    for datom in datoms.iter().filter(|datom| !datom.added) {
        for position in index.remove(datom.entity, datom.attribute, &datom.value) {
            result[position] = None;
        }
    }
    for datom in datoms.iter().filter(|datom| datom.added) {
        if let Some(position) = index.first(datom.entity, datom.attribute, &datom.value) {
            // Attribute-alter hooks are deliberately non-redundant events.
            // The current logical coordinate follows the newest such event,
            // while history retains every immutable hook assertion.
            if u64::from(datom.attribute) == crate::DB_ALTER_ATTRIBUTE {
                result[position]
                    .as_mut()
                    .expect("indexed fact is present")
                    .tx = tx;
            }
        } else {
            index.insert(datom.entity, datom.attribute, &datom.value, result.len());
            result.push(Some(CurrentFact {
                entity: datom.entity,
                attribute: datom.attribute,
                value: datom.value.clone(),
                tx,
            }));
        }
    }
    result.into_iter().flatten().collect()
}

fn validate_cardinality(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
    let mut seen = BTreeSet::new();
    let duplicate_coordinates: BTreeSet<_> = facts
        .iter()
        .filter_map(|fact| {
            let coordinate = (fact.entity, fact.attribute);
            (!seen.insert(coordinate)).then_some(coordinate)
        })
        .collect();
    // Retain the original traversal/error precedence, including unknown schema
    // attributes before or after the first conflicting coordinate.
    for left in facts {
        if schema.attribute(left.attribute)?.cardinality != Cardinality::One {
            continue;
        }
        if duplicate_coordinates.contains(&(left.entity, left.attribute)) {
            return Err(SemanticError::conflict(
                "transaction/cardinality-one-conflict",
                "resulting database has multiple cardinality-one values",
            ));
        }
    }
    Ok(())
}

fn validate_uniqueness(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
    let unique_attributes: BTreeSet<_> = schema
        .attributes()
        .filter(|attribute| attribute.unique.is_some())
        .map(|attribute| attribute.id)
        .collect();
    let mut holders = BTreeMap::new();
    for fact in facts
        .iter()
        .filter(|fact| unique_attributes.contains(&fact.attribute))
    {
        let (first, multiple) = holders
            .entry((fact.attribute, IndexValue(&fact.value)))
            .or_insert((fact.entity, false));
        *multiple |= *first != fact.entity;
    }
    for left in facts {
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
        if holders[&(left.attribute, IndexValue(&left.value))].1 {
            return Err(SemanticError::conflict(
                "transaction/unique-conflict",
                "resulting database has multiple holders of a unique value",
            ));
        }
    }
    Ok(())
}

fn validate_excision_requests(facts: &[CurrentFact]) -> Result<(), SemanticError> {
    let request_entities: BTreeSet<_> = facts
        .iter()
        .filter(|fact| fact.attribute == DB_EXCISE as u32)
        .map(|fact| fact.entity)
        .collect();
    for entity in request_entities {
        let before_t = facts
            .iter()
            .find(|fact| fact.entity == entity && fact.attribute == DB_EXCISE_BEFORE_T as u32);
        let before = facts
            .iter()
            .find(|fact| fact.entity == entity && fact.attribute == DB_EXCISE_BEFORE as u32);
        if before_t.is_some() && before.is_some() {
            return Err(SemanticError::incorrect(
                "transaction/excision-cutoff-conflict",
                "an excision request may use at most one of :db.excise/beforeT and :db.excise/before",
            ));
        }
        if let Some(fact) = before_t {
            let Value::Long(value) = &fact.value else {
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

pub(crate) fn normalize_excision_before_t(value: i64) -> Result<u64, SemanticError> {
    let encoded = u64::try_from(value).map_err(|_| {
        SemanticError::incorrect(
            "transaction/excision-before-t",
            ":db.excise/beforeT must be a non-negative t or transaction entity id",
        )
    })?;
    let partition = eid_to_part(encoded).map_err(|error| {
        SemanticError::incorrect(
            "transaction/excision-before-t",
            format!(":db.excise/beforeT is outside the entity-id range: {error}"),
        )
    })?;
    match partition {
        crate::DB_PARTITION => {
            if encoded > crate::MAX_EIDX {
                Err(SemanticError::incorrect(
                    "transaction/excision-before-t",
                    ":db.excise/beforeT exceeds the logical transaction range",
                ))
            } else {
                Ok(encoded)
            }
        }
        crate::TX_PARTITION => tx_to_t(encoded).map_err(|error| {
            SemanticError::incorrect(
                "transaction/excision-before-t",
                format!(":db.excise/beforeT contains an invalid transaction id: {error}"),
            )
        }),
        partition => Err(SemanticError::incorrect(
            "transaction/excision-before-t",
            format!(
                ":db.excise/beforeT entity id belongs to partition {partition}, not the transaction partition"
            ),
        )),
    }
}

fn one_excision_value<'a>(
    datoms: &[&'a Datom],
    attribute: u32,
) -> Result<Option<&'a Value>, SemanticError> {
    let values: Vec<_> = datoms
        .iter()
        .filter(|datom| datom.attribute == attribute)
        .map(|datom| &datom.value)
        .collect();
    match values.as_slice() {
        [] => Ok(None),
        [value] => Ok(Some(*value)),
        _ => Err(SemanticError::new(
            ErrorCategory::Fault,
            "excision/frozen-cardinality",
            "frozen excision request violates cardinality-one",
        )),
    }
}

fn validate_uniqueness_for_attribute(
    facts: &[CurrentFact],
    attribute: u32,
) -> Result<(), SemanticError> {
    let mut holders = BTreeMap::new();
    for left in facts {
        if left.attribute != attribute {
            continue;
        }
        if *holders
            .entry(IndexValue(&left.value))
            .or_insert(left.entity)
            != left.entity
        {
            return Err(SemanticError::conflict(
                "schema/unique-change-conflict",
                "current values must be unique before adding uniqueness",
            ));
        }
    }
    Ok(())
}

fn validate_ensures(
    db_after: &Database,
    ensures: &[EnsureCheck],
    functions: Option<&crate::TxFunctions>,
) -> Result<(), SemanticError> {
    validate_ensure_attributes(db_after, ensures)?;
    for ensure in ensures {
        for predicate in &ensure.predicates {
            let result = functions
                .ok_or_else(|| {
                    SemanticError::incorrect(
                        "transaction/missing-predicate-context",
                        format!("entity spec {} requires predicate {predicate}", ensure.spec),
                    )
                })?
                .validate_entity_predicate(predicate, db_after, ensure.entity)?;
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

fn validate_ensure_attributes(
    db_after: &Database,
    ensures: &[EnsureCheck],
) -> Result<(), SemanticError> {
    for ensure in ensures {
        let missing: Vec<_> = ensure
            .required
            .iter()
            .filter(|attribute| db_after.values(ensure.entity, **attribute).is_empty())
            .copied()
            .collect();
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

fn material_changes(
    before: &[CurrentFact],
    after: &[CurrentFact],
    logical: &[LogicalDatom],
    tx: u64,
) -> Vec<Datom> {
    let before_index = StoredFactIndex::from_current(before);
    let after_index = StoredFactIndex::from_current(after);
    logical
        .iter()
        .filter(|datom| {
            let existed_before = before_index.contains(datom.entity, datom.attribute, &datom.value);
            let exists_after = after_index.contains(datom.entity, datom.attribute, &datom.value);
            if datom.added {
                (datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32 || !existed_before)
                    && exists_after
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
        .then_with(|| left.value.stored_cmp(&right.value))
}

fn same_stored_current(left: &[CurrentFact], right: &[CurrentFact]) -> bool {
    left.len() == right.len()
        && left.iter().zip(right).all(|(left, right)| {
            left.entity == right.entity
                && left.attribute == right.attribute
                && left.tx == right.tx
                && left.value.stored_eq(&right.value)
        })
}

fn same_stored_datoms(left: &[Datom], right: &[Datom]) -> bool {
    left.len() == right.len()
        && left.iter().zip(right).all(|(left, right)| {
            left.entity == right.entity
                && left.attribute == right.attribute
                && left.tx == right.tx
                && left.added == right.added
                && left.value.stored_eq(&right.value)
        })
}

fn validate_genesis_information(genesis: &[Datom]) -> Result<(), SemanticError> {
    let genesis_tx = t_to_tx(0).expect("genesis t is representable");
    if genesis.is_empty()
        || genesis
            .iter()
            .any(|datom| datom.tx != genesis_tx || !datom.added)
        || genesis
            .windows(2)
            .any(|pair| !pair[0].cmp_in(&pair[1], IndexOrder::Eavt).is_lt())
    {
        return Err(SemanticError::new(
            ErrorCategory::Fault,
            "kernel/noncanonical-genesis",
            "genesis must be a nonempty, strictly ordered set of t=0 assertions",
        ));
    }
    let current = canonical_genesis_datoms();
    let pre_excision = pre_excision_genesis_datoms();
    let exact_pre_excision = if same_stored_datoms(genesis, &pre_excision) {
        let encoded = crate::encode_genesis(genesis).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "kernel/legacy-genesis-encoding",
                format!("recognized legacy genesis cannot be canonically encoded: {error}"),
            )
        })?;
        crate::sha256(&encoded) == PRE_EXCISION_GENESIS_HASH
    } else {
        false
    };
    if !same_stored_datoms(genesis, &current) && !exact_pre_excision {
        return Err(SemanticError::new(
            ErrorCategory::Fault,
            "kernel/noncanonical-genesis",
            "genesis must equal a recognized exact native system information set",
        ));
    }
    Ok(())
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

/// A borrowed key with the same logical equality as the value indexes. Stored
/// equality cannot itself implement `Ord`: two decimal scales differ, but each
/// can compare stored-equal to a non-decimal number of the same logical value.
#[derive(Clone, Copy)]
struct IndexValue<'a>(&'a Value);

impl PartialEq for IndexValue<'_> {
    fn eq(&self, other: &Self) -> bool {
        self.cmp(other).is_eq()
    }
}

impl Eq for IndexValue<'_> {}

impl PartialOrd for IndexValue<'_> {
    fn partial_cmp(&self, other: &Self) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl Ord for IndexValue<'_> {
    fn cmp(&self, other: &Self) -> Ordering {
        self.0.index_cmp(other.0)
    }
}

type StoredFactKey<'a> = (u64, u32, IndexValue<'a>, Option<i64>);

/// Index original positions, rather than rebuilding facts in key order. This
/// preserves the first stored representation and survivor/insertion order of
/// the simple replay oracle, including redundant adds and alteration hooks.
#[derive(Default)]
struct StoredFactIndex<'a> {
    positions: BTreeMap<StoredFactKey<'a>, Vec<usize>>,
}

impl<'a> StoredFactIndex<'a> {
    fn scale(value: &Value) -> Option<i64> {
        match value {
            Value::BigDec(value) => Some(value.fractional_digit_count()),
            _ => None,
        }
    }

    fn from_current(facts: &'a [CurrentFact]) -> Self {
        let mut index = Self::default();
        for (position, fact) in facts.iter().enumerate() {
            index.insert(fact.entity, fact.attribute, &fact.value, position);
        }
        index
    }

    fn insert(&mut self, entity: u64, attribute: u32, value: &'a Value, position: usize) {
        self.positions
            .entry((entity, attribute, IndexValue(value), Self::scale(value)))
            .or_default()
            .push(position);
    }

    fn contains(&self, entity: u64, attribute: u32, value: &Value) -> bool {
        self.first(entity, attribute, value).is_some()
    }

    fn first(&self, entity: u64, attribute: u32, value: &Value) -> Option<usize> {
        let logical = IndexValue(value);
        if let Some(scale) = Self::scale(value) {
            [None, Some(scale)]
                .into_iter()
                .filter_map(|scale| {
                    self.positions
                        .get(&(entity, attribute, logical, scale))
                        .and_then(|positions| positions.first().copied())
                })
                .min()
        } else {
            // Non-decimal numbers match every decimal scale in the logical
            // group. Schema-valid scalar attributes have a fixed value type;
            // the range also preserves the old behavior for malformed inputs.
            self.positions
                .range(
                    (entity, attribute, logical, None)
                        ..=(entity, attribute, logical, Some(i64::MAX)),
                )
                .filter_map(|(_, positions)| positions.first().copied())
                .min()
        }
    }

    fn remove(&mut self, entity: u64, attribute: u32, value: &'a Value) -> Vec<usize> {
        let logical = IndexValue(value);
        if let Some(scale) = Self::scale(value) {
            [None, Some(scale)]
                .into_iter()
                .filter_map(|scale| self.positions.remove(&(entity, attribute, logical, scale)))
                .flatten()
                .collect()
        } else {
            let keys: Vec<_> = self
                .positions
                .range(
                    (entity, attribute, logical, None)
                        ..=(entity, attribute, logical, Some(i64::MAX)),
                )
                .map(|(key, _)| *key)
                .collect();
            keys.into_iter()
                .filter_map(|key| self.positions.remove(&key))
                .flatten()
                .collect()
        }
    }
}

fn replay<'a>(datoms: impl Iterator<Item = &'a Datom>) -> Vec<CurrentFact> {
    let mut index = StoredFactIndex::default();
    let mut result = Vec::<Option<CurrentFact>>::new();
    for datom in datoms {
        if datom.added {
            if let Some(position) = index.first(datom.entity, datom.attribute, &datom.value) {
                if u64::from(datom.attribute) == crate::DB_ALTER_ATTRIBUTE {
                    result[position]
                        .as_mut()
                        .expect("indexed fact is present")
                        .tx = datom.tx;
                }
            } else {
                index.insert(datom.entity, datom.attribute, &datom.value, result.len());
                result.push(Some(CurrentFact {
                    entity: datom.entity,
                    attribute: datom.attribute,
                    value: datom.value.clone(),
                    tx: datom.tx,
                }));
            }
        } else {
            for position in index.remove(datom.entity, datom.attribute, &datom.value) {
                result[position] = None;
            }
        }
    }
    result.into_iter().flatten().collect()
}

fn validate_stored_entity(entity: u64, frontier: u64, basis_t: u64) -> Result<(), SemanticError> {
    validate_supported_eid(entity).map_err(|error| {
        SemanticError::new(
            ErrorCategory::Fault,
            "kernel/invalid-stored-entity-id",
            format!("stored entity id {entity} is invalid: {error}"),
        )
    })?;
    let eidx = eid_to_eidx(entity).expect("supported entity id was checked");
    if eidx >= frontier {
        return Err(SemanticError::new(
            ErrorCategory::Fault,
            "kernel/unissued-stored-entity-id",
            format!("stored entity index {eidx} is not below issued frontier {frontier}"),
        ));
    }
    if eid_to_part(entity).expect("supported entity id was checked") == crate::TX_PARTITION {
        let t = tx_to_t(entity).expect("transaction partition was checked");
        if t == 0 || t > basis_t {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "kernel/stored-transaction-out-of-range",
                format!("stored transaction entity t={t} is outside basis {basis_t}"),
            ));
        }
    }
    Ok(())
}

fn validate_stored_value(value: &Value, frontier: u64, basis_t: u64) -> Result<(), SemanticError> {
    match value {
        Value::Ref(entity) => validate_stored_entity(*entity, frontier, basis_t),
        Value::Tuple(slots) => {
            for value in slots.iter().flatten() {
                validate_stored_value(value, frontier, basis_t)?;
            }
            Ok(())
        }
        _ => Ok(()),
    }
}

fn expected_frontier_after_commit(
    current_frontier: u64,
    t: u64,
    tempids: &BTreeMap<String, u64>,
    schema_changes: &[SchemaChange],
) -> Result<u64, SemanticError> {
    validate_frontier(current_frontier)?;
    let start = current_frontier.max(t.checked_add(1).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Fault,
            "recovery/basis-overflow",
            "transaction time cannot advance the issued frontier",
        )
    })?);
    validate_frontier(start)?;

    let mut allocated = BTreeSet::new();
    for change in schema_changes {
        let SchemaChange::Install(attribute) = change else {
            continue;
        };
        let eidx = u64::from(attribute.id);
        if eidx >= current_frontier {
            allocated.insert(eidx);
        }
    }
    for entity in tempids.values().copied() {
        validate_supported_eid(entity).map_err(|error| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/invalid-tempid-entity",
                format!("committed tempid resolves to an invalid entity id: {error}"),
            )
        })?;
        let eidx = eid_to_eidx(entity).expect("supported entity id was checked");
        if eidx >= current_frontier {
            if eid_to_part(entity).expect("supported entity id was checked") != USER_PARTITION
                || eidx < start
            {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "recovery/invalid-tempid-allocation",
                    "fresh tempid allocations must be contiguous user-partition entity ids",
                ));
            }
            allocated.insert(eidx);
        }
    }
    for (offset, actual) in allocated.iter().copied().enumerate() {
        let expected = start.checked_add(offset as u64).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Fault,
                "recovery/entity-id-overflow",
                "committed tempid allocation overflows the entity-index space",
            )
        })?;
        if actual != expected {
            return Err(SemanticError::new(
                ErrorCategory::Fault,
                "recovery/noncontiguous-tempid-allocation",
                format!("expected allocated entity index {expected}, got {actual}"),
            ));
        }
    }
    let frontier = start.checked_add(allocated.len() as u64).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Fault,
            "recovery/entity-id-overflow",
            "committed tempid allocation overflows the entity-index space",
        )
    })?;
    validate_frontier(frontier)?;
    Ok(frontier)
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

#[cfg(test)]
mod indexed_validation_tests {
    use super::*;
    use crate::{Attribute, DurableTransaction, Keyword};
    use std::str::FromStr;
    use std::time::Instant;

    fn linear_replay(datoms: &[Datom]) -> Vec<CurrentFact> {
        let mut result = Vec::<CurrentFact>::new();
        for datom in datoms {
            if datom.added {
                if let Some(fact) = result.iter_mut().find(|fact| {
                    fact.entity == datom.entity
                        && fact.attribute == datom.attribute
                        && fact.value.stored_eq(&datom.value)
                }) {
                    if datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32 {
                        fact.tx = datom.tx;
                    }
                } else {
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

    fn assert_exact_facts(left: &[CurrentFact], right: &[CurrentFact]) {
        assert_eq!(left.len(), right.len());
        for (left, right) in left.iter().zip(right) {
            assert_eq!(
                (left.entity, left.attribute, left.tx),
                (right.entity, right.attribute, right.tx)
            );
            assert_eq!(
                crate::encoding::encode_canonical_value(&left.value).unwrap(),
                crate::encoding::encode_canonical_value(&right.value).unwrap(),
                "first stored representation must survive logical equivalence"
            );
        }
    }

    fn values() -> Vec<Value> {
        let decimal = |text| Value::BigDec(bigdecimal::BigDecimal::from_str(text).unwrap());
        vec![
            decimal("1.0"),
            decimal("1.00"),
            decimal("1"),
            decimal("0.0"),
            Value::Long(1),
            Value::Long(0),
            Value::Ref(1),
            Value::Float(1.0),
            Value::Double(1.0),
            Value::Double(-0.0),
            Value::Double(f64::from_bits(0x7ff8_0000_0000_0001)),
            Value::Double(f64::from_bits(0x7ff8_0000_0000_0002)),
            Value::Float(f32::NAN),
            Value::Double(f64::INFINITY),
            Value::Tuple(vec![Some(decimal("1.0")), None]),
            Value::Tuple(vec![Some(decimal("1.00")), None]),
            Value::String("one".into()),
        ]
    }

    #[test]
    fn indexed_replay_and_logical_apply_match_linear_stored_semantics() {
        let values = values();
        let mut random = 0x93d2_0e5f_3284_a671_u64;
        for case in 0..256 {
            let mut datoms = Vec::new();
            for position in 0..96 {
                random = random
                    .wrapping_mul(6_364_136_223_846_793_005)
                    .wrapping_add(1);
                datoms.push(Datom {
                    entity: (random >> 40) % 4,
                    attribute: if position % 4 == 0 {
                        crate::DB_ALTER_ATTRIBUTE as u32
                    } else {
                        1_000 + ((random >> 28) % 2) as u32
                    },
                    value: values[((random >> 8) as usize) % values.len()].clone(),
                    tx: position as u64,
                    added: random >> 63 != 0,
                });
            }
            assert_exact_facts(&replay(datoms.iter()), &linear_replay(&datoms));

            let initial = linear_replay(&datoms[..32]);
            let logical: Vec<_> = datoms[32..]
                .iter()
                .map(|datom| LogicalDatom {
                    entity: datom.entity,
                    attribute: datom.attribute,
                    value: datom.value.clone(),
                    added: datom.added,
                })
                .collect();
            // Apply logical changes in transaction order: all retractions,
            // then additions. The initial stream reproduces current order.
            let mut ordered = facts_as_datoms(&initial);
            for added in [false, true] {
                ordered.extend(
                    logical
                        .iter()
                        .filter(|datom| datom.added == added)
                        .map(|datom| Datom {
                            entity: datom.entity,
                            attribute: datom.attribute,
                            value: datom.value.clone(),
                            tx: 10_000 + case,
                            added,
                        }),
                );
            }
            assert_exact_facts(
                &apply_logical(&initial, &logical, 10_000 + case),
                &linear_replay(&ordered),
            );
        }
    }

    fn linear_cardinality(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
        for (index, left) in facts.iter().enumerate() {
            if schema.attribute(left.attribute)?.cardinality == Cardinality::One
                && facts[index + 1..]
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

    fn linear_uniqueness(schema: &Schema, facts: &[CurrentFact]) -> Result<(), SemanticError> {
        for (index, left) in facts.iter().enumerate() {
            if schema.attribute(left.attribute)?.unique.is_none() {
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

    #[test]
    fn indexed_constraint_checks_preserve_exact_error_precedence() {
        let mut schema = Schema::new();
        for (id, cardinality, unique) in [
            (1_000, Cardinality::One, None),
            (1_001, Cardinality::One, Some(Unique::Value)),
            (1_002, Cardinality::Many, None),
        ] {
            let mut attribute = Attribute::new(
                id,
                Keyword::new("indexed-test", format!("a-{id}")),
                ValueType::Double,
                cardinality,
            );
            attribute.unique = unique;
            schema.install(attribute).unwrap();
        }
        let values = values();
        let mut random = 0xe373_bc92_d740_9581_u64;
        for _ in 0..512 {
            let facts: Vec<_> = (0..24)
                .map(|_| {
                    random = random
                        .wrapping_mul(6_364_136_223_846_793_005)
                        .wrapping_add(1);
                    CurrentFact {
                        entity: (random >> 40) % 8,
                        attribute: 1_000 + ((random >> 28) % 4) as u32,
                        value: values[((random >> 8) as usize) % values.len()].clone(),
                        tx: 1,
                    }
                })
                .collect();
            assert_eq!(
                validate_cardinality(&schema, &facts),
                linear_cardinality(&schema, &facts)
            );
            assert_eq!(
                validate_uniqueness(&schema, &facts),
                linear_uniqueness(&schema, &facts)
            );
        }
    }

    #[test]
    fn committed_indexed_checks_reject_duplicate_contradictory_and_nonmaterial_datoms() {
        let before = Database::bootstrap().unwrap();
        let first = before
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("subject".into()),
                    attribute: crate::DB_DOC as u32,
                    value: Value::String("first".into()).into(),
                }],
                100,
            )
            .unwrap();
        let durable = |report: &TxReport| DurableTransaction {
            database_id: "indexed-recovery-test".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash: [0; 32],
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        };
        let first_envelope = durable(&first);
        let fact = first_envelope
            .tx_data
            .iter()
            .find(|datom| datom.attribute == crate::DB_DOC as u32)
            .unwrap();
        for added in [true, false] {
            let mut invalid = first_envelope.clone();
            let mut duplicate = fact.clone();
            duplicate.added = added;
            invalid.tx_data.push(duplicate);
            assert_eq!(
                before.apply_committed(&invalid).unwrap_err().code,
                "recovery/duplicate-datom"
            );
        }
        let recovered = before.apply_committed(&first_envelope).unwrap();
        let second = recovered.with(&[], 200).unwrap();
        let mut nonmaterial = durable(&second);
        let mut duplicate = fact.clone();
        duplicate.tx = t_to_tx(second.db_after.basis_t()).unwrap();
        nonmaterial.tx_data.push(duplicate);
        assert_eq!(
            recovered.apply_committed(&nonmaterial).unwrap_err().code,
            "recovery/nonmaterial-datom"
        );
        recovered.validate_invariants().unwrap();
    }

    #[test]
    fn indexed_helpers_measure_growth_against_the_linear_oracle() {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    1_000,
                    Keyword::new("indexed-test", "identity"),
                    ValueType::Long,
                    Cardinality::One,
                )
                .unique(Unique::Value),
            )
            .unwrap();
        for count in [1_000, 4_000] {
            let datoms: Vec<_> = (0..count)
                .map(|index| Datom {
                    entity: index as u64,
                    attribute: 1_000,
                    value: Value::Long(index as i64),
                    tx: 1,
                    added: true,
                })
                .collect();
            let start = Instant::now();
            let expected = linear_replay(&datoms);
            linear_cardinality(&schema, &expected).unwrap();
            linear_uniqueness(&schema, &expected).unwrap();
            let linear = start.elapsed();
            let start = Instant::now();
            let actual = replay(datoms.iter());
            validate_cardinality(&schema, &actual).unwrap();
            validate_uniqueness(&schema, &actual).unwrap();
            let indexed = start.elapsed();
            assert_exact_facts(&actual, &expected);
            eprintln!("indexed_helper_scale facts={count} linear={linear:?} indexed={indexed:?}");
        }
    }

    #[test]
    fn committed_transition_rejects_invalid_inputs_without_a_full_audit() {
        let mut schema = Schema::new();
        for attribute in [
            Attribute::new(
                1_000,
                Keyword::new("replay-check", "one"),
                ValueType::Long,
                Cardinality::One,
            ),
            Attribute::new(
                1_001,
                Keyword::new("replay-check", "unique"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Value),
            Attribute::new(
                1_002,
                Keyword::new("replay-check", "ref"),
                ValueType::Ref,
                Cardinality::Many,
            ),
            Attribute::new(
                1_003,
                Keyword::new("replay-check", "tuple"),
                ValueType::Tuple,
                Cardinality::Many,
            )
            .tuple(TupleSpec::Homogeneous(ValueType::Ref)),
        ] {
            schema.install(attribute).unwrap();
        }
        let before = Database::new(schema).unwrap();
        let ops: Vec<_> = [
            (1_000, Value::Long(7)),
            (1_001, Value::String("key".into())),
            (1_002, Value::Ref(crate::DB_PART_DB)),
            (
                1_003,
                Value::Tuple(vec![Some(Value::Ref(crate::DB_PART_DB)), None]),
            ),
        ]
        .into_iter()
        .map(|(attribute, value)| TxOp::Add {
            entity: EntityRef::Temp("subject".into()),
            attribute,
            value: value.into(),
        })
        .collect();
        let report = before.with(&ops, 100).unwrap();
        let envelope = DurableTransaction {
            database_id: "replay-validation-test".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash: [0; 32],
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        };
        let unissued = make_eid(USER_PARTITION, envelope.eidx_frontier).unwrap();
        let future_tx = t_to_tx(envelope.basis_t + 1).unwrap();
        for (attribute, value, error) in [
            (
                1_000,
                Value::String("wrong type".into()),
                "transaction/value-type",
            ),
            (
                1_002,
                Value::Ref(unissued),
                "kernel/unissued-stored-entity-id",
            ),
            (
                1_002,
                Value::Ref(future_tx),
                "kernel/stored-transaction-out-of-range",
            ),
            (
                1_003,
                Value::Tuple(vec![Some(Value::Ref(unissued)), None]),
                "kernel/unissued-stored-entity-id",
            ),
        ] {
            let mut invalid = envelope.clone();
            invalid
                .tx_data
                .iter_mut()
                .find(|datom| datom.attribute == attribute)
                .unwrap()
                .value = value;
            assert_eq!(before.apply_committed(&invalid).unwrap_err().code, error);
        }
        for (entity, error) in [
            (unissued, "kernel/unissued-stored-entity-id"),
            (future_tx, "kernel/stored-transaction-out-of-range"),
            (u64::MAX, "kernel/invalid-stored-entity-id"),
        ] {
            let mut invalid = envelope.clone();
            invalid
                .tx_data
                .iter_mut()
                .find(|datom| datom.attribute == 1_000)
                .unwrap()
                .entity = entity;
            assert_eq!(before.apply_committed(&invalid).unwrap_err().code, error);
        }
        let mut invalid = envelope.clone();
        invalid.eidx_frontier += 1;
        assert_eq!(
            before.apply_committed(&invalid).unwrap_err().code,
            "recovery/eidx-frontier-mismatch"
        );
        let mut invalid = envelope.clone();
        let mut extra = invalid
            .tx_data
            .iter()
            .find(|datom| datom.attribute == 1_000)
            .unwrap()
            .clone();
        extra.value = Value::Long(8);
        invalid.tx_data.push(extra);
        assert_eq!(
            before.apply_committed(&invalid).unwrap_err().code,
            "transaction/cardinality-one-conflict"
        );
        let mut invalid = envelope.clone();
        let mut extra = invalid
            .tx_data
            .iter()
            .find(|datom| datom.attribute == 1_001)
            .unwrap()
            .clone();
        extra.entity = crate::DB_PART_DB;
        invalid.tx_data.push(extra);
        assert_eq!(
            before.apply_committed(&invalid).unwrap_err().code,
            "transaction/unique-conflict"
        );
        before
            .apply_committed(&envelope)
            .unwrap()
            .validate_invariants()
            .unwrap();
    }

    #[test]
    fn explicit_audit_still_rejects_independently_corrupted_caches_and_roots() {
        let before = Database::bootstrap().unwrap();
        let after = before
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("subject".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("audit", "subject")).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("subject".into()),
                        attribute: crate::DB_DOC as u32,
                        value: Value::String("documentation".into()).into(),
                    },
                ],
                100,
            )
            .unwrap()
            .db_after;
        let mut corrupt = after.clone();
        corrupt.schema = Arc::new(Schema::new());
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/derived-schema-divergence"
        );
        let mut corrupt = after.clone();
        corrupt.idents = Arc::clone(&before.idents);
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/derived-schema-divergence"
        );
        let mut corrupt = after.clone();
        corrupt.semantic_state = before.semantic_state.clone();
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/semantic-commitment-divergence"
        );
        let mut corrupt = after.clone();
        corrupt.current_indexes = before.current_indexes.clone();
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/current-index-divergence"
        );
        let mut corrupt = after.clone();
        corrupt.history_indexes = before.history_indexes.clone();
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/history-index-divergence"
        );
        let mut corrupt = after.clone();
        let mut history = corrupt.history.to_vec();
        history.push(Arc::from([]));
        corrupt.history = history.into();
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/history-basis-divergence"
        );
        let mut corrupt = after.clone();
        let mut current = corrupt.current.to_vec();
        current
            .iter_mut()
            .find(|fact| {
                fact.attribute == crate::DB_DOC as u32
                    && fact.value == Value::String("documentation".into())
            })
            .unwrap()
            .tx = t_to_tx(after.basis_t() + 1).unwrap();
        let current_datoms = facts_as_datoms(&current);
        corrupt.semantic_state = SemanticStateCommitment::from_current(&current_datoms).unwrap();
        corrupt.current_indexes = IndexRoots::build(&corrupt.schema, current_datoms);
        corrupt.current = current.into();
        assert_eq!(
            corrupt.validate_invariants().unwrap_err().code,
            "kernel/history-current-divergence"
        );
        after.validate_invariants().unwrap();
    }

    #[test]
    fn committed_replay_measures_endpoint_audit_against_every_prefix_audit() {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    1_000,
                    Keyword::new("replay-check", "value"),
                    ValueType::Long,
                    Cardinality::One,
                )
                .unique(Unique::Value),
            )
            .unwrap();
        let initial = Database::new(schema).unwrap();
        for count in [500, 2_000] {
            let mut basis = initial.basis_t();
            let mut frontier = initial.eidx_frontier();
            let transactions: Vec<_> = (0..20)
                .map(|batch| {
                    basis += 1;
                    let tx = t_to_tx(basis).unwrap();
                    let mut tempids = BTreeMap::new();
                    let mut tx_data = vec![Datom {
                        entity: tx,
                        attribute: DB_TX_INSTANT as u32,
                        value: Value::Instant(basis as i64),
                        tx,
                        added: true,
                    }];
                    for item in 0..count / 20 {
                        let entity = make_eid(USER_PARTITION, frontier).unwrap();
                        frontier += 1;
                        tempids.insert(format!("subject-{item}"), entity);
                        tx_data.push(Datom {
                            entity,
                            attribute: 1_000,
                            value: Value::Long((batch * (count / 20) + item) as i64),
                            tx,
                            added: true,
                        });
                    }
                    tx_data.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
                    DurableTransaction {
                        database_id: "replay-audit-measurement".into(),
                        basis_t: basis,
                        previous_hash: [0; 32],
                        eidx_frontier: frontier,
                        tempids,
                        tx_data,
                    }
                })
                .collect();
            let start = Instant::now();
            let mut prefix_audited = initial.clone();
            let mut state_hashes = Vec::new();
            for transaction in &transactions {
                prefix_audited = prefix_audited.apply_committed(transaction).unwrap();
                prefix_audited.validate_invariants().unwrap();
                state_hashes
                    .push(crate::state_commitment::checkpoint_state_hash(&prefix_audited).unwrap());
            }
            let prefix_elapsed = start.elapsed();
            let start = Instant::now();
            let mut endpoint_audited = initial.clone();
            for (transaction, expected_hash) in transactions.iter().zip(&state_hashes) {
                endpoint_audited = endpoint_audited.apply_committed(transaction).unwrap();
                assert_eq!(
                    crate::state_commitment::checkpoint_state_hash(&endpoint_audited).unwrap(),
                    *expected_hash
                );
            }
            endpoint_audited.validate_invariants().unwrap();
            let endpoint_elapsed = start.elapsed();
            assert!(endpoint_audited.same_information_as(&prefix_audited));
            eprintln!(
                "committed_replay_scale facts={count} transactions=20 every_prefix_audit={prefix_elapsed:?} endpoint_audit={endpoint_elapsed:?}"
            );
        }
    }
}

#[cfg(test)]
mod schema_hook_recovery_tests {
    use super::*;
    use crate::{Attribute, DurableTransaction, Keyword};

    fn database_with_attribute() -> Database {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("hook-test", "value"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        Database::new(schema).unwrap()
    }

    fn durable(report: &TxReport) -> DurableTransaction {
        DurableTransaction {
            database_id: "schema-hook-recovery-test".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash: [0; 32],
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        }
    }

    fn hook_events(datoms: &[Datom], target: u64) -> Vec<(u64, u32)> {
        let mut events: Vec<_> = datoms
            .iter()
            .filter(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.added
                    && datom.value == Value::Ref(target)
                    && (datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                        || datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32)
            })
            .map(|datom| (tx_to_t(datom.tx).unwrap(), datom.attribute))
            .collect();
        events.sort_unstable();
        events
    }

    fn current_alter_hook_tx(database: &Database, target: u64) -> u64 {
        let hooks = database
            .datoms(View::Current, IndexOrder::Eavt)
            .into_iter()
            .filter(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                    && datom.value == Value::Ref(target)
                    && datom.added
            })
            .collect::<Vec<_>>();
        assert_eq!(hooks.len(), 1, "current value must have one alter hook");
        hooks[0].tx
    }

    #[test]
    fn pure_assessment_rejects_an_explicit_hook_of_the_wrong_kind() {
        let database = database_with_attribute();
        let error = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(crate::DB_PART_DB),
                    attribute: crate::DB_INSTALL_ATTRIBUTE as u32,
                    value: TxValue::Entity(EntityRef::Id(1_000)),
                }],
                1,
            )
            .unwrap_err();

        assert_eq!(error.code, "schema/wrong-hook-kind");
    }

    #[test]
    fn recovery_rejects_missing_and_wrong_schema_hooks() {
        let database = database_with_attribute();
        let report = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(1_000),
                    attribute: crate::DB_INDEX as u32,
                    value: Value::Bool(true).into(),
                }],
                1,
            )
            .unwrap();
        let envelope = durable(&report);

        let mut missing = envelope.clone();
        missing.tx_data.retain(|datom| {
            !(datom.entity == crate::DB_PART_DB
                && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                && datom.value == Value::Ref(1_000))
        });
        let error = database.apply_committed(&missing).unwrap_err();
        assert_eq!(error.code, "recovery/invalid-schema-hooks");
        assert!(error.message.contains("schema/missing-hook"));

        let mut wrong = envelope;
        let hook = wrong
            .tx_data
            .iter_mut()
            .find(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                    && datom.value == Value::Ref(1_000)
            })
            .expect("pure assessment synthesized an alter hook");
        hook.attribute = crate::DB_INSTALL_ATTRIBUTE as u32;
        wrong
            .tx_data
            .sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let error = database.apply_committed(&wrong).unwrap_err();
        assert_eq!(error.code, "recovery/invalid-schema-hooks");
        assert!(error.message.contains("schema/wrong-hook-kind"));
    }

    #[test]
    fn repeated_alter_hook_is_a_legal_recovery_event() {
        let database = database_with_attribute();
        let indexed = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(1_000),
                    attribute: crate::DB_INDEX as u32,
                    value: Value::Bool(true).into(),
                }],
                1,
            )
            .unwrap();
        let recovered_indexed = database.apply_committed(&durable(&indexed)).unwrap();
        assert!(recovered_indexed.same_information_as(&indexed.db_after));

        let unindexed = indexed
            .db_after
            .with(
                &[TxOp::Retract {
                    entity: EntityRef::Id(1_000),
                    attribute: crate::DB_INDEX as u32,
                    value: Some(Value::Bool(true).into()),
                }],
                2,
            )
            .unwrap();
        assert!(unindexed.tx_data.iter().any(|datom| {
            datom.entity == crate::DB_PART_DB
                && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                && datom.value == Value::Ref(1_000)
                && datom.added
        }));

        let recovered_unindexed = recovered_indexed
            .apply_committed(&durable(&unindexed))
            .unwrap();
        assert!(recovered_unindexed.same_information_as(&unindexed.db_after));
    }

    #[test]
    fn hooks_follow_material_schema_history_and_recover_exactly() {
        const ATTRIBUTE: u32 = 1_000;

        let bootstrap = Database::bootstrap().unwrap();
        let plain = Attribute::new(
            ATTRIBUTE,
            Keyword::new("hook-history", "value"),
            ValueType::String,
            Cardinality::One,
        );
        let install = bootstrap
            .with(&[TxOp::InstallAttribute(plain.clone())], 10)
            .unwrap();
        assert_eq!(
            hook_events(&install.tx_data, u64::from(ATTRIBUTE)),
            vec![(1, crate::DB_INSTALL_ATTRIBUTE as u32)]
        );
        let recovered_install = bootstrap.apply_committed(&durable(&install)).unwrap();
        assert!(recovered_install.same_information_as(&install.db_after));

        let mut indexed = plain.clone();
        indexed.indexed = true;
        let first_alter = install
            .db_after
            .with(&[TxOp::AlterAttribute(indexed.clone())], 20)
            .unwrap();
        assert_eq!(
            first_alter
                .tx_data
                .iter()
                .filter(|datom| datom.entity == u64::from(ATTRIBUTE))
                .map(|datom| (datom.attribute, datom.added))
                .collect::<Vec<_>>(),
            vec![(crate::DB_INDEX as u32, true)]
        );
        assert_eq!(
            hook_events(&first_alter.tx_data, u64::from(ATTRIBUTE)),
            vec![(2, crate::DB_ALTER_ATTRIBUTE as u32)]
        );
        assert_eq!(
            current_alter_hook_tx(&first_alter.db_after, u64::from(ATTRIBUTE)),
            t_to_tx(2).unwrap()
        );
        let recovered_first = recovered_install
            .apply_committed(&durable(&first_alter))
            .unwrap();
        assert!(recovered_first.same_information_as(&first_alter.db_after));

        let mut no_history = indexed.clone();
        no_history.no_history = true;
        let second_alter = first_alter
            .db_after
            .with(&[TxOp::AlterAttribute(no_history.clone())], 30)
            .unwrap();
        assert_eq!(
            second_alter
                .tx_data
                .iter()
                .filter(|datom| datom.entity == u64::from(ATTRIBUTE))
                .map(|datom| (datom.attribute, datom.added))
                .collect::<Vec<_>>(),
            vec![(crate::DB_NO_HISTORY as u32, true)]
        );
        assert_eq!(
            hook_events(&second_alter.tx_data, u64::from(ATTRIBUTE)),
            vec![(3, crate::DB_ALTER_ATTRIBUTE as u32)]
        );
        assert_eq!(
            current_alter_hook_tx(&second_alter.db_after, u64::from(ATTRIBUTE)),
            t_to_tx(3).unwrap(),
            "a second material alter must advance the current hook coordinate"
        );
        assert_eq!(
            second_alter.db_after.semantic_state.metadata(),
            SemanticStateCommitment::recompute(&second_alter.db_after)
                .unwrap()
                .metadata(),
            "incremental commitment must bind the advanced current coordinate"
        );
        let recovered_second = recovered_first
            .apply_committed(&durable(&second_alter))
            .unwrap();
        assert!(recovered_second.same_information_as(&second_alter.db_after));
        assert_eq!(
            current_alter_hook_tx(&recovered_second, u64::from(ATTRIBUTE)),
            t_to_tx(3).unwrap(),
            "durable replay must preserve the newest current hook coordinate"
        );
        let rebuilt = recovered_second.rebuild_derived_caches().unwrap();
        assert!(rebuilt.same_information_as(&recovered_second));

        let as_of_one = second_alter
            .db_after
            .datoms(View::AsOf(1), IndexOrder::Eavt);
        assert_eq!(
            hook_events(&as_of_one, u64::from(ATTRIBUTE)),
            vec![(1, crate::DB_INSTALL_ATTRIBUTE as u32)]
        );
        assert!(!as_of_one.iter().any(|datom| {
            datom.entity == u64::from(ATTRIBUTE) && datom.attribute == crate::DB_INDEX as u32
        }));
        let as_of_two = second_alter
            .db_after
            .datoms(View::AsOf(2), IndexOrder::Eavt);
        assert!(as_of_two.iter().any(|datom| {
            datom.entity == u64::from(ATTRIBUTE)
                && datom.attribute == crate::DB_INDEX as u32
                && datom.value == Value::Bool(true)
        }));
        assert!(!as_of_two.iter().any(|datom| {
            datom.entity == u64::from(ATTRIBUTE) && datom.attribute == crate::DB_NO_HISTORY as u32
        }));

        // An ordinary assertion of an already-current metadata fact is
        // redundant before attrs-missing-hooks runs and must not emit A19.
        let no_op = second_alter
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(u64::from(ATTRIBUTE)),
                    attribute: crate::DB_NO_HISTORY as u32,
                    value: Value::Bool(true).into(),
                }],
                40,
            )
            .unwrap();
        assert!(hook_events(&no_op.tx_data, u64::from(ATTRIBUTE)).is_empty());
        assert!(!no_op.tx_data.iter().any(|datom| {
            datom.entity == u64::from(ATTRIBUTE) && datom.attribute == crate::DB_NO_HISTORY as u32
        }));
        let recovered_no_op = recovered_second.apply_committed(&durable(&no_op)).unwrap();
        assert!(recovered_no_op.same_information_as(&no_op.db_after));

        // Ident changes and ordinary application facts do not participate in
        // the attribute-hook property set.
        let mut renamed = no_history;
        let old_ident = renamed.ident.clone();
        renamed.ident = Keyword::new("hook-history", "renamed");
        let rename = no_op
            .db_after
            .with(
                &[
                    TxOp::AlterAttribute(renamed.clone()),
                    TxOp::Add {
                        entity: EntityRef::Temp("ordinary-entity".into()),
                        attribute: ATTRIBUTE,
                        value: Value::String("ordinary-value".into()).into(),
                    },
                ],
                50,
            )
            .unwrap();
        assert!(hook_events(&rename.tx_data, u64::from(ATTRIBUTE)).is_empty());
        assert_eq!(
            rename.db_after.entid(&old_ident),
            Some(u64::from(ATTRIBUTE))
        );
        assert_eq!(
            rename.db_after.entid(&renamed.ident),
            Some(u64::from(ATTRIBUTE))
        );

        // Recovery rejects a fabricated A19 attached only to the rename and
        // custom fact, while the unmodified durable transaction replays bit
        // for bit to the same immutable database value.
        let mut orphan = durable(&rename);
        orphan.tx_data.push(Datom {
            entity: crate::DB_PART_DB,
            attribute: crate::DB_ALTER_ATTRIBUTE as u32,
            value: Value::Ref(u64::from(ATTRIBUTE)),
            tx: t_to_tx(5).unwrap(),
            added: true,
        });
        orphan
            .tx_data
            .sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let error = recovered_no_op.apply_committed(&orphan).unwrap_err();
        assert_eq!(error.code, "recovery/invalid-schema-hooks");
        assert!(error.message.contains("schema/orphan-hook"));

        let recovered_rename = recovered_no_op.apply_committed(&durable(&rename)).unwrap();
        assert!(recovered_rename.same_information_as(&rename.db_after));
        assert_eq!(
            hook_events(
                &rename.db_after.datoms(View::History, IndexOrder::Eavt),
                u64::from(ATTRIBUTE)
            ),
            vec![
                (1, crate::DB_INSTALL_ATTRIBUTE as u32),
                (2, crate::DB_ALTER_ATTRIBUTE as u32),
                (3, crate::DB_ALTER_ATTRIBUTE as u32),
            ]
        );
    }
}

#[cfg(test)]
mod excision_kernel_tests {
    use super::*;
    use crate::vocabulary::pre_excision_genesis_datoms;
    use crate::{DB_DOC, DB_FULLTEXT, DurableTransaction};

    fn durable(report: &TxReport) -> DurableTransaction {
        DurableTransaction {
            database_id: "excision-kernel-test".into(),
            basis_t: report.db_after.basis_t(),
            previous_hash: [0; 32],
            eidx_frontier: report.db_after.eidx_frontier(),
            tempids: report.tempids.clone(),
            tx_data: report.tx_data.clone(),
        }
    }

    fn install_excision_vocabulary(database: &Database) -> TxReport {
        let ids = [
            DB_EXCISE as u32,
            DB_EXCISE_ATTRS as u32,
            DB_EXCISE_BEFORE_T as u32,
            DB_EXCISE_BEFORE as u32,
        ];
        let ops = supported_system_attributes()
            .into_iter()
            .filter(|attribute| ids.contains(&attribute.id))
            .map(TxOp::InstallAttribute)
            .collect::<Vec<_>>();
        database.with(&ops, 10).unwrap()
    }

    #[test]
    fn exact_legacy_genesis_upgrades_through_ordinary_queryable_information() {
        let legacy_genesis = pre_excision_genesis_datoms();
        let legacy = Database::from_genesis(legacy_genesis.clone()).unwrap();
        assert!(legacy.schema().attribute(DB_EXCISE as u32).is_err());

        let upgrade = install_excision_vocabulary(&legacy);
        assert_eq!(upgrade.db_after.basis_t(), 1);
        assert_eq!(upgrade.db_after.genesis_datoms(), legacy_genesis);
        for id in [
            DB_EXCISE,
            DB_EXCISE_ATTRS,
            DB_EXCISE_BEFORE_T,
            DB_EXCISE_BEFORE,
        ] {
            assert!(upgrade.db_after.schema().attribute(id as u32).is_ok());
            assert!(upgrade.tx_data.iter().any(|datom| {
                datom.entity == id && datom.attribute == DB_IDENT as u32 && datom.added
            }));
            assert!(upgrade.tx_data.iter().any(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                    && datom.value == Value::Ref(id)
                    && datom.added
            }));
        }

        let recovered = legacy.apply_committed(&durable(&upgrade)).unwrap();
        assert!(recovered.same_information_as(&upgrade.db_after));
        let recovered_history = recovered.datoms(View::History, IndexOrder::Eavt);
        for id in [
            DB_EXCISE,
            DB_EXCISE_ATTRS,
            DB_EXCISE_BEFORE_T,
            DB_EXCISE_BEFORE,
        ] {
            assert!(recovered_history.iter().any(|datom| {
                datom.entity == id && datom.attribute == DB_IDENT as u32 && datom.added
            }));
            assert!(recovered_history.iter().any(|datom| {
                datom.entity == crate::DB_PART_DB
                    && datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                    && datom.value == Value::Ref(id)
                    && datom.added
            }));
        }
    }

    #[test]
    fn partial_or_modified_reserved_upgrade_is_rejected() {
        let legacy = Database::from_genesis(pre_excision_genesis_datoms()).unwrap();
        let excise = supported_system_attributes()
            .into_iter()
            .find(|attribute| attribute.id == DB_EXCISE as u32)
            .unwrap();
        let error = legacy
            .with(&[TxOp::InstallAttribute(excise)], 10)
            .unwrap_err();
        assert_eq!(error.code, "schema/reserved-system-entity");

        let upgrade = install_excision_vocabulary(&legacy);
        let mut mixed = durable(&upgrade);
        mixed.tx_data.push(Datom {
            entity: make_eid(USER_PARTITION, 1).unwrap(),
            attribute: DB_DOC as u32,
            value: Value::String("smuggled".into()),
            tx: t_to_tx(1).unwrap(),
            added: true,
        });
        mixed
            .tx_data
            .sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        assert_eq!(
            legacy.apply_committed(&mixed).unwrap_err().code,
            "recovery/noncanonical-excision-bootstrap-upgrade"
        );
    }

    #[test]
    fn malformed_cutoffs_fail_in_the_request_transaction() {
        let database = Database::bootstrap().unwrap();
        let error = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(DB_FULLTEXT)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_BEFORE_T as u32,
                        value: Value::Long(1).into(),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_BEFORE as u32,
                        value: Value::Instant(1).into(),
                    },
                ],
                10,
            )
            .unwrap_err();
        assert_eq!(error.code, "transaction/excision-cutoff-conflict");

        let negative = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(DB_FULLTEXT)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_BEFORE_T as u32,
                        value: Value::Long(-1).into(),
                    },
                ],
                10,
            )
            .unwrap_err();
        assert_eq!(negative.code, "transaction/excision-before-t");
    }

    #[test]
    fn protected_target_request_commits_and_freezes_at_a15_assertion() {
        let database = Database::bootstrap().unwrap();
        // Full-text behavior itself is deliberately unsupported, so there can
        // be no installed native full-text attribute to excise. Keep that
        // limitation distinct from the documented behavior below: requests
        // targeting protected db-partition information commit as recorded
        // no-ops at the background predicate layer.
        assert!(database.schema().attribute(DB_FULLTEXT as u32).is_err());
        let request = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(crate::DB_PART_DB)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_ATTRS as u32,
                        value: TxValue::Entity(EntityRef::Id(DB_IDENT)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_BEFORE_T as u32,
                        value: Value::Long(crate::t_to_tx(1).unwrap() as i64).into(),
                    },
                ],
                100,
            )
            .unwrap();
        let request_entity = request.tempids["request"];
        let edited = request
            .db_after
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Id(request_entity),
                        attribute: DB_EXCISE_ATTRS as u32,
                        value: TxValue::Entity(EntityRef::Id(DB_TX_INSTANT)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Id(request_entity),
                        attribute: DB_EXCISE_BEFORE_T as u32,
                        value: Value::Long(0).into(),
                    },
                ],
                200,
            )
            .unwrap();

        let frozen = edited.db_after.pending_excision_requests_after(0).unwrap();
        assert_eq!(
            frozen,
            vec![FrozenExcisionRequest {
                request_entity,
                request_t: 1,
                target: crate::DB_PART_DB,
                attributes: BTreeSet::from([DB_IDENT]),
                cutoff: Some(ExcisionCutoff::BeforeT(1)),
            }]
        );
        assert!(
            edited
                .db_after
                .pending_excision_requests_after(1)
                .unwrap()
                .is_empty()
        );
    }

    #[test]
    fn retracted_a15_leaves_a_historical_audit_but_is_not_pending() {
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
            .unwrap();
        let request_entity = request.tempids["request"];
        let retracted = request
            .db_after
            .with(
                &[TxOp::Retract {
                    entity: EntityRef::Id(request_entity),
                    attribute: DB_EXCISE as u32,
                    value: Some(TxValue::Entity(EntityRef::Id(crate::DB_PART_DB))),
                }],
                200,
            )
            .unwrap()
            .db_after;
        assert!(
            retracted
                .pending_excision_requests_after(0)
                .unwrap()
                .is_empty()
        );
        assert_eq!(
            retracted.historical_excision_requests().unwrap(),
            vec![FrozenExcisionRequest {
                request_entity,
                request_t: 1,
                target: crate::DB_PART_DB,
                attributes: BTreeSet::new(),
                cutoff: None,
            }]
        );
    }

    #[test]
    fn instant_cutoff_is_exclusive_and_capped_by_request_time() {
        let database = Database::bootstrap().unwrap();
        let one = database.with(&[], 100).unwrap().db_after;
        let two = one.with(&[], 200).unwrap().db_after;
        let request = two
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE as u32,
                        value: TxValue::Entity(EntityRef::Id(DB_FULLTEXT)),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("request".into()),
                        attribute: DB_EXCISE_BEFORE as u32,
                        value: Value::Instant(200).into(),
                    },
                ],
                300,
            )
            .unwrap()
            .db_after;
        let frozen = request.pending_excision_requests_after(0).unwrap();
        assert_eq!(request.excision_before_t(&frozen[0]), 2);

        let duplicate_one = Database::bootstrap()
            .unwrap()
            .with(&[], 100)
            .unwrap()
            .db_after;
        let duplicate_two = duplicate_one.with(&[], 100).unwrap().db_after;
        assert_eq!(duplicate_two.t_at_or_after_instant(100), 1);
    }

    #[test]
    fn cutoff_matrix_uses_a_strict_boundary_and_never_crosses_request_t() {
        let one = Database::bootstrap()
            .unwrap()
            .with(&[], 100)
            .unwrap()
            .db_after;
        let two = one.with(&[], 200).unwrap().db_after;
        let three = two.with(&[], 200).unwrap().db_after;
        let four = three.with(&[], 300).unwrap().db_after;
        let request = |request_t, cutoff| FrozenExcisionRequest {
            request_entity: make_eid(USER_PARTITION, 1).unwrap(),
            request_t,
            target: crate::DB_PART_DB,
            attributes: BTreeSet::new(),
            cutoff,
        };

        assert_eq!(
            four.excision_before_t(&request(4, Some(ExcisionCutoff::BeforeT(2)))),
            2
        );
        assert_eq!(
            four.excision_before_t(&request(4, Some(ExcisionCutoff::BeforeInstant(150)))),
            2
        );
        assert_eq!(
            four.excision_before_t(&request(4, Some(ExcisionCutoff::BeforeInstant(200)))),
            2,
            "every transaction at the duplicate exact instant is excluded"
        );
        assert_eq!(
            four.excision_before_t(&request(3, Some(ExcisionCutoff::BeforeT(99)))),
            3,
            "a future cutoff cannot remove facts at or after request_t"
        );
        assert_eq!(four.excision_before_t(&request(3, None)), 3);
        assert_eq!(
            four.excision_before_t(&request(4, Some(ExcisionCutoff::BeforeT(0)))),
            0
        );
    }

    #[test]
    fn excision_replay_accepts_only_the_legitimate_dangling_retraction_case() {
        let base = Database::bootstrap().unwrap();
        let first = base
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("entity".into()),
                    attribute: DB_DOC as u32,
                    value: Value::String("old".into()).into(),
                }],
                100,
            )
            .unwrap();
        let entity = first.tempids["entity"];
        let second = first
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: DB_DOC as u32,
                    value: Value::String("new".into()).into(),
                }],
                200,
            )
            .unwrap();
        assert!(second.tx_data.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == DB_DOC as u32
                && datom.value == Value::String("old".into())
                && !datom.added
        }));

        let mut filtered_first = durable(&first);
        filtered_first
            .tx_data
            .retain(|datom| !(datom.entity == entity && datom.attribute == DB_DOC as u32));
        let after_filtered_first = base.apply_excised_committed(&filtered_first).unwrap();
        let second_envelope = durable(&second);
        assert_eq!(
            after_filtered_first
                .apply_committed(&second_envelope)
                .unwrap_err()
                .code,
            "recovery/nonmaterial-datom"
        );
        let recovered = after_filtered_first
            .apply_excised_committed(&second_envelope)
            .unwrap();
        assert_eq!(
            recovered
                .values(entity, DB_DOC as u32)
                .into_iter()
                .cloned()
                .collect::<Vec<_>>(),
            vec![Value::String("new".into())]
        );
        recovered.validate_invariants().unwrap();
    }
}
