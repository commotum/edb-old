# Stage 1 reproducible source-artifact validation

Date: `2026-08-27` (`America/Los_Angeles`).

Status: **PASS**.

The latest adversarial final run is
`/tmp/datomic-stage1-adversarial-final-v1`. This is local, ephemeral run
evidence rather than checked-in repository content.

The canonical recovered implementation is a thin, source-bearing JAR built
from the recovered Clojure namespaces, the peer/core2-free compilation of the
handwritten Java sources, and the ten exact Peer resources. It is not an AOT
repackaging of the original Peer.

## Canonical artifact

| Property | Value |
|---|---:|
| Name | `datomic-rev-peer-1.0.7277-source.jar` |
| SHA-256 | `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe` |
| Entries | 205 |
| Recovered Clojure sources | 142 |
| Recompiled handwritten Java classes | 47 from 43 sources |
| Exact Peer resources | 10 |
| Embedded provenance records | 6 |
| Packaged Clojure AOT classes | 0 |

The 205-entry allowlist excludes the CFR evidence tree, generated Clojure AOT
classes, dependency classes, Hot Rod stubs, and the four noncanonical resource
aliases at the root of `resources/`.

The five material-input manifests embedded under
`META-INF/datomic-rev/inputs/` have these SHA-256 values:

| Manifest | SHA-256 |
|---|---|
| `build-tools.tsv` | `16eee8a4affba7d6b56be510cd66f46f9b69bcfc58697ff27692c29ac6808cce` |
| `clojure-sources.tsv` | `186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1` |
| `dependencies.tsv` | `76a02d5dfd78cd63451944856cdf472c8802a63032c86e021d1f1c9450dc2058` |
| `java-sources.tsv` | `77a136d05420250c3c9160d912f0c8619b886e2d7897adc27709969b0b2a73dd` |
| `resources.tsv` | `fd8d89751c628b4562b3c31691b2c3ce6fb44d2d770eaf73ca6aa568e070e15a` |

## Reproducibility and build boundary

`scripts/validate-stage-1.sh` performed two independent clean builds. Their
artifact bytes, artifact SHA-256 values, input manifests, and normalized build
records were identical.

The canonical builder:

- pins Ubuntu OpenJDK runtime `21.0.12+8-1-24.04-Ubuntu`;
- validates the exact ordered filename/SHA-256 set of 532 dependency JARs
  against the checked bytecode-inventory baseline;
- rejects Peer and core2 by filename and by their exact SHA-256 values;
- compiles the 43 handwritten Java sources against only those 532 dependencies;
- verifies that compilation produces exactly the 47-class handwritten manifest;
- packages physically sorted, file-only, `STORED` ZIP entries with the fixed
  local timestamp `1980-01-01T00:00:00`; and
- verifies every entry's physical order, method, sizes, timestamp, comment, and
  extra field, as well as the absence of an archive comment.

The host Java image exposes the compiler API but lacks the Java 11 `ct.sym`
data and a `javac` executable, so `--release 11` is unavailable. The pinned
build uses `-source 11 -target 11 -encoding UTF-8 -proc:none`, records that
limitation, and retains the resulting system-modules warning. The resulting
47-class API/ABI surface still matches the Peer oracle exactly.

Full recovered-source AOT was investigated but not selected for the canonical
artifact. Default HotSpot compilation changed 838 of 5,830 generated class
bytes across two clean runs. An experimental identity-hash JVM option made a
prototype reproducible, but it would make the build HotSpot-specific, expand
the generated surface from the original 5,470 classes to 5,830, and require a
second AOT-specific validation regime. The source-bearing JAR has the smaller,
more observable boundary and preserves warning visibility.

## Packaged-artifact validation

The candidate classpath was exactly:

1. the canonical recovered artifact;
2. four generated Infinispan compile/load-only stub classes;
3. the 532 explicitly ordered and hash-checked dependency JARs.

It contained neither `peer-1.0.7277.jar` nor `core2-1.0.140.jar`. An origin
probe verified that all 142 recovered source URLs, all 47 handwritten class
code sources, and all ten Peer resource URLs resolved from the artifact. It
also verified that none of the 142 original Peer/core2 namespace initializer
classes was visible.

| Gate | Result |
|---|---:|
| Artifact inspection/input verification | PASS |
| Original-resource byte oracle | 10 / 10 PASS |
| Fresh-JVM packaged namespace loads | 142 PASS, 0 FAIL |
| Focused recovered behavior regressions | PASS, including expanded lower-risk Stage 4 behavior coverage |
| Handwritten Java surface | 47 classes, 108 fields, 279 methods exact |
| Runtime Var/authored-class surfaces | 142 / 142 exact |
| Unique reflection warnings | 150 |
| Unique primitive-recur warnings | 6 |
| Unique auto-boxing warnings | 1 |
| Exact unresolved-warning inventory | 157; SHA-256 `9c8f20b030205f749edc952255da131bfa6d45c1537bbf0655bd6fe772ce33ad` |
| Original/recovered in-memory parity | exact |
| Parity result SHA-256 | `228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be` |

