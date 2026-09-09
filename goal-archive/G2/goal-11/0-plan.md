# Goal 11 — Schema and Idents as Database Information

## Objective

Replace the side-channel schema/ident model with ordinary immutable database
information. Bootstrap vocabulary, attribute definitions, general idents,
renames, aliases, and enumerated values must live as datoms in the same history
and indexes as application facts, while the current typed `Schema` becomes a
synchronously derived, validated acceleration structure.

## Constraints

- Follow `goal-9/EVIDENCE_LEDGER.md` C03 and the Goal 10 identity/valid-state
  contracts. `datomic_pro_docs` is semantic authority; recovered
  `BOOT-IDS`, `bootstrap-data`, `key-hook`, `install-attribute-hook`, and
  schema-alter hooks in `1.0.7705` are the default blueprint.
- Keep Rust throughout and PostgreSQL as the only durable store. Do not add a
  generic schema store or preserve the existing side channel as a second
  authority.
- Schema entities are open entities. `:db/ident` names general entities and
  enums, not just attributes; it resolves anywhere an entity identifier is
  accepted. Rename keeps old names as aliases.
- A transaction is still unordered and resolves ordinary attributes against
  db-before. Schema hooks synchronously derive and validate the proposed
  db-after before publication. A newly installed attribute is usable only in
  the following transaction.
- Temporal views expose historical schema datoms, but their operative typed
  schema remains the current database value's schema, matching the docs and
  recovered `Db.asOf`/`since` behavior.
- Canonical format v3 is the one-authority boundary. Version 1/2 mixed-schema
  payloads are rejected explicitly; existing Datomic/Atomic database
  compatibility is not required.
- Keep the reference kernel simple enough to inspect. Goal 13, not this goal,
  owns persistent-tree performance work.

## Starting context, now resolved

- `Schema` previously owned attributes and aliases independently of datoms and
  schema changes lived in `schema_history`. That vector and the public/durable
  `SchemaChange` channel are gone; `Schema` and `IdentIndex` are derived caches.
- `Schema::resolve_ident` previously named only attributes. `Database::entid`
  now derives general entity/enum aliases, while `EntityIdentifier` resolves
  eid, ident, and lookup-ref inputs for direct entity and pull APIs.
- Recovered bootstrap IDs include `:db/ident` 10, `:db/valueType` 40,
  `:db/cardinality` 41, `:db/unique` 42, `:db/isComponent` 43, `:db/index` 44,
  `:db/noHistory` 45, and `:db/txInstant` 50. Existing test schemas reuse many
  of these IDs with unrelated meanings, so adoption needs an explicit native
  bootstrap boundary rather than accidental overlap.
- Goal 10 now guarantees partitioned entity identity, strict issuance,
  successor-schema validity, structural ordering, and exact durable recovery.

## Material implementation decisions

- `Database::bootstrap()` is the exact native t=0 information set.
  `Database::new(schema)` remains only as Rust construction sugar: it calls
  bootstrap and installs the supplied application attributes through the same
  ordinary positive transaction path at t=1. PostgreSQL persists those two
  steps separately, so the convenience cannot become a second authority.
- The native genesis is the exact supported vocabulary, not Datomic's byte
  format or every unsupported JVM feature. Its IDs and information shape
  follow recovered `BOOT-IDS`/`bootstrap-data`; unsupported old native formats
  fail rather than being guessed into the new meaning.
- Typed install/alter syntax lowers to ordinary metadata set differences.
  Hook synthesis happens only after redundancy assessment. Following
  `attrs-missing-hooks` and the A=19 nonredundancy exception in recovered
  `db.clj:4290-4462,4854-4864`, no-op metadata, ident-only rename, and custom
  facts emit no hook, while every material alteration records its own repeated
  `:db.alter/attribute` history event.
- Goal 11 supplies the exact-database read identifier resolver and direct
  pull/entity entry points. Goal 14 owns routing lookup refs and exact temporal
  sources through every query function and query pull projection; that is
  query-evaluator work, not a second ident model.

## Stages

### 1. Bootstrap and derivation contract

**Status:** Complete.

**Outcome:** One compact executable contract fixes system IDs, minimal
meta-schema datoms, general ident behavior, cache derivation, schema transaction
timing, and temporal-view behavior.

**Focus:** Trace exact docs/source hooks; decide genesis representation and
attribute-ID width; add failing witnesses proving schema is queryable and that
the typed cache can be rebuilt from information alone.

