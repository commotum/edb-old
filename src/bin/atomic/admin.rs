//! Explicit operator commands. Runtime startup never routes through this module.
use atomic_core::sql_io::SqlClient;
use atomic_core::{
    BackupPoint, DatabaseCatalog, ErrorCategory, FulltextStore, GarbageInventory, PortableBackup,
    PostgresConnectionConfig, PostgresIndexer, PostgresMigrator, PostgresOperator, PostgresStore,
    Schema, SemanticError, postgres_config_from_env,
};
use std::collections::{BTreeMap, BTreeSet};
use std::io::Write;
use std::path::Path;
use std::time::{Duration, Instant};

pub const HELP: &str = "
Administrative commands (explicit credentials/targets; no automatic provisioning):
  atomic create --database NAME
  atomic list-databases [--retired] [--after NAME-OR-STORAGE-ID] [--limit N]
  atomic rename --database NAME --new-name NAME [--lineage UUID --apply]
  atomic delete --database NAME [--lineage UUID --apply]
  atomic gc-deleted --storage-id ID --lineage UUID --postgres-database NAME
    --catalog-schema NAME --older-than-seconds N [--apply --batches N]
  atomic backup --database ID --repository PATH
  atomic list-backups --repository PATH
  atomic verify-backup --repository PATH --basis N --generation N [--presence-only]
  atomic restore --repository PATH --basis N --generation N --target-database ID
    --postgres-database NAME --catalog-schema NAME [--apply]
  atomic inspect --database ID [--shallow]
  atomic gc --postgres-database NAME --catalog-schema NAME --older-than-seconds N
    [--apply] [--batches N]
  atomic fulltext-rebuild --database ID
    [--discard-manifest SHA256 --apply --batches N]

Restore and GC preview by default; --apply authorizes their writes. Restore's
ATOMIC_POSTGRES_URL is the DESTINATION, not the backup's source. All targets must
already be migrated. Exact basis/generation are required; zero is valid. GC is
catalog-wide, not scoped to one logical database; explicit retention is required.
Each GC/repair batch bounds victims, not examined rows or elapsed time. Default
batches=1. Short retention is allowed but can disrupt disconnected readers.
Offline list/verify need no PostgreSQL environment. Verification is deep unless
--presence-only; backup success alone is not deep semantic verification.
SIGINT/SIGTERM may leave committed resumable work: retry the same exact target
and point. Rebuild preserves facts; explicit discard removes derived search data
and may interrupt cold search readers. See docs/admin.md and docs/operations.md.

Create is idempotent; list is paginated (default 1000, maximum 4096). Rename/delete
preview by default. Apply requires the lineage printed by preview to guard name
reuse. Delete retires and fences a database; it does not reclaim storage. Already
captured pinned values remain readable. gc-deleted targets one retired storage
ID/lineage, waits for pins and preserves shared content. Each reclamation batch
bounds removed rows, not all inspected metadata or elapsed time. See
docs/database-lifecycle.md. These mutations require catalog owner authority.
";

pub fn dispatch(arguments: &[String]) -> Option<Result<(), SemanticError>> {
    let command = arguments.first()?.as_str();
    let (values, switches): (&[&str], &[&str]) = match command {
        "migrate" => (&["--writer-role", "--peer-role"], &[]),
        "create" | "status" | "consolidate" => (&["--database"], &[]),
        "list-databases" => (&["--after", "--limit"], &["--retired"]),
        "rename" => (&["--database", "--new-name", "--lineage"], &["--apply"]),
        "delete" => (&["--database", "--lineage"], &["--apply"]),
        "gc-deleted" => (
            &[
                "--storage-id",
                "--lineage",
                "--postgres-database",
                "--catalog-schema",
                "--older-than-seconds",
                "--batches",
            ],
            &["--apply"],
        ),
        "backup" => (&["--database", "--repository"], &[]),
        "list-backups" => (&["--repository"], &[]),
        "verify-backup" => (
            &["--repository", "--basis", "--generation"],
            &["--presence-only"],
        ),
        "restore" => (
            &[
                "--repository",
                "--basis",
                "--generation",
                "--target-database",
                "--postgres-database",
                "--catalog-schema",
            ],
            &["--apply"],
        ),
        "inspect" => (&["--database"], &["--shallow"]),
        "gc" => (
            &[
                "--postgres-database",
                "--catalog-schema",
                "--older-than-seconds",
                "--batches",
            ],
            &["--apply"],
        ),
        "fulltext-rebuild" => (
            &["--database", "--discard-manifest", "--batches"],
            &["--apply"],
        ),
        _ => return None,
    };
    Some(Arguments::parse(command, &arguments[1..], values, switches).and_then(run))
}

