# Datomic Pro 1.0.7277 reverse engineering

This repository is a bytecode-bound educational recovery of the local Datomic
Pro 1.0.7277 Peer and Transactor. The root tree recovers
`peer-1.0.7277.jar`; `transactor/` recovers
`datomic-transactor-pro-1.0.7277.jar`. Their source-owned runtime paths compose
through PostgreSQL without original Peer, core2, Transactor, or Nano
implementation fallback.

It is a research artifact, not a clean-room implementation or a redistributable
replacement for Datomic. Comments, original formatting, some macro surface
syntax, and names erased by AOT compilation cannot be recovered.

## Architectural boundary

The **Peer** is the application-linked query/client runtime. The separately
launched recovered **Transactor** owns the serialized write path and
coordinates Datomic's log and indexes. **PostgreSQL** is the durable authority
beneath that Transactor; it does not replace it. The primary recovered-pair
gate proves boot, transactions, log publication, persistent indexes, restart,
transport recovery, acknowledgement fault cuts, and bounded HA takeover using
only recovered implementation roots plus ordinary distribution dependencies
and a content-addressed sanitized Nano derivative.

Legacy Peer Stage 2/3 gates still use the licensed original Transactor as an
isolated external interoperability oracle. They are historical evidence, not
the current candidate. Licensed originals remain outside candidate classpaths
and are admitted only by explicit hash-bound structural/oracle boundaries.
The result is a study-quality reconstruction, not a redistributable Datomic
replacement or a claim of universal equivalence for every dormant facility.

## Closure status

| Check | Result |
|---|---:|
| Peer JAR SHA-256 | `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba` |
| Peer classes inventoried | 5,517 / 5,517 |
| Clojure AOT classes mapped | 5,470 / 5,470 |
| Reconstructed namespaces | 142 / 142 |
| Deterministic decompiler baseline regenerations | 2 / 2 byte-identical (`latest35`, `latest36`) |
| Deterministic decompiler baseline manifest | `ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be` |
| Current packaged Clojure input manifest | `186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1` |
| Handwritten Java classes/sources | 47 / 43 |
| Reader-valid project Clojure files | 143 / 143 (142 namespaces plus `data_readers.clj`) |
| Fresh-JVM source-only namespace loads | 142 / 142 |
| Runtime Var/authored-class surfaces | 142 / 142 |
| Record basis names/order/metadata | 25 records / 85 fields exact |
| Primitive fn JVM ABI | 65 / 65 `invokePrim` descriptors |
| Reconstructed types/records/interfaces | 86 / 25 / 102 exact ABI surfaces |
| Handwritten Java compilation | 43 sources -> 47 classes |
| Decompilation gap markers | 0 |
| Bytecode parse warnings | 0 |
| In-memory Datomic API parity | exact canonical result (`228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`) |
| Reproducible source artifact | 2 clean builds byte-identical (`bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`) |
| Exact unresolved compiler-warning inventory | 157 (`150` reflection, `6` primitive recur, `1` auto-boxing) |
| PostgreSQL backup/recovery matrix | PASS (full, incremental, corruption, interruption, retry) |
| Bounded concurrency/transport matrix | PASS (cancellation, lifecycle, 8×8 CAS, verified `SIGSTOP`/`SIGCONT`) |
| Conservative source-navigation pass | 142 namespaces / 2,906 definitions indexed |
| Recovered Transactor Clojure sources | 247 / 247 |
| Recovered Transactor Java | 46 sources -> 52 classes, exact ABI/normalized code |
| Recovered Transactor/core2 effective namespace loads | 272 / 272 |
| Recovered Peer + Transactor PostgreSQL core | PASS (transaction, log, index, restart, transport, acknowledgement) |
| Bounded HA authority | PASS (both concurrent descriptor-CAS schedules and credential-scoped partition/heal) |

