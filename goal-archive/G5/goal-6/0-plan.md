# Goal 6 — Incremental authenticated fulltext maintenance

## Objective and constraints

Complete Goal0 Stage6/R10: reuse unchanged search content and integrate text novelty
without rebuilding all historical text for every new canonical publication.
Status: **complete, including integrated reopening (2026-09-10)**.
Goals1–7 and Goal0's integrated acceptance are complete; no child is active.

Use `datomic_pro_docs` as semantic authority and `1.0.7705` as architectural
evidence. Preserve native Rust/PostgreSQL, current/history/filter/time views,
exact retries, durable codecs and authenticated source binding. A sidecar is a
derived projection, not authority to bypass supplied-view checks or excision.
Keep explicit rebuild/repair for genuine invalidation. Do not add Lucene format/
ranking parity, other stores, or optional search features.

Starting evidence: `peer_fulltext.rs` opens an existing exact-source sidecar,
otherwise streams all historical assertions through `fulltext_records.rs` and
the external-sort builder in `fulltext_store.rs`. Bounded sorting is not delta
maintenance. Inspect retained-source/GC/publication contracts before reuse.

## Ordered work

### 1. Reuse authenticated projections and integrate novelty

- **Outcome:** Unrelated changes reuse search content; text changes touch affected
  documents/postings and routing paths while retaining historical assertions.
- **Focus:** Safe predecessor selection, source/schema/analyzer/generation checks,
  bounded delta records and persistent page maintenance; truthful work counters.
- **Completion signal:** Permanent unchanged/non-text/fixed-text regressions agree
  with a full-rebuild oracle and show no avoidable corpus-wide read/tokenization/
  rewrite. Invalid predecessors trigger an explicit safe fallback, not stale reuse.
- **Status:** Complete. Authenticated subtree diff, changed-record maintenance,
  persistent shared pages and admission-preserving empty-corpus bulk loading pass.

### 2. Preserve lifecycle and supplied-view correctness

- **Outcome:** Search remains correct across retractions, schema evolution, lag,
  retained snapshots, restart, failed publication and reclamation.
- **Focus:** Existing authenticated publication/pinning, excision/GC, repair/rebuild,
  current/history/time/filter semantics and failure/retry safety.
- **Completion signal:** Real PostgreSQL/application lifecycle regressions and
  independent rebuilt results pass; reused pages cannot resurrect excised facts
  or depend on a deleted predecessor header. Existing readers remain supported.
- **Status:** Complete. Optimized real-PG storage4/4 and lifecycle4/4 pass,
  including legacy bytes, interrupted guards, restricted roles and reclamation.

### 3. Establish complete-path costs and return to the parent

- **Outcome:** R10 is repaired with measured costs and maintainable native code.
- **Focus:** Growing corpora with fixed text and non-text deltas, cold/warm reads,
  tokenization, writes, bounded memory, SQL and cleanup; neighboring regressions.
- **Completion signal:** Configured PostgreSQL checks and scoped optimized costs
  cover actual publication/consumption, including metadata work and legitimate
  rebuild cases. Formatting/relevant Clippy pass. Record evidence here and in
  Goal0, then execute Stage7 rather than stopping at this child's completion.
- **Status:** Complete. Optimized primary PG matrix2/2 and local16/16 pass;
  complete-path counters and qualified costs are recorded below. Goal7 owns broad
  integrated acceptance and the five new minor lint sites, not another parent.

## Evidence and continuation

Disposable durable PostgreSQL15.11:127.0.0.1:55471, user/database `atomic_repair`,
plaintext; cluster `/tmp/atomic-repair-pg.vA037i/data`. Use unique fixture schemas
and approved host access; retain Goal1's genuine old-engine receipt witness.
Use Goal0's low-debug/nonincremental build policy and a consistent optimized
snapshot for performance comparisons. Skipped tests are not live PG evidence.

Initial evidence and material decisions (2026-09-10):

