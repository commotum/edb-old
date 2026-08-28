# Stage 1 exact-source AOT adversarial audit

Created: 2026-08-27 (America/Los_Angeles)  
Updated: 2026-08-28 (America/Los_Angeles)

## Scope and result

This audit challenges the promotion boundary for the 87 exact-source
namespaces and their 3,431-class AOT closure. It reviewed comparator source
SHA-256
`5c5ea8485fcb64676bd41d2bda4c9af6517844993a96337bd4be09917b1bf531`
and wrapper SHA-256
`a36c79ed8eba995deaa4ec7dddfff96b3af7ed3b1212cacdc5399d94763cd367`.
Line references from the review apply to that frozen snapshot and may move as
the implementation is repaired.

The audited comparator already exits nonzero on the retained v2 corpus at the
`clojure.core.async` macro-gensym member boundary. This audit found additional
acceptance and evidence gaps that would remain even if that immediate failure
were removed. The snapshot is therefore diagnostic only: it does **not** prove
normalized AOT equivalence and must not be promoted.

## Follow-up audit of the moving redesign

A later read-only audit froze an intermediate redesign at SHA-256
`9f7d221b08b99f0c038c3f6e2da5ca499cf5cc3da6b40f07936f063cbdf88940`.
This is an audit snapshot, not the current or promoted comparator. The wrapper
remained SHA-256
`a36c79ed8eba995deaa4ec7dddfff96b3af7ed3b1212cacdc5399d94763cd367`
and intentionally still pinned the original comparator hash, so the repository
gate refused the moving implementation before compilation. The frozen redesign
passed the original 16 self-tests, but a manual retained-v2 comparison still
failed candidate A versus candidate B in `clojure/core/memoize$fifo` on a
semantic `LDC` difference, `clojure.lang.AFn` versus `clojure.lang.IFn`. That
is an unresolved model/output difference, not a successful gate.

Neither reviewed comparator body is retained as a content-addressed repository
artifact: the later body exists only in a temporary audit directory, and the
earlier body has already been replaced at the moving canonical path. Their
hashes identify observations, not a repository-reconstructible gate. The next
reviewed freeze must retain the exact comparator, wrapper, commands, and
failure outputs together before it can serve as promotion evidence.

An independent bytecode trace locates that failure at the exact shipped source
set literal in `src-clj/clojure/core/memoize.clj` lines 335--339:
`#{clojure.lang.IFn clojure.lang.AFn java.lang.Runnable
java.util.concurrent.Callable}`. Candidate A's static initializer supplies the
class array to `PersistentHashSet.create` as Runnable, AFn, IFn, Callable;
candidate B swaps AFn and IFn, and the corresponding Symbol set swaps them as
well. The original Transactor uses a third order: Runnable, IFn, Callable,
AFn. This supports, at most, a narrowly source-bound unordered set-literal
construction rule. It does not justify normalizing arbitrary arrays or `LDC`
sequences, and any accepted rule must state the remaining observation boundary
for iteration, serialization, and reflection.

The redesign had started to bind paths to internal names, reject extra and
symlinked class files, retain declaration order, model expanded frames and
maxima, type field handles and array operands, and reject volatile or foreign
capture fields. Those partial repairs do not close the findings below. The
follow-up found these additional or still-live defects:

- member candidates are grouped corpus-wide and paired lexically without
  enforcing the already selected owner edge or descriptor edge; the solver
  must search owner-qualified alternatives and retain the least *valid* full
  witness rather than the first coarse zip;
- constructor and any method containing a collapsed construction site lose
  typed member-use edges, while capture/delegate normalization can discard
  frame nodes and the original constructor body;
- a static caller can still treat `ALOAD 0` as non-null `this`; the quotient
  must receive the caller method and reject that case;
- generated-member eligibility remains partly spelling-based instead of bound
  to compiler/source evidence, and no complete member-witness table is emitted;
- string serialization uses lossy UTF-8 replacement for unpaired UTF-16 code
  units, and strict whole-classfile consumption is not yet proved;
- array descriptors inside expanded frames are routed as internal names rather
  than descriptors;
- the class solver still rejects automorphisms while the member mapping chooses
  one unvalidated coarse automorphism, contradicting the required
  existence-of-a-complete-valid-isomorphism policy; and
