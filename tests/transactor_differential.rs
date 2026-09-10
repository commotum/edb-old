use atomic_core::{
    Attribute, Cardinality, Database, EntityRef, Keyword, PostgresMigrator, PostgresStore, Schema,
    ServiceTransactionReport, TransactionRequest, TransactionService, TransactionServiceConfig,
    TxOp, TxValue, Unique, Value, ValueType,
};
use std::fs::{File, OpenOptions};
use std::io::{Read, Write};
use std::path::{Path, PathBuf};
use std::time::{Duration, SystemTime, UNIX_EPOCH};

mod common;

const PERSON_EMAIL: u32 = 1_000;
const PERSON_NAME: u32 = 1_001;
const PERSON_AGE: u32 = 1_002;
const PERSON_TAG: u32 = 1_003;
const PERSON_FRIEND: u32 = 1_004;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique_name(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                PERSON_EMAIL,
                Keyword::new("person", "email"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_AGE,
            Keyword::new("person", "age"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_TAG,
            Keyword::new("person", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            PERSON_FRIEND,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn config(connection: &str, database_id: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.to_owned(),
        database_id: database_id.to_owned(),
        holder_id: unique_name("transactor_differential_holder"),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: atomic_core::CapacityLimits::default(),
    }
}

fn scalar(value: Value) -> TxValue {
    TxValue::Scalar(value)
}

fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: scalar(value),
    }
}

/// Apply one request to both semantic engines and compare every observable
/// part of the transaction report and both immutable database values.
fn transact_both(
    service: &TransactionService,
    oracle: &mut Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) -> ServiceTransactionReport {
    let expected = oracle.with(&operations, instant).unwrap();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap();

    assert!(!actual.replayed, "a fresh request was reported as a replay");
    assert_eq!(actual.basis_t, expected.db_after.basis_t());
    assert_eq!(actual.tx_data, expected.tx_data);
    assert_eq!(actual.tempids, expected.tempids);
    common::assert_same_information(&actual.db_before, &expected.db_before);
    common::assert_same_information(&actual.db_after, &expected.db_after);

    *oracle = expected.db_after;
    actual
}

fn reject_both(
    service: &TransactionService,
    oracle: &Database,
    request_key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) {
    let expected = oracle.with(&operations, instant).unwrap_err();
    let actual = service
        .client()
        .transact(
            TransactionRequest::new(request_key, operations)
                .comparing_basis(oracle.basis_t())
                .with_tx_instant(instant),
            Duration::from_secs(5),
        )
        .unwrap_err();
    assert_eq!(actual, expected);
}

/// Tiny seeded generator. The default preserves the original differential
/// witness; explicit seeds and a versioned choice trace make additional runs
/// reproducible without a property-test dependency.
struct Deterministic(u64);

impl Deterministic {
    fn next(&mut self) -> u64 {
        let mut value = self.0;
        value ^= value << 13;
        value ^= value >> 7;
        value ^= value << 17;
        self.0 = value;
        value
    }

    fn index(&mut self, len: usize) -> usize {
        (self.next() % len as u64) as usize
    }
}

// Bump the format version if generator draws, operation-family construction,
// seeded schema/data, or failure/restart semantics change incompatibly.
const TRACE_VERSION: &str = "atomic-transactor-differential-v1";
const DEFAULT_SEED: u64 = 0x4d59_5df4_d0f3_3173;
const DEFAULT_STEPS: usize = 72;
const MAX_STEPS: usize = 1_200;
const MAX_TRACE_BYTES: u64 = 1024 * 1024;

#[derive(Clone, Debug, Eq, PartialEq)]
struct GeneratedChoice {
    step: usize,
    family: usize,
    entity: usize,
    nonce: u64,
    first: usize,
    second: usize,
    restart: bool,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct GeneratedTrace {
    seed: u64,
    choices: Vec<GeneratedChoice>,
}

fn parse_number(value: &str) -> Result<u64, String> {
    if let Some(hex) = value.strip_prefix("0x") {
        u64::from_str_radix(hex, 16)
            .map_err(|_| "expected a decimal or 0x hexadecimal integer".into())
    } else {
        value
            .parse()
            .map_err(|_| "expected a decimal or 0x hexadecimal integer".into())
    }
}

impl GeneratedTrace {
    fn generate(seed: u64, steps: usize) -> Result<Self, String> {
        if seed == 0 {
            return Err("seed must be nonzero for the xorshift generator".into());
        }
        if !(12..=MAX_STEPS).contains(&steps) || !steps.is_multiple_of(12) {
            return Err(format!(
                "steps must be a multiple of 12 between 12 and {MAX_STEPS}"
            ));
        }
        let mut random = Deterministic(seed);
        let choices = (0..steps)
            .map(|step| {
                let family = step % 12;
                let entity = random.index(8);
                let nonce = random.next();
                // Keep the original generator's draw order. The additional
                // choices refer to synthetic entity slots, never runtime IDs.
                let first = match family {
                    3 | 11 => random.index(8),
                    4 => random.index(2),
                    8 => 2 + random.index(6),
                    _ => 0,
                };
                let second = if family == 11 { random.index(2) } else { 0 };
                GeneratedChoice {
                    step,
                    family,
                    entity,
                    nonce,
                    first,
                    second,
                    // Reopen after each complete two-pass block, excluding
                    // the final step (which is followed by durable recovery).
                    restart: (step + 1) % 24 == 0 && step + 1 < steps,
                }
            })
            .collect();
        Ok(Self { seed, choices })
    }

    fn encode(&self) -> String {
        let mut encoded = format!(
            "{TRACE_VERSION}\nseed=0x{:016x}\nsteps={}\n",
            self.seed,
            self.choices.len()
        );
        for choice in &self.choices {
            encoded.push_str(&format!(
                "step={} family={} entity={} nonce=0x{:016x} first={} second={} restart={}\n",
                choice.step,
                choice.family,
                choice.entity,
                choice.nonce,
                choice.first,
                choice.second,
                u8::from(choice.restart)
            ));
        }
        encoded
    }

    fn decode(encoded: &str) -> Result<Self, String> {
        if encoded.len() as u64 > MAX_TRACE_BYTES {
            return Err("trace exceeds the 1 MiB input limit".into());
        }
        let mut lines = encoded.lines();
        if lines.next() != Some(TRACE_VERSION) {
            return Err("unsupported differential trace version".into());
        }
        let field = |value: Option<&str>, prefix: &str| -> Result<u64, String> {
            parse_number(
                value
                    .and_then(|value| value.strip_prefix(prefix))
                    .ok_or_else(|| format!("missing or invalid {prefix} field"))?,
            )
        };
        let seed = field(lines.next(), "seed=")?;
        let steps = usize::try_from(field(lines.next(), "steps=")?)
            .map_err(|_| "steps does not fit this platform")?;
        let expected = Self::generate(seed, steps)?;
        let mut choices = Vec::with_capacity(steps);
        for (line_index, line) in lines.enumerate() {
            if choices.len() == steps {
                return Err("trace has more choices than its declared steps".into());
            }
            let mut fields = line.split_whitespace();
            let mut number = |prefix| {
                field(fields.next(), prefix)
                    .map_err(|_| format!("invalid choice field at trace line {}", line_index + 4))
            };
            let mut index = |prefix| {
                usize::try_from(number(prefix)?)
                    .map_err(|_| "choice does not fit this platform".to_owned())
            };
            let step = index("step=")?;
            let family = index("family=")?;
            let entity = index("entity=")?;
            let nonce = number("nonce=")?;
            let first = usize::try_from(number("first=")?)
                .map_err(|_| "choice does not fit this platform")?;
            let second = usize::try_from(number("second=")?)
                .map_err(|_| "choice does not fit this platform")?;
            let restart = match number("restart=")? {
                0 => false,
                1 => true,
                _ => return Err("restart must be zero or one".into()),
            };
            if fields.next().is_some() {
                return Err("trace choice has unexpected fields".into());
            }
            choices.push(GeneratedChoice {
                step,
                family,
                entity,
                nonce,
                first,
                second,
                restart,
            });
        }
        let actual = Self { seed, choices };
        if actual != expected {
            return Err("trace choices do not match its seed, steps and generator version".into());
        }
        Ok(actual)
    }

    fn from_options(
        seed: Option<&str>,
        steps: Option<&str>,
        replay: Option<&Path>,
    ) -> Result<Self, String> {
        if let Some(path) = replay {
            if seed.is_some() || steps.is_some() {
                return Err("replay cannot be combined with seed or steps overrides".into());
            }
            if !std::fs::symlink_metadata(path)
                .map_err(|_| "cannot inspect replay trace")?
                .file_type()
                .is_file()
            {
                return Err("replay trace must be a regular file, not a symlink".into());
            }
            let mut encoded = String::new();
            File::open(path)
                .map_err(|_| "cannot open replay trace")?
                .take(MAX_TRACE_BYTES + 1)
                .read_to_string(&mut encoded)
                .map_err(|_| "cannot read replay trace as UTF-8")?;
            Self::decode(&encoded)
        } else {
            let seed = seed.map(parse_number).transpose()?.unwrap_or(DEFAULT_SEED);
            let steps = steps
                .map(parse_number)
                .transpose()?
                .unwrap_or(DEFAULT_STEPS as u64);
            Self::generate(
                seed,
                usize::try_from(steps).map_err(|_| "steps does not fit this platform")?,
            )
        }
    }

    /// Artifacts contain only generated fixture choices, never connection
    /// strings, credentials, subject data, or arbitrary replay-file contents.
    /// Explicit output paths must be new absolute filenames. The default is
    /// a uniquely named private tempfile retained even when the test panics.
    fn save(&self, output: Option<&Path>) -> Result<PathBuf, String> {
        let (mut file, path) = if let Some(path) = output {
            if !path.is_absolute() {
                return Err("trace output must be an absolute filename".into());
            }
            let mut options = OpenOptions::new();
            options.write(true).create_new(true);
            #[cfg(unix)]
            {
                use std::os::unix::fs::OpenOptionsExt;
                options.mode(0o600);
            }
            (
                options.open(path).map_err(
                    |_| "cannot create trace: target must not exist and parent must exist",
                )?,
                path.to_owned(),
            )
        } else {
            let temporary = tempfile::Builder::new()
                .prefix("atomic-transactor-differential-")
                .suffix(".trace")
                .tempfile()
                .map_err(|_| "cannot create private differential trace")?;
            temporary
                .keep()
                .map_err(|_| "cannot retain differential trace")?
        };
        file.write_all(self.encode().as_bytes())
            .and_then(|()| file.sync_all())
            .map_err(|_| "cannot persist differential trace")?;
        Ok(path)
    }
}

/// Compare the production assessor with `Database::with` without repeatedly
/// materializing the growing database. Every successful transition compares
/// the complete transaction report plus constant-size database metadata; the
/// complete information state is compared once at the end of the sequence.
fn generated_step_both(
    service: &TransactionService,
    oracle: &mut Database,
    request_key: String,
    operations: Vec<TxOp>,
    instant: i64,
) -> bool {
    let expected = oracle.with(&operations, instant);
    let actual = service.client().transact(
        TransactionRequest::new(request_key, operations)
            .comparing_basis(oracle.basis_t())
            .with_tx_instant(instant),
        Duration::from_secs(5),
    );

    match (expected, actual) {
        (Ok(expected), Ok(actual)) => {
            assert!(!actual.replayed);
            assert_eq!(actual.db_before.basis_t(), expected.db_before.basis_t());
            assert_eq!(
                actual.db_before.eidx_frontier(),
                expected.db_before.eidx_frontier()
            );
            assert_eq!(actual.db_before.schema(), expected.db_before.schema());
            assert_eq!(actual.basis_t, expected.db_after.basis_t());
            assert_eq!(actual.db_after.basis_t(), expected.db_after.basis_t());
            assert_eq!(
                actual.db_after.eidx_frontier(),
                expected.db_after.eidx_frontier()
            );
            assert_eq!(actual.db_after.schema(), expected.db_after.schema());
            assert_eq!(actual.tx_data, expected.tx_data);
            assert_eq!(actual.tempids, expected.tempids);
            *oracle = expected.db_after;
            true
        }
        (Err(expected), Err(actual)) => {
            // Messages are diagnostics, not semantic identity. Category and
            // stable code are the public anomaly contract shared by the two
            // assessors.
            assert_eq!(actual.category, expected.category);
            assert_eq!(actual.code, expected.code);
            false
        }
        (expected, actual) => {
            panic!("production/oracle outcome mismatch: expected={expected:?}, actual={actual:?}")
        }
    }
}

#[test]
fn production_writer_matches_the_pure_kernel_across_transactions_and_restart() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique_name("transactor_differential");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let mut oracle = store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let service = TransactionService::start(config(&connection, &database_id)).unwrap();
    let seeded = transact_both(
        &service,
        &mut oracle,
        "seed-people",
        vec![
            add(
                EntityRef::Temp("alice".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_NAME,
                Value::String("Ada".into()),
            ),
            add(EntityRef::Temp("alice".into()), PERSON_AGE, Value::Long(36)),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("logic".into()),
            ),
            add(
                EntityRef::Temp("alice".into()),
                PERSON_TAG,
                Value::String("math".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_EMAIL,
                Value::String("bob@example.test".into()),
            ),
            add(
                EntityRef::Temp("bob".into()),
                PERSON_NAME,
                Value::String("Bob".into()),
            ),
            TxOp::Add {
                entity: EntityRef::Temp("bob".into()),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Temp("alice".into())),
            },
        ],
        10_000,
    );
    let alice = seeded.tempids["alice"];
    let bob = seeded.tempids["bob"];

    let upserted = transact_both(
        &service,
        &mut oracle,
        "upsert-and-replace",
        vec![
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_EMAIL,
                Value::String("alice@example.test".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_NAME,
                Value::String("Ada Lovelace".into()),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_AGE,
                Value::Long(37),
            ),
            add(
                EntityRef::Temp("alice-upsert".into()),
                PERSON_TAG,
                Value::String("computing".into()),
            ),
            TxOp::Retract {
                entity: EntityRef::Temp("alice-upsert".into()),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("math".into()))),
            },
        ],
        10_001,
    );
    assert_eq!(upserted.tempids["alice-upsert"], alice);

    // Force the next assessment to reconstruct its immutable db-before from
    // the durable root and log, not from the first service's resident value.
    service.shutdown();
    let service = TransactionService::start(config(&connection, &database_id)).unwrap();

    reject_both(
        &service,
        &oracle,
        "stale-cas",
        vec![TxOp::Cas {
            entity: EntityRef::Id(alice),
            attribute: PERSON_AGE,
            old: Some(scalar(Value::Long(36))),
            new: scalar(Value::Long(38)),
        }],
        10_002,
    );

    transact_both(
        &service,
        &mut oracle,
        "cas-and-retract",
        vec![
            TxOp::Cas {
                entity: EntityRef::Id(alice),
                attribute: PERSON_AGE,
                old: Some(scalar(Value::Long(37))),
                new: scalar(Value::Long(38)),
            },
            TxOp::Retract {
                entity: EntityRef::Id(alice),
                attribute: PERSON_TAG,
                value: Some(scalar(Value::String("logic".into()))),
            },
            TxOp::Retract {
                entity: EntityRef::Id(bob),
                attribute: PERSON_NAME,
                value: None,
            },
        ],
        10_002,
    );

    let mut age = oracle.schema().attribute(PERSON_AGE).unwrap().clone();
    age.ident = Keyword::new("person", "years");
    age.no_history = true;
    transact_both(
        &service,
        &mut oracle,
        "alter-age-schema",
        vec![TxOp::AlterAttribute(age)],
        10_003,
    );
    transact_both(
        &service,
        &mut oracle,
        "post-alter-and-retract-entity",
        vec![
            add(EntityRef::Id(alice), PERSON_AGE, Value::Long(39)),
            TxOp::RetractEntity(EntityRef::Id(bob)),
        ],
        10_004,
    );
    service.shutdown();

    let mut store = PostgresStore::connect(&connection).unwrap();
    let recovered = store.recover(&database_id).unwrap();
    common::assert_same_information(&recovered, &oracle);
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "age")),
        Some(PERSON_AGE)
    );
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("person", "years")),
        Some(PERSON_AGE)
    );
}

