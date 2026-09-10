# Product acceptance and operating envelope

All seven stages and integrated acceptance are complete as of September9,2026.
[Goal0](../goal-0/0-plan.md) owns the required capability set and completion
evidence. The results below describe the delivered native product and its tested
operating envelope, not the historical G3 finish line.

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

Source details and scoped measurements are retained in
[Goal2](../goal-2/0-plan.md), [Goal3](../goal-3/0-plan.md),
[Goal4](../goal-4/0-plan.md), [Goal5](../goal-5/0-plan.md),
[Goal6](../goal-6/0-plan.md) and [Goal7](../goal-7/0-plan.md).

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
  its build wrote1543191cumulative spill bytes. Full source projection rebuilding
  is not an incremental-build or arbitrary-scale claim.
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

The final supported binary/example build and all-target compile passed.
The broad library run passed341tests with1ignored; PostgreSQL was deliberately
unset there, so its early-return tests are not counted as live PostgreSQL proof.
The configured suites above and the child plans supply that proof. Thirty focused
maintenance tests and ten commitment tests also passed against actual PostgreSQL.
