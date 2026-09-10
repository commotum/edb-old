//! Bounded, opt-in phase evidence, not a throughput benchmark or timing gate.
//! Run with an actual disposable PostgreSQL catalog:
//! cargo test --release --test transaction_phases -- --ignored --nocapture
use atomic_core::{
    Attribute, BackgroundIndexingConfig, Cardinality, EntityRef, Keyword, OperationContext,
    OperationKind, PostgresMigrator, PostgresStore, Schema, TransactionRequest, TransactionService,
    TransactionServiceConfig, TxOp, Value, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const COUNT: u32 = 1_000;
const REQUESTS: usize = 4;
const WAIT: Duration = Duration::from_secs(30);

struct Fixture {
    admin: Client,
    schema: String,
    connection: String,
}

impl Fixture {
    fn new(connection: &str) -> Self {
        let schema = format!(
            "transaction_phases_{}_{}",
            std::process::id(),
            SystemTime::now()
                .duration_since(UNIX_EPOCH)
                .unwrap()
                .as_nanos()
        );
        let mut admin = Client::connect(connection, NoTls).unwrap();
        admin
            .batch_execute(&format!("CREATE SCHEMA {schema}"))
            .unwrap();
        let connection =
            if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
                format!(
                    "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
                    if connection.contains('?') { "&" } else { "?" }
                )
            } else {
                format!("{connection} options='-csearch_path={schema},pg_catalog'")
            };
        let fixture = Self {
            admin,
            schema,
            connection,
        };
        PostgresMigrator::connect(&fixture.connection)
            .unwrap()
            .migrate()
            .unwrap();
        fixture
    }
}

impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}

// Includes all threads in this test process, including writer maintenance. It
// deliberately does not claim to measure PostgreSQL server CPU or phase CPU.
#[cfg(target_os = "linux")]
fn process_cpu_ns() -> Option<u64> {
    let mut time = libc::timespec {
        tv_sec: 0,
        tv_nsec: 0,
    };
    // SAFETY: clock_gettime receives a valid writable timespec and constant id.
    let status = unsafe { libc::clock_gettime(libc::CLOCK_PROCESS_CPUTIME_ID, &mut time) };
    (status == 0).then(|| time.tv_sec as u64 * 1_000_000_000 + time.tv_nsec as u64)
}

#[cfg(not(target_os = "linux"))]
fn process_cpu_ns() -> Option<u64> {
    None
}

fn data_operations(entities: &[EntityRef], value: i64) -> Vec<TxOp> {
    entities
        .iter()
        .skip(1)
        .map(|entity| TxOp::Add {
            entity: entity.clone(),
            attribute: COUNT,
            value: Value::Long(value).into(),
        })
        .collect()
}

