# Goal 3 — Complete Native Transaction Values

## Objective

Complete Goal 0 Stage 3: declarative transactions preserve strong identity and
schema meaning across supported reference forms, and applications can apply
successive pure speculative transactions to native immutable database values.
Use the existing bounded assessor and controlled runtime without reconstructing
the whole database or weakening durable validation and retry meaning.

## Constraints and context

- Inherit Goal 0; Rust and PostgreSQL only, no new parent or nested child goals.
- Local docs govern semantics. `04_transactions/02_transaction_data.md`,
  `03_schema/00_schema_data_reference.md`, and peer API `with` sections define
  input references, tuple slots, and speculative information values. Recovered
  `1.0.7705/peer/src-clj/datomic/db.clj` provides tuple-id/tempid/upsert evidence;
  use implementation patterns where useful, not exhaustive JVM equivalence.
- Goal 2 completed independent connection/submission/observation acceptance.
  Preserve its work. Its broader peer regression run passed 19/19; any later
  owning failure reopens Goal 2 before proceeding with further child work.
- `DatabaseValue` already has an exact single-transaction assessment overlay
  and streamed reads. Its former chaining/raw-seek restrictions are now repaired;
  retain those repairs while completing reference inputs and controlled forms.
- Transaction-only `TxValue::Tuple` and `EntityRef::LookupInput` now preserve
  unresolved references until typed identity resolution. Stored values remain
  fully resolved; upsert and value-only-tempid rejection are retained.
- Stored `Value` remains fully resolved. Version new persisted/request/program
  input grammar deliberately; preserve existing durable receipts and hashes.
  Explicit resource policies must not masquerade as semantic limitations.

## Stages

### 1. Deliver pure chainable native successors

**Status:** Complete for primitive native successor values; controlled forms
and binding validation remain explicitly owned by Stage 3.

**Outcome:** Native `with` produces complete immutable before/after reports;
successors accept further transactions without persisting or changing the peer.

**Focus:** Reuse assessment and validation, retain native base sharing, support
current/history/time/index/query/pull access, and preserve schema/ident meaning
through chained successors. Avoid recursive-chain depth as an accidental limit.

**Completion signal:** Eager/native differentials cover successive adds,
retractions, identity and schema transitions; native successor queries and
navigation work; live PostgreSQL head and captured values remain unchanged,
with zero eager materializations.

### 2. Preserve unresolved reference input until identity resolution

**Status:** Complete. Typed tuple, lookup, runtime and live transport fixtures
pass; further integration failures reopen this stage.

**Outcome:** Legal tuple ref slots and reference-shaped lookup keys resolve
correctly against db-before and transaction-local identity; invalid forms fail
for the correct semantic reason.

**Focus:** Transaction-only input representation, tuple schema interpretation,
tempid/upsert fixed points, map/CAS/retract paths, and versioned request/program/
native transport handling. Stored facts contain only resolved references.

**Completion signal:** Source-backed fixtures cover homogeneous/heterogeneous
tuples, nil slots, idents/lookups/tempids, conflicting upserts, and value-only
tempids. Eager/native/durable paths agree; old request and program forms remain
readable/replayable and new forms survive native socket submission and recovery.

### 3. Integrate controlled generation and successor validation

**Status:** Complete. Controlled/filtered native transactions, exact predicates,
retained code dependencies and explicit resource controls pass actual PG tests.

**Outcome:** Speculative and durable paths enforce complete successor validation
and compose controlled functions/predicates with exact native before/after values.

**Focus:** Reuse existing program interpretation, explicit budgets, schema hooks,
and exact entity predicates. Resolve persisted bindings without writer locks or
eager recovery. Do not claim process-local JVM compatibility.

**Completion signal:** Programs and predicates accept/reject equivalent pure and
durable transactions; schema changes and chained speculation retain exact input
values; same-key retries/recovery preserve accepted information. The application
workflow includes native speculation and reference-shaped input.

