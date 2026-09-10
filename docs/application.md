# Run a separate native application

The supported `atomic` executable owns the transactor process. The
`application_workflow` example is an ordinary separate client: it receives a
logical database and local endpoint, submits application schema/data through
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

The sample reserves attribute IDs 1000–1003 and versioned request keys within
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
target/debug/atomic transactor --database application-demo --endpoint "$endpoint_dir/transactor.sock"
```

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

`APPLICATION_OK` reports the semantic checks. `BASELINE` records elapsed time,
20 repeated calculations, Linux RSS when available, connection cursor SQL reads
and bytes, and cache accounting. Those counters exclude other SQL such as
catalog, pin, lease, background-observer and outcome reads; they are neither
total process I/O nor operation-attributed measurements. This small workload
is a reusable baseline, not a throughput or scale claim. No timing threshold
determines success.

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
