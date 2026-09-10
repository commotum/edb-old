//! Explicit terminal database collection. Public names are never accepted as
//! destructive authority: every request supplies the issued storage ID/lineage.
use super::*;

pub const MAX_DATABASE_RECLAMATION_ROWS: usize = 512;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct RetiredDatabaseReclamation {
    pub storage_id: String,
    pub lineage_id: String,
    pub phase: u32,
    pub rows_selected: u64,
    pub rows_removed: u64,
    /// Advisory liveness probes, not a claim that all metadata work is constant.
    pub pins_checked: u64,
    pub complete: bool,
    pub applied: bool,
}

impl PostgresOperator {
    /// Preview one dependency-ordered batch without deleting or updating rows.
    /// Existing connected values/builders/backups can return Busy. Metadata
    /// liveness inspection is separate from the bounded selected-row count.
    pub fn preview_retired_database_reclamation(
        &mut self,
        storage_id: &str,
        lineage: &str,
        older_than: Duration,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        self.database_reclamation(storage_id, lineage, older_than, false)
    }

    /// Apply one resumable terminal batch. The issued identity tombstone is
    /// permanent; shared storage is deleted only after its last reference.
    pub fn reclaim_retired_database(
        &mut self,
        storage_id: &str,
        lineage: &str,
        older_than: Duration,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        self.database_reclamation(storage_id, lineage, older_than, true)
    }

    fn database_reclamation(
        &mut self,
        storage_id: &str,
        lineage: &str,
        older_than: Duration,
        apply: bool,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        let age = garbage_age_millis(older_than)?;
        let row = self
            .client
            .query_one(
                "SELECT phase, rows_selected, rows_removed, pins_checked, complete \
             FROM atomic_reclaim_retired_database($1,$2,$3,$4,$5)",
                &[
                    &storage_id,
                    &lineage,
                    &age,
                    &(MAX_DATABASE_RECLAMATION_ROWS as i64),
                    &apply,
                ],
            )
            .map_err(|error| operation_error("operations/database-reclamation", error))?;
        Ok(RetiredDatabaseReclamation {
            storage_id: storage_id.to_owned(),
            lineage_id: lineage.to_owned(),
            phase: u32::try_from(row.get::<_, i32>(0)).map_err(|_| {
                SemanticError::new(
                    crate::ErrorCategory::Fault,
                    "operations/reclamation-phase",
                    "database reclamation returned a negative phase",
                )
            })?,
            rows_selected: positive_or_zero(row.get(1), "database reclamation selection")?,
            rows_removed: positive_or_zero(row.get(2), "database reclamation removal")?,
            pins_checked: positive_or_zero(row.get(3), "database reclamation pin probes")?,
            complete: row.get(4),
            applied: apply,
        })
    }
}
