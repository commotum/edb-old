-- Atomic current-schema baseline (version 36).
-- Fresh installations only: this declares the final schema directly, without
-- replaying historical upgrades or inserting historical migration receipts.
-- The installer owns the selected schema and records this baseline's checksum.
-- Historical suffixes on some routines remain because current guards call them.

-- Capture the installation namespace in every routine, with temporary objects
-- searched last. SQL routine bodies may refer to tables declared later below.
SELECT pg_catalog.set_config(
    'search_path',
    pg_catalog.quote_ident(pg_catalog.current_schema()) || ', pg_catalog, pg_temp',
    true
);
SET LOCAL check_function_bodies = false;

CREATE FUNCTION atomic_abandon_log_generation(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) RETURNS TABLE(rows_removed bigint, abandonment_phase smallint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_abandon_log_generation_without_semantic_v14(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) RETURNS TABLE(rows_removed bigint, abandonment_phase smallint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    progress_phase SMALLINT;
    builder_pin_key BIGINT;
    worker_pin_key BIGINT;
    generation_pin_key BIGINT;
    restore_pin_key BIGINT;
    durable_lineage TEXT;
    durable_genesis_hash BYTEA;
    removed BIGINT := 0;
    ignored BIGINT;
    content_candidate RECORD;
    already_claimed BOOLEAN;
BEGIN
    IF candidate_generation <= 0 OR older_than_millis < 0
       OR maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic inactive-generation garbage boundary'
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
    IF NOT FOUND THEN
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
    SELECT lineage_id, genesis_hash INTO durable_lineage, durable_genesis_hash
      FROM atomic_databases WHERE database_id = candidate_database_id;
    IF generation_row.build_kind = 0 AND (
        build_row.source_generation IS NOT NULL
        OR build_row.captured_basis_t <> 0
        OR build_row.captured_head_hash <> durable_genesis_hash
        OR build_row.frozen_plan_hash IS NULL
        OR build_row.restore_manifest_hash IS NOT NULL
        OR build_row.restore_basis_t IS NOT NULL
        OR build_row.restore_head_hash IS NOT NULL
        OR EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
        )
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
    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_pin_key;
    restore_pin_key := hashtextextended(
        'atomic/restore/' || candidate_database_id,
        0
    );
    IF builder_pin_key IS NULL OR worker_pin_key IS NULL OR generation_pin_key IS NULL
       OR restore_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(worker_pin_key)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_pin_key)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(restore_pin_key) THEN
        RAISE EXCEPTION 'Atomic inactive generation is pinned by a live builder or reader'
            USING ERRCODE = '55006';
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
    ) THEN
        RAISE EXCEPTION 'Atomic inactive generation still has a derived root or build dependency'
            USING ERRCODE = '55000';
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    INSERT INTO atomic_log_generation_abandonment_progress(database_id, generation)
    VALUES (candidate_database_id, candidate_generation)
    ON CONFLICT DO NOTHING;
    SELECT phase INTO progress_phase
      FROM atomic_log_generation_abandonment_progress
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;

    IF progress_phase = 0 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_generation_request_tempids
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY request_key_hash, tempid_name LIMIT maximum_rows
        )
        DELETE FROM atomic_generation_request_tempids t
         USING victims v WHERE t.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 1 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_generation_requests
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY basis_t LIMIT maximum_rows
        )
        DELETE FROM atomic_generation_requests r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 2 THEN
        WITH victims AS MATERIALIZED (
            SELECT ctid, content_hash FROM atomic_generation_transactions
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY basis_t LIMIT maximum_rows
        ), marked AS (
            INSERT INTO atomic_log_generation_garbage_contents
                   (database_id, generation, content_hash)
            SELECT candidate_database_id, candidate_generation, content_hash
              FROM victims ON CONFLICT DO NOTHING RETURNING 1
        ), deleted AS (
            DELETE FROM atomic_generation_transactions t
             USING victims v WHERE t.ctid = v.ctid RETURNING 1
        )
        SELECT count(*), (SELECT count(*) FROM marked)
          INTO removed, ignored FROM deleted;
    ELSIF progress_phase = 3 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_program_generation_refs
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
             ORDER BY program_hash LIMIT maximum_rows
        )
        DELETE FROM atomic_program_generation_refs r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 4 THEN
        FOR content_candidate IN
            SELECT content_hash FROM atomic_log_generation_garbage_contents
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY content_hash LIMIT maximum_rows
             FOR UPDATE SKIP LOCKED
        LOOP
            DELETE FROM atomic_log_generation_garbage_contents
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
               AND content_hash = content_candidate.content_hash;
            IF NOT EXISTS (
                SELECT 1 FROM atomic_generation_transactions
                 WHERE content_hash = content_candidate.content_hash
            ) AND NOT EXISTS (
                SELECT 1 FROM atomic_log_generation_garbage_contents
                 WHERE content_hash = content_candidate.content_hash
            ) THEN
                DELETE FROM atomic_transaction_contents
                 WHERE content_hash = content_candidate.content_hash;
            END IF;
            removed := removed + 1;
        END LOOP;
    ELSIF progress_phase = 5 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_completed_excision_requests
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY request_t, request_entity LIMIT maximum_rows
        )
        DELETE FROM atomic_completed_excision_requests r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 6 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_generation_excision_predicates
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY request_t, request_entity LIMIT maximum_rows
        )
        DELETE FROM atomic_generation_excision_predicates p
         USING victims v WHERE p.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 7 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_log_generation_checkpoints
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY through_basis_t LIMIT maximum_rows
        )
        DELETE FROM atomic_log_generation_checkpoints c
         USING victims v WHERE c.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 8 THEN
        DELETE FROM atomic_log_generation_completion_stages
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 9 THEN
        -- Keep the build coordinate until the terminal transaction. It is
        -- the authenticated owner row used to resume this durable phase
        -- cursor after a crash.
        removed := 0;
    ELSE
        DELETE FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        DELETE FROM atomic_log_generation_abandonment_progress
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        DELETE FROM atomic_log_generations
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
        IF removed <> 1 THEN
            RAISE EXCEPTION 'Atomic abandoned generation lost its terminal owner row'
                USING ERRCODE = '55000';
        END IF;
        IF generation_row.build_kind = 0 THEN
            -- Every committed restore value is generation-owned. The earlier
            -- phases have therefore removed all database-scoped dependants;
            -- this guarded delete is both the final integrity check and the
            -- point at which the previously unpublished alias becomes free.
            DELETE FROM atomic_databases d
             WHERE d.database_id = candidate_database_id
               AND d.lineage_id = durable_lineage
               AND NOT EXISTS (
                    SELECT 1 FROM atomic_heads h
                     WHERE h.database_id = d.database_id
               )
               AND NOT EXISTS (
                    SELECT 1 FROM atomic_log_generations g
                     WHERE g.database_id = d.database_id
               );
            GET DIAGNOSTICS removed = ROW_COUNT;
            IF removed <> 1 THEN
                RAISE EXCEPTION 'Atomic headless restore catalog is no longer isolated'
                    USING ERRCODE = '55000';
            END IF;
        END IF;
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        rows_removed := removed;
        abandonment_phase := 10;
        is_complete := true;
        RETURN NEXT;
        RETURN;
    END IF;

    IF removed = 0 THEN
        UPDATE atomic_log_generation_abandonment_progress
           SET phase = phase + 1, updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        progress_phase := progress_phase + 1;
    ELSE
        UPDATE atomic_log_generation_abandonment_progress
           SET updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    rows_removed := removed;
    abandonment_phase := progress_phase;
    is_complete := false;
    RETURN NEXT;
END;
$$;

CREATE FUNCTION atomic_activate_initial_log_generation(candidate_database_id text, candidate_generation bigint, candidate_basis bigint, candidate_head_hash bytea, candidate_state_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    builder_pin_key BIGINT;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = candidate_database_id
       FOR UPDATE;
    IF NOT FOUND OR EXISTS (
        SELECT 1 FROM atomic_heads WHERE database_id = candidate_database_id
    ) THEN
        RAISE EXCEPTION 'Atomic initial restore target is already published or missing'
            USING ERRCODE = '55000';
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_log_generation_abandonment_progress
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic initial restore generation is permanently claimed for abandonment'
            USING ERRCODE = '55000';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
          JOIN atomic_log_generation_checkpoints c
            ON c.database_id = g.database_id AND c.generation = g.generation
          JOIN atomic_log_generation_completion_stages s
            ON s.database_id = g.database_id AND s.generation = g.generation
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind = 0
           AND b.source_generation IS NULL
           AND b.frozen_plan_hash IS NOT NULL
           AND c.through_basis_t = candidate_basis
           AND c.head_hash = candidate_head_hash
           AND c.state_hash = candidate_state_hash
           AND s.phase = 2
    ) THEN
        RAISE EXCEPTION 'Atomic initial restore candidate is incomplete'
            USING ERRCODE = '23503';
    END IF;
    SELECT atomic_tree_database_build_pin_key(candidate_database_id)
      INTO builder_pin_key;
    IF builder_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key) THEN
        RAISE EXCEPTION 'Atomic initial restore is blocked by a live builder'
            USING ERRCODE = '55006';
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    INSERT INTO atomic_heads(database_id, basis_t, tx_hash, log_generation)
    VALUES (candidate_database_id, candidate_basis, candidate_head_hash,
            candidate_generation);
    INSERT INTO atomic_log_generation_activations
           (database_id, generation, prior_generation, prior_basis_t,
            basis_t, head_hash, state_hash, manifest_hash)
    VALUES (candidate_database_id, candidate_generation, 0, 0,
            candidate_basis, candidate_head_hash, candidate_state_hash, NULL);
    INSERT INTO atomic_log_generation_completions(database_id, generation)
    VALUES (candidate_database_id, candidate_generation);
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

CREATE FUNCTION atomic_activate_log_generation(candidate_database_id text, candidate_generation bigint, candidate_basis bigint, candidate_head_hash bytea, candidate_state_hash bytea, candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    build_row atomic_log_generation_builds%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    current_generation BIGINT;
    current_basis BIGINT;
    current_hash BYTEA;
    candidate_revision BIGINT;
    source_pin_key BIGINT;
    builder_pin_key BIGINT;
BEGIN
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic generation has no active build'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF NOT FOUND OR generation_row.build_kind = 0 THEN
        RAISE EXCEPTION 'Atomic generation is not an activatable rewrite'
            USING ERRCODE = '23503';
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_log_generation_abandonment_progress
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic generation is permanently claimed for abandonment'
            USING ERRCODE = '55000';
    END IF;
    SELECT log_generation, basis_t, tx_hash
      INTO current_generation, current_basis, current_hash
      FROM atomic_heads
     WHERE database_id = candidate_database_id
       FOR UPDATE;
    IF NOT FOUND OR current_generation <> build_row.source_generation
       OR (generation_row.build_kind = 2
           AND (current_basis <> build_row.captured_basis_t
                OR current_hash <> build_row.captured_head_hash)) THEN
        RAISE EXCEPTION 'Atomic source head changed after generation capture'
            USING ERRCODE = '40001';
    END IF;
    IF generation_row.build_kind = 2 THEN
        IF EXISTS (
            SELECT 1 FROM atomic_transactor_leases
             WHERE lease_scope = candidate_database_id
               AND expires_at > clock_timestamp()
        ) THEN
            RAISE EXCEPTION 'Atomic point restore requires transactors to be stopped'
                USING ERRCODE = '55006';
        END IF;
        -- Durable build rows are resumability ledgers, not liveness evidence.
        -- Active tree/log builders hold the database-scoped shared session
        -- pin; stale intent/build rows after a crash therefore cannot wedge a
        -- restore forever.
        SELECT atomic_tree_database_build_pin_key(candidate_database_id)
          INTO builder_pin_key;
        IF builder_pin_key IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_pin_key) THEN
            RAISE EXCEPTION 'Atomic point restore is blocked by a live generation or tree builder'
                USING ERRCODE = '55006';
        END IF;
        SELECT atomic_log_generation_pin_key(candidate_database_id, current_generation)
          INTO source_pin_key;
        IF source_pin_key IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(source_pin_key) THEN
            RAISE EXCEPTION 'Atomic point restore is blocked by a live peer or backup generation pin'
                USING ERRCODE = '55006';
        END IF;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_checkpoints c
         WHERE c.database_id = candidate_database_id
           AND c.generation = candidate_generation
           AND c.through_basis_t = candidate_basis
           AND c.head_hash = candidate_head_hash
           AND c.state_hash = candidate_state_hash
           AND (generation_row.build_kind = 2
                OR (c.source_head_hash = current_hash AND c.through_basis_t = current_basis))
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation has no matching complete checkpoint'
            USING ERRCODE = '23503';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_completion_stages s
         WHERE s.database_id = candidate_database_id
           AND s.generation = candidate_generation
           AND s.phase = 2
           AND s.sealed_at IS NOT NULL
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation requires a sealed completion set'
            USING ERRCODE = '23503';
    END IF;
    IF generation_row.build_kind = 1 THEN
        IF candidate_manifest_hash IS NULL THEN
            IF EXISTS (
                SELECT 1 FROM atomic_tree_publications
                 WHERE database_id = candidate_database_id
                   AND log_generation = current_generation
            ) THEN
                RAISE EXCEPTION 'Atomic indexed excision requires a staged successor tree'
                    USING ERRCODE = '23503';
            END IF;
        ELSE
            SELECT publication_revision INTO candidate_revision
              FROM atomic_tree_manifests m
             WHERE m.database_id = candidate_database_id
               AND m.log_generation = candidate_generation
               AND m.basis_t = candidate_basis
               AND m.tx_hash = candidate_head_hash
               AND m.state_hash = candidate_state_hash
               AND m.manifest_hash = candidate_manifest_hash;
            IF NOT FOUND OR NOT EXISTS (
                SELECT 1 FROM atomic_tree_delta_headers
                 WHERE manifest_hash = candidate_manifest_hash
            ) THEN
                RAISE EXCEPTION 'Atomic excision activation requires a staged authenticated tree'
                    USING ERRCODE = '23503';
            END IF;
            IF candidate_revision <> COALESCE((
                SELECT max(publication_revision) + 1
                  FROM atomic_tree_publications
                 WHERE database_id = candidate_database_id
            ), 1) THEN
                RAISE EXCEPTION 'Atomic staged tree lost its publication revision race'
                    USING ERRCODE = '40001';
            END IF;
        END IF;
    ELSIF candidate_manifest_hash IS NOT NULL THEN
        RAISE EXCEPTION 'Atomic log-only restore must not claim a tree root'
            USING ERRCODE = '23514';
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_heads
       SET log_generation = candidate_generation,
           basis_t = candidate_basis,
           tx_hash = candidate_head_hash
     WHERE database_id = candidate_database_id;
    INSERT INTO atomic_log_generation_activations
           (database_id, generation, prior_generation, prior_basis_t,
            basis_t, head_hash, state_hash, manifest_hash)
    VALUES (candidate_database_id, candidate_generation, current_generation,
            current_basis, candidate_basis, candidate_head_hash,
            candidate_state_hash, candidate_manifest_hash);
    INSERT INTO atomic_log_generation_retirements
           (database_id, generation, successor_generation)
    VALUES (candidate_database_id, current_generation, candidate_generation);
    IF generation_row.build_kind = 2 THEN
        -- Restore prepared its exact completion set under the inactive
        -- generation. One root marker makes that set active with the head.
        INSERT INTO atomic_log_generation_completions(database_id, generation)
        VALUES (candidate_database_id, candidate_generation);
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

CREATE FUNCTION atomic_admit_semantic_commitment_node_insert() RETURNS trigger
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

CREATE FUNCTION atomic_apply_tree_publication_delta() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    mode SMALLINT;
    predecessor_revision BIGINT;
    predecessor_hash BYTEA;
BEGIN
    SELECT delta_mode, predecessor_manifest_hash
      INTO STRICT mode, predecessor_hash
      FROM atomic_tree_delta_headers
     WHERE manifest_hash = NEW.manifest_hash;

    SELECT p.publication_revision
      INTO predecessor_revision
      FROM atomic_tree_publications p
     WHERE p.database_id = NEW.database_id
       AND p.publication_revision = NEW.publication_revision - 1
       AND p.manifest_hash = predecessor_hash;

    IF predecessor_revision IS NOT NULL THEN
        INSERT INTO atomic_tree_retirements
               (database_id, publication_revision, manifest_hash, retired_at,
                bookkeeping_complete, garbage_complete)
        VALUES (NEW.database_id, predecessor_revision, predecessor_hash,
                clock_timestamp(), mode = 0, false);
    END IF;

    INSERT INTO atomic_tree_publication_states
           (manifest_hash, predecessor_manifest_hash, delta_mode)
    VALUES (NEW.manifest_hash, predecessor_hash, mode);

    IF mode = 0 THEN
        INSERT INTO atomic_tree_live_sets
               (database_id, manifest_hash, complete, problem_code, updated_at)
        VALUES (NEW.database_id, NEW.manifest_hash, false,
                'tree/live-membership-unknown', clock_timestamp())
        ON CONFLICT (database_id) DO UPDATE
            SET manifest_hash = EXCLUDED.manifest_hash,
                complete = EXCLUDED.complete,
                problem_code = EXCLUDED.problem_code,
                updated_at = EXCLUDED.updated_at;
        DELETE FROM atomic_tree_delta_headers
         WHERE manifest_hash = NEW.manifest_hash;
    ELSE
        -- The final root transaction performs only constant work.  The
        -- possibly huge upload and delta ledgers remain durable pins while
        -- bounded owner transactions fold them after publication.
        UPDATE atomic_tree_build_intents
           SET intent_state = 2, heartbeat_at = clock_timestamp()
         WHERE manifest_hash = NEW.manifest_hash
           AND intent_state = 1
           AND staged_node_count = expected_node_count;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic tree publication could not consume its sealed upload intent'
                USING ERRCODE = '40001';
        END IF;
        UPDATE atomic_tree_delta_headers
           SET delta_state = 2
         WHERE manifest_hash = NEW.manifest_hash
           AND delta_state = 1
           AND staged_node_count = expected_node_count;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic tree publication could not consume its sealed delta'
                USING ERRCODE = '40001';
        END IF;
    END IF;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_apply_tree_publication_work(candidate_manifest_hash bytea, maximum_nodes bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    candidate_database_id TEXT;
    candidate_revision BIGINT;
    mode SMALLINT;
    predecessor_hash BYTEA;
    predecessor_revision BIGINT;
    moved BIGINT := 0;
    planned BIGINT := 0;
    removed_live BIGINT := 0;
    cleared BIGINT := 0;
    remaining BIGINT;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic tree publication-work boundary'
            USING ERRCODE = '22023';
    END IF;

    SELECT p.database_id, p.publication_revision, h.delta_mode,
           h.predecessor_manifest_hash
      INTO candidate_database_id, candidate_revision, mode, predecessor_hash
      FROM atomic_tree_delta_headers h
      JOIN atomic_tree_publications p ON p.manifest_hash = h.manifest_hash
     WHERE h.manifest_hash = candidate_manifest_hash
       AND h.delta_state = 2
     FOR UPDATE OF h;
    IF NOT FOUND THEN
        RETURN EXISTS (
            SELECT 1
              FROM atomic_tree_publications p
              JOIN atomic_tree_live_sets l
                ON l.database_id = p.database_id
               AND l.manifest_hash = p.manifest_hash
             WHERE p.manifest_hash = candidate_manifest_hash
               AND l.complete
        );
    END IF;
    IF mode NOT IN (1, 2) THEN
        RAISE EXCEPTION 'Published Atomic tree work has an invalid delta mode'
            USING ERRCODE = '55000';
    END IF;

    SELECT publication_revision INTO predecessor_revision
      FROM atomic_tree_publications
     WHERE database_id = candidate_database_id
       AND publication_revision = candidate_revision - 1
       AND manifest_hash = predecessor_hash;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);

    -- Protect additions in current membership before releasing their durable
    -- delta row.  The still-retained build-intent ledger is a second pin until
    -- the entire publication fold is sealed.
    WITH batch AS MATERIALIZED (
        SELECT node_hash
          FROM atomic_tree_delta_nodes
         WHERE manifest_hash = candidate_manifest_hash AND direction = 1
         ORDER BY node_hash
         LIMIT maximum_nodes
         FOR UPDATE
    ), protected AS (
        INSERT INTO atomic_tree_live_nodes (database_id, node_hash)
        SELECT candidate_database_id, node_hash FROM batch
        ON CONFLICT DO NOTHING
        RETURNING node_hash
    ), unmarked AS (
        DELETE FROM atomic_tree_garbage_nodes g
         USING batch
         WHERE g.node_hash = batch.node_hash
        RETURNING g.node_hash
    ), consumed AS (
        DELETE FROM atomic_tree_delta_nodes d
         USING batch
         WHERE d.manifest_hash = candidate_manifest_hash
           AND d.direction = 1
           AND d.node_hash = batch.node_hash
        RETURNING d.node_hash
    )
    SELECT count(*) INTO moved FROM consumed;
    remaining := maximum_nodes - moved;

    IF remaining > 0 AND mode = 2 THEN
        IF predecessor_revision IS NULL THEN
            RAISE EXCEPTION 'Incremental Atomic tree work lost its predecessor'
                USING ERRCODE = '55000';
        END IF;
        WITH batch AS MATERIALIZED (
            SELECT node_hash
              FROM atomic_tree_delta_nodes
             WHERE manifest_hash = candidate_manifest_hash AND direction = -1
             ORDER BY node_hash
             LIMIT remaining
             FOR UPDATE
        ), retained AS (
            INSERT INTO atomic_tree_retired_nodes
                   (database_id, publication_revision, node_hash)
            SELECT candidate_database_id, predecessor_revision, node_hash FROM batch
            ON CONFLICT DO NOTHING
            RETURNING node_hash
        ), removed AS (
            DELETE FROM atomic_tree_live_nodes l
             USING batch
             WHERE l.database_id = candidate_database_id
               AND l.node_hash = batch.node_hash
            RETURNING l.node_hash
        ), consumed AS (
            DELETE FROM atomic_tree_delta_nodes d
             USING batch
             WHERE d.manifest_hash = candidate_manifest_hash
               AND d.direction = -1
               AND d.node_hash = batch.node_hash
            RETURNING d.node_hash
        )
        SELECT (SELECT count(*) FROM batch),
               (SELECT count(*) FROM removed),
               (SELECT count(*) FROM consumed)
          INTO planned, removed_live, cleared;
        IF planned <> removed_live OR planned <> cleared THEN
            RAISE EXCEPTION 'Incremental Atomic tree work retires a non-live node'
                USING ERRCODE = '55000';
        END IF;
        moved := moved + cleared;
        remaining := maximum_nodes - moved;
    END IF;

    -- A replacement's build intent is the complete successor membership, so
    -- old-minus-new is derived exactly without materializing a second full
    -- closure in the root transaction.
    IF remaining > 0 AND mode = 1 AND predecessor_revision IS NOT NULL
       AND NOT EXISTS (
           SELECT 1 FROM atomic_tree_delta_nodes
            WHERE manifest_hash = candidate_manifest_hash
       ) THEN
        WITH batch AS MATERIALIZED (
            SELECT l.node_hash
              FROM atomic_tree_live_nodes l
             WHERE l.database_id = candidate_database_id
               AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_build_intent_nodes i
                    WHERE i.manifest_hash = candidate_manifest_hash
                      AND i.node_hash = l.node_hash
               )
             ORDER BY l.node_hash
             LIMIT remaining
             FOR UPDATE OF l
        ), retained AS (
            INSERT INTO atomic_tree_retired_nodes
                   (database_id, publication_revision, node_hash)
            SELECT candidate_database_id, predecessor_revision, node_hash FROM batch
            ON CONFLICT DO NOTHING
            RETURNING node_hash
        ), removed AS (
            DELETE FROM atomic_tree_live_nodes l
             USING batch
             WHERE l.database_id = candidate_database_id
               AND l.node_hash = batch.node_hash
            RETURNING l.node_hash
        )
        SELECT (SELECT count(*) FROM batch), (SELECT count(*) FROM removed)
          INTO planned, removed_live;
        IF planned <> removed_live THEN
            RAISE EXCEPTION 'Replacement Atomic tree work changed while folding'
                USING ERRCODE = '40001';
        END IF;
        moved := moved + removed_live;
    END IF;

    IF EXISTS (
        SELECT 1 FROM atomic_tree_delta_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) OR (mode = 1 AND EXISTS (
        SELECT 1
          FROM atomic_tree_live_nodes l
         WHERE l.database_id = candidate_database_id
           AND NOT EXISTS (
               SELECT 1 FROM atomic_tree_build_intent_nodes i
                WHERE i.manifest_hash = candidate_manifest_hash
                  AND i.node_hash = l.node_hash
           )
    )) THEN
        PERFORM set_config('atomic.tree_gc_active', 'off', true);
        RETURN FALSE;
    END IF;

    IF predecessor_revision IS NOT NULL THEN
        UPDATE atomic_tree_retirements
           SET bookkeeping_complete = true, garbage_complete = true
         WHERE database_id = candidate_database_id
           AND publication_revision = predecessor_revision
           AND manifest_hash = predecessor_hash
           AND NOT bookkeeping_complete;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic tree work lost its predecessor retirement'
                USING ERRCODE = '55000';
        END IF;
    END IF;
    INSERT INTO atomic_tree_live_sets
           (database_id, manifest_hash, complete, problem_code, updated_at)
    VALUES (candidate_database_id, candidate_manifest_hash, true, NULL,
            clock_timestamp())
    ON CONFLICT (database_id) DO UPDATE
        SET manifest_hash = EXCLUDED.manifest_hash,
            complete = true,
            problem_code = NULL,
            updated_at = EXCLUDED.updated_at;
    DELETE FROM atomic_tree_delta_headers
     WHERE manifest_hash = candidate_manifest_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
    RETURN TRUE;
END;
$$;

CREATE FUNCTION atomic_assert_excision_worker(candidate_database_id text, candidate_holder text, candidate_epoch bigint) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    lease_holder TEXT;
    lease_epoch BIGINT;
    lease_expires TIMESTAMPTZ;
