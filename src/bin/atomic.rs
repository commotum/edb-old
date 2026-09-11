//! Supported local runtime and explicit administrative entry points.
#[path = "atomic/admin.rs"]
mod admin;
#[path = "atomic/data.rs"]
mod data;
#[path = "atomic/health.rs"]
mod health;
#[path = "atomic/runtime.rs"]
mod runtime;
use atomic_core::{
    DatabaseCatalog, ErrorCategory, PostgresOperator, Schema, SemanticError, TransactionDefaults,
    postgres_config_from_env,
};
use std::collections::BTreeMap;
use std::io::Write;
use std::process::ExitCode;
use std::sync::atomic::{AtomicBool, Ordering};

static STOP: AtomicBool = AtomicBool::new(false);
extern "C" fn stop_signal(_: libc::c_int) {
    // A lock-free atomic store is the only work performed in a signal handler.
    STOP.store(true, Ordering::Relaxed);
}

const HELP: &str = "Atomic — native Rust/PostgreSQL database

Usage:
  atomic install [--writer-role ROLE --peer-role ROLE]
  atomic migrate [--writer-role ROLE --peer-role ROLE]
  atomic create --database NAME
  atomic status --database NAME
  atomic consolidate --database ID
  atomic transactor --database ID --endpoint PATH [OPTIONS]
  atomic transactor --database ID --listen IP:PORT --advertise IP:PORT
    --tls-server-name NAME [OPTIONS]

Required environment (all commands except help/version):
  ATOMIC_POSTGRES_URL          PostgreSQL connection string; never printed
  ATOMIC_POSTGRES_TRANSPORT    tls (verified) or plaintext (explicit development)
Optional: ATOMIC_POSTGRES_TLS_ROOT (PEM); ATOMIC_CONNECT_TIMEOUT_MS,
  ATOMIC_STATEMENT_TIMEOUT_MS, ATOMIC_LOCK_TIMEOUT_MS, ATOMIC_TCP_USER_TIMEOUT_MS,
  ATOMIC_KEEPALIVES=true|false, ATOMIC_KEEPALIVES_IDLE_SECONDS,
  ATOMIC_KEEPALIVES_INTERVAL_SECONDS, ATOMIC_KEEPALIVES_RETRIES.
  ATOMIC_SSD_CACHE_DIR opts into a private disposable cache; optional positive
  ATOMIC_SSD_CACHE_ENTRIES and ATOMIC_SSD_CACHE_BYTES bound its shared directory.
Remote TLS: ATOMIC_REMOTE_TLS_CERT (PEM chain), ATOMIC_REMOTE_TLS_KEY (private
  PKCS8 PEM), ATOMIC_REMOTE_TOKEN_FILE (private file containing64 hex digits).
Clients use the same token file and optional ATOMIC_REMOTE_TLS_ROOT PEM trust
  anchor; certificate/name verification is mandatory. No plaintext TCP mode.

Transactor options (numeric limits are positive integers):
  --mode active|auto           active fails on contention (default); auto waits as standby
  --standby-poll-ms N          contender interval, default250; stop wakes idle polling
  --health-listen IP:PORT      opt-in plaintext status only: /health and /ready
  --telemetry-ms N             opt-in bounded JSON operational events on stdout
  --holder ID                  lease holder label (default: process-specific)
  --default-partition KEYWORD   default placement for new ordinary entities
                               (default: :db.part/user; install named partitions first)
  --lease-ms N                 default 5000
  --renew-ms N                 default 1000; must be less than lease
  --queue-capacity N           default 64
  --max-in-flight N            default 4
  --request-timeout-ms N       default 30000 (not a commit/rollback guarantee)
  --max-frame-bytes N          default library frame limit
  --index-threshold-bytes N    default library indexing threshold
  --index-max-bytes N          default library recent-memory maximum
  --index-workers N            1..8 independent edit-preparation lanes, default1
  --hint-workers N             0..8 transaction-hint lanes, default1;0 disables
  --tree-cache-entries N       default library writer cache entries
  --tree-cache-bytes N         default library writer cache bytes
  --excision-max-bytes N       eager maintenance allocation account, default512MiB (not RSS)
  --excision-log-batch N       1..4096 replay transactions per step, default256

