//! Independent backlog oracles and real service lifecycle/ordering regressions.
use super::config::{
    BackgroundIndexingConfig, CapacityLimits, ServiceOptions, TransactionServiceConfig,
};
use super::index_lane;
use super::indexing::{BackgroundIndexing, IndexCommand, IndexingSeed, Novelty};
use super::indexing::{IndexingBacklog, IndexingTotals, should_index};
use super::observations::ProjectionObservations;
use super::request::{IndexRequest, TransactionRequest};
use super::service::TransactionService;
use super::testing::{CommitObservationFault, arm_observation_fault};
use crate::{
    DurableTransaction, ErrorCategory, OperationContext, OperationKind, PostgresConnectionConfig,
    ProgramCall, SemanticError, TxForm, TxOp,
};
use std::collections::{BTreeMap, VecDeque};
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::{Arc, mpsc};
use std::thread;
use std::time::{Duration, Instant};

use crate::Datom;
use std::mem::size_of;

// RecentTier retains at most four raw BTSet entries per datom. Its
// allocator-independent conservative account reserves a fifth reference for
// persistent-log/tree overhead; keep admission on that same bound.
#[cfg(test)]
const MAX_RECENT_REFERENCES_PER_DATOM: u64 = 5;
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, Schema, TxValue, USER_PARTITION, Value, ValueType,
    make_eid,
};
use std::time::{SystemTime, UNIX_EPOCH};

const OBSERVED_ITEM_COUNT: u32 = 1_000;

fn postgres_connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

struct ObservationFixture {
    admin: postgres::Client,
    schema: String,
    connection: String,
    database: crate::storage::BlockDatabase,
}

impl Drop for ObservationFixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

fn observation_fixture() -> Option<ObservationFixture> {
    let connection = postgres_connection()?;
    let schema = format!("block_service_observe_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let connection =
        if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
            format!(
                "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                if connection.contains('?') { '&' } else { '?' }
            )
        } else {
            format!("{connection} options='-csearch_path={schema},pg_catalog'")
        };
    let config = PostgresConnectionConfig::plaintext(&connection);
    crate::storage::PgBlockStore::install(&config).unwrap();
    let database =
        crate::storage::BlockDatabase::create(&config, "observed", observation_schema()).unwrap();
    Some(ObservationFixture {
        admin,
        schema,
        connection,
        database,
    })
}

fn unique_database(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .expect("system clock precedes Unix epoch")
            .as_nanos()
    )
}

fn observation_schema() -> Schema {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            OBSERVED_ITEM_COUNT,
            Keyword::new("item", "count"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn observation_request(key: &str, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
            attribute: OBSERVED_ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(value)),
        }],
    )
}

fn observation_service_config(connection: &str, database_id: String) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: PostgresConnectionConfig::parse(connection).unwrap(),
        database_id,
        holder_id: unique_database("unknown-observer"),
        lease_duration: Duration::from_secs(10),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 4,
        capacity_limits: CapacityLimits::default(),
    }
}

fn test_config() -> BackgroundIndexingConfig {
    BackgroundIndexingConfig {
        memory_index_threshold_bytes: 1_024,
        memory_index_max_bytes: 2_048,
    }
}

fn bookkeeping_seed(width: usize) -> IndexingSeed {
    IndexingSeed {
        published_revision: 7,
        published_basis_t: 1,
        pending_avet_projections: 0,
        newest_observed_revision: 7,
        target_basis_t: width as u64 + 1,
        pending: (0..width)
            .map(|offset| Novelty {
                basis_t: offset as u64 + 2,
                datoms: offset as u64 % 3 + 1,
                bytes: offset as u64 % 7 + 17,
            })
            .collect(),
        publication_work_through: None,
        required_publication_t: 0,
        needs_publication: false,
    }
}

fn bookkeeping_config() -> BackgroundIndexingConfig {
    BackgroundIndexingConfig {
        memory_index_threshold_bytes: u64::MAX - 1,
        memory_index_max_bytes: u64::MAX,
    }
}

fn assert_backlog_oracle(indexing: &BackgroundIndexing) {
    // This deliberately independent full scan is test-only and outside
    // measured operations. Never consult any maintained aggregate here.
    let (total, frozen, revision, basis, through) = {
        let backlog = indexing.backlog.lock().unwrap();
        let mut total = [0_u128; 3];
        let mut frozen = [0_u128; 3];
        for novelty in &backlog.pending {
            let counts = [1, u128::from(novelty.datoms), u128::from(novelty.bytes)];
            for column in 0..3 {
                total[column] += counts[column];
                if backlog
                    .indexing_through
                    .is_some_and(|through| novelty.basis_t <= through)
                {
                    frozen[column] += counts[column];
                }
            }
        }
        (
            total,
            frozen,
            backlog.published_revision,
            backlog.published_basis_t,
            backlog.indexing_through,
        )
    };
    let saturated = |value: u128| value.min(u128::from(u64::MAX)) as u64;
    let stats = indexing.stats();
    assert_eq!(
        [
            stats.total_transactions,
            stats.total_datoms,
            stats.total_bytes
        ],
        total.map(saturated)
    );
    assert_eq!(
        [
            stats.indexing_transactions,
            stats.indexing_datoms,
            stats.indexing_bytes
        ],
        frozen.map(saturated)
    );
    assert_eq!(
        [
            stats.memory_index_transactions,
            stats.memory_index_datoms,
            stats.memory_index_bytes
        ],
        std::array::from_fn(|column| saturated(total[column] - frozen[column]))
    );
    assert_eq!(stats.published_revision, revision);
    assert_eq!(indexing.published_revision(), revision);
    assert_eq!(stats.published_basis_t, basis);
    assert_eq!(stats.job_in_flight, through.is_some());
    assert_eq!(stats.last_failure.is_some(), indexing.has_failure());
}

