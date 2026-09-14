# Atomic Semantic Contract v0

This document fixes the observable meaning of the first Rust implementation. It is read together with the executable reference in `src/` and `tests/semantic_conformance.rs`. It is not a PostgreSQL design or a production implementation plan.

## Authority and translation rule

1. `datomic_pro_docs` defines the intended public semantics.
2. `1.0.7705` is the default blueprint for boundaries, algorithms, invariants, and performance-sensitive structure.
3. The Rust design stays close to that blueprint unless the documentation conflicts, the recovered form is JVM/Clojure machinery, or a concrete Rust safety or clarity advantage warrants a recorded adaptation.
4. Recovered names remain useful evidence. Lack of byte/API compatibility is not permission to ignore them.

The recovered source was reconstructed from compiled artifacts. Compiler-expanded forms and generated names are evidence of control flow, not necessarily the original source expression.

## Evidence map

| Area | Documented contract | Recovered blueprint | Rust artifact |
| --- | --- | --- | --- |
| Information model and time | `02_core_concepts/00_datomic_data_model.md` | `datomic/db.clj`: `Datum`, `Db`, `with-tx` | `datom.rs`, `database.rs` |
| Value types and schema | `03_schema/00_schema_data_reference.md`, `03_schema/01_changing_schema.md` | `datomic/db.clj`: `Attribute`, `validated-v-for-attr`, tuple validators and schema hooks | `value.rs`, `schema.rs` |
| Transaction meaning | `04_transactions/01_transaction_model.md`, `04_transactions/02_transaction_data.md` | `datomic/db.clj`: `ProcessInpoint`, `ProcessExpander`, `get-ids`, `filter-assess-tx-datoms` | `Database::with`, `TxOp` |
| Transaction functions and validation | `04_transactions/04_transaction_functions.md`, schema predicates/specs | `datomic/builtins.clj`; `datomic/db.clj`: `ensure-tx`, `ensure-entity!` | normalized operation boundary and `TxOp::Ensure` |
| ACID boundary | `04_transactions/05_acid.md` | `datomic/db.clj`: pure `with-tx`; `datomic/log.clj` and `datomic/kv_store.clj`: publication/CAS | immutable `TxReport`; durability deferred to Goal 0 Stage 3 |
| Views | `02_core_concepts/02_database_filters.md` | `datomic/db.clj`: `asOfT`, `sinceT`, `history`, filtered index walks | `View` and replay fixtures |
| Index order | `06_indexes/01_index_model.md` | `datomic/db.clj`: `eavt-cmp`, `aevt-cmp`, `avet-cmp`, `raet-cmp` | `Datom::cmp_in` |
| Query | `05_query_and_pull/01_executing_queries.md`, `05_query_and_pull/02_query_reference.md` | `datomic/query.clj`, `datomic/datalog.clj`, `datomic/aggregation.clj` | contract below; evaluator deferred to Goal 0 Stage 5 |
| Pull | `05_query_and_pull/03_pull.md` | `datomic/pull.clj` | contract below; evaluator deferred to Goal 0 Stage 5 |
| Errors | `07_peer_api/02_shared_reference/01_error_handling.md` | `datomic/error.clj`, `datomic/anomalizer.clj` | `error.rs` |

Paths in the recovered column are beneath `1.0.7705/peer/src-clj/`; equivalent copies exist in the transactor corpus where the component shares the code.

## Values and identifiers

Atomic retains the documented scalar set: arbitrary decimal and integer, boolean, bytes, IEEE-754 float and double, millisecond instant, keyword, signed 64-bit integer, entity reference, Unicode string, symbol, tuple, UUID, and URI. Entity and transaction identifiers are opaque unsigned 64-bit values in the Rust model. Transaction identifiers are also entity identifiers.

The logical representation is independent of a future byte encoding. Any persistent encoding must be versioned and preserve every distinction named here.

### Comparison

`Value::index_cmp` is the only authoritative value comparator. PostgreSQL collation and Rust-derived enum ordering must never determine index or query semantics.

