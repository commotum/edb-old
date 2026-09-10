# Goal 7 — Integrated repaired-product acceptance

## Objective and constraints

Establish Goal0's complete native Rust/PostgreSQL repaired product, with all R1–R12
working together. Status: **complete (2026-09-10)**. Goal0 is complete; no active child.
Goals1–6
have observable completion evidence; reopen their owning stage if integration
exposes a gap. No new feature campaign, recursive goals, JVM parity or other stores.

Use `datomic_pro_docs` as semantic authority and `1.0.7705` as architectural
evidence. Preserve immutable facts/views, canonical bytes/hashes, durable receipts,
exact retries and native designs. Distinguish configured checks from early returns,
logical counters from RSS and SQL calls from network round trips. Preserve all
existing task/user edits. Do not re-run an entire reverse-engineering inventory.

## Ordered work

### 1. Integrate the repairs and focused Rust hygiene

- **Outcome:** The combined code has permanent regressions and clear native
  responsibility boundaries without the new avoidable warning sites.
- **Focus:** Complete broad supported tests and touched-code formatting/Clippy;
  fix actual regressions in their owning stages. No cosmetic whole-tree rewrite.
- **Completion signal:** Supported tests finish, failures are repaired and rerun,
  formatting and all-target Clippy pass with an honest warning disposition.
- **Status:** Complete. Six bounded lint sites repaired; focused34/34,
  formatting/diff checks and all-target Clippy pass with24pre-existing warnings.
  The range variant owns one coherent box with an inline cursor, no per-datom
  boxing or span-transition allocation. Initial configured broad suite finished:
  954reported passes/21failures/6ignored/7filters,139target summaries; not acceptance.
  The opt-in protocol early return among reported passes was independently rerun.
  Final runtime implementation has complete supported coverage: the second broad
  run finished973passes/4failures across140targets, then all three affected targets
  pass in full after test-only isolation/resource corrections: library408/408,
  backup10/10, service-worker9/9. That establishes977ordinary test cases across
  the broad run and clean target reruns, not a claim of one zero-exit broad command.

### 2. Prove the application, durability and operations boundaries

- **Outcome:** Separate native applications/transactors, PostgreSQL and operator
  workflows preserve the repaired semantics through failure and recovery.
- **Focus:** Local/remote submission, TLS, restricted roles, history/snapshots,
  programs, concurrent indexing/fulltext, restart/exact retry, old-data migration,
  backup/restore and GC/excision. Reuse existing application/test support.
- **Completion signal:** Real configured checks execute without hidden skips;
  generated and saved traces replay. Genuine old-engine receipts and genesis
  witnesses survive explicit current migration; isolated crash recovery agrees
  with full retained/current/history fingerprints.
- **Status:** Complete. Adjacent20/20 real checks pass: native/background/search
  programs, schema, runtime roles, migration boundary, TLS and both genuine
  old-binary one-time upgrades. Ordinary final production/example build passes;
  dedicated crash/retry and generated/saved replays also pass (details below).
  Broad-suite failures are repaired and the entire affected targets pass. Current
  application/crash/old-receipt checks and final saved traces also pass.

### 3. Reconcile evidence and finish the original objective

- **Outcome:** Users have a repaired product with reproducible scoped costs and
  accurate documentation, not merely completed child scaffolds.
- **Focus:** Fold material results/decisions into this plan, owning children,
  Goal0 and the acceptance guide. Retain qualifications and historical baselines.
- **Completion signal:** R1–R12 coverage and integrated acceptance are established;
  no core gap remains hidden as scope. Mark Goal0 complete only then.
- **Status:** Complete. Final results, failed-run qualifications and remaining
  warning disposition are reconciled here, in Goal0 and `docs/acceptance.md`.

## Fixtures and continuation

Use low-debug/nonincremental builds; optimized final profile for measurements.
Rust1.90, PostgreSQL15.11, shared development Threadripper2950X host. Target3.5GiB
before broad compilation; do not recreate the historical63GiB build output.

- Main: `/tmp/atomic-repair-pg.vA037i/data`,127.0.0.1:55471, user/database
  `atomic_repair`, plaintext. For isolated network tests use Unix socket
  `/tmp/atomic-repair-pg.vA037i` in the connection string, not namespace-local TCP.
