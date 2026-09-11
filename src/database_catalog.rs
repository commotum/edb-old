//! Public catalog API uses the opaque-block engine exclusively.
pub(crate) use crate::storage::catalog::validate_name;
pub use crate::storage::catalog::{CreateDatabaseResult, DatabaseCatalog, DatabaseCatalogEntry};
