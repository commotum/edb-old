# Partitions and time-local UUIDs

Partitions organize entity IDs for index locality. They are not shards, separate
PostgreSQL databases, tenant authorization boundaries, or independent transaction
streams. Cross-partition references, queries, history and atomic transactions use
the same database. Choose a grouping that matches actual reads and measure its
effect; assigning partitions alone does not establish a performance improvement.

The semantic references are the local [partition chapter](../datomic_pro_docs/04_transactions/07_partitions.md)
and [identity/UUID guidance](../datomic_pro_docs/03_schema/03_identity_and_uniqueness.md).
Recovered `peer/src-clj/datomic/db.clj` supplies map expansion and assignment
evidence; `common.clj` supplies the squuid layout. Native authoring uses Rust
enums, not a JVM object or EDN wire compatibility layer.

## Identity and installation

`eid_to_part(entity)` returns the raw partition bits. `partition_eid(entity)`
returns the partition's entity ID; these differ for implicit partitions.
`implicit_part(number)` returns an implicit partition entity ID without a
transaction; `implicit_part_id(partition)` decodes it. Numbers are in
`0..524_288`. The upper half of the 20-bit partition field is implicit; named
partitions occupy installed codes in the lower half.

The existing 42-bit entity-index field and its sign-bit restriction remain
unchanged. Allocation uses one global issuance frontier, not a counter per
partition. Explicit IDs still need a valid partition and an issued entity index;
constructing an integer with `make_eid` does not reserve or issue it. The default
is `:db.part/user`. Schema/partition installation has system affinity to
`:db.part/db`; transaction time uses `:db.part/tx`.
The Datomic transactor's default-partition configuration property is not mapped
to a native runtime setting; use explicit force/match policy for custom defaults.
Because named partition IDs themselves must fit the lower half of the partition
field, allocate named partition entities before the global frontier reaches
524,288. Already installed named partitions continue to allocate entities after
that point; implicit partitions do not require installation.

Install a named partition as ordinary data, then use it in a later transaction
(policy resolves against db-before):

```rust
use atomic_core::*;

let install = TxForm::EntityMap(EntityMap {
    id: Some(EntityRef::Temp("partition".into())),
    attributes: vec![
        (AttributeRef::Ident(Keyword::new("db", "ident")),
         MapValue::Value(Value::Keyword(Keyword::new("app.part", "orders")).into())),
        (AttributeRef::ReverseIdent(Keyword::new("db.install", "partition")),
         MapValue::Value(Value::Keyword(Keyword::new("db.part", "db")).into())),
    ],
});
```

`AttributeRef::Ident(Keyword::new("db.install", "_partition"))` is also accepted
for this reverse installation spelling. It lowers to ordinary installation
facts, not a hidden schema descriptor. `db.schema().partitions()` lists installed
names. An installed partition cannot be removed while retaining its allocated
identity space. No UUID or user entity is silently migrated to another partition.

New databases include `:db.install/partition`. Old supported genesis profiles
remain readable with their original bytes and commitments. Upgrade an old
database explicitly with `partition_vocabulary_upgrade_ops()` as its own ordinary
transaction, with a durable request key. Do not rerun the upgrade with a new key
on an already upgraded database; retry the original key after an unknown outcome.
Do not rewrite genesis or treat an absent attribute as already installed.

## Transaction allocation policy

Include directives alongside actual entity assertions:

```rust
let placement = vec![
    TxOp::ForcePartition {
        tempid: "order".into(),
        partition: EntityRef::Ident(Keyword::new("app.part", "orders")),
    },
    TxOp::MatchPartition {
        tempid: "line".into(),
        entity: EntityRef::Temp("order".into()),
    },
];
```

These directives are transaction policy, never stored datoms. They do not create
entities by themselves: their target tempids need ordinary entity-position facts.
Force accepts an installed partition identity or an implicit partition ID.
Match accepts an existing entity or another transaction-local tempid. Force wins
over affinity; system installation affinity remains sticky. Duplicate equivalent
directives coalesce; contradictory directives within one policy kind reject rather than pick a winner
from input order. Unanchored affinity cycles reject.

