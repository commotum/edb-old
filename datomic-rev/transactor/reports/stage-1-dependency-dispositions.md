# Stage 1 dependency dispositions

Status: **all shipped dependency identities have an explicit Stage 1
disposition; later runtime minimization remains stage-specific**.

The Transactor distribution has 533 dependency JARs in addition to the Peer
and Transactor artifacts. `stage-1-dependency-dispositions.tsv` accounts for
every one of them in frozen distribution order:

| Stage 1 disposition | Count | Meaning |
| --- | ---: | --- |
| `RETAIN_HASH_PINNED_STAGE1` | 531 | Retain the exact shipped JAR for complete-corpus structural compilation and loading. |
| `SANITIZE_AND_REPLACE` | 1 | Replace `nano-impl-0.1.325.jar` with the deterministic JKS-free derivative. |
| `EXCLUDE_ORIGINAL_RECOVER_SOURCE` | 1 | Exclude `core2-1.0.140.jar`; supply its 25 required Peer-only namespaces from recovered Peer source. |

This is deliberately a whole-corpus Stage 1 decision, not a claim that all 531
retained libraries belong in the eventual PostgreSQL production runtime. The
full 247-namespace Transactor boundary includes optional and alternative
backend code, so the structural gate retains the frozen distribution closure
while rejecting original Peer, Transactor, and core2 AOT. The per-row
`later_scope` field is a coarse filename-based routing label for where
necessity should be reevaluated; it is not a proved dependency-usage or
ownership analysis. PostgreSQL remains primary; Cassandra, AWS/Dynamo, Hot
Rod, H2, optional cache, HTTP, and other lower-priority families receive no
semantic implementation work merely because their libraries are present for
structural loading.

Twenty-four distribution dependencies supply the exact sources for 87
recovered namespaces. The candidate keeps 23 originals and substitutes the
sanitized nano derivative for the twenty-fourth. Those artifacts contain no
colliding AOT initializer or owned class for the source-owned namespaces; the
structural scanner checks that independently. Retention therefore preserves
auxiliary classes and resources without falling back to source-owned AOT.

## Reproduce

From the repository root, use a new empty output directory:

```bash
transactor/scripts/build-dependency-dispositions.sh \
  /tmp/datomic-goal2-dependency-dispositions
```

The runner hash-checks the Stage 0 distribution/POM inventories, the clean
532-artifact candidate manifest, and both exact-source ownership manifests. It
then proves a one-to-one relation: 531 original hashes retained, one exact
sanitized nano hash substituted, one original core2 identity absent, and zero
unaccounted dependencies or candidate artifacts.

Independent runs `/tmp/datomic-goal2-dependency-dispositions-v6` and `-v7`
are byte-identical. Each generated manifest file has SHA-256
`73ac406d2ad07e481b60003fd7f1c3ee98f0b315121ae76886d1b42cbbd0d51b`.
The promoted compact files verify against
`stage-1-dependency-disposition-evidence.sha256`.
