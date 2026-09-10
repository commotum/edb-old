-- A sidecar root owns an immutable page DAG, independently of the publication
-- that first wrote each page. Legacy source-owned blocks remain readable.
CREATE TABLE atomic_fulltext_pages (
    block_hash BYTEA PRIMARY KEY CHECK (octet_length(block_hash)=32),
    payload BYTEA NOT NULL CHECK (octet_length(payload) BETWEEN 1 AND 67125248),
    -- Deliberately not an FK: a successor may retain this page after its
    -- original canonical source has retired.
    created_for BYTEA NOT NULL CHECK (octet_length(created_for)=32),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CHECK (pg_catalog.sha256(payload)=block_hash)
);
CREATE INDEX atomic_fulltext_pages_created_for ON atomic_fulltext_pages(created_for);
CREATE TABLE atomic_fulltext_page_edges (
    parent_hash BYTEA NOT NULL REFERENCES atomic_fulltext_pages(block_hash) ON DELETE CASCADE,
    child_hash BYTEA NOT NULL REFERENCES atomic_fulltext_pages(block_hash),
    PRIMARY KEY (parent_hash,child_hash),
    CHECK (parent_hash<>child_hash)
);
CREATE INDEX atomic_fulltext_page_edges_child ON atomic_fulltext_page_edges(child_hash);
CREATE TABLE atomic_fulltext_page_roots (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE,
    root_hash BYTEA NOT NULL REFERENCES atomic_fulltext_pages(block_hash)
);
CREATE INDEX atomic_fulltext_page_roots_hash ON atomic_fulltext_page_roots(root_hash);
-- One persisted guard protects interrupted uploads, not one membership per
-- corpus page. An active shared GC fence additionally protects reused orphan
-- hashes whose first uploader belonged to a different source.
CREATE TABLE atomic_fulltext_page_builds (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash) ON DELETE CASCADE
);
-- Only potentially rootless pages enter this indexed frontier. Routine GC
-- never discovers candidates by walking the live corpus.
CREATE TABLE atomic_fulltext_page_garbage (
    block_hash BYTEA PRIMARY KEY REFERENCES atomic_fulltext_pages(block_hash) ON DELETE CASCADE
);

CREATE FUNCTION atomic_fulltext_gc_pin_key()
RETURNS BIGINT LANGUAGE sql STABLE PARALLEL SAFE SET search_path FROM CURRENT AS $$
    SELECT ('x'||substr(encode(pg_catalog.sha256(convert_to(
        'atomic/fulltext-page-gc/v1/'||'atomic_fulltext_pages'::regclass::oid::text,
        'UTF8')),'hex'),1,16))::bit(64)::bigint
$$;

CREATE FUNCTION atomic_validate_fulltext_page_source()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE source BYTEA;
BEGIN
    IF TG_TABLE_NAME='atomic_fulltext_pages' THEN
        source := NEW.created_for;
    ELSE
        source := NEW.manifest_hash;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=source)
       OR EXISTS (SELECT 1 FROM atomic_tree_retirement_progress WHERE manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext source manifest is unavailable' USING ERRCODE='23503';
    END IF;
    IF TG_TABLE_NAME='atomic_fulltext_pages'
       AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds WHERE manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext page upload has no build guard' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_fulltext_pages_validate_insert BEFORE INSERT ON atomic_fulltext_pages
FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();
CREATE TRIGGER atomic_fulltext_page_roots_validate_insert BEFORE INSERT ON atomic_fulltext_page_roots
FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();
CREATE TRIGGER atomic_fulltext_page_builds_validate_insert BEFORE INSERT ON atomic_fulltext_page_builds
FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_page_source();

CREATE TRIGGER atomic_fulltext_pages_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_pages
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();
CREATE TRIGGER atomic_fulltext_page_edges_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_page_edges
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();
CREATE TRIGGER atomic_fulltext_page_roots_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_page_roots
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();
CREATE TRIGGER atomic_fulltext_page_builds_immutable BEFORE UPDATE OR DELETE ON atomic_fulltext_page_builds
FOR EACH ROW EXECUTE FUNCTION atomic_reject_fulltext_mutation();

-- Headers keep their existing bytes and primary source FK. Their root may
-- now be owned by either the legacy source block set or the shared page DAG.
ALTER TABLE atomic_fulltext_projections
    DROP CONSTRAINT atomic_fulltext_projections_manifest_hash_root_hash_fkey;
CREATE FUNCTION atomic_validate_fulltext_projection_root()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_fulltext_blocks
                    WHERE manifest_hash=NEW.manifest_hash AND block_hash=NEW.root_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots
                        WHERE manifest_hash=NEW.manifest_hash AND root_hash=NEW.root_hash) THEN
        RAISE EXCEPTION 'Atomic fulltext header has no retained source root' USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER atomic_fulltext_projection_validate_root BEFORE INSERT ON atomic_fulltext_projections
