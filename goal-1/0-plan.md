# Goal 1 — Semantic Foundation

## Objective

Establish the precise, executable semantic foundation for the native Rust database: define what values, datoms, schemas, identities, transactions, database views, and failures mean before optimizing or making them durable. Preserve the spirit and stated behavior of `datomic_pro_docs`, and reconstruct the corresponding design in `1.0.7705` as faithfully as practical, translating it idiomatically rather than mechanically into Rust.

This goal realizes Stage 1 of `goal-0/0-plan.md` and must leave a firm contract for the single-process transactional kernel that follows.

## Constraints

- `datomic_pro_docs` is the primary semantic authority; cite the relevant local source for material rules.
- `1.0.7705` is the default implementation blueprint and secondary behavioral witness. Its Java/Clojure names, types, protocols, encodings, boundaries, and control flow must be studied and mapped because they reveal intended architecture and performance choices.
- Preserve recovered structures and algorithms when they remain sound. Exact Java types, Clojure evaluation machinery, Fressian/Artemis bytes, and public API compatibility are not goals in themselves, but replacement designs must be justified rather than assumed.
- Prefer the closest clear, idiomatic Rust equivalent. Deviate when direct reproduction would add needless JVM/Clojure machinery, conflict with documented behavior, weaken safety, or miss a concrete PostgreSQL/Rust advantage; record significant deviations and rationale.
- The target runtime is Rust and the eventual durable store is PostgreSQL only, but this goal must define semantics independently from SQL layout or performance optimization.
- Existing Datomic databases do not need to open in the new system.
- Transactions are declarative, semantically unordered information sets evaluated against an immutable `db-before`; all-or-nothing validation concerns the complete proposed result.
- Preserve durable history, database-value immutability, strong identity, monotonic transaction order, peer-local interpretation, and explicit time views.
- Resolve under-specified behavior deliberately, version decisions that affect persistent meaning, and avoid accidental dependence on Rust container iteration order or PostgreSQL collation.

## Known context

- The documented value set, cardinality, uniqueness, upsert, tuples, schema evolution, transaction forms, `with`, history, `as-of`, `since`, pull, and query behavior provide most of the necessary contract.
- The highest-risk open semantics include cross-type total ordering, floating-point normalization, conflicting same-transaction assertions, schema-plus-data transactions, composite-tuple upsert timing, and error precedence for unordered invalid input.
- A simple model and durable fixture corpus are more valuable here than production indexes, distributed services, or premature APIs.

## Stages

### 1. Evidence map and decision discipline

**Status:** Complete. `SEMANTICS.md` maps each foundation area to its documentation, recovered implementation path, native artifact, and translation rule.

**Outcome:** Every semantic area has an identified source of truth and a consistent method for handling omissions or conflicts.

**Focus:** Map `datomic_pro_docs` sections to the reconstructed capabilities; trace the corresponding types, names, boundaries, representations, concepts, and algorithms in `1.0.7705`; classify rules and design choices as documented, faithfully translated, idiomatically adapted, inferred, or newly decided; establish a compact decision-record format.

**Completion signal:** The semantic inventory covers all foundation areas, high-risk ambiguities are listed, and contributors can trace material rules to evidence or an explicit project decision.

### 2. Canonical values, identifiers, and datoms

**Status:** Complete. `src/value.rs`, `src/datom.rs`, and their law/edge-case tests define all supported variants, comparison, stored equality, tuple nils, index order, limits, and float decisions.

**Outcome:** The system has a stable logical vocabulary with deterministic equality, hashing, comparison, and representation rules.

**Focus:** Supported scalar and reference types; entity and transaction IDs; instants; UUIDs; keywords and symbols if retained; strings, bytes, integers, floats, decimals, big integers, tuples and null tuple elements; datom fields; assertion/retraction polarity; type ranking; numeric cross-type comparison; NaN and signed-zero policy; and version boundaries for persisted meaning.

**Completion signal:** Normative examples and executable tests define equality and total ordering for every supported value pair, datom construction is unambiguous, and no result depends on host-language or database collation accidents.