- All numeric types sort before non-numeric types and compare by mathematical value across representations.
- Finite floats are compared by their exact IEEE-754 rational value, not a formatted decimal approximation.
- Negative infinity sorts below finite numbers; positive infinity above them; NaN sorts above positive infinity. NaNs compare equal for ordering. Signed zero compares equal to exact zero.
- Supported non-numeric values have this stable rank: bytes, keyword, symbol, boolean, string, URI, instant, UUID, tuple.
- Strings and the textual parts of keywords, symbols, and URIs compare by UTF-16 code units, retaining Java string order rather than accidentally adopting Rust UTF-8 byte order.
- Bytes retain recovered length-first, then signed-byte lexicographic order. Rust gives them content equality, but bytes remain ineligible for uniqueness as the documentation requires.
- UUID order follows Java UUID's signed most-significant and least-significant halves.
- URI order is lexical over the retained URI text. This is an explicit stable native rule; URI syntax validation belongs at the API boundary.
- Tuples compare lexicographically. A missing tuple slot (`nil`) sorts below every value. Tuple length breaks an otherwise equal prefix.
- BigDecimal index comparison ignores scale, matching the recovered comparator. Stored-fact redundancy additionally requires equal scale, matching `equals-with-strict-scale` and the documented warning that applications should use consistent scale.

The numeric special-value rules are explicit native decisions where `datomic_pro_docs` warns about NaN but does not define a complete cross-type ordering. NaN is allowed as an ordinary float value but cannot participate in identity, uniqueness, or upsert.

### Bounds

- Top-level BigIntegers are limited to 8192 bits and BigDecimals to 1024 digits.
- Tuples have 2 through 8 slots. Tuple strings, BigIntegers, and BigDecimals are limited to 256 characters, bits, and digits respectively.
- Tuple slots may be nil. Bytes and nested tuples are not tuple scalar types.
- Instants are integer milliseconds since the Unix epoch.

## Datoms and indexes

A datom is `(entity, attribute, value, transaction, added)`. It is immutable. A retraction is new information; it never mutates the assertion it supersedes.

The four index orders are:

- EAVT: entity, attribute, value, descending transaction.
- AEVT: attribute, entity, value, descending transaction.
- AVET: attribute, value, entity, descending transaction; only indexed/unique attributes belong in the production AVET.
- VAET: referenced entity value, attribute, entity, descending transaction; only reference values belong in production VAET. The recovered internal name `RAET` maps to public `VAET`.

For an otherwise identical datom, an assertion sorts before a retraction. The reference model exposes the comparator for all values but does not yet restrict AVET/VAET membership because it is not the production index implementation.

## Schema and identity

- Schema is ordinary data about attributes in the universal relation.
- Every attribute has an immutable value type and a cardinality. Unique attributes must be cardinality-one and are indexed.
- `:db.unique/identity` enables tempid upsert; `:db.unique/value` only enforces a single holder.
- Lookup refs require a unique attribute and resolve against db-before. They cannot name an entity introduced in the same transaction.
- Idents are memory-resident names. Renaming preserves the old ident as an alias while the new ident is canonical.
- Component is meaningful only for references and controls recursive retract-entity and default pull traversal.
- `noHistory` is a future indexing/storage policy, not a promise of erasure.
- Current schema interprets historical database values. Time travel does not restore an older physical or interpretive schema.
- Value type, fulltext, tuple type/attrs, and tuple discontinuation are immutable after installation. Tuple discontinuation is one-way.
- Adding uniqueness requires cardinality-one, unique current values, and—when preserving the Pro operational rule—an available AVET index. Removing uniqueness affects subsequent transactions, not history.
- Attribute predicates installed in a transaction begin affecting the next transaction. Explicit entity specs validate requested entities against db-after.

Attribute resolution and value validation use db-before, as recovered `ProcessInpoint` calls `require-attr` on its input database. Therefore a newly installed attribute cannot also be used for domain datoms in that same transaction. Install it first, then transact its data.

## Transaction transition

The semantic function is:

