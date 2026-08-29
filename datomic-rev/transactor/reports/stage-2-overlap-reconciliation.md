# Stage 2 Peer/Transactor overlap reconciliation

## Current checkpoint

The current ledger fully resolves 13 of the 117 shared namespaces. Of the 104
incomplete rows, nine are `BOUNDED_PARTIAL` and 95 are `OPEN`. This is a
semantic-evidence count, not a count of files that merely load or of original
artifacts whose reachable bytecode happens to agree.

Goal 3 uses four classifications:

- `RESOLVED_EQUIVALENT`: no unexplained difference remains in the claimed
  domain.
- `RESOLVED_DIVERGENT`: a real artifact difference is understood, bounded, and
  intentionally preserved.
- `BOUNDED_PARTIAL`: supported behavior is proved, but identified dormant
  behavior or a required recovery-evidence rung remains uncovered.
- `OPEN`: an unexplained executable difference remains.

Only the two `RESOLVED_*` states count toward completion. In particular,
`BOUNDED_PARTIAL` is useful progress but never a resolved overlap.

The normal evidence ladder is: candidate ownership/isolation; namespace, Var,
arity, protocol, class-role, method, and field surface; conservative local
method structure; JVM load/verification; and focused critical behavior.
Exact-AOT is an escalation tool only for a specific unexplained executable
difference, ABI/surface mismatch, differential behavior failure, or explicit
user request. Compiler noise is normalized only under guarded predicates;
calls, constants, arguments, branches, effects, exceptions, locking, and
transaction boundaries remain semantic.

A fresh whole-cohort source-form run at
`/tmp/datomic-stage2-overlap-current-full-v1` compares the preserved Peer tree
with the promoted Transactor tree. It contains exactly 117 rows. The strict
relation accepts `datomic.cache.impl` as an oracle-proved protocol scaffold
combined with binding-aware compiler-ID alpha equivalence; the other 116 rows
remain `semantic-body-or-structure`. That diagnostic failure is expected and
useful: the newly promoted Transactor corpus is a fresh recovery of compiled
namespace initializers, while much of the Peer reference retains authored
forms such as `defn`, `defmulti`, and `defprotocol`. A direct notation mismatch
therefore remains unresolved until bytecode or behavior evidence explains it;
it is not itself evidence of a business-semantic difference.

The full-relation evidence manifest SHA-256 is
`dcd2b099e2fb669430926da2bc294e9658e010b325cf2e4729e92fcadaa32207`.
The relation SHA-256 is
`dac7313c9db45680c41fac8a194e6dfdc58086d01b31d93434ce7b4e1684310c`.
Its exact 117-row namespace index SHA-256 is
`be6f623243ce3c36e82e917ac23cec32befd90a2fdc803cee5882aac04252667`;
the same index is retained as `stage-2-shared-namespaces.tsv`.
The bridged runtime surfaces come from the already sealed Stage 1 structural
run whose evidence-manifest SHA-256 is
`4921abcfb0926790780c21889e71ab05889d0347496d3272fafd5464f5d073a2`.

## Parked global classifier diagnostic

The one-pass classifier at
`/tmp/datomic-stage2-global-overlap-classifier-v8` replaces namespace-at-a-time
static ceremony. It records source ownership, exact namespace class roles,
method ABI, normalized method bodies, field deltas, provisional recurring-
family signals, and machine-recorded runtime coverage for all 117 rows. Its
ownership rule uses the artifacts' complete namespace-initializer inventory:
real child namespaces are excluded from a parent row, while generated helper
classes without their own initializer remain with their actual namespace. This
corrects an earlier prefix-contamination diagnostic involving
`datomic.tools/*` and `datomic.cast2slf4j/*` before any promotion used it.

