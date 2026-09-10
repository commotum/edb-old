# Goal 3 — Functional Values and Useful Query Programming

## Objective and constraints

Complete Stage3 of `/home/jake/Developer/atomic/goal-0/0-plan.md`. Applications
must efficiently branch/query database values, identify and reopen exact supported
snapshots, and safely plan changes using native Rust APIs. The parent owns every
capability, invariant and integrated acceptance; this child is now complete.
Use local datomic_pro_docs as semantic authority and 1.0.7705 as algorithmic
evidence. Preserve working semantics, existing durable forms/checksums/retries,
program ownership, retention/authentication, old values and Goal1–2 repairs.

The engine already has pure/native with, temporal/history views, recursive query
rules, programs and authenticated commits. Extend these; do not replace the query
language, engine or test infrastructure. PostgreSQL remains sole durable authority.
Speculation never reserves a commit, and physical roots/cache identity are not
logical value identity. Opaque closures cannot gain invented portable equality.
Broad scans/output may scale with data; measure selective work and bounded state.
No recursive children, alternative stores, JVM parity or silent scope exclusions.

## Stages

### 1. Shared indexed values and exact snapshot identity

**Status:** Complete.

**Outcome:** Cheap independent speculative branches and stable supported logical
snapshot/view keys with versioned exact references and authorized local reopening.

**Focus:** Replace accumulated overlay-vector copying and selective full scans with
shared current/history/removal ownership and indexes. Preserve schema/ident/program
semantics, current/history/temporal views and safe iterator/drop depth. Build keys
from lineage, generation and commit identity plus logical view modifiers, not
physical indexing. Explicitly reject unsupported or unavailable references.

**Completion signal:** Pure/native/generated branches agree without writes;
depth/width and growing-prefix measurements establish sharing, indexed selective
reads, retained/discarded memory and stack safety. Keys/reopening agree across
indexing/restart without scans; distinct supported views do not collide and
authorization/retention failures never silently open another value.

### 2. Composable query sources, reuse and durable programs

**Status:** Complete.

**Outcome:** Useful native authoring and efficient reusable query evaluation over
database, tuple and log data, also available to persisted programs.

**Focus:** Versioned QueryTemplate expansion for predicates, rules, negation,
historical sources and dynamic attributes, retaining old durable meanings. Add
raw-datom/tuple database-pattern sources and tx_ids/tx_data log integration.
Implement bounded structural prepared-query caching, hash joins/grouped probes,
keeping selective seeks and per-run source/schema/binding validation.

**Completion signal:** All five persisted capabilities agree in speculation,
commit, old program versions, retry and recovery. Identical queries run on native
and tuple fixtures; log ranges join provenance. Reuse does not retain stale inputs;
growing joins preserve equality, duplicate/aggregate semantics and cancellation
with measured work/memory/I/O and preserved selective performance.

### 3. Advisory hints and safe application planning

**Status:** Complete.

**Outcome:** Speculation can supply bounded authenticated advisory prefetch hints,
and the common application safely selects and commits logical intent.

**Focus:** Read tracing, bounded generation/admission, overlapping prefetch and
cancellation after read interfaces settle. Hints never change durable request
identity or transaction meaning; Stage6 owns transport. Reuse existing reports
and intent forms for branch/compare/discard/select/revalidate/commit, avoiding
blind submission of speculative allocated IDs or datom diffs.

**Completion signal:** Valid/stale/altered/absent hints preserve results; cold/warm
measurements include peer overhead, queue/service latency, I/O and cache effects,
including no-benefit cases. Discarded branches leave live state unchanged, selected
intent resolves identities, stale protected assumptions reject/replan, and reports
expose the exact committed value.

### 4. Integrated functional acceptance and parent handoff

**Status:** Complete (2026-09-09). Return to parent Stage4/Goal4.

**Outcome:** The complete Stage3 workflow works on real PostgreSQL and in-memory
fixtures, with reproducible semantic/cost evidence and existing repairs preserved.

