pub mod schema;
pub mod value_type_set;
#[cfg(feature = "sqlite")]
pub mod sqlite;

pub use schema::{AttrCardinality, AttrUnique, Attribute, Catalog, HasSchema};
pub use value_type_set::ValueTypeSet;
#[cfg(feature = "sqlite")]
pub use sqlite::{SchemaCatalog, SchemaError};
