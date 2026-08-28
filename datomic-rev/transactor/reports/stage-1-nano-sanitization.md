# Stage 1 `nano-impl` sanitization

Status: **content-sanitization pass; runtime integration not yet claimed**.

The frozen Datomic Pro 1.0.7277 distribution contains
`lib/nano-impl-0.1.325.jar` with SHA-256
`fbde8184da356bd3a9995a9f05f01493efe4baa87c4d48377cda3e53331807cd`.
Its 21 physical ZIP entries comprise eight directory entries, eleven retained
files, and two forbidden vendor JKS files. The exact ordered policy, including
every retained and removed content hash, is
`stage-1-nano-entry-policy.tsv`.

## Enforced boundary

`../scripts/sanitize-nano-impl.sh` refuses an input, Stage 0 baseline, policy,
validator, or packager whose expected SHA-256 differs. It then:

1. inspects the archive without defining or executing any archive class;
2. requires the exact 21 physical entries in original order and validates each
   file's byte count and SHA-256;
3. stages only the eleven allowlisted files, dropping directory entries and
   removing `nano_impl/transactor-key.jks` and
   `nano_impl/transactor-trust.jks` by exact content hash;
4. invokes the existing `scripts/DeterministicJar.java` twice from two
   independently prepared staging trees;
5. requires byte-identical builds and verifies exact names, order, content
   hashes, CRCs, STORED method, timestamps, timestamp extra data, archive size,
   and archive hash; and
6. reruns five negative cases and writes a SHA-256 manifest over the derivative
   and all generated evidence.

The workflow pins `TZ=UTC`. This is material: Java's ZIP writer derives the UT
timestamp extra field from the process timezone even though
`DeterministicJar.java` supplies a fixed `LocalDateTime`. Under UTC the expected
entry timestamp is `1980-01-01T00:00`, epoch milliseconds `315532800000`, and
extra bytes `555405000100a6ce12`.

## Verified result

The candidate derivative contains eleven file-only STORED entries: two classes
and nine resources, totaling 79,362 payload bytes. Its exact result is:

| Property | Result |
| --- | --- |
| JAR | `nano-impl-0.1.325-sanitized.jar` |
| Bytes | `81218` |
| SHA-256 | `08f9699d6e35b9e052ec85ee15e0896b3a685c74e6e226d8cd89d9c61dbf8d5f` |
| Forbidden content-hash hits | `0` |
| Independent internal builds | `2`, byte-identical |
| Independent full workflow runs | `2`, full output trees identical |
| Negative tests | `5/5` rejected as intended |

The negative cases alter the original archive, alter the policy, present the
licensed original as the derivative, alter retained content, and add an
unexpected derivative entry. Each must fail with its intended diagnostic.

Two complete runs remain outside the repository:

- `/tmp/datomic-goal2-nano-sanitization-20260827-c`
- `/tmp/datomic-goal2-nano-sanitization-20260827-d`

Both output manifests have SHA-256
`af6b5efdd9a3cdf16d1e1f960910bc1b72a686fecae26734bc4b65c6ba8408eb`.
`diff -qr` reports no difference between the output trees. No derivative JAR,
licensed JKS payload, or licensed original was added to this repository.

## Reproduce

From the repository root:

```bash
transactor/scripts/sanitize-nano-impl.sh \
  /home/jake/Developer/datomic/datomic-pro-1.0.7277 \
  /tmp/datomic-goal2-nano-sanitization-reproduction
```

The checked tool inputs for the verified run were:

| Input | SHA-256 |
| --- | --- |
| `transactor/scripts/sanitize-nano-impl.sh` | `813fcaef43a1cd8950350a9100ca9583efbd33b0bc25308569f1254fa4efc27e` |
| `transactor/tools/SanitizeNanoImpl.java` | `2d97c17887cefbbcb3b19b9d4e68e7607b3350e16a68ae1ed2a48263d87c55df` |
| `transactor/reports/stage-1-nano-entry-policy.tsv` | `ff8bffd9a2d653750308ab9455509f02a613d30adf5bd11ecae767fadaf4a5f1` |
| `scripts/DeterministicJar.java` | `0ccc5df5ea072a55739d61d300e175978f27a3e7db11f831f319bb1000baaef1` |

This result proves deterministic content removal and archive construction. It
does **not** prove that a recovered Transactor can run with this derivative;
that remains a later integration gate.