```text
with(db-before, unordered transaction information, tx-instant)
    -> db-after + tx-data + tempid map
    | atomic structured failure
```

`Database::with` is the executable reference for the normalized transition. Its vectors and scans are intentionally not performance guidance.

The retained pipeline mirrors `1.0.7705`:

1. Expand maps and function calls into primitive information. Maps without `:db/id` receive anonymous tempids; cardinality-many collections fan out; nested reference maps require a component edge or their own unique identity; reverse attributes reverse E/V.
2. Resolve idents and lookup refs against db-before. Run all transaction functions against the same db-before and add all returned data to the one information set. No function observes another function's result.
3. Resolve tempids and unique identities. A tempid identifying multiple existing entities is a conflict. Fresh tempids asserting the same unique identity unify with one another. Allocate remaining entities deterministically.
4. Resolve reference-valued tempids, expand value-less retractions from db-before, and evaluate CAS against db-before.
5. Derive composite tuple datoms from affected constituent E/A pairs, filling missing constituents with nil and retracting the composite when all constituents are absent.
6. Assess the complete information set: schema, type, operation collision, cardinality-one collision, within-transaction uniqueness, installation, and redundancy.
7. Construct db-after, then apply requested entity specs and attribute predicates at their documented basis.
8. Add exactly one monotonic `:db/txInstant`, return only material datoms, and leave db-before unchanged.

The public parser for list/map forms and the sandboxed function runtime are later implementations. They must normalize to this contract; they do not get different transaction semantics.

### Settled collision rules

| Information in one transaction | Result | Evidence |
| --- | --- | --- |
| Same E/A/V/op repeated | Deduplicate | recovered `create-deduper`; documented redundancy elimination |
| Add and retract same E/A/V | Conflict | recovered `create-op-validator` keyed by E/A/V |
| Two different additions for one cardinality-one E/A | Conflict | recovered `create-card-one-validator` keyed by E/A/op |
| Retract old and add new cardinality-one value | Valid atomic replacement | complete-information-set model and recovered distinct E/A/op keys |
| Two entities add same unique A/V | Conflict | recovered `create-unique-value-validator` keyed by A/V |
| Two fresh tempids add same unique identity A/V | Unify before assessment | recovered `get-ids` in-transaction identity map |
| One tempid matches two existing entities through different identities | Conflict | documented upsert conflict; recovered `get-ids`/later uniqueness assessment |
| Value-less retract plus an addition | Retract only db-before values; do not retract the new assertion | transaction-data reference and db-before expansion |

Transaction input is semantically unordered. The reference model canonicalizes input before validation so both successful output and the selected error code are permutation-stable. This deliberately tightens recovered behavior where mutable hash-map encounter order can influence which equivalent error is reported; it does not change transaction acceptance.

### Composite identity

The recovered order is important: `get-ids` resolves explicit unique assertions before `generate-composites` derives new values. Consequently:

- An explicit composite unique-identity value on a tempid can act as an upsert hint. The direct composite datom is removed; stored composite values still come from constituents.
- A composite derived only after resolving a new tempid does not retroactively upsert that tempid. If the derived value is already held by another entity, uniqueness rejects the transaction.
- Installing a composite does not backfill existing entities. Reasserting a constituent, including a no-op assertion, triggers derivation.

These rules follow the recovered pipeline rather than inferring a stronger behavior from the general description of unique identity.

## Database values and time views

- Each successful transaction creates one immutable successor with `basis_t = previous + 1`; failure creates none.
- Transaction ids totally order transactions. `:db/txInstant` is monotonic and may be equal across transactions.
- Current view exposes facts true at the basis and retains the transaction that last asserted each current fact.
- `as-of(t)` replays all transactions through `t`.
- `since(t)` exposes the present result of only datoms whose transactions are after `t`; identity introduced before the cutoff may therefore be unavailable.
- `history` exposes assertions and retractions. Entity/map projection is invalid on history because it is not a point-in-time entity view.
- Combining `with` and `as-of` filters the speculative successor; it does not branch from the past.
- A future privacy excision operation may invalidate held pre-excision snapshots. The exact mechanism belongs to the lifecycle milestone, but silently continuing to expose excised values is forbidden.

