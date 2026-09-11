# Goal 1 — Opaque storage and coherent roots

## Objective

Execute Stage 1 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: establish the real
PostgreSQL immutable-object/revisioned-reference boundary and Rust root model that
the remaining cutover will use. This is one child, not another parent.

Use local `datomic_pro_docs/04_transactions/05_acid.md` for semantics and recovered
`1.0.7705/transactor/src-clj/datomic/{sql,kv_sql,kv_cluster,cluster,log,catalog}.clj`
for architecture. Preserve native APIs and current correctness; no old-format
support, other backend, JVM implementation or reset of unrelated databases.

## Internal stages

### 1. Reconcile ownership and define publication

Status: Complete.

Outcome: One coherent foundation for log/index/receipt roots, writer authority and
reader/build retention, not separate feature-specific SQL designs.

Focus: Reuse pure value/transaction/query/tree codecs; replace `postgres.rs`,
`tree_store.rs`, receipt, lifecycle and backup SQL ownership as their parent stages
transfer. The old baseline is temporary Stage 2–7 implementation overlap, not an
alternate supported engine. Do not remove behavior regressions just to pass tests.

Completion: The contracts below are implemented sufficiently to exercise opaque
storage and conditional publication on real PostgreSQL; unresolved engine work is
explicitly assigned to the remaining parent stages.

### 2. Implement primitives and Rust blocks

Status: Complete.

Outcome: Fresh installation, authenticated immutable object read/write, bounded
bulk/enumeration, revisioned references, CAS and guarded atomic operations.

Focus: Lossless opaque bytes, no overwrite under an existing hash, durable
tombstones to avoid ABA, explicit conflict/unknown outcome, transport safety and
ordinary generic SQL only. Root descriptors bind database identity, basis, writer
epoch and log/index/receipt/metadata pointers. Index adoption preserves newer log
state; all root transitions require the exact current reference revision.

Completion: Unit tests prove envelope/root validation and preservation; real
PostgreSQL tests prove install, conflict, races, corruption rejection and reopen.

### 3. Verify complete paths and return to parent

Status: Complete.

Outcome: An exercised foundation, honest cost evidence and a clear Stage 2 handoff.

Focus: Fresh isolated PostgreSQL schemas, actual execution (not skipped tests),
complete connect/write/publish/reopen/read timings and actual SQL-call/byte counters.
No arbitrary throughput threshold or repeated historical matrix.

Completion: Checks pass, architectural review finds only generic storage SQL,
results and next work are recorded here and in Goal 0. Then execute Goal 0 Stage 2;
this child is not the product finish line.

## Selected contracts and remaining ownership

- Objects are content-addressed SHA-256 bytes. Rust block envelopes authenticate
  outgoing links as well as payload. PostgreSQL does not decode them.
- References have monotonic revisions and nullable values; deletion retains the
  revision. CAS distinguishes conflict from success and uncertain acknowledgement.
- Writer takeover updates the same database descriptor/ref used for publication;
  a separate lease check cannot authorize a later unguarded commit.
- Captured `DatabaseValueRoot` omits receipt roots and writer epoch. Receipts can
  retain exact before/after values without a content-address cycle through the
  publication descriptor containing that very receipt.
- Guarded reference batches atomically check source/GC revisions and install pins
  or publish roots. Newly created or reused objects can be protected by a generic
  epoch stamp; payloads remain immutable. Destructive batches check authority.
- Rust's Stage 5 collector will mark roots plus before/after announcements from
  concurrent reference transitions, seal that announcement epoch, and sweep only
  older unprotected objects. All root transitions must use the engine barrier.
  Reader pins require explicit release until safe revocation exists; do not infer
  that a crashed/slow reader's data is collectible from elapsed time alone.
- Root-linked persistent log/index/receipt/catalog structures and selective reads
  belong to Stage 2; serialized actual transactions to Stage 3; background index
  adoption to Stage 4; complete GC/lifecycle to Stage 5; backup/admin to Stage 6.
  Those capabilities are not claimed implemented by these primitives.

## Continuation

Stage 1 complete. Its original handoff to Stage 2 has been executed; all seven
parent stages are now complete. The decisions and measurements here describe
the foundation at this stage's boundary. Later lifecycle/runtime work supersedes
the temporary limitations above; see Goal 0 and `docs/acceptance.md` for the final
current-product state. No Stage 1 work remains pending.

## Verified results

- `cargo check --lib --offline` and `cargo check --tests --offline` pass.
- `cargo test --release --offline --lib storage::`: 16 passed (14 new storage
  tests and two pre-existing allocation tests matched by the filter).
- Actual PostgreSQL 15.11 with fsync/synchronous_commit on:
  `--test block_storage --test block_storage_isolation`: 13 passed, none skipped;
  each owns and drops a unique schema. Installation refuses an existing catalog.
- New installed schema: two opaque tables, zero routines/triggers. Reference
  guards use short Read Committed transactions, deterministic advisory locks for
  absent keys, row locks and conditional replacement. Tombstones prevent ABA.
  Object protection is monotone and guarded deletion rejects displaced collectors.
- The first PG run exposed a numeric/bigint parameter mismatch in bounded bulk
  reads; corrected and rerun successfully. A deterministic regression also proves
  guarded publication is safe with a Repeatable Read connection default.
- Complete connect → 16 object puts → root CAS → close → reopen → root/bulk read:
  512 B / 64 KiB / 1 MiB payload took 26.430 / 36.931 / 47.957 ms respectively,
  each with 2 connects and 45 measured SQL calls. Result-cell bytes were
  1,813 / 131,861 / 2,097,941. Local single-run primitive measurements only;
  no throughput, large-database or end-to-end product scalability claim.
