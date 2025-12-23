pub mod schema;
#[cfg(feature = "sqlite")]
pub mod sqlite;

pub use schema::{AttrCardinality, AttrUnique, Attribute, Catalog, HasSchema};
#[cfg(feature = "sqlite")]
pub use sqlite::{SchemaCatalog, SchemaError};
