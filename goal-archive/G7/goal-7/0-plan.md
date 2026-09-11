# Goal 7 — One current storage engine and integrated acceptance

## Objective

Complete Stage 7 of Goal 0: ship the existing native Rust/PostgreSQL product on
the immutable-object/conditional-reference engine alone. Remove the displaced
relational engine and its maintenance obligations, preserve useful capabilities
and regressions, and establish the parent's integrated acceptance. This is the
last child, not a new parent or permission to stop after cleanup.

## Constraints and context

- Follow Goal 0's first-release, semantic and architectural boundaries. No old
  format readers, converters, historical executables or permanent dual paths.
- Local Datomic docs remain semantic authority; `1.0.7705` supplies evidence for
  small storage primitives, immutable indexes/logs and engine-owned coordination.
  Keep these corpora and unrelated user work/data. PostgreSQL remains the only
  production provider; filesystem backups are not another live engine.
- At stage entry, Goals 1–6 had passing block-path checks alongside residual
  `PostgresStore`, `PostgresIndexer`, `TieredSnapshot`, SQL fulltext adapters,
  migrations and old fixtures. Those displaced paths are now removed. Shared
  algorithms/types remain; proven Rust query/transaction logic and meaningful
  behavioral regressions were preserved rather than discarded to hide failures.
- Preserve EDN/typed and file/stdin APIs, local/remote/async applications, immutable
  values and time views, schema/identity/program/fulltext behavior, exact retries,
  current-version recovery/failover, backup/restore, lifecycle and maintenance.
- Real PostgreSQL checks must actually execute. Crash a separately created,
  disposable test cluster only; never reset or stop unrelated databases.
- Keep one child active. No grandchildren, corrective parent or open-ended audit
  campaign. Use measured complete paths, not historical performance matrices.

## Internal stages

### 1. Remove the displaced engine

Status: Complete — displaced engine removed; current binaries/examples build.

Outcome: Production modules expose one storage path and only generic storage SQL.

Focus: Retain neutral runtime types and shared log/tree/fulltext algorithms;
remove relational adapters, old fallback branches, migrations and dead dependency
edges. Keep public current workflows usable rather than preserving obsolete
SQL-specific implementation APIs.

Completion signal: Current library/binaries compile; source/SQL review finds no
legacy engine or database-policy routines/catalogs. Useful shared code remains
under accurate names and imports.

### 2. Current examples, regressions and operational guidance

Status: Complete — current examples, behavioral regressions and operational docs use the new engine.

Outcome: Stock applications, tests and documentation describe and exercise the
new first-release product, with superseded files removed.

Focus: Port real behavioral/failure cases and examples to current setup APIs;
remove old-format/SQL-shape/upgrade tests and misleading historical instructions.
Consolidate duplicates without weakening meaningful assertions. Account for the
parent capability inventory and actual installation/role boundaries.

Completion signal: All remaining targets compile; current application/operator
fixtures run against fresh object/ref schemas; no stock route or fixture needs
the old schema. Docs and commands match the implementation.

### 3. Integrated product acceptance

Status: Complete — real PostgreSQL/application acceptance and owning-stage repairs verified.

Outcome: The full product, cleanup and parent finish line are established.

Focus: Run current semantic, PostgreSQL and application checks, including real
WAL/crash recovery, failover/publication races, receipts and backup/GC interactions.
Reuse larger-than-cache fixtures to report complete latency, memory, calls/bytes
and write/maintenance amplification. Reopen owning stages for discovered gaps.

Completion signal: Current tests and integrated fresh installation pass, with
no skipped PostgreSQL checks counted as evidence. Final source/schema checks show
engine policy in Rust over opaque storage, no hidden legacy fallback, and no
silent capability reduction. Record measured limits and deletions, reconcile
Goal 0, and mark the parent complete only if its full acceptance holds.

## Continuation

Complete. The reopened Goal 4 observation boundary is verified; Goals 1–6 are
closed, this child's three completion signals hold, and Goal 0 has been reconciled
to complete. There is no active child or known deferred core gap. See
`docs/acceptance.md` for the current product evidence and reproducible prerequisites.

Cleanup removed the relational provider and SQL migration, old tree/fulltext/peer
adapters, receipt conversion and backup engines, compatibility branches and obsolete
SQL-shape/history fixtures. Shared algorithms live in neutral Rust modules; all
stock binaries/examples and preserved capability tests use the current path.
The dependency audit found no remaining dependency solely for the removed engine.
Fresh PostgreSQL inspection found exactly two opaque tables and zero policy
functions/user triggers. SQL outside the provider is limited to justified generic
notifications/administrative checks and tests, not relocated engine state machines.
Tracked deletions remain recoverable from Git history. Reference corpora and
unrelated databases were not changed; no old format reader or hidden fallback remains.

