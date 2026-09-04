-- Datomic's storage lifecycle publishes immutable index values before a root
-- and reclaims garbage only after a grace period and after all readers have
-- released the old basis.  Semantic-state v2 nodes need the same lifecycle:
-- a terminal generation loses its root coordinates first, then globally
-- shared content nodes peel away from the unreferenced parent frontier in
-- bounded batches.  No database-local ownership is invented for shared nodes.

CREATE INDEX atomic_semantic_commitment_nodes_left
    ON atomic_semantic_commitment_nodes(left_hash)
    WHERE left_hash IS NOT NULL;

CREATE INDEX atomic_semantic_commitment_nodes_right
    ON atomic_semantic_commitment_nodes(right_hash)
    WHERE right_hash IS NOT NULL;

CREATE OR REPLACE FUNCTION atomic_semantic_commitment_gc_pin_key()
RETURNS BIGINT
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT pg_catalog.hashtextextended(
        'atomic/semantic-commitment-gc/v18',
        4707476001900298240::bigint
    )
$$;

REVOKE ALL ON FUNCTION atomic_semantic_commitment_gc_pin_key() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_semantic_commitment_gc_pin_key() TO CURRENT_USER;

-- Take one shared transaction lock before the first node statement.  Content
-- upload and root insertion then cannot race the global no-reference test.
-- The transaction-local marker avoids one advisory-lock acquisition per node
-- when the Rust path inserts a changed path with separate statements.
CREATE OR REPLACE FUNCTION atomic_admit_semantic_commitment_node_insert()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
BEGIN
    IF pg_catalog.current_setting('atomic.semantic_commitment_write_lock', true)
       IS DISTINCT FROM 'v18' THEN
        PERFORM pg_catalog.pg_advisory_xact_lock_shared(
            atomic_semantic_commitment_gc_pin_key()
        );
        PERFORM set_config('atomic.semantic_commitment_write_lock', 'v18', true);
    END IF;
    RETURN NULL;
END;
$$;

CREATE TRIGGER atomic_semantic_commitment_nodes_admit_insert
BEFORE INSERT ON atomic_semantic_commitment_nodes
FOR EACH STATEMENT EXECUTE FUNCTION atomic_admit_semantic_commitment_node_insert();

-- Preserve migration 15's exact coordinate validation while fencing new roots
-- against a terminal generation claim.  A generation pin is the same lock
-- used by immutable peers, backups, excision builders, and log collection.
CREATE OR REPLACE FUNCTION atomic_validate_semantic_commitment_root()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    authoritative_hash BYTEA;
    authoritative_state BYTEA;
    authoritative_frontier BIGINT;
    stored_count BIGINT;
    generation_pin_key BIGINT;
