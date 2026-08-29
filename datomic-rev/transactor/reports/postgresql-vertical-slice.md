# Recovered PostgreSQL vertical slice

## Outcome

The recovered Datomic Pro 1.0.7277 Transactor and recovered Peer have crossed
the primary PostgreSQL runtime path:

1. recovered Transactor boot;
2. provisioned SQL catalog and Datomic database initialization;
3. recovered Peer connection and transaction submission;
4. durable PostgreSQL/log commit and Peer notification;
5. query of the committed immutable database value;
6. recovered Transactor restart and log/index catchup;
7. a second transaction after that restart;
8. a second durable PostgreSQL/log commit;
9. explicit persistent-index publication through all four index families;
10. another fresh-process restart that adopts the published root at the exact
    requested `t`; and
11. an identical recovered Peer snapshot after adoption;
12. bounded transport interruption with exact unavailable on the same Peer
    connection; and
13. same-connection reconnect, sync, commit/read, plus a fresh-Peer semantic
    equality check;
14. a second recovered Transactor proving true standby state while the first
    remains active;
15. standby promotion after the exact active PID is stopped, followed by a
    durable same-Peer commit through the promoted endpoint; and
16. stale-primary heartbeat-conflict self-fencing, continued promoted-primary
    heartbeats, and an independent fresh-Peer equality check; and
17. a dedicated missing-schema startup failure that preserves the original
    PostgreSQL exception, never reaches service readiness, and cleans up the
    still-owned recovered process without repeating the green main path; and
18. a no-service PostgreSQL gate that proves both index-root reference and
    log-tail root rejection while preserving the winning authoritative rows;
    and
19. focused stale-CAS and uniqueness rejection, concurrent CAS arbitration,
    strict accepted-transaction ordering, and exact canonical state after a
    recovered Transactor restart and fresh recovered-Peer audit; and
20. one exact prepublication crash cut in which the authoritative PostgreSQL
    log-root update is lock-observed, no Peer success is delivered, the owned
    Transactor and its exact blocked SQL session are terminated, the
    interrupted transaction remains absent, and two fresh-process recovery/
    audit passes preserve the expected state; and
21. one exact post-publication/pre-result cut in which the Peer is frozen,
    PostgreSQL durably advances the authoritative root, the owned Transactor is
    killed before result delivery, and the same Peer plus a fresh Peer recover
    exactly one committed CAS/sentinel effect with no duplicate execution.

This is a real vertical-slice milestone, not Goal 2 completion. The broader
licensed-oracle comparison and remaining HA race/in-flight boundaries remain
open.

## Repository-owned executing gate

The current full-path HA promotion run passed at
`/tmp/datomic-recovered-pair-ha-v1`. It used repository runner SHA-256
`e61b99c84c0aa8629ab0719929d9b34ed3ebf7011ccb918759ba88021a14565b`
and HA-probe SHA-256
`4858e5d7d4b0b4d685860e8966ba73904ba3709d3d05fdcd151d1682dc001808`.
Its 140-entry self-verifying `evidence.sha256` has SHA-256
`62859ef9ec8e66c43743434ea35974dde6dfd8362ff23cc6226d86721f2edf88`;
every entry verifies.

After the transport sentinel at basis `1099`, Transactor A remained active on
port 54365 while Transactor B proved repeated `:transactor/standby` events, an
advancing `pod-standby` revision, and no listening service on port 54366. The
harness then verified A's PID, start time, executable, complete argv, and
`SIGSTOP` state. B won the PostgreSQL heartbeat CAS at redacted `pod-coord`
revision 26 after A's last observed revision 25 and began serving. The same
recovered Peer connection recovered its zero-argument sync on attempt six
after one bounded timeout and four exact unavailable outcomes, reconnected
from A's port to B's port, committed exactly one `:db/doc` sentinel, and
uniquely read it at basis `1101`.

After verified `SIGCONT`, stale A logged exact heartbeat conflict and process
failure evidence, then exited 255 through its delayed self-fence path. B
continued heartbeating through redacted coordination revision 34. A new
recovered-Peer JVM saw basis `1101`, the same database id and 96-row view, and
the same datoms, history, logical-value, and row hashes as before failover.
The promoted B stopped by bounded `SIGINT`, PostgreSQL control state is
`shut down`, host ports 54365, 54366, and 55465 are closed, and no run process
remains. Only coordination revisions and counts are retained; the SQL heartbeat
map is not copied into evidence.

This closes one narrow active/standby takeover and stale-primary self-fence
slice. It does not establish all partition timing, concurrent/in-flight
transaction, acknowledgement, or writable split-brain cases, so Stage 7 is
still in progress.