The latest hardened adversarial final reruns used the unchanged artifact above.
Stage 1 recorded 1,230 hashed evidence files with manifest SHA-256
`531b2f2e9ce15e10bbe87ffcbfb5d8b884e7aacd6b20bcb4601b9f59bb5a960a`;
Stage 2 recorded 118 with
`25e5f821a84bbde27cb85a985a2728cffbe73509dfd2f7b70c115ed6ca2cf721`;
and Stage 3 recorded 88 with
`8f69d3fac2ab0d7d59f449db2c2e3489b292d24a123522a023754a94b01984f6`.
Those raw run directories are local evidence under `/tmp`, not durable
repository contents. The stage reports record their exact boundaries and
top-level hashes.

The source-only validation uses recovered Clojure, compiled recovered Java,
resources, and distribution dependencies. It deliberately excludes both
`peer-1.0.7277.jar` and the duplicate `core2` AOT JAR from the runtime
classpath. `datomic.kv-hotrod` is checked against either Datomic's declared
provided Infinispan 5.1.2 dependency or the included compile-only API stubs.

See `reports/recovery-validation.md` for the exact claims and caveats.
See `reports/stage-1-validation.md` for the canonical packaged-artifact
boundary and its complete validation gate.
See `reports/stage-2-validation.md` for the disposable PostgreSQL lifecycle,
backup, corruption, restore, and recovery matrix.
See `reports/stage-3-validation.md` for the fresh-JVM concurrency,
cancellation, connection-lifecycle, CAS-contention, and transport-fault matrix.
See `reports/stage-4-validation.md` for the bytecode-proven warning repairs and
the exact inventory deliberately left unresolved.
See `reports/source-guide.md` and `reports/stage-5-readability.md` for the
subsystem map, generated-source cautions, and conservative navigation pass.

## Repository map

- `src-clj/` — the 142 recovered Clojure namespaces plus `data_readers.clj`.
- `src-java/` — CFR evidence for all class files. The 43 handwritten sources
  are listed in `reports/handwritten-java-sources.txt`; the remaining files are
  low-level views of Clojure AOT output.
- `resources/` — all 10 canonical non-class Peer entries at their exact
  classpath paths, plus four noncanonical root aliases excluded from the
  artifact; the canonical entries include TLS key/trust stores and AWS/core2
  metadata.
- `reports/source-index/` — deterministic namespace, Var, dependency, call,
  reference, keyword, string, and class-closure indexes.
- `reports/source-guide.md` — subsystem entry points, execution flows,
  provenance boundaries, generated-source traps, and validation ladder.
- `tools/tools.decompiler/` — the repaired AOT Clojure decompiler source.
- `tools/bytecode-inventory/` — a deterministic, non-class-loading ASM scanner
  and a compact checked-in baseline.
- `tools/infinispan-compile-stubs/` — bytecode-derived compile-only declarations
  for the missing provided Hot Rod API. They are not runtime implementations.
- `scripts/` — regeneration, reader validation, Java compilation, corpus
  analysis, strict source-only namespace validation, runtime-surface comparison,
  focused recovered-behavior regressions, original-versus-recovered API parity,
  disposable PostgreSQL backup/recovery validation, and bounded concurrency and
  transport-fault validation.
- `transactor/` — all recovered Transactor Clojure/Java sources, isolated
  structural construction, the recovered-pair PostgreSQL runner, focused
  probes, and reports that bind runtime claims to recovered source.

## Reproduce the final Clojure recovery

The default distribution location from this nested repository is
`../../datomic/datomic-pro-1.0.7277`. The final-reproduction script verifies the
Peer hash, produces the deterministic 142-namespace decompiler baseline,
applies the checked-in hardening overlay for the 12 subsequently repaired
namespaces, and verifies the exact packaged-source manifest
`186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1`.
It refuses to overwrite a non-empty output directory.

```bash
DATOMIC_HOME=${DATOMIC_HOME:-../../datomic/datomic-pro-1.0.7277}
scripts/reproduce-final-clojure.sh "$DATOMIC_HOME" \
  /tmp/datomic-final-clojure
```

