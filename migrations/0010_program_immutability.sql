-- Program hashes are durable content identities.  Once admitted, the hash,
-- kind, arity, payload, and creation record must remain one immutable fact.
-- Exceptional maintenance and fault injection must opt into PostgreSQL's
-- trigger-bypass role explicitly and restore normal trigger behavior at once.
CREATE OR REPLACE FUNCTION atomic_reject_program_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Atomic program blobs are immutable'
        USING ERRCODE = '55000';
END;
$$;

DROP TRIGGER IF EXISTS atomic_programs_immutable ON atomic_programs;
CREATE TRIGGER atomic_programs_immutable
BEFORE UPDATE OR DELETE ON atomic_programs
FOR EACH ROW EXECUTE FUNCTION atomic_reject_program_mutation();

DROP TRIGGER IF EXISTS atomic_programs_reject_truncate ON atomic_programs;
CREATE TRIGGER atomic_programs_reject_truncate
BEFORE TRUNCATE ON atomic_programs
FOR EACH STATEMENT EXECUTE FUNCTION atomic_reject_program_mutation();

REVOKE UPDATE, DELETE, TRUNCATE ON atomic_programs FROM PUBLIC;
