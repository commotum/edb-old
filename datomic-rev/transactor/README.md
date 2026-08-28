# Transactor recovery boundary

This subtree is the isolated recovery target for
`datomic-transactor-pro-1.0.7277.jar`. The recovered Peer remains at the
repository root and is a reference/regression boundary; it is not silently
substituted for the Transactor's 117 same-named but byte-different namespace
initializers.

Licensed Peer and Transactor JARs remain outside this repository. Tools in this
subtree may read exact, hash-checked originals as structural evidence. A later
candidate runtime must exclude both original implementation JARs and record an
explicit decision for every additional Datomic-owned dependency it retains.

The first reproducible boundary is generated with:

```bash
transactor/scripts/inventory.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-inventory
```

The runner uses the POM embedded in the Transactor JAR. The distribution-root
`pom.xml` describes the Peer and is not authoritative for this target.

See `reports/stage-0-boundary.md` for the frozen corpus, dependency, launch,
and candidate/oracle boundaries.

## Stage 1 source recovery

Bundled library namespaces are recovered from their exact, hash-bound source
entries rather than being mislabeled as Datomic code:

```bash
transactor/scripts/recover-bundled-sources.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-bundled-sources
```

The runner verifies all 533 dependency JARs, maps the 85 non-`datomic.*`
initializers to one unambiguous `.clj` or `.cljc` entry each, validates the
declared namespace and content hash, and extracts source without loading any
distribution class. Two independent canonical runs were byte-identical. The
compact ownership, namespace, and source-manifest evidence is retained under
`reports/stage-1-bundled-source-*`.

All 46 Java-origin sources are isolated under `src-java` and validated with:

```bash
transactor/scripts/validate-java.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-java-validation
```

The runner uses the pinned minimal ten-JAR compile classpath recorded in
`reports/stage-1-java-compile-classpath.tsv`; no Peer or Transactor
implementation JAR participates in compilation. Only after compiling twice
does the ASM verifier stream original Transactor entries as an out-of-tree
oracle. The result is 46 sources -> 52 classes, exact 122-field / 303-method
ABI, exact normalized code for all 52 classes, and deterministic output. See
`reports/stage-1-java-recovery.md`.

The canonical Clojure recovery is deliberately split by proven source
ownership:

```bash
JOBS=4 NAMESPACE_TIMEOUT=1200 \
  transactor/scripts/decompile-clojure.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-decompile
```

It audits all 162 Datomic initializer-derived source paths across all 533
hash-bound dependency JARs. The final split is 160 strict Datomic decompiles,
two exact shipped Datomic sources (`datomic.query.support` and
`datomic.specs`), and the 85 exact bundled sources above. Two independent
canonical runs produced the checked-in pre-repair 247-source tree byte for
byte, with zero strict failures and zero sentinels. A later all-namespace
surface comparison found one genuine recovery defect: four function-root vars
in `datomic.rest` gained `:arglists` because generic macrocompaction emitted
`defn` even though the original bytecode's metadata has no `:arglists` entry.
The old manifest remains reproducible diagnostic evidence but is superseded as
a promotable boundary. Early metadata-aware snapshots passed their focused
checks but were disqualified before promotion: one treated literal `nil` as
end-of-input and truncated source tails, and another duplicated metadata
evaluation by failing to equate a qualified current-namespace Var with its
local definition. The corrected generic repair must pass namespace-aware,
single-evaluation, literal-tail, full Peer, and independent Transactor twin
gates before promotion. See
`reports/stage-1-clojure-recovery.md`.

The moving repair now passes a 40-marker focused validator and proves exactly
twelve late-bound Var aliases, but source promotion is still blocked by exact
runtime metadata. In the targeted eleven namespaces, Var names match 449/449;
all 449 exact metadata maps still expose Integer-versus-Long `:column` drift,
and 33 retain protocol-root, protocol-method, docstring, or nested condition
metadata differences. A separate bounded inventory of all 247 original
namespaces makes the metadata encoder fail closed on one additional value: the
anonymous zero-arity state factory at
`clojure.tools.analyzer.passes.add-binding-atom/add-binding-atom` metadata path
`:pass-info/:state`. Its compiler-numbered class name differs across builds, so
class-suffix or arity erasure is not accepted as equivalence. Exact typed
metadata, a narrow semantic proof for that sole callable, full-tree delta
audit, fresh twins, and Peer regressions all remain open.

