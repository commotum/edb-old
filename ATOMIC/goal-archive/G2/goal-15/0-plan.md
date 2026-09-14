# Goal 15 — PostgreSQL Lifecycle and Operational Safety

## Objective

Make the PostgreSQL-only native database safe and truthful to provision,
upgrade, inspect, back up, restore, reclaim, and excise. Preserve the
authoritative immutable log and derived-root architecture while replacing
prototype shortcuts with source-backed, database-scoped operational behavior.

## Constraints

- `datomic_pro_docs` is the semantic authority. Recovered migration/SQL,
  backup, garbage, excision, index publication, and lifecycle paths from
  Datomic Pro 1.0.7705 are the default architectural blueprint.
- Rust throughout and PostgreSQL only. Use PostgreSQL directly; do not add a
  storage abstraction or work on Goal 16's production-writer representation.
- The transaction log and semantic commitments remain authoritative. Tree
  nodes and manifests are derived, immutable, replaceable physical values.
- Runtime service and peer roles must not need DDL. Upgrade authority is an
  explicit operation, and a binary must fail closed on an unknown newer schema.
- Backups and inspection describe one coherent database point. Publish copied
  content before roots, make filesystem publication crash-safe, and verify the
  exact restored state rather than only checksums.
- Reclamation must honor immutable snapshot pins and a safe age boundary.
  Corruption belonging to one database must not abort unrelated logical work;
  shared deletion must become conservative when reachability is uncertain.
- Excision is a privacy operation over source-backed scope, closure, permanent
  audit information, derived data, and synchronization. State limitations
  about PostgreSQL WAL and existing external backups precisely.
- PostgreSQL tests count only when an explicit live server actually executes
  them. Preserve unrelated dirty-worktree changes.

## Known context

- Goals 10–14 established collision-free identity, information-derived
  schema/idents, one fenced writer, authenticated persistent trees, and exact
  lazy peer/query values.
- `PostgresMigrator` now owns explicit upgrade/runtime-grant authority. Service,
  peer, indexer, and tree-writer startup only validate the installed schema and
  use one configurable PostgreSQL connection policy.
- Existing operations tests were written for flat segments. Native tree roots,
  revisions, pins, and cross-database corrupt-manifest behavior are not fully
  reflected in inspection, backup, GC, or excision.
- A prior real-PostgreSQL run exposed operations GC/excision failures after a
  corrupt manifest for another database was encountered globally. The
  authoritative transactions themselves remained valid.
- Goal 9 evidence items C17–C20 and its internal correctness requirements map
  the relevant docs and recovered source. Goal 16 owns removing the eager
  production writer; Goal 17 owns the final cross-goal deployment gate.

## Stages

### 1. Explicit upgrade and secure runtime boundary

**Status:** Complete and independently revalidated on PostgreSQL 15.11. In
addition to the migration/role/ACL/TLS evidence below, four isolated live
migration fixtures now prove a fresh idempotent install, pre-v6 rejection
before mutation, populated v6 canonical replay through v12, and populated
v11-to-v12 preservation. A dedicated process-stop/restart witness proves
cached peer reads, wait-through-outage synchronization, standby recovery, and
reborrowing by the same long-lived runtime handles.

**Outcome:** Provisioning/upgrades are explicit, while least-privilege writer
and peer startup perform only version checks and normal runtime SQL over
configurable secure PostgreSQL connections.

**Focus:** Migration ownership and checksums; installed/forward version
handling; runtime role grants; removal of implicit DDL; TLS mode and trust
configuration; stable startup anomalies.

**Completion signal:** Fresh provisioning and upgrades pass under the migration
role; separately granted runtime writer/peer roles start and operate without
DDL; a future schema version fails before service; required TLS succeeds and an
insecure connection is rejected in a real PostgreSQL witness.