- Genuine pre-repair receipt: logicalDB`repair_old_receipt_c0bc499`, file
  `/tmp/atomic-repair-pg.vA037i/old-receipts.txt`, oldbinary`identity_upgrade_old`.
  Run `identity_upgrade` explicitly ignored, MODE=verify; never reseed/overwrite.
- TLS: `/tmp/atomic-repair-tls.LF7KCl/data`,port55472, same user/database;
  hostssl-only TCP, private root`/tmp/atomic-repair-tls.LF7KCl/server.crt`.
  Set `ATOMIC_POSTGRES_TLS_URL` with`sslmode=disable` and rootcertificate env;
  the configured API must override plaintext and prove actual TLS listeners.
- Dedicated crash: `/tmp/atomic-repair-crash.nkXIRU/data`,port55473, same user/db;
  config`/tmp/atomic-repair-crash.nkXIRU/acceptance.conf`, log`server.log` there.
  PostgreSQL binaries `/usr/local/MATLAB/R2025b/sys/postgresql/glnxa64/PostgreSQL/bin`.
  Never stop the shared main/TLS fixture for a crash test. All durability knobs on.
- Genuine pre-fulltext/pre-partition executables remain in
  `/tmp/atomic-fulltext-old.2AjI71/atomic` and `/tmp/atomic-pre-partition.MKkZlH/atomic`.
  Fresh isolated old-schema26 databases have been created for one-time upgrades;
  keep their results distinct from retries on already-upgraded fixtures.

Continuation: Goal7 and Goal0 are complete. Preserve the repaired product and
evidence. No required work remains; reopen an owning child only for a newly
reproduced regression, and do not add optional features without user direction.
Do not count the two one-time legacy tests as passes when their env is absent;
run them explicitly on the genuine fresh fixtures and report filtered cases.

Verified integration so far:

- Separate application/transactor workflow passes application/planning/partitions/
  fulltext markers. Dedicated PostgreSQL immediate crash with durability on:
  basis11→13; full current/history, acknowledged data, held/as-of and independent
  reopen fingerprints agree; exact marker receipt/log preserved. Restart515ms,
  replacement2576ms, tail1transaction/1range, zero eager materialization. Saved
  precrash referencebasis2 remains readable. Restart/durability targets1/1 each;
  genuine pre-repair receipt verification1/1 and file SHA256 unchanged.
- Final24-action seed42 storage schedule and its saved replay pass;48-step seed42
  durable/pure comparison and saved replay preserve36accepted/12rejected outcomes.
  Controlled reducer9→3 actions passes as an explicitly injected fixture, not a
  claim to discover a new production defect. Private evidence directories:
  `/tmp/atomic-stage7-replay.FJOiWi` and `/tmp/atomic-stage7-crash-app.1H07n1`.
- Actual opt-in PostgreSQL wire-protocol witness1/1 passes:66nodes yield
  132/2/3driver calls and396/6/9Sync+Ready cycles for per-node/batch/compressed
  upload respectively. These are not packets/TCP round trips. Migration fixtures
 5/5 pass in parallel with the500ms writer-table lock invariant unchanged.
- Broad fixture repairs isolate table faults/paused liveness witnesses, target
  the actual shared fulltext root, and observe blocked node writes without
  blocking startup reads. Admin2/2, background9/9, pending-inspection3/3 and
  fulltext lifecycle4/4 pass. Retention program ordering is a real production gap
  reopened in Goal6, distinct from fixture isolation and stale assertions.
- Program-reference upgrade8/8 passes14.16s against private schemas on the separate
  disposable55473server. The fixture now explicitly removes migration26–30 optional
  tables/functions/triggers before restoring schema23, rather than relabeling
  newer artifacts. Prior checksums, missing/corrupt dependency rollback and retained
  canonical data/references remain asserted. Operations GC15/15 passes20.78s and
  semantic GC2/2 passes4.23s with parallel cases; bounded test-only advisory-Busy
  retry leaves fault assertions and exact preview/application checks intact.
- Last broad command used release/no incremental/-j4, four harness threads,
  real Unix PostgreSQL in `stage7_final_acceptance_20260910`, verified TLS and
  protocol opt-in. It finished all140targets with973pass/4fail/6ignored/7filtered.
  Two synthetic/backup fixture interference repairs are test-only. Entire corrected
  library408/408 passes24.06s in fresh physical`stage7_lib_acceptance_20260910`;
  backup10/10 passes (152.83s including unrelated migration queue); unchanged
  service-worker9/9 passes1.31s on55473 after an80-connection shared-server resource
  failure. Original failures/interrupted runs remain in logs; no successful claim
  for them. Four-thread rerun preserves each test's internal concurrency.
