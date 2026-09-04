//! PostgreSQL-resident semantic-state v2 commitment.
//!
//! This is deliberately not another commitment scheme. Nodes use the exact
//! key, deterministic priority, empty root, leaf, node and database-coordinate
//! digests from `state_commitment`. PostgreSQL owns immutable content nodes;
//! one update retains only the paths it reads/rebuilds. The eager in-memory
//! commitment remains the semantic oracle while the writer is converted.

use crate::state_commitment::{
    CommitmentWork, SEMANTIC_STATE_VERSION, SemanticDatomKey, SemanticRootMetadata,
    semantic_datom_key, semantic_empty_hash, semantic_key_priority, semantic_node_hash,
    semantic_state_digest,
};
use crate::{Database, Datom, Digest, ErrorCategory, IndexOrder, SemanticError, View};
use postgres::GenericClient;
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};

const PAYLOAD_MAGIC: &[u8; 4] = b"ATSC";
const PAYLOAD_VERSION: u8 = 1;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct PersistentSemanticRoot {
    root: Option<Digest>,
    count: u64,
}

impl PersistentSemanticRoot {
    pub(crate) fn empty() -> Self {
        Self {
            root: None,
            count: 0,
        }
    }

    pub(crate) fn root_hash(self) -> Digest {
        self.root.unwrap_or_else(semantic_empty_hash)
    }

    pub(crate) fn count(self) -> u64 {
        self.count
    }

    pub(crate) fn metadata(self) -> SemanticRootMetadata {
        SemanticRootMetadata {
            version: SEMANTIC_STATE_VERSION,
            current_root: self.root_hash(),
            current_count: self.count,
        }
    }

