use crate::postgres::{postgres_error, recover_to};
use crate::{
    CommitReceipt, Database, Datom, Digest, DurableTransaction, Entity, ErrorCategory,
    IndexManifest, IndexOrder, IndexSegment, PullPattern, Query, QueryControl, QueryInput,
    QueryOutcome, QueryValue, SegmentRef, SemanticError, TxOp, View, decode_index_manifest,
    decode_index_segment, decode_transaction, encode_index_manifest, encode_index_segment, sha256,
    transaction_hash,
};
use postgres::{Client, GenericClient, NoTls};
use std::collections::{BTreeMap, VecDeque};
use std::sync::Arc;
use std::time::{Duration, Instant};

const DEFAULT_SEGMENT_DATOMS: usize = 4_096;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum IndexBuildFault {
    None,
    AfterSegments,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IndexBuildReceipt {
    pub basis_t: u64,
    pub manifest_hash: Digest,
    pub segment_count: usize,
    pub reused: bool,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct CacheStats {
    pub hits: u64,
    pub misses: u64,
    pub evictions: u64,
}

#[derive(Debug)]
struct SegmentCache {
    capacity: usize,
    entries: BTreeMap<Digest, Arc<IndexSegment>>,
    recency: VecDeque<Digest>,
    stats: CacheStats,
}

impl SegmentCache {
    fn new(capacity: usize) -> Self {
        Self {
            capacity,
            entries: BTreeMap::new(),
            recency: VecDeque::new(),
            stats: CacheStats::default(),
        }
    }

    fn get(&mut self, hash: &Digest) -> Option<Arc<IndexSegment>> {
        let value = self.entries.get(hash).cloned();
        if value.is_some() {
            self.stats.hits += 1;
            self.recency.retain(|candidate| candidate != hash);
            self.recency.push_back(*hash);
        } else {
            self.stats.misses += 1;
        }
        value
    }

    fn insert(&mut self, hash: Digest, segment: Arc<IndexSegment>) {
        if self.capacity == 0 {
            return;
        }
        self.entries.insert(hash, segment);
        self.recency.retain(|candidate| *candidate != hash);
        self.recency.push_back(hash);
        while self.entries.len() > self.capacity {
            if let Some(oldest) = self.recency.pop_front() {
                self.entries.remove(&oldest);
                self.stats.evictions += 1;
            }
        }
    }
}

/// Deterministic foreground entry point for Goal 4's background consolidation
/// job. Scheduling is deliberately left to the caller.
pub struct PostgresIndexer {
    client: Client,
    database_id: String,
    segment_datoms: usize,
}

impl PostgresIndexer {
    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
    ) -> Result<Self, SemanticError> {
        let client = Client::connect(connection, NoTls)
            .map_err(|error| postgres_error("index/connect", error))?;
        Ok(Self {
            client,
            database_id: database_id.into(),
            segment_datoms: DEFAULT_SEGMENT_DATOMS,
        })
    }

    pub fn with_segment_datoms(mut self, segment_datoms: usize) -> Result<Self, SemanticError> {
        if segment_datoms == 0 {
            return Err(SemanticError::incorrect(
                "index/zero-segment-size",
                "segment size must be positive",
            ));
        }
        self.segment_datoms = segment_datoms;
        Ok(self)
    }

    pub fn consolidate(&mut self) -> Result<IndexBuildReceipt, SemanticError> {
        self.consolidate_with_fault(IndexBuildFault::None)
    }

    pub fn consolidate_with_fault(
        &mut self,
        fault_point: IndexBuildFault,
    ) -> Result<IndexBuildReceipt, SemanticError> {
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("index/build-begin", error))?;
        let (basis_t, tx_hash) = read_head(&mut transaction, &self.database_id)?;
        if basis_t == 0 {
            return Err(SemanticError::incorrect(
                "index/empty-database",
                "there is no positive basis to consolidate",
            ));
        }

        // Carry an already-pruned valid base forward and apply only its tail;
        // this prevents toggling noHistory off from resurrecting forgotten facts.
        let mut cache = SegmentCache::new(0);
        let (mut database, mut database_hash, _) =
            match load_latest_base(&mut transaction, &self.database_id, basis_t, &mut cache)? {
                Some(base) => base,
                None => {
                    let recovered =
                        recover_to(&mut transaction, &self.database_id, basis_t, tx_hash)?;
                    (recovered.database, recovered.final_hash, 0)
                }
            };
        if database.basis_t() < basis_t {
            apply_tail(
                &mut transaction,
                &self.database_id,
                &mut database,
                &mut database_hash,
                basis_t,
            )?;
        }
        if database_hash != tx_hash {
            return Err(fault(
                "index/build-head-mismatch",
                "consolidation input does not reach the locked head",
            ));
        }

        let current_eavt = database.datoms(View::Current, IndexOrder::Eavt);
        let mut history_eavt = database.datoms(View::History, IndexOrder::Eavt);
        history_eavt.retain(|datom| {
            database
                .schema()
                .attribute(datom.attribute)
                .map_or(true, |attribute| {
                    !attribute.no_history
                        || (datom.added
                            && current_eavt.iter().any(|current| {
                                current.entity == datom.entity
                                    && current.attribute == datom.attribute
                                    && current.value.stored_eq(&datom.value)
                            }))
                })
        });

        let mut encoded_segments = Vec::new();
        let mut references = Vec::new();
        for history in [false, true] {
            let source = if history {
                &history_eavt
            } else {
                &current_eavt
            };
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let mut datoms: Vec<_> = source
                    .iter()
                    .filter(|datom| index_member(&database, datom, order))
                    .cloned()
                    .collect();
                datoms.sort_by(|left, right| left.cmp_in(right, order));
                for (ordinal, chunk) in datoms.chunks(self.segment_datoms).enumerate() {
                    let segment = IndexSegment {
                        order,
                        history,
                        datoms: chunk.to_vec(),
                    };
                    let payload = encode_index_segment(&segment)?;
                    let hash = sha256(&payload);
                    references.push(SegmentRef {
                        order,
                        history,
                        ordinal: u32::try_from(ordinal).map_err(|_| {
                            fault("index/too-many-segments", "segment ordinal exceeds u32")
                        })?,
                        hash,
                        count: u32::try_from(chunk.len())
                            .expect("configured segment size fits u32"),
                    });
                    encoded_segments.push((hash, payload));
                }
            }
        }
        let mut schema_history = vec![Vec::new(); basis_t as usize];
        for (basis, change) in database.schema_changes() {
            schema_history[(basis - 1) as usize].push(change.clone());
        }
        let manifest = IndexManifest {
            database_id: self.database_id.clone(),
            basis_t,
            tx_hash,
            next_eid: database.next_eid(),
            schema: database.schema().clone(),
            schema_history,
            segments: references,
        };
        let manifest_payload = encode_index_manifest(&manifest)?;
        let manifest_hash = sha256(&manifest_payload);

        for (hash, payload) in &encoded_segments {
            transaction.execute(
                "INSERT INTO atomic_index_segments (segment_hash, payload) VALUES ($1, $2) ON CONFLICT DO NOTHING",
                &[&&hash[..], &&payload[..]],
            ).map_err(|error| postgres_error("index/segment-insert", error))?;
            let stored: Vec<u8> = transaction
                .query_one(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&hash[..]],
                )
                .map_err(|error| postgres_error("index/segment-verify", error))?
                .get(0);
            if stored != *payload {
                return Err(fault(
                    "index/segment-hash-conflict",
                    "segment identity is bound to different bytes",
                ));
            }
        }
        if fault_point == IndexBuildFault::AfterSegments {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "index/injected-failure",
                "injected failure after segment insertion",
            ));
        }
        let basis_sql = sql_basis(basis_t)?;
        transaction.execute(
            "INSERT INTO atomic_index_manifests (database_id, basis_t, tx_hash, manifest_hash, payload) \
             VALUES ($1, $2, $3, $4, $5) ON CONFLICT DO NOTHING",
            &[&self.database_id, &basis_sql, &&tx_hash[..], &&manifest_hash[..], &&manifest_payload[..]],
        ).map_err(|error| postgres_error("index/manifest-insert", error))?;
        let row = transaction.query_one(
            "SELECT manifest_hash, payload FROM atomic_index_manifests WHERE database_id = $1 AND basis_t = $2",
            &[&self.database_id, &basis_sql],
        ).map_err(|error| postgres_error("index/manifest-verify", error))?;
        let stored_hash = digest(row.get(0), "manifest hash")?;
        let stored_payload: Vec<u8> = row.get(1);
        if stored_hash != manifest_hash || stored_payload != manifest_payload {
            return Err(fault(
                "index/non-deterministic-rebuild",
                "this basis already has a different index manifest",
            ));
        }
        let reused = transaction.query_one(
            "SELECT created_at < transaction_timestamp() FROM atomic_index_manifests WHERE database_id = $1 AND basis_t = $2",
            &[&self.database_id, &basis_sql],
        ).map_err(|error| postgres_error("index/manifest-age", error))?.get(0);
        transaction
            .commit()
            .map_err(|error| postgres_error("index/build-commit", error))?;
        Ok(IndexBuildReceipt {
            basis_t,
            manifest_hash,
            segment_count: encoded_segments.len(),
            reused,
        })
    }
}