The dedicated startup-failure run passed at
`/tmp/datomic-recovered-pair-startup-failure-v1` using the current repository
runner at SHA-256
`35876097349d13215dd4d8e2cb50fdd339ea78b6770bb67424b5b627cb112bc8`.
Its 69-entry self-verifying `evidence.sha256` has SHA-256
`40a45828557f88f9677f0926b992d639461c01f8f5a322344190d57fc7ac573b`;
every entry verifies. The gate created a fresh PostgreSQL role and catalog but
deliberately omitted `datomic_kvs`. The exact candidate PID, start time,
executable, argv, and pid file remained owned while all 20 bounded
`:kv-cluster/retry` events retained root cause
`org.postgresql.util.PSQLException`, followed by exact
`Terminating process - Lifecycle thread failed`. No secondary
`NullPointerException` occurred.

The launcher did print `System started`, but the service port never opened;
the gate therefore proves that the marker is not itself readiness. Once the
expected failure was established, the harness stopped the still-owned JVM by
SIGINT only (status 130), verified that the schema was still missing and
PostgreSQL remained responsive, and then shut PostgreSQL down. Both host ports
are closed and no run process remains. The result deliberately records the
main, transport, and HA paths as `NOT_RUN`: this is a focused regression for a
confirmed startup defect, not another expensive replay of already-green work.
Deeper post-coordination and partial-master cleanup rows remain optional
failure-matrix extensions.

The focused storage-CAS run passed at
`/tmp/datomic-recovered-pair-storage-cas-v2` using repository runner SHA-256
`85819e557c2ae70d112e4a1bf8ab32d281c19cfcd5ba32fedbaddac6fdc7aa6a`
and storage probe SHA-256
`31ba167f26caf7f0e453a5128912de9a0fa6ba1549f17f78c24d640019f5f080`.
Its 84-entry self-verifying `evidence.sha256` has SHA-256
`98c39f04a7536e51e218fb4d28a38b080ee15fa8d22bb0a8dadfc1fe46f5d7f4`;
every entry verifies. No Transactor service, Peer transaction workload,
transport probe, or HA sequence ran.

The gate first executed one sealed low-level probe through the recovered Peer
reference, truncated only the fresh disposable `datomic_kvs` table, and then
executed the same probe through the recovered Transactor sources. Both paths
used the production `ref-index-root/<db-id>` and `pod-log-tail/<db-id>` key
builders. They created and byte-round-tripped three immutable root targets;
proved ref creation, exact idempotent replay, revision-1 publication,
conflicting-create rejection, and differing stale-revision rejection; and
proved accepted then stale-rejected `log/write-tail-descriptor` publication.
The winning ref and pod rows were read directly from PostgreSQL and remained
byte-identical across the losing writes. The stale pod attempt intentionally
leaves an unreferenced immutable tail value, which is not an authoritative-root
mutation.

The Peer-reference and Transactor result markers are byte-identical at SHA-256
`1a706a2617fc903dd0c4f90c6888ea11dc7d13065c7c07a32982f0a739de4021`.
Each phase ended at exactly 8 rows, 43 stored value bytes, and 2 revisioned
rows; the intervening truncate was independently observed as 0/0/0. The two
candidate JVMs exited with zero Datomic-user PostgreSQL sessions, PostgreSQL
reports `shut down`, ports 54368 and 55468 are closed, and the runtime seal
verifies. A predecessor at `/tmp/datomic-recovered-pair-storage-cas-v1`
failed before Datomic execution because the restricted sandbox denied
PostgreSQL's localhost bind; cleanup succeeded, and it is retained only as
harness diagnostics.

The focused Stage 5 transaction run passed at
`/tmp/datomic-recovered-pair-transaction-boundaries-v5` using repository
runner SHA-256
`96cc9051e848b6dff811f0e0c22dccb71370eea510fdf17614c77b3c3d5d00dd`
and transaction-probe SHA-256
`d2dad703ab7532bf95f1972ac767cdf5490e56a178dd34c3c3ac7309fc826e41`.
Its 91-entry self-verifying `evidence.sha256` has SHA-256
`8efea20819c62f167d774d29d7a330f6ef1e018884c50f16a533376d667a3bdd`;
every entry verifies.

The serial stale-CAS transaction returned exact `:db.error/cas-failed`,
conflict, and `:datomic/cancelled true` data while preserving basis, counter
history, same-transaction sentinel absence, and the byte-identical direct SQL
log-root row. The distinct-entity uniqueness collision returned exact
`:db.error/unique-conflict` while preserving basis, ownership, companion
absence, and root. Two four-way CAS rounds each produced exactly one accepted
worker and three exact conflicts. Worker-distinct companion entities prove
that only the winner persisted; its report `t`, event `t`, post-round basis,
and one log-root revision agree, and every loser identity is absent.

