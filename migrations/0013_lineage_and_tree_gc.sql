-- Security-definer functions created below must resolve only this
-- installation's owner-controlled schema, pg_catalog, and pg_temp last.  An
-- application role may retain TEMP without being able to shadow a relation.
SELECT set_config(
    'search_path',
    format('%I, pg_catalog, pg_temp', current_schema()),
    true
);

-- Catalog names are locators.  Durable backup/restore identity is random and
-- survives a rename without allowing delete/recreate alias collisions.
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

-- One live membership, not one closure per revision.  The initial native
-- root seeds it once; subsequent copy-on-write publications update only the
-- exact changed-path delta emitted by the merge.
CREATE TABLE atomic_tree_live_sets (
    database_id TEXT PRIMARY KEY REFERENCES atomic_databases(database_id),
    manifest_hash BYTEA NOT NULL REFERENCES atomic_tree_manifests(manifest_hash),
    complete BOOLEAN NOT NULL,
    problem_code TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK ((complete AND problem_code IS NULL)
        OR (NOT complete AND problem_code IS NOT NULL))
);

CREATE TABLE atomic_tree_live_nodes (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    node_hash BYTEA NOT NULL REFERENCES atomic_tree_nodes(node_hash),
    PRIMARY KEY (database_id, node_hash)
);

CREATE INDEX atomic_tree_live_nodes_hash
    ON atomic_tree_live_nodes (node_hash, database_id);

-- A predecessor becomes collectible only after its successor root wins.  The
-- optional node rows are the exact old-minus-new changed-path garbage set.
-- Legacy/full-repair transitions have garbage_complete=false and therefore
-- retire metadata without guessing any raw value.
CREATE TABLE atomic_tree_retirements (
    database_id TEXT NOT NULL,
    publication_revision BIGINT NOT NULL CHECK (publication_revision > 0),
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    retired_at TIMESTAMPTZ NOT NULL,
    garbage_complete BOOLEAN NOT NULL,
    PRIMARY KEY (database_id, publication_revision),
    UNIQUE (manifest_hash),
    FOREIGN KEY (database_id, publication_revision)
        REFERENCES atomic_tree_publications(database_id, publication_revision),
    FOREIGN KEY (manifest_hash)
        REFERENCES atomic_tree_manifests(manifest_hash)
);

CREATE INDEX atomic_tree_retirements_age
    ON atomic_tree_retirements (retired_at, database_id, publication_revision);

CREATE TABLE atomic_tree_retired_nodes (
    database_id TEXT NOT NULL,
    publication_revision BIGINT NOT NULL,
    node_hash BYTEA NOT NULL REFERENCES atomic_tree_nodes(node_hash),
    PRIMARY KEY (database_id, publication_revision, node_hash),
    FOREIGN KEY (database_id, publication_revision)
        REFERENCES atomic_tree_retirements(database_id, publication_revision)
);

CREATE INDEX atomic_tree_retired_nodes_hash
    ON atomic_tree_retired_nodes (node_hash, database_id, publication_revision);

