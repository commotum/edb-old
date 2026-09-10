# Product acceptance and operating envelope

The September10,2026 repair pass completed all twelve review repairs and integrated
acceptance. Results below distinguish complete test coverage from individual run
exit status. The September9 section remains historical evidence.

## September10 repair verification

Permanent regressions now cover declarative negation/disjunction and anonymous
identity, stack-safe component traversal/recent-log ownership, exact bounded
arbitrary-precision arithmetic/comparison, delta-sized transaction bookkeeping,
bounded Pull/indexed query ranges, and incremental fulltext maintenance.

The fulltext primary optimized PostgreSQL matrix passed at64/256/1024documents:
nontext changes process0search records and upload0pages; fixed text edits admit5
records and tokenize50bytes. With64-datom canonical leaves, complete foreground
transact/consolidate/reopen/search/check/drop samples were219–230ms nontext and
264–276ms text. Routing metadata grows; default4096-datom leaves decode larger
changed leaves. These are scoped single-host samples, not constant-time guarantees
or universal transaction throughput. The [fulltext guide](fulltext.md) explains
the separate counters, empty-corpus bulk path and retention behavior.

The final runtime broad run finished all140targets:973passed,4failed,6ignored and
7filtered. The four failures were resolved and the entire affected targets rerun:
library408/408, backup/restore10/10 and service-worker9/9 pass. Thus all977normally
executed cases have passing coverage across the broad run and clean target reruns;
the broad command itself was not a zero-exit run. The manifest/backup fixtures now
isolate intentional corruption from unrelated catalog-wide migration repair. The
unchanged service target passed on the separate server after concurrent suites
exhausted the main fixture's80connections. Final library harness concurrency was4;
tests retain their own internal concurrency. Intermediate failures and interrupted
polluted-catalog runs are not counted as passes.

Real migration/runtime/search checks pass, including genuine old-binary
schema26→30 upgrades preserving original4818-/4588-byte genesis values and exact
retries, plus TLS1.3 on both PostgreSQL listener connections. Dedicated current
application/transactor processes pass planning/partitions/fulltext and exact
request replay after an actual immediate PostgreSQL crash. Basis11→13, full
current/history fingerprints, retained/as-of values and independent reopening
agree; the pre-crash basis2 reference remains readable. Restart1015ms/replacement
2061ms, one tail transaction/one range and zero eager materializations are scoped
samples, not an availability or throughput guarantee. The genuine pre-repair
direct-map and stored-program receipts still verify without changing their file
SHA256 `e625cb49f32df376c9cf1e5135582347ad502de21dd7ff713f52f210dd123c32`.

Final saved lifecycle24actions and differential48steps replay successfully,
byte-identical to their original traces:8writes/3interrupted uploads/3exact retries/
4consumer resumes, and36accepted/12rejected outcomes respectively. Controlled
9→3failure reduction was separately exercised as an explicitly injected fixture.
The seven filtered old-data/crash/generated cases use their dedicated real-fixture
runs, not missing-environment early returns. Of six ignored cases, the subprocess
worker is executed by its parent and old-receipt verification ran explicitly;
four optional comparative measurement campaigns remain unclaimed for this run.
The Unix protocol witness ran explicitly:66nodes,132/2/3driver calls and396/6/9
completed Sync/Ready cycles for per-node/batched/compressed upload—not packets or RTT.

Integration also repaired two operations defects: program GC now compares exact
membership without changing its oldest-first512-program batch policy; a permanent
513-aged-program case proves both batches and young-program retention. Concurrent
administrative indexers finish the authenticated winner's pending live-set fold
instead of rebuilding an unchanged basis. A deterministic964-datom/340-node wasted
rebuild is eliminated; complete connect/consolidate/drop still costs115.7ms and
87SQLcalls in that fixture. Corrupt-root reconstruction remains tested.

Builds used Rust1.90, PostgreSQL15.11 with durability enabled, release profile,
incremental compilation disabled and-j4 on the shared development host. Formatting
and all-target Clippy pass;24pre-existing warnings remain, with no new warning from
the repairs. This is not a warning-free or deployment certification.

## September9 historical acceptance

## What has been exercised

