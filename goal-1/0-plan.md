# Goal 1: Validate and Harden the Recovered Datomic Peer

## Objective

Turn the recovered Datomic peer source into a reproducibly packaged artifact, then build progressively stronger evidence for its correctness across external storage, recovery, concurrency, and failure behavior. Repair compiler warnings only when the original bytecode uniquely establishes the correction, and improve generated-source readability only after the stronger verification foundation is in place.

## Constraints

- The packaged artifact must be built from the recovered source and resources without using the original peer or core2 AOT classes as runtime or compilation fallbacks.
- The original licensed artifacts may be used only as explicit comparison oracles where authorized; validation must make that boundary observable.
- Preserve recovered behavior and public API/ABI compatibility throughout the work.
- Never guess a type hint, cast, primitive representation, or warning fix. Leave cases without decisive bytecode evidence documented and unchanged.
- Do not suppress warnings merely to make the warning count smaller.
- Run backend, recovery, concurrency, and fault tests only in disposable, authorized environments with no production data or services at risk.
- Keep builds and tests reproducible by recording material toolchain, dependency, configuration, seed, artifact, and result information.
- Treat readability as subordinate to behavioral fidelity.

## Known Context

- The recovered project is at `/home/jake/Developer/atomic/datomic-rev`.
- All 142 recovered Clojure namespaces have already passed isolated source-only compilation and loading without the original peer/core2 AOT classes.
- Existing checks cover artifact accounting, selected JVM surfaces, and a documented in-memory parity workload, but they do not establish correctness for every backend, recovery path, or concurrency schedule.
- The current warning inventory contains 157 exact records: 150 reflection warnings, 6 primitive-local `recur` warnings, and 1 auto-boxing warning. They are not load failures; each is retained in the checked Stage 4 inventory.
- “Complete” for this goal means satisfying the observable completion signals below, not claiming literal recovery of unpublished original source.

## Stage 1 — Reproducible Source-Built Artifact

**Status:** Complete (`2026-08-27`)

**Outcome:** A canonical, reproducible artifact is produced entirely from the recovered source and packaged resources and is suitable as the sole recovered implementation under test.

**Focus:** Establish the authoritative build boundary, pin material inputs, exclude original AOT fallbacks, package runtime resources and metadata correctly, and verify the resulting artifact with the existing source-load, API/ABI, and in-memory parity checks.

**Completion signal:** Two independent clean builds produce the same artifact hash; the build records its material inputs; inspection confirms that no original peer/core2 AOT classes satisfied the build; and the packaged artifact passes namespace loading, API/ABI validation, resource checks, and the documented in-memory parity workload.

**Verified evidence:** `datomic-rev/scripts/validate-stage-1.sh` completed the full
gate from two clean builds. Both produced the 205-entry thin source artifact
`datomic-rev-peer-1.0.7277-source.jar` with SHA-256
`bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`.
The artifact embeds hashes for the recovered source, Java source, exact ten
resources, build tools, and the baseline-locked ordered set of 532 dependencies.
Peer/core2 are excluded by name and hash from build and candidate classpaths;
origin checks proved all recovered inputs came from the artifact and all 142
original AOT initializers were absent. Packaged validation passed 142 namespace
loads, 142 runtime/API surfaces, exact 47-class/108-field/279-method handwritten
Java surfaces, all focused regressions, the 150/6/1 warning inventory,
and in-memory parity SHA-256
`228b03dac4a437465937c53dbc7c4917294de408b2311b41a662e5f39eca37be`.
Detailed design, commands, evidence, and the pinned-runtime limitation are in
`datomic-rev/reports/stage-1-validation.md`.

The final Stage 1 rerun includes three original-bytecode-proven hardening
repairs exposed during external-storage work: captured-callable invocation in
`datomic.future/filling-promise`, the object-array result in
`datomic.memory-size`, and the `Callable` overload in
`datomic.common/pfuture`. The latter accounts for the earlier one-warning
reduction. It also includes the Stage 4 warning-site repairs and the separately
proven `datomic.common/compare-byte-arrays` correction; their evidence and
accounting are recorded under Stage 4 below.

## Stage 2 — External SQL and Recovery Validation

**Status:** Complete (`2026-08-27`)

**Outcome:** The source-built artifact has repeatable evidence of correct operation with an external SQL backend and across backup, restore, and supported failure-recovery paths.

