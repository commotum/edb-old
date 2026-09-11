//! Cache ownership, eviction and complete bookkeeping-path cost samples.
//!
//! Timings include the actual mutex, lookup, recency update, returned Arc and
//! drop. These isolate bookkeeping, not query/I/O or total process memory.
//! No wall-time ratio is asserted. Collection-level tests independently check
//! slot reuse and linked order against a simple sequence-based oracle.
use super::*;
use crate::Value;
use std::hint::black_box;

const HITS: u64 = 512;
const NODE_WEIGHT: usize = 256;

fn key(ordinal: usize) -> Digest {
    let mut key = [0; 32];
    key[..8].copy_from_slice(&(ordinal as u64).to_le_bytes());
    key
}

fn node(ordinal: usize) -> Arc<TreeNode> {
    Arc::new(TreeNode::Leaf(LeafSegment {
        order: IndexOrder::Eavt,
        history: false,
        entities: vec![ordinal as u64 + 1],
        attributes: vec![1_000],
        values: vec![Value::Long(7)],
        transactions: vec![crate::t_to_tx(1).unwrap()],
        assertions: vec![true],
    }))
}

fn assert_accounting(cache: &TreeNodeCache) {
    let state = lock(&cache.state);
    assert_eq!(state.stats.current_entries, state.entries.len());
    assert_eq!(
        state.stats.current_bytes,
        state.entries.values().map(|entry| entry.bytes).sum()
    );
    assert!(state.entries.len() <= cache.max_entries);
    assert!(state.stats.current_bytes <= cache.max_bytes);
}

fn measure_hot_hit(cache: &TreeNodeCache, hot: Digest) -> (u128, u128) {
    // Keep one real decoded node alive to verify that returned values share the
    // immutable allocation. Synthetic unique keys and fixed admission weights
    // isolate cache bookkeeping; this is not an authentication/SQL fixture.
    let expected = cache.peek(&hot).unwrap();
    assert_accounting(cache);
    let before = cache.stats();
    let started = Instant::now();
    for _ in 0..HITS {
        let value = black_box(cache.get(black_box(&hot)).unwrap());
        assert!(Arc::ptr_eq(&expected, &value));
        drop(value);
    }
    let hit_elapsed = started.elapsed().as_nanos() / u128::from(HITS);
    let after = cache.stats();
    assert_eq!(after.hits - before.hits, HITS);
    assert_eq!(after.misses, before.misses);
    assert_eq!(after.evictions, before.evictions);
    assert_eq!(after.current_entries, before.current_entries);
    assert_eq!(after.current_bytes, before.current_bytes);
    assert_accounting(cache);

    // The test-only inspection takes the same mutex/map/Arc path but
    // intentionally omits recency and stats. It is only a diagnostic comparator,
    // not a recommendation to change hit semantics or an acceptance threshold.
    let started = Instant::now();
    for _ in 0..HITS {
        let value = black_box(cache.peek(black_box(&hot)).unwrap());
        assert!(Arc::ptr_eq(&expected, &value));
        drop(value);
    }
    let peek_elapsed = started.elapsed().as_nanos() / u128::from(HITS);
    assert_eq!(cache.stats(), after);
    (hit_elapsed, peek_elapsed)
}

#[test]
fn native_cache_hot_hit_shares_nodes_and_updates_eviction_order() {
    for width in [32, 128, 512, 2_048, 4_096] {
        let setup = Instant::now();
        let cache = TreeNodeCache::new(width, width * NODE_WEIGHT);
        for ordinal in 0..width {
            cache.insert(key(ordinal), node(ordinal), NODE_WEIGHT);
        }
        let setup_elapsed = setup.elapsed();
        let retained = cache.peek(&key(0)).unwrap();
        let (hit_ns, peek_ns) = measure_hot_hit(&cache, key(0));
        eprintln!(
            "CACHE_HIT residents={width} measured_hits={HITS} complete_lock_get_drop_ns={hit_ns} complete_lock_peek_drop_ns={peek_ns} setup_us={}",
            setup_elapsed.as_micros()
        );

        // The formerly oldest hot entry must survive the next insertion; the
        // next-oldest one is evicted, and previously returned nodes stay valid.
        cache.insert(key(width), node(width), NODE_WEIGHT);
        assert!(cache.peek(&key(1)).is_none());
        assert!(Arc::ptr_eq(&retained, &cache.peek(&key(0)).unwrap()));
        assert_eq!(cache.stats().evictions, 1);
        assert_accounting(&cache);
    }
}

