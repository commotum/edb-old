# Goal 2 — Single-Process Transactional Kernel

## Objective

Build the production Rust kernel that can apply complete, semantically unordered transactions to immutable database values correctly and deterministically in one process. It must implement Goal 1’s contract, preserve the documented Datomic information and transaction model, and follow the architecture and algorithms recovered from `1.0.7705` wherever they remain sound in idiomatic Rust.

This goal realizes Stage 2 of `goal-0/0-plan.md`. Its result is the semantic engine later PostgreSQL durability, peers, and query execution will share; it is not yet a storage service or distributed system.

## Constraints

- Keep every runtime component in Rust. Java and Clojure source are construction evidence only; introduce no JVM or Clojure dependency.
- Treat `datomic_pro_docs` as semantic authority, `goal-1/SEMANTICS.md` plus its conformance suite as the current executable acceptance boundary, and `1.0.7705` as the default structural and algorithmic blueprint.
- Trace relevant recovered names, types, control flow, representations, caches, and performance choices before replacing them. Preserve them through the closest clear Rust design unless documented semantics, safety, or a concrete Rust advantage justifies a recorded deviation.
- Keep `with` pure: the caller supplies an immutable db-before, transaction information, and controlled transaction context; the kernel returns one immutable successor and report or one atomic structured failure.
- Transaction input order must not change acceptance, database result, tempid equivalence, or stable error code.
- Preserve complete transaction integrity: no observable intermediate state and no partial successor on failure.
- Do not add PostgreSQL, generic storage traits, durable encoding, networking, async service machinery, peer distribution, or production segmented indexes in this milestone.
- Do not implement the full Datalog or pull engines here. Provide only the indexed database-value interfaces their later implementations require.
- Keep the Goal 1 scan-based reference behavior available for differential testing until the production kernel demonstrably supersedes it.
- Avoid speculative abstraction and compatibility machinery. Existing Datomic databases, Java APIs, Fressian bytes, and Artemis transport do not need to interoperate.

## Known context

- Goal 1 provides canonical values and comparison, datoms and index ordering, schema descriptors, structured errors, a pure vector-scan transition model, documented ambiguity decisions, and 28 passing tests.
- The current model intentionally favors auditability over production structure. It does not yet provide schema-as-data transactions, all list/map/nested normalization, attribute predicates, a general transaction-function boundary, filtered views, structural sharing, or production-shaped in-memory indexes.
- The recovered spine worth preserving is `ProcessInpoint` → `ProcessExpander` → `get-ids` → composite generation → `filter-assess-tx-datoms` → immutable db-after construction and post-state validation.
- Built-in transaction functions such as CAS and retract-entity belong in this kernel. Persisted sandboxed WASM/DSL deployment belongs to Goal 0 Stage 6; this goal needs only a deterministic function interface and enough implementation to prove db-before expansion semantics.
- In-memory EAVT, AEVT, AVET, VAET, and history access here establish logical/index contracts. Durable shallow trees, immutable storage segments, background indexing, and peer caching remain later stages.

## Result

**Status:** Complete on 2026-09-02. The production single-process path is implemented in `src/`; the original semantic assessor remains the straightforward oracle inside `database.rs`, while immutable index roots are the read path. `tests/semantic_conformance.rs` retains the Goal 1 corpus and `tests/kernel_conformance.rs` exercises the added kernel surface and invariants.

The implementation retains the recovered `ProcessInpoint`/`ProcessExpander` shape as typed form normalization followed by one complete primitive information set, deterministic ID resolution, composite derivation, assessment, and immutable successor construction. It retains `Datum`/`Attribute`/`Db` roles as `Datom`, `Attribute`/`Schema`, and `Database` without reproducing JVM protocols or Clojure collection machinery.

## Stages

### 1. Production core boundary

**Status:** Complete. Shared public value, datom, schema, transaction, report, view, and error types feed a scan-auditable assessor and immutable indexed read state; there is no second transition implementation to drift semantically.

