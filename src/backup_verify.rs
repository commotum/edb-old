//! Explicit repository integrity work. Ordinary repository opens do not call
//! this replay. Deep verification retains one eager canonical database and
//! small object/basis schedules, never one database clone per receipt prefix.
use super::{BackupVerification, ReadPoint, load_publication, read_object};
use crate::operations::{
    derive_index_projection, same_stored_datoms, validate_physical_history_projection,
};
use crate::persistent_tree::{TreeDescriptor, TreeNode};
use crate::storage::descriptors::{INDEX_DESCRIPTOR_KIND, SNAPSHOT_METADATA_KIND};
use crate::storage::log::LogRoot;
use crate::storage::receipts::{ExactReceipt, RECEIPT_KIND, RequestIndex, basis_receipt_key};
use crate::storage::root::{Block, DATABASE_VALUE_ROOT_KIND, DatabaseRoot, DatabaseValueRoot};
use crate::storage::{IndexDescriptor, ObjectId, ObjectReader, SnapshotMetadata};
use crate::{
    Database, Datom, DurableTransaction, ErrorCategory, IndexOrder, MaintenanceControl,
    SemanticError, View,
};
use std::collections::{BTreeMap, BTreeSet};
use std::path::Path;

struct Reader<'a> {
    directory: &'a Path,
    control: &'a MaintenanceControl,
}
impl ObjectReader for Reader<'_> {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        self.control.check()?;
        read_object(self.directory, id)
    }
}

#[derive(Default)]
struct Inventory {
    count: usize,
    values: BTreeMap<ObjectId, DatabaseValueRoot>,
    indexes: BTreeMap<u64, BTreeSet<ObjectId>>,
    metadata: BTreeMap<u64, Vec<SnapshotMetadata>>,
    receipts: BTreeSet<ObjectId>,
}

/// Authenticate the complete reachable graph, including program references
/// embedded in tree values, log bodies and native query/program literals.
/// Deep mode also validates tree routing/counts and typed descriptor codecs.
pub(crate) fn walk_repository(
    directory: &Path,
    roots: &[ObjectId],
    control: &MaintenanceControl,
    deep: bool,
) -> Result<usize, SemanticError> {
    Ok(inventory(directory, roots, control, deep)?.count)
}

fn inventory(
    directory: &Path,
    roots: &[ObjectId],
    control: &MaintenanceControl,
    deep: bool,
) -> Result<Inventory, SemanticError> {
    let mut reader = Reader { directory, control };
    let mut seen = BTreeSet::new();
    let mut programs = BTreeSet::new();
    let mut pending = roots.to_vec();
    let mut result = Inventory::default();
    while let Some(id) = pending.pop() {
        control.check()?;
        if !seen.insert(id) {
            continue;
        }
        let bytes = reader.read_object(id)?;
        pending.extend(crate::storage::ownership::object_children(
            &mut reader,
            id,
            &bytes,
        )?);
        if bytes.starts_with(b"ATIX") {
            if let TreeNode::Leaf(leaf) = crate::persistent_tree::decode_tree_node(&id, &bytes)? {
                for value in &leaf.values {
                    crate::program_bindings::collect_program_hashes(value, &mut programs);
                }
            }
        } else if bytes.starts_with(b"ATMC") {
            let program = crate::decode_program(&bytes)?;
            let mut dependencies = Vec::new();
            crate::program_bindings::collect_fixed_program_dependencies(
                &program.instructions,
                &mut dependencies,
            );
            programs.extend(dependencies);
        } else {
            let block = Block::decode(&id, &bytes)?;
            match block.kind {
                DATABASE_VALUE_ROOT_KIND => {
                    result
                        .values
                        .insert(id, DatabaseValueRoot::decode(&id, &bytes)?);
                }
                INDEX_DESCRIPTOR_KIND => {
                    let descriptor = IndexDescriptor::decode(&id, &bytes)?;
                    if deep {
                        for tree in descriptor
                            .trees
                            .iter()
                            .chain(descriptor.avet_work.iter().map(|w| &w.source))
                        {
                            crate::persistent_tree::validate_tree_streaming(tree, |id| {
                                reader.read_object(*id)
                            })?;
                        }
                    }
                    result
                        .indexes
                        .entry(descriptor.basis)
                        .or_default()
                        .insert(id);
                }
                SNAPSHOT_METADATA_KIND => {
                    let metadata = SnapshotMetadata::decode(&id, &bytes)?;
                    result
                        .metadata
                        .entry(metadata.basis)
                        .or_default()
                        .push(metadata);
                }
                RECEIPT_KIND => {
                    result.receipts.insert(id);
                }
                crate::storage::log::LOG_ENTRY_KIND => {
                    programs.extend(crate::storage::log::entry_program_links(
                        &mut reader,
                        &block,
                    )?);
                }
                _ => {}
            }
        }
    }
    // A hash-valid tree or log object in a Function slot is not an executable
    // definition. Preserve that typed edge check as well as graph reachability.
    for id in programs {
        crate::decode_program(&reader.read_object(id)?)?;
    }
    result.count = seen.len();
    Ok(result)
}

