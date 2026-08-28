# Stage 1 exact-source AOT gate

Date: 2026-08-28

## Boundary

This gate covers the 85 exact bundled namespaces plus the exact dependency
sources `datomic.query.support` and `datomic.specs`: 87 namespaces and 3,431
classes embedded in `datomic-transactor-pro-1.0.7277.jar`.

The compiler process excludes the original Transactor, Peer, and core2 JARs.
It uses Amazon Corretto 11.0.22.7.1, Clojure 1.11.4, the Transactor POM's
metadata-elision setting, the promoted source tree first on the classpath, and
532 hash-checked library inputs. The current integrated script substitutes the
hash-bound deterministic sanitized Nano derivative for the original
`lib/nano-impl-0.1.325.jar`; the other 531 entries remain distribution inputs.
That broad dependency classpath is an isolated validation input, not a
candidate-runtime dependency decision.

Each namespace is compiled twice, once per fresh JVM. The runner reads the
source namespace declaration, verifies its munged name against the ownership
manifest, normally requires declared dependencies while AOT emission is off,
then compiles only the target namespace into an isolated output directory.
This is necessary because a plain dependency-bearing `clojure.lang.Compile`
invocation also emits source-only dependencies; for example,
`clojure.core.async.impl.ioc-macros` emitted 1,522 classes instead of its owned
319-class closure without the preload step.

## Full-run result

The full run is retained at
`/tmp/datomic-transactor-exact-source-aot-validation-v2` for this workspace.
It made 174 fresh-JVM compilations.

| Check | Result |
| --- | ---: |
| Namespace compilations completed | 174/174 |
| Owned cardinality, build A | 3,431/3,431 |
| Owned cardinality, build B | 3,431/3,431 |
| Per-namespace cardinality | 87/87 |
| Initializer present in both builds | 87/87 |
| Deterministic class paths | 87/87 |
| Deterministic raw class bytes | 75/87 |
| Original class-name skeleton in retained v2 relation | 84/87 |

The retained v2 relation reports `DIFFER` for `clojure.core.async`,
`clojure.data.priority_map`, and `clojure.tools.analyzer.utils`. A later
post-run experiment claimed 87/87 after broadening the regex to the three
observed numeric forms (`__N`, `$evalN`, and `$inst_N__`), but that experiment
was not sealed and the broad `__N` rule also conflates generated class-node
IDs, macro gensyms, and fixed member slots such as `const__0`. It is therefore
not evidence for equivalence. Proxy hashes remain exact and are never
normalized.

The 12 raw-byte-nondeterministic namespaces and numbers of changed class files
between the two builds are:

| Namespace | Differing classes |
| --- | ---: |
| `clojure.core.async` | 105 |
| `clojure.core.memoize` | 17 |
| `clojure.data.json` | 2 |
| `clojure.data.priority_map` | 23 |
| `clojure.tools.analyzer.jvm` | 13 |
| `clojure.tools.analyzer.jvm.utils` | 2 |
| `clojure.tools.analyzer.passes` | 8 |
| `clojure.tools.analyzer.passes.jvm.annotate_host_info` | 3 |
| `cognitect.nano_impl.registration` | 22 |
| `liberator.representation` | 19 |
| `ring.core.protocols` | 2 |
| `ring.middleware.keyword_params` | 3 |

A representative `javap` comparison shows a generated closure constructor
receiving the same captured values in a different argument order, with the
corresponding local loads permuted. Raw-byte determinism remains diagnostic;
it is neither sufficient nor required for semantic equivalence.

## Integrated comparator status

`transactor/tools/CompareExactSourceAot.java` now records the non-debug
class ABI and instruction stream, including expanded stack-map frames, with
unknown attributes failing closed. Class paths must be regular files whose
namespace container, path, and internal name agree. Interfaces and declared
exceptions retain their original order. Typed class, member, state-slot, and
local-node mappings cover descriptors, signatures, handles, frames, and
executable uses. Arbitrary member names, annotation values, and String
constants remain exact; Float and Double values retain their raw bits.

The comparator admits generated differences only through closed witnesses:

1. Compilation-unit compiler IDs must have a complete, injective and
   surjective mapping established before rendering. Candidate A/B exact paths
   are construction-graph witnesses, not a numeric rank heuristic.
2. Captured-constructor permutations are pair-specific. A changed constructor
   must be a bijective argument-to-field assignment, and every construction
   site must have one unique partition into safe value blocks. Calls, casts,
   static reads, unknown effects, ambiguous partitions, and cross-block
   dependencies fail. Construction edges also constrain otherwise identical
   generated child classes.
3. `core.async` state-machine array indices and local nodes are mapped as typed
   graph nodes. The IOC debug-only local-variable-table exception is bound to
   the exact source and owner and to the generated state-machine invoke shape;
   executable conflicts still fail.
