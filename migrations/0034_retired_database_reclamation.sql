-- Terminal deletion is distinct from successor-generation collection. The
-- issued identity is never removed, even when its last physical object is gone.
ALTER TABLE atomic_database_identities ADD COLUMN reclaimed_at TIMESTAMPTZ;
CREATE TABLE atomic_database_reclamation_progress (
    database_id TEXT PRIMARY KEY REFERENCES atomic_database_identities(database_id),
    phase INTEGER NOT NULL DEFAULT 0 CHECK(phase>=0),
    -- Array-position phases are versioned storage, not freely reorderable code.
    format_version INTEGER NOT NULL DEFAULT 1 CHECK(format_version=1),
    active_backend INTEGER,
    active_xid BIGINT,
    started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);
CREATE INDEX atomic_database_reclamation_transaction
    ON atomic_database_reclamation_progress(active_backend,active_xid) WHERE active_backend IS NOT NULL;
-- The frontier is a bounded, resumable walk, not a materialized corpus upload.
-- Kinds: native tree, semantic tree, fulltext page, legacy segment, program,
-- legacy manifest, native manifest, receipt archive.
CREATE TABLE atomic_database_reclamation_objects (
    database_id TEXT NOT NULL REFERENCES atomic_database_identities(database_id),
    kind SMALLINT NOT NULL CHECK(kind BETWEEN 1 AND 8),
    object_hash BYTEA NOT NULL CHECK(octet_length(object_hash)=32),
    next_child BIGINT NOT NULL DEFAULT 0 CHECK(next_child>=0),
    expanded BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY(database_id,kind,object_hash)
);
CREATE INDEX atomic_database_reclamation_objects_pending
    ON atomic_database_reclamation_objects(database_id,kind,object_hash) WHERE NOT expanded;
CREATE INDEX atomic_database_reclamation_objects_hash
    ON atomic_database_reclamation_objects(kind,object_hash);

CREATE FUNCTION atomic_database_reclamation_authorized()
RETURNS BOOLEAN LANGUAGE sql SET search_path FROM CURRENT AS $$
    SELECT current_user=pg_get_userbyid(c.relowner) AND EXISTS(
        SELECT 1 FROM atomic_database_reclamation_progress
         WHERE active_backend=pg_backend_pid() AND active_xid=txid_current())
      FROM pg_class c WHERE c.oid='atomic_database_reclamation_progress'::regclass
$$;

-- These existing immutable guards retain their original bodies verbatim.
-- Only an owner-created transaction marker admits the terminal DELETE path;
-- no caller-controlled GUC, trigger disable, runtime grant or mutable bytes.
DO $$ DECLARE proc REGPROCEDURE; definition TEXT; BEGIN
    FOREACH proc IN ARRAY ARRAY[
        'atomic_reject_database_mutation()'::regprocedure,
        'atomic_reject_immutable_mutation()'::regprocedure,
        'atomic_reject_log_generation_gc_mutation()'::regprocedure,
        'atomic_reject_generation_staging_mutation()'::regprocedure,
        'atomic_reject_tree_gc_mutation()'::regprocedure,
        'atomic_reject_semantic_commitment_mutation()'::regprocedure,
        'atomic_reject_generation_request_base_mutation()'::regprocedure,
        'atomic_reject_request_base_archive_mutation()'::regprocedure,
        'atomic_reject_fulltext_mutation()'::regprocedure,
        'atomic_reject_tree_node_block_mutation()'::regprocedure
    ] LOOP
        definition:=pg_get_functiondef(proc);
        definition:=regexp_replace(definition, 'BEGIN',
            E'BEGIN\n    IF TG_OP = ''DELETE'' AND atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;');
        EXECUTE definition;
        EXECUTE format('ALTER FUNCTION %s SET search_path TO %I,pg_catalog,pg_temp',proc,current_schema());
    END LOOP;
END $$;

