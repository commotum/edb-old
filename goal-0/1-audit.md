# Datomic documentation audit

Review started 2026-09-10. This is source inspection, not a test execution report.
Initial committed baseline: `8735731fe7cdce72bbb02ed01efb1a3ae0bca52f`.
Concurrent EDN changes are not assumed complete.

The inventory contains 90 Markdown files. `[ ]` means pending; `[x]` means
read and compared, not that Atomic passes every requirement.

## Per-file checklist

### 00_start_here

- [ ] [00_start_here/00_introduction.md](../datomic_pro_docs/00_start_here/00_introduction.md)
- [ ] [00_start_here/01_datomic_pro_releases.md](../datomic_pro_docs/00_start_here/01_datomic_pro_releases.md)
- [ ] [00_start_here/02_datomic_pro_change_log.md](../datomic_pro_docs/00_start_here/02_datomic_pro_change_log.md)
- [ ] [00_start_here/03_release_notices.md](../datomic_pro_docs/00_start_here/03_release_notices.md)
- [ ] [00_start_here/04_pro_setup.md](../datomic_pro_docs/00_start_here/04_pro_setup.md)
- [ ] [00_start_here/05_accessing_the_peer_library.md](../datomic_pro_docs/00_start_here/05_accessing_the_peer_library.md)
- [ ] [00_start_here/06_peer_language_support.md](../datomic_pro_docs/00_start_here/06_peer_language_support.md)

### 01_tutorials

- [ ] [01_tutorials/00_peer_mem_db_getting_started.md](../datomic_pro_docs/01_tutorials/00_peer_mem_db_getting_started.md)
- [ ] [01_tutorials/01_peer_tutorial.md](../datomic_pro_docs/01_tutorials/01_peer_tutorial.md)
- [ ] [01_tutorials/02_run_a_transactor.md](../datomic_pro_docs/01_tutorials/02_run_a_transactor.md)
- [ ] [01_tutorials/03_connect_to_a_database.md](../datomic_pro_docs/01_tutorials/03_connect_to_a_database.md)
- [ ] [01_tutorials/04_transact_schema.md](../datomic_pro_docs/01_tutorials/04_transact_schema.md)
- [ ] [01_tutorials/05_transact_data.md](../datomic_pro_docs/01_tutorials/05_transact_data.md)
- [ ] [01_tutorials/06_query_the_data.md](../datomic_pro_docs/01_tutorials/06_query_the_data.md)
- [ ] [01_tutorials/07_see_historic_data.md](../datomic_pro_docs/01_tutorials/07_see_historic_data.md)

### 02_core_concepts

- [ ] [02_core_concepts/00_datomic_data_model.md](../datomic_pro_docs/02_core_concepts/00_datomic_data_model.md)
- [ ] [02_core_concepts/01_programming_with_data_and_edn.md](../datomic_pro_docs/02_core_concepts/01_programming_with_data_and_edn.md)
- [ ] [02_core_concepts/02_database_filters.md](../datomic_pro_docs/02_core_concepts/02_database_filters.md)
- [ ] [02_core_concepts/03_entities.md](../datomic_pro_docs/02_core_concepts/03_entities.md)
- [ ] [02_core_concepts/04_best_practices.md](../datomic_pro_docs/02_core_concepts/04_best_practices.md)
- [ ] [02_core_concepts/05_glossary.md](../datomic_pro_docs/02_core_concepts/05_glossary.md)

### 03_schema

- [ ] [03_schema/00_schema_data_reference.md](../datomic_pro_docs/03_schema/00_schema_data_reference.md)
- [ ] [03_schema/01_changing_schema.md](../datomic_pro_docs/03_schema/01_changing_schema.md)
- [ ] [03_schema/02_data_modeling.md](../datomic_pro_docs/03_schema/02_data_modeling.md)
- [ ] [03_schema/03_identity_and_uniqueness.md](../datomic_pro_docs/03_schema/03_identity_and_uniqueness.md)

### 04_transactions

- [ ] [04_transactions/00_transactions.md](../datomic_pro_docs/04_transactions/00_transactions.md)
- [ ] [04_transactions/01_transaction_model.md](../datomic_pro_docs/04_transactions/01_transaction_model.md)
- [ ] [04_transactions/02_transaction_data.md](../datomic_pro_docs/04_transactions/02_transaction_data.md)
- [ ] [04_transactions/03_processing_transactions.md](../datomic_pro_docs/04_transactions/03_processing_transactions.md)
- [ ] [04_transactions/04_transaction_functions.md](../datomic_pro_docs/04_transactions/04_transaction_functions.md)
- [ ] [04_transactions/05_acid.md](../datomic_pro_docs/04_transactions/05_acid.md)
- [ ] [04_transactions/06_client_synchronization.md](../datomic_pro_docs/04_transactions/06_client_synchronization.md)
- [ ] [04_transactions/07_partitions.md](../datomic_pro_docs/04_transactions/07_partitions.md)
- [ ] [04_transactions/08_transaction_hints.md](../datomic_pro_docs/04_transactions/08_transaction_hints.md)

### 05_query_and_pull

