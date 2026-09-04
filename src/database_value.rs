use crate::identity::validate_frontier;
use crate::index::compare_prefix;
use crate::{
    AttributeName, DB_IDENT, Database, Datom, EntityIdentifier, ErrorCategory, IndexOrder,
    IndexPrefix, Keyword, PeerIndexCursor, PeerSnapshot, Schema, SemanticError, Value, eid_to_eidx,
    schema_eid_to_attr_id, tx_to_t,
};
use std::fmt;
use std::iter::Cloned;
use std::slice::Iter;
use std::sync::Arc;
use std::vec::IntoIter;

type ReadFilter = dyn Fn(&DatabaseValue, &Datom) -> bool + Send + Sync;

/// One exact immutable database value used by read-side APIs.
///
/// The two basis representations are deliberate rather than an extensible
/// storage abstraction: an eager semantic-kernel value, or a native
/// PostgreSQL peer snapshot backed by its persistent tree and recent tier.
/// Temporal and custom filters belong to this value, so every consumer sees
/// the same information instead of receiving a detached `View` hint.
#[derive(Clone)]
pub struct DatabaseValue {
    basis: ReadBasis,
    as_of_t: Option<u64>,
    since_t: Option<u64>,
    history: bool,
    filters: Arc<[Arc<ReadFilter>]>,
}

#[derive(Clone)]
enum ReadBasis {
    Eager(Arc<Database>),
    Native(PeerSnapshot),
    TransactionOverlay(Arc<TransactionOverlay>),
}

/// One immutable assessment-local db-after over an exact db-before.
///
/// The overlay owns only canonical transaction datoms, transaction-local
/// ident assertions, and resident successor metadata. Prefix reads merge that
/// bounded delta with the wrapped value by exact stored E/A/V identity; no
/// complete durable index is retained or materialized.
#[derive(Clone)]
struct TransactionOverlay {
    base: DatabaseValue,
    tx_data: Arc<[Datom]>,
    ident_assertions: Arc<[(Keyword, u64)]>,
    schema: Arc<Schema>,
    basis_t: u64,
    eidx_frontier: u64,
    last_tx_instant: i64,
}

/// Lazy current-index cursor over one exact point-in-time database value.
///
/// Eager values borrow their immutable index slice. Native values own a
/// root-pinned peer cursor which lower-bound seeks both the durable tree and
/// its recent tier. Every yielded item is owned so callers cannot retain a
/// cache or tree-node borrow across cursor advancement.
pub struct DatabaseValuePrefixCursor<'a> {
    inner: DatabaseValuePrefixCursorInner<'a>,
}

enum DatabaseValuePrefixCursorInner<'a> {
    Eager(Cloned<Iter<'a, Datom>>),
    Native(Box<PeerIndexCursor>),
    Owned(IntoIter<Datom>),
}

impl Iterator for DatabaseValuePrefixCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        match &mut self.inner {
            DatabaseValuePrefixCursorInner::Eager(cursor) => cursor.next().map(Ok),
            DatabaseValuePrefixCursorInner::Native(cursor) => cursor.next(),
            DatabaseValuePrefixCursorInner::Owned(cursor) => cursor.next().map(Ok),
        }
    }
}

impl fmt::Debug for DatabaseValue {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("DatabaseValue")
            .field(
                "basis",
                &match &self.basis {
                    ReadBasis::Eager(_) => "eager",
                    ReadBasis::Native(_) => "native",
                    ReadBasis::TransactionOverlay(_) => "transaction-overlay",
                },
            )
            .field("basis_t", &self.basis_t())
            .field("as_of_t", &self.as_of_t)
            .field("since_t", &self.since_t)
            .field("history", &self.history)
            .field("filter_count", &self.filters.len())
            .finish()
    }
}

impl DatabaseValue {
    pub fn eager(database: Arc<Database>) -> Self {
        Self {
            basis: ReadBasis::Eager(database),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
        }
    }

    pub fn native(snapshot: PeerSnapshot) -> Self {
        Self {
            basis: ReadBasis::Native(snapshot),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
        }
    }