BEGIN
    IF candidate_database_id IS NULL OR candidate_database_id=''
       OR candidate_holder IS NULL OR candidate_holder=''
       OR candidate_epoch IS NULL OR candidate_epoch<=0 THEN
        RAISE EXCEPTION 'Invalid Atomic excision worker authority' USING ERRCODE='22023';
    END IF;
    -- Same ordering as the ordinary writer and retirement: lease first.
    SELECT holder_id, epoch, expires_at INTO lease_holder, lease_epoch, lease_expires
      FROM atomic_transactor_leases WHERE lease_scope=candidate_database_id FOR UPDATE;
    IF NOT FOUND OR lease_holder<>candidate_holder OR lease_epoch<>candidate_epoch
       OR lease_expires<=clock_timestamp() THEN
        RAISE EXCEPTION 'Atomic excision worker lease is not current' USING ERRCODE='55000';
    END IF;
    PERFORM 1 FROM atomic_database_identities
      WHERE database_id=candidate_database_id AND retired_at IS NULL FOR SHARE;
    IF NOT FOUND OR lease_expires<=clock_timestamp() THEN
        RAISE EXCEPTION 'Atomic excision worker identity or lease is no longer active'
            USING ERRCODE='55000';
    END IF;
END;
$$;

CREATE FUNCTION atomic_begin_receipt_archive_conversion(candidate_hash bytea, older_than_millis bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE source atomic_tree_manifests%ROWTYPE; initial_count BIGINT;
BEGIN
    IF older_than_millis IS NULL OR older_than_millis<0
       OR NOT atomic_receipt_archive_conversion_context(candidate_hash) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions WHERE manifest_hash=candidate_hash) THEN
        RETURN TRUE;
    END IF;
    SELECT m.* INTO source FROM atomic_tree_manifests m
      JOIN atomic_tree_publications p USING(manifest_hash)
      JOIN atomic_tree_retirements r ON r.manifest_hash=m.manifest_hash
      JOIN atomic_heads head ON head.database_id=p.database_id
                            AND head.log_generation=p.log_generation
     WHERE m.manifest_hash=candidate_hash
       AND r.retired_at<clock_timestamp()-older_than_millis*interval '1 millisecond'
       AND EXISTS (SELECT 1 FROM atomic_generation_request_bases base
                    WHERE base.base_manifest_hash=candidate_hash)
     FOR SHARE OF m;
    IF NOT FOUND OR pg_catalog.sha256(source.payload)<>candidate_hash THEN RETURN FALSE; END IF;
    SELECT count(DISTINCT root_hash) INTO initial_count
      FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    IF initial_count<1 OR (SELECT count(*) FROM atomic_tree_manifest_roots
                           WHERE manifest_hash=candidate_hash)<>8 THEN
        RAISE EXCEPTION 'Atomic archive conversion source has incomplete roots' USING ERRCODE='23514';
    END IF;
    INSERT INTO atomic_receipt_archive_conversions
        (manifest_hash,database_id,generation,publication_revision,phase,node_count)
    VALUES (candidate_hash,source.database_id,source.log_generation,
            source.publication_revision,1,initial_count);
    INSERT INTO atomic_request_base_archives
        (database_id,generation,archive_revision,basis_t,tx_hash,state_hash,eidx_frontier,
         manifest_version,manifest_hash,payload,expected_node_count,node_set_hash)
    VALUES (source.database_id,source.log_generation,source.publication_revision,source.basis_t,
            source.tx_hash,source.state_hash,source.eidx_frontier,source.manifest_version,
            candidate_hash,source.payload,initial_count,decode(repeat('00',32),'hex'));
    INSERT INTO atomic_request_base_archive_roots
        (manifest_hash,index_order,history,root_hash,datom_count,encoded_bytes)
    SELECT manifest_hash,index_order,history,root_hash,datom_count,encoded_bytes
      FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    INSERT INTO atomic_request_base_archive_nodes(manifest_hash,node_hash)
    SELECT DISTINCT candidate_hash,root_hash FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    INSERT INTO atomic_receipt_archive_frontier(manifest_hash,node_hash)
    SELECT manifest_hash,node_hash FROM atomic_request_base_archive_nodes WHERE manifest_hash=candidate_hash;
    RETURN TRUE;
END;
$$;

CREATE FUNCTION atomic_cleanup_log_generation_build(candidate_database_id text, candidate_generation bigint, maximum_rows bigint) RETURNS TABLE(rows_removed bigint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    removed BIGINT;
BEGIN
    IF maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic generation build cleanup boundary'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generation_activations a
          JOIN atomic_log_generation_completions c
            ON c.database_id = a.database_id AND c.generation = a.generation
         WHERE a.database_id = candidate_database_id
           AND a.generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic generation build is not durably complete'
            USING ERRCODE = '55000';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        rows_removed := 0;
        is_complete := true;
        RETURN NEXT;
        RETURN;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    WITH victims AS (
        SELECT ctid FROM atomic_generation_excision_predicates
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
         ORDER BY request_t, request_entity
         LIMIT maximum_rows
    )
    DELETE FROM atomic_generation_excision_predicates p
     USING victims v WHERE p.ctid = v.ctid;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    WITH victims AS (
        SELECT ctid FROM atomic_log_generation_checkpoints
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
         ORDER BY through_basis_t
         LIMIT maximum_rows
    )
    DELETE FROM atomic_log_generation_checkpoints c
     USING victims v WHERE c.ctid = v.ctid;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    DELETE FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    GET DIAGNOSTICS removed = ROW_COUNT;
    IF removed > 0 THEN
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        rows_removed := removed;
        is_complete := false;
        RETURN NEXT;
        RETURN;
    END IF;

    DELETE FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    GET DIAGNOSTICS removed = ROW_COUNT;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    rows_removed := removed;
    is_complete := true;
    RETURN NEXT;
END;
$$;

CREATE FUNCTION atomic_collect_fulltext_garbage(maximum_blocks bigint) RETURNS bigint
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE removed BIGINT;
BEGIN
    WITH candidates AS MATERIALIZED (
        SELECT * FROM atomic_fulltext_garbage_candidates(maximum_blocks)
    ), legacy AS (
        DELETE FROM atomic_fulltext_blocks b USING candidates c
        WHERE b.manifest_hash=c.manifest_hash AND b.block_hash=c.block_hash RETURNING 1
    ), shared AS (
        DELETE FROM atomic_fulltext_pages p USING candidates c
        WHERE c.manifest_hash IS NULL AND p.block_hash=c.block_hash RETURNING 1
    ) SELECT count(*) INTO removed FROM (SELECT * FROM legacy UNION ALL SELECT * FROM shared) deleted;
    DELETE FROM atomic_fulltext_garbage g WHERE g.manifest_hash IN (
        SELECT candidate.manifest_hash FROM atomic_fulltext_garbage candidate
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_blocks b WHERE b.manifest_hash=candidate.manifest_hash)
        ORDER BY candidate.manifest_hash LIMIT maximum_blocks
    );
    RETURN removed;
END;
$$;