- the claimed relation must remain explicitly bounded: private generated class
  and member identities may still be observable through reflection, stack
  traces, serialization, or resource lookup even when the modeled bytecode
  graph has no inbound edge.

Required focused controls now also include owner-reversing same-skeleton
members; a 2-by-2 member case whose lexical pairing fails and crossed pairing
passes; authored `state_123`/`foo__123` negatives; field handles through `LDC`,
`invokedynamic`, and nested `ConstantDynamic`; distinct unpaired-surrogate
constants; trailing classfile bytes; malformed frames in capture and delegate
constructors; static nullable receivers; and descriptor arrays in frames.

### Later implementation checkpoint (not reviewed or pinned)

A still later moving implementation was sampled at comparator SHA-256
`ec462eda3d3951a60fec455136e6a905eba8045b2b2ae4f42e09bff1d1f4914f`;
the wrapper remained the old-pinned
`a36c79ed8eba995deaa4ec7dddfff96b3af7ed3b1212cacdc5399d94763cd367`.
It still had only the original 16/16 self-tests, not the adversarial controls
specified here. On the retained corpus it completed candidate A versus B,
including a narrowly source-bound treatment of the `memoize.clj` set literal,
then failed candidate A versus the original at
`clojure.core.async$go`: typed macro-gensym `Symbol.intern` constants such as
`c__6979__auto__` versus `c__6079__auto__` remain outside the proved ID graph.

This WIP contains implementations for strict candidate path/internal-name and
entry checks, trailing-byte rejection, declaration-order preservation outside
active capture blocks, nonvolatile own-field/real-`this` capture constraints,
target-field-bound constructor parameter descriptors, member-witness
backtracking, and expanded-frame recording. Those changes are not review-
closed. Focused negative fixtures, persisted member witnesses, structural
Symbol mapping, typed metadata/signature coverage, complete verifier treatment,
full archive/candidate scans, source/class/JDK/environment sealing, wrapper
repinning, and accurate final summaries remain open. The current commands and
failure output exist only at `/tmp/compare-member-existing-17`; they are not
content-addressed promotion evidence.

After adding typed constraints for those gensym Symbols, the next moving-corpus
failure is narrower but still unresolved: five `core.async`
`$state_machine...$fn` classes use different `AtomicReferenceArray` state-slot
indices between candidate and original (for example, an IOC `aget-object`
constant slot 10 versus 20), while candidate A and B agree. Numeric `Long`/LDC
erasure is forbidden. Any accepted quotient must construct a bijective typed
state-slot graph tied to the already proved state-machine owner, member,
capture, control-flow, and every array read/write edge; persist its witness; and
reject cross-use, conflict, unmatched-slot, and ordinary-long mutations.

The current moving checkpoint, comparator SHA-256
`af195687a4392b334a23914159075e225edc8ed0798187c31f72a98178f3c131`,
is still unreviewed and unpinned. Its typed state-slot recognizer gets through
the first `clojure.core.async/filter<` root-`invoke` setter after proving the
`AtomicReferenceArray` allocation, then fails closed at the second setter
(model instruction 14): the stored value is loaded from `GETSTATIC const__3`,
which is outside the current value-block proof. `GETSTATIC` cannot be admitted
generically; an acceptable rule must bind the exact owner, field name,
descriptor, constant/value role, and mapped slot graph, with ordinary-static-
field and conflicting-slot negative controls. The checkpoint still has only
the original 16/16 self-tests. The integrated all-archive/candidate scanner
required below has not been implemented or retained; only the earlier
noncanonical original-outside-cohort exploratory scan exists.

