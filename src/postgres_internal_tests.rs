use crate::postgres::{CapacityLimits, CommitFault, MIGRATIONS, PostgresStore};
use crate::{
    Attribute, Cardinality, DB_ENTITY_ATTRS, DB_ENTITY_PREDS, DB_FN, DB_IDENT, Database, EntityRef,
    ErrorCategory, IndexOrder, IndexPrefix, Instruction, Keyword, Program, ProgramKind, Schema,
    Symbol, TxOp, TxValue, Unique, Value, ValueType, View,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::time::{SystemTime, UNIX_EPOCH};

const ITEM_NAME: u32 = 1_000;
const ITEM_COUNT: u32 = 1_001;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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

fn schema() -> Schema {
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
    schema
        .install(Attribute::new(
            ITEM_COUNT,
            Keyword::new("item", "quantity"),
            ValueType::Long,
            Cardinality::One,
        ))
        .unwrap();
    schema
}

fn add_item(name: &str, count: i64) -> Vec<TxOp> {
    vec![
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_NAME,
            value: TxValue::Scalar(Value::String(name.into())),
        },
        TxOp::Add {
            entity: EntityRef::Temp("item".into()),
            attribute: ITEM_COUNT,
            value: TxValue::Scalar(Value::Long(count)),
        },
    ]
}

fn migrated_store(connection: &str) -> PostgresStore {
    let mut migrator = crate::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    PostgresStore::connect(connection).unwrap()
}

fn assert_database_eq(left: &Database, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for view in [View::Current, View::History] {
        for order in [
            IndexOrder::Eavt,
            IndexOrder::Aevt,
            IndexOrder::Avet,
            IndexOrder::Vaet,
        ] {
            assert_eq!(left.datoms(view, order), right.datoms(view, order));
        }
    }
}

fn assert_value_eq_database(left: &crate::DatabaseValue, right: &Database) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(
            left.datoms(order).unwrap(),
            right.datoms(View::Current, order)
        );
        assert_eq!(
            left.clone().history().datoms(order).unwrap(),
            right.datoms(View::History, order)
        );
    }
}

fn assert_database_values_eq(left: &crate::DatabaseValue, right: &crate::DatabaseValue) {
    assert_eq!(left.basis_t(), right.basis_t());
    assert_eq!(left.eidx_frontier(), right.eidx_frontier());
    assert_eq!(left.schema(), right.schema());
    for order in [
        IndexOrder::Eavt,
        IndexOrder::Aevt,
        IndexOrder::Avet,
        IndexOrder::Vaet,
    ] {
        assert_eq!(left.datoms(order).unwrap(), right.datoms(order).unwrap());
        assert_eq!(
            left.clone().history().datoms(order).unwrap(),
            right.clone().history().datoms(order).unwrap()
        );
    }
}

fn publish_native_base(connection: &str, database_id: &str) {
    crate::PostgresIndexer::connect(connection, database_id)
        .unwrap()
        .consolidate()
        .unwrap();
}

fn positive_item_count_predicate() -> Program {
    Program {
        kind: ProgramKind::EntityPredicate,
        arity: 1,
        instructions: vec![
            Instruction::PushArgument(0),
            Instruction::LoadOne(ITEM_COUNT),
            Instruction::PushConstant(Value::Long(0)),
            Instruction::GreaterThan,
            Instruction::Return,
        ],
    }
}