/// One independent peer connection. Database values returned from `db` and
/// `sync_to` remain immutable after the connection advances.
pub struct Peer {
    client: Client,
    writer: crate::PostgresStore,
    database_id: String,
    current: Arc<Database>,
    current_hash: Digest,
    durable_base_t: u64,
    cache: SegmentCache,
    reports: VecDeque<DurableTransaction>,
}

impl Peer {
    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        let database_id = database_id.into();
        let mut client = Client::connect(connection, NoTls)
            .map_err(|error| postgres_error("peer/connect", error))?;
        let writer = crate::PostgresStore::connect(connection)?;
        let (head_basis, head_hash) = read_head(&mut client, &database_id)?;
        let mut cache = SegmentCache::new(cache_capacity);
        let (mut database, mut current_hash, durable_base_t) =
            match load_latest_base(&mut client, &database_id, head_basis, &mut cache)? {
                Some(base) => base,
                None => {
                    let recovered = recover_to(&mut client, &database_id, head_basis, head_hash)?;
                    (recovered.database, recovered.final_hash, 0)
                }
            };
        let mut reports = VecDeque::new();
        if database.basis_t() < head_basis {
            reports.extend(apply_tail(
                &mut client,
                &database_id,
                &mut database,
                &mut current_hash,
                head_basis,
            )?);
        }
        if current_hash != head_hash {
            return Err(fault(
                "peer/head-mismatch",
                "peer open does not reach observed head",
            ));
        }
        Ok(Self {
            client,
            writer,
            database_id,
            current: Arc::new(database),
            current_hash,
            durable_base_t,
            cache,
            reports,
        })
    }

    pub fn db(&self) -> Arc<Database> {
        Arc::clone(&self.current)
    }
    pub fn basis_t(&self) -> u64 {
        self.current.basis_t()
    }
    pub fn durable_base_t(&self) -> u64 {
        self.durable_base_t
    }
    pub fn cache_stats(&self) -> CacheStats {
        self.cache.stats
    }

    pub fn entity(&self, id: u64) -> Entity {
        Entity::new(self.db(), id)
    }

    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.current.query(query, inputs, control)
    }

    pub fn pull(&self, pattern: &PullPattern, entity: u64) -> Result<QueryValue, SemanticError> {
        self.current.pull(pattern, entity)
    }

    /// Submit through the same expected-basis/idempotency contract as the
    /// concrete store, then monotonically advance this peer to the commit.
    pub fn transact(
        &mut self,
        request_key: &str,
        expected_basis_t: u64,
        ops: &[TxOp],
        tx_instant: i64,
    ) -> Result<CommitReceipt, SemanticError> {
        let receipt = self.writer.transact(
            &self.database_id,
            request_key,
            expected_basis_t,
            ops,
            tx_instant,
        )?;
        self.sync_to(receipt.basis_t, Duration::from_secs(30))?;
        Ok(receipt)
    }

    pub fn sync(&mut self) -> Result<Arc<Database>, SemanticError> {
        let (target, _) = read_head(&mut self.client, &self.database_id)?;
        self.advance_to(target)
    }

    pub fn sync_to(
        &mut self,
        target: u64,
        timeout: Duration,
    ) -> Result<Arc<Database>, SemanticError> {
        let deadline = Instant::now() + timeout;
        loop {
            let (head, _) = read_head(&mut self.client, &self.database_id)?;
            if head >= target {
                return self.advance_to(target);
            }
            if Instant::now() >= deadline {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "peer/sync-timeout",
                    format!("basis {target} is not yet committed"),
                ));
            }
            std::thread::sleep(Duration::from_millis(10));
        }
    }

    pub fn take_tx_reports(&mut self) -> Vec<DurableTransaction> {
        self.reports.drain(..).collect()
    }

    /// Adopt a newly published physical base without changing the connection's
    /// logical basis. This is the native counterpart of recovered
    /// `notify-index`: it is optional for correctness and leaves old `Arc`
    /// snapshots untouched.
    pub fn refresh_index(&mut self) -> Result<bool, SemanticError> {
        let through = self.current.basis_t();
        let Some((mut candidate, mut hash, base_t)) = load_latest_base(
            &mut self.client,
            &self.database_id,
            through,
            &mut self.cache,
        )?
        else {
            return Ok(false);
        };
        if base_t <= self.durable_base_t {
            return Ok(false);
        }
        if candidate.basis_t() < through {
            apply_tail(
                &mut self.client,
                &self.database_id,
                &mut candidate,
                &mut hash,
                through,
            )?;
        }
        if hash != self.current_hash || !same_current_value(&candidate, &self.current) {
            return Err(fault(
                "peer/index-adoption-divergence",
                "new index base does not describe the connection's current database",
            ));
        }
        self.current = Arc::new(candidate);
        self.durable_base_t = base_t;
        Ok(true)
    }

    fn advance_to(&mut self, target: u64) -> Result<Arc<Database>, SemanticError> {
        if target <= self.current.basis_t() {
            return Ok(self.db());
        }
        let mut successor = (*self.current).clone();
        let applied = apply_tail(
            &mut self.client,
            &self.database_id,
            &mut successor,
            &mut self.current_hash,
            target,
        )?;
        self.reports.extend(applied);
        self.current = Arc::new(successor);
        Ok(self.db())
    }
}

