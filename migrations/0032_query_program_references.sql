-- Walker 2 includes fixed program literals and lookup dependencies throughout
-- native query ASTs and general query values. Old completeness is not proof
-- of this stronger traversal. Rust authenticates/rebuilds references in the
-- same migration transaction before committing the new schema boundary.
-- Schema 32 fences older writers whose walker could omit these dependencies.
UPDATE atomic_program_reference_state
   SET complete = false,
       problem_code = 'program/obsolete-reference-walker',
       updated_at = clock_timestamp();

-- Keep the callable SQL authority fail-closed too: operations inventory is not
-- the deletion boundary. A future walker is not interchangeable with this one.
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

REVOKE ALL ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_program_garbage(BIGINT, BIGINT) TO CURRENT_USER;
