-- Every non-ordinary head replacement is represented by one immutable log
-- generation activation. Require its exact PostgreSQL-resident semantic root
-- before the activation row can publish. The activation and head mutation are
-- in one SQL transaction, so this trigger closes coordinate-less cutovers
-- without coupling ordinary per-transaction head advances to this migration.
CREATE OR REPLACE FUNCTION atomic_validate_activation_semantic_root()
RETURNS trigger
LANGUAGE plpgsql
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

DROP TRIGGER IF EXISTS atomic_log_generation_activations_semantic_root
    ON atomic_log_generation_activations;
CREATE TRIGGER atomic_log_generation_activations_semantic_root
BEFORE INSERT ON atomic_log_generation_activations
FOR EACH ROW EXECUTE FUNCTION atomic_validate_activation_semantic_root();

REVOKE ALL ON FUNCTION atomic_validate_activation_semantic_root() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_validate_activation_semantic_root() TO CURRENT_USER;

-- A positive log generation may legitimately be active at genesis before an
-- application schema or user transaction exists. Its canonical genesis
-- datoms are still a real database value and now have an exact semantic root,
-- so permit a normal eight-index native tree at basis zero instead of keeping
-- a special process-resident bootstrap representation.
ALTER TABLE atomic_tree_manifests
    DROP CONSTRAINT atomic_tree_manifests_basis_t_check,
    ADD CONSTRAINT atomic_tree_manifests_basis_t_check CHECK (basis_t >= 0);
ALTER TABLE atomic_tree_publications
    DROP CONSTRAINT atomic_tree_publications_basis_t_check,
    ADD CONSTRAINT atomic_tree_publications_basis_t_check CHECK (basis_t >= 0);

CREATE OR REPLACE FUNCTION atomic_validate_tree_manifest_insert()
RETURNS trigger
LANGUAGE plpgsql
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
