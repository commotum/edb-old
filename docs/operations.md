# Native PostgreSQL operations

This is the active operator guide. Archived goal runbooks are historical
evidence. Goal 5 records current integrity/recovery checks; Goal 6 owns the
measured deployment envelope. No deployment-independent throughput, recovery
time or network-outage bound is implied.

## Measured integrated acceptance — 2026-09-09

The 100,000-record/400,000-business-fact system-of-record workload uses two
independent submitting peers, batches of 100 records, a 4MiB writer tree cache,
1MiB peer caches, and a 2MiB indexing threshold/8MiB recent-pressure cap. Native
ordinary reads, retained snapshots, retry and reopen use no eager compatibility
materialization. Actual writer SIGKILL/replacement and dedicated PostgreSQL
immediate shutdown/WAL recovery pass. These observations are from PostgreSQL
15.11 on a local Threadripper 2950X/NVMe host, not deployment-independent SLAs.

| Operation | Observed result |
| --- | --- |
| Import | 773.913s; 129.213 records/s; 1,000 import transactions, two peers |
| Writer residency | Sampled peak RSS 60,301,312 bytes; accounted cache/recent limits respected |
| First/repeat portable backup | 129.687s/137.792s; 8,214 new then 0 new objects; 484,539,927 bytes |
| Restore including mandatory deep proof | 3367.648s; 2,200,884KiB peak RSS |
| Repaired deep inspection | 2595.920s; 1,566,504KiB peak RSS; healthy with 406 authenticated pending nodes |
| Post-fold full native verification | 1.120s; 13,600KiB peak RSS; exact 401,150 current/401,152 history datoms |
| Dedicated PostgreSQL crash recovery | Storage 518ms; replacement writer 2616ms including lease expiry; 16 full fingerprint scans pass |
| Large source GC | 397.876s; 38,644KiB peak RSS; both windows quiescent; 12 full fingerprint scans pass |

The original large operations parent exited1 on the pending-membership reporting
defect. Its valid copy/restore/native/retry phases are retained; acceptance adds
the repaired audit on that unchanged target, normal bounded publication folding,
post-fold native comparison and separately verified unchanged source before GC.
It does not relabel the failed parent invocation as a pass. The complete current
small operations workflow and27 focused live publication/inspection/backup cases
also pass. [Goal6](../goal-6/0-plan.md) retains exact fixture/hash provenance and
other live semantic/failure evidence.

Data exceeds configured caches, not this host's physical RAM. Warm selective
queries in the scale run still performed SQL; no zero-I/O warm-cache benefit is
claimed. Broad restore/inspection remain expensive and eager. Source GC ran
concurrently with part of target inspection; timings are not isolated benchmarks.
GC conversion's total work follows retained receipt closures, and reachable
payloads are deliberately retained. Provision administrative memory/time and
retention policy separately from ordinary application capacity.

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

Reusing an unchanged published backup root also reads and authenticates every
reachable tree node, validates tree structure, and compares the exact captured
logical point. This is stronger than checking presence, but does not perform
semantic replay. A structurally damaged source receipt tree may still require
explicit administrative replay to repair its portable representation.

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

Successful restore finishes its selected tree publication's membership fold,
including when retry reuses an already published root. Each owner call advances
at most512 nodes; completion follows that exact work header, not an indefinitely
advancing latest index. An interrupted fold remains protected and resumes through
the normal owner API. Publication and acknowledged information need not roll
back merely because later bookkeeping was interrupted.

After restore, restart clients, perform deep `inspect_database`, and use a
cache-cold peer to check current/history values and an application's query/Pull
and retry workflow. Retain independent PostgreSQL physical backups/WAL according
to a separately tested recovery-point/recovery-time policy.

## Inspection, retention and reclamation

`PostgresOperator::inspect_database(id, true)` checks one repeatable-read point.
Report every problem; do not treat successful decoding as proof of tree/log
agreement. Corruption is scoped to its database, but uncertain shared reachability
makes global reclamation conservative.

Index publication is atomic, while its liveness bookkeeping folds in bounded
background work. A healthy report can therefore have `pending_tree_publications`
and `pending_tree_membership_nodes`: these identify authenticated, protected,
resumable maintenance, not missing facts. Inspection cross-checks the original
sealed commitments, predecessor/successor graph closures, live membership,
remaining additions/removals and consumed retirement rows. It does not merely
ignore a mismatch when a work header exists, nor require reconstructing the
historical order of otherwise safe batches. A pending header with zero remaining
nodes still needs final sealing. Missing or inconsistent protection remains an
integrity problem.

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

