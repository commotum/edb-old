# Goal 2 — Recover and Understand the Complete Datomic Pro 1.0.7277 System

## Objective

Produce an evidence-backed educational source recovery and architectural
reconstruction of classic Datomic Pro 1.0.7277 beyond the already recovered
Peer. Recover the complete Transactor artifact, explain and validate the
authoritative write, durability, indexing, coordination, and HA paths against
PostgreSQL first, and then cover the remaining maintenance, operational, and
optional access components in priority order.

The result should let a reader study how the Peer, Transactor, and durable
storage cooperate without relying on the original Peer or Transactor AOT
classes in candidate runtimes. Licensed originals may be used only as
fingerprinted, isolated evidence or behavioral oracles.

## Material constraints

- Bind recovery claims to Datomic Pro `1.0.7277` and the exact source artifacts:
  - `peer-1.0.7277.jar`, SHA-256
    `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`;
  - `datomic-transactor-pro-1.0.7277.jar`, SHA-256
    `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`.
- Preserve the completed Peer recovery as a reference and regression boundary.
  Do not edit or inspect `goal-1` unless the user explicitly changes that
  boundary.
- Recover the entire Transactor artifact: all classes and resources, all 247
  compiled namespace initializers (162 Datomic and 85 bundled-library
  namespaces), and both its 45 Transactor-only and 117 Peer-overlapping
  Datomic namespaces. Overlap is not evidence of bytecode or source
  equivalence.
- Keep candidate and oracle execution visibly separate. An original Peer or
  Transactor implementation must not enter a recovered candidate classpath.
- Use PostgreSQL as the primary durable backend. Treat PostgreSQL, JDBC,
  Artemis, Lucene, Jetty, and similar libraries as external machinery; recover
  the Datomic-specific semantics around them.
- Use disposable storage, bounded processes, strict cleanup, and evidence that
  cannot report success before cleanup and durability checks complete.
- Preserve unrelated worktree changes. Prefer deterministic regeneration and
  bytecode-constrained repairs over inferred rewrites or cosmetic cleanup.
- Defer alternative storage backends until the PostgreSQL-backed system and
  higher-priority components are complete.
- Match every completion claim to observable evidence and state uncertainty or
  evidence limits plainly.

## Known context

- The Peer artifact contains 5,517 classes and 142 Datomic namespace
  initializers. Its recovered source and validation evidence live in this
  repository and currently form the reference implementation.
- The Transactor artifact contains 9,682 classes and 247 namespace
  initializers: 162 under `datomic.*` and 85 from bundled libraries. Its 117
  Datomic namespace-name overlaps with the Peer require explicit comparison
  rather than wholesale reuse of Peer sources.
- `core2-1.0.140.jar` is support packaging, not a fourth architectural role;
  its classes duplicate the Peer-embedded core2 classes for this distribution.
- PostgreSQL is the external storage service. Datomic's SQL, key/value,
  cluster, catalog, and coordination layers live inside the Peer and
  Transactor artifacts.
- Log writing, persistent indexing, transport, lifecycle, backup, and
  maintenance are logical subsystems rather than additional mandatory
  data-plane servers.

## Stages

### Stage 0 — Freeze the reference boundary

**Status:** Complete

**Outcome:** A trusted starting point for Goal 2 that preserves the recovered
Peer and identifies every input to the Transactor recovery.

**Focus:** Reconcile the actual worktree and retained evidence with the Peer
baseline; fingerprint the complete Transactor artifact, distribution
dependencies, resources, launch scripts, and optional Datomic-owned artifacts;
record candidate/oracle boundaries before modifying recovery tooling.

**Completion signal:** The Peer baseline still passes its authoritative gates,
the Transactor corpus and dependency boundary are completely inventoried, and
the plan records any changed facts that affect later stages.

**Evidence:** The unchanged Peer inputs match all final manifests and retained
evidence was reverified. `datomic-rev/transactor/reports/stage-0-boundary.md`
records the complete corpus, launch/dependency map, optional products, and
candidate/oracle policy. The deterministic checked-in baseline accounts for
all 9,871 archive entries, and its `manifest.sha256` verifies in full.

### Stage 1 — Recover the whole Transactor corpus

**Status:** In progress

**Outcome:** A deterministic, navigable structural recovery of the entire
Transactor JAR, not merely its Transactor-only namespace names.

**Focus:** Account for every class and resource; reconstruct all Datomic
namespaces and handwritten Java surfaces; distinguish Datomic code from bundled
or external dependencies; retain reproducible provenance and exact artifact
manifests.

**Completion signal:** Every Transactor entry is classified, the recovered
source tree regenerates deterministically, all source is readable/loadable at
the appropriate structural boundary, and no unexplained class or resource gap
remains.

**Current evidence:** Entry-closure accounting maps all 9,682 classfiles to all
293 source owners: 9,630 AOT classes to 247 namespace closures and 52 Java
classes to 46 Java sources, with zero orphan and zero unresolved rows. All 11
resources are classified: eight are byte-identical to Peer and three are
Transactor-only. Independent accounting trees are byte-identical at
manifest-file SHA-256
`33cfe7358aac525f657b175d8a639b42e4a16160a388ea0282a31c035c387484`.

The checked-in `datomic-rev/transactor/src-clj` tree accounts for all 247
namespace initializers as 160 strict Datomic decompiles, two exact Datomic
dependency sources, and 85 exact bundled sources. Two independent canonical
runs produced byte-identical trees with 247 passes, zero failures, and zero
`BROKEN DECOMP` sentinels. That pre-repair source-manifest-file SHA-256 is
`2774eb1cffcbdf2e5d98b71a0d9633650c50a11c39bd0f43fc0d2d340495a55a`.
That manifest is no longer promotable as the final Stage 1 source boundary: a
later exact Var-surface comparison found one bytecode-significant metadata
defect in `datomic.rest`, described below. It remains reproducible evidence for
the pre-repair state while a new canonical twin recovery and manifest are
required.
The exhaustive 533-JAR ownership scan found exact, unambiguous source only for
`datomic.query.support` in `query-support-0.8.28.jar` and `datomic.specs` in
`datomic.specs-0.1.3.jar`; recompiling those two sources twice under the pinned
JDK, Clojure version, compiler options, and aligned compiler IDs reproduces all
36 corresponding Transactor AOT classes byte for byte. All 85 bundled sources
map one-to-one to exact entries in 22 hash-verified dependency JARs. Together
with the two exact Datomic sources they now reproduce the expected structural
3,431-class cohort twice; the strict normalized AOT relation remains open. See
`datomic-rev/transactor/reports/stage-1-clojure-recovery.md`.

The decompiler repairs remain constrained by the recovered Peer. Its 24
focused regressions now include namespace qualification across successive
`in-ns` forms. A full final Peer regeneration recovered and reader-validated
all 142 namespaces at
manifest-file SHA-256
`0ec14f378130c57646a8f4a2f80b83c6e1d64551cf4debe5cf512be1908c85b6`.
It confirms all 16 core.async IOC value exits retain the bytecode-proven
two-arm value expression. Thirteen changed namespaces pass strict recovery,
reader validation, and source-only require with original Peer and `core2` AOT
excluded; the remaining 24 form differences are exception-table-supported,
semantically equivalent nested `try` forms. The checked-in Peer source remains
untouched. Earlier apparently clean Transactor runs affected by the IOC defect
are explicitly rejected rather than promoted.

The first all-namespace structural load exposed and rejected one additional
false-clean decompile: after `datomic.log.specs` switches into `datomic.log`, a
call to `datomic.log.specs/sorted-by-t?` had lost its required qualification.
The original initializer's `const__3` Var and `getRawRoot` bytecode prove the
qualified reference. The namespace-elision repair, focused regression, and
independent canonical v7/v8 recoveries now preserve it; v7, v8, the checked
pre-repair tree, and its manifest are byte-identical.

The complete Java-origin slice under `datomic-rev/transactor/src-java`
compiles deterministically from 46 sources to all 52 expected classes with 122
fields and 303 methods, 52/52 exact ABI matches, and 52/52 normalized
executable-code matches. Its pinned ten-JAR compile classpath excludes Peer,
Transactor, `core2`, and unrelated AOT implementation JARs. See
`datomic-rev/transactor/reports/stage-1-java-recovery.md`.

The JKS-bearing `nano-impl-0.1.325.jar` now has a deterministic, twin-built UTC
derivative containing the exact eleven allowed files and neither forbidden
payload. Its SHA-256 is
`08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f`.
The sanitizer wrapper now gives every JVM invocation an explicit isolated
`java.io.tmpdir`, rejects symbolic, nonempty, repository/distribution, and
output-overlapping external temp roots, and requires zero retained entries.
The first implementation at SHA-256
`79f0e4e69d266767eef350a907b68d5df9076ebb8f3060fd767ad37b76ebf864`
reproduced the derivative independently at
`/tmp/datomic-nano-tmp-contract.CXwmm1`, with output manifest-file SHA-256
`6fad7a6030b9e47736e1b6cffeb0f92a5645b387815707aa3ba52a6796eab982`;
nonempty-root, symbolic-root, and repository-root controls rejected at
`/tmp/datomic-nano-tmp-negatives.DA7JU2`. A stricter superseding freeze,
SHA-256
`e906b8e5ae56207677bb45457519d782fba31fabfe539bcd4465e089d0566186`,
also requires a supplied temp root to be absolute and already existing. It
passes at `/tmp/datomic-nano-run-validation.XIHbMq/output`, leaves
`/tmp/datomic-nano-tmp-validation.R0KmBo` empty, and preserves the exact
derivative hash. An independent replay at
`/tmp/datomic-nano-strict-independent.77Gsna` confirms the same wrapper and
derivative hashes, zero retained Java-temp entries, and output manifest-file
SHA-256
`7c5eb6452c65043cad4d4ecbeb7480bbea0d1f09d9b33fcb9115703c7b4e781f`.
This stricter identity is the one consumed by the current Peer and structural
harnesses.
The resulting 532-artifact candidate dependency closure retains 531 shipped
JARs, substitutes sanitized nano, excludes original `core2`, recursively scans
185,393 direct and 95 nested entries, and has zero forbidden hits or
violations. Ten negative controls cover renamed and embedded originals,
extensionless and malformed/concatenated nested archives, hash mismatch, and
symlinks; independent v4 runs are byte-identical. The scanner's explicit
boundary is stable non-adversarial inputs and complete files/logical entry
payloads, not arbitrary substring or fragmented-encoding detection.

All 533 shipped dependency identities now have an explicit Stage 1
disposition: 531 hash-pinned retentions for complete-corpus structural
compatibility, one sanitized replacement, and original `core2` excluded in
favor of 25 recovered Peer support sources. Per-row later scopes are coarse
filename-based routing labels, not proved usage analysis; they preserve the
PostgreSQL-first order without mistaking Stage 1 retention for final runtime
necessity.

The 85 exact bundled sources plus the two exact Datomic sources compile twice
in fresh JVMs to all 3,431 expected classes. The retained v2 run proves
cardinality, initializer presence, and deterministic paths 87/87; its original
name-skeleton rule passed 84/87, while a later unsealed three-form post-run
check reported 87/87 and must be re-established by the integrated gate. Raw
bytes are deterministic for 75/87 namespaces; 12 namespaces have 219 generated
class differences involving compiler identifiers and captured-field
constructor order. Because each namespace is compiled in a fresh JVM, numeric
tokens are not a whole-cohort identity domain: the current three-form filename
regex finds 1,529 distinct tokens in candidate build A, 251 recurring across
namespace outputs, and token 6789 in all 87 namespaces. These are filename-token
counts, not proved `RT.nextID` identities; they also demonstrate that the regex
conflates token kinds. The comparator is therefore being tightened around
bijective typed owned-class nodes per compile unit, exact boundary routing, and
a complete normalized ABI/instruction relation rather than generic numeric
replacement. No equivalence is claimed by merely erasing those differences.
The frozen diagnostic ASM comparator models owned class nodes explicitly,
keeps ordinary member names and semantic strings exact, narrowly normalizes
typed class references and capture permutations, and passes its initial 16/16
positive and negative fixtures. Its retained-v2 corpus run still fails:
`core.async`
macro-generated member nodes such as `state_8891` versus `state_7991` and
`G__*` names need a typed member-node graph mapping, not numeric or rank-based
substitution. An adversarial review also rejects that frozen comparator and
wrapper as promotable even if the present corpus failure were removed: class
filesystem paths are not yet bound to internal names; verifier frames/maxima
are skipped without a sealed verification quotient; observable interface,
exception, field, and method order is over-normalized; volatile/static-receiver
capture cases and auxiliary-constructor ABI are under-constrained; several
typed classfile locations remain unmodeled; and the full source root, JDK image,
candidate outputs, process environment, and runtime archive/resource boundary
are not yet sealed end to end. The active redesign must close those acceptance
paths with focused negatives and accept the existence of a complete labeled
class/member graph isomorphism rather than demand unique internal names. The
diagnostic wrapper does replace Nano at its exact classpath slot, records 632
runtime inputs before and after execution, and verifies its output manifest,
but a fresh full run is intentionally withheld while both the relation and its
trust boundary remain open. An exploratory class-byte boundary scan reported
no licensed-Peer or dependency reference to any of the 2,074 generated cohort
internal or dotted names across all 533 dependency JAR identities; it did not
scan the recovered Peer. Its Peer input was the pinned 6,801,387-byte licensed
JAR (5,517 classes), and the retained 59,497-line `jdeps` output has SHA-256
`39bb75a2e03d6c8ee03ef31a3c7f91182cd1198a1385def813d567304f7087de`.
One modular CBOR JAR did not resolve under isolated `jdeps` and has only the
corroborating raw-byte result. Exact commands and a contemporaneous entry-count
manifest were not retained, so the temporary zero-hit files are not canonical
evidence. The final wrapper must record a fail-closed per-archive scan including
the recovered Peer, non-class resources, and bootstrap/annotation values.
The frozen and moving-implementation review is retained in
`datomic-rev/transactor/reports/stage-1-exact-source-aot-adversarial-audit.md`
(SHA-256
`20ee5c49fbc89df87e9cd521c3ccc1efa79232fe0fb53a000941c5b5b64bd616`).
A follow-up audit snapshot at comparator SHA-256
`9f7d221b08b99f0c038c3f6e2da5ca499cf5cc3da6b40f07936f063cbdf88940`
shows partial path, order, frame, field-handle, and capture-field repairs, but
the wrapper still pins the old reviewed comparator identity and therefore
correctly refuses this moving snapshot. Its original 16 fixtures pass while a manual retained-v2 A/B check
still fails on a semantic `clojure.lang.AFn` versus `clojure.lang.IFn` `LDC` in
`clojure/core/memoize$fifo`. That difference comes from the exact shipped
four-class set literal: the two compiles swap AFn and IFn in the array supplied
to `PersistentHashSet.create` and in its parallel Symbol set, while the original
Transactor uses a third element order. Any quotient must
be narrowly source-bound rather than normalize arbitrary arrays or constants.
Owner-constrained member edges, full valid-witness
backtracking, constructor/use-edge and frame retention, static receivers,
source-proved generated-member eligibility, lossless string encoding, strict
classfile consumption, descriptor-aware frames, witness evidence, and a
consistent automorphism policy remain open. This moving snapshot is neither a
current identity nor promotion evidence. Neither reviewed comparator body is
retained as a content-addressed repository artifact, so the next freeze must
preserve exact source, commands, wrapper, and failure output together.
A later, still-unreviewed WIP at comparator SHA-256
`ec462eda3d3951a60fec455136e6a905eba8045b2b2ae4f42e09bff1d1f4914f`
completes retained candidate A/B, including the narrow exact-source set-literal
rule, then fails candidate A/original at `clojure.core.async$go` on typed macro-
gensym `Symbol.intern` identities such as `c__6979__auto__` versus
`c__6079__auto__`. It implements several path, order, frame, capture, descriptor,
and member-backtracking repairs, but still has only the original 16 fixtures.
Adversarial negatives, persisted member witnesses, structural-Symbol and typed-
metadata coverage, full verification and runtime scans, end-to-end sealing,
accurate summaries, and deliberate wrapper repinning remain open; its temporary
`/tmp/compare-member-existing-17` evidence is not promotion evidence.
After adding typed gensym-Symbol constraints, the moving comparison reaches a
new explicit oracle boundary: five `core.async` state-machine closure classes
use different `AtomicReferenceArray` slot constants (for example IOC
`aget-object` slot 10 versus 20), while candidate A/B agree. Generic Long/LDC
normalization is forbidden; closure requires a persisted bijective typed slot
graph tied to mapped owner/member/capture/control-flow and every array read/
write edge, with conflict, cross-use, unmatched, and ordinary-long negatives.
The latest moving checkpoint has comparator SHA-256
`af195687a4392b334a23914159075e225edc8ed0798187c31f72a98178f3c131`.
Its typed state-slot recognizer proves the first `clojure.core.async/filter<`
root-`invoke` setter and then fails closed at the second setter because the
stored value comes from `GETSTATIC const__3`. A generic `GETSTATIC` allowance
would erase semantics; any accepted edge must bind the exact owner, field,
descriptor, value role, and slot graph and pass ordinary-static-field and
conflicting-slot negatives. This checkpoint remains unreviewed and unpinned,
still has only 16/16 legacy fixtures, and does not contain the required
integrated all-archive/candidate scanner.
A subsequent quiescent WIP at comparator SHA-256
`10c29f0ebd13a285f9aae2985aad7ac7b77d705f227557a6876b337306f14aaa`
passes 21/21 focused controls (self-test SHA-256
`3ad076db2ef528cf2cca2ebf30b1f777152c6cab6340e269921b899698920ddf`)
and recognizes every typed state-array `aget`/`aset` shape in the retained
corpus. It then fails three `core.async` skeletons because ordinary JVM local
indexes are permuted with downstream aliases. The retained failure and
comparison ledgers at `/tmp/datomic-slot-corpus13.MauBiW` have SHA-256 values
`1b66f06c668f3a89bb2298c72bc6491367af8401d8fe3796bc2f16e715a6a61b`
and `e5c49f25f0caff15ed55beee12083690308aab5e5b061e43d95462265694c519`.
No generic VAR-index normalization is allowed: closure requires a per-method
typed def-use/live-range graph over VAR, IINC, frame, and debug-table uses,
with exact `this`/parameter slots, category widths, control-flow and exception
anchors, state-slot edges, and adversarial conflict/ambiguity negatives. It
remains unpinned, non-equivalent, and without the integrated boundary scanner.

