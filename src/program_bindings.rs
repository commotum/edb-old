//! Controlled transaction semantics shared by durable publication and pure
//! native speculation. The resolver grants immutable code content only; these
//! helpers cannot acquire writer authority or follow a mutable database head.
use crate::database::PredicateRole;
use crate::encoding::program_call_digest;
use crate::program::ValidatedProgram;
use crate::{
    CallableRef, DatabaseValue, Datom, Digest, ErrorCategory, ProgramBudget, ProgramCall,
    ProgramHash, ProgramKind, ProgramOutput, ProgramRuntime, SemanticError, TxForm, TxFunctions,
    TxValue, Value,
};
use std::collections::{BTreeMap, BTreeSet};
use std::sync::{Arc, Mutex};

pub(crate) type SharedProgramBudget = Arc<Mutex<ProgramBudget<'static>>>;
pub(crate) type ResolvedPrograms = BTreeMap<ProgramHash, Arc<ValidatedProgram>>;
pub(crate) type ProgramResolver<'a> =
    dyn FnMut(ProgramHash) -> Result<Arc<ValidatedProgram>, SemanticError> + 'a;

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

fn qualified_program_ident(name: &str) -> Result<crate::Keyword, SemanticError> {
    let Some((namespace, local)) = name.split_once('/') else {
        return Err(SemanticError::incorrect(
            "program/unqualified-predicate",
            format!("predicate {name} must be a fully qualified symbol"),
        ));
    };
    if namespace.is_empty() || local.is_empty() || local.contains('/') {
        return Err(SemanticError::incorrect(
            "program/unqualified-predicate",
            format!("predicate {name} must be a fully qualified symbol"),
        ));
    }
    Ok(crate::Keyword::new(namespace, local))
}

/// Resolve a database function exactly at the recovered `Db.getFn` boundary:
/// first resolve its ident in this immutable db-before, then read its
/// cardinality-one `:db/fn` value. The native value is a content hash rather
/// than a JVM function object.
fn bound_program_hash(
    database: &DatabaseValue,
    ident: &crate::Keyword,
) -> Result<ProgramHash, SemanticError> {
    let entity = database.entid(ident).ok_or_else(|| {
        SemanticError::new(
            ErrorCategory::NotFound,
            "program/function-not-found",
            format!(
                "database function {} is not installed",
                ident.qualified_name()
            ),
        )
    })?;
    match database.values(entity, crate::DB_FN as u32)?.as_slice() {
        [Value::Function(hash)] => Ok(*hash),
        [] => Err(SemanticError::incorrect(
            "program/not-a-database-function",
            format!("entity {} has no :db/fn value", ident.qualified_name()),
        )),
        _ => Err(fault(
            "program/invalid-function-binding",
            format!(
                "entity {} has a malformed :db/fn value",
                ident.qualified_name()
            ),
        )),
    }
}

fn database_callable_entity(
    database: &DatabaseValue,
    reference: &crate::EntityRef,
) -> Result<u64, SemanticError> {
    match reference {
        crate::EntityRef::Id(entity) => Ok(*entity),
        crate::EntityRef::Ident(ident) => database.entid(ident).ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "program/function-not-found",
                format!(
                    "database function {} is not installed",
                    ident.qualified_name()
                ),
            )
        }),
        crate::EntityRef::Lookup { attribute, value } => {
            database.lookup(*attribute, value)?.ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "program/function-not-found",
                    "database function lookup reference did not resolve",
                )
            })
        }
        crate::EntityRef::LookupInput { attribute, value } => database
            .resolve_lookup_input(*attribute, value)?
            .ok_or_else(|| {
                SemanticError::new(
                    ErrorCategory::NotFound,
                    "program/function-not-found",
                    "database function lookup reference did not resolve",
                )
            }),
        crate::EntityRef::Temp(_) | crate::EntityRef::Tx => Err(SemanticError::incorrect(
            "program/non-temporal-callable",
            "database functions must resolve from db-before, not a transaction-local entity",
        )),
    }
}

