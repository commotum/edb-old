# Atomic implementation trace — storage provider

Development companion to [Storage Services](00_storage_services.md), not a
replacement for its text. Named symbols locate code at unannotated revision
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; comments may move current lines.
This slice covers the provider boundary and SQL setup. Other provider chapters
and engine-level retention/publication still require their own dispositions.

## “Storage Services” — fully API-compatible providers

The promise is separation of database behavior from storage implementation, not
that every application must implement every listed service.

- [KVStore](../../../1.0.7705/transactor/src-clj/datomic/kv_store.clj), protocol
  `put/get/delete/close`, distinguishes byte-buffer values from revisioned refs.
  It does not parse datoms, query expressions, log trees or receipts.
- [KVCluster](../../../1.0.7705/transactor/src-clj/datomic/kv_cluster.clj),
  `create-val`, `set-ref`, `get-ref` are consumers of that provider seam.
  `set-ref` resolves a failed conditional put by checking whether the intended
  revision/value was already installed; it does not reassess a transaction.
- Atomic's [ObjectReader/ObjectWriter](../../../src/storage/object_io.rs) own
  required immutable-object access. `PgBlockStore` supplies opaque byte storage,
  single/multiple reference CAS and bounded enumeration/removal. Rust engine
  modules, not PostgreSQL functions, interpret the database structures.

**Disposition:** retain the opaque boundary and PostgreSQL-only product scope.
The current provider's guarded batches and protection epochs are more involved
than `KVStore`; calling them generic does not prove equivalent complexity.
They currently enforce Atomic's concurrent publication/collector contract and
need evaluation with their actual engine consumers. Do not delete these barriers
solely to match a protocol method count, or imply Datomic uses the same scheme.

## “SQL Database” — table, users, connection and driver

[KVSql](../../../1.0.7705/transactor/src-clj/datomic/kv_sql.clj), `get` and `put`,
maps opaque values and metadata into SQL columns. `:ensure/:rev` becomes a
revision predicate; creation uses a unique insert. A constraint violation is a
conditional miss, not a successful overwrite. SQL does not validate entity
identity, schema or index reachability.

Atomic's [schema](../../../src/storage/schema.sql) separates immutable objects
from references into two tables. `PgBlockStore::install` uses an explicitly
selected namespace; `connect` does no runtime installation.
`compare_exchange_many` locks guard keys in deterministic order (including
absent keys), compares revisions, and commits opaque changes atomically.
Content-addressed `put` authenticates canonical content and never replaces the
stored payload. `protected_epoch` is caller-selected storage metadata, not an
interpretation of that payload.

**Disposition:** retain two typed physical tables; one-versus-two tables is not
the architectural test. Preserve empty-namespace admission, current-version
reopen and explicit role grants. No old schema/format conversion is required.
The PostgreSQL driver is native Rust; JDBC/classpath instructions are not native
features to recreate. Provider connection settings and TLS remain explicit.

Independent checks: [block_storage](../../../tests/block_storage.rs) verifies
opaque arbitrary bytes, concurrent creation/replacement, tombstone ABA,
all-or-nothing guarded batches, and reopen. Its fresh-install case inspects the
actual PostgreSQL catalog for no routines or trigger state machines.
[block_storage_isolation](../../../tests/block_storage_isolation.rs) covers
namespace isolation. Configured PostgreSQL is required; early returns are skips.

## “Validation Query” — evidence discrepancy

The reference prose lists `select 1 from dual` as the default and `select 1`
for Oracle. Recovered [kv-sql-ext](../../../1.0.7705/transactor/src-clj/datomic/kv_sql_ext.clj),
`validation-query*`, shows the reverse: `:default` uses `select 1`; `"oracle"`
uses `select 1 from dual`. `validation-query` permits a configuration override.
`create-datasource` memoizes a pool with borrow validation; `cluster-conf->spec`
accepts a URL, DataSource or connection factory.

**Disposition:** record the doc/source discrepancy, not an author-intent claim.
Native PostgreSQL connection/recovery checks exercise real driver connections;
copying a `dual` query into them would be a defect. Pool topology and JDBC
factories are implementation options, not semantic requirements.

## Limits of this comparison

Atomic additionally authenticates content hashes and bounds physical/canonical
decode allocation. These are native representation choices, not evidence of
Datomic wire-format equivalence. Cached object bytes are not cached mutable
refs. Provider SQL and Rust orchestration must continue to be inspected separately:
a small DDL file alone cannot establish a small or faithful storage boundary.

## Retry authority and database names

Source `kv-cluster/retry-fn` bounds attempts and measures time spent inside
provider calls. Its elapsed counter does not include semaphore waits or backoff
sleep, and the first three attempts bypass the elapsed threshold. Therefore its
10,000-ms comparison is not an end-to-end request deadline. `set-ref` resolves
an uncertain conditional write by reading the proposed revision and value; it
does not rerun transaction assessment. Native provider CAS results, writer
receipts and request controls remain separate responsibilities. In particular,
an exact committed receipt is not interchangeable with a retryable SQL failure.

