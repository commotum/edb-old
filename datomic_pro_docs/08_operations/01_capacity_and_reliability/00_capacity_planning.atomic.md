# Atomic implementation trace — indexing capacity

Development companion to [Capacity Planning](00_capacity_planning.md), focused
on transactor memory and indexing. This does not claim equivalent JVM memory
sizes, cloud instance recommendations or process-wide multi-database scheduling.

## Frozen work remains charged until publication

Passages: “Transactor Memory,” indexing thresholds, and multiple-database memory
sharing. Recovered [indexer.clj](../../../1.0.7705/transactor/src-clj/datomic/indexer.clj)::
`IndexerImpl` tracks live `:memidx`, frozen `:indexing`, and total usage.
`indexing-started` transfers the current database's live charge to frozen work
without subtracting it from the total. Later commits accrue a new live charge.
`indexing-completed` subtracts only the frozen charge. Resetting all memory
accounting at job start or completion would hide either retained job input or
new arrivals and defeat the admission bound.

The actual caller [update.clj](../../../1.0.7705/transactor/src-clj/datomic/update.clj)::
`process-request-index` acquires the indexing permit, marks indexing started and
freezes the database before dispatching work. `process-new-index` adopts the
result and reports completion before considering more indexing. This is an
ownership handoff, not merely a timer scheduling a scan.

Native [transactor/indexing.rs](../../../src/transactor/indexing.rs)::
`note_commit`, `begin_job`, and `publish_completed` retain novelty by basis.
Worker input is frozen, newer pending bases remain charged, and completion
removes only the covered prefix. `force_publication` preserves a covering
request when exact recent admission reaches its hard cap; it must survive an
older job's completion. [transactor/index_lane.rs](../../../src/transactor/index_lane.rs)
prepares candidates, while [storage/index_publication.rs](../../../src/storage/index_publication.rs)
checks current lineage and guards adoption without discarding newer log data
or receipts. These are distinct worker, admission and canonical-publication
responsibilities.

## Thresholds do not prove cross-database fairness

Source `queue-index-jobs` reacts above half the process-wide maximum and selects
the database with the largest frozen-plus-live charge. `queue-db-index-job`
separately applies a per-database threshold. Native services use explicit
individual worker, novelty and admission policies. Preserving frozen/new-arrival
accounting does not establish the same global scheduler or a fairness guarantee
across native services. The absence of a JVM memory-pool type is not a missing
database semantic contract.

For reclamation costs and conservative retirement cutoffs, see the
[storage companion](../00_architecture_and_storage/00_storage_services.atomic.md#atomic-note-lifecycle-reclamation-boundary).
For log-excision rebasing versus ordinary index adoption, see the
[excision companion](../../09_optional/02_specialized_operations/02_excision.atomic.md#index-publication-log-rewriting-and-adoption-are-distinct).
Independent native checks include
[block_service.rs](../../../tests/block_service.rs) and the
[transactor unit tests](../../../src/transactor/tests.rs).
Those checks require actual configured PostgreSQL to establish runtime behavior;
this source comparison alone is not a throughput or peak-memory measurement.
