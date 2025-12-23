// Copyright 2016 Mozilla
//
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

pub mod entities;
pub mod intern_set;
pub use intern_set::{
    InternSet,
};
// Intentionally not pub.
mod namespaceable_name;
pub mod query;
pub mod symbols;
pub mod types;
pub mod pretty_print;
pub mod utils;
pub mod matcher;
pub mod value_rc;
pub use value_rc::{
    Cloned,
    FromRc,
    ValueRc,
};

pub mod parse;

// Re-export the types we use.
pub use chrono::{DateTime, Utc};
pub use bigdecimal::BigDecimal;
pub use num_bigint::BigInt;
pub use ordered_float::OrderedFloat;
pub use uuid::Uuid;

// Export from our modules.
pub use parse::ParseError;
pub use uuid::Error as UuidParseError;
pub use types::{
    FromMicros,
    FromMillis,
    Span,
    SpannedValue,
    ToMicros,
    ToMillis,
    Value,
    ValueAndSpan,
};

pub use symbols::{
    Keyword,
    NamespacedSymbol,
    PlainSymbol,
};

pub fn parse_value(input: &str) -> Result<Value, ParseError> {
    parse::value(input).map(|v| v.without_spans())
}

pub fn parse_query(input: &str) -> Result<query::ParsedQuery, ParseError> {
    match parse::parse_query(input) {
        Ok(query) => Ok(query),
        Err(list_err) => {
            let value = match parse::value(input) {
                Ok(value) => value,
                Err(_) => return Err(list_err),
            };
            match value.inner {
                SpannedValue::Map(map) => {
                    let list_query = map_query_to_list(&map);
                    parse::parse_query(&list_query)
                }
                _ => Err(list_err),
            }
        }
    }
}

fn map_query_to_list(map: &std::collections::BTreeMap<ValueAndSpan, ValueAndSpan>) -> String {
    use std::collections::BTreeMap;

    let mut parts: Vec<String> = Vec::new();
    let mut keyed: BTreeMap<String, &ValueAndSpan> = BTreeMap::new();
    let mut other_entries: Vec<(&ValueAndSpan, &ValueAndSpan)> = Vec::new();

    for (k, v) in map.iter() {
        if let SpannedValue::Keyword(ref kw) = k.inner {
            keyed.insert(kw.to_string(), v);
        } else {
            other_entries.push((k, v));
        }
    }

    if let Some(v) = keyed.remove(":find") {
        parts.push(":find".to_string());
        push_find_spec(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":keys") {
        parts.push(":keys".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":strs") {
        parts.push(":strs".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":syms") {
        parts.push(":syms".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":with") {
        parts.push(":with".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":in") {
        parts.push(":in".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":where") {
        parts.push(":where".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":order") {
        parts.push(":order".to_string());
        push_collection_parts(&mut parts, v);
    }
    if let Some(v) = keyed.remove(":limit") {
        parts.push(":limit".to_string());
        parts.push(format!("{}", v));
    }

    for (k, v) in keyed {
        parts.push(k);
        parts.push(format!("{}", v));
    }
    for (k, v) in other_entries {
        parts.push(format!("{}", k));
        parts.push(format!("{}", v));
    }

    format!("[{}]", parts.join(" "))
}

fn push_find_spec(parts: &mut Vec<String>, value: &ValueAndSpan) {
    match value.inner {
        SpannedValue::Vector(ref items) => {
            if items.len() == 2 && is_plain_symbol(&items[1], "...") {
                parts.push(format!("{}", value));
            } else {
                for item in items {
                    parts.push(format!("{}", item));
                }
            }
        }
        SpannedValue::List(ref items) => {
            for item in items {
                parts.push(format!("{}", item));
            }
        }
        _ => parts.push(format!("{}", value)),
    }
}

fn push_collection_parts(parts: &mut Vec<String>, value: &ValueAndSpan) {
    match value.inner {
        SpannedValue::Vector(ref items) => {
            for item in items {
                parts.push(format!("{}", item));
            }
        }
        SpannedValue::List(ref items) => {
            for item in items {
                parts.push(format!("{}", item));
            }
        }
        _ => parts.push(format!("{}", value)),
    }
}

fn is_plain_symbol(value: &ValueAndSpan, name: &str) -> bool {
    match value.inner {
        SpannedValue::PlainSymbol(ref sym) => sym.0.as_str() == name,
        _ => false,
    }
}
