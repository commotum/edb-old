//! Direct backup reads use the same immutable tree format as online peers.
//! Capture publishes these derived roots before the discoverable backup point.
use super::*;
use crate::peer::{ExactEndpoint, TieredSnapshot};
use crate::persistent_tree::{TreeConfig, TreeDescriptor, TreeMergeEdits};
use crate::reserved_allocation::ReservedAllocation;
use crate::{Datom, IndexOrder, IndexPrefix, ManifestTree, Value};

#[derive(Clone, Debug)]
pub(crate) struct BackupReadMetadata {
    pub(crate) point: BackupPoint,
    pub(crate) manifest: PersistentTreeManifest,
    pub(crate) log_tree: TreeDescriptor,
    pub(crate) genesis_hash: Digest,
    pub(crate) reserved_allocation: Option<ReservedAllocation>,
    pub(crate) open_object_reads: u64,
    pub(crate) open_object_bytes: u64,
}

pub(super) fn capture_exact_read_tree(
    connection: &PostgresConnectionConfig,
    database_id: &str,
    backup: &Manifest,
    source_hash: Digest,
    captured: Option<TreeBackup>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<(TreeBackup, Option<ReservedAllocation>), SemanticError> {
    let frontier = if backup.basis == 0 {
        Database::from_genesis(decode_genesis(&read_object(
            publisher.directory,
            backup.genesis_hash,
        )?)?)?
        .eidx_frontier()
    } else {
        decode_backup_membership(
            &read_object(publisher.directory, backup.head_transaction_hash)?,
            backup.log_generation,
        )?
        .eidx_frontier
    };
    let (snapshot, _) = TieredSnapshot::open_exact_configured(
        connection,
        database_id,
        ExactEndpoint {
            generation: backup.log_generation,
            basis_t: backup.basis,
            tx_hash: source_hash,
            state_hash: backup.head_state_hash,
            eidx_frontier: frontier,
        },
        None,
        64,
        16 * 1024 * 1024,
        crate::recent::RecentLimits::default(),
    )?;
    let reserved = snapshot.reserved_allocation()?;
    if let Some(tree) = captured {
        let manifest =
            PersistentTreeManifest::decode(&read_object(publisher.directory, tree.manifest_hash)?)?;
        if manifest.basis_t == backup.basis {
            return Ok((tree, reserved));
        }
    }
    // When indexing lags, build the exact read projection at capture time.
    // This is a streaming full-index pass, not a hidden full replay at open.
    // Encoded nodes are emitted promptly; subsequent points reuse their hashes.
    let mut trees = Vec::with_capacity(8);
    for history in [false, true] {
        let db = if history {
            snapshot.database_value().history()
        } else {
            snapshot.database_value()
        };
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            let cursor = db.scan_cursor(order)?;
            let build = persistent_tree::build_tree_with_sink(
                order,
                history,
                cursor,
                &TreeConfig::default(),
                &mut |hash, bytes| publisher.publish(hash, bytes),
            )?;
            trees.push(ManifestTree {
                descriptor: build.descriptor,
                root_bytes: build.stats.root_bytes,
            });
        }
    }
    // Manifest tree order is canonical by history then index order.
    let pending_avet = snapshot
        .database_value()
        .schema()
        .attributes()
        .filter(|attribute| {
            (attribute.indexed || attribute.unique.is_some()) && !snapshot.avet_ready(attribute.id)
        })
        .map(|attribute| crate::AvetProjectionWork::new(attribute.id, true))
        .collect::<Vec<_>>();
    let manifest = PersistentTreeManifest {
        database_id: backup.lineage_id.clone(),
        publication_revision: 1,
        basis_t: backup.basis,
        index_basis_t: if pending_avet.is_empty() {
            backup.basis
        } else {
            0
        },
        tx_hash: backup.head_transaction_hash,
        state_hash: backup.head_state_hash,
        excision_generation: backup.log_generation,
        eidx_frontier: frontier,
        trees,
        pending_avet,
    };
    let bytes = manifest.encode()?;
    let hash = sha256(&bytes);
    publisher.publish(hash, &bytes)?;
    Ok((
        TreeBackup {
            manifest_hash: hash,
            legacy_node_hashes: None,
        },
        reserved,
    ))
}