CREATE FUNCTION atomic_collect_log_generation(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) RETURNS TABLE(rows_removed bigint, collection_phase smallint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_collect_log_generation_without_semantic_v14(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) RETURNS TABLE(rows_removed bigint, collection_phase smallint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    retirement_row atomic_log_generation_retirements%ROWTYPE;
    progress_phase SMALLINT;
    generation_pin_key BIGINT;
    removed BIGINT := 0;
    ignored BIGINT;
    content_candidate RECORD;
BEGIN
    IF candidate_generation < 0 OR older_than_millis < 0
       OR maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic log-generation garbage boundary'
            USING ERRCODE = '22023';
    END IF;
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
       AND retirement_row.retired_at + older_than_millis * interval '1 millisecond'
           > clock_timestamp() THEN
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

    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_pin_key;
    IF generation_pin_key IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_pin_key) THEN
        RAISE EXCEPTION 'Atomic log generation is pinned by a peer or backup'
            USING ERRCODE = '55006';
    END IF;

    -- These ledgers are consumed by their own bounded collectors. Numeric
    -- generation columns deliberately have no cascading FK: collection must
    -- prove every physical reader is gone before removing authoritative bytes.
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
        SELECT 1 FROM atomic_log_generation_builds
         WHERE database_id = candidate_database_id
           AND (generation = candidate_generation
                OR source_generation = candidate_generation)
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND successor_generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic log generation still has a derived root or build dependency'
            USING ERRCODE = '55000';
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    IF retirement_row.collecting_at IS NULL THEN
        UPDATE atomic_log_generation_retirements
           SET collecting_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    INSERT INTO atomic_log_generation_collection_progress
           (database_id, generation)
    VALUES (candidate_database_id, candidate_generation)
    ON CONFLICT DO NOTHING;
    SELECT phase INTO progress_phase
      FROM atomic_log_generation_collection_progress
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;

    IF progress_phase = 0 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_index_publications
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_index_publications p
             USING victims v WHERE p.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 1 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_index_manifests
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_index_manifests m
             USING victims v WHERE m.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 2 THEN
        IF candidate_generation > 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_generation_request_tempids
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY request_key_hash, tempid_name
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_generation_request_tempids t
             USING victims v WHERE t.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 3 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_requests
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_requests r
             USING victims v WHERE r.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        ELSE
            WITH victims AS (
                SELECT ctid FROM atomic_generation_requests
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_generation_requests r
             USING victims v WHERE r.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 4 THEN
        IF candidate_generation = 0 THEN
            WITH victims AS (
                SELECT ctid FROM atomic_transactions
                 WHERE database_id = candidate_database_id
                 ORDER BY basis_t
                 LIMIT maximum_rows
            )
            DELETE FROM atomic_transactions t
             USING victims v WHERE t.ctid = v.ctid;
            GET DIAGNOSTICS removed = ROW_COUNT;
        ELSE
            WITH victims AS MATERIALIZED (
                SELECT ctid, content_hash
                  FROM atomic_generation_transactions
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY basis_t
                 LIMIT maximum_rows
            ), marked AS (
                INSERT INTO atomic_log_generation_garbage_contents
                       (database_id, generation, content_hash)
                SELECT candidate_database_id, candidate_generation, content_hash
                  FROM victims
                ON CONFLICT DO NOTHING
                RETURNING 1
            ), deleted AS (
                DELETE FROM atomic_generation_transactions t
                 USING victims v WHERE t.ctid = v.ctid
                RETURNING 1
            )
            SELECT count(*), (SELECT count(*) FROM marked)
              INTO removed, ignored FROM deleted;
        END IF;
    ELSIF progress_phase = 5 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_program_generation_refs
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
             ORDER BY program_hash
             LIMIT maximum_rows
        )
        DELETE FROM atomic_program_generation_refs r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 6 THEN
        IF candidate_generation > 0 THEN
            FOR content_candidate IN
                SELECT content_hash
                  FROM atomic_log_generation_garbage_contents
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                 ORDER BY content_hash
                 LIMIT maximum_rows
                 FOR UPDATE SKIP LOCKED
            LOOP
                DELETE FROM atomic_log_generation_garbage_contents
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation
                   AND content_hash = content_candidate.content_hash;
                IF NOT EXISTS (
                    SELECT 1 FROM atomic_generation_transactions
                     WHERE content_hash = content_candidate.content_hash
                ) AND NOT EXISTS (
                    SELECT 1 FROM atomic_log_generation_garbage_contents
                     WHERE content_hash = content_candidate.content_hash
                ) THEN
                    DELETE FROM atomic_transaction_contents
                     WHERE content_hash = content_candidate.content_hash;
                END IF;
                removed := removed + 1;
            END LOOP;
        END IF;
    ELSIF progress_phase = 7 THEN
        WITH victims AS (
            SELECT ctid FROM atomic_completed_excision_requests
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
             ORDER BY request_t, request_entity
             LIMIT maximum_rows
        )
        DELETE FROM atomic_completed_excision_requests r
         USING victims v WHERE r.ctid = v.ctid;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 8 THEN
        DELETE FROM atomic_log_generation_completions
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
    ELSIF progress_phase = 9 THEN
        IF candidate_generation > 0 THEN
            DELETE FROM atomic_log_generation_activations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSIF progress_phase = 10 THEN
        IF candidate_generation > 0 THEN
            DELETE FROM atomic_log_generations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            GET DIAGNOSTICS removed = ROW_COUNT;
        END IF;
    ELSE
        DELETE FROM atomic_log_generation_retirements
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        GET DIAGNOSTICS removed = ROW_COUNT;
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        rows_removed := removed;
        collection_phase := 11;
        is_complete := true;
        RETURN NEXT;
        RETURN;
    END IF;

    IF removed = 0 THEN
        UPDATE atomic_log_generation_collection_progress
           SET phase = phase + 1, updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
        progress_phase := progress_phase + 1;
    ELSE
        UPDATE atomic_log_generation_collection_progress
           SET updated_at = clock_timestamp()
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    rows_removed := removed;
    collection_phase := progress_phase;
    is_complete := false;
    RETURN NEXT;
END;
$$;

CREATE FUNCTION atomic_collect_program_garbage(older_than_millis bigint, maximum_programs bigint) RETURNS SETOF bytea
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF older_than_millis < 0 OR maximum_programs < 1 OR maximum_programs > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic program garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    PERFORM singleton FROM atomic_program_reference_state
     WHERE singleton AND complete AND problem_code IS NULL AND walker_version = 2
       FOR SHARE;
    IF NOT FOUND THEN
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
    RETURNING p.program_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

CREATE FUNCTION atomic_collect_request_base_archive(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS TABLE(rows_removed bigint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RAISE EXCEPTION 'Atomic receipt archive collection is busy' USING ERRCODE='55006';
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions
                WHERE manifest_hash=candidate_manifest_hash) THEN
        RAISE EXCEPTION 'Atomic receipt archive conversion is still active' USING ERRCODE='55006';
    END IF;
    RETURN QUERY SELECT * FROM atomic_collect_request_base_archive_unconverted_v20(
        candidate_database_id,candidate_generation,candidate_manifest_hash,older_than_millis,maximum_nodes);
END;
$$;

CREATE FUNCTION atomic_collect_request_base_archive_unconverted_v20(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS TABLE(rows_removed bigint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    archive_row atomic_request_base_archives%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    retirement_row atomic_log_generation_retirements%ROWTYPE;
    generation_lock BIGINT;
    manifest_lock BIGINT;
    builder_lock BIGINT;
    worker_lock BIGINT;
    restore_lock BIGINT;
    durable_lineage TEXT;
    durable_genesis_hash BYTEA;
    already_claimed BOOLEAN := false;
    abandoned BOOLEAN := false;
    removed BIGINT := 0;
    one_removed BIGINT := 0;
BEGIN
    IF candidate_generation <= 0 OR octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0 OR maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic request-base archive garbage boundary'
            USING ERRCODE = '22023';
    END IF;
    SELECT * INTO archive_row
      FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND THEN
        RETURN QUERY SELECT 0::bigint, true;
        RETURN;
    END IF;
    SELECT atomic_log_generation_pin_key(candidate_database_id, candidate_generation)
      INTO generation_lock;
    manifest_lock := (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                     # 4707465863597391872::bigint;
    IF generation_lock IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock(generation_lock)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(manifest_lock) THEN
        RAISE EXCEPTION 'Atomic request-base archive is pinned by a live reader'
            USING ERRCODE = '55006';
    END IF;

    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND build_kind IN (0, 1, 2)
     FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic request-base archive lost its generation owner'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;
    SELECT * INTO retirement_row
      FROM atomic_log_generation_retirements
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
     FOR UPDATE;
    IF EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Active Atomic request-base archive cannot be collected'
            USING ERRCODE = '55000';
    END IF;

    IF retirement_row.generation IS NOT NULL THEN
        IF retirement_row.collecting_at IS NULL
           AND retirement_row.retired_at
               + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic request-base archive has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
    ELSE
        abandoned := true;
        IF build_row.generation IS NULL
           OR EXISTS (
               SELECT 1 FROM atomic_log_generation_activations
                WHERE database_id = candidate_database_id
                  AND generation = candidate_generation
           ) THEN
            RAISE EXCEPTION 'Atomic request-base archive generation is not an inactive restore'
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
        ELSIF generation_row.build_kind = 2
              AND build_row.source_generation IS NULL THEN
            RAISE EXCEPTION 'Atomic point restore abandonment has no captured source generation'
                USING ERRCODE = '23503';
        END IF;
        IF NOT already_claimed AND build_row.source_generation IS NOT NULL AND EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = build_row.source_generation
        ) THEN
            RAISE EXCEPTION 'Atomic inactive restore remains exactly resumable'
                USING ERRCODE = '55000';
        END IF;
        IF NOT already_claimed AND generation_row.created_at
           + older_than_millis * interval '1 millisecond' > clock_timestamp() THEN
            RAISE EXCEPTION 'Atomic inactive restore has not reached its retention age'
                USING ERRCODE = '55000';
        END IF;
        SELECT atomic_tree_database_build_pin_key(candidate_database_id)
          INTO builder_lock;
        worker_lock := hashtextextended(
            'atomic/excision-worker/v1/' || durable_lineage,
            4707476001900298240::bigint
        );
        restore_lock := hashtextextended(
            'atomic/restore/' || candidate_database_id,
            0
        );
        IF builder_lock IS NULL OR worker_lock IS NULL OR restore_lock IS NULL
           OR NOT pg_catalog.pg_try_advisory_xact_lock(builder_lock)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(worker_lock)
           OR NOT pg_catalog.pg_try_advisory_xact_lock(restore_lock) THEN
            RAISE EXCEPTION 'Atomic inactive restore is pinned by a live builder or reader'
                USING ERRCODE = '55006';
        END IF;
    END IF;

    PERFORM set_config('atomic.log_generation_gc', 'v14', true);
    PERFORM set_config('atomic.request_base_archive_gc', 'v20', true);
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
    WITH victims AS MATERIALIZED (
        SELECT base.ctid
          FROM atomic_generation_request_bases base
         WHERE base.database_id = candidate_database_id
           AND base.generation = candidate_generation
           AND base.base_manifest_hash = candidate_manifest_hash
         ORDER BY base.request_key_hash
         LIMIT maximum_nodes
         FOR UPDATE
    )
    DELETE FROM atomic_generation_request_bases base
     USING victims
     WHERE base.ctid = victims.ctid;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    IF EXISTS (
        SELECT 1 FROM atomic_generation_request_bases
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
           AND base_manifest_hash = candidate_manifest_hash
    ) THEN
        PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        RETURN QUERY SELECT removed, false;
        RETURN;
    END IF;
    DELETE FROM atomic_request_base_archive_completions
     WHERE manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    INSERT INTO atomic_tree_garbage_nodes(node_hash, marked_at)
    SELECT planned.node_hash, clock_timestamp()
      FROM atomic_request_base_archive_nodes planned
      JOIN atomic_tree_nodes stored ON stored.node_hash = planned.node_hash
     WHERE planned.manifest_hash = candidate_manifest_hash
     ORDER BY planned.node_hash LIMIT maximum_nodes
    ON CONFLICT (node_hash) DO NOTHING;
    WITH batch AS MATERIALIZED (
        SELECT node_hash FROM atomic_request_base_archive_nodes
         WHERE manifest_hash = candidate_manifest_hash
         ORDER BY node_hash LIMIT maximum_nodes FOR UPDATE
    )
    DELETE FROM atomic_request_base_archive_nodes node
     USING batch
     WHERE node.manifest_hash = candidate_manifest_hash
       AND node.node_hash = batch.node_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    IF EXISTS (
        SELECT 1 FROM atomic_request_base_archive_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
        PERFORM set_config('atomic.log_generation_activation', 'off', true);
        PERFORM set_config('atomic.log_generation_gc', 'off', true);
        RETURN QUERY SELECT removed, false;
        RETURN;
    END IF;
    DELETE FROM atomic_request_base_archive_roots
     WHERE manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    DELETE FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash;
    GET DIAGNOSTICS one_removed = ROW_COUNT;
    removed := removed + one_removed;
    PERFORM set_config('atomic.request_base_archive_gc', 'off', true);
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    PERFORM set_config('atomic.log_generation_gc', 'off', true);
    RETURN QUERY SELECT removed, true;
END;
$$;

CREATE FUNCTION atomic_collect_semantic_commitment_garbage(older_than_millis bigint, maximum_nodes bigint) RETURNS TABLE(node_hash bytea)
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_collect_semantic_commitment_generation_roots(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_roots bigint, abandoned boolean) RETURNS TABLE(basis_t bigint, tx_hash bytea, current_root bytea)
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_collect_tree_build_intent(candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    lock_key BIGINT;
    removed_intents BIGINT;
    removed_nodes BIGINT;
    remaining BIGINT;
    state SMALLINT;
    last_heartbeat TIMESTAMPTZ;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0
       OR maximum_nodes < 1
       OR maximum_nodes > 4096 THEN
        RETURN FALSE;
    END IF;
    lock_key :=
        (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
        # 4707460391809056768::bigint;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(lock_key) THEN
        RETURN FALSE;
    END IF;
    SELECT intent_state, heartbeat_at INTO state, last_heartbeat
      FROM atomic_tree_build_intents
     WHERE manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- A durable activation owns a future tree only before publication. State
    -- 2 proves that publication already occurred; if the superseded root was
    -- collected by an older binary, its upload ledger is ordinary bounded
    -- bookkeeping and must remain collectible.
    IF state IN (0, 1) AND EXISTS (
        SELECT 1 FROM atomic_log_generation_activations
         WHERE manifest_hash = candidate_manifest_hash
    ) AND NOT EXISTS (
        SELECT 1 FROM atomic_tree_publications
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
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

    IF state = 2 AND EXISTS (
        SELECT 1 FROM atomic_tree_delta_headers
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        RETURN FALSE;
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
    GET DIAGNOSTICS removed_nodes = ROW_COUNT;
    remaining := maximum_nodes - removed_nodes;
    IF remaining > 0 AND state = 3 THEN
        DELETE FROM atomic_tree_delta_nodes n
         USING (
                SELECT selected.node_hash
                  FROM atomic_tree_delta_nodes selected
                 WHERE selected.manifest_hash = candidate_manifest_hash
                 ORDER BY selected.node_hash
                 LIMIT remaining
               ) batch
         WHERE n.manifest_hash = candidate_manifest_hash
           AND n.node_hash = batch.node_hash;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_tree_build_intent_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) AND NOT EXISTS (
        SELECT 1 FROM atomic_tree_delta_nodes
         WHERE manifest_hash = candidate_manifest_hash
    ) THEN
        DELETE FROM atomic_tree_delta_headers
         WHERE manifest_hash = candidate_manifest_hash;
        IF state = 3 THEN
            DELETE FROM atomic_tree_manifest_roots
             WHERE manifest_hash = candidate_manifest_hash;
            DELETE FROM atomic_tree_manifests
             WHERE manifest_hash = candidate_manifest_hash
               AND NOT EXISTS (
                   SELECT 1 FROM atomic_tree_publications
                    WHERE manifest_hash = candidate_manifest_hash
               );
        END IF;
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

CREATE FUNCTION atomic_collect_tree_garbage(maximum_nodes bigint) RETURNS SETOF bytea
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF maximum_nodes < 1 OR maximum_nodes > 4096 THEN
        RAISE EXCEPTION 'Atomic tree garbage batch must be between 1 and 4096'
            USING ERRCODE = '22023';
    END IF;
    IF EXISTS (
        SELECT 1 FROM (
            SELECT DISTINCT ON (database_id) database_id, manifest_hash
              FROM atomic_tree_publications
             ORDER BY database_id, publication_revision DESC
        ) current_root
        LEFT JOIN atomic_tree_live_sets live USING (database_id)
        WHERE live.database_id IS NULL
           OR live.manifest_hash <> current_root.manifest_hash
           OR NOT live.complete
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_manifest_roots root
        JOIN atomic_tree_live_sets live ON live.manifest_hash = root.manifest_hash
        WHERE live.complete AND NOT EXISTS (
            SELECT 1 FROM atomic_tree_live_nodes node
             WHERE node.database_id = live.database_id
               AND node.node_hash = root.root_hash
        )
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_retirements WHERE NOT garbage_complete
    ) OR EXISTS (
        SELECT 1 FROM atomic_tree_publications publication
         WHERE EXISTS (
             SELECT 1 FROM atomic_tree_publications newer
              WHERE newer.database_id = publication.database_id
                AND newer.publication_revision > publication.publication_revision
         ) AND NOT EXISTS (
             SELECT 1 FROM atomic_tree_retirements retirement
              WHERE retirement.database_id = publication.database_id
                AND retirement.publication_revision = publication.publication_revision
                AND retirement.manifest_hash = publication.manifest_hash
         )
    ) THEN
        RETURN;
    END IF;

    PERFORM set_config('atomic.tree_gc_active', 'v13', true);
    RETURN QUERY
    WITH candidates AS MATERIALIZED (
        SELECT garbage.node_hash
          FROM atomic_tree_garbage_nodes garbage
         WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_live_nodes live
                            WHERE live.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_retired_nodes retired
                            WHERE retired.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_nodes delta
                            WHERE delta.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intent_nodes intent
                            WHERE intent.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots root
                            WHERE root.root_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_nodes archive
                            WHERE archive.node_hash = garbage.node_hash)
           AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_roots archive_root
                            WHERE archive_root.root_hash = garbage.node_hash)
         ORDER BY garbage.node_hash
         LIMIT maximum_nodes FOR UPDATE OF garbage SKIP LOCKED
    )
    DELETE FROM atomic_tree_nodes node USING candidates
     WHERE node.node_hash = candidates.node_hash
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_live_nodes live
                        WHERE live.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_retired_nodes retired
                        WHERE retired.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_delta_nodes delta
                        WHERE delta.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intent_nodes intent
                        WHERE intent.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_manifest_roots root
                        WHERE root.root_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_nodes archive
                        WHERE archive.node_hash = node.node_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_roots archive_root
                        WHERE archive_root.root_hash = node.node_hash)
    RETURNING node.node_hash;
    PERFORM set_config('atomic.tree_gc_active', 'off', true);
END;
$$;

CREATE FUNCTION atomic_collect_tree_retirement(candidate_database_id text, candidate_publication_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions
                WHERE manifest_hash=candidate_manifest_hash) THEN RETURN FALSE; END IF;
    RETURN atomic_collect_tree_retirement_unconverted_v22(
        candidate_database_id,candidate_publication_revision,candidate_manifest_hash,older_than_millis,maximum_nodes);
END;
$$;

CREATE FUNCTION atomic_collect_tree_retirement_unbound_v13(candidate_database_id text, candidate_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
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
           AND r.bookkeeping_complete
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
           AND r.bookkeeping_complete
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

CREATE FUNCTION atomic_collect_tree_retirement_unconverted_v22(candidate_database_id text, candidate_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    manifest_lock_key BIGINT;
    build_lock_key BIGINT;
BEGIN
    IF octet_length(candidate_manifest_hash) <> 32
       OR older_than_millis < 0
       OR maximum_nodes < 1
       OR maximum_nodes > 4096 THEN
        RETURN FALSE;
    END IF;
    manifest_lock_key :=
        (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
        # 4707465863597391872::bigint;
    build_lock_key :=
        (('x' || encode(substring(candidate_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
        # 4707460391809056768::bigint;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(manifest_lock_key)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(build_lock_key) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (
        SELECT 1 FROM atomic_tree_build_intents intent
         WHERE intent.manifest_hash = candidate_manifest_hash
    ) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (
        SELECT 1
          FROM atomic_generation_request_bases base
          JOIN atomic_tree_publications publication
            ON publication.manifest_hash = base.base_manifest_hash
          JOIN atomic_heads head
            ON head.database_id = publication.database_id
           AND head.log_generation = publication.log_generation
         WHERE base.base_manifest_hash = candidate_manifest_hash
           AND publication.database_id = candidate_database_id
           AND publication.publication_revision = candidate_revision
    ) THEN
        RETURN FALSE;
    END IF;
    RETURN atomic_collect_tree_retirement_unbound_v13(
        candidate_database_id,
        candidate_revision,
        candidate_manifest_hash,
        older_than_millis,
        maximum_nodes
    );
END;
$$;

CREATE FUNCTION atomic_complete_excision_generation(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    activation_row atomic_log_generation_activations%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    candidate_revision BIGINT;
BEGIN
    SELECT * INTO activation_row
      FROM atomic_log_generation_activations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF activation_row.generation IS NULL OR generation_row.build_kind <> 1
       OR activation_row.manifest_hash IS DISTINCT FROM candidate_manifest_hash
       OR NOT EXISTS (
            SELECT 1 FROM atomic_log_generation_completion_stages
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
               AND phase = 2
       )
       OR NOT EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = candidate_database_id
               AND log_generation = candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic excision generation is not the active staged completion'
            USING ERRCODE = '40001';
    END IF;
    IF candidate_manifest_hash IS NOT NULL THEN
        SELECT publication_revision INTO candidate_revision
          FROM atomic_tree_manifests
         WHERE database_id = candidate_database_id
           AND log_generation = candidate_generation
           AND basis_t = activation_row.basis_t
           AND tx_hash = activation_row.head_hash
           AND state_hash = activation_row.state_hash
           AND manifest_hash = candidate_manifest_hash;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'Atomic excision completion has no staged authenticated tree'
                USING ERRCODE = '23503';
        END IF;

        IF NOT EXISTS (
            SELECT 1 FROM atomic_tree_publications
             WHERE database_id = candidate_database_id
               AND publication_revision = candidate_revision
               AND manifest_hash = candidate_manifest_hash
               AND log_generation = candidate_generation
        ) THEN
            INSERT INTO atomic_tree_publications
                   (database_id, publication_revision, basis_t, tx_hash,
                    manifest_hash, log_generation)
            VALUES (candidate_database_id, candidate_revision, activation_row.basis_t,
                    activation_row.head_hash, candidate_manifest_hash,
                    candidate_generation);
        END IF;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    INSERT INTO atomic_log_generation_completions(database_id, generation)
    VALUES (candidate_database_id, candidate_generation)
    ON CONFLICT DO NOTHING;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

CREATE FUNCTION atomic_complete_request_base_archive(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    archive atomic_request_base_archives%ROWTYPE;
    planned BIGINT;
    valid_nodes BIGINT;
    valid_roots BIGINT;
    actual_set_hash BYTEA;
BEGIN
    SELECT * INTO archive
      FROM atomic_request_base_archives
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND manifest_hash = candidate_manifest_hash
     FOR UPDATE;
    IF NOT FOUND
       OR NOT atomic_request_base_archive_build_live(
           candidate_database_id, candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic request-base archive lost its restore owner'
            USING ERRCODE = '55000';
    END IF;

    SELECT count(*),
           pg_catalog.sha256(
               convert_to('atomic/request-base-archive-node-set/v1', 'UTF8')
               || decode('00', 'hex')
               || int8send(count(*))
               || COALESCE(string_agg(node_hash, ''::bytea ORDER BY node_hash), ''::bytea)
           )
      INTO planned, actual_set_hash
      FROM atomic_request_base_archive_nodes
     WHERE manifest_hash = candidate_manifest_hash;
    SELECT count(*) INTO valid_nodes
      FROM atomic_request_base_archive_nodes planned
      JOIN atomic_tree_nodes stored ON stored.node_hash = planned.node_hash
     WHERE planned.manifest_hash = candidate_manifest_hash
       AND pg_catalog.sha256(stored.payload) = planned.node_hash;
    SELECT count(*) INTO valid_roots
      FROM atomic_request_base_archive_roots root
      JOIN atomic_tree_nodes stored ON stored.node_hash = root.root_hash
      JOIN atomic_request_base_archive_nodes planned
        ON planned.manifest_hash = root.manifest_hash
       AND planned.node_hash = root.root_hash
     WHERE root.manifest_hash = candidate_manifest_hash
       AND root.encoded_bytes = octet_length(stored.payload);
    IF planned <> archive.expected_node_count
       OR valid_nodes <> archive.expected_node_count
       OR actual_set_hash <> archive.node_set_hash
       OR valid_roots <> 8 THEN
        RAISE EXCEPTION 'Atomic request-base archive closure is incomplete or inconsistent'
            USING ERRCODE = '23514';
    END IF;
    INSERT INTO atomic_request_base_archive_completions(manifest_hash)
    VALUES (candidate_manifest_hash)
    ON CONFLICT DO NOTHING;
END;
$$;

CREATE FUNCTION atomic_content_envelope_version() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF octet_length(NEW.payload) < 58
       OR substring(NEW.payload FROM 1 FOR 4) <> decode('41544c43', 'hex') THEN
        RAISE EXCEPTION 'invalid transaction content header';
    END IF;
    NEW.envelope_version := (get_byte(NEW.payload, 4) * 256 + get_byte(NEW.payload, 5))::smallint;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_database_reclamation_authorized() RETURNS boolean
    LANGUAGE sql
    SET search_path FROM CURRENT
    AS $$
    SELECT current_user=pg_get_userbyid(c.relowner) AND EXISTS(
        SELECT 1 FROM atomic_database_reclamation_progress
         WHERE active_backend=pg_backend_pid() AND active_xid=txid_current())
      FROM pg_class c WHERE c.oid='atomic_database_reclamation_progress'::regclass
$$;

CREATE FUNCTION atomic_discover_remote_writer(requested_database text, requested_lineage text) RETURNS TABLE(holder_id text, lease_epoch bigint, instance_id bytea, network_address text, tls_server_name text, protocol_version integer)
    LANGUAGE sql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
    SELECT e.holder_id,e.lease_epoch,e.instance_id,e.network_address,e.tls_server_name,e.protocol_version
      FROM atomic_remote_writer_endpoints e JOIN atomic_databases d USING(database_id)
      JOIN atomic_transactor_leases l ON l.lease_scope=e.database_id
     WHERE e.database_id=requested_database AND e.lineage_id=requested_lineage
       AND d.lineage_id=e.lineage_id AND l.holder_id=e.holder_id
       AND l.epoch=e.lease_epoch AND l.expires_at>clock_timestamp()
$$;

CREATE FUNCTION atomic_enqueue_fulltext_source_pages(source bytea) RETURNS TABLE(pages_examined bigint, candidates_added bigint)
    LANGUAGE sql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
    WITH pages AS MATERIALIZED (
        SELECT p.block_hash FROM atomic_fulltext_pages p WHERE p.created_for=source
    ), added AS (
        INSERT INTO atomic_fulltext_page_garbage(block_hash)
        SELECT p.block_hash FROM pages p
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=source)
        ON CONFLICT DO NOTHING RETURNING 1
    ) SELECT (SELECT count(*) FROM pages),(SELECT count(*) FROM added)
$$;

CREATE FUNCTION atomic_fill_tree_retirement_generation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    SELECT log_generation INTO NEW.log_generation
      FROM atomic_tree_publications
     WHERE database_id = NEW.database_id
       AND publication_revision = NEW.publication_revision
       AND manifest_hash = NEW.manifest_hash;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree retirement has no matching publication'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_finish_fulltext_build(source bytea) RETURNS TABLE(pages_examined bigint, candidates_added bigint)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_fulltext_projections p
                    JOIN atomic_fulltext_page_roots r
                      ON r.manifest_hash=p.manifest_hash AND r.root_hash=p.root_hash
                   WHERE p.manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext build has no published shared root' USING ERRCODE='23503';
    END IF;
    DELETE FROM atomic_fulltext_page_builds WHERE manifest_hash=source;
    RETURN QUERY SELECT * FROM atomic_enqueue_fulltext_source_pages(source);
END;
$$;

CREATE FUNCTION atomic_finish_receipt_archive_conversion(candidate_hash bytea, older_than_millis bigint) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE work atomic_receipt_archive_conversions%ROWTYPE; retired BOOLEAN;
BEGIN
    IF older_than_millis IS NULL OR older_than_millis<0
       OR NOT atomic_receipt_archive_conversion_context(candidate_hash) THEN RETURN FALSE; END IF;
    SELECT * INTO work FROM atomic_receipt_archive_conversions
     WHERE manifest_hash=candidate_hash FOR UPDATE;
    IF NOT FOUND OR work.phase<>3 OR work.hashed_nodes<>work.node_count
       OR EXISTS (SELECT 1 FROM atomic_receipt_archive_frontier WHERE manifest_hash=candidate_hash)
       OR EXISTS (SELECT 1 FROM atomic_tree_retired_nodes
                   WHERE database_id=work.database_id AND publication_revision=work.publication_revision)
       OR NOT EXISTS (SELECT 1 FROM atomic_request_base_archives archive
                       WHERE archive.manifest_hash=candidate_hash
                         AND archive.expected_node_count=work.node_count
                         AND archive.node_set_hash<>decode(repeat('00',32),'hex')) THEN
        RETURN FALSE;
    END IF;
    -- The incremental owner checked every immutable node, edge, and ordered
    -- hash checkpoint. No final string_agg or full-closure authentication pass.
    INSERT INTO atomic_request_base_archive_completions(manifest_hash) VALUES(candidate_hash);
    -- Drop the source FK before v13 deletes its metadata; no frontier rows
    -- remain, and rollback restores both ownership records if retirement fails.
    DELETE FROM atomic_receipt_archive_conversions WHERE manifest_hash=candidate_hash;
    retired := atomic_collect_tree_retirement_unbound_v13(
        work.database_id,work.publication_revision,candidate_hash,older_than_millis,512);
    IF NOT retired THEN
        RAISE EXCEPTION 'Atomic receipt archive source changed during ownership handoff' USING ERRCODE='40001';
    END IF;
    RETURN TRUE;
END;
$$;

CREATE FUNCTION atomic_finish_removed_database_identity() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    DELETE FROM atomic_database_names WHERE database_id=OLD.database_id;
    UPDATE atomic_database_identities SET retired_at=coalesce(retired_at,clock_timestamp()),
        reclaimed_at=CASE WHEN EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=OLD.database_id)
            THEN reclaimed_at ELSE clock_timestamp() END WHERE database_id=OLD.database_id;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_finish_tree_build(candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_fulltext_garbage_candidates(maximum_blocks bigint) RETURNS TABLE(manifest_hash bytea, block_hash bytea)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF maximum_blocks<1 OR maximum_blocks>4096 THEN
        RAISE EXCEPTION 'Atomic fulltext garbage batch must be between 1 and 4096' USING ERRCODE='22023';
    END IF;
    IF NOT pg_try_advisory_xact_lock(atomic_fulltext_gc_pin_key()) THEN
        RAISE EXCEPTION 'Atomic fulltext build or repair is active' USING ERRCODE='55P03';
    END IF;
    RETURN QUERY
    SELECT candidate.source,candidate.hash FROM (
        SELECT b.manifest_hash AS source,b.block_hash AS hash FROM atomic_fulltext_garbage g
        JOIN atomic_fulltext_blocks b ON b.manifest_hash=g.manifest_hash
        WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_manifests m WHERE m.manifest_hash=b.manifest_hash)
        UNION ALL
        SELECT NULL::BYTEA AS source,p.block_hash AS hash FROM atomic_fulltext_page_garbage g
        JOIN atomic_fulltext_pages p ON p.block_hash=g.block_hash
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=p.created_for)
    ) candidate ORDER BY candidate.source NULLS FIRST,candidate.hash LIMIT maximum_blocks;
END;
$$;

CREATE FUNCTION atomic_fulltext_gc_pin_key() RETURNS bigint
    LANGUAGE sql STABLE PARALLEL SAFE
    SET search_path FROM CURRENT
    AS $$
    SELECT ('x'||substr(encode(pg_catalog.sha256(convert_to(
        'atomic/fulltext-page-gc/v1/'||'atomic_fulltext_pages'::regclass::oid::text,
        'UTF8')),'hex'),1,16))::bit(64)::bigint
$$;

CREATE FUNCTION atomic_heartbeat_tree_build(candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
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

CREATE FUNCTION atomic_log_generation_pin_key(candidate_database_id text, candidate_generation bigint) RETURNS bigint
    LANGUAGE sql STABLE SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
    SELECT pg_catalog.hashtextextended(
               'atomic/log-generation-pin/v2/' ||
               'atomic_databases'::regclass::oid::text || '/' ||
               lineage_id || '/' || candidate_generation::text,
               4707476001900298240::bigint
           )
      FROM atomic_databases
     WHERE database_id = candidate_database_id
       AND candidate_generation >= 0
$$;

CREATE FUNCTION atomic_mark_fulltext_garbage() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN NULL; END IF;
    IF EXISTS (SELECT 1 FROM atomic_fulltext_blocks WHERE manifest_hash=OLD.manifest_hash) THEN
        INSERT INTO atomic_fulltext_garbage(manifest_hash) VALUES(OLD.manifest_hash) ON CONFLICT DO NOTHING;
    END IF;
    RETURN OLD;
END;
$$;

CREATE FUNCTION atomic_prepare_database_reclamation(target text, lineage text, age_millis bigint, apply boolean) RETURNS TABLE(phase integer, complete boolean)
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE identity atomic_database_identities%ROWTYPE; owner NAME;
BEGIN
    SELECT pg_get_userbyid(relowner) INTO owner FROM pg_class WHERE oid='atomic_databases'::regclass;
    IF current_user<>owner THEN RAISE EXCEPTION 'Atomic terminal collection requires catalog ownership' USING ERRCODE='42501'; END IF;
    IF age_millis IS NULL OR age_millis<0 THEN RAISE EXCEPTION 'Atomic retirement age must be nonnegative' USING ERRCODE='22023'; END IF;
    -- Publication locks head then identity. Try-only acquisition preserves
    -- that order and never waits holding the reverse side of an in-flight write.
    PERFORM 1 FROM atomic_heads WHERE database_id=target FOR UPDATE NOWAIT;
    SELECT * INTO identity FROM atomic_database_identities WHERE database_id=target FOR UPDATE NOWAIT;
    IF NOT FOUND OR identity.lineage_id<>lineage THEN
        RAISE EXCEPTION 'Atomic retired database identity does not match target' USING ERRCODE='22023';
    END IF;
    IF identity.retired_at IS NULL THEN RAISE EXCEPTION 'Atomic active databases cannot be reclaimed' USING ERRCODE='55000'; END IF;
    IF identity.retired_at>clock_timestamp()-age_millis*interval '1 millisecond' THEN
        RAISE EXCEPTION 'Atomic database has not reached retirement age' USING ERRCODE='55P03';
    END IF;
    IF identity.reclaimed_at IS NOT NULL THEN RETURN QUERY SELECT 2147483647,TRUE; RETURN; END IF;
    IF EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=target AND format_version<>1) THEN
        RAISE EXCEPTION 'Atomic terminal collection progress version is unsupported' USING ERRCODE='55000';
    END IF;
    IF apply THEN
        INSERT INTO atomic_database_reclamation_progress(database_id,active_backend,active_xid)
        VALUES(target,pg_backend_pid(),txid_current()) ON CONFLICT(database_id)
        DO UPDATE SET active_backend=excluded.active_backend,active_xid=excluded.active_xid;
    END IF;
    RETURN QUERY SELECT coalesce((SELECT p.phase FROM atomic_database_reclamation_progress p WHERE p.database_id=target),0),FALSE;
END;
$$;

CREATE FUNCTION atomic_protect_reclaiming_database() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE target TEXT; object BYTEA; object_kind SMALLINT;
BEGIN
    IF atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    IF TG_ARGV[0]='database_id' THEN target:=OLD.database_id; END IF;
    IF TG_ARGV[0]='lease_scope' THEN target:=OLD.lease_scope; END IF;
    IF target IS NOT NULL AND EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=target) THEN
        RAISE EXCEPTION 'Atomic terminal database collection owns this metadata' USING ERRCODE='55P03';
    END IF;
    IF TG_ARGV[1]='manifest_hash' THEN
        object:=OLD.manifest_hash;
        IF EXISTS(SELECT 1 FROM atomic_database_reclamation_objects
                   WHERE kind IN(6,7,8) AND object_hash=object) THEN
            RAISE EXCEPTION 'Atomic terminal database collection owns this manifest' USING ERRCODE='55P03';
        END IF;
    END IF;
    object_kind:=CASE TG_TABLE_NAME WHEN 'atomic_tree_nodes' THEN 1 WHEN 'atomic_semantic_commitment_nodes' THEN 2
        WHEN 'atomic_fulltext_pages' THEN 3 WHEN 'atomic_index_segments' THEN 4 WHEN 'atomic_programs' THEN 5 ELSE NULL END;
    IF object_kind IS NOT NULL THEN
        IF object_kind IN(1,2) THEN object:=OLD.node_hash;
        ELSIF object_kind=3 THEN object:=OLD.block_hash;
        ELSIF object_kind=4 THEN object:=OLD.segment_hash;
        ELSE object:=OLD.program_hash; END IF;
        IF EXISTS(SELECT 1 FROM atomic_database_reclamation_objects WHERE kind=object_kind AND object_hash=object) THEN
            RAISE EXCEPTION 'Atomic terminal database collection owns this object frontier' USING ERRCODE='55P03';
        END IF;
    END IF;
    RETURN OLD;
END;
$$;

CREATE FUNCTION atomic_publish_tree(candidate_database_id text, candidate_revision bigint, candidate_basis bigint, candidate_tx_hash bytea, candidate_manifest_hash bytea) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    candidate_generation BIGINT;
BEGIN
    SELECT log_generation INTO candidate_generation
      FROM atomic_heads WHERE database_id = candidate_database_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication database does not exist'
            USING ERRCODE = '23503';
    END IF;
    INSERT INTO atomic_tree_publications
           (database_id, publication_revision, basis_t, tx_hash, manifest_hash,
            log_generation)
    VALUES (candidate_database_id, candidate_revision, candidate_basis,
            candidate_tx_hash, candidate_manifest_hash, candidate_generation);
END;
$$;

CREATE FUNCTION atomic_receipt_archive_conversion_context(candidate_hash bytea) RETURNS boolean
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE manifest_lock BIGINT; build_lock BIGINT; generation_lock BIGINT;
BEGIN
    IF candidate_hash IS NULL OR octet_length(candidate_hash)<>32 THEN RETURN FALSE; END IF;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RETURN FALSE;
    END IF;
    manifest_lock := (('x'||encode(substring(candidate_hash FROM 1 FOR 8),'hex'))::bit(64)::bigint)
                     # 4707465863597391872::bigint;
    build_lock := (('x'||encode(substring(candidate_hash FROM 1 FOR 8),'hex'))::bit(64)::bigint)
                  # 4707460391809056768::bigint;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(manifest_lock)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(build_lock) THEN RETURN FALSE; END IF;
    SELECT atomic_log_generation_pin_key(p.database_id,p.log_generation)
      INTO generation_lock FROM atomic_tree_publications p
      JOIN atomic_tree_retirements r USING(database_id,publication_revision,manifest_hash)
     WHERE p.manifest_hash=candidate_hash AND r.bookkeeping_complete
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications older
                        WHERE older.database_id=p.database_id
                          AND older.publication_revision<p.publication_revision)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress
                        WHERE progress.manifest_hash=candidate_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents intent
                        WHERE intent.manifest_hash=candidate_hash);
    IF generation_lock IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock_shared(generation_lock) THEN RETURN FALSE; END IF;
    PERFORM set_config('atomic.receipt_archive_conversion','v25',true);
    RETURN TRUE;
END;
$$;

CREATE FUNCTION atomic_reject_completed_request_base_archive_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM atomic_request_base_archive_completions complete
         WHERE complete.manifest_hash = NEW.manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic completed request-base archive is immutable'
            USING ERRCODE = '55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_reject_database_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND pg_catalog.current_setting('atomic.log_generation_gc', true) = 'v14'
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner
       AND NOT EXISTS (
            SELECT 1 FROM atomic_heads
             WHERE database_id = OLD.database_id
       )
       AND NOT EXISTS (
            SELECT 1 FROM atomic_log_generations
             WHERE database_id = OLD.database_id
       ) THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic database records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_fulltext_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF TG_OP='DELETE' AND current_user=relation_owner THEN RETURN OLD; END IF;
    RAISE EXCEPTION 'Atomic fulltext content is immutable' USING ERRCODE='55000';
END;
$$;

CREATE FUNCTION atomic_reject_generation_request_base_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF TG_OP = 'DELETE'
       AND current_user = relation_owner
       AND (
            pg_catalog.current_setting('atomic.tree_gc_active', true) = 'v13'
            OR pg_catalog.current_setting('atomic.log_generation_gc', true) = 'v14'
       ) THEN
        RETURN OLD;
    END IF;
    RAISE EXCEPTION 'Atomic request base bindings are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_generation_staging_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF TG_OP IN ('UPDATE', 'DELETE')
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Atomic generation staging records are immutable outside activation'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_immutable_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    RAISE EXCEPTION 'Atomic committed records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_log_generation_gc_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF pg_catalog.current_setting('atomic.log_generation_gc', true) = 'v14'
       AND current_user = relation_owner THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        IF TG_OP = 'UPDATE'
           AND TG_TABLE_NAME = 'atomic_log_generation_retirements'
           AND (to_jsonb(NEW) - 'collecting_at') =
               (to_jsonb(OLD) - 'collecting_at')
           AND OLD.collecting_at IS NULL
           AND NEW.collecting_at IS NOT NULL THEN
            RETURN NEW;
        END IF;
    END IF;
    RAISE EXCEPTION 'Atomic committed log-generation records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_program_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    RAISE EXCEPTION 'Atomic program blobs are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_request_base_archive_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF TG_OP='DELETE' AND current_user=relation_owner
       AND pg_catalog.current_setting('atomic.request_base_archive_gc',true)='v20' THEN
        RETURN OLD;
    END IF;
    IF TG_OP='UPDATE' AND TG_TABLE_NAME='atomic_request_base_archives'
       AND current_user=relation_owner
       AND pg_catalog.current_setting('atomic.receipt_archive_conversion',true)='v25'
       AND EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions work
                    WHERE work.manifest_hash=OLD.manifest_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_completions complete
                        WHERE complete.manifest_hash=OLD.manifest_hash)
       AND (to_jsonb(NEW)-'expected_node_count'-'node_set_hash')
          =(to_jsonb(OLD)-'expected_node_count'-'node_set_hash') THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Atomic request-base archive records are immutable' USING ERRCODE='55000';
END;
$$;

CREATE FUNCTION atomic_reject_segment_conflict() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NEW.payload <> OLD.payload THEN
        RAISE EXCEPTION 'Atomic segment hash is bound to different bytes'
            USING ERRCODE = '55000';
    END IF;
    RETURN OLD;
END;
$$;

CREATE FUNCTION atomic_reject_semantic_commitment_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
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

CREATE FUNCTION atomic_reject_tree_gc_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner)
      INTO relation_owner
      FROM pg_catalog.pg_class
     WHERE oid = TG_RELID;
    IF TG_OP IN ('UPDATE', 'DELETE')
       AND pg_catalog.current_setting('atomic.tree_gc_active', true) = 'v13'
       AND current_user = relation_owner THEN
        IF TG_OP = 'DELETE' THEN
            RETURN OLD;
        END IF;
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Atomic committed tree records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE FUNCTION atomic_reject_tree_node_block_mutation() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE relation_owner NAME;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    -- The owner can discard an optional bad projection. Canonical-node GC
    -- also removes it through the FK cascade; runtime writers cannot mutate it.
    IF TG_OP='DELETE' AND current_user=relation_owner THEN RETURN OLD; END IF;
    RAISE EXCEPTION 'Atomic compressed node representations are immutable'
        USING ERRCODE='55000';
END;
$$;

CREATE FUNCTION atomic_release_inactive_request_bases() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE relation_owner NAME;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_generation_request_bases
                    WHERE base_manifest_hash=OLD.manifest_hash) THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF current_user IS DISTINCT FROM relation_owner
       OR pg_catalog.current_setting('atomic.tree_gc_active',true) IS DISTINCT FROM 'v13' THEN
        RAISE EXCEPTION 'Atomic request base can be released only by tree garbage collection' USING ERRCODE='55000';
    END IF;
    IF pg_catalog.current_setting('atomic.receipt_archive_conversion',true)='v25'
       AND EXISTS (SELECT 1 FROM atomic_request_base_archives archive
                    JOIN atomic_request_base_archive_completions complete USING(manifest_hash)
                    WHERE archive.manifest_hash=OLD.manifest_hash
                      AND archive.database_id=OLD.database_id
                      AND archive.generation=OLD.log_generation) THEN RETURN OLD; END IF;
    IF EXISTS (SELECT 1 FROM atomic_heads WHERE database_id=OLD.database_id
                AND log_generation=OLD.log_generation) THEN
        RAISE EXCEPTION 'Atomic active request base cannot be retired' USING ERRCODE='55000';
    END IF;
    DELETE FROM atomic_generation_request_bases WHERE base_manifest_hash=OLD.manifest_hash;
    RETURN OLD;
END;
$$;

CREATE FUNCTION atomic_request_base_archive_build_live(candidate_database_id text, candidate_generation bigint) RETURNS boolean
    LANGUAGE sql STABLE SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
    SELECT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
          LEFT JOIN atomic_heads h ON h.database_id = g.database_id
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind IN (0, 2)
           AND NOT EXISTS (
               SELECT 1 FROM atomic_log_generation_activations a
                WHERE a.database_id = g.database_id
                  AND a.generation = g.generation
           )
           AND NOT EXISTS (
               SELECT 1 FROM atomic_log_generation_abandonment_progress p
                WHERE p.database_id = g.database_id
                  AND p.generation = g.generation
           )
           AND (
               (g.build_kind = 0
                AND b.source_generation IS NULL
                AND b.frozen_plan_hash IS NOT NULL
                AND h.database_id IS NULL)
               OR
               (g.build_kind = 2
                AND b.source_generation = h.log_generation
                AND b.captured_basis_t = h.basis_t
                AND b.captured_head_hash = h.tx_hash
                AND b.restore_manifest_hash IS NOT NULL
                AND b.restore_basis_t IS NOT NULL
                AND b.restore_head_hash IS NOT NULL)
           )
    )
$$;

CREATE FUNCTION atomic_require_active_publication() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE published_database TEXT;
BEGIN
    IF TG_TABLE_NAME='atomic_fulltext_projections' THEN
        SELECT database_id INTO published_database FROM atomic_tree_manifests WHERE manifest_hash=NEW.manifest_hash;
    ELSE
        published_database:=NEW.database_id;
    END IF;
    PERFORM 1 FROM atomic_database_identities
        WHERE database_id=published_database AND retired_at IS NULL FOR SHARE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic database identity is retired or unregistered' USING ERRCODE='55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_require_published_generation_transaction() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    -- Inactive content-first build rows are deliberately not visible. Once a
    -- generation is active, every later inserted transaction must publish.
    IF EXISTS (
        SELECT 1 FROM atomic_log_generation_activations
         WHERE database_id = NEW.database_id AND generation = NEW.generation
    ) AND NOT EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = NEW.database_id
           AND log_generation = NEW.generation
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic lineage transaction was not published by its commit'
            USING ERRCODE = '40001';
    END IF;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_require_published_transaction() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = NEW.database_id
           AND log_generation = 0
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic legacy transaction was not published by its commit'
            USING ERRCODE = '40001';
    END IF;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_retire_fulltext_build() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN NULL; END IF;
    -- Canonical retirement of an interrupted build has no explicit finish.
    -- A normal publication/discard performs its counted source pass itself.
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=OLD.manifest_hash) THEN
        PERFORM * FROM atomic_enqueue_fulltext_source_pages(OLD.manifest_hash);
    END IF;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_runtime_excision_step(candidate_database_id text, candidate_generation bigint, candidate_holder text, candidate_epoch bigint, candidate_action text, maximum_rows bigint, candidate_basis bigint, candidate_head bytea, candidate_state bytea, candidate_manifest bytea) RETURNS TABLE(rows_advanced bigint, is_complete boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    current_generation BIGINT;
    current_basis BIGINT;
    source_hash BYTEA;
BEGIN
    IF candidate_generation IS NULL OR candidate_generation<=0
       OR candidate_action IS NULL
       OR candidate_action NOT IN ('stage','activate','complete','cleanup')
       OR maximum_rows IS NULL OR maximum_rows<1 OR maximum_rows>4096 THEN
        RAISE EXCEPTION 'Invalid Atomic runtime excision step' USING ERRCODE='22023';
    END IF;
    PERFORM atomic_assert_excision_worker(candidate_database_id,candidate_holder,candidate_epoch);
    SELECT * INTO generation_row FROM atomic_log_generations
      WHERE database_id=candidate_database_id AND generation=candidate_generation;
    IF NOT FOUND OR generation_row.build_kind<>1 OR generation_row.request_count<=0 THEN
        RAISE EXCEPTION 'Atomic runtime maintenance requires a committed-request excision generation'
            USING ERRCODE='55000';
    END IF;
    SELECT log_generation,basis_t INTO current_generation,current_basis FROM atomic_heads
      WHERE database_id=candidate_database_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic excision worker has no active head' USING ERRCODE='55000';
    END IF;
    IF candidate_action IN ('stage','activate') THEN
        SELECT * INTO build_row FROM atomic_log_generation_builds
          WHERE database_id=candidate_database_id AND generation=candidate_generation;
        IF NOT FOUND OR build_row.source_generation IS NULL
           OR build_row.source_generation<>current_generation
           OR build_row.captured_basis_t>current_basis
           OR build_row.frozen_plan_hash IS NULL
           OR build_row.restore_manifest_hash IS NOT NULL
           OR build_row.restore_basis_t IS NOT NULL
           OR build_row.restore_head_hash IS NOT NULL
           OR EXISTS(SELECT 1 FROM atomic_log_generation_abandonment_progress
                     WHERE database_id=candidate_database_id AND generation=candidate_generation) THEN
            RAISE EXCEPTION 'Atomic excision source or frozen capture is not current'
                USING ERRCODE='55000';
        END IF;
        IF build_row.captured_basis_t=0 THEN
            SELECT genesis_hash INTO source_hash FROM atomic_databases
              WHERE database_id=candidate_database_id;
        ELSIF build_row.source_generation=0 THEN
            SELECT tx_hash INTO source_hash FROM atomic_transactions
              WHERE database_id=candidate_database_id AND basis_t=build_row.captured_basis_t;
        ELSE
            SELECT tx_hash INTO source_hash FROM atomic_generation_transactions
              WHERE database_id=candidate_database_id AND generation=build_row.source_generation
                AND basis_t=build_row.captured_basis_t;
        END IF;
        IF source_hash IS NULL OR source_hash<>build_row.captured_head_hash
           OR (SELECT count(*) FROM atomic_generation_excision_predicates
               WHERE database_id=candidate_database_id AND generation=candidate_generation)
              <>generation_row.request_count
           OR EXISTS(SELECT 1 FROM atomic_generation_excision_predicates
                     WHERE database_id=candidate_database_id AND generation=candidate_generation
                       AND request_t>build_row.captured_basis_t) THEN
            RAISE EXCEPTION 'Atomic excision capture has no matching source or complete frozen requests'
                USING ERRCODE='55000';
        END IF;
    ELSE
        IF (candidate_action='complete' AND current_generation<>candidate_generation)
           OR (candidate_action='cleanup' AND current_generation<>candidate_generation
               AND NOT EXISTS(SELECT 1 FROM atomic_log_generation_retirements
                   WHERE database_id=candidate_database_id AND generation=candidate_generation))
           OR NOT EXISTS(
            SELECT 1 FROM atomic_log_generation_activations
             WHERE database_id=candidate_database_id AND generation=candidate_generation
        ) THEN
            RAISE EXCEPTION 'Atomic excision completion requires its active generation'
                USING ERRCODE='55000';
        END IF;
    END IF;
    CASE candidate_action
        WHEN 'stage' THEN
            RETURN QUERY SELECT staged.rows_advanced,staged.is_sealed
              FROM atomic_stage_excision_completions(candidate_database_id,candidate_generation,maximum_rows) staged;
        WHEN 'activate' THEN
            IF candidate_basis IS NULL OR candidate_head IS NULL OR candidate_state IS NULL THEN
                RAISE EXCEPTION 'Atomic excision activation requires an exact checkpoint'
                    USING ERRCODE='22023';
            END IF;
            PERFORM atomic_activate_log_generation(candidate_database_id,candidate_generation,
                candidate_basis,candidate_head,candidate_state,candidate_manifest);
            RETURN QUERY SELECT 0::BIGINT,true;
        WHEN 'complete' THEN
            PERFORM atomic_complete_excision_generation(candidate_database_id,candidate_generation,candidate_manifest);
            RETURN QUERY SELECT 0::BIGINT,true;
        WHEN 'cleanup' THEN
            RETURN QUERY SELECT cleaned.rows_removed,cleaned.is_complete
              FROM atomic_cleanup_log_generation_build(candidate_database_id,candidate_generation,maximum_rows) cleaned;
    END CASE;
END;
$$;

CREATE FUNCTION atomic_scrub_excision_tree_predecessor() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14' THEN
        NEW.predecessor_manifest_hash := NULL;
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_seal_restore_completions(candidate_database_id text, candidate_generation bigint, expected_count bigint) RETURNS void
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF expected_count < 0 OR NOT EXISTS (
        SELECT 1
          FROM atomic_log_generation_completion_stages s
          JOIN atomic_log_generations g
            ON g.database_id = s.database_id AND g.generation = s.generation
          JOIN atomic_log_generation_builds b
            ON b.database_id = s.database_id AND b.generation = s.generation
         WHERE s.database_id = candidate_database_id
           AND s.generation = candidate_generation
           AND s.phase = 1
           AND s.row_count = expected_count
           AND g.build_kind IN (0, 2)
           AND (g.build_kind = 2 OR b.frozen_plan_hash IS NOT NULL)
    ) THEN
        RAISE EXCEPTION 'Atomic restore completion set is incomplete'
            USING ERRCODE = '23503';
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_log_generation_completion_stages
       SET phase = 2, sealed_at = clock_timestamp()
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
END;
$$;

CREATE FUNCTION atomic_semantic_commitment_gc_pin_key() RETURNS bigint
    LANGUAGE sql IMMUTABLE
    SET search_path FROM CURRENT
    AS $$
    SELECT pg_catalog.hashtextextended(
        'atomic/semantic-commitment-gc/v18',
        4707476001900298240::bigint
    )
$$;

CREATE FUNCTION atomic_stage_excision_completions(candidate_database_id text, candidate_generation bigint, maximum_rows bigint) RETURNS TABLE(rows_advanced bigint, is_sealed boolean)
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    generation_row atomic_log_generations%ROWTYPE;
    build_row atomic_log_generation_builds%ROWTYPE;
    stage_row atomic_log_generation_completion_stages%ROWTYPE;
    completion_row RECORD;
    processed BIGINT := 0;
    inserted_rows BIGINT := 0;
    inserted_one BIGINT;
BEGIN
    IF maximum_rows < 1 OR maximum_rows > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic completion staging boundary'
            USING ERRCODE = '22023';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    IF generation_row.generation IS NULL OR generation_row.build_kind <> 1
       OR build_row.generation IS NULL OR EXISTS (
            SELECT 1 FROM atomic_log_generation_activations
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
       ) THEN
        RAISE EXCEPTION 'Atomic completion staging requires an inactive excision build'
            USING ERRCODE = '55000';
    END IF;
    IF build_row.source_generation > 0 AND NOT EXISTS (
        SELECT 1 FROM atomic_log_generation_completions
         WHERE database_id = candidate_database_id
           AND generation = build_row.source_generation
    ) THEN
        RAISE EXCEPTION 'Atomic excision source has not completed its own publication'
            USING ERRCODE = '55000';
    END IF;

    INSERT INTO atomic_log_generation_completion_stages
           (database_id, generation, phase)
    VALUES (candidate_database_id, candidate_generation, 0)
    ON CONFLICT DO NOTHING;
    SELECT * INTO stage_row
      FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       FOR UPDATE;
    IF stage_row.phase = 2 THEN
        rows_advanced := 0;
        is_sealed := true;
        RETURN NEXT;
        RETURN;
    END IF;

    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    IF stage_row.phase = 0 THEN
        IF build_row.source_generation = 0 THEN
            UPDATE atomic_log_generation_completion_stages
               SET phase = 1, cursor_t = NULL, cursor_entity = NULL
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
        ELSE
            FOR completion_row IN
                SELECT request_t, request_entity
                  FROM atomic_completed_excision_requests
                 WHERE database_id = candidate_database_id
                   AND generation = build_row.source_generation
                   AND (stage_row.cursor_t IS NULL OR
                        (request_t, request_entity) >
                        (stage_row.cursor_t, stage_row.cursor_entity))
                 ORDER BY request_t, request_entity
                 LIMIT maximum_rows
            LOOP
                INSERT INTO atomic_completed_excision_requests
                       (database_id, request_t, request_entity, generation)
                VALUES (candidate_database_id, completion_row.request_t,
                        completion_row.request_entity, candidate_generation)
                ON CONFLICT DO NOTHING;
                GET DIAGNOSTICS inserted_one = ROW_COUNT;
                inserted_rows := inserted_rows + inserted_one;
                processed := processed + 1;
                stage_row.cursor_t := completion_row.request_t;
                stage_row.cursor_entity := completion_row.request_entity;
            END LOOP;
            UPDATE atomic_log_generation_completion_stages
               SET cursor_t = stage_row.cursor_t,
                   cursor_entity = stage_row.cursor_entity,
                   row_count = row_count + inserted_rows,
                   phase = CASE WHEN processed < maximum_rows THEN 1 ELSE 0 END
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation;
            IF processed < maximum_rows THEN
                UPDATE atomic_log_generation_completion_stages
                   SET cursor_t = NULL, cursor_entity = NULL
                 WHERE database_id = candidate_database_id
                   AND generation = candidate_generation;
            END IF;
        END IF;
    ELSE
        FOR completion_row IN
            SELECT request_t, request_entity
              FROM atomic_generation_excision_predicates
             WHERE database_id = candidate_database_id
               AND generation = candidate_generation
               AND (stage_row.cursor_t IS NULL OR
                    (request_t, request_entity) >
                    (stage_row.cursor_t, stage_row.cursor_entity))
             ORDER BY request_t, request_entity
             LIMIT maximum_rows
        LOOP
            INSERT INTO atomic_completed_excision_requests
                   (database_id, request_t, request_entity, generation)
            VALUES (candidate_database_id, completion_row.request_t,
                    completion_row.request_entity, candidate_generation)
            ON CONFLICT DO NOTHING;
            GET DIAGNOSTICS inserted_one = ROW_COUNT;
            inserted_rows := inserted_rows + inserted_one;
            processed := processed + 1;
            stage_row.cursor_t := completion_row.request_t;
            stage_row.cursor_entity := completion_row.request_entity;
        END LOOP;
        UPDATE atomic_log_generation_completion_stages
           SET cursor_t = stage_row.cursor_t,
               cursor_entity = stage_row.cursor_entity,
               row_count = row_count + inserted_rows,
               phase = CASE WHEN processed < maximum_rows THEN 2 ELSE 1 END,
               sealed_at = CASE WHEN processed < maximum_rows
                                THEN clock_timestamp() ELSE NULL END
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation;
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);

    SELECT phase = 2 INTO is_sealed
      FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    rows_advanced := processed;
    RETURN NEXT;
END;
$$;

CREATE FUNCTION atomic_stage_restore_completions(candidate_database_id text, candidate_generation bigint, request_ts bigint[], request_entities bigint[]) RETURNS bigint
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    input_count BIGINT;
    inserted_count BIGINT;
BEGIN
    input_count := cardinality(request_ts);
    IF input_count IS NULL OR input_count <> cardinality(request_entities)
       OR input_count > 4096 THEN
        RAISE EXCEPTION 'Invalid Atomic restore completion batch'
            USING ERRCODE = '22023';
    END IF;
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_log_generations g
          JOIN atomic_log_generation_builds b
            ON b.database_id = g.database_id AND b.generation = g.generation
         WHERE g.database_id = candidate_database_id
           AND g.generation = candidate_generation
           AND g.build_kind IN (0, 2)
           AND (g.build_kind = 2 OR b.frozen_plan_hash IS NOT NULL)
    ) OR EXISTS (
        SELECT 1 FROM atomic_log_generation_activations
         WHERE database_id = candidate_database_id
           AND generation = candidate_generation
    ) THEN
        RAISE EXCEPTION 'Atomic restore completion staging requires an inactive restore build'
            USING ERRCODE = '55000';
    END IF;
    INSERT INTO atomic_log_generation_completion_stages
           (database_id, generation, phase)
    VALUES (candidate_database_id, candidate_generation, 1)
    ON CONFLICT DO NOTHING;
    PERFORM 1 FROM atomic_log_generation_completion_stages
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation
       AND phase = 1
     FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic restore completion set is already sealed'
            USING ERRCODE = '55000';
    END IF;
    WITH supplied AS (
        SELECT request_t, request_entity
          FROM unnest(request_ts, request_entities)
               AS u(request_t, request_entity)
    ), inserted AS (
        INSERT INTO atomic_completed_excision_requests
               (database_id, request_t, request_entity, generation)
        SELECT candidate_database_id, request_t, request_entity,
               candidate_generation
          FROM supplied
         WHERE request_t > 0 AND request_entity >= 0
        ON CONFLICT DO NOTHING
        RETURNING 1
    )
    SELECT count(*) INTO inserted_count FROM inserted;
    IF inserted_count <> input_count AND EXISTS (
        SELECT 1 FROM unnest(request_ts, request_entities)
             AS u(request_t, request_entity)
         WHERE request_t <= 0 OR request_entity < 0
    ) THEN
        RAISE EXCEPTION 'Invalid Atomic restore completion identity'
            USING ERRCODE = '22023';
    END IF;
    PERFORM set_config('atomic.log_generation_activation', 'v14', true);
    UPDATE atomic_log_generation_completion_stages
       SET row_count = row_count + inserted_count
     WHERE database_id = candidate_database_id
       AND generation = candidate_generation;
    PERFORM set_config('atomic.log_generation_activation', 'off', true);
    RETURN inserted_count;
END;
$$;

CREATE FUNCTION atomic_track_fulltext_page_reference() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE page BYTEA;
BEGIN
    IF TG_OP = 'DELETE' AND atomic_database_reclamation_authorized() THEN RETURN NULL; END IF;
    IF TG_TABLE_NAME='atomic_fulltext_page_edges' THEN
        IF TG_OP='INSERT' THEN page:=NEW.child_hash; ELSE page:=OLD.child_hash; END IF;
    ELSE
        IF TG_OP='INSERT' THEN page:=NEW.root_hash; ELSE page:=OLD.root_hash; END IF;
    END IF;
    IF TG_OP='INSERT' THEN
        DELETE FROM atomic_fulltext_page_garbage WHERE block_hash=page;
    ELSE
        INSERT INTO atomic_fulltext_page_garbage(block_hash)
        SELECT p.block_hash FROM atomic_fulltext_pages p WHERE p.block_hash=page
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=page)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=page)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=p.created_for)
        ON CONFLICT DO NOTHING;
    END IF;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_track_program_candidate() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