fn load_latest_base<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    through: u64,
    cache: &mut SegmentCache,
) -> Result<Option<(Database, Digest, u64)>, SemanticError> {
    let through_sql = sql_basis(through)?;
    let rows = client
        .query(
            "SELECT basis_t, tx_hash, manifest_hash, payload FROM atomic_index_manifests \
         WHERE database_id = $1 AND basis_t <= $2 ORDER BY basis_t DESC",
            &[&database_id, &through_sql],
        )
        .map_err(|error| postgres_error("peer/index-manifests", error))?;
    for row in rows {
        let basis = pg_basis(row.get(0), "index manifest")?;
        let tx_hash = digest(row.get(1), "index transaction hash")?;
        let manifest_hash = digest(row.get(2), "manifest hash")?;
        let payload: Vec<u8> = row.get(3);
        if sha256(&payload) != manifest_hash {
            continue;
        }
        let Ok(manifest) = decode_index_manifest(&payload) else {
            continue;
        };
        if manifest.database_id != database_id
            || manifest.basis_t != basis
            || manifest.tx_hash != tx_hash
        {
            continue;
        }
        if let Ok(database) = load_manifest_database(client, &manifest, cache) {
            return Ok(Some((database, tx_hash, basis)));
        }
    }
    Ok(None)
}

