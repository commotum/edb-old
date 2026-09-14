# Development trace — EDN, exact values and serialized storage

This companion is separate from [Programming with Data and EDN](01_programming_with_data_and_edn.md).
It traces the important representation passages below, not the entire EDN
grammar, query compiler or storage lifecycle. Source coordinates refer to
unannotated revision `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; symbols remain
the stable anchors after comments shift lines. Rust paths name the current
Stage 2 checkout. Tests listed below were inspected, not run by this study.

## Ordinary data and EDN text are not the durable wire format

Passages: opening two paragraphs (ordinary data structures represented as text),
“Other Scalar Data Types” (separate keywords/symbols), and “Collection Data Types”
(heterogeneous lists, vectors and maps).

Source path: [transactor `io.clj`](../../1.0.7705/transactor/src-clj/datomic/io.clj),
`with-serialization-print-settings` (baseline 567), `clj->bbuf` (592),
`bbuf->clj` (605). Printing disables ordinary collection/nesting truncation and
then encodes UTF-8. The actual [catalog.clj](../../1.0.7705/transactor/src-clj/datomic/catalog.clj)
callers are `put-catalog` and `pod->catalog`: textual metadata is written and
read as data. This does **not** establish that index segments are EDN text.

For binary objects, [transactor `fressian.clj`](../../1.0.7705/transactor/src-clj/datomic/fressian.clj)
supplies `write-named` (276), `clojure-write-handlers` (290), and
`clojure-read-handlers` (325). Keyword and symbol handlers keep namespace and
name as separate components and request writer caching for repeated names.
Reader handling reconstructs the corresponding Clojure types, not plain strings.
Its map handler chooses a Clojure map representation by size; that JVM collection
choice is not a second stored attribute-value domain to copy into Rust.

Rust owners: [edn.rs](../../src/edn.rs), `read_edn`/`write_edn`, and
[edn_value.rs](../../src/edn_value.rs), `edn_to_value`, `value_to_edn`,
`edn_to_query_value`. Keep syntax data distinct from stored `Value` and general
query data. For example, reading a character or a general map does not imply a
stored attribute of that type exists. [Schema Data Reference](../03_schema/00_schema_data_reference.md)'s
stored `:db/valueType` vocabulary, rather than the EDN reader's entire domain,
governs stored attributes. This is a **retain** decision, not a missing `Value::Char`
finding. Native typed tags can replace JVM/Fressian tags without erasing these
type distinctions.

Checks: [edn_values.rs](../../tests/edn_values.rs),
`reading_data_does_not_silently_expand_the_stored_value_domain` tests that reading
a map, set, character, unknown tag or empty vector does not silently admit a
stored value; `result_shapes_and_nested_pull_maps_are_readable_data` checks output
as ordinary data. Complete query/transaction example execution is traced in
[Transaction Data](../04_transactions/02_transaction_data.atomic.md); it is not
proved by these text-conversion tests alone.

## Exact numeric values and named data survive native encoding

Passages: “Scalar Data Types for Numbers,” the signed long/bigint and exact bigdec
definitions and table; “Other Scalar Data Types,” the instant, UUID, symbol and
keyword rows. URI's stored type comes from the schema reference.

Source: `fressian/clojure-write-handlers` (baseline 294) writes Clojure BigInt
through a signed BigInteger byte array, not floating-point conversion;
`write-named` preserves name components. `user-write-handlers`/`user-read-handlers`
(345/369) add Datomic functions and configured extensions. Builtin scalar
serialization delegates to `org.fressian`; that library's Java implementation
is absent from these recovered Java trees. This pass therefore makes no claim
about the exact bundled library's builtin byte grammar or every IEEE payload.

Rust owner: [encoding/mod.rs](../../src/encoding/mod.rs), `encode_value`, `decode_value`,
`put_bigint`, `Cursor::bigint`, `encode_keyword`, `encode_symbol`.

| Value information | Native durable representation and boundary |
| --- | --- |
| BigDecimal | Signed coefficient plus signed scale; no conversion through double |
| BigInteger | Minimal signed big-endian bytes; redundant sign bytes rejected on decode |
| Keyword / symbol | Distinct tag, optional namespace and name; not flattened to a string |
| Instant / UUID | Signed millisecond integer / 128 original bits |
| URI | Original spelling; syntax validated separately from component equality |
| Float / double | Finite values encoded at their width; signed zero and NaNs canonicalized |

The last row is a necessary exactness qualification: `canonical_f32_bits` and
`canonical_f64_bits` normalize zero signs and NaN payloads in durable and request
bytes. [Native schema/identity documentation](../../docs/03_schema/01_identity_and_values.md#nan-replace-ordinary-values-never-use-as-a-unique-identity)
explicitly declines arbitrary NaN-payload preservation. EDN bit tags and
`stored_types_roundtrip_without_rewriting_representations` preserve those bits
only on the EDN conversion path. Do not advertise that test as durable
bit-for-bit preservation of every accepted representation. Retain the documented
native floating-point contract unless a separate source/contract decision changes
it; this study has not established Fressian's corresponding special-value behavior.

The native function value contains an immutable program-content hash, whereas
the source function handler serializes selected JVM function-definition fields.
This is an explicit native computation adaptation, not a claim to execute or
decode the original JVM payload.

Checks: [encoding/mod.rs](../../src/encoding/mod.rs),
`every_value_variant_round_trips_in_a_transaction` checks canonical re-encoding
and count, not every original float bit; `function_value_and_schema_type_have_stable_native_tags`
checks exact native tag bytes. [stored_value_repair.rs](../../tests/stored_value_repair.rs),
`scale_distinctions_survive_postgres_log_base_and_peer_recovery`, checks explicit
decimal scales across log/index/reopen when its PostgreSQL fixture is configured.
[uri_values.rs](../../tests/uri_values.rs),
`exact_edn_canonical_datoms_and_request_digests_keep_uri_spelling`, distinguishes
logical aliases from exact request representation. Durable tests are not claimed
fresh by this documentation-only study.

## Structure handlers, integrity and compression are separate layers

This section traces the mechanism supporting retained values, not a promise that
EDN syntax specifies compression or that a checksum by itself establishes ACID.

Source: `fressian/as-lookup` and `write-handler-lookup` (baseline 80/85) compose
custom structure handlers with the library. Real consumers include
[index.clj](../../1.0.7705/transactor/src-clj/datomic/index.clj), `fress`, which
serializes with `:footer true` and then compresses;
[log.clj](../../1.0.7705/transactor/src-clj/datomic/log.clj), `fressianed-dir` and
`fressianed-leaf`; [fulltext.clj](../../1.0.7705/transactor/src-clj/datomic/fulltext.clj),
`write-changed-val`; and [domain.clj](../../1.0.7705/transactor/src-clj/datomic/domain.clj),
`uncached-lookup-factory`/`peer-object-lookup`, which install `fressian/val->obj`.

Footer validation is caller-dependent. `fressian`/`defressian` (154/171) take
explicit footer options. `val->obj` (395) constructs its Fressian reader with
checksum validation disabled, reads one object, and drains the gzip stream before
returning it; failures are recorded and rethrown. Therefore it is incorrect to
say this materialization path calls `validateFooter` merely because a footer was
written or `create-reader` has a validating default. `io/gzip-buffer` and
`gunzip-buffer` provide compression, while `io/crc32`/`describe-bbuf` (762/779)
are also used by `cache/report-val-fn-fail` for diagnostic byte fingerprints.
Neither a diagnostic CRC nor a decoding success proves a durable acknowledgement.

Native paths to retain:

- [encoding/mod.rs](../../src/encoding/mod.rs), `encode_canonical_value` and
  `decode_canonical_value`, share one typed value grammar between
  [index/tree/mod.rs](../../src/index/tree/mod.rs) and durable data. Decode consumes
  exactly one value and requires byte-identical re-encoding. `encode_blob`/
  `decode_blob` separately enforce kind, current version, reserved byte, length
  and SHA-256 checksum. `decode_canonical_datoms` checks a count against remaining
  bytes before allocating the vector.
- [storage/codec.rs](../../src/storage/codec.rs), `encode_block`, `inspect_block`,
  `decode_gzip`, keeps raw content when compression plus envelope would be larger.
  The expected hash is over canonical bytes, independent of physical compression.
  Decode bounds declared output, checks gzip completion, rejects trailing data or
  another member, and checks canonical SHA-256. This is a native envelope, not
  Fressian wire compatibility.
- [storage/postgres.rs](../../src/storage/postgres.rs), `put`/`put_protected` and
  `decode_objects`, actually use that envelope at the opaque object boundary.
  Batch decode totals advertised canonical lengths before output allocation.
  [storage/object_io.rs](../../src/storage/object_io.rs)'s `ObjectReader` and
  `ObjectWriter` expose canonical bytes, so index/log consumers need not know the
  physical representation. Conditional publication remains a separate protocol.

The source fulltext writer names new values with `common/rand-uuid`; it is not
evidence of SHA-256 object addressing. Atomic's `put` computes the object ID from
canonical bytes and deduplicates by that ID. Retaining content addressing is an
explicit native choice supporting verified reads and reuse of identical payloads,
not a claim that Datomic's source uses the same object-name scheme.

SHA-256 checks agreement with an expected content identity; an unkeyed checksum
does not authenticate an arbitrary sender. Publication ordering, writer authority
and success acknowledgement require the separate storage/transaction trace.

Checks: `encoding::tests::checksum_version_truncation_and_wrong_kind_fail_closed`;
`block_codec_raw_and_compressed_round_trip_without_identity_changes`;
`block_codec_rejects_corruption_lengths_versions_and_multiple_members`.
These include malformed headers, content corruption, exact lengths, compressed
trailing data and raw content that happens to start with the envelope marker.
[block_backup.rs](../../tests/block_backup.rs),
`exact_numeric_values_and_earlier_receipts_survive_restore_and_rebackup`, supplies
a configured-PostgreSQL lifecycle check; it explicitly accounts for floating
canonicalization. No corruption guarantee is inferred for uninspected codecs.

## Scope, provenance and module disposition

[datomic/data.clj](../../1.0.7705/transactor/src-clj/datomic/data.clj) is a fixed
128-entry permutation table, not the datom model. The visible `config-ext` caller
optionally loads it alongside unrecovered `obscure`/`license-inline` code; no
table consumer was located. Preserve that evidence and its unknown purpose;
do not invent a native codec feature from the namespace name.
[datomic/codec.clj](../../1.0.7705/transactor/src-clj/datomic/codec.clj) provides
UTF-8/Base64 conversions used by crypto and coordination metadata. Those text
conversions neither encrypt bytes nor define the binary object grammar.
The peer/transactor `data.clj` and `codec.clj` files are baseline-identical;
`io.clj`/`fressian.clj` have corresponding helper mechanisms but generated-local
differences and are not byte-identical.

Disposition: retain native typed value bytes, canonical identity independent of
compression, bounded exact decoding and separate EDN conversion. Adapt JVM handler
dispatch, mutable ByteBuffers and backend text encodings at their native boundaries.
Do not add a Fressian dependency or new crate solely to mirror packaging.
The native owner is now [encoding/mod.rs](../../src/encoding/mod.rs), with natural
[program_query](../../src/encoding/program_query.rs) and
[submission](../../src/encoding/submission.rs) children, while physical compression
belongs to [storage/codec.rs](../../src/storage/codec.rs). This ownership cutover
changes no encoded bytes or public facade. Stage 5 application decomposition should
consider separating the shared canonical-value/framing core from transaction and
program protocols: the current helpers and cursor are shared across those grammars,
so extracting them is intentionally outside this bounded path cleanup. Moving the
entire module under the value model would couple generic values to higher layers.

No additional source-proven runtime semantic gap was established in this bounded
pass. Unreviewed areas include the absent builtin Fressian implementation,
complete EDN grammar conformance, arbitrary custom JVM handler behavior, all
streaming transport/lifecycle failure paths, and all durable formats. Existing
test names or reciprocal encoder/decoder agreement are not exhaustive proof.
