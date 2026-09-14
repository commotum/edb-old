# Goal 6 — Safe logical database lifecycle

## Objective and ownership

Users can idempotently create, list, rename, retire/delete and eventually reclaim
logical databases through supported Rust APIs and CLI. This is Stage 5 of
[Goal 0](../goal-0/0-plan.md), owning P02.

**Status: complete (2026-09-10).** All three internal stages and the parent-stage
signal are verified. Return to Goal 0 / Goal 7; reopen this child for an integrated
lifecycle gap, not for unrelated service work.

## Constraints and source-backed contract

Local Datomic Pro docs govern semantics; recovered 1.0.7705 supplies architectural
evidence. Native equivalence means useful behavior, not JVM machinery. Preserve
immutable values, identities/schema, canonical bytes, old program encodings and
receipt-first exact retries. No replacement engine, alternate store or nested goal.

The Peer API documents created/existed and rename/delete outcomes. Capacity
planning separates immediate deletion/unavailability from later reclamation.
Recovered catalog.clj moves a public name while retaining db-id and records
deleted identities separately.

- Active names map to immutable storage ID + lineage. Existing names seed
  one-to-one; no canonical facts, EIDs, manifests or receipts are rewritten.
  Rename changes only the route; no implicit historical aliases. Reused names get
  fresh storage IDs and lineages. Public ingress resolves once; captured IDs are
  never fed back into a mutable-name constructor.
- Strict create remains compatible. Additive create-if-absent avoids replaying an
  existing database or evaluating an irrelevant new schema. Listings are keyset
  paginated. Genuine prior binaries support long names: no invented 1024-byte cap.
  Headless restore reservations remain visible/idempotent/retirable, not readable.
- Retirement atomically removes the name and fences the writer. New named and
  exact-reference opens reject retired identities. Healthy pinned immutable
  values already handed to applications remain readable; pins are not permanent
  offline availability guarantees.
- Reclamation is separately owner-authorized against exact storage ID, lineage,
  physical PostgreSQL database, catalog schema and retention age. CLI mutations
  preview by default; rename/delete apply compares the expected lineage under the
  same coordination as name mutation. Runtime roles receive no destructive power.
- Completed portable backups are independent. Restore of a retired lineage into
  the same catalog requires finishing reclamation or choosing another catalog.
  After reclamation, restore uses a fresh storage route with the backup lineage;
  old exact routes remain retired.

## Internal stages and results

### 1. Reconcile behavior and contract — complete

Confirmed name versus canonical identity, headless restoration, active writers,
retained readers, backups and shared storage boundaries against original docs/code.
No ad hoc user catalog SQL is required.

### 2. Implement native capability — complete

Additive DatabaseCatalog APIs, stable-ID ingress, lease/publication fences,
identity-checked CLI, backup integration and versioned resumable terminal collection
are implemented. Migration34 is an explicit quiesced upgrade; no universal rolling
compatibility is claimed.

Terminal collection uses authenticated frontier discovery and dependency-ordered
metadata removal, not a large cascading header deletion or fake successor generation.
Interrupted build intents may legally name unuploaded nodes; only actual stored
nodes seed the physical frontier. Shared native/fulltext/program content survives.

Safety findings repaired during integration:

- Mixed-source EDN and application snapshot handoff now resolve names and compare
  ID + lineage, preventing the old-name-equals-storage-ID trap after name reuse.
- Publication locks issued identity FOR SHARE, including headless restore and
  Repeatable Read cases. Independent index publication must not additionally lock
  the head: the concurrent regression reproduced SQL40P01 from canonical-row/head
  lock inversion. The corrected guard and deterministic trigger-order witness pass.
- Global semantic/fulltext collector fences are taken only for physical deletion,
  not unrelated discovery/metadata phases. Target pins remain throughout.
- Native-node collection locks the candidate and repeats liveness/reference checks
  at DELETE time; newly shared nodes survive and incomplete proof retains progress.
- Terminal fulltext side effects avoid duplicate unbounded enqueue work. Ordinary
  GC's original behavior remains intact. Historical test rollback restores all13
  affected pre34 guard/side-effect bodies exactly.