Four simultaneous accepted submissions formed one strict report-basis chain
with exact worker-to-persisted-`t` identity and ordered values
`[1011 1013 1015 1017]`. No event was duplicated or lost. These gaps are
evidence that transaction-number allocation is monotonic rather than
contiguous: rejected attempts can consume or reserve `t` without advancing the
published database basis or root. The normal accepted CAS returned at basis
`1005`; a direct PostgreSQL read after return observed root revision 3 advance
to 4. This is a normal-path observation, not proof against a crash between
durable publication and acknowledgement.

After graceful restart, a fresh recovered Peer reproduced database id, basis
`1017`, and canonical transaction-projection SHA-256
`f5d50940a93d012606e45a840ddf1104d7c05f0be373d082822f07515f9194b9`.
The fresh Transactor adopted 8,044 log bytes through `tail-t 1017`; PostgreSQL
remained exactly 48 rows, 12,133 stored value bytes, and 5 revisioned rows.
Both recovered Transactors stopped by `SIGINT` only with status 130, zero
Datomic-user sessions remained, PostgreSQL reports `shut down`, and ports
54369 and 55469 are closed. That normal-path result explicitly records the
crash boundary and full licensed-oracle equality as `NOT_RUN`; the separate
prepublication cut below supplies the first of those two rows.

The focused prepublication crash-consistency run passed at
`/tmp/datomic-recovered-pair-ack-crash-v2`. It uses repository runner SHA-256
`200049fcc793579a32d5b4e283ef716b7ecb39448a3ffd69c070461cd574030d`
and acknowledgement probe SHA-256
`2aa34216585da3c996d81e886b9d01784200f99fb268068a988d5f4bf8c3faad`.
Its 110-entry self-verifying `evidence.sha256` has SHA-256
`6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5`;
every entry verifies.

At baseline basis `1001`, the probe held a row lock on the exact authoritative
`pod-log-tail/<database-id>` row, whose revision was `3` and whose canonical
row SHA-256 was
`86a140d0753dbdfc4401b348aa9ff7e76b6a6213b49fe8ed00f9f4ecb20e9980`.
After submitting one asynchronous transaction, independent PostgreSQL evidence
identified one active writer backend blocked by the holder. The Future remained
incomplete; Peer basis/projection, fault-sentinel absence, and the byte-exact
root remained unchanged. Exactly one 151-byte nonrevisioned immutable row had
been inserted. Its parsed metadata was exactly `{:prev <baseline-tail>}`,
which makes it a strongly attributable transaction-append candidate rather
than the separate log-tree adoption path, whose descriptor write has no
prior-tail etag. Its Fressian payload was not decoded.

The harness reverified the complete PID/start-time/executable/argv identity and
deliberately killed only that Transactor, observing status `137`. PostgreSQL can
leave a server-side backend asleep in the lock manager after its JVM socket
dies, so the harness retained the root lock and terminated only the exact
blocked writer session already identified by PID, user, query, wait state, and
blocking-holder PID. That forced rollback before lock release. The Peer Future
then completed with exact unavailable rather than a transaction report. The
root row, basis `1001`, canonical SHA-256
`5097b63403add0ea605721300fed58d3f2afa85d14acba9adc0c2bbba8d4e81c`,
and fault-sentinel absence all remained exact. The immutable row remained as an
unreachable orphan candidate and is recorded rather than treated as published
state.

A fresh Transactor replayed 1,456 bytes through `tail-t 1001`; its normal
startup claim advanced the raw root revision from `3` to `4` without changing
the semantic database. A recovery transaction then returned at basis/event
`t 1003` and advanced the root from revision `4` to `5`. A second fresh
Transactor replayed 1,824 bytes through `tail-t 1003`; a fresh Peer reproduced
the exact final canonical SHA-256
`49494672a9c0f6a0860a4a74c5756ed40ae45f7634b962f9a922e440219f249b`,
the recovery sentinel, and fault-sentinel absence. PostgreSQL ended at 43 rows
and 11,032 stored value bytes, both restarted Transactors stopped through
bounded `SIGINT`, zero Datomic-user sessions remained, PostgreSQL reports
`shut down`, both ports are closed, and all three owned Transactor PIDs are
absent.

The failed predecessor `-v1` is retained only as diagnostic evidence. It found
the lock-manager/JVM-lifetime mismatch that required exact blocked-session
rollback in the corrected cut. The passing row proves no successful Peer result
for this exact prepublication root-update interruption; it does not prove the
separate crash window after root publication but before result delivery. That
window is covered by the next checkpoint; full licensed-oracle transaction
equivalence remains open.