fn callable_hash(
    database: &DatabaseValue,
    callable: &CallableRef,
) -> Result<ProgramHash, SemanticError> {
    match callable {
        CallableRef::Database(reference) => {
            let entity = database_callable_entity(database, reference)?;
            match database.values(entity, crate::DB_FN as u32)?.as_slice() {
                [Value::Function(hash)] => Ok(*hash),
                [] => Err(SemanticError::incorrect(
                    "program/not-a-database-function",
                    format!("entity {entity} has no :db/fn value"),
                )),
                _ => Err(fault(
                    "program/invalid-function-binding",
                    format!("entity {entity} has a malformed :db/fn value"),
                )),
            }
        }
        CallableRef::ExactHash(hash) => Ok(*hash),
        CallableRef::Local(symbol) => Err(SemanticError::new(
            ErrorCategory::NotFound,
            "program/local-function-not-found",
            format!(
                "no process-local function registry contains {}",
                symbol.qualified_name()
            ),
        )),
    }
}

fn execute_program_calls(
    resolve: &mut ProgramResolver<'_>,
    db_before: &DatabaseValue,
    calls: &[ProgramCall],
    budget: &mut ProgramBudget<'_>,
) -> Result<Vec<TxForm>, SemanticError> {
    let mut ordered = calls
        .iter()
        .map(|call| Ok((program_call_digest(call)?, call)))
        .collect::<Result<Vec<_>, SemanticError>>()?;
    ordered.sort_by(|left, right| left.0.cmp(&right.0));

    let mut forms = Vec::new();
    for (_, call) in ordered {
        expand_program_call(resolve, db_before, call, budget, 0, &mut forms)?;
    }
    Ok(forms)
}

pub(crate) fn expand_submission_forms(
    resolve: &mut ProgramResolver<'_>,
    db_before: &DatabaseValue,
    submitted: &[TxForm],
    budget: &mut ProgramBudget<'_>,
) -> Result<Vec<TxForm>, SemanticError> {
    crate::transaction::validate_forms_input(submitted)?;
    let mut forms = Vec::with_capacity(submitted.len());
    let mut calls = Vec::new();
    for form in submitted {
        match form {
            TxForm::Op(_) | TxForm::EntityMap(_) => forms.push(form.clone()),
            TxForm::ProgramCall(call) => calls.push(call.clone()),
            TxForm::Call(_) => {
                return Err(SemanticError::incorrect(
                    "service/process-local-call",
                    "process-local Rust transaction callbacks cannot cross the authoritative service boundary",
                ));
            }
        }
    }
    forms.extend(execute_program_calls(resolve, db_before, &calls, budget)?);
    Ok(forms)
}

fn expand_program_call(
    resolve: &mut ProgramResolver<'_>,
    db_before: &DatabaseValue,
    call: &ProgramCall,
    budget: &mut ProgramBudget<'_>,
    depth: usize,
    output: &mut Vec<TxForm>,
) -> Result<(), SemanticError> {
    if depth > 32 {
        return Err(SemanticError::incorrect(
            "transaction/function-depth",
            "persisted transaction-function expansion exceeded 32 nested calls",
        ));
    }
    let hash = callable_hash(db_before, &call.function)?;
    let program = resolve(hash)?;
    if program.program().kind != ProgramKind::Transaction {
        return Err(SemanticError::incorrect(
            "program/not-transaction-function",
            "transaction data called a non-transaction program",
        ));
    }
    let ProgramOutput::Transaction(forms) = ProgramRuntime
        .execute_prevalidated_runtime_exact_with_budget(
            &program,
            db_before,
            &call.arguments,
            budget,
        )?
    else {
        unreachable!("program kind was checked");
    };
    for form in forms {
        match form {
            TxForm::ProgramCall(nested) => {
                expand_program_call(resolve, db_before, &nested, budget, depth + 1, output)?;
            }
            TxForm::Call(_) => {
                return Err(SemanticError::incorrect(
                    "program/process-local-output",
                    "persisted transaction functions cannot emit process-local Rust callbacks",
                ));
            }
            form => output.push(form),
        }
    }
    Ok(())
}

