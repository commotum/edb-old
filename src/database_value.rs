use crate::{
    AttributeName, DB_IDENT, Database, Datom, EntityIdentifier, ErrorCategory, IndexOrder,
    IndexPrefix, Keyword, PeerIndexCursor, PeerSnapshot, Schema, SemanticError, Value, eid_to_eidx,
    schema_eid_to_attr_id, tx_to_t,
};
use std::fmt;
use std::iter::Cloned;
use std::slice::Iter;
use std::sync::Arc;

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
}

impl Iterator for DatabaseValuePrefixCursor<'_> {
    type Item = Result<Datom, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        match &mut self.inner {
            DatabaseValuePrefixCursorInner::Eager(cursor) => cursor.next().map(Ok),
            DatabaseValuePrefixCursorInner::Native(cursor) => cursor.next(),
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

    /// The basis remains the basis of the underlying immutable value even
    /// when an as-of or since window exposes less information.
    pub fn basis_t(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.basis_t(),
            ReadBasis::Native(snapshot) => snapshot.basis_t(),
        }
    }

    /// Exclusive entity-index issuance frontier at this immutable basis.
    pub fn eidx_frontier(&self) -> u64 {
        match &self.basis {
            ReadBasis::Eager(database) => database.eidx_frontier(),
            ReadBasis::Native(snapshot) => snapshot.eidx_frontier(),
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
        }
    }

    pub fn schema(&self) -> &Schema {
        match &self.basis {
            ReadBasis::Eager(database) => database.schema(),
            ReadBasis::Native(snapshot) => snapshot.schema(),
        }
    }

    /// Resolve an ident from the immutable basis' derived ident cache.
    /// Datomic's temporal predicates window index data, not this dictionary.
    pub fn entid(&self, ident: &Keyword) -> Option<u64> {
        match &self.basis {
            ReadBasis::Eager(database) => database.entid(ident),
            ReadBasis::Native(snapshot) => snapshot.entid(ident),
        }
    }

    pub fn ident(&self, entity: u64) -> Option<&Keyword> {
        match &self.basis {
            ReadBasis::Eager(database) => database.ident(entity),
            ReadBasis::Native(snapshot) => snapshot.ident(entity),
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
