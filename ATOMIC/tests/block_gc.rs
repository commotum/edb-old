//! Complete incremental collection paths on fresh opaque providers.
mod common;

use atomic_core::storage::ownership::{BlockCollector, CollectionPhase, CollectionStats};
use atomic_core::storage::{BlockDatabase, CasOutcome, PgBlockStore};
use atomic_core::{
    Attribute, Cardinality, DatabaseCatalog, EntityRef, ErrorCategory, Keyword, OperationContext,
    OperationKind, Peer, PostgresConnectionConfig, Schema, SemanticError, TransactionRequest, TxOp,
    Value, ValueType,
};
use std::time::{Duration, Instant};

fn fixture(label: &str) -> Option<(common::PostgresFixture, PostgresConnectionConfig)> {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP PostgreSQL GC: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let fixture = common::PostgresFixture::new(&url, label);
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    Some((fixture, config))
}

fn collect(collector: &mut BlockCollector, age: Duration, label: &str) -> CollectionStats {
    let start = Instant::now();
    let context = OperationContext::new(OperationKind::Transaction);
    let guard = context.enter();
    let mut total = CollectionStats::default();
    for calls in 1..=64 {
        let progress = collector.advance(age, 4096).unwrap();
        total.ownership_steps += progress.stats.ownership_steps;
        total.newly_owned_objects += progress.stats.newly_owned_objects;
        total.ownership_payload_bytes += progress.stats.ownership_payload_bytes;
        total.metadata_objects_protected += progress.stats.metadata_objects_protected;
        total.objects_examined += progress.stats.objects_examined;
        total.objects_removed += progress.stats.objects_removed;
        total.stored_bytes_removed += progress.stats.stored_bytes_removed;
        total.events_removed += progress.stats.events_removed;
        if progress.phase == CollectionPhase::Complete {
            let io = context.snapshot();
            eprintln!(
                "BLOCK_GC_SAMPLE {label} calls={calls} elapsed_ms={} sql_calls={} read_bytes={} write_bytes={} newly_owned={} ownership_bytes={} metadata_protected={} examined={} removed={}",
                start.elapsed().as_millis(),
                io.sql_calls,
                io.known_payload_read_bytes,
                io.known_payload_write_bytes,
                total.newly_owned_objects,
                total.ownership_payload_bytes,
                total.metadata_objects_protected,
                total.objects_examined,
                total.objects_removed,
            );
            drop(guard);
            return total;
        }
    }
    panic!("tiny GC fixture did not complete within its bounded advance calls");
}

fn add(key: &str, entity: EntityRef, value: i64) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity,
            attribute: 1000,
            value: Value::Long(value).into(),
        }],
    )
}