-- Exact marks survive root-metadata retirement until the immutable value is
-- absent from every current membership and every other retirement ledger.
-- A later publication that reintroduces a hash removes its stale mark before
-- that publication can become visible, restarting both pin and age safety.
CREATE TABLE atomic_tree_garbage_nodes (
    node_hash BYTEA PRIMARY KEY
        REFERENCES atomic_tree_nodes(node_hash) ON DELETE CASCADE,
    marked_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX atomic_tree_garbage_nodes_age
    ON atomic_tree_garbage_nodes (marked_at, node_hash);

-- Content is uploaded before its manifest/root transaction. The builder
-- records the exact O(delta) upload set first and holds a session advisory
-- lock derived from manifest_hash. A crashed/losing build therefore becomes
-- an age-gated exact garbage source without storing one full closure per
-- published revision.
CREATE TABLE atomic_tree_build_intents (
    manifest_hash BYTEA PRIMARY KEY CHECK (octet_length(manifest_hash) = 32),
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    expected_revision BIGINT NOT NULL CHECK (expected_revision >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    heartbeat_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX atomic_tree_build_intents_age
    ON atomic_tree_build_intents (heartbeat_at, manifest_hash);

CREATE TABLE atomic_tree_build_intent_nodes (
    manifest_hash BYTEA NOT NULL REFERENCES atomic_tree_build_intents(manifest_hash)
        ON DELETE CASCADE,
    node_hash BYTEA NOT NULL CHECK (octet_length(node_hash) = 32),
    PRIMARY KEY (manifest_hash, node_hash)
);

CREATE INDEX atomic_tree_build_intent_nodes_hash
    ON atomic_tree_build_intent_nodes (node_hash, manifest_hash);

-- Transaction-local staging lets the ordinary Rust publisher write an O(delta)
-- set before the root.  The root trigger consumes it atomically, so no added
-- membership can appear after a retired membership is removed.
CREATE TABLE atomic_tree_delta_headers (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash),
    predecessor_manifest_hash BYTEA REFERENCES atomic_tree_manifests(manifest_hash)
        ON DELETE CASCADE,
    delta_mode SMALLINT NOT NULL CHECK (delta_mode BETWEEN 0 AND 2),
    -- 0 unknown/conservative, 1 complete replacement, 2 exact incremental
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE atomic_tree_delta_nodes (
    manifest_hash BYTEA NOT NULL REFERENCES atomic_tree_delta_headers(manifest_hash)
        ON DELETE CASCADE,
    node_hash BYTEA NOT NULL REFERENCES atomic_tree_nodes(node_hash),
    direction SMALLINT NOT NULL CHECK (direction IN (-1, 1)),
    PRIMARY KEY (manifest_hash, node_hash)
);

CREATE INDEX atomic_tree_delta_nodes_hash
    ON atomic_tree_delta_nodes (node_hash, manifest_hash);

-- O(1) publication provenance makes ambiguous retries auditable without
-- retaining the transient added-node half of every delta.
CREATE TABLE atomic_tree_publication_states (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash),
    predecessor_manifest_hash BYTEA,
    delta_mode SMALLINT NOT NULL CHECK (delta_mode BETWEEN 0 AND 2)
);

CREATE OR REPLACE FUNCTION atomic_validate_tree_publication_delta()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    mode SMALLINT;
    predecessor BYTEA;
    current_hash BYTEA;
    current_complete BOOLEAN;
    invalid_nodes BIGINT;
    covered_roots BIGINT;
BEGIN
    SELECT delta_mode, predecessor_manifest_hash
      INTO mode, predecessor
      FROM atomic_tree_delta_headers
     WHERE manifest_hash = NEW.manifest_hash;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication requires one explicit delta header'
            USING ERRCODE = '23503';
    END IF;

    SELECT p.manifest_hash, COALESCE(l.complete, false)
      INTO current_hash, current_complete
      FROM atomic_tree_publications p
      LEFT JOIN atomic_tree_live_sets l
        ON l.database_id = p.database_id AND l.manifest_hash = p.manifest_hash
     WHERE p.database_id = NEW.database_id
     ORDER BY p.publication_revision DESC
     LIMIT 1;

    IF FOUND THEN
        IF predecessor IS DISTINCT FROM current_hash THEN
            RAISE EXCEPTION 'Atomic tree delta names the wrong predecessor root'
                USING ERRCODE = '40001';
        END IF;
    ELSIF predecessor IS NOT NULL THEN
        RAISE EXCEPTION 'First Atomic tree delta cannot name a predecessor'
            USING ERRCODE = '40001';
    END IF;

    IF mode = 0 THEN
        IF EXISTS (
            SELECT 1 FROM atomic_tree_delta_nodes
             WHERE manifest_hash = NEW.manifest_hash
        ) THEN
            RAISE EXCEPTION 'Unknown Atomic tree delta cannot claim node changes'
                USING ERRCODE = '23514';
        END IF;
        RETURN NEW;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM atomic_tree_build_intents i
         WHERE i.manifest_hash = NEW.manifest_hash
           AND i.database_id = NEW.database_id
           AND i.expected_revision = NEW.publication_revision - 1
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_delta_nodes d
         WHERE d.manifest_hash = NEW.manifest_hash
           AND d.direction = 1
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_build_intent_nodes i
                WHERE i.manifest_hash = d.manifest_hash
                  AND i.node_hash = d.node_hash
           )
    ) THEN
        RAISE EXCEPTION 'Atomic tree publication lacks its pre-upload intent'
            USING ERRCODE = '23514';
    END IF;

    IF mode = 1 THEN
        SELECT count(*) INTO covered_roots
          FROM atomic_tree_manifest_roots r
          JOIN atomic_tree_delta_nodes d
            ON d.manifest_hash = r.manifest_hash
           AND d.node_hash = r.root_hash
           AND d.direction = 1
         WHERE r.manifest_hash = NEW.manifest_hash;
        IF covered_roots <> 8 OR EXISTS (
            SELECT 1 FROM atomic_tree_delta_nodes
             WHERE manifest_hash = NEW.manifest_hash AND direction = -1
        ) THEN
            RAISE EXCEPTION 'Replacement Atomic tree delta must add all roots and retire none'
                USING ERRCODE = '23514';
        END IF;
        RETURN NEW;
    END IF;

    IF predecessor IS NULL OR NOT current_complete THEN
        RAISE EXCEPTION 'Incremental Atomic tree delta requires a complete predecessor membership'
            USING ERRCODE = '23514';
    END IF;
    SELECT count(*) INTO invalid_nodes
      FROM atomic_tree_delta_nodes d
      LEFT JOIN atomic_tree_live_nodes l
        ON l.database_id = NEW.database_id AND l.node_hash = d.node_hash
     WHERE d.manifest_hash = NEW.manifest_hash
       AND d.direction = -1
       AND l.node_hash IS NULL;
    IF invalid_nodes <> 0 THEN
        RAISE EXCEPTION 'Incremental Atomic tree delta retires a non-live node'
            USING ERRCODE = '23514';
    END IF;
    SELECT count(*) INTO covered_roots
      FROM atomic_tree_manifest_roots r
     WHERE r.manifest_hash = NEW.manifest_hash
       AND (
           EXISTS (
               SELECT 1 FROM atomic_tree_delta_nodes d
                WHERE d.manifest_hash = NEW.manifest_hash
                  AND d.node_hash = r.root_hash AND d.direction = 1
           )
           OR (
               EXISTS (
                   SELECT 1 FROM atomic_tree_live_nodes l
                    WHERE l.database_id = NEW.database_id
                      AND l.node_hash = r.root_hash
               )
               AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_delta_nodes d
                    WHERE d.manifest_hash = NEW.manifest_hash
                      AND d.node_hash = r.root_hash AND d.direction = -1
               )
           )
       );
    IF covered_roots <> 8 THEN
        RAISE EXCEPTION 'Incremental Atomic tree delta does not cover every successor root'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TRIGGER atomic_tree_publications_validate_delta