The corrected sweep finds 116 exact class-role rows and one different row; 102
exact method-ABI rows and 15 different rows; 43 rows with exact fields, 69 with
only Transactor generated-static-Var fields, and five with other field deltas.
The sweep snapshot's conservative triage is 13 already resolved, 48 recurring-
family review, 38 semantic investigation, and 18 ABI/field investigation.
Every ABI-exact row still has a normalized whole-body residual, so the sweep
alone promotes nothing. It is parked and must not be rerun or expanded while
the selected HA boundary is unfinished. The evidence manifest verifies at SHA-256
`cd1b8adf665169bd280392173cf1aaf0da464f7db0b4b11a2e801b840e3575d2`;
classifier and summary SHA-256 values are respectively
`ceb96f1a51a257b60c97e4cef3a1cc17b0c76d44d0c5ccd33f3a00af658514d2`
and `2a2466a5a6263926d1b5ff33cc763129b6141841b33f20ea8d88bdd5a50e4c35`.

The sweep is triage beneath the evidence ladder above. It cannot promote a row
merely because the two original artifacts have the same ABI or reachable
method relation; the recovered candidate must satisfy the applicable recovery
evidence as well.

## Bounded original-artifact CFG cohort

The CFG proof at
`/tmp/datomic-stage2-dead-verifier-slots-global-v2-corrected-v8` analyzes all
20,044 methods in the exact 117-namespace corpus. It roots method entry and
every exception handler, follows branches, switches, fallthrough, and
conservative exception edges, and compares reachable instruction streams, CFG
targets, constants, and projected exception tables after generated-ID
normalization. Negative controls reject both a differing block with a reachable
predecessor and a differing handler target; the unreachable `pop`/`athrow`
positive is accepted.

That proof usefully bounds nine rows: `datomic.adopter`,
`datomic.aggregation`, `datomic.codec`, `datomic.combined-cluster`,
`datomic.data`, `datomic.garbage.fressian`, `datomic.memory`,
`datomic.process.events`, and `datomic.validators`. Their class roles, method
ABI, and fields are exact between the original artifacts. Across those rows,
every reachable instruction, CFG edge, and exception projection agrees; the
only bytecode differences occupy 60 unreachable blocks in 50 methods. However,
the proof compares the licensed original Peer JAR with the licensed original
Transactor JAR; it does not compile or compare the recovered candidate methods.
The nine rows are therefore `BOUNDED_PARTIAL`, not resolved. The other 108 rows fail closed;
107 still contain reachable instruction/CFG differences and 57 contain
reachable exception-table differences, with overlaps between those sets.

A claimed supplemental 87-class pairing table was not retained inside the
sealed evidence manifest. It is not used for promotion. Generated-role pairing
therefore remains an additional recovery-evidence gap for any affected row.

The proof manifest verifies at SHA-256
`682ea3b8e4b808a2b8fb878e6b6b09d9f86692e964d837510ebd79f89ee7745e`.
Namespace-summary, promoted-row, and negative-control SHA-256 values are
respectively
`d74416be55af820277378ab14b9598a93d52551843240bfa244f6c0893a8c456`,
`6bea7f537dd6b0c5e71261ae16d6ddb36afa34373cb8ae0260c8b208aabf3db6`,
and `8f42765aa841736f9b8902a77e1fab4529bd21f683dff5dce616688656427710`.
The proof advances nine rows from `OPEN` to `BOUNDED_PARTIAL`; the fully
resolved count remains 13/117.

The first generated-static-Var mapper attempt is deliberately not promoted.
It found 711 candidate fields but mapped zero safely and reported 1,027 target/
use join blockers across 69 namespaces; 48 namespaces had no such delta. With
no accepted mapping, it could not run a meaningful swapped-target negative.
The failed diagnostic remains at `/tmp/datomic-stage2-global-var-fields-dev`,
but its unused helper was not retained in the repository. This family is frozen
until its target/use model is the highest-value remaining blocker.

## First four resolved rows

The durable row details are in
`stage-2-overlap-classification.tsv`. All four original/recovered lanes emit
the same byte-for-byte behavior result:

- original Peer artifact;
- recovered Peer source;
- original Transactor artifact;
- recovered Transactor source.

