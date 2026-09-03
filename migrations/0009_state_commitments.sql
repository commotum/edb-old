-- A transaction hash commits to the chronological delta, while this digest
-- commits to the canonical materialized database value after that delta.  A
-- zero value marks pre-migration rows: they remain recoverable from the log
-- but cannot authenticate a persistent index base.
ALTER TABLE atomic_transactions
    ADD COLUMN IF NOT EXISTS state_hash bytea NOT NULL
    DEFAULT decode(repeat('00', 32), 'hex');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM pg_constraint
         WHERE conname = 'atomic_transactions_state_hash_length'
           AND conrelid = 'atomic_transactions'::regclass
    ) THEN
        ALTER TABLE atomic_transactions
            ADD CONSTRAINT atomic_transactions_state_hash_length
            CHECK (octet_length(state_hash) = 32);
    END IF;
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
    IF NEW.state_hash = decode(repeat('00', 32), 'hex') THEN
        RAISE EXCEPTION 'New Atomic transactions require a state commitment'
            USING ERRCODE = '55000';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_validate_index_publication()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
          FROM atomic_transactions t
         WHERE t.database_id = NEW.database_id
           AND t.basis_t = NEW.basis_t
           AND t.tx_hash = NEW.tx_hash
           AND t.state_hash <> decode(repeat('00', 32), 'hex')
    ) OR NOT EXISTS (
        SELECT 1
          FROM atomic_index_manifests m
         WHERE m.database_id = NEW.database_id
           AND m.basis_t = NEW.basis_t
           AND m.tx_hash = NEW.tx_hash
           AND m.manifest_hash = NEW.manifest_hash
    ) THEN
        RAISE EXCEPTION 'Atomic index publication must bind one committed state and manifest'
            USING ERRCODE = '23503';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS atomic_index_publications_validate_insert
    ON atomic_index_publications;
CREATE TRIGGER atomic_index_publications_validate_insert
BEFORE INSERT ON atomic_index_publications
FOR EACH ROW EXECUTE FUNCTION atomic_validate_index_publication();
