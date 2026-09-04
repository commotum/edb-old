//! Concrete PostgreSQL persistence for immutable persistent-index trees.
//!
//! Nodes are content values: write and verify them before publishing a root.
//! Manifests and their eight root bindings are installed transactionally, and
//! the append-only publication row is inserted last.  This module deliberately
//! does not define a storage trait; PostgreSQL is the only durable boundary.

use crate::postgres::{postgres_error, verify_schema_compatibility};
use crate::{Digest, ErrorCategory, IndexOrder, PostgresConnectionConfig, SemanticError, sha256};
use postgres::Client;
use sha2::{Digest as _, Sha256};
use std::collections::BTreeSet;

const TREE_MANIFEST_VERSION: i16 = 4;
const ROOT_BINDING_COUNT: usize = 8;
const DELTA_INSERT_BATCH: usize = 512;

/// Measured physical work performed by one tree-store handle.
///
/// Attempts include failed/missing reads. Row and byte counters include only
/// content successfully verified after PostgreSQL returned it.
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub struct TreeStoreStats {
    pub build_intent_node_batches: u64,
    pub build_intent_node_writes: u64,
    pub node_insert_attempts: u64,
    pub node_writes: u64,
    pub node_reuses: u64,
    pub node_write_bytes: u64,
    pub node_read_attempts: u64,
    pub node_rows_read: u64,
    pub node_read_bytes: u64,
    pub manifest_read_attempts: u64,
    pub manifest_rows_read: u64,
    pub manifest_read_bytes: u64,
    pub manifest_write_attempts: u64,
    pub manifest_writes: u64,
    pub root_binding_writes: u64,
    pub publication_writes: u64,
    pub delta_insert_batches: u64,
    pub delta_node_writes: u64,
}

/// One of the eight roots named by a persistent-tree manifest.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeRootBinding {
    pub order: IndexOrder,
    pub history: bool,
    pub root_hash: Digest,
    pub datom_count: u64,
    /// Encoded bytes in the root node itself, not its complete subtree.
    pub encoded_bytes: u64,
}

/// Canonical manifest metadata bound to one authoritative database value.
///
/// `payload` is decoded and checked by the persistent-tree codec. This layer
/// independently checks its content hash, normalized root bindings, log/state
/// binding, and generation before returning it.
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct TreeManifestRecord {
    pub database_id: String,
    pub publication_revision: u64,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub state_hash: Digest,
    pub excision_generation: u64,
    pub eidx_frontier: u64,
    pub manifest_hash: Digest,
    pub payload: Vec<u8>,
    pub roots: Vec<TreeRootBinding>,
}

/// Exact physical membership change installed with a root publication.
///
/// Initial/full repair builds replace the one current membership once.
/// Incremental copy-on-write builds carry only changed-path additions and the
/// source-shaped old-minus-new garbage set. Unknown callers may publish a
/// root conservatively, but make no node collectible.
#[derive(Clone, Debug, Eq, PartialEq)]
pub enum TreePublicationDelta {
    Unknown,
    Replace {
        live_nodes: BTreeSet<Digest>,
    },
    Incremental {
        predecessor_manifest_hash: Digest,
        added_nodes: BTreeSet<Digest>,
        retired_nodes: BTreeSet<Digest>,
    },
}

/// Result of conditionally advancing the append-only physical root reference.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum TreePublishOutcome {
    /// This call installed the requested next revision.
    Published,
    /// The identical revision was already durable, as after an ambiguous
    /// client-side outcome. No row was rewritten or inserted by this call.
    AlreadyPublished,
}

/// Direct PostgreSQL owner for tree content and root publication.
pub struct PostgresTreeStore {
    client: Client,
    connection: PostgresConnectionConfig,
    stats: TreeStoreStats,
    active_build_intent: Option<Digest>,
    active_build_database_lock: Option<i64>,
}

