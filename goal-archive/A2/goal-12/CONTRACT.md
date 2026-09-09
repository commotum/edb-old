# Goal 12 Transactor Contract

This is the Stage 1 contract for the one logical transaction writer. It is
normative for Goals 12–16 unless later source evidence changes it. Documentation
defines observable semantics; recovered `1.0.7705` control flow is the default
implementation blueprint. PostgreSQL fencing and retry mechanics are identified
separately so that native strengthening is not presented as a Datomic promise.

## Normative behavior

### Submission and serialization

1. An ordinary request is declarative transaction data submitted through a
   client already bound to one database. It does not contain a required observed
   basis or caller clock. A compare-basis condition is an explicit native
   extension, not the normal submission form.
2. One active writer per database dequeues requests and assesses each against
   the actual current immutable `db-before`. The resulting transition is the
   same pure `Database::with` computation used speculatively; only publication
   is effectful.
3. Transaction functions in one information set all see that same `db-before`.
   Their emitted data is combined as unordered transaction data; one function
   cannot observe another function's emitted data.
4. The expected-head comparison remains inside publication. A client must not
   race another client merely because both formed ordinary requests from the
   same earlier observation.

**Documentation:**
`datomic_pro_docs/04_transactions/03_processing_transactions.md:9-17`;
`04_transactions/01_transaction_model.md:21-40,44-50`;
`04_transactions/04_transaction_functions.md:13-25`.

**Recovered blueprint:**
`1.0.7705/transactor/src-clj/datomic/transaction.clj:353-355`
(`create-procargs` carries id, data, and options, but no basis);
`transactor/src-clj/datomic/update.clj:1100-1179,1214-1314`
(`process-transaction` swaps the current `db-ref`; `processor` is the serial
database-local loop); `peer/src-clj/datomic/db.clj:7534-7550,7892-7925`
(recursive expansion against one db and the complete pure report);
`transactor/src-clj/datomic/cluster.clj:155-167` and
`transactor/src-clj/datomic/log.clj:547-583` (revision/etag CAS and log claim are
internal ownership/publication mechanics).

### Transaction time

1. The transactor captures time after selecting and locking the actual
   `db-before`. In the ordinary case it supplies the current transaction's
   `:db/txInstant`.
2. An explicit import override may be expressed once for the current
   transaction. It must be no earlier than the basis transaction instant and
   no later than the transactor's captured clock. Conflicting option/data
   overrides and multiple values reject the whole transaction.
3. `Database::with` stays clock-explicit and deterministic. PostgreSQL supplies
   the authoritative clock only in the durable transactor path.

**Documentation:**
`datomic_pro_docs/04_transactions/02_transaction_data.md:355-378`.

**Recovered blueprint:**
`1.0.7705/peer/src-clj/datomic/db.clj:6831-6890,7451-7489`
(`has-tx-inst?`, `next-valid-inst`, clock capture, validation, and insertion).

### Decision, acknowledgement, retry, and reports

1. Assessment failure is a known rejection and publishes nothing. Publication
   atomically records the immutable log transaction, its request decision, and
   the new head. There is no partially visible transaction.
2. Success is returned only after PostgreSQL has committed. A known durable
   commit must never be converted into an ordinary error by report construction,
   cache work, notification, or post-commit recovery.
3. Loss of the connection or timeout across the commit decision is an unknown
   outcome. Retrying the same durable request key and same declarative request
   reconciles to the original decision; reusing the key for different data is a
   conflict and cannot create a second commit.
4. The successful report is the report assessed before publication:
   `db-before`, `db-after`, `tx-data`, and `tempids`. These values are carried
   through the decision path, not reconstructed by replay after commit.
5. Attached report consumers observe newly committed transactions in commit
   order. An idempotent replay is not a new transaction and emits no new report.
   Report delivery may not hold up the serial assessment/publication loop.
6. Pre-admission overload is a known `Busy` result. Once admitted, a local wait
   timeout is unknown unless the durable request decision has been read.

**Documentation:**
`datomic_pro_docs/04_transactions/05_acid.md:12-17,33-47`;
`04_transactions/03_processing_transactions.md:19-38`;
`08_operations/00_architecture_and_storage/02_datomic_deployment.md:47-61`.

