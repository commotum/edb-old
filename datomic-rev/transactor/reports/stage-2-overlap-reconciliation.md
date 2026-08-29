# Stage 2 Peer/Transactor overlap reconciliation

## Current checkpoint

The current promoted-tree ledger resolves 9 of the 117 shared namespaces. The
remaining 108 are open. This is a semantic-evidence count, not a count of files
that merely load.

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

## Next boundary

Continue the bounded transaction/transport cohort in this order:
`datomic.promise`, `datomic.queue`,
`datomic.builtins`, `datomic.reconnector2`, `datomic.connector`, and
`datomic.artemis-client`. Existing transaction, crash/ack, reconnect, and HA
evidence prioritizes and partially covers those rows; it does not itself
resolve any of them. Keep broad `datomic.db`, `datomic.peer`,
`datomic.fressian`, and `datomic.error` rows and the other six central-cohort
rows explicitly partial. Return to a partial row only when a focused dormant-
branch probe or sealed namespace-wide bytecode relation can close it. Do not
normalize arbitrary compiler scaffolding merely to improve the source-form
acceptance count.