impl PostgresTreeStore {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        let mut client = connection.connect_for("tree/connect")?;
        verify_schema_compatibility(&mut client)?;
        Ok(Self {
            client,
            connection: connection.clone(),
            stats: TreeStoreStats::default(),
            active_build_intent: None,
            active_build_database_lock: None,
        })
    }

    pub fn into_client(self) -> Client {
        self.client
    }

    pub fn stats(&self) -> TreeStoreStats {
        self.stats
    }

    pub fn reset_stats(&mut self) {
        self.stats = TreeStoreStats::default();
    }

    /// Reborrow a schema-checked session while retaining only local physical
    /// work counters. Tree values and publications are immutable/idempotent;
    /// callers may safely reselect and retry a complete operation afterward.
    pub fn reconnect(&mut self) -> Result<(), SemanticError> {
        let mut client = self.connection.connect_for("tree/reconnect")?;
        verify_schema_compatibility(&mut client)?;
        self.client = client;
        self.active_build_intent = None;
        self.active_build_database_lock = None;
        Ok(())
    }

    /// Record the exact content-first upload set before uploading any value
    /// and pin the intent to this PostgreSQL session. A crash releases the pin
    /// while retaining the durable heartbeat/ledger for age-gated GC.
    pub fn begin_build_intent(
        &mut self,
        database_id: &str,
        log_generation: u64,
        expected_revision: u64,
        manifest_hash: Digest,
        node_hashes: &BTreeSet<Digest>,
    ) -> Result<(), SemanticError> {
        if self.active_build_intent.is_some() {
            return Err(SemanticError::conflict(
                "tree/build-intent-already-active",
                "one tree-store session can pin only one content-first build at a time",
            ));
        }
        let database_lock_key: i64 = self
            .client
            .query_one(
                "SELECT atomic_tree_database_build_pin_key($1)",
                &[&database_id],
            )
            .map_err(|error| postgres_error("tree/database-build-pin-key", error))?
            .get(0);
        self.client
            .query_one("SELECT pg_advisory_lock_shared($1)", &[&database_lock_key])
            .map_err(|error| postgres_error("tree/database-build-pin", error))?;
        let lock_key = tree_build_advisory_key(&manifest_hash);
        if let Err(error) = self
            .client
            .query_one("SELECT pg_advisory_lock_shared($1)", &[&lock_key])
        {
            let _ = self.client.query_one(
                "SELECT pg_advisory_unlock_shared($1)",
                &[&database_lock_key],
            );
            return Err(postgres_error("tree/build-intent-pin", error));
        }
        let result = (|| {
            let expected_revision = sql_u64(expected_revision, "expected publication revision")?;
            let log_generation = sql_u64(log_generation, "tree build log generation")?;
            let publication_revision = expected_revision.checked_add(1).ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unsupported,
                    "tree/publication-revision-exhausted",
                    "persistent tree publication revision is exhausted",
                )
            })?;
            // An ambiguous successful publication may already have had its
            // bookkeeping intent drained. The immutable publication itself is
            // sufficient evidence; do not recreate a full upload ledger only
            // to resolve the retry.
            if let Some(row) = self
                .client
                .query_opt(
                    "SELECT database_id, publication_revision \
                       FROM atomic_tree_publications WHERE manifest_hash = $1",
                    &[&&manifest_hash[..]],
                )
                .map_err(|error| postgres_error("tree/build-intent-publication", error))?
            {
                if row.get::<_, String>(0) != database_id
                    || row.get::<_, i64>(1) != publication_revision
                {
                    return Err(fault(
                        "tree/build-intent-publication-conflict",
                        "manifest hash is published at a different tree coordinate",
                    ));
                }
                return Ok(());
            }

            let expected_node_count = i64::try_from(node_hashes.len()).map_err(|_| {
                SemanticError::new(
                    ErrorCategory::Unsupported,
                    "tree/build-intent-too-large",
                    "tree upload ledger exceeds PostgreSQL bigint",
                )
            })?;
            let node_set_hash = build_intent_node_set_hash(node_hashes);
            {
                let mut transaction = self
                    .client
                    .transaction()
                    .map_err(|error| postgres_error("tree/build-intent-begin", error))?;
                transaction
                    .execute(
                        "INSERT INTO atomic_tree_build_intents \
                               (manifest_hash, database_id, log_generation, expected_revision, \
                                expected_node_count, node_set_hash) \
                         VALUES ($1, $2, $3, $4, $5, $6) \
                         ON CONFLICT (manifest_hash) DO NOTHING",
                        &[
                            &&manifest_hash[..],
                            &database_id,
                            &log_generation,
                            &expected_revision,
                            &expected_node_count,
                            &&node_set_hash[..],
                        ],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-insert", error))?;
                let row = transaction
                    .query_one(
                        "SELECT database_id, log_generation, expected_revision, \
                                expected_node_count, staged_node_count, node_set_hash, intent_state \
                           FROM atomic_tree_build_intents WHERE manifest_hash = $1",
                        &[&&manifest_hash[..]],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-verify", error))?;
                let state: i16 = row.get(6);
                if row.get::<_, String>(0) != database_id
                    || row.get::<_, i64>(1) != log_generation
                    || row.get::<_, i64>(2) != expected_revision
                    || row.get::<_, i64>(3) != expected_node_count
                    || digest(row.get(5), "tree build node-set hash")? != node_set_hash
                {
                    return Err(fault(
                        "tree/build-intent-conflict",
                        "manifest hash is already bound to a different build intent",
                    ));
                }
                if state == 3 {
                    return Err(SemanticError::conflict(
                        "tree/build-intent-being-collected",
                        "an abandoned instance of this deterministic build is still being collected",
                    ));
                }
                if state == 2 {
                    let published: bool = transaction
                        .query_one(
                            "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                                            WHERE manifest_hash = $1)",
                            &[&&manifest_hash[..]],
                        )
                        .map_err(|error| {
                            postgres_error("tree/build-intent-published-verify", error)
                        })?
                        .get(0);
                    if !published || row.get::<_, i64>(4) != expected_node_count {
                        return Err(fault(
                            "tree/build-intent-published-state",
                            "consumed tree upload intent has no matching publication",
                        ));
                    }
                }
                transaction
                    .commit()
                    .map_err(|error| postgres_error("tree/build-intent-header-commit", error))?;
                if state == 2 {
                    return Ok(());
                }
            }

            let hashes = node_hashes.iter().collect::<Vec<_>>();
            for chunk in hashes.chunks(DELTA_INSERT_BATCH) {
                let batch = chunk.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
                let mut transaction = self
                    .client
                    .transaction()
                    .map_err(|error| postgres_error("tree/build-intent-batch-begin", error))?;
                let inserted = transaction
                    .execute(
                        "INSERT INTO atomic_tree_build_intent_nodes \
                               (manifest_hash, node_hash) \
                         SELECT $1, node_hash FROM unnest($2::bytea[]) AS node_hash \
                         ON CONFLICT DO NOTHING",
                        &[&&manifest_hash[..], &batch],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-node-insert", error))?;
                let advanced = transaction
                    .execute(
                        "UPDATE atomic_tree_build_intents \
                            SET staged_node_count = staged_node_count + $2, \
                                heartbeat_at = clock_timestamp() \
                          WHERE manifest_hash = $1 AND intent_state = 0 \
                            AND staged_node_count + $2 <= expected_node_count",
                        &[
                            &&manifest_hash[..],
                            &i64::try_from(inserted).map_err(|_| {
                                fault(
                                    "tree/build-intent-batch-count",
                                    "inserted upload-ledger batch count exceeds PostgreSQL bigint",
                                )
                            })?,
                        ],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-node-count", error))?;
                if advanced != 1 {
                    let row = transaction
                        .query_one(
                            "SELECT staged_node_count, expected_node_count, intent_state \
                               FROM atomic_tree_build_intents WHERE manifest_hash = $1",
                            &[&&manifest_hash[..]],
                        )
                        .map_err(|error| postgres_error("tree/build-intent-batch-state", error))?;
                    let state: i16 = row.get(2);
                    if row.get::<_, i64>(0) != expected_node_count
                        || row.get::<_, i64>(1) != expected_node_count
                        || !matches!(state, 1 | 2)
                    {
                        return Err(fault(
                            "tree/build-intent-node-conflict",
                            "manifest build intent did not stage its exact upload set",
                        ));
                    }
                }
                transaction
                    .commit()
                    .map_err(|error| postgres_error("tree/build-intent-batch-commit", error))?;
                self.stats.build_intent_node_batches =
                    self.stats.build_intent_node_batches.saturating_add(1);
                self.stats.build_intent_node_writes =
                    self.stats.build_intent_node_writes.saturating_add(inserted);
            }

            let mut transaction = self
                .client
                .transaction()
                .map_err(|error| postgres_error("tree/build-intent-seal-begin", error))?;
            let sealed = transaction
                .execute(
                    "UPDATE atomic_tree_build_intents \
                        SET intent_state = 1, heartbeat_at = clock_timestamp() \
                      WHERE manifest_hash = $1 AND intent_state = 0 \
                        AND staged_node_count = expected_node_count",
                    &[&&manifest_hash[..]],
                )
                .map_err(|error| postgres_error("tree/build-intent-seal", error))?;
            if sealed != 1 {
                let row = transaction
                    .query_one(
                        "SELECT staged_node_count, expected_node_count, intent_state \
                           FROM atomic_tree_build_intents WHERE manifest_hash = $1",
                        &[&&manifest_hash[..]],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-seal-state", error))?;
                let state: i16 = row.get(2);
                if row.get::<_, i64>(0) != expected_node_count
                    || row.get::<_, i64>(1) != expected_node_count
                    || !matches!(state, 1 | 2)
                {
                    return Err(fault(
                        "tree/build-intent-node-conflict",
                        "manifest build intent could not seal its exact upload set",
                    ));
                }
            }
            transaction
                .commit()
                .map_err(|error| postgres_error("tree/build-intent-seal-commit", error))
        })();
        if let Err(error) = result {
            let _ = self
                .client
                .query_one("SELECT pg_advisory_unlock_shared($1)", &[&lock_key]);
            let _ = self.client.query_one(
                "SELECT pg_advisory_unlock_shared($1)",
                &[&database_lock_key],
            );
            return Err(error);
        }
        self.active_build_intent = Some(manifest_hash);
        self.active_build_database_lock = Some(database_lock_key);
        Ok(())
    }

    /// Release the session liveness pin. The durable intent remains after a
    /// failed build and becomes a post-publication liveness pin until bounded
    /// membership folding and bookkeeping cleanup finish.
    pub fn release_build_intent(&mut self) -> Result<(), SemanticError> {
        let Some(manifest_hash) = self.active_build_intent.take() else {
            return Ok(());
        };
        let database_lock_key = self.active_build_database_lock.take().ok_or_else(|| {
            fault(
                "tree/database-build-pin-lost",
                "tree build session lost its database-scoped liveness coordinate",
            )
        })?;
        let manifest_unlocked = self
            .client
            .query_one(
                "SELECT pg_advisory_unlock_shared($1)",
                &[&tree_build_advisory_key(&manifest_hash)],
            )
            .map(|row| row.get::<_, bool>(0))
            .map_err(|error| postgres_error("tree/build-intent-unpin", error));
        let database_unlocked = self
            .client
            .query_one(
                "SELECT pg_advisory_unlock_shared($1)",
                &[&database_lock_key],
            )
            .map(|row| row.get::<_, bool>(0))
            .map_err(|error| postgres_error("tree/database-build-unpin", error));
        let manifest_unlocked = manifest_unlocked?;
        let database_unlocked = database_unlocked?;
        if !manifest_unlocked || !database_unlocked {
            return Err(fault(
                "tree/build-intent-pin-lost",
                "tree build session no longer holds both liveness pins",
            ));
        }
        Ok(())
    }

    /// Insert one immutable node or verify the identical value already stored
    /// at that hash. Hash disagreement fails closed before any SQL write.
    pub fn insert_node(
        &mut self,
        expected_hash: Digest,
        payload: &[u8],
    ) -> Result<(), SemanticError> {
        self.stats.node_insert_attempts = self.stats.node_insert_attempts.saturating_add(1);
        validate_content_hash(expected_hash, payload, "tree/node-hash-mismatch")?;

        let inserted = self
            .client
            .execute(
                "INSERT INTO atomic_tree_nodes (node_hash, payload) VALUES ($1, $2) \
                 ON CONFLICT (node_hash) DO NOTHING",
                &[&&expected_hash[..], &payload],
            )
            .map_err(|error| postgres_error("tree/node-insert", error))?;
        let row = self
            .client
            .query_one(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&expected_hash[..]],
            )
            .map_err(|error| postgres_error("tree/node-verify", error))?;
        let stored: Vec<u8> = row.get(0);
        if stored.as_slice() != payload || sha256(&stored) != expected_hash {
            return Err(fault(
                "tree/node-hash-conflict",
                "tree node hash is already bound to different or corrupt bytes",
            ));
        }

        if inserted == 1 {
            self.stats.node_writes = self.stats.node_writes.saturating_add(1);
            self.stats.node_write_bytes = self
                .stats
                .node_write_bytes
                .saturating_add(payload.len() as u64);
        } else {
            self.stats.node_reuses = self.stats.node_reuses.saturating_add(1);
        }
        Ok(())
    }

    /// Load and authenticate an immutable node. A missing hash is distinct
    /// from corrupt bytes so a tree walker can report the owning reference.
    pub fn load_node(&mut self, expected_hash: Digest) -> Result<Option<Vec<u8>>, SemanticError> {
        self.stats.node_read_attempts = self.stats.node_read_attempts.saturating_add(1);
        let row = self
            .client
            .query_opt(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash = $1",
                &[&&expected_hash[..]],
            )
            .map_err(|error| postgres_error("tree/node-read", error))?;
        let Some(row) = row else {
            return Ok(None);
        };
        let payload: Vec<u8> = row.get(0);
        validate_content_hash(expected_hash, &payload, "tree/node-content-corrupt")?;
        self.stats.node_rows_read = self.stats.node_rows_read.saturating_add(1);
        self.stats.node_read_bytes = self
            .stats
            .node_read_bytes
            .saturating_add(payload.len() as u64);
        Ok(Some(payload))
    }

    /// Publish one complete manifest by compare-and-set of the physical root
    /// revision. Nodes must already have been committed; the manifest,
    /// normalized roots, and final append-only publication are one SQL
    /// transaction. Repeating the exact revision is idempotent even if a later
    /// revision has since won another publication.
    pub fn publish_manifest(
        &mut self,
        manifest: &TreeManifestRecord,
        expected_revision: u64,
    ) -> Result<TreePublishOutcome, SemanticError> {
        self.publish_manifest_with_delta(
            manifest,
            expected_revision,
            &TreePublicationDelta::Unknown,
        )
    }

    /// Stage one complete authenticated candidate without publishing its
    /// root. This is the shared path used by ordinary indexing and by an
    /// inactive copy-on-write log generation that must be sealed before its
    /// small activation transaction. A failed/lost candidate remains tied to
    /// its build intent and is reclaimed only by that exact bounded ledger.
    pub fn stage_manifest_with_delta(
        &mut self,
        manifest: &TreeManifestRecord,
        expected_revision: u64,
        delta: &TreePublicationDelta,
    ) -> Result<(), SemanticError> {
        validate_manifest(manifest)?;
        validate_publication_delta(manifest, expected_revision, delta)?;
        if matches!(delta, TreePublicationDelta::Unknown) {
            return Err(SemanticError::incorrect(
                "tree/unknown-delta-staging",
                "an unknown membership has no exact abandoned-build ledger",
            ));
        }
        if self.active_build_intent != Some(manifest.manifest_hash) {
            return Err(SemanticError::incorrect(
                "tree/missing-build-intent",
                "complete tree staging requires a pinned pre-upload build intent",
            ));
        }
        if manifest.publication_revision
            != expected_revision.checked_add(1).ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Unsupported,
                    "tree/publication-revision-exhausted",
                    "persistent tree publication revision is exhausted",
                )
            })?
        {
            return Err(SemanticError::incorrect(
                "tree/publication-revision-mismatch",
                "manifest revision must be exactly one greater than the expected revision",
            ));
        }
        let revision = sql_u64(manifest.publication_revision, "publication revision")?;
        let basis = sql_u64(manifest.basis_t, "manifest basis")?;
        let generation = sql_u64(manifest.excision_generation, "excision generation")?;
        let frontier = sql_u64(manifest.eidx_frontier, "entity frontier")?;
        let lineage_id: String = self
            .client
            .query_opt(
                "SELECT lineage_id FROM atomic_databases WHERE database_id = $1",
                &[&manifest.database_id],
            )
            .map_err(|error| postgres_error("tree/stage-database", error))?
            .map(|row| row.get(0))
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "tree/database-not-found",
                    format!("database {} does not exist", manifest.database_id),
                )
            })?;
        let manifest_lineage = (manifest.excision_generation > 0).then_some(lineage_id.as_str());
        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("tree/stage-manifest-begin", error))?;
        verify_root_nodes(&mut transaction, manifest)?;
        let manifest_inserted = transaction
            .execute(
                "INSERT INTO atomic_tree_manifests \
                   (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                    excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                    log_generation, lineage_id) \
                 VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12) \
                 ON CONFLICT (manifest_hash) DO NOTHING",
                &[
                    &manifest.database_id,
                    &revision,
                    &basis,
                    &&manifest.tx_hash[..],
                    &&manifest.state_hash[..],
                    &generation,
                    &frontier,
                    &TREE_MANIFEST_VERSION,
                    &&manifest.manifest_hash[..],
                    &&manifest.payload[..],
                    &generation,
                    &manifest_lineage,
                ],
            )
            .map_err(|error| postgres_error("tree/stage-manifest-insert", error))?;
        verify_manifest_row(&mut transaction, manifest)?;
        let mut root_binding_writes = 0_u64;
        for root in &manifest.roots {
            let inserted = transaction
                .execute(
                    "INSERT INTO atomic_tree_manifest_roots \
                       (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
                     VALUES ($1, $2, $3, $4, $5, $6) ON CONFLICT DO NOTHING",
                    &[
                        &&manifest.manifest_hash[..],
                        &index_order_i16(root.order),
                        &root.history,
                        &&root.root_hash[..],
                        &sql_u64(root.datom_count, "root datom count")?,
                        &sql_u64(root.encoded_bytes, "root encoded bytes")?,
                    ],
                )
                .map_err(|error| postgres_error("tree/stage-root-insert", error))?;
            root_binding_writes = root_binding_writes.saturating_add(inserted);
            verify_root_row(&mut transaction, manifest.manifest_hash, root)?;
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("tree/stage-manifest-commit", error))?;
        let (delta_insert_batches, delta_node_writes) =
            stage_publication_delta(&mut self.client, manifest, delta)?;
        self.stats.manifest_writes = self.stats.manifest_writes.saturating_add(manifest_inserted);
        self.stats.root_binding_writes = self
            .stats
            .root_binding_writes
            .saturating_add(root_binding_writes);
        self.stats.delta_insert_batches = self
            .stats
            .delta_insert_batches
            .saturating_add(delta_insert_batches);
        self.stats.delta_node_writes = self
            .stats
            .delta_node_writes
            .saturating_add(delta_node_writes);
        Ok(())
    }

    pub fn publish_manifest_with_delta(
        &mut self,
        manifest: &TreeManifestRecord,
        expected_revision: u64,
        delta: &TreePublicationDelta,
    ) -> Result<TreePublishOutcome, SemanticError> {
        self.stats.manifest_write_attempts = self.stats.manifest_write_attempts.saturating_add(1);
        validate_manifest(manifest)?;
        validate_publication_delta(manifest, expected_revision, delta)?;
        if !matches!(delta, TreePublicationDelta::Unknown)
            && self.active_build_intent != Some(manifest.manifest_hash)
        {
            return Err(SemanticError::incorrect(
                "tree/missing-build-intent",
                "complete tree publication requires a pinned pre-upload build intent",
            ));
        }
        let required_revision = expected_revision.checked_add(1).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::Unsupported,
                "tree/publication-revision-exhausted",
                "persistent tree publication revision is exhausted",
            )
        })?;
        if manifest.publication_revision != required_revision {
            return Err(SemanticError::incorrect(
                "tree/publication-revision-mismatch",
                "manifest revision must be exactly one greater than the expected revision",
            )
            .detail("expected_revision", expected_revision.to_string())
            .detail(
                "candidate_revision",
                manifest.publication_revision.to_string(),
            ));
        }
        let revision = sql_u64(manifest.publication_revision, "publication revision")?;
        let basis = sql_u64(manifest.basis_t, "manifest basis")?;
        let generation = sql_u64(manifest.excision_generation, "excision generation")?;
        let frontier = sql_u64(manifest.eidx_frontier, "entity frontier")?;

        // A database has at most one incomplete publication fold: the SQL
        // root validator will not admit its successor until this work seals.
        // Drain a predecessor outside the root lock in bounded transactions.
        self.drain_publication_work(&manifest.database_id)?;
        let already_published: bool = self
            .client
            .query_one(
                "SELECT EXISTS (SELECT 1 FROM atomic_tree_publications \
                                WHERE database_id = $1 \
                                  AND publication_revision = $2 \
                                  AND manifest_hash = $3)",
                &[
                    &manifest.database_id,
                    &revision,
                    &&manifest.manifest_hash[..],
                ],
            )
            .map_err(|error| postgres_error("tree/publication-preflight", error))?
            .get(0);
        if !already_published && !matches!(delta, TreePublicationDelta::Unknown) {
            self.stage_manifest_with_delta(manifest, expected_revision, delta)?;
        }

        let mut transaction = self
            .client
            .transaction()
            .map_err(|error| postgres_error("tree/publication-begin", error))?;

        // The immutable database catalog row is the serialization point used
        // by both this path and the SQL publication trigger.
        let lineage_id: String = transaction
            .query_opt(
                "SELECT lineage_id FROM atomic_databases \
                 WHERE database_id = $1 FOR UPDATE",
                &[&manifest.database_id],
            )
            .map_err(|error| postgres_error("tree/publication-lock", error))?
            .map(|row| row.get(0))
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "tree/database-not-found",
                    format!("database {} does not exist", manifest.database_id),
                )
            })?;

        // Resolve an ambiguous outcome before testing the current root. An
        // exact old revision remains a successful retry after newer revisions
        // have been appended; a different value at that coordinate is a lost
        // compare-and-set, never permission to overwrite it.
        if let Some(row) = transaction
            .query_opt(
                "SELECT basis_t, tx_hash, manifest_hash \
                 FROM atomic_tree_publications \
                 WHERE database_id = $1 AND publication_revision = $2",
                &[&manifest.database_id, &revision],
            )
            .map_err(|error| postgres_error("tree/publication-retry-read", error))?
        {
            let stored_basis = pg_u64(row.get(0), "published tree basis")?;
            let stored_tx = digest(row.get(1), "published tree transaction hash")?;
            let stored_manifest = digest(row.get(2), "published tree manifest hash")?;
            if stored_basis != manifest.basis_t
                || stored_tx != manifest.tx_hash
                || stored_manifest != manifest.manifest_hash
            {
                return Err(SemanticError::conflict(
                    "tree/publication-revision-conflict",
                    "publication revision is already bound to a different persistent tree",
                )
                .detail(
                    "publication_revision",
                    manifest.publication_revision.to_string(),
                ));
            }
            verify_manifest_row(&mut transaction, manifest)?;
            verify_root_nodes(&mut transaction, manifest)?;
            for root in &manifest.roots {
                verify_root_row(&mut transaction, manifest.manifest_hash, root)?;
            }
            verify_published_delta(&mut transaction, manifest, delta)?;
            if self.active_build_intent == Some(manifest.manifest_hash) {
                transaction
                    .query_one(
                        "SELECT atomic_finish_tree_build($1)",
                        &[&&manifest.manifest_hash[..]],
                    )
                    .map_err(|error| postgres_error("tree/build-intent-finish", error))?;
            }
            transaction
                .commit()
                .map_err(|error| postgres_error("tree/publication-retry-commit", error))?;
            if !matches!(delta, TreePublicationDelta::Unknown) {
                self.advance_publication_work(manifest.manifest_hash)?;
            }
            return Ok(TreePublishOutcome::AlreadyPublished);
        }

        let current = transaction
            .query_opt(
                "SELECT publication_revision, basis_t, log_generation, manifest_hash \
                 FROM atomic_tree_publications WHERE database_id = $1 \
                 ORDER BY publication_revision DESC LIMIT 1",
                &[&manifest.database_id],
            )
            .map_err(|error| postgres_error("tree/publication-current", error))?;
        let (observed_revision, observed_basis, observed_generation, observed_manifest) =
            if let Some(row) = current {
                (
                    pg_u64(row.get(0), "current tree publication revision")?,
                    Some(pg_u64(row.get(1), "current tree publication basis")?),
                    Some(pg_u64(row.get(2), "current tree publication generation")?),
                    Some(digest(row.get(3), "current tree publication manifest")?),
                )
            } else {
                (0, None, None, None)
            };
        if observed_revision != expected_revision {
            return Err(SemanticError::conflict(
                "tree/publication-cas-lost",
                "persistent tree root changed after the candidate build began",
            )
            .detail("expected_revision", expected_revision.to_string())
            .detail("observed_revision", observed_revision.to_string()));
        }
        if observed_generation == Some(manifest.excision_generation)
            && observed_basis.is_some_and(|current_basis| manifest.basis_t < current_basis)
        {
            return Err(SemanticError::conflict(
                "tree/publication-basis-regression",
                "persistent tree publication cannot regress the logical transaction basis",
            )
            .detail("candidate_basis", manifest.basis_t.to_string())
            .detail(
                "current_basis",
                observed_basis.expect("checked as present").to_string(),
            ));
        }

        // Heartbeat only the genuinely new publication path. An ambiguous
        // retry may run after the root trigger consumed the intent and after
        // bounded bookkeeping cleanup removed its header; the immutable
        // coordinate check above is sufficient for that case.
        if self.active_build_intent == Some(manifest.manifest_hash) {
            transaction
                .query_one(
                    "SELECT atomic_heartbeat_tree_build($1)",
                    &[&&manifest.manifest_hash[..]],
                )
                .map_err(|error| postgres_error("tree/build-intent-heartbeat", error))?;
        }

        // Verify every root was durably stored before creating any manifest
        // record. The SQL publication trigger repeats the existence/size gate.
        verify_root_nodes(&mut transaction, manifest)?;

        let mut manifest_inserted = 0_u64;
        let mut root_binding_writes = 0_u64;
        if matches!(delta, TreePublicationDelta::Unknown) {
            let manifest_lineage =
                (manifest.excision_generation > 0).then_some(lineage_id.as_str());
            manifest_inserted = transaction
                .execute(
                    "INSERT INTO atomic_tree_manifests \
                       (database_id, publication_revision, basis_t, tx_hash, state_hash, \
                        excision_generation, eidx_frontier, manifest_version, manifest_hash, payload, \
                        log_generation, lineage_id) \
                     VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12) \
                     ON CONFLICT (manifest_hash) DO NOTHING",
                    &[
                        &manifest.database_id,
                        &revision,
                        &basis,
                        &&manifest.tx_hash[..],
                        &&manifest.state_hash[..],
                        &generation,
                        &frontier,
                        &TREE_MANIFEST_VERSION,
                        &&manifest.manifest_hash[..],
                        &&manifest.payload[..],
                        &generation,
                        &manifest_lineage,
                    ],
                )
                .map_err(|error| postgres_error("tree/manifest-insert", error))?;
        }
        verify_manifest_row(&mut transaction, manifest)?;

        for root in &manifest.roots {
            let order = index_order_i16(root.order);
            let count = sql_u64(root.datom_count, "root datom count")?;
            let bytes = sql_u64(root.encoded_bytes, "root encoded bytes")?;
            let inserted = if matches!(delta, TreePublicationDelta::Unknown) {
                transaction
                    .execute(
                        "INSERT INTO atomic_tree_manifest_roots \
                           (manifest_hash, index_order, history, root_hash, datom_count, encoded_bytes) \
                         VALUES ($1, $2, $3, $4, $5, $6) ON CONFLICT DO NOTHING",
                        &[
                            &&manifest.manifest_hash[..],
                            &order,
                            &root.history,
                            &&root.root_hash[..],
                            &count,
                            &bytes,
                        ],
                    )
                    .map_err(|error| postgres_error("tree/root-insert", error))?
            } else {
                0
            };
            root_binding_writes = root_binding_writes.saturating_add(inserted);
            verify_root_row(&mut transaction, manifest.manifest_hash, root)?;
        }

        if matches!(delta, TreePublicationDelta::Unknown) {
            stage_unknown_delta(&mut transaction, manifest.manifest_hash, observed_manifest)?;
        }

        // This is intentionally the final write. A crash or rollback before
        // here cannot make the candidate discoverable by a peer.
        transaction
            .query_one(
                "SELECT atomic_publish_tree($1, $2, $3, $4, $5)",
                &[
                    &manifest.database_id,
                    &revision,
                    &basis,
                    &&manifest.tx_hash[..],
                    &&manifest.manifest_hash[..],
                ],
            )
            .map_err(|error| postgres_error("tree/publication-insert", error))?;
        let publication_inserted = 1;
        verify_publication_row(&mut transaction, manifest)?;
        transaction
            .commit()
            .map_err(|error| postgres_error("tree/publication-commit", error))?;

        self.stats.manifest_writes = self.stats.manifest_writes.saturating_add(manifest_inserted);
        self.stats.root_binding_writes = self
            .stats
            .root_binding_writes
            .saturating_add(root_binding_writes);
        self.stats.publication_writes = self
            .stats
            .publication_writes
            .saturating_add(publication_inserted);
        if !matches!(delta, TreePublicationDelta::Unknown) {
            self.advance_publication_work(manifest.manifest_hash)?;
        }
        Ok(TreePublishOutcome::Published)
    }

    /// Advance one already-visible publication's derived liveness by at most
    /// one fixed batch. `true` means the fold is now sealed as the current
    /// live set. `false` means either another bounded call remains or this
    /// manifest was already sealed and subsequently superseded; callers that
    /// resume work select manifests from the durable delta-header ledger.
    pub fn advance_publication_work(
        &mut self,
        manifest_hash: Digest,
    ) -> Result<bool, SemanticError> {
        self.client
            .query_one(
                "SELECT atomic_apply_tree_publication_work($1, $2)",
                &[&&manifest_hash[..], &(DELTA_INSERT_BATCH as i64)],
            )
            .map(|row| row.get(0))
            .map_err(|error| postgres_error("tree/publication-work", error))
    }

    fn drain_publication_work(&mut self, database_id: &str) -> Result<(), SemanticError> {
        loop {
            let pending = self
                .client
                .query_opt(
                    "SELECT h.manifest_hash \
                       FROM atomic_tree_delta_headers h \
                       JOIN atomic_tree_publications p ON p.manifest_hash = h.manifest_hash \
                      WHERE p.database_id = $1 AND h.delta_state = 2 \
                      ORDER BY p.publication_revision LIMIT 1",
                    &[&database_id],
                )
                .map_err(|error| postgres_error("tree/publication-work-read", error))?;
            let Some(row) = pending else {
                return Ok(());
            };
            let manifest_hash = digest(row.get(0), "pending tree publication hash")?;
            self.advance_publication_work(manifest_hash)?;
        }
    }

    /// Read the current physical root revision, or zero before the first
    /// publication. Builders retain this value as their compare-and-set input.
    pub fn current_publication_revision(
        &mut self,
        database_id: &str,
    ) -> Result<u64, SemanticError> {
        let row = self
            .client
            .query_opt(
                "SELECT publication_revision FROM atomic_tree_publications \
                 WHERE database_id = $1 ORDER BY publication_revision DESC LIMIT 1",
                &[&database_id],
            )
            .map_err(|error| postgres_error("tree/publication-current-read", error))?;
        row.map(|row| pg_u64(row.get(0), "current publication revision"))
            .transpose()
            .map(|revision| revision.unwrap_or(0))
    }

    /// Find the newest physical publication whose logical basis is at or
    /// before `target_basis`. Reading it remains a separate authenticated step
    /// so callers can reject one corrupt derived value and try an older
    /// revision, including an older revision at the same basis.
    pub fn latest_published_revision(
        &mut self,
        database_id: &str,
        target_basis: u64,
    ) -> Result<Option<u64>, SemanticError> {
        let target = sql_u64(target_basis, "target basis")?;
        let row = self
            .client
            .query_opt(
                "SELECT publication_revision FROM atomic_tree_publications \
                 WHERE database_id = $1 AND basis_t <= $2 \
                 ORDER BY publication_revision DESC LIMIT 1",
                &[&database_id, &target],
            )
            .map_err(|error| postgres_error("tree/publication-read", error))?;
        row.map(|row| pg_u64(row.get(0), "published revision"))
            .transpose()
    }

    /// Load one publication and authenticate all SQL metadata and normalized
    /// roots. The persistent-tree decoder must additionally prove that the
    /// canonical manifest envelope names these same roots.
    pub fn load_manifest(
        &mut self,
        database_id: &str,
        publication_revision: u64,
    ) -> Result<Option<TreeManifestRecord>, SemanticError> {
        self.stats.manifest_read_attempts = self.stats.manifest_read_attempts.saturating_add(1);
        let revision = sql_u64(publication_revision, "publication revision")?;
        let row = self
            .client
            .query_opt(
                "SELECT m.basis_t, m.tx_hash, m.state_hash, m.excision_generation, \
                        m.eidx_frontier, m.manifest_version, m.manifest_hash, m.payload, \
                        m.log_generation, m.lineage_id, \
                        COALESCE(legacy.tx_hash, native.tx_hash), \
                        COALESCE(legacy.state_hash, native.state_hash), \
                        h.log_generation, d.lineage_id, p.log_generation \
                   FROM atomic_tree_publications p \
                   JOIN atomic_tree_manifests m \
                     ON m.database_id = p.database_id \
                    AND m.publication_revision = p.publication_revision \
                    AND m.basis_t = p.basis_t AND m.tx_hash = p.tx_hash \
                    AND m.manifest_hash = p.manifest_hash \
                    AND m.log_generation = p.log_generation \
                   JOIN atomic_heads h ON h.database_id = m.database_id \
                   JOIN atomic_databases d ON d.database_id = m.database_id \
                   LEFT JOIN atomic_transactions legacy \
                     ON m.log_generation = 0 \
                    AND legacy.database_id = m.database_id \
                    AND legacy.basis_t = m.basis_t AND legacy.tx_hash = m.tx_hash \
                   LEFT JOIN atomic_generation_transactions native \
                     ON m.log_generation > 0 \
                    AND native.database_id = m.database_id \
                    AND native.generation = m.log_generation \
                    AND native.basis_t = m.basis_t AND native.tx_hash = m.tx_hash \
                  WHERE p.database_id = $1 AND p.publication_revision = $2",
                &[&database_id, &revision],
            )
            .map_err(|error| postgres_error("tree/manifest-read", error))?;
        let Some(row) = row else {
            return Ok(None);
        };

        let basis_t = pg_u64(row.get(0), "tree manifest basis")?;
        let tx_hash = digest(row.get(1), "tree manifest transaction hash")?;
        let state_hash = digest(row.get(2), "tree manifest state hash")?;
        let generation = pg_u64(row.get(3), "tree manifest generation")?;
        let frontier = pg_u64(row.get(4), "tree manifest entity frontier")?;
        let version: i16 = row.get(5);
        let manifest_hash = digest(row.get(6), "tree manifest hash")?;
        let payload: Vec<u8> = row.get(7);
        let stored_log_generation = pg_u64(row.get(8), "tree manifest log generation")?;
        let stored_lineage: Option<String> = row.get(9);
        let authoritative_tx: Option<Vec<u8>> = row.get(10);
        let authoritative_state: Option<Vec<u8>> = row.get(11);
        let current_generation = pg_u64(row.get(12), "current log generation")?;
        let durable_lineage: String = row.get(13);
        let publication_generation = pg_u64(row.get(14), "tree publication generation")?;
        if version != TREE_MANIFEST_VERSION
            || stored_log_generation != generation
            || publication_generation != generation
            || current_generation != generation
            || if generation == 0 {
                stored_lineage.is_some()
            } else {
                stored_lineage.as_deref() != Some(durable_lineage.as_str())
            }
            || authoritative_tx
                .map(|bytes| digest(bytes, "authoritative transaction hash"))
                .transpose()?
                != Some(tx_hash)
            || authoritative_state
                .map(|bytes| digest(bytes, "authoritative state hash"))
                .transpose()?
                != Some(state_hash)
        {
            return Err(fault(
                "tree/unauthenticated-manifest",
                "tree manifest is stale or disagrees with the authoritative log",
            ));
        }
        validate_content_hash(manifest_hash, &payload, "tree/manifest-content-corrupt")?;

        let roots = load_root_rows(&mut self.client, manifest_hash)?;
        let manifest = TreeManifestRecord {
            database_id: database_id.to_owned(),
            publication_revision,
            basis_t,
            tx_hash,
            state_hash,
            excision_generation: generation,
            eidx_frontier: frontier,
            manifest_hash,
            payload,
            roots,
        };
        validate_manifest(&manifest)?;
        self.stats.manifest_rows_read = self.stats.manifest_rows_read.saturating_add(1);
        self.stats.manifest_read_bytes = self
            .stats
            .manifest_read_bytes
            .saturating_add(manifest.payload.len() as u64);
        Ok(Some(manifest))
    }
}

