mod common;

use atomic_core::*;
use common::PostgresFixture;
use std::time::{Duration, Instant};

const WAIT: Duration = Duration::from_secs(30);
const LEFT: u32 = 1_000;
const RIGHT: u32 = 1_001;
const PAIR: u32 = 1_002;

fn wide_schema(groups: u32) -> Schema {
    let mut schema = Schema::new();
    for group in 0..groups {
        let first = LEFT + group * 3;
        for offset in 0..2 {
            schema
                .install(Attribute::new(
                    first + offset,
                    Keyword::new("bookkeeping", format!("part-{}", first + offset)),
                    ValueType::Long,
                    Cardinality::One,
                ))
                .unwrap();
        }
        schema
            .install(
                Attribute::new(
                    first + 2,
                    Keyword::new("bookkeeping", format!("pair-{group}")),
                    ValueType::Tuple,
                    Cardinality::One,
                )
                .tuple(TupleSpec::Composite(vec![first, first + 1])),
            )
            .unwrap();
    }
    schema
}

fn config(connection: &str, database: &str, holder: &str) -> TransactionServiceConfig {
    TransactionServiceConfig {
        connection: connection.into(),
        database_id: database.into(),
        holder_id: holder.into(),
        lease_duration: Duration::from_secs(5),
        renew_interval: Duration::from_millis(100),
        queue_capacity: 8,
        capacity_limits: CapacityLimits::default(),
    }
}

fn indexing() -> BackgroundIndexingConfig {
    // Deliberately retain this small tail. Background publication remains enabled,
    // but the measured data-only transactions do not cross the threshold.
    BackgroundIndexingConfig {
        memory_index_threshold_bytes: 32 * 1024 * 1024,
        memory_index_max_bytes: 64 * 1024 * 1024,
    }
}

fn request(ordinal: u64) -> TransactionRequest {
    TransactionRequest::new(
        format!("item-{ordinal}"),
        [LEFT, RIGHT]
            .into_iter()
            .map(|attribute| TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute,
                value: Value::Long(ordinal as i64).into(),
            })
            .collect(),
    )
}

fn expected_pair(value: i64) -> Vec<Value> {
    vec![Value::Tuple(vec![
        Some(Value::Long(value)),
        Some(Value::Long(value)),
    ])]
}

