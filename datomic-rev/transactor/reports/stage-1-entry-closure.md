# Goal 2 Stage 1: Transactor entry-closure accounting

## Result

Every non-directory entry in `datomic-transactor-pro-1.0.7277.jar` is
accounted for without loading a licensed class:

- 9,682 class entries -> 293 source owners.
- 9,630 Clojure AOT classes -> all 247 initializer namespace closures.
- 52 Java classes -> all 46 Java source candidates.
- zero orphan classes and zero unresolved class-owner ambiguities.
- 11 resources -> exact evidence extraction under `/tmp` plus an explicit
  candidate-vs-reference policy; zero classfiles were extracted.

Input SHA-256:

- Transactor: `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`
- Peer: `cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`
- checked-in Stage-0 baseline manifest file:
  `8777e071f03f3ecf1008bfc151f4ae77c16bc26f2b61f8185798b697786dfa82`

## Class closure

Role totals:

| Role | Classes |
|---|---:|
| namespace initializer | 247 |
| AOT function/generated | 9,087 |
| AOT type/interface | 296 |
| Java top-level | 46 |
| Java nested | 6 |
| **Total** | **9,682** |

Initializer matching uses the JVM namespace stem and the only three Clojure
AOT shapes in the artifact: exact `__init`, `$` function/generated prefix, or
`/` type/proxy prefix. `SourceFile` (`.clj` or `.cljc`) resolves nested
namespace-prefix overlap. Java ownership is exact package + `SourceFile`.

Of 9,682 classes, 7,687 have one structural source candidate, 1,555 have two,
and 440 have three. The 1,995 multiple-prefix rows are expected when one
namespace lives beneath another: 1,993 resolve to a unique `SourceFile`. Two
generated proxy classes omit `SourceFile` and therefore remain explicitly
listed as convention-resolved in `ambiguous-classes.tsv`:

1. `clojure/tools/reader/default_data_readers/proxy$java/lang/ThreadLocal$ff19274a.class`
   (`bd17eb93631cfaa0a4442df5237306f6bb3a5e1dad647095e4d469ac2858362d`)
   -> `clojure.tools.reader.default_data_readers`. Raw call evidence independently
   corroborates the longest-prefix assignment: both
   `default_data_readers$fn__3881` and `$fn__3897` invoke its constructor.
2. `datomic/query/support/proxy$clojure/lang/ASeq$Counted$7e5d62ee.class`
   (`548838e60f4b66cfccebc746b89608e625518c1b5de1d5a238e7d35cc0a8f8e0`)
   -> `datomic.query.support`. `datomic/query/support$counted_seq` directly
   invokes its `(IPersistentMap)` constructor.

Thus neither row is an unresolved ownership gap; both are retained so the
weaker missing-`SourceFile` evidence is not hidden. The compact map also agrees
with all 9,682 canonical raw-inventory roles and all 9,630 raw Clojure namespace
assignments.

The six Java nested classes are:

- `datomic/Database$Predicate`
- `datomic/impl/Exceptions$IllegalArgumentExceptionInfo`
- `datomic/impl/Exceptions$IllegalStateExceptionInfo`
- `datomic/impl/PriorityExecutor$ComparableFutureTask`
- `org/eclipse/jetty/servlets/EventSource$Emitter`
- `org/eclipse/jetty/servlets/EventSourceServlet$EventSourceEmitter`

## Resources

Eight resources are byte-identical to Peer and three are Transactor-only.
Exact Transactor bytes were extracted only to
`/tmp/datomic-goal2-entry-closure-v4/licensed-evidence/resources`; these are
reference evidence, not proposed candidate-runtime inputs.

