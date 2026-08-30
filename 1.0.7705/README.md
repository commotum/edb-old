# Datomic Pro 1.0.7705 reference corpus

This is the recovered strict initializer-level source corpus for the matched
Datomic Pro 1.0.7705 distribution. Artifact recovery was completed on
2026-08-30; source-study normalization is intentionally partial.

The source artifacts were retrieved from the official distribution at
`https://datomic-pro-downloads.s3.amazonaws.com/1.0.7705/datomic-pro-1.0.7705.zip`.

| Artifact | Bytes | Declared Clojure | SHA-256 |
|---|---:|---:|---|
| Distribution ZIP | 272,642,957 | — | `a17c2603b893dfb0d998a35a032a7295736d234d32937222c8ec21d81a1b8c7e` |
| Peer JAR | 7,212,966 | 1.11.4 | `d8145fecb7fad6a0dc8517235c3accc86e85756e765bda85bd02091c3e8f79b9` |
| Transactor JAR | 12,378,379 | 1.12.5 | `621297e55a92205d923c5711ec1e52d356cb682288e3ed91c016880e52ca29cf` |

## Contents

| Artifact | Clojure reference source | Java-origin reference source |
|---|---:|---:|
| `peer/` | 156 Datomic namespaces plus `data_readers.clj` | 43 files |
| `transactor/` | 247 files: 246 `.clj` and one `.cljc` | 46 files |

The Transactor Clojure count comprises 163 `datomic.*` files and 84 bundled
dependency files. The dependency files are part of the artifact's runtime
closure; their presence is not an authorship claim.

## Recovery provenance

- Peer recovery produced 155 namespaces directly in isolated strict runs. The
  remaining namespace, `datomic.index`, needed one small control-flow repair in
  `aligned-dedup`. AOT compilation of that replacement matches the official
  nested method's normalized JVM instructions exactly. The packaged
  `data_readers.clj` was retained verbatim.
- Of the 163 Datomic Transactor files, 160 were produced directly in isolated
  strict runs, the same `datomic.index/aligned-dedup` repair was applied, and
  two sources were available verbatim in bundled Datomic dependency JARs.
- All 84 non-Datomic dependency sources were available verbatim in their
  owning JARs. `transactor/source-manifests/exact-clojure-sources.tsv` records
  those 84 files and the two exact Datomic dependency sources.
- The retained Java boundary is unchanged from 1.0.7277: 43 source owners are
  shared and the Transactor has three additional owners. The recovered Java was
  checked against the 1.0.7705 class surfaces and updated for the current API.
  The 43 shared source renderings are identical in both directories, matching
  the fact that their official 1.0.7705 classes are byte-identical.

The repaired decompiler also uses persisted function metadata to restore an
ordinary `defn` when source names and fixed/variadic arities can be established
safely across Clojure 1.11/1.12 bytecode shapes. This raised readable Datomic
`defn` forms from 928 to 1,904 in the Peer and from 1,056 to 2,115 in the
Transactor. Ambiguous macro-generated roots remain visibly compiler-shaped
instead of being guessed.

The current Peer and Transactor are substantially better aligned than the
historical pair: every shared Java-origin class is byte-identical across the two
1.0.7705 JARs, and both Clojure recoveries encountered the same single hard
method. They still declare different Clojure versions, so compiler-lineage
differences have not disappeared entirely.

## Validation boundary

- Every Peer source file reads successfully under its declared Clojure 1.11.4,
  and every Transactor source file reads successfully under its declared
  Clojure 1.12.5. One exact legacy Ring source uses the historical, benign
  reader form `#=(int \=)` and was validated with normal read-eval behavior
  enabled.
- Neither source tree contains a broken-decompilation marker.
- All 43 Peer and all 46 Transactor Java sources compile with the retained
  compiler helper using Java 11 source/target settings.
- The repaired decompiler's retained regression suite passes, including its
  Clojure 1.11/1.12 function/protocol metadata and JVM `invokedynamic`
  regressions. A current-tool historical check also recovers all 142 Peer
  namespaces from 1.0.7277.

This does not mean the tree is the unpublished original source or that it
rebuilds byte-for-byte identical JARs. Compilation erased comments, formatting,
some names, and some macro choices. The local Java compiler also differs from
the official build compiler. A complete source-only runtime, resource bundle,
and executable oracle have not been rebuilt for this release.

The Clojure remains initializer-level source: namespace-loading wrappers,
exact Var metadata resets, protocol metadata scaffolding, and some compiler
temporaries are still present. It is substantially easier to read than the raw
first pass, but should not be treated as original-like source when inferring
Rich Hickey's source formatting or macro choices.

Only recovered source, source-boundary manifests, and concise provenance belong
in this directory. The downloaded distribution, JARs, extracted classes,
reports, and scratch recovery products are deliberately absent.
