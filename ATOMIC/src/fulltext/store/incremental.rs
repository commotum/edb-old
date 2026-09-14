//! Path-copy maintenance of the existing authenticated byte-key tree. The
//! mutation spool, one routing path, and one bounded page are resident; old
//! subtrees are referenced directly, never copied into a successor namespace.
use super::*;

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct FulltextMutation {
    pub key: Vec<u8>,
    pub value: Option<Vec<u8>>,
}

pub(crate) fn sort_mutations(
    mutations: impl Iterator<Item = Result<FulltextMutation, SemanticError>>,
    limits: &FulltextBuildLimits,
    stats: &mut FulltextBuildStats,
) -> Result<File, SemanticError> {
    let mut sorter = Sorter::new(limits);
    for mutation in mutations {
        let mutation = mutation?;
        let tag = u8::from(mutation.value.is_some());
        let mut record = FulltextRecord {
            key: mutation.key,
            value: mutation.value.unwrap_or_default(),
        };
        // The ephemeral spool has one operation byte; persisted record limits
        // and page encodings are unchanged.
        validate_record(&record, limits.max_record_bytes)?;
        if record.key.len() > limits.max_key_bytes {
            return Err(incorrect(
                "fulltext/key-size",
                "search key exceeds configured capacity",
            ));
        }
        stats.input_records += 1;
        if stats.input_records > limits.max_records {
            return Err(incorrect(
                "fulltext/build-limit",
                "search mutation input limit exhausted",
            ));
        }
        record.value.insert(0, tag);
        sorter.push(record, stats)?;
    }
    sorter.finish(stats)
}

fn decode_mutation(mut record: FulltextRecord) -> Result<FulltextMutation, SemanticError> {
    let Some(tag) = record.value.first().copied() else {
        return Err(fault("fulltext/spill-frame", "missing mutation tag"));
    };
    if tag > 1 || (tag == 0 && record.value.len() != 1) {
        return Err(fault("fulltext/spill-frame", "invalid mutation tag"));
    }
    record.value.remove(0);
    Ok(FulltextMutation {
        key: record.key,
        value: (tag == 1).then_some(record.value),
    })
}

/// Two streaming lookaheads, independent of both corpus and delta width. The
/// base was authenticated by the source producer; validate its complete count
/// and strict ordering before allowing the bulk builder to publish.
pub(crate) struct EmptyCorpusMerge<'a> {
    base: &'a mut dyn Iterator<Item = Result<FulltextRecord, SemanticError>>,
    mutations: File,
    limits: &'a FulltextBuildLimits,
    expected: u64,
    seen: u64,
    prior: Option<Vec<u8>>,
    base_next: Option<FulltextRecord>,
    mutation_next: Option<FulltextMutation>,
    base_done: bool,
    mutation_done: bool,
    failed: bool,
}
impl<'a> EmptyCorpusMerge<'a> {
    pub(crate) fn new(
        base: &'a mut dyn Iterator<Item = Result<FulltextRecord, SemanticError>>,
        mutations: File,
        expected: u64,
        limits: &'a FulltextBuildLimits,
    ) -> Self {
        Self {
            base,
            mutations,
            limits,
            expected,
            seen: 0,
            prior: None,
            base_next: None,
            mutation_next: None,
            base_done: false,
            mutation_done: false,
            failed: false,
        }
    }
    fn advance(&mut self) -> Result<Option<FulltextRecord>, SemanticError> {
        loop {
            if self.base_next.is_none() && !self.base_done {
                match self.base.next().transpose()? {
                    Some(record) => {
                        validate_record(&record, self.limits.max_record_bytes)?;
                        if record.key.len() > self.limits.max_key_bytes {
                            return Err(incorrect(
                                "fulltext/key-size",
                                "search base key exceeds configured capacity",
                            ));
                        }
                        if self.seen >= self.expected
                            || self.prior.as_ref().is_some_and(|key| key >= &record.key)
                        {
                            return Err(fault(
                                "fulltext/empty-base",
                                "authenticated empty-corpus base is duplicated, unordered, or exceeds its root count",
                            ));
                        }
                        self.prior = Some(record.key.clone());
                        self.seen += 1;
                        self.base_next = Some(record);
                    }
                    None => {
                        self.base_done = true;
                        if self.seen != self.expected {
                            return Err(fault(
                                "fulltext/empty-base",
                                "authenticated empty-corpus base is incomplete",
                            ));
                        }
                    }
                }
            }
            if self.mutation_next.is_none() && !self.mutation_done {
                self.mutation_next = read_record(&mut self.mutations)?
                    .map(decode_mutation)
                    .transpose()?;
                self.mutation_done = self.mutation_next.is_none();
            }
            match (&self.base_next, &self.mutation_next) {
                (None, None) => return Ok(None),
                (Some(_), None) => return Ok(self.base_next.take()),
                (Some(base), Some(mutation)) if base.key < mutation.key => {
                    return Ok(self.base_next.take());
                }
                _ => {
                    let mutation = self.mutation_next.take().unwrap();
                    if self
                        .base_next
                        .as_ref()
                        .is_some_and(|base| base.key == mutation.key)
                    {
                        self.base_next = None;
                    }
                    if let Some(value) = mutation.value {
                        return Ok(Some(FulltextRecord {
                            key: mutation.key,
                            value,
                        }));
                    }
                }
            }
        }
    }
}
impl Iterator for EmptyCorpusMerge<'_> {
    type Item = Result<FulltextRecord, SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.advance() {
            Ok(record) => record.map(Ok),
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

fn checked_total(value: i128) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        fault(
            "fulltext/header-counts",
            "incremental search totals overflowed",
        )
    })
}

