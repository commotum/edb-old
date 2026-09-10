//! Bounded authenticated closure discovery and exact shared-object release.
use super::*;

pub(super) fn expand<C: GenericClient>(
    c: &mut C,
    id: &str,
    r: &mut RetiredDatabaseReclamation,
) -> Result<bool, SemanticError> {
    let Some(row)=c.query_opt("SELECT kind,object_hash,next_child FROM atomic_database_reclamation_objects WHERE database_id=$1 AND NOT expanded ORDER BY kind,object_hash LIMIT 1",&[&id]).map_err(sql_error)? else{return Ok(false)};
    let kind: i16 = row.get(0);
    let hash = digest(row.get(1), "reclamation object")?;
    let offset = usize::try_from(row.get::<_, i64>(2))
        .map_err(|_| busy("invalid reclamation child cursor"))?;
    let (child_kind, children) = match kind {
        1 => {
            let loaded = crate::compressed_nodes::load_node_block(c, hash)?.ok_or_else(|| {
                busy("a retained native node is missing during reclamation discovery")
            })?;
            let children = match decode_tree_node(&hash, &loaded.canonical)? {
                TreeNode::Leaf(_) => vec![],
                TreeNode::Directory(node) => {
                    node.leaves.into_iter().map(|child| child.hash).collect()
                }
                TreeNode::Root(node) => node
                    .directories
                    .into_iter()
                    .map(|child| child.hash)
                    .collect(),
            };
            (1, children)
        }
        2 => {
            let row = c.query_one(
                "SELECT left_hash,right_hash FROM atomic_semantic_commitment_nodes WHERE node_hash=$1",
                &[&&hash[..]],
            ).map_err(sql_error)?;
            let children = [row.get::<_, Option<Vec<u8>>>(0), row.get(1)]
                .into_iter()
                .flatten()
                .map(|bytes| digest(bytes, "semantic child"))
                .collect::<Result<Vec<_>, _>>()?;
            (2, children)
        }
        3 => {
            let children = c.query(
                "SELECT child_hash FROM atomic_fulltext_page_edges WHERE parent_hash=$1 ORDER BY child_hash",
                &[&&hash[..]],
            ).map_err(sql_error)?
                .into_iter()
                .map(|row| digest(row.get(0), "fulltext child"))
                .collect::<Result<Vec<_>, _>>()?;
            (3, children)
        }
        6 => {
            let payload: Vec<u8> = c
                .query_one(
                    "SELECT payload FROM atomic_index_manifests WHERE manifest_hash=$1",
                    &[&&hash[..]],
                )
                .map_err(sql_error)?
                .get(0);
            if sha256(&payload) != hash {
                return Err(busy(
                    "legacy manifest failed authentication during reclamation",
                ));
            }
            let children = decode_index_manifest(&payload)?
                .segments
                .into_iter()
                .map(|segment| segment.hash)
                .collect();
            (4, children)
        }
        _ => return Err(busy("invalid reclamation object kind")),
    };
    r.objects_read += 1;
    if offset > children.len() {
        return Err(busy("reclamation cursor exceeds immutable node"));
    }
    let end = children
        .len()
        .min(offset.saturating_add(MAX_DATABASE_RECLAMATION_ROWS - 1));
    r.rows_selected = (end - offset + 1) as u64;
    if r.applied {
        for child in &children[offset..end] {
            r.rows_inserted += insert_object(c, id, child_kind, child)?;
        }
        r.rows_updated+=c.execute("UPDATE atomic_database_reclamation_objects SET next_child=$4,expanded=$5 WHERE database_id=$1 AND kind=$2 AND object_hash=$3",&[&id,&kind,&&hash[..],&(end as i64),&(end==children.len())]).map_err(sql_error)?;
    }
    Ok(true)
}

