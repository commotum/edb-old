# Goal 5 supported surface and deliberate omissions

## Implemented

- Typed scalar, tuple, collection, and relation inputs; explicit immutable
  named database sources and current/history/`as-of`/`since` views.
- Three-, four-, and five-component data patterns with constants, blanks and
  variables; implicit joins; EAVT/AEVT/AVET/VAET bound-prefix selection; a
  forced-EAVT reference mode; plan steps and work/index statistics.
- Relation, collection, tuple, and scalar find shapes; set projection; `with`
  bag semantics; query pull expressions.
- Equality/order predicates and `missing`; integer/double arithmetic,
  `ground`, tuple/untuple, `get-else`, and `get-some` functions.
- `not`, `not-join`, `or`, `or-join`, multiple rule heads, required rule
  bindings, mutually usable rule relations, duplicate-free fixed-point
  recursion, and cycle termination.
- `count`, `count-distinct`, `distinct`, `min`, `max`, `min n`, `max n`, `sum`,
  `avg`, `median`, population `variance`, and population `stddev` aggregates.
- Typed pull attributes, wildcard, aliases, defaults, cardinality limits,
  forward/reverse navigation, explicit nested patterns, bounded/unbounded
  recursion, component defaults, cycle-safe `db/id` fallback, multiple pulls,
  immutable entity views, cancellation, depth, and entity-work limits.
- `Peer` snapshot/query/pull/entity/transact/sync composition with explicit
  expected basis and idempotency key.

## Deliberately unsupported in Goal 5

- EDN/list/map textual parsing, Clojure/Java collection coercions, return-map
  key/symbol/string decoration, and JVM API/wire compatibility. The typed Rust
  AST retains the semantic distinctions without embedding a Clojure reader.
- Arbitrary Clojure core or Java method calls, user namespace resolution,
  persisted/custom query code, and pull `xform`. Goal 6 owns controlled native
  and persisted programmability.
- `fulltext`, because no full-text schema/index contract exists; nested `q` and
  query access to the transaction log, because they require additional source
  and resource-accounting boundaries.
- Random `rand`/`sample` aggregates. Hidden ambient randomness conflicts with
  the native deterministic request boundary; a future API would require an
  explicit seeded source.
- Lazy `qseq`, asynchronous futures, automatic query-plan caching, and opaque
  JVM query-stats compatibility. Typed `Query` values are reusable, execution
  is synchronous/cancelable, and native plan/stats data is returned directly.
- A first-class nil query scalar. Optional tuple slots are retained in stored
  values, but binding such a nil slot fails explicitly until the value model
  gains a non-storage nil representation.
- Pull legacy syntax and textual attribute expressions. Their current options
  are represented directly by `PullAttribute`.

These omissions do not weaken the implemented forms: an unsupported operation
has no AST variant or returns a structured unsupported/incorrect error. They
are recorded to prevent the native surface from accidentally implying the
entire JVM host-language extension model.