- [ ] [05_query_and_pull/00_query.md](../datomic_pro_docs/05_query_and_pull/00_query.md)
- [ ] [05_query_and_pull/01_executing_queries.md](../datomic_pro_docs/05_query_and_pull/01_executing_queries.md)
- [ ] [05_query_and_pull/02_query_reference.md](../datomic_pro_docs/05_query_and_pull/02_query_reference.md)
- [ ] [05_query_and_pull/03_pull.md](../datomic_pro_docs/05_query_and_pull/03_pull.md)

### 06_indexes

- [ ] [06_indexes/00_indexes.md](../datomic_pro_docs/06_indexes/00_indexes.md)
- [ ] [06_indexes/01_index_model.md](../datomic_pro_docs/06_indexes/01_index_model.md)
- [ ] [06_indexes/02_background_indexing.md](../datomic_pro_docs/06_indexes/02_background_indexing.md)
- [ ] [06_indexes/03_index_pull.md](../datomic_pro_docs/06_indexes/03_index_pull.md)
- [ ] [06_indexes/04_index_apis.md](../datomic_pro_docs/06_indexes/04_index_apis.md)
- [ ] [06_indexes/05_rseek_datoms.md](../datomic_pro_docs/06_indexes/05_rseek_datoms.md)

### 07_peer_api

- [ ] [07_peer_api/00_clojure/00_datomic_api.md](../datomic_pro_docs/07_peer_api/00_clojure/00_datomic_api.md)
- [ ] [07_peer_api/01_java/00_package_datomic.md](../datomic_pro_docs/07_peer_api/01_java/00_package_datomic.md)
- [ ] [07_peer_api/01_java/01_attribute.md](../datomic_pro_docs/07_peer_api/01_java/01_attribute.md)
- [ ] [07_peer_api/01_java/02_connection.md](../datomic_pro_docs/07_peer_api/01_java/02_connection.md)
- [ ] [07_peer_api/01_java/03_database.md](../datomic_pro_docs/07_peer_api/01_java/03_database.md)
- [ ] [07_peer_api/01_java/04_database_predicate.md](../datomic_pro_docs/07_peer_api/01_java/04_database_predicate.md)
- [ ] [07_peer_api/01_java/05_datom.md](../datomic_pro_docs/07_peer_api/01_java/05_datom.md)
- [ ] [07_peer_api/01_java/06_entity.md](../datomic_pro_docs/07_peer_api/01_java/06_entity.md)
- [ ] [07_peer_api/01_java/07_listenable_future.md](../datomic_pro_docs/07_peer_api/01_java/07_listenable_future.md)
- [ ] [07_peer_api/01_java/08_log.md](../datomic_pro_docs/07_peer_api/01_java/08_log.md)
- [ ] [07_peer_api/01_java/09_peer.md](../datomic_pro_docs/07_peer_api/01_java/09_peer.md)
- [ ] [07_peer_api/01_java/10_query_request.md](../datomic_pro_docs/07_peer_api/01_java/10_query_request.md)
- [ ] [07_peer_api/01_java/11_util.md](../datomic_pro_docs/07_peer_api/01_java/11_util.md)
- [ ] [07_peer_api/02_shared_reference/00_log_api.md](../datomic_pro_docs/07_peer_api/02_shared_reference/00_log_api.md)
- [ ] [07_peer_api/02_shared_reference/01_error_handling.md](../datomic_pro_docs/07_peer_api/02_shared_reference/01_error_handling.md)
- [ ] [07_peer_api/02_shared_reference/02_io_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/02_io_stats.md)
- [ ] [07_peer_api/02_shared_reference/03_query_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/03_query_stats.md)
- [ ] [07_peer_api/02_shared_reference/04_transaction_stats.md](../datomic_pro_docs/07_peer_api/02_shared_reference/04_transaction_stats.md)

### 08_operations

- [ ] [08_operations/00_architecture_and_storage/00_storage_services.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/00_storage_services.md)
- [ ] [08_operations/00_architecture_and_storage/01_transactor_reference.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/01_transactor_reference.md)
- [ ] [08_operations/00_architecture_and_storage/02_datomic_deployment.md](../datomic_pro_docs/08_operations/00_architecture_and_storage/02_datomic_deployment.md)
- [ ] [08_operations/01_capacity_and_reliability/00_capacity_planning.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/00_capacity_planning.md)
- [ ] [08_operations/01_capacity_and_reliability/01_high_availability.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/01_high_availability.md)
- [ ] [08_operations/01_capacity_and_reliability/02_backup_and_restore.md](../datomic_pro_docs/08_operations/01_capacity_and_reliability/02_backup_and_restore.md)
- [ ] [08_operations/02_observability_and_tuning/00_monitoring_and_performance.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/00_monitoring_and_performance.md)
- [ ] [08_operations/02_observability_and_tuning/01_memory_and_caching.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/01_memory_and_caching.md)
- [ ] [08_operations/02_observability_and_tuning/02_configuring_logging.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/02_configuring_logging.md)
- [ ] [08_operations/02_observability_and_tuning/03_system_properties.md](../datomic_pro_docs/08_operations/02_observability_and_tuning/03_system_properties.md)