4. Four remaining local-variable-table families are source-, owner-, method-,
   shape-, and cardinality-sealed: the exact `alts!` map/p family; 76 terminal
   `__auto__<suffix>` rows in 42 shapes; and seven boundary-parameter
   `p1__ID_SHARP_` rows in six shapes; plus the single authored
   `vec__<ID>` row in the exact `do_alt.invokeStatic(Object,Object)` method.
   These are not general numeric, `_SHARP_`, or `vec__` normalizers. Only the
   proved spelling component is omitted; the complete stable base and typed
   live-range graph remain exact.

The current comparator-only boundary snapshot is:

| Evidence | SHA-256 / count |
| --- | --- |
| Comparator source | `af6c7482f839bc60e7874cb5d53b7e031d3fbd40537ac2612e8235964ae1026e` |
| Compiled top-level comparator class | `ea0c9d8e690d5fc9879bd5e73ed7ca114100e805f3aa13ce57f38a382a83ae1c` |
| Complete 63-class relative manifest | `84c328ce4a425c6c7e30da5ff9e2c43e3012244b1d3ec30ff4dc3b42da374768` |
| Self-test TSV | `79b3da66faf71f2f4f9d832c1baa1c384cfd3a313d3c3f288fd28f43b5f01a86` |
| Adversarial self-tests | 112/112 PASS |
| Toolchain and complete command contract | `5e592c81cbeb3a0ac8dea36fac3e0fe8ef27b7650c0facc0358706ab8ef0d9f0` |
| Compiler-flag audit | `dbfc9a59f06db6269eaa360cdf3fbf0d5becc7a17bacb20bd2ab6275778aea51` |
| Clean-root reproduction relation | `60d17da0988f1e19deb29a2dc7a3a4095cdbb1241ef44474ac0147843bfed1f8` |
| Relation-driver evidence manifest | `19260f44756f879feedd409b7fafd228d69c4e206a2343f162e1b53144f2189a` |
| Accepted terminal-auto inventory shape multiset | `ae90e99a057f70538a746773b7eb2d0664556477d7d40a80ee0614e5d8201fa1` |
| Exact `do_alt` vec-local witness string | `70a41200b61f380b691adb79b22c42fc37d79ef3adfa7c7f74e2280fbfa51098` |

The comparator driver now runs the three requested relations sequentially
with a private evidence buffer for each relation. An expected semantic
`Failure` records that relation as `FAIL`, discards its partial mappings, and
does not prevent either later relation from running. Overall status remains
nonzero if any requested relation fails or if the ordered three-row relation
ledger is incomplete. Unexpected implementation failures are sealed to the
extent possible and then rethrown; they are not converted into semantic
mismatches.

The sealed class tree was compiled from
`/home/jake/Developer/atomic/datomic-rev` with this exact non-default
invocation:

```sh
/tmp/amazon-corretto-11.0.22.7.1/bin/javac -g:none -proc:none -cp /home/jake/Developer/datomic/datomic-pro-1.0.7277/lib/asm-9.2.jar -d /tmp/datomic-aot-relation-driver.CQQfQQ/classes transactor/tools/CompareExactSourceAot.java
```

At that invocation `JDK_JAVAC_OPTIONS`, `JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS`,
and `JDK_JAVA_OPTIONS` were empty. The complete ambient environment was not
captured, and the evidence says so rather than reconstructing it. A later
clean-root replay made the relevant boundary explicit by unsetting all four
option variables and setting `LC_ALL=C` and `TZ=UTC`:

```sh
env -u JDK_JAVAC_OPTIONS -u JAVA_TOOL_OPTIONS -u _JAVA_OPTIONS -u JDK_JAVA_OPTIONS LC_ALL=C TZ=UTC /tmp/amazon-corretto-11.0.22.7.1/bin/javac -g:none -proc:none -cp /home/jake/Developer/datomic/datomic-pro-1.0.7277/lib/asm-9.2.jar -d /tmp/datomic-aot-relation-repro.lH0x6r/classes /home/jake/Developer/atomic/datomic-rev/transactor/tools/CompareExactSourceAot.java
```

That replay reproduced the top-level class, all 63 relative class hashes, and
the 112-row self-test ledger byte-for-byte. A four-way flag audit explains the
otherwise different default-compiler hash. Default flags and `-proc:none`
produce top-level hash
`667f44e4d2254b3ba649ae062a396ccbe254a40d1a3681f38b026eb888eba35b`,
347 methods, 131 lambda implementation methods, and a `SourceFile` attribute.
Both `-g:none` variants produce the sealed hash, 328 methods, 112 lambda
implementation methods, and no `SourceFile`. The frozen Corretto 11 compiler
source confirms that its lambda-to-method pass deduplicates equivalent lambda
bodies only when line and variable debug information is absent. Thus the 19
methods are javac's debug-dependent duplicate lambda bodies, not an unrecorded
lambda compiler option or a comparator-source discrepancy.

