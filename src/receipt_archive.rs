//! Bounded ownership transfer of exact receipt bases out of the accelerator
//! retirement prefix. Archive nodes share immutable payloads; this copies only
//! closure ownership, not application data or a reconstructed Database.
use super::receipt_archive_hash::ReceiptArchiveHash;
use super::{digest, operation_error, positive_or_zero};
use crate::persistent_tree::{ChildRef, TreeNode, decode_tree_node};
use crate::{Digest, ErrorCategory, PersistentTreeManifest, SemanticError};
use postgres::GenericClient;
use std::collections::BTreeSet;

pub const MAX_RECEIPT_ARCHIVE_WORK_PER_GC: usize = 512;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ReceiptArchiveConversion {
    pub database_id: String,
    pub generation: u64,
    pub manifest_hash: Digest,
    /// 0=start, 1=closure traversal, 2=ordered hash, 3=retired-node drain,
    /// 4=atomic ownership handoff.
    pub phase: u16,
    pub node_reads: u64,
    pub node_read_bytes: u64,
    pub closure_nodes_added: u64,
    pub hashes_processed: u64,
    pub retired_rows_drained: u64,
}

#[derive(Clone)]
pub(super) struct ConversionPlan {
    pub report: ReceiptArchiveConversion,
    action: Action,
}

#[derive(Clone)]
enum Action {
    Start,
    Traverse {
        changes: Vec<FrontierChange>,
        admitted: Vec<Digest>,
        old_count: u64,
    },
    Hash {
        checkpoint: Vec<u8>,
        cursor: Option<Digest>,
        processed: u64,
        complete: Option<Digest>,
    },
    Drain {
        hashes: Vec<Digest>,
        database: String,
        revision: i64,
    },
    Finish,
}

#[derive(Clone)]
struct FrontierChange {
    hash: Digest,
    old_offset: i32,
    next_offset: Option<i32>,
}

fn fault(message: impl Into<String>) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Fault,
        "operations/receipt-archive-conversion",
        message,
    )
}