### 3. Schema, identity, and entity semantics

**Status:** Complete. `src/schema.rs`, schema-change validation, identity resolution, composite derivation, and conformance fixtures cover the transition-critical rules; `SEMANTICS.md` fixes the remaining schema-evolution contract for the later schema-as-data implementation.

**Outcome:** Schema-as-data and entity identity have complete rules for both ordinary use and evolution.

**Focus:** Attribute definitions; cardinality; uniqueness and identity; lookup refs; idents and aliases; tempids; upsert unification; references and components; tuples and derived composite tuples; attribute and entity predicates; schema changes; no-history; indexing flags; discontinuation; and validation timing.

**Completion signal:** Fixtures cover legal and illegal schema states, identity resolution and conflicts, schema evolution, same-transaction schema/data cases, and composite identity behavior with deterministic outcomes.

### 4. Declarative transaction semantics

**Status:** Complete. `Database::with` is the small pure reference transition. Conformance fixtures cover order independence, atomic failure, lookup timing, tempids/upsert, collisions, redundancy, CAS, replacements, components, composites, entity ensures, and transaction reification.

**Outcome:** Every supported transaction form reduces to a deterministic proposed information set and either produces one immutable successor database or fails atomically.

**Focus:** List and map forms; nested entities; add and retract; retract-entity; CAS; tempid allocation; lookup timing; redundancy elimination; cardinality-one replacement; transaction entities and monotonic instants; db-before transaction functions; db-after entity validation; same-EAV add/retract; collisions; and deterministic error selection.

**Completion signal:** A compact reference transition model passes examples and properties for atomicity, semantic order-independence, identity unification, constraint enforcement, reified transactions, and all recorded collision decisions.

### 5. Database values, time, and read semantics

**Status:** Complete at the foundation boundary. `View` fixtures distinguish current, as-of, since, and history; `SEMANTICS.md` fixes entity, query, and pull behavior for the later evaluator milestone without pretending those production evaluators exist now.

**Outcome:** Immutable snapshots and their observable read views are precisely defined for later peer, index, query, and pull implementations.

**Focus:** Basis and total transaction order; current and historical datoms; `as-of`, `since`, history, and filtered views; interaction with current schema; entity lookup; index membership and ordering; query relation set semantics; `with` bag behavior; pull omission, recursion, components, and cycles; and snapshot validity boundaries such as future excision.

**Completion signal:** Fixtures distinguish each view and its compositions, specify snapshot/entity/pull behavior, and define the contracts later indexes and query evaluators must preserve without prescribing their optimized implementation.

### 6. Errors and conformance corpus

**Status:** Complete. `SemanticError` supplies stable categories/codes/details, deterministic validation selects the same error across input permutations, and the Rust test corpus is the executable acceptance boundary for Goal 0 Stage 2.

**Outcome:** The foundation is consumable as a stable contract by the transactional-kernel milestone.

**Focus:** Structured error categories and details; incorrect input, conflict, busy, unavailable, interrupted, fault, and unknown-outcome distinctions; deterministic validation reporting; machine-readable fixtures; reference-model traces; property generators; expected-success and expected-failure cases; and documentation of deliberate differences.

**Completion signal:** The corpus runs automatically, covers each normative rule and ambiguity decision, produces reproducible results, and provides a clear acceptance boundary for Goal 0 Stage 2.

## Foundation exit condition

Goal 1 is complete when a new implementation can be judged for semantic correctness without consulting hidden JVM behavior: the native contract is traceable to the documentation, ambiguous cases have explicit decisions, and an executable model or fixtures verify the complete transition from `db-before` plus transaction to either `db-after` plus report or a structured atomic failure.

**Status:** Achieved on 2026-09-02. The authoritative artifacts are `SEMANTICS.md`, `src/`, and `tests/semantic_conformance.rs`. This completion claims the semantic foundation only; the EDN/API parser, full query and pull evaluators, persisted function runtime, production indexes, PostgreSQL durability, and services remain in their existing Goal 0 stages.
