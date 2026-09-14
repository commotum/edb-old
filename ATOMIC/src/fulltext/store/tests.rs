use super::*;
fn record(key: &[u8]) -> FulltextRecord {
    FulltextRecord {
        key: key.to_vec(),
        value: vec![7; 32],
    }
}
fn projection(root: Digest, count: u64) -> FulltextProjection {
    FulltextProjection {
        source_manifest: [1; 32],
        source_basis_t: 5,
        source_generation: 1,
        analyzer_version: 1,
        root_hash: root,
        record_count: count,
        encoded_bytes: 1,
        block_count: 1,
    }
}
#[test]
fn merkle_ranges_authenticate_presence_absence_and_child_boundaries() {
    let left = Page::Leaf(vec![record(b"ant"), record(b"ape")]);
    let right = Page::Leaf(vec![record(b"bat"), record(b"cat")]);
    let lh = sha256(&left.encode().unwrap());
    let rh = sha256(&right.encode().unwrap());
    let root = Page::Branch(vec![left.descriptor(lh), right.descriptor(rh)]);
    let hash = sha256(&root.encode().unwrap());
    let pages = Arc::new(BTreeMap::from([
        (lh, Arc::new(left)),
        (rh, Arc::new(right)),
        (hash, Arc::new(root)),
    ]));
    for (prefix, expected) in [
        (b"ap".as_slice(), vec![b"ape".to_vec()]),
        (b"az".as_slice(), vec![]),
    ] {
        let pages = Arc::clone(&pages);
        let mut cursor = FulltextCursor::new(
            &projection(hash, 4),
            prefix,
            FulltextReadLimits::default(),
            Box::new(move |h, s| {
                s.cache_hits += 1;
                Ok(Arc::clone(&pages[&h]))
            }),
        )
        .unwrap();
        let actual = cursor.by_ref().map(|r| r.unwrap().key).collect::<Vec<_>>();
        assert_eq!(actual, expected);
        assert!(
            cursor.stats().cache_hits <= 2,
            "prefix traversed unrelated branch"
        );
        assert_eq!(cursor.stats().block_bytes, 0);
        assert!(cursor.stats().visited_bytes > 0);
    }
    let bytes = pages[&lh].encode().unwrap();
    let mut tampered = bytes.clone();
    *tampered.last_mut().unwrap() ^= 1;
    assert_eq!(
        Page::decode(lh, &tampered).unwrap_err().code,
        "fulltext/page-hash"
    );
    let mut trailing = bytes.clone();
    trailing.push(0);
    assert!(Page::decode(sha256(&trailing), &trailing).is_err());
    let overflow = Page::Branch(vec![
        Child {
            first: b"a".to_vec(),
            last: b"a".to_vec(),
            hash: lh,
            count: u64::MAX,
        },
        Child {
            first: b"b".to_vec(),
            last: b"b".to_vec(),
            hash: rh,
            count: 1,
        },
    ])
    .encode()
    .unwrap();
    assert_eq!(
        Page::decode(sha256(&overflow), &overflow).unwrap_err().code,
        "fulltext/page-count"
    );
    let bad = Child {
        first: b"ant".to_vec(),
        last: b"apple".to_vec(),
        hash: lh,
        count: 2,
    };
    assert!(pages[&lh].validate_child(&bad).is_err());
    let pages = Arc::clone(&pages);
    let mut limited = FulltextCursor::new(
        &projection(hash, 4),
        b"",
        FulltextReadLimits {
            max_records: 100,
            max_block_bytes: 1,
        },
        Box::new(move |h, s| {
            s.cache_hits += 1;
            Ok(Arc::clone(&pages[&h]))
        }),
    )
    .unwrap();
    let exhausted = limited.next().unwrap().unwrap_err();
    assert_eq!(exhausted.code, "fulltext/read-limit");
    assert_eq!(exhausted.category, ErrorCategory::Busy);
    assert!(limited.next().is_none());
}
#[test]
fn spill_sort_is_bounded_deterministic_and_rejects_conflicting_duplicates() {
    let limits = FulltextBuildLimits {
        sort_memory_bytes: 160,
        ..Default::default()
    };
    let mut sorter = Sorter::new(&limits);
    let mut stats = FulltextBuildStats::default();
    for n in (0u32..100).rev() {
        sorter.push(record(&n.to_be_bytes()), &mut stats).unwrap();
        sorter.push(record(&n.to_be_bytes()), &mut stats).unwrap();
    }
    let mut file = sorter.finish(&mut stats).unwrap();
    for n in 0u32..100 {
        assert_eq!(
            read_record(&mut file).unwrap().unwrap().key,
            n.to_be_bytes()
        );
    }
    assert!(read_record(&mut file).unwrap().is_none());
    assert!(stats.peak_buffer_bytes <= limits.sort_memory_bytes + 100);
    assert!(stats.spill_bytes > 100 * 36);
    let mut sorter = Sorter::new(&limits);
    let mut stats = FulltextBuildStats::default();
    sorter.push(record(b"same"), &mut stats).unwrap();
    let mut other = record(b"same");
    other.value.push(9);
    let result = sorter
        .push(other, &mut stats)
        .and_then(|_| sorter.finish(&mut stats).map(|_| ()));
    assert_eq!(result.unwrap_err().code, "fulltext/duplicate-key");
    let limits = FulltextBuildLimits {
        sort_memory_bytes: 1,
        max_spill_bytes: 1,
        ..Default::default()
    };
    assert!(
        Sorter::new(&limits)
            .push(record(b"a"), &mut FulltextBuildStats::default())
            .is_err()
    );
}
#[test]
fn positive_cache_obeys_combined_decoded_bytes_and_entry_limits() {
    let page = Arc::new(Page::Leaf(vec![record(b"item")]));
    let hash = sha256(&page.encode().unwrap());
    let disabled = FulltextCache::new(10, 0);
    disabled.insert_header_at([1; 32], projection(hash, 1));
    disabled.insert([1; 32], hash, Arc::clone(&page), 1);
    assert!(disabled.header([1; 32]).is_none());
    assert!(disabled.get([1; 32], hash).is_none());
    let cache = FulltextCache::new(1, 4096);
    cache.insert([1; 32], hash, Arc::clone(&page), 1);
    cache.insert_header_at([1; 32], projection(hash, 1));
    assert!(cache.get([1; 32], hash).is_none());
    assert!(cache.header([1; 32]).is_some());
    let cache = FulltextCache::new(10, page.retained_bytes());
    cache.insert([1; 32], hash, page, 1);
    assert!(cache.get([1; 32], hash).is_none());
}

