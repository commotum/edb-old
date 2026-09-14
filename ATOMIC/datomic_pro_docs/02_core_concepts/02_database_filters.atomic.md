# Atomic trace — immutable database values and views

This development companion preserves the original [Database Filters](02_database_filters.md).
Source locators below use artifact-qualified symbols; numeric anchors refer to
the annotated checkout before the Stage 4 split, not stable API identifiers.
The unannotated reference revision is
`cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`.

## Value ownership and index reuse

The opening paragraphs promise that queries/index traversals accept differently
filtered values without changing their logic. The “Present Pays No Penalty”
paragraph requires a direct current read path, not a history scan that happens
to return current facts.

**Source:** [peer db.clj](../../1.0.7705/peer/src-clj/datomic/db.clj)::`Db`,
`seekEAVT/seekAEVT/seekAVET/seekRAET` (around 4727/5040 before this note).
The same shared implementation occurs in the
[transactor artifact](../../1.0.7705/transactor/src-clj/datomic/db.clj).
The record holds fixed index layers and view coordinates. Seeks merge ordered
layers; ordinary current reads do not unconditionally add the archived history
index. `acceptDataCheck` derives new persistent indexes and a new record.

**Native ownership:** [value.rs](../../src/database_value/value.rs) holds the exact shared handle;
[cursor.rs](../../src/database_value/cursor.rs) chooses current/history sources;
[overlay.rs](../../src/database_value/overlay.rs) flattens speculative
chains over one committed base and path-copied `index::overlay` structures.
The peer captures/adopts handles but does not own their read semantics.
Query selection in `query.rs::select_datoms`, Pull navigation and transaction
assessment use the same value. Retain this structural sharing and selective
dispatch; no second peer or transactor database-value engine is warranted.

## as-of and since

The “as-of” time-point list and examples specify an inclusive upper boundary;
“since” specifies an exclusive lower boundary and warns that an ordinary lookup
identity may be outside the resulting view. Its two-source query uses the
current value to resolve identity and the since value to inspect recent facts.

**Source:** `Db.asOf/Db.since` update record coordinates;
`as-of-t` seeks `:db/txInstant` using AVET, preserving its first exact
millisecond match (around 3995). Between matches it returns the next candidate's
T minus one; after all candidates it returns `nextT`, a boundary that need not
name an assigned transaction. `windowed` applies `<= asof` and `> since`.
`resolve-id` delegates ordinary lookup refs through `resolve-lookup-ref`;
idents use the derived dictionary rather than a temporal schema reconstruction.

**Native:** [views.rs](../../src/database_value/views.rs)::`resolve_time_point`,
`as_of_time_point`, `since_time_point` and the single window state machine
preserve these rules; [resolve.rs](../../src/database_value/resolve.rs)::`lookup`
uses windowed AVET except for the ident dictionary. View changes preserve the
captured basis, schema and ident projection. `entid_at` has the source's
separate first-at-or-after instant rule, not the as-of predecessor rule.

Existing independent expected-result checks:
[native_time_points.rs](../../tests/native_time_points.rs) covers duplicate
instants, exact T/Tx, inclusivity/exclusivity and partition-shaped boundaries.
[read_sequence_sources.rs](../../tests/read_sequence_sources.rs) and
[query_runtime_sources.rs](../../tests/query_runtime_sources.rs) exercise
composition; their full behavior is not established by this bounded trace.

## history and contextual custom predicates

“history” includes assertion/retraction events and excludes point-in-time entity
navigation. “Filtering Errors”, “Filtering For Security” and “Filtering on
Transaction Attributes” describe custom predicates. API `filter` specifies
conjunction and passes the unfiltered database to the predicate.

**Source:** `windowed` filters temporal/custom predicates before
`filter-retractions` (around 1426/1810); callbacks receive `(assoc db :filt nil)`.
`Db.filter` conjoins callbacks; `Db.history` sets raw without removing other
view coordinates. `Db.entity` and `Db.with` reject raw history.
`rseek-datoms` windows raw reverse history, then keeps each E/A/V group's last
event and retains assertions.

**Native:** `views.rs` owns these derivations and forward/reverse window state;
`cursor.rs` polls controls below the window, so a long rejected range remains
interruptible. History supports query/index access while Pull/entity and
speculation reject it. Native top-level stored BigDecimal representations remain
distinct; this established adaptation is not literal equality parity with every
recovered collapse path.

[streaming_time_windows.rs](../../tests/streaming_time_windows.rs) checks lazy
consumption and early drop. The moved value tests retain reverse filtering before
collapse and exact-value cases. These native/eager comparisons check internal
consistency where they share the engine, not independent Datomic equivalence.

## with is not a branch of the past

The “as-of Is Not a Branch” paragraph is authoritative: compute `with` over the
full basis, then reapply the read view, regardless of call order.

**Source:** `Db.with` calls shared `with-tx+opts/with-tx`; the latter builds
`ProcessInpoint/ProcessExpander` and an immutable successor, returning both
values, datoms and tempids without publishing a durable head. Recovered
filtered-call details are not promoted over the explicit documented contract.

**Native:** `views.rs::{speculation_base,with_speculation_view}` is composed by
the single [transaction pipeline](../../src/transaction/pipeline.rs).
`overlay.rs` owns successor sharing, not transaction semantic assessment.
[filtered_speculation.rs](../../tests/filtered_speculation.rs) independently
checks CAS against the full basis, retained views and history rejection.
[speculative_sharing.rs](../../tests/speculative_sharing.rs) and structural
overlay tests preserve branch independence, selective ranges and reclamation.

## Discardable acceleration is not database information

[read_context.rs](../../src/database_value/read_context.rs) retains one bounded
exact-prefix memo per attempt. A key
retains immutable-value identity; complete successful ranges alone are admitted.
Partial/error/cancelled reads cannot become complete results, and memo replay
still counts as logical work. Current temporal/custom windows do not memoize raw
history results. The shared transaction pipeline strips context from both
reported values. This is a native resource/acceleration policy, not a recovered
cache-port or source I/O-counter parity claim.

Stage 4 removes a different, now-redundant memo: `LastTxInstantMemo` used
`Arc/OnceLock/Mutex`, but every current basis already owns an infallible resident
instant (`Database` field, authenticated `BlockSnapshot` metadata, or overlay
field). Direct `Option<i64>` access preserves the value while removing the
allocation/lock/error path. Basis/clone/window/successor assertions replace only
the obsolete internal memo test. This is not a database scan optimization claim.

Fresh Stage 4 verification passed the moved library value/view/overlay and
read-context cases, including direct resident-time assertions. PostgreSQL
integration runs passed database_value (8), native_time_points (5),
native_speculation (3), snapshot_references (3) and the actual connection/log
workflows. These establish the exercised contracts, not exhaustive semantic
equivalence or a measured benefit from removing the scalar memo.
