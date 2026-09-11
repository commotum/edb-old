mod common;
use atomic_core::storage::{BlockDatabase, PgBlockStore};
use atomic_core::*;
use std::time::Duration;

#[test]
fn retired_reclamation_checks_identity_and_keeps_held_shared_values() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "operator_retirement");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let first = BlockDatabase::create(&config, "first", Schema::new()).unwrap();
    BlockDatabase::create(&config, "second", Schema::new()).unwrap();
    let mut catalog = DatabaseCatalog::connect_configured(&config).unwrap();
    let entry = catalog.resolve("first").unwrap();
    let peer = Peer::connect(&fixture.connection, "first", 0).unwrap();
    let held = peer.database_value();
    let expected = held.datoms(IndexOrder::Eavt).unwrap();
    let mut store = PgBlockStore::connect(&config).unwrap();
    let root = store.read_ref(&first.reference_key()).unwrap().unwrap();
    let root_id = root.value.as_deref().unwrap().try_into().unwrap();
    let mut operator = PostgresOperator::connect_configured(&config).unwrap();
    assert_eq!(
        operator
            .reclaim_retired_database(&entry.database_id, &entry.lineage_id, Duration::ZERO)
            .unwrap_err()
            .code,
        "operations/database-not-retired"
    );
    catalog.retire("first").unwrap();
    assert_eq!(
        operator
            .reclaim_retired_database(&entry.database_id, "other", Duration::ZERO)
            .unwrap_err()
            .code,
        "catalog/identity-mismatch"
    );
    let checkpoint = store.read_ref("ownership/state").unwrap();
    let preview = operator
        .preview_retired_database_reclamation(&entry.database_id, &entry.lineage_id, Duration::ZERO)
        .unwrap();
    assert!(!preview.applied);
    assert_eq!(store.read_ref("ownership/state").unwrap(), checkpoint);
    let mut complete = false;
    for _ in 0..64 {
        let progress = operator
            .reclaim_retired_database(&entry.database_id, &entry.lineage_id, Duration::ZERO)
            .unwrap();
        assert!(progress.applied);
        if progress.cycle_complete {
            complete = true;
            break;
        }
    }
    assert!(complete);
    assert!(
        store.get(root_id).unwrap().is_some(),
        "cycle completion does not revoke held root owners"
    );
    assert_eq!(held.datoms(IndexOrder::Eavt).unwrap(), expected);
    assert_eq!(
        Peer::connect(&fixture.connection, "second", 0)
            .unwrap()
            .database_value()
            .datoms(IndexOrder::Eavt)
            .unwrap(),
        expected
    );
    assert_eq!(
        catalog
            .require_active_id(&entry.database_id)
            .unwrap_err()
            .code,
        "catalog/database-retired"
    );
}
