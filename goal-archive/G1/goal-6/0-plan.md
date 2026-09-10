# Goal 6 — Deterministic Controlled Programmability

## Objective

Add custom transaction invariants and local query transformations without
giving persisted behavior ambient authority or a path around the semantic
kernel. Preserve Datomic's database-function intent and db-before evaluation,
but replace JVM/Clojure code compilation with one explicit, versioned,
deterministic, resource-metered native program format. Programs must deploy
immutably through PostgreSQL, execute reproducibly across processes/restarts,
and produce ordinary transaction information or query values that pass the
same validators as hand-written input.

This goal realizes Stage 6 of `goal-0/0-plan.md`. It does not build the
production request service, leader election/fencing, backup, garbage
collection, or other Goal 7/8 operations.

## Constraints

- Rust throughout; PostgreSQL is the sole durable program registry. No generic
  storage/provider abstraction.
- Treat the transaction-function and query-function documentation as semantic
  authority and trace `1.0.7705` `datomic.function`, `datomic.extensions`,
  transaction expansion, query expression resolution, cancellation, and
  database-function lookup before fixing the native runtime.
- Preserve `[db-before, args] -> transaction data`, complete-transaction
  validation, query snapshot locality, deterministic errors, composition, and
  cancellation. Generated transaction forms never observe intermediate writes.
- Persist a compact versioned instruction/ABI format with canonical encoding,
  content hashes, static validation, explicit typed inputs/outputs, bounded
  stack/control flow, deterministic host calls, and metered execution.
- No filesystem, network, process, environment, clock, randomness, threads,
  dynamic library loading, SQL, or unrestricted host-language calls from a
  persisted program.
- A small purpose-built bytecode/DSL is acceptable as the "comparably
  constrained DSL" named by Goal 0 and is preferred if it makes determinism and
  auditing clearer than embedding a large WASM runtime. Record this decision
  and its expressive limits explicitly.
- Keep process-local Rust extensions separate and visibly non-persisted. They
  may never be required to replay committed history.
- Deployment and activation are immutable/versioned and race-safe. A request
  identifies the exact program hash/version it executed so retry/restart cannot
  silently change behavior.
- Every emitted operation, predicate decision, and query result remains subject
  to existing schema, transaction, query, and resource checks.

## Known context

- Goal 2 already has `TxFunctions`, recursive `TxCall` normalization, original
  db-before passing, 32-call-depth and 100,000-primitive expansion limits, and
  process-local schema predicates.
- Goal 5 has a closed pure built-in query surface, immutable query sources,
  cancellation/work controls, and typed values. Extension hooks should compose
  with that evaluator without allowing arbitrary Rust callbacks in persisted
  state.
- Goal 3's canonical envelope records resulting datoms, not the function call.
  Recovery therefore remains independent of code availability; request-time
  retry identity must additionally bind the chosen program version.

## Stages

### 1. Recovered function contract and runtime choice

**Status:** Complete. `RUNTIME_CONTRACT.md` maps recovered lazy compiled
functions/extensions to immutable hash-resolved native programs, fixes the
db-before/output-validation and retry boundaries, and selects a straight-line
metered DSL whose absent instructions make ambient effects impossible.

**Outcome:** Evaluation timing, authorities, program kinds, ABI types, limits,
deployment identity, and deviations from JVM database functions are explicit.

**Focus:** Documentation/source trace; db-before/db-after rules; transaction
composition; query extensions; failure/cancel behavior; deterministic host
surface; bytecode-versus-WASM decision and threat model.

**Completion signal:** A concise contract states what every program can
observe, emit, and never access, how versions are selected, and how all outputs
re-enter ordinary validation.

### 2. Canonical program format and metered interpreter

**Status:** Complete. `src/program.rs` implements the statically validated
three-kind instruction ABI and deterministic interpreter; durable encoding
kind 6 includes the kind, arity, constants and instructions in its checksum and
content hash. `tests/program_runtime.rs` fixes a golden hash and covers
round-trip/corruption, db-before reads, output revalidation, typed results,
malformed stacks, fuel, stack, cancellation, and argument type failures.

**Outcome:** Rust can encode, validate, hash, decode, and execute typed native
programs deterministically under explicit fuel/stack/output limits.

**Focus:** Program header/version/kind/signature; instruction set; constants;
control flow; db-before read host calls; arithmetic/comparison; transaction-form
emission and query return values; canonical validation; cancellation.

**Completion signal:** Golden formats and malformed/adversarial programs are
covered; same program/context always produces identical output; fuel, stack,
depth, type, cancellation, and output limits fail with stable structured errors.

### 3. PostgreSQL deployment and exact resolution