The next moving local-graph checkpoint was reported at comparator SHA-256
`a15fd7b8f6f54610e3c93f1d1bfc86d5137b4840c265620617d927b8675a9a79`;
it is not frozen, while `validate-exact-source-aot.sh` remains at SHA-256
`a36c79ed8eba995deaa4ec7dddfff96b3af7ed3b1212cacdc5399d94763cd367`
and still pins the earlier reviewed comparator.
That retained corpus failure has been superseded by a stronger, still-moving
checkpoint. Comparator SHA-256
`027849702a4b1b1fe7bd7223b4ba7178a3f8e72a8765701e3199fbf22af01a6e`
and compile-runner SHA-256
`c10f41439273457d414b519961706463721ba8fb3f0f372fe3b79de0e3e17d98`
pass 35/35 fixtures at
`/tmp/datomic-local-check.6hSkhy/self-test/self-test.tsv` (ledger SHA-256
`39e5f2436a3f523ab8d5f48a16b381def7c3275b32c0db4f62119da36f3ab06f`).
The added controls retain TOP, NULL, and UNINITIALIZED_THIS and reject local
category/width conflicts, parameter or `this` movement, same-arity cross-use
swaps, IINC mutation, LVT name/signature/control-range changes, unmatched local
nodes, and unknown generated-local IDs; the physical-slot remap positive also
passes. Candidate A/B now passes the earlier `do_alt` ambiguity using only the
pre-proved identity bijection. The current candidate/original failure is one
`clojure.core.async` pipeline state-machine closure: candidate LVT rows named
`from` and `jobs` pair by type and control region with two original `G__<id>`
rows. Because non-generated local names and LVT multiplicity remain protected,
the comparator correctly leaves this FAIL pending proof that pairing is wrong
or that the exact source/compiler lowering justifies a narrower relation.
Debug-only diagnostics remain in the moving source, the wrapper is still
pinned to its prior comparator, and the integrated full-boundary scanner and
explicit verifier remain absent. This is a later localized failure, not an
equivalence result that can be promoted.

A narrower subsequent checkpoint resolves that localized case without
globally erasing ordinary local names. Comparator SHA-256
`075f97fc4e4b038d0eac9c7a681a5eb4af8d0eb7f6f7c1ec142a71ad0259fe12`
passes 44/44 focused fixtures at
`/tmp/datomic-ioc-lvt-check.e7macy/self-test/self-test.tsv` (ledger SHA-256
`6f1d4bf755390583b330bd53b2063679ca56bfcb3b064a9e033507c952822bf7`).
The nine new controls restrict name variability to metadata-only LVT nodes in
the exact source-bound `async.clj` core.async IOC state-machine
`invoke()Object` shape; executable, frame, boundary, ordinary-method,
wrong-source, wrong-descriptor, and ambiguous/cross-node cases remain strict.
A third compile from exact `async.clj`, Clojure 1.11.4, Corretto 11.0.22, and
the Transactor compiler flags—changing only dependency load/AOT context—at
`/tmp/datomic-core-async-aot-deps.6hwX45` independently produces
`[ex_handler, ex_handler, jobs]` at the same three LVT-only nodes where the
retained candidate twins produce `[jobs, jobs, from]` and the oracle produces
`[G__6181, G__6181, jobs]`; the selected class is SHA-256
`6b1ce7e1534028deb51f6952ad0b0029c4702fb4c79d4310b1b55474f58e144c`.
This establishes compiler-context variability only for that narrow debug
metadata shape, not arbitrary local-name equivalence. The fresh 3,431-class
candidate-A/candidate-B/original run at
`/tmp/datomic-ioc-corpus.Yvq4op` nevertheless fails before comparison while
building compiler-ID class pairs: the same pipeline child is the sole
left/right ID-placeholder full-class skeleton difference. Its summary,
failure, and comparison ledgers have SHA-256 values
`3cc9a9b4f7a7656977570528b8fffec5ae9b8d0908a6d7fb800f52b1789750ba`,
`5c4b7127bd94e6803cdbcbe4b3497b6617f192c012320a84789db36d1bdebfec`,
and `ad1915ca0818464f17e6eba4495af0cf5728d86af5689f06336fdd41440be0a6`.
The narrow rule is therefore not yet consistently represented in the
pre-pairing skeleton; it must be applied at that earlier boundary with
wrong-source/method class-pair negatives, not replaced by generic skeleton
weakening. The wrapper, complete scanner, and verifier also remain unfinished,
so the checkpoint is diagnostic.

After correcting that predicate to key on exact `async.clj` SourceFile while
preserving the non-null SourceDebugExtension SMAP as ordinary exact class
payload, the fresh rerun at `/tmp/datomic-ioc-corpus.5pMtFh` clears the prior
skeleton difference but still fails closed before comparison: the full
`clojure.core.async` compiler-ID cohort has two valid pairings. Its failure
ledger has SHA-256
`c3809109a33ccfdc062f0b2bbc004fa1ba110f041aee6f72412d0d437f666fd3`.
No ordering or deterministic tie-break may choose between them; the next step
is to enumerate the two bijections and find an independent exact capture,
constructor, owner, or call-site graph constraint, with a genuinely symmetric
fixture that remains ambiguous.
A moving comparator snapshot at SHA-256
`30ff9b6f838f38033b9fc6350603ff5397212cf4a3ae6b80bccc74219dfe9ed1`
identifies the ambiguity as one two-node generated-class swap and adds the
ordered typed construction references from the already paired outer pipeline
class as an independent constraint. Its prior 44 controls still pass, but the
new constraint has not yet passed a direct candidate/oracle or whole-corpus
run and lacks its graph-disambiguation and true-symmetry controls. It is WIP,
not evidence that the ambiguity is resolved.
A subsequent diagnostic at `/tmp/datomic-construction-pairing.CUSUIc` adds
both controls and passes 45/45: ordered typed `NEW` references disambiguate the
real two children, while a truly symmetric unreferenced pair still rejects.
The direct candidate/oracle pairing then clears that ambiguity and stops at the
next exact boundary rather than erasing it: `core.async$alts_BANG_` has
generated `map__6925`/`p__6924` LVT spellings versus oracle
`map__6025`/`p__6024`, with no established compilation-unit ID mapping. A
third exact-source compile independently emits `map__4611`/`p__4610` at the
same two executable-linked map ranges and fixed boundary-parameter row. This
proves context variability only for that exact debug metadata shape. The
moving comparator may add a source-hash/class/method/descriptor/range/slot/
multiplicity-bound spelling rule with adversarial controls, but must keep the
executable local graph and boundary slot exact and must continue rejecting
generic unknown IDs. No corpus equivalence follows yet.

