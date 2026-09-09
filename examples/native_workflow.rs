//! A repeatable public-API workflow. Run against a disposable PostgreSQL database:
//! ATOMIC_POSTGRES_URL='host=... user=... dbname=...' cargo run --example native_workflow
use atomic_core::{
    Attribute, AttributeName, CapacityLimits, Cardinality, Clause, Connection, DataPattern,
    EntityRef, FindElement, FindSpec, IndexOrder, Keyword, PostgresMigrator, PostgresStore,
    PullAttribute, PullPattern, Query, QueryControl, QueryResult, QueryValue, Schema, Term,
    TransactionRequest, TransactionService, TransactionServiceConfig, TxOp, Value, ValueType,
    Variable,
};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const NAME: u32 = 1_000;

fn main() -> Result<(), Box<dyn std::error::Error>> {
    let postgres = std::env::var("ATOMIC_POSTGRES_URL")?;
    let database_id = format!(
        "workflow-{}-{}",
        std::process::id(),
        SystemTime::now().duration_since(UNIX_EPOCH)?.as_nanos()
    );
    // Provisioning is explicit. All subsequent application work uses Connection.
    PostgresMigrator::connect(&postgres)?.migrate()?;
    PostgresStore::connect(&postgres)?.create_database(&database_id, Schema::new())?;
    let config = TransactionServiceConfig {
        connection: postgres,
        database_id: database_id.clone(),
        holder_id: format!("workflow-{}", std::process::id()),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 32,
        capacity_limits: CapacityLimits::default(),
    };
    let writer = TransactionService::start(config.clone())?;
    let connection = Connection::attach(&config.connection, writer.client(), 64)?;
    let reader = Connection::connect(&config.connection, &database_id, 64)?;
    let timeout = Duration::from_secs(10);
    connection.transact(
        TransactionRequest::new(
            "schema",
            vec![TxOp::InstallAttribute(Attribute::new(
                NAME,
                Keyword::new("person", "name"),
                ValueType::String,
                Cardinality::One,
            ))],
        ),
        timeout,
    )?;
    let inserted = connection.transact(
        TransactionRequest::new(
            "insert",
            vec![TxOp::Add {
                entity: EntityRef::Temp("person".into()),
                attribute: NAME,
                value: Value::String("Ada".into()).into(),
            }],
        ),
        timeout,
    )?;
    let person = inserted.tempids["person"];
    let original = connection.db();
    let variable = Variable::new("name")?;
    let query = Query::new(
        FindSpec::Collection(FindElement::Variable(variable.clone())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Constant(Value::Ref(person)),
            Term::Constant(Value::Keyword(Keyword::new("person", "name"))),
            Term::Variable(variable),
        )))],
    );
    let names = |database: &atomic_core::DatabaseValue| {
        database
            .query(&query, &[], &QueryControl::default())
            .map(|outcome| outcome.result)
    };
    assert_eq!(
        names(&original)?,
        QueryResult::Collection(vec![QueryValue::Scalar(Value::String("Ada".into()))])
    );
    let pattern = PullPattern {
        wildcard: false,
        attributes: vec![PullAttribute::forward(AttributeName::Ident(Keyword::new(
            "person", "name",
        )))],
    };
    let original_pull = original.pull(&pattern, person)?;
    let changed = connection.transact(
        TransactionRequest::new(
            "rename",
            vec![TxOp::Add {
                entity: EntityRef::Id(person),
                attribute: NAME,
                value: Value::String("Ada Lovelace".into()).into(),
            }],
        ),
        timeout,
    )?;
    assert_eq!(original.pull(&pattern, person)?, original_pull);
    assert_eq!(
        connection
            .db()
            .as_of(original.basis_t())
            .pull(&pattern, person)?,
        original_pull
    );
    let history: Vec<_> = connection
        .db()
        .history()
        .scan_cursor(IndexOrder::Eavt)?
        .filter_map(|row| match row {
            Ok(datom) if datom.entity == person && datom.attribute == NAME => Some(Ok(datom)),
            Ok(_) => None,
            Err(error) => Some(Err(error)),
        })
        .collect::<Result<_, _>>()?;
    assert_eq!(history.len(), 3);
    assert_eq!(history.iter().filter(|datom| !datom.added).count(), 1);
    let expected = names(&connection.db())?;
    assert_eq!(
        expected,
        QueryResult::Collection(vec![QueryValue::Scalar(Value::String(
            "Ada Lovelace".into()
        ))])
    );
    let identity = connection.identity().clone();
    assert_eq!(names(&reader.sync_to(changed.basis_t, timeout)?)?, expected);
    assert_eq!(reader.load_stats().compatibility_materializations, 0);
    drop(connection);
    writer.shutdown();
    // Reads and reopening do not acquire or retain a writer lease.
    assert_eq!(names(&reader.db())?, expected);
    let reopened = Connection::connect(&config.connection, &database_id, 64)?;
    assert_eq!(reopened.identity(), &identity);
    assert_eq!(reopened.db().basis_t(), changed.basis_t);
    assert_eq!(names(&reopened.db())?, expected);
    assert_eq!(original.pull(&pattern, person)?, original_pull);
    println!(
        "PASS {database_id}: schema, transact, query, pull, history, immutable values, reopen at t={}",
        changed.basis_t
    );
    Ok(())
}