Source [catalog.clj](../../../1.0.7705/transactor/src-clj/datomic/catalog.clj)::
`update-catalog` rereads the catalog and reevaluates its condition after each
conflict. `rename-database` moves the same database identity between names;
`delete-database` removes the live name and records the identity in the deleted
set. Neither operation itself traverses and deletes that database's objects.
The peer copy carries these same reviewed mechanisms; comments do not assert
that the complete artifacts are byte-identical.

Native [catalog/database.rs](../../../src/storage/catalog/database.rs) owns
identity, names and creation. [operations/operator.rs](../../../src/operations/operator.rs)
owns lifecycle actions; [operations/reclamation.rs](../../../src/operations/reclamation.rs)
owns bounded collection entry points. Retirement and publication fencing must
remain explicit even though they are reachable through a small admin API.
[operations/deployment.rs](../../../src/operations/deployment.rs) owns stock
writer/peer installation roles; PostgreSQL connection/TLS policy stays in the
provider connection owner, not application configuration. `atomic install` is
the sole setup spelling and refuses a nonempty unrelated namespace without
changing its contents. Runtime connection does not install a schema.

The [backup companion](../01_capacity_and_reliability/02_backup_and_restore.atomic.md)
explains the deliberate `storage::snapshot` → `backup` dependency: repository
file access supplies the same immutable reader, without starting a writer or
providing reference mutation. It is not another storage-engine implementation.

## ATOMIC-NOTE: lifecycle reclamation boundary

Source [garbage.clj](../../../1.0.7705/transactor/src-clj/datomic/garbage.clj)::
`do-mark-garbage` (baseline 244) timestamps supplied superseded IDs; `gc-leaf`
(475) selects marks strictly before the caller's cutoff, and `gc-delete-vals`
(456) issues paced deletes. This is recorded-garbage traversal, not a scan/count
of every live root. Source `gc-deleted-db` (676) uses `partition 1000`, dropping
trailing smaller traversal groups even while its final message reports the full
count. That recovered behavior is not a deletion algorithm to preserve in Rust.

The precise documentation passages are Capacity Planning's
[live-database GC](../01_capacity_and_reliability/00_capacity_planning.md#garbage-collection-for-live-databases),
[separate collector](../01_capacity_and_reliability/00_capacity_planning.md#separated-garbage-collection-tool-for-datomic)
and [deleted-database GC](../01_capacity_and_reliability/00_capacity_planning.md#garbage-collection-for-deleted-databases).
The conservative cutoff protects readers that have not adopted new trees; it is
not a reader pin or permission to collect every held snapshot safely.

`append-leaf` (151) path-copies garbage metadata, includes superseded metadata
among its marks, awaits leaf/directory/root creation, then conditionally publishes
the next garbage-root revision. That is separate from the index/log publication
which emitted the marks. In the recovered `do-mark-garbage` body, an append
exception is caught/logged and the pending cluster batch is still removed.
`flush-garbage` (400) awaits this agent, so it cannot by itself prove every mark
was persisted. The reviewed live `gc` and standalone tool do not reconstruct
unmarked IDs by scanning ordinary roots; the deleted-database walker traverses
its current log/index trees and recorded garbage, not every storage object.
Recovery of omitted marks elsewhere remains unproven; this is a bounded observed
error-handling limitation, not a claim that all such failures permanently leak.
The source documentation itself disclaims complete reclamation of all garbage.

[update.clj](../../../1.0.7705/transactor/src-clj/datomic/update.clj)::
`run-admin-command` (3203) returns `:queued` after `queue-gc` (617) schedules its
process-wide agent; errors alarm/log within that task. In contrast,
[tools/gc_db.clj](../../../1.0.7705/transactor/src-clj/datomic/tools/gc_db.clj)::
`-main` (27) calls the same collector synchronously and prints completion after
it returns. Missing garbage leaves/directories count as complete, allowing a
later attempt to revisit a partly collected tree. Neither entry point shown here
introduces a cross-process collector lock. `gc-deleted-dbs` (771) skips IDs also
present in its captured active catalog, but this review does not prove safety
against every concurrent restore or catalog mutation schedule.

Native [ownership.rs](../../../src/storage/ownership.rs)::`publish_refs` records
root additions/removals atomically with root publication. `BlockCollector::advance`
seals an epoch, folds additions before removals, protects ownership metadata and
checkpoints folded state before deletion; provider `remove_unprotected` checks
collector guards and the protection threshold atomically. These barriers address
shared content, stale collectors and interrupted progress; replacing them with
source-style recorded-ID deletion would omit native safety conditions.

The implementation is bounded per advance, not sublinear per collection cycle.
`ProtectingMetadata` walks the count-index metadata; `Sweeping` enumerates every
namespace object with `list_object_info(..., 1)`, including live objects. Thus
there is at least a full object-enumeration cost per cycle, independent of how
little new garbage exists. Keep that cost visible before any batching/frontier
optimization; a small `maximum_steps` limits one call, not total work. Retirement
age protects previously owned objects; unknown orphan writes are handled by epoch
fencing, not an assertion that every object receives the same age grace.

Keep root classification/events with publication, mutable resumable collector
state with collection, and opaque guarded deletion in the provider. The existing
`object_children` interpreter is the graph-codec boundary (tree blocks are not
all generic `ATOB` blocks). Splitting files is useful only if those dependencies
remain explicit; neither a generic coordinator nor SQL payload interpretation is
required. This review did not execute collector tests or prove all failure schedules.