pub(super) fn plan<C: GenericClient>(
    client: &mut C,
    age: i64,
) -> Result<Option<ConversionPlan>, SemanticError> {
    let mut offset = 0_i64;
    loop {
        let rows = client.query(
            "SELECT p.database_id,p.log_generation,p.publication_revision,p.manifest_hash,work.phase,work.node_count,work.hashed_nodes,work.hash_cursor,work.hash_state \
             FROM atomic_tree_publications p JOIN atomic_tree_retirements r USING(database_id,publication_revision,manifest_hash) \
             LEFT JOIN atomic_receipt_archive_conversions work USING(manifest_hash) \
             WHERE r.bookkeeping_complete \
               AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications older WHERE older.database_id=p.database_id AND older.publication_revision<p.publication_revision) \
               AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents intent WHERE intent.manifest_hash=p.manifest_hash) \
               AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress WHERE progress.manifest_hash=p.manifest_hash) \
               AND (work.manifest_hash IS NOT NULL OR (r.retired_at<clock_timestamp()-$1::bigint*interval '1 millisecond' \
                    AND EXISTS (SELECT 1 FROM atomic_heads head JOIN atomic_generation_request_bases base ON base.database_id=head.database_id AND base.generation=head.log_generation \
                                WHERE head.database_id=p.database_id AND head.log_generation=p.log_generation AND base.base_manifest_hash=p.manifest_hash))) \
             ORDER BY r.retired_at,p.database_id,p.publication_revision LIMIT 64 OFFSET $2", &[&age,&offset]
        ).map_err(|error| operation_error("operations/receipt-archive-candidates",error))?;
        if rows.is_empty() {
            return Ok(None);
        }
        offset += rows.len() as i64;
        for row in rows {
            let hash = digest(row.get(3), "receipt archive source")?;
            let locked: bool = client
                .query_one(
                    "SELECT atomic_receipt_archive_conversion_context($1)",
                    &[&&hash[..]],
                )
                .map_err(|error| operation_error("operations/receipt-archive-lock", error))?
                .get(0);
            if !locked {
                continue;
            }
            let phase: Option<i16> = row.get(4);
            let mut report = ReceiptArchiveConversion {
                database_id: row.get(0),
                generation: positive_or_zero(row.get(1), "archive generation")?,
                manifest_hash: hash,
                phase: phase.unwrap_or(0) as u16,
                node_reads: 0,
                node_read_bytes: 0,
                closure_nodes_added: 0,
                hashes_processed: 0,
                retired_rows_drained: 0,
            };
            let action = match phase {
                None => {
                    validate_source(client, &mut report)?;
                    Action::Start
                }
                Some(1) => traversal(
                    client,
                    &mut report,
                    positive_or_zero(row.get(5), "archive node count")?,
                )?,
                Some(2) => hash_batch(
                    client,
                    &mut report,
                    positive_or_zero(row.get(5), "archive node count")?,
                    positive_or_zero(row.get(6), "archive hashed count")?,
                    row.get(7),
                    row.get(8),
                )?,
                Some(3) => {
                    let total = positive_or_zero(row.get(5), "archive completed node count")?;
                    let hashed = positive_or_zero(row.get(6), "archive completed hash count")?;
                    let bytes: Option<Vec<u8>> = row.get(8);
                    let state = ReceiptArchiveHash::decode(
                        &bytes.ok_or_else(|| fault("completed archive has no hash checkpoint"))?,
                    )?;
                    if hashed != total
                        || state.processed_bytes()
                            != total
                                .checked_mul(32)
                                .ok_or_else(|| fault("archive completed byte count overflow"))?
                    {
                        return Err(fault("archive completed checkpoint/count mismatch"));
                    }
                    let stored = client.query_one("SELECT expected_node_count,node_set_hash FROM atomic_request_base_archives WHERE manifest_hash=$1", &[&&hash[..]])
                        .map_err(|error| operation_error("operations/receipt-archive-completed-hash", error))?;
                    if positive_or_zero(stored.get(0), "archive header node count")? != total
                        || digest(stored.get(1), "archive header node set hash")?
                            != state.finalize()
                    {
                        return Err(fault("archive completed checkpoint/header mismatch"));
                    }
                    let rows=client.query("SELECT node_hash FROM atomic_tree_retired_nodes WHERE database_id=$1 AND publication_revision=$2 ORDER BY node_hash LIMIT $3", &[&report.database_id,&row.get::<_,i64>(2),&(MAX_RECEIPT_ARCHIVE_WORK_PER_GC as i64)])
                        .map_err(|error| operation_error("operations/receipt-archive-retired-read",error))?;
                    let hashes = rows
                        .into_iter()
                        .map(|row| digest(row.get(0), "archive retired node"))
                        .collect::<Result<Vec<_>, _>>()?;
                    if hashes.is_empty() {
                        validate_source(client, &mut report)?;
                        report.phase = 4;
                        Action::Finish
                    } else {
                        report.retired_rows_drained = hashes.len() as u64;
                        Action::Drain {
                            hashes,
                            database: report.database_id.clone(),
                            revision: row.get(2),
                        }
                    }
                }
                _ => return Err(fault("invalid archive conversion phase")),
            };
            return Ok(Some(ConversionPlan { report, action }));
        }
    }
}

fn load_node<C: GenericClient>(
    client: &mut C,
    hash: Digest,
    report: &mut ReceiptArchiveConversion,
) -> Result<TreeNode, SemanticError> {
    let row = client
        .query_opt(
            "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
            &[&&hash[..]],
        )
        .map_err(|error| operation_error("operations/receipt-archive-node", error))?
        .ok_or_else(|| fault("receipt archive source references missing immutable node"))?;
    let payload: Vec<u8> = row.get(0);
    report.node_reads += 1;
    report.node_read_bytes += payload.len() as u64;
    decode_tree_node(&hash, &payload)
}

