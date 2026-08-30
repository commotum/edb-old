# Datomic Pro 1.0.7277 reference corpus

This is the validated historical recovery of the matched Datomic Pro 1.0.7277
artifacts.

| Artifact | Declared Clojure | SHA-256 |
|---|---:|---|
| Peer | 1.9.0 | `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba` |
| Transactor | 1.11.4 | `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692` |

- `peer/` contains 142 recovered namespaces, `data_readers.clj`, 43 retained
  handwritten-origin Java sources, and their source manifest.
- `transactor/` contains 247 recovered Clojure source files and 46 retained
  Java-origin source files. Its Clojure tree includes bundled dependencies, so
  presence is not an authorship claim.

The different Clojure compiler lineages generated substantial bytecode-shape
noise across otherwise shared code. Preserve this corpus as a known-good
historical witness when evaluating later releases.
