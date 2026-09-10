//! Supported local runtime and explicit administrative entry points.
use atomic_core::{
    BackgroundIndexingConfig, CapacityLimits, ErrorCategory, LocalTransactionEndpoint,
    LocalTransportConfig, PostgresIndexer, PostgresMigrator, PostgresStore, Schema, SemanticError,
    TransactionService, TransactionServiceConfig, postgres_config_from_env,
};
use std::collections::BTreeMap;
use std::io::Write;
use std::path::Path;
use std::process::ExitCode;
use std::sync::atomic::{AtomicBool, Ordering};
use std::time::Duration;

static STOP: AtomicBool = AtomicBool::new(false);
extern "C" fn stop_signal(_: libc::c_int) {
    // A lock-free atomic store is the only work performed in a signal handler.
    STOP.store(true, Ordering::Relaxed);
}

const HELP: &str = "Atomic — native Rust/PostgreSQL database

Usage:
  atomic migrate [--writer-role ROLE --peer-role ROLE]
  atomic create --database ID
  atomic status --database ID
  atomic consolidate --database ID
  atomic transactor --database ID --endpoint PATH [OPTIONS]

Required environment (all commands except help/version):
  ATOMIC_POSTGRES_URL          PostgreSQL connection string; never printed
  ATOMIC_POSTGRES_TRANSPORT    tls (verified) or plaintext (explicit development)
Optional: ATOMIC_POSTGRES_TLS_ROOT (PEM); ATOMIC_CONNECT_TIMEOUT_MS,
  ATOMIC_STATEMENT_TIMEOUT_MS, ATOMIC_LOCK_TIMEOUT_MS, ATOMIC_TCP_USER_TIMEOUT_MS,
  ATOMIC_KEEPALIVES=true|false, ATOMIC_KEEPALIVES_IDLE_SECONDS,
  ATOMIC_KEEPALIVES_INTERVAL_SECONDS, ATOMIC_KEEPALIVES_RETRIES.

Transactor options (positive integers):
  --holder ID                  lease holder label (default: process-specific)
  --lease-ms N                 default 5000
  --renew-ms N                 default 1000; must be less than lease
  --queue-capacity N           default 64
  --max-in-flight N            default 4
  --request-timeout-ms N       default 30000 (not a commit/rollback guarantee)
  --max-frame-bytes N          default library frame limit
  --index-threshold-bytes N    default library indexing threshold
  --index-max-bytes N          default library recent-memory maximum
  --tree-cache-entries N       default library writer cache entries
  --tree-cache-bytes N         default library writer cache bytes

Use a pre-existing private owner-only endpoint directory (mkdir -m 700).
SIGINT/SIGTERM stop admission, drain bounded local requests, then stop the writer.
Runtime never migrates or creates databases. Run migrate/create separately with
administrative credentials. Roles passed to migrate must already exist and be
dedicated restricted roles. consolidate is explicit missing-index recovery or
maintenance, not automatic startup. status is catalog state, not deep integrity
verification or proof of a live transactor. See docs/application.md.
";

struct Arguments {
    command: String,
    options: BTreeMap<String, String>,
}

impl Arguments {
    fn parse(args: impl IntoIterator<Item = String>) -> Result<Self, SemanticError> {
        let mut args = args.into_iter();
        let command = args.next().unwrap_or_else(|| "--help".into());
        let allowed: &[&str] = match command.as_str() {
            "--help" | "help" | "--version" => &[],
            "migrate" => &["--writer-role", "--peer-role"],
            "create" | "status" | "consolidate" => &["--database"],
            "transactor" => &[
                "--database",
                "--endpoint",
                "--holder",
                "--lease-ms",
                "--renew-ms",
                "--queue-capacity",
                "--max-in-flight",
                "--request-timeout-ms",
                "--max-frame-bytes",
                "--index-threshold-bytes",
                "--index-max-bytes",
                "--tree-cache-entries",
                "--tree-cache-bytes",
            ],
            _ => return Err(usage("unknown command; run atomic --help")),
        };
        let mut options = BTreeMap::new();
        while let Some(flag) = args.next() {
            if !allowed.contains(&flag.as_str()) {
                return Err(usage("unknown or misplaced option; run atomic --help"));
            }
            let value = args
                .next()
                .filter(|s| !s.is_empty() && !s.starts_with("--"))
                .ok_or_else(|| usage("option requires a nonempty value"))?;
            if options.insert(flag, value).is_some() {
                return Err(usage("duplicate option"));
            }
        }
        let parsed = Self { command, options };
        match parsed.command.as_str() {
            "create" | "status" | "consolidate" | "transactor" => {
                parsed.required("--database")?;
            }
            "migrate" => {
                if parsed.options.contains_key("--writer-role")
                    != parsed.options.contains_key("--peer-role")
                {
                    return Err(usage(
                        "--writer-role and --peer-role must be supplied together",
                    ));
                }
                if parsed.options.get("--writer-role").is_some()
                    && parsed.options.get("--writer-role") == parsed.options.get("--peer-role")
                {
                    return Err(usage("writer and peer roles must be distinct"));
                }
            }
            _ => {}
        }
        if parsed.command == "transactor" {
            parsed.required("--endpoint")?;
            // Validate every numeric setting before touching the database.
            for flag in parsed
                .options
                .keys()
                .filter(|key| !matches!(key.as_str(), "--database" | "--endpoint" | "--holder"))
            {
                parsed.number(flag, 1)?;
            }
        }
        Ok(parsed)
    }

