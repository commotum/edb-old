//! Text data commands use the ordinary peer and fenced transactor interfaces.
use atomic_core::edn::{EdnValue, read_edn, write_edn};
use atomic_core::edn_pull::parse_pull_edn;
use atomic_core::edn_query::{EdnQueryArgument, EdnQueryInput, parse_query_edn};
use atomic_core::edn_value::{edn_keyword, edn_to_value, query_value_to_edn, value_to_edn};
use atomic_core::{
    AttributeName, Connection, DatabaseValue, Datom, EntityIdentifier, ErrorCategory,
    PostgresConnectionConfig, PullControl, QueryControl, QuerySourceValue, SemanticError,
    TransactionRequest, Value, postgres_config_from_env, remote_client_config_from_env,
};
use std::collections::{BTreeMap, BTreeSet};
use std::io::{Read, Write};
use std::sync::Arc;
use std::time::{Duration, SystemTime, UNIX_EPOCH};

const MAX_INPUT_BYTES: usize = 16 * 1024 * 1024;
pub const HELP: &str = "
EDN data commands (file path '-' reads stdin; results are EDN on stdout):
  atomic transact --database ID --file PATH --request-key KEY
    (--endpoint PATH | --remote) [--basis N] [--tx-instant MILLIS] [--timeout-ms N]
  atomic with --database ID --file PATH [--tx-instant MILLIS]
  atomic query --database ID --file PATH [--inputs PATH] [--sources PATH]
    [--as-of N] [--since N] [--history] [--timeout-ms N] [--max-work N]
    [--max-results N] [--max-join-bytes N]
  atomic pull --database ID --file PATH --entity EDN
    [--as-of N] [--since N] [--max-depth N] [--max-entities N]