Four new synthetic controls cover pass/fail/pass, all-pass, first-fail, and a
missing output-ledger row. They prove execution order, continuation after an
expected failure, overall failure propagation, complete ordered rows, and the
absence of failed-relation mapping leakage. A separate missing-input process
control exited 1 while still emitting exactly three ordered `FAIL` comparison
rows with `comparison_ledger.complete=true`. Two independent Corretto
replays plus the original sealed compilation produced byte-identical 63-class
trees and byte-identical 112-row self-test ledgers. The sealed evidence is at
`/tmp/datomic-aot-relation-driver.CQQfQQ`; its manifest verifies every file.
This is a driver/failure-evidence repair only and does not change the semantic
comparison quotient.

The terminal-auto inventory was replayed against candidate A, candidate B, an
oracle compile, and a third compilation context. Each contained 209 terminal
auto rows, exactly 76 accepted rows, and the same 42 accepted shapes. The
`p1__ID_SHARP_` inventory likewise yielded exactly seven accepted rows in six
shapes in all four outputs. The rules are bound to
`clojure/core/async.clj` SHA-256
`f205f3eb5be1b05c7191d514eedd1b968fde53e4e207f14782e6374bbf271f58`
and owner JAR SHA-256
`2e9160118c381d418ab1c4d330b6323ff76b7947e6e2d9b1cd1be950fd269d9f`.
Negative tests alter counts, rows, owners, methods, descriptors, slots, live
ranges, parameter roles, anchors, aliases, spelling, source identity, and
typed graphs. All fail closed.

The former first boundary at
`clojure/core/async$do_alt.invokeStatic(Object,Object)` is now closed by one
exact witness. Candidate A/B use `vec__6929`, the licensed oracle uses
`vec__6029`, and a third dependency-load context uses `vec__4615`. Every build
contains exactly one matching row in that class/method and the same Object
local at slot 6, labels 23–112/control region 3–3, non-boundary typed node
`local-6:A`, physical-slot set `[6]`, four-instruction def/use graph
`ASTORE 6, ALOAD 6, ALOAD 6, ASTORE 6`, zero frame aliases, and one LVT alias.
Each varying ID occurs once in LVT metadata and nowhere in non-LVT structure
or an owned class name. All four builds retain 13 `vec__<ID>` rows across
`core.async`; the other 12 remain on the ordinary exact/mapped path. The raw
four-build inventory SHA-256 is
`c0158beca6a4418b4e6da965cd9981d45757fe8cc583b862d24550667a38fa5b`;
the post-rule replay SHA-256 is
`b49f18513d4d52e9c7146c999775a80a5f251aca818d2f6c0e7bd7244542ba3d`.

The new self-tests include the positive one-row seal plus failures for short,
long, and extra inventories; source, owner, class header, method name,
descriptor and access; spelling, local descriptor and method LVT count; slot,
range, boundary role, category and physical-slot graph; def/use and frame
graphs; aliases; and non-LVT anchors or duplicate ID occurrences. The earlier
76-row terminal-auto and seven-row sharp rules still close in all four real
outputs.

Normalized equivalence is still **open**. No broader compiler-number or
`vec__` rule has been added, and a new complete 3,431-class comparison has not
been run to identify the next fail-closed boundary. Resolving this one row is
not evidence for whole-cohort equivalence.

The comparator contains an external-use scanner for normalized constructor
owners, but a successful whole-cohort comparison has not yet reached and
recorded that scanner result. More importantly, the validator does not yet
run a separate whole-cohort JVM verifier. `max_stack` and `max_locals` are
intentionally absent from the semantic model and cannot be accepted until
that verifier gate exists and passes. Therefore the full scanner, verifier,
and complete 3,431-class A/B/original relation are all **NOT RUN** for this
snapshot.

`validate-exact-source-aot.sh` still pins an earlier comparator hash
(`5c5ea8485fcb64676bd41d2bda4c9af6517844993a96337bd4be09917b1bf531`),
so it fails closed before using this snapshot. That pin is intentionally not
promoted while scanner/verifier integration and the remaining comparison
boundary are unfinished. Its existing pre/post runtime-input hashing, output
sealing, candidate-tree isolation, and sanitized-Nano substitution remain
useful machinery, but no integrated Stage 1 PASS is claimed.

## Cost

The 174 compiler processes consumed 1,064.83 aggregate elapsed seconds when
run sequentially. The slowest process took 15.34 seconds and the maximum
observed RSS was 865,508 KiB. The validator applies a 180-second timeout and a
1 GiB heap to every target JVM.

## Reproduction

Run `transactor/scripts/validate-exact-source-aot.sh` with the Datomic
distribution path, a new empty output directory outside the repository, the
Corretto 11 java executable, and a sanitized Nano JAR from a complete
hash-verified sanitization output directory. The script intentionally exits
nonzero while the normalized executable relation remains unresolved; raw-byte
nondeterminism is retained as a diagnostic rather than treated as failure by
itself.
