# Run a separate native application

The supported `atomic` executable owns the transactor process. The
`application_workflow` example is an ordinary separate client: it receives a
logical database and local endpoint or authenticated remote discovery, submits application schema/data through
public APIs, and never provisions PostgreSQL or starts a writer.

Build both programs:

```sh
cargo build --offline --bin atomic --example application_workflow
```

Use a dedicated PostgreSQL installation and distinct administrative, writer and
peer credentials. `ATOMIC_POSTGRES_URL` contains the connection parameters;
`ATOMIC_POSTGRES_TRANSPORT` must explicitly be `tls` or `plaintext`. TLS verifies
certificates and hostnames and optionally accepts one PEM trust root through
`ATOMIC_POSTGRES_TLS_ROOT`. Plaintext is for an explicitly chosen local/development
deployment. The example and CLI share `postgres_config_from_env()`.

Optional `ATOMIC_CONNECT_TIMEOUT_MS`, `ATOMIC_STATEMENT_TIMEOUT_MS` and
`ATOMIC_LOCK_TIMEOUT_MS` select positive per-connection/statement/lock bounds.
Omitting them retains the existing policy. These are not a single end-to-end
deadline; the example separately uses a 20-second transaction/query wait budget.
See `atomic --help` and [transport policy](operations.md#transport-and-failure-policy)
for the other existing I/O settings and their scope. Diagnostics omit connection
strings, subject values and detailed transaction errors.

## Explicit setup and runtime

Create dedicated PostgreSQL LOGIN roles through your normal administration,
then run the following with the object-owning administrative connection in
`ATOMIC_POSTGRES_URL`. Roles must satisfy the
[runtime credential requirements](operations.md#provisioning-and-current-storage).

```sh
export ATOMIC_POSTGRES_TRANSPORT=plaintext
target/debug/atomic install --writer-role atomic_writer --peer-role atomic_peer
target/debug/atomic create --database application-demo
target/debug/atomic status --database application-demo
```

Installation and runtime grants are separate committed actions. `INSTALLED`
confirms the two-table storage installation even if subsequent role validation
or grants fail. Correct the roles and rerun the same command. `migrate` remains
an installation alias, not an upgrade path. Runtime roles are trusted clients
of this shared storage, not per-database SQL authorization; grants are additive
and do not audit inherited access.

The sample reserves IDs 1000–1011 (including its named partition entity) and versioned request keys within
this dedicated logical database. Use a fresh logical database for the sample. Installation and creation are
explicit administrative actions; runtime processes do neither. Creation supplies
initial indexes. Consolidation is explicit administrative maintenance/recovery,
not a startup fallback for damaged read indexes.

Create a private endpoint directory and keep its path for the next terminal:

```sh
endpoint_dir=$(mktemp -d /tmp/atomic-application.XXXXXX)
```

With the writer connection in `ATOMIC_POSTGRES_URL`, run:

```sh
target/debug/atomic transactor --database application-demo --index-threshold-bytes 1 --endpoint "$endpoint_dir/transactor.sock"
```

The one-byte novelty threshold is an explicit small-fixture policy: it schedules
background indexing promptly enough for the example's bounded fulltext wait.
It is not the throughput-oriented default or a recommended production setting.
Wait for the `READY` line. In another terminal, set `ATOMIC_POSTGRES_URL` to the
peer connection, select the explicit transport policy, set `endpoint_dir` to
that same private directory, and run:

```sh
target/debug/examples/application_workflow --database application-demo --endpoint "$endpoint_dir/transactor.sock"
```

The local endpoint is for processes running as the same OS user. PostgreSQL
credentials remain distinct. Stop the transactor with Ctrl-C or SIGTERM and
restart it using the same database, writer configuration and endpoint path.
Rerun the application: its fixed request keys resolve the exact prior schema,
seed and update receipts. The second run reports `seed_replayed=true` and
`update_replayed=true`. It does not silently repeat changes under new keys.

## Remote applications

Remote transaction submission uses verified TLS (at least TLS1.2) and a shared
32-byte bearer token. PostgreSQL credentials still authorize each peer's direct
storage reads; possessing the transaction token does not grant storage access.
Use your normal certificate provisioning and private secret distribution. The
server needs `ATOMIC_REMOTE_TLS_CERT` (PEM certificate chain),
`ATOMIC_REMOTE_TLS_KEY` (PKCS8 PEM private key), and `ATOMIC_REMOTE_TOKEN_FILE`
(64 hexadecimal digits). Key/token files must be regular, owned by the process
user and inaccessible to group/other users, typically mode0600. Secrets are not
command-line arguments and are omitted from diagnostics.

With writer PostgreSQL credentials and those environment variables configured:

```sh
target/debug/atomic transactor --database application-demo --listen 0.0.0.0:7443 --advertise 192.0.2.10:7443 --tls-server-name atomic.example.com --index-threshold-bytes 1
```

Replace the example address/name with a reachable numeric unicast address and
the certificate's DNS name. The listen address may be wildcard; the advertised
address may not. The small indexing threshold has the same fixture-only purpose
as in the local example above. Do not supply `--endpoint` for a remote listener.
The writer publishes its endpoint through a guarded opaque reference, bound to
the database route/lineage, current fenced lease and a new listener instance. A peer does not accept a
stale endpoint merely because its address is reused.

Writer ownership is a Rust lease/epoch protocol over conditional references.
A successor claims a new epoch; every fresh publication checks the captured root,
lease and GC protection. A stale writer cannot commit merely because its work
started before takeover. Lease duration is not an application callback timeout;
use transaction and I/O controls separately. Receipt lookup can resolve an
already committed request without reassessing it or reviving writer authority.

In the application environment, set peer PostgreSQL credentials and the same
`ATOMIC_REMOTE_TOKEN_FILE`. System certificate roots are used; optionally set
`ATOMIC_REMOTE_TLS_ROOT` to a public PEM trust anchor. Hostname verification
cannot be disabled. Then run:

```sh
target/debug/examples/application_workflow --database application-demo --remote
```

The application keeps a `Connection::remote_writer(postgres, tls)` handle. It
captures the connection's stable database identity and caches a verified route;
renaming or reusing a public name cannot redirect it. When a cached endpoint is
unavailable or stale **before request transmission**, it rediscovers and tries
the successor once. Construction and `cached_endpoint()` do no I/O; `refresh`
explicitly discovers. Cloned handles share the route, without holding its lock
during network work.

An unknown transaction outcome is returned unchanged, not automatically replayed.
Retry explicitly with identical content and key, never a newly generated key.
The lower-level `Connection::discover_remote_writer`, `transact_remote`, and
`transact_remote_with_hints` remain supported. A confirmed commit remains confirmed
if opening its local report subsequently fails (`CommittedTransaction::report`).
Discovery consumes the submission time budget and tightens configured driver
limits to its remainder; this is not a preemptive bound on DNS, authentication,
SQL or report construction. Use the async facade on executor threads.

Transport admission, frame and timeout limits are configurable through
`RemoteTransportConfig` (the executable exposes its common transaction limits in
`--help`). Transport deadlines cover TLS handshake and response/acknowledgment;
PostgreSQL discovery and local report construction have their separate configured
I/O policies. Valid optional hints above the configured admission limit are
ignored; malformed framing is rejected. Hints never form part of request identity.

For transaction reactions and durable consumer checkpoints, see
[change consumers](change-consumers.md). For provisioning, backup/verification,
restore, inspection, GC and search repair, see [administration](admin.md).

## Persistent standby and health

Add `--mode auto` to each transactor's normal command to wait for authority and
take over when the active lease is released or expires. Each process needs its
own local socket or TCP listen address; do not bind two contenders to the same
address. Configure the same database, deployment defaults and verified TLS policy
on contenders. Indexing, excision admission, transaction defaults and transport
settings survive the handoff. The default `--mode active` retains fail-fast
startup when another writer owns authority.

`STANDBY` is not write readiness. Only `READY` means writer authority and its
configured transaction listener are active. `--standby-poll-ms` defaults to250;
SIGTERM interrupts the wait rather than sleeping out a long poll interval.
In-flight PostgreSQL startup/cleanup still obeys its synchronous I/O policy.
Once active, loss of service/listener availability exits unsuccessfully for the
supervisor to restart; automatic election does not conceal an unhealthy writer.
Keep the same stable configuration when supervising its restart.

Optionally add `--health-listen 127.0.0.1:8081` (numeric `IP:PORT`). This is a
plaintext, status-only listener, not a query or administration gateway. Restrict
it to a trusted probe network; use your ingress for TLS if externally exposed.
`GET /health` returns200 while starting, waiting or active; `GET /ready` returns200
only while active and503 otherwise. Both return small JSON with `live`, `ready`
and `state`; `HEAD` is supported. Availability is sampled by the lifecycle loop,
not a linearizable promise that the next transaction succeeds. No database calls,
names, credentials or transaction data are exposed by probes. Two workers, eight
queued sockets,1KiB request buffers and short cumulative read deadlines bound
listener resources; overload closes connections.

Run foreground processes under your OS/container supervisor: its process identity,
signal handling and restart policy replace hand-maintained PID files. The stock
binary intentionally serves one logical database per process, giving independent
failure, configuration and capacity boundaries. Applications can compose multiple
`Connection`s, or embed multiple `TransactionService`s when they deliberately want
shared process fate; they must own separate listeners and resource budgets.
This is native process composition, not a missing multi-database storage engine.

For Rust Future/Stream methods, bounded worker admission, exact read capture and
drop/cancellation semantics, see [async clients](async-client.md). The evolving
application runs a current-value query and a one-row-at-a-time old-value stream
on a single-thread Tokio runtime, reporting `ASYNC_READ_OK`; native database and
connection lifecycle remain outside its executor task.

## What the application demonstrates

`calculation(&DatabaseValue)` queries projects and follows each project's owner
reference through the lazy Entity API. It operates on one captured seed snapshot.
The same function runs against a complete in-memory database constructed with
`Database::new`, `with` and `database_value`, including schema and references.
The fixture uses the existing semantic engine; it is not a replacement store.

The example changes one project's hours, reruns the calculation on the retained
value, checks an as-of view and assertion/retraction history, and independently
reopens the native connection. Old total hours remain 8; the current total is 12.
Native values remain backed by PostgreSQL: a local `db()` capture is not an
offline-query guarantee. A speculative `with` result is also not a guarantee
that a later transaction will commit against a changed live database.

A lost response after attempted delivery is an unknown outcome: retry identical
content under the same request key. A confirmed commit whose local report fails
to open remains committed. The example exits on a failure; rerunning these same
requests performs exact reconciliation through their durable receipts.

The same executable now also prints `PLANNING_OK`. It compares two pure branches,
discards them without writes, then exercises a real intervening transaction.
The stale protected plan rejects; the application replans against the intervening
receipt and submits its logical intent (lookup refs and tempids), not speculative
allocated IDs or a datom diff. The committed ID demonstrably differs from the
first preview. A final normal transaction restores the example's current facts;
history still records the intervention, selected plan and restoration. All three
use fixed new request keys and reconcile on restart. The original v1 schema/seed/
update requests keep their exact meaning. This planning stage finishes at basis6.

`PARTITIONS_OK` installs a named tenant partition and transacts a tenant plus its
nested component, checking both allocations share that partition. UUIDv7 and
SQUUID values survive exact receipt/reference round-trips. Fixed request keys and
the fixture's unique tenant identity preserve the same UUID intent on restart;
a general application should persist its request in an outbox before submission.
This stage finishes at basis9. See [partitions.md](partitions.md).

`FULLTEXT_OK` installs a fulltext article attribute and two articles, then joins
native fulltext candidates against the structured public-status fact. Only the
public article remains. It demonstrates a locally encoded/decoded ABI9 query
program without granting the read-only application program-deployment authority.
The separate PostgreSQL test deploys a stored fulltext query and transaction
program and checks restart/retry. Index/search preparation publishes one coherent
descriptor. The sample synchronizes to the article basis before querying and
uses a finite deadline; it does not claim that an unsynchronized or older
captured value has caught up. This final stage finishes at basis11;
the original project calculations and fixed requests are unchanged.
See [fulltext.md](fulltext.md) for search grammar, limits and consistency caveats.

The baseline calculation/statistics still use the exact update receipt at basis3,
not a newer value returned by sync after a rerun. `PLANNING_OK` reports its own
additional time and foreground SQL after the baseline counters. Its generated
hints are generated locally; the sample does not transport them. The remote SDK
provides the separate versioned hint channel described below.

`APPLICATION_OK` reports the semantic checks. One explicit `QUERY_WARMUP`
calculation reports its own time and SQL before the unchanged 20-calculation
loop. The same eight-entry cache is used throughout. `QUERY_SQL` measures that
loop directly with a child `OperationContext`; this resident fixture asserts
zero SQL calls/errors/cell bytes after warmup, not an offline-database guarantee.
`BASELINE` separately reports `stats_scan_us` and the history-datom count, plus
elapsed time, Linux RSS, cursor-node counters and cache accounting. Statistics
scan history; their cost is outside the calculation loop.

`APPLICATION_SQL` includes foreground opening, receipt reads, queries, reopening
and statistics. `PROCESS_SQL` additionally includes independent background work,
with `PeerObservation` classified separately. These are snapshots before teardown,
not process-wide deltas or measurements of the separate transactor process.
Driver calls and returned binary cell bytes are not SQL-statement counts,
network round trips, disk I/O or total memory; cursor counters remain a narrower
node-read subset. No timing threshold determines success or proves scale.

`DIAGNOSTICS_OK` checks named per-operation index attribution without changing
the captured database value. See [observability](observability.md),
[query diagnostics](query-diagnostics.md), and
[transaction diagnostics](transaction-diagnostics.md) for optional typed/EDN
reports and their cost/interpretation. Normal API and CLI results remain unchanged
when detailed diagnostics are disabled.

For an attached writer, a caller's `OperationContext` follows submitted work
across the service queue. Its `phases` include transaction expansion, assessment,
encoding/object preparation, guarded publication and report construction;
these wall durations are separate from SQL-call elapsed time. Outer transaction
time is inclusive, and phase invocations are not transaction counts.
Retries and partial failures record only executed work;
unknown-outcome reconciliation can add measurements after the initial response.
Metric callbacks run only when the caller explicitly calls `publish()`.

## Captured values, references and query programs

An enabled transaction report queue follows every transaction observed while
the peer remains connected, including transactions from other writers. A live
peer can catch up across local excision rewrites without losing the original
before/after values, transaction datoms or tempids. Excision retains a small
generation handoff while a connected lagging observer still needs it; catchup
or observer drop permits later GC to reclaim that ownership. A held database
value or drained report keeps only its own immutable data, not an interest in
future transactions. Remove unused queues and drop unused peers/connections.

A same-route restore can import an excision generation whose original reports
never existed on that route. If a required handoff is absent, an enabled queue
returns `peer/report-history-unavailable` without advancing its value or
silently omitting reports. Disconnect and reopen against the restored state;
this is not reconstruction of erased reports from a sanitized backup.

`DatabaseValue::snapshot_key()` returns a cheap comparable/hashable committed
view key: database lineage, excision generation, commit identity and temporal/
history modifiers. Constructing or cloning it performs no SQL or fact scan.
Physical index roots and cache placement are deliberately absent. It identifies
a supported logical view, not arbitrary query results or the retained history
policy: queries, inputs, rules, extensions and `noHistory` retention still matter.
An as-of view on a newer basis is not an exact older database reopening.

`snapshot_reference()` adds a versioned encoded handoff containing the stable
database route and its exact retained index witness. Decode it with
`SnapshotReference::decode`, then use `Peer::reopen_snapshot`,
`Connection::reopen_snapshot`, or `reference.open(&config, entries, bytes)`.
Reopening authenticates current publication authority, lineage, generation,
canonical log/metadata coordinates and the authorized retained index; it does not advance the peer or recover the latest
database first. References are not credentials and do not hold retention pins.
If GC collected the witness, reopening fails instead of substituting another
tree. This matters especially when `noHistory` consolidation changes retained
historical exposure while preserving the logical commit key. Existing held values
retain their normal pin policy. New reopening of pre-excision references is
rejected, even if old artifacts remain; this does not erase already held copies.
Cache entry/byte arguments bound the node cache, not the memory or time needed
to replay an exact retained transaction tail. Opening a reference can perform
storage I/O and is not itself an untrusted-work admission budget.

The executable example can save its captured seed reference at basis2 while
running the normal local or remote workflow:

```sh
target/debug/examples/application_workflow --database application-demo --remote --reference-out /private/path/seed.reference
target/debug/examples/application_workflow --database application-demo --reference-in /private/path/seed.reference
```

Output uses a new private file and refuses overwrite. The second command needs
only authorized PostgreSQL access, not a running transactor or transaction token.
It verifies the original key and seed calculation even after the normal workflow
has advanced data and schema to basis11. The file neither grants authorization
nor preserves retention. GC or excision can make a later reopening unavailable;
the program fails explicitly instead of choosing the latest database.

Speculative/in-memory values and opaque predicate filters explicitly reject
portable committed keys/references rather than inventing equality for closures
or uncommitted branches. They remain usable ordinary database values. The common
application demonstrates pure branching and exact reference round-trip; the
[query sources/reuse guide](queries.md) explains native database/tuple/log joins;
the [persisted program API](programs.md) exposes richer native query authoring with
current canonical codecs and shared resource controls.

### Protected program deployment

An administrator can call
`PostgresOperator::deploy_program(&Program) -> ProgramDeployment`.
Use `deployment.hash()` when binding or invoking the program, and retain the
handle until the binding transaction is acknowledged. Deployment authenticates
and protects the complete fixed-dependency closure; dependencies must already
exist. Keep child deployment handles alive until parent deployment succeeds.

`release(self)` explicitly releases protection and may perform blocking I/O.
Dropping the handle queues cleanup on the bounded worker lane. Once a committed
database graph references the program, ordinary graph ownership retains it.
An unbound program can become collectable after the deployment handle is
released; uploading bytes alone is not durable application ownership. A
concurrent GC seal can reject deployment safely with a conflict; retry the
deployment without dropping protection for its dependencies.

The read-only application example does not need this administrative capability
for its local encode/decode demonstration.

## Advisory transaction hints

`DatabaseValue::with_forms_with_hints` runs ordinary pure speculation with the
supplied `SpeculationLimits` and an additional bounded `HintLimits` read trace.
It returns the unchanged kind of speculative report, optional `TransactionHints`
and trace statistics. Broad scans are counted, not expanded into unbounded hint
lists. Unsupported portable origins still speculate normally but return no hints.
Keep the original logical forms for the actual transaction; preview IDs/datoms
are not a safe replacement for that intent.

An attached in-process `TransactionClient` or `Connection` accepts
`submit_with_hints(request, hints, HintPrefetchOptions)` and returns the ordinary
ticket plus `HintExecution`. The canonical request hash does not include hints.
Stale, absent, unrelated or failed prefetch never changes transaction meaning.
The writer reads its own authenticated current value, not caller-supplied blocks.
`Connection::transact_remote_with_hints` carries hints in a separate versioned
channel, retaining the same canonical request. The application example currently
generates hints without transporting them; the remote transport tests exercise
valid, altered, stale and foreign hints against identical receipts.

Prefetch has its own driver, pin and cold-miss lanes and shares only bounded
authenticated RAM index cache. It does not use the SSD path. The default is one
worker per writer; `ServiceOptions::hint_prefetch` can disable it or select up to
eight. At most eight per process can remain active, including stalled workers
after service replacement. Saturation skips hints; authority never joins a
worker before acknowledgement. Cancellation after processing stops subsequent
reads but does not claim to interrupt synchronous driver calls or connection
startup. `HintExecution::snapshot()` can change after acknowledgement while
`active_workers` is nonzero. Waiting for measurements is optional, not commit work.

Client/server limits are independent and clamped. The read-byte budget is checked
between datoms and shared across the request's workers; each active lane may have
one already-admitted datom beyond the byte allowance. The largest delivered datom
is reported as `max_inflight_datom_bytes`. The datom-count allowance is shared and
does not multiply with workers. Native block/cache limits still apply independently.
Trace weights and delivered-datom bytes are not allocator/RSS bounds. Compare
hint generation, queue/processing/overlap latency and total SQL/cache effects on
your workload: an already warm transaction can pay more I/O and gain nothing.
See [maintenance controls](maintenance-controls.md) for concurrency, pacing and
measurement details.

## Optional local SSD cache and compressed blocks

Create a dedicated private cache directory (mode `0700`) and opt in with
`ATOMIC_SSD_CACHE_DIR`. Optional positive `ATOMIC_SSD_CACHE_ENTRIES` and
`ATOMIC_SSD_CACHE_BYTES` bound its shared directory; defaults are 4096 entries
and 1 GiB of physical file lengths, including framing, not filesystem allocation.
All processes sharing that directory must use the same limits. Without the
directory setting SSD caching is disabled. Unsafe paths or conflicting limits
fail opening clearly; runtime contention, corrupt entries or disk errors fall
back to PostgreSQL. The library equivalent is
`PostgresConnectionConfig::with_ssd_cache(SsdCacheConfig { directory, limits })`.
Concurrent initialization retries its directory lock for one second before
returning `cache/busy`. A crash before publishing the ownership record can leave
an unclaimed initialization file: opening then reports `cache/unsafe-config`
and preserves it. Inspect that directory or choose a new empty private root;
these uncertain files are not automatically deleted.
Malformed owned files at recognized `.block` names do not prevent reopening:
the cache reports corruption and `inventory_complete=false`, preserves those
files, and bypasses writes/purge while reads fall back to PostgreSQL. Reported
occupancy includes their physical sizes. Unknown files, unsafe permissions or
links still reject opening; malformed framing is not permission to delete them.

New peers still authorize and authenticate their root through PostgreSQL. Cache
names separate connection/trust configuration, database lineage, excision
generation and format; they are not authorization tokens. An SSD hit supplies
hash-authenticated canonical bytes under the captured reader's protection.
A new live capture still needs PostgreSQL authority; SSD reuse does not promise
disconnected startup or indefinitely available historical data. Fully resident
RAM reads avoid foreground SQL within the stated retention policy.

`Peer`/`Connection::ssd_cache_stats()` distinguishes hits, misses, corruption,
busy/error bypasses, eviction and physical/canonical byte counts. Files are
private but not encrypted. They may contain personal or sensitive data. Excision
does not automatically remove existing local copies, backups or RAM values.
`purge_ssd_generation(generation)` removes recognized entries of that generation
from this cache root, returning false on a busy/error path; it is not secure
erasure and does not purge other processes' differently configured namespaces.
Retire obsolete cache roots explicitly under your data-retention policy.

Opaque object storage chooses raw bytes or a versioned gzip envelope when the
latter is smaller. The single stored representation authenticates against the
canonical SHA-256; it is not an optional SQL projection beside a second raw copy.
Declared sizes, decompression output, complete input consumption and canonical
hash are checked. A corrupt authoritative object fails clearly; there is no
hidden raw fallback. The canonical per-object limit is 64 MiB, independent of
whether compression makes its physical representation small.

`node_block_read_stats()` separates canonical/physical node payload bytes,
compressed/raw reads and decode time. `PgBlockStore::object_read_stats()` gives
generic per-connection object counts. These measure transferred payloads, not
PostgreSQL disk allocation, WAL, network framing or whole-process memory.
Backup repositories contain canonical objects, independent of storage compression.

## CLI acceptance driver

After building both binaries, set `ATOMIC_POSTGRES_URL` to a disposable local
PostgreSQL administrative fixture and run:

```sh
cargo test --offline --test product_cli -- --test-threads=1 --nocapture
```

The driver creates and removes its own unique schema. If the account can create
roles, it also creates dedicated restricted writer/peer roles, grants through
the CLI, and removes those roles afterward. Otherwise it explicitly reports
that the restricted-role witness was unavailable. Two actual application
processes run across a graceful transactor restart at the same private endpoint.
The restricted peer is also rejected when used as a writer. The fixture owner can deliberately remove its own current index-descriptor
object, check that startup fails without rebuilding, run `atomic consolidate`,
and restart. This replaces only the damaged current read index; canonical
log, receipts and logical basis remain unchanged. It does not certify damaged
historical read roots.
This destructive fault is test-only; the fixture's schema is removed afterward.
Configuration/redaction checks run without PostgreSQL. The real workflow reports
a skip if its PostgreSQL URL is absent; that skip is not integration evidence.
Set `ATOMIC_APPLICATION_BIN` if the example executable is in a different location.

## Reproduce a generated differential check

The existing pure-kernel/PostgreSQL differential test accepts an additional
nonzero seed and a step count (multiples of 12, from 12 to 1200). Its default
seed, 72 operations and two orderly restarts remain unchanged. With the same
disposable PostgreSQL fixture configured, record and replay a smaller run:

```sh
trace_dir=$(mktemp -d /tmp/atomic-differential.XXXXXX)
ATOMIC_DIFFERENTIAL_SEED=42 ATOMIC_DIFFERENTIAL_STEPS=36 ATOMIC_DIFFERENTIAL_TRACE="$trace_dir/seed42.trace" cargo test --offline --test transactor_differential generated_production_transactions_match_the_pure_kernel -- --exact --nocapture
ATOMIC_DIFFERENTIAL_REPLAY="$trace_dir/seed42.trace" ATOMIC_DIFFERENTIAL_TRACE="$trace_dir/replay.trace" cargo test --offline --test transactor_differential generated_production_transactions_match_the_pure_kernel -- --exact --nocapture
```

Do not combine replay with seed/step overrides. Explicit output must be a new
absolute filename in an existing directory; it is created private and never
overwritten. With no output override, the test prints a retained private temporary
trace path. The trace is saved before the campaign, including when a check later
fails, and contains generated fixture choices rather than connection credentials.
These checks cover semantic acceptance/rejection and orderly restart against the
existing `Database::with` oracle, not arbitrary crash/network-fault simulation or
automatic failure minimization. A PostgreSQL-unset skip is not a generated run.

A separate seeded block-storage schedule exercises abandoned prepared indexes,
exact outcomes, immutable values and durable consumer restart:

```sh
ATOMIC_BLOCK_FAULT_SEED=42 cargo test --offline --test storage_fault_replay seeded_block_schedule_preserves_publication_receipts_and_consumer_checkpoints -- --exact --nocapture
```

This is a fixed-size seeded schedule, not a saved/reduced trace framework or a
process-crash simulation. The PostgreSQL environment must select a disposable
fixture. An unconfigured skip is not evidence of a live run.

## Measure complete transaction and read costs

Use [read-load](read-load.md) for independent readers and
[operations](operations.md#disposable-workload-checks) for capture/restore.
The focused `block_live_costs` fixture reports startup/capture, resident
advancement, automatic indexing and reused-value warm queries. The opt-in
`transaction_phases` target reports ordered transaction phase costs:

```sh
cargo test --offline --release --test block_live_costs -- --nocapture --test-threads=1
cargo test --offline --release --test transaction_phases -- --ignored --nocapture --test-threads=1
```

Run against disposable storage and retain configuration with results. Count
startup, preparation, publication, result consumption and cleanup when reporting
complete-path costs; show excluded phases explicitly. Foreground operation
counters differ from process-wide work, and neither captures PostgreSQL server
CPU. Cache/novelty budgets are accounted residency, not measured process RSS.
A warm query on a reused immutable value can perform zero SQL when its working
set is resident; that does not establish zero-I/O capture or unlimited scale.
No former relational-engine benchmark is evidence for the current engine.
