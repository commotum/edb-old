//! Explicit index/search administration coexists with the ordinary writer.
mod common;
use atomic_core::storage::root::DatabaseRoot;
use atomic_core::storage::{BlockDatabase, IndexDescriptor, PgBlockStore};
use atomic_core::*;
use std::process::{Command, Output};
use std::time::Duration;

fn command(connection: &str, arguments: &[&str]) -> Output {
    Command::new(env!("CARGO_BIN_EXE_atomic"))
        .env("ATOMIC_POSTGRES_URL", connection)
        .env("ATOMIC_POSTGRES_TRANSPORT", "plaintext")
        .env_remove("ATOMIC_SSD_CACHE_DIR")
        .args(arguments)
        .output()
        .unwrap()
}
fn succeeded(connection: &str, arguments: &[&str]) -> String {
    let output = command(connection, arguments);
    assert!(
        output.status.success(),
        "{arguments:?}: {}\n{}",
        String::from_utf8_lossy(&output.stdout),
        String::from_utf8_lossy(&output.stderr)
    );
    String::from_utf8(output.stdout).unwrap()
}
fn root(store: &mut PgBlockStore, key: &str) -> DatabaseRoot {
    let id: Digest = store
        .read_ref(key)
        .unwrap()
        .unwrap()
        .value
        .unwrap()
        .try_into()
        .unwrap();
    DatabaseRoot::decode(&id, &store.get(id).unwrap().unwrap()).unwrap()
}
fn request(key: &str, entity: EntityRef, text: &str) -> TransactionRequest {
    TransactionRequest::new(
        key,
        vec![TxOp::Add {
            entity,
            attribute: 1000,
            value: Value::String(text.into()).into(),
        }],
    )
}

#[test]
fn manual_index_and_guarded_search_rebuild_preserve_active_writer_and_exact_receipts() {
    let Ok(url) = std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIP block index CLI: ATOMIC_POSTGRES_URL unset");
        return;
    };
    let fixture = common::PostgresFixture::new(&url, "block_index_cli");
    let config = PostgresConnectionConfig::plaintext(&fixture.connection);
    PgBlockStore::install(&config).unwrap();
    let mut schema = Schema::new();
    schema
        .install(
            Attribute::new(
                1000,
                Keyword::new("item", "text"),
                ValueType::String,
                Cardinality::One,
            )
            .fulltext(),
        )
        .unwrap();
    let database = BlockDatabase::create(&config, "documents", schema).unwrap();
    let service = TransactionService::start_with_indexing(
        TransactionServiceConfig {
            connection: fixture.connection.clone(),
            database_id: "documents".into(),
            holder_id: "manual-index-witness".into(),
            lease_duration: Duration::from_secs(120),
            renew_interval: Duration::from_secs(30),
            queue_capacity: 8,
            capacity_limits: CapacityLimits::default(),
        },
        BackgroundIndexingConfig {
            memory_index_threshold_bytes: 1 << 30,
            memory_index_max_bytes: 2 << 30,
        },
    )
    .unwrap();
    let client = service.client();
    let transaction = request(
        "first",
        EntityRef::Temp("document".into()),
        "violet original",
    );
    let first = client
        .transact(transaction.clone(), Duration::from_secs(10))
        .unwrap();
    let entity = first.tempids["document"];
    let mut store = PgBlockStore::connect(&config).unwrap();
    let key = database.reference_key();
    let before = root(&mut store, &key);
    let output = succeeded(
        &fixture.connection,
        &["consolidate", "--database", "documents"],
    );
    assert!(output.contains(&format!("INDEXED basis_t={}", first.basis_t)));
    let indexed = root(&mut store, &key);
    assert_eq!(
        (
            indexed.identity,
            indexed.basis,
            indexed.writer_epoch,
            indexed.log,
            indexed.receipts,
            indexed.metadata
        ),
        (
            before.identity,
            before.basis,
            before.writer_epoch,
            before.log,
            before.receipts,
            before.metadata
        )
    );
    let old_index = indexed.indexes.unwrap();
    let descriptor =
        IndexDescriptor::decode(&old_index, &store.get(old_index).unwrap().unwrap()).unwrap();
    assert_eq!(descriptor.basis, first.basis_t);
    let old_search = descriptor.fulltext.unwrap();
    let held = Peer::connect(&fixture.connection, "documents", 0)
        .unwrap()
        .db();

    let wrong = "00".repeat(32);
    let rejected = command(
        &fixture.connection,
        &[
            "fulltext-rebuild",
            "--database",
            "documents",
            "--discard-manifest",
            &wrong,
            "--apply",
        ],
    );
    assert!(!rejected.status.success());
    assert!(String::from_utf8_lossy(&rejected.stderr).contains("cli/repair-target-mismatch"));
    assert_eq!(root(&mut store, &key), indexed);
    let selected = old_index
        .iter()
        .map(|byte| format!("{byte:02x}"))
        .collect::<String>();
    let output = succeeded(
        &fixture.connection,
        &[
            "fulltext-rebuild",
            "--database",
            "documents",
            "--discard-manifest",
            &selected,
            "--apply",
            "--batches",
            "1",
        ],
    );
    assert!(output.contains("FULLTEXT_REBUILT"));
    assert!(output.contains("old_objects_deleted=false"));
    assert!(store.get(old_index).unwrap().is_some());
    assert!(store.get(old_search).unwrap().is_some());
    let rebuilt = root(&mut store, &key);
    assert_eq!(
        (
            rebuilt.basis,
            rebuilt.writer_epoch,
            rebuilt.log,
            rebuilt.receipts,
            rebuilt.metadata
        ),
        (
            indexed.basis,
            indexed.writer_epoch,
            indexed.log,
            indexed.receipts,
            indexed.metadata
        )
    );
    assert_eq!(
        held.fulltext(1000, "violet", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    let replay = client
        .transact(transaction, Duration::from_secs(10))
        .unwrap();
    assert!(replay.replayed);
    assert_eq!(replay.tx_hash, first.tx_hash);
    let second = client
        .transact(
            request("second", EntityRef::Id(entity), "violet successor"),
            Duration::from_secs(10),
        )
        .unwrap();
    assert_eq!(second.basis_t, first.basis_t + 1);
    succeeded(
        &fixture.connection,
        &["consolidate", "--database", "documents"],
    );
    assert_eq!(
        first.db_after.values(entity, 1000).unwrap(),
        vec![Value::String("violet original".into())]
    );
    assert_eq!(
        Peer::connect(&fixture.connection, "documents", 0)
            .unwrap()
            .db()
            .fulltext(1000, "successor", &FulltextOptions::default())
            .unwrap()
            .hits
            .len(),
        1
    );
    assert!(client.is_available());
    assert_eq!(client.background_indexing_stats().jobs_failed, 0);
    service.shutdown();
}