**Focus:** Exercise normal storage lifecycle and recovery scenarios in disposable environments, check persisted state and invariants before and after recovery, and compare against documented expectations or the licensed reference implementation where an explicit oracle is needed.

**Completion signal:** A repeatable end-to-end matrix passes for SQL operation, backup, restore, and selected recovery scenarios using the source-built artifact, with data-integrity assertions and diagnostic records sufficient to reproduce any failure.

**Verified evidence:** `datomic-rev/scripts/stage2/validate-postgresql.sh`
completed one fresh PostgreSQL 16.15 run with the canonical artifact as the only
peer/core2 implementation in every candidate JVM. The licensed transactor was
a separately fingerprinted external fixture. The aggregate gate passed guarded
t1/t2/t3 SQL workloads; full and incremental backup/restore; exact logical,
datom, history, row, and identity comparisons; post-restore writes; failed root
publication without a false restore point; normal retry; exact one-segment
missing and unreadable verification; both restore rejections before any SQL
write; and a late interrupted restore followed by an incremental retry that
reused partial work. The final recovered t3 target matched logical SHA-256
`de1debf98a63e10fa775591c7a57d557d559e7af1c42a4d676f10882cb0ca684`
and remained writable. Its retained summary records `stage.complete=true`,
`fault-injection.included=true`, and `services.stopped=true`. Design,
The retained post-Stage-4 run is
`/tmp/datomic-stage2-stage4-final-v1`; its 118-file evidence manifest has
SHA-256 `78ba14d01454f90d650174c7f92f9fa1bf80244686d95faa6029770ba831dca5`.
Reproduction, hashes, and the test-only Storage-adapter boundary are in
`datomic-rev/reports/stage-2-validation.md`.

## Stage 3 — Concurrency and Fault Injection

**Status:** Complete (`2026-08-27`)

**Outcome:** The source-built artifact has meaningful resilience evidence for concurrent and rejected work rather than only successful sequential paths.

**Focus:** Cover connection lifecycle races, async/IOC paths, transaction contention, thread-pool rejection, cancellation, cleanup, and bounded fault scenarios with observable invariants and reproducible schedules or seeds where practical.

**Completion signal:** The repeatable concurrency and fault suite covers the named risk areas and completes without unexplained behavioral mismatches, deadlocks, leaked lifecycle state, silent data loss, or unbounded resource growth; failures retain enough evidence for deterministic investigation.

**Verified evidence:** `datomic-rev/scripts/stage3/validate-postgresql.sh`
completed the candidate-only fresh-JVM local and PostgreSQL matrix. Promise
listener/cancellation/cleanup, core2 async/IOC, saturated-pool rejection,
query timeout/cancellation, a synchronized connection-release race, and eight
rounds of eight-way SQL CAS contention all passed. Contention produced exactly
8 winners and 56 metadata-verified conflicts. A watchdog-protected fault run
verified the owned transactor entered stopped state under `SIGSTOP`, observed
exact unavailable, held the pause for at least 10 seconds, recovered after
`SIGCONT`, and committed/read exactly one sentinel. Fresh-JVM audits before and
after transport recovery were byte-identical with canonical SHA-256
`abe3a8e000587079b64965cb99d468ef355d14ef11bd0855aeefab4eba394510`.
The retained run records `stage.complete=true`, `services.stopped=true`, and
evidence manifest SHA-256
`f3033d28744e604e621dcbe9d0596a73f46c6ab8860d4db9ff84a878d9736bc8`.
The retained post-Stage-4 run is
`/tmp/datomic-stage3-stage4-final-v1`.
Design, exact boundaries, diagnostics, and reproduction are in
`datomic-rev/reports/stage-3-validation.md`.

## Stage 4 — Bytecode-Proven Warning Repairs

**Status:** Complete (`2026-08-27`)

**Outcome:** Compiler warnings are reduced only where a correction can be proven from the original bytecode, while every unresolved case remains explicit and unchanged.

**Focus:** Map each candidate warning to the relevant original descriptors, casts, invocation instructions, and primitive operations; make the smallest uniquely supported source correction; and re-run all applicable structural and behavioral checks after each coherent change set.

**Completion signal:** Every modified warning site has recorded bytecode evidence that uniquely determines the correction; no warning was guessed or suppressed; all prior validation remains green; and the warning inventory clearly distinguishes repaired, intentionally unresolved, and newly discovered cases.

