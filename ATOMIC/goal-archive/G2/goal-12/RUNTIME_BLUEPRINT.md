# Goal 12 Stage 4 runtime blueprint

## Verdict

The current direction has two good native ideas: immutable content-addressed program blobs with a bounded compiled-program cache, and a deterministic Rust runtime instead of an embedded JVM language. Two implementation choices are not faithful enough to use as the Stage 4 contract:

1. A mutable `atomic_active_programs` row is a place outside the database value. In Datomic, a database function is ordinary temporal information: an entity (normally named by `:db/ident`) has a cardinality-one `:db/fn` value. Function selection therefore participates in transactions, history, `as-of`, recovery, and db-before semantics.
2. The current straight-line scalar VM is a false semantic limitation. The documented function example branches over a map and iterates a collection, functions can return nested function calls, and 1.0.7705 deliberately exposes `d/q` inside compiled database functions. Exact Clojure/Java compatibility is out of scope; meaningful conditional, collection, query, and expansion capabilities are not.

The smallest faithful design is consequently **content-addressed code, bound by ordinary temporal datoms, evaluated as a pure expansion against one immutable database value**. Hashes are an excellent Rust implementation identity. They are not a substitute for the database's logical identity and time model.

## Source contract

These are required semantics, not optional compatibility work.

### Transaction functions

- A transaction function is pure `[db-before, args] -> tx-data`; every initial and recursively returned call receives the same db-before. It cannot see sibling input, another function's output, or a partially applied transaction (`datomic_pro_docs/04_transactions/04_transaction_functions.md:13-25,152-160`; `datomic_pro_docs/04_transactions/01_transaction_model.md:46-50`).
- Its result is valid transaction data, possibly empty, and may itself contain more transaction functions. The result joins the one unordered information set before assessment (`04_transaction_functions.md:68-76,152-160`; `02_transaction_data.md:54-65,203-213`).
- 1.0.7705 implements exactly this: `ProcessExpander.inject` resolves and calls the function with its captured `db`, then recursively sends every returned element through `ProcessInpoint` (`1.0.7705/transactor/src-clj/datomic/db.clj:7491-7550`). `with-tx` performs expansion before applying the completed information set (`db.clj:7892-7925`). Recursion/nesting is therefore real; sequential visibility is not.
- A qualified Symbol denotes a classpath function while a database id/ident denotes a database function (`db.clj:6901-6909`). Rust should retain this distinction with typed references rather than a `String`.
- The documented database function accepts up to ten arguments, is lazily compiled, and thereafter cached (`04_transaction_functions.md:49-66,208-216`; `1.0.7705/transactor/src-clj/datomic/function.clj:56-99,145-249`). We need not reproduce JVM `FnN` classes, but validation and invocation need a deliberate, documented arity convention.

### Predicates and entity specs

- Attribute predicates are qualified symbols called on each surviving asserted value. **Only the boolean value `true` succeeds**; every other returned value fails and is reported as the actual predicate result. Changes to predicate schema become effective on the following transaction and do not retroactively validate stored values (`datomic_pro_docs/03_schema/00_schema_data_reference.md:509-557`; `db.clj:2942-2996,3035-3065,3688-3705`). The 1.0.7705 pipeline applies them to assessed/deduplicated assertions (`db.clj:4430-4462,7782-7855`), so a redundant raw reassertion must not create a new rejection.
- An entity spec is ordinary data containing required attributes and/or entity predicate symbols. `:db/ensure` is an explicit, virtual assertion: it triggers validation but is never stored (`00_schema_data_reference.md:559-619`; `db.clj:7037-7041,7867-7879`). Specs are not automatically applied to every entity.
- The spec definition and active predicate bindings come from db-before, while required-attribute checks and entity predicates inspect the proposed entity in db-after. Entity predicates receive `[db-after, eid]`, may read other entities, and only exact `true` succeeds; failure retains the actual returned value (`04_transaction_functions.md:29-41`; `db.clj:7703-7769`). The broad wording at `00_schema_data_reference.md:591` says “db-before”, but the transaction-function table and executable 1.0.7705 path are more specific and establish db-after for entity predicates.
- Any function or predicate error aborts the entire pure transition. A panic must not escape the Rust runtime boundary or publish a partial value.

### Deployment, identity, and activation