pub(crate) fn verify_read_point(
    directory: &Path,
    point: ReadPoint,
    deep: bool,
    control: &MaintenanceControl,
) -> Result<BackupVerification, SemanticError> {
    control.check()?;
    let publication = load_publication(directory, &point)?;
    let mut inventory = inventory(directory, &[point.point.manifest_hash], control, deep)?;
    let mut reader = Reader { directory, control };
    let endpoint = metadata_for(&mut reader, &point.value)?;
    if point.value.identity != publication.identity
        || point.value.basis != publication.basis
        || point.value.log != publication.log
        || point.value.metadata != publication.metadata
        || endpoint.generation != point.point.log_generation
    {
        return Err(fault(
            "backup/read-coordinate",
            "Read value differs from its committed publication",
        ));
    }
    let log = match publication.log {
        Some(id) => LogRoot::open(&mut reader, id)?,
        None => LogRoot::empty(),
    };
    if log.basis_t() != publication.basis {
        return Err(fault(
            "backup/log-basis",
            "Captured log does not reach publication basis",
        ));
    }
    if deep {
        // Selective prefix lookup assumes one coherent skip graph. An explicit
        // deep pass proves that alternative skip levels cannot select forks.
        log.validate_structure(&mut reader)?;
    }
    inventory
        .values
        .insert(point.value.id()?, point.value.clone());
    validate_authorization(&mut reader, &publication, &inventory)?;
    let mut values_by_basis = BTreeMap::<u64, Vec<DatabaseValueRoot>>::new();
    for value in inventory.values.values() {
        let metadata = metadata_for(&mut reader, value)?;
        if value.identity != publication.identity
            || value.basis > publication.basis
            || metadata.generation != endpoint.generation
            || value.log != log.prefix_hash(&mut reader, value.basis)?
        {
            return Err(fault(
                "backup/value-authority",
                "Retained value differs from the canonical generation or log prefix",
            ));
        }
        let id = value
            .indexes
            .ok_or_else(|| fault("backup/value-index", "Retained value has no indexes"))?;
        let index = IndexDescriptor::decode(&id, &reader.read_object(id)?)?;
        if index.identity != value.identity
            || index.generation != metadata.generation
            || index.basis > value.basis
        {
            return Err(fault(
                "backup/value-index",
                "Retained index does not belong to its value",
            ));
        }
        values_by_basis
            .entry(value.basis)
            .or_default()
            .push(value.clone());
    }
    validate_receipts(&mut reader, &publication, &log, &inventory)?;
    let mut database = Database::bootstrap()?;
    database.entity_origin =
        crate::entity_identity::DatabaseOrigin::durable(&point.point.lineage_id);
    check_basis(
        &mut reader,
        &database,
        &endpoint,
        &mut inventory,
        &mut values_by_basis,
        deep,
    )?;
    // One canonical forward replay. Captured receipt names are never invented
    // to justify allocation gaps; authenticated log frontiers are the proof.
    let mut previous = [0; 32];
    for basis in 1..=publication.basis {
        control.check()?;
        let record = log
            .read_record(&mut reader, basis)?
            .ok_or_else(|| fault("backup/log-gap", "Canonical log has a missing transaction"))?;
        let after = crate::reserved_allocation::ReservedAllocation::from_frontier(
            record.entry.reserved_frontier,
            record.entry.eidx_frontier,
        )?;
        let prior = database.reserved_allocation().ok_or_else(|| {
            fault(
                "backup/allocation",
                "Replay lost its authenticated allocation state",
            )
        })?;
        let transaction = DurableTransaction {
            database_id: point.point.lineage_id.clone(),
            basis_t: basis,
            previous_hash: previous,
            eidx_frontier: record.entry.eidx_frontier,
            tempids: BTreeMap::new(),
            tx_data: record.entry.tx_data,
        };
        database = database.apply_committed_with_allocation_frontiers(
            &transaction,
            prior,
            after,
            endpoint.generation != 0,
        )?;
        previous = record.id;
        check_basis(
            &mut reader,
            &database,
            &endpoint,
            &mut inventory,
            &mut values_by_basis,
            deep,
        )?;
    }
    if !inventory.indexes.is_empty()
        || !inventory.metadata.is_empty()
        || !values_by_basis.is_empty()
    {
        return Err(fault(
            "backup/future-value",
            "Repository graph retains a value beyond its captured basis",
        ));
    }
    database.validate_invariants()?;
    Ok(BackupVerification {
        point: point.point,
        database,
        objects_read: inventory.count,
    })
}

fn metadata_for(
    reader: &mut Reader<'_>,
    value: &DatabaseValueRoot,
) -> Result<SnapshotMetadata, SemanticError> {
    let id = value
        .metadata
        .ok_or_else(|| fault("backup/value-metadata", "Retained value has no metadata"))?;
    let metadata = SnapshotMetadata::decode(&id, &reader.read_object(id)?)?;
    if metadata.identity != value.identity || metadata.basis != value.basis {
        return Err(fault(
            "backup/value-metadata",
            "Retained value and metadata disagree",
        ));
    }
    Ok(metadata)
}