struct Arguments {
    command: String,
    values: BTreeMap<String, String>,
    switches: BTreeSet<String>,
}
impl Arguments {
    fn parse(
        command: &str,
        raw: &[String],
        values: &[&str],
        switches: &[&str],
    ) -> Result<Self, SemanticError> {
        let mut parsed = Self {
            command: command.into(),
            values: BTreeMap::new(),
            switches: BTreeSet::new(),
        };
        let mut iter = raw.iter();
        while let Some(flag) = iter.next() {
            if switches.contains(&flag.as_str()) {
                if !parsed.switches.insert(flag.clone()) {
                    return Err(usage("duplicate option"));
                }
            } else if values.contains(&flag.as_str()) {
                let value = iter
                    .next()
                    .filter(|value| !value.is_empty() && !value.starts_with("--"))
                    .ok_or_else(|| usage("option requires a nonempty value"))?;
                if parsed.values.insert(flag.clone(), value.clone()).is_some() {
                    return Err(usage("duplicate option"));
                }
            } else {
                return Err(usage("unknown or misplaced option; run atomic --help"));
            }
        }
        let required: &[&str] = match command {
            "migrate" => &[],
            "list-databases" => &[],
            "rename" => &["--database", "--new-name"],
            "delete" => &["--database"],
            "gc-deleted" => &[
                "--storage-id",
                "--lineage",
                "--postgres-database",
                "--catalog-schema",
                "--older-than-seconds",
            ],
            "create" | "status" | "consolidate" | "inspect" | "fulltext-rebuild" => &["--database"],
            "backup" => &["--database", "--repository"],
            "list-backups" => &["--repository"],
            "verify-backup" => &["--repository", "--basis", "--generation"],
            "restore" => &[
                "--repository",
                "--basis",
                "--generation",
                "--target-database",
                "--postgres-database",
                "--catalog-schema",
            ],
            "gc" => &[
                "--postgres-database",
                "--catalog-schema",
                "--older-than-seconds",
            ],
            _ => unreachable!(),
        };
        for flag in required {
            parsed.required(flag)?;
        }
        for flag in ["--basis", "--generation", "--older-than-seconds", "--limit"] {
            if parsed.values.contains_key(flag) {
                parsed.number(flag)?;
            }
        }
        if parsed.values.contains_key("--batches") && parsed.number("--batches")? == 0 {
            return Err(usage("--batches requires a positive integer"));
        }
        if command == "migrate" {
            let writer = parsed.values.get("--writer-role");
            let peer = parsed.values.get("--peer-role");
            if writer.is_some() != peer.is_some() {
                return Err(usage(
                    "--writer-role and --peer-role must be supplied together",
                ));
            }
            if writer.is_some() && writer == peer {
                return Err(usage("writer and peer roles must be distinct"));
            }
        }
        if matches!(command, "rename" | "delete") && parsed.has("--apply") {
            parsed.required("--lineage")?;
        }
        if command == "list-databases"
            && parsed.values.contains_key("--limit")
            && !(1..=4096).contains(&parsed.number("--limit")?)
        {
            return Err(usage("--limit must be between 1 and 4096"));
        }
        if matches!(command, "gc" | "gc-deleted") {
            if parsed.number("--older-than-seconds")? > (i64::MAX as u64) / 1000 {
                return Err(usage(
                    "retention interval exceeds PostgreSQL millisecond range",
                ));
            }
            if !parsed.has("--apply") && parsed.values.contains_key("--batches") {
                return Err(usage(
                    "--batches requires --apply; preview is one read-only inventory",
                ));
            }
        }
        if command == "fulltext-rebuild" {
            let discard = parsed.values.contains_key("--discard-manifest");
            if discard {
                parse_digest(parsed.required("--discard-manifest")?)?;
            }
            if discard != parsed.has("--apply") {
                return Err(usage(
                    "derived discard requires both --discard-manifest and --apply",
                ));
            }
            if !discard && parsed.values.contains_key("--batches") {
                return Err(usage("--batches applies only to explicit derived discard"));
            }
        }
        Ok(parsed)
    }
    fn required(&self, flag: &str) -> Result<&str, SemanticError> {
        self.values
            .get(flag)
            .map(String::as_str)
            .ok_or_else(|| usage(format!("required option {flag} is missing")))
    }
    fn has(&self, flag: &str) -> bool {
        self.switches.contains(flag)
    }
    fn number(&self, flag: &str) -> Result<u64, SemanticError> {
        let value = self.required(flag)?;
        if !value.bytes().all(|b| b.is_ascii_digit()) {
            return Err(usage(format!("{flag} requires a nonnegative integer")));
        }
        value
            .parse()
            .map_err(|_| usage(format!("{flag} exceeds its supported integer range")))
    }
    fn batches(&self) -> Result<u64, SemanticError> {
        if self.values.contains_key("--batches") {
            self.number("--batches")
        } else {
            Ok(1)
        }
    }
}