#[test]
fn native_data_commits_share_the_schema_projection_until_a_schema_edit() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("native_schema_arc_sharing");
    let mut store = migrated_store(&connection);
    let eager = store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);

    let ops = add_item("shared-schema", 1);
    let expected_data = eager.with(&ops, 2_000).unwrap();
    let ordinary = store
        .transact_with_fault(
            &database_id,
            "native-schema-sharing-data",
            eager.basis_t(),
            &ops,
            2_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(ordinary.tx_data, expected_data.tx_data);
    assert_eq!(ordinary.database.schema(), expected_data.db_after.schema());
    assert!(
        std::sync::Arc::ptr_eq(
            &ordinary.db_before.schema_arc(),
            &ordinary.database.schema_arc(),
        ),
        "an ordinary native successor must retain its resident schema Arc"
    );

    let mut altered = ordinary
        .database
        .schema()
        .attribute(ITEM_COUNT)
        .unwrap()
        .clone();
    altered.no_history = true;
    let schema_ops = [TxOp::AlterAttribute(altered)];
    let expected_schema = expected_data.db_after.with(&schema_ops, 3_000).unwrap();
    let schema_edit = store
        .transact_with_fault(
            &database_id,
            "native-schema-sharing-edit",
            ordinary.basis_t,
            &schema_ops,
            3_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(schema_edit.tx_data, expected_schema.tx_data);
    assert_eq!(
        schema_edit.database.schema(),
        expected_schema.db_after.schema()
    );
    assert!(
        !std::sync::Arc::ptr_eq(
            &schema_edit.db_before.schema_arc(),
            &schema_edit.database.schema_arc(),
        ),
        "a native schema edit must install a replacement projection"
    );
}

#[test]
fn transaction_read_work_includes_predicate_and_commitment_reads() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("transaction_wide_read_work");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);

    let predicate_hash = store
        .deploy_program_blob(&positive_item_count_predicate())
        .unwrap();
    let predicate_ident = Keyword::new("item.predicates", "positive-count");
    let spec_ident = Keyword::new("item.spec", "counted");
    let installed = store
        .transact_with_fault(
            &database_id,
            "install-read-work-predicate",
            1,
            &[
                TxOp::Add {
                    entity: EntityRef::Temp("predicate".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(predicate_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("predicate".into()),
                    attribute: DB_FN as u32,
                    value: Value::Function(predicate_hash).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_IDENT as u32,
                    value: Value::Keyword(spec_ident.clone()).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_ATTRS as u32,
                    value: Value::Keyword(Keyword::new("item", "name")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_ATTRS as u32,
                    value: Value::Keyword(Keyword::new("item", "quantity")).into(),
                },
                TxOp::Add {
                    entity: EntityRef::Temp("spec".into()),
                    attribute: DB_ENTITY_PREDS as u32,
                    value: Value::Symbol(Symbol::new("item.predicates", "positive-count")).into(),
                },
            ],
            1_000,
            CommitFault::None,
        )
        .unwrap();
    let seeded = store
        .transact_with_fault(
            &database_id,
            "seed-read-work-entity",
            installed.basis_t,
            &add_item("metered", 1),
            2_000,
            CommitFault::None,
        )
        .unwrap();
    let entity = seeded.tempids["item"];
    // Commitment nodes are globally content-addressed, so make this leaf
    // unique per run; otherwise a correct idempotent INSERT can report zero
    // physical writes after an earlier test produced the same semantic node.
    let update_count = (SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos()
        % (i64::MAX as u128)) as i64
        + 1;
    let update = vec![
        TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: Value::Long(update_count).into(),
        },
        TxOp::Ensure {
            entity: EntityRef::Id(entity),
            spec: EntityRef::Ident(spec_ident),
        },
    ];
    let assessed = crate::tiered_assessor::assess_tiered(&seeded.database, &update, 3_000)
        .expect("eager-oracle-equivalent assessment");
    assert!(assessed.read_work.datoms > 0);

    // Exactly the assessor's allowance is insufficient: resolving the
    // persisted predicate from db-before, checking required attributes and
    // running it over complete db-after, then recovering the prior assertion
    // for the commitment all perform real logical reads after assessment.
    let before_rejection = store.writer_residency_stats(&database_id);
    store
        .set_capacity_limits(CapacityLimits {
            max_transaction_read_datoms: assessed.read_work.datoms,
            max_transaction_read_bytes: u64::MAX,
            ..CapacityLimits::default()
        })
        .unwrap();
    let rejected = store
        .transact_with_fault(
            &database_id,
            "transaction-wide-read-work",
            seeded.basis_t,
            &update,
            3_000,
            CommitFault::None,
        )
        .unwrap_err();
    assert_eq!(
        (rejected.category, rejected.code),
        (ErrorCategory::Busy, "transaction/read-capacity")
    );
    assert_eq!(
        store.writer_residency_stats(&database_id),
        before_rejection,
        "a rejected attempt cannot replace the last committed work sample"
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        seeded.basis_t
    );

    store
        .set_capacity_limits(CapacityLimits {
            max_transaction_read_datoms: assessed.read_work.datoms + 64,
            max_transaction_read_bytes: u64::MAX,
            ..CapacityLimits::default()
        })
        .unwrap();
    let committed = store
        .transact_with_fault(
            &database_id,
            "transaction-wide-read-work",
            seeded.basis_t,
            &update,
            3_000,
            CommitFault::None,
        )
        .unwrap();
    let committed_stats = store.writer_residency_stats(&database_id);
    assert!(committed_stats.last_transaction_read_datoms > assessed.read_work.datoms);
    assert!(committed_stats.last_transaction_read_bytes > assessed.read_work.retained_bytes);
    assert!(
        committed_stats.last_transaction_prefix_memo_hits > 0,
        "successor validation, entity predicates, and commitment recovery must reuse completed exact prefixes"
    );
    assert!(committed_stats.last_transaction_prefix_memo_admissions > 0);
    assert!(
        committed_stats.last_transaction_source_read_datoms
            < committed_stats.last_transaction_read_datoms,
        "memo replays remain logical reads but cannot repeat source deliveries"
    );
    assert!(committed_stats.last_native_cursor_ranges > 0);
    assert!(
        committed_stats.last_native_cursor_ranges
            >= committed_stats.last_transaction_prefix_memo_misses
    );
    assert_eq!(
        committed_stats.last_native_cache_misses,
        committed_stats.last_native_sql_reads
    );
    assert!(committed_stats.last_native_recent_datoms_examined > 0);
    assert!(committed_stats.last_native_recent_datoms_yielded > 0);
    assert!(committed_stats.last_commitment_sql_node_reads > 0);
    assert!(committed_stats.last_commitment_sql_node_read_bytes > 0);
    assert!(committed_stats.last_commitment_sql_node_writes > 0);
    assert!(committed_stats.last_commitment_sql_node_write_bytes > 0);
    assert_eq!(committed_stats.last_commitment_sql_coordinate_reads, 1);
    assert_eq!(committed_stats.last_commitment_sql_coordinate_writes, 1);
    assert!(committed.db_before.transaction_read_context().is_none());
    assert!(committed.database.transaction_read_context().is_none());

    let replay = store
        .transact_with_fault(
            &database_id,
            "transaction-wide-read-work",
            seeded.basis_t,
            &update,
            3_000,
            CommitFault::None,
        )
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.basis_t, committed.basis_t);
    let replay_stats = store.writer_residency_stats(&database_id);
    assert_eq!(
        (
            replay_stats.last_transaction_read_datoms,
            replay_stats.last_transaction_read_bytes,
            replay_stats.last_commitment_node_visits,
            replay_stats.last_commitment_node_hashes,
            replay_stats.last_commitment_leaf_changes,
        ),
        (
            committed_stats.last_transaction_read_datoms,
            committed_stats.last_transaction_read_bytes,
            committed_stats.last_commitment_node_visits,
            committed_stats.last_commitment_node_hashes,
            committed_stats.last_commitment_leaf_changes,
        ),
        "an exact receipt replay performs no new transaction assessment"
    );

    // Enabling AVET over an attribute with history performs one final
    // `has-values?` probe while constructing the immutable successor. Compute
    // every earlier logical datom delivery, leave no allowance for that final
    // probe, and prove the schema transaction stays invisible.
    let mut indexed_count = committed
        .database
        .schema()
        .attribute(ITEM_COUNT)
        .unwrap()
        .clone();
    indexed_count.indexed = true;
    let schema_ops = vec![TxOp::AlterAttribute(indexed_count)];
    let schema_assessed =
        crate::tiered_assessor::assess_tiered(&committed.database, &schema_ops, 4_000).unwrap();
    let mut commitment_prior_reads = 0_u64;
    for datom in &schema_assessed.tx_data {
        let prefix = IndexPrefix::Eavt {
            entity: datom.entity,
            attribute: Some(datom.attribute),
            value: Some(datom.value.clone()),
        };
        for candidate in committed.database.current_prefix_cursor(&prefix).unwrap() {
            let candidate = candidate.unwrap();
            commitment_prior_reads += 1;
            if candidate.value.stored_eq(&datom.value) {
                break;
            }
        }
    }
    let prior_tx = crate::t_to_tx(committed.basis_t).unwrap();
    let tx_instant_datoms = committed
        .database
        .datoms_with_prefix(&IndexPrefix::Eavt {
            entity: prior_tx,
            attribute: Some(crate::DB_TX_INSTANT as u32),
            value: None,
        })
        .unwrap();
    assert_eq!(tx_instant_datoms.len(), 1);
    // The first lookup seeds the immutable value's shared resident memo;
    // assessor monotonicity validation and overlay construction reuse it.
    let tx_instant_reads = u64::try_from(tx_instant_datoms.len()).unwrap();
    let before_schema_rejection = store.writer_residency_stats(&database_id);
    // No program-binding attribute changed, so successor binding validation
    // now returns before the unqualified :db.entity/preds/schema scan.
    let without_successor_probe =
        schema_assessed.read_work.datoms + tx_instant_reads + commitment_prior_reads;
    assert_eq!(without_successor_probe + 1, 8);
    store
        .set_capacity_limits(CapacityLimits {
            max_transaction_read_datoms: without_successor_probe,
            max_transaction_read_bytes: u64::MAX,
            ..CapacityLimits::default()
        })
        .unwrap();
    let schema_rejected = store
        .transact_with_fault(
            &database_id,
            "schema-successor-read-work",
            committed.basis_t,
            &schema_ops,
            4_000,
            CommitFault::None,
        )
        .unwrap_err();
    assert_eq!(schema_rejected.code, "transaction/read-capacity");
    assert_eq!(
        store.writer_residency_stats(&database_id),
        before_schema_rejection
    );
    assert_eq!(
        store.recover(&database_id).unwrap().basis_t(),
        committed.basis_t
    );

    store
        .set_capacity_limits(CapacityLimits {
            max_transaction_read_datoms: without_successor_probe + 1,
            max_transaction_read_bytes: u64::MAX,
            ..CapacityLimits::default()
        })
        .unwrap();
    let indexed = store
        .transact_with_fault(
            &database_id,
            "schema-successor-read-work",
            committed.basis_t,
            &schema_ops,
            4_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(indexed.basis_t, committed.basis_t + 1);
    assert_eq!(
        store
            .writer_residency_stats(&database_id)
            .last_transaction_read_datoms,
        without_successor_probe + 1
    );
}

pub(crate) fn create_isolated_schema(connection: &str, prefix: &str) -> String {
    let schema = unique(prefix);
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("CREATE SCHEMA \"{schema}\""))
        .unwrap();
    schema
}