The recovered lanes contain no Peer, Transactor, or core2 implementation JAR.
The focused gate uses bounded JVMs, rechecks every selected source and runtime
input after execution, and covers cache mutation/counting, dynamic retry binding and
exception dispatch, queued-put MultiFn/protocol behavior, and simple-KV
packing, unpacking, raw-buffer boundaries, retry, put, and delete. Its evidence
root is `/tmp/datomic-stage2-trivial-overlaps-v2`; result SHA-256 is
`50bbd483b95347215e7cd37efb475a731c56a3000f95413cc975b8d877e4c60f`
and evidence-manifest SHA-256 is
`aede25fe1c59a58ec5381b4b3490a26664fcc1e4ff348461d129486f7b62decc`.
The runner SHA-256 is
`a4a4fb63af3f7a80e0a95d85b34c1d0ba874c2257c97048eb5490efb3bed2fb9`
and the probe SHA-256 is
`6e982fc4f923618cf4f59f0a65d23a0c0ec502eeeffee9cfc2404e499968e5cd`.

The independent original-artifact surface and bytecode audits are retained at
`/tmp/datomic-stage2-trivial-overlap-artifact-surfaces-v1` and
`/tmp/datomic-stage2-trivial-overlap-bytecode-v1`. Their verified manifest
SHA-256 values are respectively
`89c45165168e53d8d106d6be451997c712420a93a7779c7f11cbfacef641e898`
and
`5196c915f40de1645ce544cc2ed69b54b168176a5d9160e5d91fe1a0526aa809`.
They bound the residuals to Clojure-version compiler scaffolding, generated
identifiers, unreachable verifier slots, and—only in `datomic.simple-kv`—a
discarded `ByteBuffer.getLong()` result that the Peer boxes before discarding
while the Transactor discards the primitive directly. Both execute the same
state-changing read and exception path.

## Comparator repair

The source-form comparator now composes exact protocol-scaffold normalization
with binding-aware compiler-ID comparison. It accepts protocol method docs in
their emitted trailing position, recognizes the optional `defmethod` function
binder, and compares map values only after exact semantic-key pairing. Its
focused self-test includes collision, cross-key, dispatch-value, malformed-doc,
metadata, and scope negatives. The existing positive fixture still passes and
the altered-tag and altered-body fixtures still fail with status 2. Comparator
SHA-256 at this checkpoint is
`beca31b663fce43b25f1418580c83860f9f1358d4d7f6e862f0b540d4973d05b`.

## Central PostgreSQL cohort checkpoint

The next eight shared namespaces were audited as one bounded cohort:
`datomic.kv-sql`, `datomic.kv-sql-ext`, `datomic.kv-cluster`,
`datomic.cluster`, `datomic.catalog`, `datomic.coordination`, `datomic.log`,
and `datomic.index`. The guarded source-delta diagnostic at
`/tmp/datomic-stage2-central-source-delta-v1` intentionally remains a failing
diagnostic: it accepts 322 exact compiler-transition events and reports 400
residual issues across the eight files. Its verified evidence-manifest SHA-256
is `bf0adf1f2dcec5067a5cca208843fc52157f9a8581761fa3f2cd85a28ded33cd`.
The residual families are compiled `defn`/`def` initialization, protocol and
multimethod scaffolds, generated identifiers, Clojure-version map
destructuring, keyword-lookup optimization, and equivalent
`with-open`/`try`/`finally` layouts. That diagnostic alone resolves no row.

Five already-banked recovered PostgreSQL gates provide direct execution
evidence for the cohort:

- storage CAS: manifest
  `98c39f04a7536e51e218fb4d28a38b080ee15fa8d22bb0a8dadfc1fe46f5d7f4`;
- transaction rejection and ordering: manifest
  `8efea20819c62f167d774d29d7a330f6ef1e018884c50f16a533376d667a3bdd`;
- prepublication crash/acknowledgement cut: manifest
  `6f3a5ab8d6c877d7a9f2f8e2e23c4670b9a2228995dd290f854426a5a20fd0c5`;
- persistent-index publication and restart adoption: manifest
  `c23915e1406f1fac4046643683d93dc9cf06fee187657761e8f96fa1313b4238`;
- active/standby takeover and stale-writer fencing: manifest
  `62859ef9ec8e66c43743434ea35974dde6dfd8362ff23cc6226d86721f2edf88`.