| Entry | Relation | Category / phase | Candidate treatment | SHA-256 |
|---|---|---|---|---|
| `META-INF/MANIFEST.MF` | Peer-identical | build metadata / core packaging | regenerate | `ac381c98524493f5f6e16c30e196f3c556371f894c58295d12caa519f9e48b0b` |
| `META-INF/maven/com.datomic/datomic-transactor-pro/pom.xml` | Transactor-only | dependency/provenance metadata / core build | reconstruct candidate POM from verified dependency inventory | `8de366709ed36188a8596ae9e412c6e9ed9a226938eb39e6b6650a36a8892304` |
| `copyright` | Peer-identical | legal notice / core packaging | candidate notice plus required third-party notices; retain original as evidence pending license review | `42007cd18bf4de9dfe65a1707878fae9c761ebb1c95f2bd599b19ce9a17ecc7b` |
| `css/bootstrap.min.css` | Transactor-only | Bootstrap 2.1.1 / optional UI | source from official upstream release and retain license | `a267aec5f3af2e1a85516a75831aaa4d0205fdbdef72ff35cf58525653e1dcf3` |
| `data_readers.clj` | Peer-identical | Clojure reader registration / core runtime | reconstruct and test the three reader registrations | `d681bd3c1854dd6303b29565f18451645a7265d64710fb7d2ee4e6abd55c3fc0` |
| `datomic/VERSION` | Peer-identical | version metadata / core packaging | generate candidate version; record 1.0.7277 as compatibility target | `5b9f9a16f9249dd933274578f4deb26eeb208178c66077c6be373365d780fc5d` |
| `datomic/aws/instance-arch.edn` | Peer-identical | AWS deployment metadata / later backend | reconstruct or replace with tested current discovery | `a0ce8dc0e131071523b6be446a8ea1ca91bc7085f1661b44b7d39b6e50750973` |
| `datomic/aws/region-arch-ami.edn` | Peer-identical | stale AMI catalog / later backend | replace with current API/config discovery | `402a1c4343bdc7951d5806344e026c4e18fa10942a642b4f4b41ccda4f143cb5` |
| `datomic/transactor-key.jks` | Peer-identical | TLS identity/private-key store / transport | never ship; generate a new candidate-owned identity per environment | `f3627f52580b84fe8f536423643fb46999fd2458498989307f2820d778eb73a1` |
| `datomic/transactor-trust.jks` | Peer-identical | TLS trust store / transport | never ship; generate trust anchors paired with candidate identity | `ca64f839d051d909974a624b4345d83cc739a0b90edbe54e7c3605a2fd8fc13b` |
| `js/bootstrap.min.js` | Transactor-only | Bootstrap 5.3.3 / optional UI | source from official upstream release and retain MIT notice | `dd0b8fdef4bd9d4d51bafdb1f039ebbda981a4134b4d88f55f4f08b5a246395d` |

The current recovered Peer source artifact intentionally packages ten
byte-exact Peer resources. Eight of those are the Peer-identical Transactor
rows above, including both JKS files. Therefore class/JAR isolation is clean,
but strict *all licensed bytes* isolation is not true of that artifact. The
safe Goal-2 shape is two profiles:

1. Keep the existing byte-parity Peer artifact and `resources/` frozen as a
   reference/oracle profile.
2. Build a separate integration candidate from recovered source plus a new
   `candidate-resources` tree. Generate manifests, POM/version, and ephemeral
   TLS stores; source optional Bootstrap assets from upstream; reconstruct or
   defer AWS catalogs. Never put the vendor JKS hashes above on its classpath.

This preserves the Peer reference without silently treating it as the clean
Peer+Transactor runtime.

## Original classfile boundary

The accounting program streams and hashes original ZIP class entries but never
extracts them. A current repository scan (excluding `goal-1`) found 21 direct
`.class` files: 19 regenerated SQL fixtures and two decompiler-tool outputs.
None has a SHA-256 equal to any of the 9,682 original Transactor classes. The
audit files are:

- original hash set: `74e6b156f6b28bb00048c5cc8bfa7ad0d6f61da7c4004e989caaf5311b62e578`
- repository direct-class audit:
  `cf3e98403d5b5f54e2d0f4c29409d00c340258f440e6cc44b9650530d3721704`

