# Recovery validation record

Validated artifact: Datomic Pro `peer-1.0.7277.jar`.

```text
SHA-256 cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba
```

## 1. Artifact closure

The deterministic ASM inventory accounts for every peer class without loading
or executing analyzed classes.

| Measure | Exact value |
|---|---:|
| Classes | 5,517 |
| Fields | 38,718 |
| Methods | 22,014 |
| Instructions | 850,016 |
| Calls, including 26 `invokedynamic` | 169,882 |
| Field accesses | 107,220 |
| Instruction literals | 191,255 |
| `ConstantValue` field literals | 10 |
| Resources | 10 |
| ASM warnings | 0 |

Source provenance is exhaustive:

| Provenance | Classes | Recovered source paths |
|---|---:|---:|
| Clojure AOT | 5,470 | 142 |
| Handwritten Java | 47 | 43 |
| Unknown | 0 | 0 |

Classfile majors are 4,960 Java-5-era classes (49), 510 Java-8 classes (52),
and 47 Java-11 classes (55).

Two complete inventory runs were byte-for-byte identical. The compact baseline
and scanner live under `tools/bytecode-inventory/`; occurrence tables too large
for this research repository can be regenerated deterministically.

All 10 non-class entries are also recovered under `resources/` at their exact
JAR paths and bytes. This includes both transactor JKS files used directly by
`datomic.connector`, the AWS EDNs, the core2 manifest, `data_readers.clj`, the
embedded POM, manifest, copyright, and version resource. The source-only sweep
now rejects a missing, relocated, or byte-different resource before loading any
namespace.

## 2. Distribution and dependency closure

The distribution index covers 533 companion JARs and 187,068 distinct class
names. It records 544 duplicate names. The peer overlaps the shipped
`core2-1.0.140.jar` on all 510 core2 classes, and all 510 pairs are
byte-identical.

The embedded peer POM and distribution POM are byte-identical. Of 33 direct
dependencies, 29 resolve by exact artifact/version filename, two resolve by
artifact fallback because a newer compatible JAR is shipped, and two
`provided` dependencies are absent:

- `org.infinispan:infinispan-client-hotrod:5.1.2.FINAL`
- `couchbase:couchbase-client:1.0.3`

Every unresolved primary-bytecode owner is one of five Infinispan types,
accounting for seven method calls and one field access. There are no Couchbase
references in peer bytecode. `datomic.kv-hotrod` compiles/loads with the exact
declared Hot Rod dependency and matching core JAR. The included stubs expose
only the referenced API surface for offline compile/load validation.

## 3. Clojure reconstruction

The final `latest35` and independent `latest36` post-repair runs each produced
142 successes and zero failures. Their 142-file source trees are byte-for-byte
identical. A sorted content manifest for each has SHA-256:

```text
ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be
```

All files pass a non-evaluating reader check. The final surface contains:

- 1,975 `defn` forms;
- 79 protocols;
- 23 bytecode-signature-derived plain interfaces;
- 25 records;
- 86 types;
- zero explicit `BROKEN DECOMP` markers;
- zero empty generated `AFunction`/`RestFn` constructor placeholders;
- zero leaked generated `$reify` or `.proxy$` class references.

The analyzer reports 2,906 definitions: 2,631 public and 275 private. It emits
3,533 call-edge rows, 2,798 reference-edge rows, 1,722 distinct keywords, and
273 heuristic configuration/SQL/URI strings. Its normalized corpus record is
`reports/source-index/corpus.edn` (SHA-256
`0c427dc3bce814f69fc7f342d5872929a3214c4f0bd02c13fbd1a1d107bb9f9c`).
Analyzer gap count is zero under its explicit failed-marker contract.

## 4. Source-only compile/load closure

Every recovered namespace was required in its own fresh JVM. Result:

```text
PASS 142
FAIL   0
```