fn validate_authorization(
    reader: &mut Reader<'_>,
    publication: &DatabaseRoot,
    inventory: &Inventory,
) -> Result<(), SemanticError> {
    use crate::storage::read_authorization::{IndexAuthorization, ReadAuthorization, index_key};
    let id = publication.read_authorization.ok_or_else(|| {
        fault(
            "backup/read-authorization",
            "Publication has no read authorization",
        )
    })?;
    let authorization = ReadAuthorization::decode(&id, &reader.read_object(id)?)?;
    let initial = inventory
        .values
        .get(&authorization.initial_value())
        .ok_or_else(|| {
            fault(
                "backup/read-authorization",
                "Initial read anchor is absent from the repository graph",
            )
        })?;
    if authorization.identity() != publication.identity
        || initial.identity != publication.identity
        || initial.basis != authorization.initial_basis()
    {
        return Err(fault(
            "backup/read-authorization",
            "Initial read authorization belongs to another committed value",
        ));
    }
    let registry = RequestIndex::from_root(Some(authorization.indexes_root()));
    let mut after = None;
    loop {
        let page = registry.scan(reader, after, 256)?;
        if page.is_empty() {
            break;
        }
        for (key, id) in &page {
            let witness = IndexAuthorization::decode(id, &reader.read_object(*id)?)?;
            if witness.identity != publication.identity
                || *key != index_key(&publication.identity, witness.index)
            {
                return Err(fault(
                    "backup/read-authorization",
                    "Index publication witness differs from its scoped registry key",
                ));
            }
            let index =
                IndexDescriptor::decode(&witness.index, &reader.read_object(witness.index)?)?;
            if index.identity != publication.identity || index.basis > publication.basis {
                return Err(fault(
                    "backup/read-authorization",
                    "Authorized index differs from its canonical publication",
                ));
            }
        }
        after = page.last().map(|entry| entry.0);
    }
    Ok(())
}

fn validate_receipts(
    reader: &mut Reader<'_>,
    publication: &DatabaseRoot,
    log: &LogRoot,
    inventory: &Inventory,
) -> Result<(), SemanticError> {
    let requests = RequestIndex::from_root(publication.receipts);
    let mut mapped = BTreeSet::new();
    let mut after = None;
    loop {
        let page = requests.scan(reader, after, 256)?;
        if page.is_empty() {
            break;
        }
        for (_, id) in &page {
            let bytes = reader.read_object(*id)?;
            let block = Block::decode(id, &bytes)?;
            if block.kind == crate::storage::excision::TOMBSTONE_KIND {
                let error = crate::storage::excision::reject_tombstone_bytes(
                    *id,
                    &bytes,
                    &publication.identity,
                )
                .unwrap_err();
                if error.code != "postgres/idempotency-predates-excision" {
                    return Err(error);
                }
                continue;
            }
            if block.kind != RECEIPT_KIND {
                return Err(fault(
                    "backup/request-outcome",
                    "Request index points to an invalid outcome",
                ));
            }
            if !mapped.insert(*id) {
                continue;
            }
            let receipt = ExactReceipt::load_from_bytes(reader, *id, &bytes)?;
            let before = inventory
                .values
                .get(&receipt.before)
                .ok_or_else(|| fault("backup/receipt-value", "Receipt before value is absent"))?;
            let after_value = inventory
                .values
                .get(&receipt.after)
                .ok_or_else(|| fault("backup/receipt-value", "Receipt after value is absent"))?;
            if receipt.identity != publication.identity
                || before.identity != receipt.identity
                || after_value.identity != receipt.identity
                || before.basis.checked_add(1) != Some(receipt.basis)
                || after_value.basis != receipt.basis
            {
                return Err(fault(
                    "backup/receipt-coordinate",
                    "Receipt values do not describe one canonical transaction",
                ));
            }
            let record = log
                .read_record(reader, receipt.basis)?
                .ok_or_else(|| fault("backup/receipt-log", "Receipt transaction is missing"))?;
            if record.id != receipt.transaction {
                return Err(fault(
                    "backup/receipt-log",
                    "Receipt transaction differs from canonical log",
                ));
            }
            crate::storage::receipts::validate_receipt_frontiers(
                &receipt.tempids,
                receipt.basis,
                record.entry.eidx_frontier,
                record.entry.reserved_frontier,
            )?;
            if requests.lookup(reader, basis_receipt_key(&receipt.identity, receipt.basis))?
                != Some(*id)
            {
                return Err(fault(
                    "backup/receipt-address",
                    "Receipt has no matching canonical basis address",
                ));
            }
        }
        after = page.last().map(|entry| entry.0);
    }
    if mapped != inventory.receipts {
        return Err(fault(
            "backup/receipt-map",
            "Reachable receipt is not a current canonical request outcome",
        ));
    }
    Ok(())
}

fn check_basis(
    reader: &mut Reader<'_>,
    database: &Database,
    endpoint: &SnapshotMetadata,
    inventory: &mut Inventory,
    values: &mut BTreeMap<u64, Vec<DatabaseValueRoot>>,
    deep: bool,
) -> Result<(), SemanticError> {
    let basis = database.basis_t();
    for metadata in inventory.metadata.remove(&basis).unwrap_or_default() {
        if metadata.identity != endpoint.identity
            || metadata.generation != endpoint.generation
            || metadata.eidx_frontier != database.eidx_frontier()
            || Some(metadata.reserved_frontier)
                != database.reserved_allocation().map(|a| a.frontier())
            || metadata.last_tx_instant != database.last_tx_instant()
        {
            return Err(fault(
                "backup/metadata-semantic-mismatch",
                "Read metadata differs from its canonical replay checkpoint",
            ));
        }
    }
    values.remove(&basis);
    for id in inventory.indexes.remove(&basis).unwrap_or_default() {
        let index = IndexDescriptor::decode(&id, &reader.read_object(id)?)?;
        if index.identity != endpoint.identity || index.generation != endpoint.generation {
            return Err(fault(
                "backup/index-generation",
                "Retained index belongs to another generation",
            ));
        }
        if deep {
            verify_index(reader, database, &index)?;
        }
    }
    Ok(())
}

