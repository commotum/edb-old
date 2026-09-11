# Goal 6 — Portable block backup, offline values and restore

## Objective

Execute Stage 6 of Goal 0. Replace the old relational backup/restore system with
current-format immutable object copying, shared Rust readers, and guarded Rust
restore publication. Finish this child, then return to Goal 0 Stage 7 for complete
cleanup and integrated product acceptance. Do not create grandchildren.

## Constraints and design evidence

- Parent invariants and first-release policy apply: no old backup readers,
  converters, relational staging tables or alternate live storage engine.
- Local `datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md`
  supplies differential per-lineage repositories, point selection, live capture,
  presence/deep verification, read-only backup values and conditional restoration.
  `1.0.7705/peer/src-clj/datomic/backup.clj` demonstrates identity claims, shared
  immutable segments, root-last publication and reuse of index/log readers.
- Reuse current canonical log/index/program/fulltext/receipt bytes. Capture pins
  the source once; a private filesystem repository publishes points only after
  their complete authenticated dependency closure is durable.
- A repository-local exact covering read value is separate from the canonical
  publication. Build it with shared index algorithms and file-only writes;
  ordinary offline opens must not replay an unbounded log tail. Offline values
  are read-only data, not live root authority or writer pins.
- Separate physical route identity from canonical lineage. Same-lineage restore
  after retirement must not revive old handles or rewrite the content graph.
  Prevent simultaneous active aliases of one restored lineage in one namespace.
- Restore checkpoints strongly retain only fully copied child-closed subtrees;
  pending IDs are weak data until their closure exists. Guard staging/activation
  against GC, target changes, retirement and active writers. Resolve ambiguous
  completion and preserve receipt-first exact retries without callback execution.
- Current-version correctness is mandatory; older development formats are not.
  Never reset unrelated databases or delete the Datomic reference corpora.

## Internal stages

### 1. Shared reads and portable capture

Status: Complete.

Outcome: Differential root-last backups and selective offline values use the same
current immutable structures as the live engine.

Focus: Small object-read/write seams for file capture, authenticated bounded file
admission, per-lineage claims, point selection, exact read-index preparation,
query/history/log/fulltext/program consumers and explicit deep verification.

Completion: Real PostgreSQL capture of new and populated databases produces
offline-readable points; repeats reuse objects, malformed/incomplete repositories
fail clearly, and ordinary opens/queries are selective without source writes.

### 2. Protected restore and catalog publication

Status: Complete.

Outcome: Interrupted/retried restore is safe and resumeable with one conditional
activation and no SQL workflow machinery.

Focus: Route/lineage separation, visible headless reservations, bounded durable
postorder copy, protection across GC and orphaned jobs, writer/retirement fences,
deep verification and exact unknown-outcome resolution.

Completion: Fresh/same-lineage destinations restore exact information and receipts;
GC preserves staged programs/fulltext, retirement and stale workers cannot publish,
and old retired references cannot access restored routes. Actual crash/fault seams
and post-restore matching/conflicting retries pass.

### 3. Administration and complete-path acceptance

Status: Complete.

Outcome: Stock library/CLI backup, point selection, offline reads, verification,
restore and maintenance all use the new path, with displaced implementation removed.

Focus: Port useful cases from catalog_backup, reclamation_backup,
fulltext_backup_restore, admin_backup_cli and the other backup/restore suites;
delete old-format and SQL-shape checks. Preserve role/transport safety, bounded
input and cancellation; measure complete capture/verify/restore/cleanup costs.

Completion: Actual file/stdin/CLI and PostgreSQL workflows pass, including native
programs, temporal/fulltext data and exact receipts. No hidden old backup reader
or SQL restore fallback remains. Record limits and return to Goal 0 Stage 7.

## Continuation

Current-format repository/capture, shared offline reads and route-aware protected
restore are implemented. No legacy backup decoder or relational restore staging
is used by these entry points. Capture writes exact read indexes only to files;
restore uses bounded postorder checkpoints and receipt-first completion records.
Same-lineage restoration now checks prior request occupancy as well as log
prefixes: identical facts under different request keys are not interchangeable.

Material implementation decisions:

- Canonical lineage stays in immutable objects; live roots, leases, notices,
  checkpoints and handles use a separately issued route. Catalog lineage bindings
  prohibit concurrent active aliases. Retirement permanently fences the route;
  restore can issue a fresh route for the same canonical lineage.
- Restore persists at most 64 traversal steps per checkpoint. It retains only a
  bounded in-memory stack prefix (at most 65 frames) and at most 64 newly completed
  object IDs, then persists the final frontier and one batched persistent-map
  update. Its immutable-read cache has an explicit 1 MiB budget. Canonical object
  writes remain individually authenticated and guarded against target/GC changes.
- Root activation and exact restore completion are one guarded publication.
  Completion is checked before writer admission or callback hooks. Excision and
  retirement tombstone both pending restore work and completion owners in their
  own atomic transitions, so old-generation data cannot survive through those
  owners or be returned by an obsolete restore receipt.
- Deep verification authenticates replay/frontiers, all eight index projections,
  earlier receipt values, exact pending-AVET prefixes, fulltext and program edges.
  Reserved receipt aliases use the reserved allocation bound; transaction aliases
  use transaction basis, not the ordinary entity-index frontier.

Displaced implementations removed after bounded equivalence review:

- Legacy source modules `backup_file.rs`, `backup_read.rs`,
  `backup_allocation_tests.rs`, and `backup_tree_semantics_tests.rs`.
