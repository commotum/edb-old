# Stage 1 candidate dependency isolation

Status: **bounded stable-input dependency-content pass; complete candidate
runtime not yet claimed**.

The frozen distribution contains 533 dependency JARs. The candidate dependency
boundary excludes the original `core2-1.0.140.jar`, retains 531 shipped JARs,
and replaces the original JKS-bearing `nano-impl-0.1.325.jar` with the
deterministic sanitized derivative documented in
`stage-1-nano-sanitization.md`. The resulting ordered manifest contains 532
JARs and is recorded in `stage-1-candidate-dependencies.tsv`.

## Exhaustive content result

`../scripts/audit-candidate-dependencies.sh` hash-verifies the Stage 0
inventory, every top-level JAR, the sanitized derivative, the scanner, and the
deterministic test packager. The scanner does not define any archive class. It
streams every non-directory direct entry, hashes every complete entry payload,
and recursively scans nested ordinary ZIP payloads discovered by their byte
signature independently of filename.

Nested payloads must be single-disk, non-ZIP64 ZIPs with a consistent central
directory and end record, no archive comment, and no bytes left outside the
declared envelope. An archive-named payload without ZIP magic is also a hard
violation. Recursion is bounded to four levels and 64 MiB per nested archive;
exceeding either bound is a violation rather than an uninspected exception.

Two independent complete runs remain outside the repository:

- `/tmp/datomic-goal2-candidate-dependency-audit-v4-a`
- `/tmp/datomic-goal2-candidate-dependency-audit-v4-b`

Their output trees are byte-identical. Each output manifest has SHA-256
`b875de172d94d1660201b4617233bcfab46f369bef2421b4fd29883fd502d3d7`
and verifies every generated artifact.

| Property | Result |
| --- | ---: |
| Ordered candidate dependency JARs | 532 |
| Retained distribution JARs | 531 |
| Top-level JAR bytes | 387,428,686 |
| Direct non-directory archive entries | 185,393 |
| Direct uncompressed payload bytes | 2,312,738,044 |
| Recursively inspected nested entries | 95 |
| Nested uncompressed payload bytes | 649,478 |
| Forbidden content-hash hits | 0 |
| Scan violations | 0 |
| Negative controls | 10/10 pass |

The sole signature-discovered nested archive in the exact candidate set is
`org/h2/util/data.zip` inside `h2-2.1.214.jar`; all 95 of its entries are
content-scanned.

The five-rule deny policy covers the exact complete Peer, Transactor, and
`core2` artifact hashes plus the two vendor JKS payload hashes. It is applied
to top-level files, direct archive entries, recursively discovered archive
entries, and files in directory classpath elements. Individual original
classfile hashes are deliberately not denied: exact recompilation from
recovered source can legitimately reproduce a class byte for byte.

## Negative boundary

Ten negative controls were rejected for their intended reason:

1. the original `nano-impl` yields exactly the two vendor JKS content hits;
2. a renamed Peer artifact is found by its whole-file hash;
3. a renamed Transactor artifact is found by its whole-file hash;
4. a renamed `core2` artifact is found by its whole-file hash;
5. a complete Peer artifact stored under a non-JAR entry name is found as a
   direct entry payload;
6. a complete Peer artifact inside an extensionless nested ZIP is found as a
   recursive entry payload;
7. an empty-ZIP end record followed by a complete Peer artifact is rejected as
   an inconsistent/unparsed ZIP envelope;
8. a plain-text payload named `broken.zip` is rejected for lacking ZIP magic;
9. a top-level expected-hash mismatch is rejected; and
10. a symbolic-link classpath element is rejected.

The promoted compact evidence is bound by
`stage-1-candidate-dependency-evidence.sha256`, whose own SHA-256 is
`5b714d5eed1e084a592202095eca8f9e5b8b00c3f7523d7e60edd942ee166684`.
The verified scanner and wrapper source hashes are respectively
`0c2fc038183997e900e27b228aa427aecf881779d1237005e2ef7eb1332948b8`
and `ead3b7aded6b5ad5af35ea9ae8e99d8620f599b0c93630d0bf02e263e860e0ec`.

## Stability and scope boundary

For each top-level file the scanner hashes before inspection and rehashes after
inspection. The supported wrapper canonicalizes root arguments, rejects direct
symlink arguments, and the scanner rejects symlink components in classpath
elements. These checks detect ordinary input replacement during a scan, but
they are not an atomic filesystem snapshot: a deliberately timed ABA swap
could evade two equal endpoint hashes. This evidence therefore assumes the
frozen inputs remain stable and are not concurrently mutated during the run.

The deny policy matches exact complete files and logical entry payloads. It is
not a general substring search across arbitrary archive metadata, non-ZIP
container formats, or deliberately fragmented encodings. It also does not yet
add or load recovered Transactor/Peer-core2 candidate classes, reconstruct
root resources such as `data_readers.clj`, prove that sanitized nano works at
runtime, or decide the minimal production classpath for later subsystems. The
whole-corpus Stage 1 keep/replace/exclude decision for every shipped dependency
is now recorded separately in `stage-1-dependency-dispositions.md`; structural
runtime integration remains an independent gate.

## Reproduce

```bash
transactor/scripts/sanitize-nano-impl.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-nano-sanitized

transactor/scripts/audit-candidate-dependencies.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-nano-sanitized/nano-impl-0.1.325-sanitized.jar \
  /tmp/datomic-goal2-candidate-dependency-audit
```