All results below used actual PostgreSQL15.11 and Rust1.90 on the development
host. Test fixtures, crash targets and network namespaces were disposable. These
are reproducible checks and workload-specific measurements, not a deployment
certification, an uptime SLA or Datomic wire/storage parity.

| Boundary | Evidence |
| --- | --- |
| Supported application | Local CLI and separate application preserve fixed schema/data requests, history, safe preview/replanning, partitions/UUIDs, fulltext and exact receipts through restart. Two-process regression2/2 passed8.97s. |
| Remote application | Two isolated network namespaces connected by veth, verified TLS, restricted roles, invalid-token rejection, writer SIGKILL/replacement/rediscovery, exact retry and cross-process snapshot reference/excision policy. Passed15.43s. |
| Remote programs and hints | Stored query/transaction program preview agrees with commit; retry after replacement and program rebinding keeps the original receipt. Altered/stale/foreign/absent hints do not change request identity. Transport tests3/3 passed13.35s. |
| Observation | Consumer replay/checkpoint/RLS/generation/reconnect tests6/6 passed13.26s;512-notice flood coalesced504, maximum batch64. Whole reader process0SQL over500ms idle; separate-writer observation77.117ms consumer/77.121ms peer in the final debug sample. |
| PostgreSQL TLS | Both actual asynchronous LISTEN backends verified TLS1.3 through pg_stat_ssl; missing trust and plaintext rejected. Consumer checkpoint/peer advancement pass;120ms SQL timeout observed121.03ms. |
| Administration | Real backup reuse, offline verification, guarded separate-target restore, SIGTERM during an observed restore node-write wait followed by identical retry, inspection, authorized GC and diagnosed missing-search-root repair.2/2 passed14.84s. |
| Crash recovery | Separate PostgreSQL immediate shutdown/WAL recovery with fsync/synchronous_commit/full_page_writes on. Current product at basis11, acknowledged12, successor13; full current/history fingerprints, retained values/log, exact retry and independent reopen agree. |
| Old data | Preserved pre-partition/pre-fulltext binaries created actual4588/4818-byte genesis databases. Explicit schema26→29 migration and vocabulary transactions retain genesis bytes/hash, old values and retry identity. |
| Generated lifecycle | Final24-action V2 saved trace replays after both repairs:8writes,3interrupted uploads,3writer restarts,3exact retries and4consumer resumes. A48-step durable-versus-pure comparison preserves36accepted/12rejected outcomes across restart. Controlled failure reduces9actions→3 and passes without the injected assertion. V1 action meanings remain readable. |

Usage details and scoped measurements are in the [application guide](application.md),
[query guide](queries.md), [fulltext guide](fulltext.md),
[consumer guide](change-consumers.md), [operator guide](operations.md) and
[independent-reader measurements](read-load.md). Original implementation records
remain available in Git history at commit `d1670aeabffa92ad54fd200e86aee675351eb6e3`.

## Cost evidence to interpret separately

- The [independent-reader campaign](read-load.md) measures real Datalog work in
  1/2/4processes, cold openings, concurrent scans and paced writes. At2048records
  with a1MiBdecoded-node allowance, warm throughput was79.6k/104.5k/155.2kqueries/s
  with0foregroundSQL. Doubling data with the same cache caused thrashing and
  reduced it to122/237/497queries/s. Smaller-cache admission failures and all
  foreground/background SQL, CPU, RSS and latency tails are reported. This is a
  short shared-host sample, not cross-host linear scaling or a production ceiling.
- Authenticated bounded subtree fetching reduced commitment encoding from1109
  to243SQL calls for four256-operation transactions. Sequential/queued wall time
  changed258.27→238.80ms /225.57→194.39ms while returned result-cell bytes grew
  by44944. Smaller transactions did not consistently improve; the measured
  ordered-transactor decision remains, with no added pipeline complexity.
