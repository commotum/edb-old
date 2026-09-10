use super::*;
use crate::{DB_IDENT, DB_PART_DB, EntityRef, Keyword, TxOp, Value};

const LINEAGE: &str = "82d578ad-21e1-436e-8b9f-7039dd9d87d5";

fn content(report: &crate::TxReport) -> LineageTransactionContent {
    let transaction = DurableTransaction {
        database_id: LINEAGE.into(),
        basis_t: report.db_after.basis_t(),
        previous_hash: [0; 32],
        eidx_frontier: report.db_after.eidx_frontier(),
        tempids: report.tempids.clone(),
        tx_data: report.tx_data.clone(),
    };
    LineageTransactionContent::from_transaction_v2(
        LINEAGE,
        report.db_before.eidx_frontier(),
        report.db_before.reserved_allocation().unwrap(),
        report.db_after.reserved_allocation().unwrap(),
        &transaction,
    )
    .unwrap()
}

fn reserved_upsert() -> (
    BackupAllocationReplay,
    Database,
    LineageTransactionContent,
    u64,
) {
    let genesis = Database::bootstrap().unwrap();
    let name = Value::Keyword(Keyword::new("allocation", "existing"));
    let created = genesis
        .with(
            &[
                TxOp::ForcePartition {
                    tempid: "first".into(),
                    partition: EntityRef::Id(DB_PART_DB),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("first".into()),
                    attribute: DB_IDENT as u32,
                    value: name.clone().into(),
                },
            ],
            1,
        )
        .unwrap();
    let id = created.tempids["first"];
    assert_eq!(id, 1_000);
    let first = content(&created);
    let mut replay = BackupAllocationReplay::new(&genesis).unwrap();
    let before = replay
        .apply(
            &genesis,
            &first.to_transaction([0; 32]),
            first.reserved_frontier,
            false,
        )
        .unwrap();
    let upsert = created
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("existing-receipt-name".into()),
                attribute: DB_IDENT as u32,
                value: name.into(),
            }],
            2,
        )
        .unwrap();
    assert_eq!(upsert.tempids["existing-receipt-name"], id);
    let upsert_content = content(&upsert);
    assert!(upsert_content.allocations.is_empty());
    (replay, before, upsert_content, id)
}

#[test]
fn backup_replay_rejects_a_hash_valid_reserved_witness_below_its_prior_frontier() {
    let (mut replay, before, mut malformed, prior_id) = reserved_upsert();
    malformed.allocations.push(prior_id);
    // This is a valid standalone canonical envelope. Only the authenticated
    // predecessor can prove that this purported fresh allocation is old.
    let payload = malformed.encode().unwrap();
    let decoded = LineageTransactionContent::decode(&payload).unwrap();
    assert_eq!(
        decoded
            .validate_reserved_transition(replay.observed)
            .unwrap_err()
            .code,
        "generation/reserved-allocation-not-fresh"
    );
    let prior = replay.observed;
    let result = replay.apply(
        &before,
        &decoded.to_transaction([0; 32]),
        decoded.reserved_frontier,
        false,
    );
    let Err(error) = result else {
        panic!("backup replay accepted a reserved issuance witness below its predecessor");
    };
    assert_eq!(error.code, "generation/reserved-allocation-not-fresh");
    assert_eq!(replay.observed, prior);
    assert_eq!(before.basis_t(), 1);
}

#[test]
fn backup_replay_preserves_existing_id_upserts_outside_the_fresh_witness_list() {
    let (mut replay, before, content, prior_id) = reserved_upsert();
    let decoded = LineageTransactionContent::decode(&content.encode().unwrap()).unwrap();
    let canonical = decoded.to_transaction([0; 32]);
    assert!(canonical.tempids.is_empty());
    // Actual caller-named receipt aliases are validated separately; they are
    // not the synthetic issuance map consumed by this replay boundary.
    let after = replay
        .apply(&before, &canonical, decoded.reserved_frontier, false)
        .unwrap();
    assert_eq!(after.basis_t(), 2);
    assert_eq!(after.reserved_allocation(), before.reserved_allocation());
    assert_eq!(after.eidx_frontier(), before.eidx_frontier());
    assert_eq!(
        after.values(prior_id, DB_IDENT as u32),
        before.values(prior_id, DB_IDENT as u32)
    );
}

#[test]
fn backup_receipt_cannot_claim_a_reserved_id_beyond_its_authenticated_checkpoint() {
    let (_, before, _, prior_id) = reserved_upsert();
    let growth = before
        .with(
            &(0..128)
                .map(|i| TxOp::RetractEntity(EntityRef::Temp(format!("growth-{i:03}"))))
                .collect::<Vec<_>>(),
            2,
        )
        .unwrap();
    let upsert = growth
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("existing-receipt-name".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(Keyword::new("allocation", "existing")).into(),
            }],
            3,
        )
        .unwrap();
    let content = content(&upsert);
    let canonical = content.to_transaction([0; 32]);
    let prior_frontier = growth.db_after.eidx_frontier();
    let reserved = content.reserved_frontier.unwrap();
    assert!(reserved < prior_frontier);
    assert_eq!(upsert.tempids["existing-receipt-name"], prior_id);
    let mut receipt = BackupRequestRecord {
        lineage_id: LINEAGE.into(),
        log_generation: 1,
        basis: canonical.basis_t,
        transaction_hash: [0; 32],
        previous_hash: [0; 32],
        request_key_hash: [0; 32],
        digest: [0; 32],
        request_kind: 1,
        base_manifest_hash: None,
        receipt_tempids: upsert.tempids,
    };
    validate_backup_receipt(&canonical, &receipt, prior_frontier, Some(reserved)).unwrap();
    receipt
        .receipt_tempids
        .insert("unissued-reserved".into(), reserved);
    // V1 has no reserved checkpoint and retains its old ordinary-frontier
    // contract; v2 must not reinterpret that upper bound as reserved issuance.
    validate_backup_receipt(&canonical, &receipt, prior_frontier, None).unwrap();
    assert_eq!(
        validate_backup_receipt(&canonical, &receipt, prior_frontier, Some(reserved))
            .unwrap_err()
            .code,
        "backup/request-chain"
    );
}
