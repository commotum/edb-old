# Administrative CLI

`atomic` exposes the existing native operators as commands. Use a separately
controlled administrative account and an explicitly chosen PostgreSQL catalog.
Commands do not install PostgreSQL, create PostgreSQL databases, migrate on
startup, or silently repair a damaged database. Provision the storage service and
credentials separately; then use the commands below for the Atomic catalog.

Build with `cargo build --bin atomic`. Set `ATOMIC_POSTGRES_URL` and the explicit
`ATOMIC_POSTGRES_TRANSPORT=tls` or `plaintext` policy as described in
[application.md](application.md). Verified TLS/root certificates and the same
connection/statement/lock settings apply to administrative connections. Offline
`list-backups` and `verify-backup` need no PostgreSQL credentials. Never paste
connection strings into logs or shell command arguments.

## Provisioning and inspection

```sh
atomic migrate --writer-role atomic_writer --peer-role atomic_peer
atomic create --database application
atomic status --database application
atomic inspect --database application
```

The two distinct runtime roles must already exist and satisfy restricted-role
requirements. `MIGRATED` and `GRANTED` are separate committed results: grant
failure does not undo the migration. Migrations preserve their prior checksums;
follow the quiesced upgrade guidance in [operations.md](operations.md).

`status` is a lightweight catalog-coordinate observation, not a deep integrity
check or proof of a live transactor. `inspect` captures one consistent PostgreSQL
snapshot and checks derived data deeply by default; `--shallow` omits the deep
derived-index comparison, not every recovery/read operation. Inspection can use
memory and time proportional to retained history. A nonzero `INTEGRITY_PROBLEM`
count exits unsuccessfully. Diagnostic labels are printed, not subject-bearing
problem messages.

`pending_tree_publications`, `pending_tree_membership_nodes`,
`pending_avet_projections` and ordinary index lag are reported separately.
Authenticated pending maintenance is not by itself corruption. Do not infer
global storage health from one database's status, or force an unhealthy report
to green by suppressing a problem.

## Back up and verify

```sh
atomic backup --database application --repository /private/backups/application
atomic list-backups --repository /private/backups/application
atomic verify-backup --repository /private/backups/application --basis 42 --generation 0
```

Use a private repository owned by the process user. The native backup code
checks paths/permissions, claims a repository for one lineage, serializes its
writes, and publishes a point only after referenced immutable objects are
durable. Do not mix databases in one repository. Repeating backup reuses
unchanged objects; `objects_written` and `objects_reused` report what happened.
Backup is live and pins its captured information generation, but backup capture
alone is not semantic verification. Media encryption and repository retention
remain deployment responsibilities.

`list-backups` prints exact basis/generation pairs and manifest hashes. Generation
matters when excision produces different physical information at one basis;
verification/restore never silently select a different generation. Both zero
basis and zero generation are valid when listed.

Verification is offline and deep by default: it authenticates data and compares
derived information to authoritative replay. `--presence-only` is the explicitly
weaker reachability check and prints `semantic=false`. It is not a substitute for
deep verification or a corruption-free certificate. Neither command changes a
repository's immutable contents.

## Restore into a separately selected destination

For this command, `ATOMIC_POSTGRES_URL` is the **destination connection**. The
repository is the source; the live source need not be reachable. Explicitly
provision/migrate the destination catalog first, then stop its writers and peers
and exclude concurrent backup/restore against this repository.

```sh
# First select the destination credentials/transport in your environment.
atomic migrate
atomic restore --repository /private/backups/application --basis 42 --generation 0 \
  --target-database recovered --postgres-database atomic_recovery --catalog-schema public

# The same exact selection, with explicit activation authority:
atomic restore --repository /private/backups/application --basis 42 --generation 0 \
  --target-database recovered --postgres-database atomic_recovery --catalog-schema public --apply
atomic inspect --database recovered
```

`--postgres-database` names PostgreSQL's database, `--catalog-schema` names the
namespace containing the actual `atomic_databases` relation, and
`--target-database` names the logical Atomic database. The first two names must
match the configured destination; this protects against a wrong connection or
an earlier empty schema in `search_path`. No SQL identifiers are interpolated
from these flags.

Without `--apply`, restore only deep-verifies the selected backup and observes
the target catalog. It does not stage, create or activate a logical database;
the preview explicitly leaves target-lineage compatibility to the apply-time
guard. Apply repeats the existing mandatory verification, stages inactive data,
and conditionally activates the exact point. It rejects an unrelated existing
lineage and cannot duplicate one lineage under a second name in the same catalog.
Use an independent PostgreSQL database/catalog for a simultaneous copy of a live
source. Renaming at a separately selected destination is supported.