**Verified evidence:** Stage 4 repaired 159 bytecode-proven source warning
sites across 11 namespaces, removing 160 warning records: 155 reflection, four
primitive-local `recur`, and one auto-boxing warning. The exact inventory fell
from 317 (`305/10/2`) to 157 (`150/6/1`). The evidence tables have SHA-256
`7183b407fc4d9edabaad8f8d29d463eea6fe692043e22dc0951db388ea8dc9bf`
and `e320e50bf67cff4198020cbf767db3fe6aca03c2c852889ce749811a5764bb22`.
The exact unresolved inventory has SHA-256
`9c8f20b030205f749edc952255da131bfa6d45c1537bbf0655bd6fe772ce33ad`.
The separate `datomic.common/compare-byte-arrays` defect was repaired and
regression-tested without counting it as a warning repair. Focused bytecode and
surface probes, all 142 source-only loads, the final Stage 1 two-build gate,
and fresh complete Stage 2 and Stage 3 PostgreSQL matrices all passed. Details
and claim boundaries are in `datomic-rev/reports/stage-4-validation.md`.

## Stage 5 — Behavior-Preserving Readability

**Status:** Complete (`2026-08-27`)

**Outcome:** The recovered generated source is easier to inspect and maintain without weakening its verified behavior or provenance.

**Focus:** Apply reviewable, conservative improvements to organization, naming, formatting, navigation, and explanatory context, using recovered evidence rather than inventing original author intent.

**Completion signal:** Readability improvements are documented and reviewable, the reproducible artifact and complete accumulated validation suite still pass, public API/ABI surfaces remain stable, and no known behavioral or provenance ambiguity has been hidden.

**Verified evidence:** `datomic-rev/reports/source-guide.md` now maps provenance
boundaries, subsystem entry points, execution flows, generated-source hazards,
and the validation ladder without rewriting the recovered corpus. The
deterministic semantic index was regenerated for all 142 namespaces and 2,906
definitions; `corpus.edn` has SHA-256
`88ac1c629a205cba92e00b1ec7b25327575ef24a89d57e83912a6d14cd366bcb`.
Only the corpus, namespace, and Var layers changed; call, reference,
dependency, keyword, string, gap, and unmapped-class tables remained
byte-identical. `.gitattributes` preserves meaningful trailing empty TSV
fields without changing their bytes. The final Stage 1, Stage 2, and Stage 3
gates all passed after these documentation/index changes; details are in
`datomic-rev/reports/stage-5-readability.md`.

## Goal Completion

This goal is complete when all five stages meet their completion signals, material evidence and remaining limitations are recorded, and a fresh continuation audit finds no unfinished stage or unexplained regression against the recovered baseline.

## Current continuation state

- Current stage: **Goal complete — all five stages passed (`2026-08-27`)**.
- Verified state: Stages 1 through 5 are complete through the durable
  `datomic-rev/scripts/validate-stage-1.sh` and
  `datomic-rev/scripts/stage2/validate-postgresql.sh` and
  `datomic-rev/scripts/stage3/validate-postgresql.sh` gates, the Stage 4
  evidence/inventory, and the Stage 5 guide/index audit. The final recovered
  artifact SHA-256 is
  `bc7836b124896706a9bde9cdd0e6af84279dc06dbb0bee41ea448382f818cdfe`.
- Fresh continuation audit: every shell script passed `bash -n`; all 143
  Clojure files in `src-clj` passed the non-evaluating reader; all 118 Stage 2
  and 64 Stage 3 retained evidence hashes revalidated; the regenerated source
  index is byte-identical to the checked-in final index; `git diff --check`
  passed; and all owned fixture PIDs/listeners were stopped.
- Known limitation carried forward: the canonical artifact is source-bearing;
  the pinned Ubuntu Java image lacks Java 11 `ct.sym`, so handwritten Java uses
  recorded `-source/-target 11` settings rather than `--release 11`.
- Known Stage 2 boundary carried forward: recovered `datomic.backup` was tested
  through a test-only Storage implementation because the proprietary
  `datomic.fsbackup` adapter is absent from the recovered artifact; the public
  `file:` adapter/CLI is not claimed.
- Remaining claim boundary: the 157 unresolved warnings remain explicit; other
  backends, broader schedules, security behavior, multi-transactor failover,
  and recovery of the proprietary transactor remain outside this goal.
- No unfinished action remains within Goal 1. Any expansion of those bounded
  claims should begin as a separate goal with its own disposable fixtures and
  acceptance criteria.