FOR EACH ROW EXECUTE FUNCTION atomic_validate_fulltext_projection_root();

CREATE FUNCTION atomic_enqueue_fulltext_source_pages(source BYTEA)
RETURNS TABLE(pages_examined BIGINT,candidates_added BIGINT)
LANGUAGE sql SECURITY DEFINER SET search_path FROM CURRENT AS $$
    WITH pages AS MATERIALIZED (
        SELECT p.block_hash FROM atomic_fulltext_pages p WHERE p.created_for=source
    ), added AS (
        INSERT INTO atomic_fulltext_page_garbage(block_hash)
        SELECT p.block_hash FROM pages p
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=source)
        ON CONFLICT DO NOTHING RETURNING 1
    ) SELECT (SELECT count(*) FROM pages),(SELECT count(*) FROM added)
$$;

CREATE FUNCTION atomic_track_fulltext_page_reference()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE page BYTEA;
BEGIN
    IF TG_TABLE_NAME='atomic_fulltext_page_edges' THEN
        IF TG_OP='INSERT' THEN page:=NEW.child_hash; ELSE page:=OLD.child_hash; END IF;
    ELSE
        IF TG_OP='INSERT' THEN page:=NEW.root_hash; ELSE page:=OLD.root_hash; END IF;
    END IF;
    IF TG_OP='INSERT' THEN
        DELETE FROM atomic_fulltext_page_garbage WHERE block_hash=page;
    ELSE
        INSERT INTO atomic_fulltext_page_garbage(block_hash)
        SELECT p.block_hash FROM atomic_fulltext_pages p WHERE p.block_hash=page
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=page)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=page)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=p.created_for)
        ON CONFLICT DO NOTHING;
    END IF;
    RETURN NULL;
END;
$$;
CREATE TRIGGER atomic_fulltext_page_edge_frontier AFTER INSERT OR DELETE ON atomic_fulltext_page_edges
FOR EACH ROW EXECUTE FUNCTION atomic_track_fulltext_page_reference();
CREATE TRIGGER atomic_fulltext_page_root_frontier AFTER INSERT OR DELETE ON atomic_fulltext_page_roots
FOR EACH ROW EXECUTE FUNCTION atomic_track_fulltext_page_reference();

CREATE FUNCTION atomic_retire_fulltext_build()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    -- Canonical retirement of an interrupted build has no explicit finish.
    -- A normal publication/discard performs its counted source pass itself.
    IF NOT EXISTS (SELECT 1 FROM atomic_tree_manifests WHERE manifest_hash=OLD.manifest_hash) THEN
        PERFORM * FROM atomic_enqueue_fulltext_source_pages(OLD.manifest_hash);
    END IF;
    RETURN NULL;
END;
$$;
CREATE TRIGGER atomic_fulltext_build_retirement AFTER DELETE ON atomic_fulltext_page_builds
FOR EACH ROW EXECUTE FUNCTION atomic_retire_fulltext_build();

-- Runtime writers may finish an authenticated publication, not delete search
-- roots/pages. Explicit discard remains a catalog-owner operation.
CREATE FUNCTION atomic_finish_fulltext_build(source BYTEA)
RETURNS TABLE(pages_examined BIGINT,candidates_added BIGINT)
LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_fulltext_projections p
                    JOIN atomic_fulltext_page_roots r
                      ON r.manifest_hash=p.manifest_hash AND r.root_hash=p.root_hash
                   WHERE p.manifest_hash=source) THEN
        RAISE EXCEPTION 'Atomic fulltext build has no published shared root' USING ERRCODE='23503';
    END IF;
    DELETE FROM atomic_fulltext_page_builds WHERE manifest_hash=source;
    RETURN QUERY SELECT * FROM atomic_enqueue_fulltext_source_pages(source);
END;
$$;

