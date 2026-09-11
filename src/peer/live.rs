//! Live observation over the same immutable block snapshots used by writers.
//! The updater serializes observations, never queries or lazy snapshot reads.
use super::{PeerIndexCursor, PeerLoadStats, PeerSnapshot, lock, transaction_hash};
use crate::index::cursor::MergeSource;
use crate::index::recent::{RecentLimits, RecentStats};
use crate::storage::{BlockDatabase, BlockReadConfig, BlockReader, BlockSnapshot};
use crate::{
    CacheStats, DatabaseIdentity, DatabaseValue, Datom, Digest, Entity, EntityIdentifier,
    ErrorCategory, IndexOrder, IndexPrefix, Keyword, PostgresConnectionConfig, PullPattern, Query,
    QueryControl, QueryExtensions, QueryInput, QueryOutcome, QueryValue, SemanticError,
    ServiceTransactionReport,
};
use std::collections::VecDeque;
#[cfg(test)]
use std::sync::MutexGuard;
use std::sync::{Arc, Condvar, Mutex, RwLock};
use std::time::{Duration, Instant};

#[derive(Clone)]
pub struct Peer {
    core: Arc<Core>,
}

struct Core {
    reader: BlockReader,
    reference: String,
    route: [u8; 16],
    identity: DatabaseIdentity,
    current: RwLock<State>,
    observed_basis: Mutex<u64>,
    state_advanced: Condvar,
    update: Mutex<()>,
    reports: Mutex<Option<VecDeque<ServiceTransactionReport>>>,
    report_ready: Condvar,
}

#[derive(Clone)]
struct State {
    snapshot: BlockSnapshot,
    publication_revision: u64,
    observation_generation: u64,
}

