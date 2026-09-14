use super::*;

#[test]
fn raw_boundaries_resolve_idents_and_lookup_refs_in_every_index_order() {
    let (value, alice, bob, alice_ident, bob_ident) = raw_boundary_fixture();
    let alice_lookup = EntityIdentifier::Lookup {
        attribute: AttributeName::Ident(Keyword::new("boundary", "email")),
        value: Value::String("alice@example.test".into()),
    };
    let bob_lookup = EntityIdentifier::Lookup {
        attribute: AttributeName::Id(BOUNDARY_EMAIL),
        value: Value::String("bob@example.test".into()),
    };
    let link = AttributeName::Ident(Keyword::new("boundary", "link"));
    let t = value.basis_t();
    let tx = t_to_tx(t).unwrap();

    assert_eq!(
        value
            .eavt_boundary(IndexComponents::Four(
                alice_lookup.clone(),
                link.clone(),
                RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident.clone())),
                IndexTransaction::T(t),
            ))
            .unwrap(),
        IndexBoundary::Eavt(IndexComponents::Four(
            alice,
            BOUNDARY_LINK,
            Value::Ref(bob),
            IndexTransaction::T(t),
        ))
    );
    assert_eq!(
        value
            .aevt_boundary(IndexComponents::Four(
                link.clone(),
                EntityIdentifier::Ident(alice_ident.clone()),
                RawIndexValue::Entity(bob_lookup.clone()),
                IndexTransaction::Tx(tx),
            ))
            .unwrap(),
        IndexBoundary::Aevt(IndexComponents::Four(
            BOUNDARY_LINK,
            alice,
            Value::Ref(bob),
            IndexTransaction::Tx(tx),
        ))
    );
    assert_eq!(
        value
            .avet_boundary(IndexComponents::Four(
                link.clone(),
                RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident)),
                alice_lookup,
                IndexTransaction::T(t),
            ))
            .unwrap(),
        IndexBoundary::Avet(IndexComponents::Four(
            BOUNDARY_LINK,
            Value::Ref(bob),
            alice,
            IndexTransaction::T(t),
        ))
    );
    assert_eq!(
        value
            .vaet_boundary(IndexComponents::Four(
                RawIndexValue::Entity(bob_lookup),
                link,
                EntityIdentifier::Ident(alice_ident),
                IndexTransaction::Tx(tx),
            ))
            .unwrap(),
        IndexBoundary::Vaet(IndexComponents::Four(
            Value::Ref(bob),
            BOUNDARY_LINK,
            alice,
            IndexTransaction::Tx(tx),
        ))
    );

    assert_eq!(
        value
            .avet_boundary(IndexComponents::Two(
                AttributeName::Ident(Keyword::new("boundary", "email")),
                RawIndexValue::Stored(Value::String("alice@example.test".into())),
            ))
            .unwrap(),
        IndexBoundary::Avet(IndexComponents::Two(
            BOUNDARY_EMAIL,
            Value::String("alice@example.test".into()),
        ))
    );
}

#[test]
fn raw_tuple_boundaries_resolve_only_ref_slots_and_preserve_nil() {
    let (value, alice, bob, alice_ident, bob_ident) = raw_boundary_fixture();
    let bob_lookup = EntityIdentifier::Lookup {
        attribute: AttributeName::Id(BOUNDARY_EMAIL),
        value: Value::String("bob@example.test".into()),
    };

    assert_eq!(
        value
            .avet_boundary(IndexComponents::Two(
                AttributeName::Ident(Keyword::new("boundary", "homogeneous-refs")),
                RawIndexValue::Tuple(vec![
                    Some(RawIndexValue::Entity(EntityIdentifier::Ident(
                        alice_ident.clone(),
                    ))),
                    None,
                    Some(RawIndexValue::Entity(bob_lookup.clone())),
                ]),
            ))
            .unwrap(),
        IndexBoundary::Avet(IndexComponents::Two(
            BOUNDARY_HOMOGENEOUS_REFS,
            Value::Tuple(vec![Some(Value::Ref(alice)), None, Some(Value::Ref(bob))]),
        ))
    );

    assert_eq!(
        value
            .eavt_boundary(IndexComponents::Three(
                EntityIdentifier::Id(alice),
                AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                RawIndexValue::Tuple(vec![
                    Some(RawIndexValue::Entity(EntityIdentifier::Ident(alice_ident))),
                    Some(RawIndexValue::Stored(Value::String("middle".into()))),
                    Some(RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident))),
                ]),
            ))
            .unwrap(),
        IndexBoundary::Eavt(IndexComponents::Three(
            alice,
            BOUNDARY_HETEROGENEOUS,
            Value::Tuple(vec![
                Some(Value::Ref(alice)),
                Some(Value::String("middle".into())),
                Some(Value::Ref(bob)),
            ]),
        ))
    );
}

#[test]
fn raw_boundaries_reject_unresolved_wrong_typed_and_unqualified_inputs() {
    let (value, alice, _, _, bob_ident) = raw_boundary_fixture();
    let missing = EntityIdentifier::Ident(Keyword::new("boundary", "missing"));

    assert_eq!(
        value
            .eavt_boundary(IndexComponents::One(missing.clone()))
            .unwrap_err()
            .code,
        "index/unresolved-entity"
    );
    assert_eq!(
        value
            .eavt_boundary(IndexComponents::Three(
                EntityIdentifier::Id(alice),
                AttributeName::Id(BOUNDARY_TEXT),
                RawIndexValue::Entity(EntityIdentifier::Ident(bob_ident)),
            ))
            .unwrap_err()
            .code,
        "transaction/value-type"
    );
    assert_eq!(
        value
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                RawIndexValue::Tuple(vec![
                    Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                    None,
                    None,
                    None,
                ]),
            ))
            .unwrap_err()
            .code,
        "transaction/invalid-tuple-length"
    );
    assert_eq!(
        value
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(BOUNDARY_HETEROGENEOUS),
                RawIndexValue::Tuple(vec![
                    Some(RawIndexValue::Stored(Value::String("not-a-ref".into()))),
                    None,
                    Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                ]),
            ))
            .unwrap_err()
            .code,
        "transaction/invalid-tuple-element"
    );
    assert_eq!(
        value
            .avet_boundary(IndexComponents::Two(
                AttributeName::Id(BOUNDARY_HOMOGENEOUS_REFS),
                RawIndexValue::Tuple(vec![
                    Some(RawIndexValue::Entity(missing)),
                    Some(RawIndexValue::Entity(EntityIdentifier::Id(alice))),
                ]),
            ))
            .unwrap_err()
            .code,
        "index/unresolved-entity"
    );
    assert_eq!(
        value
            .vaet_boundary(IndexComponents::One(RawIndexValue::Stored(Value::String(
                "not-a-ref".into()
            ),)))
            .unwrap_err()
            .code,
        "index/vaet-value-not-ref"
    );
    assert_eq!(
        value
            .avet_boundary(IndexComponents::One(AttributeName::Id(BOUNDARY_TEXT)))
            .unwrap_err()
            .code,
        "index/attribute-not-in-avet"
    );
}

#[test]
fn raw_avet_boundary_can_use_a_speculative_attribute_backfill() {
    let (_, overlay, ..) = overlay_fixture();
    let boundary = overlay
        .avet_boundary(IndexComponents::One(AttributeName::Id(AMOUNT)))
        .unwrap();
    assert!(overlay.seek_cursor(&boundary).unwrap().next().is_some());
}