**Evidence and decisions:** The docs make SQL provisioning explicit
(`00_storage_services.md:107-131`) and separately require an intentional,
version-ordered logical base-schema upgrade
(`02_datomic_deployment.md:235-258`). Atomic's checksummed relational migration
protocol is PostgreSQL-native machinery inspired by those operational
boundaries, not Datomic's `:upgrade-schema` representation. The recovered
`kv_sql_ext.clj:113-170` builds one validated configured data source, while
`sql.clj` performs ordinary CRUD/CAS. Native code therefore uses the concrete
`PostgresMigrator` plus `PostgresConnectionConfig`, not a backend trait. Runtime
checks require the complete checksummed migration prefix and reject an unknown
future version before any database read or publication.

`PostgresStore::connect[_configured]` is now a checked runtime constructor and
the store has no migration method; `PostgresMigrator` is the only public
unchecked/admin connection owner. The populated in-place floor is version 6.
Recovered pre-v6 source used format-1 transactions with raw entity/transaction
coordinates and separate schema changes, whereas migration 6 names the
format-3 schema-information boundary; a populated pre-v6 catalog is therefore
rejected transactionally with `postgres/upgrade-rebuild-required` and must be
exported through a compatible old decoder. Migration 9 originally introduced
zero state-commitment placeholders for old rows. The migrator now performs one
locked canonical genesis/log replay, verifies every basis/hash/envelope/head,
and backfills each commitment before admitting the upgrade; an incompatible or
corrupt history rolls the entire schema change back. Migration 12 continues to
discard only replaceable derived tree publications, never authoritative log
bytes. Live isolated-schema tests establish nonzero commitments and exact
current/history recovery after populated v6-to-v12, plus unchanged transaction
hash/payload rows and exact recovery after populated v11-to-v12.

The recovered datasource validates/reborrows connections, and the deployment
docs require peers to reconnect automatically, keep serving their latest
consistent local value, and let sync wait through storage/transactor outage
(`02_datomic_deployment.md:47-61`). Native long-lived handles therefore retain
their concrete connection policy. Transport and restart SQLSTATEs are tagged
separately from semantic SQL failures; peer sync, lazy reads/materialization,
and index adoption reborrow without replacing immutable `PeerState`, root pins,
or caches. `sync_to` caps every reconnect attempt by its remaining deadline.
Standby startup retries storage loss, while arbitrary writes are deliberately
not retried. Store/tree handles expose explicit checked reborrow, and index
consolidation safely retries one whole immutable/idempotent build. The dedicated
restart test stops PostgreSQL after opening all handles, serves the cached old
database while offline, blocks a sync, restarts storage, commits through the
same standby object, and advances the same peer/store/tree/indexer instances;
the old `Arc` database and snapshot retain their original basis. It passed 1/1
in 1.29 seconds. A reserved unreachable-host witness passed in 0.05 seconds
with a 50 ms connection cap. The final fresh-catalog library gate ran with
`ATOMIC_TEST_POSTGRES_URL` explicitly set and passed 135/135 nonignored tests
(one subprocess helper is intentionally ignored), including private-publication
faults, actual process death, and every populated migration witness.

Closing the lifecycle work exposed two additional migration defects rather
than hiding them behind serial tests. A populated v11 fixture had incorrectly
called current v14 creation SQL; v6/v11 fixtures now construct their authentic
generation-zero catalog representation directly. Migration 13 legitimately
fills the new lineage column, so the administrative migrator suspends only the
v1 database-immutability trigger around that data migration and re-enables it
in the same transaction without changing the checksummed historical SQL.
Repeated `migrate()` calls also used to rebuild complete tree/program ledgers
and acquire locks opposite live writers. Healthy current catalogs now take a
read-only discovery fast path, exceptional repair follows generation-owner,
membership, then legacy-log lock order, and routine search paths are altered
only when mismatched. A live regression holds the ordinary writer relation
locks with a 500 ms timeout while an already-current migrator completes; fresh,
pre-v6, populated-v6, populated-v11, zero-state repair, and current-idempotence
witnesses all pass.

