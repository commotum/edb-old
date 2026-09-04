//! Packing-independent commitment to one immutable database value.
//!
//! Transaction hashes authenticate the chronological log, but a materialized
//! index can copy the right terminal transaction hash while containing
//! different facts.  The v2 commitment below authenticates the semantic
//! current logical set without depending on physical index segments, fanout,
//! insertion order, or the timing of noHistory consolidation. The separately
//! bound transaction hash authenticates chronological log identity.
//!
//! The accumulator is a deterministic persistent Merkle treap. Keys are
//! `(attribute, canonical-datom-digest)`, which gives exact datom identity
//! under SHA-256 collision resistance. Priorities are separately
//! domain-separated hashes of keys, so the Cartesian tree is canonical for a
//! set rather than for its insertion history. Every node hash authenticates
//! its leaf, child roots and subtree count. This is not an algebraic/XOR
//! accumulator: removal rebuilds and re-authenticates the affected path.
//!
//! A semantic root copied into a physical-tree manifest remains only a claim.
//! Ordinary recovery authenticates and trusts the conditionally published
//! native indexer root, as it trusts the transactor/indexer boundary itself.
//! Explicit deep integrity inspection reconstructs the authoritative log value
//! and compares its exact current and retained-history information with the
//! physical tree. Manifest metadata alone cannot prove that cross-structure
//! equivalence against a privileged publisher capable of forging both values.

use crate::encoding::canonical_datom_hash;
use crate::{
    DB_NO_HISTORY, Database, Datom, Digest, ErrorCategory, IndexOrder, IndexPrefix, SemanticError,
    Value, View, schema_eid_to_attr_id, tx_to_t,
};
use sha2::{Digest as _, Sha256};
use std::cmp::Ordering;
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;

const LEGACY_DOMAIN: &[u8] = b"atomic/checkpoint-state/v1\0";
const STATE_DOMAIN: &[u8] = b"atomic/semantic-state/v2\0";
const EMPTY_DOMAIN: &[u8] = b"atomic/semantic-set/v2/empty\0";
const LEAF_DOMAIN: &[u8] = b"atomic/semantic-set/v2/leaf\0";
const PRIORITY_DOMAIN: &[u8] = b"atomic/semantic-set/v2/priority\0";
const NODE_DOMAIN: &[u8] = b"atomic/semantic-set/v2/node\0";

pub(crate) const SEMANTIC_STATE_VERSION: u32 = 2;

/// Canonical identity of one committed datom. The attribute prefix provides a
/// stable range key for future authenticated physical-tree proofs; the digest
/// commits to every stored field, including transaction, assertion bit and
/// typed value bytes.
#[derive(Clone, Copy, Debug, Eq, Ord, PartialEq, PartialOrd)]
pub(crate) struct SemanticDatomKey {
    pub(crate) attribute: u32,
    pub(crate) digest: Digest,
}

pub(crate) fn semantic_datom_key(datom: &Datom) -> Result<SemanticDatomKey, SemanticError> {
    Ok(SemanticDatomKey {
        attribute: datom.attribute,
        digest: canonical_datom_hash(datom)?,
    })
}

pub(crate) fn semantic_leaf_hash(key: SemanticDatomKey) -> Digest {
    hash_parts(LEAF_DOMAIN, &[&key.attribute.to_be_bytes(), &key.digest])
}

/// Packing-independent roots suitable for binding a physical index to an
/// already-authoritative database semantic state. They are not, by themselves,
/// evidence that an untrusted physical tree actually contains those sets.
#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct SemanticRootMetadata {
    pub(crate) version: u32,
    pub(crate) current_root: Digest,
    pub(crate) current_count: u64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct CommitmentWork {
    /// Search-tree nodes inspected by the incremental update.
    pub(crate) node_visits: u64,
    /// Merkle nodes rehashed (including new leaves).
    pub(crate) node_hashes: u64,
    /// Set members actually inserted or removed.
    pub(crate) leaf_changes: u64,
}

impl CommitmentWork {
    pub(crate) fn visit(&mut self) {
        self.node_visits = self.node_visits.saturating_add(1);
    }

    pub(crate) fn rehash(&mut self) {
        self.node_hashes = self.node_hashes.saturating_add(1);
    }

