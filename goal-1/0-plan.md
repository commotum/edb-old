# Goal 1 — Native EDN data frontend

## Objective

Make Atomic directly usable with EDN transaction data, queries, query inputs and
pull patterns, including readable EDN results, through native Rust APIs and a
real application workflow. In particular, with the corresponding schema installed,
this text must create Alice without hand-building Rust transaction structs:

```edn
[{:person/name "Alice" :person/email "alice@example.com"}]
```

Implement a reusable data frontend, not a second database engine or a Clojure
evaluator. Preserve the existing typed Rust interfaces and route both interfaces
through the same transaction, query, pull and durability machinery.

## Status and integration

Implementation started 2026-09-10. The scaffold and actual source were reconciled.
Stage 1 is active; transaction and query adapters are being developed against its
shared value contract. The first stored/result conversion regressions passed
(3 tests, unoptimized); no complete stage is claimed yet.

The concurrently created Goal 0 is a documentation capability audit, not an
implementation parent loop. Its plan explicitly identifies Goal 1 as the
independent EDN owner and forbids duplicating this work. Both plans were read;
continue EDN implementation here, leaving the other session's audit files intact.
Do not mark its stages complete or expand this implementation into its audit.
These internal stages create no additional goal folders. Archived G1–G5 are
evidence, not active instructions. EDN completion does not imply audit completion.

## Verified starting point

- `src/transaction.rs` already has `EntityMap`, `MapValue::{Value,Nested,Many}`
  and `TxForm::EntityMap`, including symbolic/reverse attribute references.
  `tests/kernel_conformance.rs`, `tests/anonymous_identity.rs` and
  `tests/service_worker.rs` contain map semantics and authoritative submission
  regressions. The earlier claim that map transactions were absent was incorrect.
- Source/dependency inspection found no general EDN reader/writer or EDN
  transaction/query/pull adapters. `src/bin/atomic.rs` is currently an operational
  CLI, not an EDN transaction shell. Existing native text/debug output is not an
  EDN serialization contract.
- `src/value.rs` already provides keywords, symbols, exact numbers, UUIDs and
  millisecond instants. Its stored `Value` is not a general EDN value: it lacks,
  for example, standalone nil, characters and arbitrary maps/sets. Query and
  program value domains also differ. Do not conflate these domains.
- Primitive `TxOp` and transaction lookup-reference attributes currently use
  numeric IDs (`src/database.rs`), unlike map attributes. Symbolic EDN operations
  and schema-dependent vector meanings need a deliberate integration boundary;
  parsing text alone cannot supply that boundary safely.

## Authorities and reusable evidence

- Semantic authority: `datomic_pro_docs/02_core_concepts/01_programming_with_data_and_edn.md`,
  `04_transactions/02_transaction_data.md`, `03_schema/03_identity_and_uniqueness.md`,
  `05_query_and_pull/02_query_reference.md`, `05_query_and_pull/03_pull.md`, and
  `07_peer_api/01_java/09_peer.md` beneath `datomic_pro_docs/`.