Role provisioning safely quotes identifiers and requires dedicated roles: it
rejects elevation or membership, non-system relation/schema or database
ownership, effective database/schema CREATE outside the resettable Atomic
schema grant, residual column grants, and PUBLIC CREATE or relation/column
authority. It clears direct table grants from every discovered `atomic_*`
ordinary/partitioned relation—including administrative and future relations—
before applying a fixed positive whitelist of only the reads, fenced log
writes, lease updates, and immutable-tree inserts actually used. A live
catalog-matrix witness checks all table privileges and every column-capable
privilege for every `atomic_*` relation, rather than inferring least privilege
from a few denied statements.
The writer's UPDATE privilege on `atomic_databases` exists solely because
PostgreSQL requires it for the publication serialization row lock; the
immutable trigger is independently proven to reject mutation. Verified
PostgreSQL TLS is a deliberate native strengthening: the SQL-specific docs
show configurable driver parameters, including a non-validating example
(`01_transactor_reference.md:19-26`), while Atomic forces verified TCP TLS even
if parameters request disable, supports system and explicit PEM trust roots,
redacts connection failures, and offers no non-validating mode. The recorded
live PostgreSQL 15.11 run executed both migration/role tests and both TLS tests;
the isolated, panic-cleaned future-row fixture proved migrator, service, peer,
standalone indexer, and standalone tree writer all fail with
`postgres/schema-too-new`; the restricted-role fixture committed, published a
tree, served a peer, and released its lease while DDL/history/peer writes were
denied. PostgreSQL 15.11 reruns executed 2/2 migration-boundary and 1/1
adversarial-ACL tests over both keyword and URI connection syntax, with no
generated roles or schemas left behind.

### 2. Coherent inspection and database-scoped failure

**Status:** Complete on PostgreSQL 15.11. Two focused live witnesses pass,
including a deliberately blocked mid-report inspection racing a committed
successor/root and cross-database native-manifest corruption during inspection,
GC inventory, and excision. The existing integrity suite also passes with
native-tree metrics, and all-target check plus warning-denying Clippy are green.

**Outcome:** Integrity and capacity reports describe one repeatable database
snapshot and isolate corrupt derived state to its owning database.

**Focus:** Transaction/locking boundary; authoritative-versus-derived checks;
root revision and reachability accounting; actionable scoped diagnostics;
conservative treatment of undecodable shared roots.

**Completion signal:** Concurrent commit/consolidation cannot create a torn
inspection report; corruption in database A is reported for A without aborting
inspection or logical maintenance for B, and no uncertain shared content is
declared collectible.

**Evidence and decisions:** `inspect_database` now performs its head read,
authoritative chain checks, legacy/native root inspection, metrics, temporal
program scan, and exact `recover_to` under one read-only Repeatable Read SQL
transaction. It no longer opens a second store connection for semantic
recovery. Only published derived roots are eligible: uploaded-but-unpublished
manifests remain harmless content-first orphans. Native inspection authenticates
the monotonic physical publication revision, canonical v4 envelope, relational
eight-root projection, authoritative transaction/state commitment, current
excision generation for eligibility, and recursively reachable root/directory/
leaf nodes; deep mode additionally validates complete tree topology and ranges.
Legacy and native counts are separate so the old flat representation cannot
silently stand in for the production tree representation.

Global orphan accounting first proves complete reachability from every retained
legacy and native publication, including canonical envelopes, authoritative
coordinates, immutable payload hashes, segment descriptors, root bindings, and
every referenced object. An undecodable/missing value in database A therefore
sets B's `shared_reachability_uncertain` and withholds orphan counts without
making B unhealthy; GC dry-run/apply returns
`operations/reachability-uncertain`. Excision no longer runs opportunistic
global segment cleanup at all, keeping its logical database rewrite independent
of unrelated shared derived state. This follows the docs' log-versus-immutable-
persistent-index split (`00_introduction.md:79-109`) and background durable tree
model (`02_background_indexing.md:13-25`). Recovered 1.0.7705 writes content
before conditionally publishing the root and marks old values only after the
root CAS succeeds (`index.clj:6327`, `6427-6448`); its garbage path records
exact newly unreachable value keys and applies an explicit age boundary
(`garbage.clj:318-358`, `475-603`). PostgreSQL Repeatable Read and conservative
global refusal are the direct native equivalents, not claims of byte/storage
compatibility.

