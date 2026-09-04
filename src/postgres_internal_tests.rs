use crate::postgres::{CapacityLimits, CommitFault, MIGRATIONS, PostgresStore};
use crate::state_commitment::checkpoint_state_hash;
use crate::{
    Attribute, Cardinality, DB_ENTITY_ATTRS, DB_ENTITY_PREDS, DB_FN, DB_IDENT, Database,
    DurableTransaction, EntityRef, ErrorCategory, IndexOrder, IndexPrefix, Instruction, Keyword,
    Program, ProgramKind, Schema, Symbol, TxOp, TxValue, Unique, Value, ValueType, View,
    encode_genesis, encode_transaction, request_digest, sha256, transaction_hash,
};
use postgres::{Client, NoTls};
use std::collections::BTreeMap;
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
    let update = vec![
        TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_COUNT,
            value: Value::Long(2).into(),
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
    let binding_role_reads = u64::try_from(
        committed
            .database
            .datoms_with_prefix(&IndexPrefix::Aevt {
                attribute: DB_ENTITY_PREDS as u32,
                entity: None,
                value: None,
            })
            .unwrap()
            .len(),
    )
    .unwrap();
    let before_schema_rejection = store.writer_residency_stats(&database_id);
    let without_successor_probe = schema_assessed.read_work.datoms
        + tx_instant_reads
        + binding_role_reads
        + commitment_prior_reads;
    assert_eq!(without_successor_probe + 1, 6);
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

fn create_isolated_schema(connection: &str, prefix: &str) -> String {
    let schema = unique(prefix);
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("CREATE SCHEMA \"{schema}\""))
        .unwrap();
    schema
}

fn client_in_schema(connection: &str, schema: &str) -> Client {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("SET search_path TO \"{schema}\""))
        .unwrap();
    client
}

fn drop_isolated_schema(connection: &str, schema: &str) {
    let mut client = Client::connect(connection, NoTls).unwrap();
    client
        .batch_execute(&format!("DROP SCHEMA \"{schema}\" CASCADE"))
        .unwrap();
}

fn install_migration_prefix(client: &mut Client, through: i64) {
    for (version, sql) in MIGRATIONS.iter().filter(|(version, _)| *version <= through) {
        client.batch_execute(sql).unwrap();
        let checksum = crate::sha256(sql.as_bytes());
        client
            .execute(
                "INSERT INTO atomic_schema_migrations (version, checksum) VALUES ($1, $2)",
                &[version, &&checksum[..]],
            )
            .unwrap();
    }
}

/// Provision the alias-bound generation-zero representation implemented by
/// the v9-v11 catalog. Upgrade fixtures must write that historical shape
/// directly; routing them through the current store would test v14 creation
/// SQL against columns and generation tables that intentionally do not exist.
fn provision_legacy_generation_zero_database(
    client: &mut Client,
    database_id: &str,
    schema: Schema,
) -> Database {
    let schema_ops = schema
        .attributes()
        .cloned()
        .map(TxOp::InstallAttribute)
        .collect::<Vec<_>>();
    let database = Database::new(schema).unwrap();
    let bootstrap = Database::bootstrap().unwrap();
    let genesis = encode_genesis(bootstrap.genesis_datoms()).unwrap();
    let genesis_hash = sha256(&genesis);
    let initial = if schema_ops.is_empty() {
        None
    } else {
        assert_eq!(database.basis_t(), 1);
        let tx = crate::t_to_tx(1).unwrap();
        let envelope = DurableTransaction {
            database_id: database_id.to_owned(),
            basis_t: 1,
            previous_hash: genesis_hash,
            eidx_frontier: database.eidx_frontier(),
            tempids: BTreeMap::new(),
            tx_data: database
                .datoms(View::History, IndexOrder::Eavt)
                .into_iter()
                .filter(|datom| datom.tx == tx)
                .collect(),
        };
        let payload = encode_transaction(&envelope).unwrap();
        let tx_hash = transaction_hash(&payload);
        let state_hash = checkpoint_state_hash(&database).unwrap();
        let request_hash = request_digest(&schema_ops, 0, 0).unwrap();
        Some((payload, tx_hash, state_hash, request_hash))
    };

    let mut transaction = client.transaction().unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_databases (database_id, genesis, genesis_hash) \
             VALUES ($1, $2, $3)",
            &[&database_id, &&genesis[..], &&genesis_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
            &[&database_id, &&genesis_hash[..]],
        )
        .unwrap();
    transaction
        .execute(
            "INSERT INTO atomic_database_generations (database_id, excision_generation) \
             VALUES ($1, 0)",
            &[&database_id],
        )
        .unwrap();
    if let Some((payload, tx_hash, state_hash, request_hash)) = initial {
        transaction
            .execute(
                "INSERT INTO atomic_transactions \
                     (database_id, basis_t, previous_hash, tx_hash, payload, state_hash) \
                 VALUES ($1, 1, $2, $3, $4, $5)",
                &[
                    &database_id,
                    &&genesis_hash[..],
                    &&tx_hash[..],
                    &payload,
                    &&state_hash[..],
                ],
            )
            .unwrap();
        transaction
            .execute(
                "INSERT INTO atomic_requests \
                     (database_id, request_key, request_digest, basis_t, tx_hash) \
                 VALUES ($1, '__atomic/create-schema/v1', $2, 1, $3)",
                &[&database_id, &&request_hash[..], &&tx_hash[..]],
            )
            .unwrap();
        transaction
            .execute(
                "UPDATE atomic_heads SET basis_t = 1, tx_hash = $2 WHERE database_id = $1",
                &[&database_id, &&tx_hash[..]],
            )
            .unwrap();
    }
    transaction.commit().unwrap();
    database
}