The complementary post-publication/pre-result acknowledgement cut passed at
`/tmp/datomic-recovered-pair-ack-postpublication-v4`. It uses repository runner
SHA-256
`423e1c3e705727eddc2f02b212fc8cc563388706a6389a3a4da5ce39cd9b9dd8`
and acknowledgement-probe SHA-256
`d3c8192827d0e8718aa67c1cf39c060e83d9aadfdf319e3c8eec0e2a458295cf`.
Its self-verifying `evidence.sha256` has SHA-256
`d8ac5e5314603c1bca54aa8a773d60a7eec136e29066b4d220d110e1591374bf`;
every entry verifies.

At baseline basis `1001`, the probe submitted one asynchronous CAS plus a
unique sentinel while the authoritative root writer was blocked. The harness
proved the Future incomplete, verified the exact Peer PID/start time/argv, and
froze that Peer with `SIGSTOP`. It then terminated only the recorded PostgreSQL
holder backend, observed the authoritative root advance from revision `3` to
`4` while the Peer remained stopped, reverified the exact Transactor identity,
and killed it with status `137` before result delivery.

After a recovered Transactor restart, the same Peer was resumed. Its bounded
sync saw 44 exact unavailable outcomes and succeeded on attempt 45; the
original Future returned a report at basis `1003`. History contained exactly
one baseline-kind retraction and one published-kind assertion at `t 1003`, and
the unique fault sentinel existed only at that `t`. The CAS itself is a
re-execution guard: a second execution would conflict rather than silently
duplicate the effect. A second recovered Transactor restart and fresh recovered
Peer reproduced the exact canonical SHA-256
`ac4cc27c950b957db877cd2c30775e3dbb29b7a31ca3830f0a6c9e2073f12dbe`.
The post-publication result and fresh-audit result independently hash to
`69a2eb5cb0bd52c1d458e1224130e0cd5bf1481c20e257c1388f415e0c166e6e`
and
`2196976c85a9aaba6f1347b5dfe6fd72e503a0d6929353d2216d4c5619f28dd1`.

All owned processes and Datomic-user PostgreSQL sessions were absent after
cleanup, PostgreSQL reports `shut down`, and the service ports are closed. The
run deliberately records full licensed-oracle equivalence as `NOT_RUN`: it
closes the recovered-pair acknowledgement window, not the separate oracle row.

The transport-only predecessor passed at
`/tmp/datomic-recovered-pair-transport-v2`. It uses repository runner SHA-256
`70be908febf3487e41d7c84dd9583e729c3bc51e6ff819ea14c7f63a8e348691`
and transport-probe SHA-256
`24ebf30a78088a5720737d1c7ac5c3a3fc91b16f130cbefdc10f6f276f645a9a`.
Its self-verifying `evidence.sha256` has SHA-256
`87eaf2e129aa5d3ec12b4e84a9c3143e623b21767a00ee0c3916ea3c037383f6`;
all 123 entries verify.

After fresh-process adoption at `tail-t 1066, index-t 1066`, the harness held
the exact owned Transactor PID in `SIGSTOP` for 20 seconds. Zero-argument sync
on the same recovered Peer connection returned exactly
`:cognitect.anomalies/unavailable`. After verified `SIGCONT`, that connection
recovered on attempt 44 after 43 bounded unavailable retries, synced at basis
`1066`, committed one `:db/doc` sentinel, and uniquely read it at basis `1099`.
A new recovered-Peer JVM then saw basis `1099` while preserving database id,
the 96-row Stage 2 view, and all four augment semantic hashes. The candidate
boundary contained 535 entries, reported recovered Peer/core2 source protocol
`file`, exposed no Peer/core2 AOT, and found zero forbidden implementation
entries. All three Transactors stopped by bounded `SIGINT`, PostgreSQL reports
`shut down`, and host ports `54363` and `55463` are closed.

The first transport-v1 attempt failed before Datomic execution because the
network-restricted sandbox denied PostgreSQL's localhost bind. Cleanup status
was zero and the cluster is shut down; it is retained as harness diagnostics,
not runtime evidence.

The persistent-index predecessor passed at
`/tmp/datomic-recovered-pair-index-v5`. It used the repository gate at SHA-256
`19af9096c7aba455b6c7efce7bbfd345732c4b9ccb6462c887e75bcdff693427`
and its self-verifying `evidence.sha256` has SHA-256
`c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`.
The manifest verifies in full.

