# Goal 3 — Serialized block publication and exact outcomes

## Objective

Execute Stage 3 of `/home/jake/Developer/atomic/goal-0/0-plan.md`: make the actual
Rust transactor durably publish transactions on the opaque PostgreSQL store,
retain exact receipt-first retries and recover with fenced writer replacement.
Use `datomic_pro_docs/04_transactions/05_acid.md` and recovered `datomic.log` for
serialized conditional publication; do not copy its historical compatibility
machinery. Preserve the existing typed/EDN, program/native predicate and identity
semantics, canonical requests and configured resource controls.

## Internal stages

### 1. Exact outcomes and protected publication structures

Status: Complete — actual PostgreSQL receipt and publication tests passed.

Outcome: Engine-owned persistent receipts and current roots retain exact before,
after, datoms and tempids, independently of changing writer/index authority.

Focus: Selective persistent request lookup, canonical bounded receipt encoding,
shared reader capture and object-write protection. Writer authority, receipt and
log publication must join one guarded transition; no relational ledgers or hash
cycles. Reuse proven kernel/codecs, not a full-state serialization per request.

Completion: Receipt lookup/update costs follow touched paths; matching/conflicting
requests and malformed content have permanent tests on real opaque storage.

### 2. Real serialized transactions and application service

Status: Complete — actual queued service, standby and typed/EDN tests passed.

Outcome: The actual application submission path uses one serialized block writer.

Focus: Read an existing receipt before current-state preconditions, program
resolution or assessment. Reuse the selective immutable transaction pipeline;
validate programs/schema/predicates against exact bases. Durably stage objects,
CAS the coherent root, then acknowledge/notify. Reconnect existing bounded service
admission and preserve advisory hints, explicit defaults and diagnostics.

Completion: Typed/EDN applications transact and retry using the new store, including
identity/schema/program behavior, without old SQL tables or eager recovery fallback.

### 3. Recovery, failover and measured handoff

Status: Complete — actual failure/notification checks and optimized costs passed.

Outcome: Current-format recovery and ambiguous outcomes are correct and selective.

Focus: Concurrent/stale writers, writer death/replacement, absent versus committed
ambiguous requests, exact retained/reopened receipts and index-only root changes.
Measure complete small writes and recovery/retries on a meaningful indexed fixture.
Port useful regressions as old authoritative writer code is displaced.

Completion: Actual PostgreSQL/service checks and failure regressions pass; complete
costs and remaining Stage 4 live-index/lifecycle hooks are explicit. Resume Goal 0,
not another parent or child hierarchy. This stage is not the product finish line.

## Continuation

Stage 3 and the full seven-stage parent are complete. The following evidence
records this child's original boundary; later children completed the indexing,
peer and lifecycle work it handed off. Final integrated evidence and current
costs are in Goal 0 and `docs/acceptance.md`; no Stage 3 work remains pending.

The new engine stages log, exact receipt and value roots, then publishes the
database root and renewed writer token through one guarded reference batch.
Receipt lookup uses a persistent compressed binary request tree and precedes
current schema/program/default/capacity checks. Captured before/after value roots
exclude receipts to avoid content-hash cycles. Shared pure assessment retains the
existing typed/EDN/program/predicate semantics; explicit transaction timestamps
must be selected after expansion, not silently replaced by the local clock.

The existing TransactionService/standby now starts only the block writer. Its old
transaction worker and SQL admission lookup were removed. Local queue/tickets,
report subscribers, diagnostics and receipt-only unknown-outcome reconciliation
are retained. Stage 4 still owns live indexing, functional hint prefetch, peer/
remote routing and automatic tail consolidation; index requests currently report
that unfinished work explicitly. This is development overlap, not a finished app.

Review repaired create/name races and lease-revision recovery after lost final
acknowledgements. A collector revision conflict is distinct from lost writer
ownership. Runtime testing found and repaired two further integration defects:
service timeout hashing used the old UUID-v4-only helper, and creation mislabeled
the schema constructor's basis-1 transaction as genesis. Creation now retains
the schema log/basis/frontiers/instant; an empty initial request index is valid.
An all-index/history/log oracle regression prevents that relabeling from recurring.

Actual debug-profile PostgreSQL tests now pass: seven block transaction/creation/
program/indexed-fixture tests, two public service/standby tests, five deterministic
engine race/lost-ack tests, two protected-write/capture tests, and both ported
service diagnostic/notify-once tests. All use fresh isolated schemas, none skipped.
The wider `storage::` unit filter passed 28 total tests. Thirteen selected pure
duplicate-datom/clock/shared-assessment/program-binding tests passed too. Earlier
release receipt tests passed four cases, including 2,048 indexed outcomes and
64 MiB chunked tempids. Optimized writer/service tests also passed all nine cases.

Measured release samples on local PostgreSQL 15.11:

- 4,096 entities, 348 indexed objects / 1,568,846 bytes. With 64 / 256 KiB caches,
  cold complete append took 69 / 65 ms, 218 / 216 SQL calls, 77,193 / 43,892 read
  bytes and 1,667 / 1,817 written bytes. Release/reclaim/exact receipt reconstruction
  took 53 / 35 ms, 127 / 115 calls and 76,794 / 43,306 read bytes. Old exact
  values survive an index-only publication and reopened receipt lookup.
- 2,048 entities seeded in 179 ms; 100 small writes without consolidation took
  8.866 s, 23,312 SQL calls and 274,204 written bytes. First/second 50 writes took
  3.733 / 5.074 s. This exposes bounded-tail recapture cost, not constant-time
  resident writes; Stage 4 must reuse recent state and amortize indexing. Reopened
  old receipt took 79 ms. No unmeasured throughput/scalability claim.
- Three queued public-service writes plus one exact retry took 91.734 ms.
- Persistent receipt map at 64 / 512 / 2,048 outcomes needed at most 9 / 14 / 16
  immutable objects per insertion and 9 / 10 / 14 reads for selected lookup plus
  receipt load. The 64 MiB tempid case used 16 chunks and put/load took 1.195 s.

The original handoff to Stage 4 and Stage 5 automatic pin cleanup/complete
mark-publication lifecycle have been executed and verified. Fresh isolated
targets only; no compatibility matrix, unrelated database reset or reference-corpus
deletion was introduced.
