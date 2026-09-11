//! Storage-neutral immutable index preparation and metadata reconstruction.
use crate::idents::IdentIndex;
#[cfg(test)]
use crate::persistent_tree::build_tree;
use crate::persistent_tree::{
    ChildRef, DirectoryNode, LeafSegment, RootNode, TreeConfig, TreeMergeEdits, TreeNode,
    TreeNodeSet, decode_tree_node, merge_tree_with_boundary_loader,
};
use crate::recent::RecentTier;
use crate::tree_cursor::{
    floor_tree_child, leaf_lower_bound, prefix_start_child, validate_loaded_child_datom,
    validate_loaded_child_key,
};
#[cfg(test)]
use crate::{Database, View};
use crate::{
    Datom, Digest, DurableTransaction, ErrorCategory, IndexOrder, IndexPrefix, SemanticError,
};
use std::collections::{BTreeMap, BTreeSet};
use std::sync::Arc;
use std::sync::atomic::Ordering;
#[cfg(test)]
use std::time::Instant;
#[cfg(test)]
#[path = "peer_boundary_bias_tests.rs"]
mod boundary_bias_tests;
#[cfg(test)]
mod metadata_alias_test {
    use super::*;
    use crate::{
        Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, TxValue, Value, ValueType,
    };
    #[test]
    fn tree_metadata_rebuild_preserves_aliases_without_retaining_assertion_history() {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("item", "kind"),
                ValueType::Ref,
                Cardinality::One,
            ))
            .unwrap();
        let database = Database::new(schema).unwrap();
        let old = Keyword::new("kind", "old");
        let new = Keyword::new("kind", "new");
        let created = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("enum".into()),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(old.clone())),
                }],
                1_000,
            )
            .unwrap();
        let original = created.tempids["enum"];
        let renamed = created
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(original),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(new.clone())),
                }],
                2_000,
            )
            .unwrap();
        let repurposed = renamed
            .db_after
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("replacement".into()),
                    attribute: crate::DB_IDENT as u32,
                    value: TxValue::Scalar(Value::Keyword(old.clone())),
                }],
                3_000,
            )
            .unwrap();
        let replacement = repurposed.tempids["replacement"];

        let mut nodes = TreeNodeSet::default();
        let mut roots = BTreeMap::new();
        for history in [false, true] {
            for order in all_index_orders() {
                let datoms = repurposed.db_after.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                );
                let tree = build_tree(order, history, datoms, &TreeConfig::default()).unwrap();
                let TreeNode::Root(root) = decode_tree_node(
                    &tree.descriptor.root_hash,
                    tree.nodes.get(&tree.descriptor.root_hash).unwrap(),
                )
                .unwrap() else {
                    panic!("root");
                };
                roots.insert((history, order_tag(order)), Arc::new(root));
                for (id, bytes) in tree.nodes.iter() {
                    nodes.insert_known(*id, bytes.to_vec()).unwrap();
                }
            }
        }
        let projection = derive_metadata_from_roots(&roots, |hash| {
            let bytes = nodes.get(&hash).ok_or_else(|| {
                fault(
                    "test/missing-tree-node",
                    "metadata derivation requested an absent test node",
                )
            })?;
            decode_tree_node(&hash, bytes).map(Arc::new)
        })
        .unwrap();

        assert_eq!(projection.idents.resolve(&old), Some(replacement));
        assert_eq!(projection.idents.resolve(&new), Some(original));
        assert_eq!(projection.idents.ident(original), Some(&new));
        assert_eq!(projection.idents.ident(replacement), Some(&old));
        assert_eq!(
            projection.schema.attribute(1_000).unwrap(),
            repurposed.db_after.schema().attribute(1_000).unwrap()
        );
        assert!(
            projection
                .schema_current
                .iter()
                .all(|datom| datom.entity != original && datom.entity != replacement),
            "general ident entities belong only in the folded ident maps"
        );
    }
}
#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ResidentMetadataStats {
    pub(crate) schema_attributes: usize,
    pub(crate) schema_information_datoms: usize,
    pub(crate) schema_estimated_bytes: u64,
    pub(crate) ident_names: usize,
    pub(crate) ident_entities: usize,
    pub(crate) ident_estimated_bytes: u64,
}

#[derive(Clone, Copy, Debug, Default, Eq, PartialEq)]
pub(crate) struct ResidentTreeRootStats {
    pub(crate) children: usize,
    pub(crate) estimated_bytes: u64,
}

/// Small discardable projection needed to classify recent AVET/VAET datoms.
/// Both inputs remain ordinary datoms authenticated by the native manifest;
/// `Schema` and `IdentIndex` are always re-derived and are never authorities.
#[derive(Clone)]
pub(crate) struct MetadataProjection {
    pub(crate) schema_current: Arc<[Datom]>,
    pub(crate) idents: Arc<IdentIndex>,
    pub(crate) schema: Arc<crate::Schema>,
    // The projection is immutable. Compute its diagnostic sizes only when
    // deriving new metadata, never on an ordinary writer's statistics read.
    residency: ResidentMetadataStats,
}

#[cfg(test)]
thread_local! {
    static METADATA_STAT_COMPUTATIONS: std::cell::Cell<u64> = const { std::cell::Cell::new(0) };
}

impl MetadataProjection {
    #[cfg(test)]
    pub(crate) fn from_information(
        mut schema_current: Vec<Datom>,
        ident_assertions: Vec<Datom>,
    ) -> Result<Self, SemanticError> {
        sort_dedup_datoms(&mut schema_current, IndexOrder::Eavt);
        let idents = IdentIndex::derive(ident_assertions.iter(), crate::DB_IDENT as u32)?;
        Self::from_current_and_idents(schema_current, idents)
    }

    fn from_current_and_idents(
        mut schema_current: Vec<Datom>,
        idents: IdentIndex,
    ) -> Result<Self, SemanticError> {
        sort_dedup_datoms(&mut schema_current, IndexOrder::Eavt);
        retain_schema_working_set(&mut schema_current);
        let schema = crate::Schema::derive_from_information(&schema_current, &idents)?;
        let residency = Self::compute_residency(&schema_current, &schema, &idents);
        Ok(Self {
            schema_current: schema_current.into(),
            idents: Arc::new(idents),
            schema: Arc::new(schema),
            residency,
        })
    }

    #[cfg(test)]
    fn from_database(database: &Database) -> Result<Self, SemanticError> {
        let schema_current = database
            .datoms(View::Current, IndexOrder::Eavt)
            .into_iter()
            .filter(|datom| schema_information_attribute(datom.attribute))
            .collect();
        let ident_assertions = database
            .datoms(View::History, IndexOrder::Aevt)
            .into_iter()
            .filter(|datom| datom.added && datom.attribute == crate::DB_IDENT as u32)
            .collect();
        Self::from_information(schema_current, ident_assertions)
    }

