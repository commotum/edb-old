use std::collections::HashMap;

use edb_encoding::ValueType;
use edb_schema::{AttrCardinality, AttrUnique, Attribute, Catalog};
use rusqlite::Connection;

#[derive(Debug)]
pub enum SchemaError {
    Sqlite(rusqlite::Error),
    UnknownValueType(i64),
}

impl From<rusqlite::Error> for SchemaError {
    fn from(err: rusqlite::Error) -> Self {
        SchemaError::Sqlite(err)
    }
}

#[derive(Debug, Default)]
pub struct SchemaCatalog {
    catalog: Catalog,
    alias_to_ident: HashMap<String, String>,
}

pub trait SchemaLookup {
    fn attribute(&self, ident: &str) -> Option<&Attribute>;
    fn canonical_ident(&self, ident: &str) -> Option<String>;
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

        Ok(SchemaCatalog { catalog, alias_to_ident })
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

impl SchemaLookup for SchemaCatalog {
    fn attribute(&self, ident: &str) -> Option<&Attribute> {
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
}

fn map_value_type(v: i64) -> Option<ValueType> {
    match v {
        1 => Some(ValueType::Long),
        2 => Some(ValueType::Double),
        3 => Some(ValueType::Boolean),
        4 => Some(ValueType::String),
        5 => Some(ValueType::Keyword),
        6 => Some(ValueType::Uuid),
        7 => Some(ValueType::Instant),
        8 => Some(ValueType::Ref),
        9 => Some(ValueType::Bytes),
        10 => Some(ValueType::Uint8),
        11 => Some(ValueType::Bigint),
        12 => Some(ValueType::Decimal),
        13 => Some(ValueType::Float32),
        14 => Some(ValueType::Float16),
        15 => Some(ValueType::Bfloat16),
        _ => None,
    }
}
