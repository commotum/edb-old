-- Application progress is not canonical database state or a retention pin.
-- One SQL login can share a named consumer; compare-and-swap prevents lost
-- progress updates. Different logins cannot read or change each other's rows.
CREATE TABLE atomic_change_checkpoints (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id),
    consumer_name TEXT NOT NULL CHECK (octet_length(consumer_name) BETWEEN 1 AND 512),
    checkpoint_owner NAME NOT NULL DEFAULT CURRENT_USER,
    lineage_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation >= 0),
    last_t BIGINT NOT NULL CHECK (last_t >= 0),
    commit_hash BYTEA NOT NULL CHECK (octet_length(commit_hash)=32),
    revision BIGINT NOT NULL CHECK (revision >= 0),
    PRIMARY KEY(database_id, consumer_name, checkpoint_owner)
);
ALTER TABLE atomic_change_checkpoints ENABLE ROW LEVEL SECURITY;
CREATE POLICY atomic_own_change_checkpoints ON atomic_change_checkpoints
    USING (checkpoint_owner = CURRENT_USER)
    WITH CHECK (checkpoint_owner = CURRENT_USER);
REVOKE ALL ON atomic_change_checkpoints FROM PUBLIC;