The current moving repair has two focused positives, neither of which promotes
the source tree yet. A generic protocol reconstruction now makes a freshly
recovered `datomic.kv-store` exact at the typed runtime-metadata surface. The
sole callable also matches between isolated original and candidate-only
runtimes under a path-bound fresh-empty-Atom contract. That replay exposed a
required runtime input: source probes must reproduce the Transactor POM's
`clojure.compiler.elide-meta='[:doc :file :line]'` setting. Without it, source
loading invents retained `:doc`, `:file`, and `:line` metadata absent from the
shipped AOT; `:column` and `:pass-info` must remain. The structural worker now
pins that setting. The top-level structural validator additionally hash-checks
the Transactor artifact, verifies the exact setting in its embedded Maven POM,
and records it in the evidence summary. A fresh discovery-only replay passed
the 247-Transactor/25-Peer-core2 source and isolated 532-JAR candidate
classpath boundary; it did not execute namespace loads or surface comparisons.
The full 11- and 247-namespace reruns and hardened Peer twins remain the
deciding gates.

A fresh diagnostic recovery using the typed/protocol repair now produces
247/247 sources with zero sentinels and a verified 247-row manifest at
`/tmp/datomic-full-exact-v22`. It is not promoted: the full-tree source-delta
reader currently fails on an auto-resolved alias keyword (`::ana/resolved-op`),
and an attempted surface-emitter freeze was rejected because its proxy ledger
mistakenly labeled every generated proxy root as `java.lang.Class`. Both
failure paths are retained as explicit Stage 1 blockers; the corrected
source-aware delta audit and proxy-bound emitter must precede fresh corpus
surfaces and twins.

The two exact Datomic sources additionally reproduce all 36 corresponding
embedded AOT classes byte for byte under the original compiler settings:

```bash
transactor/scripts/validate-exact-datomic-sources.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-exact-datomic-source-validation
```

The JKS-bearing `nano-impl` dependency is replaced by a deterministic sanitized
derivative, and the resulting 532-artifact dependency closure is recursively
content-audited without loading archive classes:

```bash
transactor/scripts/sanitize-nano-impl.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-nano-sanitized

transactor/scripts/audit-candidate-dependencies.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-nano-sanitized/nano-impl-0.1.325-sanitized.jar \
  /tmp/datomic-goal2-candidate-dependency-audit
```

The clean closure retains 531 exact shipped dependencies, substitutes sanitized
nano, excludes original `core2`, and rejects all ten licensed-artifact,
forbidden-payload, malformed-archive, hash, and symlink controls. Every one of
the 533 shipped dependency identities has a separate keep/replace/exclude row
and a coarse later-stage routing label, reproducible with:

```bash
transactor/scripts/build-dependency-dispositions.sh \
  /tmp/datomic-goal2-dependency-dispositions
```

The 85 bundled plus two exact Datomic sources have also compiled twice in
fresh JVMs to their complete expected 3,431-class closure. Cardinality,
initializers, and deterministic paths pass 87/87. The retained v2 skeleton
column passes 84/87; a later unsealed three-form check reported 87/87 and the
integrated gate must reproduce it. The stronger normalized ABI/instruction relation remains
open for 12 namespaces whose generated classes permute compiler identifiers or
captured-field constructor order; the gate intentionally exits nonzero until a
strict ASM comparator proves those mappings:

```bash
transactor/scripts/validate-exact-source-aot.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-exact-source-aot-validation
```