**Focus:** Extend the evolving application and existing native speculation,
differential and fault support; test changed boundaries and old durable artifacts.
Record material decisions, failures and measured limits in this plan and Goal0.

**Completion signal:** Every preceding signal holds together on actual PostgreSQL;
no self-skipped test counts as evidence. Return to Goal0 and execute its next
unfinished child. Neither this scaffold nor Goal3 completion finishes the product.

## Verified implementation and material decisions

- Shared speculative overlays now use persistent path-copied AVL indexes for
  current/history/removals/idents with shared datom ownership, rooted directly at
  the committed base rather than recursively nesting overlays. Existing native
  schema/program ownership and AVET semantics remain. Focused pure/native suites
  passed32 tests with real PostgreSQL configured. Generated64-step branches,
  64siblings and retained ancestors agree with the existing semantic oracle.
- The growing-depth witness covers128/512/2048/4096 extensions. At4096,
  8192history datoms had tree height14; selective AVET visited24nodes, not the
  accumulated prefix.128siblings shared over99% of parent nodes; discarded
  payload weak references expired and deep destruction passed on a256KiB stack.
  Memory accounting is inline tree/datom storage, excluding allocator/Arc headers,
  not process RSS. Broad scans and changed-attribute backfill still scale.
- `SnapshotKey` uses lineage/generation/commit/view coordinates, never physical
  roots. Versioned `SnapshotReference` additionally carries the required manifest
  as a retention witness, not logical equality. Actual `noHistory` consolidation
  can change retained historical exposure at one logical commit: exact reopening
  must retain that witness or fail, not silently substitute the latest tree.
  References are neither credentials nor pins; new pre-excision reopening rejects.
  Speculative/eager/opaque-filter values explicitly lack portable committed keys.
  Native reference suite passed3/3(6.20s), including same-key reindex, exact schema
  basis, forged references and excision policy.10,000key/clone operations took
  7,742us debug with0SQL. Actual isolated GC retirement test passed(2.49s): a held
  snapshot remains readable; its serialized reference fails after witness removal.
- `QueryTemplate::native` selects ABI7 only for V2-containing programs; independent
  V1 bytes/hash and ABI4/5/6 meanings are preserved. Combined predicates, recursive
  rules, negation, temporal sources and dynamic attributes pass speculation,
  commit, retained bindings, identical retries and restart. Native suite7/7 and
  extension budgets5/5 passed on actual PostgreSQL(2.51s/4.45s); existing runtime
  22/22 and exact-source5/5 pass. Real log sources are authenticated captured logs,
  not history substitutes; speculative values cannot pretend to supply one.
  Serializable query ASTs reuse existing native budgets. Rust callbacks/random
  aggregates and non-scalar VM map keys have explicit documented limits rather
  than changing old durable runtime forms. See `docs/programs.md`.
- The executable application now compares/discards pure branches, exercises an
  intervening allocation, rejects a stale basis, replans logical intent and commits
  a different ID from its first preview. A normal restoration transaction retains
  all history. Fixed new request keys preserve the original v1 baseline requests.
  Actual restricted-role/separate-process/restart acceptance passed2/2(7.66s).
  Baseline still observes exact basis3; planning finishes at6. First/replay runs:
  baseline820/770ms, warm calculations0SQL; planning253/283ms,90/88foregroundSQL,
  8generated hints. Hints are not yet socket-transported(Stage6). Rebuild both
  binaries before testing: an initially stale example correctly lacked PLANNING_OK.
- Hint review found scoped worker joins could delay a committed response under
  stalled PostgreSQL I/O. Replaced that design with detached, per-writer and
  process-bounded admission, independent connection/pin/miss lanes, shared only
  authenticated bounded RAM cache and cancellation after authority finishes.
  Final adversarial blocking and cold/warm cost checks pass (below). Driver
  timeouts are not a promise to interrupt DNS/startup or arbitrary synchronous I/O.