    pub(crate) fn state_hash(self, basis_t: u64, eidx_frontier: u64) -> Digest {
        semantic_state_digest(self.metadata(), basis_t, eidx_frontier)
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum SemanticSetChange {
    Insert(SemanticDatomKey),
    Remove(SemanticDatomKey),
}

impl SemanticSetChange {
    pub(crate) fn insert(datom: &Datom) -> Result<Self, SemanticError> {
        Ok(Self::Insert(semantic_datom_key(datom)?))
    }

    pub(crate) fn remove(datom: &Datom) -> Result<Self, SemanticError> {
        Ok(Self::Remove(semantic_datom_key(datom)?))
    }
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub(crate) struct PersistentCommitmentCoordinate {
    pub(crate) database_id: String,
    pub(crate) generation: u64,
    pub(crate) basis_t: u64,
    pub(crate) tx_hash: Digest,
    pub(crate) state_hash: Digest,
    pub(crate) eidx_frontier: u64,
    pub(crate) root: PersistentSemanticRoot,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
struct NodeRef {
    hash: Digest,
    count: u64,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct StoredNode {
    key: SemanticDatomKey,
    left: Option<Digest>,
    right: Option<Digest>,
    count: u64,
}

impl StoredNode {
    fn encode(&self) -> Vec<u8> {
        let flags = u8::from(self.left.is_some()) | (u8::from(self.right.is_some()) << 1);
        let mut bytes =
            Vec::with_capacity(4 + 1 + 4 + 32 + 8 + 1 + 32 * usize::from(flags.count_ones() as u8));
        bytes.extend_from_slice(PAYLOAD_MAGIC);
        bytes.push(PAYLOAD_VERSION);
        bytes.extend_from_slice(&self.key.attribute.to_be_bytes());
        bytes.extend_from_slice(&self.key.digest);
        bytes.extend_from_slice(&self.count.to_be_bytes());
        bytes.push(flags);
        if let Some(left) = self.left {
            bytes.extend_from_slice(&left);
        }
        if let Some(right) = self.right {
            bytes.extend_from_slice(&right);
        }
        bytes
    }

    fn decode(bytes: &[u8]) -> Result<Self, SemanticError> {
        const FIXED: usize = 4 + 1 + 4 + 32 + 8 + 1;
        if bytes.len() < FIXED || &bytes[..4] != PAYLOAD_MAGIC || bytes[4] != PAYLOAD_VERSION {
            return Err(corrupt(
                "persistent-commitment/node-codec",
                "semantic commitment node has an unknown or truncated payload",
            ));
        }
        let attribute = u32::from_be_bytes(bytes[5..9].try_into().expect("fixed slice"));
        let digest = bytes[9..41].try_into().expect("fixed digest slice");
        let count = u64::from_be_bytes(bytes[41..49].try_into().expect("fixed slice"));
        let flags = bytes[49];
        if count == 0 || flags & !0b11 != 0 {
            return Err(corrupt(
                "persistent-commitment/node-codec",
                "semantic commitment node has non-canonical flags or count",
            ));
        }
        let expected = FIXED + usize::from(flags & 1 != 0) * 32 + usize::from(flags & 2 != 0) * 32;
        if bytes.len() != expected {
            return Err(corrupt(
                "persistent-commitment/node-codec",
                "semantic commitment node payload has trailing or missing bytes",
            ));
        }
        let mut offset = FIXED;
        let left = if flags & 1 != 0 {
            let value = bytes[offset..offset + 32]
                .try_into()
                .expect("validated left digest");
            offset += 32;
            Some(value)
        } else {
            None
        };
        let right = if flags & 2 != 0 {
            Some(
                bytes[offset..offset + 32]
                    .try_into()
                    .expect("validated right digest"),
            )
        } else {
            None
        };
        Ok(Self {
            key: SemanticDatomKey { attribute, digest },
            left,
            right,
            count,
        })
    }

    fn hash(&self) -> Digest {
        semantic_node_hash(
            self.key,
            self.count,
            self.left.unwrap_or_else(semantic_empty_hash),
            self.right.unwrap_or_else(semantic_empty_hash),
        )
    }
}

struct NodeStore<'a, C: GenericClient> {
    client: &'a mut C,
    cache: BTreeMap<Digest, StoredNode>,
    /// Nodes constructed by this update but not yet known durable. Multiple
    /// logical changes can replace an earlier path again before a coordinate
    /// is published; keeping those versions local lets the final flush omit
    /// content that was never reachable from the transaction's final root.
    pending: BTreeMap<Digest, StoredNode>,
    work: CommitmentWork,
}

impl<'a, C: GenericClient> NodeStore<'a, C> {
    fn new(client: &'a mut C) -> Self {
        Self {
            client,
            cache: BTreeMap::new(),
            pending: BTreeMap::new(),
            work: CommitmentWork::default(),
        }
    }

    fn load(&mut self, hash: Digest) -> Result<StoredNode, SemanticError> {
        if let Some(node) = self.cache.get(&hash) {
            return Ok(node.clone());
        }
        let row = self
            .client
            .query_opt(
                "SELECT payload, left_hash, right_hash, subtree_count \
                   FROM atomic_semantic_commitment_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| pg_error("persistent-commitment/node-read", error))?
            .ok_or_else(|| {
                corrupt(
                    "persistent-commitment/missing-node",
                    "semantic commitment names a missing content node",
                )
            })?;
        let payload: Vec<u8> = row.get(0);
        let node = StoredNode::decode(&payload)?;
        let left = optional_digest(row.get(1), "semantic commitment left child")?;
        let right = optional_digest(row.get(2), "semantic commitment right child")?;
        let count = from_sql_u64(row.get(3), "semantic commitment subtree count")?;
        if node.left != left || node.right != right || node.count != count || node.hash() != hash {
            return Err(corrupt(
                "persistent-commitment/node-authentication",
                "semantic commitment node payload, columns, or content hash disagree",
            ));
        }
        self.cache.insert(hash, node.clone());
        Ok(node)
    }

    /// Load the immediate children needed to prove this node's count and the
    /// local BST/heap invariants. This intentionally does not descend into an
    /// untouched sibling subtree; its authenticated root remains sufficient.
    fn links(
        &mut self,
        node: &StoredNode,
    ) -> Result<(Option<NodeRef>, Option<NodeRef>), SemanticError> {
        let left = node
            .left
            .map(|hash| self.load(hash).map(|child| (hash, child)))
            .transpose()?;
        let right = node
            .right
            .map(|hash| self.load(hash).map(|child| (hash, child)))
            .transpose()?;
        if left
            .as_ref()
            .is_some_and(|(_, child)| child.key >= node.key || heap_precedes(child.key, node.key))
            || right.as_ref().is_some_and(|(_, child)| {
                child.key <= node.key || heap_precedes(child.key, node.key)
            })
        {
            return Err(corrupt(
                "persistent-commitment/tree-invariant",
                "semantic commitment violates its canonical treap ordering",
            ));
        }
        let left_ref = left.map(|(hash, child)| NodeRef {
            hash,
            count: child.count,
        });
        let right_ref = right.map(|(hash, child)| NodeRef {
            hash,
            count: child.count,
        });
        let count = link_count(left_ref)
            .checked_add(1)
            .and_then(|count| count.checked_add(link_count(right_ref)))
            .ok_or_else(count_overflow)?;
        if count != node.count {
            return Err(corrupt(
                "persistent-commitment/tree-count",
                "semantic commitment subtree count disagrees with its children",
            ));
        }
        Ok((left_ref, right_ref))
    }

    fn store(
        &mut self,
        key: SemanticDatomKey,
        left: Option<NodeRef>,
        right: Option<NodeRef>,
    ) -> Result<NodeRef, SemanticError> {
        let count = link_count(left)
            .checked_add(1)
            .and_then(|count| count.checked_add(link_count(right)))
            .ok_or_else(count_overflow)?;
        let node = StoredNode {
            key,
            left: left.map(|node| node.hash),
            right: right.map(|node| node.hash),
            count,
        };
        let hash = node.hash();
        self.work.rehash();
        if let Some(stored) = self.cache.get(&hash) {
            if stored != &node {
                return Err(corrupt(
                    "persistent-commitment/node-collision",
                    "semantic commitment node hash resolves to different canonical content",
                ));
            }
            // A cached node is either already authenticated durable content or
            // is already pending. In either case, do not turn it into a new
            // write merely because another transient path rebuilt it.
            return Ok(NodeRef { hash, count });
        }
        self.cache.insert(hash, node.clone());
        self.pending.insert(hash, node);
        Ok(NodeRef { hash, count })
    }

    /// Persist only newly constructed nodes reachable from `root`. Children
    /// are flushed before parents because PostgreSQL's immutable node links
    /// are foreign keys. Existing children are already authenticated by the
    /// read path; an existing hash encountered during insert is authenticated
    /// again rather than treated as permission to accept a collision.
    fn flush_reachable(&mut self, root: Option<Digest>) -> Result<(), SemanticError> {
        let mut visiting = BTreeSet::new();
        let mut flushed = BTreeSet::new();
        if let Some(root) = root {
            self.flush_pending(root, &mut visiting, &mut flushed)?;
        }
        Ok(())
    }

    fn flush_pending(
        &mut self,
        hash: Digest,
        visiting: &mut BTreeSet<Digest>,
        flushed: &mut BTreeSet<Digest>,
    ) -> Result<(), SemanticError> {
        if flushed.contains(&hash) {
            return Ok(());
        }
        let Some(node) = self.pending.get(&hash).cloned() else {
            return Ok(());
        };
        if !visiting.insert(hash) {
            return Err(corrupt(
                "persistent-commitment/pending-cycle",
                "new semantic commitment nodes contain a cycle",
            ));
        }
        if let Some(left) = node.left {
            self.flush_pending(left, visiting, flushed)?;
        }
        if let Some(right) = node.right {
            self.flush_pending(right, visiting, flushed)?;
        }
        self.persist_node(hash, &node)?;
        visiting.remove(&hash);
        flushed.insert(hash);
        Ok(())
    }

    fn persist_node(&mut self, hash: Digest, node: &StoredNode) -> Result<(), SemanticError> {
        let payload = node.encode();
        let left_bytes = node.left.map(|value| value.to_vec());
        let right_bytes = node.right.map(|value| value.to_vec());
        let count_sql = to_sql_u64(node.count, "semantic commitment subtree count")?;
        self.client
            .execute(
                "INSERT INTO atomic_semantic_commitment_nodes \
                     (node_hash, payload, left_hash, right_hash, subtree_count) \
                 VALUES ($1, $2, $3, $4, $5) ON CONFLICT (node_hash) DO NOTHING",
                &[&&hash[..], &payload, &left_bytes, &right_bytes, &count_sql],
            )
            .map_err(|error| pg_error("persistent-commitment/node-insert", error))?;
        let stored = self
            .client
            .query_one(
                "SELECT payload, left_hash, right_hash, subtree_count \
                   FROM atomic_semantic_commitment_nodes WHERE node_hash = $1",
                &[&&hash[..]],
            )
            .map_err(|error| pg_error("persistent-commitment/node-verify", error))?;
        if stored.get::<_, Vec<u8>>(0) != payload
            || optional_digest(stored.get(1), "semantic commitment left child")? != node.left
            || optional_digest(stored.get(2), "semantic commitment right child")? != node.right
            || from_sql_u64(stored.get(3), "semantic commitment subtree count")? != node.count
        {
            return Err(corrupt(
                "persistent-commitment/node-collision",
                "semantic commitment node hash resolves to different canonical content",
            ));
        }
        Ok(())
    }

    fn insert(
        &mut self,
        root: Option<NodeRef>,
        key: SemanticDatomKey,
    ) -> Result<(Option<NodeRef>, bool), SemanticError> {
        self.work.visit();
        let Some(root) = root else {
            return Ok((Some(self.store(key, None, None)?), true));
        };
        let node = self.load(root.hash)?;
        if node.count != root.count {
            return Err(corrupt(
                "persistent-commitment/root-count",
                "semantic commitment link count disagrees with its node",
            ));
        }
        let (left, right) = self.links(&node)?;
        match key.cmp(&node.key) {
            Ordering::Equal => Ok((Some(root), false)),
            Ordering::Less => {
                let (left, changed) = self.insert(left, key)?;
                if !changed {
                    return Ok((Some(root), false));
                }
                let left = left.expect("inserted left child");
                let left_node = self.load(left.hash)?;
                if heap_precedes(left_node.key, node.key) {
                    let (pivot_left, pivot_right) = self.links(&left_node)?;
                    let right = self.store(node.key, pivot_right, right)?;
                    Ok((
                        Some(self.store(left_node.key, pivot_left, Some(right))?),
                        true,
                    ))
                } else {
                    Ok((Some(self.store(node.key, Some(left), right)?), true))
                }
            }
            Ordering::Greater => {
                let (right, changed) = self.insert(right, key)?;
                if !changed {
                    return Ok((Some(root), false));
                }
                let right = right.expect("inserted right child");
                let right_node = self.load(right.hash)?;
                if heap_precedes(right_node.key, node.key) {
                    let (pivot_left, pivot_right) = self.links(&right_node)?;
                    let left = self.store(node.key, left, pivot_left)?;
                    Ok((
                        Some(self.store(right_node.key, Some(left), pivot_right)?),
                        true,
                    ))
                } else {
                    Ok((Some(self.store(node.key, left, Some(right))?), true))
                }
            }
        }
    }

    fn remove(
        &mut self,
        root: Option<NodeRef>,
        key: SemanticDatomKey,
    ) -> Result<(Option<NodeRef>, bool), SemanticError> {
        self.work.visit();
        let Some(root) = root else {
            return Ok((None, false));
        };
        let node = self.load(root.hash)?;
        if node.count != root.count {
            return Err(corrupt(
                "persistent-commitment/root-count",
                "semantic commitment link count disagrees with its node",
            ));
        }
        let (left, right) = self.links(&node)?;
        match key.cmp(&node.key) {
            Ordering::Equal => Ok((self.merge(left, right)?, true)),
            Ordering::Less => {
                let (left, changed) = self.remove(left, key)?;
                if !changed {
                    return Ok((Some(root), false));
                }
                Ok((Some(self.store(node.key, left, right)?), true))
            }
            Ordering::Greater => {
                let (right, changed) = self.remove(right, key)?;
                if !changed {
                    return Ok((Some(root), false));
                }
                Ok((Some(self.store(node.key, left, right)?), true))
            }
        }
    }

    fn merge(
        &mut self,
        left: Option<NodeRef>,
        right: Option<NodeRef>,
    ) -> Result<Option<NodeRef>, SemanticError> {
        match (left, right) {
            (None, right) => Ok(right),
            (left, None) => Ok(left),
            (Some(left), Some(right)) => {
                let left_node = self.load(left.hash)?;
                let right_node = self.load(right.hash)?;
                if heap_precedes(left_node.key, right_node.key) {
                    let (left_left, left_right) = self.links(&left_node)?;
                    let merged = self.merge(left_right, Some(right))?;
                    Ok(Some(self.store(left_node.key, left_left, merged)?))
                } else {
                    let (right_left, right_right) = self.links(&right_node)?;
                    let merged = self.merge(Some(left), right_left)?;
                    Ok(Some(self.store(right_node.key, merged, right_right)?))
                }
            }
        }
    }

    fn verify_root(&mut self, root: PersistentSemanticRoot) -> Result<(), SemanticError> {
        match (root.root, root.count) {
            (None, 0) => Ok(()),
            (Some(hash), count) if count > 0 => {
                let node = self.load(hash)?;
                if node.count != count {
                    return Err(corrupt(
                        "persistent-commitment/root-count",
                        "semantic commitment coordinate disagrees with its root node",
                    ));
                }
                self.links(&node)?;
                Ok(())
            }
            _ => Err(corrupt(
                "persistent-commitment/root-shape",
                "empty and non-empty semantic commitment coordinates are inconsistent",
            )),
        }
    }
}

/// Apply exact logical-set changes against a persisted root. The supplied
/// PostgreSQL client is normally an already-open writer transaction, so nodes
/// and the eventual root coordinate share its commit/rollback fate.
pub(crate) fn advance_persistent_commitment<C: GenericClient>(
    client: &mut C,
    before: PersistentSemanticRoot,
    changes: &[SemanticSetChange],
) -> Result<(PersistentSemanticRoot, CommitmentWork), SemanticError> {
    let mut store = NodeStore::new(client);
    store.verify_root(before)?;
    let mut root = before.root.map(|hash| NodeRef {
        hash,
        count: before.count,
    });
    for change in changes {
        let (next, changed) = match *change {
            SemanticSetChange::Insert(key) => store.insert(root, key)?,
            SemanticSetChange::Remove(key) => store.remove(root, key)?,
        };
        root = next;
        if changed {
            store.work.change_leaf();
        }
    }
    let root = PersistentSemanticRoot {
        root: root.map(|node| node.hash),
        count: root.map_or(0, |node| node.count),
    };
    store.flush_reachable(root.root)?;
    Ok((root, store.work))
}

/// Administrative/offline seed from one already-authenticated eager value.
/// This is intentionally O(current datoms) and is not a transaction-time path.
pub(crate) fn persist_eager_snapshot<C: GenericClient>(
    client: &mut C,
    database: &Database,
) -> Result<PersistentSemanticRoot, SemanticError> {
    let mut changes = Vec::new();
    for datom in database.datoms(View::Current, IndexOrder::Eavt) {
        changes.push(SemanticSetChange::insert(&datom)?);
    }
    advance_persistent_commitment(client, PersistentSemanticRoot::empty(), &changes)
        .map(|(root, _)| root)
}

/// Translate an eager transaction report into the exact set changes used by
/// the current v2 oracle. This adapter exists for differential conversion and
/// migration; the final tiered assessor can emit these changes directly.
pub(crate) fn eager_semantic_changes(
    before: &Database,
    tx_data: &[Datom],
) -> Result<Vec<SemanticSetChange>, SemanticError> {
    let mut changes = Vec::new();
    for datom in tx_data {
        let prior = before
            .datoms_with_prefix(&crate::IndexPrefix::Eavt {
                entity: datom.entity,
                attribute: Some(datom.attribute),
                value: Some(datom.value.clone()),
            })?
            .into_iter()
            .find(|candidate| candidate.value.stored_eq(&datom.value));
        if datom.added {
            if prior.is_none() {
                changes.push(SemanticSetChange::insert(datom)?);
            }
        } else if let Some(prior) = prior {
            changes.push(SemanticSetChange::remove(&prior)?);
        }
    }
    Ok(changes)
}

/// Recover exact current assertion identities through bounded EAV point
/// reads. Retraction datoms carry the new transaction id and `added=false`, so
/// hashing the retraction itself would remove the wrong key: the commitment
/// contains the prior assertion's transaction id and assertion bit. This is
/// the production-shaped adapter for a tiered db-before.
pub(crate) fn exact_semantic_changes(
    before: &crate::DatabaseValue,
    tx_data: &[Datom],
) -> Result<Vec<SemanticSetChange>, SemanticError> {
    let mut changes = Vec::new();
    for datom in tx_data {
        let prefix = crate::IndexPrefix::Eavt {
            entity: datom.entity,
            attribute: Some(datom.attribute),
            value: Some(datom.value.clone()),
        };
        let mut prior = None;
        for candidate in before.current_prefix_cursor(&prefix)? {
            let candidate = candidate?;
            if candidate.value.stored_eq(&datom.value) {
                prior = Some(candidate);
                break;
            }
        }
        if datom.added {
            if prior.is_none() {
                changes.push(SemanticSetChange::insert(datom)?);
            }
        } else if let Some(prior) = prior {
            changes.push(SemanticSetChange::remove(&prior)?);
        }
    }
    Ok(changes)
}

pub(crate) fn record_persistent_coordinate<C: GenericClient>(
    client: &mut C,
    coordinate: &PersistentCommitmentCoordinate,
) -> Result<(), SemanticError> {
    let mut store = NodeStore::new(client);
    store.verify_root(coordinate.root)?;
    if coordinate
        .root
        .state_hash(coordinate.basis_t, coordinate.eidx_frontier)
        != coordinate.state_hash
    {
        return Err(corrupt(
            "persistent-commitment/state-hash",
            "semantic commitment root does not reproduce the authoritative state digest",
        ));
    }
    let generation = to_sql_u64(coordinate.generation, "semantic commitment generation")?;
    let basis = to_sql_u64(coordinate.basis_t, "semantic commitment basis")?;
    let frontier = to_sql_u64(
        coordinate.eidx_frontier,
        "semantic commitment entity frontier",
    )?;
    let count = to_sql_u64(coordinate.root.count, "semantic commitment member count")?;
    let root = coordinate.root.root.map(|hash| hash.to_vec());
    client
        .execute(
            "INSERT INTO atomic_semantic_commitment_roots \
                 (database_id, generation, basis_t, tx_hash, state_hash, eidx_frontier, \
                  commitment_version, current_root, current_count) \
             VALUES ($1, $2, $3, $4, $5, $6, 2, $7, $8) \
             ON CONFLICT (database_id, generation, basis_t) DO NOTHING",
            &[
                &coordinate.database_id,
                &generation,
                &basis,
                &&coordinate.tx_hash[..],
                &&coordinate.state_hash[..],
                &frontier,
                &root,
                &count,
            ],
        )
        .map_err(|error| pg_error("persistent-commitment/root-insert", error))?;
    let stored = load_persistent_coordinate(
        client,
        &coordinate.database_id,
        coordinate.generation,
        coordinate.basis_t,
    )?
    .ok_or_else(|| {
        corrupt(
            "persistent-commitment/root-missing",
            "inserted semantic commitment coordinate is missing",
        )
    })?;
    if &stored != coordinate {
        return Err(corrupt(
            "persistent-commitment/root-collision",
            "semantic commitment coordinate already names different content",
        ));
    }
    Ok(())
}

/// Seed one endpoint already materialized by a broad administrative workflow.
/// Creation, excision, and restore inherently reconstruct the complete target
/// value; paying the one-time O(current information) conversion here keeps
/// ordinary transaction publication on the incremental touched-path API.
pub(crate) fn record_eager_endpoint<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    tx_hash: Digest,
    expected_state_hash: Digest,
    database: &Database,
) -> Result<PersistentSemanticRoot, SemanticError> {
    let root = persist_eager_snapshot(client, database)?;
    let state_hash = root.state_hash(database.basis_t(), database.eidx_frontier());
    if state_hash != expected_state_hash {
        return Err(corrupt(
            "persistent-commitment/endpoint-state",
            "administrative endpoint does not reproduce its authoritative state digest",
        ));
    }
    record_persistent_coordinate(
        client,
        &PersistentCommitmentCoordinate {
            database_id: database_id.to_owned(),
            generation,
            basis_t: database.basis_t(),
            tx_hash,
            state_hash,
            eidx_frontier: database.eidx_frontier(),
            root,
        },
    )?;
    Ok(root)
}

pub(crate) fn load_persistent_coordinate<C: GenericClient>(
    client: &mut C,
    database_id: &str,
    generation: u64,
    basis_t: u64,
) -> Result<Option<PersistentCommitmentCoordinate>, SemanticError> {
    let generation_sql = to_sql_u64(generation, "semantic commitment generation")?;
    let basis_sql = to_sql_u64(basis_t, "semantic commitment basis")?;
    let Some(row) = client
        .query_opt(
            "SELECT tx_hash, state_hash, eidx_frontier, commitment_version, \
                    current_root, current_count \
               FROM atomic_semantic_commitment_roots \
              WHERE database_id = $1 AND generation = $2 AND basis_t = $3",
            &[&database_id, &generation_sql, &basis_sql],
        )
        .map_err(|error| pg_error("persistent-commitment/root-read", error))?
    else {
        return Ok(None);
    };
    let version: i16 = row.get(3);
    if version != i16::try_from(SEMANTIC_STATE_VERSION).expect("v2 fits i16") {
        return Err(corrupt(
            "persistent-commitment/root-version",
            "semantic commitment coordinate has an unsupported version",
        ));
    }
    let root = PersistentSemanticRoot {
        root: optional_digest(row.get(4), "semantic commitment root")?,
        count: from_sql_u64(row.get(5), "semantic commitment member count")?,
    };
    let coordinate = PersistentCommitmentCoordinate {
        database_id: database_id.to_owned(),
        generation,
        basis_t,
        tx_hash: required_digest(row.get(0), "semantic commitment transaction hash")?,
        state_hash: required_digest(row.get(1), "semantic commitment state hash")?,
        eidx_frontier: from_sql_u64(row.get(2), "semantic commitment entity frontier")?,
        root,
    };
    let mut store = NodeStore::new(client);
    store.verify_root(root)?;
    if root.state_hash(basis_t, coordinate.eidx_frontier) != coordinate.state_hash {
        return Err(corrupt(
            "persistent-commitment/state-hash",
            "stored semantic root does not reproduce its state digest",
        ));
    }
    Ok(Some(coordinate))
}

/// Offline migration/repair for every generation that can still receive
/// writes: the published generation and any explicitly staged COW build.
/// Retired generations are deliberately excluded because their log rows may
/// already have been collected. One eager replay per terminal coordinate is
/// the finite conversion cost; subsequent updates use touched paths only.
pub(crate) fn backfill_terminal_persistent_commitments<C: GenericClient>(
    client: &mut C,
) -> Result<(), SemanticError> {
    let databases = client
        .query(
            "SELECT d.database_id, d.genesis_hash, h.log_generation \
               FROM atomic_databases d JOIN atomic_heads h USING (database_id) \
              ORDER BY d.database_id",
            &[],
        )
        .map_err(|error| pg_error("persistent-commitment/backfill-databases", error))?;
    for row in databases {
        let database_id: String = row.get(0);
        let genesis_hash = required_digest(row.get(1), "semantic commitment genesis hash")?;
        let active_generation = from_sql_u64(row.get(2), "semantic commitment generation")?;
        let mut generations = client
            .query(
                "SELECT generation FROM atomic_log_generation_builds \
                  WHERE database_id = $1 ORDER BY generation",
                &[&database_id],
            )
            .map_err(|error| pg_error("persistent-commitment/backfill-builds", error))?
            .into_iter()
            .map(|row| from_sql_u64(row.get(0), "semantic commitment build generation"))
            .collect::<Result<Vec<_>, _>>()?;
        generations.push(active_generation);
        generations.sort_unstable();
        generations.dedup();

        for generation in generations {
            let terminal = if generation == 0 {
                client
                    .query_opt(
                        "SELECT basis_t, tx_hash, state_hash, NULL::bigint \
                           FROM atomic_transactions WHERE database_id = $1 \
                          ORDER BY basis_t DESC LIMIT 1",
                        &[&database_id],
                    )
                    .map_err(|error| pg_error("persistent-commitment/backfill-legacy", error))?
            } else {
                client
                    .query_opt(
                        "SELECT basis_t, tx_hash, state_hash, eidx_frontier \
                           FROM atomic_generation_transactions \
                          WHERE database_id = $1 AND generation = $2 \
                          ORDER BY basis_t DESC LIMIT 1",
                        &[
                            &database_id,
                            &to_sql_u64(generation, "semantic commitment generation")?,
                        ],
                    )
                    .map_err(|error| pg_error("persistent-commitment/backfill-generation", error))?
            };
            let (basis_t, tx_hash, authoritative_state, authoritative_frontier) = match terminal {
                Some(row) => (
                    from_sql_u64(row.get(0), "semantic commitment terminal basis")?,
                    required_digest(row.get(1), "semantic commitment transaction hash")?,
                    Some(required_digest(
                        row.get(2),
                        "semantic commitment state hash",
                    )?),
                    row.get::<_, Option<i64>>(3)
                        .map(|value| from_sql_u64(value, "semantic commitment entity frontier"))
                        .transpose()?,
                ),
                None => (0, genesis_hash, None, None),
            };
            if load_persistent_coordinate(client, &database_id, generation, basis_t)?.is_some() {
                continue;
            }
            let recovered = crate::postgres::recover_generation_to(
                client,
                &database_id,
                generation,
                basis_t,
                tx_hash,
            )?;
            let eidx_frontier = recovered.database.eidx_frontier();
            if authoritative_frontier.is_some_and(|frontier| frontier != eidx_frontier) {
                return Err(corrupt(
                    "persistent-commitment/backfill-frontier",
                    "recovered database frontier disagrees with its generation membership",
                ));
            }
            let root = persist_eager_snapshot(client, &recovered.database)?;
            let state_hash = root.state_hash(basis_t, eidx_frontier);
            if authoritative_state.is_some_and(|expected| expected != state_hash) {
                return Err(corrupt(
                    "persistent-commitment/backfill-state",
                    "recovered semantic root disagrees with its authoritative state digest",
                ));
            }
            record_persistent_coordinate(
                client,
                &PersistentCommitmentCoordinate {
                    database_id: database_id.clone(),
                    generation,
                    basis_t,
                    tx_hash,
                    state_hash,
                    eidx_frontier,
                    root,
                },
            )?;
        }
    }
    Ok(())
}

fn heap_precedes(left: SemanticDatomKey, right: SemanticDatomKey) -> bool {
    semantic_key_priority(left)
        .cmp(&semantic_key_priority(right))
        .then_with(|| left.cmp(&right))
        .is_lt()
}

fn link_count(link: Option<NodeRef>) -> u64 {
    link.map_or(0, |node| node.count)
}

fn count_overflow() -> SemanticError {
    SemanticError::new(
        ErrorCategory::Fault,
        "commitment/member-count-overflow",
        "semantic commitment cannot contain more than u64 datoms",
    )
}

fn required_digest(bytes: Vec<u8>, label: &str) -> Result<Digest, SemanticError> {
    bytes.try_into().map_err(|_| {
        corrupt(
            "persistent-commitment/digest-length",
            format!("{label} is not a 32-byte digest"),
        )
    })
}

fn optional_digest(bytes: Option<Vec<u8>>, label: &str) -> Result<Option<Digest>, SemanticError> {
    bytes.map(|bytes| required_digest(bytes, label)).transpose()
}

fn to_sql_u64(value: u64, label: &str) -> Result<i64, SemanticError> {
    i64::try_from(value).map_err(|_| {
        corrupt(
            "persistent-commitment/sql-range",
            format!("{label} exceeds PostgreSQL bigint"),
        )
    })
}

fn from_sql_u64(value: i64, label: &str) -> Result<u64, SemanticError> {
    u64::try_from(value).map_err(|_| {
        corrupt(
            "persistent-commitment/sql-range",
            format!("{label} is negative"),
        )
    })
}

fn corrupt(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

fn pg_error(code: &'static str, error: postgres::Error) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, error.to_string())
}

#[cfg(test)]
mod codec_tests {
    use super::*;
    use crate::postgres::PostgresStore;
    use crate::state_commitment::{checkpoint_root_metadata, checkpoint_state_hash};
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, TxValue, Unique, Value, ValueType,
    };
    use postgres::{Client, NoTls};
    use std::time::{SystemTime, UNIX_EPOCH};

    const ITEM_NAME: u32 = 1_000;
    const ITEM_COUNT: u32 = 1_001;

    #[test]
    fn node_codec_is_strict_and_round_trips() {
        let node = StoredNode {
            key: SemanticDatomKey {
                attribute: 42,
                digest: [7; 32],
            },
            left: Some([8; 32]),
            right: None,
            count: 9,
        };
        let encoded = node.encode();
        assert_eq!(StoredNode::decode(&encoded).unwrap(), node);

        let mut trailing = encoded.clone();
        trailing.push(0);
        assert_eq!(
            StoredNode::decode(&trailing).unwrap_err().code,
            "persistent-commitment/node-codec"
        );
    }

    fn connection() -> Option<String> {
        std::env::var("ATOMIC_POSTGRES_URL").ok()
    }

    fn unique(prefix: &str) -> String {
        format!(
            "{prefix}_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        )
    }

    fn client_in_schema(connection: &str, schema: &str) -> Client {
        let mut client = Client::connect(connection, NoTls).unwrap();
        client
            .batch_execute(&format!("SET search_path TO \"{schema}\""))
            .unwrap();
        client
    }

    fn isolated_schema(connection: &str, prefix: &str) -> String {
        let schema = unique(prefix);
        let mut client = Client::connect(connection, NoTls).unwrap();
        client
            .batch_execute(&format!("CREATE SCHEMA \"{schema}\""))
            .unwrap();
        schema
    }

    fn drop_schema(connection: &str, schema: &str) {
        let mut client = Client::connect(connection, NoTls).unwrap();
        client
            .batch_execute(&format!("DROP SCHEMA \"{schema}\" CASCADE"))
            .unwrap();
    }

    fn test_schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(
                Attribute::new(
                    ITEM_NAME,
                    Keyword::new("item", "name"),
                    ValueType::String,
                    Cardinality::One,
                )
                .unique(Unique::Identity),
            )
            .unwrap();
        schema
            .install(Attribute::new(
                ITEM_COUNT,
                Keyword::new("item", "count"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        schema
    }

    fn assert_matches_eager(root: PersistentSemanticRoot, database: &Database) {
        assert_eq!(root.metadata(), checkpoint_root_metadata(database));
        assert_eq!(
            root.state_hash(database.basis_t(), database.eidx_frontier()),
            checkpoint_state_hash(database).unwrap()
        );
    }

    #[test]
    fn eager_seed_writes_only_the_final_reachable_tree_child_first() {
        let Some(connection) = connection() else {
            return;
        };
        let schema_name = isolated_schema(&connection, "persistent_commitment_final_tree");
        let mut client = client_in_schema(&connection, &schema_name);
        crate::PostgresMigrator::from_client(client)
            .migrate()
            .unwrap();
        client = client_in_schema(&connection, &schema_name);

        let database = Database::new(test_schema()).unwrap();
        let root = {
            let mut transaction = client.transaction().unwrap();
            let root = persist_eager_snapshot(&mut transaction, &database).unwrap();
            assert_matches_eager(root, &database);
            assert!(root.count() > 1, "witness requires a multi-change seed");

            let root_hash = root.root.expect("schema database has a non-empty root");
            let counts = transaction
                .query_one(
                    "WITH RECURSIVE reachable(node_hash) AS ( \
                         VALUES ($1::bytea) \
                         UNION \
                         SELECT child.node_hash \
                           FROM reachable AS reachable_parent \
                           JOIN atomic_semantic_commitment_nodes AS parent \
                             ON parent.node_hash = reachable_parent.node_hash \
                          CROSS JOIN LATERAL \
                             (VALUES (parent.left_hash), (parent.right_hash)) AS child(node_hash) \
                          WHERE child.node_hash IS NOT NULL \
                     ) \
                     SELECT (SELECT count(*) FROM atomic_semantic_commitment_nodes), \
                            (SELECT count(*) FROM reachable)",
                    &[&&root_hash[..]],
                )
                .unwrap();
            let stored: i64 = counts.get(0);
            let reachable: i64 = counts.get(1);
            assert_eq!(stored, i64::try_from(root.count()).unwrap());
            assert_eq!(reachable, stored, "transient path nodes must remain local");

            // The child references are immediate foreign keys, so reaching
            // this commit on an initially empty node table also witnesses the
            // required child-before-parent insertion order.
            transaction.commit().unwrap();
            root
        };
        let durable: i64 = client
            .query_one("SELECT count(*) FROM atomic_semantic_commitment_nodes", &[])
            .unwrap()
            .get(0);
        assert_eq!(durable, i64::try_from(root.count()).unwrap());

        drop(client);
        drop_schema(&connection, &schema_name);
    }

    #[test]
    fn postgres_paths_match_eager_v2_and_reject_corruption() {
        let Some(connection) = connection() else {
            return;
        };
        let schema_name = isolated_schema(&connection, "persistent_commitment");
        let mut client = client_in_schema(&connection, &schema_name);
        crate::PostgresMigrator::from_client(client)
            .migrate()
            .unwrap();
        client = client_in_schema(&connection, &schema_name);

        let mut database = Database::new(test_schema()).unwrap();
        let mut root = persist_eager_snapshot(&mut client, &database).unwrap();
        assert_matches_eager(root, &database);

        let first = database
            .with(
                &[
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: ITEM_NAME,
                        value: TxValue::Scalar(Value::String("widget".into())),
                    },
                    TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: ITEM_COUNT,
                        value: TxValue::Scalar(Value::Long(1)),
                    },
                ],
                10,
            )
            .unwrap();
        let entity = first.tempids["item"];
        assert_eq!(
            exact_semantic_changes(&database.database_value().history(), &first.tx_data)
                .unwrap_err()
                .code,
            "database/prefix-cursor-requires-current"
        );
        let changes = eager_semantic_changes(&database, &first.tx_data).unwrap();
        assert_eq!(
            exact_semantic_changes(&database.database_value(), &first.tx_data).unwrap(),
            changes
        );
        (root, _) = advance_persistent_commitment(&mut client, root, &changes).unwrap();
        database = first.db_after;
        assert_matches_eager(root, &database);

        let replacement = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: ITEM_COUNT,
                    value: TxValue::Scalar(Value::Long(2)),
                }],
                11,
            )
            .unwrap();
        let changes = eager_semantic_changes(&database, &replacement.tx_data).unwrap();
        assert_eq!(
            exact_semantic_changes(&database.database_value(), &replacement.tx_data).unwrap(),
            changes
        );
        let members_before = root.count();
        let (next, work) = advance_persistent_commitment(&mut client, root, &changes).unwrap();
        assert_eq!(work.leaf_changes, changes.len() as u64);
        assert!(
            work.node_visits < members_before,
            "a three-datom update inspected {} of {members_before} members",
            work.node_visits
        );
        assert!(
            work.node_hashes < members_before,
            "a three-datom update rehashed {} of {members_before} members",
            work.node_hashes
        );
        root = next;
        database = replacement.db_after;
        assert_matches_eager(root, &database);

        let removal = database
            .with(&[TxOp::RetractEntity(EntityRef::Id(entity))], 12)
            .unwrap();
        let changes = eager_semantic_changes(&database, &removal.tx_data).unwrap();
        assert_eq!(
            exact_semantic_changes(&database.database_value(), &removal.tx_data).unwrap(),
            changes
        );
        (root, _) = advance_persistent_commitment(&mut client, root, &changes).unwrap();
        database = removal.db_after;
        assert_matches_eager(root, &database);

        let missing = PersistentSemanticRoot {
            root: Some([0x55; 32]),
            count: 1,
        };
        assert_eq!(
            advance_persistent_commitment(&mut client, missing, &[])
                .unwrap_err()
                .code,
            "persistent-commitment/missing-node"
        );

        let root_hash = root.root.expect("bootstrap datoms keep the root non-empty");
        client
            .batch_execute(
                "ALTER TABLE atomic_semantic_commitment_nodes \
                 DISABLE TRIGGER atomic_semantic_commitment_nodes_immutable",
            )
            .unwrap();
        client
            .execute(
                "UPDATE atomic_semantic_commitment_nodes \
                    SET payload = set_byte(payload, 0, 0) WHERE node_hash = $1",
                &[&&root_hash[..]],
            )
            .unwrap();
        let error = advance_persistent_commitment(&mut client, root, &[]).unwrap_err();
        assert!(
            matches!(
                error.code,
                "persistent-commitment/node-codec" | "persistent-commitment/node-authentication"
            ),
            "unexpected corruption error: {error:?}"
        );

        drop(client);
        drop_schema(&connection, &schema_name);
    }

    #[test]
    fn v15_upgrade_backfills_the_active_terminal_coordinate() {
        let Some(connection) = connection() else {
            return;
        };
        let schema_name = isolated_schema(&connection, "persistent_commitment_upgrade");
        let client = client_in_schema(&connection, &schema_name);
        crate::PostgresMigrator::from_client(client)
            .migrate()
            .unwrap();
        let database_id = unique("persistent_commitment_database");
        let client = client_in_schema(&connection, &schema_name);
        let mut store = PostgresStore::from_client(client);
        let expected = store.create_database(&database_id, test_schema()).unwrap();
        drop(store);

        // Recreate the only materially relevant interim-v14 state: an active
        // authenticated database with neither persistent semantic table nor
        // migration record. The log/catalog bytes remain untouched and the
        // v15 Rust hook must derive their terminal coordinate from replay.
        let mut client = client_in_schema(&connection, &schema_name);
        client
            .batch_execute(
                "DROP TRIGGER atomic_log_generation_activations_semantic_root \
                    ON atomic_log_generation_activations; \
                 DROP TABLE atomic_semantic_commitment_roots; \
                 DROP TABLE atomic_semantic_commitment_nodes; \
                 DELETE FROM atomic_schema_migrations WHERE version >= 15",
            )
            .unwrap();
        drop(client);
        let client = client_in_schema(&connection, &schema_name);
        crate::PostgresMigrator::from_client(client)
            .migrate()
            .unwrap();
        let mut client = client_in_schema(&connection, &schema_name);
        let head = client
            .query_one(
                "SELECT log_generation, basis_t, tx_hash FROM atomic_heads \
                  WHERE database_id = $1",
                &[&database_id],
            )
            .unwrap();
        let generation = u64::try_from(head.get::<_, i64>(0)).unwrap();
        let basis_t = u64::try_from(head.get::<_, i64>(1)).unwrap();
        let tx_hash = required_digest(head.get(2), "test head hash").unwrap();
        let coordinate = load_persistent_coordinate(&mut client, &database_id, generation, basis_t)
            .unwrap()
            .expect("v15 migration must backfill the active endpoint");
        assert_eq!(coordinate.tx_hash, tx_hash);
        assert_eq!(
            coordinate.root.metadata(),
            checkpoint_root_metadata(&expected)
        );
        assert_eq!(
            coordinate.state_hash,
            checkpoint_state_hash(&expected).unwrap()
        );

        drop(client);
        drop_schema(&connection, &schema_name);
    }

    #[test]
    fn database_creation_publishes_restartable_genesis_and_schema_coordinates() {
        let Some(connection) = connection() else {
            return;
        };
        let schema_name = isolated_schema(&connection, "persistent_commitment_create");
        let client = client_in_schema(&connection, &schema_name);
        crate::PostgresMigrator::from_client(client)
            .migrate()
            .unwrap();
        let empty_id = unique("persistent_empty_database");
        let schema_id = unique("persistent_schema_database");
        let mut store = PostgresStore::from_client(client_in_schema(&connection, &schema_name));
        let empty = store.create_database(&empty_id, Schema::new()).unwrap();
        let installed = store.create_database(&schema_id, test_schema()).unwrap();
        drop(store);

        let mut client = client_in_schema(&connection, &schema_name);
        for (database_id, expected, expected_coordinate_count) in
            [(&empty_id, &empty, 1_i64), (&schema_id, &installed, 2_i64)]
        {
            let head = client
                .query_one(
                    "SELECT log_generation, basis_t, tx_hash FROM atomic_heads \
                      WHERE database_id = $1",
                    &[database_id],
                )
                .unwrap();
            let generation = u64::try_from(head.get::<_, i64>(0)).unwrap();
            let basis = u64::try_from(head.get::<_, i64>(1)).unwrap();
            let coordinate =
                load_persistent_coordinate(&mut client, database_id, generation, basis)
                    .unwrap()
                    .expect("created head requires its semantic coordinate");
            assert_eq!(
                coordinate.root.metadata(),
                checkpoint_root_metadata(expected)
            );
            assert_eq!(
                coordinate.state_hash,
                checkpoint_state_hash(expected).unwrap()
            );
            let count: i64 = client
                .query_one(
                    "SELECT count(*) FROM atomic_semantic_commitment_roots \
                      WHERE database_id = $1 AND generation = $2",
                    &[database_id, &i64::try_from(generation).unwrap()],
                )
                .unwrap()
                .get(0);
            assert_eq!(count, expected_coordinate_count);

            let restarted = PostgresStore::from_client(client_in_schema(&connection, &schema_name))
                .recover(database_id)
                .unwrap();
            assert!(restarted.same_information_as(expected));
        }

        drop(client);
        drop_schema(&connection, &schema_name);
    }
}