pub(crate) fn client_in_schema(connection: &str, schema: &str) -> Client {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("SET search_path TO \"{schema}\""))
        .unwrap();
    client
}

pub(crate) fn drop_isolated_schema(connection: &str, schema: &str) {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("DROP SCHEMA \"{schema}\" CASCADE"))
        .unwrap();
}

#[test]
fn fresh_administrative_install_is_complete_and_idempotent() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_fresh");
    let client = client_in_schema(&connection, &isolated);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    migrator.migrate().unwrap();

    let mut client = client_in_schema(&connection, &isolated);
    let versions: i64 = client
        .query_one("SELECT count(*) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(versions, MIGRATIONS.len() as i64);
    assert_eq!(versions, 1, "fresh installations have one baseline record");
    let row = client
        .query_one(
            "SELECT version, checksum FROM atomic_schema_migrations",
            &[],
        )
        .unwrap();
    assert_eq!(row.get::<_, i64>(0), crate::POSTGRES_SCHEMA_VERSION);
    assert_eq!(
        row.get::<_, Vec<u8>>(1),
        crate::sha256(MIGRATIONS[0].1.as_bytes())
    );
    drop(client);
    drop(migrator);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn current_install_recheck_preserves_canonical_nodes_and_optional_compression() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_blocks");
    crate::PostgresMigrator::from_client(client_in_schema(&connection, &isolated))
        .migrate()
        .unwrap();
    let mut client = client_in_schema(&connection, &isolated);
    let checksums = client
        .query(
            "SELECT version,checksum FROM atomic_schema_migrations ORDER BY version",
            &[],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get::<_, i64>(0), row.get::<_, Vec<u8>>(1)))
        .collect::<Vec<_>>();
    let database = Database::new(schema())
        .unwrap()
        .with(
            &[TxOp::Add {
                entity: EntityRef::Temp("current".into()),
                attribute: ITEM_NAME,
                value: Value::String("canonical-preserved-".repeat(100)).into(),
            }],
            1000,
        )
        .unwrap()
        .db_after;
    let build = crate::persistent_tree::build_tree(
        IndexOrder::Eavt,
        true,
        database.datoms(View::History, IndexOrder::Eavt),
        &Default::default(),
    )
    .unwrap();
    for (hash, bytes) in build.nodes.iter() {
        client
            .execute(
                "INSERT INTO atomic_tree_nodes(node_hash,payload) VALUES($1,$2)",
                &[&&hash[..], &bytes.as_ref()],
            )
            .unwrap();
    }
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    let mut client = client_in_schema(&connection, &isolated);
    let after = client
        .query(
            "SELECT version,checksum FROM atomic_schema_migrations ORDER BY version",
            &[],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get::<_, i64>(0), row.get::<_, Vec<u8>>(1)))
        .collect::<Vec<_>>();
    assert_eq!(checksums, after);
    assert_eq!(
        client
            .query_one("SELECT count(*) FROM atomic_tree_node_blocks", &[])
            .unwrap()
            .get::<_, i64>(0),
        0
    );
    for (hash, bytes) in build.nodes.iter() {
        let loaded = crate::compressed_nodes::load_node_block(&mut client, *hash)
            .unwrap()
            .unwrap();
        assert_eq!(loaded.canonical, bytes.as_ref());
        assert_eq!(loaded.stats.compressed_hits, 0);
    }
    let batch = build
        .nodes
        .iter()
        .map(|(hash, bytes)| (*hash, bytes.as_ref()))
        .collect::<Vec<_>>();
    let compressed = crate::compressed_nodes::store_node_blocks(&mut client, &batch).unwrap();
    assert!(compressed.inserted > 0);
    for (hash, bytes) in build.nodes.iter() {
        let raw: Vec<u8> = client
            .query_one(
                "SELECT payload FROM atomic_tree_nodes WHERE node_hash=$1",
                &[&&hash[..]],
            )
            .unwrap()
            .get(0);
        assert_eq!(raw, bytes.as_ref());
        assert_eq!(
            crate::compressed_nodes::load_node_block(&mut client, *hash)
                .unwrap()
                .unwrap()
                .canonical,
            raw
        );
    }
    drop(client);
    drop(migrator);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn already_current_migrate_does_not_lock_live_log_tables_for_repair() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_current_concurrent");
    let client = client_in_schema(&connection, &isolated);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    // These are the relation lock modes held by ordinary legacy/native log
    // writers. Before the current-schema fast path became conditional, the
    // program-reference repair requested SHARE in the opposite order and a
    // harmless startup migrate either blocked or deadlocked the writer.
    let mut writer = client_in_schema(&connection, &isolated);
    let mut writer_transaction = writer.transaction().unwrap();
    writer_transaction
        .batch_execute(
            "LOCK TABLE atomic_log_generations IN ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_generation_transactions IN ROW EXCLUSIVE MODE; \
             LOCK TABLE atomic_transactions IN ROW EXCLUSIVE MODE",
        )
        .unwrap();

    let mut client = client_in_schema(&connection, &isolated);
    // Other isolated fixtures share the migrator's database-wide advisory
    // lock. Wait for that setup contention before imposing the relation-lock
    // deadline under test. The session guard must be on this same backend:
    // migrate's nested transaction lock is reentrant, and dropping the
    // migrator releases the guard without changing production lock semantics.
    client
        .query_one("SELECT pg_advisory_lock($1)", &[&0x41544f4d_i64])
        .unwrap();
    client.batch_execute("SET lock_timeout TO '500ms'").unwrap();
    let mut concurrent_migrator = crate::PostgresMigrator::from_client(client);
    concurrent_migrator.migrate().unwrap();
    drop(concurrent_migrator);
    writer_transaction.rollback().unwrap();
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn private_publication_faults_are_invisible_and_unknown_outcome_resolves_once() {
    let Some(connection) = connection() else {
        return;
    };
    for fault in [
        CommitFault::BeforeTransactionInsert,
        CommitFault::AfterTransactionInsert,
        CommitFault::AfterHeadUpdate,
    ] {
        let database_id = unique("private_rollback");
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        publish_native_base(&connection, &database_id);
        let error = store
            .transact_with_fault(
                &database_id,
                "request-1",
                1,
                &add_item("rolled-back", 1),
                1_000,
                fault,
            )
            .unwrap_err();
        assert_eq!(error.code, "postgres/injected-failure");
        assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
        assert!(
            store
                .resolve_request_outcome(&database_id, "request-1")
                .unwrap()
                .is_none()
        );
    }

    let database_id = unique("private_unknown");
    let mut store = migrated_store(&connection);
    let db_before = store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);
    let error = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(
        error.details.get("ambiguity_kind").map(String::as_str),
        Some("publication")
    );
    let resolved = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("the committed request has a durable outcome");
    assert!(resolved.replayed);
    assert_eq!(resolved.basis_t, 2);
    assert_value_eq_database(&resolved.db_before, &db_before);
    assert_eq!(resolved.database.basis_t(), 2);
    let resolved_again = store
        .resolve_request_outcome(&database_id, "request-unknown")
        .unwrap()
        .expect("outcome lookup remains stable");
    assert_eq!(resolved_again.tx_hash, resolved.tx_hash);
    assert_eq!(resolved_again.tempids, resolved.tempids);
    assert_eq!(resolved_again.tx_data, resolved.tx_data);
    assert_database_values_eq(&resolved_again.db_before, &resolved.db_before);
    assert_database_values_eq(&resolved_again.database, &resolved.database);

    // Retrying through the authoritative writer reconstructs exactly the
    // bound old root plus one authenticated transaction, then installs that
    // immutable value without retaining an eager database.
    let replayed = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::None,
        )
        .unwrap();
    assert!(replayed.replayed);
    assert_database_values_eq(&replayed.db_before, &resolved.db_before);
    assert_database_values_eq(&replayed.database, &resolved.database);
    assert!(
        resolved
            .database
            .shares_tiered_read_core(&replayed.database)
    );
    assert!(
        replayed
            .db_before
            .shares_tiered_read_core(&replayed.database)
    );

    // Retaining many exact old reports must retain immutable states and their
    // pins, but not create one PeerIo session, tree cache, and pin manager per
    // retry. All reconstructed values reuse the installed writer read core.
    let replay_core = replayed.database.clone();
    let mut retained_replays = vec![replayed];
    for _ in 0..16 {
        let retained = store
            .transact_with_fault(
                &database_id,
                "request-unknown",
                1,
                &add_item("committed", 1),
                1_000,
                CommitFault::None,
            )
            .unwrap();
        assert!(retained.replayed);
        assert!(retained.db_before.shares_tiered_read_core(&replay_core));
        assert!(retained.database.shares_tiered_read_core(&replay_core));
        retained_replays.push(retained);
    }
    assert_eq!(retained_replays.len(), 17);
    let residency = store.writer_residency_stats(&database_id);
    assert_eq!(residency.eager_database_values, 0);
    assert_eq!(residency.eager_current_facts, 0);
    assert_eq!(residency.eager_history_datoms, 0);

    // Losing the acknowledgment of a read-only idempotent replay is not a
    // possibly-new publication. It remains an honest UnknownOutcome for that
    // response, but the service must not emit a second committed report.
    let outcome_read_error = store
        .transact_with_fault(
            &database_id,
            "request-unknown",
            1,
            &add_item("committed", 1),
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(outcome_read_error.category, ErrorCategory::UnknownOutcome);
    assert_eq!(
        outcome_read_error
            .details
            .get("ambiguity_kind")
            .map(String::as_str),
        Some("outcome-read")
    );
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 2);

    let fresh = store
        .transact_with_fault(
            &database_id,
            "request-after-replay",
            2,
            // Update the existing identity so the read-work assertion below
            // observes a non-empty AVET/EAVT result rather than a perfectly
            // valid collection of zero-result prefix probes.
            &add_item("committed", 2),
            2_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(fresh.basis_t, 3);
    let residency = store.writer_residency_stats(&database_id);
    assert_eq!(residency.eager_database_values, 0);
    assert!(residency.publication_revision > 0);
    assert!(residency.recent_datoms > 0);
    assert!(residency.last_transaction_read_datoms > 0);
    assert!(residency.last_transaction_read_bytes > 0);
    assert!(residency.last_commitment_node_visits > 0);
    assert!(residency.last_commitment_node_hashes > 0);
    assert!(residency.last_commitment_leaf_changes > 0);

    let mut verifier = Client::connect(&connection, NoTls).unwrap();
    let bound: (i64, i64) = verifier
        .query_one(
            "SELECT count(*) FILTER (WHERE request_kind = 2), \
                    (SELECT count(*) FROM atomic_generation_request_bases b \
                      WHERE b.database_id = $1) \
               FROM atomic_generation_requests r WHERE r.database_id = $1",
            &[&database_id],
        )
        .map(|row| (row.get(0), row.get(1)))
        .unwrap();
    assert_eq!(bound, (2, 2));
}

