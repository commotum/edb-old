//! A single cooperatively scheduled excision job. The synchronous operator
//! and automatic service share the authenticated generation rewrite driver.
use super::*;
use crate::postgres::TransactorLease;
use std::sync::{
    atomic::{AtomicBool, Ordering},
    mpsc,
};
use std::thread::{self, JoinHandle};

/// Admission for rare, potentially whole-database maintenance. The account is
/// a policy envelope (1 MiB + 64 times distinct canonical source bytes), not
/// allocator RSS or a proof of peak heap usage. Eager replay/build phases are
/// admitted before entry; cancellation is cooperative between their steps.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ExcisionConfig {
    pub enabled: bool,
    pub max_admitted_bytes: u64,
    pub log_batch_transactions: usize,
}
impl Default for ExcisionConfig {
    fn default() -> Self {
        Self {
            enabled: true,
            max_admitted_bytes: 512 * 1024 * 1024,
            log_batch_transactions: 256,
        }
    }
}
impl ExcisionConfig {
    pub(crate) fn validate(self) -> Result<Self, SemanticError> {
        if self.max_admitted_bytes == 0
            || self.log_batch_transactions == 0
            || self.log_batch_transactions > 4096
        {
            return Err(SemanticError::incorrect(
                "excision/invalid-config",
                "excision bytes must be positive and each log batch must contain 1..=4096 transactions",
            ));
        }
        Ok(self)
    }
}

#[derive(Clone, Debug, Default, Eq, PartialEq)]
pub struct ExcisionProgress {
    pub phase: &'static str,
    pub steps: u64,
    /// Loaded source rows, including capture and resumed replay visits.
    pub source_transactions: u64,
    pub rewritten_transactions: u64,
    pub source_payload_bytes: u64,
    pub peak_admitted_bytes: u64,
    /// Rows advanced by completion staging and build cleanup, not total SQL writes.
    pub rows_staged: u64,
    pub complete: bool,
}