### 09_optional

- [ ] [09_optional/00_pro_client/00_accessing_the_client_library.md](../datomic_pro_docs/09_optional/00_pro_client/00_accessing_the_client_library.md)
- [ ] [09_optional/00_pro_client/01_peer_server.md](../datomic_pro_docs/09_optional/00_pro_client/01_peer_server.md)
- [ ] [09_optional/00_pro_client/02_client_getting_started.md](../datomic_pro_docs/09_optional/00_pro_client/02_client_getting_started.md)
- [ ] [09_optional/00_pro_client/03_client_library_reference.md](../datomic_pro_docs/09_optional/00_pro_client/03_client_library_reference.md)
- [ ] [09_optional/00_pro_client/04_client_api_sync.md](../datomic_pro_docs/09_optional/00_pro_client/04_client_api_sync.md)
- [ ] [09_optional/00_pro_client/05_client_api_async.md](../datomic_pro_docs/09_optional/00_pro_client/05_client_api_async.md)
- [ ] [09_optional/01_aws/00_running_on_aws.md](../datomic_pro_docs/09_optional/01_aws/00_running_on_aws.md)
- [ ] [09_optional/01_aws/01_aws_access_control.md](../datomic_pro_docs/09_optional/01_aws/01_aws_access_control.md)
- [ ] [09_optional/02_specialized_operations/00_read_only_connections.md](../datomic_pro_docs/09_optional/02_specialized_operations/00_read_only_connections.md)
- [ ] [09_optional/02_specialized_operations/01_valcache.md](../datomic_pro_docs/09_optional/02_specialized_operations/01_valcache.md)
- [ ] [09_optional/02_specialized_operations/02_excision.md](../datomic_pro_docs/09_optional/02_specialized_operations/02_excision.md)
- [ ] [09_optional/03_technical_notes/00_comparison_with_updating_transactions.md](../datomic_pro_docs/09_optional/03_technical_notes/00_comparison_with_updating_transactions.md)
- [ ] [09_optional/03_technical_notes/01_composing_transactions_by_example.md](../datomic_pro_docs/09_optional/03_technical_notes/01_composing_transactions_by_example.md)
- [ ] [09_optional/03_technical_notes/02_hosting_a_private_maven_repository.md](../datomic_pro_docs/09_optional/03_technical_notes/02_hosting_a_private_maven_repository.md)
- [ ] [09_optional/03_technical_notes/03_querying_byte_array_attributes.md](../datomic_pro_docs/09_optional/03_technical_notes/03_querying_byte_array_attributes.md)
- [ ] [09_optional/03_technical_notes/04_outer_joins.md](../datomic_pro_docs/09_optional/03_technical_notes/04_outer_joins.md)
- [ ] [09_optional/04_tools_and_support/00_datomic_pro_console.md](../datomic_pro_docs/09_optional/04_tools_and_support/00_datomic_pro_console.md)
- [ ] [09_optional/04_tools_and_support/01_writing_a_problem_report.md](../datomic_pro_docs/09_optional/04_tools_and_support/01_writing_a_problem_report.md)

### Supporting files

- [ ] `00_start_here/_images/00_introduction/topology-abstract.png` — inspect in parent-document context or reconcile inventory.
- [ ] `01_tutorials/_images/01_peer_tutorial/datomic-logo-documentation-horizontal.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `02_core_concepts/_images/03_entities/entities-basics.png` — inspect in parent-document context or reconcile inventory.
- [ ] `02_core_concepts/_images/03_entities/entities-time.png` — inspect in parent-document context or reconcile inventory.
- [ ] `05_query_and_pull/_images/00_query/datomic-logo-documentation-horizontal.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/01_index_model/clientarch-orig.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms1-attr-only.png` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms2-attr-value.png` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms3-tuple-value.png` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms4-value-not-in-index.png` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms5-partial-tuple.png` — inspect in parent-document context or reconcile inventory.
- [ ] `06_indexes/_images/05_rseek_datoms/rseek-datoms6-multiple-entities-same-value.png` — inspect in parent-document context or reconcile inventory.
- [ ] `08_operations/01_capacity_and_reliability/_images/00_capacity_planning/transactor-memory.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `08_operations/02_observability_and_tuning/_images/00_monitoring_and_performance/monitoring.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/00_pro_client/_images/01_peer_server/clientarch-client.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/02_specialized_operations/_images/01_valcache/valcache.svg` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-dataset.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-datasources.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-entities.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-indexes.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db-input.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-db1-input.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query-with-tuple-input.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-query.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-schema.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-specify-db.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-transactions.png` — inspect in parent-document context or reconcile inventory.
- [ ] `09_optional/04_tools_and_support/_images/00_datomic_pro_console/console-window.png` — inspect in parent-document context or reconcile inventory.
- [ ] `datomic_pro_docs_manifest.csv` — inspect in parent-document context or reconcile inventory.

## Findings

Pending substantive review. EDN requirements belong to [Goal1](../goal-1/0-plan.md).
