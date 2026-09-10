//! A separate application for the supported local transactor. See docs/application.md.
use atomic_core::{
    Attribute, Cardinality, Clause, Connection, DataPattern, Database, DatabaseValue, Entity,
    EntityRef, EntityValue, FindElement, FindSpec, IndexPrefix, Keyword, Query, QueryControl,
    QueryResult, QueryValue, Schema, SemanticError, TransactionRequest, TxOp, TxValue, Unique,
    Value, ValueType, Variable, postgres_config_from_env,
};
use std::collections::BTreeMap;
use std::path::PathBuf;
use std::time::{Duration, Instant};

type Result<T> = std::result::Result<T, Box<dyn std::error::Error>>;
const PROJECT: u32 = 1_000;
const HOURS: u32 = 1_001;
const OWNER: u32 = 1_002;
const NAME: u32 = 1_003;
const WAIT: Duration = Duration::from_secs(20);

#[derive(Debug, Eq, PartialEq)]
struct Summary {
    projects: BTreeMap<String, (i64, String)>,
    total_hours: i64,
}

/// Application logic depends on one immutable value, not a connection or storage backend.
fn calculation(database: &DatabaseValue) -> Result<Summary> {
    let entity = Variable::from("project");
    let query = Query::new(
        FindSpec::Collection(FindElement::Variable(entity.clone())),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            atomic_core::Term::Variable(entity),
            atomic_core::Term::Constant(Value::Ref(u64::from(PROJECT))),
            atomic_core::Term::Blank,
        )))],
    );
    let control = QueryControl {
        timeout: Some(WAIT),
        max_work: 10_000,
        max_result_rows: 100,
        max_intermediate_rows: 100,
        ..QueryControl::default()
    };
    let QueryResult::Collection(ids) = database.query(&query, &[], &control)?.result else {
        return Err("application query returned an unexpected shape".into());
    };
    let mut projects = BTreeMap::new();
    let mut total_hours = 0;
    for id in ids {
        let QueryValue::Scalar(Value::Ref(id)) = id else {
            return Err("application query returned a non-entity".into());
        };
        let project = database.entity(id)?.ok_or("project was not found")?;
        let key = string(&project, PROJECT)?;
        let Some(EntityValue::Scalar(Value::Long(hours))) = project.get(HOURS)? else {
            return Err("project has no hours".into());
        };
        let Some(EntityValue::Entity(owner)) = project.get(OWNER)? else {
            return Err("project has no owner".into());
        };
        projects.insert(key, (hours, string(&owner, NAME)?));
        total_hours += hours;
    }
    Ok(Summary {
        projects,
        total_hours,
    })
}

fn string(entity: &Entity, attribute: u32) -> Result<String> {
    match entity.get(attribute)? {
        Some(EntityValue::Scalar(Value::String(value))) => Ok(value),
        _ => Err("application entity has no expected string".into()),
    }
}

fn schema() -> Result<Schema> {
    let mut schema = Schema::new();
    for attribute in [
        Attribute::new(
            PROJECT,
            Keyword::new("project", "key"),
            ValueType::String,
            Cardinality::One,
        )
        .unique(Unique::Identity),
        Attribute::new(
            HOURS,
            Keyword::new("project", "hours"),
            ValueType::Long,
            Cardinality::One,
        ),
        Attribute::new(
            OWNER,
            Keyword::new("project", "owner"),
            ValueType::Ref,
            Cardinality::One,
        ),
        Attribute::new(
            NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ),
    ] {
        schema.install(attribute)?;
    }
    Ok(schema)
}

fn add(entity: &str, attribute: u32, value: TxValue) -> TxOp {
    TxOp::Add {
        entity: EntityRef::Temp(entity.into()),
        attribute,
        value,
    }
}

fn initial_data() -> Vec<TxOp> {
    let mut operations = vec![add("owner", NAME, Value::String("Ada".into()).into())];
    for (key, hours) in [("alpha", 3), ("beta", 5)] {
        operations.extend([
            add(key, PROJECT, Value::String(key.into()).into()),
            add(key, HOURS, Value::Long(hours).into()),
            add(key, OWNER, TxValue::Entity(EntityRef::Temp("owner".into()))),
        ]);
    }
    operations
}

fn update() -> Vec<TxOp> {
    vec![TxOp::Add {
        entity: EntityRef::Lookup {
            attribute: PROJECT,
            value: Value::String("alpha".into()),
        },
        attribute: HOURS,
        value: Value::Long(7).into(),
    }]
}

fn require(condition: bool, message: &'static str) -> Result<()> {
    if condition {
        Ok(())
    } else {
        Err(message.into())
    }
}

