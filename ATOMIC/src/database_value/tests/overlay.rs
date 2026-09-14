use super::*;

#[test]
fn speculative_index_sharing_scales_with_paths_and_selective_ranges() {
    std::thread::Builder::new().stack_size(256 * 1024).spawn(|| {
        fn indexes(value: &DatabaseValue) -> &OverlayIndexes {
            let ReadBasis::TransactionOverlay(overlay) = &value.basis else { panic!("overlay expected") };
            assert!(!matches!(overlay.base.basis, ReadBasis::TransactionOverlay(_)));
            &overlay.indexes
        }
        fn append(base: DatabaseValue, entity: u64, instant: i64) -> DatabaseValue {
            let basis = base.basis_t() + 1;
            let tx = t_to_tx(basis).unwrap();
            let schema = base.schema_arc();
            let frontier = base.eidx_frontier() + 2;
            let mut datoms = vec![
                Datom { entity, attribute: AMOUNT, value: Value::Long(instant), tx, added: true },
                Datom { entity: tx, attribute: crate::DB_TX_INSTANT as u32, value: Value::Instant(instant), tx, added: true },
            ];
            datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
            DatabaseValue::transaction_overlay(base, datoms.into(), schema, basis, frontier, instant).unwrap()
        }
        let mut schema = Schema::new();
        let mut amount = Attribute::new(AMOUNT, Keyword::new("item", "amount"), ValueType::Long, Cardinality::One);
        amount.indexed = true;
        schema.install(amount).unwrap();
        let original = Database::new(schema).unwrap().database_value();
        let original_basis = original.basis_t();
        let entity = make_eid(USER_PARTITION, 100_000).unwrap();
        let mut value = original.clone();
        let started = std::time::Instant::now();
        for depth in 1..=4096 {
            value = append(value, entity + depth, depth as i64);
            if [128, 512, 2048, 4096].contains(&depth) {
                let (len, height, work) = indexes(&value).metrics();
                let prefix = IndexPrefix::Avet { attribute: AMOUNT, value: Some(Value::Long((depth / 2) as i64)), entity: None };
                let mut cursor = indexes(&value).cursor(false, IndexOrder::Avet, |datom| compare_prefix(datom, &prefix), false, Some(prefix.clone()));
                let selected: Vec<_> = cursor.by_ref().collect();
                assert_eq!(selected.len(), 1);
                assert!(cursor.visited() <= u64::from(height) * 3 + 3);
                assert!(work.nodes_created < len as u64 * u64::from(height + 4));
                assert_eq!(value.datoms_with_prefix(&prefix).unwrap().len(), 1);
                eprintln!("overlay depth={depth} history_datoms={len} history_height={height} cumulative_history_nodes_created={} cumulative_history_comparisons={} selective_avet_nodes_visited={} resident_inline_index_and_datom_bytes={} cumulative_elapsed_us={}; byte estimate excludes Arc/allocator headers and ident heaps", work.nodes_created, work.comparisons, cursor.visited(), indexes(&value).resident_bytes(), started.elapsed().as_micros());
            }
        }
        let base_nodes = indexes(&value).node_ids();
        let payload = indexes(&value).cursor(true, IndexOrder::Eavt, |_| Ordering::Equal, false, None).next().unwrap();
        let shared_payload = Arc::downgrade(&payload);
        drop(payload);
        let mut branches = Vec::new();
        let mut all_nodes = base_nodes.clone();
        let mut discarded_payloads = Vec::new();
        for branch in 0..128 {
            let fork = append(value.clone(), entity + 10_000 + branch, 5_000 + branch as i64);
            let nodes = indexes(&fork).node_ids();
            let shared = nodes.intersection(&base_nodes).count();
            assert!(shared > base_nodes.len() * 99 / 100);
            all_nodes.extend(nodes);
            let prefix = IndexPrefix::Eavt { entity: entity + 10_000 + branch, attribute: Some(AMOUNT), value: None };
            let datom = indexes(&fork).cursor(false, IndexOrder::Eavt, |datom| compare_prefix(datom, &prefix), false, Some(prefix.clone())).next().unwrap();
            discarded_payloads.push(Arc::downgrade(&datom));
            branches.push(fork);
        }
        let extra = all_nodes.len() - base_nodes.len();
        assert!(extra < 128 * 400);
        assert!(discarded_payloads.iter().all(|weak| weak.upgrade().is_some()));
        let drop_started = std::time::Instant::now();
        drop(branches);
        assert!(discarded_payloads.iter().all(|weak| weak.upgrade().is_none()));
        assert!(shared_payload.upgrade().is_some());
        eprintln!("overlay width=128 base_live_index_nodes={} retained_extra_path_nodes={extra} branch_drop_us={} discarded_new_payloads=128; prior payload stays owned", base_nodes.len(), drop_started.elapsed().as_micros());
        let last_drop = std::time::Instant::now();
        drop(value);
        assert!(shared_payload.upgrade().is_none());
        eprintln!("overlay depth=4096 final_drop_us={} stack_bytes=262144", last_drop.elapsed().as_micros());
        assert_eq!(original.basis_t(), original_basis);
    }).unwrap().join().unwrap();
}

