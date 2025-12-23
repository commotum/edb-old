use edb_edn::query::{PatternNonValuePlace, PatternValuePlace, ParsedQuery, WhereClause};
use edb_schema::HasSchema;
use rusqlite::Connection;
use serde_json::Value as JsonValue;

#[derive(thiserror::Error, Debug)]
pub enum QueryError {
    #[error("parse: {0}")]
    Parse(#[from] edb_edn::ParseError),
    #[error("schema: {0:?}")]
    Schema(#[from] edb_schema::sqlite::SchemaError),
    #[error("index: {0}")]
    Index(String),
    #[error("unsupported query: {0}")]
    Unsupported(&'static str),
    #[error("unknown attribute: {0}")]
    UnknownAttribute(String),
}

pub fn execute_simple_edn(db_path: &str, query: &str) -> Result<Vec<i64>, QueryError> {
    let parsed = edb_edn::parse_query(query)?;
    execute_simple(db_path, &parsed)
}

pub fn execute_simple(db_path: &str, query: &ParsedQuery) -> Result<Vec<i64>, QueryError> {
    let pattern = extract_single_pattern(query)?;
    let attr_ident = attr_ident_from_pattern(&pattern.attribute)?;

    let conn = Connection::open(db_path).map_err(|e| QueryError::Index(e.to_string()))?;
    let schema = edb_schema::sqlite::SchemaCatalog::load(&conn)?;
    let attr = schema
        .attribute_for_ident(&attr_ident)
        .ok_or_else(|| QueryError::UnknownAttribute(attr_ident.clone()))?;
    let value_json = pattern_value_to_json(&pattern.value)?;
    let v_bytes = edb_encoding::encode_scalar(attr.value_type, &value_json)
        .map_err(QueryError::Index)?;

    let idx = edb_index::AvetIndexer::open(db_path).map_err(|e| QueryError::Index(e.to_string()))?;
    let datoms = idx.scan_av_eq(&attr_ident, &v_bytes).map_err(|e| QueryError::Index(e.to_string()))?;
    Ok(current_entities_from_avet(datoms))
}

fn extract_single_pattern(query: &ParsedQuery) -> Result<edb_edn::query::Pattern, QueryError> {
    if query.where_clauses.len() != 1 {
        return Err(QueryError::Unsupported("expected exactly one :where clause"));
    }
    match &query.where_clauses[0] {
        WhereClause::Pattern(p) => Ok(p.clone()),
        _ => Err(QueryError::Unsupported("expected a single data pattern")),
    }
}

fn attr_ident_from_pattern(place: &PatternNonValuePlace) -> Result<String, QueryError> {
    match place {
        PatternNonValuePlace::Ident(kw) => Ok(kw.to_string()),
        _ => Err(QueryError::Unsupported("attribute must be a keyword ident")),
    }
}

fn pattern_value_to_json(value: &PatternValuePlace) -> Result<JsonValue, QueryError> {
    use edb_edn::query::NonIntegerConstant;
    Ok(match value {
        PatternValuePlace::EntidOrInteger(x) => JsonValue::from(*x),
        PatternValuePlace::IdentOrKeyword(kw) => JsonValue::from(kw.to_string()),
        PatternValuePlace::Constant(c) => match c {
            NonIntegerConstant::Boolean(v) => JsonValue::from(*v),
            NonIntegerConstant::Float(v) => JsonValue::from(v.into_inner()),
            NonIntegerConstant::BigInteger(v) => JsonValue::from(v.to_string()),
            NonIntegerConstant::Decimal(v) => JsonValue::from(v.to_string()),
            NonIntegerConstant::Text(s) => JsonValue::from(s.to_string()),
            NonIntegerConstant::Instant(i) => JsonValue::from(i.timestamp_micros()),
            NonIntegerConstant::Uuid(u) => JsonValue::from(u.to_string()),
        },
        PatternValuePlace::Variable(_) => {
            return Err(QueryError::Unsupported("value must be a constant for simple queries"));
        }
        PatternValuePlace::Placeholder => {
            return Err(QueryError::Unsupported("value must be a constant for simple queries"));
        }
    })
}

fn current_entities_from_avet(datoms: Vec<edb_index::AvetDatom>) -> Vec<i64> {
    let mut seen = std::collections::BTreeSet::new();
    let mut out = Vec::new();
    for d in datoms {
        if seen.insert(d.e) && d.added {
            out.push(d.e);
        }
    }
    out
}