Semantic-node sweeping is a separate final phase after eligible tree, intent,
receipt, program and generation work. Its no-incoming-reference search can scan
the whole semantic-node catalog even when it finds nothing; the512-victim limit
does not bound examined rows. Empty previews are not scanned again by the delete
function. On the100k fixture, one read-only empty scan of4,314,635nodes took19.67s;
this is a workload observation, not a deadline. Set I/O policy for that broad
phase and include preview time when measuring an operator loop.

Unlike Datomic's documented no-live-segment-read storage GC, this native ownership
conversion authenticates live receipt trees and uses short administrative fences.
It can contend with writers; pace batches and retry explicit Busy admission
without treating integrity failures as transient. Actual upgrade, resumability,
backup/retry and retired-generation fixtures pass. On the100k-record fixture,
both large GC windows pass in397.876s total at38644KiB peak RSS:68 obsolete
publications retire and68 exact receipt archives own48378 node memberships.
All old/current/history fingerprints and independent reopen remain exact.
Conversion reads97394 nodes/11227667529bytes across distinct receipt closures;
all5216 tree payloads/448956990bytes remain reachable and are not deleted.
The run shared a host with deep inspection; these are observed costs, not an
isolated benchmark or a payload-space reduction guarantee.

Use `RECOMMENDED_GARBAGE_COLLECTION_AGE` (30 days) or a deliberately chosen horizon
covering disconnected readers and outages. Zero age is for controlled tests or
imports, not routine deployment. Live snapshot and backup pins protect owned
generations while their PostgreSQL sessions are healthy. Broken sessions/server
restart lose advisory pins; the age horizon protects the interval until readers
reconnect. Do not claim indefinite retention through a longer disconnected
period than the chosen policy. Partially collected, unreadable generations retain
code referenced by remaining authenticated rows until those rows disappear.

## Reproduce the disposable-workload checks

Run from the repository root on a Linux host with PostgreSQL and Rust
dependencies already available. These are administrative test drivers using
plaintext development connections, not production deployment commands. Replace
the paths/account below with existing disposable installations. Use a source
catalog named `atomic_goal6_*` for the later GC guard, and a separate target on
a dedicated PostgreSQL server whose data directory is below the system temporary
directory. Never point the crash driver at a shared server.

```sh
cargo build --offline --release --example scale_workflow --example operations_workflow \
  --example restart_workflow --example gc_workflow
export ATOMIC_POSTGRES_URL='host=/source/socket port=55432 user=atomic_test dbname=atomic_goal6_scale'
export ATOMIC_RESTORE_POSTGRES_URL='host=/target/socket port=55434 user=atomic_test dbname=atomic_goal6_restore'
```

First create the workload. This launches its own writer and two submitting peer
processes, exercises writer SIGKILL/replacement, and leaves the writer stopped
and SQL data retained. Copy the exact `database=` value from its final `PASS`:

```sh
ATOMIC_SCALE_RECORDS=100000 ATOMIC_SCALE_BATCH=100 \
ATOMIC_SCALE_REQUIRE_CACHE_EXCEEDED=1 target/release/examples/scale_workflow
export ATOMIC_DATABASE_ID='ID_FROM_SCALE_PASS'
```

Defaults are 10,000 records and batches of 100; accepted ranges are 2–10,000,000
and 1–1,000 respectively. Cache-exceeding proof is required by default at 25,000
records or more; set `ATOMIC_SCALE_REQUIRE_CACHE_EXCEEDED=1` explicitly for a
scale claim. Small smoke runs are not that proof. `ATOMIC_SCALE_IMPORT_TIMEOUT_SECS`
defaults to 7,200 (range 1–86,400); individual uncertain requests reconcile for
up to 15 minutes with the same key/content. Optional positive
`ATOMIC_SCALE_PROBE_TIMEOUT_MS` shortens each peer's first response wait to
exercise that retry path; it is unset by default.

Next keep the source stopped and restore into the independent target. Stop
target writers and peers too. The driver checks actual server/catalog/schema
identity, refuses active source/target writer leases, and runs capture, unchanged
repeat, deep verification, restore, native comparisons, exact scale-request
retry, deep inspection and a final source-unchanged comparison:

