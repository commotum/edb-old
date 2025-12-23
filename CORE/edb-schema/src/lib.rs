pub mod schema;
#[cfg(feature = "sqlite")]
pub mod sqlite;

pub use schema::{AttrCardinality, AttrUnique, Attribute, Catalog, SchemaLookup};
#[cfg(feature = "sqlite")]
pub use sqlite::{SchemaCatalog, SchemaError};