Structural class/dependency discovery merges 247 recovered Transactor sources
with 25 recovered Peer-core2 support sources, all 52 recovered Java classes,
and the sanitized 532-dependency closure. It proves zero collisions across all
10,192 recovered class paths versus 181,555 dependency classes, exact source
overlap for all 87 dependency-source namespaces, and absence of every original
implementation AOT artifact and JKS payload. An initial diagnostic load reached
271 cold passes plus `datomic.transactor-ext` in its bytecode-proven production
order, with zero timeouts, but was rejected because it copied two Peer-reference
AWS EDNs that the resource classification marks licensed evidence. The
corrected runner now stages separately authored reader tags, a generated
compatibility version, and explicit non-production empty AWS deferral maps;
denies all six reference resource hashes; verifies unique origins and parsed
semantics; and leaves packaging, UI, TLS generation, and AWS behavior explicitly
deferred. Fresh corrected discovery passes with zero collisions or source
differences. A separately rerun, fully input-bound discovery-only regression
also passes: it verifies the frozen source, ownership, class, distribution,
archive, and Peer-support manifests before doing any candidate work. The first
corrected full run was stopped before promotion when an
independent audit found that the wrapper recorded but did not independently
verify every canonical source/baseline manifest or include every staged/runtime
input in the top evidence closure. The tightened validator now seals 1,055
files that can affect the candidate or oracle JVM and rechecks that seal after
the load and surface phases. The next fully input-bound diagnostic ran at
`/tmp/datomic-transactor-structural-bound-full-v2`. Its load phase is complete:
271 namespaces pass cold, `datomic.transactor-ext` passes in the
bytecode-proven `datomic.transactor` preload order, and the 1,055-path declared
runtime-input seal verifies after the batch. The surface phase then exposed
both a runner-protocol defect and a real recovery defect. Jetty and AWS
libraries write log lines to raw stdout before the one valid surface EDN, so 36
probes were recorded `INVALID_OUTPUT`; extracting the single tagged EDN record
from every raw output nevertheless compared all 247 namespaces and found 246
exact surface matches. `datomic.rest` alone differs: candidate vars `api`,
`index`, `query`, and `stores` incorrectly acquire `:arglists` metadata,
whereas their original initializer applies a metadata map containing only
`:column` before binding each function root. `routes` is the matching positive
control: its original metadata has the same shape, and the recovered source
already retains `def` rather than `defn`. The pre-repair generic decompiler
rewrote every such `.bindRoot` function pattern as `defn`, thereby
inventing the arglists. A bytecode-constrained macrocompaction repair must emit
`def` plus a function root when the original metadata does not support `defn`,
pass a focused regression and the full 142-namespace Peer regression, and
regenerate the 247-source tree in independent twins. The first metadata-aware
repair keyed bind-root metadata by exact Var identity and required full
normalized arglist/form/name/variadic/metadata congruence before emitting
`defn`; its 26/26 focused markers passed. That superseded compact source and
validator had SHA-256 values
`f811ecef81fc94e7144f22504f2c87638508f7d1ef17d22d0b24ce2a02a5de91`
and `fb8ee7e4d9901463ea30fbe21f466c4578c126b94340cc812b70fe4784a341fe`.
An independent fresh `datomic.rest`
diagnostic repairs all four surface-affecting roots and retains the already
correct `routes` definition. It also renders `read-edn` as `def` plus a named
function and explicit metadata reset: the original typed `:arglists` order is
the reverse of its function-clause order, so a plain `defn` cannot preserve
both. The resulting candidate produces surface SHA-256
`918051c92a85b007a89412bd1778582d69c4259b513ddd6a298876ba4416124a`,
byte-identical to the oracle; the focused recovered source SHA-256 is
`01da56fb92982b14ccc37a57d055845ddc85d8caf865a3cd188e7df1be526a90`.
A later direct oracle JVM probe confirms the full intended Var metadata:
`api`, `index`, `query`, `stores`, and `routes` each retain exactly `:column`,
`:name`, and `:ns`; `read-edn` additionally retains ordered `([str] [stm
encoding])` arglists, and `set-storage-map` retains `([alias-uri-map])`.
Consequently adding `:name`/`:ns` during exact reset is correct, but the arity-
only structural surface is insufficient for promotion; the fresh source gate
must compare normalized full Var metadata, including argument-symbol metadata.
That gate's frozen contract is fail-closed and now has two deliberately
different views. The promotion view compares exact typed metadata for every
realized interned Var, including source-coordinate key presence and numeric
runtime type. A secondary diagnostic view may remove `:file`, `:line`,
`:column`, `:end-line`, and `:end-column` to cluster stable semantic failures,
but it cannot turn an exact failure into a pass. Any coordinate difference that
proves genuinely unavoidable must be enumerated Var-by-Var with initializer
evidence rather than waived globally. Both views normalize a Namespace value
to its name; recursively encode maps, lists, vectors, sets, symbols (including
symbol metadata), Vars, and Classes with explicit type tags; retain sequence
order; and canonicalize only unordered maps and sets. Any otherwise encountered
opaque or unsupported value must fail with its metadata path and runtime type
rather than fall back to `pr-str` or a class-only placeholder. Oracle and
candidate Var-name maps must then be exactly equal; arity/root-kind surfaces
remain complementary checks, not substitutes for metadata equality. That
complementary root surface must encode a Var-valued root by exact target
namespace/name so the twelve late-bound aliases cannot collapse to a shared
opaque kind. Every compiler-generated proxy scratch Var omitted from the
surface must also appear in a complete per-side exclusion ledger whose entries
and reasons compare exactly; a blanket predicate is not evidence by itself.
Two subsequent bound full recoveries at
`/tmp/datomic-goal2-transactor-source-final-v9` and `-v10` each pass all 247
namespaces (160 strict Datomic, two exact Datomic dependency sources, and 85
exact bundled sources) with zero failures or sentinels. Their source trees are
byte-identical; source-manifest file SHA-256 is
`f0790c74178195f15034d77bc8b59e567fa9546cd674f862c437cc71a278ec03`,
results SHA-256 is
`b0429931c643f4e282a0e17fce4e52cbc892e4308e675097b406de470336a21c`,
and summary SHA-256 is
`cad2a1992f73247dc9e3837032a59e17f62ec5758edf8681809d2a77ea49887c`.
The bound twin evidence at
`/tmp/datomic-goal2-transactor-source-final-v9-v10-evidence` reports PASS,
verifies inputs after both runs, has an empty source-tree diff, copied no
licensed originals, and has evidence-manifest file SHA-256
`87c5830494b76addafdd5c11c8fab40b23cc3a4f909c0e2fa0d275d7d25f1c30`.
Independent checks reverified both source manifests, 247 regular files and zero
source symlinks. Those facts prove deterministic processing only, not complete
recovery. An independent delta audit found that
`annotate-bind-root-metadata-body` destructured `[form & remaining]` and used
the truth of `form` as its end sentinel; a literal `nil` or `false` in a `do`
therefore discarded every following form. For example, candidate
`datomic/common.clj` shrank from 896 to 338 lines and ended after the
`assert-all` macro, losing later functions such as `await-derefs` and
`array-cat`. The pre-repair tree has 2,275 `defn` forms versus only 902 in v11;
976 `def`-plus-function transformations leave 397 functions dropped outright.
Of 1,878 surviving functions, 225 bodies also differ: 224 lose 474 literal
`nil` nodes; five of those bodies also lose a literal `false`, while
`backup/unreadable-seg-ids` is the sole additional false-only case. The earlier
`query.support/disallow-find-variants!` result was a parser-canonicalization
comparison artifact; its source file is byte-identical across the audited
trees. A broad census
also finds missing non-function definitions across `def`, `defonce`,
`defmulti`, `defmethod`, `defprotocol`, `defrecord`, `deftype`, and `declare`.
Thus v9/v10 are deterministic but semantically
incomplete and invalid. Their apparent 145-file delta is only a historical
diagnostic and must be recomputed after repair. The independent final-delta
lane has frozen the checked-in pre-repair source
boundary read-only at
`/tmp/datomic-source-delta-audit-baseline-20260828/source`: 247 regular source
files, 39 directories including the root, zero symlinks, and 5,270,449 source
bytes. Its 247-row manifest-file SHA-256 is
`6f92866cfabbb7854df00d79bf2627e57db761e2a02ed623fe374adc263f589e`.
This is the comparison baseline for the eventual frozen replacement, not a
claim that the baseline itself is semantically complete. A pre-promotion audit
also found
two other generic defects in that snapshot: `repair-definition-body` compared
only unqualified Var names and
could conflate qualified same-name Vars, while reset-metadata elision treated
`:doc` as disposable even when the reconstructed definition had no docstring.
The next frozen repair uses exact Var identity throughout and conservatively
retains documented metadata. It passes 27/27 focused markers, including
qualified `alpha/x` versus `beta/x` isolation and executable exact-doc
retention. Its compact and validator SHA-256 values are
`c288540c093962e86cb6ad7d292afb949c4fbffbdb0e5ae40b42ec92c44ec2db`
and `31d0c3c0a0d1413651bdffb11746384fea5c3e9e1e8747d83c289d423975f55e`.
The v9/v10 results are therefore sealed but superseded diagnostics; fresh
v11/v12 Transactor twins were started on this 27-marker snapshot, but an
independent source diff caught a missing positive identity case: a qualified
current-namespace Var such as `datomic.api/db` must match the local definition
`db`, while `alpha/x` and `beta/x` must remain distinct. Exact raw-symbol
comparison failed to pair the former and emitted both the original pre-bind
metadata reset and a synthesized post-definition reset across many files,
duplicating metadata-expression evaluation. The v11/v12 run was stopped and is
invalid/superseded. The next repair must carry namespace-aware Var identity,
prove qualified-current/local equality and cross-namespace inequality, and
prove a side-effecting metadata expression is evaluated exactly once. It must
also preserve and execute forms after literal `nil` and `false`, using collection
exhaustion rather than form truth as the loop sentinel, before fresh Transactor
twins and bound Peer regressions run. Because moving `setMeta` after a compacted
definition can also change observable order, only directly adjacent pairs, or
pairs separated solely by retained literal `nil`/`false` no-observation forms,
with strictly proved pure metadata construction may compact; unsafe, impure,
or observably separated pairs must retain explicit low-level `setMeta` then
`bindRoot` semantics and stable full Var identity. A control must observe the
original unbound -> metadata-visible -> bound sequence with one metadata
evaluation. The current pre-freeze audit also finds that non-function and
dynamic root sequences can fall outside that pairing and leave an unresolved
`(var x)` before any source definition. This is corpus-relevant: the checked-in
Datomic Transactor sources contain ten explicit `^{:dynamic true}` root
definitions. The replacement is not frozen until executable dynamic and
scalar-root fixtures prove intern/setDynamic/setMeta/bindRoot order and
targeted recovery and source loading cover every affected Datomic namespace.
An independent pre-macrocompaction extraction from the hash-pinned Transactor
confirms the real shape rather than the initial plain-Var synthetic model:
`datomic.assert` and `datomic.datalog` use nested targets such as
`(.setMeta (.setDynamic #'*level* true) ...)` and
`(.bindRoot (.setDynamic #'*level* true) 0)`, while `datalog/*cancel*` binds the
impure `(atom nil)` root. Temporary raw witnesses live at
`/tmp/datomic-goal2-raw-dynamic.44OmWnU1`; their source SHA-256 values are
`7ab7cb9c410223c7996590c8065d0b24c2731680cd822993504e2f09366c8da7`
and `49d6cf729f52f7337f912614dcbd4d906a8f665d2af7e8dd2d3f9bad2c25ddcb`.
They are diagnostic, not canonical promotion evidence, but require nested-
target and impure-root positive controls in the frozen repair.
On the now-superseded moving compact/validator SHA-256 values
`1b217ef7c5589f9ccf257d741cc25feb85d3ac11e41d2733c03793a3c383dfcf`
and `9d0b7967943f2534bcb288b6425de7cc358687b40b34daaa5cac9b394e7f9a9e`,
an independent pre/post-hash-stable focused run passed all 39 markers. A
targeted recovery at `/tmp/datomic-goal2-targeted-source.uqiKkleo` then
read-validated 11/11 namespaces: all eight dynamic-root namespaces plus
`datomic.common`, `datomic.backup`, and `datomic.api`. Its 11-row source-
manifest file has SHA-256
`05f351adfa1e7289e51edc6cb69c025da3469b9ccfdeab8b59aae6eb826739df`.
That candidate retained `common/await-derefs`, `common/array-cat`, and
`backup/unreadable-seg-ids`; preserved three nested `setDynamic` calls and the
impure roots in `datalog` and `math` through qualified `RT.var` targets; kept
the qualified two-operation `setDynamic`/`setMeta` no-bind shape for the four
unbound `*retry*` Vars; and emitted no opaque marker or unresolved plain-Var
target sentinel. It is nevertheless a failed/stale rehearsal, not positive
promotion evidence, because its qualified `datomic.datalog/q` bind *value* was
compile-time strict.

The current repair converts only the exact top-level bind-value shape
`(var qualified/name)` (or its `clojure.core/var` spelling) to the bytecode-
equivalent late-bound `RT.var`; it does not recursively rewrite arbitrary Var
forms or alter bind targets. Its compact and validator SHA-256 values are
`d9e14e54210b4e8bc4e8a428f058a9298374cfe21b31be51af47f4b5e7f66139`
and `b9def4cfa7afa7358998b13cb3ecb1facf189948871249756e8a56b7a5cfae44`.
An independent pre/post-hash-stable run passes all 40 focused markers. The new
fixture proves that an absent target namespace need not exist at compile time,
that the recovered root is identical to the unbound target Var created by
`RT.var`, and that source-Var metadata survives. Fresh focused `datomic.cache`
and `datomic.datalog` outputs at `/tmp/datomic-late-bound-focused-v19` have
SHA-256 values
`c6c5229c464795a628afbb1f7b91596ce026521fccc1df87beccbd1d78db41e8`
and `117f38c9eddfbfc63c4c629131205a9d85cad2cd32cf7dd1456b1b637372e976`.
An independent baseline-derived audit corrects the earlier cardinality claim:
there are twelve aliases, not eleven. `datomic.cache` has eleven—five into
`datomic.cache.impl`, including the previously missed `cache-keys`, and six
into `datomic.cache.caffeine`, including `create-response-map`—plus
`datomic.datalog/q`. All twelve use value-side `RT.var`, with no missing,
extra, changed, or compile-strict qualified `(var ...)` bind value. The
machine report `/tmp/datomic-late-bound-focused-v19/late-bound-alias-audit.edn`
has SHA-256
`ff3f595a28addcbf8af6fcf00c88edcf2f153b2a2cd8a4ed8ed31e4226de65bc`.
An exact-scope negative helper proves six nonmatching shapes plus a nested bind
value remain unchanged; its report SHA-256 is
`1bc1d2a9f1fbd4694e57c42cecd6d4f077a7a03957f0550efc9fe2b755d88946`.
The focused two-file normalized delta matches 104/104 definition events and
bodies plus 149/149 `nil` and 6/6 `false` nodes; its report SHA-256 is
`9e360b8386f96b67ea27fa29823b0b9c48cdc96bdc5e9e97bf87e60499b8af32`.
Its raw source skeletons intentionally differ because the recovery retains
low-level metadata/root operations, so that diagnostic is not a raw-text
equivalence claim.

The sealed exact-typed runtime-metadata oracle at
`/tmp/datomic-targeted-full-var-metadata-v3` exposes a broader, actionable
defect before full regeneration. Var name sets and counts are exact, but all
449 candidate Vars carry `:column` as `java.lang.Long` where the initializer
carries `java.lang.Integer`. After normalizing only that numeric class for
diagnosis, exactly 33 residuals remain: ten protocol-root Vars differ only in
source location; 21 protocol methods include 17 lost real docstrings and four
stable nested method-symbol-metadata differences; and the ordinary functions
`datomic.index/drop-avet-indexes` and `datomic.math/uniform` lose nested post/
precondition source-column metadata. By namespace the residual split is 11 in
`datomic.backup`, two in `datomic.common`, five in `datomic.datalog`, seven in
`datomic.index`, seven in `datomic.kv-store`, and one in `datomic.math`;
`datomic.api`, `datomic.assert`, both Cassandra-value namespaces, and
`datomic.ddb-values` otherwise match. There are zero unsupported values and no
unclassified residuals. The comparison report SHA-256 is
`428a8b450edf2401bb622fe751141086e5ec0dce272d65bd90147844645e7b02`;
its comparator SHA-256 is
`30aa9de232939a6b6a4f6ada595d5464ac2f7604961839c9a4b7e033476b86c3`.
The current self-excluding evidence-manifest SHA-256 is
`47a3f3dad5863f4e5362b9f22053ac62f4e04b7651c1ac063af3bdcc36a4db27`,
and an independent final verification log has SHA-256
`5cae6dea857e2c6a6192ef3ee1d050f6e7adc8118a677ba093168cecec16104d`
with zero failed rows. This supersedes root
`46afb4b023a15d5d9cfe7fd4cb1dc215c4328badb0795866840c498988ec7b85`
only because the sealed tool-input manifest replaced one row with the
strengthened per-file `nil`/`false` semantic-auditor hash; the captured metadata
and comparison did not rerun or change, and substituting that prior tool-input
member digest reconstructs the prior root exactly. An earlier self-referential
manifest was rejected and is not evidence. Raw initializer forms retain the
exact metadata needed to repair these paths, so numeric/coordinate
normalization remains diagnostic only: the recovery must preserve typed integer
literals, protocol docs/signature metadata, nested condition metadata, and
exact post-macro Var metadata.
An independent implementation audit localizes two of those losses rather than
treating them as unexplained output drift. The generic sugar pass currently
folds `java.lang.Integer/valueOf` (and the other boxed numeric `valueOf`
calls) to an untyped `:const` AST node before the later compact pass can emit
an `(int ...)` form; the wrapper identity is therefore already gone when the
source is printed. Any repair must retain the proved boxed-call type at that
AST boundary and must not coerce unrelated primitive integer constants or
quoted data. Separately, the current `defprotocol` compaction rule destructures
only each signature's `:arglists` and applies `protocol-arg-symbol`, thereby
discarding method docs and nested parameter-symbol metadata by construction.
The protocol repair must carry the full signature metadata and restore exact
protocol-root metadata after macro expansion, with negative fixtures for
ordinary numeric/quoted data and non-protocol symbols.
A later frozen generic implementation of that repair has compact SHA-256
`14bcf6d21a39170a5ea52fdd42c5bc1182fa502a10e57b2a4488b885f5e4203f`,
pprint SHA-256
`2c2fa6e0b5805d9e7fc2b4b500b4e06c13303f3f4a3eda43f678cdebd1965440`,
and validator SHA-256
`08b44942b43c146f4407e2f7386903cb8954a4e746bf652d049d4e71125860f6`.
An independent Java 21 replay passes all 44 printed focused markers, including
typed-metadata versus quoted-domain isolation, semantic `.withMeta`
protection, and generic protocol order/metadata/scaffold negatives. A freshly
decompiled `datomic.kv-store` at `/tmp/datomic-protocol-repair-v20` then
produces candidate and original exact-surface SHA-256
`aaf999cb84b7b60aaadd0d91fcdda2cb2ecac41da7908486cd4c5acbc30aa84e`.
That focused directory is intentionally diagnostic and unsealed: it proves the
repair shape but not a frozen toolchain, complete input closure, candidate
classpath isolation, or the other protocol/source paths. The all-eleven and
all-247 gates remain decisive.
The same compact/pprint/validator hashes produced a fresh diagnostic complete
recovery at `/tmp/datomic-full-exact-v22`: all 247 initializers pass, all 247
source paths are unique, there are zero `BROKEN DECOMP` sentinels, its 247-row
source-manifest file has SHA-256
`8198136dafa9770688c7c9e16eea613de42a9bb5d940a5b8bbaf8c5ef24751d4`,
and independent manifest verification passes. This is the first complete tree
from the generic typed/protocol repair, but it is not promotable yet. The
existing full-tree delta validator fails before comparison on
`clojure/tools/analyzer/jvm.clj`'s `::ana/resolved-op`, because its reader does
not establish the source namespace alias context; the retained failing audit
root is `/tmp/datomic-v22-independent-audit.NsXF2xTb/delta`. A source-aware,
fail-closed reader plus auto-resolved alias/current-namespace controls is only
the first repair. The same validator currently removes every `reset-meta!`
from its source skeleton, selects only the first reset grouped by name, and
does not prove exact reset count, order, target, or payload; it could therefore
hide an added, duplicated, moved, or changed reset. The replacement full-tree
audit must permit only the bytecode-proved `defn` to `def`+`fn`+exact-reset
transition while preserving the ordered event stream and every other form,
literal, and semantic metadata value, with adversarial reset negatives, before
a fresh complete old-to-new audit can count. A direct textual census shows the
scale of that boundary: v22 contains 2,499 `reset-meta!` calls across 157 files
versus 578 across 92 files in the checked-in historical tree, and 159 files
differ, all under `datomic/`. These are investigation counts, not permission
to normalize all 1,921 additional calls.
The moving reader now gets past both `::ana/resolved-op` and the exact legacy
`ring/util/codec.clj` `#=(int \=)` form. Its self-test exercises the actual
corpus alias option orderings and prefix specs, quoted setup, namespace cleanup,
the exact SHA-bound legacy exception and wrong-hash/second-occurrence controls,
plus duplicate, extra, moved, wrong-target, and wrong-payload reset negatives.
At validator SHA-256
`6856b2b37e28e8444ae83ef408fc047e997323124ddfae7c2b6e89211436d17a`,
the first complete checked-in-to-v22 run at
`/tmp/datomic-full-ordered-delta-v2.jC5PAn5I` parses all 247 files and fails
honestly. Its summary SHA-256 is
`ef5c829dc8f81dff1b969eef8275bb65511525de4043d895de7be4e5d091aa0a`:
88 files are text-identical, 159 differ, 1,875 definition events are compared,
847 satisfy the sole currently approved `defn` to `def`+`fn`+exact-reset
relation, and 6,860 issues remain. The first is `aggregation/sample`: the
`defn` clauses remain exact but an adjacent typed-metadata reset is newly
inserted. This may justify a second narrowly bytecode-proved same-`defn` plus
exact-reset event class; it is not permission to admit arbitrary resets.
`datomic/api` then exposes removed high-level definitions/resets and added raw
`.setMeta`/`.bindRoot` forms that should be repaired or independently proved,
not hidden by alignment. Twenty-six `function-clauses-changed` rows may include
syntax-quote gensym noise and still require classification. The validator is
therefore useful diagnostic WIP, not yet a frozen promotion gate.