BEGIN
    IF pg_catalog.current_setting('atomic.semantic_commitment_write_lock', true)
       IS DISTINCT FROM 'v18' THEN
        PERFORM pg_catalog.pg_advisory_xact_lock_shared(
            atomic_semantic_commitment_gc_pin_key()
        );
        PERFORM set_config('atomic.semantic_commitment_write_lock', 'v18', true);
    END IF;
    SELECT atomic_log_generation_pin_key(NEW.database_id, NEW.generation)
      INTO generation_pin_key;
    IF generation_pin_key IS NULL THEN
        RAISE EXCEPTION 'Atomic semantic commitment has no generation pin coordinate'
            USING ERRCODE = '23503';
    END IF;
    PERFORM pg_catalog.pg_advisory_xact_lock_shared(generation_pin_key);
    IF EXISTS (
        SELECT 1 FROM atomic_log_generation_collection_progress
         WHERE database_id = NEW.database_id AND generation = NEW.generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_abandonment_progress
         WHERE database_id = NEW.database_id AND generation = NEW.generation
    ) THEN
        RAISE EXCEPTION 'Atomic semantic commitment generation is being collected'
            USING ERRCODE = '55000';
    END IF;

    IF NEW.basis_t = 0 THEN
        SELECT genesis_hash INTO authoritative_hash
          FROM atomic_databases WHERE database_id = NEW.database_id;
        IF NOT FOUND OR authoritative_hash <> NEW.tx_hash THEN
            RAISE EXCEPTION 'Atomic genesis commitment has invalid identity'
                USING ERRCODE = '23503';
        END IF;
    ELSIF NEW.generation = 0 THEN
        SELECT tx_hash, state_hash
          INTO authoritative_hash, authoritative_state
          FROM atomic_transactions
         WHERE database_id = NEW.database_id AND basis_t = NEW.basis_t;
        IF NOT FOUND OR authoritative_hash <> NEW.tx_hash
                     OR authoritative_state <> NEW.state_hash THEN
            RAISE EXCEPTION 'Atomic semantic commitment does not identify its legacy transaction'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        SELECT tx_hash, state_hash, eidx_frontier
          INTO authoritative_hash, authoritative_state, authoritative_frontier
          FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.generation AND basis_t = NEW.basis_t;
        IF NOT FOUND OR authoritative_hash <> NEW.tx_hash
                     OR authoritative_state <> NEW.state_hash
                     OR authoritative_frontier <> NEW.eidx_frontier THEN
            RAISE EXCEPTION 'Atomic semantic commitment does not identify its generation transaction'
                USING ERRCODE = '23503';
        END IF;
    END IF;

    IF NEW.current_root IS NOT NULL THEN
        SELECT subtree_count INTO stored_count
          FROM atomic_semantic_commitment_nodes
         WHERE node_hash = NEW.current_root;
        IF NOT FOUND OR stored_count <> NEW.current_count THEN
            RAISE EXCEPTION 'Atomic semantic commitment root count is invalid'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

-- Normal UPDATE/DELETE remains impossible.  DELETE is admitted only inside an
-- owner SECURITY DEFINER collector, under its explicit transaction marker,
-- and after the row-local liveness predicate is rechecked by the trigger.
CREATE OR REPLACE FUNCTION atomic_reject_semantic_commitment_mutation()
RETURNS trigger
LANGUAGE plpgsql
SET search_path FROM CURRENT
AS $$
DECLARE
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF TG_OP <> 'DELETE'
       OR current_user <> relation_owner
       OR pg_catalog.current_setting('atomic.semantic_commitment_gc', true)
          IS DISTINCT FROM 'v18' THEN
        RAISE EXCEPTION 'Atomic semantic commitments are immutable'
            USING ERRCODE = '55000';
    END IF;
    IF TG_TABLE_NAME = 'atomic_semantic_commitment_roots' THEN
        IF pg_catalog.current_setting('atomic.log_generation_gc', true)
           IS DISTINCT FROM 'v14'
           OR EXISTS (
                SELECT 1 FROM atomic_heads
                 WHERE database_id = OLD.database_id
                   AND log_generation = OLD.generation
           )
           OR NOT (
                EXISTS (
                    SELECT 1 FROM atomic_log_generation_collection_progress
                     WHERE database_id = OLD.database_id
                       AND generation = OLD.generation
                ) OR EXISTS (
                    SELECT 1 FROM atomic_log_generation_abandonment_progress
                     WHERE database_id = OLD.database_id
                       AND generation = OLD.generation
                )
           ) THEN
            RAISE EXCEPTION 'Atomic semantic root is still live'
                USING ERRCODE = '55000';
        END IF;
    ELSIF TG_TABLE_NAME = 'atomic_semantic_commitment_nodes' THEN
        IF EXISTS (
            SELECT 1 FROM atomic_semantic_commitment_roots
             WHERE current_root = OLD.node_hash
        ) OR EXISTS (
            SELECT 1 FROM atomic_semantic_commitment_nodes
             WHERE left_hash = OLD.node_hash OR right_hash = OLD.node_hash
        ) THEN
            RAISE EXCEPTION 'Atomic semantic commitment node is still referenced'
                USING ERRCODE = '55000';
        END IF;
    ELSE
        RAISE EXCEPTION 'Atomic semantic commitment guard used on an unknown relation'
            USING ERRCODE = '55000';
    END IF;
    RETURN OLD;
END;
$$;

DROP TRIGGER atomic_semantic_commitment_nodes_immutable
    ON atomic_semantic_commitment_nodes;
CREATE TRIGGER atomic_semantic_commitment_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_semantic_commitment_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_semantic_commitment_mutation();

