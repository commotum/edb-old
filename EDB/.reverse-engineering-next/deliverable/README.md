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
| Recovered-source manifest | `ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be` |
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

The source-only validation uses recovered Clojure, compiled recovered Java,
resources, and distribution dependencies. It deliberately excludes both
`peer-1.0.7277.jar` and the duplicate `core2` AOT JAR from the runtime
classpath. `datomic.kv-hotrod` is checked against either Datomic's declared
provided Infinispan 5.1.2 dependency or the included compile-only API stubs.

See `reports/recovery-validation.md` for the exact claims and caveats.

## Repository map

- `src-clj/` — the 142 recovered Clojure namespaces plus `data_readers.clj`.
- `src-java/` — CFR evidence for all class files. The 43 handwritten sources
  are listed in `reports/handwritten-java-sources.txt`; the remaining files are
  low-level views of Clojure AOT output.
- `resources/` — all 10 non-class peer entries at their exact classpath paths,
  including the TLS key/trust stores and AWS/core2 metadata.
- `reports/source-index/` — deterministic namespace, Var, dependency, call,
  reference, keyword, string, and class-closure indexes.
- `tools/tools.decompiler/` — the repaired AOT Clojure decompiler source.
- `tools/bytecode-inventory/` — a deterministic, non-class-loading ASM scanner
  and a compact checked-in baseline.
- `tools/infinispan-compile-stubs/` — bytecode-derived compile-only declarations
  for the missing provided Hot Rod API. They are not runtime implementations.
- `scripts/` — regeneration, reader validation, Java compilation, corpus
  analysis, strict source-only namespace validation, runtime-surface comparison,
  focused recovered-behavior regressions, and original-versus-recovered API
  parity validation.

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
under every backend and schedule. Reflection warnings remain where AOT erased
source-level hints; they are recorded compiler warnings, not load errors.
Subtle storage, security, distributed-failure, and concurrency behavior should
still be checked against bytecode and a disposable licensed Datomic
environment.

Useful starting points:

- Public API: `src-java/datomic/{Peer,Connection,Database,Datom,Entity,Log}.java`
- Data model and transactions: `src-clj/datomic/{db,transaction}.clj`
- Datalog/query: `src-clj/datomic/{datalog,query}.clj`
- Indexes: `src-clj/datomic/index.clj`
- Storage/log: `src-clj/datomic/{log,cluster,kv_store}.clj`
- Integrity tooling: `src-clj/datomic/integrity.clj`

The root `linux-install.sh` predates this work and is unrelated to the recovery
pipeline.