/// Separate advisory namespace for active content-first build intents.
pub(crate) fn tree_build_advisory_key(manifest_hash: &Digest) -> i64 {
    let mut key = [0_u8; 8];
    key.copy_from_slice(&manifest_hash[..8]);
    i64::from_be_bytes(key) ^ 0x4154_4249_0000_0000_i64
}

/// Compact commitment to the complete, sorted upload ledger.  Staging writes
/// the ledger in bounded transactions, so the header must bind every retry to
/// the same set before any partial rows are reused.
fn build_intent_node_set_hash(node_hashes: &BTreeSet<Digest>) -> Digest {
    let mut hasher = Sha256::new();
    hasher.update(b"atomic/tree-build-node-set/v1\0");
    hasher.update((node_hashes.len() as u64).to_be_bytes());
    for node_hash in node_hashes {
        hasher.update(node_hash);
    }
    hasher.finalize().into()
}

fn validate_publication_delta(
    manifest: &TreeManifestRecord,
    expected_revision: u64,
    delta: &TreePublicationDelta,
) -> Result<(), SemanticError> {
    match delta {
        TreePublicationDelta::Unknown => Ok(()),
        TreePublicationDelta::Replace { live_nodes } => {
            if manifest
                .roots
                .iter()
                .any(|root| !live_nodes.contains(&root.root_hash))
            {
                return Err(SemanticError::incorrect(
                    "tree/replacement-delta-missing-root",
                    "complete replacement membership must contain all eight roots",
                ));
            }
            Ok(())
        }
        TreePublicationDelta::Incremental {
            added_nodes,
            retired_nodes,
            ..
        } => {
            if expected_revision == 0 {
                return Err(SemanticError::incorrect(
                    "tree/incremental-delta-without-predecessor",
                    "incremental membership requires a predecessor root",
                ));
            }
            if !added_nodes.is_disjoint(retired_nodes) {
                return Err(SemanticError::incorrect(
                    "tree/overlapping-node-delta",
                    "one content hash cannot be both added and retired",
                ));
            }
            Ok(())
        }
    }
}