    pub(crate) fn change_leaf(&mut self) {
        self.leaf_changes = self.leaf_changes.saturating_add(1);
    }
}

type Link = Option<Arc<Node>>;

#[derive(Clone, Debug)]
struct Node {
    key: SemanticDatomKey,
    priority: Digest,
    left: Link,
    right: Link,
    count: u64,
    hash: Digest,
}

impl Node {
    fn build(
        key: SemanticDatomKey,
        left: Link,
        right: Link,
        work: &mut CommitmentWork,
    ) -> Result<Arc<Self>, SemanticError> {
        let count = link_count(&left)
            .checked_add(1)
            .and_then(|count| count.checked_add(link_count(&right)))
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::Fault,
                    "commitment/member-count-overflow",
                    "semantic commitment cannot contain more than u64 datoms",
                )
            })?;
        let hash = semantic_node_hash(key, count, link_hash(&left), link_hash(&right));
        work.rehash();
        Ok(Arc::new(Self {
            key,
            priority: semantic_key_priority(key),
            left,
            right,
            count,
            hash,
        }))
    }
}

#[derive(Clone, Debug, Default)]
struct AuthenticatedSet {
    root: Link,
}

impl AuthenticatedSet {
    fn from_datoms<'a>(datoms: impl IntoIterator<Item = &'a Datom>) -> Result<Self, SemanticError> {
        let mut set = Self::default();
        let mut ignored = CommitmentWork::default();
        for datom in datoms {
            set.insert(datom, &mut ignored)?;
        }
        Ok(set)
    }

    fn insert(&mut self, datom: &Datom, work: &mut CommitmentWork) -> Result<(), SemanticError> {
        let key = semantic_datom_key(datom)?;
        let (root, changed) = insert(self.root.clone(), key, work)?;
        if changed {
            work.change_leaf();
            self.root = root;
        }
        Ok(())
    }

    fn remove(&mut self, datom: &Datom, work: &mut CommitmentWork) -> Result<(), SemanticError> {
        let key = semantic_datom_key(datom)?;
        let (root, changed) = remove(self.root.clone(), key, work)?;
        if changed {
            work.change_leaf();
            self.root = root;
        }
        Ok(())
    }

    fn count(&self) -> u64 {
        link_count(&self.root)
    }

    fn root_hash(&self) -> Digest {
        link_hash(&self.root)
    }
}

/// Incrementally maintained roots carried by every immutable Database value.
#[derive(Clone, Debug, Default)]
pub(crate) struct SemanticStateCommitment {
    current: AuthenticatedSet,
}

impl SemanticStateCommitment {
    pub(crate) fn recompute(database: &Database) -> Result<Self, SemanticError> {
        Self::from_current(&database.datoms(View::Current, IndexOrder::Eavt))
    }

    pub(crate) fn from_current(current: &[Datom]) -> Result<Self, SemanticError> {
        Ok(Self {
            current: AuthenticatedSet::from_datoms(current)?,
        })
    }

    pub(crate) fn advance(
        &self,
        before: &Database,
        tx_data: &[Datom],
    ) -> Result<(Self, CommitmentWork), SemanticError> {
        let mut next = self.clone();
        let mut work = CommitmentWork::default();

        for datom in tx_data {
            apply_current_change(&mut next.current, before, datom, &mut work)?;
        }
        Ok((next, work))
    }

    pub(crate) fn metadata(&self) -> SemanticRootMetadata {
        SemanticRootMetadata {
            version: SEMANTIC_STATE_VERSION,
            current_root: self.current.root_hash(),
            current_count: self.current.count(),
        }
    }

    pub(crate) fn digest(&self, basis_t: u64, eidx_frontier: u64) -> Digest {
        semantic_state_digest(self.metadata(), basis_t, eidx_frontier)
    }
}

fn apply_current_change(
    set: &mut AuthenticatedSet,
    before: &Database,
    datom: &Datom,
    work: &mut CommitmentWork,
) -> Result<(), SemanticError> {
    let prior = exact_current_datom(before, datom)?;
    if datom.added {
        match prior {
            Some(prior) if u64::from(datom.attribute) == crate::DB_ALTER_ATTRIBUTE => {
                set.remove(&prior, work)?;
                set.insert(datom, work)?;
            }
            None => {
                set.insert(datom, work)?;
            }
            Some(_) => {}
        }
    } else if let Some(prior) = prior {
        set.remove(&prior, work)?;
    }
    Ok(())
}