#[test]
fn backlog_aggregates_match_oracle_across_failure_retry_and_partial_adoption() {
    for width in [1, 32, 512, 8_192] {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing =
            BackgroundIndexing::new(bookkeeping_config(), bookkeeping_seed(width), sender);
        assert_backlog_oracle(&indexing);
        let target = width as u64 + 1;
        assert_eq!(indexing.request_index().target_t, target);
        assert_eq!(indexing.begin_job(), Some(target));
        assert_eq!(indexing.begin_job(), None);
        assert_backlog_oracle(&indexing);
        let frozen = indexing.stats();
        assert_eq!(frozen.indexing_transactions, width as u64);
        assert_eq!(frozen.memory_index_transactions, 0);

        indexing.note_commit(
            Novelty {
                basis_t: target,
                datoms: 100,
                bytes: 999,
            },
            true,
        );
        assert_eq!(
            indexing.stats(),
            frozen,
            "duplicate receipt changed accounting"
        );
        indexing.note_commit(
            Novelty {
                basis_t: target + 1,
                datoms: 3,
                bytes: 71,
            },
            true,
        );
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().memory_index_transactions, 1);

        let partial = 1 + width as u64 / 2;
        indexing.complete_job(8, partial, 1, true);
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().pending_avet_projections, 1);
        assert_eq!(indexing.begin_job(), Some(partial));
        assert_eq!(
            indexing.stats().indexing_transactions,
            0,
            "maintenance froze an unpublished suffix"
        );
        indexing.note_commit(
            Novelty {
                basis_t: target + 2,
                datoms: 2,
                bytes: 41,
            },
            false,
        );
        assert_backlog_oracle(&indexing);
        indexing.complete_job(9, partial, 0, false);
        assert_backlog_oracle(&indexing);
        assert!(
            indexing.should_continue(),
            "older maintenance lost new publication demand"
        );

        assert_eq!(indexing.begin_job(), Some(target + 2));
        let before_retry = indexing.stats();
        indexing.retry_job();
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.begin_job(), Some(target + 2));
        let after_retry = indexing.stats();
        assert_eq!(
            (
                after_retry.indexing_transactions,
                after_retry.indexing_datoms,
                after_retry.indexing_bytes
            ),
            (
                before_retry.indexing_transactions,
                before_retry.indexing_datoms,
                before_retry.indexing_bytes
            ),
            "whole-job retry lost frozen novelty"
        );
        assert_eq!(after_retry.total_bytes, before_retry.total_bytes);
        assert_eq!(after_retry.jobs_started, before_retry.jobs_started + 1);
        indexing.fail_job(SemanticError::new(
            ErrorCategory::Fault,
            "index/test-failure",
            "terminal failure",
        ));
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().indexing_transactions, 0);
        assert_eq!(indexing.stats().jobs_failed, 1);
        assert!(indexing.has_failure());
        assert_eq!(
            indexing.limiting_error().unwrap().code,
            "service/indexing-failed"
        );

        // Fatal indexing failure still closes the writer. Reconstructing
        // from authenticated startup state, not a stats read, clears it.
        let seed = {
            let backlog = indexing.backlog.lock().unwrap();
            IndexingSeed {
                published_revision: backlog.published_revision,
                published_basis_t: backlog.published_basis_t,
                pending_avet_projections: backlog.pending_avet_projections,
                newest_observed_revision: backlog.newest_observed_revision,
                target_basis_t: backlog.target_basis_t,
                pending: backlog.pending.clone(),
                publication_work_through: backlog.publication_work_through,
                required_publication_t: backlog.required_publication_t,
                needs_publication: backlog.needs_publication,
            }
        };
        let (sender, _receiver) = mpsc::sync_channel(1);
        let restarted = BackgroundIndexing::new(bookkeeping_config(), seed, sender);
        assert!(!restarted.has_failure());
        assert_eq!(restarted.begin_job(), Some(target + 2));
        assert_backlog_oracle(&restarted);
        restarted.complete_job(10, target + 2, 0, false);
        assert_backlog_oracle(&restarted);
        assert_eq!(restarted.stats().total_transactions, 0);
        assert!(!restarted.should_continue());
        restarted.complete_job(9, partial, 0, false);
        assert_eq!(
            restarted.published_revision(),
            10,
            "adoption revision regressed"
        );
        assert_backlog_oracle(&restarted);
    }
}

#[test]
fn saturated_public_backlog_counts_recover_exact_suffix_after_adoption() {
    let (sender, _receiver) = mpsc::sync_channel(1);
    let mut seed = bookkeeping_seed(2);
    seed.pending[0].datoms = u64::MAX;
    seed.pending[0].bytes = u64::MAX;
    seed.pending[1].datoms = 7;
    seed.pending[1].bytes = 7;
    let indexing = BackgroundIndexing::new(bookkeeping_config(), seed, sender);
    assert_backlog_oracle(&indexing);
    assert_eq!(indexing.stats().total_datoms, u64::MAX);
    assert!(indexing.at_hard_limit());
    assert_eq!(indexing.begin_job(), Some(3));
    indexing.note_commit(
        Novelty {
            basis_t: 4,
            datoms: 9,
            bytes: 9,
        },
        false,
    );
    assert_backlog_oracle(&indexing);
    assert_eq!(indexing.stats().memory_index_datoms, 9);
    indexing.complete_job(8, 2, 0, false);
    assert_backlog_oracle(&indexing);
    assert_eq!(indexing.stats().total_datoms, 16);
    assert_eq!(indexing.stats().total_bytes, 16);
    assert!(!indexing.at_hard_limit());
}

#[test]
fn fixed_append_revision_availability_and_status_work_does_not_scan_backlog() {
    use std::hint::black_box;
    const APPENDS: u64 = 256;
    for width in [32, 128, 512, 2_048, 8_192, 32_768] {
        let (sender, _receiver) = mpsc::sync_channel(1);
        let indexing =
            BackgroundIndexing::new(bookkeeping_config(), bookkeeping_seed(width), sender);
        indexing.request_index();
        let through = indexing.begin_job().unwrap();
        assert_backlog_oracle(&indexing);
        let started = Instant::now();
        for offset in 1..=APPENDS {
            indexing.note_commit(
                Novelty {
                    basis_t: through + offset,
                    datoms: 1,
                    bytes: 64,
                },
                false,
            );
            black_box(indexing.published_revision());
            black_box(indexing.has_failure());
            black_box(indexing.at_hard_limit());
            black_box(indexing.limiting_error());
            // Include the complete public aggregate snapshot and drop,
            // not only insertion or the dedicated revision getter.
            black_box(indexing.stats());
        }
        let append_elapsed = started.elapsed();
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().indexing_transactions, width as u64);
        assert_eq!(indexing.stats().memory_index_transactions, APPENDS);
        let adoption_started = Instant::now();
        indexing.complete_job(8, through, 0, false);
        let adoption_elapsed = adoption_started.elapsed();
        assert_backlog_oracle(&indexing);
        assert_eq!(indexing.stats().total_transactions, APPENDS);
        eprintln!(
            "SERVICE_BOOKKEEPING retained={width} fixed_appends={APPENDS} append_revision_availability_stats_drop_ns_per_op={} adoption_removed={width} adoption_us={}",
            append_elapsed.as_nanos() / u128::from(APPENDS),
            adoption_elapsed.as_micros()
        );
    }
}