fn stage_publication_delta(
    client: &mut Client,
    manifest: &TreeManifestRecord,
    delta: &TreePublicationDelta,
) -> Result<(u64, u64), SemanticError> {
    let predecessor = client
        .query_opt(
            "SELECT manifest_hash FROM atomic_tree_publications \
              WHERE database_id = $1 ORDER BY publication_revision DESC LIMIT 1",
            &[&manifest.database_id],
        )
        .map_err(|error| postgres_error("tree/delta-predecessor", error))?
        .map(|row| digest(row.get(0), "delta predecessor manifest"))
        .transpose()?;
    let empty = BTreeSet::new();
    let (mode, claimed_predecessor, added, retired): (
        i16,
        Option<Digest>,
        &BTreeSet<Digest>,
        &BTreeSet<Digest>,
    ) = match delta {
        TreePublicationDelta::Unknown => {
            return Err(fault(
                "tree/unknown-delta-staging",
                "unknown tree deltas are staged only in their constant root transaction",
            ));
        }
        TreePublicationDelta::Replace { live_nodes } => (1, predecessor, live_nodes, &empty),
        TreePublicationDelta::Incremental {
            predecessor_manifest_hash,
            added_nodes,
            retired_nodes,
        } => (
            2,
            Some(*predecessor_manifest_hash),
            added_nodes,
            retired_nodes,
        ),
    };
    let expected_node_count = added.len().checked_add(retired.len()).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "tree/delta-too-large",
            "tree publication work exceeds addressable memory",
        )
    })?;
    let expected_node_count = i64::try_from(expected_node_count).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "tree/delta-too-large",
            "tree publication work exceeds PostgreSQL bigint",
        )
    })?;
    let added_node_count = i64::try_from(added.len()).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "tree/delta-too-large",
            "tree publication additions exceed PostgreSQL bigint",
        )
    })?;
    let added_set_hash = build_intent_node_set_hash(added);
    let delta_set_hash = publication_delta_set_hash(mode, added, retired);
    {
        let mut transaction = client
            .transaction()
            .map_err(|error| postgres_error("tree/delta-header-begin", error))?;
        transaction
            .execute(
                "INSERT INTO atomic_tree_delta_headers \
                       (manifest_hash, predecessor_manifest_hash, delta_mode, \
                        expected_node_count, delta_set_hash, added_node_count, \
                        added_set_hash) \
                 VALUES ($1, $2, $3, $4, $5, $6, $7) \
                 ON CONFLICT (manifest_hash) DO NOTHING",
                &[
                    &&manifest.manifest_hash[..],
                    &claimed_predecessor.as_ref().map(|hash| &hash[..]),
                    &mode,
                    &expected_node_count,
                    &&delta_set_hash[..],
                    &added_node_count,
                    &&added_set_hash[..],
                ],
            )
            .map_err(|error| postgres_error("tree/delta-header-insert", error))?;
        let row = transaction
            .query_one(
                "SELECT predecessor_manifest_hash, delta_mode, expected_node_count, \
                        staged_node_count, delta_set_hash, added_node_count, \
                        added_set_hash, delta_state \
                   FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
                &[&&manifest.manifest_hash[..]],
            )
            .map_err(|error| postgres_error("tree/delta-header-verify", error))?;
        let stored_predecessor = row
            .get::<_, Option<Vec<u8>>>(0)
            .map(|bytes| digest(bytes, "staged delta predecessor"))
            .transpose()?;
        let state: i16 = row.get(7);
        if stored_predecessor != claimed_predecessor
            || row.get::<_, i16>(1) != mode
            || row.get::<_, i64>(2) != expected_node_count
            || digest(row.get(4), "staged delta set hash")? != delta_set_hash
            || row.get::<_, i64>(5) != added_node_count
            || digest(row.get(6), "staged delta addition hash")? != added_set_hash
        {
            return Err(fault(
                "tree/delta-header-conflict",
                "manifest hash is already bound to different publication work",
            ));
        }
        if state == 2 {
            transaction
                .commit()
                .map_err(|error| postgres_error("tree/delta-header-commit", error))?;
            return Ok((0, 0));
        }
        if state != 0 && state != 1 {
            return Err(fault(
                "tree/delta-header-state",
                "tree publication work has an invalid lifecycle state",
            ));
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("tree/delta-header-commit", error))?;
    }
    let mut batches = 0_u64;
    let mut writes = 0_u64;
    let (one_batches, one_writes) = insert_delta_nodes(client, manifest.manifest_hash, added, 1)?;
    batches = batches.saturating_add(one_batches);
    writes = writes.saturating_add(one_writes);
    let (one_batches, one_writes) =
        insert_delta_nodes(client, manifest.manifest_hash, retired, -1)?;
    batches = batches.saturating_add(one_batches);
    writes = writes.saturating_add(one_writes);
    let mut transaction = client
        .transaction()
        .map_err(|error| postgres_error("tree/delta-seal-begin", error))?;
    let sealed = transaction
        .execute(
            "UPDATE atomic_tree_delta_headers \
                SET delta_state = 1 \
              WHERE manifest_hash = $1 AND delta_state = 0 \
                AND staged_node_count = expected_node_count",
            &[&&manifest.manifest_hash[..]],
        )
        .map_err(|error| postgres_error("tree/delta-seal", error))?;
    if sealed != 1 {
        let row = transaction
            .query_one(
                "SELECT staged_node_count, expected_node_count, delta_state \
                   FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
                &[&&manifest.manifest_hash[..]],
            )
            .map_err(|error| postgres_error("tree/delta-seal-state", error))?;
        if row.get::<_, i64>(0) != expected_node_count
            || row.get::<_, i64>(1) != expected_node_count
            || !matches!(row.get::<_, i16>(2), 1 | 2)
        {
            return Err(fault(
                "tree/delta-node-conflict",
                "tree publication work could not seal its exact delta set",
            ));
        }
    }
    transaction
        .commit()
        .map_err(|error| postgres_error("tree/delta-seal-commit", error))?;
    Ok((batches, writes))
}