    pub(crate) fn apply(&self, transactions: &[DurableTransaction]) -> Result<Self, SemanticError> {
        if !transactions.iter().any(|transaction| {
            transaction
                .tx_data
                .iter()
                .any(|datom| schema_information_attribute(datom.attribute))
        }) {
            return Ok(self.clone());
        }

        if !transactions
            .iter()
            .flat_map(|transaction| &transaction.tx_data)
            .any(|datom| {
                self.schema.datom_may_change_schema(
                    datom.entity,
                    datom.attribute,
                    &datom.value,
                    datom.added,
                )
            })
        {
            // General entity idents change the identity projection, not the
            // installed schema. Keep its validated descriptors/information and
            // size totals shared. Attribute/partition renames and repurposed
            // schema aliases deliberately take the full derivation below.
            let mut updates = transactions
                .iter()
                .flat_map(|transaction| &transaction.tx_data)
                .filter(|datom| datom.added && datom.attribute == crate::DB_IDENT as u32)
                .collect::<Vec<_>>();
            if updates.is_empty() {
                return Ok(self.clone());
            }
            updates.sort_by(|left, right| ident_assertion_cmp(left, right));
            let mut idents = (*self.idents).clone();
            for datom in updates {
                idents.apply_assertion(datom, crate::DB_IDENT as u32)?;
            }
            let mut residency = self.residency;
            residency.ident_names = idents.name_count();
            residency.ident_entities = idents.entity_count();
            residency.ident_estimated_bytes = idents.estimated_retained_bytes();
            return Ok(Self {
                schema_current: Arc::clone(&self.schema_current),
                schema: Arc::clone(&self.schema),
                idents: Arc::new(idents),
                residency,
            });
        }

        // Schema/ident edits are rare. Only those edits copy the small
        // authenticated working set and derive a replacement projection;
        // ordinary transaction successors retain all three resident Arcs.
        let mut current = self.schema_current.to_vec();
        let mut idents = (*self.idents).clone();
        for transaction in transactions {
            // Older exact genesis profiles already reserve :db/fulltext's
            // immutable t=0 ident, but the small schema working set excludes
            // its entity until the explicit descriptor upgrade. Reattach that
            // authenticated ident when its install marker arrives; never emit
            // a new logical assertion or reinterpret the old root.
            if self.schema.attribute(crate::DB_FULLTEXT as u32).is_err()
                && transaction.tx_data.iter().any(|datom| {
                    datom.added
                        && datom.entity == crate::DB_PART_DB
                        && datom.attribute == crate::DB_INSTALL_ATTRIBUTE as u32
                        && datom.value == crate::Value::Ref(crate::DB_FULLTEXT)
                })
                && !current.iter().any(|datom| {
                    datom.entity == crate::DB_FULLTEXT && datom.attribute == crate::DB_IDENT as u32
                })
                && let Some(ident) = idents.ident(crate::DB_FULLTEXT)
            {
                current.push(Datom {
                    entity: crate::DB_FULLTEXT,
                    attribute: crate::DB_IDENT as u32,
                    value: crate::Value::Keyword(ident.clone()),
                    tx: crate::t_to_tx(0)?,
                    added: true,
                });
            }
            let mut ident_updates = transaction
                .tx_data
                .iter()
                .filter(|datom| datom.added && datom.attribute == crate::DB_IDENT as u32)
                .collect::<Vec<_>>();
            ident_updates.sort_by(|left, right| ident_assertion_cmp(left, right));
            for datom in ident_updates {
                idents.apply_assertion(datom, crate::DB_IDENT as u32)?;
            }
            for datom in &transaction.tx_data {
                if !schema_information_attribute(datom.attribute) {
                    continue;
                }
                if datom.added {
                    if !current.iter().any(|fact| same_eav(fact, datom)) {
                        current.push(datom.clone());
                    }
                } else {
                    current.retain(|fact| !same_eav(fact, datom));
                }
            }
        }
        Self::from_current_and_idents(current, idents)
    }

    pub(crate) fn resident_stats(&self) -> ResidentMetadataStats {
        self.residency
    }

    fn compute_residency(
        schema_current: &[Datom],
        schema: &crate::Schema,
        idents: &IdentIndex,
    ) -> ResidentMetadataStats {
        #[cfg(test)]
        METADATA_STAT_COMPUTATIONS.with(|count| count.set(count.get() + 1));
        let schema_information_bytes = schema_current.iter().fold(0_u64, |bytes, datom| {
            bytes.saturating_add(datom.retained_bytes())
        });
        ResidentMetadataStats {
            schema_attributes: schema.attributes().count(),
            schema_information_datoms: schema_current.len(),
            schema_estimated_bytes: schema
                .estimated_retained_bytes()
                .saturating_add(schema_information_bytes),
            ident_names: idents.name_count(),
            ident_entities: idents.entity_count(),
            ident_estimated_bytes: idents.estimated_retained_bytes(),
        }
    }
}

pub(crate) fn apply_metadata_and_avet_readiness<F>(
    base: &MetadataProjection,
    initial_unready: &Arc<BTreeSet<u32>>,
    transactions: &[DurableTransaction],
    mut has_base_history: F,
) -> Result<(MetadataProjection, Arc<BTreeSet<u32>>), SemanticError>
where
    F: FnMut(u32) -> Result<bool, SemanticError>,
{
    let mut metadata = base.clone();
    // Ordinary successors share even a wide pending-backfill set. Copy only
    // for an actual membership change, retaining old snapshots' readiness.
    let mut unready = Arc::clone(initial_unready);
    let mut base_history = BTreeMap::<u32, bool>::new();
    let mut prior_tail_history = BTreeSet::<u32>::new();

    for transaction in transactions {
        let endpoint = metadata.apply(std::slice::from_ref(transaction))?;
        // MetadataProjection::apply retains all three Arcs for ordinary data
        // transactions. Pointer identity is therefore a proof that no schema
        // transition exists; avoid walking every resident attribute merely to
        // rediscover that fact on every commit.
        let changed_avet = if Arc::ptr_eq(&metadata.schema, &endpoint.schema) {
            Vec::new()
        } else {
            changed_avet_attributes(&metadata.schema, &endpoint.schema)
        };
        for (attribute, before, after) in changed_avet {
            let needs_backfill = if !after {
                // Dropping AVET also drops any pending backfill. This matters
                // to an unqualified AVET scan, which must not be poisoned by
                // a no-longer-indexed attribute.
                false
            } else if !before {
                if prior_tail_history.contains(&attribute) {
                    true
                } else if let Some(had_history) = base_history.get(&attribute) {
                    *had_history
                } else {
                    let had_history = has_base_history(attribute)?;
                    base_history.insert(attribute, had_history);
                    had_history
                }
            } else {
                continue;
            };
            if unready.contains(&attribute) != needs_backfill {
                let unready = Arc::make_mut(&mut unready);
                if needs_backfill {
                    unready.insert(attribute);
                } else {
                    // Empty attributes can toggle physical AVET membership
                    // synchronously, matching recovered add-avet/add-unique.
                    unready.remove(&attribute);
                }
            }
        }
        prior_tail_history.extend(transaction.tx_data.iter().map(|datom| datom.attribute));
        metadata = endpoint;
    }

    Ok((metadata, unready))
}

fn retain_schema_working_set(current: &mut Vec<Datom>) {
    let installed = current
        .iter()
        .filter_map(|datom| {
            if datom.added
                && datom.entity == crate::DB_PART_DB
                && matches!(
                    u64::from(datom.attribute),
                    crate::DB_INSTALL_ATTRIBUTE
                        | crate::DB_ALTER_ATTRIBUTE
                        | crate::DB_INSTALL_PARTITION
                )
            {
                match &datom.value {
                    crate::Value::Ref(entity) => Some(*entity),
                    _ => None,
                }
            } else {
                None
            }
        })
        .collect::<BTreeSet<_>>();
    current.retain(|datom| {
        (datom.entity == crate::DB_PART_DB
            && matches!(
                u64::from(datom.attribute),
                crate::DB_INSTALL_ATTRIBUTE
                    | crate::DB_ALTER_ATTRIBUTE
                    | crate::DB_INSTALL_PARTITION
            ))
            || (installed.contains(&datom.entity) && schema_information_attribute(datom.attribute))
    });
}