BEGIN
    INSERT INTO atomic_program_gc_candidates (program_hash, candidate_at)
    VALUES (NEW.program_hash, NEW.created_at)
    ON CONFLICT (program_hash) DO NOTHING;
    RETURN NULL;
END;
$$;

CREATE FUNCTION atomic_tree_database_build_pin_key(candidate_database_id text) RETURNS bigint
    LANGUAGE plpgsql STABLE SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    candidate_lineage TEXT;
BEGIN
    SELECT lineage_id INTO candidate_lineage
      FROM atomic_databases
     WHERE database_id = candidate_database_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree build database does not exist'
            USING ERRCODE = '23503';
    END IF;
    RETURN hashtextextended('atomic/tree-build-db/' || candidate_lineage, 0);
END;
$$;

CREATE FUNCTION atomic_validate_activation_semantic_root() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_semantic_commitment_roots r
         WHERE r.database_id = NEW.database_id
           AND r.generation = NEW.generation
           AND r.basis_t = NEW.basis_t
           AND r.tx_hash = NEW.head_hash
           AND r.state_hash = NEW.state_hash
           AND r.commitment_version = 2
    ) THEN
        RAISE EXCEPTION 'Atomic generation activation requires its semantic root coordinate'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_fulltext_block_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=NEW.manifest_hash)
       OR EXISTS (SELECT 1 FROM atomic_tree_retirement_progress WHERE manifest_hash=NEW.manifest_hash) THEN
        RAISE EXCEPTION 'Atomic fulltext source manifest is unavailable' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_fulltext_page_source() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE source BYTEA;
BEGIN
    IF TG_TABLE_NAME='atomic_fulltext_pages' THEN
        source := NEW.created_for;
    ELSE
        source := NEW.manifest_hash;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=source)
       OR EXISTS (SELECT 1 FROM atomic_tree_retirement_progress WHERE manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext source manifest is unavailable' USING ERRCODE='23503';
    END IF;
    IF TG_TABLE_NAME='atomic_fulltext_pages'
       AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds WHERE manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext page upload has no build guard' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_fulltext_projection_root() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_fulltext_blocks
                    WHERE manifest_hash=NEW.manifest_hash AND block_hash=NEW.root_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots
                        WHERE manifest_hash=NEW.manifest_hash AND root_hash=NEW.root_hash) THEN
        RAISE EXCEPTION 'Atomic fulltext header has no retained source root' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_generation_checkpoint_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    build_row atomic_log_generation_builds%ROWTYPE;
    generation_row atomic_log_generations%ROWTYPE;
    stored_count BIGINT;
    terminal_hash BYTEA;
    terminal_state BYTEA;
    terminal_frontier BIGINT;
    source_hash BYTEA;
BEGIN
    SELECT * INTO build_row
      FROM atomic_log_generation_builds
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has no active build'
            USING ERRCODE = '23503';
    END IF;
    SELECT * INTO generation_row
      FROM atomic_log_generations
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND
       OR (generation_row.build_kind = 2
           AND NEW.through_basis_t <> build_row.restore_basis_t)
       OR (generation_row.build_kind = 0 AND build_row.source_generation IS NOT NULL)
       OR (generation_row.build_kind = 1 AND build_row.source_generation IS NULL)
       OR (generation_row.build_kind = 2
           AND (build_row.source_generation IS NULL
                OR build_row.restore_manifest_hash IS NULL)) THEN
        RAISE EXCEPTION 'Atomic generation checkpoint precedes its frozen capture'
            USING ERRCODE = '23514';
    END IF;
    SELECT count(*) INTO stored_count
      FROM atomic_generation_excision_predicates
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF stored_count <> generation_row.request_count THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has an incomplete request set'
            USING ERRCODE = '23503';
    END IF;
    SELECT count(*) INTO stored_count
      FROM atomic_generation_transactions
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation
       AND basis_t <= NEW.through_basis_t;
    IF stored_count <> NEW.through_basis_t THEN
        RAISE EXCEPTION 'Atomic generation checkpoint has a noncontiguous transaction prefix'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.through_basis_t = 0 THEN
        SELECT genesis_hash INTO terminal_hash
          FROM atomic_databases WHERE database_id = NEW.database_id;
        terminal_state := NEW.state_hash;
        terminal_frontier := NEW.eidx_frontier;
    ELSE
        SELECT t.tx_hash, t.state_hash, t.eidx_frontier
          INTO terminal_hash, terminal_state, terminal_frontier
          FROM atomic_generation_transactions t
         WHERE t.database_id = NEW.database_id
           AND t.generation = NEW.generation
           AND t.basis_t = NEW.through_basis_t;
    END IF;
    IF terminal_hash IS NULL OR terminal_hash <> NEW.head_hash
                 OR terminal_state <> NEW.state_hash
                 OR terminal_frontier <> NEW.eidx_frontier THEN
        RAISE EXCEPTION 'Atomic generation checkpoint disagrees with its terminal transaction'
            USING ERRCODE = '23503';
    END IF;

    IF generation_row.build_kind = 0 THEN
        IF NEW.source_head_hash IS NOT NULL THEN
            RAISE EXCEPTION 'Initial Atomic generation cannot claim a source head'
                USING ERRCODE = '23514';
        END IF;
    ELSIF generation_row.build_kind = 1 THEN
        IF build_row.source_generation = 0 THEN
            IF NEW.through_basis_t = 0 THEN
                SELECT genesis_hash INTO source_hash
                  FROM atomic_databases WHERE database_id = NEW.database_id;
            ELSE
                SELECT tx_hash INTO source_hash
                  FROM atomic_transactions
                 WHERE database_id = NEW.database_id
                   AND basis_t = NEW.through_basis_t;
            END IF;
        ELSE
            IF NEW.through_basis_t = 0 THEN
                SELECT genesis_hash INTO source_hash
                  FROM atomic_databases WHERE database_id = NEW.database_id;
            ELSE
                SELECT tx_hash INTO source_hash
                  FROM atomic_generation_transactions
                 WHERE database_id = NEW.database_id
                   AND generation = build_row.source_generation
                   AND basis_t = NEW.through_basis_t;
            END IF;
        END IF;
        IF source_hash IS NULL OR source_hash <> NEW.source_head_hash THEN
            RAISE EXCEPTION 'Atomic generation checkpoint disagrees with its source prefix'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        -- Restore rows were authenticated against the portable manifest by
        -- Rust before staging. They intentionally need not exist on the
        -- target's current branch or at its current basis.
        IF NEW.source_head_hash <> build_row.restore_head_hash THEN
            RAISE EXCEPTION 'Atomic restore checkpoint disagrees with its portable source root'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_generation_request_base_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    lock_key BIGINT;
BEGIN
    lock_key := (('x' || encode(substring(NEW.base_manifest_hash FROM 1 FOR 8), 'hex'))::bit(64)::bigint)
                # 4707465863597391872::bigint;
    PERFORM pg_catalog.pg_advisory_xact_lock_shared(lock_key);

    IF NOT EXISTS (
        SELECT 1
          FROM atomic_generation_requests request
         WHERE request.database_id = NEW.database_id
           AND request.generation = NEW.generation
           AND request.request_key_hash = NEW.request_key_hash
           AND request.request_kind = 2
           AND (
               EXISTS (
                   SELECT 1
                     FROM atomic_tree_publications publication
                     JOIN atomic_tree_manifests manifest
                       ON manifest.manifest_hash = publication.manifest_hash
                      AND manifest.database_id = publication.database_id
                      AND manifest.publication_revision = publication.publication_revision
                      AND manifest.basis_t = publication.basis_t
                      AND manifest.tx_hash = publication.tx_hash
                      AND manifest.log_generation = publication.log_generation
                     JOIN atomic_semantic_commitment_roots semantic
                       ON semantic.database_id = manifest.database_id
                      AND semantic.generation = manifest.log_generation
                      AND semantic.basis_t = manifest.basis_t
                      AND semantic.tx_hash = manifest.tx_hash
                      AND semantic.state_hash = manifest.state_hash
                      AND semantic.eidx_frontier = manifest.eidx_frontier
                      AND semantic.commitment_version = 2
                    WHERE publication.manifest_hash = NEW.base_manifest_hash
                      AND publication.database_id = request.database_id
                      AND publication.log_generation = request.generation
                      AND publication.basis_t <= request.basis_t - 1
                      AND manifest.manifest_version IN (4, 5, 6)
                      AND NOT EXISTS (
                          SELECT 1 FROM atomic_tree_retirement_progress progress
                           WHERE progress.manifest_hash = publication.manifest_hash
                      )
               )
               OR EXISTS (
                   SELECT 1
                     FROM atomic_request_base_archives archive
                     JOIN atomic_request_base_archive_completions complete
                       ON complete.manifest_hash = archive.manifest_hash
                     JOIN atomic_semantic_commitment_roots semantic
                       ON semantic.database_id = archive.database_id
                      AND semantic.generation = archive.generation
                      AND semantic.basis_t = archive.basis_t
                      AND semantic.tx_hash = archive.tx_hash
                      AND semantic.state_hash = archive.state_hash
                      AND semantic.eidx_frontier = archive.eidx_frontier
                      AND semantic.commitment_version = 2
                    WHERE archive.manifest_hash = NEW.base_manifest_hash
                      AND archive.database_id = request.database_id
                      AND archive.generation = request.generation
                      AND archive.basis_t <= request.basis_t - 1
                      AND archive.manifest_version IN (4, 5, 6)
                      AND (atomic_request_base_archive_build_live(
                               archive.database_id, archive.generation
                           ) OR EXISTS (
                               SELECT 1 FROM atomic_heads head
                                WHERE head.database_id = archive.database_id
                                  AND head.log_generation = archive.generation
                           ))
               )
           )
    ) THEN
        RAISE EXCEPTION 'Atomic native request base is absent, late, retired, or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_generation_request_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.generation
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic generation request does not identify its transaction'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_generation_transaction_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    build_lineage TEXT;
    expected_previous BYTEA;
