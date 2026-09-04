-- PostgreSQL-resident form of the packing-independent semantic-state v2
-- commitment.  The logical node hash remains byte-for-byte identical to the
-- in-memory Merkle treap; this table moves ownership of immutable nodes out of
-- the transaction service heap without inventing another state identity.
CREATE TABLE atomic_semantic_commitment_nodes (
    node_hash BYTEA PRIMARY KEY CHECK (octet_length(node_hash) = 32),
    -- Strict ATSC v1 payload. Rust authenticates and decodes it before use.
    payload BYTEA NOT NULL CHECK (octet_length(payload) > 0),
    left_hash BYTEA REFERENCES atomic_semantic_commitment_nodes(node_hash),
    right_hash BYTEA REFERENCES atomic_semantic_commitment_nodes(node_hash),
    subtree_count BIGINT NOT NULL CHECK (subtree_count > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK (left_hash IS NULL OR octet_length(left_hash) = 32),
    CHECK (right_hash IS NULL OR octet_length(right_hash) = 32)
);

CREATE INDEX atomic_semantic_commitment_nodes_created_at
    ON atomic_semantic_commitment_nodes(created_at);

-- A coordinate is immutable and generation-qualified because excision and
-- point restore may produce a different authenticated value at the same t.
-- NULL is the canonical empty-set root; every non-empty root has an FK-backed
-- content node. `state_hash` remains the authoritative digest already bound
-- into the corresponding transaction membership.
CREATE TABLE atomic_semantic_commitment_roots (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    generation BIGINT NOT NULL CHECK (generation >= 0),
    basis_t BIGINT NOT NULL CHECK (basis_t >= 0),
    tx_hash BYTEA NOT NULL CHECK (octet_length(tx_hash) = 32),
    state_hash BYTEA NOT NULL CHECK (
        octet_length(state_hash) = 32
        AND state_hash <> decode(repeat('00', 32), 'hex')
    ),
    eidx_frontier BIGINT NOT NULL CHECK (eidx_frontier > 0),
    commitment_version SMALLINT NOT NULL CHECK (commitment_version = 2),
    current_root BYTEA REFERENCES atomic_semantic_commitment_nodes(node_hash),
    current_count BIGINT NOT NULL CHECK (current_count >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, generation, basis_t),
    UNIQUE (database_id, generation, tx_hash),
    CHECK ((current_count = 0 AND current_root IS NULL)
        OR (current_count > 0 AND current_root IS NOT NULL)),
    CHECK (current_root IS NULL OR octet_length(current_root) = 32)
);

CREATE INDEX atomic_semantic_commitment_roots_node
    ON atomic_semantic_commitment_roots(current_root)
    WHERE current_root IS NOT NULL;

CREATE OR REPLACE FUNCTION atomic_validate_semantic_commitment_root()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    authoritative_hash BYTEA;
    authoritative_state BYTEA;
    authoritative_frontier BIGINT;
    stored_count BIGINT;
BEGIN
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

DROP TRIGGER IF EXISTS atomic_semantic_commitment_nodes_immutable
    ON atomic_semantic_commitment_nodes;
CREATE TRIGGER atomic_semantic_commitment_nodes_immutable
BEFORE UPDATE OR DELETE ON atomic_semantic_commitment_nodes
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_semantic_commitment_roots_validate_insert
    ON atomic_semantic_commitment_roots;
CREATE TRIGGER atomic_semantic_commitment_roots_validate_insert
BEFORE INSERT ON atomic_semantic_commitment_roots
FOR EACH ROW EXECUTE FUNCTION atomic_validate_semantic_commitment_root();

DROP TRIGGER IF EXISTS atomic_semantic_commitment_roots_immutable
    ON atomic_semantic_commitment_roots;
CREATE TRIGGER atomic_semantic_commitment_roots_immutable
BEFORE UPDATE OR DELETE ON atomic_semantic_commitment_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_semantic_commitment_nodes FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_semantic_commitment_roots FROM PUBLIC;
