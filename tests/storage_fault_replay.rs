//! Seeded, replayable storage-fault schedules using the existing index fault
//! hook and pure/native information oracle. No process crash is simulated:
//! `AfterSegments` means an injected interruption after immutable upload and
//! before root publication. V2 also crosses graceful writer replacement, exact
//! receipt retry and bounded consumer restart/acknowledgment. V1 traces retain
//! their exact action meanings. A separately labeled controlled assertion proves
//! reduction; it is not evidence of a production defect.
use atomic_core::{
    Attribute, Cardinality, ChangeConsumer, ChangeConsumerConfig, Database, DatabaseValue,
    EntityRef, ErrorCategory, IndexBuildFault, Keyword, Peer, PostgresIndexer, PostgresMigrator,
    PostgresStore, Schema, SemanticError, ServiceTransactionReport, TransactionRequest,
    TransactionService, TxOp, Value, ValueType,
};
use std::collections::BTreeMap;
use std::fs::OpenOptions;
use std::io::{Read, Write};
use std::panic::{AssertUnwindSafe, catch_unwind};
use std::path::{Path, PathBuf};
use std::time::Duration;

mod common;

const VERSION: &str = "atomic-storage-fault-trace-v2";
const LEGACY_VERSION: &str = "atomic-storage-fault-trace-v1";
const DEFAULT_SEED: u64 = 0x39dc_55b0_63e7_a412;
const DEFAULT_STEPS: usize = 12;
const MAX_STEPS: usize = 128;
const MAX_TRACE_BYTES: u64 = 64 * 1024;
const VALUE: u32 = 1_000;

#[derive(Clone, Debug, Eq, PartialEq)]
enum Step {
    Put { slot: usize, value: i64 },
    InterruptUpload,
    PublishIndex,
    ReopenPeer { cache_entries: usize },
    RestartWriter,
    RetryLast,
    ResumeConsumer,
}

#[derive(Clone, Debug, Eq, PartialEq)]
struct Trace {
    seed: u64,
    steps: Vec<Step>,
}

impl Trace {
    fn generate(seed: u64, count: usize) -> Result<Self, String> {
        if seed == 0 || !(3..=MAX_STEPS).contains(&count) {
            return Err(
                "seed must be nonzero and generated steps must be between 3 and 128".into(),
            );
        }
        // Same small xorshift construction used by transactor_differential.
        let mut state = seed;
        let mut next = || {
            state ^= state << 13;
            state ^= state >> 7;
            state ^= state << 17;
            state
        };
        let mut steps = Vec::new();
        for index in 0..count {
            let draw = next();
            let family = match index {
                0 => 0,
                1 => 1,
                2 => 3,
                3 => 5,
                4 | 8 => 6,
                5 | 7 => 7,
                6 => 0,
                _ => (draw % 8) as usize,
            };
            steps.push(match family {
                0 | 4 => Step::Put {
                    slot: (next() % 4) as usize,
                    value: (next() % 2001) as i64 - 1000,
                },
                1 => Step::InterruptUpload,
                2 => Step::PublishIndex,
                5 => Step::RestartWriter,
                6 => Step::RetryLast,
                7 => Step::ResumeConsumer,
                _ => Step::ReopenPeer {
                    cache_entries: if next() & 1 == 0 { 0 } else { 16 },
                },
            });
        }
        Ok(Self { seed, steps })
    }

    fn encode(&self) -> String {
        let mut encoded = format!("{VERSION}\nseed {}\n", self.seed);
        for step in &self.steps {
            let line = match step {
                Step::Put { slot, value } => format!("put {slot} {value}\n"),
                Step::InterruptUpload => "interrupt-upload\n".into(),
                Step::PublishIndex => "publish-index\n".into(),
                Step::ReopenPeer { cache_entries } => format!("reopen-peer {cache_entries}\n"),
                Step::RestartWriter => "restart-writer\n".into(),
                Step::RetryLast => "retry-last\n".into(),
                Step::ResumeConsumer => "resume-consumer\n".into(),
            };
            encoded.push_str(&line);
        }
        encoded
    }

