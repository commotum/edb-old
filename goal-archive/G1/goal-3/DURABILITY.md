# Atomic Durability Contract v1

## Authority and recovered mapping

`datomic_pro_docs/04_transactions/05_acid.md` requires one total, gap-free transaction order, a single atomic durable publication, conditional root updates, and storage acknowledgment before success. In `1.0.7705`, `datomic.log/write-tail-descriptor` conditionally advances a revisioned log descriptor, while `datomic.kv-sql/KVSql.put` maps revision preconditions to guarded SQL updates and uniqueness failures.

Atomic retains those roles with PostgreSQL-specific structure:

| Recovered role | Native PostgreSQL role |
| --- | --- |
| Immutable log/tree value | Insert-only encoded transaction row |
| Tail/root descriptor | One `atomic_heads` row per database |
| Descriptor revision/etag | `basis_t` plus `tx_hash` predecessor identity |
| Conditional KV put | `UPDATE ... WHERE basis_t = expected AND tx_hash = previous` |
| Log seek/reload | Ordered transaction-chain recovery |

The single PostgreSQL transaction makes separately eventual immutable writes unnecessary: the transaction row, idempotency outcome, and head publication commit together. This is a deliberate PostgreSQL advantage, not a change to the visible atomicity model.

## Authoritative records

- A database catalog row fixes database identity, its canonical bootstrap schema bytes and digest, and creation metadata.
- An immutable transaction row fixes database id, basis t, predecessor digest, transaction-envelope bytes and digest, and the next entity id needed for deterministic continuation.
- One mutable head row names the only published `(basis_t, tx_hash)` for a database. Reachability ends at this head; no other row can make a transaction visible.
- An idempotency row binds `(database_id, request_key)` to a canonical request digest and, after the same SQL commit, the resulting basis and transaction digest.
- Checkpoints are optional derived records. The transaction chain remains authoritative and any checkpoint must be discardable and independently verified against a published chain position.

## Invariants

1. Genesis is basis zero with an all-zero predecessor digest.
2. A published transaction at `t` has exactly one published predecessor at `t-1`; its envelope declares both values.
3. The head advances by exactly one basis and only when its stored basis and digest equal the writer's db-before.
4. Transaction payload and schema-change order are canonical. Every durable blob carries a format kind, explicit version, bounded length, and SHA-256 checksum.
5. Immutable transaction and bootstrap bytes are never updated or deleted by the runtime role.
6. A request key is permanently bound to one request digest. A matching retry returns its committed outcome; a mismatched retry is a conflict.
7. A transaction is successful only after PostgreSQL confirms `COMMIT`. A connection failure during or after commit is `UnknownOutcome`, never an assumed rejection; retrying the same key resolves it.
8. Recovery trusts only the catalog, published head, and complete reachable chain. It verifies versions, digests, predecessor links, contiguous basis, transaction datoms, schema changes, next entity id, and Goal 2 invariants before returning a database value.
9. Rows not reachable from the published head are invisible and may only be diagnosed or reclaimed by later lifecycle work.

## Commit state machine

1. Begin a PostgreSQL transaction and lock the database head row.
2. Resolve an existing idempotency key before running the kernel. Matching committed input returns its recorded outcome; mismatched input fails.
3. Require the locked head to equal the caller's expected basis and predecessor digest.
4. Run the pure kernel against the exact recovered db-before and construct the canonical transaction envelope.
5. Insert the immutable transaction and idempotency outcome.
6. Advance the head with the expected-basis/digest guard.
7. Commit and only then return `Committed`. Before commit, any error is a known rejection/rollback. A commit-call transport failure is `UnknownOutcome` carrying the request key.

PostgreSQL row locking serializes writers for one database. The conditional head predicate is still mandatory: it states and checks the semantic CAS, protects against stale callers, and matches the recovered descriptor-revision design. Independent databases do not share a write lock.

Like the recovered transactor's current immutable database value, `PostgresStore` retains a process-local `(head hash, Database)` after create, recovery, or acknowledged commit. A locked head match makes the next transition constant-replay-cost; a new process or stale writer falls back to authoritative log recovery. This is only a verified write-side cache, not the peer cache or durable index architecture deferred to Goal 4.

## Recovery and failure policy

Recovery starts from the head rather than the greatest transaction row. It walks/retrieves the exact chain from genesis through the head, verifies each envelope, and applies committed material datoms plus schema changes to the native bootstrap database. Missing, corrupt, noncanonical, unsupported, reordered, or semantically invalid reachable data fails closed with a structured fault. No partially recovered database is returned.

The minimum fault matrix covers failure before transaction insert, after insert but before head update, after head update but before commit, and after commit but before response; duplicate matching and mismatched retries; concurrent expected-basis writers; missing/corrupt reachable rows; and unreachable rows. Real PostgreSQL, not a mock, is the acceptance authority.

## Deferred boundaries

Production persistent read-index trees and consolidation belong to Goal 4. Sandboxed persisted functions belong to Goal 6. Network queues, leader epochs/fencing, and transactor failover belong to Goal 7. Backup, garbage collection, and operator lifecycle belong to Goal 8. This goal stores no speculative structures for those stages.