transact requires a stable caller-supplied request key; reuse it AND the original
intent/options after an unknown outcome. --basis is an optional optimistic guard.
with is a local preview and never commits. Its temporary IDs are not reserved.
query --inputs is a vector of non-source :in arguments (including rules/patterns).
--sources is a map from source symbols to tuple rows or descriptors such as
{$past {:database \"customers\" :as-of 42} $log {:database \"customers\" :log true}}.
The default $ source is the selected --database value. Reads never start a writer.
The same PostgreSQL/TLS credential configuration applies. --remote uses verified
TLS writer discovery and ATOMIC_REMOTE_TOKEN_FILE, not plaintext TCP.
Input files are limited to16MiB each. Output conversion/printing is bounded;
query defaults to a30s evaluation timeout. See docs/edn.md for format/type limits.
";

struct Arguments {
    command: String,
    values: BTreeMap<String, String>,
    switches: BTreeSet<String>,
}
pub fn dispatch(raw: &[String]) -> Option<Result<(), SemanticError>> {
    let command = raw.first()?.as_str();
    if !matches!(command, "transact" | "with" | "query" | "pull") {
        return None;
    }
    Some(parse(command, &raw[1..]).and_then(run))
}
fn usage(message: &str) -> SemanticError {
    SemanticError::incorrect("cli/edn-usage", message)
}
fn parse(command: &str, raw: &[String]) -> Result<Arguments, SemanticError> {
    let (flags, switches): (&[&str], &[&str]) = match command {
        "transact" => (
            &[
                "--database",
                "--file",
                "--request-key",
                "--endpoint",
                "--basis",
                "--tx-instant",
                "--timeout-ms",
            ],
            &["--remote"],
        ),
        "with" => (&["--database", "--file", "--tx-instant"], &[]),
        "query" => (
            &[
                "--database",
                "--file",
                "--inputs",
                "--sources",
                "--as-of",
                "--since",
                "--timeout-ms",
                "--max-work",
                "--max-results",
                "--max-join-bytes",
            ],
            &["--history"],
        ),
        "pull" => (
            &[
                "--database",
                "--file",
                "--entity",
                "--as-of",
                "--since",
                "--max-depth",
                "--max-entities",
            ],
            &[],
        ),
        _ => unreachable!(),
    };
    let mut args = Arguments {
        command: command.into(),
        values: BTreeMap::new(),
        switches: BTreeSet::new(),
    };
    let mut rest = raw.iter();
    while let Some(flag) = rest.next() {
        if switches.contains(&flag.as_str()) {
            if !args.switches.insert(flag.clone()) {
                return Err(usage("duplicate option"));
            }
        } else if flags.contains(&flag.as_str()) {
            let value = rest
                .next()
                .filter(|v| !v.is_empty() && !v.starts_with("--"))
                .ok_or_else(|| usage("option requires a value"))?;
            if args.values.insert(flag.clone(), value.clone()).is_some() {
                return Err(usage("duplicate option"));
            }
        } else {
            return Err(usage("unknown or misplaced option; run atomic --help"));
        }
    }
    args.required("--database")?;
    args.required("--file")?;
    if command == "transact" {
        args.required("--request-key")?;
        if args.values.contains_key("--endpoint") == args.switches.contains("--remote") {
            return Err(usage(
                "transact requires exactly one of --endpoint or --remote",
            ));
        }
    }
    if command == "pull" {
        args.required("--entity")?;
    }
    let stdin_count = ["--file", "--inputs", "--sources"]
        .iter()
        .filter(|flag| args.values.get(**flag).is_some_and(|v| v == "-"))
        .count();
    if stdin_count > 1 {
        return Err(usage("only one input may read stdin"));
    }
    for flag in [
        "--basis",
        "--as-of",
        "--since",
        "--timeout-ms",
        "--max-work",
        "--max-results",
        "--max-join-bytes",
        "--max-depth",
        "--max-entities",
    ] {
        if args.values.contains_key(flag) {
            args.number(flag, 0)?;
        }
    }
    if args
        .values
        .get("--tx-instant")
        .is_some_and(|v| v.parse::<i64>().is_err())
    {
        return Err(usage("--tx-instant requires signed 64-bit milliseconds"));
    }
    if args.values.contains_key("--timeout-ms") && args.number("--timeout-ms", 1)? == 0 {
        return Err(usage("timeout must be positive"));
    }
    Ok(args)
}
impl Arguments {
    fn required(&self, flag: &str) -> Result<&str, SemanticError> {
        self.values
            .get(flag)
            .map(String::as_str)
            .ok_or_else(|| usage("required data command option is missing; run atomic --help"))
    }
    fn number(&self, flag: &str, default: u64) -> Result<u64, SemanticError> {
        self.values
            .get(flag)
            .map(|v| {
                v.parse()
                    .map_err(|_| usage("numeric option requires a nonnegative 64-bit integer"))
            })
            .unwrap_or(Ok(default))
    }
    fn size(&self, flag: &str, default: usize) -> Result<usize, SemanticError> {
        usize::try_from(self.number(flag, default as u64)?)
            .map_err(|_| usage("numeric option exceeds this platform's supported size"))
    }
    fn preview_instant(&self) -> Result<i64, SemanticError> {
        match self.values.get("--tx-instant") {
            Some(value) => value.parse().map_err(|_| usage("invalid preview instant")),
            None => SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .ok()
                .and_then(|v| i64::try_from(v.as_millis()).ok())
                .ok_or_else(|| usage("preview clock is outside the supported range")),
        }
    }
}
fn read_file(path: &str) -> Result<String, SemanticError> {
    let mut input: Box<dyn Read> = if path == "-" {
        Box::new(std::io::stdin())
    } else {
        Box::new(std::fs::File::open(path).map_err(|_| usage("could not open EDN input file"))?)
    };
    let mut bytes = Vec::new();
    input
        .by_ref()
        .take(MAX_INPUT_BYTES as u64 + 1)
        .read_to_end(&mut bytes)
        .map_err(|_| usage("could not read EDN input"))?;
    if bytes.len() > MAX_INPUT_BYTES {
        return Err(usage("EDN input exceeds16MiB"));
    }
    String::from_utf8(bytes).map_err(|_| usage("EDN input must be UTF-8"))
}
fn output(value: &EdnValue) -> Result<(), SemanticError> {
    let text = write_edn(value)?;
    let mut stdout = std::io::stdout().lock();
    stdout
        .write_all(text.as_bytes())
        .and_then(|_| stdout.write_all(b"\n"))
        .and_then(|_| stdout.flush())
        .map_err(|_| {
            SemanticError::new(
                ErrorCategory::Unavailable,
                "cli/edn-output",
                "could not write EDN result; a failed observation does not imply rollback",
            )
        })
}
fn run(args: Arguments) -> Result<(), SemanticError> {
    let text = read_file(args.required("--file")?)?;
    // Reject lexical errors before opening any database or attempting a write.
    read_edn(&text)?;
    let config = postgres_config_from_env()?;
    let connection =
        Connection::connect_configured(config.clone(), args.required("--database")?, 128)?;
    match args.command.as_str() {
        "transact" => {
            let mut request = TransactionRequest::from_edn(args.required("--request-key")?, &text)?;
            if args.values.contains_key("--basis") {
                request = request.comparing_basis(args.number("--basis", 0)?);
            }
            if args.values.contains_key("--tx-instant") {
                request = request.with_tx_instant(args.preview_instant()?);
            }
            let timeout = Duration::from_millis(args.number("--timeout-ms", 30_000)?);
            let commit = if args.switches.contains("--remote") {
                let endpoint = connection.discover_remote_writer(&config)?;
                connection.transact_remote(
                    &endpoint,
                    &remote_client_config_from_env()?,
                    request,
                    timeout,
                )?
            } else {
                connection.transact_socket(args.required("--endpoint")?, request, timeout)?
            };
            // A confirmed commit remains confirmed even if opening its local
            // report or encoding its payload fails. Always retain a minimal receipt.
            let mut fields = vec![
                (key("committed"), EdnValue::Bool(true)),
                (key("basis-t"), integer(commit.basis_t)),
                (key("replayed"), EdnValue::Bool(commit.replayed)),
                (
                    key("tx-hash"),
                    EdnValue::String(commit.tx_hash.iter().map(|b| format!("{b:02x}")).collect()),
                ),
            ];
            match commit.report {
                Ok(report) => match report_fields(
                    report.db_before.basis_t(),
                    report.db_after.basis_t(),
                    &report.tx_data,
                    &report.tempids,
                ) {
                    Ok(report) => fields.extend(report),
                    Err(error) => {
                        fields.push((key("report-error"), EdnValue::String(error.code.into())))
                    }
                },
                Err(error) => {
                    fields.push((key("report-error"), EdnValue::String(error.code.into())))
                }
            }
            if let Err(error) = output(&EdnValue::Map(fields)) {
                if error.code == "cli/edn-output" {
                    return Err(error);
                }
                // The short receipt may still be printable if only the full
                // report exceeded output admission. Never describe this as rejection.
                output(&EdnValue::Map(vec![
                    (key("committed"), EdnValue::Bool(true)),
                    (key("basis-t"), integer(commit.basis_t)),
                    (key("replayed"), EdnValue::Bool(commit.replayed)),
                    (key("report-error"), EdnValue::String(error.code.into())),
                ]))?;
            }
        }
        "with" => {
            let db = connection.db();
            let report = db.with_edn(&text, args.preview_instant()?)?;
            let mut fields = report_fields(
                report.db_before.basis_t(),
                report.db_after.basis_t(),
                &report.tx_data,
                &report.tempids,
            )?;
            fields.push((key("committed"), EdnValue::Bool(false)));
            fields.push((key("speculative"), EdnValue::Bool(true)));
            output(&EdnValue::Map(fields))?;
        }
        "query" => {
            let query = parse_query_edn(&text)?;
            let db = view(connection.db(), &args)?;
            let inputs = args
                .values
                .get("--inputs")
                .map(|path| read_file(path).and_then(|s| read_edn(&s)))
                .transpose()?
                .unwrap_or_else(|| EdnValue::Vector(Vec::new()));
            let EdnValue::Vector(input_values) = &inputs else {
                return Err(usage(
                    "--inputs must contain a vector of non-source arguments",
                ));
            };
            let mut values = input_values.iter();
            let sources = args
                .values
                .get("--sources")
                .map(|path| read_file(path).and_then(|s| read_edn(&s)))
                .transpose()?
                .unwrap_or_else(|| EdnValue::Map(Vec::new()));
            let EdnValue::Map(source_values) = &sources else {
                return Err(usage("--sources must contain a map"));
            };
            let mut supplied = BTreeSet::new();
            let mut arguments = Vec::new();
            for input in &query.inputs {
                match input {
                    EdnQueryInput::Source(name) => {
                        let descriptor = source_values
                            .iter()
                            .find(|(key, _)| source_name(key).as_deref() == Some(name));
                        let source = if let Some((_, descriptor)) = descriptor {
                            supplied.insert(name.clone());
                            source_value(descriptor, &config, &connection)?
                        } else if name == "$" {
                            QuerySourceValue::Database(db.clone())
                        } else {
                            return Err(usage("a named query source has no --sources descriptor"));
                        };
                        arguments.push(EdnQueryArgument::Source(source));
                    }
                    _ => arguments.push(EdnQueryArgument::Data(
                        values
                            .next()
                            .ok_or_else(|| usage("not enough non-source query inputs"))?
                            .clone(),
                    )),
                }
            }
            if values.next().is_some() {
                return Err(usage("too many non-source query inputs"));
            }
            if supplied.len() != source_values.len() {
                return Err(usage("unused or invalid query source descriptor"));
            }
            let bound = query.bind(&arguments)?;
            let control = QueryControl {
                timeout: Some(Duration::from_millis(args.number("--timeout-ms", 30_000)?)),
                max_work: args.size("--max-work", usize::MAX)?,
                max_result_rows: args.size("--max-results", usize::MAX)?,
                max_join_bytes: args.size("--max-join-bytes", 4 * 1024 * 1024)?,
                ..QueryControl::default()
            };
            let result = bound.execute(&control, None)?;
            output(&bound.result_to_edn(&result.result)?)?;
        }
        "pull" => {
            let pattern = parse_pull_edn(&text)?;
            let identifier = entity_identifier(&read_edn(args.required("--entity")?)?)?;
            let db = view(connection.db(), &args)?;
            let control = PullControl {
                max_depth: args.size("--max-depth", usize::MAX)?,
                max_entities: args.size("--max-entities", usize::MAX)?,
                ..PullControl::default()
            };
            output(&query_value_to_edn(
                &db.pull_with_control(&pattern, identifier, &control)?,
            )?)?;
        }
        _ => unreachable!(),
    }
    Ok(())
}
fn key(name: &str) -> EdnValue {
    edn_keyword("atomic", name)
}
fn integer(value: u64) -> EdnValue {
    i64::try_from(value)
        .map(EdnValue::Long)
        .unwrap_or_else(|_| EdnValue::BigInt(value.into()))
}
fn report_fields(
    before: u64,
    after: u64,
    datoms: &[Datom],
    tempids: &BTreeMap<String, u64>,
) -> Result<Vec<(EdnValue, EdnValue)>, SemanticError> {
    // Bound containers before cloning/encoding their contents; the value writer
    // subsequently enforces total output bytes and nested values.
    if datoms.len().saturating_add(tempids.len()) > 100_000 {
        return Err(usage("transaction report exceeds text output item limit"));
    }
    let data = datoms
        .iter()
        .map(|d| {
            Ok(EdnValue::Vector(vec![
                integer(d.entity),
                integer(u64::from(d.attribute)),
                value_to_edn(&d.value)?,
                integer(d.tx),
                EdnValue::Bool(d.added),
            ]))
        })
        .collect::<Result<_, SemanticError>>()?;
    Ok(vec![
        (key("db-before-t"), integer(before)),
        (key("db-after-t"), integer(after)),
        (key("tx-data"), EdnValue::Vector(data)),
        (
            key("tempids"),
            EdnValue::Map(
                tempids
                    .iter()
                    .map(|(k, v)| (EdnValue::String(k.clone()), integer(*v)))
                    .collect(),
            ),
        ),
    ])
}
fn view(mut db: DatabaseValue, args: &Arguments) -> Result<DatabaseValue, SemanticError> {
    if args.values.contains_key("--as-of") {
        db = db.as_of(args.number("--as-of", 0)?);
    }
    if args.values.contains_key("--since") {
        db = db.since(args.number("--since", 0)?);
    }
    if args.switches.contains("--history") {
        db = db.history();
    }
    Ok(db)
}
fn entity_identifier(value: &EdnValue) -> Result<EntityIdentifier, SemanticError> {
    Ok(match value {
        EdnValue::Long(id) if *id >= 0 => EntityIdentifier::Id(*id as u64),
        EdnValue::Keyword(name) => EntityIdentifier::Ident(name.clone()),
        EdnValue::Vector(values) | EdnValue::List(values) if values.len() == 2 => {
            let attribute = match &values[0] {
                EdnValue::Keyword(name) => AttributeName::Ident(name.clone()),
                EdnValue::Long(id) => AttributeName::Id(
                    u32::try_from(*id)
                        .map_err(|_| usage("lookup attribute ID is outside range"))?,
                ),
                _ => return Err(usage("lookup attribute must be an ident or attribute ID")),
            };
            EntityIdentifier::Lookup {
                attribute,
                value: edn_to_value(&values[1])?,
            }
        }
        _ => match edn_to_value(value)? {
            Value::Ref(id) => EntityIdentifier::Id(id),
            _ => return Err(usage("entity must be an ID, ident or lookup reference")),
        },
    })
}
fn source_name(value: &EdnValue) -> Option<String> {
    match value {
        EdnValue::Symbol(s) => Some(s.qualified_name()),
        EdnValue::String(s) => Some(s.clone()),
        _ => None,
    }
}
fn source_value(
    value: &EdnValue,
    config: &PostgresConnectionConfig,
    primary: &Connection,
) -> Result<QuerySourceValue, SemanticError> {
    if let EdnValue::Vector(rows) | EdnValue::List(rows) = value {
        let rows = rows
            .iter()
            .map(|row| {
                let (EdnValue::Vector(values) | EdnValue::List(values)) = row else {
                    return Err(usage("raw source requires rows of values"));
                };
                values
                    .iter()
                    .map(edn_to_value)
                    .collect::<Result<Vec<_>, _>>()
            })
            .collect::<Result<Vec<_>, _>>()?;
        return Ok(QuerySourceValue::Tuples(Arc::new(rows)));
    }
    let EdnValue::Map(fields) = value else {
        return Err(usage("source must be rows or a database descriptor"));
    };
    let mut options = BTreeMap::new();
    for (key, value) in fields {
        let EdnValue::Keyword(key) = key else {
            return Err(usage("source descriptor keys must be keywords"));
        };
        if key.namespace.is_some()
            || !matches!(
                key.name.as_str(),
                "database" | "as-of" | "since" | "history" | "log"
            )
        {
            return Err(usage("unknown source descriptor field"));
        }
        options.insert(key.name.as_str(), value);
    }
    let opened;
    let connection = match options.get("database") {
        Some(EdnValue::String(id)) if id != primary.identity().database_id() => {
            opened = Connection::connect_configured(config.clone(), id.as_str(), 128)?;
            &opened
        }
        Some(EdnValue::String(_)) | None => primary,
        _ => return Err(usage("database source name must be a string")),
    };
    for name in ["history", "log"] {
        if options
            .get(name)
            .is_some_and(|value| !matches!(value, EdnValue::Bool(_)))
        {
            return Err(usage("history and log source flags must be booleans"));
        }
    }
    if matches!(options.get("log"), Some(EdnValue::Bool(true))) {
        if ["as-of", "since", "history"]
            .iter()
            .any(|key| options.contains_key(key))
        {
            return Err(usage("log source does not accept database-view options"));
        }
        return Ok(QuerySourceValue::Log(connection.log()));
    }
    let mut db = connection.db();
    for name in ["as-of", "since"] {
        if let Some(value) = options.get(name) {
            let EdnValue::Long(t) = value else {
                return Err(usage("source basis must be a nonnegative integer"));
            };
            let t = u64::try_from(*t).map_err(|_| usage("source basis must be nonnegative"))?;
            db = if name == "as-of" {
                db.as_of(t)
            } else {
                db.since(t)
            };
        }
    }
    if matches!(options.get("history"), Some(EdnValue::Bool(true))) {
        db = db.history();
    }
    Ok(QuerySourceValue::Database(db))
}