The licensed Peer SHA-256
`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`
was used only in explicitly separated resource, bytecode-surface, namespace-
surface, and behavior-oracle lanes. It never entered the candidate build or
candidate runtime classpath.

The expanded direct behavior gate now also covers overloaded-setter selection;
statistics accumulation and callback delivery; heap/direct UTF-8, base128, and
CRC paths; HMAC validation and tamper rejection; logging/retry/scheduled-failure
paths; heterogeneous comparison, key comparators, and pooled-map behavior; and
selected Datalog hash, join, and invalid-source paths. Stage 4 records the
remaining site-specific evidence boundary rather than treating this broader
regression set as exhaustive query equivalence.

## Bytecode-proven hardening included in the artifact

The first PostgreSQL validation exposed three decompiler artifacts whose
correct forms were uniquely determined by the original bytecode:

- `datomic.future/filling-promise` had a named inner function that shadowed and
  recursively called itself. The original class captures and invokes the outer
  callable, so the recovered wrapper is now anonymous. Success and failure
  regressions prove exactly one outer invocation and the expected delivery.
- The object-array branch of `datomic.memory-size/memory-size` discarded its
  completed loop with a trailing `nil`. The original method returns the boxed
  long. Focused checks now cover empty and populated object arrays.
- `datomic.common/pfuture` submitted an unhinted value reflectively, allowing
  overload resolution to choose `submit(Runnable)` and discard results. The
  original bytecode casts to `Callable` and invokes
  `ExecutorService.submit(Callable)`. The recovered source now carries that
  exact hint, with result and identical-cause failure regressions. This is the
  one bytecode-proven repair that reduced the reflection inventory from 306 to
  305.

Stage 4 then repaired 159 additional source warning sites across 11 namespaces,
but only where instruction-level oracle evidence established the source form.
Those changes removed 160 warning records: 155 reflection, four primitive-
recur, and one auto-boxing warning. The separate
`datomic.common/compare-byte-arrays` repair corrected an equal-length branch
that discarded its loop result; focused tests cover equality, prefixes, and
signed high-bit bytes. The two Stage 4 evidence tables and the exact inventory
of the 157 warnings deliberately left unchanged are documented in
`stage-4-validation.md`.

After all of these repairs, the complete Stage 1 gate—not merely focused
probes—was rerun from two clean builds and produced the canonical hash above.

## Final-run evidence

The adversarial rerun hashes the validation harness itself and accounts for all
files beneath both builds, packaged-artifact validation, and the recorded
inputs:

| Record | SHA-256 |
|---|---|
| `stage-1-summary.properties` | `0ecd7443db30698b19e46c538694ecef14a705bab33b8823959d26620b31b0bb` |
| `artifact-validation/validation-summary.properties` | `2cd299ff624ac21e020fc35a7283eeb7d255d86ba2008988f485dc34b8892d76` |
| `inputs/stage1-validation-harness.tsv` | `8c04b2dd38cf403fe29a981b5f025b1f5bb86863ddeb651f5b4d3f3c7f2dd404` |
| `evidence.sha256` | `531b2f2e9ce15e10bbe87ffcbfb5d8b884e7aacd6b20bcb4601b9f59bb5a960a` |

`evidence.sha256` contains 1,230 file records and verifies in full in the local
run directory. The manifest binds that local evidence while it exists; `/tmp`
is not a durable evidence store.

## Reproduce

Run the complete Stage 1 gate in a new empty directory:

```bash
DATOMIC_HOME=${DATOMIC_HOME:-../../datomic/datomic-pro-1.0.7277}
STAGE1_ROOT=/tmp/datomic-stage-1
JOBS=4 scripts/validate-stage-1.sh "$DATOMIC_HOME" "$STAGE1_ROOT"
ARTIFACT="$STAGE1_ROOT/build-a/datomic-rev-peer-1.0.7277-source.jar"
```

`STAGE1_ROOT` must be absent or empty. The run writes both clean builds, full
candidate/oracle diagnostics, the exact candidate classpath record, the
validation-harness manifest, `stage-1-summary.properties`, and
`evidence.sha256` beneath it. The `ARTIFACT` assignment is the composable input
used by the Stage 2 and Stage 3 commands.

This completes the reproducible-artifact stage. PostgreSQL lifecycle,
backup/restore, fault recovery, bounded concurrency, and transport-pause
evidence are recorded separately in `stage-2-validation.md` and
`stage-3-validation.md`.