#[test]
fn transaction_overlay_matches_eager_successor_without_materializing_the_base() {
    let (report, overlay, first, second, old, new, spare) = overlay_fixture();
    let eager = report.db_after.database_value();

    assert_eq!(overlay.basis_t(), report.db_after.basis_t());
    assert_eq!(overlay.eidx_frontier(), report.db_after.eidx_frontier());
    assert_eq!(overlay.last_tx_instant(), Some(4_000));
    assert_eq!(overlay.schema(), report.db_after.schema());

    // Ident lookup is an assertion-history cache, not a projection of
    // current :db/ident facts. The transaction-local assertion wins over
    // the base alias without cloning the complete base IdentIndex.
    assert_eq!(report.db_before.entid(&old), Some(first));
    assert_eq!(overlay.entid(&old), Some(second));
    assert_eq!(overlay.entid(&new), Some(first));
    assert_eq!(overlay.entid(&spare), Some(second));
    assert_eq!(overlay.ident(first), Some(&new));
    assert_eq!(overlay.ident(second), Some(&old));
    assert_eq!(overlay.entid(&old), eager.entid(&old));
    assert_eq!(overlay.ident(first), eager.ident(first));
    assert_eq!(overlay.ident(second), eager.ident(second));

    // Enabling :db/index makes pre-existing facts visible in successor
    // AVET. The exact 1.0M retraction must not hide equal-magnitude 1.00M.
    let amount_eavt = IndexPrefix::Eavt {
        entity: first,
        attribute: Some(AMOUNT),
        value: None,
    };
    let amount_aevt = IndexPrefix::Aevt {
        attribute: AMOUNT,
        entity: Some(first),
        value: None,
    };
    let amount_avet = IndexPrefix::Avet {
        attribute: AMOUNT,
        value: Some(decimal("1.0")),
        entity: None,
    };
    let link_vaet = IndexPrefix::Vaet {
        value: Value::Ref(second),
        attribute: Some(LINK),
        entity: None,
    };
    for prefix in [&amount_eavt, &amount_aevt, &amount_avet, &link_vaet] {
        assert_prefix_matches_eager(&overlay, &eager, prefix);
    }

    let amounts = overlay.datoms_with_prefix(&amount_eavt).unwrap();
    assert_eq!(amounts.len(), 1);
    assert!(amounts[0].value.stored_eq(&decimal("1.00")));
    assert!(!amounts[0].value.stored_eq(&decimal("1.0")));

    // Disabling :db/index suppresses both current and historical AVET
    // membership even though AEVT continues to retain the underlying data.
    let mut unindexed = report.db_after.schema().attribute(AMOUNT).unwrap().clone();
    unindexed.indexed = false;
    let unindexed_report = report
        .db_after
        .with(&[TxOp::AlterAttribute(unindexed)], 5_000)
        .unwrap();
    let unindexed_overlay = overlay_for(&unindexed_report, 5_000);
    let unindexed_eager = unindexed_report.db_after.database_value();
    assert_prefix_matches_eager(&unindexed_overlay, &unindexed_eager, &amount_avet);

    // This is the second material alteration of AMOUNT. Both overlay read
    // shapes must replace the first hook's current transaction coordinate
    // while retaining both immutable events in history.
    let alter_hook = IndexPrefix::Eavt {
        entity: crate::DB_PART_DB,
        attribute: Some(crate::DB_ALTER_ATTRIBUTE as u32),
        value: Some(Value::Ref(u64::from(AMOUNT))),
    };
    let current_hook = unindexed_overlay.datoms_with_prefix(&alter_hook).unwrap();
    assert_eq!(current_hook.len(), 1);
    assert_eq!(
        current_hook[0].tx,
        t_to_tx(unindexed_report.db_after.basis_t()).unwrap()
    );
    let scanned_hook = unindexed_overlay
        .datoms(IndexOrder::Eavt)
        .unwrap()
        .into_iter()
        .filter(|datom| {
            datom.entity == crate::DB_PART_DB
                && datom.attribute == crate::DB_ALTER_ATTRIBUTE as u32
                && datom.value == Value::Ref(u64::from(AMOUNT))
        })
        .collect::<Vec<_>>();
    assert_same_stored_datoms(&scanned_hook, &current_hook);
    assert_same_stored_datoms(
        &current_hook,
        &unindexed_eager.datoms_with_prefix(&alter_hook).unwrap(),
    );
    assert_eq!(
        unindexed_overlay
            .clone()
            .history()
            .datoms_with_prefix(&alter_hook)
            .unwrap()
            .len(),
        2
    );
    assert!(
        unindexed_overlay
            .datoms_with_prefix(&amount_avet)
            .unwrap()
            .is_empty()
    );
    assert!(
        unindexed_overlay
            .history()
            .datoms_with_prefix(&amount_avet)
            .unwrap()
            .is_empty()
    );
}

