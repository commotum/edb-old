//! Mutable public names over permanent canonical identities. Catalog mutation
//! requires operator authority; runtime roles receive read access only.
use crate::postgres::postgres_error;
use crate::sql_io::GenericClient;
use crate::{ErrorCategory, PostgresConnectionConfig, PostgresStore, Schema, SemanticError};

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct DatabaseCatalogEntry {
    /// None for a retired identity; names are reusable, identities are not.
    pub name: Option<String>,
    pub database_id: String,
    pub lineage_id: String,
    pub retired: bool,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct CreateDatabaseResult {
    pub created: bool,
    pub database: DatabaseCatalogEntry,
}

pub struct DatabaseCatalog {
    store: PostgresStore,
}

pub(crate) fn validate_name(name: &str) -> Result<(), SemanticError> {
    if name.is_empty() || name.contains('\0') {
        Err(SemanticError::incorrect(
            "catalog/invalid-name",
            "database names must be non-empty and contain no NUL",
        ))
    } else {
        Ok(())
    }
}

fn entry(row: postgres::Row) -> DatabaseCatalogEntry {
    DatabaseCatalogEntry {
        name: row.get(0),
        database_id: row.get(1),
        lineage_id: row.get(2),
        retired: row.get(3),
    }
}

pub(crate) fn resolve_name_opt_in<C: GenericClient>(
    client: &mut C,
    name: &str,
) -> Result<Option<DatabaseCatalogEntry>, SemanticError> {
    // A durable restore reservation owns its name before it publishes a head.
    // Catalog existence must not be confused with readiness to open a value.
    client
        .query_opt(
            "SELECT n.name,i.database_id,i.lineage_id,i.retired_at IS NOT NULL \
        FROM atomic_database_names n JOIN atomic_database_identities i USING(database_id) \
        JOIN atomic_databases d USING(database_id) \
        WHERE n.name=$1 AND i.retired_at IS NULL AND i.lineage_id=d.lineage_id",
            &[&name],
        )
        .map(|row| row.map(entry))
        .map_err(|error| postgres_error("catalog/resolve", error))
}

pub(crate) fn resolve_name_in<C: GenericClient>(
    client: &mut C,
    name: &str,
) -> Result<DatabaseCatalogEntry, SemanticError> {
    resolve_name_opt_in(client, name)?.ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::NotFound,
            "catalog/name-not-found",
            format!("no active database is named {name}"),
        )
    })
}

pub(crate) fn require_active_id_in<C: GenericClient>(
    client: &mut C,
    database_id: &str,
) -> Result<DatabaseCatalogEntry, SemanticError> {
    let row = client
        .query_opt(
            "SELECT n.name,i.database_id,i.lineage_id,i.retired_at IS NOT NULL \
        FROM atomic_database_identities i LEFT JOIN atomic_database_names n USING(database_id) \
        WHERE i.database_id=$1",
            &[&database_id],
        )
        .map_err(|error| postgres_error("catalog/identity", error))?
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "catalog/database-not-found",
                "database storage identity does not exist",
            )
        })?;
    let entry = entry(row);
    if entry.retired {
        return Err(SemanticError::new(
            ErrorCategory::NotFound,
            "catalog/database-retired",
            "database storage identity is retired",
        ));
    }
    Ok(entry)
}

pub(crate) fn lock_names<C: GenericClient>(
    client: &mut C,
    names: &[&str],
) -> Result<(), SemanticError> {
    let mut names = names.to_vec();
    names.sort_unstable();
    names.dedup();
    for name in names {
        client
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended('atomic/catalog/name/' || $1,0))",
                &[&name],
            )
            .map_err(|error| postgres_error("catalog/name-lock", error))?;
    }
    Ok(())
}

/// Register a never-issued stable ID and its initial route in the same
/// transaction as canonical creation/restore. Reclaimed IDs are not recycled.
pub(crate) fn register_new_in<C: GenericClient>(
    client: &mut C,
    name: &str,
    database_id: &str,
    lineage_id: &str,
) -> Result<(), SemanticError> {
    validate_name(name)?;
    client
        .execute(
            "INSERT INTO atomic_database_identities(database_id,lineage_id) VALUES($1,$2)",
            &[&database_id, &lineage_id],
        )
        .map_err(|error| postgres_error("catalog/register-identity", error))?;
    client
        .execute(
            "INSERT INTO atomic_database_names(name,database_id) VALUES($1,$2)",
            &[&name, &database_id],
        )
        .map_err(|error| postgres_error("catalog/register-name", error))?;
    Ok(())
}