    fn decode(encoded: &str) -> Result<Self, String> {
        if encoded.len() as u64 > MAX_TRACE_BYTES {
            return Err("trace exceeds its byte limit".into());
        }
        let mut lines = encoded.lines();
        let version = lines.next();
        if ![Some(VERSION), Some(LEGACY_VERSION)].contains(&version) {
            return Err("unsupported storage fault trace version".into());
        }
        let seed = number(
            lines
                .next()
                .and_then(|line| line.strip_prefix("seed "))
                .ok_or("missing seed")?,
        )?;
        if seed == 0 {
            return Err("seed must be nonzero".into());
        }
        let mut steps = Vec::new();
        for line in lines {
            if steps.len() == MAX_STEPS {
                return Err("trace exceeds its action limit".into());
            }
            let fields: Vec<_> = line.split_whitespace().collect();
            let step = match fields.as_slice() {
                ["put", slot, value] => {
                    let slot = number(slot)?;
                    let value: i64 = value.parse().map_err(|_| "invalid synthetic value")?;
                    if slot >= 4 || !(-1000..=1000).contains(&value) {
                        return Err("synthetic slot/value is out of range".into());
                    }
                    Step::Put {
                        slot: slot as usize,
                        value,
                    }
                }
                ["interrupt-upload"] => Step::InterruptUpload,
                ["publish-index"] => Step::PublishIndex,
                ["restart-writer"] if version == Some(VERSION) => Step::RestartWriter,
                ["retry-last"] if version == Some(VERSION) => Step::RetryLast,
                ["resume-consumer"] if version == Some(VERSION) => Step::ResumeConsumer,
                ["reopen-peer", entries] => {
                    let entries = number(entries)?;
                    if ![0, 16].contains(&entries) {
                        return Err("unsupported fixture cache size".into());
                    }
                    Step::ReopenPeer {
                        cache_entries: entries as usize,
                    }
                }
                _ => return Err("unknown storage fault action".into()),
            };
            steps.push(step);
        }
        // Reduced traces are explicit subsequences, not regenerated choices.
        // The origin seed is retained but does not assert generator equality.
        Ok(Self { seed, steps })
    }

    fn from_options(
        seed: Option<&str>,
        steps: Option<&str>,
        replay: Option<&Path>,
    ) -> Result<Self, String> {
        if let Some(path) = replay {
            if seed.is_some() || steps.is_some() {
                return Err("replay cannot override seed or steps".into());
            }
            let metadata =
                std::fs::symlink_metadata(path).map_err(|_| "cannot inspect replay trace")?;
            if !metadata.is_file() || metadata.file_type().is_symlink() {
                return Err("replay trace must be a regular non-symlink file".into());
            }
            let mut options = OpenOptions::new();
            options.read(true);
            #[cfg(unix)]
            {
                use std::os::unix::fs::OpenOptionsExt;
                options.custom_flags(libc::O_NOFOLLOW | libc::O_NONBLOCK);
            }
            let mut encoded = String::new();
            options
                .open(path)
                .map_err(|_| "cannot open replay trace")?
                .take(MAX_TRACE_BYTES + 1)
                .read_to_string(&mut encoded)
                .map_err(|_| "cannot read replay trace")?;
            Self::decode(&encoded)
        } else {
            let seed = seed.map(number).transpose()?.unwrap_or(DEFAULT_SEED);
            let steps = steps
                .map(number)
                .transpose()?
                .unwrap_or(DEFAULT_STEPS as u64);
            Self::generate(
                seed,
                usize::try_from(steps).map_err(|_| "steps do not fit this platform")?,
            )
        }
    }

    /// New, private, absolute artifacts containing synthetic choices only.
    fn save(&self, output: Option<&Path>, reduced: bool) -> Result<PathBuf, String> {
        let (mut file, path) = if let Some(path) = output {
            if !path.is_absolute() || path.file_name().is_none() {
                return Err("trace output must be an absolute filename".into());
            }
            let mut options = OpenOptions::new();
            options.write(true).create_new(true);
            #[cfg(unix)]
            {
                use std::os::unix::fs::OpenOptionsExt;
                options.mode(0o600).custom_flags(libc::O_NOFOLLOW);
            }
            (
                options
                    .open(path)
                    .map_err(|_| "trace target must be new and its parent must exist")?,
                path.to_owned(),
            )
        } else {
            tempfile::Builder::new()
                .prefix(if reduced {
                    "atomic-storage-fault-reduced-"
                } else {
                    "atomic-storage-fault-"
                })
                .suffix(".trace")
                .tempfile()
                .map_err(|_| "cannot create private trace")?
                .keep()
                .map_err(|_| "cannot retain private trace")?
        };
        file.write_all(self.encode().as_bytes())
            .and_then(|()| file.sync_all())
            .map_err(|_| "cannot persist trace")?;
        Ok(path)
    }
}