An interrupted/ambiguous restore may have durably staged or activated work.
Retry the same repository/basis/generation/destination; do not manually publish
staged heads or assume process failure rolled back earlier commits. The library
resumes its bounded ownership/fold phases. After success, restart clients and
test the application's current/history/query and exact retry workflow.

## Explicit derived-index maintenance

```sh
atomic consolidate --database application
atomic fulltext-rebuild --database application
```

`consolidate` rebuilds/advances native indexes from authoritative information; it
is explicit recovery or maintenance, not an ordinary startup fallback.
`INDEXED` is the canonical result. A separate `SEARCH status=failed` means the
optional search build failed after that canonical success; it does not undo
index publication. Inspect the reported code and retry the derived work.

`fulltext-rebuild` builds a missing search projection for the newest published
manifest. It may reuse an existing projection; its success is not a fresh deep
proof of every existing search page. The command does not advance unindexed
transaction novelty: consolidate/request indexing first when that is intended.

For a diagnosed corrupt projection, select the exact newest source manifest and
authorize discarding only its derived search data:

```sh
atomic fulltext-rebuild --database application \
  --discard-manifest EXACT_64_HEX_MANIFEST --apply --batches 1
```

Obtain the manifest from an earlier `FULLTEXT_REBUILT source_manifest=...` result
or an authorized `NativeFulltextReader::projection()` observation. The supplied
digest must be this logical database's newest canonical publication, not an
arbitrary hash. Each discard batch removes at most 4,096 blocks; the owner-only
library repair serializes with builds. A `status=pending` result is partial
progress: repeat the same command. Once discard completes, reconstruction runs.
If the database advances meanwhile, the command reports a conflict rather than
silently relabeling another manifest as the requested repair. Quiesce indexers
when deliberately repairing a selected source.

Canonical facts/history are untouched. Existing cached search data may remain
readable, while cold readers can fail until reconstruction; discard is neither
a privacy-erasure guarantee nor a rollback of committed data.

## Catalog-wide garbage collection

```sh
atomic gc --postgres-database atomic_storage --catalog-schema public --older-than-seconds 2592000
atomic gc --postgres-database atomic_storage --catalog-schema public --older-than-seconds 2592000 \
  --apply --batches 3
```

GC operates on the entire shared Atomic catalog, **not one logical database**.
The explicit physical target names are verified before collection. Preview is
the default and changes no ownership or payloads. Apply recomputes/checks its
own transactional candidates; a separate preview is not a frozen deletion list
and may legitimately differ after concurrent activity.

Retention is mandatory and measured as an age in seconds. Thirty days is the
documented normal margin; a shorter age (including zero for controlled imports
or isolated tests) is allowed and reported as `short_retention=true`, not silently
substituted. Connected snapshots retain existing pins, but short retention can
break disconnected long-lived readers or unpinned snapshot references. It does
not erase already observed values or live receipt-owned information.

`--batches` is a positive finite number, default 1, and applies only with
`--apply`. Each existing collector call limits victim/ownership work (typically
512 items for node phases, with separate fixed limits for other owners).
Candidate searches can still scan large catalogs; this is not a total-memory,
SQL-row or wall-clock bound. See the measured caveats in
[operations.md](operations.md#inspection-retention-and-reclamation).

Progress includes receipt-archive conversion and log-generation work even when
no physical bytes are deleted. A completed requested batch count does not prove
global quiescence; output says `global_quiescence=not-established`. Repeat under
the same retention policy as needed and inspect the relevant owner state. A
signal or connection failure can leave already committed batches; retrying
continues from durable state, never a command-local cursor. Removed unreferenced
physical values are not recoverable through an undo command; retained backups
and authoritative facts provide the supported recovery path.

## Evidence and process output

Commands emit coarse `PROGRESS` phase starts, completion receipts and per-batch
counts. They do not estimate an unmeasured percentage or hide a long internal
verify/scan. CLI errors include category/code and return nonzero; no failed
observation implies rollback of earlier committed work. Success with explicitly
pending search repair or completed bounded GC batches is not whole-system
completion.

[admin_cli.rs](../tests/admin_cli.rs) drives actual commands for provisioning,
repeat backup, offline verification, separately targeted restore/preview/retry,
deep inspection, restricted/authorized GC and derived search repair. Its live
test creates two dedicated disposable PostgreSQL databases; catalog-wide GC
never runs on the shared connection's original database. On Unix it also holds
an ordinary destination node-table lock, observes the restore process waiting
to write a node, sends SIGTERM, releases the lock and retries the exact restore
selection before checking current/history/search. No production fault hook is
used. Missing
`ATOMIC_POSTGRES_URL` is explicitly reported as skipped, not as verified coverage.
