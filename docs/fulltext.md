# Native fulltext search

Fulltext is an optional, eventually consistent search index over string facts.
It complements structured query; it does not replace identity, schema validation,
or the immutable database value supplied to a query. PostgreSQL remains the
durable store. The analyzer, expression evaluator, scoring and queries run in
Rust; Lucene/JVM interoperability and identical Lucene scores are not promised.

## Declare and search

Install a `ValueType::String` attribute with `Attribute::fulltext()`:

```rust
Attribute::new(1000, Keyword::new("article", "text"),
    ValueType::String, Cardinality::One).fulltext()
```

The flag is false by default, is queryable as `:db/fulltext` schema data, and
cannot be toggled after installation. Both single- and multi-valued string
attributes are supported. Legacy databases missing the descriptor must use the
explicit `fulltext_vocabulary_upgrade_ops` upgrade before installing flagged
attributes; opening a database does not silently rewrite its schema.

```rust
let report = database.fulltext(1000, "\"blue river\" OR mountain*",
    &FulltextOptions::default())?;
for hit in report.hits {
    // entity: u64, value: original String, tx: assertion transaction, score: f64
}
```

The direct API returns descending scores, with deterministic entity/value/tx
tie-breaks within that search. Datalog's `Function::Fulltext` takes an attribute
keyword or numeric reference and a search string; relation binding produces
`[entity, value, tx, score]`. Join those entities with ordinary structured facts
to enforce status, ownership or other selection rules. See the working syntax
in [queries.md](queries.md#fulltext-and-structured-facts).

## Analyzer and expression rules

The versioned native analyzer lowercases Unicode alphanumeric words, strips
English possessive `'s`, and removes straight/curly apostrophes. Accents remain;
there is no stemming or arbitrary substring matching. Thus `JANE'S` matches
`Jane`, `café` does not become `cafe`, and `cat` does not match `category`.

The documented English stop words are omitted:

```text
a an and are as at be but by for if in into is it no not of on or such that
the their then there these they this to was will with
```

Queries support terms, quoted phrases, parentheses, uppercase `AND`/`OR`/`NOT`,
and a trailing `*` on a nonempty word prefix. Adjacent terms default to OR;
AND binds more tightly than OR. Phrases retain stop-word position gaps, so
`"red blue"` does not match `red and blue`. A stop-word-only search is empty.

Every OR branch must have a positive anchor: `river AND NOT mountain` is valid;
`NOT mountain` and `river OR NOT mountain` are rejected rather than scanning a
negative universe. Leading/infix wildcards, fuzzy terms, field syntax, boosts
and range expressions are rejected with `fulltext/query-syntax`. This is an
explicit native grammar, not a permissive Lucene parser.

Scores use native BM25 term-frequency, document-length and inverse-frequency
weighting. Treat them as relative relevance for the available index, not a
probability, application invariant or cross-version/Lucene-compatible number.
Datalog relation ordering is unspecified even when the direct search is ranked.
The corpus is the retained historical assertions in the selected search
publication, not just entities visible through the final current/time/filter
view. Repeated assertions at different transactions are distinct documents;
speculation adds its local assertion documents. Final hit validation enforces
the supplied view, but does not recalculate a view-specific corpus. Consequently
historical facts and filtered-out documents can influence a visible hit's score.
Filters are not a promise that ranking statistics reveal nothing about the
underlying corpus; use an appropriate independent database/access boundary for
that requirement.

## Views, indexing and limits

Candidates are checked against the supplied value's visible facts. Retained,
`as_of`, `since`, history, filtered and speculative views must not leak a fact
hidden by that view. The `tx` column is the visible assertion transaction, not
the indexing time. An old assertion can still appear in history after its
current value is retracted. Candidate availability and scoring remain eventual:
a stable logical snapshot key is not a promise of identical fulltext results
as its search projection becomes available or advances.

Native search reads the separately derived immutable projection; it must not
fall back to materializing the whole native database when the projection is
missing. Missing/unusable search data is an explicit error. Fulltext coverage
is reported through `FulltextStats::index_basis_t`; a value newer than this
frontier may have missing matches. Ordinary transaction acknowledgment is not
a search-index completion receipt. Small deployments can lower background
index scheduling thresholds, or an attached writer connection can request
indexing. A read-only connection's transaction socket currently does not carry
maintenance requests. Do not turn an absent hit into a uniqueness or absence
constraint.

`FulltextOptions` defaults to at most 10,000 returned hits, 10 million work
units, 64 MiB cumulative logical allocation/read admission, a 64 KiB query,
4,096 lexical query tokens and 64 levels of grouping/negation. Nesting is also
clamped to a hard maximum of 128, even if a caller requests a larger allowance;
flat Boolean expressions are balanced rather than recursively chained. These are configurable operation
budgets, not datom/storage limits, process RSS or network-traffic measurements.
Cancellation and an optional deadline are checked cooperatively. `limit` is
explicit top-k truncation (`stats.truncated`); work/byte/parser budgets fail with
an error instead of silently returning partial results. Limit zero returns no
hits and reports truncation when matches exist.

Queries debit search work and allocated bytes from their enclosing query or
stored-program budget, including attempted work on failure. A query row limit
remains an error, not an implicit search top-k. `QueryStats` reports search,
lagging-search and truncation counts; nested query counters are included.
`FulltextStats::read_bytes` counts authenticated search pages fetched from
PostgreSQL (zero for eager fixtures); `admitted_bytes` is the separate cumulative
logical allowance. Search-page bytes exclude metadata, protocol overhead and
structured candidate-validation reads. Use the operation's total SQL observation
for that broader I/O picture.

## Stored programs and operations

Fulltext-bearing portable native templates select program ABI 9, including
nested queries and rules. Existing programs that do not need fulltext retain
their previous encoding and hash. Fulltext is also allowed in transaction
programs: the writer re-executes the program and commits its grounded facts.
Its selection can depend on eventual index availability, so do not describe
such a program as deterministic from its logical db-before key alone. Use
structured indexes for correctness constraints needing complete membership.

Search data is derived, not authoritative history: backups/recovery must
preserve canonical facts and rebuild search projections. Cache hits must not
bypass access/generation rules or turn corrupted search pages into empty
results. Operational benchmarks should report dataset/index coverage, cold
versus warmed reads, measured SQL/payload I/O and real memory separately from
logical work counters.

The current builder streams fulltext history into bounded external sorted runs
and builds a separate Merkle tree for each canonical source manifest. It does
not incrementally reuse the previous search corpus; rebuild cost grows with
that corpus. It preserves canonical datom roots and their old encodings.
`PostgresIndexer::with_fulltext_build_limits` controls sort memory, target page
size, record/key capacities, record count, cumulative spill writes and the work
directory. Default sort admission is8 MiB, target pages64 KiB, at most10 million
input records and4 GiB cumulative spill writes. One admitted record/page can
exceed the target sort/page size; hard format limits remain enforced, never
silently truncating tokens. Spill counts include merge rewrites, not live disk
occupancy; `peak_buffer_bytes` measures the sort buffer, not total process RSS.

`NativeFulltextReader::cache_stats` exposes cache-owned decoded bytes, entries
and configured bounds. This positive header/page cache has a separate allowance
equal to the configured native tree-cache bounds; it is not part of tree-cache
occupancy and is not persisted in the optional SSD cache. Active cursors may
retain additional pages outside cache ownership. A fully warmed search requires
no SQL, including no foreground health check; a cold miss checks exact retention
and reacquires that source's pin or fails safely.

Canonical index success and search readiness are separate. The background
service exposes `BackgroundIndexingStats.fulltext`, with attempted-source basis,
failure details and retry state, not a universal search-ready watermark. Transient
Busy/Unavailable/Interrupted failures receive up to8 idle retries with exponential
250ms delay capped at30s; permanent failures remain visible until an explicit
index request or repair. Exhaustion is visible and needs operator intervention.
Ordinary transactions do not wait for search readiness. `atomic consolidate`
reports canonical `INDEXED` and a separate `SEARCH checked/failed` line.

Restore deliberately starts without derived search pages; `rebuild_fulltext`
reconstructs them from authenticated restored facts. Owner-only
`FulltextStore::discard_projection` is an explicit bounded repair of one named
source; it never edits source facts. During repair, retained readers may cold-fail;
reopen peers after repair that changes the search root/analyzer to discard cached
headers. Ordinary GC preserves pinned sources, retires unpinned canonical sources
under existing policy, and reclaims orphan search blocks in bounded ledger batches.
Excision publishes a new generation; search is reconstructed from that generation
and may be unavailable until its projection is ready. New current and history
views cannot return removed facts. Already-held authorized old values
retain the repository's existing pre-excision access policy; excision is not a
promise to erase bytes or memories already delivered to an application.

[query_fulltext.rs](../tests/query_fulltext.rs) exercises structured joins,
prepared inputs, exact views, grammar/ranking/truncation and program budgets.
With `ATOMIC_POSTGRES_URL` set, it also deploys stored ABI9 query/transaction
programs and verifies native execution, speculative/committed agreement and exact
receipt retry before/after writer restart; an unset URL explicitly skips this
PostgreSQL witness. The separate application executes the read-only query path
with the setup in [application.md](application.md).
[fulltext_schema.rs](../tests/fulltext_schema.rs) covers schema installation and
immutable-flag enforcement. These tests do not establish Lucene parity or a
general-purpose search throughput claim.