fn validate_source<C: GenericClient>(
    client: &mut C,
    report: &mut ReceiptArchiveConversion,
) -> Result<(), SemanticError> {
    let row = client
        .query_opt(
            "SELECT m.payload,m.database_id,m.publication_revision,m.basis_t,m.tx_hash, \
                    m.state_hash,m.excision_generation,m.eidx_frontier,m.manifest_version, \
                    m.log_generation,p.log_generation, \
                    (semantic.tx_hash IS NOT NULL AND \
                     ((m.basis_t=0 AND m.tx_hash=catalog.genesis_hash) OR native.tx_hash IS NOT NULL)) \
               FROM atomic_tree_manifests m \
               JOIN atomic_tree_publications p \
                 ON p.manifest_hash=m.manifest_hash AND p.database_id=m.database_id \
                AND p.publication_revision=m.publication_revision \
                AND p.basis_t=m.basis_t AND p.tx_hash=m.tx_hash \
               JOIN atomic_databases catalog ON catalog.database_id=m.database_id \
               LEFT JOIN atomic_generation_transactions native \
                 ON native.database_id=m.database_id AND native.generation=m.log_generation \
                AND native.basis_t=m.basis_t AND native.tx_hash=m.tx_hash \
                AND native.state_hash=m.state_hash \
               LEFT JOIN atomic_semantic_commitment_roots semantic \
                 ON semantic.database_id=m.database_id AND semantic.generation=m.log_generation \
                AND semantic.basis_t=m.basis_t AND semantic.tx_hash=m.tx_hash \
                AND semantic.state_hash=m.state_hash AND semantic.eidx_frontier=m.eidx_frontier \
                AND semantic.commitment_version=2 \
              WHERE m.manifest_hash=$1",
            &[&&report.manifest_hash[..]],
        )
        .map_err(|error| operation_error("operations/receipt-archive-manifest", error))?
        .ok_or_else(|| fault("receipt source has no matching publication and manifest"))?;
    let payload: Vec<u8> = row.get(0);
    if crate::sha256(&payload) != report.manifest_hash {
        return Err(fault("receipt source manifest checksum mismatch"));
    }
    let manifest = PersistentTreeManifest::decode(&payload)?;
    if manifest.database_id != report.database_id
        || manifest.database_id != row.get::<_, String>(1)
        || manifest.publication_revision
            != positive_or_zero(row.get(2), "receipt source publication revision")?
        || manifest.basis_t != positive_or_zero(row.get(3), "receipt source basis")?
        || manifest.tx_hash != digest(row.get(4), "receipt source transaction hash")?
        || manifest.state_hash != digest(row.get(5), "receipt source state hash")?
        || manifest.excision_generation != report.generation
        || manifest.excision_generation
            != positive_or_zero(row.get(6), "receipt source excision generation")?
        || manifest.eidx_frontier != positive_or_zero(row.get(7), "receipt source entity frontier")?
        || PersistentTreeManifest::encoded_version(&payload)? != row.get::<_, i16>(8)
        || manifest.excision_generation
            != positive_or_zero(row.get(9), "receipt source manifest generation")?
        || manifest.excision_generation
            != positive_or_zero(row.get(10), "receipt source publication generation")?
        || !row.get::<_, bool>(11)
    {
        return Err(fault(
            "receipt source manifest is not one canonical authoritative value",
        ));
    }
    let roots=client.query("SELECT index_order,history,root_hash,datom_count,encoded_bytes FROM atomic_tree_manifest_roots WHERE manifest_hash=$1 ORDER BY index_order,history", &[&&report.manifest_hash[..]])
        .map_err(|error|operation_error("operations/receipt-archive-roots",error))?;
    if roots.len() != 8 || manifest.trees.len() != 8 {
        return Err(fault("receipt source has incomplete root projection"));
    }
    for tree in &manifest.trees {
        let hash = tree.descriptor.root_hash;
        let previous_bytes = report.node_read_bytes;
        let root = load_node(client, hash, report)?;
        let root_bytes = report.node_read_bytes - previous_bytes;
        let TreeNode::Root(root) = root else {
            return Err(fault("receipt source root has wrong node kind"));
        };
        if root.order != tree.descriptor.order
            || root.history != tree.descriptor.history
            || root.count != tree.descriptor.count
            || root_bytes != tree.root_bytes
        {
            return Err(fault("receipt source root descriptor mismatch"));
        }
        let order = match root.order {
            crate::IndexOrder::Eavt => 0_i16,
            crate::IndexOrder::Aevt => 1,
            crate::IndexOrder::Avet => 2,
            crate::IndexOrder::Vaet => 3,
        };
        let found = roots.iter().any(|row| {
            row.get::<_, i16>(0) == order
                && row.get::<_, bool>(1) == root.history
                && row.get::<_, Vec<u8>>(2) == hash
                && row.get::<_, i64>(3) >= 0
                && row.get::<_, i64>(3) as u64 == root.count
                && row.get::<_, i64>(4) >= 0
                && row.get::<_, i64>(4) as u64 == tree.root_bytes
        });
        if !found {
            return Err(fault(
                "receipt source relational root differs from manifest",
            ));
        }
    }
    Ok(())
}

