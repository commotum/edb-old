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
[restricted runtime requirements](operations.md#provision-and-upgrade).

```sh
export ATOMIC_POSTGRES_TRANSPORT=plaintext
target/debug/atomic migrate --writer-role atomic_writer --peer-role atomic_peer
target/debug/atomic create --database application-demo
target/debug/atomic status --database application-demo
```

Migration and runtime grants are separate committed actions. `MIGRATED` confirms
the schema migration even if subsequent role validation or grants fail; a failed
grant does not roll migration back. Correct the roles and rerun the grant command.

The sample reserves IDs 1000–1011 (including its named partition entity) and versioned request keys within
this dedicated logical database. Use a fresh database for the sample. Migration
and creation are explicit administrative actions; runtime processes do not
migrate. Consolidation is an explicit administrative recovery operation when a
diagnosed missing native publication requires it, not normal startup work.

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
The writer publishes its endpoint through PostgreSQL, bound to database lineage,
the current fenced lease and a new listener instance. A peer does not accept a
stale endpoint merely because its address is reused.

In the application environment, set peer PostgreSQL credentials and the same
`ATOMIC_REMOTE_TOKEN_FILE`. System certificate roots are used; optionally set
`ATOMIC_REMOTE_TLS_ROOT` to a public PEM trust anchor. Hostname verification
cannot be disabled. Then run:

```sh
target/debug/examples/application_workflow --database application-demo --remote
```

The application discovers the current writer before each explicit request.
After writer replacement, restart the application with the same request keys;
it rediscovers and resolves exact prior outcomes. This is not automatic replay
of arbitrary application effects. An unknown transaction outcome must be retried
with identical content and key, never a newly generated key. The SDK exposes
`Connection::discover_remote_writer`, `transact_remote`, and
`transact_remote_with_hints`. A confirmed commit remains confirmed if opening
its local report subsequently fails.

Transport admission, frame and timeout limits are configurable through
`RemoteTransportConfig` (the executable exposes its common transaction limits in
`--help`). Transport deadlines cover TLS handshake and response/acknowledgment;
PostgreSQL discovery and local report construction have their separate configured
I/O policies. Valid optional hints above the configured admission limit are
ignored; malformed framing is rejected. Hints never form part of request identity.

For transaction reactions and durable consumer checkpoints, see
[change consumers](change-consumers.md). For provisioning, backup/verification,
restore, inspection, GC and search repair, see [administration](admin.md).

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
program and checks restart/retry. The application waits for canonical indexing,
then retries only an unavailable search projection against the same captured
value, with a finite deadline. Search is eventual: canonical `sync_index` alone
does not establish fulltext readiness. This final stage finishes at basis11;
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

For an attached writer, a caller's `OperationContext` follows submitted work
across the service queue. Its `phases` include transaction expansion, assessment,
encoding/commitment preparation, publication/COMMIT and report construction;
these wall durations are separate from SQL-call elapsed time. Outer transaction
time is inclusive, commit has two disjoint spans, and phase invocations are not
transaction counts. Retries and partial failures record only executed work;
unknown-outcome reconciliation can add measurements after the initial response.
Metric callbacks run only when the caller explicitly calls `publish()`.

## Captured values, references and query programs

`DatabaseValue::snapshot_key()` returns a cheap comparable/hashable committed
view key: database lineage, excision generation, commit identity and temporal/
history modifiers. Constructing or cloning it performs no SQL or fact scan.
Physical index roots and cache placement are deliberately absent. It identifies
a supported logical view, not arbitrary query results or the retained history
policy: queries, inputs, rules, extensions and `noHistory` retention still matter.
An as-of view on a newer basis is not an exact older database reopening.

`snapshot_reference()` adds a versioned encoded handoff containing the logical
database route and its exact retained manifest witness. Decode it with
`SnapshotReference::decode`, then use `Peer::reopen_snapshot`,
`Connection::reopen_snapshot`, or `reference.open(&config, entries, bytes)`.
Reopening authenticates PostgreSQL authority, lineage, generation, commit and
required tree/log data; it does not advance the peer or recover the latest
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
versioned durable compatibility and shared resource controls.

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
authenticated RAM index cache. It does not use the SSD path. At most one worker
per writer and eight per process can remain active, including stalled workers
after service replacement. Saturation skips hints; authority never joins a
worker before acknowledgement. Cancellation after processing stops subsequent
reads but does not claim to interrupt synchronous driver calls or connection
startup. `HintExecution::snapshot()` can change after acknowledgement while
`active_workers` is nonzero. Waiting for measurements is optional, not commit work.

Client/server limits are independent and clamped. The read-byte budget is checked
between datoms; one delivered datom may exceed the remaining allowance, reported
as `max_inflight_datom_bytes`. Native block/cache limits still apply independently.
Trace weights and delivered-datom bytes are not allocator/RSS bounds. Compare
hint generation, queue/processing/overlap latency and total SQL/cache effects on
your workload: an already warm transaction can pay more I/O and gain nothing.

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
hash-authenticated canonical bytes, but a cold RAM miss first checks retention
pins. Therefore SSD reuse reduces payload reads, not all SQL, and does not promise
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

New native node uploads also create a compressed read representation when it is
smaller. Versioned, bounded decompression authenticates the unchanged canonical
hash. Existing canonical node rows and their SQL integrity checks remain
authoritative; missing/corrupt optional representations fall back to raw nodes.
The SQL read checks canonical raw length/hash even on a compressed hit, so a
projection cannot mask canonical corruption. This saves suitable transferred
payloads and SSD bytes, **not PostgreSQL disk space**: the projection adds storage
and compression/server-hashing work. `node_block_read_stats()` reports this
distinction. Backups remain canonical and restore recreates optional projections.

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
The restricted peer is also rejected when used as a writer. With a superuser
fixture, the driver deliberately removes only its own derived index publication,
checks that startup reports `service/native-index-required` without rebuilding,
runs `atomic consolidate`, and restarts without changing the authoritative head.
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

The storage campaign extends the same pure/native oracle with generated writes,
interrupted uploads, index publication and peer reopen. Run with a disposable
PostgreSQL URL and save/replay a private synthetic trace:

```sh
ATOMIC_STORAGE_FAULT_SEED=0xbeef ATOMIC_STORAGE_FAULT_STEPS=18 cargo test --offline --test storage_fault_replay generated_storage_fault_schedule_replays_real_postgres_and_exact_values -- --exact --nocapture
ATOMIC_STORAGE_FAULT_REPLAY=/absolute/path/printed.trace cargo test --offline --test storage_fault_replay generated_storage_fault_schedule_replays_real_postgres_and_exact_values -- --exact --nocapture
cargo test --offline --test storage_fault_replay controlled_storage_fault_fixture_is_automatically_reduced_on_real_postgres -- --exact --nocapture
```

`ATOMIC_STORAGE_FAULT_TRACE` and `ATOMIC_STORAGE_FAULT_REDUCED_TRACE` optionally
select new absolute output files. No overwrite; traces contain fixture actions,
not connection strings. The interruption is the existing `AfterSegments` hook,
after upload and before root publication—not a process-kill simulation. A real
failure is reduced while preserving its failure signature; the separate
controlled fixture proves reduction without claiming a production defect.

## Measure upload and transaction costs

`PostgresIndexer` supports `with_node_upload_limits`,
`with_compressed_node_blocks` and `with_node_block_encoding_overlap`. Default
uploads are bounded to128 nodes/8MiB; a larger valid node travels alone within
the existing codec bound. Canonical insertion and verification precede root
publication. Batches of at least64KiB can use one scoped codec worker overlapping
canonical SQL; it joins on both success and failure. `None` disables overlap,
and compression can be disabled independently. These controls also exist on
`PostgresTreeStore`; they do not change durable meaning.

To compare canonical-only, serial compression and overlap, build the release
upload witness, then run the printed test executable as a fresh process for each
case using `--ignored --exact postgres_upload_measurement_child --nocapture`.
`ATOMIC_UPLOAD_BENCH_MODE` accepts `canonical`, `serial`, `overlap`;
`ATOMIC_UPLOAD_BENCH_ENTROPY` accepts `repeat`, `random`;
`ATOMIC_UPLOAD_BENCH_SIZE` selects the per-value byte size. The fixture reports
driver calls, canonical/physical bytes, actual overlap, per-upload process CPU
and process peak RSS; input-byte bounds are not RSS estimates.

```sh
cargo test --offline --release --test node_upload_batch --no-run
cargo test --offline --release --test transaction_phases -- --ignored --nocapture --test-threads=1
```

The phase campaign compares sequential versus queued, dependent CAS/basis-checked
transactions on several working-set sizes. PostgreSQL server CPU is not measured;
Rust process CPU includes maintenance threads. One shared-host sample is not a
universal throughput claim. Current measurements retain the ordered transaction
path: expansion/report work is small, while larger encoding phases contain
substantial commitment-tree SQL. Profile that work before adding cross-transaction
pipeline stages. This decision is distinct from implemented node-codec/I/O overlap.