The next fail-closed audit revision has SHA-256
`199060dfcdc0f915e4d6740c1dfa4f8dc1b05e72ee7b2b4a5442d088820750b0`.
It encodes regex Patterns by exact pattern and flags, proving that all 26 prior
function-clause differences were reader-object identity false positives, and
adds adversarial exact-reset addition/order/target/payload/duplicate/moved
controls. Its complete v3 run at
`/tmp/datomic-full-ordered-delta-v3.ElVKKM` still fails honestly: the summary,
ordered-event, and issue ledgers have SHA-256 values
`112d14112015083f503b2b2582ba3054e5ad5a156ce762228d224113f18bb462`,
`ee969026e0095f4af49a1d5bfda5d9138650404e4e852fee1f50b7209ce23520`,
and
`ecf4e9780ac4c91a2f3d7e8e5fbfb2c1b795b50ab90bb35a5150496e895d6122`.
It admits exactly 851 same-`defn` plus adjacent exact-reset events and 851
`defn` to `def`+`fn`+exact-reset events, leaves 173 definition deltas, and
reports 5,797 issues. All 173 definition deltas still have exact encoded
function clauses; their reset-conversion diagnostics partition into 110
argument-metadata mismatches, 28 compiler-local spelling mismatches, 19
argument-form/name mismatches, seven malformed arglists, five arglist-order
mismatches, and four `datomic.rest` definitions whose prior metadata omitted
arglists. This narrows those events to definition/reset representation and
metadata proof, but does not approve any changed reset. The remaining 87
unapproved definition/reset transitions all retain a reset on both sides but
change that historical reset; they remain failures and are separately counted
among 169 existing-reset changes. No function-clause-change issue remains. The
six-file self-excluding diagnostic
manifest has SHA-256
`93cfff07ba80b9d154663491f60bcd434b1e729cb806dc9a1b52c7bd126ae64b`
and verifies in full.
The ten targeted protocol roots are not a corpus census: the current
`transactor/src-clj/datomic` tree contains 79 `defprotocol` forms. The repair
must therefore be generic, and the fresh full-247 runtime metadata gate—not the
eleven-namespace slice—decides protocol completeness.

An independent full-oracle inventory at
`/tmp/datomic-full-metadata-inventory.2aIbmNhv` extends that emitter boundary
from the targeted eleven namespaces to all 247 Transactor namespaces. The exact encoder
completed 246 namespaces and failed closed on exactly one value:
`clojure.tools.analyzer.passes.add-binding-atom/add-binding-atom` has an
anonymous zero-arity function at metadata path `[:pass-info :state]`, with
oracle class suffix `$fn__2655`; the source-loaded candidate uses `$fn__392`,
so deleting the compiler number or comparing callable arity would be a false
exactness claim. The remaining observed value types are fully classified as
keywords, namespace/Var/Class references, symbols, booleans, Integer/Long,
strings, `nil`, and Clojure maps, sets, lists/LazySeqs, and vectors. The run
verified all 533 oracle dependency hashes, recorded 247 bounded isolated
results, and left every per-process working directory file-free. Its
self-excluding 997-member evidence-manifest SHA-256 is
`f7e86cd16bd95068ed4384d2d35d285f7bd09da3d4cfdab248b1e68671976b0c`;
the independent verification-log SHA-256 is
`4a166f79ebd477f2a3a096b272d7ff1354fc9fc6188bc4cf9f6fac8014d6ee57`.
This is a diagnostic failure, not candidate evidence. The promotion comparator
must either prove a fail-closed semantic fingerprint for that sole state
factory—bounded behavior plus the separately exact bundled-source ownership is
the current narrow option—or reject it; it must reject all unclassified
callables and include a negative that distinguishes different zero-arity
bodies.

The first moving implementation of that narrow semantic proxy, emitter SHA-256
`442ebac6931aee631be5fb2b832e7dc1330d2450576f82b5e44af477d9009db4`,
passes its direct-verifier self-test but still fails the real child-process path
at `/tmp/datomic-metadata-proxy-replay.OrbxGLMt`. The generated child expression
uses `clojure.core/throw`; `throw` is a special form and cannot be namespace-
qualified, so the child exits one at compile-syntax-check before exercising the
factory. The temporary instrumented replay preserves the exact child error.
This exposes a self-test coverage gap as well as the need for a dedicated,
noise-safe tagged child payload and retained raw diagnostics. The 247-namespace
zero-unsupported rerun remains open until the production path—not an injected
direct verifier—passes.

A later moving emitter at SHA-256
`1beaaa39c33a742551668e301c28a75e7b8529dcffb11ff2bb98b3bccb9d7c4f`
now passes that real process path independently for both sides at
`/tmp/datomic-metadata-proxy-current.s4H5ax5R`. The original runtime and the
candidate-only 537-entry structural classpath produce byte-identical canonical
surfaces (SHA-256
`c31d9f5e4a88589ba0d75fbf59bf348a961ea749ec3b57900f08d524dd657ef5`),
exercise two fresh empty Atoms, and leave both isolated scratch roots empty.
Their raw compiler-generated factory classes deliberately differ
(`$fn__2655` versus `$fn__636`) while both ledger entries resolve to the same
path-bound `:fresh-empty-atom-factory-v1` contract. This focused pass also
exposed a separate runtime-boundary requirement: without the shipped
`clojure.compiler.elide-meta='[:doc :file :line]'` setting, the source-loaded
candidate retained `:doc`, `:file`, and `:line` absent from the licensed AOT
surface; with that exact setting it matched while retaining `:column` and
`:pass-info`. The candidate classpath table has SHA-256
`9974e551f9286e4c9cadbb69ecba5b0e8fb96f69d573123c1bea7fe76919c093`
and contains no Peer, Transactor, or core2 implementation JAR. The current
Peer-twin launcher still omits this compiler setting, so its next run is
blocked on reproducing and binding it rather than hiding the resulting metadata
delta. The structural worker has now been repaired at SHA-256
`964fcd56ab8ea6f34da3f5fea6025ee80f23d1035ae89b5369019819084fd9a2`.
An independent candidate/oracle worker replay at
`/tmp/datomic-structural-elide-worker.XKQlmu8G` records PASS/PASS, exact
dedicated surface SHA-256
`c31d9f5e4a88589ba0d75fbf59bf348a961ea749ec3b57900f08d524dd657ef5`,
and zero process-working-directory or scratch files. The callable case is now
a focused positive, not yet a full-corpus result: freeze the emitter and its
production self-test, then rerun all 247 namespaces with zero unsupported
values and the exact compiler setting.

One attempted emitter freeze at SHA-256
`5167aba6ee7f86e7e7d6a1c013b180b1b31e407a4e0fb1e53f830c4fb78fb3c5`
was rejected during independent Peer-harness review before its active all-247
surface run could count. Its proxy-exclusion ledger used
`(.getName (class @v))`; because the root is itself a `Class`, every excluded
proxy was mislabeled `java.lang.Class` rather than identifying the generated
class. The focused callable proof above remains useful, but this emitter and
any corpus result produced by it are diagnostic only. The replacement must
record the actual `^Class @v` identity, bind each exclusion to its source
proxy shape, and include a collision negative before a fresh all-247 replay.
The same production semantic-proxy helper currently redirects only child
stdout while leaving stderr as an unread pipe and silently ignores failure of
`File.delete` on its temporary log. Before freezing, it must drain/capture
stderr without deadlock, retain useful bounded failure diagnostics, make temp
cleanup failure fatal, and prove noisy-child/error/cleanup negatives; the
structural worker's process-working-directory seal alone does not cover the
default global Java temp directory.

The replacement surface/recovery freeze now closes those specific defects
without claiming the still-open full-tree delta relation. Its shared emitter,
structural wrapper, and isolated worker have SHA-256 values
`a1ca4454920514f650bbcc76034e5269e20f083a4ab33806678feef27429a497`,
`f82eb4a09c903a8325d17f2f54ae4aebc6457f4de6ede8f95e363d8c82a13c86`,
and
`a2f69b29336c3a52cf3c3a40cf0bda0d91a16e50d6ee0b81738068b29296dba9`.
The compact, pprint, and 44-control validator remain exactly the hashes above;
the Transactor recovery runner and per-namespace worker are frozen at
`152b43a22b2c4fe4c47feb295b0b549ec337985c030ad6b94550193a5a5ae562`
and
`7c46eb7c34373be1a3ce7387bfdef9ee41fded664df24f63d64e3d29d5e6c2c1`.
The emitter self-test now covers real proxy Class identity, lane-specific
class origin, separate child output/error capture, noisy/error children,
second-temp creation, primary-plus-cleanup failure, and fatal deletion; its
author retained probes are rooted at `/tmp/datomic-emitter-freeze-probe-v1`.
An independent replay at `/tmp/datomic-emitter-independent-v1.ms3Fyp` passes
with empty stderr and Java temp, while the independent 44/44 validator replay
at `/tmp/datomic-validator-independent-v1.DmzcNu` is hash-stable and also
leaves its temp root empty. Independent candidate-source/oracle-AOT probes at
`/tmp/datomic-surface-independent-v1.Jmt6cN` reproduce exact surface SHA-256
`c31d9f5e4a88589ba0d75fbf59bf348a961ea749ec3b57900f08d524dd657ef5`
and prove respectively absent versus present Class resource/CodeSource
origins. A separate real `ring.util.servlet` replay at
`/tmp/datomic-proxy-independent-v1.XoDy7L` reproduces exact surface SHA-256
`f23ed3b34f9066fb4ffbe3ef8e0abb05d64b2b413b7f5571678433d8674dc09a`;
the candidate ledger has two distinct generated root-Class identities, neither
`java.lang.Class`, while the AOT oracle excludes none. These are focused
positives; the fresh all-247 relation and ordered old-to-new source audit still
decide promotion.

The first attempted all-247 replay with this freeze was stopped and excluded
because the driver accidentally supplied initializer-munged namespace names
instead of the 247 source-declared names; it is not recovery evidence. The
corrected, discovery-bound replay at
`/tmp/datomic-247-surface-freeze-v1b.pb6Zc5` runs every source-declared
namespace in a separate candidate and oracle JVM. Both lanes pass 247/247
processes, leave their working directories, isolated HOME, and explicit Java
temp roots file-free, and reverify 247 candidate sources, 332 candidate-owned
inputs, 532 candidate dependencies, 533 oracle dependencies, and all five
frozen tool inputs after execution. The candidate classpath has 537 entries,
contains the single sanitized Nano derivative, and has zero original
Peer/Transactor/core2/Nano archive or Nano-keystore hash hits. This is an
honest FAIL: 228 canonical typed Var surfaces match and 19 differ. The 19 are
fully accounted for by extra nested argument-symbol `:line` metadata in eleven
namespaces, over-retained protocol method-name symbol metadata in three,
missing nested default-value `:column` metadata in three, and compiler-gensym
symbol spelling in two (one of those also has line differences). The
namespace relation has SHA-256
`16c5f8b0128703249ca54c7b4a31a9c13cfe3d45f39511648c0cb11c7e1a5bde`.
The 303-leaf evidence and 19-row classification ledgers have SHA-256 values
`90e2a4a6eaff8e30d4fe669248b947a9cd8ae880c1fc394d7340d333ea1d890b`
and
`2a79292dbc9cfbb97ac9e63001d0c5cfe41e2fa1fb1c5888d7d0c4cd73329b51`;
they separate fifteen hash-bound bundled-source/AOT namespaces from four true
decompiler-recovery namespaces (`datomic.artemis-client`,
`datomic.external-sort`, `datomic.peer-client`, and `datomic.valcache`).
The candidate also records ten source-bound generated-proxy exclusions across
eight namespaces versus zero for the oracle, and the one path-bound
`add-binding-atom` semantic proxy has the already proved opposite lane origins.

The same run exposes a separate provenance boundary that the value surface
alone does not show: raw metadata runtime-type ledgers match in only 164/247
namespaces and differ in 83. The frozen side-ledger comparator fails first on
the source-loaded `clojure.tools.reader.default-data-readers`, where
`SubVector` and `PersistentVector$ChunkedSeq` appear only in the candidate.
The raw type-relation SHA-256 is
`e51cb3a91c0c9037757189f1f6cf877f613265a2f0db747298292ba3016115a4`.
This must not be hidden by a generic collection-type projection. An
independent original-free AOT rebuild of the exact hash-bound shipped source
at `/tmp/datomic-aot-probe-default-reader.7ZmVJY`, using Corretto 11.0.22 and
the licensed `[:doc :file :line]` elision setting, produces the exact oracle
surface SHA-256
`56447c0d1500a889088c45c3756803b7c9094a1bd36f600a6abfb4f7897d5525`,
the exact oracle metadata-type set, and an empty proxy-exclusion ledger. That
proves this observed type delta is source-evaluation-versus-AOT provenance,
not permission to normalize values. The promotion gate must therefore compare
the 85 hash-bound shipped sources AOT-to-AOT while retaining source-exact and
source-delta proof for the genuinely decompiled Datomic cohort. The replay's
FAIL summary and self-excluding 3,543-file evidence manifests have SHA-256
values
`af1e9d877125e1c6db4b82456b74652936ee0bb4e5339baf720fbc9f5e46e2ba`
and
`41189983cfd0adc8f7ee609c44e6cfe462a41c30fc589dd6592ebde64a9a2d88`;
the latter independently verifies in full.