**Outcome:** The crate clearly separates the simple Goal 1 reference oracle from the production kernel while sharing one semantic vocabulary and avoiding duplicate truth.

**Focus:** Stabilize public value, identifier, datom, schema, transaction, database-value, report, and error types; identify which recovered `Datum`, `Attribute`, `Db`, process, and comparator roles map directly to Rust; establish explicit ownership and structural-sharing boundaries.

**Completion signal:** Reference and production paths compile against the same logical types, existing conformance tests still pass, and later stages can improve representation without changing public meaning.

### 2. Immutable indexed database values

**Status:** Complete. `index.rs` implements typed EAVT/AEVT/AVET/VAET roots, exact Pro membership, prefix seek/match and AVET ranges. `Arc`-owned schema/index roots and per-transaction history chunks keep old database values valid and share immutable history.

**Outcome:** A database value provides correct current and historical access through production-shaped in-memory EAVT, AEVT, AVET, and VAET indexes while old snapshots remain valid.

**Focus:** Explicit index keys and membership; descending transaction order; immutable snapshot roots; structural sharing; current versus history representation; ident and schema caches; recent-transaction layering where useful; indexed seek/range primitives; and schema-aware entity access needed by transaction processing.

**Completion signal:** Index cross-checks prove all indexes represent the same facts, old snapshots remain unchanged after successors are produced, index ordering matches Goal 1 for adversarial values, and reads no longer require scanning the full fact history.

### 3. Schema-as-data and identity

**Status:** Complete at the native kernel boundary. Schema installs and alterations are transaction operations, use db-before for domain resolution, update the successor synchronously, preserve aliases, enforce immutable/change invariants, rebuild AVET membership, and are retained as transaction-grouped `SchemaChange` information. Identity/upsert remains deterministic and index-backed.

**Outcome:** Installed schema, schema evolution, idents, entity identifiers, lookup refs, tempids, uniqueness, and upsert are processed as ordinary transaction information with the documented timing rules.

**Focus:** Bootstrap schema; attribute installation and alteration; immutable schema fields; aliases; cardinality changes; AVET/unique prerequisites at the logical boundary; tuple definitions and discontinuation; db-before attribute resolution; deterministic entity allocation; within-transaction identity unification; and conflicts among multiple identities.

**Completion signal:** Legal schema transactions update the successor synchronously, invalid changes fail atomically, historical views use current-basis schema as documented, and identity/upsert fixtures pass across input permutations and existing/new entity combinations.

### 4. Transaction normalization and expansion

**Status:** Complete. `transaction.rs` normalizes primitive operations, entity maps, anonymous tempids, cardinality-many values, reverse attributes, nested maps, lookup/current-tx/entity refs, and recursively returned function data. Bounded process-local functions all receive the same db-before.

**Outcome:** Every supported transaction syntax and deterministic function expansion converges on one complete primitive information set before state transition.

**Focus:** Primitive add/retract forms; value-less retract; map forms; anonymous tempids; cardinality-many expansion; reverse attributes; nested component and uniquely identified maps; current transaction entity; lookup resolution; deterministic built-ins; a bounded native function interface receiving only db-before and explicit arguments; and cancellation/error propagation.

**Completion signal:** Equivalent list, map, nested, and function-produced transactions normalize to equivalent information, every function observes the same db-before, malformed input has stable errors, and no normalization step exposes or depends on an intermediate database.

### 5. Complete assessment and successor construction

**Status:** Complete. The kernel covers type/tuple checks, redundancy and collisions, cardinality, uniqueness, CAS, inbound/outbound/component retract-entity, composite derivation, transaction instants, attribute predicates, explicit entity ensures, material tx-data, and atomic immutable reports.

**Outcome:** The kernel accepts exactly consistent information sets and constructs their immutable successors, including derived data and post-state validation.

