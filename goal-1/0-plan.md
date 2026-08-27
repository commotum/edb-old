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
- The current warning inventory contains 318 unique texts: 306 reflection warnings, 10 primitive-local `recur` warnings, and 2 auto-boxing warnings. They are not load failures.
- “Complete” for this goal means satisfying the observable completion signals below, not claiming literal recovery of unpublished original source.

## Stage 1 — Reproducible Source-Built Artifact

**Status:** Pending

**Outcome:** A canonical, reproducible artifact is produced entirely from the recovered source and packaged resources and is suitable as the sole recovered implementation under test.

**Focus:** Establish the authoritative build boundary, pin material inputs, exclude original AOT fallbacks, package runtime resources and metadata correctly, and verify the resulting artifact with the existing source-load, API/ABI, and in-memory parity checks.

**Completion signal:** Two independent clean builds produce the same artifact hash; the build records its material inputs; inspection confirms that no original peer/core2 AOT classes satisfied the build; and the packaged artifact passes namespace loading, API/ABI validation, resource checks, and the documented in-memory parity workload.

## Stage 2 — External SQL and Recovery Validation

**Status:** Pending

**Outcome:** The source-built artifact has repeatable evidence of correct operation with an external SQL backend and across backup, restore, and supported failure-recovery paths.

**Focus:** Exercise normal storage lifecycle and recovery scenarios in disposable environments, check persisted state and invariants before and after recovery, and compare against documented expectations or the licensed reference implementation where an explicit oracle is needed.

**Completion signal:** A repeatable end-to-end matrix passes for SQL operation, backup, restore, and selected recovery scenarios using the source-built artifact, with data-integrity assertions and diagnostic records sufficient to reproduce any failure.

## Stage 3 — Concurrency and Fault Injection

**Status:** Pending

**Outcome:** The source-built artifact has meaningful resilience evidence for concurrent and rejected work rather than only successful sequential paths.

**Focus:** Cover connection lifecycle races, async/IOC paths, transaction contention, thread-pool rejection, cancellation, cleanup, and bounded fault scenarios with observable invariants and reproducible schedules or seeds where practical.

**Completion signal:** The repeatable concurrency and fault suite covers the named risk areas and completes without unexplained behavioral mismatches, deadlocks, leaked lifecycle state, silent data loss, or unbounded resource growth; failures retain enough evidence for deterministic investigation.

## Stage 4 — Bytecode-Proven Warning Repairs

**Status:** Pending

**Outcome:** Compiler warnings are reduced only where a correction can be proven from the original bytecode, while every unresolved case remains explicit and unchanged.

**Focus:** Map each candidate warning to the relevant original descriptors, casts, invocation instructions, and primitive operations; make the smallest uniquely supported source correction; and re-run all applicable structural and behavioral checks after each coherent change set.

**Completion signal:** Every modified warning site has recorded bytecode evidence that uniquely determines the correction; no warning was guessed or suppressed; all prior validation remains green; and the warning inventory clearly distinguishes repaired, intentionally unresolved, and newly discovered cases.

## Stage 5 — Behavior-Preserving Readability

**Status:** Pending

**Outcome:** The recovered generated source is easier to inspect and maintain without weakening its verified behavior or provenance.

**Focus:** Apply reviewable, conservative improvements to organization, naming, formatting, navigation, and explanatory context, using recovered evidence rather than inventing original author intent.

**Completion signal:** Readability improvements are documented and reviewable, the reproducible artifact and complete accumulated validation suite still pass, public API/ABI surfaces remain stable, and no known behavioral or provenance ambiguity has been hidden.

## Goal Completion

This goal is complete when all five stages meet their completion signals, material evidence and remaining limitations are recorded, and a fresh continuation audit finds no unfinished stage or unexplained regression against the recovered baseline.