### 4. Return verified transaction semantics to Goal 0

**Status:** Complete. Parent Stage 3 reconciled; Goal 4 is the next active child.

**Outcome:** The coherent transaction surface and remaining differences are
documented, with relevant actual PostgreSQL evidence.

**Focus:** Focused semantic differentials, durable/socket/restart checks, and
application integration. Update the parent and resume Goal 4 after closure.

**Completion signal:** Important transaction paths pass without full database
reconstruction; no known core input/successor gap is hidden as an exclusion;
parent Stage 3 records evidence and selects its next unfinished stage.

## Continuation

Goal 3 is complete, including the briefly reopened native stored-input depth
repair. Preserve these repairs and continue Goal 0 through scaffolded Goal 4.
Reopen Goal 3 if later acceptance exposes a transaction/successor gap. The
current-only `with` guard was repaired using the docs' "as-of is not a branch"
rule; full-basis transactions retain supplied result filters. History rejects.

## Closure evidence — 2026-09-08

- Final review reopened native structured admission for deeply nested stored
  `Value::Tuple` inputs. Iterative preflight now covers scalar inputs, legacy/
  new lookup keys, runtime map keys and encoding before recursive clone/sort.
  It matches the existing 16-level stored decoder policy, adding no legal
  tuple restriction. New regression passed, encoding suite 13/13 passed (1.62s),
  all-target clippy passed; ordinary scalar encoding avoids a new allocation.

- Final controlled speculation suite 4/4 live PostgreSQL, 66.79s; additional
  code-dependency count/byte cases 1/1, 17.64s. A 32-datom-limited attempt returns
  an unrestricted read value; generation and exact-after predicates share one
  invocation budget. Eighty chained code bindings survive cache misses and
  reclamation of precisely two unique uncommitted blobs in the disposable test.
- Filtered speculation: 1 pure test (0.29s), 1 actual PostgreSQL controlled test
  (18.41s). Full-basis CAS/time/uniqueness and basis schema, preserved as-of/since/
  custom filters, chain/view replacement and history rejection pass. noHistory
  checks preserve the captured basis's actual visibility, not an invented
  immediate-reclamation promise. Native eager materializations remain zero.
- Final library run: 250 passed, 1 ignored, no failures, 32.05s, with socket
  permission. Conditional PostgreSQL cases in that run are not integration
  evidence. Semantic conformance 22/22, tuple-schema 9/9, runtime 22/22 pass.
  All-target clippy, formatting and diff whitespace checks pass. A mistyped
  test-target invocation ran no tests; corrected explicit target names passed.
- Final public workflow after filter/code-limit integration:
  `workflow-129578-1788933777015713762`, reopen t=3. Schema, controlled rename,
  tuple references/lookup keys, query/pull/history, chained speculation,
  independent reads after writer shutdown and immutable old values pass.
- No scale claim follows from these fixtures. Existing generation-reference
  metadata reconciliation remains explicit Goal 5 work; measured operating
  envelope and final integrated acceptance remain Goal 6 work.

## Current integration checkpoint — 2026-09-08

- Tuple suite: 9/9 live PostgreSQL 15.11 main socket, 33.97s. Operation/map
  permutations, CAS/retraction, nil/type errors, conflicting upserts, socket
  receipts/recovery. Distinct symbolic tuple keys that converge only after
  target allocation reject atomically; recovered `get-ids` (6642) followed by
  `ProcessExpander.getData` (7366) does not require a recursive identity solver.
- Lookup suite: 5/5 live, 19.01s. Typed ref/tuple/nil/nested keys, map/CAS/retract/
  ref positions, exact db-before lookup, local/invalid reference rejection,
  unchanged SQL head, old/new receipt replay and zero eager materializations.
  A private assessor test proves both nested lookups charge explicit read limits.