Nested component maps automatically emit child-to-parent affinity, including
anonymous child tempids. A primitive component-reference assertion alone does not
add this map-authoring policy. An explicit match conflicting with component
affinity also rejects. Direct matching to an existing reserved db/tx entity does
not relocate ordinary data into those reserved partitions; temporary targets
carry their effective transaction-local policy. Uniqueness/upsert may resolve a
tempid to an existing entity: that identity and its partition remain unchanged.
Matching another tempid follows that tempid's original allocation policy, even
if it upserts to an existing entity elsewhere. Matching that existing entity via
its ID or lookup ref instead follows the existing partition.
When matching another tempid, its requested allocation policy is followed; an
upsert does not retroactively turn that policy into a relocation operation.

Speculation applies the same policy but reserves nothing. Submit replayable
logical intent, not speculative entity IDs. Named partitions do not remove the
database's finite ID-space and serialized-write constraints.

Persisted transaction programs can use `Instruction::EmitForcePartition` and
`EmitMatchPartition`, consuming `[tempid, partition/entity]` from the stack. A
tempid can be a native Temp reference or nonempty string; qualified keywords can
name partitions. These instructions obey the shared fuel, emitted-form and byte
budgets. Only programs containing them select ABI 8; existing ABI 4–7 content
retains its old encoding. Explicit directive forms select native submission wire
version 3 and request-identity grammar 4; old forms keep their prior bytes.

## UUID helpers

`squuid()` and `uuid_v7()` return `Result<u128, SemanticError>`; store the result
as `Value::Uuid`. Their `_at(unix_millis)` variants accept an explicit clock but
still obtain fresh OS randomness. There is no deterministic name-to-UUID function
or request-key lookup hidden inside these generators.

Squuids have 32 leading bits of Unix seconds, UUID version 4, the standard variant,
and 90 random bits. `squuid_time_millis` reads that field at second precision; it
cannot prove that an arbitrary UUID was generated as a squuid. Times past the
32-bit seconds range reject rather than wrap.

UUIDv7 uses 48 leading Unix-millisecond bits, version 7, the standard variant and
74 random bits, following [RFC 9562 section 5.7](https://www.rfc-editor.org/rfc/rfc9562.html#section-5.7).
`uuid_v7_time_millis` checks the version and variant; out-of-range clocks reject.
The native generator has no monotonic counter: same-tick values have random
order, and clock regression can produce earlier timestamps.

Both expose creation-time information and are not credentials or authenticated
provenance. They are not transaction order, entity allocation order, or substitutes
for transaction `t`. The existing UUID index comparator compares signed 64-bit
halves; it is deliberately unchanged for compatibility. Time-local prefixes help
group nearby times, but unsigned byte order and native index order differ at a
high-half sign boundary. No universal latency or throughput improvement is claimed.

Generate an intent once and retain its UUIDs across every retry, preferably in an
application outbox. The owned [application example](../examples/application_workflow.rs)
stores both UUID kinds under one stable tenant key and, on restart, reconstructs
the same request from those committed values. It rejects missing committed UUID
fields instead of regenerating them. This demonstration assumes those fixture
facts are not independently removed or rewritten; it is not a general outbox.

## Verification

[Partition tests](../tests/partitions.rs) cover allocation, identity and bounded
native locality workloads. [Authoring tests](../tests/partition_authoring.rs)
cover version selection, stored-program speculation/commit, receipt retry and
restart replay. Existing encoding/program goldens remain regression gates.

The separate-process [product acceptance](../tests/product_cli.rs) exercises the
application using restricted PostgreSQL roles, a native Unix-socket transactor,
named tenant placement, automatic component affinity, both UUID kinds and exact
report/reference reopening. Original project calculations and planning request
identities are unchanged. The added schema, partition and tenant transactions
extend that owned workflow from basis 6 to basis 9; a rerun replays those receipts.