fn exact_current_datom(
    database: &Database,
    candidate: &Datom,
) -> Result<Option<Datom>, SemanticError> {
    Ok(database
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: candidate.entity,
            attribute: Some(candidate.attribute),
            value: Some(candidate.value.clone()),
        })?
        .iter()
        .find(|datom| datom.value.stored_eq(&candidate.value))
        .cloned())
}

fn insert(
    root: Link,
    key: SemanticDatomKey,
    work: &mut CommitmentWork,
) -> Result<(Link, bool), SemanticError> {
    work.visit();
    let Some(node) = root else {
        return Ok((Some(Node::build(key, None, None, work)?), true));
    };
    match key.cmp(&node.key) {
        Ordering::Equal => Ok((Some(node), false)),
        Ordering::Less => {
            let (left, changed) = insert(node.left.clone(), key, work)?;
            if !changed {
                return Ok((Some(node), false));
            }
            let rebuilt = Node::build(node.key, left, node.right.clone(), work)?;
            if heap_precedes(
                rebuilt.left.as_ref().expect("inserted left child"),
                &rebuilt,
            ) {
                Ok((Some(rotate_right(rebuilt, work)?), true))
            } else {
                Ok((Some(rebuilt), true))
            }
        }
        Ordering::Greater => {
            let (right, changed) = insert(node.right.clone(), key, work)?;
            if !changed {
                return Ok((Some(node), false));
            }
            let rebuilt = Node::build(node.key, node.left.clone(), right, work)?;
            if heap_precedes(
                rebuilt.right.as_ref().expect("inserted right child"),
                &rebuilt,
            ) {
                Ok((Some(rotate_left(rebuilt, work)?), true))
            } else {
                Ok((Some(rebuilt), true))
            }
        }
    }
}

fn remove(
    root: Link,
    key: SemanticDatomKey,
    work: &mut CommitmentWork,
) -> Result<(Link, bool), SemanticError> {
    work.visit();
    let Some(node) = root else {
        return Ok((None, false));
    };
    match key.cmp(&node.key) {
        Ordering::Equal => Ok((merge(node.left.clone(), node.right.clone(), work)?, true)),
        Ordering::Less => {
            let (left, changed) = remove(node.left.clone(), key, work)?;
            if !changed {
                return Ok((Some(node), false));
            }
            Ok((
                Some(Node::build(node.key, left, node.right.clone(), work)?),
                true,
            ))
        }
        Ordering::Greater => {
            let (right, changed) = remove(node.right.clone(), key, work)?;
            if !changed {
                return Ok((Some(node), false));
            }
            Ok((
                Some(Node::build(node.key, node.left.clone(), right, work)?),
                true,
            ))
        }
    }
}

fn merge(left: Link, right: Link, work: &mut CommitmentWork) -> Result<Link, SemanticError> {
    match (left, right) {
        (None, right) => Ok(right),
        (left, None) => Ok(left),
        (Some(left), Some(right)) if heap_precedes(&left, &right) => {
            let merged = merge(left.right.clone(), Some(right), work)?;
            Ok(Some(Node::build(
                left.key,
                left.left.clone(),
                merged,
                work,
            )?))
        }
        (Some(left), Some(right)) => {
            let merged = merge(Some(left), right.left.clone(), work)?;
            Ok(Some(Node::build(
                right.key,
                merged,
                right.right.clone(),
                work,
            )?))
        }
    }
}

fn rotate_right(root: Arc<Node>, work: &mut CommitmentWork) -> Result<Arc<Node>, SemanticError> {
    let pivot = root.left.as_ref().expect("rotation requires left child");
    let right = Node::build(root.key, pivot.right.clone(), root.right.clone(), work)?;
    Node::build(pivot.key, pivot.left.clone(), Some(right), work)
}

fn rotate_left(root: Arc<Node>, work: &mut CommitmentWork) -> Result<Arc<Node>, SemanticError> {
    let pivot = root.right.as_ref().expect("rotation requires right child");
    let left = Node::build(root.key, root.left.clone(), pivot.left.clone(), work)?;
    Node::build(pivot.key, Some(left), pivot.right.clone(), work)
}

