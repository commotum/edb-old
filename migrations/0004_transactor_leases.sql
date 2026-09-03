CREATE TABLE IF NOT EXISTS atomic_transactor_leases (
    lease_scope TEXT PRIMARY KEY CHECK (lease_scope <> ''),
    holder_id TEXT NOT NULL CHECK (holder_id <> ''),
    epoch BIGINT NOT NULL CHECK (epoch > 0),
    expires_at TIMESTAMPTZ NOT NULL
);
