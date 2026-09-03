CREATE TABLE IF NOT EXISTS atomic_schema_migrations (
    version bigint PRIMARY KEY CHECK (version > 0),
    checksum bytea NOT NULL CHECK (octet_length(checksum) = 32),
    applied_at timestamptz NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS atomic_databases (
    database_id text PRIMARY KEY CHECK (database_id <> ''),
    bootstrap_schema bytea NOT NULL,
    bootstrap_hash bytea NOT NULL CHECK (octet_length(bootstrap_hash) = 32),
    created_at timestamptz NOT NULL DEFAULT clock_timestamp()
);

CREATE TABLE IF NOT EXISTS atomic_heads (
    database_id text PRIMARY KEY REFERENCES atomic_databases(database_id),
    basis_t bigint NOT NULL CHECK (basis_t >= 0),
    tx_hash bytea NOT NULL CHECK (octet_length(tx_hash) = 32),
    CHECK (basis_t <> 0 OR tx_hash = decode(repeat('00', 32), 'hex'))
);

CREATE TABLE IF NOT EXISTS atomic_transactions (
    database_id text NOT NULL REFERENCES atomic_databases(database_id),
    basis_t bigint NOT NULL CHECK (basis_t > 0),
    previous_hash bytea NOT NULL CHECK (octet_length(previous_hash) = 32),
    tx_hash bytea NOT NULL CHECK (octet_length(tx_hash) = 32),
    payload bytea NOT NULL CHECK (octet_length(payload) >= 48),
    committed_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, basis_t),
    UNIQUE (database_id, tx_hash),
    UNIQUE (database_id, basis_t, tx_hash)
);

CREATE TABLE IF NOT EXISTS atomic_requests (
    database_id text NOT NULL REFERENCES atomic_databases(database_id),
    request_key text NOT NULL CHECK (request_key <> ''),
    request_digest bytea NOT NULL CHECK (octet_length(request_digest) = 32),
    basis_t bigint NOT NULL CHECK (basis_t > 0),
    tx_hash bytea NOT NULL CHECK (octet_length(tx_hash) = 32),
    committed_at timestamptz NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (database_id, request_key),
    UNIQUE (database_id, basis_t),
    FOREIGN KEY (database_id, basis_t, tx_hash)
        REFERENCES atomic_transactions(database_id, basis_t, tx_hash)
);

CREATE OR REPLACE FUNCTION atomic_reject_immutable_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Atomic committed records are immutable'
        USING ERRCODE = '55000';
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_transaction_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    current_basis bigint;
    current_hash bytea;
BEGIN
    SELECT basis_t, tx_hash INTO current_basis, current_hash
      FROM atomic_heads
     WHERE database_id = NEW.database_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Atomic database head does not exist'
            USING ERRCODE = '23503';
    END IF;
    IF NEW.basis_t <> current_basis + 1 OR NEW.previous_hash <> current_hash THEN
        RAISE EXCEPTION 'Atomic transaction does not extend the current head'
            USING ERRCODE = '40001';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_request_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_transactions
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic request does not identify its transaction'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_head_advance()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.database_id <> OLD.database_id
       OR NEW.basis_t <> OLD.basis_t + 1 THEN
        RAISE EXCEPTION 'Atomic head must advance by exactly one basis'
            USING ERRCODE = '40001';
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM atomic_transactions
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND previous_hash = OLD.tx_hash
           AND tx_hash = NEW.tx_hash
    ) OR NOT EXISTS (
        SELECT 1 FROM atomic_requests
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic head does not identify a complete transaction publication'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_require_published_transaction()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM atomic_heads
         WHERE database_id = NEW.database_id
           AND basis_t = NEW.basis_t
           AND tx_hash = NEW.tx_hash
    ) THEN
        RAISE EXCEPTION 'Atomic transaction was not published by its commit'
            USING ERRCODE = '40001';
    END IF;
    RETURN NULL;
END;
$$;

DROP TRIGGER IF EXISTS atomic_databases_immutable ON atomic_databases;
CREATE TRIGGER atomic_databases_immutable
BEFORE UPDATE OR DELETE ON atomic_databases
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_transactions_validate_insert ON atomic_transactions;
CREATE TRIGGER atomic_transactions_validate_insert
BEFORE INSERT ON atomic_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_validate_transaction_insert();

DROP TRIGGER IF EXISTS atomic_transactions_immutable ON atomic_transactions;
CREATE TRIGGER atomic_transactions_immutable
BEFORE UPDATE OR DELETE ON atomic_transactions
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_requests_validate_insert ON atomic_requests;
CREATE TRIGGER atomic_requests_validate_insert
BEFORE INSERT ON atomic_requests
FOR EACH ROW EXECUTE FUNCTION atomic_validate_request_insert();

DROP TRIGGER IF EXISTS atomic_requests_immutable ON atomic_requests;
CREATE TRIGGER atomic_requests_immutable
BEFORE UPDATE OR DELETE ON atomic_requests
FOR EACH ROW EXECUTE FUNCTION atomic_reject_immutable_mutation();

DROP TRIGGER IF EXISTS atomic_heads_validate_advance ON atomic_heads;
CREATE TRIGGER atomic_heads_validate_advance
BEFORE UPDATE ON atomic_heads
FOR EACH ROW EXECUTE FUNCTION atomic_validate_head_advance();

DROP TRIGGER IF EXISTS atomic_transactions_require_publication ON atomic_transactions;
CREATE CONSTRAINT TRIGGER atomic_transactions_require_publication
AFTER INSERT ON atomic_transactions
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW EXECUTE FUNCTION atomic_require_published_transaction();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_databases FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_transactions FROM PUBLIC;
REVOKE UPDATE, DELETE, TRUNCATE ON atomic_requests FROM PUBLIC;
