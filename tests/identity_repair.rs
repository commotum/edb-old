use atomic_core::{
    Attribute, Cardinality, Clause, DataPattern, Database, DurableTransaction, EntityRef,
    FindElement, FindSpec, IndexOrder, Keyword, Query, QueryControl, QueryResult, QueryValue,
    Schema, Term, TxOp, TxValue, USER_PARTITION, Value, ValueType, Variable, View,
    decode_transaction, eid_to_eidx, eid_to_part, encode_transaction, make_eid, t_to_tx, tx_to_t,
};

const DB_TX_INSTANT: u32 = 50;
const NAME: u32 = 1_000;
const FRIEND: u32 = 1_001;
const FIRST_USER_EIDX: u64 = 1_002;

fn database() -> Database {
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            FRIEND,
            Keyword::new("person", "friend"),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();
    schema
        .install(Attribute::new(
            NAME,
            Keyword::new("person", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    Database::new(schema).unwrap()
}

fn add(entity: EntityRef, name: &str) -> TxOp {
    TxOp::Add {
        entity,
        attribute: NAME,
        value: TxValue::Scalar(Value::String(name.into())),
    }
}

#[test]
fn transaction_time_and_user_entities_have_disjoint_recovered_ids() {
    let report = database()
        .with(&[add(EntityRef::Temp("ada".into()), "Ada")], 1_000)
        .unwrap();
    let user = report.tempids["ada"];
    let tx = t_to_tx(2).unwrap();

    assert_eq!(eid_to_part(user).unwrap(), USER_PARTITION);
    assert_eq!(eid_to_eidx(user).unwrap(), FIRST_USER_EIDX);
    assert_eq!(tx_to_t(tx).unwrap(), 2);
    assert_ne!(user, tx);
    assert_ne!(
        make_eid(USER_PARTITION, FIRST_USER_EIDX).unwrap(),
        t_to_tx(FIRST_USER_EIDX).unwrap()
    );
    assert!(report.tx_data.iter().all(|datom| datom.tx == tx));
    assert!(report.tx_data.iter().any(|datom| {
        datom.entity == tx
            && datom.attribute == DB_TX_INSTANT
            && datom.value == Value::Instant(1_000)
    }));
}

#[test]
fn explicit_ids_must_be_issued_but_empty_issued_ids_can_be_reused() {
    let future = make_eid(USER_PARTITION, FIRST_USER_EIDX).unwrap();
    let error = database()
        .with(&[add(EntityRef::Id(future), "minted")], 1_000)
        .unwrap_err();
    assert_eq!(error.code, "transaction/invalid-entity-id");
    let scalar_ref_error = database()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(make_eid(USER_PARTITION, 42).unwrap()),
                attribute: FRIEND,
                value: TxValue::Scalar(Value::Ref(future)),
            }],
            1_000,
        )
        .unwrap_err();
    assert_eq!(scalar_ref_error.code, "transaction/invalid-entity-id");

    let allocated = database()
        .with(&[add(EntityRef::Temp("issued".into()), "issued")], 1_000)
        .unwrap();
    assert_eq!(allocated.tempids["issued"], future);
    let empty = allocated
        .db_after
        .with(&[TxOp::RetractEntity(EntityRef::Id(future))], 2_000)
        .unwrap()
        .db_after;
    assert!(empty.values(future, NAME).is_empty());

    let reused = empty
        .with(&[add(EntityRef::Id(future), "reused")], 3_000)
        .unwrap();
    assert_eq!(
        reused.db_after.values(future, NAME),
        vec![&Value::String("reused".into())]
    );
}

#[test]
fn logical_views_and_query_tx_bindings_keep_t_distinct_from_tx_ids() {
    let first = database()
        .with(&[add(EntityRef::Temp("p".into()), "Ada")], 1_000)
        .unwrap();
    let entity = first.tempids["p"];
    let second = first
        .db_after
        .with(&[add(EntityRef::Id(entity), "Grace")], 2_000)
        .unwrap()
        .db_after;

    assert_eq!(second.t_at_or_before_instant(1_500), 2);
    assert_eq!(second.t_at_or_after_instant(1_500), 3);
    assert_eq!(
        second
            .datoms(View::AsOf(2), IndexOrder::Eavt)
            .iter()
            .find(|datom| datom.entity == entity && datom.attribute == NAME)
            .map(|datom| &datom.value),
        Some(&Value::String("Ada".into()))
    );

    let tx_var = Variable::new("tx").unwrap();
    let mut pattern = DataPattern::new(
        Term::Constant(Value::Ref(entity)),
        Term::Constant(Value::Ref(u64::from(NAME))),
        Term::Constant(Value::String("Ada".into())),
    );
    pattern.transaction = Some(Term::Variable(tx_var.clone()));
    let query = Query::new(
        FindSpec::Scalar(FindElement::Variable(tx_var)),
        vec![Clause::Pattern(Box::new(pattern))],
    );
    let outcome = first
        .db_after
        .query(&query, &[], &QueryControl::default())
        .unwrap();
    assert_eq!(
        outcome.result,
        QueryResult::Scalar(Some(QueryValue::Scalar(Value::Ref(t_to_tx(2).unwrap()))))
    );
}

#[test]
fn durable_transactions_encode_basis_and_tx_entity_as_different_fields() {
    let report = database()
        .with(&[add(EntityRef::Temp("p".into()), "Ada")], 1_000)
        .unwrap();
    let transaction = DurableTransaction {
        database_id: "identity-test".into(),
        basis_t: report.db_after.basis_t(),
        previous_hash: [0; 32],
        eidx_frontier: report.db_after.eidx_frontier(),
        tempids: report.tempids,
        tx_data: report.tx_data,
    };
    let encoded = encode_transaction(&transaction).unwrap();
    let decoded = decode_transaction(&encoded).unwrap();
    assert_eq!(decoded.basis_t, 2);
    assert!(
        decoded
            .tx_data
            .iter()
            .all(|datom| datom.tx == t_to_tx(decoded.basis_t).unwrap())
    );

    let mut malformed = decoded;
    malformed.tx_data[0].tx = malformed.basis_t;
    assert_eq!(
        encode_transaction(&malformed).unwrap_err().code,
        "encoding/datom-transaction-mismatch"
    );
}

#[test]
fn issued_frontier_advances_for_transaction_entities_without_user_allocation() {
    let mut db = database();
    for instant in 1..=1_000 {
        db = db.with(&[], instant).unwrap().db_after;
    }
    assert_eq!(db.basis_t(), 1_001);
    assert_eq!(db.eidx_frontier(), FIRST_USER_EIDX);
    assert!(
        db.datoms(View::Current, IndexOrder::Eavt)
            .iter()
            .any(|datom| datom.entity == t_to_tx(1_001).unwrap())
    );

    // The first user allocation after t=1000 is beyond the transaction's
    // issued component and remains in another partition.
    let report = db
        .with(&[add(EntityRef::Temp("p".into()), "Ada")], 1_001)
        .unwrap();
    assert_eq!(
        eid_to_eidx(report.tempids["p"]).unwrap(),
        FIRST_USER_EIDX + 1
    );
    assert_ne!(report.tempids["p"], t_to_tx(FIRST_USER_EIDX).unwrap());
}
