-- Discardable, manifest-bound search projections. Canonical datom roots and
-- their checksums are unchanged. Header deletion is a one-row FK cascade;
-- potentially numerous orphan blocks are reclaimed in explicit bounded work.
CREATE TABLE atomic_fulltext_blocks (
    manifest_hash BYTEA NOT NULL CHECK (octet_length(manifest_hash)=32),
    block_hash BYTEA NOT NULL CHECK (octet_length(block_hash)=32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) BETWEEN 1 AND 67125248),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (manifest_hash, block_hash),
    CHECK (pg_catalog.sha256(payload)=block_hash)
);
CREATE TABLE atomic_fulltext_projections (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE,
    analyzer_version INTEGER NOT NULL CHECK (analyzer_version>0),
    root_hash BYTEA NOT NULL CHECK (octet_length(root_hash)=32),
    header_hash BYTEA NOT NULL CHECK (octet_length(header_hash)=32),
    header BYTEA NOT NULL CHECK (octet_length(header) BETWEEN 1 AND 1024),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    FOREIGN KEY (manifest_hash,root_hash) REFERENCES atomic_fulltext_blocks(manifest_hash,block_hash),
    CHECK (pg_catalog.sha256(header)=header_hash)
);
CREATE TABLE atomic_fulltext_garbage (
    manifest_hash BYTEA PRIMARY KEY CHECK (octet_length(manifest_hash)=32)
);

CREATE FUNCTION atomic_mark_fulltext_garbage()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    IF EXISTS (SELECT 1 FROM atomic_fulltext_blocks WHERE manifest_hash=OLD.manifest_hash) THEN
        INSERT INTO atomic_fulltext_garbage(manifest_hash) VALUES(OLD.manifest_hash) ON CONFLICT DO NOTHING;
    END IF;
    RETURN OLD;
END;
$$;
CREATE TRIGGER atomic_tree_manifest_fulltext_garbage AFTER DELETE ON atomic_tree_manifests
FOR EACH ROW EXECUTE FUNCTION atomic_mark_fulltext_garbage();

CREATE FUNCTION atomic_validate_fulltext_block_insert()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=NEW.manifest_hash)
       OR EXISTS (SELECT 1 FROM atomic_tree_retirement_progress WHERE manifest_hash=NEW.manifest_hash) THEN
        RAISE EXCEPTION 'Atomic fulltext source manifest is unavailable' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_fulltext_blocks_validate_insert
BEFORE INSERT ON atomic_fulltext_blocks FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_block_insert();

CREATE FUNCTION atomic_reject_fulltext_mutation()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF TG_OP='DELETE' AND current_user=relation_owner THEN RETURN OLD; END IF;
    RAISE EXCEPTION 'Atomic fulltext content is immutable' USING ERRCODE='55000';
END;
$$;
CREATE TRIGGER atomic_fulltext_blocks_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_blocks
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();
CREATE TRIGGER atomic_fulltext_projections_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_projections
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

CREATE FUNCTION atomic_collect_fulltext_garbage(maximum_blocks BIGINT)
RETURNS BIGINT LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE removed BIGINT;
BEGIN
    IF maximum_blocks<1 OR maximum_blocks>4096 THEN
        RAISE EXCEPTION 'Atomic fulltext garbage batch must be between 1 and 4096' USING ERRCODE='22023';
    END IF;
    WITH candidates AS MATERIALIZED (
        SELECT b.manifest_hash,b.block_hash FROM atomic_fulltext_garbage g
        JOIN atomic_fulltext_blocks b USING(manifest_hash)
        WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_manifests m WHERE m.manifest_hash=b.manifest_hash)
        ORDER BY b.manifest_hash,b.block_hash LIMIT maximum_blocks FOR UPDATE OF b SKIP LOCKED
    ) DELETE FROM atomic_fulltext_blocks b USING candidates c
      WHERE b.manifest_hash=c.manifest_hash AND b.block_hash=c.block_hash;
    GET DIAGNOSTICS removed=ROW_COUNT;
    DELETE FROM atomic_fulltext_garbage g WHERE g.manifest_hash IN (
        SELECT candidate.manifest_hash FROM atomic_fulltext_garbage candidate
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_blocks b WHERE b.manifest_hash=candidate.manifest_hash)
        ORDER BY candidate.manifest_hash LIMIT maximum_blocks
    );
    RETURN removed;
END;
$$;
REVOKE ALL ON atomic_fulltext_blocks,atomic_fulltext_projections,atomic_fulltext_garbage FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_validate_fulltext_block_insert(),atomic_reject_fulltext_mutation(),atomic_collect_fulltext_garbage(BIGINT),atomic_mark_fulltext_garbage() FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_collect_fulltext_garbage(BIGINT) TO CURRENT_USER;