fn load_manifest_database<C: GenericClient>(
    client: &mut C,
    manifest: &IndexManifest,
    cache: &mut SegmentCache,
) -> Result<Database, SemanticError> {
    let mut lists = BTreeMap::<(bool, u8), Vec<Datom>>::new();
    for reference in &manifest.segments {
        let segment = if let Some(segment) = cache.get(&reference.hash) {
            segment
        } else {
            let row = client
                .query_opt(
                    "SELECT payload FROM atomic_index_segments WHERE segment_hash = $1",
                    &[&&reference.hash[..]],
                )
                .map_err(|error| postgres_error("peer/index-segment", error))?
                .ok_or_else(|| {
                    fault(
                        "index/missing-segment",
                        "manifest references a missing segment",
                    )
                })?;
            let payload: Vec<u8> = row.get(0);
            if sha256(&payload) != reference.hash {
                return Err(fault(
                    "index/segment-checksum",
                    "segment row does not match its hash",
                ));
            }
            let decoded = Arc::new(decode_index_segment(&payload)?);
            cache.insert(reference.hash, Arc::clone(&decoded));
            decoded
        };
        if segment.order != reference.order
            || segment.history != reference.history
            || segment.datoms.len() != reference.count as usize
        {
            return Err(fault(
                "index/segment-reference-mismatch",
                "segment metadata disagrees with its manifest reference",
            ));
        }
        lists
            .entry((reference.history, order_tag(reference.order)))
            .or_default()
            .extend(segment.datoms.iter().cloned());
    }
    for ((_, tag), datoms) in &lists {
        let order = order_from_tag(*tag);
        if datoms
            .windows(2)
            .any(|pair| pair[0].cmp_in(&pair[1], order).is_gt())
        {
            return Err(fault(
                "index/cross-segment-order",
                "combined segments are not ordered",
            ));
        }
    }
    let current = lists.remove(&(false, 0)).unwrap_or_default();
    let history = lists.remove(&(true, 0)).unwrap_or_default();
    let mut history_chunks = vec![Vec::new(); manifest.basis_t as usize];
    for datom in history {
        if datom.tx == 0 || datom.tx > manifest.basis_t {
            return Err(fault(
                "index/datom-basis",
                "historical segment contains an out-of-range transaction",
            ));
        }
        history_chunks[(datom.tx - 1) as usize].push(datom);
    }
    let database = Database::from_index_base(
        manifest.schema.clone(),
        manifest.basis_t,
        manifest.next_eid,
        current,
        history_chunks,
        manifest.schema_history.clone(),
    )?;
    for history in [false, true] {
        for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
            let actual = lists
                .remove(&(history, order_tag(order)))
                .unwrap_or_default();
            let expected = database.datoms(
                if history {
                    View::History
                } else {
                    View::Current
                },
                order,
            );
            if actual != expected {
                return Err(fault(
                    "index/redundant-root-mismatch",
                    "persistent index orders disagree",
                ));
            }
        }
    }
    Ok(database)
}