## Query contract

The production evaluator must preserve these rules:

- A relation result is a set of tuples; row order is unspecified unless a future explicit ordering feature says otherwise.
- Data clauses unify repeated variables and joins are implicit through shared variables.
- Predicates filter bindings; functions extend bindings and are pure.
- Rules with the same head are alternatives. Recursive rules evaluate to a monotonic least fixed point with set deduplication.
- `not`/`not-join` are set difference with their required bindings. `or`/`or-join` are relation union with the declared joining variables.
- Projection normally removes duplicates. `:with` keeps additional variables through basis-set formation and removes them afterward, producing a bag for aggregates.
- Aggregates group by non-aggregate find elements. `count` counts bag entries; `count-distinct` and `distinct` use value semantics.
- Query clauses may be evaluated in a tuned order only when the rewrite preserves answers and failure semantics. No implementation may rely on incidental hash iteration order.

## Pull contract

- Pull projects a point-in-time entity graph; missing attributes are omitted unless a default is requested.
- Cardinality-one values are scalar and cardinality-many values are collections.
- Component refs recurse by default under wildcard; ordinary refs default to `:db/id` unless nested selection requests more.
- Reverse attributes traverse incoming references.
- Explicit recursion can be bounded or unbounded. Encountering an already visited entity returns only its `:db/id`, terminating cycles.
- Pulling an absent entity normally returns nil; a pattern that explicitly asks for `:db/id` can represent the unresolved identifier as documented by the API surface.
- Pull/entity projection is rejected on history views.

## Errors

Errors are structured and extensible: category, stable code, human message, and details. The native category vocabulary includes incorrect, forbidden, unsupported, not-found, conflict, fault, busy, unavailable, interrupted, and unknown-outcome. Service errors will additionally say whether retry is safe and whether commit is known, rejected, or unknown.

The semantic kernel emits incorrect for malformed or invalid information and conflict for a valid request incompatible with db-before or the complete proposed information set. Rust error sources replace Java exception-cause walking; arbitrary dependency failures must be converted at component boundaries rather than leaking unstable type names into the public contract.

## Deliberate adaptations

| Adaptation | Reason |
| --- | --- |
| Rust enums/newtypes instead of `Datum`, `Attribute`, Java boxed classes, Clojure maps and protocols | Retains the same roles with compile-time exhaustiveness and no JVM dispatch machinery |
| Explicit value comparator and type rank | Prevents Rust enum order or PostgreSQL collation from becoming persistent semantics while retaining recovered ordering where defined |
| Exact finite-number comparison | Stable cross-type behavior without Java numeric coercion accidents |
| Content equality for bytes, while still forbidding unique bytes | Idiomatic safe ownership; preserves the documented restriction without Java array identity |
| Lexical retained-text URI order | Stable native rule; exact `java.net.URI.compareTo` compatibility is unnecessary and would import Java-specific decomposition |
| Lexically deterministic tempid allocation and error selection | Transaction order is declared incidental; stable fixtures and retries should not depend on input/hash encounter order |
| Caller-supplied transaction instant in pure `with` | Separates the pure transition from the transactor clock; the service supplies and bounds the value later |
| Vector-scan reference state | Makes the model auditable; production indexes replace representation without changing results |

## Verification boundary

Run:

```text
cargo test --offline
```

The unit tests verify value and datom comparison laws. `tests/semantic_conformance.rs` verifies immutable successors, reified transactions, identity and upsert, lookup timing, collisions, cardinality replacement, CAS, entity specs, component retraction, composite derivation/upsert timing, monotonic transaction time, and current/as-of/since/history distinctions.

Not implemented or claimed here: a public EDN parser, query evaluator, pull evaluator, persisted function runtime, production indexes, PostgreSQL encoding/schema, transaction service, or distributed operation. Their semantics are fixed above where Goal 1 needs them; their construction belongs to later Goal 0 stages.