fn schema_information_attribute(attribute: u32) -> bool {
    matches!(
        u64::from(attribute),
        crate::DB_IDENT
            | crate::DB_INSTALL_PARTITION
            | crate::DB_INSTALL_ATTRIBUTE
            | crate::DB_ALTER_ATTRIBUTE
            | crate::DB_VALUE_TYPE
            | crate::DB_CARDINALITY
            | crate::DB_UNIQUE
            | crate::DB_IS_COMPONENT
            | crate::DB_INDEX
            | crate::DB_NO_HISTORY
            | crate::DB_FULLTEXT
            | crate::DB_TUPLE_TYPE
            | crate::DB_TUPLE_TYPES
            | crate::DB_TUPLE_ATTRS
            | crate::DB_TUPLE_DISCONTINUED
            | crate::DB_ATTR_PREDS
    )
}

fn same_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.stored_eq(&right.value)
}

fn same_logical_eav(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.value.index_cmp(&right.value) == std::cmp::Ordering::Equal
}

fn ident_assertion_cmp(left: &Datom, right: &Datom) -> std::cmp::Ordering {
    left.tx
        .cmp(&right.tx)
        .then(left.entity.cmp(&right.entity))
        .then_with(|| left.value.stored_cmp(&right.value))
        .then(left.added.cmp(&right.added))
}

pub(crate) fn prepare_index_group<T: Send>(
    group: &[(bool, IndexOrder)],
    prepare: &(impl Fn((bool, IndexOrder)) -> Result<T, SemanticError> + Sync),
) -> Result<(Vec<T>, u64, u64), SemanticError> {
    if group.len() == 1 {
        return Ok((vec![prepare(group[0])?], 0, 0));
    }
    let active = std::sync::atomic::AtomicUsize::new(0);
    let peak = std::sync::atomic::AtomicUsize::new(0);
    let mut workers = 0;
    let prepared = std::thread::scope(|scope| {
        let mut pending = Vec::with_capacity(group.len());
        for &projection in group {
            let active = &active;
            let peak = &peak;
            let context = crate::OperationContext::current_or_process();
            let worker = std::thread::Builder::new()
                .name("atomic-index-prepare".into())
                .spawn_scoped(scope, move || {
                    let _scope = context.enter();
                    let count = active.fetch_add(1, Ordering::AcqRel) + 1;
                    peak.fetch_max(count, Ordering::Relaxed);
                    let result = prepare(projection);
                    active.fetch_sub(1, Ordering::AcqRel);
                    result
                });
            pending.push(match worker {
                Ok(worker) => {
                    workers += 1;
                    Ok(worker)
                }
                // A denied optional worker does not invalidate a legal build.
                Err(_) => Err(prepare(projection)),
            });
        }
        // Join every task, including after an error, before borrowed inputs or
        // build ownership can be released. No worker survives publication.
        let mut output = Vec::with_capacity(group.len());
        let mut failure = None;
        for task in pending {
            let result = match task {
                Ok(worker) => worker.join().unwrap_or_else(|_| {
                    Err(fault(
                        "index/preparation-panic",
                        "index edit preparation worker panicked",
                    ))
                }),
                Err(result) => result,
            };
            match result {
                Ok(value) => output.push(value),
                Err(error) => {
                    failure.get_or_insert(error);
                }
            }
        }
        failure.map_or(Ok(output), Err)
    })?;
    Ok((prepared, workers, peak.load(Ordering::Relaxed) as u64))
}

// Borrow the paired schema/datom changes and projection work without an owned staging wrapper.
#[allow(clippy::too_many_arguments)]
pub(crate) fn current_edits(
    order: IndexOrder,
    removals: &[Datom],
    insertions: &[Datom],
    base_schema: &crate::Schema,
    endpoint_schema: &crate::Schema,
    changed_avet: &[(u32, bool, bool)],
    avet_backfills: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
    avet_drops: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
) -> Result<TreeMergeEdits, SemanticError> {
    let changed = |attribute| {
        order == IndexOrder::Avet
            && changed_avet
                .iter()
                .any(|(candidate, _, _)| *candidate == attribute)
    };
    let mut edits = TreeMergeEdits::default();
    for datom in removals {
        if !changed(datom.attribute) && schema_index_member(base_schema, datom, order)? {
            edits.removals.push(datom.clone());
        }
    }
    for datom in insertions {
        if !changed(datom.attribute) && schema_index_member(endpoint_schema, datom, order)? {
            edits.insertions.push(datom.clone());
        }
    }
    if order == IndexOrder::Avet {
        for (current, _) in avet_backfills.values() {
            edits.insertions.extend(current.iter().cloned());
        }
        for (current, _) in avet_drops.values() {
            edits.projection_removals.extend(current.iter().cloned());
        }
    }
    Ok(edits)
}

pub(crate) fn history_edits(
    order: IndexOrder,
    recent: &RecentTier,
    changed_avet: &[(u32, bool, bool)],
    avet_backfills: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
    avet_drops: &BTreeMap<u32, (Vec<Datom>, Vec<Datom>)>,
) -> Result<TreeMergeEdits, SemanticError> {
    let changed = |attribute| {
        order == IndexOrder::Avet
            && changed_avet
                .iter()
                .any(|(candidate, _, _)| *candidate == attribute)
    };
    let projection = recent.projection();
    let mut edits = TreeMergeEdits {
        insertions: recent
            .datoms(order)
            .iter()
            .filter(|datom| !changed(datom.attribute))
            .cloned()
            .collect(),
        // Recovered `filter-nohist-pairs` runs after old and new segment data
        // are merged. Let `merge_tree` inspect every complete selected leaf
        // stream so incidental older durable pairs in rewritten segments are
        // eligible too; untouched leaves remain opaque and unchanged.
        no_history_attributes: projection
            .schema()
            .attributes()
            .filter(|attribute| attribute.no_history && !changed(attribute.id))
            .map(|attribute| attribute.id)
            .collect(),
        ..TreeMergeEdits::default()
    };
    if order == IndexOrder::Avet {
        for (_, history) in avet_backfills.values() {
            edits.insertions.extend(history.iter().cloned());
        }
        for (_, history) in avet_drops.values() {
            edits.projection_removals.extend(history.iter().cloned());
        }
    }
    Ok(edits)
}

pub(crate) fn canonicalize_merge_edits(edits: &mut TreeMergeEdits, order: IndexOrder) {
    sort_dedup_datoms(&mut edits.removals, order);
    sort_dedup_datoms(&mut edits.projection_removals, order);
    sort_dedup_datoms(&mut edits.insertions, order);
    edits
        .no_history_pairs
        .sort_by(|left, right| left.retraction.cmp_in(&right.retraction, order));
}

pub(crate) fn changed_avet_attributes(
    base: &crate::Schema,
    endpoint: &crate::Schema,
) -> Vec<(u32, bool, bool)> {
    let mut attributes = base
        .attributes()
        .chain(endpoint.attributes())
        .map(|attribute| attribute.id)
        .collect::<Vec<_>>();
    attributes.sort_unstable();
    attributes.dedup();
    attributes
        .into_iter()
        .filter_map(|attribute| {
            let before = effective_avet(base, attribute);
            let after = effective_avet(endpoint, attribute);
            (before != after).then_some((attribute, before, after))
        })
        .collect()
}

pub(crate) fn effective_avet(schema: &crate::Schema, attribute: u32) -> bool {
    schema
        .attribute(attribute)
        .is_ok_and(|attribute| attribute.indexed || attribute.unique.is_some())
}

pub(crate) fn schema_index_member(
    schema: &crate::Schema,
    datom: &Datom,
    order: IndexOrder,
) -> Result<bool, SemanticError> {
    match order {
        IndexOrder::Eavt | IndexOrder::Aevt => Ok(true),
        IndexOrder::Avet => {
            let attribute = schema.attribute(datom.attribute)?;
            Ok(attribute.indexed || attribute.unique.is_some())
        }
        IndexOrder::Vaet => {
            Ok(schema.attribute(datom.attribute)?.value_type == crate::ValueType::Ref)
        }
    }
}

