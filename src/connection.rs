use crate::peer::Peer;
use crate::{
    DatabaseValue, Datom, Entity, EntityIdentifier, IndexBoundary, IndexOrder, IndexPrefix,
    PostgresConnectionConfig, PullPattern, Query, QueryControl, QueryExtensions, QueryInput,
    QueryOutcome, QueryValue, SemanticError, ServiceTransactionReport, TransactionClient,
    TransactionRequest, TransactionService, TransactionServiceConfig, TransactionTicket,
};
use std::sync::Arc;
use std::time::Duration;

/// Stable identity of one logical database catalog entry.
///
/// A database name is an address chosen by an operator.  The lineage is the
/// durable identity behind that address and prevents a long-lived connection
/// from silently retargeting if a catalog entry is dropped and recreated.
#[derive(Clone, Debug, Eq, Hash, Ord, PartialEq, PartialOrd)]
pub struct DatabaseIdentity {
    database_id: String,
    lineage_id: String,
}

impl DatabaseIdentity {
    pub(crate) fn new(database_id: impl Into<String>, lineage_id: impl Into<String>) -> Self {
        Self {
            database_id: database_id.into(),
            lineage_id: lineage_id.into(),
        }
    }

    pub fn database_id(&self) -> &str {
        &self.database_id
    }

    pub fn lineage_id(&self) -> &str {
        &self.lineage_id
    }
}

struct ConnectionCore {
    identity: DatabaseIdentity,
    peer: Peer,
    client: TransactionClient,
    // The embedded service owns the single-writer lease and both worker
    // lifetimes. It is intentionally retained by the last Connection clone.
    _service: TransactionService,
}

/// Cloneable, long-lived native connection to one PostgreSQL database.
///
/// Like recovered `datomic.peer.Connection`, the mutable connection is only
/// an observation/co-ordination point. Every `db` call returns an immutable
/// value, and advancing this shared connection never changes an older value.
/// Unlike the JVM implementation, the only transport here is the concrete
/// in-process PostgreSQL-backed transactor.
#[derive(Clone)]
pub struct Connection {
    core: Arc<ConnectionCore>,
}

impl Connection {
    /// Start the embedded transactor and strict native peer using plaintext
    /// PostgreSQL transport (the existing local/development policy).
    pub fn start(
        config: TransactionServiceConfig,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        let connection = PostgresConnectionConfig::plaintext(config.connection.clone());
        Self::start_configured(config, connection, cache_capacity)
    }

    /// Start every connection-owned role under one explicit PostgreSQL
    /// transport policy. The transactor is started first so the bounded fresh
    /// database exception can publish its native basis before the strict peer
    /// opens it.
    pub fn start_configured(
        config: TransactionServiceConfig,
        connection: PostgresConnectionConfig,
        cache_capacity: usize,
    ) -> Result<Self, SemanticError> {
        let service = TransactionService::start_configured(config, connection.clone())?;
        let client = service.client();
        let client_identity = client.identity();
        let peer =
            Peer::connect_configured(&connection, client_identity.database_id(), cache_capacity)?;
        let peer_identity = peer.identity();
        if peer_identity != client_identity {
            return Err(identity_mismatch(&client_identity, &peer_identity));
        }
        Ok(Self {
            core: Arc::new(ConnectionCore {
                identity: client_identity,
                peer,
                client,
                _service: service,
            }),
        })
    }

    pub fn identity(&self) -> &DatabaseIdentity {
        &self.core.identity
    }

    /// Return the connection's most recently adopted immutable value without
    /// PostgreSQL I/O or transactor co-ordination.
    pub fn db(&self) -> DatabaseValue {
        self.core.peer.database_value()
    }