DROP TRIGGER atomic_semantic_commitment_roots_immutable
    ON atomic_semantic_commitment_roots;
CREATE TRIGGER atomic_semantic_commitment_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_semantic_commitment_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_semantic_commitment_mutation();

-- Claim one otherwise-eligible generation using the exact v14 age, dependency,
-- and advisory-lock boundary, then detach only a bounded prefix of coordinates.
-- Existing request-base pins must be gone, so no reconstructable native
-- transaction report is weakened by this operation.
CREATE OR REPLACE FUNCTION atomic_collect_semantic_commitment_generation_roots(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    older_than_millis BIGINT,
    maximum_roots BIGINT,
    abandoned BOOLEAN
)
RETURNS TABLE(basis_t BIGINT, tx_hash BYTEA, current_root BYTEA)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
DECLARE
    retirement_row atomic_log_generation_retirements%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    generation_pin_key BIGINT;
    builder_pin_key BIGINT;
    worker_pin_key BIGINT;
    restore_pin_key BIGINT;
    durable_lineage TEXT;
    durable_genesis_hash BYTEA;
    already_claimed BOOLEAN;
BEGIN
    IF candidate_generation < 0 OR older_than_millis < 0
       OR maximum_roots < 1 OR maximum_roots > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic semantic-root garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(
        atomic_semantic_commitment_gc_pin_key()
    ) THEN
        RAISE EXCEPTION 'Atomic semantic commitment collector is busy'
            USING ERRCODE = '55006';
    END IF;
    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_pin_key;
    IF generation_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_pin_key) THEN
        RAISE EXCEPTION 'Atomic log generation is pinned by a peer or backup'
            USING ERRCODE = '55006';
    END IF;

    IF abandoned THEN
        IF candidate_generation <= 0 THEN
            RAISE EXCEPTION 'Generation zero cannot be an abandoned rewrite'
                USING ERRCODE = '22023';
        END IF;
        SELECT * INTO generation_row
          FROM atomic_log_generations
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
           AND build_kind IN (0, 1, 2)
         FOR UPDATE;
        IF FOUND THEN
            SELECT * INTO build_row
              FROM atomic_log_generation_builds
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             FOR UPDATE;
        END IF;
        IF NOT FOUND OR build_row.generation IS NULL THEN
            RAISE EXCEPTION 'Atomic log generation is not an inactive rewrite build'
                USING ERRCODE = '23503';
        END IF;
        IF EXISTS (
            SELECT 1 FROM atomic_log_generation_activations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
        ) OR EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
        ) THEN
            RAISE EXCEPTION 'Published Atomic log generation cannot be abandoned'
                USING ERRCODE = '55000';
        END IF;
        SELECT EXISTS (
            SELECT 1 FROM atomic_log_generation_abandonment_progress
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
        ) INTO already_claimed;
        SELECT lineage_id, genesis_hash
          INTO durable_lineage, durable_genesis_hash
          FROM atomic_databases WHERE database_id = candidate_database_id;
        IF generation_row.build_kind = 0 AND (
            build_row.source_generation IS NOT NULL
            OR build_row.captured_basis_t <> 0
            OR build_row.captured_head_hash <> durable_genesis_hash
            OR build_row.frozen_plan_hash IS NULL
            OR build_row.restore_manifest_hash IS NOT NULL
            OR build_row.restore_basis_t IS NOT NULL
            OR build_row.restore_head_hash IS NOT NULL
            OR EXISTS (SELECT 1 FROM atomic_heads WHERE database_id = candidate_database_id)
            OR EXISTS (
                SELECT 1 FROM atomic_log_generations other
                 WHERE other.database_id = candidate_database_id
                   AND other.generation <> candidate_generation
            )
        ) THEN
            RAISE EXCEPTION 'Atomic initial restore is not an isolated authenticated headless build'
                USING ERRCODE = '55000';
        ELSIF generation_row.build_kind <> 0
              AND build_row.source_generation IS NULL THEN
            RAISE EXCEPTION 'Atomic rewrite abandonment has no captured source generation'
                USING ERRCODE = '23503';
        END IF;
        IF NOT already_claimed AND build_row.source_generation IS NOT NULL AND EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = build_row.source_generation
        ) THEN
            RAISE EXCEPTION 'Atomic inactive generation remains exactly resumable'
                USING ERRCODE = '55000';
        END IF;
        IF NOT already_claimed AND generation_row.created_at
           + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic inactive generation has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
        SELECT atomic_tree_database_build_pin_key(candidate_database_id)
          INTO builder_pin_key;
        worker_pin_key := hashtextextended(
            'atomic/excision-worker/v1/' || durable_lineage,
            4707476001900298240::bigint
        );
        restore_pin_key := hashtextextended(
            'atomic/restore/' || candidate_database_id,
            0
        );
        IF builder_pin_key IS NULL OR worker_pin_key IS NULL OR restore_pin_key IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(worker_pin_key)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(restore_pin_key) THEN
            RAISE EXCEPTION 'Atomic inactive generation is pinned by a live builder or reader'
                USING ERRCODE = '55006';
        END IF;
    ELSE
        SELECT * INTO retirement_row
          FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
         FOR UPDATE;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic log generation is not retired'
                USING ERRCODE = '23503';
        END IF;
        IF retirement_row.collecting_at IS NULL
           AND retirement_row.retired_at
               + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic log generation has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
        IF EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
        ) THEN
            RAISE EXCEPTION 'Active Atomic log generation cannot be collected'
                USING ERRCODE = '55000';
        END IF;
    END IF;

    IF EXISTS (
        SELECT 1 FROM atomic_tree_build_intents
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_manifests
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_publications
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_retirements
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_builds dependent
         WHERE dependent.database_id = candidate_database_id
           AND dependent.source_generation = candidate_generation
           AND dependent.generation <> candidate_generation
    ) OR EXISTS (
        SELECT 1 FROM atomic_generation_request_bases
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) OR (NOT abandoned AND EXISTS (
        SELECT 1 FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    )) OR (NOT abandoned AND EXISTS (
        SELECT 1 FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND successor_generation = candidate_generation
    )) THEN
        RAISE EXCEPTION 'Atomic log generation still has a root, request, or build dependency'
            USING ERRCODE = '55000';
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    PERFORM set_config('atomic.semantic_commitment_gc', 'v18', true);
    IF abandoned THEN
        PERFORM set_config('atomic.log_generation_activation', 'v14', true);
        INSERT INTO atomic_log_generation_abandonment_progress(database_id, generation)
        VALUES (candidate_database_id, candidate_generation)
        ON CONFLICT DO NOTHING;
    ELSE
        IF retirement_row.collecting_at IS NULL THEN
            UPDATE atomic_log_generation_retirements
               SET collecting_at = clock_timestamp()
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
        END IF;
        INSERT INTO atomic_log_generation_collection_progress(database_id, generation)
        VALUES (candidate_database_id, candidate_generation)
        ON CONFLICT DO NOTHING;
    END IF;

    RETURN QUERY
    WITH victims AS MATERIALIZED (
        SELECT root.ctid
          FROM atomic_semantic_commitment_roots root
         WHERE root.database_id = candidate_database_id
           AND root.generation = candidate_generation
         ORDER BY root.basis_t
         LIMIT maximum_roots
         FOR UPDATE
    ), deleted AS (
        DELETE FROM atomic_semantic_commitment_roots root
         USING victims victim
         WHERE root.ctid = victim.ctid
         RETURNING root.basis_t, root.tx_hash, root.current_root
    )
    SELECT deleted.basis_t, deleted.tx_hash, deleted.current_root
      FROM deleted ORDER BY deleted.basis_t;

    PERFORM set_config('atomic.semantic_commitment_gc', 'off', true);
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    IF abandoned THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
    END IF;
