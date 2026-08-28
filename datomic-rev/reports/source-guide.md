# Recovered Peer source navigation guide

This guide is a map of the recovered Datomic Pro 1.0.7277 Peer source. It is
not a reconstruction of the original authors' design notes, naming choices, or
intent. Follow the named files and Vars below as observed implementation
boundaries, then use the validation reports to decide how strong a conclusion
the recovered source can support.

## Fidelity and provenance boundary

The recovery is bound to `peer-1.0.7277.jar` with SHA-256
`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`.
The [bytecode architecture inventory](bytecode-architecture.md) accounts for
5,517 classes: 5,470 Clojure AOT classes mapped to 142 recovered namespaces and
47 handwritten Java classes mapped to 43 source paths.

Keep these source roles separate:

- [`src-clj/`](../src-clj/) is the preferred implementation-reading surface.
  Its 142 namespace files are reconstructed Clojure source and are the
  Clojure inputs to the canonical source artifact.
- [`src-java/`](../src-java/) is a complete CFR evidence tree. Only the 43
  paths in the [handwritten Java source manifest](handwritten-java-sources.txt)
  are canonical Java build inputs. Files such as
  [`datomic/Peer.java`](../src-java/datomic/Peer.java),
  [`datomic/Connection.java`](../src-java/datomic/Connection.java), and
  [`datomic/Database.java`](../src-java/datomic/Database.java) are in that
  handwritten subset. The thousands of files named like
  `datomic/api$connect.java` are forensic views of generated Clojure AOT
  classes; use the corresponding `src-clj` namespace instead.
- [`resources/`](../resources/) contains the exact non-class Peer resources.
  These are runtime inputs, not recovered program text.
- [`reports/source-index/`](source-index/) is a deterministic navigation aid.
  It is not a replacement for reading a call site, and it is not a dynamic
  whole-program call graph.

The recovered files preserve executable structure, types, interfaces,
records, protocol methods, and many bytecode-required hints. AOT compilation
does not preserve original comments, formatting, all local names, or original
macro spelling. Do not infer those missing details or treat generated names as
domain terminology. The exact recovery and claim boundary is recorded in
[`recovery-validation.md`](recovery-validation.md).

## Where to start

### Public API

Start with [`datomic.api`](../src-clj/datomic/api.clj) for the Clojure-facing
wrappers. It exposes connection and database operations, queries, transaction
submission, time views, entities, pull, index access, cancellation, and
lifecycle calls.

For Java-facing contracts, read the handwritten
[`Peer`](../src-java/datomic/Peer.java),
[`Connection`](../src-java/datomic/Connection.java),
[`Database`](../src-java/datomic/Database.java),
[`Log`](../src-java/datomic/Log.java),
[`Datom`](../src-java/datomic/Datom.java),
[`Entity`](../src-java/datomic/Entity.java), and
[`ListenableFuture`](../src-java/datomic/ListenableFuture.java) sources. These
show the stable Java method surface and, in `Peer`, the Vars used to enter the
recovered Clojure implementation. Read method bodies in `src-clj` after the
bridge; the AOT-shaped Java files are evidence only.

### Connections and the Peer runtime

The main file is [`datomic.peer`](../src-clj/datomic/peer.clj). Useful landmarks
are `ConnectionState`, `TWatcherImpl`, `Connection`, `LocalConnection`,
`create-connection`, `get-connection`, `connect-uri`, `stop-connection`, and
`shutdown`.

Read outward from it according to the question:

- URI parsing and database-name resolution:
  [`datomic.uri`](../src-clj/datomic/uri.clj) and
  [`datomic.coordination`](../src-clj/datomic/coordination.clj).
- Transactor-side transport as seen by the Peer:
  [`datomic.connector`](../src-clj/datomic/connector.clj) and
  [`datomic.artemis-client`](../src-clj/datomic/artemis_client.clj).
- Reconnection and release cleanup:
  [`datomic.reconnector2`](../src-clj/datomic/reconnector2.clj) and
  [`datomic.cleanup`](../src-clj/datomic/cleanup.clj).
- Shared caches and storage lookup:
  [`datomic.domain`](../src-clj/datomic/domain.clj),
  [`datomic.cache`](../src-clj/datomic/cache.clj), and
  [`datomic.cluster-stack`](../src-clj/datomic/cluster_stack.clj).

`datomic.peer` contains both remote and in-memory connection paths. Keep the
`Connection` and `LocalConnection` method bodies distinct when following a
transaction or release path. The repository recovers the Peer; the licensed
transactor used by the external validation gates is a separate process, not an
implementation hidden elsewhere in this tree.