- Runtime tuple inputs: 4 pure tests, all 22 existing runtime tests, and 1 live
  PostgreSQL/socket test pass (live 1.99s). Operation vectors represent tuples;
  entity-map vectors retain cardinality-many meaning. Lookup code literals use
  conditional ABI 6; old ABI 4/5 bytes stay unchanged and downgrades reject.
  New input selects native request version 2 and receipt grammar 3. Structured
  reference anomalies likewise require version 2; old receipt golden hashes pass.
- Shared `program_bindings.rs`: helpers 4/4 and existing live controlled durable
  suite 6/6, 99.62s. New native controlled suite initially passed 4/4, 66.59s,
  including full db-after predicates, same-before nested calls, head-lock
  independence, invalid bindings, and retained uncommitted code after reclaim.
  Additional shared-budget and dependency-count/byte checks are still running.
- Peer code fetches use a bounded cache; returned speculative values retain
  their authenticated code closure independently of eviction/GC. Explicit
  per-attempt dependency count/byte limits cover dormant fixed calls as well
  as execution. Cumulative speculative state can grow across calls, as its data
  delta does; no constant-memory chain claim is made.
- Post-integration native speculation: 3/3 live, 3.03s, including 2,000-step
  small-stack chaining. Updated public workflow passed as
  `workflow-119338-1788933232514102964`, reopen t=3, with controlled rename,
  typed tuple references, structured lookup and chained speculation.
- Library: 247 passed, 1 ignored, 1 sandbox socket denial; the denied deadline
  test passed when rerun with socket permission. Conditional PG fixtures in
  that library run are not live evidence. All-target clippy passed before the
  latest filter/closure-limit changes; rerun after final integration.
- Dependency traversal now includes callable lookup values and dual-predicate
  bodies. Older persisted generation-reference rows are not automatically
  repaired: Goal 5 must reconcile them before claiming complete GC integrity.
- Native filtered `with` follows documentation: the entire pipeline sees the
  full basis, then the original as-of/since/custom view is attached to the
  successor. Recovered internals likewise assess full-basis information, but
  pass the original view to custom function bodies. The native full-pipeline
  choice intentionally follows the docs' API-order rule, not that ambiguous
  implementation detail. New filtered-value tests are in progress.

## Verified changes — 2026-09-08

- `SpeculativeTransactionReport` and `DatabaseValue::with` reuse the exact tiered
  assessor and delayed validation. Values release attempt-local read diagnostics
  before returning; no writer call or eager materialization is introduced.
- Chaining failed with `database/overlay-cannot-chain` before repair. Overlays
  now flatten only speculative information over one shared committed base,
  preserving current additions, base retractions, complete history, and newest
  ident aliases. Chain iterator/drop stack depth is constant. Copy/sort work
  grows with the speculative delta; no constant-time chaining claim is made.
- Raw forward/reverse seeks merge a bounded-root base cursor with speculative
  information and preserve temporal windows. Instant resolution searches the
  flattened transaction sequence. Index enablement backfills the local AEVT
  attribute, following recovered `db.clj:add-avet` (3622); it does not wait for a
  durable publication that a pure successor can never create. A narrow internal
  base cursor reads only existing projection; normal native readiness checks
  remain intact. The old overlay-only rejection test was revised accordingly.
- `native_speculation`: 3/3 on PostgreSQL 15.11 restart fixture (no server restart
  in this suite), 2.85s. Includes eager/overlay/native current and history
  differentials, index enable/disable/re-enable, noHistory metadata, alias rename/
  repurposing, time windows, raw traversal, unchanged durable head, zero native
  eager loads, and a 2,000-step chain on a 256 KiB stack.
- Database-value unit fixtures 18/18 after backfill repair, 0.95s. Committed
  `avet_transition_waits_for_a_covering_native_publication` rerun on PostgreSQL
  passed 1/1, 2.43s: speculative-local readiness did not weaken live readiness.