fn insert_delta_nodes(
    client: &mut Client,
    manifest_hash: Digest,
    hashes: &BTreeSet<Digest>,
    direction: i16,
) -> Result<(u64, u64), SemanticError> {
    let mut batches = 0_u64;
    let mut writes = 0_u64;
    let hashes = hashes.iter().collect::<Vec<_>>();
    for chunk in hashes.chunks(DELTA_INSERT_BATCH) {
        let batch = chunk.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
        let mut transaction = client
            .transaction()
            .map_err(|error| postgres_error("tree/delta-node-begin", error))?;
        let inserted = transaction
            .execute(
                "INSERT INTO atomic_tree_delta_nodes (manifest_hash, node_hash, direction) \
                 SELECT $1, node_hash, $3 \
                   FROM unnest($2::bytea[]) AS node_hash \
                 ON CONFLICT DO NOTHING",
                &[&&manifest_hash[..], &batch, &direction],
            )
            .map_err(|error| postgres_error("tree/delta-node-insert", error))?;
        let advanced = transaction
            .execute(
                "UPDATE atomic_tree_delta_headers \
                    SET staged_node_count = staged_node_count + $2 \
                  WHERE manifest_hash = $1 AND delta_state = 0 \
                    AND staged_node_count + $2 <= expected_node_count",
                &[
                    &&manifest_hash[..],
                    &i64::try_from(inserted).map_err(|_| {
                        fault(
                            "tree/delta-node-count",
                            "inserted delta batch count exceeds PostgreSQL bigint",
                        )
                    })?,
                ],
            )
            .map_err(|error| postgres_error("tree/delta-node-count", error))?;
        if advanced != 1 {
            let row = transaction
                .query_one(
                    "SELECT staged_node_count, expected_node_count, delta_state \
                       FROM atomic_tree_delta_headers WHERE manifest_hash = $1",
                    &[&&manifest_hash[..]],
                )
                .map_err(|error| postgres_error("tree/delta-node-state", error))?;
            if row.get::<_, i64>(0) != row.get::<_, i64>(1)
                || !matches!(row.get::<_, i16>(2), 1 | 2)
            {
                return Err(fault(
                    "tree/delta-node-conflict",
                    "tree publication work did not stage its exact delta set",
                ));
            }
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("tree/delta-node-commit", error))?;
        batches = batches.saturating_add(1);
        writes = writes.saturating_add(inserted);
    }
    Ok((batches, writes))
}

