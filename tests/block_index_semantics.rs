//! Current service/index semantics preserved from the former SQL adapter witnesses.
mod common;
use atomic_core::{
    Attribute, Cardinality, Clause, DataPattern, Datom, EntityRef, FindElement, FindSpec,
    IndexOrder, Keyword, Peer, PeerSnapshot, Query, QueryControl, QueryEngine, QueryOutcome,
    QueryResult, QuerySource, QueryValue, Schema, Term, TxOp, TxValue, Unique, Value, ValueType,
    Variable, View,
};
use common::InformationSource;
use std::time::{SystemTime, UNIX_EPOCH};
const ITEM_NAME: u32 = 1000;
const ITEM_COUNT: u32 = 1001;
const ITEM_PARENT: u32 = 1002;
fn connection() -> Option<common::PostgresFixture> {
    std::env::var("ATOMIC_POSTGRES_URL")
        .ok()
        .map(|url| common::PostgresFixture::new(&url, "postgres_peer"))
}

fn unique(prefix: &str) -> String {
    format!(
        "{prefix}_{}_{}",
        std::process::id(),
        SystemTime::now()
            .duration_since(UNIX_EPOCH)
            .unwrap()
            .as_nanos()
    )
}

fn schema(no_history: bool) -> Schema {
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                ITEM_NAME,
                Keyword::new("item", "name"),
                ValueType::String,
                Cardinality::One,
            )
            .unique(Unique::Identity),
        )
        .unwrap();
    let mut count = Attribute::new(
        ITEM_COUNT,
        Keyword::new("item", "count"),
        ValueType::Long,
        Cardinality::One,
    );
    count.indexed = true;
    count.no_history = no_history;
    schema.install(count).unwrap();
    let mut parent = Attribute::new(
        ITEM_PARENT,
        Keyword::new("item", "parent"),
        ValueType::Ref,
        Cardinality::One,
    );
    parent.indexed = true;
    parent.no_history = no_history;
    schema.install(parent).unwrap();
    schema
}

fn retained_attribute_history(
    snapshot: &PeerSnapshot,
    order: IndexOrder,
    entity: u64,
    attribute: u32,
) -> Vec<Datom> {
    let mut datoms = snapshot
        .datoms(true, order)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == attribute)
        .collect::<Vec<_>>();
    datoms.sort_by(|left, right| left.cmp_in(right, IndexOrder::Eavt));
    datoms
}

fn assert_current_eq(left: &impl InformationSource, right: &impl InformationSource) {
    assert_eq!(left.test_basis_t(), right.test_basis_t());
    assert_eq!(left.test_eidx_frontier(), right.test_eidx_frontier());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.test_datoms(View::Current, order),
            right.test_datoms(View::Current, order)
        );
    }
}

fn assert_eager_native_query_differential(
    label: &str,
    query: &Query,
    eager: atomic_core::DatabaseValue,
    native: atomic_core::DatabaseValue,
) -> Vec<QueryOutcome> {
    let execute = |database, force_scan| {
        QueryEngine::execute(
            query,
            &[QuerySource {
                name: "$".into(),
                database,
            }],
            &[],
            &QueryControl {
                force_scan,
                ..QueryControl::default()
            },
        )
        .unwrap_or_else(|error| panic!("{label} failed (force_scan={force_scan}): {error}"))
    };
    let outcomes = vec![
        execute(eager.clone(), false),
        execute(eager, true),
        execute(native.clone(), false),
        execute(native, true),
    ];
    for outcome in &outcomes[1..] {
        assert_eq!(
            outcome.result, outcomes[0].result,
            "{label} diverged between eager/native or optimized/scan evaluation"
        );
    }
    assert!(
        outcomes[0].stats.datoms_examined <= outcomes[1].stats.datoms_examined,
        "{label} eager optimized path examined more datoms than force-scan"
    );
    assert!(
        outcomes[2].stats.datoms_examined <= outcomes[3].stats.datoms_examined,
        "{label} native optimized path examined more datoms than force-scan"
    );
    outcomes
}

