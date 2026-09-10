# Product acceptance and operating envelope

Goal7 is in progress. The delivered application/security/lifecycle results below
are established; final maintenance and independent-reader measurements are not
yet complete. [Goal0](../goal-0/0-plan.md) owns completion, not this document or
the historical G3 finish line.

## What has been exercised

All results below used actual PostgreSQL15.11 and Rust1.90 on the development
host. Test fixtures, crash targets and network namespaces were disposable. These
are reproducible checks and workload-specific measurements, not a deployment
certification, an uptime SLA or Datomic wire/storage parity.

| Boundary | Evidence |
| --- | --- |
| Supported application | Local CLI and separate application preserve fixed schema/data requests, history, safe preview/replanning, partitions/UUIDs, fulltext and exact receipts through restart. Two-process regression2/2 passed8.90s. |
| Remote application | Two isolated network namespaces connected by veth, verified TLS, restricted roles, invalid-token rejection, writer SIGKILL/replacement/rediscovery, exact retry and cross-process snapshot reference/excision policy. Passed15.24s. |
| Remote programs and hints | Stored query/transaction program preview agrees with commit; retry after replacement and program rebinding keeps the original receipt. Altered/stale/foreign/absent hints do not change request identity. Transport tests3/3 passed11.34s. |
| Observation | Consumer replay/checkpoint/RLS/generation/reconnect tests6/6 passed12.68s;512-notice flood coalesced504, maximum batch64. Whole reader process0SQL over500ms idle; separate-writer observation75.474ms consumer/75.479ms peer in the final debug sample. |
| PostgreSQL TLS | Both actual asynchronous LISTEN backends verified TLS1.3 through pg_stat_ssl; missing trust and plaintext rejected. Consumer checkpoint/peer advancement pass;120ms SQL timeout observed121.03ms. |
| Administration | Real backup reuse, offline verification, guarded separate-target restore, SIGTERM during an observed restore node-write wait followed by identical retry, inspection, authorized GC and diagnosed missing-search-root repair.2/2 passed12.84s. |
| Crash recovery | Separate PostgreSQL immediate shutdown/WAL recovery with fsync/synchronous_commit/full_page_writes on. Current product at basis11, acknowledged12, successor13; full current/history fingerprints, retained values/log, exact retry and independent reopen agree. |
| Old data | Preserved pre-partition/pre-fulltext binaries created actual4588/4818-byte genesis databases. Explicit schema26→29 migration and vocabulary transactions retain genesis bytes/hash, old values and retry identity. |
| Generated lifecycle | V2 stored traces combine transactions, interrupted index publication, exact retries, peer reopen, graceful writer replacement and bounded consumer restart/acknowledgment. Saved trace replays on a fresh fixture. Controlled failure reduces9actions→3 and passes without the injected assertion. V1 action meanings remain readable. |

Source details and scoped measurements are retained in
[Goal2](../goal-2/0-plan.md), [Goal3](../goal-3/0-plan.md),
[Goal4](../goal-4/0-plan.md), [Goal5](../goal-5/0-plan.md),
[Goal6](../goal-6/0-plan.md) and [Goal7](../goal-7/0-plan.md).

## Cost evidence to interpret separately

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
