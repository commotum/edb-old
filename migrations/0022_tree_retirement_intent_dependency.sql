-- A sealed tree-build intent remains an exact liveness owner until its
-- bounded upload ledger has been drained. Migration 17 protected request-base
-- archives at this same fixed path; retain that check and add the independent
-- build-intent dependency. Acquiring both advisory fences closes the gap
-- between checking for an intent and deleting the immutable publication.
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

REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT)
    FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_retirement(TEXT, BIGINT, BYTEA, BIGINT, BIGINT)
    TO CURRENT_USER;

-- Version 13 intended an activated/no-publication intent to be protected only
-- while it was a pre-publication state-0/1 build. Its candidate query already
-- admits state 2 after the published root is retired, but the fixed-path
-- function's broader guard rejected that valid cleanup forever. Restate the
-- function with the guard scoped to the pre-publication states so catalogs
-- affected before the retirement dependency above can converge after upgrade.
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

REVOKE ALL ON FUNCTION atomic_collect_tree_build_intent(BYTEA, BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_build_intent(BYTEA, BIGINT, BIGINT)
    TO CURRENT_USER;