After seed/restart and the augment commit at basis `1066`, the recovered Peer
requested an index at `t 1066` and received both acceptance and reported
`index-t 1066`. Persistent publication grew PostgreSQL from 42 rows and 18,993
stored value bytes to 86 rows and 34,597 bytes. A third freshly staged
Transactor then loaded `tail-t 1066, index-t 1066` with zero log replay bytes.
Its recovered-Peer snapshot retained the same database id, basis, 96 logical
rows, and all four semantic hashes; the pre/post-adoption fingerprint is
byte-identical at SHA-256
`a9bcda0771d2c1ec7f6b63df54d964ad73220d475306a59312308a7fde2c8347`.
The run stopped all three Transactors by bounded `SIGINT` and shut down its
PostgreSQL instance. A host-level cleanup also stopped three leaked PostgreSQL
instances from earlier disposable diagnostic runs; no associated service port
remains open and their evidence directories were preserved.

The index-v5 run is the persistent-index predecessor to transport-v2. The
earlier current-source v3 run below remains the main-path predecessor. It
proved log replay/adoption but not persistent-index construction, and is now
superseded for the Stage 6 publication claim by v5.

`transactor/scripts/validate-postgresql-vertical-slice.sh` now reproduces this
sequence from a fresh current-source Peer build and a freshly prepared
Transactor runtime. The earlier executing run passed at
`/tmp/datomic-recovered-pair-live-v3`; the script SHA-256 is
`3544bdb504ab7926359cbfe366c3bcbc8c85ad14292f68d1e4827cc228e88639`.
The run's self-verifying `evidence.sha256` has SHA-256
`100245c7dbb8639ea4bbc8ad46594818bfb9909e6aae75a3b79c1b26263d24bf`
and verifies in full from the run root.

This run consumes the freshly rebuilt structural candidate list at
`/tmp/datomic-transactor-recovered-pair-current-v2/evidence/candidate-classpath.tsv`,
SHA-256
`ba0f0c6d56fa59e0060b6f662dc8be95cba766a4519839cff2fc232f1b85e4b1`.
That structural run loaded all 272 Transactor/core2 namespaces (271 cold and
one documented order-dependent) and recorded evidence-manifest SHA-256
`5490edf69d9ae86a1d78edf9644e1196ebf644bbea649c022e83b141f58b5bc8`.
Surfaces were deliberately not executed and remain an open Stage 1 boundary.
The v3 247-row source manifest is SHA-256
`6f27a4259ea02bb3eba6214d44b7c155d0d3dda1128a8d0be5301c2d00257786`.

The gate does not trust historical runtime directories. It rebuilds the Peer
and requires byte identity with the supplied canonical artifact, unpacks a
runtime derivative with both licensed JKS resources removed, snapshots the
current Transactor-owned roots, rebuilds candidate resources, regenerates the
272-row source inventory, and seals every candidate runtime file. The live
runtime seal SHA-256 is
`b821ed9c68a20f3f21baeb7f22b727f6caa176ef9dfadecd2ebab40647db7403`;
the sealed runtime-membership ledger is
`ab1c42d424061a8ed309e550f5092ffb5cc03711c9c80b4a000d09418c244236`.
The 535-entry Peer and 537-entry Transactor classpaths contain zero original
Peer, Transactor, core2, or Nano implementations, exactly one sanitized Nano,
and no licensed key/trust bytes. Separate origin probes passed for all 142 Peer
and 272 Transactor/core2 namespaces. The resolved Peer and Transactor
classpath-ledger hashes are respectively
`0cb5a2b425b0a979a0baf37272393184eee22b25e369a541b4185fbc2ee67935`
and `5d009a86b73ec4c926fb2f420598479986becf3e3d5cb172770869ddc47fdbc8`.
The gate also copies the focused runtime validator into the sealed candidate
inputs and executes it before starting PostgreSQL; that validator's SHA-256 is
`9e661bd3211dd3c483675d08138421d4255f97ae0cb1311f5d7adbe731c66a42`.

The executing result is:

- seed basis `1001`, 64 logical rows, then an exactly equal snapshot from a
  fresh Transactor and fresh data directory;
- 37,372 positive catchup bytes at `tail-t 1001, index-t 66`;
- augment basis `1066`, 96 logical rows, then another exactly equal snapshot
  from a third fresh Transactor and fresh data directory;
- 64,032 positive catchup bytes at `tail-t 1066, index-t 66`;
- PostgreSQL growth from 0 rows/0 value bytes to 41/15,254 and then
  42/18,996, with five revisioned rows at both commit points; and
- three bounded `SIGINT` shutdowns with no `SIGTERM` or `SIGKILL`, followed by
  closed service ports and PostgreSQL control state `shut down`.

The seed and augment content hashes match the earlier strict manual run. The
new database id is
`recovered-pair-dad95659-f007-463c-8dd5-f1a7d9b4d63f`; it remains stable across
both commits and both restarts. Independent rechecks passed the evidence
manifest and both exact fingerprint comparisons. The seed and restart
fingerprints are byte-identical at SHA-256
`071b6799a2fbf4e70d25787d1cc0a5819eba73850bd086dc74a37bad34d00b0e`;
the augment and final-restart fingerprints are byte-identical at
`a5186eb24ddc24d34c0f2615953daa05c5bb65acc3e6618fe9ad453bb8f0171f`.