pub(crate) fn validate_successor_program_bindings(
    resolve: &mut ProgramResolver<'_>,
    db_before: &DatabaseValue,
    db_after: &DatabaseValue,
    tx_data: &[Datom],
) -> Result<(), SemanticError> {
    if !tx_data.iter().any(|datom| {
        matches!(
            u64::from(datom.attribute),
            crate::DB_FN | crate::DB_IDENT | crate::DB_ATTR_PREDS | crate::DB_ENTITY_PREDS
        )
    }) {
        return Ok(());
    }
    let mut changed_function_entities = BTreeSet::new();
    let mut changed_function_bindings = BTreeSet::new();
    let mut changed_predicate_names = BTreeSet::new();

    for datom in tx_data {
        match u64::from(datom.attribute) {
            crate::DB_FN => {
                changed_function_entities.insert(datom.entity);
                changed_function_bindings.insert(datom.entity);
            }
            crate::DB_IDENT => {
                changed_function_entities.insert(datom.entity);
            }
            crate::DB_ATTR_PREDS | crate::DB_ENTITY_PREDS => {
                let Value::Symbol(symbol) = &datom.value else {
                    return Err(SemanticError::incorrect(
                        "program/invalid-predicate-name",
                        "predicate bindings must contain symbols",
                    ));
                };
                let name = symbol.qualified_name();
                qualified_program_ident(&name)?;
                changed_predicate_names.insert(name);
            }
            _ => {}
        }
    }

    // A changed :db/fn value is content-addressed and must resolve before the
    // source transaction publishes. Db.getFn also accepts an eid, so neither
    // an ident nor a qualified ident is required for a generic function. An
    // ident rename is included only because it can break a symbol-named
    // predicate reference even when the hash itself is unchanged.
    for entity in changed_function_entities {
        for database in [db_before, db_after] {
            for value in database.values(entity, crate::DB_IDENT as u32)? {
                if let Value::Keyword(ident) = value {
                    changed_predicate_names.insert(ident.qualified_name());
                }
            }
        }

        let functions = db_after.values(entity, crate::DB_FN as u32)?;
        if functions.is_empty() {
            continue;
        }
        let [Value::Function(hash)] = functions.as_slice() else {
            return Err(fault(
                "program/invalid-function-binding",
                format!("entity {entity} has a malformed :db/fn value"),
            ));
        };
        resolve(*hash)?;
    }

    // Validate only dependency names whose binding/reference changed. An old
    // unused bad binding is not transaction input and must not become a
    // global availability gate for unrelated writes.
    // `:db/ident` renames preserve the old name as an alias. A later :db/fn
    // change therefore affects every active predicate name that resolves to
    // the function entity, not merely the entity's current :db/ident datom.
    // Resolve operative names through the immutable db-after dictionary so
    // repurposed aliases follow their new entity. This dependency scan occurs
    // only for a function-binding change; the outer attribute gate keeps
    // ordinary data transactions off this path entirely.
    let changed_roles = predicate_roles(
        db_after,
        &changed_predicate_names,
        &changed_function_bindings,
    )?;
    for (name, role) in changed_roles {
        let ident = qualified_program_ident(&name)?;
        let hash = bound_program_hash(db_after, &ident)?;
        let program = resolve(hash)?;
        if role.requires_attribute() && !program.program().supports_attribute_predicate() {
            return Err(SemanticError::incorrect(
                "program/not-attribute-predicate",
                format!("active program {name} has no attribute-predicate body"),
            ));
        }
        if role.requires_entity() && !program.program().supports_entity_predicate() {
            return Err(SemanticError::incorrect(
                "program/not-entity-predicate",
                format!("active program {name} has no entity-predicate body"),
            ));
        }
    }
    Ok(())
}

