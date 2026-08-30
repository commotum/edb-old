# Peer reference source

This directory contains the recovered Datomic Pro 1.0.7277 Peer corpus. The
Peer is the application-linked side of the system: it exposes database values,
runs local queries, submits transactions, and advances to newly published
database states. The Transactor corpus is its sibling in `../transactor/`.

This is not a buildable product, a redistributable Datomic replacement, or the
literal source tree written by the original authors.

The Clojure files were recovered from ahead-of-time JVM classes. They preserve
substantial program structure and behavior, but compilation erased comments,
formatting, some local names, and some original macro syntax. The Java boundary
keeps only files identified as handwritten-origin source; Java decompilations
of Clojure-generated classes were removed because they duplicate the Clojure
recovery in a much noisier form.

## Active corpus

| Path | Contents |
|---|---|
| `src-clj/` | 142 recovered Peer namespaces plus `data_readers.clj` |
| `src-java/` | 43 handwritten-origin Peer Java files |
| `source-manifests/` | The retained handwritten Peer Java boundary; entries are relative to `src-java/` |

This directory records which artifact a source file was recovered from; it is
not a strict authorship or subsystem-ownership claim. Some `datomic.*`
namespaces have related or different counterparts in the Transactor corpus.

## Deliberate omissions

The active tree no longer carries licensed or reconstructed JARs, class files,
key stores, packaged resources, generated source indexes, forensic baselines,
recovery reports, one-off probes, launchers, or broad validation harnesses.
Consequently, this directory does not currently compile or run the recovered
Peer by itself.

When the Rust port needs an answer key, build the smallest fixture and oracle
adapter that exposes the behavior in question. Historical recovery machinery
is available at Git commit
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`; consult or restore individual
pieces only when a concrete ambiguity justifies it.
