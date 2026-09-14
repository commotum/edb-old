# ATOMIC-NOTE: public facades, observation and asynchronous outcomes

Development trace for the [Clojure API](00_datomic_api.md), the adjacent
[Java package](../01_java/00_package_datomic.md), and
[error handling](../02_shared_reference/01_error_handling.md). Source baseline:
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d` (1.0.7705). Symbol names are stable
locators; recovered metadata and current web-derived reference pages can differ.
The rows below cover method families, not a claim that every initializer or JVM
interop edge is an independent native feature. Checks are locators, not fresh
test execution.

## Facade and captured values

| Pro passage / method family | Exact recovered mechanism | Current native owner and disposition / focused check |
| --- | --- | --- |
| `connect`, Java `Peer.connect`, `Connection.db/log`; thread-safe long-lived connection paragraphs | [peer.clj](../../../1.0.7705/peer/src-clj/datomic/peer.clj) `connect-uri`, `get-connection`, `Connection.db`, `Connection.log`; [Peer.java](../../../1.0.7705/peer/src-java/datomic/Peer.java) static Var bindings | [application/connection.rs](../../../src/application/connection.rs) composes submission and [peer/live.rs](../../../src/peer/live.rs) observation; [database_value/value.rs](../../../src/database_value/value.rs) owns a captured value. Native clones share an identity, not a JVM URI-global connection cache. `tests/native_connection.rs::writer_replacement_preserves_connection_identity_and_captured_values`. |
| `sync` versus `sync(t)`, `sync-index/schema/excise`; Java overloads | `Connection.sync` queues a barrier without t; `TWatcherImpl`/`sync-t`/`sync-background-t` wait for distinct local coordinates | `Connection::{sync,sync_to,sync_index,sync_schema,sync_excise}` and peer readiness. Retain target-specific completion, not “latest observed means every background task finished.” `tests/native_connection.rs::physical_index_and_excision_sync_require_their_own_completion`. |
| `transact`, `transact-async`, `with`, temporary IDs and report fields | `Connection.transactAsync` registers a settable future then queues request data; `await-tx-result` waits without cancelling; `notify-data` integrates before delivery; `db/with-tx+opts` assesses locally | Shared [transaction pipeline](../../../src/transaction/pipeline.rs), application submission and native durable receipts. [Processing trace](../../04_transactions/03_processing_transactions.atomic.md) distinguishes unknown outcomes from rejection and exact-key retry from source request correlation. `tests/async_client.rs::postgres_captured_reads_dropped_transaction_retry_restart_and_cleanup`. |
| `tx-report-queue`, `remove-tx-report-queue`; submitting future delivered first | `Connection.txReportQueue` installs one unbounded queue; `notify-data` supplies captured before/after values; `notify-db` reconnect does not fabricate individual reports | Peer reports are distinct from [observation/notices.rs](../../../src/observation/notices.rs) discardable wakeups and [observation/consumer.rs](../../../src/observation/consumer.rs) log replay/checkpoints. Native replay repairs dropped hints; acknowledgement requires authorized reference writes. `tests/change_consumer.rs::consumer_reconnect_repairs_a_dropped_notice_from_the_log`; `tests/native_connection.rs::a_lagging_report_queue_does_not_skip_transactions_across_excision`. |
| `release`, `shutdown`; read-only connection exceptions | `Connection.release` removes matching cached entries and dereferences async shutdown; `shutdown` drains cached connections; `connect-uri` selects fixed storage/backup values separately | Native explicit ownership and worker cleanup, not Java process-global release semantics. Retained handles do not pin durable objects beyond retention grace. [Deployment trace](../../08_operations/00_architecture_and_storage/02_datomic_deployment.atomic.md) and [read-only disposition](../../09_optional/02_specialized_operations/00_read_only_connections.atomic.md). Source release body waits on a future despite broad asynchronous wording; do not derive a strict nonblocking guarantee from that wording. |

## Query, navigation and data interfaces

| Pro passage / method family | Source symbol | Native trace / disposition |
| --- | --- | --- |
| Java `q`, generic `query`, `QueryRequest.create/timeout/asData`, `qseq`; Clojure equivalents | [Peer.java](../../../1.0.7705/peer/src-java/datomic/Peer.java) delegates to `query/{q,query,qseq}`; [QueryRequest.java](../../../1.0.7705/peer/src-java/datomic/QueryRequest.java) shallow request map | [Query trace](../../05_query_and_pull/02_query_reference.atomic.md) and [execution trace](../../05_query_and_pull/01_executing_queries.atomic.md). Prepared AST, explicit sources and `QueryControl` remain separate. Java generic casts are not a second result engine. `tests/query_result_contracts.rs`; `tests/query_runtime_sources.rs`. |
| `Database`, `Database.Predicate`, `Datom`, `Attribute`: identity, five datom fields, immutable derivations, filter input, schema flags | [Database.java](../../../1.0.7705/peer/src-java/datomic/Database.java) includes nested `Predicate`; [db.clj](../../../1.0.7705/peer/src-clj/datomic/db.clj) `Db`, `Datum`, schema/identifier resolution | [Filter trace](../../02_core_concepts/02_database_filters.atomic.md), [identity trace](../../03_schema/03_identity_and_uniqueness.atomic.md), [index trace](../../06_indexes/01_index_model.atomic.md). Native model/schema and database_value are shared owners. A separate `DatabasePredicate.java` is not missing: the interface is nested. |
| `entity`, `touch`, `entity-db`, `pull`, `pull-many`, `index-pull` | `query/EntityMap`, `pull/{pull*,index-pull}` and Java `Entity` | [Pull trace](../../05_query_and_pull/03_pull.atomic.md); lazy handle cache, explicit immutable basis, reverse direction and missing projection behavior have independent typed/EDN tests. JVM collection protocols are adapters. |
| `datoms`, `seek-datoms`, `rseek-datoms`, `index-range`, `entid-at`; `Log.txRange` | `Db` index methods, `windowed`, `LogValue.txRange` | [Index trace](../../06_indexes/01_index_model.atomic.md), [log trace](../02_shared_reference/00_log_api.atomic.md). Prefix termination differs from unbounded seek; range end is exclusive. Native [database_value/cursor.rs](../../../src/database_value/cursor.rs) and [database_value/log.rs](../../../src/database_value/log.rs). |
| `function`, `invoke`, `cancel`; `Util` collection/reader helpers | `function/construct`, `Db.invoke`, `api/cancel`; [Util.java](../../../1.0.7705/peer/src-java/datomic/Util.java) invokes EDN readers | Native [database_value/invoke.rs](../../../src/database_value/invoke.rs), explicit native computation and persisted programs; [data/EDN trace](../../02_core_concepts/01_programming_with_data_and_edn.atomic.md). No classloader/eval/Fressian compatibility promise. `Util` null list/map branches return mutable empty collections despite the general immutable wording; native ownership need not reproduce this host quirk. |

## Completion, error and reconnection mechanisms

The [ListenableFuture.addListener paragraph](../01_java/07_listenable_future.md)
promises one execution through a supplied executor, including registration after
completion; it does not promise listener ordering. In
[promise.clj](../../../1.0.7705/peer/src-clj/datomic/promise.clj)
`settable-future`, a sentinel CAS selects completion, and a shared listener lock
closes the registration/countdown race. Execution happens outside that lock.
`cancel` only attempts delivery of a cancellation exception: it does not
interrupt a submitted transaction. `call-user-code` catches executor submission
failure, not every possible later failure inside an asynchronous executor.

[Error Handling](../02_shared_reference/01_error_handling.md), “Wrapped
Exceptions” and “Arbitrary Java Exceptions,” maps to
`promise/throw-executionexception-if-throwable`,
[error/deserialize-exception](../../../1.0.7705/peer/src-clj/datomic/error.clj)
and [anomalizer/category-delegate, throwable-category,
throwable->anom](../../../1.0.7705/peer/src-clj/datomic/anomalizer.clj).
Peer `notify-error` reconstructs structured errors before future delivery. The
source explicitly cannot guarantee every foreign exception is normalized.
Native [SemanticError](../../../src/error.rs) and transport codecs preserve
categories/data without Java class reconstruction. Error receipt is not proof
that a transaction failed to commit.

[reconnector2/Reconnector](../../../1.0.7705/peer/src-clj/datomic/reconnector2.clj)
serializes reconnect/shutdown around a worker reference and replaceable promise.
Peer `create-connection` supplies endpoint rediscovery/retry and fails pending
requests. Native peer observation and transport routing remain separate from
receipt-first retries. The fixed [runtime executor](../../../src/runtime/executor/mod.rs)
and [async application facade](../../../src/application/asynchronous/client.rs)
use wakeable results, bounded admission and worker-side abandoned-resource
cleanup. Query deadlines include queued waiting; an admitted transaction remains
owned after its waiter is dropped. Existing checks:
`tests/async_client.rs::database_free_callbacks_do_not_block_and_queued_deadlines_count_waiting`,
`query_streams_demand_drive_pull_fuse_errors_and_wait_for_worker_admission`, and
`streams_resume_after_full_admission_and_honor_cancellation`.

Two source distinctions prevent false parity claims. [async/daemon](../../../1.0.7705/peer/src-clj/datomic/async.clj)
conveys dynamic bindings to a named daemon; it is not a bounded executor.
[future/add-bounding-warning](../../../1.0.7705/peer/src-clj/datomic/future.clj)
records an alarm, not an operation deadline. Its recovered threshold branch
exits via states 4→6→3, contrary to metadata saying it continues waiting. No
in-corpus caller was found. Keep this discrepancy as recovered-source evidence,
not a required native behavior or a verified statement about another release.

Queue admission is another explicit distinction. `Connection.transactAsync`
uses [queue/put](../../../1.0.7705/peer/src-clj/datomic/queue.clj) on the
128-entry `ArrayBlockingQueue` created by `create-connection`. That extension
calls blocking Java `put` and then returns true: the caller's false-result branch
does not reject saturation for this concrete queue. “Returns immediately” means
the future precedes transaction completion, not necessarily nonblocking local
admission. Atomic's bounded Busy admission and asynchronous completion are a
deliberate service-availability policy, not source-equivalent queue behavior.

Configured [callback/create-callback](../../../1.0.7705/peer/src-clj/datomic/callback.clj)
is reached by process metrics and Cassandra configuration. It resolves Clojure
Vars or public static Java methods; it is not query extension registration.
Native explicit callback registries preserve application computation without
requiring JVM host conveniences or non-target Cassandra integrations.