#[test]
fn native_cache_fixed_entry_or_byte_capacity_caps_recency_after_more_insertions() {
    const RESIDENTS: usize = 128;
    for byte_limited in [false, true] {
        for distinct_nodes in [128, 512, 2_048, 8_192] {
            let setup = Instant::now();
            let max_entries = if byte_limited { 16_384 } else { RESIDENTS };
            let max_bytes = if byte_limited {
                RESIDENTS * NODE_WEIGHT
            } else {
                16_384 * NODE_WEIGHT
            };
            let cache = TreeNodeCache::new(max_entries, max_bytes);
            for ordinal in 0..distinct_nodes {
                cache.insert(key(ordinal), node(ordinal), NODE_WEIGHT);
            }
            let setup_elapsed = setup.elapsed();
            let stats = cache.stats();
            assert_eq!(stats.current_entries, RESIDENTS);
            assert_eq!(stats.current_bytes, RESIDENTS * NODE_WEIGHT);
            assert_eq!(stats.evictions, (distinct_nodes - RESIDENTS) as u64);
            assert!(cache.peek(&key(distinct_nodes)).is_none());
            let (hit_ns, peek_ns) = measure_hot_hit(&cache, key(distinct_nodes - 1));
            eprintln!(
                "CACHE_FIXED_CAP byte_limited={byte_limited} distinct_inserted={distinct_nodes} residents={RESIDENTS} evictions={} measured_hits={HITS} complete_lock_get_drop_ns={hit_ns} complete_lock_peek_drop_ns={peek_ns} setup_us={}",
                stats.evictions,
                setup_elapsed.as_micros()
            );
        }
    }
}

#[test]
fn contended_non_mru_hits_preserve_values_and_accounting() {
    const WIDTH: usize = 4096;
    const PER_WORKER: usize = 2048;
    for workers in [1, 4] {
        let cache = TreeNodeCache::new(WIDTH, WIDTH * NODE_WEIGHT);
        for ordinal in 0..WIDTH {
            cache.insert(key(ordinal), node(ordinal), NODE_WEIGHT);
        }
        let before = cache.stats();
        let barrier = std::sync::Barrier::new(workers);
        let start = Instant::now();
        std::thread::scope(|scope| {
            for worker in 0..workers {
                let cache = &cache;
                let barrier = &barrier;
                scope.spawn(move || {
                    barrier.wait();
                    for n in 0..PER_WORKER {
                        let ordinal = (n * 17 + worker * 7) % WIDTH;
                        let value = black_box(cache.get(&key(ordinal)).unwrap());
                        let TreeNode::Leaf(leaf) = value.as_ref() else {
                            panic!("fixture is a leaf");
                        };
                        assert_eq!(leaf.entities, [ordinal as u64 + 1]);
                    }
                });
            }
        });
        let elapsed = start.elapsed();
        let after = cache.stats();
        assert_eq!(after.hits - before.hits, (workers * PER_WORKER) as u64);
        assert_eq!(after.misses, 0);
        assert_eq!(after.evictions, 0);
        assert_accounting(&cache);
        eprintln!(
            "CACHE_CONTENTION residents={WIDTH} workers={workers} hits={} wall_us={} wall_ns_per_hit={} includes_thread_start_and_join=true",
            workers * PER_WORKER,
            elapsed.as_micros(),
            elapsed.as_nanos() / (workers * PER_WORKER) as u128,
        );
    }
}