fn children(node: &TreeNode) -> &[ChildRef] {
    match node {
        TreeNode::Root(node) => &node.directories,
        TreeNode::Directory(node) => &node.leaves,
        TreeNode::Leaf(_) => &[],
    }
}

fn validate_edge(
    parent: &TreeNode,
    reference: &ChildRef,
    child: &TreeNode,
) -> Result<(), SemanticError> {
    // Routing separators may precede their child's first key (they are not
    // necessarily exact datoms), but may never skip that first key. Match the
    // strict lazy reader's edge validation before retaining an archive owner.
    let (count, invalid_boundary) = match (parent, child) {
        (TreeNode::Root(_), TreeNode::Directory(child)) => (
            child.count,
            child
                .leaves
                .first()
                .is_none_or(|first| reference.key.cmp_key(&first.key, parent.order()).is_gt()),
        ),
        (TreeNode::Directory(_), TreeNode::Leaf(child)) => (
            child.len() as u64,
            child
                .datom(0)
                .as_ref()
                .is_none_or(|first| reference.key.cmp_datom(first, parent.order()).is_gt()),
        ),
        _ => return Err(fault("receipt source has a wrong-kind child")),
    };
    if parent.order() != child.order()
        || parent.history() != child.history()
        || count != reference.count
        || invalid_boundary
    {
        return Err(fault(
            "receipt source child identity/count/routing mismatch",
        ));
    }
    Ok(())
}

fn traversal<C: GenericClient>(
    client: &mut C,
    report: &mut ReceiptArchiveConversion,
    old_count: u64,
) -> Result<Action, SemanticError> {
    let rows=client.query("SELECT node_hash,next_child FROM atomic_receipt_archive_frontier WHERE manifest_hash=$1 ORDER BY node_hash LIMIT $2", &[&&report.manifest_hash[..],&(MAX_RECEIPT_ARCHIVE_WORK_PER_GC as i64)])
        .map_err(|error|operation_error("operations/receipt-archive-frontier",error))?;
    let mut changes = Vec::new();
    let mut discovered = BTreeSet::new();
    for row in rows {
        if report.node_reads + 1 >= MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64 {
            break;
        }
        let hash = digest(row.get(0), "archive frontier node")?;
        let offset: i32 = row.get(1);
        let node = load_node(client, hash, report)?;
        let children = children(&node);
        if offset < 0 || offset as usize > children.len() {
            return Err(fault("archive frontier child offset is invalid"));
        }
        let mut next = offset as usize;
        while next < children.len() && report.node_reads < MAX_RECEIPT_ARCHIVE_WORK_PER_GC as u64 {
            let reference = &children[next];
            let child = load_node(client, reference.hash, report)?;
            validate_edge(&node, reference, &child)?;
            discovered.insert(reference.hash);
            next += 1;
        }
        changes.push(FrontierChange {
            hash,
            old_offset: offset,
            next_offset: if next == children.len() {
                None
            } else {
                Some(next as i32)
            },
        });
    }
    let hashes = discovered
        .iter()
        .map(|hash| hash.to_vec())
        .collect::<Vec<_>>();
    let existing=client.query("SELECT node_hash FROM atomic_request_base_archive_nodes WHERE manifest_hash=$1 AND node_hash=ANY($2::bytea[])", &[&&report.manifest_hash[..],&hashes])
        .map_err(|error|operation_error("operations/receipt-archive-known-nodes",error))?;
    for row in existing {
        discovered.remove(&digest(row.get(0), "known archive node")?);
    }
    let admitted = discovered.into_iter().collect::<Vec<_>>();
    report.closure_nodes_added = admitted.len() as u64;
    Ok(Action::Traverse {
        changes,
        admitted,
        old_count,
    })
}

