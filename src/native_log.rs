//! An immutable log endpoint and a lazy, authenticated transaction cursor.
//!
//! A retained native snapshot owns generation/root pins; an offline backup
//! owns its authenticated immutable point. Log data comes
//! from authoritative transaction content, never an index whose noHistory
//! consolidation may already have discarded earlier assertions.
use super::{
    TieredSnapshot, digest, fault, is_postgres_connection_error, lock, postgres_error,
    read_authenticated_log_range, reconnect_peer_io, sql_basis, verify_database_lineage,
};
use crate::{
    Datom, Digest, IndexBoundary, IndexComponents, IndexTransaction, SemanticError, TimePoint,
    Value,
};
use std::fmt;
use std::iter::FusedIterator;

/// One immutable transaction in historical order. `data` includes both
/// additions and retractions, including information omitted from noHistory
/// indexes. The owning log's excision generation determines its retained data.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct LogTransaction {
    pub t: u64,
    pub data: Vec<Datom>,
}

/// Captured transaction-log value, independent of subsequent peer advancement.
#[derive(Clone)]
pub struct LogValue {
    source: LogSource,
}

#[derive(Clone)]
enum LogSource {
    Native(TieredSnapshot),
    Backup(crate::backup_snapshot::BackupSnapshot),
}

impl fmt::Debug for LogValue {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("LogValue")
            .field("offline", &matches!(self.source, LogSource::Backup(_)))
            .field("basis_t", &self.basis_t())
            .field("generation", &self.generation())
            .finish()
    }
}

/// Successful payload-read work performed by one cursor. Creating an integer-
/// bounded cursor performs no payload reads. Instant bounds may consult the
/// captured database's txInstant index separately from this accounting.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct LogCursorStats {
    /// Logical page requests, including a failing request. An internal
    /// transport retry does not constitute a second logical page.
    pub range_reads: u64,
    pub transactions_read: u64,
    pub datoms_read: u64,
    pub payload_bytes_read: u64,
    /// Canonical payload bytes transferred from PostgreSQL, excluding the
    /// small authoritative metadata reads which still occur on cache hits.
    pub postgres_payload_bytes_read: u64,
    /// Transactions decoded from authenticated persistent local payloads.
    pub cache_hits: u64,
    /// The implementation holds at most one complete transaction at a time.
    /// A caller retaining yielded values owns that additional memory.
    pub peak_buffered_transactions: usize,
}

/// Fallible ordered transaction traversal. It reads one authenticated
/// transaction per payload page and fuses after an error or range exhaustion.
/// Additional traversal memory, beyond the shared captured snapshot, is bounded
/// by one codec-bounded transaction rather than the range length. A large
/// individual transaction still requires correspondingly large memory.
pub struct LogCursor {
    log: LogValue,
    operation: Option<crate::OperationContext>,
    next_t: u64,
    end_t: u64,
    predecessor: Option<Digest>,
    stats: LogCursorStats,
}

impl fmt::Debug for LogCursor {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        formatter
            .debug_struct("LogCursor")
            .field("log", &self.log)
            .field("next_t", &self.next_t)
            .field("end_t", &self.end_t)
            .field("stats", &self.stats)
            .finish()
    }
}

impl LogValue {
    pub(crate) fn new(snapshot: TieredSnapshot) -> Self {
        Self {
            source: LogSource::Native(snapshot),
        }
    }

    pub(crate) fn from_backup(snapshot: crate::backup_snapshot::BackupSnapshot) -> Self {
        Self {
            source: LogSource::Backup(snapshot),
        }
    }

    pub fn basis_t(&self) -> u64 {
        match &self.source {
            LogSource::Native(snapshot) => snapshot.state.basis_t,
            LogSource::Backup(snapshot) => snapshot.basis_t(),
        }
    }

    /// The physical log generation captured with this immutable value.
    #[allow(clippy::misnamed_getters)] // TieredState.generation is a peer-local publication counter.
    pub fn generation(&self) -> u64 {
        match &self.source {
            LogSource::Native(snapshot) => snapshot.state.excision_generation,
            LogSource::Backup(snapshot) => snapshot.generation(),
        }
    }

