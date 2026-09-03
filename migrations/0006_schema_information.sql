-- Version 3 stores one authoritative t=0 information set. The basis-zero
-- head is its content hash, so every positive transaction hash transitively
-- commits the exact bootstrap facts it was assessed against.
-- Atomic's old v2 rows mixed encoded Schema state with log datoms. There is
-- no sound in-place reinterpretation, so fail before changing catalog shape.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM atomic_databases) THEN
        RAISE EXCEPTION
            'Atomic v2 databases require explicit export/rebuild before schema-information migration'
            USING ERRCODE = '55000';
    END IF;
END;
$$;

ALTER TABLE atomic_databases RENAME COLUMN bootstrap_schema TO genesis;
ALTER TABLE atomic_databases RENAME COLUMN bootstrap_hash TO genesis_hash;

ALTER TABLE atomic_heads DROP CONSTRAINT atomic_heads_check;

CREATE OR REPLACE FUNCTION atomic_validate_head_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    expected_hash bytea;
BEGIN
    IF NEW.basis_t <> 0 THEN
        RAISE EXCEPTION 'Atomic initial head must be basis zero'
            USING ERRCODE = '23514';
    END IF;
    SELECT genesis_hash INTO expected_hash
      FROM atomic_databases
     WHERE database_id = NEW.database_id;
    IF NOT FOUND OR NEW.tx_hash <> expected_hash THEN
        RAISE EXCEPTION 'Atomic basis-zero head must identify database genesis'
            USING ERRCODE = '23514';
    END IF;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS atomic_heads_validate_insert ON atomic_heads;
CREATE TRIGGER atomic_heads_validate_insert
BEFORE INSERT ON atomic_heads
FOR EACH ROW EXECUTE FUNCTION atomic_validate_head_insert();
