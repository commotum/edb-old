# Development trace: shared transaction processing

This companion covers the value-computation part of
[Processing Transactions](03_processing_transactions.md), with its governing
[Transaction Model](01_transaction_model.md) and
[Transaction Functions](04_transaction_functions.md) passages. Submission queues,
durability, notifications and timeout recovery are separate lifecycle concerns;
see the [ACID trace](05_acid.atomic.md). The original documentation is unchanged.

Source locators below use the unannotated reference revision
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`. Function/type anchors are authoritative
when added comments shift lines. This pass inspected the transactor bodies and
confirmed corresponding named mechanisms in the peer artifact; artifact membership
does not make them transactor-exclusive. The initial Rust inspection used
`142078c0e0a1979bb765037cd6d3259d8744d344`; links now follow the Stage 3 ownership
cutover. Test results must come from the coordinated integration run, not the
presence of a source anchor or a regression test.

## One information set, then a proposed successor

Passage: Transaction Model / **d/with and d/transact**, paragraphs beginning
“Even d/transact is built upon the pure function” and “The declarative,
set-oriented nature”; Processing Transactions / **Monitoring Transactions**, the
report-key table and paragraph defining `tx-data` as assertions and retractions.

Source: [transactor datomic/db.clj](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`ProcessInpoint.inject` (baseline 6530), `ProcessExpander.getData` (7361),
`filter-assess-tx-datoms` (4430), `add-ensured-data` (7867), `with-tx` (7892).
The [peer artifact](../../1.0.7705/peer/src-clj/datomic/db.clj) contains the same
named semantic pipeline at these baseline locators. The inpoint lowers maps and
validates primitive inputs against one db-before. The expander collects datoms,
resolves ids and composites, and assesses the combined information. `with-tx`
returns proposed before/after values, material datoms and tempids; its local
mutable accumulators do not constitute durable publication.

Rust: [transaction/pipeline.rs](../../src/transaction/pipeline.rs), `assess_operations`,
`assess_forms_with_clock`, `ValidatedTransaction`; and
[transaction/assess/mod.rs](../../src/transaction/assess/mod.rs),
`assess_tiered_with_remaining_limits_and_defaults`, `material_changes`,
`TieredAssessment::validate_exact`. The assessor gathers the full delta, checks
within-transaction conflicts and successor constraints, and creates an immutable
overlay. It is used by [database.rs](../../src/database.rs),
`Database::with_defaults`, the `DatabaseValue::with*` entry points in
`transaction/pipeline.rs`, and [transactor/authority/mod.rs](../../src/transactor/authority/mod.rs),
`BlockTransactor::transact_captured`. The durable caller resolves a matching
receipt before fresh assessment and separately protects/publishes its output.

Retain this single semantic implementation. `Database::materialize_assessment`
is an eager representation adapter, not an independent transaction oracle.
Committed replay validation has a different responsibility from fresh assessment:
it must not reexecute callbacks or synthesize facts absent from the committed log.

Checks to preserve: [native_speculation.rs](../../tests/native_speculation.rs),
`chained_speculation_preserves_current_and_history_across_memory_representations`
and `native_speculation_never_advances_postgres_or_materializes_the_database`;
[block_transactions.rs](../../tests/block_transactions.rs),
`typed_edn_identity_and_exact_receipts_survive_new_writes_and_writer_reopen`.
The shared-assessor comparison tests establish representation consistency, not
independent Datomic equivalence. PostgreSQL cases require a live fixture.

## Functions see db-before; entity predicates see complete db-after

Passages: Transaction Functions / **Transaction Function Semantics**, the pure
function contract and paragraph beginning “Transaction functions are tightly
focused”; **Writing Transaction Functions**, rules 1–4; and Transaction Model /
**Application Correctness**, the distinction between generation and entity specs.

Source: transactor `db.clj`, `ProcessExpander.inject` (baseline 7361), resolves
the callable and invokes `(apply pfn db (rest procargs))`. Returned forms reenter
`ProcessInpoint` with that same `db`; one function never reads another function's
partially applied results. `ensure-entity!` (7737) reads the spec from db-before
but invokes its predicates on db-after. `add-ensured-data` invokes `ensure-tx`
only after constructing that proposed value. These are reasons to separate
expansion and successor validation, not reasons to create separate assessors.

Rust: [program_bindings.rs](../../src/program_bindings.rs),
`expand_submission_forms_with_native`, `expand_program_call`, and
`persisted_predicates_with_native`; [transaction/native.rs](../../src/transaction/native.rs),
`NativeRegistry::{transaction,attribute,entity}`;
[transaction/functions.rs](../../src/transaction/functions.rs),
`TxFunctions::{invoke,validate_entity_predicate}`;
[transaction/expand.rs](../../src/transaction/expand.rs), `expand_local_calls`;
`transaction/assess/mod.rs`, `TieredAssessment::{predicate_requirements,validate_exact}`.
Both controlled persisted programs and explicitly deployed native callbacks
converge on the same post-expansion assessor. `TxFunctions` also carries predicate
adapters built by `persisted_predicates_with_native`; it is not merely a duplicate
deployment registry. Only literal true accepts a predicate result.

Retain the deployment distinction. Persisted native programs replace JVM function
objects; `NativeRegistry` is trusted cooperative Rust code, not a sandbox or
serialized closure. Its `NativeCallContext` can enforce reported work/allocation
and preserve a reported failure, but cannot preempt noncooperative host code.
This matches the documentation's need to trust deployed code; it does not prove
arbitrary JVM language or function-body compatibility.

Independent expected-result checks:
[native_transaction_functions.rs](../../tests/native_transaction_functions.rs),
`exact_speculation_shares_db_before_and_checks_complete_db_after` (nested callback
explicitly cannot see its parent's generated fact), and
`deployment_identity_limits_panics_and_application_errors_are_explicit`;
[program_transactions.rs](../../tests/program_transactions.rs),
`persisted_functions_compose_on_db_before_and_predicates_guard_commit` (setter
produces 99 while the snapshot function still reads 10), plus
`persisted_entity_spec_predicate_validates_complete_db_after_via_service`.

## Identity and clock selection belong to shared assessment

Passages: Transaction Model / **d/with and d/transact**, identity unification and
assigned t; Transaction Data / **Tempids**, **Lookup Refs**, and
**Explicit:db/txInstant**. The latter permits explicit times no earlier than the
basis and no later than the transactor's clock.

Source: transactor `db.clj`, `get-ids` (baseline 6640),
`ProcessExpander.getData` (7361), `has-tx-inst?` (6831), `next-valid-inst` (6882).
`get-ids` keys identity assertions before replacing tempid-valued references.
`replace_tempid` rejects a tempid used only as a value. Clock checks occur after
expansion, and the default time cannot move backward.

Rust: `transaction/assess/mod.rs`, `resolve_tempids`, `union_identity_assertions`,
`resolve_upsert_identity_value`;
[transaction/input.rs](../../src/transaction/input.rs), `UpsertIdentityValue`,
`resolve_tuple_input`, `validated_entity_tempids`, and
`DatabaseValue::resolve_lookup_input_with`. `transaction/pipeline.rs`,
`assess_durable_forms`/`select_tx_instant`, chooses time from normalized forms
without running functions twice; pure speculation takes an explicit clock.
The [canonical owner](../../src/transaction/canonical.rs) also retains
representation ties for exact request identity:
URI spelling and decimal scale must not disappear merely because logical values
compare equal.

Checks: [transaction/tests.rs](../../src/transaction/tests.rs),
`durable_clock_policy_preserves_explicit_errors_and_monotone_default`,
`durable_clock_expands_generated_instant_once_even_when_rejected`, and
`structural_value_ties_preserve_stored_numeric_representations`;
[edn_transactions.rs](../../tests/edn_transactions.rs),
`schema_disambiguates_tuples_lookups_and_many_reference_collections`.
These checks were inspected, not executed in this pass.

Known map distinction: source `has-unique-id?` (baseline 5951) accepts only
`:db.unique/identity`, while the Nested Maps documentation says “unique” more
broadly. Keep native `Unique::Value` anonymous-child admission, while preserving
its conflict semantics rather than turning it into upsert. Explicit nested IDs
bypass source `make-child-id` (6038); the already-identified native guard repair
belongs to the [map trace](02_transaction_data.atomic.md), not a second repair here.

## Bounded ownership disposition and demonstrated cost

The implemented responsibility split retains behavior and one assessor:

- [transaction/mod.rs](../../src/transaction/mod.rs) is the small type facade.
  [pipeline.rs](../../src/transaction/pipeline.rs) owns entry-point composition,
  the receipt-free `ValidatedTransaction` handoff and fixed/durable clock policy.
- [forms.rs](../../src/transaction/forms.rs) and
  [canonical.rs](../../src/transaction/canonical.rs) own declarative forms and
  exact input ordering. `EntityRef`, `TxValue` and `TxOp` moved out of
  `database.rs`; curated root public exports remain, not old private-path aliases.
- [input.rs](../../src/transaction/input.rs) owns admission, symbolic identity
  keys, tuple/reference input and tempid/reserved-input validation;
  [expand.rs](../../src/transaction/expand.rs) owns anonymous-map allocation and
  recursive form generation.
- [functions.rs](../../src/transaction/functions.rs) owns local callback roles
  and predicate adapters; [native.rs](../../src/transaction/native.rs) owns
  explicit deployed callbacks. `program_bindings.rs` retains its shared owner:
  database invocation, backup, storage ownership and log consumers also need its
  lookup/dependency traversal.
- [assess/mod.rs](../../src/transaction/assess/mod.rs) retains the current assessor
  as one mechanism, including its reader, identity grouping, schema transition
  and delta validation. Its scaling tests are a natural child module. Low-level
  eid layout remains model identity; replay/materialization and stored-state
  checks remain database owners.

Retain selective exact-prefix reads, cross-phase read charging, schema reuse,
reverse composite dependencies and sorted conflict grouping. Source
`ProcessExpander` overlaps prefetch for identity/composite/redundancy checks;
native `Reader::prefix` instead reuses exact-prefix reads through the shared
attempt context. These are different scheduling choices, not evidence of equal
latency. Existing counters and tests are not PostgreSQL throughput or RSS proof.

One additional concrete cost defect was identified in `UnionFind`: the former
`root(&self)` traversed parent links without compression, while `join` attaches
the larger root to the smaller. Identity groups processed in the order
`(n-2,n-1), (n-3,n-2), ..., (0,1)` create a length-n chain.
This can arise from valid cardinality-one identity attributes: give each pair
its own successively ordered attribute and the same value on both endpoints.
The unified entity has one value per attribute, so this is not a rejected
cardinality-conflict construction. The later per-name root walks in
`resolve_tempids` therefore took triangular, O(n²), parent steps.

This is a static algorithmic counterexample, not a measured application stall.
The existing `identity_grouping_is_n_log_n_and_unions_transitively` test in
[transaction/assess/scaling_tests.rs](../../src/transaction/assess/scaling_tests.rs)
counts key comparisons and uses disjoint pairs; it does not bound parent steps.
The implemented repair uses two-pass path compression while preserving the minimum
representative. `identity_parent_compression_preserves_minimum_roots_and_bounds_chain_walks`
compares counted search/rewrite edges with the former parent walker;
`connected_identity_chain_assessment_preserves_allocation_and_conflicts` runs valid
32/128/512-entity connected claims through actual assessment, checks independently
expected facts/frontier and existing upsert/partition conflicts, and records setup
and full assessment/drop cost separately. Its test-only counter includes parent
walks inside `resolve_tempids`, not just a collection microbenchmark. The linear
hop bound is specific to the descending-chain sequence; minimum-root union without
weighting does not justify an inverse-Ackermann claim.

The coordinated pre-move debug run passed both new tests. At 32/128/512 entities,
old parent hops were 992/16,256/261,632 versus 184/760/3,064 compressed hops inside
actual assessment. Assessment plus drop took 2.55/10.80/48.95 ms, while fixture
setup took 189.80/448.46/1,632.21 ms. The 2,048-element collection-only chain fell
from 4,192,256 to 12,280 parent hops. Minimum representatives, exact allocation and
partition/upsert conflict assertions passed. These are uncontrolled debug samples,
not production latency, PostgreSQL throughput or RSS claims; the subsequent
organization cutover still requires its coordinated compile/test run.

Recovered `get-ids` uses keyed maps; no `fold-ut`, `fold_ut` or `foldUt` symbol
was located in either Java/Clojure artifact. Do not infer an equivalent source
slowdown or claim an uninspected source union-find implementation.

A lower-priority optimization opportunity is repeated allocation/sorting inside
`compare_map_entries` and `compare_unordered_map_values`. Nested comparisons can
reorder the same children repeatedly during outer sorting. Finite-budget local
callback discovery also performs a primitive-admission expansion before the final
anonymous-ID allocation pass. Preserve the identity/capacity invariants; measure
complete normalization before redesigning either mechanism. Neither is a newly
demonstrated semantic error.

No other behavioral defect was established in this bounded pass. Exhaustive
schema-transition cases, full source callback compilation, all transaction-error
precedence, and transport/publication failure paths were not reaudited here.