#[test]
fn projection_observations_track_attempt_retry_and_adoption_only() {
    let now = Instant::now();
    let error = SemanticError::conflict("storage/write-protection-conflict", "GC guard changed");
    let mut observation = ProjectionObservations::default();
    observation.started(3);
    observation.failed(&error, Some(now + Duration::from_millis(50)), false);
    let failed = observation.snapshot_with_messages(now, true);
    assert_eq!((failed.attempts, failed.failures), (1, 1));
    assert_eq!(failed.attempted_basis_t, Some(3));
    assert_eq!(failed.checked_basis_t, None);
    assert_eq!(failed.retry_in, Some(Duration::from_millis(50)));
    assert_eq!(failed.last_failure.unwrap().code, error.code);
    assert!(!failed.retry_exhausted);

    observation.started(3);
    let retrying = observation.snapshot_with_messages(now, false);
    assert_eq!(retrying.attempts, 2);
    assert_eq!(retrying.checked_basis_t, None);
    assert_eq!(retrying.retry_in, None);
    assert!(retrying.last_failure.unwrap().message.is_empty());
    observation.adopted(3);
    let recovered = observation.snapshot_with_messages(now, true);
    assert_eq!(recovered.checked_basis_t, Some(3));
    assert_eq!(recovered.failures, 1);
    assert!(recovered.last_failure.is_none());

    observation.started(4);
    observation.failed(&error, None, true);
    let exhausted = observation.snapshot_with_messages(now, true);
    assert_eq!(exhausted.checked_basis_t, Some(3));
    assert_eq!(exhausted.attempted_basis_t, Some(4));
    assert_eq!(exhausted.retry_in, None);
    assert!(exhausted.retry_exhausted);
    assert_eq!(exhausted.last_failure.unwrap().message, error.message);
}

#[test]
fn satisfied_index_request_does_not_schedule_projection_work() {
    let (sender, receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 1,
            pending: VecDeque::new(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    let error = SemanticError::conflict("test/projection", "attempt failed");
    indexing.projection_started(1);
    indexing.projection_failed(&error, None, false);
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 1,
            scheduled: false
        }
    );
    assert_eq!(receiver.try_recv(), Err(mpsc::TryRecvError::Empty));
    assert_eq!(indexing.stats().fulltext.retry_in, None);
}

#[test]
fn explicit_index_requests_coalesce_and_do_not_chase_later_commits() {
    let (sender, receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 1,
            pending: VecDeque::new(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 1,
            scheduled: false
        }
    );
    indexing.note_commit(
        Novelty {
            basis_t: 2,
            datoms: 1,
            bytes: 100,
        },
        false,
    );
    assert!(!indexing.should_continue());
    let first = indexing.request_index();
    assert_eq!(
        first,
        IndexRequest {
            target_t: 2,
            scheduled: true
        }
    );
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 2,
            scheduled: false
        }
    );
    assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
    assert_eq!(indexing.begin_job(), Some(2));
    indexing.note_commit(
        Novelty {
            basis_t: 3,
            datoms: 1,
            bytes: 100,
        },
        false,
    );
    indexing.complete_job(2, 2, 0, false);
    assert!(
        !indexing.should_continue(),
        "a finite request chased unrequested later novelty"
    );
    assert_eq!(first.target_t, 2);
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 3,
            scheduled: true
        }
    );
    assert_eq!(indexing.begin_job(), Some(3));
    indexing.note_commit(
        Novelty {
            basis_t: 4,
            datoms: 1,
            bytes: 100,
        },
        false,
    );
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 4,
            scheduled: true
        }
    );
    indexing.complete_job(3, 3, 0, false);
    assert!(
        indexing.should_continue(),
        "older completion lost a newer explicit request"
    );
    assert_eq!(indexing.begin_job(), Some(4));
    indexing.complete_job(4, 4, 0, false);
    assert!(!indexing.should_continue());
    assert_eq!(
        indexing.request_index(),
        IndexRequest {
            target_t: 4,
            scheduled: false
        }
    );
}

#[test]
fn basis_zero_bootstrap_is_publishable_and_first_threshold_crossing_advances_it() {
    let (sender, receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 0,
            published_basis_t: 0,
            pending_avet_projections: 0,
            newest_observed_revision: 0,
            target_basis_t: 0,
            pending: VecDeque::new(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: true,
        },
        sender,
    );

    assert!(indexing.should_continue());
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(1, 0, 0, false);
    assert!(!indexing.should_continue());
    indexing.note_commit(
        Novelty {
            basis_t: 1,
            datoms: 1,
            bytes: 2_048,
        },
        false,
    );
    assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
    assert!(indexing.should_continue());
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(2, 1, 0, false);
    assert!(!indexing.should_continue());
    assert_eq!(indexing.stats().published_basis_t, 1);
    assert_eq!(indexing.stats().published_revision, 2);
}

#[test]
fn corrupt_publication_at_head_forces_same_basis_repair() {
    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 0,
            published_basis_t: 0,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 1,
            pending: VecDeque::from([Novelty {
                basis_t: 1,
                datoms: 1,
                bytes: 1_024,
            }]),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: true,
        },
        sender,
    );

    assert!(indexing.should_continue());
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(2, 1, 0, false);
    let repaired = indexing.stats();
    assert_eq!(repaired.published_revision, 2);
    assert_eq!(repaired.newest_observed_revision, 2);
    assert_eq!(repaired.published_basis_t, 1);
    assert_eq!(repaired.target_basis_t, 1);
    assert_eq!(repaired.total_bytes, 0);
    assert!(!indexing.should_continue());
}

#[test]
fn index_thresholds_are_strict_crossings() {
    let config = test_config();
    let mut backlog = IndexingBacklog {
        published_revision: 1,
        published_basis_t: 1,
        pending_avet_projections: 0,
        newest_observed_revision: 1,
        target_basis_t: 2,
        pending: VecDeque::new(),
        total_datoms: 1,
        total_bytes: u128::from(config.memory_index_threshold_bytes),
        indexing_through: None,
        indexing_totals: IndexingTotals::default(),
        publication_work_through: None,
        required_publication_t: 0,
        needs_publication: false,
    };
    assert!(!should_index(&backlog, config));
    backlog.total_bytes += 1;
    assert!(should_index(&backlog, config));

    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        config,
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 2,
            pending: VecDeque::from([Novelty {
                basis_t: 2,
                datoms: 1,
                bytes: config.memory_index_max_bytes,
            }]),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    assert!(!indexing.at_hard_limit());
    assert!(indexing.limiting_error().is_none());
    {
        let mut backlog = indexing
            .backlog
            .lock()
            .expect("index backlog mutex poisoned");
        backlog.total_bytes += 1;
        backlog.pending.front_mut().unwrap().bytes += 1;
    }
    assert!(indexing.at_hard_limit());
    assert_eq!(
        indexing.limiting_error().unwrap().code,
        "service/index-backpressure"
    );
}