    /// Capture and adopt the newest durable head visible at the time of this
    /// call. This is the native synchronous counterpart of recovered
    /// zero-argument `sync`.
    pub fn sync(&self) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_database_value()
    }

    /// Wait until this connection has adopted at least `target_t` without
    /// communicating with a separate transactor endpoint.
    pub fn sync_to(
        &self,
        target_t: u64,
        timeout: Duration,
    ) -> Result<DatabaseValue, SemanticError> {
        self.core.peer.sync_to_database_value(target_t, timeout)
    }

    /// Admit a transaction without blocking. The returned ticket does not
    /// expose a successful report until the report's exact native successor
    /// has been adopted by this shared connection.
    pub fn submit(
        &self,
        request: TransactionRequest,
    ) -> Result<ConnectionTransactionTicket, SemanticError> {
        Ok(ConnectionTransactionTicket {
            ticket: self.core.client.submit(request)?,
            peer: self.core.peer.clone(),
        })
    }

    pub fn transact(
        &self,
        request: TransactionRequest,
        timeout: Duration,
    ) -> Result<ServiceTransactionReport, SemanticError> {
        self.submit(request)?.wait(timeout)
    }

    /// Enable the single unbounded report queue associated with this
    /// connection. Reports begin with the next transaction adopted after the
    /// queue is enabled; no already-observed prefix is manufactured.
    pub fn enable_transaction_reports(&self) -> bool {
        self.core.peer.enable_tx_reports()
    }

    pub fn try_next_transaction_report(&self) -> Option<ServiceTransactionReport> {
        self.core.peer.try_next_tx_report()
    }

    pub fn next_transaction_report(
        &self,
        timeout: Duration,
    ) -> Result<Option<ServiceTransactionReport>, SemanticError> {
        self.core.peer.next_tx_report(timeout)
    }

    /// Remove this connection's report queue and release all unconsumed
    /// immutable db-before/db-after values.
    pub fn remove_transaction_report_queue(&self) -> bool {
        self.core.peer.remove_tx_reports()
    }

    /// Query convenience that captures exactly one immutable value.
    pub fn query(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
    ) -> Result<QueryOutcome, SemanticError> {
        let database = self.db();
        database.query(query, inputs, control)
    }

    pub fn query_with_extensions(
        &self,
        query: &Query,
        inputs: &[QueryInput],
        control: &QueryControl,
        extensions: &QueryExtensions,
    ) -> Result<QueryOutcome, SemanticError> {
        let database = self.db();
        database.query_with_extensions(query, inputs, control, extensions)
    }

    /// Pull convenience that pins all recursive navigation to one immutable
    /// value even if this connection advances concurrently.
    pub fn pull(
        &self,
        pattern: &PullPattern,
        entity: impl Into<EntityIdentifier>,
    ) -> Result<QueryValue, SemanticError> {
        let database = self.db();
        database.pull(pattern, entity)
    }

    /// Entity convenience that pins subsequent lazy navigation to the one
    /// immutable value captured here.
    pub fn entity(
        &self,
        identifier: impl Into<EntityIdentifier>,
    ) -> Result<Option<Entity>, SemanticError> {
        let database = self.db();
        database.entity(identifier)
    }

    pub fn collect_datoms(&self, order: IndexOrder) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_datoms(order)
    }

    pub fn collect_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_seek_datoms(boundary)
    }

    pub fn collect_reverse_seek_datoms(
        &self,
        boundary: &IndexBoundary,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_reverse_seek_datoms(boundary)
    }

    pub fn collect_datoms_with_prefix(
        &self,
        prefix: &IndexPrefix,
    ) -> Result<Vec<Datom>, SemanticError> {
        let database = self.db();
        database.collect_datoms_with_prefix(prefix)
    }
}

/// An admitted transaction whose success observation is ordered after native
/// connection adoption, matching recovered `notify-data`: install db-after,
/// notify the submitting future, then enqueue the observer report.
pub struct ConnectionTransactionTicket {
    ticket: TransactionTicket,
    peer: Peer,
}

impl ConnectionTransactionTicket {
    pub fn wait(self, timeout: Duration) -> Result<ServiceTransactionReport, SemanticError> {
        let report = self.ticket.wait(timeout)?;
        self.peer.adopt_committed_report(&report)?;
        Ok(report)
    }

    pub fn request_key(&self) -> &str {
        self.ticket.request_key()
    }
}

fn identity_mismatch(client: &DatabaseIdentity, peer: &DatabaseIdentity) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Fault,
        "connection/database-identity-mismatch",
        "the transaction client and native peer do not address the same database lineage",
    )
    .detail("client_database_id", client.database_id())
    .detail("client_lineage_id", client.lineage_id())
    .detail("peer_database_id", peer.database_id())
    .detail("peer_lineage_id", peer.lineage_id())
}

#[cfg(test)]
mod tests {
    use super::*;

    fn assert_clone_send_sync<T: Clone + Send + Sync>() {}

    #[test]
    fn native_connection_handle_is_cloneable_send_and_sync() {
        assert_clone_send_sync::<Connection>();
    }

    #[test]
    fn database_identity_includes_lineage_not_only_address() {
        let first = DatabaseIdentity::new("catalog-name", "lineage-a");
        let same = DatabaseIdentity::new("catalog-name", "lineage-a");
        let replacement = DatabaseIdentity::new("catalog-name", "lineage-b");
        assert_eq!(first, same);
        assert_ne!(first, replacement);
        assert_eq!(first.database_id(), "catalog-name");
        assert_eq!(first.lineage_id(), "lineage-a");
    }

    #[test]
    fn identity_mismatch_is_a_fail_closed_fault() {
        let client = DatabaseIdentity::new("catalog-name", "lineage-a");
        let peer = DatabaseIdentity::new("catalog-name", "lineage-b");
        let error = identity_mismatch(&client, &peer);
        assert_eq!(error.category, crate::ErrorCategory::Fault);
        assert_eq!(error.code, "connection/database-identity-mismatch");
    }
}