pub(super) fn capture_read_log_index(
    parent: Option<Digest>,
    start: u64,
    hashes: &[Digest],
    reserved: Option<ReservedAllocation>,
    publisher: &mut ObjectPublisher<'_>,
) -> Result<Digest, SemanticError> {
    let datoms = hashes
        .iter()
        .enumerate()
        .map(|(offset, hash)| log_datom(start + offset as u64, *hash));
    let descriptor = if let Some(parent) = parent {
        let (old, _) = decode_log_index(&read_object(publisher.directory, parent)?)?;
        let edits = TreeMergeEdits {
            insertions: datoms.collect(),
            ..Default::default()
        };
        let merged = persistent_tree::merge_tree_with_boundary_loader(
            &old,
            &persistent_tree::TreeNodeSet::default(),
            &edits,
            &TreeConfig::default(),
            &mut |hash| read_object(publisher.directory, *hash),
        )?;
        for (hash, bytes) in merged.new_nodes.iter() {
            publisher.publish(*hash, bytes)?;
        }
        merged.descriptor
    } else {
        persistent_tree::build_tree_with_sink(
            IndexOrder::Eavt,
            false,
            datoms.map(Ok),
            &TreeConfig::default(),
            &mut |hash, bytes| publisher.publish(hash, bytes),
        )?
        .descriptor
    };
    let bytes = encode_log_index(
        &descriptor,
        reserved.map_or(u64::MAX, ReservedAllocation::frontier),
    );
    let hash = sha256(&bytes);
    publisher.publish(hash, &bytes)?;
    Ok(hash)
}

fn log_datom(t: u64, hash: Digest) -> Datom {
    Datom {
        entity: t,
        attribute: 0,
        value: Value::Bytes(hash.to_vec()),
        tx: crate::t_to_tx(t).expect("captured transaction t is valid"),
        added: true,
    }
}

fn encode_log_index(tree: &TreeDescriptor, reserved: u64) -> Vec<u8> {
    let mut bytes = b"ATLX\x00\x01".to_vec();
    bytes.extend_from_slice(&tree.root_hash);
    bytes.extend_from_slice(&tree.count.to_be_bytes());
    bytes.extend_from_slice(&tree.first_hash.unwrap_or([0; 32]));
    bytes.extend_from_slice(&tree.last_hash.unwrap_or([0; 32]));
    bytes.extend_from_slice(&reserved.to_be_bytes());
    bytes
}

fn decode_log_index(bytes: &[u8]) -> Result<(TreeDescriptor, u64), SemanticError> {
    if bytes.len() != 118 || &bytes[..6] != b"ATLX\x00\x01" {
        return Err(fault(
            "backup/log-index-format",
            "unsupported or malformed offline log index",
        ));
    }
    let count = u64::from_be_bytes(bytes[38..46].try_into().unwrap());
    let tree = TreeDescriptor {
        root_hash: bytes[6..38].try_into().unwrap(),
        order: IndexOrder::Eavt,
        history: false,
        count,
        first_hash: (count > 0).then(|| bytes[46..78].try_into().unwrap()),
        last_hash: (count > 0).then(|| bytes[78..110].try_into().unwrap()),
    };
    let reserved = u64::from_be_bytes(bytes[110..118].try_into().unwrap());
    if encode_log_index(&tree, reserved) != bytes {
        return Err(fault(
            "backup/log-index-format",
            "noncanonical offline log index",
        ));
    }
    Ok((tree, reserved))
}

