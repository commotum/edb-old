-- A transaction basis identifies a logical database value, not a physical
-- index build.  Derived roots therefore carry their own database-scoped,
-- monotonic publication revision.  This permits a repaired/re-encoded tree to
-- succeed at the same basis while preserving every previously published root.
--
-- ATIM v3 did not authenticate such a revision.  Its relational projections
-- are derived and replaceable, so migration discards those manifests and
-- publications rather than assigning them SQL-only identities.  Immutable
-- content-addressed nodes remain available for reuse by the first v4 rebuild.
DROP TABLE IF EXISTS atomic_tree_publications;
DROP TABLE IF EXISTS atomic_tree_manifest_roots;
DROP TABLE IF EXISTS atomic_tree_manifests;

CREATE TABLE atomic_tree_manifests (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    publication_revision BIGINT NOT NULL CHECK (publication_revision > 0),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (octet_length(state_hash) = 32),
    excision_generation BIGINT NOT NULL CHECK (excision_generation >= 0),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    manifest_version SMALLINT NOT NULL CHECK (manifest_version = 4),
    manifest_hash BYTEA PRIMARY KEY CHECK (octet_length(manifest_hash) = 32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    UNIQUE (
        database_id,
        publication_revision,
        basis_t,
        tx_hash,
        manifest_hash
    ),
    FOREIGN KEY (database_id, basis_t, tx_hash)
        REFERENCES atomic_transactions(database_id, basis_t, tx_hash)
);

CREATE INDEX atomic_tree_manifests_generation_basis
    ON atomic_tree_manifests
       (database_id, excision_generation, basis_t DESC, publication_revision DESC);

-- Keeping root references relational as well as in the canonical manifest
-- envelope lets PostgreSQL reject a publication with a missing root without
-- understanding the native node codec.  Readers still compare these rows to
-- the authenticated ATIM envelope before exposing a snapshot.
CREATE TABLE atomic_tree_manifest_roots (
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

CREATE INDEX atomic_tree_manifest_roots_node
    ON atomic_tree_manifest_roots (root_hash);

-- This append-only sequence is the root reference.  The greatest revision is
-- current; no mutable head row is required, and older roots remain available
-- for authenticated fallback and diagnosis.
CREATE TABLE atomic_tree_publications (
    database_id TEXT NOT NULL,
    publication_revision BIGINT NOT NULL CHECK (publication_revision > 0),
    basis_t BIGINT NOT NULL CHECK (basis_t > 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    published_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, publication_revision),
    UNIQUE (manifest_hash),
    FOREIGN KEY (
        database_id,
        publication_revision,
        basis_t,
        tx_hash,
        manifest_hash
    ) REFERENCES atomic_tree_manifests (
        database_id,
        publication_revision,
        basis_t,
        tx_hash,
        manifest_hash
    )
);

CREATE INDEX atomic_tree_publications_latest
    ON atomic_tree_publications (database_id, publication_revision DESC);

CREATE INDEX atomic_tree_publications_basis
    ON atomic_tree_publications
       (database_id, basis_t DESC, publication_revision DESC);

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
    current_revision BIGINT;
    current_basis BIGINT;
    valid_roots BIGINT;
BEGIN
    -- This row is the database-scoped serialization point.  All ordinary and
    -- direct SQL publishers take the same lock before observing the current
    -- append-only root revision.
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = NEW.database_id
       FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication database does not exist'
            USING ERRCODE = '23503';
    END IF;

    SELECT publication_revision, basis_t
      INTO current_revision, current_basis
      FROM atomic_tree_publications
     WHERE database_id = NEW.database_id
     ORDER BY publication_revision DESC
     LIMIT 1;
    IF FOUND THEN
        IF current_revision = 9223372036854775807 THEN
            RAISE EXCEPTION 'Atomic tree publication revision is exhausted'
                USING ERRCODE = '54000';
        END IF;
        IF NEW.publication_revision <> current_revision + 1 THEN
            RAISE EXCEPTION 'Atomic tree publication must advance exactly one revision'
                USING ERRCODE = '40001';
        END IF;
        IF NEW.basis_t < current_basis THEN
            RAISE EXCEPTION 'Atomic tree publication basis cannot regress'
                USING ERRCODE = '40001';
        END IF;
    ELSIF NEW.publication_revision <> 1 THEN
        RAISE EXCEPTION 'First Atomic tree publication must use revision one'
            USING ERRCODE = '40001';
    END IF;

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
           AND m.publication_revision = NEW.publication_revision
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

CREATE TRIGGER atomic_tree_manifests_validate_insert
BEFORE INSERT ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_manifest_insert();

CREATE TRIGGER atomic_tree_manifests_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

CREATE TRIGGER atomic_tree_manifest_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_manifest_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

CREATE TRIGGER atomic_tree_publications_validate_insert
BEFORE INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_publication_insert();

CREATE TRIGGER atomic_tree_publications_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifests FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_manifest_roots FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_publications FROM PUBLIC;
