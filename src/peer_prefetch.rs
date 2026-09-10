//! Optional hint reads have their own I/O and pin lanes. A stalled advisory
//! fetch must never monopolize the writer's synchronous driver connection.
use super::*;

/// Captured immutable data only: no original read core, driver or pin ownership.
/// A delayed worker must not become the last owner of a writer pin and perform
/// synchronous unlock/reconnect under that writer's pin-lane mutex on drop.
pub(crate) struct HintReadPlan {
    database_id: String,
    lineage_id: String,
    connection: PostgresConnectionConfig,
    recent_limits: RecentLimits,
    tree_cache: TreeNodeCache,
    ssd_namespace: Digest,
    basis_t: u64,
    eidx_frontier: u64,
    current_hash: Digest,
    current_state_hash: Digest,
    excision_generation: u64,
    durable_base_t: u64,
    tree_base: Option<Arc<TreeBase>>,
    recent: Arc<RecentTier>,
    metadata: Arc<MetadataProjection>,
    avet_unready: Arc<BTreeSet<u32>>,
    generation: u64,
}

impl DatabaseValue {
    /// Capture before spawning, while the writer still owns its current state.
    /// No I/O or pin acquisition; the worker later acquires its own retention.
    pub(crate) fn hint_prefetch_plan(&self) -> Result<HintReadPlan, SemanticError> {
        let (source, ..) = self.snapshot_parts()?;
        Ok(HintReadPlan {
            database_id: source.core.database_id.clone(),
            lineage_id: source.core.lineage_id.clone(),
            connection: source.core.connection.clone(),
            recent_limits: source.core.recent_limits,
            tree_cache: source.core.tree_cache.clone(),
            ssd_namespace: source.core.ssd_namespace,
            basis_t: source.state.basis_t,
            eidx_frontier: source.state.eidx_frontier,
            current_hash: source.state.current_hash,
            current_state_hash: source.state.current_state_hash,
            excision_generation: source.state.excision_generation,
            durable_base_t: source.state.durable_base_t,
            tree_base: source.state.tree_base.clone(),
            recent: source.state.recent.clone(),
            metadata: source.state.metadata.clone(),
            avet_unready: source.state.avet_unready.clone(),
            generation: source.state.generation,
        })
    }
}

impl HintReadPlan {
    /// Called inside the bounded background worker, never the serialized writer.
    /// Tightened I/O policy is not a guarantee of cancelable DNS/startup/driver I/O.
    pub(crate) fn open(self, timeout: Duration) -> Result<DatabaseValue, SemanticError> {
        if timeout.is_zero() {
            return Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "hints/deadline",
                "prefetch deadline elapsed",
            ));
        }
        let mut policy = self.connection.io_policy().clone();
        fn tighten(current: Option<Duration>, limit: Duration) -> Option<Duration> {
            Some(current.map_or(limit, |current| current.min(limit)))
        }
        // Millisecond-based PG policy is capped by its representable range.
        let timeout = timeout.min(Duration::from_secs(30));
        policy.connect_timeout = tighten(policy.connect_timeout, timeout);
        policy.statement_timeout = tighten(policy.statement_timeout, timeout);
        policy.lock_timeout = tighten(policy.lock_timeout, timeout);
        policy.tcp_user_timeout = tighten(policy.tcp_user_timeout, timeout);
        let connection = self.connection.with_io_policy(policy)?;
        let mut client = connection.connect_for("hints/read-connect")?;
        verify_database_lineage(&mut client, &self.database_id, &self.lineage_id)?;
        let root_pins = RootPinManager::connect(&connection, &self.database_id, &self.lineage_id)?;
        let generation_pin = root_pins.acquire_generation(self.excision_generation)?;
        let root_pin = root_pins.acquire(self.tree_base.as_deref())?;
        let state = TieredState {
            basis_t: self.basis_t,
            eidx_frontier: self.eidx_frontier,
            current_hash: self.current_hash,
            current_state_hash: self.current_state_hash,
            excision_generation: self.excision_generation,
            durable_base_t: self.durable_base_t,
            tree_base: self.tree_base,
            _root_pin: root_pin,
            _generation_pin: generation_pin,
            recent: self.recent,
            metadata: self.metadata,
            avet_unready: self.avet_unready,
            generation: self.generation,
        };
        let cache = self.tree_cache;
        let core = Arc::new(TieredReadCore {
            database_id: self.database_id,
            lineage_id: self.lineage_id,
            connection,
            recent_limits: self.recent_limits,
            load_counters: PeerLoadCounters::default(),
            root_pins,
            programs: Arc::new(Mutex::new(crate::postgres::ProgramCache::default())),
            tree_cache: cache.clone(),
            fulltext_cache: crate::fulltext_store::FulltextCache::new(cache.max_entries, cache.max_bytes),
            tree_node_miss: Mutex::new(None),
            // Filesystem cache operations have no interruptible deadline. Hints
            // warm only the shared bounded RAM node cache through authenticated PG.
            ssd_cache: crate::SsdCache::disabled(),
            ssd_namespace: self.ssd_namespace,
            block_reads: Mutex::new(crate::NodeBlockReadStats::default()),
            io: Mutex::new(PeerIo {
                client,
                tree_cache: cache,
            }),
        });
        Ok(TieredSnapshot {
            core,
            state: Arc::new(state),
        }
        .database_value())
    }
}
