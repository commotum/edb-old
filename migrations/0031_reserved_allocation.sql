-- ATLC v2 authenticates the independent reserved-identity frontier in the
-- immutable transaction content. Existing v1 content and receipts are untouched.
-- The schema boundary excludes old writers before any v2 content is admitted.
ALTER TABLE atomic_transaction_contents
    DROP CONSTRAINT atomic_transaction_contents_envelope_version_check;
ALTER TABLE atomic_transaction_contents
    ADD CONSTRAINT atomic_transaction_contents_envelope_version_check
    CHECK (envelope_version IN (1, 2));

-- The relational version is a projection of the envelope, never an independent
-- caller setting. This also keeps restore/COW insertion paths uniform.
CREATE FUNCTION atomic_content_envelope_version() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    IF octet_length(NEW.payload) < 58
       OR substring(NEW.payload FROM 1 FOR 4) <> decode('41544c43', 'hex') THEN
        RAISE EXCEPTION 'invalid transaction content header';
    END IF;
    NEW.envelope_version := (get_byte(NEW.payload, 4) * 256 + get_byte(NEW.payload, 5))::smallint;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_content_envelope_version
BEFORE INSERT ON atomic_transaction_contents
FOR EACH ROW EXECUTE FUNCTION atomic_content_envelope_version();