The raw baseline remains under `decompiler-baseline/`; the exact final tree is
under `src-clj/`, with `hardening-overlay.tsv` recording both sides of every
overlay. `scripts/decompile-clojure.sh` remains available when only the
historical deterministic decompiler baseline is wanted.

## Reproduce the build checks

Build the canonical thin recovered-source artifact twice and run every Stage 1
artifact gate:

```bash
DATOMIC_HOME=${DATOMIC_HOME:-../../datomic/datomic-pro-1.0.7277}
STAGE1_ROOT=/tmp/datomic-stage-1
JOBS=4 scripts/validate-stage-1.sh "$DATOMIC_HOME" "$STAGE1_ROOT"
ARTIFACT="$STAGE1_ROOT/build-a/datomic-rev-peer-1.0.7277-source.jar"
```

The artifact contains the 142 recovered Clojure sources, 47 classes freshly
compiled from the 43 handwritten Java sources, the ten exact Peer resources,
and six provenance records. It contains no Clojure AOT classes. Its build and
candidate runtime classpaths exclude the original Peer and duplicate core2 AOT
JARs; licensed Peer use is confined to explicit oracle subprocesses.

Compile the handwritten Java surface:

```bash
scripts/compile-handwritten-java.sh "$DATOMIC_HOME" \
  /tmp/datomic-handwritten-classes
```

Compare its complete class/field/method API surface with the original Peer
classfiles:

```bash
scripts/compare-handwritten-java-surfaces.sh "$DATOMIC_HOME" \
  /tmp/datomic-java-surface-validation
```

Load every recovered namespace in a separate JVM without proprietary AOT
classes:

```bash
JOBS=4 scripts/validate-all-namespaces.sh "$DATOMIC_HOME" \
  /tmp/datomic-source-validation
```

The latter creates only temporary compiled classes, library symlinks, and logs
under the supplied output directory. It first verifies every recovered resource
byte-for-byte against the peer JAR. Its Hot Rod stubs are sufficient only for
compilation/loading; never place them on a runtime Datomic client classpath.
The validation also runs focused regressions for reflective-call arguments,
foreign-instance field reads, nested-reify receiver capture, character
literals, namespaced keyword lookups, record keys, and terminal throws.

Compare runtime Var and authored class surfaces against the original AOT
artifact in isolated JVMs:

```bash
JOBS=4 scripts/compare-namespace-surfaces.sh "$DATOMIC_HOME" \
  /tmp/datomic-surface-validation
```

Run the original Peer and recovered source through the same deterministic
in-memory database workload in independent JVMs:

```bash
scripts/validate-end-to-end-parity.sh "$DATOMIC_HOME" \
  /tmp/datomic-end-to-end-parity
```

That wrapper first rebuilds the source-only validation classpath, then compares
one canonical result covering database lifecycle, schema and data transactions,
CAS/retract operations, Datalog aggregates/inputs/rules, entity and pull APIs,
EAVT/AVET/range/seek indexes, history/as-of/since, and `d/with`.

Regenerate the semantic source index:

```bash
scripts/validate-analyze-corpus.sh "$DATOMIC_HOME"

java -cp "$DATOMIC_HOME/lib/*" \
  clojure.main scripts/analyze_corpus.clj \
  src-clj \
  "$DATOMIC_HOME/peer-1.0.7277.jar" \
  /tmp/datomic-source-analysis
```

Regenerate the bytecode inventory:

```bash
tools/bytecode-inventory/run-datomic.sh \
  "$DATOMIC_HOME" \
  "$PWD" \
  /tmp/datomic-bytecode-inventory
```

## Reproduce the PostgreSQL gates