    /// Build the ephemeral exact db-after used while validating one assessed
    /// transaction. A committed successor must install a new tiered value;
    /// overlays are deliberately not chainable across commits.
    pub(crate) fn transaction_overlay(
        base: DatabaseValue,
        tx_data: Arc<[Datom]>,
        schema: Arc<Schema>,
        basis_t: u64,
        eidx_frontier: u64,
        last_tx_instant: i64,
    ) -> Result<Self, SemanticError> {
        if !base.direct_current() {
            return Err(SemanticError::incorrect(
                "database/overlay-requires-current",
                "a transaction overlay requires an unfiltered point-current db-before",
            ));
        }
        if matches!(&base.basis, ReadBasis::TransactionOverlay(_)) {
            return Err(SemanticError::incorrect(
                "database/overlay-cannot-chain",
                "a committed tiered successor must replace an assessment overlay",
            ));
        }
        let expected_basis = base.basis_t().checked_add(1).ok_or_else(|| {
            SemanticError::incorrect(
                "database/overlay-basis-overflow",
                "a transaction overlay cannot advance the maximum database basis",
            )
        })?;
        if basis_t != expected_basis {
            return Err(SemanticError::incorrect(
                "database/overlay-noncontiguous-basis",
                format!("expected overlay basis {expected_basis}, got {basis_t}"),
            ));
        }
        validate_frontier(eidx_frontier)?;
        if eidx_frontier < base.eidx_frontier() {
            return Err(SemanticError::incorrect(
                "database/overlay-frontier-regression",
                "a transaction overlay cannot move the entity issuance frontier backward",
            ));
        }
        let expected_tx = crate::t_to_tx(basis_t)?;
        if tx_data.iter().any(|datom| datom.tx != expected_tx) {
            return Err(SemanticError::incorrect(
                "database/overlay-transaction-mismatch",
                "every overlay datom must name the successor transaction",
            ));
        }
        if tx_data
            .windows(2)
            .any(|pair| !pair[0].cmp_in(&pair[1], IndexOrder::Eavt).is_lt())
        {
            return Err(SemanticError::incorrect(
                "database/overlay-noncanonical-datoms",
                "overlay datoms must be strictly ordered in canonical EAVT order",
            ));
        }
        if base
            .last_tx_instant()?
            .is_some_and(|previous| last_tx_instant < previous)
        {
            return Err(SemanticError::incorrect(
                "database/overlay-non-monotonic-instant",
                "overlay transaction instant precedes its db-before",
            ));
        }

        // Recovered ident reconstruction folds assertions only: retractions
        // do not erase aliases, and a later assertion may repurpose one. Keep
        // just this transaction's delta and resolve it before the base cache.
        let mut ident_assertions = Vec::new();
        for datom in tx_data
            .iter()
            .filter(|datom| datom.added && u64::from(datom.attribute) == DB_IDENT)
        {
            let Value::Keyword(ident) = &datom.value else {
                return Err(SemanticError::incorrect(
                    "database/overlay-invalid-ident",
                    ":db/ident overlay assertions must contain keywords",
                ));
            };
            if ident_assertions.iter().any(|(prior_ident, prior_entity)| {
                prior_ident == ident || *prior_entity == datom.entity
            }) {
                return Err(SemanticError::incorrect(
                    "database/overlay-conflicting-ident",
                    "overlay ident assertions must be unique by ident and entity",
                ));
            }
            ident_assertions.push((ident.clone(), datom.entity));
        }

        Ok(Self {
            basis: ReadBasis::TransactionOverlay(Arc::new(TransactionOverlay {
                base,
                tx_data,
                ident_assertions: ident_assertions.into(),
                schema,
                basis_t,
                eidx_frontier,
                last_tx_instant,
            })),
            as_of_t: None,
            since_t: None,
            history: false,
            filters: Arc::default(),
        })
    }

