# Atomic study companion: Pull and entity navigation

This development trace accompanies [Pull](03_pull.md), the core
[Entities chapter](../02_core_concepts/03_entities.md), the
[Entity API](../07_peer_api/01_java/06_entity.md), and
[Index-Pull](../06_indexes/03_index_pull.md). It does not replace those chapters
with JVM-shaped Rust APIs or declare all Stage 4 components finished.

The full recovered Peer [pull namespace](../../1.0.7705/peer/src-clj/datomic/pull.clj)
was read, including its selector, traversal and index-Pull bodies. The Transactor
counterpart has the same bodies after generated-identifier normalization. The
navigation bodies in [query.clj](../../1.0.7705/peer/src-clj/datomic/query.clj)
(`eav`, `rae`, `ref-val`, `get-lazy-entity`, `EntityMap`, `emap`), direct
`api/{entity,pull,pull-many,index-pull,touch,entity-db}` callers, relevant
[Db methods and naming helpers](../../1.0.7705/peer/src-clj/datomic/db.clj), and both
[Entity interfaces](../../1.0.7705/peer/src-java/datomic/Entity.java) supply the
entity trace. This is selected Db coverage, not a claim to have explained that
entire namespace.

## Native ownership

The [Pull module](../../src/pull/mod.rs) owns navigation, independently of
Datalog relational evaluation. Its owners are
[model](../../src/pull/model.rs) (selectors/identifiers and stack-safe pattern
ownership), [attributes](../../src/pull/attributes.rs) (schema/direction resolution,
validation, bounded attribute cursors), [execute](../../src/pull/execute.rs)
(public database methods and graph continuations),
[transform](../../src/pull/transform.rs) (whole-value transforms/defaults),
[entity](../../src/pull/entity.rs) (lazy navigation/cache/touch),
[control](../../src/pull/control.rs) (standalone and shared-query accounting), and
[index](../../src/pull/index.rs) (lazy index-Pull cursor).
[Entity identity](../../src/entity_identity.rs) remains a database-lineage value.
[EDN Pull](../../src/edn_pull.rs) admits syntax into these types; it does not
traverse a different database. Query selection/projection remains in
`query::{results,sequence}`; see the [query trace](02_query_reference.atomic.md).

## Paragraph-to-mechanism trace

Source symbols below refer to `datomic.pull` unless qualified. Checks identify
observable assertions, not merely tests that mention the same helper.

