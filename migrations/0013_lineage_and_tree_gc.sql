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

-- Program bytes are content-addressed values prepared before ordinary
-- :db/fn information refers to them. Every deployment is therefore a future
-- age-gated candidate, while per-log-generation temporal references are the
-- exact retention authority. v13 deliberately starts incomplete; migration
-- v14 authenticates generations, populates references, then flips the guard.
CREATE TABLE atomic_program_gc_candidates (
    program_hash BYTEA PRIMARY KEY
        REFERENCES atomic_programs(program_hash) ON DELETE CASCADE,
    candidate_at TIMESTAMPTZ NOT NULL
);

INSERT INTO atomic_program_gc_candidates (program_hash, candidate_at)
SELECT program_hash, created_at FROM atomic_programs;

CREATE TABLE atomic_program_generation_refs (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    log_generation BIGINT NOT NULL CHECK (log_generation >= 0),
    program_hash BYTEA NOT NULL REFERENCES atomic_programs(program_hash),
    PRIMARY KEY (database_id, log_generation, program_hash)
);

CREATE INDEX atomic_program_generation_refs_hash
    ON atomic_program_generation_refs (program_hash, database_id, log_generation);

CREATE TABLE atomic_program_reference_state (
    singleton BOOLEAN PRIMARY KEY DEFAULT true CHECK (singleton),
    complete BOOLEAN NOT NULL,
    problem_code TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK ((complete AND problem_code IS NULL)
        OR (NOT complete AND problem_code IS NOT NULL))
);

INSERT INTO atomic_program_reference_state (complete, problem_code)
VALUES (false, 'program/legacy-references-unindexed');

CREATE OR REPLACE FUNCTION atomic_track_program_candidate()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    INSERT INTO atomic_program_gc_candidates (program_hash, candidate_at)
    VALUES (NEW.program_hash, NEW.created_at)
    ON CONFLICT (program_hash) DO NOTHING;
    RETURN NULL;
END;
$$;

CREATE TRIGGER atomic_programs_track_candidate
AFTER INSERT ON atomic_programs
FOR EACH ROW EXECUTE FUNCTION atomic_track_program_candidate();

REVOKE ALL ON FUNCTION atomic_track_program_candidate() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_track_program_candidate() TO CURRENT_USER;

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

-- Claiming a retirement is a separate, durable step because one physical
-- root may have an arbitrarily large changed-path witness.  Once present,
-- this row means no new peer may pin the old publication; existing pins must
-- have disappeared before the collector could take the manifest lock and
-- create it.  The exact retired-node ledger can then be drained in bounded
-- transactions without making the half-drained root observable again.
CREATE TABLE atomic_tree_retirement_progress (
    database_id TEXT NOT NULL,
    publication_revision BIGINT NOT NULL,
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash) = 32),
    started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, publication_revision),
    UNIQUE (manifest_hash),
    FOREIGN KEY (database_id, publication_revision)
        REFERENCES atomic_tree_retirements(database_id, publication_revision)
        ON DELETE CASCADE,
    FOREIGN KEY (manifest_hash)
        REFERENCES atomic_tree_manifests(manifest_hash)
);

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
    expected_node_count BIGINT NOT NULL CHECK (expected_node_count >= 0),
    staged_node_count BIGINT NOT NULL DEFAULT 0 CHECK (
        staged_node_count >= 0 AND staged_node_count <= expected_node_count
    ),
    node_set_hash BYTEA NOT NULL CHECK (octet_length(node_set_hash) = 32),
    -- 0=staging, 1=sealed/ready, 2=published bookkeeping, 3=abandoned
    -- collection in progress.  State 3 is deliberately not reusable: after
    -- a crash, a retry waits for the bounded collector to finish and then
    -- creates the same deterministic intent afresh.
    intent_state SMALLINT NOT NULL DEFAULT 0 CHECK (intent_state BETWEEN 0 AND 3),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    heartbeat_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK (intent_state <> 1 OR staged_node_count = expected_node_count),
    CHECK (intent_state <> 2 OR staged_node_count = expected_node_count)
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
SECURITY DEFINER
SET search_path FROM CURRENT
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
           AND i.intent_state = 1
           AND i.staged_node_count = i.expected_node_count
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