- `tests/fulltext_incremental.rs` reproduces R10 against the actual service/indexer:
  64 documents plus one unrelated Long change rebuilds257 search records and12
  pages/22011bytes, with56570 spill bytes and16438 sort-buffer peak. Complete
  transact/consolidate/reopen/search/check/drop takes458783µs, with278 foreground
  SQL calls, excluding Connect/Close (the baseline log mislabeled these driver
  calls; final regression reports both). Worker transaction SQL is not attributed
  to that thread. The
  regression fails on the required zero non-text record input. Baseline2.77s;
  raw log `/tmp/atomic-repair-pg.vA037i/stage6-before.log`.
- Existing live PG baseline: fulltext storage4/4 in5.10s (interruption/repair,
  excision, portable restore and pin/GC), native supplied-view search1/1 in5.72s.
  These precede the repair and are not final acceptance. New permanent comparisons
  cover growing corpora, noHistory64/1024, retraction/reassertion, new attributes
  and exact retry, comparing all records to an explicit full reconstruction.
- Docs confirm eventual fulltext and immutable fulltext attribute declarations;
  background indexing may legitimately perform occasional broad work. Recovered
  `fulltext_index.clj/update-fulltext` reuses persistent per-attribute directories
  and adds supplied novelty. Native `noHistory`/excision behavior remains our
  existing supported contract, not an excuse to keep stale search documents.
- Per-source `(manifest,hash)` ownership cannot safely share pages after predecessor
  retirement. Implement shared content-addressed pages/edges and manifest roots with
  bounded GC, a persisted interrupted-build guard and concurrent-build/GC fence.
  Preserve FORMAT1 page/header bytes and canonical encodings; legacy conversion is
  explicit one-off authenticated work, not repeated corpus copying.
- Compare authenticated same-lineage/generation canonical history EAVT trees,
  skipping matching directory/leaf hashes; globally merge unmatched streams so
  repacking does not become false novelty. This also sees actual noHistory
  removals. Tokenize changed assertions only, update touched corpus statistics,
  and path-copy changed search pages. Account root/directory inspection and SQL.
- Public rebuild-on-missing remains a genuine full reconstruction after explicit
  discard. Ordinary indexer/service ensure/retry uses incremental maintenance;
  no new optional search features or silent repair weakening.

Development verification (superseded by the final snapshot below):

- The path-copy store and record/history diff are implemented. Store tests7/7
  pass0.58s, including900 deterministic upsert/delete operations versus an
  independent map and reachable-page/count oracle, retained old roots, no-op
  writes, corruption and input admission. One additional successor-independence
  case awaits the final snapshot. Fixed replacement64/256/1024/4096 takes2/3/3/4
  path reads/writes, workspace5754/7114/7978/8874 accounted bytes (not total RSS).
- Pure source diff3/3 passes initially; fixed8-datom leaves inspect16 datoms,
  two directories/two leaves at128/512/2048. Metadata references16/40/136 grow
  with root width. A fourth error/fusing case awaits final verification.
- First live repaired run demonstrated64-document non-text0 input/0 page uploads/
  0 tokenized bytes and text5 input/23 page uploads/50 tokenized bytes. It then
  failed on two fixture issues: concurrent unrelated schemas shared a GC fence,
  and a new test combined installing a numeric attribute with using it before the
  engine's resolver knows it. Scope the fence to its installation table; oracle
  discard retries only bounded Busy. Use the established separate schema/data
  transactions, without adding same-vector schema-resolution support to R10.
  These are explicitly recorded, not counted as a passing target.
- Default4096-datom leaves may contain much of a64–1024-document corpus; the
  second real-PG ladder uses explicit64-datom leaves to expose bounded source work.
  Both print physical configuration. Build max_records stays an input admission
  budget, not a new corpus-size restriction. A one-input budget permits unchanged
  reuse but rejects a larger text mutation without rolling back canonical data.
- Shared GC needs indexed candidate discovery, not just LIMIT on a global live-
  page scan. The final implementation supplies that frontier, installation-scoped
  fencing, hardened definer search paths, restricted-role and legacy coverage.

Final acceptance (2026-09-10):

- Optimized store10/diff4/empty-proof2:16/16 pass,0.04/0.02/0.02s. Includes900
  deterministic edits against independent records/reachable-page totals, retained
  roots, complete predecessor removal, corruption, fused errors and the general
  two-zero-attribute/max5-input→six-output-record bulk-merge witness.