#[test]
fn page_recency_and_replacement_preserve_header_fallback_policy() {
    let page = Arc::new(Page::Leaf(vec![record(b"item")]));
    let hash = sha256(&page.encode().unwrap());
    let page_bytes = page.retained_bytes() + 192;
    let cache = FulltextCache::new(3, usize::MAX);
    cache.insert_header_at([9; 32], projection(hash, 1));
    cache.insert([1; 32], hash, Arc::clone(&page), 1);
    cache.insert([2; 32], hash, Arc::clone(&page), 1);
    let held = cache.get([1; 32], hash).unwrap();
    cache.insert([3; 32], hash, Arc::clone(&page), 1);
    assert!(cache.get([2; 32], hash).is_none());
    assert!(cache.header([9; 32]).is_some());
    assert!(Arc::ptr_eq(&cache.get([1; 32], hash).unwrap(), &held));

    cache.insert([1; 32], hash, Arc::clone(&page), 1);
    assert_eq!(cache.stats().entries, 3);
    assert_eq!(
        cache.stats().retained_bytes,
        page_bytes * 2 + Cache::HEADER_BYTES
    );
    cache.insert_header_at([8; 32], projection(hash, 1));
    assert!(cache.get([3; 32], hash).is_none());
    cache.insert_header_at([7; 32], projection(hash, 1));
    assert!(cache.get([1; 32], hash).is_none());
    assert!(Arc::ptr_eq(&held, &page));

    // After pages are exhausted, headers retain their existing key-order
    // eviction policy; they do not participate in page recency.
    cache.insert_header_at([6; 32], projection(hash, 1));
    assert!(cache.header([6; 32]).is_none());
    for source in [[7; 32], [8; 32], [9; 32]] {
        assert!(cache.header(source).is_some());
    }
    assert_eq!(cache.stats().entries, 3);
    assert_eq!(cache.stats().retained_bytes, Cache::HEADER_BYTES * 3);
}