fn number(value: &str) -> Result<u64, String> {
    if let Some(hex) = value.strip_prefix("0x") {
        u64::from_str_radix(hex, 16)
    } else {
        value.parse()
    }
    .map_err(|_| "expected a decimal or hexadecimal integer".into())
}

/// Standard deletion-based ddmin. It returns a 1-minimal action sequence for
/// the supplied failure predicate, not a globally smallest program or values.
fn reduce(mut trace: Trace, mut still_fails: impl FnMut(&Trace) -> bool) -> (Trace, usize) {
    let mut granularity = 2;
    let mut attempts = 0;
    while !trace.steps.is_empty() {
        let chunk = trace.steps.len().div_ceil(granularity);
        let mut reduced = false;
        for start in (0..trace.steps.len()).step_by(chunk) {
            let end = (start + chunk).min(trace.steps.len());
            let candidate = Trace {
                seed: trace.seed,
                steps: trace.steps[..start]
                    .iter()
                    .chain(&trace.steps[end..])
                    .cloned()
                    .collect(),
            };
            attempts += 1;
            if still_fails(&candidate) {
                trace = candidate;
                granularity = granularity.saturating_sub(1).max(2);
                reduced = true;
                break;
            }
        }
        if !reduced {
            if granularity >= trace.steps.len() {
                break;
            }
            granularity = (granularity * 2).min(trace.steps.len());
        }
    }
    (trace, attempts)
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum PostgresContext {
    Connection,
    PublicationRead,
}

#[derive(Clone, Debug, Eq, PartialEq)]
enum Failure {
    Storage {
        category: ErrorCategory,
        code: &'static str,
    },
    Postgres {
        context: PostgresContext,
        sqlstate: Option<String>,
    },
    Information,
    ConsumerData,
    ConsumerReplay,
    ConsumerCheckpoint,
    PublicationChanged,
    UnexpectedFault,
    UnexpectedPanic,
    ControlledFixture,
}

impl Failure {
    // Reduction compares stable signatures, never potentially sensitive or
    // nondeterministic backend messages, detail maps, or panic payloads.
    fn storage(error: SemanticError) -> Self {
        Self::Storage {
            category: error.category,
            code: error.code,
        }
    }

    fn postgres(context: PostgresContext, error: postgres::Error) -> Self {
        Self::Postgres {
            context,
            sqlstate: error.code().map(|code| code.code().to_owned()),
        }
    }
}

#[derive(Clone, Copy, Debug, Default)]
struct ReplayCounts {
    writes: usize,
    interrupted_uploads: usize,
    peer_reopens: usize,
    writer_restarts: usize,
    exact_retries: usize,
    consumer_resumes: usize,
    consumer_events: usize,
}

fn exact(value: &DatabaseValue, expected: &Database) -> Result<(), Failure> {
    catch_unwind(AssertUnwindSafe(|| {
        common::assert_same_information(value, expected)
    }))
    .map_err(|_| Failure::Information)
}

fn publication(client: &mut postgres::Client, database: &str) -> Result<(i64, Vec<u8>), Failure> {
    let row = client.query_one("SELECT publication_revision,manifest_hash FROM atomic_tree_publications WHERE database_id=$1 ORDER BY publication_revision DESC LIMIT 1", &[&database]).map_err(|error| Failure::postgres(PostgresContext::PublicationRead, error))?;
    Ok((row.get(0), row.get(1)))
}

fn replay(
    connection: &str,
    trace: &Trace,
    controlled_fixture: bool,
) -> Result<ReplayCounts, Failure> {
    catch_unwind(AssertUnwindSafe(|| {
        replay_inner(connection, trace, controlled_fixture)
    }))
    .unwrap_or(Err(Failure::UnexpectedPanic))
}

fn replay_inner(
    connection: &str,
    trace: &Trace,
    controlled_fixture: bool,
) -> Result<ReplayCounts, Failure> {
    let fixture = common::PostgresFixture::new(connection, "storage_fault");
    let connection = &fixture.connection;
    let database = format!("storage-fault-{}", fixture.schema);
    PostgresMigrator::connect(connection)
        .map_err(Failure::storage)?
        .migrate()
        .map_err(Failure::storage)?;
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            VALUE,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .map_err(Failure::storage)?;
    let mut expected = PostgresStore::connect(connection)
        .map_err(Failure::storage)?
        .create_database(&database, schema)
        .map_err(Failure::storage)?;
    // Start every full or reduced trace from an already completed baseline,
    // so a fault requires work introduced by the explicit retained actions.
    PostgresIndexer::connect(connection, &database)
        .map_err(Failure::storage)?
        .consolidate()
        .map_err(Failure::storage)?;
    let mut peer = Peer::connect(connection, &database, 16).map_err(Failure::storage)?;
    let mut retained = vec![(peer.database_value(), expected.clone())];
    let mut writer: Option<TransactionService> = None;
    let mut entities = BTreeMap::new();
    let mut control = postgres::Client::connect(connection, postgres::NoTls)
        .map_err(|error| Failure::postgres(PostgresContext::Connection, error))?;
    let mut counts = ReplayCounts::default();
    let mut last_receipt: Option<(TransactionRequest, ServiceTransactionReport)> = None;
    let mut expected_events = BTreeMap::new();
    // Nonempty create_database(schema) records schema as ordinary t=1 data,
    // not genesis. A log consumer must see it, including in reduced traces
    // whose only action is opening that consumer.
    if expected.basis_t() == 1 {
        expected_events.insert(
            1,
            expected
                .datoms(atomic_core::View::History, atomic_core::IndexOrder::Eavt)
                .into_iter()
                .filter(|datom| datom.tx == atomic_core::t_to_tx(1).unwrap())
                .collect(),
        );
    }
    for (ordinal, step) in trace.steps.iter().enumerate() {
        match *step {
            Step::Put { slot, value } => {
                let entity = entities
                    .get(&slot)
                    .copied()
                    .map(EntityRef::Id)
                    .unwrap_or_else(|| EntityRef::Temp(format!("slot-{slot}")));
                let operations = vec![TxOp::Add {
                    entity,
                    attribute: VALUE,
                    value: Value::Long(value).into(),
                }];
                let instant = 1_000 + counts.writes as i64;
                let pure = expected
                    .with(&operations, instant)
                    .map_err(Failure::storage)?;
                let service =
                    writer.get_or_insert_with(|| common::start_service(connection, &database));
                let request = TransactionRequest::new(format!("trace-write-{ordinal}"), operations)
                    .comparing_basis(expected.basis_t())
                    .with_tx_instant(instant);
                let report = service
                    .client()
                    .transact(request.clone(), Duration::from_secs(5))
                    .map_err(Failure::storage)?;
                if report.tempids != pure.tempids {
                    return Err(Failure::Information);
                }
                if let Some(entity) = pure.tempids.get(&format!("slot-{slot}")) {
                    entities.insert(slot, *entity);
                }
                exact(&report.db_before, &expected)?;
                exact(&report.db_after, &pure.db_after)?;
                expected_events.insert(report.basis_t, report.tx_data.clone());
                last_receipt = Some((request, report));
                expected = pure.db_after;
                counts.writes += 1;
                let value = peer.sync().map_err(Failure::storage)?;
                exact(&value, &expected)?;
                if retained.len() == 1 {
                    retained.push((value, expected.clone()));
                }
            }
            Step::InterruptUpload | Step::PublishIndex => {
                // Stop automatic indexing before fault injection. Otherwise
                // an independent successful publication could race this check.
                if let Some(service) = writer.take() {
                    service.shutdown();
                }
                let before = publication(&mut control, &database)?;
                let mut indexer = PostgresIndexer::connect(connection, &database)
                    .map_err(Failure::storage)?
                    .with_segment_datoms(8)
                    .map_err(Failure::storage)?;
                if matches!(step, Step::InterruptUpload) {
                    match indexer.consolidate_with_fault(IndexBuildFault::AfterSegments) {
                        Err(error) if error.code == "index/injected-failure" => {
                            counts.interrupted_uploads += 1;
                            if publication(&mut control, &database)? != before {
                                return Err(Failure::PublicationChanged);
                            }
                        }
                        Ok(receipt) if receipt.reused => {}
                        Err(error) => return Err(Failure::storage(error)),
                        Ok(_) => return Err(Failure::UnexpectedFault),
                    }
                } else {
                    indexer.consolidate().map_err(Failure::storage)?;
                }
                let reopened = Peer::connect(connection, &database, 0).map_err(Failure::storage)?;
                exact(&reopened.database_value(), &expected)?;
                for (value, expected) in &retained {
                    exact(value, expected)?;
                }
            }
            Step::ReopenPeer { cache_entries } => {
                drop(peer);
                peer = Peer::connect(connection, &database, cache_entries)
                    .map_err(Failure::storage)?;
                exact(&peer.database_value(), &expected)?;
                for (value, expected) in &retained {
                    exact(value, expected)?;
                }
                counts.peer_reopens += 1;
                if controlled_fixture && counts.interrupted_uploads > 0 {
                    return Err(Failure::ControlledFixture);
                }
            }
            Step::RestartWriter => {
                if let Some(service) = writer.take() {
                    service.shutdown();
                }
                writer = Some(common::start_service(connection, &database));
                exact(&peer.sync().map_err(Failure::storage)?, &expected)?;
                counts.writer_restarts += 1;
            }
            Step::RetryLast => {
                // Removing the preceding write during reduction makes retry
                // a no-op, not a fabricated request with different meaning.
                if let Some((request, original)) = &last_receipt {
                    let service =
                        writer.get_or_insert_with(|| common::start_service(connection, &database));
                    let replay = service
                        .client()
                        .transact(request.clone(), Duration::from_secs(5))
                        .map_err(Failure::storage)?;
                    if !replay.replayed
                        || replay.basis_t != original.basis_t
                        || replay.tx_hash != original.tx_hash
                        || replay.tx_data != original.tx_data
                        || replay.tempids != original.tempids
                    {
                        return Err(Failure::Information);
                    }
                    // Exact old receipt, not merely equivalent current facts.
                    catch_unwind(AssertUnwindSafe(|| {
                        common::assert_same_information(&replay.db_before, &original.db_before);
                        common::assert_same_information(&replay.db_after, &original.db_after);
                    }))
                    .map_err(|_| Failure::Information)?;
                    exact(&peer.sync().map_err(Failure::storage)?, &expected)?;
                    counts.exact_retries += 1;
                }
            }
            Step::ResumeConsumer => {
                let open = || {
                    ChangeConsumer::connect(
                        connection,
                        &database,
                        "trace-consumer",
                        ChangeConsumerConfig::default(),
                    )
                    .map_err(Failure::storage)
                };
                let mut consumer = open()?;
                let before = consumer.checkpoint().last_t();
                let mut restarted_unacknowledged = false;
                while let Some(mut event) =
                    consumer.next(Duration::ZERO).map_err(Failure::storage)?
                {
                    if expected_events.get(&event.transaction.t) != Some(&event.transaction.data) {
                        return Err(Failure::ConsumerData);
                    }
                    if !restarted_unacknowledged {
                        drop(consumer);
                        consumer = open()?;
                        let replay = consumer
                            .next(Duration::ZERO)
                            .map_err(Failure::storage)?
                            .ok_or(Failure::ConsumerReplay)?;
                        if replay != event {
                            return Err(Failure::ConsumerReplay);
                        }
                        event = replay;
                        restarted_unacknowledged = true;
                    }
                    consumer
                        .acknowledge(&event.checkpoint)
                        .map_err(Failure::storage)?;
                    counts.consumer_events += 1;
                }
                if consumer.checkpoint().last_t() != expected.basis_t()
                    || before > expected.basis_t()
                {
                    return Err(Failure::ConsumerCheckpoint);
                }
                counts.consumer_resumes += 1;
            }
        }
        if peer.load_stats().compatibility_materializations != 0 {
            return Err(Failure::Information);
        }
    }
    if let Some(service) = writer.take() {
        service.shutdown();
    }
    PostgresIndexer::connect(connection, &database)
        .map_err(Failure::storage)?
        .consolidate()
        .map_err(Failure::storage)?;
    drop(peer);
    let reopened = Peer::connect(connection, &database, 0).map_err(Failure::storage)?;
    exact(&reopened.database_value(), &expected)?;
    for (value, expected) in &retained {
        exact(value, expected)?;
    }
    Ok(counts)
}

#[test]
fn generated_storage_fault_schedule_replays_real_postgres_and_exact_values() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is required for storage fault replay");
        return;
    };
    let seed = std::env::var("ATOMIC_STORAGE_FAULT_SEED").ok();
    let steps = std::env::var("ATOMIC_STORAGE_FAULT_STEPS").ok();
    let replay_path = std::env::var_os("ATOMIC_STORAGE_FAULT_REPLAY").map(PathBuf::from);
    let output = std::env::var_os("ATOMIC_STORAGE_FAULT_TRACE").map(PathBuf::from);
    let trace =
        Trace::from_options(seed.as_deref(), steps.as_deref(), replay_path.as_deref()).unwrap();
    let path = trace.save(output.as_deref(), false).unwrap();
    eprintln!(
        "storage fault trace: seed=0x{:016x} steps={} path={path:?}; replay with ATOMIC_STORAGE_FAULT_REPLAY; fault=injected-after-upload-before-root, not process kill",
        trace.seed,
        trace.steps.len()
    );
    match replay(&connection, &trace, false) {
        Ok(counts) => {
            if replay_path.is_none() {
                assert!(counts.interrupted_uploads > 0 && counts.peer_reopens > 0);
                if trace.steps.len() >= 9 {
                    assert!(
                        counts.writer_restarts > 0
                            && counts.exact_retries >= 2
                            && counts.consumer_resumes >= 2
                            && counts.consumer_events >= 2
                    );
                }
            }
            eprintln!(
                "storage fault replay passed: {counts:?}; exact current/history and retained old values"
            );
        }
        Err(failure) => {
            let (reduced, attempts) = reduce(trace, |candidate| {
                replay(&connection, candidate, false).err().as_ref() == Some(&failure)
            });
            let output = std::env::var_os("ATOMIC_STORAGE_FAULT_REDUCED_TRACE").map(PathBuf::from);
            let reduced_path = reduced.save(output.as_deref(), true).unwrap();
            panic!(
                "storage replay failure={failure:?}; original={path:?} reduced={reduced_path:?} actions={} reduction_replays={attempts}",
                reduced.steps.len()
            );
        }
    }
}