pub(crate) struct ExcisionWork {
    pub config: ExcisionConfig,
    pub lease: Option<TransactorLease>,
    progress: ExcisionProgress,
    stop: Arc<AtomicBool>,
    gate: Option<(mpsc::Receiver<()>, mpsc::SyncSender<JobEvent>)>,
    admitted_basis: u64,
    admitted_payload: u64,
}
impl ExcisionWork {
    pub fn operator() -> Self {
        Self {
            config: ExcisionConfig {
                max_admitted_bytes: u64::MAX,
                ..ExcisionConfig::default()
            },
            lease: None,
            progress: ExcisionProgress::default(),
            stop: Arc::new(AtomicBool::new(false)),
            gate: None,
            admitted_basis: 0,
            admitted_payload: 0,
        }
    }
    pub fn check(&self) -> Result<(), SemanticError> {
        if self.stop.load(Ordering::Acquire) {
            Err(SemanticError::new(
                crate::ErrorCategory::Interrupted,
                "excision/stopped",
                "excision stopped at a cooperative boundary; durable progress remains resumable",
            ))
        } else {
            Ok(())
        }
    }
    pub fn admit(&mut self, phase: &'static str, bytes: u64) -> Result<(), SemanticError> {
        self.check()?;
        self.progress.phase = phase;
        self.progress.peak_admitted_bytes = self.progress.peak_admitted_bytes.max(bytes);
        if bytes > self.config.max_admitted_bytes {
            return Err(SemanticError::new(
                crate::ErrorCategory::Busy,
                "excision/admission-capacity",
                "excision eager phase exceeds its configured accounted-byte allowance",
            )
            .detail("phase", phase)
            .detail("required_bytes", bytes.to_string())
            .detail("maximum_bytes", self.config.max_admitted_bytes.to_string()));
        }
        Ok(())
    }
    pub fn checkpoint(&mut self, phase: &'static str) -> Result<(), SemanticError> {
        self.check()?;
        self.progress.phase = phase;
        self.progress.steps += 1;
        if let Some((commands, events)) = &self.gate {
            events
                .send(JobEvent::Progress(self.progress.clone()))
                .map_err(|_| stopped())?;
            commands.recv().map_err(|_| stopped())?;
        }
        self.check()
    }
    pub fn source_rows(&mut self, count: usize, bytes: u64, rewritten: bool) {
        self.progress.source_transactions += count as u64;
        self.progress.source_payload_bytes += bytes;
        if rewritten {
            self.progress.rewritten_transactions += count as u64;
        }
    }
    pub fn staged(&mut self, rows: u64) {
        self.progress.rows_staged += rows;
    }
    pub fn admitted_basis(&self) -> u64 {
        self.admitted_basis
    }
    pub fn admit_source(&mut self, basis: u64, additional_bytes: u64) -> Result<(), SemanticError> {
        let total = self.admitted_payload.saturating_add(additional_bytes);
        self.admit(
            "source-and-candidate",
            total.saturating_mul(64).saturating_add(1024 * 1024),
        )?;
        self.admitted_basis = self.admitted_basis.max(basis);
        self.admitted_payload = total;
        Ok(())
    }
    pub fn authorize<C: GenericClient>(&self, c: &mut C, id: &str) -> Result<(), SemanticError> {
        self.check()?;
        if let Some(lease) = &self.lease {
            c.query_one(
                "SELECT atomic_assert_excision_worker($1,$2,$3)",
                &[
                    &id,
                    &lease.holder_id,
                    &sql_u64(lease.epoch, "worker epoch")?,
                ],
            )
            .map_err(|e| operation_error("excision/worker-authority", e))?;
        }
        Ok(())
    }
    #[allow(clippy::too_many_arguments)]
    pub fn action<C: GenericClient>(
        &self,
        c: &mut C,
        id: &str,
        generation: u64,
        action: &str,
        basis: u64,
        head: Option<Digest>,
        state: Option<Digest>,
        manifest: Option<Digest>,
    ) -> Result<Option<(u64, bool)>, SemanticError> {
        self.check()?;
        let Some(lease) = &self.lease else {
            return Ok(None);
        };
        let row = c.query_one("SELECT rows_advanced,is_complete FROM atomic_runtime_excision_step($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)",
            &[&id,&sql_u64(generation,"generation")?,&lease.holder_id,&sql_u64(lease.epoch,"worker epoch")?,&action,&EXCISION_COMPLETION_BATCH,&sql_u64(basis,"basis")?,&head.as_ref().map(|v|v.as_slice()),&state.as_ref().map(|v|v.as_slice()),&manifest.as_ref().map(|v|v.as_slice())]).map_err(|e|operation_error("excision/runtime-step",e))?;
        Ok(Some((
            positive_or_zero(row.get(0), "excision rows")?,
            row.get(1),
        )))
    }
}
fn stopped() -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Interrupted,
        "excision/stopped",
        "excision job owner stopped",
    )
}
enum JobEvent {
    Progress(ExcisionProgress),
    Finished(
        Result<Option<ExcisionReceipt>, SemanticError>,
        ExcisionProgress,
    ),
}
pub(crate) enum ExcisionStep {
    Progress(ExcisionProgress),
    Complete(Option<ExcisionReceipt>),
}
/// A rendezvous keeps the rewrite stack and its authenticated state alive
/// between steps. There is at most one worker, one permit and one event; no
/// queued whole-database jobs or repeated replay on ordinary scheduler ticks.
pub(crate) struct ExcisionJob {
    permits: mpsc::SyncSender<()>,
    events: mpsc::Receiver<JobEvent>,
    stop: Arc<AtomicBool>,
    thread: Option<JoinHandle<()>>,
    progress: ExcisionProgress,
}
impl ExcisionJob {
    pub fn start(
        connection: PostgresConnectionConfig,
        lease: TransactorLease,
        config: ExcisionConfig,
    ) -> Result<Self, SemanticError> {
        let config = config.validate()?;
        let (permits, commands) = mpsc::sync_channel(1);
        let (events, receiver) = mpsc::sync_channel(1);
        let stop = Arc::new(AtomicBool::new(false));
        let stopping = stop.clone();
        let operation = crate::OperationContext::current_or_process();
        let thread = thread::Builder::new()
            .name("atomic-excision".into())
            .spawn(move || {
                let _operation = operation.enter();
                if commands.recv().is_err() {
                    return;
                }
                let id = lease.database_id.clone();
                let mut work = ExcisionWork {
                    config,
                    lease: Some(lease),
                    progress: ExcisionProgress::default(),
                    stop: stopping,
                    gate: Some((commands, events.clone())),
                    admitted_basis: 0,
                    admitted_payload: 0,
                };
                let result = (|| {
                    let mut client = connection.connect_for("excision/worker-connect")?;
                    work.authorize(&mut client, &id)?;
                    if !has_pending(&mut client, &connection, &id, &mut work)? {
                        return Ok(None);
                    }
                    process_excision_with_session_fences(
                        &mut client,
                        &connection,
                        &id,
                        ExcisionFault::None,
                        &mut work,
                    )
                    .map(Some)
                })();
                work.progress.complete = result.is_ok();
                let _ = events.send(JobEvent::Finished(result, work.progress.clone()));
            })
            .map_err(|e| {
                SemanticError::new(
                    crate::ErrorCategory::Unavailable,
                    "excision/spawn",
                    e.to_string(),
                )
            })?;
        Ok(Self {
            permits,
            events: receiver,
            stop,
            thread: Some(thread),
            progress: ExcisionProgress::default(),
        })
    }
    pub fn advance(&mut self) -> Result<ExcisionStep, SemanticError> {
        self.permits.send(()).map_err(|_| stopped())?;
        match self.events.recv().map_err(|_| stopped())? {
            JobEvent::Progress(progress) => {
                self.progress = progress.clone();
                Ok(ExcisionStep::Progress(progress))
            }
            JobEvent::Finished(result, progress) => {
                self.progress = progress;
                result.map(ExcisionStep::Complete)
            }
        }
    }
    pub fn progress(&self) -> ExcisionProgress {
        self.progress.clone()
    }
}
impl Drop for ExcisionJob {
    fn drop(&mut self) {
        self.stop.store(true, Ordering::Release);
        let _ = self.permits.try_send(());
        // Drop the event receiver only after draining a possible terminal
        // event: the bounded sender must not keep shutdown waiting forever.
        while let Ok(event) = self.events.recv() {
            if matches!(event, JobEvent::Finished(..)) {
                break;
            }
            let _ = self.permits.try_send(());
        }
        if let Some(thread) = self.thread.take() {
            let _ = thread.join();
        }
    }
}