    fn required(&self, flag: &str) -> Result<&str, SemanticError> {
        self.options
            .get(flag)
            .map(String::as_str)
            .ok_or_else(|| usage(format!("required option {flag} is missing")))
    }

    fn number(&self, flag: &str, default: u64) -> Result<u64, SemanticError> {
        self.options
            .get(flag)
            .map(|value| {
                value
                    .parse::<u64>()
                    .ok()
                    .filter(|n| *n > 0)
                    .ok_or_else(|| usage(format!("{flag} requires a positive integer")))
            })
            .unwrap_or(Ok(default))
    }

    fn size(&self, flag: &str, default: usize) -> Result<usize, SemanticError> {
        usize::try_from(self.number(flag, default as u64)?)
            .map_err(|_| usage(format!("{flag} exceeds this platform's supported size")))
    }
}

fn usage(message: impl Into<String>) -> SemanticError {
    SemanticError::incorrect("cli/usage", message)
}

fn io_error() -> SemanticError {
    SemanticError::new(ErrorCategory::Unavailable, "cli/io", "process I/O failed")
}

fn run(args: Arguments) -> Result<(), SemanticError> {
    match args.command.as_str() {
        "--help" | "help" => {
            print!("{HELP}");
            return Ok(());
        }
        "--version" => {
            println!("atomic {}", env!("CARGO_PKG_VERSION"));
            return Ok(());
        }
        _ => {}
    }
    let connection = postgres_config_from_env()?;
    match args.command.as_str() {
        "migrate" => {
            let mut migrator = PostgresMigrator::connect_configured(&connection)?;
            migrator.migrate()?;
            // Migration and grants are separate administrative steps. Preserve
            // the first known result if subsequent role validation fails.
            println!(
                "MIGRATED schema_version={}",
                atomic_core::POSTGRES_SCHEMA_VERSION
            );
            std::io::stdout().flush().map_err(|_| io_error())?;
            if let Some(writer) = args.options.get("--writer-role") {
                migrator.grant_runtime_privileges(writer, args.required("--peer-role")?)?;
                println!("GRANTED runtime_roles=true");
            }
        }
        "create" => {
            let id = args.required("--database")?;
            let db = PostgresStore::connect_configured(&connection)?
                .create_database(id, Schema::new())?;
            println!("CREATED database={id:?} basis_t={}", db.basis_t());
        }
        "status" => {
            // Check the existing schema using the same non-migrating runtime path.
            drop(PostgresStore::connect_configured(&connection)?);
            let mut client = connection.connect()?;
            let row = client
                .query_opt(
                    "SELECT d.lineage_id, h.basis_t, h.log_generation FROM atomic_databases d \
                 JOIN atomic_heads h USING (database_id) WHERE d.database_id = $1",
                    &[&args.required("--database")?],
                )
                .map_err(|_| {
                    SemanticError::new(
                        ErrorCategory::Unavailable,
                        "cli/status-read",
                        "catalog status unavailable",
                    )
                })?
                .ok_or_else(|| {
                    SemanticError::new(
                        ErrorCategory::NotFound,
                        "cli/database-not-found",
                        "database not found",
                    )
                })?;
            println!(
                "STATUS database={:?} lineage={} basis_t={} generation={}",
                args.required("--database")?,
                row.get::<_, String>(0),
                row.get::<_, i64>(1),
                row.get::<_, i64>(2)
            );
        }
        "consolidate" => {
            let receipt =
                PostgresIndexer::connect_configured(&connection, args.required("--database")?)?
                    .consolidate()?;
            println!(
                "INDEXED basis_t={} input_datoms={}",
                receipt.basis_t, receipt.input_datoms
            );
        }
        "transactor" => {
            let mut capacity = CapacityLimits::default();
            capacity.writer_tree_cache_entries =
                args.size("--tree-cache-entries", capacity.writer_tree_cache_entries)?;
            capacity.writer_tree_cache_bytes =
                args.size("--tree-cache-bytes", capacity.writer_tree_cache_bytes)?;
            let config = TransactionServiceConfig {
                // The configured constructor ignores this compatibility field.
                connection: String::new(),
                database_id: args.required("--database")?.to_owned(),
                holder_id: args
                    .options
                    .get("--holder")
                    .cloned()
                    .unwrap_or_else(|| format!("atomic-{}", std::process::id())),
                lease_duration: Duration::from_millis(args.number("--lease-ms", 5_000)?),
                renew_interval: Duration::from_millis(args.number("--renew-ms", 1_000)?),
                queue_capacity: args.size("--queue-capacity", 64)?,
                capacity_limits: capacity,
            };
            let default_index = BackgroundIndexingConfig::default();
            let index = BackgroundIndexingConfig {
                memory_index_threshold_bytes: args.number(
                    "--index-threshold-bytes",
                    default_index.memory_index_threshold_bytes,
                )?,
                memory_index_max_bytes: args
                    .number("--index-max-bytes", default_index.memory_index_max_bytes)?,
            };
            let default_transport = LocalTransportConfig::default();
            let transport = LocalTransportConfig {
                max_in_flight: args.size("--max-in-flight", default_transport.max_in_flight)?,
                request_timeout: Duration::from_millis(
                    args.number("--request-timeout-ms", 30_000)?,
                ),
                max_frame_bytes: args
                    .size("--max-frame-bytes", default_transport.max_frame_bytes)?,
            };
            transport.validate()?;
            let endpoint =
                LocalTransactionEndpoint::bind_at(Path::new(args.required("--endpoint")?))?;
            install_signals()?;
            let service =
                TransactionService::start_configured_with_indexing(config, connection, index)?;
            let server = match endpoint.start(service.client(), transport) {
                Ok(server) => server,
                Err(error) => {
                    service.shutdown();
                    return Err(error);
                }
            };
            println!(
                "READY database={:?} endpoint={:?} lineage={}",
                service.identity().database_id(),
                server.endpoint(),
                service.identity().lineage_id()
            );
            std::io::stdout().flush().map_err(|_| io_error())?;
            let mut lost_authority = false;
            while !STOP.load(Ordering::Relaxed) {
                if !service.client().is_available() || !server.is_available() {
                    lost_authority = true;
                    break;
                }
                std::thread::sleep(Duration::from_millis(25));
            }
            drop(server);
            service.shutdown();
            if lost_authority {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "cli/transactor-unavailable",
                    "transactor lost availability; supervisor restart required",
                ));
            }
            println!("STOPPED");
        }
        _ => unreachable!("validated command"),
    }
    Ok(())
}