That AOT provenance test now covers all fifteen hash-bound bundled-source
namespaces implicated by the 19-row surface classification. The isolated
Corretto 11 replay at `/tmp/datomic-bundled-aot-all-v1.dr2bl9rL` compiles and
loads 15/15 and exactly matches all fifteen oracle raw-metadata-type,
semantic-proxy, and proxy-exclusion ledgers. Canonical value surfaces match in
13/15. The only residuals are 29 paths containing compiler-assigned gensym
spellings: two `p1__...#` paths in `cognitect.http-client` and 27
`context__...__auto__` paths in `liberator.core`; their surrounding values and
all three side ledgers match. This is evidence for a narrow compiler-provenance
relation, not a generic compiler-number normalization. The 15-row relation,
summary, residual-path ledger, original evidence manifest, and extended
manifest have SHA-256 values
`743686c8c994431d77be032fab0a2c599e7aacac2b085cd14bfb8d1c8ba43618`,
`c72fc031dfca023ffe68302520393275022abbab8847fd5be3dcb9376778de3a`,
`3854355587c5dba7466a1ae8bbe0abcb4813f9d11f8590360ff5877e34576f5f`,
`a60ff36bf870c177200bfb314e3262b55584420cbeabd61fb2b2a1154e48d2b0`,
and
`d8c15b579f1824b81825cd27f6aeabc047f8e9ebcb5f65d72317417939fa2fb8`.

The structural validator now binds that setting to primary artifact evidence
instead of merely repeating it in a worker command: validator SHA-256
`a16d67fa0ac9f98ee0e62ca1e2e5de7f5a14ab180facd5f389cfde75baed5ddf`
hash-checks the licensed Transactor and then requires its embedded Maven POM to
contain the exact
`-Dclojure.compiler.elide-meta='[:doc :file :line]'` VM argument. A fresh
discovery-only replay at
`/tmp/datomic-structural-pom-proof.DfQPhiaH/run` passes with 247 Transactor and
25 Peer-core2 namespaces, 532 candidate dependency JARs including exactly one
sanitized nano substitution, zero candidate class collisions, no original
Peer/Transactor/core2 implementation AOT on the candidate classpath, and the
compiler setting recorded in its summary. Its recursively verified evidence
manifest has SHA-256
`2933199100af83135b1ca760488bce5e56ddad03f642a7155ed2997386106505`.
This is a discovery/input-boundary result only; it executes no namespace load
or surface comparison and does not replace the fresh full structural gate.

The independent 11-file delta rehearsal matches 424/424 canonical event names
and order, literal-node censuses of 561/561 `nil` and 40/40 `false`, seven/seven
unique dynamic Var names, and all 79 shared explicit-reset metadata payloads.
It classifies 24 additional candidate metadata events as retained macro/
declaration transitions, including six declaration-to-definition targets,
rather than silently dropping them. Its strengthened body invariant correctly
downgrades the stale tree to 423/424 exact normalized definition bodies: the
sole mismatch is `datomic.datalog/q`'s strict qualified Var value. The final
audit must run against a fresh full tree, prove exactly the expected twelve
late-bound alias values with no extra rewrites, cover low-level targets as well
as values, and prove exact typed full runtime metadata for every realized Var.

A candidate-only hybrid diagnostic at `/tmp/datomic-dynamic-hybrid-v18` now
loads nine sensitive namespaces in nine separate JVMs: `datomic.assert`,
`datomic.cache`, `datomic.cassandra-values`, `datomic.cassandra-values-v4`,
`datomic.datalog`, `datomic.ddb-values`, `datomic.index`, `datomic.kv-store`,
and `datomic.math`. Its classpath is the candidate source overlay plus the
candidate Peer-core2 sources and Java classes, four separately staged runtime
resources, stubs, and the audited 532-dependency closure; it contains no
original Peer or Transactor implementation AOT. This closes the previously
observed circular-alias load failure diagnostically. The exact commands,
per-namespace output, classpath/origin proof, alias audit, and input seal still
need a retained evidence bundle before the result is promotable.

A pre-run adversarial review also blocks the current Peer-twin wrapper,
`scripts/recover-peer-regression-twins.sh` at SHA-256
`7c5fe503aaf77775a0f75cd993c10f7c1f975cb77b5b5f02032a0bfe3b2c3bef`,
from promotion as written. It seals many file inputs but does not require the
two run roots and evidence root to be pairwise non-ancestor/disjoint from the
licensed distribution; does not sanitize JVM/Bash injection variables or bind
an isolated HOME/TMP; hashes the Java launcher but not the complete JDK image;
omits the `/usr/bin/env` shebang interpreter from its tool seal; does not
reverify both recovered source manifests after runtime probing; and has no
failure ledger/manifest. Its candidate-runtime exclusion claim is inferred
from linked dependency basenames rather than a complete classpath-origin/hash
scan, and the Peer-only lane still stages the unsanitized Nano dependency.
Its per-namespace surface probe also selects `tail -n 1` from raw stdout rather
than the already proved dedicated/tagged EDN channel, so late or interleaved
library logging can still hide or corrupt the canonical payload.
Finally, arbitrary source-body differences from the preserved Peer reference
are merely counted when the coarse runtime surface passes. The wrapper must be
hardened and the expected source/AST delta independently constrained before a
fresh Peer twin result can be treated as semantic regression evidence.
The surface runner now has
a dedicated EDN channel: a logging-heavy `datomic.cloudwatch` smoke proves that
raw logs remain preserved while the canonical payload compares cleanly, and a
`datomic.rest` smoke correctly reports the genuine difference. After the source
repair, both the exact-source and fully sealed structural runs must start from
new roots; this diagnostic run will not be promoted. Stage 1 remains open until
the fully bound
resource/load/surface gate and the normalized exact-source AOT relation close
without original implementation fallback.

### Stage 2 — Reconcile the shared semantic kernel

**Status:** In progress

**Outcome:** Correct Transactor-specific versions of the namespaces shared in
name with the Peer, with reuse and divergence justified by evidence.

**Focus:** Compare database values, transaction representations, validation,
builtins, stored functions, serialization, log/index structures, storage,
cluster, catalog, and coordination code across both artifacts. Preserve exact
ABI and authored surfaces while repairing only bytecode-constrained recovery
defects.

**Completion signal:** All 117 overlaps are classified and resolved; the
Transactor shared kernel builds and loads without original Peer/Transactor AOT
fallback; surface and focused behavior comparisons have no unexplained
differences.

### Stage 3 — Establish a recovered Transactor process

**Status:** In progress

**Outcome:** A bounded recovered process that owns its launch, configuration,
lifecycle, and shutdown without yet claiming transaction correctness.

**Focus:** Recover launcher, Transactor entry points, configuration and
extension layers, lifecycle wiring, process ownership, resource cleanup, and a
minimal no-write startup mode.

**Completion signal:** The source-built Transactor starts from a recorded
candidate classpath, reaches a deterministic ready boundary, and stops cleanly
under success and injected startup failure without loading the original
Transactor implementation.

### Stage 4 — Make PostgreSQL the proven durable substrate

**Status:** In progress

**Outcome:** A recovered Transactor foundation that can safely create, locate,
read, and update Datomic storage state through disposable PostgreSQL.

**Focus:** SQL and key/value adapters, revision CAS, clustered values and
references, system catalog, database identity, log-tail and index-root
references, active endpoint coordination, and failure-safe root publication.

**Completion signal:** Disposable PostgreSQL gates prove exact value bytes,
revision behavior, catalog/coordination semantics, database identity, and root
publication/rejection while all candidate and service boundaries remain
auditable and cleanup-complete.

### Stage 5 — Recover transactions and the durable log

**Status:** In progress

**Outcome:** An authoritative single-writer path whose transaction semantics,
total order, durable commit, and acknowledgement boundary are understood and
validated.

**Focus:** The Transactor update processor and writer; tempids, lookup refs,
uniqueness/upserts, CAS, schema changes, transaction functions, basis `t`
assignment, request ordering, log tree construction, durable publication,
notifications, and crash/failure boundaries.

**Completion signal:** Direct and end-to-end PostgreSQL workloads match the
licensed oracle for accepted and rejected transactions; concurrent submissions
produce one monotonic order; injected failures demonstrate that success is
never acknowledged before the exact durable commit boundary.

### Stage 6 — Produce indexes and complete the Peer round trip

**Status:** In progress

**Outcome:** Committed transactions become persistent indexes and flow through
the real Peer–Transactor protocol into correct immutable Peer database values.

**Focus:** Persistent index scheduling/building/publication; EAVT, AEVT, AVET,
and VAET/reverse-reference roots; memory-tail replay and Peer adoption;
Transactor Artemis server, shared Fressian protocol, Peer connector,
notifications, syncs, errors, reconnection, and cleanup.

**Completion signal:** A recovered Peer and recovered Transactor complete the
full submit–commit–notify–query cycle on PostgreSQL without original AOT
implementations; index lag/adoption and transport interruption tests preserve
the exact logical database state.

### Stage 7 — Prove active/standby correctness

**Status:** Pending

**Outcome:** The recovered lifecycle and storage coordination enforce one
authoritative writer across standby, failure, and takeover transitions.

**Focus:** Master and standby loops, heartbeats, fencing, endpoint discovery,
semaphores, takeover, reconnection, in-flight outcomes, monotonic `t`, and
split-brain prevention.

**Completion signal:** Bounded failover matrices show one active writer,
monotonic committed history, deterministic client recovery, no duplicate or
lost acknowledged transaction, and no writable split-brain interval.

### Stage 8 — Complete data lifecycle, features, and operations

**Status:** Pending

**Outcome:** The recovered core supports the major correctness-sensitive and
production-facing facilities beyond ordinary transactions.

**Focus:** In this priority order: full-text writer/search integration;
filesystem and S3 backup/restore adapters and CLI; stored-value garbage
collection, excision, integrity, and repair; non-authoritative caches and
value-cache layers; metrics, monitoring, logging/rotation, licensing,
authentication, diagnostics, and provisioning. Keep JVM GC telemetry distinct
from Datomic stored-value reclamation.

**Completion signal:** Each included facility has an explicit artifact/source
map, isolated candidate validation, failure and cleanup coverage, and a bounded
claim that composes with the PostgreSQL-backed core without weakening its
durability or correctness guarantees.

### Stage 9 — Recover optional access tiers and close the system map

**Status:** Pending

**Outcome:** The remaining Datomic-owned interfaces are either recovered and
validated or explicitly deferred with a complete, evidence-backed boundary.

**Focus:** In order: Peer Server and thin Client API; REST; Presto integration;
Console; then alternative storage backends such as DynamoDB/S3, Cassandra,
H2/dev, and Hot Rod only after PostgreSQL and higher-priority components are
stable. Reconcile all documentation, reproduction paths, and subsystem maps.

**Completion signal:** Optional artifacts and interfaces have reproducible
source/evidence or a precise justified deferral; the complete classic Datomic
topology and source ownership map is documented; all required core stages are
green; and no known gap is mislabeled as a recovered component.

## Current continuation

**Active workstream:** The repository-owned PostgreSQL main path is green from
the corrected current sources. Close the exhaustive Stage 1 exact-source and
surface boundary, then classify all 117 Stage 2 overlaps before advancing the
concrete failure, transport, and HA gates. Do not reopen an unconstrained
residual-by-residual loop.

**PostgreSQL vertical-slice checkpoint (2026-08-28):** The recovered
Transactor and recovered Peer now complete the main path through provisioned
PostgreSQL storage: process boot, Datomic catalog/database initialization,
Peer transaction submission, durable SQL/log publication, notification and
query, process restart, and Peer log/index adoption. The strict `vslice8` run
used a resolved Peer classpath with the licensed Peer, Transactor, core2, and
original Nano implementations absent and exactly one sanitized Nano
derivative present. Its seed produced basis `1001`, 64 logical rows, 41
PostgreSQL KV rows, 15,250 stored value bytes, a log-tail row, and a published
index-root reference. A fresh recovered Transactor restart loaded
the same database, advanced log catchup from `index-t 66` to `index-t 1001`,
and the recovered Peer snapshot reproduced every seed hash exactly. From that
strict restart state, a second recovered-Peer transaction advanced basis to
`1066` and produced 96 logical rows. PostgreSQL then held 42 KV rows and
18,992 stored value bytes. A third fresh recovered Transactor replayed 64,032
bytes from `tail-t 1066, index-t 66`, and the final recovered-Peer snapshot
reproduced the second commit's database id, basis, row count, and every hash
exactly. The earlier `vslice7` seed/restart/augment result is retained only as
a diagnostic: its Peer dependency list still contained the original Nano JAR,
so it is not promotion evidence. Full evidence, hashes, repairs, and remaining limits are
recorded in
`datomic-rev/transactor/reports/postgresql-vertical-slice.md`.

The historical `vslice8` inputs were subsequently found unsuitable as a
reproducible gate: its Transactor discovery record pointed at live repository
sources, its Peer artifact was stale against 11 current Peer files, and the
canonical Peer artifact still packaged two licensed JKS resources. Those facts
are no longer papered over. The new repository runner
`datomic-rev/transactor/scripts/validate-postgresql-vertical-slice.sh`, SHA-256
`3544bdb504ab7926359cbfe366c3bcbc8c85ad14292f68d1e4827cc228e88639`,
rebuilds and byte-compares the current Peer, stages a JKS-free runtime
derivative, snapshots current Transactor roots, rebuilds resources, proves all
142 Peer and 272 Transactor/core2 origins, and recursively seals both
classpaths. Its no-service preflight passed at
`/tmp/datomic-recovered-pair-gate-dry-v7`, including the sealed focused
runtime-regression probe; its evidence-manifest SHA-256 is
`94cc29daebbedec4123d712f31f127bacad449df6d5e620014dbab79095eed0c`.

The current executing repository-gate run passed at
`/tmp/datomic-recovered-pair-live-v3`. Seed basis `1001`/64 rows survived a
fresh Transactor and fresh data-directory restart with exact database id,
basis, row count, and four semantic hashes; catchup replayed 37,372 bytes from
`tail-t 1001, index-t 66`. The augment advanced to basis `1066`/96 rows and
survived a third fresh process/data directory with the same exact checks;
catchup replayed 64,032 bytes from `tail-t 1066, index-t 66`. PostgreSQL state
was exactly 41 rows/15,254 value bytes after seed and 42/18,996 after augment,
unchanged across each restart. All three Transactors stopped by bounded
`SIGINT` with no TERM/KILL, both service ports closed, and `pg_controldata`
reported `shut down`. The self-verifying evidence-manifest SHA-256 is
`100245c7dbb8639ea4bbc8ad46594818bfb9909e6aae75a3b79c1b26263d24bf`;
the candidate-runtime seal SHA-256 is
`b821ed9c68a20f3f21baeb7f22b727f6caa176ef9dfadecd2ebab40647db7403`;
the runtime-membership SHA-256 is
`ab1c42d424061a8ed309e550f5092ffb5cc03711c9c80b4a000d09418c244236`.
The resolved Peer and Transactor classpath ledgers have SHA-256 values
`0cb5a2b425b0a979a0baf37272393184eee22b25e369a541b4185fbc2ee67935`
and `5d009a86b73ec4c926fb2f420598479986becf3e3d5cb172770869ddc47fdbc8`.
The database id is
`recovered-pair-dad95659-f007-463c-8dd5-f1a7d9b4d63f`; seed/restart
fingerprints match at
`071b6799a2fbf4e70d25787d1cc0a5819eba73850bd086dc74a37bad34d00b0e`
and augment/restart fingerprints match at
`a5186eb24ddc24d34c0f2615953daa05c5bb65acc3e6618fe9ad453bb8f0171f`.
The v1 and v2 runs remain historical main-path evidence. v1 predates the
checked-in `compare-byte-arrays` correction; v2 used the corrected source but
did not integrate the focused runtime validator into the gate itself.

