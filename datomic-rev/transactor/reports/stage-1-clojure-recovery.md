# Stage 1 Transactor Clojure source recovery

## Goal 3 checkpoint — bounded recovery complete

Goal 3 now accepts this stage by **bounded semantic equivalence**, not by global
exact-AOT identity. Ownership/isolation, the deterministic recovered corpus,
the complete namespace and callable/class surface, JVM-valid exercised classes,
and focused runtime regressions close the normal Stage 1 boundary. Exact-AOT is
retained only as a diagnostic for a specific unexplained executable difference,
ABI/surface mismatch, differential behavior failure, or explicit user request.

The sealed production exact-AOT attempt at
`/tmp/datomic-goal3-stage1-production-v4` is honest negative evidence. Its
`result.tsv` says `FAIL` because the normalized whole-cohort comparator failed;
both complete 3,431-class candidate trees had compiled. The production
`gate-status.tsv` records comparator `FAIL`, scanner `NOT_RUN`, verifier
`NOT_RUN`, and final seal `PASS`; scanner and verifier self-tests passing does
not mean their production gates ran. The output-manifest file SHA-256 is
`68d80b1112def5308863443ee62726e0d5fe986ee72ee99d089885458e6b34fa`.
The bounded v122 follow-up also did not pass: it stopped at search index 45
after the 1,000-node limit with 1,051 ambiguous nodes in 927 groups. Neither
result is an exact-AOT PASS, and neither reopens this completed bounded stage by
itself.

## Integrated corpus evidence

Fresh canonical recoveries at
`/tmp/datomic-stage1-integrated-twin-v3-a` and
`/tmp/datomic-stage1-integrated-twin-v3-b` each pass all 247 namespace rows:
160 strict Datomic decompiles, two exact Datomic dependency sources, and 85
exact bundled sources. Both have zero failures and zero `BROKEN DECOMP`
sentinels, and their source trees are byte-identical. The promoted 247-row
source-manifest-file SHA-256 is
`bc44fdc277a5452b8fa950eaa390b595f5d93b5705cfb1b2ab4689d07656b69f`.
The twin evidence manifest at
`/tmp/datomic-stage1-integrated-twin-v3-evidence/manifest.sha256` has SHA-256
`f01000248fd4a9093100aebcf94f783b6863369aba7c74b41da289ea482e097d`
and verifies in full.

The full external-source structural run at
`/tmp/datomic-stage1-integrated-twin-v3-surface` proves 272/272 effective
loads: 271 isolated cold loads plus the documented topological
`datomic.transactor-ext` pass. All 247 oracle and 247 candidate probes produce
valid dedicated EDN. Raw exact agreement is 232/247, source-location-stripped
agreement is 243/247, and callable/root/class agreement with Var metadata
excluded is 247/247. The only strict metadata remainder is 31 Vars across four
namespaces in two families: 28 `:arglists` rows across two namespaces and
three `:name` rows across two namespaces. All fifteen raw-differing namespaces
are hash-bound shipped-source namespaces; no decompiled Datomic namespace
remains in the residual cohort. Those source-evaluation-versus-AOT differences
belong to the normalized exact-AOT gate, not to another decompiler edit loop.
The structural evidence manifest SHA-256 is
`4921abcfb0926790780c21889e71ab05889d0347496d3272fafd5464f5d073a2`
and verifies in full; the typed classifier is a semantic PASS despite the
top-level strict-exact gate's expected 15-row FAIL.

The first focused runtime pass against the prior fresh twin stopped promotion
on `datomic.promise/settable-future`: the source still contained raw
`monitor-enter`/`monitor-exit` forms and threw
`IllegalMonitorStateException`. The generic recognizer accepted only an
unqualified `let`, while the preceding compaction pass emitted the in-memory
symbol `clojure.core/let`; pprint/readback had hidden that distinction from the
old fixture. Recovery now accepts exactly `let` or `clojure.core/let`, retains
all existing lock-temp/enter/try/finally/exit guards, and proves an unrelated
`foreign/let` and ordinary qualified `let` cannot match. An aborted v2 twin
then exposed an eager would-be-`try` inspection; the recognizer now proves the
complete prefix and actual `try` form before traversing it. The complete
decompiler validator passes, the regenerated `datomic.promise` contains two
source-level `locking` forms, and the focused runtime suite passes against the
complete v3 tree. That suite also now proves an empty persistent index returns
the initialized `disjoined-datoms` partition rather than stale `nil`.
The current compactor, decompiler validator, and focused runtime-regression
SHA-256 values are
`9e392d551c873626d29a0c79c09bd52ca1aca5eb67f4bc77555476bfd5b1542e`,
`4859299065cadb5a0e707b6aea20a8404b91ee35174d1e02d41a82477e835983`,
and `62a1111ed043d694c26a3468aac6b0f6a1bab9e86ea1e4b6badaf6eed400cc24`.

