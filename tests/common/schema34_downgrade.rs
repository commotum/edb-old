//! Explicit rollback of lifecycle-only artifacts in stopped, disposable
//! historical fixtures. Never use this to downgrade an application catalog.

use postgres::{GenericClient, Transaction};

const REPLACED_FUNCTIONS: [(&str, &str); 13] = [
    (
        "atomic_reject_database_mutation",
        include_str!("../../migrations/0014_log_generations.sql"),
    ),
    (
        "atomic_reject_immutable_mutation",
        include_str!("../../migrations/0001_atomic.sql"),
    ),
    (
        "atomic_reject_log_generation_gc_mutation",
        include_str!("../../migrations/0014_log_generations.sql"),
    ),
    (
        "atomic_reject_generation_staging_mutation",
        include_str!("../../migrations/0014_log_generations.sql"),
    ),
    (
        "atomic_reject_tree_gc_mutation",
        include_str!("../../migrations/0013_lineage_and_tree_gc.sql"),
    ),
    (
        "atomic_reject_semantic_commitment_mutation",
        include_str!("../../migrations/0018_semantic_commitment_gc.sql"),
    ),
    (
        "atomic_reject_generation_request_base_mutation",
        include_str!("../../migrations/0017_request_snapshot_bases.sql"),
    ),
    (
        "atomic_reject_request_base_archive_mutation",
        include_str!("../../migrations/0025_receipt_archive_conversion.sql"),
    ),
    (
        "atomic_reject_fulltext_mutation",
        include_str!("../../migrations/0027_fulltext_sidecars.sql"),
    ),
    (
        "atomic_reject_tree_node_block_mutation",
        include_str!("../../migrations/0026_optional_compressed_nodes.sql"),
    ),
    (
        "atomic_mark_fulltext_garbage",
        include_str!("../../migrations/0027_fulltext_sidecars.sql"),
    ),
    (
        "atomic_track_fulltext_page_reference",
        include_str!("../../migrations/0030_shared_fulltext_pages.sql"),
    ),
    (
        "atomic_retire_fulltext_build",
        include_str!("../../migrations/0030_shared_fulltext_pages.sql"),
    ),
];

fn historical_guard(sql: &str, name: &str) -> String {
    let start = sql
        .find(&format!("CREATE OR REPLACE FUNCTION {name}("))
        .or_else(|| sql.find(&format!("CREATE FUNCTION {name}(")))
        .expect("exact historical guard definition");
    let end = start + sql[start..].find("\n$$;").expect("guard terminator") + 4;
    sql[start..end].replacen("CREATE FUNCTION", "CREATE OR REPLACE FUNCTION", 1)
}

fn assert_legacy_names(client: &mut impl GenericClient) {
    let mismatches: i64 = client
        .query_one(
            "SELECT count(*) FROM atomic_database_identities i \
         FULL JOIN atomic_databases d USING(database_id) \
         FULL JOIN atomic_database_names n USING(database_id) \
         WHERE i.database_id IS NULL OR d.database_id IS NULL OR n.name IS NULL \
            OR i.lineage_id<>d.lineage_id OR n.name<>i.database_id \
            OR i.retired_at IS NOT NULL OR i.reclaimed_at IS NOT NULL",
            &[],
        )
        .unwrap()
        .get(0);
    assert_eq!(
        mismatches, 0,
        "historical fixture must not erase renamed, retired, reclaimed or reused identities"
    );
    for table in [
        "atomic_database_reclamation_progress",
        "atomic_database_reclamation_objects",
    ] {
        assert_eq!(
            client
                .query_one(&format!("SELECT count(*) FROM {table}"), &[])
                .unwrap()
                .get::<_, i64>(0),
            0,
            "historical fixture must not discard lifecycle work"
        );
    }
}

