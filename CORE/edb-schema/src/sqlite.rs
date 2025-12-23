use std::collections::HashMap;

use edb_encoding::ValueType;
use rusqlite::Connection;

use crate::schema::{AttrCardinality, AttrUnique, Attribute, Catalog, HasSchema};

#[derive(Debug)]
pub enum SchemaError {
    Sqlite(rusqlite::Error),
    UnknownValueType(i64),
}

impl std::fmt::Display for SchemaError {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self {
            SchemaError::Sqlite(err) => write!(f, "sqlite: {}", err),
            SchemaError::UnknownValueType(v) => write!(f, "unknown value type {}", v),
        }
    }
}

impl std::error::Error for SchemaError {}

impl From<rusqlite::Error> for SchemaError {
    fn from(err: rusqlite::Error) -> Self {
        SchemaError::Sqlite(err)
    }
}

#[derive(Debug, Default)]
pub struct SchemaCatalog {
    catalog: Catalog,
    alias_to_ident: HashMap<String, String>,
    component_attributes: Vec<String>,
}

impl SchemaCatalog {
    pub fn load(conn: &Connection) -> Result<Self, SchemaError> {
        let mut catalog = Catalog::new();
        let mut stmt = conn.prepare(
            "SELECT ident,vt,card,uniq,is_component,no_history,doc FROM attrs",
        )?;
        let rows = stmt.query_map([], |r| {
            Ok((
                r.get::<_, String>(0)?,
                r.get::<_, i64>(1)?,
                r.get::<_, i64>(2)?,
                r.get::<_, i64>(3)?,
                r.get::<_, i64>(4)?,
                r.get::<_, i64>(5)?,
                r.get::<_, Option<String>>(6)?,
            ))
        })?;
        for row in rows {
            let (ident, vt_i, card_i, uniq_i, is_comp, no_hist, doc) = row?;
            let vt = map_value_type(vt_i).ok_or(SchemaError::UnknownValueType(vt_i))?;
            catalog.upsert_attribute(Attribute {
                ident,
                value_type: vt,
                cardinality: if card_i == 1 { AttrCardinality::One } else { AttrCardinality::Many },
                unique: match uniq_i { 1 => AttrUnique::Identity, 2 => AttrUnique::Value, _ => AttrUnique::None },
                is_component: is_comp != 0,
                no_history: no_hist != 0,
                doc,
                aliases: Vec::new(),
            });
        }

        let mut alias_to_ident = HashMap::new();
        let mut stmt = conn.prepare("SELECT alias, target FROM aliases")?;
        let rows = stmt.query_map([], |r| Ok((r.get::<_, String>(0)?, r.get::<_, String>(1)?)))?;
        for row in rows {
            let (alias, target) = row?;
            alias_to_ident.insert(alias, target);
        }

        let mut component_attributes: Vec<String> = catalog
            .component_attributes();
        component_attributes.sort();

        Ok(SchemaCatalog { catalog, alias_to_ident, component_attributes })
    }

    pub fn aliases_for(&self, ident: &str) -> Vec<String> {
        self.alias_to_ident
            .iter()
            .filter_map(|(alias, target)| {
                if target == ident {
                    Some(alias.clone())
                } else {
                    None
                }
            })
            .collect()
    }

    pub fn catalog(&self) -> &Catalog {
        &self.catalog
    }

    pub fn alias_map(&self) -> &HashMap<String, String> {
        &self.alias_to_ident
    }
}

impl HasSchema for SchemaCatalog {
    fn attribute_for_ident(&self, ident: &str) -> Option<&Attribute> {
        let target = self.alias_to_ident.get(ident).map(|s| s.as_str()).unwrap_or(ident);
        self.catalog.get(target)
    }

    fn canonical_ident(&self, ident: &str) -> Option<String> {
        if self.catalog.get(ident).is_some() {
            Some(ident.to_string())
        } else {
            self.alias_to_ident.get(ident).cloned()
        }
    }

    fn component_attributes(&self) -> Vec<String> {
        self.component_attributes.clone()
    }
}

fn map_value_type(v: i64) -> Option<ValueType> {
    ValueType::try_from(v).ok()
}