| Pro paragraph or example | Recovered mechanism and reason | Native owner and focused check |
|---|---|---|
| Pull introduction / Pattern: declarative map selected from a supplied database and identifier | `pull-1`, `pull`, `pull*` resolve once per selected entity and pass the same `db` to nested calls. No connection refresh is hidden in navigation. | `execute::{DatabaseValue::pull,pull_many}` and identifier resolution. `pull_api::pull_and_entities_retain_the_exact_database_value_and_reject_history`. |
| Grammar / Attribute Names / Map Specifications: names, option expressions, nested maps and recursion | `normalize-pattern`, `normalize-attr`, `normalize-recur-limit` validate and prepare selectors before reads; normalized-pattern-cache is a selector cache, not an entity cache. | `edn_pull::{pattern,attribute}` and `attributes::validate_pattern`. `pull_api::pull_rejects_duplicate_selectors_and_non_positive_limits`; EDN malformed-pattern tests. Native explicit numeric attribute IDs are an adaptation. |
| Reverse Lookup: leading underscore reverses a reference, except actual underscore-prefixed attribute names | `db/reverse-lookup?` checks the exact schema name; `fix-specs-for-underscore-prefix-attrs` repairs schema-independent normalization. | `PullDirection::SchemaResolved` defers EDN underscore resolution to `attributes::direction_parts`. `pull_api::leading_underscore_schema_idents_take_precedence_in_edn_pull_and_wildcard_overrides` requires exact literal data, wildcard alias override, unchanged explicit typed reverse and a portable program round-trip. |
| `:as` Option: the selected value appears under the requested key | `attr-with-opts->attr-tuple` builds keyfn separately from valfn; wildcard consults explicit forward specs. | `PullAttribute::alias`, `attributes::selector_selects_forward`. `pull_api::pull_aliases_accept_string_numeric_and_collection_keys` and the underscore override regression. Native general immutable keys are deliberate, not a claim about every host key class. |
| `:limit` Option: default 1000, positive explicit limit, nil removes the limit | `limit-default-from-map`, `limit-iterable`, `ea->v`, `ra->e` stop source consumption before nested projection. | `prepare_attribute` stops its controlled prefix cursor at the visible-value limit. `pull_bounded::{forward_limit_one_consumes_a_bounded_prefix_of_one_thousand_values,filtered_limits_count_visible_values_without_scanning_unrelated_suffixes,default_and_unlimited_limits_preserve_the_documented_result_prefixes}` checks work and values independently. |
| `:default` / `:xform`: transform sees nil; non-nil result wins; default follows only nil | `try-xform`, `attr-with-opts->valfn`, and valfn calls in `ea->v`/`ra->e` preserve the whole scalar/collection/map value and run even without a resolved entity. | `transform::finish_attribute`, `execute::unresolved_pull`. `pull_transforms::{transforms_see_nil_and_defaults_are_only_applied_afterward,native_builtins_and_whole_collection_nested_alias_transforms}`; `pull_api::unresolved_entities_still_apply_explicit_defaults_and_transforms` covers typed/EDN unresolved idents and lookup refs. |
| `:xform` host function names and exception handling | `try-xform` resolves allowed transforms, preserves cancellation and wraps other failures. This is not permission to run arbitrary JVM evaluation. | Native `PullTransform` and explicit `EdnPullTransforms` registry replace host namespace loading; callback panics/errors and cancellation have typed boundaries. `pull_transforms::transform_errors_panics_and_cancellation_do_not_escape_controls`. |
| Wildcard / Wildcard with Map Specification: all direct attributes, explicit selector overrides | `a-iter`, `AIter`, `next-a` seek to the next attribute rather than exhausting an unselected many-value tail; `pull*` consults explicit forward selectors during wildcard processing. | `attributes::next_pull_attribute` and execution selector dispatch. `pull_bounded::wildcard_limit_override_still_discovers_later_attributes_without_full_values`; `pull_api::wildcard_expands_components_and_recursion_is_cycle_safe`. |
| Component Defaults / Non-Component Defaults / Multiple Results | `default-spec` expands only forward component refs; other refs default to id maps. `ra->e` independently makes reverse components singular and other reverse refs vectors. | `prepare_attribute` keeps `multiple` separate from forward-only component expansion. `pull_api::reverse_components_default_to_one_id_map_and_expand_only_when_requested` requires a singleton id map and explicit nested parent expansion on typed and EDN paths. |
| Nesting / Recursive Specifications: finite depth, arbitrary depth and cycles | `pull*`'s `mk_xf` tracks visited entities and changes the selected recursive subpattern. Explicit nested patterns differ from repetition of the current selector. | Iterative `PullTask`/context in `execute` and per-selector `RecursionKey` in `control`. `pull_api::nested_paths_and_recursive_selectors_have_local_cycle_and_depth_state`; `pull_unbounded` and deep pattern/touch tests cover ownership as well as evaluation. |
| Empty Results / Missing Attributes: empty root map; omit unmatched attributes; retained empty nested result vectors | `nilify-empty` and nested `ea->v`/`ra->e` expose a source/prose discrepancy: recovered empties become nil and may be removed. | Native follows Pro's explicit empty-map/vector examples. `pull_api::pull_explicit_id_unknown_attributes_and_nested_empty_results`. Unknown numeric attribute IDs remain errors; unknown named attributes are missing selections. |
| Entity identifiers: numeric IDs, idents and lookup refs | `Db.entity` rejects history, uses `resolve-id`, and constructs `emap` without proving facts exist. `pull*` includes a resolved-or-nil `:db/id` only when requested. | `execute` and `database_value` identifier resolution. `pull_api::unresolved_idents_and_lookup_refs_retain_only_requested_nil_db_id` requires exact empty/plain and nil-id/wildcard shapes. Explicit defaults/transforms are still evaluated; wildcard does not invent schema attributes. |
| Entities / Basics: references navigate to entities; cardinality-many is a set-valued association | `query/eav` uses `ref-val`, returning an ident keyword when available for forward refs. `query/rae` directly calls `emap` for incoming entities, including identified parents; reverse components are singular. | `entity::read_direction` and `entity_navigation_value` preserve the direction distinction. `pull_api::{eager_entity_navigation_returns_entities_and_keeps_one_snapshot,filtered_entity_many_and_reverse_navigation_remain_set_valued,reverse_entity_navigation_keeps_identified_parents_as_entities}`. Pull maps remain different from lazy Entity values. |
| Entities / Laziness and Caching: access caches one attribute; keys do not cache values; touch follows components only | `EntityMap.valAt` memoizes successful `eav` reads; `seq`/`keySet` obtain keys through `get-lazy-entity`; `touch` follows only ref components. | `Entity::{get_direction,keys,touch}` uses a synchronized local cache and an iterative work stack. Missing-value memoization is a safe native adaptation on immutable input. `pull_transforms::explicit_pattern_ownership_and_touched_component_caches_are_stack_safe` verifies touched component caches; ordinary noncomponent neighbors stay lazy. |
| Entities / Entities and Time / Usage Considerations: navigation retains one exact time basis, not history | `Db.entity`, `EntityMap.db`, `eav`/`rae` retain the same Db and its `windowed` reads. `Db.{asOf,since,filter}` and speculative `with` derive values; history is a time-spanning view rejected by entity/Pull. | All navigation holds `DatabaseValue`, not a live peer. `pull_api::pull_and_entities_retain_the_exact_database_value_and_reject_history`, `pull_bounded::bounded_pull_preserves_old_current_temporal_views_and_rejects_history`, and index-Pull temporal/custom tests cover explicit boundaries. |
| Entity API class contract: equal IDs and database IDs imply equal entities | `EntityMap.equals`/`hashCode` use raw database identity plus eid, not basis, filter, cache or payload. | `Entity::identity` / `EntityIdentity` keep lineage separate from snapshot keys. `read_entity_identity::{identity_survives_values_and_cache_but_not_independent_memory_databases,postgres_entity_identity_survives_connections_recovery_and_writer_shutdown_without_io}`. Equality must not be used to infer equal historical contents. |
| Index-Pull / AVET / AEVT / Start: walk one attribute from an index position; third component is projected | `index-pull` validates cardinality/type, selects E for AVET and V for AEVT, then fences only on attribute. It does not globally deduplicate referenced entities. | `index::{DatabaseValue::index_pull_with_control,IndexPullCursor::next}`. `index_pull::{avet_walks_one_attribute_in_both_directions_with_directional_offset_limit,aevt_pulls_reference_values_and_preserves_documented_duplicates,index_pull_checks_index_start_schema_and_selector_even_for_empty_results}`. |
| Index-Pull introduction / Reverse: lazy maps, reverse walking, directional offsets | `index-pull` creates delayed maps over the same captured Db; `dereffed-index-pull` realizes them on consumption. Peer source has no Client default-result cap. | Native cursor defaults to unlimited results, adds explicit offset/limit and fuses errors. `index_pull::{projection_is_lazy_and_cancellation_covers_offsets_and_fuses_errors,cancellation_interrupts_rejected_index_candidates_before_exhaustion,temporal_and_custom_views_use_the_same_snapshot_for_walk_and_projection}`. |