A subsequent quiescent WIP checkpoint has comparator SHA-256
`10c29f0ebd13a285f9aae2985aad7ac7b77d705f227557a6876b337306f14aaa`.
Its expanded focused suite is 21/21 PASS at
`/tmp/datomic-slot-selftest4.FLTReZ/self-test.tsv` (SHA-256
`3ad076db2ef528cf2cca2ebf30b1f777152c6cab6340e269921b899698920ddf`).
The retained corpus run at `/tmp/datomic-slot-corpus13.MauBiW` recognizes all
typed `AtomicReferenceArray` `aget`/`aset` shapes, then fails three
`core.async` class skeletons because ordinary JVM local indexes are permuted
alongside the proved state slots (for example ALOAD/ASTORE 10 and 11 plus
downstream aliases exchange roles). Its `failures.tsv` and `comparisons.tsv`
SHA-256 values are
`1b66f06c668f3a89bb2298c72bc6491367af8401d8fe3796bc2f16e715a6a61b`
and `e5c49f25f0caff15ed55beee12083690308aab5e5b061e43d95462265694c519`.
Raw `VAR` indexes may not be erased generically. Any quotient must build a
per-method typed def-use/live-range graph over every VAR, IINC, frame, and
local-variable-table occurrence; keep `this` and parameter slots exact,
including category-two width; anchor control-flow, exception-handler, and
state-slot edges; and reject conflicting, category-incompatible, frame-only,
or ambiguous reuse. This checkpoint remains unpinned and is not an equivalence
result; the integrated boundary scanner is still absent.

## Required repairs and negative controls

1. **Bind file paths to class identities.** `readCandidate` modeled the
   `ClassReader` internal name without requiring the classfile's relative path
   and namespace container to equal `<internal-name>.class`. Swapping two
   generated classfile payloads under unchanged expected paths could leave the
   model unchanged while JVM loading fails. Require regular non-symlink files,
   exact container/path/internal-name agreement, and no extra runtime files.
   A swapped-payload fixture must fail.

2. **Close the verifier boundary.** Stack-map frames were skipped and
   `max_stack`/`max_locals` were recorded but not compared. Invalid candidate
   bytes could therefore compare equal and later fail verification. Record and
   normalize frames plus maxima, or define and seal a separate complete JVM
   verification quotient. Mutated max-stack, max-locals, frame-type, and frame-
   offset fixtures must fail.

3. **Preserve observable declaration order.** The snapshot sorted interfaces,
   declared exceptions, fields, and methods globally. Those orders are not part
   of the proved compiler-ID/capture transformation and some are reflectively
   observable. Preserve their original order; canonicalize only a narrowly
   proved captured-field subset. Ordinary interface, exception, field, and
   method reorder fixtures must fail.

4. **Constrain reordered capture effects.** Capture-site blocks allowed field
   reads/clears without resolving volatility and assumed local zero was a
   non-null `this` receiver without rejecting static callers. Reordering
   volatile access or nullable static receivers can change behavior. Resolve
   exact own declared nonvolatile fields and prove a nonstatic receiver (or an
   equivalent non-null fact). Volatile and static-null-receiver fixtures must
   fail.

5. **Keep auxiliary-constructor ABI exact.** Treating all reference types as
   interchangeable and replacing a constructor descriptor with a field-based
   token can erase a real `(String,Object)` versus `(Object,String)` ABI change.
   Retain each normalized declared parameter descriptor per mapped field. An
   unequal-reference-parameter reorder fixture with no callers must fail.

6. **Use structured typed remapping everywhere.** Generic signatures,
   annotation/type-annotation payloads, inner/nest/outer metadata, TYPE array
   operands, MULTIANEWARRAY descriptors, bootstrap handles and arguments, and
   related callbacks must route exact owned class/member nodes structurally.
   Do not apply regex replacement to serialized event strings. Fixtures must
   exercise field handles, nested annotation `Type` values, code type
   annotations, generic signatures, array TYPE operands, and multi-arrays.

7. **Accept graph isomorphism, not guessed uniqueness.** Multiple private
   automorphisms are valid when every complete labeled-graph and boundary
   constraint holds. Search complete witnesses, validate the full typed model
   at each leaf, and retain the deterministic lexicographically least valid
   witness. Two indistinguishable closures must pass with non-uniqueness
   recorded; a case whose first coarse witness fails but second succeeds must
   pass; zero valid witnesses must fail.

8. **Map only proved generated member nodes.** Macro-gensym mapping must use
   owner-qualified field/method nodes and constructor assignment plus every
   typed use edge. A broad `__N`, `state_N`, or `G__N` name regex is not proof
   and conflates semantic names with generated identities. Preserve
   `const__N` and unrelated members exactly, anchor mapped handles and external
   uses, and persist `member-mappings.tsv`. Cover independent same suffixes in
   unrelated owners, state/G__ positives, const/unrelated negatives, unmatched
   fields, and field-handle updates.

