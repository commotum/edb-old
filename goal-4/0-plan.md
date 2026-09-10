# Goal 4 — Delta-sized transaction bookkeeping

## Objective and constraints

Complete Goal0 Stage4, repairs R7/R8. Ordinary fixed-size transactions must not
rescan accumulated recent trees/logs, queued service work or unchanged schema
definitions for bookkeeping. Status: **complete** (2026-09-10).

Use `datomic_pro_docs` as semantic authority and `1.0.7705` as architectural
evidence. Preserve Goals1–3, immutable snapshots, stack-safe log release, canonical
bytes, transaction/identity/schema/tuple meaning, exact retries and lease fencing.
No alternate stores, JVM parity, new product features or arbitrary history/schema
caps. Counts must cover all work rather than merely a favorable insertion loop;
instrumentation itself must not scan the structure it claims is cheap.

Starting evidence: `recent.rs::recent_stats` recounts four trees and log chunks;
`service.rs` reads a published revision through backlog statistics; unchanged
schema Arc reuse is followed by schema/tuple-definition validation scans in
`tiered_assessor.rs`. Reconcile these with current code before making changes.

## Ordered work

### 1. Maintain immutable recent and service aggregates

- **Outcome:** Appends and revision reads do not walk unrelated retained data.
- **Focus:** R7 tree/log counts, immutable aggregate maintenance, backlog/revision
  access and complete bookkeeping instrumentation; preserve sharing and adoption.
- **Completion signal:** Independently recomputed totals agree after append,
  duplicate keys, rebuild/adoption, shared snapshots and failure. Fixed-size append
  work varies with touched paths, not entire tail/backlog. Relevant regressions pass.
- **Status:** Complete; independent recent/service oracles and local tests pass.
  Integrated acceptance remains in work3 below.

### 2. Reuse validated unchanged schema

- **Outcome:** Data-only transactions do not repeatedly validate or enumerate
  unchanged attributes and tuple definitions.
- **Focus:** R8 validated schema identity and dependency lookup; preserve changed
  schema validation, derived tuples, predicates and read/admission behavior.
- **Completion signal:** Wide-schema fixed-delta measurements include validation
  and dependency work; independent expectations and changed-schema/tuple/predicate
  regressions pass. No invalid schema bypass is introduced.
- **Status:** Complete and verified: assessor width/safety, complete-path
  metadata/equality/readiness, predicate dependencies and real PostgreSQL pass.

### 3. Prove complete native transaction paths

- **Outcome:** Both repairs work together across actual application/PostgreSQL
  writes, index adoption, recovery and exact retries with honest scoped costs.
- **Focus:** Reuse existing test/application support; vary tail, backlog and schema
  width at fixed transaction size, checking complete-path results and cleanup.
- **Completion signal:** Real PG runs, permanent regressions and affected earlier
  suites pass; stats match an independent oracle. Record configuration, counts,
  elapsed samples and remaining unavoidable work; formatting/all-target checks pass.
  Fold results into Goal0 and execute Stage5, not a new corrective parent.
- **Status:** Complete. Actual PG, permanent regressions, complete-path costs,
  adjacent lifecycle/index checks and formatting/Clippy pass as recorded below.

## Evidence and continuation

Disposable durable PG15.11 remains at127.0.0.1:55471, user/database `atomic_repair`,
cluster `/tmp/atomic-repair-pg.vA037i/data`, transport plaintext. Use approved host
execution for sockets and isolated test schemas. Do not overwrite Goal1's genuine
old-engine receipt fixture. Low-debug/nonincremental builds as in Goal0.

Verified implementation/evidence (2026-09-10):

- R7 immutable branch summaries cache retained node count/height; log chunks derive
  from maintained length without changing Goal2 ownership. New work counters include
  aggregate child reads, stats root reads and allocated log chunks. Independent
  full traversals verify append, retained snapshots, duplicates, rebuild/adoption,
  schema backfill and admission failure outside measured operations.31 actual local
  recent/tree/service tests pass; a PG-gated match returned early and is not counted.
  At32/128/512/2048/8192 retained txs, fixed append bookkeeping18/54/75/108/144,
  node visits6/6/9/12/12 and always5 stats root reads.16 complete append/stats/drop
  samples624/657/749/825/871µs unoptimized, not transactor throughput.
- Service revision/availability and full indexing stats now read maintained totals,
  not backlog entries. Exact private u128 totals preserve suffix accounting even
  after public u64 saturation; frozen totals distinguish novelty vs maintenance.
  Independent failure/retry/partial adoption/startup/saturation oracle passes;15
  service units actually execute (PG tests explicitly excluded).256 complete
  append/revision/failure/admission/stats/drop samples at32..32768 retained entries
  are about1.1–2µs/op; startup walks its recovered tail and adoption walks only the
  removed prefix. Neither necessary operation is mislabeled constant time.
- R8 counters now live inside real schema validators and are exposed in native
  writer diagnostics. Before, actual PG groups4/38 total attributes visited76
  transition descriptors on its first two-op write (2.61s test failure). Unit widths
  32/256/1024 plus composites visited74/410/1562 definitions,148/820/3124 transition
  descriptors and32/256/1024 constituents. After pointer-identical validated-schema
  reuse: all zero, dependency lookup/edge/candidate1/1/1.18 width/safety tests pass;
  complete assessment/drop samples0.431/0.500/0.660ms vs0.570/0.959/3.120ms before.
  Wide fixture setup0.333/2.438/19.052s is explicitly excluded, not hidden throughput.