Before that run, the six earlier deliberate runtime repairs in `datomic.common`,
`datomic.db`, `datomic.future`, `datomic.kv-cluster`,
`datomic.memory-size`, and `datomic.update` passed their AOT/decompiler and
focused behavior checks. Together with the `compare-byte-arrays` correction
described below, they were honestly repinned. The current 247-row source
manifest SHA-256 is
`6f27a4259ea02bb3eba6214d44b7c155d0d3dda1128a8d0be5301c2d00257786`.
A fresh structural root at
`/tmp/datomic-transactor-recovered-pair-current-v2` then passed 272/272 namespace
loads (271 cold, one documented order-dependent) with evidence-manifest
SHA-256
`5490edf69d9ae86a1d78edf9644e1196ebf644bbea649c022e83b141f58b5bc8`.
Its candidate classpath is
`/tmp/datomic-transactor-recovered-pair-current-v2/evidence/candidate-classpath.tsv`,
SHA-256
`ba0f0c6d56fa59e0060b6f662dc8be95cba766a4519839cff2fc232f1b85e4b1`.
Structural surfaces remain deferred; they were not silently counted as run.

The confirmed `datomic.common/compare-byte-arrays` defect is now repaired and
revalidated rather than carried as an open runtime item. Exact AOT class
`datomic/common$compare_byte_arrays.class`, SHA-256
`23e34d076e57e17dce2947ffa8a20b000cfe888d55cc512ee7059f95bab9f4fe`,
proves that the loop body continuation at bytecode offset 114 follows one
forward `goto` to the sole `LRETURN` at 120; it does not produce the stale
synthetic trailing `nil` that remained in the checked-in source. The generic
continuation-aware loop recovery, exact-label decompiler regression, focused
signed-byte/equal/prefix behavior matrix, 247-row source-manifest check, fresh
272-namespace structural load, and the complete v3 PostgreSQL gate all pass.
The v3 runner copies, seals, and executes the focused runtime validator at
SHA-256
`9e661bd3211dd3c483675d08138421d4255f97ae0cb1311f5d7adbe731c66a42`
before it starts PostgreSQL, so `recovery.common.compare-byte-arrays=PASS` is
an executed probe rather than a report-only marker.

The corpus analyzer also now handles ordinary multi-form `ns` sources without
accepting namespace declarations nested in `comment`; its focused fixture
passes and a regenerated 142-namespace Peer index is byte-identical to the
checked-in machine outputs. A legacy third-party `#=` reader form still blocks
using that analyzer over the entire bundled Transactor dependency corpus with
`*read-eval*` disabled. That safe-reader limitation is explicit and is not a
runtime-gate blocker.

The empty-schema startup probe also exposed a real recovery defect rather
than a storage feature gap: both recovered `root-cause` functions discarded
their recursive branch result, so retry reporting crashed with a secondary
NPE. Exact Transactor AOT and the preserved Peer source prove the recursive
deepest-cause form. The existing generic dead-`ATHROW` lexical-boundary repair
now freshly recovers both functions, and one exact AOT-shaped decompiler
regression plus one focused runtime regression pass. With that defect fixed,
the same probe correctly reports PostgreSQL's missing `datomic_kvs` relation;
external table provisioning is therefore an explicit harness precondition.

This checkpoint advances Stages 2--6 but does not close them. Stage 2 still
needs final classification of all 117 overlaps. Stage 3 now proves graceful
success-path shutdown but still needs injected startup-failure cleanup. Stage
4 still needs the exact CAS/root rejection and failure matrix. Stage 5 still
needs rejection,
concurrency, and fault-injected acknowledgement-boundary tests. Stage 6 still
needs persistent-index scheduling/publication and transport-interruption
coverage. Stage 7 HA remains pending.

**Superseded anti-ceremony checkpoint (2026-08-28):** Before the runtime result
above, the recovery work had produced a real candidate corpus, but the most
recent residual-by-residual source quotient and acceptance-scanner refinements
had ceased to be the shortest route to the goal's educational finish line.
The 189 exact-source residuals, the report-only `defonce` analysis, and the
remaining scanner/wrapper gaps stay recorded as open evidence obligations, but
further refinement is deferred until direct execution exposes a defect that
requires it. The moving validator was restored to frozen v10 SHA-256
`a283dd2c5577bbfd45db7693e60ace898deed9b713061f9201b6220cff396051`
and passes its source-first self-test. The scanner was left buildable at
SHA-256
`c9703d7a7d4289484bd2f5078b6de638e22891bbbfa2db7c5d76f92dc5a6055d`;
its 31/31 self-tests pass, while production scanner/verifier/wrapper execution
remains honestly `NOT_RUN`. This is a dependency-order change, not a claim that
Stage 1 is complete: direct PostgreSQL boot/write is now the critical-path
probe, and only concrete failures from that probe should pull lower-level
proof work back onto the active path.

**Proven:** Stage 0 is closed; the full archive and dependency boundary are
hash-bound; every one of 9,682 classes and 11 resources has a deterministic
owner or explicit treatment; and the reproducible pre-repair 247-source tree
accounts for
160 strict Datomic decompiles, two exact Datomic sources with 36/36 raw AOT
matches, and 85 exact bundled sources. The earlier hardened decompiler snapshot
passed 24 focused checks and the complete 142-namespace Peer regression,
including all 16 IOC value exits, but those results predate the newly proved
`datomic.rest` metadata issue. A superseded 26-marker metadata-aware snapshot
produced byte-identical 247/247 Transactor v9/v10 twins, but final audit caught
qualified-same-name Var aliasing and doc-metadata loss before promotion. The
repaired snapshot passed 27/27 focused markers, but an independent v11 source
diff exposed duplicate metadata evaluation caused by treating a qualified
current-namespace Var and its local definition as different identities. v11/v12
were stopped and superseded. The same audit then proved v9--v11 source
truncation after falsey forms, invalidating even the deterministic twins.
A later frozen repair covers namespace-aware identity, single evaluation,
literal-`nil`/`false` tail retention, typed location values, and generic protocol
reconstruction and produced the verified v22 tree. The current decompiler adds
the bytecode-proved captured-field/function-name discrimination, bounded
impure-loop return repair, and persistent pre-namespace-load protocol-method
Var discrimination described below. Its source-first validator passes 52/52
tagged groups; v22 predates these final repairs and is still only diagnostic.
The fresh whole-tree failures below, rather than these superseded snapshots,
now define the promotion boundary.
The Java slice independently reproduces 46 sources to all 52 classes with
exact ABI/code and an isolated compile/oracle boundary. Sanitized nano and the
recursively scanned 532-artifact dependency closure pass twin-build/audit
gates, all 533 dependency dispositions are explicit, and corrected structural
discovery proves zero recovered/dependency class collisions with four
candidate-owned runtime resources. The first load run was rejected for AWS
resource provenance, and the first corrected full run was stopped for
incomplete evidence binding. The subsequent 1,055-input-sealed diagnostic
proved 271 cold loads plus the one bytecode-supported production-order load and
verified the post-load/post-surface input seals. That historical run remains
non-promotable because its raw-output protocol obscured 36 probes. The
replacement dedicated-channel all-247 run now supersedes its surface diagnosis:
all 247 processes pass on both sides, with 228 exact surfaces and the 19
classified differences recorded above.
The current structural validator additionally proves the exact compiler
metadata-elision setting from the hash-pinned Transactor's embedded POM; its
fresh discovery-only replay passes and has a recursively verified evidence
manifest, but executes neither loads nor surfaces and is not a Stage 1 pass.

**Still open:** The verified v22 source tree is diagnostic, not promotable.
Its correct all-247 replay loads every namespace on both sides but retains the
19 exact value-surface differences and 83 raw runtime-type-ledger differences
recorded above. The current full-tree ordered delta reads all 247 files and has
removed the regex-identity false positives. Its fresh v8 replay remains an
honest expected failure and now reports 804 residual issues after the exact
v6--v8 relation stages described below. The v6 baseline reduced the issue
count from 5,797 to 1,658 while emitting 2,558 explicit events: 921
identical-`defn` plus adjacent exact resets, 959
`defn`-to-`def`+`fn`+exact-reset relations, 380 exact adjacent
`.setMeta`+`.bindRoot` Var-initialization units, and 47 unchanged scalar
definitions plus exact reset additions. An independent potential enumerator
matches all 427 newly accepted units with zero misses and zero invalid
admissions. The remaining issues include changed or removed historical resets,
removed or added forms, one changed function-clause relation, and other
body/metadata/order changes; all remain failures. No blanket reset,
coordinate, compiler-number, or collection-representation normalization is
permitted. The next source snapshot must repair or independently prove every
remaining class, then pass the complete ordered old-to-new audit and a fresh
247-namespace value/type gate with the 85 shipped sources compared AOT-to-AOT.
The prior 5,797-issue ledger was highly concentrated:
`datomic/db.clj` accounted for 2,852. Its sorted per-file ledger has SHA-256
`94ef36f3a4ef8116c62934d81aab0b68ddab47fbb4c536c441d6f92ec81f5f30`.
A read-only audit at `/tmp/datomic-db-delta-analysis.EDHuGR` proves this file is
overwhelmingly one early alignment cascade. The first divergence is an exact
metadata reset added after scalar `BOOT-IDS` at outer item 8; the earlier
validator neither treated scalar-Var initialization as a unit nor looked more
than eight items ahead. It then reported 2,445/2,852 issues beneath already
mispaired outer forms. Of 244 old function definitions, 235/236 ordinary
paired definitions have exact encoded clauses, eight function-valued
`.bindRoot` forms have exact clauses against their old definitions, and the
sole `add-hook` residual contains the same scalar `.setMeta`/`.bindRoot`
transformation inside its body. All 69 adjacent top-level low-level Var units
target old definitions: 61 have exact initializers and eight have those exact
function clauses. The compactor correctly retained all 69 because neither the
root nor final typed metadata satisfied its rewrite-purity proof. Therefore
the current ledger exposes no independent `datomic/db` logic defect and must
not drive hand edits. The audit summary, low-level-unit, function-clause, and
outer-coordinate ledgers have SHA-256 values
`30635883955c8086c920ba390fead5281965dc921f83dfccd5ba096f795b2d43`,
`1ceb9c42e3149ee4cdc5e30fcff38f580f44c95f1d35a2fd52b21c00f34e855a`,
`47dd28c73aab788ff1cfdc3657f6d8cff519fc130db9ec13bb5d51e25e8ab48b`,
and
`8b9b22599281143099ade0b31e5b37cd3dfe781d69248f327b2855958efba77c`.
The exact relation now accepts a unit only when the old canonical definition's
active source namespace, exact RT Var target, adjacency, scalar initializer or
encoded function clauses, and historical metadata all prove the
transformation. Historical metadata may differ only by exact
`:column n`-to-`:column (int n)` typing; an absent historical reset permits
only exact `{:column (int 1)}`. Adversarial target, qualified-target, value,
clause, metadata, order, interposition, duplication, and wrong-reset controls
pass. `datomic/db.clj` falls from 2,852 issues to 327 with all 69 independently
audited Var units and all nine scalar resets recognized; current top residuals
are `db` 327, `log` 91, `valcache` 58, `backup` 54, and `index` 52. The
validator has SHA-256
`09e6e8049bb565042760cf750e61ebd8fc91b5c77a1fc65a6cf7192a62516536`.
The nine-file self-excluding evidence manifest at
`/tmp/datomic-full-var-unit-v6.x9bFlAa1/evidence` has SHA-256
`4cbd333be4ce06a8e76b25c5bb3c712da5b563e40a3c5a01c68992eb1b413f56`;
its independent verification log has SHA-256
`af631be7062b4f1bff1e7562c2ec0ea2d82913a97bfa1aad2e75597bcfde1e87`.
This is alignment proof, not permission to normalize raw Var operations.

The next exact source-delta unit is now implemented and independently
replayed. A direct one-symbol `(declare x)` is paired only with a same-position
low-level `.setMeta` on the exact same source-namespace/runtime-Var identity,
where the runtime target name is unqualified, both identities are unique in
the direct source stream, and the payload is encoded-exactly
`{:declared true :column (int 1)}`. Qualified and unqualified old declaration
spellings are the two positives; changed namespace/name/column/operation,
qualified runtime names, missing or extra metadata, multi-symbol declarations,
duplicates, reordering, and intervening forms remain failures. The fresh v7
corpus therefore remains an honest expected failure, but falls from 1,658 to
1,391 issues and `datomic/db.clj` falls from 327 to 230. It records 75 exact
same-position declaration events; an independent census finds 82 possible
identities, leaving seven position-shifted cases visible rather than forcing
alignment. Definition, function-clause, reset, and semantic-metadata issue
cohorts are unchanged. The validator has SHA-256
`c19d3b495c2c8d98ea4bf942b8dcc64c49991bd247ab487b69c5318704105e92`.
Its source-first self-test independently passes with an empty Java temp root.
The self-excluding 18-file evidence manifest at
`/tmp/datomic-full-declare-unit-v7-final.6yuRc1y` has SHA-256
`dd783583faec1c0012e418256946e371d3ab03ee8555247e702f02083f4095ea`;
all entries independently reverify, with summary and issue-ledger SHA-256
values `625d62b68c6c8143418128a2c069c3d0f1db1126093915c0e2ca1f391af18010`
and `1772b5f5f1d53adc53dac6fe005065105212977e7da0945166ea5b01be0e471f`.
This remains sequence alignment proof, not protocol/record/type normalization.

The next exact relation is also implemented, frozen, and independently
replayed. It accepts a canonical `reset-meta!` pair only when both sides have
the exact same source target and position, each direct reset identity is unique
on its side, both metadata operands are literal maps, and reconstructing the
new complete form with its sole `:column (int n)` value replaced by the old
literal integer `:column n` makes the encoded forms identical. The same proof
is consumed at the existing definition-adjacent reset boundary; there is no
nearby-anchor or free alignment scan. Two positives, eight negatives, direct
and definition-adjacent integrations, and duplicate-old, duplicate-new,
reordering, and intervening-form safeguards pass. An independent source-first
self-test passes and leaves its Java temp root empty. The fresh v8 corpus
remains an expected failure but falls from 1,391 to 804 issues, while
`datomic/db.clj` falls from 230 to 62. All 341 independently enumerated exact
candidates produce exactly 341 valid events with unique counts 1/1, zero
misses, and zero invalid admissions. Only the four frozen reset-gated cohorts
move; function clauses, ordinary and semantic changes, invalid payloads, extra
forms, and removed forms remain failures. The validator now has SHA-256
`ecb736a37ad7247e7887709b668e23718b55206bb9a2b9774a08a74c6485f8a1`.
The self-excluding 22-file evidence manifest at
`/tmp/datomic-full-reset-cast-v8-final.vHv7ZpU` has SHA-256
`3751a1f1bf29cd811703200220f4ede15994c5cbebdc76005250b813925e896b`;
all entries independently reverify. Its summary, issue, and ordered-event
ledger SHA-256 values are
`d0288cd3d81f58828a03c57f4b08fdaadf2f90481c07e895b87350ffdd1f76ec`,
`27cf7d8431b94cf27050bcca126924393b16b8e00b02c73a025ad5e9ddc91e65`,
and
`93fe7d6627f55ba77dc8f6b1bedabd190362dc30dab381232270bdec77336cb4`.
This is exact source-representation alignment proof, not permission to erase
arbitrary location metadata; all 804 residuals remain open.

