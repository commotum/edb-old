# Atomic query execution: source trace and native decisions

This development companion traces the Datalog/query/rules component of Stage 4.
It does not replace the recovered Pro text or the application documentation.
Paragraph locators below use section names and distinctive phrases because added
study comments shift source line numbers. Source symbols refer to both recovered
artifacts unless a packaging difference is stated. The [source atlas](../../development/source/README.md)
owns provenance; the [goal plan](../../goal-0/0-plan.md) owns acceptance status.

## Read path and responsibility

The Peer facade `api/q`, `api/query` and `api/qseq` pass explicit inputs to
`query/q*`. That function extracts return-map metadata, gets a prepared query,
executes `datalog/qsqr`, groups the basis, and constructs optional Pull/result
projection. No live Connection is dereferenced by this evaluation path.
`DbRel` retains the supplied Db, including its window/filter behavior, and its
join implementation probes that value's indexes. Raw relations use `ExtRel`
and collection joins instead of database-specific attribute resolution.

The recovered owners are [query](../../1.0.7705/peer/src-clj/datomic/query.clj),
[datalog](../../1.0.7705/peer/src-clj/datomic/datalog.clj),
[aggregation](../../1.0.7705/peer/src-clj/datomic/aggregation.clj),
[math](../../1.0.7705/peer/src-clj/datomic/math.clj),
[extensions](../../1.0.7705/peer/src-clj/datomic/extensions.clj) and
[query/support](../../1.0.7705/peer/src-clj/datomic/query/support.clj).
The [transactor support source](../../1.0.7705/transactor/src-clj/datomic/query/support.clj)
is useful for reading its equivalent forms without initializer expansion.

Atomic's [query module](../../src/query/mod.rs) follows these responsibilities:

| Native owner | Responsibility |
|---|---|
| [model](../../src/query/model.rs), [prepare](../../src/query/prepare.rs) | Typed query and result shapes; validation, structural preparation and cache reuse. |
| [execute](../../src/query/execute.rs), [control](../../src/query/control.rs) | Public execution entry points, ready-clause scheduling and dispatch; one invocation's limits, deadline and accounting. |
| [sources](../../src/query/sources.rs), [patterns](../../src/query/patterns.rs), [ranges](../../src/query/ranges.rs) | Named immutable database/relation/log inputs, semantic source validation, index selection and bounded cursor traversal. |
| [bindings](../../src/query/bindings.rs), [join](../../src/query/join.rs), [value](../../src/query/value.rs) | Input/output destructuring, unification, set formation, raw relation hash joins and exact general query values. |
| [rules](../../src/query/rules.rs), [dependencies](../../src/query/dependencies.rs) | Demand tables and positive fixed points; required bindings, branch scope and completed negative subqueries. |
| [functions](../../src/query/functions.rs), [extensions](../../src/query/extensions.rs), [nested](../../src/query/nested.rs), [numeric](../../src/query/numeric.rs) | Built-ins, explicitly registered native calls/programs, nested query execution and numeric domain rules. |
| [results](../../src/query/results.rs), [custom_aggregate](../../src/query/custom_aggregate.rs), [sequence](../../src/query/sequence.rs) | Basis/grouping, built-in/custom aggregation and eager/lazy Pull projection. |
| [return_maps](../../src/query/return_maps.rs), [value_debug](../../src/query/value_debug.rs), [diagnostics](../../src/query/diagnostics.rs) | Ordered named results, iterative value diagnostics and bounded execution observations. |

Pull's traversal implementation and fulltext's search/storage mechanism are
separate navigation/search work. This trace covers their query entry boundary;
the [Pull/entity](03_pull.atomic.md) and
[fulltext](02_query_reference.fulltext.atomic.md) companions trace their own
mechanisms and bounded verification, without asserting full release completion.

## Paragraph-to-mechanism trace

Each check names an existing integration target, with an individual test where
that distinguishes the promise. These are executable locators, not a claim that
every target has been rerun after every subsequent edit.