fn read_tree(
    reader: &mut Reader<'_>,
    descriptor: &TreeDescriptor,
) -> Result<Vec<Datom>, SemanticError> {
    let mut datoms = Vec::new();
    crate::persistent_tree::validate_tree_streaming(descriptor, |id| {
        let bytes = reader.read_object(*id)?;
        if let TreeNode::Leaf(leaf) = crate::persistent_tree::decode_tree_node(id, &bytes)? {
            for row in 0..leaf.len() {
                datoms.push(leaf.datom(row).expect("decoded leaf columns"));
            }
        }
        Ok(bytes)
    })?;
    datoms.sort_by(|a, b| a.cmp_in(b, descriptor.order));
    Ok(datoms)
}

fn verify_index(
    reader: &mut Reader<'_>,
    database: &Database,
    index: &IndexDescriptor,
) -> Result<(), SemanticError> {
    let current = database.datoms(View::Current, IndexOrder::Eavt);
    let replayed = database.datoms(View::History, IndexOrder::Eavt);
    let physical = read_tree(reader, &index.trees[4])?;
    validate_physical_history_projection(&replayed, &physical, database.basis_t())
        .map_err(|e| fault("backup/tree-semantic-mismatch", e.message))?;
    for tree in &index.trees {
        reader.control.check()?;
        if tree.history && tree.order == IndexOrder::Eavt {
            continue;
        }
        let source = if tree.history { &physical } else { &current };
        let mut expected = derive_index_projection(database, source, tree.order)?;
        let mut actual = read_tree(reader, tree)?;
        if tree.order == IndexOrder::Avet {
            let pending =
                verify_pending(reader, index, database, &actual, &replayed, tree.history)?;
            expected.retain(|d| !pending.contains(&d.attribute));
            actual.retain(|d| !pending.contains(&d.attribute));
        }
        if !same_stored_datoms(&actual, &expected) {
            return Err(fault(
                "backup/tree-semantic-mismatch",
                "Covering tree disagrees with canonical replay",
            )
            .detail("basis_t", database.basis_t().to_string())
            .detail("order", format!("{:?}", tree.order))
            .detail("history", tree.history.to_string()));
        }
    }
    if index.fulltext.is_some() {
        verify_fulltext(reader, database, index, &physical)?;
    }
    Ok(())
}

fn verify_fulltext(
    reader: &mut Reader<'_>,
    database: &Database,
    index: &IndexDescriptor,
    history: &[Datom],
) -> Result<(), SemanticError> {
    let projection =
        crate::storage::fulltext::load_projection(index, &mut |id| reader.read_object(id))?;
    let mut source = history.to_vec();
    source.sort_by(|a, b| a.cmp_in(b, IndexOrder::Aevt));
    let records = crate::fulltext::records_from(database.schema(), |attribute| {
        let first = source.partition_point(|d| d.attribute < attribute);
        let end = source.partition_point(|d| d.attribute <= attribute);
        Ok(Box::new(source[first..end].iter().cloned().map(Ok)))
    });
    let limits = crate::FulltextBuildLimits {
        max_records: u64::MAX,
        max_spill_bytes: u64::MAX,
        ..Default::default()
    };
    let mut expected = crate::fulltext_store::sorted_records(
        records.map(|r| {
            reader.control.check()?;
            r
        }),
        &limits,
        &mut Default::default(),
    )?;
    let directory = reader.directory.to_path_buf();
    let control = reader.control.clone();
    let mut observed = crate::FulltextCursor::new(
        &projection,
        &[],
        crate::FulltextReadLimits {
            max_records: u64::MAX,
            max_block_bytes: u64::MAX,
        },
        Box::new(move |id, stats| {
            control.check()?;
            let (page, blocks, bytes) = crate::storage::fulltext::read_page(id, &mut |id| {
                control.check()?;
                read_object(&directory, id)
            })?;
            stats.blocks_read = stats.blocks_read.saturating_add(blocks);
            stats.block_bytes = stats.block_bytes.saturating_add(bytes);
            Ok(std::sync::Arc::new(page))
        }),
    )?;
    loop {
        reader.control.check()?;
        let expected = expected.next().transpose()?;
        let actual = observed.next().transpose()?;
        if expected != actual {
            return Err(fault(
                "backup/fulltext-semantic-mismatch",
                "Search records disagree with their canonical history source",
            ));
        }
        if expected.is_none() {
            return Ok(());
        }
    }
}