fn usage(message: impl Into<String>) -> SemanticError {
    SemanticError::incorrect("cli/usage", message)
}

fn catalog_entry(label: &str, entry: &atomic_core::DatabaseCatalogEntry) {
    println!(
        "{label} name={:?} storage_id={:?} lineage={} retired={}",
        entry.name, entry.database_id, entry.lineage_id, entry.retired
    );
}
fn process_io() -> SemanticError {
    SemanticError::new(ErrorCategory::Unavailable, "cli/io", "process I/O failed")
}
fn progress(command: &str, phase: &str) -> Result<(), SemanticError> {
    println!("PROGRESS command={command} phase={phase}");
    std::io::stdout().flush().map_err(|_| process_io())
}
fn hex(hash: &[u8]) -> String {
    hash.iter().map(|byte| format!("{byte:02x}")).collect()
}
fn parse_digest(text: &str) -> Result<[u8; 32], SemanticError> {
    if text.len() != 64 || !text.bytes().all(|b| b.is_ascii_hexdigit()) {
        return Err(usage(
            "--discard-manifest requires exactly 64 hexadecimal characters",
        ));
    }
    let mut result = [0; 32];
    for (i, byte) in result.iter_mut().enumerate() {
        *byte = u8::from_str_radix(&text[i * 2..i * 2 + 2], 16)
            .map_err(|_| usage("invalid manifest digest"))?;
    }
    Ok(result)
}
fn point(label: &str, point: &BackupPoint) {
    println!(
        "{label} lineage={} basis_t={} generation={} manifest={} objects_written={} objects_reused={}",
        point.lineage_id,
        point.basis_t,
        point.log_generation,
        hex(&point.manifest_hash),
        point.objects_written,
        point.objects_reused
    );
}
fn sql_client(config: &PostgresConnectionConfig) -> Result<SqlClient, SemanticError> {
    SqlClient::connect_with(|| config.connect())
}
fn query_error(_: postgres::Error) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "cli/catalog-observation",
        "catalog observation failed",
    )
}
fn verify_target(config: &PostgresConnectionConfig, args: &Arguments) -> Result<(), SemanticError> {
    // Resolve the actual Atomic relation namespace, not merely current_schema
    // (an earlier empty search_path entry could otherwise approve another catalog).
    let row = sql_client(config)?.query_one("SELECT current_database(), n.nspname FROM pg_catalog.pg_class c JOIN pg_catalog.pg_namespace n ON n.oid=c.relnamespace WHERE c.oid='atomic_databases'::regclass", &[]).map_err(query_error)?;
    let database: String = row.get(0);
    let schema: String = row.get(1);
    if database != args.required("--postgres-database")?
        || schema != args.required("--catalog-schema")?
    {
        return Err(SemanticError::new(
            ErrorCategory::Conflict,
            "cli/target-mismatch",
            "configured PostgreSQL database/catalog do not match the explicit target",
        ));
    }
    println!("TARGET postgres_database={database:?} catalog_schema={schema:?}");
    Ok(())
}
fn selected_point(args: &Arguments) -> Result<BackupPoint, SemanticError> {
    let basis = args.number("--basis")?;
    let generation = args.number("--generation")?;
    PortableBackup::list_backup_points(Path::new(args.required("--repository")?))?
        .into_iter()
        .find(|p| p.basis_t == basis && p.log_generation == generation)
        .ok_or_else(|| {
            SemanticError::new(
                ErrorCategory::NotFound,
                "cli/backup-point-not-found",
                "repository does not contain the exact selected basis/generation",
            )
        })
}

