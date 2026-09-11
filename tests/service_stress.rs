mod common;
use atomic_core::{
    Attribute, Cardinality, EntityRef, ErrorCategory, Keyword, Schema, TransactionRequest,
    TransactionService, TransactionServiceConfig, TxOp, TxValue, Value, ValueType,
};
use std::time::{Duration, Instant, SystemTime, UNIX_EPOCH};

const COUNTER_VALUE: u32 = 1_000;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
}

fn unique(prefix: &str) -> String {
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
        .install(Attribute::new(
            COUNTER_VALUE,
            Keyword::new("counter", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

#[test]
fn concurrent_load_stays_bounded_and_every_timeline_is_serial() {
    let Some(connection) = connection() else {
        return;
    };
    const PRODUCERS: usize = 12;
    const TRANSACTIONS: u64 = 20;
    const QUEUE_CAPACITY: usize = 4;
    common::install(&connection).unwrap();
    let mut setup = common::TestStore::connect(&connection).unwrap();
    let database_id = unique("stress");
    let initial_basis = setup
        .create_database(&database_id, schema())
        .unwrap()
        .basis_t();
    drop(setup);

    let service = TransactionService::start(TransactionServiceConfig {
        connection: connection.clone(),
        database_id: database_id.clone(),
        holder_id: "stress-leader".into(),
        lease_duration: Duration::from_secs(2),
        renew_interval: Duration::from_millis(100),
        queue_capacity: QUEUE_CAPACITY,
        capacity_limits: atomic_core::CapacityLimits::default(),
    })
    .unwrap();
    let client = service.client();
    let seeded = client
        .transact(
            TransactionRequest::new(
                "counters",
                (0..PRODUCERS)
                    .map(|producer| TxOp::Add {
                        entity: EntityRef::Temp(format!("counter-{producer}")),
                        attribute: COUNTER_VALUE,
                        value: Value::Long(0).into(),
                    })
                    .collect(),
            ),
            Duration::from_secs(10),
        )
        .unwrap();
    let counters: Vec<_> = (0..PRODUCERS)
        .map(|producer| seeded.tempids[&format!("counter-{producer}")])
        .collect();
    let started = Instant::now();
    let handles: Vec<_> = (0..PRODUCERS)
        .map(|producer| {
            let client = client.clone();
            let counter = counters[producer];
            std::thread::spawn(move || {
                for ordinal in 0..TRANSACTIONS {
                    let request = TransactionRequest::new(
                        format!("producer-{producer}-request-{ordinal}"),
                        vec![TxOp::Add {
                            entity: EntityRef::Id(counter),
                            attribute: COUNTER_VALUE,
                            value: TxValue::Scalar(Value::Long((ordinal + 1) as i64)),
                        }],
                    );
                    loop {
                        match client.submit(request.clone()) {
                            Ok(ticket) => {
                                let report = ticket.wait(Duration::from_secs(5)).unwrap();
                                assert_eq!(report.db_before.basis_t() + 1, report.basis_t);
                                break;
                            }
                            Err(error) if error.category == ErrorCategory::Busy => {
                                std::thread::sleep(Duration::from_micros(100));
                            }
                            Err(error) => panic!("unexpected service failure: {error}"),
                        }
                    }
                }
            })
        })
        .collect();
    for handle in handles {
        handle.join().unwrap();
    }
    let elapsed = started.elapsed();
    let stats = client.stats();
    assert_eq!(stats.processed, 1 + PRODUCERS as u64 * TRANSACTIONS);
    assert!(
        stats.max_queued <= QUEUE_CAPACITY + 1,
        "accounting includes the one request handed from the channel to the worker"
    );
    assert!(
        stats.rejected_full > 0,
        "load did not exercise backpressure"
    );

    let mut verify = common::TestStore::connect(&connection).unwrap();
    let recovered = verify.recover(&database_id).unwrap();
    assert_eq!(
        recovered.basis_t(),
        initial_basis + 1 + PRODUCERS as u64 * TRANSACTIONS
    );
    for &counter in &counters[..PRODUCERS] {
        assert_eq!(
            recovered.values(counter, COUNTER_VALUE).unwrap(),
            vec![Value::Long(TRANSACTIONS as i64)]
        );
    }
    eprintln!(
        "processed {} transactions in {:?}; queue high-water {}, rejected admissions {}",
        stats.processed, elapsed, stats.max_queued, stats.rejected_full
    );
    service.shutdown();
}
