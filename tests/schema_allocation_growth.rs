//! Public allocation correctness checks.

use atomic_core::{
    DB_TX_INSTANT, Database, EntityRef, IndexOrder, TxOp, USER_PARTITION, View, eid_to_eidx,
    eid_to_part,
};
use std::time::Instant;

fn retractions(count: usize) -> Vec<TxOp> {
    (0..count)
        .map(|number| TxOp::RetractEntity(EntityRef::Temp(format!("unused-{number:06}"))))
        .collect()
}

#[test]
fn retract_only_tempids_issue_real_frontier_without_live_user_facts() {
    let before = Database::bootstrap().unwrap();
    let frontier = before.eidx_frontier();
    let operations = retractions(128);
    let start = Instant::now();
    let eager = before.with(&operations, 10).unwrap();
    let exact = before.database_value().with(&operations, 10).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids.len(), 128);
    assert_eq!(eager.db_after.eidx_frontier(), frontier + 128);
    assert_eq!(exact.db_after.eidx_frontier(), frontier + 128);
    for (offset, entity) in eager.tempids.values().enumerate() {
        assert_eq!(eid_to_part(*entity).unwrap(), USER_PARTITION);
        assert_eq!(eid_to_eidx(*entity).unwrap(), frontier + offset as u64);
    }
    assert!(
        eager
            .tx_data
            .iter()
            .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
    );
    assert!(
        eager
            .db_after
            .datoms(View::Current, IndexOrder::Eavt)
            .iter()
            .all(|datom| eid_to_part(datom.entity).unwrap() != USER_PARTITION)
    );
    assert_eq!(before.eidx_frontier(), frontier);
    eprintln!(
        "retract-only public eager+exact:128 real tempids, frontier {frontier}->{}, zero live user facts; complete {:?}",
        eager.db_after.eidx_frontier(),
        start.elapsed()
    );
}

#[test]
fn reserved_and_ordinary_domains_do_not_spend_each_others_indices() {
    let before = Database::bootstrap().unwrap();
    let text = r#"[{:db/id "a-schema" :db/ident :growth/field :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
                   {:db/id "z-ordinary" :db/ident :growth/entity}]"#;
    let eager = before.with_edn(text, 1).unwrap();
    let exact = before.database_value().with_edn(text, 1).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids["a-schema"], 1_000);
    assert_eq!(eid_to_eidx(eager.tempids["z-ordinary"]).unwrap(), 1_000);
    assert_eq!(
        eid_to_part(eager.tempids["z-ordinary"]).unwrap(),
        USER_PARTITION
    );
    assert_eq!(eager.db_after.eidx_frontier(), 1_001);
    let later = eager.db_after.with_edn(r#"[{:db/id "schema" :db/ident :growth/later :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#, 2).unwrap();
    assert_eq!(later.tempids["schema"], 1_001);
    eager.db_after.validate_invariants().unwrap();
    later.db_after.validate_invariants().unwrap();
}

#[test]
fn explicit_reserved_claims_precede_allocation_and_survive_no_op_successors() {
    let before = Database::bootstrap()
        .unwrap()
        .with(&retractions(32), 1)
        .unwrap()
        .db_after;
    let text = r#"[{:db/id "schema" :db/ident :growth/claimed :db/valueType :db.type/long :db/cardinality :db.cardinality/one}
                   [:db/retractEntity 1010]]"#;
    let eager = before.with_edn(text, 2).unwrap();
    let exact = before.database_value().with_edn(text, 2).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tempids["schema"], 1_011);
    assert!(eager.tx_data.iter().all(|datom| datom.entity != 1_010));
    let no_op = eager
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(1_020))], 3)
        .unwrap();
    let exact_no_op = exact
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(1_020))], 3)
        .unwrap();
    assert!(
        no_op
            .tx_data
            .iter()
            .all(|datom| u64::from(datom.attribute) == DB_TX_INSTANT)
    );
    let next_text = r#"[{:db/id "next" :db/ident :growth/next :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#;
    let next = no_op.db_after.with_edn(next_text, 4).unwrap();
    let exact_next = exact_no_op.db_after.with_edn(next_text, 4).unwrap();
    assert_eq!(next.tempids, exact_next.tempids);
    assert_eq!(next.tempids["next"], 1_021);
    assert_eq!(before.eidx_frontier(), 1_032);
    assert_eq!(next.db_after.eidx_frontier(), 1_032);
}

#[test]
fn stored_ref_and_tuple_ref_operands_reserve_ids_but_numeric_slots_do_not() {
    let before = Database::bootstrap().unwrap().with_edn(r#"[
        {:db/id "ref" :db/ident :growth/ref :db/valueType :db.type/ref :db/cardinality :db.cardinality/one}
        {:db/id "tuple" :db/ident :growth/tuple :db/valueType :db.type/tuple :db/tupleTypes [:db.type/ref :db.type/long] :db/cardinality :db.cardinality/one}]
        "#, 1).unwrap().db_after.with(&retractions(32), 2).unwrap().db_after;
    let text = r#"[{:db/id "entity" :growth/ref 1010 :growth/tuple [1015 1000000]}
                   {:db/id "schema" :db/ident :growth/after-refs :db/valueType :db.type/long :db/cardinality :db.cardinality/one}]"#;
    let eager = before.with_edn(text, 3).unwrap();
    let exact = before.database_value().with_edn(text, 3).unwrap();
    assert_eq!(eager.tempids, exact.tempids);
    assert_eq!(eager.tx_data, exact.tx_data);
    assert_eq!(eager.tempids["schema"], 1_016);
    assert_eq!(
        eid_to_eidx(eager.tempids["entity"]).unwrap(),
        before.eidx_frontier()
    );
}
