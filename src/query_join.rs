//! Per-execution, bounded relational join helpers.
use super::*;
use std::collections::HashMap;
use std::hash::{BuildHasher, Hash, Hasher, RandomState};

pub(super) fn bound_bytes(value: &BoundValue) -> usize {
    let inline = std::mem::size_of::<BoundValue>();
    match value {
        BoundValue::Nil => inline,
        BoundValue::Stored(value) => inline
            .saturating_add(usize::try_from(value.retained_heap_bytes()).unwrap_or(usize::MAX)),
        BoundValue::Query(value) => inline.saturating_add(query_value_bytes(value)),
    }
}

pub(super) fn query_value_bytes(value: &QueryValue) -> usize {
    let mut bytes = 0usize;
    let mut pending = vec![value];
    while let Some(value) = pending.pop() {
        bytes = bytes.saturating_add(std::mem::size_of::<QueryValue>());
        match value {
            QueryValue::Nil => {}
            QueryValue::Scalar(value) => {
                bytes = bytes.saturating_add(
                    usize::try_from(value.retained_heap_bytes()).unwrap_or(usize::MAX),
                )
            }
            QueryValue::Tuple(values) | QueryValue::Collection(values) => pending.extend(values),
            QueryValue::Map(values) => {
                pending.extend(values.iter().flat_map(|(key, value)| [key, value]))
            }
        }
    }
    bytes
}

pub(super) fn row_bytes(row: &Row) -> usize {
    row.iter()
        .fold(std::mem::size_of::<Row>(), |bytes, (variable, value)| {
            bytes
                .saturating_add(
                    std::mem::size_of::<Variable>()
                        + variable.name().len()
                        + 4 * std::mem::size_of::<usize>(),
                )
                .saturating_add(bound_bytes(value))
        })
}

fn hash_values<'a>(values: impl IntoIterator<Item = &'a Value>, random: &RandomState) -> u64 {
    let mut hash = random.build_hasher();
    for value in values {
        1u8.hash(&mut hash);
        value.logical_hash(&mut hash);
    }
    hash.finish()
}

// Conservative per-row table allowance: hash bucket, collision-vector capacity,
// row ordinal and allocator overhead. Source values remain borrowed, not copied.
const TABLE_ROW_BYTES: usize = 128;
fn chunk_rows(state: &State<'_>) -> usize {
    (state.control.max_join_bytes / TABLE_ROW_BYTES).max(1)
}

pub(super) fn input_join(
    rows: &[Row],
    relation: &[Vec<(Option<&Variable>, &Value)>],
    state: &mut State<'_>,
) -> Result<Vec<Row>, SemanticError> {
    let mut next = Vec::new();
    let shared: Vec<(usize, &Variable)> = relation
        .first()
        .into_iter()
        .flat_map(|row| row.iter().enumerate())
        .filter_map(|(column, (variable, _))| {
            variable
                .filter(|var| !rows.is_empty() && rows.iter().all(|row| row.contains_key(*var)))
                .map(|var| (column, var))
        })
        .collect();
    let use_hash = !shared.is_empty()
        && relation.len() > 10
        && !state.control.force_scan
        && state.control.max_join_bytes >= TABLE_ROW_BYTES;
    let random = RandomState::new();
    let chunk_size = if use_hash {
        chunk_rows(state)
    } else {
        relation.len().max(1)
    };
    for chunk in relation.chunks(chunk_size) {
        let mut table: HashMap<u64, Vec<usize>> = HashMap::new();
        if use_hash {
            for (index, bindings) in chunk.iter().enumerate() {
                state.check(1)?;
                let hash = hash_values(
                    shared.iter().map(|(column, _)| bindings[*column].1),
                    &random,
                );
                table.entry(hash).or_default().push(index);
                state.stats.hash_join_build_rows += 1;
            }
            state.stats.peak_join_bytes = state
                .stats
                .peak_join_bytes
                .max(chunk.len() * TABLE_ROW_BYTES);
        }
        for row in rows {
            state.check(1)?;
            let candidates: Box<dyn Iterator<Item = usize> + '_> = if use_hash {
                state.stats.hash_join_probes += 1;
                let Some(values) = shared
                    .iter()
                    .map(|(_, variable)| row.get(*variable).and_then(BoundValue::stored))
                    .collect::<Option<Vec<_>>>()
                else {
                    continue;
                };
                let hash = hash_values(values, &random);
                Box::new(table.get(&hash).into_iter().flatten().copied())
            } else {
                Box::new(0..chunk.len())
            };
            for index in candidates {
                state.check(1)?;
                state.stats.join_candidates += 1;
                let mut candidate = row.clone();
                if chunk[index].iter().all(|(variable, value)| {
                    variable.is_none_or(|variable| {
                        unify_variable(
                            &mut candidate,
                            variable,
                            &BoundValue::Stored((*value).clone()),
                        )
                    })
                }) {
                    state.push_row(&mut next, candidate)?;
                }
            }
        }
    }
    Ok(next)
}