    /// The basis remains the basis of the underlying immutable value even
    /// when an as-of or since window exposes less information.
    pub fn basis_t(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.basis_t(),
            ReadBasis::Native(snapshot) => snapshot.basis_t(),
            ReadBasis::TransactionOverlay(overlay) => overlay.basis_t,
        }
    }

    /// Exclusive entity-index issuance frontier at this immutable basis.
    pub fn eidx_frontier(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.eidx_frontier(),
            ReadBasis::Native(snapshot) => snapshot.eidx_frontier(),
            ReadBasis::TransactionOverlay(overlay) => overlay.eidx_frontier,
        }
    }

    /// The most recent transaction instant at this immutable basis.
    ///
    /// A native value resolves the single transaction entity through its lazy
    /// index. It never invokes the eager compatibility materializer.
    pub fn last_tx_instant(&self) -> Result<Option<i64>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => Ok(database.last_tx_instant()),
            ReadBasis::Native(snapshot) => snapshot.last_tx_instant(),
            ReadBasis::TransactionOverlay(overlay) => Ok(Some(overlay.last_tx_instant)),
        }
    }

    pub fn schema(&self) -> &Schema {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema(),
            ReadBasis::Native(snapshot) => snapshot.schema(),
            ReadBasis::TransactionOverlay(overlay) => &overlay.schema,
        }
    }

    /// Resolve an ident from the immutable basis' derived ident cache.
    /// Datomic's temporal predicates window index data, not this dictionary.
    pub fn entid(&self, ident: &Keyword) -> Option<u64> {
        match &self.basis {
            ReadBasis::Eager(database) => database.entid(ident),
            ReadBasis::Native(snapshot) => snapshot.entid(ident),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .ident_assertions
                .iter()
                .find_map(|(candidate, entity)| (candidate == ident).then_some(*entity))
                .or_else(|| overlay.base.entid(ident)),
        }
    }

    pub fn ident(&self, entity: u64) -> Option<&Keyword> {
        match &self.basis {
            ReadBasis::Eager(database) => database.ident(entity),
            ReadBasis::Native(snapshot) => snapshot.ident(entity),
            ReadBasis::TransactionOverlay(overlay) => overlay
                .ident_assertions
                .iter()
                .find_map(|(ident, candidate)| (*candidate == entity).then_some(ident))
                .or_else(|| overlay.base.ident(entity)),
        }
    }

    pub fn as_of_t(&self) -> Option<u64> {
        self.as_of_t
    }

    pub fn since_t(&self) -> Option<u64> {
        self.since_t
    }

    pub fn is_history(&self) -> bool {
        self.history
    }

    pub fn is_filtered(&self) -> bool {
        !self.filters.is_empty()
    }

    /// Derive a value with an inclusive upper transaction boundary.
    /// Repeating `as_of` replaces the prior upper boundary, matching the
    /// recovered immutable `Db` record update.
    pub fn as_of(mut self, t: u64) -> Self {
        self.as_of_t = Some(t);
        self
    }

    /// Derive a value with an exclusive lower transaction boundary.
    /// Repeating `since` replaces the prior lower boundary.
    pub fn since(mut self, t: u64) -> Self {
        self.since_t = Some(t);
        self
    }

    /// Derive a raw history value. Temporal and custom predicates remain in
    /// force; only assertion/retraction collapse is disabled.
    pub fn history(mut self) -> Self {
        self.history = true;
        self
    }

    /// Compose an additional custom datom predicate by logical conjunction.
    /// During evaluation the predicate receives the same temporal/history
    /// value with custom predicates removed, preventing recursive filtering.
    pub fn filter<F>(mut self, predicate: F) -> Self
    where
        F: Fn(&DatabaseValue, &Datom) -> bool + Send + Sync + 'static,
    {
        let mut filters = self.filters.iter().cloned().collect::<Vec<_>>();
        filters.push(Arc::new(predicate));
        self.filters = filters.into();
        self
    }

    /// Reject operations such as entity navigation and pull that require one
    /// point in time. History remains valid for index access and queries.
    pub fn require_point_in_time(&self, operation: &'static str) -> Result<(), SemanticError> {
        if self.history {
            return Err(SemanticError::new(
                ErrorCategory::Incorrect,
                "database/history-not-point-in-time",
                format!("{operation} requires a point-in-time database value"),
            ));
        }
        Ok(())
    }

    /// Return datoms in one logical index after applying this value's complete
    /// temporal/custom window.
    pub fn datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        if self.direct_current() {
            return self.basis_datoms(false, order);
        }
        if self.direct_history() {
            return self.basis_datoms(true, order);
        }
        let datoms = self.basis_datoms(true, order)?;
        self.window(datoms)
    }

    /// Read a left-contiguous prefix before applying temporal/custom
    /// predicates. This preserves the important property that filtered and
    /// historical point reads do not first materialize an entire index.
    pub fn datoms_with_prefix(&self, prefix: &IndexPrefix) -> Result<Vec<Datom>, SemanticError> {
        prefix.validate()?;
        if self.direct_current() {
            return self.basis_prefix(false, prefix);
        }
        if self.direct_history() {
            return self.basis_prefix(true, prefix);
        }
        let datoms = self.basis_prefix(true, prefix)?;
        self.window(datoms)
    }

    /// Lazily read one left-contiguous prefix of an unfiltered current value.
    ///
    /// Transaction processing requires exactly this point-current capability.
    /// Temporal, history, and custom-filter values intentionally use the
    /// existing materializing APIs until their retraction-window semantics can
    /// be represented by a dedicated streaming cursor.
    pub fn current_prefix_cursor(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<DatabaseValuePrefixCursor<'_>, SemanticError> {
        prefix.validate()?;
        if !self.direct_current() {
            return Err(SemanticError::incorrect(
                "database/prefix-cursor-requires-current",
                "lazy transaction prefix access requires an unfiltered current database value",
            ));
        }
        let inner = match &self.basis {
            ReadBasis::Eager(database) => DatabaseValuePrefixCursorInner::Eager(
                database.datoms_with_prefix(prefix)?.iter().cloned(),
            ),
            ReadBasis::Native(snapshot) => DatabaseValuePrefixCursorInner::Native(Box::new(
                snapshot.prefix_cursor(false, prefix)?,
            )),
            ReadBasis::TransactionOverlay(overlay) => DatabaseValuePrefixCursorInner::Owned(
                overlay.prefix(false, prefix)?.into_iter(),
            ),
        };
        Ok(DatabaseValuePrefixCursor { inner })
    }

    pub fn values(&self, entity: u64, attribute: u32) -> Result<Vec<Value>, SemanticError> {
        Ok(self
            .datoms_with_prefix(&IndexPrefix::Eavt {
                entity,
                attribute: Some(attribute),
                value: None,
            })?
            .into_iter()
            .map(|datom| datom.value)
            .collect())
    }

    /// Resolve a lookup ref against this exact value. `:db/ident` follows the
    /// recovered dictionary fast path; all other identities use windowed AVET.
    pub fn lookup(&self, attribute: u32, value: &Value) -> Result<Option<u64>, SemanticError> {
        let schema = self.schema().attribute(attribute)?;
        if schema.unique.is_none() {
            return Err(SemanticError::incorrect(
                "transaction/lookup-non-unique",
                format!("attribute {} is not unique", schema.ident.qualified_name()),
            ));
        }
        self.schema().validate_value(schema, value)?;
        if u64::from(attribute) == DB_IDENT {
            let Value::Keyword(ident) = value else {
                return Ok(None);
            };
            return Ok(self.entid(ident));
        }
        Ok(self
            .datoms_with_prefix(&IndexPrefix::Avet {
                attribute,
                value: Some(value.clone()),
                entity: None,
            })?
            .first()
            .map(|datom| datom.entity))
    }

    /// Resolve the public eid/ident/lookup-ref forms against this exact value.
    pub fn resolve_entity_identifier(
        &self,
        identifier: &EntityIdentifier,
    ) -> Result<Option<u64>, SemanticError> {
        match identifier {
            EntityIdentifier::Id(entity) => {
                eid_to_eidx(*entity)?;
                Ok(Some(*entity))
            }
            EntityIdentifier::Ident(ident) => Ok(self.entid(ident)),
            EntityIdentifier::Lookup { attribute, value } => {
                let attribute = self.resolve_attribute(attribute)?;
                self.lookup(attribute, value)
            }
        }
    }

    fn resolve_attribute(&self, name: &AttributeName) -> Result<u32, SemanticError> {
        let attribute = match name {
            AttributeName::Id(attribute) => *attribute,
            AttributeName::Ident(ident) => {
                let entity = self.entid(ident).ok_or_else(|| {
                    SemanticError::incorrect(
                        "database/unknown-attribute",
                        format!("unknown attribute {}", ident.qualified_name()),
                    )
                })?;
                schema_eid_to_attr_id(entity).map_err(|_| {
                    SemanticError::incorrect(
                        "database/unknown-attribute",
                        format!("{} does not identify an attribute", ident.qualified_name()),
                    )
                })?
            }
        };
        self.schema().attribute(attribute).map_err(|_| {
            SemanticError::incorrect(
                "database/unknown-attribute",
                format!("unknown attribute id {attribute}"),
            )
        })?;
        Ok(attribute)
    }

    fn direct_current(&self) -> bool {
        !self.history && self.as_of_t.is_none() && self.since_t.is_none() && self.filters.is_empty()
    }

    fn direct_history(&self) -> bool {
        self.history && self.as_of_t.is_none() && self.since_t.is_none() && self.filters.is_empty()
    }

    fn basis_datoms(&self, history: bool, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => Ok(database.datoms(
                if history {
                    crate::View::History
                } else {
                    crate::View::Current
                },
                order,
            )),
            ReadBasis::Native(snapshot) => Ok(snapshot.datoms(history, order)?.datoms),
            ReadBasis::TransactionOverlay(_) => Err(SemanticError::new(
                ErrorCategory::Unsupported,
                "database/overlay-unbounded-read",
                "transaction overlays require a bounded index prefix",
            )),
        }
    }

    fn basis_prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        match &self.basis {
            ReadBasis::Eager(database) => {
                if history {
                    Ok(database.history_with_prefix(prefix)?.to_vec())
                } else {
                    Ok(database.datoms_with_prefix(prefix)?.to_vec())
                }
            }
            ReadBasis::Native(snapshot) => Ok(snapshot.datoms_with_prefix(history, prefix)?.datoms),
            ReadBasis::TransactionOverlay(overlay) => overlay.prefix(history, prefix),
        }
    }

    fn without_filters(&self) -> Self {
        let mut database = self.clone();
        database.filters = Arc::default();
        database
    }

    fn window(&self, datoms: Vec<Datom>) -> Result<Vec<Datom>, SemanticError> {
        let unfiltered = self.without_filters();
        let mut selected = Vec::with_capacity(datoms.len());
        for datom in datoms {
            let t = tx_to_t(datom.tx)?;
            if self.as_of_t.is_some_and(|as_of| t > as_of)
                || self.since_t.is_some_and(|since| t <= since)
                || !self
                    .filters
                    .iter()
                    .all(|predicate| predicate(&unfiltered, &datom))
            {
                continue;
            }
            selected.push(datom);
        }
        if self.history {
            Ok(selected)
        } else {
            Ok(collapse_retractions(selected))
        }
    }
}

