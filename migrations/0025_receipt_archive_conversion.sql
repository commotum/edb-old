-- Ordinary receipt bases must not permanently pin the accelerator retirement
-- prefix. Keep the exact manifest hash and request bindings, incrementally
-- authenticate its closure, then atomically transfer its ownership to the
-- existing exact-retry archive representation. Only the installation owner
-- can drive this operation; no runtime role gains mutation privileges.
CREATE TABLE atomic_receipt_archive_conversions (
    manifest_hash BYTEA PRIMARY KEY REFERENCES atomic_tree_manifests(manifest_hash),
    database_id TEXT NOT NULL,
    generation BIGINT NOT NULL CHECK (generation > 0),
    publication_revision BIGINT NOT NULL CHECK (publication_revision > 0),
    phase SMALLINT NOT NULL CHECK (phase BETWEEN 1 AND 3),
    node_count BIGINT NOT NULL CHECK (node_count > 0),
    hashed_nodes BIGINT NOT NULL DEFAULT 0 CHECK (hashed_nodes >= 0),
    hash_cursor BYTEA CHECK (octet_length(hash_cursor) = 32),
    hash_state BYTEA,
    started_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    FOREIGN KEY (database_id, generation)
        REFERENCES atomic_log_generations(database_id, generation),
    CHECK (hashed_nodes <= node_count)
);

CREATE TABLE atomic_receipt_archive_frontier (
    manifest_hash BYTEA NOT NULL
        REFERENCES atomic_receipt_archive_conversions(manifest_hash),
    node_hash BYTEA NOT NULL CHECK (octet_length(node_hash) = 32),
    next_child INTEGER NOT NULL DEFAULT 0 CHECK (next_child >= 0),
    PRIMARY KEY (manifest_hash, node_hash)
);

-- A conversion header is deliberately incomplete. Required-root readers
-- count only completed archive owners, so the original publication remains
-- usable between every bounded conversion batch.
CREATE OR REPLACE FUNCTION atomic_validate_request_base_archive_insert()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE conversion_owned BOOLEAN;
BEGIN
    SELECT current_user = pg_catalog.pg_get_userbyid(c.relowner)
       AND pg_catalog.current_setting('atomic.receipt_archive_conversion', true) = 'v25'
       AND EXISTS (
            SELECT 1 FROM atomic_receipt_archive_conversions work
            JOIN atomic_tree_manifests source ON source.manifest_hash=work.manifest_hash
            WHERE work.manifest_hash=NEW.manifest_hash
              AND work.database_id=NEW.database_id AND work.generation=NEW.generation
              AND source.payload=NEW.payload
              AND source.publication_revision=NEW.archive_revision
       ) INTO conversion_owned
      FROM pg_catalog.pg_class c WHERE c.oid=TG_RELID;
    IF pg_catalog.sha256(NEW.payload) <> NEW.manifest_hash
       OR (atomic_request_base_archive_build_live(NEW.database_id,NEW.generation)
               OR conversion_owned) IS NOT TRUE
       OR NOT EXISTS (
           SELECT 1 FROM atomic_semantic_commitment_roots semantic
            WHERE semantic.database_id=NEW.database_id
              AND semantic.generation=NEW.generation AND semantic.basis_t=NEW.basis_t
              AND semantic.tx_hash=NEW.tx_hash AND semantic.state_hash=NEW.state_hash
              AND semantic.eidx_frontier=NEW.eidx_frontier
              AND semantic.commitment_version=2
       ) THEN
        RAISE EXCEPTION 'Atomic request-base archive has no authenticated restore or conversion owner'
            USING ERRCODE='23503';
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION atomic_reject_request_base_archive_mutation()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE relation_owner NAME;
BEGIN
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF TG_OP='DELETE' AND current_user=relation_owner
       AND pg_catalog.current_setting('atomic.request_base_archive_gc',true)='v20' THEN
        RETURN OLD;
    END IF;
    IF TG_OP='UPDATE' AND TG_TABLE_NAME='atomic_request_base_archives'
       AND current_user=relation_owner
       AND pg_catalog.current_setting('atomic.receipt_archive_conversion',true)='v25'
       AND EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions work
                    WHERE work.manifest_hash=OLD.manifest_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_request_base_archive_completions complete
                        WHERE complete.manifest_hash=OLD.manifest_hash)
       AND (to_jsonb(NEW)-'expected_node_count'-'node_set_hash')
          =(to_jsonb(OLD)-'expected_node_count'-'node_set_hash') THEN
        RETURN NEW;
    END IF;
    RAISE EXCEPTION 'Atomic request-base archive records are immutable' USING ERRCODE='55000';