pub(crate) fn allocate_storage_id_in<C: GenericClient>(
    client: &mut C,
    name: &str,
) -> Result<String, SemanticError> {
    let issued = client.query_one("SELECT EXISTS(SELECT 1 FROM atomic_database_identities WHERE database_id=$1) OR EXISTS(SELECT 1 FROM atomic_databases WHERE database_id=$1)", &[&name])
        .map_err(|error| postgres_error("catalog/check-issued-id", error))?.get::<_,bool>(0);
    if issued {
        client
            .query_one("SELECT 'atomic-' || gen_random_uuid()::text", &[])
            .map(|row| row.get(0))
            .map_err(|error| postgres_error("catalog/new-storage-id", error))
    } else {
        Ok(name.to_owned())
    }
}

fn check_identity(
    entry: &DatabaseCatalogEntry,
    expected: Option<&str>,
) -> Result<(), SemanticError> {
    if expected.is_some_and(|lineage| lineage != entry.lineage_id) {
        Err(SemanticError::conflict(
            "catalog/identity-mismatch",
            "the active name no longer identifies the expected lineage",
        ))
    } else {
        Ok(())
    }
}

fn page_limit(limit: usize) -> Result<i64, SemanticError> {
    if !(1..=4096).contains(&limit) {
        return Err(SemanticError::incorrect(
            "catalog/page-limit",
            "catalog page limit must be in 1..=4096",
        ));
    }
    Ok(limit as i64)
}