    /// Start is inclusive and end exclusive. `None` means the beginning or
    /// captured end of this log. T/Tx bounds are exact; an Instant selects the
    /// first transaction at or after it, including the earliest duplicate.
    /// A reversed or disjoint range is empty.
    pub fn tx_range(
        &self,
        start: Option<TimePoint>,
        end: Option<TimePoint>,
    ) -> Result<LogCursor, SemanticError> {
        let next_t = self.basis_t().checked_add(1).ok_or_else(|| {
            fault(
                "log/time-overflow",
                "captured log has no next transaction boundary",
            )
        })?;
        let start = start
            .map(|point| self.resolve_bound(point))
            .transpose()?
            .unwrap_or(1)
            .max(1)
            .min(next_t);
        let end = end
            .map(|point| self.resolve_bound(point))
            .transpose()?
            .unwrap_or(next_t)
            .min(next_t);
        Ok(LogCursor {
            log: self.clone(),
            operation: crate::OperationContext::current(),
            next_t: start,
            end_t: end,
            predecessor: None,
            stats: LogCursorStats::default(),
        })
    }

    /// Transaction entity ids in the same captured range. Payloads are still
    /// authenticated; this does not substitute unauthenticated SQL metadata.
    pub fn tx_ids(
        &self,
        start: Option<TimePoint>,
        end: Option<TimePoint>,
    ) -> Result<impl Iterator<Item = Result<u64, SemanticError>> + use<>, SemanticError> {
        Ok(self
            .tx_range(start, end)?
            .map(|transaction| crate::t_to_tx(transaction?.t)))
    }

    /// Full datoms for one T or transaction entity id. Genesis (T=0) is not a
    /// submitted transaction; absent or future transactions return `None`.
    pub fn tx_data(
        &self,
        transaction: IndexTransaction,
    ) -> Result<Option<Vec<Datom>>, SemanticError> {
        let t = match transaction {
            IndexTransaction::T(t) => {
                crate::t_to_tx(t)?;
                t
            }
            IndexTransaction::Tx(tx) => crate::tx_to_t(tx)?,
        };
        if t == 0 || t > self.basis_t() {
            return Ok(None);
        }
        self.read_transaction(t, None)
            .map(|(transaction, _, _, _)| Some(transaction.data))
    }

    fn resolve_bound(&self, point: TimePoint) -> Result<u64, SemanticError> {
        match point {
            TimePoint::T(t) => {
                crate::t_to_tx(t)?;
                Ok(t)
            }
            TimePoint::Tx(tx) => crate::tx_to_t(tx),
            TimePoint::Instant(instant) => {
                // Recovered log.clj tx-range uses db/t-at-or-since, not
                // as-of-t's predecessor rule for an instant between txes.
                let database = match &self.source {
                    LogSource::Native(snapshot) => snapshot.database_value(),
                    LogSource::Backup(snapshot) => snapshot.database_value(),
                };
                let mut datoms = database.seek_cursor(&IndexBoundary::Avet(
                    IndexComponents::Two(crate::DB_TX_INSTANT as u32, Value::Instant(instant)),
                ))?;
                let candidate = datoms.next().transpose()?;
                if let Some(datom) =
                    candidate.filter(|datom| datom.attribute == crate::DB_TX_INSTANT as u32)
                {
                    let Value::Instant(found) = datom.value else {
                        return Err(fault(
                            "log/invalid-tx-instant",
                            "transaction instant index contains a non-instant value",
                        ));
                    };
                    let t = crate::tx_to_t(datom.tx).map_err(|_| {
                        fault(
                            "log/invalid-tx-instant",
                            "transaction instant index contains an invalid transaction entity id",
                        )
                    })?;
                    if !datom.added
                        || found < instant
                        || datom.entity != datom.tx
                        || t == 0
                        || t > self.basis_t()
                    {
                        return Err(fault(
                            "log/invalid-tx-instant",
                            "transaction instant index contains an invalid transaction coordinate",
                        ));
                    }
                    Ok(t)
                } else {
                    self.basis_t().checked_add(1).ok_or_else(|| {
                        fault(
                            "log/time-overflow",
                            "captured log has no next transaction boundary",
                        )
                    })
                }
            }
        }
    }