-- Terminal work already owns a complete page frontier. Suppress duplicate
-- enqueue side effects (including source-wide abandoned-build discovery) only
-- in that transaction; ordinary shared-page GC retains its original protocol.
DO $$ DECLARE proc REGPROCEDURE; definition TEXT; BEGIN
    FOREACH proc IN ARRAY ARRAY['atomic_mark_fulltext_garbage()'::regprocedure,
        'atomic_track_fulltext_page_reference()'::regprocedure,
        'atomic_retire_fulltext_build()'::regprocedure] LOOP
        definition:=pg_get_functiondef(proc);
        definition:=regexp_replace(definition,'BEGIN',
            E'BEGIN\n    IF TG_OP = ''DELETE'' AND atomic_database_reclamation_authorized() THEN RETURN NULL; END IF;');
        EXECUTE definition;
        EXECUTE format('ALTER FUNCTION %s SET search_path TO %I,pg_catalog,pg_temp',proc,current_schema());
    END LOOP;
END $$;

-- Once terminal work has started, an ordinary historical collector must not
-- dismantle a root while the bounded frontier is still discovering children.
CREATE FUNCTION atomic_protect_reclaiming_database()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE target TEXT; object BYTEA; object_kind SMALLINT;
BEGIN
    IF atomic_database_reclamation_authorized() THEN RETURN OLD; END IF;
    IF TG_ARGV[0]='database_id' THEN target:=OLD.database_id; END IF;
    IF TG_ARGV[0]='lease_scope' THEN target:=OLD.lease_scope; END IF;
    IF target IS NOT NULL AND EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=target) THEN
        RAISE EXCEPTION 'Atomic terminal database collection owns this metadata' USING ERRCODE='55P03';
    END IF;
    IF TG_ARGV[1]='manifest_hash' THEN
        object:=OLD.manifest_hash;
        IF EXISTS(SELECT 1 FROM atomic_database_reclamation_objects
                   WHERE kind IN(6,7,8) AND object_hash=object) THEN
            RAISE EXCEPTION 'Atomic terminal database collection owns this manifest' USING ERRCODE='55P03';
        END IF;
    END IF;
    object_kind:=CASE TG_TABLE_NAME WHEN 'atomic_tree_nodes' THEN 1 WHEN 'atomic_semantic_commitment_nodes' THEN 2
        WHEN 'atomic_fulltext_pages' THEN 3 WHEN 'atomic_index_segments' THEN 4 WHEN 'atomic_programs' THEN 5 ELSE NULL END;
    IF object_kind IS NOT NULL THEN
        IF object_kind IN(1,2) THEN object:=OLD.node_hash;
        ELSIF object_kind=3 THEN object:=OLD.block_hash;
        ELSIF object_kind=4 THEN object:=OLD.segment_hash;
        ELSE object:=OLD.program_hash; END IF;
        IF EXISTS(SELECT 1 FROM atomic_database_reclamation_objects WHERE kind=object_kind AND object_hash=object) THEN
            RAISE EXCEPTION 'Atomic terminal database collection owns this object frontier' USING ERRCODE='55P03';
        END IF;
    END IF;
    RETURN OLD;
END;
$$;
DO $$ DECLARE relation REGCLASS; database_column TEXT; manifest_column TEXT; BEGIN
    FOR relation IN SELECT c.oid::regclass FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
        WHERE n.nspname=current_schema() AND c.relkind='r' AND c.relname LIKE 'atomic\_%' ESCAPE '\'
          AND c.relname NOT IN('atomic_database_identities','atomic_database_names',
              'atomic_database_reclamation_progress','atomic_database_reclamation_objects','atomic_schema_migrations')
    LOOP
        SELECT coalesce(max(attname::text),'') INTO database_column FROM pg_attribute
            WHERE attrelid=relation AND attname IN('database_id','lease_scope') AND NOT attisdropped;
        SELECT coalesce(max(attname::text),'') INTO manifest_column FROM pg_attribute
            WHERE attrelid=relation AND attname='manifest_hash' AND NOT attisdropped;
        EXECUTE format('CREATE TRIGGER atomic_terminal_collection_barrier BEFORE DELETE ON %s FOR EACH ROW EXECUTE FUNCTION atomic_protect_reclaiming_database(%L,%L)',relation,database_column,manifest_column);
    END LOOP;
END $$;

