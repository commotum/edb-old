//! Bounded, at-least-once application consumption of the authenticated log.
//! Checkpoints neither reserve history nor acknowledge external side effects.
use crate::change_notices::NoticeListener;
use crate::postgres::{
    is_postgres_connection_error, postgres_error, read_authenticated_log_range,
    verify_schema_compatibility,
};
use crate::sql_io::{GenericClient, SqlClient};
use crate::{
    Digest, ErrorCategory, LogTransaction, ObservationConfig, PostgresConnectionConfig,
    SemanticError,
};
use std::sync::atomic::{AtomicBool, Ordering};
use std::time::{Duration, Instant};

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub struct ChangeConsumerConfig {
    pub observation: ObservationConfig,
    /// Admission bound for one encoded payload and its decoded datoms.
    /// Oversize errors leave the checkpoint untouched; reopen with a larger
    /// bound to process that transaction. This is not a transaction-size rule.
    pub max_event_bytes: usize,
}
impl Default for ChangeConsumerConfig {
    fn default() -> Self {
        Self {
            observation: ObservationConfig::default(),
            max_event_bytes: 128 * 1024 * 1024,
        }
    }
}

/// An immutable progress token; only a token delivered by this consumer's
/// current next() call can be acknowledged. SQL rows are reauthenticated on
/// reopen, never treated as an alternate source of transaction authority.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ChangeCheckpoint {
    lineage_id: String,
    generation: u64,
    last_t: u64,
    commit_hash: Digest,
    revision: u64,
}
impl ChangeCheckpoint {
    pub fn lineage_id(&self) -> &str {
        &self.lineage_id
    }
    pub fn generation(&self) -> u64 {
        self.generation
    }
    pub fn last_t(&self) -> u64 {
        self.last_t
    }
    pub fn commit_hash(&self) -> Digest {
        self.commit_hash
    }
    pub fn revision(&self) -> u64 {
        self.revision
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ChangeEvent {
    pub transaction: LogTransaction,
    pub checkpoint: ChangeCheckpoint,
    pub accounted_bytes: usize,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct ChangeConsumerStats {
    pub delivered: u64,
    pub acknowledged: u64,
    pub notices: u64,
    pub reconnects: u64,
    pub gap_checks: u64,
    pub payload_bytes: u64,
    /// Only a progress token is retained internally after delivery. The
    /// returned event is caller-owned; no database snapshots are held.
    pub unacknowledged: bool,
}

pub struct ChangeConsumer {
    connection: PostgresConnectionConfig,
    database: String,
    name: String,
    config: ChangeConsumerConfig,
    client: SqlClient,
    listener: Option<NoticeListener>,
    checkpoint: ChangeCheckpoint,
    pending: Option<ChangeCheckpoint>,
    stats: ChangeConsumerStats,
}

struct Head {
    lineage: String,
    generation: u64,
    t: u64,
    hash: Digest,
    genesis: Digest,
}
fn number(value: i64) -> Result<u64, SemanticError> {
    u64::try_from(value)
        .map_err(|_| fault("consumer/invalid-coordinate", "negative durable coordinate"))
}
fn sql(value: u64) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        fault(
            "consumer/coordinate-overflow",
            "durable coordinate exceeds PostgreSQL bigint",
        )
    })
}
fn hash(value: Vec<u8>) -> Result<Digest, SemanticError> {
    value
        .try_into()
        .map_err(|_| fault("consumer/invalid-hash", "durable hash has the wrong length"))
}
fn fault(code: &'static str, message: &str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}
fn unavailable(code: &'static str, message: &str) -> SemanticError {
    SemanticError::new(ErrorCategory::Unavailable, code, message)
}
fn head(client: &mut impl GenericClient, database: &str) -> Result<Head, SemanticError> {
    let row = client.query_opt("SELECT d.lineage_id,h.log_generation,h.basis_t,h.tx_hash,d.genesis_hash FROM atomic_databases d JOIN atomic_heads h USING(database_id) WHERE d.database_id=$1", &[&database])
        .map_err(|e| postgres_error("consumer/head", e))?.ok_or_else(|| unavailable("consumer/database-unavailable", "consumer database no longer exists"))?;
    Ok(Head {
        lineage: row.get(0),
        generation: number(row.get(1))?,
        t: number(row.get(2))?,
        hash: hash(row.get(3))?,
        genesis: hash(row.get(4))?,
    })
}
fn verify_head(head: &Head, checkpoint: &ChangeCheckpoint) -> Result<(), SemanticError> {
    if head.lineage != checkpoint.lineage_id {
        return Err(unavailable(
            "consumer/lineage-changed",
            "database name now identifies another lineage",
        ));
    }
    if head.generation != checkpoint.generation {
        return Err(unavailable(
            "consumer/generation-changed",
            "checkpoint predates excision; explicitly choose a new consumer in the current generation",
        ));
    }
    if checkpoint.last_t > head.t {
        return Err(fault(
            "consumer/checkpoint-future",
            "checkpoint is beyond the authenticated log head",
        ));
    }
    Ok(())
}
fn pin(
    client: &mut impl GenericClient,
    database: &str,
    checkpoint: &ChangeCheckpoint,
) -> Result<Head, SemanticError> {
    let acquired: bool = client.query_one("SELECT COALESCE(pg_try_advisory_xact_lock_shared(atomic_log_generation_pin_key($1,$2)),false)", &[&database, &sql(checkpoint.generation)?])
        .map_err(|e| postgres_error("consumer/generation-pin",e))?.get(0);
    if !acquired {
        return Err(unavailable(
            "consumer/history-unavailable",
            "consumer generation is being reclaimed",
        ));
    }
    let current = head(client, database)?;
    verify_head(&current, checkpoint)?;
    Ok(current)
}
fn predecessor(
    client: &mut impl GenericClient,
    database: &str,
    generation: u64,
    t: u64,
    genesis: Digest,
) -> Result<Digest, SemanticError> {
    if t == 1 {
        return Ok(genesis);
    }
    let row = if generation == 0 {
        client.query_opt("SELECT tx_hash FROM atomic_transactions WHERE database_id=$1 AND basis_t=$2", &[&database,&sql(t-1)?])
    } else {
        client.query_opt("SELECT tx_hash FROM atomic_generation_transactions WHERE database_id=$1 AND generation=$2 AND basis_t=$3", &[&database,&sql(generation)?,&sql(t-1)?])
    }.map_err(|e| postgres_error("consumer/predecessor",e))?.ok_or_else(|| unavailable("consumer/history-unavailable", "checkpoint predecessor is not retained"))?;
    hash(row.get(0))
}

fn admit_payload(
    client: &mut impl GenericClient,
    database: &str,
    generation: u64,
    t: u64,
    limit: usize,
) -> Result<(), SemanticError> {
    let row = if generation == 0 {
        client.query_opt("SELECT octet_length(payload)::bigint FROM atomic_transactions WHERE database_id=$1 AND basis_t=$2", &[&database,&sql(t)?])
    } else {
        client.query_opt("SELECT octet_length(c.payload)::bigint FROM atomic_generation_transactions t JOIN atomic_transaction_contents c ON c.content_hash=t.content_hash WHERE t.database_id=$1 AND t.generation=$2 AND t.basis_t=$3", &[&database,&sql(generation)?,&sql(t)?])
    }.map_err(|e|postgres_error("consumer/event-size",e))?.ok_or_else(||unavailable("consumer/history-unavailable","transaction is not retained"))?;
    if number(row.get(0))? > limit as u64 {
        return Err(SemanticError::new(
            ErrorCategory::Busy,
            "consumer/event-too-large",
            "transaction exceeds configured event byte limit",
        ));
    }
    Ok(())
}

impl ChangeConsumer {
    pub fn connect(
        connection: &str,
        database: impl Into<String>,
        name: impl Into<String>,
        config: ChangeConsumerConfig,
    ) -> Result<Self, SemanticError> {
        Self::connect_configured(
            PostgresConnectionConfig::plaintext(connection),
            database,
            name,
            config,
        )
    }
    pub fn connect_configured(
        connection: PostgresConnectionConfig,
        database: impl Into<String>,
        name: impl Into<String>,
        config: ChangeConsumerConfig,
    ) -> Result<Self, SemanticError> {
        config.observation.validate()?;
        let database = database.into();
        let name = name.into();
        if name.is_empty() || name.len() > 512 || config.max_event_bytes == 0 {
            return Err(SemanticError::incorrect(
                "consumer/invalid-config",
                "consumer name must contain 1..512 bytes and event byte limit must be positive",
            ));
        }
        let mut client = connection.connect_for("consumer/connect")?;
        verify_schema_compatibility(&mut client)?;
        // Subscribe before taking the durable endpoint; a concurrent commit
        // is therefore either in this read or leaves a wakeup for next().
        let listener = Some(NoticeListener::connect(&connection, &database)?);
        let mut tx = client
            .transaction()
            .map_err(|e| postgres_error("consumer/open-begin", e))?;
        let initial = head(&mut tx, &database)?;
        let genesis: Vec<u8> = tx
            .query_one(
                "SELECT genesis FROM atomic_databases WHERE database_id=$1",
                &[&database],
            )
            .map_err(|e| postgres_error("consumer/genesis", e))?
            .get(0);
        if crate::sha256(&genesis) != initial.genesis {
            return Err(fault(
                "consumer/genesis-hash",
                "canonical genesis does not match its digest",
            ));
        }
        crate::decode_genesis(&genesis)?;
        tx.execute("INSERT INTO atomic_change_checkpoints(database_id,consumer_name,lineage_id,generation,last_t,commit_hash,revision) VALUES($1,$2,$3,$4,0,$5,0) ON CONFLICT DO NOTHING", &[&database,&name,&initial.lineage,&sql(initial.generation)?,&&initial.genesis[..]])
            .map_err(|e|postgres_error("consumer/checkpoint-create",e))?;
        let row = tx.query_one("SELECT lineage_id,generation,last_t,commit_hash,revision FROM atomic_change_checkpoints WHERE database_id=$1 AND consumer_name=$2 AND checkpoint_owner=current_user", &[&database,&name])
            .map_err(|e|postgres_error("consumer/checkpoint-open",e))?;
        let checkpoint = ChangeCheckpoint {
            lineage_id: row.get(0),
            generation: number(row.get(1))?,
            last_t: number(row.get(2))?,
            commit_hash: hash(row.get(3))?,
            revision: number(row.get(4))?,
        };
        let current = pin(&mut tx, &database, &checkpoint)?;
        if checkpoint.last_t == 0 {
            if checkpoint.commit_hash != current.genesis {
                return Err(fault(
                    "consumer/checkpoint-hash",
                    "checkpoint does not identify canonical genesis",
                ));
            }
        } else {
            admit_payload(
                &mut tx,
                &database,
                checkpoint.generation,
                checkpoint.last_t,
                config.max_event_bytes,
            )?;
            let previous = predecessor(
                &mut tx,
                &database,
                checkpoint.generation,
                checkpoint.last_t,
                current.genesis,
            )?;
            let rows = read_authenticated_log_range(
                &mut tx,
                &database,
                checkpoint.generation,
                checkpoint.last_t - 1,
                checkpoint.last_t,
                previous,
            )?;
            let observed = rows
                .last()
                .ok_or_else(|| {
                    unavailable(
                        "consumer/history-unavailable",
                        "checkpoint transaction is not retained",
                    )
                })?
                .tx_hash;
            if observed != checkpoint.commit_hash
                || (checkpoint.last_t == current.t && observed != current.hash)
            {
                return Err(fault(
                    "consumer/checkpoint-hash",
                    "checkpoint does not identify the authenticated transaction",
                ));
            }
        }
        tx.commit()
            .map_err(|e| postgres_error("consumer/open-commit", e))?;
        Ok(Self {
            connection,
            database,
            name,
            config,
            client,
            listener,
            checkpoint,
            pending: None,
            stats: ChangeConsumerStats::default(),
        })
    }
    pub fn checkpoint(&self) -> &ChangeCheckpoint {
        &self.checkpoint
    }
    pub fn stats(&self) -> ChangeConsumerStats {
        let mut stats = self.stats;
        stats.unacknowledged = self.pending.is_some();
        stats
    }

    /// Reconnect transport only. Durable progress is never silently changed;
    /// another consumer's advance is detected by checkpoint CAS at acknowledge.
    pub fn reconnect(&mut self) -> Result<(), SemanticError> {
        self.client = self.connection.connect_for("consumer/reconnect")?;
        self.listener = Some(NoticeListener::connect(&self.connection, &self.database)?);
        verify_head(&head(&mut self.client, &self.database)?, &self.checkpoint)?;
        self.stats.reconnects = self.stats.reconnects.saturating_add(1);
        Ok(())
    }
    pub fn next(&mut self, timeout: Duration) -> Result<Option<ChangeEvent>, SemanticError> {
        static NEVER: AtomicBool = AtomicBool::new(false);
        self.next_with_cancel(timeout, &NEVER)
    }
    /// Timeout/cancellation bound idle waiting, not an already-issued SQL
    /// statement; the configured PostgreSQL I/O policy bounds that operation.
    pub fn next_with_cancel(
        &mut self,
        timeout: Duration,
        cancelled: &AtomicBool,
    ) -> Result<Option<ChangeEvent>, SemanticError> {
        if self.pending.is_some() {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "consumer/unacknowledged",
                "acknowledge the delivered event before requesting another",
            ));
        }
        let deadline = Instant::now().checked_add(timeout).ok_or_else(|| {
            SemanticError::incorrect("consumer/timeout-overflow", "consumer timeout is too large")
        })?;
        let mut check = true;
        let mut repair_at = Instant::now();
        loop {
            if cancelled.load(Ordering::Relaxed) {
                return Err(SemanticError::new(
                    ErrorCategory::Interrupted,
                    "consumer/cancelled",
                    "consumer wait was cancelled",
                ));
            }
            if check || Instant::now() >= repair_at {
                let result = self.read_next();
                match result {
                    Ok(Some(event)) => return Ok(Some(event)),
                    Ok(None) => {}
                    Err(error) if is_postgres_connection_error(&error) => {
                        self.reconnect()?;
                        if let Some(event) = self.read_next()? {
                            return Ok(Some(event));
                        }
                    }
                    Err(error) => return Err(error),
                }
                check = false;
                repair_at = Instant::now() + self.config.observation.anti_entropy_interval;
            }
            let now = Instant::now();
            if now >= deadline {
                return Ok(None);
            }
            let wait = deadline
                .saturating_duration_since(now)
                .min(repair_at.saturating_duration_since(now))
                .min(Duration::from_millis(25));
            let result = self
                .listener
                .as_mut()
                .expect("consumer listener exists")
                .wait(wait);
            match result {
                Ok(true) => {
                    self.stats.notices = self.stats.notices.saturating_add(1);
                    check = true;
                }
                Ok(false) => {}
                Err(_) => {
                    self.reconnect()?;
                    check = true;
                }
            }
        }
    }
    fn read_next(&mut self) -> Result<Option<ChangeEvent>, SemanticError> {
        self.stats.gap_checks = self.stats.gap_checks.saturating_add(1);
        let mut tx = self
            .client
            .transaction()
            .map_err(|e| postgres_error("consumer/read-begin", e))?;
        let current = pin(&mut tx, &self.database, &self.checkpoint)?;
        if current.t == self.checkpoint.last_t {
            if current.hash != self.checkpoint.commit_hash {
                return Err(fault(
                    "consumer/head-hash",
                    "checkpoint and current head disagree",
                ));
            }
            tx.commit()
                .map_err(|e| postgres_error("consumer/read-commit", e))?;
            return Ok(None);
        }
        let next = self.checkpoint.last_t + 1;
        admit_payload(
            &mut tx,
            &self.database,
            self.checkpoint.generation,
            next,
            self.config.max_event_bytes,
        )?;
        let mut rows = read_authenticated_log_range(
            &mut tx,
            &self.database,
            self.checkpoint.generation,
            self.checkpoint.last_t,
            next,
            self.checkpoint.commit_hash,
        )?;
        let row = rows.pop().ok_or_else(|| {
            unavailable(
                "consumer/history-unavailable",
                "next transaction is not retained",
            )
        })?;
        if next == current.t && row.tx_hash != current.hash {
            return Err(fault(
                "consumer/head-hash",
                "transaction does not match current head",
            ));
        }
        let accounted = row
            .transaction
            .tx_data
            .iter()
            .fold(row.payload.len(), |n, d| {
                n.saturating_add(d.retained_bytes() as usize)
            });
        if accounted > self.config.max_event_bytes {
            return Err(SemanticError::new(
                ErrorCategory::Busy,
                "consumer/event-too-large",
                "decoded transaction exceeds configured event byte limit",
            ));
        }
        tx.commit()
            .map_err(|e| postgres_error("consumer/read-commit", e))?;
        let checkpoint = ChangeCheckpoint {
            lineage_id: self.checkpoint.lineage_id.clone(),
            generation: self.checkpoint.generation,
            last_t: next,
            commit_hash: row.tx_hash,
            revision: self.checkpoint.revision.checked_add(1).ok_or_else(|| {
                fault(
                    "consumer/revision-overflow",
                    "checkpoint revision exhausted",
                )
            })?,
        };
        self.pending = Some(checkpoint.clone());
        self.stats.delivered = self.stats.delivered.saturating_add(1);
        self.stats.payload_bytes = self
            .stats
            .payload_bytes
            .saturating_add(row.payload.len() as u64);
        Ok(Some(ChangeEvent {
            transaction: LogTransaction {
                t: next,
                data: row.transaction.tx_data,
            },
            checkpoint,
            accounted_bytes: accounted,
        }))
    }
    /// Call only after processing the event. CAS prevents stale instances from
    /// overwriting progress; the external effect and this write are not atomic.
    /// On ambiguous transport failure, reopen and use the persisted checkpoint.
    pub fn acknowledge(&mut self, checkpoint: &ChangeCheckpoint) -> Result<(), SemanticError> {
        if self.pending.as_ref() != Some(checkpoint) {
            return Err(SemanticError::incorrect(
                "consumer/not-delivered",
                "checkpoint is not this consumer's pending contiguous event",
            ));
        }
        verify_head(&head(&mut self.client, &self.database)?, &self.checkpoint)?;
        let updated=self.client.execute("UPDATE atomic_change_checkpoints SET last_t=$3,commit_hash=$4,revision=$5 WHERE database_id=$1 AND consumer_name=$2 AND checkpoint_owner=current_user AND lineage_id=$6 AND generation=$7 AND last_t=$8 AND commit_hash=$9 AND revision=$10 AND EXISTS (SELECT 1 FROM atomic_heads h JOIN atomic_databases d USING(database_id) WHERE h.database_id=$1 AND h.log_generation=$7 AND d.lineage_id=$6)", &[&self.database,&self.name,&sql(checkpoint.last_t)?,&&checkpoint.commit_hash[..],&sql(checkpoint.revision)?,&self.checkpoint.lineage_id,&sql(self.checkpoint.generation)?,&sql(self.checkpoint.last_t)?,&&self.checkpoint.commit_hash[..],&sql(self.checkpoint.revision)?]);
        let updated=updated.map_err(|e| {
            let error=postgres_error("consumer/checkpoint-update",e);
            if is_postgres_connection_error(&error) { SemanticError::new(ErrorCategory::UnknownOutcome,"consumer/checkpoint-unknown-outcome","checkpoint acknowledgment may have committed; reopen to resolve durable progress") } else { error }
        })?;
        if updated != 1 {
            verify_head(&head(&mut self.client, &self.database)?, &self.checkpoint)?;
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "consumer/checkpoint-conflict",
                "another consumer advanced this checkpoint; reopen before continuing",
            ));
        }
        self.checkpoint = checkpoint.clone();
        self.pending = None;
        self.stats.acknowledged = self.stats.acknowledged.saturating_add(1);
        Ok(())
    }
}
