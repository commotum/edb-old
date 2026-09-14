use std::collections::BTreeMap;

use base64::{engine::general_purpose, Engine as _};
use edb_edn::query::{
    ContainsVariables,
    Element,
    FindSpec,
    FnArg,
    Limit,
    NotJoin,
    OrJoin,
    OrWhereClause,
    Pattern,
    PatternNonValuePlace,
    PatternValuePlace,
    ParsedQuery,
    Predicate,
    WhereClause,
};
use edb_schema::HasSchema;
use rusqlite::Connection;
use serde_json::Value as JsonValue;

#[derive(thiserror::Error, Debug)]
pub enum QueryError {
    #[error("parse: {0}")]
    Parse(#[from] edb_edn::ParseError),
    #[error("schema: {0}")]
    Schema(#[from] edb_schema::sqlite::SchemaError),
    #[error("index: {0}")]
    Index(String),
    #[error("unsupported query: {0}")]
    Unsupported(&'static str),
    #[error("unknown attribute: {0}")]
    UnknownAttribute(String),
}

type Binding = BTreeMap<String, JsonValue>;

pub fn execute_edn(db_path: &str, query: &str) -> Result<JsonValue, QueryError> {
    let parsed = edb_edn::parse_query(query)?;
    execute(db_path, &parsed)
}

pub fn execute(db_path: &str, query: &ParsedQuery) -> Result<JsonValue, QueryError> {
    validate_query(query)?;

    let conn = Connection::open(db_path).map_err(|e| QueryError::Index(e.to_string()))?;
    let schema = edb_schema::sqlite::SchemaCatalog::load(&conn)?;

    let bindings = evaluate_where(db_path, &schema, &query.where_clauses)?;
    project_results(&query.find_spec, &bindings, &query.limit)
}

fn validate_query(query: &ParsedQuery) -> Result<(), QueryError> {
    if !query.inputs.is_empty() {
        return Err(QueryError::Unsupported(":in inputs are not supported yet"));
    }
    if !query.with.is_empty() {
        return Err(QueryError::Unsupported(":with is not supported yet"));
    }
    if query.return_map.is_some() {
        return Err(QueryError::Unsupported("return maps are not supported yet"));
    }
    if query.order.is_some() {
        return Err(QueryError::Unsupported(":order is not supported yet"));
    }
    match query.limit {
        Limit::None | Limit::Fixed(_) => {}
        Limit::Variable(_) => return Err(QueryError::Unsupported(":limit var is not supported yet")),
    }
    Ok(())
}

fn evaluate_where(
    db_path: &str,
    schema: &edb_schema::sqlite::SchemaCatalog,
    clauses: &[WhereClause],
) -> Result<Vec<Binding>, QueryError> {
    evaluate_where_with_seed(db_path, schema, clauses, vec![Binding::new()])
}

fn evaluate_where_with_seed(
    db_path: &str,
    schema: &edb_schema::sqlite::SchemaCatalog,
    clauses: &[WhereClause],
    mut acc: Vec<Binding>,
) -> Result<Vec<Binding>, QueryError> {
    if clauses.is_empty() {
        return Err(QueryError::Unsupported("missing :where clauses"));
    }
    for clause in clauses {
        match clause {
            WhereClause::Pattern(p) => {
                let next = bindings_for_pattern(db_path, schema, p)?;
                acc = join_bindings(&acc, &next);
            }
            WhereClause::OrJoin(o) => {
                let next = bindings_for_or_join(db_path, schema, o)?;
                acc = join_bindings(&acc, &next);
            }
            WhereClause::Pred(p) => {
                acc = filter_predicate(acc, p)?;
            }
            WhereClause::NotJoin(n) => {
                acc = filter_not_join(db_path, schema, n, acc)?;
            }
            WhereClause::WhereFn(_) => {
                return Err(QueryError::Unsupported("where-fn clauses are not supported yet"));
            }
            WhereClause::RuleExpr(_) => {
                return Err(QueryError::Unsupported("rule expressions are not supported yet"));
            }
            WhereClause::TypeAnnotation(_) => {
                return Err(QueryError::Unsupported("type annotations are not supported yet"));
            }
        }
        if acc.is_empty() {
            break;
        }
    }
    Ok(acc)
}

fn bindings_for_or_join(
    db_path: &str,
    schema: &edb_schema::sqlite::SchemaCatalog,
    or_join: &OrJoin,
) -> Result<Vec<Binding>, QueryError> {
    let mut out = Vec::new();
    for arm in &or_join.clauses {
        let bindings = match arm {
            OrWhereClause::Clause(clause) => {
                evaluate_where_with_seed(db_path, schema, std::slice::from_ref(clause), vec![Binding::new()])?
            }
            OrWhereClause::And(clauses) => {
                evaluate_where_with_seed(db_path, schema, clauses, vec![Binding::new()])?
            }
        };
        out.extend(bindings);
    }
    Ok(out)
}

fn filter_not_join(
    db_path: &str,
    schema: &edb_schema::sqlite::SchemaCatalog,
    not_join: &NotJoin,
    acc: Vec<Binding>,
) -> Result<Vec<Binding>, QueryError> {
    let mentioned = not_join.collect_mentioned_variables();
    let mut out = Vec::new();
    for binding in acc {
        if !vars_bound(&binding, &mentioned) {
            return Err(QueryError::Unsupported("not-join requires bound variables"));
        }
        let matches = evaluate_where_with_seed(db_path, schema, &not_join.clauses, vec![binding.clone()])?;
        if matches.is_empty() {
            out.push(binding);
        }
    }
    Ok(out)
}

fn filter_predicate(bindings: Vec<Binding>, pred: &Predicate) -> Result<Vec<Binding>, QueryError> {
    let mut out = Vec::new();
    for binding in bindings {
        if eval_predicate(pred, &binding)? {
            out.push(binding);
        }
    }
    Ok(out)
}

fn eval_predicate(pred: &Predicate, binding: &Binding) -> Result<bool, QueryError> {
    let op = pred.operator.0.as_str();
    let args: Vec<JsonValue> = pred
        .args
        .iter()
        .map(|arg| fn_arg_to_json(arg, binding))
        .collect::<Result<_, _>>()?;
    match op {
        "=" => {
            ensure_arity(&args, 2)?;
            Ok(args[0] == args[1])
        }
        "!=" => {
            ensure_arity(&args, 2)?;
            Ok(args[0] != args[1])
        }
        "<" => {
            ensure_arity(&args, 2)?;
            Ok(compare_json(&args[0], &args[1])?.is_lt())
        }
        "<=" => {
            ensure_arity(&args, 2)?;
            Ok(!compare_json(&args[0], &args[1])?.is_gt())
        }
        ">" => {
            ensure_arity(&args, 2)?;
            Ok(compare_json(&args[0], &args[1])?.is_gt())
        }
        ">=" => {
            ensure_arity(&args, 2)?;
            Ok(!compare_json(&args[0], &args[1])?.is_lt())
        }
        _ => Err(QueryError::Unsupported("unsupported predicate operator")),
    }
}

fn ensure_arity(args: &[JsonValue], n: usize) -> Result<(), QueryError> {
    if args.len() == n {
        Ok(())
    } else {
        Err(QueryError::Unsupported("predicate arity not supported"))
    }
}

fn compare_json(a: &JsonValue, b: &JsonValue) -> Result<std::cmp::Ordering, QueryError> {
    if let (Some(la), Some(lb)) = (numeric_value(a), numeric_value(b)) {
        return la
            .partial_cmp(&lb)
            .ok_or(QueryError::Unsupported("numeric predicate failed"));
    }
    if let (Some(sa), Some(sb)) = (a.as_str(), b.as_str()) {
        return Ok(sa.cmp(sb));
    }
    Err(QueryError::Unsupported(
        "predicate operands must be numeric or string",
    ))
}

fn numeric_value(v: &JsonValue) -> Option<f64> {
    match v {
        JsonValue::Number(n) => n.as_f64(),
        JsonValue::String(s) => s.parse::<f64>().ok(),
        _ => None,
    }
}

fn fn_arg_to_json(arg: &FnArg, binding: &Binding) -> Result<JsonValue, QueryError> {
    use edb_edn::query::NonIntegerConstant;
    Ok(match arg {
        FnArg::Variable(v) => binding
            .get(&v.to_string())
            .cloned()
            .ok_or(QueryError::Unsupported("predicate variable not bound"))?,
        FnArg::EntidOrInteger(i) => JsonValue::from(*i),
        FnArg::IdentOrKeyword(k) => JsonValue::from(k.to_string()),
        FnArg::Constant(c) => match c {
            NonIntegerConstant::Boolean(v) => JsonValue::from(*v),
            NonIntegerConstant::Float(v) => JsonValue::from(v.into_inner()),
            NonIntegerConstant::BigInteger(v) => JsonValue::from(v.to_string()),
            NonIntegerConstant::Decimal(v) => JsonValue::from(v.to_string()),
            NonIntegerConstant::Text(s) => JsonValue::from(s.to_string()),
            NonIntegerConstant::Instant(i) => JsonValue::from(i.timestamp_micros()),
            NonIntegerConstant::Uuid(u) => JsonValue::from(u.to_string()),
        },
        FnArg::SrcVar(_) => {
            return Err(QueryError::Unsupported("src vars in predicates are not supported"));
        }
        FnArg::Vector(_) => {
            return Err(QueryError::Unsupported("vector predicate args are not supported"));
        }
    })
}

#[derive(Clone, Debug)]
enum EntitySpec {
    Const(i64),
    Var(String),
    Placeholder,
}

#[derive(Clone, Debug)]
enum ValueSpec {
    Const(JsonValue),
    Var(String),
    Placeholder,
}

fn bindings_for_pattern(
    db_path: &str,
    schema: &edb_schema::sqlite::SchemaCatalog,
    pattern: &Pattern,
) -> Result<Vec<Binding>, QueryError> {
    if pattern.source.is_some() {
        return Err(QueryError::Unsupported("src vars are not supported yet"));
    }
    if !matches!(pattern.tx, PatternNonValuePlace::Placeholder) {
        return Err(QueryError::Unsupported("tx/added positions are not supported yet"));
    }

    let attr_ident = attr_ident_from_pattern(&pattern.attribute)?;
    let attr = schema
        .attribute_for_ident(&attr_ident)
        .ok_or_else(|| QueryError::UnknownAttribute(attr_ident.clone()))?;

    let entity_spec = entity_spec_from_place(&pattern.entity)?;
    let value_spec = value_spec_from_place(&pattern.value)?;

    let mut bindings = match (&entity_spec, &value_spec) {
        (_, ValueSpec::Const(value_json)) => {
            let v_bytes = edb_encoding::encode_scalar(attr.value_type, value_json)
                .map_err(QueryError::Index)?;
            let idx = edb_index::AvetIndexer::open(db_path)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = idx
                .scan_av_eq(&attr_ident, &v_bytes)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = current_avet(datoms);
            bindings_from_avet(datoms, &entity_spec, &value_spec, attr.value_type)
        }
        (EntitySpec::Const(e), _) => {
            let idx = edb_index::EavtIndexer::open(db_path)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = idx
                .scan_ea(*e, &attr_ident)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = current_eavt(datoms);
            bindings_from_eavt(datoms, &entity_spec, &value_spec, attr.value_type)
        }
        _ => {
            let idx = edb_index::AevtIndexer::open(db_path)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = idx
                .scan_a(&attr_ident)
                .map_err(|e| QueryError::Index(e.to_string()))?;
            let datoms = current_aevt(datoms);
            bindings_from_aevt(datoms, &entity_spec, &value_spec, attr.value_type)
        }
    }?;

    if bindings.iter().all(|b| b.is_empty()) {
        if bindings.is_empty() {
            return Ok(Vec::new());
        }
        bindings = vec![Binding::new()];
    }

    Ok(bindings)
}

fn entity_spec_from_place(place: &PatternNonValuePlace) -> Result<EntitySpec, QueryError> {
    Ok(match place {
        PatternNonValuePlace::Entid(e) => EntitySpec::Const(*e),
        PatternNonValuePlace::Variable(v) => EntitySpec::Var(v.to_string()),
        PatternNonValuePlace::Placeholder => EntitySpec::Placeholder,
        PatternNonValuePlace::Ident(_) => {
            return Err(QueryError::Unsupported("entity idents are not supported yet"));
        }
    })
}

fn value_spec_from_place(place: &PatternValuePlace) -> Result<ValueSpec, QueryError> {
    Ok(match place {
        PatternValuePlace::Variable(v) => ValueSpec::Var(v.to_string()),
        PatternValuePlace::Placeholder => ValueSpec::Placeholder,
        _ => ValueSpec::Const(pattern_value_to_json(place)?),
    })
}

fn bindings_from_avet(
    datoms: Vec<edb_index::AvetDatom>,
    entity_spec: &EntitySpec,
    value_spec: &ValueSpec,
    value_type: edb_encoding::ValueType,
) -> Result<Vec<Binding>, QueryError> {
    let mut out = Vec::new();
    for d in datoms {
        if let EntitySpec::Const(e) = entity_spec {
            if d.e != *e {
                continue;
            }
        }
        let mut binding = Binding::new();
        if let EntitySpec::Var(var) = entity_spec {
            binding.insert(var.clone(), JsonValue::from(d.e));
        }
        if let ValueSpec::Var(var) = value_spec {
            let value_json = decode_value(&d.v_b64, value_type)?;
            binding.insert(var.clone(), value_json);
        }
        out.push(binding);
    }
    Ok(out)
}

fn bindings_from_aevt(
    datoms: Vec<edb_index::AevtDatom>,
    entity_spec: &EntitySpec,
    value_spec: &ValueSpec,
    value_type: edb_encoding::ValueType,
) -> Result<Vec<Binding>, QueryError> {
    let mut out = Vec::new();
    for d in datoms {
        if let EntitySpec::Const(e) = entity_spec {
            if d.e != *e {
                continue;
            }
        }
        let mut binding = Binding::new();
        if let EntitySpec::Var(var) = entity_spec {
            binding.insert(var.clone(), JsonValue::from(d.e));
        }
        if let ValueSpec::Var(var) = value_spec {
            let value_json = decode_value(&d.v_b64, value_type)?;
            binding.insert(var.clone(), value_json);
        }
        out.push(binding);
    }
    Ok(out)
}

fn bindings_from_eavt(
    datoms: Vec<edb_index::Datom>,
    entity_spec: &EntitySpec,
    value_spec: &ValueSpec,
    value_type: edb_encoding::ValueType,
) -> Result<Vec<Binding>, QueryError> {
    let mut out = Vec::new();
    for d in datoms {
        let mut binding = Binding::new();
        if let EntitySpec::Var(var) = entity_spec {
            binding.insert(var.clone(), JsonValue::from(d.e));
        }
        if let ValueSpec::Var(var) = value_spec {
            let value_json = decode_value(&d.v_b64, value_type)?;
            binding.insert(var.clone(), value_json);
        }
        out.push(binding);
    }
    Ok(out)
}

fn decode_value(v_b64: &str, value_type: edb_encoding::ValueType) -> Result<JsonValue, QueryError> {
    let bytes = general_purpose::STANDARD_NO_PAD
        .decode(v_b64.as_bytes())
        .map_err(|e| QueryError::Index(e.to_string()))?;
    edb_encoding::decode_scalar(value_type, &bytes).map_err(QueryError::Index)
}

fn current_avet(datoms: Vec<edb_index::AvetDatom>) -> Vec<edb_index::AvetDatom> {
    let mut seen = std::collections::BTreeSet::new();
    let mut out = Vec::new();
    for d in datoms {
        let key = (d.e, d.v_b64.clone());
        if seen.insert(key) && d.added {
            out.push(d);
        }
    }
    out
}

fn current_aevt(datoms: Vec<edb_index::AevtDatom>) -> Vec<edb_index::AevtDatom> {
    let mut seen = std::collections::BTreeSet::new();
    let mut out = Vec::new();
    for d in datoms {
        let key = (d.e, d.v_b64.clone());
        if seen.insert(key) && d.added {
            out.push(d);
        }
    }
    out
}

fn current_eavt(datoms: Vec<edb_index::Datom>) -> Vec<edb_index::Datom> {
    let mut seen = std::collections::BTreeSet::new();
    let mut out = Vec::new();
    for d in datoms {
        let key = (d.e, d.v_b64.clone());
        if seen.insert(key) && d.added {
            out.push(d);
        }
    }
    out
}

fn join_bindings(left: &[Binding], right: &[Binding]) -> Vec<Binding> {
    if left.is_empty() || right.is_empty() {
        return Vec::new();
    }
    let mut out = Vec::new();
    for l in left {
        for r in right {
            if bindings_compatible(l, r) {
                let mut merged = l.clone();
                for (k, v) in r {
                    merged.entry(k.clone()).or_insert_with(|| v.clone());
                }
                out.push(merged);
            }
        }
    }
    out
}

fn bindings_compatible(left: &Binding, right: &Binding) -> bool {
    for (k, v) in left {
        if let Some(rv) = right.get(k) {
            if rv != v {
                return false;
            }
        }
    }
    true
}

fn vars_bound(binding: &Binding, vars: &std::collections::BTreeSet<edb_edn::query::Variable>) -> bool {
    vars.iter().all(|v| binding.contains_key(&v.to_string()))
}

fn project_results(
    find_spec: &FindSpec,
    bindings: &[Binding],
    limit: &Limit,
) -> Result<JsonValue, QueryError> {
    match find_spec {
        FindSpec::FindRel(elements) => {
            let rows = project_rel(elements, bindings)?;
            let mut out: Vec<JsonValue> = rows.into_iter().map(JsonValue::Array).collect();
            out = apply_limit(out, limit);
            Ok(JsonValue::Array(out))
        }
        FindSpec::FindTuple(elements) => {
            let mut rows = project_rel(elements, bindings)?;
            if let Limit::Fixed(n) = limit {
                if *n == 0 {
                    return Ok(JsonValue::Null);
                }
            }
            Ok(rows.pop().map(JsonValue::Array).unwrap_or(JsonValue::Null))
        }
        FindSpec::FindColl(element) => {
            let mut rows = project_rel(&vec![element.clone()], bindings)?;
            let mut values: Vec<JsonValue> = rows
                .drain(..)
                .filter_map(|mut row| row.pop())
                .collect();
            values = apply_limit(values, limit);
            Ok(JsonValue::Array(values))
        }
        FindSpec::FindScalar(element) => {
            let mut rows = project_rel(&vec![element.clone()], bindings)?;
            if let Limit::Fixed(n) = limit {
                if *n == 0 {
                    return Ok(JsonValue::Null);
                }
            }
            Ok(rows
                .pop()
                .and_then(|mut row| row.pop())
                .unwrap_or(JsonValue::Null))
        }
    }
}

fn project_rel(elements: &[Element], bindings: &[Binding]) -> Result<Vec<Vec<JsonValue>>, QueryError> {
    let vars: Vec<String> = elements
        .iter()
        .map(element_variable)
        .collect::<Result<Vec<_>, _>>()?;
    let mut rows: Vec<Vec<JsonValue>> = Vec::new();
    for binding in bindings {
        let mut row = Vec::with_capacity(vars.len());
        for var in &vars {
            let val = binding
                .get(var)
                .cloned()
                .ok_or(QueryError::Unsupported("unbound variable in :find"))?;
            row.push(val);
        }
        rows.push(row);
    }
    Ok(rows)
}

fn element_variable(elem: &Element) -> Result<String, QueryError> {
    match elem {
        Element::Variable(v) => Ok(v.to_string()),
        _ => Err(QueryError::Unsupported("only variable find elements are supported")),
    }
}

fn apply_limit<T: Clone>(mut rows: Vec<T>, limit: &Limit) -> Vec<T> {
    if let Limit::Fixed(n) = limit {
        rows.truncate(*n as usize);
    }
    rows
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
        PatternValuePlace::Variable(_) | PatternValuePlace::Placeholder => {
            return Err(QueryError::Unsupported("value must be a constant here"));
        }
    })
}