BEGIN
    SELECT lineage_id INTO build_lineage
      FROM atomic_log_generations
     WHERE database_id = NEW.database_id
       AND generation = NEW.generation;
    IF NOT FOUND OR NOT EXISTS (
        SELECT 1 FROM atomic_transaction_contents c
         WHERE c.content_hash = NEW.content_hash
           AND c.lineage_id = build_lineage
           AND c.basis_t = NEW.basis_t
           AND c.eidx_frontier = NEW.eidx_frontier
    ) THEN
        RAISE EXCEPTION 'Atomic generation transaction has no matching lineage build'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.basis_t = 1 THEN
        SELECT genesis_hash INTO expected_previous
          FROM atomic_databases
         WHERE database_id = NEW.database_id;
    ELSE
        SELECT tx_hash INTO expected_previous
          FROM atomic_generation_transactions
         WHERE database_id = NEW.database_id
           AND generation = NEW.generation
           AND basis_t = NEW.basis_t - 1;
    END IF;
    IF NOT FOUND OR NEW.previous_hash <> expected_previous THEN
        RAISE EXCEPTION 'Atomic generation transaction does not extend its immutable prefix'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_head_advance() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    relation_owner NAME;
BEGIN
    IF NEW.database_id <> OLD.database_id THEN
        RAISE EXCEPTION 'Atomic head database identity is immutable'
            USING ERRCODE = '55000';
    END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF NEW.log_generation <> OLD.log_generation THEN
        IF pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
           AND current_user = relation_owner THEN
            RETURN NEW;
        END IF;
        RAISE EXCEPTION 'Atomic log generation can change only through verified activation'
            USING ERRCODE = '55000';
    END IF;
    IF NEW.basis_t <> OLD.basis_t + 1 THEN
        RAISE EXCEPTION 'Atomic head must advance by exactly one basis'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.log_generation = 0 THEN
        IF NOT EXISTS (
            SELECT 1 FROM atomic_transactions
             WHERE database_id = NEW.database_id
               AND basis_t = NEW.basis_t
               AND previous_hash = OLD.tx_hash
               AND tx_hash = NEW.tx_hash
        ) OR NOT EXISTS (
            SELECT 1 FROM atomic_requests
             WHERE database_id = NEW.database_id
               AND basis_t = NEW.basis_t
               AND tx_hash = NEW.tx_hash
        ) THEN
            RAISE EXCEPTION 'Atomic head does not identify a complete legacy transaction publication'
                USING ERRCODE = '23503';
        END IF;
    ELSE
        IF NOT EXISTS (
            SELECT 1 FROM atomic_generation_transactions
             WHERE database_id = NEW.database_id
               AND generation = NEW.log_generation
               AND basis_t = NEW.basis_t
               AND previous_hash = OLD.tx_hash
               AND tx_hash = NEW.tx_hash
        ) OR NOT EXISTS (
            SELECT 1
              FROM atomic_generation_requests request
             WHERE request.database_id = NEW.database_id
               AND request.generation = NEW.log_generation
               AND request.basis_t = NEW.basis_t
               AND request.tx_hash = NEW.tx_hash
               AND (
                    request.request_kind IN (0, 1)
                    OR (
                        request.request_kind = 2
                        AND EXISTS (
                            SELECT 1
                              FROM atomic_generation_request_bases base
                             WHERE base.database_id = request.database_id
                               AND base.generation = request.generation
                               AND base.request_key_hash = request.request_key_hash
                        )
                    )
               )
        ) THEN
            RAISE EXCEPTION 'Atomic head does not identify a complete lineage transaction publication'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_head_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    expected_hash BYTEA;
    relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid = TG_RELID;
    IF NEW.log_generation > 0
       AND pg_catalog.current_setting('atomic.log_generation_activation', true) = 'v14'
       AND current_user = relation_owner
       AND EXISTS (
            SELECT 1
              FROM atomic_log_generations g
              JOIN atomic_log_generation_builds b
                ON b.database_id = g.database_id AND b.generation = g.generation
              JOIN atomic_log_generation_checkpoints c
                ON c.database_id = g.database_id AND c.generation = g.generation
             WHERE g.database_id = NEW.database_id
               AND g.generation = NEW.log_generation
               AND g.build_kind = 0
               AND c.through_basis_t = NEW.basis_t
               AND c.head_hash = NEW.tx_hash
       ) THEN
        RETURN NEW;
    END IF;
    IF NEW.basis_t <> 0 THEN
        RAISE EXCEPTION 'Atomic initial head must be basis zero'
            USING ERRCODE = '23514';
    END IF;
    SELECT genesis_hash INTO expected_hash
      FROM atomic_databases
     WHERE database_id = NEW.database_id;
    IF NOT FOUND OR NEW.tx_hash <> expected_hash THEN
        RAISE EXCEPTION 'Atomic basis-zero head must identify database genesis'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_index_publication() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_transactions t
         WHERE t.database_id = NEW.database_id
           AND t.basis_t = NEW.basis_t
           AND t.tx_hash = NEW.tx_hash
           AND t.state_hash <> decode(repeat('00', 32), 'hex')
    ) OR NOT EXISTS (
        SELECT 1
          FROM atomic_index_manifests m
         WHERE m.database_id = NEW.database_id
           AND m.basis_t = NEW.basis_t
           AND m.tx_hash = NEW.tx_hash
           AND m.manifest_hash = NEW.manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic index publication must bind one committed state and manifest'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_remote_writer_endpoint() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_databases d WHERE d.database_id=NEW.database_id AND d.lineage_id=NEW.lineage_id) THEN
        RAISE EXCEPTION 'Atomic remote endpoint lineage mismatch' USING ERRCODE='23514';
    END IF;
    PERFORM 1 FROM atomic_transactor_leases l
      WHERE l.lease_scope=NEW.database_id AND l.holder_id=NEW.holder_id
        AND l.epoch=NEW.lease_epoch AND l.expires_at>clock_timestamp() FOR SHARE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic remote endpoint lease is not current' USING ERRCODE='55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_request_base_archive_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE conversion_owned BOOLEAN;
BEGIN
    SELECT current_user = pg_catalog.pg_get_userbyid(c.relowner)
       AND pg_catalog.current_setting('atomic.receipt_archive_conversion', true) = 'v25'
       AND EXISTS (
            SELECT 1 FROM atomic_receipt_archive_conversions work
            JOIN atomic_tree_manifests source ON source.manifest_hash=work.manifest_hash
            WHERE work.manifest_hash=NEW.manifest_hash
              AND work.database_id=NEW.database_id AND work.generation=NEW.generation
              AND source.payload=NEW.payload
              AND source.publication_revision=NEW.archive_revision
       ) INTO conversion_owned
      FROM pg_catalog.pg_class c WHERE c.oid=TG_RELID;
    IF pg_catalog.sha256(NEW.payload) <> NEW.manifest_hash
       OR (atomic_request_base_archive_build_live(NEW.database_id,NEW.generation)
               OR conversion_owned) IS NOT TRUE
       OR NOT EXISTS (
           SELECT 1 FROM atomic_semantic_commitment_roots semantic
            WHERE semantic.database_id=NEW.database_id
              AND semantic.generation=NEW.generation AND semantic.basis_t=NEW.basis_t
              AND semantic.tx_hash=NEW.tx_hash AND semantic.state_hash=NEW.state_hash
              AND semantic.eidx_frontier=NEW.eidx_frontier
              AND semantic.commitment_version=2
       ) THEN
        RAISE EXCEPTION 'Atomic request-base archive has no authenticated restore or conversion owner'
            USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_request_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_transactions
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic request does not identify its transaction'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_semantic_commitment_root() RETURNS trigger
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

CREATE FUNCTION atomic_validate_transaction_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    current_basis BIGINT;
    current_hash BYTEA;
    current_generation BIGINT;
BEGIN
    SELECT basis_t, tx_hash, log_generation
      INTO current_basis, current_hash, current_generation
      FROM atomic_heads WHERE database_id = NEW.database_id;
    IF NOT FOUND OR current_generation <> 0 THEN
        RAISE EXCEPTION 'Legacy Atomic transactions require the active generation-zero head'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.basis_t <> current_basis + 1 OR NEW.previous_hash <> current_hash THEN
        RAISE EXCEPTION 'Atomic transaction does not extend the current head'
            USING ERRCODE = '40001';
    END IF;
    IF NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'New Atomic transactions require a state commitment'
            USING ERRCODE = '55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_tree_index_basis_progress() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    candidate_index_basis BIGINT;
    current_index_basis BIGINT;
    current_generation BIGINT;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = NEW.database_id
       FOR UPDATE;
    IF NOT FOUND THEN
        RETURN NEW;
    END IF;

    SELECT manifest.index_basis_t INTO candidate_index_basis
      FROM atomic_tree_manifests manifest
     WHERE manifest.database_id = NEW.database_id
       AND manifest.publication_revision = NEW.publication_revision
       AND manifest.basis_t = NEW.basis_t
       AND manifest.tx_hash = NEW.tx_hash
       AND manifest.manifest_hash = NEW.manifest_hash
       AND manifest.log_generation = NEW.log_generation;
    IF NOT FOUND THEN
        RETURN NEW;
    END IF;

    SELECT publication.log_generation, manifest.index_basis_t
      INTO current_generation, current_index_basis
      FROM atomic_tree_publications publication
      JOIN atomic_tree_manifests manifest
        ON manifest.manifest_hash = publication.manifest_hash
       AND manifest.database_id = publication.database_id
       AND manifest.publication_revision = publication.publication_revision
       AND manifest.basis_t = publication.basis_t
       AND manifest.tx_hash = publication.tx_hash
       AND manifest.log_generation = publication.log_generation
     WHERE publication.database_id = NEW.database_id
     ORDER BY publication.publication_revision DESC
     LIMIT 1;
    IF FOUND AND current_generation = NEW.log_generation
       AND current_index_basis IS NOT NULL
       AND (candidate_index_basis IS NULL
            OR candidate_index_basis < current_index_basis) THEN
        RAISE EXCEPTION 'Atomic tree publication index basis cannot regress'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_tree_manifest_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    authoritative_state BYTEA;
    durable_lineage TEXT;
BEGIN
    SELECT lineage_id INTO durable_lineage
      FROM atomic_databases WHERE database_id = NEW.database_id;
    IF NOT FOUND OR NEW.excision_generation <> NEW.log_generation THEN
        RAISE EXCEPTION 'Atomic tree manifest has invalid generation metadata'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.log_generation = 0 THEN
        -- Alias-bound generation zero has no transaction row at genesis and
        -- therefore retains its historical positive-basis limitation.
        IF NEW.basis_t = 0 THEN
            RAISE EXCEPTION 'Legacy Atomic tree manifest requires a positive basis'
                USING ERRCODE = '23503';
        END IF;
        SELECT state_hash INTO authoritative_state
          FROM atomic_transactions
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash;
        IF NEW.lineage_id IS NOT NULL THEN
            RAISE EXCEPTION 'Legacy Atomic tree manifest cannot claim lineage encoding'
                USING ERRCODE = '23514';
        END IF;
    ELSE
        IF NEW.basis_t = 0 THEN
            SELECT r.state_hash INTO authoritative_state
              FROM atomic_semantic_commitment_roots r
              JOIN atomic_databases d ON d.database_id = r.database_id
             WHERE r.database_id = NEW.database_id
               AND r.generation = NEW.log_generation
               AND r.basis_t = 0
               AND r.tx_hash = NEW.tx_hash
               AND r.tx_hash = d.genesis_hash
               AND r.state_hash = NEW.state_hash
               AND r.eidx_frontier = NEW.eidx_frontier
               AND r.commitment_version = 2;
        ELSE
            SELECT state_hash INTO authoritative_state
              FROM atomic_generation_transactions
             WHERE database_id = NEW.database_id
               AND generation = NEW.log_generation
               AND basis_t = NEW.basis_t
               AND tx_hash = NEW.tx_hash;
        END IF;
        IF NEW.lineage_id IS DISTINCT FROM durable_lineage OR NOT (
            EXISTS (
                SELECT 1 FROM atomic_heads h
                 WHERE h.database_id = NEW.database_id
                   AND h.log_generation = NEW.log_generation
                   AND h.basis_t >= NEW.basis_t
            )
            OR EXISTS (
                SELECT 1 FROM atomic_log_generation_checkpoints c
                 WHERE c.database_id = NEW.database_id
                   AND c.generation = NEW.log_generation
                   AND c.through_basis_t = NEW.basis_t
                   AND c.head_hash = NEW.tx_hash
                   AND c.state_hash = NEW.state_hash
            )
        ) THEN
            RAISE EXCEPTION 'Atomic tree manifest does not identify a complete lineage build'
                USING ERRCODE = '23503';
        END IF;
    END IF;
    IF authoritative_state IS NULL OR authoritative_state <> NEW.state_hash
       OR NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'Atomic tree manifest does not identify one committed state'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_tree_node_block_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE canonical_length BIGINT; canonical_hash BYTEA;
BEGIN
    SELECT octet_length(payload), pg_catalog.sha256(payload)
      INTO canonical_length, canonical_hash
      FROM atomic_tree_nodes WHERE node_hash=NEW.node_hash;
    IF NOT FOUND OR canonical_hash<>NEW.node_hash
       OR canonical_length<>NEW.canonical_bytes
       OR pg_catalog.sha256(NEW.physical_payload)<>NEW.physical_hash THEN
        RAISE EXCEPTION 'Atomic compressed node has no matching canonical content or physical fingerprint'
            USING ERRCODE='23503';
    END IF;
    -- SQL deliberately does not decode gzip. Native readers must authenticate
    -- the decoded bytes against the requested canonical hash on every load.
    RETURN NEW;
END;
$$;

CREATE FUNCTION atomic_validate_tree_publication_delta() RETURNS trigger
    LANGUAGE plpgsql SECURITY DEFINER
    SET search_path FROM CURRENT
    AS $$
DECLARE
    mode SMALLINT;
    predecessor BYTEA;
    expected_nodes BIGINT;
    staged_nodes BIGINT;
    added_nodes BIGINT;
    added_hash BYTEA;
    work_state SMALLINT;
    current_hash BYTEA;
    current_complete BOOLEAN;
    covered_roots BIGINT;
BEGIN
    SELECT delta_mode, predecessor_manifest_hash, expected_node_count,
           staged_node_count, added_node_count, added_set_hash, delta_state
      INTO STRICT mode, predecessor, expected_nodes, staged_nodes,
                  added_nodes, added_hash, work_state
      FROM atomic_tree_delta_headers
     WHERE manifest_hash = NEW.manifest_hash;

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
        IF expected_nodes <> 0 OR staged_nodes <> 0 OR added_nodes <> 0
           OR work_state <> 1 THEN
            RAISE EXCEPTION 'Unknown Atomic tree delta cannot claim node changes'
                USING ERRCODE = '23514';
        END IF;
        RETURN NEW;
    END IF;

    IF work_state <> 1 OR staged_nodes <> expected_nodes OR NOT EXISTS (
        SELECT 1 FROM atomic_tree_build_intents i
         WHERE i.manifest_hash = NEW.manifest_hash
           AND i.database_id = NEW.database_id
           AND i.expected_revision = NEW.publication_revision - 1
           AND i.log_generation = (
               SELECT m.excision_generation FROM atomic_tree_manifests m
                WHERE m.manifest_hash = NEW.manifest_hash
           )
           AND i.intent_state = 1
           AND i.staged_node_count = i.expected_node_count
           AND i.expected_node_count = added_nodes
           AND i.node_set_hash = added_hash
    ) THEN
        RAISE EXCEPTION 'Atomic tree publication lacks its sealed pre-upload work'
            USING ERRCODE = '23514';
    END IF;

    IF current_hash IS NOT NULL AND NOT current_complete THEN
        RAISE EXCEPTION 'Previous Atomic tree publication work is incomplete'
            USING ERRCODE = '55000';
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

CREATE FUNCTION atomic_validate_tree_publication_insert() RETURNS trigger
    LANGUAGE plpgsql
    SET search_path FROM CURRENT
    AS $$
DECLARE
    current_revision BIGINT;
    current_basis BIGINT;
    current_publication_generation BIGINT;
    valid_roots BIGINT;
    head_generation BIGINT;
    head_basis BIGINT;
    head_hash BYTEA;
    relation_owner NAME;
BEGIN
    PERFORM database_id
      FROM atomic_databases
     WHERE database_id = NEW.database_id
       FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic tree publication database does not exist'
            USING ERRCODE = '23503';
    END IF;

    SELECT publication_revision, basis_t, log_generation
      INTO current_revision, current_basis, current_publication_generation
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
        IF NEW.log_generation = current_publication_generation
           AND NEW.basis_t < current_basis THEN
            SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
              FROM pg_catalog.pg_class WHERE oid = TG_RELID;
            IF pg_catalog.current_setting('atomic.log_generation_activation', true) <> 'v14'
               OR current_user <> relation_owner THEN
                RAISE EXCEPTION 'Atomic tree publication basis cannot regress'
                    USING ERRCODE = '40001';
            END IF;
        END IF;
    ELSIF NEW.publication_revision <> 1 THEN
        RAISE EXCEPTION 'First Atomic tree publication must use revision one'
            USING ERRCODE = '40001';
    END IF;

    SELECT log_generation, basis_t, tx_hash
      INTO head_generation, head_basis, head_hash
      FROM atomic_heads WHERE database_id = NEW.database_id;
    IF NOT FOUND OR head_generation <> NEW.log_generation
                 OR head_basis < NEW.basis_t
                 OR (head_basis = NEW.basis_t AND head_hash <> NEW.tx_hash) THEN
        RAISE EXCEPTION 'Atomic tree publication is not in the active log generation'
            USING ERRCODE = '40001';
    END IF;

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
        SELECT 1 FROM atomic_tree_manifests m
         WHERE m.database_id = NEW.database_id
           AND m.publication_revision = NEW.publication_revision
           AND m.basis_t = NEW.basis_t
           AND m.tx_hash = NEW.tx_hash
           AND m.manifest_hash = NEW.manifest_hash
           AND m.log_generation = NEW.log_generation
    ) THEN
        RAISE EXCEPTION 'Atomic tree publication is stale or unauthenticated'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE TABLE atomic_change_checkpoints (
    database_id text NOT NULL,
    consumer_name text NOT NULL,
    checkpoint_owner name DEFAULT CURRENT_USER NOT NULL,
    lineage_id text NOT NULL,
    generation bigint NOT NULL,
    last_t bigint NOT NULL,
    commit_hash bytea NOT NULL,
    revision bigint NOT NULL,
    CONSTRAINT atomic_change_checkpoints_commit_hash_check CHECK ((octet_length(commit_hash) = 32)),
    CONSTRAINT atomic_change_checkpoints_consumer_name_check CHECK (((octet_length(consumer_name) >= 1) AND (octet_length(consumer_name) <= 512))),
    CONSTRAINT atomic_change_checkpoints_generation_check CHECK ((generation >= 0)),
    CONSTRAINT atomic_change_checkpoints_last_t_check CHECK ((last_t >= 0)),
    CONSTRAINT atomic_change_checkpoints_revision_check CHECK ((revision >= 0))
);

CREATE TABLE atomic_completed_excision_requests (
    database_id text NOT NULL,
    request_t bigint NOT NULL,
    request_entity bigint NOT NULL,
    generation bigint NOT NULL,
    completed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_completed_excision_requests_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_completed_excision_requests_request_entity_check CHECK ((request_entity >= 0)),
    CONSTRAINT atomic_completed_excision_requests_request_t_check CHECK ((request_t > 0))
);

CREATE TABLE atomic_heads (
    database_id text NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    log_generation bigint DEFAULT 0 NOT NULL,
    CONSTRAINT atomic_heads_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_heads_log_generation_check CHECK ((log_generation >= 0)),
    CONSTRAINT atomic_heads_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE VIEW atomic_database_generations AS
 SELECT atomic_heads.database_id,
    atomic_heads.log_generation AS excision_generation
   FROM atomic_heads;

CREATE TABLE atomic_database_identities (
    database_id text NOT NULL,
    lineage_id text NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    retired_at timestamp with time zone,
    reclaimed_at timestamp with time zone,
    CONSTRAINT atomic_database_identities_database_id_check CHECK ((database_id <> ''::text))
);

CREATE TABLE atomic_database_names (
    name text NOT NULL,
    database_id text NOT NULL,
    CONSTRAINT atomic_database_names_name_check CHECK ((name <> ''::text))
);

CREATE TABLE atomic_database_reclamation_objects (
    database_id text NOT NULL,
    kind smallint NOT NULL,
    object_hash bytea NOT NULL,
    next_child bigint DEFAULT 0 NOT NULL,
    expanded boolean DEFAULT false NOT NULL,
    CONSTRAINT atomic_database_reclamation_objects_kind_check CHECK (((kind >= 1) AND (kind <= 8))),
    CONSTRAINT atomic_database_reclamation_objects_next_child_check CHECK ((next_child >= 0)),
    CONSTRAINT atomic_database_reclamation_objects_object_hash_check CHECK ((octet_length(object_hash) = 32))
);

CREATE TABLE atomic_database_reclamation_progress (
    database_id text NOT NULL,
    phase integer DEFAULT 0 NOT NULL,
    format_version integer DEFAULT 1 NOT NULL,
    active_backend integer,
    active_xid bigint,
    started_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_database_reclamation_progress_format_version_check CHECK ((format_version = 1)),
    CONSTRAINT atomic_database_reclamation_progress_phase_check CHECK ((phase >= 0))
);

CREATE TABLE atomic_databases (
    database_id text NOT NULL,
    genesis bytea NOT NULL,
    genesis_hash bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    lineage_id text DEFAULT (gen_random_uuid())::text NOT NULL,
    CONSTRAINT atomic_databases_bootstrap_hash_check CHECK ((octet_length(genesis_hash) = 32)),
    CONSTRAINT atomic_databases_database_id_check CHECK ((database_id <> ''::text)),
    CONSTRAINT atomic_databases_lineage_id_format CHECK ((lineage_id ~ '^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$'::text))
);

CREATE TABLE atomic_fulltext_blocks (
    manifest_hash bytea NOT NULL,
    block_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_fulltext_blocks_block_hash_check CHECK ((octet_length(block_hash) = 32)),
    CONSTRAINT atomic_fulltext_blocks_check CHECK ((sha256(payload) = block_hash)),
    CONSTRAINT atomic_fulltext_blocks_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_fulltext_blocks_payload_check CHECK (((octet_length(payload) >= 1) AND (octet_length(payload) <= 67125248)))
);

CREATE TABLE atomic_fulltext_garbage (
    manifest_hash bytea NOT NULL,
    CONSTRAINT atomic_fulltext_garbage_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32))
);

CREATE TABLE atomic_fulltext_page_builds (
    manifest_hash bytea NOT NULL
);

CREATE TABLE atomic_fulltext_page_edges (
    parent_hash bytea NOT NULL,
    child_hash bytea NOT NULL,
    CONSTRAINT atomic_fulltext_page_edges_check CHECK ((parent_hash <> child_hash))
);

CREATE TABLE atomic_fulltext_page_garbage (
    block_hash bytea NOT NULL
);

CREATE TABLE atomic_fulltext_page_roots (
    manifest_hash bytea NOT NULL,
    root_hash bytea NOT NULL
);

CREATE TABLE atomic_fulltext_pages (
    block_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_for bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_fulltext_pages_block_hash_check CHECK ((octet_length(block_hash) = 32)),
    CONSTRAINT atomic_fulltext_pages_check CHECK ((sha256(payload) = block_hash)),
    CONSTRAINT atomic_fulltext_pages_created_for_check CHECK ((octet_length(created_for) = 32)),
    CONSTRAINT atomic_fulltext_pages_payload_check CHECK (((octet_length(payload) >= 1) AND (octet_length(payload) <= 67125248)))
);

CREATE TABLE atomic_fulltext_projections (
    manifest_hash bytea NOT NULL,
    analyzer_version integer NOT NULL,
    root_hash bytea NOT NULL,
    header_hash bytea NOT NULL,
    header bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_fulltext_projections_analyzer_version_check CHECK ((analyzer_version > 0)),
    CONSTRAINT atomic_fulltext_projections_check CHECK ((sha256(header) = header_hash)),
    CONSTRAINT atomic_fulltext_projections_header_check CHECK (((octet_length(header) >= 1) AND (octet_length(header) <= 1024))),
    CONSTRAINT atomic_fulltext_projections_header_hash_check CHECK ((octet_length(header_hash) = 32)),
    CONSTRAINT atomic_fulltext_projections_root_hash_check CHECK ((octet_length(root_hash) = 32))
);

CREATE TABLE atomic_generation_excision_predicates (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    request_entity bigint NOT NULL,
    request_t bigint NOT NULL,
    target_id bigint NOT NULL,
    target_kind smallint NOT NULL,
    requested_cutoff_kind smallint NOT NULL,
    requested_cutoff_value bigint,
    effective_before_t bigint NOT NULL,
    requested_attributes bigint[] NOT NULL,
    component_extent bigint[] NOT NULL,
    reference_attributes integer[] NOT NULL,
    protected_entity_target boolean NOT NULL,
    predicate_hash bytea NOT NULL,
    CONSTRAINT atomic_generation_excision_predicat_requested_cutoff_kind_check CHECK (((requested_cutoff_kind >= 0) AND (requested_cutoff_kind <= 2))),
    CONSTRAINT atomic_generation_excision_predicates_check CHECK ((((requested_cutoff_kind = 0) AND (requested_cutoff_value IS NULL)) OR ((requested_cutoff_kind = ANY (ARRAY[1, 2])) AND (requested_cutoff_value IS NOT NULL)))),
    CONSTRAINT atomic_generation_excision_predicates_effective_before_t_check CHECK ((effective_before_t >= 0)),
    CONSTRAINT atomic_generation_excision_predicates_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_generation_excision_predicates_predicate_hash_check CHECK ((octet_length(predicate_hash) = 32)),
    CONSTRAINT atomic_generation_excision_predicates_request_entity_check CHECK ((request_entity >= 0)),
    CONSTRAINT atomic_generation_excision_predicates_request_t_check CHECK ((request_t > 0)),
    CONSTRAINT atomic_generation_excision_predicates_target_id_check CHECK ((target_id >= 0)),
    CONSTRAINT atomic_generation_excision_predicates_target_kind_check CHECK ((target_kind = ANY (ARRAY[0, 1])))
);