- A database function is deployed by an ordinary transaction which asserts a function value at `:db/fn`, normally on an entity with `:db/ident` (`04_transaction_functions.md:208-216`). 1.0.7705 bootstraps `:db.type/fn` at id 26 and cardinality-one `:db/fn` at id 52 (`db.clj:7983-8055`), and `Db.getFn` resolves the entity and reads `:db/fn` from that immutable `Db` (`db.clj:5017-5034`). Function definitions themselves serialize as values (`1.0.7705/transactor/src-clj/datomic/fressian.clj:345-380`).
- Therefore “active version” is the function value asserted in the invocation's db-before. Replacing it is a normal logged transaction. The replacement cannot be invoked by another form in that same transaction, an old `as-of` database continues to select the old value, and history explains every selection.
- The content hash is a justified native strengthening for verification, deduplication, cache keys, and recovery. A separate mutable name/version pointer is not source-backed and must not participate in semantic resolution. Human version rows may remain operator metadata only. The selected hash is already derivable from the binding in the assessed db-before; a separate durable `(basis t, binding entity, program hash)` record is not a Datomic requirement and is not part of this milestone.
- Uploading an immutable blob may be a privileged preparatory operation, but it does not activate anything. A transaction asserting the corresponding function value is activation. Validate that the referenced blob exists, hashes correctly, has supported ABI/kind, and can decode before root publication; an orphan blob after a failed publication is harmless. Blobs reachable from current values, history, retained snapshots, backups, or receipts cannot be collected.

Source Datomic resolves attribute/entity predicate Symbols from the classpath (`db.clj:2942-2996,7703-7717`), not from database functions. The closest Rust analogue is a process-local, identically configured native registry. If Atomic supports **persisted predicate symbols**—a useful native replacement for classpath deployment—it must be explicitly identified as an extension and remain temporal:

1. Preserve the qualified Symbol in schema.
2. Canonically map `ns/name` to the database ident `:ns/name` (or store an equivalent explicit Symbol-to-entity binding as ordinary datoms).
3. From the transaction's db-before, resolve that ident entity and read its cardinality-one `db/fn` program hash; check the required attribute- or entity-predicate role.
4. If process-local fallback is retained for source-style classpath predicates, define one deterministic precedence rule and reject ambiguous dual bindings. Never consult `atomic_active_programs`.

This gives predicate installation/replacement the same history and “effective next transaction” behavior as the schema which names it. It also makes restart/failover resolution reproducible. A SQL symbol-to-hash side table, even if immutable blobs sit behind it, would recreate the same non-temporal error as the current active-program pointer.

### Cancellation and resource behavior

- `d/cancel` accepts only `:cognitect.anomalies/incorrect` or `:cognitect.anomalies/conflict`, with optional message and qualified serializable details; it may be called by transaction functions and both predicate kinds (`04_transaction_functions.md:162-173`; `datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:111-133`; `1.0.7705/transactor/src-clj/datomic/api.clj:571-595`). `Forbidden` is not a valid program cancellation category.
- Cancellation and any runtime failure leave db-before unchanged (`1.0.7705/transactor/src-clj/datomic/update.clj:1085-1105`). Predicate failure should include entity/attribute/value, predicate identity, and its actual non-true result.
- Datomic runs this arbitrary code in the serialized transaction pipeline and warns that slow work or allocation blocks following transactions (`04_transaction_functions.md:43-47`; `datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:64-72`). 1.0.7705 does not provide VM fuel, stack, or output quotas. Such quotas are a sound native safety strengthening, not a recovered semantic requirement.
- Quotas must cover the **whole transaction**, not reset for each function/predicate. One mutable execution budget must cover initial and nested calls, predicate invocations, expansion, query scans/intermediates, collection iteration, allocated runtime values, and emitted forms/bytes. Charge before materializing large vectors. Keep distinct limits for form count, encoded bytes, heap/value bytes, call depth, operand stack, query work/results, and total fuel; one `max_output` number must not ambiguously mean both count and bytes. External deadlines/operator cancellation are interruption/busy failures, distinct from user `cancel`.

## Smallest sufficient idiomatic Rust design

No general storage layer, dynamic Rust loading, JVM emulator, or Wasm platform is needed for this milestone.