#[test]
fn generated_production_transactions_match_the_pure_kernel() {
    let setting = |name| match std::env::var(name) {
        Ok(value) => Some(value),
        Err(std::env::VarError::NotPresent) => None,
        Err(std::env::VarError::NotUnicode(_)) => panic!("{name} must be UTF-8"),
    };
    let seed = setting("ATOMIC_DIFFERENTIAL_SEED");
    let steps = setting("ATOMIC_DIFFERENTIAL_STEPS");
    let replay = setting("ATOMIC_DIFFERENTIAL_REPLAY");
    let trace = GeneratedTrace::from_options(
        seed.as_deref(),
        steps.as_deref(),
        replay.as_deref().map(Path::new),
    )
    .unwrap_or_else(|error| panic!("invalid differential configuration: {error}"));
    let Some(connection) = connection() else {
        eprintln!("SKIPPED generated PostgreSQL differential test: ATOMIC_POSTGRES_URL is not set");
        return;
    };
    let output = setting("ATOMIC_DIFFERENTIAL_TRACE");
    let trace_path = trace
        .save(output.as_deref().map(Path::new))
        .unwrap_or_else(|error| panic!("cannot save differential trace: {error}"));
    eprintln!(
        "differential trace: seed=0x{:016x} steps={} path={:?}; replay with ATOMIC_DIFFERENTIAL_REPLAY (without seed/steps overrides)",
        trace.seed,
        trace.choices.len(),
        trace_path
    );
    let database_id = unique_name("generated_transactor_differential");
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();
    let mut store = PostgresStore::connect(&connection).unwrap();
    let mut oracle = store.create_database(&database_id, schema()).unwrap();
    drop(store);

    let mut service = TransactionService::start(config(&connection, &database_id)).unwrap();
    let mut seed_ops = Vec::new();
    for ordinal in 0..8_u64 {
        let tempid = format!("person-{ordinal}");
        seed_ops.extend([
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_EMAIL,
                Value::String(format!("person-{ordinal}@example.test")),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_NAME,
                Value::String(format!("Person {ordinal}")),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_AGE,
                Value::Long(20 + ordinal as i64),
            ),
            add(
                EntityRef::Temp(tempid.clone()),
                PERSON_TAG,
                Value::String(format!("seed-tag-{}", ordinal % 3)),
            ),
        ]);
        if ordinal > 0 {
            seed_ops.push(TxOp::Add {
                entity: EntityRef::Temp(tempid),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Temp(format!("person-{}", ordinal - 1))),
            });
        }
    }
    let seeded = transact_both(&service, &mut oracle, "generated-seed", seed_ops, 20_000);
    let entities: Vec<u64> = (0..8)
        .map(|ordinal| seeded.tempids[&format!("person-{ordinal}")])
        .collect();

    // Keep schema/data interaction inside the generated production run, not
    // only in isolated assessor units. This one localized transition covers
    // rename/alias derivation, AVET enablement over extant values, no-history
    // metadata, background publication, and exact report equivalence.
    let mut altered_tag = oracle.schema().attribute(PERSON_TAG).unwrap().clone();
    altered_tag.ident = Keyword::new("person", "label");
    altered_tag.indexed = true;
    altered_tag.no_history = true;
    assert!(generated_step_both(
        &service,
        &mut oracle,
        "generated-schema-transition".into(),
        vec![TxOp::AlterAttribute(altered_tag)],
        20_001,
    ));

    let mut coverage = [0_usize; 12];
    let mut successes = 0_usize;
    let mut rejections = 0_usize;

    // Each complete pass covers all twelve families, including three semantic
    // rejections. The trace has already been saved, so a failure in migration,
    // assessment, restart or final comparison cannot lose its planned choices.
    for choice in &trace.choices {
        let step = choice.step;
        let family = choice.family;
        eprintln!(
            "differential step={step} family={family} restart_after={}",
            choice.restart
        );
        coverage[family] += 1;
        let entity = entities[choice.entity];
        let nonce = choice.nonce;
        let mut operations = match family {
            // Cardinality-one replacement.
            0 => vec![add(
                EntityRef::Id(entity),
                PERSON_NAME,
                Value::String(format!("generated-name-{step}-{nonce}")),
            )],
            // Cardinality-many growth.
            1 => vec![add(
                EntityRef::Id(entity),
                PERSON_TAG,
                Value::String(format!("generated-tag-{}", nonce % 11)),
            )],
            // Exact cardinality-many retraction, preferring a fact that is
            // currently present so this normally changes information.
            2 => {
                let value = oracle
                    .values(entity, PERSON_TAG)
                    .into_iter()
                    .next()
                    .cloned()
                    .unwrap_or_else(|| Value::String(format!("missing-tag-{nonce}")));
                vec![TxOp::Retract {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_TAG,
                    value: Some(scalar(value)),
                }]
            }
            // Ref-valued cardinality-one replacement.
            3 => vec![TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: PERSON_FRIEND,
                value: TxValue::Entity(EntityRef::Id(entities[choice.first])),
            }],
            // Unique-identity upsert. The first two identities are never
            // retracted by this generator, so this always resolves an
            // otherwise-new tempid onto an existing entity.
            4 => {
                let owner = choice.first;
                let tempid = format!("generated-upsert-{step}");
                vec![
                    add(
                        EntityRef::Temp(tempid.clone()),
                        PERSON_EMAIL,
                        Value::String(format!("person-{owner}@example.test")),
                    ),
                    add(
                        EntityRef::Temp(tempid),
                        PERSON_NAME,
                        Value::String(format!("upserted-name-{step}")),
                    ),
                ]
            }
            // Successful CAS, including the Datomic `nil` old-value case
            // after an entity has been retracted.
            5 => {
                let old = oracle
                    .values(entity, PERSON_AGE)
                    .into_iter()
                    .next()
                    .cloned()
                    .map(scalar);
                vec![TxOp::Cas {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_AGE,
                    old,
                    new: scalar(Value::Long(100 + step as i64)),
                }]
            }
            // Stale CAS conflict.
            6 => {
                let wrong = oracle
                    .values(entity, PERSON_AGE)
                    .into_iter()
                    .next()
                    .and_then(|value| match value {
                        Value::Long(value) => Some(Value::Long(value.saturating_add(1))),
                        _ => None,
                    })
                    .unwrap_or(Value::Long(-1));
                vec![TxOp::Cas {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_AGE,
                    old: Some(scalar(wrong)),
                    new: scalar(Value::Long(200 + step as i64)),
                }]
            }
            // Attribute-wide cardinality-many retraction.
            7 => vec![TxOp::Retract {
                entity: EntityRef::Id(entity),
                attribute: PERSON_TAG,
                value: None,
            }],
            // Entity retraction. Preserve two stable identity owners for the
            // upsert and uniqueness-conflict families.
            8 => vec![TxOp::RetractEntity(EntityRef::Id(entities[choice.first]))],
            // Unique-value conflict between two permanent entities.
            9 => vec![add(
                EntityRef::Id(entities[0]),
                PERSON_EMAIL,
                Value::String("person-1@example.test".into()),
            )],
            // Stable incorrect-type failure.
            10 => vec![add(
                EntityRef::Id(entity),
                PERSON_NAME,
                Value::Long(step as i64),
            )],
            // A multi-form transaction with a lookup ref. Reversing an
            // unordered declaration on half the cases also exercises the
            // kernel's normalization boundary.
            11 => vec![
                add(
                    EntityRef::Id(entity),
                    PERSON_TAG,
                    Value::String(format!("batch-tag-{}", nonce % 7)),
                ),
                add(
                    EntityRef::Id(entities[choice.first]),
                    PERSON_NAME,
                    Value::String(format!("batch-name-{step}")),
                ),
                TxOp::Add {
                    entity: EntityRef::Id(entity),
                    attribute: PERSON_FRIEND,
                    value: TxValue::Entity(EntityRef::Lookup {
                        attribute: PERSON_EMAIL,
                        value: Value::String(format!("person-{}@example.test", choice.second)),
                    }),
                },
            ],
            _ => unreachable!(),
        };
        if nonce & 1 == 1 {
            operations.reverse();
        }

        let accepted = generated_step_both(
            &service,
            &mut oracle,
            format!("generated-step-{step}"),
            operations,
            20_002 + step as i64,
        );
        let expected_rejection = matches!(family, 6 | 9 | 10);
        assert_eq!(
            accepted, !expected_rejection,
            "unexpected outcome for generated operation family {family} at step {step}"
        );
        if accepted {
            successes += 1;
        } else {
            rejections += 1;
        }

        // These are orderly writer restarts, not process-kill fault injection.
        // The default 72-step run retains the original two restart boundaries.
        if choice.restart {
            service.shutdown();
            service = TransactionService::start(config(&connection, &database_id)).unwrap();
        }
    }

    let passes = trace.choices.len() / coverage.len();
    assert_eq!(coverage, [passes; 12]);
    assert_eq!(successes, passes * 9);
    assert_eq!(rejections, passes * 3);
    service.shutdown();

    // One linear full-state comparison avoids an O(N^2) test while proving
    // that matching reports compose into the same current and historical
    // information in every index order after durable recovery.
    let mut store = PostgresStore::connect(&connection).unwrap();
    let recovered = store.recover(&database_id).unwrap();
    common::assert_same_information(&recovered, &oracle);
    eprintln!(
        "PostgreSQL differential complete: steps={} accepted={successes} rejected={rejections} orderly_restarts={} trace={:?}",
        trace.choices.len(),
        trace.choices.iter().filter(|choice| choice.restart).count(),
        trace_path
    );
}