The PostgreSQL gates require Linux with readable `/proc`, the GNU `find`,
`readlink`, `sort`, and `timeout` behavior used by the scripts, and a
user-supplied PostgreSQL 16 installation. Stage 3 additionally requires
`SIGSTOP` and `SIGCONT`. `POSTGRES_ROOT` must name an extracted root from which
the scripts can infer `bin`, `lib`, and `share`; use the explicit
`--pg-bin-dir`, `--pg-lib-dir`, and `--pg-share-dir` options instead when that
layout is unavailable.

After the Stage 1 command above, choose four currently unused loopback ports and
run the complete gates. Omitting `--work-root` makes each runner create a fresh
temporary work root and print its location:

```bash
POSTGRES_ROOT=${POSTGRES_ROOT:?set this to a PostgreSQL 16 installation root}
SANITIZED_NANO_ROOT=${SANITIZED_NANO_ROOT:?set this to a new path outside the repository}
STAGE2_PG_PORT=${STAGE2_PG_PORT:-55436}
STAGE2_TRANSACTOR_PORT=${STAGE2_TRANSACTOR_PORT:-54340}
STAGE3_PG_PORT=${STAGE3_PG_PORT:-55437}
STAGE3_TRANSACTOR_PORT=${STAGE3_TRANSACTOR_PORT:-54341}

transactor/scripts/sanitize-nano-impl.sh \
  "$DATOMIC_HOME" "$SANITIZED_NANO_ROOT"
SANITIZED_NANO="$SANITIZED_NANO_ROOT/nano-impl-0.1.325-sanitized.jar"

scripts/stage2/validate-postgresql.sh \
  --datomic-home "$DATOMIC_HOME" \
  --artifact "$ARTIFACT" \
  --sanitized-nano "$SANITIZED_NANO" \
  --postgres-root "$POSTGRES_ROOT" \
  --pg-port "$STAGE2_PG_PORT" \
  --transactor-port "$STAGE2_TRANSACTOR_PORT" \
  --confirm-disposable DATOMIC_STAGE2_DISPOSABLE

scripts/stage3/validate-postgresql.sh \
  --datomic-home "$DATOMIC_HOME" \
  --artifact "$ARTIFACT" \
  --sanitized-nano "$SANITIZED_NANO" \
  --postgres-root "$POSTGRES_ROOT" \
  --pg-port "$STAGE3_PG_PORT" \
  --transactor-port "$STAGE3_TRANSACTOR_PORT" \
  --confirm-disposable DATOMIC_STAGE3_DISPOSABLE
```

The example port values are not reservations; replace any occupied value.
The Nano output root must be absent before the sanitizer runs. Both gates
replace the licensed Nano entry with that exact content-addressed derivative
and fail closed if the original, a symlink, or altered bytes enter the
candidate classpath.
These two legacy gates launch the licensed Transactor only as an isolated
external fixture. They remain useful Peer-oracle checks, but they are not the
recovered-pair acceptance path.

The recovered-Peer/recovered-Transactor gate is separate:

```bash
transactor/scripts/validate-postgresql-vertical-slice.sh --help
```

It rebuilds and verifies current candidate inputs, removes licensed JKS
resources from the Peer runtime derivative, supports a no-service preflight,
and executes focused or complete PostgreSQL paths for the recovered Peer and
Transactor. Its focused modes cover storage CAS, transaction ordering,
pre-/post-publication acknowledgement faults, concurrent takeover schedules,
and credential-scoped asymmetric partition/heal; `--help` is the authoritative
option list. See
[`transactor/reports/postgresql-vertical-slice.md`](transactor/reports/postgresql-vertical-slice.md)
for the retained proof and current completion boundary.

## What was repaired

The decompiler now reconstructs reachable exception/switch control flow,
protocols, records, types, 23 plain interfaces, dynamic Vars, mutable fields,
primitive and array hints, compiler-generated closures, IOC state machines,
case constants, circular Vars, proxies, and source-stable reify/interface
hints. Unsafe readability compactions that changed meaning were removed.

