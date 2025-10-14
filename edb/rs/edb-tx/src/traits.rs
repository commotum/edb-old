use crate::model::Value;
use edb_schema::Attribute;

#[derive(Debug, Clone, PartialEq)]
pub enum UniquenessResult {
    Absent,
    Present(i64),     // existing entity with (a,v)
}

// Abstraction for current DB state (used during validation/normalization)
pub trait DbView {
    // Lookup a unique attr value to entid (identity/value uniqueness)
    fn lookup_by_unique(&self, attr_ident: &str, v: &Value) -> Option<i64>;
    // Get current cardinality-one value for (e, a)
    fn current_value(&self, e: i64, attr_ident: &str) -> Option<Value>;
    // Check uniqueness; return owning e if present
    fn uniqueness_check(&self, attr_ident: &str, v: &Value) -> UniquenessResult;
    // Fetch attribute metadata by ident
    fn get_attr(&self, ident: &str) -> Option<Attribute>;
}