#[test]
fn retained_receipt_cores_survive_reconnect_without_retaining_writer_state() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("weak_receipt_core");
    let application = unique("receipt_core");
    let pin_application = format!(
        "atomic-pin-{}",
        sha256(database_id.as_bytes())[..16]
            .iter()
            .map(|byte| format!("{byte:02x}"))
            .collect::<String>()
    );
    let tagged_connection =
        if connection.starts_with("postgres://") || connection.starts_with("postgresql://") {
            format!(
                "{connection}{}application_name={application}",
                if connection.contains('?') { '&' } else { '?' }
            )
        } else {
            format!("{connection} application_name={application}")
        };
    let mut store = migrated_store(&tagged_connection);
    store
        .set_capacity_limits(CapacityLimits {
            writer_tree_cache_entries: 8,
            writer_tree_cache_bytes: 64 * 1024,
            ..CapacityLimits::default()
        })
        .unwrap();
    store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&tagged_connection, &database_id);
    let operations = add_item("known-committed", 17);
    let ambiguous = store
        .transact_with_fault(
            &database_id,
            "receipt-reuse",
            1,
            &operations,
            1_000,
            CommitFault::AfterCommitBeforeResponse,
        )
        .unwrap_err();
    assert_eq!(ambiguous.category, ErrorCategory::UnknownOutcome);
    store.reconnect().unwrap();
    // A cold immutable receipt must remain reconstructable even when its
    // committed recent tail is larger than the new writer-admission bound.
    store
        .set_writer_recent_limits(crate::recent::RecentLimits {
            soft_datoms: 1,
            soft_bytes: u64::MAX - 1,
            hard_datoms: 1,
            hard_bytes: u64::MAX,
        })
        .unwrap();
    let first = store
        .resolve_request_outcome(&database_id, "receipt-reuse")
        .unwrap()
        .unwrap();
    assert_eq!(first.basis_t, 2);
    let entity = first.tempids["item"];
    assert_eq!(
        first.database.values(entity, ITEM_COUNT).unwrap(),
        [Value::Long(17)]
    );
    assert!(
        first
            .db_before
            .values(entity, ITEM_COUNT)
            .unwrap()
            .is_empty()
    );
    assert!(first.tx_data.len() > 1);
    assert_eq!(
        store
            .writer_residency_stats(&database_id)
            .publication_revision,
        0
    );

    let mut observer = Client::connect(&connection, NoTls).unwrap();
    let backend_count = |observer: &mut Client| -> i64 {
        observer
            .query_one(
                "SELECT count(*) FROM pg_stat_activity \
                 WHERE datname = current_database() AND application_name IN ($1, $2) \
                   AND backend_type = 'client backend'",
                &[&application, &pin_application],
            )
            .unwrap()
            .get(0)
    };
    let await_backend_bound = |observer: &mut Client, maximum: i64| -> i64 {
        let deadline = std::time::Instant::now() + std::time::Duration::from_secs(5);
        loop {
            let count = backend_count(observer);
            if count <= maximum {
                return count;
            }
            assert!(
                std::time::Instant::now() < deadline,
                "retained receipts use {count} PostgreSQL backends, expected at most {maximum}"
            );
            std::thread::sleep(std::time::Duration::from_millis(10));
        }
    };
    // One store session plus one shared read lane and one pin-manager lane.
    let initial_backends = await_backend_bound(&mut observer, 3);
    assert_eq!(initial_backends, 3);
    for attempt in 0..24 {
        if attempt % 3 == 0 {
            store.reconnect().unwrap();
        }
        let latest = store
            .transact_with_fault(
                &database_id,
                "receipt-reuse",
                1,
                &operations,
                1_000,
                CommitFault::None,
            )
            .unwrap();
        assert!(latest.replayed);
        assert_eq!(latest.tx_hash, first.tx_hash);
        assert_eq!(latest.tempids, first.tempids);
        assert_eq!(latest.tx_data, first.tx_data);
        assert_database_values_eq(&latest.db_before, &first.db_before);
        assert_database_values_eq(&latest.database, &first.database);
        assert!(latest.database.shares_tiered_read_core(&first.database));
        let snapshot = latest.database.native_tiered_snapshot().unwrap();
        let cache = snapshot.tree_cache_stats();
        assert!(cache.current_entries <= 8);
        assert!(cache.current_bytes <= 64 * 1024);
        assert!(cache.peak_entries <= 8);
        assert!(cache.peak_bytes <= 64 * 1024);
        assert!(snapshot.recent_stats().datoms > 1);
        assert_eq!(
            store
                .writer_residency_stats(&database_id)
                .publication_revision,
            0
        );
        drop(snapshot);
        // Drop the latest returned state while the FIRST receipt still owns
        // the core. A weak pointer to only the latest state would fail here.
        drop(latest);
        assert_eq!(
            await_backend_bound(&mut observer, initial_backends),
            initial_backends
        );
    }
    let rejected = store
        .transact_with_fault(
            &database_id,
            "new-over-lowered-limit",
            2,
            &add_item("not-admitted", 99),
            2_000,
            CommitFault::None,
        )
        .unwrap_err();
    assert_eq!(rejected.code, "recent/hard-capacity");
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 2);
    assert_eq!(
        first.database.values(entity, ITEM_COUNT).unwrap(),
        [Value::Long(17)]
    );
    drop(first);
    assert_eq!(
        await_backend_bound(&mut observer, 1),
        1,
        "weak reuse must not retain a read lane, endpoint, or pins after all receipts drop"
    );
    eprintln!(
        "receipt-core witness: 24 retries, 8 reconnects, {initial_backends} live PostgreSQL sessions with receipt retained, 1 store session after receipt drop; cache <=8 entries/65536 bytes"
    );
}