The exact protocol-initialization relation selected by the read-only v8 census
is now implemented, frozen, and independently replayed. An adjacent old
`(defonce P {})` plus `(defprotocol P ...)` pair can correspond to one
compiler-expanded new protocol `let` wrapper only when the empty cache,
protocol identity and monotone order, source namespace and runtime Var targets,
unit uniqueness on both sides, exact `{:column (int 1)}`, protocol metadata
reset, full body cardinality, every method arglist/doc/tag, and every
compiler-local binding use prove the whole unit. The admitted relation is
documentation-only: method tag changes remain failures, as do every other
non-documentation protocol-content change. Two positives, eight negatives,
and explicit adjacency, duplicate-old/new, reorder, intervening-form,
wrong-namespace, and tagged-method safeguards pass. All 71/71 frozen
candidates emit one exact event with zero missing, extra, invalid, or duplicate
keys. The tagged `datomic.memcached/RecoveringClientImpl` and the new-only
`datomic.cluster/ClusteredStore`, `Get2`, and `RefClusterStore` units emit no
event and remain visible.

The fresh v9 corpus is still an honest expected failure, but it exactly
reproduces the simulated reduction: 804 to 349 residual issues, with
`datomic/db.clj` falling from 62 to 6. Ordered events rise from 2,974 to 3,052:
71 are the new protocol-unit events, while the corrected alignment exposes
seven events already accepted by older exact relations. The only remaining
issue cohorts are five definition changes, 156 extra forms, 119 removed forms,
one function-clause change, three removed historical resets, four invalid reset
payloads, 58 ordinary-form changes, two semantic-metadata changes, and one
unapproved definition/reset transition. The validator SHA-256 is
`a69b2203943f03f3fa898bda6f1e92e0372f4623215e40ff46d0437c967b0598`.
An independent source-first replay passed with a fresh Java temp root remaining
empty, and an independent full-corpus replay reproduced exit 1, 247 files, 71
protocol events, and all 349 residuals. The 35-entry self-excluding evidence
manifest at `/tmp/datomic-full-protocol-unit-v9-final.g0VZEFp` has SHA-256
`46937ea532f6314e1deba073d9a2f96081e6ce860d081cc59d0fd536873d9811`
and all entries independently reverify. Its report, summary, issue ledger, and
ordered-event ledger SHA-256 values are
`c773d810384e35b2fbcf19f03f1f3969faf062522dfd79005f89995d5b9bb504`,
`523d20f0ab4d7c04363f37083c1889f7ac9c13b11badf50df87746852f286036`,
`765819160b4ad51cbd1e9282aa9eca374e00a85ce830706d4faa322d427d85cc`,
and
`880b561f010e931dc791d091fb6339923c8c8c300f3bd9e19d4b282d77e4a494`.
This closes only one exact source-representation relation; all 349 residuals,
the corpus, and Stage 1 remain open.

The next exact source relation is now implemented, frozen, and independently
replayed. `exact-defmulti-initialization-unit` accepts one three-item
`(defmulti Name Dispatch)` form with no docs/options/semantic metadata only
against its adjacent exact `.setMeta` plus complete single-body guarded
`MultiFn` wrapper. It proves source namespace and Var identity at every target,
reference, and string; exact `{:column (int 1)}`; body cardinality; compiler-
local uses including the sole exact `clojure.lang.Var` tag at `.hasRoot`;
encoded dispatch equality; `:default`; global hierarchy; uniqueness;
adjacency; and relative order. Two positives, twelve independent negatives,
and duplicate-old/new, reorder, and intervening-form safeguards pass. All 22
independently enumerated candidates across 15 files reconcile exactly by path,
identity, namespace, indexes, and four form hashes, with zero missing, extra,
invalid, or duplicate events. All 25 nearby `.setMeta`/`let` lookalikes prove
to originate from `defonce`, while 30 `defmulti` forms in text-identical
dependency sources emit no event.

The fresh v10 corpus remains an honest expected failure but falls from 349 to
189 issues: extra forms 156 to 80, removed forms 119 to 65, and ordinary form
changes 58 to 28, with every other issue cohort unchanged. Ordered events rise
from 3,052 to 3,074 solely through the 22 new exact units; all 71 protocol
events and every older event-kind count are preserved. `datomic/db.clj` has no
candidate and its six-issue ledger is byte-identical. The validator SHA-256 is
`a283dd2c5577bbfd45db7693e60ace898deed9b713061f9201b6220cff396051`.
Independent source-first and full-corpus replays reproduce exit 0 for the
self-test, exit 1 for the corpus, 247 files, 3,074 events, and all 189
residuals, with both fresh Java temp roots empty. The 43-entry self-excluding
manifest at `/tmp/datomic-full-defmulti-v10-final.5t0J5wt` has SHA-256
`a1fb4a22948289ad79a62483834c6d6fa823c1d28b6c54fad38dd8bda3837e2f`
and independently reverifies. This closes only the exact defmulti relation;
all 189 residuals, the corpus, and Stage 1 remain open.

The hardened Peer harness now consumes the frozen emitter, worker, Nano
sanitizer, and explicit Peer decompiler Java-temp contract. Its classpath audit
scans candidate directories by content and rejects renamed licensed
Peer/Transactor/core2/Nano archives as well as archive-shaped nested files,
closing the earlier directory-origin hole. The first two fresh attempts each
recovered all 142 sources but stopped on harness-only manifest/resource-
selection defects before compilation or runtime comparison. The corrected R3
attempt then recovered byte-identical 142-source twins and passed all 142
source-only namespace loads, but its behavior gate failed with a
`StackOverflowError` in `datomic.future/filling-promise`. The preserved Peer
reference correctly emits an anonymous function that closes over argument
`f`; the fresh decompiler emits named `(fn f ...)`, so the body resolves `f`
to itself and recurses forever. This is a genuine semantic decompiler
regression, not a harness failure. The generic repair is now implemented: a
class-derived function name is suppressed only when the generated function
class declares a same-named non-static field, its constructor writes that
exact same-class field, and an invocation method reads it. Explicit `letfn`
names remain authoritative. Negatives cover genuine recursion, unrelated or
static fields, absent constructor writes or invocation reads, and foreign
field owners. The 45/45 validator replay includes an evaluated closure with
the `filling-promise` shadowing shape and proves the captured outer function is
called exactly once; its stdout has SHA-256
`67ee27b2440ceeff4673f66da2ae3ab0651f2f9dead5d9fbe5febfe199dfe717`.
The frozen AST, source emitter, and validator hashes are
`43b54d9cbfbe750c524dc1dbf2885a0ba9cb180ddd1583eabf763003ac1f5f5f`,
`2dab0b7494c56ca646650397bf7f79372a0e4aca8781516a659a83c17dcab179`,
and
`9c0443c48e1e1d656e564fb180525898fc869e56b2fc8ba7a11edf1415d22319`.
R3 stopped before exact source, side-ledger, and full surface comparison and is
retained only as fail-closed negative evidence at
`/tmp/datomic-goal2-peer-frozen-20260828-r3-evidence`. The targeted recovery
now emits the closure anonymously, but promotion still requires a new pair of
142-source twins through every comparison plus classpath, post-run manifest,
temp, and cwd seals.

Fresh R4 twins prove that captured-function repair and expose the next generic
control-flow defect. R4 produced byte-identical 142-source trees, passed all
142 source-only namespace loads, and cleared `datomic.future`, then failed
closed before any surface files at `datomic.common/compare-byte-arrays`.
Recovery emits `(do (loop ...) nil)` in the equal-length branch, discarding the
loop's primitive-`long` result and causing unboxing to throw. The licensed
class has SHA-256
`078107584eeaa7f199fe03094ded1ccb0da4c54425dcca65c8bdab93b37df7bd`;
its bytecode proves zero/nonzero comparison results merge at label 114, flow
through one forward `goto` to the sole `lreturn` at 120, and contain no
`aconst_null` after the loop begins. The bytecode EDN and exact control-flow
ledger have SHA-256 values
`d4f599e364a77ac2b9438911fd1e566a564a44f979f68579228d6e566e036dfb`
and
`120936d5396434fb30d3402ed97209db7850b62f3bbc22515adf7860caf3458a`.
The 443-file self-excluding R4 negative-evidence manifest at
`/tmp/datomic-goal2-peer-frozen-20260828-r4-evidence` has SHA-256
`0c2c21cd6fdd7ac2dd6ac140734be78d04a78f23d396023b8e129706c7a7ae52`
and independently reverifies. The generic repair is now frozen and
independently replayed. `process-impure-loop` distinguishes the loop entry from
the returned body continuation, and `will-ret?` accepts at most one real,
strictly forward, unconditional jump from that continuation to an explicit
value-return epilogue. Three positive forms and nine negative control-flow
shapes reject backward, self, chained, missing-target, fall-off,
non-scaffolding, conditional, void-return, and branched-epilogue cases;
process witnesses prove that the returned body continuation rather than the
return-looking entry is classified. The current AST and validator have
SHA-256 values
`ab962601e45467585aa41951b879a5b6f0fa6c3e96e0562bd18c4b4e63041a20`
and
`f74e8da02f051a1987e2b81adc8852b8e12c9a773212704103c587172e935ebb`.
An independent source-first replay passed all 47 tagged groups and left its
Java temp directory empty. Three isolated targeted recoveries are
byte-identical at
`b2aaa84d70f6d8a47dcf878fddba5d940adb06a7b6953843fb87fa5b00c5e07d`;
their direct-loop structural check, source read/load, and eight-case
candidate-only `compare-byte-arrays`/`compare-ex` behavior matrix pass. The
actual-JVM candidate classpath has 536 unique entries, no forbidden
proprietary AOT or original Peer/Transactor/core2/Nano, and exactly one
sanitized Nano. The self-excluding 1,663-file evidence manifest at
`/tmp/datomic-peer-loop-return-v1.WtZREpJj/evidence` has SHA-256
`e42ca9fe1d3758c9ea26a5ce1148584be57c39d81bcedaa11450b6b59f4ea130`;
the independent verification log has SHA-256
`012e78524a5c71641379cfa391f2ee690ab434c1a71a77cb3084f92342b1174a`.
A wholly fresh full R5 run is now frozen as verified negative evidence, not a
Peer promotion. Both new roots recover 142/142 namespaces, their source
manifests are byte-identical at
`882f920ff8c50a0e132a16db38b991e7777160b53b279f8f3b3a4f28ccfde1dc`,
and all 142 source-only loads plus the focused future, byte-array,
object-array, record, throw, and nested-reify behaviors pass on the isolated
candidate runtime. All 142 exact surface probes complete without a namespace
exception: 136 pass and six fail only the exact-metadata relation:
`datomic.artemis-client`, `datomic.external-sort`, `datomic.valcache`,
`datomic.core2.aws.s3.sdkv1`, `datomic.core2.async`, and
`datomic.core2.val-store.s3`. The first three confirm the prior nested-column
family; the three core2 failures are newly visible in this fresh Peer order.
The earlier statement that this R5 run also proved `datomic.peer-client` fixed
was incorrect and is retracted: that namespace is not in the 142-source Peer
recovery/index and R5 did not probe it. The sealed reconciliation now proves
that `datomic.peer-client` is Transactor-only: Peer contains zero
`datomic/peer_client*` entries while Transactor contains 53. Two fresh current
recoveries are byte-identical and reproduce the same `unwrap-proxies` nested
`:name` metadata residual, so the stale checked-in source is not causal. The
licensed initializer has already pre-interned the `unwrap-proxies` method Var
with a plain name symbol before its first `in-ns`; the later rich `intern`
input preserves that existing plain symbol. In the structurally identical
passing `datomic.monitor/Metrics.metrics` control, no Var exists before
`in-ns`, so `intern` creates it with the rich nested name metadata. The current
protocol expansion loses this bytecode-visible state distinction by forcing a
rich name for every method. The smallest generic boundary is therefore a
per-method, initializer-proved pre-intern marker propagated into protocol
expansion: only marked methods use a plain name symbol, and unmarked methods
retain the current rich name. No namespace special-case or global stripping is
admissible. The read-only report has SHA-256
`d6cae6459961250fbc024c76a640c1821b426d8b3026c7167571a3c7280db17f`;
its 29-entry manifest at
`/tmp/datomic-nested-list-metadata-repair-v1.7NC7GYcO/evidence/peer-client-reconciliation-manifest.sha256`
has SHA-256
`93fe8a998a7e2a8a6ae04b2916e6be389865974ae09967288f0bd4aac5a5761f`
and independently reverifies. Its verification log has SHA-256
`f88f5949a6014c86fbcbabbd2d39c9c69a07cb35de445c09be441488cdd2aa7f`.
That marker is now implemented and frozen at the deliberately narrow boundary.
The namespace initializer must expose exactly one zero-arity void `load` call;
its executed pre-load prefix and same-class `__initN` helpers must be straight-
line and handler-free. Only a literal `RT.classForName(String)` target whose
own straight-line `<clinit>` stores an exact literal `RT.var(String,String)`
result through `Var` checkcast into its own static field is eligible, and every
subsequent target/namespace instruction through `load` must belong to a narrow
mapping-preserving compiler-constant grammar. Direct `RT.var`, conditional or
post-load work, unmap/remove, arbitrary helpers, unmodeled field/class calls,
and non-String/non-long constant-pool forms fail closed. The shipped
`clojure-1.11.4.jar` and `RT.class` are hash-bound at
`fc7ff1b6610d0e3494a75cc11ef81810bda402ac157012fe4e3b899586cf4133`
and `abf65cbd762a0c078dc54b793419f6ea3852b2455b0544133283c2903029b4f0`;
the exact `RT.classForName(String)` implementation proves initialization is
requested. Actual bytecode marks only
`datomic.peer-client/unwrap-proxies`; the structurally nearby
`datomic.monitor/metrics` control remains unmarked and retains rich name
metadata. Two fresh recoveries are byte-identical at SHA-256
`7b2cb2005acf61a5d467a98149f669d4ca0c4da7af77aaed52e3f8c90b88aca4`
and `5329145fa8d3d3e0cb1968d94d33931f458dbf5f55ddcf17863a37b4cea5e2a6`;
both exact original-versus-candidate value and metadata-type surfaces pass.
The current decompiler, compactor, and validator SHA-256 values are
`804f76469f2b1d896f5524e3446fd83c2d1c943a3caf4c1bfac25064cc9565cb`,
`025af008848fa3082d061728d68afde8da2766cff87081b26581407c25748f50`,
and `c8d3f74f877b333bcac4df71160bb99cd3ab53ec75ddf043fc130c08edc83b0b`.
An independent source-first replay passes all 51 groups and leaves its fresh
Java temp root empty. The self-excluding 66-entry evidence manifest at
`/tmp/datomic-protocol-preintern-repair-v3.ZnQExT5i/evidence` has SHA-256
`448d3937fbb1ea8f36471eae00c1ec1ed233b80c57b16121fb7661421030128e`
and independently reverifies. Its 997-entry candidate-input manifest has
SHA-256
`d1c52dbc5981d06da3b1c2988a7c28b41bfadcbd2370e1caeabbb275e1d629db`;
all entries verify and no proprietary Peer/Transactor/core2 archive or exact
licensed hash is present. This closes the focused Peer Client marker only;
whole-corpus validation and Peer promotion remain open. The historical R5
wrapper stops at `runtime-exact-surface-validation` with comparator status 1,
so later source/side-ledger and final positive seals did not run. Its exact
surface result ledger has SHA-256
`2c70f6e323aadf1b3a0f8bd94f2bf617ea8bb25d88a1555950f17e46328eead8`.
The self-excluding 5,572-file negative manifest at
`/tmp/datomic-goal2-peer-r5-evidence-20260828` has SHA-256
`1f39aaf03a0a28d688b976c7061cd35ab39d3171afb4d66b1943970ea8b520b9`;
its independent verification log has SHA-256
`9d10a030a3c340d9de3d25681f0ab03971bc489954e4545a5df7ca198c232b47`
and proves 5,572/5,572 entries with zero non-OK rows. R5 therefore promotes
the two generic semantic repairs to the surface boundary, but not the Peer
tree itself.

