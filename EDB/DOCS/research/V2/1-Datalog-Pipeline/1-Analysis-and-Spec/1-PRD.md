# A. Overview
This module provides a Datalog-style query pipeline with EDN parsing, a minimal EDN query executor, and an optional HTTP API for JSON-based querying and pull-style projections. The external behavior is defined by the EDN grammar it accepts, the query semantics it executes against a SQLite-backed store, and the JSON shapes it returns. The module is designed to be Datomic/Mentat-compatible where possible, but the current executor supports only a subset of the full query language.

The query pipeline is split into three observable surfaces:
- EDN parsing for values and query forms.
- In-process query execution that takes a database path and returns JSON results.
- HTTP endpoints for JSON queries and pull projections.

All query evaluation is against the current (non-history) database view. Time-travel views exist at the database layer but are not accepted as query inputs in the current public API.

# B. Glossary & Terminology (generic names)
- Attribute ident: A keyword string identifying an attribute, e.g. `:user/name`.
- Entity ID (EID): A signed 64-bit integer identifying an entity.
- Value: A typed scalar (string, integer, UUID, etc.) or EDN collection; see Data Model.
- Variable: A symbol starting with `?` that binds to a value.
- Placeholder: The symbol `_` which matches any value but does not bind.
- Pattern: A data clause of the form `[e a v]` with optional extra positions, used in `:where`.
- Binding set: A set of variable bindings produced by evaluating `:where` clauses.
- Relation/tuple/collection/scalar: The four `:find` result shapes.
- Pull spec: A structured description of which attributes to fetch from an entity and how to traverse refs.
- Reverse attribute: A keyword whose name starts with `_`, indicating reverse traversal.

# C. Public Surface (Canonical Names that must be preserved, if any)
In-process API names:
- `parse_value`
- `parse_query`
- `execute_edn`
- `execute`

HTTP endpoints (paths):
- `POST /q`
- `POST /pull`
- `GET /db`
- `GET /health`
- `GET /metrics`
- `GET /subscribe`

JSON field names for `POST /q`:
- `where` (top-level array)
- Clause fields: `a`, `op`, `value`, `ge`, `lt`, `le`, `gt`, `var`, `ref_var`
- `op` values: `eq`, `ge`, `gt`, `le`, `lt`, `between`, `has`
- Response field: `rows`

JSON field names for `POST /pull`:
- `eid`, `specs`
- Spec encoding: tagged objects with `type` and `data`
- `type` values: `Attr`, `Reverse`, `Nested`, `ReverseNested`, `NestedLimit`, `ReverseNestedLimit`, `AttrAs`, `AttrDefault`
- Spec object fields inside `data`: `attr`, `specs`, `limit`, `max_depth`, `alias`, `default`

EDN query keywords and tokens:
- `:find`, `:where`, `:in`, `:with`, `:keys`, `:strs`, `:syms`, `:order`, `:limit`
- `or`, `or-join`, `not`, `not-join`, `and`, `type`
- `pull`, `the`, `...`, `.`
- Variable prefixes: `?`, source var `$`, rules var `%`, placeholder `_`

EDN tagged literals:
- `#inst`, `#instmillis`, `#instmicros`, `#uuid`, `#f NaN`, `#f +Infinity`, `#f -Infinity`

Environment variables (HTTP server configuration):
- `EDB_SQLITE` (database path)
- `EDB_BIND` (bind address)

# D. Inputs & Outputs (by entry point)
## `parse_value(input)`
Inputs:
- `input`: UTF-8 string containing a single EDN value.

Outputs:
- On success, returns a structured EDN value (see Data Model).

Side effects:
- None.

Notes:
- Commas are treated as whitespace.
- `;` comments run to end of line.
- Tagged literals supported: `#inst`, `#instmillis`, `#instmicros`, `#uuid`, `#f NaN`, `#f +/-Infinity`.
- String escape support is limited to `\\`, `\"`, `\n`, `\t`, `\r`.

## `parse_query(input)`
Inputs:
- `input`: UTF-8 string containing a query in list form (vector) or map form.

Outputs:
- On success, returns a structured query object with:
  - `:find` specification
  - `:where` clauses
  - Optional `:in`, `:with`, `:order`, `:limit`, return map spec

Side effects:
- None.

Notes:
- Exactly one `:find` and one `:where` are required.
- Duplicate sections (e.g., repeated `:find`) are rejected.
- Map-form queries are normalized to list-form semantics before parsing.

## `execute_edn(db_path, query_text)`
Inputs:
- `db_path`: filesystem path to the SQLite database.
- `query_text`: EDN query text.

Outputs:
- JSON value with shape determined by `:find` (relation/tuple/collection/scalar).

Side effects:
- Reads the database and index files from disk.

