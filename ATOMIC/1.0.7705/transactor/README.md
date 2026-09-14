# Transactor reference source

This directory contains the Datomic Pro 1.0.7705 Transactor reference corpus.
The Transactor admits and serializes writes, assigns their authoritative order,
publishes the durable log, coordinates indexing, and prevents stale writers
from retaining authority. The matching application-facing Peer corpus is its
sibling in `../peer/`.

| Path | Contents |
|---|---|
| `src-clj/` | 247 source files: 163 Datomic and 84 bundled dependencies |
| `src-java/` | 46 recovered Java-origin files |
| `source-manifests/exact-clojure-sources.tsv` | Archive ownership for 86 sources retained verbatim |

Of the 163 Datomic Clojure files, 160 were recovered directly in isolated
strict runs, two were present as exact source in bundled Datomic dependency
JARs, and `datomic.index` received the same bytecode-verified
`aligned-dedup` repair as the Peer. All 84 non-Datomic Clojure files were
available as exact source in their owning dependency JARs. Classify a file's
origin before drawing conclusions about Datomic-specific design or style.

Within the Datomic files, the decompiler safely restored 2,115 ordinary named
functions as `defn`. Ambiguous macro-generated roots, namespace loaders, exact
metadata resets, protocol scaffolding, and compiler temporaries remain
explicit. The Datomic portion is therefore recovered initializer source, not
an original-like source tree; the 86 manifest-listed archive sources are
verbatim exceptions.

The Java tree contains the same 43 source owners as the Peer plus
`datomic_jetty.impl.ProxyHandler`, Jetty `EventSource`, and
`EventSourceServlet`. All 46 files compile with Java 11 source/target settings.

This is a source-reference corpus, not a runnable recovered distribution.
JARs, classes, packaged resources, dependency binaries, launchers, and broad
historical recovery harnesses are deliberately absent.