#[test]
fn generated_trace_roundtrips_and_preserves_default_restart_boundaries() {
    let trace = GeneratedTrace::from_options(None, None, None).unwrap();
    assert_eq!(trace.seed, DEFAULT_SEED);
    assert_eq!(trace.choices.len(), DEFAULT_STEPS);
    assert_eq!(GeneratedTrace::decode(&trace.encode()).unwrap(), trace);
    assert_eq!(
        trace
            .choices
            .iter()
            .filter(|choice| choice.restart)
            .map(|choice| choice.step)
            .collect::<Vec<_>>(),
        vec![23, 47]
    );
    for seed in [1, 2, u64::MAX] {
        let trace = GeneratedTrace::generate(seed, 24).unwrap();
        assert_eq!(GeneratedTrace::decode(&trace.encode()).unwrap(), trace);
    }
    assert_ne!(
        GeneratedTrace::generate(1, 24).unwrap().choices,
        GeneratedTrace::generate(2, 24).unwrap().choices
    );
}

#[test]
fn generated_trace_rejects_invalid_configuration_and_changed_choices() {
    assert_eq!(parse_number("42").unwrap(), 42);
    assert_eq!(parse_number("0x2a").unwrap(), 42);
    for invalid in ["", "-1", "no-seed", "18446744073709551616"] {
        assert!(parse_number(invalid).is_err());
    }
    assert!(GeneratedTrace::generate(0, 72).is_err());
    for steps in [0, 1, 13, MAX_STEPS + 12] {
        assert!(GeneratedTrace::generate(1, steps).is_err());
    }
    assert!(GeneratedTrace::from_options(Some("1"), None, Some(Path::new("unused"))).is_err());
    let trace = GeneratedTrace::generate(42, 12).unwrap();
    let encoded = trace.encode();
    assert!(GeneratedTrace::decode(&encoded.replace(TRACE_VERSION, "unsupported-v2")).is_err());
    assert!(GeneratedTrace::decode(&encoded.replace("family=0", "family=9")).is_err());
    assert!(GeneratedTrace::decode(&encoded.replace("restart=0", "restart=2")).is_err());
    assert!(GeneratedTrace::decode(&format!("{encoded}unexpected\n")).is_err());
    assert!(
        GeneratedTrace::decode(&encoded.lines().take(14).collect::<Vec<_>>().join("\n")).is_err()
    );
    assert!(GeneratedTrace::decode(&"x".repeat(MAX_TRACE_BYTES as usize + 1)).is_err());
}

