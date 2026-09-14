# ATOMIC-NOTE: optional client contracts versus native composition

This companion covers the six Pro Client pages: library acquisition, Peer
Server, getting started, client reference, sync API and async API. AWS/IAM/Ions,
classpath installation, HTTP Peer Server endpoints and Cloud query groups are
non-target integrations, not missing native query engines. Recovered source
contains pieces of these integrations, not an evidence basis for whole-product
or cross-version client compatibility.

The [reference](03_client_library_reference.md) “Synchronous API,” “Asynchronous
API,” “Handling Errors,” “Timeouts” and “Chunked Results” distinguish blocking
values from channel results, failure values and transport chunks. Native
[application/asynchronous](../../../src/application/asynchronous/mod.rs)
adapts local captured values through a bounded
[runtime executor](../../../src/runtime/executor/mod.rs); it does not make
query/Pull remote or implement core.async channels. Source peer completion and
error mechanisms are traced at exact symbols in the
[API companion](../../07_peer_api/00_clojure/00_datomic_api.atomic.md).

Native streams are demand-driven and fuse on failure. Query result chunking
does not turn eager relational execution into lazy joins; deferred Pull is
separate. Deadlines include queued wait, callbacks execute on workers, and
admitted transactions remain owned if a waiter is dropped. Independent checks:
`tests/async_client.rs::database_free_callbacks_do_not_block_and_queued_deadlines_count_waiting`,
`query_streams_demand_drive_pull_fuse_errors_and_wait_for_worker_admission`,
`streams_resume_after_full_admission_and_honor_cancellation`, and the real
PostgreSQL dropped-transaction retry/restart case in the same file.

Sync/async API entries for `as-of`, `since`, `history`, `datoms`, `index-range`,
`(r)seek-datoms`, `pull`, `q/qseq`, `with` and `tx-range` share the corresponding
native database_value/query/Pull/index/log owners; the source Peer symbols and
focused checks are enumerated in the API companion. Catalog, synchronization
and transaction entries belong to application/operations/writer composition.
Client-specific `with-db`, offset/limit defaults, chunk sizes and retry/backoff
defaults are not silently imported into the richer native Peer-style APIs.

“Connection” and “busy” describe source client caching/retries. Native
connections keep stable database identity and explicit ownership; native
Busy admission and durable request receipts define whether retry is safe. There
is no claim that a Datomic busy error and an Atomic Busy error prove the same
internal retry history. Peer Server health-check and authentication examples
are not the native transaction endpoint protocol or security configuration.
