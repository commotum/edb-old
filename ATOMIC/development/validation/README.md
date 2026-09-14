# Validation and workload reproduction

This directory contains reproducible development checks, not user-facing release
certification. Current goal completion and verified runs belong in
[goal-0](../../goal-0/0-plan.md); do not copy historical pass counts into product
documentation.

Build process fixtures before testing them against a dedicated disposable
PostgreSQL database with an administrative fixture login:

```sh
cargo build --offline --bin atomic --examples
cargo test --offline --test documentation
cargo test --offline --test product_cli --test edn_cli --test admin_backup_cli -- --test-threads=1 --nocapture
cargo clippy --offline --all-targets -- -D warnings
cargo fmt --all -- --check
```

Set `ATOMIC_POSTGRES_URL` for live tests. Restricted-role witnesses additionally
require role-creation authority. Missing configuration can skip live work; an
unconfigured pass is not PostgreSQL evidence. Run broad `--all-targets` checks
when appropriate, with process examples rebuilt from the same source.

`product_cli` drives a separate application across transactor restart;
`edn_cli` exercises file/stdin schema, transaction, preview, query, Pull and exact
retry; `admin_backup_cli` covers offline reading and separately targeted restore.
Use [reader measurements](read_load.md) and [maintenance workloads](maintenance.md)
for complete-path measurements, not universal throughput claims.

The `postgres_restart_resilience` test requires its own disposable server and
`ATOMIC_ALLOW_DISPOSABLE_PG_CRASH=1`, `ATOMIC_RESTART_POSTGRES_URL`,
`ATOMIC_RESTART_POSTGRES_DATA`, `ATOMIC_RESTART_PG_CTL`,
`ATOMIC_RESTART_POSTGRES_LOG` and `ATOMIC_RESTART_POSTGRES_OPTIONS`.
Run it alone, never against a shared server. TLS tests require their separate
certificate/server fixtures. Do not infer either witness from a skipped run.

Record source/executable identity, configuration, full successful output and
excluded phases. SQL driver-call counters are not network round trips; cache
accounting is not RSS. Include startup, result consumption and cleanup when
reporting complete-path costs.

## Current first-release acceptance

The final integrated run first built the standalone process prerequisites with
`cargo build --offline --bin atomic --examples`, then ran
`cargo test --offline --all-targets -- --test-threads=1` with the central
PostgreSQL fixture configured. It exited 0 across 1,191 registered tests
(466 library, seven binary, and integration/example targets). An earlier
incomplete run exposed the missing standalone example prerequisite and is not
counted as acceptance. Configuration-dependent witnesses were run separately:

- Disposable PostgreSQL crash/restart: 1/1 passed in 32.20 s, including exact
  retry, rollback and held-value checks.
- PostgreSQL TLS: 1/1 passed in 2.67 s, using two TLS 1.3 listeners and checking
  untrusted-root and plaintext rejection.
- Final formatting, all-target compilation and diff checks passed; all three
  documentation/navigation/help checks passed. The preservation/ledger guard
  verified 493 source bodies, 99 comment-only files, 493 source dispositions,
  90 chapter dispositions and 686 local trace links.

The final `BLOCK_LIVE_COST` sample recorded 13.193 s startup through shutdown
and 1,868 SQL driver calls. Twenty-four writes took 0.500 s, or 4.599 s including
automatic indexing; three automatic jobs ran. Thirty-two subsequent warm reads
used zero SQL calls. This local debug sample used uncontrolled warm PostgreSQL/OS
caches: it is not a scalability benchmark, RSS bound or vendor-parity claim.

The central disposable fixture at `/tmp/atomic-release-pg.rRGLRj` was stopped
after acceptance. These results establish the scoped native cutover, not
deployment, JVM/vendor equivalence or exhaustive failure-schedule coverage.