fn verify_pending(
    reader: &mut Reader<'_>,
    index: &IndexDescriptor,
    database: &Database,
    actual: &[Datom],
    replayed: &[Datom],
    history: bool,
) -> Result<BTreeSet<u32>, SemanticError> {
    let mut pending = BTreeSet::new();
    for attribute in &index.pending_avet {
        if !crate::index_support::effective_avet(database.schema(), *attribute) {
            return Err(fault(
                "backup/avet-direction",
                "Pending AVET attribute is not indexed in its frozen schema",
            ));
        }
        if !index.avet_work.iter().any(|w| w.attribute == *attribute) {
            pending.insert(*attribute);
        }
    }
    for work in &index.avet_work {
        if work.adding != crate::index_support::effective_avet(database.schema(), work.attribute)
            || work.source != index.trees[usize::from(work.history) * 4 + 1]
        {
            return Err(fault(
                "backup/avet-source",
                "AVET work disagrees with frozen schema or AEVT source",
            ));
        }
        if work.history && !history {
            continue;
        }
        pending.insert(work.attribute);
        if work.history == history && !work.clearing {
            let expected = projection_prefix(reader, work)?;
            let observed = actual
                .iter()
                .filter(|d| d.attribute == work.attribute)
                .cloned()
                .collect::<Vec<_>>();
            if !same_stored_datoms(&observed, &expected) {
                return Err(fault(
                    "backup/avet-prefix",
                    "Pending AVET copy is not its exact authenticated source prefix",
                ));
            }
        }
    }
    // Clearing/readiness-only ranges may retain old facts, but never fabricated
    // facts. Copy prefixes above receive the stronger exact-position proof.
    for datom in actual.iter().filter(|d| pending.contains(&d.attribute)) {
        let at =
            replayed.partition_point(|candidate| candidate.cmp_in(datom, IndexOrder::Eavt).is_lt());
        if (!history && !datom.added)
            || !replayed[at..]
                .iter()
                .take_while(|row| row.cmp_in(datom, IndexOrder::Eavt).is_eq())
                .any(|row| {
                    same_stored_datoms(std::slice::from_ref(row), std::slice::from_ref(datom))
                })
        {
            return Err(fault(
                "backup/avet-fabricated",
                "Pending AVET contains a fact absent from canonical history",
            ));
        }
    }
    Ok(pending)
}

