//! Native Rust/PostgreSQL Datomic-inspired database.
//!
//! The kernel applies declarative transaction information to immutable
//! database values. PostgreSQL stores the serialized durable log and indexes;
//! independent peers provide local queries, pull, temporal views, and native
//! immutable snapshots. The pure kernel remains available as an explicit oracle.

mod allocation_storage;
mod backup;
mod block_codec;
mod change_consumer;
pub(crate) mod change_notices;
pub(crate) mod compressed_nodes;
mod connection;
mod cow_generation;
mod database;
pub(crate) mod database_catalog;
pub use database_catalog::{CreateDatabaseResult, DatabaseCatalog, DatabaseCatalogEntry};
pub mod database_invoke;
mod database_stats;
mod database_value;
mod datom;
pub mod edn;
pub mod edn_pull;
pub mod edn_query;
pub mod edn_transaction;
pub mod edn_value;
mod encoding;
mod entity_identity;
mod error;
mod excision;
mod fulltext;
mod fulltext_analysis;
mod fulltext_store;
mod identity;
mod idents;
mod index;
mod index_pull;
#[cfg(unix)]
mod local_transport;
mod log_generation;
mod operations;
mod overlay_index;
mod partitions;
mod reserved_allocation;
pub use partitions::TransactionDefaults;
mod native_registry;
mod peer;
mod persistent_commitment;
pub mod persistent_tree;
mod postgres;
mod postgres_connection;
#[cfg(test)]
mod postgres_internal_tests;
mod program;
mod pull;
mod query;
mod query_return_maps;
mod query_value_debug;
pub mod recent;
mod recent_btset;
#[cfg(unix)]
mod remote_config;
#[cfg(unix)]
mod remote_transport;
mod runtime_config;
#[cfg(unix)]
pub use remote_config::{remote_client_config_from_env, remote_server_credentials_from_env};
mod schema;
mod service;
mod shared_map;
pub mod sql_io;
mod ssd_cache;
mod state_commitment;
mod tiered_assessor;
mod time_point;
mod transaction;
mod transaction_hints;
mod tree_manifest;
mod tree_store;
mod uuid;
mod value;
mod vocabulary;