BEFORE INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_publication_delta();

CREATE OR REPLACE FUNCTION atomic_apply_tree_publication_delta()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    mode SMALLINT;
    predecessor_revision BIGINT;
    predecessor_hash BYTEA;
    predecessor_complete BOOLEAN;
BEGIN
    SELECT delta_mode, predecessor_manifest_hash
      INTO STRICT mode, predecessor_hash
      FROM atomic_tree_delta_headers
     WHERE manifest_hash = NEW.manifest_hash;

    SELECT p.publication_revision, COALESCE(l.complete, false)
      INTO predecessor_revision, predecessor_complete
      FROM atomic_tree_publications p
      LEFT JOIN atomic_tree_live_sets l
        ON l.database_id = p.database_id AND l.manifest_hash = p.manifest_hash
     WHERE p.database_id = NEW.database_id
       AND p.publication_revision = NEW.publication_revision - 1
       AND p.manifest_hash = predecessor_hash;

    -- Added/reintroduced content becomes protected before any old membership
    -- is removed. PostgreSQL FK row locking then closes the content-first/GC
    -- race across databases.
    INSERT INTO atomic_tree_live_nodes (database_id, node_hash)
    SELECT NEW.database_id, node_hash
      FROM atomic_tree_delta_nodes
     WHERE manifest_hash = NEW.manifest_hash AND direction = 1
    ON CONFLICT DO NOTHING;

    -- A globally pending mark may predate a later reintroduction by this or a
    -- different database. Remove it while the new live membership already
    -- protects the value. A future exact retirement will establish a fresh
    -- grace boundary.
    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    DELETE FROM atomic_tree_garbage_nodes g
     USING atomic_tree_delta_nodes d
     WHERE d.manifest_hash = NEW.manifest_hash
       AND d.direction = 1
       AND g.node_hash = d.node_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);

    IF mode = 1 THEN
        DELETE FROM atomic_tree_live_nodes l
         WHERE l.database_id = NEW.database_id
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_delta_nodes d
                WHERE d.manifest_hash = NEW.manifest_hash
                  AND d.direction = 1 AND d.node_hash = l.node_hash
           );
    ELSIF mode = 2 THEN
        DELETE FROM atomic_tree_live_nodes l
         WHERE l.database_id = NEW.database_id
           AND EXISTS (
               SELECT 1 FROM atomic_tree_delta_nodes d
                WHERE d.manifest_hash = NEW.manifest_hash
                  AND d.direction = -1 AND d.node_hash = l.node_hash
           );
    END IF;

    IF predecessor_revision IS NOT NULL THEN
        INSERT INTO atomic_tree_retirements
               (database_id, publication_revision, manifest_hash, retired_at,
                garbage_complete)
        VALUES (NEW.database_id, predecessor_revision, predecessor_hash,
                clock_timestamp(), mode = 2 AND predecessor_complete);
        IF mode = 2 AND predecessor_complete THEN
            INSERT INTO atomic_tree_retired_nodes
                   (database_id, publication_revision, node_hash)
            SELECT NEW.database_id, predecessor_revision, node_hash
              FROM atomic_tree_delta_nodes
             WHERE manifest_hash = NEW.manifest_hash AND direction = -1;
        END IF;
    END IF;

    INSERT INTO atomic_tree_publication_states
           (manifest_hash, predecessor_manifest_hash, delta_mode)
    VALUES (NEW.manifest_hash, predecessor_hash, mode);

    INSERT INTO atomic_tree_live_sets
           (database_id, manifest_hash, complete, problem_code, updated_at)
    VALUES (NEW.database_id, NEW.manifest_hash, mode <> 0,
            CASE WHEN mode = 0 THEN 'tree/live-membership-unknown' ELSE NULL END,
            clock_timestamp())
    ON CONFLICT (database_id) DO UPDATE
        SET manifest_hash = EXCLUDED.manifest_hash,
            complete = EXCLUDED.complete,
            problem_code = EXCLUDED.problem_code,
            updated_at = EXCLUDED.updated_at;

    DELETE FROM atomic_tree_delta_nodes WHERE manifest_hash = NEW.manifest_hash;
    DELETE FROM atomic_tree_delta_headers WHERE manifest_hash = NEW.manifest_hash;
    RETURN NULL;