- Complete-path audit found writer metadata statistics rescanned all schema/ident
  sizes each commit. Permanent witness reproduced1000 full recomputations for1000
  ordinary applies. Immutable cached totals now give zero at widths16/128/512;
  apply/stats/drop1000 samples134/134/139µs, with independent changed-schema totals.
  A structural successor-schema comparison also discarded Arc identity; now reuse
  is checked before the full comparison, which remains for distinct schemas.
  Ordinary enum idents keep schema descriptors shared; real identity-map edits
  still reconstruct changed identity metadata and are not claimed constant time.

Further integration:

- Readiness now shares an Arc-backed pending AVET set on unchanged successors,
  cloning only on actual membership changes. Three permanent tests pass; widths
  32/128/512 retain pointer identity, including no-op changes; actual add/remove,
  probe failure and old snapshot contents agree with independent expectations.
- Attribute predicate-name occurrence counts are maintained in Schema so ordinary
  ident changes use point dependency lookups instead of scanning descriptors.
  Genuine function-binding changes still inspect active names for historical
  aliases. The exact `:db.entity/preds` data range remains necessary to validate
  affected identity/function bindings (it has no value index); it is observed by
  existing read admission and is not mislabeled constant time or silently skipped.
- Initial live width/tail samples pass at38attributes and1/33/129txs: schema scans0,
  reuse1, dependency lookups/edges2/2,9ranges,0source datoms,0immutable-node SQLreads;
  complete transact/stats/query/drop samples38.0/26.3/39.9ms. A first adoption test
  incorrectly assumed unsolicited notification from an external administrative
  indexer. It now uses the existing request_index API to let the service coordinator
  authenticate that publication below the novelty threshold; no new polling feature.
- Live AVET schema tests pass2/2; incremental consolidation passed AVET-backfill and
  noHistory tests but failed its localized-cost assertion:91node reads vs a strict
  <87 bound derived as10% of870initial nodes. Diagnose this before acceptance;
  the failed command stopped before its other requested test targets. A separate
  final core PG batch is executing those program/tuple/bookkeeping targets.

Final core evidence:

- Actual PG core batch passes16/16: persisted programs6, bookkeeping1 (all9
  width/tail combinations plus schema rejection, late composite/no-op population,
  enum alias/repurpose, retained values, nonempty-tail restart, exact retries,
  explicit indexing/adoption), tuple input9 including separate Unix processes.
  Bookkeeping test41.99s; widths38/122/410 installed attributes and tails1/33/129
  all use0schema scans,1reuse,2dependency lookups/edges,9cursor ranges,0source datoms
  and0immutable-node SQLreads. Complete transact/stats/query/drop samples25–54ms;
  actual SQL writes/durability are included, not counted as zero I/O.
- Program bindings9/9 pass including5 new permanent witnesses.16 full validations
  at32/256/1024 schema width each use16 cached name points,0 active-name enumeration,
  48entity-predicate visits and64 logical/source reads. Unresolved names, retained
  aliases, wrong program kinds, repurposing, last-reference removal and shared read
  admission stay enforced. Width samples673/667/797µs exclude eager fixture setup.
- Scoped optimized costs pass5 tests (fixed-delta2, metadata1, readiness1, service1):
  recent16append/stats/drop52/54/64/83/99µs at32/128/512/2048/8192retained txs;
  schema assess/drop114/113/241µs at32/256/1024width, zero definition scans;
  metadata1000apply/stats/drop80/43/46µs at16/128/512width, zero full computations;
  readiness137/139/148ns per apply/drop with shared pending sets32/128/512;
  service256append/revision/failure/admission/stats/drop197–263ns/op with32..32768
  backlog entries. Retirement still scales with removed prefix (0–68µs). These are
  scoped diagnostics, not whole product throughput or RSS. Release logs and binary
  fingerprint retained at `/tmp/atomic-repair-pg.vA037i/stage4-release-cost.log`.

Separate CLI prior-repair checks pass2/2 with restricted roles and real PostgreSQL:
Goal1 identity/negation/Or/history/restart/retry3.83s and Goal3 numeric stored-scale/
index/history/restart/retry3.82s. Formatting, diff whitespace and all-target offline
Clippy pass (24.85s);25 distinct prior warnings remain, no new Goal4 warnings.

Final adjacent index gate passes on actual PG (38.19s). Its obsolete10%-of-tree
read bound predated root/separator authentication; the replacement retains all
reads, checks source-derived touched-path bounds and compares1024/4096-item cases.
Initial nodes870→3174; complete successor reads91→91, metadata reads41→41,
new nodes32→32, reused references853→3157, fixed delta3datoms. Independent current/
history and failed-publication/retry/orphan-reuse checks remain. Bytes read grow
87055→246031 and encoded bytes65289→224265 because touched directory blocks widen:
this demonstrates node/path locality, not constant byte or CPU cost.4096-item bulk
setup took7.77s; only its caller wait is now30s, not production lease duration or
ordinary five-second transaction deadlines. Formatting/diff checks pass afterward.

Continuation: Goal4 and Goal0's integrated acceptance are complete. This repair
did not change canonical encodings or require a migration.
