# Datomic documentation capability audit

Initial complete static pass: 2026-09-10. **90/90 Markdown documents reviewed**, plus
28 supporting images/SVGs accounted for and the CSV manifest reconciled. This report
identifies missing/partial capabilities and explicit differences; it is not a test
execution report or a claim that all Datomic behavior is now covered by Atomic.

EDN reader/writer and transaction/query/Pull frontends are owned by
[Goal1](../goal-1/0-plan.md), actively changing during this audit. Existing typed
entity-map transactions are present. No product source, test, data or Goal1 files
were changed by this initial audit. Its scope was read-only; the later user request
authorized the implementation coordinator in [0-plan.md](0-plan.md).

## Implementation reconciliation — 2026-09-10

The catalog below preserves the initial static findings. Current ownership and
verified progress belong to the parent and child plans; this dated reconciliation
does not turn the historical audit into a test report.

| Findings | Current disposition |
| --- | --- |
| AO04, AO05, AO-C01, P04, AO07, AO-C03 | Verified/native disposition in Goal7: stock persistent standby, automatic runtime-role excision, safe cached-route recovery, bounded async operations/streams, health/readiness and supervisor-native packaging. Relevant actual PostgreSQL and separate application/crash/retry workflows pass; see the owning plan for evidence and limits. |
| P02 | Verified in Goal 6: idempotent catalog, identity-preserving rename, fenced retirement, bounded resumable reclamation, backup/pin protection and fresh identity on name reuse. Final29 active checks pass (26 actual isolated PostgreSQL), including concurrent writers, genuine schema33 upgrade, exact retries, historical programs and stock CLI/application; see its measured costs and retention boundaries. |
| Q01, Q05, ST-02 | Verified in Goal 5: grouped native aggregates, seven portable data functions, versioned Rust transaction/predicate deployments, stock EDN clients and exact speculation/retries. Final configured release acceptance 36/36 including genuine old-binary upgrade, durable ABI11 invocation, backup/restore and application/restart; see its measured costs and trusted callback contract. |
| Q02, Q04 | Verified in Goal 4: arbitrary-width relations, general typed/EDN values, pure callbacks, source-free CLI and persisted query bridge. Actual PostgreSQL/application, old ABI/receipt and reference-retention compatibility pass; see complete-path costs and explicit native semantics in its plan. |
| Q03, P01, P03, ST-05, P06 | Verified in Goal 2 with public PostgreSQL regressions, restricted-role application/restart workflow, existing EDN/program compatibility and unchanged old receipts. See its final acceptance for commands and measured costs. |
| ST-03, ST-01 | Verified in Goal 3: independent reserved allocation, native/retained-old million-ID histories, explicit defaults, application and old receipt/backup/upgrade compatibility. See its recorded artifact boundaries and complete-path costs. |
| SC-01 | Verified/documented in Goal 3: recovered 1.0.7705 ordering supports the existing explicit composite-upsert hint contract; no invented constituent-only upsert. |
| SC-02 | Verified in Goal 3: sound native NaN replacement retained, with permanent eager/native PostgreSQL regressions and documented JVM difference. |
| D01, D02, D03, AO06, Q06 | Verified/native equivalent in Goal8: attributable I/O, query and transaction diagnostics, bounded operational events/warnings and explained native scheduling. |
| P05, AO09, ST-04, AO10, AO11 | Verified in Goal9: lazy fixed backup db/log and offline EDN, authenticated persistent log cache, bounded advisory prefetch/index preparation and cooperative maintenance pacing/cancellation. Actual PostgreSQL/corruption/retry and selective64/8192-entity checks pass; measured limits remain explicit. |
| AO-C02 | Out of scope by the user's fresh-database policy: no mixed-version rolling-upgrade obligation. Current-version restart/failover/backup/restore remain required. |
| AO02, AO13, AO08, ENV02, TU-01, OC-C1 | Explicitly deferred optional gateway/console/shared-cache/AWS/memory-Connection/BI workflows; see the parent's decision table. No implementation is claimed and none blocks required completion. |
| TU-02, ENV01, ENV03 | Outside the retained native Rust/PostgreSQL scope: alternate durable stores, vendor topologies and JVM/runtime/wire/distribution parity. |

## Where to start

The most concrete next investigations are general relation-source arity (Q02),
custom aggregates (Q01), composable lazy query sources (Q03), and general query
values (Q04). For operations, prioritize catalog lifecycle (P02), automatic
excision execution (AO04), stock standby startup (AO05), and diagnostics (D01–D03,
AO06). **ST-03 merits an early boundary reproduction:** automatic schema/partition
allocation appears constrained by ordinary data growth; the explicit-low-ID
workaround and target-version semantics must be checked before calling it a bug.

Most foundational features already have substantive native implementations:
schema-as-data, maps/nested maps, idents/aliases/upsert, temporal values, entity
navigation, rules/joins/Pull, all listed built-in aggregates, raw indexes,
partitions/hints, fulltext, authenticated logs, backup/restore, SSD caching,
excision workers and fenced writer failover. A missing public integration or
option is not absence of its entire underlying subsystem.

## Consolidated catalog

Each ID appears once in the detailed findings. **Gap/partial** means the stated
public capability is absent or narrower in inspected source, not a newly failing
test. Optional features and scope decisions are labeled. **Candidate** means more
evidence is required. Priority is an initial user-impact judgment, not an approved
implementation order or a performance claim.

