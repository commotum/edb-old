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

The Datomic namespace decompiler is deliberately separate:

```bash
JOBS=4 NAMESPACE_TIMEOUT=1200 \
  transactor/scripts/decompile-clojure.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-decompile
```

It isolates each initializer in a bounded JVM and supports an explicit
`DATOMIC_TRANSACTOR_JAVA_HEAP` for diagnostics. Stage 1 remains incomplete:
the Datomic Clojure output and clean candidate-resource/dependency closure still
have open validation work. No candidate Transactor runtime is claimed by these
structural tools.