fn stage_unknown_delta(
    client: &mut postgres::Transaction<'_>,
    manifest_hash: Digest,
    predecessor_manifest_hash: Option<Digest>,
) -> Result<(), SemanticError> {
    let empty = BTreeSet::new();
    let empty_set_hash = build_intent_node_set_hash(&empty);
    let delta_set_hash = publication_delta_set_hash(0, &empty, &empty);
    client
        .execute(
            "INSERT INTO atomic_tree_delta_headers \
                   (manifest_hash, predecessor_manifest_hash, delta_mode, \
                    expected_node_count, staged_node_count, delta_set_hash, \
                    added_node_count, added_set_hash, delta_state) \
             VALUES ($1, $2, 0, 0, 0, $3, 0, $4, 1)",
            &[
                &&manifest_hash[..],
                &predecessor_manifest_hash.as_ref().map(|hash| &hash[..]),
                &&delta_set_hash[..],
                &&empty_set_hash[..],
            ],
        )
        .map_err(|error| postgres_error("tree/unknown-delta-header", error))?;
    Ok(())
}

fn publication_delta_set_hash(
    mode: i16,
    added: &BTreeSet<Digest>,
    retired: &BTreeSet<Digest>,
) -> Digest {
    let mut hasher = Sha256::new();
    hasher.update(b"atomic/tree-publication-delta/v1\0");
    hasher.update(mode.to_be_bytes());
    hasher.update(((added.len() + retired.len()) as u64).to_be_bytes());
    for node_hash in added {
        hasher.update([1]);
        hasher.update(node_hash);
    }
    for node_hash in retired {
        hasher.update([u8::MAX]);
        hasher.update(node_hash);
    }
    hasher.finalize().into()
}

