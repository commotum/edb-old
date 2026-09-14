# Decompiler comparison

Input: Datomic peer 1.0.7277
(`cb55c9d01e155f9965e3332cd8ef70be42cdefacb50c072076e9ecf22b9a23ba`).

| Artifact | Surface | Structural/compile result | Role |
|---|---:|---:|---|
| CFR 0.152 | 5,513 Java files | 205 unstructured files; 2,488 historical compiler errors | low-level evidence |
| Vineflower 1.12.0 | 5,513 Java files | 41 structural failures; 421 historical compiler errors | independent corroboration |
| Recovered Clojure | 142 namespaces | 142 reader PASS; 142 source-only load PASS | primary Clojure recovery |
| Handwritten Java subset | 43 sources | 43 compile PASS -> 47 classes | Java rebuild surface |
| Original/recovered API probe | one canonical in-memory workload | byte-for-byte result match | behavioral parity check |

The general-purpose Java trees flatten 5,470 Clojure AOT classes into synthetic
classes. Their compiler-error counts therefore do not measure the quality of
the recovered Clojure source. `src-clj/` is the primary rebuild and analysis
surface; CFR/Vineflower remain useful for checking exact low-level control flow.

## CFR findings

- The original invocation lacked the distribution dependency classpath.
- Unstructured methods contain pseudo-gotos and failed stack merges.
- Demunged Clojure locals can be Java keywords or otherwise illegal names.
- The compiler's namespace, Var, protocol, closure, and IOC structure is lost.

The 43 genuinely handwritten Java sources were separated by classfile
provenance. After two explicit `String[]` casts, that complete subset compiles.

## Vineflower caution

Vineflower structures substantially more methods, but lower error counts are
not semantic proof. A direct check in `datomic.log/tail-pod-key` found that the
Vineflower view assigned an input local to `null` before use, while bytecode,
CFR's unstructured form, and recovered Clojure preserved the input. Vineflower
is retained as corroboration rather than promoted as canonical source.

## Recommended evidence order

1. Recovered Clojure namespace/function.
2. Original bytecode and signatures.
3. Deterministic bytecode inventory/call tables.
4. CFR for readable low-level evidence.
5. Vineflower as an independent control-flow interpretation.
6. Runtime observation in a disposable licensed Datomic environment.

See `recovery-validation.md` for the final closure tests and accuracy boundary.