CREATE TABLE atomic_generation_request_bases (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    request_key_hash bytea NOT NULL,
    base_manifest_hash bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_generation_request_bases_base_manifest_hash_check CHECK ((octet_length(base_manifest_hash) = 32)),
    CONSTRAINT atomic_generation_request_bases_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_generation_request_bases_request_key_hash_check CHECK ((octet_length(request_key_hash) = 32))
);

CREATE TABLE atomic_generation_request_tempids (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    request_key_hash bytea NOT NULL,
    tempid_name text NOT NULL,
    entity_id bigint NOT NULL,
    CONSTRAINT atomic_generation_request_tempids_entity_id_check CHECK ((entity_id >= 0)),
    CONSTRAINT atomic_generation_request_tempids_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_generation_request_tempids_request_key_hash_check CHECK ((octet_length(request_key_hash) = 32))
);

CREATE TABLE atomic_generation_requests (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    request_key_hash bytea NOT NULL,
    request_digest bytea NOT NULL,
    request_kind smallint NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    committed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_generation_requests_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_generation_requests_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_generation_requests_request_digest_check CHECK ((octet_length(request_digest) = 32)),
    CONSTRAINT atomic_generation_requests_request_key_hash_check CHECK ((octet_length(request_key_hash) = 32)),
    CONSTRAINT atomic_generation_requests_request_kind_check CHECK ((request_kind = ANY (ARRAY[0, 1, 2]))),
    CONSTRAINT atomic_generation_requests_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_generation_transactions (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    basis_t bigint NOT NULL,
    previous_hash bytea NOT NULL,
    tx_hash bytea NOT NULL,
    content_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    eidx_frontier bigint NOT NULL,
    committed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_generation_transactions_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_generation_transactions_content_hash_check CHECK ((octet_length(content_hash) = 32)),
    CONSTRAINT atomic_generation_transactions_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_generation_transactions_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_generation_transactions_previous_hash_check CHECK ((octet_length(previous_hash) = 32)),
    CONSTRAINT atomic_generation_transactions_state_hash_check CHECK (((octet_length(state_hash) = 32) AND (state_hash <> decode(repeat('00'::text, 32), 'hex'::text)))),
    CONSTRAINT atomic_generation_transactions_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_index_manifests (
    database_id text NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    manifest_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_index_manifests_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_index_manifests_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_index_manifests_payload_check CHECK ((octet_length(payload) >= 48)),
    CONSTRAINT atomic_index_manifests_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_index_publications (
    database_id text NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    manifest_hash bytea NOT NULL,
    published_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_index_publications_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_index_publications_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_index_publications_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_index_segments (
    segment_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_index_segments_payload_check CHECK ((octet_length(payload) >= 48)),
    CONSTRAINT atomic_index_segments_segment_hash_check CHECK ((octet_length(segment_hash) = 32))
);

CREATE TABLE atomic_log_generation_abandonment_progress (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    phase smallint DEFAULT 0 NOT NULL,
    started_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    updated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_abandonment_progress_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generation_abandonment_progress_phase_check CHECK (((phase >= 0) AND (phase <= 10)))
);

CREATE TABLE atomic_log_generation_activations (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    prior_generation bigint NOT NULL,
    prior_basis_t bigint NOT NULL,
    basis_t bigint NOT NULL,
    head_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    manifest_hash bytea,
    activated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_activations_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_log_generation_activations_check CHECK ((prior_generation <> generation)),
    CONSTRAINT atomic_log_generation_activations_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generation_activations_head_hash_check CHECK ((octet_length(head_hash) = 32)),
    CONSTRAINT atomic_log_generation_activations_manifest_hash_check CHECK (((manifest_hash IS NULL) OR (octet_length(manifest_hash) = 32))),
    CONSTRAINT atomic_log_generation_activations_prior_basis_t_check CHECK ((prior_basis_t >= 0)),
    CONSTRAINT atomic_log_generation_activations_prior_generation_check CHECK ((prior_generation >= 0)),
    CONSTRAINT atomic_log_generation_activations_state_hash_check CHECK ((octet_length(state_hash) = 32))
);

CREATE TABLE atomic_log_generation_builds (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    source_generation bigint,
    captured_basis_t bigint NOT NULL,
    captured_head_hash bytea NOT NULL,
    frozen_plan_hash bytea,
    restore_manifest_hash bytea,
    restore_basis_t bigint,
    restore_head_hash bytea,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_builds_captured_basis_t_check CHECK ((captured_basis_t >= 0)),
    CONSTRAINT atomic_log_generation_builds_captured_head_hash_check CHECK ((octet_length(captured_head_hash) = 32)),
    CONSTRAINT atomic_log_generation_builds_check CHECK ((((source_generation IS NULL) AND (restore_manifest_hash IS NULL) AND (restore_basis_t IS NULL) AND (restore_head_hash IS NULL)) OR ((source_generation IS NOT NULL) AND (restore_manifest_hash IS NULL) AND (restore_basis_t IS NULL) AND (restore_head_hash IS NULL)) OR ((source_generation IS NOT NULL) AND (restore_manifest_hash IS NOT NULL) AND (restore_basis_t IS NOT NULL) AND (restore_head_hash IS NOT NULL)))),
    CONSTRAINT atomic_log_generation_builds_frozen_plan_hash_check CHECK (((frozen_plan_hash IS NULL) OR (octet_length(frozen_plan_hash) = 32))),
    CONSTRAINT atomic_log_generation_builds_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generation_builds_restore_basis_t_check CHECK (((restore_basis_t IS NULL) OR (restore_basis_t >= 0))),
    CONSTRAINT atomic_log_generation_builds_restore_head_hash_check CHECK (((restore_head_hash IS NULL) OR (octet_length(restore_head_hash) = 32))),
    CONSTRAINT atomic_log_generation_builds_restore_manifest_hash_check CHECK (((restore_manifest_hash IS NULL) OR (octet_length(restore_manifest_hash) = 32))),
    CONSTRAINT atomic_log_generation_builds_source_generation_check CHECK ((source_generation >= 0))
);

CREATE TABLE atomic_log_generation_checkpoints (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    through_basis_t bigint NOT NULL,
    head_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    source_head_hash bytea,
    eidx_frontier bigint NOT NULL,
    removed_datoms bigint NOT NULL,
    recorded_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_checkpoints_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_log_generation_checkpoints_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generation_checkpoints_head_hash_check CHECK ((octet_length(head_hash) = 32)),
    CONSTRAINT atomic_log_generation_checkpoints_removed_datoms_check CHECK ((removed_datoms >= 0)),
    CONSTRAINT atomic_log_generation_checkpoints_source_head_hash_check CHECK (((source_head_hash IS NULL) OR (octet_length(source_head_hash) = 32))),
    CONSTRAINT atomic_log_generation_checkpoints_state_hash_check CHECK ((octet_length(state_hash) = 32)),
    CONSTRAINT atomic_log_generation_checkpoints_through_basis_t_check CHECK ((through_basis_t >= 0))
);

CREATE TABLE atomic_log_generation_collection_progress (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    phase smallint DEFAULT 0 NOT NULL,
    started_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    updated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_collection_progress_generation_check CHECK ((generation >= 0)),
    CONSTRAINT atomic_log_generation_collection_progress_phase_check CHECK (((phase >= 0) AND (phase <= 11)))
);

CREATE TABLE atomic_log_generation_completion_stages (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    phase smallint NOT NULL,
    cursor_t bigint,
    cursor_entity bigint,
    row_count bigint DEFAULT 0 NOT NULL,
    sealed_at timestamp with time zone,
    CONSTRAINT atomic_log_generation_completion_stages_check CHECK ((((phase = 2) AND (sealed_at IS NOT NULL)) OR ((phase < 2) AND (sealed_at IS NULL)))),
    CONSTRAINT atomic_log_generation_completion_stages_check1 CHECK (((cursor_t IS NULL) = (cursor_entity IS NULL))),
    CONSTRAINT atomic_log_generation_completion_stages_cursor_entity_check CHECK (((cursor_entity IS NULL) OR (cursor_entity >= 0))),
    CONSTRAINT atomic_log_generation_completion_stages_cursor_t_check CHECK (((cursor_t IS NULL) OR (cursor_t > 0))),
    CONSTRAINT atomic_log_generation_completion_stages_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generation_completion_stages_phase_check CHECK (((phase >= 0) AND (phase <= 2))),
    CONSTRAINT atomic_log_generation_completion_stages_row_count_check CHECK ((row_count >= 0))
);

CREATE TABLE atomic_log_generation_completions (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    completed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generation_completions_generation_check CHECK ((generation > 0))
);

CREATE TABLE atomic_log_generation_garbage_contents (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    content_hash bytea NOT NULL,
    CONSTRAINT atomic_log_generation_garbage_contents_content_hash_check CHECK ((octet_length(content_hash) = 32)),
    CONSTRAINT atomic_log_generation_garbage_contents_generation_check CHECK ((generation > 0))
);

CREATE TABLE atomic_log_generation_retirements (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    successor_generation bigint NOT NULL,
    retired_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    collecting_at timestamp with time zone,
    CONSTRAINT atomic_log_generation_retirements_check CHECK ((generation <> successor_generation)),
    CONSTRAINT atomic_log_generation_retirements_generation_check CHECK ((generation >= 0)),
    CONSTRAINT atomic_log_generation_retirements_successor_generation_check CHECK ((successor_generation > 0))
);

CREATE TABLE atomic_log_generations (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    lineage_id text NOT NULL,
    build_kind smallint NOT NULL,
    request_count bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_log_generations_build_kind_check CHECK (((build_kind >= 0) AND (build_kind <= 2))),
    CONSTRAINT atomic_log_generations_check CHECK ((((build_kind = 0) AND (request_count = 0)) OR ((build_kind = 1) AND (request_count > 0)) OR ((build_kind = 2) AND (request_count = 0)))),
    CONSTRAINT atomic_log_generations_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_log_generations_request_count_check CHECK ((request_count >= 0))
);

CREATE TABLE atomic_program_gc_candidates (
    program_hash bytea NOT NULL,
    candidate_at timestamp with time zone NOT NULL
);

CREATE TABLE atomic_program_generation_refs (
    database_id text NOT NULL,
    log_generation bigint NOT NULL,
    program_hash bytea NOT NULL,
    CONSTRAINT atomic_program_generation_refs_log_generation_check CHECK ((log_generation >= 0))
);

CREATE TABLE atomic_program_reference_state (
    singleton boolean DEFAULT true NOT NULL,
    complete boolean NOT NULL,
    problem_code text,
    updated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    walker_version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT atomic_program_reference_state_check CHECK (((complete AND (problem_code IS NULL)) OR ((NOT complete) AND (problem_code IS NOT NULL)))),
    CONSTRAINT atomic_program_reference_state_singleton_check CHECK (singleton),
    CONSTRAINT atomic_program_reference_state_walker_version_check CHECK ((walker_version >= 0))
);

CREATE TABLE atomic_programs (
    program_hash bytea NOT NULL,
    kind smallint NOT NULL,
    arity smallint NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT transaction_timestamp() NOT NULL,
    CONSTRAINT atomic_programs_arity_check CHECK (((arity >= 0) AND (arity <= 10))),
    CONSTRAINT atomic_programs_kind_check CHECK (((kind >= 0) AND (kind <= 4))),
    CONSTRAINT atomic_programs_program_hash_check CHECK ((octet_length(program_hash) = 32))
);

CREATE TABLE atomic_receipt_archive_conversions (
    manifest_hash bytea NOT NULL,
    database_id text NOT NULL,
    generation bigint NOT NULL,
    publication_revision bigint NOT NULL,
    phase smallint NOT NULL,
    node_count bigint NOT NULL,
    hashed_nodes bigint DEFAULT 0 NOT NULL,
    hash_cursor bytea,
    hash_state bytea,
    started_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_receipt_archive_conversions_check CHECK ((hashed_nodes <= node_count)),
    CONSTRAINT atomic_receipt_archive_conversions_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_receipt_archive_conversions_hash_cursor_check CHECK ((octet_length(hash_cursor) = 32)),
    CONSTRAINT atomic_receipt_archive_conversions_hashed_nodes_check CHECK ((hashed_nodes >= 0)),
    CONSTRAINT atomic_receipt_archive_conversions_node_count_check CHECK ((node_count > 0)),
    CONSTRAINT atomic_receipt_archive_conversions_phase_check CHECK (((phase >= 1) AND (phase <= 3))),
    CONSTRAINT atomic_receipt_archive_conversions_publication_revision_check CHECK ((publication_revision > 0))
);

CREATE TABLE atomic_receipt_archive_frontier (
    manifest_hash bytea NOT NULL,
    node_hash bytea NOT NULL,
    next_child integer DEFAULT 0 NOT NULL,
    CONSTRAINT atomic_receipt_archive_frontier_next_child_check CHECK ((next_child >= 0)),
    CONSTRAINT atomic_receipt_archive_frontier_node_hash_check CHECK ((octet_length(node_hash) = 32))
);

CREATE TABLE atomic_remote_writer_endpoints (
    database_id text NOT NULL,
    lineage_id text NOT NULL,
    holder_id text NOT NULL,
    lease_epoch bigint NOT NULL,
    instance_id bytea NOT NULL,
    network_address text NOT NULL,
    tls_server_name text NOT NULL,
    protocol_version integer NOT NULL,
    CONSTRAINT atomic_remote_writer_endpoints_instance_id_check CHECK ((octet_length(instance_id) = 32)),
    CONSTRAINT atomic_remote_writer_endpoints_lease_epoch_check CHECK ((lease_epoch > 0)),
    CONSTRAINT atomic_remote_writer_endpoints_network_address_check CHECK (((octet_length(network_address) >= 3) AND (octet_length(network_address) <= 128))),
    CONSTRAINT atomic_remote_writer_endpoints_protocol_version_check CHECK ((protocol_version = 1)),
    CONSTRAINT atomic_remote_writer_endpoints_tls_server_name_check CHECK (((octet_length(tls_server_name) >= 1) AND (octet_length(tls_server_name) <= 253)))
);

CREATE TABLE atomic_request_base_archive_completions (
    manifest_hash bytea NOT NULL,
    completed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL
);

CREATE TABLE atomic_request_base_archive_nodes (
    manifest_hash bytea NOT NULL,
    node_hash bytea NOT NULL,
    CONSTRAINT atomic_request_base_archive_nodes_node_hash_check CHECK ((octet_length(node_hash) = 32))
);

CREATE TABLE atomic_request_base_archive_roots (
    manifest_hash bytea NOT NULL,
    index_order smallint NOT NULL,
    history boolean NOT NULL,
    root_hash bytea NOT NULL,
    datom_count bigint NOT NULL,
    encoded_bytes bigint NOT NULL,
    CONSTRAINT atomic_request_base_archive_roots_datom_count_check CHECK ((datom_count >= 0)),
    CONSTRAINT atomic_request_base_archive_roots_encoded_bytes_check CHECK ((encoded_bytes > 0)),
    CONSTRAINT atomic_request_base_archive_roots_index_order_check CHECK (((index_order >= 0) AND (index_order <= 3)))
);

CREATE TABLE atomic_request_base_archives (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    archive_revision bigint NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    eidx_frontier bigint NOT NULL,
    manifest_version smallint NOT NULL,
    manifest_hash bytea NOT NULL,
    payload bytea NOT NULL,
    expected_node_count bigint NOT NULL,
    node_set_hash bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_request_base_archives_archive_revision_check CHECK ((archive_revision > 0)),
    CONSTRAINT atomic_request_base_archives_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_request_base_archives_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_request_base_archives_expected_node_count_check CHECK ((expected_node_count > 0)),
    CONSTRAINT atomic_request_base_archives_generation_check CHECK ((generation > 0)),
    CONSTRAINT atomic_request_base_archives_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_request_base_archives_manifest_version_check CHECK ((manifest_version = ANY (ARRAY[4, 5, 6]))),
    CONSTRAINT atomic_request_base_archives_node_set_hash_check CHECK ((octet_length(node_set_hash) = 32)),
    CONSTRAINT atomic_request_base_archives_payload_check CHECK ((octet_length(payload) > 0)),
    CONSTRAINT atomic_request_base_archives_state_hash_check CHECK ((octet_length(state_hash) = 32)),
    CONSTRAINT atomic_request_base_archives_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_requests (
    database_id text NOT NULL,
    request_key text NOT NULL,
    request_digest bytea NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    committed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_requests_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_requests_request_digest_check CHECK ((octet_length(request_digest) = 32)),
    CONSTRAINT atomic_requests_request_key_check CHECK ((request_key <> ''::text)),
    CONSTRAINT atomic_requests_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_schema_migrations (
    version bigint NOT NULL,
    checksum bytea NOT NULL,
    applied_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_schema_migrations_checksum_check CHECK ((octet_length(checksum) = 32)),
    CONSTRAINT atomic_schema_migrations_version_check CHECK ((version > 0))
);

CREATE TABLE atomic_semantic_commitment_nodes (
    node_hash bytea NOT NULL,
    payload bytea NOT NULL,
    left_hash bytea,
    right_hash bytea,
    subtree_count bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_semantic_commitment_nodes_left_hash_check CHECK (((left_hash IS NULL) OR (octet_length(left_hash) = 32))),
    CONSTRAINT atomic_semantic_commitment_nodes_node_hash_check CHECK ((octet_length(node_hash) = 32)),
    CONSTRAINT atomic_semantic_commitment_nodes_payload_check CHECK ((octet_length(payload) > 0)),
    CONSTRAINT atomic_semantic_commitment_nodes_right_hash_check CHECK (((right_hash IS NULL) OR (octet_length(right_hash) = 32))),
    CONSTRAINT atomic_semantic_commitment_nodes_subtree_count_check CHECK ((subtree_count > 0))
);

CREATE TABLE atomic_semantic_commitment_roots (
    database_id text NOT NULL,
    generation bigint NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    eidx_frontier bigint NOT NULL,
    commitment_version smallint NOT NULL,
    current_root bytea,
    current_count bigint NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_semantic_commitment_roots_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_semantic_commitment_roots_check CHECK ((((current_count = 0) AND (current_root IS NULL)) OR ((current_count > 0) AND (current_root IS NOT NULL)))),
    CONSTRAINT atomic_semantic_commitment_roots_commitment_version_check CHECK ((commitment_version = 2)),
    CONSTRAINT atomic_semantic_commitment_roots_current_count_check CHECK ((current_count >= 0)),
    CONSTRAINT atomic_semantic_commitment_roots_current_root_check CHECK (((current_root IS NULL) OR (octet_length(current_root) = 32))),
    CONSTRAINT atomic_semantic_commitment_roots_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_semantic_commitment_roots_generation_check CHECK ((generation >= 0)),
    CONSTRAINT atomic_semantic_commitment_roots_state_hash_check CHECK (((octet_length(state_hash) = 32) AND (state_hash <> decode(repeat('00'::text, 32), 'hex'::text)))),
    CONSTRAINT atomic_semantic_commitment_roots_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_transaction_contents (
    content_hash bytea NOT NULL,
    lineage_id text NOT NULL,
    basis_t bigint NOT NULL,
    eidx_frontier bigint NOT NULL,
    envelope_version smallint DEFAULT 1 NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_transaction_contents_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_transaction_contents_content_hash_check CHECK ((octet_length(content_hash) = 32)),
    CONSTRAINT atomic_transaction_contents_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_transaction_contents_envelope_version_check CHECK ((envelope_version = ANY (ARRAY[1, 2]))),
    CONSTRAINT atomic_transaction_contents_payload_check CHECK ((octet_length(payload) >= 58))
);

CREATE TABLE atomic_transactions (
    database_id text NOT NULL,
    basis_t bigint NOT NULL,
    previous_hash bytea NOT NULL,
    tx_hash bytea NOT NULL,
    payload bytea NOT NULL,
    committed_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    state_hash bytea DEFAULT decode(repeat('00'::text, 32), 'hex'::text) NOT NULL,
    CONSTRAINT atomic_transactions_basis_t_check CHECK ((basis_t > 0)),
    CONSTRAINT atomic_transactions_payload_check CHECK ((octet_length(payload) >= 48)),
    CONSTRAINT atomic_transactions_previous_hash_check CHECK ((octet_length(previous_hash) = 32)),
    CONSTRAINT atomic_transactions_state_hash_length CHECK ((octet_length(state_hash) = 32)),
    CONSTRAINT atomic_transactions_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_transactor_leases (
    lease_scope text NOT NULL,
    holder_id text NOT NULL,
    epoch bigint NOT NULL,
    expires_at timestamp with time zone NOT NULL,
    CONSTRAINT atomic_transactor_leases_epoch_check CHECK ((epoch > 0)),
    CONSTRAINT atomic_transactor_leases_holder_id_check CHECK ((holder_id <> ''::text)),
    CONSTRAINT atomic_transactor_leases_lease_scope_check CHECK ((lease_scope <> ''::text))
);

CREATE TABLE atomic_tree_build_intent_nodes (
    manifest_hash bytea NOT NULL,
    node_hash bytea NOT NULL,
    CONSTRAINT atomic_tree_build_intent_nodes_node_hash_check CHECK ((octet_length(node_hash) = 32))
);

CREATE TABLE atomic_tree_build_intents (
    manifest_hash bytea NOT NULL,
    database_id text NOT NULL,
    log_generation bigint NOT NULL,
    expected_revision bigint NOT NULL,
    expected_node_count bigint NOT NULL,
    staged_node_count bigint DEFAULT 0 NOT NULL,
    node_set_hash bytea NOT NULL,
    intent_state smallint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    heartbeat_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_build_intents_check CHECK (((staged_node_count >= 0) AND (staged_node_count <= expected_node_count))),
    CONSTRAINT atomic_tree_build_intents_check1 CHECK (((intent_state <> 1) OR (staged_node_count = expected_node_count))),
    CONSTRAINT atomic_tree_build_intents_check2 CHECK (((intent_state <> 2) OR (staged_node_count = expected_node_count))),
    CONSTRAINT atomic_tree_build_intents_expected_node_count_check CHECK ((expected_node_count >= 0)),
    CONSTRAINT atomic_tree_build_intents_expected_revision_check CHECK ((expected_revision >= 0)),
    CONSTRAINT atomic_tree_build_intents_intent_state_check CHECK (((intent_state >= 0) AND (intent_state <= 3))),
    CONSTRAINT atomic_tree_build_intents_log_generation_check CHECK ((log_generation >= 0)),
    CONSTRAINT atomic_tree_build_intents_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_tree_build_intents_node_set_hash_check CHECK ((octet_length(node_set_hash) = 32))
);

CREATE TABLE atomic_tree_delta_headers (
    manifest_hash bytea NOT NULL,
    predecessor_manifest_hash bytea,
    delta_mode smallint NOT NULL,
    expected_node_count bigint NOT NULL,
    staged_node_count bigint DEFAULT 0 NOT NULL,
    delta_set_hash bytea NOT NULL,
    added_node_count bigint NOT NULL,
    added_set_hash bytea NOT NULL,
    delta_state smallint DEFAULT 0 NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_delta_headers_added_set_hash_check CHECK ((octet_length(added_set_hash) = 32)),
    CONSTRAINT atomic_tree_delta_headers_check CHECK (((staged_node_count >= 0) AND (staged_node_count <= expected_node_count))),
    CONSTRAINT atomic_tree_delta_headers_check1 CHECK (((added_node_count >= 0) AND (added_node_count <= expected_node_count))),
    CONSTRAINT atomic_tree_delta_headers_check2 CHECK (((delta_state = 0) OR (staged_node_count = expected_node_count))),
    CONSTRAINT atomic_tree_delta_headers_delta_mode_check CHECK (((delta_mode >= 0) AND (delta_mode <= 2))),
    CONSTRAINT atomic_tree_delta_headers_delta_set_hash_check CHECK ((octet_length(delta_set_hash) = 32)),
    CONSTRAINT atomic_tree_delta_headers_delta_state_check CHECK (((delta_state >= 0) AND (delta_state <= 2))),
    CONSTRAINT atomic_tree_delta_headers_expected_node_count_check CHECK ((expected_node_count >= 0)),
    CONSTRAINT atomic_tree_delta_headers_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_tree_delta_headers_predecessor_manifest_hash_check CHECK (((predecessor_manifest_hash IS NULL) OR (octet_length(predecessor_manifest_hash) = 32)))
);

CREATE TABLE atomic_tree_delta_nodes (
    manifest_hash bytea NOT NULL,
    node_hash bytea NOT NULL,
    direction smallint NOT NULL,
    CONSTRAINT atomic_tree_delta_nodes_direction_check CHECK ((direction = ANY (ARRAY['-1'::integer, 1])))
);

CREATE TABLE atomic_tree_garbage_nodes (
    node_hash bytea NOT NULL,
    marked_at timestamp with time zone NOT NULL
);

CREATE TABLE atomic_tree_live_nodes (
    database_id text NOT NULL,
    node_hash bytea NOT NULL
);

CREATE TABLE atomic_tree_live_sets (
    database_id text NOT NULL,
    manifest_hash bytea NOT NULL,
    complete boolean NOT NULL,
    problem_code text,
    updated_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_live_sets_check CHECK (((complete AND (problem_code IS NULL)) OR ((NOT complete) AND (problem_code IS NOT NULL))))
);

CREATE TABLE atomic_tree_manifest_roots (
    manifest_hash bytea NOT NULL,
    index_order smallint NOT NULL,
    history boolean NOT NULL,
    root_hash bytea NOT NULL,
    datom_count bigint NOT NULL,
    encoded_bytes bigint NOT NULL,
    CONSTRAINT atomic_tree_manifest_roots_datom_count_check CHECK ((datom_count >= 0)),
    CONSTRAINT atomic_tree_manifest_roots_encoded_bytes_check CHECK ((encoded_bytes > 0)),
    CONSTRAINT atomic_tree_manifest_roots_index_order_check CHECK (((index_order >= 0) AND (index_order <= 3)))
);

CREATE TABLE atomic_tree_manifests (
    database_id text NOT NULL,
    publication_revision bigint NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    state_hash bytea NOT NULL,
    excision_generation bigint NOT NULL,
    eidx_frontier bigint NOT NULL,
    manifest_version smallint NOT NULL,
    manifest_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    log_generation bigint DEFAULT 0 NOT NULL,
    lineage_id text,
    index_basis_t bigint,
    CONSTRAINT atomic_tree_manifests_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_tree_manifests_eidx_frontier_check CHECK ((eidx_frontier > 0)),
    CONSTRAINT atomic_tree_manifests_excision_generation_check CHECK ((excision_generation >= 0)),
    CONSTRAINT atomic_tree_manifests_generation_identity CHECK ((((log_generation = 0) AND (lineage_id IS NULL)) OR ((log_generation > 0) AND (lineage_id IS NOT NULL)))),
    CONSTRAINT atomic_tree_manifests_index_basis_check CHECK ((((manifest_version = ANY (ARRAY[4, 5])) AND (index_basis_t IS NULL)) OR ((manifest_version = 6) AND (index_basis_t IS NOT NULL) AND (index_basis_t >= 0) AND (index_basis_t <= basis_t)))),
    CONSTRAINT atomic_tree_manifests_log_generation_check CHECK ((log_generation >= 0)),
    CONSTRAINT atomic_tree_manifests_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_tree_manifests_manifest_version_check CHECK ((manifest_version = ANY (ARRAY[4, 5, 6]))),
    CONSTRAINT atomic_tree_manifests_payload_check CHECK ((octet_length(payload) > 0)),
    CONSTRAINT atomic_tree_manifests_publication_revision_check CHECK ((publication_revision > 0)),
    CONSTRAINT atomic_tree_manifests_state_hash_check CHECK ((octet_length(state_hash) = 32)),
    CONSTRAINT atomic_tree_manifests_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_tree_node_blocks (
    node_hash bytea NOT NULL,
    canonical_bytes bigint NOT NULL,
    physical_hash bytea NOT NULL,
    physical_payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_node_blocks_canonical_bytes_check CHECK (((canonical_bytes >= 1) AND (canonical_bytes <= 67108864))),
    CONSTRAINT atomic_tree_node_blocks_check CHECK (((octet_length(physical_payload) > 0) AND (octet_length(physical_payload) < canonical_bytes))),
    CONSTRAINT atomic_tree_node_blocks_physical_hash_check CHECK ((octet_length(physical_hash) = 32))
);

CREATE TABLE atomic_tree_nodes (
    node_hash bytea NOT NULL,
    payload bytea NOT NULL,
    created_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_nodes_node_hash_check CHECK ((octet_length(node_hash) = 32)),
    CONSTRAINT atomic_tree_nodes_payload_check CHECK ((octet_length(payload) > 0))
);

CREATE TABLE atomic_tree_publication_states (
    manifest_hash bytea NOT NULL,
    predecessor_manifest_hash bytea,
    delta_mode smallint NOT NULL,
    CONSTRAINT atomic_tree_publication_states_delta_mode_check CHECK (((delta_mode >= 0) AND (delta_mode <= 2)))
);

CREATE TABLE atomic_tree_publications (
    database_id text NOT NULL,
    publication_revision bigint NOT NULL,
    basis_t bigint NOT NULL,
    tx_hash bytea NOT NULL,
    manifest_hash bytea NOT NULL,
    published_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    log_generation bigint DEFAULT 0 NOT NULL,
    CONSTRAINT atomic_tree_publications_basis_t_check CHECK ((basis_t >= 0)),
    CONSTRAINT atomic_tree_publications_log_generation_check CHECK ((log_generation >= 0)),
    CONSTRAINT atomic_tree_publications_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_tree_publications_publication_revision_check CHECK ((publication_revision > 0)),
    CONSTRAINT atomic_tree_publications_tx_hash_check CHECK ((octet_length(tx_hash) = 32))
);

CREATE TABLE atomic_tree_retired_nodes (
    database_id text NOT NULL,
    publication_revision bigint NOT NULL,
    node_hash bytea NOT NULL
);

CREATE TABLE atomic_tree_retirement_progress (
    database_id text NOT NULL,
    publication_revision bigint NOT NULL,
    manifest_hash bytea NOT NULL,
    started_at timestamp with time zone DEFAULT clock_timestamp() NOT NULL,
    CONSTRAINT atomic_tree_retirement_progress_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32))
);

CREATE TABLE atomic_tree_retirements (
    database_id text NOT NULL,
    publication_revision bigint NOT NULL,
    manifest_hash bytea NOT NULL,
    retired_at timestamp with time zone NOT NULL,
    bookkeeping_complete boolean NOT NULL,
    garbage_complete boolean NOT NULL,
    log_generation bigint DEFAULT 0 NOT NULL,
    CONSTRAINT atomic_tree_retirements_log_generation_check CHECK ((log_generation >= 0)),
    CONSTRAINT atomic_tree_retirements_manifest_hash_check CHECK ((octet_length(manifest_hash) = 32)),
    CONSTRAINT atomic_tree_retirements_publication_revision_check CHECK ((publication_revision > 0))
);

ALTER TABLE ONLY atomic_change_checkpoints
    ADD CONSTRAINT atomic_change_checkpoints_pkey PRIMARY KEY (database_id, consumer_name, checkpoint_owner);

ALTER TABLE ONLY atomic_completed_excision_requests
    ADD CONSTRAINT atomic_completed_excision_requests_pkey PRIMARY KEY (database_id, generation, request_t, request_entity);

ALTER TABLE ONLY atomic_database_identities
    ADD CONSTRAINT atomic_database_identities_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_database_names
    ADD CONSTRAINT atomic_database_names_database_id_key UNIQUE (database_id);

ALTER TABLE ONLY atomic_database_names
    ADD CONSTRAINT atomic_database_names_pkey PRIMARY KEY (name);

ALTER TABLE ONLY atomic_database_reclamation_objects
    ADD CONSTRAINT atomic_database_reclamation_objects_pkey PRIMARY KEY (database_id, kind, object_hash);

ALTER TABLE ONLY atomic_database_reclamation_progress
    ADD CONSTRAINT atomic_database_reclamation_progress_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_databases
    ADD CONSTRAINT atomic_databases_id_lineage_key UNIQUE (database_id, lineage_id);

ALTER TABLE ONLY atomic_databases
    ADD CONSTRAINT atomic_databases_lineage_id_key UNIQUE (lineage_id);

ALTER TABLE ONLY atomic_databases
    ADD CONSTRAINT atomic_databases_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_fulltext_blocks
    ADD CONSTRAINT atomic_fulltext_blocks_pkey PRIMARY KEY (manifest_hash, block_hash);

ALTER TABLE ONLY atomic_fulltext_garbage
    ADD CONSTRAINT atomic_fulltext_garbage_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_fulltext_page_builds
    ADD CONSTRAINT atomic_fulltext_page_builds_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_fulltext_page_edges
    ADD CONSTRAINT atomic_fulltext_page_edges_pkey PRIMARY KEY (parent_hash, child_hash);

ALTER TABLE ONLY atomic_fulltext_page_garbage
    ADD CONSTRAINT atomic_fulltext_page_garbage_pkey PRIMARY KEY (block_hash);

ALTER TABLE ONLY atomic_fulltext_page_roots
    ADD CONSTRAINT atomic_fulltext_page_roots_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_fulltext_pages
    ADD CONSTRAINT atomic_fulltext_pages_pkey PRIMARY KEY (block_hash);

ALTER TABLE ONLY atomic_fulltext_projections
    ADD CONSTRAINT atomic_fulltext_projections_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_generation_excision_predicates
    ADD CONSTRAINT atomic_generation_excision_pr_database_id_generation_predic_key UNIQUE (database_id, generation, predicate_hash);

ALTER TABLE ONLY atomic_generation_excision_predicates
    ADD CONSTRAINT atomic_generation_excision_predicates_pkey PRIMARY KEY (database_id, generation, request_t, request_entity);

ALTER TABLE ONLY atomic_generation_request_bases
    ADD CONSTRAINT atomic_generation_request_bases_pkey PRIMARY KEY (database_id, generation, request_key_hash);

ALTER TABLE ONLY atomic_generation_request_tempids
    ADD CONSTRAINT atomic_generation_request_tempids_pkey PRIMARY KEY (database_id, generation, request_key_hash, tempid_name);

ALTER TABLE ONLY atomic_generation_requests
    ADD CONSTRAINT atomic_generation_requests_database_id_generation_basis_t_key UNIQUE (database_id, generation, basis_t);

ALTER TABLE ONLY atomic_generation_requests
    ADD CONSTRAINT atomic_generation_requests_pkey PRIMARY KEY (database_id, generation, request_key_hash);

ALTER TABLE ONLY atomic_generation_transactions
    ADD CONSTRAINT atomic_generation_transaction_database_id_generation_basis__key UNIQUE (database_id, generation, basis_t, tx_hash);

ALTER TABLE ONLY atomic_generation_transactions
    ADD CONSTRAINT atomic_generation_transaction_database_id_generation_tx_has_key UNIQUE (database_id, generation, tx_hash);

ALTER TABLE ONLY atomic_generation_transactions
    ADD CONSTRAINT atomic_generation_transactions_pkey PRIMARY KEY (database_id, generation, basis_t);

ALTER TABLE ONLY atomic_heads
    ADD CONSTRAINT atomic_heads_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_index_manifests
    ADD CONSTRAINT atomic_index_manifests_manifest_hash_key UNIQUE (manifest_hash);

ALTER TABLE ONLY atomic_index_manifests
    ADD CONSTRAINT atomic_index_manifests_pkey PRIMARY KEY (database_id, basis_t);

ALTER TABLE ONLY atomic_index_publications
    ADD CONSTRAINT atomic_index_publications_pkey PRIMARY KEY (database_id, basis_t);

ALTER TABLE ONLY atomic_index_segments
    ADD CONSTRAINT atomic_index_segments_pkey PRIMARY KEY (segment_hash);

ALTER TABLE ONLY atomic_log_generation_abandonment_progress
    ADD CONSTRAINT atomic_log_generation_abandonment_progress_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_activations
    ADD CONSTRAINT atomic_log_generation_activations_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_builds
    ADD CONSTRAINT atomic_log_generation_builds_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_checkpoints
    ADD CONSTRAINT atomic_log_generation_checkpoints_pkey PRIMARY KEY (database_id, generation, through_basis_t);

ALTER TABLE ONLY atomic_log_generation_collection_progress
    ADD CONSTRAINT atomic_log_generation_collection_progress_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_completion_stages
    ADD CONSTRAINT atomic_log_generation_completion_stages_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_completions
    ADD CONSTRAINT atomic_log_generation_completions_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generation_garbage_contents
    ADD CONSTRAINT atomic_log_generation_garbage_contents_pkey PRIMARY KEY (database_id, generation, content_hash);

ALTER TABLE ONLY atomic_log_generation_retirements
    ADD CONSTRAINT atomic_log_generation_retirem_database_id_successor_generat_key UNIQUE (database_id, successor_generation);

ALTER TABLE ONLY atomic_log_generation_retirements
    ADD CONSTRAINT atomic_log_generation_retirements_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_log_generations
    ADD CONSTRAINT atomic_log_generations_pkey PRIMARY KEY (database_id, generation);

ALTER TABLE ONLY atomic_program_gc_candidates
    ADD CONSTRAINT atomic_program_gc_candidates_pkey PRIMARY KEY (program_hash);

ALTER TABLE ONLY atomic_program_generation_refs
    ADD CONSTRAINT atomic_program_generation_refs_pkey PRIMARY KEY (database_id, log_generation, program_hash);

ALTER TABLE ONLY atomic_program_reference_state
    ADD CONSTRAINT atomic_program_reference_state_pkey PRIMARY KEY (singleton);

ALTER TABLE ONLY atomic_programs
    ADD CONSTRAINT atomic_programs_pkey PRIMARY KEY (program_hash);

ALTER TABLE ONLY atomic_receipt_archive_conversions
    ADD CONSTRAINT atomic_receipt_archive_conversions_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_receipt_archive_frontier
    ADD CONSTRAINT atomic_receipt_archive_frontier_pkey PRIMARY KEY (manifest_hash, node_hash);

ALTER TABLE ONLY atomic_remote_writer_endpoints
    ADD CONSTRAINT atomic_remote_writer_endpoints_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_request_base_archive_completions
    ADD CONSTRAINT atomic_request_base_archive_completions_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_request_base_archive_nodes
    ADD CONSTRAINT atomic_request_base_archive_nodes_pkey PRIMARY KEY (manifest_hash, node_hash);

ALTER TABLE ONLY atomic_request_base_archive_roots
    ADD CONSTRAINT atomic_request_base_archive_roots_pkey PRIMARY KEY (manifest_hash, index_order, history);

ALTER TABLE ONLY atomic_request_base_archives
    ADD CONSTRAINT atomic_request_base_archives_database_id_generation_archive_key UNIQUE (database_id, generation, archive_revision);

ALTER TABLE ONLY atomic_request_base_archives
    ADD CONSTRAINT atomic_request_base_archives_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_requests
    ADD CONSTRAINT atomic_requests_database_id_basis_t_key UNIQUE (database_id, basis_t);

ALTER TABLE ONLY atomic_requests
    ADD CONSTRAINT atomic_requests_pkey PRIMARY KEY (database_id, request_key);

ALTER TABLE ONLY atomic_schema_migrations
    ADD CONSTRAINT atomic_schema_migrations_pkey PRIMARY KEY (version);

ALTER TABLE ONLY atomic_semantic_commitment_nodes
    ADD CONSTRAINT atomic_semantic_commitment_nodes_pkey PRIMARY KEY (node_hash);

ALTER TABLE ONLY atomic_semantic_commitment_roots
    ADD CONSTRAINT atomic_semantic_commitment_ro_database_id_generation_tx_has_key UNIQUE (database_id, generation, tx_hash);

ALTER TABLE ONLY atomic_semantic_commitment_roots
    ADD CONSTRAINT atomic_semantic_commitment_roots_pkey PRIMARY KEY (database_id, generation, basis_t);

ALTER TABLE ONLY atomic_transaction_contents
    ADD CONSTRAINT atomic_transaction_contents_pkey PRIMARY KEY (content_hash);

ALTER TABLE ONLY atomic_transactions
    ADD CONSTRAINT atomic_transactions_database_id_basis_t_tx_hash_key UNIQUE (database_id, basis_t, tx_hash);

ALTER TABLE ONLY atomic_transactions
    ADD CONSTRAINT atomic_transactions_database_id_tx_hash_key UNIQUE (database_id, tx_hash);

ALTER TABLE ONLY atomic_transactions
    ADD CONSTRAINT atomic_transactions_pkey PRIMARY KEY (database_id, basis_t);

ALTER TABLE ONLY atomic_transactor_leases
    ADD CONSTRAINT atomic_transactor_leases_pkey PRIMARY KEY (lease_scope);

ALTER TABLE ONLY atomic_tree_build_intent_nodes
    ADD CONSTRAINT atomic_tree_build_intent_nodes_pkey PRIMARY KEY (manifest_hash, node_hash);

ALTER TABLE ONLY atomic_tree_build_intents
    ADD CONSTRAINT atomic_tree_build_intents_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_tree_delta_headers
    ADD CONSTRAINT atomic_tree_delta_headers_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_tree_delta_nodes
    ADD CONSTRAINT atomic_tree_delta_nodes_pkey PRIMARY KEY (manifest_hash, node_hash);

ALTER TABLE ONLY atomic_tree_garbage_nodes
    ADD CONSTRAINT atomic_tree_garbage_nodes_pkey PRIMARY KEY (node_hash);

ALTER TABLE ONLY atomic_tree_live_nodes
    ADD CONSTRAINT atomic_tree_live_nodes_pkey PRIMARY KEY (database_id, node_hash);

ALTER TABLE ONLY atomic_tree_live_sets
    ADD CONSTRAINT atomic_tree_live_sets_pkey PRIMARY KEY (database_id);

ALTER TABLE ONLY atomic_tree_manifest_roots
    ADD CONSTRAINT atomic_tree_manifest_roots_pkey PRIMARY KEY (manifest_hash, index_order, history);

ALTER TABLE ONLY atomic_tree_manifests
    ADD CONSTRAINT atomic_tree_manifests_database_id_publication_revision_basi_key UNIQUE (database_id, publication_revision, basis_t, tx_hash, manifest_hash);

ALTER TABLE ONLY atomic_tree_manifests
    ADD CONSTRAINT atomic_tree_manifests_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_tree_node_blocks
    ADD CONSTRAINT atomic_tree_node_blocks_pkey PRIMARY KEY (node_hash);

ALTER TABLE ONLY atomic_tree_nodes
    ADD CONSTRAINT atomic_tree_nodes_pkey PRIMARY KEY (node_hash);

ALTER TABLE ONLY atomic_tree_publication_states
    ADD CONSTRAINT atomic_tree_publication_states_pkey PRIMARY KEY (manifest_hash);

ALTER TABLE ONLY atomic_tree_publications
    ADD CONSTRAINT atomic_tree_publications_manifest_hash_key UNIQUE (manifest_hash);

ALTER TABLE ONLY atomic_tree_publications
    ADD CONSTRAINT atomic_tree_publications_pkey PRIMARY KEY (database_id, publication_revision);

ALTER TABLE ONLY atomic_tree_retired_nodes
    ADD CONSTRAINT atomic_tree_retired_nodes_pkey PRIMARY KEY (database_id, publication_revision, node_hash);

ALTER TABLE ONLY atomic_tree_retirement_progress
    ADD CONSTRAINT atomic_tree_retirement_progress_manifest_hash_key UNIQUE (manifest_hash);

ALTER TABLE ONLY atomic_tree_retirement_progress
    ADD CONSTRAINT atomic_tree_retirement_progress_pkey PRIMARY KEY (database_id, publication_revision);

ALTER TABLE ONLY atomic_tree_retirements
    ADD CONSTRAINT atomic_tree_retirements_manifest_hash_key UNIQUE (manifest_hash);

ALTER TABLE ONLY atomic_tree_retirements
    ADD CONSTRAINT atomic_tree_retirements_pkey PRIMARY KEY (database_id, publication_revision);

CREATE INDEX atomic_database_identities_retired ON atomic_database_identities USING btree (database_id) WHERE (retired_at IS NOT NULL);

CREATE INDEX atomic_database_reclamation_objects_hash ON atomic_database_reclamation_objects USING btree (kind, object_hash);

CREATE INDEX atomic_database_reclamation_objects_pending ON atomic_database_reclamation_objects USING btree (database_id, kind, object_hash) WHERE (NOT expanded);

CREATE INDEX atomic_database_reclamation_transaction ON atomic_database_reclamation_progress USING btree (active_backend, active_xid) WHERE (active_backend IS NOT NULL);

CREATE INDEX atomic_fulltext_page_edges_child ON atomic_fulltext_page_edges USING btree (child_hash);

CREATE INDEX atomic_fulltext_page_roots_hash ON atomic_fulltext_page_roots USING btree (root_hash);

CREATE INDEX atomic_fulltext_pages_created_for ON atomic_fulltext_pages USING btree (created_for);

CREATE INDEX atomic_generation_request_bases_manifest ON atomic_generation_request_bases USING btree (base_manifest_hash);

CREATE INDEX atomic_program_generation_refs_hash ON atomic_program_generation_refs USING btree (program_hash, database_id, log_generation);

CREATE INDEX atomic_request_base_archive_nodes_node ON atomic_request_base_archive_nodes USING btree (node_hash);

CREATE INDEX atomic_request_base_archive_roots_node ON atomic_request_base_archive_roots USING btree (root_hash);

CREATE INDEX atomic_semantic_commitment_nodes_created_at ON atomic_semantic_commitment_nodes USING btree (created_at);

CREATE INDEX atomic_semantic_commitment_nodes_left ON atomic_semantic_commitment_nodes USING btree (left_hash) WHERE (left_hash IS NOT NULL);

CREATE INDEX atomic_semantic_commitment_nodes_right ON atomic_semantic_commitment_nodes USING btree (right_hash) WHERE (right_hash IS NOT NULL);

CREATE INDEX atomic_semantic_commitment_roots_node ON atomic_semantic_commitment_roots USING btree (current_root) WHERE (current_root IS NOT NULL);

CREATE INDEX atomic_tree_build_intent_nodes_hash ON atomic_tree_build_intent_nodes USING btree (node_hash, manifest_hash);

CREATE INDEX atomic_tree_build_intents_age ON atomic_tree_build_intents USING btree (heartbeat_at, manifest_hash);

CREATE INDEX atomic_tree_delta_nodes_direction ON atomic_tree_delta_nodes USING btree (manifest_hash, direction, node_hash);

CREATE INDEX atomic_tree_delta_nodes_hash ON atomic_tree_delta_nodes USING btree (node_hash, manifest_hash);

CREATE INDEX atomic_tree_garbage_nodes_age ON atomic_tree_garbage_nodes USING btree (marked_at, node_hash);

CREATE INDEX atomic_tree_live_nodes_hash ON atomic_tree_live_nodes USING btree (node_hash, database_id);

CREATE INDEX atomic_tree_manifest_roots_node ON atomic_tree_manifest_roots USING btree (root_hash);

CREATE INDEX atomic_tree_manifests_generation_basis ON atomic_tree_manifests USING btree (database_id, excision_generation, basis_t DESC, publication_revision DESC);

CREATE INDEX atomic_tree_nodes_created_at ON atomic_tree_nodes USING btree (created_at);

CREATE INDEX atomic_tree_publications_basis ON atomic_tree_publications USING btree (database_id, basis_t DESC, publication_revision DESC);

CREATE INDEX atomic_tree_publications_generation_revision ON atomic_tree_publications USING btree (database_id, log_generation, publication_revision DESC);

CREATE INDEX atomic_tree_publications_latest ON atomic_tree_publications USING btree (database_id, publication_revision DESC);

CREATE INDEX atomic_tree_retired_nodes_hash ON atomic_tree_retired_nodes USING btree (node_hash, database_id, publication_revision);

CREATE INDEX atomic_tree_retirements_age ON atomic_tree_retirements USING btree (retired_at, database_id, publication_revision);

CREATE TRIGGER atomic_completed_excision_requests_immutable BEFORE DELETE OR UPDATE ON atomic_completed_excision_requests FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_content_envelope_version BEFORE INSERT ON atomic_transaction_contents FOR EACH ROW EXECUTE FUNCTION atomic_content_envelope_version();

CREATE TRIGGER atomic_databases_immutable BEFORE DELETE OR UPDATE ON atomic_databases FOR EACH ROW EXECUTE FUNCTION atomic_reject_database_mutation();

CREATE TRIGGER atomic_fulltext_blocks_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_blocks FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_fulltext_blocks_validate_insert BEFORE INSERT ON atomic_fulltext_blocks FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_block_insert();

CREATE TRIGGER atomic_fulltext_build_retirement AFTER DELETE ON atomic_fulltext_page_builds FOR EACH ROW EXECUTE FUNCTION atomic_retire_fulltext_build();

CREATE TRIGGER atomic_fulltext_page_builds_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_page_builds FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_fulltext_page_builds_validate_insert BEFORE INSERT ON atomic_fulltext_page_builds FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();

CREATE TRIGGER atomic_fulltext_page_edge_frontier AFTER INSERT OR DELETE ON atomic_fulltext_page_edges FOR EACH ROW EXECUTE FUNCTION atomic_track_fulltext_page_reference();

CREATE TRIGGER atomic_fulltext_page_edges_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_page_edges FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_fulltext_page_root_frontier AFTER INSERT OR DELETE ON atomic_fulltext_page_roots FOR EACH ROW EXECUTE FUNCTION atomic_track_fulltext_page_reference();

CREATE TRIGGER atomic_fulltext_page_roots_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_page_roots FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_fulltext_page_roots_validate_insert BEFORE INSERT ON atomic_fulltext_page_roots FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();

CREATE TRIGGER atomic_fulltext_pages_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_pages FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_fulltext_pages_validate_insert BEFORE INSERT ON atomic_fulltext_pages FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();

CREATE TRIGGER atomic_fulltext_projection_validate_root BEFORE INSERT ON atomic_fulltext_projections FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_projection_root();

CREATE TRIGGER atomic_fulltext_projections_active BEFORE INSERT ON atomic_fulltext_projections FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();

CREATE TRIGGER atomic_fulltext_projections_immutable BEFORE DELETE OR UPDATE ON atomic_fulltext_projections FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE TRIGGER atomic_generation_excision_predicates_staging_immutable BEFORE DELETE OR UPDATE ON atomic_generation_excision_predicates FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();

CREATE TRIGGER atomic_generation_request_bases_immutable BEFORE DELETE OR UPDATE ON atomic_generation_request_bases FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_request_base_mutation();

CREATE TRIGGER atomic_generation_request_bases_validate_insert BEFORE INSERT ON atomic_generation_request_bases FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_request_base_insert();

CREATE TRIGGER atomic_generation_request_tempids_immutable BEFORE DELETE OR UPDATE ON atomic_generation_request_tempids FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_generation_requests_immutable BEFORE DELETE OR UPDATE ON atomic_generation_requests FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_generation_requests_validate_insert BEFORE INSERT ON atomic_generation_requests FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_request_insert();

CREATE TRIGGER atomic_generation_transactions_immutable BEFORE DELETE OR UPDATE ON atomic_generation_transactions FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE CONSTRAINT TRIGGER atomic_generation_transactions_require_publication AFTER INSERT ON atomic_generation_transactions DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION atomic_require_published_generation_transaction();

CREATE TRIGGER atomic_generation_transactions_validate_insert BEFORE INSERT ON atomic_generation_transactions FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_transaction_insert();

CREATE TRIGGER atomic_heads_active BEFORE INSERT OR UPDATE ON atomic_heads FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();

CREATE TRIGGER atomic_heads_validate_advance BEFORE UPDATE ON atomic_heads FOR EACH ROW EXECUTE FUNCTION atomic_validate_head_advance();

CREATE TRIGGER atomic_heads_validate_insert BEFORE INSERT ON atomic_heads FOR EACH ROW EXECUTE FUNCTION atomic_validate_head_insert();

CREATE TRIGGER atomic_index_manifests_immutable BEFORE DELETE OR UPDATE ON atomic_index_manifests FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_index_publications_active BEFORE INSERT ON atomic_index_publications FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();

CREATE TRIGGER atomic_index_publications_immutable BEFORE DELETE OR UPDATE ON atomic_index_publications FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_index_publications_validate_insert BEFORE INSERT ON atomic_index_publications FOR EACH ROW EXECUTE FUNCTION atomic_validate_index_publication();

CREATE TRIGGER atomic_index_segments_immutable BEFORE DELETE OR UPDATE ON atomic_index_segments FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

CREATE TRIGGER atomic_log_generation_activations_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_activations FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_log_generation_activations_semantic_root BEFORE INSERT ON atomic_log_generation_activations FOR EACH ROW EXECUTE FUNCTION atomic_validate_activation_semantic_root();

CREATE TRIGGER atomic_log_generation_builds_staging_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_builds FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();

CREATE TRIGGER atomic_log_generation_checkpoints_staging_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_checkpoints FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();

CREATE TRIGGER atomic_log_generation_checkpoints_validate_insert BEFORE INSERT ON atomic_log_generation_checkpoints FOR EACH ROW EXECUTE FUNCTION atomic_validate_generation_checkpoint_insert();

CREATE TRIGGER atomic_log_generation_completion_stages_staging_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_completion_stages FOR EACH ROW EXECUTE FUNCTION atomic_reject_generation_staging_mutation();

CREATE TRIGGER atomic_log_generation_completions_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_completions FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_log_generation_retirements_immutable BEFORE DELETE OR UPDATE ON atomic_log_generation_retirements FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_log_generations_immutable BEFORE DELETE OR UPDATE ON atomic_log_generations FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_programs_immutable BEFORE DELETE OR UPDATE ON atomic_programs FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_programs_reject_truncate BEFORE TRUNCATE ON atomic_programs FOR EACH STATEMENT EXECUTE FUNCTION atomic_reject_program_mutation();

CREATE TRIGGER atomic_programs_track_candidate AFTER INSERT ON atomic_programs FOR EACH ROW EXECUTE FUNCTION atomic_track_program_candidate();

CREATE TRIGGER atomic_remote_writer_endpoint_validate BEFORE INSERT OR UPDATE ON atomic_remote_writer_endpoints FOR EACH ROW EXECUTE FUNCTION atomic_validate_remote_writer_endpoint();

CREATE TRIGGER atomic_removed_database_identity AFTER DELETE ON atomic_databases FOR EACH ROW EXECUTE FUNCTION atomic_finish_removed_database_identity();

CREATE TRIGGER atomic_request_base_archive_completions_immutable BEFORE DELETE OR UPDATE ON atomic_request_base_archive_completions FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();

CREATE TRIGGER atomic_request_base_archive_nodes_immutable BEFORE DELETE OR UPDATE ON atomic_request_base_archive_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();

CREATE TRIGGER atomic_request_base_archive_nodes_open_insert BEFORE INSERT ON atomic_request_base_archive_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_completed_request_base_archive_insert();

CREATE TRIGGER atomic_request_base_archive_roots_immutable BEFORE DELETE OR UPDATE ON atomic_request_base_archive_roots FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();

CREATE TRIGGER atomic_request_base_archive_roots_open_insert BEFORE INSERT ON atomic_request_base_archive_roots FOR EACH ROW EXECUTE FUNCTION atomic_reject_completed_request_base_archive_insert();

CREATE TRIGGER atomic_request_base_archives_immutable BEFORE DELETE OR UPDATE ON atomic_request_base_archives FOR EACH ROW EXECUTE FUNCTION atomic_reject_request_base_archive_mutation();

CREATE TRIGGER atomic_request_base_archives_validate_insert BEFORE INSERT ON atomic_request_base_archives FOR EACH ROW EXECUTE FUNCTION atomic_validate_request_base_archive_insert();

CREATE TRIGGER atomic_requests_immutable BEFORE DELETE OR UPDATE ON atomic_requests FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_requests_validate_insert BEFORE INSERT ON atomic_requests FOR EACH ROW EXECUTE FUNCTION atomic_validate_request_insert();

CREATE TRIGGER atomic_semantic_commitment_nodes_admit_insert BEFORE INSERT ON atomic_semantic_commitment_nodes FOR EACH STATEMENT EXECUTE FUNCTION atomic_admit_semantic_commitment_node_insert();

CREATE TRIGGER atomic_semantic_commitment_nodes_immutable BEFORE DELETE OR UPDATE ON atomic_semantic_commitment_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_semantic_commitment_mutation();

CREATE TRIGGER atomic_semantic_commitment_roots_immutable BEFORE DELETE OR UPDATE ON atomic_semantic_commitment_roots FOR EACH ROW EXECUTE FUNCTION atomic_reject_semantic_commitment_mutation();

CREATE TRIGGER atomic_semantic_commitment_roots_validate_insert BEFORE INSERT ON atomic_semantic_commitment_roots FOR EACH ROW EXECUTE FUNCTION atomic_validate_semantic_commitment_root();

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_change_checkpoints FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_completed_excision_requests FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_databases FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_blocks FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_garbage FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_page_builds FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_page_edges FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_page_garbage FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_page_roots FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_pages FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_fulltext_projections FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_generation_excision_predicates FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_generation_request_bases FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_generation_request_tempids FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_generation_requests FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_generation_transactions FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_heads FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_index_manifests FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_index_publications FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_index_segments FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_abandonment_progress FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_activations FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_builds FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_checkpoints FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_collection_progress FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_completion_stages FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_completions FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_garbage_contents FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generation_retirements FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_log_generations FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_program_gc_candidates FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_program_generation_refs FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_program_reference_state FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_programs FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_receipt_archive_conversions FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_receipt_archive_frontier FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_remote_writer_endpoints FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_request_base_archive_completions FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_request_base_archive_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_request_base_archive_roots FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_request_base_archives FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_requests FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_semantic_commitment_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_semantic_commitment_roots FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_transaction_contents FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_transactions FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_transactor_leases FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('lease_scope', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_build_intent_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_build_intents FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_delta_headers FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_delta_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_garbage_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_live_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_live_sets FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_manifest_roots FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_manifests FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_node_blocks FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_publication_states FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_retired_nodes FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', '');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_retirement_progress FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON atomic_tree_retirements FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database('database_id', 'manifest_hash');

CREATE TRIGGER atomic_transaction_contents_immutable BEFORE DELETE OR UPDATE ON atomic_transaction_contents FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE TRIGGER atomic_transactions_immutable BEFORE DELETE OR UPDATE ON atomic_transactions FOR EACH ROW EXECUTE FUNCTION atomic_reject_log_generation_gc_mutation();

CREATE CONSTRAINT TRIGGER atomic_transactions_require_publication AFTER INSERT ON atomic_transactions DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION atomic_require_published_transaction();

CREATE TRIGGER atomic_transactions_validate_insert BEFORE INSERT ON atomic_transactions FOR EACH ROW EXECUTE FUNCTION atomic_validate_transaction_insert();

CREATE TRIGGER atomic_tree_garbage_nodes_immutable BEFORE DELETE OR UPDATE ON atomic_tree_garbage_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_manifest_fulltext_garbage AFTER DELETE ON atomic_tree_manifests FOR EACH ROW EXECUTE FUNCTION atomic_mark_fulltext_garbage();

CREATE TRIGGER atomic_tree_manifest_roots_immutable BEFORE DELETE OR UPDATE ON atomic_tree_manifest_roots FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_manifests_immutable BEFORE DELETE OR UPDATE ON atomic_tree_manifests FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_manifests_validate_insert BEFORE INSERT ON atomic_tree_manifests FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_manifest_insert();

CREATE TRIGGER atomic_tree_node_blocks_immutable BEFORE DELETE OR UPDATE ON atomic_tree_node_blocks FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_node_block_mutation();

CREATE TRIGGER atomic_tree_node_blocks_validate_insert BEFORE INSERT ON atomic_tree_node_blocks FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_node_block_insert();

CREATE TRIGGER atomic_tree_nodes_immutable BEFORE DELETE OR UPDATE ON atomic_tree_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_publication_states_immutable BEFORE DELETE OR UPDATE ON atomic_tree_publication_states FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_publication_states_scrub_excision_predecessor BEFORE INSERT ON atomic_tree_publication_states FOR EACH ROW EXECUTE FUNCTION atomic_scrub_excision_tree_predecessor();

CREATE TRIGGER atomic_tree_publications_active BEFORE INSERT ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_require_active_publication();

CREATE TRIGGER atomic_tree_publications_apply_delta AFTER INSERT ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_apply_tree_publication_delta();

CREATE TRIGGER atomic_tree_publications_immutable BEFORE DELETE OR UPDATE ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_publications_index_basis_monotonic BEFORE INSERT ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_index_basis_progress();

CREATE TRIGGER atomic_tree_publications_release_request_bases BEFORE DELETE ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_release_inactive_request_bases();

CREATE TRIGGER atomic_tree_publications_validate_delta BEFORE INSERT ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_publication_delta();

CREATE TRIGGER atomic_tree_publications_validate_insert BEFORE INSERT ON atomic_tree_publications FOR EACH ROW EXECUTE FUNCTION atomic_validate_tree_publication_insert();

CREATE TRIGGER atomic_tree_retired_nodes_immutable BEFORE DELETE OR UPDATE ON atomic_tree_retired_nodes FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retirement_progress_immutable BEFORE DELETE OR UPDATE ON atomic_tree_retirement_progress FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

CREATE TRIGGER atomic_tree_retirements_fill_generation BEFORE INSERT ON atomic_tree_retirements FOR EACH ROW EXECUTE FUNCTION atomic_fill_tree_retirement_generation();

CREATE TRIGGER atomic_tree_retirements_immutable BEFORE DELETE OR UPDATE ON atomic_tree_retirements FOR EACH ROW EXECUTE FUNCTION atomic_reject_tree_gc_mutation();

ALTER TABLE ONLY atomic_change_checkpoints
    ADD CONSTRAINT atomic_change_checkpoints_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_completed_excision_requests
    ADD CONSTRAINT atomic_completed_excision_requests_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_database_names
    ADD CONSTRAINT atomic_database_names_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_database_identities(database_id);

ALTER TABLE ONLY atomic_database_reclamation_objects
    ADD CONSTRAINT atomic_database_reclamation_objects_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_database_identities(database_id);

ALTER TABLE ONLY atomic_database_reclamation_progress
    ADD CONSTRAINT atomic_database_reclamation_progress_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_database_identities(database_id);

ALTER TABLE ONLY atomic_fulltext_page_builds
    ADD CONSTRAINT atomic_fulltext_page_builds_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_fulltext_page_edges
    ADD CONSTRAINT atomic_fulltext_page_edges_child_hash_fkey FOREIGN KEY (child_hash) REFERENCES atomic_fulltext_pages(block_hash);

ALTER TABLE ONLY atomic_fulltext_page_edges
    ADD CONSTRAINT atomic_fulltext_page_edges_parent_hash_fkey FOREIGN KEY (parent_hash) REFERENCES atomic_fulltext_pages(block_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_fulltext_page_garbage
    ADD CONSTRAINT atomic_fulltext_page_garbage_block_hash_fkey FOREIGN KEY (block_hash) REFERENCES atomic_fulltext_pages(block_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_fulltext_page_roots
    ADD CONSTRAINT atomic_fulltext_page_roots_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_fulltext_page_roots
    ADD CONSTRAINT atomic_fulltext_page_roots_root_hash_fkey FOREIGN KEY (root_hash) REFERENCES atomic_fulltext_pages(block_hash);

ALTER TABLE ONLY atomic_fulltext_projections
    ADD CONSTRAINT atomic_fulltext_projections_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_generation_excision_predicates
    ADD CONSTRAINT atomic_generation_excision_predicat_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_generation_request_bases
    ADD CONSTRAINT atomic_generation_request_bas_database_id_generation_reque_fkey FOREIGN KEY (database_id, generation, request_key_hash) REFERENCES atomic_generation_requests(database_id, generation, request_key_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_generation_request_tempids
    ADD CONSTRAINT atomic_generation_request_tem_database_id_generation_reque_fkey FOREIGN KEY (database_id, generation, request_key_hash) REFERENCES atomic_generation_requests(database_id, generation, request_key_hash);

ALTER TABLE ONLY atomic_generation_requests
    ADD CONSTRAINT atomic_generation_requests_database_id_generation_basis_t__fkey FOREIGN KEY (database_id, generation, basis_t, tx_hash) REFERENCES atomic_generation_transactions(database_id, generation, basis_t, tx_hash);

ALTER TABLE ONLY atomic_generation_transactions
    ADD CONSTRAINT atomic_generation_transactions_content_hash_fkey FOREIGN KEY (content_hash) REFERENCES atomic_transaction_contents(content_hash);

ALTER TABLE ONLY atomic_generation_transactions
    ADD CONSTRAINT atomic_generation_transactions_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_heads
    ADD CONSTRAINT atomic_heads_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_index_manifests
    ADD CONSTRAINT atomic_index_manifests_database_id_basis_t_tx_hash_fkey FOREIGN KEY (database_id, basis_t, tx_hash) REFERENCES atomic_transactions(database_id, basis_t, tx_hash);

ALTER TABLE ONLY atomic_index_manifests
    ADD CONSTRAINT atomic_index_manifests_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_index_publications
    ADD CONSTRAINT atomic_index_publications_database_id_basis_t_tx_hash_fkey FOREIGN KEY (database_id, basis_t, tx_hash) REFERENCES atomic_transactions(database_id, basis_t, tx_hash);

ALTER TABLE ONLY atomic_index_publications
    ADD CONSTRAINT atomic_index_publications_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_index_manifests(manifest_hash);

ALTER TABLE ONLY atomic_log_generation_abandonment_progress
    ADD CONSTRAINT atomic_log_generation_abandonment_p_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_activations
    ADD CONSTRAINT atomic_log_generation_activations_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_builds
    ADD CONSTRAINT atomic_log_generation_builds_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_checkpoints
    ADD CONSTRAINT atomic_log_generation_checkpoints_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generation_builds(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_collection_progress
    ADD CONSTRAINT atomic_log_generation_collection_pr_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generation_retirements(database_id, generation) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_log_generation_completion_stages
    ADD CONSTRAINT atomic_log_generation_completion_st_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generation_builds(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_completions
    ADD CONSTRAINT atomic_log_generation_completions_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generation_activations(database_id, generation);

ALTER TABLE ONLY atomic_log_generation_garbage_contents
    ADD CONSTRAINT atomic_log_generation_garbage_contents_content_hash_fkey FOREIGN KEY (content_hash) REFERENCES atomic_transaction_contents(content_hash);

ALTER TABLE ONLY atomic_log_generation_retirements
    ADD CONSTRAINT atomic_log_generation_retirem_database_id_successor_genera_fkey FOREIGN KEY (database_id, successor_generation) REFERENCES atomic_log_generation_activations(database_id, generation);

ALTER TABLE ONLY atomic_log_generations
    ADD CONSTRAINT atomic_log_generations_database_id_lineage_id_fkey FOREIGN KEY (database_id, lineage_id) REFERENCES atomic_databases(database_id, lineage_id);

ALTER TABLE ONLY atomic_program_gc_candidates
    ADD CONSTRAINT atomic_program_gc_candidates_program_hash_fkey FOREIGN KEY (program_hash) REFERENCES atomic_programs(program_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_program_generation_refs
    ADD CONSTRAINT atomic_program_generation_refs_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_program_generation_refs
    ADD CONSTRAINT atomic_program_generation_refs_program_hash_fkey FOREIGN KEY (program_hash) REFERENCES atomic_programs(program_hash);

ALTER TABLE ONLY atomic_receipt_archive_conversions
    ADD CONSTRAINT atomic_receipt_archive_conversions_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_receipt_archive_conversions
    ADD CONSTRAINT atomic_receipt_archive_conversions_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE ONLY atomic_receipt_archive_frontier
    ADD CONSTRAINT atomic_receipt_archive_frontier_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_receipt_archive_conversions(manifest_hash);

ALTER TABLE ONLY atomic_remote_writer_endpoints
    ADD CONSTRAINT atomic_remote_writer_endpoints_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_request_base_archive_completions
    ADD CONSTRAINT atomic_request_base_archive_completions_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_request_base_archives(manifest_hash);

ALTER TABLE ONLY atomic_request_base_archive_nodes
    ADD CONSTRAINT atomic_request_base_archive_nodes_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_request_base_archives(manifest_hash);

ALTER TABLE ONLY atomic_request_base_archive_roots
    ADD CONSTRAINT atomic_request_base_archive_roots_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_request_base_archives(manifest_hash);

ALTER TABLE ONLY atomic_request_base_archive_roots
    ADD CONSTRAINT atomic_request_base_archive_roots_root_hash_fkey FOREIGN KEY (root_hash) REFERENCES atomic_tree_nodes(node_hash);

ALTER TABLE ONLY atomic_request_base_archives
    ADD CONSTRAINT atomic_request_base_archives_database_id_generation_fkey FOREIGN KEY (database_id, generation) REFERENCES atomic_log_generations(database_id, generation);

ALTER TABLE ONLY atomic_requests
    ADD CONSTRAINT atomic_requests_database_id_basis_t_tx_hash_fkey FOREIGN KEY (database_id, basis_t, tx_hash) REFERENCES atomic_transactions(database_id, basis_t, tx_hash);

ALTER TABLE ONLY atomic_requests
    ADD CONSTRAINT atomic_requests_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_semantic_commitment_nodes
    ADD CONSTRAINT atomic_semantic_commitment_nodes_left_hash_fkey FOREIGN KEY (left_hash) REFERENCES atomic_semantic_commitment_nodes(node_hash);

ALTER TABLE ONLY atomic_semantic_commitment_nodes
    ADD CONSTRAINT atomic_semantic_commitment_nodes_right_hash_fkey FOREIGN KEY (right_hash) REFERENCES atomic_semantic_commitment_nodes(node_hash);

ALTER TABLE ONLY atomic_semantic_commitment_roots
    ADD CONSTRAINT atomic_semantic_commitment_roots_current_root_fkey FOREIGN KEY (current_root) REFERENCES atomic_semantic_commitment_nodes(node_hash);

ALTER TABLE ONLY atomic_semantic_commitment_roots
    ADD CONSTRAINT atomic_semantic_commitment_roots_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_transaction_contents
    ADD CONSTRAINT atomic_transaction_contents_lineage_id_fkey FOREIGN KEY (lineage_id) REFERENCES atomic_databases(lineage_id);

ALTER TABLE ONLY atomic_transactions
    ADD CONSTRAINT atomic_transactions_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_tree_build_intent_nodes
    ADD CONSTRAINT atomic_tree_build_intent_nodes_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_build_intents(manifest_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_tree_build_intents
    ADD CONSTRAINT atomic_tree_build_intents_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_tree_delta_nodes
    ADD CONSTRAINT atomic_tree_delta_nodes_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_delta_headers(manifest_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_tree_delta_nodes
    ADD CONSTRAINT atomic_tree_delta_nodes_node_hash_fkey FOREIGN KEY (node_hash) REFERENCES atomic_tree_nodes(node_hash);

ALTER TABLE ONLY atomic_tree_garbage_nodes
    ADD CONSTRAINT atomic_tree_garbage_nodes_node_hash_fkey FOREIGN KEY (node_hash) REFERENCES atomic_tree_nodes(node_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_tree_live_nodes
    ADD CONSTRAINT atomic_tree_live_nodes_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_tree_live_nodes
    ADD CONSTRAINT atomic_tree_live_nodes_node_hash_fkey FOREIGN KEY (node_hash) REFERENCES atomic_tree_nodes(node_hash);

ALTER TABLE ONLY atomic_tree_live_sets
    ADD CONSTRAINT atomic_tree_live_sets_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_tree_live_sets
    ADD CONSTRAINT atomic_tree_live_sets_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE ONLY atomic_tree_manifest_roots
    ADD CONSTRAINT atomic_tree_manifest_roots_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE ONLY atomic_tree_manifest_roots
    ADD CONSTRAINT atomic_tree_manifest_roots_root_hash_fkey FOREIGN KEY (root_hash) REFERENCES atomic_tree_nodes(node_hash);

ALTER TABLE ONLY atomic_tree_manifests
    ADD CONSTRAINT atomic_tree_manifests_database_id_fkey FOREIGN KEY (database_id) REFERENCES atomic_databases(database_id);

ALTER TABLE ONLY atomic_tree_node_blocks
    ADD CONSTRAINT atomic_tree_node_blocks_node_hash_fkey FOREIGN KEY (node_hash) REFERENCES atomic_tree_nodes(node_hash) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_tree_publication_states
    ADD CONSTRAINT atomic_tree_publication_states_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE ONLY atomic_tree_publications
    ADD CONSTRAINT atomic_tree_publications_database_id_publication_revision__fkey FOREIGN KEY (database_id, publication_revision, basis_t, tx_hash, manifest_hash) REFERENCES atomic_tree_manifests(database_id, publication_revision, basis_t, tx_hash, manifest_hash);

ALTER TABLE ONLY atomic_tree_retired_nodes
    ADD CONSTRAINT atomic_tree_retired_nodes_database_id_publication_revision_fkey FOREIGN KEY (database_id, publication_revision) REFERENCES atomic_tree_retirements(database_id, publication_revision);

ALTER TABLE ONLY atomic_tree_retired_nodes
    ADD CONSTRAINT atomic_tree_retired_nodes_node_hash_fkey FOREIGN KEY (node_hash) REFERENCES atomic_tree_nodes(node_hash);

ALTER TABLE ONLY atomic_tree_retirement_progress
    ADD CONSTRAINT atomic_tree_retirement_progre_database_id_publication_revi_fkey FOREIGN KEY (database_id, publication_revision) REFERENCES atomic_tree_retirements(database_id, publication_revision) ON DELETE CASCADE;

ALTER TABLE ONLY atomic_tree_retirement_progress
    ADD CONSTRAINT atomic_tree_retirement_progress_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE ONLY atomic_tree_retirements
    ADD CONSTRAINT atomic_tree_retirements_database_id_publication_revision_fkey FOREIGN KEY (database_id, publication_revision) REFERENCES atomic_tree_publications(database_id, publication_revision);

ALTER TABLE ONLY atomic_tree_retirements
    ADD CONSTRAINT atomic_tree_retirements_manifest_hash_fkey FOREIGN KEY (manifest_hash) REFERENCES atomic_tree_manifests(manifest_hash);

ALTER TABLE atomic_change_checkpoints ENABLE ROW LEVEL SECURITY;


CREATE POLICY atomic_own_change_checkpoints ON atomic_change_checkpoints USING ((checkpoint_owner = CURRENT_USER)) WITH CHECK ((checkpoint_owner = CURRENT_USER));

REVOKE ALL ON FUNCTION atomic_abandon_log_generation(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_abandon_log_generation_without_semantic_v14(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_activate_initial_log_generation(candidate_database_id text, candidate_generation bigint, candidate_basis bigint, candidate_head_hash bytea, candidate_state_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_activate_log_generation(candidate_database_id text, candidate_generation bigint, candidate_basis bigint, candidate_head_hash bytea, candidate_state_hash bytea, candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_apply_tree_publication_delta() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_apply_tree_publication_work(candidate_manifest_hash bytea, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_assert_excision_worker(candidate_database_id text, candidate_holder text, candidate_epoch bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_begin_receipt_archive_conversion(candidate_hash bytea, older_than_millis bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_cleanup_log_generation_build(candidate_database_id text, candidate_generation bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_fulltext_garbage(maximum_blocks bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_log_generation(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_log_generation_without_semantic_v14(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_program_garbage(older_than_millis bigint, maximum_programs bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_request_base_archive(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_request_base_archive_unconverted_v20(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_semantic_commitment_garbage(older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_semantic_commitment_generation_roots(candidate_database_id text, candidate_generation bigint, older_than_millis bigint, maximum_roots bigint, abandoned boolean) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_tree_build_intent(candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_tree_garbage(maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(candidate_database_id text, candidate_publication_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement_unbound_v13(candidate_database_id text, candidate_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement_unconverted_v22(candidate_database_id text, candidate_revision bigint, candidate_manifest_hash bytea, older_than_millis bigint, maximum_nodes bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_complete_excision_generation(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_complete_request_base_archive(candidate_database_id text, candidate_generation bigint, candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_database_reclamation_authorized() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_discover_remote_writer(requested_database text, requested_lineage text) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_enqueue_fulltext_source_pages(source bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_finish_fulltext_build(source bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_finish_receipt_archive_conversion(candidate_hash bytea, older_than_millis bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_finish_removed_database_identity() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_finish_tree_build(candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_fulltext_garbage_candidates(maximum_blocks bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_fulltext_gc_pin_key() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_heartbeat_tree_build(candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_log_generation_pin_key(candidate_database_id text, candidate_generation bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_mark_fulltext_garbage() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_prepare_database_reclamation(target text, lineage text, age_millis bigint, apply boolean) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_protect_reclaiming_database() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_publish_tree(candidate_database_id text, candidate_revision bigint, candidate_basis bigint, candidate_tx_hash bytea, candidate_manifest_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_receipt_archive_conversion_context(candidate_hash bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_completed_request_base_archive_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_database_mutation() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_fulltext_mutation() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_generation_request_base_mutation() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_request_base_archive_mutation() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_reject_tree_node_block_mutation() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_release_inactive_request_bases() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_request_base_archive_build_live(candidate_database_id text, candidate_generation bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_require_active_publication() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_retire_fulltext_build() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_runtime_excision_step(candidate_database_id text, candidate_generation bigint, candidate_holder text, candidate_epoch bigint, candidate_action text, maximum_rows bigint, candidate_basis bigint, candidate_head bytea, candidate_state bytea, candidate_manifest bytea) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_seal_restore_completions(candidate_database_id text, candidate_generation bigint, expected_count bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_semantic_commitment_gc_pin_key() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_stage_excision_completions(candidate_database_id text, candidate_generation bigint, maximum_rows bigint) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_stage_restore_completions(candidate_database_id text, candidate_generation bigint, request_ts bigint[], request_entities bigint[]) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_track_fulltext_page_reference() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_track_program_candidate() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_tree_database_build_pin_key(candidate_database_id text) FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_activation_semantic_root() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_fulltext_block_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_fulltext_page_source() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_fulltext_projection_root() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_generation_request_base_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_head_advance() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_remote_writer_endpoint() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_request_base_archive_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_tree_index_basis_progress() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_tree_manifest_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_tree_node_block_insert() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_tree_publication_delta() FROM PUBLIC;

REVOKE ALL ON FUNCTION atomic_validate_tree_publication_insert() FROM PUBLIC;

-- A new empty installation already has complete current program references.
INSERT INTO atomic_program_reference_state
    (singleton, complete, problem_code, walker_version)
VALUES (true, true, NULL, 2);

