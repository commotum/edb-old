# Identity representation decision

This repair follows the recovered `datomic.db` representation in 1.0.7705:

- `make-eid` places partition bits above a 42-bit entity-index component
  (`src-clj/datomic/db.clj`, `make-eid`, lines 350–361).
- `eid->part` recovers the partition from those high bits (lines 363–371).
- `eid->eidx` sign-extends bit 41 (lines 373–384). Consequently the native
  permanent-ID API accepts only non-negative indexes through `2^41 - 1`; the
  remaining 42-bit patterns belong to the recovered signed/tempid space.
- `t->tx` is `make-eid` with partition 3
  (`src-clj/datomic/peer.clj`, lines 2144–2152).
- `get-ids` rejects an explicit permanent entity whose recovered index is at
  or beyond the database's issued frontier, while allowing an older issued ID
  even when it currently has no facts (`src-clj/datomic/db.clj`, lines
  6797–6805).

The public documentation corroborates the semantic boundary: entity IDs use
high partition bits (`datomic_pro_docs/04_transactions/02_transaction_data.md`,
lines 233–241), and the built-in system, transaction, and default application
partitions are `:db.part/db`, `:db.part/tx`, and `:db.part/user`
(`datomic_pro_docs/04_transactions/07_partitions.md`, lines 32–44).

## Deliberate native deviation

Recovered Datomic uses one novelty clock: the current `nextT` becomes the
transaction entity index, `get-ids` starts fresh allocation after it, and
`Db.addData` advances `nextT` past every encountered entity index. Transaction
`t` values can therefore contain gaps.

Atomic retains a contiguous logical PostgreSQL log basis `t` and maintains a
separate exclusive non-negative entity-index issuance frontier. A transaction
datom stores `t->tx(t)`, never raw `t`; fresh application entities are allocated
in partition 4 at the current exclusive frontier; and committing a transaction
advances the frontier past both that transaction's `t` and all allocations.
This preserves disjoint identities, ordering, checked exhaustion, and the
recovered issued-ID rule without coupling PostgreSQL log row contiguity to the
number of tempids in a request. Existing byte compatibility is intentionally
rejected by canonical encoding version 2.