END;
$$;

-- Check and lock the immutable source on every batch. A generation may have
-- retired since staging began; its publication and the conversion dependency
-- still prevent generation GC until ownership has transferred safely.
CREATE FUNCTION atomic_receipt_archive_conversion_context(candidate_hash BYTEA)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE manifest_lock BIGINT; build_lock BIGINT; generation_lock BIGINT;
BEGIN
    IF candidate_hash IS NULL OR octet_length(candidate_hash)<>32 THEN RETURN FALSE; END IF;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RETURN FALSE;
    END IF;
    manifest_lock := (('x'||encode(substring(candidate_hash FROM 1 FOR 8),'hex'))::bit(64)::bigint)
                     # 4707465863597391872::bigint;
    build_lock := (('x'||encode(substring(candidate_hash FROM 1 FOR 8),'hex'))::bit(64)::bigint)
                  # 4707460391809056768::bigint;
    IF NOT pg_catalog.pg_try_advisory_xact_lock(manifest_lock)
       OR NOT pg_catalog.pg_try_advisory_xact_lock(build_lock) THEN RETURN FALSE; END IF;
    SELECT atomic_log_generation_pin_key(p.database_id,p.log_generation)
      INTO generation_lock FROM atomic_tree_publications p
      JOIN atomic_tree_retirements r USING(database_id,publication_revision,manifest_hash)
     WHERE p.manifest_hash=candidate_hash AND r.bookkeeping_complete
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_publications older
                        WHERE older.database_id=p.database_id
                          AND older.publication_revision<p.publication_revision)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_retirement_progress progress
                        WHERE progress.manifest_hash=candidate_hash)
       AND NOT EXISTS (SELECT 1 FROM atomic_tree_build_intents intent
                        WHERE intent.manifest_hash=candidate_hash);
    IF generation_lock IS NULL
       OR NOT pg_catalog.pg_try_advisory_xact_lock_shared(generation_lock) THEN RETURN FALSE; END IF;
    PERFORM set_config('atomic.receipt_archive_conversion','v25',true);
    RETURN TRUE;
END;
$$;

CREATE FUNCTION atomic_begin_receipt_archive_conversion(candidate_hash BYTEA, older_than_millis BIGINT)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE source atomic_tree_manifests%ROWTYPE; initial_count BIGINT;
BEGIN
    IF older_than_millis IS NULL OR older_than_millis<0
       OR NOT atomic_receipt_archive_conversion_context(candidate_hash) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions WHERE manifest_hash=candidate_hash) THEN
        RETURN TRUE;
    END IF;
    SELECT m.* INTO source FROM atomic_tree_manifests m
      JOIN atomic_tree_publications p USING(manifest_hash)
      JOIN atomic_tree_retirements r ON r.manifest_hash=m.manifest_hash
      JOIN atomic_heads head ON head.database_id=p.database_id
                            AND head.log_generation=p.log_generation
     WHERE m.manifest_hash=candidate_hash
       AND r.retired_at<clock_timestamp()-older_than_millis*interval '1 millisecond'
       AND EXISTS (SELECT 1 FROM atomic_generation_request_bases base
                    WHERE base.base_manifest_hash=candidate_hash)
     FOR SHARE OF m;
    IF NOT FOUND OR pg_catalog.sha256(source.payload)<>candidate_hash THEN RETURN FALSE; END IF;
    SELECT count(DISTINCT root_hash) INTO initial_count
      FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    IF initial_count<1 OR (SELECT count(*) FROM atomic_tree_manifest_roots
                           WHERE manifest_hash=candidate_hash)<>8 THEN
        RAISE EXCEPTION 'Atomic archive conversion source has incomplete roots' USING ERRCODE='23514';
    END IF;
    INSERT INTO atomic_receipt_archive_conversions
        (manifest_hash,database_id,generation,publication_revision,phase,node_count)
    VALUES (candidate_hash,source.database_id,source.log_generation,
            source.publication_revision,1,initial_count);
    INSERT INTO atomic_request_base_archives
        (database_id,generation,archive_revision,basis_t,tx_hash,state_hash,eidx_frontier,
         manifest_version,manifest_hash,payload,expected_node_count,node_set_hash)
    VALUES (source.database_id,source.log_generation,source.publication_revision,source.basis_t,
            source.tx_hash,source.state_hash,source.eidx_frontier,source.manifest_version,
            candidate_hash,source.payload,initial_count,decode(repeat('00',32),'hex'));
    INSERT INTO atomic_request_base_archive_roots
        (manifest_hash,index_order,history,root_hash,datom_count,encoded_bytes)
    SELECT manifest_hash,index_order,history,root_hash,datom_count,encoded_bytes
      FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    INSERT INTO atomic_request_base_archive_nodes(manifest_hash,node_hash)
    SELECT DISTINCT candidate_hash,root_hash FROM atomic_tree_manifest_roots WHERE manifest_hash=candidate_hash;
    INSERT INTO atomic_receipt_archive_frontier(manifest_hash,node_hash)
    SELECT manifest_hash,node_hash FROM atomic_request_base_archive_nodes WHERE manifest_hash=candidate_hash;
    RETURN TRUE;