fn run() -> Result<()> {
    let mut database_id = None;
    let mut endpoint = None;
    let mut arguments = std::env::args().skip(1);
    while let Some(argument) = arguments.next() {
        match argument.as_str() {
            "--database" if database_id.is_none() => {
                database_id = Some(arguments.next().ok_or("missing database")?)
            }
            "--endpoint" if endpoint.is_none() => {
                endpoint = Some(PathBuf::from(arguments.next().ok_or("missing endpoint")?))
            }
            _ => return Err("usage: application_workflow --database ID --endpoint PATH".into()),
        }
    }
    let database_id = database_id.ok_or("--database is required")?;
    let endpoint = endpoint.ok_or("--endpoint is required")?;
    let started = Instant::now();
    let config = postgres_config_from_env()?;
    let connection = Connection::connect_configured(config.clone(), &database_id, 8)?;

    // Database provisioning belongs to the operator. These are ordinary application facts.
    let schema = schema()?;
    let schema_request = TransactionRequest::new(
        "application-workflow/schema/v1",
        schema
            .attributes()
            .cloned()
            .map(TxOp::InstallAttribute)
            .collect(),
    );
    connection
        .transact_socket(&endpoint, schema_request, WAIT)?
        .report?;
    let request = TransactionRequest::new("application-workflow/seed/v1", initial_data());
    let seeded = connection.transact_socket(&endpoint, request.clone(), WAIT)?;
    let seed_replayed = seeded.replayed;
    let seeded = seeded.report?;
    let replay = connection.transact_socket(&endpoint, request, WAIT)?;
    require(
        replay.replayed && replay.tx_hash == seeded.tx_hash,
        "retry changed the receipt",
    )?;
    require(
        replay.report?.tempids == seeded.tempids,
        "retry changed allocated identities",
    )?;

    // Capture the exact seed value from its receipt, including on a later replay after restart.
    let captured = seeded.db_after;
    let old = calculation(&captured)?;
    let fixture = Database::new(schema)?
        .with(&initial_data(), 1_000)?
        .db_after;
    require(
        calculation(&fixture.database_value())? == old,
        "native/fixture calculation mismatch",
    )?;
    require(
        old.total_hours == 8 && old.projects.len() == 2,
        "unexpected initial calculation",
    )?;

    let updated = connection.transact_socket(
        &endpoint,
        TransactionRequest::new("application-workflow/update/v1", update()),
        WAIT,
    )?;
    let update_replayed = updated.replayed;
    let updated = updated.report?;
    let current = connection.sync_to(updated.basis_t, WAIT)?;
    let expected = fixture.with(&update(), 2_000)?.db_after.database_value();
    require(
        calculation(&current)? == calculation(&expected)?,
        "updated calculation mismatch",
    )?;
    require(
        calculation(&captured)? == old,
        "retained value changed after commit",
    )?;
    require(
        calculation(&current.clone().as_of(captured.basis_t()))? == old,
        "as-of calculation mismatch",
    )?;

    let events = current
        .clone()
        .history()
        .collect_datoms_with_prefix(&IndexPrefix::Eavt {
            entity: seeded.tempids["alpha"],
            attribute: Some(HOURS),
            value: None,
        })?;
    require(
        events.len() == 3 && events.iter().filter(|event| event.added).count() == 2,
        "history did not retain the assertion and retraction",
    )?;
    let reopened = Connection::connect_configured(config, &database_id, 8)?;
    require(
        reopened.identity() == connection.identity(),
        "reopen changed database lineage",
    )?;
    require(
        calculation(&reopened.db())? == calculation(&current)?,
        "reopen changed application facts",
    )?;

    let loop_started = Instant::now();
    for _ in 0..20 {
        require(
            calculation(&captured)? == old,
            "repeated calculation changed",
        )?;
    }
    let load = connection.load_stats();
    require(
        load.compatibility_materializations == 0
            && reopened.load_stats().compatibility_materializations == 0,
        "ordinary application materialized an eager database",
    )?;
    println!(
        "APPLICATION_OK basis_t={} old_basis_t={} seed_replayed={} update_replayed={} projects=2 old_hours=8 current_hours=12 history_events=3",
        current.basis_t(),
        captured.basis_t(),
        seed_replayed,
        update_replayed
    );
    println!(
        "BASELINE elapsed_ms={} repeated_calculations=20 calculation_loop_us={} rss_kib={:?} connection_cursor_sql_reads={} connection_cursor_sql_bytes={} cache={:?}",
        started.elapsed().as_millis(),
        loop_started.elapsed().as_micros(),
        rss_kib(),
        load.cursor_sql_reads,
        load.cursor_sql_read_bytes,
        connection.cache_stats()
    );
    Ok(())
}

fn rss_kib() -> Option<u64> {
    std::fs::read_to_string("/proc/self/status")
        .ok()?
        .lines()
        .find(|line| line.starts_with("VmRSS:"))?
        .split_whitespace()
        .nth(1)?
        .parse()
        .ok()
}

fn main() {
    if let Err(error) = run() {
        // A semantic error may contain subject data or credentials in its details.
        if let Some(error) = error.downcast_ref::<SemanticError>() {
            eprintln!(
                "application failed: category={:?} code={}",
                error.category, error.code
            );
        } else {
            eprintln!(
                "application failed; check arguments, endpoint and documented fixture contract"
            );
        }
        std::process::exit(1);
    }
}
