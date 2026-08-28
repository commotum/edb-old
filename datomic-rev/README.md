# Datomic Pro 1.0.7277 reverse engineering

This repository is a bytecode-bound recovery of the local Datomic Pro
`peer-1.0.7277.jar`. The structural recovery is complete for that exact
artifact: every class is classified, every Clojure AOT namespace has recovered
source, all handwritten Java sources compile, and the recovered source tree can
be loaded without the original peer/core2 AOT classes.

It is a research artifact, not a clean-room implementation or a redistributable
replacement for Datomic. Comments, original formatting, some macro surface
syntax, and names erased by AOT compilation cannot be recovered.

## Closure status

| Check | Result |
|---|---:|
| Peer JAR SHA-256 | `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba` |
| Peer classes inventoried | 5,517 / 5,517 |
| Clojure AOT classes mapped | 5,470 / 5,470 |
| Reconstructed namespaces | 142 / 142 |
| Deterministic final regenerations | 2 / 2 byte-identical (`latest35`, `latest36`) |
| Deterministic decompiler baseline manifest | `ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be` |
| Current packaged Clojure input manifest | `186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1` |
| Handwritten Java classes/sources | 47 / 43 |
| Reader-valid Clojure files | 142 / 142 |
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
- `resources/` — all 10 non-class peer entries at their exact classpath paths,
  including the TLS key/trust stores and AWS/core2 metadata.
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

## Reproduce the Clojure recovery

The default distribution location from this nested repository is
`../../datomic/datomic-pro-1.0.7277`. The regeneration script verifies the peer
hash and refuses to overwrite a non-empty output directory.

```bash
scripts/decompile-clojure.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-clj-regenerated
```

The patched source directory is placed before the historical standalone JAR on
the classpath. A successful run reports 142 successes, zero failures, and then
performs a non-evaluating reader pass with `*read-eval*` disabled.

## Reproduce the build checks

Build the canonical thin recovered-source artifact twice and run every Stage 1
artifact gate:

```bash
JOBS=4 scripts/validate-stage-1.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-stage-1
```

The artifact contains the 142 recovered Clojure sources, 47 classes freshly
compiled from the 43 handwritten Java sources, the ten exact Peer resources,
and six provenance records. It contains no Clojure AOT classes. Its build and
candidate runtime classpaths exclude the original Peer and duplicate core2 AOT
JARs; licensed Peer use is confined to explicit oracle subprocesses.

Compile the handwritten Java surface:

```bash
scripts/compile-handwritten-java.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-handwritten-classes
```

Compare its complete class/field/method API surface with the original Peer
classfiles:

```bash
scripts/compare-handwritten-java-surfaces.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-java-surface-validation
```

Load every recovered namespace in a separate JVM without proprietary AOT
classes:

```bash
JOBS=4 scripts/validate-all-namespaces.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
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
JOBS=4 scripts/compare-namespace-surfaces.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-surface-validation
```

Run the original Peer and recovered source through the same deterministic
in-memory database workload in independent JVMs:

```bash
scripts/validate-end-to-end-parity.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-end-to-end-parity
```

That wrapper first rebuilds the source-only validation classpath, then compares
one canonical result covering database lifecycle, schema and data transactions,
CAS/retract operations, Datalog aggregates/inputs/rules, entity and pull APIs,
EAVT/AVET/range/seek indexes, history/as-of/since, and `d/with`.

Regenerate the semantic source index:

```bash
java -cp '/home/jake/Developer/datomic/datomic-pro-1.0.7277/lib/*' \
  clojure.main scripts/analyze_corpus.clj \
  src-clj \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277/peer-1.0.7277.jar \
  /tmp/datomic-source-analysis
```

Regenerate the bytecode inventory:

```bash
tools/bytecode-inventory/run-datomic.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  "$PWD" \
  /tmp/datomic-bytecode-inventory
```

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