The final semantic repairs also preserve reflective argument arrays, distinguish
the current object from a foreign same-class field receiver, capture outer
receivers in nested reifies, retain and coerce mutable primitive fields, persist
primitive function argument/return metadata through source printing, and splice
an `IFn` array tail only for the exact 20-fixed-plus-`Object[]` descriptor.

The final runtime-driven pass additionally restores boxed character constants,
complete namespaced `KeywordLookupSite` expressions, source-authored record
field spelling and `getBasis` metadata, and methods whose only terminal is JVM
`ATHROW`. Those systemic fixes repaired 63 namespaced lookup sites, all 25
record bases, `datomic.core2.anomalies/athrow`, and the rejected-execution path
in `datomic.core2.thread`.

Two CFR casts were repaired in the handwritten Lucene directory sources. Those
were the only errors in the 43-source handwritten Java compilation surface.

Three later storage-driven repairs were also fixed from original
bytecode: the future wrapper now invokes its captured outer callable instead of
recursing through a decompiler-created self-name; object-array memory sizing
returns its completed long result; and `datomic.common/pfuture` submits the
captured function through the original `Callable` overload. Focused regressions
cover both success and failure paths. Historically, the `Callable` repair
reduced the reflection inventory from 306 to 305; no warning was suppressed.

Stage 4 subsequently repaired another 159 source warning sites across 11
namespaces, exclusively where instruction-level oracle evidence established
the correct source form. This removed 160 warning records and reduced the full
inventory from 317 (`305` reflection, `10` primitive recur, `2` auto-boxing) to
157 (`150`, `6`, and `1`, respectively). It also repaired
`datomic.common/compare-byte-arrays`, whose equal-length branch discarded its
computed result. The remaining 157 warnings are retained in an exact checked
inventory; see `reports/stage-4-validation.md`.

The prior recovery's direct proxy constructor and generated reify-class hint
were also replaced with source forms that can be recompiled. The recovered
`datomic.query.support/counted-seq` proxy matches the original AOT behavior on
count, sequence, metadata, equality, list lookup, containment, and sub-list
probes. Additional regressions cover the recovered query-support `toArray`
call, four statistics `.ident` calls, `TransposedData` sub-list/iterator access,
and equality/identity paths in `datomic.db` and `datomic.query`.

## Accuracy boundary

“Complete” here means complete artifact accounting, deterministic structural
reconstruction, reader validity, Java compilation, exact checked JVM surfaces,
source-only compile/load closure, and exact parity for the documented in-memory
API workload for this peer JAR. It does not mean literal recovery of Datomic's
unpublished source or proof that every operation is behaviorally equivalent
under every backend and schedule. The PostgreSQL lifecycle and selected backup,
corruption, restore, and interruption paths are covered by the Stage 2 gate.
Stage 3 additionally covers bounded promise/IOC/pool cancellation paths,
connection-release races, eight rounds of eight-way SQL CAS contention, and a
watchdog-protected transactor pause/recovery. Other storage backends, wider
concurrency schedules, security behavior, multi-transactor failover, and
broader distributed failures remain outside the demonstrated boundary. The 157
unresolved compiler warnings remain where this bounded audit did not establish
a unique source correction; they are exact recorded evidence, not load errors
or suppressed diagnostics.

The fuller subsystem and execution-flow map is `reports/source-guide.md`.
Useful starting points:

- Public API: `src-java/datomic/{Peer,Connection,Database,Datom,Entity,Log}.java`
- Data model and transactions: `src-clj/datomic/{db,transaction}.clj`
- Datalog/query: `src-clj/datomic/{datalog,query}.clj`
- Indexes: `src-clj/datomic/index.clj`
- Storage/log: `src-clj/datomic/{log,cluster,kv_store}.clj`
- Integrity tooling: `src-clj/datomic/integrity.clj`

The root `linux-install.sh` predates this work and is unrelated to the recovery
pipeline.
