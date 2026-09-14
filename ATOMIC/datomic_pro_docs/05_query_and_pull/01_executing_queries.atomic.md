# ATOMIC-NOTE: acquiring, executing and consuming query results

This companion closes the entry-point paragraphs in
[Query](00_query.md) and [Executing Queries](01_executing_queries.md); the
[query reference trace](02_query_reference.atomic.md) owns grammar and engine
details. No reference text is changed and no benchmark ratio is adopted.

| Exact passage family | Source symbol / current owner | Focused evidence and disposition |
| --- | --- | --- |
| “Querying a Database,” `q`, and the overview's declarative-query examples | [peer/Connection.db](../../1.0.7705/peer/src-clj/datomic/peer.clj) captures one value; [query/q, q*](../../1.0.7705/peer/src-clj/datomic/query.clj) execute locally; [query/execute.rs](../../src/query/execute.rs) | `tests/edn_query_pull.rs` and `tests/query_runtime_sources.rs`. Native typed/EDN entry points share the same captured value and result engine. Tuple-only introductory prose does not erase scalar, tuple, collection and return-map shapes in the reference. |
| “qseq,” especially deferred Pull/xform and count | `query/qseq`, `FindRel` projection; [query/sequence.rs](../../src/query/sequence.rs) | `tests/query_result_contracts.rs`; `tests/async_client.rs::query_streams_demand_drive_pull_fuse_errors_and_wait_for_worker_admission`. Relational execution remains eager; only item projection is deferred. Small value-byte limits survive into lazy Pull, and async chunking does not promise streaming joins. |
| “Unification,” “List Form vs. Map Form” | `query/parse-query`, `load-query`; `datalog` joins; [edn_query.rs](../../src/edn_query.rs), [query/bindings.rs](../../src/query/bindings.rs) | Existing EDN and runtime-source cases cover list/map parsing and input joins. Native EDN is data, not eval; equality and exact numeric adaptations are in the reference trace. |
| “Timeout” | `query/query` and query cancellation checks; [query/control.rs](../../src/query/control.rs) | `tests/query_runtime_sources.rs` controls; [fulltext control regression](../../tests/query_fulltext.rs). Cooperative cancellation/work/value bounds are explicit. Neither a Java future timeout nor a warning watcher proves that an operation was interrupted. |
| “Clause Order,” including the McCartney ratio | `datalog/sched-in-order`, query dependency preparation; [query/execute.rs](../../src/query/execute.rs) ready-clause scheduling | Native bound-score scheduling and bounded fallback scans are adaptations. Selectivity advice still applies, but the source example's 50× ratio is not a native measurement or guaranteed optimizer outcome. |
| “Query Caching,” parameterization and structural equality | `query/load-query` cache; [query/prepare.rs](../../src/query/prepare.rs) | `tests/query_runtime_sources.rs` cache capacity/eviction, changed inputs and oversize bypass. Cache keys describe query structure, not DB contents or bindings; cached plans must never reuse old answers. |

Related [index APIs](../06_indexes/04_index_apis.md),
[(r)seek-datoms](../06_indexes/05_rseek_datoms.md) and
[index-Pull](../06_indexes/03_index_pull.md) do not add another query engine.
Their covering-datom, prefix/seek-position, direction and end-bound paragraphs
map to `Db.datoms/seekDatoms/rseekDatoms/indexRange`, `windowed` iterators and
`pull/index-pull`. Current owners are
[database_value/cursor.rs](../../src/database_value/cursor.rs),
[index/cursor.rs](../../src/index/cursor.rs) and
[pull/index.rs](../../src/pull/index.rs); the
[index](../06_indexes/01_index_model.atomic.md) and
[Pull](03_pull.atomic.md) traces identify the detailed independent checks.

Two [technical-note](../09_optional/03_technical_notes/04_outer_joins.md)
examples add no distinct engine: query finds entities before Pull selects
possibly missing attributes, while `get-else` supplies a default in the relation.
Source `process-pulls` and `extensions/get-else` map to query/results and function
dispatch; the query/Pull traces cover their separate binding/projection behavior.
[Byte-array querying](../09_optional/03_technical_notes/03_querying_byte_array_attributes.md)
instead records a JVM identity-equality trap and a Java Arrays workaround.
Native `Value::Bytes` owns bytes with content equality and query bindings use
that value model; the host-object identity failure is deliberately not a native
requirement. This is a type-system adaptation, not a claim of JVM-array or Java
static-call compatibility.