fn predicate_roles(
    database: &DatabaseValue,
    names: &BTreeSet<String>,
    function_entities: &BTreeSet<u64>,
) -> Result<BTreeMap<String, PredicateRole>, SemanticError> {
    let mut roles = BTreeMap::<String, (bool, bool)>::new();
    for name in database
        .schema()
        .attributes()
        .flat_map(|attribute| &attribute.predicates)
    {
        let affected = names.contains(name)
            || (!function_entities.is_empty()
                && database
                    .entid(&qualified_program_ident(name)?)
                    .is_some_and(|entity| function_entities.contains(&entity)));
        if affected {
            roles.entry(name.clone()).or_default().0 = true;
        }
    }
    for datom in database.datoms_with_prefix(&crate::IndexPrefix::Aevt {
        attribute: crate::DB_ENTITY_PREDS as u32,
        entity: None,
        value: None,
    })? {
        let Value::Symbol(symbol) = &datom.value else {
            return Err(fault(
                "postgres/invalid-entity-predicate",
                "current :db.entity/preds information is not a symbol",
            ));
        };
        let name = symbol.qualified_name();
        let affected = names.contains(&name)
            || (!function_entities.is_empty()
                && database
                    .entid(&qualified_program_ident(&name)?)
                    .is_some_and(|entity| function_entities.contains(&entity)));
        if affected {
            roles.entry(name).or_default().1 = true;
        }
    }
    roles
        .into_iter()
        .map(|(name, (attribute, entity))| {
            let role = match (attribute, entity) {
                (true, false) => PredicateRole::Attribute,
                (false, true) => PredicateRole::Entity,
                (true, true) => PredicateRole::Both,
                (false, false) => unreachable!("only operative names are inserted"),
            };
            Ok((name, role))
        })
        .collect()
}

pub(crate) fn persisted_predicates(
    resolve: &mut ProgramResolver<'_>,
    database: &DatabaseValue,
    required: &BTreeMap<String, PredicateRole>,
    shared_budget: SharedProgramBudget,
) -> Result<TxFunctions, SemanticError> {
    let mut functions = TxFunctions::new();
    for (name, role) in required {
        let hash = bound_program_hash(database, &qualified_program_ident(name)?)?;
        let program = resolve(hash)?;
        if role.requires_attribute() {
            if !program.program().supports_attribute_predicate() {
                return Err(SemanticError::incorrect(
                    "program/not-attribute-predicate",
                    format!("active program {name} has no attribute-predicate body"),
                ));
            }
            let program = Arc::clone(&program);
            let budget = Arc::clone(&shared_budget);
            functions.register_attribute_value_predicate(name.clone(), move |value| {
                let mut budget = budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                ProgramRuntime.execute_prevalidated_attribute_predicate_with_budget(
                    &program,
                    value,
                    &mut budget,
                )
            });
        }
        if role.requires_entity() {
            if !program.program().supports_entity_predicate() {
                return Err(SemanticError::incorrect(
                    "program/not-entity-predicate",
                    format!("active program {name} has no entity-predicate body"),
                ));
            }
            let program = Arc::clone(&program);
            let budget = Arc::clone(&shared_budget);
            functions.register_entity_value_predicate(name.clone(), move |db_after, entity| {
                let mut budget = budget.lock().map_err(|_| {
                    fault(
                        "program/budget-poisoned",
                        "transaction program budget mutex was poisoned",
                    )
                })?;
                ProgramRuntime.execute_prevalidated_entity_predicate_exact_with_budget(
                    &program,
                    db_after,
                    entity,
                    &mut budget,
                )
            });
        }
    }
    Ok(functions)
}

/// All code carried by transaction information, including retractions: history
/// also retains content identity, just as durable per-generation references do.
pub(crate) fn transaction_program_roots(datoms: &[Datom]) -> BTreeSet<Digest> {
    let mut roots = BTreeSet::new();
    for datom in datoms {
        collect_program_hashes(&datom.value, &mut roots);
    }
    roots
}

