# Development trace — values, datoms, schema and identity foundations

This companion is separate from the preserved Pro text. It details the passages
below; the [passage ledger](../../development/source/passages.tsv) accounts for
the surrounding schema/value families without claiming every symbol is annotated. Source coordinates
refer to unannotated revision `cd7192e63d883a4a34aa7de4d5bcd17e6edb692d`; use the
named symbols after inline comments shift line numbers. Rust paths describe the
current component owners. Existing tests were inspected; the original study ran only
the new URI mechanism's three standalone unit tests. Integrated acceptance is
owned by the parent task; no older or PostgreSQL test is claimed fresh here.

The recovered corpus puts `Datum`, ID packing, ident hooks and schema descriptors
inside `datomic/db.clj`; there are no separate `datomic/datom`, `dbid`, `ids` or
`schema` Clojure files here. Java interfaces expose that implementation, not an
additional missing Rust object model. The peer/transactor Java interfaces below
are baseline-identical. Their `common.clj` comparison sections through `cl` are
also identical, but the complete namespaces are not.

## Immutable facts and transaction coordinates

Passages: [Datomic Data Model](../02_core_concepts/00_datomic_data_model.md),
the datom E/A/V/Tx/Op description and immutable, point-in-time database paragraphs;
[Identity and Uniqueness](03_identity_and_uniqueness.md), “Entities,” the permanent
entity-ID and encoded-partition paragraphs.