1. **Temporal function value.** Represent `db.type/fn`/`db/fn` in the kernel. A function value contains or references a validated 32-byte `ProgramHash`; PostgreSQL stores canonical immutable blobs keyed by that hash. Database values contain the binding, so `as-of` and recovery naturally select the correct blob.
2. **Typed callable identity.** Replace untyped call names with `CallableRef::{Database(EntityRef), Local(Symbol), ExactHash(ProgramHash)}`. `ExactHash` is a useful explicit native extension; ordinary calls use `Database` and resolve from the locked actual db-before. Use an analogous explicit predicate reference/resolver policy.
3. **Runtime values separate from datom values.** Keep stored `Value` restricted to legal database values, and add bounded/canonically encoded `RuntimeValue` variants for scalar, entity ref, vector/tuple, and map. The docs explicitly pass maps/vectors through serialized tx-data; exact Fressian bytes and concrete Clojure collection classes are unnecessary.
4. **Full transaction-data output.** A transaction program returns `Vec<TxForm>`, not `Vec<TxOp>`. Structured emit/template operations must be able to produce add, retract, CAS, retract-entity, ensure, entity maps, temp/current-tx refs, and nested calls. Empty output is valid. Feed all returned forms back through the single normalizer and kernel; do not create a privileged program mutation path.
5. **Enough control and data access.** Branching is directly required by the documented `if` example (`04_transaction_functions.md:175-190`). Finite collection traversal is required by its `every?` and by arbitrary tx-data transformations. Local database query is source-backed: compiled functions make `d/q` available (`function.clj:145-161`) and the transaction model explicitly permits declarative Datalog inside functions (`01_transaction_model.md:62-75`). Implement structured `If` plus bounded `Map/Filter/Fold/Any/All` (or an equivalently verifiable forward CFG), and a canonical query-template host operation against the supplied snapshot. General backward jumps and unmetered recursion are not required; nested transaction calls and finite iteration consume the shared budget.
6. **Role-correct snapshot.** `ExecutionContext` carries the immutable snapshot, role, resolver pinned to that database value, shared budget, and cancellation token. Transaction programs query db-before; entity predicates query db-after; attribute predicates receive only the value. Static `ProgramKind` checking is a reasonable Rust safety feature. A standalone `Query` program kind is not part of the Datomic transaction-function contract; query should principally be a capability used by transaction/entity-predicate programs.
7. **Truth-preserving results.** Predicate programs/callbacks return a runtime value or an error, not `bool`; compare to exact `true` at the caller so diagnostics retain non-true results. Wrap both interpreted programs and native callbacks in a panic boundary and translate unwind to a fault before publication (without claiming to catch process abort/OOM).
8. **One authoritative service path.** The service request accepts declarative `TxForm`s, including program calls. The worker first locks/selects the actual db-before, then resolves temporal bindings, expands, assesses, and conditionally publishes. Direct `PostgresStore::transact_program*` paths must not bypass idempotency, admission, or the serialized database worker.
9. **Stable retry identity.** Durable request digests cover submitted declarative forms and explicit constraints. For an ident-based call, the dynamically selected hash is determined from the locked db-before and remains recoverable from that immutable value; it is not injected into the stable submission digest, otherwise an ambiguous retry after a head change can be misclassified. An explicit `ExactHash` is part of the submitted form and therefore naturally part of the digest.

A canonical, versioned IR remains appropriate. It must bump ABI/version when semantics change and reject noncanonical/unknown encodings. It need not imitate Clojure bytecode, but it cannot call a straight-line arithmetic DSL a port of Datomic's documented transaction functions.

## Concrete gaps in the reviewed tree

At the review snapshot:

- `src/program.rs` exposes straight-line scalar instructions, `ProgramOutput::Transaction(Vec<TxOp>)`, boolean predicate output, and per-invocation `ProgramControl`; it lacks branches, collection/map runtime values, query calls, and nested `TxForm` emission.
- `src/transaction.rs` already has the right recursive `TxForm` normalization shape for process-local functions, but `TxCall.function: String` erases database-ident versus qualified-Symbol resolution and its depth/op limits are separate from program/query/predicate budgets.
- `src/postgres.rs` deploys and selects via `atomic_active_programs`, resolves name calls before the authoritative locked transition, and repeatedly creates `ProgramControl::default()` for predicates. That can mix a logical database snapshot with out-of-band activation state and bypass quotas by splitting work.
- `src/service.rs` accepts only `Vec<TxOp>`, so persisted program invocation cannot yet use the sole fenced/idempotent service path.
- `src/encoding.rs` still encodes/decodes `ErrorCategory::Forbidden` for `Require` although the runtime only admits Incorrect/Conflict. That ABI inconsistency should be removed by a versioned encoding change, not normalized silently.
- Attribute predicate evaluation currently occurs before material redundancy/deduplication, unlike the 1.0.7705 assessed-datom boundary.

These are product gaps, not reasons to add another abstraction layer.

## Stage 4 exit evidence

Stage 4 is complete only when tests demonstrate all of the following through the authoritative service path and after restart where persistence matters:

- A branching/map-inspecting function and a finite collection transform emit full tx-data; an empty result succeeds.
- A returned nested call expands recursively, every call reads the identical db-before, and neither sibling/generated data is visible during expansion.
- A local Datalog join/cardinality-many lookup works inside a tx function against db-before.
- Entity predicates inspect cross-field/cross-entity db-after; spec/predicate changes take effect next transaction; `db/ensure` never appears in current data or history.
- Attribute predicates require exact true, preserve an arbitrary non-true result in the error, run only on surviving assertions, and do not retrovalidate.
- Rebinding `db/fn` is an ordinary transaction: the new binding is unavailable inside that same transaction, current reads use it afterward, and `as-of` invokes the prior hash. Persisted predicate-symbol rebinding has the same temporal proof.
- Many nested calls, predicate assertions, collection items, and query rows cannot reset or evade the shared budget. User cancellation, budget exhaustion, decode/corruption, missing blob, and caught panic publish nothing.
- Restart/failover resolves the same temporal binding from the same recovered database value, while concurrent rebinding cannot produce a mixed snapshot.

Until these pass, the honest status is “immutable program catalog and partial VM implemented,” not “Datomic-style transaction functions implemented.”
