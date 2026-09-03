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

**Status:** Complete on PostgreSQL 15.11. The explicit live harness passed two
migration/role tests and two TLS tests; the pure migration classifier passed
three cases, existing idempotent migration passed, and warning-denying Clippy
is green.

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

**Evidence and decisions:** The docs make SQL provisioning and schema upgrades
explicit (`00_storage_services.md:107-131`, `02_datomic_deployment.md:235-258`)
and describe TLS/trust inputs (`01_transactor_reference.md:17-27,81-132`). The
recovered `kv_sql_ext.clj:113-170` builds one validated configured data source,
while `sql.clj` performs ordinary CRUD/CAS. Native code therefore uses the
concrete `PostgresMigrator` plus `PostgresConnectionConfig`, not a backend
trait. Runtime checks require the complete checksummed migration prefix and
reject an unknown future version before any database read or publication.
Role provisioning safely quotes identifiers, rejects elevated/inherited/
owning roles and insecure PUBLIC schema creation, and grants only the reads,
fenced log writes, lease updates, and immutable-tree inserts actually used.
The writer's UPDATE privilege on `atomic_databases` exists solely because
PostgreSQL requires it for the publication serialization row lock; the
immutable trigger is independently proven to reject mutation. Required TLS
forces verified TCP TLS even if parameters request disable, supports system
and explicit PEM trust roots, redacts connection failures, and deliberately
offers no non-validating mode. The live future-row fixture proved migrator,
service, peer, standalone indexer, and standalone tree writer all fail with
`postgres/schema-too-new`; the restricted-role fixture committed, published a
tree, served a peer, and released its lease while DDL/history/peer writes were
denied.

### 2. Coherent inspection and database-scoped failure

**Status:** Pending.

**Outcome:** Integrity and capacity reports describe one repeatable database
snapshot and isolate corrupt derived state to its owning database.

**Focus:** Transaction/locking boundary; authoritative-versus-derived checks;
root revision and reachability accounting; actionable scoped diagnostics;
conservative treatment of undecodable shared roots.

**Completion signal:** Concurrent commit/consolidation cannot create a torn
inspection report; corruption in database A is reported for A without aborting
inspection or logical maintenance for B, and no uncertain shared content is
declared collectible.

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
backup/excision pins; grace age; dry-run/apply agreement; concurrent
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
component and inbound-reference closure; cutoff semantics; limits and backup
gate; audit/query visibility; cache/root invalidation; restart and peer
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
