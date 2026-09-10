# Goal 11 source contract: schema and idents are information

This contract fixes the minimum Goal 11 design before source migration. It is
not a second schema specification: the authoritative state is the ordinary
datom set. Rust structures below are derived indexes over that information.

## Evidence that controls the design

- Schema entities are open entities and schema is ordinary transaction data:
  `datomic_pro_docs/03_schema/00_schema_data_reference.md:58-68`.
- `:db/ident` names any entity (especially attributes and enum values), is kept
  in memory, and is accepted in E, A, and V positions:
  `03_schema/00_schema_data_reference.md:152-179` and
  `04_transactions/02_transaction_data.md:286-310`.
- Enum entities need only an ident:
  `03_schema/02_data_modeling.md:9-37`.
- Rename is synchronous; old and new names resolve to the same entity, the
  current entity value contains the new name, and a later assertion may
  repurpose the old name: `03_schema/01_changing_schema.md:17-32`.
- A time view uses the schema of the database value's current basis rather
  than rewinding schema: `03_schema/01_changing_schema.md:9-15,34-47`.
- Recovered fixed boot IDs are `BOOT-IDS` at
  `1.0.7705/peer/src-clj/datomic/db.clj:112-157`; the self-describing base
  schema is ordinary E/A/V data at `:7983-8055`.
- `key-hook` derives both lookup directions from ident assertions at
  `db.clj:2550-2568`; `addKeyword` overwrites the keyword and entity maps at
  `:5001-5005`.
- Recovery collects ident **assertions** from current, mid, and history
  indexes, orders them by transaction, and then reruns hooks at
  `db.clj:5302-5364`. This is why rename aliases survive a restart and why a
  later reuse of a name wins.
- Missing schema hooks are synthesized from ordinary schema datoms at
  `db.clj:4430-4462`. `install-attribute-hook` reads the complete proposed
  entity and constructs the typed `Attribute` at `:3123-3196`; on rebuild,
  both install and alter markers run that installation projection at
  `:5328-5364`.
- Primitive/map forms resolve ordinary attributes against db-before in
  `ProcessInpoint`, `db.clj:6525-6607`; hooks see the proposed db-after in
  `Db.addData`, `:4807-4965`.
- Recovered datoms use an `int` A (`src-java/datomic/impl/db/IDatum.java:8-27`)
  and the schema element vector is bounded at 2^20 by
  `db.clj:293-298,4994-5000`.

## Native system vocabulary

Use the recovered numeric IDs where they are fixed. Attribute IDs remain
`u32`, but every installed attribute must be in partition 0 and have an ID at
most 1,048,576. Entity and transaction IDs remain `u64`. Conversion from an
entity ID to an attribute ID is checked once at the schema boundary.

### Fixed recovered core

| ID | Ident | Role |
|---:|---|---|
| 0 | `:db.part/db` | schema/system partition entity and install target |
| 1, 2 | `:db/add`, `:db/retract` | primitive operation idents |
| 3, 4 | `:db.part/tx`, `:db.part/user` | transaction and default user partitions |
| 10 | `:db/ident` | keyword, cardinality one, unique identity/indexed |
| 13 | `:db.install/attribute` | ref, cardinality many |
| 19 | `:db.alter/attribute` | ref, cardinality many |
| 20-25, 27 | `:db.type/ref`, `keyword`, `long`, `string`, `boolean`, `instant`, `bytes` | value-type entities |
| 35, 36 | `:db.cardinality/one`, `many` | cardinality entities |
| 37, 38 | `:db.unique/value`, `identity` | uniqueness entities |
| 40 | `:db/valueType` | ref, cardinality one |
| 41 | `:db/cardinality` | ref, cardinality one |
| 42 | `:db/unique` | ref, cardinality one |
| 43 | `:db/isComponent` | boolean, cardinality one |
| 44 | `:db/index` | boolean, cardinality one |
| 45 | `:db/noHistory` | boolean, cardinality one |
| 50 | `:db/txInstant` | instant, cardinality one, indexed |

### Recovered schema-upgrade IDs

These IDs are not literal in `BOOT-IDS`, but follow deterministically from the
upgrade forms at `db.clj:8240-8301`, system-partition allocation beginning at
the element-vector count (`:6640-6680,6806-6810`), and the transaction bases
shown by `bootstrap-db*` (`:8407-8436`):

| ID | Ident | Role |
|---:|---|---|
| 54, 55 | `:db.fn/retractEntity`, `:db.fn/cas` | general built-in idents |
| 56-62 | `:db.type/uuid`, `double`, `float`, `uri`, `bigint`, `bigdec`, `:db/doc` | six value types; doc is string/one |
| 63-65 | `:db.type/tuple`, `:db.type/symbol`, `:db/tupleType` | tuple/symbol types; homogeneous tuple type metadata |
| 66-71 | `:db/tupleTypes`, `:db/tupleAttrs`, `:db/ensure`, `:db.entity/attrs`, `:db.entity/preds`, `:db.attr/preds` | tuple and predicate metadata |
| 72 | `:db.tuple/discontinued` | irreversible composite stop flag |