| Pro paragraph / promise | Recovered symbols and reason | Native owner and focused check |
|---|---|---|
| [Executing Queries: Querying a Database](01_executing_queries.md#querying-a-database), acquire a Db before querying | `api/q` → `query/q*` → `datalog/qsqr`, `DbRel`; use the caller's immutable basis rather than an advancing connection. | `execute::QueryEngine`, `sources::QuerySourceValue`; `query_exact_sources::named_as_of_and_since_sources_govern_patterns_missing_and_functions`. |
| [List Form vs. Map Form](01_executing_queries.md#list-form-vs-map-form), equivalent query data | `query/support/query-map`, `query/mapify-query`, `listq->mapq`, `parse-query`; normalize syntax before validation/planning. | `edn_query::parse_query_edn`, `prepare::PreparedQuery`; `edn_query_pull::vector_list_and_map_queries_preserve_native_ast_bindings_and_find_shapes`. |
| [Find Specs](02_query_reference.md#find-specs), four result shapes; Variables paragraph, find order | `process-find-bindings`, `q*` dispatch to `one-value`, `first-tuple`, `one-column`; projection retains find positions. | `model::{FindSpec,QueryResult}`, `results::shape_results`; `edn_query_pull` and `query_nested_shapes::empty_query_results_preserve_nil_or_empty_container_as_values`. |
| [Inputs / Binding Forms](02_query_reference.md#inputs), scalar/tuple/collection/relation inputs | `process-in-bindings`, `binding-type`, `expr-clause`, `compile-expr-clause`; ground adapters form relations and retain runtime input bindings separately from prepared structure. | `bindings::bind_inputs`, `general_input_relation`; `query_general_edn::set_input_is_a_collection_but_not_a_positional_tuple_or_relation_row`, `edn_query_pull`. |
| [Data Patterns](02_query_reference.md#data-patterns) and [Unification](01_executing_queries.md#unification), shared variables agree; blanks do not bind | `process-self-unifications`, `matchf`, `create-join-maps`, `extrel-coll`, `DbRel` joins; enforce residual equality even when a prefix narrows candidate enumeration. | `patterns` and `bindings` unification; `query_semantics`, `query_general_values::complete_general_relation_queries_match_scan_and_have_bounded_growth`. |
| [Predicates](02_query_reference.md#predicates), comparison predicates can use AVET | `process-ranges`, `bound-consts`, `ranges`, `DbRel` seek/while predicates; keep the original comparison clause because a seek hint is not the whole predicate. | `ranges`, `patterns::select_datoms`; `query_ranges::all_six_comparisons_preserve_mixed_numeric_boundaries_and_operand_order`, `local_conjunction_hints_do_not_escape_or_not_or_named_rule_scopes`. |
| [Clause Order](01_executing_queries.md#clause-order), put selective clauses early | `sched-in-order` defers clauses lacking inputs; `push-preds` fuses ready predicates into a preceding join. No statistics-based cardinality model appears here. | `execute::{evaluate_clauses_in_phase,clause_score}`; bound-score adaptation below. `query_diagnostics::original_clauses_actual_order_bindings_and_warnings_are_correlated`. |
| [Not Clauses](02_query_reference.md#not-clauses), all shared variables must bind; Not-join restricts unification | `unifying-vars`, `not-or->not-or-join`, `eval-not-join`; run a complete nested query, then set difference against the input relation. | `dependencies::evaluate_complete`, `evaluate_negative`; `query_dependency_repairs::negation_waits_for_recursive_positive_closure_and_keeps_local_variables_private`, `negative_seed_batches_do_not_rescan_the_outer_relation_once_per_entity`. |
| [Or Clauses](02_query_reference.md#or-clauses), branches expose the same variables | `unifying-vars` checks branch sets; `normalize-or-join`, `or-join->rule-preds`, `prep-clauses` lower branches to alternative rule bodies. | `prepare::validate_or`, `dependencies::clause_bindings`, `execute`; `query_dependency_repairs::predicate_only_or_waits_for_inputs_in_every_outer_clause_order`, `nested_join_scopes_do_not_leak_private_variables_into_plain_or`. |
| [Or-join Clause](02_query_reference.md#or-join-clause), explicit unifying variables | `or-join->rule-preds` → `add-rule` → `sched-in-order`. The exported list and required-input prefix are different; see the prose/source discrepancy below. | `dependencies::clause_bindings`; `query_dependency_repairs::or_join_can_bind_outputs_and_schedule_branch_functions_and_required_rules`. |
| [Rules](02_query_reference.md#rules), same-name definitions are alternatives | `rule-map` groups by name and validates arity; `eval-query` visits every body; `eval-rule` unions projected heads into answer sets. | `rules::{solve_rule_invocations,stabilize_rule_memo,evaluate_rule_key}`; `query_rules::mutually_recursive_required_rules_reach_the_finite_fixed_point`. |
| [Required Bindings](02_query_reference.md#required-bindings), an explicit head prefix must bind | `add-rule` stores `:reqcnt`; `sched-in-order` checks it; `adorned-pred` separately records the actual bound/free call pattern. | `model::Rule.required`, `execute::clause_ready`, `rules`; `query_rules::required_bound_recursion_is_demand_driven_per_invocation`. |
| [Rule Database Scoping](02_query_reference.md#rule-database-scoping), source prefix changes the rule's default Db | `move-sources-to-meta`, `eval-rule` selects `cdb`, `eval-clause` forwards source identity; `eval-query` keys seen inputs by source/adornment and answers by source/predicate. | `sources::effective_source`, `rules::RuleInvocationKey`; `query_rules::rule_invocation_source_is_inherited_and_partitions_the_memo`, `query_dependency_repairs::completed_negative_cache_is_local_to_clause_source_seed_and_execution`. |
| [With Clauses](02_query_reference.md#with-clauses), form the basis before removing with variables | `process-aggregates` appends with coordinates to the head; `qsqr` forms a set; `group-rel` removes them afterward. Equal values from different identities form a bag, while identical input rows still coalesce. | `results::shape_results`, `aggregate_rows`; `query_result_contracts::distinct_is_a_set_and_with_preserves_only_distinct_basis_rows_in_each_group`, `query_general_values::general_aggregate_groups_and_sorted_results_preserve_values_and_bags`. |
| [Aggregates](02_query_reference.md#aggregates), nonaggregate variables group results | `group-fv` associates input column/functions; `group-rel` sorts by grouping columns and exposes each group as a collection view. | `results::aggregate_rows`, `custom_aggregate`; `query_custom_aggregates`, `query_general_values`. |
| [Aggregates Returning Collections: distinct](02_query_reference.md#aggregates-returning-collections), result is a set | `aggregation/distinct` calls `set`, unlike bounded min/max vectors and rand/sample sequences. Shape is observable independently of membership. | `results::aggregate` returns `QueryValue::Set`; `query_result_contracts::edn_distinct_result_is_readable_set_data`, existing general aggregate assertion. |
| Same section: min/max n, rand n and sample n cardinality | `aggregation/{min,max,rand,sample}`, `math/reservoir-sample`; min/max retain ordered prefixes, rand samples with replacement, sample first deduplicates. | `results::aggregate`; `query_composition::local_random_aggregates_have_documented_cardinality_and_membership`. Native partial shuffle differs from the recovered reservoir algorithm. |
| [Built-in Predicates and Functions](02_query_reference.md#built-in-predicates-and-functions), integer `/` avoids ratio results; aggregate statistics | `extensions//` selects `quot` for integer operands; `aggregation/{sum,avg,median,variance,stddev}` define aggregate formulas. | `numeric::{binary,quotient,aggregate}`; `query_numeric_operations`, `query_numeric_statistics_range`, `query_numeric_program_budget`. Exact-domain adaptation below. |
| [Pull Expressions](02_query_reference.md#pull-expressions), navigate selected entities; [qseq](01_executing_queries.md#qseq), defer Pull/xforms | `process-pulls` retains entity variables in the basis; `pull-fv`, `xf-tuple`, `q*`, `apply-pf`, `qseq`, `counted-seq` separate relation discovery from navigation. | `results::project_element`, `sequence::QuerySequence`; `query_composition::sequence_defers_transforms_preserves_sources_and_fuses_after_cancellation`, `query_result_contracts::typed_and_edn_lazy_pull_keep_the_value_byte_limit_after_relational_execution`. |
| [Return Maps](02_query_reference.md#return-maps), keys correspond to find positions; indexed/destructured access | `support/parse-as` extracts key metadata; `q*` constructs `MapOnIndexed` over each projected tuple. The concrete class implementation is not present in this recovered source inventory; dual access is a documented contract independently checked in Rust. | `return_maps::{ReturnMap,ReturnMaps}`, `edn_query::BoundEdnQuery::result_to_edn`; `query_return_maps::typed_keys_and_positional_access_preserve_exact_nested_values`, `tuple_relation_and_empty_shapes_remain_distinct`. |
| [Timeout](01_executing_queries.md#timeout), abort after elapsed threshold | `qsqr` schedules a shared cancel flag; `maybe-cancel` observes it at relational checkpoints. This cannot preempt arbitrary callback code. | `control::State`, `sequence`, shared `pull::QueryPullBudget`; `query_exact_sources::find_pull_shares_the_enclosing_query_cancel_deadline_and_work_budget`, `query_dependency_repairs::negative_subqueries_share_work_and_cancellation_with_their_parent`. |
| [Query Caching](01_executing_queries.md#query-caching), structural query reuse | `query-cache` stores `load-query`; `rule-cache` stores `rule-map`. `q*` and `qsqr` still create invocation-local source/answer state. | `prepare::{PreparedQuery,PreparedQueryCache}`; `query_preparation::prepared_rule_analysis_is_reused_only_after_success_and_controls_still_apply`, `query_runtime_sources`. |

## Semantic boundaries and deliberate adaptations

The or-join paragraph says its unifying variables need binding before execution.
The recovered mechanism is more precise: ordinary exported variables may be
produced by a branch. Only a leading nested argument list causes `add-rule` to
record a required prefix, which the scheduler checks separately. Atomic follows
the source distinction by checking branch readiness and outward bindings rather
than requiring every exported variable to be present at entry. Native `Rule`
can express required positions directly; it is not limited to the source's
contiguous required-head prefix syntax.

Both engines form sets during relational evaluation, but neither should turn
every final projection into a fresh set. `:with` preserves repeated projected
observations, and two distinct entity bindings may Pull to equal maps. Return
map keys describe already-ordered find columns. Atomic additionally preserves an
empty result's key schema and distinguishes absent tuples from empty relations;
its conversion rejects duplicate keys and malformed widths. Iterative
`QueryValue` Debug traversal belongs to result representation, with deep-value
and writer-error checks in `query_value_debug`; it is not EDN serialization.

The repaired `distinct` result is a public set in typed and EDN calls. The native
comparator permits general values, including nested sets, maps, characters and
inert tags, beyond stored datom values. Tests require the `Set` variant and `#{}`
readable syntax, not just matching sorted members. Collection destructuring of
a nested distinct aggregate remains covered by `query_nested_shapes`.

The recovered scheduler selects ready clauses in source order after moving
input/expression groups and pushing predicates. Its database join refuses a
wholly unbound scan. Atomic retains its existing ready-clause bound-score
heuristic and controlled EAVT fallback. These can select a different clause order
and accept a query that the recovered engine rejects. They are explicit native
choices, not a claim of planner or scan-cost equivalence. Candidate traversal,
intermediate rows, result rows, work and accounted value bytes remain limited by
`QueryControl`. Forced-scan comparisons in `query_ranges` and
`query_general_values` check results independently of selective access.

Atomic's positive rule answers are keyed by effective source and bound arguments.
Its explicit negative-task driver computes a completed lower relation before
subtracting it; a provisional positive fixed point cannot be negated. Restarts,
cloned seeds and repeated rounds are charged to the same execution. The source
QSQR likewise retains monotone answer sets across rounds and resets seen inputs;
neither this source trace nor matching results establish delta-only complexity.

`qseq` has already materialized its relational basis; it is not a streaming join
engine. In the recovered relation path, Pull projection is deferred and the
count is known before navigation. Source find-binding variants pass through
`q*`'s shape branch, so the exact amount of deferred work is shape-dependent.
Atomic exposes a fallible tuple iterator for all native find shapes. Its
`remaining_rows()` counts prepared rows, not guaranteed successful Pull results.
The repaired lazy path retains both already-accounted bytes and the caller's
`max_value_bytes`; setting only the former while silently using an unlimited
maximum allowed deferred Pull to escape a small allowance. Failure fuses the
iterator; another call can reuse the immutable database/prepared query.

Native eager and lazy phases share an absolute deadline, cancellation and work
allowance. This is stronger than inferring a projection-wide timeout from the
source `qsqr` dynamic binding, which is popped before later lazy consumption.
`qseq`'s recovered `io-context` wrapper measures relational preparation; native
`QuerySequence::stats()` continues accumulating lazy work as rows are consumed.
These scopes must not be equated when comparing costs. The existing
[diagnostic guide](../../docs/05_query_and_pull/04_diagnostics.md) explains phase/counter scope.

Exact native arithmetic selects Long/BigInt/BigDec before floating conversion,
checks Long overflow, and admits coefficient growth before allocating decimal
expansions. Integer division truncates toward zero. Sum and exact median retain
their selected domain; mean, variance and standard deviation are explicitly
approximate doubles. Exact centering and rescaling protect representable
statistics from intermediate overflow or loss of small differences. Genuine
output range failures remain checked errors. Recovered `aggregation/median`
sorts and uses `quot` for its even pair; the similarly named `math/median`
merely selects an element of a sorted sequence and is not the query implementation.
Native decimal midpoint behavior and floating rounding are deliberate domain
choices, not identical JVM numeric semantics.

Prepared-query reuse retains validated structure, not database values, arguments,
callback registries, result rows or rule answers. Successful source-independent
negative-cycle analysis may be reused; canceled or failed analysis does not
publish success. Source caches use computing cache factories; the native cache
has its own bounded retention policy. Structural equality is not semantic
canonicalization, and neither cache eliminates invocation-time scheduling.

## Source preservation and verification

The full peer query, datalog, aggregation, math, extensions and query/support
bodies and their direct query callers were inspected. Comparing the corresponding
transactor bodies after ignoring added comments, whitespace and generated
identifier numbers found the same bodies for query, datalog, aggregation, math
and extensions. Transactor query/support is separately packaged readable source;
its actual definitions were read alongside the recovered peer initializer.
Packaging and generated names do not justify a second native engine.

Additional `ATOMIC-NOTE` comments fill gaps adjacent to the owning symbols;
existing explanations remain in place. The inventory guard checks all recovered
source files and rejects body changes. It does not certify semantic coverage.

Focused component checks are grouped by the promises above:

```sh
python3 development/source/inventory.py --check
cargo test --test query_result_contracts --test query_general_values --test query_return_maps --test query_value_debug --test query_nested_shapes --test query_composition --test edn_query_pull
cargo test --test query_rules --test query_dependency_repairs --test query_exact_sources --test query_ranges --test query_preparation --test query_runtime_sources --test query_diagnostics
cargo test --test query_numeric_operations --test query_numeric_statistics_range --test query_numeric_program_budget --test query_custom_aggregates
```

Database-dependent cases require the real PostgreSQL fixture. These commands
are component checks; final application/restart/failover/backup acceptance is
still owned by the integrated release stage.
