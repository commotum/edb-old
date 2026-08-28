# Stage 0 — frozen Peer/Transactor boundary

Stage 0 is complete for repository HEAD
`0b9f274f50d8bbfbd6dfc3b2585a3ab88563083b`. This report freezes what is an
input, what is already recovered, and what must still be reconstructed. It
does not claim that a recovered Transactor exists yet.

## Peer reference

The licensed Peer input is `peer-1.0.7277.jar`, 6,801,387 bytes, SHA-256
`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`.
The current repository inputs match the retained final manifests with no
mismatch:

- 142 Clojure namespace sources — manifest
  `186b247033a461b4e5f391a302c43b9a727cf4fde0db2b10487a6c001b9da2e1`;
- 43 handwritten Java sources — manifest
  `77a136d05420250c3c9160d912f0c8619b886e2d7897adc27709969b0b2a73dd`;
- 10 exact resources — manifest
  `fd8d89751c628b4562b3c31691b2c3ce6fb44d2d770eaf73ca6aa568e070e15a`;
- 532 dependency JARs — manifest
  `76a02d5dfd78cd63451944856cdf472c8802a63032c86e021d1f1c9450dc2058`.

The two retained recovered-Peer builds remain byte-identical at SHA-256
`bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`.
The Stage 1, 2, and 3 evidence manifests under `/tmp` were rechecked at 1,230,
118, and 88 files respectively, with top hashes `531b2f2e...960a`,
`25e5f821...f721`, and `8f69d3fa...84f6`. Because no Peer input or harness
changed, rerunning the expensive service matrices would add no new evidence.
Those raw bundles are still temporary and their exact identities, rather than
false archival durability, are the boundary used here.

## Complete Transactor corpus

The licensed Transactor input is
`datomic-transactor-pro-1.0.7277.jar`, 12,689,398 bytes, SHA-256
`d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`.
Its physical archive has 9,871 entries:

| Entry kind | Count |
|---|---:|
| Directories | 178 |
| Clojure AOT classfiles | 9,630 |
| Java classfiles | 52 |
| Non-class resources | 11 |

The archive expands to 22,749,547 bytes; 9,694 entries are deflated and 177
are stored. It has no duplicate, absolute, or parent-traversing names and no
archive or entry comments. The checked-in physical entry manifest is SHA-256
`33f00c550950d9ceab871a03e7d39d7c28cda9229e0e55b64f2f4c8e6972d5fa`.

The 247 namespace initializers are not all proprietary Datomic namespaces:

| Initializer ownership | Count |
|---|---:|
| `datomic.*` | 162 |
| Bundled Clojure/library code | 85 |

The whole artifact must still be accounted for. The ownership split prevents
bundled core.async, Ring, Hiccup, Liberator, Cognitect, and related code from
being mislabeled as Datomic-authored source.

Against the Peer, the Datomic namespace union has 187 names: 117 shared, 45
Transactor-only, and 25 Peer-only. Every shared initializer differs
byte-for-byte. Across all class paths, 2,090 names are shared, 7,592 are
Transactor-only, and 3,427 are Peer-only. Only 47 shared classfiles are
byte-identical; all 47 are the Peer's Java class surface. The other 2,043
shared classfiles differ. Peer source is therefore reference evidence, not an
automatic overlay.

The 52 Java classfiles coalesce to 46 source-path candidates: 43 shared paths
and three Transactor-only paths:

- `datomic_jetty/impl/ProxyHandler.java`;
- `org/eclipse/jetty/servlets/EventSource.java`;
- `org/eclipse/jetty/servlets/EventSourceServlet.java`.

Those three are recovery inputs, but their paths do not by themselves prove
Datomic authorship; the two `org.eclipse.jetty` paths are treated as vendored
third-party surfaces until provenance is resolved.

All 11 resources are explicitly recorded, including the embedded POM,
manifest, copyright, `data_readers.clj`, `datomic/VERSION`, AWS EDN data, two
JKS stores, and Bootstrap CSS/JavaScript.

## Dependency and launch boundary

