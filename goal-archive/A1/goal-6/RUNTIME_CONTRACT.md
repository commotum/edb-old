# Goal 6 controlled runtime contract

## Recovered semantics

The normative transaction shape is the documented pure
`[db-before, args] -> tx-data`. Calls do not see the transaction's other input,
do not see another call's output, and never observe an intermediate database.
All returned information is combined and passed through the ordinary kernel in
one unordered assessment. Cancellation aborts that whole assessment. Committed
history stores material datoms and therefore never needs code execution during
recovery.

The recovered `datomic.function` `Function` object stores language, params,
code, imports and requires, compiles lazily, then caches a JVM callable. The
native mapping stores a validated `Program`, resolves it by immutable content
hash, decodes/caches it once, and interprets a compact instruction vector.
Recovered classpath/query extensions map separately to explicitly process-local
Rust callbacks; they are never serialized or required for recovery.

## Runtime choice

Atomic uses Goal 0's permitted "comparably constrained DSL" rather than WASM.
Programs are straight-line, typed stack bytecode with at most ten arguments.
The instruction set supplies constants, arguments, stack manipulation,
deterministic comparison/arithmetic/boolean operations, bounded db-before value
reads, ordinary transaction-op emission, and typed return. There are no jumps,
loops, recursion, allocation primitives, foreign calls, memory ABI, or ambient
imports. Useful repetition is represented by multiple declarative calls in the
outer transaction/query.

This is intentionally less expressive than Clojure/JVM code and general WASM,
but materially easier to statically audit and meter. It avoids a large runtime,
JIT/version drift, and host-import capability mistakes while meeting current
invariant and derived-value needs. A future WASM ABI would be a new program
kind/version, not a silent reinterpretation.

## Program identity and kinds

- Canonical versioned bytes determine a SHA-256 `ProgramHash`. Decoding
  re-encodes and rejects noncanonical bytes.
- Kinds are transaction function, attribute predicate, and query function.
  Arity and kind are part of the hash. A program may only return its kind's
  output.
- PostgreSQL program blobs and `(database, name, version, hash)` bindings are
  immutable. Activation is a conditional monotonic head update. Execution can
  name an exact hash; retry identity includes that hash.
- Caches key only by hash and admit bytes after hash, version, canonical and
  static validation. Eviction changes only decode work.

## Authority and host calls

- Programs receive copied `Value`/entity arguments and an immutable
  `&Database` db-before/query snapshot.
- Database reads are current-value lookups for a constant attribute after an
  entity id is popped. Reads cannot enumerate PostgreSQL, mutate state, or
  acquire a newer database.
- Emitted adds/retracts are ordinary `TxOp` values. Tempid allocation,
  identity, schema type/cardinality/uniqueness, CAS, predicates, and all other
  transaction rules remain exclusively in `Database::with`.
- Query programs return scalar/tuple/relation values into the existing binding
  path and inherit parent cancellation/work/output limits.
- Attribute predicate programs return exactly one boolean. They cannot emit
  data. Entity/db-after predicates remain unsupported until the kernel has an
  entity-spec semantic contract.

## Limits and failures

Every instruction and database value examined consumes fuel. Stack depth,
instruction count, emitted operations/result values, and call nesting have hard
limits. A caller-supplied atomic cancellation flag is checked between
instructions and database reads. Exhaustion is `Busy`, cancellation is
`Interrupted`, bad arguments/program types are `Incorrect`, corrupt persisted
bytes are `Fault`, and an explicit program rejection preserves its chosen
incorrect/conflict category.

There is no filesystem, network, SQL, environment, process, clock, randomness,
thread, dynamic code, host pointer, or unsafe-memory instruction. Consequently
forbidden effects are absent by construction rather than detected at runtime.

## Deployment and execution races

Activation never rewrites a version. Concurrent activators with the same
expected version have one winner unless they deploy identical bytes, in which
case retry resolves idempotently. Transaction execution names the exact active
hash observed for the request and binds it into idempotency; activation after
that observation cannot change that attempt. A stale expected database basis
still fails normally, requiring the caller to choose whether to retry and
therefore re-observe db-before/program activation.