fn populated(
    connection: &str,
    database_id: &str,
    no_history: bool,
    updates: i64,
) -> (common::TestStore, u64) {
    common::blocks::install(connection).unwrap();
    let mut store = common::TestStore::connect(connection).unwrap();
    let created = store
        .create_database(database_id, schema(no_history))
        .unwrap();
    let service = common::start_service(connection, database_id);
    let first = common::transact(
        &service,
        "create",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String(database_id.into())),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(0)),
            },
            TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: ITEM_PARENT,
                value: TxValue::Entity(EntityRef::Temp("item".into())),
            },
        ],
        1_000,
    );
    let entity = first.tempids["item"];
    let mut basis = first.basis_t;
    for value in 1..=updates {
        let receipt = common::transact(
            &service,
            &format!("update-{value}"),
            basis,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(value)),
            }],
            1_000 + value,
        );
        basis = receipt.basis_t;
    }
    service.shutdown();
    (store, entity)
}

#[test]
fn native_metadata_rebuild_and_recent_tail_preserve_ident_alias_lifecycle() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("peer_ident_projection");
    let (mut store, _) = populated(&connection, &database_id, false, 1);
    let initial = store.recover(&database_id).unwrap();
    let old = Keyword::new("status", "pending");
    let new = Keyword::new("status", "awaiting");
    let service = common::start_service(&connection, &database_id);
    let introduced = common::transact(
        &service,
        "introduce-ident",
        initial.basis_t(),
        &[TxOp::Add {
            entity: EntityRef::Temp("original".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(old.clone()).into(),
        }],
        2_000,
    );
    let original = introduced.tempids["original"];

    common::consolidate(&connection, &database_id).unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    let based = peer.snapshot();
    assert_eq!(based.entid(&old), Some(original));
    assert_eq!(based.ident(original), Some(&old));

    let renamed = common::transact(
        &service,
        "rename-ident",
        introduced.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(original),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(new.clone()).into(),
        }],
        2_001,
    );
    let repurposed = common::transact(
        &service,
        "repurpose-ident",
        renamed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Temp("replacement".into()),
            attribute: atomic_core::DB_IDENT as u32,
            value: Value::Keyword(old.clone()).into(),
        }],
        2_002,
    );
    let replacement = repurposed.tempids["replacement"];

    // The unconsolidated authenticated tail updates the same discardable
    // maps as a recovered base: the old name is repurposed while the renamed
    // entity keeps its latest canonical name.
    let tailed = peer.sync_snapshot().unwrap();
    assert_eq!(tailed.basis_t(), repurposed.basis_t);
    assert_eq!(tailed.entid(&old), Some(replacement));
    assert_eq!(tailed.entid(&new), Some(original));
    assert_eq!(tailed.ident(original), Some(&new));
    assert_eq!(tailed.ident(replacement), Some(&old));

    // Rebuilding a durable tree base from historical AEVT must produce the
    // identical observation without treating the discardable alias projection as authority.
    common::consolidate(&connection, &database_id).unwrap();
    assert!(peer.refresh_index().unwrap());
    let consolidated = peer.snapshot();
    assert_eq!(consolidated.entid(&old), Some(replacement));
    assert_eq!(consolidated.entid(&new), Some(original));
    assert_eq!(consolidated.ident(original), Some(&new));
    assert_eq!(consolidated.ident(replacement), Some(&old));

    service.shutdown();
    drop(store);
    let restarted = Peer::connect(&connection, &database_id, 16).unwrap();
    let snapshot = restarted.snapshot();
    assert_eq!(snapshot.entid(&old), Some(replacement));
    assert_eq!(snapshot.entid(&new), Some(original));
    assert_eq!(snapshot.ident(original), Some(&new));
    assert_eq!(snapshot.ident(replacement), Some(&old));
}