Live evidence: `operations_inspection_scope` 2/2 proves the report remains at
the old basis, transaction/request counts, recovered current/history values,
and tree revision while a successor commits and consolidates before the
blocked report finishes. Its second witness corrupts A's published v4 payload,
observes a healthy B report with zero guessed orphan counts, an unhealthy A
report, exact GC refusal, successful B excision, and preservation of an
unrelated orphan. `operations_integrity` 2/2 passes after replacing its obsolete
flat-segment assertion with native publication/node evidence. The older shared
GC fixture database intentionally contains a retained corrupt root from prior
fault tests, and now fails closed as designed; Stage 4 owns isolated complete
root-retirement/GC evidence rather than weakening this boundary.

### 3. Differential root-last backup and exact-point restore

**Status:** Operationally complete on PostgreSQL 15.11. Backup unit tests pass
10/10 and the normal-parallel live backup/restore suite passes 9/9, including
deterministic zero-age GC handoff, injected publication/restore faults,
ambiguous retry,
same-basis generations, corrupt/missing content, temporal programs, and
derived-tree fallback. This does not claim the still-unproved semantic
cross-check of every restored/request-archive tree described below.

**Outcome:** Backups capture a stable database identity and authoritative point,
reuse already-copied immutable content, publish roots last and crash-safely,
and restore either the copied physical projection or a source-admissible
rebuild at that exact log/current coordinate.

**Focus:** Coherent capture; database identity/lineage; differential object
reuse; temporary-file/rename/directory durability; shallow/deep verification;
point selection; restore postconditions and retry/fault behavior.

**Completion signal:** Live incremental backups reuse content; interruption at
each publication boundary leaves no visible partial backup; retry converges;
deep verification detects missing/corrupt reachability; restored log, schema,
idents, functions, basis, and commitments are exactly equal to the source
point. Copied roots remain byte-identical; rebuilt `noHistory` projections need
only be admissible because retained-history removal and timing are explicitly
nonsemantic (`03_schema/00_schema_data_reference.md:201-213`).

**Evidence and decisions:** `PortableBackup` captures one Repeatable Read point
while holding an exact active-generation pin. Its canonical constant-size v4
root binds lineage, generation, basis, genesis, linked transactions/content,
requests, state commitments, completed excision, temporal-program closure, and
optional authenticated tree commitments. A private per-lineage filesystem
repository serializes writers, reuses immutable objects, publishes a
no-clobber/fsynced root last, and removes only exact regular temporary files
without following symlinks. Exact `(generation, basis)` APIs distinguish the
pre/post-excision points that a basis-only API cannot. Shallow verification
proves complete reachability; the implemented deep mode also hashes and
decodes its portable authoritative rows and tree topology. It does **not yet**
establish semantic equality of every current/history sibling or every
request-base archive against the log. Goal 20 owns that non-skipping proof,
including the admissible (not uniquely exact) `noHistory` history rule.

Restore deep-verifies before mutation, stages an invisible database-local
generation, owns ATLC content and program references atomically with its
membership, seals the exact completion set, and hands its builder pin directly
to the activation lock before expected-source publication. Retry resumes only
the exact unclaimed build or recognizes its committed point. Superseded,
permanently claimed, unrelated excision, and failed first-restore generations
cannot be accidentally adopted and are age-gated, boundedly reclaimable.

This preserves the documented live/consistent, per-database, differential,
selectable-point, same-lineage restore, and shallow/deep behavior
(`08_operations/01_capacity_and_reliability/02_backup_and_restore.md:18-40,50-118`;
`01_high_availability.md:82-95`). The recovered claim, immutable-value copy,
stable capture, values-before-root, restore, and `read-all` split are in
`backup.clj:264-301,1070-1208,1348-1420,1572-1631,1890-1943`, with filesystem
publication in `fsbackup.clj:105-179`. ATBK/ATLC, fsync details, and
database-local generation rebinding are deliberate native formats. Deliberate
limits are a local filesystem repository, no same-catalog lineage clone,
writer-quiesced restore, memory-proportional deep verification, and optional
replaceable tree accelerators rather than tree authority. Backups,
request-base archives, retired roots, live snapshot pins, WAL/replicas, and
exports can remain retention anchors after the active projection changes;
Goal 20 must make those anchors visible in the final operator disclosure and
verify request-archive semantics rather than treating reachability as semantic
proof.

