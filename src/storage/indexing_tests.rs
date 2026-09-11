//! Real opaque-store preparation/adoption witnesses; each test owns a fresh
//! schema. No server restart or existing catalog is changed.
use super::*;
use crate::index_support as peer;
use crate::storage::{BlockDatabase, BlockTransactor, BlockWriterOptions};
use crate::{
    Attribute, Cardinality, EntityRef, Keyword, PostgresConnectionConfig, Schema,
    TransactionRequest, TxOp, Value, ValueType,
};

const VALUE: u32 = 1000;
const LINK: u32 = 1001;
const LATER: u32 = 1002;

struct Fixture {
    admin: postgres::Client,
    schema: String,
    config: PostgresConnectionConfig,
    database: BlockDatabase,
}
impl Drop for Fixture {
    fn drop(&mut self) {
        let _ = self
            .admin
            .batch_execute(&format!("DROP SCHEMA {} CASCADE", self.schema));
    }
}
fn fixture(no_history: bool) -> Option<Fixture> {
    fixture_with_text(no_history, false)
}
fn fixture_with_text(no_history: bool, text: bool) -> Option<Fixture> {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block indexing PostgreSQL: ATOMIC_POSTGRES_URL unset");
        return None;
    };
    let schema = format!("block_index_{:032x}", crate::uuid_v7().unwrap());
    let mut admin = postgres::Client::connect(&connection, postgres::NoTls).unwrap();
    admin
        .batch_execute(&format!("CREATE SCHEMA {schema}"))
        .unwrap();
    let scoped = if connection.starts_with("postgres://") || connection.starts_with("postgresql://")
    {
        format!(
            "{connection}{}options=-csearch_path%3D{schema}%2Cpg_catalog",
            if connection.contains('?') { '&' } else { '?' }
        )
    } else {
        format!("{connection} options='-csearch_path={schema},pg_catalog'")
    };
    let config = PostgresConnectionConfig::plaintext(scoped);
    PgBlockStore::install(&config).unwrap();
    let mut application = Schema::new();
    for (id, name, kind, indexed) in [
        (VALUE, "value", ValueType::Long, true),
        (LINK, "link", ValueType::Ref, true),
        (LATER, "later", ValueType::Long, false),
    ] {
        let mut attribute = Attribute::new(id, Keyword::new("item", name), kind, Cardinality::One);
        attribute.indexed = indexed;
        attribute.no_history = no_history && id != LATER;
        application.install(attribute).unwrap();
    }
    if text {
        application
            .install(
                Attribute::new(
                    1003,
                    Keyword::new("item", "text"),
                    ValueType::String,
                    Cardinality::One,
                )
                .fulltext(),
            )
            .unwrap();
    }
    let database = BlockDatabase::create(&config, "indexing", application).unwrap();
    Some(Fixture {
        admin,
        schema,
        config,
        database,
    })
}
fn add(entity: EntityRef, attribute: u32, value: Value) -> TxOp {
    TxOp::Add {
        entity,
        attribute,
        value: value.into(),
    }
}
fn commit(
    writer: &mut BlockTransactor,
    key: &str,
    operations: Vec<TxOp>,
    instant: i64,
) -> crate::ServiceTransactionReport {
    writer
        .transact(&TransactionRequest::new(key, operations).with_tx_instant(instant))
        .unwrap()
}
fn config() -> TreeConfig {
    TreeConfig {
        max_leaf_datoms: 64,
        target_leaf_bytes: 4096,
        ..TreeConfig::default()
    }
}

