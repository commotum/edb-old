# Goal 2 — Block-backed immutable database values

## Objective

Execute Stage 2 of `/home/jake/Developer/atomic/goal-0/0-plan.md`. Reconstruct and
query immutable database values from Rust-owned log/index/metadata structures over
the new opaque store. Keep existing Datalog, EDN, Pull, navigation and speculative
semantics. No eager whole-database fallback or relational catalog lookup.

Use local `datomic_pro_docs/06_indexes/{01_index_model,02_background_indexing}.md`
and transaction/time docs as authority; recovered `datomic.log` and index code as
architectural evidence. Preserve current first-release correctness, not old formats.

## Internal stages

### 1. Shared immutable structures and traversal

Status: Complete.

Outcome: Selective authenticated log and index access over opaque objects.

Focus: Extract/reuse the existing generic durable cursor and recent merge rather
than rewriting query algorithms. Keep raw current tree encodings and their SHA
identities; Rust reachability understands each current block kind. Add compact
engine descriptors and persistent lookup structures only where needed for log,
receipt/catalog roots. Log append must not require rewriting an entire log tree.

Completion: New-store fixtures load selected tree/log ranges with bounded working
memory and fail clearly for missing/corrupt reachable objects; no old SQL involved.

### 2. Captured values and existing consumers

Status: Complete.

Outcome: `DatabaseValue` uses a block-backed snapshot with coherent metadata,
bounded recent tail, retained identity and protection.

Focus: Schema/idents, allocation and time coordinates; forward/reverse index seeks;
history/as-of/since/filter views and speculative branches. Use guarded captures
and explicit pin ownership. Keep program/fulltext/log hooks visible for their
owning implementation stages; never disguise unsupported native paths as absence.

Completion: Existing typed/EDN query, Pull, navigation and branching consumers
operate on actual PostgreSQL block-backed fixtures and agree with pure-engine
expected values, including captured views after newer publication.

### 3. Selectivity proof and parent handoff

Status: Complete.

Outcome: Honest evidence that ordinary opens/reads are selective, not full replay.

Focus: Larger-than-cache fixture, zero/tiny caches, narrow and reverse reads,
bounded tail reconstruction, cancellation through hidden cursor consumption;
measure complete open/query/branch costs and actual storage bytes/calls.

Completion: Permanent tests and actual PG application-library checks pass, costs
are recorded, and Goal 0 resumes Stage 3's real serialized transactor. Fixtures
are not the final writer; no claim that the product cutover is already complete.

## Decisions and continuation

Stage 2 and the full seven-stage parent are complete. The original boundary
evidence below records this child's handoffs; later children completed automatic
pin cleanup, live endpoints, fulltext and backup routing. Goal 0 and
`docs/acceptance.md` contain final integrated results and current measurements.
No Stage 2 work remains pending.

Goal 1's object/ref implementation and root codecs are verified. Do not wrap old
`TieredSnapshot` relational machinery behind another provider variant. Extract its
already generic traversal into a neutral module and build `BlockSnapshot` beneath
existing higher-level value functions. Temporary old/new read dispatch belongs to
this staged transfer and must disappear during the full cutover.

The generic durable traversal/cache/recent merge now lives in `tree_cursor.rs`;
old peer/backup paths reuse it while ownership transfers. New `storage/log.rs`
stores bounded immutable tail pages with backward skip links, canonical transaction
objects and chunks for large transactions. Current raw tree/program hashes remain
their actual object identities. Rust index/metadata descriptors and captured value
roots provide schema, allocation and time coordinates without SQL catalogs.

`DatabaseValue` now supports the block source for queries, Pull, navigation,
historical/filter views and selective speculation. Captures pin an exact ref
revision; explicit release refuses shared values. Complete automatic pin/barrier
lifecycle remains Stage 5. The read connection/cache is being shared through a
`BlockReader` using existing configurable async resource cleanup; no new global
limit on the number of immutable values.

Verified initial real PostgreSQL run (fresh schemas; none skipped): four log and
four block-value tests passed. The log appended 1,091 transactions with 2,182
immutable object writes; complete append loop 2.036s, reopened root two object
reads and selected lookup at most five. A current 64MiB transaction body round
tripped through chunk objects in 1.336s (append/open/read). These are local samples,
not throughput promises. The 10,000-entity fixture encoded 13,638,612 tree bytes;
complete open/query/speculation used 115/114 SQL calls and 1,011,146/1,006,742
payload bytes with zero/16KiB caches, respectively (60.503/58.758ms).

Final checks passed: `cargo check --tests --offline`; release unit filters
`storage::` (20 tests, including two existing allocation tests),
`boundary_bias_tests` (3), `cache_cost_tests` (2). Actual PostgreSQL release targets
`block_log` (4), `block_native_log` (1), `block_snapshot` (10), `block_storage` (12)
and `block_storage_isolation` (1) all passed with no skips. The empty fixture first
used an invalid reserved frontier above genesis's ordinary frontier; the fixture
was corrected and the whole reader/foundation group rerun. Native log query
adapters retain original noHistory information and exact T/Tx/Instant bounds.
Programs invoke/speculate from opaque objects; schema/readiness/time/cancellation
and corrupt cold leaf behavior match the retained semantics. Eighty captured
snapshots share one connection/cache; cleanup reuses the existing executor.

Final larger-than-cache sample: same bytes/calls as above, 61.710/62.055ms for
complete open/query/speculation with zero/16KiB cache. Latency varies with local
load; no constant-time cache-hit or arbitrary-scale claim is made. Explicit
pin release and GC barrier integration remain the named lifecycle stage.

The original writer, live-index/endpoint and backup handoffs to Stages 3/4/6 have
been executed. Full-product acceptance rests on their actual application and
operations checks as well as these reader fixtures.