The checked-in `transactor/src-clj` tree and checked-in source manifest are
byte-identical to Twin A. A checked-in source preflight at
`/tmp/datomic-stage1-integrated-twin-v3-promotion-preflight` passes exact
247-file membership and hashes; its evidence-manifest SHA-256 is
`4751cec79d9ff802fc116b1bc7f82e13d00b9238cc97740f51cfcfed046ec150`.
The promoted generator output retains deterministic trailing spaces on some
multiline forms, so `git diff --check` reports generated-source whitespace;
those twin-bound bytes were not silently normalized during promotion.

Under the superseded global exact-AOT policy, Stage 1 remained open at the
whole 3,431-class relation. Under Goal 3's bounded-semantic policy, the
integrated Clojure corpus, protocol metadata family, load boundary, callable
surface, JVM-valid exercised runtime, and focused regressions close Stage 1;
the failed relation above remains nonblocking diagnostic evidence.

## Superseded historical checkpoint

This report originally promoted the v7/v8 tree. That promotion is retracted.
Later whole-tree auditing proved that generic compaction could truncate every
form after a literal `nil` or `false`, could duplicate metadata evaluation for
qualified current-namespace Vars, and could emit strict cross-namespace
`(var ...)` aliases that fail during circular source loading. Those focused
defects are repaired in the moving decompiler, but no replacement 247-file
manifest is promoted yet. The exact runtime-metadata gate also currently fails:
all 449 Vars in the targeted eleven namespaces differ on Integer-versus-Long
`:column` values, with 33 additional protocol or nested-condition metadata
residuals; the full 247-namespace emitter inventory rejects one anonymous
callable stored at `add-binding-atom`'s `:pass-info/:state` path. See
`../../../goal-2/0-plan.md` when the goal folder is beside the repository,
or `/home/jake/Developer/atomic/goal-2/0-plan.md`, for the current evidence and
continuation boundary.

The moving repair now has focused, non-promotable positives for both classes
of defect: freshly recovered `datomic.kv-store` matches its original exact
typed metadata surface after generic protocol reconstruction, and the sole
callable matches between isolated original and candidate-only runtimes under a
path-bound fresh-empty-Atom contract. The callable replay also proved that
source loading must reproduce the embedded POM's
`clojure.compiler.elide-meta='[:doc :file :line]'` option; omitting it retains
metadata that the shipped AOT deliberately elides. The structural worker now
pins that option, and the top-level structural validator now verifies it
against the hash-pinned artifact's embedded Maven POM and records it in every
successful evidence summary. The discovery-only replay at
`/tmp/datomic-structural-pom-proof.DfQPhiaH/run` passed the complete source and
candidate-classpath discovery boundary with a recursively verified manifest,
but deliberately executed no namespace loads or surfaces. A fresh complete
source tree, all-11 and all-247 exact metadata comparisons, Transactor twins,
Peer regressions, and the full structural replay are still required before
this report can promote a replacement manifest.

The repair has now generated one complete diagnostic tree at
`/tmp/datomic-full-exact-v22`: 247/247 recovery rows pass, no sentinel is
present, and its 247-row source manifest verifies. Promotion is still blocked
by two independently caught audit defects. The old-to-new source-delta reader
cannot currently read `::ana/resolved-op` with the source namespace's alias
context, and the first frozen surface emitter recorded actual proxy `Class`
roots as the meta-class `java.lang.Class`. Results depending on those tools are
diagnostic until a source-aware reader and identity-bound proxy ledger pass
their negatives and the complete gates are rerun from fresh roots.

The historical checked-in tree at `transactor/src-clj` accounts for all 247 Transactor
namespace initializers without treating namespace-name overlap with the Peer as
source identity:

| Recovery boundary | Namespaces | Method |
|---|---:|---|
| Datomic with no shipped dependency source | 160 | strict decompilation; lenient mode disabled |
| Datomic with exact shipped dependency source | 2 | exact source, owner/hash/AOT constrained |
| Bundled non-Datomic libraries | 85 | exact shipped source, owner/hash/declared namespace constrained |
| **Total** | **247** | **246 `.clj` and 1 `.cljc`** |