END;
$$;

CREATE TRIGGER atomic_tree_publications_apply_delta
AFTER INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_apply_tree_publication_delta();

REVOKE ALL ON FUNCTION atomic_apply_tree_publication_delta() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_apply_tree_publication_delta() TO CURRENT_USER;

-- Existing v12 roots have no exact merge garbage witness. They may age out as
-- metadata, but no node hash is ever inferred from them. Rust authenticates
-- only each database's newest root once to seed current live membership.
INSERT INTO atomic_tree_retirements
       (database_id, publication_revision, manifest_hash, retired_at,
        garbage_complete)
SELECT database_id, publication_revision, manifest_hash, successor_at, false
  FROM (
        SELECT database_id, publication_revision, manifest_hash,
               lead(published_at) OVER (
                   PARTITION BY database_id ORDER BY publication_revision
               ) AS successor_at
          FROM atomic_tree_publications
       ) publications
 WHERE successor_at IS NOT NULL;

INSERT INTO atomic_tree_publication_states
       (manifest_hash, predecessor_manifest_hash, delta_mode)
SELECT manifest_hash, predecessor_hash, 0
  FROM (
        SELECT manifest_hash,
               lag(manifest_hash) OVER (
                   PARTITION BY database_id ORDER BY publication_revision
               ) AS predecessor_hash
          FROM atomic_tree_publications
       ) publications;

-- Runtime writers cannot choose published_at: only this owner function can
-- install the final root row, and its default clock value drives retirement.
CREATE OR REPLACE FUNCTION atomic_publish_tree(
    candidate_database_id TEXT,
    candidate_revision BIGINT,
    candidate_basis BIGINT,
    candidate_tx_hash BYTEA,
    candidate_manifest_hash BYTEA
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    INSERT INTO atomic_tree_publications
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash)
    VALUES (candidate_database_id, candidate_revision, candidate_basis,
            candidate_tx_hash, candidate_manifest_hash);
END;
$$;

REVOKE ALL ON FUNCTION atomic_publish_tree(TEXT, BIGINT, BIGINT, BYTEA, BYTEA) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_publish_tree(TEXT, BIGINT, BIGINT, BYTEA, BYTEA) TO CURRENT_USER;

-- Ordinary statements remain immutable.  Only the fixed-path collector below
-- runs as the relation owner with this transaction-local marker.
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

CREATE TRIGGER atomic_tree_retirements_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retired_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_retired_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_publication_states_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_publication_states
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_garbage_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_garbage_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