The later post-publication/pre-result acknowledgement cut adds manifest
`d8ac5e5314603c1bca54aa8a773d60a7eec136e29066b4d220d110e1591374bf`.
It freezes the exact Peer, observes durable authoritative-root advancement,
kills the exact Transactor before result delivery, then proves same-Peer
reconnect/retry and fresh-Peer equality with exactly one committed CAS/sentinel
effect. This is shared live-path evidence for the transaction/transport
cohort; it is not namespace-wide proof of dormant functions.

The focused SQL gate at `/tmp/datomic-stage2-sql-overlaps-v8` now resolves
`datomic.kv-sql`, `datomic.kv-sql-ext`, and `datomic.sql`. Four separate
bounded JVMs run the original Peer, recovered Peer, original Transactor, and
recovered Transactor. The recovered lanes compile only the two required
recovered Java dependencies and contain no Peer, Transactor, core2, Nano, or
PostgreSQL implementation archive. Their supported map/JDBC payload is
byte-identical in all four lanes, SHA-256
`2440c471a4251a36cb10d71d7d07e1c67707745ee01543014a1c14f010351392`.
The adapter coverage proves SQLState/retry dispatch, value transformation,
revision CAS, provider/query dispatch, validation failures and close order,
datasource construction/memoization, configuration precedence, and factory
adaptation. Direct `datomic.sql` coverage proves DataSource/factory connect,
fixed-null and dynamic insert/update SQL and binding order, select hit/miss,
delete, multi-command execution, and prepare/execute failure cleanup. The gate
also fully reverifies and hash-binds the five live PostgreSQL evidence roots
above.

The comparison deliberately does not claim universal four-way equivalence.
At the six map-destructuring sites across the three namespaces, original and
recovered Peer preserve the Clojure 1.9 expansion while original and recovered
Transactor preserve the Clojure 1.11 expansion. Unsupported singleton-
sequence inputs therefore differ, while documented map inputs agree. The
within-lineage compiler-domain payloads are byte-identical, with Peer SHA-256
`a98a8c2732b0086d5bbbeae6966ab5d246936ffcc175eb426497bb4875ef6681`
and Transactor SHA-256
`05b68dcd26cbf0cc8812643dd846b02f74c99527b598f98e93269fd293c46d71`.
The gate also records rather than repairs a shared original-product behavior:
the SQL-extension Callable adapter accepts one argument, while
`datomic.sql/connect` invokes it with zero and therefore throws
`ArityException` in all four lanes.

The v8 evidence manifest independently verifies at SHA-256
`46d5027cf9d517a546a02f8a57a16197367a1ada48613b97fed3dd635e6011aa`.
The runner SHA-256 is
`f58fa93f85f8065c8b6a1069d00e9ab194ce2c3b4c23d27caa43dfc0a2c5e799`
and probe SHA-256 is
`79f962bf2196dfe0295d9f71d78f92276384bb15b0f1aa91b19b85e42e808282`.
The sealed Transactor original/recovered surface payloads also match exactly:
`datomic.kv-sql` is
`cae6b70509a920c8584ff6519253b8d67707ecf6891fa6532db48f8de8635a55`,
`datomic.kv-sql-ext` is
`f3b0de9934ff2b22ac23e455b07bfea98653efde3816a2c21bc1d59cec1c90d0`,
and `datomic.sql` is
`2d317e219cdfc279d6eb9215b14e41671c33de93ce5e5908046e8cc5137d709c`.

The downstream gates strongly cover the common PostgreSQL
write/CAS/log/index/HA spine, but they do not establish namespace-wide
equivalence for the other six central-cohort rows. The remaining
open branches include cluster cleanup and failure retries; catalog
rename/delete/undelete and racing conflicts; negative coordination version and
configuration paths; log conversion, excision, segmentation, and broad range
traversal; and index repair, AVET add/drop, fulltext, excision, and corrupt
version paths. Those six rows therefore remain partial.

## Transaction overlap checkpoint

The next runtime-pulled row, `datomic.transaction`, is now resolved by the
four-lane gate at `/tmp/datomic-stage2-transaction-overlap-v6`. Original Peer,
recovered Peer, original Transactor, and recovered Transactor emit one exact
supported-domain payload with SHA-256
`e859ba6f306b827962afc4c6b7900b4f596a7b6eca7490a050ad9db48fdd5eba`.
It covers every message-routing case and exact address, both writer arities and
cache modes, DbId plus asserting/retracting Datum Fressian roundtrips,
read-message success/cache/error behavior, deterministic procargs, event-state
merge/override, the exact loggable-key set, and log completion across absent,
no-timing, read-only timing, apply timing, chunked/nonchunked, repeat-removal,
and logger-enabled/disabled branches. Monitor values and logger effects are
captured without comparing wall-clock values.

