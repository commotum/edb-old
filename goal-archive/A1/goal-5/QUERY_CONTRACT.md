# Goal 5 query, pull, and API contract

## Recovered boundary

The native split follows `datomic.datalog`'s relations, binding-aware clause
scheduler and index-backed database relation; `datomic.query`'s parse/validate,
find-shaping, aggregate and cache boundary; and `datomic.pull`'s parsed selector
and recursive frame traversal. JVM protocols, array-specialized relation types,
dynamic Vars, reflection, and Clojure collection coercion become Rust enums,
owned rows, explicit control state, and immutable borrowed/`Arc` snapshots.

## Values, variables, and relations

- A query variable is a case-sensitive nonempty native name. A row is a partial
  variable-to-`Value` binding. Unification uses the database/query value
  equality relation (including numeric cross-representation equality), not
  Rust address identity or display text.
- Relations have set semantics. Duplicate complete rows are removed after each
  clause. `:with` adds variables to the pre-projection basis set; removing them
  may intentionally leave a bag for aggregation or relation output.
- Data patterns expose E, A, V and optional T/added components. Entity,
  attribute, and transaction identifiers use `Value::Ref`; keyword attribute
  constants resolve through the snapshot-local schema.
- Inputs are explicitly typed scalar, tuple, collection, relation, source, pull
  pattern, or rule set. Arity and destructuring mismatches are incorrect input,
  not empty results.
- Find shapes are relation, collection, tuple, and scalar. Relation results are
  unordered. Native vectors are deterministic test output only.

## Evaluation

- One request captures its sources as immutable database values. Every clause,
  nested rule, pull expression, entity lookup, and aggregate sees those exact
  values even if a `Peer` advances concurrently.
- The reference engine schedules only clauses whose required variables are
  available, matching recovered binding pushdown. It fails with
  `query/insufficient-binding` when no remaining clause can run.
- A data clause chooses EAVT, AEVT, AVET, or VAET from bound components but the
  choice is observationally invisible and differentially checked against a
  full EAVT scan. Covering datoms mean no PostgreSQL access during evaluation.
- `not` requires every body variable already bound; `not-join` requires only
  its declared join variables. `or` branches expose the same variable set;
  `or-join` exposes only its declared variables. Branch/body evaluation is set
  union/difference.
- Rule definitions with the same head are alternatives. Rule relations are
  grown to a duplicate-free fixed point, so cycles terminate. Declared required
  head arguments must be bound at invocation.
- Cancellation, deadline, intermediate-row, result-row, recursion, and work
  limits are explicit request controls and produce structured interruption or
  resource errors.

## Expressions and aggregates

- Built-ins are an allow-list of pure native operations. This goal includes
  comparison/equality, integer/double arithmetic, `ground`, `tuple`, `untuple`,
  `get-else`, `get-some`, and `missing?`; arbitrary Java/Clojure invocation,
  fulltext, randomness, log functions, and custom deployment are unsupported.
- Nonaggregate find elements form group keys. Aggregate arguments retain the
  multiplicity created by the `find + with` basis. `count` counts that bag;
  `count-distinct` removes stored-equal values. `min`, `max`, `sum`, and `avg`
  have explicit type/error behavior. Random aggregates are omitted because a
  deterministic native API cannot preserve their effect without an explicit
  random source.

## Pull and entity views

- Pull keys are schema keywords (or explicit aliases). Missing attributes are
  omitted unless a default is supplied. Cardinality-many values are
  collections and default to a 1,000-value limit; `None` means unbounded.
- Forward refs without a subpattern return `{:db/id ...}`; component refs
  recursively wildcard-pull. Reverse refs use VAET and are multiple unless the
  forward attribute is a component.
- Wildcard includes `:db/id`, all direct attributes, and recursively expands
  component refs. Explicit nested patterns recurse as requested. Revisiting an
  entity in recursive pull returns only `:db/id`, making unlimited recursion
  cycle-safe.
- Entity views retain their `Arc<Database>` and never follow a connection's
  future current value. Missing scalar attributes return none; cardinality-many
  and reverse navigation return deterministic native collections without an
  ordering promise.

## Native API decisions

The first public surface is a typed AST rather than EDN text. This removes a
Clojure-reader compatibility obligation while retaining the grammar's actual
semantic distinctions. `Database::query`, `Database::pull`, and immutable
`Entity` values are the local read boundary; `Peer` adds snapshot acquisition,
sync, and PostgreSQL transaction submission without hiding expected basis or
idempotency keys. Unsupported forms fail explicitly rather than being silently
misinterpreted.
