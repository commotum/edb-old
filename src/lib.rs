//! Native single-process transactional kernel for Atomic.
//!
//! The kernel applies declarative transaction information to immutable
//! database values and exposes Datomic-shaped indexes and temporal views. It
//! deliberately contains no PostgreSQL, network, query, or pull machinery.

mod database;
mod datom;
mod error;
mod index;
mod schema;
mod transaction;
mod value;

pub use database::{
    Database, DatabaseView, EntityRef, SchemaChange, TxOp, TxReport, TxValue, View,
};
pub use datom::{Datom, IndexOrder};
pub use error::{ErrorCategory, SemanticError};
pub use index::IndexPrefix;
pub use schema::{Attribute, Cardinality, Schema, TupleSpec, Unique, ValueType};
pub use transaction::{AttributeRef, EntityMap, MapValue, TxCall, TxForm, TxFunctions};
pub use value::{Keyword, Symbol, Value};