REVOKE ALL ON FUNCTION atomic_validate_tree_publication_delta() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_validate_tree_publication_delta() TO CURRENT_USER;

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
    -- Consuming an upload intent is deliberately O(1).  Its possibly huge
    -- node ledger is ordinary post-publication bookkeeping and is drained by
    -- the bounded collector instead of being cascade-deleted in this root
    -- publication transaction.
    IF mode <> 0 THEN
        UPDATE atomic_tree_build_intents
           SET intent_state = 2, heartbeat_at = clock_timestamp()
         WHERE manifest_hash = NEW.manifest_hash
           AND intent_state = 1
           AND staged_node_count = expected_node_count;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic tree publication could not consume its sealed upload intent'
                USING ERRCODE = '40001';
        END IF;
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER atomic_tree_publications_apply_delta
AFTER INSERT ON atomic_tree_publications
FOR EACH ROW EXECUTE FUNCTION atomic_apply_tree_publication_delta();

REVOKE ALL ON FUNCTION atomic_apply_tree_publication_delta() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_apply_tree_publication_delta() TO CURRENT_USER;

-- v12's trigger functions were created without a fixed search path. Pin them
-- to the installation schema before runtime roles are admitted; otherwise a
-- role retaining PostgreSQL TEMP could shadow an unqualified validation read.
ALTER FUNCTION atomic_validate_tree_manifest_insert() SECURITY DEFINER;
ALTER FUNCTION atomic_validate_tree_manifest_insert() SET search_path FROM CURRENT;
ALTER FUNCTION atomic_validate_tree_publication_insert() SECURITY DEFINER;
ALTER FUNCTION atomic_validate_tree_publication_insert() SET search_path FROM CURRENT;
REVOKE ALL ON FUNCTION atomic_validate_tree_manifest_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_tree_publication_insert() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_validate_tree_manifest_insert() TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_validate_tree_publication_insert() TO CURRENT_USER;

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

CREATE OR REPLACE FUNCTION atomic_heartbeat_tree_build(candidate_manifest_hash BYTEA)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    UPDATE atomic_tree_build_intents
       SET heartbeat_at = clock_timestamp()
     WHERE manifest_hash = candidate_manifest_hash
       AND intent_state IN (0, 1);
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree build intent is absent or no longer reusable'
            USING ERRCODE = '23503';
    END IF;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_finish_tree_build(candidate_manifest_hash BYTEA)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_tree_publications
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic tree build cannot finish before root publication'
            USING ERRCODE = '23503';
    END IF;
    -- Ambiguous publication retries may arrive after the root trigger already
    -- performed this transition, or after bounded bookkeeping cleanup removed
    -- the header entirely.  Both are successful terminal outcomes.
    UPDATE atomic_tree_build_intents
       SET intent_state = 2, heartbeat_at = clock_timestamp()
     WHERE manifest_hash = candidate_manifest_hash
       AND intent_state IN (1, 2);
END;
$$;

REVOKE ALL ON FUNCTION atomic_heartbeat_tree_build(BYTEA) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_finish_tree_build(BYTEA) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_heartbeat_tree_build(BYTEA) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_finish_tree_build(BYTEA) TO CURRENT_USER;

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

