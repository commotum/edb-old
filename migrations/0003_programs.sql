CREATE TABLE IF NOT EXISTS atomic_programs (
    program_hash BYTEA PRIMARY KEY CHECK (octet_length(program_hash) = 32),
    kind SMALLINT NOT NULL CHECK (kind BETWEEN 0 AND 2),
    arity SMALLINT NOT NULL CHECK (arity BETWEEN 0 AND 10),
    payload BYTEA NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT transaction_timestamp()
);

CREATE TABLE IF NOT EXISTS atomic_program_versions (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id) ON DELETE CASCADE,
    name TEXT NOT NULL CHECK (name <> ''),
    version BIGINT NOT NULL CHECK (version > 0),
    program_hash BYTEA NOT NULL REFERENCES atomic_programs(program_hash),
    PRIMARY KEY (database_id, name, version)
);

CREATE TABLE IF NOT EXISTS atomic_active_programs (
    database_id TEXT NOT NULL REFERENCES atomic_databases(database_id) ON DELETE CASCADE,
    name TEXT NOT NULL CHECK (name <> ''),
    version BIGINT NOT NULL CHECK (version > 0),
    program_hash BYTEA NOT NULL CHECK (octet_length(program_hash) = 32),
    PRIMARY KEY (database_id, name),
    FOREIGN KEY (database_id, name, version)
        REFERENCES atomic_program_versions(database_id, name, version),
    FOREIGN KEY (program_hash) REFERENCES atomic_programs(program_hash)
);