#[test]
fn fixed_transactions_reuse_metadata_and_account_tails_through_recovery_and_adoption() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED transaction bookkeeping PG: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = PostgresFixture::new(&url, "bookkeeping");
    PostgresMigrator::connect(&fixture.connection)
        .unwrap()
        .migrate()
        .unwrap();
    for groups in [4, 32, 128] {
        let database = format!("bookkeeping_{groups}");
        let mut setup = PostgresStore::connect(&fixture.connection).unwrap();
        let initial = setup
            .create_database(&database, wide_schema(groups))
            .unwrap();
        let initial_t = initial.basis_t();
        drop(initial);
        drop(setup);
        let service = TransactionService::start_with_indexing(
            config(&fixture.connection, &database, "first"),
            indexing(),
        )
        .unwrap();
        let client = service.client();
        let mut total_datoms = 0_u64;
        let mut saved = None;
        for ordinal in 0..129_u64 {
            let request = request(ordinal);
            let started = Instant::now();
            let report = client.transact(request, WAIT).unwrap();
            assert_eq!(report.basis_t, initial_t + ordinal + 1);
            let entity = report.tempids["item"];
            assert_eq!(
                report.db_after.values(entity, PAIR).unwrap(),
                expected_pair(ordinal as i64)
            );
            total_datoms += report.tx_data.len() as u64;
            let stats = service.background_indexing_stats();
            assert_eq!(stats.total_transactions, ordinal + 1);
            assert_eq!(stats.total_datoms, total_datoms);
            let writer = service.writer_residency_stats();
            assert_eq!(writer.recent_datoms, total_datoms);
            assert_eq!(writer.recent_accounted_bytes, stats.total_bytes);
            assert_eq!(writer.eager_database_values, 0);
            assert_eq!(writer.last_schema_transition_attributes, 0);
            assert_eq!(writer.last_schema_projection_attributes, 0);
            assert_eq!(writer.last_schema_validation_attributes, 0);
            assert_eq!(writer.last_schema_validation_tuple_constituents, 0);
            assert!(writer.last_schema_reuses > 0);
            assert!(writer.last_schema_dependency_lookups <= 16);
            assert!(writer.last_schema_dependency_edges <= 8);
            assert!(writer.last_schema_composite_candidates > 0);
            if ordinal == 0 {
                saved = Some(report.clone());
            }
            drop(report);
            let elapsed = started.elapsed();
            if [0, 32, 128].contains(&ordinal) {
                eprintln!(
                    "BOOKKEEPING_PG groups={groups} attributes={} tail={} datoms={total_datoms} schema_scans=0 schema_reuses={} dependency_lookups={} dependency_edges={} source_datoms={} ranges={} sql_reads={} commitment_visits={} transact_stats_query_drop_us={}",
                    writer.resident_schema_attributes,
                    ordinal + 1,
                    writer.last_schema_reuses,
                    writer.last_schema_dependency_lookups,
                    writer.last_schema_dependency_edges,
                    writer.last_transaction_source_read_datoms,
                    writer.last_native_cursor_ranges,
                    writer.last_native_sql_reads,
                    writer.last_commitment_node_visits,
                    elapsed.as_micros()
                );
            }
        }
        let original = saved.unwrap();
        let first = original.tempids["item"];
        // Unchanged-schema reuse must never become an invalid transition bypass.
        let before = client.transact(request(128), WAIT).unwrap();
        let mut invalid = before.db_after.schema().attribute(LEFT).unwrap().clone();
        invalid.cardinality = Cardinality::Many;
        let error = client
            .transact(
                TransactionRequest::new("invalid-schema", vec![TxOp::AlterAttribute(invalid)]),
                WAIT,
            )
            .unwrap_err();
        assert_eq!(error.code, "schema/invalid-tuple-attributes");
        assert_eq!(
            client.transact(request(128), WAIT).unwrap().basis_t,
            before.basis_t
        );
        let late_pair = u32::try_from(before.db_after.eidx_frontier()).unwrap();

        let installed = client
            .transact(
                TransactionRequest::new(
                    "late-composite",
                    vec![TxOp::InstallAttribute(
                        Attribute::new(
                            late_pair,
                            Keyword::new("bookkeeping", "late"),
                            ValueType::Tuple,
                            Cardinality::One,
                        )
                        .tuple(TupleSpec::Composite(vec![LEFT, RIGHT])),
                    )],
                ),
                WAIT,
            )
            .unwrap();
        assert!(
            installed
                .db_after
                .values(first, late_pair)
                .unwrap()
                .is_empty()
        );
        let populated = client
            .transact(
                TransactionRequest::new(
                    "touch-old",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(first),
                        attribute: LEFT,
                        value: Value::Long(0).into(),
                    }],
                ),
                WAIT,
            )
            .unwrap();
        assert_eq!(
            populated.db_after.values(first, late_pair).unwrap(),
            expected_pair(0)
        );
        assert!(original.db_after.schema().attribute(late_pair).is_err());
        assert_eq!(
            original.db_after.values(first, PAIR).unwrap(),
            expected_pair(0)
        );
        // Ordinary enum identity metadata must not rebuild installed schema.
        // Alias history and repurposing still use the exact db-value dictionary.
        let old_name = Keyword::new("status", "queued");
        let new_name = Keyword::new("status", "waiting");
        let named = client
            .transact(
                TransactionRequest::new(
                    "enum",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("enum".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(old_name.clone()).into(),
                    }],
                ),
                WAIT,
            )
            .unwrap();
        let enum_id = named.tempids["enum"];
        let renamed = client
            .transact(
                TransactionRequest::new(
                    "rename-enum",
                    vec![TxOp::Add {
                        entity: EntityRef::Id(enum_id),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(new_name.clone()).into(),
                    }],
                ),
                WAIT,
            )
            .unwrap();
        assert_eq!(renamed.db_after.entid(&old_name), Some(enum_id));
        assert_eq!(renamed.db_after.entid(&new_name), Some(enum_id));
        let repurposed = client
            .transact(
                TransactionRequest::new(
                    "repurpose-enum",
                    vec![TxOp::Add {
                        entity: EntityRef::Temp("replacement".into()),
                        attribute: DB_IDENT as u32,
                        value: Value::Keyword(old_name.clone()).into(),
                    }],
                ),
                WAIT,
            )
            .unwrap();
        assert_eq!(
            repurposed.db_after.entid(&old_name),
            Some(repurposed.tempids["replacement"])
        );
        assert_eq!(repurposed.db_after.entid(&new_name), Some(enum_id));
        assert_eq!(named.db_after.entid(&old_name), Some(enum_id));
        let enum_work = service.writer_residency_stats();
        assert_eq!(enum_work.last_schema_projection_attributes, 0);
        assert_eq!(enum_work.last_schema_transition_attributes, 0);
        assert_eq!(enum_work.last_schema_validation_attributes, 0);
        assert_eq!(enum_work.last_schema_reuses, 1);
        let endpoint = repurposed.basis_t;
        drop(client);
        service.shutdown();

        // Cold activation authenticates the nonempty tail and reconstructs all
        // bookkeeping, rather than trusting the process-local cached totals.
        let recovered = TransactionService::start_with_indexing(
            config(&fixture.connection, &database, "recovered"),
            indexing(),
        )
        .unwrap();
        assert_eq!(recovered.recovery_stats().target_t, endpoint);
        assert!(recovered.recovery_stats().tail_transactions > 0);
        let replay = recovered.client().transact(request(0), WAIT).unwrap();
        assert!(replay.replayed);
        assert_eq!(replay.tx_hash, original.tx_hash);
        assert_eq!(replay.tempids, original.tempids);
        assert_eq!(replay.tx_data, original.tx_data);
        assert_eq!(replay.basis_t, original.basis_t);
        assert_eq!(
            replay.db_after.values(first, PAIR).unwrap(),
            expected_pair(0)
        );
        let mut indexer = PostgresIndexer::connect(&fixture.connection, &database).unwrap();
        let publication = indexer.consolidate().unwrap();
        assert_eq!(publication.basis_t, endpoint);
        // An administrative external publication is not an unsolicited service
        // notification. Use the normal bounded request to have its coordinator
        // authenticate/adopt the already-built root below the novelty threshold.
        assert_eq!(
            recovered.client().request_index().unwrap().target_t,
            endpoint
        );
        let deadline = Instant::now() + WAIT;
        loop {
            // Exact retries trigger safe adoption without adding novelty.
            let replay = recovered.client().transact(request(128), WAIT).unwrap();
            assert!(replay.replayed);
            if recovered.writer_residency_stats().recent_datoms == 0 {
                break;
            }
            assert!(
                Instant::now() < deadline,
                "writer did not adopt the external publication"
            );
            std::thread::sleep(Duration::from_millis(20));
        }
        let fresh = recovered.client().transact(request(129), WAIT).unwrap();
        assert_eq!(fresh.basis_t, endpoint + 1);
        assert_eq!(
            fresh
                .db_after
                .values(fresh.tempids["item"], late_pair)
                .unwrap(),
            expected_pair(129)
        );
        assert_eq!(
            original.db_after.values(first, PAIR).unwrap(),
            expected_pair(0)
        );
        recovered.shutdown();
        eprintln!(
            "BOOKKEEPING_PG_OK groups={groups} tail_recovery=true schema_validation=true late_composite=true retained_snapshot=true exact_retry=true adoption=true"
        );
    }
}