#[test]
fn no_history_consolidation_forgets_old_values_without_changing_current_state() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("nohistory");
    let (mut store, entity) = populated(&connection, &database_id, true, 5);
    let before_parent_replacement = store.recover(&database_id).unwrap();
    let service = common::start_service(&connection, &database_id);
    let parent_replacement = common::transact(
        &service,
        "replace-nohistory-parent",
        before_parent_replacement.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("replacement-parent".into()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String(format!("{database_id}-parent"))),
            },
            TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_PARENT,
                value: TxValue::Entity(EntityRef::Temp("replacement-parent".into())),
            },
        ],
        2_000,
    );
    let replacement_parent = parent_replacement.tempids["replacement-parent"];
    drop(parent_replacement);
    service.shutdown();
    let expected = store.recover(&database_id).unwrap();
    let authoritative_old: Vec<_> = expected
        .clone()
        .history()
        .datoms(IndexOrder::Eavt)
        .unwrap()
        .into_iter()
        .filter(|d| d.entity == entity && d.attribute == ITEM_COUNT)
        .collect();
    assert!(authoritative_old.len() > 1);
    let authoritative_parent: Vec<_> = expected
        .clone()
        .history()
        .datoms(IndexOrder::Eavt)
        .unwrap()
        .into_iter()
        .filter(|d| d.entity == entity && d.attribute == ITEM_PARENT)
        .collect();
    assert!(
        authoritative_parent.len() > 1,
        "parent replacement did not create a noHistory omission pair"
    );
    common::consolidate(&connection, &database_id).unwrap();
    let peer = Peer::connect(&connection, &database_id, 8).unwrap();
    assert_current_eq(&peer.database_value(), &expected);
    let snapshot = peer.snapshot();
    let retained = retained_attribute_history(&snapshot, IndexOrder::Eavt, entity, ITEM_COUNT);
    assert_eq!(retained.len(), 1);
    assert!(retained[0].added);
    assert_eq!(retained[0].value, Value::Long(5));

    // Every applicable physical history order must retain the same logical
    // facts even though each tree has different keys and leaf boundaries.
    for order in [IndexOrder::Aevt, IndexOrder::Avet] {
        let ordered = retained_attribute_history(&snapshot, order, entity, ITEM_COUNT);
        assert_eq!(ordered, retained, "noHistory diverged in {order:?}");
    }
    let retained_parent =
        retained_attribute_history(&snapshot, IndexOrder::Eavt, entity, ITEM_PARENT);
    assert_eq!(retained_parent.len(), 1);
    assert!(retained_parent[0].added);
    assert_eq!(retained_parent[0].value, Value::Ref(replacement_parent));
    for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
        assert_eq!(
            retained_attribute_history(&snapshot, order, entity, ITEM_PARENT),
            retained_parent,
            "reference noHistory omission diverged in {order:?}"
        );
    }
    drop(peer);

    let reopened = Peer::connect(&connection, &database_id, 8).unwrap();
    let reopened = reopened.snapshot();
    for order in [IndexOrder::Eavt, IndexOrder::Aevt, IndexOrder::Avet] {
        assert_eq!(
            retained_attribute_history(&reopened, order, entity, ITEM_COUNT),
            retained,
            "reopened count noHistory diverged in {order:?}"
        );
    }
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            retained_attribute_history(&reopened, order, entity, ITEM_PARENT),
            retained_parent,
            "reopened reference noHistory diverged in {order:?}"
        );
    }
}