fn hash_batch<C: GenericClient>(
    client: &mut C,
    report: &mut ReceiptArchiveConversion,
    total: u64,
    processed: u64,
    cursor: Option<Vec<u8>>,
    checkpoint: Option<Vec<u8>>,
) -> Result<Action, SemanticError> {
    let mut hash = match checkpoint {
        Some(bytes) => ReceiptArchiveHash::decode(&bytes)?,
        None => ReceiptArchiveHash::new(total),
    };
    if (processed == 0) != cursor.is_none()
        || hash.processed_bytes()
            != processed
                .checked_mul(32)
                .ok_or_else(|| fault("archive hash byte count overflow"))?
    {
        return Err(fault("archive hash cursor/count mismatch"));
    }
    let rows=client.query("SELECT node_hash FROM atomic_request_base_archive_nodes WHERE manifest_hash=$1 AND ($2::bytea IS NULL OR node_hash>$2) ORDER BY node_hash LIMIT $3", &[&&report.manifest_hash[..],&cursor,&(MAX_RECEIPT_ARCHIVE_WORK_PER_GC as i64)])
        .map_err(|error|operation_error("operations/receipt-archive-hash-page",error))?;
    let mut cursor = cursor
        .map(|bytes| digest(bytes, "archive hash cursor"))
        .transpose()?;
    let mut count = processed;
    for row in rows {
        let next = digest(row.get(0), "archive hash member")?;
        hash.update(&next)?;
        cursor = Some(next);
        count += 1;
    }
    if count > total {
        return Err(fault("archive node count grew after closure completion"));
    }
    report.hashes_processed = count - processed;
    let checkpoint = hash.encode();
    let complete = if count == total {
        Some(hash.finalize())
    } else {
        None
    };
    if count == processed && complete.is_none() {
        return Err(fault("archive hash traversal ended before expected count"));
    }
    Ok(Action::Hash {
        checkpoint,
        cursor,
        processed: count,
        complete,
    })
}

