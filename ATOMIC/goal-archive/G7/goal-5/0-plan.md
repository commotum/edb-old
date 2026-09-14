# Goal 5 — Retention, reclamation and database lifecycle

## Objective

Execute Stage 5 of `/home/jake/Developer/atomic/goal-0/0-plan.md`. Make Rust own
safe incremental reclamation, retained values and receipts, excision, and catalog
lifecycle over opaque blocks and conditional references. This is one child, not
a new parent. Preserve the working public product and finish this stage before
returning to the parent's backup/admin and final cleanup stages.

## Constraints and current evidence

- Local Datomic docs are semantic authority, especially capacity/GC and excision.
  Recovered `datomic/garbage.clj`, `cleanup.clj`, catalog and index code inform the
  algorithms. Translate ownership/concurrency idiomatically into Rust.
- Ordinary GC does not scan live data segments. Advance root ownership and
  retirement incrementally, exploiting immutable sharing. Account its complete
  work, not only deletion. The Stage 1 full-mark proposal is superseded here.
- Shared object references, active readers/builds, retained exact receipts and
  current roots are real owners. No reclaiming them merely because a local clock
  expired. Explicit revocation/fencing must prevent stale access/publication.
- Current pins have explicit release but leak on ordinary Drop/crash; read-index
  provenance retains every published index. Fix these with bounded non-blocking
  cleanup and documented retention, without weakening exact retries.
- Excision is exceptional, outside the historical timeline. Preserve prior policy:
  held values may remain readable, new opens cannot regain the old generation,
  and pre-excision request keys remain occupied but privacy-safe tombstones reject
  retries rather than retaining old plaintext or invoking their callbacks again.
- No per-feature SQL ledgers, SQL workflow functions, legacy format readers or
  alternate engine. Fresh formats allowed. No reset of unrelated databases or
  deletion of reference corpora. Retain relevant behavior tests, not old SQL shape.

## Internal stages

### 1. Ownership, pins and incremental collection

Status: Complete.

Outcome: Coherent Rust root transitions and incremental ownership/retirement make
bounded collection safe across publication, pins, receipts, shared data and builds.

Focus: Complete the guarded epoch protocol; track changes as immutable/opaque
engine metadata, release reader ownership off-thread, handle abandoned preparation,
prune superseded unowned index authorization, and persist resumable collector work.
No whole-live-database replay/tracing in normal collection or publication.

Completion: Actual PG tests interrupt/resume collection, race publication and
captures, retain old values/exact retries, reject stale workers, and demonstrably
delete eligible objects. Complete costs include ownership and cleanup.

### 2. Catalog and excision

Status: Complete.

Outcome: Creation, rename, retirement/name reuse and background excision operate
on the same block engine with stable identity and unambiguous durable outcomes.

Focus: Replace relational catalog/maintenance drivers; reuse pure predicate/tree
algorithms. Sanitize log/index/fulltext/receipt/provenance state coherently, retain
excision records/allocation meaning, resume partial work and integrate the real
service and sync-excise workflow. Coordinate admission and final writer adoption.

Completion: Real application checks cover name reuse without redirection, stale
writer rejection, all supported excision selectors/components/history, interrupted
rewrite, protected old handles, new-generation reads and privacy-safe retry errors.

### 3. Integration and removal

Status: Complete.

Outcome: One live retention/lifecycle implementation remains, with useful regressions
ported and backup/restore protection hooks ready for Stage 6.

Focus: Exercise real writer/index/consumer/maintenance races, measure bounded
complete paths and remove superseded stage-owned SQL/Rust/tests. Do not retain
the old maintenance engine as a fallback or call lost functionality cleanup.

Completion: Stage capabilities pass actual PG/application checks and architectural
review; results, limits and next action are recorded here and in Goal 0. Then
return to Goal 0 Stage 6; this child is not the product finish line.

## Decisions and verified progress

