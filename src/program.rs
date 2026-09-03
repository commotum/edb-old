use crate::{Database, Digest, EntityRef, ErrorCategory, SemanticError, TxOp, TxValue, Value};
use std::cmp::Ordering;
use std::sync::atomic::{AtomicBool, Ordering as AtomicOrdering};

pub type ProgramHash = Digest;

const MAX_ARITY: u8 = 10;
const MAX_INSTRUCTIONS: usize = 4_096;

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ProgramKind {
    Transaction,
    AttributePredicate,
    Query,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub enum Instruction {
    PushArgument(u8),
    PushConstant(Value),
    Duplicate,
    Pop,
    LoadOne(u32),
    Exists(u32),
    Add,
    Subtract,
    Multiply,
    Divide,
    Equal,
    LessThan,
    GreaterThan,
    Not,
    And,
    Or,
    Require {
        category: ErrorCategory,
        message: String,
    },
    EmitAdd(u32),
    EmitRetract(u32),
    EmitRow(u8),
    Return,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct Program {
    pub kind: ProgramKind,
    pub arity: u8,
    pub instructions: Vec<Instruction>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct ProgramInvocation {
    pub hash: ProgramHash,
    pub arguments: Vec<Value>,
}

#[derive(Clone, Copy, Debug)]
pub struct ProgramControl<'a> {
    pub fuel: u64,
    pub max_stack: usize,
    pub max_output: usize,
    pub cancelled: Option<&'a AtomicBool>,
}

impl Default for ProgramControl<'_> {
    fn default() -> Self {
        Self {
            fuel: 100_000,
            max_stack: 1_024,
            max_output: 100_000,
            cancelled: None,
        }
    }
}

#[derive(Clone, Debug)]
pub enum ProgramOutput {
    Transaction(Vec<TxOp>),
    AttributePredicate(bool),
    Query(Vec<Vec<Value>>),
}

#[derive(Clone, Debug, Default)]
pub struct ProgramRuntime;

impl Program {
    pub fn validate(&self) -> Result<(), SemanticError> {
        if self.arity > MAX_ARITY {
            return Err(incorrect(
                "program/arity-limit",
                format!("program arity exceeds {MAX_ARITY}"),
            ));
        }
        if self.instructions.is_empty() || self.instructions.len() > MAX_INSTRUCTIONS {
            return Err(incorrect(
                "program/instruction-limit",
                format!("program must contain 1 to {MAX_INSTRUCTIONS} instructions"),
            ));
        }
        if !matches!(self.instructions.last(), Some(Instruction::Return))
            || self.instructions[..self.instructions.len() - 1]
                .iter()
                .any(|instruction| matches!(instruction, Instruction::Return))
        {
            return Err(incorrect(
                "program/noncanonical-return",
                "program must contain exactly one final return instruction",
            ));
        }

        let mut depth: isize = 0;
        let mut emits_rows = false;
        for instruction in &self.instructions {
            if let Instruction::PushArgument(index) = instruction
                && *index >= self.arity
            {
                return Err(incorrect(
                    "program/argument-index",
                    format!("argument {index} is outside arity {}", self.arity),
                ));
            }
            if self.kind == ProgramKind::AttributePredicate
                && matches!(
                    instruction,
                    Instruction::LoadOne(_) | Instruction::Exists(_)
                )
            {
                return Err(incorrect(
                    "program/predicate-database-read",
                    "attribute predicates may inspect only their value argument",
                ));
            }
            if self.kind != ProgramKind::Transaction
                && matches!(
                    instruction,
                    Instruction::EmitAdd(_) | Instruction::EmitRetract(_)
                )
            {
                return Err(incorrect(
                    "program/output-kind",
                    "only transaction programs may emit transaction operations",
                ));
            }
            if matches!(instruction, Instruction::EmitRow(0)) {
                return Err(incorrect(
                    "program/empty-row",
                    "query rows must contain at least one value",
                ));
            }
            if self.kind != ProgramKind::Query && matches!(instruction, Instruction::EmitRow(_)) {
                return Err(incorrect(
                    "program/output-kind",
                    "only query programs may emit result rows",
                ));
            }
            emits_rows |= matches!(instruction, Instruction::EmitRow(_));
            let (needed, delta) = stack_effect(instruction);
            if depth < needed {
                return Err(incorrect(
                    "program/static-stack-underflow",
                    "instruction consumes more values than are available",
                ));
            }
            depth += delta;
        }
        let valid_result = match self.kind {
            ProgramKind::Transaction => depth == 0,
            ProgramKind::AttributePredicate => self.arity == 1 && depth == 1,
            ProgramKind::Query => depth > 0 || emits_rows,
        };
        if !valid_result {
            return Err(incorrect(
                "program/result-shape",
                "final stack does not match the program kind",
            ));
        }
        Ok(())
    }
}

impl ProgramRuntime {
    pub fn execute(
        &self,
        program: &Program,
        database: &Database,
        arguments: &[Value],
        control: ProgramControl<'_>,
    ) -> Result<ProgramOutput, SemanticError> {
        program.validate()?;
        if arguments.len() != usize::from(program.arity) {
            return Err(incorrect(
                "program/arity",
                format!(
                    "program expects {} arguments, received {}",
                    program.arity,
                    arguments.len()
                ),
            ));
        }
        if control.max_stack == 0 || control.max_output == 0 {
            return Err(busy(
                "program/resource-limit",
                "program limits must be positive",
            ));
        }

        let mut fuel = control.fuel;
        let mut stack = Vec::new();
        let mut operations = Vec::new();
        let mut query_rows = Vec::new();
        for instruction in &program.instructions {
            check_cancel(control.cancelled)?;
            charge(&mut fuel, 1)?;
            match instruction {
                Instruction::PushArgument(index) => {
                    stack.push(arguments[usize::from(*index)].clone());
                }
                Instruction::PushConstant(value) => stack.push(value.clone()),
                Instruction::Duplicate => {
                    let value = top(&stack)?.clone();
                    stack.push(value);
                }
                Instruction::Pop => {
                    pop(&mut stack)?;
                }
                Instruction::LoadOne(attribute) => {
                    let entity = entity_id(pop(&mut stack)?)?;
                    let values = database.values(entity, *attribute);
                    charge(&mut fuel, values.len() as u64)?;
                    match values.as_slice() {
                        [value] => stack.push((*value).clone()),
                        [] => {
                            return Err(incorrect(
                                "program/missing-value",
                                format!("entity {entity} has no value for attribute {attribute}"),
                            ));
                        }
                        _ => {
                            return Err(incorrect(
                                "program/not-cardinality-one",
                                format!("attribute {attribute} has multiple values"),
                            ));
                        }
                    }
                }
                Instruction::Exists(attribute) => {
                    let entity = entity_id(pop(&mut stack)?)?;
                    let values = database.values(entity, *attribute);
                    charge(&mut fuel, values.len() as u64)?;
                    stack.push(Value::Bool(!values.is_empty()));
                }
                Instruction::Add => binary_long(&mut stack, "add", i64::checked_add)?,
                Instruction::Subtract => binary_long(&mut stack, "subtract", i64::checked_sub)?,
                Instruction::Multiply => binary_long(&mut stack, "multiply", i64::checked_mul)?,
                Instruction::Divide => {
                    let right = long(pop(&mut stack)?)?;
                    let left = long(pop(&mut stack)?)?;
                    let result = left.checked_div(right).ok_or_else(|| {
                        incorrect("program/arithmetic", "division by zero or integer overflow")
                    })?;
                    stack.push(Value::Long(result));
                }
                Instruction::Equal => {
                    let right = pop(&mut stack)?;
                    let left = pop(&mut stack)?;
                    stack.push(Value::Bool(left.stored_eq(&right)));
                }
                Instruction::LessThan => compare(&mut stack, Ordering::Less)?,
                Instruction::GreaterThan => compare(&mut stack, Ordering::Greater)?,
                Instruction::Not => {
                    let value = boolean(pop(&mut stack)?)?;
                    stack.push(Value::Bool(!value));
                }
                Instruction::And => binary_bool(&mut stack, |left, right| left && right)?,
                Instruction::Or => binary_bool(&mut stack, |left, right| left || right)?,
                Instruction::Require { category, message } => {
                    if !boolean(pop(&mut stack)?)? {
                        return Err(SemanticError::new(
                            allowed_rejection_category(*category)?,
                            "program/rejected",
                            message.clone(),
                        ));
                    }
                }
                Instruction::EmitAdd(attribute) => {
                    let value = pop(&mut stack)?;
                    let entity = entity_id(pop(&mut stack)?)?;
                    operations.push(TxOp::Add {
                        entity: EntityRef::Id(entity),
                        attribute: *attribute,
                        value: TxValue::Scalar(value),
                    });
                }
                Instruction::EmitRetract(attribute) => {
                    let value = pop(&mut stack)?;
                    let entity = entity_id(pop(&mut stack)?)?;
                    operations.push(TxOp::Retract {
                        entity: EntityRef::Id(entity),
                        attribute: *attribute,
                        value: Some(TxValue::Scalar(value)),
                    });
                }
                Instruction::EmitRow(width) => {
                    let width = usize::from(*width);
                    let at = stack.len().checked_sub(width).ok_or_else(|| {
                        incorrect("program/stack-underflow", "query row exceeds program stack")
                    })?;
                    query_rows.push(stack.split_off(at));
                }
                Instruction::Return => {}
            }
            if stack.len() > control.max_stack {
                return Err(busy(
                    "program/stack-limit",
                    "program exceeded its stack limit",
                ));
            }
            if operations.len() > control.max_output {
                return Err(busy(
                    "program/output-limit",
                    "program exceeded its output limit",
                ));
            }
            if query_rows
                .iter()
                .map(Vec::len)
                .sum::<usize>()
                .saturating_add(stack.len())
                > control.max_output
            {
                return Err(busy(
                    "program/output-limit",
                    "program exceeded its output limit",
                ));
            }
        }

        match program.kind {
            ProgramKind::Transaction => Ok(ProgramOutput::Transaction(operations)),
            ProgramKind::AttributePredicate => Ok(ProgramOutput::AttributePredicate(boolean(
                pop(&mut stack)?,
            )?)),
            ProgramKind::Query => {
                if !stack.is_empty() {
                    query_rows.push(stack);
                }
                Ok(ProgramOutput::Query(query_rows))
            }
        }
    }
}

fn stack_effect(instruction: &Instruction) -> (isize, isize) {
    match instruction {
        Instruction::PushArgument(_) | Instruction::PushConstant(_) => (0, 1),
        Instruction::Duplicate => (1, 1),
        Instruction::Pop | Instruction::Require { .. } => (1, -1),
        Instruction::LoadOne(_) | Instruction::Exists(_) | Instruction::Not => (1, 0),
        Instruction::Add
        | Instruction::Subtract
        | Instruction::Multiply
        | Instruction::Divide
        | Instruction::Equal
        | Instruction::LessThan
        | Instruction::GreaterThan
        | Instruction::And
        | Instruction::Or => (2, -1),
        Instruction::EmitAdd(_) | Instruction::EmitRetract(_) => (2, -2),
        Instruction::EmitRow(width) => (isize::from(*width), -isize::from(*width)),
        Instruction::Return => (0, 0),
    }
}

fn pop(stack: &mut Vec<Value>) -> Result<Value, SemanticError> {
    stack
        .pop()
        .ok_or_else(|| incorrect("program/stack-underflow", "program stack is empty"))
}

fn top(stack: &[Value]) -> Result<&Value, SemanticError> {
    stack
        .last()
        .ok_or_else(|| incorrect("program/stack-underflow", "program stack is empty"))
}

fn long(value: Value) -> Result<i64, SemanticError> {
    if let Value::Long(value) = value {
        Ok(value)
    } else {
        Err(incorrect(
            "program/type",
            "integer arithmetic requires long values",
        ))
    }
}

fn boolean(value: Value) -> Result<bool, SemanticError> {
    if let Value::Bool(value) = value {
        Ok(value)
    } else {
        Err(incorrect(
            "program/type",
            "boolean operation requires boolean values",
        ))
    }
}

fn entity_id(value: Value) -> Result<u64, SemanticError> {
    if let Value::Ref(value) = value {
        Ok(value)
    } else {
        Err(incorrect(
            "program/type",
            "database access and transaction emission require a ref entity",
        ))
    }
}

fn binary_long(
    stack: &mut Vec<Value>,
    operation: &'static str,
    function: fn(i64, i64) -> Option<i64>,
) -> Result<(), SemanticError> {
    let right = long(pop(stack)?)?;
    let left = long(pop(stack)?)?;
    stack.push(Value::Long(function(left, right).ok_or_else(|| {
        incorrect(
            "program/arithmetic",
            format!("integer overflow during {operation}"),
        )
    })?));
    Ok(())
}

fn binary_bool(
    stack: &mut Vec<Value>,
    function: fn(bool, bool) -> bool,
) -> Result<(), SemanticError> {
    let right = boolean(pop(stack)?)?;
    let left = boolean(pop(stack)?)?;
    stack.push(Value::Bool(function(left, right)));
    Ok(())
}

fn compare(stack: &mut Vec<Value>, expected: Ordering) -> Result<(), SemanticError> {
    let right = pop(stack)?;
    let left = pop(stack)?;
    stack.push(Value::Bool(left.index_cmp(&right) == expected));
    Ok(())
}

fn charge(fuel: &mut u64, amount: u64) -> Result<(), SemanticError> {
    *fuel = fuel
        .checked_sub(amount)
        .ok_or_else(|| busy("program/fuel-exhausted", "program exhausted its fuel"))?;
    Ok(())
}

fn check_cancel(cancelled: Option<&AtomicBool>) -> Result<(), SemanticError> {
    if cancelled.is_some_and(|flag| flag.load(AtomicOrdering::Relaxed)) {
        Err(SemanticError::new(
            ErrorCategory::Interrupted,
            "program/cancelled",
            "program execution was cancelled",
        ))
    } else {
        Ok(())
    }
}

fn allowed_rejection_category(category: ErrorCategory) -> Result<ErrorCategory, SemanticError> {
    match category {
        ErrorCategory::Incorrect | ErrorCategory::Conflict | ErrorCategory::Forbidden => {
            Ok(category)
        }
        _ => Err(incorrect(
            "program/rejection-category",
            "program rejection category must be incorrect, conflict, or forbidden",
        )),
    }
}

fn incorrect(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Incorrect, code, message)
}

fn busy(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Busy, code, message)
}