pub(super) fn evaluate_tuples(
    pattern: &DataPattern,
    rows: Vec<Row>,
    tuples: &[Vec<Value>],
    state: &mut State<'_>,
) -> Result<(Vec<Row>, String), SemanticError> {
    let mut terms = vec![
        (0, &pattern.entity),
        (1, &pattern.attribute),
        (2, &pattern.value),
    ];
    if let Some(term) = &pattern.transaction {
        terms.push((3, term));
    }
    if let Some(term) = &pattern.added {
        terms.push((4, term));
    }
    let width = terms.last().map_or(3, |(column, _)| column + 1);
    let shared: Vec<_> = terms
        .iter()
        .filter(|(_, term)| {
            !matches!(term, Term::Blank)
                && !rows.is_empty()
                && rows.iter().all(|row| term_is_bound(term, row))
        })
        .copied()
        .collect();
    let use_hash = !shared.is_empty()
        && tuples.len() > 10
        && !state.control.force_scan
        && state.control.max_join_bytes >= TABLE_ROW_BYTES;
    let random = RandomState::new();
    let mut next = Vec::new();
    let chunk_size = if use_hash {
        chunk_rows(state)
    } else {
        tuples.len().max(1)
    };
    for chunk in tuples.chunks(chunk_size) {
        let mut table: HashMap<u64, Vec<usize>> = HashMap::new();
        if use_hash {
            for (index, tuple) in chunk.iter().enumerate() {
                state.check(1)?;
                state.stats.datoms_examined += 1;
                if tuple.len() < width {
                    continue;
                }
                let hash = hash_values(shared.iter().map(|(column, _)| &tuple[*column]), &random);
                table.entry(hash).or_default().push(index);
                state.stats.hash_join_build_rows += 1;
            }
            state.stats.peak_join_bytes = state
                .stats
                .peak_join_bytes
                .max(chunk.len() * TABLE_ROW_BYTES);
        }
        for row in &rows {
            state.check(1)?;
            let candidates: Box<dyn Iterator<Item = usize> + '_> = if use_hash {
                state.stats.hash_join_probes += 1;
                let bounds: Vec<_> = shared
                    .iter()
                    .map(|(_, term)| term_bound_value(term, row))
                    .collect();
                let Some(values) = bounds
                    .iter()
                    .map(|value| value.as_ref().and_then(BoundValue::stored))
                    .collect::<Option<Vec<_>>>()
                else {
                    continue;
                };
                let hash = hash_values(values, &random);
                Box::new(table.get(&hash).into_iter().flatten().copied())
            } else {
                Box::new(0..chunk.len())
            };
            for index in candidates {
                state.check(1)?;
                state.stats.join_candidates += 1;
                if !use_hash {
                    state.stats.datoms_examined += 1;
                }
                let tuple = &chunk[index];
                if tuple.len() < width {
                    continue;
                }
                let mut candidate = row.clone();
                if terms.iter().all(|(column, term)| {
                    unify_term(
                        &mut candidate,
                        term,
                        &BoundValue::Stored(tuple[*column].clone()),
                    )
                }) {
                    state.push_row(&mut next, candidate)?;
                }
            }
        }
    }
    Ok((
        next,
        if use_hash {
            "tuple hash join"
        } else {
            "tuple scan"
        }
        .into(),
    ))
}