#[test]
fn exact_recent_hard_cap_can_force_a_below_threshold_publication() {
    let (sender, receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 2,
            pending: VecDeque::from([Novelty {
                basis_t: 2,
                datoms: 1,
                bytes: 1,
            }]),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );

    assert!(!indexing.should_continue());
    assert!(indexing.force_publication());
    assert_eq!(receiver.try_recv(), Ok(IndexCommand::Wake));
    assert!(indexing.should_continue());
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(2, 2, 0, false);
    assert_eq!(indexing.stats().total_bytes, 0);
    assert!(!indexing.should_continue());

    // With no durable tail to consolidate, repeating an intrinsically
    // oversized transaction must return its capacity error rather than
    // park forever on a publication that cannot make room.
    assert!(!indexing.force_publication());
}

#[test]
fn physical_progress_does_not_clear_pending_or_newer_schema_work() {
    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 1,
            pending: VecDeque::new(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    indexing.note_commit(
        Novelty {
            basis_t: 2,
            datoms: 1,
            bytes: 1,
        },
        true,
    );
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(2, 2, 1, true);
    let partial = indexing.stats();
    assert_eq!(partial.published_basis_t, 2);
    assert_eq!(
        partial.total_bytes, 0,
        "durable EAVT progress releases recent novelty"
    );
    assert_eq!(partial.pending_avet_projections, 1);
    assert!(indexing.should_continue());

    // A newer toggle racing the older same-basis chunks must survive the
    // older job's final completion signal.
    indexing.note_commit(
        Novelty {
            basis_t: 3,
            datoms: 1,
            bytes: 1,
        },
        true,
    );
    assert!(indexing.begin_job().is_some());
    indexing.complete_job(3, 2, 0, false);
    assert!(indexing.should_continue());

    assert!(indexing.begin_job().is_some());
    indexing.complete_job(4, 3, 0, false);
    let complete = indexing.stats();
    assert_eq!(complete.pending_avet_projections, 0);
    assert_eq!(complete.total_bytes, 0);
    assert!(!indexing.should_continue());
}

#[test]
fn publication_maintenance_does_not_turn_small_new_tails_into_demand() {
    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 2,
            pending: VecDeque::from([Novelty {
                basis_t: 2,
                datoms: 1,
                bytes: 1_025,
            }]),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    assert_eq!(indexing.begin_job(), Some(2));
    indexing.complete_job(2, 2, 0, true);
    indexing.note_commit(
        Novelty {
            basis_t: 3,
            datoms: 1,
            bytes: 100,
        },
        false,
    );
    assert_eq!(
        indexing.begin_job(),
        Some(2),
        "finish only the published value"
    );
    indexing.complete_job(2, 2, 0, false);
    assert_eq!(indexing.stats().total_bytes, 100);
    assert_eq!(
        indexing.begin_job(),
        None,
        "new novelty is below the threshold"
    );
    indexing.note_commit(
        Novelty {
            basis_t: 4,
            datoms: 1,
            bytes: 925,
        },
        false,
    );
    assert_eq!(indexing.begin_job(), Some(4));
}

#[test]
fn forced_demand_survives_an_older_publications_final_maintenance_receipt() {
    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        test_config(),
        IndexingSeed {
            published_revision: 2,
            published_basis_t: 2,
            pending_avet_projections: 0,
            newest_observed_revision: 2,
            target_basis_t: 2,
            pending: VecDeque::new(),
            publication_work_through: Some(2),
            required_publication_t: 0,
            needs_publication: true,
        },
        sender,
    );
    assert_eq!(indexing.begin_job(), Some(2));
    indexing.note_commit(
        Novelty {
            basis_t: 3,
            datoms: 1,
            bytes: 100,
        },
        false,
    );
    assert!(indexing.force_publication());
    indexing.complete_job(2, 2, 0, false);
    assert_eq!(
        indexing.begin_job(),
        Some(3),
        "the newer force remains parked"
    );
    indexing.complete_job(3, 3, 0, true);
    assert_eq!(indexing.begin_job(), Some(3));
    indexing.complete_job(3, 3, 0, false);
    assert_eq!(indexing.begin_job(), None);
}

#[test]
fn tuple_of_nils_cannot_evade_novelty_backpressure() {
    let datom = Datom {
        entity: 1,
        attribute: 2,
        value: Value::Tuple(vec![None; 1_024]),
        tx: crate::t_to_tx(2).unwrap(),
        added: true,
    };
    let transaction = DurableTransaction {
        database_id: "accounting-test".to_owned(),
        basis_t: 2,
        previous_hash: [0; 32],
        eidx_frontier: crate::INITIAL_EIDX_FRONTIER,
        tempids: BTreeMap::from([("large-envelope-tempid".repeat(32), 1)]),
        tx_data: vec![datom.clone()],
    };
    let canonical_value_bytes = crate::encoding::encode_canonical_value(&datom.value)
        .unwrap()
        .len() as u64;
    let locator_bytes = (size_of::<crate::index::recent::btset::RecentDatomRef>() as u64)
        .saturating_mul(MAX_RECENT_REFERENCES_PER_DATOM);
    let canonical_only_account = (size_of::<Datom>() as u64)
        .saturating_add(canonical_value_bytes)
        .saturating_add(locator_bytes);
    let retained = crate::index::recent::retained_entry_stats(&transaction).unwrap();
    let retained_account = retained.accounted_bytes;
    assert!(retained_account > canonical_only_account);

    let (sender, _receiver) = mpsc::sync_channel(1);
    let indexing = BackgroundIndexing::new(
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1,
            memory_index_max_bytes: canonical_only_account.saturating_add(1),
        },
        IndexingSeed {
            published_revision: 1,
            published_basis_t: 1,
            pending_avet_projections: 0,
            newest_observed_revision: 1,
            target_basis_t: 1,
            pending: VecDeque::new(),
            publication_work_through: None,
            required_publication_t: 0,
            needs_publication: false,
        },
        sender,
    );
    indexing.note_commit(
        Novelty {
            basis_t: 2,
            datoms: retained.datoms,
            bytes: retained.accounted_bytes,
        },
        false,
    );
    let limit = indexing
        .limiting_error()
        .expect("retained tuple slots must cross the hard byte limit");
    assert_eq!(limit.code, "service/index-backpressure");
    assert_eq!(indexing.stats().total_bytes, retained_account);
}