The recovered lanes use current repository source, freshly build all 43 Peer
and 46 Transactor Java sources, and freshly stage candidate-owned resources.
They contain none of the Peer, Transactor, core2, or Nano implementation JARs.
The five already-sealed transaction, crash/ack, persistent-index, transport,
and HA PostgreSQL manifests are fully reverified and hash-bound rather than
rerun. Each lane emits exactly two stdout rows and empty stderr, all input trees
and dependencies are rechecked after execution, and the self-verifying evidence
manifest has SHA-256
`7dc62498833b65f915f5a56d4c16bbff67b581193c94b281e4afe0be7cc79230`.
Runner SHA-256 is
`d0980446ce79907a1a9cd213a92e667d18732453c30e13272d48b15d0c8a9bf4`;
probe SHA-256 is
`e40aec0aed57894423c0cbc408781dc7ae3def47f523a7532838b69137a14cbf`.

The comparison preserves rather than erases one Clojure-era compiler edge.
With an invalid singleton sequence stored where the internal contract requires
a map, original/recovered Peer both throw `IllegalArgumentException`, while
original/recovered Transactor both unwrap the map and complete. Their pairwise
payload SHA-256 values are respectively
`f3ef575865f167857c86e2c3062c925b7fde8d262d26743d7cda385c16bfcd47`
and
`fddf312a7a12405fdc68eddec613165c9628a842e14b319388a771b3ad065fbc`.
Both artifacts expose the same 13 Vars and 17 class roles. Fourteen normalized
class bodies agree; the other three are confined to that map-destructuring
expansion, harmless nil placement, one unreachable Datum-reader `pop` versus
`athrow` slot, and namespace-initializer metadata scaffolding. No
Transactor-specific business behavior remains unexplained.

## Cache overlap checkpoint

`datomic.cache` is now resolved by the corrected four-lane gate at
`/tmp/datomic-stage2-cache-overlap-v3`. Its supported payload is byte-identical
across original Peer, recovered Peer, original Transactor, and recovered
Transactor at SHA-256
`17b1982ee501d705c5ec6d518d2f76f80419768652eb97e8b58d0b397afb7a50`.
The probe covers all 11 forwarded Var roots and their arities; plain-map and
`ICachedLookup` hit/miss routing; uncached/getx failures; ordinary and safe
transformers; disabled, cached, and submitted read-ahead; lookup-cache
population, callbacks, falsey cached values, and mutation protocols; every
public cache constructor; same-key in-flight collapse and timing; failed
in-flight cleanup/retry; function/double lookups; repairing lookup, write,
close flags, order, and first-failure behavior; and exact conversion-error
data and cause retention.

The compiler-provenance payload remains intentionally lineage-sensitive.
Original/recovered Peer agree at SHA-256
`5a96ca2b2247d4c31cd9dfda08ab9667afee784df3f4619d88157ce28146f1d9`,
while original/recovered Transactor agree at
`2af487805102fb2967cf2ef87b236597e62418990c3ae99854479ac7f733b9a2`.
The difference is confined to the three Clojure 1.9/1.11 singleton
map-destructuring edges in the two transformers and repairing stack. Supported
maps and keyword option pairs agree. Artifact audit finds 25 Vars, 32 class
roles, and 13/32 normalized class bodies exact; the 19 residuals collapse to
that map expansion, closure capture/GC-clearing order, unreachable verifier
scaffolding, and a redundant void/null sequence.