#[test]
fn enabling_no_history_changes_only_future_indexing_jobs() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("nohistory_preserves_prior");
    let (mut store, entity) = populated(&connection, &database_id, false, 2);
    // Establish an exact pre-toggle durable base. Changing :db/noHistory does
    // not rewrite that base or alter ordinary history reads by itself.
    common::consolidate(&connection, &database_id).unwrap();
    let before = store.recover(&database_id).unwrap();
    let mut count = before.schema().attribute(ITEM_COUNT).unwrap().clone();
    count.no_history = true;
    let service = common::start_service(&connection, &database_id);
    let altered = common::transact(
        &service,
        "stop-retaining-future-history",
        before.basis_t(),
        &[TxOp::AlterAttribute(count)],
        2_000,
    );
    let third = common::transact(
        &service,
        "forgotten-three",
        altered.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        2_001,
    );
    let fourth = common::transact(
        &service,
        "retained-current-four",
        third.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(4)),
        }],
        2_002,
    );
    service.shutdown();

    let before_index_job = Peer::connect(&connection, &database_id, 16).unwrap();
    assert!(before_index_job.durable_base_t() < fourth.basis_t);
    let unindexed_history = before_index_job
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    for value in 0..=4 {
        assert!(
            unindexed_history
                .iter()
                .any(|datom| datom.value == Value::Long(value)),
            "changing :db/noHistory immediately changed history: {unindexed_history:?}"
        );
    }
    let expected_current = store.recover(&database_id).unwrap();

    common::consolidate(&connection, &database_id).unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(peer.durable_base_t(), fourth.basis_t);
    assert_current_eq(&peer.database_value(), &expected_current);
    let retained = peer
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    // The future indexing job merges affected old and new segment data before
    // applying endpoint noHistory. It may therefore forget an adjacent pair
    // straddling the toggle/base boundary; there is no pre-toggle cutoff.
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(2)));
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(3)));
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(4) && datom.added)
    );
}

#[test]
fn no_history_false_resumes_retention_across_a_pruned_base() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("nohistory_resume");
    let (mut store, entity) = populated(&connection, &database_id, true, 2);

    // Keep a full-log transactor value alive while an independent indexer
    // publishes a legally pair-pruned base. The v2 current-state commitment
    // must be independent of that background retention timing.
    let service = common::start_service(&connection, &database_id);
    common::consolidate(&connection, &database_id).unwrap();
    let before = store.recover(&database_id).unwrap();
    let mut count = before.schema().attribute(ITEM_COUNT).unwrap().clone();
    count.no_history = false;
    let altered = common::transact(
        &service,
        "retain-history-again",
        before.basis_t(),
        &[TxOp::AlterAttribute(count)],
        2_000,
    );
    let third = common::transact(
        &service,
        "post-nohistory-3",
        altered.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(3)),
        }],
        2_001,
    );
    let fourth = common::transact(
        &service,
        "post-nohistory-4",
        third.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(4)),
        }],
        2_002,
    );
    service.shutdown();

    common::consolidate(&connection, &database_id).unwrap();
    let peer = Peer::connect(&connection, &database_id, 16).unwrap();
    assert_eq!(peer.durable_base_t(), fourth.basis_t);
    let retained = peer
        .snapshot()
        .datoms(true, IndexOrder::Eavt)
        .unwrap()
        .datoms
        .into_iter()
        .filter(|datom| datom.entity == entity && datom.attribute == ITEM_COUNT)
        .collect::<Vec<_>>();
    assert!(!retained.iter().any(|datom| datom.value == Value::Long(1)));
    assert!(retained.iter().any(|datom| datom.value == Value::Long(2)));
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(3) && datom.added)
    );
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(3) && !datom.added)
    );
    assert!(
        retained
            .iter()
            .any(|datom| datom.value == Value::Long(4) && datom.added)
    );
}

