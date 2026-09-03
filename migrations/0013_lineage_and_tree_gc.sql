-- A database name is an operator-facing alias, not durable identity. Backups
-- and restores bind to this immutable random lineage so deleting/recreating a
-- name cannot silently combine unrelated histories.
ALTER TABLE atomic_databases
    ADD COLUMN lineage_id TEXT;

UPDATE atomic_databases
   SET lineage_id = gen_random_uuid()::text
 WHERE lineage_id IS NULL;

ALTER TABLE atomic_databases
    ALTER COLUMN lineage_id SET DEFAULT gen_random_uuid()::text,
    ALTER COLUMN lineage_id SET NOT NULL;

ALTER TABLE atomic_databases
    ADD CONSTRAINT atomic_databases_lineage_id_format
        CHECK (lineage_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'),
    ADD CONSTRAINT atomic_databases_lineage_id_key UNIQUE (lineage_id);

-- Exact immutable reachability closure for each physical tree manifest.
-- Rust authenticates and traverses every node before inserting these rows;
-- PostgreSQL then keeps candidates referenced until every retaining manifest
-- has gone away. GC never has to read live node payloads.
CREATE TABLE atomic_tree_manifest_nodes (
    manifest_hash BYTEA NOT NULL
        REFERENCES atomic_tree_manifests(manifest_hash),
    node_hash BYTEA NOT NULL
        REFERENCES atomic_tree_nodes(node_hash),
    PRIMARY KEY (manifest_hash, node_hash)
);

CREATE INDEX atomic_tree_manifest_nodes_node
    ON atomic_tree_manifest_nodes (node_hash);

-- A status row distinguishes an authenticated empty closure from a legacy or
-- corrupt graph that could not be traversed during upgrade. GC refuses shared
-- deletion while any retained publication is incomplete, without making
-- unrelated authoritative databases unavailable.
CREATE TABLE atomic_tree_manifest_closures (
    manifest_hash BYTEA PRIMARY KEY
        REFERENCES atomic_tree_manifests(manifest_hash),
    complete BOOLEAN NOT NULL,
    node_count BIGINT NOT NULL CHECK (node_count >= 0),
    problem_code TEXT,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK ((complete AND problem_code IS NULL)
        OR (NOT complete AND problem_code IS NOT NULL))
);

-- A root becomes garbage when a successor publication wins, not when the old
-- root was originally created. This is the PostgreSQL form of Datomic's exact
-- post-CAS garbage mark.
CREATE TABLE atomic_tree_retirements (
    database_id TEXT NOT NULL,
    publication_revision BIGINT NOT NULL CHECK (publication_revision > 0),
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    retired_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (database_id, publication_revision),
    UNIQUE (manifest_hash),
    FOREIGN KEY (database_id, publication_revision)
        REFERENCES atomic_tree_publications(database_id, publication_revision),
    FOREIGN KEY (manifest_hash)
        REFERENCES atomic_tree_manifests(manifest_hash)
);

CREATE INDEX atomic_tree_retirements_age
    ON atomic_tree_retirements (retired_at, database_id, publication_revision);

CREATE OR REPLACE FUNCTION atomic_validate_tree_closure_publication()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    declared_count BIGINT;
    actual_count BIGINT;
    covered_roots BIGINT;
BEGIN
    SELECT node_count INTO declared_count
      FROM atomic_tree_manifest_closures
     WHERE manifest_hash = NEW.manifest_hash
       AND complete;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication requires a complete authenticated node closure'
            USING ERRCODE = '23503';
    END IF;
    SELECT count(*) INTO actual_count
      FROM atomic_tree_manifest_nodes
     WHERE manifest_hash = NEW.manifest_hash;
    SELECT count(*) INTO covered_roots
      FROM atomic_tree_manifest_roots r
      JOIN atomic_tree_manifest_nodes n
        ON n.manifest_hash = r.manifest_hash
       AND n.node_hash = r.root_hash
     WHERE r.manifest_hash = NEW.manifest_hash;
    IF actual_count <> declared_count OR covered_roots <> 8 THEN
        RAISE EXCEPTION 'Atomic tree publication node closure is incomplete'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_tree_publications_validate_closure
BEFORE INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_closure_publication();

-- Backfill the exact historical transition time for existing append-only
-- publications. The Rust migration step backfills and authenticates closure
-- rows in the same schema transaction before recording version 13.
INSERT INTO atomic_tree_retirements
       (database_id, publication_revision, manifest_hash, retired_at)
SELECT database_id, publication_revision, manifest_hash, successor_at
  FROM (
        SELECT database_id, publication_revision, manifest_hash,
               lead(published_at) OVER (
                   PARTITION BY database_id ORDER BY publication_revision
               ) AS successor_at
          FROM atomic_tree_publications
       ) publications
 WHERE successor_at IS NOT NULL;

CREATE OR REPLACE FUNCTION atomic_mark_tree_retirement()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    INSERT INTO atomic_tree_retirements
           (database_id, publication_revision, manifest_hash, retired_at)
    SELECT database_id, publication_revision, manifest_hash, clock_timestamp()
      FROM atomic_tree_publications
     WHERE database_id = NEW.database_id
       AND publication_revision = NEW.publication_revision - 1;
    RETURN NULL;
END;
$$;

CREATE TRIGGER atomic_tree_publications_mark_retirement
AFTER INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_mark_tree_retirement();

REVOKE ALL ON FUNCTION atomic_mark_tree_retirement() FROM PUBLIC;

-- Tree values remain immutable to every ordinary statement.  The only delete
-- escape hatch is entered by the schema owner's fixed-path collector below.
-- Checking current_user as well as a transaction-local marker means a caller
-- cannot manufacture the marker and bypass these guards directly.
CREATE OR REPLACE FUNCTION atomic_reject_tree_gc_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND pg_catalog.current_setting('atomic.tree_gc_active', true) = 'v13'
       AND current_user = relation_owner THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic committed tree records are immutable'
        USING ERRCODE = '55000';
END;
$$;

DROP TRIGGER atomic_tree_nodes_immutable ON atomic_tree_nodes;
CREATE TRIGGER atomic_tree_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

DROP TRIGGER atomic_tree_manifests_immutable ON atomic_tree_manifests;
CREATE TRIGGER atomic_tree_manifests_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

DROP TRIGGER atomic_tree_manifest_roots_immutable ON atomic_tree_manifest_roots;
CREATE TRIGGER atomic_tree_manifest_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifest_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

DROP TRIGGER atomic_tree_publications_immutable ON atomic_tree_publications;
CREATE TRIGGER atomic_tree_publications_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_manifest_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifest_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_manifest_closures_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifest_closures
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retirements_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

-- Delete one exact post-publication garbage mark.  The function rechecks the
-- age and successor facts, and takes the same advisory coordinate held shared
-- by live peers.  A hash collision is conservative: it can only retain data.
CREATE OR REPLACE FUNCTION atomic_collect_tree_manifest(
    candidate_database_id TEXT,
    candidate_revision BIGINT,
    candidate_manifest_hash BYTEA,
    older_than_millis BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
    deleted_rows BIGINT;
    candidate_nodes BYTEA[];
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32 OR older_than_millis < 0 THEN
        RETURN FALSE;
    END IF;
    lock_key := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    IF NOT pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_tree_retirements r
          JOIN atomic_tree_publications p
            ON p.database_id = r.database_id
           AND p.publication_revision = r.publication_revision
           AND p.manifest_hash = r.manifest_hash
         WHERE r.database_id = candidate_database_id
           AND r.publication_revision = candidate_revision
           AND r.manifest_hash = candidate_manifest_hash
           AND r.retired_at < clock_timestamp()
                              - older_than_millis * interval '1 millisecond'
           AND EXISTS (
               SELECT 1 FROM atomic_tree_publications newer
                WHERE newer.database_id = r.database_id
                  AND newer.publication_revision > r.publication_revision
           )
    ) THEN
        RETURN FALSE;
    END IF;

    SELECT COALESCE(array_agg(node_hash), ARRAY[]::BYTEA[])
      INTO candidate_nodes
      FROM atomic_tree_manifest_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    DELETE FROM atomic_tree_retirements
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision
       AND manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS deleted_rows = ROW_COUNT;
    IF deleted_rows <> 1 THEN
        RAISE EXCEPTION 'Atomic tree retirement changed during collection'
            USING ERRCODE = '40001';
    END IF;
    DELETE FROM atomic_tree_publications
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision
       AND manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS deleted_rows = ROW_COUNT;
    IF deleted_rows <> 1 THEN
        RAISE EXCEPTION 'Atomic tree publication changed during collection'
            USING ERRCODE = '40001';
    END IF;
    DELETE FROM atomic_tree_manifest_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifest_closures
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifest_roots
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifests
     WHERE manifest_hash = candidate_manifest_hash
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_publications
            WHERE manifest_hash = candidate_manifest_hash
       );
    GET DIAGNOSTICS deleted_rows = ROW_COUNT;
    DELETE FROM atomic_tree_nodes n
     WHERE n.node_hash = ANY(candidate_nodes)
       AND n.created_at < clock_timestamp()
                          - older_than_millis * interval '1 millisecond'
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_manifest_nodes r
            WHERE r.node_hash = n.node_hash
       );
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
    RETURN deleted_rows = 1;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_collect_unpublished_tree_manifest(
    candidate_manifest_hash BYTEA,
    older_than_millis BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
    deleted_rows BIGINT;
    candidate_nodes BYTEA[];
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32 OR older_than_millis < 0 THEN
        RETURN FALSE;
    END IF;
    lock_key := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    IF NOT pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_tree_manifests m
         WHERE m.manifest_hash = candidate_manifest_hash
           AND m.created_at < clock_timestamp()
                              - older_than_millis * interval '1 millisecond'
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_publications p
                WHERE p.manifest_hash = m.manifest_hash
           )
    ) THEN
        RETURN FALSE;
    END IF;

    SELECT COALESCE(array_agg(node_hash), ARRAY[]::BYTEA[])
      INTO candidate_nodes
      FROM atomic_tree_manifest_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    DELETE FROM atomic_tree_manifest_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifest_closures
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifest_roots
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifests
     WHERE manifest_hash = candidate_manifest_hash
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_publications
            WHERE manifest_hash = candidate_manifest_hash
    );
    GET DIAGNOSTICS deleted_rows = ROW_COUNT;
    DELETE FROM atomic_tree_nodes n
     WHERE n.node_hash = ANY(candidate_nodes)
       AND n.created_at < clock_timestamp()
                          - older_than_millis * interval '1 millisecond'
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_manifest_nodes r
            WHERE r.node_hash = n.node_hash
       );
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
    RETURN deleted_rows = 1;
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_tree_manifest(TEXT, BIGINT, BYTEA, BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_unpublished_tree_manifest(BYTEA, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_manifest(TEXT, BIGINT, BYTEA, BIGINT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_collect_unpublished_tree_manifest(BYTEA, BIGINT) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifest_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifest_closures FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_retirements FROM PUBLIC;