The exact derivation is: the t=54 bootstrap transaction allocates system
entities 54-55; t=56 allocates 56-62; the documentation-only transaction is
t=63; t=64 allocates 63-65; t=66 allocates 66-71; and t=72 allocates 72.
Atomic may hard-code this recovered result because compatibility with dynamic
schema-level upgrades is not useful here.

`db/tupleType` is keyword/one. `db/tupleTypes` and `db/tupleAttrs` are
tuple/one with homogeneous keyword slots. `db/ensure` is ref/many;
`db.entity/attrs` is keyword/many; entity and attribute predicate properties
use symbol/many. All supported value-type entities must have ordinary ident
datoms. `:db/fulltext` retains its recovered reserved ID 51 but is not an
installed Atomic attribute until a full-text contract exists; attempts to use
it as schema are explicitly unsupported, not silently ignored. Extensible
JVM value-type/partition/function-install machinery is omitted; persisted
native programs remain a separate Goal 12 correction.

## Genesis and one authority

`canonical_genesis_datoms()` is a protocol constant, analogous to recovered
`BOOT-IDS` plus `bootstrap-data`, not a mutable side-channel schema. It emits a
canonical immutable baseline at logical t=0 (`tx = t_to_tx(0)`):

1. one `:db/ident` assertion for every supported system entity above;
2. the complete definitions of every supported built-in attribute;
3. `(0, :db.install/attribute, attribute-id)` for every installed built-in
   attribute.

Genesis is stored, indexed, queryable, hashed, and backed up like other
information, but is not a user transaction and has no `:db/txInstant`.
Positive transaction chunks remain one per basis t. This deliberately
collapses recovered schema-level upgrade transactions into one native base;
the source itself already separates t=0 bootstrap data from later upgrades at
`db.clj:8375-8436`. Exact Datomic database compatibility is out of scope.

The constructor becomes `Database::new()`/`Database::bootstrap()` with no
caller-supplied `Schema`. A trusted bootstrap routine may break the apparent
self-description cycle only by loading the exact canonical genesis bytes,
deriving caches, and checking that the result describes those bytes. Recovery
rejects a missing, altered, duplicated, or noncanonical genesis. Tests and
applications install all non-system attributes with transactions.

`Schema`, the ident maps, tuple constituent maps, and AVET/VAET membership are
discardable acceleration structures. Canonical transaction, index-base, and
backup formats contain genesis plus ordinary datoms—not encoded `Schema`,
`SchemaChange`, aliases, or `schema_history`. A persisted derived cache is
allowed only as a hash-bound hint which is independently regenerated and
compared before use.

## Deterministic cache derivation

Given canonical genesis plus committed history through one basis:

1. Replay datoms to obtain current E/A/V facts, retaining all history.
2. Select every added datom whose A is 10, including historical assertions;
   order by `(t, canonical within-transaction datom order)`. Fold each
   `(e, :db/ident, kw)` as `ident_to_eid[kw] = e` and
   `eid_to_ident[e] = kw`. Retractions do not erase either mapping; a later
   assertion overwrites it. This mirrors `ident-setting-datoms`, `key-hook`,
   and `addKeyword` exactly.
3. From current facts, union the refs asserted at `(0, 13, _)` and
   `(0, 19, _)`. In ascending entity ID order, read each target's current
   metadata facts and construct an `Attribute`. Require exactly one current
   ident, value type, and cardinality; decode absent booleans as false and
   absent optional properties as none; reject wrong types, unknown enum refs,
   contradictory tuple shapes, and duplicate functional metadata.
4. Validate per-attribute and cross-attribute tuple rules, the schema element
   limit, current cardinality, uniqueness, and every fact's value type.
5. Build schema-dependent AVET/VAET and tuple-constituent indexes. A basic
   EAVT/AEVT/current replay must therefore precede the typed projection.

This algorithm is used by bootstrap, every accepted successor, index-base
load, log recovery, restore, and an explicit test-only cache-drop/rebuild
check. There is one implementation, not a transaction projector plus a
different decoder.

## Transaction and ident behavior

- `TxOp::InstallAttribute(Attribute)` may remain temporarily as convenience
  syntax, but normalization must lower it before assessment to ordinary meta
  datoms plus `(0, 13, eid)`. `AlterAttribute` similarly lowers changed facts
  and the `(0, 19, eid)` hook event. Neither typed object nor `SchemaChange`
  may be persisted or returned as authoritative information.
- As recovered `attrs-missing-hooks` does at `db.clj:4430-4462`, map/schema
  sugar may synthesize the install marker for a new partition-0 schema entity
  or the alter marker for an installed one. Primitive schema retractions need
  the alter marker. Any operative metadata change without the corresponding
  event is rejected so data and the rebuildable cache cannot diverge.
