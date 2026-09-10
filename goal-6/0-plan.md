# Goal 6 — Safe logical database lifecycle

## Objective and ownership

Users can idempotently create, list, rename, retire/delete and eventually reclaim logical databases through supported APIs and CLI.

This is Stage 5 of [Goal 0](../goal-0/0-plan.md), owning P02.
Status: active; internal Stage 1 reconciliation. Preserve completed EDN and all other child work.
The historical audit is [goal-0/1-audit.md](../goal-0/1-audit.md).

## Constraints

Use local Datomic Pro documentation as semantic authority and recovered 1.0.7705
as architectural evidence. Equivalence is in spirit and observable usefulness;
prefer native Rust APIs and reuse existing mechanisms. Preserve immutable values,
identity/schema, exact values, durable bytes, old program encodings/receipts and
receipt-first retries. No JVM evaluation, replacement engine, alternate durable
store or recursive goals. Parent decisions govern optional scope.

Use PostgreSQL coordination and stable database lineage, not ad hoc catalog edits. Define active writers/readers, exact references, retention, backups, rename aliases/name reuse and reclamation separately. Make destructive targets explicit and observable.

## Internal stages

### 1. Reconcile behavior and contract

**Outcome:** A precise public user workflow and evidence-backed implementation boundary.
**Focus:** Inspect the owning audit findings, relevant docs, current code and tests.
Challenge candidates with a minimal reproduction; distinguish an existing native
equivalent from a missing integration. Record consequential decisions here.
**Completion signal:** Required behavior, native design, compatibility risks and
permanent regression witnesses are clear enough to implement without scope guessing.

### 2. Implement the native capability

**Outcome:** Users can idempotently create, list, rename, retire/delete and eventually reclaim logical databases through supported APIs and CLI.
**Focus:** Use PostgreSQL coordination and stable database lineage, not ad hoc catalog edits. Define active writers/readers, exact references, retention, backups, rename aliases/name reuse and reclamation separately. Make destructive targets explicit and observable.
**Completion signal:** Public interfaces and permanent regressions demonstrate the
required behavior, including failure/limit cases, without breaking existing callers
or silently weakening the objective.

### 3. Exercise and hand off

**Outcome:** A usable, documented capability with verified compatibility.
**Focus:** Evolve application examples and EDN paths where relevant; use actual
PostgreSQL and ordinary service/peer boundaries. Measure complete costs and verify
old durable meaning/retries. Reopen implementation for integration gaps.
**Completion signal:** Isolated real PostgreSQL tests exercise concurrent create, identity-preserving rename, active-service fencing, delete versus reclamation, old reference behavior, backup protection and name reuse. Ordinary users need no hand-written catalog SQL; failures do not partially rename/delete or reuse identity.

## Completion and continuation

This child finishes only when all three outcomes and its parent-stage signal hold.
Record commands/results, material design decisions, remaining gaps and a concise
next action here, then return to Goal 0. A scaffold or pure helper is not completion.
Current next action: reconcile stable storage identity versus public names, writer
fencing, retained readers/backups and bounded reclamation; then implement additive
public lifecycle and CLI paths. Goals 1–5 are complete; this is the sole active child.

## Reconciled native contract (2026-09-10)

The local Peer API documents created/existed and rename/delete outcomes. Capacity
planning distinguishes immediate deletion/unavailability from later storage
reclamation. Recovered catalog.clj moves the name mapping while preserving db-id
and records deleted identities separately. Atomic's immutable manifest/envelope
bytes already contain its database_id, so changing that ID in place is unsound.

Add an active-name catalog separate from durable storage identity/lineage. Seed
existing names one-to-one without rewriting data. Rename moves a name only;
there are no implicit historical aliases. A reused name gets a fresh storage ID
and lineage. Public named opens resolve once; held handles and exact references
retain stable identity and never resolve through a reused name. Existing strict
create API remains compatible; additive create-if-absent returns created/existed
without loading the whole existing database. Listings are paginated.

Retire removes the active name and fences the writer atomically. No new named
connection or exact snapshot reopening may acquire a retired database. Native
captured, pinned immutable values remain readable: retirement does not mutate a
value already handed to an application. Reclamation waits for those pins and
ongoing backups, then proceeds in bounded resumable steps against an explicit
storage-ID/lineage target. It is distinct from deleting historical facts/excision
and must protect unrelated databases and shared content/programs. Completed
portable backups remain independent; restore behavior must not accidentally
resurrect a retired identity or redirect an old handle.

Owner-authorized rename/delete/reclaim CLI commands preview by default and
require --apply to mutate. Names are user-facing labels; durable IDs and lineage
are printed where needed for exact destructive targets. Runtime peers/writers
must not acquire catalog/destructive privileges. Migration34 provides the upgrade
boundary; this is a quiesced old-writer upgrade, not unproved rolling compatibility.

The genuine final schema33 executable was retained before implementation:
`/tmp/atomic-pre-lifecycle.L5YhZH/atomic`, SHA256
`8139b156c4219997a7e9ffbdc3a0660bed65aaaa0e393c284a9780d8049955dd`.
Old-binary creation/receipt/rename/retry verification passed the interim build
below; final reclamation-migration acceptance is still required.

## Implementation and interim verification

The catalog API, public name/stable-ID ingress split, lease/publication retirement
fences and preview/identity-checked CLI are implemented. Existing strict create
is preserved; create-if-absent, paginated lists and checked mutations are additive.
The initial proposed 1024-byte name cap was removed: the genuine prior binary can
create a 1200-byte name, and migration/resolution must preserve such existing names.

A real CLI integration exposed name-versus-ID confusion in additional query
source descriptors: after renaming customers to people and creating new customers,
the old storage ID still spells customers. The frontend now resolves explicit
source names and compares both ID and lineage; it fails closed if identity changes
during open. The Alice/Bob mixed-source regression passed.

Interim actual PostgreSQL (isolated schemas, not shared migrations): catalog 2/2
passed, including six concurrent creators and an existing-create path that avoids
invalid new-schema evaluation; sampled complete existing-create 4.24ms debug.
Handle/standby/reference tests 2/2 passed in 19.61s, including genuine cold leaf I/O
from captured values after retirement. Genuine schema33 executable upgrade passed
1/1 in 3.49s: old long name, unchanged genesis, old-startup fence, rename and exact
receipt. Separate stock CLI workflow passed 1/1 in 10.97s (measured workflow 5369ms),
including restricted-role rejection, live rename, name reuse, mixed sources and
writer restart. These runs used an unfinished reclamation migration fragment;
they are NOT final schema34/migration-checksum acceptance. Rerun final source.

Continuation: bounded terminal reclamation and restore integration are in progress.
Permanent reclamation, final migration/legacy-fixture and complete application
checks remain required before Goal 6 completes. Do not activate Goal 7 yet.
