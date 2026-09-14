# ATOMIC-NOTE: tutorial contracts and native application checks

This trace covers all eight tutorial pages, including the single-page memory
tutorial. They repeat the same end-to-end contracts rather than define eight
additional engines.

| Tutorial passage family | Recovered source / native owner | Existing focused check and disposition |
| --- | --- | --- |
| Memory getting-started, run a transactor, connect/create | `peer/connect-uri`, `connect-local-database`, `get-connection`; native application Connection and operations catalog | [native workflow example](../../examples/native_workflow.rs), [EDN CLI tests](../../tests/edn_cli.rs), [connection tests](../../tests/native_connection.rs). Native PostgreSQL install/roles/endpoint setup replaces the JVM memory URI and launch scripts; memory fixtures do not prove durable acceptance. |
| Transact schema and transact data, maps/tempids/report | `api/transact`, `db/expand-map`, `with-tx`; native transaction admission/expansion/assessment | [transaction-data trace](../04_transactions/02_transaction_data.atomic.md), [identity trace](../03_schema/03_identity_and_uniqueness.atomic.md), [EDN transaction tests](../../tests/edn_transactions.rs). Schema is database data, not DDL for SQL-backed datom tables. |
| Query the data: capture db then bind inputs | `Connection.db`, `query/q*`; native database_value/query | [execution trace](../05_query_and_pull/01_executing_queries.atomic.md), [typed/EDN query tests](../../tests/edn_query_pull.rs). Capture is independent of future connection advancement. |
| See historic data, original/retracted movie attributes | `Db.asOf/history`, `windowed`; native database_value temporal views | [filter trace](../02_core_concepts/02_database_filters.atomic.md), [time-point tests](../../tests/native_time_points.rs), [application example](../../examples/application_workflow.rs). Old captured values remain immutable but do not pin backing objects beyond native retention grace. |

The tutorial's named movie/person values and printed allocated IDs are examples,
not an allocation-format compatibility requirement. Native exercises use their
own deterministic expected values and test the same observable transitions.
Current file/stdin and CLI navigation are checked by
[documentation.rs](../../tests/documentation.rs); durable acceptance belongs to
the actual PostgreSQL tests and goal plan, not this prose table.
