# Plan: Schema/vocabulary tooling

## Scope

- Schema/vocabulary tooling (versioned installs/migrations, ident aliases).
- Schema adapter and metadata for the algebrizer.

## Schema Adapter for Algebrizer (M2)

- Map EDB `attrs` rows to Mentat schema/attribute structures.
- Provide `HasSchema` and type/tag metadata.
- Unit tests on minimal schema.

Deliverable:
- `edb-schema::sqlite` loader + algebrizer-facing schema adapter (name TBD).

## Immediate Next Steps

1) Extend schema metadata for algebrizer needs (index/fulltext flags or equivalents, if required).

## Current Core Inventory

- Core schema types live in `CORE/edb-schema`.
- SQLite loader/alias resolution in `CORE/edb-schema/src/sqlite.rs` (feature `sqlite`) with tests in `CORE/edb-schema/tests/schema_load.rs`.
- Mentat-style `HasSchema` trait exists; `ValueTypeSet` provides basic type inference utilities.
- `ValueType::try_from(i64)` is the canonical attrs.vt mapping.

## Datomic Reference Files

- REFERENCE/datomic-reference/schema/1_schema.md — core schema shape and attributes.
- REFERENCE/datomic-reference/schema/2_changing_schema.md — migration and compatibility rules.
- REFERENCE/datomic-reference/schema/3_data_modeling.md — modeling patterns to encode into vocab tooling.
- REFERENCE/datomic-reference/schema/4_identity_and_uniqueness.md — idents/unique behavior that tool must enforce.
- REFERENCE/datomic-reference/transactions/3_transaction_data.md — schema is transacted data.
- REFERENCE/datomic-reference/operation/tutorial/3_assertion.md — practical schema install patterns.

## Mentat Reference Files to Reuse

- `REFERENCE/mentat/core/src/lib.rs`
- `REFERENCE/mentat/core/src/sql_types.rs`
- `REFERENCE/mentat/core-traits/lib.rs`
- `REFERENCE/mentat/core-traits/value_type_set.rs`
- `REFERENCE/mentat/core-traits/values.rs`
