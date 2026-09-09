# PostgreSQL lifecycle contract

This is the current operator contract for Atomic. `goal-8/RUNBOOK.md` records
an earlier prototype and is not authoritative where it differs from this
document.

## State and authority

The transaction log, database catalog, generation membership, requests, and
published head are authoritative. Persistent index trees are immutable,
authenticated, derived values. Uploading content does not make it visible;
visibility changes only in the small conditional head/root publication.

Run `PostgresMigrator::migrate` with a dedicated migration owner before any
runtime starts. Then use `grant_runtime_privileges` for distinct, pre-created
writer and peer roles. Runtime constructors validate the complete checksummed
migration prefix and reject a newer schema. They do not run DDL. Runtime roles
must not own database objects, inherit another role, create schemas, or hold
ambient privileges on Atomic relations or columns. Every installed `atomic_*`
routine has a pinned lookup path of the installation schema, `pg_catalog`, then
`pg_temp`;
this applies to invoker triggers as well as definer functions.

Use a verified `PostgresConnectionConfig` for remote PostgreSQL. Do not put
credentials, database identifiers, request keys, or subject data in logs.
Unknown transaction outcomes expose a lineage-bound request-key digest, never
the plaintext key. Retry only the identical request under the identical key.
After an excision activation, keys from the replaced generation remain
permanently occupied but intentionally lose their replayable receipt/content;
resolution or resubmission returns `postgres/idempotency-predates-excision`
rather than reintroducing excised information.

`PostgresOperator` and restore are destructive administrative surfaces, not
writer/peer runtime APIs. The current grant helper deliberately provisions only
the ordinary writer and peer roles. Run lifecycle mutation with the migration
owner, or with a separately audited operator role carrying the required
installed-routine and relation privileges; never add those privileges to the
service or peer roles. Backup capture itself is read-only but restore uses the
same administrative boundary.

These boundaries translate explicit SQL provisioning and reconnect behavior
from `datomic_pro_docs/08_operations/00_architecture_and_storage/` and the
configured datasource/ordinary CRUD split in recovered
`1.0.7705/transactor/src-clj/datomic/kv_sql_ext.clj` and `sql.clj`. The
checksummed migrations, PostgreSQL ACL matrix, and mandatory certificate
verification are native safety mechanisms, not claimed Datomic formats.

## Portable backup and restore

`PortableBackup::backup_database` captures a live Repeatable Read point while
pinning its exact log generation. A repository is private (`0700` on Unix),
claimed by one durable lineage, and serialized by its lock file. Immutable
objects are differential; a canonical snapshot root is atomically installed
without clobber and the directory is synced only after all referenced objects
exist. A failed publication exposes no new backup point, and retry reuses
complete immutable objects. Stale temporary
files are cleaned only when they match Atomic's exact regular-file grammar;
symlinks are never followed.

Use `list_backup_points` and the exact `(log_generation, basis_t)` overloads
when an excision and its predecessor share the same logical basis. Shallow
verification checks the complete reachable object graph. Deep verification
also reads, hashes, decodes, and semantically reconstructs it. Backup and
restore have the documented memory-proportional tradeoff; Atomic's current deep
verifier also materializes the selected database point, which is an explicit
native implementation limit rather than a Datomic claim.

The repository is permission-private but not encrypted by Atomic. Encrypt its
filesystem or media and manage copied database keys under deployment policy;
Datomic likewise documents that backup data is unencrypted by default.

Restore is a stopped-world operation: stop the transaction service and peers
for the target database, and do not back up the same lineage concurrently.
The target may be absent, may use a new catalog name, or may already name a
different point in the same lineage. It may not belong to another lineage and
restore is not a way to clone one lineage twice in the same PostgreSQL catalog.
For an existing lineage, restore publishes a new database-local physical
generation and may move the visible basis backward. For an absent target, no
synthetic genesis head is exposed while staging. Content and generation
ownership are committed together; activation is one expected-source
publication. An ambiguous retry either resumes the exact unclaimed build or
recognizes the already-published point. A superseded or headless failed build
is reclaimed only after an explicit age boundary and durable abandonment
claim, in bounded restart-safe phases.

After restore, restart services and peers, run a deep
`PostgresOperator::inspect_database`, open a cache-cold peer, and verify current,
history, temporal functions, query, and pull at the chosen point. PostgreSQL
physical backup/WAL archiving remains a separate cluster-level disaster-
recovery layer with deployment-owned RPO, RTO, encryption, and retention.