Source: [transactor `datomic/Datom.java`](../../1.0.7705/transactor/src-java/datomic/Datom.java),
public five-position fact; [transactor `impl/db/IDatum.java`](../../1.0.7705/transactor/src-java/datomic/impl/db/IDatum.java),
primitive `getT`/`getTx`/`getP`; [transactor `db.clj`](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`Datum` (baseline 720), `make-eid` (350), `eid->part` (363), `eid->eidx` (373),
`partition-eid` (386), and `eavt-cmp`/`avet-cmp` (after 883).
`Datum` packs logical time and assertion state in `tOp`, reconstructs transaction
entity IDs in partition 3, and derives the entity partition from the entity ID.
The index comparators group logical keys before descending time and assertion
before retraction. Public tuple shape alone does not specify this order.

Rust owners: [model/datom.rs](../../src/model/datom.rs), `Datom::cmp_in`, and
[model/identity.rs](../../src/model/identity.rs), `make_eid`, `eid_to_part`, `eid_to_eidx`,
`partition_eid`, `t_to_tx`, `tx_to_t`. Retain these typed fields and checked ID
construction: they preserve the coordinate distinction without reproducing JVM
boxing or exposing negative packed tempids. The 42-bit entity-index field includes
a source sign bit; Rust deliberately accepts only its permanent nonnegative half.

Independent assertions: [identity_repair.rs](../../tests/identity_repair.rs),
`transaction_time_and_user_entities_have_disjoint_recovered_ids` and
`explicit_ids_must_be_issued_but_empty_issued_ids_can_be_reused`, assert exact
partition/frontier behavior and transaction metadata; [model/datom.rs](../../src/model/datom.rs),
`transaction_sorts_descending_and_assertion_first`, checks ordering directly.
[partitions.rs](../../tests/partitions.rs),
`implicit_partition_boundaries_round_trip_without_allocating_entities`, checks
the zero and upper implicit-partition boundaries. Full partition-affinity source
resolution was not re-audited in this pass; merely reading Rust's iterative
`PartitionPolicy::resolve_partition` is not proof of its full source equivalence.

## Value equality is shared by indexes and identities

Passages: [Schema Data Reference](00_schema_data_reference.md), `:db/valueType`
table including bytes, bigdec and URI; [Identity and Uniqueness](03_identity_and_uniqueness.md),
“Unique Identities” indexed-value and upsert rules, and “Lookup Refs.”

Source: [transactor `common.clj`](../../1.0.7705/transactor/src-clj/datomic/common.clj),
`compare-byte-arrays` (63), `compare-ex` (92), `compare` (123),
`equals-with-strict-scale` (157), `cl` (174). Actual consumers include
[transactor `db.clj`](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`Datum.equals`, `eavt-cmp`, `avet-cmp`, `unique-violator` (3233), and redundancy
checks in `Accrual` (4856) and the prefetch path (7303). These are not unused
general-purpose comparison helpers: equality determines grouping and identity.

Rust owners: [model/value/mod.rs](../../src/model/value/mod.rs), `Value::index_cmp`, `logical_hash`,
`stored_eq`, `stored_cmp`; [model/datom.rs](../../src/model/datom.rs), `Datom::cmp_in`;
[transaction/assess/mod.rs](../../src/transaction/assess/mod.rs), `Reader::lookup`,
`validate_unique_successor`, `group_unique_deltas`;
[database_value/resolve.rs](../../src/database_value/resolve.rs), `DatabaseValue::lookup_with_control`.
Retain length-first signed-byte ordering, UTF-16 string ordering, and separate
logical versus top-level BigDecimal-scale equality. Do not generalize scale
sensitivity into tuple elements: the source recurses through logical comparison.
Rust's explicit nonnumeric type rank replaces JVM class-name order; this is an
observed adaptation, not evidence that every cross-type ordering consumer has
been reconciled. Numeric corner cases and function-value comparison remain
outside this bounded audit.

Independent assertions: [model/value/mod.rs](../../src/model/value/mod.rs),
`byte_order_matches_recovered_length_then_signed_bytes`,
`strings_use_java_compatible_utf16_order`, `big_decimal_storage_equality_preserves_scale`;
[stored_value_repair.rs](../../tests/stored_value_repair.rs),
`cardinality_many_retains_storage_distinct_equal_magnitudes`, asserts exact scales
in both input orders. Its `scale_distinctions_survive_postgres_log_base_and_peer_recovery`
requires `ATOMIC_POSTGRES_URL` and returns early when unset; its existence is not
fresh durable-path verification.

### Demonstrated URI gap and bounded native repair

The schema table maps URI to `java.net.URI`; `common/compare-ex` delegates to that
class. At discovery, Rust `Value::Uri(String)` compared raw UTF-16 text and hashed
a raw string. [edn_value.rs](../../src/edn_value.rs), `edn_to_value`'s `#atomic/uri`
branch, retained input without URI parsing; generic transaction admission,
`Schema::validate_value` and `validate_tuple_slot` had no URI syntax check. Thus
`HTTP://EXAMPLE.COM/x` and `http://example.com/x` are different Atomic keys where
the recovered source treats them as one. The lookup/uniqueness consumers above
make this a domain-identity discrepancy, not only a wire-format difference.

Primary platform contract, consulted 2026-09-11:
[URI.equals(Object)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#equals(java.lang.Object)),
[URI.hashCode()](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#hashCode()),
[URI.compareTo(URI)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#compareTo(java.net.URI)),
[URI(String)](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#%3Cinit%3E(java.lang.String)).

The contracts distinguish opaque from hierarchical URIs. Schemes and server
hosts compare case-insensitively; other raw components preserve case except
hexadecimal escape digits. Server authorities compare user info, host and numeric
port; registry authorities compare their raw text. Absent components sort before
present ones. Hierarchical paths, queries and fragments are separate coordinates;
opaque values compare scheme-specific part then fragment. Hashes must agree with
equality. Parsing can fall back to registry authority where server authority is
not valid. These are component rules, not URL fetching or path normalization.
See also [parseServerAuthority](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#parseServerAuthority())
and [toString](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/net/URI.html#toString()).

Repair boundary: [model/uri.rs](../../src/model/uri.rs) now preserves original
`Value::Uri(String)` spelling while a borrowed native component view supplies
validation, comparison and logical hashing. Admission callers choose their own
error policy; the comparison has a separate, deterministic invalid-string domain
for unchecked public enum construction. Do not add a raw-spelling tie-break to
`stored_eq`/`stored_cmp`: source strict storage equality adds only top-level decimal
scale, so URI aliases must remain redundant facts. Ordinary raw bytes in durable
receipts still preserve exactly what was submitted. No implicit dot-segment
removal, percent decoding, default-port removal, DNS lookup or Unicode host
conversion is introduced. [transaction/canonical.rs](../../src/transaction/canonical.rs),
`compare_value`, retains its raw-URI tie-break for exact submitted representation,
unlike stored-fact or domain-key equality.

One intentional order adaptation is necessary. The primary
[OpenJDK 17 URI implementation](https://github.com/openjdk/jdk17u/blob/master/src/java.base/share/classes/java/net/URI.java),
`compareTo` (lines 1600–1616 at inspection), uses raw authority when either
authority is registry-based. From that rule, `http://A` and `http://a` are equal
server values yet compare on opposite sides of registry value `http://Z_`.
This is an inferred counterexample, not a JVM execution result. Native comparison
therefore ranks absent, server and registry authority kinds, then compares their
components. It preserves URI equality and within-kind ordering while satisfying
the transitivity and equivalence-substitution laws required by ordered keys.
The unit test exercises that counterexample and the full cross-product laws.

Independent regression fixtures needed for integrated acceptance: scheme/host and percent-hex aliases;
case-sensitive path/query/fragment and registry authority; opaque versus
hierarchical values; missing versus empty components; numeric ports; invalid
escapes and syntax; equal logical hashes; URI tuple slots; unique-identity upsert,
unique-value conflict, equivalent-value retraction and lookup through AVET.
Extend [edn_values.rs](../../tests/edn_values.rs),
`stored_types_roundtrip_without_rewriting_representations`, with spelling pairs
and a durable transaction/index/reopen test that checks retained spelling and
the same identity. [uri_values.rs](../../tests/uri_values.rs) is the independently
owned Stage 2 regression target; results must come from the integration run.

## Idents and schema are derived from information

Passages: [Identity and Uniqueness](03_identity_and_uniqueness.md), “Idents,”
the in-memory availability paragraph and E/A/V usage list;
[Changing Schema](01_changing_schema.md), “Changing a:db/ident,” the rename,
retained old alias and repurposing paragraphs.

Source: [transactor `db.clj`](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`ident-setting-datoms` (5302) selects assertions in transaction order;
`run-hooks` (5328) folds them through `key-hook` (2550).
`prevent-ident-retarget!` (2527) additionally protects active composite constituents.
Retractions do not remove historical names from the lookup dictionary.

Rust owners: [model/idents.rs](../../src/model/idents.rs), `IdentIndex::derive` and
`apply_assertion`; [model/schema.rs](../../src/model/schema.rs),
`derive_from_information_with_work` and `derive_from_entity_information`;
[database.rs](../../src/database.rs), `rebuild_derived_caches`, and
[transaction/assess/mod.rs](../../src/transaction/assess/mod.rs)'s successor-schema derivation.
Retain discardable dictionaries, historical assertion folding and schema projection
from ordinary datoms. They preserve the source's fast name lookup without making
Rust's descriptor maps a second authoritative database.

Independent assertions: [schema_information_repair.rs](../../tests/schema_information_repair.rs),
`typed_schema_and_ident_caches_rebuild_from_information_alone`,
`general_ident_rename_alias_and_repurpose_follow_assertion_history` (asserts the
old/new/reused names and rebuilt result), and
`active_composite_constituent_ident_cannot_be_retargeted_until_discontinued`.
[ident_query_pull.rs](../../tests/ident_query_pull.rs) provides downstream query
and Pull alias cases; the complete query/Pull implementations were not audited here.

## Schema configuration is not physical index readiness

Passages: [Changing Schema](01_changing_schema.md), opening current-basis schema
paragraph, “Adding an AVET index to an attribute,” and “Changing a:db/unique”;
[Identity and Uniqueness](03_identity_and_uniqueness.md), identity indexing rule.

Source: [transactor `Attribute.java`](../../1.0.7705/transactor/src-java/datomic/Attribute.java),
`isIndexed` versus `hasAVET`; [transactor `db.clj`](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`Attribute.hasAVET` (1023), `AttrInfo` (1583), `create-attribute` (3008),
`unique-violator` (3233). Availability requires `storageHasAVET` and `needsAVET`.
The public descriptor represents installed facts; it does not make background
physical index work synchronous.

The actual schema-change path is `add-unique` (baseline 3413): reject bytes;
when AVET exists, scan adjacent current values for conflicts; without AVET, allow
the immediate change only if `has-values?` (3382) finds no datoms in the history
AEVT slice. “No current values” is not the same as “nothing to backfill.”

Rust owners: [model/schema.rs](../../src/model/schema.rs), `Attribute.indexed` and derived
unique membership; [transaction/assess/mod.rs](../../src/transaction/assess/mod.rs),
`validate_unique_successor` consults `physical_avet_ready` before choosing AVET.
Retain the distinction rather than collapsing these facts into one boolean.
The public [database_value/resolve.rs](../../src/database_value/resolve.rs), `has_avet`, reports
captured readiness without waiting or refreshing; `resolve_ready_avet_attribute`
rejects an unready raw AVET range. Eager values have complete in-memory coverage;
block values consult snapshot coverage; transaction overlays track newly enabled
backfill separately. These are implementations of readiness, not extra schema
attributes to persist as user information.
Independent assertions: [schema_information_repair.rs](../../tests/schema_information_repair.rs),
`explicit_index_fact_is_distinct_from_unique_derived_avet_membership`;
[avet_schema_semantics.rs](../../tests/avet_schema_semantics.rs),
`eager_unique_enablement_uses_history_not_only_current_values` and the
PostgreSQL-dependent `postgres_writer_rejects_logical_but_unready_avet_until_publication`.
[schema_temporal_repair.rs](../../tests/schema_temporal_repair.rs) asserts that a
time view uses its creating database value's schema while a held older value
keeps its own schema. The full temporal-view source path remains unreviewed here.

## Bytes uniqueness — resolved specific documentation exception

The identity chapter's broad “any value type” uniqueness sentence must be read
with [Schema Data Reference / Legacy / Limitations of Bytes](00_schema_data_reference.md#limitations-of-bytes)
(reference lines 685–693), which explicitly disallows unique bytes and lookup by
bytes. This specific exception agrees with `db/install-attribute-errors` (baseline
2870), `add-unique` (3413), and Rust `Schema::validate_attribute_with_work`.
**Retain the rejection.** An earlier version of this development trace called
the two paragraphs an unresolved conflict; the explicit exception resolves the
decision. Native byte-content ordering does not itself authorize expanding the
documented uniqueness domain.

## Tuple size boundaries — demonstrated differences requiring repair

Passages: [Schema Data Reference](00_schema_data_reference.md), “Tuples,” the
256-character string limit; “Notes on Value Types,” the scalar BigInteger bit
length of 8192 and BigDecimal precision of 1024.

Source: [transactor `db.clj`](../../1.0.7705/transactor/src-clj/datomic/db.clj),
`TupleElem` extensions (baseline 6287–6302), `validated-tuple` (6406), and its
`validated-v-for-attr` caller. Assertions run `tuple-elem?` before tuple-shape
validation. String `.length` counts UTF-16 units: 128 astral characters fit, 129
do not. BigInteger `.bitLength` uses minimal signed representation without a sign
bit, rather than magnitude width: `-2^256` fits; `+2^256` and `-2^256 - 1` do not.
The latter follows the primary
[BigInteger.bitLength contract](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/math/BigInteger.html#bitLength()),
not an executed JVM comparison.

At discovery, [model/schema.rs](../../src/model/schema.rs), `validate_tuple_slot`, used
`chars().count()` and `BigInt::bits()`. The latter delegates to unsigned magnitude
in the locally installed num-bigint implementation. Consequently Rust accepted
129 astral characters and rejected the valid negative power-of-two boundary.
The parent task repaired the shared schema helpers to count UTF-16 units and use
signed bit length, without replacing the numeric engine. The independent
[tuple_schema_repair.rs](../../tests/tuple_schema_repair.rs),
`tuple_and_scalar_boundaries_use_source_string_and_signed_integer_units`, checks
immediately inside/outside the boundaries. No fresh test result for that repair
is claimed by this trace.

Scalar 8192-bit disposition has weaker source evidence: the Pro paragraph names
bit length and maps the type to Java BigInteger, but a search of both recovered
Java/Clojure artifact trees found only the tuple `.bitLength` check, not the scalar
8192 guard. Applying the same signed meaning to the scalar limit is doc-driven
consistency, not proof that an unseen scalar implementation was read.
`validated-tuple` also applies slot/shape checks only to assertions; the complete
Rust non-asserting tuple validation policy has not been reconciled in this pass.

## Related families and evidence limits

The [Partitions](../04_transactions/07_partitions.md) paragraphs on implicit and
named partitions, forced/matched placement, defaults and new-entity scans map to
`db/{implicit-part,implicit-part-id,partition-eid,partbits,process-force-partition,process-match-partition}`,
`AssignPartitions` and `Db.entidAt`. Current pure coordinates live in
[model/identity.rs](../../src/model/identity.rs); transaction-local placement and
explicit defaults live in [partitions.rs](../../src/partitions.rs), consumed by
shared expansion/assessment. Existing checks are
[partitions.rs](../../tests/partitions.rs),
[partition_authoring.rs](../../tests/partition_authoring.rs) and
[default_partition.rs](../../tests/default_partition.rs), including upsert,
affinity, restart and exact retry. Locality/sharding advice is an application
optimization, not a new ownership boundary or a measured native speedup.

API `squuid`/`squuid-time-millis` map to `common/squuid` and `squuid-time-ms`,
which put seconds in the high 32 bits of a random UUID and recover that field.
Native [uuid.rs](../../src/uuid.rs) retains this explicit layout with checked
time range and native UUID ordering; `squuid_layout_precision_boundaries_and_native_order_are_explicit`
is the focused check. UUIDv7 is a separate native capability, not attributed to
the recovered source or equated with semi-sequential UUIDs.

The original foundation pass did not audit every numeric edge, tuple/partition
lifecycle, lookup-ref position or execution engine. Current family dispositions
are in the [source](../../development/source/coverage.tsv) and
[passage](../../development/source/passages.tsv) ledgers. Exact arithmetic and
query binding behavior are traced in the
[query companion](../05_query_and_pull/02_query_reference.atomic.md), navigation
in the [Pull companion](../05_query_and_pull/03_pull.atomic.md), assessment and
predicates in the [processing companion](../04_transactions/03_processing_transactions.atomic.md),
and retention/alteration in the [storage](../08_operations/00_architecture_and_storage/00_storage_services.atomic.md)
and [indexing](../08_operations/01_capacity_and_reliability/00_capacity_planning.atomic.md)
companions. This closes family accounting, not exhaustive input-space or
whole-namespace equivalence; source-only host allocation/hash quirks remain
distinct from the documented native identity and exact-value contracts.