fn apply_tail<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    database: &mut Database,
    previous_hash: &mut Digest,
    target: u64,
) -> Result<Vec<DurableTransaction>, SemanticError> {
    let after = sql_basis(database.basis_t())?;
    let through = sql_basis(target)?;
    let rows = client
        .query(
            "SELECT basis_t, previous_hash, tx_hash, payload FROM atomic_transactions \
         WHERE database_id = $1 AND basis_t > $2 AND basis_t <= $3 ORDER BY basis_t",
            &[&database_id, &after, &through],
        )
        .map_err(|error| postgres_error("peer/log-tail", error))?;
    if rows.len() != usize::try_from(target - database.basis_t()).unwrap_or(usize::MAX) {
        return Err(fault(
            "peer/missing-tail",
            "peer log tail is not contiguous",
        ));
    }
    let mut reports = Vec::with_capacity(rows.len());
    for row in rows {
        let basis = pg_basis(row.get(0), "tail transaction")?;
        let stored_previous = digest(row.get(1), "tail predecessor")?;
        let stored_hash = digest(row.get(2), "tail transaction hash")?;
        let payload: Vec<u8> = row.get(3);
        if basis != database.basis_t() + 1
            || stored_previous != *previous_hash
            || transaction_hash(&payload) != stored_hash
        {
            return Err(fault(
                "peer/invalid-tail-link",
                "peer log tail has a gap or hash mismatch",
            ));
        }
        let envelope = decode_transaction(&payload)?;
        if envelope.database_id != database_id
            || envelope.basis_t != basis
            || envelope.previous_hash != stored_previous
        {
            return Err(fault(
                "peer/tail-envelope-mismatch",
                "tail envelope disagrees with its row",
            ));
        }
        *database = database.apply_committed(&envelope)?;
        *previous_hash = stored_hash;
        reports.push(envelope);
    }
    Ok(reports)
}

fn index_member(database: &Database, datom: &Datom, order: IndexOrder) -> bool {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => true,
        IndexOrder::Avet => database
            .schema()
            .attribute(datom.attribute)
            .is_ok_and(|a| a.indexed || a.unique.is_some()),
        IndexOrder::Vaet => database
            .schema()
            .attribute(datom.attribute)
            .is_ok_and(|a| a.value_type == crate::ValueType::Ref),
    }
}

fn same_current_value(left: &Database, right: &Database) -> bool {
    left.basis_t() == right.basis_t()
        && left.next_eid() == right.next_eid()
        && crate::encode_schema(left.schema()).ok() == crate::encode_schema(right.schema()).ok()
        && [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ]
        .into_iter()
        .all(|order| left.datoms(View::Current, order) == right.datoms(View::Current, order))
}

fn read_head<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<(u64, Digest), SemanticError> {
    let row = client
        .query_opt(
            "SELECT basis_t, tx_hash FROM atomic_heads WHERE database_id = $1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("peer/head", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "postgres/database-not-found",
                format!("database {database_id} does not exist"),
            )
        })?;
    Ok((
        pg_basis(row.get(0), "head")?,
        digest(row.get(1), "head hash")?,
    ))
}

