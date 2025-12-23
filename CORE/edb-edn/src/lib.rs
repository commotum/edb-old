use thiserror::Error;

#[derive(Debug, Error)]
pub enum EdnError {
    #[error("edn parse: {0}")]
    Parse(#[from] edn::ParseError),
}

pub fn parse_value(input: &str) -> Result<edn::Value, EdnError> {
    let val = edn::parse::value(input)?.without_spans();
    Ok(val)
}

pub fn parse_query(input: &str) -> Result<edn::query::ParsedQuery, EdnError> {
    let query = edn::parse::parse_query(input)?;
    Ok(query)
}