**Focus:** Type and tuple validation; exact redundancy rules; add/retract collisions; cardinality-one replacement and collision; uniqueness; CAS; component retract-entity; composite tuple derivation and upsert timing; transaction instant/provenance; attribute predicates at their effective basis; explicitly requested entity specs against db-after; and complete transaction reports.

**Completion signal:** Goal 1 collision and ambiguity fixtures pass through the production path, validation failures leave all inputs unchanged, tx-data contains exactly material datoms plus the reified transaction, and successful output is deterministic.

### 6. Time views and kernel read surface

**Status:** Complete. Current/history/as-of/since/custom-filter views compose over immutable history, instant-to-t boundaries are explicit, filters run before retraction collapse, and index-prefix reads work on both base and derived views.

**Outcome:** Immutable database values expose the time and filtering semantics needed by later peer, query, and pull implementations without embedding those engines prematurely.

**Focus:** Basis and transaction lookup; current, history, as-of, and since; filtered datom views; composition rules; current-schema interpretation; entity and index access restrictions on history; monotonic successor relationships; and small cancellation-aware iteration surfaces.

**Completion signal:** View composition fixtures match `goal-1/SEMANTICS.md`, index seeks return the same facts under each view as the reference evaluator, and no view mutates or branches an earlier database value.

### 7. Differential and invariant verification

**Status:** Complete for the kernel scope. The Goal 1 corpus runs unchanged through the indexed database value. Kernel tests cross-check every root against scan facts, replay history back to current state, compare reversed transaction permutations, retain old snapshots, and run 64 deterministic state transitions through all four current and history orders.

**Outcome:** The production kernel is a trustworthy replacement for the reference transition on their shared scope and a stable base for PostgreSQL durability.

**Focus:** Run the Goal 1 corpus against both paths; generate transaction permutations and state-machine sequences; check index agreement, uniqueness, cardinality, history pairs, basis monotonicity, atomic failure, deterministic output, and snapshot immutability; retain narrowly useful recovered examples as fixtures; remove reference duplication only when equivalence is proved.

**Completion signal:** All conformance, property, model-differential, and invariant checks pass without warnings; known semantic differences are intentional and documented; and the same db-before, transaction, and context always produce the same report or stable failure.

## Kernel exit condition

Goal 2 is complete when one Rust process can construct and evolve valid immutable database values through the full supported declarative transaction model, answer the indexed and temporal reads required by transaction processing, and demonstrate through differential and invariant testing that it implements Goal 1’s contract. PostgreSQL durability, service orchestration, peer synchronization, full query/pull evaluation, and persisted sandbox deployment must remain clearly unclaimed.

**Status:** Achieved. `cargo test --all-targets` passes 41 tests and `cargo clippy --all-targets -- -D warnings` is clean.

## Deliberate native boundaries

- `SchemaChange::{Install, Alter}` is the typed normalized representation of schema transaction information. The initial `Schema` is native bootstrap input and subsequent schema information is retained by transaction. Assigning versioned persistent IDs/bytes and projecting that information into the later Datalog universal relation belongs with PostgreSQL encoding and query execution; the kernel does not invent compatibility-oriented Datomic boot IDs.
- In-memory roots are sorted immutable arrays and history is shared in transaction chunks. Recovered shallow trees, novelty layers, consolidation, and storage-backed index availability belong to the PostgreSQL/index-peer milestones; reproducing them here would violate this goal's explicit boundary.
- Process-local transaction functions and attribute predicates are named Rust closures with expansion limits. They preserve db-before and failure semantics; persisted deterministic WASM/DSL deployment remains Goal 0 Stage 6.
- The Rust API uses typed transaction forms rather than an EDN/Clojure parser. This removes parsing/runtime machinery without creating different transaction meaning.
- The straightforward Goal 1 assessment functions remain in the production transition instead of maintaining a duplicate “optimized” assessor. Production indexes are checked against that representation on demand with `Database::validate_invariants`.
