# ATOMIC-NOTE: transaction map → committed value → peer read

This is development commentary for [Transaction Data](02_transaction_data.md),
not part of the recovered Datomic documentation. It traces the Stage 1 map-form
pilot; the rest of this chapter is not yet covered. The original text and source
URLs remain in the reference chapters.

Baseline for all line coordinates below:
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`. Locate source by the named symbol;
added annotations can move current lines. Use `git show BASELINE:path` to recover
the recorded coordinates. The baseline identifies the recovered renderings we
inspected, not the authors' original source or a runnable Datomic distribution.

## Source and owner locators

| Locator | Artifact-qualified source and baseline symbols |
| --- | --- |
| S-DB | [transactor/src-clj/datomic/db.clj](../../1.0.7705/transactor/src-clj/datomic/db.clj): `nested-entity-map?` 5924; `has-unique-id?` 5951; `make-child-id` 6038; `expand-submap` 6058; `expand-map` 6159; `ProcessInpoint.inject` 6530; `get-ids` 6640; `ProcessExpander` 7361; `with-tx` 7892; `with-tx+opts` 7937. |
| S-DB-PEER | [peer/src-clj/datomic/db.clj](../../1.0.7705/peer/src-clj/datomic/db.clj): the map-expansion region at 5924–6228 has the same inspected control flow as S-DB with different generated local names. The files are not byte-identical; this correspondence does not assert whole-file equivalence. |
| S-UPDATE | [transactor/src-clj/datomic/update.clj](../../1.0.7705/transactor/src-clj/datomic/update.clj): `with-tx*` 1087; `process-transaction` 1102; `writer` 1658; `block-notifier` 1785. |
| S-PEER | [peer/src-clj/datomic/peer.clj](../../1.0.7705/peer/src-clj/datomic/peer.clj): `accept-new-data` 430; `Connection` 530, particularly `notify-data` 633; `db` 1733. |
| S-API | [peer/src-clj/datomic/api.clj](../../1.0.7705/peer/src-clj/datomic/api.clj): `q` 98; `transact` 233; `with` 311. These delegate; they do not themselves expand maps or establish durability. |
| S-QUERY | [peer/src-clj/datomic/query.clj](../../1.0.7705/peer/src-clj/datomic/query.clj): `q*` 1450 parses the query, takes explicit sources and calls `datalog/qsqr`; `q` 1522 applies the result transformation. This pilot traces the read boundary, not all Datalog algorithms. |

The current Rust owners are [edn_transaction.rs](../../src/edn_transaction.rs)
for EDN admission and schema-aware lowering,
[transaction.rs](../../src/transaction.rs) for typed forms, map normalization and
shared assessment orchestration, [transaction_input.rs](../../src/transaction_input.rs)
for input validation, and [tiered_assessor.rs](../../src/tiered_assessor.rs) for
identity resolution, the logical delta and successor validation. Durable
publication belongs to [storage/engine.rs](../../src/storage/engine.rs);
[connection.rs](../../src/connection.rs), [database_value.rs](../../src/database_value.rs)
and [edn_query.rs](../../src/edn_query.rs) provide the captured read value and query
boundary. These are present owners, not a claim that final module organization
is complete.

## TD-DATA: declarative input and expansion

Passages: [Transaction Data → Transaction Data](02_transaction_data.md#transaction-data),
baseline paragraphs at lines 57–65; [Assert and Retract](02_transaction_data.md#assert-and-retract),
the preprocessing explanation at 114–124;
[Programming with Data and EDN](../02_core_concepts/01_programming_with_data_and_edn.md),
opening paragraph and “Transaction EDN Example”; and
[Transaction Model → d/with and d/transact](01_transaction_model.md#dwith-and-dtransact).

**Observed mechanism:** S-DB `ProcessInpoint.inject` expands map input and feeds
the resulting forms into the same transaction processor. `ProcessExpander`
collects/resolves data; `with-tx` produces the before/after values and datoms.
S-UPDATE `with-tx*` uses that shared computation for durable processing.
**Documented rationale:** authoring order is not a sequence of externally
visible intermediate database states. The transaction model explains validation
of the complete information set against one `db-before`.

**Rust trace:** `read_edn_transaction` (111) retains admitted data as `TxForm::Edn`;
`lower_forms` (194) resolves its meaning against a supplied database value.
`normalize_forms_against` (1280) expands forms; `assess_forms_with_clock` (1051)
passes normalized operations to
`assess_tiered_with_remaining_limits_and_defaults` (442), which resolves tempids,
checks the combined delta and constructs a successor. `validate_forms_input`
(transaction_input.rs:119) guards shapes/depth before normalization; it is an
input-boundary check, not the transaction semantic engine.

**Existing checks:** [anonymous_identity.rs](../../tests/anonymous_identity.rs),
`allocation_skips_every_explicit_name_and_is_declarative_under_reordering` (124),
checks five distinct entities and reordered inputs. Its exact synthetic tempid
names are an Atomic convention, not Datomic API parity. Shared-engine comparisons
in this and other tests establish consistency, not an independent Datomic oracle.

## TD-MAP-ID: omitted and explicit entity identity

Passages: [Map Forms](02_transaction_data.md#map-forms), the first two explanatory
paragraphs at 142 and 151; [Entity ids](02_transaction_data.md#entity-ids), the
existing-entity example at 225–234; and the Anna transaction in
[Transaction EDN Example](../02_core_concepts/01_programming_with_data_and_edn.md#transaction-edn-example).

**Observed mechanism:** S-DB `expand-map` selects `:db/id` or an anonymous tempid,
passes that through `local-id`, and emits additions for the supplied attributes.
`get-ids` later handles permanent allocation and unique-identity unification.
Omitting `:db/id` therefore does not promise a newly distinct entity when an
identity assertion unifies with an existing one.

**Rust trace:** `Adapter::entity_map` (433) removes `:db/id` from attribute data;
`Adapter::entity` (392) admits IDs, idents, tempids and lookup references.
`Normalizer::expand_map` (1558) chooses the explicit ID or
`anonymous_tempid` (1516); the allocator avoids collisions with submitted names.
`resolve_tempids` (tiered_assessor.rs:1464) and `resolve_entity` (1942) perform
allocation/upsert and authoritative resolution. Numeric IDs must satisfy the
database's issued-ID rules; the reference's illustrative `42` is not a promise
that any arbitrary integer is valid in a fresh Atomic database.

**Existing checks/examples:** [people.edn](../../examples/edn/people.edn) omits IDs.
[edn_transactions.rs](../../tests/edn_transactions.rs),
`nested_reverse_and_anonymous_identities_share_the_typed_normalizer` (231), checks
explicit/anonymous separation and an unchanged predecessor;
`schema_disambiguates_tuples_lookups_and_many_reference_collections` (291) uses an
explicit lookup-ref `:db/id` and checks the resulting reference.
[kernel_conformance.rs](../../tests/kernel_conformance.rs),
`map_forms_normalize_to_primitive_forms_including_many_nested_and_reverse` (295),
also exercises a numeric map target, but its primitive-form comparison shares
the assessor and is not independent equivalence proof.

## TD-MAP-ADD: omitted attributes are not retractions

Passage: [Map Forms](02_transaction_data.md#map-forms), “shorthand for a set of
additions” and its primitive-add expansion, baseline 142–157. Preservation of
omitted attributes follows from that contract; the chapter does not separately
state an entity-replacement operation.

**Observed mechanism:** S-DB `expand-map` iterates supplied entries and emits
`:db/add`; it does not enumerate the entity's other attributes for deletion.
**Rust trace:** `Normalizer::expand_map`/`expand_map_value` iterate those entries
and emit `TxOp::Add`. `add_cardinality_one_retractions`
(tiered_assessor.rs:2128) retracts a previous value when an addition changes that
same cardinality-one attribute. It does not clear unrelated omitted attributes;
many-valued additions accumulate members unless explicit retractions are given.

**Independent expected result:** [edn_transactions.rs](../../tests/edn_transactions.rs),
`alice_maps_match_typed_facts_and_upsert_does_not_erase_omitted_attributes` (183),
asserts Alice's omitted name remains `Alice` and adding one tag preserves the
two existing tags. Its earlier typed/EDN equality check is supplementary.

## TD-MAP-MANY: collections and reference lookup boundaries

Passages: [Map Forms](02_transaction_data.md#map-forms), cardinality-many paragraph
and Bob aliases example at 159–166; [Nested Maps](02_transaction_data.md#nested-maps),
the parent-to-several-children reference collection.

**Observed mechanism:** S-DB `expand-map` enumerates a Java list only when the
forward attribute is cardinality-many, and enumerates sets; otherwise the input
is one value. This is why a single lookup-ref pair for a many-valued ref must be
inside an outer collection of references, while a cardinality-one lookup remains
one value. **Rust trace:** `Adapter::map_value` (484) uses schema cardinality/type
to retain that distinction before `Normalizer::expand_map_value` (1596) emits
the individual assertions. Tuple sequences must likewise remain distinguishable
from a collection of tuples.

**Independent expected results:** [edn_transactions.rs](../../tests/edn_transactions.rs),
`schema_disambiguates_tuples_lookups_and_many_reference_collections` (291), checks
two exact child references, wrapped many-ref lookups, and rejection of the
unwrapped ambiguous pair. [people.edn](../../examples/edn/people.edn) supplies
aliases and nested friends with [schema.edn](../../examples/edn/schema.edn).

## TD-MAP-NESTED: child expansion and the ownership constraint

Passages: [Nested Maps](02_transaction_data.md#nested-maps), the ref-attribute
paragraph, three-map/list-form expansions, and final ownership paragraph at
170–203. **Documented rationale:** the component-or-unique constraint prevents
accidental creation of entities with neither ownership nor a useful identity.

**Observed mechanism:** S-DB `nested-entity-map?` requires a reference attribute;
`expand-submap` emits the parent edge and recursively expands the child.
For a child without `:db/id`, `make-child-id` applies its component/identity guard.
For components it also records partition affinity. **Rust trace:**
`Adapter::map_value` constructs `MapValue::Nested`;
`Normalizer::expand_map_value` checks reference/ownership conditions, expands the
child and emits the edge, plus component affinity for newly allocated tempids.

**Independent expected result:** [edn_transactions.rs](../../tests/edn_transactions.rs),
`nested_reverse_and_anonymous_identities_share_the_typed_normalizer` (231), checks
the child's stored name, parent edge, rejection of an anonymous orphan, and the
unchanged predecessor. The many-friends example reaches the actual application
in the complete-path check below.

**Unresolved source/doc reconciliation:** the recovered source permits an
explicit child `:db/id` without calling `make-child-id`; Rust's forward nested-map
guard currently requires component-or-unique even when an explicit ID exists.
Also, S-DB `has-unique-id?` checks enum 38 (`:db.unique/identity` in `BOOT-IDS`),
whereas Rust `has_unique_attribute` (1738) accepts both `Unique::Identity` and
`Unique::Value`. The documentation says “unique” without this distinction.
These are observed branch differences, not conclusions inferred from names.
The next reconciliation must exercise (1) a noncomponent nested map naming an
existing ID with no unique attribute and (2) an anonymous noncomponent nested
map with only a unique-value attribute, then settle the documented/source-version
contract and add independent expected outcomes. The inspected tests above do not
cover those two boundary cases. No Datomic runtime was used for this trace.

The reference also spells the parent edge `:order/line-items` in its maps but
`:order/line-item` in its primitive example. Preserve this reference discrepancy;
an executable example must use one installed attribute consistently.

## TD-REPORT-READ: a result value, durability and a later query

Passages: [Entity ids](02_transaction_data.md#entity-ids), obtaining IDs from the
transact result (223); [Processing Transactions](03_processing_transactions.md),
“Submitting Transactions” and the “Monitoring Transactions” report-field table;
[Use DbAfter](../02_core_concepts/04_best_practices.md#use-dbafter-to-see-the-result-of-a-transaction)
and [Use a Consistent Db Value](../02_core_concepts/04_best_practices.md#use-a-consistent-db-value-for-a-unit-of-work);
[Executing Queries → Querying a Database](../05_query_and_pull/01_executing_queries.md#querying-a-database)
and “q”; [Query EDN Example](../02_core_concepts/01_programming_with_data_and_edn.md#query-edn-example).

**Observed mechanism:** S-DB `with-tx` returns the original and successor values,
datoms and tempid mapping without itself establishing durable publication.
S-UPDATE `process-transaction` applies it; `writer` calls `log/append` before
delivering the logged promise, and `block-notifier` waits on that promise before
sending transaction data. S-PEER `Connection.notify-data` accepts that data,
constructs the report and delivers the waiting result. S-PEER `db` captures a
database value; S-API `q` → S-QUERY `q*` evaluates explicit sources. A database
value, rather than a transaction authoring map, is the source for a database query.

**Rust trace:** `assess_forms`/`assess_durable_forms` share successor assessment.
`BlockTransactor::transact_captured` (engine.rs:790) resolves an existing receipt
before fresh lowering/assessment, persists the new objects/receipt and calls
`publish_refs` (994); a fresh `ServiceTransactionReport` is constructed only after
an applied publication result (1045). `Connection::db` (428) captures the peer
value. `BoundEdnQuery::execute` (edn_query.rs:159) executes against bound sources.
The Rust transport and exact-request receipt are native mechanisms, not claims
of Clojure future/JVM wire parity. Immutability means a captured value does not
change; it does not promise unlimited storage retention after reclamation.

**Independent expected results:** [block_transactions.rs](../../tests/block_transactions.rs),
`typed_edn_identity_and_exact_receipts_survive_new_writes_and_writer_reopen` (163),
checks the original report's before/after scores remain 1/2 while the writer
advances to 3, then checks the reopened exact receipt still describes 1/2.
[edn_transactions.rs](../../tests/edn_transactions.rs),
`postgres_socket_schema_maps_and_receipt_replay_precede_ident_resolution` (527),
checks the committed Alice value and exact retry after restart/ident repurposing.
Both require PostgreSQL; an early return for unset configuration is not acceptance.

## Existing complete-path acceptance target

[tests/edn_cli.rs](../../tests/edn_cli.rs),
`actual_edn_commands_install_transact_preview_query_pull_history_and_retry` (272),
uses disposable PostgreSQL fixtures and the actual CLI/socket. It installs schema,
submits the no-ID/nested-map people example, checks query/Pull results, proves a
preview does not commit, changes Alice's name while preserving aliases/friends,
joins old/current values with an explicit expected row, queries while the writer
is stopped and verifies the exact original receipt after restart.

```sh
cargo test --offline --test edn_cli actual_edn_commands_install_transact_preview_query_pull_history_and_retry -- --exact --nocapture
```

This Unix test requires `ATOMIC_POSTGRES_URL` and local process/socket support.
This companion records inspected code and test assertions, not an execution
result. Record any actual run and its prerequisites in the active goal's existing
verification record; do not convert this test inventory into a passing claim.

**Disposition from this pilot:** retain the shared normalizer/assessor and native
EDN application path, subject to the explicit nested-map reconciliation above.
Their information flow implements the main documented map-form path; reorganize
their ownership as part of the component cutover. This finding does not establish
full transaction, query, lifecycle or corpus coverage.