#[test]
fn generated_queries_match_eager_native_and_force_scan_references() {
    let Some(fixture) = connection() else {
        return;
    };
    let connection = fixture.connection.clone();
    let database_id = unique("native_query_differential");
    common::blocks::install(&connection).unwrap();
    let mut store = common::TestStore::connect(&connection).unwrap();
    let created = store.create_database(&database_id, schema(false)).unwrap();
    let service = common::start_service(&connection, &database_id);

    // Generate enough connected facts to exercise AVET selection, joins, and
    // history without introducing a separate property-test harness. The data
    // and query sequence are fixed so a failure is exactly reproducible.
    const ENTITY_COUNT: usize = 12;
    let mut seed_ops = Vec::with_capacity(ENTITY_COUNT * 3);
    for ordinal in 0..ENTITY_COUNT {
        let entity = format!("node-{ordinal}");
        let parent = format!("node-{}", (ordinal + 1) % ENTITY_COUNT);
        seed_ops.extend([
            TxOp::Add {
                entity: EntityRef::Temp(entity.clone()),
                attribute: ITEM_NAME,
                value: TxValue::Scalar(Value::String(format!("{database_id}-node-{ordinal}"))),
            },
            TxOp::Add {
                entity: EntityRef::Temp(entity.clone()),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long((ordinal % 4) as i64)),
            },
            TxOp::Add {
                entity: EntityRef::Temp(entity),
                attribute: ITEM_PARENT,
                value: TxValue::Entity(EntityRef::Temp(parent)),
            },
        ]);
    }
    let seeded = common::transact(
        &service,
        "differential-seed",
        created.basis_t(),
        &seed_ops,
        10_000,
    );
    let entities = (0..ENTITY_COUNT)
        .map(|ordinal| seeded.tempids[&format!("node-{ordinal}")])
        .collect::<Vec<_>>();

    let updates = (0..ENTITY_COUNT)
        .filter(|ordinal| ordinal % 3 == 0)
        .map(|ordinal| TxOp::Add {
            entity: EntityRef::Id(entities[ordinal]),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(100 + ordinal as i64)),
        })
        .collect::<Vec<_>>();
    let updated = common::transact(
        &service,
        "differential-updates",
        seeded.basis_t,
        &updates,
        10_001,
    );
    let retracted = common::transact(
        &service,
        "differential-retract",
        updated.basis_t,
        &[TxOp::Retract {
            entity: EntityRef::Id(entities[5]),
            attribute: ITEM_COUNT,
            value: None,
        }],
        10_002,
    );
    service.shutdown();

    let pure_seed = atomic_core::Database::new(schema(false))
        .unwrap()
        .with(&seed_ops, 10_000)
        .unwrap();
    assert_eq!(pure_seed.tempids, seeded.tempids);
    let pure_update = pure_seed.db_after.with(&updates, 10_001).unwrap();
    let eager_database = pure_update
        .db_after
        .with(
            &[TxOp::Retract {
                entity: EntityRef::Id(entities[5]),
                attribute: ITEM_COUNT,
                value: None,
            }],
            10_002,
        )
        .unwrap()
        .db_after;
    assert_eq!(eager_database.basis_t(), retracted.basis_t);

    let publication = common::consolidate(&connection, &database_id).unwrap();
    assert_eq!(publication.basis_t, retracted.basis_t);
    let peer = Peer::connect(&connection, &database_id, 32).unwrap();
    let native = peer.database_value();
    let eager = eager_database.database_value();
    assert_eq!(native.basis_t(), eager.basis_t());
    assert_eq!(
        native.datoms(IndexOrder::Eavt).unwrap(),
        eager.datoms(IndexOrder::Eavt).unwrap()
    );

    let variable = |name: &str| Variable::new(name).unwrap();
    let attribute = |name: &str| Term::Constant(Value::Keyword(Keyword::new("item", name)));
    let pattern = |entity, name, value| {
        Clause::Pattern(Box::new(DataPattern::new(entity, attribute(name), value)))
    };

    // Generate several selective cases, including values affected and not
    // affected by cardinality-one replacement and explicit retraction.
    for wanted in 0..4 {
        let entity = variable("entity");
        let query = Query::new(
            FindSpec::Relation(vec![FindElement::Variable(entity.clone())]),
            vec![pattern(
                Term::Variable(entity),
                "count",
                Term::Constant(Value::Long(wanted)),
            )],
        );
        let outcomes = assert_eager_native_query_differential(
            &format!("selective-count-{wanted}"),
            &query,
            eager.clone(),
            native.clone(),
        );
        assert!(outcomes[0].stats.datoms_examined < outcomes[1].stats.datoms_examined);
        assert!(outcomes[2].stats.datoms_examined < outcomes[3].stats.datoms_examined);
    }

    let child = variable("child");
    let parent = variable("parent");
    let parent_name = variable("parent-name");
    let join = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(child.clone()),
            FindElement::Variable(parent_name.clone()),
        ]),
        vec![
            pattern(
                Term::Variable(child.clone()),
                "count",
                Term::Constant(Value::Long(2)),
            ),
            pattern(
                Term::Variable(child),
                "parent",
                Term::Variable(parent.clone()),
            ),
            pattern(Term::Variable(parent), "name", Term::Variable(parent_name)),
        ],
    );
    let join_outcomes = assert_eager_native_query_differential(
        "three-pattern-join",
        &join,
        eager.clone(),
        native.clone(),
    );
    assert!(join_outcomes[0].stats.datoms_examined < join_outcomes[1].stats.datoms_examined);
    assert!(join_outcomes[2].stats.datoms_examined < join_outcomes[3].stats.datoms_examined);

    let entity = variable("entity");
    let count = variable("count");
    let name = variable("name");
    let temporal_filtered = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(entity.clone()),
            FindElement::Variable(count.clone()),
            FindElement::Variable(name.clone()),
        ]),
        vec![
            pattern(
                Term::Variable(entity.clone()),
                "count",
                Term::Variable(count),
            ),
            pattern(Term::Variable(entity), "name", Term::Variable(name)),
        ],
    );
    let excluded = entities[4];
    let eager_temporal = eager
        .clone()
        .as_of(seeded.basis_t)
        .filter(move |_, datom| datom.entity != excluded);
    let native_temporal = native
        .clone()
        .as_of(seeded.basis_t)
        .filter(move |_, datom| datom.entity != excluded);
    let temporal_outcomes = assert_eager_native_query_differential(
        "as-of-plus-filter",
        &temporal_filtered,
        eager_temporal,
        native_temporal,
    );
    let mut expected_temporal_rows = entities
        .iter()
        .enumerate()
        .filter(|(ordinal, _)| *ordinal != 4)
        .map(|(ordinal, entity)| {
            vec![
                QueryValue::Scalar(Value::Ref(*entity)),
                QueryValue::Scalar(Value::Long((ordinal % 4) as i64)),
                QueryValue::Scalar(Value::String(format!("{database_id}-node-{ordinal}"))),
            ]
        })
        .collect::<Vec<_>>();
    expected_temporal_rows.sort_by(|left, right| left[0].canonical_cmp(&right[0]));
    assert_eq!(
        temporal_outcomes[0].result,
        QueryResult::Relation(expected_temporal_rows)
    );

    let historical_count = variable("historical-count");
    let tx = variable("tx");
    let added = variable("added");
    let mut history_pattern = DataPattern::new(
        Term::Blank,
        attribute("count"),
        Term::Variable(historical_count.clone()),
    );
    history_pattern.transaction = Some(Term::Variable(tx.clone()));
    history_pattern.added = Some(Term::Variable(added.clone()));
    let history = Query::new(
        FindSpec::Relation(vec![
            FindElement::Variable(historical_count),
            FindElement::Variable(tx),
            FindElement::Variable(added),
        ]),
        vec![Clause::Pattern(Box::new(history_pattern))],
    );
    let history_outcomes = assert_eager_native_query_differential(
        "raw-history",
        &history,
        eager.history(),
        native.history(),
    );
    let QueryResult::Relation(history_rows) = &history_outcomes[0].result else {
        panic!("history differential must return a relation")
    };
    assert!(
        history_rows
            .iter()
            .any(|row| { row[2] == QueryValue::Scalar(Value::Bool(false)) })
    );
}