pub fn remove_migration_34(transaction: &mut Transaction<'_>) {
    let mut version: i64 = transaction
        .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
        .unwrap()
        .get(0);
    if version == 35 {
        // SQL35 adds only two wrappers. Remove any exact new runtime grants
        // provisioned through those wrappers before erasing their ACLs; the
        // previous writer/peer policy and all canonical data stay untouched.
        transaction.batch_execute(
            "DO $$ DECLARE runtime_role TEXT; BEGIN \
             FOR runtime_role IN SELECT DISTINCT r.rolname FROM pg_proc p \
                 CROSS JOIN LATERAL aclexplode(p.proacl) a \
                 JOIN pg_roles r ON r.oid=a.grantee \
                 WHERE p.oid='atomic_runtime_excision_step(text,bigint,text,bigint,text,bigint,bigint,bytea,bytea,bytea)'::regprocedure \
                   AND a.grantee<>p.proowner LOOP \
                 EXECUTE format('REVOKE SELECT ON atomic_log_generation_builds,atomic_generation_excision_predicates,atomic_log_generation_completion_stages FROM %I',runtime_role); \
                 EXECUTE format('REVOKE INSERT ON atomic_log_generations,atomic_log_generation_builds,atomic_generation_excision_predicates,atomic_log_generation_checkpoints FROM %I',runtime_role); \
             END LOOP; END $$; \
             DROP FUNCTION atomic_runtime_excision_step(TEXT,BIGINT,TEXT,BIGINT,TEXT,BIGINT,BIGINT,BYTEA,BYTEA,BYTEA); \
             DROP FUNCTION atomic_assert_excision_worker(TEXT,TEXT,BIGINT); \
             DELETE FROM atomic_schema_migrations WHERE version=35;"
        ).unwrap();
        version = 34;
    }
    if version < 34 {
        return;
    }
    assert_eq!(
        version, 34,
        "newer fixture artifacts need an explicit rollback"
    );
    assert_legacy_names(transaction);

    // Restore the ten immutable guards and three fulltext side-effect bodies
    // before dropping the helper they currently invoke. No trigger disabling
    // or permissive replacement. Request-base release is unchanged by SQL34.
    for (name, sql) in REPLACED_FUNCTIONS {
        transaction
            .batch_execute(&historical_guard(sql, name))
            .unwrap();
        let body: String = transaction
            .query_one(
                "SELECT prosrc FROM pg_proc WHERE oid=$1::text::regprocedure",
                &[&format!("{name}()")],
            )
            .unwrap()
            .get(0);
        assert!(!body.contains("atomic_database_reclamation_authorized"));
    }
    transaction
        .batch_execute(
            "DO $$ DECLARE relation REGCLASS; BEGIN \
           FOR relation IN SELECT t.tgrelid::regclass FROM pg_trigger t \
            WHERE t.tgfoid='atomic_protect_reclaiming_database()'::regprocedure \
              AND t.tgname='atomic_terminal_collection_barrier' LOOP \
              EXECUTE format('DROP TRIGGER atomic_terminal_collection_barrier ON %s',relation); \
           END LOOP; END $$; \
         DROP TRIGGER atomic_heads_active ON atomic_heads; \
         DROP TRIGGER atomic_tree_publications_active ON atomic_tree_publications; \
         DROP TRIGGER atomic_index_publications_active ON atomic_index_publications; \
         DROP TRIGGER atomic_fulltext_projections_active ON atomic_fulltext_projections; \
         DROP TRIGGER atomic_removed_database_identity ON atomic_databases; \
         DROP FUNCTION atomic_require_active_publication(), \
             atomic_prepare_database_reclamation(TEXT,TEXT,BIGINT,BOOLEAN), \
             atomic_finish_removed_database_identity(), \
             atomic_protect_reclaiming_database(), \
             atomic_database_reclamation_authorized(); \
         DROP TABLE atomic_database_reclamation_objects, \
             atomic_database_reclamation_progress, atomic_database_names; \
         DROP TABLE atomic_database_identities; \
         DELETE FROM atomic_schema_migrations WHERE version=34;",
        )
        .unwrap();
    assert!(
        transaction
            .query_one(
                "SELECT to_regclass('atomic_database_identities') IS NULL \
            AND to_regprocedure('atomic_database_reclamation_authorized()') IS NULL",
                &[]
            )
            .unwrap()
            .get::<_, bool>(0)
    );
}

pub fn assert_restored(client: &mut impl GenericClient) {
    assert_legacy_names(client);
    assert_eq!(
        client
            .query_one("SELECT max(version) FROM atomic_schema_migrations", &[])
            .unwrap()
            .get::<_, i64>(0),
        35
    );
    for (name, _) in REPLACED_FUNCTIONS {
        let body: String = client
            .query_one(
                "SELECT prosrc FROM pg_proc WHERE oid=$1::text::regprocedure",
                &[&format!("{name}()")],
            )
            .unwrap()
            .get(0);
        assert_eq!(
            body.matches("atomic_database_reclamation_authorized()")
                .count(),
            1,
            "normal migration must install the lifecycle guard exactly once"
        );
    }
    assert!(
        client
            .query_one(
                "SELECT EXISTS(SELECT 1 FROM pg_trigger \
            WHERE tgfoid='atomic_require_active_publication()'::regprocedure) \
            AND EXISTS(SELECT 1 FROM pg_trigger \
            WHERE tgfoid='atomic_protect_reclaiming_database()'::regprocedure)",
                &[]
            )
            .unwrap()
            .get::<_, bool>(0)
    );
    assert!(client.query_one(
        "SELECT to_regprocedure('atomic_assert_excision_worker(text,text,bigint)') IS NOT NULL \
         AND to_regprocedure('atomic_runtime_excision_step(text,bigint,text,bigint,text,bigint,bigint,bytea,bytea,bytea)') IS NOT NULL", &[]
    ).unwrap().get::<_,bool>(0));
}
