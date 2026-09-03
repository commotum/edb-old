//! Native single-process transactional kernel for Atomic.
//!
//! The kernel applies declarative transaction information to immutable
//! database values and exposes Datomic-shaped indexes and temporal views. It
//! deliberately contains no PostgreSQL, network, query, or pull machinery.

mod database;
mod datom;
mod encoding;
mod error;
mod index;
mod peer;
mod postgres;
mod schema;
mod transaction;
mod value;

pub use database::{
    Database, DatabaseView, EntityRef, SchemaChange, TxOp, TxReport, TxValue, View,
};
pub use datom::{Datom, IndexOrder};
pub use encoding::{
    Digest, DurableTransaction, IndexManifest, IndexSegment, SegmentRef, decode_index_manifest,
    decode_index_segment, decode_schema, decode_transaction, encode_index_manifest,
    encode_index_segment, encode_schema, encode_transaction, request_digest, sha256,
    transaction_hash,
};
pub use error::{ErrorCategory, SemanticError};
pub use index::IndexPrefix;
pub use peer::{CacheStats, IndexBuildFault, IndexBuildReceipt, Peer, PostgresIndexer};
pub use postgres::{CommitFault, CommitReceipt, PostgresStore};
pub use schema::{Attribute, Cardinality, Schema, TupleSpec, Unique, ValueType};
pub use transaction::{AttributeRef, EntityMap, MapValue, TxCall, TxForm, TxFunctions};
pub use value::{Keyword, Symbol, Value};