pub(crate) fn collect_program_hashes(value: &Value, output: &mut BTreeSet<Digest>) {
    let mut pending = vec![value];
    while let Some(value) = pending.pop() {
        match value {
            Value::Function(hash) => {
                output.insert(*hash);
            }
            Value::Tuple(values) => pending.extend(values.iter().flatten()),
            _ => {}
        }
    }
}

/// Resolve the complete immutable fixed-dependency closure once per hash.
/// Native speculative values retain this map, independently of evictable
/// shared caches and the lifetime of unreferenced deployed PostgreSQL blobs.
#[cfg(test)]
pub(crate) fn resolve_program_closure(
    resolve: &mut ProgramResolver<'_>,
    roots: BTreeSet<Digest>,
) -> Result<ResolvedPrograms, SemanticError> {
    let mut resolved = BTreeMap::new();
    visit_program_closure(resolve, roots, &mut |hash, program| {
        resolved.insert(hash, program);
    })?;
    Ok(resolved)
}

/// Durable marking needs authenticated reachability but not resident program
/// bodies. Share traversal with speculation without changing that footprint.
pub(crate) fn visit_program_closure(
    resolve: &mut ProgramResolver<'_>,
    roots: BTreeSet<Digest>,
    visit: &mut dyn FnMut(ProgramHash, Arc<ValidatedProgram>),
) -> Result<BTreeSet<Digest>, SemanticError> {
    let mut pending = roots.into_iter().collect::<Vec<_>>();
    let mut reachable = BTreeSet::new();
    while let Some(hash) = pending.pop() {
        if !reachable.insert(hash) {
            continue;
        }
        let program = resolve(hash)?;
        collect_fixed_program_dependencies(&program.program().instructions, &mut pending);
        visit(hash, program);
    }
    Ok(reachable)
}

fn collect_fixed_program_dependencies(
    instructions: &[crate::program::Instruction],
    output: &mut Vec<Digest>,
) {
    use crate::program::{Instruction, QueryTerm};
    let mut pending = instructions.iter().collect::<Vec<_>>();
    while let Some(instruction) = pending.pop() {
        match instruction {
            Instruction::PushConstant(value) => collect_program_hashes_vec(value, output),
            Instruction::PushEntity(reference) => collect_entity_program_hashes(reference, output),
            Instruction::If {
                then_branch,
                else_branch,
            } => {
                pending.extend(then_branch);
                pending.extend(else_branch);
            }
            Instruction::PredicateDispatch { attribute, entity } => {
                pending.extend(attribute);
                pending.extend(entity);
            }
            Instruction::ForEach { body } => pending.extend(body),
            Instruction::Query(query) => {
                for pattern in query.patterns() {
                    for term in [&pattern.entity, &pattern.value] {
                        if let QueryTerm::Constant(value) = term {
                            collect_program_hashes_vec(value, output);
                        }
                    }
                }
            }
            Instruction::EmitCall { function, .. } => match function {
                CallableRef::ExactHash(hash) => output.push(*hash),
                CallableRef::Database(reference) => {
                    collect_entity_program_hashes(reference, output)
                }
                CallableRef::Local(_) => {}
            },
            _ => {}
        }
    }
}

fn collect_entity_program_hashes(reference: &crate::EntityRef, output: &mut Vec<Digest>) {
    match reference {
        crate::EntityRef::Lookup { value, .. } => collect_program_hashes_vec(value, output),
        crate::EntityRef::LookupInput { value, .. } => collect_input_program_hashes(value, output),
        crate::EntityRef::Id(_)
        | crate::EntityRef::Ident(_)
        | crate::EntityRef::Temp(_)
        | crate::EntityRef::Tx => {}
    }
}

fn collect_input_program_hashes(value: &TxValue, output: &mut Vec<Digest>) {
    let mut pending = vec![value];
    while let Some(value) = pending.pop() {
        match value {
            TxValue::Scalar(value) => collect_program_hashes_vec(value, output),
            TxValue::Tuple(values) => pending.extend(values.iter().flatten()),
            TxValue::Entity(crate::EntityRef::LookupInput { value, .. }) => pending.push(value),
            TxValue::Entity(reference) => collect_entity_program_hashes(reference, output),
        }
    }
}