The runtime classpath used recovered Clojure, 47 classfiles compiled from the
43 recovered handwritten Java files, recovered resources, 532 shipped library
JARs after excluding `core2`, and bytecode-derived compile-only declarations
for the five referenced Infinispan types. Neither the original peer JAR nor
core2 JAR was present. This prevents an AOT fallback from satisfying a source
failure. The stubs are validation aids, not runtime Hot Rod implementations.

The sweep includes direct passes for `datomic.query.support`, `datomic.query`,
`datomic.peer`, and `datomic.kv-hotrod`. Exact inputs, hashes, warnings, and the
142-line proof are in `reports/compile-audit/`.

The final sweep produced 318 unique compiler-warning texts: 306 reflection, 10
primitive-local recur, and two auto-boxing texts. Across the isolated processes
these repeated 6,898 times. They did not prevent compilation/loading but remain
relevant to performance and source fidelity.

## 5. Exact JVM and runtime surfaces

Fresh peer-free AOT builds were compared with the original Peer classfiles at
descriptor level:

- all 65 primitive `invokePrim` methods across 61 function classes match in
  descriptor, access, exceptions, superclass, and interface set (34 return
  `long`, one returns `double`, and 30 return `Object`);
- all 86 reconstructed `deftype` classes match, including 301 non-static
  fields, 86 constructors, and all 682 original methods;
- all 25 record classes match, including primitive record fields and
  constructors; their generated `getBasis` values also match all 85 authored
  field names, field order, and metadata exactly;
- all 102 generated interfaces match exactly: 79 protocol interfaces and 23
  restored `definterface` classes.

The only additional type method is the JDK 21 synthetic bridge
`TransposedData.reversed(): SequencedCollection`; it is a compiler/JDK artifact,
not a recovered-source ABI omission. Compiler cache field numbering and raw
interface-table order are likewise not authored ABI.

The checked-in namespace-surface comparison loads original and recovered forms
in separate JVMs and matches all 142 namespaces. It compares Var names, flags,
arity/root kinds, primitive function interfaces and methods, and authored class
interfaces, instance fields, constructors, and methods. It excludes only
source-evaluation proxy scratch Vars, static compiler caches, and
synthetic/bridge methods.

Focused behavior probes pass for reflective argument preservation
(`query.support/toArray` and four `stats` calls), foreign same-class receiver
reads (`Datum` wrappers, `EntityMap`, and `Db.eq`), nested-reify outer receiver
access in `TransposedData` sub-lists and iterators, character literals,
namespaced schema-index lookups, source-authored record keys, and terminal
`ExceptionInfo` throws. The earlier `counted-seq` probe also matches sequence,
count, metadata, equality, lookup, containment, and sub-list behavior.

The durable end-to-end parity validator runs original and recovered
implementations in independent JVMs and compares one canonical result
byte-for-byte. It covers in-memory database lifecycle, six-attribute schema
installation, seed/update/CAS/retract transactions, Datalog aggregates,
inputs, scalar results and rules, entity/touch/reverse references, pull and
pull-many, EAVT/AVET/range/seek access, history/as-of/since, and pure `d/with`.
Both results have SHA-256
`228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`.
The recovered parity JVM excludes the original peer and core2 AOT classes.

## 6. Handwritten Java

The 43 handwritten sources compile together into the exact expected 47
top-level/nested classfiles. Two CFR type-erasure repairs were necessary:

- `datomic.impl.lucene.ClusterDirectory.listAll`: cast `Collection.toArray` to
  `String[]`;
- `datomic.impl.lucene.DirectoryRef.listAll`: cast `Set.toArray` to `String[]`.

No other handwritten Java compile errors remain. A classfile-surface comparison
also matches all 47 class names, 108 fields, and 279 methods by descriptor,
signature, access, and declared exceptions. This is exact structural evidence,
not a claim that method bytecode is reproduced instruction-for-instruction.
`scripts/compare-handwritten-java-surfaces.sh` reproduces that comparison.

