# ATOMIC-NOTE: native scope and reference versions

This companion classifies [Introduction](00_introduction.md), the setup/library/
language-support pages and the release-history pages in this chapter. It is
development navigation, not a native installation guide.

The Introduction's information-model, history, index and local-query paragraphs
map to `db/Datum`, `Db`, `index/Index`, `query/q*` and `peer/Connection.db` in the
recovered peer. Native [model](../../src/model/mod.rs),
[database_value](../../src/database_value/mod.rs),
[index](../../src/index/mod.rs) and [query](../../src/query/mod.rs) implement these
shared responsibilities. Detailed paragraph/check locators are in the
[identity](../03_schema/03_identity_and_uniqueness.atomic.md),
[filter](../02_core_concepts/02_database_filters.atomic.md),
[index](../06_indexes/01_index_model.atomic.md),
[query](../05_query_and_pull/02_query_reference.atomic.md) and
[ACID](../04_transactions/05_acid.atomic.md) traces. “Never removes” describes
ordinary transactions, not a waiver of `noHistory`, excision or physical garbage
collection; those boundaries have their own traces.

The topology and API-comparison paragraphs require native peer-local query/Pull
and a separate ordered writer, not a port of every edition. Native
[application composition](../../src/application/connection.rs) and
[deployment trace](../08_operations/00_architecture_and_storage/02_datomic_deployment.atomic.md)
make that boundary explicit. No arbitrary read-scaling, performance parity or
unbounded-memory claim follows from immutable values.

The setup, Maven/Leiningen/classpath, Java/Clojure language, REST and third-party
library paragraphs are host/distribution guidance. Source `Peer` and `api` are
facades over shared mechanisms, as the [API trace](../07_peer_api/00_clojure/00_datomic_api.atomic.md)
shows. Atomic exposes Rust and EDN, not a JVM library, Datomic REST wire protocol,
Cloud/Ions product or community client compatibility layer. Current native
guidance begins at [docs](../../docs/00_start_here/00_introduction.md).

The Pro releases, change log and release notices are version chronology, not a
feature checklist for native Rust. The recovered source is specifically
1.0.7705; the reference includes other editions, later Java documentation and
historical operational notices. A mismatch is recorded where consequential;
neither newer reference prose nor old release migration recipes silently
override observed source. Licensing/distribution, vendor service status and
historical performance assertions are not reverified here.