pub(crate) fn sort_dedup_datoms(datoms: &mut Vec<Datom>, order: IndexOrder) {
    datoms.sort_by(|left, right| left.cmp_in(right, order));
    datoms.dedup_by(|right, left| same_stored_datom(left, right));
}

pub(crate) fn stored_eav_key(datom: &Datom) -> Result<Vec<u8>, SemanticError> {
    let value = crate::encoding::encode_canonical_value(&datom.value)?;
    let mut key = Vec::with_capacity(12 + value.len());
    key.extend_from_slice(&datom.entity.to_be_bytes());
    key.extend_from_slice(&datom.attribute.to_be_bytes());
    key.extend_from_slice(&value);
    Ok(key)
}

fn eav_bound(exemplar: &Datom, lower: bool) -> Datom {
    Datom {
        entity: exemplar.entity,
        attribute: exemplar.attribute,
        value: exemplar.value.clone(),
        tx: if lower { u64::MAX } else { 0 },
        added: lower,
    }
}

fn attribute_bounds(attribute: u32) -> Result<(Datom, Datom), SemanticError> {
    let end = attribute.checked_add(1).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::Unsupported,
            "index/attribute-range-overflow",
            "AVET attribute range cannot represent an exclusive upper bound",
        )
    })?;
    let bound = |attribute| Datom {
        entity: 0,
        attribute,
        value: crate::Value::Double(f64::NEG_INFINITY),
        tx: u64::MAX,
        added: true,
    };
    Ok((bound(attribute), bound(end)))
}

pub(crate) trait IndexNodeReader {
    fn load_node(&mut self, hash: Digest) -> Result<Option<Vec<u8>>, SemanticError>;
}

pub(crate) fn load_old_tree_node(
    store: &mut impl IndexNodeReader,
    cache: &mut TreeNodeSet,
    hash: Digest,
) -> Result<TreeNode, SemanticError> {
    if let Some(bytes) = cache.get(&hash) {
        return decode_tree_node(&hash, bytes);
    }
    let bytes = store.load_node(hash)?.ok_or_else(|| {
        fault(
            "index/missing-tree-node",
            "published tree path references missing immutable content",
        )
    })?;
    let node = decode_tree_node(&hash, &bytes)?;
    cache.insert_known(hash, bytes)?;
    Ok(node)
}

pub(crate) fn load_old_root(
    store: &mut impl IndexNodeReader,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    cache: &mut TreeNodeSet,
) -> Result<RootNode, SemanticError> {
    let TreeNode::Root(root) = load_old_tree_node(store, cache, descriptor.root_hash)? else {
        return Err(fault(
            "index/tree-root-kind",
            "tree descriptor resolves to a non-root node",
        ));
    };
    if root.order != descriptor.order
        || root.history != descriptor.history
        || root.count != descriptor.count
        || (root.count == 0) != root.directories.is_empty()
    {
        return Err(fault(
            "index/tree-root-content",
            "tree root disagrees with its manifest descriptor",
        ));
    }
    Ok(root)
}

pub(crate) fn load_old_directory(
    store: &mut impl IndexNodeReader,
    cache: &mut TreeNodeSet,
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
) -> Result<DirectoryNode, SemanticError> {
    let TreeNode::Directory(directory) = load_old_tree_node(store, cache, reference.hash)? else {
        return Err(fault(
            "index/tree-directory-kind",
            "root child resolves to a non-directory node",
        ));
    };
    validate_loaded_child_key(
        reference,
        directory.order,
        directory.history,
        directory.count,
        directory.leaves.first().map(|leaf| &leaf.key),
        order,
        history,
    )?;
    Ok(directory)
}

pub(crate) fn load_old_leaf(
    store: &mut impl IndexNodeReader,
    cache: &mut TreeNodeSet,
    reference: &ChildRef,
    order: IndexOrder,
    history: bool,
) -> Result<LeafSegment, SemanticError> {
    let TreeNode::Leaf(leaf) = load_old_tree_node(store, cache, reference.hash)? else {
        return Err(fault(
            "index/tree-leaf-kind",
            "directory child resolves to a non-leaf node",
        ));
    };
    let first = leaf.datom(0);
    validate_loaded_child_datom(
        reference,
        leaf.order,
        leaf.history,
        leaf.len() as u64,
        first.as_ref(),
        order,
        history,
    )?;
    Ok(leaf)
}

pub(crate) struct IndexEavLookup {
    root: RootNode,
    directory: Option<(usize, DirectoryNode)>,
    leaf: Option<(usize, usize, LeafSegment)>,
}

impl IndexEavLookup {
    pub(crate) fn new(
        store: &mut impl IndexNodeReader,
        descriptor: &crate::persistent_tree::TreeDescriptor,
        cache: &mut TreeNodeSet,
    ) -> Result<Self, SemanticError> {
        Ok(Self {
            root: load_old_root(store, descriptor, cache)?,
            directory: None,
            leaf: None,
        })
    }

    /// BigDecimal scale is ordered after T/op. Seek through the complete
    /// logical-comparator group until the exact stored representation appears.
    pub(crate) fn exact(
        &mut self,
        store: &mut impl IndexNodeReader,
        exemplar: &Datom,
        cache: &mut TreeNodeSet,
    ) -> Result<Option<Datom>, SemanticError> {
        let lower = eav_bound(exemplar, true);
        let mut candidate = self.seek(store, &lower, false, cache)?;
        while let Some(datom) = candidate {
            if !same_logical_eav(&datom, exemplar) {
                return Ok(None);
            }
            if same_eav(&datom, exemplar) {
                return Ok(Some(datom));
            }
            candidate = self.seek(store, &datom, true, cache)?;
        }
        Ok(None)
    }

    fn seek(
        &mut self,
        store: &mut impl IndexNodeReader,
        key: &Datom,
        strict: bool,
        cache: &mut TreeNodeSet,
    ) -> Result<Option<Datom>, SemanticError> {
        if self.root.directories.is_empty() {
            return Ok(None);
        }
        let order = self.root.order;
        let history = self.root.history;
        let first_directory = floor_tree_child(&self.root.directories, key, order);
        for directory_index in first_directory..self.root.directories.len() {
            if self
                .directory
                .as_ref()
                .is_none_or(|(index, _)| *index != directory_index)
            {
                let directory = load_old_directory(
                    store,
                    cache,
                    &self.root.directories[directory_index],
                    order,
                    history,
                )?;
                self.directory = Some((directory_index, directory));
            }
            let directory = &self.directory.as_ref().expect("loaded directory").1;
            let first_leaf = if directory_index == first_directory {
                floor_tree_child(&directory.leaves, key, order)
            } else {
                0
            };
            for leaf_index in first_leaf..directory.leaves.len() {
                if self.leaf.as_ref().is_none_or(|(directory, leaf, _)| {
                    (*directory, *leaf) != (directory_index, leaf_index)
                }) {
                    let leaf =
                        load_old_leaf(store, cache, &directory.leaves[leaf_index], order, history)?;
                    self.leaf = Some((directory_index, leaf_index, leaf));
                }
                let leaf = &self.leaf.as_ref().expect("loaded leaf").2;
                let mut index = if directory_index == first_directory && leaf_index == first_leaf {
                    leaf_lower_bound(leaf, key, order)
                } else {
                    0
                };
                while let Some(datom) = leaf.datom(index) {
                    if !strict || datom.cmp_in(key, order).is_gt() {
                        return Ok(Some(datom));
                    }
                    index += 1;
                }
            }
        }
        Ok(None)
    }
}