#[test]
fn cached_incremental_lookup_preserves_decimal_scale_ties_across_single_datom_leaves() {
    use std::str::FromStr;
    let Some(f) = fixture(false) else {
        return;
    };
    let mut schema = Schema::new();
    let mut amount = Attribute::new(
        VALUE,
        Keyword::new("measurement", "amount"),
        ValueType::BigDec,
        Cardinality::Many,
    );
    amount.indexed = true;
    schema.install(amount).unwrap();
    let database = BlockDatabase::create(&f.config, "decimal-leaves", schema).unwrap();
    let mut writer =
        BlockTransactor::claim(&f.config, database, BlockWriterOptions::default()).unwrap();
    let values = ["1.0", "1.00", "1.000"]
        .map(|text| Value::BigDec(bigdecimal::BigDecimal::from_str(text).unwrap()));
    let first = commit(
        &mut writer,
        "scales",
        values
            .iter()
            .map(|value| add(EntityRef::Temp("item".into()), VALUE, value.clone()))
            .collect(),
        10,
    );
    let entity = first.tempids["item"];
    let tree = TreeConfig {
        max_leaf_datoms: 1,
        ..config()
    };
    let prepare = |writer: &mut BlockTransactor| {
        writer
            .index_input()
            .unwrap()
            .prepare(&mut PgBlockStore::connect(&f.config).unwrap(), &tree)
            .unwrap()
    };
    let initial = prepare(&mut writer);
    writer.adopt_index(initial).unwrap();
    let old = writer.db().unwrap();
    commit(
        &mut writer,
        "retract-middle-scale",
        vec![TxOp::Retract {
            entity: EntityRef::Id(entity),
            attribute: VALUE,
            value: Some(values[1].clone().into()),
        }],
        20,
    );
    let second = prepare(&mut writer);
    writer.adopt_index(second).unwrap();
    let current = writer.db().unwrap();
    assert_eq!(old.values(entity, VALUE).unwrap(), values.to_vec());
    assert_eq!(
        current.values(entity, VALUE).unwrap(),
        vec![values[0].clone(), values[2].clone()]
    );
    for order in [IndexOrder::Eavt, IndexOrder::Aevt, IndexOrder::Avet] {
        let actual = current
            .datoms(order)
            .unwrap()
            .into_iter()
            .filter(|d| d.entity == entity && d.attribute == VALUE)
            .map(|d| d.value)
            .collect::<Vec<_>>();
        assert_eq!(actual, vec![values[0].clone(), values[2].clone()]);
        assert_eq!(
            current
                .clone()
                .history()
                .datoms(order)
                .unwrap()
                .iter()
                .filter(|d| d.entity == entity && d.attribute == VALUE)
                .count(),
            4
        );
    }
    writer.release().unwrap();
}
fn prepare(writer: &mut BlockTransactor, fixture: &Fixture, lanes: usize) -> PreparedIndex {
    // A new independent transport on every invocation also proves resumability
    // does not depend on a surviving process-local sort workspace.
    writer
        .index_input()
        .unwrap()
        .prepare_with_parallelism(
            &mut PgBlockStore::connect(&fixture.config).unwrap(),
            &config(),
            lanes,
            &mut || Ok(()),
        )
        .unwrap()
}
fn assert_current(left: &crate::DatabaseValue, right: &crate::DatabaseValue) {
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.datoms(order).unwrap(),
            right.datoms(order).unwrap(),
            "{order:?}"
        );
    }
}