## Controls, costs and deliberate boundaries

Standalone `PullControl` has depth, entity-visit and cancellation controls;
unlimited defaults are not a hidden recursion ceiling. Query Pull receives the
enclosing query's shared work, deadline and value-byte state through
`QueryPullBudget`. The `query_result_contracts` lazy Pull regression uses a
deliberately small byte maximum and a large attribute, so relational execution
can finish while consumption correctly fails. Reusing already-used bytes without
the original maximum was not equivalent accounting. Standalone Entity access and
`touch` do not claim that query budget; index-Pull's entity/depth limits apply to
each projection, while cancellation also covers scanning and offsets.

The index-Pull rejected-candidate regression is distinct from the older standalone
Pull test: polling only before outer `next()` misses a filter that consumes an
entire rejected range inside that call. The repaired controlled cursor checks
before/after candidate filtering and stops at the attribute fence before unrelated
attributes can run user filter code. Bounded visible prefixes do not promise a
fixed total candidate count when filters reject values.

Native stack-safe evaluation, pattern clone/drop, component touch and graph-cycle
termination are explicit safety adaptations, not a translation of recursive JVM
frames. Pro promises an id-only result at a recursive revisit; recovered `mk_xf`
instead retains its `:db/id` clause only when the pattern already had one. The
native documented id-only shape is retained. These decisions and the empty-result
discrepancy must remain visible rather than described as byte-for-byte parity.

Neither a cached entity nor a held database value implies a server round trip per
attribute. They also do not grant a storage-retention pin. Existing captured-value
and PostgreSQL tests check local immutable reads; GC/retention guarantees belong
to their owning storage/lifecycle contract. JVM collection interfaces, Clojure
dynamic reader/symbol resolution, Java string-key interop and `io-context` map
wrappers are host conveniences, not additional native engines to build.

## Focused verification

The new cases belong to existing `pull_api` and `index_pull` integration targets;
the EDN suite changes only the necessary parsed direction expectation. The
integration owner runs Cargo after module wiring is stable:

```sh
cargo test --test pull_api --test pull_bounded --test pull_transforms --test pull_unbounded --test index_pull --test read_entity_identity --test edn_query_pull --test query_result_contracts
python3 development/source/inventory.py --check
```

PostgreSQL-backed cases require the repository's real test database setup; a
skipped provider case is not equivalent to executing it. The source inventory
checks body preservation only, not semantic completion, performance parity or
first-release readiness.
