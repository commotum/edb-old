//! Full traversals below are independent test oracles, deliberately outside
//! measured append paths. Production bookkeeping must only read root summaries.
use super::*;
use crate::{Attribute, Cardinality, INITIAL_EIDX_FRONTIER, Keyword, Value};
use std::collections::BTreeMap;
use std::time::Instant;

const DATABASE: &str = "recent-stats";
const BASE_T: u64 = 10;
const BASE_HASH: Digest = [0x77; 32];

fn projection(indexed: bool) -> EndpointProjection {
    let mut schema = Schema::new();
    let mut number = Attribute::new(
        1000,
        Keyword::new("item", "number"),
        ValueType::Long,
        Cardinality::One,
    );
    number.indexed = indexed;
    schema.install(number).unwrap();
    let mut reference = Attribute::new(
        1001,
        Keyword::new("item", "parent"),
        ValueType::Ref,
        Cardinality::One,
    );
    reference.indexed = true;
    schema.install(reference).unwrap();
    EndpointProjection::new(schema)
}

fn limits() -> RecentLimits {
    RecentLimits {
        soft_datoms: u64::MAX,
        soft_bytes: u64::MAX,
        hard_datoms: u64::MAX,
        hard_bytes: u64::MAX,
    }
}

fn transaction(t: u64, previous_hash: Digest, datoms: usize) -> DurableTransaction {
    DurableTransaction {
        database_id: DATABASE.into(),
        basis_t: t,
        previous_hash,
        eidx_frontier: INITIAL_EIDX_FRONTIER.max(t * 4 + datoms as u64 + 1),
        tempids: BTreeMap::new(),
        tx_data: (0..datoms)
            .map(|offset| Datom {
                entity: t * 4 + offset as u64,
                attribute: if offset == 1 { 1001 } else { 1000 },
                value: if offset == 1 {
                    Value::Ref(t)
                } else {
                    Value::Long(t as i64)
                },
                tx: t_to_tx(t).unwrap(),
                added: true,
            })
            .collect(),
    }
}

fn empty(projection: &EndpointProjection) -> RecentTier {
    RecentTier::new(
        DATABASE,
        BASE_T,
        BASE_HASH,
        [],
        projection.clone(),
        limits(),
    )
    .unwrap()
}

fn check_stats(tier: &RecentTier) {
    let mut chunks = Vec::new();
    let mut cursor = tier.log.head.as_deref();
    while let Some(chunk) = cursor {
        chunks.push(chunk);
        cursor = chunk.previous.as_deref();
    }
    let mut entries = 0_u64;
    let mut datoms = 0_u64;
    let mut encoded = 0_u64;
    let mut accounted = 0_u64;
    let mut previous = tier.base_hash;
    for chunk in chunks.iter().rev() {
        assert!(!chunk.entries.is_empty() && chunk.entries.len() <= LOG_CHUNK_SIZE);
        if let Some(previous) = chunk.previous.as_ref() {
            assert_eq!(previous.entries.len(), LOG_CHUNK_SIZE);
        }
        for entry in chunk.entries.iter() {
            entries += 1;
            assert_eq!(entry.transaction.previous_hash, previous);
            assert_eq!(entry.transaction.basis_t, tier.base_t + entries);
            let bytes = encode_transaction(&entry.transaction).unwrap();
            assert_eq!(entry.encoded_bytes, bytes.len() as u64);
            datoms += entry.transaction.tx_data.len() as u64;
            encoded += bytes.len() as u64;
            accounted += retained_entry_stats(&entry.transaction)
                .unwrap()
                .accounted_bytes;
            previous = entry.hash;
        }
        assert_eq!(chunk.total_len, entries, "immutable log prefix total");
    }
    let mut tree_entries = 0;
    let mut nodes = 0;
    let mut height = 0;
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        let tree = tier.indexes.get(order);
        let recomputed = tree.recompute_summary();
        assert_eq!(tree.summary(), recomputed);
        tree_entries += recomputed.0;
        nodes += recomputed.1;
        height = height.max(recomputed.2);
    }
    assert_eq!(
        tier.stats,
        RecentStats {
            base_t: tier.base_t,
            end_t: tier.base_t + entries,
            transactions: entries,
            datoms,
            encoded_bytes: encoded,
            accounted_bytes: accounted,
            base_hash: tier.base_hash,
            end_hash: previous,
            needs_consolidation: datoms >= tier.limits.soft_datoms
                || accounted >= tier.limits.soft_bytes,
            raw_index_entries: tree_entries,
            index_nodes: nodes,
            max_index_height: height,
            log_chunks: chunks.len() as u64,
        }
    );
    assert_eq!(tier.work.authenticated_datoms, datoms);
    assert_eq!(tier.work.log_chunks_created, entries);
    assert_eq!(tier.last_work.statistics_root_reads, 5);
}