END;
$$;

-- Do not release immutable receipt bindings at the normal-publication delete
-- trigger when the same exact hash has gained completed archive ownership.
CREATE OR REPLACE FUNCTION atomic_release_inactive_request_bases()
RETURNS trigger LANGUAGE plpgsql SET search_path FROM CURRENT AS $$
DECLARE relation_owner NAME;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM atomic_generation_request_bases
                    WHERE base_manifest_hash=OLD.manifest_hash) THEN RETURN OLD; END IF;
    SELECT pg_catalog.pg_get_userbyid(relowner) INTO relation_owner
      FROM pg_catalog.pg_class WHERE oid=TG_RELID;
    IF current_user IS DISTINCT FROM relation_owner
       OR pg_catalog.current_setting('atomic.tree_gc_active',true) IS DISTINCT FROM 'v13' THEN
        RAISE EXCEPTION 'Atomic request base can be released only by tree garbage collection' USING ERRCODE='55000';
    END IF;
    IF pg_catalog.current_setting('atomic.receipt_archive_conversion',true)='v25'
       AND EXISTS (SELECT 1 FROM atomic_request_base_archives archive
                    JOIN atomic_request_base_archive_completions complete USING(manifest_hash)
                    WHERE archive.manifest_hash=OLD.manifest_hash
                      AND archive.database_id=OLD.database_id
                      AND archive.generation=OLD.log_generation) THEN RETURN OLD; END IF;
    IF EXISTS (SELECT 1 FROM atomic_heads WHERE database_id=OLD.database_id
                AND log_generation=OLD.log_generation) THEN
        RAISE EXCEPTION 'Atomic active request base cannot be retired' USING ERRCODE='55000';
    END IF;
    DELETE FROM atomic_generation_request_bases WHERE base_manifest_hash=OLD.manifest_hash;
    RETURN OLD;
END;
$$;

CREATE FUNCTION atomic_finish_receipt_archive_conversion(candidate_hash BYTEA, older_than_millis BIGINT)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
DECLARE work atomic_receipt_archive_conversions%ROWTYPE; retired BOOLEAN;
BEGIN
    IF older_than_millis IS NULL OR older_than_millis<0
       OR NOT atomic_receipt_archive_conversion_context(candidate_hash) THEN RETURN FALSE; END IF;
    SELECT * INTO work FROM atomic_receipt_archive_conversions
     WHERE manifest_hash=candidate_hash FOR UPDATE;
    IF NOT FOUND OR work.phase<>3 OR work.hashed_nodes<>work.node_count
       OR EXISTS (SELECT 1 FROM atomic_receipt_archive_frontier WHERE manifest_hash=candidate_hash)
       OR EXISTS (SELECT 1 FROM atomic_tree_retired_nodes
                   WHERE database_id=work.database_id AND publication_revision=work.publication_revision)
       OR NOT EXISTS (SELECT 1 FROM atomic_request_base_archives archive
                       WHERE archive.manifest_hash=candidate_hash
                         AND archive.expected_node_count=work.node_count
                         AND archive.node_set_hash<>decode(repeat('00',32),'hex')) THEN
        RETURN FALSE;
    END IF;
    -- The incremental owner checked every immutable node, edge, and ordered
    -- hash checkpoint. No final string_agg or full-closure authentication pass.
    INSERT INTO atomic_request_base_archive_completions(manifest_hash) VALUES(candidate_hash);
    -- Drop the source FK before v13 deletes its metadata; no frontier rows
    -- remain, and rollback restores both ownership records if retirement fails.
    DELETE FROM atomic_receipt_archive_conversions WHERE manifest_hash=candidate_hash;
    retired := atomic_collect_tree_retirement_unbound_v13(
        work.database_id,work.publication_revision,candidate_hash,older_than_millis,512);
    IF NOT retired THEN
        RAISE EXCEPTION 'Atomic receipt archive source changed during ownership handoff' USING ERRCODE='40001';
    END IF;
    RETURN TRUE;