pub(crate) trait Pages {
    fn load(&mut self, hash: Digest) -> Result<Page, SemanticError>;
    fn save(&mut self, page: &Page) -> Result<Child, SemanticError>;
    fn peak(&mut self, bytes: usize);
}

/// Path-copy the shared byte-key tree using an already admitted sorted spool.
pub(crate) fn edit_pages(
    io: impl Pages,
    predecessor: &FulltextProjection,
    mut sorted: File,
    limits: &FulltextBuildLimits,
) -> Result<PageSummary, SemanticError> {
    let mut editor = Editor {
        io,
        limits,
        blocks: i128::from(predecessor.block_count),
        bytes: i128::from(predecessor.encoded_bytes),
        resident: 0,
    };
    let mut root = predecessor.root_hash;
    let mut count = predecessor.record_count;
    while let Some(record) = read_record(&mut sorted)? {
        (root, count) = editor.apply(root, count, &decode_mutation(record)?)?;
    }
    Ok(PageSummary {
        root,
        records: count,
        bytes: checked_total(editor.bytes)?,
        blocks: checked_total(editor.blocks)?,
    })
}
struct Editor<'a, P> {
    io: P,
    limits: &'a FulltextBuildLimits,
    blocks: i128,
    bytes: i128,
    resident: usize,
}
impl<P: Pages> Editor<'_, P> {
    fn save(&mut self, page: Page) -> Result<Child, SemanticError> {
        if let Page::Branch(children) = &page {
            children
                .iter()
                .try_fold(0u64, |total, child| total.checked_add(child.count))
                .ok_or_else(|| {
                    fault(
                        "fulltext/page-count",
                        "incremental branch record count overflowed",
                    )
                })?;
        }
        let bytes = page.encode()?.len();
        self.io.peak(
            self.resident
                .saturating_add(page.retained_bytes())
                .saturating_add(bytes.saturating_mul(2)),
        );
        let child = self.io.save(&page)?;
        self.blocks += 1;
        self.bytes += bytes as i128;
        Ok(child)
    }
    fn leaves(&mut self, records: Vec<FulltextRecord>) -> Result<Vec<Child>, SemanticError> {
        let mut out = Vec::new();
        let mut group = Vec::new();
        let mut bytes = 13;
        for record in records {
            let size = 8 + record.key.len() + record.value.len();
            if !group.is_empty() && (group.len() == FANOUT || bytes + size > self.limits.page_bytes)
            {
                out.push(self.save(Page::Leaf(std::mem::take(&mut group)))?);
                bytes = 13;
            }
            bytes += size;
            group.push(record);
        }
        if !group.is_empty() {
            out.push(self.save(Page::Leaf(group))?);
        }
        Ok(out)
    }
    fn branches(&mut self, children: Vec<Child>) -> Result<Vec<Child>, SemanticError> {
        let mut out = Vec::new();
        let mut group = Vec::new();
        let mut bytes = 13;
        for child in children {
            let size = 48 + child.first.len() + child.last.len();
            if !group.is_empty() && (group.len() == FANOUT || bytes + size > self.limits.page_bytes)
            {
                out.push(self.save(Page::Branch(std::mem::take(&mut group)))?);
                bytes = 13;
            }
            bytes += size;
            group.push(child);
        }
        if !group.is_empty() {
            out.push(self.save(Page::Branch(group))?);
        }
        Ok(out)
    }
    fn apply(
        &mut self,
        root: Digest,
        count: u64,
        mutation: &FulltextMutation,
    ) -> Result<(Digest, u64), SemanticError> {
        let mut deepest = 0;
        let (mut children, changed) =
            self.edit(root, None, Some(count), mutation, 0, &mut deepest)?;
        if !changed {
            return Ok((root, count));
        }
        if children.is_empty() {
            children.push(self.save(Page::Leaf(Vec::new()))?);
        }
        while children.len() > 1 {
            deepest += 1;
            if deepest > MAX_DEPTH {
                return Err(incorrect(
                    "fulltext/tree-depth",
                    "search update exceeds maximum depth",
                ));
            }
            let old_len = children.len();
            children = self.branches(children)?;
            if children.len() >= old_len {
                return Err(incorrect(
                    "fulltext/encoding-size",
                    "routing keys cannot fit a bounded branch",
                ));
            }
        }
        let root = children.pop().unwrap();
        Ok((root.hash, root.count))
    }
    fn edit(
        &mut self,
        hash: Digest,
        expected: Option<&Child>,
        root_count: Option<u64>,
        mutation: &FulltextMutation,
        depth: usize,
        deepest: &mut usize,
    ) -> Result<(Vec<Child>, bool), SemanticError> {
        if depth > MAX_DEPTH {
            return Err(fault(
                "fulltext/tree-depth",
                "search update path exceeds depth bound",
            ));
        }
        *deepest = (*deepest).max(depth);
        let page = self.io.load(hash)?;
        if let Some(expected) = expected {
            page.validate_child(expected)?;
        }
        if root_count.is_some_and(|count| count != page.count()) {
            return Err(fault(
                "fulltext/root-count",
                "search root differs from header count",
            ));
        }
        let original = page.descriptor(hash);
        let old_bytes = page.encode()?.len();
        let retained = page.retained_bytes();
        self.resident = self.resident.saturating_add(retained);
        self.io.peak(
            self.resident
                .saturating_add(mutation.key.len())
                .saturating_add(mutation.value.as_ref().map_or(0, Vec::len)),
        );
        let result = match page {
            Page::Leaf(mut records) => {
                let position = records.binary_search_by(|r| r.key.cmp(&mutation.key));
                let changed = match (position, &mutation.value) {
                    (Ok(i), Some(value)) if records[i].value == *value => false,
                    (Ok(i), Some(value)) => {
                        records[i].value = value.clone();
                        true
                    }
                    (Ok(i), None) => {
                        records.remove(i);
                        true
                    }
                    (Err(i), Some(value)) => {
                        records.insert(
                            i,
                            FulltextRecord {
                                key: mutation.key.clone(),
                                value: value.clone(),
                            },
                        );
                        true
                    }
                    (Err(_), None) => false,
                };
                if changed {
                    self.blocks -= 1;
                    self.bytes -= old_bytes as i128;
                    (self.leaves(records)?, true)
                } else {
                    (vec![original], false)
                }
            }
            Page::Branch(mut children) => {
                let i = children
                    .partition_point(|c| c.first <= mutation.key)
                    .saturating_sub(1);
                let (replacement, changed) = self.edit(
                    children[i].hash,
                    Some(&children[i]),
                    None,
                    mutation,
                    depth + 1,
                    deepest,
                )?;
                if changed {
                    children.splice(i..=i, replacement);
                    self.blocks -= 1;
                    self.bytes -= old_bytes as i128;
                    (self.branches(children)?, true)
                } else {
                    (vec![original], false)
                }
            }
        };
        self.resident = self.resident.saturating_sub(retained);
        Ok(result)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[derive(Default)]
    struct MemoryPages {
        pages: BTreeMap<Digest, Vec<u8>>,
        reads: usize,
        writes: usize,
        peak: usize,
    }
    impl Pages for MemoryPages {
        fn load(&mut self, hash: Digest) -> Result<Page, SemanticError> {
            self.reads += 1;
            Page::decode(
                hash,
                self.pages
                    .get(&hash)
                    .ok_or_else(|| fault("fulltext/missing-block", "missing memory page"))?,
            )
        }
        fn save(&mut self, page: &Page) -> Result<Child, SemanticError> {
            self.writes += 1;
            let bytes = page.encode()?;
            let hash = sha256(&bytes);
            self.pages.insert(hash, bytes);
            Ok(page.descriptor(hash))
        }
        fn peak(&mut self, bytes: usize) {
            self.peak = self.peak.max(bytes);
        }
    }
    fn key(n: u32) -> Vec<u8> {
        n.to_be_bytes().to_vec()
    }
    fn verify(
        pages: &MemoryPages,
        hash: Digest,
        expected: &BTreeMap<Vec<u8>, Vec<u8>>,
    ) -> (u64, u64) {
        let mut pending = vec![(hash, None)];
        let mut seen = BTreeMap::new();
        let mut blocks = 0;
        let mut bytes = 0;
        while let Some((hash, descriptor)) = pending.pop() {
            let encoded = &pages.pages[&hash];
            let page = Page::decode(hash, encoded).unwrap();
            if let Some(descriptor) = descriptor {
                page.validate_child(&descriptor).unwrap();
            }
            blocks += 1;
            bytes += encoded.len() as u64;
            match page {
                Page::Leaf(records) => {
                    for r in records {
                        assert!(seen.insert(r.key, r.value).is_none());
                    }
                }
                Page::Branch(children) => {
                    for c in children {
                        pending.push((c.hash, Some(c)));
                    }
                }
            }
        }
        assert_eq!(&seen, expected);
        (blocks, bytes)
    }
    fn seed<'a>(
        n: u32,
        limits: &'a FulltextBuildLimits,
    ) -> (Editor<'a, MemoryPages>, Digest, BTreeMap<Vec<u8>, Vec<u8>>) {
        let mut editor = Editor {
            io: MemoryPages::default(),
            limits,
            blocks: 0,
            bytes: 0,
            resident: 0,
        };
        let expected: BTreeMap<_, _> = (0..n).map(|n| (key(n * 2), vec![n as u8; 180])).collect();
        let mut children = editor
            .leaves(
                expected
                    .iter()
                    .map(|(key, value)| FulltextRecord {
                        key: key.clone(),
                        value: value.clone(),
                    })
                    .collect(),
            )
            .unwrap();
        if children.is_empty() {
            children.push(editor.save(Page::Leaf(Vec::new())).unwrap());
        }
        while children.len() > 1 {
            children = editor.branches(children).unwrap();
        }
        let root = children.pop().unwrap().hash;
        assert_eq!(
            verify(&editor.io, root, &expected),
            (editor.blocks as u64, editor.bytes as u64)
        );
        (editor, root, expected)
    }
    #[test]
    fn incremental_path_copy_matches_independent_map_and_exact_reachable_totals() {
        let limits = FulltextBuildLimits {
            page_bytes: 1024,
            ..Default::default()
        };
        let (mut editor, mut root, mut expected) = seed(300, &limits);
        let original_root = root;
        let original_expected = expected.clone();
        for step in 0..900u32 {
            let n = (step * 73) % 701;
            let value = (step % 3 != 0).then(|| vec![step as u8; (step % 270) as usize]);
            let mutation = FulltextMutation {
                key: key(n),
                value: value.clone(),
            };
            let next = editor
                .apply(root, expected.len() as u64, &mutation)
                .unwrap();
            match value {
                Some(value) => {
                    expected.insert(key(n), value);
                }
                None => {
                    expected.remove(&key(n));
                }
            }
            root = next.0;
            assert_eq!(next.1, expected.len() as u64);
            if step % 19 == 0 {
                assert_eq!(
                    verify(&editor.io, root, &expected),
                    (editor.blocks as u64, editor.bytes as u64)
                );
            }
        }
        // A successor cannot mutate the retained predecessor's exact bytes.
        verify(&editor.io, original_root, &original_expected);
        for k in expected.keys().cloned().collect::<Vec<_>>() {
            let mutation = FulltextMutation {
                key: k.clone(),
                value: None,
            };
            let next = editor
                .apply(root, expected.len() as u64, &mutation)
                .unwrap();
            expected.remove(&k);
            root = next.0;
            assert_eq!(next.1, expected.len() as u64);
        }
        assert_eq!(verify(&editor.io, root, &expected), (1, 13));
        assert_eq!((editor.blocks, editor.bytes), (1, 13));
    }
    #[test]
    fn incremental_fixed_mutation_reads_only_one_bounded_path_and_noop_writes_nothing() {
        let limits = FulltextBuildLimits {
            page_bytes: 1024,
            ..Default::default()
        };
        for n in [64, 256, 1024, 4096] {
            let (mut editor, root, mut expected) = seed(n, &limits);
            editor.io.reads = 0;
            editor.io.writes = 0;
            let mutation = FulltextMutation {
                key: key(n),
                value: Some(vec![99; 180]),
            };
            let next = editor.apply(root, u64::from(n), &mutation).unwrap();
            expected.insert(mutation.key.clone(), mutation.value.clone().unwrap());
            assert!(editor.io.reads <= 4, "N={n} reads={}", editor.io.reads);
            assert!(editor.io.writes <= 4, "N={n} writes={}", editor.io.writes);
            eprintln!(
                "fulltext kernel N={n}: reads={}, writes={}, peak logical path/page bytes={}",
                editor.io.reads, editor.io.writes, editor.io.peak
            );
            assert_eq!(
                verify(&editor.io, next.0, &expected),
                (editor.blocks as u64, editor.bytes as u64)
            );
            editor.io.writes = 0;
            let repeated = editor.apply(next.0, next.1, &mutation).unwrap();
            assert_eq!(repeated, next);
            assert_eq!(editor.io.writes, 0);
            let missing = editor
                .apply(
                    next.0,
                    next.1,
                    &FulltextMutation {
                        key: key(u32::MAX),
                        value: None,
                    },
                )
                .unwrap();
            assert_eq!(missing, next);
            assert_eq!(editor.io.writes, 0);
        }
    }
    #[test]
    fn mutation_spool_preserves_delete_empty_value_and_rejects_conflicts_or_input_overflow() {
        let limits = FulltextBuildLimits {
            sort_memory_bytes: 1,
            ..Default::default()
        };
        let deletes = FulltextMutation {
            key: key(1),
            value: None,
        };
        let empty = FulltextMutation {
            key: key(2),
            value: Some(Vec::new()),
        };
        let mut stats = FulltextBuildStats::default();
        let mut file = sort_mutations(
            [Ok(empty.clone()), Ok(deletes.clone()), Ok(deletes.clone())].into_iter(),
            &limits,
            &mut stats,
        )
        .unwrap();
        assert_eq!(stats.input_records, 3);
        assert_eq!(
            decode_mutation(read_record(&mut file).unwrap().unwrap()).unwrap(),
            deletes
        );
        assert_eq!(
            decode_mutation(read_record(&mut file).unwrap().unwrap()).unwrap(),
            empty
        );
        assert!(read_record(&mut file).unwrap().is_none());
        let conflict = FulltextMutation {
            key: key(1),
            value: Some(Vec::new()),
        };
        assert_eq!(
            sort_mutations(
                [Ok(deletes.clone()), Ok(conflict)].into_iter(),
                &limits,
                &mut stats
            )
            .unwrap_err()
            .code,
            "fulltext/duplicate-key"
        );
        let limits = FulltextBuildLimits {
            max_records: 1,
            ..limits
        };
        assert_eq!(
            sort_mutations(
                [Ok(deletes), Ok(empty)].into_iter(),
                &limits,
                &mut FulltextBuildStats::default()
            )
            .unwrap_err()
            .code,
            "fulltext/build-limit"
        );
    }
    #[test]
    fn incremental_path_authenticates_hash_parent_bounds_and_root_count() {
        let limits = FulltextBuildLimits {
            page_bytes: 1024,
            ..Default::default()
        };
        let (mut editor, root, _) = seed(64, &limits);
        let mutation = FulltextMutation {
            key: key(0),
            value: None,
        };
        assert_eq!(
            editor.apply(root, 63, &mutation).unwrap_err().code,
            "fulltext/root-count"
        );
        let mut children = match Page::decode(root, &editor.io.pages[&root]).unwrap() {
            Page::Branch(c) => c,
            _ => panic!("expected branch"),
        };
        children[0].count += 1;
        let bad = editor.io.save(&Page::Branch(children)).unwrap();
        assert!(editor.apply(bad.hash, 65, &mutation).is_err());
        let bytes = editor.io.pages.get_mut(&root).unwrap();
        *bytes.last_mut().unwrap() ^= 1;
        assert_eq!(
            editor.apply(root, 64, &mutation).unwrap_err().code,
            "fulltext/page-hash"
        );
    }

    #[test]
    fn successor_root_is_self_contained_after_unpinned_predecessor_and_intermediates_retire() {
        let limits = FulltextBuildLimits {
            page_bytes: 1024,
            ..Default::default()
        };
        let (mut editor, mut root, mut expected) = seed(256, &limits);
        let initial_writes = editor.io.writes;
        for version in 1..=20 {
            let mutation = FulltextMutation {
                key: key(128),
                value: Some(vec![version; 180]),
            };
            root = editor
                .apply(root, expected.len() as u64, &mutation)
                .unwrap()
                .0;
            expected.insert(mutation.key, mutation.value.unwrap());
        }
        // Complete uploads include intermediate paths; header totals do not.
        assert_eq!(editor.io.writes - initial_writes, 60);
        let mut live = std::collections::BTreeSet::new();
        let mut pending = vec![root];
        while let Some(hash) = pending.pop() {
            assert!(live.insert(hash));
            if let Page::Branch(children) = Page::decode(hash, &editor.io.pages[&hash]).unwrap() {
                pending.extend(children.into_iter().map(|c| c.hash));
            }
        }
        assert!(editor.io.pages.len() > live.len() + 40);
        editor.io.pages.retain(|hash, _| live.contains(hash));
        assert_eq!(
            verify(&editor.io, root, &expected),
            (editor.blocks as u64, editor.bytes as u64)
        );
        assert_eq!(live.len(), editor.blocks as usize);
    }

    #[test]
    fn empty_corpus_bulk_merge_charges_five_mutations_not_six_final_records() {
        let limits = FulltextBuildLimits {
            max_records: 5,
            sort_memory_bytes: 1,
            ..Default::default()
        };
        let base = vec![
            FulltextRecord {
                key: key(10),
                value: vec![0; 16],
            },
            FulltextRecord {
                key: key(20),
                value: vec![0; 16],
            },
        ];
        let mutations = [1, 2, 3, 4, 10].into_iter().map(|n| {
            Ok(FulltextMutation {
                key: key(n),
                value: Some(vec![n as u8; 16]),
            })
        });
        let mut stats = FulltextBuildStats::default();
        let sorted = sort_mutations(mutations, &limits, &mut stats).unwrap();
        let mut base_iter = base.into_iter().map(Ok);
        let records = EmptyCorpusMerge::new(&mut base_iter, sorted, 2, &limits)
            .collect::<Result<Vec<_>, _>>()
            .unwrap();
        assert_eq!(stats.input_records, 5);
        assert_eq!(records.len(), 6);
        assert_eq!(
            records.iter().map(|r| r.key.clone()).collect::<Vec<_>>(),
            [1, 2, 3, 4, 10, 20].map(key)
        );
        assert_eq!(records.last().unwrap().value, vec![0; 16]);
        let mut editor = Editor {
            io: MemoryPages::default(),
            limits: &limits,
            blocks: 0,
            bytes: 0,
            resident: 0,
        };
        let expected = records
            .iter()
            .map(|r| (r.key.clone(), r.value.clone()))
            .collect();
        let root = editor.leaves(records).unwrap().pop().unwrap();
        assert_eq!(
            verify(&editor.io, root.hash, &expected),
            (editor.blocks as u64, editor.bytes as u64)
        );
    }

    #[test]
    fn empty_corpus_bulk_merge_rejects_incomplete_duplicate_or_failed_base_and_fuses() {
        let limits = FulltextBuildLimits::default();
        for (records, count) in [
            (vec![], 1),
            (
                vec![Ok(FulltextRecord {
                    key: key(1),
                    value: vec![],
                })],
                0,
            ),
            (
                vec![
                    Ok(FulltextRecord {
                        key: key(1),
                        value: vec![]
                    });
                    2
                ],
                2,
            ),
            (
                vec![Err(fault("fulltext/test-base", "base source failed"))],
                1,
            ),
        ] {
            let sorted = sort_mutations(
                std::iter::empty(),
                &limits,
                &mut FulltextBuildStats::default(),
            )
            .unwrap();
            let mut records = records.into_iter();
            let mut merge = EmptyCorpusMerge::new(&mut records, sorted, count, &limits);
            assert!(merge.by_ref().collect::<Result<Vec<_>, _>>().is_err());
            assert!(merge.next().is_none());
        }
    }
}