fn authoritative_rows(client: &mut Client, database_id: &str) -> Vec<(i64, Vec<u8>, Vec<u8>)> {
    client
        .query(
            "SELECT basis_t, tx_hash, payload FROM atomic_transactions \
             WHERE database_id = $1 ORDER BY basis_t",
            &[&database_id],
        )
        .unwrap()
        .into_iter()
        .map(|row| (row.get(0), row.get(1), row.get(2)))
        .collect()
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
    assert_eq!(versions, crate::POSTGRES_SCHEMA_VERSION);
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
    client.batch_execute("SET lock_timeout TO '500ms'").unwrap();
    let mut concurrent_migrator = crate::PostgresMigrator::from_client(client);
    concurrent_migrator.migrate().unwrap();
    drop(concurrent_migrator);
    writer_transaction.rollback().unwrap();
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_pre_v6_catalog_is_rejected_before_any_schema_mutation() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_pre_v6");
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, 5);
    client
        .execute(
            "INSERT INTO atomic_databases \
             (database_id, bootstrap_schema, bootstrap_hash) VALUES ($1, $2, $3)",
            &[&"old", &vec![1_u8, 0, 0, 0], &&[0_u8; 32][..]],
        )
        .unwrap();
    client
        .execute(
            "INSERT INTO atomic_heads (database_id, basis_t, tx_hash) VALUES ($1, 0, $2)",
            &[&"old", &&[0_u8; 32][..]],
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    let error = migrator.migrate().unwrap_err();
    assert_eq!(error.category, ErrorCategory::Unsupported);
    assert_eq!(error.code, "postgres/upgrade-rebuild-required");

    let mut client = client_in_schema(&connection, &isolated);
    let latest: i64 = client
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    let old_column_remains: bool = client
        .query_one(
            "SELECT EXISTS (SELECT 1 FROM information_schema.columns \
             WHERE table_schema = current_schema() AND table_name = 'atomic_databases' \
               AND column_name = 'bootstrap_schema')",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(latest, 5);
    assert!(old_column_remains);
    drop(client);
    drop(migrator);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_v6_log_is_replayed_for_commitments_and_upgrades_without_loss() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_v6");
    let database_id = "canonical-v6";
    let mut client = client_in_schema(&connection, &isolated);
    // Construct canonical v3 logical content using the first schema that has
    // the commitment column, then faithfully remove migrations 7--9. This is
    // the exact v6 authoritative catalog; migrations 7/8 add no log fields.
    install_migration_prefix(&mut client, 9);
    let before = provision_legacy_generation_zero_database(&mut client, database_id, schema());

    let mut client = client_in_schema(&connection, &isolated);
    let rows_before = authoritative_rows(&mut client, database_id);
    client
        .batch_execute(
            "CREATE OR REPLACE FUNCTION atomic_validate_transaction_insert() \
             RETURNS trigger LANGUAGE plpgsql AS $$ \
             DECLARE current_basis bigint; current_hash bytea; BEGIN \
               SELECT basis_t, tx_hash INTO current_basis, current_hash \
                 FROM atomic_heads WHERE database_id = NEW.database_id; \
               IF NOT FOUND THEN RAISE EXCEPTION 'Atomic database head does not exist' \
                 USING ERRCODE = '23503'; END IF; \
               IF NEW.basis_t <> current_basis + 1 OR NEW.previous_hash <> current_hash THEN \
                 RAISE EXCEPTION 'Atomic transaction does not extend the current head' \
                 USING ERRCODE = '40001'; END IF; RETURN NEW; END; $$; \
             DROP TRIGGER IF EXISTS atomic_index_publications_validate_insert \
               ON atomic_index_publications; \
             DROP FUNCTION IF EXISTS atomic_validate_index_publication(); \
             DROP TABLE atomic_index_publications; \
             ALTER TABLE atomic_transactions DROP COLUMN state_hash; \
             ALTER TABLE atomic_programs DROP CONSTRAINT atomic_programs_kind_check; \
             ALTER TABLE atomic_programs ADD CONSTRAINT atomic_programs_kind_check \
               CHECK (kind BETWEEN 0 AND 2); \
             DELETE FROM atomic_schema_migrations WHERE version >= 7",
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    assert_eq!(authoritative_rows(&mut client, database_id), rows_before);
    let committed: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    let authenticated: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions WHERE database_id = $1 \
             AND state_hash <> decode(repeat('00', 32), 'hex')",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert!(committed > 0);
    assert_eq!(authenticated, committed);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &before);
    drop(store);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn populated_v11_to_v12_preserves_authoritative_bytes_and_exact_state() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_v11");
    let database_id = "canonical-v11";
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, 11);
    let before = provision_legacy_generation_zero_database(&mut client, database_id, schema());
    let mut client = client_in_schema(&connection, &isolated);
    let rows_before = authoritative_rows(&mut client, database_id);
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    assert_eq!(authoritative_rows(&mut client, database_id), rows_before);
    let latest: i64 = client
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(latest, crate::POSTGRES_SCHEMA_VERSION);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &before);
    drop(store);
    drop_isolated_schema(&connection, &isolated);
}