### 4. Safe retirement of superseded physical roots

**Status:** Complete on a fresh PostgreSQL 15.11 catalog. The live
`operations_gc` suite passes 13/13, covering exact dry/apply, pins and grace,
same-basis/successor publication, shared content, bounded paging, failed
initial restore cleanup/retry, concurrent consolidation, restart-safe phases,
and age-policy changes after a durable claim.

**Outcome:** Normal consolidation growth is reclaimable after a declared grace
boundary while every current, retained, backup-bound, or live-snapshot root
remains readable.

**Focus:** Root/node reachability; publication revision history; peer pins;
active backup/excision snapshots; grace age; dry-run/apply agreement; concurrent
publication; conservative failure on uncertain reachability.

**Completion signal:** Repeated same-basis and successor publications create
measurable reclaimable content; dry-run exactly predicts apply; pinned/young/
reachable content survives; aged unreachable content is removed; concurrent
consolidation and restart never lose a published value.

**Evidence and decisions:** PostgreSQL now retains exact publication-intent and
delta ledgers, current/retired root sets, build/retirement/abandonment cursors,
and exact old-minus-new node marks. Root publication remains content-first and
root-last. Each collector call performs bounded, restartable work under root,
generation, peer, builder, or backup advisory pins; raw deletion is driven by
durable provenance, never an orphan guess. A PostgreSQL restart necessarily
drops session pins, so the declared age horizon covers the interval until a
peer reborrows and reacquires them. Claimed work remains collectible if an
operator later lengthens the age horizon, and a failed unlock discards the
uncertain session rather than leaking an uncounted permanent pin.

The safety boundary follows the documented delayed collection of garbage made
by indexing (`00_capacity_planning.md:275-286`) and the recovered publish-then-
mark and explicit `:older-than` collector (`index.clj:3940-3985,6327,6433-6448`;
`garbage.clj:318-390,475-603`; `tools/gc_db.clj`; peer `api.clj`'s
`gc-storage`). PostgreSQL ledgers and advisory coordinates are native
mechanisms. Remaining debts are throughput/tuning (one globally selected
root, intent, or generation per call), conservative retention of flat-index
and pre-ledger content pending an authenticated backfill, age protection for
disconnected readers, and deployment-owned `VACUUM`; the native versioned-tree
path itself has exact liveness evidence.

### 5. Source-faithful excision and synchronization

**Status:** Complete in the pure semantic suite and in one end-to-end live
PostgreSQL fault/restart witness. The broad A=15 test passes 1/1 in 4.04s on a
fresh catalog; focused predicate, request/replay, and COW tests pass 18/18.

**Outcome:** Excision selects exactly the documented entity/attribute/time
extent, preserves protected facts by identity, records an immutable audit
predicate, rewrites every affected derived value, and gives peers a precise
synchronization boundary.

**Focus:** Recovered keeper predicate; entity 42 and boot identities;
component and inbound-reference closure; cutoff semantics; precise limits and
the documented backup recommendation without an authorization gate;
audit/query visibility; cache/root invalidation; restart and peer
adoption; WAL/external-backup disclosure.

**Completion signal:** Independent fixtures prove exact retained/removed
datoms for entity, attribute, cutoff, component, and inbound-ref cases;
protected identities cannot be removed; failure is atomic; old peer snapshots
have an explicit validity boundary; acknowledged excision survives restart and
cannot be bypassed by unrelated corrupt derived state.

