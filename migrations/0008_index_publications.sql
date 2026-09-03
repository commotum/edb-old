-- Index segments and manifests are derived, immutable values.  A manifest is
-- eligible for recovery only after it has also been named by this small,
-- append-only publication relation.  This is the PostgreSQL counterpart of
-- Datomic's conditionally adopted index-root pointer: rewriting a coherent
-- derived tree and all of its checksums cannot rewrite the authoritative root
-- publication that selected the original manifest.
CREATE TABLE IF NOT EXISTS atomic_index_publications (
    database_id text NOT NULL,
    basis_t bigint NOT NULL CHECK (basis_t > 0),
    tx_hash bytea NOT NULL CHECK (octet_length(tx_hash) = 32),
    manifest_hash bytea NOT NULL CHECK (octet_length(manifest_hash) = 32),
    published_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, basis_t),
    FOREIGN KEY (database_id, basis_t, tx_hash)
        REFERENCES atomic_transactions(database_id, basis_t, tx_hash),
    FOREIGN KEY (manifest_hash)
        REFERENCES atomic_index_manifests(manifest_hash)
);

DROP TRIGGER IF EXISTS atomic_index_publications_immutable
    ON atomic_index_publications;
CREATE TRIGGER atomic_index_publications_immutable
BEFORE UPDATE OR DELETE ON atomic_index_publications
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_index_publications FROM PUBLIC;
