//! Pending AVET membership is immutable snapshot state, not per-commit work.
use super::*;
use crate::{Attribute, Cardinality, EntityRef, Keyword, Schema, TxOp, Value, ValueType};

fn database(width: u32, unindexed: &[u32]) -> Database {
    let mut schema = Schema::new();
    for id in 1_000..1_000 + width {
        let mut attribute = Attribute::new(
            id,
            Keyword::new("readiness", format!("a{id}")),
            ValueType::Long,
            Cardinality::One,
        );
        attribute.indexed = !unindexed.contains(&id);
        schema.install(attribute).unwrap();
    }
    Database::new(schema).unwrap()
}

fn transaction(report: &crate::TxReport) -> DurableTransaction {
    DurableTransaction {
        database_id: "readiness-sharing".into(),
        basis_t: report.db_after.basis_t(),
        previous_hash: [7; 32],
        eidx_frontier: report.db_after.eidx_frontier(),
        tempids: report.tempids.clone(),
        tx_data: report.tx_data.clone(),
    }
}

#[test]
fn readiness_ordinary_successors_share_wide_pending_sets() {
    for width in [32, 128, 512] {
        let database = database(width, &[]);
        let mut metadata = MetadataProjection::from_database(&database).unwrap();
        let initial: Arc<BTreeSet<_>> = Arc::new((1_000..1_000 + width).collect());
        let mut unready = Arc::clone(&initial);
        let report = database
            .with(
                &[TxOp::Add {
                    entity: EntityRef::Temp("item".into()),
                    attribute: 1_000,
                    value: Value::Long(7).into(),
                }],
                1,
            )
            .unwrap();
        let transaction = transaction(&report);
        let started = Instant::now();
        for _ in 0..64 {
            let (next_metadata, next_unready) = apply_metadata_and_avet_readiness(
                &metadata,
                &unready,
                std::slice::from_ref(&transaction),
                |_| panic!("ordinary data must not inspect base history"),
            )
            .unwrap();
            assert!(
                Arc::ptr_eq(&initial, &next_unready),
                "ordinary successor copied {width} pending attributes"
            );
            assert!(Arc::ptr_eq(&metadata.schema, &next_metadata.schema));
            metadata = next_metadata;
            unready = next_unready;
        }
        let elapsed = started.elapsed();
        // The oracle scan happens after the complete apply/drop measurement.
        assert_eq!(
            unready.as_ref(),
            &(1_000..1_000 + width).collect::<BTreeSet<_>>()
        );
        assert_eq!(initial.len(), width as usize);
        eprintln!(
            "READINESS_SHARING pending_attributes={width} complete_apply_drop_calls=64 ns_per_call={}",
            elapsed.as_nanos() / 64
        );
    }
}

#[test]
fn readiness_changes_copy_membership_without_mutating_old_snapshots_or_failures() {
    let database = database(3, &[1_001, 1_002]);
    let seeded = database
        .with(
            &[1_000, 1_001].map(|attribute| TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute,
                value: Value::Long(7).into(),
            }),
            1,
        )
        .unwrap()
        .db_after;
    let metadata = MetadataProjection::from_database(&seeded).unwrap();
    let initial = Arc::new(BTreeSet::from([1_000]));
    let changes: Vec<_> = [1_000, 1_001, 1_002]
        .into_iter()
        .map(|id| {
            let mut attribute = seeded.schema().attribute(id).unwrap().clone();
            attribute.indexed = id != 1_000;
            TxOp::AlterAttribute(attribute)
        })
        .collect();
    let report = seeded.with(&changes, 2).unwrap();
    let transaction = transaction(&report);
    let failure = apply_metadata_and_avet_readiness(
        &metadata,
        &initial,
        std::slice::from_ref(&transaction),
        |_| {
            Err(SemanticError::new(
                ErrorCategory::Unavailable,
                "test/readiness-history",
                "injected base read failure",
            ))
        },
    )
    .err()
    .expect("history failure must propagate");
    assert_eq!(failure.code, "test/readiness-history");
    assert_eq!(initial.as_ref(), &BTreeSet::from([1_000]));

    let mut probes = Vec::new();
    let (after, unready) =
        apply_metadata_and_avet_readiness(&metadata, &initial, &[transaction], |attribute| {
            probes.push(attribute);
            Ok(attribute == 1_001)
        })
        .unwrap();
    assert_eq!(probes, vec![1_001, 1_002]);
    assert_eq!(unready.as_ref(), &BTreeSet::from([1_001]));
    assert!(!Arc::ptr_eq(&initial, &unready));
    assert_eq!(initial.as_ref(), &BTreeSet::from([1_000]));
    assert_eq!(after.schema.as_ref(), report.db_after.schema());
    assert!(metadata.schema.attribute(1_000).unwrap().indexed);
    assert!(!metadata.schema.attribute(1_001).unwrap().indexed);
}

#[test]
fn readiness_noop_membership_changes_keep_the_existing_allocation() {
    let database = database(32, &[1_000]);
    let metadata = MetadataProjection::from_database(&database).unwrap();
    let initial = Arc::new((1_001..1_032).collect::<BTreeSet<_>>());
    let mut attribute = database.schema().attribute(1_000).unwrap().clone();
    attribute.indexed = true;
    let enabled = database
        .with(&[TxOp::AlterAttribute(attribute.clone())], 1)
        .unwrap();
    let mut probes = 0;
    let (enabled_metadata, enabled_unready) =
        apply_metadata_and_avet_readiness(&metadata, &initial, &[transaction(&enabled)], |id| {
            assert_eq!(id, 1_000);
            probes += 1;
            Ok(false)
        })
        .unwrap();
    assert_eq!(probes, 1);
    assert!(
        Arc::ptr_eq(&initial, &enabled_unready),
        "enabling an empty attribute need not copy unrelated pending backfills"
    );

    attribute.indexed = false;
    let disabled = enabled
        .db_after
        .with(&[TxOp::AlterAttribute(attribute)], 2)
        .unwrap();
    let (_, disabled_unready) = apply_metadata_and_avet_readiness(
        &enabled_metadata,
        &enabled_unready,
        &[transaction(&disabled)],
        |_| panic!("disabling AVET must not probe history"),
    )
    .unwrap();
    assert!(
        Arc::ptr_eq(&initial, &disabled_unready),
        "removing an absent pending attribute need not copy"
    );
    assert_eq!(initial.as_ref(), &(1_001..1_032).collect::<BTreeSet<_>>());
}