fn run(args: Arguments) -> Result<(), SemanticError> {
    let started = Instant::now();
    if args.command == "list-backups" {
        let points = PortableBackup::list_backup_points(Path::new(args.required("--repository")?))?;
        for p in &points {
            point("BACKUP_POINT", p);
        }
        println!("BACKUPS count={}", points.len());
        return Ok(());
    }
    if args.command == "verify-backup" {
        progress("verify-backup", "verify")?;
        let directory = Path::new(args.required("--repository")?);
        if args.has("--presence-only") {
            let p = PortableBackup::verify_backup_point_presence(
                directory,
                args.number("--basis")?,
                args.number("--generation")?,
            )?;
            point("VERIFIED_PRESENCE", &p);
            println!(
                "VERIFICATION semantic=false elapsed_ms={}",
                started.elapsed().as_millis()
            );
        } else {
            let verified = PortableBackup::verify_backup_point(
                directory,
                args.number("--basis")?,
                args.number("--generation")?,
                true,
            )?;
            point("VERIFIED_DEEP", &verified.point);
            println!(
                "VERIFICATION semantic=true objects_read={} elapsed_ms={}",
                verified.objects_read,
                started.elapsed().as_millis()
            );
        }
        return Ok(());
    }
    let config = postgres_config_from_env()?;
    match args.command.as_str() {
        "migrate" => {
            let mut migrator = PostgresMigrator::connect_configured(&config)?;
            migrator.migrate()?;
            println!(
                "MIGRATED schema_version={}",
                atomic_core::POSTGRES_SCHEMA_VERSION
            );
            std::io::stdout().flush().map_err(|_| process_io())?;
            if let Some(writer) = args.values.get("--writer-role") {
                migrator.grant_runtime_privileges(writer, args.required("--peer-role")?)?;
                println!("GRANTED runtime_roles=true");
            }
        }
        "create" => {
            let id = args.required("--database")?;
            let result = DatabaseCatalog::connect_configured(&config)?
                .create_if_absent(id, Schema::new())?;
            catalog_entry(
                if result.created { "CREATED" } else { "EXISTS" },
                &result.database,
            );
        }
        "list-databases" => {
            let mut catalog = DatabaseCatalog::connect_configured(&config)?;
            let after = args.values.get("--after").map(String::as_str);
            let limit = if args.values.contains_key("--limit") {
                args.number("--limit")? as usize
            } else {
                1000
            };
            let entries = if args.has("--retired") {
                catalog.list_retired(after, limit)?
            } else {
                catalog.list(after, limit)?
            };
            for entry in &entries {
                catalog_entry("DATABASE", entry);
            }
            println!(
                "DATABASE_PAGE entries={} limit={limit} retired={} full_page={}",
                entries.len(),
                args.has("--retired"),
                entries.len() == limit
            );
        }
        "rename" | "delete" => {
            let mut catalog = DatabaseCatalog::connect_configured(&config)?;
            let name = args.required("--database")?;
            if !args.has("--apply") {
                let entry = catalog.resolve(name)?;
                catalog_entry("LIFECYCLE_PREVIEW", &entry);
                println!(
                    "LIFECYCLE action={} new_name={:?} applied=false",
                    args.command,
                    args.values.get("--new-name")
                );
            } else if args.command == "rename" {
                let entry = catalog.rename_checked(
                    name,
                    args.required("--new-name")?,
                    args.required("--lineage")?,
                )?;
                catalog_entry("RENAMED", &entry);
            } else if let Some(entry) = catalog.retire_checked(name, args.required("--lineage")?)? {
                catalog_entry("RETIRED", &entry);
                println!("DELETED reclaimed=false");
            } else {
                println!("DELETE unchanged=true active_name_absent=true");
            }
        }
        "gc-deleted" => {
            verify_target(&config, &args)?;
            let mut operator = PostgresOperator::connect_configured(&config)?;
            let storage_id = args.required("--storage-id")?;
            let lineage = args.required("--lineage")?;
            let age = Duration::from_secs(args.number("--older-than-seconds")?);
            let batches = if args.has("--apply") {
                args.batches()?
            } else {
                1
            };
            for batch in 1..=batches {
                let report = if args.has("--apply") {
                    operator.reclaim_retired_database(storage_id, lineage, age)?
                } else {
                    operator.preview_retired_database_reclamation(storage_id, lineage, age)?
                };
                println!(
                    "RECLAMATION storage_id={:?} lineage={} batch={batch} phase={} rows_selected={} rows_removed={} rows_inserted={} rows_updated={} objects_read={} pins_checked={} complete={} applied={}",
                    report.storage_id,
                    report.lineage_id,
                    report.phase,
                    report.rows_selected,
                    report.rows_removed,
                    report.rows_inserted,
                    report.rows_updated,
                    report.objects_read,
                    report.pins_checked,
                    report.complete,
                    report.applied
                );
                std::io::stdout().flush().map_err(|_| process_io())?;
                if report.complete {
                    break;
                }
            }
        }
        "status" => {
            let entry = DatabaseCatalog::connect_configured(&config)?
                .resolve(args.required("--database")?)?;
            let status =
                PostgresStore::connect_configured(&config)?.database_status(&entry.database_id)?;
            println!(
                "STATUS database={:?} lineage={} basis_t={} generation={}",
                args.required("--database")?,
                status.lineage_id,
                status.basis_t,
                status.log_generation
            );
        }
        "consolidate" => {
            let mut indexer =
                PostgresIndexer::connect_configured(&config, args.required("--database")?)?;
            let receipt = indexer.consolidate()?;
            println!(
                "INDEXED basis_t={} input_datoms={}",
                receipt.basis_t, receipt.input_datoms
            );
            if let Some(error) = indexer.fulltext_build_error() {
                println!(
                    "SEARCH basis_t={} status=failed category={:?} code={}",
                    receipt.basis_t, error.category, error.code
                );
            } else {
                println!("SEARCH basis_t={} status=checked", receipt.basis_t);
            }
        }
        "backup" => {
            progress("backup", "capture")?;
            let p = PortableBackup::connect_configured(&config)?.backup_database(
                args.required("--database")?,
                Path::new(args.required("--repository")?),
            )?;
            point("BACKED_UP", &p);
            println!(
                "BACKUP semantic_verification=false elapsed_ms={}",
                started.elapsed().as_millis()
            );
        }
        "restore" => {
            let p = selected_point(&args)?;
            drop(PostgresStore::connect_configured(&config)?);
            verify_target(&config, &args)?;
            let directory = Path::new(args.required("--repository")?);
            if !args.has("--apply") {
                progress("restore", "preview-deep-verification")?;
                PortableBackup::verify_backup_point(directory, p.basis_t, p.log_generation, true)?;
                point("RESTORE_POINT", &p);
                println!(
                    "RESTORE_PREVIEW target_database={:?} applied=false target_compatibility=checked-on-apply elapsed_ms={}",
                    args.required("--target-database")?,
                    started.elapsed().as_millis()
                );
            } else {
                progress("restore", "verify-stage-activate")?;
                let db = PortableBackup::connect_configured(&config)?.restore_backup_point(
                    directory,
                    p.basis_t,
                    p.log_generation,
                    args.required("--target-database")?,
                )?;
                println!(
                    "RESTORED target_database={:?} lineage={} basis_t={} selected_generation={} elapsed_ms={}",
                    args.required("--target-database")?,
                    p.lineage_id,
                    db.basis_t(),
                    p.log_generation,
                    started.elapsed().as_millis()
                );
            }
        }
        "inspect" => {
            progress("inspect", "consistent-snapshot")?;
            let deep = !args.has("--shallow");
            let entry = DatabaseCatalog::connect_configured(&config)?
                .resolve(args.required("--database")?)?;
            let report = PostgresOperator::connect_configured(&config)?
                .inspect_database(&entry.database_id, deep)?;
            let m = &report.metrics;
            println!(
                "INSPECT healthy={} deep_derived={} basis_t={} index_basis_t={} index_lag={} problems={} pending_avet_projections={} pending_tree_publications={} pending_tree_membership_nodes={} shared_reachability_uncertain={} elapsed_ms={}",
                report.healthy(),
                deep,
                m.basis_t,
                m.index_basis_t,
                m.index_lag,
                report.problems.len(),
                m.pending_avet_projections,
                m.pending_tree_publications,
                m.pending_tree_membership_nodes,
                m.shared_reachability_uncertain,
                started.elapsed().as_millis()
            );
            for problem in &report.problems {
                // Codes are diagnostic labels; messages may contain subject data.
                let code = if problem.code.len() <= 160
                    && problem
                        .code
                        .bytes()
                        .all(|b| b.is_ascii_alphanumeric() || b"/-_.".contains(&b))
                {
                    problem.code.as_str()
                } else {
                    "redacted"
                };
                println!("INTEGRITY_PROBLEM code={code}");
            }
            if !report.healthy() {
                return Err(SemanticError::new(
                    ErrorCategory::Fault,
                    "cli/integrity-problems",
                    "inspection found integrity problems",
                ));
            }
        }
        "gc" => {
            verify_target(&config, &args)?;
            let age = Duration::from_secs(args.number("--older-than-seconds")?);
            let mut operator = PostgresOperator::connect_configured(&config)?;
            println!(
                "GC_POLICY older_than_seconds={} short_retention={} scope=entire-catalog",
                age.as_secs(),
                age < atomic_core::RECOMMENDED_GARBAGE_COLLECTION_AGE
            );
            if args.has("--apply") {
                for batch in 1..=args.batches()? {
                    progress("gc", "collect-bounded-batch")?;
                    inventory("GC_APPLIED", batch, &operator.collect_garbage(age)?);
                    std::io::stdout().flush().map_err(|_| process_io())?;
                }
                println!(
                    "GC_FINISHED batches={} global_quiescence=not-established elapsed_ms={}",
                    args.batches()?,
                    started.elapsed().as_millis()
                );
            } else {
                inventory("GC_PREVIEW", 1, &operator.garbage_inventory(age)?);
            }
        }
        "fulltext-rebuild" => rebuild(&config, &args)?,
        _ => unreachable!("validated administrative command"),
    }
    Ok(())
}