**Recovered blueprint:**
`1.0.7705/transactor/src-clj/datomic/update.clj:1100-1199`
(assessment result and one `:logged` promise);
`:1356-1408` (encode the already-assessed result);
`:1454-1523` (encode the same result for notification);
`:1656-1717` (deliver `:logged` only after append);
`:1784-1839` (wait for that durable boundary before notification);
`lifecycle.clj:36-208` (establish/renew ownership before serving).

### Controlled behavior

1. A transaction function is a pure `[db-before, args] -> tx-data`
   transformation. It may make nontrivial conditional decisions, read local
   database information, reject, and emit structured ordinary transaction
   forms, including bounded recursive function expansion.
2. Attribute predicates validate asserted values. Required attributes and
   entity predicates validate the complete proposed `db-after`; a false or
   invalid result rejects before publication.
3. Persisted program content has immutable hash identity, while the selected
   database function is ordinary temporal information: an entity's current
   `:db/fn` value in `db-before`. A rebinding transaction still invokes the old
   binding and only later database values observe the replacement. The
   submitted ident call remains the stable retry identity; the assessed
   binding/hash is an execution witness rather than an out-of-band activation.
4. The native runtime is deterministic and database-local: no ambient network,
   filesystem, process, clock, or randomness. Fuel, stack, iteration, output,
   recursion, and panic boundaries reject safely without bypassing
   `Database::with`.

**Documentation:**
`datomic_pro_docs/04_transactions/04_transaction_functions.md:13-47,68-76,127-159,208-233`;
`04_transactions/01_transaction_model.md:42-50`;
`04_transactions/05_acid.md:18-31`.

**Recovered blueprint:**
`1.0.7705/peer/src-clj/datomic/db.clj:5017-5034`
(`Db.getFn` resolves an ident/entity from the database value and reads
`:db/fn`);
`1.0.7705/peer/src-clj/datomic/db.clj:7534-7550`
(function lookup/invocation and recursive injection);
`:7703-7769,7782-7879` (attribute/entity checks over proposed state);
`:7892-7925` (one final report pipeline);
`1.0.7705/transactor/src-clj/datomic/function.clj:56-99,145-161,173-249`
(stored representation, lazy compilation, arity, and language boundary).

### Ownership and recovery

1. Leadership/fencing is bound to the database being served. A lease or epoch
   for database A cannot authorize publication to database B. Losing or
   expiring ownership stops admission/publication before a standby serves.
2. Startup selects the newest independently verified immutable base whose
   database identity, genesis, basis, transaction hash, and reachable segments
   bind to the authoritative log, then applies only the contiguous log tail.
3. A missing or corrupt derived base falls back to an older verified base or
   genesis. It never repairs or replaces authoritative history. Base-plus-tail
   and genesis replay must yield the same database value, program activation,
   and next request decision.
4. The active transactor retains the resulting current value and advances it
   with each assessed commit. Normal commits and warm restart must not replay
   from genesis.

**Documentation:**
`datomic_pro_docs/00_start_here/00_introduction.md:79-109`;
`06_indexes/02_background_indexing.md:11-19`.

**Recovered blueprint:**
`1.0.7705/transactor/src-clj/datomic/lifecycle.clj:36-208`
(revisioned active/standby lifecycle);
`transactor/src-clj/datomic/update.clj:2793-2861`
(claim log, load index root, catch up, install `db-ref`);
`transactor/src-clj/datomic/log.clj:1406-1520`
(seek from `db.nextT` and reduce the tail only).

## Native strengthening and deliberate deviations

- **Durable request keys are Atomic-specific.** Datomic documents that a timed
  out peer may not know the outcome and recommends querying/annotation rather
  than blind retry. Recovered `create-procargs` has a UUID request id, but no
  cited Datomic contract promises durable idempotent replay by that id. Atomic
  deliberately persists request key, canonical declarative digest, and decision.