#[test]
fn already_current_catalog_repairs_legacy_zero_state_commitments() {
    let Some(connection) = connection() else {
        return;
    };
    let isolated = create_isolated_schema(&connection, "migration_current_zero");
    let database_id = "current-with-zero-commitments";
    let mut client = client_in_schema(&connection, &isolated);
    install_migration_prefix(&mut client, crate::POSTGRES_SCHEMA_VERSION);
    let mut store = PostgresStore::from_client(client);
    let expected = store.create_database(database_id, schema()).unwrap();
    drop(store);

    let mut client = client_in_schema(&connection, &isolated);
    client
        .batch_execute(
            "ALTER TABLE atomic_transactions DISABLE TRIGGER atomic_transactions_immutable; \
             UPDATE atomic_transactions \
                SET state_hash = decode(repeat('00', 32), 'hex') \
              WHERE database_id = 'current-with-zero-commitments'; \
             ALTER TABLE atomic_transactions ENABLE TRIGGER atomic_transactions_immutable",
        )
        .unwrap();
    let mut migrator = crate::PostgresMigrator::from_client(client);
    migrator.migrate().unwrap();
    drop(migrator);

    let mut client = client_in_schema(&connection, &isolated);
    let remaining: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_transactions \
             WHERE database_id = $1 \
               AND state_hash = decode(repeat('00', 32), 'hex')",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(remaining, 0);
    let mut store = PostgresStore::from_client(client);
    assert_database_eq(&store.recover(database_id).unwrap(), &expected);
    drop(store);
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
