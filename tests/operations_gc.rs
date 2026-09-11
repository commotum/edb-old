mod common;
use atomic_core::storage::ownership::CollectionPhase;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::sync::{Arc, atomic::AtomicBool};
use std::time::Duration;

#[test]
fn operator_preview_is_read_only_and_collection_reports_bounded_resumable_work() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "operator_gc");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    BlockDatabase::create(&config, "live", Schema::new()).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let orphan = store.put(b"unpublished operator-test upload").unwrap();
    let before = store.list_refs(None, 128).unwrap();
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    let context = OperationContext::new(OperationKind::Query);
    let guard = context.enter();
    let preview = operator.garbage_inventory(Duration::ZERO).unwrap();
    assert!(!preview.applied);
    assert_eq!(preview.phase, None);
    assert!(preview.stored_objects.unwrap() > 0);
    assert!(preview.pending_events.unwrap() > 0);
    assert_eq!(preview.collection.objects_removed, 0);
    assert_eq!(context.snapshot().known_payload_write_bytes, 0);
    drop(guard);
    assert_eq!(store.list_refs(None, 128).unwrap(), before);
    let first = operator.collect_garbage(Duration::ZERO).unwrap();
    assert!(first.applied);
    assert_ne!(first.phase, Some(CollectionPhase::Complete));
    assert_eq!(
        first.stored_objects, None,
        "bounded collection does not add a global inventory scan"
    );
    let mut complete = false;
    let mut removed = 0;
    for _ in 0..64 {
        let progress = operator.collect_garbage(Duration::ZERO).unwrap();
        removed += progress.collection.objects_removed;
        if progress.phase == Some(CollectionPhase::Complete) {
            assert!(
                progress.authorization.is_some(),
                "settled cycles report their bounded authorization-maintenance page"
            );
            complete = true;
            break;
        }
    }
    assert!(complete && removed > 0);
    assert_eq!(store.get(orphan).unwrap(), None);
    let checkpoint = store.read_ref("ownership/state").unwrap();
    let control =
        MaintenanceControl::new(Duration::ZERO, Arc::new(AtomicBool::new(false))).unwrap();
    control.cancel();
    let mut canceled = PostgresOperator::connect_configured(&config)
        .unwrap()
        .with_maintenance_control(control);
    assert_eq!(
        canceled.collect_garbage(Duration::ZERO).unwrap_err().code,
        "maintenance/canceled"
    );
    assert_eq!(store.read_ref("ownership/state").unwrap(), checkpoint);
}