DROP TRIGGER atomic_programs_immutable ON atomic_programs;
CREATE TRIGGER atomic_programs_immutable
BEFORE UPDATE OR DELETE ON atomic_programs
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retirements_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_retirements
FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retirement_progress_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_retirement_progress
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
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
    removed_publications BIGINT;
    retirement_time TIMESTAMPTZ;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0
       OR maximum_nodes < 1
       OR maximum_nodes > 4096 THEN
        RETURN FALSE;
    END IF;
    lock_key := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    IF NOT pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_tree_retirement_progress
         WHERE database_id = candidate_database_id
           AND publication_revision = candidate_revision
           AND manifest_hash = candidate_manifest_hash
    ) THEN
        SELECT r.retired_at INTO retirement_time
          FROM atomic_tree_retirements r
          JOIN atomic_tree_publications p
            ON p.database_id = r.database_id
           AND p.publication_revision = r.publication_revision
           AND p.manifest_hash = r.manifest_hash
         WHERE r.database_id = candidate_database_id
           AND r.publication_revision = candidate_revision
           AND r.manifest_hash = candidate_manifest_hash
         FOR UPDATE OF r;
        IF NOT FOUND THEN
            RETURN FALSE;
        END IF;
    ELSE
        SELECT r.retired_at INTO retirement_time
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
         FOR UPDATE OF r;
        IF NOT FOUND THEN
            RETURN FALSE;
        END IF;
        INSERT INTO atomic_tree_retirement_progress
               (database_id, publication_revision, manifest_hash)
        VALUES (candidate_database_id, candidate_revision,
                candidate_manifest_hash);
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    INSERT INTO atomic_tree_garbage_nodes (node_hash, marked_at)
    SELECT batch.node_hash, retirement_time
      FROM (
            SELECT n.node_hash
              FROM atomic_tree_retired_nodes n
             WHERE n.database_id = candidate_database_id
               AND n.publication_revision = candidate_revision
             ORDER BY n.node_hash
             LIMIT maximum_nodes
             FOR UPDATE
           ) batch
    ON CONFLICT (node_hash) DO NOTHING;

    DELETE FROM atomic_tree_retired_nodes n
     USING (
            SELECT selected.node_hash
              FROM atomic_tree_retired_nodes selected
             WHERE selected.database_id = candidate_database_id
               AND selected.publication_revision = candidate_revision
             ORDER BY selected.node_hash
             LIMIT maximum_nodes
           ) batch
     WHERE n.database_id = candidate_database_id
       AND n.publication_revision = candidate_revision
       AND n.node_hash = batch.node_hash;
    IF EXISTS (
        SELECT 1 FROM atomic_tree_retired_nodes
         WHERE database_id = candidate_database_id
           AND publication_revision = candidate_revision
    ) THEN
        PERFORM set_config('atomic.tree_gc_active', 'off', true);
        RETURN TRUE;
    END IF;
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

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT) TO CURRENT_USER;

