//! Immutable byte-key search projection, independently derived from one pinned
//! canonical manifest. Its Merkle pages authenticate range boundaries/absence.
//! It does not assign meaning to document/posting values or change datom roots.
use crate::postgres::{postgres_error, verify_schema_compatibility};
use crate::sql_io::{GenericClient, SqlClient};
use crate::{
    Digest, ErrorCategory, OperationContext, PostgresConnectionConfig, SemanticError, sha256,
};
use std::collections::{BTreeMap, VecDeque};
use std::fs::File;
use std::io::{Read, Seek, SeekFrom, Write};
use std::path::PathBuf;
use std::sync::{Arc, Mutex};

const PAGE_MAGIC: &[u8; 4] = b"ATFP";
const HEADER_MAGIC: &[u8; 4] = b"ATFH";
const FORMAT: u32 = 1;
// Unicode lowercase may expand a legal 16MiB string (for example U+0130).
// Routing-page capacity can still reject pathological combinations explicitly.
const MAX_KEY: usize = 32 * 1024 * 1024 + 64;
const MAX_RECORD: usize = 64 * 1024 * 1024;
const MAX_PAGE: usize = MAX_RECORD + 16384;
const FANOUT: usize = 128;
const MAX_DEPTH: usize = 32;

fn projection_lock_key(source: Digest) -> i64 {
    let mut input = b"atomic/fulltext-projection-lock/v1".to_vec();
    input.extend_from_slice(&source);
    i64::from_be_bytes(sha256(&input)[..8].try_into().unwrap())
}

