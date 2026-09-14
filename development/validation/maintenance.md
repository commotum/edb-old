# Disposable workload checks

Use [independent reader measurements](read_load.md) for the multi-process query
campaign. The source examples `scale_workflow` and `operations_workflow` exercise
transactions, retained values, writer replacement and portable operations on
explicit disposable installations. They are measurement drivers, not production
deployment commands or a performance promise.

```sh
cargo build --offline --release --example scale_workflow --example operations_workflow
export ATOMIC_POSTGRES_URL='host=/source/socket port=55432 user=atomic_test dbname=source_test'
export ATOMIC_RESTORE_POSTGRES_URL='host=/target/socket port=55434 user=atomic_test dbname=restore_test'

ATOMIC_SCALE_RECORDS=2048 ATOMIC_SCALE_BATCH=100 target/release/examples/scale_workflow
export ATOMIC_DATABASE_ID='DATABASE_NAME_FROM_SCALE_OUTPUT'
target/release/examples/operations_workflow
```

These two examples use explicit plaintext development connections. Select
separate source/target PostgreSQL catalogs or schemas; both need administrative
fixture credentials. The scale driver creates a unique logical database and
leaves its data retained with the writer stopped. The operations driver captures
that stopped source, repeats the copy, deep-verifies, restores, compares current/
history/query results, checks scale-request exact retry, deeply inspects the
target and checks the source stayed unchanged.

`ATOMIC_BACKUP_DIRECTORY` can select a fresh/private repository; when omitted
the driver creates a private temporary directory and prints its retained path.
After a completed capture/repeat, `ATOMIC_OPS_RESUME_MANIFEST` can select the exact
retained manifest to resume the remaining phases. Keep earlier output: resumed
time excludes the original copy and standalone verification. Restore copies and
authenticates objects without another semantic replay. Leave other `ATOMIC_OPS_PHASE` and
`ATOMIC_OPS_EXPECT_*` controls unset for the ordinary driver.

Each phase reports its own elapsed time, process CPU/RSS and observed SQL work.
Read the printed configuration and keep successful exits with the output.
Configured cache bytes are not total-memory limits, and a small run does not
prove data larger than caches. `ATOMIC_SCALE_REQUIRE_CACHE_EXCEEDED=1` explicitly
enables the scale driver's cache-exceeding check. No historical run's timings
describe this engine.

The drivers retain repositories and database contents; dispose of those exact
fixtures deliberately. They do not authorize restarting a shared PostgreSQL
server or collecting unrelated data. Use the administrative CLI's explicit
target/retention controls for GC and its resumable progress reporting.