#[test]
fn frozen_prefix_adoption_preserves_newer_writes_and_coordinated_no_history() {
    let Some(f) = fixture(true) else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let seed = commit(
        &mut writer,
        "seed",
        vec![
            add(EntityRef::Temp("one".into()), VALUE, Value::Long(0)),
            add(EntityRef::Temp("two".into()), VALUE, Value::Long(100)),
            add(EntityRef::Temp("three".into()), VALUE, Value::Long(200)),
            TxOp::Add {
                entity: EntityRef::Temp("one".into()),
                attribute: LINK,
                value: crate::TxValue::Entity(EntityRef::Temp("two".into())),
            },
        ],
        10,
    );
    let one = seed.tempids["one"];
    let two = seed.tempids["two"];
    let three = seed.tempids["three"];
    let initial = prepare(&mut writer, &f, 1);
    writer.adopt_index(initial).unwrap();
    commit(
        &mut writer,
        "second",
        vec![
            add(EntityRef::Id(one), VALUE, Value::Long(1)),
            add(EntityRef::Id(one), LINK, Value::Ref(three)),
        ],
        20,
    );
    let before_cleanup = commit(
        &mut writer,
        "third",
        vec![
            add(EntityRef::Id(one), VALUE, Value::Long(2)),
            add(EntityRef::Id(one), LINK, Value::Ref(two)),
        ],
        30,
    );
    let held = before_cleanup.db_after.clone().history();
    let held_rows = held.datoms(IndexOrder::Eavt).unwrap();
    let candidate = prepare(&mut writer, &f, 4);
    assert!(candidate.stats.no_history_pairs >= 4);
    assert!(candidate.stats.preparation_workers > 0);
    assert!(candidate.stats.peak_preparation_workers <= 4);
    let through = candidate.through_basis;
    let latest = commit(
        &mut writer,
        "newer",
        vec![add(EntityRef::Id(three), LATER, Value::Long(99))],
        40,
    );
    let adopted = writer.adopt_index(candidate).unwrap();
    assert_eq!(adopted.indexed_basis, through);
    let current = writer.db().unwrap();
    assert_eq!(current.basis_t(), latest.basis_t);
    assert_current(&current, &latest.db_after);
    assert_eq!(
        held.datoms(IndexOrder::Eavt).unwrap(),
        held_rows,
        "pinned source keeps its prior history"
    );
    let retained = current.clone().history().datoms(IndexOrder::Eavt).unwrap();
    for attribute in [VALUE, LINK] {
        let rows: Vec<_> = retained
            .iter()
            .filter(|d| d.entity == one && d.attribute == attribute)
            .collect();
        assert_eq!(
            rows.len(),
            1,
            "removed only matched old assertion/retraction pairs"
        );
        assert!(rows[0].added);
        assert_eq!(rows[0].tx, crate::t_to_tx(before_cleanup.basis_t).unwrap());
    }
    // Independent projection oracle from canonical retained EAVT, not a
    // separately filtered history stream that could disagree on leaf locality.
    for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
        let mut expected = retained
            .iter()
            .filter(|d| peer::schema_index_member(current.schema(), d, order).unwrap())
            .cloned()
            .collect::<Vec<_>>();
        expected.sort_by(|a, b| a.cmp_in(b, order));
        assert_eq!(current.clone().history().datoms(order).unwrap(), expected);
    }
    writer.release().unwrap();
}

#[test]
fn avet_structural_work_resumes_after_worker_restart_and_concurrent_toggle_tail() {
    let Some(f) = fixture(false) else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let seed = commit(
        &mut writer,
        "seed",
        (0..2050)
            .map(|i| add(EntityRef::Temp(format!("e{i}")), LATER, Value::Long(i)))
            .collect(),
        10,
    );
    let mut initial = prepare(&mut writer, &f, 1);
    assert!(initial.descriptor.avet_work.is_empty());
    writer.adopt_index(initial).unwrap();
    let mut enabled = writer
        .db()
        .unwrap()
        .schema()
        .attribute(LATER)
        .unwrap()
        .clone();
    enabled.indexed = true;
    commit(
        &mut writer,
        "enable",
        vec![TxOp::AlterAttribute(enabled.clone())],
        20,
    );
    initial = prepare(&mut writer, &f, 1);
    assert_eq!(initial.descriptor.pending_avet, vec![LATER]);
    writer.adopt_index(initial).unwrap();
    let mut changed_tail = false;
    let mut copy_steps = 0;
    for step in 0..40 {
        let pending = writer.pending_avet_projections();
        if pending == 0 && writer.indexed_basis_t() == writer.db().unwrap().basis_t() {
            break;
        }
        let candidate = prepare(&mut writer, &f, 1);
        if candidate.stats.projection_steps != 0 {
            assert!(candidate.stats.projection_datoms <= PROJECTION_DATOMS as u64);
            if candidate.stats.projection_datoms != 0 {
                copy_steps += 1;
            }
        }
        if !changed_tail && candidate.stats.projection_datoms != 0 {
            let entity = seed.tempids["e0"];
            commit(
                &mut writer,
                "new-value",
                vec![add(EntityRef::Id(entity), LATER, Value::Long(9000))],
                30,
            );
            let mut disabled = enabled.clone();
            disabled.indexed = false;
            commit(
                &mut writer,
                "disable",
                vec![TxOp::AlterAttribute(disabled)],
                40,
            );
            commit(
                &mut writer,
                "reenable",
                vec![TxOp::AlterAttribute(enabled.clone())],
                50,
            );
            changed_tail = true;
        }
        writer.adopt_index(candidate).unwrap();
        assert!(step < 39, "bounded checkpoints must make progress");
    }
    assert!(changed_tail && copy_steps >= 6);
    assert_eq!(writer.pending_avet_projections(), 0);
    let current = writer.db().unwrap();
    assert_eq!(writer.indexed_basis_t(), current.basis_t());
    for history in [false, true] {
        let view = if history {
            current.clone().history()
        } else {
            current.clone()
        };
        let mut expected = view
            .datoms(IndexOrder::Aevt)
            .unwrap()
            .into_iter()
            .filter(|d| peer::schema_index_member(current.schema(), d, IndexOrder::Avet).unwrap())
            .collect::<Vec<_>>();
        expected.sort_by(|a, b| a.cmp_in(b, IndexOrder::Avet));
        assert_eq!(view.datoms(IndexOrder::Avet).unwrap(), expected);
    }
    assert_eq!(
        current.values(seed.tempids["e0"], LATER).unwrap(),
        vec![Value::Long(9000)]
    );
    writer.release().unwrap();
}