### Database values and transactions

[`datomic.db`](../src-clj/datomic/db.clj) is the central and largest source
file. Begin with `Datum`, `Attribute`, `IndexSet`, and `Db`, then move to the
functions involved in the operation being investigated. In particular,
`datoms` and `seek-datoms` are raw read paths, `with-tx+opts` is the local/pure
transaction path used by `LocalConnection`, and `accept-txes` is part of
incorporating transaction data into a database value.

[`datomic.transaction`](../src-clj/datomic/transaction.clj) contains the
Peer/transactor message representation, Fressian handlers, and
`create-procargs`. It should not be mistaken for a recovered transactor.
Schema and operation checks are split across
[`datomic.validators`](../src-clj/datomic/validators.clj),
[`datomic.builtins`](../src-clj/datomic/builtins.clj), and database-processing
code in `datomic.db`. Stored database functions are represented in
[`datomic.function`](../src-clj/datomic/function.clj).

### Query and Datalog

Read [`datomic.query`](../src-clj/datomic/query.clj) first for query shape,
parsing, caching, result shaping, pull integration, and the public `q`,
`query`, and `qseq` paths. Landmarks include `parse-query`, `load-query`,
`q*`, and `query*`.

Then read [`datomic.datalog`](../src-clj/datomic/datalog.clj) for relation and
join execution, rule preparation, scheduling, cancellation checks, and
`qsqr`. `eval-query`, `eval-rule`, and `eval-clause` are the lower-level
execution landmarks. Supporting surfaces include
[`datomic.query.support`](../src-clj/datomic/query/support.clj),
[`datomic.aggregation`](../src-clj/datomic/aggregation.clj),
[`datomic.pull`](../src-clj/datomic/pull.clj), and
[`datomic.extensions`](../src-clj/datomic/extensions.clj).

### Indexes

Use [`datomic.db`](../src-clj/datomic/db.clj) for database-facing index access
and [`datomic.index`](../src-clj/datomic/index.clj) for persistent index nodes,
lookup, loading, writing, and traversal. The low-level ordered structures and
iterators are in [`datomic.btset`](../src-clj/datomic/btset.clj) and
[`datomic.iter`](../src-clj/datomic/iter.clj).

For index integration into a live connection, read
[`datomic.adopter`](../src-clj/datomic/adopter.clj) beside the `notify-index`
method of `datomic.peer/Connection`. Full-text paths branch through
[`datomic.fulltext`](../src-clj/datomic/fulltext.clj),
[`datomic.fulltext-index`](../src-clj/datomic/fulltext_index.clj), and
[`datomic.lucene`](../src-clj/datomic/lucene.clj).

### Storage, log, and backup

Start at the narrow protocols and move upward:

- [`datomic.kv-store/KVStore`](../src-clj/datomic/kv_store.clj) is the
  key/value backend protocol.
- [`datomic.cluster`](../src-clj/datomic/cluster.clj) defines clustered value,
  pod, and reference operations. [`datomic.kv-cluster`](../src-clj/datomic/kv_cluster.clj)
  adapts a `KVStore` to that layer.
- [`datomic.log`](../src-clj/datomic/log.clj) contains log values, tails,
  transaction iteration, and log/index coordination.
- [`datomic.clusterfs`](../src-clj/datomic/clusterfs.clj) and
  [`datomic.treewalk`](../src-clj/datomic/treewalk.clj) support stored trees and
  traversal used by backup and maintenance paths.
- [`datomic.backup`](../src-clj/datomic/backup.clj) defines `Storage`,
  `ValueBackup`, `ValueRestore`, backup/restore jobs, root publication, and
  verification.

The Stage 2 report documents an important boundary: the recovered artifact
does not contain the proprietary `datomic.fsbackup` implementation. Its
PostgreSQL gate invokes recovered `datomic.backup` through a test-only
`Storage` adapter. Read
[`stage-2-validation.md`](stage-2-validation.md) before treating a backup
result as evidence about a public filesystem adapter or CLI.

### Concurrency and core2

There are several related, but non-identical, layers:

- [`datomic.promise`](../src-clj/datomic/promise.clj) implements
  `settable-future`, listener delivery, cancellation, and dereference behavior.
- [`datomic.future`](../src-clj/datomic/future.clj) combines futures with
  core.async channels; `filling-promise`, `future-call`, and `pfuture-call` are
  useful entry points.
- [`datomic.core2.async`](../src-clj/datomic/core2/async.clj) contains
  `aderef`, retry, `put-all!`, and the recovered IOC state machines.
