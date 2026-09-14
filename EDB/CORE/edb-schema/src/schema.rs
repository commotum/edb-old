use edb_encoding::ValueType;
use crate::value_type_set::ValueTypeSet;
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
    pub fn is_ref(&self) -> bool { self.value_type == ValueType::Ref }
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

pub trait HasSchema {
    fn attribute_for_ident(&self, ident: &str) -> Option<&Attribute>;
    fn canonical_ident(&self, ident: &str) -> Option<String>;
    fn component_attributes(&self) -> Vec<String>;

    fn attribute_value_type_set(&self, ident: &str) -> Option<ValueTypeSet> {
        self.attribute_for_ident(ident)
            .map(|attr| ValueTypeSet::of_one(attr.value_type))
    }

    fn identifies_attribute(&self, ident: &str) -> bool {
        self.attribute_for_ident(ident).is_some()
    }
}

impl HasSchema for Catalog {
    fn attribute_for_ident(&self, ident: &str) -> Option<&Attribute> {
        if let Some(attr) = self.by_ident.get(ident) {
            return Some(attr);
        }
        self.by_ident
            .values()
            .find(|attr| attr.aliases.iter().any(|alias| alias == ident))
    }

    fn canonical_ident(&self, ident: &str) -> Option<String> {
        if self.by_ident.contains_key(ident) {
            return Some(ident.to_string());
        }
        self.by_ident
            .values()
            .find(|attr| attr.aliases.iter().any(|alias| alias == ident))
            .map(|attr| attr.ident.clone())
    }

    fn component_attributes(&self) -> Vec<String> {
        let mut components: Vec<String> = self
            .by_ident
            .values()
            .filter(|attr| attr.is_component)
            .map(|attr| attr.ident.clone())
            .collect();
        components.sort();
        components
    }
}
