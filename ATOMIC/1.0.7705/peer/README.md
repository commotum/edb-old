# Peer reference source

This directory contains the Datomic Pro 1.0.7705 Peer reference corpus. The
Peer is the application-linked side of the system: it exposes database values,
runs local queries, submits transactions, and advances to newly published
database states. The matching Transactor corpus is its sibling in
`../transactor/`.

| Path | Contents |
|---|---|
| `src-clj/` | 156 recovered Datomic namespaces plus the packaged `data_readers.clj` |
| `src-java/` | 43 recovered handwritten-origin Java files |
| `source-manifests/` | The retained Java-origin boundary; entries are relative to `src-java/` |

The Clojure recovery completed directly for 155 of 156 namespaces in isolated
strict runs. `datomic.index` required a manual reconstruction of the small
`aligned-dedup` loop. Its AOT-compiled normalized JVM instructions match the
official Peer method exactly; only erased local variable spellings remain
inferred.

The decompiler restored 1,904 ordinary named functions as `defn` using the
artifact's persisted names and arities. Ambiguous macro-generated roots remain
in explicit `def`/`fn` form. Namespace loaders, exact metadata resets, protocol
scaffolding, and compiler temporaries also remain, so this is easier-to-read
initializer source rather than an original-like source tree.

The 1.0.7705 Java surface adds the current tuple-discontinuation query,
reverse-seek database operation, and backup-listing API to the historical
source. Its 43 renderings are identical to the Transactor's shared Java files,
and all compile with Java 11 source/target settings.

This is evidence recovered from an AOT-compiled artifact, not the authors'
literal source tree and not a standalone build. JARs, class files, packaged
resources, dependency libraries, and a runnable Peer distribution are
deliberately absent.