END;
$$;

-- A staged archive cannot be collected independently of its source. Its
-- conversion finishes first even if the generation became inactive meanwhile.
ALTER FUNCTION atomic_collect_tree_retirement(TEXT,BIGINT,BYTEA,BIGINT,BIGINT)
    RENAME TO atomic_collect_tree_retirement_unconverted_v22;
CREATE FUNCTION atomic_collect_tree_retirement(
    candidate_database_id TEXT,candidate_publication_revision BIGINT,candidate_manifest_hash BYTEA,
    older_than_millis BIGINT,maximum_nodes BIGINT
)
RETURNS BOOLEAN LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RETURN FALSE;
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions
                WHERE manifest_hash=candidate_manifest_hash) THEN RETURN FALSE; END IF;
    RETURN atomic_collect_tree_retirement_unconverted_v22(
        candidate_database_id,candidate_publication_revision,candidate_manifest_hash,older_than_millis,maximum_nodes);
END;
$$;

ALTER FUNCTION atomic_collect_request_base_archive(TEXT,BIGINT,BYTEA,BIGINT,BIGINT)
    RENAME TO atomic_collect_request_base_archive_unconverted_v20;
CREATE FUNCTION atomic_collect_request_base_archive(
    candidate_database_id TEXT,candidate_generation BIGINT,candidate_manifest_hash BYTEA,
    older_than_millis BIGINT,maximum_nodes BIGINT
)
RETURNS TABLE(rows_removed BIGINT,is_complete BOOLEAN)
LANGUAGE plpgsql SECURITY DEFINER SET search_path FROM CURRENT AS $$
BEGIN
    IF NOT pg_catalog.pg_try_advisory_xact_lock(atomic_semantic_commitment_gc_pin_key()) THEN
        RAISE EXCEPTION 'Atomic receipt archive collection is busy' USING ERRCODE='55006';
    END IF;
    IF EXISTS (SELECT 1 FROM atomic_receipt_archive_conversions
                WHERE manifest_hash=candidate_manifest_hash) THEN
        RAISE EXCEPTION 'Atomic receipt archive conversion is still active' USING ERRCODE='55006';
    END IF;
    RETURN QUERY SELECT * FROM atomic_collect_request_base_archive_unconverted_v20(
        candidate_database_id,candidate_generation,candidate_manifest_hash,older_than_millis,maximum_nodes);
END;
$$;

-- The previous collector's restore-only owner check predates ordinary archive
-- conversion. A retired excision generation is equally valid; unpublished
-- restore/abandonment checks later in that function remain unchanged.
DO $$
DECLARE definition TEXT;
BEGIN
    SELECT pg_get_functiondef('atomic_collect_request_base_archive_unconverted_v20(text,bigint,bytea,bigint,bigint)'::regprocedure)
      INTO definition;
    IF position('AND build_kind IN (0, 2)' IN definition)=0 THEN
        RAISE EXCEPTION 'Unexpected request archive collector definition';
    END IF;
    EXECUTE replace(definition,'AND build_kind IN (0, 2)','AND build_kind IN (0, 1, 2)');
END;
$$;

REVOKE ALL ON atomic_receipt_archive_conversions,atomic_receipt_archive_frontier FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_receipt_archive_conversion_context(BYTEA) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_begin_receipt_archive_conversion(BYTEA,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_finish_receipt_archive_conversion(BYTEA,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_tree_retirement(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_tree_retirement_unconverted_v22(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_request_base_archive(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION atomic_collect_request_base_archive_unconverted_v20(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION atomic_receipt_archive_conversion_context(BYTEA) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_begin_receipt_archive_conversion(BYTEA,BIGINT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_finish_receipt_archive_conversion(BYTEA,BIGINT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_collect_tree_retirement(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) TO CURRENT_USER;
GRANT EXECUTE ON FUNCTION atomic_collect_request_base_archive(TEXT,BIGINT,BYTEA,BIGINT,BIGINT) TO CURRENT_USER;