#[cfg(test)]
mod tests {
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
        disabled.insert_header(projection(hash, 1));
        disabled.insert([1; 32], hash, Arc::clone(&page), 1);
        assert!(disabled.header([1; 32]).is_none());
        assert!(disabled.get([1; 32], hash).is_none());
        let cache = FulltextCache::new(1, 4096);
        cache.insert([1; 32], hash, Arc::clone(&page), 1);
        cache.insert_header(projection(hash, 1));
        assert!(cache.get([1; 32], hash).is_none());
        assert!(cache.header([1; 32]).is_some());
        let cache = FulltextCache::new(10, page.retained_bytes());
        cache.insert([1; 32], hash, page, 1);
        assert!(cache.get([1; 32], hash).is_none());
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct FulltextRecord {
    pub key: Vec<u8>,
    pub value: Vec<u8>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct FulltextBuildLimits {
    pub sort_memory_bytes: usize,
    pub page_bytes: usize,
    pub max_record_bytes: usize,
    pub max_key_bytes: usize,
    pub max_records: u64,
    /// Cumulative spill writes, including merge passes (not a disk/RSS claim).
    pub max_spill_bytes: u64,
    pub work_directory: PathBuf,
}
impl Default for FulltextBuildLimits {
    fn default() -> Self {
        Self {
            sort_memory_bytes: 8 * 1024 * 1024,
            page_bytes: 64 * 1024,
            max_record_bytes: MAX_RECORD,
            max_key_bytes: MAX_KEY,
            max_records: 10_000_000,
            max_spill_bytes: 4 * 1024 * 1024 * 1024,
            work_directory: std::env::temp_dir(),
        }
    }
}
impl FulltextBuildLimits {
    fn validate(&self) -> Result<(), SemanticError> {
        if self.sort_memory_bytes == 0
            || !(1024..=1024 * 1024).contains(&self.page_bytes)
            || self.max_record_bytes == 0
            || self.max_record_bytes > MAX_RECORD
            || self.max_key_bytes == 0
            || self.max_key_bytes > MAX_KEY
            || self.max_records == 0
            || self.max_spill_bytes == 0
        {
            return Err(incorrect(
                "fulltext/build-limits",
                "invalid fulltext build limits",
            ));
        }
        Ok(())
    }
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct FulltextBuildStats {
    pub input_records: u64,
    pub records: u64,
    pub blocks: u64,
    pub encoded_bytes: u64,
    pub spill_bytes: u64,
    pub peak_buffer_bytes: usize,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct FulltextProjection {
    pub source_manifest: Digest,
    pub source_basis_t: u64,
    pub source_generation: u64,
    pub analyzer_version: u32,
    pub root_hash: Digest,
    pub record_count: u64,
    pub encoded_bytes: u64,
    pub block_count: u64,
}
impl FulltextProjection {
    fn encode(&self) -> Vec<u8> {
        let mut bytes = Vec::new();
        bytes.extend_from_slice(HEADER_MAGIC);
        bytes.extend_from_slice(&FORMAT.to_be_bytes());
        bytes.extend_from_slice(&self.analyzer_version.to_be_bytes());
        bytes.extend_from_slice(&self.source_manifest);
        for n in [self.source_basis_t, self.source_generation] {
            bytes.extend_from_slice(&n.to_be_bytes());
        }
        bytes.extend_from_slice(&self.root_hash);
        for n in [self.record_count, self.encoded_bytes, self.block_count] {
            bytes.extend_from_slice(&n.to_be_bytes());
        }
        bytes
    }
    fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        let mut d = Decoder::new(bytes);
        if d.take(4)? != HEADER_MAGIC || d.u32()? != FORMAT {
            return Err(fault("fulltext/header-format", "unsupported search header"));
        }
        let analyzer_version = d.u32()?;
        let source_manifest = d.digest()?;
        let source_basis_t = d.u64()?;
        let source_generation = d.u64()?;
        let root_hash = d.digest()?;
        let record_count = d.u64()?;
        let encoded_bytes = d.u64()?;
        let block_count = d.u64()?;
        d.finish()?;
        if analyzer_version == 0 || block_count == 0 || encoded_bytes == 0 {
            return Err(fault(
                "fulltext/header-counts",
                "invalid search header counts",
            ));
        }
        Ok(Self {
            source_manifest,
            source_basis_t,
            source_generation,
            analyzer_version,
            root_hash,
            record_count,
            encoded_bytes,
            block_count,
        })
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct FulltextReadLimits {
    pub max_records: u64,
    pub max_block_bytes: u64,
}
impl Default for FulltextReadLimits {
    fn default() -> Self {
        Self {
            max_records: 100_000,
            max_block_bytes: 64 * 1024 * 1024,
        }
    }
}
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct FulltextReadStats {
    pub cache_hits: u64,
    pub blocks_read: u64,
    pub block_bytes: u64,
    /// Decoded page allocation bytes visited, including cache hits. One bounded
    /// in-flight page may exceed the remaining allowance before rejection.
    pub visited_bytes: u64,
    pub records_examined: u64,
    pub records_yielded: u64,
}

#[derive(Clone, Debug)]
pub(crate) struct Child {
    first: Vec<u8>,
    last: Vec<u8>,
    hash: Digest,
    count: u64,
}
#[derive(Clone, Debug)]
pub(crate) enum Page {
    Leaf(Vec<FulltextRecord>),
    Branch(Vec<Child>),
}
impl Page {
    fn retained_bytes(&self) -> usize {
        std::mem::size_of::<Self>()
            + match self {
                Self::Leaf(records) => {
                    records.capacity() * std::mem::size_of::<FulltextRecord>()
                        + records
                            .iter()
                            .map(|r| r.key.capacity() + r.value.capacity())
                            .sum::<usize>()
                }
                Self::Branch(children) => {
                    children.capacity() * std::mem::size_of::<Child>()
                        + children
                            .iter()
                            .map(|c| c.first.capacity() + c.last.capacity())
                            .sum::<usize>()
                }
            }
    }
    fn encode(&self) -> Result<Vec<u8>, SemanticError> {
        let length = 13usize.saturating_add(match self {
            Self::Leaf(records) => records
                .iter()
                .map(|r| r.key.len().saturating_add(r.value.len()).saturating_add(8))
                .sum::<usize>(),
            Self::Branch(children) => children
                .iter()
                .map(|c| {
                    c.first
                        .len()
                        .saturating_add(c.last.len())
                        .saturating_add(48)
                })
                .sum::<usize>(),
        });
        if length > MAX_PAGE {
            return Err(incorrect(
                "fulltext/page-size",
                "search page exceeds physical bound",
            ));
        }
        let mut b = Vec::with_capacity(length);
        b.extend_from_slice(PAGE_MAGIC);
        b.extend_from_slice(&FORMAT.to_be_bytes());
        match self {
            Self::Leaf(records) => {
                b.push(0);
                b.extend_from_slice(&(records.len() as u32).to_be_bytes());
                for r in records {
                    put_bytes(&mut b, &r.key)?;
                    put_bytes(&mut b, &r.value)?;
                }
            }
            Self::Branch(children) => {
                b.push(1);
                b.extend_from_slice(&(children.len() as u32).to_be_bytes());
                for c in children {
                    put_bytes(&mut b, &c.first)?;
                    put_bytes(&mut b, &c.last)?;
                    b.extend_from_slice(&c.hash);
                    b.extend_from_slice(&c.count.to_be_bytes());
                }
            }
        }
        if b.len() > MAX_PAGE {
            return Err(incorrect(
                "fulltext/page-size",
                "search page exceeds physical bound",
            ));
        }
        Ok(b)
    }
    pub(crate) fn decode(hash: Digest, bytes: &[u8]) -> Result<Self, SemanticError> {
        if bytes.len() > MAX_PAGE || sha256(bytes) != hash {
            return Err(fault(
                "fulltext/page-hash",
                "search page hash/length mismatch",
            ));
        }
        let mut d = Decoder::new(bytes);
        if d.take(4)? != PAGE_MAGIC || d.u32()? != FORMAT {
            return Err(fault("fulltext/page-format", "unsupported search page"));
        }
        let tag = d.take(1)?[0];
        let count = d.u32()? as usize;
        if count > FANOUT {
            return Err(fault(
                "fulltext/page-count",
                "search page count exceeds bound",
            ));
        }
        let page = match tag {
            0 => {
                let mut records = Vec::with_capacity(count);
                for _ in 0..count {
                    let key = d.bytes(MAX_KEY)?;
                    let value = d.bytes(MAX_RECORD)?;
                    if key.len().saturating_add(value.len()) > MAX_RECORD {
                        return Err(fault(
                            "fulltext/page-size",
                            "search record exceeds physical bound",
                        ));
                    }
                    if key.is_empty()
                        || records
                            .last()
                            .is_some_and(|r: &FulltextRecord| r.key >= key)
                    {
                        return Err(fault(
                            "fulltext/page-order",
                            "search leaf keys are not strictly ordered",
                        ));
                    }
                    records.push(FulltextRecord { key, value });
                }
                Self::Leaf(records)
            }
            1 if count > 0 => {
                let mut children = Vec::with_capacity(count);
                let mut total = 0u64;
                for _ in 0..count {
                    let first = d.bytes(MAX_KEY)?;
                    let last = d.bytes(MAX_KEY)?;
                    let hash = d.digest()?;
                    let count = d.u64()?;
                    total = total.checked_add(count).ok_or_else(|| {
                        fault("fulltext/page-count", "search child counts overflow")
                    })?;
                    if first.is_empty()
                        || first > last
                        || count == 0
                        || children.last().is_some_and(|c: &Child| c.last >= first)
                    {
                        return Err(fault(
                            "fulltext/page-order",
                            "search child ranges overlap or are invalid",
                        ));
                    }
                    children.push(Child {
                        first,
                        last,
                        hash,
                        count,
                    });
                }
                Self::Branch(children)
            }
            _ => return Err(fault("fulltext/page-kind", "invalid search page kind")),
        };
        d.finish()?;
        Ok(page)
    }
    fn descriptor(&self, hash: Digest) -> Child {
        match self {
            Self::Leaf(r) => Child {
                first: r.first().map_or_else(Vec::new, |r| r.key.clone()),
                last: r.last().map_or_else(Vec::new, |r| r.key.clone()),
                hash,
                count: r.len() as u64,
            },
            Self::Branch(c) => Child {
                first: c.first().unwrap().first.clone(),
                last: c.last().unwrap().last.clone(),
                hash,
                count: c.iter().map(|c| c.count).sum(),
            },
        }
    }
    fn count(&self)->u64 {
        match self {Self::Leaf(records)=>records.len() as u64,Self::Branch(children)=>children.iter().map(|child|child.count).sum()}
    }
    fn validate_child(&self, expected: &Child) -> Result<(), SemanticError> {
        let (first,last)=match self {
            Self::Leaf(records)=>(records.first().map(|r|r.key.as_slice()).unwrap_or_default(),records.last().map(|r|r.key.as_slice()).unwrap_or_default()),
            Self::Branch(children)=>(children.first().unwrap().first.as_slice(),children.last().unwrap().last.as_slice()),
        };
        if first != expected.first
            || last != expected.last
            || self.count() != expected.count
        {
            return Err(fault(
                "fulltext/child-binding",
                "search child differs from authenticated range/count",
            ));
        }
        Ok(())
    }
}

/// Positive-only bounded cache. Namespace is the authenticated source manifest;
/// missing projections are deliberately retried because search is asynchronous.
#[derive(Clone, Debug)]
pub(crate) struct FulltextCache(Arc<Mutex<Cache>>);
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct FulltextCacheStats {
    pub entries: usize,
    /// Cache-owned decoded capacities plus conservative entry overhead; not
    /// process RSS or Arc pages retained independently by active cursors.
    pub retained_bytes: usize,
    pub max_entries: usize,
    pub max_bytes: usize,
}
#[derive(Debug)]
struct Cache {
    pages: BTreeMap<(Digest, Digest), (Arc<Page>, usize)>,
    headers: BTreeMap<Digest, FulltextProjection>,
    recency: VecDeque<(Digest, Digest)>,
    bytes: usize,
    max_entries: usize,
    max_bytes: usize,
}
impl Cache {
    const HEADER_BYTES: usize =
        std::mem::size_of::<FulltextProjection>() + std::mem::size_of::<Digest>() + 64;
    fn evict(&mut self) {
        while self.pages.len() + self.headers.len() > self.max_entries
            || self.bytes > self.max_bytes
        {
            if let Some(key) = self.recency.pop_front() {
                if let Some((_, bytes)) = self.pages.remove(&key) {
                    self.bytes -= bytes;
                }
            } else if let Some(key) = self.headers.keys().next().copied() {
                self.headers.remove(&key);
                self.bytes -= Self::HEADER_BYTES;
            } else {
                break;
            }
        }
    }
}
impl FulltextCache {
    pub(crate) fn stats(&self) -> FulltextCacheStats {
        let c = self.0.lock().unwrap();
        FulltextCacheStats {
            entries: c.pages.len() + c.headers.len(),
            retained_bytes: c.bytes,
            max_entries: c.max_entries,
            max_bytes: c.max_bytes,
        }
    }
    pub(crate) fn new(max_entries: usize, max_bytes: usize) -> Self {
        Self(Arc::new(Mutex::new(Cache {
            pages: BTreeMap::new(),
            headers: BTreeMap::new(),
            recency: VecDeque::new(),
            bytes: 0,
            max_entries,
            max_bytes,
        })))
    }
    pub(crate) fn header(&self, source: Digest) -> Option<FulltextProjection> {
        self.0.lock().unwrap().headers.get(&source).cloned()
    }
    pub(crate) fn insert_header(&self, p: FulltextProjection) {
        let mut c = self.0.lock().unwrap();
        if c.max_entries == 0 || Cache::HEADER_BYTES > c.max_bytes {
            return;
        }
        if c.headers.insert(p.source_manifest, p).is_none() {
            c.bytes += Cache::HEADER_BYTES;
        }
        c.evict();
    }
    pub(crate) fn get(&self, source: Digest, hash: Digest) -> Option<Arc<Page>> {
        let mut c = self.0.lock().unwrap();
        let key = (source, hash);
        let p = c.pages.get(&key).map(|(p, _)| Arc::clone(p));
        if p.is_some() {
            c.recency.retain(|k| *k != key);
            c.recency.push_back(key);
        }
        p
    }
    pub(crate) fn insert(
        &self,
        source: Digest,
        hash: Digest,
        page: Arc<Page>,
        _encoded_bytes: usize,
    ) {
        let mut c = self.0.lock().unwrap();
        // Decoded allocations plus conservative per-entry tree/LRU/Arc overhead.
        // Cursor-owned Arc references can outlive cache eviction and are charged
        // to the caller's bounded traversal, not falsely claimed as freed RSS.
        let bytes = page.retained_bytes().saturating_add(192);
        if c.max_entries == 0 || bytes > c.max_bytes {
            return;
        }
        let key = (source, hash);
        if let Some((_, old)) = c.pages.remove(&key) {
            c.bytes -= old;
        }
        c.pages.insert(key, (page, bytes));
        c.bytes += bytes;
        c.recency.retain(|k| *k != key);
        c.recency.push_back(key);
        c.evict();
    }
}

type Loader =
    Box<dyn FnMut(Digest, &mut FulltextReadStats) -> Result<Arc<Page>, SemanticError> + Send>;
/// Owned, lazy Merkle range traversal. Dropping it releases its captured source
/// pin. Each decoded child is checked against its parent's range/count proof.
pub struct FulltextCursor {
    loader: Loader,
    pending: Vec<(Child, usize)>,
    leaf: Option<Arc<Page>>,
    offset: usize,
    lower: Vec<u8>,
    upper: Option<Vec<u8>>,
    limits: FulltextReadLimits,
    stats: FulltextReadStats,
    failed: bool,
    operation: Option<OperationContext>,
}
impl FulltextCursor {
    pub(crate) fn new(
        projection: &FulltextProjection,
        prefix: &[u8],
        limits: FulltextReadLimits,
        loader: Loader,
    ) -> Result<Self, SemanticError> {
        if prefix.len() > MAX_KEY || limits.max_records == 0 || limits.max_block_bytes == 0 {
            return Err(incorrect(
                "fulltext/read-limits",
                "invalid search range/read limits",
            ));
        }
        Ok(Self {
            loader,
            pending: vec![(
                Child {
                    first: Vec::new(),
                    last: Vec::new(),
                    hash: projection.root_hash,
                    count: projection.record_count,
                },
                0,
            )],
            leaf: None,
            offset: 0,
            lower: prefix.to_vec(),
            upper: prefix_upper(prefix),
            limits,
            stats: FulltextReadStats::default(),
            failed: false,
            operation: OperationContext::current(),
        })
    }
    pub fn stats(&self) -> FulltextReadStats {
        self.stats
    }
    fn advance(&mut self) -> Result<Option<FulltextRecord>, SemanticError> {
        loop {
            if let Some(page) = &self.leaf {
                let Page::Leaf(records) = page.as_ref() else {
                    unreachable!()
                };
                while let Some(record) = records.get(self.offset) {
                    self.offset += 1;
                    self.stats.records_examined += 1;
                    if record.key < self.lower {
                        continue;
                    }
                    if self
                        .upper
                        .as_ref()
                        .is_some_and(|upper| record.key >= *upper)
                    {
                        self.pending.clear();
                        self.leaf = None;
                        return Ok(None);
                    }
                    if self.stats.records_yielded >= self.limits.max_records {
                        return Err(SemanticError::new(
                            ErrorCategory::Busy,
                            "fulltext/read-limit",
                            "search record budget exhausted",
                        ));
                    }
                    self.stats.records_yielded += 1;
                    return Ok(Some(record.clone()));
                }
                self.leaf = None;
            }
            let Some((child, depth)) = self.pending.pop() else {
                return Ok(None);
            };
            if depth > MAX_DEPTH {
                return Err(fault(
                    "fulltext/tree-depth",
                    "search tree depth exceeds bound",
                ));
            }
            let page = (self.loader)(child.hash, &mut self.stats)?;
            self.stats.visited_bytes = self
                .stats
                .visited_bytes
                .saturating_add(page.retained_bytes() as u64);
            if self.stats.block_bytes.max(self.stats.visited_bytes) > self.limits.max_block_bytes {
                return Err(SemanticError::new(
                    ErrorCategory::Busy,
                    "fulltext/read-limit",
                    "search block-byte budget exhausted (including current bounded page)",
                ));
            }
            if depth > 0 {
                page.validate_child(&child)?;
            } else if page.count() != child.count {
                return Err(fault(
                    "fulltext/root-count",
                    "search root differs from header count",
                ));
            }
            match page.as_ref() {
                Page::Leaf(_) => {
                    self.leaf = Some(page);
                    self.offset = 0;
                }
                Page::Branch(children) => {
                    for child in children.iter().rev() {
                        if child.last >= self.lower
                            && self.upper.as_ref().is_none_or(|u| child.first < *u)
                        {
                            self.pending.push((child.clone(), depth + 1));
                        }
                    }
                }
            }
        }
    }
}
impl Iterator for FulltextCursor {
    type Item = Result<FulltextRecord, SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        let operation = self.operation.clone();
        let _guard = operation.as_ref().map(OperationContext::enter);
        match self.advance() {
            Ok(Some(r)) => Some(Ok(r)),
            Ok(None) => None,
            Err(e) => {
                self.failed = true;
                Some(Err(e))
            }
        }
    }
}

pub(crate) fn load_projection(
    client: &mut impl GenericClient,
    source: Digest,
) -> Result<Option<FulltextProjection>, SemanticError> {
    let row=client.query_opt("SELECT analyzer_version,root_hash,header_hash,header FROM atomic_fulltext_projections WHERE manifest_hash=$1", &[&&source[..]])
        .map_err(|e|postgres_error("fulltext/read-header",e))?;
    let Some(row) = row else { return Ok(None) };
    let bytes: Vec<u8> = row.get(3);
    let stored_hash: Vec<u8> = row.get(2);
    if stored_hash.as_slice() != sha256(&bytes) {
        return Err(fault("fulltext/header-hash", "search header hash mismatch"));
    }
    let projection = FulltextProjection::decode(&bytes)?;
    let root: Vec<u8> = row.get(1);
    let version: i32 = row.get(0);
    if projection.source_manifest != source
        || root.as_slice() != projection.root_hash
        || version <= 0
        || projection.analyzer_version != version as u32
    {
        return Err(fault(
            "fulltext/header-binding",
            "search header differs from stored source binding",
        ));
    }
    Ok(Some(projection))
}
pub(crate) fn load_page(
    client: &mut impl GenericClient,
    source: Digest,
    hash: Digest,
    stats: &mut FulltextReadStats,
) -> Result<Arc<Page>, SemanticError> {
    let row = client
        .query_opt(
            "SELECT payload FROM atomic_fulltext_blocks WHERE manifest_hash=$1 AND block_hash=$2",
            &[&&source[..], &&hash[..]],
        )
        .map_err(|e| postgres_error("fulltext/read-block", e))?
        .ok_or_else(|| {
            fault(
                "fulltext/missing-block",
                "published search block is missing",
            )
        })?;
    let bytes: Vec<u8> = row.get(0);
    stats.blocks_read += 1;
    stats.block_bytes = stats.block_bytes.saturating_add(bytes.len() as u64);
    Ok(Arc::new(Page::decode(hash, &bytes)?))
}

pub struct FulltextStore {
    client: SqlClient,
}
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub enum FulltextBuildFault {
    #[default]
    None,
    /// Deterministic interrupted-upload seam: pages durable, header absent.
    BeforePublication,
}
impl FulltextStore {
    pub fn connect(config: &PostgresConnectionConfig) -> Result<Self, SemanticError> {
        let mut client = config.connect_for("fulltext/connect")?;
        verify_schema_compatibility(&mut client)?;
        Ok(Self { client })
    }
    pub fn open(
        &mut self,
        source: Digest,
        analyzer_version: u32,
    ) -> Result<Option<FulltextProjection>, SemanticError> {
        let p = load_projection(&mut self.client, source)?;
        if p.as_ref()
            .is_some_and(|p| p.analyzer_version != analyzer_version)
        {
            return Err(incorrect(
                "fulltext/analyzer-version",
                "search projection requires rebuild for this analyzer",
            ));
        }
        Ok(p)
    }

    /// Source pin must be held by the caller for the complete iterator/build.
    /// Failed uploads publish no header; replay regenerates deterministic pages.
    pub fn build(
        &mut self,
        source: Digest,
        source_basis_t: u64,
        source_generation: u64,
        analyzer_version: u32,
        records: impl Iterator<Item = Result<FulltextRecord, SemanticError>>,
        limits: &FulltextBuildLimits,
    ) -> Result<(FulltextProjection, FulltextBuildStats), SemanticError> {
        self.build_with_fault(
            source,
            source_basis_t,
            source_generation,
            analyzer_version,
            records,
            limits,
            FulltextBuildFault::None,
        )
    }
    #[allow(clippy::too_many_arguments)]
    pub fn build_with_fault(
        &mut self,
        source: Digest,
        source_basis_t: u64,
        source_generation: u64,
        analyzer_version: u32,
        records: impl Iterator<Item = Result<FulltextRecord, SemanticError>>,
        limits: &FulltextBuildLimits,
        fault_injection: FulltextBuildFault,
    ) -> Result<(FulltextProjection, FulltextBuildStats), SemanticError> {
        let key = projection_lock_key(source);
        let locked: bool = self
            .client
            .query_one("SELECT pg_try_advisory_lock($1)", &[&key])
            .map_err(|e| postgres_error("fulltext/build-lock", e))?
            .get(0);
        if !locked {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "fulltext/projection-busy",
                "search projection build or repair already active",
            ));
        }
        let result = self.build_locked(
            source,
            source_basis_t,
            source_generation,
            analyzer_version,
            records,
            limits,
            fault_injection,
        );
        let release = self
            .client
            .query_one("SELECT pg_advisory_unlock($1)", &[&key])
            .map_err(|e| postgres_error("fulltext/build-unlock", e));
        match (result, release) {
            (Err(error), _) => Err(error),
            (Ok(_), Err(error)) => Err(error),
            (Ok(result), Ok(_)) => Ok(result),
        }
    }
    #[allow(clippy::too_many_arguments)]
    fn build_locked(
        &mut self,
        source: Digest,
        source_basis_t: u64,
        source_generation: u64,
        analyzer_version: u32,
        records: impl Iterator<Item = Result<FulltextRecord, SemanticError>>,
        limits: &FulltextBuildLimits,
        fault_injection: FulltextBuildFault,
    ) -> Result<(FulltextProjection, FulltextBuildStats), SemanticError> {
        limits.validate()?;
        if analyzer_version == 0 || analyzer_version > i32::MAX as u32 {
            return Err(incorrect(
                "fulltext/analyzer-version",
                "analyzer version must fit a positive PostgreSQL integer",
            ));
        }
        let source_row = self
            .client
            .query_opt(
                "SELECT basis_t,log_generation FROM atomic_tree_manifests WHERE manifest_hash=$1",
                &[&&source[..]],
            )
            .map_err(|e| postgres_error("fulltext/build-source", e))?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unavailable,
                    "fulltext/source-unavailable",
                    "search source manifest is not retained",
                )
            })?;
        if u64::try_from(source_row.get::<_, i64>(0)).ok() != Some(source_basis_t)
            || u64::try_from(source_row.get::<_, i64>(1)).ok() != Some(source_generation)
        {
            return Err(incorrect(
                "fulltext/source-binding",
                "search publication coordinates differ from its canonical source",
            ));
        }
        if let Some(p) = self.open(source, analyzer_version)? {
            return Ok((p, FulltextBuildStats::default()));
        }
        let mut stats = FulltextBuildStats::default();
        let mut sorter = Sorter::new(limits);
        for record in records {
            let record = record?;
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
                    "search record build limit exhausted",
                ));
            }
            sorter.push(record, &mut stats)?;
        }
        let mut sorted = sorter.finish(&mut stats)?;
        let mut levels: Vec<Vec<Child>> = Vec::new();
        let mut leaf = Vec::new();
        let mut leaf_bytes = 13;
        while let Some(record) = read_record(&mut sorted)? {
            let bytes = record.key.len() + record.value.len() + 8;
            if !leaf.is_empty() && (leaf.len() >= FANOUT || leaf_bytes + bytes > limits.page_bytes)
            {
                let page = Page::Leaf(std::mem::take(&mut leaf));
                self.push_page(source, page, 0, &mut levels, limits, &mut stats)?;
                leaf_bytes = 13;
            }
            leaf_bytes += bytes;
            leaf.push(record);
            stats.records += 1;
        }
        if !leaf.is_empty() || levels.is_empty() {
            self.push_page(source, Page::Leaf(leaf), 0, &mut levels, limits, &mut stats)?;
        }
        let root = loop {
            let nonempty: Vec<_> = levels
                .iter()
                .enumerate()
                .filter(|(_, c)| !c.is_empty())
                .map(|(i, _)| i)
                .collect();
            if nonempty.len() == 1 && levels[nonempty[0]].len() == 1 {
                break levels[nonempty[0]].pop().unwrap();
            }
            let level = nonempty[0];
            let children = std::mem::take(&mut levels[level]);
            self.push_page(
                source,
                Page::Branch(children),
                level + 1,
                &mut levels,
                limits,
                &mut stats,
            )?;
        };
        let projection = FulltextProjection {
            source_manifest: source,
            source_basis_t,
            source_generation,
            analyzer_version,
            root_hash: root.hash,
            record_count: stats.records,
            encoded_bytes: stats.encoded_bytes,
            block_count: stats.blocks,
        };
        let header = projection.encode();
        if fault_injection == FulltextBuildFault::BeforePublication {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "fulltext/injected-before-publication",
                "search blocks uploaded; publication intentionally interrupted",
            ));
        }
        let header_hash = sha256(&header);
        self.client.execute("INSERT INTO atomic_fulltext_projections(manifest_hash,analyzer_version,root_hash,header_hash,header) VALUES($1,$2,$3,$4,$5) ON CONFLICT(manifest_hash) DO NOTHING", &[&&source[..],&(analyzer_version as i32),&&root.hash[..],&&header_hash[..],&header])
            .map_err(|e|postgres_error("fulltext/publish",e))?;
        let published = self
            .open(source, analyzer_version)?
            .ok_or_else(|| fault("fulltext/missing-header", "search publication disappeared"))?;
        if published != projection {
            return Err(fault(
                "fulltext/publication-conflict",
                "same source produced a different search projection",
            ));
        }
        Ok((projection, stats))
    }

    /// Explicit operator repair invalidates this source's derived search bytes.
    /// Canonical data is untouched. Previously opened search readers may fail
    /// cold reads until reconstruction; this is not an erasure guarantee.
    /// Returns true once all blocks for this exact source have been removed.
    pub fn discard_projection(
        &mut self,
        source: Digest,
        maximum_blocks: usize,
    ) -> Result<bool, SemanticError> {
        if !(1..=4096).contains(&maximum_blocks) {
            return Err(incorrect(
                "fulltext/discard-limit",
                "repair batch must contain 1..4096 blocks",
            ));
        }
        let mut tx = self
            .client
            .transaction()
            .map_err(|e| postgres_error("fulltext/discard-begin", e))?;
        let owner:bool=tx.query_one("SELECT current_user=pg_catalog.pg_get_userbyid(relowner) FROM pg_catalog.pg_class WHERE oid='atomic_fulltext_blocks'::regclass", &[])
            .map_err(|e|postgres_error("fulltext/discard-owner",e))?.get(0);
        if !owner {
            return Err(SemanticError::new(
                ErrorCategory::Forbidden,
                "fulltext/operator-required",
                "search repair requires the catalog owner",
            ));
        }
        let locked: bool = tx
            .query_one(
                "SELECT pg_try_advisory_xact_lock($1)",
                &[&projection_lock_key(source)],
            )
            .map_err(|e| postgres_error("fulltext/discard-lock", e))?
            .get(0);
        if !locked {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "fulltext/projection-busy",
                "search projection build or repair already active",
            ));
        }
        tx.execute(
            "DELETE FROM atomic_fulltext_projections WHERE manifest_hash=$1",
            &[&&source[..]],
        )
        .map_err(|e| postgres_error("fulltext/discard-header", e))?;
        tx.execute("WITH candidates AS (SELECT block_hash FROM atomic_fulltext_blocks WHERE manifest_hash=$1 ORDER BY block_hash LIMIT $2) DELETE FROM atomic_fulltext_blocks b USING candidates c WHERE b.manifest_hash=$1 AND b.block_hash=c.block_hash", &[&&source[..],&(maximum_blocks as i64)])
            .map_err(|e|postgres_error("fulltext/discard-blocks",e))?;
        let complete: bool = tx
            .query_one(
                "SELECT NOT EXISTS(SELECT 1 FROM atomic_fulltext_blocks WHERE manifest_hash=$1)",
                &[&&source[..]],
            )
            .map_err(|e| postgres_error("fulltext/discard-status", e))?
            .get(0);
        tx.commit()
            .map_err(|e| postgres_error("fulltext/discard-commit", e))?;
        Ok(complete)
    }
    fn push_page(
        &mut self,
        source: Digest,
        page: Page,
        level: usize,
        levels: &mut Vec<Vec<Child>>,
        limits: &FulltextBuildLimits,
        stats: &mut FulltextBuildStats,
    ) -> Result<(), SemanticError> {
        if level > MAX_DEPTH {
            return Err(incorrect(
                "fulltext/tree-depth",
                "search build exceeds maximum depth",
            ));
        }
        let bytes = page.encode()?;
        let hash = sha256(&bytes);
        let child = page.descriptor(hash);
        self.client.execute("INSERT INTO atomic_fulltext_blocks(manifest_hash,block_hash,payload) VALUES($1,$2,$3) ON CONFLICT DO NOTHING", &[&&source[..],&&hash[..],&bytes]).map_err(|e|postgres_error("fulltext/write-block",e))?;
        let stored:Vec<u8>=self.client.query_one("SELECT payload FROM atomic_fulltext_blocks WHERE manifest_hash=$1 AND block_hash=$2", &[&&source[..],&&hash[..]]).map_err(|e|postgres_error("fulltext/verify-block",e))?.get(0);
        if stored != bytes {
            return Err(fault(
                "fulltext/block-conflict",
                "content-addressed search block differs",
            ));
        }
        stats.blocks += 1;
        stats.encoded_bytes = stats.encoded_bytes.saturating_add(bytes.len() as u64);
        while levels.len() <= level {
            levels.push(Vec::new());
        }
        levels[level].push(child);
        let estimated: usize = levels[level]
            .iter()
            .map(|c| c.first.len() + c.last.len() + 48)
            .sum();
        if levels[level].len() >= FANOUT
            || (levels[level].len() > 1 && estimated >= limits.page_bytes)
        {
            let children = std::mem::take(&mut levels[level]);
            self.push_page(
                source,
                Page::Branch(children),
                level + 1,
                levels,
                limits,
                stats,
            )?;
        }
        Ok(())
    }
}