### 3. Exercise and hand off — complete

Final configured verification on frozen source:

| Target | Result |
| --- | --- |
| database_catalog | 4/4 actual PostgreSQL; 7.70s |
| lifecycle_handles | 2/2 actual PostgreSQL; 8.14s |
| postgres_runtime_roles | 1/1 actual PostgreSQL; 2.46s |
| database_reclamation | 5/5 actual PostgreSQL; 133.81s |
| program_reference_upgrade | 10 active pass; 66.47s; one fixture generator intentionally ignored |
| query_reference_walker_upgrade | 2/2 pass; 4.56s |
| database_lifecycle_cli | 5/5; 11.42s; genuine old executable supplied |

Total29 active passes, zero failures/skips; 26 exercise actual isolated PostgreSQL
and three are pure/artifact checks. The historical targets include old ABI5/ABI7,
unchanged canonical fingerprints, schema30 receipt before10/after11 through restart,
and real23→34/31→34 remigration. CLI covers restricted roles, preview/target checks,
live rename/name reuse/mixed EDN sources, exact retries, final reclamation and the
evolving application/reference handoff. Builds and all-target check pass; Clippy
retains15 pre-existing warnings, none introduced by the collector.

Measured complete paths (samples, not throughput certification):

- Existing-create/check/drop: 3.36ms.
- Application rename/reference/retry handoff: 2015ms; stock live lifecycle workflow
  2543ms; explicit empty-database CLI reclamation/check 1094ms.
- Pinned-value/backup release→reclamation: 536 batches, 652 removals, 253 insertions,
  250 updates/decoded objects, 2645 pin probes, 27.95s including reconnect/check work.
- Shared first/last retirement: 656 batches each, 446/772 removals, 313 insertions,
  310 updates/decoded objects, 3245 pin probes; 28.28s/38.65s.
- 600 receipt-only IDs plus600 unuploaded intent rows cross the actual512-row
  mutation boundary; completion472 batches/20.74s.
- Concurrent normal writer:16 successful commits plus background convergence while
 265 collection batches removed10 exclusive semantic pages, no background failure.
  Ordinary catalog GC still progresses after terminal completion.

Each batch touches at most512 data/frontier rows plus fixed progress bookkeeping.
This is not a bound on all metadata scanned, wall time or RSS. Legacy segment
sharing can inspect all remaining manifests one payload at a time. Completion
covers attributable target metadata and exclusive reachable objects; already
detached global orphans belong to ordinary catalog GC. No claim erases external
backups, WAL, replicas or PostgreSQL backups. These are documented operating
boundaries, not hidden missing lifecycle work.

## Compatibility artifacts and continuation

Genuine pre-lifecycle schema33 executable:
`/tmp/atomic-pre-lifecycle.L5YhZH/atomic`
SHA256 `8139b156c4219997a7e9ffbdc3a0660bed65aaaa0e393c284a9780d8049955dd`.
It created the actual upgrade fixture; old startup fencing, unchanged genesis,
long names, rename and exact receipt checks pass.

Accepted final schema34 executable retained before Goal7 changes:
`/tmp/atomic-pre-services.vJBL8z/atomic`
SHA256 `ac98c9c660dbdcb4b6b1d1226735e6e4b98053aba0ff5cf137b1a8867091a7d3`.
Final reclamation artifact SHA256
`1d26b61ca5d70eb7d24bf998154fdba9f87f42026d2f458563f8dd9e4340fa86`;
log `/tmp/atomic-goal6-reclamation-final.jRJA1p.log`.

Tests use configured isolated schemas/roles, never broad shared-catalog migration.
Release commands use CARGO_PROFILE_RELEASE_DEBUG=0, CARGO_INCREMENTAL=0,
cargo --offline --release -j4; build atomic and application_workflow before CLI tests.
Supply ATOMIC_POSTGRES_URL and ATOMIC_PRE_LIFECYCLE_BIN for genuine acceptance.

Next: Goal7 service/client integration. Keep this lifecycle identity and retirement
contract intact; preserve the schema34 artifact for the next genuine upgrade test.