- Library's protocol check actually runs; its sole ignored subprocess worker is
  invoked by the passing process-death parent. The broad run's other ignored cases
  are the explicitly verified old receipt and four optional comparative measurement
  campaigns, not hidden required feature gaps. Seven filtered destructive/old-data/
  generated cases have separate real-fixture evidence above.
- Post-fixture formatting/diff/all-target Clippy pass,24pre-existing warnings,
  no new warnings. Repaired manifest test and retained-receipt24retry/8reconnect
  witness pass: three held pin backends, one after dropping reports. An interrupted
  polluted-catalog run and an over-capacity default-thread run were not counted.
- Final runtime application on fresh logical`stage7_final_crash_application_sbbqod`
  passes separate CLI/application, immediate PostgreSQL crash basis11→13, exact
  current/history/held/as-of/reopen fingerprints, immutable referencebasis2 and
  original request replays. Restart1015ms/replacement2061ms, tail1tx/1range, zero
  eager materialization. Both transactors stopped. Actual optional peer restart
  branch1/1 passes3.65s. Original old-engine receipt verification1/1 passes96.04s
  including migration queue; SHA256 unchanged. Logs/commands in
  `/tmp/atomic-stage7-final-crash.Sbbqod`; no reseeding/receipt overwrites.
- Same-head admin recovery complete connect/consolidate/drop cost115739µs,
  92driver/87SQLcalls,44398result-cellbytes,11374knownpayloadreadbytes. Avoiding
  the964-datom/340-node rebuild does not mean zero bookkeeping SQL or constant
  cost at every database size.
- Final saved traces rerun after the integrated production fixes: storage24actions
  passes3.26s (8writes,3interrupted uploads,1peer reopen,3writer restarts/3exact
  retries,4consumer resumes/8events); differential48steps passes1.23s with
  36accepted/12rejected/1writer restart. No seed/step overrides; new traces compare
  byte-identical to untouched originals. Evidence `/tmp/atomic-stage7-final-replay.tZlFAs`.
  Final four-thread library408/408, formatting/diff and all-target Clippy pass;
  `stage7-lib-fresh-bounded-final.log`, `stage7-final-frozen-clippy.log`,
  `stage7-backup-fixture-repair.log`, `stage7-service-worker-final.log` supplement
  `stage7-final-broad.log`. The latter retains its original nonzero exit/failures.

- ActualPG optimized fulltext_native1/1, fulltext_background1/1,
  fulltext_schema4/4, query_fulltext8/8, postgres_runtime_roles1/1,
  postgres_migration_boundary2/2, postgres_tls2/2, partition_upgrade1/1:20/20,
  no skips/filtered/ignored. TLS1.3 observed on two LISTEN connections;
  untrusted certificate and plaintext rejected.
- Preserved old binaries seeded fresh schemas
  `stage6_legacy_fulltext_1789038531652` and`stage6_legacy_partitions_1789038531652`.
  Their4818/4588-byte genesis values/hashes survive explicit schema26→30 migration,
  vocabulary transactions, indexing/recovery and exact retry. OldCLI create did
  not publish a native base; normal explicit consolidation supplied that derived
  fixture prerequisite without editing canonical genesis. Both fixtures nowbasis1;
  do not rerun the one-time tests against them as if they were fresh.
- Fulltext oldbinary SHA256
  `f1a2eea82d437017e1431a5d5e9b12d0ca7fe6a0d1fc9fb8ffd4258f8c09aeb0`;
  prepartition`ae81325a36a131183df02724304027df67b3f9b57bfbc79334be7bb46e02ab6d`.
  Original pre-repair receipt file checksum
  `e625cb49f32df376c9cf1e5135582347ad502de21dd7ff713f52f210dd123c32`.
- Hygiene log`/tmp/atomic-repair-pg.vA037i/stage7-hygiene.log`; adjacent targetlogs
  `stage7-{target}.log`. Final ordinary production/example release build51.51s;
  final broad compile/run logs`stage7-final-build.log` and`stage7-broad.log` there.