CREATE FUNCTION atomic_prepare_database_reclamation(target TEXT,lineage TEXT,age_millis BIGINT,apply BOOLEAN)
RETURNS TABLE(phase INTEGER,complete BOOLEAN)
LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE identity atomic_database_identities%ROWTYPE; owner NAME;
BEGIN
    SELECT pg_get_userbyid(relowner) INTO owner FROM pg_class WHERE oid='atomic_databases'::regclass;
    IF current_user<>owner THEN RAISE EXCEPTION 'Atomic terminal collection requires catalog ownership' USING ERRCODE='42501'; END IF;
    IF age_millis IS NULL OR age_millis<0 THEN RAISE EXCEPTION 'Atomic retirement age must be nonnegative' USING ERRCODE='22023'; END IF;
    -- Publication locks head then identity. Try-only acquisition preserves
    -- that order and never waits holding the reverse side of an in-flight write.
    PERFORM 1 FROM atomic_heads WHERE database_id=target FOR UPDATE NOWAIT;
    SELECT * INTO identity FROM atomic_database_identities WHERE database_id=target FOR UPDATE NOWAIT;
    IF NOT FOUND OR identity.lineage_id<>lineage THEN
        RAISE EXCEPTION 'Atomic retired database identity does not match target' USING ERRCODE='22023';
    END IF;
    IF identity.retired_at IS NULL THEN RAISE EXCEPTION 'Atomic active databases cannot be reclaimed' USING ERRCODE='55000'; END IF;
    IF identity.retired_at>clock_timestamp()-age_millis*interval '1 millisecond' THEN
        RAISE EXCEPTION 'Atomic database has not reached retirement age' USING ERRCODE='55P03';
    END IF;
    IF identity.reclaimed_at IS NOT NULL THEN RETURN QUERY SELECT 2147483647,TRUE; RETURN; END IF;
    IF EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=target AND format_version<>1) THEN
        RAISE EXCEPTION 'Atomic terminal collection progress version is unsupported' USING ERRCODE='55000';
    END IF;
    IF apply THEN
        INSERT INTO atomic_database_reclamation_progress(database_id,active_backend,active_xid)
        VALUES(target,pg_backend_pid(),txid_current()) ON CONFLICT(database_id)
        DO UPDATE SET active_backend=excluded.active_backend,active_xid=excluded.active_xid;
    END IF;
    RETURN QUERY SELECT coalesce((SELECT p.phase FROM atomic_database_reclamation_progress p WHERE p.database_id=target),0),FALSE;
END;
$$;

-- Existing headless-restore abandonment must not leave a live name pointing
-- to a removed locator. It may retire an issued route, never recycle it.
CREATE FUNCTION atomic_finish_removed_database_identity()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
BEGIN
    DELETE FROM atomic_database_names WHERE database_id=OLD.database_id;
    UPDATE atomic_database_identities SET retired_at=coalesce(retired_at,clock_timestamp()),
        reclaimed_at=CASE WHEN EXISTS(SELECT 1 FROM atomic_database_reclamation_progress WHERE database_id=OLD.database_id)
            THEN reclaimed_at ELSE clock_timestamp() END WHERE database_id=OLD.database_id;
    RETURN NULL;
END;
$$;
CREATE TRIGGER atomic_removed_database_identity AFTER DELETE ON atomic_databases
    FOR EACH ROW EXECUTE FUNCTION atomic_finish_removed_database_identity();
DO $$ DECLARE proc REGPROCEDURE; BEGIN
    FOREACH proc IN ARRAY ARRAY['atomic_database_reclamation_authorized()'::regprocedure,
        'atomic_protect_reclaiming_database()'::regprocedure,
        'atomic_prepare_database_reclamation(text,text,bigint,boolean)'::regprocedure,
        'atomic_finish_removed_database_identity()'::regprocedure] LOOP
        EXECUTE format('ALTER FUNCTION %s SET search_path TO %I,pg_catalog,pg_temp',proc,current_schema());
        EXECUTE format('REVOKE ALL ON FUNCTION %s FROM PUBLIC',proc);
    END LOOP;
END $$;
REVOKE ALL ON atomic_database_reclamation_progress,atomic_database_reclamation_objects FROM PUBLIC;
