//! Explicit, resumable terminal collection against an issued ID and lineage.
use super::*;
#[path = "database_reclamation_phases.rs"]
mod phases;
use phases::{DETACH, SEEDS};
#[path = "database_reclamation_objects.rs"]
mod objects;
pub const MAX_DATABASE_RECLAMATION_ROWS: usize = 512;
#[derive(Clone, Debug, Eq, PartialEq)]
pub struct RetiredDatabaseReclamation {
    pub storage_id: String,
    pub lineage_id: String,
    pub phase: u32,
    pub rows_selected: u64,
    pub rows_removed: u64,
    pub rows_inserted: u64,
    pub rows_updated: u64,
    pub objects_read: u64,
    /// Metadata pin probes are separate work, not claimed constant.
    pub pins_checked: u64,
    pub complete: bool,
    pub applied: bool,
}
impl PostgresOperator {
    /// Preview without changing SQL rows; liveness locks are still checked.
    pub fn preview_retired_database_reclamation(
        &mut self,
        id: &str,
        lineage: &str,
        age: Duration,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        self.database_reclamation(id, lineage, age, false)
    }
    /// One bounded data/frontier batch plus fixed progress bookkeeping.
    pub fn reclaim_retired_database(
        &mut self,
        id: &str,
        lineage: &str,
        age: Duration,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        self.database_reclamation(id, lineage, age, true)
    }
    fn database_reclamation(
        &mut self,
        id: &str,
        lineage: &str,
        age: Duration,
        apply: bool,
    ) -> Result<RetiredDatabaseReclamation, SemanticError> {
        let age = garbage_age_millis(age)?;
        let mut tx = self.client.transaction().map_err(sql_error)?;
        let row = tx
            .query_one(
                "SELECT phase,complete FROM atomic_prepare_database_reclamation($1,$2,$3,$4)",
                &[&id, &lineage, &age, &apply],
            )
            .map_err(sql_error)?;
        let mut r = RetiredDatabaseReclamation {
            storage_id: id.into(),
            lineage_id: lineage.into(),
            phase: row.get::<_, i32>(0) as u32,
            rows_selected: 0,
            rows_removed: 0,
            rows_inserted: 0,
            rows_updated: 0,
            objects_read: 0,
            pins_checked: 0,
            complete: row.get(1),
            applied: apply,
        };
        if !r.complete {
            lock_storage(&mut tx, id, &mut r)?;
            advance(&mut tx, id, lineage, &mut r)?;
            if apply {
                tx.execute("UPDATE atomic_database_reclamation_progress SET phase=$2,active_backend=NULL,active_xid=NULL WHERE database_id=$1",&[&id,&(r.phase as i32)]).map_err(sql_error)?;
            }
        }
        tx.commit().map_err(sql_error)?;
        Ok(r)
    }
}
fn sql_error(e: postgres::Error) -> SemanticError {
    operation_error("operations/database-reclamation", e)
}
fn busy(s: &str) -> SemanticError {
    SemanticError::new(
        crate::ErrorCategory::Busy,
        "operations/database-reclamation-busy",
        s,
    )
}
fn lock_storage<C: GenericClient>(
    c: &mut C,
    id: &str,
    r: &mut RetiredDatabaseReclamation,
) -> Result<(), SemanticError> {
    // The final phase retains only the issued identity; canonical pin-key
    // helpers intentionally reject an already removed database locator.
    if !c
        .query_one(
            "SELECT EXISTS(SELECT 1 FROM atomic_databases WHERE database_id=$1)",
            &[&id],
        )
        .map_err(sql_error)?
        .get::<_, bool>(0)
    {
        return Ok(());
    }
    // Discovery/metadata release touches only this retired target. Its pins,
    // retained roots, SQL DAG edges, and persistent frontier barriers prevent
    // an ordinary collector from erasing unexpanded descendants. Do not take
    // global projection-writer fences merely to inspect that ownership.
    let mut keys = Vec::new();
    if let Some(key) = c
        .query_one("SELECT atomic_tree_database_build_pin_key($1)", &[&id])
        .map_err(sql_error)?
        .get::<_, Option<i64>>(0)
    {
        keys.push(key);
    }
    for row in c.query("SELECT atomic_log_generation_pin_key($1,generation) FROM (SELECT 0::bigint AS generation UNION SELECT generation FROM atomic_log_generations WHERE database_id=$1) g",&[&id]).map_err(sql_error)?{if let Some(key)=row.get::<_,Option<i64>>(0){keys.push(key);}}
    for row in c.query("SELECT manifest_hash FROM atomic_tree_manifests WHERE database_id=$1 UNION SELECT manifest_hash FROM atomic_request_base_archives WHERE database_id=$1 UNION SELECT manifest_hash FROM atomic_tree_build_intents WHERE database_id=$1",&[&id]).map_err(sql_error)?{let hash=digest(row.get(0),"reclaimed manifest")?;keys.push(tree_manifest_advisory_key(&hash));keys.push(crate::tree_store::tree_build_advisory_key(&hash));}
    for key in keys {
        r.pins_checked += 1;
        if !c
            .query_one("SELECT pg_try_advisory_xact_lock($1)", &[&key])
            .map_err(sql_error)?
            .get::<_, bool>(0)
        {
            return Err(busy(
                "an immutable value, backup, or builder still pins retired storage",
            ));
        }
    }
    Ok(())
}
fn advance<C: GenericClient>(
    c: &mut C,
    id: &str,
    lineage: &str,
    r: &mut RetiredDatabaseReclamation,
) -> Result<(), SemanticError> {
    loop {
        let phase = r.phase as usize;
        if phase < SEEDS.len() {
            let (kind, source) = SEEDS[phase];
            let sql = format!(
                "SELECT DISTINCT hash FROM ({source}) s WHERE NOT EXISTS(SELECT 1 FROM atomic_database_reclamation_objects o WHERE o.database_id=$1 AND o.kind=$2 AND o.object_hash=s.hash) ORDER BY hash LIMIT $3"
            );
            let rows = c
                .query(&sql, &[&id, &kind, &(MAX_DATABASE_RECLAMATION_ROWS as i64)])
                .map_err(sql_error)?;
            if rows.is_empty() {
                r.phase += 1;
                continue;
            }
            r.rows_selected = rows.len() as u64;
            if r.applied {
                for row in rows {
                    r.rows_inserted +=
                        insert_object(c, id, kind, &digest(row.get(0), "reclamation seed")?)?;
                }
            }
            return Ok(());
        }
        if phase == SEEDS.len() {
            if objects::expand(c, id, r)? {
                return Ok(());
            }
            r.phase += 1;
            continue;
        }
        let detach = phase - SEEDS.len() - 1;
        if detach < DETACH.len() {
            let (table, predicate) = DETACH[detach];
            if detach_rows(c, id, table, predicate, r)? {
                return Ok(());
            }
            r.phase += 1;
            continue;
        }
        if detach == DETACH.len() {
            if objects::collect(c, id, r)? {
                return Ok(());
            }
            r.phase += 1;
            continue;
        }
        if detach == DETACH.len() + 1 {
            if detach_rows(
                c,
                id,
                "atomic_transaction_contents",
                "lineage_id=(SELECT lineage_id FROM atomic_database_identities WHERE database_id=$1)",
                r,
            )? {
                return Ok(());
            }
            r.phase += 1;
            continue;
        }
        if detach == DETACH.len() + 2 {
            if detach_rows(c, id, "atomic_databases", "database_id=$1", r)? {
                return Ok(());
            }
            r.phase += 1;
            continue;
        }
        if r.applied {
            c.execute("UPDATE atomic_database_identities SET reclaimed_at=clock_timestamp() WHERE database_id=$1 AND lineage_id=$2 AND retired_at IS NOT NULL",&[&id,&lineage]).map_err(sql_error)?;
        }
        r.complete = true;
        return Ok(());
    }
}
fn insert_object<C: GenericClient>(
    c: &mut C,
    id: &str,
    kind: i16,
    hash: &Digest,
) -> Result<u64, SemanticError> {
    c.execute("INSERT INTO atomic_database_reclamation_objects(database_id,kind,object_hash,expanded) VALUES($1,$2,$3,$4) ON CONFLICT DO NOTHING",&[&id,&kind,&&hash[..],&matches!(kind,4|5|7|8)]).map_err(sql_error)
}
fn detach_rows<C: GenericClient>(
    c: &mut C,
    id: &str,
    table: &str,
    predicate: &str,
    r: &mut RetiredDatabaseReclamation,
) -> Result<bool, SemanticError> {
    let selection = format!(
        "SELECT ctid FROM {table} WHERE {predicate} LIMIT {}",
        MAX_DATABASE_RECLAMATION_ROWS
    );
    r.rows_selected = positive_or_zero(
        c.query_one(&format!("SELECT count(*) FROM ({selection}) s"), &[&id])
            .map_err(sql_error)?
            .get(0),
        "terminal rows",
    )?;
    if r.rows_selected == 0 {
        return Ok(false);
    }
    if r.applied {
        r.rows_removed += c
            .execute(
                &format!("DELETE FROM {table} WHERE ctid IN({selection})"),
                &[&id],
            )
            .map_err(sql_error)?;
    }
    Ok(true)
}
