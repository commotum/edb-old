# Goal 4 index and peer contract

This contract fixes the boundary between Goal 3's authoritative PostgreSQL
transaction chain and Goal 4's disposable read acceleration. It follows the
recovered `datomic.db-io`, `datomic.index`, `datomic.btset`, `datomic.valcache`,
and peer `Connection`/`Database` split, translated to one PostgreSQL store.

## Truth and publication

- `atomic_heads` plus the hash-chained `atomic_transactions` rows are the only
  transaction truth. A peer never infers a committed transaction from an
  index, notification, cache entry, or builder result.
- A persistent index manifest names one database, positive basis, and exact
  transaction hash. PostgreSQL accepts it only when that authoritative
  transaction exists. Its segments and manifest are immutable and checksummed.
- All segments and their manifest are inserted in one PostgreSQL transaction.
  Until the manifest commits they are unreachable; a failed build changes no
  peer-visible state. Repeating a deterministic build is idempotent.
- The latest valid manifest at or below the observed head is the durable base.
  Contiguous transactions after it are the recent layer. A peer validates the
  base and every link in the tail before exposing their combined snapshot.
- Consolidation publishes a newer immutable base. It cannot mutate an older
  manifest or any `Database` already returned to a reader.

PostgreSQL's transactional insertion plus a unique `(database, basis)`
manifest replaces Datomic's mutable storage-root slot. This is an intentional
PostgreSQL-specific simplification: it keeps the same conditional publication
property without emulating a key/value root CAS.

## Artifacts and indexes

- A manifest records its format version, database/basis anchor, final schema,
  next entity id, schema-change history, and ordered content-addressed segment
  references.
- Segments are shallow, wide leaves containing thousands of complete datoms.
  Fixed deterministic boundaries make retries converge. Each current and
  history EAVT/AEVT/AVET/VAET sequence has independent segments.
- EAVT and AEVT contain all applicable datoms; AVET contains only attributes
  marked indexed or unique; VAET contains only reference-valued datoms. Within
  each index, E/A/V components ascend and transaction descends, exactly as in
  the kernel oracle and recovered implementation.
- Segment hashes cover the versioned canonical bytes. Bounds, count, ordering,
  membership, hash, manifest anchor, and reconstructed kernel invariants are
  checked before a base is trusted.
- If any reachable derived artifact is absent or corrupt, the peer discards
  that candidate and rebuilds from the authoritative log. Corruption of the
  transaction chain itself remains a fail-closed Goal 3 error.

The first native representation is immutable sorted leaf segments rather than
a byte-compatible Datomic branching tree. The logical four-tree boundary,
covering entries, wide leaves, content identity, and base/recent merge are
preserved; JVM object layout and key/value-store traversal machinery are not.

## Peer state and synchronization

- A peer connection owns an atomically replaceable `Arc<Database>` current
  value, the hash that anchors it, a durable-base basis, a bounded immutable
  segment cache, and transaction-report observations.
- Every returned `Arc<Database>` is an immutable value. Advancing the
  connection constructs and swaps in a successor; old handles remain exact.
- States are `Opening`, `Ready { base <= basis <= observed_head }`, and
  `Disconnected`. Opening chooses a verified base and applies only a gap-free,
  hash-linked recent tail. A ready peer may lag the PostgreSQL head.
- `sync(t)` succeeds only with a database whose basis is at least `t`; it
  repeatedly observes the authoritative head and applies contiguous log rows.
  If `t` is not yet committed it waits or returns the caller's timeout. It
  never regresses or exposes a partial transaction.
- PostgreSQL notifications may wake waiters but carry no state and are never
  required for correctness. Polling the authoritative head is sufficient.
- Transaction reports are derived from each verified durable envelope applied
  by the peer. Missing hints may delay observation but cannot omit a committed
  envelope during catch-up.

## Cache trust and concurrency

- Cache keys are segment SHA-256 identities, never mutable names. A hit is
  still tied to the manifest reference; decoded canonical bytes and hash are
  validated on first admission.
- Bounded eviction changes only I/O. Re-fetching the same hash must decode to
  the same immutable segment; a conflicting payload is corruption.
- Readers need only cloned `Arc` handles and immutable indexes. Synchronization
  and cache bookkeeping may lock their own small state but never mutate a
  reader's snapshot or require a global read lock.

## `:db/noHistory`

The authoritative transaction log remains complete. Consolidation may forget
superseded history for an attribute only after that attribute is marked
`noHistory`; it must retain the current assertion set so reconstruction still
produces the same current database. Turning the flag off retains subsequent
history but does not promise to recover already-forgotten durable-index facts.
The first implementation carries prior consolidated history forward and
filters at consolidation, matching the recovered `filter-nohist-pairs` timing
model instead of rebuilding forgotten history from the full log. Because the
documentation deliberately gives no precise removal time, delayed
consolidation is observable only as extra history and is permitted.

## Failure outcomes

- Authoritative gap, predecessor/hash mismatch, bad canonical transaction, or
  head mismatch: fail closed as corruption.
- Bad/missing derived segment or manifest: reject that base, recover from an
  older valid base or genesis, and permit a deterministic rebuild.
- Interrupted/concurrent builder: no manifest, or the same immutable manifest;
  peers continue from the previous base.
- Stale peer: serves its named immutable basis and advances on demand.
- Cache miss/eviction or missed notification: additional PostgreSQL work only;
  no semantic change.