The v1 and v2 executing runs remain useful historical main-path evidence. v1
predates the checked-in `compare-byte-arrays` source correction. v2 used the
corrected source, but its summary marker did not itself execute the focused
runtime validator. v3 closed that evidence gap and was then superseded as the
current-source promotion boundary by v5.

Before the executing gate, a no-service run passed at
`/tmp/datomic-recovered-pair-gate-dry-v7`, including the sealed focused runtime
probe. Its evidence-manifest SHA-256 is
`94cc29daebbedec4123d712f31f127bacad449df6d5e620014dbab79095eed0c`.
Use the script's `--help` output for the complete fail-closed input and
disposable-service contract.

## Historical manual candidate boundary

Before the repository gate existed, the strict manual evidence run was
`vslice8`. Its Transactor classpath is recorded at
`/tmp/datomic-transactor-structural-canonical-load/evidence/candidate-classpath.tsv`:

- manifest SHA-256:
  `f3fd44b1b1e704e0cae6f245ae9fe20f42607d2647d50f765f4d34900ab85f9d`;
- 537 entries;
- zero licensed `peer-1.0.7277.jar`,
  `datomic-transactor-pro-1.0.7277.jar`, or `core2-1.0.140.jar` entries;
- zero original Nano implementation entries; and
- exactly one sanitized Nano derivative.

The recovered Peer artifact was
`/tmp/datomic-stage2-candidate-v4/datomic-rev-peer-1.0.7277-source.jar`, SHA-256
`dd43d538f3a5caf1d107028760c34720999430f4442f62a57e2f269401c223a7`.
The pre-existing Peer manifest at
`/tmp/datomic-stage2-orchestrator-v4-live/candidate-classpath.tsv`, SHA-256
`e589990031c2fa22e269665efc6cf0465b5431e863d7fd971f0b16580756e79d`,
contained the original `nano-impl-0.1.325.jar` at position 486. The strict run
therefore resolved a new 535-entry Peer path list that replaced that entry with
`/tmp/datomic-transactor-structural-canonical-load/sanitized-nano/nano-impl-0.1.325-sanitized.jar`,
SHA-256
`08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f`.
The newline-delimited resolved path list had SHA-256
`6a8cb5c3ca3aaacb83ef8d4fc9aa1063c833e0129ab3cf8b5cdbcb4869fc46ca`,
with zero forbidden implementations, one recovered Peer, and one sanitized
Nano.

Third-party libraries still come from the licensed distribution's `lib/`
directory. They are external dependencies, not recovered Datomic
implementations. This result does not claim a distribution-independent build.

The earlier `vslice7` path completed seed, restart, augment, and a second
restart, but its Peer classpath retained the original Nano implementation. It
is useful behavioral evidence only and is not part of the strict promotion
claim.

## Historical manual clean run

The disposable PostgreSQL catalog was `datomic_goal2_vslice8`, the recovered
Transactor transport port was `54339`, and PostgreSQL used port `55439`. The
standard SQL storage table was provisioned externally as required by the
existing PostgreSQL harness:

```sql
CREATE TABLE public.datomic_kvs (
  id text PRIMARY KEY,
  rev integer,
  map text,
  val bytea
);
```

The recovered Peer seed returned database id
`goal2-vslice8-6ba13bb3-985d-4bef-8a31-4b607cf0dfa6`, basis `1001`, and 64
logical rows. Its hashes were:

| Observation | SHA-256 |
| --- | --- |
| datoms | `369a4b5be974de88053bb310a7ac071be34602175308313c2500732af728d3be` |
| history | `369a4b5be974de88053bb310a7ac071be34602175308313c2500732af728d3be` |
| logical value | `2795bae60836639c1202a80a9ed0f0a6f521119cad7f013001eb50218e4a307d` |
| rows | `1e47a0057b7c78538d595272b4e736ac31044746435e3797e48ac925246397ca` |

After seed, PostgreSQL contained 41 KV rows and 15,250 stored value bytes,
including five revisioned rows, a log-tail row, and an index-root reference.
The first process was stopped and a fresh recovered Transactor was started over
the same catalog. Its log reported catchup of 37,372 bytes from
`tail-t 1001, index-t 66`, followed by zero remaining bytes at
`tail-t 1001, index-t 1001`. A strict recovered Peer snapshot then reproduced
the seed database id, basis, row count, and all four hashes exactly.