**Evidence and decisions:** A=15 remains ordinary immutable transaction data.
Atomic freezes the request's optional fields at its A=15 assertion rather than
letting later edits change queued work; this deliberately strengthens the
documented one-transaction request shape, while recovered Datomic materializes
the current entity at indexing. The operator rewrites an
inactive copy-on-write log generation through durable bounded checkpoints,
catches up transactions committed during the build, stages the corresponding
tree, conditionally activates the generation, and records root-last completion
for `sync_excise`. Faults after capture, candidate staging, and activation
prove that no partial generation is visible and retries resume instead of
leaking candidates. Unaffected ATLC content is shared, the final receipt binds
the caught-up source hash, restart recovers the exact rewritten state, old
immutable peer values remain honest old branches, and refreshed current and
history indexes no longer contain the selected secret while the A=15 audit fact
remains queryable.

The selection plan uses the recovered datom-local `keeper?` identity set,
strict cutoff, selected-attribute behavior, recursive component extent, and
inbound references from `excise.clj:39-90,139-266,331-428`; it does not revive
the incorrect `<1000` heuristic. In particular, component discovery now reads
raw history-as-of, so a pre-request retracted ownership edge still carries its
child history into the privacy extent, and a protected partition-zero target
keeps its own facts without falsely protecting unprivileged application facts
that point to it. This matches the ordinary asynchronous request, protected
data, permanent predicate, background effect, backup recommendation, and
synchronization contract in
`09_optional/02_specialized_operations/02_excision.md:10-15,33-58,103-129`.
Atomic's copy-on-write SQL generations strengthen atomic visibility without
turning backup into authorization. The live witness also begins with a
consolidated `noHistory` retract/reassert pair and proves the active rewritten
log plus all new current/history tree orders contain no selected value. That is
a tested native strengthening of Datomic's `noHistory` warning, not a claim
about old physical copies. PostgreSQL WAL, replicas, exports, logs, retired
generations before GC, old peer values, and existing backups remain outside
logical excision and must have matching retention; fulltext is outside Atomic's
type/index surface.

### 6. Operational closure

**Status:** Complete 2026-09-03. Fresh real-PostgreSQL evidence and the current
operator contract establish every Goal 15 stage; formatting, `git diff
--check`, and warning-denying all-target Clippy pass.

**Outcome:** The repaired lifecycle surface is reproducible, observable, and
ready for Goal 16/17 integration without overstated guarantees.

**Focus:** Non-skipping PostgreSQL harness; migration/security/backup/restore/
inspection/GC/excision fault matrix; bounded-growth evidence; operator-facing
contracts; source/deviation ledger and parent-plan fold-back.

**Completion signal:** Format and warning-denying Clippy pass; focused pure and
live-PostgreSQL suites prove every stage and relevant restart/fault boundary;
the runbook states exact recovery/privacy/security limits; Goal 9 and Goal 0
truthfully reflect the established evidence and remaining Goal 16 work.

**Evidence and decisions:** `goal-15/OPERATIONS.md` supersedes the prototype
Goal 8 runbook where they differ and states the exact authority, TLS, backup,
restore, GC, excision, WAL, disconnected-reader, and native-format boundaries.
The final explicitly enabled library run passed 135/135 nonignored tests with
one intentional subprocess helper ignored. Focused fresh-catalog evidence is
backup/restore 9/9, GC 13/13, inspection scope 2/2, integrity 2/2, excision
1/1, and runtime ACL/search-path 1/1, in addition to migration, TLS, restart,
and fault witnesses recorded above. Goal 16 owns removal of the eager writer,
while Goal 20 owns the reproducible cross-goal deployment gate plus the
semantic backup/request-archive and retention-anchor checks; none is
misreported as Goal 15 lifecycle work.

## Exit condition

Goal 15 completes only when PostgreSQL can be provisioned and operated with
separate authority, secure/version-checked runtime access, coherent and scoped
diagnostics, crash-safe differential backup/exact-point restore, safe bounded
physical reclamation, and source-faithful auditable excision, all demonstrated
by non-skipping real-PostgreSQL evidence.

**Status:** Achieved 2026-09-03 with the evidence above. The corrective parent
continues at Goal 16.