The smallest generic repair for the six R5 failures is now frozen separately.
Only the metadata operand of exact-arity internal metadata-aware bind,
dynamic-bind, and ordered-set markers is traversed by the new guard, and only
an exact three-form `.withMeta` whose value is a `list` call and whose metadata
keyset is `#{:line}`, `#{:column}`, or `#{:line :column}` is protected from
the existing location-scaffolding erasure rule. Executable/value-lane
lookalikes, wrong-arity markers, foreign heads, and unrelated metadata retain
their prior behavior. Source-first validation passes 50/50 with an empty Java
temp root, and exact original-free candidate surfaces pass 11/11: all six R5
failures plus five nearby controls. The current compactor and validator have
SHA-256 values
`8d506fd83aaddc6d746fc2a69a6346a22c3a62f7bc3efdfd2ef507df799463ab`
and `1062df5a3836713b041d815941703633eebec2eb185a3dd0e7dcc4bd61f6b756`.
The candidate classpath scan finds no original Peer, Transactor, core2, Nano,
JKS, or keystore input. The sealed evidence root is
`/tmp/datomic-nested-list-metadata-repair-v1.7NC7GYcO/evidence`; its seal and
self-excluding evidence-manifest SHA-256 values are
`12327f05a2c9f2e546b6fbaf7ef1fb92287ee88745db1d83d3b5a39e7ec04197`
and `a5afa9860823d8fbfb24a3337c505948b120b917ef1737cb1a03d1909756abe7`.
A wholly fresh R6 pair at `/tmp/datomic-goal2-peer-r6-a-20260828` and
`/tmp/datomic-goal2-peer-r6-b-20260828` is now frozen as verified negative
evidence, not a Peer promotion. Both recover 142/142 namespaces, their source
manifests are byte-identical at
`bd2adfc6cca80b0a3deb7391ca9f6c46068e453135d8cf9c50f00a19463957f5`,
both retained output manifests independently verify, and all 142 source-only
loads, focused behaviors, and exact typed Var value surfaces pass. The exact
surface ledger has SHA-256
`1fa8c7ba0e5f1070ac1143e469bd0e3e2412358c0ade4ae3b187a15652d46282`.
R6 then fails closed in the side-ledger phase, before source-form comparison
or final positive seals: after the exact path-bound semantic-proxy projection,
the original AOT metadata-type set contains `PersistentList`, while the
source-loaded shipped dependency contains `PersistentVector$ChunkedSeq`.
Their compiler-option value surfaces are nevertheless byte-identical at
`c31d9f5e4a88589ba0d75fbf59bf348a961ea749ec3b57900f08d524dd657ef5`.
This reaches the already identified shipped-source source-evaluation-versus-
AOT provenance boundary and is not permission to normalize collection runtime
types; the bundled dependency cohort must be compared AOT-to-AOT. Candidate
isolation is clean at 536 entries with exactly one sanitized Nano derivative
and no original/proprietary archive, AOT, nested archive, or keystore payload.
All 6,556 sealed inputs and both 144-entry output manifests independently
reverify. The self-excluding 5,537-file negative-evidence manifest at
`/tmp/datomic-goal2-peer-r6-evidence-20260828` has SHA-256
`039bce245897ac7af61c02d35a8df20970442781372502f76a76b454ed99c328`;
its verification log has SHA-256
`68e885b63190d7c90caf143c6a8ecd3fed9d2657318dc32e1d0049c0975a3c85`
and contains zero non-OK rows.

The exact-source AOT comparator remains a separate Stage 1 blocker. Four-build
evidence now supports ordered typed-construction pairing, the exact `alts!`
LVT case, exactly 76 one-node/one-alias auto-terminal LVT rows across 42
invariant shapes, and exactly seven entry-boundary `p1__..._SHARP_` rows across
six invariant shapes. The next `vec__` boundary is also now narrowly proved,
not broadly normalized. In the exact
`clojure/core/async$do_alt.invokeStatic(Object,Object)` method, candidate A/B
use `vec__6929`, the licensed oracle uses `vec__6029`, and a third dependency
context uses `vec__4615`; each build contains exactly one matching row and the
same ID-erased source/owner/header/method/slot/range/control-region/physical-
slot/def-use/frame/alias witness. The witness string has SHA-256
`70a41200b61f380b691adb79b22c42fc37d79ef3adfa7c7f74e2280fbfa51098`.
The predicate accepts only that one hash-sealed row and leaves the other 12
`vec__` rows untouched; 19 independent near-miss dimensions plus short, long,
and extra-row seals reject scope expansion. The comparator now also has an
independent ordered three-relation driver. Candidate A/B, A/original, and
B/original execute sequentially with isolated evidence buffers; an expected
semantic failure records and discards only that relation's partial mappings,
later relations still run, and any failed or missing ordered row fails the
process. Pass/fail/pass, all-pass, first-fail, missing-ledger, and missing-input
process controls pass. The comparator source and report have SHA-256 values
`af6c7482f839bc60e7874cb5d53b7e031d3fbd40537ac2612e8235964ae1026e`
and
`9234626640eed6441dd6a5e1cbce6d0a82263d0dedea2ba9180359b2bedbd87b`.
Two pinned Corretto 11 compilations with explicit `-g:none -proc:none` emit
byte-identical 63-class trees whose manifest and top-class SHA-256 values are
`84c328ce4a425c6c7e30da5ff9e2c43e3012244b1d3ec30ff4dc3b42da374768`
and `ea0c9d8e690d5fc9879bd5e73ed7ca114100e805f3aa13ce57f38a382a83ae1c`;
all 112 adversarial self-tests reproduce byte-identically at
`79b3da66faf71f2f4f9d832c1baa1c384cfd3a313d3c3f288fd28f43b5f01a86`.
The compile-command discrepancy found by independent replay is closed:
`-g:none` is decisive because this pinned javac deduplicates equivalent lambda
bodies when debug data is disabled; no hidden lambda option is involved. The
updated self-excluding relation-driver evidence manifest at
`/tmp/datomic-aot-relation-driver.CQQfQQ` has SHA-256
`19260f44756f879feedd409b7fafd228d69c4e206a2343f162e1b53144f2189a`.
The frozen evidence roots are `/tmp/datomic-vec-local-inventory.MjoqTx` and
`/tmp/datomic-vec-comparator-selftest.fUY396`. This still does not close the
fresh 3,431-class relation. A separate scanner kernel now exists at
`transactor/tools/ScanExactAotBoundary.java`, source SHA-256
`f2207c753849d11add0f4515817f96078ee1dc1b51b7b0d8ddbb42f599ef8879`.
It binds canonical inputs and pre/post hashes, inventories candidate trees and
every runtime/archive/nested-archive entry, checks path/internal-name identity,
walks typed ASM reference callbacks, rejects unknown attributes, aliases,
special files, forbidden content/names, keystores, and unresolved references,
and writes a complete discovered/recorded ledger. Two clean Corretto 11
compilations produced byte-identical 26-class trees whose manifest SHA-256 is
`9cdc34838d1bdbe33787b3d7f6481e1668c09ba1c43780309d6ee9a1e3a5cf2b`;
all 14 focused scanner tests pass with ledger SHA-256
`1ced64dfca2e741259461d9221d54d3945cc43653489774a3db900e697e837bb`.
The frozen scanner evidence at
`/tmp/datomic-exact-aot-boundary-scanner.hoxMZu` has self-excluding manifest
SHA-256
`46a7479bc876a835743bf993607f25a1725b2f679e0be7ac6c653b9477cad993`;
all 66 entries independently reverify, and an independent clean compile and
self-test reproduce the class tree and 14-row ledger exactly.

That implementation is not yet a complete acceptance scanner. Independent
source review found six fail-closed gaps before production use: ownership
input is retained only as per-namespace cardinality, so a same-cardinality
unknown candidate class is not yet rejected against the exact expected entry
set; the tracked per-runtime-element class/resource/byte/error/hit counts are
not emitted as a reconciled per-element ledger; the unconditional forbidden
Nano filename rule would also reject the required hash-bound sanitized Nano
derivative; typed member references are recorded and declared members are
collected, but resolution currently proves only target-class presence; and the
single shared runtime lane cannot separately inventory the required licensed
oracle inputs without either contaminating candidate resolution or rejecting
those originals. In addition, the driver creates its output directory before
canonical candidate/runtime origin validation; a previously absent output
nested beneath an input can therefore mutate that input before its pre-scan
seal. Exact membership, per-element reconciliation, a role-and-
hash-bound sanitized-derivative exception, member-resolution or explicit
verifier/comparator handoff anchors, and disjoint candidate/oracle ledgers all
require adversarial controls and a new freeze; output/input non-overlap must be
proved read-only before the first output write and covered by nested-output
negatives. The completed verifier also makes two scanner-to-verifier handoff
obligations explicit: mapping-derived identity-shadow/nonidentity-alias
dispositions must be scanned against the same exact class map, and the
Transactor-at-position-zero plus ordered oracle-fallback collision semantics
must be inventoried rather than inferred from cardinality. Both need focused
controls in the scanner freeze. The earlier read-only audit also confirms
that `ScanCandidateClasspath.java` remains a sound dependency-isolation kernel
and should remain unchanged, but it cannot replace this broader scanner.
A separate JVM verifier/maxima gate is now implemented and independently
reproduced at `transactor/tools/VerifyExactAotRuntime.java`, source SHA-256
`4b4f1786e47e123462315c8eb04f2f0f4cba7e10f05621a75362b5811063ef81`.
Normal mode hard-requires the exact ordered 532-element candidate and
533-element oracle-fallback ledgers, with the licensed Transactor separately
owned at oracle URL position zero. Its five-identity policy forbids Peer from
both verifier lanes, forbids Transactor/core2/original Nano from candidate,
requires core2/original Nano from exact oracle roles, and requires only the
hash/role-bound sanitized Nano on candidate. Platform-parented candidate
loaders are child-first only for exact owned names, block mapped nonidentity
original aliases before URL lookup, and never receive oracle URLs. The oracle
loader receives the Transactor first and the ordered original fallback.
All three relations require exact mapped method identity, access, code
presence, max stack, max locals, and every expanded frame value; owned
shadows, blocked aliases, and oracle first-wins collisions are independently
ledgered and actively resolved without initialization. Output/input overlap is
rejected before output creation and both success and post-creation failure
paths seal evidence. Two clean pinned Corretto builds produce the same 29
classfiles, and 28/28 adversarial controls pass under `-Xverify:all`. An
independent repository-source compile reproduces every frozen class hash and
all 28 controls. The self-excluding manifest at
`/tmp/datomic-exact-aot-runtime-verifier.gS5EmH` has SHA-256
`0ee3030d9562140bbe133bc1604612063eceb06b4a9f0a798f0852e04604887f`
and independently reverifies. This freezes only the standalone verifier:
production cohort and wrapper integration are `NOT_RUN`, so Stage 1 is not
claimed. The scanner repairs, wrapper integration, and a sealed whole-cohort
result remain absent. `validate-exact-source-aot.sh` still pins an obsolete
comparator hash and must not be repinned by itself.
Independent wrapper review confirms that repinning the three Java tools will
not by itself make that script promotable. The current compiler classpath
exposes all 247 recovered source files while its input ledger binds only the 87
exact-cohort sources; the final wrapper must either stage a closed source root
or seal the complete source/resource membership and contents. It must also bind
the complete pinned JDK image rather than only `java`/`javac`, reject injected
`JAVA_TOOL_OPTIONS`, `_JAVA_OPTIONS`, `JDK_JAVA_OPTIONS`, agents, and ambient
classpath state, use fixed empty work/home/temp roots, and prove candidate,
sanitized, licensed, runtime, and output roots mutually non-overlapping before
the first write. Generated candidate trees require pre/post seals around the
scanner, comparator, and verifier; every failed as well as successful run needs
a complete manifest and per-relation status. The historical sed class-name
skeleton must become diagnostic rather than an independent semantic gate, and
candidate A/B byte determinism must be enforced rather than merely reported.
These are same-run acceptance requirements carried forward from the frozen
adversarial audit, not optional later hardening.
The current read-only integration audit further fixes the concrete wrapper
shape. The compile root should contain exactly the 87 report-bound source
entries (85 bundled plus `datomic.query.support` and `datomic.specs`, including
the single `clojure/tools/cli.cljc`) rather than expose the other 160 files.
Retained build logs declare no `datomic.*` dependency from that cohort, but a
fresh closed-root run must still prove the assumption. The wrapper must
generate the ordered runtime inputs before invoking any Java gate: candidate
preserves distribution order while omitting core2 and replacing original Nano
in place with the sanitized derivative (532 rows); oracle fallback is all 533
original `lib/*` rows; Transactor remains a separately owned oracle root and
Peer remains inventory-only. No repository generator currently emits the two
five-column ledgers and ten-column policy. Current-path diagnostic ledger
SHA-256 values are
`d511232ca9caf72a4f159da4d6defc2f1636788d34da55cc78d9e1d4c5b441ff`
and `07dfbd5017a907e13112aa0f70027517054bb0b6bb3dc0786581bc3084b34b01`,
with diagnostic policy SHA-256
`be9003a92b5634be340cf1d9489be68f38441ac2968143857a7ceea55040964f`;
these are path-bound observations, not retained acceptance inputs. Comparator
must run before scanner and verifier because its exact 3-by-3,431 class map is
an input to both. The clean environment must additionally reject
`JDK_JAVAC_OPTIONS` and `CLASSPATH`; the full Corretto image presently contains
465 files and 90 directories and needs a reproducible relative tree seal.
The known 12-namespace/219-class candidate A/B byte differences remain an
acceptance failure even when later diagnostic gates continue. A post-preflight
EXIT evidence path, bounded TERM-to-KILL cleanup, per-gate candidate/input
seals, and explicit PASS/FAIL/NOT_RUN rows are required before this wrapper can
be promoted.
The full merged 247 Transactor plus 25 Peer-core2 structural discovery/load
gate has now rerun from a fresh root with the sanitized Nano and generated
resources proved at the runtime boundary: 272/272 loads pass. The exhaustive
surface half and normalized exact-source AOT promotion boundary remain open;
they are distinct from the now-proven PostgreSQL main-path runtime slice.

**Next direct action:** Close the deferred Stage 1 exact-source/surface boundary
and classify all 117 Stage 2 overlaps. Then exercise the first still-open
concrete Stage 3--7 boundary: injected-startup-failure cleanup, exact SQL
CAS/root rejection, transport interruption, or active/standby takeover. The
existing Stage 2/3 Peer harnesses remain licensed-Transactor oracle fixtures and
must not be relabeled as the recovered-pair gate. Do not resume an
unconstrained 189-residual loop.

## Goal completion

Goal 2 is complete only when the recovered Peer and recovered Transactor can be
studied and exercised together against PostgreSQL through the authoritative
write, log, index, transport, and HA paths, with the remaining ranked
components recovered or explicitly bounded as described above. A successful
Transactor boot, a single transaction, or recovery of only the 45 unique
namespaces is not completion.