#[test]
fn cancellation_and_competing_preparations_never_publish_partial_indexes() {
    let Some(f) = fixture(false) else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    commit(
        &mut writer,
        "seed",
        vec![add(EntityRef::Temp("one".into()), VALUE, Value::Long(1))],
        10,
    );
    let mut store = PgBlockStore::connect(&f.config).unwrap();
    let before = store.read_ref(&f.database.reference_key()).unwrap();
    let mut checks = 0;
    let result =
        writer
            .index_input()
            .unwrap()
            .prepare_with_control(&mut store, &config(), &mut || {
                checks += 1;
                if checks >= 3 {
                    Err(SemanticError::new(
                        ErrorCategory::Interrupted,
                        "test/index-cancel",
                        "cancelled",
                    ))
                } else {
                    Ok(())
                }
            });
    assert_eq!(result.err().unwrap().category, ErrorCategory::Interrupted);
    assert!(store.write_protection().is_none());
    assert_eq!(store.read_ref(&f.database.reference_key()).unwrap(), before);
    let first = prepare(&mut writer, &f, 1);
    let other = prepare(&mut writer, &f, 1);
    writer.adopt_index(first).unwrap();
    let after = store.read_ref(&f.database.reference_key()).unwrap();
    assert_eq!(
        writer.adopt_index(other).unwrap_err().code,
        "storage/index-source-changed"
    );
    assert_eq!(store.read_ref(&f.database.reference_key()).unwrap(), after);
    writer.release().unwrap();
}