- A second isolation review removed original writer pin ownership from detached
  workers: an unpinned immutable read plan is captured before authority proceeds,
  and only the worker's new independent pin lane performs its setup/destruction.
- Combined native/application rerun passed10/10: native speculation3(8.21s),
  application2(7.53s), references3(7.36s), generated branches2(23.66s). Both real
  application processes used restricted roles and restart/recovery; warm queries
  remained0SQL and planning confirmed remapped IDs/exact reports/references.
  Latest key construction/clone was6,744us/10,000,0SQL. A broad library check had
  302passes/13sandbox failures/1ignored; PostgreSQL-unset early returns are not PG
  evidence. The13failures were private-path/socket sandbox restrictions. Rerunning
  the owning SSD and local-transport groups as the actual user passed9/9 and8/8;
  ownership protections were not weakened. The required PG paths are separately
  exercised by the configured integration checks recorded above.
- Query sources/preparation/joins pass58 existing regressions and10 new tests
  with PostgreSQL configured. Bounded cache keys preserve numeric representations,
  decimal scale and float bits; opaque callbacks/deep/oversized structures bypass
  automatic caching. Bindings/schema remain per-run. Actual log/provenance joins
  retain original noHistory events after consolidation. Cold native probe4SQL;
  warm10/100/1000-row grouped probes each1seek/1datom/0foregroundSQL.
  Six fresh-process release numeric equijoins,1k/4k/16krows: hash wall11.2/31.2/
  109ms versus reference468ms/7.94s/125s; candidates2n versus n+n²,0SQL. PeakRSS
  KiB11388/19980/56408 versus10376/18536/53756; estimated auxiliary table128k/
  512k/2048kbytes. This is one local workload, not general query throughput.
  Reproduce the opt-in `measured_query_runtime_scaling` test using
  ATOMIC_QUERY_BENCH_ROWS and ATOMIC_QUERY_BENCH_MODE=hash|reference; see docs/queries.md.
- Final release hint suite passed4/4, plus the strengthened blocked-worker test
  rerun and2pure admission/scope tests. Two acknowledgements completed22.56ms
  while independent hint setup remained blocked; the second skipped its busy
  slot, joins remained0 and cancellation prevented subsequent datom reads.
  One-byte budget delivered1datom/96bytes, exposed the one-item overshoot, then
  stopped. Dead/stalled calls keep permits across service replacement:1/writer,
  8/process. Unpinned capture prevents original-writer pin release contention.
- Meaningful release cold/warm hint sample(8-datom leaves,128items/32updates):
  absent→hinted cold preview4.154→4.690ms, peerSQL8→8, ack29.640→32.065ms,
  totalSQL175→191, writer leaf reads4→4. Warm preview1.493→1.899ms, peerSQL0→0,
  ack19.717→29.103ms, SQL171→187, leaf reads0→0. Each hinted run added2connections/
  16SQL, retained98prefixes/10976bytes, delivered64datoms/6144bytes and overlapped
  16.926/13.155ms; no joins. No benefit demonstrated: setup lost the cold-read
  race. n=1/mode/condition, PostgreSQL buffers not flushed, not an SLA. Earlier
  larger-leaf fixture was already warm and is not cold evidence. Reproduce with
  `cargo test --release --offline --test transaction_hints -- --include-ignored
  --test-threads=1 --nocapture` and configured disposable PostgreSQL.
- Final `cargo check --offline --all-targets` passes. Goals1–2 repairs remain
  exercised; formatting/diff checks are clean. No Goal3 capability is deferred
  except its explicitly parent-owned Stage6 transport and Stage7 broader load.

## Continuation

Goal3 complete, 2026-09-09. Return to Goal0 and execute Goal4(partitions/UUID).
No later child or parent completion is claimed. Reopen Goal3 for integrated gaps.
PostgreSQL fixture is the isolated
socket-only15.11 cluster `/tmp/atomic-product-pg.jJPI2p` on55439; global GC uses
its separate `atomic_goal2_gc_36829` database, not concurrent application fixtures.
