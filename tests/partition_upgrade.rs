//! Run against an isolated database created by the preserved pre-partition
//! executable, not a fresh database reinterpreted by the new binary.
mod common;
use atomic_core::{
    DB_INSTALL_PARTITION, DB_PART_DB, DB_PART_TX, DB_PART_USER, IndexOrder, Peer, PostgresIndexer,
    PostgresStore, TransactionRequest, Value, partition_vocabulary_upgrade_ops,
};
use std::time::Duration;

#[test]
fn actual_old_binary_genesis_upgrades_explicitly_and_retains_its_chain() {
    let Ok(url) = std::env::var("ATOMIC_PARTITION_LEGACY_URL") else {
        eprintln!("SKIPPED legacy binary PostgreSQL upgrade: ATOMIC_PARTITION_LEGACY_URL unset");
        return;
    };
    let name = "legacy-partitions";
    let mut admin = postgres::Client::connect(&url, postgres::NoTls).unwrap();
    let row = admin
        .query_one(
            "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id=$1",
            &[&name],
        )
        .unwrap();
    let genesis: Vec<u8> = row.get(0);
    let hash: Vec<u8> = row.get(1);
    assert_eq!(
        genesis.len(),
        4588,
        "fixture must come from the pre-partition executable"
    );
    assert_eq!(hash, atomic_core::sha256(&genesis));
    assert_eq!(
        hash.iter().map(|b| format!("{b:02x}")).collect::<String>(),
        "b99d04f25079c8646fc066975d6d9b952984a68e314d9d8299db62ed511cab75"
    );
    let peer = Peer::connect(&url, name, 16).unwrap();
    let initial = peer.db();
    // This witness is deliberately a one-time upgrade, not a fresh-vocabulary test.
    assert_eq!(initial.basis_t(), 0);
    assert!(
        initial
            .schema()
            .attribute(DB_INSTALL_PARTITION as u32)
            .is_err()
    );
    let service = common::start_service(&url, name);
    assert_eq!(Peer::connect(&url, name, 8).unwrap().basis_t(), 0);
    let ops = partition_vocabulary_upgrade_ops();
    let preview = initial.with(&ops, 10).unwrap();
    assert!(
        initial
            .schema()
            .attribute(DB_INSTALL_PARTITION as u32)
            .is_err()
    );
    let request = TransactionRequest::new("partition-vocabulary-upgrade/v1", ops)
        .comparing_basis(0)
        .with_tx_instant(10);
    let upgraded = service
        .client()
        .transact(request.clone(), Duration::from_secs(10))
        .unwrap();
    common::assert_same_information(&preview.db_after, &upgraded.db_after);
    assert_eq!(
        upgraded
            .db_after
            .values(DB_PART_DB, DB_INSTALL_PARTITION as u32)
            .unwrap(),
        vec![
            Value::Ref(DB_PART_DB),
            Value::Ref(DB_PART_TX),
            Value::Ref(DB_PART_USER)
        ]
    );
    assert_eq!(
        initial.collect_datoms(IndexOrder::Eavt).unwrap(),
        atomic_core::decode_genesis(&genesis).unwrap()
    );
    service.shutdown();
    PostgresIndexer::connect(&url, name)
        .unwrap()
        .consolidate()
        .unwrap();
    let service = common::start_service(&url, name);
    let replay = service
        .client()
        .transact(request, Duration::from_secs(10))
        .unwrap();
    assert_eq!(replay.tx_hash, upgraded.tx_hash);
    assert_eq!(replay.basis_t, 1);
    common::assert_same_information(&replay.db_after, &upgraded.db_after);
    service.shutdown();
    let recovered = PostgresStore::connect(&url).unwrap().recover(name).unwrap();
    common::assert_same_information(&recovered, &upgraded.db_after);
    let row = admin
        .query_one(
            "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id=$1",
            &[&name],
        )
        .unwrap();
    assert_eq!(row.get::<_, Vec<u8>>(0), genesis);
    assert_eq!(row.get::<_, Vec<u8>>(1), hash);
    println!(
        "LEGACY_PARTITION_UPGRADE_OK old_genesis_bytes=4588 basis_t=1 exact_retry=true retained_old=true indexed_recovery=true"
    );
}