#[test]
fn unified_projection_retry_reports_adopted_search_basis() {
    let Some(fixture) = observation_fixture() else {
        eprintln!("SKIP unified projection retry: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    const TEXT: u32 = 1_001;
    let connection = PostgresConnectionConfig::plaintext(&fixture.connection);
    let mut schema = observation_schema();
    schema
        .install(
            Attribute::new(
                TEXT,
                Keyword::new("document", "body"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let database =
        crate::storage::BlockDatabase::create(&connection, "projection", schema).unwrap();
    let service = TransactionService::start_with_indexing(
        observation_service_config(&fixture.connection, "projection".into()),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let report = client
        .transact(
            TransactionRequest::new(
                "document",
                vec![TxOp::Add {
                    entity: EntityRef::Temp("document".into()),
                    attribute: TEXT,
                    value: Value::String("needle coherent retry".into()).into(),
                }],
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let identity = crate::storage::catalog::identity_string(database.identity);
    let (first_ready, first_release) = index_lane::pause_preparation(&identity);
    assert!(client.request_index().unwrap().scheduled);
    first_ready.recv_timeout(Duration::from_secs(10)).unwrap();
    let staged = client.background_indexing_stats();
    assert_eq!(staged.fulltext.attempts, 1);
    assert_eq!(staged.fulltext.attempted_basis_t, Some(report.basis_t));
    assert_eq!(
        staged.fulltext.checked_basis_t, None,
        "staging is not adoption"
    );
    assert_eq!(staged.fulltext.failures, 0);

    // This candidate really built the search attachment. Changing only its
    // generic GC guard makes the actual adoption fail without corrupting
    // objects or pretending the failure arose in the search builder.
    let (retry_ready, retry_release) = index_lane::pause_preparation(&identity);
    let mut store = crate::storage::PgBlockStore::connect(&connection).unwrap();
    let before_adoption = store
        .read_ref(&database.reference_key())
        .unwrap()
        .unwrap()
        .value;
    let gc_key = crate::storage::protection::GC_REFERENCE;
    let previous = store.read_ref(gc_key).unwrap();
    assert!(matches!(
        store
            .compare_exchange(
                gc_key,
                previous.as_ref().map(|r| r.revision),
                Some(&1_u64.to_be_bytes()),
            )
            .unwrap(),
        crate::storage::CasOutcome::Applied(_)
    ));
    first_release.send(()).unwrap();
    retry_ready.recv_timeout(Duration::from_secs(10)).unwrap();
    let retrying = client.background_indexing_stats();
    assert_eq!(
        (retrying.fulltext.attempts, retrying.fulltext.failures),
        (2, 1)
    );
    assert_eq!(retrying.fulltext.checked_basis_t, None);
    assert_eq!(
        retrying.fulltext.retry_in, None,
        "retry is running, not scheduled"
    );
    assert!(!retrying.fulltext.retry_exhausted);
    let failure = retrying.fulltext.last_failure.as_ref().unwrap();
    // Adoption checks the staged object's original guard before loading
    // provenance or writing its new publication root.
    assert_eq!(
        (failure.category, failure.code),
        (
            ErrorCategory::Conflict,
            "storage/index-publication-conflict",
        )
    );
    assert_eq!((retrying.jobs_completed, retrying.jobs_failed), (0, 0));
    assert_eq!(
        store
            .read_ref(&database.reference_key())
            .unwrap()
            .unwrap()
            .value,
        before_adoption,
        "a stale preparation must not publish"
    );
    assert!(retrying.last_failure.is_none());
    assert!(client.is_available());
    retry_release.send(()).unwrap();

    let deadline = Instant::now() + Duration::from_secs(10);
    let recovered = loop {
        let stats = client.background_indexing_stats();
        if stats.published_basis_t == report.basis_t && !stats.job_in_flight {
            break stats;
        }
        assert!(
            Instant::now() < deadline && client.is_available(),
            "{stats:?}"
        );
        thread::sleep(Duration::from_millis(5));
    };
    assert_eq!(recovered.fulltext.checked_basis_t, Some(report.basis_t));
    assert_eq!(
        (recovered.fulltext.attempts, recovered.fulltext.failures),
        (2, 1)
    );
    assert!(recovered.fulltext.last_failure.is_none());
    assert!(recovered.fulltext.retry_in.is_none());
    assert!(!recovered.fulltext.retry_exhausted);
    assert_eq!((recovered.jobs_completed, recovered.jobs_failed), (1, 0));
    let peer = crate::Peer::connect(&fixture.connection, "projection", 64).unwrap();
    let search = peer
        .db()
        .fulltext(TEXT, "needle", &crate::FulltextOptions::default())
        .unwrap();
    assert_eq!(search.stats.index_basis_t, report.basis_t);
    assert_eq!(search.hits.len(), 1);
    assert_eq!(search.hits[0].entity, report.tempids["document"]);
    eprintln!(
        "UNIFIED_PROJECTION basis={} attempts={} failures={} adopted_jobs={}",
        report.basis_t,
        recovered.fulltext.attempts,
        recovered.fulltext.failures,
        recovered.jobs_completed
    );
    service.shutdown();
}

#[test]
fn submitted_context_measures_transaction_phases_retries_and_unknown_outcomes() {
    let Some(fixture) = observation_fixture() else {
        eprintln!("SKIPPED transaction phase integration: ATOMIC_POSTGRES_URL is unset");
        return;
    };
    let database_id = crate::storage::catalog::identity_string(fixture.database.identity);
    let service = TransactionService::start_with_indexing(
        observation_service_config(&fixture.connection, "observed".into()),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let callbacks = Arc::new(AtomicUsize::new(0));
    let delivered = Arc::clone(&callbacks);
    let measured = OperationContext::with_callback(
        OperationKind::Application,
        Arc::new(move |_| {
            delivered.fetch_add(1, Ordering::Relaxed);
        }),
    );
    let request = observation_request("measured", 11);
    // The caller's scope is gone before waiting: only explicit Work
    // propagation can attribute calls made on the transactor thread.
    let ticket = {
        let _scope = measured.enter();
        client.submit(request.clone()).unwrap()
    };
    assert_eq!(
        ticket.request_key_hash,
        crate::storage::receipts::scoped_request_key(&fixture.database.identity, "measured")
            .unwrap()
    );
    let first = ticket.wait(Duration::from_secs(5)).unwrap();
    assert_eq!(first.db_before.basis_t(), service.recovery_stats().target_t);
    assert_eq!(first.basis_t, service.recovery_stats().target_t + 1);
    let first_stats = measured.snapshot();
    for kind in [
        OperationKind::Transaction,
        OperationKind::TransactionExpansion,
        OperationKind::TransactionAssessment,
        OperationKind::TransactionEncoding,
        OperationKind::TransactionCommit,
        OperationKind::TransactionReport,
    ] {
        assert!(first_stats.phases[&kind].invocations > 0);
        assert!(first_stats.phases[&kind].elapsed_nanos > 0);
    }
    // Assessment may be entirely cache-resident; SQL is not required to
    // make a real semantic phase observable.
    assert!(first_stats.by_operation[&OperationKind::TransactionCommit].calls > 0);
    assert_eq!(
        callbacks.load(Ordering::Relaxed),
        0,
        "worker invoked caller metric callback"
    );
    assert!(measured.publish());
    assert_eq!(callbacks.load(Ordering::Relaxed), 1);

    let replay_context = OperationContext::new(OperationKind::Application);
    let replay = {
        let _scope = replay_context.enter();
        client.submit(request).unwrap()
    }
    .wait(Duration::from_secs(5))
    .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let replay_stats = replay_context.snapshot();
    assert_eq!(
        replay_stats.phases[&OperationKind::TransactionReport].invocations,
        1,
        "one receipt reconstruction must record one shared report phase"
    );
    for kind in [
        OperationKind::TransactionExpansion,
        OperationKind::TransactionAssessment,
        OperationKind::TransactionEncoding,
        OperationKind::TransactionCommit,
    ] {
        assert!(
            !replay_stats.phases.contains_key(&kind),
            "replay fabricated an unexecuted phase"
        );
    }
    assert_eq!(
        measured.snapshot().phases,
        first_stats.phases,
        "sibling operation changed original phase totals"
    );

    let rejected_context = OperationContext::new(OperationKind::Application);
    let rejected = {
        let _scope = rejected_context.enter();
        client
            .submit(TransactionRequest::new(
                "invalid-value",
                vec![TxOp::Add {
                    entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
                    attribute: OBSERVED_ITEM_COUNT,
                    value: TxValue::Scalar(Value::String("not a long".into())),
                }],
            ))
            .unwrap()
    }
    .wait(Duration::from_secs(5))
    .unwrap_err();
    assert_ne!(rejected.category, ErrorCategory::UnknownOutcome);
    let rejected_stats = rejected_context.snapshot();
    assert!(
        rejected_stats
            .phases
            .contains_key(&OperationKind::TransactionAssessment)
    );
    assert!(
        !rejected_stats
            .phases
            .contains_key(&OperationKind::TransactionEncoding)
    );
    assert!(
        !rejected_stats
            .phases
            .contains_key(&OperationKind::TransactionCommit)
    );

    let unknown_context = OperationContext::new(OperationKind::Application);
    let reports = client.subscribe_reports();
    let unknown_request = observation_request("measured-unknown", 22);
    arm_observation_fault(
        &database_id,
        "measured-unknown",
        CommitObservationFault::AfterCommitBeforeResponse,
    );
    let error = {
        let _scope = unknown_context.enter();
        client.submit(unknown_request.clone()).unwrap()
    }
    .wait(Duration::from_secs(5))
    .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    let durable = reports.recv_timeout(Duration::from_secs(5)).unwrap();
    // A subsequent request cannot overtake reconciliation; awaiting it
    // also makes that original context's post-response measurements final.
    let reconciled = client
        .transact(unknown_request, Duration::from_secs(5))
        .unwrap();
    assert!(reconciled.replayed);
    assert_eq!(reconciled.tx_hash, durable.tx_hash);
    let unknown_stats = unknown_context.snapshot();
    let report_phase = unknown_stats
        .phases
        .get(&OperationKind::TransactionReport)
        .expect("unknown-outcome reconciliation must measure exact report reconstruction");
    assert!(report_phase.invocations >= 1);
    assert!(report_phase.elapsed_nanos > 0);
    assert!(unknown_stats.by_operation[&OperationKind::TransactionReport].calls > 0);
    assert_eq!(callbacks.load(Ordering::Relaxed), 1);
    assert_eq!(service.writer_residency_stats().eager_database_values, 0);
    println!(
        "TRANSACTION_PHASES_OK sql_calls={} phases={:?} replay_sql_calls={} rejected_before_publication=true unknown_reconciled=true explicit_callbacks=1",
        first_stats.sql_calls, first_stats.phases, replay_stats.sql_calls
    );
    service.shutdown();
}

#[test]
fn block_index_preparation_does_not_replace_newer_transactions_or_receipts() {
    let Some(fixture) = observation_fixture() else {
        return;
    };
    let service = TransactionService::start_with_indexing(
        observation_service_config(&fixture.connection, "observed".into()),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let first_request = observation_request("index-prefix", 11);
    let first = client
        .transact(first_request.clone(), Duration::from_secs(10))
        .unwrap();
    let identity = crate::storage::catalog::identity_string(fixture.database.identity);
    let (ready, release) = index_lane::pause_preparation(&identity);
    let requested = client.request_index().unwrap();
    assert_eq!(requested.target_t, first.basis_t);
    ready.recv_timeout(Duration::from_secs(10)).unwrap();
    let newer_request = observation_request("index-newer", 22);
    let newer = client
        .transact(newer_request.clone(), Duration::from_secs(10))
        .unwrap();
    assert_eq!(newer.basis_t, first.basis_t + 1);
    release.send(()).unwrap();
    let deadline = Instant::now() + Duration::from_secs(10);
    while client.background_indexing_stats().published_basis_t < requested.target_t {
        assert!(
            Instant::now() < deadline && client.is_available(),
            "index failed: {:?}",
            client.background_indexing_stats()
        );
        thread::sleep(Duration::from_millis(5));
    }
    let stats = client.background_indexing_stats();
    assert_eq!(stats.published_basis_t, first.basis_t);
    assert_eq!(stats.target_basis_t, newer.basis_t);
    assert_eq!(
        stats.total_transactions, 1,
        "only the covered prefix retires"
    );
    assert_eq!(stats.jobs_completed, 1);
    for (request, expected) in [(first_request, &first), (newer_request, &newer)] {
        let replay = client.transact(request, Duration::from_secs(10)).unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, expected.tx_hash);
        assert_eq!(
            replay.db_after.datoms(crate::IndexOrder::Eavt).unwrap(),
            expected.db_after.datoms(crate::IndexOrder::Eavt).unwrap()
        );
    }
    assert_eq!(
        first
            .db_after
            .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
            .unwrap(),
        vec![Value::Long(11)]
    );
    service.shutdown();
}

#[test]
fn operator_index_publication_discards_a_stale_background_candidate_without_fencing() {
    let Some(fixture) = observation_fixture() else {
        return;
    };
    let service = TransactionService::start_with_indexing(
        observation_service_config(&fixture.connection, "observed".into()),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let request = observation_request("operator-prefix", 11);
    let first = client
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    let identity = crate::storage::catalog::identity_string(fixture.database.route);
    let (ready, release) = index_lane::pause_preparation(&identity);
    assert!(client.request_index().unwrap().scheduled);
    ready.recv_timeout(Duration::from_secs(10)).unwrap();
    let manual = crate::PostgresOperator::connect(&fixture.connection)
        .unwrap()
        .consolidate_database(&identity);
    // Always release the independent worker before asserting the outcome.
    // Fixture cleanup must never race a stranded preparation thread.
    release.send(()).unwrap();
    assert_eq!(manual.unwrap().basis_t, first.basis_t);
    let deadline = Instant::now() + Duration::from_secs(10);
    let stats = loop {
        let stats = client.background_indexing_stats();
        if stats.published_basis_t >= first.basis_t && !stats.job_in_flight {
            break stats;
        }
        assert!(
            Instant::now() < deadline && client.is_available(),
            "{stats:?}"
        );
        thread::sleep(Duration::from_millis(5));
    };
    assert_eq!(stats.jobs_failed, 0);
    assert!(
        stats.jobs_started >= 2,
        "stale job was not recaptured: {stats:?}"
    );
    assert!(stats.last_failure.is_none());
    let replay = client.transact(request, Duration::from_secs(10)).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let second = client
        .transact(
            observation_request("after-operator", 22),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_eq!(second.basis_t, first.basis_t + 1);
    assert_eq!(
        first
            .db_after
            .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
            .unwrap(),
        vec![Value::Long(11)]
    );
    service.shutdown();
}

#[test]
fn block_index_backpressure_replays_before_gate_and_invokes_parked_callback_once() {
    let Some(fixture) = observation_fixture() else {
        return;
    };
    let called = Arc::new(AtomicUsize::new(0));
    let counted = Arc::clone(&called);
    let name = crate::Symbol::new("test.index.v1", "write");
    let mut registry = crate::NativeRegistry::builder();
    registry
        .transaction(name.clone(), move |_, _, control| {
            control.check(1)?;
            counted.fetch_add(1, Ordering::SeqCst);
            Ok(observation_request("unused", 22).forms)
        })
        .unwrap();
    let service = TransactionService::start_with_options(
        observation_service_config(&fixture.connection, "observed".into()),
        ServiceOptions {
            indexing: BackgroundIndexingConfig {
                memory_index_threshold_bytes: 128,
                memory_index_max_bytes: 256,
            },
            execution: crate::TransactionExecutionOptions {
                native: registry.build(),
                ..Default::default()
            },
            ..Default::default()
        },
    )
    .unwrap();
    let client = service.client();
    let identity = crate::storage::catalog::identity_string(fixture.database.identity);
    let (ready, release) = index_lane::pause_preparation(&identity);
    let request = observation_request("before-pressure", 11);
    let first = client
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    ready.recv_timeout(Duration::from_secs(10)).unwrap();
    assert!(client.background_indexing_stats().total_bytes > 256);
    let replay = client.transact(request, Duration::from_secs(10)).unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let pending = client
        .submit(TransactionRequest::from_forms(
            "parked-native",
            vec![TxForm::ProgramCall(ProgramCall {
                function: crate::CallableRef::Local(name),
                arguments: vec![],
            })],
        ))
        .unwrap();
    let deadline = Instant::now() + Duration::from_secs(10);
    while client.background_indexing_stats().backpressure_stalls == 0 {
        assert!(Instant::now() < deadline && client.is_available());
        thread::sleep(Duration::from_millis(5));
    }
    assert_eq!(
        called.load(Ordering::SeqCst),
        0,
        "parked admission ran a callback"
    );
    assert_eq!(client.stats().queued, 1);
    release.send(()).unwrap();
    let second = pending.wait(Duration::from_secs(10)).unwrap();
    assert_eq!(second.basis_t, first.basis_t + 1);
    assert_eq!(called.load(Ordering::SeqCst), 1);
    assert_eq!(client.stats().queued, 0);
    assert!(client.background_indexing_stats().jobs_completed >= 1);
    assert_eq!(
        first
            .db_after
            .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
            .unwrap(),
        vec![Value::Long(11)]
    );
    service.shutdown();
}

#[test]
fn unknown_outcome_reconciliation_notifies_once_only_for_a_durable_decision() {
    let Some(fixture) = observation_fixture() else {
        return;
    };
    let database_id = crate::storage::catalog::identity_string(fixture.database.identity);

    // Keep committed novelty resident so the accounting assertions cannot
    // race a background publication which legitimately drains it.
    let service = TransactionService::start_with_indexing(
        observation_service_config(&fixture.connection, "observed".into()),
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let reports = client.subscribe_reports();
    let initial_basis = service.recovery_stats().target_t;
    assert_eq!(initial_basis, 1, "schema installation is a transaction");

    let committed_request = observation_request("committed-unknown", 11);
    arm_observation_fault(
        &database_id,
        "committed-unknown",
        CommitObservationFault::AfterCommitBeforeResponse,
    );
    let committed_error = client
        .submit(committed_request.clone())
        .unwrap()
        .wait(Duration::from_secs(2))
        .unwrap_err();
    assert_eq!(committed_error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(committed_error.details["ambiguity_kind"], "publication");

    // The origin observes Unknown first. The worker then reconnects,
    // renews its fenced lease, resolves without resubmission, and emits
    // the notification which authoritative peer data delivery would have
    // produced even though the request acknowledgement was lost.
    let committed_report = reports.recv_timeout(Duration::from_secs(2)).unwrap();
    assert_eq!(committed_report.basis_t, initial_basis + 1);
    assert!(!committed_report.replayed);
    assert_eq!(
        committed_report
            .db_after
            .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
            .unwrap(),
        vec![Value::Long(11)]
    );
    let committed_datoms = committed_report.tx_data.len() as u64;

    let replay = client
        .transact(committed_request.clone(), Duration::from_secs(2))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, committed_report.tx_hash);
    assert_eq!(replay.tx_data, committed_report.tx_data);
    assert_eq!(replay.tempids, committed_report.tempids);
    for (left, right) in [
        (&replay.db_before, &committed_report.db_before),
        (&replay.db_after, &committed_report.db_after),
    ] {
        assert_eq!(left.basis_t(), right.basis_t());
        assert_eq!(
            left.datoms(crate::IndexOrder::Eavt).unwrap(),
            right.datoms(crate::IndexOrder::Eavt).unwrap()
        );
    }
    let first_snapshot = committed_report.db_after.block_snapshot().unwrap();
    let replay_snapshot = replay.db_after.block_snapshot().unwrap();
    let cache_before = first_snapshot.cache_stats();
    replay.db_after.datoms(crate::IndexOrder::Eavt).unwrap();
    assert!(replay_snapshot.cache_stats().hits > cache_before.hits);
    assert_eq!(
        first_snapshot.cache_stats(),
        replay_snapshot.cache_stats(),
        "receipt values share the block reader cache, not backend-specific writer state"
    );
    let after_commit = client.background_indexing_stats();
    assert_eq!(after_commit.target_basis_t, initial_basis + 1);
    assert_eq!(after_commit.total_transactions, 1);
    assert_eq!(after_commit.total_datoms, committed_datoms);
    assert!(
        matches!(
            reports.recv_timeout(Duration::from_millis(100)),
            Err(mpsc::RecvTimeoutError::Timeout)
        ),
        "an ordinary idempotent retry must not duplicate the live report"
    );
    assert_eq!(
        client.background_indexing_stats().total_transactions,
        1,
        "an ordinary replay must not charge indexing novelty twice"
    );

    arm_observation_fault(
        &database_id,
        "committed-unknown",
        CommitObservationFault::AfterCommitBeforeResponse,
    );
    let replay_read_error = client
        .submit(committed_request.clone())
        .unwrap()
        .wait(Duration::from_secs(2))
        .unwrap_err();
    assert_eq!(replay_read_error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(replay_read_error.details["ambiguity_kind"], "outcome-read");
    assert!(
        matches!(
            reports.recv_timeout(Duration::from_millis(100)),
            Err(mpsc::RecvTimeoutError::Timeout)
        ),
        "acknowledgement loss while reading a replay must not duplicate its original report"
    );

    let replay_after_read_ambiguity = client
        .transact(committed_request, Duration::from_secs(2))
        .unwrap();
    assert!(replay_after_read_ambiguity.replayed);
    assert_eq!(
        replay_after_read_ambiguity.tx_hash,
        committed_report.tx_hash
    );
    assert!(
        matches!(
            reports.recv_timeout(Duration::from_millis(100)),
            Err(mpsc::RecvTimeoutError::Timeout)
        ),
        "reconciling an ambiguous replay read must remain report-suppressed"
    );
    assert_eq!(
        client.background_indexing_stats().total_transactions,
        1,
        "an ordinary replay must not charge indexing novelty twice"
    );

    arm_observation_fault(
        &database_id,
        "absent-unknown",
        CommitObservationFault::AbsentUnknownOutcome,
    );
    let absent_error = client
        .submit(observation_request("absent-unknown", 22))
        .unwrap()
        .wait(Duration::from_secs(2))
        .unwrap_err();
    assert_eq!(absent_error.category, ErrorCategory::UnknownOutcome);

    // The next ordinary request cannot overtake reconciliation. A locked,
    // successful absence decision produces no report and no novelty; only
    // this genuinely committed successor is observed.
    let successor = client
        .transact(
            observation_request("after-absent", 33),
            Duration::from_secs(2),
        )
        .unwrap();
    assert_eq!(successor.basis_t, initial_basis + 2);
    let successor_report = reports.recv_timeout(Duration::from_secs(2)).unwrap();
    assert_eq!(successor_report.tx_hash, successor.tx_hash);
    assert!(matches!(
        reports.recv_timeout(Duration::from_millis(100)),
        Err(mpsc::RecvTimeoutError::Timeout)
    ));

    arm_observation_fault(
        &database_id,
        "rolled-back",
        CommitObservationFault::BeforePublication,
    );
    let rollback_error = client
        .submit(observation_request("rolled-back", 44))
        .unwrap()
        .wait(Duration::from_secs(2))
        .unwrap_err();
    assert_eq!(
        (rollback_error.category, rollback_error.code),
        (ErrorCategory::Interrupted, "storage/injected-failure")
    );
    assert!(
        matches!(
            reports.recv_timeout(Duration::from_millis(100)),
            Err(mpsc::RecvTimeoutError::Timeout)
        ),
        "rolled-back publication work must remain invisible"
    );
    let final_stats = client.background_indexing_stats();
    assert_eq!(final_stats.target_basis_t, initial_basis + 2);
    assert_eq!(final_stats.total_transactions, 2);
    assert_eq!(
        final_stats.total_datoms,
        committed_datoms + successor_report.tx_data.len() as u64
    );

    service.shutdown();
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    let mut verifier = crate::BlockTransactor::claim(
        &config,
        fixture.database.clone(),
        crate::BlockWriterOptions::default(),
    )
    .unwrap();
    assert_eq!(verifier.db().unwrap().basis_t(), initial_basis + 2);
    for (key, value, present) in [
        ("committed-unknown", 11, true),
        ("absent-unknown", 22, false),
        ("rolled-back", 44, false),
    ] {
        let request = observation_request(key, value);
        let (digest, _) = crate::encoding::canonical_submission_request(
            &request.forms,
            request.compare_basis_t,
            request.tx_instant_override,
            crate::storage::log::MAX_LOG_TRANSACTION_BYTES,
        )
        .unwrap();
        assert_eq!(
            verifier
                .resolve_request_outcome(key, digest)
                .unwrap()
                .is_some(),
            present
        );
    }
    assert_eq!(
        committed_report
            .db_after
            .values(make_eid(USER_PARTITION, 42).unwrap(), OBSERVED_ITEM_COUNT)
            .unwrap(),
        vec![Value::Long(11)],
        "captured pre-successor value remains immutable"
    );
    verifier.release().unwrap();
}
