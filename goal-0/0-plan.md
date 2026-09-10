# Goal 0 — Datomic documentation capability audit

## Objective

Systematically read every file in `datomic_pro_docs` and compare the user-visible
functionality it describes with Atomic's current implementation. Identify the
remaining missing or partial capabilities, like the EDN frontend gap, and produce
an actionable, deduplicated catalog with source evidence. This goal identifies
gaps; it does not implement them or create implementation goals automatically.

## Status and ownership

Started 2026-09-10. Audit in progress. Ten documentation stages follow the ten
major folders; final synthesis reconciles their findings. The per-file checklist
and findings are in [1-audit.md](1-audit.md); this plan owns scope and stage status.
Initial corpus: 90 Markdown documents plus supporting illustrations and the CSV
manifest. Source has concurrent uncommitted EDN work, so each finding describes
inspected evidence rather than claiming a frozen or newly tested build.

[Goal1](../goal-1/0-plan.md) owns EDN reading/writing and transaction/query/pull
frontends in another session. Link EDN requirements to that owner; do not edit its
files, reimplement its work, or assume it is complete. Review the semantic engine
beneath EDN independently. EDN is an external workstream, not a prerequisite for
continuing this audit. Historical archived goals are context, not instructions.

## Constraints and evidence standard

- Preserve source, tests, data, existing goals and concurrent changes. Write this
  audit under `goal-0`; no product mutations are needed for identification.
- Local Datomic Pro docs are the requirement source. Read each document, including
  normative tables, examples and API entries. Follow local cross-references when
  necessary; use recovered source only to resolve concrete ambiguity. Images are
  supporting parts of their parent document, and the manifest is inventory.
- Compare behavior, public API reachability and user workflows, not symbol names
  alone. Typed Rust equivalents count as present; optional and inconvenient
  requirements must still receive an explicit disposition.
- Distinguish **source-confirmed gap**, **partial support**, **candidate requiring
  validation**, **present equivalent**, **EDN-owned**, and **platform difference**.
  Lack of a search hit alone cannot establish absence. A platform difference does
  not automatically justify omitting a portable capability.
- Record the documented requirement, inspected implementation, user impact,
  document/code paths and line anchors, confidence and a concrete validation or
  implementation boundary. Merge duplicate mentions under stable gap IDs.
- Keep documented semantics separate from performance claims and historical
  bug-fix notes. Do not convert old release chronology into a list of current
  requirements without checking applicability.
- Inspected tests are evidence of intended coverage, not new passing results.
  Use focused nonmutating checks only when they settle uncertainty. Do not run
  shared PostgreSQL fixtures or builds against another session's changing source.
- Do not infer total Datomic/JVM/wire parity as the goal. Record incompatible
  runtimes, backends and vendor services explicitly alongside useful native
  equivalents and any missing portable functionality.

## Stages

Each folder's completion signal requires all its files to have an evidence-backed
review entry in the audit, with gaps/candidates linked to the shared catalog and
present or platform-specific behavior accounted for. A reviewed stage may retain
explicitly unresolved candidates; it must not call them confirmed absence.

### 1. Orientation — `00_start_here`

**Status:** Active (7 documents).

**Outcome:** Account for promised product, installation, language and release capabilities.

**Focus:** Current portable features, supported entry points, historical release
notes and vendor packaging; assess applicability rather than port every old fix.

**Completion signal:** All seven files meet the evidence standard above.

### 2. Tutorial workflows — `01_tutorials`

**Status:** Pending (8 documents).

**Outcome:** Map each introductory end-to-end workflow to Atomic APIs or tools.

**Focus:** Database lifecycle, schema/data authoring, queries and history,
including usability gaps and EDN-owned input paths.

**Completion signal:** All eight files meet the evidence standard above.

### 3. Core data model — `02_core_concepts`

**Status:** Pending (6 documents).

**Outcome:** Resolve gaps in database values, entities, filters and programming with data.

**Focus:** Read semantics, best practices and glossary promises; link EDN work to Goal1.

**Completion signal:** All six files meet the evidence standard above.

### 4. Schema and identity — `03_schema`

**Status:** Pending (4 documents).

**Outcome:** Compare schema, evolution, modeling and identity with the documented contract.

**Focus:** Portable semantics and authoring workflows, including limitations hidden
behind otherwise present typed APIs.

**Completion signal:** All four files meet the evidence standard above.

### 5. Transactions — `04_transactions`

**Status:** Pending (9 documents).

**Outcome:** Account for every documented transaction and synchronization capability.

**Focus:** Forms, functions, atomicity, lifecycle, partitions and hints; distinguish
native equivalents from unsupported behavior.

**Completion signal:** All nine files meet the evidence standard above.

### 6. Queries and pull — `05_query_and_pull`

**Status:** Pending (4 documents).

**Outcome:** Map query and pull grammar and behavior to actual native capabilities.

**Focus:** Inputs, outputs, operators, joins, rules, functions, projections and
evaluation semantics; EDN syntax stays with Goal1.

**Completion signal:** All four files meet the evidence standard above.

### 7. Indexes — `06_indexes`

**Status:** Pending (6 documents).

**Outcome:** Resolve differences in raw index access and index lifecycle.

**Focus:** Ranges, direction, index pull, readiness, caching and public index APIs.

**Completion signal:** All six files meet the evidence standard above.

### 8. Peer API and diagnostics — `07_peer_api`

**Status:** Pending (18 documents).

**Outcome:** Account for the portable capability behind documented Clojure and Java APIs.

**Focus:** Deduplicate language wrappers; examine lifecycle, database values,
futures, logs, errors and statistics.

**Completion signal:** All 18 files meet the evidence standard above.

### 9. Operations — `08_operations`

**Status:** Pending (10 documents).

**Outcome:** Identify missing deployment, reliability and observability capabilities.

**Focus:** Storage, transactor configuration, capacity, failover, backup/restore,
caches, logging and tuning; assess Rust/PostgreSQL equivalents.

**Completion signal:** All ten files meet the evidence standard above.

### 10. Optional capabilities — `09_optional`

**Status:** Pending (18 documents).

**Outcome:** Account explicitly for optional clients, deployment, maintenance and tooling.

**Focus:** Do not silently exclude optional features. Distinguish product gaps
from platform choices and proprietary services.

**Completion signal:** All 18 files meet the evidence standard above.

### 11. Reconcile the remaining gaps

**Status:** Pending.

**Outcome:** A coherent catalog of the remaining functionality, with no duplicate
EDN work, unsupported absence claims or silently omitted documentation areas.

**Focus:** Cross-folder/API duplicates, dependency boundaries, useful groupings
and priority by user impact. Reconcile concurrent source changes that affect a
finding. Separate confirmed gaps, partial semantics and unresolved questions.

**Completion signal:** Every corpus file is accounted for; every material finding
has documentation and implementation evidence; duplicates are merged; EDN ownership
and platform differences are explicit. State the review method and uncertainty.
A static audit is not conformance-test certification. Implementation is outside
this goal's completion.

## Continuation

Start with the first unfinished folder, consulting the per-file checklist. Bounded
parallel reviews of disjoint folders are useful; integrate actual evidence before
marking a stage reviewed. Next: finish orientation/tutorial/core review, merge
schema/transaction/index, query/API and operations/optional reviews, then validate
the strongest candidates and reconcile the catalog. Do not stop after creating
this scaffold: gap identification was requested in the same task.