- Shared authenticated replay reduced inspection of the same8192-record,
  nine-publication source from10.591s to6.784s. Restore SQL fell152963→39198,
  but elapsed19.315→19.247s was essentially unchanged and returned bytes rose.
  Single-publication target inspection also did not improve. See the
  [maintenance measurements](operations.md#goal7-maintenance-measurement--2026-09-09) for exact fixtures, CPU/memory,
  verification and reproduction. Large restore remains a substantial broad
  replay/validation operation; fewer driver calls are not a speed guarantee.
- Resident native reads avoid foreground SQL; new peers and cold misses still
  authorize, authenticate and maintain retention through PostgreSQL. A serialized
  snapshot reference is neither a credential nor a retention pin.
- Four thousand ninety-six speculative extensions had height14 and a selective
  read visited24 overlay nodes. This is structural sharing, not4096 whole-database
  copies. The16k-row numeric hash-join fixture took109ms versus125s for its
  reference nested-loop evaluator; this is not universal query throughput.
- Transaction hints are implemented, bounded and advisory. The measured small
  cold/warm fixture gained no speed and added16SQL calls; use evidence from the
  actual workload before enabling them as an optimization.
- Compression is a versioned optional physical projection. Canonical hashes and
  rows remain authoritative, so transfer/cache savings can add PostgreSQL storage
  and codec CPU. The batch-upload witness measured132→2driver calls (3with
  compression) for66nodes; driver calls are not TCP round trips.
- Fulltext is eventual and supplied-view validated, not a basis-stable ranking
  service. A402-document native fixture read10167search bytes cold and0SQL warm;
  its historical build wrote1543191cumulative spill bytes. That September9
  full-projection build is superseded by the incremental maintenance above;
  neither sample establishes arbitrary-scale search throughput.
- The earlier100k-record G3 import/restore/inspection values remain
  [historical baselines](operations.md#historical-g3-integrated-acceptance--2026-09-09),
  not measurements of the newer reader/cache/maintenance paths.

## Repeat the relevant checks

Use a dedicated disposable PostgreSQL database with an administrative test login,
never production. Runtime-role tests require permission to create restricted
test roles. Set `ATOMIC_POSTGRES_URL` and an explicit
`ATOMIC_POSTGRES_TRANSPORT=plaintext` for a private local fixture, or use the
configured TLS APIs. Build the executable/example before process tests, which
intentionally invoke those prebuilt files:

```sh
cargo build --offline --bin atomic --example application_workflow
cargo test --offline --test product_cli --test remote_transport --test remote_product --test change_consumer --test admin_cli --test postgres_runtime_roles -- --nocapture --test-threads=1
cargo test --offline --test storage_fault_replay -- --nocapture --test-threads=1
ATOMIC_DIFFERENTIAL_SEED=42 ATOMIC_DIFFERENTIAL_STEPS=48 cargo test --offline --test transactor_differential generated_ -- --nocapture --test-threads=1
```

The Linux remote-product test additionally requires `unshare`, `nsenter`, `ip`,
OpenSSL and permission to create isolated user/network namespaces. It does not
modify the host network. Admin tests create separate physical PostgreSQL
databases for destructive/recovery checks. Missing required privileges are not
evidence of a passing acceptance run.

For another generated lifecycle schedule, set `ATOMIC_STORAGE_FAULT_SEED=42` and
`ATOMIC_STORAGE_FAULT_STEPS=24`. The test prints a private saved trace path.
Re-run it with `ATOMIC_STORAGE_FAULT_REPLAY=/absolute/saved.trace`, omitting seed/
step overrides. Failure reduction retains a second trace and a stable failure
signature. The controlled reducer witness is explicitly injected, not a claim
that it discovered a production defect.

PostgreSQL TLS and server-crash tests require their separate configured fixtures;
see [transport policy](operations.md#transport-and-failure-policy), comments in
`tests/postgres_tls.rs`, and the guarded `examples/restart_workflow.rs` driver.
Do not reuse the shared application server for an availability-destroying test.
A default green test run with missing PostgreSQL/TLS/crash configuration is not
evidence that those paths executed.

The September9 supported binary/example build and all-target compile passed.
That broad library run passed341tests with1ignored; PostgreSQL was deliberately
unset there, so its early-return tests are not counted as live PostgreSQL proof.
The configured suites above and the historical implementation records supply that proof. Thirty focused
maintenance tests and ten commitment tests also passed against actual PostgreSQL.