fn install_signals() -> Result<(), SemanticError> {
    // POSIX handlers only set a lock-free atomic flag. The main thread owns all
    // cleanup, and handlers remain installed for the process lifetime.
    unsafe {
        let mut action: libc::sigaction = std::mem::zeroed();
        action.sa_sigaction = stop_signal as *const () as libc::sighandler_t;
        libc::sigemptyset(&mut action.sa_mask);
        action.sa_flags = libc::SA_RESTART;
        for signal in [libc::SIGINT, libc::SIGTERM] {
            if libc::sigaction(signal, &action, std::ptr::null_mut()) != 0 {
                return Err(io_error());
            }
        }
    }
    Ok(())
}

fn main() -> ExitCode {
    match Arguments::parse(std::env::args().skip(1)).and_then(run) {
        Ok(()) => ExitCode::SUCCESS,
        Err(error) => {
            // Never print arbitrary server errors, anomalies, transaction data,
            // connection strings or paths originating in failed input.
            eprintln!("ERROR category={:?} code={}", error.category, error.code);
            if error.code.starts_with("cli/") || error.code.starts_with("config/") {
                eprintln!("{}", error.message);
            }
            if error.code == "service/native-index-required" {
                eprintln!(
                    "Recover explicitly with atomic consolidate --database ID using authorized credentials."
                );
            }
            eprintln!(
                "See atomic --help and docs/application.md; no rollback is implied by a failed observation."
            );
            ExitCode::from(if error.category == ErrorCategory::Incorrect {
                2
            } else {
                1
            })
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    fn parse(args: &[&str]) -> Result<Arguments, SemanticError> {
        Arguments::parse(args.iter().map(|s| s.to_string()))
    }
    #[test]
    fn usage_is_strict_and_does_not_echo_values() {
        for args in [
            vec!["create"],
            vec!["create", "--database", "one", "--database", "two"],
            vec!["migrate", "--writer-role", "writer"],
            vec!["transactor", "--database", "d"],
            vec![
                "migrate",
                "--writer-role",
                "writer",
                "--peer-role",
                "writer",
            ],
            vec![
                "transactor",
                "--database",
                "d",
                "--endpoint",
                "/private",
                "--lease-ms",
                "secret",
            ],
            vec!["secret"],
            vec!["--help", "secret"],
        ] {
            let error = parse(&args).err().expect("invalid arguments accepted");
            assert!(!format!("{error:?}").contains("secret"));
        }
        assert_eq!(parse(&[]).unwrap().command, "--help");
        assert!(parse(&["create", "--database", "d"]).is_ok());
    }
}