- Format authority: the [EDN specification](https://github.com/edn-format/edn).
  Distinguish it from the [Clojure reader](https://clojure.org/reference/reader).
- Architectural evidence: `1.0.7705/peer/src-clj/datomic/query.clj`
  (`mapify-query`), `datomic/pull.clj` (`normalize-pattern`), `datomic/db.clj`
  (transaction expansion/identity), and `data_readers.clj` beneath that same
  `src-clj/` directory. Reuse the separation of reading from semantic processing;
  do not port JVM code execution or require exhaustive decompilation parity.
- Reuse `tests/tuple_input.rs`, `tests/ref_unique_identity.rs`,
  `tests/program_transactions.rs`, `tests/schema_information_repair.rs`,
  `tests/ident_query_pull.rs`, existing query/pull suites, and
  `examples/application_workflow.rs`. These are inspected starting points, not
  claims of newly passing checks.

## Essential constraints

- Rust and PostgreSQL remain the implementation. Standard EDN is required, not
  merely a JSON-like subset. Parsing data must not evaluate code. Clojure reader
  macros, namespace resolution and JVM constructors are not required; report
  unsupported syntax clearly. Treat Datomic-specific tags separately from EDN.
- Keep parsed EDN separate from stored facts, query values and program arguments.
  A readable value is not automatically a valid stored attribute value. Retain
  exact numeric representations; define conversion errors rather than silently
  stringifying, truncating or discarding unsupported input. In particular, handle
  instant precision/range and the stored Float/Double distinction explicitly.
- Preserve datom meaning, immutable database values, identity/upsert, schema as
  data, declarative unordered transaction semantics and all prior repairs.
  Resolve schema/idents/lookups against the correct immutable basis. A new
  transaction is assessed against authoritative db-before, not a stale peer's
  schema. Preserve existing rules for schema installation/use in transactions.
- Preserve existing canonical encodings, request digests and acknowledged receipts.
  Do not resolve a retried request afresh against changed schema or function
  bindings before checking its retained receipt. Define stable EDN request identity
  and formatting/map-order behavior; do not assume logically equivalent existing
  typed requests necessarily share a digest. Any necessary new input/transport
  representation must be additive/versioned without reinterpreting old requests.
- Keep work/admission limits effective through parsing, conversion, evaluation and
  printing. Fail safely on oversized, malformed or deeply nested input, including
  cleanup after failure. Report errors with useful locations/paths without
  indiscriminately logging transaction values. Do not add an unrelated service,
  alternate storage backend, arbitrary code runtime or general product rewrite.

## Ordered stages

### 1. Shared EDN reader and writer — in progress

**Outcome:** A public, reusable Rust EDN value model and reader/writer with a
tested format contract independent of any database connection.

**Focus:** Support nil, booleans, Unicode strings/characters, symbols/keywords,
integers including `N`, floating/exact `M` numbers, lists/vectors, arbitrary-key
maps, sets, `#inst`, `#uuid`, tags, comments, commas and discard. Offer explicit
Rust tag handlers and a defined unknown-tag policy; discarded data must not run
handlers. Preserve collection distinctions and use EDN equality for duplicate
keys/elements, not the engine's cross-numeric index equality. Provide single-value
reading with trailing-input validation and sequential reading for EDN streams.
Choose dependency reuse versus a small native implementation from actual coverage.

**Completion signal:** Reader/writer round trips and valid/invalid fixtures pass,
including non-string keys, collection/numeric equality, escapes, tags, discarded
forms, malformed/truncated input and resource boundaries. Precision is retained;
no evaluation or database access occurs. Record format extensions separately.

### 2. EDN transactions through the existing engine — not started

**Outcome:** Applications submit EDN maps or primitive forms and preview them
locally, with the same semantics as typed transactions and safe durable retries.

**Focus:** Adapt parsed data into the authoritative transaction pipeline. Cover
schema maps; optional `:db/id`; anonymous/string tempids; existing IDs, idents and
lookup refs; cardinality-many collections; reverse attributes; nested component
or uniquely identified entities; tuple/ref distinctions; current-transaction
metadata; adds, value-less/value-bearing retractions and existing built-ins/native
program calls. Do not invent JVM callback execution. Disambiguate collections and
references using transaction context/schema, not vector length alone. Reuse map
expansion and validation rather than maintaining separate EDN transaction rules.

**Completion signal:** The Alice example and schema installation work via public
Rust APIs and actual PostgreSQL-backed transaction submission. Permanent paired
EDN/typed tests prove equivalent reports/facts, identity unification, invalid
ownership/type rejection, tuple/many boundaries, omitted-attribute preservation
and preview isolation. Retry after restart, schema/ident changes and native
program rebinding returns the original receipt; changed input under the same
request key is rejected according to the defined identity contract. Existing
typed request hashes and old receipt behavior remain valid.

### 3. EDN queries, pull and result data — not started

**Outcome:** The same frontend exposes the existing peer-local query/navigation
capabilities without requiring users to construct native ASTs manually.

**Focus:** Support documented list/vector and map query forms, rules and explicit
inputs/sources, and pull pattern strings. Cover the current engine's corresponding
find shapes, aggregates, `:with`, bindings, joins, negation/disjunction, function
clauses, query pull and return maps. Cover pull wildcard, nesting, reverse refs,
recursion, options and registered native transforms. Reuse prepared queries and
their controls. Resolve extension names only through explicit native registries;
reading a symbol/list does not invoke arbitrary code. Encode results as data,
including nested maps and exact values; label any native-only tagged encodings.

**Completion signal:** Paired EDN/typed query and pull regressions agree over
current/history/as-of/since/speculative values and named database/raw-data sources.
Inputs and outputs retain their intended shapes and types. Existing supported
query/pull capabilities do not silently disappear at the EDN interface. Valid EDN
outside an adapter's semantic domain produces a specific conversion error; any
uncovered documented capability is recorded as a gap, not renamed "complete."

### 4. Usable application path and integrated acceptance — not started

**Outcome:** EDN is a documented, exercised product capability, not an isolated
parser or an example that bypasses the normal transactor.

**Focus:** Expose a practical file/stdin-driven executable path for transactions,
queries and pull using the library adapters; extend the existing CLI/application
support without creating a new server or requiring Rust source edits per request.
Keep credentials and read/write authority on existing paths. Include EDN results
and errors with useful exit behavior. Run relevant permanent regressions and the
real application workflow, including restart/reopen and retained retry evidence.
Measure representative increasing input/result sizes and large exact numbers:
separate parser/conversion costs from complete submit/query/pull/print costs,
and distinguish allocation accounting from measured process memory.

**Completion signal:** A documented runnable scenario installs schema from EDN,
creates Alice and related entities, updates Alice by identity without erasing
omitted facts, previews without committing, queries and pulls nested maps, checks
history, restarts and retries durably. It uses actual PostgreSQL and normal
application/transactor boundaries. Format, adapter, compatibility and integration
checks pass with commands/results recorded; skipped checks are not passes and
performance claims match measurements. Public documentation accurately separates
EDN syntax, Atomic semantics, explicit extensions and unsupported JVM behavior.

## Completion and continuation

Goal 1 finishes only when all four outcomes and the end-to-end scenario are
established. A parser, a successful Alice insert, or a scaffold alone is not
feature completion. Do not weaken required coverage to obtain a green status.

Current implementation boundary: a shared EDN value/reader/writer remains
independent of stored `Value`. Transaction EDN will retain a schema-independent
request representation until authoritative receipt-first processing; existing
typed encodings remain unchanged. Query and pull adapters reuse native engines.
Next: finish format/value conversion regressions, integrate the adapters, then
exercise executable and PostgreSQL acceptance. No runtime result is claimed yet.