pub(super) fn apply<C: GenericClient>(
    client: &mut C,
    plan: ConversionPlan,
    age: i64,
) -> Result<(), SemanticError> {
    let hash = plan.report.manifest_hash;
    let locked: bool = client
        .query_one(
            "SELECT atomic_receipt_archive_conversion_context($1)",
            &[&&hash[..]],
        )
        .map_err(|error| operation_error("operations/receipt-archive-relock", error))?
        .get(0);
    if !locked {
        return Err(fault("receipt archive source changed after preview"));
    }
    match plan.action {
        Action::Start => {
            let started: bool = client
                .query_one(
                    "SELECT atomic_begin_receipt_archive_conversion($1,$2)",
                    &[&&hash[..], &age],
                )
                .map_err(|error| operation_error("operations/receipt-archive-begin", error))?
                .get(0);
            if !started {
                return Err(fault("receipt archive source changed before staging"));
            }
        }
        Action::Traverse {
            changes,
            admitted,
            old_count,
        } => {
            let nodes = admitted
                .iter()
                .map(|hash| hash.to_vec())
                .collect::<Vec<_>>();
            let inserted=client.execute("INSERT INTO atomic_request_base_archive_nodes(manifest_hash,node_hash) SELECT $1,node_hash FROM unnest($2::bytea[]) node_hash", &[&&hash[..],&nodes])
                .map_err(|error|operation_error("operations/receipt-archive-admit-nodes",error))?;
            if inserted != admitted.len() as u64 {
                return Err(fault("archive admitted-node count mismatch"));
            }
            client.execute("INSERT INTO atomic_receipt_archive_frontier(manifest_hash,node_hash) SELECT $1,node_hash FROM unnest($2::bytea[]) node_hash", &[&&hash[..],&nodes])
                .map_err(|error|operation_error("operations/receipt-archive-admit-frontier",error))?;
            for change in changes {
                let affected=if let Some(next)=change.next_offset {
                    client.execute("UPDATE atomic_receipt_archive_frontier SET next_child=$4 WHERE manifest_hash=$1 AND node_hash=$2 AND next_child=$3", &[&&hash[..],&&change.hash[..],&change.old_offset,&next])
                }else{
                    client.execute("DELETE FROM atomic_receipt_archive_frontier WHERE manifest_hash=$1 AND node_hash=$2 AND next_child=$3", &[&&hash[..],&&change.hash[..],&change.old_offset])
                }.map_err(|error|operation_error("operations/receipt-archive-advance-frontier",error))?;
                if affected != 1 {
                    return Err(fault("archive frontier changed after preview"));
                }
            }
            let count = old_count
                .checked_add(inserted)
                .and_then(|value| i64::try_from(value).ok())
                .ok_or_else(|| fault("archive closure count overflow"))?;
            client.execute("UPDATE atomic_receipt_archive_conversions SET node_count=$2,phase=CASE WHEN EXISTS (SELECT 1 FROM atomic_receipt_archive_frontier WHERE manifest_hash=$1) THEN 1 ELSE 2 END WHERE manifest_hash=$1", &[&&hash[..],&count])
                .map_err(|error|operation_error("operations/receipt-archive-advance-closure",error))?;
            client.execute("UPDATE atomic_request_base_archives SET expected_node_count=$2 WHERE manifest_hash=$1", &[&&hash[..],&count])
                .map_err(|error|operation_error("operations/receipt-archive-final-count",error))?;
        }
        Action::Hash {
            checkpoint,
            cursor,
            processed,
            complete,
        } => {
            let cursor = cursor.map(|hash| hash.to_vec());
            let processed =
                i64::try_from(processed).map_err(|_| fault("archive hash count overflow"))?;
            client.execute("UPDATE atomic_receipt_archive_conversions SET hash_state=$2,hash_cursor=$3,hashed_nodes=$4,phase=$5 WHERE manifest_hash=$1", &[&&hash[..],&checkpoint,&cursor,&processed,&if complete.is_some(){3_i16}else{2_i16}])
                .map_err(|error|operation_error("operations/receipt-archive-hash-checkpoint",error))?;
            if let Some(complete) = complete {
                client.execute("UPDATE atomic_request_base_archives SET node_set_hash=$2 WHERE manifest_hash=$1", &[&&hash[..],&&complete[..]])
                .map_err(|error|operation_error("operations/receipt-archive-final-hash",error))?;
            }
        }
        Action::Drain {
            hashes,
            database,
            revision,
        } => {
            let nodes = hashes.iter().map(|hash| hash.to_vec()).collect::<Vec<_>>();
            client
                .batch_execute("SET LOCAL atomic.tree_gc_active='v13'")
                .map_err(|error| {
                    operation_error("operations/receipt-archive-drain-context", error)
                })?;
            client.execute("INSERT INTO atomic_tree_garbage_nodes(node_hash,marked_at) SELECT node.node_hash,r.retired_at FROM atomic_tree_retired_nodes node JOIN atomic_tree_retirements r USING(database_id,publication_revision) WHERE node.database_id=$1 AND node.publication_revision=$2 AND node.node_hash=ANY($3::bytea[]) ON CONFLICT(node_hash) DO NOTHING", &[&database,&revision,&nodes])
                .map_err(|error|operation_error("operations/receipt-archive-mark-retired",error))?;
            let removed=client.execute("DELETE FROM atomic_tree_retired_nodes WHERE database_id=$1 AND publication_revision=$2 AND node_hash=ANY($3::bytea[])", &[&database,&revision,&nodes])
                .map_err(|error|operation_error("operations/receipt-archive-drain-retired",error))?;
            if removed != hashes.len() as u64 {
                return Err(fault("archive retired ledger changed after preview"));
            }
        }
        Action::Finish => {
            let finished: bool = client
                .query_one(
                    "SELECT atomic_finish_receipt_archive_conversion($1,$2)",
                    &[&&hash[..], &age],
                )
                .map_err(|error| operation_error("operations/receipt-archive-handoff", error))?
                .get(0);
            if !finished {
                return Err(fault("receipt archive ownership handoff was rejected"));
            }
        }
    }
    Ok(())
}