#[test]
#[ignore = "manual bounded phase campaign requires ATOMIC_POSTGRES_URL"]
fn queued_dependent_transaction_phase_sample() {
    let connection = std::env::var("ATOMIC_POSTGRES_URL")
        .expect("phase campaign requires an actual disposable PostgreSQL catalog");
    let fixture = Fixture::new(&connection);
    for data_count in [1, 64, 256] {
        for queued in [false, true] {
            let database = format!("sample_{data_count}_{queued}");
            let mut schema = Schema::new();
            schema
                .install(Attribute::new(
                    COUNT,
                    Keyword::new("sample", "count"),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
            PostgresStore::connect(&fixture.connection)
                .unwrap()
                .create_database(&database, schema)
                .unwrap();
            let service = TransactionService::start_with_indexing(
                TransactionServiceConfig {
                    connection: fixture.connection.clone(),
                    database_id: database.clone(),
                    holder_id: database,
                    lease_duration: Duration::from_secs(5),
                    renew_interval: Duration::from_millis(100),
                    queue_capacity: REQUESTS + 1,
                    capacity_limits: Default::default(),
                },
                BackgroundIndexingConfig {
                    memory_index_threshold_bytes: 1 << 30,
                    memory_index_max_bytes: 2 << 30,
                },
            )
            .unwrap();
            let client = service.client();
            let entities: Vec<_> = (0..=data_count)
                .map(|index| EntityRef::Temp(format!("sample-{index}")))
                .collect();
            let mut seed = data_operations(&entities, 0);
            seed.push(TxOp::Add {
                entity: entities[0].clone(),
                attribute: COUNT,
                value: Value::Long(0).into(),
            });
            // Populate this exact access set before either sample mode. Setup,
            // startup and final semantic checks are outside the measured scope.
            let seeded = client
                .transact(
                    TransactionRequest::new("seed", seed).with_tx_instant(1_000),
                    WAIT,
                )
                .unwrap();
            let counter = seeded.tempids["sample-0"];
            let entities: Vec<_> = (0..=data_count)
                .map(|index| EntityRef::Id(seeded.tempids[&format!("sample-{index}")]))
                .collect();
            let mut requests = Vec::new();
            for index in 0..REQUESTS {
                let mut operations = data_operations(&entities, index as i64 + 1);
                // Both CAS and basis comparison make each request depend on
                // the preceding durable decision, not an independent workload.
                operations.push(TxOp::Cas {
                    entity: entities[0].clone(),
                    attribute: COUNT,
                    old: Some(Value::Long(index as i64).into()),
                    new: Value::Long(index as i64 + 1).into(),
                });
                requests.push(
                    TransactionRequest::new(format!("measured-{index}"), operations)
                        // The tx instant participates in commitment keys: fix
                        // it so before/after runs traverse the same treaps.
                        .with_tx_instant(2_000 + index as i64)
                        .comparing_basis(seeded.basis_t + index as u64),
                );
            }
            let context = OperationContext::new(OperationKind::Application);
            let cpu_before = process_cpu_ns();
            let started = Instant::now();
            let mut reports = Vec::new();
            let mut tickets = Vec::new();
            for request in requests {
                let ticket = {
                    let _scope = context.enter();
                    client.submit(request).unwrap()
                };
                if queued {
                    tickets.push(ticket);
                } else {
                    reports.push(ticket.wait(WAIT).unwrap());
                }
            }
            for ticket in tickets {
                reports.push(ticket.wait(WAIT).unwrap());
            }
            let wall_ns = started.elapsed().as_nanos();
            let cpu_ns = cpu_before
                .zip(process_cpu_ns())
                .map(|(before, after)| after - before);
            let stats = context.snapshot();
            assert_eq!(stats.errors, 0);
            assert_eq!(
                stats.phases[&OperationKind::Transaction].invocations,
                REQUESTS as u64
            );
            for (index, report) in reports.iter().enumerate() {
                assert!(!report.replayed);
                assert_eq!(report.basis_t, seeded.basis_t + index as u64 + 1);
                assert_eq!(report.db_before.basis_t(), seeded.basis_t + index as u64);
                assert_eq!(
                    report.db_after.values(counter, COUNT).unwrap(),
                    vec![Value::Long(index as i64 + 1)]
                );
            }
            assert_eq!(service.writer_residency_stats().eager_database_values, 0);
            println!(
                "TRANSACTION_SAMPLE data_ops={} requests={} queued={} wall_ns={} rust_process_cpu_ns={:?} sql_calls={} sql_ns={} result_cell_bytes={} known_read_payload_bytes={} known_write_payload_bytes={} phases={:?} sql_by_operation={:?} dependent_cas_and_basis=true background_indexing=false pg_cpu_unmeasured=true",
                data_count,
                REQUESTS,
                queued,
                wall_ns,
                cpu_ns,
                stats.sql_calls,
                stats.elapsed_nanos,
                stats.result_cell_bytes,
                stats.known_payload_read_bytes,
                stats.known_payload_write_bytes,
                stats.phases,
                stats.by_operation,
            );
            service.shutdown();
        }
    }
}