- **PostgreSQL leases and epochs are Atomic-specific.** They implement the
  recovered single-owner/log-claim shape directly in PostgreSQL and strengthen
  it by binding the capability to a database id. They are not a portable storage
  interface and are not claimed to reproduce Datomic's heartbeat protocol.
- **Optional compare-basis is an Atomic extension.** It is useful for explicit
  conditional publication but is never mandatory for ordinary submission;
  datom-level `:db/cas` remains the usual information-model primitive.
- **The program sandbox is a safety restriction and strengthening.** Datomic Pro
  permits arbitrary deployed Clojure/Java functions. Atomic intentionally does
  not reproduce ambient JVM authority, but must provide enough deterministic
  forms for the documented db-before transformation and db-after validation
  roles. Straight-line arithmetic alone is not sufficient.
- **Bounded admission and resource metering are native operational policy.**
  They must preserve transaction semantics and honest outcome classification.

## Public mutation-route inventory

This is the Stage 1 ownership decision. “Internalize” means the capability may
remain below the service or in a test/operator harness; it must not remain a
second application publication API.

| Route at the Goal 12 audit boundary | Disposition |
| --- | --- |
| `Database::with` / `with_function_context` | Keep public and pure. They produce immutable speculative values and never publish. |
| `TransactionClient::submit` / `transact` | Keep as the sole ordinary application writer, bound by `TransactionServiceConfig.database_id`. |
| `TransactionService::start` / standby takeover | Keep as owner of the database-bound queue, recovered current state, lease renewal, decision, and reports. Runtime startup verifies migrations; it does not run DDL. |
| `Peer::transact` | Remove or make it delegate to a `TransactionClient`; the peer's storage connection must not publish directly. |
| `PostgresStore::transact`, `transact_with_fault` | Internalize. They are unfenced competing publication paths; fault injection belongs to a privileged test seam. |
| `PostgresStore::transact_fenced` and authoritative generated/CAS helpers | Internalize beneath the bound service. Preserve optional compare-basis and internal head CAS, not the old mandatory client basis/clock API. |
| `PostgresStore::transact_program*`, `transact_programs`, `transact_with_persisted_predicates` | Route invocation through the same service request/decision path, then internalize direct publication methods. |
| `acquire_lease`, `renew_lease`, `release_lease`, `TransactorLease` | Service/operator capability only; do not expose a way for application callers to pair a lease with raw publication. |
| `deploy_program_blob` | Keep as privileged immutable-content preparation. A blob is not callable merely because it exists; ordinary `:db/ident` + `:db/fn` transaction information selects it temporally. |
| `activate_program`, named deployment aliases | Remove from transaction semantics. `Db.getFn` resolves the function entity and its `:db/fn` in the immutable database value; a mutable SQL activation row is not a Datomic binding. |
| `create_database`, `migrate`, backup restore | Keep as explicit offline/administrative operations, never actions performed by ordinary service startup. Restore targets an offline/empty database identity. |
| `PostgresIndexer::consolidate*` | Keep as derived-index maintenance. Root adoption must not alter logical transaction history or authorize ordinary writes. |
| `PostgresOperator::collect_garbage` | Keep as derived physical maintenance behind safe pins/grace; not a logical database write. Goal 15 owns final policy. |
| `PostgresOperator::excise_database*` | Keep only as an exceptional privileged maintenance path coordinated against the database writer. It is intentionally outside ordinary accretion; Goal 15 owns the destructive fence/audit contract. |

## Stage 1 observed gaps and witnesses

The audit baseline made an ordinary request carry `database_id`, mandatory
`expected_basis_t`, and caller `tx_instant`, while the service lease used an
independent free-form scope. Public peer/store/program routes could bypass that
service, and service success reconstructed `db-before` after commit. Cold
transactor recovery replayed from genesis, and the persisted program ISA had no
branch or bounded iteration.

At the Stage 1 snapshot, focused witnesses expressed ordinary no-basis/no-clock
submission, serialized concurrent success, exact carried reports,
database-bound leases, bounded overrides, unknown/retry, and failover behavior.
They were not then evidence that later stages were complete. `0-plan.md` now
records the implemented closure and its verified boundaries; this section
remains the historical audit baseline rather than a current status claim.