Use a pre-existing private owner-only endpoint directory (mkdir -m 700).
SIGINT/SIGTERM stop admission, drain bounded local requests, then stop the writer.
Runtime never installs storage or creates databases. Run install/create separately
with administrative credentials. migrate aliases fresh installation, without an
upgrade chain. Runtime grants are additive; use pre-existing dedicated roles.
consolidate is explicit missing-index recovery or
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
            "install" | "migrate" => &["--writer-role", "--peer-role"],
            "create" | "status" | "consolidate" => &["--database"],
            "transactor" => &[
                "--mode",
                "--standby-poll-ms",
                "--health-listen",
                "--telemetry-ms",
                "--database",
                "--endpoint",
                "--listen",
                "--advertise",
                "--tls-server-name",
                "--holder",
                "--default-partition",
                "--lease-ms",
                "--renew-ms",
                "--queue-capacity",
                "--max-in-flight",
                "--request-timeout-ms",
                "--max-frame-bytes",
                "--index-threshold-bytes",
                "--index-max-bytes",
                "--index-workers",
                "--hint-workers",
                "--tree-cache-entries",
                "--tree-cache-bytes",
                "--excision-max-bytes",
                "--excision-log-batch",
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
            "install" | "migrate" => {
                if parsed.options.contains_key("--writer-role")
                    != parsed.options.contains_key("--peer-role")
                {
                    return Err(usage(
                        "--writer-role and --peer-role must be supplied together",
                    ));
                }
                if parsed.options.contains_key("--writer-role")
                    && parsed.options.get("--writer-role") == parsed.options.get("--peer-role")
                {
                    return Err(usage("writer and peer roles must be distinct"));
                }
            }
            _ => {}
        }
        if parsed.command == "transactor" {
            if parsed
                .options
                .get("--mode")
                .is_some_and(|mode| !matches!(mode.as_str(), "active" | "auto"))
            {
                return Err(usage("--mode must be active or auto"));
            }
            if let Some(address) = parsed.options.get("--health-listen") {
                address
                    .parse::<std::net::SocketAddr>()
                    .map_err(|_| usage("--health-listen requires a numeric IP:port"))?;
            }
            if parsed.number("--excision-log-batch", 256)? > 4096 {
                return Err(usage("--excision-log-batch must be 1..4096"));
            }
            transaction_defaults(
                parsed
                    .options
                    .get("--default-partition")
                    .map(String::as_str),
            )?;
            let local = parsed.options.contains_key("--endpoint");
            let remote = parsed.options.contains_key("--listen");
            if local == remote {
                return Err(usage("choose exactly one of --endpoint or --listen"));
            }
            if remote {
                parsed
                    .required("--advertise")?
                    .parse::<std::net::SocketAddr>()
                    .map_err(|_| usage("--advertise requires a numeric IP:port"))?;
                parsed
                    .required("--listen")?
                    .parse::<std::net::SocketAddr>()
                    .map_err(|_| usage("--listen requires a numeric IP:port"))?;
                parsed.required("--tls-server-name")?;
            } else if parsed.options.contains_key("--advertise")
                || parsed.options.contains_key("--tls-server-name")
            {
                return Err(usage("remote address/name options require --listen"));
            }
            // Validate every numeric setting before touching the database.
            for flag in parsed.options.keys().filter(|key| {
                !matches!(
                    key.as_str(),
                    "--database"
                        | "--mode"
                        | "--health-listen"
                        | "--endpoint"
                        | "--holder"
                        | "--default-partition"
                        | "--listen"
                        | "--advertise"
                        | "--tls-server-name"
                )
            }) {
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

fn transaction_defaults(partition: Option<&str>) -> Result<TransactionDefaults, SemanticError> {
    let Some(text) = partition else {
        return Ok(TransactionDefaults::default());
    };
    let atomic_core::edn::EdnValue::Keyword(name) = atomic_core::edn::read_edn(text)
        .map_err(|_| usage("--default-partition requires one EDN keyword"))?
    else {
        return Err(usage("--default-partition requires one EDN keyword"));
    };
    Ok(TransactionDefaults::default().with_default_partition(name))
}

fn io_error() -> SemanticError {
    SemanticError::new(ErrorCategory::Unavailable, "cli/io", "process I/O failed")
}

fn run(args: Arguments) -> Result<(), SemanticError> {
    match args.command.as_str() {
        "--help" | "help" => {
            print!("{HELP}{}{}", admin::HELP, data::HELP);
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
        "install" | "migrate" => {
            atomic_core::storage::PgBlockStore::install(&connection)?;
            println!("INSTALLED storage_format=atomic/opaque-storage/1 tables=2");
            std::io::stdout().flush().map_err(|_| io_error())?;
            if let Some(writer) = args.options.get("--writer-role") {
                atomic_core::storage::PgBlockStore::connect(&connection)?
                    .grant_runtime_privileges(writer, args.required("--peer-role")?)?;
                println!("GRANTED runtime_roles=true additive=true object_delete=operator_only");
            }
        }
        "create" => {
            let id = args.required("--database")?;
            let result = DatabaseCatalog::connect_configured(&connection)?
                .create_if_absent(id, Schema::new())?;
            println!(
                "{} name={id:?} storage_id={:?} lineage={}",
                if result.created { "CREATED" } else { "EXISTS" },
                result.database.database_id,
                result.database.lineage_id
            );
        }
        "status" => {
            let entry = DatabaseCatalog::connect_configured(&connection)?
                .resolve(args.required("--database")?)?;
            let status = PostgresOperator::connect_configured(&connection)?
                .inspect_database(&entry.database_id, false)?;
            println!(
                "STATUS database={:?} lineage={} basis_t={} generation={}",
                args.required("--database")?,
                entry.lineage_id,
                status.metrics.basis_t,
                status.metrics.generation
            );
        }
        "consolidate" => {
            admin::consolidate(
                &connection,
                args.required("--database")?,
                &atomic_core::MaintenanceControl::default(),
            )?;
        }
        "transactor" => runtime::run(&args, connection)?,
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
    let raw: Vec<String> = std::env::args().skip(1).collect();
    let result = data::dispatch(&raw)
        .or_else(|| admin::dispatch(&raw))
        .unwrap_or_else(|| Arguments::parse(raw).and_then(run));
    match result {
        Ok(()) => ExitCode::SUCCESS,
        Err(error) => {
            // Never print arbitrary server errors, anomalies, transaction data,
            // connection strings or paths originating in failed input.
            eprintln!("ERROR category={:?} code={}", error.category, error.code);
            for field in ["line", "column", "offset"] {
                if let Some(value) = error.details.get(field)
                    && !value.is_empty()
                    && value.bytes().all(|byte| byte.is_ascii_digit())
                {
                    eprintln!("{field}={value}");
                }
            }
            if error.code.starts_with("cli/") || error.code.starts_with("config/") {
                eprintln!("{}", error.message);
            }
            if error.category == ErrorCategory::Fault && error.code == "storage/missing-object" {
                eprintln!(
                    "Inspect the database before retrying. If only the derived current index is damaged, rebuild it with atomic consolidate --database ID using authorized credentials. Missing canonical or retained-receipt data requires separate recovery."
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

    #[test]
    fn default_partition_is_data_not_a_numeric_limit_or_clojure_expression() {
        let args = [
            "transactor",
            "--database",
            "d",
            "--endpoint",
            "/private",
            "--default-partition",
        ];
        let mut valid = args.to_vec();
        valid.push(":app/people");
        assert!(parse(&valid).is_ok());
        for invalid in [
            "42",
            "app/people",
            "::app",
            "(run private-marker)",
            ":app/p :other/p",
        ] {
            let mut input = args.to_vec();
            input.push(invalid);
            let error = parse(&input).err().expect("non-keyword default accepted");
            assert_eq!(error.code, "cli/usage");
            assert!(!format!("{error:?}").contains("private-marker"));
        }
    }

    #[test]
    fn service_mode_health_and_excision_limits_validate_before_io() {
        let base = ["transactor", "--database", "d", "--endpoint", "/private"];
        for (flag, value) in [
            ("--mode", "auto"),
            ("--mode", "active"),
            ("--health-listen", "[::1]:0"),
            ("--standby-poll-ms", "20000"),
            ("--excision-max-bytes", "1"),
            ("--excision-log-batch", "4096"),
        ] {
            let mut args = base.to_vec();
            args.extend([flag, value]);
            assert!(parse(&args).is_ok());
        }
        for (flag, value) in [
            ("--mode", "private-marker"),
            ("--health-listen", "private-marker:80"),
            ("--standby-poll-ms", "0"),
            ("--excision-max-bytes", "0"),
            ("--excision-log-batch", "4097"),
        ] {
            let mut args = base.to_vec();
            args.extend([flag, value]);
            let error = parse(&args).err().expect("invalid settings accepted");
            assert_eq!(error.code, "cli/usage");
            assert!(!format!("{error:?}").contains("private-marker"));
        }
    }
}
