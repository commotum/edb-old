# Bytecode architecture inventory

Primary artifact: `peer-1.0.7277.jar` (`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`).

## Exact primary counts

| Classes | Fields | Methods | Instructions | Calls | Literals |
|---:|---:|---:|---:|---:|---:|
| 5517 | 38718 | 22014 | 850016 | 169882 | 191265 |

Calls include direct method instructions and `invokedynamic`. Literals follow the rules documented in `BytecodeInventory.java` and `README.md`.

## Source provenance

| Kind | Classes |
|---|---:|
| clojure-aot | 5470 |
| java | 47 |
| unknown | 0 |

The 5470 AOT class files map to 142 reconstructed Clojure source paths; the 47 Java class files map to 43 top-level Java source paths. Namespace init classes: 142. Detailed closure is in `namespace-classes.tsv` and `namespace-dependencies.tsv`.

The provided Java source catalog contains 5513 files versus 5517 primary class entries. For this artifact, the difference is the nested handwritten classes that decompilers coalesce into their enclosing Java source; `classes.tsv` remains the class-file authority.

### Class-file versions

| Major | Class entries |
|---:|---:|
| 49 | 4960 |
| 52 | 510 |
| 55 | 47 |

## Largest primary namespaces by instruction count

| Namespace | Classes | Methods | Instructions | Calls |
|---|---:|---:|---:|---:|
| `datomic.db` | 579 | 2634 | 95792 | 18107 |
| `datomic.index` | 248 | 934 | 46829 | 8820 |
| `datomic.integrity` | 189 | 684 | 38259 | 7672 |
| `datomic.datalog` | 218 | 749 | 32240 | 6335 |
| `datomic.log` | 194 | 907 | 31758 | 6397 |
| `datomic.peer` | 127 | 548 | 22234 | 4267 |
| `datomic.backup` | 136 | 532 | 21582 | 4296 |
| `datomic.ddb` | 69 | 269 | 20544 | 4064 |
| `datomic.query` | 115 | 434 | 17060 | 3416 |
| `datomic.core2.atom.logged` | 90 | 296 | 16386 | 2635 |
| `datomic.cluster` | 88 | 320 | 15139 | 3114 |
| `datomic.common` | 101 | 424 | 13225 | 2782 |
| `datomic.connector` | 85 | 300 | 12329 | 2377 |
| `datomic.valcache` | 65 | 242 | 12178 | 2438 |
| `datomic.s3-api` | 41 | 163 | 12137 | 2406 |
| `datomic.kv-cluster` | 58 | 202 | 11473 | 2019 |
| `datomic.fulltext` | 58 | 288 | 11263 | 2192 |
| `datomic.cloudwatch` | 33 | 130 | 10762 | 2101 |
| `datomic.datafy` | 77 | 285 | 10714 | 2762 |
| `datomic.tools` | 81 | 298 | 10307 | 2229 |
| `datomic.garbage` | 54 | 193 | 9449 | 1930 |
| `datomic.pull` | 59 | 220 | 8943 | 1744 |
| `datomic.artemis-client` | 92 | 347 | 8608 | 1693 |
| `datomic.uri` | 63 | 239 | 8393 | 1745 |
| `datomic.api` | 67 | 286 | 7610 | 1538 |

## Distribution and dependency evidence

The companion index covers 533 JARs and 187068 distinct class names. The distribution POM declares 33 direct dependencies: 29 exact filename matches, 2 artifact-only fallbacks, and 2 missing artifacts.

The embedded Maven POM is byte-identical to the distribution POM (`34b9caaa92b72b5a6d69397c5d7908a5b79fc7c29cb75ba4af490ff59fed0be9`).

| Coordinate | Scope | Resolution | Shipped JARs |
|---|---|---|---|
| `org.clojure:clojure:1.9.0` | compile | artifact-fallback | clojure-1.11.4.jar |
| `org.slf4j:slf4j-api:1.7.36` | compile | artifact-fallback | slf4j-api-1.7.32.jar |
| `org.infinispan:infinispan-client-hotrod:5.1.2.FINAL` | provided | missing |  |
| `couchbase:couchbase-client:1.0.3` | provided | missing |  |

There are 544 duplicate class names across the indexed distribution, including 510 primary/companion overlaps. Resolution consumers must retain classpath order; `duplicate-classes.tsv` records every ambiguity.

| Overlap companion | Shared classes | Companion classes | Complete class closure | Byte-identical | Different bytes |
|---|---:|---:|---|---:|---:|
| `core2-1.0.140.jar` | 510 | 510 | true | 510 | 0 |

Use `external-owners.tsv` for every non-primary owner referenced by primary bytecode, `pom-dependencies.tsv` for declared-vs-shipped dependency evidence, `resources.tsv` for the ZIP resource catalog, and `duplicate-classes.tsv` before assigning any external owner to a unique companion.

### Primary resources

| Entry | Bytes | SHA-256 |
|---|---:|---|
| `META-INF/MANIFEST.MF` | 64 | `ac381c98524493f5f6e16c30e196f3c556371f894c58295d12caa519f9e48b0b` |
| `META-INF/maven/com.datomic/peer/pom.xml` | 13023 | `34b9caaa92b72b5a6d69397c5d7908a5b79fc7c29cb75ba4af490ff59fed0be9` |
| `com/datomic/core2/manifest.edn` | 72 | `344505234c22f96fcac180b118225f8021fb14ad5f6bd662c734560d4b06c67c` |
| `copyright` | 58 | `42007cd18bf4de9dfe65a1707878fae9c761ebb1c95f2bd599b19ce9a17ecc7b` |
| `data_readers.clj` | 103 | `d681bd3c1854dd6303b29565f18451645a7265d64710fb7d2ee4e6abd55c3fc0` |
| `datomic/VERSION` | 8 | `5b9f9a16f9249dd933274578f4deb26eeb208178c66077c6be373365d780fc5d` |
| `datomic/aws/instance-arch.edn` | 1729 | `a0ce8dc0e131071523b6be446a8ea1ca91bc7085f1661b44b7d39b6e50750973` |
| `datomic/aws/region-arch-ami.edn` | 751 | `402a1c4343bdc7951d5806344e026c4e18fa10942a642b4f4b41ccda4f143cb5` |
| `datomic/transactor-key.jks` | 1368 | `f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1` |
| `datomic/transactor-trust.jks` | 662 | `ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b` |

## Reproducibility boundary

ZIP timestamps are intentionally excluded. Rows, sets, and maps are sorted; paths identify inputs; JAR SHA-256 values bind the run to exact bytes. No class loading or code execution from an analyzed JAR occurs.