```sh
ATOMIC_BACKUP_DIRECTORY='/tmp/atomic-backup-UNIQUE_RUN' \
  target/release/examples/operations_workflow
```

Choose a fresh directory or an existing operator-private repository for this
lineage. Omitting `ATOMIC_BACKUP_DIRECTORY` creates a private temporary repository
and prints its retained path. Each phase runs in its own process and reports
elapsed time and RSS/high-water memory. Leave `ATOMIC_OPS_PHASE` and
`ATOMIC_OPS_EXPECT_*` unset; the parent driver manages those internal variables.

If an earlier run completed capture and unchanged repeat,
retain that output and resume the remaining phases with its exact manifest hash:

```sh
ATOMIC_BACKUP_DIRECTORY='/tmp/atomic-backup-UNIQUE_RUN' \
ATOMIC_OPS_RESUME_MANIFEST='EXACT_PREVIOUS_OP_RESULT_MANIFEST' \
  target/release/examples/operations_workflow
```

Resume checks that the selected root matches the source's current lineage,
generation and basis before changing the target. Restore still deep-verifies
the backup, and all target/native/retry/integrity/source-unchanged checks run.
The final line reports `resumed=true`; its elapsed time excludes the earlier
capture/repeat, whose separate evidence must be retained. A prior standalone
deep check may be complete or interrupted; report that result accurately.
Resumed restore always performs its own mandatory deep semantic verification.

Only after portable operations succeed, crash/restart the dedicated **target**
server. Supply its actual data directory, matching `pg_ctl`, log path and exact
startup options, including any external configuration file:

```sh
ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1 \
ATOMIC_RESTART_POSTGRES_URL="$ATOMIC_RESTORE_POSTGRES_URL" \
ATOMIC_RESTART_POSTGRES_DATA='/tmp/DEDICATED_TARGET/data' \
ATOMIC_RESTART_PG_CTL='/absolute/path/to/pg_ctl' \
ATOMIC_RESTART_POSTGRES_LOG='/tmp/DEDICATED_TARGET/server.log' \
ATOMIC_RESTART_POSTGRES_OPTIONS='-c config_file=/tmp/DEDICATED_TARGET/postgresql.conf' \
  target/release/examples/restart_workflow
```

The guard requires explicit opt-in, a canonical data directory strictly below
the system temporary directory matching `SHOW data_directory`, and `fsync`,
`synchronous_commit` and `full_page_writes` all on. Those checks do not establish
that the server is unshared; the operator must ensure that. The driver performs
an immediate PostgreSQL stop, attempts restart even after a later failure, and
adds two marker transactions. `ATOMIC_RESTART_ATTRIBUTE` defaults to 1002 and
must select a Long attribute with existing workload facts. It does not provision
a server or delete its data directory. Sixteen bounded canonical current/history
fingerprint scans verify acknowledged and retained old values, recovered as-of
views, the successor and independent writer-offline reopen; eager compatibility
counters must remain zero. Uncached reads need storage to return after the crash;
the outage assertion itself only requires the captured immutable basis locally.

Finally run GC against the **source**, only after all source-unchanged checks
have finished. Every writer in that installation schema must be stopped:

```sh
ATOMIC_ALLOW_DISPOSABLE_GC=1 ATOMIC_GC_MAX_BATCHES=4096 ATOMIC_GC_WALL_SECONDS=1800 \
  target/release/examples/gc_workflow
```

`ATOMIC_POSTGRES_URL` still selects the source above. GC requires explicit
zero-retention opt-in, an actual catalog name beginning `atomic_goal6_`, and
existing cardinality-one Long facts at attribute 1002. It adds one marker and
collects globally across the selected installation schema, first with an old
snapshot held and then released. Defaults are 128 batches and 120 seconds
**per window**, both positive; the command raises them explicitly. Wall budgets
are checked between calls, not a preemptive SQL/Rust deadline. A cap or blocked
retirement prefix returns failure even after `gc_snapshot_safety=passed` prints.

Require successful process exits and retain the phase output; these instructions
are not claims that a particular large run passed. Unconfigured PostgreSQL tests
may skip their live checks and are not substitute evidence. Backup repositories
and PostgreSQL data directories are not automatically deleted by these drivers;
retain or dispose of them deliberately under the retention policy. GC itself
does delete eligible database storage, so its zero-age opt-in is test-only.

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