#[test]
fn transaction_overlay_only_backfills_aevt_when_avet_is_enabled() {
    let (enabled_report, enabled, first, ..) = overlay_fixture();
    let prefix = IndexPrefix::Avet {
        attribute: AMOUNT,
        value: Some(decimal("1.0")),
        entity: None,
    };
    assert_eq!(
        overlay_source_prefix(&enabled, &prefix),
        IndexPrefix::Aevt {
            attribute: AMOUNT,
            entity: None,
            value: None,
        }
    );

    let steady_report = enabled_report
        .db_after
        .with(
            &[TxOp::Add {
                entity: EntityRef::Id(first),
                attribute: AMOUNT,
                value: decimal("2.0").into(),
            }],
            4_500,
        )
        .unwrap();
    let steady = overlay_for(&steady_report, 4_500);
    assert_eq!(overlay_source_prefix(&steady, &prefix), prefix);

    let mut unindexed = enabled_report
        .db_after
        .schema()
        .attribute(AMOUNT)
        .unwrap()
        .clone();
    unindexed.indexed = false;
    let disabled_report = enabled_report
        .db_after
        .with(&[TxOp::AlterAttribute(unindexed)], 5_000)
        .unwrap();
    let disabled = overlay_for(&disabled_report, 5_000);
    assert_eq!(overlay_source_prefix(&disabled, &prefix), prefix);
}

#[test]
fn transaction_overlay_streams_unbounded_reads_and_rejects_invalid_construction() {
    let (report, overlay, ..) = overlay_fixture();
    let eager = report.db_after.database_value();
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_same_stored_datoms(
            &overlay.datoms(order).unwrap(),
            &eager.datoms(order).unwrap(),
        );
        assert_same_stored_datoms(
            &overlay.clone().history().datoms(order).unwrap(),
            &eager.clone().history().datoms(order).unwrap(),
        );
    }

    // A fully unbound data pattern is the observable path that exposed
    // the former overlay-only limitation: eager and native values could
    // scan it, while an entity predicate's exact db-after could not.
    let entity = Variable::new("e").unwrap();
    let attribute = Variable::new("a").unwrap();
    let value = Variable::new("v").unwrap();
    let query = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(entity.clone()),
            FindElement::Variable(attribute.clone()),
            FindElement::Variable(value.clone()),
        ]),
        vec![Clause::Pattern(Box::new(DataPattern::new(
            Term::Variable(entity),
            Term::Variable(attribute),
            Term::Variable(value),
        )))],
    );
    assert_eq!(
        overlay
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
        eager
            .query(&query, &[], &QueryControl::default())
            .unwrap()
            .result,
    );

    let chained = DatabaseValue::transaction_overlay(
        overlay.clone(),
        Arc::default(),
        Arc::new(overlay.schema().clone()),
        overlay.basis_t() + 1,
        overlay.eidx_frontier(),
        5_000,
    )
    .unwrap();
    assert_eq!(chained.basis_t(), overlay.basis_t() + 1);

    let mut reversed = report.tx_data.clone();
    reversed.reverse();
    let noncanonical = DatabaseValue::transaction_overlay(
        report.db_before.database_value(),
        Arc::from(reversed),
        Arc::new(report.db_after.schema().clone()),
        report.db_after.basis_t(),
        report.db_after.eidx_frontier(),
        4_000,
    )
    .unwrap_err();
    assert_eq!(noncanonical.code, "database/overlay-noncanonical-datoms");
}