/// A repository-shaped writer: source reads may reach PostgreSQL, while puts
/// can only reach this separate destination. The product repository supplies
/// files at this same seam; no publication authority is available here.
struct RepositoryDestination {
    source: PgBlockStore,
    objects: BTreeMap<ObjectId, Vec<u8>>,
}
impl crate::storage::ObjectReader for RepositoryDestination {
    fn read_object(&mut self, id: ObjectId) -> Result<Vec<u8>, SemanticError> {
        match self.objects.get(&id) {
            Some(bytes) => Ok(bytes.clone()),
            None => crate::storage::ObjectReader::read_object(&mut self.source, id),
        }
    }
}
impl ObjectWriter for RepositoryDestination {
    fn put_object(&mut self, bytes: &[u8]) -> Result<ObjectId, SemanticError> {
        let id = crate::sha256(bytes);
        self.objects.insert(id, bytes.to_vec());
        Ok(id)
    }
}
#[derive(Clone, Copy)]
struct RepositoryNodes<'a>(&'a std::cell::RefCell<RepositoryDestination>);
impl crate::tree_cursor::DurableTreeSource for RepositoryNodes<'_> {
    fn load_node(
        &self,
        id: ObjectId,
        _: &mut crate::persistent_tree::TreeReadStats,
    ) -> Result<std::sync::Arc<crate::persistent_tree::TreeNode>, SemanticError> {
        use crate::storage::ObjectReader;
        let bytes = self.0.borrow_mut().read_object(id)?;
        Ok(std::sync::Arc::new(
            crate::persistent_tree::decode_tree_node(&id, &bytes)?,
        ))
    }
}
fn repository_datoms(
    destination: &std::cell::RefCell<RepositoryDestination>,
    descriptor: &TreeDescriptor,
) -> Vec<Datom> {
    use crate::tree_cursor::DurableTreeSource;
    let source = RepositoryNodes(destination);
    let root = source
        .load_node(descriptor.root_hash, &mut Default::default())
        .unwrap();
    let crate::persistent_tree::TreeNode::Root(root) = root.as_ref() else {
        panic!("tree root")
    };
    let mut cursor = crate::tree_cursor::DurableTreeCursor::new(
        source,
        std::sync::Arc::new(root.clone()),
        descriptor.history,
        descriptor.order,
        None,
        None,
    );
    std::iter::from_fn(|| cursor.next_datom().transpose())
        .collect::<Result<Vec<_>, _>>()
        .unwrap()
}