The other 5,470 CFR files are forensic Java views of Clojure-generated classes;
they are not the preferred rebuild surface and are superseded by `src-clj/`.

## 7. Async/IOC structural audit

The detailed async audit covers all 16 recovered IOC machines and all 155 state
arms:

- bytecode and recovered switch labels match;
- per-arm `aset-object` effects match the CFR control-flow reference;
- 114 captured thunk definitions and 114 calls match with zero orphans;
- channel calls are exactly `take!` 23, `put!` 6, `return-chan` 16,
  `close!` 4, and channel-closed error 6;
- IOC locals are `captured_bindings` 32, `old_frame` 32, and `c__` 44;
- numeric-suffixed surrogate references: zero;
- all eight related namespaces source-load.

This is a detailed structural/compile audit, not a proof of equivalence under
all possible concurrency schedules.

## 8. Final defects closed

The final repair pass fixed concrete failures rather than suppressing them:

- exact reachability across jumps, switches, exception handlers, and throws;
- IOC state-arm and captured-closure reconstruction;
- protocol, interface, record, type, field mutability, method signature,
  primitive, array, case, dynamic-Var, and circular-Var recovery;
- reference-aware threading compactions and removal of unsafe destructuring,
  temporary, sequence, and `doseq` rewrites;
- bytecode-exact overload hints, including `Callable` scheduling;
- source-stable case constants and self-type references;
- proxy superclass/interfaces and constructor arguments from generated proxy
  bytecode;
- generated reify-class casts replaced by the stable method-bearing interface;
- reflective argument arrays retained through fixed-point sugar passes;
- captured outer receivers distinguished from nested-reify `this` bindings;
- foreign same-class `getfield`/`putfield` receivers preserved instead of being
  collapsed to lexical fields;
- mutable primitive field tags and assignment casts preserved, including the
  exact 11 mutable primitive fields and their constructor descriptors;
- primitive function argument and return tags persisted through namespace
  rewriting and pretty-printing, restoring all 65 Peer `invokePrim` methods;
- ordinary array-valued function arguments distinguished from the exact
  20-fixed-plus-`Object[]` `IFn` calling convention;
- numeric boxed-character constants restored as actual Clojure character
  literals;
- complete namespaced `KeywordLookupSite` expressions retained, restoring all
  63 namespaced sites instead of collapsing them to unqualified keywords;
- source-authored record field spelling and field `:tag` metadata decoded from
  static `getBasis` bytecode, restoring all 25 bases and 85 fields exactly;
- method-local AST state reset and reachable terminal `ATHROW` finalized,
  repairing `datomic.core2.anomalies/athrow` and the rejected-execution handler
  in `datomic.core2.thread`;
- peer resources restored at their exact classpath paths with byte-for-byte
  validation;
- qualified `clojure.core/in-ns` parsing in the bytecode inventory, with all
  142 names checked against the semantic namespace index.

Synthetic negative/positive regressions cover fixed-point argument retention,
ordinary array arguments versus true `IFn` variadic tails, primitive mutable
assignments, primitive function source round-tripping, same-class receivers,
nested-reify receiver alpha-renaming, boxed characters, namespaced keyword
sites, record basis spelling/metadata, and terminal throws.

## 9. Accuracy boundary

“Complete” in this repository means complete artifact accounting,
deterministic structural reconstruction, reader validity, Java compilation,
namespace compile/load closure, dependency accounting, exact checked JVM/runtime
surfaces, the targeted behavior and async/proxy checks above, and exact parity
for the documented in-memory API workload.

It does not mean literal recovery of the unpublished source or exhaustive
behavioral equivalence for every backend and schedule. AOT compilation erases comments, formatting, original
macro spelling, some metadata, and some local names. The validation does not
exercise every transaction, storage backend, distributed failure, security
configuration, or scheduling interleaving. Use original bytecode and a
disposable licensed Datomic system as the final authority for subtle behavior.
