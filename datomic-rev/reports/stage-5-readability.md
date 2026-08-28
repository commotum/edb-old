# Stage 5 readability and navigation

Date: `2026-08-27` (`America/Los_Angeles`).

Status: **PASS**.

Stage 5 makes the final recovered source easier to enter and traverse without
performing a broad source rewrite. It adds a conservative
[source navigation guide](source-guide.md) and refreshes the checked-in
[semantic index](source-index/README.md) after the Stage 4 repairs. It does not
rename recovered namespaces, Vars, generated locals, or types; reformat the
recovered corpus; infer original comments or intent; or claim that a static
index is a complete runtime call graph.

## Navigation deliverable

The source guide records:

- the boundary between the preferred 142-file Clojure reading surface, the 43
  canonical handwritten Java build inputs, the larger CFR evidence tree, and
  exact resources;
- subsystem entry points for the public API, connections, databases,
  transactions, query/Datalog, indexes, storage/backup, concurrency,
  monitoring, and backend selection;
- narrow connect, transaction, query, index-adoption, and backup/restore
  reading routes grounded in the recovered bodies and checked index; and
- generated-source hazards, index limitations, and the evidence ladder that
  constrains behavioral claims.

This is navigation work only. In particular, apparently redundant casts,
type hints, bindings, and generated forms remain implementation material when
they select bytecode shape or runtime dispatch. The guide directs readers to
the [Stage 4 evidence](stage-4-validation.md) rather than presenting warning
removal as stylistic cleanup.

## Regenerated semantic index

The deterministic analyzer was rerun against the final recovered source tree
and exact Peer JAR. The refreshed index contains:

| Property | Final value |
|---|---:|
| Recovered namespaces | 142 |
| Definitions | 2,906 |
| Public definitions | 2,631 |
| Private definitions | 275 |
| `corpus.edn` SHA-256 | `88ac1c629a205cba92e00b1ec7b25327575ef24a89d57e83912a6d14cd366bcb` |
| `source-guide.md` SHA-256 | `72d04f92ab3a160b04d84e1b79cb07b9d93d830b1675dcf6dc89252b7944ac49` |

Among the analyzer's generated data files, only `corpus.edn`,
`namespaces.tsv`, and `vars.tsv` changed from the pre-Stage-4 checked-in index.
That is the expected layer for source hashes, line locations, definitions, and
their recovered forms. The relationship tables `calls.tsv`, `references.tsv`,
and `dependencies.tsv` remained byte-identical. The other generated tables
(`keywords.tsv`, `strings.tsv`, `gaps.tsv`, and `unmapped-classes.tsv`) also
remained byte-identical. The index README changed separately to record the new
`corpus.edn` hash.

The index remains deliberately conservative. It resolves recovered Vars from
definitions, aliases, and explicit refers; it does not fully model Java
interop, protocols, multimethods, dynamically resolved Vars, or runtime
dispatch. Zero explicit decompiler-gap markers is not an independent semantic
equivalence result.

## TSV byte preservation

Generated TSV rows use trailing tab characters to represent empty final
fields. The root `.gitattributes` entry

```gitattributes
datomic-rev/reports/source-index/*.tsv whitespace=-blank-at-eol
```

suppresses Git's blank-at-end-of-line warning for those TSV files only. It
does not trim, normalize, or otherwise rewrite their bytes. This preserves the
analyzer's deterministic empty-field encoding while leaving ordinary
whitespace checks active elsewhere.

## Accumulated validation status

Stage 5 adds no substitute for the accumulated runtime gates. Their status at
the time this report was drafted is:

| Gate | Post-Stage-4 status |
|---|---|
| [Stage 1](stage-1-validation.md) | **PASS** — two clean builds and the complete packaged-artifact gate produced canonical artifact SHA-256 `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`. |
| [Stage 2](stage-2-validation.md) | **PASS** — local ephemeral run `/tmp/datomic-stage2-adversarial-final-v2`; final artifact SHA `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`; 118-file evidence-manifest SHA `25e5f821a84bbde27cb85a985a2728cffbe73509dfd2f7b70c115ed6ca2cf721`; services stopped. |
| [Stage 3](stage-3-validation.md) | **PASS** — local ephemeral run `/tmp/datomic-stage3-adversarial-final-v2`; final artifact SHA `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`; 88-file evidence-manifest SHA `8f69d3fac2ab0d7d59f449db2c2e3489b292d24a123522a023754a94b01984f6`; services stopped; the manifest includes the retained post-preflight Hot Rod stub recheck. |

The Stage 2 and Stage 3 reports remain the authorities for their bounded
PostgreSQL, recovery, concurrency, and transport claims. Their final reruns
close the accumulated-validation condition for this readability/navigation
change; Stage 5 itself adds no new runtime claim.

## Claim boundary

The resulting source surface is more navigable, not more original. The guide
and index do not recover unpublished comments, names, formatting, or design
intent; do not establish a recovered transactor; and do not broaden backend or
schedule coverage. Runtime and equivalence claims remain limited to the exact
artifact, oracle comparisons, and bounded gates named in Stages 1 through 4.