- Ordinary E/A/V resolution and value validation use db-before. Install/alter
  events derive and validate the complete successor after all transaction
  datoms are provisionally applied. A new attribute or enum ident is usable in
  the following transaction, not earlier. The returned db-after exposes the
  new schema and idents immediately.
- Ident resolution is general: `resolve_ident -> Option<u64>`; resolving an A
  additionally requires a checked installed `AttrId`. Ident keywords resolve
  in transaction E/A/ref-V, query constants, pull/entity input, lookup refs,
  and reverse navigation against the exact database value supplied.
- Rename creates the ordinary cardinality-one retraction/assertion pair. Both
  names resolve to the entity, while eid-to-ident and the current entity's
  `:db/ident` yield the new name. Reusing the old keyword on another entity
  later makes that keyword resolve to the later entity, as recovered map
  overwrite does.
- Schema entities remain open. Non-schema application attributes on them are
  ordinary facts ignored by typed derivation.

An active composite constituent ident is the one material conflict: docs say
old idents may generally be repurposed (`changing_schema.md:30-32`), while the
source explicitly rejects retargeting an ident whose old entity is in the
constituent cache (`db.clj:2527-2539`). Preserve the recovered restriction for
active composites so immutable `:db/tupleAttrs` keywords cannot silently name
different attributes on rebuild; lift it only after discontinuation, matching
the source's removal of active constituent links. Record this as a narrow
source-backed qualification of the general documentation.

## Temporal behavior

Historical/as-of/since relations select schema datoms by the view's time just
like all other datoms. Interpretation does not: a view derived from database
value D uses D's current typed schema and ident cache. Thus an as-of view can
show an older `:db/cardinality` assertion while entity/pull interpretation
uses D's newer cardinality. This follows docs `changing_schema.md:13,47` and
the recovered `Db.asOf`/`since`, which only associate time bounds on the same
Db at `db.clj:5139-5140`.

An independently retained old immutable `Database` value still has the schema
of its own basis. Advancing a connection produces a new value; it does not
mutate the old one.

## Implementation slices

1. Add `src/vocabulary.rs`: IDs, checked attribute conversion, canonical
   genesis datoms, and bootstrap verification.
2. Split `Schema` into general ident indexes (`Keyword <-> u64`) plus installed
   `u32 -> Attribute`; add the single history-to-cache derivation routine.
3. Add genesis to `Database`; seed current/history indexes from it; remove
   `schema_history` and the public/durable `SchemaChange` mutation path after
   schema syntax lowering works. The retained `Database::new(schema)` is only
   construction sugar for exact bootstrap plus an ordinary t=1 install
   transaction; it never extends or replaces genesis.
4. Lower schema forms to ordinary operations, collect hook events, derive the
   successor once, and keep db-before resolution plus successor validation.
5. Change canonical encoding and PostgreSQL catalog/base/backup formats to a
   new explicit version whose only schema authority is genesis/datoms. Reject
   v2 mixed-authority payloads rather than guessing a migration.
6. Route transaction, query, pull, entity, peer, recovery, inspection, and
   operations code through general ident resolution.

## Source-backed executable witnesses

- `bootstrap_is_queryable_and_self_describing`: EAVT/query sees representative
  ident, meta-schema, value-type, and install-marker genesis datoms.
- `cache_drop_rebuild_is_exact`: erase all derived maps/attributes/index
  membership, rebuild from genesis+history, and compare every observable
  result and canonical byte/hash.
- `malformed_genesis_and_schema_information_fail_closed`: mutate a boot fact,
  omit required metadata, introduce conflicting functional metadata, exceed
  the schema bound, or point a marker outside partition 0.
- `general_enum_ident_works_in_e_a_and_ref_v_after_installing_tx`: install a
  ref attribute and enum ident, then use the enum ident in the next tx and in
  query/pull/entity input; unknown/new-same-tx names fail against db-before.
- `rename_alias_repurpose_and_restart`: old+new resolve after rename, current
  ident is new, a later reuse redirects old, and recovery/cache rebuild yields
  identical maps.
- `schema_is_ordinary_open_history`: install and alter through ordinary forms,
  query custom facts on the attribute entity, and observe exact schema datom
  assertions/retractions in history and time views.
- `schema_hook_timing_and_successor_validation`: new attributes are unusable in
  their install tx; valid alterations are immediately operative in db-after;
  an invalid complete successor aborts without durable publication.
- `time_views_use_current_basis_schema`: historical schema datoms rewind but
  cardinality/ident interpretation uses the current database value's cache;
  an older retained database value remains unchanged.
- `canonical_restart_contains_no_schema_side_channel`: PostgreSQL restart,
  index-base recovery, backup/restore, and tail replay reproduce caches using
  only authoritative datoms; obsolete v2 `SchemaChange` payloads fail with a
  structured unsupported-version error.