9. **Scan the complete runtime boundary.** The snapshot checked only original
   Transactor classes outside the cohort. The final scan must cover the Peer,
   core2, all 532 candidate and 533 oracle dependency identities, candidate
   class/source/resource directories, every modeled classfile callback and
   constant, and every decompressed non-class/nested resource. Unreadable,
   duplicate, unsupported, corrupt, or unaccounted archive entries must fail.
   Emit per-archive hash, class/resource/byte/error/hit counts and reconcile
   expected versus scanned totals.

10. **Use boundary references as anchors.** An outside typed reference need not
    cause blanket rejection when the candidate preserves the referenced class
    or member identity exactly. Feed such hits into the graph solver as
    identity constraints; fail when the anchored identity cannot be retained.

11. **Close the source classpath.** The wrapper put all 247 `src-clj` files on
    the compiler classpath while sealing only the 87 cohort files. Stage a
    closed exact-source root or bind the entire source-root membership,
    contents, resources, and symlink absence. A shadow macro/source outside the
    cohort must be rejected before compilation.

12. **Remove tool/output TOCTOU.** Seal inputs before comparator compilation or
    self-test; bind the complete JDK image, sanitize JVM-option environment
    variables, and use fixed empty working/home/temp directories. Seal both
    candidate class trees immediately before and after comparison. Fixtures
    must catch source/comparator mutation, non-cohort shadowing, injected Java
    agents, candidate-class mutation, and a changed JDK module with an unchanged
    launcher binary.

13. **Demote the broad sed name skeleton.** The three-form regex skeleton is a
    historical diagnostic and currently fails 3/87 in retained v2. Once the
    typed graph relation is authoritative, it must not remain an independent
    semantic gate or a source of generic numeric normalization.

14. **Make long runs resumable and failure evidence complete.** Use TERM plus a
    bounded KILL fallback, verified per-namespace checkpoints or a transactional
    run root, an EXIT failure ledger, explicit overall status, and a sealed
    output manifest on failure as well as success. Record the three comparisons
    independently so a later failure cannot erase prior valid rows or leave
    unqualified partial mappings.

15. **Require candidate/licensed path disjointness.** Candidate output and the
    sanitized Nano derivative must be outside, and not an ancestor of, the
    licensed distribution/JAR/library roots. Merely keeping them outside the
    repository is insufficient.

## Corroborating boundary scan

An exploratory scan found no references to the 2,074 generated cohort internal
names in the **licensed original** Peer JAR or any of the 533 shipped
`lib/*.jar` identities. It did not scan the recovered Peer. The Peer input was
`peer-1.0.7277.jar`, 6,801,387 bytes, SHA-256
`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`,
with 5,635 ZIP entries, 5,527 non-directory files, and 5,517 class entries.
Temporary evidence includes the 2,074-line internal-name target at
`/tmp/exact-aot-generated-owned.tsv` (SHA-256
`0fbc5e82da6dba6546c0aa929db49493f2069238a620fe2fd8d8401ad46307c6`),
59,497 lines of Peer `jdeps` output at
`/tmp/datomic-peer-jdeps-verbose.txt` (SHA-256
`39bb75a2e03d6c8ee03ef31a3c7f91182cd1198a1385def813d567304f7087de`),
and empty hit files for that run. It checked slash-form and dotted names in
decompressed class bytes; `jdeps` also found zero hits, although one explicit-
module CBOR JAR could not resolve in an isolated `jdeps` invocation and was
checked separately as raw class bytes.

The exact commands, contemporaneous archive manifest, and entry-count ledger
were not retained. Therefore neither the empty hit files nor these temporary
paths are canonical or independently resumable evidence. These zero-hit
results are corroborating only and do not replace the fail-closed, per-archive,
all-entry scan required above.

## Promotion boundary

Promotion requires all focused controls, all three complete comparisons
(candidate A/B, candidate A/original, candidate B/original), the sealed verifier
and runtime-boundary scans, and pre/post input/output seals to pass from the same
frozen run. A comparator that merely gets past the frozen snapshot's
`core.async` failure, or the moving snapshot's `memoize$fifo` set-literal
ordering failure, does not close Stage 1.
