use edb_encoding::ValueType;
use serde::{Deserialize, Serialize};
use std::collections::HashMap;

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum AttrCardinality {
    One,
    Many,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq, Serialize, Deserialize)]
pub enum AttrUnique {
    None,
    Identity,
    Value,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Attribute {
    pub ident: String,           // ":ns/name" canonical keyword string
    pub value_type: ValueType,   // EDB value type
    pub cardinality: AttrCardinality,
    pub unique: AttrUnique,
    pub is_component: bool,
    pub no_history: bool,
    pub doc: Option<String>,
    pub aliases: Vec<String>,    // optional alias idents
}

impl Attribute {
    pub fn is_unique(&self) -> bool { matches!(self.unique, AttrUnique::Identity | AttrUnique::Value) }
}

#[derive(Debug, Default)]
pub struct Catalog {
    by_ident: HashMap<String, Attribute>,
}

impl Catalog {
    pub fn new() -> Self { Self { by_ident: HashMap::new() } }

    pub fn upsert_attribute(&mut self, attr: Attribute) {
        self.by_ident.insert(attr.ident.clone(), attr);
    }

    pub fn get(&self, ident: &str) -> Option<&Attribute> { self.by_ident.get(ident) }
}