-- One call consumes only the oldest retained publication of a database. Exact
-- post-CAS marks move to a persistent relational ledger before root metadata
-- disappears. Raw values are drained separately in a bounded batch.
CREATE OR REPLACE FUNCTION atomic_collect_tree_retirement(
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
    removed_publications BIGINT;
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
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_publications older
                WHERE older.database_id = r.database_id
                  AND older.publication_revision < r.publication_revision
           )
    ) THEN
        RETURN FALSE;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    INSERT INTO atomic_tree_garbage_nodes (node_hash, marked_at)
    SELECT n.node_hash, r.retired_at
      FROM atomic_tree_retired_nodes n
      JOIN atomic_tree_retirements r
        ON r.database_id = n.database_id
       AND r.publication_revision = n.publication_revision
     WHERE n.database_id = candidate_database_id
       AND n.publication_revision = candidate_revision
    ON CONFLICT (node_hash) DO NOTHING;

    DELETE FROM atomic_tree_retired_nodes
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision;
    DELETE FROM atomic_tree_retirements
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision
       AND manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_publications
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision
       AND manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS removed_publications = ROW_COUNT;
    IF removed_publications <> 1 THEN
        RAISE EXCEPTION 'Atomic tree retirement candidate changed during collection'
            USING ERRCODE = '40001';
    END IF;
    DELETE FROM atomic_tree_manifest_roots
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_publication_states
     WHERE manifest_hash = candidate_manifest_hash;
    DELETE FROM atomic_tree_manifests
     WHERE manifest_hash = candidate_manifest_hash
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_publications p
            WHERE p.manifest_hash = candidate_manifest_hash
       );
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
    RETURN removed_publications = 1;
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT) TO CURRENT_USER;

-- Drain only hashes carrying an exact post-publication mark. A corrupt or
-- legacy transition blocks raw-value deletion until its root metadata ages
-- out, but does not prevent that metadata from retiring. Selection and delete
-- are both bounded and deterministic; concurrent reuse either wins a live FK
-- first or makes the publisher retry before its root becomes visible.
CREATE OR REPLACE FUNCTION atomic_collect_tree_garbage(
    maximum_nodes BIGINT
)
RETURNS SETOF BYTEA
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Atomic tree garbage batch must be between 1 and 4096'
            USING ERRCODE = '22023';
    END IF;
    IF EXISTS (
        SELECT 1
          FROM (
                SELECT DISTINCT ON (database_id)
                       database_id, manifest_hash
                  FROM atomic_tree_publications
                 ORDER BY database_id, publication_revision DESC
               ) current_root
          LEFT JOIN atomic_tree_live_sets l
            ON l.database_id = current_root.database_id
         WHERE l.database_id IS NULL
            OR l.manifest_hash <> current_root.manifest_hash
            OR NOT l.complete
    ) OR EXISTS (
        SELECT 1
          FROM atomic_tree_manifest_roots r
          JOIN atomic_tree_live_sets l ON l.manifest_hash = r.manifest_hash
         WHERE l.complete
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_live_nodes n
                WHERE n.database_id = l.database_id
                  AND n.node_hash = r.root_hash
           )
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_retirements WHERE NOT garbage_complete
    ) OR EXISTS (
        SELECT 1
          FROM atomic_tree_publications publication
         WHERE EXISTS (
                   SELECT 1 FROM atomic_tree_publications newer
                    WHERE newer.database_id = publication.database_id
                      AND newer.publication_revision > publication.publication_revision
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_retirements r
                    WHERE r.database_id = publication.database_id
                      AND r.publication_revision = publication.publication_revision
                      AND r.manifest_hash = publication.manifest_hash
               )
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT g.node_hash
          FROM atomic_tree_garbage_nodes g
         WHERE NOT EXISTS (
                   SELECT 1 FROM atomic_tree_live_nodes l
                    WHERE l.node_hash = g.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_retired_nodes r
                    WHERE r.node_hash = g.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_delta_nodes d
                    WHERE d.node_hash = g.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_manifest_roots r
                    WHERE r.root_hash = g.node_hash
               )
         ORDER BY g.node_hash
         LIMIT maximum_nodes
         FOR UPDATE OF g SKIP LOCKED
    )
    DELETE FROM atomic_tree_nodes n
     USING candidates c
     WHERE n.node_hash = c.node_hash
       AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_live_nodes l
                WHERE l.node_hash = n.node_hash
           )
       AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_retired_nodes r
                WHERE r.node_hash = n.node_hash
           )
       AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_delta_nodes d
                WHERE d.node_hash = n.node_hash
           )
       AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_manifest_roots r
                WHERE r.root_hash = n.node_hash
           )
    RETURNING n.node_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_tree_garbage(BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_garbage(BIGINT) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_retirements FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_retired_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_garbage_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_live_sets FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_live_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_publication_states FROM PUBLIC;