fn heap_precedes(left: &Node, right: &Node) -> bool {
    left.priority
        .cmp(&right.priority)
        .then_with(|| left.key.cmp(&right.key))
        .is_lt()
}

fn link_count(link: &Link) -> u64 {
    link.as_ref().map_or(0, |node| node.count)
}

fn link_hash(link: &Link) -> Digest {
    link.as_ref()
        .map_or_else(semantic_empty_hash, |node| node.hash)
}

pub(crate) fn semantic_empty_hash() -> Digest {
    hash_parts(EMPTY_DOMAIN, &[])
}

pub(crate) fn semantic_key_priority(key: SemanticDatomKey) -> Digest {
    hash_parts(
        PRIORITY_DOMAIN,
        &[&key.attribute.to_be_bytes(), &key.digest],
    )
}

/// Reproduce the exact v2 node digest from a canonical logical node. Durable
/// implementations use this boundary instead of copying the domain separator
/// or field ordering and silently creating a second commitment format.
pub(crate) fn semantic_node_hash(
    key: SemanticDatomKey,
    count: u64,
    left: Digest,
    right: Digest,
) -> Digest {
    let leaf = semantic_leaf_hash(key);
    hash_parts(NODE_DOMAIN, &[&count.to_be_bytes(), &left, &leaf, &right])
}

/// Bind one v2 semantic-set root to the immutable database coordinate.
pub(crate) fn semantic_state_digest(
    metadata: SemanticRootMetadata,
    basis_t: u64,
    eidx_frontier: u64,
) -> Digest {
    hash_parts(
        STATE_DOMAIN,
        &[
            &metadata.version.to_be_bytes(),
            &basis_t.to_be_bytes(),
            &eidx_frontier.to_be_bytes(),
            &metadata.current_count.to_be_bytes(),
            &metadata.current_root,
        ],
    )
}

fn hash_parts(domain: &[u8], parts: &[&[u8]]) -> Digest {
    let mut hash = Sha256::new();
    hash.update(domain);
    for part in parts {
        hash.update((part.len() as u64).to_be_bytes());
        hash.update(part);
    }
    hash.finalize().into()
}

/// Return the exact current and retained-history EAVT information represented
/// by a canonical durable base.
///
/// This is a physical indexing projection, not part of transaction-time state
/// identity. Like the recovered `filter-nohist-pairs`, it consults the schema
/// of the endpoint database and removes only adjacent retraction/assertion
/// pairs for attributes that are `:db/noHistory` at that indexing job. The
/// supplied EAVT history already represents the complete old-base/new-data
/// merge, so neither transaction cutoffs nor pre-toggle preservation belong at
/// this boundary. Changing `:db/noHistory` still does nothing immediately: the
/// projection takes effect only when a caller actually builds a new base.
pub(crate) fn checkpoint_information(
    database: &Database,
) -> Result<(Vec<Datom>, Vec<Datom>), SemanticError> {
    let current = database.datoms(View::Current, IndexOrder::Eavt);
    let history = filter_no_history_pairs(
        database.schema(),
        &database.datoms(View::History, IndexOrder::Eavt),
    )?;
    Ok((current, history))
}

fn filter_no_history_pairs(
    schema: &crate::Schema,
    history: &[Datom],
) -> Result<Vec<Datom>, SemanticError> {
    let mut retained = Vec::with_capacity(history.len());
    let mut offset = 0;
    while offset < history.len() {
        let datom = &history[offset];
        let no_history = schema.attribute(datom.attribute)?.no_history;
        let matching_assertion = history.get(offset + 1).is_some_and(|next| {
            next.added
                && datom.entity == next.entity
                && datom.attribute == next.attribute
                && datom.value.index_cmp(&next.value).is_eq()
        });
        if !datom.added && no_history && matching_assertion {
            offset += 2;
        } else {
            retained.push(datom.clone());
            offset += 1;
        }
    }
    Ok(retained)
}