    fn read_transaction(
        &self,
        t: u64,
        predecessor: Option<Digest>,
    ) -> Result<(LogTransaction, Digest, u64, bool), SemanticError> {
        let snapshot = match &self.source {
            LogSource::Native(snapshot) => snapshot,
            LogSource::Backup(snapshot) => {
                return snapshot
                    .read_log_transaction(t, predecessor)
                    .map(|(transaction, hash, bytes)| (transaction, hash, bytes, false));
            }
        };
        snapshot.core.root_pins.ensure()?;
        let mut io = lock(&snapshot.core.io);
        if io.client.is_closed() {
            reconnect_peer_io(&snapshot.core, &mut io)?;
        }
        let read = |client: &mut crate::sql_io::SqlClient| {
            let database_id = &snapshot.core.database_id;
            verify_database_lineage(client, database_id, &snapshot.core.lineage_id)?;
            let previous = match predecessor {
                Some(hash) => hash,
                None => {
                    let row = if t == 1 {
                        client.query_opt("SELECT genesis_hash FROM atomic_databases WHERE database_id = $1", &[database_id])
                    } else if self.generation() == 0 {
                        client.query_opt("SELECT tx_hash FROM atomic_transactions WHERE database_id = $1 AND basis_t = $2", &[database_id, &sql_basis(t - 1)?])
                    } else {
                        client.query_opt("SELECT tx_hash FROM atomic_generation_transactions WHERE database_id = $1 AND generation = $2 AND basis_t = $3", &[database_id, &sql_basis(self.generation())?, &sql_basis(t - 1)?])
                    }.map_err(|error| postgres_error("log/predecessor-read", error))?.ok_or_else(|| fault("log/missing-predecessor", "captured generation has no transaction predecessor anchor"))?;
                    digest(row.get(0), "log predecessor hash")?
                }
            };
            let (row, cache_hit) = if snapshot.core.ssd_cache.is_enabled() {
                let cache = snapshot.core.ssd_log_for_generation(self.generation());
                read_cached_transaction(
                    client,
                    database_id,
                    &snapshot.core.lineage_id,
                    self.generation(),
                    t,
                    previous,
                    &cache,
                )?
            } else {
                let mut rows = read_authenticated_log_range(
                    client,
                    database_id,
                    self.generation(),
                    t - 1,
                    t,
                    previous,
                )?;
                let row = rows.pop().ok_or_else(missing_transaction)?;
                (row, false)
            };
            if t == self.basis_t() && row.tx_hash != snapshot.state.current_hash {
                return Err(fault(
                    "log/captured-endpoint-mismatch",
                    "transaction does not match the captured immutable log endpoint",
                ));
            }
            Ok((
                LogTransaction {
                    t: row.transaction.basis_t,
                    data: row.transaction.tx_data,
                },
                row.tx_hash,
                row.payload.len() as u64,
                cache_hit,
            ))
        };
        match read(&mut io.client) {
            Err(error) if is_postgres_connection_error(&error) => {
                reconnect_peer_io(&snapshot.core, &mut io)?;
                read(&mut io.client)
            }
            result => result,
        }
    }
}

fn missing_transaction() -> SemanticError {
    fault(
        "recovery/missing-transaction",
        "transaction range is incomplete or lacks its exact request record",
    )
}