Both recovered lanes freshly build their Java closures and candidate-owned
resources and contain no Peer, Transactor, core2, or Nano implementation JAR.
All four lanes emit exactly two stdout rows and empty stderr. The corrected
probe derives the disabled and cache-hit short-circuit call counts from the
observed call ledger and requires its complete frequency map; it does not
hard-code those results. The 214-file self-excluding evidence manifest
independently verifies at SHA-256
`aa61b91b9ef75427cf887a674058ca6b4adb23dded576246837e7085f6318385`.
Runner SHA-256 is
`3e1c91a5133540b0a26dd9c530aff5b44318b2e868f43040008cb321f87cea61`;
probe SHA-256 is
`9ed04795a4209bb449b275b1a2cb710b43c703ac3a08e6454ee6ce9dc40ce36d`.
This resolves only `datomic.cache`; `datomic.cache.caffeine` remains its own
open overlap row.

## Promise overlap checkpoint

`datomic.promise` is now resolved by the independently reviewed four-lane gate
at `/tmp/datomic-goal3-stage2-promise-v1`. Original Peer, recovered Peer,
original Transactor, and recovered Transactor emit the same supported-domain
payload at SHA-256
`aa2e38f2a9fa23c2e03c887af969b40a77b52c0a3e3d210805ca0e4ed8439b49`.
The probe covers ordinary and Throwable delivery, `Future`, `IPending`, deref,
timed get, cancellation, metadata, listeners before and after delivery,
rejected executors, listener failure, twelve-way delivery contention,
twenty-listener registration/delivery contention, interruption of a blocked
waiter, and bounded thread/global-handler cleanup.

Original Peer, original Transactor, and recovered Transactor have byte-exact
metadata for all four public Vars. Recovered Peer has the expected authored-
source metadata difference while preserving roots and callable arities. The
two original artifacts expose the same eleven class roles, 51 methods, and 60
fields after generated-ID normalization. Four normalized class bodies are
exact; the seven residuals are bounded to source-versus-AOT locking lowering,
capture ordering, captured-field clearing, nil placement, and dead verifier
slots. No unexplained executable behavior remains in the namespace.

Both recovered lanes freshly compile their complete Java closures, stage
candidate-owned resources, prove the current promise source origin, and exclude
Peer, Transactor, core2, and Nano implementation archives. All four lanes emit
exactly two result rows and empty stderr. The fresh evidence manifest verifies
independently at SHA-256
`0c4b33d520f7286f8105e54c0aaf13384620f2823088610b7d5548e405833ce4`.
Runner SHA-256 is
`22d81537625598097f546b16f2df90d4c7ff21157a9d8484a5b7db31bff7efa8`;
probe SHA-256 is
`6cade8a83b0ec3eb027882c8464832305ea0262416e85614daf8fa7e7b205498`.

## Transaction/transport cohort checkpoint

One bounded static pass over the pinned original Peer and Transactor artifacts
at `/tmp/datomic-stage2-transport-cohort-static-v2` classifies the five-row
transaction/transport cohort without rebuilding either candidate or creating
five behavior gates. Its summary SHA-256 is
`764e1dc988e4df46a08ffde3fd776fb40a13504c58b79589fdb5f301356ed7af`;
runner SHA-256 is
`bf9f231db42e9a8739e68c1bd78f45cb9bb28e9b31548e7f3651cdc14545a90e`.
The run binds both original artifact hashes, the pinned Corretto `javap`, and
all ten recovered source inputs.

Across `datomic.queue`, `datomic.builtins`, `datomic.reconnector2`,
`datomic.connector`, and `datomic.artemis-client`, all 261 normalized class
roles align and all 945 method signatures are exact. There are no Peer-only
fields. The Transactor artifacts add respectively 3, 6, 3, 7, and 21 fields;
every one is a compiler-generated static `clojure.lang.Var` constant. Whole-
body bytecode remains residual and was not normalized into a false exact
result.

The source/body review plus already banked runtime evidence resolves three
rows. `datomic.queue` has source-equivalent queue primitives and `queue-seq`;
its only map-expansion residual is inside `DelayingQueue`, which consumes maps
created by that same type. The live Peer/Transactor runs exercise clear,
offer/put, poll/take, Fressian queue sequencing, and ReferenceQueue cleanup.
`datomic.builtins` has source-equivalent component/retract logic on production
`IDbImpl`/`IDatum` values, while accepted, rejected, concurrent, and both crash
cuts exercise CAS. `datomic.reconnector2` has source-equivalent supported
keyword construction and state-machine bodies; transport, acknowledgement,
and HA runs exercise repeated unavailable reconnect, successful replacement,
same-Peer continuation, and shutdown.