pub use backup::{BackupFault, BackupPoint, BackupVerification, PortableBackup, RestoreFault};
pub use change_consumer::{
    ChangeCheckpoint, ChangeConsumer, ChangeConsumerConfig, ChangeConsumerStats, ChangeEvent,
};
pub use change_notices::{
    NoticeListenerStats, NoticePublisherStats, ObservationConfig, notice_listener_stats,
    notice_publisher_stats,
};
pub use compressed_nodes::{NodeBlockReadStats, NodeBlockWriteStats};
pub use connection::{Connection, ConnectionTransactionTicket, DatabaseIdentity};
pub use database::{Database, EntityRef, TxOp, TxReport, TxValue, View};
pub use database_invoke::{InvokeControl, InvokeRole};
pub use database_stats::{AttributeStats, DatabaseStats};
pub use database_value::{
    DatabaseValue, DatabaseValuePrefixCursor, DatabaseValueScanCursor, RawIndexValue,
    SpeculativeTransactionReport,
};
pub use datom::{Datom, IndexOrder};
pub use encoding::{
    Digest, DurableTransaction, IndexManifest, IndexSegment, SegmentRef, decode_genesis,
    decode_index_manifest, decode_index_segment, decode_program, decode_transaction,
    encode_genesis, encode_index_manifest, encode_index_segment, encode_program,
    encode_program_output, encode_transaction, program_hash, program_request_digest,
    request_digest, sha256, submission_request_digest, transaction_hash,
};
pub use entity_identity::EntityIdentity;
pub use error::{ErrorCategory, SemanticError};
pub use fulltext::{FulltextHit, FulltextOptions, FulltextReport, FulltextStats};
pub use fulltext_store::{
    FulltextBuildFault, FulltextBuildLimits, FulltextBuildStats, FulltextCacheStats,
    FulltextCursor, FulltextProjection, FulltextReadLimits, FulltextReadStats, FulltextRecord,
    FulltextStore,
};
pub use identity::{
    DB_PARTITION, EIDX_BITS, EIDX_MASK, INITIAL_EIDX_FRONTIER, MAX_EID, MAX_EIDX, MAX_PARTITION,
    PARTITION_BITS, TX_PARTITION, USER_PARTITION, eid_to_eidx, eid_to_part, implicit_part,
    implicit_part_id, make_eid, partition_eid, t_to_tx, tx_to_t,
};
pub use index::{IndexBoundary, IndexComponents, IndexPrefix, IndexTransaction};
pub use index_pull::{IndexPullCursor, IndexPullOptions};
#[cfg(unix)]
pub use local_transport::{
    CommittedTransaction, LocalTransactionEndpoint, LocalTransactionServer, LocalTransportConfig,
};
pub use native_registry::{
    NativeCallContext, NativeRegistry, NativeRegistryBuilder, TransactionExecutionOptions,
    native_deployment_attribute, native_deployment_ident,
};
pub use operations::{
    ExcisionFault, ExcisionReceipt, GarbageInventory, IntegrityProblem, IntegrityReport,
    LogGenerationGarbage, MAX_DATABASE_RECLAMATION_ROWS, MAX_LOG_GENERATION_ROWS_PER_GC,
    MAX_LOG_GENERATIONS_PER_GC, MAX_PROGRAMS_PER_GC, MAX_RECEIPT_ARCHIVE_WORK_PER_GC,
    MAX_REQUEST_BASE_ARCHIVE_NODES_PER_GC, MAX_SEMANTIC_COMMITMENT_NODES_PER_GC,
    MAX_SEMANTIC_COMMITMENT_ROOTS_PER_GC, MAX_TREE_BUILD_INTENT_NODES_PER_GC,
    MAX_TREE_BUILD_INTENTS_PER_GC, MAX_TREE_NODES_PER_GC, MAX_TREE_RETIREMENT_NODES_PER_GC,
    MAX_TREE_RETIREMENTS_PER_GC, OperationalMetrics, PostgresOperator,
    RECOMMENDED_GARBAGE_COLLECTION_AGE, ReceiptArchiveConversion, RequestBaseArchiveGarbage,
    RetiredDatabaseReclamation, SemanticCommitmentRootGarbage, TreeBuildIntentGarbage,
    TreePublicationGarbage,
};
pub use peer::NativeFulltextReader;
pub use peer::native_log::{LogCursor, LogCursorStats, LogTransaction, LogValue};
pub use peer::snapshot_reference::{SnapshotKey, SnapshotReference};
pub use peer::{
    CacheStats, IndexBuildFault, IndexBuildReceipt, Peer, PeerCursorStats, PeerIndexCursor,
    PeerLoadStats, PeerSnapshot, PostgresIndexer, RecoveryStats,
};
pub use postgres::{
    CapacityLimits, DatabaseStatus, POSTGRES_IN_PLACE_UPGRADE_FLOOR, POSTGRES_SCHEMA_VERSION,
    PostgresMigrator, PostgresStore, ProgramCacheStats, WriterResidencyStats,
};
pub use postgres_connection::{PostgresConnectionConfig, PostgresIoPolicy};
pub use program::{
    CallableRef, DATA_FUNCTION_QUERY_TEMPLATE_VERSION, GENERAL_QUERY_TEMPLATE_VERSION, Instruction,
    MAX_QUERY_PATTERNS, MAX_QUERY_VARIABLES, NATIVE_QUERY_TEMPLATE_VERSION, PROGRAM_ABI_VERSION,
    Program, ProgramBudget, ProgramCall, ProgramControl, ProgramHash, ProgramInvocation,
    ProgramKind, ProgramLimits, ProgramOutput, ProgramRuntime, QUERY_TEMPLATE_VERSION,
    QueryPattern, QueryTemplate, QueryTemplateSource, QueryTemplateTime, QueryTerm, RuntimeValue,
    is_exact_true, require_exact_true,
};
pub use pull::{
    AttributeName, Entity, EntityIdentifier, EntityValue, PullAttribute, PullControl,
    PullDirection, PullLimit, PullNested, PullPattern, PullTransform,
};
pub use query::{
    Aggregate, AggregateArg, AggregateCall, AggregateGroup, AggregateSource, AggregateValue,
    Binding, Clause, DataPattern, FindElement, FindSpec, Function, InputSpec, PlanStep, Predicate,
    PreparedQuery, PreparedQueryCache, PreparedQueryCacheStats, Query, QueryControl,
    QueryDataSource, QueryEngine, QueryExtensions, QueryInput, QueryOutcome, QueryResult,
    QuerySequence, QuerySource, QuerySourceValue, QueryStats, QueryValue, RelationPattern, Rule,
    Term, Variable,
};
pub use query_return_maps::{ReturnMap, ReturnMapShape, ReturnMaps};
#[cfg(unix)]
pub use remote_transport::{
    RemoteAuthToken, RemoteClientConfig, RemoteTransactionEndpoint, RemoteTransactionServer,
    RemoteTransportConfig, RemoteTransportStats, RemoteWriterEndpoint,
};
pub use runtime_config::postgres_config_from_env;
pub use schema::{Attribute, Cardinality, Schema, TupleSpec, Unique, ValueType};
pub use service::{
    BackgroundFulltextStats, BackgroundIndexingConfig, BackgroundIndexingFailure,
    BackgroundIndexingStats, IndexRequest, ReportSubscription, ServiceStats,
    ServiceTransactionReport, TransactionClient, TransactionRequest, TransactionService,
    TransactionServiceConfig, TransactionStandby, TransactionTicket,
};
pub use sql_io::{
    OperationContext, OperationKind, SqlCallKind, SqlCallStats, SqlIoReport, SqlIoStats,
    SqlMetricCallback, process_sql_stats,
};
pub use ssd_cache::{SsdCache, SsdCacheConfig, SsdCacheLimits, SsdCacheStats};
pub use time_point::TimePoint;
pub use transaction::SpeculationLimits;
pub use transaction::{AttributeRef, EntityMap, MapValue, TxCall, TxForm, TxFunctions};
pub use transaction_hints::{
    HintExecution, HintLimits, HintPrefetchOptions, HintPrefetchStats, HintTraceStats,
    HintedSpeculation, ReadHint, TransactionHints,
};
pub use tree_manifest::{AvetProjectionWork, ManifestTree, PersistentTreeManifest};
pub use tree_store::{
    NodeUploadLimits, PostgresTreeStore, TreeManifestRecord, TreePublicationDelta,
    TreePublishOutcome, TreeRootBinding, TreeStoreStats,
};
pub use uuid::{squuid, squuid_at, squuid_time_millis, uuid_v7, uuid_v7_at, uuid_v7_time_millis};
pub use value::{Keyword, Symbol, Value};
pub use vocabulary::{
    DB_ADD, DB_ALTER_ATTRIBUTE, DB_ATTR_PREDS, DB_CARDINALITY, DB_CARDINALITY_MANY,
    DB_CARDINALITY_ONE, DB_DOC, DB_ENSURE, DB_ENTITY_ATTRS, DB_ENTITY_PREDS, DB_EXCISE,
    DB_EXCISE_ATTRS, DB_EXCISE_BEFORE, DB_EXCISE_BEFORE_T, DB_FN, DB_FN_CAS, DB_FN_RETRACT_ENTITY,
    DB_FULLTEXT, DB_IDENT, DB_INDEX, DB_INSTALL_ATTRIBUTE, DB_INSTALL_PARTITION, DB_IS_COMPONENT,
    DB_NO_HISTORY, DB_PART_DB, DB_PART_TX, DB_PART_USER, DB_RETRACT, DB_TUPLE_ATTRS,
    DB_TUPLE_DISCONTINUED, DB_TUPLE_TYPE, DB_TUPLE_TYPES, DB_TX_INSTANT, DB_TYPE_BIGDEC,
    DB_TYPE_BIGINT, DB_TYPE_BOOLEAN, DB_TYPE_BYTES, DB_TYPE_DOUBLE, DB_TYPE_FLOAT, DB_TYPE_FN,
    DB_TYPE_INSTANT, DB_TYPE_KEYWORD, DB_TYPE_LONG, DB_TYPE_REF, DB_TYPE_STRING, DB_TYPE_SYMBOL,
    DB_TYPE_TUPLE, DB_TYPE_URI, DB_TYPE_UUID, DB_UNIQUE, DB_UNIQUE_IDENTITY, DB_UNIQUE_VALUE,
    DB_VALUE_TYPE, MAX_SCHEMA_ATTRIBUTE_ID, canonical_genesis_datoms,
    fulltext_vocabulary_upgrade_ops, partition_vocabulary_upgrade_ops, schema_eid_to_attr_id,
};
