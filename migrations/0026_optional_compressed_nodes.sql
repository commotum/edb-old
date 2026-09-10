-- Optional transfer/cache representations. Canonical atomic_tree_nodes rows
-- remain the sole content authority for publication, archives, and GC.
CREATE TABLE atomic_tree_node_blocks (
    node_hash BYTEA PRIMARY KEY
        REFERENCES atomic_tree_nodes(node_hash) ON DELETE CASCADE,
    canonical_bytes BIGINT NOT NULL CHECK (canonical_bytes BETWEEN 1 AND 67108864),
    physical_hash BYTEA NOT NULL CHECK (octet_length(physical_hash) = 32),
    physical_payload BYTEA NOT NULL
        CHECK (octet_length(physical_payload) > 0
           AND octet_length(physical_payload) < canonical_bytes),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE FUNCTION atomic_validate_tree_node_block_insert()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
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

CREATE FUNCTION atomic_reject_tree_node_block_mutation()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    -- The owner can discard an optional bad projection. Canonical-node GC
    -- also removes it through the FK cascade; runtime writers cannot mutate it.
    IF TG_OP='DELETE' AND current_user=relation_owner THEN RETURN OLD; END IF;
    RAISE EXCEPTION 'Atomic compressed node representations are immutable'
        USING ERRCODE='55000';
END;
$$;

CREATE TRIGGER atomic_tree_node_blocks_validate_insert
BEFORE INSERT ON atomic_tree_node_blocks FOR EACH ROW
EXECUTE FUNCTION atomic_validate_tree_node_block_insert();
CREATE TRIGGER atomic_tree_node_blocks_immutable
BEFORE UPDATE OR DELETE ON atomic_tree_node_blocks FOR EACH ROW
EXECUTE FUNCTION atomic_reject_tree_node_block_mutation();

REVOKE ALL ON atomic_tree_node_blocks FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_tree_node_block_insert() FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_reject_tree_node_block_mutation() FROM PUBLIC;