impl DatabaseCatalog {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }
    pub fn connect_configured(
        connection: &PostgresConnectionConfig,
    ) -> Result<Self, SemanticError> {
        Ok(Self {
            store: PostgresStore::connect_configured(connection)?,
        })
    }
    /// Resolve an issued active name, including an unfinished restore target.
    /// Opening a database value independently requires a published head.
    pub fn resolve(&mut self, name: &str) -> Result<DatabaseCatalogEntry, SemanticError> {
        resolve_name_in(self.store.catalog_client(), name)
    }
    pub fn require_active_id(
        &mut self,
        database_id: &str,
    ) -> Result<DatabaseCatalogEntry, SemanticError> {
        require_active_id_in(self.store.catalog_client(), database_id)
    }
    pub fn list(
        &mut self,
        after_name: Option<&str>,
        limit: usize,
    ) -> Result<Vec<DatabaseCatalogEntry>, SemanticError> {
        let limit = page_limit(limit)?;
        self.store
            .catalog_client()
            .query(
                "SELECT n.name,i.database_id,i.lineage_id,false \
            FROM atomic_database_names n JOIN atomic_database_identities i USING(database_id) \
            JOIN atomic_databases d USING(database_id) \
            WHERE i.retired_at IS NULL AND i.lineage_id=d.lineage_id \
            AND ($1::text IS NULL OR n.name>$1) ORDER BY n.name LIMIT $2",
                &[&after_name, &limit],
            )
            .map(|rows| rows.into_iter().map(entry).collect())
            .map_err(|error| postgres_error("catalog/list", error))
    }
    pub fn list_retired(
        &mut self,
        after_storage_id: Option<&str>,
        limit: usize,
    ) -> Result<Vec<DatabaseCatalogEntry>, SemanticError> {
        let limit = page_limit(limit)?;
        self.store
            .catalog_client()
            .query(
                "SELECT NULL::text,i.database_id,i.lineage_id,true \
            FROM atomic_database_identities i WHERE i.retired_at IS NOT NULL \
            AND ($1::text IS NULL OR i.database_id>$1) ORDER BY i.database_id LIMIT $2",
                &[&after_storage_id, &limit],
            )
            .map(|rows| rows.into_iter().map(entry).collect())
            .map_err(|error| postgres_error("catalog/list-retired", error))
    }
    /// Existing names return only catalog metadata; no replay or schema rebuild.
    pub fn create_if_absent(
        &mut self,
        name: &str,
        schema: Schema,
    ) -> Result<CreateDatabaseResult, SemanticError> {
        self.store.create_database_if_absent(name, schema)
    }
    pub fn rename(
        &mut self,
        name: &str,
        new_name: &str,
    ) -> Result<DatabaseCatalogEntry, SemanticError> {
        self.rename_inner(name, new_name, None)
    }
    pub fn rename_checked(
        &mut self,
        name: &str,
        new_name: &str,
        expected_lineage: &str,
    ) -> Result<DatabaseCatalogEntry, SemanticError> {
        self.rename_inner(name, new_name, Some(expected_lineage))
    }
    fn rename_inner(
        &mut self,
        name: &str,
        new_name: &str,
        expected_lineage: Option<&str>,
    ) -> Result<DatabaseCatalogEntry, SemanticError> {
        validate_name(new_name)?;
        let mut transaction = self
            .store
            .catalog_client()
            .transaction()
            .map_err(|error| postgres_error("catalog/rename-begin", error))?;
        lock_names(&mut transaction, &[name, new_name])?;
        let mut entry = resolve_name_in(&mut transaction, name)?;
        check_identity(&entry, expected_lineage)?;
        if name != new_name {
            if resolve_name_opt_in(&mut transaction, new_name)?.is_some() {
                return Err(SemanticError::conflict(
                    "catalog/name-exists",
                    "rename target is already active",
                ));
            }
            transaction
                .execute(
                    "UPDATE atomic_database_names SET name=$2 WHERE name=$1",
                    &[&name, &new_name],
                )
                .map_err(|error| postgres_error("catalog/rename", error))?;
            entry.name = Some(new_name.to_owned());
        }
        transaction
            .commit()
            .map_err(|error| postgres_error("catalog/rename-commit", error))?;
        Ok(entry)
    }
    pub fn retire(&mut self, name: &str) -> Result<Option<DatabaseCatalogEntry>, SemanticError> {
        self.retire_inner(name, None)
    }
    pub fn retire_checked(
        &mut self,
        name: &str,
        expected_lineage: &str,
    ) -> Result<Option<DatabaseCatalogEntry>, SemanticError> {
        self.retire_inner(name, Some(expected_lineage))
    }
    fn retire_inner(
        &mut self,
        name: &str,
        expected_lineage: Option<&str>,
    ) -> Result<Option<DatabaseCatalogEntry>, SemanticError> {
        let mut transaction = self
            .store
            .catalog_client()
            .transaction()
            .map_err(|error| postgres_error("catalog/retire-begin", error))?;
        lock_names(&mut transaction, &[name])?;
        let Some(mut entry) = resolve_name_opt_in(&mut transaction, name)? else {
            return Ok(None);
        };
        check_identity(&entry, expected_lineage)?;
        // Match lease acquisition, then the ordinary writer's lease-before-head
        // order. A committed retirement invalidates every already running epoch.
        transaction
            .query_one(
                "SELECT pg_advisory_xact_lock(hashtextextended($1,0))",
                &[&entry.database_id],
            )
            .map_err(|error| postgres_error("catalog/retire-advisory", error))?;
        transaction
            .query_opt(
                "SELECT epoch FROM atomic_transactor_leases WHERE lease_scope=$1 FOR UPDATE",
                &[&entry.database_id],
            )
            .map_err(|error| postgres_error("catalog/retire-lease", error))?;
        transaction
            .query_opt(
                "SELECT basis_t FROM atomic_heads WHERE database_id=$1 FOR UPDATE",
                &[&entry.database_id],
            )
            .map_err(|error| postgres_error("catalog/retire-head", error))?;
        transaction.execute("UPDATE atomic_transactor_leases SET epoch=epoch+1,expires_at=clock_timestamp() WHERE lease_scope=$1", &[&entry.database_id])
            .map_err(|error| postgres_error("catalog/retire-fence", error))?;
        transaction.execute("UPDATE atomic_database_identities SET retired_at=clock_timestamp() WHERE database_id=$1 AND retired_at IS NULL", &[&entry.database_id])
            .map_err(|error| postgres_error("catalog/retire-identity", error))?;
        transaction
            .execute(
                "DELETE FROM atomic_database_names WHERE name=$1 AND database_id=$2",
                &[&name, &entry.database_id],
            )
            .map_err(|error| postgres_error("catalog/retire-name", error))?;
        transaction
            .commit()
            .map_err(|error| postgres_error("catalog/retire-commit", error))?;
        entry.name = None;
        entry.retired = true;
        Ok(Some(entry))
    }
}