- [`datomic.core2.thread`](../src-clj/datomic/core2/thread.clj) contains
  binding-preserving submission, `pfuture`, bounded mapping, thread factories,
  and observable pool construction.
- [`datomic.common`](../src-clj/datomic/common.clj) supplies shared scheduling,
  retry, `AsyncShutdown`, bounded dereference, and pool helpers.
- [`datomic.queue`](../src-clj/datomic/queue.clj) and
  [`datomic.cleanup`](../src-clj/datomic/cleanup.clj) cover queue protocols and
  phantom-reference cleanup.

The large generated blocks in `datomic.core2.async` are recovered IOC machine
source, not ordinary hand-written `go` forms. The structural IOC audit and the
bounded runtime cases are described separately in
[`recovery-validation.md`](recovery-validation.md) and
[`stage-3-validation.md`](stage-3-validation.md).

### Monitoring and backends

[`datomic.monitor`](../src-clj/datomic/monitor.clj) is the base statistics and
alarm surface; start with `Metrics`, `Statistics`, `snapshot-statistics`,
`add-stat`, and `alarm`. Process-wide collection and callback reporting are in
[`datomic.process-monitor`](../src-clj/datomic/process_monitor.clj). Logging
format and redaction helpers are in
[`datomic.slf4j`](../src-clj/datomic/slf4j.clj). The CloudWatch route is split
between [`datomic.aws-monitor`](../src-clj/datomic/aws_monitor.clj) and
[`datomic.cloudwatch`](../src-clj/datomic/cloudwatch.clj).

For backend selection, begin with
[`datomic.uri`](../src-clj/datomic/uri.clj), then the `create-cluster` methods in
[`datomic.coordination-ext`](../src-clj/datomic/coordination_ext.clj). That file
is the clearest dispatch map from protocol keywords to backend adapters.

For SQL/PostgreSQL, the concrete reading path is:

```text
datomic.coordination-ext/create-cluster :sql
  -> datomic.kv-sql-ext/kv-sql
  -> datomic.kv-sql/from-spec
  -> datomic.kv-sql/KVSql
  -> datomic.sql/{select,insert,update,delete}
```

The corresponding files are
[`coordination_ext.clj`](../src-clj/datomic/coordination_ext.clj),
[`kv_sql_ext.clj`](../src-clj/datomic/kv_sql_ext.clj),
[`kv_sql.clj`](../src-clj/datomic/kv_sql.clj), and
[`sql.clj`](../src-clj/datomic/sql.clj). `kv_sql_ext.clj` constructs or accepts
the connection source and validates it; `KVSql` implements the common
`KVStore` protocol; `datomic.sql` owns the JDBC statements.

Other backend entry points are grouped by family:

- in-memory/development: [`datomic.memory`](../src-clj/datomic/memory.clj),
  [`datomic.kv-mem`](../src-clj/datomic/kv_mem.clj), and
  [`datomic.h2`](../src-clj/datomic/h2.clj);
- DynamoDB and DynamoDB/S3:
  [`datomic.ddb-cluster`](../src-clj/datomic/ddb_cluster.clj),
  [`datomic.ddb-s3-cluster`](../src-clj/datomic/ddb_s3_cluster.clj),
  [`datomic.kv-dynamo`](../src-clj/datomic/kv_dynamo.clj), and
  [`datomic.ddb`](../src-clj/datomic/ddb.clj);
- Cassandra:
  [`datomic.kv-cassandra`](../src-clj/datomic/kv_cassandra.clj),
  [`datomic.kv-cassandra2`](../src-clj/datomic/kv_cassandra2.clj), and
  [`datomic.kv-cassandra3`](../src-clj/datomic/kv_cassandra3.clj), with their
  driver helpers in [`datomic.cassandra`](../src-clj/datomic/cassandra.clj)
  and [`datomic.cassandra-v4`](../src-clj/datomic/cassandra_v4.clj);
- value-store composition: [`datomic.val-cluster`](../src-clj/datomic/val_cluster.clj)
  and the [`datomic.core2.val-store`](../src-clj/datomic/core2/val_store.clj)
  family.

Presence in the recovered tree is not evidence that a backend has passed the
PostgreSQL validation matrix. Backend behavior claims must come from a gate
that names that backend.

## Key call and data flows

These are narrow reading routes confirmed by the recovered source and the
resolved edges in [`calls.tsv`](source-index/calls.tsv). Java calls, protocol
dispatch, and multimethod dispatch are included only where the named source
body makes the bridge explicit.

### Connect

1. `datomic.api/connect` calls the handwritten static `datomic.Peer/connect`.
2. `Peer.connect` invokes `datomic.peer/connect-uri` through its `CONNECT_URI`
   Var.
