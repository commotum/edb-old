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
8. a second durable PostgreSQL/log commit; and
9. another fresh-process restart with an identical recovered Peer snapshot.

This is a real vertical-slice milestone, not Goal 2 completion. Failure
matrices, persistent-index publication, transport interruption, and HA remain
open.

## Repository-owned executing gate

`transactor/scripts/validate-postgresql-vertical-slice.sh` now reproduces this
sequence from a fresh current-source Peer build and a freshly prepared
Transactor runtime. The current executing run passed at
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
The 247-row current-source manifest is SHA-256
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
runtime validator. v3 closes that evidence gap and is the current-source
promotion boundary.

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

Each confirmed family has a focused regression. The current decompiler
validator passes, including the exact AOT-shaped recursive `root-cause` and
`compare-byte-arrays` fixtures, and
`transactor/scripts/validate_runtime_regressions.clj` passes against the strict
candidate classpath. The relevant current hashes are:

- generic decompiler `ast.clj`:
  `d5a1f337486adf1bf375746cb752844997d0f6622bb40f106d0855f2f64b1cd8`;
- decompiler validator:
  `8bf72f48b5f04769e1fc6c8eff6d9b2b98aabb995050239e384e9a469c13e333`;
- focused runtime validator:
  `9e661bd3211dd3c483675d08138421d4255f97ae0cb1311f5d7adbe731c66a42`;
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

## Honest boundary and next probe

- The repository gate now proves three bounded graceful `SIGINT` shutdowns,
  exact PID/argv/start-time ownership, closed ports, and final PostgreSQL
  shutdown. Injected-startup-failure cleanup is still not proven.
- The `index-t 66` to `1001` observation proves restart replay/adoption. It
  does not yet prove the complete persistent-index scheduling and publication
  machinery.
- SQL CAS/root rejection, transaction rejection/concurrency, acknowledgement
  under injected failure, transport interruption, and HA/fencing remain open.

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

The main path and its current-source repository gate are green, including the
confirmed `compare-byte-arrays` repair. The primary workstream now returns to
the deferred exhaustive Stage 1 exact-source/surface boundary and the 117
Stage 2 overlaps. After that, the next service probe should be
injected-startup cleanup, exact SQL CAS/root rejection, transport interruption,
or active/standby takeover—not another unconstrained source-residual pass.