fn exists<C: GenericClient>(c: &mut C, sql: &str, hash: &Digest) -> Result<bool, SemanticError> {
    Ok(c.query_one(sql, &[&&hash[..]]).map_err(sql_error)?.get(0))
}
fn count<C: GenericClient>(
    c: &mut C,
    table: &str,
    predicate: &str,
    hash: &Digest,
) -> Result<u64, SemanticError> {
    positive_or_zero(
        c.query_one(
            &format!("SELECT count(*) FROM {table} WHERE {predicate}"),
            &[&&hash[..]],
        )
        .map_err(sql_error)?
        .get(0),
        "reclamation physical rows",
    )
}
fn remove<C: GenericClient>(
    c: &mut C,
    table: &str,
    predicate: &str,
    hash: &Digest,
    r: &mut RetiredDatabaseReclamation,
) -> Result<(), SemanticError> {
    if r.applied {
        r.rows_removed += c
            .execute(
                &format!("DELETE FROM {table} WHERE {predicate}"),
                &[&&hash[..]],
            )
            .map_err(sql_error)?;
    }
    Ok(())
}

const TREE_REFERENCED: &str = "SELECT EXISTS(SELECT 1 FROM atomic_tree_live_nodes WHERE node_hash=$1) OR EXISTS(SELECT 1 FROM atomic_tree_retired_nodes WHERE node_hash=$1) OR EXISTS(SELECT 1 FROM atomic_tree_delta_nodes WHERE node_hash=$1) OR EXISTS(SELECT 1 FROM atomic_tree_build_intent_nodes WHERE node_hash=$1) OR EXISTS(SELECT 1 FROM atomic_tree_manifest_roots WHERE root_hash=$1) OR EXISTS(SELECT 1 FROM atomic_request_base_archive_nodes WHERE node_hash=$1) OR EXISTS(SELECT 1 FROM atomic_request_base_archive_roots WHERE root_hash=$1)";
const TREE_UNCERTAIN: &str = "SELECT EXISTS(SELECT 1 FROM (SELECT DISTINCT ON(database_id) database_id,manifest_hash FROM atomic_tree_publications ORDER BY database_id,publication_revision DESC) p LEFT JOIN atomic_tree_live_sets l USING(database_id) WHERE l.database_id IS NULL OR l.manifest_hash<>p.manifest_hash OR NOT l.complete) OR EXISTS(SELECT 1 FROM atomic_tree_retirements WHERE NOT garbage_complete) OR EXISTS(SELECT 1 FROM atomic_tree_publications p WHERE EXISTS(SELECT 1 FROM atomic_tree_publications n WHERE n.database_id=p.database_id AND n.publication_revision>p.publication_revision) AND NOT EXISTS(SELECT 1 FROM atomic_tree_retirements r WHERE r.database_id=p.database_id AND r.publication_revision=p.publication_revision AND r.manifest_hash=p.manifest_hash)) OR EXISTS(SELECT 1 FROM atomic_tree_manifest_roots r JOIN atomic_tree_live_sets l USING(manifest_hash) WHERE l.complete AND NOT EXISTS(SELECT 1 FROM atomic_tree_live_nodes n WHERE n.database_id=l.database_id AND n.node_hash=r.root_hash))";

fn remove_tree<C: GenericClient>(
    c: &mut C,
    id: &str,
    hash: &Digest,
    r: &mut RetiredDatabaseReclamation,
) -> Result<(), SemanticError> {
    // The candidate row is already locked. Repeat the proof at DELETE time as
    // ordinary tree GC does: intent membership deliberately has no node FK.
    let removed = c
        .execute(
            &format!(
                "DELETE FROM atomic_tree_nodes WHERE node_hash=$1 \
             AND NOT ({TREE_REFERENCED}) AND NOT ({TREE_UNCERTAIN}) \
             AND NOT EXISTS(SELECT 1 FROM atomic_database_reclamation_objects \
                            WHERE database_id<>$2 AND kind=1 AND object_hash=$1)"
            ),
            &[&&hash[..], &id],
        )
        .map_err(|error| {
            if error
                .as_db_error()
                .is_some_and(|error| error.code().code() == "23503")
            {
                busy("a concurrent tree publication now references this native node")
            } else {
                sql_error(error)
            }
        })?;
    r.rows_removed += removed;
    if removed == 0
        && exists(
            c,
            "SELECT EXISTS(SELECT 1 FROM atomic_tree_nodes WHERE node_hash=$1)",
            hash,
        )?
        && !exists(c, TREE_REFERENCED, hash)?
        && !c
            .query_one(
                "SELECT EXISTS(SELECT 1 FROM atomic_database_reclamation_objects \
                           WHERE database_id<>$2 AND kind=1 AND object_hash=$1)",
                &[&&hash[..], &id],
            )
            .map_err(sql_error)?
            .get::<_, bool>(0)
    {
        // New incomplete liveness is not evidence of genuine sharing. Roll
        // back the batch, retaining our frontier until the proof is complete.
        return Err(busy(
            "native tree liveness changed during physical collection",
        ));
    }
    Ok(())
}