Material integration repairs are implemented and verified: canonical signed-zero
reports; decoded program reuse/protected deployment; cache accounting and batched
uploads; fresh-unclaimed and live lease-safe operator index publication; fulltext
and tree-size controls; lossless generation handoffs for connected peers; bounded
excision observation; and idle pin cleanup retry after a GC epoch race. Separate
peer interest tokens retain report continuity without an immortal chain in ordinary
database values. The final repair shares bounded initial-pin re-observation across
exact reports, serialized references, inspection, backup capture and receipt lookup.
Only transient capture conflicts retry; explicit captures remain strict, fixed-root
validation is unchanged, and this never reexecutes a transaction.

Verification records (logs are local run evidence, not another repository fixture):

- Broad `/tmp/atomic-goal7-all2.log` completed with three failed cases and an
  interrupted expensive snapshot target. Earlier broad runs also exposed stale
  fixture assumptions and real integration gaps. Neither failed/interrupted runs
  nor skipped PostgreSQL work count as acceptance; all affected paths subsequently
  passed after repairs. Five optional manual-cost campaigns remain opt-in.
- `/tmp/atomic-goal7-final-lib.log`: all 438 library tests passed in 324.76 s,
  including physical excision/program/search GC and deterministic cleanup races.
  `/tmp/atomic-goal7-observers3.log`: five protection tests passed in 4.45 s,
  including two new tests forcing concurrent publication across all five observer
  paths and proving the retry bound. This is not a full 440-test run.
- `/tmp/atomic-goal7-final-snapshot.log`: all ten tests passed in 198.61 s at
  the same 10,000-entity size; batching changes fixture setup, not asserted data.
- Sixteen affected targets in `/tmp/atomic-goal7-final-focus.log` passed. Its
  remote failure and the later inspection failure were addressed by the shared
  observer repair. Final `/tmp/atomic-goal7-observers-integration.log` passed all
  20 tests across nine targets: backup CLI/library/roles, inspection/integrity,
  isolated TLS application/stock automatic failover, routing/transport and exact
  references. Isolated TLS workflow: 17.852 s; stock cancellation 62 ms, takeover
  4.452 s, complete workflow 6.159 s. No local fallback substituted for that test.
- `/tmp/atomic-goal7-wal.log`: actual immediate shutdown, redo and restart passed
  (518 ms restart, 32.81 s fixture), including acknowledged exact receipts,
  uncommitted-reference rollback, held values/log, failover and successor write.
  Native and separate-process stock examples also passed. Backups were queried
  offline after source-schema removal; restore/exact retry and restricted roles
  passed on the current format.
- All-target strict Clippy, formatting and strict rustdoc passed. The doctest
  command found zero tests; executable examples/application checks supply that
  coverage. Final source/schema review found no old engine policy path.

Complete-path costs remain explicit. Standalone `block_live_costs` used 2,048
entities and 1,572,864 scalar bytes against a 1 MiB cache: 15.34 s process wall,
71,388 KiB peak Rust-process RSS (including setup/workers, excluding PostgreSQL),
14.950 s startup-through-shutdown, 9,782 driver calls, 20,949,013 bytes read and
8,952,849 written. Twenty-four writes took 3.338 s, or 6.681 s through four
automatic index jobs; 32 warm reads took 630 µs with zero SQL. The 10,000-entity
fixture's 13,640,218-byte index needed about 470 KB for selective native
open/query/speculation in 272–285 ms. Its 190.524 s eager setup is not an ingestion
benchmark. One hundred small writes took 12.569 s /31,538 calls; a small complete
fulltext/program restore took 2.984 s /9,297 calls. Fixed publication/retention
overhead is substantial, and no production-scale throughput or universal RSS bound
is claimed. Five exact reports across two excision generations survived three GC
cycles (8.315 s complete cleanup workflow).

Checks used our disposable PostgreSQL cluster `/tmp/atomic-cutover-pg.TvK29h`
(55479, `atomic_cutover`, `goal7_acceptance`) and dedicated TLS cluster
`/tmp/atomic-cutover-tls.nOaNWs` (55480), not the earlier shared development server.
Both disposable servers were stopped after verification with no clients remaining;
their data and logs were retained for inspection. The shared server was untouched.
No further implementation or test campaign is required by this parent. Future
work begins with a new user objective or a concrete reproduced regression.