END;
$$;

-- The original generation collectors remain the authoritative phase machines.
-- Wrap them so no direct caller can skip the new root-first dependency.
ALTER FUNCTION atomic_collect_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
    RENAME TO atomic_collect_log_generation_without_semantic_v14;

CREATE OR REPLACE FUNCTION atomic_collect_log_generation(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    older_than_millis BIGINT,
    maximum_rows BIGINT
)
RETURNS TABLE(rows_removed BIGINT, collection_phase SMALLINT, is_complete BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM atomic_semantic_commitment_roots
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic semantic roots must be collected before log rows'
            USING ERRCODE = '55000';
    END IF;
    RETURN QUERY
    SELECT prior.rows_removed, prior.collection_phase, prior.is_complete
      FROM atomic_collect_log_generation_without_semantic_v14(
          candidate_database_id,
          candidate_generation,
          older_than_millis,
          maximum_rows
      ) AS prior;
END;
$$;

ALTER FUNCTION atomic_abandon_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
    RENAME TO atomic_abandon_log_generation_without_semantic_v14;

CREATE OR REPLACE FUNCTION atomic_abandon_log_generation(
    candidate_database_id TEXT,
    candidate_generation BIGINT,
    older_than_millis BIGINT,
    maximum_rows BIGINT
)
RETURNS TABLE(rows_removed BIGINT, abandonment_phase SMALLINT, is_complete BOOLEAN)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM atomic_semantic_commitment_roots
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic semantic roots must be collected before log rows'
            USING ERRCODE = '55000';
    END IF;
    RETURN QUERY
    SELECT prior.rows_removed, prior.abandonment_phase, prior.is_complete
      FROM atomic_abandon_log_generation_without_semantic_v14(
          candidate_database_id,
          candidate_generation,
          older_than_millis,
          maximum_rows
      ) AS prior;
END;
$$;

-- Delete only the currently unreferenced parent frontier. Child FKs make this
-- naturally root-to-leaf across calls, and structural sharing keeps a node
-- alive until every retained root/path has released it.
CREATE OR REPLACE FUNCTION atomic_collect_semantic_commitment_garbage(
    older_than_millis BIGINT,
    maximum_nodes BIGINT
)
RETURNS TABLE(node_hash BYTEA)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path FROM CURRENT
AS $$
BEGIN
    IF older_than_millis < 0 OR maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic semantic-node garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(
        atomic_semantic_commitment_gc_pin_key()
    ) THEN
        RAISE EXCEPTION 'Atomic semantic commitment collector is busy'
            USING ERRCODE = '55006';
    END IF;
    PERFORM set_config('atomic.semantic_commitment_gc', 'v18', true);
    RETURN QUERY
    WITH victims AS MATERIALIZED (
        SELECT node.node_hash
          FROM atomic_semantic_commitment_nodes node
         WHERE node.created_at < clock_timestamp()
               - older_than_millis * interval '1 millisecond'
           AND NOT EXISTS (
                SELECT 1 FROM atomic_semantic_commitment_roots root
                 WHERE root.current_root = node.node_hash
           )
           AND NOT EXISTS (
                SELECT 1 FROM atomic_semantic_commitment_nodes parent
                 WHERE parent.left_hash = node.node_hash
                    OR parent.right_hash = node.node_hash
           )
         ORDER BY node.created_at, node.node_hash
         LIMIT maximum_nodes
         FOR UPDATE SKIP LOCKED
    ), deleted AS (
        DELETE FROM atomic_semantic_commitment_nodes node
         USING victims victim
         WHERE node.node_hash = victim.node_hash
         RETURNING node.node_hash
    )
    SELECT deleted.node_hash FROM deleted ORDER BY deleted.node_hash;
    PERFORM set_config('atomic.semantic_commitment_gc', 'off', true);
END;
$$;

REVOKE ALL ON FUNCTION atomic_collect_semantic_commitment_generation_roots(
    TEXT, BIGINT, BIGINT, BIGINT, BOOLEAN
) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_semantic_commitment_generation_roots(
    TEXT, BIGINT, BIGINT, BIGINT, BOOLEAN
) TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_collect_semantic_commitment_garbage(BIGINT, BIGINT)
FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_semantic_commitment_garbage(BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_collect_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_abandon_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_abandon_log_generation(TEXT, BIGINT, BIGINT, BIGINT)
TO CURRENT_USER;
REVOKE ALL ON FUNCTION atomic_collect_log_generation_without_semantic_v14(
    TEXT, BIGINT, BIGINT, BIGINT
) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_abandon_log_generation_without_semantic_v14(
    TEXT, BIGINT, BIGINT, BIGINT
) FROM PUBLIC;
