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
with a 50 ms connection cap. A second brand-new cluster run passed all 97
nonignored unit tests (including the four migration witnesses) plus background
indexing, backup/restore, database-value, identity, incremental-tree, and
kernel suites before stopping at the already-owned Stage 5 obsolete
legacy-manifest assertion in `operations_excision`; this is partial integrated
evidence, not a claim that the full Goal 15 suite is green.

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

### 3. Differential root-last backup and exact restore

**Status:** Pending.

**Outcome:** Backups capture a stable database identity and point, reuse
already-copied immutable content, publish roots last and crash-safely, and
restore the exact requested database value.

**Focus:** Coherent capture; database identity/lineage; differential object
reuse; temporary-file/rename/directory durability; shallow/deep verification;
point selection; restore postconditions and retry/fault behavior.

**Completion signal:** Live incremental backups reuse content; interruption at
each publication boundary leaves no visible partial backup; retry converges;
deep verification detects missing/corrupt reachability; restored log, schema,
idents, functions, current/history indexes, basis, and commitments are exactly
equal to the source point.

### 4. Safe retirement of superseded physical roots

**Status:** Pending.

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

### 5. Source-faithful excision and synchronization

**Status:** Pending.

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

### 6. Operational closure

**Status:** Pending.

**Outcome:** The repaired lifecycle surface is reproducible, observable, and
ready for Goal 16/17 integration without overstated guarantees.

**Focus:** Non-skipping PostgreSQL harness; migration/security/backup/restore/
inspection/GC/excision fault matrix; bounded-growth evidence; operator-facing
contracts; source/deviation ledger and parent-plan fold-back.

**Completion signal:** Format and warning-denying Clippy pass; focused pure and
live-PostgreSQL suites prove every stage and relevant restart/fault boundary;
the runbook states exact recovery/privacy/security limits; Goal 9 and Goal 0
truthfully reflect the established evidence and remaining Goal 16 work.

## Exit condition

Goal 15 completes only when PostgreSQL can be provisioned and operated with
separate authority, secure/version-checked runtime access, coherent and scoped
diagnostics, crash-safe differential backup/exact restore, safe bounded
physical reclamation, and source-faithful auditable excision, all demonstrated
by non-skipping real-PostgreSQL evidence.
