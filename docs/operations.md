# Native PostgreSQL operations

This is the active operator guide. Archived goal runbooks are historical
evidence. Goal 5 records current integrity/recovery checks; Goal 6 owns the
measured deployment envelope. No deployment-independent throughput, recovery
time or network-outage bound is implied.

## Provision and upgrade

Use a dedicated object-owning migration account to run
`PostgresMigrator::migrate`, then grant distinct pre-created writer and peer
roles with `grant_runtime_privileges`. Runtime accounts must not own the
installation, inherit administrative roles or have ambient schema/column
privileges. Runtime constructors verify the checksummed schema prefix and do
not perform DDL. Use the writer role for the service/indexer, the peer role
for independent reads, and a separately controlled administrative account for
restore, repair, inspection and GC. Native Unix submission is same-host,
same-OS-user only (private directory and socket), not an internet service.

Migration 24 versions program-dependency completeness. Quiesce legacy writers,
indexers and GC before upgrading; preserve a recoverable backup. The migration
authenticates and repairs older marks before trusting them. A failure rolls
back the migration and the new binary refuses the older schema. Correct the
reported missing/corrupt information and retry; do not resume obsolete binaries
or their unsafe GC routines after a failed repair. Atomic rollback does not
retrofit safety into old code. Schema-24 GC refuses an incomplete/obsolete
walker marker and retains references during repair. Do not edit applied SQL
migrations or manually set the completeness marker to true.

Migration 25 adds resumable exact-receipt archive conversion for ordinary GC.
Upgrade quiescent installations with the current binary; older binaries do not
understand its ownership transition. Applied migration1–24 bytes and durable
request/manifest hashes are unchanged. Conversion is an administrative operation;
neither runtime role receives new mutation or collector privileges.

Exceptional reference repair is a quiesced administrative operation too. It
locks durable owners and authenticates retained log/code roots, including code
pre-staged by a paused restore. Readable generations also undergo semantic
replay against their published endpoint. This one-time integrity guard can use
memory and time proportional to retained history; it is not required on healthy
repeated migration. Running repair concurrently with lifecycle GC can deadlock;
PostgreSQL aborts a participant safely, but online repair is not the supported
procedure.

Ordinary peer opens trust authenticated roots published by an authorized
indexer. Hashes authenticate content and coordinates, not the honesty of that
indexer. Deep inspection/backup verification additionally compares derived
information to the authoritative log. It is deliberately not an ordinary-read
or transaction prerequisite.

## Transport and failure policy

Use private Unix-domain sockets locally, or
`PostgresConnectionConfig::require_tls` for remote PostgreSQL. The latter
requires verified certificates/hostnames and TLS 1.2 or newer, even when a
parameter string requests plaintext. A private CA can be added explicitly.
The legacy string constructors deliberately select plaintext development
behavior; `sslmode` in that string is not a substitute for the configured API.

Choose `PostgresIoPolicy` per role. Defaults preserve driver/server settings;
there is no hidden universal production timeout. For example, a runtime policy
can set statement and lock deadlines while a separate administrative policy
allows longer backup/index work. All durations must be positive. The policy
is shared by configured plaintext and TLS connections and preserves unrelated
startup options.

| Setting | What it bounds | What it does not bound |
| --- | --- | --- |
| `statement_timeout` | Each server-side SQL statement | Entire transaction, local query, arbitrary Rust callback |
| `lock_timeout` | Each server-side lock wait | All work surrounding that wait |
| `connect_timeout` | Each socket/address attempt; minimum of configured, policy and caller caps | DNS, TLS/auth/startup, all addresses combined |
| TCP user timeout/keepalives | OS-specific liveness behavior | A measured end-to-end outage SLA; Unix sockets |
| Query/Pull controls | Cooperative evaluator/interpreter/projection work | Preempting arbitrary Rust or an already blocked SQL call |

SQL cancellation (`57014`) is Interrupted; lock timeout (`55P03`) is Busy.
These are not transport-loss claims. A delivery timeout or connection loss
after attempted submission can still be `UnknownOutcome`: reconcile/retry the
identical request key and content. Do not submit under a new key to resolve an
unknown result. A known commit remains committed if opening its local report
fails; the native socket result separates those facts. Report observation
failure is likewise separate from transaction success.

Do not log credentials, plaintext request keys or subject data. Use structured
categories and lineage-bound request digests for diagnostics. Configure process
supervision and measure failover in the target environment; peers retain
immutable database handles during writer unavailability, while cache misses
still require available storage. PostgreSQL HA/WAL shipping is a separate
deployment responsibility, not implemented by the Rust transaction lease.

## Backup, verify and restore

`PortableBackup::backup_database` captures a live, repeatable-read information
point and pins its log generation. A private repository is claimed by one
lineage and serialized by its lock; immutable content is reused across backups
and a root becomes visible only after referenced objects are durable.
Repositories are permission-private, not encrypted by Atomic. Encrypt media
and control copied backups under your retention policy.

Backup capture authenticates and copies immutable objects and linked
coordinates without replaying transaction semantics. Hash-valid content
can still contain a false state or transition claim. Run deep verification to
establish semantic consistency; restore performs it before activation. Copy
success alone is not a semantic integrity claim. Canonical encoding, hashes,
chain/membership, receipt/program closure and durable root publication remain
checked during copying.