This follows live/differential/per-database backup, same-lineage restore,
rename-not-clone, stopped processes, selectable `t`, and shallow/deep verify in
`datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md`.
Recovered `backup.clj:1177-1210,1517-1632,1348-1420,1890-1939` supplies the
coherent capture, content reuse, values-before-roots, restore, and reachability
blueprint. ATBK/ATLC encodings, filesystem fsync rules, and database-local
generation rebinding are deliberate native designs.

## Inspection and reclamation

`inspect_database(id, deep)` observes one Repeatable Read database point. It
checks the authoritative generation chain and state commitment separately from
the derived tree. Corruption in database A is reported for A and does not make
database B's logical inspection fail. Shared raw-value deletion nevertheless
fails closed whenever any retained root has uncertain reachability.

Call `garbage_inventory(age)` first and record its exact result. Apply with
`collect_garbage(age)`; the same transaction requires its preview and apply to
agree. Each call advances bounded work from durable ledgers:

- consumed or abandoned tree build intents;
- superseded physical tree publications and exact old-minus-new node marks;
- unreferenced temporal program content;
- retired authoritative log generations; and
- superseded/headless inactive restore or excision generations.

Repeat until the relevant ledgers are complete. Current roots, a connected
peer's immutable old snapshot while its pin session is healthy, live backup
generation pins, active builders, and dependent generations remain protected.
PostgreSQL restart or a broken pin session drops advisory locks; until the peer
reborrows and reacquires them, safety comes from the age horizon. Collection
never guesses from a global orphan scan. `Duration::ZERO` is supported for
controlled imports and tests, but normal deployments should use a safety
horizon at least as long as their longest disconnected/long-running reader or
storage outage. The Datomic guidance is at least one month outside initial
import.

The semantic boundary comes from
`datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:275-286`.
Recovered `index.clj:6327,6433-6448` publishes before marking replaced values,
and `garbage.clj:318-390,475-603` persists exact marks and deletes only after a
caller boundary. PostgreSQL ledgers, advisory pins, and phased generation
collection are the direct native realization.

## Excision

Excision is rare privacy/retention work, never correction. Submit ordinary
transaction data asserting `:db/excise` (A=15) plus optional selected
attributes and one exclusive cutoff in that same transaction. Atomic freezes
the complete request at the A=15 assertion; later ordinary edits remain
historical information but do not retroactively change queued privacy work.
Recovered Datomic materializes the excision entity when indexing, so this is a
deliberate native determinism/safety strengthening of the documented one-map
request shape. A backup is strongly recommended before the request; it is not
an authorization token. Run
`process_excision_requests(database_id)` as the operator/background worker,
then use `sync_excise(database_id, request_t)` to establish completion.

The worker freezes all pending predicates at a captured value, rewrites an
inactive copy-on-write log generation in bounded checkpoints, catches up
transactions committed during the rewrite, stages a corresponding persistent
tree, and conditionally publishes the generation. Retries resume exact durable
work. The immutable A=15 request remains queryable, while matching source
datoms are absent from current state, history, the active log generation, and
new derived indexes.

Protected facts use exact recovered identity: every entity in partition zero,
the recovered bootstrap IDs, and the excision request/audit facts themselves.
Whole-entity requests include recursively owned components and inbound
references. Selected-attribute requests include matching inbound/outbound
values and recursively owned component entities. Attribute-target requests do
not invent component/reference cleanup beyond the documented application
boundary. Cutoffs are strict and cannot pass the request transaction. Where
the recovered instant conversion and documentation disagree for duplicate
transaction instants, Atomic follows the documentation and excludes every
transaction at the cutoff instant.

An already-held peer snapshot is an immutable historical copy and can still
contain the removed value until dropped; controlled processes must sync and
discard it. Excision cannot erase replicas, application exports, PostgreSQL
WAL, logs, or portable/physical backups. Apply the same retention decision to
those copies. Reclaim retired physical generations only after the chosen GC
horizon. Atomic has no fulltext index, so Datomic's unsupported fulltext
excision case is outside the native type surface rather than silently accepted.
Unlike Datomic's documented `noHistory` caveat, Atomic has a live
consolidated-base witness proving removal from the active rewritten log and all
new tree orders. That strengthening does not erase any of the old physical
copies just listed.

These rules come from
`datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md`.
Recovered `excise.clj:139-266` provides `keeper?`, cutoff/entity/attribute,
component, and reference closure; recovered update/index paths provide the
background adoption shape. Copy-on-write PostgreSQL generations strengthen
crash visibility while preserving the documented ordinary request,
asynchronous effect, audit, and synchronization semantics.