impl TransactionOverlay {
    fn prefix(
        &self,
        history: bool,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        let source_prefix = match prefix {
            // AVET membership can change when :db/index or :db/unique changes
            // in this transaction. AEVT is the complete attribute source on
            // both eager and native bases, so it can populate or suppress the
            // successor AVET exactly without scanning the whole database.
            IndexPrefix::Avet { attribute, .. } => IndexPrefix::Aevt {
                attribute: *attribute,
                entity: None,
                value: None,
            },
            _ => prefix.clone(),
        };
        let mut datoms = self.base.basis_prefix(history, &source_prefix)?;
        datoms.retain(|datom| {
            overlay_index_member(&self.schema, datom, prefix.order())
                && compare_prefix(datom, prefix).is_eq()
        });

        let delta = self.tx_data.iter().filter(|datom| {
            overlay_index_member(&self.schema, datom, prefix.order())
                && compare_prefix(datom, prefix).is_eq()
        });
        if history {
            datoms.extend(delta.cloned());
        } else {
            for datom in delta {
                if datom.added {
                    // Repeated schema-hook assertions are transaction events
                    // even when the same stored E/A/V is already current.
                    // They enter history but do not replace the current fact's
                    // original transaction coordinate.
                    if !datoms.iter().any(|current| same_stored_eav(current, datom)) {
                        datoms.push(datom.clone());
                    }
                } else {
                    datoms.retain(|current| !same_stored_eav(current, datom));
                }
            }
        }
        datoms.sort_by(|left, right| left.cmp_in(right, prefix.order()));
        Ok(datoms)
    }
}

