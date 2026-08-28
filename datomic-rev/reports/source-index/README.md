# Recovered-source semantic index

Generated deterministically by `scripts/analyze_corpus.clj` from the final
142-namespace source tree and the exact peer JAR.

| File | Contents |
|---|---|
| `corpus.edn` | complete machine-readable index and provenance |
| `namespaces.tsv` | file, lines, definition visibility, requires, imports |
| `vars.tsv` | definitions, kinds, visibility, arglists, docs |
| `calls.tsv` | resolved recovered-Var calls and occurrence counts |
| `references.tsv` | resolved qualified-Var references and counts |
| `dependencies.tsv` | namespace require/call/reference edges |
| `keywords.tsv` | keyword totals and namespaces |
| `strings.tsv` | heuristic SQL/URI/configuration strings |
| `gaps.tsv` | explicit decompiler failure markers; header only |
| `unmapped-classes.tsv` | the 47 handwritten Java classes |

Final summary: 142 namespaces; 2,906 definitions (2,631 public, 275 private);
3,533 call rows; 2,798 reference rows; 1,722 keywords; 273 interesting
strings; zero explicit gaps.

`corpus.edn` SHA-256:
`88ac1c629a205cba92e00b1ec7b25327575ef24a89d57e83912a6d14cd366bcb`.

Call/reference resolution is intentionally conservative: it indexes Vars that
can be resolved from recovered definitions, namespace aliases, and explicit
refers. It is not a whole-program dynamic call graph. String classification is
heuristic. A zero gap count means no `BROKEN DECOMP` marker was found; it is
not an independent semantic-equivalence proof.