fn order_tag(order: IndexOrder) -> u8 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}
fn order_from_tag(tag: u8) -> IndexOrder {
    match tag {
        0 => IndexOrder::Eavt,
        1 => IndexOrder::Aevt,
        2 => IndexOrder::Avet,
        3 => IndexOrder::Vaet,
        _ => unreachable!(),
    }
}
fn sql_basis(value: u64) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "postgres/basis-out-of-range",
            "PostgreSQL BIGINT cannot represent this basis",
        )
    })
}
fn pg_basis(value: i64, record: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        fault(
            "index/negative-basis",
            format!("{record} contains a negative basis"),
        )
    })
}
fn digest(bytes: Vec<u8>, record: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|bytes: Vec<u8>| {
        fault(
            "index/invalid-digest",
            format!("{record} has {} bytes", bytes.len()),
        )
    })
}
fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{Attribute, Cardinality, Keyword, Schema, Value, ValueType};

    fn schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1,
                Keyword::new("db", "txInstant"),
                ValueType::Instant,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(Attribute::new(
                2,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            ))
            .unwrap();
        schema
    }

    #[test]
    fn canonical_segment_and_manifest_round_trip() {
        let segment = IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 10,
                attribute: 2,
                value: Value::String("Ada".into()),
                tx: 1,
                added: true,
            }],
        };
        let bytes = encode_index_segment(&segment).unwrap();
        assert_eq!(decode_index_segment(&bytes).unwrap(), segment);
        let manifest = IndexManifest {
            database_id: "db".into(),
            basis_t: 1,
            tx_hash: [3; 32],
            next_eid: 1000,
            schema: schema(),
            schema_history: vec![vec![]],
            segments: vec![SegmentRef {
                order: IndexOrder::Eavt,
                history: false,
                ordinal: 0,
                hash: sha256(&bytes),
                count: 1,
            }],
        };
        let encoded = encode_index_manifest(&manifest).unwrap();
        assert_eq!(
            hex(&sha256(&bytes)),
            "0cdfc38ca52d96e7230995075c5c3f41e8a9902954dd6dd375ccd19eb6284c06"
        );
        assert_eq!(
            hex(&sha256(&encoded)),
            "21d3896db5c43acf62a5d9963151a90477a874b9b6971a9f6953dea47bb9c7d9"
        );
        let decoded = decode_index_manifest(&encoded).unwrap();
        assert_eq!(encode_index_manifest(&decoded).unwrap(), encoded);
    }

    fn hex(bytes: &Digest) -> String {
        bytes.iter().map(|byte| format!("{byte:02x}")).collect()
    }

    #[test]
    fn malformed_segment_order_and_manifest_gaps_fail_closed() {
        let datom = |entity| Datom {
            entity,
            attribute: 2,
            value: Value::String("x".into()),
            tx: 1,
            added: true,
        };
        assert_eq!(
            encode_index_segment(&IndexSegment {
                order: IndexOrder::Eavt,
                history: true,
                datoms: vec![datom(2), datom(1)]
            })
            .unwrap_err()
            .code,
            "encoding/unsorted-index-segment"
        );
        let manifest = IndexManifest {
            database_id: "db".into(),
            basis_t: 1,
            tx_hash: [0; 32],
            next_eid: 1000,
            schema: schema(),
            schema_history: vec![vec![]],
            segments: vec![SegmentRef {
                order: IndexOrder::Eavt,
                history: false,
                ordinal: 1,
                hash: [0; 32],
                count: 1,
            }],
        };
        assert_eq!(
            encode_index_manifest(&manifest).unwrap_err().code,
            "encoding/index-segment-gap"
        );
    }

    #[test]
    fn immutable_cache_hits_and_eviction_do_not_change_values() {
        let first = Arc::new(IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 1,
                attribute: 2,
                value: Value::Long(3),
                tx: 1,
                added: true,
            }],
        });
        let second = Arc::new(IndexSegment {
            order: IndexOrder::Eavt,
            history: false,
            datoms: vec![Datom {
                entity: 2,
                attribute: 2,
                value: Value::Long(4),
                tx: 1,
                added: true,
            }],
        });
        let mut cache = SegmentCache::new(1);
        cache.insert([1; 32], Arc::clone(&first));
        assert_eq!(cache.get(&[1; 32]).unwrap().datoms, first.datoms);
        cache.insert([2; 32], Arc::clone(&second));
        assert!(cache.get(&[1; 32]).is_none());
        assert_eq!(cache.get(&[2; 32]).unwrap().datoms, second.datoms);
        assert_eq!(
            cache.stats,
            CacheStats {
                hits: 2,
                misses: 1,
                evictions: 1
            }
        );
    }
}