fn verify_published_delta(
    client: &mut postgres::Transaction<'_>,
    manifest: &TreeManifestRecord,
    delta: &TreePublicationDelta,
) -> Result<(), SemanticError> {
    let row = client
        .query_opt(
            "SELECT s.predecessor_manifest_hash, s.delta_mode, \
                    NOT EXISTS (SELECT 1 FROM atomic_tree_publications newer \
                                WHERE newer.database_id = p.database_id \
                                  AND newer.publication_revision > p.publication_revision), \
                    l.manifest_hash, l.complete, \
                    EXISTS (SELECT 1 FROM atomic_tree_delta_headers h \
                            WHERE h.manifest_hash = p.manifest_hash \
                              AND h.delta_state = 2) \
               FROM atomic_tree_publications p \
               JOIN atomic_tree_publication_states s \
                 ON s.manifest_hash = p.manifest_hash \
               LEFT JOIN atomic_tree_live_sets l ON l.database_id = p.database_id \
              WHERE p.database_id = $1 AND p.publication_revision = $2 \
                AND p.manifest_hash = $3",
            &[
                &manifest.database_id,
                &sql_u64(manifest.publication_revision, "publication revision")?,
                &&manifest.manifest_hash[..],
            ],
        )
        .map_err(|error| postgres_error("tree/publication-delta-verify", error))?
        .ok_or_else(|| {
            fault(
                "tree/publication-delta-missing",
                "published tree is missing its delta provenance",
            )
        })?;
    let stored_predecessor = row
        .get::<_, Option<Vec<u8>>>(0)
        .map(|bytes| digest(bytes, "stored delta predecessor"))
        .transpose()?;
    let stored_mode: i16 = row.get(1);
    let is_current: bool = row.get(2);
    let live_manifest = row
        .get::<_, Option<Vec<u8>>>(3)
        .map(|bytes| digest(bytes, "current live manifest"))
        .transpose()?;
    let live_complete: Option<bool> = row.get(4);
    let fold_pending: bool = row.get(5);
    let (expected_mode, expected_predecessor, expects_complete) = match delta {
        TreePublicationDelta::Unknown => (0, None, None),
        TreePublicationDelta::Replace { .. } => (1, None, Some(true)),
        TreePublicationDelta::Incremental {
            predecessor_manifest_hash,
            ..
        } => (2, Some(*predecessor_manifest_hash), Some(true)),
    };
    if stored_mode != expected_mode
        || expected_predecessor.is_some_and(|expected| stored_predecessor != Some(expected))
        || (is_current
            && !fold_pending
            && (live_manifest != Some(manifest.manifest_hash)
                || expects_complete.is_some_and(|expected| live_complete != Some(expected))))
    {
        return Err(fault(
            "tree/publication-delta-mismatch",
            "published tree delta provenance or current live membership is inconsistent",
        ));
    }
    Ok(())
}

fn verify_manifest_row(
    client: &mut postgres::Transaction<'_>,
    expected: &TreeManifestRecord,
) -> Result<(), SemanticError> {
    let row = client
        .query_opt(
            "SELECT database_id, publication_revision, basis_t, tx_hash, state_hash, \
                    excision_generation, eidx_frontier, manifest_version, payload, \
                    log_generation, lineage_id, \
                    (SELECT lineage_id FROM atomic_databases \
                      WHERE database_id = atomic_tree_manifests.database_id) \
             FROM atomic_tree_manifests WHERE manifest_hash = $1",
            &[&&expected.manifest_hash[..]],
        )
        .map_err(|error| postgres_error("tree/manifest-verify", error))?
        .ok_or_else(|| {
            fault(
                "tree/manifest-conflict",
                "manifest identity is already occupied by a different value",
            )
        })?;
    let stored_database: String = row.get(0);
    let stored_revision = pg_u64(row.get(1), "stored tree publication revision")?;
    let stored_basis = pg_u64(row.get(2), "stored tree basis")?;
    let stored_tx = digest(row.get(3), "stored tree transaction hash")?;
    let stored_state = digest(row.get(4), "stored tree state hash")?;
    let stored_generation = pg_u64(row.get(5), "stored tree generation")?;
    let stored_frontier = pg_u64(row.get(6), "stored tree entity frontier")?;
    let stored_version: i16 = row.get(7);
    let stored_payload: Vec<u8> = row.get(8);
    let stored_log_generation = pg_u64(row.get(9), "stored tree log generation")?;
    let stored_lineage: Option<String> = row.get(10);
    let durable_lineage: Option<String> = row.get(11);
    if stored_database != expected.database_id
        || stored_revision != expected.publication_revision
        || stored_basis != expected.basis_t
        || stored_tx != expected.tx_hash
        || stored_state != expected.state_hash
        || stored_generation != expected.excision_generation
        || stored_frontier != expected.eidx_frontier
        || stored_version != TREE_MANIFEST_VERSION
        || stored_payload != expected.payload
        || stored_log_generation != expected.excision_generation
        || if expected.excision_generation == 0 {
            stored_lineage.is_some()
        } else {
            stored_lineage != durable_lineage || stored_lineage.is_none()
        }
    {
        return Err(fault(
            "tree/manifest-conflict",
            "manifest hash is already bound to different publication metadata",
        ));
    }
    Ok(())
}

fn verify_root_row(
    client: &mut postgres::Transaction<'_>,
    manifest_hash: Digest,
    expected: &TreeRootBinding,
) -> Result<(), SemanticError> {
    let order = index_order_i16(expected.order);
    let row = client
        .query_one(
            "SELECT root_hash, datom_count, encoded_bytes \
             FROM atomic_tree_manifest_roots \
             WHERE manifest_hash = $1 AND index_order = $2 AND history = $3",
            &[&&manifest_hash[..], &order, &expected.history],
        )
        .map_err(|error| postgres_error("tree/root-verify", error))?;
    let hash = digest(row.get(0), "stored tree root hash")?;
    let count = pg_u64(row.get(1), "stored root datom count")?;
    let bytes = pg_u64(row.get(2), "stored root encoded bytes")?;
    if hash != expected.root_hash
        || count != expected.datom_count
        || bytes != expected.encoded_bytes
    {
        return Err(fault(
            "tree/root-conflict",
            "manifest root coordinate is already bound to different metadata",
        ));
    }
    Ok(())
}