pub(crate) struct TreeStructuralChunk {
    pub(crate) datoms: Vec<Datom>,
    pub(crate) directory: u32,
    pub(crate) leaf: u32,
    pub(crate) slot: u32,
    pub(crate) complete: bool,
}

// Keep the durable resume coordinates and independent byte/datom limits explicit at this boundary.
#[allow(clippy::too_many_arguments)]
pub(crate) fn tree_attribute_chunk(
    store: &mut impl IndexNodeReader,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    attribute: u32,
    resume_directory: u32,
    resume_leaf: u32,
    resume_slot: u32,
    maximum_datoms: usize,
    maximum_bytes: u64,
    cache: &mut TreeNodeSet,
) -> Result<TreeStructuralChunk, SemanticError> {
    if !matches!(descriptor.order, IndexOrder::Aevt | IndexOrder::Avet) {
        return Err(SemanticError::incorrect(
            "index/invalid-avet-projection-source",
            "AVET projection chunks require an AEVT or AVET source",
        ));
    }
    if maximum_datoms == 0 || maximum_bytes == 0 {
        return Err(SemanticError::incorrect(
            "index/invalid-avet-projection-limit",
            "AVET projection chunk limits must be positive",
        ));
    }
    let (start, end) = attribute_bounds(attribute)?;
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(TreeStructuralChunk {
            datoms: Vec::new(),
            directory: 0,
            leaf: 0,
            slot: 0,
            complete: true,
        });
    }

    let fresh = resume_directory == 0 && resume_leaf == 0 && resume_slot == 0;
    let mut directory_index = if fresh {
        floor_tree_child(&root.directories, &start, descriptor.order)
    } else {
        usize::try_from(resume_directory).map_err(|_| {
            fault(
                "index/avet-projection-cursor",
                "AVET projection directory cursor exceeds usize",
            )
        })?
    };
    if directory_index >= root.directories.len() {
        return Err(fault(
            "index/avet-projection-cursor",
            "AVET projection directory cursor is outside its immutable source root",
        ));
    }
    let mut output = Vec::new();
    let mut retained_bytes = 0_u64;
    while directory_index < root.directories.len() {
        let reference = &root.directories[directory_index];
        if !reference.key.cmp_datom(&end, descriptor.order).is_lt() {
            return Ok(TreeStructuralChunk {
                datoms: output,
                directory: 0,
                leaf: 0,
                slot: 0,
                complete: true,
            });
        }
        let directory = load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )?;
        let mut leaf_index = if fresh
            && directory_index == floor_tree_child(&root.directories, &start, descriptor.order)
        {
            floor_tree_child(&directory.leaves, &start, descriptor.order)
        } else {
            usize::try_from(resume_leaf).map_err(|_| {
                fault(
                    "index/avet-projection-cursor",
                    "AVET projection leaf cursor exceeds usize",
                )
            })?
        };
        if !fresh && directory_index != usize::try_from(resume_directory).unwrap_or(usize::MAX) {
            leaf_index = 0;
        }
        while leaf_index < directory.leaves.len() {
            let leaf_ref = &directory.leaves[leaf_index];
            if !leaf_ref.key.cmp_datom(&end, descriptor.order).is_lt() {
                return Ok(TreeStructuralChunk {
                    datoms: output,
                    directory: 0,
                    leaf: 0,
                    slot: 0,
                    complete: true,
                });
            }
            let leaf = load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
            let mut slot = if fresh
                && directory_index == floor_tree_child(&root.directories, &start, descriptor.order)
                && leaf_index == floor_tree_child(&directory.leaves, &start, descriptor.order)
            {
                leaf_lower_bound(&leaf, &start, descriptor.order)
            } else if !fresh
                && directory_index == usize::try_from(resume_directory).unwrap_or(usize::MAX)
                && leaf_index == usize::try_from(resume_leaf).unwrap_or(usize::MAX)
            {
                usize::try_from(resume_slot).map_err(|_| {
                    fault(
                        "index/avet-projection-cursor",
                        "AVET projection slot cursor exceeds usize",
                    )
                })?
            } else {
                0
            };
            if slot > leaf.len() {
                return Err(fault(
                    "index/avet-projection-cursor",
                    "AVET projection slot cursor is outside its immutable source leaf",
                ));
            }
            while slot < leaf.len() {
                let datom = leaf.datom(slot).expect("validated leaf columns");
                if datom.cmp_in(&start, descriptor.order).is_lt() {
                    slot += 1;
                    continue;
                }
                if !datom.cmp_in(&end, descriptor.order).is_lt() {
                    return Ok(TreeStructuralChunk {
                        datoms: output,
                        directory: 0,
                        leaf: 0,
                        slot: 0,
                        complete: true,
                    });
                }
                let datom_bytes = datom.retained_bytes();
                if !output.is_empty()
                    && (output.len() >= maximum_datoms
                        || retained_bytes.saturating_add(datom_bytes) > maximum_bytes)
                {
                    return Ok(TreeStructuralChunk {
                        datoms: output,
                        directory: u32::try_from(directory_index).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection directory cursor exceeds u32",
                            )
                        })?,
                        leaf: u32::try_from(leaf_index).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection leaf cursor exceeds u32",
                            )
                        })?,
                        slot: u32::try_from(slot).map_err(|_| {
                            fault(
                                "index/avet-projection-cursor",
                                "AVET projection slot cursor exceeds u32",
                            )
                        })?,
                        complete: false,
                    });
                }
                retained_bytes = retained_bytes.saturating_add(datom_bytes);
                output.push(datom);
                slot += 1;
            }
            leaf_index += 1;
        }
        directory_index += 1;
    }
    Ok(TreeStructuralChunk {
        datoms: output,
        directory: 0,
        leaf: 0,
        slot: 0,
        complete: true,
    })
}

fn tree_datoms_with_prefix<F>(
    root: &RootNode,
    prefix: &IndexPrefix,
    load_node: &mut F,
) -> Result<Vec<Datom>, SemanticError>
where
    F: FnMut(Digest) -> Result<Arc<TreeNode>, SemanticError>,
{
    prefix.validate()?;
    let order = prefix.order();
    if root.order != order {
        return Err(fault(
            "tree/metadata-root-order",
            "metadata prefix was routed through the wrong persistent index",
        ));
    }
    if root.directories.is_empty() {
        return Ok(Vec::new());
    }

    let mut output = Vec::new();
    let first_directory = prefix_start_child(&root.directories, prefix);
    for directory_ref in root.directories.iter().skip(first_directory) {
        if directory_ref.key.cmp_prefix(prefix).is_gt() {
            break;
        }
        let loaded_directory = load_node(directory_ref.hash)?;
        let TreeNode::Directory(directory) = loaded_directory.as_ref() else {
            return Err(fault(
                "tree/metadata-directory-kind",
                "metadata root child is not a directory",
            ));
        };
        validate_loaded_child_key(
            directory_ref,
            directory.order,
            directory.history,
            directory.count,
            directory.leaves.first().map(|child| &child.key),
            root.order,
            root.history,
        )?;
        let first_leaf = prefix_start_child(&directory.leaves, prefix);
        for leaf_ref in directory.leaves.iter().skip(first_leaf) {
            if leaf_ref.key.cmp_prefix(prefix).is_gt() {
                break;
            }
            let loaded_leaf = load_node(leaf_ref.hash)?;
            let TreeNode::Leaf(leaf) = loaded_leaf.as_ref() else {
                return Err(fault(
                    "tree/metadata-leaf-kind",
                    "metadata directory child is not a leaf",
                ));
            };
            let first = leaf.datom(0);
            validate_loaded_child_datom(
                leaf_ref,
                leaf.order,
                leaf.history,
                leaf.len() as u64,
                first.as_ref(),
                root.order,
                root.history,
            )?;
            for index in 0..leaf.len() {
                let datom = leaf.datom(index).expect("validated metadata leaf columns");
                match crate::index::compare_prefix(&datom, prefix) {
                    std::cmp::Ordering::Less => {}
                    std::cmp::Ordering::Equal => output.push(datom),
                    std::cmp::Ordering::Greater => return Ok(output),
                }
            }
        }
    }
    Ok(output)
}