fn inventory(label: &str, batch: u64, inventory: &GarbageInventory) {
    println!(
        "{label} batch={batch} applied={} segments={} programs={} tree_publications={} tree_build_intents={} tree_nodes={} receipt_archives={} receipt_conversions={} log_generations={} semantic_roots={} semantic_nodes={} fulltext_blocks={}",
        inventory.applied,
        inventory.segment_hashes.len(),
        inventory.program_hashes.len(),
        inventory.tree_publications.len(),
        inventory.tree_build_intents.len(),
        inventory.tree_node_hashes.len(),
        inventory.request_base_archives.len(),
        inventory.receipt_archive_conversions.len(),
        inventory.log_generations.len(),
        inventory.semantic_commitment_roots.len(),
        inventory.semantic_commitment_node_hashes.len(),
        inventory.fulltext_blocks
    );
}
fn latest_manifest(
    config: &PostgresConnectionConfig,
    database: &str,
) -> Result<[u8; 32], SemanticError> {
    let row = sql_client(config)?.query_opt("SELECT manifest_hash FROM atomic_tree_publications WHERE database_id=$1 ORDER BY publication_revision DESC LIMIT 1", &[&database]).map_err(query_error)?
        .ok_or_else(|| SemanticError::new(ErrorCategory::NotFound, "cli/no-native-publication", "database has no native publication; diagnose before explicit consolidation"))?;
    let bytes: Vec<u8> = row.get(0);
    bytes.try_into().map_err(|_| {
        SemanticError::new(
            ErrorCategory::Fault,
            "cli/manifest-hash",
            "published manifest digest has invalid length",
        )
    })
}
fn rebuild(config: &PostgresConnectionConfig, args: &Arguments) -> Result<(), SemanticError> {
    let database = args.required("--database")?;
    // Validate catalog/runtime compatibility before considering a destructive repair.
    let mut indexer = PostgresIndexer::connect_configured(config, database)?;
    // Missing publication is a diagnosis, not a successful no-op (and includes
    // misspelled/absent logical database names). Never consolidate implicitly.
    let current_manifest = latest_manifest(config, indexer.database_id())?;
    if let Some(hash) = args.values.get("--discard-manifest") {
        let selected = parse_digest(hash)?;
        if current_manifest != selected {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "cli/repair-target-mismatch",
                "discard digest is not this database's newest published manifest",
            ));
        }
        let mut store = FulltextStore::connect(config)?;
        let mut complete = false;
        for batch in 1..=args.batches()? {
            complete = store.discard_projection(selected, 4096)?;
            println!(
                "FULLTEXT_DISCARD manifest={} batch={batch} complete={complete}",
                hex(&selected)
            );
            std::io::stdout().flush().map_err(|_| process_io())?;
            if complete {
                break;
            }
        }
        if !complete {
            println!("FULLTEXT_REPAIR status=pending action=repeat-same-command");
            return Ok(());
        }
        if latest_manifest(config, indexer.database_id())? != selected {
            return Err(SemanticError::new(
                ErrorCategory::Conflict,
                "cli/repair-target-advanced",
                "database advanced during repair; reselect the intended manifest explicitly",
            ));
        }
    }
    progress("fulltext-rebuild", "reconstruct-missing-search")?;
    if let Some(projection) = indexer.rebuild_fulltext()? {
        println!(
            "FULLTEXT_REBUILT basis_t={} generation={} source_manifest={} records={} blocks={} encoded_bytes={}",
            projection.source_basis_t,
            projection.source_generation,
            hex(&projection.source_manifest),
            projection.record_count,
            projection.block_count,
            projection.encoded_bytes
        );
    } else {
        println!("FULLTEXT_REBUILT status=not-required");
    }
    Ok(())
}