The basis-1001 PostgreSQL directory was copied to
`/tmp/datomic-goal2-recovered-smoke/runtime/postgres-data-vslice8-basis1001`
before the continuation. Another strict snapshot first re-established the
same seed marker. The recovered Peer then submitted the post-restart augment
transaction. It returned basis `1066`, the same database id, and 96 logical
rows:

| Observation | SHA-256 |
| --- | --- |
| datoms | `a2b126a6ce783b95c66388f5359eded7c53fb7047105a2c653b9953c777eaa3c` |
| history | `18bbcaa546a670c66046594ecd9d914583e7f62c46eb6ef24356c5f513b2d8fc` |
| logical value | `c1960691a114d43de53d1c8511d3174dbb79b6b1bed89c9b1938656e66663e57` |
| rows | `32475f1570c7fc43602f84c280c8ef411fb656aec687819e5b0abc17cc644c06` |

PostgreSQL then contained 42 KV rows and 18,992 stored value bytes. The
Transactor was stopped, a third fresh recovered Transactor was started over
the same PostgreSQL catalog, and it replayed 64,032 bytes from
`tail-t 1066, index-t 66`. A final strict recovered Peer snapshot reproduced
basis `1066`, 96 rows, the database id, and every augment hash exactly.

The detached test launch inherited `SIGINT` as ignored; the verified process
stopped within two seconds on `SIGTERM`. PostgreSQL then stopped normally and
`pg_controldata` reported `shut down`. The live disposable data directory at
`/tmp/datomic-goal2-recovered-smoke/runtime/postgres-data` now contains the
basis-1066 state. `/tmp` evidence is not a permanent reproduction package.

## Defects exposed by the runtime path

The slice has already paid for itself by finding recovery defects that broad
surface comparison did not make operationally obvious:

| Recovered area | Runtime symptom | Generic recovery cause |
| --- | --- | --- |
| `datomic.future/filling-promise` | startup `StackOverflowError` | named function shadowed its captured outer function |
| `datomic.memory-size` | startup value loss | value-bearing object-array loop was followed by synthetic `nil` |
| `datomic.db/filter-retractions` | seed stopped while filtering history | lexical branch value was lost across dead `ATHROW` padding |
| transaction processor argument loop | transaction lacked its processor id | value-bearing loop was classified from the loop entry instead of its continuation |
| `datomic.update/swap-xf!` | transaction path returned `nil` and failed | same generic value-loop continuation defect |
| `datomic.common/root-cause` and `datomic.kv-cluster/root-cause` | empty-schema retry reporting threw a secondary NPE | recursive false branch was lost across dead `ATHROW` padding |
| `datomic.common/compare-byte-arrays` | broader recovered-behavior gate lost the equal-length comparison result | stale recovered source retained a synthetic trailing `nil` after a value-bearing loop |
| `datomic.index/filter-nohist-pairs` and `datomic.index/separating-retractions` | the candidate published a malformed root whose EAVT stopped before new entity data | stale source truncated the false branch at dead padding instead of preserving the lexical-region continuation |

Each confirmed family has a focused regression. The current decompiler
validator passes, including the exact AOT-shaped recursive `root-cause` and
`compare-byte-arrays` fixtures, and
`transactor/scripts/validate_runtime_regressions.clj` passes against the strict
candidate classpath. The relevant current hashes are:

- generic decompiler `ast.clj`:
  `d5a1f337486adf1bf375746cb752844997d0f6622bb40f106d0855f2f64b1cd8`;
- decompiler validator:
  `a1891d4b82a27f17f7b92bb0c8313155bb9a5efd69ab9f383137f8d814899aa6`;
- focused runtime validator:
  `ec68238e3cffbb3bc4573c1897a00cf67dc8d1227c3ee2b465e4b6b7278886e9`;
- recovered `datomic.common`:
  `4c2ec9ffa19f227dbc39352bd9a1f0d77c19a75b01325ff1e08f074dcaa553d3`;
- recovered `datomic.kv-cluster`:
  `ca7db52657a5dbec8d4b3f38a15ab03686a0c90785314228aba2358796ab6b67`.

Exact Transactor AOT confirms that both deepest-cause loops recur on
`.getCause` and return the current throwable when no cause remains. After the
repair, booting against a catalog without `datomic_kvs` reports PostgreSQL's
actual `relation does not exist` error and retries normally. That establishes
schema provisioning as a harness precondition rather than another recovered
runtime defect.

