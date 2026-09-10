//! Binding availability and complete negative subqueries over immutable sources.
//! A positive fixed point may expose provisional answers to another positive
//! rule, but never to set difference. Recovered `eval-not-join` invokes `q` to
//! completion with a separate answer table before removing matching tuples.
use super::*;

pub(super) fn evaluate_negative(
    clauses: &[Clause],
    seed: Row,
    rules: &[Rule],
    inherited_source: Option<&str>,
    state: &mut State<'_>,
) -> Result<Vec<Row>, SemanticError> {
    // Share sources, deadline, cancellation, work and allocation accounting.
    // Only the provisional positive answer table belongs to this subquery.
    // Restore the outer table on errors too, without refunding attempted work.
    let outer_memo = std::mem::take(&mut state.rule_memo);
    let outer_solving = std::mem::replace(&mut state.solving_rules, false);
    let result = evaluate_clauses(clauses, vec![seed], rules, inherited_source, state);
    state.rule_memo = outer_memo;
    state.solving_rules = outer_solving;
    result
}

pub(super) fn ready(
    clause: &Clause,
    row: &Row,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<bool, SemanticError> {
    let bound = row.keys().cloned().collect();
    Ok(clause_bindings(clause, &bound, rules, state)?.is_some())
}

type Bindings = BTreeSet<Variable>;

fn term_ready(term: &Term, bound: &Bindings) -> bool {
    match term {
        Term::Variable(variable) => bound.contains(variable),
        Term::Blank => false,
        Term::Constant(_) | Term::Nil => true,
    }
}

fn sequence_bindings(
    clauses: &[Clause],
    mut bound: Bindings,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<Option<Bindings>, SemanticError> {
    let mut remaining: Vec<_> = clauses.iter().collect();
    while !remaining.is_empty() {
        let mut selected = None;
        for (index, clause) in remaining.iter().enumerate() {
            if let Some(produced) = clause_bindings(clause, &bound, rules, state)? {
                selected = Some((index, produced));
                break;
            }
        }
        let Some((index, produced)) = selected else {
            return Ok(None);
        };
        remaining.remove(index);
        bound.extend(produced);
    }
    Ok(Some(bound))
}

fn clause_bindings(
    clause: &Clause,
    bound: &Bindings,
    rules: &[Rule],
    state: &mut State<'_>,
) -> Result<Option<Bindings>, SemanticError> {
    state.check(1)?;
    match clause {
        Clause::Or { join, branches } => {
            validate_or(branches, join.as_deref())?;
            let outward: Bindings = join.as_ref().map_or_else(
                || variables_in_clauses(&branches[0]).into_iter().collect(),
                |variables| variables.iter().cloned().collect(),
            );
            let seed: Bindings = join.as_ref().map_or_else(
                || bound.clone(),
                |variables| {
                    variables
                        .iter()
                        .filter(|v| bound.contains(*v))
                        .cloned()
                        .collect()
                },
            );
            let mut produced: Option<Bindings> = None;
            for branch in branches {
                let Some(branch) = sequence_bindings(branch, seed.clone(), rules, state)? else {
                    return Ok(None);
                };
                // Every successful branch must supply the same outward bindings.
                // Join-local variables do not leak or acquire outer values merely
                // because the caller happens to use the same variable name.
                if !outward.is_subset(&branch) {
                    return Ok(None);
                }
                produced = Some(match produced {
                    Some(previous) => previous.intersection(&branch).cloned().collect(),
                    None => branch,
                });
            }
            let mut produced = produced.unwrap_or_default();
            if let Some(join) = join {
                produced.retain(|variable| join.contains(variable));
            }
            Ok(Some(produced))
        }
        Clause::Not { join, clauses } => {
            let required: Bindings = join
                .clone()
                .unwrap_or_else(|| variables_in_clauses(clauses))
                .into_iter()
                .collect();
            if !required.is_subset(bound) {
                return Ok(None);
            }
            let seed = if join.is_some() {
                required
            } else {
                bound.clone()
            };
            Ok(sequence_bindings(clauses, seed, rules, state)?.map(|_| Bindings::new()))
        }
        Clause::Function { args, .. } | Clause::Predicate { args, .. }
            if args
                .iter()
                .any(|term| matches!(term, Term::Variable(v) if !bound.contains(v))) =>
        {
            Ok(None)
        }
        Clause::Rule { name, args, .. } => {
            // Preserve the evaluator's precise unknown-rule/arity diagnostics.
            if let Ok((arity, required)) = rule_signature(rules, name)
                && arity == args.len()
                && required
                    .iter()
                    .any(|index| !term_ready(&args[*index], bound))
            {
                return Ok(None);
            }
            Ok(Some(
                variables_in_clauses(std::slice::from_ref(clause))
                    .into_iter()
                    .collect(),
            ))
        }
        Clause::Predicate { .. } => Ok(Some(Bindings::new())),
        Clause::Function { binding, .. } => Ok(Some(
            binding_variables(binding).into_iter().cloned().collect(),
        )),
        Clause::Pattern(_) => Ok(Some(
            variables_in_clauses(std::slice::from_ref(clause))
                .into_iter()
                .collect(),
        )),
    }
}

/// A cycle that crosses a complement cannot use the monotone positive-rule
/// evaluator. Reject it before exposing any provisional result, while permitting
/// positive/mutual recursion and arbitrarily many acyclic negative strata. Only
/// rules reachable from this query participate; nested `q` has its own rule scope.
pub(super) fn validate_negation(query: &Query, state: &mut State<'_>) -> Result<(), SemanticError> {
    if query.rules.is_empty() {
        return Ok(());
    }
    let names: BTreeSet<_> = query.rules.iter().map(|rule| rule.name.as_str()).collect();
    let indices: BTreeMap<_, _> = names
        .iter()
        .copied()
        .enumerate()
        .map(|(i, name)| (name, i))
        .collect();
    let names: Vec<_> = names.into_iter().collect();
    let mut edges = vec![Vec::new(); names.len()];
    for rule in &query.rules {
        edges[indices[rule.name.as_str()]].extend(rule_edges(&rule.clauses, &indices, state)?);
    }
    let mut reachable = vec![false; names.len()];
    let mut pending: Vec<_> = rule_edges(&query.clauses, &indices, state)?
        .into_iter()
        .map(|(i, _)| i)
        .collect();
    while let Some(node) = pending.pop() {
        state.check(1)?;
        if std::mem::replace(&mut reachable[node], true) {
            continue;
        }
        pending.extend(edges[node].iter().map(|(next, _)| *next));
    }
    // Iterative Kosaraju traversal: graph depth never becomes Rust call depth.
    let mut seen = vec![false; names.len()];
    let mut finished = Vec::new();
    for start in 0..names.len() {
        if !reachable[start] || seen[start] {
            continue;
        }
        seen[start] = true;
        let mut stack = vec![(start, 0)];
        while let Some((node, next)) = stack.last_mut() {
            state.check(1)?;
            if let Some((child, _)) = edges[*node].get(*next) {
                *next += 1;
                if !seen[*child] {
                    seen[*child] = true;
                    stack.push((*child, 0));
                }
            } else {
                finished.push(*node);
                stack.pop();
            }
        }
    }
    let mut reverse = vec![Vec::new(); names.len()];
    for (owner, dependencies) in edges.iter().enumerate() {
        for (dependency, _) in dependencies {
            state.check(1)?;
            reverse[*dependency].push(owner);
        }
    }
    let mut components = vec![usize::MAX; names.len()];
    for root in finished.into_iter().rev() {
        if components[root] != usize::MAX {
            continue;
        }
        let mut pending = vec![root];
        while let Some(node) = pending.pop() {
            state.check(1)?;
            if !reachable[node] || components[node] != usize::MAX {
                continue;
            }
            components[node] = root;
            pending.extend(reverse[node].iter().copied());
        }
    }
    for (owner, dependencies) in edges.iter().enumerate() {
        if !reachable[owner] {
            continue;
        }
        for (dependency, negative) in dependencies {
            state.check(1)?;
            if *negative && components[owner] == components[*dependency] {
                return Err(SemanticError::incorrect(
                    "query/unstratified-negation",
                    "a recursive rule dependency crosses negation; its complement has no completed positive fixed point",
                ).detail("rule", names[owner]).detail("dependency", names[*dependency]));
            }
        }
    }
    Ok(())
}

fn rule_edges(
    clauses: &[Clause],
    indices: &BTreeMap<&str, usize>,
    state: &mut State<'_>,
) -> Result<Vec<(usize, bool)>, SemanticError> {
    let mut pending: Vec<_> = clauses.iter().map(|clause| (clause, false)).collect();
    let mut edges = Vec::new();
    while let Some((clause, negative)) = pending.pop() {
        state.check(1)?;
        match clause {
            Clause::Rule { name, .. } => {
                if let Some(index) = indices.get(name.as_str()) {
                    state.charge_value_bytes(std::mem::size_of::<(usize, bool)>())?;
                    edges.push((*index, negative));
                }
            }
            Clause::Not { clauses, .. } => {
                pending.extend(clauses.iter().map(|clause| (clause, true)))
            }
            Clause::Or { branches, .. } => {
                pending.extend(branches.iter().flatten().map(|clause| (clause, negative)))
            }
            _ => {}
        }
    }
    Ok(edges)
}
