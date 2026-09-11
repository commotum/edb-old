use atomic_core::{
    Attribute, AttributeName, Cardinality, DB_IDENT, EntityIdentifier, EntityRef, Keyword,
    PostgresStore, PullAttribute, PullPattern, Schema, TxOp, TxValue, Unique, Value, ValueType,
    canonical_genesis_datoms, encode_genesis, sha256,
};
use postgres::{Client, NoTls};
use std::process::Command;
use std::sync::{Arc, Barrier};
use std::time::{SystemTime, UNIX_EPOCH};

mod common;

const ITEM_NAME: u32 = 1_000;
const ITEM_COUNT: u32 = 1_001;
const ITEM_TAG: u32 = 1_002;
const ITEM_KIND: u32 = 1_002;

fn connection() -> Option<String> {
    std::env::var("ATOMIC_POSTGRES_URL").ok()
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

fn unique(prefix: &str) -> String {
    let nanos = SystemTime::now()
        .duration_since(UNIX_EPOCH)
        .unwrap()
        .as_nanos();
    format!("{prefix}_{}_{}", std::process::id(), nanos)
}

fn migrated_store(connection: &str) -> PostgresStore {
    let mut migrator = atomic_core::PostgresMigrator::connect(connection).unwrap();
    migrator.migrate().unwrap();
    PostgresStore::connect(connection).unwrap()
}

fn assert_database_eq(
    left: &impl common::InformationSource,
    right: &impl common::InformationSource,
) {
    common::assert_same_information(left, right);
}

#[test]
fn migrations_are_idempotent() {
    let Some(connection) = connection() else {
        return;
    };
    let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
    migrator.migrate().unwrap();
    migrator.migrate().unwrap();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let versions: i64 = client
        .query_one("SELECT count(*) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(versions, 1);
    let installed: i64 = client
        .query_one("SELECT version FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    assert_eq!(installed, atomic_core::POSTGRES_SCHEMA_VERSION);
}

#[test]
fn migration_commit_recovery_and_idempotency_are_exact() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("exact");
    let mut store = migrated_store(&connection);
    let created = store.create_database(&database_id, schema()).unwrap();
    assert_eq!(created.basis_t(), 1);
    assert_database_eq(&created, &store.recover(&database_id).unwrap());

    let basis_zero = store.recover_basis(&database_id, 0).unwrap();
    assert_eq!(basis_zero.basis_t(), 0);
    assert!(basis_zero.schema().attribute(ITEM_NAME).is_err());
    let mut client = Client::connect(&connection, NoTls).unwrap();
    let catalog = client
        .query_one(
            "SELECT genesis, genesis_hash FROM atomic_databases WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap();
    let genesis: Vec<u8> = catalog.get(0);
    let genesis_hash: Vec<u8> = catalog.get(1);
    assert_eq!(
        genesis,
        encode_genesis(&canonical_genesis_datoms()).unwrap()
    );
    assert_eq!(genesis_hash, sha256(&genesis));

    let ops = add_item("one", 1);
    let service = common::start_service(&connection, &database_id);
    let committed = common::transact(&service, "request-1", 1, &ops, 1_000);
    assert!(!committed.replayed);
    assert_eq!(committed.basis_t, 2);
    let recovered = store.recover(&database_id).unwrap();
    assert_database_eq(&committed.db_after, &recovered);
    assert_eq!(
        recovered
            .schema()
            .resolve_ident(&Keyword::new("item", "quantity")),
        Some(ITEM_COUNT)
    );

    let schema_commit = common::transact(
        &service,
        "request-schema",
        2,
        &[TxOp::InstallAttribute(Attribute::new(
            ITEM_TAG,
            Keyword::new("item", "tag"),
            ValueType::String,
            Cardinality::Many,
        ))],
        1_001,
    );
    let entity = committed.tempids["item"];
    let mut latest = common::transact(
        &service,
        "request-tag",
        3,
        &[TxOp::Add {
            entity: EntityRef::Id(entity),
            attribute: ITEM_TAG,
            value: TxValue::Scalar(Value::String("durable-schema".into())),
        }],
        1_002,
    );
    assert!(schema_commit.tx_data.iter().any(|datom| {
        datom.entity == 0
            && datom.attribute == atomic_core::DB_INSTALL_ATTRIBUTE as u32
            && datom.value == Value::Ref(ITEM_TAG.into())
            && datom.added
    }));

    for value in 2..=20 {
        latest = common::transact(
            &service,
            &format!("request-count-{value}"),
            latest.basis_t,
            &[TxOp::Add {
                entity: EntityRef::Id(entity),
                attribute: ITEM_COUNT,
                value: TxValue::Scalar(Value::Long(value)),
            }],
            1_001 + value,
        );
    }
    assert_database_eq(&latest.db_after, &store.recover(&database_id).unwrap());

    let retry = common::transact(&service, "request-1", 1, &ops, 1_000);
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, committed.basis_t);
    assert_eq!(retry.tx_hash, committed.tx_hash);
    assert_eq!(retry.tempids, committed.tempids);
    assert_eq!(retry.tx_data, committed.tx_data);
    assert_database_eq(&retry.db_after, &committed.db_after);
    service.shutdown();

    let mut reopened = PostgresStore::connect(&connection).unwrap();
    assert_database_eq(&latest.db_after, &reopened.recover(&database_id).unwrap());
    let restarted_service = common::start_service(&connection, &database_id);
    let next = common::transact(
        &restarted_service,
        "request-next-entity",
        latest.basis_t,
        &add_item("two", 2),
        1_022,
    );
    assert_eq!(
        next.tempids["item"],
        atomic_core::make_eid(atomic_core::USER_PARTITION, 1_003).unwrap()
    );
    assert_database_eq(&next.db_after, &reopened.recover(&database_id).unwrap());

    let mismatch = common::try_transact(
        &restarted_service,
        "request-1",
        1,
        &add_item("other", 2),
        1_000,
    )
    .unwrap_err();
    assert_eq!(mismatch.code, "postgres/idempotency-key-reused");
    restarted_service.shutdown();
}

#[test]
fn general_idents_aliases_and_schema_recover_from_only_the_ordinary_log() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("ident_restart");
    let old_holder = Keyword::new("holder", "old");
    let new_holder = Keyword::new("holder", "new");
    let old_enum = Keyword::new("kind", "old");
    let new_enum = Keyword::new("kind", "new");
    let old_attribute = Keyword::new("item", "kind");
    let new_attribute = Keyword::new("item", "category");
    let mut app_schema = schema();
    app_schema
        .install(Attribute::new(
            ITEM_KIND,
            old_attribute.clone(),
            ValueType::Ref,
            Cardinality::One,
        ))
        .unwrap();

    let mut store = migrated_store(&connection);
    let created = store.create_database(&database_id, app_schema).unwrap();
    let service = common::start_service(&connection, &database_id);
    let populated = common::transact(
        &service,
        "idents-create",
        created.basis_t(),
        &[
            TxOp::Add {
                entity: EntityRef::Temp("enum".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(old_enum.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: DB_IDENT as u32,
                value: Value::Keyword(old_holder.clone()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: ITEM_NAME,
                value: Value::String("durable holder".into()).into(),
            },
            TxOp::Add {
                entity: EntityRef::Temp("holder".into()),
                attribute: ITEM_KIND,
                value: TxValue::Entity(EntityRef::Temp("enum".into())),
            },
        ],
        1_000,
    );
    let holder = populated.tempids["holder"];
    let enum_id = populated.tempids["enum"];
    let rename_ops = vec![
        TxOp::Add {
            entity: EntityRef::Id(holder),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_holder.clone()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(enum_id),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_enum.clone()).into(),
        },
        TxOp::Add {
            entity: EntityRef::Id(u64::from(ITEM_KIND)),
            attribute: DB_IDENT as u32,
            value: Value::Keyword(new_attribute.clone()).into(),
        },
    ];
    let renamed = common::transact(
        &service,
        "idents-rename",
        populated.basis_t,
        &rename_ops,
        2_000,
    );
    assert!(renamed.tx_data.iter().all(|datom| {
        datom.attribute != atomic_core::DB_INSTALL_ATTRIBUTE as u32
            && datom.attribute != atomic_core::DB_ALTER_ATTRIBUTE as u32
    }));
    let advanced = common::transact(
        &service,
        "idents-advance",
        renamed.basis_t,
        &[TxOp::Add {
            entity: EntityRef::Id(holder),
            attribute: ITEM_COUNT,
            value: Value::Long(7).into(),
        }],
        3_000,
    );
    let retry = common::transact(
        &service,
        "idents-rename",
        populated.basis_t,
        &rename_ops,
        2_000,
    );
    assert!(retry.replayed);
    assert_eq!(retry.basis_t, renamed.basis_t);
    assert_eq!(retry.tx_hash, renamed.tx_hash);
    assert_eq!(retry.tempids, renamed.tempids);
    assert_eq!(retry.tx_data, renamed.tx_data);
    assert_database_eq(&retry.db_after, &renamed.db_after);

    service.shutdown();
    drop(store);
    let mut reopened = PostgresStore::connect(&connection).unwrap();
    let recovered = reopened.recover(&database_id).unwrap();
    assert_database_eq(&recovered, &advanced.db_after);
    assert_database_eq(&recovered.rebuild_derived_caches().unwrap(), &recovered);
    assert_eq!(recovered.entid(&old_holder), Some(holder));
    assert_eq!(recovered.entid(&new_holder), Some(holder));
    assert_eq!(recovered.entid(&old_enum), Some(enum_id));
    assert_eq!(recovered.entid(&new_enum), Some(enum_id));
    assert_eq!(recovered.entid(&old_attribute), Some(u64::from(ITEM_KIND)));
    assert_eq!(recovered.entid(&new_attribute), Some(u64::from(ITEM_KIND)));

    let pattern = PullPattern::attributes(vec![PullAttribute::forward(AttributeName::Ident(
        old_attribute,
    ))]);
    assert_eq!(
        recovered
            .pull(&pattern, EntityIdentifier::Ident(old_holder))
            .unwrap(),
        recovered.pull(&pattern, holder).unwrap()
    );
}

#[test]
fn concurrent_expected_basis_writers_cannot_fork() {
    let Some(connection) = connection() else {
        return;
    };
    let migration_barrier = Arc::new(Barrier::new(3));
    let mut migration_handles = Vec::new();
    for _ in 0..2 {
        let connection = connection.clone();
        let barrier = Arc::clone(&migration_barrier);
        migration_handles.push(std::thread::spawn(move || {
            let mut migrator = atomic_core::PostgresMigrator::connect(&connection).unwrap();
            barrier.wait();
            migrator.migrate()
        }));
    }
    migration_barrier.wait();
    for handle in migration_handles {
        handle.join().unwrap().unwrap();
    }

    let database_id = unique("concurrent");
    let mut creator = PostgresStore::connect(&connection).unwrap();
    creator.create_database(&database_id, schema()).unwrap();
    drop(creator);
    let service = common::start_service(&connection, &database_id);

    let barrier = Arc::new(Barrier::new(3));
    let mut handles = Vec::new();
    for writer in 0..2 {
        let client = service.client();
        let barrier = Arc::clone(&barrier);
        handles.push(std::thread::spawn(move || {
            barrier.wait();
            common::try_transact_client(
                &client,
                &format!("writer-{writer}"),
                1,
                &add_item(&format!("item-{writer}"), writer),
                1_000,
            )
        }));
    }
    barrier.wait();
    let results: Vec<_> = handles
        .into_iter()
        .map(|handle| handle.join().unwrap())
        .collect();
    assert_eq!(results.iter().filter(|result| result.is_ok()).count(), 1);
    let loser = results
        .iter()
        .find_map(|result| result.as_ref().err())
        .unwrap();
    assert_eq!(loser.code, "postgres/stale-basis");

    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_eq!(store.recover(&database_id).unwrap().basis_t(), 2);
    service.shutdown();
}

#[test]
fn sql_constraints_immutability_and_corruption_checks_fail_closed() {
    let Some(connection) = connection() else {
        return;
    };
    let database_id = unique("constraints");
    let mut store = migrated_store(&connection);
    store.create_database(&database_id, schema()).unwrap();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let constraint_count: i64 = client
        .query_one(
            "SELECT count(*) FROM pg_constraint \
             WHERE conrelid IN ('atomic_transactions'::regclass, \
                                 'atomic_requests'::regclass, \
                                 'atomic_heads'::regclass)",
            &[],
        )
        .unwrap()
        .get(0);
    assert!(constraint_count >= 15);
    let triggers = client
        .query(
            "SELECT c.relname::text, t.tgname::text, p.proname::text, t.tgenabled::text, \
                    t.tgdeferrable, t.tginitdeferred \
               FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid \
               JOIN pg_proc p ON p.oid=t.tgfoid \
              WHERE NOT t.tgisinternal AND t.tgrelid IN \
               ('atomic_databases'::regclass, 'atomic_transactions'::regclass, \
                'atomic_requests'::regclass, 'atomic_heads'::regclass)",
            &[],
        )
        .unwrap();
    // The original eight guards gained an active-identity check, four
    // terminal-collection barriers, and retired-identity cleanup before the
    // baseline collapse. Require all current protections by name and target,
    // while allowing additional independent guards without a stale count.
    for (table, trigger, function) in [
        (
            "atomic_databases",
            "atomic_databases_immutable",
            "atomic_reject_database_mutation",
        ),
        (
            "atomic_databases",
            "atomic_removed_database_identity",
            "atomic_finish_removed_database_identity",
        ),
        (
            "atomic_databases",
            "atomic_terminal_collection_barrier",
            "atomic_protect_reclaiming_database",
        ),
        (
            "atomic_heads",
            "atomic_heads_active",
            "atomic_require_active_publication",
        ),
        (
            "atomic_heads",
            "atomic_heads_validate_advance",
            "atomic_validate_head_advance",
        ),
        (
            "atomic_heads",
            "atomic_heads_validate_insert",
            "atomic_validate_head_insert",
        ),
        (
            "atomic_heads",
            "atomic_terminal_collection_barrier",
            "atomic_protect_reclaiming_database",
        ),
        (
            "atomic_requests",
            "atomic_requests_immutable",
            "atomic_reject_log_generation_gc_mutation",
        ),
        (
            "atomic_requests",
            "atomic_requests_validate_insert",
            "atomic_validate_request_insert",
        ),
        (
            "atomic_requests",
            "atomic_terminal_collection_barrier",
            "atomic_protect_reclaiming_database",
        ),
        (
            "atomic_transactions",
            "atomic_transactions_immutable",
            "atomic_reject_log_generation_gc_mutation",
        ),
        (
            "atomic_transactions",
            "atomic_transactions_validate_insert",
            "atomic_validate_transaction_insert",
        ),
        (
            "atomic_transactions",
            "atomic_transactions_require_publication",
            "atomic_require_published_transaction",
        ),
        (
            "atomic_transactions",
            "atomic_terminal_collection_barrier",
            "atomic_protect_reclaiming_database",
        ),
    ] {
        let row = triggers
            .iter()
            .find(|row| row.get::<_, String>(0) == table && row.get::<_, String>(1) == trigger)
            .unwrap_or_else(|| panic!("required guard {table}.{trigger} is missing"));
        assert_eq!(row.get::<_, String>(2), function, "{table}.{trigger}");
        assert!(
            matches!(row.get::<_, String>(3).as_str(), "O" | "A"),
            "required guard {table}.{trigger} is disabled for ordinary writes"
        );
        if trigger == "atomic_transactions_require_publication" {
            assert!(row.get::<_, bool>(4) && row.get::<_, bool>(5));
        }
    }
    let mut malformed = client.transaction().unwrap();
    let bytes = [0_u8; 48];
    let hash = [1_u8; 32];
    let error = malformed
        .execute(
            "INSERT INTO atomic_transactions \
             (database_id, basis_t, previous_hash, tx_hash, payload) \
             VALUES ($1, 2, $2, $3, $4)",
            &[&database_id, &&[0_u8; 32][..], &&hash[..], &&bytes[..]],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "40001");
    drop(malformed);

    let service = common::start_service(&connection, &database_id);
    common::transact(&service, "request-1", 1, &add_item("immutable", 1), 1_000);
    service.shutdown();
    let error = client
        .execute(
            "UPDATE atomic_transaction_contents SET payload = payload \
             WHERE content_hash IN (SELECT content_hash FROM atomic_generation_transactions \
             WHERE database_id = $1)",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "55000");

    let role = unique("atomic_runtime");
    let schema_name: String = client
        .query_one("SELECT current_schema()", &[])
        .unwrap()
        .get(0);
    let quoted_schema = format!("\"{}\"", schema_name.replace('"', "\"\""));
    client
        .batch_execute(&format!(
            "CREATE ROLE {role}; \
             GRANT USAGE ON SCHEMA {quoted_schema} TO {role}; \
             GRANT SELECT, INSERT ON atomic_generation_transactions, \
             atomic_generation_requests, atomic_transaction_contents TO {role}; \
             GRANT SELECT, UPDATE ON atomic_heads TO {role}; \
             GRANT SELECT ON atomic_databases TO {role}"
        ))
        .unwrap();
    let mut runtime = client.transaction().unwrap();
    runtime
        .batch_execute(&format!("SET LOCAL ROLE {role}"))
        .unwrap();
    let error = runtime
        .execute(
            "DELETE FROM atomic_generation_transactions WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap_err();
    assert_eq!(error.as_db_error().unwrap().code().code(), "42501");
    drop(runtime);
    client
        .batch_execute(&format!("DROP OWNED BY {role}; DROP ROLE {role}"))
        .unwrap();

    common::with_replica_triggers_disabled(&mut client, |client| {
        let affected = client.execute(
            "UPDATE atomic_transaction_contents SET payload = set_byte(payload, 16, \
             get_byte(payload, 16) # 1) WHERE content_hash IN \
             (SELECT content_hash FROM atomic_generation_transactions \
             WHERE database_id = $1 AND basis_t = 1)",
            &[&database_id],
        )?;
        assert_eq!(affected, 1, "fault injection must corrupt actual content");
        Ok(())
    })
    .unwrap();
    let error = store.recover(&database_id).unwrap_err();
    assert_eq!(error.code, "recovery/content-checksum-mismatch");

    let missing_id = unique("missing");
    store.create_database(&missing_id, schema()).unwrap();
    let missing_service = common::start_service(&connection, &missing_id);
    common::transact(
        &missing_service,
        "request-1",
        1,
        &add_item("missing", 1),
        1_000,
    );
    missing_service.shutdown();
    // This is an intentional disk-corruption witness. The native background
    // index now holds foreign keys to the authoritative endpoint, so disabling
    // only our USER immutability triggers no longer suffices to manufacture a
    // missing transaction. Replica mode bypasses both those triggers and the
    // referential triggers for this one scoped fault injection, leaving the
    // dangling derived publication in place for recovery to distrust.
    common::with_replica_triggers_disabled(&mut client, |client| {
        let requests = client.execute(
            "DELETE FROM atomic_generation_requests WHERE database_id = $1",
            &[&missing_id],
        )?;
        let transactions = client.execute(
            "DELETE FROM atomic_generation_transactions WHERE database_id = $1",
            &[&missing_id],
        )?;
        // Provisioning the nonempty schema is itself the first transaction.
        assert_eq!(requests, 2);
        assert_eq!(
            transactions, 2,
            "fault injection must remove an actual transaction"
        );
        Ok(())
    })
    .unwrap();
    let error = store.recover(&missing_id).unwrap_err();
    assert_eq!(error.code, "recovery/missing-transaction");
}

#[test]
fn acknowledged_commit_survives_postgres_restart() {
    let (Some(connection), Ok(pg_ctl), Ok(data_dir)) = (
        connection(),
        std::env::var("ATOMIC_POSTGRES_CTL"),
        std::env::var("ATOMIC_POSTGRES_DATA"),
    ) else {
        return;
    };
    let database_id = unique("restart");
    let expected = {
        let mut store = migrated_store(&connection);
        store.create_database(&database_id, schema()).unwrap();
        let service = common::start_service(&connection, &database_id);
        let expected =
            common::transact(&service, "request-1", 1, &add_item("durable", 1), 1_000).db_after;
        service.shutdown();
        expected
    };
    let status = Command::new(pg_ctl)
        .args(["-D", &data_dir, "-m", "fast", "-w", "restart"])
        .status()
        .unwrap();
    assert!(status.success());
    let mut store = PostgresStore::connect(&connection).unwrap();
    assert_database_eq(&expected, &store.recover(&database_id).unwrap());
}
