# Datomic Transactor dependency-source ownership

This read-only scan checked all **533** hash-bound shipped dependency JARs (192412 physical ZIP entries) for exact `.clj`/`.cljc` paths derived from all 162 Datomic Transactor initializer class paths.

## Result

- `missing`: 160
- `single-exact-path`: 2

## Owner JARs

- `lib/datomic.specs-0.1.3.jar`: 1 namespace source entries
- `lib/query-support-0.8.28.jar`: 1 namespace source entries

## Missing exact source paths

- `datomic.adopter`
- `datomic.aggregation`
- `datomic.api`
- `datomic.artemis_client`
- `datomic.artemis_server`
- `datomic.assert`
- `datomic.async`
- `datomic.aws`
- `datomic.aws_detect`
- `datomic.aws_monitor`
- `datomic.backup`
- `datomic.backup_cli`
- `datomic.btset`
- `datomic.builtins`
- `datomic.cache`
- `datomic.cache.caffeine`
- `datomic.cache.impl`
- `datomic.callback`
- `datomic.cassandra`
- `datomic.cassandra_v4`
- `datomic.cassandra_values`
- `datomic.cassandra_values_v4`
- `datomic.cast2slf4j`
- `datomic.cast2slf4j.peer_server`
- `datomic.catalog`
- `datomic.cleanup`
- `datomic.cli`
- `datomic.client_server.auth`
- `datomic.client_server.marshaling`
- `datomic.client_server.spi_support`
- `datomic.cloudformation`
- `datomic.cloudwatch`
- `datomic.cluster`
- `datomic.cluster_stack`
- `datomic.clusterfs`
- `datomic.codec`
- `datomic.combined_cluster`
- `datomic.common`
- `datomic.config`
- `datomic.config_ext`
- `datomic.connector`
- `datomic.coordination`
- `datomic.coordination_ext`
- `datomic.crypto`
- `datomic.data`
- `datomic.datafy`
- `datomic.datalog`
- `datomic.db`
- `datomic.db.specs`
- `datomic.db_io`
- `datomic.ddb`
- `datomic.ddb_cluster`
- `datomic.ddb_s3_cluster`
- `datomic.ddb_values`
- `datomic.domain`
- `datomic.ec2`
- `datomic.error`
- `datomic.excise`
- `datomic.extension_resolver`
- `datomic.extensions`
- `datomic.external_sort`
- `datomic.external_sort_datoms`
- `datomic.fressian`
- `datomic.fsbackup`
- `datomic.fulltext`
- `datomic.fulltext_index`
- `datomic.function`
- `datomic.future`
- `datomic.garbage`
- `datomic.garbage.fressian`
- `datomic.garbage.pod`
- `datomic.h2`
- `datomic.iam`
- `datomic.index`
- `datomic.indexer`
- `datomic.integrity`
- `datomic.io`
- `datomic.iter`
- `datomic.janino`
- `datomic.jar`
- `datomic.jetty`
- `datomic.kv_cache`
- `datomic.kv_cassandra`
- `datomic.kv_cassandra2`
- `datomic.kv_cassandra3`
- `datomic.kv_cluster`
- `datomic.kv_dynamo`
- `datomic.kv_dynamo_skv`
- `datomic.kv_hotrod`
- `datomic.kv_mem`
- `datomic.kv_sql`
- `datomic.kv_sql_ext`
- `datomic.kv_store`
- `datomic.launcher`
- `datomic.lifecycle`
- `datomic.lifecycle_ext`
- `datomic.log`
- `datomic.log.specs`
- `datomic.log_gc`
- `datomic.logrotate`
- `datomic.lucene`
- `datomic.math`
- `datomic.memcached`
- `datomic.memory`
- `datomic.memory_size`
- `datomic.monitor`
- `datomic.peer`
- `datomic.peer_client`
- `datomic.peer_server`
- `datomic.process`
- `datomic.process.events`
- `datomic.process_monitor`
- `datomic.promise`
- `datomic.provisioning.aws`
- `datomic.pull`
- `datomic.qtune`
- `datomic.query`
- `datomic.queue`
- `datomic.reconnector2`
- `datomic.require`
- `datomic.rest`
- `datomic.s3`
- `datomic.s3_api`
- `datomic.s3_kv`
- `datomic.s3backup`
- `datomic.simple_kv`
- `datomic.slf4j`
- `datomic.slf4j.bridge`
- `datomic.spec`
- `datomic.sql`
- `datomic.stats`
- `datomic.summary`
- `datomic.tools`
- `datomic.tools.detect_865`
- `datomic.tools.filter_index`
- `datomic.tools.gc_db`
- `datomic.tools.index_checks`
- `datomic.tools.index_stats`
- `datomic.tools.locate_transactor`
- `datomic.tools.log_tools`
- `datomic.tools.postdiag_865`
- `datomic.tools.read_segment`
- `datomic.tools.rebuild_index`
- `datomic.tools.rebuild_log_leaf`
- `datomic.tools.rebuild_log_root`
- `datomic.tools.repair_1455`
- `datomic.tools.repair_865`
- `datomic.tools.restore_log_root`
- `datomic.transaction`
- `datomic.transactor`
- `datomic.transactor_ext`
- `datomic.treewalk`
- `datomic.update`
- `datomic.uri`
- `datomic.val_cluster`
- `datomic.valcache`
- `datomic.valcache.puts_pool`
- `datomic.valcache.puts_pool_impl`
- `datomic.valcache_direct`
- `datomic.validators`

## Ambiguous exact paths

None.

## Interpretation boundary

A single exact path in the distribution establishes a concrete shipped source candidate and ownership/version lead. It does not alone prove that the embedded AOT class was compiled from byte-for-byte that source; AOT surface or recompilation evidence is a separate validation gate. Missing paths remain decompiler recovery inputs.