#[test]
fn lower_recent_limits_preserve_exact_retry_and_the_fresher_live_head() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("lower_limits_exact_retry");
    let mut original = migrated_store(&connection);
    original.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);
    let committed = original
        .transact_with_fault(
            &database_id,
            "oversized-under-new-limit",
            1,
            &add_item("already-committed", 1),
            1_000,
            CommitFault::None,
        )
        .unwrap();
    assert_eq!(committed.basis_t, 2);
    assert!(committed.tx_data.len() > 1);
    publish_native_base(&connection, &database_id);
    drop(original);

    // Simulate a restart with a limit lower than an outcome that was valid
    // when committed. The covering head opens with an empty recent tail.
    let mut restarted = PostgresStore::connect(&connection).unwrap();
    restarted
        .set_writer_recent_limits(crate::recent::RecentLimits {
            soft_datoms: 1,
            soft_bytes: u64::MAX - 1,
            hard_datoms: 1,
            hard_bytes: u64::MAX,
        })
        .unwrap();
    let lease = restarted
        .acquire_lease(&database_id, &unique("lower-limit-holder"), 60_000)
        .unwrap();
    restarted.activate_transactor_state(&lease, 60_000).unwrap();
    assert_eq!(
        restarted.writer_residency_stats(&database_id).recent_datoms,
        0
    );

    // The receipt's archived db-before plus its one authenticated transaction
    // must remain reconstructable. Installing that older physical shape would
    // unnecessarily reintroduce the oversized tail into the live writer, so
    // the already-open covering head remains authoritative for residency.
    let replayed = restarted
        .transact_with_fault(
            &database_id,
            "oversized-under-new-limit",
            1,
            &add_item("already-committed", 1),
            1_000,
            CommitFault::None,
        )
        .unwrap();
    assert!(replayed.replayed);
    assert_eq!(replayed.basis_t, 2);
    assert_eq!(replayed.tx_data, committed.tx_data);
    assert_eq!(
        restarted.writer_residency_stats(&database_id).recent_datoms,
        0
    );

    // The reconstruction exception is not an admission loophole: an ordinary
    // new transaction that crosses the current hard cap is still rejected and
    // leaves the committed head unchanged.
    let error = restarted
        .transact_with_fault(
            &database_id,
            "new-over-current-limit",
            2,
            &add_item("must-consolidate-first", 2),
            2_000,
            CommitFault::None,
        )
        .unwrap_err();
    assert_eq!(error.code, "recent/hard-capacity");
    assert_eq!(restarted.recover(&database_id).unwrap().basis_t(), 2);
    assert_eq!(
        restarted.writer_residency_stats(&database_id).recent_datoms,
        0
    );
    restarted.release_lease(&lease).unwrap();
}

#[test]
fn process_death_at_private_precommit_kill_point_is_invisible() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("private_process_death");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();
    publish_native_base(&connection, &database_id);
    drop(store);

    let status = Command::new(std::env::current_exe().unwrap())
        .args([
            "--ignored",
            "--exact",
            "postgres_internal_tests::postgres_crash_worker",
        ])
        .env("ATOMIC_POSTGRES_URL", &connection)
        .env("ATOMIC_CRASH_DATABASE", &database_id)
        .status()
        .unwrap();
    assert!(!status.success());

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 1);
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_generation_transactions \
              WHERE database_id = $1 AND basis_t = 2",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(count, 0);
}

#[test]
#[ignore = "subprocess worker for the process-death test"]
fn postgres_crash_worker() {
    let Ok(connection) = std::env::var("ATOMIC_POSTGRES_URL") else {
        return;
    };
    let Ok(database_id) = std::env::var("ATOMIC_CRASH_DATABASE") else {
        return;
    };
    let mut store = PostgresStore::connect(&connection).unwrap();
    let _ = store.transact_with_fault(
        &database_id,
        "crash-request",
        1,
        &add_item("never-visible", 1),
        1_000,
        CommitFault::AfterHeadUpdateProcessAbort,
    );
    unreachable!();
}
