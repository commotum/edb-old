-- Runtime writers may execute only a committed-request excision lifecycle.
-- They remain trusted decoders of A=15 facts and frozen Rust execution plans:
-- SQL checks coordinates/ownership, not the semantic meaning of those bytes.
-- No generic restore, generation activation, abandonment or GC grant is added.
CREATE FUNCTION atomic_assert_excision_worker(
    candidate_database_id TEXT, candidate_holder TEXT, candidate_epoch BIGINT
)
RETURNS VOID LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
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

CREATE FUNCTION atomic_runtime_excision_step(
    candidate_database_id TEXT, candidate_generation BIGINT,
    candidate_holder TEXT, candidate_epoch BIGINT, candidate_action TEXT,
    maximum_rows BIGINT, candidate_basis BIGINT, candidate_head BYTEA,
    candidate_state BYTEA, candidate_manifest BYTEA
)
RETURNS TABLE(rows_advanced BIGINT, is_complete BOOLEAN)
LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
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

DO $$ BEGIN
    EXECUTE format('ALTER FUNCTION %I.atomic_assert_excision_worker(TEXT,TEXT,BIGINT) SET search_path TO %I,pg_catalog,pg_temp',current_schema(),current_schema());
    EXECUTE format('ALTER FUNCTION %I.atomic_runtime_excision_step(TEXT,BIGINT,TEXT,BIGINT,TEXT,BIGINT,BIGINT,BYTEA,BYTEA,BYTEA) SET search_path TO %I,pg_catalog,pg_temp',current_schema(),current_schema());
END $$;
REVOKE ALL ON FUNCTION atomic_assert_excision_worker(TEXT,TEXT,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_runtime_excision_step(TEXT,BIGINT,TEXT,BIGINT,TEXT,BIGINT,BIGINT,BYTEA,BYTEA,BYTEA) FROM PUBLIC;