#[test]
fn repository_preparation_finishes_base_work_toggle_tail_no_history_and_search_without_source_writes()
 {
    use crate::storage::ObjectReader;
    let Some(mut f) = fixture_with_text(true, true) else {
        return;
    };
    let mut writer =
        BlockTransactor::claim(&f.config, f.database.clone(), BlockWriterOptions::default())
            .unwrap();
    let mut seed_ops = (0..2050)
        .map(|i| add(EntityRef::Temp(format!("e{i}")), LATER, Value::Long(i)))
        .collect::<Vec<_>>();
    seed_ops.push(add(EntityRef::Temp("e0".into()), VALUE, Value::Long(0)));
    seed_ops.push(add(
        EntityRef::Temp("e0".into()),
        1003,
        Value::String("original searchable token".into()),
    ));
    let seed = commit(&mut writer, "repo-seed", seed_ops, 10);
    let entity = seed.tempids["e0"];
    let candidate = prepare(&mut writer, &f, 1);
    writer.adopt_index(candidate).unwrap();
    let mut enabled = writer
        .db()
        .unwrap()
        .schema()
        .attribute(LATER)
        .unwrap()
        .clone();
    enabled.indexed = true;
    commit(
        &mut writer,
        "repo-enable",
        vec![TxOp::AlterAttribute(enabled.clone())],
        20,
    );
    let candidate = prepare(&mut writer, &f, 1);
    assert_eq!(candidate.descriptor.pending_avet, vec![LATER]);
    writer.adopt_index(candidate).unwrap();
    let mut disabled = enabled.clone();
    disabled.indexed = false;
    commit(
        &mut writer,
        "repo-disable",
        vec![TxOp::AlterAttribute(disabled)],
        30,
    );
    commit(
        &mut writer,
        "repo-reenable",
        vec![TxOp::AlterAttribute(enabled)],
        40,
    );
    commit(
        &mut writer,
        "repo-update",
        vec![
            add(EntityRef::Id(entity), LATER, Value::Long(9000)),
            add(EntityRef::Id(entity), VALUE, Value::Long(1)),
            add(
                EntityRef::Id(entity),
                1003,
                Value::String("replacement searchable token".into()),
            ),
        ],
        50,
    );
    commit(
        &mut writer,
        "repo-update-again",
        vec![add(EntityRef::Id(entity), VALUE, Value::Long(2))],
        60,
    );
    let snapshot = writer.index_input().unwrap().snapshot;
    let current = snapshot.database_value();
    let mut destination = RepositoryDestination {
        source: PgBlockStore::connect(&f.config).unwrap(),
        objects: BTreeMap::new(),
    };
    let count_sql = format!("SELECT count(*) FROM {}.atomic_objects", f.schema);
    let count_before: i64 = f.admin.query_one(&count_sql, &[]).unwrap().get(0);
    let publication = destination
        .source
        .read_ref(&f.database.reference_key())
        .unwrap();
    let mut checks = 0;
    let cancelled = prepare_read_value(&snapshot, &mut destination, &config(), &mut || {
        checks += 1;
        if checks == 4 {
            Err(SemanticError::new(
                ErrorCategory::Interrupted,
                "test/repository-cancel",
                "cancelled",
            ))
        } else {
            Ok(())
        }
    });
    assert_eq!(
        cancelled.err().unwrap().category,
        ErrorCategory::Interrupted
    );
    let prepared =
        prepare_read_value(&snapshot, &mut destination, &config(), &mut || Ok(())).unwrap();
    assert_eq!(prepared.value.identity, snapshot.captured_root().identity);
    assert_eq!(prepared.value.basis, snapshot.basis_t());
    assert_eq!(prepared.value.log, snapshot.captured_root().log);
    assert_eq!(prepared.value.metadata, snapshot.captured_root().metadata);
    assert!(prepared.index_stats.projection_steps > 8);
    assert!(prepared.index_stats.no_history_pairs >= 2);
    assert!(prepared.fulltext_stats.unwrap().documents_added > 0);
    assert_eq!(
        destination
            .source
            .read_ref(&f.database.reference_key())
            .unwrap(),
        publication
    );
    let count_after: i64 = f.admin.query_one(&count_sql, &[]).unwrap().get(0);
    assert_eq!(
        count_before, count_after,
        "repository preparation must never write source objects"
    );
    let index_id = prepared.value.indexes.unwrap();
    let descriptor =
        IndexDescriptor::decode(&index_id, &destination.read_object(index_id).unwrap()).unwrap();
    assert_eq!(descriptor.basis, snapshot.basis_t());
    assert!(descriptor.avet_work.is_empty() && descriptor.pending_avet.is_empty());
    let attachment = descriptor.fulltext.unwrap();
    let block = crate::storage::root::Block::decode(
        &attachment,
        &destination.read_object(attachment).unwrap(),
    )
    .unwrap();
    let mut source_descriptor = descriptor.clone();
    source_descriptor.fulltext = None;
    assert_eq!(
        block.links[0],
        crate::sha256(&source_descriptor.encode().unwrap()),
        "search binds the final exact index, not an intermediate projection"
    );
    let destination = std::cell::RefCell::new(destination);
    // The source deliberately has an unready AVET range, so derive its fully
    // ready projection from canonical EAVT instead of treating the partial
    // source AVET cursor as an oracle for the completed destination.
    let current_eavt = current.datoms(IndexOrder::Eavt).unwrap();
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        let mut expected = current_eavt
            .iter()
            .filter(|d| peer::schema_index_member(current.schema(), d, order).unwrap())
            .cloned()
            .collect::<Vec<_>>();
        expected.sort_by(|a, b| a.cmp_in(b, order));
        assert_eq!(
            repository_datoms(&destination, tree(&descriptor, false, order)),
            expected
        );
    }
    let retained = repository_datoms(&destination, tree(&descriptor, true, IndexOrder::Eavt));
    let value_rows = retained
        .iter()
        .filter(|d| d.entity == entity && d.attribute == VALUE)
        .collect::<Vec<_>>();
    assert_eq!(value_rows.len(), 1);
    assert_eq!(value_rows[0].value, Value::Long(2));
    for order in [IndexOrder::Aevt, IndexOrder::Avet, IndexOrder::Vaet] {
        let mut expected = retained
            .iter()
            .filter(|d| peer::schema_index_member(current.schema(), d, order).unwrap())
            .cloned()
            .collect::<Vec<_>>();
        expected.sort_by(|a, b| a.cmp_in(b, order));
        assert_eq!(
            repository_datoms(&destination, tree(&descriptor, true, order)),
            expected
        );
    }
    writer.release().unwrap();
}