fn projection_prefix(
    reader: &mut Reader<'_>,
    work: &crate::storage::BlockAvetWork,
) -> Result<Vec<Datom>, SemanticError> {
    let bytes = reader.read_object(work.source.root_hash)?;
    let TreeNode::Root(root) =
        crate::persistent_tree::decode_tree_node(&work.source.root_hash, &bytes)?
    else {
        return Err(fault("backup/avet-root", "AVET source is not a root"));
    };
    let mut output = Vec::new();
    let mut coordinate_found = false;
    for (directory_index, child) in root.directories.iter().enumerate() {
        if directory_index > work.directory as usize {
            break;
        }
        let bytes = reader.read_object(child.hash)?;
        let TreeNode::Directory(directory) =
            crate::persistent_tree::decode_tree_node(&child.hash, &bytes)?
        else {
            return Err(fault(
                "backup/avet-directory",
                "AVET source directory is invalid",
            ));
        };
        for (leaf_index, child) in directory.leaves.iter().enumerate() {
            if directory_index == work.directory as usize && leaf_index > work.leaf as usize {
                break;
            }
            let bytes = reader.read_object(child.hash)?;
            let TreeNode::Leaf(leaf) =
                crate::persistent_tree::decode_tree_node(&child.hash, &bytes)?
            else {
                return Err(fault("backup/avet-leaf", "AVET source leaf is invalid"));
            };
            let boundary =
                directory_index == work.directory as usize && leaf_index == work.leaf as usize;
            let count = if boundary {
                coordinate_found = (work.slot as usize) < leaf.len();
                (work.slot as usize).min(leaf.len())
            } else {
                leaf.len()
            };
            for slot in 0..count {
                let datom = leaf.datom(slot).expect("decoded leaf columns");
                if datom.attribute == work.attribute {
                    output.push(datom);
                }
            }
        }
    }
    // A freshly started population phase has no copied facts and position 0.
    if !(coordinate_found
        || work.directory == 0 && work.leaf == 0 && work.slot == 0 && root.directories.is_empty())
    {
        return Err(fault(
            "backup/avet-position",
            "AVET continuation lies outside its authenticated source",
        ));
    }
    output.sort_by(|a, b| a.cmp_in(b, IndexOrder::Avet));
    Ok(output)
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::backup::repository;

    fn put(directory: &Path, bytes: &[u8]) -> ObjectId {
        repository::put_object(directory, bytes).unwrap().0
    }
    fn genesis_point(directory: &Path, wrong_tree: bool, wrong_frontier: bool) -> ReadPoint {
        repository::admit(directory, true).unwrap();
        let identity = [7; 16];
        repository::claim(directory, identity).unwrap();
        let database = Database::bootstrap().unwrap();
        let mut trees = Vec::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let built = crate::persistent_tree::build_tree(
                    order,
                    history,
                    database.datoms(
                        if history {
                            View::History
                        } else {
                            View::Current
                        },
                        order,
                    ),
                    &Default::default(),
                )
                .unwrap();
                for (_, bytes) in built.nodes.iter() {
                    put(directory, bytes);
                }
                trees.push(built.descriptor);
            }
        }
        let mut index = IndexDescriptor {
            identity,
            basis: 0,
            generation: 0,
            trees,
            pending_avet: vec![],
            avet_work: vec![],
            fulltext: None,
        };
        let metadata = SnapshotMetadata {
            identity,
            basis: 0,
            generation: 0,
            eidx_frontier: database.eidx_frontier() + u64::from(wrong_frontier),
            reserved_frontier: database.reserved_allocation().unwrap().frontier(),
            last_tx_instant: None,
            excision: None,
        };
        let mut publication = DatabaseRoot {
            identity,
            basis: 0,
            writer_epoch: 0,
            log: None,
            indexes: Some(put(directory, &index.encode().unwrap())),
            receipts: None,
            metadata: Some(put(directory, &metadata.encode().unwrap())),
            read_authorization: None,
        };
        // Encode the current one-entry immutable registry without creating a
        // live store merely for a file-only verification fixture.
        let initial = DatabaseValueRoot::from(&publication);
        let initial_id = put(directory, &initial.encode().unwrap());
        let index_id = publication.indexes.unwrap();
        let mut witness_payload = identity.to_vec();
        witness_payload.extend_from_slice(&1u64.to_be_bytes());
        let witness = put(
            directory,
            &Block {
                kind: crate::storage::read_authorization::INDEX_AUTHORIZATION_KIND,
                links: vec![index_id],
                payload: witness_payload,
            }
            .encode()
            .unwrap(),
        );
        let registry = put(
            directory,
            &Block {
                kind: crate::storage::receipts::REQUEST_LEAF_KIND,
                links: vec![witness],
                payload: crate::storage::read_authorization::index_key(&identity, index_id)
                    .to_vec(),
            }
            .encode()
            .unwrap(),
        );
        let mut authorization_payload = identity.to_vec();
        authorization_payload.extend_from_slice(&0u64.to_be_bytes());
        publication.read_authorization = Some(put(
            directory,
            &Block {
                kind: crate::storage::read_authorization::READ_AUTHORIZATION_KIND,
                links: vec![initial_id, registry],
                payload: authorization_payload,
            }
            .encode()
            .unwrap(),
        ));
        if wrong_tree {
            // Corrupt a covering projection that is not used to derive schema
            // during ordinary open; the deep semantic comparator must catch it.
            let mut missing = database.datoms(View::Current, IndexOrder::Vaet);
            assert!(missing.pop().is_some());
            let built = crate::persistent_tree::build_tree(
                IndexOrder::Vaet,
                false,
                missing,
                &Default::default(),
            )
            .unwrap();
            for (_, bytes) in built.nodes.iter() {
                put(directory, bytes);
            }
            index.trees[3] = built.descriptor;
            publication.indexes = Some(put(directory, &index.encode().unwrap()));
        }
        let publication_id = put(directory, &publication.encode().unwrap());
        let mut payload = identity.to_vec();
        payload.extend_from_slice(&0u64.to_be_bytes());
        payload.extend_from_slice(&0u64.to_be_bytes());
        let manifest = Block {
            kind: crate::backup::POINT_KIND,
            links: vec![publication_id],
            payload,
        }
        .encode()
        .unwrap();
        put(directory, &manifest);
        repository::publish_point(directory, 0, 0, &manifest).unwrap();
        crate::backup::load_selected(directory, Some(0), 0).unwrap()
    }

    #[test]
    fn deep_repository_verification_rejects_hash_valid_wrong_tree_and_frontier() {
        for (wrong_tree, wrong_frontier, code) in [
            (false, false, None),
            (true, false, Some("backup/tree-semantic-mismatch")),
            (false, true, Some("backup/metadata-semantic-mismatch")),
        ] {
            let temporary = tempfile::tempdir().unwrap();
            let directory = temporary.path().join("repository");
            let point = genesis_point(&directory, wrong_tree, wrong_frontier);
            assert!(
                walk_repository(
                    &directory,
                    &[point.point.manifest_hash],
                    &Default::default(),
                    false
                )
                .unwrap()
                    > 0
            );
            let result = verify_read_point(&directory, point, true, &Default::default());
            match code {
                Some(code) => assert_eq!(result.unwrap_err().code, code),
                None => assert_eq!(result.unwrap().database.basis_t(), 0),
            }
        }
    }

    #[test]
    fn repository_walk_checks_program_edge_type_and_cancellation() {
        let temporary = tempfile::tempdir().unwrap();
        let directory = temporary.path().join("repository");
        let point = genesis_point(&directory, false, false);
        let invalid = crate::Datom {
            entity: crate::t_to_tx(1).unwrap(),
            attribute: crate::DB_FN as u32,
            value: crate::Value::Function(point.publication),
            tx: crate::t_to_tx(1).unwrap(),
            added: true,
        };
        let tree = crate::persistent_tree::build_tree(
            IndexOrder::Eavt,
            false,
            vec![invalid],
            &Default::default(),
        )
        .unwrap();
        for (_, bytes) in tree.nodes.iter() {
            put(&directory, bytes);
        }
        assert!(
            walk_repository(
                &directory,
                &[tree.descriptor.root_hash],
                &Default::default(),
                false
            )
            .is_err()
        );
        let control = MaintenanceControl::default();
        control.cancel();
        assert_eq!(
            walk_repository(&directory, &[point.point.manifest_hash], &control, true)
                .unwrap_err()
                .category,
            ErrorCategory::Interrupted
        );
    }

    fn transaction_object(directory: &Path, report: &crate::TxReport) -> ObjectId {
        let mut datoms = report.tx_data.iter().collect::<Vec<_>>();
        datoms.sort_by(|a, b| a.cmp_in(b, IndexOrder::Eavt));
        let mut content = Vec::new();
        for datom in datoms {
            content.extend_from_slice(&crate::encoding::canonical_datom_bytes(datom).unwrap());
        }
        let mut payload = Vec::new();
        for number in [
            report.db_after.basis_t(),
            report.db_after.eidx_frontier(),
            report.db_after.reserved_allocation().unwrap().frontier(),
            report.tx_data.len() as u64,
            content.len() as u64,
        ] {
            payload.extend_from_slice(&number.to_be_bytes());
        }
        payload.extend_from_slice(&content);
        put(
            directory,
            &Block {
                kind: crate::storage::log::LOG_ENTRY_KIND,
                links: vec![],
                payload,
            }
            .encode()
            .unwrap(),
        )
    }
    fn short_log(directory: &Path, entries: Vec<ObjectId>, database: &Database) -> ObjectId {
        let mut payload = Vec::new();
        for number in [
            0,
            database.eidx_frontier(),
            database.reserved_allocation().unwrap().frontier(),
        ] {
            payload.extend_from_slice(&number.to_be_bytes());
        }
        payload.extend_from_slice(&(entries.len() as u16).to_be_bytes());
        payload.extend_from_slice(&[0, 0]);
        put(
            directory,
            &Block {
                kind: crate::storage::log::LOG_PAGE_KIND,
                links: entries,
                payload,
            }
            .encode()
            .unwrap(),
        )
    }
    fn retained_value(
        directory: &Path,
        database: &Database,
        log: ObjectId,
        wrong: bool,
    ) -> DatabaseValueRoot {
        let mut trees = Vec::new();
        for history in [false, true] {
            for order in [
                IndexOrder::Eavt,
                IndexOrder::Aevt,
                IndexOrder::Avet,
                IndexOrder::Vaet,
            ] {
                let mut datoms = database.datoms(
                    if history {
                        View::History
                    } else {
                        View::Current
                    },
                    order,
                );
                if wrong && !history && order == IndexOrder::Eavt {
                    datoms.pop();
                }
                let tree =
                    crate::persistent_tree::build_tree(order, history, datoms, &Default::default())
                        .unwrap();
                for (_, bytes) in tree.nodes.iter() {
                    put(directory, bytes);
                }
                trees.push(tree.descriptor);
            }
        }
        let indexes = IndexDescriptor {
            identity: [7; 16],
            basis: database.basis_t(),
            generation: 0,
            trees,
            pending_avet: vec![],
            avet_work: vec![],
            fulltext: None,
        };
        let metadata = SnapshotMetadata {
            identity: [7; 16],
            basis: database.basis_t(),
            generation: 0,
            eidx_frontier: database.eidx_frontier(),
            reserved_frontier: database.reserved_allocation().unwrap().frontier(),
            last_tx_instant: database.last_tx_instant(),
            excision: None,
        };
        DatabaseValueRoot {
            identity: [7; 16],
            basis: database.basis_t(),
            log: Some(log),
            indexes: Some(put(directory, &indexes.encode().unwrap())),
            metadata: Some(put(directory, &metadata.encode().unwrap())),
        }
    }

    #[test]
    fn deep_verification_checks_earlier_receipt_trees_and_map_coordinates() {
        for (wrong_before_tree, wrong_map, wrong_before_basis, error) in [
            (false, false, false, None),
            (true, false, false, Some("backup/tree-semantic-mismatch")),
            (false, true, false, Some("backup/receipt-address")),
            (false, false, true, Some("backup/receipt-coordinate")),
        ] {
            let temporary = tempfile::tempdir().unwrap();
            let directory = temporary.path().join("repository");
            let genesis = genesis_point(&directory, false, false);
            let original = load_publication(&directory, &genesis).unwrap();
            let first = Database::bootstrap().unwrap().with(&[], 10).unwrap();
            let second = first.db_after.with(&[], 20).unwrap();
            let one = transaction_object(&directory, &first);
            let two = transaction_object(&directory, &second);
            let log_one = short_log(&directory, vec![one], &first.db_after);
            let log_two = short_log(&directory, vec![one, two], &second.db_after);
            let before = if wrong_before_basis {
                genesis.value.clone()
            } else {
                retained_value(&directory, &first.db_after, log_one, wrong_before_tree)
            };
            let after = retained_value(&directory, &second.db_after, log_two, false);
            let before_id = put(&directory, &before.encode().unwrap());
            let after_id = put(&directory, &after.encode().unwrap());
            let mut payload = [7; 16].to_vec();
            payload.extend_from_slice(&[5; 32]);
            payload.extend_from_slice(&2u64.to_be_bytes());
            payload.extend_from_slice(&4u64.to_be_bytes());
            payload.extend_from_slice(&0u32.to_be_bytes());
            let receipt = put(
                &directory,
                &Block {
                    kind: RECEIPT_KIND,
                    links: vec![before_id, after_id, two],
                    payload,
                }
                .encode()
                .unwrap(),
            );
            let key = basis_receipt_key(&[7; 16], if wrong_map { 3 } else { 2 });
            let receipts = put(
                &directory,
                &Block {
                    kind: crate::storage::receipts::REQUEST_LEAF_KIND,
                    links: vec![receipt],
                    payload: key.to_vec(),
                }
                .encode()
                .unwrap(),
            );
            let publication = DatabaseRoot {
                identity: [7; 16],
                basis: 2,
                writer_epoch: 0,
                log: after.log,
                indexes: after.indexes,
                receipts: Some(receipts),
                metadata: after.metadata,
                read_authorization: original.read_authorization,
            };
            let publication_id = put(&directory, &publication.encode().unwrap());
            let mut payload = [7; 16].to_vec();
            payload.extend_from_slice(&0u64.to_be_bytes());
            payload.extend_from_slice(&2u64.to_be_bytes());
            let manifest = Block {
                kind: crate::backup::POINT_KIND,
                links: vec![publication_id],
                payload,
            }
            .encode()
            .unwrap();
            put(&directory, &manifest);
            repository::publish_point(&directory, 0, 2, &manifest).unwrap();
            let point = crate::backup::load_selected(&directory, Some(0), 2).unwrap();
            let result = verify_read_point(&directory, point, true, &Default::default());
            match error {
                None => assert_eq!(result.unwrap().database.basis_t(), 2),
                Some(code) => assert_eq!(result.unwrap_err().code, code),
            }
        }
    }

    #[test]
    fn pending_avet_requires_real_history_and_the_exact_copy_prefix() {
        use crate::{Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, Value, ValueType};
        let temporary = tempfile::tempdir().unwrap();
        let directory = temporary.path().join("repository");
        repository::admit(&directory, true).unwrap();
        let mut schema = Schema::new();
        let mut attribute = Attribute::new(
            1000,
            Keyword::new("pending", "value"),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.indexed = true;
        schema.install(attribute).unwrap();
        let database = Database::new(schema)
            .unwrap()
            .with(
                &(0..3)
                    .map(|n| TxOp::Add {
                        entity: EntityRef::Temp(format!("e{n}")),
                        attribute: 1000,
                        value: Value::Long(n).into(),
                    })
                    .collect::<Vec<_>>(),
                1000,
            )
            .unwrap()
            .db_after;
        let value = retained_value(&directory, &database, [1; 32], false);
        let id = value.indexes.unwrap();
        let original = IndexDescriptor::decode(&id, &read_object(&directory, id).unwrap()).unwrap();
        let control = MaintenanceControl::default();
        let mut reader = Reader {
            directory: &directory,
            control: &control,
        };
        let replayed = database.datoms(View::History, IndexOrder::Eavt);
        for history in [false, true] {
            let mut index = original.clone();
            let source = index.trees[usize::from(history) * 4 + 1].clone();
            let mut position = None;
            let TreeNode::Root(root) = crate::persistent_tree::decode_tree_node(
                &source.root_hash,
                &reader.read_object(source.root_hash).unwrap(),
            )
            .unwrap() else {
                unreachable!()
            };
            for (directory_index, child) in root.directories.iter().enumerate() {
                let TreeNode::Directory(dir) = crate::persistent_tree::decode_tree_node(
                    &child.hash,
                    &reader.read_object(child.hash).unwrap(),
                )
                .unwrap() else {
                    unreachable!()
                };
                for (leaf_index, child) in dir.leaves.iter().enumerate() {
                    let TreeNode::Leaf(leaf) = crate::persistent_tree::decode_tree_node(
                        &child.hash,
                        &reader.read_object(child.hash).unwrap(),
                    )
                    .unwrap() else {
                        unreachable!()
                    };
                    for slot in 0..leaf.len() {
                        let datom = leaf.datom(slot).unwrap();
                        if datom.attribute == 1000 && datom.value == Value::Long(1) {
                            position =
                                Some((directory_index as u32, leaf_index as u32, slot as u32));
                        }
                    }
                }
            }
            let (directory_index, leaf, slot) = position.unwrap();
            index.pending_avet = vec![1000];
            index.avet_work = vec![crate::storage::BlockAvetWork {
                attribute: 1000,
                adding: true,
                history,
                clearing: true,
                directory: directory_index,
                leaf,
                slot,
                source,
            }];
            let actual =
                read_tree(&mut reader, &index.trees[usize::from(history) * 4 + 2]).unwrap();
            verify_pending(&mut reader, &index, &database, &actual, &replayed, history).unwrap();
            let mut forged = actual.clone();
            forged
                .iter_mut()
                .find(|d| d.attribute == 1000)
                .unwrap()
                .value = Value::Long(99);
            assert_eq!(
                verify_pending(&mut reader, &index, &database, &forged, &replayed, history)
                    .unwrap_err()
                    .code,
                "backup/avet-fabricated"
            );
            index.avet_work[0].clearing = false;
            let prefix = projection_prefix(&mut reader, &index.avet_work[0]).unwrap();
            assert_eq!(prefix.len(), 1);
            verify_pending(&mut reader, &index, &database, &prefix, &replayed, history).unwrap();
            index.avet_work[0].slot = 0;
            assert_eq!(
                verify_pending(&mut reader, &index, &database, &prefix, &replayed, history)
                    .unwrap_err()
                    .code,
                "backup/avet-prefix"
            );
        }
    }
}
