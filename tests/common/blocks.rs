//! Test setup over the current product, never a second storage implementation.
//! Bootstrap/explicit object faults use provider primitives; values are always
//! captured by the ordinary block-backed Connection and writes use the service.
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::{
    Connection, DatabaseValue, IndexMaintenanceReceipt, PostgresConnectionConfig, PostgresOperator,
    Program, ProgramHash, Schema, SemanticError,
};

pub fn install(connection: &str) -> Result<(), SemanticError> {
    PgBlockStore::install(&PostgresConnectionConfig::plaintext(connection))
}

pub fn consolidate(connection: &str, name: &str) -> Result<IndexMaintenanceReceipt, SemanticError> {
    let config = PostgresConnectionConfig::plaintext(connection);
    let database = atomic_core::DatabaseCatalog::connect_configured(&config)?.resolve(name)?;
    PostgresOperator::connect_configured(&config)?.consolidate_database(&database.database_id)
}

pub struct TestStore {
    config: PostgresConnectionConfig,
}

impl TestStore {
    pub fn connect(connection: &str) -> Result<Self, SemanticError> {
        Self::connect_configured(&PostgresConnectionConfig::plaintext(connection))
    }

    pub fn connect_configured(config: &PostgresConnectionConfig) -> Result<Self, SemanticError> {
        PgBlockStore::connect(config)?;
        Ok(Self {
            config: config.clone(),
        })
    }

    pub fn create_database(
        &mut self,
        name: &str,
        schema: Schema,
    ) -> Result<DatabaseValue, SemanticError> {
        BlockDatabase::create(&self.config, name, schema)?;
        self.recover(name)
    }

    pub fn recover(&mut self, name: &str) -> Result<DatabaseValue, SemanticError> {
        Ok(Connection::connect_configured(self.config.clone(), name, 128)?.db())
    }

    pub fn recover_basis(
        &mut self,
        name: &str,
        basis: u64,
    ) -> Result<DatabaseValue, SemanticError> {
        Ok(self.recover(name)?.as_of(basis))
    }

    /// Explicit fixture staging without concurrent collection. Product clients
    /// use PostgresOperator::deploy_program; no unpublished object is promised
    /// permanent retention by this helper.
    pub fn deploy_program_blob(&mut self, program: &Program) -> Result<ProgramHash, SemanticError> {
        PgBlockStore::connect(&self.config)?.put(&atomic_core::encode_program(program)?)
    }

    pub fn resolve_program(&mut self, hash: ProgramHash) -> Result<Program, SemanticError> {
        let bytes = PgBlockStore::connect(&self.config)?
            .get(hash)?
            .ok_or_else(|| {
                SemanticError::incorrect("program/not-found", "Fixture program is absent")
            })?;
        atomic_core::decode_program(&bytes)
    }
}