#[test]
fn immutable_aggregates_match_full_oracles_across_appends_rebuild_and_failure() {
    let projection = projection(true);
    let mut tier = empty(&projection);
    check_stats(&tier);
    let mut retained = Vec::new();
    for count in 1..=513 {
        let before = tier.stats();
        tier = tier
            .append(
                transaction(before.end_t + 1, before.end_hash, (count % 3) as usize),
                projection.clone(),
            )
            .unwrap();
        check_stats(&tier);
        assert_eq!(tier.last_work().statistics_root_reads, 5);
        assert_eq!(tier.last_work().log_chunks_created, 1);
        if matches!(count, 1 | 31 | 32 | 33 | 127 | 256) {
            retained.push(tier.clone());
        }
    }
    for snapshot in &retained {
        check_stats(snapshot);
    }
    let entries = tier.entries();
    let rebuilt = RecentTier::new(
        DATABASE,
        BASE_T,
        BASE_HASH,
        entries
            .iter()
            .map(|entry| entry.transaction.as_ref().clone()),
        projection.clone(),
        limits(),
    )
    .unwrap();
    check_stats(&rebuilt);
    assert_eq!(rebuilt.stats(), tier.stats());
    let covering = &entries[255];
    let adopted = RecentTier::new(
        DATABASE,
        covering.transaction.basis_t,
        covering.hash,
        entries[256..]
            .iter()
            .map(|entry| entry.transaction.as_ref().clone()),
        projection.clone(),
        limits(),
    )
    .unwrap();
    check_stats(&adopted);
    assert_eq!(adopted.stats().transactions, 257);
    assert_eq!(adopted.stats().end_hash, tier.stats().end_hash);

    let before = tier.stats();
    let mut invalid = transaction(before.end_t + 1, before.end_hash, 1);
    invalid.previous_hash = [0; 32];
    assert_eq!(
        tier.append(invalid, projection.clone()).unwrap_err().code,
        "recent/hash-chain-mismatch"
    );
    let invalid = transaction(before.end_t + 2, before.end_hash, 1);
    assert_eq!(
        tier.append(invalid, projection.clone()).unwrap_err().code,
        "recent/noncontiguous-basis"
    );
    // A strict duplicate inside one transaction is still rejected, even after
    // the temporary tree has already acquired its first copy.
    let mut duplicate = transaction(before.end_t + 1, before.end_hash, 1);
    duplicate.tx_data.push(duplicate.tx_data[0].clone());
    assert!(tier.append(duplicate, projection.clone()).is_err());
    assert_eq!(tier.stats(), before);
    check_stats(&tier);
    check_stats(&adopted);

    let tight_limits = RecentLimits {
        soft_datoms: 1,
        hard_datoms: 1,
        ..limits()
    };
    let tight = RecentTier::new(
        DATABASE,
        BASE_T,
        BASE_HASH,
        [transaction(BASE_T + 1, BASE_HASH, 1)],
        projection.clone(),
        tight_limits,
    )
    .unwrap();
    let tight_before = tight.stats();
    let error = tight
        .append(
            transaction(tight_before.end_t + 1, tight_before.end_hash, 1),
            projection.clone(),
        )
        .unwrap_err();
    assert_eq!(error.code, "recent/hard-capacity");
    assert_eq!(tight.stats(), tight_before);
    check_stats(&tight);
    drop(rebuilt);
    drop(tier);
    for snapshot in retained {
        check_stats(&snapshot);
    }
}