pub(crate) fn open_read_point(
    directory: &Path,
    requested: Option<&BackupPoint>,
) -> Result<BackupReadMetadata, SemanticError> {
    validate_backup_directory(directory, true)?;
    let point = match requested {
        Some(point) => point.clone(),
        None => PortableBackup::list_backup_points(directory)?
            .pop()
            .ok_or_else(|| {
                fault(
                    "backup/no-points",
                    "backup repository contains no published points",
                )
            })?,
    };
    let (backup, hash) = load_manifest_generation(directory, point.basis_t, point.log_generation)?;
    if hash != point.manifest_hash || backup.lineage_id != point.lineage_id {
        return Err(fault(
            "backup/read-point-binding",
            "requested backup identity differs from published root",
        ));
    }
    verify_claim(directory, &backup)?;
    let tree = backup.tree.as_ref().ok_or_else(|| {
        fault(
            "backup/read-index-missing",
            "backup lacks an exact read index; create a current-version backup",
        )
    })?;
    let tree_bytes = read_object(directory, tree.manifest_hash)?;
    let manifest = PersistentTreeManifest::decode(&tree_bytes)?;
    if manifest.database_id != backup.lineage_id
        || manifest.excision_generation != backup.log_generation
        || manifest.basis_t != backup.basis
        || manifest.tx_hash != backup.head_transaction_hash
        || manifest.state_hash != backup.head_state_hash
        || manifest.publication_revision != 1
    {
        return Err(fault(
            "backup/read-tree-binding",
            "read index differs from published backup coordinate",
        ));
    }
    let log_hash = backup.read_log_index.ok_or_else(|| {
        fault(
            "backup/read-log-index-missing",
            "backup lacks a log read index; create a current-version backup",
        )
    })?;
    let log_bytes = read_object(directory, log_hash)?;
    let (log_tree, reserved) = decode_log_index(&log_bytes)?;
    if log_tree.count != backup.basis {
        return Err(fault(
            "backup/read-log-count",
            "log index count differs from backup basis",
        ));
    }
    let reserved_allocation = if reserved == u64::MAX {
        None
    } else {
        Some(ReservedAllocation::from_frontier(
            reserved,
            manifest.eidx_frontier,
        )?)
    };
    Ok(BackupReadMetadata {
        point,
        manifest,
        log_tree,
        genesis_hash: backup.genesis_hash,
        reserved_allocation,
        open_object_reads: 2,
        open_object_bytes: (tree_bytes.len() + log_bytes.len()) as u64,
    })
}

fn log_hash_at(
    metadata: &BackupReadMetadata,
    t: u64,
    load: &mut dyn FnMut(Digest) -> Result<Arc<persistent_tree::TreeNode>, SemanticError>,
) -> Result<Digest, SemanticError> {
    let node = load(metadata.log_tree.root_hash)?;
    let persistent_tree::TreeNode::Root(root) = node.as_ref() else {
        return Err(fault("backup/log-root-kind", "log root is not a tree root"));
    };
    if root.order != IndexOrder::Eavt || root.history || root.count != metadata.log_tree.count {
        return Err(fault(
            "backup/log-root-binding",
            "log tree root differs from captured descriptor",
        ));
    }
    let source = CallbackSource(std::cell::RefCell::new(load));
    let mut cursor = crate::peer::DurableTreeCursor::new_prefix(
        source,
        Arc::new(root.clone()),
        false,
        IndexPrefix::Eavt {
            entity: t,
            attribute: None,
            value: None,
        },
    );
    let datom = cursor.next_datom()?.ok_or_else(|| {
        fault(
            "backup/log-page-missing",
            "log index omits requested transaction",
        )
    })?;
    if datom.entity != t || datom.attribute != 0 || datom.tx != crate::t_to_tx(t)? || !datom.added {
        return Err(fault(
            "backup/log-page-binding",
            "log page has invalid coordinates",
        ));
    }
    match &datom.value {
        Value::Bytes(bytes) if bytes.len() == 32 => Ok(bytes.as_slice().try_into().unwrap()),
        _ => Err(fault(
            "backup/log-page-hash",
            "log index value is not a content hash",
        )),
    }
}

#[allow(clippy::type_complexity)]
struct CallbackSource<'a>(
    std::cell::RefCell<
        &'a mut dyn FnMut(Digest) -> Result<Arc<persistent_tree::TreeNode>, SemanticError>,
    >,
);
impl crate::peer::DurableTreeSource for CallbackSource<'_> {
    fn load_node(
        &self,
        hash: Digest,
        _: &mut persistent_tree::TreeReadStats,
    ) -> Result<Arc<persistent_tree::TreeNode>, SemanticError> {
        (self.0.borrow_mut())(hash)
    }
}