3. `connect-uri` calls `datomic.uri/parse-db`. It selects
   `connect-local-database` for `:mem`, otherwise `get-connection`.
4. `get-connection` calls `datomic.coordination/resolve-db-name`, consults the
   connection cache, and calls `create-connection` on a miss.
5. `create-connection` constructs the `Connection`, database and system
   clusters, watchers, and a `datomic.reconnector2/reconnector-ref`; its
   connection state creates the notifier and updater through
   `datomic.connector`.

### Remote transaction submission and result incorporation

1. `datomic.api/transact` invokes the `datomic.Connection/transact` method.
2. The `transactAsync` method on `datomic.peer/Connection` calls
   `datomic.transaction/create-procargs`, stores a settable future in
   `pending-txes`, and puts the request on `unsent-updates-queue`.
3. `datomic.connector/TransactorHornetConnector.start-updater` consumes that
   queue for transport. Incoming `:tx` messages dispatch through
   `datomic.connector/notify` to the connection's `notify-data` method.
4. `notify-data` calls `datomic.peer/accept-new-data`, updates `db-ref`,
   delivers the transaction report, and releases eligible sync watchers.

This is the Peer half of the protocol. The source does not contain the code
that executes the transaction inside a transactor.

### Query execution

1. `datomic.api/q` calls `datomic.query/q`.
2. `datomic.query/q` calls `q*`; `q*` obtains the compiled query through
   `query-cache` and calls `datomic.datalog/qsqr`.
3. A cache miss runs `load-query`, which calls `parse-query` and
   `datomic.datalog/prep-clauses` before compiling result construction.
4. `qsqr` calls `datomic.datalog/eval-query`; clause execution proceeds
   through `eval-clause`, with `eval-rule` for rule paths.
5. `q*` then performs grouping, pull shaping, and result-form conversion as
   requested by the parsed query.

### Persisted index adoption

1. The `notify-index` method on `datomic.peer/Connection` calls
   `datomic.index/find-index-root-id` and `datomic.index/load-index`.
2. It constructs an adoptable database with `datomic.db/db`.
3. `datomic.adopter/adopt-index` combines the persisted index state with the
   connection's current state.
4. On success, the connection releases eligible pending transaction, index,
   and background syncs against the adopted database.

### Backup and restore

1. `datomic.backup/backup` creates a `Storage` and calls `backup-db`.
2. `backup-db` builds a job with `create-backup-job`, copies values through
   `create-value-backup`, then calls `backup-roots` to store the restore-point
   roots.
3. `datomic.backup/restore` creates a `Storage` and calls `restore-db`.
4. `restore-db` uses `create-restore-job` and `create-value-restore`, then
   publishes the restored roots through `restore-roots`.
5. `verify-backup` reads the roots, derives reachable segment IDs, and calls
   `missing-seg-ids` and `unreadable-seg-ids`.

Stage 2 exercises this flow through its explicitly test-only storage adapter;
that adapter is not part of the recovered artifact.

## Reading generated source safely

- A file commonly begins with a top-level `do`, `in-ns`, repeated loading
  context, explicit imports, and later metadata resets. Those forms are part of
  the recovered loadable source; do not assume the repetition reflects how the
  unpublished source was formatted.
- Names such as `fn__21590`, `G__19482`, `map__19542`, and `p__19541` are
  generated or recovered locals. Use their enclosing Var or type method as the
  semantic landmark. Do not rename them when the task is source analysis.
- Namespace names use hyphens while many file paths use underscores. For
  example, `datomic.kv-sql-ext` maps to `datomic/kv_sql_ext.clj`. Use
  [`namespaces.tsv`](source-index/namespaces.tsv) rather than guessing a path.
- Methods inside `deftype`, `reify`, and protocol implementations are not
  ordinary top-level Vars. In `calls.tsv`, calls found inside a type can be
  grouped under the type name, such as source Var `Connection` in namespace
  `datomic.peer`.
- Java interop, protocol dispatch, multimethods, `resolve`, and dynamically
  required Vars can cross boundaries that the resolved-Var call index cannot
  express. Confirm the actual call site before concluding that an edge is
  absent.
- Primitive casts, array hints, receiver hints, field tags, and apparently
  redundant bindings may select a JVM overload or preserve ABI. Stage 1
  records concrete examples in `datomic.future/filling-promise`,
  `datomic.memory-size/memory-size`, and `datomic.common/pfuture` where small
  source shapes changed runtime behavior. Treat cleanup or simplification as a
  code change requiring bytecode and regression evidence.
