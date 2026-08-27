# Patched AOT decompiler record

Upstream/base commit:
`51f222ed7db0049f27ebee3c3071672d516a6b5e`.

Final binary Git-diff SHA-256:
`820cea96f1a54f13c1f6424ee264e32d30895bd14b39f2e4123f7b17d7e5b983`.

The patch changes seven source files (1,131 insertions, 298 deletions). The
checked-in source is loaded ahead of the historical standalone JAR, so no
locally rebuilt decompiler binary is required.

## Bytecode and control flow

- Compute exact reachable instructions across conditional/unconditional jumps,
  table/lookup switches, and exception handlers.
- Retain abstract methods and safely handle methods without `Code` attributes.
- Correct stack handling for reachable versus dead `athrow` instructions,
  reset method-local AST state, and finalize methods whose only terminal is
  JVM `ATHROW`.
- Split compiler return spills and try ranges without merging unrelated state
  arms.
- Treat only BCEL `ClassFormatException` from referenced modern classes as
  “nested bytecode unavailable”; unrelated failures are no longer swallowed.

## Clojure structure

- Recover protocols, records, types, their methods, mutable fields, and
  self-references.
- Restore 23 non-protocol `definterface` forms from exact abstract descriptors.
- Reconstruct captured closure arguments by both field order and JVM slot,
  including wide primitive slots.
- Mark captured-field roots and alpha-rename an actual nested-reify receiver
  when its source name collides with an outer captured receiver.
- Preserve IOC state-machine arms, captured binding frames, throw paths, and
  channel operations.
- Repair namespace setup, private metadata, definition ordering, circular Vars,
  dynamic Vars, case constants, and generated definition metadata.
- Restore source-authored `defrecord` field spelling and field metadata from
  each generated class's exact static `getBasis` bytecode. All 25 record bases
  and all 85 fields now match the original names, order, and metadata.

## Types and Java interop

- Retain primitive, array, argument, return, and local CHECKCAST hints needed to
  select exact overloads.
- Retain `Reflector` argument arrays through repeated sugar passes instead of
  truncating arguments when synthetic AST nodes lack type vectors.
- Preserve a foreign same-class field receiver; only the real current receiver
  may collapse to a lexical `deftype` field.
- Emit primitive metadata together with field mutability and coerce every
  primitive `putfield` value, including numeric constants.
- Carry primitive function argument and return descriptors into source and
  preserve arity-vector return metadata through namespace elision and printing.
- Stabilize casts to generated reify classes by selecting the method-bearing
  superclass/interface that will survive recompilation.
- Recover proxy superclass/interfaces from generated proxy bytecode, exclude
  compiler-added `IProxy`, and preserve constructor arguments.
- Preserve object methods on records/types and splice an `IFn` `Object[]` tail
  only for the exact 20-fixed-arguments-plus-array JVM descriptor; an ordinary
  array-valued argument remains one argument.
- Fold boxed numeric `Character/valueOf` constants back to character literals.
- Preserve the complete `RT.keyword` expression in `KeywordLookupSite`
  recovery instead of discarding its namespace. This restores all 63
  namespaced sites in the peer artifact.

## Conservative source compaction

- Remove unsafe global rewrites for maps, sequences, vectors, temporary locals,
  destructuring, `doseq`, and generated `p__` bindings.
- Apply threading and conditional-threading rewrites only when reference counts
  prove the temporary can be eliminated safely.
- Retain switch-arm statements and collision/default semantics.
- Modernize pretty-print compatibility while preserving metadata on collection
  nodes used for primitive function returns.

## Final validation

- The patched tool namespace loads cleanly and `git diff --check` passes.
- Final `latest35` and `latest36` decompilations are byte-identical: 142
  successes, zero failures. Their sorted source manifest SHA-256 is
  `ec43cd7e963514d855b0b6080c485caa638858dced0a3e40d4fca0b6866531be`.
- No raw `deftype*`, `reify*`, `loop*`, `import*`, `.bindRoot`, `.setDynamic`,
  `invokeStatic`, `__init`, generated `$reify`, or generated `.proxy$`
  references remain in emitted source.
- All 142 emitted namespaces load in isolated source-only JVMs.
- All 142 runtime Var/authored-class surfaces match the original AOT artifact.
- Exact AOT ABI comparisons match all 65 primitive function methods, all 86
  types, all 25 records, and all 102 generated interfaces. The sole extra
  method is a JDK 21 synthetic `SequencedCollection` bridge.
- All 13 decompiler regressions pass, including namespaced keyword sites,
  character literals, record basis spelling/metadata, and terminal throws.
- Focused source-only behavior probes pass for reflective arguments,
  same-class foreign receivers, nested-reify outer receiver access, literal and
  lookup recovery, record keys, and `ExceptionInfo` throw behavior.
- The original and recovered implementations produce the exact same canonical
  result for the documented in-memory API workload (SHA-256
  `228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`).

These checks establish deterministic reconstruction and the enumerated
structural/behavioral properties for this Peer JAR. They do not recover the
unpublished source literally or exhaustively prove transaction, storage,
distributed-failure, security, or concurrency semantics.