- Root changes atomically append opaque ownership events with their publication.
  The collector seals an epoch, folds additions before removals, caches immutable
  child metadata, and sweeps object metadata under checkpoint/epoch guards. New
  objects' ownership must be learned once; ordinary removal never rereads their
  data segments. Whole-path measurements include that initial ownership work.
- Reader liveness uses a namespace-scoped shared session lock. Revocation requires
  obtaining its exclusive lock. Disconnected sessions get five minutes after
  revocation before abandoned pins are reaped; active sessions are never expired
  by age. Final-owner cleanup is bounded/off-thread and physically forgets its
  one-use pin keys. Collector-reaped releases are folded in the next epoch.
- GC age applies to retired published objects. Never-published abandoned objects
  and superseded collector metadata are reclaimable after their builders' epoch
  is fenced. Read-index authorization pruning preserves current indexes, initial
  anchors, receipt/pin owners and uses conditional ownership proofs.
- Catalog creation, concurrent create, UTF-8/long-name paging, rename, retirement,
  name reuse and writer fencing passed three actual PostgreSQL regressions.
  Two broader lifecycle regressions exposed standby handling of capture conflicts;
  repair/re-run is pending rather than weakening their expectations.
- Two actual PostgreSQL GC tests passed: live Peer/held values/exact receipts and
  writer reopen; interruption/checkpoint displacement/retirement age/reclamation.
  Costs were unacceptable: initial two-write fixture 6.7 s/13,545 SQL calls,
  repeat 14.5 s/29,401. Batch ownership/tree/frontier updates, small-node caching
  and bulk metadata protection are now implemented, not yet remeasured.
- Re-measurement passed: first cycle 1.090 s/2,281 SQL calls/19,391 written bytes;
  repeat 0.892 s/1,881 calls/6,284 bytes on the same small two-write workflow.
  Catalog (3) and broader lifecycle (2) tests now pass. These are fixture costs,
  not an unmeasured large-database scalability claim.
- The collector now durably checkpoints before data deletion and before event
  forgetting. Permanent interruption/restart tests prove those boundaries.
  Session cleanup, ownership-conditional pruning, batch-tree equivalence,
  reclaimed-index rejection and root-only renewal/replaced-token races pass on PG.
  Tests run with bounded harness parallelism: unrestricted simultaneous fixture
  creation exhausted the shared test server's connections, not a semantic check.
- Block-native excision checkpoints, service adoption, sanitized receipt occupancy,
  catalog/operator routing and retired-database reclamation are being integrated.
  Excision parks fresh writes only after successful admission, while reads, exact
  retries and lease renewal continue. Its whole fresh-write pause must be measured.
  New source and tests are not yet acceptance evidence.
- Service excision and interrupted/operator checkpoint paths passed before the
  final stable-ID operator refactor. Small automatic sample: 2.317 s complete,
  1.514 s fresh-write pause, four rewritten transactions/904 source bytes.
  Program/fulltext post-release GC and the final operator changes still need rerun.
- Serialized values are being corrected to carry their small value descriptor,
  not depend on an old wrapper object surviving GC or require peer object writes.
  Reopen still must prove canonical receipt/index authority and generation before
  building a pinned in-memory value. This is a fresh reference format, not an old
  format reader. Read-only-object role regression is pending.

## Continuation

Goal 5 is complete; earlier pending paragraphs above are chronological evidence,
superseded by these final results. Actual configured PostgreSQL checks passed:
59 storage unit tests (including seven excision tests, physical program/search
reclamation after held-value release, crash/session/pruning/publication races and
read-only-object-role serialized reopening); GC 2, catalog 4, lifecycle 2,
reclamation 1, operator GC 1, scoped inspection 1, integrity 2, CLI 3, consumers 9,
fulltext 2 and TLS remote 4. ABI identity and bounded reference codec regressions
also passed. Automatic excision measured 2.256 s complete / 1.405 s fresh-write
pause; the held program/search release workflow reclaimed 126 obsolete objects.
These are fixture costs, not broad production scalability claims.

