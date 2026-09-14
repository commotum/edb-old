# Atomic implementation trace — backup and restore

Development companion to [Backup and Restore](02_backup_and_restore.md).
Source symbols below are in the recovered 1.0.7705 peer and transactor artifacts;
the primary links use the peer copy. This compares reviewed publication and
validation mechanisms, not file-format, JVM, S3 or command-line equivalence.

## Capture, differential copy and root-last publication

Passages: “Backing Up,” “Differential Backup,” and “Listing Backups.”
[backup.clj](../../../1.0.7705/peer/src-clj/datomic/backup.clj)::`ensure-claim`
checks repository ownership by database lineage. Its read/write/read sequence
is not an exclusive backup-job lock. `create-backup-job` captures the tail
descriptor and data before separately reading the index reference; the source
does not obtain those references with one atomic multi-key read.

`ValueBackup` waits for descendant work before copying its parent. Existing
segments allow incremental subtree reuse because a published prior backup is
child-closed; mere presence of an arbitrary parent is not that proof.
`backup-db` finishes both index and log copies before `backup-roots` advertises
the selected point. This is why interrupted staging must not expose a restorable
point with missing dependencies.

Native [backup/capture.rs](../../../src/backup/capture.rs)::`capture` freezes one
published database root and follows its authenticated graph, including receipt,
read-authorization and persisted-program objects. A concurrent live publication
does not retarget this capture. [backup/repository.rs](../../../src/backup/repository.rs)
owns lineage claims, content-addressed files and no-clobber point publication.
It writes/fsyncs immutable objects before exposing the point file. Existing
content is reused, but capture still visits reachable objects and retains a
visited set: differential bytes copied do not imply constant memory or work.
No repository operation publishes a live reader pin; the configured retirement
grace remains a prerequisite for a long-running capture of live objects.

[fsbackup.clj](../../../1.0.7705/peer/src-clj/datomic/fsbackup.clj)::
`FileSystemStorage` is a byte/key provider. Its alternate prefix-case lookup is
an old-layout accommodation, not an Atomic reader to reproduce. Native current
repository layout, bounded regular-file reads, content authentication and
private-directory checks belong together. The repository is not encrypted by
the product; protected deployment/storage remains the operator's responsibility.

## Restore publishes authority, not just files

Passages: “Restoring,” its stopped-process preconditions, and same-database
lineage/name restrictions. Source `restore-roots` separately resets the index
reference and tail; the documentation's stopped-users requirement matters.
Copying source call order into a concurrent multi-reference native system would
not itself preserve a coherent publication.

Native [backup/restore.rs](../../../src/backup/restore.rs) verifies the selected
point, copies objects under the collection protection protocol and retains a
resumable checkpoint. Final activation conditionally publishes the coherent
current roots with catalog/lease/generation checks. Completion receipts resolve
an uncertain acknowledgment without silently starting another restore. Live
writer authority is rejected; lineage and target-name checks are not bypassed
by renaming a folder or selecting a different point. This is a deliberate native
publication adaptation, not an assertion that Datomic uses batch CAS or native
restore receipts.

## Verification is not ordinary opening

Passage: “Verifying Backups.” Source `verify-backup` walks log/index segments,
checking presence and optionally reading them. The documented `read-all` mode
does not promise native semantic replay or validation of native receipt and
program authority objects.

[backup/verify](../../../src/backup/verify/mod.rs) separates graph inventory,
authority/retained-value checks, canonical replay and derived projection checks.
Presence verification and full verification are distinct APIs. Ordinary
[backup/snapshot.rs](../../../src/backup/snapshot.rs)::`BackupConnection` opens
one authenticated read point and loads reachable data selectively; it does not
run full historical replay on every open.

The canonical `1..basis` replay in
[verify/replay.rs](../../../src/backup/verify/replay.rs)::`canonical_log` retains
the log owner's `LogTraversal` between transactions. The former per-entry
`read_record` loop discarded forward navigation. Its permanent oracle runs both
navigation shapes over the same 515-transaction fixture: 832 sealed-page reads
for repeated seeks, eight for forward traversal, each sealed page once. It also
compares final history and checks cancellation and missing-object propagation.
Sparse receipt and retained-prefix authority checks still perform point reads
in their own order. This is a bounded navigation improvement, not a whole-verifier
linear-work, bounded-total-memory or latency claim.

## One immutable read engine and explicit product scope

Passage: “Reading from Backups.” Source `load-database` composes log/index reads
over a backup provider rather than creating a second query implementation.
Native [storage/snapshot.rs](../../../src/storage/snapshot.rs)::`SourceBackend`
similarly selects live object storage or the repository reader. Schema, exact
values, queries, Pull and speculation continue through `DatabaseValue`; the
repository backend cannot supply live reference mutation. This is why the
storage snapshot's narrow dependency on the backup reader is intentional.

Atomic supports current-format filesystem repositories and PostgreSQL restore.
AWS encryption/configuration, JVM memory flags, old-layout converters and other
provider transports are not promised native capabilities. Backup, verification
and restore controls bound/check individual work units; a copied-object graph
and full semantic replay still have costs proportional to the selected data.
Relevant independent contracts live in [block_backup.rs](../../../tests/block_backup.rs),
[backup_object_admission.rs](../../../tests/backup_object_admission.rs),
[backup_reads.rs](../../../tests/backup_reads.rs), and the repository/replay unit
tests. PostgreSQL-dependent tests need a real configured server; their presence
alone is not runtime evidence.