CREATE OR REPLACE FUNCTION atomic_collect_tree_build_intent(
    candidate_manifest_hash BYTEA,
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    lock_key BIGINT;
    removed_intents BIGINT;
    state SMALLINT;
    last_heartbeat TIMESTAMPTZ;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0
       OR maximum_nodes < 1
       OR maximum_nodes > 4096 THEN
        RETURN FALSE;
    END IF;
    lock_key := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707460391809056768::bigint;
    IF NOT pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    SELECT intent_state, heartbeat_at INTO state, last_heartbeat
      FROM atomic_tree_build_intents
     WHERE manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    IF state IN (0, 1) THEN
        IF EXISTS (
            SELECT 1 FROM atomic_tree_publications
             WHERE manifest_hash = candidate_manifest_hash
        ) THEN
            IF state <> 1 THEN
                RAISE EXCEPTION 'Atomic published tree has an unsealed upload intent'
                    USING ERRCODE = '55000';
            END IF;
            UPDATE atomic_tree_build_intents
               SET intent_state = 2, heartbeat_at = clock_timestamp()
             WHERE manifest_hash = candidate_manifest_hash;
            state := 2;
        ELSE
            IF last_heartbeat >= clock_timestamp()
                                 - older_than_millis * interval '1 millisecond' THEN
                RETURN FALSE;
            END IF;
            UPDATE atomic_tree_build_intents
               SET intent_state = 3
             WHERE manifest_hash = candidate_manifest_hash;
            state := 3;
        END IF;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    IF state = 3 THEN
        INSERT INTO atomic_tree_garbage_nodes (node_hash, marked_at)
        SELECT batch.node_hash, last_heartbeat
          FROM (
                SELECT n.node_hash
                  FROM atomic_tree_build_intent_nodes n
                 WHERE n.manifest_hash = candidate_manifest_hash
                 ORDER BY n.node_hash
                 LIMIT maximum_nodes
                 FOR UPDATE OF n
               ) batch
          JOIN atomic_tree_nodes stored USING (node_hash)
        ON CONFLICT (node_hash) DO NOTHING;
    END IF;
    DELETE FROM atomic_tree_build_intent_nodes n
     USING (
            SELECT selected.node_hash
              FROM atomic_tree_build_intent_nodes selected
             WHERE selected.manifest_hash = candidate_manifest_hash
             ORDER BY selected.node_hash
             LIMIT maximum_nodes
           ) batch
     WHERE n.manifest_hash = candidate_manifest_hash
       AND n.node_hash = batch.node_hash;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_tree_build_intent_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        DELETE FROM atomic_tree_build_intents
         WHERE manifest_hash = candidate_manifest_hash;
        GET DIAGNOSTICS removed_intents = ROW_COUNT;
    ELSE
        removed_intents := 1;
    END IF;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
    RETURN removed_intents = 1;
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_tree_build_intent(BYTEA, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_build_intent(BYTEA, BIGINT, BIGINT) TO CURRENT_USER;

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
                   SELECT 1 FROM atomic_tree_build_intent_nodes i
                    WHERE i.node_hash = g.node_hash
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
               SELECT 1 FROM atomic_tree_build_intent_nodes i
                WHERE i.node_hash = n.node_hash
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

-- Secondary cleanup for pre-ledger and content-first crash orphans. This is
-- deliberately unavailable until the retained publication history proves
-- exact: every current live set is complete and every superseded root has a
-- complete retirement witness. Age alone is never sufficient while that
-- global proof is absent.
CREATE OR REPLACE FUNCTION atomic_collect_tree_orphans(
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS SETOF BYTEA
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF older_than_millis < 0 OR maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic tree orphan collection boundary'
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
                      AND r.garbage_complete
               )
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT n.node_hash
          FROM atomic_tree_nodes n
         WHERE n.created_at < clock_timestamp()
                              - older_than_millis * interval '1 millisecond'
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_live_nodes l
                    WHERE l.node_hash = n.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_retired_nodes r
                    WHERE r.node_hash = n.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_garbage_nodes g
                    WHERE g.node_hash = n.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_delta_nodes d
                    WHERE d.node_hash = n.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_build_intent_nodes i
                    WHERE i.node_hash = n.node_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_manifest_roots r
                    WHERE r.root_hash = n.node_hash
               )
         ORDER BY n.created_at, n.node_hash
         LIMIT maximum_nodes
         FOR UPDATE OF n SKIP LOCKED
    )
    DELETE FROM atomic_tree_nodes n
     USING candidates c
     WHERE n.node_hash = c.node_hash
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_live_nodes l WHERE l.node_hash = n.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_retired_nodes r WHERE r.node_hash = n.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_garbage_nodes g WHERE g.node_hash = n.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_nodes d WHERE d.node_hash = n.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intent_nodes i WHERE i.node_hash = n.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots r WHERE r.root_hash = n.node_hash)
    RETURNING n.node_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_tree_orphans(BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_orphans(BIGINT, BIGINT) TO CURRENT_USER;

CREATE OR REPLACE FUNCTION atomic_collect_program_garbage(
    older_than_millis BIGINT,
    maximum_programs BIGINT
)
RETURNS SETOF BYTEA
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF older_than_millis < 0 OR maximum_programs < 1 OR maximum_programs > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic program garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_program_reference_state
         WHERE singleton AND complete AND problem_code IS NULL
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT c.program_hash
          FROM atomic_program_gc_candidates c
         WHERE c.candidate_at < clock_timestamp()
                                - older_than_millis * interval '1 millisecond'
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_program_generation_refs r
                    WHERE r.program_hash = c.program_hash
               )
           AND NOT EXISTS (
                   SELECT 1 FROM atomic_program_versions v
                    WHERE v.program_hash = c.program_hash
               )
         ORDER BY c.candidate_at, c.program_hash
         LIMIT maximum_programs
         FOR UPDATE OF c SKIP LOCKED
    )
    DELETE FROM atomic_programs p
     USING candidates c
     WHERE p.program_hash = c.program_hash
       AND NOT EXISTS (
               SELECT 1 FROM atomic_program_generation_refs r
                WHERE r.program_hash = p.program_hash
           )
       AND NOT EXISTS (
               SELECT 1 FROM atomic_program_versions v
                WHERE v.program_hash = p.program_hash
           )
    RETURNING p.program_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) TO CURRENT_USER;

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_retirements FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_retired_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_garbage_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_build_intents FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_build_intent_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_live_sets FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_live_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_tree_publication_states FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_program_gc_candidates FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_program_generation_refs FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_program_reference_state FROM PUBLIC;