Final integration repaired two actual gaps: explicit block-log allocation
checkpoints replace receipt-tempid inference in deep replay; remote discovery
checks permanent retirement before an absent endpoint. An asynchronous writer
pin-cleanup fixture now checks for newly retained pin identities, not an unstable
total count. No lifetime-pin requirement was weakened.

Removed the superseded relational excision/reclamation drivers and SQL-specific
GC/program/fulltext tests. Useful ABI and deferred backup cases were retained in
focused tests; all deletions remain recoverable from git. Subsequent Stages 6–7
removed the remaining SQL installation/backup implementations and completed
same-lineage restore with route/lineage separation, preserving permanent
retirement of old references.
No unrelated database or Datomic corpus was reset or removed.

Stage 7 reopened protected program deployment acceptance after deleting the old
store API. `PostgresOperator::deploy_program` now returns a session-owned
`ProgramDeployment`: transitive code is authenticated and protected under one GC
guard before pin publication; a committed function binding transfers ownership
to the ordinary database graph. Missing dependencies fail. Explicit release or
asynchronous Drop permits unbound reclamation. The new concurrent GC/binding
regression passed on actual PostgreSQL in the first broad Goal 7 run (one test,
3.76 s), including exact code/dependency round-trip, binding, reopen and unbound
reclamation. The deployment-specific reopen is closed; final integrated evidence
is recorded below.

Stage 7 also repaired connected lagging-Peer report continuity across excision.
Each generation handoff retains its final publication and the next generation's
interest token, never its own token. Peer interest belongs to connected peers,
not ordinary held database values, so those values do not retain future report
history. Root publication atomically moves the current observer anchor;
read-only-object peers pin the precreated token without inserting objects.
Creation and exact restore activation publish the matching route/generation
anchor, and retirement tombstones it under the same fencing guards. Tokens are
route-scoped, so restoring a lineage onto a fresh route cannot revive old handles.
Bounded handoff pruning requires a settled zero-owner count and joins its
ownership-clock/root/GC proof to the removal CAS. Earlier handoffs preserve
skipped intermediate generations until peers advance or drop; removal then
cascades through subsequent collection cycles without a permanent root chain.

Final focused PostgreSQL verification passed all seven `native_connection`
tests in 31.51 s, including original exact reports across two excision rewrites
and reclamation after both connected observers release their interests. The
handoffs were reclaimed in three cycles / 8.315291 s while five delivered reports
remained held. The read-only backup-role test (one) and catalog/restore suite
(five) also passed: source retirement/collection, headless restore fencing,
exact activation retry, stale head refusal, same-basis generation selection,
privacy-safe tombstone retries and excision revocation of old restore work.
These are measured fixture results, not a large-database cost claim.

The broader run exposed idle pin cleanup stopping permanently after a GC or
ownership conflict. Cleanup now requeues its one reserved job only while the
original session/pin authority still matches, with 1–50 ms worker backoff;
terminal authority failures remain terminal. A deterministic real-GC race test
passed in 0.41 s without another reader operation. All nine excision library
tests passed, including bounded recapture for public synchronous excision and
explicit semantic pruning in the raw-collector fixture. The program/search
workflow physically reclaimed 139 objects after release in 17.267 s. Restricted-
role automatic excision also passed (one test, 7.87 s), including real checkpoints.

Final verification passed the full 438-test library run (324.76 s) and the seven
native-connection regressions, closing the idle-pin/GC repair. Later shared
current-reference capture checks also passed: five focused protection tests and
nine affected integration targets / 20 tests, including exact outcomes,
inspection, reference reopening and read-only backup roles. These focused tests
are separate from the earlier full-library count.

Goal 5 and its lifecycle integration repairs are complete. Return to the completed
Goal 0/7 integrated acceptance record; no child work remains pending here.
