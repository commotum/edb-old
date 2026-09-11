# Persistent native log cache

`PostgresConnectionConfig::with_ssd_cache` enables the same disposable local
cache for native tree nodes and transaction-log payloads. `Peer::log()` and its
`tx_range` / `tx_data` reads need no separate cache configuration. The directory
can reside on any supported local filesystem; SSD hardware is not required.

A restarted peer can reuse canonical transaction payloads without transferring
them from PostgreSQL again. It still establishes root/generation retention,
checks the database lineage, and reads authoritative transaction membership and
request metadata. The cached payload is hash-checked, decoded, and validated
against that metadata using the same authenticator as uncached recovery. The
captured endpoint must also match. Cache possession is not read authorization,
an offline database, or an independent source of truth.

The cache is namespaced by access configuration, lineage, physical excision
generation and log format. A new generation never reads the previous
generation's cached log. Already retained immutable log values may continue
reading their original generation. `Peer::purge_ssd_generation` removes both
recognized node and log entries for that generation; it does not erase held
values, backups, other cache directories, or filesystem remnants. Reading a
still-retained old value after purge may populate its cache again.

Corrupt, unavailable, disabled or capacity-rejected cache entries fall back to
PostgreSQL. The existing entry/physical-byte limits cover node and log entries
together. Transaction payloads larger than the cache's block codec allowance
remain readable but are not cached. No transaction encoding or durable receipt
changes are involved.

Log traversal still holds at most one codec-bounded transaction at a time,
apart from the captured snapshot and caller-retained results. The cache codec
may simultaneously hold encoded and decoded buffers for that transaction;
cache limits are residency limits, not process RSS guarantees. Cold reads add
a payload-fetch statement after their metadata lookup. Warm reads reduce
payload transfer, not all SQL or authentication work.

`LogCursorStats::payload_bytes_read` counts successful canonical payload bytes
from either tier. `postgres_payload_bytes_read` isolates payload bytes fetched
from PostgreSQL, while `cache_hits` counts successfully authenticated cached
transactions. Metadata bytes/calls remain visible in `OperationContext` SQL
statistics. `Peer::ssd_cache_stats` reports shared persistent-cache physical
I/O, admission, corruption and residency. One transaction may be too large to
cache even though its ordinary log read is valid.

The focused `native_log_cache` PostgreSQL test uses 12 noHistory updates with
the RAM node cache disabled and a 128-entry / 8 MiB persistent allowance. One
local run read 1,132,927 canonical payload bytes: cold SQL returned 1,136,343
field bytes, versus 3,416 metadata bytes and zero PostgreSQL payload bytes for
both warm and reopened scans. Cold scan time was 39.8 ms, warm 17.4 ms, and
reopen plus scan/checks 72.6 ms. Complete setup, reads, validation, result drop
and disposable fixture cleanup took 1.91 s. These are host-specific observations,
not a throughput or hardware claim; PostgreSQL and OS caches were not flushed.
