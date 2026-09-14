# ATOMIC-NOTE: fixed reads, backup reads and local caches

[Read Only Connections](00_read_only_connections.md), “Approach,” “Liveness”
and “Capabilities,” maps to
[peer/connect-uri, connect-to-storage, connect-to-backup,
StorageOnlyConnection](../../../1.0.7705/peer/src-clj/datomic/peer.clj).
Storage-only construction loads one database/log basis and never installs a live
notification subscription. The documented dev-storage exception is about an
embedded source provider, not a requirement to start a native transaction writer.

Native [PeerSnapshot](../../../src/peer/snapshot.rs) captures a read-only durable
basis; [backup snapshot](../../../src/backup/snapshot.rs) reads an explicit backup
point through repository object access. Both reuse database_value/query/Pull and
the log engine. Native names, repository format, credentials and lifecycle are
not Datomic URI compatibility. Checks are in
[backup_reads.rs](../../../tests/backup_reads.rs) and
[native_log.rs](../../../tests/native_log.rs); backup correctness and restore
authority are traced in the [backup companion](../../08_operations/01_capacity_and_reliability/02_backup_and_restore.atomic.md).
Held values do not confer reader pins: live storage is subject to explicit grace
and offline repositories require the caller to retain their files.

[Valcache](01_valcache.md), opening immutable-segment/restart paragraphs and
“Valcache vs. Memcached,” maps to
[domain/system-cache-olookup](../../../1.0.7705/peer/src-clj/datomic/domain.clj),
[cache/lookup-with-inflight-cache](../../../1.0.7705/peer/src-clj/datomic/cache.clj),
[valcache-direct/ValcacheDirect](../../../1.0.7705/peer/src-clj/datomic/valcache_direct.clj)
and `valcache/direct-get`, `direct-put`, `direct-init`. Pending puts are checked
before disk; the direct adapter rejects oversized cache admissions and exposes
read errors to the composing caller. These are optional copies of immutable
objects, not current-database authority.

Native [ssd_cache.rs](../../../src/ssd_cache.rs) instead validates bounded,
authenticated, generation-scoped entries and treats recoverable cache corruption
as a miss. It can stay hot across restart without claiming the source directory
layout, atime-based eviction, exact admission policy or Memcached protocol.
`strictatime`/`lazytime`, Java properties, AWS advice and vendor metric names are
source operational instructions, not native prerequisites. Existing checks:
[native_ssd_cache.rs](../../../tests/native_ssd_cache.rs),
[native_log_cache.rs](../../../tests/native_log_cache.rs) and
`ssd_cache::tests::ssd_cache_corruption_is_a_miss_and_invalid_admission_creates_no_entry`.
The [cache trace](../../08_operations/02_observability_and_tuning/01_memory_and_caching.atomic.md)
owns weighted limits, immutable payloads and the Caffeine-versus-native-LRU
distinction. No native cache is an indefinite storage-retention promise.