| ID | Remaining capability or difference | Disposition / priority |
| --- | --- | --- |
| [Q02](#q02) | Named relation-source patterns outside 3–5 columns | Gap; high |
| [Q01](#q01) | Custom grouped aggregates and richer aggregate arguments | Gap; high |
| [Q03](#q03) | Lazy qseq with tuple/log as well as database sources | Partial; high |
| [Q04](#q04) | General query input/constant/callback values, including nil/maps/collections | Partial; high; coordinate with EDN owner |
| [P02](#p02) | Logical database list/rename/delete/reclamation and idempotent create | Gap/partial; high |
| [AO04](#ao04) | Automatically process excision requests during indexing; supported operator entry point | Integration gap; high |
| [AO05](#ao05) | Launch stock executables as a persistent active/standby pair | Tooling gap; high; library standby exists |
| [D01](#d01) | Named per-operation cache/index/SQL I/O attribution | Implemented/verified2026-09-10; [Goal8](../goal-8/0-plan.md), typed/EDN contexts and actual cache/index reads |
| [D02](#d02) | Identifiable query clauses, binding sets, nested phases and warnings | Implemented/verified2026-09-10; [Goal8](../goal-8/0-plan.md), opt-in bounded capture and native scheduling |
| [D03](#d03) | Transaction-correlated semantic counts and timings | Implemented/verified2026-09-10; [Goal8](../goal-8/0-plan.md), ephemeral receipt/basis correlation and exact replay |
| [AO06](#ao06) | Operational logging, automatic metric publication and alarms | Implemented/verified2026-09-10; [Goal8](../goal-8/0-plan.md), bounded stock JSON/callbacks/warnings; no vendor backend or fabricated latency distributions |
| [P01](#p01) | Public per-attribute physical AVET readiness introspection | Exposure gap; medium |
| [ST-01](#st-01) | Configurable default partition | Missing setting; medium |
| [ST-02](#st-02) | Production registration of ordinary native Rust transaction functions/predicates | Partial; product extension-model decision |
| [ST-05](#st-05) | Schema-aware partial-tuple seek boundary authoring | Partial; medium; lower-level workaround exists |
| [P03](#p03) | Entity identity equality/collection-key contract | API gap; lower |
| [P04](#p04) | Completion listeners/Futures and nonblocking client operations | Partial; medium; pipelined submission exists |
| [P05](#p05) | Lazy backup-backed db/log reads with normal caches | Partial; medium; eager offline queries exist |
| [AO02](#ao02) | Thin remote read/query/Pull/log/index gateway and language-neutral clients | Missing optional client/service; larger scope |
| [AO07](#ao07) | HTTP health/readiness probes | Missing optional deployment tool |
| [AO08](#ao08) | Shared network immutable cache / Memcached | Missing optional cache tier |
| [AO09](#ao09) | SSD reuse for transaction-log payloads | Partial optional cache coverage |
| [ST-04](#st-04) | Configurable transaction-hint prefetch concurrency | Missing tuning control; measure before changing |
| [AO10](#ao10) | Backup/restore concurrency/pacing and GC pacing | Missing tuning controls; workload-dependent |
| [AO11](#ao11) | Configurable parallel index-sort/build work | Partial tuning capability; no speedup assumed |
| [AO13](#ao13) | Graphical schema/entity/history/index/query console | Missing optional tool; separate from EDN CLI |
| [TU-01](#tu-01) | Named memory-backed Connection/transact workflow without PostgreSQL | Optional workflow gap; in-memory semantic engine exists |
| [Q05](#q05) | Portable standard-function library vs all Clojure/Java functions | Explicit library/runtime scope choice |
| [Q06](#q06) | User clause-order tuning vs Atomic automatic scheduling | Intentional native equivalent; verified2026-09-10 in [Goal8](../goal-8/0-plan.md); actual schedule explained, optimizer preserved |
| [TU-02](#tu-02) | Standalone disk-backed development store | Unsupported optional backend; PostgreSQL-only scope choice |
| [ST-03](#st-03) | Automatic schema/partition IDs reaching range limits after ordinary data growth | High-priority candidate for boundary reproduction |
| [SC-01](#sc-01) | Constituent-only composite-identity upsert semantics | Unresolved docs/target-version candidate |
| [SC-02](#sc-02) | Replacing an existing NaN without a prior separate retraction | Unresolved semantic compatibility candidate |
| [P06](#p06) | Direct invocation by database-function identity at an exact basis | Public API candidate; native programs already execute |
| [OC-C1](#oc-c1) | SQL/BI analytics integration mentioned in release history | Optional candidate; complete analytics spec outside corpus |
| [AO-C01](#ao-c01) | Connection-owned remote writer rediscovery/reconnection | Integration candidate; explicit discovery/retry exists |
| [AO-C02](#ao-c02) | Mixed-version rolling upgrades | Unverified compatibility question; not a blanket missing claim |
| [AO-C03](#ao-c03) | PID-file and multi-database stock transactor packaging | Small packaging candidates; libraries can compose services |
| [ENV01](#env01) | Other durable backends, cloud topology and vendor provisioning | Explicit platform differences; TU-01/TU-02 cover local workflow |
| [ENV02](#env02) | S3/SSE backup, CloudWatch, S3 log rotation, AWS credential providers | Missing optional integrations; compatible with Rust in principle |
| [ENV03](#env03) | JVM libraries, runtime flags, Maven/private release distribution, Fressian/Transit | Explicit runtime/wire/distribution differences |

Input-bound Pull selectors are **not** an additional confirmed gap: Goal1 already
normalizes named EDN pattern inputs into native queries. Ordinary fixed online
snapshots, async transaction admission, eager backup queries and manual indexed
ranges are already expressible. These corrections prevent duplicate or inflated
backlog items.

## Per-file review

Each checked row means the file was read and compared to source/API/test evidence;
it does not mean all its semantics passed an executed test. Line anchors are from
the inspected working tree and should be refreshed before implementation. Paths
in evidence use the repository root unless a neighboring source path supplies the
prefix for a short `:line` reference.

### 00_start_here

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [00_start_here/00_introduction.md](../datomic_pro_docs/00_start_here/00_introduction.md) | Core immutable fact/schema/history/query/peer topology present ([src/datom.rs](../src/datom.rs), `database_value.rs:1750`, `service.rs:1494`, `query.rs:1021`, `persistent_tree.rs`). Catalog lifecycle, thin remote readers, shared cache and operational diagnostics are incomplete; cross-reference API/operations findings. Local/Cloud editions and alternate storage are explicit platform differences, not implied PostgreSQL parity. Topology diagram agrees with the text. |
| [x] [00_start_here/01_datomic_pro_releases.md](../datomic_pro_docs/00_start_here/01_datomic_pro_releases.md) | Release/distribution inventory, not an extra database-semantic requirement. HA and memcache claims map to operations findings. Atomic is source-local (`Cargo.toml:1-7`, `publish=false`); no claim of a published Datomic-style SDK/binary distribution. License/JVM/backend choices are platform/distribution differences. |
| [x] [00_start_here/02_datomic_pro_change_log.md](../datomic_pro_docs/00_start_here/02_datomic_pro_change_log.md) | Entire release chronology read. Current features (composite tuple discontinuation, reverse seek, read-only connections, backup listing, hints, stats, partitions, fulltext, qseq, pull transforms, specs, return maps, lookup refs, time APIs) cross-reference their owning folder audit. Historical dependency/security fixes are not inferred to be Atomic bugs. Additional leads: SQL analytics (lines 549-556), entity equality (1421), connection catalog caching (1410). Old interleaving bug notes remain potential validation inspiration, not newly proven defects. |
| [x] [00_start_here/03_release_notices.md](../datomic_pro_docs/00_start_here/03_release_notices.md) | Version-specific Java/Clojure/wire migrations and obsolete index/log formats are platform/history. Portable concerns have native owners: explicit migration/legacy checks ([src/postgres.rs:436](../src/postgres.rs:436), [tests/postgres_migration_boundary.rs](../tests/postgres_migration_boundary.rs)), integrity/rebuild ([src/operations.rs:342](../src/operations.rs:342), [docs/admin.md](../docs/admin.md)), excision history ([tests/operations_excision.rs](../tests/operations_excision.rs)), authenticated logs ([src/native_log.rs](../src/native_log.rs)). No evidence that Datomic's historical excision repair defect exists in Atomic; do not invent a requirement to ship its exact repair tool. |
| [x] [00_start_here/04_pro_setup.md](../datomic_pro_docs/00_start_here/04_pro_setup.md) | Explicit Rust transactor start/stop/provisioning present ([src/bin/atomic.rs:25](../src/bin/atomic.rs:25), [docs/application.md](../docs/application.md)). PostgreSQL-free included dev storage absent from supported Connection/CLI path (cross-reference tutorial finding); standalone semantic Database exists. JVM prerequisites and downloaded zip packaging differ. |
| [x] [00_start_here/05_accessing_the_peer_library.md](../datomic_pro_docs/00_start_here/05_accessing_the_peer_library.md) | Cargo/native Rust source usage is the language equivalent of adding Peer to a project. Maven/Lein/deps coordinates and Java interop are platform-specific. External release/package availability is not established by this repo ([Cargo.toml:7](../Cargo.toml:7)). |
| [x] [00_start_here/06_peer_language_support.md](../datomic_pro_docs/00_start_here/06_peer_language_support.md) | Rust native API present. General HTTP/REST data service and supported non-Rust SDKs absent from inspected public surface ([src/remote_transport.rs](../src/remote_transport.rs), [src/bin/atomic.rs](../src/bin/atomic.rs)); this overlaps thin-client operations gap but includes language-neutral HTTP access and self-describing endpoints. EDN parsing/printing is Goal1-owned; adding EDN alone does not supply an HTTP service. |

### 01_tutorials

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [01_tutorials/00_peer_mem_db_getting_started.md](../datomic_pro_docs/01_tutorials/00_peer_mem_db_getting_started.md) | **Partial TU-01.** The tutorial explicitly creates/connects/transacts against a named in-memory database without a separately running transactor. Atomic can bootstrap an immutable in-memory `Database`, transact schema/data using pure `with`/`with_forms`, and query/Pull/entity/history over it. It lacks the named mutable `Connection` lifecycle over memory using the ordinary transaction API. Map/EDN syntax is owned by goal-1; native transaction maps already exist. Docs `:11`, `:25`, `:49`, `:63`, `:75`, `:97`, `:205`, `:262`, `:391`; [src/database.rs:325](../src/database.rs:325), `:369`, `:378`; [src/transaction.rs:881](../src/transaction.rs:881); [src/query.rs:1554](../src/query.rs:1554); [src/connection.rs:80](../src/connection.rs:80), `:95`; [tests/semantic_conformance.rs:128](../tests/semantic_conformance.rs:128), `:284`, `:648`; [tests/database_value.rs:143](../tests/database_value.rs:143), `:272`. |
| [x] [01_tutorials/01_peer_tutorial.md](../datomic_pro_docs/01_tutorials/01_peer_tutorial.md) | **Navigation/platform prerequisites accounted for.** Links the six standalone tutorial steps and assumes Datomic/JVM library/REPL setup. Native Cargo dependency/examples and CLI are different platform entry points; lack of a Clojure REPL binary is not itself a missing database feature. TU-01/TU-02 capture portable storage/workflow differences. Docs `:13`–`:20`; [README.md:22](../README.md:22), `:79`; [docs/application.md:8](../docs/application.md:8), `:29`; all six linked local Markdown files reviewed below. |
| [x] [01_tutorials/02_run_a_transactor.md](../datomic_pro_docs/01_tutorials/02_run_a_transactor.md) | **Partial TU-02.** A real local native transactor executable exists, with local and TLS remote submission. However, Datomic's tutorial selects local disk-file `dev` storage, whereas Atomic's service and CLI require PostgreSQL. A Unix socket is a transport, and SSD files are caches, not this backing-store capability. Docs `:11`, `:13`, `:21`; [src/bin/atomic.rs:223](../src/bin/atomic.rs:223), `:242`, `:274`; [src/service.rs:1628](../src/service.rs:1628), `:1646`, `:1665`; [src/tree_store.rs:538](../src/tree_store.rs:538); [docs/application.md:14](../docs/application.md:14), `:59`. |
| [x] [01_tutorials/03_connect_to_a_database.md](../datomic_pro_docs/01_tutorials/03_connect_to_a_database.md) | **Present PostgreSQL equivalent; dev mode absent TU-02.** Named logical databases, explicit creation, connections, transactions and before/after/tempid reports exist. The URI containing a storage protocol is replaced by PostgreSQL configuration plus database ID; spelling alone is not a gap. The documented `dev://` backend is absent. The simple `:db/doc` entity transaction is expressible as an existing native entity map; EDN text is external. Docs `:30`, `:43`, `:54`, `:82`; [src/bin/atomic.rs:240](../src/bin/atomic.rs:240); [src/connection.rs:95](../src/connection.rs:95), `:184`, `:570`; [src/service.rs:269](../src/service.rs:269); [src/transaction.rs:40](../src/transaction.rs:40); [examples/native_workflow.rs:27](../examples/native_workflow.rs:27), `:51`, `:94`. |
| [x] [01_tutorials/04_transact_schema.md](../datomic_pro_docs/01_tutorials/04_transact_schema.md) | **Present typed equivalent.** Schema is ordinary transactional/queryable information; names, types, cardinalities and documentation facts can be submitted in entity maps/primitive forms, with complete reports. The broader automatic schema allocation limitation is ST-03 in the schema audit, not a tutorial-specific missing feature. EDN adapter work remains goal-1. Docs `:11`, `:17`, `:19`, `:71`; [src/database.rs:366](../src/database.rs:366); [src/transaction.rs:40](../src/transaction.rs:40), `:1390`; [src/schema.rs:1138](../src/schema.rs:1138); [tests/schema_information_repair.rs:11](../tests/schema_information_repair.rs:11), `:33`, `:172`; [examples/native_workflow.rs:55](../examples/native_workflow.rs:55), `:62`. |
| [x] [01_tutorials/05_transact_data.md](../datomic_pro_docs/01_tutorials/05_transact_data.md) | **Present typed equivalent.** Anonymous entity-map transactions can create several entities, allocate IDs, and return complete before/after/datoms/tempid reports. Input maps can be built/preprocessed in Rust. Native maps are not a new feature still waiting for EDN. Docs `:13`, `:17`, `:33`, `:37`; [src/transaction.rs:40](../src/transaction.rs:40), `:1334`, `:1348`, `:1390`; [src/service.rs:269](../src/service.rs:269); [tests/semantic_conformance.rs:128](../tests/semantic_conformance.rs:128), `:157`; [examples/native_workflow.rs:94](../examples/native_workflow.rs:94). |
| [x] [01_tutorials/06_query_the_data.md](../datomic_pro_docs/01_tutorials/06_query_the_data.md) | **Present typed equivalent.** Immutable database capture; Datalog find/where, variables, blanks, predicates and shared-variable joins; hierarchical Pull; and lazy entity navigation all exist. Omitted trailing pattern positions map naturally to `Term::Blank`. Textual query/map/list adapters are external. Tutorial prose says “two mechanisms” but lists three; that is a source-document wording error, not a feature gap. Docs `:13`, `:21`, `:37`, `:57`, `:93`, `:121`, `:143`; [src/query.rs:67](../src/query.rs:67), `:83`, `:149`, `:236`, `:1516`; [src/connection.rs:441](../src/connection.rs:441); [src/pull.rs:529](../src/pull.rs:529), `:563`, `:602`; [examples/native_workflow.rs:113](../examples/native_workflow.rs:113), `:115`; [tests/pull_bounded.rs:203](../tests/pull_bounded.rs:203); [tests/database_value.rs:364](../tests/database_value.rs:364). |
| [x] [01_tutorials/07_see_historic_data.md](../datomic_pro_docs/01_tutorials/07_see_historic_data.md) | **Present equivalent.** A cardinality-one replacement records a retraction and assertion, old captured values remain stable, newly captured values show the successor, and as-of/since/history all exist. Logical T, transaction EID and timestamp inputs have explicit native `TimePoint` variants. No additional missing historical-read capability found in this tutorial. Docs `:41`, `:71`, `:87`, `:107`, `:125`, `:143`; [src/time_point.rs:8](../src/time_point.rs:8); [src/database_value.rs:1799](../src/database_value.rs:1799), `:1808`, `:1878`, `:1886`, `:2053`; [src/connection.rs:441](../src/connection.rs:441); [tests/semantic_conformance.rs:284](../tests/semantic_conformance.rs:284), `:648`; [tests/native_time_points.rs:28](../tests/native_time_points.rs:28), `:70`; [tests/native_connection.rs:359](../tests/native_connection.rs:359); [tests/database_value.rs:272](../tests/database_value.rs:272). |

### 02_core_concepts

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [02_core_concepts/00_datomic_data_model.md](../datomic_pro_docs/02_core_concepts/00_datomic_data_model.md) | Immutable E/A/V/Tx/added values, schema-as-data, transaction metadata, aliases, uniqueness and basis-correct lookups implemented ([src/datom.rs](../src/datom.rs), `database.rs:1425`, `schema.rs`, `idents.rs:6`, `transaction_input.rs`). Typed entity identifiers are public (`database_value.rs`); coverage sources include [tests/schema_information_repair.rs](../tests/schema_information_repair.rs), `ref_unique_identity.rs`, `tx_instant_semantics.rs`. No new core-model absence found. |
| [x] [02_core_concepts/01_programming_with_data_and_edn.md](../datomic_pro_docs/02_core_concepts/01_programming_with_data_and_edn.md) | EDN reader, printer and adapters belong to Goal1, actively changing. Native maps already exist (`src/transaction.rs:27-62`). Keep EDN values separate from stored attributes: this overview's character/collection discussion is not proof that Datomic has a storable character/map attribute type; schema reference owns that list. Native query arbitrary-value limitations cross-reference query findings. |
| [x] [02_core_concepts/02_database_filters.md](../datomic_pro_docs/02_core_concepts/02_database_filters.md) | T/Tx/instant as-of/since, history, composed predicates receiving a database, and filtered speculative semantics implemented (`src/database_value.rs:1786-1808,1878-1889,2053-2082`; `tests/database_value.rs:189,272`, `filtered_speculation.rs`, `native_time_points.rs`, `query_exact_sources.rs:111,195`). No separate missing filter feature. Arbitrary Clojure entity-returning query expression maps to general query extension/value-domain limits, not a missing filter API. |
| [x] [02_core_concepts/03_entities.md](../datomic_pro_docs/02_core_concepts/03_entities.md) | Lazy same-basis forward/reverse navigation, component touch, per-instance cache, keys, empty entity and history rejection present (`src/pull.rs:529-785`, `database_value.rs:2074`). Mutex/Arc and Send+Sync filter contract support shared access; tests inspected, not executed. Both diagrams match native E/A/V/Tx representation and point-in-time projection. Identity equality trait is a separate narrow candidate; no claim that entity navigation is absent. |
| [x] [02_core_concepts/04_best_practices.md](../datomic_pro_docs/02_core_concepts/04_best_practices.md) | Native APIs support growth/aliases (`idents.rs:6-15`), transaction provenance, CAS, db-after, explicit instants ([tests/tx_instant_semantics.rs](../tests/tx_instant_semantics.rs)), in-flight submission tickets (`connection.rs:526`, `service.rs:1112`), parameterized queries/cache (`query_prepare.rs`), collection bindings, blank terms, pull, exact temporal sources and log (`native_log.rs`). Host runtime class/method syntax is not required for equivalent Rust callbacks. Remote async/diagnostic limitations cross-reference API findings. No blanket 'pipelining missing' claim. |
| [x] [02_core_concepts/05_glossary.md](../datomic_pro_docs/02_core_concepts/05_glossary.md) | Vocabulary coverage maps to owning chapters. Fressian (`src/encoding.rs:30-35` native ATMC binary format), cloud hash rings/EFS/Ions, JVM REPL and vendor services are explicit differences. Missing shared memcache, backup S3 and metrics exporters map to operations findings. Glossary entity/partition/transaction/log concepts have native representations. |

### 03_schema

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [03_schema/00_schema_data_reference.md](../datomic_pro_docs/03_schema/00_schema_data_reference.md) | **Mostly present; partial extensibility and allocation; two semantic candidates.** Native schema-as-data covers all documented stored scalar types, three tuple kinds, composite derivation/discontinuation, cardinality, uniqueness, components, noHistory, fulltext, predicates, and entity ensures. Automatic schema allocation can hit an entity-frontier cap before the documented schema-element count (ST-03). Host-language predicate deployment is limited (ST-02). Composite upsert wording and NaN replacement need version-specific reconciliation (SC-01/SC-02). Docs `:66`, `:259`, `:302`, `:396`, `:455`, `:509`, `:559`, `:669`, `:677`; [src/schema.rs:28](../src/schema.rs:28), `:48`, `:840`, `:950`, `:1084`; [src/fulltext_analysis.rs:7](../src/fulltext_analysis.rs:7); [src/tiered_assessor.rs:2144](../src/tiered_assessor.rs:2144); [tests/schema_information_repair.rs:11](../tests/schema_information_repair.rs:11), `:172`; [tests/tuple_schema_repair.rs:17](../tests/tuple_schema_repair.rs:17), `:181`; [tests/program_transactions.rs:152](../tests/program_transactions.rs:152). |
| [x] [03_schema/01_changing_schema.md](../datomic_pro_docs/03_schema/01_changing_schema.md) | **Present equivalent.** Ident renames retain aliases; old names can be repurposed. Mutable cardinality/components/history/index/uniqueness changes use schema hooks and current-fact validation; immutable types/fulltext/tuple definitions and irreversible discontinuation reject changes. As-of retains current basis schema. AVET readiness has an explicit synchronization path. New automatic schema additions inherit ST-03. Docs `:9`, `:13`, `:17`, `:34`, `:62`, `:75`, `:88`, `:100`; [src/schema.rs:292](../src/schema.rs:292), `:323`; [src/database.rs:3494](../src/database.rs:3494); [src/connection.rs:507](../src/connection.rs:507); [tests/schema_information_repair.rs:89](../tests/schema_information_repair.rs:89), `:254`, `:302`; [tests/semantic_conformance.rs:731](../tests/semantic_conformance.rs:731); [tests/avet_schema_semantics.rs:106](../tests/avet_schema_semantics.rs:106); [tests/postgres_peer.rs:1249](../tests/postgres_peer.rs:1249), `:1345`; [tests/filtered_speculation.rs:130](../tests/filtered_speculation.rs:130). |
| [x] [03_schema/02_data_modeling.md](../datomic_pro_docs/03_schema/02_data_modeling.md) | **Present equivalent.** Ordinary `:db/ident` assertions define enum entities, and ref values resolve known idents. No separate enum storage type is needed. Docs `:9`; [src/transaction.rs:19](../src/transaction.rs:19), `:1475`; [src/database_value.rs:2793](../src/database_value.rs:2793); [tests/schema_information_repair.rs:89](../tests/schema_information_repair.rs:89); [tests/entity_identifier_repair.rs:35](../tests/entity_identifier_repair.rs:35); [tests/ref_unique_identity.rs:98](../tests/ref_unique_identity.rs:98). |
| [x] [03_schema/03_identity_and_uniqueness.md](../datomic_pro_docs/03_schema/03_identity_and_uniqueness.md) | **Present equivalent with shared semantic candidates.** Entity IDs, idents, lookup refs, unique identity upsert, unique-value rejection, ref-valued uniqueness, partition extraction, squuids and UUIDv7 all have native representations/helpers. Lookup refs resolve db-before. Composite wording inherits SC-01. Structural tempid objects are legacy API spelling; strings plus force/match directives provide current placement capability. Docs `:20`, `:73`, `:97`, `:103`, `:113`; [src/identity.rs:23](../src/identity.rs:23), `:43`; [src/uuid.rs:44](../src/uuid.rs:44), `:70`, `:76`; [src/database_value.rs:2793](../src/database_value.rs:2793); [tests/semantic_conformance.rs:157](../tests/semantic_conformance.rs:157), `:187`, `:227`; [tests/ref_unique_identity.rs:98](../tests/ref_unique_identity.rs:98), `:304`; [tests/entity_identifier_repair.rs:35](../tests/entity_identifier_repair.rs:35). |

### 04_transactions

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [04_transactions/00_transactions.md](../datomic_pro_docs/04_transactions/00_transactions.md) | **Navigation accounted for.** Introduces the subordinate chapters; no independent feature requirement. Docs `:10`; all eight subordinate transaction files reviewed below. |
| [x] [04_transactions/01_transaction_model.md](../datomic_pro_docs/04_transactions/01_transaction_model.md) | **Present core model; extensibility partial ST-02.** Native `with`/`with_forms`, immutable before/after values, declarative expansion, stable normalization, db-before transaction programs, and complete db-after entity predicates exist. Broad claims about arbitrary Clojure/Java business logic exceed the committed native VM/callback boundary. Docs `:30`, `:42`, `:64`; [src/transaction.rs:879](../src/transaction.rs:879), `:908`, `:1175`; [src/program_bindings.rs:146](../src/program_bindings.rs:146); [src/tiered_assessor.rs:478](../src/tiered_assessor.rs:478); [tests/semantic_conformance.rs:128](../tests/semantic_conformance.rs:128), `:253`, `:377`, `:434`, `:789`; [tests/program_transactions.rs:287](../tests/program_transactions.rs:287). |
| [x] [04_transactions/02_transaction_data.md](../datomic_pro_docs/04_transactions/02_transaction_data.md) | **Present typed forms; EDN externally owned.** Primitive add/retract (including value-less retract), redundancy elimination, anonymous/entity/nested/many maps, ref resolution, tempids, current-tx annotation and explicit transaction time are implemented. Map nesting enforces component or unique identity rules. Function deployment limitation ST-02 and automatic schema/partition allocation ST-03 apply. Docs `:77`, `:128`, `:136`, `:166`, `:201`, `:242`, `:312`, `:355`, `:374`; [src/transaction.rs:19](../src/transaction.rs:19), `:40`, `:52`, `:1390`, `:1514`; [tests/semantic_conformance.rs:227](../tests/semantic_conformance.rs:227), `:316`; [tests/tx_instant_semantics.rs:30](../tests/tx_instant_semantics.rs:30), `:150`; [src/service.rs:262](../src/service.rs:262), `:269`. |
| [x] [04_transactions/03_processing_transactions.md](../datomic_pro_docs/04_transactions/03_processing_transactions.md) | **Present equivalent.** Async admission tickets plus blocking waits, timeout/unknown-outcome errors, exact before/after transaction reports, subscriptions/queues and queue removal exist. Rust tickets/channels substitute for futures/core.async. Database transaction datoms can become query relation inputs through the ordinary query API. Docs `:11`, `:19`, `:23`; [src/service.rs:269](../src/service.rs:269), `:1112`, `:1204`, `:1212`, `:1325`, `:1351`; [src/connection.rs:526](../src/connection.rs:526), `:570`, `:581`, `:598`; [tests/query_runtime_sources.rs:732](../tests/query_runtime_sources.rs:732); source timeout comments at [src/service.rs:1332](../src/service.rs:1332). |
| [x] [04_transactions/04_transaction_functions.md](../datomic_pro_docs/04_transactions/04_transaction_functions.md) | **Partial ST-02.** Stored native programs are real temporal database functions: `:db/fn` binds deployed hashes, calls resolve against db-before, nested calls emit forms, and structured cancellation is preserved. CAS, entity retraction and force/match exist. Committed arbitrary Rust/classpath-equivalent deployment is absent; VM implementation is not direct Clojure/Java function execution. Docs `:49`, `:63`, `:109`, `:127`, `:152`, `:162`, `:212`, `:231`; [src/program_bindings.rs:115](../src/program_bindings.rs:115), `:165`, `:198`; [src/program.rs:2362](../src/program.rs:2362); [src/transaction.rs:700](../src/transaction.rs:700), `:1191`; [tests/program_transactions.rs:152](../tests/program_transactions.rs:152), `:287`, `:399`, `:625`; [tests/semantic_conformance.rs:340](../tests/semantic_conformance.rs:340), `:526`. |
| [x] [04_transactions/05_acid.md](../datomic_pro_docs/04_transactions/05_acid.md) | **Present PostgreSQL implementation; not runtime-certified here.** Shared transaction assessment, locked durable head, lease/epoch fencing, SQL commit and immutable peer adoption substantiate the ACID design. Cassandra/DynamoDB mentions explain Datomic's alternative backing stores; they do not prove Atomic supports those backends, which belong to the storage/operations audit. Docs `:12`, `:18`, `:33`, `:45`, `:49`; [src/postgres.rs:3519](../src/postgres.rs:3519), `:3852`, `:4069`; [src/connection.rs:441](../src/connection.rs:441), `:467`; [tests/postgres_durability.rs:100](../tests/postgres_durability.rs:100), `:358`, `:563`. No fault/restart test was run. |
| [x] [04_transactions/06_client_synchronization.md](../datomic_pro_docs/04_transactions/06_client_synchronization.md) | **Present equivalent.** `Connection::sync_to(t, timeout)` waits for local basis without a separate transactor call; normal background observation advances it. `DatabaseValue::as_of` composes the upper bound, retaining immutable exact values. Docs `:22`, `:33`; [src/connection.rs:467](../src/connection.rs:467); [src/database_value.rs:1799](../src/database_value.rs:1799), `:1878`; [tests/native_time_points.rs:70](../tests/native_time_points.rs:70); [tests/index_pull.rs:341](../tests/index_pull.rs:341). |
| [x] [04_transactions/07_partitions.md](../datomic_pro_docs/04_transactions/07_partitions.md) | **Mostly present; ST-01 and ST-03.** All 524,288 implicit partitions, named installation, force/match, component affinity, partition extraction and `entid_at` exist. Default partition cannot be configured globally (ST-01). Automatic named-partition allocation is constrained by the shared frontier (ST-03). Application routing for partition sharding is intentionally an application technique; it is not database storage sharding. Docs `:22`, `:30`, `:44`, `:52`, `:63`, `:120`, `:126`, `:138`; [src/identity.rs:23](../src/identity.rs:23), `:43`; [src/partitions.rs:18](../src/partitions.rs:18); [src/database_value.rs:1821](../src/database_value.rs:1821); [docs/partitions.md:25](../docs/partitions.md:25); [tests/partitions.rs:288](../tests/partitions.rs:288), `:300`, `:419`; [tests/partition_authoring.rs:160](../tests/partition_authoring.rs:160); [tests/native_time_points.rs:122](../tests/native_time_points.rs:122). |
| [x] [04_transactions/08_transaction_hints.md](../datomic_pro_docs/04_transactions/08_transaction_hints.md) | **Present hints; tuning partial ST-04.** Speculation collects bounded advisory read prefixes, submissions accept them, and asynchronous prefetch does not determine transaction semantics or identity. Native stats expose work/overlap. Configurable intra-writer concurrent prefetch is missing. Docs `:11`, `:17`, `:23`, `:29`; [src/transaction_hints.rs:101](../src/transaction_hints.rs:101), `:165`, `:258`, `:429`, `:550`; [src/connection.rs:546](../src/connection.rs:546); [tests/transaction_hints.rs:49](../tests/transaction_hints.rs:49), `:90`, `:291`. |

### 05_query_and_pull

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [05_query_and_pull/00_query.md](../datomic_pro_docs/05_query_and_pull/00_query.md) | Present overview Datalog and hierarchical Pull exist: [src/query.rs:711](../src/query.rs:711), [src/pull.rs:884](../src/pull.rs:884). This navigation page adds no independent requirement. |
| [x] [05_query_and_pull/01_executing_queries.md](../datomic_pro_docs/05_query_and_pull/01_executing_queries.md) | Partial; Q03, Q06 Native queries, unification, timeout and cached preparation are present ([src/query.rs:676](../src/query.rs:676), [src/query.rs:1044](../src/query.rs:1044), [src/query_prepare.rs:82](../src/query_prepare.rs:82)). Lazy Pull projection is present ([src/query_sequence.rs:8](../src/query_sequence.rs:8), `:40`), but does not expose all eager source kinds. Author-controlled clause-order advice differs from Atomic's scheduler. Map/list/text syntax belongs to goal-1. |
| [x] [05_query_and_pull/02_query_reference.md](../datomic_pro_docs/05_query_and_pull/02_query_reference.md) | Partial; Q01, Q02, Q04; related Q03/Q06 Find shapes, bindings, all listed built-in aggregates, `:with`, joins, rules/required bindings, negation/disjunction, nested query, fulltext, tuple/get-else/get-some/missing and log functions exist ([src/query.rs:83](../src/query.rs:83), `:178`, `:202`, `:236`, `:1763`, `:2984`; [src/query_nested.rs](../src/query_nested.rs); [src/query_sources.rs:74](../src/query_sources.rs:74)). Raw-source arity and general query values are restricted; custom aggregate extension is absent. Portable custom function behavior exists through Rust callbacks and native programs ([src/query.rs:755](../src/query.rs:755), `:773`); loading arbitrary Java/Clojure code is a platform difference, not a blanket missing-function feature. Return maps exist ([src/query_return_maps.rs:32](../src/query_return_maps.rs:32), `:160`). |
| [x] [05_query_and_pull/03_pull.md](../datomic_pro_docs/05_query_and_pull/03_pull.md) | Native equivalent; goal-1 syntax ownership Forward/reverse navigation; wildcard/component defaults; arbitrary nesting/recursion and cycle handling; aliases/defaults/limits; five built-in native transforms and Rust callbacks all exist ([src/pull.rs:18](../src/pull.rs:18), `:61`, `:69`, `:82`, `:194`, `:228`, `:884`). [tests/pull_transforms.rs:94](../tests/pull_transforms.rs:94), `:157`, `:257` inspect transform/default/error behavior; [tests/pull_unbounded.rs](../tests/pull_unbounded.rs) and [tests/pull_bounded.rs](../tests/pull_bounded.rs) cover navigation limits. EDN `read-string` transform and serialized/legacy pattern syntax belong to goal-1. No additional core Pull gap was established by this pass. |

### 06_indexes

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [06_indexes/00_indexes.md](../datomic_pro_docs/06_indexes/00_indexes.md) | **Present equivalent.** All four covering index orders, descending transaction order, selective Pro AVET and ref-only VAET exist. Docs `:12`; [src/datom.rs:14](../src/datom.rs:14), `:30`; [src/index.rs:72](../src/index.rs:72); [tests/avet_schema_semantics.rs:106](../tests/avet_schema_semantics.rs:106); [src/datom.rs:70](../src/datom.rs:70). |
| [x] [06_indexes/01_index_model.md](../datomic_pro_docs/06_indexes/01_index_model.md) | **Present architecture; performance claims unverified.** Immutable segmented persistent trees, recent in-memory tiers, durable log, selective AVET, reverse-reference access and generic caches exist. No evidence from this read-only review establishes universal 1–2 storage reads, sublinear complexity for every workload, or comparative memory-speed performance. Docs `:21`, `:49`, `:66`, `:79`, `:87`, `:99`; [src/datom.rs:30](../src/datom.rs:30); [src/peer.rs:8004](../src/peer.rs:8004), `:8182`; [src/persistent_tree.rs:1805](../src/persistent_tree.rs:1805); [src/database_value.rs:692](../src/database_value.rs:692); [tests/incremental_tree_consolidation.rs:178](../tests/incremental_tree_consolidation.rs:178); [tests/postgres_peer.rs:370](../tests/postgres_peer.rs:370). |
| [x] [06_indexes/02_background_indexing.md](../datomic_pro_docs/06_indexes/02_background_indexing.md) | **Present equivalent; workload capacity remains empirical.** Threshold-triggered background indexing and hard memory-index backpressure exist, as do immutable publication/recovery paths. Native implementation need not use Lucene to implement eventual fulltext maintenance. Docs `:13`, `:17`, `:30`; [src/service.rs:43](../src/service.rs:43), `:49`, `:164`; [src/peer.rs:2500](../src/peer.rs:2500); [tests/background_indexing.rs:345](../tests/background_indexing.rs:345), `:415`, `:867`, `:965`; [tests/fulltext_incremental.rs:343](../tests/fulltext_incremental.rs:343). |
| [x] [06_indexes/03_index_pull.md](../datomic_pro_docs/06_indexes/03_index_pull.md) | **Present equivalent.** Lazy AVET/AEVT projection from the third component, attribute-range termination, AEVT ref-many checks, AVET-many start-value requirement, duplicates, direction-sensitive offset, limit, cancellation and retained temporal views have explicit implementations. Peer default unlimited versus Client default 1000 is documented in the native API and is not a missing feature. Boundary-authoring ST-05 may affect shortened tuple starts. Docs `:10`, `:15`, `:29`, `:83`, `:154`, `:164`; [src/index_pull.rs:23](../src/index_pull.rs:23), `:77`, `:88`, `:174`; [tests/index_pull.rs:127](../tests/index_pull.rs:127), `:185`, `:216`, `:289`, `:341`, `:392`. |
| [x] [06_indexes/04_index_apis.md](../datomic_pro_docs/06_indexes/04_index_apis.md) | **Present capability, different native shape.** Typed prefix/seek iterators expose datoms. Eager `Database::avet_range` supplies half-open attribute ranges; native snapshot range cursors and exact-value AVET seek plus Rust stopping predicates provide the same retrieval capability. There is no identically named `DatabaseValue::index_range` convenience; this alone is not recorded as a missing database capability. Docs `:15`, `:39`; [src/database.rs:977](../src/database.rs:977); [src/database_value.rs:2133](../src/database_value.rs:2133), `:2313`; [src/peer.rs:8004](../src/peer.rs:8004), `:8159`, `:8610`; [src/index.rs:72](../src/index.rs:72). |
| [x] [06_indexes/05_rseek_datoms.md](../datomic_pro_docs/06_indexes/05_rseek_datoms.md) | **Present traversal; partial boundary normalization ST-05.** Forward/reverse lazy seeks support absent suffixes, nonexisting positions, full transaction components and temporal views. Schema-aware convenience builders wrongly require full stored tuple shape, although lower-level numeric boundaries can represent shorter virtual tuples. Docs `:10`, `:12`, `:28`, `:41`, plus `_images/05_rseek_datoms/rseek-datoms5-partial-tuple.png` inspected; [src/database_value.rs:2127](../src/database_value.rs:2127), `:2196`, `:2677`, `:2712`, `:2842`; [src/index.rs:263](../src/index.rs:263); [tests/postgres_peer.rs:370](../tests/postgres_peer.rs:370), `:403`; [tests/native_speculation.rs:155](../tests/native_speculation.rs:155). |

### 07_peer_api

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [07_peer_api/00_clojure/00_datomic_api.md](../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md) | Partial; Q03/Q05/Q06; P01–P06, D01–D03 Native database/transaction/query/Pull/index/log/time/partition/UUID operations exist, as do schema upgrade, GC, report queues and readiness waits. Missing/partial public surfaces are indexed below. Clojure collection interfaces, JVM exception wrappers and JVM shutdown are language/platform differences. `next-t` is computable as `basis_t + 1`; lack of its public spelling is not treated as a core capability gap. |
| [x] [07_peer_api/01_java/00_package_datomic.md](../datomic_pro_docs/07_peer_api/01_java/00_package_datomic.md) | Native architecture equivalent; platform choice Embedded peer-local queries, caching, transaction submission and live observation exist ([src/connection.rs:80](../src/connection.rs:80), `:443`, `:526`; [src/peer.rs:151](../src/peer.rs:151)). Eager in-process `Database` exists as an oracle ([src/lib.rs:1](../src/lib.rs:1), [src/database.rs](../src/database.rs)); a named `datomic:mem://` service is not automatically required. PostgreSQL is Atomic's chosen durable backend. |
| [x] [07_peer_api/01_java/01_attribute.md](../datomic_pro_docs/07_peer_api/01_java/01_attribute.md) | Partial; P01 Resident schema attributes expose type, cardinality, uniqueness, index/fulltext/component/no-history flags ([src/schema.rs:53](../src/schema.rs:53), `:419`; [src/database_value.rs:1723](../src/database_value.rs:1723)). Physical AVET readiness is correctly distinguished internally but is not public ([src/database_value.rs:2400](../src/database_value.rs:2400), [src/peer.rs:7765](../src/peer.rs:7765)). |
| [x] [07_peer_api/01_java/02_connection.md](../datomic_pro_docs/07_peer_api/01_java/02_connection.md) | Mostly equivalent; P04/P05 and D01/D03 Local nonblocking `db`/`log`, submit/wait, background observation, sync/index/schema/excision coordination, request-index and unbounded report queues exist ([src/connection.rs:443](../src/connection.rs:443), `:456`, `:463`, `:470`, `:479`, `:495`, `:507`, `:515`, `:526`, `:570`, `:581`). GC is on `PostgresOperator` ([src/operations.rs:847](../src/operations.rs:847)). Futures/listeners and explicit fixed read-only connection contract differ. Request-index transport ownership warrants a separate integration check. |
| [x] [07_peer_api/01_java/03_database.md](../datomic_pro_docs/07_peer_api/01_java/03_database.md) | Mostly equivalent; P01/P03/P06; API convenience candidates Immutable `DatabaseValue` supports time/history/filter/ident/entity/Pull/raw-index/with/stats ([src/database_value.rs:1689](../src/database_value.rs:1689), `:1799`, `:1808`, `:2053`, `:2061`, `:2090`, `:2133`, `:2203`; [src/pull.rs:874](../src/pull.rs:874); [src/transaction.rs:912](../src/transaction.rs:912); [src/database_stats.rs:30](../src/database_stats.rs:30); [src/index_pull.rs:79](../src/index_pull.rs:79)). Bound function invocation, entity equality, and attribute readiness have remaining public-interface differences. AVET ranges are expressible using cursor boundaries; eager `Database::avet_range` exists ([src/database.rs:978](../src/database.rs:978)); absence of a `DatabaseValue::index_range` spelling is a convenience candidate only. |
| [x] [07_peer_api/01_java/04_database_predicate.md](../datomic_pro_docs/07_peer_api/01_java/04_database_predicate.md) | Present equivalent Native `DatabaseValue::filter` passes unfiltered database plus datom to a Rust closure and composes filters with AND ([src/database_value.rs:2058](../src/database_value.rs:2058)); this matches the portable Java Predicate contract. |
| [x] [07_peer_api/01_java/05_datom.md](../datomic_pro_docs/07_peer_api/01_java/05_datom.md) | Present equivalent Immutable fact fields and assert/retract distinction are represented by `Datom` ([src/datom.rs](../src/datom.rs)). Named Rust fields replace Java getters and positional `get`; this is API spelling, not missing database information. |
| [x] [07_peer_api/01_java/06_entity.md](../datomic_pro_docs/07_peer_api/01_java/06_entity.md) | Partial; P03 Lazy cached getters, cardinality-many collection shape, reverse references, key enumeration, pinned basis and recursive component touch exist ([src/pull.rs:529](../src/pull.rs:529), `:602`, `:616`, `:726`, `:747`). Documented identity-based equality has no public Entity equivalence contract. |
| [x] [07_peer_api/01_java/07_listenable_future.md](../datomic_pro_docs/07_peer_api/01_java/07_listenable_future.md) | Partial portable ergonomics; P04 Transaction admission is asynchronous, and tickets support waiting ([src/connection.rs:526](../src/connection.rs:526), `:679`; [src/service.rs:1325](../src/service.rs:1325)), but tickets do not expose callback registration or implement a Rust Future. Do not describe this as absent async submission. |
| [x] [07_peer_api/01_java/08_log.md](../datomic_pro_docs/07_peer_api/01_java/08_log.md) | Present equivalent Immutable log capture and inclusive-start/exclusive-end T/Tx/instant/unbounded iteration exist ([src/connection.rs:456](../src/connection.rs:456); [src/native_log.rs:28](../src/native_log.rs:28), `:104`; [src/time_point.rs](../src/time_point.rs)). [tests/native_log.rs](../tests/native_log.rs) and [tests/query_runtime_sources.rs:732](../tests/query_runtime_sources.rs:732) are relevant existing fixtures, not new passing evidence. |
| [x] [07_peer_api/01_java/09_peer.md](../datomic_pro_docs/07_peer_api/01_java/09_peer.md) | Partial; Q01–Q04, P02/P04/P05/P06 Native APIs cover much of Peer through separate modules. Missing logical database list/rename/delete and idempotent-create behavior are portable. Direct lazy backup connection is partial, because offline deep verification already produces a queryable eager `Database` ([src/backup.rs:86](../src/backup.rs:86), `:782`). Datomic protocol URLs, JVM `Fn`, Java methods and classpath configuration are platform choices. `squuid`, extraction and T/Tx helpers exist ([src/uuid.rs:44](../src/uuid.rs:44), `:70`; [src/identity.rs:91](../src/identity.rs:91)). |
| [x] [07_peer_api/01_java/10_query_request.md](../datomic_pro_docs/07_peer_api/01_java/10_query_request.md) | Native equivalent; goal-1 data syntax `Query`, inputs and `QueryControl.timeout` provide request and approximate timeout semantics ([src/query.rs:244](../src/query.rs:244), `:676`). A Java builder or Java `Map` conversion is not required in Rust. |
| [x] [07_peer_api/01_java/11_util.md](../datomic_pro_docs/07_peer_api/01_java/11_util.md) | Language equivalent + externally owned Rust collections/iterators and `Keyword`/`Symbol` fields cover collection construction, streaming, names/namespaces ([src/value.rs:11](../src/value.rs:11), `:40`). EDN read/readAll belongs to goal-1. No separate utility-library implementation is needed. |
| [x] [07_peer_api/02_shared_reference/00_log_api.md](../datomic_pro_docs/07_peer_api/02_shared_reference/00_log_api.md) | Present equivalent; Q03 source composition caveat Immutable log, T/Tx/instant range bounds, `tx-ids` and `tx-data` query functions exist ([src/native_log.rs:104](../src/native_log.rs:104), `:138`, `:150`; [src/query_sources.rs:74](../src/query_sources.rs:74)). Native log source is supplied by an explicit named source instead of a boxed Clojure argument. Logs are not replaced with database history. |
| [x] [07_peer_api/02_shared_reference/01_error_handling.md](../datomic_pro_docs/07_peer_api/02_shared_reference/01_error_handling.md) | Native equivalent; platform difference Structured error categories/code/message/details and original structured program cancellation anomalies exist ([src/error.rs:11](../src/error.rs:11), `:25`). Rust `Result`/Error replaces exception/channel mechanics. JVM third-party exception nesting is not an independent Rust feature requirement. |
| [x] [07_peer_api/02_shared_reference/02_io_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/02_io_stats.md) | Partial; D01 SQL operation contexts, nested inclusive driver-call attribution, callbacks, phase times and cache metrics exist ([src/sql_io.rs:73](../src/sql_io.rs:73), `:104`, `:193`, `:216`; [src/connection.rs:366](../src/connection.rs:366)). Missing documented unified application-context/cache-tier/index-sort per-call I/O report/logging contract. |
| [x] [07_peer_api/02_shared_reference/03_query_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md) | Partial; D02; related Q06 `QueryStats` and executed `PlanStep` row counts exist ([src/query.rs:635](../src/query.rs:635), `:661`, `:1793`). Full clause identity, binding sets, query phase hierarchy and warnings are absent; actual scheduler also differs. |
| [x] [07_peer_api/02_shared_reference/04_transaction_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/04_transaction_stats.md) | Partial; D03 Existing SQL phase times, queue statistics, hint stats and internal assessment work are useful ([src/sql_io.rs:18](../src/sql_io.rs:18), `:65`; [src/service.rs:1469](../src/service.rs:1469); [src/transaction_hints.rs:155](../src/transaction_hints.rs:155); [src/tiered_assessor.rs:26](../src/tiered_assessor.rs:26)). They do not constitute the documented per-transaction semantic counts and timings automatically logged with transaction T. |

### 08_operations

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [08_operations/00_architecture_and_storage/00_storage_services.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.md) | Native PostgreSQL provisioning, distinct roles, TLS, and in-memory kernel exist ([src/postgres.rs:436](../src/postgres.rs:436), `:449`, `:2862`; [src/postgres_connection.rs:156](../src/postgres_connection.rs:156); [src/database.rs:378](../src/database.rs:378)). Other durable backends/embedded dev-server packaging are deliberate differences ENV01; optional shared caching and AWS services are AO08/ENV02. |
| [x] [08_operations/00_architecture_and_storage/01_transactor_reference.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/01_transactor_reference.md) | Transaction service, executable, discovery, and verified TLS exist ([src/bin/atomic.rs:275](../src/bin/atomic.rs:275); [src/remote_transport.rs:377](../src/remote_transport.rs:377)). HTTP health endpoint AO07 is absent; shared Memcached AO08 absent; packaged standby AO05 partial. PID-file convenience is AO-C03. JVM flags/keystores are host-specific ENV03. |
| [x] [08_operations/00_architecture_and_storage/02_datomic_deployment.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/02_datomic_deployment.md) | Lease fencing, independent reads, retries, migrations, and operational docs exist ([src/service.rs:1502](../src/service.rs:1502), [src/connection.rs:443](../src/connection.rs:443), [docs/operations.md:51](../docs/operations.md:51)). Stock standby and diagnostics partial AO05/AO06. Remote route rediscovery AO-C01 and cross-version live upgrades AO-C02 need narrower follow-up; source does not establish broad rolling-upgrade compatibility. |
| [x] [08_operations/01_capacity_and_reliability/00_capacity_planning.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md) | Recent-memory thresholds/backpressure, cache budgets, SQUUIDs, async submission tickets, and separate GC command exist ([src/service.rs:49](../src/service.rs:49), `:164`, `:1112`; [src/uuid.rs](../src/uuid.rs); [src/bin/atomic/admin.rs:61](../src/bin/atomic/admin.rs:61)). Configurable parallel indexing AO11, shared caching AO08, maintenance pacing AO10, and database deletion/reclamation P02 remain gaps. DDB sizing is ENV01. |
| [x] [08_operations/01_capacity_and_reliability/01_high_availability.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/01_high_availability.md) | Library standby and fencing exist; test source [tests/service_failover.rs:44](../tests/service_failover.rs:44) exercises takeover/read availability. Stock transactor cannot simply be launched twice as active/standby (AO05). Storage replication remains PostgreSQL deployment responsibility ([docs/operations.md:141](../docs/operations.md:141)); not a missing database semantic. |
| [x] [08_operations/01_capacity_and_reliability/02_backup_and_restore.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md) | Live differential backup, listing, explicit exact-point restore, verification and lineage guards exist ([src/backup.rs:297](../src/backup.rs:297), `:689`, `:782`, `:1027`; [tests/backup_restore.rs:276](../tests/backup_restore.rs:276)). Offline eager querying is already possible via `BackupVerification.database` ([src/backup.rs:86](../src/backup.rs:86)); missing lazy backup connection/log surface is P05. S3/SSE integration ENV02; tuning controls AO10. |
| [x] [08_operations/02_observability_and_tuning/00_monitoring_and_performance.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/00_monitoring_and_performance.md) | SQL/phase callbacks and service/cache/indexing/inspection counters exist ([src/sql_io.rs:73](../src/sql_io.rs:73), `:112`, `:275`; [src/service.rs:164](../src/service.rs:164), `:1469`; [src/operations.rs:91](../src/operations.rs:91)). Automatic metric delivery, operational event logging, heartbeat distributions and alarms are partial AO06; CloudWatch ENV02; Memcached AO08. |
| [x] [08_operations/02_observability_and_tuning/01_memory_and_caching.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/01_memory_and_caching.md) | RAM/recent/entity/SSD cache foundations exist ([src/peer.rs:5224](../src/peer.rs:5224), `:8438`; [src/pull.rs](../src/pull.rs); [src/service.rs:49](../src/service.rs:49)). Shared network Memcached missing AO08. SSD log-segment coverage partial AO09. No requirement for Java LRU object representation itself. |
| [x] [08_operations/02_observability_and_tuning/02_configuring_logging.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/02_configuring_logging.md) | CLI startup/results/errors exist, but no configurable operational logging facade/appender pipeline found (AO06; [src/bin/atomic.rs:375](../src/bin/atomic.rs:375), `:448`). S3 log rotation ENV02; SLF4J/logback/log4j APIs are ENV03. |
| [x] [08_operations/02_observability_and_tuning/03_system_properties.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/03_system_properties.md) | Native config APIs/env/flags provide transport deadlines, lease durations, memory budgets and hints ([src/postgres_connection.rs](../src/postgres_connection.rs), [src/runtime_config.rs](../src/runtime_config.rs), [src/bin/atomic.rs:35](../src/bin/atomic.rs:35), [src/transaction_hints.rs](../src/transaction_hints.rs)). Missing capability controls: AO06/AO08/AO10/AO11; PID file AO-C03. Java property names and obsolete licenses not Rust requirements. |

### 09_optional

| Reviewed document | Disposition and evidence |
| --- | --- |
| [x] [09_optional/00_pro_client/00_accessing_the_client_library.md](../datomic_pro_docs/09_optional/00_pro_client/00_accessing_the_client_library.md) | Maven installation is ENV03. Native library exists; thin client and fully nonblocking client facade absent AO02/P04. No duplicate EDN work. |
| [x] [09_optional/00_pro_client/01_peer_server.md](../datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md) | Lightweight server-side reads/query/cache gateway absent AO02. Current TLS endpoint serves transaction submission to an already PostgreSQL-connected native peer ([src/remote_transport.rs:377](../src/remote_transport.rs:377), `:412`). Gateway-specific chunking/multiple databases/credential sets belong to AO02; HTTP health AO07. |
| [x] [09_optional/00_pro_client/02_client_getting_started.md](../datomic_pro_docs/09_optional/00_pro_client/02_client_getting_started.md) | Native application workflow/schema/maps/report data exist ([docs/application.md](../docs/application.md), [src/service.rs:232](../src/service.rs:232), `:269`). Tutorial cannot be followed with a thin network-only Atomic client (AO02/P04). Named memory peer-server convenience is a packaging difference under ENV01. |
| [x] [09_optional/00_pro_client/03_client_library_reference.md](../datomic_pro_docs/09_optional/00_pro_client/03_client_library_reference.md) | Immutable thread-safe native connections, typed errors, timeouts and local cursors exist ([src/connection.rs:89](../src/connection.rs:89), [src/error.rs](../src/error.rs), [src/database_value.rs](../src/database_value.rs)). Thin network chunking and async client facade absent AO02/P04; catalog list/delete absent P02. Automatic remote-route reuse AO-C01. |
| [x] [09_optional/00_pro_client/04_client_api_sync.md](../datomic_pro_docs/09_optional/00_pro_client/04_client_api_sync.md) | Most underlying semantics have native query/Pull/index/log/temporal/speculative APIs ([src/lib.rs:88](../src/lib.rs:88), `:102`, `:165` and corresponding modules). Missing thin facade AO02, list/delete P02; native migration workflow substitutes for JVM administer-system. EDN query/map text handled by goal-1. |
| [x] [09_optional/00_pro_client/05_client_api_async.md](../datomic_pro_docs/09_optional/00_pro_client/05_client_api_async.md) | Typed transaction admission returns a ticket, but result consumption blocks; queries/Pull/log and network submission remain synchronous (P04). No thin server/chunk stream facade AO02. Underlying query/index/log semantics should be deduplicated with main query/peer audit. |
| [x] [09_optional/01_aws/00_running_on_aws.md](../datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md) | AWS provisioning/AMI/CloudFormation/DDB-specific tooling not supplied ENV01/ENV02. The document itself marks legacy CloudFormation tools deprecated at line 9; do not promote these to required roadmap work. Optional S3 logs/CloudWatch could still help PostgreSQL deployments ENV02/AO06. |
| [x] [09_optional/01_aws/01_aws_access_control.md](../datomic_pro_docs/09_optional/01_aws/01_aws_access_control.md) | No direct AWS SDK/IAM role-provider integration found ENV02; PostgreSQL roles and TLS exist ([src/postgres.rs:449](../src/postgres.rs:449), [src/postgres_connection.rs](../src/postgres_connection.rs)). Different security ecosystem, not evidence that Atomic lacks authentication. |
| [x] [09_optional/02_specialized_operations/00_read_only_connections.md](../datomic_pro_docs/09_optional/02_specialized_operations/00_read_only_connections.md) | Transactor-independent reads exist using `Peer`, captured `DatabaseValue`/`LogValue`, and exact snapshot references ([src/peer.rs:4818](../src/peer.rs:4818), `:5167`, `:5173`; [src/snapshot_reference.rs](../src/snapshot_reference.rs)). Eager offline backup reads also exist; missing lazy backup connection/cache/log P05. `Connection` auto-advances, whereas a captured value or ordinary unsynced `Peer` supplies fixed-basis behavior. |
| [x] [09_optional/02_specialized_operations/01_valcache.md](../datomic_pro_docs/09_optional/02_specialized_operations/01_valcache.md) | Native SSD cache survives reopen, authenticates entries, bounds space, and reports stats ([src/ssd_cache.rs:105](../src/ssd_cache.rs:105); [src/peer.rs:8438](../src/peer.rs:8438); [tests/native_ssd_cache.rs:13](../tests/native_ssd_cache.rs:13)). Log segment caching is missing AO09. Memcached alternative AO08. No reason to require Datomic's exact filesystem-atime implementation. |
| [x] [09_optional/02_specialized_operations/02_excision.md](../datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md) | Entity/attribute/selected-attribute/cutoff/component/inbound-reference predicates, protected requests, resumable generation rewrite and synchronization exist ([src/excision.rs:192](../src/excision.rs:192), `:279`; [src/operations.rs:1143](../src/operations.rs:1143), `:1166`; [tests/operations_excision.rs:79](../tests/operations_excision.rs:79)). Automatic first-index-job execution missing AO04. Fulltext support goes beyond the document's stated limitation; do not label fulltext excision missing. |
| [x] [09_optional/03_technical_notes/00_comparison_with_updating_transactions.md](../datomic_pro_docs/09_optional/03_technical_notes/00_comparison_with_updating_transactions.md) | Set-based declarative transaction expansion/assessment exists ([src/service.rs:214](../src/service.rs:214); [src/transaction.rs:1112](../src/transaction.rs:1112), `:1176`; [src/database.rs:1706](../src/database.rs:1706), `:3178`). Functions see db-before, final predicates see db-after. No separate gap established from this conceptual note. |
| [x] [09_optional/03_technical_notes/01_composing_transactions_by_example.md](../datomic_pro_docs/09_optional/03_technical_notes/01_composing_transactions_by_example.md) | Transaction functions plus attribute/entity predicates exist ([src/transaction.rs:828](../src/transaction.rs:828), `:792`, `:806`; [tests/program_transactions.rs:152](../tests/program_transactions.rs:152) verifies a complete proposed db-after entity-spec workflow). Arbitrary JVM code is replaced by versioned native programs; no separate missing composition requirement established. |
| [x] [09_optional/03_technical_notes/02_hosting_a_private_maven_repository.md](../datomic_pro_docs/09_optional/03_technical_notes/02_hosting_a_private_maven_repository.md) | Dependency-hosting guide is JVM distribution ENV03. Cargo package exists with publish=false ([Cargo.toml:1](../Cargo.toml:1)); private artifact publishing strategy is not a transaction/query feature defect. |
| [x] [09_optional/03_technical_notes/03_querying_byte_array_attributes.md](../datomic_pro_docs/09_optional/03_technical_notes/03_querying_byte_array_attributes.md) | Deliberate host-language difference: Rust bytes compare by content ([src/value.rs:198](../src/value.rs:198)), so Java identity-equality workaround is unnecessary. Binary values exist ([src/value.rs:80](../src/value.rs:80)). |
| [x] [09_optional/03_technical_notes/04_outer_joins.md](../datomic_pro_docs/09_optional/03_technical_notes/04_outer_joins.md) | Pull and GetElse implement core outer-join use case ([src/query.rs:125](../src/query.rs:125), `:222`, `:2547`; [tests/query_exact_sources.rs:135](../tests/query_exact_sources.rs:135)). Runtime input-bound Pull pattern native AST limitation is AO-an EDN-owned normalization concern, not an established gap. |
| [x] [09_optional/04_tools_and_support/00_datomic_pro_console.md](../datomic_pro_docs/09_optional/04_tools_and_support/00_datomic_pro_console.md) | Graphical database/schema/entity/history/index/query browser absent AO13. Inspected `console-window.png`: confirms integrated schema tree, temporal selectors, query builder, dataset and data-source panes; other screenshot filenames/links inventoried with the document. CLI data commands now exist from goal-1 and do not provide graphical console. |
| [x] [09_optional/04_tools_and_support/01_writing_a_problem_report.md](../datomic_pro_docs/09_optional/04_tools_and_support/01_writing_a_problem_report.md) | Process guidance, not a database capability contract. Immutable values/pure in-memory construction and Rust examples make small repros possible ([src/database.rs:378](../src/database.rs:378), `:1425`; [docs/application.md:149](../docs/application.md:149)). EDN/REPL ergonomics overlap goal-1; no invented requirement for an automated support bundle. |

## Detailed gaps and partial capabilities

### Q02

**Relation data patterns require 3–5 columns**

- **Status:** Missing generality; high confidence; independent of EDN parsing.
- **Requirement:** [datomic_pro_docs/05_query_and_pull/02_query_reference.md:680](../datomic_pro_docs/05_query_and_pull/02_query_reference.md:680) defines a pattern over a relation with one or more fields; `:1461`–`:1492` explicitly uses `$artists` containing `[["The Beatles"]]` and the one-column pattern `[$artists ?aname]`.
- **Actual:** [src/query.rs:83](../src/query.rs:83) hard-codes entity/attribute/value plus optional transaction/added fields. [src/query_join.rs:152](../src/query_join.rs:152) always creates positions 0,1,2; `:163` gives a minimum width of three; `:190` and `:229` skip rows shorter than that width. There is no sixth pattern slot.
- **Impact:** The documented one-column named relation source cannot match; general two-column and six-plus-column source relations also cannot be represented correctly. Relation input bindings do exist and can be a workaround, but they do not provide equivalent independently named relation-source/rule scoping.
- **Next validation:** Translate the `$artists` example directly to typed AST with trailing blanks, and compare with a one-column relation input. Add a six-column source example. Coordinate any AST change with goal-1, which must report rather than disguise this limitation.

### Q01

**Custom aggregates have no native extension point**

- **Status:** Missing; high confidence; portable capability.
- **Requirement:** [datomic_pro_docs/05_query_and_pull/02_query_reference.md:260](../datomic_pro_docs/05_query_and_pull/02_query_reference.md:260) allows custom aggregate/predicate/function clauses; `:313` allows aggregate arguments to include variables, constants and source variables.
- **Actual:** [src/query.rs:202](../src/query.rs:202) has a closed built-in `Aggregate` enum. `FindElement::Aggregate` at `:222` accepts one variable plus that enum. `aggregate_rows` at `:2958` dispatches only those cases. `QueryExtensions` at `:730` extends ordinary functions, not aggregation.
- **Impact:** An application cannot define a weighted/custom reduction over each query group within `:find`; it must fetch/group/reduce outside the query or redesign the query. All documented built-in aggregates are already represented, including median/variance/stddev/random/sample/min-N/max-N.
- **Next validation:** Design one grouped weighted aggregate using two variables and one constant; prove whether the intended native callback/bytecode ABI can express it without executing Clojure. A future implementation should distinguish aggregate registration from ordinary row functions.

### Q03

**Lazy query sequences accept database sources only**

- **Status:** Partial; high confidence; portable composition gap.
- **Requirement:** [datomic_pro_docs/05_query_and_pull/01_executing_queries.md:58](../datomic_pro_docs/05_query_and_pull/01_executing_queries.md:58) says qseq uses the same arguments and grammar as q; [datomic_pro_docs/07_peer_api/01_java/09_peer.md:321](../datomic_pro_docs/07_peer_api/01_java/09_peer.md:321) repeats qseq's relation to ordinary query. Ordinary query can mix databases and plain data/log functions.
- **Actual:** [src/query_sequence.rs:94](../src/query_sequence.rs:94)/`:103` take `&[QuerySource]`; each `QuerySource` contains only a `DatabaseValue` ([src/query.rs:265](../src/query.rs:265)). Eager execution accepts `QueryDataSource::{Database,Tuples,Log}` ([src/query.rs:1044](../src/query.rs:1044), [src/query_sources.rs:6](../src/query_sources.rs:6)). The sequence reconstructs database-only source references ([src/query_sequence.rs:114](../src/query_sequence.rs:114)).
- **Impact:** A query joining a database to a raw relation or immutable log cannot use the public lazy sequence path with the same inputs; caller must eagerly project Pull or restructure the query.
- **Next validation:** Reuse the native log/raw-source fixture with a Pull projection, and verify that no public sequence entry point accepts its sources. Preserve lazy transform execution and efficient `remaining_rows()` when extending it.

### Q04

**Native query values and custom callbacks have a narrower domain than general data queries**

- **Status:** Partial; high confidence on API limitation; exact desired domain requires a compatibility decision. Coordinate with goal-1.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/09_peer.md:299](../datomic_pro_docs/07_peer_api/01_java/09_peer.md:299)–`:305` allows arbitrary values among inputs. [datomic_pro_docs/05_query_and_pull/02_query_reference.md:38](../datomic_pro_docs/05_query_and_pull/02_query_reference.md:38) defines constants as any non-variable data literal, and `:945` demonstrates a collection literal in ground. General predicate/function contracts appear at `:742` and `:791`.
- **Actual:** `QueryInput` and raw source cells use stored `Value` ([src/query.rs:194](../src/query.rs:194), [src/query_sources.rs:10](../src/query_sources.rs:10)). `Value` has no standalone nil, arbitrary map, or set ([src/value.rs:75](../src/value.rs:75)), although internal/result `QueryValue` can hold nil/maps/collections ([src/query.rs:270](../src/query.rs:270)) and `Term::Nil` exists. `Term::Constant` also takes `Value` ([src/query.rs:67](../src/query.rs:67)). Custom callback ABI takes and returns stored values ([src/query.rs:713](../src/query.rs:713), `:755`); extension dispatch explicitly requires stored args (`:2621`) and a database source (`:2613`).
- **Impact:** Standalone nil/maps/general collections cannot pass through ordinary typed query input/constants or extension arguments/results with their original shape. A database-free custom function query also needs an artificial database source. This is a semantic boundary below the EDN reader; successful EDN parsing alone cannot fix it.
- **Existing equivalents:** RuntimeValue maps/vectors exist for native programs; QueryValue maps/collections/nil exist for results; built-in tuple/nil behavior has fixtures ([tests/query_primitives.rs:187](../tests/query_primitives.rs:187), `:394`). Do not say Atomic cannot represent any map/nil anywhere.
- **Next validation:** Inventory desired typed query domain using nil, an arbitrary-key map, a nested vector/set and a pure custom string function with no database. Record accepted limitations separately from parser support; avoid requiring arbitrary JVM objects.

### P02

**Logical database lifecycle API is incomplete**

- **Status:** Missing list/rename/delete; create idempotency differs; high confidence from public API/CLI inventory.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/09_peer.md:176](../datomic_pro_docs/07_peer_api/01_java/09_peer.md:176)–`:220` and Clojure sections `create-database`, `delete-database`, `get-database-names`, `rename-database` require catalog lifecycle operations; create returns false if already present.
- **Actual:** [src/postgres.rs:2862](../src/postgres.rs:2862) creates a database and `:2915` unconditionally INSERTs its catalog row; duplicate errors propagate as `postgres/create-catalog`, rather than a created/existed result. [src/bin/atomic/admin.rs:39](../src/bin/atomic/admin.rs:39) dispatches create/status/consolidate/backup/restore/inspect/GC/fulltext commands, with no list/rename/delete. No corresponding public native functions were found after reading the operation/store surfaces. Backup restore into another target is available, but is not an identity-preserving rename.
- **Impact:** Users must write catalog SQL or application wrappers for logical-database enumeration/lifecycle and idempotent provisioning. Rename/delete need explicit native lineage, leases, snapshots, retention and reference semantics, not a direct SQL one-liner.
- **Next validation:** Confirm intended product support and define lifecycle behavior under active readers/writer/backups, then add bounded isolated catalog fixtures. Coordinate with operations audit to deduplicate.

Include the documented deleted-database lifecycle in the same item: [datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:290](../datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:290) separates making a database unavailable from later reclaiming its storage. Current global GC and abandoned-restore cleanup do not expose that lifecycle for live logical databases. Do not count listing/deletion/reclamation again as a separate operations gap.

### AO04

**Automatically execute committed excision requests during indexing (partial)**

- Requirement: [datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md:42](../datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md:42) says effects occur during the first indexing job after the excision transaction; `:44` exposes sync-excise for completion coordination.
- Present: transaction predicates and a resumable operator implementation ([src/excision.rs:192](../src/excision.rs:192); [src/operations.rs:1143](../src/operations.rs:1143)), sync ([src/operations.rs:1166](../src/operations.rs:1166), [src/connection.rs:515](../src/connection.rs:515)). [tests/operations_excision.rs](../tests/operations_excision.rs) exercises operator fault/retry paths, not automatic scheduling.
- Limitation: [docs/operations.md:395](../docs/operations.md:395) instructs callers to run `process_excision_requests` separately. The background worker invokes `consolidate_background_once` ([src/service.rs:2180](../src/service.rs:2180)), and repository-wide call searches find no production scheduler calling `process_excision_requests`. CLI admin dispatch lacks an excision-worker command ([src/bin/atomic/admin.rs:41](../src/bin/atomic/admin.rs:41)).
- Impact: merely committing a request and waiting for normal indexing does not cause the documented erasure workflow to progress; operators need custom Rust invocation.
- Confidence: high from source/call graph. Next validation: commit one excision request, force/wait for canonical indexing, inspect pending status without explicit operator invocation, then invoke worker to prove the difference. No such runtime reproduction was run in this audit.

### AO05

**Stock transactor active/standby launch workflow (partial tooling)**

- Requirement: [datomic_pro_docs/08_operations/01_capacity_and_reliability/01_high_availability.md:13](../datomic_pro_docs/08_operations/01_capacity_and_reliability/01_high_availability.md:13) says launching two configured transactors results in active and standby; deployment doc `:266` uses this for takeover/upgrades.
- Present: `TransactionStandby::start_configured` polls for authority ([src/service.rs:1517](../src/service.rs:1517)); test source [tests/service_failover.rs:44](../tests/service_failover.rs:44) covers takeover and reads. HA is not missing as a library capability.
- Limitation: executable transactor path directly calls `TransactionService::start_configured_with_indexing` ([src/bin/atomic.rs:343](../src/bin/atomic.rs:343)) and propagates errors. Allowed flags (`:84`) have no standby/automatic-election mode; main returns failure. Starting a second stock executable while the first holds the lease exits instead of staying standby.
- Impact: deployment requires a custom harness or a supervisor repeatedly relaunching contenders, rather than the documented persistent standby process.
- Confidence: high. Next validation: two real stock transactor processes with separate listeners and one database, then terminate the active; compare with library standby behavior.

### D01

**Per-operation I/O report coverage is incomplete**

- **Status:** Partial; high confidence; portable observability gap.
- **Requirement:** [datomic_pro_docs/07_peer_api/02_shared_reference/02_io_stats.md:73](../datomic_pro_docs/07_peer_api/02_shared_reference/02_io_stats.md:73), `:85`, `:116`, `:133` and `:147`: caller-provided qualified io-context; returned transaction/query/Pull stats; context-grouped nested reports; cache-tier counts/latencies and index-sort/load counts; automatic transaction logging (`:32`).
- **Actual:** [src/sql_io.rs:73](../src/sql_io.rs:73) records driver-call counts/latencies/payloads/phases, with inclusive parent aggregation and callbacks (`:104`, `:193`, `:216`, `:273`). `OperationKind` is a fixed enum (`:18`). Cache/SSD/node/peer-load metrics are separate cumulative accessors ([src/connection.rs:366](../src/connection.rs:366)–`:384`). QueryOutcome carries query stats and plan ([src/query.rs:669](../src/query.rs:669)); Pull returns QueryValue ([src/pull.rs:884](../src/pull.rs:884)); no unified documented context/cache/index report is attached to those results.
- **Impact:** Users can observe SQL and cache activity, but cannot directly attribute all cache-tier and index-sort reads/latencies to one named business operation and its named nested calls through the documented API contract. Production concurrency makes subtracting global metrics unreliable.
- **Next validation:** Trace one cached/uncached query, a nested query and a transaction program through existing OperationContext; identify the minimal missing per-call cache/index counters and business-context labeling. Reuse existing SQL attribution rather than invent a second tracing stack.

### D02

**Query stats cannot identify full clauses or show bindings/phases/warnings**

- **Status:** Partial; high confidence.
- **Requirement:** [datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md:20](../datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md:20) and `:38`–`:67` require original query, phases/schedule, full clause, rows in/out, bound variables in/out, used predicates and optional expansion/unbound warnings.
- **Actual:** [src/query.rs:661](../src/query.rs:661) PlanStep includes only string clause/access and row counts. At `:1793` it records `clause_name`; `:3548` maps every data pattern to the same string `"pattern"`. QueryStats is aggregate counters (`:635`) and has no binding sets or nested phase records.
- **Impact:** Two data patterns are not reliably distinguishable from the plan record alone; users lack documented binding/selectivity diagnostics to locate inefficient clauses and nested subqueries.
- **Next validation:** Review output of a query with two patterns, a pushed predicate and a nested query; specify stable clause references, bindings and phase structure before adding optional warnings.

### D03

**Per-transaction semantic work statistics are not exposed/logged**

- **Status:** Partial; high confidence.
- **Requirement:** [datomic_pro_docs/07_peer_api/02_shared_reference/04_transaction_stats.md:18](../datomic_pro_docs/07_peer_api/02_shared_reference/04_transaction_stats.md:18) automatically logs every transaction's tx-stats, with semantic metrics enumerated at `:96`–`:114` for identity resolution, tuple generation, redundancy checks, uniqueness, transaction functions and considered/duplicate datoms.
- **Actual:** [src/service.rs:1469](../src/service.rs:1469) ServiceStats covers queue/processed/report-retention metrics. [src/sql_io.rs:18](../src/sql_io.rs:18), `:65` provides high-level transaction/expansion/assessment phase timing. [src/transaction_hints.rs:155](../src/transaction_hints.rs:155) traces prefixes; [src/tiered_assessor.rs:26](../src/tiered_assessor.rs:26) has crate-private assessment read work. These do not expose the requested resolution/uniqueness/dedup/composite counts and phase times as a transaction-T-correlated public record.
- **Impact:** Hard to diagnose why one transaction costs more than another beyond SQL effects/overall phases, especially transaction-function vs uniqueness/identity/composite work.
- **Next validation:** Instrument or inspect one upsert/composite/redundant transaction and map already available counters; design a public optional report/log sink that reuses current phases and records only real work.

### AO06

**Operational event logging, periodic metric publication and alarms (partial)**

- Requirement: [datomic_pro_docs/08_operations/02_observability_and_tuning/00_monitoring_and_performance.md:49](../datomic_pro_docs/08_operations/02_observability_and_tuning/00_monitoring_and_performance.md:49) describes custom peer/transactor callbacks; `:116` lists heartbeat, latency distributions, indexing/backpressure/cache metrics; `:179` defines alarm reporting. `02_configuring_logging.md:11`–`:25` supplies configurable transactor/peer logging and appenders.
- Present: rich SQL/phase attribution and explicit `SqlMetricCallback` ([src/sql_io.rs:73](../src/sql_io.rs:73), `:112`), `ServiceStats`, `BackgroundIndexingStats`, cache stats and integrity metrics ([src/service.rs:164](../src/service.rs:164), `:1469`; [src/operations.rs:91](../src/operations.rs:91)). Do not claim Atomic lacks metrics.
- Limitation: callback delivery requires an explicit `publish()` and is never automatic ([src/sql_io.rs:272](../src/sql_io.rs:272)); standalone transactor emits READY/STOPPED/errors ([src/bin/atomic.rs:375](../src/bin/atomic.rs:375), `:414`, `:448`) without a metrics/logging configuration. No production logging facade/appender pipeline, periodic exporter, heartbeat latency distribution, or documented alarm stream found across `src`, `docs`, [Cargo.toml](../Cargo.toml). Current SQL stats aggregate totals, not Datomic's full lo/hi/count distributions.
- Impact: stock deployment cannot externally observe all promised operations without a custom polling/export/logging wrapper; failures/backpressure are harder to correlate across processes.
- Confidence: high for publication/logging absence; medium for the complete metric-by-metric coverage boundary. Next validation: make a supported metric/event matrix, start stock transactor under workload/index failure/failover, and inspect emitted events. CloudWatch adapter is ENV02, not the only possible solution.

### P01

**Attribute physical AVET readiness is private**

- **Status:** Missing public introspection; high confidence.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/01_attribute.md:162](../datomic_pro_docs/07_peer_api/01_java/01_attribute.md:162) distinguishes `hasAVET` (currently usable) from `isIndexed` (configured) during background index creation; Clojure key `:has-avet` appears at [datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:86](../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:86).
- **Actual:** Attribute's public fields describe logical schema ([src/schema.rs:53](../src/schema.rs:53)); physical readiness is correctly implemented as crate-private `DatabaseValue::physical_avet_ready` ([src/database_value.rs:2400](../src/database_value.rs:2400)) and `PeerSnapshot::avet_ready` ([src/peer.rs:7765](../src/peer.rs:7765)).
- **Impact:** Applications cannot ask the documented readiness question for one attribute without attempting a read or relying on broader synchronization. This is an exposure gap, not absent background index readiness tracking.
- **Next validation:** Confirm public export inventory and an index-enable-before-backfill fixture; expose the existing exact-value answer with clear attribute identity resolution if chosen.

### ST-01

**Configurable default partition is absent**

- **Documented requirement:** [datomic_pro_docs/04_transactions/07_partitions.md:44](../datomic_pro_docs/04_transactions/07_partitions.md:44) allows a transactor `default-partition` setting, so transactions that omit placement use an application-selected partition.
- **Actual behavior:** [docs/partitions.md:31](../docs/partitions.md:31) explicitly says the Datomic setting has no native runtime mapping. [src/partitions.rs:157](../src/partitions.rs:157), [src/database.rs:2474](../src/database.rs:2474), and [src/tiered_assessor.rs:1554](../src/tiered_assessor.rs:1554) use `USER_PARTITION`; explicit force/match placement already exists.
- **User impact:** Applications cannot change the default once for every writer/client. They must inject placement directives for new entities, including authoring paths that otherwise omit partition decisions.
- **Classification/confidence:** Missing configuration capability, high confidence from explicit documentation and production allocation paths. Medium priority.
- **Next validation:** Inspect the final runtime configuration surface when implementation is scheduled, then demonstrate two transactions with omitted placement under a custom default, including nested maps and retry/restart semantics. Do not alter allocation policy in this audit.

### ST-02

**No committed process-local Rust function/predicate registry**

- **Documented requirement:** [datomic_pro_docs/04_transactions/04_transaction_functions.md:49](../datomic_pro_docs/04_transactions/04_transaction_functions.md:49), `:63`, `:231` supports both transactionally deployed database functions and externally deployed classpath functions; [datomic_pro_docs/03_schema/00_schema_data_reference.md:553](../datomic_pro_docs/03_schema/00_schema_data_reference.md:553), `:584` permits external attribute/entity predicate code. The portable capability is deploying ordinary application-language logic beside the transactor.
- **Actual behavior:** Database functions are substantially implemented using `Program`, `RuntimeValue`, persisted hashes and `:db/fn` bindings ([src/program_bindings.rs:115](../src/program_bindings.rs:115), `:198`). However, `CallableRef::Local` unconditionally returns `program/local-function-not-found` (`:135`), submitted `TxForm::Call` rejects at the authoritative boundary (`:185`), and ordinary native callbacks require the eager speculative API ([src/transaction.rs:1191](../src/transaction.rs:1191)). Registration methods exist for speculative functions and bool-returning predicates ([src/transaction.rs:700](../src/transaction.rs:700), `:707`, `:735`), so this is a boundary gap, not absence of all extension functionality. [docs/programs.md:72](../docs/programs.md:72) also explains restrictions of serializable native code.
- **User impact:** A business rule needing application Rust libraries, callback query/pull extensions, or arbitrary host-language computation cannot simply be registered on a production transactor and invoked by name. It must fit/rewrite into the supported native VM or change Atomic itself. The same ordinary Rust helper is not deployable across speculative and committed paths.
- **Classification/confidence:** Partial portable extensibility, high confidence. Medium/high priority depending on intended extension model. Clojure/JVM binary execution is platform-specific and is not automatically required to close the Rust capability gap.
- **Next validation:** Decide whether a transactor-local native registry is a supported product goal or an intentional portability boundary; then exercise a concrete application function outside current VM operators with db-before semantics and a predicate with db-after semantics. Preserve existing persisted-program equivalents in any backlog description.

### ST-05

**Schema-aware raw-index builders reject partial tuple seek values**

- **Documented requirement:** [datomic_pro_docs/06_indexes/05_rseek_datoms.md:28](../datomic_pro_docs/06_indexes/05_rseek_datoms.md:28) describes virtual start positions; `:41` links an explicit partial-tuple seek illustration. A prefix tuple used as a virtual boundary need not be a valid stored tuple of the full attribute arity.
- **Actual behavior:** `DatabaseValue::avet_boundary` calls `normalize_index_value` ([src/database_value.rs:2564](../src/database_value.rs:2564)), which invokes stored-value schema validation at `:2708`. The tuple normalizer directly rejects shortened heterogeneous/composite values at `:2731`–`:2737`; [src/schema.rs:1093](../src/schema.rs:1093)–`:1124` enforces stored arity as well. This affects both `RawIndexValue::Tuple` and `RawIndexValue::Stored(Value::Tuple(...))` through the convenience normalizer.
- **Existing workaround/equivalent:** A caller who resolves IDs themselves can construct `IndexBoundary::Avet(IndexComponents::Two(attr_id, Value::Tuple(shorter_slots)))` directly; the lower-level boundary access validator only checks the boundary and AVET readiness ([src/database_value.rs:2842](../src/database_value.rs:2842)), while the comparator can position virtual tuples. Therefore this is not absence of forward/reverse seeks.
- **User impact:** The documented schema-aware authoring path rejects legitimate partial-tuple seek positions, particularly awkward for tuple slots containing idents or lookup refs that otherwise benefit from normalization.
- **Classification/confidence:** Partial index API authoring/validation, high confidence in rejection path; medium priority.
- **Next validation:** Reproduce the documentation's two-column tuple example using one-slot `RawIndexValue` starts in forward/reverse directions, compare manually normalized `IndexBoundary`, and cover nil/ref tuple slots and empty results. Treat virtual-boundary validation separately from stored-value validation if changed later.

### P03

**Entity identity-based equality is not defined**

- **Status:** Missing public value behavior; high confidence; smaller ergonomic gap.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/06_entity.md:11](../datomic_pro_docs/07_peer_api/01_java/06_entity.md:11) says entities compare equal if their entity IDs and database IDs match.
- **Actual:** [src/pull.rs:529](../src/pull.rs:529) derives Clone/Debug only; no PartialEq/Eq/Hash implementation or public equivalent identity method exists. Entity exposes `id()` and `database()` at `:594`, `:598`; DatabaseValue identity is not a comparable globally unique public entity-identity token.
- **Impact:** Applications cannot directly deduplicate/compare lazy Entity handles according to the documented identity contract, including handles from different time views of the same database.
- **Next validation:** Decide whether to provide explicit entity identity or equality traits; validate equal ID within one lineage vs same numeric ID across different lineages and restored database addresses. Do not use full attribute-value equality.

### P04

**Nonblocking completion and async client facade**

- **Status:** Partial async ergonomics; high confidence; lower priority than core capability.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/07_listenable_future.md:17](../datomic_pro_docs/07_peer_api/01_java/07_listenable_future.md:17) and Clojure `add-listener` at [datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:18](../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:18) specify once-only completion listeners, including immediate registration after completion.
- **Actual:** Nonblocking admission exists ([src/connection.rs:526](../src/connection.rs:526)). ConnectionTransactionTicket and TransactionTicket provide consuming blocking waits/request keys ([src/connection.rs:679](../src/connection.rs:679), [src/service.rs:1325](../src/service.rs:1325)), without callback registration or Future implementation. Sync methods are blocking Rust methods with timeouts ([src/connection.rs:463](../src/connection.rs:463)).
- **Impact:** Event-loop applications must dedicate blocking workers or write adapter code to await completion; shared reusable futures/listeners are not part of the public contract.
- **Next validation:** Decide whether native Future/async adapters are desirable and specify cancellation/drop semantics independently of transaction durability; avoid porting Java Executor classes literally.

The same gap extends beyond transaction listeners: [datomic_pro_docs/09_optional/00_pro_client/03_client_library_reference.md:37](../datomic_pro_docs/09_optional/00_pro_client/03_client_library_reference.md:37) specifies operations that never block callers, and `05_client_api_async.md:18` describes asynchronous chunk streams. Atomic query/Pull ([src/connection.rs:603](../src/connection.rs:603), `:626`), log traversal ([src/native_log.rs:222](../src/native_log.rs:222)) and remote submission ([src/remote_transport.rs:412](../src/remote_transport.rs:412)) expose synchronous calls. Internal Tokio use is not a public async SDK. A native Future/Stream facade is a portable option; no Clojure core.async dependency is required. Validate from a single-thread executor and keep cancellation distinct from durable transaction outcome.

### P05

**Lazy backup-backed database and log connection**

- **Status:** Partial capability, high confidence. Online fixed values already exist through captured `DatabaseValue`/`LogValue` and an unsynced `Peer`; no extra absence is claimed for that spelling.
- **Requirement:** [datomic_pro_docs/09_optional/02_specialized_operations/00_read_only_connections.md:29](../datomic_pro_docs/09_optional/02_specialized_operations/00_read_only_connections.md:29) describes direct backup connections, `:33` queries without restore, and `:79` fixed db/log access through the cache stack. [datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md:122](../datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md:122) repeats the workflow.
- **Present:** `PortableBackup::verify_backup` and `verify_backup_point` work offline and return `BackupVerification.database` ([src/backup.rs:86](../src/backup.rs:86), `:782`, `:795`). Applications can already query that eager database without restoring PostgreSQL.
- **Limitation/impact:** Verification reconstructs the entire eager database ([src/backup.rs:828](../src/backup.rs:828)); native lazy peer constructors accept PostgreSQL config ([src/peer.rs:4818](../src/peer.rs:4818), [src/connection.rs:95](../src/connection.rs:95)). No public lazy backup-backed database/log connection or integrated backup cache path was found. Selective offline reads pay full materialization, and ordinary lazy backup log traversal is unavailable.
- **Next validation:** Demonstrate the existing eager path, then specify one selective backup index/log query through the usual native cache stack. This is a scoped extension, not a new backup engine or a claim that backups cannot be queried.

### AO02

**Lightweight remote query/Pull/index/log client and Peer Server gateway (missing)**

- Requirement: [datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md:9](../datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md:9)–`:30`: lightweight processes send reads and writes to a gateway, with database/query/cache work on the server. `03_client_library_reference.md:191` describes chunked results and `:290` offset/limit.
- Limitation: native network API is `Connection::transact_remote` ([src/remote_transport.rs:412](../src/remote_transport.rs:412)) plus explicit PostgreSQL-backed endpoint discovery (`:377`); `Connection` itself opens a PostgreSQL peer ([src/connection.rs:157](../src/connection.rs:157)). Query and Pull execute locally against `self.db()` ([src/connection.rs:603](../src/connection.rs:603), `:626`). [docs/application.md:92](../docs/application.md:92) explicitly says PostgreSQL credentials still authorize each peer's direct reads. No read/query gateway protocol or thin-client type is exported ([src/lib.rs:178](../src/lib.rs:178)).
- Impact: every reader needs a native peer/runtime/storage connection; applications cannot offload query memory/cache/CPU or use a supported network-only client. Network chunk streaming, gateway multi-database serving and its client auth rotation are consequently absent too.
- Confidence: high. Next validation: define one external-client workflow with no PostgreSQL connection performing transact/query/Pull/tx-range and receiving chunks; verify protocol/API scope before implementation.

### AO07

**Optional HTTP health endpoint (missing tooling)**

- Requirement: [datomic_pro_docs/08_operations/00_architecture_and_storage/01_transactor_reference.md:58](../datomic_pro_docs/08_operations/00_architecture_and_storage/01_transactor_reference.md:58)–`:71` offers opt-in HTTP `/health`; [datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md:105](../datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md:105) provides gateway health.
- Present: service/listener `is_available()` methods and fail-fast CLI supervision ([src/service.rs:1319](../src/service.rs:1319); [src/remote_transport.rs:324](../src/remote_transport.rs:324); [src/bin/atomic.rs:390](../src/bin/atomic.rs:390)).
- Limitation: no HTTP route/server or health listener configuration in the supported CLI/API; the TLS listener handles a binary transaction protocol, and `atomic status` observes catalog coordinates rather than live process readiness ([docs/admin.md:29](../docs/admin.md:29)).
- Impact: generic HTTP probes require an external adapter. This is optional deployment convenience, not proof that HA is broken.
- Confidence: high. Next validation: decide liveness/readiness semantics and test one explicit health endpoint with available/unavailable service states.

### AO08

**Shared network immutable cache tier / Memcached (missing optional feature)**

- Requirement: [datomic_pro_docs/08_operations/02_observability_and_tuning/01_memory_and_caching.md:43](../datomic_pro_docs/08_operations/02_observability_and_tuning/01_memory_and_caching.md:43)–`:75`: configurable shared Memcached, immutable segment write-through/read-fill, multiple nodes, SASL and optional ElastiCache discovery.
- Present: native RAM and local SSD caching ([src/peer.rs:8438](../src/peer.rs:8438); [src/ssd_cache.rs:105](../src/ssd_cache.rs:105)).
- Limitation: no Memcached client/configuration/protocol or shared network cache tier found in native source/dependencies/config. Search covered memcache/memcached/SASL/ElastiCache; Datomic distribution references are not Atomic implementation. Native cold reads go SSD then PostgreSQL ([src/peer.rs:8438](../src/peer.rs:8438), `:8455`).
- Impact: cache sharing across machines and cache lifetimes independent of local SSDs are unavailable.
- Confidence: high. Next validation: establish workload value and explicit choice to implement a shared cache; use fail-open cache corruption/unavailability acceptance. Not a requirement to adopt an alternate durable backend.

### AO09

**SSD cache does not cache log transaction payloads (partial Valcache parity)**

- Requirement: [datomic_pro_docs/09_optional/02_specialized_operations/01_valcache.md:13](../datomic_pro_docs/09_optional/02_specialized_operations/01_valcache.md:13) explicitly caches both index and log segments; `:16` stays hot across restart.
- Present: authenticated durable SSD caching of native tree nodes and reopen coverage ([src/peer.rs:8438](../src/peer.rs:8438), `:8479`; [tests/native_ssd_cache.rs:13](../tests/native_ssd_cache.rs:13)). Writer readers also share native tree machinery, so do not assume SSD is peer-only.
- Limitation: log traversal unconditionally checks pins/lineage and calls `read_authenticated_log_range` ([src/native_log.rs:222](../src/native_log.rs:222)–`:254`); no SSD payload lookup/fill path there or in log recovery. SSD use sites in the native read implementation cover tree nodes.
- Impact: repeated historical log scans/recovery cannot reuse cached log segments from SSD as the described feature allows.
- Confidence: high for log cursor; medium for every indirect recovery path. Next validation: inspect repeated/reopened log scans with RAM disabled and inspect SQL/SSD payload counters; decide immutable log-block representation before extension.

### ST-04

**Transaction-hint prefetch concurrency is fixed**

- **Documented requirement:** [datomic_pro_docs/04_transactions/08_transaction_hints.md:29](../datomic_pro_docs/04_transactions/08_transaction_hints.md:29) says concurrent prefetch reads can be tuned using `datomic.prefetchConcurrency`.
- **Actual behavior:** Hints and concurrent overlap with authority work exist, but [src/transaction_hints.rs:429](../src/transaction_hints.rs:429) documents at most one worker per writer and eight per process; `:550`/`:569` consumes hinted prefixes serially. Public `HintPrefetchOptions` (`:258`) configures counts, bytes, time and cancellation, not concurrency. The eight-worker process ceiling is a constant at `:22`.
- **User impact:** Operators cannot increase independent prefetch read concurrency for a writer with greater CPU/I/O capacity. This is a missing tuning control, not evidence that hints are ineffective or that any particular workload is slow.
- **Classification/confidence:** Partial operational performance control, high confidence; lower priority than functional gaps. Deduplicate with the operations/system-properties audit.
- **Next validation:** Identify the intended native configurable concurrency model, then measure a real latency-bound multi-prefix transaction before implementing a new setting. Existing bounded worker/timeout behavior must remain deliberate.

### AO10

**Backup/restore pacing and concurrency; GC pacing (missing tuning controls)**

- Requirement: [datomic_pro_docs/08_operations/02_observability_and_tuning/03_system_properties.md:90](../datomic_pro_docs/08_operations/02_observability_and_tuning/03_system_properties.md:90)–`:98` defines backup pacing and file/S3 backup concurrency; [datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:312](../datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:312)–`:318` defines GC pacing.
- Present: durable differential copy, resumable bounded GC batches and bounded upload sizes ([src/backup.rs:297](../src/backup.rs:297); [src/operations.rs:847](../src/operations.rs:847); [src/tree_store.rs:442](../src/tree_store.rs:442)). These are substantial implementations.
- Limitation: backup API accepts database/path/fault, with no pacing/concurrency options ([src/backup.rs:297](../src/backup.rs:297)); backup/restore CLI admits no such flags ([src/bin/atomic/admin.rs:44](../src/bin/atomic/admin.rs:44)). GC offers finite batch count but no per-operation pace setting (`:61`). Bounded victims/upload size does not implement adjustable rate or parallelism.
- Impact: operators have less control over throughput vs concurrent application I/O pressure without custom external scheduling.
- Confidence: high for absent public controls. Next validation: first measure representative backup/restore/GC workload interference; introduce only controls whose semantics can be verified.

### AO11

**Configurable parallel indexing across index work (partial performance capability)**

- Requirement: [datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:182](../datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md:182)–`:190` offers index-parallelism 1–8.
- Present: one background index worker, batched native uploads and optional compression/upload overlap ([src/service.rs:164](../src/service.rs:164); [src/tree_store.rs:599](../src/tree_store.rs:599), `:1079`). Do not claim there is no parallel work anywhere.
- Limitation: `BackgroundIndexingConfig` contains only threshold/max bytes ([src/service.rs:49](../src/service.rs:49)); index sorts are built in sequential loops ([src/peer.rs:1693](../src/peer.rs:1693), `:2536`) and no public index-worker parallelism setting was found. Compression overlap is distinct from configurable index-sort parallelism.
- Impact: operators cannot allocate spare CPU/storage concurrency to index work using the documented kind of control.
- Confidence: high for control absence, medium for performance benefit. Next validation: profile current index phases and benchmark independent sort/build work under bounded parallelism; no speedup assumed.

### AO13

**Graphical database console (missing optional tool)**

- Requirement: [datomic_pro_docs/09_optional/04_tools_and_support/00_datomic_pro_console.md:9](../datomic_pro_docs/09_optional/04_tools_and_support/00_datomic_pro_console.md:9) describes schema/query/entity/transaction/index exploration; `:35` temporal selectors and `:121` named data sources.
- Present: native APIs and administrative CLI; goal-1 is concurrently adding query/Pull/transaction text CLI ([src/bin/atomic/data.rs:20](../src/bin/atomic/data.rs:20)).
- Limitation: no Atomic graphical console/web application or UI command found in source/files/package dependencies. Datomic's bundled console and screenshots are reference material, not a Rust-port UI. Viewed console-window.png confirms UI beyond plain query execution.
- Impact: interactive graph/schema/history browsing and graphical query construction require an external/custom tool.
- Confidence: high. Next validation: scope a small read-only browser against existing APIs; this should not duplicate EDN frontend work.

### TU-01

**In-memory engine has no supported named Connection/transact lifecycle**

- **Documented capability:** [datomic_pro_docs/01_tutorials/00_peer_mem_db_getting_started.md:25](../datomic_pro_docs/01_tutorials/00_peer_mem_db_getting_started.md:25) explicitly uses local memory without a separate transactor; `:49`, `:63`, `:75` create and connect to a named memory database; subsequent schema/data/query/history steps use the ordinary connection transaction API.
- **What Atomic already has:** `Database` is an actual immutable single-process engine ([src/database.rs:325](../src/database.rs:325)), with bootstrap (`:369`), schema construction (`:378`), pure `with_forms` ([src/transaction.rs:881](../src/transaction.rs:881)), and the shared query API ([src/query.rs:1554](../src/query.rs:1554)). Existing test sources exercise real transaction and historical semantics in memory. A caller could own/reassign successor values itself.
- **What is absent:** All public `Connection` creation paths lead to PostgreSQL ([src/connection.rs:80](../src/connection.rs:80), `:95`, `:107`, `:157`, `:204`). Starting the transaction service opens `PostgresStore` and `PostgresIndexer` ([src/service.rs:1646](../src/service.rs:1646), `:1665`). No supported memory-backed named create/connect transaction service substitutes behind those APIs. The main public workflow explicitly requires PostgreSQL ([examples/native_workflow.rs:21](../examples/native_workflow.rs:21); [README.md:86](../README.md:86)).
- **User impact:** Lightweight tests, demos and embedded applications cannot use the same ordinary named connection/transact lifecycle without a PostgreSQL setup. They instead drive the pure immutable engine themselves or provision PostgreSQL. Do not describe this as “Atomic has no in-memory database.”
- **Classification/confidence:** Partial memory development/embedded workflow, high confidence. Scope decision needed on whether backend-neutral Connection is desired; the audit does not mandate parity with a Datomic URI string.
- **Next validation:** Inventory the final public constructors after concurrent work settles, then write a small no-PostgreSQL acceptance scenario only when implementing: create a named memory database, attach two connections in one process, transact schema/data, observe old/new values and history, and define memory lifetime/name cleanup. Reuse the existing engine.

## Explicit behavior and product-scope choices

### Q05

**Standard Clojure function names are a library compatibility choice**

- **Status:** Platform/library difference, not a missing general custom-function capability; medium confidence on desired scope.
- **Requirement:** [datomic_pro_docs/05_query_and_pull/02_query_reference.md:851](../datomic_pro_docs/05_query_and_pull/02_query_reference.md:851) includes all clojure.core functions except eval; `:801` and `:1087` illustrate quot/count/subs; `:1043` documents Java method invocation.
- **Actual:** `Function`/`Predicate` are finite native enums ([src/query.rs:106](../src/query.rs:106), `:117`), while registered Rust callbacks/native programs provide ordinary custom function execution (`:755`, `:773`). Pull likewise has Rust callback transforms ([src/pull.rs:97](../src/pull.rs:97)).
- **Impact:** Literal Clojure/Java function names do not all map automatically to standard Rust behavior. Portable string/count/collection conveniences could be supplied as a compatibility library if desired. JVM classpath loading, reflection and Clojure evaluation should remain explicitly outside the Rust port unless separately requested.
- **Next validation:** Choose a bounded list of portable documented functions used by examples, map existing equivalents/registry registrations, and leave frontend name-resolution mechanics with goal-1.

### Q06

**Clause-order tuning contract differs**

- **Status:** Documented behavioral/performance difference; high confidence; decide whether intentional.
- **Requirement:** [datomic_pro_docs/05_query_and_pull/01_executing_queries.md:123](../datomic_pro_docs/05_query_and_pull/01_executing_queries.md:123) recommends putting selective clauses first; [datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md:12](../datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md:12) says evaluation follows user clause order except guaranteed winning reorderings.
- **Actual:** [src/query.rs:1763](../src/query.rs:1763) scans all ready clauses and picks the highest `clause_score`, replacing the selection on `>=` ties (thus later equally scored clauses win). `clause_score` at `:2254` ranks clause kinds and bound positions. `QueryControl` at `:676` has no preserve-input-order option.
- **Impact:** Merely reordering a Datomic query according to the documented tuning guidance may not produce the same execution order/performance. This is not evidence of wrong result sets; Atomic already exposes actual coarse execution order.
- **Next validation:** Inspect a selective/unselective two-pattern query with reversed source order, compare plans and decide whether to document Atomic's optimizer or add an author-order option.

### TU-02

**Standalone disk-backed dev storage mode is absent**

- **Documented capability:** [datomic_pro_docs/01_tutorials/02_run_a_transactor.md:11](../datomic_pro_docs/01_tutorials/02_run_a_transactor.md:11) uses local disk files through `dev` storage; `03_connect_to_a_database.md:30` then creates/connects to that backend via the transactor.
- **Actual behavior:** Atomic has a local native transactor and real persistent storage through PostgreSQL. The supported CLI obtains PostgreSQL configuration before normal operations ([src/bin/atomic.rs:223](../src/bin/atomic.rs:223)), creates databases using `PostgresStore` (`:242`), and the transaction service unconditionally opens the PostgreSQL store/indexer ([src/service.rs:1646](../src/service.rs:1646), `:1665`). `PostgresTreeStore` is the concrete durable tree store ([src/tree_store.rs:538](../src/tree_store.rs:538)). The current product setup explicitly requires PostgreSQL ([docs/application.md:14](../docs/application.md:14)). Existing SSD-cache files do not provide an authoritative transaction log/database backend.
- **User impact:** Users cannot start a persistent development database by pointing the Atomic transactor at an empty filesystem directory without installing/running PostgreSQL. Local development remains supported through PostgreSQL, so the gap is a deployment/storage mode rather than absence of durable local use.
- **Classification/confidence:** Missing optional backing-store/development mode, high confidence. Deduplicate with operation/storage docs; this may be an intentional PostgreSQL-only product boundary rather than an implementation priority.
- **Next validation:** Record the backend scope decision. If desired, specify durable log/head/index/crash-recovery behavior for a native file backend and demonstrate tutorial create/restart/query without PostgreSQL. A local transport or export file by itself does not satisfy this requirement.

## Candidates requiring validation

### ST-03

**Automatic schema/partition allocation can exhaust its allowed ID range through ordinary data growth**

- **Documented requirement:** New schema is ordinary transaction data ([datomic_pro_docs/03_schema/00_schema_data_reference.md:66](../datomic_pro_docs/03_schema/00_schema_data_reference.md:66)), with the limit expressed as fewer than 2^20 **schema elements** (`:669`). Named partitions are installed by ordinary entity-map transactions ([datomic_pro_docs/04_transactions/07_partitions.md:52](../datomic_pro_docs/04_transactions/07_partitions.md:52)).
- **Actual behavior:** The temporary-ID allocator uses a single `allocation_start`/`next` across partitions ([src/tiered_assessor.rs:1544](../src/tiered_assessor.rs:1544)–`:1566`; eager counterpart [src/database.rs:2474](../src/database.rs:2474)). Schema/partition tempids get system affinity ([src/partitions.rs:30](../src/partitions.rs:30)–`:65`), but still receive the same global frontier. Schema attribute EIDs must be at most 1,048,576 ([src/vocabulary.rs:108](../src/vocabulary.rs:108)–`:124`); named partition entities must be below 524,288 ([src/schema.rs:810](../src/schema.rs:810)–`:816`). [docs/partitions.md:25](../docs/partitions.md:25) explicitly documents the shared frontier and `:33` the early named-partition installation constraint.
- **User impact:** After enough ordinary entity/transaction issuance, an ordinary anonymous/tempid schema map or named-partition map can fail even with a small schema and few named partitions. This is especially relevant for long-lived databases adding features after an import.
- **Important limit on the finding:** This does **not** establish that all late schema installation is impossible. An explicitly selected unused low schema ID below the frontier is accepted by the typed installation path ([src/tiered_assessor.rs:620](../src/tiered_assessor.rs:620)–`:636`), and ordinary explicit IDs may provide a similar partition workaround. The missing piece is normal automatic allocation independent of data volume, not necessarily remaining physical address space.
- **Classification/confidence:** Partial schema/partition evolution; high confidence in the inspected allocation arithmetic, medium/high confidence in the exact end-user failure without a boundary reproduction. High priority for validation.
- **Next validation:** Use a small legitimate high-frontier fixture or a controlled import to cross each threshold, then attempt the documented tempid/map installations and compare the explicit-unused-low-ID workaround. Check Datomic 1.0.7705 allocation/version behavior before choosing migration policy; do not redesign existing encoded identities during this audit.

### SC-01

**Composite-identity upsert documentation versus existing semantics**

[datomic_pro_docs/03_schema/00_schema_data_reference.md:394](../datomic_pro_docs/03_schema/00_schema_data_reference.md:394) broadly says a composite unique identity makes assertions about the same constituent combination resolve to one entity, while `:396` says composites are managed automatically. Atomic explicitly tests that constituent-only assertions with a fresh tempid fail with `transaction/unique-conflict` when the derived composite already exists ([tests/semantic_conformance.rs:620](../tests/semantic_conformance.rs:620)); supplying an explicit composite value acts as an upsert hint (`:580`). This may be a limitation of Datomic itself, an overbroad documentation sentence, or a target-version difference, rather than an Atomic gap. Current source ordering resolves tempids before deriving composites ([src/tiered_assessor.rs:480](../src/tiered_assessor.rs:480), `:2144`). **Next action:** verify the exact documented example against the target Datomic release or its recovered source, then classify as semantic gap or required compatibility guidance. Do not change behavior based solely on the broad sentence.

### SC-02

**NaN replacement is more permissive than the schema-reference warning appears to require**

[datomic_pro_docs/03_schema/00_schema_data_reference.md:679](../datomic_pro_docs/03_schema/00_schema_data_reference.md:679) says replacing an attribute whose current value is NaN requires an explicit earlier retraction. Atomic gives NaNs logical/index equality ([src/value_numeric.rs:68](../src/value_numeric.rs:68), `:140`; [src/value.rs:226](../src/value.rs:226)) and its cardinality-one replacement path generates an implicit retraction of any different existing value, with no NaN guard ([src/tiered_assessor.rs:2107](../src/tiered_assessor.rs:2107)–`:2141`). NaN uniqueness/upsert is explicitly rejected elsewhere ([src/tiered_assessor.rs:1360](../src/tiered_assessor.rs:1360), `:1492`). Existing numeric test source covers NaN storage/retraction/redundancy ([tests/database_validation_scaling.rs:167](../tests/database_validation_scaling.rs:167)) but does not establish the documented cardinality-one replacement edge. **Next action:** compare a nonunique cardinality-one Double/Float NaN replacement with target Datomic behavior. This is a semantic compatibility candidate, not a missing general numeric feature.

### P06

**Bound database function invocation requires manual plumbing**

- **Status:** Public API candidate, not absence of native programs; medium confidence on user impact.
- **Requirement:** [datomic_pro_docs/07_peer_api/01_java/03_database.md:256](../datomic_pro_docs/07_peer_api/01_java/03_database.md:256) and [datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:551](../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md:551) provide `invoke(db, eid-or-ident, args)` by database function identity.
- **Actual:** Native database callable resolution exists but is private ([src/program_bindings.rs:111](../src/program_bindings.rs:111)); public `ProgramRuntime::execute_query` accepts a resolved Program and captured DatabaseValue ([src/program.rs:1380](../src/program.rs:1380)). Callers can manually resolve entity, read :db/fn, load a blob with `PostgresStore::resolve_program` ([src/postgres.rs:3321](../src/postgres.rs:3321)) and invoke an appropriate program. Transaction native function calls already work through ProgramCall/CallableRef.
- **Impact:** Peer-local direct invocation by database entity/ident lacks one public path owning exact binding/code resolution and general result behavior. This may merit a convenience API rather than a new execution engine.
- **Next validation:** Try the documented direct-invoke workflow with an old immutable value and rebound function, using a read-only peer. Determine whether public immutable blob resolution is sufficient without granting writer authority. Classify supported native-program roles/results before committing scope.

### OC-C1

**OC-C1 — SQL analytics integration: candidate, optional ecosystem feature.**
  Changelog `datomic_pro_docs/00_start_here/02_datomic_pro_change_log.md:549-556` announces analytics
  and SQL COUNT(*) handling, with later Presto upgrades at 457/469. No public SQL
  query/Presto connector appears in [src/lib.rs](../src/lib.rs), [src/bin/atomic.rs](../src/bin/atomic.rs), native query
  entry points (`src/query.rs:1021-1053`) or the source/docs search. PostgreSQL is
  Atomic's storage authority, not a SQL projection of its logical datoms. Impact:
  SQL/BI clients cannot currently query Atomic's logical schema through an
  established connector. The complete analytics spec is not included in this
  corpus, so scope/continuing applicability require reading the linked official
  analytics docs before promoting this to an implementation requirement. Do not
  infer that native Datalog analytics workloads are missing.

### AO-C01

**AO-C01 — Automatic remote route discovery/reconnection:** Datomic deployment doc line 49 describes automatic peer reconnect. Atomic reads automatically recover independently, but applications explicitly call `discover_remote_writer` then `transact_remote`; [docs/application.md:133](../docs/application.md:133) demonstrates rediscovery per explicit request/restart. Existing single-endpoint submission has no automatic endpoint replacement. Verify whether a desired connection-bound remote writer adapter is a separate missing convenience or already accepted architecture; do not confuse route refresh with retrying arbitrary effects.

### AO-C02

**AO-C02 — Cross-version rolling upgrade support:** Datomic deployment doc lines 260–307 describes compatible rolling upgrades. Atomic migration guides require quiescence for schema changes and preserve checksummed prefixes; current-source failover tests do not establish mixed-version operability. Compare supported migration/version gates and retained fixture versions before declaring live upgrade missing globally.

### AO-C03

**AO-C03 — PID-file and multi-database transactor packaging:** transactor reference line 40 describes a PID file; capacity doc line 194 supports multiple databases per process. Stock CLI has one `--database` and no PID-file option; a Rust process can instantiate multiple services. These are small packaging differences to prioritize only if needed, not missing core DB capabilities.

## Ecosystem differences and optional integrations

### ENV01

**ENV01 — Backend/deployment alternatives:** SQL vendors other than PostgreSQL, Cassandra, DynamoDB/local DynamoDB, embedded dev storage, backend URI switching and Datomic AMI/provisioning are documented in [datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.md:17](../datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.md:17), `:47`, `:107`, `:179`, `:392` and [datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md](../datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md). Atomic explicitly uses native PostgreSQL ([src/lib.rs:1](../src/lib.rs:1); [Cargo.toml](../Cargo.toml)) plus a pure in-memory `Database` oracle. Record as unsupported alternatives, not defects within PostgreSQL semantics. Named transient server/connection packaging is separate from the existing pure memory engine.

### ENV02

**ENV02 — Optional AWS integrations:** direct S3 backups plus SSE ([datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md:9](../datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md:9), `:42`), S3 log rotation, CloudWatch metrics ([datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md:110](../datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md:110)) and IAM role-based SDK credentials (`01_aws_access_control.md:7`) are not implemented. Backup destinations are filesystem paths ([src/backup.rs:297](../src/backup.rs:297)), and [Cargo.toml](../Cargo.toml) has no AWS client. These are useful optional operational integrations even for PostgreSQL deployments; no claim they are incompatible with Rust. Original CloudFormation tools explicitly deprecated in source doc line 9 should not be reproduced automatically.

### ENV03

**ENV03 — Host/runtime/distribution APIs:** JVM tuning flags, GC metrics, Java keystores, logback/SLF4J names, Maven coordinates, legacy license keys, private JAR publishing and Clojure REPL instructions are not native Rust database requirements. Portable intent is represented by the relevant cache/config/diagnostic/tooling findings. Rust byte-content equality removes the Java byte-array workaround rather than creating a gap.


Native fulltext explicitly supports a bounded grammar and native ranking rather
than identical Lucene syntax/scores ([docs/fulltext.md:7](../docs/fulltext.md:7), `:60`). The local schema
chapter specifies case-insensitivity, apostrophe handling and stop words, which
have implementations; it does not by itself establish every Lucene operator as a
requirement. Advanced syntax parity would need a separate, precise requirement,
not a blanket claim that fulltext is absent. Likewise Cargo's `publish=false`
records source-local distribution, not evidence about any external release service.

## Supporting files and inventory checks

All 21 PNGs were visually inspected across the review: topology, two entity
projections, six seek examples, and twelve console screenshots. Their capabilities
are accounted for in the parent documents (not 21 additional feature tickets).
Five diagram SVGs had their XML/text labels inspected for topology/cache/monitoring
requirements; the two SVG logos are branding-only inventory. The CSV's 90 unique
paths exactly match all Markdown files on disk, with no missing or unlisted files.

| File | Accounting |
| --- | --- |
| [x] [00_start_here/_images/00_introduction/topology-abstract.png](../datomic_pro_docs/00_start_here/_images/00_introduction/topology-abstract.png) | Visually inspected; parent-document capability accounted for |
| [x] [01_tutorials/_images/01_peer_tutorial/datomic-logo-documentation-horizontal.svg](../datomic_pro_docs/01_tutorials/_images/01_peer_tutorial/datomic-logo-documentation-horizontal.svg) | Branding asset; no independent API requirement |
| [x] [02_core_concepts/_images/03_entities/entities-basics.png](../datomic_pro_docs/02_core_concepts/_images/03_entities/entities-basics.png) | Visually inspected; parent-document capability accounted for |
| [x] [02_core_concepts/_images/03_entities/entities-time.png](../datomic_pro_docs/02_core_concepts/_images/03_entities/entities-time.png) | Visually inspected; parent-document capability accounted for |
| [x] [05_query_and_pull/_images/00_query/datomic-logo-documentation-horizontal.svg](../datomic_pro_docs/05_query_and_pull/_images/00_query/datomic-logo-documentation-horizontal.svg) | Branding asset; no independent API requirement |
| [x] [06_indexes/_images/01_index_model/clientarch-orig.svg](../datomic_pro_docs/06_indexes/_images/01_index_model/clientarch-orig.svg) | SVG XML/text labels inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms1-attr-only.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms1-attr-only.png) | Visually inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms2-attr-value.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms2-attr-value.png) | Visually inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms3-tuple-value.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms3-tuple-value.png) | Visually inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms4-value-not-in-index.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms4-value-not-in-index.png) | Visually inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms5-partial-tuple.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms5-partial-tuple.png) | Visually inspected; parent-document capability accounted for |
| [x] [06_indexes/_images/05_rseek_datoms/rseek-datoms6-multiple-entities-same-value.png](../datomic_pro_docs/06_indexes/_images/05_rseek_datoms/rseek-datoms6-multiple-entities-same-value.png) | Visually inspected; parent-document capability accounted for |
| [x] [08_operations/01_capacity_and_reliability/_images/00_capacity_planning/transactor-memory.svg](../datomic_pro_docs/08_operations/01_capacity_and_reliability/_images/00_capacity_planning/transactor-memory.svg) | SVG XML/text labels inspected; parent-document capability accounted for |
| [x] [08_operations/02_observability_and_tuning/_images/00_monitoring_and_performance/monitoring.svg](../datomic_pro_docs/08_operations/02_observability_and_tuning/_images/00_monitoring_and_performance/monitoring.svg) | SVG XML/text labels inspected; parent-document capability accounted for |
| [x] [09_optional/00_pro_client/_images/01_peer_server/clientarch-client.svg](../datomic_pro_docs/09_optional/00_pro_client/_images/01_peer_server/clientarch-client.svg) | SVG XML/text labels inspected; parent-document capability accounted for |
| [x] [09_optional/02_specialized_operations/_images/01_valcache/valcache.svg](../datomic_pro_docs/09_optional/02_specialized_operations/_images/01_valcache/valcache.svg) | SVG XML/text labels inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-dataset.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-dataset.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-datasources.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-datasources.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-entities.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-entities.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-indexes.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-indexes.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db-input.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db-input.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db1-input.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db1-input.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-tuple-input.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-tuple-input.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-schema.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-schema.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-specify-db.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-specify-db.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-transactions.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-transactions.png) | Visually inspected; parent-document capability accounted for |
| [x] [09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-window.png](../datomic_pro_docs/09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-window.png) | Visually inspected; parent-document capability accounted for |
| [x] [datomic_pro_docs_manifest.csv](../datomic_pro_docs/datomic_pro_docs_manifest.csv) | Manifest reconciled: 90 unique paths, exact disk match |

## Verification and continuation

- Coverage reconciled against all 90 Markdown files and all 29 supporting files;
  no document is silently skipped. Historical changelog/dependency entries were
  assessed for applicability rather than automatically copied into the backlog.
- Root inspection challenged important findings against actual code: relation
  source arity, Aggregate/QueryInput/QuerySequence public types, partial-tuple
  normalization, private AVET readiness, catalog INSERT behavior, stock transactor
  startup and the explicit excision worker workflow. Other detailed findings retain
  independently inspected code/test evidence and explicit confidence levels.
- No Cargo or PostgreSQL tests ran in this audit. Existing acceptance reports and
  test sources are historical/intended evidence, not new passing results. No
  performance difference is inferred merely from a missing tuning knob.
- Duplicate backup, async, catalog, diagnostics and entity-equality reports were
  merged. EDN itself and input-bound Pull normalization remain with Goal1.
- First static identification pass is complete. For a deeper follow-up, start
  with ST-03, SC-01, SC-02 and P06, then AO-C01/AO-C02; each record states the next
  evidence needed. Recheck affected source after Goal1 settles. Implementation
  requires its own authorized scope and does not belong to this audit task.