struct Sorter<'a> {
    limits: &'a FulltextBuildLimits,
    buffer: Vec<FulltextRecord>,
    bytes: usize,
    levels: Vec<Option<File>>,
}
impl<'a> Sorter<'a> {
    fn new(limits: &'a FulltextBuildLimits) -> Self {
        Self {
            limits,
            buffer: Vec::new(),
            bytes: 0,
            levels: Vec::new(),
        }
    }
    fn push(
        &mut self,
        record: FulltextRecord,
        stats: &mut FulltextBuildStats,
    ) -> Result<(), SemanticError> {
        self.bytes = self.bytes.saturating_add(
            record.key.len() + record.value.len() + std::mem::size_of::<FulltextRecord>(),
        );
        self.buffer.push(record);
        stats.peak_buffer_bytes = stats.peak_buffer_bytes.max(self.bytes);
        if self.bytes >= self.limits.sort_memory_bytes {
            self.flush(stats)?;
        }
        Ok(())
    }
    fn file(&self) -> Result<File, SemanticError> {
        tempfile::tempfile_in(&self.limits.work_directory)
            .map_err(|e| io_error("fulltext/spill-open", e))
    }
    fn flush(&mut self, stats: &mut FulltextBuildStats) -> Result<(), SemanticError> {
        if self.buffer.is_empty() {
            return Ok(());
        }
        self.buffer.sort_by(|a, b| a.key.cmp(&b.key));
        let mut run = self.file()?;
        let mut prior: Option<FulltextRecord> = None;
        for record in self.buffer.drain(..) {
            if let Some(p) = prior.as_ref() {
                if p.key == record.key {
                    if p.value != record.value {
                        return Err(incorrect(
                            "fulltext/duplicate-key",
                            "search records disagree on one key",
                        ));
                    }
                    continue;
                }
            }
            if let Some(p) = prior.replace(record) {
                write_record(&mut run, &p, self.limits, stats)?;
            }
        }
        if let Some(p) = prior {
            write_record(&mut run, &p, self.limits, stats)?;
        }
        self.bytes = 0;
        rewind(&mut run)?;
        let mut level = 0;
        loop {
            if self.levels.len() <= level {
                self.levels.push(None);
            }
            if let Some(existing) = self.levels[level].take() {
                run = self.merge(existing, run, stats)?;
                level += 1;
            } else {
                self.levels[level] = Some(run);
                break;
            }
        }
        Ok(())
    }
    fn merge(
        &self,
        mut a: File,
        mut b: File,
        stats: &mut FulltextBuildStats,
    ) -> Result<File, SemanticError> {
        let mut output = self.file()?;
        let mut left = read_record(&mut a)?;
        let mut right = read_record(&mut b)?;
        while left.is_some() || right.is_some() {
            let take_left = match (&left, &right) {
                (Some(l), Some(r)) => l.key <= r.key,
                (Some(_), None) => true,
                _ => false,
            };
            let record = if take_left {
                let value = left.take().unwrap();
                left = read_record(&mut a)?;
                if right.as_ref().is_some_and(|r| r.key == value.key) {
                    let r = right.take().unwrap();
                    if r.value != value.value {
                        return Err(incorrect(
                            "fulltext/duplicate-key",
                            "search records disagree on one key",
                        ));
                    }
                    right = read_record(&mut b)?;
                }
                value
            } else {
                let value = right.take().unwrap();
                right = read_record(&mut b)?;
                value
            };
            write_record(&mut output, &record, self.limits, stats)?;
        }
        rewind(&mut output)?;
        Ok(output)
    }
    fn finish(mut self, stats: &mut FulltextBuildStats) -> Result<File, SemanticError> {
        self.flush(stats)?;
        let mut result = None;
        for level in 0..self.levels.len() {
            if let Some(run) = self.levels[level].take() {
                result = Some(if let Some(previous) = result {
                    self.merge(previous, run, stats)?
                } else {
                    run
                });
            }
        }
        match result {
            Some(run) => Ok(run),
            None => self.file(),
        }
    }
}
fn validate_record(r: &FulltextRecord, max: usize) -> Result<(), SemanticError> {
    if r.key.is_empty() || r.key.len() > MAX_KEY || r.key.len().saturating_add(r.value.len()) > max
    {
        return Err(incorrect(
            "fulltext/record-size",
            "search record exceeds configured key/value bound",
        ));
    }
    Ok(())
}
fn write_record(
    file: &mut File,
    r: &FulltextRecord,
    limits: &FulltextBuildLimits,
    stats: &mut FulltextBuildStats,
) -> Result<(), SemanticError> {
    let mut bytes = Vec::new();
    put_bytes(&mut bytes, &r.key)?;
    put_bytes(&mut bytes, &r.value)?;
    let hash = sha256(&bytes);
    stats.spill_bytes = stats.spill_bytes.saturating_add(bytes.len() as u64 + 32);
    if stats.spill_bytes > limits.max_spill_bytes {
        return Err(incorrect(
            "fulltext/spill-limit",
            "search cumulative spill-write budget exhausted",
        ));
    }
    file.write_all(&bytes)
        .and_then(|_| file.write_all(&hash))
        .map_err(|e| io_error("fulltext/spill-write", e))
}
fn read_record(file: &mut File) -> Result<Option<FulltextRecord>, SemanticError> {
    let mut length = [0; 4];
    let read = file
        .read(&mut length)
        .map_err(|e| io_error("fulltext/spill-read", e))?;
    if read == 0 {
        return Ok(None);
    }
    file.read_exact(&mut length[read..])
        .map_err(|e| io_error("fulltext/spill-frame", e))?;
    let key_length = u32::from_be_bytes(length) as usize;
    if key_length == 0 || key_length > MAX_KEY {
        return Err(fault("fulltext/spill-frame", "invalid spill key length"));
    }
    let mut key = vec![0; key_length];
    file.read_exact(&mut key)
        .map_err(|e| io_error("fulltext/spill-frame", e))?;
    let mut value_length = [0; 4];
    file.read_exact(&mut value_length)
        .map_err(|e| io_error("fulltext/spill-frame", e))?;
    let n = u32::from_be_bytes(value_length) as usize;
    if n > MAX_RECORD.saturating_sub(key_length) {
        return Err(fault("fulltext/spill-frame", "invalid spill value length"));
    }
    let mut value = vec![0; n];
    file.read_exact(&mut value)
        .map_err(|e| io_error("fulltext/spill-frame", e))?;
    let mut hash = [0; 32];
    file.read_exact(&mut hash)
        .map_err(|e| io_error("fulltext/spill-frame", e))?;
    let mut bytes = Vec::new();
    bytes.extend_from_slice(&length);
    bytes.extend_from_slice(&key);
    bytes.extend_from_slice(&value_length);
    bytes.extend_from_slice(&value);
    if sha256(&bytes) != hash {
        return Err(fault(
            "fulltext/spill-hash",
            "search spill checksum mismatch",
        ));
    }
    Ok(Some(FulltextRecord { key, value }))
}
fn rewind(file: &mut File) -> Result<(), SemanticError> {
    file.seek(SeekFrom::Start(0))
        .map(|_| ())
        .map_err(|e| io_error("fulltext/spill-seek", e))
}
fn prefix_upper(prefix: &[u8]) -> Option<Vec<u8>> {
    let mut upper = prefix.to_vec();
    while let Some(last) = upper.pop() {
        if last != 255 {
            upper.push(last + 1);
            return Some(upper);
        }
    }
    None
}
fn put_bytes(out: &mut Vec<u8>, bytes: &[u8]) -> Result<(), SemanticError> {
    let n = u32::try_from(bytes.len())
        .map_err(|_| incorrect("fulltext/encoding-size", "field exceeds u32"))?;
    out.extend_from_slice(&n.to_be_bytes());
    out.extend_from_slice(bytes);
    Ok(())
}
struct Decoder<'a> {
    bytes: &'a [u8],
    offset: usize,
}
impl<'a> Decoder<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, offset: 0 }
    }
    fn take(&mut self, n: usize) -> Result<&'a [u8], SemanticError> {
        let end = self
            .offset
            .checked_add(n)
            .filter(|end| *end <= self.bytes.len())
            .ok_or_else(|| fault("fulltext/truncated", "truncated search encoding"))?;
        let value = &self.bytes[self.offset..end];
        self.offset = end;
        Ok(value)
    }
    fn u32(&mut self) -> Result<u32, SemanticError> {
        Ok(u32::from_be_bytes(self.take(4)?.try_into().unwrap()))
    }
    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.take(8)?.try_into().unwrap()))
    }
    fn digest(&mut self) -> Result<Digest, SemanticError> {
        Ok(self.take(32)?.try_into().unwrap())
    }
    fn bytes(&mut self, max: usize) -> Result<Vec<u8>, SemanticError> {
        let n = self.u32()? as usize;
        if n > max {
            return Err(fault("fulltext/field-size", "search field exceeds bound"));
        }
        Ok(self.take(n)?.to_vec())
    }
    fn finish(self) -> Result<(), SemanticError> {
        if self.offset == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "fulltext/trailing-data",
                "search encoding has trailing data",
            ))
        }
    }
}
fn incorrect(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::incorrect(code, message)
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
fn io_error(code: &'static str, error: std::io::Error) -> SemanticError {
    SemanticError::new(ErrorCategory::Unavailable, code, error.to_string())
}