Two independent canonical runs, `/tmp/datomic-goal2-transactor-source-final-v7`
and `-v8`, produced byte-identical source trees. Both reported 247 passes, zero
failures, zero `BROKEN DECOMP` sentinels, and an `xargs` status of zero. The
historical tree is byte-identical to both runs.

The final source-manifest file SHA-256 is
`2774eb1cffcbdf2e5d98b71a0d9633650c50a11c39bd0f43fc0d2d340495a55a`.
Compact result hashes are recorded in `stage-1-clojure-provenance.tsv`; the
manifest itself is `stage-1-clojure-source-manifest.sha256`.

This proves reproducibility of that historical processing only. It no longer
closes the deterministic source-recovery slice of Stage 1 and does not claim a
recovered Transactor runtime.

## Exact Datomic dependency sources

An exhaustive scan of all 533 hash-bound dependency JARs checked the exact
`.clj` and `.cljc` paths derived from every one of the 162 `datomic.*`
initializers. It found 160 missing paths, two single exact paths, and no
ambiguity:

| Namespace | Owner | Owner SHA-256 | Source SHA-256 | Embedded AOT closure |
|---|---|---|---|---:|
| `datomic.query.support` | `lib/query-support-0.8.28.jar` | `086973461a46353cb10f6f4a9bfff9e083bd05f3754494069acf6f0a7651f84e` | `54d6bba7e770c528ecd55abdb2950ff7bce9263e290d3a50ee9532f83965fd1c` | 32 classes |
| `datomic.specs` | `lib/datomic.specs-0.1.3.jar` | `77480e064e4c4774d5f64effe39b78f998465247d616bcaf11fd237a6358b4a0` | `42b50993e1e360eb66b587ab9f5ade8dc50f8383c4977e76e96bcc12a25c6876` | 4 classes |

The scan and canonical runner do not rely on those path matches alone. The
checked-in `validate-exact-datomic-sources.sh` recompiles each exact source
twice with Clojure 1.11.4 on Corretto 11.0.22.7.1. It reproduces the embedded
POM's `{:elide-meta [:doc :file :line]}` compiler setting and aligns the
monotonic Clojure compiler IDs to the original AOT identifiers. Both builds
produce the same 36-class closure, and all 36 fresh classfiles are raw-byte
identical to the corresponding Transactor ZIP entries. Separate original and
source-only JVMs also match both namespace surfaces.

The candidate compile classpath contains only the two recovered sources and
the pinned Clojure/spec libraries. Peer, Transactor, and `core2` implementation
JARs are absent. Original Transactor class bytes are streamed only after both
builds as a ZIP oracle; original AOT is loaded only by the separate original
surface process. The query-support JAR's two `MapOnIndexed` Java classes are
not part of this 36-class embedded closure and are not copied into the source
tree.

Evidence:

- `stage-1-datomic-dependency-source-ownership.tsv`
- `stage-1-datomic-dependency-source-validation.tsv`
- `stage-1-datomic-dependency-aot-relation.tsv`
- `stage-1-datomic-dependency-aot-summary.tsv`
- `stage-1-datomic-dependency-surface-relation.tsv`

## Decompiler defects and the Peer regression boundary

Whole-artifact recovery exposed real control-flow defects rather than a need
for larger JVM heaps. The repaired cases include terminal throws and
non-returning methods, implicit and nested loops, split `try` ranges,
exceptional-only `finally` handlers, structured continuation boundaries, and
namespace qualification across successive `in-ns` forms. The focused validator
passes all current groups, including the multi-`classForName` protocol-preintern
persistence regression. Its SHA-256 is
`a19fbd8ed0085fd65be3f274fa8dc8fde62684fd692da806e3cdfbf97e31ce6b`;
the retained regression-log SHA-256 is
`e8231f52542c083964058e3c9e065cdb34de1ab5cdaad63260c16392050f0446`.

One initially clean-looking 247-source result was rejected after a complete
Peer regeneration exposed value loss in 16 core.async IOC state-machine loops.
The faulty output replaced the value-producing form

```clojure
(if (identical? result :recur) (recur) result)
```

with a one-armed branch followed by `nil`. That made `recur` non-tail and
discarded non-`:recur` results. The repaired discriminator uses the bytecode's
explicit false-arm local load to preserve the two-arm value expression while
keeping true one-arm loops tail-positioned.