#[test]
fn controlled_storage_fault_fixture_is_automatically_reduced_on_real_postgres() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP: ATOMIC_POSTGRES_URL is required for storage fault reduction");
        return;
    };
    let trace = Trace::generate(DEFAULT_SEED, 9).unwrap();
    let original = trace.save(None, false).unwrap();
    assert_eq!(
        replay(&connection, &trace, true).err(),
        Some(Failure::ControlledFixture)
    );
    let (reduced, attempts) = reduce(trace.clone(), |candidate| {
        replay(&connection, candidate, true).err() == Some(Failure::ControlledFixture)
    });
    let output = std::env::var_os("ATOMIC_STORAGE_FAULT_REDUCED_TRACE").map(PathBuf::from);
    let reduced_path = reduced.save(output.as_deref(), true).unwrap();
    assert_eq!(
        reduced.steps.len(),
        3,
        "minimal dependency is write, interrupted upload, peer reopen"
    );
    assert!(attempts > 0 && reduced.steps.len() < trace.steps.len());
    assert_eq!(
        Trace::decode(&std::fs::read_to_string(&reduced_path).unwrap()).unwrap(),
        reduced
    );
    assert_eq!(
        replay(&connection, &reduced, true).err(),
        Some(Failure::ControlledFixture)
    );
    assert!(
        replay(&connection, &reduced, false).is_ok(),
        "the reduced controlled fixture must pass production invariants"
    );
    eprintln!(
        "CONTROLLED REDUCER WITNESS ONLY: original={original:?} reduced={reduced_path:?} actions=9->3 reduction_replays={attempts}; no production defect claimed"
    );
}

