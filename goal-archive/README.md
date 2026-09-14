# Archived goal passes

Archive restored from commit `d1670ae` and extended on 2026-09-10.
G6 is the completed EDN and native capability pass;
there are currently no active root-level goal scaffolds. See the
[product guide](../README.md) and [acceptance record](../development/validation/README.md)
for current usage and verified operating limits.

| Pass | Folders | Purpose |
| --- | --- | --- |
| G1 (formerly A1) | `goal-0` through `goal-8` | Original Rust/PostgreSQL roadmap and initial implementation milestones. |
| G2 (formerly A2) | `goal-9` through `goal-17` | Corrective parent, semantic/architectural repairs, and partial native connection work. |
| G3 | `goal-0` through `goal-6` | Completed native core/application/recovery acceptance, measured scale, and the preserved failures and repairs leading to it. |
| G4 | `goal-0` through `goal-7` | Completed seven-stage product pass: storage and functional upgrades, partitions, fulltext, secure remote applications, consumers, administration and integrated operating acceptance. |
| G5 | `goal-0` through `goal-7` | Completed twelve-review-repair pass: declarative correctness, stack-safe ownership, numeric behavior, transaction bookkeeping, bounded peer reads, incremental fulltext and integrated PostgreSQL/application acceptance. |
| G6 | `goal-0` through `goal-10` | Completed EDN and documentation-gap capability pass: composable reads, query data/computation, schema/identity evolution, lifecycle, operations/diagnostics, offline backup reads, log caching, maintenance controls and integrated acceptance. |

These 52 folders retain their original contents. Plans, continuation prompts,
status claims, source line numbers, and paths describe their historical state;
they are evidence, not active execution instructions or fresh verification.
Resolve original root-relative goal paths within the corresponding historical
pass, not against any future repository goals. G3's former root `goal-0` through
`goal-6` now live under `G3/`; the subsequent root `goal-0` through `goal-7`
live under `G4/`. The review-repair pass's root `goal-0` through `goal-7`
now live under `G5/`; the subsequent EDN/capability pass's root `goal-0` through
`goal-10` now live under `G6/`. Historical A1/A2 references mean G1/G2. Links to
the former repository root or across passes may need that mapping. The moved
documents are not rewritten or turned into active instructions. Earlier Goals
18–20 were planned but never had folders; their relevant work was reconciled
during G3. G4's parent records completion of the subsequent product/capability
phase; its continuation prompts are archived evidence, not a new active task.

Useful references:

- [Original objective](G1/goal-0/0-plan.md)
- [Semantic foundation](G1/goal-1/SEMANTICS.md)
- [Corrective evidence ledger](G2/goal-9/EVIDENCE_LEDGER.md)
- [Operational contract](G2/goal-15/OPERATIONS.md)
- [Tiered writer architecture](G2/goal-16/ARCHITECTURE.md)
- [Partial connection milestone](G2/goal-17/0-plan.md)
- [Completed native core parent](G3/goal-0/0-plan.md)
- [Native semantic/read acceptance](G3/goal-4/0-plan.md)
- [Operational integrity and repairs](G3/goal-5/0-plan.md)
- [Integrated workload and failure evidence](G3/goal-6/0-plan.md)
- [Completed native product parent](G4/goal-0/0-plan.md)
- [Final product and operating acceptance](G4/goal-7/0-plan.md)
- [Completed review-repair parent](G5/goal-0/0-plan.md)
- [Repair verification and integrated acceptance](G5/goal-7/0-plan.md)
- [Completed native capability parent](G6/goal-0/0-plan.md)
- [Documentation capability audit and dispositions](G6/goal-0/1-audit.md)
- [EDN frontend acceptance](G6/goal-1/0-plan.md)
- [Final capability integration](G6/goal-10/0-plan.md)

The earlier JVM recovery goals discussed in the review are a different
historical numbering scheme, retained in Git at
`b8ebb1af74e6357d8242b2713af92f98d9e20e3a`. They were not present as active goal
folders at this archival boundary.