- Tiered-assessor unit/differential fixtures 24/24, 7.44s, after speculative
  chaining/readiness changes. All-target clippy with warnings denied passed.
- Updated `native_workflow` passed with a queried/pulled speculative branch and
  unchanged original value before durable continuation; database
  `workflow-92820-1788931494225501085`, reopened at t=3.
- These are focused primitive-value checks, not full transaction/core/scale
  acceptance. Existing controlled function and input gaps remain above.

### Next implementation checkpoint

No external blocker is established. Stage 2 has a verified first tuple-input
implementation; reference-shaped lookup keys and controlled program conversion
remain unfinished. `db.clj:1261–1294` resolves only ref-typed tuple slots
and preserves nils/string tempids; `6940–6961` performs local tuple-id handling;
`7366–7455` replaces allocated tempids and rejects value-only tempids. Existing
`get-ids` compares unresolved identity values before final replacement; preserve
that distinction instead of prematurely storing temporary ids in `Value`.

`TxValue::Tuple(Vec<Option<TxValue>>)` now preserves symbolic entity slots through
upsert, resolves schema-typed ref slots before stored validation, and rejects
value-only tempids. Both eager and tiered assessors use the shared tuple resolver;
stored `Value::Tuple` remains unchanged. Encoding/decoding reject nested input
tuples and enforce the documented 2–8 scalar-slot shape. Local primitive grammar
and canonical comparisons/byte accounting recognize the new variant.

Authoritative submission hashing selects grammar 3 only for new tuple input;
old forms retain grammar 2 and their exact hashes. Native request frames select
version 2 only for new input; existing request/response version 1 remains usable.
`tuple_input_versions_are_explicit_and_old_receipt_hashes_remain_stable` passes
against two actual pre-extension receipt hashes read from Goal 2's PostgreSQL
process workflow, and rejects a checksummed version downgrade/truncated input.
`tuple_input` passed 2/2 with PostgreSQL configured, 2.20s: homogeneous and
heterogeneous refs/nils, symbolic tempid identity grouping, existing-owner upsert,
idents/lookup refs inside slots, invalid/value-only tempids, real native socket
submission, retry after writer restart, and durable recovery. Related pure
semantic fixtures 22/22, tuple-schema 9/9, and pure ref-identity witness pass;
the PostgreSQL-conditional ref-identity test in that unconfigured run was skipped.
It was then rerun with PostgreSQL explicitly configured: ref-identity suite
2/2, 1.93s, including actual production assessment and writer restart. Final
all-target clippy, formatting, and whitespace checks pass.

Primary next code points: `database.rs` EntityRef and eager
resolution; `tiered_assessor.rs` identity keys, resolve_value/resolve_entity;
`transaction.rs` canonical ordering and map normalization; `encoding.rs` request/
program input encoding; `submission_codec.rs` decoder; `program.rs` structured
runtime conversion and byte accounting. Keep legacy Lookup/Scalar requests
replayable. Add a transaction-only reference-shaped lookup-key variant without
breaking the existing stored-value Lookup constructor. Extend the conditional
grammar detection through every new entity/runtime position; guard recursive
input depth before comparison/encoding/normalization rather than trusting a
deeply nested Rust value. Runtime Vector-to-tuple emission, tuple CAS/retraction/
map fixtures, and broader invalid/permutation witnesses still need attention.
Do not claim Stage 2 complete yet. Successor program-binding validation and pure
controlled-form expansion remain Stage 3 work, including validation of changed
bindings even if this transaction does not execute the newly named predicate.

The temporary restart PostgreSQL fixture remains available at
`host=/tmp/atomic-goal-pg.MWicgR/restart-socket port=55433 user=jake dbname=postgres`;
the main fixture is `.../socket port=55432`. Do not overlap tests with a server
restart; README has the separate restart environment requirements. Tests run
here with explicit socket permissions. No test process is intentionally left
running at this checkpoint; PostgreSQL fixtures remain running for continuation.
