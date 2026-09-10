//! Explicit database, raw tuple, and immutable log arguments to one evaluator.
use super::*;
use crate::{Datom, IndexTransaction, LogValue, TimePoint};

#[derive(Clone, Debug)]
pub enum QuerySourceValue {
    Database(DatabaseValue),
    /// E/A/V(/T/assertion) rows. Values are literal: raw tuples have no schema
    /// and do not perform keyword/entity/lookup-ref resolution.
    Tuples(Arc<Vec<Vec<Value>>>),
    Log(LogValue),
}

#[derive(Clone, Debug)]
pub struct QueryDataSource {
    pub name: String,
    pub value: QuerySourceValue,
}

impl QueryDataSource {
    pub fn database(name: impl Into<String>, database: DatabaseValue) -> Self {
        Self {
            name: name.into(),
            value: QuerySourceValue::Database(database),
        }
    }
    pub fn tuples(name: impl Into<String>, rows: Vec<Vec<Value>>) -> Self {
        Self {
            name: name.into(),
            value: QuerySourceValue::Tuples(Arc::new(rows)),
        }
    }
    pub fn datoms(name: impl Into<String>, datoms: impl IntoIterator<Item = Datom>) -> Self {
        Self::tuples(name, datoms.into_iter().map(datom_tuple).collect())
    }
    pub fn log(name: impl Into<String>, log: LogValue) -> Self {
        Self {
            name: name.into(),
            value: QuerySourceValue::Log(log),
        }
    }
    pub(super) fn borrowed(&self) -> SourceRef<'_> {
        match &self.value {
            QuerySourceValue::Database(database) => SourceRef::Database(database),
            QuerySourceValue::Tuples(tuples) => SourceRef::Tuples(tuples),
            QuerySourceValue::Log(log) => SourceRef::Log(log),
        }
    }
}

#[derive(Clone, Copy)]
pub(super) enum SourceRef<'a> {
    Database(&'a DatabaseValue),
    Tuples(&'a [Vec<Value>]),
    Log(&'a LogValue),
}

pub(super) fn datom_tuple(datom: Datom) -> Vec<Value> {
    vec![
        Value::Ref(datom.entity),
        Value::Ref(u64::from(datom.attribute)),
        datom.value,
        Value::Ref(datom.tx),
        Value::Bool(datom.added),
    ]
}

pub(super) fn log_function(
    function: Function,
    source: &str,
    args: &[BoundValue],
    binding: &Binding,
    state: &mut State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    let log = match state.sources.get(source) {
        Some(SourceRef::Log(log)) => *log,
        Some(_) => {
            return Err(SemanticError::incorrect(
                "query/source-kind",
                "transaction functions require an immutable log source",
            ));
        }
        None => {
            return Err(SemanticError::incorrect(
                "query/unknown-source",
                format!("unknown source {source}"),
            ));
        }
    };
    let point = |value: &BoundValue| -> Result<Option<TimePoint>, SemanticError> {
        match value {
            BoundValue::Nil => Ok(None),
            BoundValue::Stored(Value::Long(t)) if *t >= 0 => Ok(Some(TimePoint::T(*t as u64))),
            BoundValue::Stored(Value::Ref(tx)) => Ok(Some(TimePoint::Tx(*tx))),
            BoundValue::Stored(Value::Instant(instant)) => Ok(Some(TimePoint::Instant(*instant))),
            _ => Err(SemanticError::incorrect(
                "query/log-time",
                "log bound must be nil, nonnegative T, transaction ref, or instant",
            )),
        }
    };
    let mut rows = Vec::new();
    match function {
        Function::TxIds => {
            let [start, end] = args else {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "tx-ids requires start and end",
                ));
            };
            let mut values = Vec::new();
            state.check(1)?;
            for tx in log.tx_ids(point(start)?, point(end)?)? {
                state.check(1)?;
                let value = Value::Ref(tx?);
                state.charge_value_bytes(std::mem::size_of::<Value>())?;
                values.push(QueryValue::Scalar(value));
                state.check_row_count(values.len())?;
            }
            return ground_output(
                &[BoundValue::Query(QueryValue::Collection(values))],
                binding,
            );
        }
        Function::TxData => {
            let [tx] = args else {
                return Err(SemanticError::incorrect(
                    "query/function-arity",
                    "tx-data requires one transaction",
                ));
            };
            let tx = match point(tx)? {
                Some(TimePoint::T(t)) => IndexTransaction::T(t),
                Some(TimePoint::Tx(tx)) => IndexTransaction::Tx(tx),
                _ => {
                    return Err(SemanticError::incorrect(
                        "query/log-transaction",
                        "tx-data requires a T or transaction ref",
                    ));
                }
            };
            state.check(1)?;
            for datom in log.tx_data(tx)?.unwrap_or_default() {
                state.check(1)?;
                state.stats.datoms_examined += 1;
                let tuple = datom_tuple(datom);
                state.charge_value_bytes(tuple.iter().fold(0usize, |bytes, value| {
                    bytes
                        .saturating_add(std::mem::size_of::<Value>())
                        .saturating_add(
                            usize::try_from(value.retained_heap_bytes()).unwrap_or(usize::MAX),
                        )
                }))?;
                rows.push(tuple.into_iter().map(BoundValue::Stored).collect());
                state.check_row_count(rows.len())?;
            }
        }
        _ => unreachable!("log function dispatch"),
    }
    let value = QueryValue::Collection(
        rows.into_iter()
            .map(|row: Vec<BoundValue>| {
                QueryValue::Tuple(row.into_iter().map(|value| value.query_value()).collect())
            })
            .collect(),
    );
    ground_output(&[BoundValue::Query(value)], binding)
}