## `execute(db_path, query_object)`
Inputs:
- `db_path`: filesystem path to the SQLite database.
- `query_object`: the parsed query object from `parse_query`.

Outputs:
- JSON value with shape determined by `:find` (relation/tuple/collection/scalar).

Side effects:
- Reads the database and index files from disk.

## `POST /q`
Inputs:
- JSON object with field `where`, an array of clause objects.
- Each clause object includes:
  - `a` (required): attribute ident.
  - `op` (optional, default `eq`): operator.
  - `value` (for `eq` or `ge` fallback): scalar JSON value.
  - `ge` (for `ge`/`between`): scalar JSON value.
  - `lt` (for `between`): scalar JSON value.
  - `var` (optional): variable name for binding entity IDs.
  - `ref_var` (optional): variable name to join via a ref-typed attribute.

Outputs:
- JSON object with field `rows`:
  - If no `ref_var` join is used, `rows` is an array of entity IDs.
  - If a `ref_var` join is used, `rows` is an array of `[parent_eid, child_eid]` pairs.

Side effects:
- Reads schema and index data from disk.

Semantics:
- Clauses are combined with logical AND (set intersection).
- `var` binds entity IDs; repeated use of the same `var` intersects results.
- `ref_var` performs a join from a previously bound parent variable through a ref-typed attribute.
- Operator requirements:
  - `eq` requires `value`.
  - `ge` uses `ge` if present, otherwise `value`.
  - `between` requires both `ge` and `lt` and uses a half-open range `[ge, lt)`.
  - `has` ignores value fields and returns entities that have the attribute.
  - `gt`, `lt`, and `le` are parsed but rejected at runtime.

Value encoding for `POST /q` inputs:
- Integer/ref/instant attributes accept JSON numbers or numeric strings (instants may also be RFC3339 `Z` strings).
- Floating-point attributes accept JSON numbers or numeric strings (including `NaN`, `+inf`, `-inf`).
- String/keyword/UUID attributes accept JSON strings.
- Big integer and decimal attributes accept JSON strings or integers.
- Bytes attributes accept JSON strings (raw bytes).

## `POST /pull`
Inputs:
- JSON object with:
  - `eid`: entity ID.
  - `specs`: list of pull spec items encoded as tagged objects.
    - `Attr`: `data` is an attribute ident string.
    - `Reverse`: `data` is an attribute ident string.
    - `Nested`: `data` is `{ \"attr\": <ident>, \"specs\": [<spec>...] }`.
    - `ReverseNested`: `data` is `{ \"attr\": <ident>, \"specs\": [<spec>...] }`.
    - `NestedLimit`: `data` is `{ \"attr\": <ident>, \"specs\": [<spec>...], \"limit\": <int|null>, \"max_depth\": <int|null> }`.
    - `ReverseNestedLimit`: `data` is `{ \"attr\": <ident>, \"specs\": [<spec>...], \"limit\": <int|null>, \"max_depth\": <int|null> }`.
    - `AttrAs`: `data` is `{ \"attr\": <ident>, \"alias\": <ident> }`.
    - `AttrDefault`: `data` is `{ \"attr\": <ident>, \"default\": <json> }`.

Outputs:
- JSON object mapping attribute keys to values (see Data Model).

Side effects:
- Reads schema and data from disk.

## `GET /db`
Inputs: none.
Outputs: JSON object `{ "t": <basis> }` where `<basis>` is the latest transaction sequence number.

## `GET /health`
Inputs: none.
Outputs: JSON object `{ \"ok\": true, \"t\": <basis> }`.

## `GET /metrics`
Inputs: none.
Outputs: JSON object with numeric fields:
- `tx_count`, `avg_tx_ms`, `last_t`
- `merges_total`, `compactions_total`
- `eavt_segments`, `aevt_segments`, `avet_segments`, `vaet_segments`
- `merge_ms_avg`, `compaction_ms_avg`

## `GET /subscribe`
Inputs: none.
Outputs: Server-sent events stream; each event's `data` field is a JSON string for a transaction report.

# E. Data Model (observable structures only)
## EDN values
Supported EDN scalars:
- `nil`, booleans, integers, floats, decimals (`M`), big integers (`N`), strings, keywords, symbols, instants, UUIDs.
- Instants accept RFC3339 with `Z`, `#instmillis`, or `#instmicros`.

Supported EDN collections:
- Lists, vectors, sets, maps.

## Query patterns and variables
- Variables are symbols beginning with `?`.
- Placeholders are `_`.
- Source variables are `$` or `$name` (parsed, but not executed).
- Rules variable is `%` (parsed, but not executed).
- Patterns are vectors of 3 to 5 elements: `[e a v]`, `[e a v tx]`, or `[e a v tx added]`.