#[test]
fn service_peer_and_exact_receipts_survive_incremental_collection_and_reopen() {
    let Some((fixture, config)) = fixture("block_gc_live") else {
        return;
    };
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1000,
            Keyword::new("item", "value"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    BlockDatabase::create(&config, "live", schema).unwrap();
    let writer = common::start_service(&fixture.connection, "live");
    let request = add("first", EntityRef::Temp("item".into()), 7);
    let first = writer
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    let entity = first.tempids["item"];
    let second = writer
        .client()
        .transact(
            add("second", EntityRef::Id(entity), 8),
            Duration::from_secs(10),
        )
        .unwrap();
    let peer = Peer::connect(&fixture.connection, "live", 0).unwrap();
    let held = peer.database_value();
    let before_key = first.db_before.snapshot_key().unwrap();
    let after_key = first.db_after.snapshot_key().unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let orphan = store
        .put(b"abandoned upload without any published owner")
        .unwrap();
    let mut collector = BlockCollector::connect(&config).unwrap();
    let initial = collect(&mut collector, Duration::ZERO, "live-first");
    assert!(initial.newly_owned_objects > 0);
    assert!(initial.ownership_payload_bytes > 0);
    assert!(initial.objects_removed > 0);
    assert!(initial.events_removed > 0);
    assert_eq!(store.get(orphan).unwrap(), None);
    assert_eq!(held.values(entity, 1000).unwrap(), vec![Value::Long(8)]);
    assert_eq!(
        first.db_after.values(entity, 1000).unwrap(),
        vec![Value::Long(7)]
    );
    assert_eq!(
        second.db_before.values(entity, 1000).unwrap(),
        vec![Value::Long(7)]
    );
    assert_eq!(first.db_before.snapshot_key().unwrap(), before_key);
    assert_eq!(first.db_after.snapshot_key().unwrap(), after_key);
    writer.shutdown();

    let reopened = common::start_service(&fixture.connection, "live");
    let replay = reopened
        .client()
        .transact(request, Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, first.basis_t);
    assert_eq!(replay.tx_hash, first.tx_hash);
    assert_eq!(replay.tempids, first.tempids);
    assert_eq!(replay.tx_data, first.tx_data);
    assert_eq!(replay.db_before.snapshot_key().unwrap(), before_key);
    assert_eq!(replay.db_after.snapshot_key().unwrap(), after_key);
    let repeated = collect(&mut collector, Duration::ZERO, "live-reopened");
    assert!(
        repeated.ownership_payload_bytes < initial.ownership_payload_bytes,
        "shared live subgraphs must use cached child ownership rather than re-read all payloads"
    );
    assert_eq!(
        replay.db_after.values(entity, 1000).unwrap(),
        vec![Value::Long(7)]
    );
    assert_eq!(held.values(entity, 1000).unwrap(), vec![Value::Long(8)]);
    reopened.shutdown();
}

#[test]
fn interrupted_collection_resumes_rejects_displaced_checkpoint_and_respects_retirement_age() {
    let Some((_fixture, config)) = fixture("block_gc_resume") else {
        return;
    };
    let database = BlockDatabase::create(&config, "retiring", Schema::new()).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root = store.read_ref(&database.reference_key()).unwrap().unwrap();
    let root_id = root.value.as_deref().unwrap().try_into().unwrap();
    let mut collector = BlockCollector::connect(&config).unwrap();
    assert_eq!(
        collector.advance(Duration::ZERO, 1).unwrap().phase,
        CollectionPhase::Adding
    );
    let before = store.read_ref("ownership/state").unwrap().unwrap();
    let mut checks = 0;
    let error = collector
        .advance_with_control(Duration::ZERO, 16, &mut || {
            checks += 1;
            if checks == 3 {
                return Err(SemanticError::new(
                    ErrorCategory::Unavailable,
                    "test/gc-interrupted",
                    "bounded checkpoint interruption",
                ));
            }
            Ok(())
        })
        .unwrap_err();
    assert_eq!(error.code, "test/gc-interrupted");
    assert_eq!(
        store.read_ref("ownership/state").unwrap(),
        Some(before.clone())
    );
    assert!(store.get(root_id).unwrap().is_some());
    drop(collector);

    let mut reopened = BlockCollector::connect(&config).unwrap();
    let mut checks = 0;
    let error = reopened
        .advance_with_control(Duration::ZERO, 1, &mut || {
            checks += 1;
            if checks == 2 {
                assert!(matches!(
                    store
                        .compare_exchange(
                            "ownership/state",
                            Some(before.revision),
                            before.value.as_deref()
                        )
                        .unwrap(),
                    CasOutcome::Applied(_)
                ));
            }
            Ok(())
        })
        .unwrap_err();
    assert_eq!(
        error.category,
        ErrorCategory::Conflict,
        "a collector that lost checkpoint authority must not publish its progress"
    );
    collect(&mut reopened, Duration::ZERO, "resume");
    assert!(store.get(root_id).unwrap().is_some());

    let mut catalog = DatabaseCatalog::connect_configured(&config).unwrap();
    assert!(catalog.retire("retiring").unwrap().unwrap().retired);
    collect(
        &mut reopened,
        Duration::from_secs(24 * 60 * 60),
        "age-retained",
    );
    assert!(
        store.get(root_id).unwrap().is_some(),
        "unowned roots must respect minimum retirement age"
    );
    let expired = collect(&mut reopened, Duration::ZERO, "age-expired");
    assert!(expired.objects_removed > 0);
    assert_eq!(store.get(root_id).unwrap(), None);
    assert_eq!(
        catalog
            .require_active_id(&database.reference_key()[10..])
            .unwrap_err()
            .code,
        "catalog/database-retired"
    );
    assert_eq!(catalog.list_retired(None, 1).unwrap().len(), 1);
}