fn overlay_index_member(schema: &Schema, datom: &Datom, order: IndexOrder) -> bool {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => true,
        IndexOrder::Avet => schema
            .attribute(datom.attribute)
            .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some()),
        IndexOrder::Vaet => schema
            .attribute(datom.attribute)
            .is_ok_and(|attribute| attribute.value_type == crate::ValueType::Ref),
    }
}

fn same_stored_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

impl From<Database> for DatabaseValue {
    fn from(database: Database) -> Self {
        Self::eager(Arc::new(database))
    }
}

impl From<&Database> for DatabaseValue {
    fn from(database: &Database) -> Self {
        Self::from(database.clone())
    }
}

impl From<Arc<Database>> for DatabaseValue {
    fn from(database: Arc<Database>) -> Self {
        Self::eager(database)
    }
}

impl From<PeerSnapshot> for DatabaseValue {
    fn from(snapshot: PeerSnapshot) -> Self {
        Self::native(snapshot)
    }
}

impl From<&PeerSnapshot> for DatabaseValue {
    fn from(snapshot: &PeerSnapshot) -> Self {
        Self::native(snapshot.clone())
    }
}

fn collapse_retractions(datoms: Vec<Datom>) -> Vec<Datom> {
    // Datomic's recovered index comparators place T after logical E/A/V. Our
    // physical comparators refine V with stored representation first, which is
    // necessary to retain values such as 1.0M and 1.00M but can separate one
    // exact value's events in AVET/VAET. Establish the recovered logical order
    // over this already prefix-limited result, decide visibility there, then
    // emit in the caller's original physical index order.
    let mut temporal_order = (0..datoms.len()).collect::<Vec<_>>();
    temporal_order.sort_by(|left, right| {
        let left = &datoms[*left];
        let right = &datoms[*right];
        left.entity
            .cmp(&right.entity)
            .then(left.attribute.cmp(&right.attribute))
            .then_with(|| left.value.index_cmp(&right.value))
            .then_with(|| right.tx.cmp(&left.tx))
            .then_with(|| right.added.cmp(&left.added))
            .then_with(|| left.value.stored_cmp(&right.value))
    });

    let mut visible = vec![false; datoms.len()];
    let mut start = 0;
    while let Some(first_offset) = temporal_order.get(start) {
        let first = &datoms[*first_offset];
        let mut end = start + 1;
        while temporal_order.get(end).is_some_and(|offset| {
            let candidate = &datoms[*offset];
            candidate.entity == first.entity
                && candidate.attribute == first.attribute
                && candidate.value.index_cmp(&first.value).is_eq()
        }) {
            end += 1;
        }

        // Track exact representations separately while walking newest first.
        // A binary-searched slice avoids cloning potentially large values.
        let mut retracted = Vec::<&Value>::new();
        for offset in &temporal_order[start..end] {
            let datom = &datoms[*offset];
            let stored = retracted.binary_search_by(|value| value.stored_cmp(&datom.value));
            if datom.added {
                // `filter-retractions` returns each assertion until a matching
                // retraction. This matters when a custom predicate removes an
                // intervening retraction and exposes multiple assertions.
                visible[*offset] = stored.is_err();
            } else if let Err(position) = stored {
                retracted.insert(position, &datom.value);
            }
        }

        // The recovered implementation uses `common/compare` as the exact
        // shadowing boundary. Its transaction redundancy path instead uses
        // `equals-with-strict-scale`, and native storage deliberately retains
        // 1.0M and 1.00M as distinct legal facts. Equality under `stored_cmp`
        // above is the port's `stored_eq` boundary: it keeps a window at the
        // current basis equal to the unwindowed value and recursively retains
        // the same distinction inside tuples.
        start = end;
    }
    datoms
        .into_iter()
        .zip(visible)
        .filter(|(_, visible)| *visible)
        .map(|(datom, _)| datom)
        .collect()
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{DB_IDENT, EntityRef, TxOp, TxValue, USER_PARTITION, make_eid};

    #[test]
    fn point_current_metadata_and_prefix_cursor_match_the_eager_oracle() {
        let database = Database::bootstrap().unwrap();
        let genesis = database.database_value();
        assert_eq!(genesis.eidx_frontier(), database.eidx_frontier());
        assert_eq!(genesis.last_tx_instant().unwrap(), None);

        let entity = make_eid(USER_PARTITION, 42).unwrap();
        let report = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(Keyword::new("cursor", "entity"))),
                }],
                1_234,
            )
            .unwrap();
        let value = report.db_after.database_value();
        assert_eq!(value.eidx_frontier(), report.db_after.eidx_frontier());
        assert_eq!(value.last_tx_instant().unwrap(), Some(1_234));

        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: None,
            value: None,
        };
        let lazy = value
            .current_prefix_cursor(&prefix)
            .unwrap()
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(lazy, value.datoms_with_prefix(&prefix).unwrap());
        assert!(lazy.iter().all(|datom| datom.entity == entity));
    }

    #[test]
    fn prefix_cursor_rejects_non_current_database_values() {
        let value = Database::bootstrap().unwrap().database_value().history();
        assert_eq!(
            value
                .current_prefix_cursor(&IndexPrefix::Aevt {
                    attribute: DB_IDENT as u32,
                    entity: None,
                    value: None,
                })
                .err()
                .expect("history values must reject the point-current cursor")
                .code,
            "database/prefix-cursor-requires-current"
        );
    }
}
