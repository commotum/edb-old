# Atomic implementation trace — authority and recovery

Development companion to [High Availability](01_high_availability.md), not
recovered documentation. This selected lifecycle slice does not claim identical
deployment topology, timing, wire protocol or whole-namespace coverage.
Source coordinates refer to baseline
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; use named symbols after annotations.

## Active ownership is not merely failure detection

Passages: “Enabling HA” and “Peer Recovery Time.”
[lifecycle_ext.clj](../../../1.0.7705/transactor/src-clj/datomic/lifecycle_ext.clj)::`standby-loop`
(84) observes two consecutive unchanged published heartbeat timestamps before
calling [lifecycle.clj](../../../1.0.7705/transactor/src-clj/datomic/lifecycle.clj)::`pump`
(38). `pump` conditionally advances the process endpoint reference; only success
starts serving. `master-loop` (77) renews and fails the process after ownership
loss. The detector compares timestamp observations, not two machines' wall-clock
values. Timing can trigger an attempt; the revision CAS decides its success.

There is a second fence: [update.clj](../../../1.0.7705/transactor/src-clj/datomic/update.clj)::
`internal-start-database` (2745) calls `log/claim` for each database before
recovery. [log.clj](../../../1.0.7705/transactor/src-clj/datomic/log.clj)::`claim-log`
(577) advances that database's tail descriptor revision without appending data.
A process-level active endpoint is not alone permission to append every log.

**Rust disposition:** [transactor/authority/mod.rs](../../../src/transactor/authority/mod.rs)::
`BlockTransactor::claim`, `renew`, `refresh_owned_lease` and `release` own one
database's epoch/token/expiry lease. Claim atomically publishes the advanced
writer epoch and lease under captured root/lease/GC conditions through
`ownership::publish_refs`. Losing a lease token/revision fences subsequent work;
an uncertain claim acknowledgment is resolved only by the exact proposed token.
Expiry admission uses wall-clock time, unlike the source observation counter.
Do not advertise the source's two-heartbeat/11-second recovery bound for Rust.
Keep lease authority together; endpoint discovery and standby process scheduling
are consumers, not another implementation of canonical write authority.

The Stage 3 ownership split puts this canonical writer and its CAS witnesses in
`transactor/authority/{mod,tests}.rs`. The public writer types are exported from
the root facade, not the storage-provider namespace. Shared
[catalog/database.rs](../../../src/storage/catalog/database.rs) owns fixed database
identity, name mapping and finite genesis creation; none of those starts a writer.
[storage/protection.rs](../../../src/storage/protection.rs) owns collection-epoch
guard assembly, used directly by catalog, maintenance, backup and program
deployment as well as the writer. Those callers do not import writer scheduling
merely to protect immutable objects. Remote endpoint and restore lease checks
remain authority read helpers and do not start a runtime. Object formats, guard
conditions, exact receipt handling and publication order are unchanged by the move.

## Catch-up, explicit repair and read availability

Passages: “Reads Remain Available,” “Transaction Latency After HA Recovery,” and
“Application-Level Retry.” Source `internal-start-database` starts its reader and
delivers the peer-start promise before `log/catchup` (1444) rebuilds memory state
from the durable index plus a log iterator. This explains connected peers waiting
behind recovery; it is not an eager replay of all history on every takeover.

Rust [transactor/service.rs](../../../src/transactor/service.rs)::`start` calls
`BlockTransactor::activate` before returning the active service. Activation uses
`BlockReader::capture_root` to reconstruct the selected index's recent tail.
Existing immutable peer values do not acquire writer authority to read. This
preserves separation of reads from takeover but is a different startup sequence.
Exact request receipts, resolved before fresh admission, are an additional native
retry contract; the source chapter explicitly leaves application retry policy to
the application.

Keep ordinary activation separate from
[index_recovery.rs](../../../src/storage/index_recovery.rs)::`prepare_recovery`:
the latter is an explicit administrative full-log rebuild of the current derived
index, preserves receipt/read-authorization roots, and does not repair damaged
historical retained indexes. Source `index/repair-disjoined` (4929) is instead a
build-revision-gated compatibility repair, not a mechanism to reproduce in a
first-release format. The demonstrated per-entry reseek in native recovery has
now been replaced by the existing retained log range cursor. Deep skip-lineage
validation remains a separate full pass; replay still applies every transaction
and preserves its allocation checks. This changes navigation, not recovery scope.
The new actual-path test
`actual_recovery_reads_each_sealed_page_once_for_validation_and_once_for_replay`
uses 130 transactions/three pages and observes page reads in the maintenance
reader: the captured tail once, each sealed page once per validation/replay pass.
The integrated library run passed this regression and measured
`RECOVERY_FORWARD basis=130 pages=3 pageReads=5 totalSqlCalls=412`: tail once,
each sealed page twice. Driver calls include rebuilding, not just replay; this
is a navigation/read-shape result, not a latency improvement or cheap-repair
claim. `backup_verify.rs` has a separate full-replay
consumer to evaluate in the backup-owning component; it was not changed here.

## Boundaries and evidence

Keep worker candidate preparation separate from current-source validation and
guarded adoption in [index_publication.rs](../../../src/storage/index_publication.rs).
The shared publisher checks generation, source-index lineage and the canonical
log endpoint, preserves newer log/receipts, and allows only narrowly proven
operator lease-renewal retries. Renaming files must not erase these checks.
Source's frozen-novelty handoff and log-excision rebase are traced separately in
the [excision companion](../../09_optional/02_specialized_operations/02_excision.atomic.md#index-publication-log-rewriting-and-adoption-are-distinct);
they explain how source workers retain later arrivals, not a claim that native
excision already supports the same concurrent fresh-write schedule.

Relevant permanent checks include [service_standby.rs](../../../tests/service_standby.rs),
[authority/tests.rs](../../../src/transactor/authority/tests.rs) and
[index_recovery_tests.rs](../../../src/storage/index_recovery_tests.rs).
The coordinated cutover run passed all five service_standby tests and the
authority/recovery library cases with PostgreSQL enabled, including stale
writer fencing, exact replay, damaged-index repair and cancellation. These are
current runtime checks, not merely inspected tests. Cross-region storage copying, automatic
process supervision, all clock-skew schedules and arbitrary canonical corruption
remain outside this comparison.