fn verify_publication_row(
    client: &mut postgres::Transaction<'_>,
    expected: &TreeManifestRecord,
) -> Result<(), SemanticError> {
    let row = client
        .query_opt(
            "SELECT basis_t, tx_hash, manifest_hash, log_generation \
             FROM atomic_tree_publications \
             WHERE database_id = $1 AND publication_revision = $2",
            &[
                &expected.database_id,
                &sql_u64(expected.publication_revision, "publication revision")?,
            ],
        )
        .map_err(|error| postgres_error("tree/publication-verify", error))?
        .ok_or_else(|| {
            fault(
                "tree/missing-publication",
                "persistent tree publication disappeared before verification",
            )
        })?;
    if pg_u64(row.get(0), "published tree basis")? != expected.basis_t
        || digest(row.get(1), "published tree transaction hash")? != expected.tx_hash
        || digest(row.get(2), "published tree manifest hash")? != expected.manifest_hash
        || pg_u64(row.get(3), "published tree log generation")? != expected.excision_generation
    {
        return Err(fault(
            "tree/publication-conflict",
            "publication revision names a different persistent tree",
        ));
    }
    Ok(())
}

fn verify_root_nodes(
    client: &mut postgres::Transaction<'_>,
    manifest: &TreeManifestRecord,
) -> Result<(), SemanticError> {
    for root in &manifest.roots {
        let actual_bytes: Option<i32> = client
            .query_opt(
                "SELECT octet_length(payload) FROM atomic_tree_nodes \
                 WHERE node_hash = $1",
                &[&&root.root_hash[..]],
            )
            .map_err(|error| postgres_error("tree/root-read", error))?
            .map(|row| row.get(0));
        if actual_bytes.and_then(|bytes| u64::try_from(bytes).ok()) != Some(root.encoded_bytes) {
            return Err(fault(
                "tree/missing-or-mismatched-root",
                "manifest root is absent or its encoded size disagrees",
            ));
        }
    }
    Ok(())
}

fn load_root_rows(
    client: &mut Client,
    manifest_hash: Digest,
) -> Result<Vec<TreeRootBinding>, SemanticError> {
    let rows = client
        .query(
            "SELECT r.index_order, r.history, r.root_hash, r.datom_count, \
                    r.encoded_bytes, octet_length(n.payload) \
               FROM atomic_tree_manifest_roots r \
               LEFT JOIN atomic_tree_nodes n ON n.node_hash = r.root_hash \
              WHERE r.manifest_hash = $1 \
              ORDER BY r.history, r.index_order",
            &[&&manifest_hash[..]],
        )
        .map_err(|error| postgres_error("tree/roots-read", error))?;
    let mut roots = Vec::with_capacity(rows.len());
    for row in rows {
        let encoded_bytes = pg_u64(row.get(4), "tree root encoded bytes")?;
        let actual_bytes: Option<i32> = row.get(5);
        if actual_bytes.and_then(|value| u64::try_from(value).ok()) != Some(encoded_bytes) {
            return Err(fault(
                "tree/missing-or-mismatched-root",
                "published manifest root is absent or has unexpected bytes",
            ));
        }
        roots.push(TreeRootBinding {
            order: decode_index_order(row.get(0))?,
            history: row.get(1),
            root_hash: digest(row.get(2), "tree root hash")?,
            datom_count: pg_u64(row.get(3), "tree root datom count")?,
            encoded_bytes,
        });
    }
    Ok(roots)
}

fn validate_manifest(manifest: &TreeManifestRecord) -> Result<(), SemanticError> {
    if manifest.database_id.is_empty()
        || manifest.publication_revision == 0
        || manifest.eidx_frontier == 0
    {
        return Err(SemanticError::incorrect(
            "tree/invalid-manifest-metadata",
            "tree manifest needs a database, positive publication revision and entity frontier",
        ));
    }
    validate_content_hash(
        manifest.manifest_hash,
        &manifest.payload,
        "tree/manifest-hash-mismatch",
    )?;
    if manifest.roots.len() != ROOT_BINDING_COUNT {
        return Err(SemanticError::incorrect(
            "tree/incomplete-root-set",
            "tree manifest must name current and history roots for all four orders",
        ));
    }
    let mut seen = [[false; 4]; 2];
    for root in &manifest.roots {
        if root.encoded_bytes == 0 {
            return Err(SemanticError::incorrect(
                "tree/invalid-root-metadata",
                "tree roots must have positive encoded size",
            ));
        }
        let history = usize::from(root.history);
        let order = index_order_i16(root.order) as usize;
        if std::mem::replace(&mut seen[history][order], true) {
            return Err(SemanticError::incorrect(
                "tree/duplicate-root",
                "tree manifest contains a duplicate index root coordinate",
            ));
        }
    }
    if seen.iter().flatten().any(|present| !present) {
        return Err(SemanticError::incorrect(
            "tree/incomplete-root-set",
            "tree manifest root coordinates are incomplete",
        ));
    }
    Ok(())
}

fn validate_content_hash(
    expected_hash: Digest,
    payload: &[u8],
    code: &'static str,
) -> Result<(), SemanticError> {
    if payload.is_empty() || sha256(payload) != expected_hash {
        return Err(fault(
            code,
            "content bytes do not match their expected hash",
        ));
    }
    Ok(())
}

fn index_order_i16(order: IndexOrder) -> i16 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}

fn decode_index_order(order: i16) -> Result<IndexOrder, SemanticError> {
    match order {
        0 => Ok(IndexOrder::Eavt),
        1 => Ok(IndexOrder::Aevt),
        2 => Ok(IndexOrder::Avet),
        3 => Ok(IndexOrder::Vaet),
        _ => Err(fault(
            "tree/invalid-index-order",
            "persistent root has an invalid index order",
        )),
    }
}

fn sql_u64(value: u64, label: &str) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "tree/value-out-of-range",
            format!("{label} cannot be represented by PostgreSQL BIGINT"),
        )
    })
}

fn pg_u64(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value)
        .map_err(|_| fault("tree/negative-metadata", format!("{label} is negative")))
}

fn digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|bytes: Vec<u8>| {
        fault(
            "tree/invalid-digest-length",
            format!("{label} has {} bytes instead of 32", bytes.len()),
        )
    })
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn build_intent_set_commitment_is_ordered_and_cardinality_bound() {
        let one = sha256(b"one");
        let two = sha256(b"two");
        let mut set = BTreeSet::new();
        set.insert(two);
        set.insert(one);
        let first = build_intent_node_set_hash(&set);
        assert_eq!(first, build_intent_node_set_hash(&set));
        set.remove(&one);
        assert_ne!(first, build_intent_node_set_hash(&set));
    }

    fn roots(hash: Digest, bytes: u64) -> Vec<TreeRootBinding> {
        let mut roots = Vec::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                roots.push(TreeRootBinding {
                    order,
                    history,
                    root_hash: hash,
                    datom_count: 0,
                    encoded_bytes: bytes,
                });
            }
        }
        roots
    }

    fn manifest() -> TreeManifestRecord {
        let payload = b"canonical tree manifest".to_vec();
        let node = b"canonical empty root";
        TreeManifestRecord {
            database_id: "database".to_owned(),
            publication_revision: 1,
            basis_t: 1,
            tx_hash: [1; 32],
            state_hash: [2; 32],
            excision_generation: 0,
            eidx_frontier: 1,
            manifest_hash: sha256(&payload),
            payload,
            roots: roots(sha256(node), node.len() as u64),
        }
    }

    #[test]
    fn manifest_requires_exactly_one_of_each_root_coordinate() {
        let mut candidate = manifest();
        validate_manifest(&candidate).unwrap();
        candidate.roots[7] = candidate.roots[0].clone();
        let error = validate_manifest(&candidate).unwrap_err();
        assert_eq!(error.code, "tree/duplicate-root");
    }

    #[test]
    fn content_identity_is_checked_before_storage() {
        let mut candidate = manifest();
        candidate.payload.push(0);
        let error = validate_manifest(&candidate).unwrap_err();
        assert_eq!(error.code, "tree/manifest-hash-mismatch");
    }

    #[test]
    fn publication_revision_is_required_metadata() {
        let mut candidate = manifest();
        candidate.publication_revision = 0;
        let error = validate_manifest(&candidate).unwrap_err();
        assert_eq!(error.code, "tree/invalid-manifest-metadata");
    }
}