**Status:** Complete. Migration 3 installs global immutable content rows plus
database-local immutable name/version bindings and conditional active heads.
`PostgresStore` provides idempotent deploy, activation CAS with identical-target
retry, exact-hash and active resolution, canonical metadata/hash verification,
and a bounded hash-only LRU. Real PostgreSQL tests cover restart, immutability,
cache eviction, a two-writer activation race, and fail-closed blob corruption.

**Outcome:** Immutable content-addressed program versions and explicit active
name mappings survive restart and concurrent deployment without ambiguity.

**Focus:** One migration; kind/name/version/hash/signature/payload constraints;
immutable rows; conditional activation; idempotent deployment; exact-hash
lookup; checksums; cache safety; corruption and stale activation.

**Completion signal:** Real PostgreSQL tests prove immutable/idempotent deploy,
activation CAS, concurrent version races, restart resolution, bounded immutable
cache behavior, and fail-closed corruption.

### 4. Persisted transaction functions and predicates

**Status:** Complete. `PostgresStore::{transact_program,
transact_program_hash,transact_programs}` resolve immutable bytecode, bind exact
function and predicate hashes into the request digest, interpret only after the
database head is locked, combine batch outputs against one db-before, and pass
all emitted `TxOp`s through the kernel. `transact_with_persisted_predicates`
enforces active one-argument predicate programs for ordinary writes. Real
PostgreSQL tests cover same-db-before composition, rejected output atomicity,
predicate enforcement, exact-hash replay across activation, conflict on a
changed active version, and recovery while program bytes are unavailable.

**Outcome:** Stored programs can expand transaction calls and enforce schema
predicates using only db-before, while the kernel remains the sole authority on
result validity.

**Focus:** Resolve exact active/hash-bound code under the write lock; bind args;
read db-before values; compose calls safely; emit native `TxForm`/`TxOp` data;
schema predicate invocation; request digest/version binding; errors and limits.

**Completion signal:** End-to-end PostgreSQL tests show deterministic expansion,
same-db-before nested calls, invalid emitted data rejected atomically, predicate
enforcement, retry version stability, and restart independence of committed
history.

### 5. Local Rust and persisted query extensions

**Status:** Complete. `QueryExtensions` is an explicit per-request registry;
local callbacks are panic-contained and never serialized, while persisted
query programs are registered with an exact hash and run on the clause's
immutable source under parent cancellation/work/result bounds. Query bytecode
can return a scalar/tuple row or emit bounded relation rows, and explain access
records local identity or the complete persisted hash. Fixtures cover all
shapes, local errors/panic/output bounds, restart resolution, exact hash
visibility, cancellation, and an old snapshot queried concurrently with a
durable advance.

**Outcome:** Applications can register process-local pure Rust query functions
or invoke constrained persisted scalar/relation programs without weakening
snapshot locality or query controls.

**Focus:** Separate registries and names; typed argument/result binding;
immutable database host reads; query fuel charged to the parent request;
relation output bounds; panic containment for local callbacks; exact version
selection and explain visibility.

**Completion signal:** Query fixtures cover scalar/tuple/relation extension
results, snapshot consistency, local-versus-persisted resolution, cancellation,
panics/errors, output limits, and concurrency while peers advance.

### 6. Reproducibility and security verification

**Status:** Complete. `SECURITY_AND_REPRODUCIBILITY.md` audits the closed host
surface, output authority, identity/recovery chain, resource envelope, and
intentional expressive limits. Canonical program-output encoding and an
independent-decode fixture prove byte-identical observations. The complete
serial real-PostgreSQL suite passes (88 tests: 87 passed and the subprocess
worker intentionally ignored), including actual server restart, process death,
publication faults, activation races, corruption, old-snapshot concurrency,
and all prior semantic/query/peer coverage; formatting and strict Clippy also
pass.

**Outcome:** The programmability boundary remains deterministic, bounded, and
non-authoritative under restart, concurrency, malformed code, and hostile work.

**Focus:** Cross-instance byte/output equality; PostgreSQL restart; concurrent
activation/execution; resource exhaustion; forbidden-effect absence; corrupt
code; generated transaction validation; full prior suite and performance cost.

**Completion signal:** Identical program hash/context yields byte-identical
results across independent runtimes and restart; every limit and corruption
path fails closed; invalid output cannot commit; and all format/lint/test suites
pass against real PostgreSQL.

## Programmability exit condition

**Status:** Complete. The six stages above establish the useful deterministic
transaction/predicate/query surface, immutable PostgreSQL lifecycle, exact
retry identity, hard authority/resource boundary, and integrated regression
evidence required by this condition.

Goal 6 is complete when versioned persisted programs and explicitly local Rust
extensions provide useful transaction predicates/expansion and query behavior,
execute deterministically on immutable inputs under hard limits, survive
deployment races and restart, and can neither perform ambient effects nor
bypass kernel/query validation. Unsupported host-language behavior must be
explicit; Goal 7 service availability and Goal 8 lifecycle operations remain
unclaimed.