#[test]
fn reduction_preserves_stable_failure_signatures_without_messages() {
    let signature = |category, code, message| {
        Failure::storage(SemanticError::new(category, code, message).detail("secret", message))
    };
    let target = signature(
        ErrorCategory::Unavailable,
        "storage/read",
        "private-message",
    );
    assert_eq!(
        target,
        signature(ErrorCategory::Unavailable, "storage/read", "other-message")
    );
    assert!(!format!("{target:?}").contains("private-message"));
    let other_code = signature(
        ErrorCategory::Unavailable,
        "storage/connect",
        "private-message",
    );
    let other_category = signature(ErrorCategory::Fault, "storage/read", "private-message");
    assert_ne!(target, other_code);
    assert_ne!(target, other_category);
    let connection = Failure::Postgres {
        context: PostgresContext::Connection,
        sqlstate: Some("08006".into()),
    };
    assert_ne!(
        connection,
        Failure::Postgres {
            context: PostgresContext::PublicationRead,
            sqlstate: Some("08006".into()),
        }
    );
    assert_ne!(
        connection,
        Failure::Postgres {
            context: PostgresContext::Connection,
            sqlstate: None,
        }
    );

    // A deletion may expose a different storage failure. It must not replace
    // the target, even though all candidates below fail in some way.
    let required = vec![Step::Put { slot: 0, value: 1 }, Step::InterruptUpload];
    let mut steps = required.clone();
    steps.push(Step::ReopenPeer { cache_entries: 0 });
    let (reduced, _) = reduce(Trace { seed: 1, steps }, |candidate| {
        let has_write = candidate.steps.contains(&required[0]);
        let has_interrupt = candidate.steps.contains(&required[1]);
        let failure = match (has_write, has_interrupt) {
            (true, true) => &target,
            (true, false) => &other_category,
            (false, true) => &other_code,
            (false, false) => &connection,
        };
        failure == &target
    });
    assert_eq!(reduced.steps, required);
}

