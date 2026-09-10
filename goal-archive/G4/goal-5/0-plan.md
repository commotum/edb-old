# Goal 5 — Integrated Peer-Local Fulltext

## Objective and constraints

**Status:** Complete2026-09-09. All three signals passed; Goal0 continues to Goal6.

Implement Stage5 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: useful analyzed,
relevance-ranked search composed with Datalog and the supplied database value,
with durable PostgreSQL indexing and complete lifecycle integration. Goal0 owns
all invariants and remaining stages; this is the sole active child. Use local
datomic_pro_docs as semantic authority and1.0.7705 fulltext/fulltext_index/lucene
as architecture evidence. Preserve all engine, cache, identity and prior repairs,
old genesis/migration/request/program bytes and durable meaning. No JVM/Lucene
score or wire parity, external search service, alternate store or recursive goal.

Existing :db/fulltext ident is now installed as Boolean attribute51 on fresh
databases; old databases use an explicit ordinary vocabulary upgrade.
Search is an eventually consistent supplement, not part of logical snapshot
identity. Every returned fact must belong to the supplied view; no synchronous
freshness or stable-ranking fiction. Substring matching is not fulltext. Use
bounded resource policy and explicit error/lag states rather than silent omission.

## Stages

### 1. Search semantics and compatible authoring

**Status:** Complete; verified schema/legacy and query/program tests below.

**Outcome:** Fulltext schema and native search have explicit, versioned meaning.

**Focus:** String-only immutable schema property, compatible vocabulary upgrade,
analyzer/query syntax, ranked hits, public Rust/Datalog and durable program forms.
Reuse query controls and view/index interfaces; no new language/JIT requirement.

**Completion signal:** Analyzer, syntax, ranking and input/control checks pass;
old data and program/request meaning remain exact. Search joins structured facts
in pure fixtures and returns entity/value/transaction/score with stated semantics.

### 2. Durable background search and safe ownership

**Status:** Complete; actual PostgreSQL lifecycle and background failure/retry pass.

**Outcome:** Peers search PostgreSQL-backed immutable search structures without
centralized query evaluation or dependence on transactor query availability.

**Focus:** Authenticated versioned storage/publication, bounded construction/read
paths and cache reuse, indexing lag, current/history/temporal/speculative filtering,
rebuild/recovery, backup/restore, GC and explicit excision/retention policy.

**Completion signal:** Durable blocks precede publication; interrupted indexing
cannot expose partial search. Retained values and new captures obey ownership;
missing/corrupt data fails safely. Real PostgreSQL lifecycle tests cover updates,
retractions, restart, rebuild, backup/restore and reclamation without source edits.

### 3. Application and measured acceptance

**Status:** Complete; real application, measured native/cache and ACL checks pass.

**Outcome:** The evolving application uses search as part of real database work.

**Focus:** Search/structured joins and retained/history views, documented analyzer,
query/rank/lag limits, working public APIs and measured indexed work, storage/cache
bytes and background/foreground SQL. Extend existing fixtures and fault controls.

**Completion signal:** Actual PostgreSQL/application checks prove all preceding
signals together; search remains peer-local and view-correct. Record results and
limits here and in Goal0, then execute Goal6. This child is not the parent's end.

## Continuation

Completed2026-09-09 after Goal4. Return to Goal0 and execute Goal6. Reopen this
child for a later integrated search gap; this is not parent completion.

## Decisions and verified results

- `Attribute.fulltext` is false by default, String-only and immutable after
  installation. Exact standalone `fulltext_vocabulary_upgrade_ops()` installs51;
  no startup rewrite or conversion of existing ordinary attributes. All prior
  genesis profiles remain byte/hash-identical; flagged descriptor tags9/10 use
  wiregrammar4, fulltext query templates use ABI9, old forms retain old bytes.
  Real old-binary4818-byte genesis upgrade/retry/native reopen passed; preserved
  binary `/tmp/atomic-fulltext-old.2AjI71/atomic` SHA256
  `f1a2eea82d437017e1431a5d5e9b12d0ca7fe6a0d1fc9fb8ffd4258f8c09aeb0`.
  The one-time schema `atomic_fulltext_legacy_2aji71` is now upgraded; recreate
  with that binary before another legacy test. Schema4/4 actualPG, pure upgrade
  2/2, vocabulary6/6, wire7/7, excision8/8 passed.