- Stage 4 removed 160 warning records at 159 source sites across 11 namespaces,
  but only where the bound original bytecode selected a unique receiver, array,
  primitive, overload, or return shape. Those edits are implementation repairs,
  not readability cleanup. The separate `datomic.common/compare-byte-arrays`
  repair restores the equal-length loop result established by the original
  bytecode and focused behavior checks.
- The exact unresolved inventory deliberately remains 157 records: 150
  reflection warnings, six primitive-recur warnings, and one auto-boxing
  warning. See the [Stage 4 report](stage-4-validation.md) and the
  [machine-readable inventory](stage-4-unresolved-warnings.txt). A successful
  namespace load does not turn a remaining warning into proof of equivalent
  performance or dispatch, and a warning's removal is not by itself proof of
  semantic equivalence.

## Using the source index

The most useful checked-in tables are:

- [`namespaces.tsv`](source-index/namespaces.tsv): namespace, source path, line
  count, definition visibility counts, direct requires, and imports;
- [`dependencies.tsv`](source-index/dependencies.tsv): namespace-to-namespace
  `require`, resolved `call`, and qualified `reference` edges;
- [`calls.tsv`](source-index/calls.tsv): resolved recovered-Var calls, grouped
  by source namespace and source Var, with occurrence counts.

From the `datomic-rev` directory, a practical sequence is:

```bash
# Map a namespace to its file and immediate declared context.
awk -F '\t' '$1 == "datomic.peer" || NR == 1' \
  reports/source-index/namespaces.tsv

# Inspect both outgoing and incoming namespace edges.
awk -F '\t' '$1 == "datomic.peer" || $2 == "datomic.peer" || NR == 1' \
  reports/source-index/dependencies.tsv

# List resolved Var calls originating in one namespace.
awk -F '\t' '$1 == "datomic.query" || NR == 1' \
  reports/source-index/calls.tsv

# Find callers of one recovered Var.
awk -F '\t' '$3 == "datomic.datalog/qsqr" || NR == 1' \
  reports/source-index/calls.tsv
```

Use `namespaces.tsv` to choose the file, `dependencies.tsv` to choose adjacent
namespaces, and `calls.tsv` to choose specific Vars to inspect. Then read the
source body. Counts describe the recovered corpus; a missing row is not proof
that runtime dispatch cannot reach a target.

## Validation ladder and claim discipline

| Evidence | What it establishes | What it does not establish |
|---|---|---|
| [Bytecode architecture](bytecode-architecture.md) | Exact non-executing inventory, provenance classification, resource and dependency accounting for the bound Peer JAR. | Source semantics or runtime behavior. |
| [Recovery validation](recovery-validation.md) | Deterministic 142-namespace reconstruction, reader/load closure, checked JVM/runtime surfaces, focused regressions, IOC structural audit, and the documented in-memory parity workload. | Literal unpublished source or exhaustive behavior. |
| [Stage 1](stage-1-validation.md) | Reproducible source-bearing candidate artifact, exact origins, no original Peer/core2 AOT fallback, fresh-JVM namespace and surface gates. | Every backend, workload, or schedule. |
| [Stage 2](stage-2-validation.md) | Bounded PostgreSQL lifecycle plus recovered backup/restore, corruption, interrupted restore, retry, and post-recovery writes, with the licensed transactor isolated as an external fixture. | A recovered transactor, proprietary `file:` adapter/CLI, or other storage backends. |
| [Stage 3](stage-3-validation.md) | Bounded fresh-JVM promise/IOC/pool/query cases, connection lifecycle race, 8-by-8 SQL CAS contention, and verified transactor pause/recovery. | All schedules, long partitions, multi-transactor failover, or security behavior. |
| [Stage 4](stage-4-validation.md) | Instruction-level-oracle repairs at 159 warning sites, one separately demonstrated return-value defect repair, an exact 157-record unresolved-warning baseline, and the accumulated source-artifact gates named in the report. | Broad warning elimination, stylistic normalization, or correctness of warning sites left unresolved. |
| [Stage 5](stage-5-readability.md) | A conservative source map and regenerated deterministic semantic index for the final 142-namespace tree, with relationship-table stability checked after Stage 4. | New runtime evidence, original author intent, or a complete dynamic call graph. |

The strongest supported statement is therefore scoped: this is a
bytecode-bound recovery of the Peer implementation for one exact Datomic Pro
version, with the structural and bounded behavioral evidence named in those
reports. It is not the unpublished original source, a recovered transactor, or
a proof over every backend and interleaving. This guide adds no validation
claim of its own; it only makes the checked evidence and recovered source
easier to navigate.
