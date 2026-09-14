# Atomic study companion: fulltext search

This development trace expands the [Query Reference fulltext paragraph](02_query_reference.md#fulltext),
[Schema `:db/fulltext`](../03_schema/00_schema_data_reference.md#dbfulltext),
[Changing Schema](../03_schema/01_changing_schema.md),
[Background Indexing](../06_indexes/02_background_indexing.md), and the immutable
segment/caching passages in the [Index Model](../06_indexes/01_index_model.md).
[Native fulltext documentation](../../docs/05_query_and_pull/03_fulltext.md) owns the user-facing
grammar, scoring and controls. This companion explains source evidence and
dispositions; it does not assert Lucene interoperability or release completion.

## Sources and native owners

The complete recovered Peer
[fulltext](../../1.0.7705/peer/src-clj/datomic/fulltext.clj),
[fulltext-index](../../1.0.7705/peer/src-clj/datomic/fulltext_index.clj), and
[lucene](../../1.0.7705/peer/src-clj/datomic/lucene.clj) bodies were read and compared
with their Transactor counterparts. Their executable bodies match after generated
identifier normalization. Read callers include `extensions/{project,fulltext}`,
`peer/{Connection.notify-data,integrate-lucene,create-connection}`, and relevant
`db/{add-fulltext,install-attribute-hook,fulltext?,fulltext-attrs}` bodies and
`index/merge-db*` call sites. This does not claim complete Peer/Db/index coverage.
The five `datomic.impl.lucene` Java directory/input classes and `DatomicThunk`
were also read; their counterparts are identical before study comments.

| Native owner | Responsibility |
|---|---|
| [fulltext/model](../../src/fulltext/model.rs), [control](../../src/fulltext/control.rs), [search](../../src/fulltext/search.rs) | Options/hits/reports, cumulative budgets, source composition, candidate validation, ranking and truncation. |
| [analysis](../../src/fulltext/analysis.rs) | Versioned tokenization, bounded native expression grammar, phrase positions and BM25. |
| [reader](../../src/fulltext/reader.rs), [records](../../src/fulltext/records.rs), [difference](../../src/fulltext/difference.rs) | Captured reader, document/posting/statistic semantics and authenticated canonical-history differences. |
| [store/page](../../src/fulltext/store/page.rs), [cursor](../../src/fulltext/store/cursor.rs), [cache](../../src/fulltext/store/cache.rs) | Immutable byte-key pages/range authentication, independent lazy traversal state and bounded positive header/page cache. |
| [store/build](../../src/fulltext/store/build.rs), [incremental](../../src/fulltext/store/incremental.rs), [model](../../src/fulltext/store/model.rs) | Spill sorting/bulk construction, path-copy edits, build/read policies and projection metadata. |
| [storage/fulltext](../../src/storage/fulltext.rs) | Opaque object I/O, attachment/source binding and provider-side preparation. Publication remains with the coherent index/lifecycle owner. |
| [query/fulltext](../../src/query/fulltext.rs) | Four-column Datalog adapter, explicit source selection and enclosing query/program budget debit. |

## Paragraph-to-mechanism trace

Unqualified source symbols below are in `datomic.fulltext`.

| Pro passage | Exact recovered mechanism / reason | Native disposition and focused evidence |
|---|---|---|
| Schema `:db/fulltext`: optional boolean, default false, eventually consistent index | `db/install-attribute-hook` reads schema data and enables fulltext only for strings; `fulltext?`/`fulltext-attrs` select participating attributes. `peer/Connection.notify-data` queues Lucene work separately from datom integration; `integrate-lucene` batches it on a dedicated thread. | Native string-only immutable schema flag is ordinary queryable data; `fulltext_schema::fulltext_schema_is_string_only_immutable_ordinary_information` tests install, map form, invalid type and all alteration paths. Eventual source search availability is not copied as an artificial native delay. |
| Changing Schema: `:db/fulltext` cannot be altered | Installed descriptors feed `datum->doc`, per-attribute directories and query readers; changing analyzer membership is not an ordinary attribute edit. Pro supplies the explicit immutability requirement. | `model/schema::alter_with_work` rejects changes; request/program encoding includes the flag. `fulltext_schema::{attribute_requests_and_program_outputs_are_canonical_and_include_fulltext,native_fulltext_schema_survives_exact_retry_indexing_and_reopen}`. |
| Query Reference fulltext: database + attribute + search produce `[entity value transaction score]` relation | `fulltext_index/datum->doc` stores E/T and original analyzed V; attribute comes from the directory. `SearchIterator.next` emits six fields; `extensions/fulltext` calls `project` on columns 2–5 into a HashSet. | `query/fulltext::execute` retains four typed fields and ordinary relational joining. `query_fulltext::fulltext_relation_joins_structured_facts_and_prepared_inputs_are_rebound` checks expected E/V/T and prepared input rebinding. Fulltext is not entity-only lookup or uniqueness enforcement. |
| Query Reference example's value/transaction columns | `find-historic-docid` searches E/T but then compares V, since many strings can share one entity and transaction. `search-iterable` validates E/A/V membership but `SearchIterator` retains the indexed document's original T. | Native `records::doc_id` includes E/T/text digest and validates original text. Hit acceptance takes visible assertion T from the supplied value, not an older candidate. Existing many-string and reassertion cases in `query_fulltext` and `fulltext_native` independently check this native exact-transaction contract. |
| Schema analyzer defaults: case insensitive, apostrophe/possessive removal and listed English stop words | `lucene/index-writer`, `tokenize-terms`, `parse-query` use StandardAnalyzer with LUCENE_33. Source delegates token details to that library. | `analysis::{normalize,next_token,analyze}` implements declared native Unicode alphanumeric words and the same listed stop words; positions count omitted words, accents remain, no stemming. `analysis` unit tests plus `query_fulltext::{public_fulltext_syntax_scores_and_explicit_truncation,long_unicode_terms_are_not_truncated_or_conflated}`. This is not Lucene token ABI parity. |
| Query Reference search expression and relevance score | `lucene/parse-query` delegates to Lucene QueryParser. `search-iterable` asks for top-N (default 10,000) before exact-view filtering; `SearchIterator` divides by the highest raw hit score. | Declared native terms/phrases/Boolean groups/trailing prefixes reject unsupported syntax and unanchored negative branches. Native BM25 and deterministic direct-API tie-breaks are not normalized Lucene scores; final visible hits are deduplicated before truncation. Existing syntax/ranking/truncation tests check this contract, not copied floating examples. |
| Background Indexing / The Job: recent tier merges with immutable durable indexes | `fulltext_index/update-fulltext` publishes closed writers' persistent directory maps. `fulltext/search` combines `:memidx`, `:indexing`, `:index` readers; `lucene/multi-reader` does not close subreaders it does not own. | `search_native` combines the captured attachment with disjoint committed-recent/speculative assertions. `fulltext_native::indexed_native_search_is_view_safe_lag_visible_and_selective` checks matches before/after indexing and held snapshots; `block_fulltext::fulltext_installed_in_recent_tail_is_searchable_without_scanning_unrelated_facts` checks new attributes and unchanged work despite unrelated tail facts. |
| Background Indexing / When Sublinear Isn't: Lucene can perform large merges | `build-index` partitions changed fulltext attributes; `separate-history` finds assertions for retractions; `do-indexing-job` builds local overlays then promotes immutable files. Per-attribute work still delegates Lucene segment merging. | Native canonical `HistoryDifference` skips equal hashes, `DeltaRecords` tokenizes logical changes, and spill sorting/path-copy editing change only required search records. `block_fulltext::block_fulltext_is_selective_incremental_temporal_and_no_history_safe` checks small text deltas and nontext root reuse; difference/editor unit tests check repacking and bounded changed paths. No universal sublinear or constant-I/O claim. |
| Immutable indexes / caching: readers directly consume shared immutable data | `ClusterDirectory` is read-only; `ClusterIndexInput.clone` duplicates buffer position over shared bytes. `DirectoryRef` replaces persistent-map entries with newly written RAM files; `HybridDirectory` records base deletions for later publication. | Authenticated attachment/source/page identity and independent cursor state replace Lucene filesystem formats. Positive cache keys include source/page identity; header/page occupancy and decoded capacity are bounded. Cache mutation changes residency, not Db facts; live Arc readers can outlast cache eviction. Existing cache unit tests and cold/warm `fulltext_native` assertions verify values, occupancy and zero warm SQL. |
| Fulltext against explicit database values, including history/time/filter views | `search` includes its history tier only when `.isHistory`; `search-iterable` passes the same Db through `db/windowed` before accepting E/A/V candidates. The recovered tier branch alone does not establish every native temporal contract. | Native canonical-history corpus plus exact-view validation explicitly supports current/as-of/since/history/filter/speculation and respects noHistory omissions. `query_fulltext::fulltext_uses_retained_temporal_filtered_and_speculative_values`, native/block cases and assertion-only history-filter tests are the evidence. Do not infer precise native T/ranking from source tier names. |
| Bounded query execution applied to fulltext's exact-view validation | `search-iterable` demonstrates that database validation is part of search, not an optional postprocessing step. Source Lucene top-N is not itself a native work/deadline guarantee. | `search::accept_fulltext_candidate` uses controlled traversal through rejected historical candidates. The new `query_fulltext::fulltext_candidate_validation_polls_cancellation_inside_rejected_history` independently checks direct search and enclosing Datalog cancellation at candidate 3–4, then reuses the same view/controls and verifies every expected assertion T. |
| Background indexing failure and immutable publication boundaries | `do-indexing-job` propagates construction/promotion failures and schedules local cleanup; `write-changed-val` returns new IDs only after successful writes; `index/merge-db*` carries both new fulltext roots into its coherent root. | Build/read/corruption errors are explicit, not empty results or silent partial success. `fulltext_build_controls` checks invalid policy before connection, observable failure/restart recovery, and unchanged publication on restricted operator failure. `block_fulltext::block_fulltext_authenticates_attachment_binding_pages_and_cancellation` checks valid-hash wrong bindings, missing pages, page links and cancellation. |

## Deliberate semantic and operational boundaries

Native `index_basis_t` reports physical search-publication progress, not a
missing-match frontier. Search includes the requested attribute's bounded recent
and speculative assertions synchronously. If an indexed attribute requires an
attachment that is missing or incompatible, the operation fails explicitly;
it does not silently scan the whole persisted database. A newly installed
attribute after the captured index basis is different: its whole corpus is in
the unindexed tier and no older attachment is required for it.

Scores depend on retained assertion corpus statistics, not just final visible
entities. Ordinary retractions keep historical documents; physical noHistory
omissions/excision remove them. Exact-view validation prevents returning hidden
facts but does not make scoring statistics private to a filter. Logical snapshot
identity is therefore not a complete fulltext-answer-cache key, and a
score-dependent stored program is not deterministic from that key alone. Native
BM25/limit-after-validation/visible-T choices are explicit adaptations, not claims
that recovered Lucene produces the same ranking or historical tuples.

Search budgets are cumulative logical work/allocation/read admission, including
failed work and warm decoded-page visits, not process RSS or raw network traffic.
`query/fulltext` debits the enclosing query/program as search proceeds; cancellation
must reach inside candidate filtering. Cooperative polling cannot interrupt a
callback that never returns. Low-level page reads remain separately bounded, and
the reported search-object bytes exclude canonical validation reads and transport
overhead. Existing complete-operation SQL measurements cover a broader boundary.

The native first build and administrative reconstruction use bounded spill runs;
ordinary publications use authenticated history deltas and path-copy search
pages. Build policy includes record/key/page/spill bounds and explicit failure.
Source chunk sizes, Java directory bridges, Lucene segment merge scheduling and
daemon thread lifecycle are mechanism evidence, not native port requirements.
The removed idle-retry statistic has no live behavior to preserve or test.

Search attachments are derived but remain part of the authenticated published
read graph. Existing `fulltext_backup_restore` coverage exercises search alongside
stored-program dependencies, GC, restore and receipt-first retries. This study
does not replace lifecycle/retention ownership or promise that held handles pin
otherwise reclaimable storage. It also does not prove hostile-storage,
concurrency, scaling or analyzer compatibility beyond the named checks.

## Verification boundary

The new regression is confined to `query_fulltext`; broader existing cases are
reused. After core module integration, the integration owner runs the relevant
targets with the repository's real PostgreSQL fixture configuration:

```sh
cargo test --test query_fulltext --test fulltext_native --test block_fulltext --test fulltext_schema --test fulltext_build_controls --test fulltext_backup_restore
cargo test --lib fulltext
python3 development/source/inventory.py --check
```

Provider tests that skip without PostgreSQL are not equivalent to provider
verification. Source preservation and local links are mechanical checks, not a
semantic coverage percentage or first-release readiness decision.