impl Peer {
    pub fn connect(
        connection: &str,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured(
            &PostgresConnectionConfig::parse(connection)?,
            database_id,
            cache_capacity,
        )
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            connection,
            database_id,
            cache_capacity,
            cache_capacity.saturating_mul(512 * 1024),
        )
    }

    pub fn connect_with_cache_limits(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_cache_limits(
            &PostgresConnectionConfig::parse(connection)?,
            database_id,
            cache_entries,
            cache_bytes,
        )
    }

    pub fn connect_configured_with_cache_limits(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
    ) -> Result<Self, SemanticError> {
        let database = BlockDatabase::resolve(connection, &database_id.into())?;
        Self::open(connection, database, cache_entries, cache_bytes, None)
    }

    pub fn connect_with_limits(
        connection: &str,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured_with_limits(
            &PostgresConnectionConfig::parse(connection)?,
            database_id,
            cache_entries,
            cache_bytes,
            recent_limits,
        )
    }

    pub fn connect_configured_with_limits(
        connection: &PostgresConnectionConfig,
        database_id: impl Into<String>,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: RecentLimits,
    ) -> Result<Self, SemanticError> {
        let database = BlockDatabase::resolve(connection, &database_id.into())?;
        Self::open(
            connection,
            database,
            cache_entries,
            cache_bytes,
            Some(recent_limits),
        )
    }

    pub(crate) fn connect_identity_configured(
        connection: &PostgresConnectionConfig,
        identity: &DatabaseIdentity,
        cache_entries: usize,
    ) -> Result<Self, SemanticError> {
        let identity_bytes = parse_identity(identity.lineage_id())?;
        let peer = Self::open(
            connection,
            BlockDatabase {
                identity: identity_bytes,
                route: parse_identity(identity.database_id())?,
                name: String::new(),
            },
            cache_entries,
            cache_entries.saturating_mul(512 * 1024),
            None,
        )?;
        if peer.identity() != *identity {
            return Err(identity_error());
        }
        Ok(peer)
    }

    fn open(
        connection: &PostgresConnectionConfig,
        database: BlockDatabase,
        cache_entries: usize,
        cache_bytes: usize,
        recent_limits: Option<RecentLimits>,
    ) -> Result<Self, SemanticError> {
        let mut reads = BlockReadConfig {
            cache_entries,
            cache_bytes,
            ..BlockReadConfig::default()
        };
        if let Some(recent_limits) = recent_limits {
            if recent_limits.soft_bytes == 0
                || recent_limits.soft_datoms == 0
                || recent_limits.soft_bytes > recent_limits.hard_bytes
                || recent_limits.soft_datoms > recent_limits.hard_datoms
            {
                return Err(SemanticError::incorrect(
                    "recent/invalid-limits",
                    "Recent soft limits must be positive and not exceed hard limits",
                ));
            }
            reads.max_recent_bytes =
                usize::try_from(recent_limits.hard_bytes).unwrap_or(usize::MAX);
            reads.max_recent_datoms =
                usize::try_from(recent_limits.hard_datoms).unwrap_or(usize::MAX);
        }
        let reader = BlockReader::connect(connection, reads)?;
        let reference = database.reference_key();
        let snapshot = reader.capture(&reference)?;
        if snapshot.captured_root().identity != database.identity {
            return Err(identity_error());
        }
        let id = crate::storage::catalog::identity_string(database.identity);
        let revision = snapshot.publication_revision().ok_or_else(|| {
            fault(
                "peer/publication-coordinate",
                "Publication capture has no reference revision",
            )
        })?;
        let initial_basis = snapshot.basis_t();
        Ok(Self {
            core: Arc::new(Core {
                reader,
                reference,
                route: database.route,
                identity: DatabaseIdentity::new(
                    crate::storage::catalog::identity_string(database.route),
                    id,
                ),
                current: RwLock::new(State {
                    snapshot,
                    publication_revision: revision,
                    observation_generation: 0,
                }),
                observed_basis: Mutex::new(initial_basis),
                state_advanced: Condvar::new(),
                update: Mutex::new(()),
                reports: Mutex::new(None),
                report_ready: Condvar::new(),
            }),
        })
    }

    fn state(&self) -> State {
        self.core
            .current
            .read()
            .unwrap_or_else(|e| e.into_inner())
            .clone()
    }

    fn publish(&self, state: State) {
        let mut observed = lock(&self.core.observed_basis);
        let basis = state.snapshot.basis_t();
        *self.core.current.write().unwrap_or_else(|e| e.into_inner()) = state;
        *observed = basis;
        self.core.state_advanced.notify_all();
    }

    /// Wait only on local adoption, independently of update/storage locks.
    pub(crate) fn wait_for_local_basis(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<(), SemanticError> {
        let start = Instant::now();
        let mut observed = lock(&self.core.observed_basis);
        while *observed < target {
            let remaining = timeout.saturating_sub(start.elapsed());
            if remaining.is_zero() {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "connection/observation-timeout",
                    "Transaction committed but local observation has not caught up",
                )
                .detail("basis_t", target.to_string()));
            }
            observed = self
                .core
                .state_advanced
                .wait_timeout(observed, remaining)
                .unwrap_or_else(|e| e.into_inner())
                .0;
        }
        Ok(())
    }

    pub fn db(&self) -> DatabaseValue {
        self.database_value()
    }
    pub fn database_value(&self) -> DatabaseValue {
        self.state().snapshot.database_value()
    }
    pub fn snapshot(&self) -> PeerSnapshot {
        let state = self.state();
        PeerSnapshot {
            native: state.snapshot,
            publication_revision: state.publication_revision,
        }
    }
    pub fn identity(&self) -> DatabaseIdentity {
        self.core.identity.clone()
    }
    pub fn basis_t(&self) -> u64 {
        self.state().snapshot.basis_t()
    }
    pub fn durable_base_t(&self) -> u64 {
        self.state().snapshot.indexed_basis_t()
    }
    pub fn durable_base_revision(&self) -> u64 {
        self.state().publication_revision
    }
    pub fn generation(&self) -> u64 {
        self.state().observation_generation
    }
    pub fn excision_generation(&self) -> u64 {
        self.state().snapshot.generation()
    }
    pub fn recent_stats(&self) -> RecentStats {
        self.state().snapshot.recent_tier().stats()
    }
    pub fn log(&self) -> crate::LogValue {
        self.state().snapshot.log()
    }
    pub fn cache_stats(&self) -> CacheStats {
        self.core.reader.cache_stats()
    }
    pub fn ssd_cache_stats(&self) -> crate::SsdCacheStats {
        self.core.reader.ssd_cache_stats()
    }
    pub fn node_block_read_stats(&self) -> crate::NodeBlockReadStats {
        self.core.reader.node_block_read_stats()
    }
    pub fn load_stats(&self) -> PeerLoadStats {
        self.core.reader.load_stats()
    }
    pub fn purge_ssd_generation(&self, generation: u64) -> bool {
        self.core
            .reader
            .purge_ssd_generation(self.state().snapshot.captured_root().identity, generation)
    }
    pub fn reopen_snapshot(
        &self,
        reference: &crate::SnapshotReference,
    ) -> Result<DatabaseValue, SemanticError> {
        if reference.database_id() != self.core.identity.database_id() {
            return Err(identity_error());
        }
        reference.open_with_reader(&self.core.reader)
    }
    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        self.database_value().entity(identifier)
    }
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value().query(query, inputs, control)
    }
    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        self.database_value()
            .query_with_extensions(query, inputs, control, extensions)
    }
    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        self.database_value().pull(pattern, entity)
    }

    fn sync_once(&self) -> Result<PeerSnapshot, SemanticError> {
        let _update = lock(&self.core.update);
        let previous = self.state();
        let Some(next) = self
            .core
            .reader
            .capture_changed(&self.core.reference, &previous.snapshot)?
        else {
            return Ok(PeerSnapshot {
                native: previous.snapshot,
                publication_revision: previous.publication_revision,
            });
        };
        if next.captured_root().identity != previous.snapshot.captured_root().identity {
            return Err(identity_error());
        }
        if next.basis_t() < previous.snapshot.basis_t() {
            return Err(fault(
                "peer/basis-regressed",
                "Publication regressed the observed basis",
            ));
        }
        if next.generation() < previous.snapshot.generation() {
            return Err(fault(
                "peer/generation-regressed",
                "Publication regressed the observed generation",
            ));
        }
        let revision = next.publication_revision().ok_or_else(|| {
            fault(
                "peer/publication-coordinate",
                "Publication capture has no reference revision",
            )
        })?;
        let mut reports = Vec::new();
        if self.tx_reports_enabled() {
            let mut through = previous.snapshot.basis_t();
            let source = self
                .core
                .reader
                .report_publication(&self.core.reference, &previous.snapshot)?;
            for generation in previous.snapshot.generation()..next.generation() {
                let (source, end) = self.core.reader.report_handoff(
                    &source,
                    self.core.route,
                    next.captured_root().identity,
                    previous.snapshot.generation(),
                    generation,
                )?;
                if end < through || end > next.basis_t() {
                    return Err(fault(
                        "peer/report-handoff-order",
                        "Excision handoff does not extend the observed transaction prefix",
                    ));
                }
                self.collect_reports(&source, generation, through, end, &mut reports)?;
                through = end;
            }
            let source = self
                .core
                .reader
                .report_publication(&self.core.reference, &next)?;
            self.collect_reports(
                &source,
                next.generation(),
                through,
                next.basis_t(),
                &mut reports,
            )?;
        }
        self.publish(State {
            snapshot: next.clone(),
            publication_revision: revision,
            observation_generation: previous.observation_generation.saturating_add(1),
        });
        if !reports.is_empty()
            && let Some(queue) = lock(&self.core.reports).as_mut()
        {
            for mut report in reports {
                report.replayed = false;
                queue.push_back(report);
            }
            self.core.report_ready.notify_all();
        }
        Ok(PeerSnapshot {
            native: next,
            publication_revision: revision,
        })
    }

    fn collect_reports(
        &self,
        source: &crate::storage::snapshot::RootCapture,
        generation: u64,
        after_basis: u64,
        through_basis: u64,
        reports: &mut Vec<ServiceTransactionReport>,
    ) -> Result<(), SemanticError> {
        for basis in after_basis.saturating_add(1)..=through_basis {
            let report = self.core.reader.exact_report_from_capture(source, basis)?;
            let before = direct_report_value(&report.db_before)?;
            let after = direct_report_value(&report.db_after)?;
            if before.generation() != generation
                || after.generation() != generation
                || after.lineage_id() != self.core.identity.lineage_id()
            {
                return Err(fault(
                    "peer/report-generation-changed",
                    "Exact report differs from its retained publication generation",
                ));
            }
            reports.push(report);
        }
        Ok(())
    }

    pub fn sync_snapshot(&self) -> Result<PeerSnapshot, SemanticError> {
        match self.sync_once() {
            Err(error) if error.category == ErrorCategory::Unavailable => {
                self.reconnect()?;
                self.sync_once()
            }
            result => result,
        }
    }
    pub fn sync(&self) -> Result<DatabaseValue, SemanticError> {
        self.sync_database_value()
    }
    pub fn sync_database_value(&self) -> Result<DatabaseValue, SemanticError> {
        Ok(self.sync_snapshot()?.database_value())
    }
    pub fn reconnect(&self) -> Result<(), SemanticError> {
        self.core.reader.reconnect()
    }
    pub fn refresh_index(&self) -> Result<bool, SemanticError> {
        let _update = lock(&self.core.update);
        let previous = self.state();
        let Some(next) = self
            .core
            .reader
            .capture_changed(&self.core.reference, &previous.snapshot)?
        else {
            return Ok(false);
        };
        // Physical refresh never advances logical time. A caller that wants
        // newer transactions uses sync; a newer-than-value base is not usable.
        if next.basis_t() != previous.snapshot.basis_t()
            || (next.captured_root().indexes == previous.snapshot.captured_root().indexes
                && next.generation() == previous.snapshot.generation())
        {
            return Ok(false);
        }
        let revision = next.publication_revision().ok_or_else(|| {
            fault(
                "peer/publication-coordinate",
                "Publication capture has no reference revision",
            )
        })?;
        self.publish(State {
            snapshot: next,
            publication_revision: revision,
            observation_generation: previous.observation_generation.saturating_add(1),
        });
        Ok(true)
    }
    pub fn sync_to(&self, target: u64, timeout: Duration) -> Result<DatabaseValue, SemanticError> {
        self.sync_to_database_value(target, timeout)
    }
    pub fn sync_to_snapshot(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<PeerSnapshot, SemanticError> {
        self.wait_for(target, timeout, "peer/sync-timeout", |_| true)
    }
    pub fn sync_to_database_value(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        Ok(self.sync_to_snapshot(target, timeout)?.database_value())
    }
    pub fn sync_index(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        Ok(self
            .wait_for(target, timeout, "peer/sync-index-timeout", |snapshot| {
                snapshot.indexed_basis_t() >= target
                    && snapshot.index_descriptor().avet_work.is_empty()
            })?
            .database_value())
    }
    pub fn sync_schema(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        Ok(self
            .wait_for(target, timeout, "peer/sync-schema-timeout", |snapshot| {
                snapshot.avet_unready().is_empty()
            })?
            .database_value())
    }
    pub fn sync_excise(
        &self,
        target: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        let started = Instant::now();
        loop {
            let snapshot = self.sync_snapshot()?;
            if snapshot.basis_t() >= target
                && crate::storage::excision::sync_complete(&snapshot.native, target)?
            {
                return Ok(snapshot.database_value());
            }
            let remaining = timeout.saturating_sub(started.elapsed());
            if remaining.is_zero() {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "peer/excision-not-ready",
                    "Timed out waiting for committed excision completion",
                ));
            }
            std::thread::sleep(remaining.min(Duration::from_millis(10)));
        }
    }
    fn wait_for(
        &self,
        target: u64,
        timeout: Duration,
        code: &'static str,
        ready: impl Fn(&BlockSnapshot) -> bool,
    ) -> Result<PeerSnapshot, SemanticError> {
        let started = Instant::now();
        loop {
            match self.sync_snapshot() {
                Ok(snapshot) if snapshot.basis_t() >= target && ready(&snapshot.native) => {
                    return Ok(snapshot);
                }
                Ok(_) => {}
                Err(error)
                    if error.category == ErrorCategory::Unavailable
                        || error.category == ErrorCategory::Conflict => {}
                Err(error) => return Err(error),
            }
            let remaining = timeout.saturating_sub(started.elapsed());
            if remaining.is_zero() {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    code,
                    "Timed out waiting for the requested database frontier",
                )
                .detail("target_t", target.to_string()));
            }
            std::thread::sleep(remaining.min(Duration::from_millis(10)));
        }
    }

    pub fn enable_tx_reports(&self) -> bool {
        let _update = lock(&self.core.update);
        let mut reports = lock(&self.core.reports);
        if reports.is_some() {
            return false;
        }
        *reports = Some(VecDeque::new());
        true
    }
    pub fn remove_tx_reports(&self) -> bool {
        let _update = lock(&self.core.update);
        let removed = lock(&self.core.reports).take().is_some();
        self.core.report_ready.notify_all();
        removed
    }
    pub fn tx_reports_enabled(&self) -> bool {
        lock(&self.core.reports).is_some()
    }
    pub fn take_tx_reports(&self) -> Vec<ServiceTransactionReport> {
        let _update = lock(&self.core.update);
        lock(&self.core.reports)
            .as_mut()
            .map_or_else(Vec::new, |queue| queue.drain(..).collect())
    }
    pub fn try_next_tx_report(&self) -> Option<ServiceTransactionReport> {
        lock(&self.core.reports)
            .as_mut()
            .and_then(VecDeque::pop_front)
    }
    pub fn next_tx_report(
        &self,
        timeout: Duration,
    ) -> Result<Option<ServiceTransactionReport>, SemanticError> {
        let start = Instant::now();
        let mut slot = lock(&self.core.reports);
        loop {
            let Some(queue) = slot.as_mut() else {
                return Ok(None);
            };
            if let Some(report) = queue.pop_front() {
                return Ok(Some(report));
            }
            let remaining = timeout.saturating_sub(start.elapsed());
            if remaining.is_zero() {
                return Ok(None);
            }
            let (next, timed) = self
                .core
                .report_ready
                .wait_timeout(slot, remaining)
                .unwrap_or_else(|e| e.into_inner());
            slot = next;
            if timed.timed_out() {
                return Ok(None);
            }
        }
    }

    /// Local receipt observation never performs storage I/O after acknowledgement.
    pub(crate) fn adopt_committed_report(
        &self,
        report: &ServiceTransactionReport,
    ) -> Result<DatabaseValue, SemanticError> {
        let before = direct_report_value(&report.db_before)?;
        let after = direct_report_value(&report.db_after)?;
        if before.lineage_id() != self.core.identity.lineage_id()
            || after.lineage_id() != before.lineage_id()
        {
            return Err(identity_error());
        }
        if before.basis_t().checked_add(1) != Some(report.basis_t)
            || after.basis_t() != report.basis_t
            || transaction_hash(&after) != report.tx_hash
            || before.generation() != after.generation()
        {
            return Err(fault(
                "peer/report-coordinate",
                "Receipt values do not form their claimed successor",
            ));
        }
        let _update = lock(&self.core.update);
        let previous = self.state();
        if previous.snapshot.basis_t() > report.basis_t {
            return Ok(previous.snapshot.database_value());
        }
        if previous.snapshot.basis_t() == report.basis_t {
            if transaction_hash(&previous.snapshot) != report.tx_hash {
                return Err(fault(
                    "peer/report-fork",
                    "Receipt conflicts with the observed basis",
                ));
            }
            return Ok(previous.snapshot.database_value());
        }
        if previous.snapshot.basis_t() != before.basis_t()
            || transaction_hash(&previous.snapshot) != transaction_hash(&before)
            || previous.snapshot.generation() != before.generation()
        {
            return Err(fault(
                "peer/report-gap",
                "Receipt is not a contiguous observed successor",
            ));
        }
        let after = self.core.reader.adopt_snapshot(&after);
        self.publish(State {
            snapshot: after.clone(),
            publication_revision: previous.publication_revision,
            observation_generation: previous.observation_generation.saturating_add(1),
        });
        if !report.replayed
            && let Some(queue) = lock(&self.core.reports).as_mut()
        {
            queue.push_back(report.clone());
            self.core.report_ready.notify_one();
        }
        Ok(after.database_value())
    }

    #[cfg(unix)]
    pub(crate) fn open_socket_report(
        &self,
        wire: crate::encoding::WireReport,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        let mut report = self
            .core
            .reader
            .exact_report(&self.core.reference, wire.after.basis_t)?;
        let matches = |value: &DatabaseValue,
                       endpoint: &crate::encoding::ExactEndpoint,
                       root: Digest|
         -> Result<bool, SemanticError> {
            let (snapshot, ..) = value.committed_block_parts()?;
            let key = value.snapshot_key()?;
            let same_root = if snapshot.storage_root_id() == root {
                true
            } else {
                let bytes = self.core.reader.read_object(root)?;
                let block = crate::storage::root::Block::decode(&root, &bytes)?;
                let claimed = match block.kind {
                    crate::storage::root::DATABASE_ROOT_KIND => {
                        crate::storage::root::DatabaseValueRoot::from(
                            &crate::storage::root::DatabaseRoot::decode(&root, &bytes)?,
                        )
                    }
                    crate::storage::root::DATABASE_VALUE_ROOT_KIND => {
                        crate::storage::root::DatabaseValueRoot::decode(&root, &bytes)?
                    }
                    _ => return Ok(false),
                };
                claimed == *snapshot.captured_root()
            };
            Ok(same_root
                && key.basis_t() == endpoint.basis_t
                && key.generation() == endpoint.generation
                && key.transaction_hash() == endpoint.tx_hash
                && key.state_hash() == endpoint.state_hash
                && key.eidx_frontier() == endpoint.eidx_frontier)
        };
        if !matches(&report.db_before, &wire.before, wire.before_manifest)?
            || !matches(&report.db_after, &wire.after, wire.after_manifest)?
            || report.tx_data != wire.tx_data
            || report.tempids != wire.tempids
        {
            return Err(fault(
                "transport/report-coordinate",
                "Transport receipt differs from its authenticated outcome",
            ));
        }
        report.replayed = wire.replayed;
        Ok(report)
    }

    #[cfg(test)]
    pub(crate) fn pause_updates_for_test(&self) -> MutexGuard<'_, ()> {
        lock(&self.core.update)
    }
}

fn direct_report_value(value: &DatabaseValue) -> Result<BlockSnapshot, SemanticError> {
    let (snapshot, as_of, since, history) = value.committed_block_parts()?;
    if as_of.is_some() || since.is_some() || history {
        return Err(fault(
            "peer/report-value",
            "Receipt requires direct committed values",
        ));
    }
    Ok(snapshot)
}
fn fault(code: &'static str, message: &'static str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
fn identity_error() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Forbidden,
        "peer/identity-mismatch",
        "Captured database identity does not match the publication",
    )
}

fn parse_identity(text: &str) -> Result<[u8; 16], SemanticError> {
    if text.len() != 36 {
        return Err(identity_error());
    }
    let compact = text.replace('-', "");
    let bytes = u128::from_str_radix(&compact, 16)
        .map_err(|_| identity_error())?
        .to_be_bytes();
    if bytes == [0; 16] || crate::storage::catalog::identity_string(bytes) != text {
        return Err(identity_error());
    }
    Ok(bytes)
}
