# Plan: EDN + Full Datalog in EDB

Goal: Add EDN support for transactions and full Datalog queries, staying as close to Datomic/Mentat semantics as possible while remaining Rust/WASM-friendly.

## Decisions and Constraints

- Transaction syntax: EDN map-form and `:db/add`-style vectors (Datomic style).
- Query syntax: full EDN Datalog (Mentat-style `[:find ... :where ...]`).
- WASM constraint: avoid heavy runtime dependencies that are not WASM-safe; keep the EDN parser and query stack pure Rust.
- Compatibility target: Datomic/Mentat semantics where feasible; deviations must be explicit.

## Phase 0: Confirm EDN Type Semantics (Open Decision)

We need a Datomic-leaning mapping for non-core types and bytes/decimal behavior.

Options (choose one):

1) Datomic-like tagged forms (preferred for fidelity)
   - `#inst` for instants (already supported by Mentat EDN)
   - `#uuid` for UUIDs (already supported)
   - `#bigint` or `123N` for BigInt (already supported as `N`)
   - Proposed additions:
     - `#bigdec "1.23"` for Decimal
     - `#bytes "BASE64"` for Bytes

2) String conventions (lower parser complexity, lower fidelity)
   - Decimal and Bytes expressed as strings (e.g., `"1.23"`, `"BASE64"`) and coerced by schema.

Recommendation: pick (1) to match Datomic/Mentat expectations, but it requires extending the EDN grammar for `#bigdec` and `#bytes`.

## Phase 1: EDN Parser Integration (Mentat EDN)

1) Add a new crate/module (e.g., `edb-edn`) that depends on `REFERENCE/mentat/edn`.
2) Expose EDN parsing entry points:
   - `parse_value(edn_str) -> edn::Value`
   - `parse_query(edn_str) -> edn::query::FindQuery` (Mentat query AST)
3) If using tagged `#bigdec` / `#bytes`, extend `REFERENCE/mentat/edn/src/edn.rustpeg` and EDN Value types accordingly.

Deliverable:
- A small EDN parsing layer with tests verifying keyword, symbol, list, vector, map, set, `#inst`, `#uuid`, bigint, and any new tags.

## Phase 2: EDN Transactions in EDB

Add EDN-to-TxOp normalization that mirrors `edb-tx` JSON grammar but accepts Datomic forms:

Supported transaction forms:

- Vector ops:
  - `[:db/add e a v]`
  - `[:db/retract e a v]`
  - `[:db/cas e a expected v]`
  - `[:db/retractEntity e]` (optional; may expand to component cascade)
  - `[:db.fn/call fn-ident args...]` (optional; if we map to `tx-fn`)
  - `[:db/txInstant <inst>]` or `[:tx-meta {:db/txInstant ...}]`

- Map form:
  - `{:db/id <e>, :ns/attr v, :ns/ref {:db/id ...}, ...}`
  - Expand to Add ops using schema to coerce types.

Implementation steps:

1) Add `edb-tx` entry points:
   - `normalize_grammar_edn(db, alloc, temps, edn_values, txfns)`
2) EDN value conversions:
   - Integer, float, boolean, string, keyword, uuid, instants, bigint, decimal, bytes.
   - Use schema (`DbView.get_attr`) to coerce for ref/uuid/keyword/decimal/bytes.
3) Map-form expansion:
   - Treat `:db/id` as entity ref; expand other attrs to `TxOp::Add`.
   - Follow component semantics for nested maps (if component attribute).
4) Transaction meta:
   - Support `:db/txInstant` mapping to `tx-meta`.

Deliverable:
- `SqliteTransactor::apply_tx_edn(edn_str)`
- Server support for `Content-Type: application/edn` (or `/transact-edn`).

## Phase 3: Full EDN Datalog Queries

We want Mentat-style `[:find ... :where ...]` and pull in `:find`.

Approach options:

A) Integrate Mentat query pipeline (closest to Datomic semantics)
   - Parse EDN query to Mentat AST (`edn::query`).
   - Algebrize with Mentat schema and cached attributes.
   - Translate to SQL via Mentat query-sql.
   - Project rows using Mentat projectors.
   - Use Mentat pull (or adapt EDB pull) for two-stage pull projections.

B) Implement a smaller Datalog engine over EDB indexes
   - Use EAVT/AVET/AEVT/VAET to evaluate patterns.
   - Implement joins, predicates, and `:find` shaping manually.
   - More work and risk; less Datomic parity.

Recommendation: start with A to maximize compatibility and reduce semantic drift.

Integration outline for A:

1) Create an EDB-backed Mentat schema adapter:
   - Map EDB schema (`attrs` table) to Mentat `Schema` and `Attribute` shapes.
   - Provide `HasSchema` and any required type-tag mappings.
2) Provide Mentat DB adapter:
   - Implement Mentat query execution interfaces backed by EDB SQLite.
   - Ensure Mentat SQL builder uses EDB schema and value tags.
3) Pull integration:
   - Use Mentat `query-pull` if feasible; otherwise map Mentat pull AST to `edb-pull::AttrSpec` and execute with EDB puller.

Deliverable:
- `/q` endpoint accepts EDN query text and returns Mentat-shaped results.

## Phase 4: EDN Pull API

1) Parse EDN pull specs (Datomic syntax) into `edb-pull::AttrSpec`.
2) Support nested pulls, reverse refs, limit/max-depth where feasible.
3) Integrate with server:
   - `POST /pull` accepts EDN via `Content-Type: application/edn`.

## Phase 5: WASM Readiness

1) Ensure EDN parser and query pipeline avoid non-WASM dependencies.
2) Gate sqlite-specific code behind server/transactor crates; keep query planning logic portable.
3) Add WASM-compatible test harness for parsing and query planning.

## Phase 6: Tests and Validation

- EDN transaction parsing (map form + vector form).
- EDN query parsing and execution against a small schema.
- Pull in `:find` and direct pull API.
- Decimal/bytes tag coverage if implemented.

## Open Questions to Resolve Early

1) Decide decimal/bytes representation (tagged vs string conventions).
2) Choose EDN tx function mapping (`:db.fn/call` vs `tx-fn`).
3) Confirm whether to adopt Mentat query pipeline wholesale or integrate selectively.

## Immediate Next Steps

1) Confirm decision for decimal/bytes (Phase 0).
2) Implement `edb-edn` crate with Mentat EDN parser.
3) Add EDN transaction normalization in `edb-tx`.
4) Add server endpoint or content-type branch for EDN transact.