/// Cache only content, never membership or authorization. The indexed joins
/// deliberately retain request/content existence checks without transferring
/// toasted payloads. Ordinary and cached reads share the exact authenticator.
fn read_cached_transaction(
    client: &mut crate::sql_io::SqlClient,
    database_id: &str,
    lineage_id: &str,
    generation: u64,
    t: u64,
    previous: Digest,
    cache: &crate::SsdCache,
) -> Result<(crate::postgres::AuthenticatedLogTransaction, bool), SemanticError> {
    let basis = sql_basis(t)?;
    let generation_sql = sql_basis(generation)?;
    let metadata = if generation == 0 {
        client.query_opt(
            "SELECT t.basis_t, t.previous_hash, t.tx_hash, NULL::bytea, \
                    t.state_hash, NULL::bytea, NULL::bigint, NULL::text, \
                    1::smallint, r.request_key, NULL::bytea, r.request_digest \
               FROM atomic_transactions t JOIN atomic_requests r \
                 ON r.database_id = t.database_id AND r.basis_t = t.basis_t \
                AND r.tx_hash = t.tx_hash \
              WHERE t.database_id = $1 AND t.basis_t = $2",
            &[&database_id, &basis],
        )
    } else {
        client.query_opt(
            "SELECT t.basis_t, t.previous_hash, t.tx_hash, NULL::bytea, \
                    t.state_hash, t.content_hash, t.eidx_frontier, c.lineage_id, \
                    r.request_kind, NULL::text, r.request_key_hash, r.request_digest \
               FROM atomic_generation_transactions t \
               JOIN atomic_transaction_contents c ON c.content_hash = t.content_hash \
               JOIN atomic_generation_requests r \
                 ON r.database_id = t.database_id AND r.generation = t.generation \
                AND r.basis_t = t.basis_t AND r.tx_hash = t.tx_hash \
              WHERE t.database_id = $1 AND t.generation = $2 AND t.basis_t = $3",
            &[&database_id, &generation_sql, &basis],
        )
    }
    .map_err(|error| postgres_error("log/cache-membership-read", error))?
    .ok_or_else(missing_transaction)?;
    let hash = digest(
        metadata.get(if generation == 0 { 2 } else { 5 }),
        "log payload hash",
    )?;
    let (payload, cache_hit) = match cache.get(&hash) {
        Some(payload) => (payload, true),
        None => {
            let payload_row = if generation == 0 {
                client.query_opt(
                    "SELECT payload FROM atomic_transactions \
                      WHERE database_id = $1 AND basis_t = $2 AND tx_hash = $3",
                    &[&database_id, &basis, &&hash[..]],
                )
            } else {
                client.query_opt(
                    "SELECT payload FROM atomic_transaction_contents \
                      WHERE content_hash = $1 AND lineage_id = $2",
                    &[&&hash[..], &lineage_id],
                )
            }
            .map_err(|error| postgres_error("log/cache-payload-read", error))?
            .ok_or_else(missing_transaction)?;
            (payload_row.get(0), false)
        }
    };
    let authenticated = crate::postgres::authenticate_log_row(
        &metadata,
        payload,
        database_id,
        lineage_id,
        generation,
        t,
        previous,
    )?;
    if !cache_hit {
        // Cache admission is optional. Its envelope capacity can be smaller
        // than a valid transaction; refusing admission never rejects a read.
        cache.put(&hash, &authenticated.payload);
    }
    Ok((authenticated, cache_hit))
}

impl LogCursor {
    pub fn stats(&self) -> LogCursorStats {
        self.stats
    }
}

impl Iterator for LogCursor {
    type Item = Result<LogTransaction, SemanticError>;

    fn next(&mut self) -> Option<Self::Item> {
        let operation = self.operation.clone();
        let _scope = operation.as_ref().map(crate::OperationContext::enter);
        if self.next_t >= self.end_t {
            return None;
        }
        self.stats.range_reads = self.stats.range_reads.saturating_add(1);
        match self.log.read_transaction(self.next_t, self.predecessor) {
            Ok((transaction, hash, bytes, cache_hit)) => {
                self.next_t += 1;
                self.predecessor = Some(hash);
                self.stats.transactions_read = self.stats.transactions_read.saturating_add(1);
                self.stats.datoms_read = self
                    .stats
                    .datoms_read
                    .saturating_add(transaction.data.len() as u64);
                self.stats.payload_bytes_read = self.stats.payload_bytes_read.saturating_add(bytes);
                if cache_hit {
                    self.stats.cache_hits = self.stats.cache_hits.saturating_add(1);
                } else if matches!(self.log.source, LogSource::Native(_)) {
                    self.stats.postgres_payload_bytes_read =
                        self.stats.postgres_payload_bytes_read.saturating_add(bytes);
                }
                self.stats.peak_buffered_transactions = 1;
                Some(Ok(transaction))
            }
            Err(error) => {
                self.next_t = self.end_t;
                Some(Err(error))
            }
        }
    }

    fn size_hint(&self) -> (usize, Option<usize>) {
        (
            0,
            usize::try_from(self.end_t.saturating_sub(self.next_t)).ok(),
        )
    }
}

impl FusedIterator for LogCursor {}
