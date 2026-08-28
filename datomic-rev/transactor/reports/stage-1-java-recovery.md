# Goal 2 Stage 1: Transactor Java-origin recovery

## Result

The complete Java-origin slice of `datomic-transactor-pro-1.0.7277.jar` is
recovered under `transactor/src-java` without changing the frozen Peer source:

- 46 recovered `.java` sources compile to exactly 52 expected `.class` files.
- The 52 classes contain 122 fields and 303 methods/descriptors, exactly matching
  the original surface.
- All 52 class ABIs match under the checked-in structural normalization.
- All 52 executable-code normalizations match instruction-for-instruction,
  including constants, calls, field operations, branches, switches, and
  try/catch tables.
- Two independent Corretto 11.0.22 builds have byte-identical class manifests.
- No original classfile was copied into the repository or extracted by the
  validator.

This closes Java source recovery, not Stage 1 or Goal 2. Clojure AOT recovery,
candidate resources, PostgreSQL behavior, transport, indexing, and HA still
have separate completion gates.

## Corpus and overlap

The Stage-0 class census identifies 52 Java-origin classes from 46 source
owners. The checked-in source and class lists are complete against that census.

- 43 sources / 47 classes are shared with Peer. The original Peer and
  Transactor entries for all 47 are byte-identical.
- 3 sources / 5 classes are Transactor-only:
  - `datomic_jetty.impl.ProxyHandler`
  - `org.eclipse.jetty.servlets.EventSource` and `EventSource$Emitter`
  - `org.eclipse.jetty.servlets.EventSourceServlet` and
    `EventSourceServlet$EventSourceEmitter`

Shared files live only in the dedicated Transactor tree. The existing Peer
tree was neither overwritten nor made dependent on this work.

## Clean build and oracle boundary

`transactor/scripts/validate-java.sh` is the authoritative repository
procedure. It deliberately supersedes the early scratch-report compile command
that placed `datomic-transactor-pro-1.0.7277.jar` on `javac`'s classpath. That
exploratory command established recovery evidence but is not acceptable
candidate provenance.

The checked-in procedure enforces this sequence:

1. Verify all 46 recovered sources against the checked-in SHA-256 manifest.
2. Compile twice with only `transactor/src-java` as source and the ten JARs in
   `stage-1-java-compile-classpath.tsv` as `javac`'s classpath. Neither the Peer
   nor Transactor implementation JAR is on the compile classpath.
3. Compare the two emitted class manifests for deterministic equality.
4. Compile the verifier with ASM only.
5. After candidate compilation, pass the licensed Transactor JAR to the
   verifier as data. `VerifyJavaRecovery` streams the 52 named ZIP entries and
   never extracts them.
6. Compare exact class closure, class/field/method ABI, and normalized code.

The script rejects a licensed original located inside the repository and also
rejects a validation output directory inside the repository. Its verifier
runtime classpath contains only its generated tool classes and ASM; the
original JAR is a `ZipFile` argument, not executable classpath input.

The compile classpath was reduced by omission testing: removing any one of the
retained ten makes `javac` fail, while `artemis-core-client`, `jetty-http`, and
`jetty-io` proved unnecessary and were removed. The wildcard also would have
included `core2-1.0.140.jar` and unrelated Datomic AOT; none of it is present.
The retained, explicit dependencies are Clojure, Guava, Artemis commons,
Servlet API, Jetty continuation/server/util, Fressian, SLF4J API, and
`datomic-lucene-core`. The last is a pinned Datomic-owned compile API and still
needs an explicit recovery/retention decision in the broader dependency
closure. This Java gate does not claim that every compile dependency is
approved for the eventual clean runtime.

Reproduce with:

```bash
transactor/scripts/validate-java.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-transactor-java-validation
```

The script selects `/tmp/amazon-corretto-11.0.22.7.1/bin/javac` when present,
or accepts an explicit third argument / `DATOMIC_JAVA_JAVAC`. It refuses any
compiler/runtime other than Amazon Corretto 11.0.22.7.1. The official archive
used for this recovery has SHA-256
`f512bedb85adbef31c3823e219d9369e2bccb650575615478619b499f8e21117`.

## Normalization and byte identity

Raw classfile equality is not the primary correctness test: debug line tables,
source layout, frames, and compiler metadata can differ without changing ABI or
executable behavior. The verifier therefore has two explicit views:

- ABI: class hierarchy/access, inner/nest relationships, fields, method names,
  descriptors, signatures, and declared exceptions.
- Code: ordered JVM instructions, constants, variable slots, field/method
  operations, dynamic calls, labels, branches, switches, and exception tables;
  debug records, frames, and computed max stack/local values are excluded.

Twenty of the 52 freshly compiled classfiles are nevertheless raw-byte
identical to their originals. This is expected regenerated output from exact
source plus the exact compiler, not evidence of copied classfiles. The build
provenance and classpath boundary prove isolation; a blanket “candidate hash
must differ from every original hash” rule would incorrectly reject these 20
legitimate results.

## Recovery decisions and provenance

Eight initial code differences were decompiler source-shape artifacts repaired
using local-variable metadata and original instruction order:

1. `ActiveMQInputStream`: remove a decompiler-introduced unused assignment.
2. `QueryRequest`: restore `Map` and `String` local types.
3. `Util`: restore `List` and `Map` interface-typed locals.
4. `JavaByteUtil`: restore structured branches and declaration order.
5. `MurmurHash`: split a folded multiply/XOR expression.
6. `Shell`: restore a `Reader` local and original slot order.
7. `ClusterIndexInput`: restore local declaration order.
8. `HybridDirectory`: restore the `Set` interface local type.

`MersenneTwisterFast` is the BSD-noticed ECJ Version 13 implementation, with
the embedded notice retained. Its Datomic form changes the package and two
bounded-number exception messages. `EventSource.java` is source-identical to
the Apache-2.0 Jetty event-source source; the retained servlet version has three
identified Datomic changes (charset setter removal, no-cache header, and the
heartbeat colon). `ProxyHandler` remains conservatively classified as
Datomic-specific/unattributed. Original comments, whitespace, and parameter
names absent from debug metadata cannot be claimed exact; they do not affect
the validated ABI/code result.

## Checked-in evidence

| Evidence | Rows | SHA-256 |
|---|---:|---|
| `stage-1-java-compile-classpath.tsv` | 10 + header | `b99670e560b39157bbeceeb71f00b82d8cdf6815dc0c84dfe5c0c8ae01ee3f81` |
| `stage-1-java-source-manifest.sha256` | 46 | `f48edbd567b53316b6e20cd1b2bc889f71a9c45eee3a6576cacf454a20da7f5f` |
| `stage-1-java-class-list.txt` | 52 | `c5fe736882eed01ca09dfc9e7d7de6eb7999ae2cfa1431ff0e39f716702a8c2b` |
| `stage-1-java-code-relation.tsv` | 52 | `d76a019725d1077eebe3c2698436a91628589aa10b59c78e429eeb28725a8dcc` |
| `stage-1-java-validation-relation.tsv` | 52 + header | `13547dc0ea153fc6922e0bdfc292746de728355b9b63d93b7200ecc90a57fd1c` |
| `stage-1-java-validation-summary.tsv` | 10 + header | `ef3a9d35e97c94f85d46c6ac226b2324c9395921e208f802c82967ae8d81fee3` |

Pinned inputs:

- Transactor JAR:
  `d90819b57e2138085ddc26c93314f953c2cd776d1d1aa8b43a6bec1e475d3692`
- ASM 9.2:
  `b9d4fe4d71938df38839f0eca42aaaa64cf8b313d678da036f0cb3ca199b47f5`
- Corretto 11.0.22.7.1 archive:
  `f512bedb85adbef31c3823e219d9369e2bccb650575615478619b499f8e21117`

Canonical repository validation runs:

- outputs: `/tmp/datomic-transactor-java-repo-validation-v4` and
  `/tmp/datomic-transactor-java-repo-validation-v5`
- the complete `evidence/` trees are byte-identical
- repeat class-manifest hash (both builds):
  `9f5eb9a9c95b66f4094ce94f0302dbe4caae89cd196c296731c9096d56833250`
- generated evidence-manifest file hash:
  `970e0cb93f2c265b620793a5852eac44af9acd22b3900e45cde9f7fa59f8472f`
- result: 46 sources, 52 classes, 122 fields, 303 methods, 52/52 ABI
  matches, 52/52 normalized-code matches, zero differences.