pub(crate) fn derive_metadata_from_roots<F>(
    roots: &BTreeMap<(bool, u8), Arc<RootNode>>,
    mut load_node: F,
) -> Result<MetadataProjection, SemanticError>
where
    F: FnMut(Digest) -> Result<Arc<TreeNode>, SemanticError>,
{
    let root = |history: bool, order: IndexOrder| {
        roots
            .get(&(history, order_tag(order)))
            .map(Arc::as_ref)
            .ok_or_else(|| {
                fault(
                    "tree/missing-metadata-root",
                    "persistent publication omits an index required for metadata reconstruction",
                )
            })
    };
    let history_aevt = root(true, IndexOrder::Aevt)?;
    let current_aevt = root(false, IndexOrder::Aevt)?;
    let current_eavt = root(false, IndexOrder::Eavt)?;

    // Recovered `ident-setting-datoms` reads assertions from AEVT and folds
    // them in transaction order. Atomic's history tree contains the complete
    // retained stream, and :db/ident is an immutable built-in without
    // :db/noHistory, so aliases and later name repurposing survive bases.
    let ident_prefix = IndexPrefix::Aevt {
        attribute: crate::DB_IDENT as u32,
        entity: None,
        value: None,
    };
    let ident_assertions = tree_datoms_with_prefix(history_aevt, &ident_prefix, &mut load_node)?
        .into_iter()
        .filter(|datom| datom.added)
        .collect::<Vec<_>>();
    let idents = IdentIndex::derive(ident_assertions.iter(), crate::DB_IDENT as u32)?;

    // As recovered `run-hooks` does, discover installed attributes from the
    // two current hook attributes, then read each complete current schema
    // entity through EAVT. General entity idents and unrelated open-entity
    // facts never become a second schema projection.
    let mut schema_current = Vec::new();
    for attribute in [
        crate::DB_INSTALL_ATTRIBUTE,
        crate::DB_ALTER_ATTRIBUTE,
        crate::DB_INSTALL_PARTITION,
    ] {
        let prefix = IndexPrefix::Aevt {
            attribute: attribute as u32,
            entity: None,
            value: None,
        };
        schema_current.extend(tree_datoms_with_prefix(
            current_aevt,
            &prefix,
            &mut load_node,
        )?);
    }
    let mut installed = BTreeSet::new();
    for datom in &schema_current {
        if !datom.added || datom.entity != crate::DB_PART_DB {
            continue;
        }
        let crate::Value::Ref(entity) = datom.value else {
            return Err(fault(
                "tree/invalid-schema-hook",
                "current schema hook does not contain an attribute entity ref",
            ));
        };
        installed.insert(entity);
    }
    for entity in installed {
        let prefix = IndexPrefix::Eavt {
            entity,
            attribute: None,
            value: None,
        };
        schema_current.extend(
            tree_datoms_with_prefix(current_eavt, &prefix, &mut load_node)?
                .into_iter()
                .filter(|datom| schema_information_attribute(datom.attribute)),
        );
    }
    MetadataProjection::from_current_and_idents(schema_current, idents)
}

pub(crate) fn merge_index_tree(
    store: &mut impl IndexNodeReader,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    preloaded: &TreeNodeSet,
    edits: &TreeMergeEdits,
    config: &TreeConfig,
) -> Result<crate::persistent_tree::TreeMerge, SemanticError> {
    merge_tree_with_boundary_loader(descriptor, preloaded, edits, config, &mut |hash| {
        store.load_node(*hash)?.ok_or_else(|| {
            fault(
                "index/missing-tree-node",
                "published tree boundary references missing immutable content",
            )
        })
    })
}

pub(crate) fn preload_merge_paths(
    store: &mut impl IndexNodeReader,
    descriptor: &crate::persistent_tree::TreeDescriptor,
    edits: &TreeMergeEdits,
    cache: &mut TreeNodeSet,
) -> Result<(), SemanticError> {
    let root = load_old_root(store, descriptor, cache)?;
    if root.directories.is_empty() {
        return Ok(());
    }
    let mut points = edits
        .removals
        .iter()
        .chain(&edits.projection_removals)
        .chain(&edits.insertions)
        .cloned()
        .collect::<Vec<_>>();
    for pair in &edits.no_history_pairs {
        points.push(pair.retraction.clone());
        points.push(pair.assertion.clone());
    }
    sort_dedup_datoms(&mut points, descriptor.order);
    let selected = select_merge_leaves(&root, &points, |reference| {
        load_old_directory(
            store,
            cache,
            reference,
            descriptor.order,
            descriptor.history,
        )
    })?;
    for (directory_index, leaf_index) in selected.leaves {
        let leaf_ref = &selected.directories[&directory_index].leaves[leaf_index];
        load_old_leaf(store, cache, leaf_ref, descriptor.order, descriptor.history)?;
    }
    Ok(())
}

struct SelectedMergeLeaves {
    directories: BTreeMap<usize, DirectoryNode>,
    leaves: BTreeSet<(usize, usize)>,
}

fn select_merge_leaves(
    root: &RootNode,
    points: &[Datom],
    mut load_directory: impl FnMut(&ChildRef) -> Result<DirectoryNode, SemanticError>,
) -> Result<SelectedMergeLeaves, SemanticError> {
    let mut directories = BTreeMap::new();
    let mut leaves = BTreeSet::new();
    for point in points {
        let directory_index = floor_tree_child(&root.directories, point, root.order);
        let directory = match directories.entry(directory_index) {
            std::collections::btree_map::Entry::Occupied(entry) => entry.into_mut(),
            std::collections::btree_map::Entry::Vacant(entry) => {
                entry.insert(load_directory(&root.directories[directory_index])?)
            }
        };
        // The affected directory itself is re-encoded even when only one
        // interior leaf changes. Root separator repair authenticates its new
        // first and last datoms, which may live in otherwise untouched leaves.
        leaves.insert((directory_index, 0));
        leaves.insert((directory_index, directory.leaves.len() - 1));
        let leaf_index = floor_tree_child(&directory.leaves, point, root.order);
        let first_leaf = leaf_index.saturating_sub(1);
        let last_leaf = leaf_index
            .saturating_add(1)
            .min(directory.leaves.len().saturating_sub(1));
        for leaf_index in first_leaf..=last_leaf {
            leaves.insert((directory_index, leaf_index));
        }

        // Rewriting a leaf or directory can change the sparse separator on
        // either side even though those neighboring children are themselves
        // reused. `merge_tree` authenticates those boundary datoms when it
        // repairs routing keys, so preload exactly those adjacent paths too.
        if let Some(previous_index) = directory_index.checked_sub(1) {
            let previous = match directories.entry(previous_index) {
                std::collections::btree_map::Entry::Occupied(entry) => entry.into_mut(),
                std::collections::btree_map::Entry::Vacant(entry) => {
                    entry.insert(load_directory(&root.directories[previous_index])?)
                }
            };
            leaves.insert((previous_index, previous.leaves.len() - 1));
        }
        let next_index = directory_index + 1;
        if let Some(next_ref) = root.directories.get(next_index) {
            if let std::collections::btree_map::Entry::Vacant(entry) = directories.entry(next_index)
            {
                entry.insert(load_directory(next_ref)?);
            }
            leaves.insert((next_index, 0));
        }
    }
    Ok(SelectedMergeLeaves {
        directories,
        leaves,
    })
}