fn collect_program_hashes_vec(value: &Value, output: &mut Vec<Digest>) {
    let mut hashes = BTreeSet::new();
    collect_program_hashes(value, &mut hashes);
    output.extend(hashes);
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::{EntityRef, Instruction, Program};

    #[test]
    fn initial_and_nested_generation_share_one_unchanged_db_before() {
        use crate::{Attribute, Cardinality, Database, Keyword, Schema, TxOp, ValueType};
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1_000,
                Keyword::new("account", "balance"),
                ValueType::Long,
                Cardinality::One,
            ))
            .unwrap();
        let entity = crate::make_eid(crate::USER_PARTITION, 42).unwrap();
        let database = Database::new(schema)
            .unwrap()
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: 1_000,
                    value: Value::Long(100).into(),
                }],
                1_000,
            )
            .unwrap()
            .db_after
            .database_value();
        let increment = Program {
            kind: ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::Duplicate,
                Instruction::LoadOne(1_000),
                Instruction::PushConstant(Value::Long(1)),
                Instruction::Add,
                Instruction::EmitAdd(1_000),
                Instruction::Return,
            ],
        };
        let increment_hash = crate::program_hash(&increment).unwrap();
        let nested = Program {
            kind: ProgramKind::Transaction,
            arity: 1,
            instructions: vec![
                Instruction::PushArgument(0),
                Instruction::EmitCall {
                    function: CallableRef::ExactHash(increment_hash),
                    argument_count: 1,
                },
                Instruction::Return,
            ],
        };
        let nested_hash = crate::program_hash(&nested).unwrap();
        let programs = BTreeMap::from([
            (
                increment_hash,
                Arc::new(ValidatedProgram::from_canonical(increment)),
            ),
            (
                nested_hash,
                Arc::new(ValidatedProgram::from_canonical(nested)),
            ),
        ]);
        let direct = TxForm::Op(TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: 1_000,
            value: Value::Long(900).into(),
        });
        let mut forms = vec![direct.clone()];
        for hash in [increment_hash, nested_hash] {
            forms.push(TxForm::ProgramCall(ProgramCall {
                function: CallableRef::ExactHash(hash),
                arguments: vec![Value::Ref(entity).into()],
            }));
        }
        let mut budget = ProgramBudget::new(crate::ProgramControl::default()).unwrap();
        let expanded = expand_submission_forms(
            &mut |hash| Ok(Arc::clone(&programs[&hash])),
            &database,
            &forms,
            &mut budget,
        )
        .unwrap();
        assert_eq!(format!("{:?}", expanded[0]), format!("{direct:?}"));
        assert_eq!(expanded.len(), 3);
        for form in &expanded[1..] {
            let TxForm::Op(TxOp::Add { value, .. }) = form else {
                panic!("unexpected generated form: {form:?}");
            };
            assert_eq!(*value, TxValue::Scalar(Value::Long(101)));
        }
        assert_eq!(
            database.values(entity, 1_000).unwrap(),
            vec![Value::Long(100)]
        );

        forms.reverse();
        let mut budget = ProgramBudget::new(crate::ProgramControl::default()).unwrap();
        let reversed_expansion = expand_submission_forms(
            &mut |hash| Ok(Arc::clone(&programs[&hash])),
            &database,
            &forms,
            &mut budget,
        )
        .unwrap();
        assert_eq!(format!("{reversed_expansion:?}"), format!("{expanded:?}"));
    }

    #[test]
    fn fixed_dependencies_include_structured_lookup_and_dual_predicate_bodies() {
        let instructions = vec![
            Instruction::PushEntity(EntityRef::LookupInput {
                attribute: 1_000,
                value: Box::new(TxValue::Tuple(vec![
                    Some(TxValue::Entity(EntityRef::LookupInput {
                        attribute: 1_001,
                        value: Box::new(TxValue::Scalar(Value::Function([1; 32]))),
                    })),
                    None,
                ])),
            }),
            Instruction::EmitCall {
                function: CallableRef::Database(EntityRef::LookupInput {
                    attribute: 1_002,
                    value: Box::new(TxValue::Scalar(Value::Function([2; 32]))),
                }),
                argument_count: 0,
            },
            Instruction::PredicateDispatch {
                attribute: vec![Instruction::PushConstant(Value::Function([3; 32]))],
                entity: vec![Instruction::PushConstant(Value::Function([4; 32]))],
            },
            Instruction::If {
                then_branch: vec![Instruction::EmitCall {
                    function: CallableRef::ExactHash([5; 32]),
                    argument_count: 0,
                }],
                else_branch: vec![Instruction::ForEach {
                    body: vec![Instruction::PushEntity(EntityRef::Lookup {
                        attribute: 1_003,
                        value: Value::Tuple(vec![Some(Value::Function([6; 32])), None]),
                    })],
                }],
            },
        ];
        let mut dependencies = Vec::new();
        collect_fixed_program_dependencies(&instructions, &mut dependencies);
        assert_eq!(
            dependencies.into_iter().collect::<BTreeSet<_>>(),
            (1..=6).map(|marker| [marker; 32]).collect()
        );
    }

    fn code(dependencies: &[ProgramHash]) -> (ProgramHash, Arc<ValidatedProgram>) {
        let mut instructions = dependencies
            .iter()
            .flat_map(|hash| {
                [
                    Instruction::PushConstant(Value::Function(*hash)),
                    Instruction::Pop,
                ]
            })
            .collect::<Vec<_>>();
        instructions.push(Instruction::Return);
        let program = Program {
            kind: ProgramKind::Transaction,
            arity: 0,
            instructions,
        };
        let hash = crate::program_hash(&program).unwrap();
        (hash, Arc::new(ValidatedProgram::from_canonical(program)))
    }

    #[test]
    fn closure_resolves_shared_transitive_content_once_and_propagates_missing_content() {
        let (leaf_hash, leaf) = code(&[]);
        let (middle_hash, middle) = code(&[leaf_hash]);
        let (root_hash, root) = code(&[middle_hash, leaf_hash]);
        let programs =
            BTreeMap::from([(leaf_hash, leaf), (middle_hash, middle), (root_hash, root)]);
        let mut loads = BTreeMap::<ProgramHash, u64>::new();
        let resolved = resolve_program_closure(
            &mut |hash| {
                *loads.entry(hash).or_default() += 1;
                Ok(Arc::clone(&programs[&hash]))
            },
            BTreeSet::from([root_hash, middle_hash]),
        )
        .unwrap();
        assert_eq!(
            resolved.keys().copied().collect::<BTreeSet<_>>(),
            programs.keys().copied().collect()
        );
        assert!(loads.values().all(|loads| *loads == 1));

        let error = resolve_program_closure(
            &mut |hash| {
                if hash == leaf_hash {
                    Err(SemanticError::new(
                        ErrorCategory::NotFound,
                        "test/missing-content",
                        "missing",
                    ))
                } else {
                    Ok(Arc::clone(&programs[&hash]))
                }
            },
            BTreeSet::from([root_hash]),
        )
        .unwrap_err();
        assert_eq!(error.code, "test/missing-content");
    }

    #[test]
    fn transaction_roots_retain_retracted_and_tuple_nested_function_content() {
        let datoms = [
            Datom {
                entity: 1,
                attribute: 1_000,
                value: Value::Function([1; 32]),
                tx: 1,
                added: false,
            },
            Datom {
                entity: 2,
                attribute: 1_001,
                value: Value::Tuple(vec![Some(Value::Function([2; 32])), None]),
                tx: 1,
                added: true,
            },
        ];
        assert_eq!(
            transaction_program_roots(&datoms),
            BTreeSet::from([[1; 32], [2; 32]])
        );
    }
}