#[test]
fn storage_fault_trace_roundtrips_and_rejects_unsafe_inputs_or_overwrite() {
    for seed in [1, 42, DEFAULT_SEED] {
        let trace = Trace::generate(seed, 12).unwrap();
        assert_eq!(Trace::decode(&trace.encode()).unwrap(), trace);
    }
    assert!(Trace::generate(0, 3).is_err());
    let old = format!("{LEGACY_VERSION}\nseed 42\nput 0 1\ninterrupt-upload\nreopen-peer 16\n");
    assert_eq!(
        Trace::decode(&old).unwrap().steps,
        vec![
            Step::Put { slot: 0, value: 1 },
            Step::InterruptUpload,
            Step::ReopenPeer { cache_entries: 16 }
        ]
    );
    assert!(Trace::decode(&format!("{LEGACY_VERSION}\nseed 42\nretry-last\n")).is_err());
    assert!(Trace::generate(1, MAX_STEPS + 1).is_err());
    assert!(Trace::decode(&format!("{VERSION}\nseed 1\nput 4 0\n")).is_err());
    let directory = tempfile::tempdir().unwrap();
    let path = directory.path().join("trace.txt");
    let trace = Trace::generate(42, 6).unwrap();
    trace.save(Some(&path), false).unwrap();
    assert!(trace.save(Some(&path), false).is_err());
    assert!(
        trace
            .save(Some(Path::new("relative.trace")), false)
            .is_err()
    );
    assert_eq!(Trace::from_options(None, None, Some(&path)).unwrap(), trace);
    assert!(Trace::from_options(Some("42"), None, Some(&path)).is_err());
    #[cfg(unix)]
    {
        use std::os::unix::fs::{PermissionsExt, symlink};
        assert_eq!(
            std::fs::metadata(&path).unwrap().permissions().mode() & 0o777,
            0o600
        );
        let alias = directory.path().join("alias");
        symlink(&path, &alias).unwrap();
        assert!(Trace::from_options(None, None, Some(&alias)).is_err());
        assert!(trace.save(Some(&alias), false).is_err());
    }
    assert_eq!(std::fs::read_to_string(path).unwrap(), trace.encode());
}
