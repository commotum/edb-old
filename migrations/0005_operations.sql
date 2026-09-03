CREATE TABLE IF NOT EXISTS atomic_database_generations (
    database_id TEXT PRIMARY KEY REFERENCES atomic_databases(database_id) ON DELETE CASCADE,
    excision_generation BIGINT NOT NULL DEFAULT 0 CHECK (excision_generation >= 0)
);

INSERT INTO atomic_database_generations (database_id)
SELECT database_id FROM atomic_databases
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS atomic_excisions (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    excision_id TEXT NOT NULL CHECK (excision_id <> ''),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    backup_basis_t BIGINT NOT NULL CHECK (backup_basis_t >= 0),
    backup_manifest_hash BYTEA NOT NULL CHECK (octet_length(backup_manifest_hash) = 32),
    target_kind SMALLINT NOT NULL CHECK (target_kind IN (0, 1)),
    target_id BIGINT NOT NULL CHECK (target_id >= 0),
    attribute_ids INTEGER[] NOT NULL DEFAULT '{}',
    before_t BIGINT,
    removed_datoms BIGINT NOT NULL CHECK (removed_datoms >= 0),
    old_head_hash BYTEA NOT NULL CHECK (octet_length(old_head_hash) = 32),
    new_head_hash BYTEA NOT NULL CHECK (octet_length(new_head_hash) = 32),
    generation BIGINT NOT NULL CHECK (generation > 0),
    PRIMARY KEY (database_id, excision_id),
    UNIQUE (database_id, generation)
);

DROP TRIGGER IF EXISTS atomic_excisions_immutable ON atomic_excisions;
CREATE TRIGGER atomic_excisions_immutable
BEFORE UPDATE OR DELETE ON atomic_excisions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_excisions FROM PUBLIC;
