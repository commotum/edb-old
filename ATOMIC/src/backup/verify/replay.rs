//! Canonical replay retains authenticated forward navigation between transactions.
use super::*;

pub(super) fn canonical_log<R: ObjectReader>(
    log: &LogRoot,
    reader: &mut R,
    lineage: &str,
    generation: u64,
    control: &MaintenanceControl,
    database: &mut Database,
    mut check_basis: impl FnMut(&mut R, &Database) -> Result<(), SemanticError>,
) -> Result<(), SemanticError> {
    // ATOMIC-NOTE: keep the reader-independent traversal while checking retained
    // values between transactions. Reseeking the tail for every basis discards
    // the log owner's authenticated forward anchors. Sparse receipt/prefix
    // authority checks remain point reads; this is not a whole-verifier bound.
    let mut traversal = log.traversal(1, log.basis_t() + 1)?;
    let mut previous = [0; 32];
    for basis in 1..=log.basis_t() {
        control.check()?;
        let record = traversal
            .next_record(reader)
            .transpose()?
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
        // Caller tempid names cannot justify allocation gaps: authenticated
        // log frontiers, not captured receipt names, are the replay proof.
        let transaction = DurableTransaction {
            database_id: lineage.to_owned(),
            basis_t: basis,
            previous_hash: previous,
            eidx_frontier: record.entry.eidx_frontier,
            tempids: BTreeMap::new(),
            tx_data: record.entry.tx_data,
        };
        *database = database.apply_committed_with_allocation_frontiers(
            &transaction,
            prior,
            after,
            generation != 0,
        )?;
        previous = record.id;
        check_basis(reader, database)?;
    }
    Ok(())
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::storage::ObjectWriter;
    use crate::storage::log::{LOG_PAGE_ENTRIES, LOG_PAGE_KIND, LogEntry};

    #[derive(Default)]
    struct Memory {
        objects: BTreeMap<ObjectId, Vec<u8>>,
        pages: BTreeMap<ObjectId, usize>,
    }
    impl ObjectReader for Memory {
        fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
            let bytes = self
                .objects
                .get(&id)
                .ok_or_else(|| fault("backup/test-missing", "Missing fixture object"))?;
            if Block::decode(&id, bytes)?.kind == LOG_PAGE_KIND {
                *self.pages.entry(id).or_default() += 1;
            }
            Ok(bytes.clone())
        }
    }
    impl ObjectWriter for Memory {
        fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError> {
            let id = crate::sha256(bytes);
            self.objects.insert(id, bytes.to_vec());
            Ok(id)
        }
        fn flush_objects(&mut self) -> Result<(), SemanticError> {
            Ok(())
        }
    }
    fn fixture() -> (Memory, LogRoot, Database) {
        let mut store = Memory::default();
        let mut log = LogRoot::empty();
        let mut database = Database::bootstrap().unwrap();
        for basis in 1..=(8 * LOG_PAGE_ENTRIES + 3) as u64 {
            let report = database.with(&[], basis as i64).unwrap();
            log = log
                .append(
                    &mut store,
                    &LogEntry {
                        basis_t: basis,
                        eidx_frontier: report.db_after.eidx_frontier(),
                        reserved_frontier: report
                            .db_after
                            .reserved_allocation()
                            .unwrap()
                            .frontier(),
                        tx_data: report.tx_data,
                    },
                )
                .unwrap();
            database = report.db_after;
        }
        store.pages.clear();
        (store, log, database)
    }

    #[test]
    fn canonical_replay_reads_each_navigation_page_once_not_once_per_transaction() {
        let (mut store, log, expected) = fixture();
        // Red oracle: the exact former per-entry lookup re-reads navigation
        // pages. Keep it independent of production's traversal implementation.
        for basis in 1..=log.basis_t() {
            assert_eq!(
                log.read_record(&mut store, basis)
                    .unwrap()
                    .unwrap()
                    .entry
                    .basis_t,
                basis
            );
        }
        let reseek_reads = store.pages.values().sum::<usize>();
        assert!(store.pages.values().any(|count| *count > 1));
        store.pages.clear();
        let mut database = Database::bootstrap().unwrap();
        let mut visited = 0;
        canonical_log(
            &log,
            &mut store,
            "fixture",
            0,
            &MaintenanceControl::default(),
            &mut database,
            |_, database| {
                visited += 1;
                assert_eq!(database.basis_t(), visited);
                Ok(())
            },
        )
        .unwrap();
        assert_eq!(visited, log.basis_t());
        assert_eq!(
            database.datoms(View::History, IndexOrder::Eavt),
            expected.datoms(View::History, IndexOrder::Eavt)
        );
        assert_eq!(store.pages.len(), 8); // ninth page is the captured tail
        assert!(store.pages.values().all(|count| *count == 1));
        assert!(reseek_reads > store.pages.len());
        eprintln!(
            "BACKUP_REPLAY_PAGE_READS reseek={reseek_reads} forward={}",
            store.pages.len()
        );
    }

    #[test]
    fn canonical_replay_checks_cancel_between_bases_and_propagates_missing_objects() {
        let (mut store, log, _) = fixture();
        let control = MaintenanceControl::default();
        let mut database = Database::bootstrap().unwrap();
        let error = canonical_log(
            &log,
            &mut store,
            "fixture",
            0,
            &control,
            &mut database,
            |_, database| {
                if database.basis_t() == 65 {
                    control.cancel();
                }
                Ok(())
            },
        )
        .unwrap_err();
        assert_eq!(error.category, ErrorCategory::Interrupted);
        assert_eq!(database.basis_t(), 65);
        let missing = log.record_id(&mut store, 66).unwrap().unwrap();
        store.objects.remove(&missing);
        let mut database = Database::bootstrap().unwrap();
        let error = canonical_log(
            &log,
            &mut store,
            "fixture",
            0,
            &MaintenanceControl::default(),
            &mut database,
            |_, _| Ok(()),
        )
        .unwrap_err();
        assert_eq!(error.code, "backup/test-missing");
        assert_eq!(database.basis_t(), 65);
    }
}
