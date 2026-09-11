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
