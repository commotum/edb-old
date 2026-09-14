# Atomic trace — immutable log values and traversal

Development commentary; original [Log API](00_log_api.md) is unchanged apart
from its companion link. Source locators use unannotated revision
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; symbols remain stable after comments.

## LOG-VALUE — introductory paragraph and Log

The introductory promise is historical transaction order, organized for access
by transaction; `Log` returns an immutable value. Recovered
[log.clj](../../../1.0.7705/peer/src-clj/datomic/log.clj) `LogValue`
(1905), `LogTailValue` (1836) and the `MemLog` extension (1865) retain a
captured database, tree lookup/root and recent transactions. `LogValue` does
not fetch a new connection value when its iterator advances.

Native [database_value/log.rs](../../../src/database_value/log.rs) `LogValue`
owns a captured `BlockSnapshot`, including its log root and excision generation.
`Peer::log` and application `Connection::log` capture this same representation;
subsequent observations cannot extend it. Offline backup values use the same
log structures with repository object reads. A retained live handle is subject
to the documented GC grace, not an indefinite storage pin. `noHistory` index
consolidation is not permission to remove transaction-log facts.

Disposition: retain native immutable endpoint and content-addressed pages,
not the source's compatibility-constrained tail pod. See the
[log/publication comparison](../../04_transactions/05_acid.atomic.md).

## LOG-RANGE — bound list, transaction table and Java txRange

The `tx-range` argument list specifies inclusive start, exclusive end and
T/transaction-ID/instant/nil bounds. Its result table names `:t` and `:data`.
The [Java method](../01_java/08_log.md#method-details) repeats these rules.
Source `tx-range` (1876) applies `db/t-at-or-since`, clamps to captured
next-T, and wraps a seek in a less-than-end iterator. `LogValue.seek-tx-impl`
(1918) seeks root/directory/segment once; `LogTxIter.next` (952) advances those
positions. Its private mutable cursor does not mutate the database. Read-ahead
is a cache optimization, not authority to invent membership.

Native `LogValue::resolve_bound` uses the captured txInstant AVET index with
the same ceiling rule (including earliest duplicate), not as-of's predecessor
rule. The result is `LogTransaction { t, data }`; T=0 is native genesis,
not a submitted transaction. Public ranges remain lazy and fuse on errors.

Stage 4 found the shared storage range already retained forward anchors, but
the public cursor still called point lookup for every transaction. That repeated
authenticated page navigation unnecessarily. `storage::log::LogTraversal` now
holds that one traversal state independently of an I/O borrow; both borrowed
`LogRange` and owned `LogCursor` use it. Snapshot `next_log_record_measured`
supplies the same generation-scoped SSD cache and authenticated object reader
as point lookup. There is no second algorithm, per-cursor connection or unsafe
self-reference. Memory holds a codec-bounded transaction plus logarithmic
page anchors; retained caller results are additional. Complete traversal costs
include object reads, not merely cursor creation. No cold-cache throughput
or constant-memory claim follows from the structural bound.

The current regression owner is [native_log.rs](../../../tests/native_log.rs):
held endpoints, time bounds, noHistory, restricted-role reads, excision and
fusing after payload corruption. The new 130-transaction application case
compares point reads to forward scans of one captured endpoint with caches
disabled. [native_log_cache.rs](../../../tests/native_log_cache.rs) covers
restart-hot payloads, bounded cache, corrupted-cache fallback and generation
separation. Fresh Stage 4 PostgreSQL runs passed all five native-log and both
native-log-cache cases. The final isolated log run measured 257 driver calls /
284,489 result-cell bytes for repeated point reads and 132 calls / 20,348 bytes
for the forward scan: 130 entry reads plus two sealed pages fetched once.
Setup took 4.20 s; point reads 156 ms, forward scan 79 ms; startup, writes,
shutdown, both reads, result release and fixture cleanup totaled 4.45 s.
This is a debug, warm/uncontrolled PostgreSQL/OS sample, not a controlled
latency ratio. Cache restart checks also passed, with zero SQL for the warm
and restarted scans, plus corrupted-cache fallback and generation separation.

## LOG-QUERY — tx-ids and tx-data

Source [extensions.clj](../../../1.0.7705/peer/src-clj/datomic/extensions.clj)
`tx-ids` (101) maps the captured log range into partition-3 entity IDs;
`tx-data` (117) takes one transaction's datoms or an empty collection.
Native `LogValue::tx_ids` reuses the range; `tx_data` retains direct authenticated
point lookup. [query/sources.rs](../../../src/query/sources.rs)::`log_function`
adapts these into collection/relation bindings and charges materialized rows
and values to query controls. `edn_query` recognizes the corresponding names.
The [runtime-source regression](../../../tests/query_runtime_sources.rs)
`log_functions_join_provenance_and_keep_captured_basis_on_actual_postgres`
joins a held log to transaction metadata, observes retraction data missing from
noHistory indexes and excludes a later write. This is a specific adapter
contract, not whole query-engine verification; the
[query companion](../../05_query_and_pull/02_query_reference.atomic.md) now traces
that family. The docs warn against binding an already-bound tx position;
Atomic permits its ordinary unification (the regression does this), an explicit
native convenience; the prose warning alone does not prove a source failure
mode. Its actual PostgreSQL regression passed in the ten-case runtime-source
run, returning three historical rows and excluding the later write.