The final full Peer run recovered and reader-validated all 142 namespaces. Its
sorted manifest-file SHA-256 is
`0ec14f378130c57646a8f4a2f80b83c6e1d64551cf4debe5cf512be1908c85b6`,
not the historical decompiler manifest
`ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be`.
All 13 changed namespaces passed strict recovery, reader validation, and a
source-only require with original Peer and `core2` AOT excluded. All 16 IOC
value exits returned to the correct two-arm form. The remaining 24 form
differences were audited against exception tables and reduce solely to
equivalent nested `try`/`catch`/`finally` structure. The checked-in recovered
Peer source was not modified.

A first complete structural-load attempt then exposed a separate false-clean
source defect in `datomic.log.specs`: after the file switches from
`datomic.log.specs` into `datomic.log`, the recovered call at source line 117
had incorrectly become the unqualified `sorted-by-t?`. The namespace elider
was retaining every prior `in-ns` target as an empty alias and therefore
stripped a qualification that had become necessary. The original
`datomic.log.specs__init` bytecode binds `const__3` to the Var
`datomic.log.specs/sorted-by-t?` and invokes `Var.getRawRoot` at load bytecode
offset 765, which proves the intended reference. The repair discards stale
empty namespace aliases when `in-ns` changes; a focused two-namespace
regression prevents recurrence. Canonical runs v7 and v8 supersede v5 and v6,
and differ from them only by restoring that one qualification.

After the IOC repair, two 162-decompile Transactor twins were valid but were
superseded when the dependency scan found the two exact Datomic sources above.
The final canonical twins therefore use the stronger 160 + 2 + 85 boundary;
no prior diagnostic or superseded run is used as promoted evidence.

## Reader and isolation boundaries

The canonical runner applies a strict, non-evaluating full reader pass to all
162 Datomic sources, including the two exact dependency sources. It validates
the 85 bundled sources by exact owner/JAR/source hashes and declared namespace,
then verifies their hashes again after merging.

A generic cold reader is not a sound validator for all 85 bundled files. One
contains a legacy `#=` form, and another contains auto-resolved aliases whose
meaning is established only after its `ns` form has run. Evaluating those files
merely to make a reader gate pass would weaken the isolation boundary. Their
load/AOT equivalence is therefore a separate remaining gate.

The original Transactor JAR is unpacked only into a disposable directory as
decompiler data. It is never put on a decompiler or candidate classpath. The
exact-source validator uses its ZIP entries only after candidate compilation,
except for an explicitly separate original-surface JVM. Licensed originals
remain outside the repository.

## Reproduction

```bash
JOBS=8 NAMESPACE_TIMEOUT=600 DATOMIC_TRANSACTOR_JAVA_HEAP=512m \
  transactor/scripts/decompile-clojure.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-source

transactor/scripts/validate-exact-datomic-sources.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-exact-datomic-source-validation
```

Both commands refuse a non-empty output directory and verify the frozen input
hashes before recovery.

## Parked exact-AOT diagnostics

- The 85 exact bundled namespaces plus the two exact Datomic sources compile
  twice in fresh JVMs to the complete expected 3,431-class closure, with
  cardinality, initializer presence, and deterministic paths passing 87/87.
  The retained v2 skeleton column passes 84/87; a later unsealed three-form
  post-run check reported 87/87 and must be reproduced by the integrated gate.
  Raw bytes are deterministic for
  75/87 namespaces; 12 namespaces contain 219 classfiles with compiler-ID and
  captured-field/constructor-order differences. A strict ASM relation must
  still prove bijective per-fresh-JVM compilation-unit mappings, exact
  cross-namespace routing, and complete normalized ABI/code
  equivalence without erasing ambiguity. That investigation is parked unless a
  Goal 3 escalation trigger selects it.
- The promoted 247-namespace plus 25 Peer-core2 structural gate is sealed at
  272/272 effective loads and 247/247 callable/root/class surface agreement,
  without original Peer, Transactor, or `core2` AOT fallback. The protocol
  family is integrated. Its strict one-tier status remains an expected FAIL
  only for fifteen hash-bound shipped-source namespaces: 31 metadata rows in
  `:arglists` and `:name`. Those source-evaluation-versus-AOT differences are
  recorded diagnostics; they are not a global prerequisite under the current
  acceptance policy.
- The deterministic JKS-free `nano-impl` derivative, the clean hash-bound
  532-JAR dependency manifest, and four separately authored/generated
  candidate resources are integrated into that structural classpath. The
  archive-content audit and keep/replace/exclude accounting for all 533 shipped
  dependencies are closed. The promoted-tree structural evidence manifest and
  both post-load/post-surface input and directory-membership seals verify.
