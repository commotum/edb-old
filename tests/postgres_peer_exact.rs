use atomic_core::{
    Attribute, Cardinality, Keyword, Peer, PostgresIndexer, PostgresMigrator, PostgresStore,
    Schema, ValueType,
};
use postgres::{Client, NoTls};
use std::time::{SystemTime, UNIX_EPOCH};

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

#[test]
fn corrupt_sole_native_publication_makes_public_peer_fail_closed() {
    let Some(connection) = connection() else {
        return;
    };
    PostgresMigrator::connect(&connection)
        .unwrap()
        .migrate()
        .unwrap();

    let database_id = unique("peer_corrupt_sole_native");
    let mut schema = Schema::new();
    schema
        .install(Attribute::new(
            1_000,
            Keyword::new("item", "name"),
            ValueType::String,
            Cardinality::One,
        ))
        .unwrap();
    PostgresStore::connect(&connection)
        .unwrap()
        .create_database(&database_id, schema)
        .unwrap();
    let publication = PostgresIndexer::connect(&connection, &database_id)
        .unwrap()
        .consolidate()
        .unwrap();

    let mut client = Client::connect(&connection, NoTls).unwrap();
    let publication_count: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_tree_publications WHERE database_id = $1",
            &[&database_id],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        publication_count, 1,
        "fixture must have one native authority"
    );

    // DDL is transactional in PostgreSQL: a panic before commit restores the
    // immutable-data trigger instead of contaminating later tests.
    let mut fault = client.transaction().unwrap();
    fault
        .batch_execute("ALTER TABLE atomic_tree_manifests DISABLE TRIGGER USER")
        .unwrap();
    assert_eq!(
        fault
            .execute(
                "UPDATE atomic_tree_manifests \
                    SET payload = set_byte(payload, 16, get_byte(payload, 16) # 1) \
                  WHERE manifest_hash = $1",
                &[&&publication.manifest_hash[..]],
            )
            .unwrap(),
        1
    );
    fault
        .batch_execute("ALTER TABLE atomic_tree_manifests ENABLE TRIGGER USER")
        .unwrap();
    fault.commit().unwrap();

    let error = Peer::connect(&connection, &database_id, 32)
        .err()
        .expect("a corrupt native authority must not fall back to the log");
    assert_eq!(error.code, "peer/all-native-publications-invalid");
}