Use `list_backup_points` and exact `(log_generation, basis_t)` overloads when
different physical generations share a logical basis. Presence checks verify
reachability; deep verification reads/authenticates content and reconstructs
authoritative information. Main indexes and every exact request-base archive
are compared at their own log point, including current/history/index membership
and legitimate noHistory/pending-AVET projections. A valid hash with false
contents is rejected. Restore performs deep verification before activation.

Backup, restore and deep inspection are intentionally broad operations, not
constant-memory/constant-time queries. Current verification replays the selected
database and materializes comparison datoms for individual indexes; it does not
retain a database copy for every receipt. Measure resources against actual
history, retained archives and tree count. Shallow presence alone is not a
semantic correctness proof.

Restore is stopped-world for the target lineage: stop its writers and peers and
exclude concurrent backup/restore on that repository. Use
`restore_backup_point(directory, basis_t, generation, target_id)` for an exact
point. The destination may be absent or name another point in the same lineage;
it cannot be another lineage or clone the same lineage twice in one catalog.
Staging is inactive until conditional activation. Retry an ambiguous restore
with the same selected point and destination; do not manually publish staged
heads or resume permanently claimed/abandoned builders.

After restore, restart clients, perform deep `inspect_database`, and use a
cache-cold peer to check current/history values and an application's query/Pull
and retry workflow. Retain independent PostgreSQL physical backups/WAL according
to a separately tested recovery-point/recovery-time policy.

## Inspection, retention and reclamation

`PostgresOperator::inspect_database(id, true)` checks one repeatable-read point.
Report every problem; do not treat successful decoding as proof of tree/log
agreement. Corruption is scoped to its database, but uncertain shared reachability
makes global reclamation conservative.

Preview `garbage_inventory(age)` and apply `collect_garbage(age)` under the same
chosen policy; concurrent activity can legitimately change separate previews.
Each applied operation checks its own transactional preview and advances bounded
durable phases. Repeat until the relevant work is complete. It covers retired
roots/nodes, programs, request-base archives, authoritative generations and
abandoned content-first builds. A single successful batch is not full cleanup.

Schema24 has an ordinary-GC liveness defect: active receipt bases can permanently
pin the oldest publication and block later index reclamation. Schema25 transfers
those exact bases to archive ownership without changing the manifest or deleting
receipts. Include `receipt_archive_conversions` in progress accounting, even
when a batch reports no physical deletions. Traversal, ordered digest, retirement
ledger drain and the atomic handoff resume from durable progress after reopening
the operator. Incomplete archives are not readable owners; the original
publication remains readable and protected until the final handoff commits.

Conversion authenticates up to512 immutable node reads, hashes or retirement
rows per phase; start/final source checks read eight root nodes. These are work
item bounds, not a wall-clock deadline or a total-GC memory bound. Total conversion
cost follows the sum of distinct retained receipt-base tree closures, not just
changed paths. Archive membership shares immutable node payloads rather than
copying application data. Required receipt nodes remain retained, while obsolete
publication metadata and genuinely unreachable nodes can be reclaimed. Do not
expect GC to erase information still owned by receipts, current roots or pins.

Unlike Datomic's documented no-live-segment-read storage GC, this native ownership
conversion authenticates live receipt trees and uses short administrative fences.
It can contend with writers; pace batches and retry explicit Busy admission
without treating integrity failures as transient. Actual upgrade, resumability,
backup/retry and retired-generation fixtures pass; larger-workload maintenance
costs remain part of Goal6 acceptance.

Use `RECOMMENDED_GARBAGE_COLLECTION_AGE` (30 days) or a deliberately chosen horizon
covering disconnected readers and outages. Zero age is for controlled tests or
imports, not routine deployment. Live snapshot and backup pins protect owned
generations while their PostgreSQL sessions are healthy. Broken sessions/server
restart lose advisory pins; the age horizon protects the interval until readers
reconnect. Do not claim indefinite retention through a longer disconnected
period than the chosen policy. Partially collected, unreadable generations retain
code referenced by remaining authenticated rows until those rows disappear.

## Excision

Excision is rare privacy/retention work, not correction; ordinary retraction
preserves the history needed to explain past decisions. Submit an ordinary
`:db/excise` request with optional selected attributes and one strict cutoff,
then run `process_excision_requests` and establish completion with `sync_excise`
or the connection's corresponding synchronization. Request facts are protected
audit information. Atomic freezes the complete predicate at its A=15 assertion;
later ordinary edits do not rewrite an already queued request.

The worker rewrites an inactive generation, stages indexes and conditionally
activates it. It can resume interrupted work. Large excisions can have costs
proportional to the whole database and reduce write availability. An old held
database/log value remains an immutable copy: discard it and sync controlled
clients before claiming they observe removal. Reclaim retired generations only
after the appropriate horizon.

Excision does not erase application exports, logs, old process memory, replicas,
PostgreSQL WAL or portable/physical backups. Apply the same retention decision
to those separately. A pre-excision backup is recommended for operational
recovery, but retaining it also retains the removed information; it is not an
authorization token or a physical-erasure guarantee. Fulltext is unsupported.

Semantic references: local Datomic backup/restore, HA and excision documentation
under `datomic_pro_docs/08_operations/01_capacity_and_reliability/` and
`datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md`.
