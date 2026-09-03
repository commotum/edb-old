CREATE TABLE IF NOT EXISTS atomic_index_segments (
    segment_hash bytea PRIMARY KEY CHECK (octet_length(segment_hash) = 32),
    payload bytea NOT NULL CHECK (octet_length(payload) >= 48),
    created_at timestamptz NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS atomic_index_manifests (
    database_id text NOT NULL REFERENCES atomic_databases(database_id),
    basis_t bigint NOT NULL CHECK (basis_t > 0),
    tx_hash bytea NOT NULL CHECK (octet_length(tx_hash) = 32),
    manifest_hash bytea NOT NULL CHECK (octet_length(manifest_hash) = 32),
    payload bytea NOT NULL CHECK (octet_length(payload) >= 48),
    created_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, basis_t),
    UNIQUE (manifest_hash),
    FOREIGN KEY (database_id, basis_t, tx_hash)
        REFERENCES atomic_transactions(database_id, basis_t, tx_hash)
);

CREATE OR REPLACE FUNCTION atomic_reject_segment_conflict()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.payload <> OLD.payload THEN
        RAISE EXCEPTION 'Atomic segment hash is bound to different bytes'
            USING ERRCODE = '55000';
    END IF;
    RETURN OLD;
END;
$$;

DROP TRIGGER IF EXISTS atomic_index_segments_immutable ON atomic_index_segments;
CREATE TRIGGER atomic_index_segments_immutable
BEFORE UPDATE OR DELETE ON atomic_index_segments
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_index_manifests_immutable ON atomic_index_manifests;
CREATE TRIGGER atomic_index_manifests_immutable
BEFORE UPDATE OR DELETE ON atomic_index_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_index_segments FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_index_manifests FROM PUBLIC;