**Completion signal:** Established by `CONTRACT.md` and the initially red, now
green `bootstrap_schema_is_ordinary_queryable_information` witness. The contract
ties exact IDs, t=0 genesis, cache derivation order, install/alter timing,
aliases, temporal interpretation, and the narrow composite-ident exception to
specific documentation and recovered-source locations.

### 2. Authoritative bootstrap datoms and derived caches

**Status:** Complete 2026-09-03.

**Outcome:** Every database starts from canonical system/schema datoms, and its
typed schema and ident indexes are rebuilt solely from the current information
set.

**Focus:** Recovered system identities; minimal supported schema vocabulary;
genesis transaction/base representation; schema entity openness; deterministic
derivation and corruption checks; removal of parallel authority.

**Completion signal:** `src/vocabulary.rs`, `src/idents.rs`, and the single
derivation path in `src/schema.rs` produce exact bootstrap/current caches.
`tests/schema_information_repair.rs` proves queryable exact genesis,
positive-t application schema, cache rebuild, malformed transition rejection,
explicit-index semantics, composite retarget protection, and native-schema
immutability. Recovery replays information and independently validates every
cache/current/history invariant.

### 3. General idents, enums, and aliases

**Status:** Complete 2026-09-03.

**Outcome:** Any entity may acquire a unique keyword ident, enums work as ref
values, and every retained rename alias resolves to the same entity in
transactions, query, pull, and entity navigation.

**Focus:** `:db/ident` uniqueness; E/A/V resolution; current ident choice versus
alias history; reverse attributes; lookup refs; query constants; canonical
results without rewriting an alias into a mismatching keyword.

**Completion signal:** `tests/ident_query_pull.rs` covers query E/A/ref-V alias
resolution and pull attribute aliases; `tests/entity_identifier_repair.rs`
covers eid/current-ident/historical-alias/lookup-ref entity and mixed pull
inputs; `tests/schema_temporal_repair.rs` proves basis-local/current-cache ident
semantics. A real-PostgreSQL witness in `tests/postgres_durability.rs` rebuilds
general idents, enums, attribute aliases, and typed schema from only genesis
plus ordinary log datoms after a new connection, and exact idempotent retry
receipts retain the rename transaction.

### 4. Schema transactions as ordinary history

**Status:** Complete 2026-09-03.

**Outcome:** Install, alter, rename, tuple, predicate, and supported schema
properties are represented by ordinary transaction datoms; hooks derive the
successor cache synchronously and the side-channel schema history disappears.

**Focus:** Map/primitive normalization; db-before resolution; install markers;
retractions/alterations; immutable properties; tuple generation metadata;
current-schema time views; schema queries and transaction reports.

**Completion signal:** Typed and primitive schema operations share ordinary
datom lowering and complete-successor validation. The focused hook-history unit
witness proves one install event, two distinct repeated alter events, as-of
visibility, no event for no-op/ident/custom facts, exact replay, and rejection
of missing/wrong/orphan hooks. `tests/schema_temporal_repair.rs` proves that
relations rewind schema datoms while interpretation uses the originating
database value's current schema and ident cache.

### 5. Durable migration and integrated closure

**Status:** Complete 2026-09-03.

**Outcome:** Canonical PostgreSQL log/base/backup representations contain one
schema/ident authority and reject obsolete mixed representations explicitly.

**Focus:** Encoding version and hash chain; manifest/base construction;
backup/restore; restart; corruption/fault injection; test-fixture migration;
parent-plan fold-back.

**Completion signal:** Format and warning-denying Clippy pass. The full suite
passes with 149 tests and one intentionally ignored subprocess-only worker.
The same full suite was run serially with a fresh PostgreSQL 15.11 URL and
explicit `pg_ctl`/data-dir controls, so PostgreSQL tests did not self-skip; it
observed process death, ambiguous retry, backup/restore, concurrency, and two
actual server restarts. Coherently rewritten index roots that disagree with
history, and a self-consistent manifest carrying the wrong authoritative log
hash, both fall back to log recovery. Migration 0006 rejects populated v2
catalogs and repeat migration is a tested no-op.

## Exit condition

Goal 11 completes only when schema and general idents are ordinary immutable,
queryable, recoverable database information; all typed schema/ident structures
are demonstrably derived; schema transaction timing matches the documented and
recovered hooks; and no side-channel representation remains authoritative.

**Status:** Achieved 2026-09-03. Query-evaluator source propagation and lookup
representation remain explicitly owned by Goal 14; no competing schema/ident
authority is deferred with them.