pub(super) fn collect<C: GenericClient>(
    c: &mut C,
    id: &str,
    r: &mut RetiredDatabaseReclamation,
) -> Result<bool, SemanticError> {
    // SQL DAG parents precede children. A parent retained by another root is
    // released from our frontier first; its children then remain shared too.
    let row=c.query_opt("SELECT o.kind,o.object_hash FROM atomic_database_reclamation_objects o WHERE o.database_id=$1 AND NOT(o.kind=2 AND EXISTS(SELECT 1 FROM atomic_semantic_commitment_nodes p JOIN atomic_database_reclamation_objects q ON q.kind=2 AND q.object_hash=p.node_hash AND q.database_id=$1 WHERE p.left_hash=o.object_hash OR p.right_hash=o.object_hash)) AND NOT(o.kind=3 AND EXISTS(SELECT 1 FROM atomic_fulltext_page_edges e JOIN atomic_database_reclamation_objects q ON q.kind=3 AND q.object_hash=e.parent_hash AND q.database_id=$1 WHERE e.child_hash=o.object_hash)) ORDER BY o.kind,o.object_hash LIMIT 1",&[&id]).map_err(sql_error)?;
    let Some(row) = row else {
        if c.query_one(
            "SELECT EXISTS(SELECT 1 FROM atomic_database_reclamation_objects WHERE database_id=$1)",
            &[&id],
        )
        .map_err(sql_error)?
        .get::<_, bool>(0)
        {
            return Err(busy("reclamation object graph contains a cycle"));
        }
        return Ok(false);
    };
    let kind: i16 = row.get(0);
    let hash = digest(row.get(1), "reclamation candidate")?;
    // Only physical SQL-DAG release needs the global producer/collector fence.
    // Take it before sharedness checks, which then see any completed competing
    // root/parent insertion. Try-only acquisition cannot invert target pins.
    let fence = match kind {
        2 => Some("SELECT pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key())"),
        3 => Some("SELECT pg_try_advisory_xact_lock(atomic_fulltext_gc_pin_key())"),
        _ => None,
    };
    if let Some(fence) = fence {
        r.pins_checked += 1;
        if !c
            .query_one(fence, &[])
            .map_err(sql_error)?
            .get::<_, bool>(0)
        {
            return Err(busy(
                "a projection builder pins this physical object domain",
            ));
        }
    }
    if kind == 1 {
        // Serialize with node-FK publishers before taking a fresh liveness
        // snapshot. Try-only acquisition keeps a competing builder retryable.
        // Preview takes the same lock, but never mutates a row.
        c.query_opt(
            "SELECT node_hash FROM atomic_tree_nodes WHERE node_hash=$1 FOR UPDATE NOWAIT",
            &[&&hash[..]],
        )
        .map_err(sql_error)?;
    }
    let shared_frontier:bool=c.query_one("SELECT EXISTS(SELECT 1 FROM atomic_database_reclamation_objects WHERE database_id<>$1 AND kind=$2 AND object_hash=$3)",&[&id,&kind,&&hash[..]]).map_err(sql_error)?.get(0);
    let mut shared = shared_frontier;
    if !shared {
        shared = match kind {
            1 => {
                let shared = exists(c, TREE_REFERENCED, &hash)?;
                if !shared
                    && c.query_one(TREE_UNCERTAIN, &[])
                        .map_err(sql_error)?
                        .get::<_, bool>(0)
                {
                    return Err(busy(
                        "another retained tree has incomplete liveness evidence; repair or collect that root before terminal physical collection",
                    ));
                }
                shared
            }
            2 => exists(
                c,
                "SELECT EXISTS(SELECT 1 FROM atomic_semantic_commitment_roots WHERE current_root=$1) OR EXISTS(SELECT 1 FROM atomic_semantic_commitment_nodes WHERE left_hash=$1 OR right_hash=$1)",
                &hash,
            )?,
            3 => exists(
                c,
                "SELECT EXISTS(SELECT 1 FROM atomic_fulltext_page_roots WHERE root_hash=$1) OR EXISTS(SELECT 1 FROM atomic_fulltext_page_edges WHERE child_hash=$1) OR EXISTS(SELECT 1 FROM atomic_fulltext_pages p JOIN atomic_fulltext_page_builds b ON b.manifest_hash=p.created_for WHERE p.block_hash=$1)",
                &hash,
            )?,
            4 => {
                let mut shared = false;
                let mut after: Option<Vec<u8>> = None;
                while let Some(row) = c.query_opt(
                    "SELECT manifest_hash,payload FROM atomic_index_manifests WHERE ($1::bytea IS NULL OR manifest_hash>$1) ORDER BY manifest_hash LIMIT 1",
                    &[&after],
                ).map_err(sql_error)? {
                    let expected = digest(row.get(0), "shared legacy manifest")?;
                    let payload: Vec<u8> = row.get(1);
                    r.objects_read += 1;
                    if sha256(&payload) != expected {
                        return Err(busy("a remaining legacy manifest is corrupt; segment sharing cannot be proved"));
                    }
                    if decode_index_manifest(&payload)?.segments.iter().any(|segment| segment.hash == hash) {
                        shared = true;
                        break;
                    }
                    after = Some(expected.to_vec());
                }
                shared
            }
            5 => {
                let references_complete: bool = c.query_one(
                    "SELECT complete AND problem_code IS NULL AND walker_version=$1 FROM atomic_program_reference_state WHERE singleton",
                    &[&crate::postgres::PROGRAM_REFERENCE_WALKER_VERSION],
                ).map_err(sql_error)?.get(0);
                if !references_complete {
                    return Err(busy("program reachability evidence is incomplete"));
                }
                exists(
                    c,
                    "SELECT EXISTS(SELECT 1 FROM atomic_program_generation_refs WHERE program_hash=$1)",
                    &hash,
                )?
            }
            6..=8 => true,
            _ => return Err(busy("invalid reclamation candidate kind")),
        };
    }
    let mut deletes = Vec::new();
    if !shared {
        match kind {
            1 => {
                deletes.push(("atomic_tree_garbage_nodes", "node_hash=$1"));
                deletes.push(("atomic_tree_node_blocks", "node_hash=$1"));
                deletes.push(("atomic_tree_nodes", "node_hash=$1"));
            }
            2 => deletes.push(("atomic_semantic_commitment_nodes", "node_hash=$1")),
            3 => {
                deletes.push(("atomic_fulltext_page_edges", "parent_hash=$1"));
                deletes.push(("atomic_fulltext_page_garbage", "block_hash=$1"));
                deletes.push(("atomic_fulltext_pages", "block_hash=$1"));
            }
            4 => deletes.push(("atomic_index_segments", "segment_hash=$1")),
            5 => {
                deletes.push(("atomic_program_gc_candidates", "program_hash=$1"));
                deletes.push(("atomic_programs", "program_hash=$1"));
            }
            _ => {}
        }
    }
    r.rows_selected = 1;
    for (table, predicate) in &deletes {
        r.rows_selected += count(c, table, predicate, &hash)?;
    }
    // Terminal fulltext deletion owns its own exact frontier; SQL suppresses
    // the ordinary collector's duplicate enqueue side effect for this path.
    if r.rows_selected > MAX_DATABASE_RECLAMATION_ROWS as u64 {
        return Err(busy(
            "one physical object's fanout exceeds the bounded reclamation batch",
        ));
    }
    if r.applied {
        for (table, predicate) in deletes {
            if table == "atomic_tree_nodes" {
                remove_tree(c, id, &hash, r)?;
            } else {
                remove(c, table, predicate, &hash, r)?;
            }
        }
        r.rows_removed+=c.execute("DELETE FROM atomic_database_reclamation_objects WHERE database_id=$1 AND kind=$2 AND object_hash=$3",&[&id,&kind,&&hash[..]]).map_err(sql_error)?;
    }
    Ok(true)
}