The initial 16-fixture comparator snapshot is diagnostic, not promotable. An
adversarial audit also requires exact class-path/internal-name binding, a
sealed verifier treatment for frames and maxima, preservation of observable
declaration order, stronger capture and constructor-ABI proofs, complete typed
classfile remapping, and a whole-runtime archive/resource/input seal before a
green relation can close this boundary. The detailed review is in
`reports/stage-1-exact-source-aot-adversarial-audit.md`. A later audited work-in-
progress snapshot still failed the retained candidate-A/candidate-B corpus on
an `AFn` versus `IFn` semantic `LDC`; its partial repairs are not promotion
evidence, and the wrapper remains pinned to the earlier reviewed comparator
until the relation and its trust boundary are deliberately closed. A newer
unreviewed WIP gets through candidate A/B with a source-bound set-literal rule,
then fails candidate A/original on typed `core.async$go` macro-gensym Symbols;
after a further typed-Symbol step, the current moving checkpoint fails closed
on a `core.async/filter<` state-slot setter whose value is a static constant.
A later 21-fixture WIP proves the typed state-array shapes and next fails three
`core.async` skeletons on ordinary JVM local-slot permutations; those require a
complete per-method typed def-use/control-flow mapping, not erased indexes. It
is superseded by a moving 35/35-fixture local-graph checkpoint that retains JVM
frame tags and rejects category, parameter/`this`, cross-use, IINC, LVT range/
name/signature, unmatched-node, and unknown-ID negatives. Its current
candidate/original failure is narrower but still real: one pipeline
state-machine closure has candidate `from`/`jobs` debug locals where the
original has generated `G__<id>` rows. That relation remains fail-closed until
exact source/macro lowering or a corrected class pairing proves it. A
subsequent 44/44-fixture checkpoint restricts variation to source-bound
core.async IOC state-machine LVT-only nodes, and a third exact-source build
produced a third name sequence at the same three nodes when only dependency
load/AOT context changed. Its fresh 3,431-class A/B/original replay still fails
earlier while constructing ID-placeholder class pairs for that same pipeline
child. Correctly carrying the source-bound rule into that skeleton clears the
difference but exposes two valid whole-`core.async` compiler-ID pairings, which
remain an explicit ambiguity rather than being tie-broken. The integrated
whole-corpus boundary scanner, verifier treatment, and end-to-end seals are
also still required before review or repinning.

The complete structural boundary is exercised with 247 recovered Transactor
sources, 25 recovered Peer-core2 support sources, 52 recovered Java classes,
reconstructed resources, and the sanitized 532-dependency classpath:

```bash
transactor/scripts/validate-structural-load.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-structural-load
```

The current fresh structural run is
`/tmp/datomic-transactor-recovered-pair-current-v2`. Its candidate classpath
ledger has SHA-256
`ba0f0c6d56fa59e0060b6f662dc8be95cba766a4519839cff2fc232f1b85e4b1`,
its evidence manifest has SHA-256
`5490edf69d9ae86a1d78edf9644e1196ebf644bbea649c022e83b141f58b5bc8`,
and its current 247-source manifest has SHA-256
`6f27a4259ea02bb3eba6214d44b7c155d0d3dda1128a8d0be5301c2d00257786`.
All 272 Transactor/core2 namespaces load (271 cold plus one
bytecode-supported production-order load) with the original Peer, Transactor,
core2, and unsanitized Nano implementations absent. Surfaces were explicitly
not executed in this run.

Stage 1 therefore remains incomplete at the exact-source normalized AOT and
full bounded surface boundaries, not at structural namespace load. Separately,
the repository-owned gate at
`/tmp/datomic-recovered-pair-live-v3` now proves the recovered Transactor and
recovered Peer through PostgreSQL boot, seed, durable commit, two fresh-process
restart/adoption checks, and a post-restart augment transaction. It also runs
the sealed focused recovery regressions. This runtime result does not close
persistent-index publication, injected-failure cleanup, transport interruption,
or HA/fencing. See `reports/postgresql-vertical-slice.md` for its exact evidence
and limits.
