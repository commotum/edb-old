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
mod program;
mod pull;
mod query;
mod schema;
mod service;
mod transaction;
mod value;

pub use database::{
    Database, DatabaseView, EntityRef, SchemaChange, TxOp, TxReport, TxValue, View,
};
pub use datom::{Datom, IndexOrder};
pub use encoding::{
    Digest, DurableTransaction, IndexManifest, IndexSegment, SegmentRef, decode_index_manifest,
    decode_index_segment, decode_program, decode_schema, decode_transaction, encode_index_manifest,
    encode_index_segment, encode_program, encode_program_output, encode_schema, encode_transaction,
    program_hash, program_request_digest, request_digest, sha256, transaction_hash,
};
pub use error::{ErrorCategory, SemanticError};
pub use index::IndexPrefix;
pub use peer::{CacheStats, IndexBuildFault, IndexBuildReceipt, Peer, PostgresIndexer};
pub use postgres::{
    CommitFault, CommitReceipt, PostgresStore, ProgramCommitReceipt, TransactorLease,
};
pub use program::{
    Instruction, Program, ProgramControl, ProgramHash, ProgramInvocation, ProgramKind,
    ProgramOutput, ProgramRuntime,
};
pub use pull::{
    AttributeName, Entity, PullAttribute, PullControl, PullDirection, PullLimit, PullNested,
    PullPattern,
};
pub use query::{
    Aggregate, Binding, Clause, DataPattern, FindElement, FindSpec, Function, InputSpec, PlanStep,
    Predicate, Query, QueryControl, QueryEngine, QueryExtensions, QueryInput, QueryOutcome,
    QueryResult, QuerySource, QueryStats, QueryValue, Rule, Term, Variable,
};
pub use schema::{Attribute, Cardinality, Schema, TupleSpec, Unique, ValueType};
pub use service::{
    ReportSubscription, ServiceStats, ServiceTransactionReport, TransactionClient,
    TransactionRequest, TransactionService, TransactionServiceConfig, TransactionTicket,
};
pub use transaction::{AttributeRef, EntityMap, MapValue, TxCall, TxForm, TxFunctions};
pub use value::{Keyword, Symbol, Value};