-- The preview and collector deliberately use the same bounded, materialized
-- frontier. Removing one parent releases only its direct edges; descendants
-- become eligible in later calls, never a recursive deletion cascade.
CREATE FUNCTION atomic_fulltext_garbage_candidates(maximum_blocks BIGINT)
RETURNS TABLE(manifest_hash BYTEA,block_hash BYTEA)
LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    IF maximum_blocks<1 OR maximum_blocks>4096 THEN
        RAISE EXCEPTION 'Atomic fulltext garbage batch must be between 1 and 4096' USING ERRCODE='22023';
    END IF;
    IF NOT pg_try_advisory_xact_lock(atomic_fulltext_gc_pin_key()) THEN
        RAISE EXCEPTION 'Atomic fulltext build or repair is active' USING ERRCODE='55P03';
    END IF;
    RETURN QUERY
    SELECT candidate.source,candidate.hash FROM (
        SELECT b.manifest_hash AS source,b.block_hash AS hash FROM atomic_fulltext_garbage g
        JOIN atomic_fulltext_blocks b ON b.manifest_hash=g.manifest_hash
        WHERE NOT EXISTS (SELECT 1 FROM atomic_tree_manifests m WHERE m.manifest_hash=b.manifest_hash)
        UNION ALL
        SELECT NULL::BYTEA AS source,p.block_hash AS hash FROM atomic_fulltext_page_garbage g
        JOIN atomic_fulltext_pages p ON p.block_hash=g.block_hash
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_roots r WHERE r.root_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_edges e WHERE e.child_hash=p.block_hash)
          AND NOT EXISTS (SELECT 1 FROM atomic_fulltext_page_builds b WHERE b.manifest_hash=p.created_for)
    ) candidate ORDER BY candidate.source NULLS FIRST,candidate.hash LIMIT maximum_blocks;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_collect_fulltext_garbage(maximum_blocks BIGINT)
RETURNS BIGINT LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE removed BIGINT;
BEGIN
    WITH candidates AS MATERIALIZED (
        SELECT * FROM atomic_fulltext_garbage_candidates(maximum_blocks)
    ), legacy AS (
        DELETE FROM atomic_fulltext_blocks b USING candidates c
        WHERE b.manifest_hash=c.manifest_hash AND b.block_hash=c.block_hash RETURNING 1
    ), shared AS (
        DELETE FROM atomic_fulltext_pages p USING candidates c
        WHERE c.manifest_hash IS NULL AND p.block_hash=c.block_hash RETURNING 1
    ) SELECT count(*) INTO removed FROM (SELECT * FROM legacy UNION ALL SELECT * FROM shared) deleted;
    DELETE FROM atomic_fulltext_garbage g WHERE g.manifest_hash IN (
        SELECT candidate.manifest_hash FROM atomic_fulltext_garbage candidate
        WHERE NOT EXISTS (SELECT 1 FROM atomic_fulltext_blocks b WHERE b.manifest_hash=candidate.manifest_hash)
        ORDER BY candidate.manifest_hash LIMIT maximum_blocks
    );
    RETURN removed;
END;
$$;

-- pg_temp must be explicit and last: otherwise PostgreSQL searches temporary
-- relations before a SECURITY DEFINER function's apparently fixed path.
DO $$
DECLARE installation_schema TEXT:=current_schema(); routine REGPROCEDURE;
BEGIN
    FOREACH routine IN ARRAY ARRAY[
        'atomic_fulltext_gc_pin_key()'::regprocedure,
        'atomic_validate_fulltext_page_source()'::regprocedure,
        'atomic_validate_fulltext_projection_root()'::regprocedure,
        'atomic_enqueue_fulltext_source_pages(bytea)'::regprocedure,
        'atomic_track_fulltext_page_reference()'::regprocedure,
        'atomic_retire_fulltext_build()'::regprocedure,
        'atomic_finish_fulltext_build(bytea)'::regprocedure,
        'atomic_fulltext_garbage_candidates(bigint)'::regprocedure,
        'atomic_collect_fulltext_garbage(bigint)'::regprocedure
    ] LOOP
        EXECUTE format('ALTER FUNCTION %s SET search_path TO %I, pg_catalog, pg_temp',routine,installation_schema);
    END LOOP;
END;
$$;

REVOKE ALL ON atomic_fulltext_pages,atomic_fulltext_page_edges,atomic_fulltext_page_roots,atomic_fulltext_page_builds,atomic_fulltext_page_garbage FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_fulltext_gc_pin_key(),atomic_validate_fulltext_page_source(),atomic_validate_fulltext_projection_root(),atomic_enqueue_fulltext_source_pages(BYTEA),atomic_track_fulltext_page_reference(),atomic_retire_fulltext_build(),atomic_finish_fulltext_build(BYTEA),atomic_fulltext_garbage_candidates(BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_fulltext_gc_pin_key(),atomic_finish_fulltext_build(BYTEA),atomic_fulltext_garbage_candidates(BIGINT) TO CURRENT_USER;
