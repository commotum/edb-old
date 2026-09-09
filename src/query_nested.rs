//! Subqueries reuse the enclosing budget and immutable named database values.
use super::*;

pub(super) fn execute(
    query: &Query,
    source: &str,
    args: &[BoundValue],
    binding: &Binding,
    parent: &mut State<'_>,
) -> Result<Vec<Vec<BoundValue>>, SemanticError> {
    validate_query(query, args.len())?;
    let mut sources = parent.sources.clone();
    let default = parent.sources.get(source).copied().ok_or_else(|| {
        SemanticError::incorrect("query/unknown-source", format!("unknown source {source}"))
    })?;
    sources.insert("$", default);
    validate_consumed_sources(query, &sources)?;
    let mut child = State {
        sources,
        control: parent.control,
        deadline: parent.deadline,
        work: parent.work,
        stats: QueryStats::default(),
        plan: Vec::new(),
        extensions: parent.extensions,
        rule_memo: BTreeMap::new(),
        solving_rules: false,
    };
    let initial = bind_arguments(&query.inputs, args, &mut child)?;
    let rows = evaluate_clauses(&query.clauses, initial, &query.rules, None, &mut child)?;
    let mut budget = QueryPullBudget::new(
        Arc::clone(&parent.control.cancel),
        parent.deadline,
        parent.control.max_work,
        child.work,
    );
    let result = shape_results(
        query,
        rows,
        parent.control.max_result_rows,
        &child.sources,
        &mut budget,
    )?;
    // q returns one value in its find-selected shape. The enclosing function
    // binding decides whether to keep that value or destructure it once.
    let value = match result {
        QueryResult::Relation(rows) => {
            QueryValue::Collection(rows.into_iter().map(QueryValue::Tuple).collect())
        }
        QueryResult::Tuple(row) => row.map_or(QueryValue::Nil, QueryValue::Tuple),
        QueryResult::Collection(values) => QueryValue::Collection(values),
        QueryResult::Scalar(value) => value.unwrap_or(QueryValue::Nil),
    };
    parent.work = budget.work();
    parent.stats.clauses_executed += child.stats.clauses_executed;
    parent.stats.datoms_examined += child.stats.datoms_examined;
    parent.stats.index_seeks += child.stats.index_seeks;
    parent.stats.rule_iterations += child.stats.rule_iterations;
    parent.plan.extend(child.plan.into_iter().map(|mut step| {
        step.clause.insert_str(0, "nested/");
        step
    }));
    ground_output(&[BoundValue::from_query_value(value)], binding)
}

fn bind_arguments(
    specs: &[InputSpec],
    args: &[BoundValue],
    state: &mut State<'_>,
) -> Result<Vec<Row>, SemanticError> {
    let mut rows = vec![Row::new()];
    for (spec, arg) in specs.iter().zip(args) {
        let binding = match spec {
            InputSpec::Scalar(variable) => Binding::Scalar(variable.clone()),
            InputSpec::Tuple(variables) => Binding::Tuple(variables.clone()),
            InputSpec::Collection(variable) => Binding::Collection(variable.clone()),
            InputSpec::Relation(variables) => Binding::Relation(variables.clone()),
        };
        let relation = ground_output(std::slice::from_ref(arg), &binding)?;
        let mut next = Vec::new();
        for row in &rows {
            for tuple in &relation {
                state.check(1)?;
                let bound = bind_output(row, &binding, tuple)?;
                state.check(bound.len())?;
                next.extend(bound);
            }
        }
        rows = dedupe_rows(next);
        if rows.len() > state.control.max_intermediate_rows {
            return Err(resource(
                "query/intermediate-limit",
                "nested input exceeded its intermediate row limit",
            ));
        }
    }
    state.check(0)?;
    Ok(rows)
}