- Actual PG primary2/2 pass27.47s. Sizes64/256/1024, canonical leaves4096 and64,
  search pages2048, sort16384: nontext0input/0uploads/0tokens; fixedtext5inputs,
  50tokenbytes,21/23/31upload attempts at64-datom leaves. Source datoms130/131 stay
  fixed there, but metadata references11/17/41 and13/19/43 grow. Whole foreground
  transact/consolidate/reopen/search/check/drop costs225844/229502/219058µs nontext,
  271836/275083/263531µs text. Default4096-datom leaves legitimately decode larger
  changed leaves; no universal constant-read claim. Counters include upload
  verification, physical inserts, direct edges/roots and the counted retention pass.
- Normal128/noHistory64/1024 replacement/retraction/reassertion and new fulltext
  attributes match all records from forced reconstruction, preserving supplied
  views and restart/exact receipts. A failed small-input search build does not
  roll back canonical acknowledgment. Additional live empty-corpus test1/1 passes
  with nontext0input, unchanged-stat admission and empty-string-document protection.
- A new per-record-COW initial-load regression was caught and repaired within R10.
  Authenticated empty-corpus bulk merge preserves delta-only admission.1024doc
  optimized setup1.059s(default leaves)/0.940s(64leaves); low-debug comparable
  before/after44.068→3.486s and25.090→4.102s. Profiles are not mixed as speedups.
- Final optimized existing storage4/4 pass7.77s and new lifecycle4/4 pass8.45s:
  frozen legacy FORMAT1 bytes, interrupted builds/GC fence, runtime upload grants,
  temp-table shadow rejection, excision, pinning, bounded discard, restore/rebuild,
  predecessor retirement and last-root reclamation.128doc lifecycle uploads1156→8
  after bulk repair; successor cold128hits and rebuild128hits remain correct.
- Migration30 keeps canonical/FORMAT1 bytes; shared root/edge references avoid
  predecessor chains. GC scans an indexed candidate frontier, not all live pages;
  physical deletion is bounded, but guarded candidates may add inspection work.
  A build guard protects interrupted uploads; finish counts its source-origin
  page pass. Explicit discard is owner-only and can require multiple batches.
- Formatting/diff checks and all-target Clippy pass;30 distinct warnings include
  five new mechanical sites, assigned to integrated Rust hygiene. Primary release
  binary SHA2562f085cfeb5c894aca72ef8b78413ccef96c8624a7c5f33fc60b007a1defd887d;
  logs under `/tmp/atomic-repair-pg.vA037i/`: `stage6-pg-release.log`,
  `stage6-store-release.log`, `stage6-storage-final.log`, `stage6-lifecycle-final.log`.

Integrated reopening (now complete): Goal6 repaired the shared
program-GC preview/deletion ordering mismatch discovered by broad acceptance.
Retain oldest-first bounded selection, compare exact membership consistently, and
prove multiple aged orphan programs can be collected without deleting live ones.
This is a required operations repair, not an optional product feature.

- New isolated `program_gc_order` regression reproduces the false conflict in
  3.67s with513 aged programs whose age order opposes hash order. Sorting only a
  bounded comparison copy preserves public preview/oldest-first selection. Repaired
  test passes3.49s:512 then1 collected, one young orphan retained, empty pass safe.
  No format/migration change. Fulltext lifecycle4/4 also passes24.53s while other
  suites run; that elapsed time is correctness evidence, not a performance sample.
- Broad race check reproduced a second real defect: two builders returned
  different same-basis physical roots because the loser rebuilt1306datoms with
  zero tail. Deterministic pending-fold witness reproduced964input/340nodewrites
  and an unnecessary extra revision. Administrative consolidation now finishes
  only authenticated newest/same-head resumable work, then reselects/reuses it.
  Corrupt/missing witnesses still permit full repair. All20peer and9background
  checks pass11.53s/5.76s; both builders agree on revision/hash and the deterministic
  winner is retained with no new input/nodewrites (folding still performs SQL).

Continuation: Goal7 and Goal0's integrated acceptance are complete, including old
receipt/upgrade, TLS and independent crash fixtures. Reopen Goal6 only for a newly
reproduced owning regression. R10's measured
workspace is not process RSS, SQL calls are not wire round trips, and optional
search readiness is not the canonical transaction receipt.