The distribution-root `pom.xml` describes the Peer. The authoritative
Transactor dependency manifest is
`META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml` inside the
Transactor JAR, SHA-256
`8de366709ed36188a8596ae9e412c6e9ed9a226938eb39e6b6650a36a8892304`.
It declares 61 direct dependencies: 59 resolve exactly in the distribution and
two are absent provided dependencies:

- `org.infinispan:infinispan-client-hotrod:5.1.2.FINAL`;
- `couchbase:couchbase-client:1.0.3`.

Both absences concern deferred alternative backends. The PostgreSQL driver
`postgresql-42.5.1.jar`, Tomcat JDBC pool, and Tomcat Juli support resolve in
the shipped dependency set.

`bin/transactor` changes to the distribution root and launches
`clojure.main --main datomic.launcher`. `bin/classpath` expands to
`resources`, the Transactor JAR, all 533 `lib/*.jar` files, `samples/clj`,
`bin`, and caller-controlled `DATOMIC_EXT_CLASSPATH`. It does not include the
Peer JAR. Canonical candidate runs will use an explicit sorted, hash-bound
classpath and require an empty or fully manifested `DATOMIC_EXT_CLASSPATH`;
they will not inherit this wildcard launch boundary blindly.

The optional Datomic-owned surfaces are now located but remain lower priority:

- Peer Server and REST implementations are namespaces inside the Transactor
  artifact; thin-client support is in the shipped `client*.jar` family.
- Console is `lib/console/datomic-console-0.1.242.jar`, SHA-256
  `64b345bb381528d9beebae285522be91c3fe86b53ceaed1df951ee69ea8aa206`.
- Presto integration is
  `presto-server/plugin/datomic/datomic-presto-0.9.72.jar`, SHA-256
  `a11d56c996139ccfc617c723a6b66fe196ea2da277745a7d2094b2a43864a6f3`.

The 533 direct libraries, 53 `bin` files, distribution resource, and the two
optional first-party artifacts are hash-bound in `baseline/`.

## Tool and execution boundary

The structural scanner is the repository's generic ASM 9
`BytecodeInventory.java`. It reads bytecode without defining or executing any
analyzed class. `ArchiveInventory.java` adds physical order, ZIP metadata, and
per-entry content hashes. `inventory.sh` verifies both proprietary artifact
hashes, extracts and verifies only the embedded POM, runs the scanner with the
Peer as an explicit comparison detail JAR, and emits the compact baseline.

Three complete Transactor-primary scans were run during this reconciliation;
the two independent audit scans produced byte-identical copies of all 19 raw
outputs, parsed all 9,682 classes, and reported zero warnings. The canonical
checked-in baseline verifies with `sha256sum -c`; its manifest file is SHA-256
`8777e071f03f3ecf1008bfc151f4ae77c16bc26f2b61f8185798b697786dfa82`.
The raw occurrence-level scans are roughly 323 MB each and remain under
`/tmp`; the compact baseline retains exhaustive entry/class identity without
checking proprietary classfiles into the repository.

Original Peer and Transactor implementations are licensed inputs/oracles only.
Neither may enter a candidate runtime. Every other Datomic-owned dependency
must receive an explicit recover-or-retain decision before an end-to-end claim;
third-party machinery such as PostgreSQL, JDBC, Artemis, Lucene, and Jetty can
remain dependencies while Datomic-specific semantics around them are
recovered.

## Result and next action

The Peer reference, whole Transactor input, dependency and launch boundary,
optional artifact map, and candidate/oracle separation are frozen. Stage 1 can
now recover the whole corpus.

Stage 1 evidence supersedes the recovery method anticipated here without
changing this frozen boundary. An exhaustive dependency-source scan proved
that the canonical 247-namespace tree is 160 strict Datomic decompiles, two
exact Datomic dependency sources, and 85 exact bundled sources, rather than
247 decompiles. See `stage-1-clojure-recovery.md`. The 46-source Java slice is
reported separately in `stage-1-java-recovery.md`. Resource classification is
complete, while clean candidate-resource and dependency closure remains a
Stage 1 gate.
