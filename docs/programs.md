# Persisted native queries

`QueryTemplate::new` remains the original version-1 conjunctive query format.
Its bytes, hashes and existing request meaning are unchanged. Use
`QueryTemplate::native(query, input_arguments, sources)` for version 2: it embeds
the ordinary Rust `Query` AST and selects program ABI 7 only for programs that
contain a version-2 template. Existing ABI 4/5/6 programs remain readable.

For example, this query program returns entity/value rows above a threshold;
the attribute itself is a program argument, not a fixed schema assumption:

```rust
use atomic_core::*;

fn selector() -> Result<Program, SemanticError> {
    let mut query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable("entity".into()),
            FindElement::Variable("value".into()),
        ]),
        vec![
            Clause::Pattern(Box::new(DataPattern::new(
                Term::var("entity"), Term::var("attribute"), Term::var("value"),
            ))),
            Clause::Predicate {
                predicate: Predicate::GreaterOrEqual,
                source: "$".into(),
                args: vec![Term::var("value"), Term::var("minimum")],
            },
        ],
    );
    query.inputs = vec![
        InputSpec::Scalar("attribute".into()),
        InputSpec::Scalar("minimum".into()),
    ];
    Ok(Program {
        kind: ProgramKind::Query,
        arity: 2,
        instructions: vec![
            Instruction::Query(QueryTemplate::native(query, vec![0, 1], vec![])?),
            Instruction::ForEach {
                body: vec![Instruction::Unpack(2), Instruction::EmitRow(2)],
            },
            Instruction::Return,
        ],
    })
}
```

Invoke it with `ProgramRuntime::execute_query` and one captured `&DatabaseValue`,
passing an attribute ref/ident and the threshold. Deploying the same artifact
uses `PostgresStore::deploy_program_blob`. Transaction-kind programs can consume
the query result using ordinary VM instructions and emit transaction forms;
they still run against the transaction's immutable db-before. Deployment does
not install a function binding: ordinary `:db/ident` and `:db/fn` assertions do.

Native templates support the shared evaluator's predicates, functions, recursive
rules, `not`/`not-join`, `or`/`or-join`, dynamic attributes, aggregates and Pull.
Input mappings are positional program-argument indexes; tuple, collection and
relation inputs use runtime vectors. All named database sources derive from the
invocation's exact value, including its existing filters:

- Empty sources mean `QueryTemplateSource::current("$")`.
- `QueryTemplateSource::history("$history")` exposes assertions and retractions.
- `.as_of(QueryTemplateTime::Argument(2))` and `.since(...)` accept a nonnegative
  Long T, a transaction Ref, or an Instant argument. `Literal(TimePoint::...)`
  embeds an explicit bound. Bounds compose with the captured value's view.
- `QueryTemplateSource::log("$log")` supplies its actual captured committed log
  to `Function::TxIds` / `TxData`. Database time filters do not truncate that
  log; use explicit log-function ranges. Eager fixtures, speculative values and
  opaque predicate filters cannot supply a committed log and return Unsupported.
  Database history is never substituted for a log, including with `noHistory`.

The evaluator shares the enclosing program's fuel, deadline, cancellation,
row/collection and retained-value allocation limits. Failed query work is not
refunded when a caller reuses a `ProgramBudget`. Durable ASTs also have bounded
depth, node counts and the existing 4 MiB program payload limit. These are
cooperative limits, not hard allocator or PostgreSQL preemption guarantees.

This is native serializable code, not closure/JVM serialization: callback
extensions, callback Pull transforms and random/sample aggregates are rejected.
Query containers become existing VM vectors/maps; VM map keys remain stored
scalar values, so non-scalar Pull aliases return `program/query-map-key`.
Emitted query rows still contain stored scalar values; use VM operations to
consume nested query/Pull results before emitting them.

[The lifecycle fixture](../tests/program_native_queries.rs) combines all five
required capabilities in one recursive selection, exercises speculative and
committed execution, retained prior bindings, exact retries before/after restart,
and verifies independently captured version-1 bytes. Run its actual PostgreSQL
checks with `ATOMIC_POSTGRES_URL` set; it creates a disposable isolated schema.