- Six SQL-era integration suites (4,001 lines): `backup_restore.rs`,
  `backup_copy_boundary.rs`, `backup_capture_fallback.rs`,
  `backup_semantic_integrity.rs`, `restore_target_proof.rs`, and
  `restore_publication_completion.rs`.
- Current replacements remain in `block_backup`, `backup_reads`,
  `backup_object_admission`, `catalog_backup`, `reclamation_backup`,
  `fulltext_backup_restore`, `admin_backup_cli`, provider-role tests and the shared
  verification/receipt regressions. Obsolete SQL owner ledgers, historical-format
  witnesses, eager damaged-tree fallback and active-route rewind expectations
  were not reintroduced.

## Verified completion

All seven backup-verification unit tests and 65 shared storage unit tests passed;
the latter included configured PostgreSQL checks, not skips. Actual integration
passed: catalog/restore 5, fulltext/program restore 1, reclamation/capture 1,
restricted backup role 1, 64/8192-entity offline read workflow 1, bounded object
admission 1, block backup 4, relative-path 1 and stock backup administration 2.
Manual index CLI, two explicit index-recovery tests and the paused background
candidate/operator race passed. TLS/remote exact-retry tests passed (4).

The final numeric fixture found and repaired a real mismatch: canonical log bytes
normalize signed zero/NaNs, but immediate reports/resident values kept the raw
assessment bits. The writer now uses the once-encoded log's canonical datoms for
both. Exact decimal scale and float bits survive original/retried/restored reports
and re-backup; the full restore/receipt/re-backup fixture took 2.535 s.

Same 16-document fulltext/program restore measured 7.502 s /24,149 SQL calls before
bounded frontier/map batching, then 3.038 s /9,223 SQL calls, read bytes 156,351 and
write bytes 72,871. Capture/verify on the 512-entity fixture measured 937/1748 ms,
85 objects written, with no source object writes. Offline selective open at 64/8192
entities used 24 object reads (36,961/434,045 bytes); complete workflow includes
file/stdin EDN query/Pull/speculation/log, source removal, corruption and cancellation.
The 2 MiB valid-value/96 MiB sparse malformed-object admission test passed with no
RSS growth for the rejected sparse file. These are debug local measurements, not
universal scale claims.

Manual consolidation/fulltext rebuilding shares guarded index publication with
the writer and never steals its lease. Maintenance accepts epoch zero only on a
still-unclaimed root. The validated operator `with_tree_config` policy reaches
ordinary consolidation and explicit recovery; a 512-datom/32-datom-leaf locality
fixture read 16 interleaved versus 3 grouped cold leaves, then zero leaves/SQL in
both warm reads. Explicit recovery can repair the current read index from the
authenticated log, but does not pretend that damaged earlier receipt trees are
repaired: deep inspection remains unhealthy for those roots.

Final-cleanup focused reruns retained the Goal 6 behavior after removing the old
engine (`/tmp/atomic-goal7-focus3.log`): restricted backup role 1/1, catalog/restore
5/5, fulltext/program restore 1/1, lifecycle CLI 4/4, stock product CLI 3/3 and
snapshot references 3/3 passed. Read-only capture of 48 unindexed datoms took
416 ms; capture, concurrent retirement/GC, source drop and offline query/search/log
checks completed in 2.40 s with no source object writes. Fulltext/program restore
took 2.989 s /9,141 SQL calls within its 24.64 s complete fixture. The application
rename/exact-retry/reference handoff completed in 10.241 s.

The same integration pass exposed an operator race with ordinary lease renewal.
Immutable staging now guards the source root, pin and GC state; an operator
observes the non-owned writer lease immediately before final CAS and retries only
lease-only revision changes while the other guards remain exact. Writer-owned
adoption keeps its exact lease guard throughout. Initial/recapture conflicts use
at most three cooperative retries, not an unbounded preparation loop. Both
deterministic publication races passed in 2.62 s
(`/tmp/atomic-goal7-publication.log`); the previously failing complete
bookkeeping/recovery/adoption workflow passed in 70.67 s, with standby 5/5 and
service worker 9/9 passing in the same focus4 rerun. Focus3 as a whole was not a
clean run; these focus4 results supersede its corresponding failures.

The separately opted-in disposable-server WAL test also passed 1/1
(`/tmp/atomic-goal7-wal.log`): immediate PostgreSQL shutdown with durability
settings enabled, restart in 518 ms, acknowledged exact retry, rollback of an
uncommitted reference, and retained immutable values/log. The complete fixture
took 32.81 s. All timings here are local observations, not recovery or throughput
guarantees.

The final moving-publication observation repair is also verified. Backup capture,
inspection, exact outcomes, serialized snapshot references and read-only receipt
resolution share bounded `pin_current_reference` retries; they do not replay a
transaction or relax identity/generation/retention checks. Five focused protection
tests passed in 4.45 s, followed by nine integration targets / 20 tests with exit
zero, including backup roles/CLI/reopen/outcome/inspection/reference cases and
remote product 2/2 (`/tmp/atomic-goal7-observers3.log` and
`/tmp/atomic-goal7-observers-integration.log`). The preceding full library run
passed 438 tests; the later focused tests are not an additional full-suite count.

Goal 6 and its integration repairs are complete. Return to the completed Goal 0/7
integrated acceptance record; no child work remains pending here. No unrelated
database was reset.
