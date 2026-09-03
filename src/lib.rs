//! Native single-process transactional kernel for Atomic.
//!
//! The kernel applies declarative transaction information to immutable
//! database values and exposes Datomic-shaped indexes and temporal views. It
//! deliberately contains no PostgreSQL, network, query, or pull machinery.

mod backup;
mod database;
mod database_value;
mod datom;
mod encoding;
mod error;
mod identity;
mod idents;
mod index;
mod operations;
mod peer;
pub mod persistent_tree;
mod postgres;
#[cfg(test)]
mod postgres_internal_tests;
mod program;
mod pull;
mod query;
pub mod recent;
mod recent_btset;
mod schema;
mod service;
mod state_commitment;
mod transaction;
mod tree_manifest;
mod tree_store;
mod value;
mod vocabulary;

pub use backup::{BackupPoint, BackupVerification, PortableBackup};
pub use database::{Database, EntityRef, TxOp, TxReport, TxValue, View};
pub use database_value::DatabaseValue;
pub use datom::{Datom, IndexOrder};
pub use encoding::{
    Digest, DurableTransaction, IndexManifest, IndexSegment, SegmentRef, decode_genesis,
    decode_index_manifest, decode_index_segment, decode_program, decode_transaction,
    encode_genesis, encode_index_manifest, encode_index_segment, encode_program,
    encode_program_output, encode_transaction, program_hash, program_request_digest,
    request_digest, sha256, submission_request_digest, transaction_hash,
};
pub use error::{ErrorCategory, SemanticError};
pub use identity::{
    DB_PARTITION, EIDX_BITS, EIDX_MASK, INITIAL_EIDX_FRONTIER, MAX_EID, MAX_EIDX, MAX_PARTITION,
    PARTITION_BITS, TX_PARTITION, USER_PARTITION, eid_to_eidx, eid_to_part, make_eid, t_to_tx,
    tx_to_t,
};
pub use index::IndexPrefix;
pub use operations::{
    ExcisionFault, ExcisionReceipt, ExcisionSpec, ExcisionTarget, GarbageInventory,
    IntegrityProblem, IntegrityReport, OperationalMetrics, PostgresOperator,
};
pub use peer::{
    CacheStats, IndexBuildFault, IndexBuildReceipt, Peer, PeerCursorStats, PeerIndexCursor,
    PeerLoadStats, PeerSnapshot, PostgresIndexer, RecoveryStats,
};
pub use postgres::{CapacityLimits, PostgresStore, ProgramCacheStats};
pub use program::{
    CallableRef, Instruction, MAX_QUERY_PATTERNS, MAX_QUERY_VARIABLES, PROGRAM_ABI_VERSION,
    Program, ProgramBudget, ProgramCall, ProgramControl, ProgramHash, ProgramInvocation,
    ProgramKind, ProgramLimits, ProgramOutput, ProgramRuntime, QUERY_TEMPLATE_VERSION,
    QueryPattern, QueryTemplate, QueryTerm, RuntimeValue, is_exact_true, require_exact_true,
};
pub use pull::{
    AttributeName, Entity, EntityIdentifier, EntityValue, PullAttribute, PullControl, PullDirection,
    PullLimit, PullNested, PullPattern,
};
pub use query::{
    Aggregate, Binding, Clause, DataPattern, FindElement, FindSpec, Function, InputSpec, PlanStep,
    Predicate, Query, QueryControl, QueryEngine, QueryExtensions, QueryInput, QueryOutcome,
    QueryResult, QuerySource, QueryStats, QueryValue, Rule, Term, Variable,
};
pub use schema::{Attribute, Cardinality, Schema, TupleSpec, Unique, ValueType};
pub use service::{
    BackgroundIndexingConfig, BackgroundIndexingFailure, BackgroundIndexingStats,
    ReportSubscription, ServiceStats, ServiceTransactionReport, TransactionClient,
    TransactionRequest, TransactionService, TransactionServiceConfig, TransactionStandby,
    TransactionTicket,
};
pub use transaction::{AttributeRef, EntityMap, MapValue, TxCall, TxForm, TxFunctions};
pub use tree_manifest::{ManifestTree, PersistentTreeManifest};
pub use tree_store::{
    PostgresTreeStore, TreeManifestRecord, TreePublishOutcome, TreeRootBinding, TreeStoreStats,
};
pub use value::{Keyword, Symbol, Value};
pub use vocabulary::{
    DB_ADD, DB_ALTER_ATTRIBUTE, DB_ATTR_PREDS, DB_CARDINALITY, DB_CARDINALITY_MANY,
    DB_CARDINALITY_ONE, DB_DOC, DB_ENSURE, DB_ENTITY_ATTRS, DB_ENTITY_PREDS, DB_FN, DB_FN_CAS,
    DB_FN_RETRACT_ENTITY, DB_FULLTEXT, DB_IDENT, DB_INDEX, DB_INSTALL_ATTRIBUTE, DB_IS_COMPONENT,
    DB_NO_HISTORY, DB_PART_DB, DB_PART_TX, DB_PART_USER, DB_RETRACT, DB_TUPLE_ATTRS,
    DB_TUPLE_DISCONTINUED, DB_TUPLE_TYPE, DB_TUPLE_TYPES, DB_TX_INSTANT, DB_TYPE_BIGDEC,
    DB_TYPE_BIGINT, DB_TYPE_BOOLEAN, DB_TYPE_BYTES, DB_TYPE_DOUBLE, DB_TYPE_FLOAT, DB_TYPE_FN,
    DB_TYPE_INSTANT, DB_TYPE_KEYWORD, DB_TYPE_LONG, DB_TYPE_REF, DB_TYPE_STRING, DB_TYPE_SYMBOL,
    DB_TYPE_TUPLE, DB_TYPE_URI, DB_TYPE_UUID, DB_UNIQUE, DB_UNIQUE_IDENTITY, DB_UNIQUE_VALUE,
    DB_VALUE_TYPE, MAX_SCHEMA_ATTRIBUTE_ID, canonical_genesis_datoms, schema_eid_to_attr_id,
};