That zero-intersection result is a contamination check for the repository as
it existed before the recovered Java tree was integrated, not a valid rule for
future source builds. A deterministic recompilation can legitimately emit a
classfile byte-identical to the original. Candidate isolation must instead be
proved by a clean source build whose recorded compile and runtime classpaths
exclude both original implementation JARs, whose outputs originate only in
the clean build directory, and whose comparison tool merely streams originals
as an out-of-process oracle. A separate resource gate should reject the two
vendor JKS hashes while allowing independently sourced public assets to carry
their own provenance even if bytes happen to agree.

## Determinism and hashes

Reproduction tool:

- `datomic-rev/transactor/tools/MapTransactorEntries.java`
- SHA-256: `6ad235df82a4ac6ea60b20d4d4bc7a81e9c05ee34a34163c9f4aff45e2d973a0`

The checked-in wrapper is
`datomic-rev/transactor/scripts/account-entries.sh` (SHA-256
`a9ca16e0b36ed4efbb3ab5825eb0a003a3d06ddeef5bec74a48505e4a1241eff`).
It rejects any evidence output path inside the repository, verifies the
Stage-0 manifest before reading the licensed artifacts, and verifies the
complete generated manifest before reporting success.

Canonical result:

- `/tmp/datomic-goal2-entry-closure-v4`
- `manifest.sha256` file SHA-256:
  `33cfe7358aac525f657b175d8a639b42e4a16160a388ea0282a31c035c387484`
- `class-ownership.tsv`:
  `bb3be13708ba945dd2f8d2edce7adffb7d43c8a6fb265cdca0c3d2fa65122b87`
- `owner-class-counts.tsv`:
  `4289c557017fe208c84f691271dfeb713f0cb19ce12c92f26ddada7a89112b7a`
- `ambiguous-classes.tsv`:
  `6840518b5044fb21f75e4fb577422b3bcdc7062a35d5ec9cd5bc803d6512cfe4`
- `unresolved-classes.tsv`:
  `79907e32b540d628e9fe2d4e75898d8aecab7d31151f049d1a91b109c69db0f6`
- `orphan-classes.tsv`:
  `b9585c0f6091281e455ec3035f65697acba8515aaf7f05b57f56bd0e91a98141`
- `resolved-prefix-overlaps.tsv`:
  `e4d80392625110d52efe8e0ab40394ed3fb454a4d9182d8b99286b82699778d7`
- `resource-classification.tsv`:
  `a0005b405646621cb102af214b907d737b412458358875fd7fcd5e10d63e2d35`
- `licensed-evidence/resources-manifest.tsv`:
  `39ac89ca172adcbd45e8bf6094daff6cb1f4175905f978deaadd183d75a1338e`
- `summary.tsv`:
  `52c2b5c60dbdb53dc04789f30d01c0a67fa1c5127a943b8c0466534acd60d92b`

The v4 and wrapper-generated v5 output trees compare byte-for-byte equal.
Reproduce with:

```bash
datomic-rev/transactor/scripts/account-entries.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-entry-closure-v5
diff -qr /tmp/datomic-goal2-entry-closure-v4 \
  /tmp/datomic-goal2-entry-closure-v5
```

## Repository integration and remaining gates

The program, guarded wrapper, compact ownership/count/ambiguity/resource maps,
and summary are now checked in under `transactor/tools`, `scripts`, and
`reports`. Extracted originals remain outside the repository; no original
class or resource byte is part of the compact evidence set.

The structural accounting portion of Stage 1 is closed: `9,682 accounted / 0
orphan / 0 unresolved`, all 293 owners present, and deterministic rerun
equality. When candidate artifacts first exist, their build must add the two
deferred isolation gates: (a) reproducible compile/runtime provenance showing
that neither original implementation JAR nor extracted original class is a
candidate input, and (b) no candidate resource hash equals either vendor JKS
hash. Byte-identical classes produced by the controlled source compilation are
valid recovered outputs, not evidence of copying. Optional UI assets
additionally require explicit upstream origin and license records.