fn facts_at(
    history: &[Datom],
    attribute: u32,
    through_t: u64,
) -> Result<Vec<Datom>, SemanticError> {
    let mut datoms = history
        .iter()
        .filter(|datom| {
            datom.attribute == attribute && tx_to_t(datom.tx).is_ok_and(|t| t <= through_t)
        })
        .cloned()
        .collect::<Vec<_>>();
    datoms.sort_by(|left, right| {
        left.tx
            .cmp(&right.tx)
            .then_with(|| left.entity.cmp(&right.entity))
            .then_with(|| left.value.stored_cmp(&right.value))
            .then_with(|| left.added.cmp(&right.added))
    });
    let mut live = Vec::new();
    for datom in datoms {
        if datom.added {
            if !contains_stored(&live, &datom) {
                live.push(datom);
            }
        } else {
            live.retain(|fact| !same_eav(fact, &datom));
        }
    }
    Ok(live)
}

fn contains_stored(datoms: &[Datom], candidate: &Datom) -> bool {
    datoms.iter().any(|datom| {
        datom.tx == candidate.tx && datom.added == candidate.added && same_eav(datom, candidate)
    })
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

/// O(1) v2 digest from the roots carried by this immutable database value.
pub(crate) fn checkpoint_state_hash(database: &Database) -> Result<Digest, SemanticError> {
    Ok(database
        .semantic_state_commitment()
        .digest(database.basis_t(), database.eidx_frontier()))
}

#[allow(dead_code)] // Contract for the physical-tree range-proof integration.
pub(crate) fn checkpoint_root_metadata(database: &Database) -> SemanticRootMetadata {
    database.semantic_state_commitment().metadata()
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) enum StateHashVersion {
    IncrementalV2,
    LegacyV1,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub(crate) struct StateHashVerification {
    pub(crate) matched_version: Option<StateHashVersion>,
    /// Number of canonical datoms hashed only when a v2 mismatch required the
    /// explicit persisted-v1 compatibility path.
    pub(crate) legacy_datoms_hashed: u64,
}

impl StateHashVerification {
    pub(crate) fn matches(self) -> bool {
        self.matched_version.is_some()
    }
}

/// Match a persisted state hash. New values take the O(1) v2 path. A mismatch
/// explicitly tries the former v1 full projection so databases written during
/// the v1 development epoch remain recoverable; the returned work count makes
/// that exceptional O(information) path visible to callers and tests.
pub(crate) fn verify_checkpoint_state_hash(
    database: &Database,
    expected: Digest,
) -> Result<StateHashVerification, SemanticError> {
    if checkpoint_state_hash(database)? == expected {
        return Ok(StateHashVerification {
            matched_version: Some(StateHashVersion::IncrementalV2),
            legacy_datoms_hashed: 0,
        });
    }
    let (legacy, count) = legacy_checkpoint_state_hash(database)?;
    Ok(StateHashVerification {
        matched_version: (legacy == expected).then_some(StateHashVersion::LegacyV1),
        legacy_datoms_hashed: count,
    })
}

fn legacy_checkpoint_state_hash(database: &Database) -> Result<(Digest, u64), SemanticError> {
    // Preserve byte-for-byte verification of rows emitted before v2. The v1
    // projection incorrectly treated noHistory as a wholesale purge; keeping
    // that algorithm here is compatibility, not current semantics.
    let (current, history) = legacy_v1_information(database)?;
    let mut commitment = Sha256::new();
    commitment.update(LEGACY_DOMAIN);
    commitment.update(database.basis_t().to_be_bytes());
    commitment.update(database.eidx_frontier().to_be_bytes());
    legacy_commit_datoms(&mut commitment, false, &current)?;
    legacy_commit_datoms(&mut commitment, true, &history)?;
    let count = u64::try_from(current.len())
        .unwrap_or(u64::MAX)
        .saturating_add(u64::try_from(history.len()).unwrap_or(u64::MAX));
    Ok((commitment.finalize().into(), count))
}

fn legacy_v1_information(database: &Database) -> Result<(Vec<Datom>, Vec<Datom>), SemanticError> {
    let current = database.datoms(View::Current, IndexOrder::Eavt);
    let mut history = database.datoms(View::History, IndexOrder::Eavt);
    let mut last_disabled = BTreeMap::<u32, u64>::new();
    let mut mentioned = BTreeSet::<u32>::new();
    for datom in &history {
        if datom.attribute != DB_NO_HISTORY as u32 {
            continue;
        }
        let attribute = schema_eid_to_attr_id(datom.entity)?;
        match (&datom.value, datom.added) {
            (Value::Bool(true), true) => {
                mentioned.insert(attribute);
            }
            (Value::Bool(true), false) | (Value::Bool(false), true) => {
                mentioned.insert(attribute);
                let disabled_at = tx_to_t(datom.tx)?;
                last_disabled
                    .entry(attribute)
                    .and_modify(|known| *known = (*known).max(disabled_at))
                    .or_insert(disabled_at);
            }
            _ => {}
        }
    }
    let mut boundary = BTreeMap::<u32, Vec<Datom>>::new();
    for (&attribute, &cutoff) in &last_disabled {
        boundary.insert(attribute, facts_at(&history, attribute, cutoff)?);
    }
    history.retain(|datom| {
        if !mentioned.contains(&datom.attribute) {
            return true;
        }
        let currently_forgetting = database
            .schema()
            .attribute(datom.attribute)
            .is_ok_and(|attribute| attribute.no_history);
        if currently_forgetting {
            return datom.added && contains_stored(&current, datom);
        }
        match last_disabled.get(&datom.attribute) {
            Some(cutoff) => {
                tx_to_t(datom.tx).is_ok_and(|t| t >= *cutoff)
                    || boundary
                        .get(&datom.attribute)
                        .is_some_and(|facts| datom.added && contains_stored(facts, datom))
            }
            None => true,
        }
    });
    Ok((current, history))
}

fn legacy_commit_datoms(
    commitment: &mut Sha256,
    history: bool,
    datoms: &[Datom],
) -> Result<(), SemanticError> {
    commitment.update([u8::from(history)]);
    commitment.update((datoms.len() as u64).to_be_bytes());
    for datom in datoms {
        commitment.update(canonical_datom_hash(datom)?);
    }
    Ok(())
}

#[cfg(test)]
fn oracle_v2(database: &Database) -> Result<Digest, SemanticError> {
    Ok(SemanticStateCommitment::recompute(database)?
        .digest(database.basis_t(), database.eidx_frontier()))
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, TxValue, ValueType, t_to_tx,
    };
    use bigdecimal::BigDecimal;
    use std::str::FromStr;

    fn long_attribute(id: u32, no_history: bool) -> Attribute {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("item", format!("value-{id}")),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.no_history = no_history;
        attribute
    }

    #[test]
    fn commitment_is_deterministic_for_bootstrap() {
        let database = Database::bootstrap().unwrap();
        assert_eq!(
            checkpoint_state_hash(&database).unwrap(),
            oracle_v2(&database).unwrap()
        );
    }

    #[test]
    fn adversarial_insertion_order_and_remove_reinsert_have_one_root() {
        let datoms = (0_u64..257)
            .map(|offset| Datom {
                entity: 10_000 + ((offset * 97) % 257),
                attribute: 1_000 + (offset % 7) as u32,
                value: Value::Long(offset as i64),
                tx: 1 + offset,
                added: offset % 3 != 0,
            })
            .collect::<Vec<_>>();
        let forward = AuthenticatedSet::from_datoms(&datoms).unwrap();
        let reverse = AuthenticatedSet::from_datoms(datoms.iter().rev()).unwrap();
        assert_eq!(forward.root_hash(), reverse.root_hash());
        assert_eq!(forward.count(), reverse.count());

        let mut changed = forward.clone();
        let mut work = CommitmentWork::default();
        for datom in datoms.iter().step_by(5) {
            changed.remove(datom, &mut work).unwrap();
        }
        for datom in datoms.iter().step_by(5).rev() {
            changed.insert(datom, &mut work).unwrap();
        }
        assert_eq!(forward.root_hash(), changed.root_hash());
        assert_eq!(forward.count(), changed.count());
    }

    #[test]
    fn incremental_transitions_match_full_oracle_across_no_history_toggles() {
        let attribute = long_attribute(1_000, false);
        let mut schema = Schema::new();
        schema.install(attribute.clone()).unwrap();
        let mut database = Database::new(schema).unwrap();
        let mut entity = None;
        let mut lcg = 0x9e37_79b9_u64;
        for step in 1_i64..=96 {
            lcg = lcg
                .wrapping_mul(6_364_136_223_846_793_005)
                .wrapping_add(1_442_695_040_888_963_407);
            let report = if step == 1 {
                database
                    .with(
                        &[TxOp::Add {
                            entity: EntityRef::Temp("entity".into()),
                            attribute: 1_000,
                            value: TxValue::Scalar(Value::Long(lcg as i64)),
                        }],
                        step,
                    )
                    .unwrap()
            } else if step % 17 == 0 {
                let mut altered = attribute.clone();
                altered.no_history = (step / 17) % 2 == 1;
                // Carry the operative descriptor forward across later toggles.
                let mut current = database.schema().attribute(1_000).unwrap().clone();
                current.no_history = altered.no_history;
                database
                    .with(&[TxOp::AlterAttribute(current)], step)
                    .unwrap()
            } else {
                database
                    .with(
                        &[TxOp::Add {
                            entity: EntityRef::Id(entity.unwrap()),
                            attribute: 1_000,
                            value: TxValue::Scalar(Value::Long(lcg as i64)),
                        }],
                        step,
                    )
                    .unwrap()
            };
            entity = entity.or_else(|| report.tempids.get("entity").copied());
            database = report.db_after;
            assert_eq!(
                checkpoint_state_hash(&database).unwrap(),
                oracle_v2(&database).unwrap(),
                "incremental commitment diverged at step {step}"
            );
        }
    }

    #[test]
    fn false_before_indexing_preserves_the_unconsolidated_true_interval() {
        let mut attribute = long_attribute(1_000, true);
        let mut schema = Schema::new();
        schema.install(attribute.clone()).unwrap();
        let first = Database::new(schema)
            .unwrap()
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: attribute.id,
                    value: TxValue::Scalar(Value::Long(1)),
                }],
                1,
            )
            .unwrap();
        let entity = first.tempids["item"];
        let database = first
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: attribute.id,
                    value: TxValue::Scalar(Value::Long(2)),
                }],
                2,
            )
            .unwrap()
            .db_after;
        attribute.no_history = false;
        let database = database
            .with(&[TxOp::AlterAttribute(attribute)], 3)
            .unwrap()
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(3)),
                }],
                4,
            )
            .unwrap()
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(4)),
                }],
                5,
            )
            .unwrap()
            .db_after;

        let (_, retained) = checkpoint_information(&database).unwrap();
        assert_eq!(
            retained,
            database.datoms(View::History, IndexOrder::Eavt),
            "false at the indexing endpoint makes an unconsolidated interval an identity projection"
        );
        assert!(retained.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == 1_000
                && datom.value == Value::Long(1)
                && datom.added
        }));
        assert!(retained.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == 1_000
                && datom.value == Value::Long(1)
                && !datom.added
        }));
        assert!(retained.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == 1_000
                && datom.value == Value::Long(2)
                && datom.added
        }));
        assert!(retained.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == 1_000
                && datom.value == Value::Long(3)
                && datom.added
        }));
        assert!(retained.iter().any(|datom| {
            datom.entity == entity
                && datom.attribute == 1_000
                && datom.value == Value::Long(3)
                && !datom.added
        }));
        assert_eq!(
            checkpoint_state_hash(&database).unwrap(),
            oracle_v2(&database).unwrap()
        );
    }

    #[test]
    fn full_initial_build_filters_all_endpoint_no_history_pairs() {
        let mut attribute = long_attribute(1_000, false);
        let mut schema = Schema::new();
        schema.install(attribute.clone()).unwrap();
        let first = Database::new(schema)
            .unwrap()
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(1)),
                }],
                1,
            )
            .unwrap();
        let entity = first.tempids["item"];
        let database = first
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(2)),
                }],
                2,
            )
            .unwrap()
            .db_after;
        attribute.no_history = true;
        let database = database
            .with(&[TxOp::AlterAttribute(attribute)], 3)
            .unwrap()
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(3)),
                }],
                4,
            )
            .unwrap()
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(4)),
                }],
                5,
            )
            .unwrap()
            .db_after;

        let (current, retained) = checkpoint_information(&database).unwrap();
        assert_eq!(
            current,
            database.datoms(View::Current, IndexOrder::Eavt),
            "the physical noHistory projection must not change current facts"
        );
        let values = retained
            .iter()
            .filter(|datom| datom.entity == entity && datom.attribute == 1_000)
            .collect::<Vec<_>>();
        let has = |value, added| {
            values
                .iter()
                .any(|datom| datom.value == Value::Long(value) && datom.added == added)
        };
        for forgotten in [1, 2, 3] {
            assert!(
                !values
                    .iter()
                    .any(|datom| datom.value == Value::Long(forgotten)),
                "a full build applies the endpoint schema to the entire supplied EAVT history"
            );
        }
        assert!(has(4, true));
    }

    #[test]
    fn no_history_pair_matching_uses_logical_numeric_equality() {
        let mut attribute = Attribute::new(
            1_000,
            Keyword::new("item", "decimal"),
            ValueType::BigDec,
            Cardinality::One,
        );
        attribute.no_history = true;
        let mut schema = Schema::new();
        schema.install(attribute).unwrap();

        let retraction = Datom {
            entity: 100,
            attribute: 1_000,
            value: Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
            tx: t_to_tx(2).unwrap(),
            added: false,
        };
        let assertion = Datom {
            entity: 100,
            attribute: 1_000,
            value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
            tx: t_to_tx(1).unwrap(),
            added: true,
        };
        assert!(
            retraction.cmp_in(&assertion, IndexOrder::Eavt).is_lt(),
            "the fixture must be a valid adjacent EAVT pair"
        );
        assert!(!retraction.value.stored_eq(&assertion.value));
        assert!(retraction.value.index_cmp(&assertion.value).is_eq());

        assert!(
            filter_no_history_pairs(&schema, &[retraction, assertion])
                .unwrap()
                .is_empty(),
            "recovered common/compare semantics ignore BigDecimal scale here"
        );
    }

    #[test]
    fn one_small_delta_does_logarithmic_commitment_work_on_large_state() {
        let mut attribute = long_attribute(1_000, false);
        attribute.cardinality = Cardinality::Many;
        let mut schema = Schema::new();
        schema.install(attribute).unwrap();
        let mut database = Database::new(schema).unwrap();
        // The kernel oracle still rebuilds its eager indexes, so keep this
        // large enough to distinguish logarithmic commitment work without
        // turning that unrelated known O(N) path into a unit-test benchmark.
        let bulk = (0_i64..1_024)
            .map(|value| TxOp::Add {
                entity: EntityRef::Temp("entity".into()),
                attribute: 1_000,
                value: TxValue::Scalar(Value::Long(value)),
            })
            .collect::<Vec<_>>();
        let report = database.with(&bulk, 1).unwrap();
        let entity = report.tempids["entity"];
        database = report.db_after;
        database = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(10_000)),
                }],
                2,
            )
            .unwrap()
            .db_after;
        let work = database.semantic_commitment_work();
        assert!(work.leaf_changes <= 4, "unexpected leaf work: {work:?}");
        assert!(
            work.node_hashes < 256,
            "non-logarithmic hash work: {work:?}"
        );
        assert_eq!(
            checkpoint_state_hash(&database).unwrap(),
            oracle_v2(&database).unwrap()
        );
    }

    #[test]
    fn legacy_v1_fallback_is_explicit_and_measured() {
        let database = Database::bootstrap().unwrap();
        let (legacy, count) = legacy_checkpoint_state_hash(&database).unwrap();
        let verification = verify_checkpoint_state_hash(&database, legacy).unwrap();
        assert_eq!(
            verification.matched_version,
            Some(StateHashVersion::LegacyV1)
        );
        assert_eq!(verification.legacy_datoms_hashed, count);
        assert!(count > 0);
        let v2 = verify_checkpoint_state_hash(&database, checkpoint_state_hash(&database).unwrap())
            .unwrap();
        assert_eq!(v2.matched_version, Some(StateHashVersion::IncrementalV2));
        assert_eq!(v2.legacy_datoms_hashed, 0);
    }

    #[test]
    fn streaming_legacy_compatibility_has_no_multi_datom_blob_ceiling() {
        const VALUE_BYTES: usize = 13 * 1024 * 1024;
        let datoms = (0_u64..5)
            .map(|offset| Datom {
                entity: 1_000 + offset,
                attribute: 1_000,
                value: Value::Bytes(vec![offset as u8; VALUE_BYTES]),
                tx: 1,
                added: true,
            })
            .collect::<Vec<_>>();
        assert!(datoms.len() * VALUE_BYTES > 64 * 1024 * 1024);
        let mut digest = Sha256::new();
        legacy_commit_datoms(&mut digest, false, &datoms).unwrap();
    }
}