pub(crate) fn read_log_transaction(
    _directory: &Path,
    metadata: &BackupReadMetadata,
    t: u64,
    predecessor: Option<Digest>,
    load: &mut dyn FnMut(Digest) -> Result<Vec<u8>, SemanticError>,
    load_node: &mut dyn FnMut(Digest) -> Result<Arc<persistent_tree::TreeNode>, SemanticError>,
) -> Result<(crate::LogTransaction, Digest, u64), SemanticError> {
    if t == 0 || t > metadata.point.basis_t {
        return Err(fault(
            "backup/log-range",
            "transaction is outside captured backup",
        ));
    }
    let hash = log_hash_at(metadata, t, load_node)?;
    let bytes = load(hash)?;
    if sha256(&bytes) != hash {
        return Err(fault(
            "backup/log-membership-hash",
            "membership checksum mismatch",
        ));
    }
    let membership = decode_backup_membership(&bytes, metadata.point.log_generation)?;
    let previous = if let Some(previous) = predecessor {
        previous
    } else if t == 1 {
        metadata.genesis_hash
    } else {
        log_hash_at(metadata, t - 1, load_node)?
    };
    if membership.lineage_id != metadata.point.lineage_id
        || membership.basis != t
        || membership.previous_hash != previous
    {
        return Err(fault(
            "backup/log-membership-binding",
            "log membership does not match captured lineage and predecessor",
        ));
    }
    let bytes = load(membership.content_hash)?;
    if sha256(&bytes) != membership.content_hash {
        return Err(fault(
            "backup/log-content-hash",
            "transaction checksum mismatch",
        ));
    }
    let content = LineageTransactionContent::decode(&bytes)?;
    if content.lineage_id != metadata.point.lineage_id
        || content.basis_t != t
        || content.eidx_frontier != membership.eidx_frontier
    {
        return Err(fault(
            "backup/log-content-binding",
            "transaction does not match its authenticated membership",
        ));
    }
    if t == metadata.point.basis_t
        && (hash != metadata.manifest.tx_hash
            || membership.state_hash != metadata.manifest.state_hash
            || membership.eidx_frontier != metadata.manifest.eidx_frontier)
    {
        return Err(fault(
            "backup/log-endpoint",
            "transaction does not match captured endpoint",
        ));
    }
    Ok((
        crate::LogTransaction {
            t,
            data: content.to_transaction(previous).tx_data,
        },
        hash,
        bytes.len() as u64,
    ))
}

pub(super) fn verify_read_log_index(
    directory: &Path,
    manifest: &Manifest,
    log: &LoadedBackupLog,
) -> Result<usize, SemanticError> {
    let Some(hash) = manifest.read_log_index else {
        return Ok(0);
    };
    let (descriptor, _) = decode_log_index(&read_object(directory, hash)?)?;
    if descriptor.count != manifest.basis {
        return Err(fault(
            "backup/read-log-count",
            "log read index omits transactions",
        ));
    }
    let validation = persistent_tree::validate_tree_streaming(&descriptor, |hash| {
        let bytes = read_object(directory, *hash)?;
        if let persistent_tree::TreeNode::Leaf(leaf) =
            persistent_tree::decode_tree_node(hash, &bytes)?
        {
            for i in 0..leaf.len() {
                let datom = leaf.datom(i).expect("decoded leaf index");
                let entry = datom
                    .entity
                    .checked_sub(1)
                    .and_then(|t| usize::try_from(t).ok())
                    .and_then(|t| log.entries.get(t))
                    .ok_or_else(|| {
                        fault(
                            "backup/read-log-binding",
                            "log read index names an absent transaction",
                        )
                    })?;
                if datom != log_datom(datom.entity, entry.transaction_hash) {
                    return Err(fault(
                        "backup/read-log-binding",
                        "log read index differs from authoritative chain",
                    ));
                }
            }
        }
        Ok(bytes)
    })?;
    Ok(1 + validation.nodes_read as usize)
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test]
    fn streaming_sink_preserves_tree_bytes_and_bounds_live_output() {
        let config = TreeConfig {
            max_leaf_datoms: 3,
            max_leaves_per_directory: 2,
            ..Default::default()
        };
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let mut input = (1..100)
                    .map(|t| log_datom(t, sha256(&t.to_be_bytes())))
                    .collect::<Vec<_>>();
                input.sort_by(|a, b| a.cmp_in(b, order));
                let expected =
                    persistent_tree::build_tree(order, history, input.clone(), &config).unwrap();
                let mut emitted = persistent_tree::TreeNodeSet::default();
                let actual = persistent_tree::build_tree_with_sink(
                    order,
                    history,
                    input.into_iter().map(Ok),
                    &config,
                    &mut |hash, bytes| emitted.insert_known(hash, bytes.to_vec()),
                )
                .unwrap();
                assert_eq!(actual.descriptor, expected.descriptor);
                assert!(actual.nodes.is_empty());
                assert_eq!(emitted.into_nodes(), expected.nodes.into_nodes());
                assert!(actual.stats.peak_live_datoms <= 3);
            }
        }
        let failure = persistent_tree::build_tree_with_sink(
            IndexOrder::Eavt,
            false,
            (1..20).map(|t| Ok(log_datom(t, [7; 32]))),
            &config,
            &mut |_, _| Err(fault("test/sink-failed", "injected")),
        )
        .unwrap_err();
        assert_eq!(failure.code, "test/sink-failed");
    }
}