The compiler-domain differences are retained: Transactor protocol docs and
metadata, Clojure 1.9/1.11 sequence-map expansion, locking/`do` lowering,
capture/clearing order, and dead verifier slots. Synthetic malformed singleton
sequences can differ, but they are outside the supported queue payload,
database-datom, and reconnector keyword-pair domains. The detailed ledger at
that checkpoint records all 13 fully resolved rows. The original-artifact CFG
cohort above adds nine bounded partial rows but does not increase that count.

`datomic.connector` and `datomic.artemis-client` remain open. Their live send,
wait, failure, reconnect, and shutdown path is strong, and their class/method
ABI is exact, but administrative requests, temporary queue/stream helpers, and
RPC-server branches are not yet bounded. The static pass is partial evidence,
not a promotion for those two rows.

## Stage 2 closure and residual ledger

The corrected in-flight transaction-during-takeover v8 passes at
`/tmp/datomic-recovered-pair-ha-inflight-v8`. Its 101-entry evidence manifest
verifies at file SHA-256
`5243885f1c0c85dbe2967171856258ad7f7665fd38391bd72444b4c72c1a2887`.
While its descriptor update was blocked, A's immutable append was merely an
unreferenced candidate. A's descriptor CAS later won and published `t=1003`;
B claimed and caught up that referenced lineage rather than adopting an
orphan. The original Future is unavailable, the effect exists exactly once, a
new same-Peer write commits at `t=1005`, a fresh Peer and SQL root agree, stale
A fences, and cleanup completes.

Concurrent accepted submissions now pass both relevant descriptor-CAS
schedules. `/tmp/datomic-recovered-pair-ha-concurrent-v6` records A publishing
all four logical writes before fencing and B catching up the referenced tail
(four adopted, zero resubmitted). Its manifest-file SHA-256 is
`773dd1f4c7e6c78f1a55d4f2742f6b4852008c8f2236da2ce2aa31ea27948d47`.
In `/tmp/datomic-recovered-pair-ha-concurrent-v7`, B first claims the unchanged
baseline while A is frozen; A's append remains unreachable and the Peer
resubmits exactly the four absent intents (zero adopted, four resubmitted). Its
manifest-file SHA-256 is
`e3866a7611b8ac8a6132f8be12d0a5cc4f4391226911684b98bb6d432f4e4798`.
Both finish at one exact order through basis 1009 with unavailable original
Futures, stale-A fencing, fresh-Peer agreement, and clean shutdown.

The accepted credential-scoped asymmetric partition/heal gate passes at
`/tmp/datomic-recovered-pair-ha-partition-v6`; all 126 manifest entries verify
at file SHA-256
`4e0a4ca8c8c58863712f8252dd66e320756dd70a02a38288f320ecf924299272`.
After a synchronized A heartbeat at revision 6, an 87 ms credential/session cut
leaves A live and transport-open with zero SQL sessions while Peer and B retain
reachability. B wins coordination at revision 7. Healing A causes its stale
heartbeat CAS to conflict and its process to self-fence. The continuing and
fresh Peers agree at basis 1066, the authoritative root advances revision 3 to
5, all three owned-role session counts finish at zero, PostgreSQL shuts down,
and all ports close. Raw negative-login stderr is not evidence; a
reason-specific result, generic node-password redaction, and an empty final
secret scan are. The v4 predecessor remains failed and unmodified.

These results close Goal 4 Stage 2 at the supported PostgreSQL authority
boundary. No additional scheduler/topology gate is justified: retained
frozen-active and concurrent schedules already pressure dual-writable
coordination, while arbitrary packet loss/reordering and multi-host topology
exceed the credential-scoped claim. Connector administration, temporary
queue/stream helpers, RPC-server branches, other dormant paths, and the 95 open
overlap rows remain classified residuals rather than automatic implementation
obligations. No overlap classification changes here; the ledger remains 13
resolved, nine bounded partial, and 95 open.

The next outcome-bearing boundary is the interleaved operational/architectural
configuration, startup, and readiness slice, not another overlap classifier.
