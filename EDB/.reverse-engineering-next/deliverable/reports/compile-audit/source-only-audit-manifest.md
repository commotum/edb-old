# Source-only namespace validation

- Timestamp: `2026-08-27` (`America/Los_Angeles`)
- Host: `Linux 6.17.0-35-generic x86_64`
- Java: `OpenJDK 21.0.12+8-1-24.04-Ubuntu`
- Result: `142 PASS, 0 FAIL`
- Execution model: one fresh JVM per recovered namespace, four in parallel

## Inputs

- Final source candidate: `/tmp/datomic-rev-latest35`
- Independent deterministic regeneration: `/tmp/datomic-rev-latest36`
- Sorted final source-content manifest SHA-256:
  `ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be`
- Peer input SHA-256:
  `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`
- Recovered handwritten Java source manifest SHA-256:
  `a56190357ccba95c77c3bddd1ee3e462dfa59b7344dc8f888284c1479ee0bf87`
- Compiled recovered Java class manifest SHA-256:
  `53fa85a1d6a345b5a6429daf7202b89c7851d3e0fcbd055fd1d089f03f478daf`
- Audited real Infinispan Hot Rod 5.1.2.FINAL SHA-256:
  `6c51255e7fa6391fa030aa843f1879dedab99876ea1818b2eccf52c198d2c3b6`
- Audited real Infinispan core 5.1.2.FINAL SHA-256:
  `8431afc1d1b48c9ed2c633019239df840fd14492ce60e5439fcd72a23b370746`
- Sorted 142-line PASS proof SHA-256:
  `b3d32105eb2aae048f0baf6945a99af1b351bb6cf87ed4989284ed4bc659377a`

The promoted overlay, installed tree, latest35, and clean latest36 regeneration are
byte-for-byte identical under `datomic/`. Both final decompilations reported
142 successes and zero failures.

## Classpath boundary

The validation classpath contained:

1. the recovered Clojure source root;
2. 47 classfiles compiled from the 43 recovered handwritten Java sources;
3. all 10 recovered peer resources at their exact classpath paths;
4. 532 shipped `lib/*.jar` dependencies after explicitly excluding the
   duplicate `core2` AOT JAR;
5. bytecode-derived compile-only declarations for the five referenced
   Infinispan Hot Rod types.

It did **not** contain `peer-1.0.7277.jar` or `core2-1.0.140.jar`. Consequently,
the 142 PASS records cannot be satisfied by falling back to the original
Clojure AOT classes.

The command shape was:

```bash
awk 'NR > 1 {print $1}' reports/source-index/namespaces.tsv \
  | xargs -P4 -n1 scripts/require-one-namespace.sh
```

Each worker executed `clojure.main scripts/require_namespace.clj NAMESPACE` in
a new process. The checked-in `scripts/validate-all-namespaces.sh` reproduces
the same isolation while using bytecode-derived compile-only Hot Rod stubs, so
it does not need to download the provided dependency.

After the namespace sweep, the same source-only classpath passed focused
regressions for reflective calls, foreign receivers, nested reifies, character
literals, namespaced keyword lookups, record keys, and terminal throws. The
separate `validate-end-to-end-parity.sh` run then matched the original Peer on
the canonical in-memory API result with SHA-256
`228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`.

## Warning accounting

Across all 142 fresh processes, 104 stderr files were nonempty and 38 were
empty. Repeated transitive compilation produced 6,898 warning lines:

- reflection warnings: 6,490 occurrences, 306 unique texts;
- primitive-local recur mismatch: 204 occurrences, 10 unique texts;
- auto-boxing loop argument: 204 occurrences, 2 unique texts;

There were 318 unique warning texts, not 6,898 distinct defects. No warning was
a load failure, but the reflection/boxing warnings define remaining
optimization and readability work.

## Scope

This proves source compilation/loading and dependency closure for every
recovered namespace. It does not prove runtime equivalence for every query,
transaction, storage backend, failure path, or concurrency schedule.