fn same_stored_datom(left: &Datom, right: &Datom) -> bool {
    left.entity == right.entity
        && left.attribute == right.attribute
        && left.tx == right.tx
        && left.added == right.added
        && left.value.stored_eq(&right.value)
}

fn order_tag(order: IndexOrder) -> u8 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
fn all_index_orders() -> [IndexOrder; 4] {
    [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ]
}

#[cfg(test)]
#[path = "peer_merge_preload_tests.rs"]
mod merge_preload_tests;
#[cfg(test)]
#[path = "peer_readiness_tests.rs"]
mod readiness_tests;
#[cfg(test)]
mod tests {
    use super::*;
    use crate::tree_cursor::{
        TreeBoundary, boundary_floor_child, boundary_start_child, leaf_boundary_lower_bound,
        leaf_reverse_upper_bound,
    };
    use crate::{
        Attribute, Cardinality, EntityRef, IndexBoundary, IndexComponents, Keyword, Schema, TxOp,
        TxValue, Value, ValueType,
    };
    #[test]
    fn bulk_merge_preloads_each_directory_and_leaf_coordinate_once() {
        let datoms = (0..512)
            .map(|index| Datom {
                entity: crate::make_eid(crate::USER_PARTITION, index + 1).unwrap(),
                attribute: 1_000,
                value: Value::String("x".repeat(128)),
                tx: crate::t_to_tx(1).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        let config = TreeConfig {
            max_leaf_datoms: 16,
            max_leaves_per_directory: 4,
            ..TreeConfig::default()
        };
        let built = build_tree(IndexOrder::Eavt, true, datoms.clone(), &config).unwrap();
        let TreeNode::Root(root) = decode_tree_node(
            &built.descriptor.root_hash,
            built.nodes.get(&built.descriptor.root_hash).unwrap(),
        )
        .unwrap() else {
            panic!("root expected")
        };
        let mut directory_loads = BTreeMap::<Digest, usize>::new();
        let selected = select_merge_leaves(&root, &datoms, |reference| {
            *directory_loads.entry(reference.hash).or_default() += 1;
            let TreeNode::Directory(directory) =
                decode_tree_node(&reference.hash, built.nodes.get(&reference.hash).unwrap())?
            else {
                panic!("directory expected")
            };
            Ok(directory)
        })
        .unwrap();
        assert_eq!(directory_loads.len(), root.directories.len());
        assert!(directory_loads.values().all(|loads| *loads == 1));
        assert_eq!(
            selected.leaves.len(),
            selected
                .directories
                .values()
                .map(|directory| directory.leaves.len())
                .sum::<usize>()
        );
        assert!(selected.leaves.len() < datoms.len() / 8);
    }

    #[test]
    fn reverse_full_t_boundary_routes_after_all_operation_and_stored_value_ties() {
        use bigdecimal::BigDecimal;
        use std::str::FromStr;

        let entity = crate::make_eid(crate::USER_PARTITION, 1).unwrap();
        let mut datoms = (1..=40)
            .map(|t| Datom {
                entity,
                attribute: 1_000,
                value: Value::BigDec(BigDecimal::from_str("1").unwrap()),
                tx: crate::t_to_tx(t).unwrap(),
                added: true,
            })
            .collect::<Vec<_>>();
        datoms.extend([
            Datom {
                value: Value::BigDec(BigDecimal::from_str("1.00").unwrap()),
                tx: crate::t_to_tx(20).unwrap(),
                ..datoms[0].clone()
            },
            Datom {
                value: Value::BigDec(BigDecimal::from_str("1.000").unwrap()),
                tx: crate::t_to_tx(20).unwrap(),
                added: false,
                ..datoms[0].clone()
            },
        ]);
        datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
        let config = TreeConfig {
            max_leaf_datoms: 2,
            target_leaf_bytes: 512,
            max_leaf_bytes: 2_048,
            max_leaves_per_directory: 2,
            max_directory_bytes: 2_048,
            max_directories_per_root: 64,
            max_root_bytes: 64 * 1_024,
        };
        let build = build_tree(IndexOrder::Eavt, true, datoms.clone(), &config).unwrap();
        let root = match decode_tree_node(
            &build.descriptor.root_hash,
            build.nodes.get(&build.descriptor.root_hash).unwrap(),
        )
        .unwrap()
        {
            TreeNode::Root(root) => root,
            _ => panic!("tree descriptor must address a root"),
        };
        assert!(root.directories.len() > 1);

        let normalized = IndexBoundary::Eavt(IndexComponents::Four(
            entity,
            1_000,
            Value::BigDec(BigDecimal::from_str("1.0").unwrap()),
            crate::IndexTransaction::T(20),
        ))
        .normalized()
        .unwrap();
        let boundary = TreeBoundary::new(normalized);
        let expected_lower =
            datoms.partition_point(|datom| boundary.normalized.compare_datom(datom).is_lt());
        let expected_upper =
            datoms.partition_point(|datom| !boundary.normalized.compare_datom(datom).is_gt());
        assert_eq!(
            datoms[expected_lower..expected_upper]
                .iter()
                .filter(|datom| datom.tx == crate::t_to_tx(20).unwrap())
                .count(),
            3,
            "the virtual T lower bound must precede operation and scale ties"
        );
        assert_eq!(
            datoms[..expected_upper]
                .iter()
                .filter(|datom| datom.tx == crate::t_to_tx(20).unwrap())
                .count(),
            3,
            "the virtual T upper bound must include assertion/retraction and scale ties"
        );

        let directory_index = boundary_floor_child(&root.directories, &boundary).unwrap();
        let directory = match decode_tree_node(
            &root.directories[directory_index].hash,
            build
                .nodes
                .get(&root.directories[directory_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Directory(directory) => directory,
            _ => panic!("root child must be a directory"),
        };
        let leaf_index = boundary_floor_child(&directory.leaves, &boundary).unwrap();
        let leaf = match decode_tree_node(
            &directory.leaves[leaf_index].hash,
            build.nodes.get(&directory.leaves[leaf_index].hash).unwrap(),
        )
        .unwrap()
        {
            TreeNode::Leaf(leaf) => leaf,
            _ => panic!("directory child must be a leaf"),
        };
        let local_upper = leaf_reverse_upper_bound(&leaf, &boundary);
        let global_upper = root.directories[..directory_index]
            .iter()
            .map(|child| child.count as usize)
            .sum::<usize>()
            + directory.leaves[..leaf_index]
                .iter()
                .map(|child| child.count as usize)
                .sum::<usize>()
            + local_upper;
        assert_eq!(global_upper, expected_upper);

        let forward_directory_index = boundary_start_child(&root.directories, &boundary);
        let forward_directory = match decode_tree_node(
            &root.directories[forward_directory_index].hash,
            build
                .nodes
                .get(&root.directories[forward_directory_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Directory(directory) => directory,
            _ => panic!("root child must be a directory"),
        };
        let forward_leaf_index = boundary_start_child(&forward_directory.leaves, &boundary);
        let forward_leaf = match decode_tree_node(
            &forward_directory.leaves[forward_leaf_index].hash,
            build
                .nodes
                .get(&forward_directory.leaves[forward_leaf_index].hash)
                .unwrap(),
        )
        .unwrap()
        {
            TreeNode::Leaf(leaf) => leaf,
            _ => panic!("directory child must be a leaf"),
        };
        let global_lower = root.directories[..forward_directory_index]
            .iter()
            .map(|child| child.count as usize)
            .sum::<usize>()
            + forward_directory.leaves[..forward_leaf_index]
                .iter()
                .map(|child| child.count as usize)
                .sum::<usize>()
            + leaf_boundary_lower_bound(&forward_leaf, &boundary);
        assert_eq!(global_lower, expected_lower);
    }

    #[test]
    fn metadata_residency_bookkeeping_reuses_immutable_totals() {
        for width in [16_u32, 128, 512] {
            let mut schema = Schema::new();
            for id in 1_000..1_000 + width {
                schema
                    .install(Attribute::new(
                        id,
                        Keyword::new("metadata-cost", format!("attribute-{id}")),
                        ValueType::Long,
                        Cardinality::One,
                    ))
                    .unwrap();
            }
            let database = Database::new(schema).unwrap();
            let metadata = MetadataProjection::from_database(&database).unwrap();
            let expected = ResidentMetadataStats {
                schema_attributes: metadata.schema.attributes().count(),
                schema_information_datoms: metadata.schema_current.len(),
                schema_estimated_bytes: metadata.schema.estimated_retained_bytes()
                    + metadata
                        .schema_current
                        .iter()
                        .map(Datom::retained_bytes)
                        .sum::<u64>(),
                ident_names: metadata.idents.name_count(),
                ident_entities: metadata.idents.entity_count(),
                ident_estimated_bytes: metadata.idents.estimated_retained_bytes(),
            };
            let assessed = database
                .with(
                    &[TxOp::Add {
                        entity: EntityRef::Temp("item".into()),
                        attribute: 1_000,
                        value: Value::Long(1).into(),
                    }],
                    10,
                )
                .unwrap();
            let transaction = DurableTransaction {
                database_id: "metadata-cost".into(),
                basis_t: assessed.db_after.basis_t(),
                previous_hash: [7; 32],
                eidx_frontier: assessed.db_after.eidx_frontier(),
                tempids: assessed.tempids,
                tx_data: assessed.tx_data,
            };
            METADATA_STAT_COMPUTATIONS.with(|count| count.set(0));
            let started = Instant::now();
            for _ in 0..1_000 {
                let successor = metadata.apply(std::slice::from_ref(&transaction)).unwrap();
                assert_eq!(successor.resident_stats(), expected);
                assert!(Arc::ptr_eq(&successor.schema, &metadata.schema));
                assert_eq!(successor.schema, metadata.schema);
                drop(successor);
            }
            let elapsed = started.elapsed();
            assert_eq!(
                METADATA_STAT_COMPUTATIONS.with(|count| count.get()),
                0,
                "ordinary metadata apply/statistics/drop must not recompute full schema totals"
            );
            let named = database
                .with(
                    &[TxOp::Add {
                        entity: EntityRef::Temp("named".into()),
                        attribute: crate::DB_IDENT as u32,
                        value: Value::Keyword(Keyword::new("status", "pending")).into(),
                    }],
                    10,
                )
                .unwrap();
            let named_transaction = DurableTransaction {
                database_id: "metadata-cost".into(),
                basis_t: named.db_after.basis_t(),
                previous_hash: [7; 32],
                eidx_frontier: named.db_after.eidx_frontier(),
                tempids: named.tempids.clone(),
                tx_data: named.tx_data.clone(),
            };
            let named_metadata = metadata.apply(&[named_transaction]).unwrap();
            assert!(Arc::ptr_eq(&named_metadata.schema, &metadata.schema));
            assert!(Arc::ptr_eq(
                &named_metadata.schema_current,
                &metadata.schema_current
            ));
            assert_eq!(named_metadata.schema, metadata.schema);
            assert_eq!(
                named_metadata
                    .idents
                    .resolve(&Keyword::new("status", "pending")),
                Some(named.tempids["named"])
            );
            assert_eq!(
                named_metadata.resident_stats().schema_estimated_bytes,
                expected.schema_estimated_bytes
            );
            assert_eq!(
                named_metadata.resident_stats().ident_names,
                expected.ident_names + 1
            );
            assert_eq!(METADATA_STAT_COMPUTATIONS.with(|count| count.get()), 0);
            // Identity maps really changed. Their reconstruction may cost work;
            // compare all new totals to a fresh independent projection outside
            // the measured unchanged-metadata path.
            let named_reference = MetadataProjection::from_database(&named.db_after).unwrap();
            assert_eq!(
                named_metadata.resident_stats(),
                named_reference.resident_stats()
            );
            assert_eq!(named_metadata.schema, named_reference.schema);
            METADATA_STAT_COMPUTATIONS.with(|count| count.set(0));
            // A real schema change rebuilds the projection and its totals once;
            // retained snapshots keep their independently checked old totals.
            let installed = database
                .with(
                    &[TxOp::InstallAttribute(Attribute::new(
                        1_000 + width,
                        Keyword::new("metadata-cost", "new"),
                        ValueType::String,
                        Cardinality::One,
                    ))],
                    11,
                )
                .unwrap();
            let schema_transaction = DurableTransaction {
                database_id: "metadata-cost".into(),
                basis_t: installed.db_after.basis_t(),
                previous_hash: [7; 32],
                eidx_frontier: installed.db_after.eidx_frontier(),
                tempids: installed.tempids,
                tx_data: installed.tx_data,
            };
            let changed = metadata.apply(&[schema_transaction]).unwrap();
            assert_eq!(
                changed.resident_stats().schema_attributes,
                expected.schema_attributes + 1
            );
            assert!(
                changed.resident_stats().schema_estimated_bytes > expected.schema_estimated_bytes
            );
            assert_eq!(metadata.resident_stats(), expected);
            assert_eq!(METADATA_STAT_COMPUTATIONS.with(|count| count.get()), 1);
            assert_ne!(changed.schema, metadata.schema);
            eprintln!(
                "METADATA_BOOKKEEPING width={width} apply_stats_drop=1000 recomputations=0 elapsed_us={}",
                elapsed.as_micros()
            );
        }
    }

    #[test]
    fn ordinary_wide_schema_metadata_transition_reuses_the_projection() {
        let mut schema = Schema::new();
        for attribute in 1_000..1_128 {
            schema
                .install(Attribute::new(
                    attribute,
                    Keyword::new("wide", format!("attribute-{attribute}")),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
        }
        let database = Database::new(schema).unwrap();
        let assessed = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("entity".into()),
                    attribute: 1_000,
                    value: TxValue::Scalar(Value::Long(1)),
                }],
                10,
            )
            .unwrap();
        let metadata = MetadataProjection::from_database(&database).unwrap();
        let transaction = DurableTransaction {
            database_id: "wide-schema".into(),
            basis_t: assessed.db_after.basis_t(),
            previous_hash: [7; 32],
            eidx_frontier: assessed.db_after.eidx_frontier(),
            tempids: assessed.tempids,
            tx_data: assessed.tx_data,
        };
        let (endpoint, unready) = apply_metadata_and_avet_readiness(
            &metadata,
            &Arc::new(BTreeSet::new()),
            &[transaction],
            |_| panic!("ordinary data must not perform an AVET history probe"),
        )
        .unwrap();
        assert!(Arc::ptr_eq(&metadata.schema, &endpoint.schema));
        assert!(Arc::ptr_eq(&metadata.idents, &endpoint.idents));
        assert!(Arc::ptr_eq(
            &metadata.schema_current,
            &endpoint.schema_current
        ));
        assert!(unready.is_empty());
        let resident = endpoint.resident_stats();
        assert_eq!(
            resident.schema_attributes,
            database.schema().attributes().count()
        );
        assert!(resident.schema_information_datoms >= 128);
        assert!(resident.schema_estimated_bytes > 0);
        assert!(resident.ident_names >= 128);
        assert!(resident.ident_entities >= 128);
        assert!(resident.ident_estimated_bytes > 0);
    }
}