## Find specification forms (syntax)
- Relation: `:find ?e ?v ...`
- Tuple: `:find [?e ?v]`
- Collection: `:find [?e ...]`
- Scalar: `:find ?e .`
- Elements MAY be variables, `pull` forms, aggregates, or `the` at parse time; only variables are executed.

## Query results (EDN execution)
`execute` and `execute_edn` return JSON:
- Relation: array of arrays `[[v1 v2 ...] ...]`.
- Tuple: array `[v1 v2 ...]` or `null` if no results.
- Collection: array `[v1 v2 ...]`.
- Scalar: a single JSON value or `null` if no results.

Scalar JSON mapping (EDN execution):
- Integer, ref, instant -> JSON number (instant is microseconds since epoch).
- Boolean -> JSON boolean.
- String and keyword -> JSON string (keywords include leading `:`).
- UUID -> JSON string.
- Decimal and big integer -> JSON string.
- Bytes -> JSON string from UTF-8 lossy decoding.

## Pull results
`POST /pull` and the in-process pull API return JSON objects keyed by attribute idents or aliases:
- Cardinality-one attributes map to a scalar JSON value.
- Cardinality-many attributes map to a JSON array.
- Reverse refs are keyed by `_<attr_ident>` (leading underscore) and map to arrays of entity IDs.
- Nested specs map to nested JSON objects or arrays of objects.
- Component attributes (cardinality-one ref marked as component) are expanded to nested objects by default when requested via `Attr`.

Scalar JSON mapping (pull):
- Integer/ref/instant/uint8 -> JSON number (instant is microseconds since epoch).
- Boolean -> JSON boolean.
- String/keyword/UUID -> JSON string.
- Bytes -> base64 string (no padding).
- Decimal and big integer -> JSON string.

## Query schema dependence
- Attributes MUST exist in the schema to be queried.
- Attribute value types drive input validation and encoding for value comparisons.

# F. State Machine / Lifecycle
## In-process functions
- `parse_value` and `parse_query` are pure and stateless.
- `execute` and `execute_edn` open a SQLite database and index files on each call and perform read-only queries.
- No explicit initialization or shutdown is required beyond ensuring the database file exists.

## Pull API lifecycle
- The pull API has configurable `limit` and `max_depth` parameters; default `max_depth` is 8.
- Pull requests do not mutate state.

## HTTP server lifecycle
- The server reads `EDB_SQLITE` at startup to locate the database and `EDB_BIND` to choose the listen address.
- Requests are handled by a single worker loop for database operations; effects are serialized.

# G. Error Model
## Categories
- Parse errors: invalid EDN or invalid query syntax.
- Validation errors: unsupported query features or invalid query shapes.
- Schema errors: missing or invalid attribute definitions.
- Index/storage errors: I/O failures, corrupt index data, or SQLite errors.
- Internal errors: unexpected failures in decoding or execution.

## Representation by entry point
- `parse_value`/`parse_query`: return a parse error with location info (line/column).
- `execute`/`execute_edn`: return a structured error with category and message.
- `POST /q` and `POST /pull`:
  - Invalid JSON payloads return HTTP 400.
  - All runtime errors return HTTP 500 with a plain-text message.

## Unsupported feature handling
The EDN executor MUST return an unsupported-feature error for any of the following:
- `:in`, `:with`, `:order`, return maps (`:keys`, `:strs`, `:syms`).
- `:limit` with a variable.
- `pull`, aggregates, or `the` in `:find`.
- `where-fn`, rules, or type annotations in `:where`.
- Non-placeholder `tx`/`added` positions in patterns.
- Source variables in patterns or predicates.

# H. Constraints, Invariants, and Determinism
## Query structure
- Queries MUST contain exactly one `:find` and one `:where` section.
- `:limit` MUST be a positive integer or `nil`.
- `:limit 0` MUST be rejected at parse time.
- `:where` MUST contain at least one clause.
- `:find` elements MUST be variables for EDN execution; other element forms MUST error.

## Pattern constraints (EDN execution)
- Attribute position MUST be a keyword ident.
- Entity position MUST be a non-negative integer, a variable, or `_`.
- Value position MAY be a variable, `_`, or a constant scalar (no `nil`, collections, or unqualified keywords).
- Reverse attributes (name starts with `_`) MUST be rewritten to forward form by swapping entity and value positions; if the value cannot be treated as an entity, the pattern MUST be rejected.

## Predicate constraints
- Supported operators are `=`, `!=`, `<`, `<=`, `>`, `>=`.
- Predicate arguments MUST resolve to numeric or string values; numeric values may be provided as numbers or numeric strings.
- Variables used in predicates MUST be bound by earlier clauses.

## Disjunction/negation
- `or`/`or-join` is evaluated as a union of independently evaluated arms, then joined against existing bindings.
- `not`/`not-join` requires all mentioned variables to be bound; otherwise it MUST error.