- Analyzer1 uses Unicode lowercase/alphanumeric words, apostrophe/possessive
  removal and the documented English stopset, no stemming/substring substitute.
  Terms/defaultOR, phrase positions, Boolean groups and trailing prefixes have
  an explicit native grammar; unsupported Lucene operators fail. Each OR branch
  needs a positive anchor. Native BM25 is relative available-corpus relevance,
  not Lucene score parity or snapshot-key-deterministic selection. Query/program
  work/bytes/deadlines/cancellation compose; truncation/lag are observable.
- Fulltext candidates come from immutable source-manifest-bound sidecars, never
  a fallback whole-native-database materialization. Original string and visible
  assertion transaction are checked through the supplied view, including
  history/as_of/since/filter/speculation. Newly reasserted old text returns the
  new visible transaction. Programs may use eventual search; complete-membership
  constraints must use structured indexes and are not delegated to search.
- RealPG query/stored-program6/6 plus native-program7/runtime22/partition-authoring4
  passed; encoding15 passed. The evolving restricted-role separate-process app
  runs schema10/articles11, two search hits/public structured join1, portableABI9,
  exact retry across writer restart and explicit publication recovery. Product
  test1/1 passed8.82s. New fixture attribute IDs moved to1010/1011 after1008
  collided with the already allocated partition entity; no old request changed.
- Initial actualPG native view/cost witness1/1 passed5.00s:402documents,
  2409records/106blocks/211095searchbytes; sortbuffer peak16424bytes and1543191
  cumulative spillbytes. One selective token fetched10167searchbytes,14totalSQL,
  cold4521us; warm155us/0SQL/0searchbytes. These are one host/debug-build samples,
  not throughput/scaling claims; PG/OS caches were not flushed. Final resource
  accounting rerun passed native1/1(5.14s) plus query6/6(2.61s), recording
  cold4147us/warm151us, the same payload/SQL counts and22918 cumulative admitted
  bytes. Warm hits charge decoded visited bytes, not zero I/O bytes; parser,
  position maps, phrase/Boolean work and sorting debit the same budget. Command: set the isolated
  `ATOMIC_POSTGRES_URL` and plaintext policy, then `cargo test --offline --test
  fulltext_native --test query_fulltext -- --nocapture` as the fixture owner.
- Final native rerun1/1 passed4.88s:4523us cold/156us warm; cache7entries/15321
  retained bytes within512entries/256MiB. Cache accounting includes positive
  headers and decoded capacities, not process RSS. Pure analyzer4/4 includes
  a4096-token balanced query on512KiB stack. Final query8/8 actualPG also covers
  cardinality-many/history, long Unicode tokens and overlapping prefix/exact
  queries with identical scores. `cargo check --offline --all-targets` passed.
- Storage pure3/3, actualPG lifecycle4/4(15.02s), runtime roles1/1(2.62s) pass.
  Migration27 sidecar blocks/header preserve canonical roots; blocks precede
  publication. Tests cover interrupted publication/exact retry, missing page
  fail-closed, owner-only bounded repair, genesis, reopen, portable restore
  with explicit initial lag/rebuild16hits, build-capacity failure leaving the
  canonical publication valid, held-root protection and bounded orphan GC,
  excisiongeneration1→2 current/history exclusion and exact authorized old value.
  Least-privilege writer publishes, peer searches, writer cannot discard/repair.
  Stress fixture600records/55blocks/54295encodedbytes,540270cumulative spillwrites,
  peak1107sortbytes(configured1024plusone record); selective10records read4blocks/
  3374payloadbytes, warm0SQL. These1KiB-page debug samples are not throughput claims.
- Background policy2/2 and actualPG failure/recovery1/1(2.28s) pass: real advisory
  lock contention is observable, transaction commits, then one idle retry after
  unlock succeeds without another request. Attempts2/failures1/retries1/canonical
  jobs0. Eight bounded transient retries; permanent/exhausted errors remain
  visible and explicitly retryable. Pending AVET work cannot mislabel search
  status. Canonical authority is independent of optional search-build failure.
- Fulltext rebuild currently scans the source's full fulltext history, uses
  bounded spill merge and constructs a new source-specific Merkle tree; it is
  not incremental. Cache has its own configured allowance and is not on SSD.
  These costs and repair/excision/retention boundaries are documented in
  `docs/fulltext.md`, not hidden as future feature exclusions. Stage7 must include
  representative search/build costs in the complete operating envelope.