#[test]
fn generated_trace_files_are_replayable_and_never_overwritten() {
    let directory = tempfile::tempdir().unwrap();
    let target = directory.path().join("recorded.trace");
    let trace = GeneratedTrace::generate(42, 24).unwrap();
    assert_eq!(trace.save(Some(&target)).unwrap(), target);
    assert_eq!(
        GeneratedTrace::from_options(None, None, Some(&target)).unwrap(),
        trace
    );
    assert!(
        GeneratedTrace::generate(7, 12)
            .unwrap()
            .save(Some(&target))
            .is_err()
    );
    assert_eq!(std::fs::read_to_string(&target).unwrap(), trace.encode());
    assert!(trace.save(Some(Path::new("relative.trace"))).is_err());
    assert!(GeneratedTrace::from_options(None, None, Some(directory.path())).is_err());
    #[cfg(unix)]
    {
        use std::os::unix::fs::{PermissionsExt, symlink};
        assert_eq!(
            std::fs::metadata(&target).unwrap().permissions().mode() & 0o777,
            0o600
        );
        let link = directory.path().join("link.trace");
        symlink(&target, &link).unwrap();
        assert!(trace.save(Some(&link)).is_err());
        assert!(GeneratedTrace::from_options(None, None, Some(&link)).is_err());
    }
}