#[test]
fn schema_backfill_and_duplicate_insertions_preserve_cached_aggregates() {
    let initial = projection(false);
    let mut tier = empty(&initial);
    for _ in 0..129 {
        let stats = tier.stats();
        tier = tier
            .append(
                transaction(stats.end_t + 1, stats.end_hash, 1),
                initial.clone(),
            )
            .unwrap();
    }
    let before = tier.stats();
    let enabled = tier.extend([], projection(true)).unwrap();
    check_stats(&tier);
    check_stats(&enabled);
    assert_eq!(
        enabled.stats().raw_index_entries,
        before.raw_index_entries + 129
    );
    assert_eq!(enabled.last_work().schema_backfill_datoms, 129);
    let disabled = enabled.extend([], projection(false)).unwrap();
    let reenabled = disabled.extend([], projection(true)).unwrap();
    // Backfill attempts hit already-present keys: do not inflate node counts.
    assert_eq!(reenabled.last_work().schema_backfill_datoms, 129);
    assert_eq!(reenabled.last_work().nodes_copied, 0);
    assert_eq!(reenabled.last_work().aggregate_child_reads, 0);
    assert_eq!(reenabled.stats(), enabled.stats());
    check_stats(&disabled);
    check_stats(&reenabled);
}

#[test]
fn complete_fixed_delta_append_bookkeeping_depends_on_paths_not_retained_tail() {
    let projection = projection(true);
    let mut tier = empty(&projection);
    let mut measurements = Vec::new();
    for target in [32_u64, 128, 512, 2048, 8192] {
        while tier.stats().transactions < target {
            let stats = tier.stats();
            tier = tier
                .append(
                    transaction(stats.end_t + 1, stats.end_hash, 1),
                    projection.clone(),
                )
                .unwrap();
        }
        check_stats(&tier); // Full oracle is intentionally outside the timer.
        let before = tier.stats();
        let tx = transaction(before.end_t + 1, before.end_hash, 1);
        let mut complete_cost = std::time::Duration::ZERO;
        let mut work = None;
        for _ in 0..16 {
            let candidate = tx.clone();
            let projection = projection.clone();
            let started = Instant::now();
            let successor = tier.append(candidate, projection).unwrap();
            let actual = successor.last_work();
            assert_eq!(actual.authenticated_datoms, 1);
            assert_eq!(actual.index_insert_attempts, 3);
            assert_eq!(actual.bulk_rebuild_datoms, 0);
            assert_eq!(actual.schema_attributes_examined, 0);
            assert_eq!(actual.statistics_root_reads, 5);
            assert_eq!(actual.log_chunks_created, 1);
            assert_eq!(actual.log_entries_copied, 0, "full retained tail chunk");
            assert!(actual.node_visits <= 3 * u64::from(before.max_index_height));
            assert!(actual.aggregate_child_reads <= 16 * actual.nodes_copied);
            if let Some(expected) = work {
                assert_eq!(actual, expected);
            }
            work = Some(actual);
            // Complete operation includes stats publication and destruction of
            // all unretained copied paths/chunks, not just insert_node time.
            drop(successor);
            complete_cost += started.elapsed();
        }
        let work = work.unwrap();
        let bookkeeping = work.aggregate_child_reads
            + work.statistics_root_reads
            + work.log_chunks_created
            + work.log_entries_copied;
        measurements.push((target, bookkeeping));
        eprintln!(
            "RECENT_APPEND retained_tx={target} retained_nodes={} log_chunks={} height={} work={work:?} bookkeeping_reads_and_copies={bookkeeping} complete_16_append_stats_drop_us={}",
            before.index_nodes,
            before.log_chunks,
            before.max_index_height,
            complete_cost.as_micros()
        );
        check_stats(&tier);
    }
    assert!(
        measurements.last().unwrap().1 < measurements.first().unwrap().1 * 16,
        "256x retained tail must not require 256x bookkeeping"
    );
}
