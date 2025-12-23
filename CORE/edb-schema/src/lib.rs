pub mod schema;
#[cfg(feature = "sqlite")]
pub mod sqlite;

pub use schema::{AttrCardinality, AttrUnique, Attribute, Catalog};
#[cfg(feature = "sqlite")]
pub use sqlite::{SchemaCatalog, SchemaError, SchemaLookup};
