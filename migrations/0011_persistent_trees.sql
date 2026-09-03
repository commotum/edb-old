-- Goal 13 replaces the flat segment manifest with a shallow immutable tree.
-- Tree content is written first and can be safely orphaned by an interrupted
-- builder.  The small publication row is written last and is the only thing
-- that makes a manifest eligible for peer adoption.
CREATE TABLE IF NOT EXISTS atomic_tree_nodes (
    node_hash BYTEA PRIMARY KEY CHECK (octet_length(node_hash) = 32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS atomic_tree_nodes_created_at
    ON atomic_tree_nodes (created_at);

CREATE TABLE IF NOT EXISTS atomic_tree_manifests (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (octet_length(state_hash) = 32),
    excision_generation BIGINT NOT NULL CHECK (excision_generation >= 0),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    manifest_version SMALLINT NOT NULL CHECK (manifest_version = 3),
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, basis_t),
    UNIQUE (manifest_hash),
    UNIQUE (database_id, basis_t, tx_hash, manifest_hash),
    FOREIGN KEY (database_id, basis_t, tx_hash)
        REFERENCES atomic_transactions(database_id, basis_t, tx_hash)
);

CREATE INDEX IF NOT EXISTS atomic_tree_manifests_generation_basis
    ON atomic_tree_manifests
       (database_id, excision_generation, basis_t DESC);

-- Keeping root references relational as well as in the canonical manifest
-- envelope lets PostgreSQL reject a publication with a missing root without
-- understanding the native node codec.  The reader checks that these rows
-- exactly agree with the canonical envelope before exposing a snapshot.
CREATE TABLE IF NOT EXISTS atomic_tree_manifest_roots (
    manifest_hash BYTEA NOT NULL
        REFERENCES atomic_tree_manifests(manifest_hash),
    index_order SMALLINT NOT NULL CHECK (index_order BETWEEN 0 AND 3),
    history BOOLEAN NOT NULL,
    root_hash BYTEA NOT NULL
        REFERENCES atomic_tree_nodes(node_hash),
    datom_count BIGINT NOT NULL CHECK (datom_count >= 0),
    encoded_bytes BIGINT NOT NULL CHECK (encoded_bytes > 0),
    PRIMARY KEY (manifest_hash, index_order, history)
);

CREATE INDEX IF NOT EXISTS atomic_tree_manifest_roots_node
    ON atomic_tree_manifest_roots (root_hash);

CREATE TABLE IF NOT EXISTS atomic_tree_publications (
    database_id TEXT NOT NULL,
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    published_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, basis_t),
    UNIQUE (manifest_hash),
    FOREIGN KEY (database_id, basis_t, tx_hash, manifest_hash)
        REFERENCES atomic_tree_manifests
                   (database_id, basis_t, tx_hash, manifest_hash)
);

CREATE INDEX IF NOT EXISTS atomic_tree_publications_latest
    ON atomic_tree_publications (database_id, basis_t DESC);

CREATE OR REPLACE FUNCTION atomic_validate_tree_manifest_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    authoritative_state BYTEA;
    current_generation BIGINT;
BEGIN
    SELECT state_hash INTO authoritative_state
      FROM atomic_transactions
     WHERE database_id = NEW.database_id
       AND basis_t = NEW.basis_t
       AND tx_hash = NEW.tx_hash;
    IF NOT FOUND OR authoritative_state <> NEW.state_hash
                 OR NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'Atomic tree manifest does not identify one committed state'
            USING ERRCODE = '23503';
    END IF;

    SELECT excision_generation INTO current_generation
      FROM atomic_database_generations
     WHERE database_id = NEW.database_id;
    IF NOT FOUND OR current_generation <> NEW.excision_generation THEN
        RAISE EXCEPTION 'Atomic tree manifest uses a stale database generation'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_tree_publication_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    valid_roots BIGINT;
BEGIN
    -- Four index orders times current/history.  The root table's constrained
    -- key space and primary key mean eight rows also means all eight pairs.
    SELECT count(*) INTO valid_roots
      FROM atomic_tree_manifest_roots r
      JOIN atomic_tree_nodes n ON n.node_hash = r.root_hash
     WHERE r.manifest_hash = NEW.manifest_hash
       AND r.encoded_bytes = octet_length(n.payload);
    IF valid_roots <> 8 THEN
        RAISE EXCEPTION 'Atomic tree publication requires eight present roots'
            USING ERRCODE = '23503';
    END IF;

    IF NOT EXISTS (
        SELECT 1
          FROM atomic_tree_manifests m
          JOIN atomic_transactions t
            ON t.database_id = m.database_id
           AND t.basis_t = m.basis_t
           AND t.tx_hash = m.tx_hash
           AND t.state_hash = m.state_hash
          JOIN atomic_database_generations g
            ON g.database_id = m.database_id
           AND g.excision_generation = m.excision_generation
         WHERE m.database_id = NEW.database_id
           AND m.basis_t = NEW.basis_t
           AND m.tx_hash = NEW.tx_hash
           AND m.manifest_hash = NEW.manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic tree publication is stale or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS atomic_tree_nodes_immutable ON atomic_tree_nodes;
CREATE TRIGGER atomic_tree_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_tree_manifests_validate_insert
    ON atomic_tree_manifests;
CREATE TRIGGER atomic_tree_manifests_validate_insert
BEFORE INSERT ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_manifest_insert();

DROP TRIGGER IF EXISTS atomic_tree_manifests_immutable
    ON atomic_tree_manifests;
CREATE TRIGGER atomic_tree_manifests_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_tree_manifest_roots_immutable
    ON atomic_tree_manifest_roots;
CREATE TRIGGER atomic_tree_manifest_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifest_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_tree_publications_validate_insert
    ON atomic_tree_publications;
CREATE TRIGGER atomic_tree_publications_validate_insert
BEFORE INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_publication_insert();

DROP TRIGGER IF EXISTS atomic_tree_publications_immutable
    ON atomic_tree_publications;
CREATE TRIGGER atomic_tree_publications_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifest_roots FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_publications FROM PUBLIC;