The `compare-byte-arrays` repair is bytecode-constrained rather than a
function-name special case. Exact AOT class
`datomic/common$compare_byte_arrays.class`, SHA-256
`23e34d076e57e17dce2947ffa8a20b000cfe888d55cc512ee7059f95bab9f4fe`,
enters the loop at bytecode offset 37, reaches its body continuation at 114,
and follows one forward `goto` to the sole `LRETURN` at 120. The generic
loop-recovery logic already represented that continuation correctly; the
checked-in source was brought back into agreement, and the exact-label
decompiler regression plus the signed-byte/equal/prefix behavior matrix pass.
The v3 PostgreSQL gate then ran that sealed focused validator and re-established
the complete main path from the corrected current source.

The persistent-index defect was similarly constrained rather than patched by
name. Exact AOT control flow for both index functions recurs past matched
retract/assert pairs and returns lazy continuation values from the alternate
branches. The generic lexical-region repair already expresses that rule and a
fresh decompile reconstructs both complete bodies. The stale checked-in
`datomic.index` was replaced with those AOT-derived forms at SHA-256
`93dedc04c7a897b69d6b5f0523c8edcd8b62d407d97d9284ad9f7db4fb9994a2`.
One focused regression verifies continuation past a no-history pair, retention
of the tail, capture of historic retract/assert pairs and unmatched retracts,
and exclusion of no-history values from history. It passes inside v5. The
updated 247-entry source-manifest file has SHA-256
`d1dce5d974827ccc8b86714e7a3569c34a61ed0e034a69ea4e23b9a1e56ca83f`
and every entry verifies.

## Honest boundary and next probe

- The HA gate proves three bounded graceful `SIGINT` shutdowns plus one
  conflict-driven stale-active self-fence, exact PID/argv/start-time ownership,
  closed ports, and final PostgreSQL shutdown. The separate missing-schema
  gate proves bounded injected-startup cleanup while the candidate remains
  owned, PostgreSQL remains responsive, and the service port never opens.
- The v5 request at `t 1066`, PostgreSQL growth, and zero-replay fresh restart
  prove persistent-index scheduling, publication, and root adoption for the
  deterministic recovered-pair workload. Automatic threshold scheduling and
  interrupted index construction remain failure/performance extensions, not a
  gap in the primary publication path.
- The fail-stop takeover and stale-active conflict/self-fence row is closed.
  The focused storage gate also closes PostgreSQL index-ref/log-root CAS
  rejection while preserving the winner. The focused transaction gate closes
  stale-CAS/uniqueness rejection and concurrent accepted ordering. The paired
  fault-injected acknowledgement cuts close both sides of durable publication
  for the recovered pair. Delayed/partitioned writers, full licensed-oracle
  transaction equivalence, and the bounded no-writable-split-brain matrix
  remain open.

The Peer PostgreSQL Stage 2 and Stage 3 harnesses require an explicit,
content-addressed sanitized Nano input, preserve the 535-entry dependency
cardinality, record one `sanitized-nano-dependency`, and reject missing,
original, tampered, symlinked, duplicate, or mislabeled inputs. Their focused
dry-run and local validations pass. They remain licensed-external-Transactor
oracle harnesses; they are not substitutes for the recovered-pair gate above.

The focused harness evidence is:

- Stage 2 dry run: `/tmp/datomic-stage2-run.gpLiwSOa`, with 535 entries,
  one sanitized Nano role, and zero original Nano hashes;
- Stage 3 dry run: `/tmp/datomic-stage3-sanitized-dry.eFmleZN4`, with the
  same classpath invariants;
- Stage 3 four-probe local run:
  `/tmp/datomic-stage3-local-sanitized.5zPDF2ae`;
- Stage 2 runner SHA-256:
  `c0ef9a0703fc9435dca3bf238756920c4b12f3e0aa8a9cffce414bdb211ed0b7`;
- Stage 3 runner SHA-256:
  `2fed07f3a7f0b0eba4d558e0c095adfdbb297d511dd4bc18c9be425d6d9941fc`;
- Stage 3 local validator SHA-256:
  `fdab53cf5bd1ac1ab60cd596f64f59824ae64228c0e583d7f2c4c157b0c3a2f3`.

The main path and its current-source repository gate are green through stale-
CAS/uniqueness rejection, concurrent transaction ordering, persistent-index
publication, fresh-process adoption, same-connection transport recovery, the
first recovered-pair takeover/self-fence row, and one focused missing-schema
startup-failure row. The exact PostgreSQL index-ref and log-root rejection row
is also closed. Both recovered-pair sides of the durable
commit/acknowledgement edge are now closed. The next runtime boundary is an
in-flight transaction during active/standby takeover, interleaved with the
transaction/transport overlap cohort. The bounded surface run
separately proves 272/272 effective loads and 247/247 callable/class shapes;
the protocol family passes a focused fresh recovery, but integrated strict-
metadata promotion and exact-AOT acceptance remain open. The 117 Stage 2
overlaps and remaining HA failure boundaries remain required; none
justify another unconstrained source-residual pass.