## Pull constraints
- Default `max_depth` is 8; once the limit is reached, nested traversal returns an empty object.
- `NestedLimit` and `ReverseNestedLimit` MAY override `limit` and `max_depth` per spec item.
- `AttrDefault` returns the default value when an attribute is absent; for cardinality-many attributes with no values, the default replaces the array.

## Determinism and ordering
- Result ordering is NOT guaranteed unless an ordering feature is explicitly supported (currently it is not).
- `:limit` truncates the unordered result set; which rows are kept is implementation-defined.
- Scalar/tuple results are derived from a single matching row; selection is implementation-defined when multiple matches exist.

# I. Edge Cases & Undefined/Implementation-Choice Areas (explicitly mark ambiguity)
- Attribute aliases in EDN queries are not guaranteed to resolve to canonical idents for index scans; behavior is undefined when aliases are used.
- Reverse refs for cardinality-many attributes in pull results may be incomplete; behavior is undefined.
- Bytes comparisons in EDN queries are not supported; providing bytes as constants is invalid input.
- EDN discard (`#_`) and general tagged elements are not supported.
- Unicode escape sequences in EDN strings are not supported.
- Numeric predicate comparisons on big integers/decimals may lose precision because comparison is performed via floating-point conversion.
- `or-join` and `not-join` explicit variable lists are parsed but not enforced beyond normal join behavior.
- In-process pull without a database path may return empty results for cardinality-many attributes and forward ref traversal.
- Instant strings in `POST /q` accept only `YYYY-MM-DDTHH:MM:SSZ` with no fractional seconds; other RFC3339 variants are rejected.
- `/subscribe` event payloads are JSON strings whose internal schema is implementation-defined.

# J. Security/Robustness Considerations
- The module performs no authentication or authorization; callers SHOULD enforce access control at a higher layer.
- Large inputs can cause high CPU and memory use; callers SHOULD apply size and time limits.
- Query execution reads local files; the process SHOULD be sandboxed where appropriate.
- No cancellation or timeout mechanism is provided by the query APIs; long-running queries may block callers.

# K. Conformance Test Plan (spec-derived)
## Must-pass examples (shape-focused)
- Parse EDN values: integers, floats, big integers (`N`), decimals (`M`), strings, keywords, UUIDs, and instants.
- Parse list-form and map-form queries; verify that missing or repeated `:find`/`:where` fails.
- Execute a query that binds an entity ID and returns a relation; verify an array of rows is returned.
- Execute a query returning a collection and scalar; verify output shape and `null` for no results.
- Use a placeholder `_` in a pattern and ensure it does not bind a variable.
- Use `or` to union two pattern arms; verify union of bindings.
- Use `not` to filter out bindings; verify that only bindings with no matches in the negated clause remain.
- Use a predicate filter with `>` and string comparison; verify filtering behavior.

## Negative tests
- EDN query using `:in`, `:with`, `:order`, return maps, `pull`, or aggregates MUST return an unsupported-feature error.
- EDN query with attribute position not a keyword MUST error.
- EDN query with unbound variables in `:find` or predicates MUST error.
- EDN query with `:limit 0` MUST fail parsing.
- `POST /q` with `op` set to `gt`, `lt`, or `le` MUST return an error.
- `POST /q` with `ref_var` on a non-ref attribute MUST return an error.
- `POST /q` with missing required fields for the chosen `op` MUST return an error.

## Property tests
- For any query, all returned bindings MUST satisfy all `:where` clauses and predicates.
- For a cardinality-one attribute, pull results MUST contain at most one value.
- For queries without ordering, reordering of results MUST NOT change set membership.

## Fuzzing strategy
- Fuzz EDN value parsing with random tokens; MUST not panic or hang.
- Fuzz EDN queries with random `:find`/`:where` forms; MUST return either a valid parse or a parse error.
- Fuzz `POST /q` payloads with random fields; MUST return 400 or 500 but not crash.
- Fuzz pull specs with deep nesting; MUST enforce `max_depth` and not overflow or panic.

# L. Open Questions / Decisions Needed
- Should EDN queries support `:in`, `:with`, `:order`, return maps, `pull`, aggregates, rules, and where-fn? If so, what are the exact semantics and output encodings?
- Should EDN queries resolve attribute aliases to canonical idents before scanning?
- Should bytes values be representable in EDN queries (e.g., base64) and, if so, how should they appear in results?
- Should `or-join` and `not-join` explicit variable lists be enforced?
- Should query results be distinct by default or preserve duplicates?
- Should the HTTP `/q` endpoint return named bindings instead of raw entity ID arrays?
- Should time-travel inputs (as-of/since/history) be supported by the query APIs?