fn has_pending(
    c: &mut Client,
    connection: &PostgresConnectionConfig,
    id: &str,
    work: &mut ExcisionWork,
) -> Result<bool, SemanticError> {
    let row=c.query_one("SELECT h.log_generation, EXISTS(SELECT 1 FROM atomic_log_generation_builds b JOIN atomic_log_generations g USING(database_id,generation) WHERE b.database_id=h.database_id AND g.build_kind=1), d.lineage_id FROM atomic_heads h JOIN atomic_databases d USING(database_id) WHERE h.database_id=$1",&[&id]).map_err(|e|operation_error("excision/discover-build",e))?;
    if row.get::<_, bool>(1) {
        return Ok(true);
    }
    let generation: i64 = row.get(0);
    let identity = crate::DatabaseIdentity::new(id, row.get::<_, String>(2));
    let peer = crate::Peer::connect_identity_configured(connection, &identity, 8)?;
    let value = peer.db().history();
    let prefix = crate::IndexPrefix::Aevt {
        attribute: crate::DB_EXCISE as u32,
        entity: None,
        value: None,
    };
    let mut cursor = value.prefix_cursor(&prefix)?;
    let mut scanned = 0;
    while let Some(datom) = cursor
        .next_with_control(&mut |_| {
            work.check()?;
            Ok(true)
        })
        .transpose()?
    {
        if datom.added {
            let t = sql_u64(crate::tx_to_t(datom.tx)?, "request t")?;
            let entity = sql_u64(datom.entity, "request entity")?;
            let complete:bool=c.query_one("SELECT EXISTS(SELECT 1 FROM atomic_completed_excision_requests WHERE database_id=$1 AND generation=$2 AND request_t=$3 AND request_entity=$4)",&[&id,&generation,&t,&entity]).map_err(|e|operation_error("excision/discover-request",e))?.get(0);
            if !complete {
                return Ok(true);
            }
        }
        scanned += 1;
        if scanned == work.config.log_batch_transactions {
            work.checkpoint("discover")?;
            scanned = 0;
        }
    }
    Ok(false)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, PostgresIndexer, PostgresMigrator,
        PostgresStore, Schema, TxOp, Value, ValueType,
    };

    struct Fixture {
        admin: postgres::Client,
        schema: String,
        connection: String,
    }
    impl Fixture {
        fn new() -> Option<Self> {
            let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
                eprintln!("SKIP cooperative excision: ATOMIC_POSTGRES_URL unset");
                return None;
            };
            let schema = format!(
                "excision_stop_{}_{}",
                std::process::id(),
                std::time::SystemTime::now()
                    .duration_since(std::time::UNIX_EPOCH)
                    .unwrap()
                    .as_nanos()
            );
            let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
            admin
                .batch_execute(&format!("CREATE SCHEMA {schema}"))
                .unwrap();
            let connection = if url.starts_with("postgres://") || url.starts_with("postgresql://") {
                format!(
                    "{url}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if url.contains('?') { "&" } else { "?" }
                )
            } else {
                format!("{url} options='-csearch_path={schema},pg_catalog'")
            };
            Some(Self {
                admin,
                schema,
                connection,
            })
        }
    }
    impl Drop for Fixture {
        fn drop(&mut self) {
            let _ = self
                .admin
                .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
        }
    }

    #[test]
    fn stopped_rendezvous_keeps_committed_checkpoint_and_resumes() {
        let Some(fixture) = Fixture::new() else {
            return;
        };
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
        let mut store = PostgresStore::connect(&fixture.connection).unwrap();
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1000,
                Keyword::new("test", "value"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        let initial_basis = store.create_database("people", schema).unwrap().basis_t();
        PostgresIndexer::connect(&fixture.connection, "people")
            .unwrap()
            .consolidate()
            .unwrap();
        let mut first = 0;
        for i in 0..8 {
            let receipt = store
                .transact_with_fault(
                    "people",
                    &format!("seed-{i}"),
                    initial_basis + i,
                    &[TxOp::Add {
                        entity: EntityRef::Temp("person".into()),
                        attribute: 1000,
                        value: Value::Long(i as i64).into(),
                    }],
                    i as i64 + 1,
                    crate::postgres::CommitFault::None,
                )
                .unwrap();
            if i == 0 {
                first = receipt.tempids["person"];
            }
        }
        store
            .transact_with_fault(
                "people",
                "request",
                initial_basis + 8,
                &[TxOp::Add {
                    entity: EntityRef::Temp("request".into()),
                    attribute: crate::DB_EXCISE as u32,
                    value: Value::Ref(first).into(),
                }],
                9,
                crate::postgres::CommitFault::None,
            )
            .unwrap();
        PostgresIndexer::connect(&fixture.connection, "people")
            .unwrap()
            .consolidate()
            .unwrap();
        let lease = store.acquire_lease("people", "stopping", 60_000).unwrap();
        let config = ExcisionConfig {
            log_batch_transactions: 1,
            ..Default::default()
        };
        let mut job = ExcisionJob::start(
            PostgresConnectionConfig::plaintext(&fixture.connection),
            lease.clone(),
            config,
        )
        .unwrap();
        let started = std::time::Instant::now();
        loop {
            match job.advance().unwrap() {
                ExcisionStep::Progress(progress)
                    if progress.phase == "rewrite" && progress.rewritten_transactions == 1 =>
                {
                    break;
                }
                ExcisionStep::Progress(_) => {}
                ExcisionStep::Complete(_) => {
                    panic!("job passed the requested cooperative stop boundary")
                }
            }
        }
        // Drop occurs while the job is parked before its second rewrite
        // batch, so this is a deterministic interruption, not a timing race.
        drop(job);
        let mut sql = postgres::Client::connect(&fixture.connection, postgres::NoTls).unwrap();
        let checkpoint: i64 = sql.query_one("SELECT MAX(through_basis_t) FROM atomic_log_generation_checkpoints WHERE database_id='people'", &[]).unwrap().get(0);
        assert_eq!(checkpoint, 1);
        assert!(
            !PostgresOperator::connect(&fixture.connection)
                .unwrap()
                .sync_excise("people", initial_basis + 9)
                .unwrap()
        );
        store.release_lease(&lease).unwrap();
        let lease = store.acquire_lease("people", "resuming", 60_000).unwrap();
        let mut job = ExcisionJob::start(
            PostgresConnectionConfig::plaintext(&fixture.connection),
            lease.clone(),
            config,
        )
        .unwrap();
        loop {
            if let ExcisionStep::Complete(Some(receipt)) = job.advance().unwrap() {
                assert!(receipt.resumed);
                assert_eq!(receipt.basis_t, initial_basis + 9);
                break;
            }
        }
        let progress = job.progress();
        drop(job);
        assert!(progress.complete);
        assert_eq!(progress.rewritten_transactions, initial_basis + 8);
        assert!(
            PostgresOperator::connect(&fixture.connection)
                .unwrap()
                .sync_excise("people", initial_basis + 9)
                .unwrap()
        );
        let current = crate::Connection::connect(&fixture.connection, "people", 8)
            .unwrap()
            .db();
        assert!(
            current
                .clone()
                .history()
                .datoms_with_prefix(&crate::IndexPrefix::Eavt {
                    entity: first,
                    attribute: Some(1000),
                    value: None
                })
                .unwrap()
                .is_empty()
        );
        store.release_lease(&lease).unwrap();
        drop((current, sql, store));
        eprintln!(
            "cooperative stop/checkpoint/reacquire/resume/check/drop {:?}: {progress:?}",
            started.elapsed()
        );
    }
}
